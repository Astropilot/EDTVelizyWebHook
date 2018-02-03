package workers;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import modeles.EDTJours;
import modeles.EDTSemaines;
import modeles.ReportLevel;
import modeles.timetable;
import utils.Data;
import utils.DiscordMessageWriter;
import utils.FileIO;
import utils.Internet;

/**
 * Classe qui est utilisable dans un Thread et qui va
 * s'occuper de comparer un emploi du temps et d'envoyer
 * les modifications en cas de détection
 * 
 * @author Yohann MARTIN
 *
 */
public class DetectorWorker implements Runnable{

	/**
	 * Le lien URL vers l'emploi du temps (format XML)
	 */
	private String edtURL;
	
	/**
	 * Le lient URL vers le WebHook sur lequel envoyer les changements
	 * d'emploi du temps
	 */
	private String webhookURL;
	
	/**
	 * L'identifiant unique du groupe (celui du site de l'emploi du temps)
	 */
	private String groupID;
	
	/**
	 * Le nom du Worker (généralement c'est l'année suivi du groupe, eg. INFO_2_FA)
	 */
	private String workerName;
	
	/**
	 * Variable volatile permettant de définir si le Thread
	 * doit s'arrêter
	 */
	private volatile boolean running = true;
	
	/**
	 * Constructeur du Worker
	 * 
	 * @param workerName Le nom du Worker (généralement c'est l'année suivi du groupe, eg. INFO_2_FA)
	 * @param groupID L'identifiant unique du groupe (celui du site de l'emploi du temps)
	 * @param webhookURL Le lient URL vers le WebHook sur lequel envoyer les changements d'emploi du temps
	 */
	public DetectorWorker(String workerName, String groupID, String webhookURL) {
		this.groupID = groupID;
		this.edtURL = Data.EDT_ENDPOINT + groupID + Data.EDT_EXTENSION;
		this.webhookURL = webhookURL;
		this.workerName = workerName;
	}
	
	/**
	 * Méthode qui sera executé au démarrage du Thread, il s'agit d'une boucle infinie
	 * qui va vérifier tout les XX secondes l'emploi du temps qui lui à été confié puis
	 * envoyer les changements par un message sur un WebHook
	 */
	@Override
	public void run() {
		while(this.running) {
			
			// on télécharge l'emploi du temps actuel
			String current_EDT = Internet.retrieve(this.edtURL, Data.EDT_ID, Data.EDT_PASSWORD);
			
			// On vérifie que tout c'est bien passé
			if(!current_EDT.contains("<timetable>")) {
				DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-run", "Une erreur est survenue pendant le téléchargement de l'emploi du temps", ReportLevel.FATAL_ERROR);
	            continue;
			}
			
			// On récupère l'ancien EDT
			String old_EDT;
			try {
				old_EDT = FileIO.readFile(Data.EDT_TMP_FOLDER + "oldedt_" + this.groupID);
			} catch (IOException e) {
				System.err.println("[Erreur] Lecture du fichier oldedt_" + this.groupID + ", cause: " + e.getMessage());
				DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-run", e.getMessage(), ReportLevel.WARNING);
				old_EDT = "";
			}
			
			// On prépare l'EDT courant
			current_EDT = current_EDT.substring(current_EDT.indexOf("<timetable>"), current_EDT.length());
			
			// On applique l'algorithme de comparaison
	        compareEDT(old_EDT, current_EDT);

	        // On fait de l'EDT courant l'ancien EDT
	        try {
				FileIO.writeFile(Data.EDT_TMP_FOLDER + "oldedt_" + this.groupID, current_EDT);
			} catch (IOException e) {
				System.err.println("[Erreur] Ecriture du fichier oldedt_" + this.groupID + ", cause: " + e.getMessage());
				DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-run", e.getMessage(), ReportLevel.FATAL_ERROR);
			}
	        
			try {
				Thread.sleep(Data.DELAY_BEFORE_CHECK * 1000);
			} catch (InterruptedException e1) {
				DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-run", e1.getMessage(), ReportLevel.WARNING);
			}
		}
	}
	
