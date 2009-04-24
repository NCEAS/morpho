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

import edu.ucsb.nceas.morpho.util.Log;
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
     Log.debug(30,  "he page from customized wizard library is "+page.hashCode());
     return page;
  }
  
  /**
   * adds a page and corresponding pageID into the library
   * @param pageID the id of this page being added
   * @param page  the AbstractUIPage page being added
   */
  public void addPage(String pageID, AbstractUIPage page)
  {
	  Log.debug(30, "add "+ pageID+" into CustomizedWizardPageLibrary with page object "+page.hashCode());
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
  
  /**
   * Gets the size of pages in the library
   * @return
   */
  public int size()
  {
	  return pageList.size();
  }
  
  /**
   * Get the page list in the library
   * @return the list in Hashtable format
   */
  public Hashtable getPageList()
  {
	  return this.pageList;
  }
}

