/**
 *  '$RCSfile: PersistentTableModel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-08-15 23:11:13 $'
 * '$Revision: 1.1.2.4 $'
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
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;
import javax.swing.event.*;
import java.lang.Class;


public class PersistentTableModel extends javax.swing.table.AbstractTableModel
{
    PersistentVector pv;
    Vector colNames = null;
    
    String delimiter = "\t";
    
    String field_delimiter = "#x09";
    
    // first row of data (allow for comments preceeding data)
    int firstRow = 0;
    
    public PersistentTableModel(PersistentVector perV) {
        super();
        pv = perV;
    }

    public PersistentTableModel(PersistentVector perV, Vector colNames) {
        super();
        pv = perV;
        this.colNames = colNames;
    }
        
    public PersistentVector getPersistentVector() {
      return pv; 
    }
    
    public void setPersistentVector(PersistentVector pv) {
      this.pv = pv;
    }
    
    public void setFieldDelimiter(String s) {
      this.field_delimiter = s.trim();
    }
    
    public void saveAsFile(String fileName) {
      pv.writeObjects(fileName);
    }
    
    public String getColumnName(int col) {
      String colName = (String)colNames.elementAt(col);
        return colName;
    }
    
    public boolean isCellEditable(int rowindex, int colindex) {
      return true;
    }
    
    public int getColumnCount()
    {
      int numCols = 0;
      if (colNames!=null) {
        numCols = colNames.size();  
      }
      else {
        String[] firstRecord = (String[])pv.elementAt(firstRow);
        numCols = firstRecord.length;
      }
      return numCols;
    }

    public int getRowCount()
    {
        return pv.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Object ocell;
            Object obj = pv.elementAt(rowIndex);
            String[] record = (String[])obj;
            if (record.length>columnIndex) {
                ocell = record[columnIndex];
            }
            else {
                ocell = "";
            }
            return ocell;
    }

	/**
	 * converts an array to a vector
	 * 
	 * @param str a String array of data for each column in a row
	 * @return a vector with each elements being column data for the row
	 */
	private Vector columnValuesAsVector(String[] str) {
	  Vector res = new Vector();
    int imax = getColumnCount();
    if (imax<str.length) imax = str.length;
    for (int i=0;i<imax;i++) {
      if (i<str.length) {
        res.addElement(str[i]);
      }
      else {
        res.addElement("");
      }
    }
	  return res;
	}
  
  /**
	* converts an vector to an array
	*/ 
  private String[] columnValuesAsArray(Vector vec) {
    String[] res = new String[vec.size()];
    for (int i=0;i<vec.size();i++) {
      res[i] = (String)vec.elementAt(i);
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
  
  /**
   * sorts the table; 
   * set sortdir=1 for ascending
   * set sortdir = -1 for descending
   */
  public void sort (int colnum, int sortdir) {
    String[] o1Str;
    String[] o2Str;
    String token1;
    String token2;
    final int cn = colnum;
    final int sdir = sortdir;
 		long start = System.currentTimeMillis();
   Collections.sort(pv.objectList, new Comparator() {
        String[] o1Str;
        String[] o2Str;
        String token1;
        String token2;
      public int compare(Object o1, Object o2) {
        int res = 0;
        try{
          if (Long.class.isInstance(o1)) {
            o1Str = (String[])pv.obj.readObject(((Long)o1).longValue());
          }
          else {
            o1Str = (String[])o1;
          }
          token1 = o1Str[cn];
          if (Long.class.isInstance(o2)) {
            o2Str = (String[])pv.obj.readObject(((Long)o2).longValue());
          }
          else {
            o2Str = (String[])o2;
          }
          token2 = o2Str[cn];
          res = token1.compareTo(token2);
          res = sdir*res;
        }
        catch (Exception w) {}
	    return res;
      }
    });
   
   	long stop = System.currentTimeMillis();
		int time = (int)(stop-start);
    System.out.println("Time = "+time);

    fireTableStructureChanged();   
  }
  
  public void setValueAt(Object obj, int row, int col) {
    String[] rowA = (String[])pv.elementAt(row);
    rowA[col] = (String)obj;
    pv.setElementAt(rowA, row);
  }
 
/* 
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
  */
  
  /**
   *  add a row to the end of the data
   *  vec is assumed to be a Vector of strings
   */
  public void addRow(Vector vec) {
    while (vec.size()<getColumnCount()) vec.addElement(" ");
    String[] record = new String[getColumnCount()];
    for (int i=0;i<getColumnCount();i++) {
      record[i] = (String)vec.elementAt(i);
    }
    pv.addElement(record);
    fireTableRowsInserted(pv.size(),pv.size());
  }
  
   /**
   *  insert a row at indicated position
   *  vec is assumed to be a Vector of strings
   */
  public void insertRow(int row, Vector vec) {
    while (vec.size()<getColumnCount()) vec.addElement(" ");
    String[] record = new String[getColumnCount()];
    for (int i=0;i<getColumnCount();i++) {
      record[i] = (String)vec.elementAt(i);
    }
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
      String[] record = (String[])obj;
      Vector vals = columnValuesAsVector(record);
      vals.addElement("");
      String[] newRecord = columnValuesAsArray(vals);
      newpv.addElement(newRecord);
    }
    pv.delete();
    pv = newpv;  
    pv.setFieldDelimiter("#x09");
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
      String[] record = (String[])obj;
      Vector vals = columnValuesAsVector(record);
      vals.insertElementAt("",colnum);
      String[] newRecord = columnValuesAsArray(vals);
      newpv.addElement(newRecord);
    }
    pv.delete();
    pv = newpv;  
    pv.setFieldDelimiter("#x09");
    fireTableStructureChanged();
  }
  
  /**
   * deletes the indicated column
   */
   public void deleteColumn(int col) {
    PersistentVector newpv = new PersistentVector();
    for (int i=firstRow;i<getRowCount();i++) {
      Object obj = pv.elementAt(i);
      String[] record = (String[])obj;
      Vector vals = columnValuesAsVector(record);
      vals.removeElementAt(col);
      String[] newRecord = columnValuesAsArray(vals);
      newpv.addElement(newRecord);
    }
    pv.delete();
    pv = newpv;  
    pv.setFieldDelimiter("#x09");
    fireTableStructureChanged();   
   }
}