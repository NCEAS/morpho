/**
 *  '$RCSfile: DataViewer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-06 21:10:39 $'
 * '$Revision: 1.18 $'
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

import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;

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

public class DataViewer extends javax.swing.JFrame
{
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

  // starting line
  int startingLine = 1;

	public DataViewer()
	{
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(755,483);
		setVisible(false);
		TablePanel.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER,TablePanel);
		TablePanel.add(BorderLayout.CENTER, DataScrollPanel);
		ControlPanel.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.SOUTH, ControlPanel);
		JPanel1.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
		ControlPanel.add(BorderLayout.CENTER, JPanel1);
		DataIDLabel.setText("Data ID: ");
		DataIDLabel.setNextFocusableComponent(CancelButton);
		JPanel1.add(DataIDLabel);
		CancelButton.setText("Cancel");
		CancelButton.setActionCommand("Cancel");
		JPanel1.add(CancelButton);
		UpdateButton.setText("Update");
		UpdateButton.setActionCommand("Update");
		JPanel1.add(UpdateButton);
		JPanel2.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		ControlPanel.add(BorderLayout.WEST, JPanel2);
		ImportNewButton.setText("Import New Data...");
		ImportNewButton.setActionCommand("Import New Data...");
		JPanel2.add(ImportNewButton);
		ImportNewButton.setVisible(false);
		//}}

		//{{INIT_MENUS
		//}}
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		CancelButton.addActionListener(lSymAction);
		UpdateButton.addActionListener(lSymAction);
		//}}
		
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
        setTitle(sTitle);
    }

    public DataViewer(String sTitle, String dataID, String dataString)
    {
        this();
        setTitle(sTitle);
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
        setTitle(sTitle);
        this.dataFile = dataFile;
    }
    
    
    public void init() {
      boolean missing_metadata_flag = false;
      if (physicalFile==null) {
          framework.debug(9, "Physical information about the data is missing!");
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
          framework.debug(9, "Entity information about the data is missing!");
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
          framework.debug(9, "Attribute information about the data is missing!");
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
    
 /*   public void setDataString(String dataString) {
        this.dataString = dataString;
        JTextArea ta = new JTextArea(dataString);
        ta.setEditable(false);
        JScrollPane1.getViewport().removeAll();
        JScrollPane1.getViewport().add(ta);
        if (dataFile!=null) {
            parseFile();
        } else {
	        parseString(dataString);
        }
//	    parseDelimited();
        
    }
*/    
    public void setParent(EntityGUI egui) {
      this.parent = egui; 
    }
    public void setGrandParent(DataPackageGUI dpgui) {
      this.grandParent = dpgui; 
    }
    
    
    public void setDataID(String dataID) {
        this.dataID = dataID;
        setTitle("DataFile: "+dataID);
        DataIDLabel.setText("DataFile: "+dataID);
    }

	static public void main(String args[])
	{
		(new DataViewer()).setVisible(true);
	}

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets and menu bar
		Insets insets = getInsets();
		javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
		int menuBarHeight = 0;
		if (menuBar != null)
			menuBarHeight = menuBar.getPreferredSize().height;
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JPanel TablePanel = new javax.swing.JPanel();
	javax.swing.JScrollPane DataScrollPanel = new javax.swing.JScrollPane();
	javax.swing.JPanel ControlPanel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JLabel DataIDLabel = new javax.swing.JLabel();
	javax.swing.JButton CancelButton = new javax.swing.JButton();
	javax.swing.JButton UpdateButton = new javax.swing.JButton();
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JButton ImportNewButton = new javax.swing.JButton();
	//}}

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
     * @param s input string
     */

    private void parseString (String s) {
        int i;
        int pos;
        String temp, temp1;
          BufferedReader in = new BufferedReader(new StringReader(s));
          nlines = 0;
          try {
            while ((temp = in.readLine())!=null) {
                if (temp.length()>0) {   // do not count blank lines
                nlines++;} 
            }
            in.close();
          }
        catch (Exception e) {};
        
        lines = new String[nlines];
          // now read again since we know how many lines
          BufferedReader in1 = new BufferedReader(new StringReader(s));
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


    /**
     * parses data input string into an array of lines (Strings)
     * 
     * @param s input file
     */

    public void parseFile () {
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
	  vec = new Vector();
      final JTable table = new JTable();
      DefaultTableModel model = new DefaultTableModel(vec, column_labels);
      table.setModel(model);
      table.setColumnSelectionAllowed(true);
      table.setRowSelectionAllowed(false);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  (table.getTableHeader()).setReorderingAllowed(false);
      
      
      ListSelectionModel colSM = table.getColumnModel().getSelectionModel();
          colSM.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                  //Ignore extra messages.
                  if (e.getValueIsAdjusting()) return;
                  
                  ListSelectionModel lsm =
                      (ListSelectionModel)e.getSource();
              }
          });
      
      DataScrollPanel.getViewport().removeAll();
      DataScrollPanel.getViewport().add(table);
      parseFile();
      num_header_lines = 0;
      Integer temp = new Integer(numHeaderLines);
      if (temp!=null) {
        num_header_lines = temp.intValue();  
      }
      for (int i=num_header_lines;i<nlines;i++) {
        Vector rowvals = getColumnValues(lines[i]); 
        model.addRow(rowvals);
      }
	}


	//{{DECLARE_MENUS
	//}}

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
		this.hide();
		this.dispose();
			 
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
      this.dispose();
      if (parent!=null) parent.dispose();
      if (grandParent!=null) grandParent.dispose();
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
	}

	
}
