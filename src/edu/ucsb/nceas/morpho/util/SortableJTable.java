/**
 *  '$RCSfile: SortableJTable.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-09-26 05:34:39 $'
 * '$Revision: 1.6 $'
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
package edu.ucsb.nceas.morpho.util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Point;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * A classs represent sortable table
 */
public class SortableJTable extends JTable implements MouseListener
{
  // Constant string
  public static final String ASCENDING = "ascending";
  public static final String DECENDING = "decending";
  public static final String NONORDERED = "nonodered"; 
  // Flag for the table sorted or not
  private boolean sorted;
  // Index of column which to be sorted
  private int indexOfSortedColumn;
  // Order of sorted cloumn
  private String orderOfSortedColumn;
  // Header of table
  private JTableHeader tableHeader;
  
 /**
  * Constructor of SortableJTable
  * @param model ColumnSortableTableModel
  */
  public SortableJTable(ColumnSortableTableModel model)
  {
    super(model);
    // initialize of unsorted table
    sorted = false;
    indexOfSortedColumn = -1;
    orderOfSortedColumn = NONORDERED;
    // set talbeHeader
    tableHeader = getTableHeader();
    // Set table header renderer
    tableHeader.setDefaultRenderer(new SortableTableHeaderCellRenderer());
    // Add mouse listener to table header
    tableHeader.addMouseListener(this);
  }

  /**
   * Get the table if sorted or not
   */
  public boolean getSorted()
  {
    return sorted;
  }
  
  /**
   * Set the table sorted or not
   * @param myStorted the table status want assign
   */
  public void setSorted(boolean mySorted)
  {
    sorted = mySorted;
  }
  
  /**
   * Get the index of Sorted column
   */
  public int getIndexOfSortedColumn()
  {
    return indexOfSortedColumn;
  } 
  
  /**
   * Set the index of sorted column 
   * @param myIndex the index of sorted column 
   */
  public void setIndexOfSortedColumn(int myIndex)
  {
    indexOfSortedColumn = myIndex;
  }
  
  /**
   * Get the sorted table oder statuts
   */
  public String getOrderOfSortedColumn()
  {
    return orderOfSortedColumn;
  }
   
  /**
   * Set the order of sorted column 
   * @param myOrder the order of sorted column 
   */
  public void setOrderOfSortedColumn(String myOrder)
  {
    orderOfSortedColumn = myOrder;
  }
   
  
  /**
   * Mouse click event handler
   */
  public void mouseClicked(MouseEvent event) 
  {
    // a sort table command
    SortTableCommand sortCommand = null;
    // Get the index in the table (dispaly)
    int index = this.columnAtPoint(new Point(event.getX(), event.getY()));
    // Order
    String order = null;
    // If not sorted
    if (!sorted)
    {
      // The first time is ascending sort
      order = ASCENDING;
    }
    else
    {
      // If this column already sorted, change order.
      if (indexOfSortedColumn == index && 
                              orderOfSortedColumn.equals(ASCENDING))
      {
        order= DECENDING;
      }
      else
      {
        order = ASCENDING;
      }
    }//else
    //create instance of SortTableCommand
    sortCommand = new SortTableCommand(this, index, order);
    sortCommand.execute(null);
  }
  public void mouseReleased(MouseEvent event){}
  public void mousePressed(MouseEvent event) {}
  public void mouseEntered(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
  
}

