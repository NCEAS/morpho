/**
 *  '$RCSfile: DataViewer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-10-25 01:02:17 $'
 * '$Revision: 1.75 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import java.awt.*;
import javax.swing.*;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.event.*;
import javax.swing.table.*;
import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Date;
import java.util.Enumeration;
import java.awt.event.*;
import java.util.Stack;


//import org.apache.xalan.xpath.xml.FormatterToXML;
//import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.EditingCompleteListener;
import edu.ucsb.nceas.morpho.framework.EditorInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.StoreStateChangeEvent;


/*
 * The DataViewer class is a panel that displays a text-based table
 * (or an image) based on metadata descriptions for an Entity and the
 * associated Phyical and Attribute modules of eml.
 * Currently, eml2-beta 1.6 is assumed. (some features are 'hard-coded'
 * in this class and will need to be updated for future versions.)
 * 
 * A PersistentTableModel is used so that very large tables can be
 * displayed (millions of rows). Data editing is also enabled with
 * the ability to add rows and columns. Columns can also be sorted.
 * Changees to the table are kept in a persisten vector until
 * an Update is made, when a new dataPackage is created which includes
 * memory updates and new metadata about added rows, columns, etc.
 *
 * Also has ability to Cut/Copy/Paste selections from the table.
 */
