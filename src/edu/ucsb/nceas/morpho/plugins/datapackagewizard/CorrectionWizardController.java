/**
 *  '$RCSfile: CorrectionWizardController.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-08 21:50:34 $'
 * '$Revision: 1.38 $'
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



import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributePage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CorrectionSummary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Entity;
import edu.ucsb.nceas.morpho.util.LoadDataPath;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.ModifyingPageDataInfo;
import edu.ucsb.nceas.morpho.util.XPathUIPageMapping;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

/**
 * This class represents a controller which will start a process to 
 * correct valid values in eml 210 documents. This class will be passed a list
 * of xml paths which contain invalid value, e.g. white spaces. First, the class
 * will figure out which wizard pages should be used upon the xml paths base on
 * a mappting file. If no UIPage was found, the path will be assigned to a tree editor.
 *  Then, it will introduce wizard page and/or tree editor to user, which can help to correct error.
 * @author tao
 *
 */
public class CorrectionWizardController 
{

	private Vector errorPathList = null;
	// page library to store wizard page
	private CustomizedWizardPageLibrary wizardPageLibrary = new CustomizedWizardPageLibrary();
	//vector to store the path which should be openned by tree editor
	private Vector pathListForTreeEditor = new Vector();
	
	// The hasttable containing the mapping which be read from mapping property file
	private XPathUIPageMapping[] mappingList = null;
	// full path mapping hash table - key is full path, value is XPathUIPageMapping
	private Hashtable fullPathMapping = new Hashtable();
	// short path mapping hash table - key is path (without root and value is XPathUIPageMapping)
	// now we only consider fullPathMapping
	private Hashtable shortPathMapping = new Hashtable();
	// metadata in DataPackage format
	MorphoDataPackage mdp = null;
	// the old frame need to be disposed.
	private MorphoFrame oldFrame = null;
	//private Document metadataDoc = null;
	private DataPackageWizardListener listener = new CorrectionDataPackageWizardListener(); //this listener is for controller itself.
	private DataPackageWizardListener externalListener = null; //this listener will do some other action  after the wizard is done, e.g. AddSthCommand.
	private DataPackageWizardPlugin plugin = new DataPackageWizardPlugin();
	private CorrectionWizardContainerFrame dpWiz = null;
	// the file path of mapping properties file (xml format)
	public static final String MAPPINGFILEPATH = "lib/xpath-wizard-map.xml";
	// element name used in mapping properties file (xml format)
	private static final String MAPPING = "mapping";
	private static final String XPATH = "xpath";
	private static final String WIZARDAGECLASS = "wizardPageClass";
	private static final String ROOT = "root";
	private static final String INFORORMODIFYINGDATA = "infoForModifyingData";
	private static final String LOADEXISTDATAPATH = "loadExistDataPath";
	private static final String XPATHFORGETTINGPAGEDATA = "xpathForGettingPageData";
	private static final String NEWDATADOCUMENTNAME = "newDataDocumentName";
	private static final String GENERICNAME = "genericName";
	private static final String XPAHTFORCREATINGORDEREDMAP = "xpathForCreatingOrderedMap";
	private static final String WIZARDAGECLASSPARAMETER ="wizardPageClassParameter";
	private static final String XPATHFORSETTINGPAGEDATA = "xpathForSettingPageData";
	private static final String WIZARDCONTAINERFRAME = "edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame";
	private static final String CORRECTIONSUMMARY = "edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CorrectionSummary";
	private static final String TITLE = "Correction Wizard for Data Package ";
	private static final String STARTPAGEID = "0";
	private static final String PARA = "para";
	private static final String SLASH = "/";
	private final static String DATATABLE = "dataTable";
	private final static String ATTRIBUTE = "attribute";
	private final static String RIGHTBRACKET = "]";
	private final static String LEFTBRACKET = "[";
	private final static char RIGHTBRACKETCHAR = ']';
	private final static char LEFTBRACKETCHAR = '[';
	private final static String KEY = "key";
	private final static String PREVNODE = "prevNode";
	private final static String NEXTNODE = "nextNode";
	private final static String INTRODUCTIONPREFIX = 
		/*"Morpho has detected metadata fields that are invalid in the newer EML version.\n"*/ 
		Language.getInstance().getMessage("CorrectionWizardController.INTRODUCTIONPREFIX_1") + "\n"
	    /*+"This can include whitespace-only fields, and/or non-numeric values where numbers are required.\n "*/
	    + Language.getInstance().getMessage("CorrectionWizardController.INTRODUCTIONPREFIX_2") +"\n"
	    ;
	
