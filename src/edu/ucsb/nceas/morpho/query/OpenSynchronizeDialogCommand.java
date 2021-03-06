/**
 *  '$RCSfile: OpenSynchronizeDialogCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-01-26 21:50:07 $'
 * '$Revision: 1.5 $'
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

import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;


/**
 * Class to handle Open a synchronize (upload or download) dialog box command
 */
public class OpenSynchronizeDialogCommand implements Command 
{
  /** A reference to OpenDialogBox */
  private OpenDialogBox openDialog = null;
  
  /**
   * Constructor of open synchronize dialog command
   *
   */
  public OpenSynchronizeDialogCommand()
  {
   
  }//OpenSynchronizeDialogBoxCommand
  
  /**
   * Constructor of OpenSynchronizeDialogCommand
   *
   * @param myDialog the open dialog box will be applied this command
   */
  public OpenSynchronizeDialogCommand(OpenDialogBox myDialog)
  {
    openDialog = myDialog;
  }//OpenDialogBoxCommand
  
  /**
   * execute cancel command
   */    
  public void execute(ActionEvent event)
  {
    ResultPanel resultPane     = null;
    MorphoFrame frame          = null;
    boolean parentIsOpenDialog = false;
    String selectDocId         = null;
    boolean inNetwork          = false;
    boolean inLocal            = false;
    String frameType           = null;
    if (openDialog != null)
    {
      // This is for parent is a open dialog
      parentIsOpenDialog = true;
      resultPane = openDialog.getResultPanel();
      frame = openDialog.getParentFrame();
    }//if
    else
    {
      // this is for parent is current active frame
      frame = UIController.getInstance().getCurrentActiveWindow();
      if ( frame != null)
      { 
        resultPane = RefreshCommand.getResultPanelFromMorphoFrame(frame);
      }//if
    }//else
    
    // make sure the resultPane is not null
    if ( resultPane != null)
    {
      selectDocId = resultPane.getSelectedId();
      inNetwork = resultPane.getMetacatLocation();
      inLocal = resultPane.getLocalLocation();
      frameType = MorphoFrame.SEARCHRESULTFRAME;
    
    }//if
    else
    {
      // To try data package frame
      DataPackageInterface dataPackage = null;
      try 
      {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider = 
                   services.getServiceProvider(DataPackageInterface.class);
        dataPackage = (DataPackageInterface)provider;
      } 
      catch (ServiceNotHandledException snhe) 
      {
        Log.debug(6, snhe.getMessage());
        return;
      }
       //Try if it is datapackage frame
      selectDocId = dataPackage.getDocIdFromMorphoFrame(frame);
      inNetwork   = DataStoreServiceController.getInstance().exists(selectDocId, DataPackageInterface.NETWORK);
      inLocal     = DataStoreServiceController.getInstance().exists(selectDocId, DataPackageInterface.LOCAL);
      frameType   = MorphoFrame.DATAPACKAGEFRAME;      
    }//else
    
    // Make sure selected a id, and there no package in metacat
    if ( selectDocId != null && !selectDocId.equals("") && 
                                                      !(inLocal && inNetwork))
    {
      if ((!inLocal) && (!inNetwork)) {
        Log.debug(1, "Cannot synchronize unsaved package!");
        return;
      }
        // Show synchronize dialog
        SynchronizeDialog synchronizeDialog = null;
        if (parentIsOpenDialog)
        {
          synchronizeDialog = new SynchronizeDialog
                           (openDialog, frame, selectDocId, inLocal, inNetwork);
        }
        else 
        {
          synchronizeDialog = new SynchronizeDialog
                            (frame, frameType, selectDocId, inLocal, inNetwork);
        }
        synchronizeDialog.setModal(true);
        synchronizeDialog.setVisible(true);
     }//if
    
      
    
  }//execute

 
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class OpenDialogBoxCommand