	/**
	 * Fonction qui va comparer deux emplois du temps et envoyer
	 * les différences
	 * 
	 * @param oldEDT l'ancien emploi du temps (sauvegarde généralement)
	 * @param currentEDT le nouvel emploi du temps
	 */
	private void compareEDT(String oldEDT, String currentEDT) {
		HashMap<String, List<String>> resultatModifications = new HashMap<String, List<String>>();
		timetable old_EDT;
        timetable current_EDT;
        
        //On sérialise nos deux emploi du temps pour les avoir dans des objets

		Serializer serializer = new Persister();
		try {
		    old_EDT = serializer.read(timetable.class, oldEDT, false);
		    serializer = new Persister();
		    current_EDT = serializer.read(timetable.class, currentEDT, false);
		} catch (Exception e) {
			if(!oldEDT.equals(""))
				DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-compareEDT", e.getMessage(), ReportLevel.WARNING);
		    return;
		}
		
		// On modifie les getRawweeks des cours par la date de la semaine pour les deux emplois du temps
		
		for(EDTJours cours : old_EDT.getEvent())
		{
		    for(EDTSemaines semaine : old_EDT.getSpan())
		    {
		        if(cours.getRawweeks().equals(semaine.getAlleventweeks()))
		            cours.setRawweeks(semaine.getDate());
		    }
		}
		
		for(EDTJours cours : current_EDT.getEvent())
		{
		    for(EDTSemaines semaine : current_EDT.getSpan())
		    {
		        if(cours.getRawweeks().equals(semaine.getAlleventweeks()))
		            cours.setRawweeks(semaine.getDate());
		    }
		}
		
		// On vérifie si nouvelle semaine disponible pour ne pas la regarder.
		// Sinon bah forcement on va trouver une semaine de nouveaux cours et ça fais beaucoup :')

        // On récupère la date de la dernière semaine de l'ancien EDT
        String derniereDate_OldDate = old_EDT.getSpan().get(old_EDT.getSpan().size() - 1).getDate();
        // On regarde si il existe une semaine supérieur dans le nouveau emploi du temps
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        Date OldDate = new Date();
        String semaine_blacklisted = "";
        String semaine_blacklisted2 = "";
        try {
            OldDate = format.parse(derniereDate_OldDate);
        } catch (ParseException e) {
        	DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-compareEDT", e.getMessage(), ReportLevel.WARNING);
            e.printStackTrace();
        }
        for(EDTSemaines semaine : current_EDT.getSpan())
        {
            Date date = new Date();
            try {
                date = format.parse(semaine.getDate());
            } catch (ParseException e) {
            	DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-compareEDT", e.getMessage(), ReportLevel.WARNING);
                e.printStackTrace();
            }
            if(OldDate.compareTo(date) == -1) {
                semaine_blacklisted = semaine.getDate();
                semaine_blacklisted2 = old_EDT.getSpan().get(0).getDate();
            }
        }
        
        // Liste des cours de l'ancien edt non présent dans le nouveau
        List<EDTJours> liste_cours_non_edt_courant = new ArrayList<>();
        // Liste des cours du nouveau edt non présent dans l'ancien
        List<EDTJours> liste_cours_non_edt_ancien = new ArrayList<>();

        // On ajoute les anciens cours dans la première liste si ils ne sont pas dans le nouveau EDT
        for(EDTJours cours : old_EDT.getEvent()) {
            boolean found = false;
            for(EDTJours cours2 : current_EDT.getEvent()) {
                if( cours.getRawweeks().equals(cours2.getRawweeks()) && cours.getStarttime().equals(cours2.getStarttime()) && cours.getEndtime().equals(cours2.getEndtime()) && cours.getDay().equals(cours2.getDay())) {
                    found = true;
                }
            }
            if(!found && !cours.getRawweeks().equals(semaine_blacklisted2)) liste_cours_non_edt_courant.add(cours);
        }

        // On ajoute les nouveaux cours dans la deuxième liste si ils ne sont pas dans l'ancien EDT
        // On ignore les cours qui sont dans la semaine black listée
        for(EDTJours cours : current_EDT.getEvent()) {
            boolean found = false;
            for(EDTJours cours2 : old_EDT.getEvent()) {
                if( cours.getRawweeks().equals(cours2.getRawweeks()) && cours.getStarttime().equals(cours2.getStarttime()) && cours.getEndtime().equals(cours2.getEndtime()) && cours.getDay().equals(cours2.getDay())) {
                    found = true;
                }
            }
            if(!found && !cours.getRawweeks().equals(semaine_blacklisted)) liste_cours_non_edt_ancien.add(cours);
        }

        // On vérifie si Suppressions

        // C'est le reste des éléments du liste_cours_non_edt_courant
        for(EDTJours cours : liste_cours_non_edt_courant) {
            cours.setAjout(false);
            addCoursToList(resultatModifications, cours, "- ");
        }

        // On vérifie si Ajout

        // C'est le reste des éléments du liste_cours_non_edt_ancien
        for(EDTJours cours : liste_cours_non_edt_ancien) {
            cours.setAjout(true);
            addCoursToList(resultatModifications, cours, "+ ");
        }
        
        // On affiche maintenant le résultat sur le WebHook Discord si on a des résultats
        
        if(!resultatModifications.isEmpty())
        	DiscordMessageWriter.writeDiscordWebHookEDT(this.workerName, this.edtURL, this.webhookURL, resultatModifications);
	}
	
