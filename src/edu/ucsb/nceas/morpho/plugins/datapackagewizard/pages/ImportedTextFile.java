/**
 *  '$RCSfile: CitationPage.java,v $'
 *    Purpose: A class that handles display of Citation Information
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 23:06:55 $'
 * '$Revision: 1.29 $'
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

import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.TextImportListener;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.UneditableTableModel;
import edu.ucsb.nceas.morpho.util.Log;

import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;




/**
 * This represents a text data file which will be imported. Data will be stored in a JTable. 
 * Some medata will be stored too.
 * @author tao
 *
 */
public class ImportedTextFile 
{
	
	  /**
	   * 	a global reference to the table used to display the lines read from the file
	   *	this is the table that is displayed on the first screen of the TIW (Not delimitered)
	   */
	  private JTable linesTable = null;

	  /**
	   * actual number of lines in fdata file
	   */
	  private int nlines_actual;

	  /**
	   * number of parsed lines in file
	   */
	  private int nlines;

	  /**
	   * max number of lines to be parsed in file
	   */
	  private int nlines_max = 5000;
	  
	  /*
	   * guessed delimiter base on parsing the text file
	   */
	  private String guessedDelimiter = " ";
	  
	  /*
	   * indication if  column label is in the starting line
	   */
	  private boolean columnLabelsInStartingLine = false;
	  
	  
	  /*
	   * The number of data starting line
	   */
	  private int dataStartingLineNumber = 1;
	  
	  /**
	   * vector containing column Title strings
	   */
	  // contains column titles
	  private Vector colTitles = new Vector();

	  /**
	   * vector containing AttributePage objects
	   */
	  private Vector columnAttributes;

		/**
	   * vector containing Orderedmaps of the AttributePage objects
	   */
	  private Vector columnMaps;

	  private boolean[] needToSetPageData;

	  /**
	   * vector of vectors with table data
	   */
	  private Vector vec = new Vector();


	  // Column Model of the table containting all the columns
	  private TableColumnModel fullColumnModel = null;

	  
	  //represents the unknow delimiter
	  public static final String  UNKNOWN = "unknown";
	  
	 
	  /**
	   * a global reference to the table used to display the data that is
	   * being referenced by this text import process
	   */
	  private JTable table;

	  // table model for JTable table (containing the delimitered data)
	  UneditableTableModel tableModel = new UneditableTableModel(vec, colTitles);
	  
	  
	  
	  /**
	   * Constructor
	   * @param dataFile
	   */
	  public ImportedTextFile(File dataFile)
	  {
		  this.dataFile = dataFile;
	  }

	  /**
	   * array of line strings
	   */
	  private String[] lines;

	  private File dataFile;

	  private String shortFileName;

	public JTable getTable() 
	{
		return table;
	}


	public JTable getLinesTable() {
		return linesTable;
	}


	public int getNlines_actual() 
	{
		return nlines_actual;
	}

	public void setNlines_actual(int nlines_actual) 
	{
		this.nlines_actual = nlines_actual;
	}

	public int getNlines() 
	{
		return nlines;
	}

	public void setNlines(int nlines) 
	{
		this.nlines = nlines;
	}

	public String[] getLines() 
	{
		return lines;
	}

	public void setLines(String[] lines) 
	{
		this.lines = lines;
	}

	public File getDataFile() {
		return dataFile;
	}

	/**
	 * Gets the short file name of the data file.
	 * null will be return if the data file is null
	 * @return
	 */
	public String getShortFilename() 
	{
		if(dataFile != null)
		{
			shortFileName = dataFile.getName();
		}
		return shortFileName;
	}


	/**
	 * Gets the guessed delimiter
	 * @return
	 */
	public String getGuessedDelimiter() 
	{
		return guessedDelimiter;
	}

	/**
	 * Set the guessed delimiter
	 * @param guessedDelimiter
	 */
	public void setGuessedDelimiter(String guessedDelimiter) 
	{
		this.guessedDelimiter = guessedDelimiter;
	}
	
	/**
	 * Indicates if the column label is in starting line
	 * @return
	 */
	public boolean isColumnLabelsInStartingLine() 
	{
		return columnLabelsInStartingLine;
	}

	/**
	 * Sets if the column label is in the starting line
	 * @param columnLabelsInStartingLine
	 */
	public void setColumnLabelsInStartingLine(boolean columnLabelsInStartingLine) 
	{
		this.columnLabelsInStartingLine = columnLabelsInStartingLine;
	}
	
	/**
	 * Gets the number of data starting line
	 * @return
	 */
	public int getDataStartingLineNumber() 
	{
		return dataStartingLineNumber;
	}
    
