package utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Classe contenant des m�thodes statiques pour
 * lire et �crire des fichiers
 * 
 * @author Yohann MARTIN
 *
 */
public class FileIO {

	/**
	 * Fonction permettant de lire un fichier et de retourner
	 * son contenu au format texte
	 * 
	 * @param path le chemin du fichier
	 * @return Le contenu du fichier au format texte
	 * @throws IOException Lance une exception si la lecture a �chou�e
	 */
	public static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, StandardCharsets.UTF_8);
	}
	
	/**
	 * Fonction permettant d'�crire du texte dans un fichier.
	 * L'�criture remplace le contenu du fichier
	 * 
	 * @param path le chemin du fichier � �crire
	 * @param content le contenu texte � �crire
	 * @throws IOException Lance une exception si l'�criture � �chou�e
	 */
	public static void writeFile(String path, String content) throws IOException {
		Files.write( Paths.get(path), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
	}
	
}