	private final static String INTRODUCTIONSUFFIX  = 
									/*"The following wizard pages will allow you to enter valid information in these fields."*/
									Language.getInstance().getMessage("CorrectionWizardController.INTRODUCTIONPREFIX_3")
									;
	private final static String INTRODUCTIONWIZARD = INTRODUCTIONPREFIX /*+ "wizard pages"*/ + INTRODUCTIONSUFFIX+".\n"; 
	private final static String MESSAGEFORWIZARD = INTRODUCTIONWIZARD +
			/*"For optional fields, you may simply choose to leave them blank"*/
		    Language.getInstance().getMessage("CorrectionWizardController.INTRODUCTIONPREFIX_4")
			;
			
	private final static String INTRODUCTIONTREEEDITOR = INTRODUCTIONPREFIX + INTRODUCTIONSUFFIX;
	private final static String MESSAGEFORTREEEDITOR = INTRODUCTIONTREEEDITOR;
	
	
	/**
	 * Constructor
	 * @param errorPathList the list of paths which contain invalid value
	 */
	public CorrectionWizardController(Vector errorPathList, MorphoDataPackage mdp, MorphoFrame oldFrame)
	{
	    this.errorPathList  = errorPathList;
	    this.mdp  = mdp;
	    this.oldFrame       = oldFrame;
	    //Log.debug(30, "==========old frame in correction wizard controller"+oldFrame);
	    dpWiz = new CorrectionWizardContainerFrame(mdp.getAbstractDataPackage());
	    // find the mapping between xpath and page class name in a file
	    XpathUIPageMappingReader reader = new XpathUIPageMappingReader(MAPPINGFILEPATH);
	    this.mappingList   = reader.getXPathUIPageMappingList();
	    this.fullPathMapping = reader.getFullPathMapping();
	    assignErrorPath();// assign the error path to UI page list or tree editor path list

	}
	
	/**
	 * Set externalListner for the controller.
	 * @param externalListener
	 */
	public void setExternalListener(DataPackageWizardListener externalListener)
	{
		this.externalListener = externalListener;
	}
	
	/**
	 * Start to run the wizard.
	 * It has 3 scenarios:
	 * 1. Run both wizard pages and tree editors.
	 * 2. Run only wizard pages
	 * 3. Run only tree editors
	 * .
	 */
	public void startWizard()
	{
		// first to run wizard page to fix the issue
		if(!wizardPageLibrary.isEmpty())
		{
			JOptionPane.showMessageDialog(oldFrame, MESSAGEFORWIZARD, Language.getInstance().getMessage("Warning"),
                    JOptionPane.WARNING_MESSAGE);
			//Scenario 1 and 2. They can be told at the wizardComplete method
		    //this part will open a tree editor too if pathListForTreeEditor is not empty
			//the DataPackageWizardListener will trigger to open tree editor
			dpWiz.setWizardPageLibrary(wizardPageLibrary);
		    dpWiz.setDataPackageWizardListener(listener);
		    dpWiz.setBounds(
		                  WizardSettings.WIZARD_X_COORD, WizardSettings.WIZARD_Y_COORD,
		                  WizardSettings.WIZARD_WIDTH,   WizardSettings.WIZARD_HEIGHT );
		    dpWiz.setCurrentPage(STARTPAGEID);
		    String totalPageNumber = (new Integer(wizardPageLibrary.size())).toString();
		    Log.debug(35, "total number is "+totalPageNumber);
		    dpWiz.setShowPageCountdown(true, totalPageNumber);
		    AbstractDataPackage dataPackage = mdp.getAbstractDataPackage();
		    dpWiz.setTitle(TITLE+dataPackage.getAccessionNumber());
		    dpWiz.setVisible(true);
		}
		else if( pathListForTreeEditor != null && !pathListForTreeEditor.isEmpty())
		{
			JOptionPane.showMessageDialog(oldFrame, MESSAGEFORTREEEDITOR, Language.getInstance().getMessage("Warning"),
                    JOptionPane.WARNING_MESSAGE);
			//Scenario 3.
			//there is no UIPage returned, we only run tree editor to fix the issue
			try
			{
				TreeEditorCorrectionController treeEditorController = new TreeEditorCorrectionController(mdp, pathListForTreeEditor, oldFrame);
				treeEditorController.setExternalListener(externalListener);
				treeEditorController.startCorrection();
			}
			catch(Exception e)
			{
				Log.debug(5, "Couldn't run tree editor to correct the eml210 document "+e.getMessage());
			}
		}
	}
	
