/**
 *  '$RCSfile: CodeDefinition.java,v $'
 *    Purpose: A class that handles the importing of new tables for taxonomical
 *						lookup for attributes
 *  	Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AccessionNumber;
import edu.ucsb.nceas.morpho.datapackage.Attribute;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

import java.io.File;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This page will given user an interface to assign which col is code and which col is definition.
 * In CodeImportPage, user has ability to choose the code/definition was already in package or
 * need to import a new table. If user chooses "need to import a new table", 
 * this page will be shown when wizard finish importing a table which contains code/definition. 
 * The start of importing the code/definition table is at CodeImportPage.
 * Its previous pages can be TextImportAttribute_index or Entity page.
 * Note: those previous pages are used to handle code/definition table.
 * @author tao
 *
 */
public class CodeDefinition extends AbstractUIPage {

  public final String pageID = DataPackageWizardInterface.CODE_DEFINITION;
  
  public final String pageNumber = "";
  
  public final String title      = "Code Defintions Import Page";
  public final String subtitle   = "Define the columns for the codes and definitions";

  private WizardContainerFrame mainWizFrame;
  private OrderedMap resultsMap;

  private CodeDefnPanel importPanel = null;


  private JTextField attrField;
  private JTextField entityField;

  private AbstractDataPackage adp = null;
  private String prevPageID = null;
  private ArrayList removedAttributeInfo = null;
  private String handledImportAttributeName = null;
  private boolean entityAdded = false;//indicates if entity already added to adp
  //private int selectedEntityIndex = -1;
  private int selectedCodeColumnIndex = -1;
  private int selectedDefinitionColumnIndex = -1;
  
 

