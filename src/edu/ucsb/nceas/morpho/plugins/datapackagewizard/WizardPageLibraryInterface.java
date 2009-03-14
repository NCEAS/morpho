package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;

/**
 * An interface defines a method to get UI page base on pageID. 
 * So user can overwrite the getUIPage method in WizardPageLibrary class.
 * @author tao
 *
 */
public interface WizardPageLibraryInterface {
	
	public AbstractUIPage getUIPage(String pageID);

}