	/*
	 * Gets list of pages for correcting the invalid value. First we will
	 * look the mapping between wizard page and xpath. If we find one, 
	 * the wizard page will bet put into list. If we couldn't find one, tree editor
	 * for this xpath will be put into list.
	 */
	private void assignErrorPath()
	{
		int pageID = 0;
		String pageIDstr = null;
		AbstractUIPage previousPage = null;		
		if (errorPathList != null)
		{				
			for (int i=0; i<errorPathList.size(); i++)
			{
				String path =(String)errorPathList.elementAt(i);
				pageIDstr = (new Integer(pageID)).toString();
				AbstractUIPage page = null;
				try
				{
				   page = getUIPage(path);
				}
				catch(Exception e)
				{
					Log.debug(30, "couldn't find the ui page for path "+path +" since "+e.getMessage());
				}
				
				// found a wizard page for this path
				if (page != null)
				{
					Log.debug(45, "find a UI page object for path "+path);
					page.addXPathWithEmptyValue(path);
					//if a page with same data exists in the library, we should skip this page.
					boolean checkPageExisted = isUIPageExisted(page, path);
					if(checkPageExisted)
					{
						Log.debug(45, "The page for path "+path +" already existed in the library. We should skip it");
						continue;
					}
					
					// set up next id for previous page
					if (previousPage != null)
					{
						previousPage.setNextPageID(pageIDstr);
					}
					previousPage = page;
					// find a wizard page and add it to the list
					
					wizardPageLibrary.addPage(pageIDstr, page);
					pageID ++;
					Log.debug(35, "The page number was set  "+(new Integer(pageID)).toString());
					page.setTemporaryPageNumber((new Integer(pageID)).toString());
				}
				else
				{
					//no wizard page found and add a tree editor to the list
					Log.debug(45, "Put the path "+path+" into tree editor list");
					path = XMLUtilities.removeAllPredicates(path);
					pathListForTreeEditor.add(path);
				}
			}
			
			// set up correction summary page as the last one page if there is at least one found wizard page 
			if (!wizardPageLibrary.isEmpty())
			{
				pageIDstr = (new Integer(pageID)).toString();
				if (previousPage != null)
				{
					previousPage.setNextPageID(pageIDstr);
				}
				boolean needTreeEditor = false;
				if(pathListForTreeEditor != null && !pathListForTreeEditor.isEmpty())
				{
					needTreeEditor = true;
				}
				//AbstractUIPage summaryPage = createAbstractUIpageObject(CORRECTIONSUMMARY,dpWiz, null);
				CorrectionSummary summaryPage = new CorrectionSummary(dpWiz, needTreeEditor);
				summaryPage.setTemporaryPageNumber((new Integer(pageID+1)).toString());
				wizardPageLibrary.addPage(pageIDstr, summaryPage);
			}
		}
	}
	
	
	/*
	 * Returns a UI page for given xml path. If no page can be found, null will be returned.
	 * The page also contains existed data.
	 */
	private AbstractUIPage getUIPage(String path) throws Exception
	{
		AbstractUIPage page = null;
		String className = null;
		boolean mapDataFit = false;
		if (mappingList != null)
		{
			path = removeParaFromXPath(path);
			XPathUIPageMapping mapping = getXPathUIPageMapping(path);
			if(mapping != null)
			{
			    
				className = mapping.getWizardPageClassName();
				Log.debug(45, "get the className from mapping "+className);
				OrderedMap additionalInfo = null;
				AbstractDataPackage dataPackage = mdp.getAbstractDataPackage();
				page = WizardUtil.getUIPage(mapping, dpWiz, additionalInfo, dataPackage.getMetadataNode(), path);
			}
		}				
		return page;
	}
	
	
	/*
	 * Find out the mapping between xpath and UIPage
	 */
	private XPathUIPageMapping getXPathUIPageMapping(String path)
	{
		XPathUIPageMapping mapping = null;
		if(path != null)
		{
			//first we need to remove all predicates since there is no predicates in the xpath in the configure file
			path =XMLUtilities.removeAllPredicates(path);
			// first to check full path mapping
			Log.debug(46, "the given error path without all predicates is"+path);
			if (fullPathMapping != null)
			{
				Log.debug(46, "in fullPathMapping != null");
				XPathUIPageMapping mappingFromList = (XPathUIPageMapping)fullPathMapping.get(path);
				//we should clone this object, since it will be assigned to every page.
				mapping = XPathUIPageMapping.copy(mappingFromList);
				Log.debug(46, "find the xpath and uipage mapping in full path mapping "+mapping);
			}
			// second to check short mapping if no class name was found in full path mapping
			/*if (mapping == null)
			{
				mapping = (XPathUIPageMapping)shortPathMapping.get(path);
				Log.debug(48, "find the class name in short path mapping "+mapping.getWizardPageClassName());
			}*/
		}
		return mapping;
	}
	
	
	/*
	 * If the last element of xmpath contains "para", it will be special, we should remove it.
	 * If no para, original path will be returned.
	 */
	private String removeParaFromXPath(String xpath)
	{
		String newPath = xpath;
		if (xpath != null)
		{
			//find the last slash
			int index = xpath.lastIndexOf(SLASH);
			if (index < (xpath.length() -1))
			{
			    String lastElement = xpath.substring(index+1);
			    if (lastElement != null && (lastElement.equals(PARA) || lastElement.startsWith(PARA)))
			    {
			    	newPath = xpath.substring(0, index);
			    }
			}
			else
			{
				//slash is the last character in the given string
				newPath = xpath;
			}
			if(newPath.equals(SLASH) || newPath.trim().equals(""))
			{
				newPath = xpath;
			}
		}
		Log.debug(40, "After removing para, the new xpath is "+newPath);
		return newPath;
	}
	/*
	 * Get the datatable index for a given xpath.
	 * If the path is "eml/dataset/datatable[2], 2 will be returned.
	 * if no index found, -1 will be returned
	 */
	private int getDataTableIndex(String path)
	{
		int index = WizardUtil.getGivenStringIndexAtXPath(DATATABLE, path);
		return index;
	}
	
