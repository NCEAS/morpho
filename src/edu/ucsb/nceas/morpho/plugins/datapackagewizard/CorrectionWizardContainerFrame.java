/**
 *  '$RCSfile: CorrectionWizardContainerFrame.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Matthew Brooke
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-25 01:33:44 $'
 * '$Revision: 1.2 $'
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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;


/**
 * This Frame will contains the correction pages. The method  collectDataFromPages() will be
 * overwritten.
 * @author tao
 *
 */
public class CorrectionWizardContainerFrame extends WizardContainerFrame 
{
	/**
	 * The method to collect data from pages in node format
	 */
	public Node collectDataFromPages() 
	{
		//pages map
	    Map pageMap = new HashMap();

	    //results Map:
	    OrderedMap wizData = new OrderedMap();

	    //NOTE: the order of pages on the stack is *not* the same as the order
	    //of writing data to the DOM. We therefore convert the Stack to a Map
	    //(indexed by pageID) and access the pages non-sequentially in a flurry of
	    //hard-coded madness:
	    //

	    while (!pageStack.isEmpty()) {

	      AbstractUIPage nextPage = (AbstractUIPage)pageStack.pop();
	      String nextPageID = nextPage.getPageID();
	      Log.debug(45, ">> collectDataFromPages() - next pageID = "+nextPageID);
	      if (nextPageID==null || nextPageID.trim().length()<1) {

	        Log.debug(15,
	                  "\n*** WARNING - WizardContainerFrame.collectDataFromPages()"
	                  +" has encountered a page with no ID! Object is: "+nextPage);
	        continue;
	      }
	      pageMap.put(nextPageID, nextPage);
	    }

	    /*AbstractUIPage GENERAL
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.GENERAL);
	    AbstractUIPage KEYWORDS
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.KEYWORDS);
	    AbstractUIPage PARTY_CREATOR_PAGE
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.PARTY_CREATOR_PAGE);
	    AbstractUIPage PARTY_CONTACT_PAGE
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.PARTY_CONTACT_PAGE);
	    AbstractUIPage PARTY_ASSOCIATED_PAGE
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.PARTY_ASSOCIATED_PAGE);
	    AbstractUIPage PROJECT
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.PROJECT);
	    AbstractUIPage METHODS
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.METHODS);
	    AbstractUIPage USAGE_RIGHTS
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.USAGE_RIGHTS);
	    AbstractUIPage GEOGRAPHIC
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.GEOGRAPHIC);
	    AbstractUIPage TEMPORAL
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.TEMPORAL);
	    AbstractUIPage TAXONOMIC
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.TAXONOMIC);
	    AbstractUIPage ACCESS
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.ACCESS);
	    AbstractUIPage DATA_LOCATION
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.DATA_LOCATION);
	    AbstractUIPage TEXT_IMPORT_WIZARD
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.TEXT_IMPORT_WIZARD);
	    AbstractUIPage DATA_FORMAT
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.DATA_FORMAT);
	    AbstractUIPage ENTITY
	        = (AbstractUIPage)pageMap.get(DataPackageWizardInterface.ENTITY);*

	    //TITLE:
	    OrderedMap generalMap = null;
	    
	    //ACCESS:
	    if (ACCESS != null) {
	      addPageDataToResultsMap( ACCESS, wizData);
	    }
	    
	    if (GENERAL != null) {

	      generalMap = GENERAL.getPageData();
	      final String titleXPath = "/eml:eml/dataset/title[1]";
	      Object titleObj = generalMap.get(titleXPath);
	      if (titleObj != null) {
	        wizData.put(titleXPath, titleObj);
//	                    XMLUtilities.normalize(titleObj));  //avoid double normalization - DFH
	      }
	    }

	    //CREATOR:
	    if (PARTY_CREATOR_PAGE != null) {
	      addPageDataToResultsMap( PARTY_CREATOR_PAGE, wizData);
	    }

	    //ASSOCIATED PARTY:
	    if (PARTY_ASSOCIATED_PAGE != null) {
	      addPageDataToResultsMap( PARTY_ASSOCIATED_PAGE, wizData);
	    }

	    //ABSTRACT:
	    if (generalMap != null) {

	      final String abstractXPath = "/eml:eml/dataset/abstract/para[1]";
	      Object abstractObj = generalMap.get(abstractXPath);
	      if (abstractObj != null) {
	        wizData.put(abstractXPath,abstractObj);
//	                    XMLUtilities.normalize(abstractObj)); //avoid double normalization - DFH
	      }
	    }

	    //KEYWORDS:
	    if (KEYWORDS != null) {
	      addPageDataToResultsMap( KEYWORDS, wizData);
	    }

	    //INTELLECTUAL RIGHTS:
	    if (USAGE_RIGHTS != null) {
	      addPageDataToResultsMap( USAGE_RIGHTS, wizData);
	    }

	    //GEOGRAPHIC:
	    if (GEOGRAPHIC != null) {
	      addPageDataToResultsMap( GEOGRAPHIC, wizData);
	    }

	    //TEMPORAL:
	    if (TEMPORAL != null) {
	      addPageDataToResultsMap( TEMPORAL, wizData);
	    }

	    //TAXONOMIC
	    if (TAXONOMIC != null) {
	      addPageDataToResultsMap( TAXONOMIC, wizData);
	    }

	    //CONTACT:
	    if (PARTY_CONTACT_PAGE != null) {
	      addPageDataToResultsMap( PARTY_CONTACT_PAGE, wizData);
	    }

	    //METHODS:
	    if (METHODS != null) {
	      addPageDataToResultsMap( METHODS, wizData);
	    }

	    //PROJECT:
	    if (PROJECT != null) {
	      addPageDataToResultsMap( PROJECT, wizData);
	    }

	   

	    if (TEXT_IMPORT_WIZARD != null) {
	      addPageDataToResultsMap( TEXT_IMPORT_WIZARD, wizData);
	    }

	    if (ENTITY != null) {
	      addPageDataToResultsMap( ENTITY, wizData);
	    }

	    if (DATA_FORMAT != null) {
	      addPageDataToResultsMap( DATA_FORMAT, wizData);
	    }

	    if (DATA_LOCATION != null) {
	      addPageDataToResultsMap( DATA_LOCATION, wizData);
	    }
	    // now add unique ID's to all dataTables and attributes
	    addIDs(
	        new String[] {
	        "/eml:eml/dataset/dataTable",
	        "/eml:eml/dataset/dataTable/attributeList/attribute"
	    }
	        , wizData);*/

	    Log.debug(45, "\n\n********** Wizard finished: NVPs:");
	    Log.debug(45, wizData.toString());

	    ////////////////////////////////////////////////////////////////////////////
	    // this is the end of the page processing - wizData OrderedMap should now
	    // contain all values in correct order
	    ////////////////////////////////////////////////////////////////////////////

	    ////////////////////////////////////////////////////////////////////////////
	    ////////////////////////////////////////////////////////////////////////////
	    ////////////////////////////////////////////////////////////////////////////


	    ////////////////////////////////////////////////////////////////////////////
	    // next, create a DOM from the OrderedMap...
	    ////////////////////////////////////////////////////////////////////////////

	    //create a new empty DOM document to be populated by the wizard values:
	    Node rootNode = getNewEmptyDataPackageDOM(WizardSettings.NEW_EML210_DOCUMENT_TEXT);

	    //now populate it...
	    try {

	      XMLUtilities.getXPathMapAsDOMTree(wizData, rootNode);

	    }
	    catch (Exception e) {

	      e.printStackTrace();
	      Log.debug(5, "unexpected error trying to create new XML document "
	                + "after wizard finished\n");
	      cancelAction();

	      return null;
	    }

	    Log.debug(45, "\n\n********** Wizard finished: DOM:");
	    Log.debug(45, XMLUtilities.getDOMTreeAsString(rootNode));
	    return rootNode;
	  }


	 
}
