/**
 *  '$RCSfile: PersistentTableModel.java,v $'
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
 *  A tableModel that uses PersistentVector for disk-based storage. This should allow for very large tables
 */
 
package edu.ucsb.nceas.morpho.datapackage;
import javax.swing.table.*;
import java.util.Vector;
import java.util.StringTokenizer;
import javax.swing.event.*;


public class PersistentTableModel extends javax.swing.table.AbstractTableModel
{
    PersistentVector pv;

    String delimiter = "\t";
    
    // first row of data (allow for comments preceeding data)
    int firstRow = 0;
    
    public PersistentTableModel(PersistentVector perV) {
        super();
        pv = perV;
    }
        
    public PersistentVector getPersistentVector() {
      return pv; 
    }
    
    public void setPersistentVector(PersistentVector pv) {
      this.pv = pv;
    }
    
    public void saveAsFile(String fileName) {
      pv.writeObjects(fileName);
    }
    
    public boolean isCellEditable(int rowindex, int colindex) {
      return true;
    }
    
    public int getColumnCount()
    {
        String firstRecord = (String)pv.elementAt(firstRow);
        Vector vals = getColumnValues(firstRecord);
        int numCols = vals.size();
        return numCols;
    }

    public int getRowCount()
    {
        return pv.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Object ocell;
 //       if (columnIndex==0) {
 //           String colcnt = "Column "+(rowIndex+1);
 //           return colcnt;
 //       }
//        else {
            Object obj = pv.elementAt(rowIndex);
            String record = (String)obj;
            Vector vals = getColumnValues(record);
            if (vals.size()>(columnIndex)) {
                ocell = vals.elementAt(columnIndex);
            }
            else {
                ocell = "";
            }
            return ocell;
//        }
    }

	/**
	 * parses a line of text data into a Vector of column data for that row
	 * 
	 * @param str a line of string data from input
	 * @return a vector with each elements being column data for the row
	 */
	private Vector getColumnValues(String str) {
	  //  String sDelim = getDelimiterString();
	    String sDelim = ",\t";
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


  public void setValueAt(Object obj, int row, int col) {
 //   System.out.println("vals "+setColumnValue(row, col, obj));
    setColumnValues(row, setColumnValue(row, col, obj));  
  }
  
  private void setColumnValues(int rowIndex, Object recordVals) {
    pv.setElementAt((String)recordVals, rowIndex);  
  }
  
  private String setColumnValue(int rowIndex, int colIndex, Object val) {
    String recordString = "";
    Object obj = pv.elementAt(rowIndex);
    String record = (String)obj;
    Vector vals = getColumnValues(record);
 //   System.out.println("val = "+val);
    vals.setElementAt(val, colIndex);
    for (int i=0;i<vals.size();i++) {
      recordString = recordString + (String)vals.elementAt(i) + delimiter;
    }
    recordString = recordString.substring(0,recordString.length()-1);
    return recordString;
  }
  
  /**
   *  add a row to the end of the data
   *  vec is assumed to be a Vector of strings
   */
  public void addRow(Vector vec) {
    while (vec.size()<getColumnCount()) vec.addElement(" ");
    String record = "";
    for (int i=0;i<getColumnCount()-1;i++) {
      record = record + (String)vec.elementAt(i) + delimiter;
    }
    record = record + (String)vec.elementAt(getColumnCount()-1);
    pv.addElement(record);
    fireTableRowsInserted(pv.size(),pv.size());
  }
  
   /**
   *  insert a row at indicated position
   *  vec is assumed to be a Vector of strings
   */
  public void insertRow(int row, Vector vec) {
    while (vec.size()<getColumnCount()) vec.addElement(" ");
    String record = "";
    for (int i=0;i<getColumnCount()-1;i++) {
      record = record + (String)vec.elementAt(i) + delimiter;
    }
    record = record + (String)vec.elementAt(getColumnCount()-1);
    pv.insertElementAt(record, row);
    fireTableRowsInserted(row,row);
  }

   /**
   *  delete a row at indicated position
   *  vec is assumed to be a Vector of strings
   */
  public void deleteRow(int row) {
    pv.removeElementAt(row);
    fireTableRowsDeleted(row,row);
  }

 /**
  *  add a column after the last current column
  *  new column is filled with spaces
  */
  public void addColumn() {
    PersistentVector newpv = new PersistentVector();
    for (int i=firstRow;i<getRowCount();i++) {
      Object obj = pv.elementAt(i);
      String record = (String)obj;
      Vector vals = getColumnValues(record);
      record = "";
      for (int j=0;j<vals.size();j++) {
        record = record + (String)vals.elementAt(j)+ delimiter;  
      }
      record = record + "";
      newpv.addElement(record);
    }
    pv.delete();
    pv = newpv;  
    fireTableStructureChanged();
  }
  
  /**
  *  add a column after the last current column
  *  new column is filled with spaces
  */
  public void insertColumn(int colnum) {
    PersistentVector newpv = new PersistentVector();
    for (int i=firstRow;i<getRowCount();i++) {
      Object obj = pv.elementAt(i);
      String record = (String)obj;
      Vector vals = getColumnValues(record);
      vals.insertElementAt("",colnum);
      record = "";
      for (int j=0;j<vals.size()-1;j++) {
        record = record + (String)vals.elementAt(j)+ delimiter;  
      }
      record = record + (String)vals.elementAt(vals.size()-1);
      newpv.addElement(record);
    }
    pv.delete();
    pv = newpv;  
    fireTableStructureChanged();
  }
  
  /**
   * deletes the indicated column
   */
   public void deleteColumn(int col) {
      PersistentVector newpv = new PersistentVector();
    for (int i=firstRow;i<getRowCount();i++) {
      Object obj = pv.elementAt(i);
      String record = (String)obj;
      Vector vals = getColumnValues(record);
      vals.removeElementAt(col);
      record = "";
      for (int j=0;j<vals.size()-1;j++) {
        record = record + (String)vals.elementAt(j)+ delimiter;  
      }
      record = record + (String)vals.elementAt(vals.size()-1);
      newpv.addElement(record);
    }
    pv.delete();
    pv = newpv;  
    fireTableStructureChanged();   
   }
}