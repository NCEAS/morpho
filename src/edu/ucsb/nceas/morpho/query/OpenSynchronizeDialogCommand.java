/**
 *  '$RCSfile: OpenSynchronizeDialogCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-31 00:27:22 $'
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


import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import javax.swing.JDialog;


/**
 * Class to handle Open a dialog box command
 */
public class OpenSynchronizeDialogCommand implements Command 
{
  

  /**
   * Constructor of SearchCommand
   *
   * @param morpho the Morpho app to which the cancel command will apply
   */
  public OpenSynchronizeDialogCommand()
  {
   
  }//OpenDialogBoxCommand
  
  
  /**
   * execute cancel command
   */    
  public void execute()
  {
    MorphoFrame frame = UIController.getInstance().getCurrentActiveWindow();
    if ( frame != null)
    {
       ResultPanel resultPane = 
                 RefreshCommand.getResultPanelFromMorphoFrame(frame);
    
       // make sure the resultPane is not null
      if ( resultPane != null)
      {
        String selectDocId = resultPane.getSelectedId();
        boolean inNetwork = resultPane.getMetacatLocation();
        boolean inLocal = resultPane.getLocalLocation();
      
        // Make sure selected a id, and there no package in metacat
        if ( selectDocId != null && !selectDocId.equals("") && 
                                                      !(inLocal && inNetwork))
        {
          // Show synchronize dialog
          SynchronizeDialog dialog = 
                new SynchronizeDialog(frame, selectDocId, inLocal, inNetwork);
          dialog.setModal(true);
          dialog.setVisible(true);
        }
      }
    }//if
      
    
  }//execute

 
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class OpenDialogBoxCommand
