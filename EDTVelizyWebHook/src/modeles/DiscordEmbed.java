package modeles;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Classe représentant et permettant de générer un message Discord Riche en JSON
 * 
 * @author Yohann MARTIN
 *
 */
public class DiscordEmbed {
	
	/**
	 * Le message riche en JSON
	 */
	private JSONObject embed;
	
	/**
	 * Les champs du message riche en JSON
	 */
	private JSONArray fields;
	
	/**
	 * Le constructeur qui initialise le message
	 */
	public DiscordEmbed() {
		this.embed = new JSONObject();
		this.fields = new JSONArray();
		this.embed.put("fields", fields);
	}
	
	/**
	 * Définit le titre du message
	 * 
	 * @param title Le titre du message
	 */
	public void setTitle(String title) {
		this.embed.put("title", title);
	}
	
	/**
	 * Définit la description du message
	 * 
	 * @param description La description du message
	 */
	public void setDescription(String description) {
		this.embed.put("description", description);
	}
	
	/**
	 * Définit le lien URL du titre du message
	 * 
	 * @param url Le lien URL
	 */
	public void setURL(String url) {
		this.embed.put("url", url);
	}
	
	/**
	 * Définit la couleur du message
	 * 
	 * @param color La couleur au format RGB en décimal
	 */
	public void setColor(long color) {
		this.embed.put("color", color);
	}
	
	/**
	 * Définit le timestamp du message (la date de génération du message généralement)
	 * 
	 * @param timestamp Le timestamp au format ISO8601
	 */
	public void setTimestamp(String timestamp) {
		this.embed.put("timestamp", timestamp);
	}
	
	/**
	 * Définit le pied de page du message
	 * 
	 * @param text Le pied de page
	 */
	public void setFooterText(String text) {
		JSONObject footer = new JSONObject();
		footer.put("text", text);
		this.embed.put("footer", footer);
	}
	
	/**
	 * Définit la vignette du message
	 * 
	 * @param url Le lien URL vers la vignette
	 */
	public void setThumbnailURL(String url) {
		JSONObject thumbnail = new JSONObject();
		thumbnail.put("url", url);
		this.embed.put("thumbnail", thumbnail);
	}
	
	/**
	 * Définit l'auteur du message
	 * 
	 * @param name Le nom de l'auteur
	 * @param url Le lien URL vers la page de l'auteur
	 * @param icon_url L'avatar de l'auteur
	 */
	public void setAuthor(String name, String url, String icon_url) {
		JSONObject author = new JSONObject();
		author.put("name", name);
		author.put("url", url);
		author.put("icon_url", icon_url);
		this.embed.put("author", author);
	}
	
	/**
	 * Ajoute un champ au message
	 * 
	 * @param name Le nom du champ
	 * @param value Son contenu
	 */
	public void addField(String name, String value) {
		JSONObject field = new JSONObject();
		field.put("name", name);
		field.put("value", value);
		this.fields.add(field);
	}
	
	/**
	 * Retourne l'objet complet généré au format JSON
	 * 
	 * @return le message riche au format JSON
	 */
	public JSONObject getJSONObject() {
		return this.embed;
	}
}