public class DataViewer extends javax.swing.JPanel 
            implements EditingCompleteListener, StoreStateChangeEvent
{
  
    
  /**popup menu for right clicks*/
  private JPopupMenu popup;
  /**menu items for the popup menu*/
  private JMenuItem addDocumentation = null;
  private GUIAction addDocumentationAction = null;
  private JMenuItem createNewDatatable = null;
  private GUIAction createNewDatatableAction = null;
  private JMenuItem sortBySelectedColumn = null;
  private GUIAction sortAction = null;
  private JMenuItem insertRowAfter = null;
  private GUIAction insertRowAfterAction = null;
  private JMenuItem insertRowBefore = null;
  private GUIAction insertRowBeforeAction = null;
  private JMenuItem deleteRow = null;
  private GUIAction deleteRowAction = null;
  private JMenuItem insertColumnBefore = null;
  private GUIAction insertColumnBeforeAction = null;
  private JMenuItem insertColumnAfter = null;
  private GUIAction insertColumnAfterAction = null;
  private JMenuItem deleteColumn = null;
  private GUIAction deleteColumnAction = null;
  private JMenuItem editColumnMetadata = null;
  // new JMenuItem("Edit Column Metadata");
  private GUIAction editColumnMetadataAction = null;

  // The following instances of JMenu are apparently needed to make a
  // menus that appears in both the menu bar and in a popup menu
  // (JComponents all have a single 'parent'. This means that the components
  // cannot be reused (because they have only one parent container)
  // One thus needs to duplicate the menu items for the Menu and Popup
  private JMenuItem addDocumentation1 = null;
  private JMenuItem createNewDatatable1 = null;
  private JMenuItem sortBySelectedColumn1 = new JMenuItem("Sort by Selected Column");
  private JMenuItem insertRowAfter1 = new JMenuItem("Insert Row After Selected Row");
  private JMenuItem insertRowBefore1 = new JMenuItem("Insert Row Before Selected Row");
  private JMenuItem deleteRow1 = new JMenuItem("Delete Selected Row");
  private JMenuItem insertColumnBefore1 = new JMenuItem("Insert Column Before Selected Column");
  private JMenuItem insertColumnAfter1 = new JMenuItem("Insert Column After Selected Column");
  private JMenuItem deleteColumn1 = new JMenuItem("Delete Selected Column");
  private JMenuItem editColumnMetadata1 = new JMenuItem("Edit Column Metadata");


  /**
   *   file containing the data
   */
   File dataFile = null;

  /**
   *   file containing the entity metadata
   */
   File entityFile = null;
     
  /**
   *   file containing the attribute metadata
   */
   File attributeFile = null;
   
  /**
   *   file containing the physical metadata
   */
   File physicalFile = null;
  
  /**
   *  entity file Id
   */
   String entityFileId = null;
  
  /**
   * data format
   */
   String format = "";
   
  /**
   *  field delimiter (hex string)
   */
   String field_delimiter = "";
     
  /**
   * number of records
   */
   int num_records;
   
  /**
   * delimiter
   */
   String delimiter_string = "";
   
  /**
   * numHeaderLines
   */
   String numHeaderLines = "0";

  /**
   * num_header_lines
   */
   int num_header_lines = 0;
   
  /**
   * number of columns
   */
   int num_columns;
    
  /**
   * Vector of column lablels
   */
  Vector column_labels;
     
	/**
	 * number of parsed lines in file
	 */
	int nlines; 
	
	/**
	 *  max nlines
	 */
	int nlines_max = 100000;

	/**
	 * array of line strings
	 */
	String[] lines; 

  /**
	 * vector containing column Title strings
	 */

	/**
	 * vector of vectors with table data
	 */
	Vector vec;
	
	/**
	 * The DataPackage that contains the data
	 */
	 DataPackage dp;

 	/**
	 * value of entityName element
	 */
  String entityName = "";
   
 	/**
	 * value of entityDescription element
	 */
  String entityDescription = "";
  
  // Vector to store stateChange event
  private Vector storedStateChangeEventlist = new Vector();
  
  TableCutAction tcuta;
  TableCopyAction tca;
  TablePasteAction tpa;
  
  boolean missing_metadata_flag = false;

    // assorted gui components
	JPanel DataViewerPanel = new javax.swing.JPanel();
	JPanel TablePanel = new javax.swing.JPanel();
	JScrollPane DataScrollPanel = new javax.swing.JScrollPane();
	JPanel ControlPanel = new javax.swing.JPanel();
  JPanel HeaderPanel = new javax.swing.JPanel();
  JLabel headerLabel;
	JPanel ButtonControlPanel = new javax.swing.JPanel();
	JLabel DataIDLabel = new javax.swing.JLabel();
	JButton CancelButton = new javax.swing.JButton();
	JButton UpdateButton = new javax.swing.JButton();
  ColumnMetadataEditPanel cmep;
    
  JPanel controlPanel;
  JButton controlOK;
  JButton controlCancel;
  JDialog columnDialog;

  /*
   * the PersistentVector used to store rows in the data table
   */
  PersistentVector pv;

  /*
   * the PersistentTableModel used for the data table
   */
  PersistentTableModel ptm;

  /*
   * the data table
   */
  JTable table;
    
  int sortdirection = 1;
  boolean columnAddFlag = true;
    
  Document attributeDoc;
  
  Morpho morpho;
  ConfigXML config;
  String datadir;
  String separator;
  String cachedir;
  String tempdir;
  String dataString = "";
  String dataID = "";
    
    
  DataViewer thisRef;
  
  // Display data view or not
  private boolean showDataView = true;

  // flag for text file
  boolean text_flag = false;
	/*
   * No argument contstructor that builds basic gui
   */
  public DataViewer()
	{
		setLayout(new BorderLayout(0,0));
//    setSize(755,483);
		setVisible(false);
		DataViewerPanel.setLayout(new BorderLayout(0,0));
		//add(DataViewerPanel);
		TablePanel.setLayout(new BorderLayout(0,0));
		DataViewerPanel.add(BorderLayout.CENTER, TablePanel);
		TablePanel.add(BorderLayout.CENTER, DataScrollPanel);
		ControlPanel.setLayout(new BorderLayout(0,0));
		DataViewerPanel.add(BorderLayout.SOUTH, ControlPanel);
		ButtonControlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
		ControlPanel.add(BorderLayout.CENTER, ButtonControlPanel);
		DataIDLabel.setText("Data ID: ");
		DataIDLabel.setNextFocusableComponent(CancelButton);
		ButtonControlPanel.add(DataIDLabel);
		CancelButton.setText("Cancel");
		CancelButton.setActionCommand("Cancel");
    String canceltext = "Removes all changes since last Update";
    CancelButton.setToolTipText(canceltext);
		ButtonControlPanel.add(CancelButton);
		UpdateButton.setText("Update");
		UpdateButton.setActionCommand("Update");
    String updatetext = "Saves all data changes to new version of the Data Package";
    UpdateButton.setToolTipText(updatetext);
		ButtonControlPanel.add(UpdateButton);
    
    headerLabel = new JLabel("DataTitle");
    headerLabel.setForeground(Color.white);
    HeaderPanel.setBackground(Color.gray);
    HeaderPanel.add(headerLabel);
		DataViewerPanel.add(BorderLayout.NORTH, HeaderPanel);
    
	  thisRef = this;
	
// REGISTER_LISTENERS for Cancel and Update buttons that
// appear at bottom of DataViewer table. These buttons
// 'Cancel' or 'Update' changes made in the data table  
		SymAction lSymAction = new SymAction();
		CancelButton.addActionListener(lSymAction);
		UpdateButton.addActionListener(lSymAction);
    
	
//Build the popup menu for the right click functionality
    popup = new JPopupMenu();
    // Create a add documentation menu item
    addDocumentationAction = new GUIAction("Add Documentation...", null, 
                                          new AddDocumentationCommand());
    addDocumentation = new JMenuItem(addDocumentationAction);
    popup.add(addDocumentation);
    createNewDatatableAction = new GUIAction("Create New Datatable...", null,
                                                new ImportDataCommand());
    createNewDatatable = new JMenuItem(createNewDatatableAction);
    popup.add(createNewDatatable);
    popup.add(new JSeparator());
    
    sortAction = new GUIAction("Sort by Selected Column", null, 
                                                new SortDataTableCommand());
    sortBySelectedColumn = new JMenuItem(sortAction);
    popup.add(sortBySelectedColumn);
    popup.add(new JSeparator());
    
    insertRowAfterAction = new GUIAction("Insert Row After Selection", null,
                                  new InsertRowCommand(InsertRowCommand.AFTER));
    insertRowAfter = new JMenuItem(insertRowAfterAction);
    popup.add(insertRowAfter);
    insertRowBeforeAction = new GUIAction("Insert Row Before Selection", null,
                                  new InsertRowCommand(InsertRowCommand.BEFORE));
    insertRowBefore = new JMenuItem(insertRowBeforeAction);
    popup.add(insertRowBefore);
    deleteRowAction = new GUIAction("Delete Selected Row", null,
                                    new DeleteRowCommand());
    deleteRow = new JMenuItem(deleteRowAction);
    popup.add(deleteRow);
    popup.add(new JSeparator());
    
    insertColumnAfterAction = new GUIAction("Insert Column After Selection", 
                      null, new InsertColumnCommand(InsertColumnCommand.AFTER));
    insertColumnAfter = new JMenuItem(insertColumnAfterAction);
    popup.add(insertColumnAfter);
    insertColumnBeforeAction = new GUIAction("Insert Column Before Selection", 
                     null, new InsertColumnCommand(InsertColumnCommand.BEFORE));
    insertColumnBefore = new JMenuItem(insertColumnBeforeAction);
    popup.add(insertColumnBefore);
    deleteColumnAction = new GUIAction("Delete Selected Column", null,
                                           new DeleteColumnCommand());
    deleteColumn = new JMenuItem(deleteColumnAction);
    popup.add(deleteColumn);
    popup.add(new JSeparator());
    
    editColumnMetadataAction = new GUIAction("Edit Column Metadata", null,
                               new EditColumnMetaDataCommand());
    editColumnMetadata = new JMenuItem(editColumnMetadataAction);
    popup.add(editColumnMetadata);
  
    //updateDataMenu();
	}

    /*
     * contructor with Morpho and Window title info
     */
    public DataViewer(Morpho morpho, String sTitle)
    {
        this();
        this.morpho = morpho;
        config = morpho.getConfiguration();
        ConfigXML profile = morpho.getProfile();
        String profileDirName = config.getConfigDirectory() + 
                                File.separator +
                                config.get("profile_directory", 0) +
                                File.separator + 
                                profile.get("profilename", 0);
        datadir = profileDirName + File.separator + profile.get("datadir", 0);
        tempdir = profileDirName + File.separator + profile.get("tempdir", 0);
        cachedir = profileDirName + File.separator + profile.get("cachedir", 0);
        separator = profile.get("separator", 0);
        separator = separator.trim();
        
    }

    /*
     * contructor which includes data to be display as a String
     */
    public DataViewer(String sTitle, String dataID, String dataString)
    {
        this();
        this.dataID = dataID;
        this.dataString = dataString;
        
    }
    
    /*
     * contructor which includes data to be display as a File
     */
    public DataViewer(Morpho morpho, String sTitle, File dataFile)
    {
        this();
		    this.morpho = morpho;
        config = morpho.getConfiguration();
        ConfigXML profile = morpho.getProfile();
        String profileDirName = config.getConfigDirectory() + File.separator +
                            config.get("profile_directory", 0) + 
                            File.separator +
                            profile.get("profilename", 0);
        datadir = profileDirName + File.separator + profile.get("datadir", 0);
        tempdir = profileDirName + File.separator + profile.get("tempdir", 0);
        cachedir = profileDirName + File.separator + profile.get("cachedir", 0);
        separator = profile.get("separator", 0);
        separator = separator.trim();
        this.dataFile = dataFile;
    }
    
    public PersistentVector getPV() {
      return pv;  
    }
    
    /**
     * Method to get the show data view
     */
    public boolean getShowDataView()
    {
      return showDataView;
    }//getShowDataView
    
    /**
     * Method to get data table
     */
    public JTable getDataTable()
    {
      return table;
    }
    
    /**
     * Method to get column meta data edit panel
     */
    public ColumnMetadataEditPanel getColumnMetadataEditPanel()
    {
      return cmep;
    }
    
    /**
     * Method to get the attribute documentation
     */ 
    public Document getAttributeDoc()
    {
      return attributeDoc;
    }
    
    /**
     * Method to get the column_lables
     */
    public Vector getColumnLabels()
    {
      return column_labels;
    }

    /**
     * Method to set the column_lables
     */
    public void setColumnLabels(Vector collabels)
    {
      column_labels = collabels;
    }
    
    /**
     * Method to get field_delimiter string
     */
    public String getFieldDelimiter()
    {
      return delimiter_string;
    }
    
    /**
     * Method to get table panel
     */
    public JPanel getTablePanel()
    {
      return TablePanel;
    }
    
    /**
     * Method to get persistent talbe model
     */
    public PersistentTableModel getPersistentTableModel()
    {
      return ptm;
    }
    
    /**
     * Method to get morpho
     */
    public Morpho getMorpho()
    {
      return morpho;
    }
    
    /**
     * Method to get EntityFileid
     */ 
    public String getEntityFileId()
    {
      return entityFileId;
    }
    
  
    /**
     * Method to get sort direction
     */
    public int getSortDirection()
    {
      return sortdirection;
    }
    
    /**
     * Method to set sort direction
     * @param direction the direction of sorting
     */
    public void setSortDirection(int direction)
    {
      sortdirection = direction;
    }
   
   /**
    * Method to get text flag
    */
   public boolean getTextFlag()
   {
     return text_flag;
   }
    
    
    /**
     * Initialization code which collects information about the data
     * from various metadata modules associated with the entity to
     * be displayed
     */
    public void init() {
      missing_metadata_flag = false;
      if (physicalFile==null) {
          Log.debug(15, "Physical information about the data is missing!");
          missing_metadata_flag = true;
      } else {
        // get format, recordDelimiter, field delimiter
        // in general, get all info need to read a record
        
        Vector formatPath = new Vector();
        formatPath.addElement("eml-physical/format");
        NodeList formatList = PackageUtil.getPathContent(physicalFile, 
                                                     formatPath, 
                                                     morpho);  
        if(formatList != null && formatList.getLength() != 0)
        {
          String s = formatList.item(0).getFirstChild().getNodeValue();
          this.format = s;
          Log.debug(20, "format: "+format);
        }
        
        Vector fieldDelimiterPath = new Vector();
        fieldDelimiterPath.addElement("eml-physical/fieldDelimiter");
        NodeList fieldDelimiterList = PackageUtil.getPathContent(physicalFile, 
                                                     fieldDelimiterPath, 
                                                     morpho);  
        if(fieldDelimiterList != null && fieldDelimiterList.getLength() != 0)
        {
          String s = fieldDelimiterList.item(0).getFirstChild().getNodeValue();
          this.field_delimiter = s;
        }
        // Set delimiter_String
        this.delimiter_string = getDelimiterString();
        
        Vector numHeaderLinesPath = new Vector();
        numHeaderLinesPath.addElement("eml-physical/numHeaderLines");
        NodeList numHeaderLinesList = PackageUtil.getPathContent(physicalFile, 
                                                     numHeaderLinesPath, 
                                                     morpho); 
        if(numHeaderLinesList != null && numHeaderLinesList.getLength() != 0) 
        {
          String s = numHeaderLinesList.item(0).getFirstChild().getNodeValue();
          this.numHeaderLines = s;
        }
                                                     
      }
      if (entityFile==null) {
          Log.debug(15, "Entity information about the data is missing!");
          missing_metadata_flag = true;
      } else {
        // get number of records, etc
        Vector numRecordsPath = new Vector();
        numRecordsPath.addElement("table-entity/numberOfRecords");
        NodeList numRecordsList = PackageUtil.getPathContent(entityFile, 
                                                     numRecordsPath, 
                                                     morpho);  
        if(numRecordsList != null && numRecordsList.getLength() != 0)
        {
          String s = numRecordsList.item(0).getFirstChild().getNodeValue();
          if ((s!=null)&&(s.length()>0))  {
            try {
              num_records = (new Integer(s.trim())).intValue();
            }
            catch(Exception w) {}
          }
        }
        
        Vector entityNamesPath = new Vector();
        entityNamesPath.addElement("table-entity/entityName");
        NodeList entityNamesList = PackageUtil.getPathContent(entityFile, 
                                                     entityNamesPath, 
                                                     morpho);  
        if(entityNamesList != null && entityNamesList.getLength() != 0)
        {
          String s = entityNamesList.item(0).getFirstChild().getNodeValue();
          if ((s!=null)&&(s.length()>0))  {
            try {
              entityName = s.trim();
            }
            catch(Exception w) {}
          }
        }
 
        Vector entityDescriptionPath = new Vector();
        entityDescriptionPath.addElement("table-entity/entityDescription");
        NodeList entityDescriptionList = PackageUtil.getPathContent(entityFile, 
                                                     entityDescriptionPath, 
                                                     morpho);  
        if(entityDescriptionList != null && entityDescriptionList.getLength() != 0)
        {
          String s = entityDescriptionList.item(0).getFirstChild().getNodeValue();
          if ((s!=null)&&(s.length()>0))  {
            try {
              entityDescription = s.trim();
            }
            catch(Exception w) {}
          }
        }
        if (entityName.length()>0) {
          headerLabel.setText(entityName);
        } 
        if (entityDescription.length()>0) {
          headerLabel.setText(entityDescription);
        } 

      }
      if (attributeFile==null) {
          Log.debug(15, "Attribute information about the data is missing!");
          missing_metadata_flag = true;
      } else {
        // build a DOM represntation of the attribute file
        try{
          attributeDoc = PackageUtil.getDoc(attributeFile, morpho);
        }
        catch (Exception qq) {
          Log.debug(20,"Error building attribute DOM !");
        }
        // get attribute labels and build column headers
        Vector attributeNamesPath = new Vector();
        attributeNamesPath.addElement("eml-attribute/attribute/attributeName");
           // use names rather than Label because name is required!
        NodeList attributeNamesList = PackageUtil.getPathContent(attributeFile, 
                                                     attributeNamesPath, 
                                                     morpho);  
       if(attributeNamesList != null && attributeNamesList.getLength() != 0)
        {
          column_labels = new Vector(); 
          for (int i=0;i<attributeNamesList.getLength();i++) {
            String unitString = "";
            String dataTypeString = "";
            String temp = attributeNamesList.item(i).getFirstChild().getNodeValue();
            // attribute Name is a required node; we want the associated
            // unit and dataType node values, which are NOT required
            Node nd = attributeNamesList.item(i).getNextSibling();
            while (nd!=null) {
              if (nd.getNodeName().equals("dataType")) {
                dataTypeString = nd.getFirstChild().getNodeValue();
              }
              if (nd.getNodeName().equals("unit")) {
                unitString = nd.getFirstChild().getNodeValue();                
              }
              nd = nd.getNextSibling();  
            }
            temp = "<html><font face=\"Courier\"><center><small>"+dataTypeString+"<br>"+unitString
                                                  +"<br></small><b>"
                                                  +temp+"</b></font></center></html>";
            column_labels.addElement(temp);
          }
        }
      }
      // now examine format info and see if we want to simply display a text
      // file, create a table, or display an image
      if (missing_metadata_flag) {
        // try displaying as text since don't know what else to do 
        
        // add text display here!!!
        Log.debug(30, "attempting to display as text");
        buildTextDisplay();
      }
      else { 
        if (format.indexOf("text")>-1){
          text_flag=true;
        }
        else if (format.indexOf("Text")>-1) {
          text_flag=true;
        }
        else if (format.indexOf("asci")>-1) {
          text_flag=true;
        }
        else if (format.indexOf("Asci")>-1) {
          text_flag=true;
        }
        else if (format.indexOf("ASCI")>-1) {
          text_flag=true;
        }
        
        boolean image_flag = false;
        if (format.indexOf("image")>-1){
          image_flag=true;
        }
        else if (format.indexOf("Image")>-1){
          image_flag=true;
        }
        else if (format.indexOf("IMAGE")>-1){
          image_flag=true;
        }
        else if (format.indexOf("gif")>-1) {
          image_flag=true;
        }
        else if (format.indexOf("GIF")>-1) {
          image_flag=true;
        }
        else if (format.indexOf("jpeg")>-1) {
          image_flag=true;
        }
        else if (format.indexOf("JPEG")>-1) {
          image_flag=true;
        }
        else if (format.indexOf("jpg")>-1) {
          image_flag=true;
        }
        else if (format.indexOf("JPG")>-1) {
          image_flag=true;
        }
        
        if (image_flag) {
          // try to display image here
          String filename = dataFile.getPath();
          Log.debug(30, "trying to display image! "+filename);
          ImageIcon icon = new ImageIcon(filename);
          JLabel imagelabel = new JLabel(icon);
          DataScrollPanel.getViewport().removeAll();
          DataScrollPanel.getViewport().add(imagelabel);
          /*StateChangeMonitor.getInstance().notifyStateChange(
               new StateChangeEvent( 
               DataViewerPanel, 
               StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME));*/
         // Store this event
         storingStateChangeEvent( new StateChangeEvent( 
               DataViewerPanel, 
               StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME));
   
        }
        else if (text_flag) {
          // try building a table
          if ((column_labels!=null)&&(column_labels.size()>0)) {
            if ((field_delimiter.trim().length()>0)) {
              buildTable();
              /*StateChangeMonitor.getInstance().notifyStateChange(
                   new StateChangeEvent( 
                   DataViewerPanel, 
                  StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME));*/
              storingStateChangeEvent( new StateChangeEvent( 
                    DataViewerPanel,
                    StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME));
              
              /*tcuta.setSource(table);
              tca.setSource(table);
              tpa.setTarget(table);*/
              //MouseListener popupListener = new PopupListener();
              //table.addMouseListener(popupListener);
            }
            else if (dataFile==null) {
              numHeaderLines = "0";
              field_delimiter = ",";
              buildTable();
              /*StateChangeMonitor.getInstance().notifyStateChange(
                  new StateChangeEvent( 
                  DataViewerPanel, 
                  StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME));*/
              storingStateChangeEvent( new StateChangeEvent( 
                    DataViewerPanel,
                    StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME));
              
              //MouseListener popupListener = new PopupListener();
              //table.addMouseListener(popupListener);              
            }
            else {
              buildTextDisplay();
              /*StateChangeMonitor.getInstance().notifyStateChange(
                 new StateChangeEvent( 
                 DataViewerPanel, 
               StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME));*/
              storingStateChangeEvent( new StateChangeEvent(
                 DataViewerPanel,
                 StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME));
                
            }
          }
        }
        else {
          //Log.debug(9, "Unable to display data!");
          // Couldn't show data view
          showDataView = false;
          /*StateChangeMonitor.getInstance().notifyStateChange(
                 new StateChangeEvent( 
                 DataViewerPanel, 
               StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME));*/
          storingStateChangeEvent( new StateChangeEvent(
                 DataViewerPanel,
                 StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME));
        
        }
      }
      
    }
    
    public void setDataPackage(DataPackage dp) {
        this.dp = dp;
    }
    
    public void setAttributeFile(File attr) {
        this.attributeFile = attr;
    }
    
    public void setEntityFile(File ent) {
        this.entityFile = ent;
    }
    
    public void setEntityFileId(String id) {
        this.entityFileId = id;
    }


    public void setPhysicalFile(File phys) {
        this.physicalFile = phys;
    }
        
    
    public void setDataID(String dataID) {
        this.dataID = dataID;
        //setTitle("DataFile: "+dataID);
        DataIDLabel.setText("DataFile: "+dataID);
    }
    
    public void getEntityInfo()  {
      if (entityFile==null) {
          Log.debug(15, "Entity information about the data is missing!");
          missing_metadata_flag = true;
      } else {
        // get number of records, etc
        Vector numRecordsPath = new Vector();
        numRecordsPath.addElement("table-entity/numberOfRecords");
        NodeList numRecordsList = PackageUtil.getPathContent(entityFile, 
                                                     numRecordsPath, 
                                                     morpho);  
        if(numRecordsList != null && numRecordsList.getLength() != 0)
        {
          String s = numRecordsList.item(0).getFirstChild().getNodeValue();
          if ((s!=null)&&(s.length()>0))  {
            try {
              num_records = (new Integer(s.trim())).intValue();
            }
            catch(Exception w) {}
          }
        }
        
        Vector entityNamesPath = new Vector();
        entityNamesPath.addElement("table-entity/entityName");
        NodeList entityNamesList = PackageUtil.getPathContent(entityFile, 
                                                     entityNamesPath, 
                                                     morpho);  
        if(entityNamesList != null && entityNamesList.getLength() != 0)
        {
          String s = entityNamesList.item(0).getFirstChild().getNodeValue();
          if ((s!=null)&&(s.length()>0))  {
            try {
              entityName = s.trim();
            }
            catch(Exception w) {}
          }
        }
 
        Vector entityDescriptionPath = new Vector();
        entityDescriptionPath.addElement("table-entity/entityDescription");
        NodeList entityDescriptionList = PackageUtil.getPathContent(entityFile, 
                                                     entityDescriptionPath, 
                                                     morpho);  
        if(entityDescriptionList != null && entityDescriptionList.getLength() != 0)
        {
          String s = entityDescriptionList.item(0).getFirstChild().getNodeValue();
          if ((s!=null)&&(s.length()>0))  {
            try {
              entityDescription = s.trim();
            }
            catch(Exception w) {}
          }
        }
        if (entityName.length()>0) {
          headerLabel.setText(entityName);
        } 
        if (entityDescription.length()>0) {
          headerLabel.setText(entityDescription);
        } 
      }
    }

	static public void main(String args[])
	{
		(new DataViewer()).setVisible(true);
	}


  class PopupListener extends MouseAdapter {
    // on the Mac, popups are triggered on mouse pressed, while mouseReleased triggers them
    // on the PC; use the trigger flag to record a trigger, but do not show popup until the
    // mouse released event (DFH)
    boolean trigger = false;
    
    public void mousePressed(MouseEvent e) 
    {
      //select the clicked row first
      
      /*
      table.clearSelection();
      int selrow = table.rowAtPoint(new Point(e.getX(), e.getY()));
      int selcol = table.columnAtPoint(new Point(e.getX(), e.getY()));
      table.setRowSelectionInterval(selrow, selrow);
      table.setEditingRow(selrow);
      table.setColumnSelectionInterval(selcol, selcol);
      */
      if (e.isPopupTrigger()) 
      {
        trigger = true;
      }  
    }
    
    public void mouseReleased(MouseEvent e) 
    {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) 
    {
      if(e.isPopupTrigger() || trigger) 
      {     
        
	      trigger = false;
        popup.show(e.getComponent(), e.getX(), e.getY());
        
      }
    }
  }
  
  
	/**
	 * parses a line of text data into a Vector of column data for that row
	 * 
	 * @param str a line of string data from input
	 * @return a vector with each elements being column data for the row
	 */
	private Vector getColumnValues(String str) {
	    String sDelim = getDelimiterString();
	    String oldToken = "";
	    String token = "";
	    Vector res = new Vector();
	    boolean ignoreConsequtiveDelimiters = false;
	    if (ignoreConsequtiveDelimiters) {
	      StringTokenizer st = new StringTokenizer(str, sDelim, false);
	      while( st.hasMoreTokens() ) {
	        token = st.nextToken().trim();
	        res.addElement(token);
	      }
	    }
	    else {
	      StringTokenizer st = new StringTokenizer(str, sDelim, true);
	      while( st.hasMoreTokens() ) {
	        token = st.nextToken().trim();
	        if (!inDelimiterList(token, sDelim)) {
	            res.addElement(token);
	        }
	        else {
	            if ((inDelimiterList(oldToken,sDelim))&&(inDelimiterList(token,sDelim))) {
	                res.addElement("");
                }
	        }
	        oldToken = token;
	      }
	    }
	    return res;
	}

	private boolean inDelimiterList(String token, String delim) {
	    boolean result = false;
	    int test = delim.indexOf(token);
	    if (test>-1) {
	        result = true;
	    }
	    else { result = false; }
	    return result;
	}


	private String getDelimiterString() {
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
    delimiter_string = str;
	  return str;
	}
	
  


    /**
     * parses data input string into an array of lines (Strings)
     * 
     * @param s input file
     */

    public void parseFile () {
      if (dataFile==null) return;
        File f = dataFile;
        int i;
        int pos;
        String temp, temp1;
        try{
          BufferedReader in = new BufferedReader(new FileReader(f));
          nlines = 0;
          try {
            while (((temp = in.readLine())!=null)&&(nlines<nlines_max)) {
                if (temp.length()>0) {   // do not count blank lines
                nlines++;} 
            }
            in.close();
          }
        catch (Exception e) {};
        }
        catch (Exception w) {};
        
        lines = new String[nlines];
          // now read again since we know how many lines
        try{  
          BufferedReader in1 = new BufferedReader(new FileReader(f));
          try {
            for (i=0;i<nlines;i++) {
                temp = in1.readLine();
                while (temp.length()==0) {temp=in1.readLine();}
                lines[i] = temp + "\n";

            }
            in1.close();
          }
          catch (Exception e) {};
        }
        catch (Exception w1) {};
    }            


    /**
     * builds a TextArea to display the data as a text file
     */
     private void buildTextDisplay() {
        JTextArea ta = new JTextArea();
        ta.setEditable(false);
        parseFile();
        for (int i=0;i<nlines;i++) {
            ta.append(lines[i]+"\n");   
        }
        ta.setCaretPosition(0);
        DataScrollPanel.getViewport().removeAll();
        DataScrollPanel.getViewport().add(ta);
        
     }

	/**
	 * builds JTable from input data and includes event code for handling clicks on
	 * table (e.g. column selection)
	 * 
	 * @param cTitles
	 * @param data
	 */
	private void buildTable() {
    num_header_lines = 0;
    // Note: numHeaderLines is a String; if temp is null, then
    // it cannot be made into an integer and default num_header_lines int
    // is used (DFH)
    Integer temp = new Integer(numHeaderLines);
    if (temp!=null) {
      num_header_lines = temp.intValue();  
    }

	  vec = new Vector();
    table = new JTable();
    pv = new PersistentVector();
    pv.setFieldDelimiter(field_delimiter);
    pv.setFirstRow(num_header_lines);
    if (dataFile==null) {
      Log.debug(20, "Null Data File");
      field_delimiter = ",";
      String[] row = new String[column_labels.size()];
      for (int ii=0;ii<column_labels.size();ii++) {
        row[ii] = "";  
      }
      pv.initEmpty(row);
    }
    else {
      pv.init(dataFile, num_header_lines);
    }
    ptm = new PersistentTableModel(pv, column_labels);
    ptm.setFieldDelimiter(field_delimiter);
    table.setModel(ptm);
    
    table.setColumnSelectionAllowed(true);
    table.setRowSelectionAllowed(true);
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	  (table.getTableHeader()).setReorderingAllowed(false);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    table.registerKeyboardAction (new TableCopyAction(),
                          KeyStroke.getKeyStroke("ctrl C"),
                          JComponent.WHEN_FOCUSED);
    table.registerKeyboardAction (new TablePasteAction(),
                          KeyStroke.getKeyStroke("ctrl V"),
                          JComponent.WHEN_FOCUSED);
    table.registerKeyboardAction (new TableCutAction(),
                          KeyStroke.getKeyStroke("ctrl X"),
                          JComponent.WHEN_FOCUSED);
      
    DataScrollPanel.getViewport().removeAll();
    DataScrollPanel.getViewport().add(table);
    
    JTableHeader header = table.getTableHeader();
    header.addMouseListener(new HeaderMouseListener());
    MouseListener popupListener = new PopupListener();
    table.addMouseListener(popupListener);  
    if (table.getRowCount()>0) {
        table.setRowSelectionInterval(0,0);
        table.setColumnSelectionInterval(0,0);
    }
    setUpDelimiterEditor(table, field_delimiter, TablePanel);
   
	}
  
    /*
   * Method to set a table's interger and string column editor
   */
   private void setUpDelimiterEditor(JTable jtable, String delimiter,
                                                         JPanel pane) 
   {
      //Set up the editor for the integer and string cells.
      int columns = jtable.getColumnCount();
      final DelimiterField delimiterField = 
                              new DelimiterField(pane, delimiter, "", columns);
      delimiterField.setHorizontalAlignment(DelimiterField.RIGHT);

      DefaultCellEditor delimiterEditor =
            new DefaultCellEditor(delimiterField) 
            {
                //Override DefaultCellEditor's getCellEditorValue method
                public Object getCellEditorValue() 
                {
                    return new String(delimiterField.getValue());
                }
            };
       TableColumnModel columnModel = jtable.getColumnModel();
       for (int j = 0; j< columns; j++)
       {
          columnModel.getColumn(j).setCellEditor(delimiterEditor);
          columnModel.getColumn(j).setPreferredWidth(85);
       }
     
    }

 

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == CancelButton)
				CancelButton_actionPerformed(event);
			else if (object == UpdateButton)
				UpdateButton_actionPerformed(event);
		}
	}

	void CancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
