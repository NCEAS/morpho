/**
 *  '$RCSfile: SortTableCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-04-12 22:52:10 $'
 * '$Revision: 1.2.6.1 $'
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

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import java.awt.event.ActionEvent;

/**
 * Class to handle sorting table command
 */
public class SortTableCommand implements Command
{
  /** A reference to the JTable */
  private JTable table = null;

  /** Column index by sorting */
  private int indexOfColumn = -1;

  /** Order of sorting */
  String order = null;

  /**
   * Constructor of SortableCommand
   * @param myTable the table need to be sorted
   * @param myIndex the index of column in the table need to be sorted
   * @param myOrder the order need to be sorted
   */
  public SortTableCommand(JTable myTable, int myIndex, String myOrder )
  {
    table = myTable;
    indexOfColumn = myIndex;
    order = myOrder;
  }//CancelCommand


  /**
   * execute sort table command
   */
  public void execute(ActionEvent event)
  {

    // table
    SortableJTable sortTable = null;
    // table model
    ColumnSortableTableModel tableModel = null;
    // table column model
    TableColumnModel columnModel = null;
    // table column
    TableColumn column = null;
    int indexInTableModel = -1;
    if (!(table instanceof SortableJTable)
                                    || order.equals(SortableJTable.NONORDERED))
    {
      return; //couldn't sort
    }
    else
    {
      // Casting
      sortTable = (SortableJTable) table;
    }
     // sorting
    // Get table column model
    columnModel = sortTable.getColumnModel();
    // Get table column
    column = columnModel.getColumn(indexOfColumn);
    // Get modelIndex in table column
    indexInTableModel = column.getModelIndex();
    // Get table model
    tableModel = (ColumnSortableTableModel)sortTable.getModel();
    // set sorted true
    sortTable.setSorted(true);
    // set sorted index
    sortTable.setIndexOfSortedColumn(indexOfColumn);
    // set order
    sortTable.setOrderOfSortedColumn(order);
    // Sort table

    tableModel.sortTableByColumn(indexInTableModel, order);
    sortTable.validate();

  }//execute

  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class CancelCommand
