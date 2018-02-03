package modeles;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Classe repr�sentant et permettant de g�n�rer un message Discord Riche en JSON
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
	 * D�finit le titre du message
	 * 
	 * @param title Le titre du message
	 */
	public void setTitle(String title) {
		this.embed.put("title", title);
	}
	
	/**
	 * D�finit la description du message
	 * 
	 * @param description La description du message
	 */
	public void setDescription(String description) {
		this.embed.put("description", description);
	}
	
	/**
	 * D�finit le lien URL du titre du message
	 * 
	 * @param url Le lien URL
	 */
	public void setURL(String url) {
		this.embed.put("url", url);
	}
	
	/**
	 * D�finit la couleur du message
	 * 
	 * @param color La couleur au format RGB en d�cimal
	 */
	public void setColor(long color) {
		this.embed.put("color", color);
	}
	
	/**
	 * D�finit le timestamp du message (la date de g�n�ration du message g�n�ralement)
	 * 
	 * @param timestamp Le timestamp au format ISO8601
	 */
	public void setTimestamp(String timestamp) {
		this.embed.put("timestamp", timestamp);
	}
	
	/**
	 * D�finit le pied de page du message
	 * 
	 * @param text Le pied de page
	 */
	public void setFooterText(String text) {
		JSONObject footer = new JSONObject();
		footer.put("text", text);
		this.embed.put("footer", footer);
	}
	
	/**
	 * D�finit la vignette du message
	 * 
	 * @param url Le lien URL vers la vignette
	 */
	public void setThumbnailURL(String url) {
		JSONObject thumbnail = new JSONObject();
		thumbnail.put("url", url);
		this.embed.put("thumbnail", thumbnail);
	}
	
	/**
	 * D�finit l'auteur du message
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
	 * Retourne l'objet complet g�n�r� au format JSON
	 * 
	 * @return le message riche au format JSON
	 */
	public JSONObject getJSONObject() {
		return this.embed;
	}
}
