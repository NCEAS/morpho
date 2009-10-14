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

import static javax.swing.JOptionPane.showMessageDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTable;


/**
 * This represents a text data file which will be imported. Data will be stored in a JTable. 
 * Some medata will be stored too.
 * @author tao
 *
 */
public class ImportedTextFile 
{
	/**
	   * a global reference to the table used to display the data that is
	   * being referenced by this text import process
	   */
	  private JTable table;

	  /**
	   * 	a global reference to the table used to display the lines read from the file
	   *	this is the table that is displayed on the first screen of the TIW
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
	  private String guessedDelimiter = null;
	  
	  //represents the unknow delimiter
	  public static final String  UNKNOWN = "unknown";

	  /**
	   * array of line strings
	   */
	  private String[] lines;

	  private File dataFile;

	  private String shortFilename;

	public JTable getTable() 
	{
		return table;
	}

	public void setTable(JTable table) 
	{
		this.table = table;
	}

	public JTable getLinesTable() {
		return linesTable;
	}

	public void setLinesTable(JTable linesTable) 
	{
		this.linesTable = linesTable;
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

	public void setDataFile(File dataFile) 
	{
		this.dataFile = dataFile;
	}

	public String getShortFilename() 
	{
		return shortFilename;
	}

	public void setShortFilename(String shortFilename) 
	{
		this.shortFilename = shortFilename;
	}
	
	public String getGuessedDelimiter() {
		return guessedDelimiter;
	}

	public void setGuessedDelimiter(String guessedDelimiter) {
		this.guessedDelimiter = guessedDelimiter;
	}

	
	 /**
	   * parses data input file into an array of lines (Strings)
	   *
	   * @param f the file name
	   * @return boolean true if parse was successful (textfile only); false if
	   *   parse unsuccessful (non-text file)
	   */
	  public boolean parsefile(File f) {

	    String temp = null;

	    if (isTextFile(f)) {

	      BufferedReader in = null;
	      try {
	        in = new BufferedReader(new FileReader(f));
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

	      return true;

	    } else {

	      JOptionPane.showMessageDialog(null, "Selected File is NOT a text file!",
	                                    "Message",
	                                    JOptionPane.INFORMATION_MESSAGE, null);
	      //CancelButton_actionPerformed(null);

	      return false;
	    }
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
	      return "tab";
	    } else if (mostFrequent(",") > 0) {
	      //CommaCheckBox.setSelected(true);
	      //parseOn = true;
	      return "comma";
	    } else if (mostFrequent(" ") > 0) {
	      //SpaceCheckBox.setSelected(true);
	     // parseOn = true;
	      return "space";
	    } else if (mostFrequent(";") > 0) {
	      //SemicolonCheckBox.setSelected(true);
	      //parseOn = true;
	      return "semicolon";
	    } else if (mostFrequent(":") > 0) {
	      //SpaceCheckBox.setSelected(true);
	      //OtherCheckBox.setSelected(true);
	      //OtherTextField.setText(":");
	      //parseOn = true;
	      return "colon";
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
	  private boolean isTextFile(File file) {
	     boolean text = true;
	     int res;
	     int cnt = 0;
	     int maxcnt = 2000; // only check this many bytes to avoid performance problems
	     FileInputStream in = null;
	     try {
	       in = new FileInputStream(file);
	       while (((res = in.read()) > -1) && (cnt < maxcnt)) {
	         cnt++;
	         if (res == 0) {
	           text = false;
	           break;
	         }
	       }
	       in.close();
	     } catch (Exception e) { e.printStackTrace(); }
	     finally {
	       try { in.close(); }
	       catch (IOException e) {}
	     }
	     return text;
	   }

	

}
