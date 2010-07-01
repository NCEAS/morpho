/**
 *  '$RCSfile: CodeDefnPanel.java,v $'
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


/*
This a page used for importing codes from another table. It gives the user
the option of importing it later or identifing the table and columns of an
already present table.
If the user chooses to import it later, the returned OrdereMap contains
null values for the entityCodeList/entityReference. The presence of this
key in the OrderedMap with a null value indicates that we need to import the
data table at the end of the current operation. Thus, whenever an
AttributePage is used anywhere in Morpho, we need to check if some data
table has to be imported after that.
*/


package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractCustomTablePopupHandler;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomTable;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Base64;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CodeDefnPanel extends JPanel implements WizardPageSubPanelAPI {

  private final String title      = /*"Import Codes and Definitions"*/ Language.getInstance().getMessage("ImportCodesAndDefinitions");
  private final String subtitle   = "";

  public short USER_RESPONSE;
  public static final short CANCEL_OPTION  = 20;


  private String[] importChoiceText = {
		  /*"Import the definitions table into Morpho later"*/ Language.getInstance().getMessage("CodeDefnPanel.importChoiceText_1"),
		  /*"The definitions table has already been included in this package"*/ Language.getInstance().getMessage("CodeDefnPanel.importChoiceText_2")
		  };

  private JLabel choiceLabel;
  private JPanel radioPanel;
  private JPanel definitionsPanel;

  private String[] entityNames = null;
  private String[] attrNames = null;

  private Vector rowData;
  private Vector tableNames;
  private Vector colNames;

  private CustomTable table;

  private short selectedImportChoice = 0;

  private static final int IMPORT_LATER = 1;
  private static final int IMPORT_DONE  = 2;
  private static final String ID_XPATH = "attribute/@id";

    // max number of rows to be displayed in the Code definition panel;
    // to display all rows, set it to -1
  private static final String TRUNCATE_STRING = "--truncated--";

  private int entityIdx = -1;
  
  private String currentEntityID = AbstractDataPackage.IMPORTLATER;
  private String codeAttributeID =AbstractDataPackage.IMPORTLATER;
  private String defnAttributeID = AbstractDataPackage.IMPORTLATER;

  private static Node[] attributeArray = null;

  // AbstractDataPackage of the current package
  private AbstractDataPackage adp = null;

  private ItemListener namePickListListener;

  // flag to indicate if the panel is to only allow the user to define codes,
  // without giving the option of importing it later.

  private boolean onlyDefnPanel = false;

  // flag to indicate if the data from the tables need to imported into the CustomTable
  // It is possible to set the table data at runtime using the setTable() interface.
  private boolean createDataTable = true;
  
  private int selectedCodeColumnIndexInTable = -1;
  private int selectedDefColumnIndexInTable = -1;
  
  public static final String SELECTEDENTITYINDEX = "selectedEntityIndex";
  public static final String CODECOLUMNINDEX = "codeColumnIndex";
  public static final String DEFINITIONCOLUMNINDEX = "definitionColumnIndex";

  public CodeDefnPanel(){

    this(false, true);
  }

  public CodeDefnPanel(boolean onlyDefinitionsPanel) {

    this(onlyDefinitionsPanel, true);
  }

  public CodeDefnPanel(boolean onlyDefinitionsPanel, boolean createDataTable) {

    onlyDefnPanel = onlyDefinitionsPanel;
    if(onlyDefnPanel)
         selectedImportChoice = IMPORT_DONE;
    this.createDataTable = createDataTable;
    init();

  }

  private void init() {

    // gets the Abstract Data Package and sets it to the member variable 'adp'
    getADP();

    setLayout(new BorderLayout());

    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

    choiceLabel = WidgetFactory.makeHTMLLabel(/*"Select one of the following"*/ Language.getInstance().getMessage("CodeDefnPanel.choiceLabel"),
    											1,true);
    topPanel.add(choiceLabel);
    topPanel.add(WidgetFactory.makeDefaultSpacer());

    ActionListener listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        if (e.getActionCommand().equals(importChoiceText[0])) {
          selectedImportChoice = IMPORT_LATER;
          definitionsPanel.setVisible(false);
        } else if (e.getActionCommand().equals(importChoiceText[1])) {
          selectedImportChoice = IMPORT_DONE;
          definitionsPanel.setVisible(true);
        }
      }
    };

    radioPanel = WidgetFactory.makeRadioPanel(importChoiceText, -1, listener);

    topPanel.add(radioPanel);

    if(onlyDefnPanel == false)
      add(topPanel, BorderLayout.NORTH);


    definitionsPanel = getDefinitionsPanel();
    add(definitionsPanel, BorderLayout.CENTER);
    if(onlyDefnPanel == false)
      definitionsPanel.setVisible(false);

  }



  private JPanel getDefinitionsPanel() {

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    JLabel headLabel = WidgetFactory.makeHTMLLabel(
    		/*"Select the two columns that define 	the codes and definitions."*/ Language.getInstance().getMessage("CodeDefnPanel.headLabel_1") +" "
    		+/*" The selected columns should be in the same data table."*/ Language.getInstance().getMessage("CodeDefnPanel.headLabel_2"),
    		2 , false);

    panel.add(headLabel, BorderLayout.NORTH);

    if(adp == null)
      getADP();
    if(adp == null) {

      Log.debug(5, "Unable to obtain the AbstractDataPackage in the CodeImportPanel");
      return panel;
    }

    Morpho morpho = Morpho.thisStaticInstance;

    entityNames = getEntityNames();
    tableNames = new Vector();
    colNames = new Vector();
    rowData = new Vector();

    if(this.createDataTable == false)
      return panel;

    for(int i =0; i < entityNames.length; i++) {

      int idx = adp.getEntityIndex(entityNames[i]);
      boolean text_file = false;

      attrNames = getAttributeNames(idx);

      File entityFile = getEntityFile(morpho, adp, idx);
      if(entityFile == null) {
        continue;
      }
      String format = adp.getPhysicalFormat(idx, 0);
      if(format.indexOf("Text") > -1 || format.indexOf("text") > -1 || format.indexOf("Asci") > -1 || format.indexOf("asci") > -1) {
	  text_file = true;
      }
      Vector colsToExtract = new Vector();
      for(int j = 0; j < attrNames.length; j++) {
        colsToExtract.add(new Integer(j));
      }
			String numHeaders = adp.getPhysicalNumberHeaderLines(idx, 0);
      int numHeaderLines = 0;
      try {
        if(numHeaders != null) {
          numHeaderLines = Integer.parseInt(numHeaders);
        }
      } catch(Exception e) {}

      String field_delimiter = adp.getPhysicalFieldDelimiter(idx, 0);
      String delimiter = getDelimiterString(field_delimiter);
      boolean ignoreConsecutiveDelimiters = adp.ignoreConsecutiveDelimiters(idx, 0);
      List data = null;
      if(text_file) {
	  data = getColumnValues(entityFile, colsToExtract, numHeaderLines, delimiter, ignoreConsecutiveDelimiters, WizardSettings.MAX_IMPORTED_ROWS_DISPLAYED_IN_CODE_IMPORT);
      } else {
	  // not a displayable data; hence just create a single empty row (with the necessary columns) to add to the resultset
	  data = new ArrayList();
	  List row1 = new ArrayList();
	  for(int ci = 0; ci < colsToExtract.size(); ci++) row1.add("**nontext**");
	  data.add(row1);
      }

      //      if(data != null && data.size() < 1)
      //  continue;
      //else {
      
      for(int k = 0; k < attrNames.length; k++) {
         tableNames.add(entityNames[i]);
         colNames.add(attrNames[k]);
      }
      
      TaxonImportPanel.addColumnsToRowData(rowData, data);

    }

    Vector headerVector = new Vector();
    headerVector.add(tableNames);
    headerVector.add(colNames);

    table = new CustomTable(headerVector, rowData);
    table.addPopupListener(new CustomPopupHandler());
    panel.add(table, BorderLayout.CENTER);

    return panel;
  }

  private void getADP() {

    adp = UIController.getInstance().getCurrentAbstractDataPackage();
   
  }

  private String[] getEntityNames() {

    if(entityNames != null)
      return entityNames;

    if(adp == null)
      getADP();

    String[] entNames = new String[0];

    if(adp != null) {

      int cnt = adp.getEntityCount();
      entNames = new String[cnt];

      for(int i = 0; i < cnt; i++) {
        entNames[i] = adp.getEntityName(i);
      }

    } else {
      Log.debug(45, "Error - Unable to get the AbstractDataPackage in CodeImportPanel. ");
    }
    return entNames;

  }


  /**
   * Function to retrieve the selected table name from where the enumerated
   * codes are imported. If the user chooses the option of importing the data
   * table later, this function returns null.
   *
   * @return String
   */
  public String getTableName() {
    if(selectedImportChoice == IMPORT_LATER || entityNames == null) {
      return null;
    }
    int[] arr = table.getSelectedColumns();
    if(arr == null || arr.length < 1) {
      return null;
    }
    Vector header = table.getColumnHeaderStrings(arr[0]);
    if(header == null) {
      return null;
    }
    return (String)header.get(0);
  }

  public void setTable(String tableName, List columns, Vector data) {

	if(columns != null)
	{
	    entityNames = new String[1];
	    Log.debug(30, "The table name in CodeDefPanel.setTable is ++++++++++++ "+tableName);
	    entityNames[0] = tableName;
	    attrNames = new String[columns.size()];
	
	    this.tableNames = new Vector();
	    this.colNames = new Vector();
	    this.rowData = data;
	
	    Iterator it = columns.iterator();
	    int cnt = 0;
	    while(it.hasNext()) {
	
	      String col = (String)it.next();
	      tableNames.add(tableName);
	      colNames.add(col);
	      attrNames[cnt++] = col;
	    }
	
	    Vector headerInfo = new Vector();
	    headerInfo.add(tableNames);
	    headerInfo.add(colNames);
	
	    if(table != null)
	      definitionsPanel.remove(table);
	    table = new CustomTable(headerInfo, rowData);
	    table.addPopupListener(new CustomPopupHandler());
	
	    definitionsPanel.add(table, BorderLayout.CENTER);
	    definitionsPanel.validate();
	    definitionsPanel.repaint();
	}
  }



  /*
  *	Function to retrieve the ID of the selected table from where the enumerated
  *	codes are imported.
  */
  /*
  private String getEntityID(int entityIndex) {
    String id = "";
    id = adp.getEntityID(entityIndex);
    Log.debug(45, "Entity ID for entityIndex = " + entityIndex + " is " + id);
    return id;
  }*/

  /**
  *	Function to retrieve the selected entity Index
  *	@return int the entity index of the selected data table
  */

  public int getSelectedEntityIndex() {

    String table = getTableName();
    if(adp == null){
      return -1;
    }
		if(table == null) return -1;
    return adp.getEntityIndex(table);
  }


  private String[] getAttributeNames(int entityIndex) {

    ArrayList names = new ArrayList();
    String attrs[] = new String[0];
    if(adp != null) {
      int num = adp.getAttributeCountForAnEntity(entityIndex);
      attrs = new String[num];
      for(int i = 0; i < num; i++) {
        attrs[i] = adp.getAttributeName(entityIndex, i);
      }
    }

    return attrs;
  }


  /**
  *  The action to be executed when the page is displayed. May be empty
  */

  public void onLoadAction() {}
  
  
  /**
   * Gets the selected code column index in JTable.
   * -1 will be returned, if we can't find it.
   * @return
   */
  public int getSelectedCodeColumnIndexInTable()
  {
    return this.selectedCodeColumnIndexInTable;
  }
  

  
  /**
   * Sets and selects a column as code and definition in the table
   * @param int array. It should has two elements. The first is code, and second is definition
   */
  public void setSelectedCodeDefColumnInTable(int[] index) throws Exception
  {
    if(index == null || index.length != 2)
    {
      throw new Exception("The arrary size of code and definition columns should be two");
    }
    this.selectedCodeColumnIndexInTable = index[0];
    this.selectedDefColumnIndexInTable = index[1];
    table.setExtraColumnHeaderInfo(selectedCodeColumnIndexInTable, "Code");
    table.setExtraColumnHeaderInfo(selectedDefColumnIndexInTable, "Definition");
    table.setSelectedColumns(index);    
  }

  /**
   * Gets the selected definition column index in JTable.
   * -1 will be returned, if we can't find it.
   * @return
   */
  public int getSelectedDefColumnIndexInTable()
  {
    return this.selectedDefColumnIndexInTable;
  }

  /**
   *  checks that the user has filled in required fields - if not, highlights
   *  labels to draw attention to them
   *
   *  @return   boolean true if user data validated OK. false if intervention
   *            required
   */

  public boolean validateUserInput() {

    if(selectedImportChoice != IMPORT_LATER && selectedImportChoice != IMPORT_DONE) {
      WidgetFactory.hiliteComponent(choiceLabel);
      return false;
    }
    WidgetFactory.unhiliteComponent(choiceLabel);

    if(selectedImportChoice == IMPORT_LATER)
      return true;

    int sel[] = table.getSelectedColumns();
    if(sel.length != 2) {

      JOptionPane.showMessageDialog(this,
    		  						/*"Select exactly two columns"*/ Language.getInstance().getMessage("CodeDefnPanel.SelectExactlyTwoColumns"),
    		  						/*"Error"*/ Language.getInstance().getMessage("Error"),
    		  						JOptionPane.ERROR_MESSAGE);
      return false;
    }

    Vector header1 = table.getColumnHeaderStrings(sel[0]);
    Vector header2 = table.getColumnHeaderStrings(sel[1]);

    String table1 = (String)header1.get(0);
    String table2 = (String)header2.get(0);
    if(!(table1.equals(table2))) {

      JOptionPane.showMessageDialog(this, 
    		  						/*"Both columns need to be selected from the same data table"*/ Language.getInstance().getMessage("CodeDefnPanel.ColumnsFromTheSameDataTable"),
    		  						/*"Error"*/ Language.getInstance().getMessage("Error"),
    		  						JOptionPane.ERROR_MESSAGE);
      return false;
    }

    if(header1.size() < 3 || header2.size() < 3) {

      JOptionPane.showMessageDialog(this,
    		  						/*"Select the data type (Code/Definition) for both the columns"*/ Language.getInstance().getMessage("CodeDefnPanel.DataTypeForColumns"),
    		  						/*"Error"*/ Language.getInstance().getMessage("Error"),
    		  						JOptionPane.ERROR_MESSAGE);
      return false;
    }

    String type1 = (String)header1.get(2);
    String type2 = (String)header2.get(2);

    if(type1.equals(type2)) {

      JOptionPane.showMessageDialog(this,
    		  						/*"Select one column for codes and one for definition"*/ Language.getInstance().getMessage("CodeDefnPanel.OneForCodeOneForDefinition"),
    		  						/*"Error"*/ Language.getInstance().getMessage("Error"),
    		  						JOptionPane.ERROR_MESSAGE);
      return false;
    }


    entityIdx = getSelectedEntityIndex();
    if(entityIdx < 0)
      return true;

    currentEntityID = adp.getEntityID(entityIdx);

    if(adp != null) {

      int codeIndex = getCodeColumnIndexInAtractPackage();
      int defnIndex = getDefnColumnIndexInAtractPackage();

      if(currentEntityID.trim().equals("")) {

        currentEntityID = WizardSettings.getUniqueID();
        Log.debug(15, "Entity doesnt have an enity ID - assigning it a new ID of " + currentEntityID);
        adp.setEntityID(entityIdx, currentEntityID);
      }

      if(codeIndex >=0 )
        codeAttributeID = adp.getAttributeID(entityIdx, codeIndex);

      // the attribute has no ID !! This should never happen for data tables
      // created using the new DPW. But if we encounter such a situation, a
      // new ID is assigned and added to the attribute.

      if(codeAttributeID.trim() == "" && codeIndex >= 0) {
        Log.debug(15, "Attribute " +
        adp.getAttributeName(entityIdx,	codeIndex) + "has no ID; assigning one now");

        codeAttributeID = WizardSettings.getUniqueID();
        assignIDToAttribute(codeIndex, codeAttributeID);
      }
      Log.debug(45, "Code AttributeID = " + codeAttributeID);

      if(defnIndex >= 0)
        defnAttributeID = adp.getAttributeID(entityIdx,	defnIndex);

      // the attribute has no ID !! This should never happen for data tables
      // created using the new DPW. But if we encounter such a situation, a
      // new ID is assigned and added to the attribute.
      if(defnAttributeID.trim() == "" && defnIndex >= 0) {
        Log.debug(15, "Attribute " +
        adp.getAttributeName(entityIdx,	defnIndex) + " has no ID; assigning one now");
        defnAttributeID = WizardSettings.getUniqueID();
        assignIDToAttribute(defnIndex, defnAttributeID);
      }
      Log.debug(45, "Defn AttributeID = " + defnAttributeID);

    } else {

      Log.debug(25, "No AbstractDataPackage found! Hence IDs could not be retrieved!");
    }

    return true;
  }

  /* function to assign a new unique ID to an attribute if that attribute doesnt
    have an ID already assigned to it. */

  private void assignIDToAttribute(int attrIndex, String value) {

    if(adp == null)
      Log.debug(15, "Abstract Data Package is null ! Cant assign ID to attribute");
    if(attributeArray == null)
      attributeArray = adp.getAttributeArray(entityIdx);

    if(attributeArray[attrIndex] == null) {
      Log.debug(15, " attribute node itself is null; Cant assign ID to it");
      return;
    } else {
      Log.debug(45, "attribute node =" + attributeArray[attrIndex].getNodeName() + ";"
      + attributeArray[attrIndex].getNodeValue());
    }

    NamedNodeMap map = attributeArray[attrIndex].getAttributes();
    Node oldIdNode = map.getNamedItem("id");

    if(oldIdNode != null) {
      attributeArray[attrIndex].removeChild(oldIdNode);
      oldIdNode.setNodeValue(value);
      attributeArray[attrIndex].appendChild(oldIdNode);
    } else {
      Log.debug(45, "Attribute element has no 'id' attribute. Adding it.");
      ((Element)attributeArray[attrIndex]).setAttribute("id", value);
    }
    adp.setLocation("");
    return;
  }


  /**
  *  gets the Map object that contains all the key/value paired
  *  settings for this particular wizard panel
  *
  *  @return   data the Map object that contains all the
  *            key/value paired settings for this particular wizard page
  */
  public OrderedMap getPanelData() {

    return getPanelData("");
  }

  /*
   * Gets the selected column index for code. -1 will be returned if we couldn't find it
   * @return
   */
  private int getCodeColumnIndexInAtractPackage() {

    if(adp == null) {
      return -1;
    }
    int arr[] = table.getSelectedColumns();
    if(arr.length != 2) {
      return -1;
    }
    Vector header = table.getColumnHeaderStrings(arr[0]);
    String colType = (String)header.get(2);
    if(colType.equalsIgnoreCase("Code")) {
      String colName = (String)header.get(1);
      selectedCodeColumnIndexInTable = arr[0];
      return adp.getAttributeIndex(getSelectedEntityIndex(), colName);
    }

    header = table.getColumnHeaderStrings(arr[1]);
    colType = (String)header.get(2);
    if(colType.equalsIgnoreCase("Code")) {
      String colName = (String)header.get(1);
      selectedCodeColumnIndexInTable = arr[1];
      return adp.getAttributeIndex(getSelectedEntityIndex(), colName);
    }
    return -1;
  }

  /**
   * Gets the selected column index for defintion. -1 will be returned if we couldn't find it
   * @return
   */
  private int getDefnColumnIndexInAtractPackage() {

    if(adp == null)
      return -1;
    int arr[] = table.getSelectedColumns();
    if(arr.length != 2) return -1;
    Vector header = table.getColumnHeaderStrings(arr[0]);
    String colType = (String)header.get(2);
    if(colType.equalsIgnoreCase("Definition")) {
      String colName = (String)header.get(1);
      selectedDefColumnIndexInTable = arr[0];
      return adp.getAttributeIndex(getSelectedEntityIndex(), colName);
    }

    header = table.getColumnHeaderStrings(arr[1]);
    colType = (String)header.get(2);
    if(colType.equalsIgnoreCase("Definition")) {
      String colName = (String)header.get(1);
      selectedDefColumnIndexInTable = arr[1];
      return adp.getAttributeIndex(getSelectedEntityIndex(), colName);

    }

    return -1;
  }

  /**
  *  gets the Map object that contains all the key/value paired
  *  settings for this particular wizard panel, given a prefix xPath
  *
  *	@param xPath the xPath that needs to be prepended to all keys that are
  *								inserted in the map
  *  @return   data the Map object that contains all the
  *            key/value paired settings for this particular wizard page
  */

  public OrderedMap getPanelData(String xPath) {

    OrderedMap map = new OrderedMap();
		
		int eIdx = getSelectedEntityIndex();
    if(adp!=null && currentEntityID.equals(AbstractDataPackage.IMPORTLATER)) {
			if(eIdx >= 0) currentEntityID = adp.getEntityID(eIdx);
    }
    if(adp!=null && codeAttributeID.equals(AbstractDataPackage.IMPORTLATER)) {
      if(eIdx >= 0) codeAttributeID = adp.getAttributeID(eIdx, getCodeColumnIndexInAtractPackage());
    }
    if(adp!=null && defnAttributeID.equals(AbstractDataPackage.IMPORTLATER)) {
      if(eIdx >= 0) defnAttributeID = adp.getAttributeID(eIdx, getDefnColumnIndexInAtractPackage());
    }

    map.put(xPath + "/entityReference", this.currentEntityID);
    map.put(xPath + "/valueAttributeReference", this.codeAttributeID);
    map.put(xPath + "/definitionAttributeReference", this.defnAttributeID);
    return map;
  }


  /**
  *  sets the fields in the wizard panel using the Map object
  *  that contains all the key/value paired
  *
  *  @param   data the Map object that contains all the
  *            key/value paired settings for this particular wizard page
  */
  public void setPanelData(OrderedMap data) {
    boolean b1 = data.containsKey(AttributeSettings.Nominal_xPath + "/enumeratedDomain/entityCodeList/entityReference");

    if(b1) { // check if its Nominal

      setPanelData(AttributeSettings.Nominal_xPath + "/enumeratedDomain/entityCodeList", data);

    } else { // check for Ordinal

      b1 = data.containsKey(AttributeSettings.Ordinal_xPath + "/enumeratedDomain/entityCodeList/entityReference");

      if(b1) {
        setPanelData(AttributeSettings.Ordinal_xPath + "/enumeratedDomain/entityCodeList", data);
      }

    }
    return;
  }

  /**
  *  sets the fields in the wizard panel using the Map object
  *  that contains all the key/value paired and the relative xPath to be used
  *
  *	@param 	xPath	the relative xPath of the keys
  *  @param   data the Map object that contains all the
  *            key/value paired settings for this particular wizard page
  */

  public void setPanelData(String xPath, OrderedMap data) {

    Object o1 = data.get(xPath + "/entityReference");
		data.remove(xPath + "/entityReference");
    Container c = (Container)(radioPanel.getComponent(1));

    if( o1 == null) {
      JRadioButton jrb = (JRadioButton)c.getComponent(0);
      jrb.setSelected(true);
      selectedImportChoice = IMPORT_LATER;
      definitionsPanel.setVisible(false);
      return;

    } else {

      JRadioButton jrb = (JRadioButton)c.getComponent(1);
      jrb.setSelected(true);
      selectedImportChoice = IMPORT_DONE;
      definitionsPanel.setVisible(true);
    }

    currentEntityID = (String)o1;
    codeAttributeID = (String)data.get(xPath + "/valueAttributeReference");
    defnAttributeID = (String)data.get(xPath + "/definitionAttributeReference");
		
		data.remove(xPath + "/valueAttributeReference");
		data.remove(xPath + "/definitionAttributeReference");
		
    int[] selectedCols = new int[2];
    boolean codeSelected = false;
    boolean defnSelected = false;

    int colCount = 0;
    for(int i = 0; i < entityNames.length; i++) {

      int entIdx = adp.getEntityIndex(entityNames[i]);
      String eID = adp.getEntityID(i);
      int attrCnt = adp.getAttributeCountForAnEntity(entIdx);
      if(!eID.equals(currentEntityID)) {
        colCount += attrCnt;
        continue;
      }

      for(int j = 0; j < attrCnt; j++) {

        String ID = adp.getAttributeID(entIdx, j);
        if(ID.equals(codeAttributeID)) {
          selectedCols[0] = colCount;
          table.setExtraColumnHeaderInfo(colCount, "Code");
          codeSelected = true;

        } else if(ID.equals(defnAttributeID)) {
          selectedCols[1] = colCount;
          table.setExtraColumnHeaderInfo(colCount, "Definition");
          defnSelected = true;

        }
        colCount++;

        if(codeSelected && defnSelected) {
          table.setSelectedColumns(selectedCols);
          break;
        }
      }

      break;
    }

    return;
  }



  private JLabel getLabel(String text) {

    if (text==null) text="";
    JLabel label = new JLabel(text);

    label.setAlignmentX(1.0f);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    label.setBorder(BorderFactory.createMatteBorder(1,10,1,3, (Color)null));

    return label;
  }

  /**
  *	Function to retrieve the imported data from the columns selected by the
  *	user. The selected table is read and the necessary two columns are
  *	extracted and returned as a List of rows. Each row is a list of two elements
  *
  * @return list the list of rows containing only the selected two columns
  */


  public List getColumnData() {

    List result = new ArrayList();
    if(rowData == null) {
      return null;
    }

    int cols[] = table.getSelectedColumns();
    if(cols.length != 2) return null;
    int codeIdx = cols[0];
    int defnIdx = cols[1];

    Vector header = table.getColumnHeaderStrings(cols[0]);
    if(header.size() < 3) return null;

    String type = (String) header.get(2);
    if(type.equalsIgnoreCase("Definition")) {
      codeIdx = cols[1];
      defnIdx = cols[0];
    }

    Iterator it = rowData.iterator();
    while(it.hasNext()) {

      List t = new ArrayList();
      Vector row = (Vector)it.next();
      t.add(row.get(codeIdx));
      t.add(row.get(defnIdx));
      result.add(t);
    }

    return result;
  }


  public static File getEntityFile(Morpho morpho, AbstractDataPackage adp, int entityIndex) {
		
		if(morpho == null) return null;
    File entityFile = null;
    String inline = adp.getDistributionInlineData(entityIndex, 0,0);

    if (inline.length()>0) {  // there is inline data

      String encMethod = adp.getEncodingMethod(entityIndex, 0);
      if ((encMethod.indexOf("Base64")>-1)||(encMethod.indexOf("base64")>-1)||
      (encMethod.indexOf("Base 64")>-1)||(encMethod.indexOf("base 64")>-1)) {
        // is Base64
        byte[] decodedData = Base64.decode(inline);
        ByteArrayInputStream bais = new ByteArrayInputStream(decodedData);
        InputStreamReader isr = new InputStreamReader(bais);
        FileSystemDataStore fds3 = new FileSystemDataStore(morpho);
        entityFile = fds3.saveTempDataFile(adp.getAccessionNumber(), isr);
      }
      else {
        // is assumed to be text
        FileSystemDataStore fds2 = new FileSystemDataStore(morpho);
        StringReader sr2 = new StringReader(inline);
        entityFile = fds2.saveTempDataFile(adp.getAccessionNumber(), sr2);
      }
    } else if (adp.getDistributionUrl(entityIndex, 0,0).length()>0) {

      String urlinfo = adp.getDistributionUrl(entityIndex, 0,0);
      // assumed that urlinfo is of the form 'protocol://systemname/localid/other'
      // protocol is probably 'ecogrid'; system name is 'knb'
      // we just want the local id here
      int indx2 = urlinfo.lastIndexOf("/");
      if(indx2 == -1) {
        Log.debug(15, "Distribution URL is not in the right format! So data couldnt be retrieved");
        return null;
      }
      urlinfo = urlinfo.substring(indx2 +1);
      if (urlinfo.length()==0) {
        Log.debug(15, "Distribution URL is not in the right format! So data couldnt be retrieved");
        return null;
      }
      // we now have the id
      try{
        String loc = adp.getLocation();
        if ((loc.equals(adp.LOCAL))||(loc.equals(adp.BOTH))) {
          FileSystemDataStore fds = new FileSystemDataStore(morpho);
          entityFile = fds.openFile(urlinfo);
        }
        else if (loc.equals(adp.METACAT)) {
          MetacatDataStore mds = new MetacatDataStore(morpho);
          entityFile = mds.openFile(urlinfo);
        }
        else if (loc.equals("")) {  // just created the package; not yet saved!!!
          FileSystemDataStore fds = new FileSystemDataStore(morpho);
          try
          {
            entityFile = fds.getDataFileFromAllSources(urlinfo);
          }
          catch(Exception eee)
          {
            Log.debug(15,"Exception opening datafile after trying all sources!");
            return null;
          }
          
          /*try{
            // first try looking in the profile temp dir
            ConfigXML profile = morpho.getProfile();
            String separator = profile.get("separator", 0);
            separator = separator.trim();
            String temp = new String();
            temp = urlinfo.substring(0, urlinfo.indexOf(separator));
            temp += "/" + urlinfo.substring(urlinfo.indexOf(separator) + 1, urlinfo.length());
            entityFile = fds.openTempFile(temp);
          }
          catch (Exception q1) {
            
            // oops - now try locally
            try{
              
              entityFile = fds.openFile(urlinfo);
            }
            catch (Exception q2) {
              try
              {
                entityFile = fds.openIncompelteFile(urlinfo);
              }
              catch(Exception q)
              {
                // now try metacat
                try{
                  MetacatDataStore mds = new MetacatDataStore(morpho);
                  entityFile = mds.openFile(urlinfo);
                }
                catch (Exception q3) {
                  // give up!
                  Log.debug(15,"Exception opening datafile after trying all sources!");
                  return null;
                }
              }
              
            
            }
          }*/
        }
      }
      catch (Exception q) {
        Log.debug(15,"Exception opening file!");
        q.printStackTrace();
      }
    }
    else if (adp.getDistributionArray(entityIndex, 0)==null) {
      // case where there is no distribution data in the package

      Log.debug(10, "The selected entity has NO distribution information!");
      return null;
    }

    if(entityFile == null) {
      Log.debug(15, "Unable to get the selected entity's data file!");
      return null;
    }

    return entityFile;
  }

  public static List getColumnValues(File file, Vector colIndices, int numHeaderLines, String delimiter, boolean ignoreConsequtiveDelimiters, int maxLinesNeeded) {

    List result = new ArrayList();
    String line;
    
    String token, oldToken = "";
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(file));
      int linecnt = 0;
      while( (line = br.readLine()) != null) {
        linecnt++;
        if(linecnt <= numHeaderLines)
          continue;
        if(line.trim().equals(""))
          continue;
        List row = new ArrayList();

        if(maxLinesNeeded != -1 && result.size() >= maxLinesNeeded) {
          int space = TRUNCATE_STRING.indexOf(" ");
	  for(int ci = 0; ci < colIndices.size(); ci++)
	      row.add(TRUNCATE_STRING);
          result.add(row);
          break;
        }

        if (ignoreConsequtiveDelimiters) {
          StringTokenizer st = new StringTokenizer(line, delimiter, false);
          int cnt = -1;
          while( st.hasMoreTokens() ) {
            token = st.nextToken().trim();
            cnt++;
            int idx = -1;
            if((idx = colIndices.indexOf(new Integer(cnt))) >  -1) {
                row.add(token);
                int lastIdx = colIndices.lastIndexOf(new Integer(cnt));
                while(idx < lastIdx) {
                  idx = colIndices.indexOf(new Integer(cnt), idx +1);
                  if(idx > -1)
                    row.add(token);
                }
            }

            if(idx == colIndices.size() -1) break;
          } // end of while
          result.add(row);
          continue;
        }
        else { //do not ignore consecutive delimiters
          int cnt = -1;
          StringTokenizer st = new StringTokenizer(line, delimiter, true);
          while( st.hasMoreTokens() ) {
            token = st.nextToken().trim();
            if (! (delimiter.indexOf(token) > -1) ) {
              cnt++;
              int idx = -1;
              if((idx = colIndices.indexOf(new Integer(cnt))) >  -1) {
								row.add(token);
                int lastIdx = colIndices.lastIndexOf(new Integer(cnt));
                while(idx < lastIdx) {
                  idx = colIndices.indexOf(new Integer(cnt), idx +1);
                  if(idx > -1) {
                    row.add(token);
									}
                }
              }
              if(idx == colIndices.size() -1) break;
            }
            else {
              if ((delimiter.indexOf(oldToken) > -1) && (delimiter.indexOf(token) > -1)) {
                cnt++;
                int idx = -1;
                if((idx = colIndices.indexOf(new Integer(cnt))) >  -1) {
									row.add("");
                  int lastIdx = colIndices.lastIndexOf(new Integer(cnt));
                  while(idx < lastIdx) {
                    idx = colIndices.indexOf(new Integer(cnt), idx +1);
                    if(idx > -1) {
											row.add("");
										}
                  }
                }
                if(idx == colIndices.size() -1) break;
              }
            }
            oldToken = token;
          }
					int idx1 = colIndices.lastIndexOf(new Integer(cnt));
					for(int rem = idx1 + 1; rem < colIndices.size(); rem++) {
						row.add("");
					}
        } // end of else

        result.add(row);
      } // end of while

    } // end of try bolck
    catch(Exception e) {
      Log.debug(15, "Exception in reading the data File: " + e);

    }

    if(result.size() == 0) {
      // add an empty row
      List row = new ArrayList();
      for(int i = 0; i < colIndices.size(); i++)
        row.add("");
      result.add(row);
    }
    return result;

  }// end of function getcolumnValues



  public static List getOneColumnValue(File file, int colIndex, int numHeaderLines, String delimiter, int maxLinesNeeded) {

    List result = new ArrayList();
    String line;
    boolean ignoreConsequtiveDelimiters = false;
    String token, oldToken = "";
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(file));
      int linecnt = 0;
      while( (line = br.readLine()) != null) {
        linecnt++;
        if(linecnt <= numHeaderLines)
          continue;
        if(line.trim().equals(""))
          continue;

        if(maxLinesNeeded != -1 && result.size() >= maxLinesNeeded) {
          result.add(TRUNCATE_STRING);
          break;
        }

        if (ignoreConsequtiveDelimiters) {
          StringTokenizer st = new StringTokenizer(line, delimiter, false);
          int cnt = -1;
          while( st.hasMoreTokens() ) {
            token = st.nextToken().trim();
            cnt++;
            if(cnt == colIndex) {
                result.add(token);
                break;
            }
          } // end of while

          continue;
        }
        else { // not consecutive delimiters
          int cnt = -1;
          StringTokenizer st = new StringTokenizer(line, delimiter, true);
          while( st.hasMoreTokens() ) {
            token = st.nextToken().trim();
            if (! (delimiter.indexOf(token) > -1) ) {
              cnt++;

              if(cnt == colIndex) {
                result.add(token);
                break;
              }
            }
            else {
              if ((delimiter.indexOf(oldToken) > -1) && (delimiter.indexOf(token) > -1)) {
                cnt++;

                if(cnt == colIndex) {
                  result.add("");
                  break;
                }
              }
            }
            oldToken = token;
          }
        } // end of else


      } // end of while

    } // end of try bolck
    catch(Exception e) {
      Log.debug(15, "Exception in reading the data File: " + e);

    }
    return result;

  }// end of function getcolumnValues

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