	/**
	 * Permet d'ajouter un cours modifié à la liste des modifications
	 * selon le jour
	 * 
	 * @param allModifs La liste des évènements par jour
	 * @param cours Le cours à rajouter
	 * @param event Le type d'évènement (Ajout: "+ " ou Suppression: "- ")
	 */
	private void addCoursToList(HashMap<String, List<String>> allModifs, EDTJours cours, String event) {
		String key = createFullDate(cours);
		if(!allModifs.containsKey(key)) {
			allModifs.put(key, new ArrayList<String>());
		}
		if(cours.getResources().getModule() != null) {
			allModifs.get(key).add(event + cours.getResources().getModule().toString() + " (" + cours.getPrettytimes() + ")");
		}
		else {
			allModifs.get(key).add(event + "[Cours sans nom] (" + cours.getPrettytimes() + ")");
		}
	}
	
	/**
	 * Permet de générer la date complète d'un cours
	 * 
	 * @param cours Le cours dont il faut générer la date
	 * @return Retourne la date au format "NomDuJour NumeroDuJour NomDuMois Annee", eg. Vendredi 2 Février 2018. En cas d'échec de conversion, la date
	 * en récupérée de l'emploi du temps sera affichée
	 */
	private String createFullDate(EDTJours cours) {
		String dateNotFormatted = "";
		String fullDate = "";
		// Ajoute le jour (début de la semaine + le jour dans la semaine)
        dateNotFormatted += String.valueOf(Integer.parseInt(cours.getRawweeks().substring(0, 2)) + Integer.parseInt(cours.getDay()));
        // Ajoute le reste de la date
        dateNotFormatted += cours.getRawweeks().substring(2, cours.getRawweeks().length());
        // On converti la date (format: dd/MM/yyyy) au format EEEE dd/MM/yyyy où EEEE donne le nom du jour !
        DateFormat sourceFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        try {
            Date date = sourceFormat.parse(dateNotFormatted);
            fullDate += new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE).format(date);
        } catch (ParseException e) {
        	DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-createFullDate", e.getMessage(), ReportLevel.WARNING);
            fullDate += dateNotFormatted;
            return fullDate;
        }
		
		return fullDate;
	}
	
	/**
	 * Permet de terminer un Worker
	 */
	public void terminate() {
		running = false;
	}
	
	/**
	 * Fonction fournissant le nom du Worker
	 * 
	 * @return Le nom du Worker
	 */
	public String getWorkerName() {
		return this.workerName;
	}

}