  public CodeDefinition(WizardContainerFrame mainWizFrame) {

    this.mainWizFrame = mainWizFrame;
    nextPageID = DataPackageWizardInterface.CODE_IMPORT_SUMMARY;
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    setLayout(new BorderLayout());

    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
    JLabel label = WidgetFactory.makeHTMLLabel("Identify the columns of the new data table that contain the Codes and Definitions for the following Attribute - ", 1, false);

    JLabel attrLabel = WidgetFactory.makeLabel("Attribute Name:", false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    attrField = WidgetFactory.makeOneLineTextField();
    attrField.setEditable(false);

    JPanel attrPanel = WidgetFactory.makePanel();
    attrPanel.add(attrLabel);
    attrPanel.add(attrField);

    JLabel entityLabel = WidgetFactory.makeLabel("Entity Name:", false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    entityField = WidgetFactory.makeOneLineTextField();
    entityField.setEditable(false);

    JPanel entityPanel = WidgetFactory.makePanel();
    entityPanel.add(entityLabel);
    entityPanel.add(entityField);

    infoPanel.add(label);
    infoPanel.add(WidgetFactory.makeDefaultSpacer());
    infoPanel.add(attrPanel);
    infoPanel.add(WidgetFactory.makeDefaultSpacer());
    infoPanel.add(entityPanel);
    infoPanel.add(WidgetFactory.makeDefaultSpacer());


    add(infoPanel, BorderLayout.NORTH);

    importPanel = new CodeDefnPanel(true, false); // CodeDefnPanel with only the definition part but without the data tables being created
    add(importPanel, BorderLayout.CENTER);

  }


  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
	 //removedAttributeInfo = null;
     adp = getADP();
     if(adp == null) {
	Log.debug(10, "Error! Unable to obtain the ADP in CodeDefinition page!");
	return;
     }
		
    if(removedAttributeInfo != null)
    {
    	adp.addFirstAttributeForImport(removedAttributeInfo);
    	removedAttributeInfo = null;
    }
    handledImportAttributeName = adp.getCurrentImportAttributeName();
    String entity = adp.getCurrentImportEntityName();

    attrField.setText(handledImportAttributeName);
    entityField.setText(entity);

   
    Vector rowData = adp.getLastImportedDataSet();
    prevPageID = mainWizFrame.getPreviousPageID();
    //if(prevPageID.equals(DataPackageWizardInterface.TEXT_IMPORT_WIZARD) || prevPageID.equals(DataPackageWizardInterface.ENTITY)) {
    if(prevPageID != null && !entityAdded && (prevPageID.startsWith(DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE) || prevPageID.equals(DataPackageWizardInterface.ENTITY))) {
      //adds the the new entity collected from previous pages into package.
      addNewEntityToAbstractDataPackage();
     
    }
    String tableName = adp.getLastImportedEntity();
    List attrs =  adp.getLastImportedAttributes();
    //Log.debug(5, "The attribtue list "+attrs);
    
    if(prevPageID.equals(DataPackageWizardInterface.ENTITY) && rowData == null) { 
      
      //data not yet read from the file. This happens when user does a MANUAL import
      // read data from the file. If its a non-text file, put "**nontext**" in first row of the columns
      // At the end of this, rowData is a valid vector of row data.


      int entityIdx = adp.getEntityCount() - 1;
      boolean text_file = false;
      String format = adp.getPhysicalFormat(entityIdx, 0);
      if(format.indexOf("Text") > -1 || format.indexOf("text") > -1 || format.indexOf("Asci") > -1 || format.indexOf("asci") > -1) {
    text_file = true;
      }
      
      MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
      DataViewContainerPanel resultPane = null;
      Morpho morpho = null;
      if(morphoFrame != null) {
    resultPane = morphoFrame.getDataViewContainerPanel();
      }
      if(resultPane != null) {
    morpho = resultPane.getFramework();
      }

      File entityFile = CodeDefnPanel.getEntityFile(morpho, adp, entityIdx);
      if(entityFile == null) return;
      Vector colsToExtract = new Vector();
      for(int ci = 0; ci < attrs.size(); ci++) colsToExtract.add(new Integer(ci));
      int numHeaderLines = 0;
      String field_delimiter = adp.getPhysicalFieldDelimiter(entityIdx, 0);
      String delimiter = getDelimiterString(field_delimiter);
      boolean ignoreConsecutiveDelimiters = adp.ignoreConsecutiveDelimiters(entityIdx, 0);
      List data = null;
      if(text_file) {
    data = CodeDefnPanel.getColumnValues(entityFile, colsToExtract, numHeaderLines, delimiter, ignoreConsecutiveDelimiters, WizardSettings.MAX_IMPORTED_ROWS_DISPLAYED_IN_CODE_IMPORT);
      } else {
    // not a displayable data; hence just create a single empty row (with the necessary columns) to add to the resultset
    data = new ArrayList();
    List row1 = new ArrayList();
    for(int ci = 0; ci < colsToExtract.size(); ci++) row1.add("**nontext**");
    data.add(row1);
      }
      rowData = new Vector();
      TaxonImportPanel.addColumnsToRowData(rowData, data);

  } 
    
    importPanel.setTable(tableName, attrs, rowData);
    importPanel.invalidate();
    if(selectedCodeColumnIndex != -1 && selectedDefinitionColumnIndex != -1 )
    {
      Log.debug(35, "Before selecting the code column "+selectedCodeColumnIndex+" and defintion columns "+selectedDefinitionColumnIndex+" in CodeImportPage.onLoad ");
      int[] columnsIndex = {selectedCodeColumnIndex, selectedDefinitionColumnIndex};//the first is code, the second is definition
      //Log.debug(5, "import panel is "+importPanel);
      try
      {
         importPanel.setSelectedCodeDefColumnInTable(columnsIndex);
      }
      catch(Exception e)
      {
        Log.debug(35, "Couldn't select code/definition column in CodeDefinition.onLoad since "+e.getMessage());
      }
      Log.debug(35, "After selecting the code and defintion columns in CodeImportPage.onLoad "+selectedCodeColumnIndex);
    }
    //adp.setLastImportedAttributes(null);
    //adp.setLastImportedEntity(null);
    //adp.setLastImportedDataSet(null);

  }


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   */
  public void onRewindAction() {

	  if(prevPageID != null && (prevPageID.startsWith(DataPackageWizardInterface.TEXT_IMPORT_ATTRIBUTE) || prevPageID.equals(DataPackageWizardInterface.ENTITY))) {
    
	         int entityIndex = mainWizFrame.getEnityIndex();
	         //entityIndex = entityIndex -1;//since the mainWizFrame stores the next available index, so we should minus 1 when we go back. 
		     Log.debug(32, "The index of the entity which was deleted to abstract package in CodeDefinition.onRewindAction is "+entityIndex);
		     adp.deleteEntity(entityIndex);
		     //mainWizFrame.setEntityIndex(entityIndex);
	         adp.setLocation("");  // we've changed it and not yet saved
	         entityAdded = false; //since we deleted the added entity, we need to set it to be false.
	         removedAttributeInfo = null;//clicking back button will set this value to null.
	  }
	  //adp.addFirstAttributeForImport(removedAttributeInfo);
	  selectedCodeColumnIndex = -1;
	  selectedDefinitionColumnIndex = -1;
  }

  /**
   *  The action to be executed when the "Next" button is pressed.
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    if(importPanel.validateUserInput()) {
      OrderedMap map = adp.getCurrentImportMap();
      //String relativeXPath = adp.getCurrentImportXPath();
      //String scale = adp.getCurrentImportScale().toLowerCase();
      //String path = relativeXPath + "/measurementScale/" + scale + "/nonNumericDomain/enumeratedDomain[1]/entityCodeList";
      
      //get new  map for reference id in import attribute
      replaceEmptyReference(map,importPanel, "CodeDefinition");
      //replace the attribute with the new reference ids
      if(adp.isCurrentImportNewTable())
      {
    	  Log.debug(30, "====it is in current import new table and previous page is code_definition in CodeImportSummary.onLoad");
    	  updateImportAttributeInNewTable(adp, map);

      }
      removedAttributeInfo = adp.removeFirstAttributeForImport();
      //selectedEntityIndex = importPanel.getSelectedEntityIndex();
      selectedCodeColumnIndex = importPanel.getSelectedCodeColumnIndexInTable();
      selectedDefinitionColumnIndex = importPanel.getSelectedDefColumnIndexInTable();
      //Log.debug(5, "The name of handledImport attributeName is "+handledImportAttributeName);
      //Log.debug(5, "the size of imported attribute is "+adp.getAttributeImportCount());
      return true;
    } else
      return false;

  }
  
  /**
   * Replace empty reference data by real data after importing is done
   * @param map the original map
   * @param importPane  panel contains real reference data
   * @param className  for error message to indicate which class
   */
  public static void replaceEmptyReference(OrderedMap map,CodeDefnPanel importPane, String className)
  {

	  if(map != null)
	  {
		  Iterator it = map.keySet().iterator();
	      String prefix = "";
	      int pos = -1;
	      while(it.hasNext()) {
	        String k1 = (String) it.next();
	
	        if((pos = k1.indexOf("/entityReference")) > -1) {
	          prefix = k1.substring(0, pos);
	          break;
	        }
	      }
	
	      if(pos == -1) {
	        Log.debug(15, "Error in "+className+"!! map doesnt have the entityReference key");
	      } else {
	        map.remove(prefix + "/entityReference");
	        map.remove(prefix + "/valueAttributeReference");
	        map.remove(prefix + "/definitionAttributeReference");
	        OrderedMap importMap = importPane.getPanelData(prefix);
	        map.putAll(importMap);
	
	      }
	  }
  }


  private AbstractDataPackage getADP() {

    AbstractDataPackage dp = mainWizFrame.getAbstractDataPackage();
    return dp;
  }


  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public OrderedMap getPageData() { return resultsMap; }

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

    throw new UnsupportedOperationException(
      "getPageData(String rootXPath) Method Not Implemented");
  }

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