	/*
	 * Get the attribute index for a given xpath.
	 * If the path is "eml/dataset/datatable[2]/attributeList/attribute[1], 1 will be returned.
	 * if no index found, -1 will be returned
	 */
	private int getAttributeIndex(String path)
	{
		int index = WizardUtil.getGivenStringIndexAtXPath(ATTRIBUTE, path);
		return index;
	}
	

	/*
	 * Gets the last predicate value in a given xpath. 0 will be returned if it is not found.
	 */
	private int getLastPredicate(String path)
	{
		int position = 0;
		if(path != null)
		{
			int start = path.lastIndexOf(LEFTBRACKET);
			int end  = path.lastIndexOf(RIGHTBRACKET);
			if (start !=-1 && end != -1)
			{
				try
				{
					String value = path.substring(start+1, end);
					position = (new Integer(value)).intValue();
					Log.debug(35, "the last predicate is "+position);
				}
				catch(Exception e)
				{
					Log.debug(30, "couldn't find the last predicate for path "+path+" since "+e.getMessage());
				}
			}
		}
		return position;
	}
	

	
	 /*
	  * This class is the listener of CorrectionDataPackageFrame. It will implement the method
	  * wizardComplete. In this method, it will trigger to start a tree editor correction controller
	  * if the pathListForTreeEditor is not empty.
	  */
	private class CorrectionDataPackageWizardListener implements DataPackageWizardListener
	{

