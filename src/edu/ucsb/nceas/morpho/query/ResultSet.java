/**
 *  '$RCSfile: ResultSet.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-02 21:39:02 $'
 * '$Revision: 1.1 $'
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

package edu.ucsb.nceas.morpho.query;

import javax.swing.table.AbstractTableModel;
import javax.swing.ImageIcon;
//import java.awt.*;
//import java.awt.event.*;

public class ResultSet extends AbstractTableModel
{
  private String query = null;

  private ImageIcon folder = new ImageIcon(
                    getClass().getResource("Btflyyel.gif"));
  private String[] columnNames = { "", "First Name",
                                   "Last Name", "Sport",
                                   "# of Years", "Vegetarian"
  };
  private Object[][] data = {
    {folder, "Mary", "Campione",
     "Snowboarding", new Integer(5), new Boolean(false)},
    {folder, "Alison", "Huml",
     "Rowing", new Integer(3), new Boolean(true)},
    {folder, "Kathy", "Walrath",
     "Chasing toddlers", new Integer(2), new Boolean(false)},
    {folder, "Mark", "Andrews",
     "Speed reading", new Integer(20), new Boolean(true)},
    {folder, "Angela", "Lih",
     "Teaching high school", new Integer(4), new Boolean(false)}
  };

  public ResultSet()
  {
/*
    folder = new ImageIcon(getClass().getResource("Btflyyel.gif"));

    columnNames = new String[6];
    columnNames[0] = "";
    columnNames[1] = "First Name";
    columnNames[2] = "Last Name";
    columnNames[3] = "Sport";
    columnNames[4] = "# of Years";
    columnNames[5] = "Vegetarian";
*/
    //data[][] = { {folder, "Mary", "Campione",
                  //"Snowboarding", new Integer(5), new Boolean(false)},
                 //{folder, "Alison", "Huml",
                  //"Rowing", new Integer(3), new Boolean(true)},
                 //{folder, "Kathy", "Walrath",
                  //"Chasing toddlers", new Integer(2), new Boolean(false)},
                 //{folder, "Mark", "Andrews",
                  //"Speed reading", new Integer(20), new Boolean(true)},
                 //{folder, "Angela", "Lih",
                  //"Teaching high school", new Integer(4), new Boolean(false)}
               //};
  }

  public int getColumnCount()
  {
    return columnNames.length;
  }

  public int getRowCount()
  {
    return data.length;
  }

  public int getRowHeight()
  {
    return folder.getIconHeight();
  }

  public String getColumnName(int col)
  {
    return columnNames[col];
  }

  public Object getValueAt(int row, int col)
  {
    return data[row][col];
  }

  /*
   * JTable uses this method to determine the default renderer/
   * editor for each cell.  If we didn't implement this method,
   * then the last column would contain text ("true"/"false"),
   * rather than a check box.
   */
  public Class getColumnClass(int c)
  {
    return getValueAt(0, c).getClass();
  }

  /*
   * Don't need to implement this method unless your table's
   * editable.
   */
  public boolean isCellEditable(int row, int col)
  {
    //Note that the data/cell address is constant,
    //no matter where the cell appears onscreen.
/*
            if (col < 2) { 
                return false;
            } else {
                return true;
            }
*/
    return false;
  }

  /*
   * Don't need to implement this method unless your table's
   * data can change.
   */
/*
  public void setValueAt(Object value, int row, int col)
  {
    if (data[0][col] instanceof Integer && !(value instanceof Integer))
    {
      //With JFC/Swing 1.1 and JDK 1.2, we need to create    
      //an Integer from the value; otherwise, the column     
      //switches to contain Strings.  Starting with v 1.3,   
      //the table automatically converts value to an Integer,
      //so you only need the code in the 'else' part of this 
      //'if' block.                                          
      //XXX: See TableEditDemo.java for a better solution!!!
      try
      {
	data[row][col] = new Integer(value.toString());
	fireTableCellUpdated(row, col);
      }
      catch(NumberFormatException e)
      {
	System.out.println("The \"" + getColumnName(col)
			   + "\" column accepts only integer values.");
      }
    }
    else
    {
      data[row][col] = value;
      fireTableCellUpdated(row, col);
    }

    if (DEBUG)
    {
      System.out.println("New value of data:");
      printDebugData();
    }
  }

  private void printDebugData()
  {
    int numRows = getRowCount();
    int numCols = getColumnCount();

    for (int i = 0; i < numRows; i++)
    {
      System.out.print("    row " + i + ":");
      for (int j = 0; j < numCols; j++)
      {
	System.out.print("  " + data[i][j]);
      }
      System.out.println();
    }
    System.out.println("--------------------------");
  }
*/
}
