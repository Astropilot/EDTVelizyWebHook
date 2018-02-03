package utils;

import java.io.IOException;

import modeles.ReportLevel;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 
 * Classe contenant des méthodes statiques permettant
 * des requêtes HTTP (Librairie OkHttp3)
 * 
 * @author Yohann MARTIN
 *
 */
public class Internet {
	
	/**
	 * Champ statique définissant le corps d'une requête HTTP au format JSON
	 */
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	/**
     * Permet de faire une requête sur une URL spécifique avec un couple (ID, MDP)
     * et de récupérer le contenu de la page
     *
     * @param url l'URL de la page
     * @param cID l'identifiant de sécurité
     * @param cPass le mot de passe de sécurité
     * @return le contenu de la page web
     */
    public static String retrieve(String url, String cID, String cPass) {

        // On démarre une requete sur la page donnée avec les identifiants de sécurité
        // donnés, puis on retourne la réponse ou "" si la requête à échouée
        OkHttpClient client = new OkHttpClient();
        String credential = Credentials.basic(cID, cPass);
        Request request = new Request.Builder()
                .header("Authorization", credential)
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        }
        catch (IOException e) {
        	DiscordMessageWriter.writeDiscordWebHookCrashReport(Data.WEBHOOK_DEVELOPER, "DetectorWorker-run", e.getMessage(), ReportLevel.FATAL_ERROR);
            return "";
        }
    }
    
    /**
     * Permet d'envoyer une requête HTTP de type POST avec un contenu
     * au format JSON
     * 
     * @param url l'URL de la page
     * @param jsonBody le contenu de la requête au format JSON
     * @throws IOException Retourne une exception en cas d'échec
     */
    public static void sendPostRequest(String url, String jsonBody) throws IOException {
    	OkHttpClient client = new OkHttpClient();
    	RequestBody body = RequestBody.create(JSON, jsonBody);
    	  Request request = new Request.Builder()
    	      .url(url)
    	      .post(body)
    	      .build();
    	Response response = client.newCall(request).execute();
    	response.close();
    }
}
