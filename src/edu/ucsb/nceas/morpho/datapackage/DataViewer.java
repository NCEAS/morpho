/**
 *  '$RCSfile: DataViewer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-08-09 21:27:07 $'
 * '$Revision: 1.17.2.5 $'
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
import javax.swing.event.*;
import javax.swing.table.*;
import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Date;
import java.util.Enumeration;
import java.awt.event.*;

import org.apache.xalan.xpath.xml.FormatterToXML;
import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import edu.ucsb.nceas.morpho.framework.*;

//public class DataViewer extends javax.swing.JFrame
public class DataViewer extends javax.swing.JPanel
{
	public JPanel DataViewerPanel = new javax.swing.JPanel();
	  JPanel TablePanel = new javax.swing.JPanel();
	  JScrollPane DataScrollPanel = new javax.swing.JScrollPane();
	  JPanel ControlPanel = new javax.swing.JPanel();
	  JPanel ButtonControlPanel = new javax.swing.JPanel();
	  JLabel DataIDLabel = new javax.swing.JLabel();
	  JButton CancelButton = new javax.swing.JButton();
	  JButton UpdateButton = new javax.swing.JButton();

    PersistentVector pv;
    PersistentTableModel ptm;
    JTable table;
    
    int sortdirection = 1;
  
    ClientFramework framework;
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



	public DataViewer()
	{
		setLayout(new BorderLayout(0,0));
    setSize(755,483);
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

	}

    public DataViewer(ClientFramework framework, String sTitle)
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
    
    public DataViewer(ClientFramework framework, String sTitle, File dataFile)
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
      boolean missing_metadata_flag = false;
      if (physicalFile==null) {
          framework.debug(15, "Physical information about the data is missing!");
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
          framework.debug(30, "format: "+format);
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
          framework.debug(15, "Entity information about the data is missing!");
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
          
      }
      if (attributeFile==null) {
          framework.debug(15, "Attribute information about the data is missing!");
          missing_metadata_flag = true;
      } else {
        // get attribute labels and build column headers
        Vector attributeLabelsPath = new Vector();
        attributeLabelsPath.addElement("eml-attribute/attribute/attributeLabel");
        NodeList attributeLabelsList = PackageUtil.getPathContent(attributeFile, 
                                                     attributeLabelsPath, 
                                                     framework);  
        if(attributeLabelsList != null && attributeLabelsList.getLength() != 0)
        {
          column_labels = new Vector(); 
          for (int i=0;i<attributeLabelsList.getLength();i++) {
            String temp = attributeLabelsList.item(i).getFirstChild().getNodeValue();
            column_labels.addElement(temp); 
          }
        }
      }
      // now examine format info and see if we want to simply display a text
      // file, create a table, or display an image
      if (missing_metadata_flag) {
        // try displaying as text since don't know what else to do 
        
        // add text display here!!!
        framework.debug(30, "attempting to display as text");
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
          framework.debug(30, "trying to display image! "+filename);
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
            }
            else {
              buildTextDisplay();
            }
            MouseListener popupListener = new PopupListener();
            table.addMouseListener(popupListener);
          }
        }
        else {
          framework.debug(9, "Unable to display data!");
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
    pv.init(dataFile, num_header_lines);
    ptm = new PersistentTableModel(pv, column_labels);
    ptm.setFieldDelimiter(field_delimiter);
    table.setModel(ptm);
    
    table.setColumnSelectionAllowed(true);
    table.setRowSelectionAllowed(true);
 //   table.setCellSelectionEnabled(true);
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	  (table.getTableHeader()).setReorderingAllowed(false);
      
    DataScrollPanel.getViewport().removeAll();
    DataScrollPanel.getViewport().add(table);
    
  
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
      else if (object == sortBySelectedColumn) {
        int sel = table.getSelectedColumn();
        if (sel>-1) {
          ptm.sort(sel, sortdirection);
          pv = ptm.getPersistentVector();
          sortdirection = -1 * sortdirection;
        }
      }
      else if (object == insertRowAfter) {
        int sel = table.getSelectedRow();
        if (sel>-1) {
          Vector blanks = new Vector();
          blanks.addElement(" \t");
          blanks.addElement(" \t");
          blanks.addElement(" \t");
          ptm.insertRow(sel+1, blanks);	 
        }
      }
      else if (object == insertRowBefore) {
        int sel = table.getSelectedRow();
        if (sel>-1) {
          Vector blanks = new Vector();
          blanks.addElement(" \t");
          blanks.addElement(" \t");
          blanks.addElement(" \t");
          ptm.insertRow(sel, blanks);	 
        }
      }
      else if (object == deleteRow) {
        int sel = table.getSelectedRow();
        if (sel>-1) {
          ptm.deleteRow(sel);	 
        }
      }
      else if (object == insertColumnBefore) {
        int sel = table.getSelectedColumn();
        if (sel>-1) {
          column_labels.insertElementAt("New Column", sel);
          ptm.insertColumn(sel); 
          pv = ptm.getPersistentVector();
        }
      }
      else if (object == insertColumnAfter) {
        int sel = table.getSelectedColumn();
        if (sel>-1) {
          column_labels.insertElementAt("New Column", sel+1);
          ptm.insertColumn(sel+1); 
          pv = ptm.getPersistentVector();
        }        
      }
      else if (object == deleteColumn) {
        int sel = table.getSelectedColumn();
        if (sel>-1) {
          column_labels.removeElementAt(sel);
          ptm.deleteColumn(sel);
          pv = ptm.getPersistentVector();
        }
      }
      else if (object == editColumnMetadata) {
        
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

	void UpdateButton_actionPerformed(java.awt.event.ActionEvent event)
	{ 
    /*
	  if(nlines>=nlines_max) {
	    framework.debug(9,"Sorry, this data file is too large to be updated from within Morpho!");
	    return;
	  }
	  if (dp!=null) {
	      // convert table info to string
	      vecToString();
	    
        framework.debug(20, "beginning of data file update");
        AccessionNumber a = new AccessionNumber(framework);
        FileSystemDataStore fsds = new FileSystemDataStore(framework);
        //System.out.println(xmlString);
  
        boolean metacatloc = false;
        boolean localloc = false;
        boolean bothloc = false;
        String newid = "";
        String location = dp.getLocation();
        String newPackageId = "";
        if(location.equals(DataPackage.BOTH))
        {
            metacatloc = true;
            localloc = true;
        }
        else if(location.equals(DataPackage.METACAT))
        {
            metacatloc = true;
        }
        else if(location.equals(DataPackage.LOCAL))
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
//          System.out.println("newid= "+newid);
          fsds.saveFile(newid, new StringReader(dataString));
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
        catch (Exception e) {
            framework.debug(20, "error in local update of data file");    
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
          StringReader sr = new StringReader(dataString);
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
            framework.debug(20, "error in metacat update of data file"+e.getMessage());    
        }
      }
      DataPackage newPackage = new DataPackage(location, newPackageId, null,
                                                 framework);
      //this.dispose();
  //    if (parent!=null) parent.dispose();
  //    if (grandParent!=null) grandParent.dispose();
      DataPackageGUI newgui = new DataPackageGUI(framework, newPackage);

      // Refresh the query results after the update
      try {
        ServiceProvider provider = 
               framework.getServiceProvider(QueryRefreshInterface.class);
        ((QueryRefreshInterface)provider).refresh();
      } catch (ServiceNotHandledException snhe) {
        framework.debug(6, snhe.getMessage());
      }
      newgui.show();
	  }		
  */  
	}

	
}