  public boolean setPageData(OrderedMap data, String xPathRoot) 
  {
    boolean success = false;    
    if(data == null)
    {
      Log.debug(30, "The map in CodeDefinition.setPageData and return false");
      return success;
    }    
    Log.debug(35, "In CodeDefinition.setPageData, the xpathRoot is "+xPathRoot+" and map is "+data.toString() );
    String codeColumnStr = (String)data.get(CodeDefnPanel.CODECOLUMNINDEX);
    data.remove(CodeDefnPanel.CODECOLUMNINDEX);
    String definitionColumnStr = (String)data.get(CodeDefnPanel.DEFINITIONCOLUMNINDEX);
    try
    {
      selectedCodeColumnIndex = (new Integer(codeColumnStr)).intValue();
      selectedDefinitionColumnIndex = (new Integer(definitionColumnStr)).intValue();
    }
    catch(Exception e)
    {
      Log.debug(30, "Couldn't get importChocie entity in CodeImportPage.setPageData since "+e.getMessage());
      return success;
    }
    removedAttributeInfo = CodeImportPage.extactRemovedImportAttribute(data);
    if(removedAttributeInfo == null)
    {
      Log.debug(30, "In CodeDefinition.setPageData, morpho couldn't get the removed the import attribute info.");
      return success;
    }
    
    try
    {
      onLoadAction(); 
    }
    catch(Exception e)
    {
      Log.debug(30, "Morpho couldn't click the radion button or select code-definition table or adds an entity into data package "+e.getMessage());
      return success;
    }    
     success = true;
     return success;
   }
  
