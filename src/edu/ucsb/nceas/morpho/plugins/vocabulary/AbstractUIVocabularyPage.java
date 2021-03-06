package edu.ucsb.nceas.morpho.plugins.vocabulary;

import java.util.List;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;

public abstract class AbstractUIVocabularyPage extends AbstractUIPage {
	
	protected static final String SLASH = "/";
	public abstract List getSelectedTerms();
	
	public abstract void setVocabulary(String vocab);


}