/** class to handle the CustomTable Popup events that are generated when a user clicks
  *	on a table header. It extends AbstractCustomTablePopupHandler that implements the
  *	CustomTablePopupListener interface.
  *
  */


class CustomPopupHandler extends AbstractCustomTablePopupHandler {

  private Vector taxonData = null;
  private String displayString = null;
  private ButtonGroup grp;
  private String[] data = new String[] { /*"Code"*/ Language.getInstance().getMessage("Code"),
		  								 /*"Definition"*/ Language.getInstance().getMessage("Definition")
		  								};
  private JRadioButton jrb1, jrb2, dummyButton;

  CustomPopupHandler() {

    super();
    setModal(true);
    init();

  }

  private void init() {

    JPanel panel = new JPanel(new BorderLayout());
    JLabel headLabel = WidgetFactory.makeHTMLLabel(
    												/*"Is this a Code or Definition?"*/ Language.getInstance().getMessage("CodeOrDefinition"),
    												2, false);

    JPanel middlePanel = new JPanel();
    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    grp = new ButtonGroup();
    jrb1 = new JRadioButton(data[0]);
    jrb1.setActionCommand(data[0]);
    jrb2 = new JRadioButton(data[1]);
    jrb2.setActionCommand(data[1]);
    dummyButton = new JRadioButton("");
    ActionListener listener = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        displayString = ((JRadioButton)ae.getSource()).getActionCommand();
        CustomPopupHandler.this.setVisible(false);
      }
    };
    jrb1.addActionListener(listener);
    jrb2.addActionListener(listener);
    grp.add(jrb1);
    grp.add(jrb2);
    grp.add(dummyButton);

    middlePanel.add(Box.createGlue());
    middlePanel.add(jrb1);
    middlePanel.add(Box.createGlue());
    middlePanel.add(jrb2);
    middlePanel.add(Box.createGlue());
    middlePanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, WizardSettings.TOP_PANEL_BG_COLOR));

    panel.add(headLabel, BorderLayout.NORTH);
    panel.add(middlePanel, BorderLayout.CENTER);

    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(panel, BorderLayout.CENTER);
    this.setSize(125,150);

  }

  /** Function to retrieve the string to be displayed in the header of the CustomTable.
  *
  * @return String the string to be displayed in the header of the table. returns null
  *									if no value is present
  */
  public String getDisplayString() {

    return displayString;
  }

  /** Function to retrieve the dialog that is to be displayed when the user clicks
  * 	on the header of the CustomTable. The dialog is responsible of disposing itself
  *		after the input is received.
  *
  * @return JDialog a dialog that is popped up when a user clicks on the table header
  */

  public JDialog getPopupDialog() {

    displayString = null;
    dummyButton.setSelected(true);
    return this;
  }

}
