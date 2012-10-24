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
 *     '$Date: 2009-05-05 23:26:37 $'
 * '$Revision: 1.12 $'
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
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.EditingCompleteListener;
import edu.ucsb.nceas.morpho.framework.EditorInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.utilities.XMLUtilities;

import org.w3c.dom.Node;

/**
 * This class represents a controller which will display a serial of tree editors to correct
 * whitespace in eml 210 documents. 
 * @author tao
 *
 */
public class TreeEditorCorrectionController 
{
	private AbstractDataPackage dataPackage = null;
	private Vector xPathList = null; //There is no predicates in the path
	private EditorInterface editor = null;
	//private Node rootNode = null;
	private CorrectionTreeEditingListener listener = new CorrectionTreeEditingListener();
	private static final String SLASH ="/";
	private static final String DOUBLESLASH = "//";
	private static final String TITLE ="Tree Editor for Correcting Data Package ";
	private MorphoFrame oldFrame = null;
	private DataPackageWizardListener externalListener = null; //this listener from AddSthCommand.
	private Hashtable fullpathPosition = new Hashtable();// this hashtable keeps track of fullpath and positions
	                                                     // has been display in tree editor. It will be used 
	                                                     //how to determine index if fullpath is same
	private final static String CONFORMATION = "Morpho has successfully upgraded your data package to the newest EML version.\n Note: These changes will not become permanent until you save the document.";
	private final static String WESTBOUNDING = "westBoundingCoordinate";
	private final static String EASTBOUNDING  = "eastBoundingCoordinate";
	private final static String SOUTHBOUNDING = "southBoundingCoordinate";
	private final static String NORTHBOUNDING = "northBoundingCoordinate";
	private final static String ALTITUDEMIN  = "altitudeMinimum";
	private final static String ALTITUDEMAX  = "altitudeMaximum";
	 private final static float  NEGTIVE180     = -180;
	 private final static float POSITIVE180    = 180;
	 private final static float NEGTIVE90 = -90;
	 private final static float POSITIVE90 = 90;
	/**
	 * Constructor with parameters datapackage and xpah list
	 * @param xPathList the list of path will be displayed
	 */
	public TreeEditorCorrectionController(AbstractDataPackage dataPackage, Vector xPathList, MorphoFrame oldFrame) throws Exception
	{
		this.dataPackage = dataPackage;
		this.xPathList = xPathList;
		this.oldFrame = oldFrame;
		Log.debug(30, "the error list for tree editor is===== "+xPathList);
		Log.debug(45,  "The old frame is "+oldFrame);
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
		displayTreeEditor();
		
	}
	
	/**
	 * Get the new data package after editing
	 * @return
	 */
	public AbstractDataPackage getAbstractDataPackage()
	{
		return this.dataPackage;
	}
	
	/**
	 * Set externalListner for the controller.
	 * @param externalListener
	 */
	public void setExternalListener(DataPackageWizardListener externalListener)
	{
		this.externalListener = externalListener;
	}
	
