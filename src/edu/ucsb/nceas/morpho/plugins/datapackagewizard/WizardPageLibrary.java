/**
 *  '$RCSfile: WizardPageLibrary.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-07-28 19:08:19 $'
 * '$Revision: 1.1 $'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import java.util.Map;
import java.util.HashMap;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.*;

/**
 *  Class       WizardPageLibrary
 *  - a container for all the wizard pages, that can be retrieved by ID using 
 *  the getPage() method
 */
public class WizardPageLibrary {

  private Map pages;

  /** 
   *  returns the WizardPage with the corresponding pageID provided
   *
   *  @param pageID the String pageID for the WizardPage to be returned
   *
   *  @return  the corresponding WizardPage with this ID
   */
  public AbstractWizardPage getPage(String pageID) {
  
    if (containsPageID(pageID)) return (AbstractWizardPage)pages.get(pageID);
    
    return null;
  }


  /** 
   *  returns boolean true if the library contains a non-null WizardPage with 
   *  the corresponding pageID provided, and boolean false if the pageID doesn't 
   *  exist, or if the pageID corresponds to a null page
   *
   *  @param    pageID the String pageID for the WizardPage
   *
   *  @return   boolean true if the library contains a non-null WizardPage with 
   *            the corresponding pageID provided, and boolean false if the 
   *            pageID doesn't exist, or if the pageID corresponds to a null 
   *            page
   */
  public boolean containsPageID(String pageID) {
  
    return (pages.containsKey(pageID) && (pages.get(pageID)!=null) );
  }
  

  public WizardPageLibrary() {

    pages = new HashMap();
    
    pages.put(PAGE01_ID,  new WizPage01());
    pages.put(PAGE02_ID,  new WizPage02());
//    pages.put(PAGE03_ID,  new WizPage03());
//    pages.put(PAGE04_ID,  new WizPage04());
//    pages.put(PAGE05_ID,  new WizPage05());
//    pages.put(PAGE06_ID,  new WizPage06());
//    pages.put(PAGE07_ID,  new WizPage07());
//    pages.put(PAGE08_ID,  new WizPage08());
//    pages.put(PAGE09_ID,  new WizPage09());
//    pages.put(PAGE10_ID,  new WizPage10());
//    pages.put(PAGE11_ID,  new WizPage11());
//    pages.put(PAGE12_ID,  new WizPage12());
//    pages.put(PAGE13_ID,  new WizPage13());
//    pages.put(PAGE14_ID,  new WizPage14());
//    pages.put(PAGE15_ID,  new WizPage15());
//    pages.put(PAGE16_ID,  new WizPage16());
//    pages.put(PAGE17_ID,  new WizPage17());
//    pages.put(PAGE18_ID,  new WizPage18());
//    pages.put(PAGE19_ID,  new WizPage19());
//    pages.put(PAGE20_ID,  new WizPage20());
//    pages.put(PAGE21_ID,  new WizPage21());
//    pages.put(PAGE22_ID,  new WizPage22());
//    pages.put(PAGE23_ID,  new WizPage23());
  }

  public static final String PAGE01_ID = "PAGE01_ID";
  public static final String PAGE02_ID = "PAGE02_ID";
  public static final String PAGE03_ID = "PAGE03_ID";
  public static final String PAGE04_ID = "PAGE04_ID";
  public static final String PAGE05_ID = "PAGE05_ID";
  public static final String PAGE06_ID = "PAGE06_ID";
  public static final String PAGE07_ID = "PAGE07_ID";
  public static final String PAGE08_ID = "PAGE08_ID";
  public static final String PAGE09_ID = "PAGE09_ID";
  public static final String PAGE10_ID = "PAGE10_ID";
  public static final String PAGE11_ID = "PAGE11_ID";
  public static final String PAGE12_ID = "PAGE12_ID";
  public static final String PAGE13_ID = "PAGE13_ID";
  public static final String PAGE14_ID = "PAGE14_ID";
  public static final String PAGE15_ID = "PAGE15_ID";
  public static final String PAGE16_ID = "PAGE16_ID";
  public static final String PAGE17_ID = "PAGE17_ID";
  public static final String PAGE18_ID = "PAGE18_ID";
  public static final String PAGE19_ID = "PAGE19_ID";
  public static final String PAGE20_ID = "PAGE20_ID";
  public static final String PAGE21_ID = "PAGE21_ID";
  public static final String PAGE22_ID = "PAGE22_ID";
  public static final String PAGE23_ID = "PAGE23_ID";
  
}
