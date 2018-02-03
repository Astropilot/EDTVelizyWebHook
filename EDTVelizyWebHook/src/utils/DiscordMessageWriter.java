package utils;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import modeles.DiscordEmbed;
import modeles.ReportLevel;

/**
 * Classe contenant des fonctions statiques permettant l'envois de message
 * depuis un Discord WebHook
 * 
 * @author Yohann MARTIN
 *
 */
public class DiscordMessageWriter {
	
	/**
	 * Fonction permettant d'écrire un message riche sur un WebHook
	 * à partir de modifications d'emploi du temps effectuée pour un groupe
	 * 
	 * @param nameWorker Le nom du Worker (généralement l'année et le groupe, eg. INFO_2_FA)
	 * @param EdtUrl Un lien vers l'emploi du temps du groupe en question
	 * @param webhookUrl Un lien vers le WebHook Discord sur lequel envoyer le message
	 * @param allModifs La liste des modifications pour chaque jour modifié
	 */
	public static void writeDiscordWebHookEDT(String nameWorker, String EdtUrl, String webhookUrl, Map<String, List<String>> allModifs) {
		JSONObject sender=new JSONObject();
		JSONArray embeds = new JSONArray();
		
		DiscordEmbed embedMessage = new DiscordEmbed();
		embedMessage.setTitle(String.format("Changement de l'emploi du temps __*%s*__", nameWorker));
		embedMessage.setURL(EdtUrl);
		embedMessage.setColor(Data.COLOR_EMBED_MESSAGE);
		embedMessage.setTimestamp(Time.getNowTimestamp());
		embedMessage.setFooterText("Changement détecté le");
		embedMessage.setThumbnailURL("http://i.epvpimg.com/iqfidab.png");
		embedMessage.setAuthor("EDTVelizy WebHook", "https://play.google.com/store/apps/details?id=com.edt.velizy.edtvelizy&hl=fr", "http://i.epvpimg.com/iqfidab.png");
		
		allModifs = sortHashMap(allModifs);
		for(String day : allModifs.keySet()) {
			StringBuilder allCourses = new StringBuilder();
			for(String eventCourse : allModifs.get(day))
				allCourses.append(eventCourse + "\n");
			embedMessage.addField(day, String.format("```diff\n%s```", allCourses));
		}
		
		embeds.add(embedMessage.getJSONObject());
		sender.put("embeds", embeds);
		
		try {
			Internet.sendPostRequest(webhookUrl, convertJSONToText(sender));
		} catch(IOException e) {
			writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DiscordMessageWriter-writeDiscordWebHookEDT", e.getMessage(), ReportLevel.FATAL_ERROR);
		}
	}
	
	/**
	 * Fonction permettant d'écrire un message riche sur un WebHook
	 * pour afficher un rapport de plantage
	 * 
	 * @param url Le lien du WebHook sur lequel envoyer le message
	 * @param source La source du plantage (généralement la classe suivi de la méthode dans laquelle c'est déclenché le plantage)
	 * @param crashTrace La trace la plus complète possible du plantage pour obtenir les informations le concernant
	 * @param reportLevel Le niveau de dangerosité du plantage (Informatio, Attention, Erreur Fatale)
	 */
	public static void writeDiscordWebHookCrashReport(String url, String source, String crashTrace, ReportLevel reportLevel) {
		if(!Data.USE_DEVELOPPER_MODE) {
			System.err.println(String.format("[%s] %s", source, crashTrace));
			return;
		}
		
		JSONObject sender=new JSONObject();
		JSONArray embeds = new JSONArray();
		
		DiscordEmbed embedMessage = new DiscordEmbed();
		embedMessage.setTitle(String.format("Rapport de crash dans __*%s*__", source));
		embedMessage.setDescription(crashTrace);
		embedMessage.setTimestamp(Time.getNowTimestamp());
		embedMessage.setFooterText("CrashReport généré le");
		embedMessage.setThumbnailURL("http://www.squishable.com/user_gallery/mini_squish_ghost_7/360s/mini_squish_ghost_7_design.jpg");
		embedMessage.setAuthor("EDTVelizy Ghost Debugger", "https://github.com/", "http://www.squishable.com/user_gallery/mini_squish_ghost_7/360s/mini_squish_ghost_7_design.jpg");
		switch (reportLevel) {
		case INFO:
			embedMessage.setColor(5412047);		// Bleu
			break;
		case WARNING:
			embedMessage.setColor(14587196);	// Orange
			break;
		case FATAL_ERROR:
			embedMessage.setColor(14564412);	// Rouge
			break;
		}
		
		embeds.add(embedMessage.getJSONObject());
		sender.put("embeds", embeds);
		
		try {
			Internet.sendPostRequest(url, convertJSONToText(sender));
		} catch(IOException e) {
			System.err.println(String.format("[%s] %s", source, crashTrace));
		}
	}
	
	/**
	 * Fonction permettant de convertir un objet JSON en son équivalent textuel
	 * 
	 * @param jsonObject l'objet JSON à convertir
	 * @return Retourne son équivalent textuel
	 * @throws IOException Déclenche une exception si la conversion à échouée
	 */
	private static String convertJSONToText(JSONObject jsonObject) throws IOException {
		StringWriter out = new StringWriter();
		jsonObject.writeJSONString(out);
		String jsonText = out.toString();
		out.close();
		return jsonText;
	}

	/**
	 * Permet de convertir une HashMap en TreeMap (donc trier la liste selon les dates)
	 * 
	 * @param hashMap la HashMap contenant les évènements pour chaque jour
	 * @return Retourne une TreeMap ou les jours sont triés
	 */
	private static TreeMap<String, List<String>> sortHashMap(Map<String, List<String>> hashMap) {
		TreeMap<String, List<String>> sortedMap = new TreeMap<String, List<String>>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
            	Date d1;
            	try {
					d1 = Time.convertStringDateToDate(o1);
				} catch (ParseException e) {
					d1 = new Date();
				}
            	Date d2;
            	try {
					d2 = Time.convertStringDateToDate(o2);
				} catch (ParseException e) {
					d2 = new Date();
					d2.setMinutes(d2.getMinutes()-1);
				}
                return d1.compareTo(d2);
            }

        });
		
		sortedMap.putAll(hashMap);
		
		return sortedMap;
	}
}
