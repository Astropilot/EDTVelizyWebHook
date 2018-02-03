package utils;

/**
 * Classe contenant des constantes d�crivant g�n�ralements les param�tres du
 * programme. Cette centralisation permet de modifier rapidement les param�tres.
 * 
 * @author Yohann MARTIN
 *
 */
public class Data {

	/*
	 * 
	 * Partie Generale
	 * 
	 */
	
	public static final String EDT_ID = "";				// L'identifiant pour se connecter au site de l'emploi du temps
	public static final String EDT_PASSWORD = "";			// Le mot de passe pour se connecter au site de l'emploi du temps
	public static final String EDT_ENDPOINT = "http://satis.iut-velizy.uvsq.fr/EDT/";	// Le lien des emploi du temps
	public static final String EDT_EXTENSION = ".xml";			// L'extension du fichier d'emploi du temps
	
	public static final String EDT_TMP_FOLDER = "tmp_edt/";		// Le dossier ou sera contenu les derniers emplois du temps t�l�charg�s.
	
	public static final int DELAY_BEFORE_CHECK = 900; 			// Temps en secondes entre chaque v�rification d'emploi du temps (par d�faut: 15min).
	
	public static final long COLOR_EMBED_MESSAGE = 12866584;	// La couleur du message Discord (couleur RGB en d�cimal)
	
	/*
	 * 
	 * Partie Workers
	 * 
	 */
	
	public static final String ID_INFO1_A = "g534";				//
	public static final String ID_INFO1_B = "g535";				//
	public static final String ID_INFO1_C = "g536";				//
	public static final String ID_INFO2_A = "g531";				// Les identifiants des groupes du site web de l'EDT.
	public static final String ID_INFO2_B = "g532";				//
	public static final String ID_INFO2_FA = "g2563";			//
	
	public static final String NAME_WORKER1 = "INFO 1 - A";		//
	public static final String NAME_WORKER2 = "INFO 1 - B";		//
	public static final String NAME_WORKER3 = "INFO 1 - C";		// Les noms des workers (le nom des groupes en somme)
	public static final String NAME_WORKER4 = "INFO 2 - A";		//
	public static final String NAME_WORKER5 = "INFO 2 - B";		//
	public static final String NAME_WORKER6 = "INFO 2 - FA";	//
	
	public static final String WEBHOOK_INFO1_A = "";			//
	public static final String WEBHOOK_INFO1_B = "";			//
	public static final String WEBHOOK_INFO1_C = "";			// Les URL des webhooks d�finis sur le serveur discord pour
	public static final String WEBHOOK_INFO2_A = "";			// chaque groupe.
	public static final String WEBHOOK_INFO2_B = "";			//
	public static final String WEBHOOK_INFO2_FA = "";			//
	
	/*
	 * 
	 * Partie Debogueur
	 * 
	 */
	
	public static final boolean USE_DEVELOPPER_MODE = true;		// Activer les rapports de plantage � travers Discord
	public static final String WEBHOOK_DEVELOPER = "";			// L'URL du webhook destin� � recevoir tout les rapport de plantage
}
