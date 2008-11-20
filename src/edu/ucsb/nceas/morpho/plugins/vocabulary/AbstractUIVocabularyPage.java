package edu.ucsb.nceas.morpho.plugins.vocabulary;

import java.util.List;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;

public abstract class AbstractUIVocabularyPage extends AbstractUIPage {
	
	public abstract List getSelectedTerms();
	
	public abstract void setVocabulary(String vocab);


}
