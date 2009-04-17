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
 *     '$Date: 2009-04-17 17:43:24 $'
 * '$Revision: 1.11 $'
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.Attribute;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributePage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.General;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Taxonomic;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.ModifyingPageDataInfo;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.XPathUIPageMapping;
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
	private AbstractDataPackage dataPackage = null;
	private Node rootNode = null;
	//private Vector pathListForTreeEditor = new Vector();
	
	/**
	 * Constructor
	 * @param docRootNode
	 */
	public CorrectionWizardContainerFrame(AbstractDataPackage dataPackage)
	{
		this.dataPackage = dataPackage;
	}
	
	/**
	 * The method to collect data from pages in node format
	 */
	public Node collectDataFromPages() 
	{
		//Node rootNode = getNewEmptyDataPackageDOM(WizardSettings.NEW_EML210_DOCUMENT_TEXT);
		if (dataPackage != null)
		{
	       //first, to correct data by UIPage
		    while (!pageStack.isEmpty()) 
		    {
		       //System.out.println("pageStack is not empty");
		       OrderedMap wizData = new OrderedMap();
		       AbstractUIPage page = (AbstractUIPage)pageStack.pop();
		       Node newSubTree = null;
		       if(page != null)
		       {
		    	   XPathUIPageMapping mapping = page.getXPathUIPageMapping();
		    	   Log.debug(30, "the page in collection data is "+page.getPageID());
			       
			       if (mapping != null)
			       {
		    	       Vector infoList = mapping.getModifyingPageDataInfoList();
			    	   if(infoList != null && infoList.size()>1)
			    	   {
			    		   wizData = page.getPageData(((ModifyingPageDataInfo)infoList.elementAt(0)).getPathForgettingPageData());
			    		   //This UI page will generate more than one sub trees.
			    		   //The order of the list of generic path name should be as same as the order of sub trees
			    	      for(int i=0; i<infoList.size(); i++)
			    	      {
			    	    	  ModifyingPageDataInfo info = (ModifyingPageDataInfo)infoList.elementAt(0);
			    	    	  //System.out.println("generic name");
			                   //Go through the every subtree
				    		   Set set = wizData.keySet();
				    		   Iterator iterator = set.iterator();
				    		   while(iterator.hasNext())
				    		   {
				    			   String key = (String)iterator.next();
				    			   //System.out.println("key "+key);
				    			   String value = (String)wizData.get(key);
				    			   //System.out.println("value "+value);
				    			   OrderedMap data = new OrderedMap();
				    			   data.put(key, value);		    			   
				    			   try
				    			   {			    			      
				    			    
				    			      modifyDataPackage(data, info, page);
				    			   }
				    			   catch(Exception e)
				    			   {
				    				   Log.debug(30, "Failed to replace old subtree "+e.getMessage());
				    				   continue;
				    			   }			    			 
				    		   }
			    	         }
			    	      }
			    	      else if(infoList != null && infoList.size()==1)
			    	      {
			    	    	  ModifyingPageDataInfo info = (ModifyingPageDataInfo)infoList.elementAt(0);
			    	    	  wizData = page.getPageData(info.getPathForgettingPageData());
			    	    	  try
			    	    	  {
			    	    	     modifyDataPackage(wizData, info, page);
			    	    	  }
			    	    	   catch(Exception e)
			    			   {
			    				   Log.debug(30, "Failed to replace old subtree "+e.getMessage());
			    			   }			    
			    	      }
		            }
		    			    	   
		       }
		  
		       
		    }
	
		  
		}
		
	    Log.debug(45, "\n\n********** Correction Wizard finished: DOM:");
	    Log.debug(45, XMLUtilities.getDOMTreeAsString(dataPackage.getMetadataNode()));
	    return dataPackage.getMetadataNode();
	  }
	
	  
	/*
	 *Repalce a subtree of the dataPakcage by a new subtree with given data and position
	 */
	  private void modifyDataPackage(OrderedMap data, ModifyingPageDataInfo info, AbstractUIPage page) throws Exception
	  {
		  if(dataPackage != null && page != null)
		  {
			 
	          if (page instanceof AttributePage)
	          {	     
	        	    dataPackage.getEntityArray();
	        	    int entityIndex =   (Integer)page.getNodeIndexList().elementAt(0);
	        	    int attrIndex    =  (Integer) page.getNodeIndexList().elementAt(1);
	        	    Log.debug(45, "======attribute is in entity "+entityIndex+ " and postition is "+attrIndex+" with data "+data.toString());
	        	    String oldID = dataPackage.getAttributeID(entityIndex, attrIndex);
	        	    Log.debug(45, "old id is "+oldID);
	        		if(oldID == null || oldID.trim().equals("")) oldID = UISettings.getUniqueID();
	                 data.put("/attribute/@id", oldID);
	                //Log.debug(45, data.toString());
	                Attribute attr = new Attribute(data);
	                dataPackage.insertAttribute(entityIndex, attr, attrIndex);
	        		dataPackage.deleteAttribute(entityIndex, attrIndex + 1);
	          }
	          else
	          {
	        	  DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
		    	  Document doc = impl.createDocument("", info.getDocumentName(), null);
		          Node newSubTree = doc.getDocumentElement();	         
		          Log.debug(45, "before creating new tree with data ====================="+data.toString());
		          XMLUtilities.getXPathMapAsDOMTree(data, newSubTree);     
		          Log.debug(45, "after creating new tree  ====================="+newSubTree);
		          Node check = null;
		          if (page instanceof Taxonomic)
		          {
		        	  try 
		        	  {
		        	      dataPackage.removeTaxonomicNodes();
		        	      // now the covRoot node may have a number of geographicCoverage children
		        	      NodeList kids = newSubTree.getChildNodes();
		        	      //Log.debug(45, "=============new subtree list has the length  "+kids.getLength());
		        	      for (int i=0;i<kids.getLength();i++) {
		        	        Node kid = kids.item(i);
		        	        //Log.debug(45, "kid is ==================="+kid);
		        	        dataPackage.insertCoverage(kid);          
		        	      }
		        	    }
		        	    catch (Exception w) {
		        	      Log.debug(5, "Unable to insert new taxomonic page"+w.getMessage());
		        	    }
		          }
		          else
		          {
			          if (newSubTree != null)
			          {
			        	  Log.debug(45, "before deleting subtree =====================");
			              dataPackage.deleteSubtree(info.getGenericName(), (Integer)page.getNodeIndexList().elementAt(0));
			              Log.debug(45, "after deleting subtree and insert new tree====================="+newSubTree);
			              // add to the datapackage
			              check = dataPackage.insertSubtree(info.getGenericName(), newSubTree, (Integer)page.getNodeIndexList().elementAt(0));
			          }
		             if (check != null) 
			         {
			            Log.debug(45, "added new subtree to package...");
			          } 
			          else 
			          {
			            Log.debug(5, "** ERROR: Unable to add new subtree into package **");
			          }
		          }
	          }
	
	          
	      }
		  
	  }
	  
	  
}
