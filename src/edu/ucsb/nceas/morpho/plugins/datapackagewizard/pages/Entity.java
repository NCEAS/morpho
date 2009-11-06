/**
 *  '$RCSfile: Entity.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-29 23:39:21 $'
 * '$Revision: 1.33 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.framework.UIController;

public class Entity extends AbstractUIPage{

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID     = DataPackageWizardInterface.ENTITY;
  private final String pageNumber = "";
  private final String title      = "Data Information:";
  private final String subtitle   = "Table (Entity)";
  private String xPathRoot  = "/eml:eml/dataset/dataTable";

  private final String[] colNames =  {"Attribute Name",
                                      "Attribute Definition",
                                      "Measurement Scale"};
  private final Object[] editors  =   null; //makes non-directly-editable

  private JTextField  entityNameField;
  private JTextArea   entityDescField;
  private JLabel      entityNameLabel;
  private CustomList  attributeList;
  private JLabel      attributesLabel;
  private boolean disableAttributeList = false;
  

  private WizardContainerFrame mainWizFrame;
  
  private Vector indexListOfImportAttribute = new Vector();
  private int indexOfImport = 0;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public Entity(WizardContainerFrame frame) {
	nextPageID = DataPackageWizardInterface.SUMMARY;
    this.mainWizFrame = frame;
    init();
    addTableModelChangeListener(mainWizFrame);
  }
  
  /**
   * Display the entity page with/without disable attribute list
   * @param disableAttributeList
   */
  public Entity(Boolean disableAttributeList)
  {
	  this.disableAttributeList = disableAttributeList.booleanValue();
	  init();
  }



  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    this.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc1 = WidgetFactory.makeHTMLLabel(
      "<b>Enter some information about the data table contained in your "
      +"file.</b> "
      +"If you have more than one data table, additional tables may be added "
      +"after the completion of this wizard.",
                                                                             2);
    this.add(desc1);

    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(WidgetFactory.makeDefaultSpacer());

    ///
    JPanel attributePanel = WidgetFactory.makePanel(1);

    entityNameLabel = WidgetFactory.makeLabel("Table name:", true);

    attributePanel.add(entityNameLabel);

    entityNameField = WidgetFactory.makeOneLineTextField();
    attributePanel.add(entityNameField);

    this.add(attributePanel);

    this.add(WidgetFactory.makeDefaultSpacer());

    ////////////////////////////////////////////////////////////////////////////

    JLabel entityDesc = WidgetFactory.makeHTMLLabel(
    "Enter a paragraph that describes the table or entity, its type, and "
    +"relevant information about the data that it contains.<br></br>"
    +"<font color=\"666666\">&nbsp;&nbsp;[Example:&nbsp;&nbsp;&nbsp;Species "
    +"abundance data for 1996 at the VCR LTER site]</font>", 3);

    this.add(entityDesc);

    JPanel entityDescPanel = WidgetFactory.makePanel();

    JLabel entityLabel = WidgetFactory.makeLabel("Description", false);
    entityLabel.setVerticalAlignment(SwingConstants.TOP);
    entityLabel.setAlignmentY(SwingConstants.TOP);
    entityDescPanel.add(entityLabel);

    entityDescField = WidgetFactory.makeTextArea("", 6, true);
    JScrollPane jscrl = new JScrollPane(entityDescField);
    entityDescPanel.add(jscrl);
    this.add(entityDescPanel);

    ////////////////////////////////////////////////////////////////////////////
    if (!disableAttributeList)
    {
	    this.add(WidgetFactory.makeDefaultSpacer());
	
	    this.add(WidgetFactory.makeHTMLLabel(
	                      "One or more attributes (columns) must be defined:", 1));
	
	    JPanel attribsPanel = WidgetFactory.makePanel();
	
	    attributesLabel = WidgetFactory.makeLabel("Attributes", true);
	    attribsPanel.add(attributesLabel);
	
	    attributeList = WidgetFactory.makeList(colNames, editors, 4,
	                                    true, true, false, true, true, true );
	    attribsPanel.add(attributeList);
	
	    this.add(attribsPanel);
	
	    initActions();
    }
  }


  /**
   *  Custom actions to be initialized for list buttons
   */
  private void initActions() {

    attributeList.setCustomAddAction(

      new AbstractAction() {

        public void actionPerformed(ActionEvent e) {

          Log.debug(45, "\nEntity: CustomAddAction called");
          showNewAttributeDialog();
        }
      });

    attributeList.setCustomEditAction(

      new AbstractAction() {

        public void actionPerformed(ActionEvent e) {

          Log.debug(45, "\nEntity: CustomEditAction called");
          showEditAttributeDialog();
        }
      });
  }

  private void showNewAttributeDialog() {

    AttributePage attributePage = new AttributePage();
    ModalDialog wpd = new ModalDialog(attributePage,
                                WizardContainerFrame.getDialogParent(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT, false);
    wpd.setBounds(wpd.getX(), WizardContainerFrame.frame.getY(),
          WizardSettings.DIALOG_WIDTH, WizardSettings.ATTR_DIALOG_HEIGHT);
    wpd.setVisible(true);
    if (wpd.USER_RESPONSE==ModalDialog.OK_OPTION) {

      List newRow = attributePage.getSurrogate();
      newRow.add(attributePage);
      attributeList.addRow(newRow);
    }
    WidgetFactory.unhiliteComponent(attributesLabel);
  }


  private void showEditAttributeDialog() {

    List selRowList = attributeList.getSelectedRowList();

    if (selRowList==null || selRowList.size() < 4) return;

    Object dialogObj = selRowList.get(3);

    if (dialogObj==null || !(dialogObj instanceof AttributePage)) return;
    AttributePage editAttributePage = (AttributePage)dialogObj;

    ModalDialog wpd = new ModalDialog(editAttributePage,
                                WizardContainerFrame.getDialogParent(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT, false);
    wpd.setBounds(wpd.getX(), WizardContainerFrame.frame.getY(),
          WizardSettings.DIALOG_WIDTH, WizardSettings.ATTR_DIALOG_HEIGHT);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE==ModalDialog.OK_OPTION) {

      List newRow = editAttributePage.getSurrogate();
      newRow.add(editAttributePage);
      attributeList.replaceSelectedRow(newRow);
    }
  }


  /**
   * Adds an table model change listener to attribute list
   * @param tableModelChangeListener
   */
  public void addTableModelChangeListener(TableModelListener tableModelChangeListener )
  {
	  if(attributeList != null)
	  {
		  TableModel model = attributeList.getTAbleModel();
		  if(model != null)
		  {
			  Log.debug(30, "add an table model listener to attribute list");
			  model.addTableModelListener(tableModelChangeListener);
		  }
	  }
  }



  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

    entityNameField.requestFocus();
    removeAllImportedAttributesFromList();
  }


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {

    WidgetFactory.unhiliteComponent(attributesLabel);
  }


  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty, but if so, must
   *  return true
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    //if (entityNameField.getText().trim().equals("")) {
    if (Util.isBlank(entityNameField.getText())) {

      WidgetFactory.hiliteComponent(entityNameLabel);
      entityNameField.requestFocus();
      return false;
    }
    if(!disableAttributeList)
    {
	    if (attributeList.getRowCount() < 1) {
	
	      WidgetFactory.hiliteComponent(attributesLabel);
	      return false;
	    }
	
	    List colNames = new ArrayList();
	
	    boolean importNeeded = false;
	    AttributePage nextAttributePage = null;
	    List rowLists = attributeList.getListOfRowLists();
	    if (rowLists==null) return true;
	    int index = 1;
	    int attrsToBeImported = 0;
	
	    //AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
	    AbstractDataPackage adp = mainWizFrame.getAbstractDataPackage();
	    if(adp == null) {
		Log.debug(10, "Error! Unable to obtain the ADP in the Entity page!");
	    } else {
		attrsToBeImported = adp.getAttributeImportCount();
		indexOfImport = adp.getAttributeImportCount();
	    }
	    String entityName = entityNameField.getText().trim();
	    for (Iterator it = rowLists.iterator(); adp != null && it.hasNext(); ) {
	
	      Object nextRowObj = it.next();
	      if (nextRowObj==null) continue;
	
	      List nextRowList = (List)nextRowObj;
	      //column 2 is user object - check it exists and isn't null:
	      if (nextRowList.size()<4)     continue;
	      Object nextUserObject = nextRowList.get(3);
	      if (nextUserObject==null) continue;
	
	      String colName = (String) nextRowList.get(0);
	      nextAttributePage = (AttributePage)nextUserObject;
	      if(nextAttributePage.isImportNeeded()) {
	
	        OrderedMap map = nextAttributePage.getPageData(xPathRoot + "/attributeList/attribute["+index + "]");
	        String mScale = (String) nextRowList.get(2);
	        adp.addAttributeForImport(entityName, colName, mScale, map, xPathRoot + "/attributeList/attribute["+index+ "]", true);
	        indexListOfImportAttribute.add(indexOfImport);
	        indexOfImport++;
	        importNeeded = true;
	      }
	      colNames.add(colName);
	      index++;
	    }
	    if(adp != null) {
		adp.setLastImportedEntity(entityName);
		adp.setLastImportedAttributes(colNames);
		adp.setLastImportedDataSet(null);
		/*	if(vec != null) adp.setLastImportedDataSet(vec);
		else {
		    adp.setLastImportedDataSet(((UneditableTableModel)table.getModel()).getDataVector());
		    }*/
	    } 
	
	
	    if(attrsToBeImported > 0) {
		this.nextPageID = DataPackageWizardInterface.CODE_DEFINITION;
	
	    } else if(importNeeded) {
		this.nextPageID = DataPackageWizardInterface.CODE_IMPORT_SUMMARY;
	
	    }
    }
    return true;
  }


  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */

  private OrderedMap returnMap = new OrderedMap();
  //
  public OrderedMap getPageData() {

    returnMap.clear();

    returnMap.put(xPathRoot + "/entityName",
                  entityNameField.getText().trim());

    String entityDesc = entityDescField.getText().trim();
    //if (!entityDesc.equals("")) {
    if (!Util.isBlank(entityDesc)) {
      returnMap.put(xPathRoot + "/entityDescription", entityDesc);
    }
    if(!disableAttributeList)
    {
	    returnMap.put(xPathRoot + "/physical/objectName", WizardSettings.UNAVAILABLE);
	    returnMap.put(xPathRoot + "/physical/dataFormat", "");
	
	    int index = 1;
	    Object  nextRowObj      = null;
	    List    nextRowList     = null;
	    Object  nextUserObject  = null;
	    OrderedMap  nextNVPMap  = null;
	    AttributePage nextAttributePage = null;
	
	    List rowLists = attributeList.getListOfRowLists();
	
	    if (rowLists==null) return null;
	
	    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
	
	      nextRowObj = it.next();
	      if (nextRowObj==null) continue;
	
	      nextRowList = (List)nextRowObj;
	      //column 2 is user object - check it exists and isn't null:
	      if (nextRowList.size()<4)     continue;
	      nextUserObject = nextRowList.get(3);
	      if (nextUserObject==null) continue;
	
	      nextAttributePage = (AttributePage)nextUserObject;
	
	      nextNVPMap = nextAttributePage.getPageData(xPathRoot
	                                + "/attributeList/attribute["+(index++) + "]");
	      returnMap.putAll(nextNVPMap);
	    }
    }
    return returnMap;
  }



  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular wizard page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular wizard page
   */
  public OrderedMap getPageData(String rootXPath) {

     if (!disableAttributeList)
     {
    	 throw new UnsupportedOperationException(
           "getPageData(String rootXPath) Method Not Implemented");
     }
     else
     {
    	   returnMap.clear();
    	   returnMap.put(xPathRoot + "/entityName",
    	                  entityNameField.getText().trim());

    	    String entityDesc = entityDescField.getText().trim();
    	    //if (!entityDesc.equals("")) {
    	    if (!Util.isBlank(entityDesc)) {
    	      returnMap.put(xPathRoot + "/entityDescription", entityDesc);
    	    }
    	    return returnMap;
     }
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return pageID; }

  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() { return title; }

  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() { return subtitle; }

  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() { return nextPageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }
   
    /**
     * Set ordered map to the page
     */
    public boolean setPageData(OrderedMap map, String _xPathRoot) 
    { 
    	//this method only is implemented when diableAttributeList is true
    	 if(disableAttributeList)
    	 {
	    	 if (_xPathRoot != null )
	    	 {
	    		 this.xPathRoot =_xPathRoot;
	    	 }
	    	 Log.debug(45,"PartyPage.setPageData() called with rootXPath = " + xPathRoot
	                 + "\n Map = \n" + map);
	    	 String nextVal = (String)map.get(xPathRoot+ "/entityName");
			  if (nextVal != null) 
			  {
				  entityNameField.setText(nextVal);
				  map.remove(xPathRoot+ "/entityName");
			   }
				nextVal = (String)map.get(xPathRoot+ "/entityDescription");
				if (nextVal != null) 
				{
					entityDescField.setText(nextVal);
				   map.remove(xPathRoot+ "/entityDescription");
				}
				//if anything left in map, then it included stuff we can't handle...
			    boolean canHandleAllData = map.isEmpty();

			    if (!canHandleAllData) {

			      Log.debug(20,
			                "Entity.setPageData returning FALSE! Map still contains:"
			                + map);
			    }
			    return canHandleAllData;
    	 }
    	 else
    	 {
    		 //TO DO need to be implemented when disableAttributeList is false
    	    return false;
    	 }
    }
    
    
    /**
     * Removes all imported attributes which are determined in this page
     */
    public void removeAllImportedAttributesFromList()
    {
    	if(mainWizFrame != null)
    	{
    	   AbstractDataPackage adp = mainWizFrame.getAbstractDataPackage();
    	   if(adp != null && indexListOfImportAttribute != null)
    	   {
    		   int size = indexListOfImportAttribute.size();
    		   for(int i=size; i>=0; i--)
    		   {
    			   int index = -1;
    			   try
    			   {
    			      index = ((Integer)indexListOfImportAttribute.elementAt(i)).intValue();
    			      adp.removeAttributeForImport(index);
    			   }
    			   catch(Exception e)
    			   {
    				   continue;
    			   }
    		   }
    	   }
    	   indexListOfImportAttribute = new Vector();
    	}
    }
}
