/**
 *  '$RCSfile: PersistentVector.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-09-02 17:34:24 $'
 * '$Revision: 1.4 $'
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
import java.util.*;


/* 
A vector-like class that uses persistent storage (ObjectFile) rather than RAM for
storage. 
Note that some sorting tests indicate that access of disk-based objects is roughly 100 times
slower than a RAM based implementation.
Thus, to speed up some operations, a RAM based cache of the first N items in the collection
will be implemented
*/

public class PersistentVector
{
    static int objNum = 0;
    
    /**
    * allow for header rows
    */
    int firstRow = 0;
    
   /**
   * number of items to be stored in memory
   */
   int inMemoryNum = 850000;
    
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
    Vector headerLinesVector;

    
    public PersistentVector() {
        objectList = new Vector();
        objNum++;
        try{
          objName = objName + objNum;
          obj = new ObjectFile(objName);  
        }
        catch (Exception w) {}
    }
    
    /*
    * needed for skipping over comments, header info
    */
    public void setFirstRow(int frow) {
      this.firstRow = frow;
    }

    /*
    * needed for skipping over comments, header info
    */
    public void setInMemoryNum(int num) {
      this.inMemoryNum = num;
    }
    
    /*
    * read a text file and store each line as an object in an ObjectFile
    */
    public void init(String filename) {
        String temp;
        int nlines;
        File f = new File(filename);    
        try{
          BufferedReader in = new BufferedReader(new FileReader(f));
          nlines = 0;
          long pos = 0;
          headerLinesVector = new Vector();
          try {
            while (((temp = in.readLine())!=null)) {
                if (temp.length()>0) {   // do not count blank lines
                  nlines++;
                  if (nlines>firstRow) {
                    String[] tempA = getColumnValues(temp);
                    if (nlines>inMemoryNum) {
                      pos = obj.writeObject(tempA);  // object added to file
                      Long lpos = new Long(pos);
                      objectList.addElement(lpos); // position added to objectList
                    }
                    else {
                      objectList.addElement(tempA);  
                    }
                  }
                  else {    // header info
                   headerLinesVector.addElement(temp);
                  }
                }
            }
            in.close();
          }
          catch (Exception e) {};
        }
        catch (Exception w) {};
    }
    
    //read a text file and store each line as an object in an ObjectFile
    // start with a file object
    public void init(File f) {
        String temp;
        int nlines;
        try{
          BufferedReader in = new BufferedReader(new FileReader(f));
          nlines = 0;
          long pos = 0;
          headerLinesVector = new Vector();
          try {
            while (((temp = in.readLine())!=null)) {
                if (temp.length()>0) {   // do not count blank lines
                  nlines++;
                  if (nlines>firstRow) {
                    String[] tempA = getColumnValues(temp);
                    if (nlines>inMemoryNum) {
                      pos = obj.writeObject(tempA);  // object added to file
                      Long lpos = new Long(pos);
                      objectList.addElement(lpos); // position added to objectList
                    }
                    else {
                      objectList.addElement(tempA);  
                    }
                  }
                  else {    // header info
                   headerLinesVector.addElement(temp);
                  } 
                } 
            }
            in.close();
          }
          catch (Exception e) {};
        }
        catch (Exception w) {};
    }

    public void init (File f, int fRow) {
      this.firstRow = fRow;
      init(f);
    }    
    
   /*
   * initiallize with a single element
   */
   public void initEmpty(String[] ar) {
     try {
       long pos = obj.writeObject(ar);  // object added to file
       Long lpos = new Long(pos);
       objectList.addElement(lpos); // position added to objectList 
     }
     catch (Exception e) {}
   }
    
    public void setFieldDelimiter(String s) {
      this.field_delimiter = s.trim();
    }
 
    //write a text file from the pv
    public void writeObjects(String filename) {
        File f = new File(filename);
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            // write header lines if they exist
            if (headerLinesVector!=null) {
              for (int jj=0;jj<headerLinesVector.size();jj++) {
                String hline = (String)headerLinesVector.elementAt(jj);
                out.write(hline, 0, hline.length());
                out.newLine(); 
              }
            }
            for (int i=0; i<this.size();i++) {
                String[] s = (String[])this.elementAt(i);
                String sss = s[0];
                for (int ii=1;ii<s.length;ii++) {
                  sss = sss + "\t" + s[ii];
                } 
                out.write(sss, 0, sss.length());
                out.newLine();
            }
        out.flush(); 
        out.close();
        }
        catch (Exception e) {}
        
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
//        if (i>inMemoryNum) {
          long pos = obj.writeObject(o);  // object added to end of file
          Long lpos = new Long(pos);
          objectList.insertElementAt(lpos, i);
//        }
//        else {
//          
//        }
      }
      catch (Exception w) {}
    }
    
    public void removeElementAt(int i) {
        objectList.removeElementAt(i); 
    }
    
    public int size() {
        return objectList.size();
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
    
    private String[] getColumnValues(String str) {
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
  
	static public void main(String args[])
	{
	    PersistentVector pv = new PersistentVector();
	    System.out.println("New Persistent Vector created.");
	    pv.init("C:/VisualCafe/Projects/PersistentVector/test.txt");
	    System.out.println("Object File has been filled!");
	    String s = (String)pv.elementAt(0);
	    System.out.println(s);
	    String s1 = (String)pv.elementAt(100);
	    System.out.println(s1);
	    pv.insertElementAt("DFH DFH DFH",3);
	    pv.writeObjects("C:/VisualCafe/Projects/PersistentVector/test1.txt");
	}
    
}