	/*
	 * display tree editor 
	 */
	private void displayTreeEditor()
	{
		if(xPathList != null && !xPathList.isEmpty())
		{
			//Every time, we start the first xpath in the list.
			String path = (String)xPathList.firstElement();
			//Then remove the object from list. When list is empty, we went through everything.
			xPathList.remove(path);
			if (dataPackage != null && editor != null)
			{
				
				String nodeName = getNodeNameFromPath(path);
				int subTreeIndex = findPositionOfNodeNameWithInvalidValue(path, nodeName);
				if(nodeName != null && subTreeIndex != -1)
				{
				  editor.openEditor(dataPackage.getMetadataNode().getOwnerDocument(), dataPackage.getMetadataId(), 
						  dataPackage.getLocation(), listener, nodeName, subTreeIndex, false, true,  TITLE+dataPackage.getAccessionNumber());
				}
				else
				{
					//skip this path, go to next one
					displayTreeEditor();
					
				}		         		

			}
		}
		else if(xPathList != null && xPathList.isEmpty())
		{
			if(xPathList != null && xPathList.isEmpty())
			  {
				  //Tree editors is done. Tree editor correction is after page wizard correction so we can display the data now
				  // and we also can dispose the old frame
				  Log.debug(45, "\n\n********** Correction Wizard by tree editor finished: DOM:");
		          Log.debug(45, XMLUtil.getDOMTreeAsString(dataPackage.getMetadataNode()));
		          try 
		          {
		        	 
		        	 JOptionPane.showMessageDialog(oldFrame, CONFORMATION, Language.getInstance().getMessage("Warning"),
		                      JOptionPane.WARNING_MESSAGE);
		            ServiceController services = ServiceController.getInstance();
		            ServiceProvider provider =
		                services.getServiceProvider(DataPackageInterface.class);
		            DataPackageInterface dataPackageInterface = (DataPackageInterface)provider;
		            dataPackageInterface.openNewDataPackage(dataPackage, null);
		            if(oldFrame != null)
			        {
		        		    Log.debug(40,  " in the if statement to  close the old frame is "+oldFrame);
			            	oldFrame.setVisible(false);                
			            	UIController controller = UIController.getInstance();
			            	controller.removeWindow(oldFrame);
			            	oldFrame.dispose();	
			        }
		          //calling the wizardComplete method in external listener
	                if(externalListener != null)
	                {
	                	externalListener.wizardComplete(dataPackage.getMetadataNode(), null);
	                }

		          } 
		          catch (ServiceNotHandledException snhe) 
		          {

		            Log.debug(6, snhe.getMessage());
		          }
		           

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
		Log.debug(30, "=====node name is "+nodeName);
		return nodeName;
	}
	
	/*
	 * This is a trick method. In our error path list, the path is full path, such as /eml:eml/dataset/title.
	 * However, tree editor only uses nodeName, e.g. title and its position for open a subtree.
	 * So the position (or index) of "/eml:eml/dataset/title" of a node can be different to the one of tile of the same node.
	 * We need to find out the correspond index base on node name rather than full path
	 */
	private int findPositionOfNodeNameWithInvalidValue(String fullPath, String nodeName) 
	{
		int position = -1;
		Node targetNode = null;
		Log.debug(40, "in the begin of findPostion method ==========the full path and node name are "+fullPath +" and "+nodeName);
		try
		{
			if (fullPath != null && nodeName != null && fullPath.contains(nodeName))
			{
				fullPath = XMLUtilities.removeAllPredicates(fullPath);
				Log.debug(30, "the full path after removing predict in findPositionOfNodeNameWithBlankValueis "+fullPath);
				Node rootNode = dataPackage.getMetadataNode();
				//System.out.println(XMLUtilities.getDOMTreeAsString(dataPackage.getMetadataNode(), false));
				//System.out.println("==========the full path and node name are "+fullPath +" and "+nodeName);
				NodeList nodeList = XPathAPI.selectNodeList(rootNode, fullPath);	
				//If there is two same fullPath in errorList, "eml/dataset/title" "eml/dataset/title"
				// we will open two editor for them. In the first editor, we will find first node with
				// blank value, then fix it (tree is updated). So in second one, we still only look
				// for the first node with blank value.
				if (nodeList != null && nodeList.getLength() != 0)
				{
					Log.debug(40, "=========the list length is "+nodeList.getLength());
					for(int i=0; i < nodeList.getLength(); i++)
					{
						Node node = nodeList.item(i);
						Log.debug(45, "the node name========== :"+node.getLocalName());
						Log.debug(30, "node has the children "+node.getChildNodes().getLength());
						// we only find the first node
						//handle non-decimal or valid text.
						boolean valid = true;
						if(node != null && node.getChildNodes().getLength() ==0)
						{
							//this case is for case <element/>
							valid = false;
							Log.debug(30, "Find <"+nodeName+"/> and set valid "+valid);
						}
						else if(node.hasChildNodes() && node.getFirstChild().getNodeType() == node.TEXT_NODE)
						{
							String value = node.getFirstChild().getNodeValue();
							Log.debug(50, "the node text child value ========== :"+value);
							if (value != null && value.trim().equals(""))
							{
							   valid = false;
							   Log.debug(30, "Find empty string and set valid "+valid);
							}
							else if(value != null && (nodeName.equals(WESTBOUNDING) || nodeName.equals(EASTBOUNDING) ||
									nodeName.equals(NORTHBOUNDING)|| nodeName.equals(SOUTHBOUNDING) ||
									nodeName.equals(ALTITUDEMIN)|| nodeName.equals(ALTITUDEMAX)))
							{
								
								try
					        	{
					        		//test if the text is number and between -180 and 180
					        		float number = (new Float(value)).floatValue();
					        		if((nodeName.equals(WESTBOUNDING) || nodeName.equals(EASTBOUNDING) ) && (number <NEGTIVE180 || number >POSITIVE180))
					        		{
							             valid = false;
							             Log.debug(30, "value of "+nodeName+" is "+value+". It is out of range -180 to 180.");
					        		}
					        		else if((nodeName.equals(NORTHBOUNDING) || nodeName.equals(SOUTHBOUNDING) ) && (number <NEGTIVE90 || number >POSITIVE90))
					        		{
					        			valid = false;
							             Log.debug(30, "value of "+nodeName+" is "+value+". It is out of range -90 to 90.");
					        		}
					        		
					        	}
					        	catch(NumberFormatException e)
					        	{				        
					        		 valid = false;
						             Log.debug(30, "value of "+nodeName+" is "+value+". It is non-decimal text.");
					        	}
							}							
						
						}
						if(!valid)
						{
							//we found a empty value or non-decimal text index for full path. but we need to determine
							//if the one is we need.
							//Here is an example. if the eml has three element /eml:eml/dataset/keywordSet/keword
							// and the position 1 and position 3 has the empty value.
							// The tree editor will display the first one first, and put /eml:eml/dataset/keyworSet/keyowrd and 1
							//into the hashtabl. However, the user didn't fill anything into the tree editor and the document
							// still has two empty element. So when code handle the second the error path /eml:eml/dataset/keyworSet/keyowrd,
							// this for loop will hit the first position with empty value first. However, we will
							//check the hashtable and will find out we already displayed it. So it will go to the third one.
							Integer indexObj = (Integer)fullpathPosition.get(fullPath);
							Log.debug(50, "====================== the object from hashtable is "+indexObj);
							if(indexObj != null)
							{
								//we found that the tree editor already displayed the fullpath before
								int index = indexObj.intValue();
								Log.debug(40, "tree editor already displayed the fullpaht at postion"+index);
								if (index >= i)
								{
									Log.debug(40,"the current index "+i+
											" equals or is less than the stored one "+index+". So skip it");
									continue;
								}
							}
							
							Log.debug(50, "find target node =============== "+node);
							//put the fullpath and index which has empty value into hashtable
							fullpathPosition.put(fullPath, i);
							targetNode = node;
							break;
						}
					}
				}
				
				// we found the target node base on full path, then will find the match node selected base on nodename.
				// the reason we want to find the position base on node name is tree editor open a frame base on node name
				// and its position, rather than the full path and position
				if(targetNode != null)
				{
					//System.out.println("target node is not null======");
					nodeList = XPathAPI.selectNodeList(rootNode, DOUBLESLASH +nodeName);
					//System.out.println("after geting node list ==============="+nodeList.getLength());
					if (nodeList != null && nodeList.getLength() != 0)
					{
						for(int i=0; i < nodeList.getLength(); i++)
						{
							Node node = nodeList.item(i);
							//System.out.println("the node name (base on node name as the selection ========== :"+node.getLocalName());
							if (targetNode.isSameNode(node))
							{
								position = i;
								Log.debug(30, "find the position ========== "+position);
								break;
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			Log.debug(30, "Couldn't find position for node name"+ nodeName+ ". The default value -1 will be returned: "+e.getMessage());
		}
		Log.debug(30, "The position for "+nodeName+" is "+position);
		return position;
	}
	
	/*
	 * A listener class for tree editor. 
	 */
	private class CorrectionTreeEditingListener implements EditingCompleteListener
	{
	  /**
	   * This method is called when editing is complete. Pass the new datapackage to next
	   * tree editor
	   *
	   * @param xmlString is the edited XML in String format
	   */
	  public void editingCompleted(String xmlString, String id, String location)
	  {
		  StringReader sr = new StringReader(xmlString);
		  Node rootNode = null;
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
		  // then open the editor again base on the new dataPackage value
		  displayTreeEditor();
	  }
	  
	  /**
	   * this method handles canceled editing. Do nothing
	   */
	  public void editingCanceled(String xmlString, String id, String location)
	  {
		  
	  }
	}
}
