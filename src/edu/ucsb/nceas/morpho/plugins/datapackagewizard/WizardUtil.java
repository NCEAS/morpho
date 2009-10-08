/**
 *  '$RCSfile: WizardSettings.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-20 18:26:05 $'
 * '$Revision: 1.77 $'
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

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.LoadDataPath;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.ModifyingPageDataInfo;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.morpho.util.XPathUIPageMapping;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * Represents methods can be repeatedly used in this plugin
 * @author tao
 *
 */
public class WizardUtil 
{
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
	public static Hashtable fullPathMapping = new Hashtable();
	public static Hashtable shortPathMapping = new Hashtable();
	
	
	
	/**
	 * Create an object for given class name. This is only for AbractUIPage object
	 * @param className
	 * @param frame
	 * @param parameters
	 * @return
	 */
	public static AbstractUIPage createAbstractUIpageObject(String className, WizardContainerFrame frame, Vector parameters)  {

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
						Class parameter= Class.forName("java.lang.Boolean");
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
			Log.debug(30, "InstantiationException "+e.getMessage() +" and we will try to instance this object again with parameter WizardContainerFrame.");
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
		Log.debug(30, "Finally create the instance "+object);
		return (AbstractUIPage)object;
	}
	
	
	/**
	 * Gets an AbstractUIPage base on the given information. This page will contain exist data
	 * @param className  the class name of UIPage
	 * @param mapping     the mapping info for this class
	 * @param dpWiz        the containFrame which the page will stay
	 * @param path           the path contains empty value (for correction wizard only). If it is null, this method will not be used by correction wizard.
	 * @return
	 * @throws Exception
	 */
  	public static AbstractUIPage getUIPage(String className, XPathUIPageMapping mapping, WizardContainerFrame dpWiz, AbstractDataPackage dataPackage, String path) throws Exception
  	{
  		AbstractUIPage page = null;
  		boolean mapDataFit = false;
  				
		if(mapping != null)
		{
			className = mapping.getWizardPageClassName();
			Log.debug(45, "get the className from mapping "+className);
			if(className != null)
			{
				OrderedMap xpathMap = null;
				page = WizardUtil.createAbstractUIpageObject(className, dpWiz, mapping.getWizardPageClassParameters());
				page.setXPathUIPageMapping(mapping);
				//load data into the page
				//first we need to check if we should load data from root path.
			    Vector infoList = mapping.getModifyingPageDataInfoList();
				NodeList nodeList = null;
				String settingPageDataPath = "";
				if (infoList != null && infoList.size() > 0)
				{
					//like General page
					Log.debug(45, "start to process load data process which has info list size more than 0");
					Vector list = mapping.getModifyingPageDataInfoList();
					boolean firstTime = true;
					for (int i=0; i<list.size(); i++)
					{
						ModifyingPageDataInfo info =(ModifyingPageDataInfo)list.elementAt(i);
						settingPageDataPath = info.getPathForSettingPageData();
						Vector loadDataPathList = info.getLoadExistingDataPath();
						
						if (loadDataPathList != null)
						{
							Node node = dataPackage.getMetadataNode();
							//If loadDataPathList has mutiple xpath, the second xpath is kid of the first one.
							//like : <loadExistDataPath>/eml:eml/dataset</loadExistDataPath>
				            //        <loadExistDataPath>./title</loadExistDataPath>
							if(node != null)
							{
								String loadNodeListStatus = info.getLoadingNodeListStatus();
								// we only need to a single node to the page as existed metadata. not a list
								if(loadNodeListStatus == null)
								{
									for(int j=0; j<loadDataPathList.size(); j++)
									{
										LoadDataPath pathObj = (LoadDataPath)loadDataPathList.elementAt(j);
										String xPath = pathObj.getPath();
										int position = 0;
										if(path != null)
										{
										   String lastElementName = getLastElementName(xPath);
										   position = getGivenStringIndexAtXPath(lastElementName, path);	
										}
										//System.out.println("==========the xpath is "+xPath);
										Log.debug(46, "Before getting the node list for path"+xPath);
										nodeList = XMLUtilities.getNodeListWithXPath(node, xPath);
										Log.debug(46, "After getting the node list for path"+xPath);
										//reset node
										if(nodeList != null)
										{
											node = nodeList.item(position);
											Log.debug(46, "Getting the node for path"+xPath+" at position "+position+ " "+node);
											pathObj.setPosition(position);
										}
										else
										{
											node = null;
										}
									}
									if(firstTime && node != null)
									{
									  Log.debug(46, "Before First time to create xPathMap with path for creating ordered map "+info.getPathForCreatingOrderedMap());
									  xpathMap = XMLUtilities.getDOMTreeAsXPathMap(node, info.getPathForCreatingOrderedMap());
									  Log.debug(46, "After First time to create xPathMap");
									  firstTime = false;
									  //we set first child as the root node for not loading data from root path directly
									  //page.setXPathRoot(node);
									}
									else if (node != null)
									{
										Log.debug(46, "Before second or more time to create xPathMap");
										xpathMap.putAll(XMLUtilities.getDOMTreeAsXPathMap(node, info.getPathForCreatingOrderedMap()));
										Log.debug(46, "After second or more time to create xPathMap");
									}
							    }
								else if(loadNodeListStatus.equals(ModifyingPageDataInfo.TRANSFORMLOADINGNODELIST))
								{
									// we need load a node list data into UIPage.
									//this is for keywords, owner, contact or associated party pages
									// currently we only handle one loading path in this suitation.
									LoadDataPath pathObj = (LoadDataPath)loadDataPathList.elementAt(0);
									String loadPath = pathObj.getPath();
									if(loadPath != null)
									{
										nodeList = XMLUtilities.getNodeListWithXPath(node, loadPath);
									    List newNodetList = dataPackage.getSubtree(nodeList);
									    xpathMap = Util.getOrderedMapFromNodeList(newNodetList, info.getGenericName());
									}
								}
								else if(loadNodeListStatus.equals(ModifyingPageDataInfo.DIRECTLOADINGNODELIST))
								{
									// we need to load a node list data into ui page.
									// this is for geographic, time and taxonamic pages.
									// currently we only handle one loading path in this suitation.
									LoadDataPath pathObj = (LoadDataPath)loadDataPathList.elementAt(0);
									String loadPath = pathObj.getPath();
									if(loadPath != null)
									{
										nodeList = XMLUtilities.getNodeListWithXPath(node, loadPath);
									    if(nodeList != null)
									    {
									    	for(int k=0; k<nodeList.getLength(); k++)
									    	{
									    		xpathMap = XMLUtilities.getDOMTreeAsXPathMap(nodeList.item(k));
									    		page.setPageData(xpathMap, settingPageDataPath);
					
									    	}
									    }
									}
									return page;
								}
							}
							
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
				   Log.debug(46, "We will set map data into map since page is not null");
				   mapDataFit = page.setPageData(xpathMap, settingPageDataPath);
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
	
	/**
	 * Get the given string index for a given xpath.
	 * If the path is "eml/dataset/datatable[2] and given string is datatable, 2 will be returned.
	 * if no index found, 0 will be returned
	 */
	public static int getGivenStringIndexAtXPath(String givenString, String path)
	{
		int index = 0;
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
		Log.debug(40, "The predication number is "+index+" for elemen"+givenString+" in path "+path);
		return index;
	}
	
	/*
	 * Gets the last element name for a given path.
	 * E.g, "/eml/dataset" will return "dataset", "/eml/dataset/" will return dataset as well.
	 * "dataset" will return "dataset"
	 */
	private static String getLastElementName(String path)
	{
		String elementName = null;
		if (path != null)
		{
			int end = path.lastIndexOf(SLASH);
			if (end ==-1)
			{
				// no slash at all
				elementName = path;
			}
			else if(end == (path.length() -1)) 
			{
				// the last character is "/"
				if (end != 0)
				{
					String pathWithoutLastSlash = path.substring(0, path.length()-1);
					elementName = getLastElementName(pathWithoutLastSlash);
				}
				else
				{
					// the given path is "/"
					return elementName;
				}
			}
			else
			{
				// "/eml/dataset" format
				elementName = path.substring(end+1);
			}
		}
		Log.debug(40, "The last element in the given path "+path + " is "+elementName);
		return elementName;
	}
	
}
