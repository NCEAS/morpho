package edu.ucsb.nceas.morpho;

import java.util.Locale;
import java.util.ResourceBundle;

public class Language {
	//Locale locale = new Locale("en", "US");
	//Locale locale = new Locale("zh", "TW");
	//Locale locale = new Locale("zh", "CN");
	//ResourceBundle lan = ResourceBundle.getBundle("language.Messages", locale);
	ResourceBundle lan = ResourceBundle.getBundle("language.Messages");
	
	public String getMessages ( String  lanId){
		
		String messages = lan.getString(lanId);
		
		return messages;
	}

}
