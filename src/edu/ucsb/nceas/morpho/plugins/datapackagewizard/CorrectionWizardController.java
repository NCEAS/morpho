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
 *     '$Date: 2009-04-19 23:27:28 $'
 * '$Revision: 1.22 $'
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
import edu.ucsb.nceas.morpho.util.LoadDataPath;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.ModifyingPageDataInfo;
import edu.ucsb.nceas.morpho.util.XPathUIPageMapping;
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
					//if a page with same data exists in the library, we should skip this page.
					boolean checkPageExisted = isUIPageExisted(page);
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
				AbstractUIPage summaryPage = createAbstractUIpageObject(CORRECTIONSUMMARY,dpWiz, null);
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
				if(className != null)
				{
					OrderedMap xpathMap = null;
					page = createAbstractUIpageObject(className, dpWiz, mapping.getWizardPageClassParameters());
					page.setXPathUIPageMapping(mapping);
					//load data into the page
					//first we need to check if we should load data from root path.
				    Vector infoList = mapping.getModifyingPageDataInfoList();
					NodeList nodeList = null;
					String settingPageDataPath = "";
					if (infoList != null && infoList.size() ==1)
					{	
						ModifyingPageDataInfo info = (ModifyingPageDataInfo)infoList.elementAt(0);
						settingPageDataPath = info.getPathForSettingPageData();
						//page.setXPathRoot(node);
						if (page instanceof AttributePage)
						{
							int entityIndex = getDataTableIndex(path);
							int attributeIndex = getAttributeIndex(path);
							Vector loadExistingDataPathList = info.getLoadExistingDataPath();
							LoadDataPath entityPath = (LoadDataPath)loadExistingDataPathList.elementAt(0);
							entityPath.setPosition(entityIndex);
							LoadDataPath attributePath = (LoadDataPath)loadExistingDataPathList.elementAt(1);
							attributePath.setPosition(attributeIndex);							
							nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(),entityPath.getPath());
							Node entityNode = nodeList.item(entityIndex);
							nodeList = XMLUtilities.getNodeListWithXPath(entityNode, attributePath.getPath());
							Node node = nodeList.item(attributeIndex);
							xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node,info.getPathForCreatingOrderedMap());							

						} 
						else if(page instanceof Entity)
						{
							nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(), mapping.getRoot());	
							Node node = nodeList.item(0);
							xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node, "");
							int entityIndex = getDataTableIndex(path);
							//page.addNodeIndex(entityIndex);
						}
						else
						{
							Log.debug(45, "start to process load data process which has info list size 1");
							Vector loadExistingDataPathList = info.getLoadExistingDataPath();
							int position = getLastPredicate(path);
							if (loadExistingDataPathList != null)
							{
								Node node = null;
								for(int k=0; k<loadExistingDataPathList.size(); k++)
								{
								  LoadDataPath loadPath = (LoadDataPath)loadExistingDataPathList.elementAt(k);
								  // store the position information
								  loadPath.setPosition(position);
								  String xpath = loadPath.getPath();
								  nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(), xpath);	
								  node = nodeList.item(position);
								}
								Log.debug(45, "before getting ordered map from subtree");
							    xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node, info.getPathForCreatingOrderedMap());
								  
							}
						}
					}
					else if (infoList != null && infoList.size() > 1)
					{
						//like General page
						Log.debug(45, "start to process load data process which has info list size more than 1");
						Vector list = mapping.getModifyingPageDataInfoList();
						boolean firstTime = true;
						for (int i=0; i<list.size(); i++)
						{
							ModifyingPageDataInfo info =(ModifyingPageDataInfo)list.elementAt(i);
							settingPageDataPath = info.getPathForSettingPageData();
							LoadDataPath pathObj = (LoadDataPath)info.getLoadExistingDataPath().elementAt(0);
							String xPath = pathObj.getPath();
							pathObj.setPosition(0);
							//System.out.println("==========the xpath is "+xPath);
							nodeList = XMLUtilities.getNodeListWithXPath(dataPackage.getMetadataNode(), xPath);
							Node node = nodeList.item(0);
							if(firstTime)
							{
							  xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node, info.getPathForCreatingOrderedMap());
							  firstTime = false;
							  //we set first child as the root node for not loading data from root path directly
							  //page.setXPathRoot(node);
							}
							else
							{
								xpathMap.putAll(XMLUtilities.getDOMTreeAsXPathMap(node));
							}
						}
						
					}	
					else
					{
						page = null;
					}
					Log.debug(46, "the xmpath map is "+xpathMap.toString());
					if (page != null)
					{
					   mapDataFit = page.setPageData(xpathMap, settingPageDataPath);
					}
				}
		
			}
		}
		Log.debug(46, "The map data fit value is "+mapDataFit);
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
			              Log.debug(55, "found the root element "+root);
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
				               Log.debug(55, "found the class name "+className);
				               unit.setWizardPageClassName(className);
				            }
				        }
			          else if ((cn.getNodeType() == Node.ELEMENT_NODE)
				              && (cn.getNodeName().equalsIgnoreCase(WIZARDAGECLASSPARAMETER)))
				       {
				            Node ccn = cn.getFirstChild();        // assumed to be a text node
				            if ((ccn != null) && (ccn.getNodeType() == Node.TEXT_NODE))
				            {
				               String classParameter = ccn.getNodeValue();
				               unit.addWizardPageClassParameters(classParameter);
				            }
				        }
			          else if ((cn.getNodeType() == Node.ELEMENT_NODE)
				              && (cn.getNodeName().equalsIgnoreCase(INFORORMODIFYINGDATA)))
				       {
				           ModifyingPageDataInfo info = new ModifyingPageDataInfo();
				           NodeList secondList = cn.getChildNodes();
				           for (int k=0; k<secondList.getLength(); k++)
				           {
				        	   Node node = secondList.item(k);
				        	   if((node.getNodeType() == Node.ELEMENT_NODE) && 
				        			   (node.getNodeName().equalsIgnoreCase(LOADEXISTDATAPATH)))
				        	   {
				        		   Node textNode = node.getFirstChild();
				        		   if(textNode != null && textNode.getNodeType() == Node.TEXT_NODE)
				        		   {
				        			   info.addLoadExistingDataPath(textNode.getNodeValue());
				        		   } 
				        	   }
				        	   else if ((node.getNodeType() == Node.ELEMENT_NODE) && 
				        			   (node.getNodeName().equalsIgnoreCase(XPATHFORGETTINGPAGEDATA)))
				        	   {
				        		   Node textNode = node.getFirstChild();
				        		   if(textNode != null && textNode.getNodeType() == Node.TEXT_NODE)
				        		   {
				        			   info.setPathForgettingPageData(textNode.getNodeValue());
				        		   } 
				        	   }
				        	   else if ((node.getNodeType() == Node.ELEMENT_NODE) && 
				        			   (node.getNodeName().equalsIgnoreCase(NEWDATADOCUMENTNAME)))
				        	   {
				        		   Node textNode = node.getFirstChild();
				        		   if(textNode != null && textNode.getNodeType() == Node.TEXT_NODE)
				        		   {
				        			   info.setDocumentName(textNode.getNodeValue());
				        		   } 
				        	   }
				        	   else if ((node.getNodeType() == Node.ELEMENT_NODE) && 
				        			   (node.getNodeName().equalsIgnoreCase(GENERICNAME)))
				        	   {
				        		   Node textNode = node.getFirstChild();
				        		   if(textNode != null && textNode.getNodeType() == Node.TEXT_NODE)
				        		   {
				        			   info.setGenericName(textNode.getNodeValue());
				        		   } 
				        	   }
				        	   else if ((node.getNodeType() == Node.ELEMENT_NODE) && 
				        			   (node.getNodeName().equalsIgnoreCase(XPAHTFORCREATINGORDEREDMAP)))
				        	   {
				        		   Node textNode = node.getFirstChild();
				        		   if(textNode != null && textNode.getNodeType() == Node.TEXT_NODE)
				        		   {
				        			   info.setPathForCreatingOrderedMap(textNode.getNodeValue());
				        		   } 
				        	   }
				        	   else if ((node.getNodeType() == Node.ELEMENT_NODE) && 
				        			   (node.getNodeName().equalsIgnoreCase(XPATHFORSETTINGPAGEDATA)))
				        	   {
				        		   Node textNode = node.getFirstChild();
				        		   if(textNode != null && textNode.getNodeType() == Node.TEXT_NODE)
				        		   {
				        			   info.setPathForSettingPageData(textNode.getNodeValue());
				        		   } 
				        	   }
				        	   
				        		  
				           }
				           unit.addModifyingPageDataInfo(info);
				        }
			            
			         }			        
			          mappingList[i] = unit;
			          Log.debug(55, "single mapping is "+unit);
				      Vector list = unit.getXpath();
				      Log.debug(55, "xpath list is "+list);
				      Log.debug(55, "root is "+unit.getRoot());
				      if (list != null && !list.isEmpty())
				      {
				    	  for(int j=0; j<list.size(); j++)
				    	  {
				    		  String shortPath = (String)list.elementAt(j);
				    		  String fullPath = root+SLASH+shortPath;
				    		  Log.debug(55, "put "+fullPath+" into full path mapping");
				    		  Log.debug(55, "put short path "+ shortPath +" into short path mapping");
				    		  fullPathMapping.put(fullPath, unit);
						      shortPathMapping.put(xpath, unit);
				    	  }
				      }
				      else if(unit.getRoot() != null)
				      {
				    	  //if there is no xpath, only root. Put the root into full path 
				    	  String fullPath = unit.getRoot();
				    	  Log.debug(45, "put "+fullPath+" into full path mapping");
			    		  fullPathMapping.put(fullPath, unit);
				    	  
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
		   Log.debug(55, "full path mapping is "+fullPathMapping);
		   Log.debug(55, "short path mapping is "+shortPathMapping);
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
	private static AbstractUIPage createAbstractUIpageObject(String className, WizardContainerFrame frame, Vector parameters)  {

		Object object = null;
		Class classDefinition = null;
		try {
			classDefinition = Class.forName(className);
			//we only consider String and boolean
			if(parameters != null && !parameters.isEmpty())
			{
				Class[] parameterList = new Class[parameters.size()];
				Object[] objectList = new Object[parameters.size()];
				for(int i=0; i<parameters.size(); i++)
				{
					String para = (String)parameters.elementAt(i);
					if(para.equalsIgnoreCase("false") || para.equalsIgnoreCase("true"))
					{
						Class parameter= Class.forName("java.long.Boolean");
						parameterList[i] = parameter;	
						objectList[i]= new Boolean(para);
					}
					else
					{
						Class parameter = Class.forName("java.lang.String");
						parameterList[i] = parameter;
						objectList[i] = para;
					}
				}
				Constructor constructor = classDefinition.getDeclaredConstructor(parameterList);
				object =constructor.newInstance(objectList);
			} 
			else
			{
			    object = classDefinition.newInstance();
			}
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
		Log.debug(30, "successfully create the instance of this class "+className);
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
					Log.debug(35, "=============the last predicate is "+position);
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

		public void wizardComplete(Node newDOM) {

	          Log.debug(30,
	              "Correction Wizard UI Page complete ");
	          AbstractDataPackage adp = DataPackageFactory.getDataPackage(newDOM);
	          Log.debug(30, "AbstractDataPackage complete");
	          adp.setAccessionNumber("temporary.1.1");
	          //second, to correct data by tree editor
			    if(pathListForTreeEditor != null && !pathListForTreeEditor.isEmpty())
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
	 private boolean isUIPageExisted(AbstractUIPage page)
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
				 AbstractUIPage existedPage = wizardPageLibrary.getUIPage(pageIndex);
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
