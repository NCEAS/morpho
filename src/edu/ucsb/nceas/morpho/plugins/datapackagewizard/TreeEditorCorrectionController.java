/**
 *  '$RCSfile: TreeEditorCorrectionController.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Jing Tao
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-01 23:18:33 $'
 * '$Revision: 1.3 $'
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
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.EditingCompleteListener;
import edu.ucsb.nceas.morpho.framework.EditorInterface;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.XMLUtilities;


/**
 * This class represents a controller which will display a serial of tree editors to correct
 * whitespace in eml 210 documents. 
 * @author tao
 *
 */
public class TreeEditorCorrectionController 
{
	private AbstractDataPackage dataPackage = null;
	private Vector xPathList = null;
	private EditorInterface editor = null;
	private Node rootNode = null;
	private int index = 0; //track the index of path in the xPathList
	private CorrectionTreeEditingListener listener = new CorrectionTreeEditingListener();
	private static final String SLASH ="/";
	
	/**
	 * Constructor with parameters datapackage and xpah list
	 * @param xPathList the list of path will be displayed
	 */
	public TreeEditorCorrectionController(AbstractDataPackage dataPackage, Vector xPathList) throws Exception
	{
		this.dataPackage = dataPackage;
		this.xPathList = xPathList;	
        try
        {
          ServiceController services = ServiceController.getInstance();
          ServiceProvider provider = 
                          services.getServiceProvider(EditorInterface.class);
          editor = (EditorInterface)provider;
        }
        catch(Exception ee)
        {
          Log.debug(0, "Error acquiring editor plugin: " + ee.getMessage());
          ee.printStackTrace();
          throw ee;
        }
	}
	
	/**
	 * Start to correct the eml by displaying the tree editors one by one
	 */
	public void startCorrection()
	{
        //display the first tree editor. The next editor will be display by listener.editedCompleted
		displayTreeEditor(index);
		
	}
	
	/**
	 * Get the root node of the DOM tree after editing
	 * @return
	 */
	public Node getRootNode()
	{
		return this.rootNode;
	}
	
	/*
	 * display tree editor at the given index of path list
	 */
	private void displayTreeEditor(int indexOfPathList)
	{
		if (dataPackage != null && xPathList != null && editor != null)
		{
			String path = (String)xPathList.elementAt(indexOfPathList);
			String nodeName = getNodeNameFromPath(path);
			int subTreeIndex = 0;
			if(nodeName != null)
			{
			  editor.openEditor(dataPackage.getMetadataNode().getOwnerDocument(), dataPackage.getPackageId(), 
					  dataPackage.getLocation(), listener, nodeName, subTreeIndex, false);
			}
			else
			{
				editor.openEditor(XMLUtilities.getDOMTreeAsString(dataPackage.getMetadataNode()), dataPackage.getAccessionNumber(), 
						  dataPackage.getLocation(), listener);
			}
	         		
			
		}
	}
	
	/*
	 * Gets the node name from xpath. E.g. it will return title when the given path is /eml:eml/dataset/title
	 */
	private String getNodeNameFromPath(String path)
	{
		String nodeName = null;
		if (path != null)
		{
			int position = path.lastIndexOf(SLASH);
			if (position != -1 && position < (path.length()-1))
			{
				// slash is not the last character
				nodeName = path.substring(position+1);
			}
			else if (position != -1 && position == (path.length()-1))
			{
				//slash is the last character: /ab/c/
				if(path.length() >1)
				{
					// the path not only contains "/"
					String pathWithoutLastSlash = path.substring(0, position);
					nodeName = getNodeNameFromPath(pathWithoutLastSlash);
				}
				
			}
			else if(position ==-1)
			{
				nodeName = path;
			}
			
			
		}
		Log.debug(30, "node name is "+nodeName);
		return nodeName;
	}
	
	/*
	 * A listener class for tree editor. 
	 */
	private class CorrectionTreeEditingListener implements EditingCompleteListener
	{
	  /**
	   * This method is called when editing is complete
	   *
	   * @param xmlString is the edited XML in String format
	   */
	  public void editingCompleted(String xmlString, String id, String location)
	  {
		  //first we need to update the index;
		  index++;
		  StringReader sr = new StringReader(xmlString);
		  try
		  {
			  rootNode = XMLUtilities.getXMLReaderAsDOMTreeRootNode(sr);
		  }
		  catch(Exception e)
		  {
			  Log.debug(30, "couldn't put the xml string into a node "+e.getMessage());
		  }
	      AbstractDataPackage newadp = DataPackageFactory.getDataPackage(rootNode);
	      dataPackage = newadp;		 
		  // when the index hit the end of pathList vector. The editing is really done
		  if(index == (xPathList.size() -1))
		  {
			  //no tree editor is needed, so we can display the data now
	          try {
	            ServiceController services = ServiceController.getInstance();
	            ServiceProvider provider =
	                services.getServiceProvider(DataPackageInterface.class);
	            DataPackageInterface dataPackageInterface = (DataPackageInterface)provider;
	            dataPackageInterface.openNewDataPackage(dataPackage, null);

	          } catch (ServiceNotHandledException snhe) {

	            Log.debug(6, snhe.getMessage());
	          }
	           Log.debug(45, "\n\n********** Correction Wizard finished: DOM:");
	           Log.debug(45, XMLUtilities.getDOMTreeAsString(dataPackage.getMetadataNode(), false));
			  return;
		  }
		  // then open the editor
		  displayTreeEditor(index);
	  }
	  
	  /**
	   * this method handles canceled editing
	   */
	  public void editingCanceled(String xmlString, String id, String location)
	  {
		  
	  }
	}
}
