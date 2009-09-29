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

import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.ModifyingPageDataInfo;
import edu.ucsb.nceas.morpho.util.XPathUIPageMapping;

/**
 * Represents methods can be repeatedly used in this plugin
 * @author tao
 *
 */
public class XpathUIPageMappingReader
{
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
	private Hashtable fullPathMapping = new Hashtable();
	private Hashtable shortPathMapping = new Hashtable();
	private XPathUIPageMapping[] mappingList = null;
	private Hashtable wizardPageClassName = new Hashtable();
	
	/**
	 * Default constructor. It will read the property file.
	 */
	public XpathUIPageMappingReader(String fileName)
	{
		mappingList = readMappingFromFile(fileName);
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
				        	   else if ((node.getNodeType() == Node.ELEMENT_NODE) && 
				        			   (node.getNodeName().equalsIgnoreCase(KEY)))
				        	   {
				        		   Node textNode = node.getFirstChild();
				        		   if(textNode != null && textNode.getNodeType() == Node.TEXT_NODE)
				        		   {
				        			   info.setKey(textNode.getNodeValue());
				        		   } 
				        	   }
				        	   else if ((node.getNodeType() == Node.ELEMENT_NODE) && 
				        			   (node.getNodeName().equalsIgnoreCase(PREVNODE)))
				        	   {
				        		   Node textNode = node.getFirstChild();
				        		   if(textNode != null && textNode.getNodeType() == Node.TEXT_NODE)
				        		   {
				        			   info.addPrevNode(textNode.getNodeValue());
				        		   } 
				        	   }
				        	   else if ((node.getNodeType() == Node.ELEMENT_NODE) && 
				        			   (node.getNodeName().equalsIgnoreCase(NEXTNODE)))
				        	   {
				        		   Node textNode = node.getFirstChild();
				        		   if(textNode != null && textNode.getNodeType() == Node.TEXT_NODE)
				        		   {
				        			   info.addNextNode(textNode.getNodeValue());
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
				      //put className-mapping object into a hashtable
				      wizardPageClassName.put(className, unit);
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
	
	/**
	 * Gets the mapping list from the property file
	 * @return
	 */
	public XPathUIPageMapping[] getXPathUIPageMappingList()
	{
		return mappingList;
	}
	
	/**
	 * Gets full path mapping
	 * @return
	 */
	public Hashtable getFullPathMapping()
	{
		return fullPathMapping;
	}
	
	/**
	 * Gets short path mapping
	 * @return
	 */
	public Hashtable getShortPathMapping()
	{
		return shortPathMapping;
	}
	
	/**
	 * Gets a hashtable containing className-mappingObject
	 * @return
	 */
	public Hashtable getClassNameMapping()
	{
		return wizardPageClassName;
		
	}
}
