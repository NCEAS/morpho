/**
 *  '$RCSfile: DataViewer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-09-04 14:56:47 $'
 * '$Revision: 1.30 $'
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

import edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.*;
import edu.ucsb.nceas.morpho.util.*;

//public class DataViewer extends javax.swing.JFrame
public class DataViewer extends javax.swing.JPanel
{
	public JPanel DataViewerPanel = new javax.swing.JPanel();
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

    PersistentVector pv;
    PersistentTableModel ptm;
    JTable table;
    
    int sortdirection = 1;
    boolean columnAddFlag = true;
    
    Document attributeDoc;
  
    Morpho framework;
    ConfigXML config;
    String datadir;
    String separator;
    String cachedir;
    String tempdir;
    String dataString = "";
    String dataID = "";
    
    DataPackageGUI grandParent;
    EntityGUI parent;
    
  /**popup menu for right clicks*/
  private JPopupMenu popup;
  /**menu items for the popup menu*/
  private JMenuItem createNewDatatable = new JMenuItem("Create New Datatable...");
  private JMenuItem sortBySelectedColumn = new JMenuItem("Sort by Selected Column");
  private JMenuItem insertRowAfter = new JMenuItem("insert Row After Selected Row");
  private JMenuItem insertRowBefore = new JMenuItem("insert Row Before Selected Row");
  private JMenuItem deleteRow = new JMenuItem("Delete Selected Row");
  private JMenuItem insertColumnBefore = new JMenuItem("insert Column Before Selected Column");
  private JMenuItem insertColumnAfter = new JMenuItem("insert Column After Selected Column");
  private JMenuItem deleteColumn = new JMenuItem("Delete Selected Column");
  private JMenuItem editColumnMetadata = new JMenuItem("Edit Column Metadata");

 // The following instances of JMenu are apparently needed to make a
 // menus that appears in both the menu bar and in a popup menu
  private JMenuItem createNewDatatable1 = new JMenuItem("Create New Datatable...");
  private JMenuItem sortBySelectedColumn1 = new JMenuItem("Sort by Selected Column");
  private JMenuItem insertRowAfter1 = new JMenuItem("insert Row After Selected Row");
  private JMenuItem insertRowBefore1 = new JMenuItem("insert Row Before Selected Row");
  private JMenuItem deleteRow1 = new JMenuItem("Delete Selected Row");
  private JMenuItem insertColumnBefore1 = new JMenuItem("insert Column Before Selected Column");
  private JMenuItem insertColumnAfter1 = new JMenuItem("insert Column After Selected Column");
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
   String numHeaderLines = "";

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
  
  boolean missing_metadata_flag = false;

	public DataViewer()
	{
		setLayout(new BorderLayout(0,0));
//    setSize(755,483);
		setVisible(false);
		DataViewerPanel.setLayout(new BorderLayout(0,0));
		add(DataViewerPanel);
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
		ButtonControlPanel.add(CancelButton);
		UpdateButton.setText("Update");
		UpdateButton.setActionCommand("Update");
		ButtonControlPanel.add(UpdateButton);
    
    headerLabel = new JLabel("DataTitle");
    headerLabel.setForeground(Color.white);
    HeaderPanel.setBackground(Color.gray);
    HeaderPanel.add(headerLabel);
		DataViewerPanel.add(BorderLayout.NORTH, HeaderPanel);
    
	
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		CancelButton.addActionListener(lSymAction);
		UpdateButton.addActionListener(lSymAction);
    //}}
	
    //Build the popup menu for the right click functionality
    popup = new JPopupMenu();
    popup.add(createNewDatatable);
    popup.add(new JSeparator());
    popup.add(sortBySelectedColumn);
    popup.add(new JSeparator());
    popup.add(insertRowAfter);
    popup.add(insertRowBefore);
    popup.add(deleteRow);
    popup.add(new JSeparator());
    popup.add(insertColumnAfter);
    popup.add(insertColumnBefore);
    popup.add(deleteColumn);
    popup.add(new JSeparator());
    popup.add(editColumnMetadata);
    
    MenuAction menuhandler = new MenuAction();
    createNewDatatable.addActionListener(menuhandler);
    sortBySelectedColumn.addActionListener(menuhandler);
    insertRowAfter.addActionListener(menuhandler);
    insertRowBefore.addActionListener(menuhandler);
    deleteRow.addActionListener(menuhandler);
    insertColumnAfter.addActionListener(menuhandler);
    insertColumnBefore.addActionListener(menuhandler);
    deleteColumn.addActionListener(menuhandler);
    editColumnMetadata.addActionListener(menuhandler);
    createNewDatatable1.addActionListener(menuhandler);
    sortBySelectedColumn1.addActionListener(menuhandler);
    insertRowAfter1.addActionListener(menuhandler);
    insertRowBefore1.addActionListener(menuhandler);
    deleteRow1.addActionListener(menuhandler);
    insertColumnAfter1.addActionListener(menuhandler);
    insertColumnBefore1.addActionListener(menuhandler);
    deleteColumn1.addActionListener(menuhandler);
    editColumnMetadata1.addActionListener(menuhandler);

    updateDataMenu();
	}

    public DataViewer(Morpho framework, String sTitle)
    {
        this();
        this.framework = framework;
        config = framework.getConfiguration();
        ConfigXML profile = framework.getProfile();
        String profileDirName = config.getConfigDirectory() + 
                                File.separator +
                                config.get("profile_directory", 0) + 
                                config.get("current_profile", 0);
        datadir = profileDirName + File.separator + profile.get("datadir", 0);
        tempdir = profileDirName + File.separator + profile.get("tempdir", 0);
        cachedir = profileDirName + File.separator + profile.get("cachedir", 0);
        separator = profile.get("separator", 0);
        separator = separator.trim();
    }

    public DataViewer(String sTitle, String dataID, String dataString)
    {
        this();
        this.dataID = dataID;
        this.dataString = dataString;
    }
    
    public DataViewer(Morpho framework, String sTitle, File dataFile)
    {
        this();
		    this.framework = framework;
        config = framework.getConfiguration();
        ConfigXML profile = framework.getProfile();
        String profileDirName = config.getConfigDirectory() + File.separator +
                            config.get("profile_directory", 0) + 
                            File.separator +
                            config.get("current_profile", 0);
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
                                                     framework);  
        if(formatList != null && formatList.getLength() != 0)
        {
          String s = formatList.item(0).getFirstChild().getNodeValue();
          this.format = s;
          Log.debug(30, "format: "+format);
        }
        
        Vector fieldDelimiterPath = new Vector();
        fieldDelimiterPath.addElement("eml-physical/fieldDelimiter");
        NodeList fieldDelimiterList = PackageUtil.getPathContent(physicalFile, 
                                                     fieldDelimiterPath, 
                                                     framework);  
        if(fieldDelimiterList != null && fieldDelimiterList.getLength() != 0)
        {
          String s = fieldDelimiterList.item(0).getFirstChild().getNodeValue();
          this.field_delimiter = s;
        }
        
        Vector numHeaderLinesPath = new Vector();
        numHeaderLinesPath.addElement("eml-physical/numHeaderLines");
        NodeList numHeaderLinesList = PackageUtil.getPathContent(physicalFile, 
                                                     numHeaderLinesPath, 
                                                     framework); 
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
                                                     framework);  
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
                                                     framework);  
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
                                                     framework);  
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
          attributeDoc = PackageUtil.getDoc(attributeFile, framework);
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
                                                     framework);  
       if(attributeNamesList != null && attributeNamesList.getLength() != 0)
        {
          column_labels = new Vector(); 
          String unitString = "";
          String dataTypeString = "";
          for (int i=0;i<attributeNamesList.getLength();i++) {
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
        boolean text_flag = false;
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
          
        }
        else if (text_flag) {
          // try building a table
          if ((column_labels!=null)&&(column_labels.size()>0)) {
            if ((field_delimiter.trim().length()>0)) {
              buildTable();
              MouseListener popupListener = new PopupListener();
              table.addMouseListener(popupListener);
            }
            else if (dataFile==null) {
              numHeaderLines = "0";
              field_delimiter = ",";
              buildTable();
              MouseListener popupListener = new PopupListener();
              table.addMouseListener(popupListener);              
            }
            else {
              buildTextDisplay();
            }
          }
        }
        else {
          Log.debug(9, "Unable to display data!");
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

    public void setPhysicalFile(File phys) {
        this.physicalFile = phys;
    }
    
    public void setParent(EntityGUI egui) {
      this.parent = egui; 
    }
    
    public void setGrandParent(DataPackageGUI dpgui) {
      this.grandParent = dpgui; 
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
                                                     framework);  
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
                                                     framework);  
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
                                                     framework);  
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
      table.clearSelection();
      int selrow = table.rowAtPoint(new Point(e.getX(), e.getY()));
      int selcol = table.columnAtPoint(new Point(e.getX(), e.getY()));
      table.setRowSelectionInterval(selrow, selrow);
      table.setEditingRow(selrow);
      table.setColumnSelectionInterval(selcol, selcol);
      
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
    if (dataFile==null) {
      Log.debug(20, "Null Data File");
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
 //   table.setCellSelectionEnabled(true);
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	  (table.getTableHeader()).setReorderingAllowed(false);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      
    DataScrollPanel.getViewport().removeAll();
    DataScrollPanel.getViewport().add(table);
    
    JTableHeader header = table.getTableHeader();
    header.addMouseListener(new HeaderMouseListener());
  
	}


  /**
   * Event handler for the right click popup menu
   */
  class MenuAction implements java.awt.event.ActionListener 
  {
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == createNewDatatable) {
        
      }
      else if ((object == sortBySelectedColumn)||(object == sortBySelectedColumn1)) {
        int sel = table.getSelectedColumn();
        if (sel>-1) {
          ptm.sort(sel, sortdirection);
          pv = ptm.getPersistentVector();
          sortdirection = -1 * sortdirection;
        }
      }
      else if ((object == insertRowAfter)||(object == insertRowAfter1)) {
        int sel = table.getSelectedRow();
        if (sel>-1) {
          Vector blanks = new Vector();
          blanks.addElement(" \t");
          blanks.addElement(" \t");
          blanks.addElement(" \t");
          ptm.insertRow(sel+1, blanks);	 
        }
      }
      else if ((object == insertRowBefore)||(object == insertRowBefore1)) {
        int sel = table.getSelectedRow();
        if (sel>-1) {
          Vector blanks = new Vector();
          blanks.addElement(" \t");
          blanks.addElement(" \t");
          blanks.addElement(" \t");
          ptm.insertRow(sel, blanks);	 
        }
      }
      else if ((object == deleteRow)||(object == deleteRow1)) {
        int sel = table.getSelectedRow();
        if (sel>-1) {
          ptm.deleteRow(sel);	 
        }
      }
      else if ((object == insertColumnBefore)||(object == insertColumnBefore1)) {
        int sel = table.getSelectedColumn();
        if (sel>-1) {
          showColumnMetadataEditPanel();
          if (columnAddFlag) {
            try {
              cmep.setMorpho(framework);
              cmep.insertNewAttributeAt(sel, attributeDoc);
 //             cmep.save();
            }
            catch (Exception w) {
              Log.debug(20, "Exception trying to modify attribute DOM");
            }
            
            String newHeader = cmep.getColumnName();
            if (newHeader.trim().length()==0) newHeader = "New Column";
            String type = cmep.getDataType();
            String unit = cmep.getUnit();
            newHeader = "<html><font face=\"Courier\"><center><small>"+type+
                                           "<br>"+unit +"<br></small><b>"+
                                           newHeader+"</b></font></center></html>";
            column_labels.insertElementAt(newHeader, sel);
            ptm.insertColumn(sel); 
            pv = ptm.getPersistentVector();
          }
        }
      }
      else if ((object == insertColumnAfter)||(object == insertColumnAfter1)) {
        int sel = table.getSelectedColumn();
        if (sel>-1) {
          showColumnMetadataEditPanel();
          if (columnAddFlag) {
            try {
              cmep.setMorpho(framework);
              cmep.insertNewAttributeAt(sel, attributeDoc);
 //             cmep.save();
            }
            catch (Exception w) {
              Log.debug(20, "Exception trying to modify attribute DOM");
            }

            String newHeader = cmep.getColumnName();
            if (newHeader.trim().length()==0) newHeader = "New Column";
            String type = cmep.getDataType();
            String unit = cmep.getUnit();
            newHeader = "<html><font face=\"Courier\"><center><small>"+type+
                                           "<br>"+unit +"<br></small><b>"+
                                           newHeader+"</b></font></center></html>";
            column_labels.insertElementAt(newHeader, sel+1);
            ptm.insertColumn(sel+1); 
            pv = ptm.getPersistentVector();
          }
        }        
      }
      else if ((object == deleteColumn)||(object == deleteColumn1)) {
        int sel = table.getSelectedColumn();
        if (sel>-1) {
          // remove the attribute node associated with the column
          NodeList nl = attributeDoc.getElementsByTagName("attribute");
          Node deleteNode = nl.item(sel);
          Node root = attributeDoc.getDocumentElement();
          root.removeChild(deleteNode);
          
          column_labels.removeElementAt(sel);
          ptm.deleteColumn(sel);
          pv = ptm.getPersistentVector();
          // need to delete column from attributeDOM here !!!
        }
      }
      else if ((object == editColumnMetadata)||(object == editColumnMetadata1)) {
        
      }

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
      JMenu menu = mb.getMenu(3); // the 'Data' menu
      menu.removeAll();
      menu.add(createNewDatatable1);
      menu.add(new JSeparator());
      menu.add(sortBySelectedColumn1);
      menu.add(new JSeparator());
      menu.add(insertRowAfter1);
      menu.add(insertRowBefore1);
      menu.add(deleteRow1);
      menu.add(new JSeparator());
      menu.add(insertColumnAfter1);
      menu.add(insertColumnBefore1);
      menu.add(deleteColumn1);
      menu.add(new JSeparator());
      menu.add(editColumnMetadata1);
    }    
  }
  
  private void showColumnMetadataEditPanel() {
    MorphoFrame mf = UIController.getInstance().getCurrentActiveWindow();
    columnDialog = new JDialog(mf,true);
    columnDialog.getContentPane().setLayout(new BorderLayout(0,0));
    columnDialog.setSize(400,650);
    cmep = new ColumnMetadataEditPanel();
    columnDialog.getContentPane().add(BorderLayout.CENTER, cmep);
    controlPanel = new JPanel();
    controlCancel = new JButton("Cancel");
    controlOK= new JButton("OK");
    controlPanel.add(controlCancel);
    controlPanel.add(controlOK);
    columnDialog.getContentPane().add(BorderLayout.SOUTH, controlPanel);
    ColAction cAction = new ColAction();
		controlOK.addActionListener(cAction);
    controlCancel.addActionListener(cAction);

    columnDialog.setVisible(true);
  }
  
	class ColAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == controlOK) {
        columnAddFlag = true;
				columnDialog.dispose();
      }
      else if (object == controlCancel) {
        columnAddFlag = false;
				columnDialog.dispose();
      }
		}
	}

	void UpdateButton_actionPerformed(java.awt.event.ActionEvent event)
	{ 
    
	  if (dp!=null) {
      // make a temporary copy of the data file
      ptm.getPersistentVector().writeObjects(tempdir + "/" + "tempdata");
      File newDataFile = new File(tempdir + "/" + "tempdata");
      long newDataFileLength = newDataFile.length();
  
      try {
        String attrDocType = "<!DOCTYPE eml-attribute PUBLIC "+
            "\"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "+
            "\"eml-attribute.dtd\">";
        String entityDocType = "<!DOCTYPE eml-entity PUBLIC "+
            "\"-//ecoinformatics.org//eml-entity-2.0.0beta6//EN\" "+
            "\"eml-entity.dtd\">";
        String physicalDocType = "<!DOCTYPE eml-physical PUBLIC "+
            "\"-//ecoinformatics.org//eml-physical-2.0.0beta6//EN\" "+
            "\"eml-physical.dtd\">";
        PackageUtil.saveDOM(tempdir + "/" + "tempattribute", attributeDoc, attrDocType, framework);
        Document doc = PackageUtil.getDoc(entityFile, framework);
        PackageUtil.saveDOM(tempdir + "/" + "tempentity", doc, entityDocType, framework);
        doc = PackageUtil.getDoc(physicalFile, framework);
        PackageUtil.saveDOM(tempdir + "/" + "tempphysical", doc, physicalDocType, framework);
      }
      catch (Exception q) {
        Log.debug(20, "Error trying to save from DOM");
      }
      
      
      
        AccessionNumber a = new AccessionNumber(framework);
        FileSystemDataStore fsds = new FileSystemDataStore(framework);
        //System.out.println(xmlString);
  
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
        
      if(localloc)
      { //save it locally
        try{
          Vector newids = new Vector();
          Vector oldids = new Vector();
          String oldid = dataID;
          newid = a.incRev(dataID);
          // save data to a temporary file
          FileReader fr = null;
          try{
            ptm.getPersistentVector().writeObjects(tempdir + "/" + "tempdata");
            File tempfile = new File(tempdir + "/" + "tempdata");
            fr = new FileReader(tempfile);
          }
          catch (Exception ww) {
            Log.debug(20,"Problem making temporary copy of data");
          }
          
          
          fsds.saveFile(newid, fr);
          newPackageId = a.incRev(dp.getID());
          oldids.addElement(oldid);
          oldids.addElement(dp.getID());
          newids.addElement(newid);
          newids.addElement(newPackageId);
          //increment the package files id in the triples
          String newPackageFile = a.incRevInTriples(dp.getTriplesFile(), 
                                                    oldids, 
                                                    newids);
          fsds.saveFile(newPackageId, new StringReader(newPackageFile)); 

        }
        catch (Exception e) {
          Log.debug(20, "error in local update of data file");    
        }
      }
      if(metacatloc)
      { //save it to metacat
        MetacatDataStore mds = new MetacatDataStore(framework);
        String oldid = dataID;
        newid = a.incRev(dataID);
        Vector parts = a.getParts(newid);
        String newFileName = (String)parts.elementAt(1)+(String)parts.elementAt(3)
                                 +(String)parts.elementAt(2);
  //      newid = a.getNextId();
        System.out.println("oldid: " + oldid + " newid: " + newid);          
        
        try{
          // save to a temporary file
          ptm.getPersistentVector().writeObjects(tempdir + "/" + newFileName);
          File tempfile = new File(tempdir + "/" + newFileName);
          
 /*         StringReader sr = new StringReader(dataString);
          File tempfile = new File(tempdir + "/" + newFileName);
          FileWriter fw = new FileWriter(tempfile);
          BufferedWriter bfw = new BufferedWriter(fw);
          int c = sr.read();
          while(c != -1)
          {
            bfw.write(c); //write out everything in the reader
            c = sr.read();
          }
          bfw.flush();
          bfw.close();
*/          
          mds.newDataFile(newid, tempfile);
          System.out.println("new data file added:newid="+newid);
          newPackageId = a.incRev(dp.getID());
          Vector newids = new Vector();
          Vector oldids = new Vector();
          oldids.addElement(oldid);
          oldids.addElement(dp.getID());
          newids.addElement(newid);
          newids.addElement(newPackageId);
          System.out.println("ready to increment triples");
          
          //increment the package files id in the triples
          String newPackageFile = a.incRevInTriples(dp.getTriplesFile(), 
                                                    oldids, 
                                                    newids);
          System.out.println("oldid: " + oldid + " newid: " + newid);          
          mds.saveFile(newPackageId, new StringReader(newPackageFile), dp); 
        }
        catch (Exception e) {
            Log.debug(20, "error in metacat update of data file"+e.getMessage());    
        }
      }
      DataPackage newPackage = new DataPackage(location, newPackageId, null,
                                                 framework);
      //this.dispose();
  //    if (parent!=null) parent.dispose();
  //    if (grandParent!=null) grandParent.dispose();
  //    DataPackageGUI newgui = new DataPackageGUI(framework, newPackage);

 /*
     // Refresh the query results after the update
      try {
        ServiceProvider provider = 
               framework.getServiceProvider(QueryRefreshInterface.class);
        ((QueryRefreshInterface)provider).refresh();
      } catch (ServiceNotHandledException snhe) {
        Log.debug(6, snhe.getMessage());
      }
      newgui.show();
  */    
	  }		
    
	}

	
  class HeaderMouseListener implements MouseListener {

    /**
     * Mouse click event handler
     */
    public void mouseClicked(MouseEvent event) 
    {
      TableColumnModel colModel = table.getColumnModel();
      int index = colModel.getColumnIndexAtX(event.getX());
      int modelIndex = colModel.getColumn(index).getModelIndex();
      table.setRowSelectionInterval(0, table.getRowCount()-1);
      table.setColumnSelectionInterval(modelIndex, modelIndex);
      javax.swing.JOptionPane.showMessageDialog(null, "Header Clicked - Column# "+modelIndex); 
    }

    public void mouseReleased(MouseEvent event){}
    public void mousePressed(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}    
  }
  
}
