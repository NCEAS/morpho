/*  '$RCSfile: DataViewer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-02-06 19:46:02 $'
 * '$Revision: 1.101 $'
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
  private JMenuItem deleteDatatable = null;
  private GUIAction deleteDatatableAction = null;
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
   *   id of the file containing the data
   */
   String dataFileId = null;

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
//DFH	 DataPackage dp = null;

   /**
	 * The AbstractDataPackage that contains the data (eml2.0.0)
	 */
	 AbstractDataPackage adp = null;

   /**
	 *  The index of the entity within the AbstractDataPackage
   *  Note that we do not need an entityfile, etc when using
   *  the AbstractDataPackage!
	 */
	 int entityIndex = -1;

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
		CancelButton.setText("Revert");
		CancelButton.setActionCommand("Revert");
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
    createNewDatatableAction = new GUIAction("Create/Import New Datatable...", null,
                                                new ImportDataCommand());
    createNewDatatable = new JMenuItem(createNewDatatableAction);
    popup.add(createNewDatatable);
    deleteDatatableAction = new GUIAction("Delete Current Datatable", null,
                                                new DeleteTableCommand());
    deleteDatatable = new JMenuItem(deleteDatatableAction);
    popup.add(deleteDatatable);
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

    public void setDataFile(File file) {
      this.dataFile = file;
    }

    public void setDataFileId(String dfid) {
      this.dataFileId = dfid;
    }

    public PersistentVector getPV() {
      return pv;
    }
		
		public void setPV(PersistentVector vector) {
			pv = vector;
			return;
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


   public void setEntityIndex(int indx) {
     entityIndex = indx;
   }


   public int getEntityIndex() {
     return this.entityIndex;
   }
    /**
     * Initialization code which collects information about the data
     * from various metadata modules associated with the entity to
     * be displayed
     */
    public void init() {
      missing_metadata_flag = false;
        if (entityIndex == -1) {
          Log.debug(1, "Entity index has not been set!");
          return;
        } else {
          Node[] physicalArray = adp.getPhysicalArray(entityIndex);
          if (physicalArray.length==0) {
            Log.debug(15, "Physical information about the data is missing!");
            missing_metadata_flag = true;
          } else {
          // get format, recordDelimiter, field delimiter
          // in general, get all info need to read a record
          this.format = adp.getPhysicalFormat(entityIndex, 0);
              // assume that we always use the first physical object
          Log.debug(20, "format: "+format);
          this.field_delimiter = adp.getPhysicalFieldDelimiter(entityIndex, 0);
          this.delimiter_string = getDelimiterString();
          Log.debug(20, "delimiter_string: "+delimiter_string);
          String nhl = adp.getPhysicalNumberHeaderLines(entityIndex, 0);
          if (nhl.length()>0) {
            this.numHeaderLines = nhl;
          } else {
            this.numHeaderLines = "0";
          }
          Log.debug(20, "numHeaderLines: "+numHeaderLines);
          }
          // get entity info (number of records, etc)
          String s = adp.getEntityNumRecords(entityIndex);
          if ((s!=null)&&(s.length()>0))  {
            try {
              num_records = (new Integer(s.trim())).intValue();
            }
            catch(Exception w) {Log.debug(20, "error converting to integer");}
          }
          entityName = adp.getEntityName(entityIndex).trim();
          entityDescription = adp.getEntityDescription(entityIndex).trim();
          if (entityName.length()>0) {
            headerLabel.setText(entityName);
          }
          if (entityDescription.length()>0) {
            headerLabel.setText(entityDescription);
          }
          Node[] attributeArray = adp.getAttributeArray(entityIndex);
          if (attributeArray.length==0) {
            Log.debug(15, "Attribute information about the data is missing!");
            missing_metadata_flag = true;
          } else {
            column_labels = new Vector();
            for (int i=0;i<attributeArray.length;i++) {
              String unitString = "";
              String dataTypeString = "";
              String temp = adp.getAttributeName(entityIndex, i);
              // attribute Name is a required node; we want the associated
              // unit and dataType node values, which are NOT required
              dataTypeString = adp.getAttributeDataType(entityIndex, i);
              unitString = adp.getAttributeUnit(entityIndex, i);

              temp = "<html><font face=\"Courier\"> <center><small>"+dataTypeString+"<br>"+unitString
                                                  +"<br></small><b>"
                                                  +temp+"</b></center></font></html>";
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
        //Log.debug(1,"format: "+format);
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
        else if ((format.trim()).length()<1) {
          Log.debug(1, "Format string in physical module is empty!");
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
        else if ((text_flag)&&(dataFile!=null)) {
          // try building a table
          if ((column_labels!=null)&&(column_labels.size()>0)) {
            if ((field_delimiter.trim().length()>0)&&
                (dataFile.length()>0)) {
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
            else if ((dataFile==null)||((dataFile.length()<1))) {
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
          // Couldn't show data view
          // create an empty table that cannot be edited since
          // do not know how to display
          Log.debug(9, "Unable to display the data when stored in this format ("
          +format+"). \nHowever, an empty table with"
                    +" the column header information will be shown.");
          showDataView = false;
          dataFile = null;
          buildTable();
          table.setEnabled(false);
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

    public void setAbstractDataPackage(AbstractDataPackage adp) {
      this.adp = adp;
    }

    public AbstractDataPackage getAbstractDataPackage() {
      return this.adp;
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
    if ((dataFile==null)||(dataFile.length()<1)) {
      Log.debug(20, "Null Data File");
      field_delimiter = ",";
      String[] row = new String[column_labels.size()];
      for (int ii=0;ii<column_labels.size();ii++) {
        row[ii] = "";
      }
      pv.initEmpty(row);
      pv.setFieldDelimiter(field_delimiter);
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

    table.registerKeyboardAction (new GUIAction("Copy", null, new TableCopyCommand()),
                          KeyStroke.getKeyStroke("ctrl C"),
                          JComponent.WHEN_FOCUSED);
    table.registerKeyboardAction (new GUIAction("Paste", null, new TablePasteCommand()),
                          KeyStroke.getKeyStroke("ctrl V"),
                          JComponent.WHEN_FOCUSED);
    table.registerKeyboardAction (new GUIAction("Cut", null, new TableCutCommand()),
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
/*DFH
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
                                           morpho, true);

    MorphoFrame thisFrame = (UIController.getInstance()).getCurrentActiveWindow();

        // Show the new package
    try
    {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider =
                      services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      dataPackage.openDataPackage(location, newPackage.getID(), null, null, null);
    }
    catch (ServiceNotHandledException snhe)
    {
       Log.debug(6, snhe.getMessage());
    }
    thisFrame.setVisible(false);
    UIController controller = UIController.getInstance();
    controller.removeWindow(thisFrame);
    thisFrame.dispose();
*/

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
	
	/**
	*
	*	Method to save the current data table after a change has been made. This is
	*	equivalent to the user pressing the 'Update' button, except that a new window is
	*	not opened
	*/
	
	public void saveCurrentTable() {
		
		
		if (adp!=null) {  // new eml2.0.0 handling
			String id = "";
			AccessionNumber an = new AccessionNumber(Morpho.thisStaticInstance);
			if (dataFileId==null) {
				id = an.getNextId();
			} else {
				id = an.incRev(dataFileId);
			}
			dataFileId = id;  // update to new value
			String tempfilename = parseId(id);
			ptm.getPersistentVector().writeObjects(tempdir + "/" + tempfilename);
			
			File newDataFile = new File(tempdir + "/" + tempfilename);
			long newDataFileLength = newDataFile.length();
			
			int rowcnt = ptm.getRowCount();
			String rowcntS = (new Integer(rowcnt)).toString();
			adp.setEntityNumRecords(entityIndex, rowcntS);
			
			String sizeS = (new Long(newDataFileLength)).toString();
			adp.setPhysicalSize(entityIndex, 0, sizeS);
			
			adp.setPhysicalFieldDelimiter(entityIndex, 0, field_delimiter);
			adp.setDistributionUrl(entityIndex, 0, 0, "ecogrid://knb/"+dataFileId);
			adp.setLocation("");
			
			AccessionNumber a = new AccessionNumber(morpho);
			String curid = adp.getAccessionNumber();
			String newid = null;
			if (!curid.equals("")) {
				newid = a.incRev(curid);
			} else {
				newid = a.getNextId();
			}
			adp.setAccessionNumber(newid);
		}
		
	}
	
	void UpdateButton_actionPerformed(java.awt.event.ActionEvent event)
	{
    
		MorphoFrame thisFrame = null;
		saveCurrentTable();
		// Log.debug(1,"Data File Number of Records: "+adp.getEntityNumRecords(entityIndex));
		// Log.debug(1,"Physical Size: "+adp.getPhysicalSize(entityIndex,0));
		// Log.debug(1,"Field Delimiter: "+adp.getPhysicalFieldDelimiter(entityIndex,0));
		
		if(adp != null) {
			MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
			morphoFrame.setVisible(false);
			UIController uicontroller = UIController.getInstance();
			try{
				ServiceController services = ServiceController.getInstance();
				ServiceProvider provider = services.getServiceProvider(DataPackageInterface.class);
				DataPackageInterface dataPackageInt = (DataPackageInterface)provider;
				dataPackageInt.openNewDataPackage(adp, null);
				uicontroller.removeWindow(morphoFrame);
				morphoFrame.dispose();
				
			} catch (Exception e) {
				Log.debug(5, "Exception in converting edited XML to DOM!");
			}
			
		}
	}

  /**
   * Parses a dotted notation id into a file path.  johnson2343.13223 becomes
   * johnson2343/13223.  Revision numbers are left on the end so
   * johnson2343.13223.2 becomes johnson2343/13223.2
   */
  private String parseId(String id)
  {
    String path = new String();
    path = id.substring(0, id.indexOf("."));
    // now create a directory in the temp dir if it does not exist
    File pathFile = new File(tempdir + "/" + path);
    if (!pathFile.exists()) {
      pathFile.mkdir();
    }
    path += "/" + id.substring(id.indexOf(separator) + 1, id.length());
    return path;
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
