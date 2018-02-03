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
 * Classe contenant des m�thodes statiques permettant
 * des requ�tes HTTP (Librairie OkHttp3)
 * 
 * @author Yohann MARTIN
 *
 */
public class Internet {
	
	/**
	 * Champ statique d�finissant le corps d'une requ�te HTTP au format JSON
	 */
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	/**
     * Permet de faire une requ�te sur une URL sp�cifique avec un couple (ID, MDP)
     * et de r�cup�rer le contenu de la page
     *
     * @param url l'URL de la page
     * @param cID l'identifiant de s�curit�
     * @param cPass le mot de passe de s�curit�
     * @return le contenu de la page web
     */
    public static String retrieve(String url, String cID, String cPass) {

        // On d�marre une requete sur la page donn�e avec les identifiants de s�curit�
        // donn�s, puis on retourne la r�ponse ou "" si la requ�te � �chou�e
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
     * Permet d'envoyer une requ�te HTTP de type POST avec un contenu
     * au format JSON
     * 
     * @param url l'URL de la page
     * @param jsonBody le contenu de la requ�te au format JSON
     * @throws IOException Retourne une exception en cas d'�chec
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
