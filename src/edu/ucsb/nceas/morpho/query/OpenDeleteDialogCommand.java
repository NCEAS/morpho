/**
 *  '$RCSfile: OpenDeleteDialogCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: cjones $'
 *     '$Date: 2002-09-26 01:57:53 $'
 * '$Revision: 1.3 $'
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


import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;


/**
 * Class to handle Open a dialog box command
 */
public class OpenDeleteDialogCommand implements Command 
{
  /** A reference to OpenDialogBox */
  private OpenDialogBox openDialog = null;

  /**
   * Constructor of OpenDeleteDialogCommand
   */
  public OpenDeleteDialogCommand()
  {
   
  }//OpenDialogBoxCommand
  
  /**
   * Constructor of OpenDeleteDialogCommand
   *
   * @param myDialog the open dialog box will be applied this command
   */
  public OpenDeleteDialogCommand(OpenDialogBox myDialog)
  {
    openDialog = myDialog;
  }//OpenDialogBoxCommand
  /**
   * execute cancel command
   */    
  public void execute(ActionEvent event)
  {
    ResultPanel resultPane = null;
    MorphoFrame frame  = null;
    boolean parentIsOpenDialog = false;
    // Get result panle from open dialog if open dialog is not null
    if ( openDialog != null)
    {
      resultPane = openDialog.getResultPanel();
      frame = openDialog.getParentFrame();
      parentIsOpenDialog = true;
    }
    else
    {
      // Get result panel from current active frame
      frame = UIController.getInstance().getCurrentActiveWindow();
      if ( frame != null)
      {
        resultPane = RefreshCommand.getResultPanelFromMorphoFrame(frame);
      }//if
    }//else
    
    // make sure the resultPane is not null
    if ( resultPane != null)
    {
        String selectDocId = resultPane.getSelectedId();
        boolean inNetwork = resultPane.getMetacatLocation();
        boolean inLocal = resultPane.getLocalLocation();
      
        // Make sure selected a id, and there no package in metacat
        if ( selectDocId != null && !selectDocId.equals(""))
        {
          // Show synchronize dialog
          DeleteDialog deleteDialog = null;
          if (parentIsOpenDialog)
          {
            deleteDialog = 
           new DeleteDialog(openDialog, frame, selectDocId, inLocal, inNetwork);
          }
          else
          {
            deleteDialog = new DeleteDialog(frame, selectDocId, inLocal, inNetwork);
          }
          deleteDialog.setModal(true);
          deleteDialog.setVisible(true);
        }
     
    }//if
      
    
  }//execute

 
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class OpenDialogBoxCommand
