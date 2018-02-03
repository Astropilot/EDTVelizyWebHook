package utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Classe contenant des méthodes statiques pour
 * lire et écrire des fichiers
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
	 * @throws IOException Lance une exception si la lecture a échouée
	 */
	public static String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, StandardCharsets.UTF_8);
	}
	
	/**
	 * Fonction permettant d'écrire du texte dans un fichier.
	 * L'écriture remplace le contenu du fichier
	 * 
	 * @param path le chemin du fichier à écrire
	 * @param content le contenu texte à écrire
	 * @throws IOException Lance une exception si l'écriture à échouée
	 */
	public static void writeFile(String path, String content) throws IOException {
		Files.write( Paths.get(path), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
	}
	
}
