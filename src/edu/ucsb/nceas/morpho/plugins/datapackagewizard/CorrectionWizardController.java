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
 *     '$Date: 2009-04-10 03:25:48 $'
 * '$Revision: 1.16 $'
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
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributePage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Entity;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	// metadata in AbrstractDataPackage format
	AbstractDataPackage dataPackage = null;
	//private Document metadataDoc = null;
	private DataPackageWizardListener listener = new CorrectionDataPackageWizardListener();
	private DataPackageWizardPlugin plugin = new DataPackageWizardPlugin();
	private CorrectionWizardContainerFrame dpWiz = null;
	// the file path of mapping properties file (xml format)
	private static final String MAPPINGFILEPATH = "lib/xpath-wizard-map.xml";
	// element name used in mapping properties file (xml format)
	private static final String MAPPING = "mapping";
	private static final String XPATH = "xpath";
	private static final String WIZARDAGECLASS = "wizardPageClass";
	private static final String ROOT = "root";
	private static final String LOADDATAFROMROOTPATH = "loadDataFromRootPath";
	private static final String WIZARDCONTAINERFRAME = "edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame";
	private static final String CORRECTIONSUMMARY = "edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.CorrectionSummary";
	private static final String TITLE = "Correction Wizard";
	private static final String STARTPAGEID = "0";
	private static final String PARA = "para";
	private static final String SLASH = "/";
	private final static String DATATABLE = "dataTable";
	private final static String ATTRIBUTE = "attribute";
	private final static String RIGHTBRACKET = "]";
	private final static String LEFTBRACKET = "[";
	private final static char RIGHTBRACKETCHAR = ']';
	private final static char LEFTBRACKETCHAR = '[';
	private final static String DATATABLEPATH = "/eml:eml/dataset/dataTable";
	private final static String ATTRIBUTEPATH ="//attributeList/attribute";
	
	/**
	 * Constructor
	 * @param errorPathList the list of paths which contain invalid value
	 */
	public CorrectionWizardController(Vector errorPathList, AbstractDataPackage dataPackage)
	{
	    this.errorPathList  = errorPathList;
	    this.dataPackage  = dataPackage;
	    dpWiz = new CorrectionWizardContainerFrame(dataPackage);
	    // find the mapping between xpath and page class name in a file
	    this.mappingList   = getXPATHMappingUIPage();
	    assignErrorPath();// assign the error path to UI page list or tree editor path list

	}
	
	/**
	 * Start to run the wizard
	 */
	public void startWizard()
	{
		// first to run wizard page to fix the issue
		if(!wizardPageLibrary.isEmpty())
		{
		    //this part will open a tree editor too if pathListForTreeEditor is not empty
			//the DataPackageWizardListener will trigger to open tree editor
			dpWiz.setWizardPageLibrary(wizardPageLibrary);
		    dpWiz.setDataPackageWizardListener(listener);
		    dpWiz.setBounds(
		                  WizardSettings.WIZARD_X_COORD, WizardSettings.WIZARD_Y_COORD,
		                  WizardSettings.WIZARD_WIDTH,   WizardSettings.WIZARD_HEIGHT );
		    dpWiz.setCurrentPage(STARTPAGEID);
		    dpWiz.setShowPageCountdown(false);
		    dpWiz.setTitle(TITLE);
		    dpWiz.setVisible(true);
		}
		else if( pathListForTreeEditor != null && !pathListForTreeEditor.isEmpty())
		{
			//there is no UIPage returned, we only run tree editor to fix the issue
			try
			{
				TreeEditorCorrectionController treeEditorController = new TreeEditorCorrectionController(dataPackage, pathListForTreeEditor);
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
					// set up next id for previous page
					if (previousPage != null)
					{
						previousPage.setNextPageID(pageIDstr);
					}
					previousPage = page;
					// find a wizard page and add it to the list
					wizardPageLibrary.addPage(pageIDstr, page);
					pageID ++;
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
				//AbstractUIPage summaryPage = WizardPageLibrary.getPage(DataPackageWizardInterface.CORRECTION_SUMMARY);
				AbstractUIPage summaryPage = createAbstractUIpageObject(CORRECTIONSUMMARY,dpWiz);
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
			XPathUIPageMapping mapping = getXPathUIPageMapping(path);
			if(mapping != null)
			{
				className = mapping.getWizardPageClassName();
				if(className != null)
				{
					OrderedMap xpathMap = null;
					page = createAbstractUIpageObject(className, dpWiz);
					//load data into the page
					//first we need to check if we should load data from root path.
					boolean loadDataFromRoot = mapping.isLoadDataFromRootPath();
					NodeList nodeList = null;
					if (loadDataFromRoot)
					{	
						//page.setXPathRoot(node);
						if (page instanceof AttributePage)
						{
							int entityIndex = getDataTableIndex(path);
							int attributeIndex = getAttributeIndex(path);
							nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(), DATATABLEPATH);
							Node entityNode = nodeList.item(entityIndex);
							nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(),ATTRIBUTEPATH);
							Node node = nodeList.item(attributeIndex);
							xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node, "/eml:eml/dataset/dataTable/attributeList");							
							page.setRootNodeIndex(entityIndex);
							AttributePage aPage =(AttributePage)page;
							aPage.setAttributeIndex(attributeIndex);
							page = aPage;
						} 
						else if(page instanceof Entity)
						{
							nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(), mapping.getRoot());	
							Node node = nodeList.item(0);
							xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node, "");
							int entityIndex = getDataTableIndex(path);
							page.setRootNodeIndex(entityIndex);
						}
						else
						{
							nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(), mapping.getRoot());	
							Node node = nodeList.item(0);
							xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node, "");
							page.setRootNodeIndex(0);
						}
					}
					else
					{
						//load data from every root+xpath(like General page)
						String xPath = null;
						Vector list = mapping.getXpath();
						boolean firstTime = true;
						for (int i=0; i<list.size(); i++)
						{
							xPath = (String)list.elementAt(i);
							//System.out.println("==========the xpath is "+xPath);
							xPath = removeParaFromXPath(xPath);
							nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(), mapping.getRoot()+SLASH+xPath);
							Node node = nodeList.item(0);
							if(firstTime)
							{
							  xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node);
							  firstTime = false;
							  //we set first child as the root node for not loading data from root path directly
							  //page.setXPathRoot(node);
							}
							else
							{
								xpathMap.putAll(XMLUtilities.getDOMTreeAsXPathMap(node));
							}
							page.setRootNodeIndex(0);
						}
						
					}	
					Log.debug(46, "the xmpath map is "+xpathMap.toString());
					mapDataFit = page.setPageData(xpathMap, "");
				}
		
			}
		}
		//The map has more data that our page can handle. so set page to null and let tree editor to handle it.
		if(!mapDataFit)
		{
			page = null;
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
			Log.debug(48, "the given error path without all predicates is"+path);
			if (fullPathMapping != null)
			{
				mapping = (XPathUIPageMapping)fullPathMapping.get(path);
				Log.debug(48, "find the xpath and uipage mapping in full path mapping "+mapping.getClass());
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
	 * Gets the mapping between xpath and UIPage. 
	 * A hash table will be returned. The key is xpath and value is UIPage
	 */
	private XPathUIPageMapping[] getXPATHMappingUIPage() 
	{
	    return readMappingFromFile(MAPPINGFILEPATH);
	}
	
	/*
	 * Reads the mapping properties file
	 */
	private XPathUIPageMapping[] readMappingFromFile(String fileName) 
	{
		XPathUIPageMapping[] mappingList = null;
		try
		{
		   ConfigXML mappingProperties = new ConfigXML(fileName);
		   Document doc = mappingProperties.getDocument();
		   NodeList nl = doc.getElementsByTagName(MAPPING);
		    if (nl.getLength() > 0)
		    {
		      mappingList = new XPathUIPageMapping[nl.getLength()];
		      for(int i = 0; i<nl.getLength(); i++)
		      {
		    	try
		    	{
			      NodeList children = nl.item(i).getChildNodes();
			      XPathUIPageMapping unit = new XPathUIPageMapping();
			      String root = null;
		          String xpath = null;
		          String className = null;
		          String loadDataFromRoot = null;
			      if (children.getLength() > 0)
			      {
			        for (int j = 0; j < children.getLength(); j++)
			        {
			          Node cn = children.item(j);
			          
			          if ((cn.getNodeType() == Node.ELEMENT_NODE)
			              && (cn.getNodeName().equalsIgnoreCase(ROOT)))
			          {
			            Node ccn = cn.getFirstChild();        // assumed to be a text node
			            if ((ccn != null) && (ccn.getNodeType() == Node.TEXT_NODE))
			            {
			              root = ccn.getNodeValue();
			              unit.setRoot(root);
			            }
			          }
			          else if ((cn.getNodeType() == Node.ELEMENT_NODE)
			              && (cn.getNodeName().equalsIgnoreCase(XPATH)))
			          {
			            Node ccn = cn.getFirstChild();        // assumed to be a text node
			            if ((ccn != null) && (ccn.getNodeType() == Node.TEXT_NODE))
			            {
			               xpath = ccn.getNodeValue();
			               unit.addXpath(xpath);
			            }
			          }
			          else if ((cn.getNodeType() == Node.ELEMENT_NODE)
				              && (cn.getNodeName().equalsIgnoreCase(WIZARDAGECLASS)))
				       {
				            Node ccn = cn.getFirstChild();        // assumed to be a text node
				            if ((ccn != null) && (ccn.getNodeType() == Node.TEXT_NODE))
				            {
				               className = ccn.getNodeValue();
				               unit.setWizardPageClassName(className);
				            }
				        }
			          else if ((cn.getNodeType() == Node.ELEMENT_NODE)
				              && (cn.getNodeName().equalsIgnoreCase(LOADDATAFROMROOTPATH)))
				       {
				            Node ccn = cn.getFirstChild();        // assumed to be a text node
				            if ((ccn != null) && (ccn.getNodeType() == Node.TEXT_NODE))
				            {
				               loadDataFromRoot = ccn.getNodeValue();
				               boolean loadData = true;
				               loadData = (new Boolean(loadDataFromRoot)).booleanValue();
				               unit.setLoadDataFromRootPath(loadData);
				            }
				        }
			            
			         }			        
			          mappingList[i] = unit;
				      Vector list = unit.getXpath();
				      if (list != null)
				      {
				    	  for(int j=0; j<list.size(); j++)
				    	  {
				    		  String shortPath = (String)list.elementAt(j);
				    		  String fullPath = root+SLASH+shortPath;
				    		  Log.debug(48, "put "+fullPath+" into full path mapping");
				    		  Log.debug(48, "put short path "+ shortPath +" into short path mapping");
				    		  fullPathMapping.put(fullPath, unit);
						      shortPathMapping.put(xpath, unit);
				    	  }
				      }
			      }		      
		      }
		       catch(Exception e)
		       {
		        	continue;
		       }
		    }//for
		   /*if (mappingList != null)
		   {
			   for (int i=0; i<mappingList.length; i++)
			   {
				   XPathUIPageMapping unit = mappingList[i];
				   if (unit != null)
				   {
				     Log.debug(48, "mapping is "+ unit.getRoot() + " "+unit.getXpath() + " "+
				    		        unit.getWizardPageClassName());
				   }
			   }
		   }*/
		   Log.debug(48, "full path mapping is "+fullPathMapping);
		   Log.debug(48, "short path mapping is "+shortPathMapping);
		  }
		}
		catch (Exception e)
		{
			Log.debug(30, "Couldn't get the mapping property from the file "+e.getMessage());
		}
		return mappingList;		
	}
	
	/*
	 * Create an object for given class name. This is only for AbractUIPage object
	 */
	private static AbstractUIPage createAbstractUIpageObject(String className, WizardContainerFrame frame)  {

		Object object = null;
		Class classDefinition = null;
		try {
			classDefinition = Class.forName(className);
			object = classDefinition.newInstance();
		} catch (InstantiationException e) {
			Log.debug(30, "InstantiationException "+e.getMessage());
			// couldn't get default constructor. The contructor has a parameter WizardContainerFrame
			try
			{
				if (classDefinition != null)
				{
					Class parameter= Class.forName(WIZARDCONTAINERFRAME);
					Class[] parameterList = new Class[1];
					parameterList[0] = parameter;
					Constructor constructor = classDefinition.getDeclaredConstructor(parameterList);
					object = constructor.newInstance(frame);
				}
			}
			catch(Exception ee)
			{
				Log.debug(30, "Exception "+e.getMessage());
			}
		} catch (IllegalAccessException e) {
			Log.debug(30, "IllegalAccessException "+e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.debug(30, "ClassNotFoundException "+e.getMessage());
		} 
		catch(Exception e)
		{
			Log.debug(30, "Exception in creating an UI page object through the class name "+className + " is " +e.getMessage());
		}
		return (AbstractUIPage)object;
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
		int index = getGivenStringIndexAtXPath(DATATABLE, path);
		return index;
	}
	
	/*
	 * Get the attribute index for a given xpath.
	 * If the path is "eml/dataset/datatable[2]/attributeList/attribute[1], 1 will be returned.
	 * if no index found, -1 will be returned
	 */
	private int getAttributeIndex(String path)
	{
		int index = getGivenStringIndexAtXPath(ATTRIBUTE, path);
		return index;
	}
	
	/*
	 * Get the given string index for a given xpath.
	 * If the path is "eml/dataset/datatable[2] and given string is datatable, 2 will be returned.
	 * if no index found, -1 will be returned
	 */
	private int getGivenStringIndexAtXPath(String givenString, String path)
	{
		int index = -1;
		if(path != null)
		{
			int position = path.lastIndexOf(givenString+LEFTBRACKET);
			int length = path.length();
			if (position != -1)
			{
				// "dataTable[" exists
				try
				{
				   char singleChar = path.charAt(position);
				   StringBuffer buffer = new StringBuffer();
				   boolean startBuffer = false;
				   while (singleChar != RIGHTBRACKETCHAR && position < length)
				   {
					   // start to buffer
					   if(startBuffer)
					   {
						   buffer.append(singleChar);
					   }
					   //when we see [, set the flag to bute
					   if (singleChar == LEFTBRACKETCHAR)
					   {
						   startBuffer = true;
					   }
					   //increase position number
					   position++;
					   singleChar = path.charAt(position);
				   }
				   if (buffer.length() != 0)
				   {
					   String indexStr = buffer.toString();
					   index = (new Integer(indexStr)).intValue();
				   }
				}
				catch(Exception e)
				{
					Log.debug(30,"Couldn't " +givenString+" index for path "+path+ " "+e.getMessage());
				}
				
			}
		}
		return index;
	}
	

	
	 /*
	  * This class is the listener of CorrectionDataPackageFrame. It will implement the method
	  * wizardComplete. In this method, it will trigger to start a tree editor correction controller
	  * if the pathListForTreeEditor is not empty.
	  */
	private class CorrectionDataPackageWizardListener implements DataPackageWizardListener
	{

		public void wizardComplete(Node newDOM) {

	          Log.debug(30,
	              "Correction Wizard UI Page complete ");
	          AbstractDataPackage adp = DataPackageFactory.getDataPackage(newDOM);
	          Log.debug(30, "AbstractDataPackage complete");
	          adp.setAccessionNumber("temporary.1.1");
	          //second, to correct data by tree editor
			    if(pathListForTreeEditor != null)
			    {
			    	//there is no UIPage returned, we only run tree editor to fix the issue
					try
					{
						TreeEditorCorrectionController treeEditorController = new TreeEditorCorrectionController(adp, pathListForTreeEditor);
						treeEditorController.startCorrection();
					}
					catch(Exception e)
					{
						Log.debug(5, "Couldn't run tree editor to correct the eml210 document "+e.getMessage());
					}
			    }
			    else
			    {        
                      //no tree editor is needed, so we can display the data now
			          try {
			            ServiceController services = ServiceController.getInstance();
			            ServiceProvider provider =
			                services.getServiceProvider(DataPackageInterface.class);
			            DataPackageInterface dataPackage = (DataPackageInterface)provider;
			            dataPackage.openNewDataPackage(adp, null);
		
			          } catch (ServiceNotHandledException snhe) {
		
			            Log.debug(6, snhe.getMessage());
			          }
			           Log.debug(45, "\n\n********** Correction Wizard finished: DOM:");
			           Log.debug(45, XMLUtilities.getDOMTreeAsString(adp.getMetadataNode(), false));
			        }
		     }


	        public void wizardCanceled() {

	          Log.debug(45, "\n\n********** Correction Wizard canceled!");
	        }
	}

}