		public void wizardComplete(Node newDOM, String autoSavedID) {

	          Log.debug(30,
	              "Correction Wizard UI Page complete ");
	          cleanUpLibrary();
	          AbstractDataPackage adp = DataPackageFactory.getDataPackage(newDOM);
	          MorphoDataPackage mdp = new MorphoDataPackage();
	          mdp.setAbstractDataPackage(adp);
	          Log.debug(45, "AbstractDataPackage complete");
	          //adp.setAccessionNumber("temporary.1.1");
	          //second, to correct data by tree editor
			    if(pathListForTreeEditor != null && !pathListForTreeEditor.isEmpty())
			    {
			    	//scenario 1. some path can't be fixed by wizard page. we need to start
			    	// tree editor to fix them
					try
					{
						Log.debug(30, "assign the old frame to tree controler "+oldFrame);
						TreeEditorCorrectionController treeEditorController = new TreeEditorCorrectionController(mdp, pathListForTreeEditor, oldFrame);
						treeEditorController.setExternalListener(externalListener);
						treeEditorController.startCorrection();
					}
					catch(Exception e)
					{
						Log.debug(5, "Couldn't run tree editor to correct the eml210 document "+e.getMessage());
					}
			    }
			    else
			    {        
			    	   //scenario 2
                      //no tree editor is needed, so we can display the data now and dispose the old morpho frame
			          try {
			            ServiceController services = ServiceController.getInstance();
			            ServiceProvider provider =
			                services.getServiceProvider(DataPackageInterface.class);
			            DataPackageInterface dataPackage = (DataPackageInterface)provider;
			            dataPackage.openNewDataPackage(mdp, null);
			            //dispose old frame
			            if(oldFrame != null)
			            {
			            	oldFrame.setVisible(false);                
			            	UIController controller = UIController.getInstance();
			            	controller.removeWindow(oldFrame);
			            	oldFrame.dispose();	
			            }
			            //do some other stuff specified by external listener
			            if(externalListener != null)
			            {
			            	externalListener.wizardComplete(newDOM,autoSavedID);
			            }
		
			          } catch (ServiceNotHandledException snhe) {
		
			            Log.debug(6, snhe.getMessage());
			          }
			           //Log.debug(45, "\n\n********** Correction Wizard finished: DOM:");
			           //Log.debug(45, XMLUtilities.getDOMTreeAsString(adp.getMetadataNode(), false));
			        }
		     }


	        public void wizardCanceled() {
	          cleanUpLibrary();
	          Log.debug(45, "\n\n********** Correction Wizard canceled!");
	        }
	        
	        /**
	         *  Method from DataPackageWizardListener. Do nothing.
	         *
	         */
	        public void wizardSavedForLater()
	        {
	          Log.debug(45, "Correction wizard was saved for later usage");
	        }

	}
	
	/*
	 * Destroy the library when wizard is canceled or wizard is done.
	 */
	private void cleanUpLibrary()
	{
		if (wizardPageLibrary != null)
		{
			Hashtable list = wizardPageLibrary.getPageList();
			if(list != null)
			{
                list.clear();
			}
		}
	}
	
