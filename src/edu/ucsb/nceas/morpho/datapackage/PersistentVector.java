/**
 *  '$RCSfile: PersistentVector.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-04-16 21:57:46 $'
 * '$Revision: 1.17 $'
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

 /**
 * Class that implements a random access file for storing multiple objects that would take up too
 * much room in memory. Used here for storing a collection of Strings (records) for very large
 * text based data sets. When combined with the PersistentVector class and PersistentTableModel class
 * this gives the ability to have tables of almost unlimited size (since only a vector pointing to
 * locations in the object file is stored in RAM)
 */
 
package edu.ucsb.nceas.morpho.datapackage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.Morpho;

/* 
A vector-like class that uses persistent storage (ObjectFile) rather than RAM for
storage. 
Note that some sorting tests indicate that access of disk-based objects is 
roughly 100 times slower than a RAM based implementation.
Thus, to speed up some operations, a RAM based cache of the first N items 
in the collection will be implemented
*/

public class PersistentVector
{
  static int objNum = 0;
    
  /**
   * allow for header rows
   */
  public int firstRow = 0;
    
  /**
   * number of items to be stored in memory
   * This parameter should be as large as possible when a PersistentVector
   * is used in the PersistentTableModel class because sorting of tables from
   * disk is much slower than sorting from memory (presumably because of the
   * seek time needed to access values for comparisons)
   * For other uses, it can be set to 0 which puts all values on disk.
   */
  int inMemoryNum = 20000;
    
  /*
   * a vector that contains either the object or a long pointer
   * to the position of the object in a random access file
   */
  Vector objectList;  

  /*
   * the ObjectFile where objects are actually stored
   */
  ObjectFile obj;
    
  /*
   * the name of the ObjectFile
   */
  String objName = "ObjectFile";
    
  /*
   * field delimiter string
   */
  String field_delimiter = "#x09";
    
  /*
   * header line vector
   */
  private Vector<String[]> headerLinesVector;

  /*
   * tmp directory for object file
   */
  private String tmpDir = null;
  
  /**
   *  flag to indicate whether to ignore consequtiveDelimiters
   */
  private boolean ignoreConsecutiveDelimiters = false;
  
  /**
   * Constructor of PersistentVector
   *
   */
  public PersistentVector() {
    objectList = new Vector(inMemoryNum+1);
    objNum++;
    getTempDirFromConfig();
    try{
      if (tmpDir != null)
      {
        objName=tmpDir+objName+objNum;
      }
      else
      {
        objName = objName + objNum;
      }
      obj = new ObjectFile(objName);  
    }
    catch (Exception w) {}
  }
  
  
  public void setIgnoreConsecutiveDelimiters(boolean flag) {
    ignoreConsecutiveDelimiters = flag;
  }
  
  /*
   * Method to get tmp directory for object file
   */
  private void getTempDirFromConfig()
  {
    Morpho morpho = UIController.getMorpho();
    ConfigXML config = morpho.getConfiguration();
    ConfigXML profile = morpho.getProfile();
    String profileDirName = config.getConfigDirectory() + 
                                File.separator +
                                config.get("profile_directory", 0) + 
                                File.separator + 
                                profile.get("profilename", 0);
    tmpDir = profileDirName + File.separator 
             + profile.get("tempdir", 0)+File.separator;
       
  }
  /*
   * needed for skipping over comments, header info
   */
   public void setFirstRow(int frow) {
     this.firstRow = frow;
   }
   
  /**
   * Method to get the value of first Row
   */
  public int getFirstRow()
  {
    return firstRow;
  }
  
    
   /**
    * Method to get headerLinesVector
    *
    */
   public Vector getHeaderLinesVector()
   {
     return headerLinesVector;
   }
   
   /**
    * Method to set a vector as a header lines vector
    * @param newHeaderLinesVector
    */
   public void setHeaderLinesVector(Vector newHeaderLinesVector)
   {
     headerLinesVector = newHeaderLinesVector;
   }
   
   /**
    * Method set tmp directory for Object file
    * 
    * @param directory  the tmp directory will be set
    */
   public void setTmpDir(String directory)
   {
     tmpDir = directory;
   }
    
