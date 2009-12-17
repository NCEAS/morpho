/**
 *  '$RCSfile: TablePasteCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-18 02:21:40 $'
 * '$Revision: 1.2 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;

import java.util.StringTokenizer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;


/**
 * Class to handle table paste command
 */
public class TablePasteCommand implements Command
{
  /* target table*/
  protected JTable target;
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  /**
   * Constructor of paste command
   */
  public TablePasteCommand()
  {

  }

  public void setTarget(JTable tbl)
  {
    target = tbl;
  }

  public JTable getTarget()
  {
    return target;
  }


  /**
   * execute paste command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event)
  {
    DataViewContainerPanel resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null)
    {
       resultPane = morphoFrame.getDataViewContainerPanel();
    }//if

    // make sure resulPanel is not null
    if (resultPane != null)
    {
       DataViewer dataView = resultPane.getCurrentDataViewer();
       if (dataView != null && dataView.getTextFlag())
       {
         // Get parameters and run it
         JTable jtable=dataView.getDataTable();
         if ( jtable != null)
         {
           setTarget(jtable);
           paste(event);
         }
       }

    }//if

  }//execute



  /* Method to insert a new row into table */
  private void paste(ActionEvent evt)

  {
    JTable tbl = target;
    if (tbl == null) {
      tbl = (JTable)evt.getSource();
    }

    Transferable t =
      tbl.getToolkit().getSystemClipboard().getContents(null);
    if (t == null) {
      // No usable content
      return;
    }

    try {
      String sel = (String)t.getTransferData(DataFlavor.stringFlavor);

      // Break the selection into rows and then into columns.
      StringTokenizer rowTokenizer = new StringTokenizer(sel, "\n");
      int rowCount = rowTokenizer.countTokens();
      String[][] data = new String[rowCount][];

      // One pass of this loop for each row
      for (int i = 0; i < rowCount; i++) {
        String row = rowTokenizer.nextToken();
        StringTokenizer colTokenizer = new StringTokenizer(row, "\t");
        int colCount = colTokenizer.countTokens();
        String[] cols = new String[colCount];
        data[i] = cols;

        // One pass of this loop for each column
        for (int j = 0; j < colCount; j++) {
          cols[j] = colTokenizer.nextToken();
        }
      }

      // Install the data in the table
      applySelection(tbl, data);

      // the following only work on tables with tableModels that implement
      // AbstrctTableModel interface; without it an update of the display
      // will not occur
      ((AbstractTableModel)(tbl.getModel())).fireTableStructureChanged();
    } catch (Exception ex) {
      // Happens when clipboard content is not a string
    }

  }//insert row


  // Handles the entire selection as a two-dimensional array.
  // Subclasses may override this to provide custom processing.
  protected void applySelection(JTable tbl, String[][] data) {
    int row = tbl.getSelectedRow();
    int viewIndex = tbl.getSelectedColumn();
	int col =  tbl.getColumnModel().getColumn(viewIndex).getModelIndex();
    int selRows = tbl.getSelectedRowCount();
    int selCols = tbl.getSelectedColumnCount();
    int dataRows = data.length;
    int dataCols = (dataRows > 0 ? data[0].length : 0);

    // If only one cell is selected, allow the paste
    // to operate on the part of the table at and
    // below and to the right of that cell. Otherwise,
    // bound the paste to the selected area
    if (selRows == 1 && selCols == 1) {
      selRows = tbl.getRowCount() - row;
      selCols = tbl.getColumnCount() - col;
    }

    // Limit the paste operation to the selection,
    // or to the table, whichever is the smaller.
    if (dataRows > selRows) {
      dataRows = selRows;
    }
    if (dataCols > selCols) {
      dataCols = selCols;
    }

    // Apply each item in turn
    for (int i = 0; i < dataRows; i++) {
      for (int j = 0; j < dataCols; j++) {
              setValueAt(tbl, row + i, col + j, data[i][j]);
      }
    }

  }

  // Sets the value for a given row and column.
  // Subclasses may override this to carry out the
  // appropriate conversions for specific TableModels.
  protected void setValueAt(JTable tbl, int row, int column, String value) {
    tbl.setValueAt(value, row, column);
  }
  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class CancelCommand
