/**
 *  '$RCSfile: WizardPageLibrary.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-29 21:49:50 $'
 * '$Revision: 1.40 $'
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

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Access;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AccessPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributePage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CitationPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CodeDefinition;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CodeImportPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CodeImportSummary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CustomUnitPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.DataFormat;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.DataLocation;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Entity;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.General;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Geographic;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.GeographicPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Introduction;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Keywords;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.KeywordsPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Methods;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.MethodsPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.OtherEntityPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyIntro;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyMainPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Project;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.ReplaceDataPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.ReplicationPolicyPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Summary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Taxonomic;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Temporal;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TemporalPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TextImportAttribute;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TextImportDelimiters;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TextImportEntity;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.UsageRights;
import edu.ucsb.nceas.morpho.plugins.vocabulary.GenericTreeVocabularyPage;
import edu.ucsb.nceas.morpho.plugins.vocabulary.ThesaurusLookupPage;
import edu.ucsb.nceas.morpho.util.Log;

/**
 *  Class       WizardPageLibrary
 *  - a container for all the wizard pages, that can be retrieved by ID using
 *  the getPage() method
 */
public class WizardPageLibrary implements WizardPageLibraryInterface{

  protected WizardContainerFrame container;
  private int textImportAttributePagesSize = 1;


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
  public  AbstractUIPage getPage(String pageID) {

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
    if(pageID.equals(DataPackageWizardInterface.REPLACE_DATA_LOCATION))
        return new ReplaceDataPage();
    if(pageID.equals(DataPackageWizardInterface.OTHER_ENTITY))
        return new OtherEntityPage();
    /*if(pageID.equals(DataPackageWizardInterface.TEXT_IMPORT_WIZARD))
      return new ImportWizard(container);*/
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
      return new Access(new Boolean(false));
    if(pageID.equals(DataPackageWizardInterface.ENTITY_ACCESS))
        return new Access(new Boolean(true));
    if(pageID.equals(DataPackageWizardInterface.ACCESS_PAGE))
      return new AccessPage();
    if(pageID.equals(DataPackageWizardInterface.REPLICATION_POLICY))
        return new ReplicationPolicyPage(false);
    if(pageID.equals(DataPackageWizardInterface.SUMMARY))
      return new Summary(container);
    if(pageID.equals(DataPackageWizardInterface.CODE_IMPORT_SUMMARY))
      return new CodeImportSummary(container);
    if(pageID.equals(DataPackageWizardInterface.CODE_DEFINITION))
      return new CodeDefinition(container);
    if(pageID.equals(DataPackageWizardInterface.CITATION_PAGE))
      return new CitationPage();
    if(pageID.equals(DataPackageWizardInterface.CUSTOM_UNIT_PAGE))
      return new CustomUnitPage(container);
	if(pageID.equals(DataPackageWizardInterface.GENERIC_VOCABULARY))
	  return new GenericTreeVocabularyPage();	
	if(pageID.equals(DataPackageWizardInterface.NBII_THESAURUS_LOOKUP))
		  return new ThesaurusLookupPage();
	if(pageID.equals(DataPackageWizardInterface.TEXT_IMPORT_ENTITY))
		  return new TextImportEntity(container);
	if(pageID.equals(DataPackageWizardInterface.TEXT_IMPORT_DELIMITERS))
		  return new TextImportDelimiters(container);
	if(pageID.startsWith(DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE))
		  return generateTextImportAttributePage(pageID, true) ;
	if(pageID.startsWith(DataPackageWizardInterface.LOAD_INCOMPLETED_ATTRIBUTE)) {
        //we will create a attribute page without initial value
        pageID = pageID.replaceFirst(DataPackageWizardInterface.LOAD_INCOMPLETED_ATTRIBUTE, "");
        System.out.println("++++++++++++++++++++ the pageId is\n"+pageID);
        if(!pageID.startsWith(DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE)) {
            throw new IllegalArgumentException(
                    "WizardPageLibrary - no page registered with identifier: "+pageID);
        }
        return generateTextImportAttributePage(pageID, false);
    }
    throw new IllegalArgumentException(
      "WizardPageLibrary - no page registered with identifier: "+pageID);
  }
  
  /*
   * Generates the TextImportAttribute pages dynamically
   */
  private TextImportAttribute generateTextImportAttributePage(String pageId, boolean withInitialValue)
  {
	  int index = parseTextImmportAttributePageID(pageId);
	  if(index < 0 || index >= textImportAttributePagesSize)
	  {
		  Log.debug(5, "WizardPageLibrary - couldn't dynamically generate TextImmportAttribute page  with identifier: "+pageId+
		      " since index "+index+" is out of range from 0 to "+textImportAttributePagesSize);
		  return null;
	  }
	  TextImportAttribute page = new TextImportAttribute(container, index, withInitialValue);
	  page.setPageID(pageId);
	  if(index < textImportAttributePagesSize-1)
	  {
		  int nexIndex = index+1;
		  String nextPageID =DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE+nexIndex;
		  page.setNextPageID(nextPageID);
	  }
	  else if(index == textImportAttributePagesSize-1)
	  {
		  //set the default next page id. It may be change on the onAdvance method
		  page.setNextPageID(DataPackageWizardInterface.SUMMARY);
	  }
	  return  page;
  }
  
  /*
   * Parses the ID of TextImmportAttribute to get the index of attribute.
   * -1 will be returned if we couldn't understand the page
   * The format of TextImmportAttribute ID is:
   * TEXT_IMPORT_ATTRIBUTE_8
   * We need to extract 8 from the string.
   */
  private int parseTextImmportAttributePageID(String pageId) 
  {
	  int index = -1;
	  if(pageId != null)
	  {
		  int position = pageId.lastIndexOf(DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE);
		  if (position != -1)
		  {
			  
			  String numberStr = pageId.substring(DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE.length());
			  Log.debug(32, "The number string extracted from "+pageId+ " is "+numberStr);
			  try
			  {
				  Integer number = new Integer(numberStr);
				  index = number.intValue();
			  }
			  catch(Exception e)
			  {
				  Log.debug(30, "Couldn't find the attribute index in the id "+pageId);
			  }
		  }
	  }
	  return index;
  }
  
  
  /**
	 * Sets the size of TextImportAttributePage. TextImportAttirubtePage
	 * is dynamically generated by the page library. So we need to know its size.
	 * @param size
	 */
	public void setTextImportAttributePagesSize(int size)
	{
		this.textImportAttributePagesSize = size;
	}
	
	/**
	 * Gets the size of text import attribute page size
	 * @return
	 */
	public int getTextImportAttributePagesSize()
	{
		return this.textImportAttributePagesSize;
	}

  
}
