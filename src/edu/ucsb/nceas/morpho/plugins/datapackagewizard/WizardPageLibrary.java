/**
 *  '$RCSfile: WizardPageLibrary.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2003-12-03 02:38:49 $'
 * '$Revision: 1.14 $'
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
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;

/**
 *  Class       WizardPageLibrary
 *  - a container for all the wizard pages, that can be retrieved by ID using
 *  the getPage() method
 */
public class WizardPageLibrary {

  private static final Map pages = new HashMap();

  /**
   *  returns the WizardPage with the corresponding pageID provided
   *
   *  @param pageID the String pageID for the WizardPage to be returned
   *
   *  @return  the corresponding WizardPage with this ID
   */
  public static AbstractWizardPage getPage(String pageID) {

    if(pageID.equals(DataPackageWizardInterface.ATTRIBUTE_PAGE)) return new AttributePage();
    if(pageID.equals(DataPackageWizardInterface.KEYWORDS_PAGE)) return new KeywordsPage();
    if(pageID.equals(DataPackageWizardInterface.PARTY_PAGE)) return new PartyPage();
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
  public static boolean containsPageID(String pageID) {

    return (pages.containsKey(pageID) && (pages.get(pageID)!=null) );
  }


  public WizardPageLibrary() {

    pages.put(DataPackageWizardInterface.INTRODUCTION,       new Introduction());
    pages.put(DataPackageWizardInterface.PROJECT_INTRO,      new ProjectIntro());
    pages.put(DataPackageWizardInterface.PROJECT,            new Project());
    pages.put(DataPackageWizardInterface.GENERAL,            new General());
    pages.put(DataPackageWizardInterface.KEYWORDS,           new Keywords());
    pages.put(DataPackageWizardInterface.PARTY_INTRO,        new PartyIntro());
    pages.put(DataPackageWizardInterface.PARTY_CREATOR,      new PartyMainPage(PartyPage.CREATOR));
    pages.put(DataPackageWizardInterface.PARTY_CONTACT,      new PartyMainPage(PartyPage.CONTACT));
    pages.put(DataPackageWizardInterface.PARTY_ASSOCIATED,   new PartyMainPage(PartyPage.ASSOCIATED));
    pages.put(DataPackageWizardInterface.USAGE_RIGHTS,       new UsageRights());
    pages.put(DataPackageWizardInterface.DATA_LOCATION,      new DataLocation());
    pages.put(DataPackageWizardInterface.TEXT_IMPORT_WIZARD, new ImportWizard());
    pages.put(DataPackageWizardInterface.DATA_FORMAT,        new DataFormat());
    pages.put(DataPackageWizardInterface.ENTITY,             new Entity());
    pages.put(DataPackageWizardInterface.ACCESS,             new Access());
    pages.put(DataPackageWizardInterface.GEOGRAPHIC,         new Geographic());
    pages.put(DataPackageWizardInterface.TAXONOMIC,          new Taxonomic());
    pages.put(DataPackageWizardInterface.TEMPORAL,           new Temporal());
    pages.put(DataPackageWizardInterface.SUMMARY,            new Summary());
  }



}