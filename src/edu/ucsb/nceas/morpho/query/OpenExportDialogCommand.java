/**
 *  '$RCSfile: OpenExportDialogCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-12-03 23:24:34 $'
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
 * Class to handle Open a dialog box command
 */
public class OpenExportDialogCommand implements Command 
{
  /** A reference to OpenDialogBox */
  private OpenDialogBox openDialog = null;
  
 
  /**
   * Constructor of OpenExportDialogCommand
   */
  public OpenExportDialogCommand()
  {

  }//OpenDialogBoxCommand
  
  /**
   * Constructor of OpenExportDialogCommand
   *
   * @param myDialog the open dialog box will be applied this command
   */
  public OpenExportDialogCommand(OpenDialogBox myDialog)
  {
    openDialog = myDialog;
  }//OpenDialogBoxCommand
  /**
   * execute open export dialog command
   */    
  public void execute(ActionEvent event)
  {
    ResultPanel resultPane     = null;
    MorphoFrame frame          = null;
    ExportDialog exportDialog  = null;
    boolean parentIsOpenDialog = false;
    String selectDocId         = null;
    boolean inNetwork          = false;
    boolean inLocal            = false;
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

        selectDocId = resultPane.getSelectedId();
        inNetwork = resultPane.getMetacatLocation();
        inLocal = resultPane.getLocalLocation();
        // Make sure selected a id, and there no package in metacat
        if (openDialog==null) {
          // Show export dialog
            exportDialog = 
                     new ExportDialog(frame, MorphoFrame.DATAPACKAGEFRAME, 
                               selectDocId, inLocal, inNetwork);
          exportDialog.setModal(true);
          exportDialog.setVisible(true);
        }
        else if ( selectDocId != null && !selectDocId.equals("") )
        {
          // Show export dialog
            exportDialog = 
              new ExportDialog(openDialog, frame, 
                               selectDocId, inLocal, inNetwork);
          exportDialog.setModal(true);
          exportDialog.setVisible(true);
        }
     
    }//if
    else
    {
      // if resultPane is null, try if the morpho frame is data package frame
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
       // Make sure selected a id, and there is local pacakge
      if ( selectDocId != null && !selectDocId.equals(""))
      {
        
        exportDialog = 
              new ExportDialog(frame, MorphoFrame.DATAPACKAGEFRAME, 
                               selectDocId, inLocal, inNetwork);
        exportDialog.setModal(true);
        exportDialog.setVisible(true);
        
      }//if
     
    }//else
      
    
  }//execute

 
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class OpenDialogBoxCommand
