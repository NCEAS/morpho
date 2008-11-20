package edu.ucsb.nceas.morpho.plugins.vocabulary;

import java.util.Map;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Log;

public class VocabularyPlugin implements PluginInterface, ServiceProvider {

	/** A reference to the container framework */
	private static Morpho morpho = null;
	
	/** will be initialized when this plugin is initialized */
	private static VocabularyPlugin instance = null;
	
	private Map vocabularies;
	
	public void initialize(Morpho morpho) {
		this.morpho = morpho;
		loadVocabularies();
		instance = this;
	}
	
	private void loadVocabularies() {
		vocabularies = Morpho.getConfiguration().getHashtable("vocabularies", "vocabulary", "className");
	}
	
	public static VocabularyPlugin getInstance() {
		return instance;
	}
	
	public AbstractUIVocabularyPage getVocabularyPage(String vocab) {
		AbstractUIVocabularyPage page = null;
		String className = (String) vocabularies.get(vocab);
		try {
			Class pageClass = Class.forName(className);
			page = (AbstractUIVocabularyPage) pageClass.newInstance();
			page.setVocabulary(vocab);
		} catch (Exception e) {
			Log.debug(5, "Could not load vocabulary: " + vocab);
			e.printStackTrace();
		}
		
		return page;
	}
	
}
