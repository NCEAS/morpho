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
 *     '$Date: 2003-09-08 22:11:21 $'
 * '$Revision: 1.7 $'
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
    
    pages.put(INTRODUCTION,     new Introduction());
    pages.put(GENERAL,          new General());
    pages.put(KEYWORDS,         new Keywords());
//    pages.put(KEYWORDS_DIALOG,  new KeywordsDialog());
    pages.put(PARTY_INTRO,      new PartyIntro());
    pages.put(PARTY_CREATOR,    new PartyPage(PartyDialog.CREATOR));
    pages.put(PARTY_CONTACT,    new PartyPage(PartyDialog.CONTACT));
    pages.put(PARTY_ASSOCIATED, new PartyPage(PartyDialog.ASSOCIATED));
    
    pages.put(USAGE_RIGHTS,     new UsageRights());
    pages.put(DATA_LOCATION,    new DataLocation());
    pages.put(DATA_FORMAT,      new DataFormat());
    pages.put(SUMMARY,          new Summary());
//    pages.put(TEXT_IMPORT_WIZARD,  new TextImportWizard());
  }

  public static final String INTRODUCTION       = "INTRODUCTION";
  public static final String GENERAL            = "GENERAL";
  public static final String KEYWORDS           = "KEYWORDS";
  public static final String KEYWORDS_DIALOG    = "KEYWORDS_DIALOG";
  public static final String PARTY_INTRO        = "PARTY_INTRO";
  public static final String PARTY_CREATOR      = "PARTY_CREATOR";
  public static final String PARTY_CONTACT      = "PARTY_CONTACT";
  public static final String PARTY_ASSOCIATED   = "PARTY_ASSOCIATED";
  public static final String USAGE_RIGHTS       = "USAGE_RIGHTS";
  public static final String DATA_LOCATION      = "DATA_LOCATION";
  public static final String TEXT_IMPORT_WIZARD = "TEXT_IMPORT_WIZARD";
  public static final String DATA_FORMAT        = "DATA_FORMAT";
  public static final String SUMMARY            = "SUMMARY";

}
