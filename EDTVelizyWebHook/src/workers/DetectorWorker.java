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
 * les modifications en cas de d�tection
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
	 * Le nom du Worker (g�n�ralement c'est l'ann�e suivi du groupe, eg. INFO_2_FA)
	 */
	private String workerName;
	
	/**
	 * Variable volatile permettant de d�finir si le Thread
	 * doit s'arr�ter
	 */
	private volatile boolean running = true;
	
	/**
	 * Constructeur du Worker
	 * 
	 * @param workerName Le nom du Worker (g�n�ralement c'est l'ann�e suivi du groupe, eg. INFO_2_FA)
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
	 * M�thode qui sera execut� au d�marrage du Thread, il s'agit d'une boucle infinie
	 * qui va v�rifier tout les XX secondes l'emploi du temps qui lui � �t� confi� puis
	 * envoyer les changements par un message sur un WebHook
	 */
	@Override
	public void run() {
		while(this.running) {
			
			// on t�l�charge l'emploi du temps actuel
			String current_EDT = Internet.retrieve(this.edtURL, Data.EDT_ID, Data.EDT_PASSWORD);
			
			// On v�rifie que tout c'est bien pass�
			if(!current_EDT.contains("<timetable>")) {
				DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-run", "Une erreur est survenue pendant le t�l�chargement de l'emploi du temps", ReportLevel.FATAL_ERROR);
	            continue;
			}
			
			// On r�cup�re l'ancien EDT
			String old_EDT;
			try {
				old_EDT = FileIO.readFile(Data.EDT_TMP_FOLDER + "oldedt_" + this.groupID);
			} catch (IOException e) {
				System.err.println("[Erreur] Lecture du fichier oldedt_" + this.groupID + ", cause: " + e.getMessage());
				DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-run", e.getMessage(), ReportLevel.WARNING);
				old_EDT = "";
			}
			
			// On pr�pare l'EDT courant
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
	 * les diff�rences
	 * 
	 * @param oldEDT l'ancien emploi du temps (sauvegarde g�n�ralement)
	 * @param currentEDT le nouvel emploi du temps
	 */
	private void compareEDT(String oldEDT, String currentEDT) {
		HashMap<String, List<String>> resultatModifications = new HashMap<String, List<String>>();
		timetable old_EDT;
        timetable current_EDT;
        
        //On s�rialise nos deux emploi du temps pour les avoir dans des objets

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
		
		// On v�rifie si nouvelle semaine disponible pour ne pas la regarder.
		// Sinon bah forcement on va trouver une semaine de nouveaux cours et �a fais beaucoup :')

        // On r�cup�re la date de la derni�re semaine de l'ancien EDT
        String derniereDate_OldDate = old_EDT.getSpan().get(old_EDT.getSpan().size() - 1).getDate();
        // On regarde si il existe une semaine sup�rieur dans le nouveau emploi du temps
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
        
        // Liste des cours de l'ancien edt non pr�sent dans le nouveau
        List<EDTJours> liste_cours_non_edt_courant = new ArrayList<>();
        // Liste des cours du nouveau edt non pr�sent dans l'ancien
        List<EDTJours> liste_cours_non_edt_ancien = new ArrayList<>();

        // On ajoute les anciens cours dans la premi�re liste si ils ne sont pas dans le nouveau EDT
        for(EDTJours cours : old_EDT.getEvent()) {
            boolean found = false;
            for(EDTJours cours2 : current_EDT.getEvent()) {
                if( cours.getRawweeks().equals(cours2.getRawweeks()) && cours.getStarttime().equals(cours2.getStarttime()) && cours.getEndtime().equals(cours2.getEndtime()) && cours.getDay().equals(cours2.getDay())) {
                    found = true;
                }
            }
            if(!found && !cours.getRawweeks().equals(semaine_blacklisted2)) liste_cours_non_edt_courant.add(cours);
        }

        // On ajoute les nouveaux cours dans la deuxi�me liste si ils ne sont pas dans l'ancien EDT
        // On ignore les cours qui sont dans la semaine black list�e
        for(EDTJours cours : current_EDT.getEvent()) {
            boolean found = false;
            for(EDTJours cours2 : old_EDT.getEvent()) {
                if( cours.getRawweeks().equals(cours2.getRawweeks()) && cours.getStarttime().equals(cours2.getStarttime()) && cours.getEndtime().equals(cours2.getEndtime()) && cours.getDay().equals(cours2.getDay())) {
                    found = true;
                }
            }
            if(!found && !cours.getRawweeks().equals(semaine_blacklisted)) liste_cours_non_edt_ancien.add(cours);
        }

        // On v�rifie si Suppressions

        // C'est le reste des �l�ments du liste_cours_non_edt_courant
        for(EDTJours cours : liste_cours_non_edt_courant) {
            cours.setAjout(false);
            addCoursToList(resultatModifications, cours, "- ");
        }

        // On v�rifie si Ajout

        // C'est le reste des �l�ments du liste_cours_non_edt_ancien
        for(EDTJours cours : liste_cours_non_edt_ancien) {
            cours.setAjout(true);
            addCoursToList(resultatModifications, cours, "+ ");
        }
        
        // On affiche maintenant le r�sultat sur le WebHook Discord si on a des r�sultats
        
        if(!resultatModifications.isEmpty())
        	DiscordMessageWriter.writeDiscordWebHookEDT(this.workerName, this.edtURL, this.webhookURL, resultatModifications);
	}
	
	/**
	 * Permet d'ajouter un cours modifi� � la liste des modifications
	 * selon le jour
	 * 
	 * @param allModifs La liste des �v�nements par jour
	 * @param cours Le cours � rajouter
	 * @param event Le type d'�v�nement (Ajout: "+ " ou Suppression: "- ")
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
	 * Permet de g�n�rer la date compl�te d'un cours
	 * 
	 * @param cours Le cours dont il faut g�n�rer la date
	 * @return Retourne la date au format "NomDuJour NumeroDuJour NomDuMois Annee", eg. Vendredi 2 F�vrier 2018. En cas d'�chec de conversion, la date
	 * en r�cup�r�e de l'emploi du temps sera affich�e
	 */
	private String createFullDate(EDTJours cours) {
		String dateNotFormatted = "";
		String fullDate = "";
		// Ajoute le jour (d�but de la semaine + le jour dans la semaine)
        dateNotFormatted += String.valueOf(Integer.parseInt(cours.getRawweeks().substring(0, 2)) + Integer.parseInt(cours.getDay()));
        // Ajoute le reste de la date
        dateNotFormatted += cours.getRawweeks().substring(2, cours.getRawweeks().length());
        // On converti la date (format: dd/MM/yyyy) au format EEEE dd/MM/yyyy o� EEEE donne le nom du jour !
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
