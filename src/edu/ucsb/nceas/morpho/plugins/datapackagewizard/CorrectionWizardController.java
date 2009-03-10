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
 *     '$Date: 2009-03-10 01:40:45 $'
 * '$Revision: 1.1 $'
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
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Log;

import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents a controller which will start a process to 
 * correct valid values in eml 210 documents. This class will be passed a list
 * of xml paths which contain invalid value, e.g. white spaces. First, the class
 * will figure out which wizard pages should be used upon the xml paths. Then, 
 * it will introduce wizard page to user, which can help to correct error.
 * @author tao
 *
 */
public class CorrectionWizardController 
{
	private Vector errorPathList = null;
	private Vector pageList = new Vector();
	// The hasttable containing the mapping which be read from mapping property file
	private MappingUnit[] mappingList = null;
	// the file path of mapping properties file (xml format)
	private static final String MAPPINGFILEPATH = "lib/xpath-wizard-map.xml";
	// element name used in mapping properties file (xml format)
	private static final String MAPPING = "mapping";
	private static final String XPATH = "xpath";
	private static final String WIZARDAGECLASS = "wizardPageClass";
	private static final String ROOT = "root";
	
	
	/**
	 * Constructor
	 * @param errorPathList the list of paths which contain invalid value
	 */
	public CorrectionWizardController(Vector errorPathList)
	{
	    this.errorPathList = errorPathList;
	    this.mappingList  = getXPATHMappingUIPage();
	}
	
	/*
	 * Gets list of pages for correcting the invalid value. First we will
	 * look the mapping between wizard page and xpath. If we find one, 
	 * the wizard page will bet put into list. If we couldn't find one, tree editor
	 * for this xpath will be put into list.
	 */
	private void getUIPageList()
	{
		if (errorPathList != null)
		{
			for (int i=0; i<errorPathList.size(); i++)
			{
				AbstractUIPage page = getUIPage((String)errorPathList.elementAt(i));
				if (page != null)
				{
					// find a wizard page and add it to the list
					pageList.add(page);
				}
				else
				{
					//no wizard page found and add a tree editor to the list
					//Todo
				}
			}
		}
	}
	
	
	/*
	 * Returns a UI page for given xml path. If no page can be found, null will be returned.
	 */
	private AbstractUIPage getUIPage(String path) 
	{
		//Todo
		AbstractUIPage page = null;
		if (mappingList != null)
		{
			
		}
		return page;
	}
	
	/*
	 * Gets the mapping between xpath and UIPage. 
	 * A hash table will be returned. The key is xpath and value is UIPage
	 */
	private MappingUnit[] getXPATHMappingUIPage() 
	{
	    return readFromFile(MAPPINGFILEPATH);
	}
	
	/*
	 * Reads the mapping properties file
	 */
	private MappingUnit[] readFromFile(String fileName) 
	{
		MappingUnit[] mappingList = null;
		try
		{
		   ConfigXML mappingProperties = new ConfigXML(fileName);
		   Document doc = mappingProperties.getDocument();
		   NodeList nl = doc.getElementsByTagName(MAPPING);
		    if (nl.getLength() > 0)
		    {
		      mappingList = new MappingUnit[nl.getLength()];
		      for(int i = 0; i<nl.getLength(); i++)
		      {
			      NodeList children = nl.item(i).getChildNodes();
			      MappingUnit unit = new MappingUnit();
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
			              unit.setRoot(ccn.getNodeValue());
			            }
			          }
			          else if ((cn.getNodeType() == Node.ELEMENT_NODE)
			              && (cn.getNodeName().equalsIgnoreCase(XPATH)))
			          {
			            Node ccn = cn.getFirstChild();        // assumed to be a text node
			            if ((ccn != null) && (ccn.getNodeType() == Node.TEXT_NODE))
			            {
			               unit.setXpath( ccn.getNodeValue());
			            }
			          }
			          else if ((cn.getNodeType() == Node.ELEMENT_NODE)
				              && (cn.getNodeName().equalsIgnoreCase(WIZARDAGECLASS)))
				       {
				            Node ccn = cn.getFirstChild();        // assumed to be a text node
				            if ((ccn != null) && (ccn.getNodeType() == Node.TEXT_NODE))
				            {
				               unit.setWizardPageClassName(( ccn.getNodeValue()));
				            }
				        }
			        }
			      }
			      mappingList[i] = unit;
		      }
		    }
		   if (mappingList != null)
		   {
			   for (int i=0; i<mappingList.length; i++)
			   {
				   MappingUnit unit = mappingList[i];
				   if (unit != null)
				   {
				     Log.debug(48, "mapping is "+ unit.getRoot() + " "+unit.getXpath() + " "+
				    		        unit.getWizardPageClassName());
				   }
			   }
		   }
		}
		catch (Exception e)
		{
			Log.debug(30, "Couldn't get the mapping property from the file "+e.getMessage());
		}
		return mappingList;		
	}
	
	/*
	 * This class represents a unit in mapping property file:
	 * 
	 * <mapping>
     *   <root>/eml:eml/dataset/</root>
     *    <xpath>title</xpath>
     *    <wizardPageClass>General</wizardPageClass>
     *  </mapping>
	 */
	private class MappingUnit
	{
		private String root = null;
		private String xpath = null;
		private String wizardPageClassName = null;
		
		public MappingUnit()
		{
			
		}
		public String getRoot() 
		{
			return root;
		}
		public void setRoot(String root) 
		{
			this.root = root;
		}
		public String getXpath() 
		{
			return xpath;
		}
		public void setXpath(String xpath) 
		{
			this.xpath = xpath;
		}
		public String getWizardPageClassName() 
		{
			return wizardPageClassName;
		}
		public void setWizardPageClassName(String wizardPageClassName) 
		{
			this.wizardPageClassName = wizardPageClassName;
		}
		
	}
}
