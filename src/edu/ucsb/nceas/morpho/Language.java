package edu.ucsb.nceas.morpho;

import java.util.Locale;
import java.util.ResourceBundle;

public class Language {
	
	private ResourceBundle lan = null;
	
	private static Language instance = null;
	
	private Language() {
		//Locale locale = new Locale("en", "US");
		//Locale locale = new Locale("zh", "TW");
		//Locale locale = new Locale("zh", "CN");
		//lan = ResourceBundle.getBundle("language.Messages", locale);
		lan = ResourceBundle.getBundle("language.Messages");
	}
	
	public static Language getInstance() {
		if (instance == null) {
			instance = new Language();
		}
		return instance;
	}
	
	public String getMessages(String  lanId) {
		String messages = lan.getString(lanId);
		return messages;
	}

}