	/**
	 * Sets the number of data starting line
	 * @param dataStartingLineNumber
	 */
	public void setDataStartingLineNumber(int dataStartingLineNumber) 
	{
		if(dataStartingLineNumber <1)
		{
			dataStartingLineNumber = 1;
		}
		this.dataStartingLineNumber = dataStartingLineNumber;
	}
	
	
	/**
	 * Gets full columnModel for the JTable
	 * @return
	 */
	public TableColumnModel getFullColumnModel() 
	{
		return fullColumnModel;
	}
	
	/**
	 * Add a Table model change listener to the data model of jtable
	 * @param listener
	 */
	public void addJTableModelChangeListener(TableModelListener listener)
	{
		if(tableModel != null)
		{
			Log.debug(30, "Add a TableMdoleListner to table in ImportedTExtFile");
			tableModel.addTableModelListener(listener);
		}
		else
		{
			Log.debug(30, "TableModel object is null, and we couldn't add a TableMdoleListner to table in ImportedTExtFile");
		}
	}


	
	
	/**
	 * Create a Table which only displays the data lines by lines (not delimitered) 
	 * We should call parse() method before calling this method.
	 */
	public void createLinesTable() {

		    if(linesTable == null) {

		      Vector listOfRows = new Vector();
		      for (int i = 0; i < nlines; i++) {
		        Vector row = new Vector();
		        row.add(new String().valueOf(i + 1));
		        row.add(lines[i]);
		        listOfRows.add(row);
		      }
		      Vector title = new Vector();
		      title.add("#");
		      title.add("Lines in " + dataFile.getName());
		      UneditableTableModel linesTM = new UneditableTableModel(listOfRows, title);
		      linesTable = new JTable(linesTM);
		      linesTable.setFont(new Font("MonoSpaced", Font.PLAIN, 14));
		      (linesTable.getTableHeader()).setReorderingAllowed(false);

		      TableColumn column = null;
		      column = linesTable.getColumnModel().getColumn(0);
		      column.setPreferredWidth(40);
		      column.setMaxWidth(40);
		    }
		    //DataScrollPanel.getViewport().removeAll();
		    //DataScrollPanel.getViewport().add(linesTable);

		  }

	
	 /**
	   * parses data input file into an array of lines (Strings)
	   *
	   * @param f the file name
	   * @return boolean true if parse was successful (textfile only); false if
	   *   parse unsuccessful (non-text file)
	   */
	  public void parsefile() {

	    String temp = null;

	    //if (isTextFile(dataFile)) {

	      BufferedReader in = null;
	      try {
	        in = new BufferedReader(new FileReader(dataFile));
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	      nlines = 0;
	      nlines_actual = 0;

	      List linesList = new ArrayList();
	      try {
	        while ((temp = in.readLine()) != null) {
	          // do not count blank lines
	          if (temp.length() > 0) {
	            nlines_actual++;
	            if (nlines < nlines_max) {
	              nlines++;
	              temp+="\n";
	              linesList.add(temp);
	            } /*else {
	              // we can stop reading the remaining lines. we dont need the actual number of
	              // lines present
	              break;
	            }*/
	          }
	        }
	      } catch (IOException e) {
	        e.printStackTrace();
	      } finally {
	        try { in.close(); } catch (IOException e) { /* ignore */ }
	      }

	      if (nlines_actual > nlines_max) {
	    	  JOptionPane.showMessageDialog(null,
	          "Data File parsing has been truncated due to large size! (Note: NO data has been lost!)",
	          "Message",
	          JOptionPane.INFORMATION_MESSAGE, null);
	      }
	      //convert list to the "lines" array:
	      lines = (String[])(linesList.toArray(new String[nlines]));
	      guessedDelimiter = guessDelimiter();

	      //return true;

	    /*} else {

	      //CancelButton_actionPerformed(null);

	      return false;
	    }*/
	  }
	  
	  /**
	   * guesses a delimiter based on frequency of appearance of common delimites
	   *
	   * @return String
	   */
	  private String guessDelimiter() {
	    //parseOn = false;
	    //TabCheckBox.setSelected(false);
	    //CommaCheckBox.setSelected(false);
	    //SpaceCheckBox.setSelected(false);
	    //SemicolonCheckBox.setSelected(false);
	    //OtherCheckBox.setSelected(false);
	    if (mostFrequent("\t") > 0) {
	      //TabCheckBox.setSelected(true);
	      //parseOn = true;
	      return TextImportDelimiters.TAB;
	    } else if (mostFrequent(",") > 0) {
	      //CommaCheckBox.setSelected(true);
	      //parseOn = true;
	      return TextImportDelimiters.COMMA;
	    } else if (mostFrequent(" ") > 0) {
	      //SpaceCheckBox.setSelected(true);
	     // parseOn = true;
	      return TextImportDelimiters.SPACE;
	    } else if (mostFrequent(";") > 0) {
	      //SemicolonCheckBox.setSelected(true);
	      //parseOn = true;
	      return TextImportDelimiters.SEMICOLON;
	    } else if (mostFrequent(":") > 0) {
	      //SpaceCheckBox.setSelected(true);
	      //OtherCheckBox.setSelected(true);
	      //OtherTextField.setText(":");
	      //parseOn = true;
	      return TextImportDelimiters.COLON;
	    } else {
	      //SpaceCheckBox.setSelected(true);
	      //parseOn = true;
	      return UNKNOWN;
	    }
	  }
      
	  /**
	   * return most frequent number of occurances of indicated substring
	   *
	   * @param subS delimiter substring
	   * @return int
	   */
	  private int mostFrequent(String subS) {
	    int maxcnt = 500; // arbitrary limit of 500 occurances
	    int[] freq = new int[maxcnt];
	    int mostfreq = 0;
	    int mostfreqindex = 0;

	    for (int i = 0; i < nlines; i++) {
	      int cnt = charCount(lines[i], subS);
	      if (cnt > maxcnt - 1)cnt = maxcnt - 1;
	      freq[cnt]++;
	      if(freq[cnt] > mostfreq) {
	        mostfreq = freq[cnt];
	        mostfreqindex = cnt;
	      }
	    }

	    int tot = nlines;

	    /*for (int j = 0; j < maxcnt; j++) {
	      tot = tot + freq[j];
	      if (freq[j] > mostfreq) {
	        mostfreq = freq[j];
	        mostfreqindex = j;
	      }
	    }*/
	    // establish a threshold; if less than, then return 0
	    if ((100 * mostfreq / tot) < 80) {
	      mostfreqindex = 0;
	    }

	    return mostfreqindex;
	  }
	  
	  /* returns the number of occurances of a substring in specified input string
	   * inS is input string
	   * subS is substring
	   */
	  private int charCount(String inS, String subS) {
	    int cnt = -1;
	    int pos = 0;
	    int pos1 = 0;
	    while (pos > -1) {
	      pos1 = inS.indexOf(subS, pos + 1);
	      pos = pos1;
	      cnt++;
	    }
	    if (cnt < 0)cnt = 0;
	    return cnt;
	  }
	  
	  /**
	   * attempts to check to see if a file is just text or is binary. Reads bytes
	   * in file and looks for '0'. If any '0's are found, assumed that the file is
	   * NOT a text file.
	   *
	   * @param file the File to be checked
	   * @return boolean true if it's a text file, false if not
	   */
	  public boolean isTextFile() {
		 if (dataFile == null)
		 {
			 return false;
		 }
	     boolean text = true;
	     int res;
	     int cnt = 0;
	     int maxcnt = 2000; // only check this many bytes to avoid performance problems
	     FileInputStream in = null;
	     try {
	       in = new FileInputStream(dataFile);
	       while (((res = in.read()) > -1) && (cnt < maxcnt)) {
	         cnt++;
	         if (res == 0) {
	           text = false;
	           break;
	         }
	       }
	       in.close();
	     } catch (Exception e) { 
	    	 e.printStackTrace(); 
	    	 text= false;
	      }
	     finally {
	       try { in.close(); }
	       catch (IOException e) {}
	       
	     }
	     return text;
	   }

	  
	  /**
	   * Compares if two ImportedTextFile object are equal.
	   * 1. If they are some object, they are equal.
	   * 2. If they contains some File Object, they are equal
	   * 3. If their data files have same file path, they are equal
	   * @param secondObj
	   * @return
	   */
	  public boolean equals(ImportedTextFile secondObj)
	  {
		  boolean equal = false;
		  if(secondObj == null)
		  {
			  Log.debug(35, "in second file is null branch in ImprtedTExtFile.equal method");
			  if(this == null)
			  {
				  equal = true;
			  }
		  }
		  else
		  {
			  Log.debug(35, "in second file is NOT null branch in ImprtedTExtFile.equal method");
			  if(this == null)
			  {
				  equal = true;
			  }
			  else
			  {
				  if(this == secondObj)
				  {
					  Log.debug(35, "They are same object in ImprtedTExtFile.equal method");
					  equal = true;
				  }
				  else
				  {
					  File thisFile = this.getDataFile();
					  File secondFile = secondObj.getDataFile();
					  if(thisFile == secondFile)
					  {
						  Log.debug(35, "They are same File object in ImprtedTExtFile.equal method");
						  equal = true;
					  }
					  else if (thisFile != null && secondFile != null && thisFile.getAbsolutePath() != null&&
							     secondFile.getAbsolutePath() != null && thisFile.getAbsolutePath().equals(secondFile.getAbsolutePath()))
					  {
						  Log.debug(35, "They have same file path string in ImprtedTExtFile.equal method");
						  equal = true;
					  }
				  }
			  }
		  }
		  Log.debug(35, "the return value of ImprtedTExtFile.equal method is "+equal);
		  return equal;
	  }

	
	 
	  /**
	   * Parse the data file into a JTable with the given delimiter 
	   * @param ignoreConsequtiveDelimiters
	   * @param sDelim
	   */
	  public void parseDelimited(boolean ignoreConsequtiveDelimiters, String sDelim) {

		    if (lines != null) {
		      int start = dataStartingLineNumber; // startingLine is 1-based not 0-based
		      int numcols = 0; // init
		      /*if (hasReturnedFromScreen2 && isScreen1Unchanged() && colTitles != null) {
		        //don't redefine column headings etc - keep user's previous values,
		        // since nothing has changed. In this case colTitles is already set:
		        numcols = colTitles.size();
		      } else {*/
		        if (columnLabelsInStartingLine) {
		          colTitles = getColumnValues(lines[dataStartingLineNumber - 1],  ignoreConsequtiveDelimiters, sDelim);
		        } else {
		          colTitles = getColumnValues(lines[dataStartingLineNumber - 1], ignoreConsequtiveDelimiters, sDelim); // use just to get # of cols
		          int temp = colTitles.size();
		          colTitles = new Vector();
		          for (int l = 0; l < temp; l++) {
		            colTitles.addElement("Column " + (l + 1));
		          }
		          start--; // include first line
		        }
		        vec = new Vector();
		        Vector vec1;
		        numcols = colTitles.size();
		        for (int i = start; i < nlines; i++) {
		          vec1 = getColumnValues(lines[i], ignoreConsequtiveDelimiters, sDelim);
		          boolean missing = false;
		          int currSize = vec1.size();
		          while (currSize < numcols) {
		            vec1.addElement("");
		            currSize++;
		            missing = true;
		          }
		          vec.addElement(vec1);
		        }

		        buildTable();
		      //}
		      //if(!hasReturnedFromScreen2) {

		        columnAttributes = new Vector();
		        needToSetPageData = new boolean[numcols];
		        Arrays.fill(needToSetPageData, true);

		      //}
		    }
		    //DataScrollPanel.getViewport().removeAll();
		    //DataScrollPanel.getViewport().add(table);
		    //hasReturnedFromScreen2 = false;
		  }
	  
	      
	  /*
	   * parses a line of text data into a Vector of column data for that row
	   *
	   * @param str a line of string data from input
	   * @return a vector with each elements being column data for the row
	   */
	  private Vector getColumnValues(String str, boolean ignoreConsequtiveDelimiters, String sDelim) {
	    //String sDelim = getDelimiterString();
	    String oldToken = "";
	    String token = "";
	    Vector res = new Vector();
	    //ignoreConsequtiveDelimiters = ConsecutiveCheckBox.isSelected();
	    if (ignoreConsequtiveDelimiters) {
	      StringTokenizer st = new StringTokenizer(str, sDelim, false);
	      while (st.hasMoreTokens()) {
	        token = st.nextToken().trim();
	        res.addElement(token);
	      }
	    } else {
	      StringTokenizer st = new StringTokenizer(str, sDelim, true);
	      while (st.hasMoreTokens()) {
	        token = st.nextToken().trim();
	        if (!inDelimiterList(token, sDelim)) {
	          res.addElement(token);
	        } else {
	          if (inDelimiterList(oldToken, sDelim)) {
	              //&& (inDelimiterList(token, sDelim))) {
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
		    if (test > -1) {
		      result = true;
		    }
		    return result;
		  }

	  /*
	   * builds JTable from given data
	   */
	  private void buildTable() {
		tableModel = new UneditableTableModel(vec, colTitles);
	    table = new JTable(tableModel);
	    table.setColumnSelectionAllowed(true);
	    table.setRowSelectionAllowed(false);
	    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    fullColumnModel = table.getColumnModel();

	  }

	
	  

}