	/*
	 * After getting the error path list, the class will create a wizard UI page for every error path if 
	 * it can find one. Since two error path can be in the same subtree of wizard UI page, it is possible
	 * the wizard UI pages can be duplicated. For instance, if we create two pages for 
	 * /eml:eml/dataset/creator[1]/individualName/salutation and /eml:eml/dataset/creator[1]/individualName/givenName.
	 * The two pages are duplicated. The criteria of pages are identical:
	 * 1) Same name (class).
	 * 2) Same data (subtree). We have a simple way to compare data:  the LoadDataPath is same.
	 * If they are same, this means the same subtree is loaded into page.
	 */
	 private boolean isUIPageExisted(AbstractUIPage page, String errorPath)
	 {
		 boolean isExisted = false;
		 
		 if(page == null)
		 {
			 // we don't want to put a null page into the library. So we assume it is existed.
			 isExisted = true;
			 return isExisted;
		 }
		 
		 //wizardPageLibrary is the place to store pages
		 if (wizardPageLibrary != null)
		 {
			 int size = wizardPageLibrary.size();
			 Log.debug(45, "The size of current library is "+size);
			 for(int i=0; i<size; i++)
			 {
				 // if we found a identical one, we don't need continue.
				 if(isExisted)
				 {
					 break;
				 }
				 //we used decrease order
				 String pageIndex = (new Integer(size-1-i)).toString();
				 Log.debug(45, "get page "+pageIndex+ " from library");
				 AbstractUIPage existedPage = wizardPageLibrary.getPage(pageIndex);
				 if (existedPage == null)
				 {
					 continue;
				 }
				 else
				 {
					 String existedClassName = existedPage.getClass().getCanonicalName();
					 Log.debug(45, "The canonical name of page already in library is "  +existedClassName);
					 String newClassName = page.getClass().getCanonicalName();
					 Log.debug(45, "The canonical name of given page is  "  +newClassName);
					 //they have the same page
					 if(existedClassName != null && newClassName != null && existedClassName.equals(newClassName))
					 {
						 XPathUIPageMapping existedMapping = existedPage.getXPathUIPageMapping();
						 XPathUIPageMapping newMapping = page.getXPathUIPageMapping();
						 if (existedMapping != null && newMapping != null);
						 {
					
							 Vector existedModifyingDataInfo = existedMapping.getModifyingPageDataInfoList();
							 Vector newModifyingDataInfo = newMapping.getModifyingPageDataInfoList();
							 if(existedModifyingDataInfo != null && newModifyingDataInfo != null &&
								existedModifyingDataInfo.size() == newModifyingDataInfo.size())
							 {
								 boolean allIndexSame = true;
								 for(int j=0; j<existedModifyingDataInfo.size(); j++)
								 {
									 //just out the loop if we already found a not same
									 if(!allIndexSame)
									 {
										 break;
									 }
									 ModifyingPageDataInfo existedInfo = (ModifyingPageDataInfo) existedModifyingDataInfo.elementAt(j);
									 ModifyingPageDataInfo newInfo = (ModifyingPageDataInfo) newModifyingDataInfo.elementAt(j);
									 Vector existedLoadDataPathList = existedInfo.getLoadExistingDataPath();
									 Vector newLoadDataPathList = newInfo.getLoadExistingDataPath();
									 if (existedLoadDataPathList != null && newLoadDataPathList != null &&
										 existedLoadDataPathList.size() == newLoadDataPathList.size())
									 {
										 for(int k=0; k<existedLoadDataPathList.size(); k++)
										 {
											   LoadDataPath existedPathObj = (LoadDataPath)existedLoadDataPathList.elementAt(k);
											   LoadDataPath newPathObj = (LoadDataPath)newLoadDataPathList.elementAt(k);
										       //find a index is not same, jump out the for loop
											   if(existedPathObj != null && !existedPathObj.compareTo(newPathObj))
											   {
												 Log.debug(45, "find a different node index value, so they are not same page even the page class is same");
												 allIndexSame = false;
												 break;
											   }
										 }
									 }
								 }
								 //all index are same and we need to assign isExisted = true;
								 if(allIndexSame)
								 {
								   existedPage.addXPathWithEmptyValue(errorPath);
								   page = null;
									 isExisted = true;
								 }
							 }
					    }
					 }
					 
				 }
			 }
		 }
		 Log.debug(45, "return the existed page value "+isExisted);
		 return isExisted;
	 }

}
