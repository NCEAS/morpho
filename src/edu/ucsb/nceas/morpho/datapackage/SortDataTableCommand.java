/**
 *  '$RCSfile: SortDataTableCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-04-06 01:27:05 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.event.ActionEvent;

import javax.swing.JTable;


/**
 * Class to handle sort data table command
 */
public class SortDataTableCommand implements Command
{
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;


  /**
   * Constructor of sort data table command
   */
  public SortDataTableCommand()
  {

  }


  /**
   * execute sort data table command
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
       if (dataView != null)
       {
         // Get parameters and run it
         JTable jtable=dataView.getDataTable();
         PersistentTableModel ptmodel=(PersistentTableModel)jtable.getModel();
         PersistentVector vector=dataView.getPV();
         int direction = dataView.getSortDirection();
         sort(morphoFrame, dataView, jtable, ptmodel, vector, direction);
       }

    }//if

  }//execute



  /* Method to sort a column into table */
  private void sort(MorphoFrame frame, DataViewer viewer,  JTable table,
              PersistentTableModel ptm, PersistentVector pv, int sortdirection)
  {
    frame.setBusy(true);
    int colsel = table.getSelectedColumn();
    int rowsel = table.getSelectedRow();
    if (colsel>-1) {
      ptm.sort(colsel, sortdirection);
      pv = ptm.getPersistentVector();
      viewer.setPV(pv);
      sortdirection = -1 * sortdirection;
      viewer.setSortDirection(sortdirection);
      table.setColumnSelectionInterval(colsel, colsel);
      table.setRowSelectionInterval(rowsel, rowsel);
    }//if
    else {
      Log.debug(45, "No selection!");
    }
    frame.setBusy(false);

  }//sort


  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class CancelCommand
