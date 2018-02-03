package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Classe fournissant des méthodes statiques pour gérer
 * le temps
 * 
 * @author Yohann MARTIN
 *
 */
public class Time {

	/**
	 * Calcul le timestamp (ISO8601) du moment ou l'appel à la fonction
	 * est faite
	 * 
	 * @return le timestamp a la norme ISO8601
	 */
	public static String getNowTimestamp() {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
        .withZone(ZoneOffset.UTC)
        .format(Instant.now());
	}
	
	/**
	 * Converti une date au format texte en objet Date
	 * 
	 * @param stringDate La date au format "NomDuJour NumeroDuJour NomDuMois Annee"
	 * @return Retourne l'objet Date correspondant
	 * @throws ParseException Lève une exception si la conversion à échouée
	 */
	public static Date convertStringDateToDate(String stringDate) throws ParseException {
		return new SimpleDateFormat("EEEE dd MMMM yyyy", Locale.FRANCE).parse(stringDate);
	}
}
