package edu.ucsb.nceas.morpho;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import edu.ucsb.nceas.utilities.Log;

public class Language {
	
	public static String DEFAULT_LANGUAGE_BUNDLE = "language.Messages";

	private List<ResourceBundle> languageBundles = null;
	
	private static Language instance = null;
	
	/**
	 * Initialize the default language bundle - core of Morpho
	 */
	private Language() {
		try {
			languageBundles = new ArrayList<ResourceBundle>();
			ResourceBundle rb = ResourceBundle.getBundle(DEFAULT_LANGUAGE_BUNDLE);
			languageBundles.add(rb);
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
	
	/**
	 * Looks up the message for the given key
	 * Searches all registered resource bundles, returning the first key found
	 * If no entry is found, the key is returned back
	 * @param key string to look up
	 * @return the message for given key
	 */
	public String getMessage(String  key) {
		String message = null;
		// loop through all registered bundles
		for (ResourceBundle bundle: languageBundles) {
			try {
				message = bundle.getString(key);
			} catch (Exception e) {
				// probably missing the key, will continue searching
				Log.debug(20, "Could not load string for language key: " + key);
			}
			if (message != null) {
				return message;
			}
		}
		
		// return the key if we got this far
		return key;
	}
	
	/**
	 * Register another language bundle with the system
	 * Plugins can then inject their own bundles without modifying the base Morpho code
	 * @param baseName name of the properties resource to add (load)
	 */
	public void addLanguageBundle(String baseName) {
		ResourceBundle rb = ResourceBundle.getBundle(baseName);
		languageBundles.add(rb);
	}
}
