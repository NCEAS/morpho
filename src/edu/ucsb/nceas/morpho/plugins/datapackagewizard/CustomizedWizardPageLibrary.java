package edu.ucsb.nceas.morpho.plugins.datapackagewizard;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;


import edu.ucsb.nceas.morpho.plugins.vocabulary.GenericVocabularyPage;
import edu.ucsb.nceas.morpho.plugins.vocabulary.ThesaurusLookupPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;

/**
 * This class represents a customized wizard page library. User can add any 
 * AbractUIPage into the library with unique id. Comparing to WizardPageLibaray
 * which has very static page return, this class is very flexible.
 */
public class CustomizedWizardPageLibrary implements WizardPageLibraryInterface {

 
  private Hashtable pageList = new Hashtable();

  public CustomizedWizardPageLibrary() 
  {

    
  }


  /**
   *  returns the WizardPage with the corresponding pageID provided
   *
   *  @param pageID the String pageID for the WizardPage to be returned
   *
   *  @return  the corresponding WizardPage with this ID
   */
  public  AbstractUIPage getUIPage(String pageID) 
  {

     AbstractUIPage page = (AbstractUIPage)pageList.get(pageID);
     return page;
  }
  
  /**
   * adds a page and corresponding pageID into the library
   * @param pageID the id of this page being added
   * @param page  the AbstractUIPage page being added
   */
  public void addPage(String pageID, AbstractUIPage page)
  {
	  pageList.put(pageID, page);
  }
  
  /**
   * tests if this library contains a wizard page
   * @return true if this library doesn't have any wizard page;otherwise false;
   */
  public boolean isEmpty()
  {
	  return pageList.isEmpty();
  }
}