// following is simply a test to see if the Log function in the
// PresistentTableModel works
/*    
		Stack st = ptm.getLogStack();
    while (!st.empty()) {
      String[] temp = (String[])st.pop();
      String tmp = temp[0]+";"+temp[1]+";"+temp[2]+";"+temp[3]+
                              ";"+temp[4]+";"+temp[5];
      System.out.println(tmp);
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
   }
// the init() function will create a new PersistentTableVector, thus
// destroying the Log
*/   
		this.init();
	}
	
	/* 
	 * Convert the vector of vectors with the table data back to a string
	 */
	void vecToString() {
	  Vector innerVec;
	  StringBuffer headerlines = new StringBuffer();
	  StringBuffer resultString = new StringBuffer();
	  for (int k=0;k<num_header_lines;k++){
        headerlines.append(lines[k].trim()+"\n");
	  }
	  resultString.append(headerlines.toString());
	  for (int i=0;i<nlines-1;i++) {
	    StringBuffer lineString = new StringBuffer();
	    innerVec = (Vector)vec.elementAt(i);
	    for (int j=0;j<innerVec.size();j++) {
	      lineString.append((String)innerVec.elementAt(j)+delimiter_string);
	    }
	    resultString.append(lineString.toString()+"\n");
	  }
	  dataString = resultString.toString();
	}
  
  /*
   * This method assigns the same menuItems that are in the popup
   * menu to appear in the menubar for the MorphoFrame where the
   * DataViewer appears
   */
  private void updateDataMenu() {
    MorphoFrame mf = UIController.getInstance().getCurrentActiveWindow();
    JMenuBar mb = mf.getJMenuBar();
    if (mb==null) {
      Log.debug(20, "MenuBar is null!");
    }
    else {
      
      JMenu menu = mb.getMenu(1);  // the "Edit" menu
      menu.removeAll();

      JMenuItem cutMenuItem = new JMenuItem("Cut");
      tcuta = new TableCutAction(table);
      tcuta.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control C"));
      tcuta.putValue(Action.SHORT_DESCRIPTION,
                "Cut the selection and put it on the Clipboard");
      tcuta.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/Cut16.gif")));
      cutMenuItem.setAction(tcuta);

      JMenuItem copyMenuItem = new JMenuItem("Copy");
      tca = new TableCopyAction(table);
      tca.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control C"));
      tca.putValue(Action.SHORT_DESCRIPTION,
                "Copy the selection and put it on the Clipboard");
      tca.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/Copy16.gif")));
      copyMenuItem.setAction(tca);

      JMenuItem pasteMenuItem = new JMenuItem("Paste");
      tpa = new TablePasteAction(table);
      tpa.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control P"));tpa.putValue(Action.SHORT_DESCRIPTION,
                "Paste from the Clipboard to the selection.");
      tpa.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/Paste16.gif")));
      pasteMenuItem.setAction(tpa);
 
      JMenuItem prefMenuItem = new JMenuItem("Preferences...", new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
      prefMenuItem.setEnabled(false);
     
      menu.add(cutMenuItem);
      menu.add(copyMenuItem);
      menu.add(pasteMenuItem);
      menu.add(new JSeparator());
      menu.add(prefMenuItem);

    }    
  }
  



    /**
   * this is called whenever the editor exits.  the file returned is saved
   * back to its  original location.
   * @param xmlString the xml in string format
   * @param id the id of the file
   * @param location the location of the file
   */
  public void editingCompleted(String xmlString, String id, String location)
  {
    //System.out.println(xmlString);
    Log.debug(11, "editing complete: id: " + id + " location: " + location);
    AccessionNumber a = new AccessionNumber(morpho);
    boolean metacatpublic = false;
    FileSystemDataStore fsds = new FileSystemDataStore(morpho);
    //System.out.println(xmlString);
  
    boolean metacatloc = false;
    boolean localloc = false;
    boolean bothloc = false;
    String newid = "";
    String newPackageId = "";
    if(location.equals(DataPackageInterface.BOTH))
    {
      metacatloc = true;
      localloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
    {
      localloc = true;
    }
  
    try
    { 
      if(localloc)
      { //save the file locally
        if(id.trim().equals(dp.getID().trim()))
        { //we just edited the package file itself
          String oldid = id;
          newid = a.incRev(id);
          File f = fsds.saveTempFile(oldid, new StringReader(xmlString));
          String newPackageFile = a.incRevInTriples(f, oldid, newid);
          fsds.saveFile(newid, new StringReader(newPackageFile));
          newPackageId = newid;
        }
        else
        { //we edited a file in the package
          Vector newids = new Vector();
          Vector oldids = new Vector();
          String oldid = id;
          newid = a.incRev(id);
          fsds.saveFile(newid, new StringReader(xmlString));
          newPackageId = a.incRev(dp.getID());
          oldids.addElement(oldid);
          oldids.addElement(dp.getID());
          newids.addElement(newid);
          newids.addElement(newPackageId);
          //increment the package files id in the triples
          String newPackageFile = a.incRevInTriples(dp.getTriplesFile(), 
                                                    oldids, 
                                                    newids);
          System.out.println("oldid: " + oldid + " newid: " + newid);          
          fsds.saveFile(newPackageId, new StringReader(newPackageFile)); 
        }
      }
    }
    catch(Exception e)
    {
      Log.debug(0, "Error saving file locally"+ id + " to " + location +
                         "--message: " + e.getMessage());
      Log.debug(11, "File: " + xmlString);
      e.printStackTrace();
    }
    
    try
    {
      if(metacatloc)
      { //save it to metacat
        MetacatDataStore mds = new MetacatDataStore(morpho);
        
        if(id.trim().equals(dp.getID().trim()))
        { //edit the package file
          Vector oldids = new Vector();
          Vector newids = new Vector();
          String oldid = id;
          newid = a.incRev(id);
          File f = fsds.saveTempFile(oldid, new StringReader(xmlString));
          oldids.addElement(oldid);
          newids.addElement(newid);
          String newPackageFile = a.incRevInTriples(f, oldids, newids);
          mds.saveFile(newid, new StringReader(newPackageFile), 
                       dp);
          newPackageId = newid;
        }
        else
        { //edit another file in the package
          Vector oldids = new Vector();
          Vector newids = new Vector();
          String oldid = id;
          newid = a.incRev(id);
 //         mds.saveFile(newid, new StringReader(xmlString), dp);
          Vector names = new Vector();
          Vector readers = new Vector();
          names.addElement(newid);
          readers.addElement(new StringReader(xmlString));
          newPackageId = a.incRev(dp.getID());
          //increment the package files id in the triples
          oldids.addElement(oldid);
          oldids.addElement(dp.getID());
          newids.addElement(newid);
          newids.addElement(newPackageId);
          String newPackageFile = a.incRevInTriples(dp.getTriplesFile(),
                                                    oldids,
                                                    newids);
//          mds.saveFile(newPackageId, new StringReader(newPackageFile), 
//                       dp);
          names.addElement(newPackageId);
          readers.addElement(new StringReader(newPackageFile));
          String res = mds.saveFilesTransaction(names, readers, dp);
          Log.debug(20,"Transaction result is: "+res);
        }
      }
    }
    catch(Exception e)
    {
      String message = e.getMessage();
      if(message.indexOf("Next revision number must be") != -1)
      {
        Log.debug(0,"The file you are attempting to update " +
                                 "has been changed by another user.  " +
                                 "Please refresh your query screen, " + 
                                 "open the package again and " +
                                 "re-enter your changes.");
        return;
      }
      Log.debug(0, "Error saving file to metacat "+ id + " to " + location +
                         "--message: " + e.getMessage());
      e.printStackTrace();
    }
    
    DataPackage newPackage = new DataPackage(location, newPackageId, null,
                                                 morpho);

    MorphoFrame thisFrame = (UIController.getInstance()).getCurrentActiveWindow();
    
        // Show the new package
    try 
    {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider = 
                      services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      dataPackage.openDataPackage(location, newPackage.getID(), null, null);
    }
    catch (ServiceNotHandledException snhe) 
    {
       Log.debug(6, snhe.getMessage());
    }
    thisFrame.setVisible(false);
    UIController controller = UIController.getInstance();
    controller.removeWindow(thisFrame);
    thisFrame.dispose();

    
  }
  
  public void editingCanceled(String xmlString, String id, String location)
  { //do nothing
  }

   /**
   * Method implements form StoreStateChangeEvent
   * This method will be called to store a event
   *
   * @param event  the state change event need to be stored
   */
  public void storingStateChangeEvent(StateChangeEvent event)
  {
    if (storedStateChangeEventlist != null)
    {
      storedStateChangeEventlist.add(event);
    }
  }
  
    
  /**
   * Get the  stored state change event.
   */
  public Vector getStoredStateChangeEvent()
  {
    return storedStateChangeEventlist;
  }
  
  /**
   * Broadcast the stored StateChangeEvent
   */
  public void broadcastStoredStateChangeEvent()
  {
    if (storedStateChangeEventlist != null)
    {
      for ( int i = 0; i< storedStateChangeEventlist.size(); i++)
      {
        StateChangeEvent event = 
                (StateChangeEvent) storedStateChangeEventlist.elementAt(i);
        (StateChangeMonitor.getInstance()).notifyStateChange(event);
      }//for
    }//if
  }
  
	void UpdateButton_actionPerformed(java.awt.event.ActionEvent event)
	{ 
    TripleCollection triples = null;
    MorphoFrame thisFrame = null;
    DataPackage newPackage = null;
    File tempfile = null;
    File tempfileAttr = null;
    File tempfilePhy = null;
    File tempfileEnt = null;
	  if (dp!=null) {
      // make a temporary copy of the data file
      // PersistentVector get from ptm
      //PersistentVector pVector = ptm.getPersistentVector(); 
      //ptm.setFieldDelimiter(delimiter_string);
      //pVector.setFieldDelimiter(delimiter_string);
      //pVector.setFirstRow(num_header_lines);
      ptm.getPersistentVector().writeObjects(tempdir + "/" + "tempdata");
      File newDataFile = new File(tempdir + "/" + "tempdata");
      long newDataFileLength = newDataFile.length();
           
      
      try {
        String attrDocType = "<!DOCTYPE eml-attribute PUBLIC "+
            "\"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "+
            "\"eml-attribute.dtd\">";
        String entityDocType = "<!DOCTYPE table-entity PUBLIC "+
            "\"-//ecoinformatics.org//eml-entity-2.0.0beta6//EN\" "+
            "\"eml-entity.dtd\">";
        String physicalDocType = "<!DOCTYPE eml-physical PUBLIC "+
            "\"-//ecoinformatics.org//eml-physical-2.0.0beta6//EN\" "+
            "\"eml-physical.dtd\">";
            
        // changes in the attribute metadata should be up-to-date since they
        // are changed when columns are added or deleted.
        // thus just save a copy to a file
        PackageUtil.saveDOM(tempdir + "/" + "tempattribute", attributeDoc, attrDocType, morpho);
        
        // entity metadata needs to be changed due to a change in the number of records
        // that occurs when rows are added to the dataset
        // Here we get the current entity metadata and save a new temp copy
        
        Document doc = PackageUtil.getDoc(entityFile, morpho);
        NodeList nl = doc.getElementsByTagName("numberOfRecords");
        Node textNode = nl.item(0).getFirstChild(); // assumed to be a text node
        int rowcnt = ptm.getRowCount();
        String rowcntS = (new Integer(rowcnt)).toString();
        textNode.setNodeValue(rowcntS);  //set to new record count
        PackageUtil.saveDOM(tempdir + "/" + "tempentity", doc, entityDocType, morpho);
        
        // physical metadata needs to be updated due to change in datafile size (and
        // perhaps the delimiter)
        doc = PackageUtil.getDoc(physicalFile, morpho);
        nl = doc.getElementsByTagName("size");
        textNode = nl.item(0).getFirstChild(); // assumed to be a text node
        String sizeS = (new Long(newDataFileLength)).toString();
        textNode.setNodeValue(sizeS);  //set to new file length

        doc = PackageUtil.getDoc(physicalFile, morpho);
        nl = doc.getElementsByTagName("fieldDelimiter");
        textNode = nl.item(0).getFirstChild(); // assumed to be a text node
        textNode.setNodeValue(field_delimiter);  //set to delimiter
        
        
        PackageUtil.saveDOM(tempdir + "/" + "tempphysical", doc, physicalDocType, morpho);
      }
      catch (Exception q) {
        Log.debug(20, "Error trying to save from DOM");
      }
      
      
        AccessionNumber a = new AccessionNumber(morpho);
        FileSystemDataStore fsds = new FileSystemDataStore(morpho);
        MetacatDataStore mds = new MetacatDataStore(morpho);
  
        boolean metacatloc = false;
        boolean localloc = false;
        boolean bothloc = false;
        String newid = "";
        String location = dp.getLocation();
        String newPackageId = "";
        if(location.equals(DataPackageInterface.BOTH))
        {
            metacatloc = true;
            localloc = true;
        }
        else if(location.equals(DataPackageInterface.METACAT))
        {
            metacatloc = true;
        }
        else if(location.equals(DataPackageInterface.LOCAL))
        {
            localloc = true;
        }

        try{
          Vector newids = new Vector();
          Vector oldids = new Vector();
          String oldid = dataID;
          if (oldid.length()>0) {
           newid = a.incRev(dataID);
          }
          else {
            newid = a.getNextId();
          }
          // save data to a temporary file
          FileReader fr = null;
          FileReader frAttr = null;
          FileReader frPhy = null;
          FileReader frEnt = null;
          
         try{
           //ptm.getPersistentVector().writeObjects(tempdir + "/" + "tempdata");
            tempfile = new File(tempdir + "/" + "tempdata");
          }
          catch (Exception ww) {
            Log.debug(1,"Problem making temporary copy of data");
          }
          
          
          try{
            tempfileAttr = new File(tempdir + "/" + "tempattribute");
          }
          catch (Exception ww) {
            Log.debug(20,"Problem making Attr FileReader");
          }
          String attrFileId = dp.getAttributeFileId(entityFileId);
          String newAttrFileId = a.incRev(attrFileId);
          oldids.addElement(attrFileId);
          newids.addElement(newAttrFileId);

          try{
            tempfilePhy = new File(tempdir + "/" + "tempphysical");
          }
          catch (Exception ww) {
            Log.debug(20,"Problem making Physical FileReader");
          }
          String phyFileId = dp.getPhysicalFileId(entityFileId);
          String newPhyFileId = a.incRev(phyFileId);
          oldids.addElement(phyFileId);
          newids.addElement(newPhyFileId);          
           
          try{
            tempfileEnt = new File(tempdir + "/" + "tempentity");
          }
          catch (Exception ww) {
            Log.debug(20,"Problem making Entity FileReader");
          }
          String newEntFileId = a.incRev(entityFileId);
          oldids.addElement(entityFileId);
          newids.addElement(newEntFileId);          
      
      fr = new FileReader(tempfile);
      frAttr = new FileReader(tempfileAttr);
      frPhy = new FileReader(tempfilePhy);
      frEnt = new FileReader(tempfileEnt);
      

      if(localloc)
      { //save it locally
        try{
          
          fsds.saveFile(newid, fr);  // this is the new datafile
          
          fsds.saveFile(newAttrFileId, frAttr);  // new attribute file

          fsds.saveFile(newPhyFileId, frPhy);  // new physical file

          fsds.saveFile(newEntFileId, frEnt);  // new entity file
           
          newPackageId = a.incRev(dp.getID());
          oldids.addElement(oldid);
          oldids.addElement(dp.getID());
          newids.addElement(newid);
          newids.addElement(newPackageId);
          //increment the package files id in the triples
          String newPackageFile = a.incRevInTriples(dp.getTriplesFile(), 
                                                    oldids, 
                                                    newids);
          // handle case where there us currently no datafile in the package
          // by adding triples for non-existing datefile and access file
          // and add triple connecting entity to data
          if (!dp.hasDataFile(entityFileId)) {
            triples = buildTriplesForNewData(dp.getAccessId(), newEntFileId, newid);
            newPackageFile = PackageUtil.addTriplesToTriplesString(triples,
                                                    newPackageFile,
                                                    morpho); 
          }
   System.out.println(newPackageFile);       
          fsds.saveFile(newPackageId, new StringReader(newPackageFile)); 
          
          fr.close();
          frAttr.close();
          frPhy.close();
          frEnt.close();

        }
        catch (Exception e) {
          Log.debug(20, "error in local update");    
        }
      }
      
      // now recreate readers since they may have been used in creating local docs
      fr = new FileReader(tempfile);
      frAttr = new FileReader(tempfileAttr);
      frPhy = new FileReader(tempfilePhy);
      frEnt = new FileReader(tempfileEnt);

      if(metacatloc)
      { //save it to metacat
        oldid = dataID;
        newid = a.incRev(dataID);
        try{

          mds.newDataFile(newid, tempfile);
          
          mds.saveFile(newAttrFileId, frAttr, dp);  // new attribute file

          mds.saveFile(newPhyFileId, frPhy, dp);  // new physical file

          mds.saveFile(newEntFileId, frEnt, dp);  // new entity file
           

          newPackageId = a.incRev(dp.getID());
          oldids.addElement(oldid);
          oldids.addElement(dp.getID());
          newids.addElement(newid);
          newids.addElement(newPackageId);
          
          //increment the package files id in the triples
          String newPackageFile = a.incRevInTriples(dp.getTriplesFile(), 
                                                    oldids, 
                                                    newids);

          // handle case where there us currently no datafile in the package
          // by adding triples for non-existing datefile and access file
          // and add triple connecting entity to data
          if (!dp.hasDataFile(entityFileId)) {
            triples = buildTriplesForNewData(dp.getAccessId(),newEntFileId,newid);
            newPackageFile = PackageUtil.addTriplesToTriplesString(triples,
                                                    newPackageFile,
                                                    morpho); 
          }
          
          Log.debug(20, "oldid: " + oldid + " newid: " + newid);          
          mds.saveFile(newPackageId, new StringReader(newPackageFile), dp); 
          
          fr.close();
          frAttr.close();
          frPhy.close();
          frEnt.close();

        }
        catch (Exception e) {
            Log.debug(20, "error in metacat update of data file"+e.getMessage());    
        }
      }
      newPackage = new DataPackage(location, newPackageId, null,
                                                 morpho);
                                                 
      thisFrame = (UIController.getInstance()).getCurrentActiveWindow();
      }
      catch (Exception www) {
        Log.debug(1, "Error!"+www.getMessage());
      }
  
    // Show the new package
    try 
    {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider = 
                      services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      dataPackage.openDataPackage(location, newPackage.getID(), null, null);
    }
    catch (ServiceNotHandledException snhe) 
    {
       Log.debug(6, snhe.getMessage());
    }
    
    thisFrame.setVisible(false);
    UIController controller = UIController.getInstance();
    controller.removeWindow(thisFrame);
    thisFrame.dispose();


	  }		
    
	}

	private TripleCollection buildTriplesForNewData(String accessId, 
                                  String entityFileId,
                                  String dataid) {
        Triple t = new Triple(dataid, "isDataFileFor", dp.getID());
        TripleCollection triples = new TripleCollection();
        triples.addTriple(t);
    
        // add an access triple for the new datafile
        Triple tacc = new Triple(accessId,"provides access control rules for", dataid);
        triples.addTriple(tacc);
    
        // connect this entity to the new datafile
        Triple t1 = new Triple(entityFileId, 
                              "provides table-entity information for DATAFILE", 
                              dataid);
        triples.addTriple(t1);
        return triples;
  }
  
  class HeaderMouseListener implements MouseListener {

    /**
     * Mouse click event handler
     */
    private boolean trigger = false;
    public void mouseClicked(MouseEvent event) 
    {
      TableColumnModel colModel = table.getColumnModel();
      int index = colModel.getColumnIndexAtX(event.getX());
      TableColumn column = colModel.getColumn(index);
      int modelIndex = column.getModelIndex();
      if (table.getRowCount()>0) {
          table.setRowSelectionInterval(0, table.getRowCount()-1);
      }
      table.setColumnSelectionInterval(modelIndex, modelIndex);
      if (event.isPopupTrigger()) 
      {
        // Show popup menu
        trigger = true;
      }
      else
      {
        // Fire a state change event to show attribute file in meta panel
        StateChangeEvent stateEvent = new 
              StateChangeEvent(table,StateChangeEvent.SELECT_DATATABLE_COLUMN);
        StateChangeMonitor stateMonitor = StateChangeMonitor.getInstance();
        stateMonitor.notifyStateChange(stateEvent);
      }  
    }
    
    public void mouseReleased(MouseEvent e) 
    {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) 
    {
      if(e.isPopupTrigger() || trigger) 
      {     
        
	      trigger = false;
        popup.show(e.getComponent(), e.getX(), e.getY());
        
      }
    }
    //public void mouseReleased(MouseEvent event){}
    public void mousePressed(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}    
  }
  
}
