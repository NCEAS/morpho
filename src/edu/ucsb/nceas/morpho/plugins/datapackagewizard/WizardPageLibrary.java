/**
 *  '$RCSfile: WizardPageLibrary.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2008-11-22 01:28:10 $'
 * '$Revision: 1.35 $'
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
import edu.ucsb.nceas.morpho.plugins.vocabulary.ThesaurusLookupPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;

/**
 *  Class       WizardPageLibrary
 *  - a container for all the wizard pages, that can be retrieved by ID using
 *  the getPage() method
 */
public class WizardPageLibrary {

  protected static WizardContainerFrame container;


  public WizardPageLibrary(WizardContainerFrame container) {

    this.container = container;
  }


  /**
   *  returns the WizardPage with the corresponding pageID provided
   *
   *  @param pageID the String pageID for the WizardPage to be returned
   *
   *  @return  the corresponding WizardPage with this ID
   */
  public static AbstractUIPage getPage(String pageID) {

    if(pageID.equals(DataPackageWizardInterface.INTRODUCTION))
      return new Introduction();
    if(pageID.equals(DataPackageWizardInterface.PROJECT))
      return new Project();
    if(pageID.equals(DataPackageWizardInterface.GENERAL))
      return new General();
    if(pageID.equals(DataPackageWizardInterface.KEYWORDS))
      return new Keywords();
    if(pageID.equals(DataPackageWizardInterface.KEYWORDS_PAGE))
      return new KeywordsPage();
    if(pageID.equals(DataPackageWizardInterface.PARTY_INTRO))
      return new PartyIntro();

    if(pageID.equals(DataPackageWizardInterface.PARTY_CREATOR_PAGE))
      return new PartyMainPage(DataPackageWizardInterface.PARTY_CREATOR);
    if(pageID.equals(DataPackageWizardInterface.PARTY_CONTACT_PAGE))
      return new PartyMainPage(DataPackageWizardInterface.PARTY_CONTACT);
    if(pageID.equals(DataPackageWizardInterface.PARTY_ASSOCIATED_PAGE))
      return new PartyMainPage(DataPackageWizardInterface.PARTY_ASSOCIATED);

    if(pageID.equals(DataPackageWizardInterface.PARTY_CREATOR))
      return new PartyPage(DataPackageWizardInterface.PARTY_CREATOR);
    if(pageID.equals(DataPackageWizardInterface.PARTY_CONTACT))
      return new PartyPage(DataPackageWizardInterface.PARTY_CONTACT);
    if(pageID.equals(DataPackageWizardInterface.PARTY_ASSOCIATED))
      return new PartyPage(DataPackageWizardInterface.PARTY_ASSOCIATED);
    if(pageID.equals(DataPackageWizardInterface.PARTY_PERSONNEL))
      return new PartyPage(DataPackageWizardInterface.PARTY_PERSONNEL);
		if(pageID.equals(DataPackageWizardInterface.PARTY_CITATION_AUTHOR))
      return new PartyPage(DataPackageWizardInterface.PARTY_CITATION_AUTHOR);

    if(pageID.equals(DataPackageWizardInterface.GEOGRAPHIC_PAGE))
      return new GeographicPage();
    if(pageID.equals(DataPackageWizardInterface.GEOGRAPHIC))
      return new Geographic();
    if(pageID.equals(DataPackageWizardInterface.TAXONOMIC))
      return new Taxonomic();
    if(pageID.equals(DataPackageWizardInterface.TEMPORAL))
      return new Temporal();
    if(pageID.equals(DataPackageWizardInterface.TEMPORAL_PAGE))
      return new TemporalPage();
    if(pageID.equals(DataPackageWizardInterface.USAGE_RIGHTS))
      return new UsageRights();
    if(pageID.equals(DataPackageWizardInterface.DATA_LOCATION))
      return new DataLocation(container);
    if(pageID.equals(DataPackageWizardInterface.TEXT_IMPORT_WIZARD))
      return new ImportWizard(container);
    if(pageID.equals(DataPackageWizardInterface.DATA_FORMAT))
      return new DataFormat(container);
    if(pageID.equals(DataPackageWizardInterface.ENTITY))
      return new Entity(container);
    if(pageID.equals(DataPackageWizardInterface.ATTRIBUTE_PAGE))
      return new AttributePage();
    if(pageID.equals(DataPackageWizardInterface.CODE_IMPORT_PAGE))
      return new CodeImportPage(container);
    if(pageID.equals(DataPackageWizardInterface.METHODS))
      return new Methods();
    if(pageID.equals(DataPackageWizardInterface.METHODS_PAGE))
      return new MethodsPage();
    if(pageID.equals(DataPackageWizardInterface.ACCESS))
      return new Access(false);
    if(pageID.equals(DataPackageWizardInterface.ENTITY_ACCESS))
        return new Access(true);
    if(pageID.equals(DataPackageWizardInterface.ACCESS_PAGE))
      return new AccessPage();
    if(pageID.equals(DataPackageWizardInterface.SUMMARY))
      return new Summary(container);
    if(pageID.equals(DataPackageWizardInterface.CODE_IMPORT_SUMMARY))
      return new CodeImportSummary(container);
    if(pageID.equals(DataPackageWizardInterface.CODE_DEFINITION))
      return new CodeDefinition(container);
    if(pageID.equals(DataPackageWizardInterface.CITATION_PAGE))
      return new CitationPage();
    if(pageID.equals(DataPackageWizardInterface.CUSTOM_UNIT_PAGE))
      return new CustomUnitPage();
	if(pageID.equals(DataPackageWizardInterface.GENERIC_VOCABULARY))
	  return new GenericVocabularyPage();	
	if(pageID.equals(DataPackageWizardInterface.NBII_THESAURUS_LOOKUP))
		  return new ThesaurusLookupPage();

    throw new IllegalArgumentException(
      "WizardPageLibrary - no page registered with identifier: "+pageID);
  }
}
