/**
 *  '$RCSfile: DeleteRowCommand.java,v $'
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

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

/**
 * Class to handle delete a row command
 */
public class DeleteRowCommand implements Command
{

    /**
     *Import Language into Morpho
     *by pstango 2010/03/15 
     */
    public static Language lan = new Language();
	
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;


  /**
   * Constructor of delete a row command
   */
  public DeleteRowCommand()
  {

  }


  /**
   * execute delete row command
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
    int opt = JOptionPane.showConfirmDialog(morphoFrame,
            /*"Are you sure that you want to delete the selected row?"*/ lan.getMessages("DeleteRowWarning") + "?",
            /*"DO YOU WANT TO CONTINUE?"*/ lan.getMessages("Warning_Continue") + "?",
            JOptionPane.YES_NO_OPTION);
   if (opt == JOptionPane.NO_OPTION) 
   {
        return;
    }

    // make sure resulPanel is not null
    if (resultPane != null)
    {
       DataViewer dataView = resultPane.getCurrentDataViewer();
       if (dataView != null)
       {
         // Get parameters and run it
         JTable jtable=dataView.getDataTable();
         PersistentTableModel ptmodel=(PersistentTableModel)jtable.getModel();
         deleteRow(jtable, ptmodel);
       }

    }//if

  }//execute



  /* Method to insert a new row into table */
  private void deleteRow(JTable table, PersistentTableModel ptm)

  {
    int sel = table.getSelectedRow();
    if (sel>-1)
    {
       ptm.deleteRow(sel);
    }

  }//insert row

  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class CancelCommand
