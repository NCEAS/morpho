/**
 *  '$RCSfile: WizardPageLibrary.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-03-11 02:54:42 $'
 * '$Revision: 1.24 $'
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

  private static WizardContainerFrame container;

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
    if(pageID.equals(DataPackageWizardInterface.METHODS_PAGE)) return new MethodsPage();
    if(pageID.equals(DataPackageWizardInterface.PARTY_PAGE)) return new PartyPage();
    if(pageID.equals(DataPackageWizardInterface.ACCESS_PAGE)) return new AccessPage();
    if(pageID.equals(DataPackageWizardInterface.TEMPORAL_PAGE)) return new TemporalPage();
    if(pageID.equals(DataPackageWizardInterface.GEOGRAPHIC_PAGE)) return new GeographicPage();
		if(pageID.equals(DataPackageWizardInterface.CODE_IMPORT_PAGE)) return new CodeImportPage(container);
		if(pageID.equals(DataPackageWizardInterface.CODE_DEFINITION)) return new CodeDefinition(container);
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


  public WizardPageLibrary(WizardContainerFrame container) {

    this.container = container;
    reInitialize();
  }

  /**
   *  clears out any existing pages from the library and populates it with
   *  new pages
   */
  public static void reInitialize() {

    pages.clear();

    pages.put(DataPackageWizardInterface.INTRODUCTION,       new Introduction());
    pages.put(DataPackageWizardInterface.PROJECT,            new Project());
    pages.put(DataPackageWizardInterface.GENERAL,            new General());
    pages.put(DataPackageWizardInterface.KEYWORDS,           new Keywords());
    pages.put(DataPackageWizardInterface.PARTY_INTRO,        new PartyIntro());
    pages.put(DataPackageWizardInterface.PARTY_CREATOR,      new PartyMainPage(PartyPage.CREATOR));
    pages.put(DataPackageWizardInterface.PARTY_CONTACT,      new PartyMainPage(PartyPage.CONTACT));
    pages.put(DataPackageWizardInterface.PARTY_ASSOCIATED,   new PartyMainPage(PartyPage.ASSOCIATED));
    pages.put(DataPackageWizardInterface.USAGE_RIGHTS,       new UsageRights());
    pages.put(DataPackageWizardInterface.DATA_LOCATION,      new DataLocation(container));
    pages.put(DataPackageWizardInterface.TEXT_IMPORT_WIZARD, new ImportWizard(container));
    pages.put(DataPackageWizardInterface.DATA_FORMAT,        new DataFormat());
    pages.put(DataPackageWizardInterface.ENTITY,             new Entity(container));
    pages.put(DataPackageWizardInterface.ACCESS,             new Access());
    pages.put(DataPackageWizardInterface.GEOGRAPHIC,         new Geographic());
    pages.put(DataPackageWizardInterface.TAXONOMIC,          new Taxonomic());
    pages.put(DataPackageWizardInterface.TEMPORAL,           new Temporal());
    pages.put(DataPackageWizardInterface.METHODS,            new Methods());
    pages.put(DataPackageWizardInterface.SUMMARY,            new Summary(container));
    pages.put(DataPackageWizardInterface.CODE_IMPORT_SUMMARY,new CodeImportSummary(container));
    
  }



}