  /*
	 * read a text file and store each line as an object in an ObjectFile each
	 * line in the file (assumed to be a text file) is turned into an array of
	 * strings
	 * 
	 * start with a file object
	 */
	public void init(File f) {
		int nlines;
		try {
			String sDelim = getDelimiterString();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), Charset.forName("UTF-8")));
			CSVReader csvReader = new CSVReader(in, sDelim.charAt(0));
			List<String[]> records = csvReader.readAll();
			nlines = 0;
			long pos = 0;
			headerLinesVector = new Vector();
			try {
				for (String[] record : records) {
					if (!isEmpty(record)) { // do not count blank lines, BRL: not sure about this one
						nlines++;
						if (nlines > firstRow) {
							if (nlines > inMemoryNum) {
								pos = obj.writeObject(record); // object added to file
								Long lpos = new Long(pos);
								objectList.addElement(lpos); // position added to objectList
							} else {
								objectList.addElement(record);
							}
						} else { // header info
							headerLinesVector.addElement(record);
						}
					}
				}
				csvReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (Exception w) {
			w.printStackTrace();
		}
	}
	
	private boolean isEmpty(String[] record) {
		for (String item: record) {
			if (item != null && item.length() > 0) {
				return false;
			}
		}
		return true;
	}

  public void init (File f, int fRow) {
    this.firstRow = fRow;
    init(f);
  }    
    
  /*
   * initiallize with a single element
   */
  public void initEmpty(String[] ar, int numEmptyRows) {
    try {
      for (int i=0;i<numEmptyRows;i++) {
        long pos = obj.writeObject(ar);  // object added to file
        Long lpos = new Long(pos);
        objectList.addElement(lpos); // position added to objectList 
      }
    }
     catch (Exception e) {}
  }
    
  public void setFieldDelimiter(String s) {
    this.field_delimiter = s.trim();
  }
  
  /**
   * Method to get field delimiter
   */
  public String getFieldDelimiter()
  {
    return field_delimiter;
  }
 
  /*
   * write a text file from the pv
   */
  public void writeObjects(File file) {
    try{
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF-8")));
      CSVWriter csvWriter = new CSVWriter(out, getDelimiterString().charAt(0));
      
      // write header lines if they exist
      if (headerLinesVector != null) {
        for (int jj=0; jj < headerLinesVector.size(); jj++) {
          String[] hline = headerLinesVector.elementAt(jj);
          csvWriter.writeNext(hline);
         }
       }
       for (int i=0; i < this.size(); i++) {
         String[] line = (String[]) this.elementAt(i);
         if (!isEmpty(line)) {
        	 csvWriter.writeNext(line);
         }
       }
       csvWriter.close();
    }
    catch (Exception e) {
    	e.printStackTrace();
    }
  }   
    
  public Object elementAt(int iii) {
    Object o = objectList.elementAt(iii);
    try{
      if (Long.class.isInstance(o)) {
        Long lll = (Long)objectList.elementAt(iii); 
        o = obj.readObject(lll.longValue());
      }
    }
    catch (Exception e) {}
    return o;
  }   

  public void addElement(Serializable o) {
    try{
      if (objectList.size()>inMemoryNum) {
        long pos = obj.writeObject(o);  // object added to end of file
        Long lpos = new Long(pos);
        objectList.addElement(lpos);
      }
    else {
      objectList.addElement(o);
      }
    }
    catch (Exception w) {}
   }
  
  
  public void setElementAt(Serializable o, int i) {
    try{ 
      if (i>inMemoryNum) {
        long pos = obj.writeObject(o);  // object added to end of file
        Long lpos = new Long(pos);
        objectList.setElementAt(lpos, i);
      }
      else {
        objectList.setElementAt(o, i);
      }
    }
    catch (Exception w) {}
  }

    
  public void insertElementAt(Serializable o, int i) {
    try{  
//    if (i>inMemoryNum) {
      long pos = obj.writeObject(o);  // object added to end of file
      Long lpos = new Long(pos);
      objectList.insertElementAt(lpos, i);
//      }
//      else {
//          
//        }
    }
    catch (Exception w) {}
  }
    
  public void removeElementAt(int i) {
    objectList.removeElementAt(i); 
  }
    
  public int size() {
		if(objectList == null) return 0;
    else return objectList.size();
  }
    
  public void removeAllElements() {
    objectList.removeAllElements();
    obj.delete();
    try{
      obj = new ObjectFile(objName+objNum);  
    }
    catch (Exception w) {}
  }
    
  public void delete() {
    obj.delete();
    objectList = null;
  }

  /** 
   * Method to assign a vector to objectList
   *
   *  @param newObjectList the new object list vector
   */
  public void setObjectList(Vector newObjectList)
  {
    objectList = newObjectList;
  }
    
  private String[] getColumnValues(String str) {
    // add a space in case line text ends in a delimiter
    // to insure that an empty string is saved
    str = str + " ";
	  String sDelim = getDelimiterString();
	  String oldToken = "";
	  String token = "";
	  Vector res = new Vector();
//	  boolean ignoreConsequtiveDelimiters = false;
	  if (ignoreConsecutiveDelimiters) {
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
    String[] vals = new String[res.size()];
    for (int i=0;i<res.size();i++) {
      vals[i] = (String)res.elementAt(i);  
    }
	  return vals;
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

  /*
   * this method converts hex delimiter strings to in-line strings
   * i.e. #x09 is converted to a tab \t
   */
	private String getDelimiterString() {
    String str = "";
    String temp = field_delimiter;
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
  
  /*
   * setter for the number of object stored in RAM before going to disk
   */
  public void setInMemoryNum (int num) {
    this.inMemoryNum = num;
  }

  /*
   * getter for the number of object stored in RAM before going to disk
   */
  public int getInMemoryNum () {
    return inMemoryNum;
  }
  
	static public void main(String args[])
	{
	  PersistentVector pv = new PersistentVector();
	  System.out.println("New Persistent Vector created.");
	  pv.init(new File("C:/VisualCafe/Projects/PersistentVector/test.txt"));
	  System.out.println("Object File has been filled!");
	  String s = (String)pv.elementAt(0);
	  System.out.println(s);
	  String s1 = (String)pv.elementAt(100);
	  System.out.println(s1);
	  pv.insertElementAt("DFH DFH DFH",3);
	  pv.writeObjects(new File("C:/VisualCafe/Projects/PersistentVector/test1.txt"));
	}
    
}
