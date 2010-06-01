package edu.ucsb.nceas.morpho;

import java.util.Locale;
import java.util.ResourceBundle;

import edu.ucsb.nceas.utilities.Log;

public class Language {
	
	private ResourceBundle lan = null;
	
	private static Language instance = null;
	
	private Language() {
		try {
			//Locale locale = new Locale("en", "US");
			//Locale locale = new Locale("zh", "TW");
			//Locale locale = new Locale("zh", "CN");
			//lan = ResourceBundle.getBundle("language.Messages", locale);
			lan = ResourceBundle.getBundle("language.Messages");
		} catch (Exception e) {
			Log.debug(5, "Could not load language resource bundle. " + e.getMessage());
		}
	}
	
	public static Language getInstance() {
		if (instance == null) {
			instance = new Language();
		}
		return instance;
	}
	
	public String getMessages(String  key) {
		String message = null;
		try {
			message = lan.getString(key);
		} catch (Exception e) {
			// probably missing the key
			Log.debug(20, "Could not load string for language key: " + key);
			message = key;
		}
		return message;
	}

}
