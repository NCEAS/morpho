/**
 *  '$RCSfile: PersistentVector.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-08-06 20:02:17 $'
 * '$Revision: 1.1.2.1 $'
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
storage
*/

public class PersistentVector
{
    static int objNum = 0;
    
    // allow for header rows
    int firstRow = 0;
    
    // a vector of Longs that contains pointer to position of real data
    Vector objectList;      
    
    // the ObjectFile where objects are actually stored
    ObjectFile obj;
    
    //the name of the ObjectFile
    String objName = "ObjectFile";
    
    public PersistentVector() {
        objectList = new Vector();
        objNum++;
        try{
          objName = objName + objNum;
          obj = new ObjectFile(objName);  
        }
        catch (Exception w) {}
    }
    
    
    //read a text file and store each line as an object in an ObjectFile
    public void init(String filename) {
        String temp;
        int nlines;
        File f = new File(filename);    
        try{
          BufferedReader in = new BufferedReader(new FileReader(f));
          nlines = 0;
          long pos = 0;
          try {
            while (((temp = in.readLine())!=null)) {
                if (temp.length()>0) {   // do not count blank lines
                  nlines++;
                  if (nlines>firstRow) {
                    pos = obj.writeObject(temp);  // object added to file
                    Long lpos = new Long(pos);
                    objectList.addElement(lpos); // position added to objectList
                  }
                } 
            }
//            System.out.println(nlines + " added to ObjectFile");
            in.close();
          }
          catch (Exception e) {};
        }
        catch (Exception w) {};

    }
    
    //write a text file from the pv
    public void writeObjects(String filename) {
        File f = new File(filename);
        try{
            BufferedWriter out = new BufferedWriter(new FileWriter(f));
            for (int i=0; i<this.size();i++) {
                String s = (String)this.elementAt(i);
                out.write(s, 0, s.length());
                out.newLine();
            }
        out.flush(); 
        out.close();
        }
        catch (Exception e) {}
        
    }   
    
    public Object elementAt(int iii) {
        Object o = null;
        try{
            Long lll = (Long)objectList.elementAt(iii); 
            o = obj.readObject(lll.longValue());
        }
        catch (Exception e) {}
        return o;
    }   

    public void addElement(Serializable o) {
      try{  
        long pos = obj.writeObject(o);  // object added to end of file
        Long lpos = new Long(pos);
        objectList.addElement(lpos);
      }
      catch (Exception w) {}
     }
    
    public void setElementAt(Serializable o, int i) {
      try{  
        long pos = obj.writeObject(o);  // object added to end of file
        Long lpos = new Long(pos);
        objectList.setElementAt(lpos, i);
      }
      catch (Exception w) {}
    }

    
    public void insertElementAt(Serializable o, int i) {
      try{  
        long pos = obj.writeObject(o);  // object added to end of file
        Long lpos = new Long(pos);
        objectList.insertElementAt(lpos, i);
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