  /**
   * Gets the attribute name be handle (imported in this page)
   * @return
   */
  public String getHandledImportAttributeName()
  {
	   return this.handledImportAttributeName;
  }
  
  /**
   * Gets the import attribute info which was removed in this page's advanceAction.
   * @return
   */
  public ArrayList getRemovedImportAttributeInfo()
  {
	  return this.removedAttributeInfo;
  }
  
  

   /**
    * Gets the selected code column index (not id) from CodeDefnPanel
    * @return
    */
	public int getSelectedCodeColumnIndex() 
	{
		return selectedCodeColumnIndex;
	}

	 /**
	    * Gets the selected definition column index (not id) from CodeDefnPanel
	    * @return
	    */
	public int getSelectedDefinitionColumnIndex() 
	{
		return selectedDefinitionColumnIndex;
	}
  
  
  /*
   * This is the real method to put referenced id into the attribute which need to be imported.
   * In CodeDefinition.replaceEmptyReference method, the curretnImportMap was modified
   * (added referenced id information), this method will put the map information into attribute.
   */
  public static void updateImportAttributeInNewTable(AbstractDataPackage dataPackage, OrderedMap map) {
   
	//Gets modified map (having the referenced id information)
    //OrderedMap map = dataPackage.getCurrentImportMap();
    //adp = getADP();
    if(dataPackage == null || map == null)
      return;
    String eName = dataPackage.getCurrentImportEntityName();
    String aName = dataPackage.getCurrentImportAttributeName();
    String xPath = dataPackage.getCurrentImportXPath();

    int entityIndex = dataPackage.getEntityIndex(eName);
    int attrIndex = dataPackage.getAttributeIndex(entityIndex, aName);


    String firstKey = (String)map.keySet().iterator().next();
    //if key of themap doesn't start with /attribute, we need to change the key path.
    if(!firstKey.startsWith("/attribute")) {
      OrderedMap newMap = new OrderedMap();
      Iterator it1 = map.keySet().iterator();
      while(it1.hasNext()) {
        String k = (String)it1.next();
        // get index of first '/' after 'attributeList'
        int idx1 = k.indexOf("/attributeList") + new String("/attributeList").length();
        // get the substring following the '/'. this starts with 'attribute'
        String tk = k.substring(idx1 + 1); //
        int idx2 = tk.indexOf("/");
        // remove the existing 'attribute' and add 'attribute' so that we handle cases
        // like 'attribute[2]'
        String newKey = "/attribute" + tk.substring(idx2);
        newMap.put(newKey, (String)map.get(k));
      }
      map = newMap;
      xPath = "/attribute";
    }

    // get the ID of old attribute and set it for the new one
    String oldID = dataPackage.getAttributeID(entityIndex, attrIndex);
    map.put(xPath + "/@id", oldID);

    /*System.out.println("New Keys in CIS page are - ");
    Iterator it = map.keySet().iterator();
    while(it.hasNext()) {
      String kk = (String) it.next();
      System.out.println(kk + " - " + (String)map.get(kk));
    }*/

    Attribute attr = new Attribute(map);

    dataPackage.insertAttribute(entityIndex, attr, attrIndex);
    dataPackage.deleteAttribute(entityIndex, attrIndex + 1);


  }


  /*
   * Add an entity to AbstractDataPacakge
   */
  private void addNewEntityToAbstractDataPackage()
  {
    Node newDOM = mainWizFrame.collectDataFromPages();       
    int entityIndex = mainWizFrame.getEnityIndex();
    Log.debug(32, "The index of the entity which was added to abstract package in CodeDefintion.onLoadAction is "+entityIndex);
    Log.debug(35, "Add/replace entity on CodeDefinition.onLoad method with index "+entityIndex);
    adp.replaceEntity(newDOM, entityIndex);
    mainWizFrame.setDOMToReturn(null);
    //since we added/replace an entity into adp, so next available index should be increase too.
    //entityIndex = entityIndex+1;
    //mainWizFrame.setEntityIndex(entityIndex);
    adp.setLocation("");  // we've changed it and not yet saved
    entityAdded = true;// change the variable to be true, so next time load would NOT modify adp again
  }

  private String getDelimiterString(String field_delimiter) {
    String str = "";
    String temp = field_delimiter.trim();
    if (temp.startsWith("#x")) {
      temp = temp.substring(2);
      if (temp.equals("0A")) str = "\n";
      if (temp.equals("09")) str = "\t";
      if (temp.equals("20")) str = " ";
    }
    else {
      str = temp;
    }
    return str;
  }



}
