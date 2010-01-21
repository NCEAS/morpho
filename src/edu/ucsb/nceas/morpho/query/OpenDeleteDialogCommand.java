/**
 *  '$RCSfile: OpenDeleteDialogCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-10-01 21:52:19 $'
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
package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
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
   * execute open delete dialog command
   */    
  public void execute(ActionEvent event)
  {
    ResultPanel resultPane     = null;
    MorphoFrame frame          = null;
    DeleteDialog deleteDialog  = null;
    boolean parentIsOpenDialog = false;
    String selectDocId         = null;
    String networkStatus          = null;
    String localStatus           = null;
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
        networkStatus = resultPane.getMetacatStatus();
        localStatus = resultPane.getLocalStatus();
        /*Log.debug(5, "local status is "+localStatus+
            "\nnewtwork status is "+networkStatus+"\non search result.openDeleteDialogCommand");*/
         
        // Make sure selected a id, and there no package in metacat
        if ( selectDocId != null && !selectDocId.equals(""))
        {
          // Show delete dialog
          if (parentIsOpenDialog)
          {
            deleteDialog = 
           new DeleteDialog(openDialog, frame, selectDocId, localStatus, networkStatus);
          }
          else
          {
            deleteDialog = 
              new DeleteDialog(frame, MorphoFrame.SEARCHRESULTFRAME, 
                               selectDocId, localStatus, networkStatus);
          }
          deleteDialog.setModal(true);
          deleteDialog.setVisible(true);
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
      boolean inNetwork   = dataPackage.isDataPackageInNetwork(frame);
      boolean inLocal     = dataPackage.isDataPackageInLocal(frame);
      if(inNetwork)
      {
        networkStatus = DataPackageInterface.METACAT;
      }
      else
      {
        networkStatus = QueryRefreshInterface.NONEXIST;
      }
      if(inLocal)
      {
        localStatus = DataPackageInterface.LOCAL;
      }
      else
      {
        localStatus = QueryRefreshInterface.NONEXIST;
      }
      /*Log.debug(5, "local status is "+localStatus+
          "\nnewtwork status is "+networkStatus+"\non datapackage. openDeleteDialogCommand");*/

       // Make sure selected a id, and there is local pacakge
      if ( selectDocId != null && !selectDocId.equals(""))
      {
        
        deleteDialog = 
              new DeleteDialog(frame, MorphoFrame.DATAPACKAGEFRAME, 
                               selectDocId, localStatus, networkStatus);
        deleteDialog.setModal(true);
        deleteDialog.setVisible(true);
        
      }//if
     
    }//else
      
    
  }//execute

 
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class OpenDialogBoxCommand
