/**
 *  '$RCSfile: DataViewer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-03-07 18:01:24 $'
 * '$Revision: 1.7 $'
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
    String delimiter;
    
    DataPackageGUI grandParent;
    EntityGUI parent;
    

	/**
	 * number of parsed lines in file
	 */
	int nlines; 
	

	/**
	 * array of line strings
	 */
	String[] lines; 

    	/**
	 * vector containing column Title strings
	 */
	// contains column titles
	Vector colTitles;

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
		getContentPane().add(BorderLayout.CENTER,TabbedViewPanel);
		TextPanel.setLayout(new BorderLayout(0,0));
		TabbedViewPanel.add(TextPanel);
		TextPanel.setBounds(2,24,750,421);
		TextPanel.setVisible(false);
		JScrollPane1.setOpaque(true);
		TextPanel.add(BorderLayout.CENTER,JScrollPane1);
		JScrollPane1.getViewport().add(DataTextArea);
		DataTextArea.setBounds(0,0,747,418);
		TablePanel.setLayout(new BorderLayout(0,0));
		TabbedViewPanel.add(TablePanel);
		TablePanel.setBounds(2,24,750,421);
		TablePanel.setVisible(false);
		TablePanel.add(BorderLayout.CENTER,DataScrollPanel);
		TabbedViewPanel.setSelectedIndex(0);
		TabbedViewPanel.setSelectedComponent(TextPanel);
		TabbedViewPanel.setTitleAt(0,"Text View");
		TabbedViewPanel.setTitleAt(1,"Table View");
		ControlPanel.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.SOUTH,ControlPanel);
		JPanel1.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
		ControlPanel.add(BorderLayout.CENTER,JPanel1);
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
		ControlPanel.add(BorderLayout.WEST,JPanel2);
		ImportNewButton.setText("Import New Data...");
		ImportNewButton.setActionCommand("Import New Data...");
		JPanel2.add(ImportNewButton);
		ImportNewButton.setVisible(false);
		//}}

		//{{INIT_MENUS
		//}}
		DataTextArea.setEditable(false);
	
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
    String profileDirName = config.get("profile_directory", 0) + 
                            File.separator +
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
    
    public void setDataPackage(DataPackage dp) {
        this.dp = dp;
    }
    
    public void setDataString(String dataString) {
        this.dataString = dataString;
        JTextArea ta = new JTextArea(dataString);
        ta.setEditable(false);
        JScrollPane1.getViewport().removeAll();
        JScrollPane1.getViewport().add(ta);
	    parseString(dataString);
	    parseDelimited();
        
    }
    
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
	javax.swing.JTabbedPane TabbedViewPanel = new javax.swing.JTabbedPane();
	javax.swing.JPanel TextPanel = new javax.swing.JPanel();
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JTextArea DataTextArea = new javax.swing.JTextArea();
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

	private String getDelimiterString() {
	  String str = "";
      String temp = guessDelimiter();
      if (temp.equals("tab")) str = "\t";
      if (temp.equals("comma")) str = ",";
      if (temp.equals("space")) str = " ";
      if (temp.equals("semicolon")) str = ";";
      delimiter = str;
	  return str;
	}
	
	private void parseDelimited() {
	  if (lines!=null) {
	    int start = startingLine;  // startingLine is 1-based not 0-based
//	    if (labelsInStartingLine) {
      if (true) {
	      colTitles = getColumnValues(lines[startingLine-1]);
	    }
	    else {
	      colTitles = getColumnValues(lines[startingLine-1]);  // use just to get # of cols
	      int temp = colTitles.size();
	      colTitles = new Vector();
	      for (int l=0;l<temp;l++) {
	        colTitles.addElement("Column "+(l+1));  
	      }
	      start--;  // include first line
	    }
	    vec = new Vector();
	    Vector vec1 = new Vector();
	    int numcols = colTitles.size();
	    for (int i=start;i<nlines;i++) {
	      vec1 = getColumnValues(lines[i]);
	      boolean missing = false;
	      while (vec1.size()<numcols) {
	        vec1.addElement("");
	        missing = true;
	      }
	      vec.addElement(vec1);
	    }
	  
	    buildTable();
    }
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

  /* returns the number of occurances of a substring in specified input string 
   * inS is input string
   * subS is substring
  */
  private int charCount(String inS, String subS ) {
    int cnt = -1;
    int pos = 0;
    int pos1 = 0;
    while (pos > -1) {
      pos1=inS.indexOf(subS, pos+1);
      pos = pos1;
      cnt++; 
    }
    if (cnt<0) cnt = 0;
    return cnt;
  }


   /**
    * return most frequent number of occurances of indicated substring
    * 
    * @param subS delimiter substring
    */
   private int mostFrequent(String subS) {
    int maxcnt = 500; // arbitrary limit of 500 occurances
    int[] freq = new int[maxcnt];  
      for (int i=0;i<nlines;i++) {
        int cnt = charCount(lines[i],subS);
        if (cnt>maxcnt-1) cnt = maxcnt-1;
        freq[cnt]++;
      }
      int mostfreq = 0;
      int mostfreqindex = 0;
      int tot = 0;
      for (int j=0;j<maxcnt;j++) {
        tot = tot + freq[j];
        if (freq[j]>mostfreq) {
          mostfreq = freq[j];
          mostfreqindex = j;
        }
      }
      // establish a threshold; if less than, then return 0
      if ( (100*mostfreq/tot)<80) mostfreq = 0;
      return mostfreqindex;
   }

  /**
   * guesses a delimiter based on frequency of appearance of common delimites
   */
  private String guessDelimiter() {
    if (mostFrequent("\t")>0) {
      return "tab";
    }
    else if (mostFrequent(",")>0) {
      return "comma";
    }
    else if (mostFrequent(" ")>0) {
      return "space";
    }
    else if (mostFrequent(";")>0) {
      return "semicolon";
    }
    else if (mostFrequent(":")>0) {
      return "colon";
    }
    return "unknown";
  }

	/**
	 * builds JTable from input data ans includes event code for handling clicks on
	 * table (e.g. column selection)
	 * 
	 * @param cTitles
	 * @param data
	 */
	private void buildTable() {
	  
      final JTable table = new JTable(vec, colTitles);
      
      table.setColumnSelectionAllowed(true);
      table.setRowSelectionAllowed(false);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      
      ListSelectionModel colSM = table.getColumnModel().getSelectionModel();
          colSM.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                  //Ignore extra messages.
                  if (e.getValueIsAdjusting()) return;
                  
                  ListSelectionModel lsm =
                      (ListSelectionModel)e.getSource();
           /*       if (lsm.isSelectionEmpty()) {
                      //no columns are selected
                  } else {
                      selectedCol = lsm.getMinSelectionIndex();
                      //selectedCol is selected
                      ColumnData cd = (ColumnData)colDataInfo.elementAt(selectedCol);
                      EnumCheckBox.setSelected(cd.useEnumerationList);
                      int numUnique =  cd.colNumUniqueItems;
                      String str = "There are "+numUnique+" unique item(s) in this column";
                      NumUniqueLabel.setText(str);
                      if (cd.colType.equals("Floating Point")) {
                        String dmin = Double.toString(cd.colMin);
                        String dmax = Double.toString(cd.colMax);
                        String daver = Double.toString(cd.colAverage);
                        MinMaxLabel.setText("Min:"+ dmin +"  Max:" + dmax + "  Aver:" +daver);
                        MinTextField.setText(dmin);
                        MaxTextField.setText(dmax);
                      } 
                      else if ((cd.colType.equals("Integers"))) {
                        String min = Integer.toString((int)cd.colMin);
                        String max = Integer.toString((int)cd.colMax);
                        String aver = Double.toString(cd.colAverage);
                        MinMaxLabel.setText("Min:"+ min +"  Max:" + max + "  Aver:" +aver);   
                        MinTextField.setText(min);
                        MaxTextField.setText(max);
                     }
                      else {
                        MinMaxLabel.setText("");
                        MinTextField.setText("");
                        MaxTextField.setText("");
                      }
                      String[] headers = new String[2];
                      headers[0] = "Code";
                      headers[1] = "Definition";
                      DefaultTableModel dtm = new DefaultTableModel(headers,0);
                      String[] row = new String[2];
                      for (int j=0;j<cd.colUniqueItemsList.size();j++) {
                        row[0] = (String)cd.colUniqueItemsList.elementAt(j);
                        row[1] = (String)cd.colUniqueItemsDefs.elementAt(j);
                        dtm.addRow(row);
                      }
                      for (int i=0;i<20;i++) {
                        row[0] = "";
                        row[1] = "";
                        dtm.addRow(row);
                      }
                      
            //          JList uniq = new JList(cd.colUniqueItemsList);
            //          JTable uniq = new JTable(dtm);
                      uniq.setModel(dtm);
                      UniqueItemsScroll.getViewport().removeAll();
                      UniqueItemsScroll.getViewport().add(uniq);
                      ColumnUnitTextField.setText(cd.colUnits);
                      ColumnNameTextField.setText(cd.colName);
                      ColumnLabelTextField.setText(cd.colTitle);
                      String colType = cd.colType;
                      DataTypeList.setSelectedValue(colType, true);
                      ColumnDefTextArea.setText(cd.colDefinition);
                     
                  }
          */        
              }
          });
      
      DataScrollPanel.getViewport().removeAll();
      DataScrollPanel.getViewport().add(table);
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
	  StringBuffer coltitles = new StringBuffer();
	  StringBuffer resultString = new StringBuffer();
	  for (int k=0;k<colTitles.size();k++){
	      coltitles.append((String)colTitles.elementAt(k)+delimiter);
	  }
	  resultString.append(coltitles.toString()+"\n");
	  for (int i=0;i<nlines-1;i++) {
	    StringBuffer lineString = new StringBuffer();
	    innerVec = (Vector)vec.elementAt(i);
	    for (int j=0;j<innerVec.size();j++) {
	      lineString.append((String)innerVec.elementAt(j)+delimiter);
	    }
	    resultString.append(lineString.toString()+"\n");
	  }
	  dataString = resultString.toString();
	}

	void UpdateButton_actionPerformed(java.awt.event.ActionEvent event)
	{ 
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