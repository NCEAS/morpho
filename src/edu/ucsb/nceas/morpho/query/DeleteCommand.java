/**
 *  '$RCSfile: DeleteCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-10-01 21:51:00 $'
 * '$Revision: 1.10 $'
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
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Class to handle delete local copy command
 */
public class DeleteCommand implements Command 
{
    
  /** A reference to the delete dialog */
   private JDialog deleteDialog = null;
   
  /** A reference to the open dialog */
   private OpenDialogBox openDialog = null;
   
  /** A reference to the MorphoFrame */
   private MorphoFrame morphoFrame = null;
   
  /** A String indicating the morpho frame' type*/
   String morphoFrameType = null;
   
  /** A refernce to the ResultPanel */
   private ResultPanel resultPane = null;
   
  /** State of the delete */
  private String state = null;
  
  /** Warning message */
  private String message = null;
  
  /** Constant String Warning message for warning message */
  private static final String LOCALWARNING = 
                          "Are you sure you want to delete \nthe package from "
                            + "your local file system?";
  private static final String NETWORKWARNING = 
                        "Are you sure you want to delete \nthe package from " +
                        "Metacat? You \nwill not be able to upload \nit " +
                        "again with the same identifier.";
  private static final String BOTHWARNING =
                        "Are you sure you want to delete \nthe package from " +
                        "Metacat and your \nlocal file system? " +
                        "Deleting a package\n cannot be undone!";
  /** selected docid to delete */
  String selectDocId = null;
  
  /** flag to indicate a deletion can be execute, it depends package location */
  private boolean execute = false;
  
  /** flag to indicate selected data package has local copy */
  private boolean inLocal = false;
  
  /** flag to indicate selected data package has local copy */
  private boolean inNetwork = false;
 
  /** flag for if the delete command come from a open dialog */
  private boolean comeFromOpenDialog = false;
  
  DataPackageInterface dataPackage = null;
  
  /** Title string for blank window*/
  private String BLANK = "Blank";
  /** index for blank window */
  private static int index = 1;
  /**
   * Constructor of DeleteCommand
   * @param myOpenDialog the open dialog which will be applied delete action 
   * @param myDeleteDialog a delete dialog need to be destroied
   * @param myFrame the parent frame of delete dialog or parent of open dialog
   * @param frameType the parent frame's type, search result or datapackage
   * @param selectId the id of data package need to be deleted
   * @param myState which deletion will happend, local, network or both
   * @param myInLocal if the datapackage has a local copy
   * @param myInNetwork if the datapackage has a network copy
   */
  public DeleteCommand(OpenDialogBox myOpenDialog, JDialog myDeleteDialog, 
                      MorphoFrame myFrame, String frameType,  String myState, 
                      String selectId, boolean myInLocal, boolean myInNetwork)
  {
    if ( myOpenDialog != null)
    { 
      openDialog = myOpenDialog;
      comeFromOpenDialog = true;
      // this come from a open dialog, so we can selt morphoFrameType = null
      morphoFrameType = null;
    }
    else
    {
      // this come from a morpho frame, we need to know it's type
      morphoFrameType = frameType;
    }
    
    deleteDialog = myDeleteDialog;
    morphoFrame = myFrame;
    selectDocId = selectId;
    state = myState;
    inLocal = myInLocal;
    inNetwork = myInNetwork;
  
  }//LocalToNetworkCommand
  
 
  /**
   * execute delete local package command
   */    
  public void execute(ActionEvent event)
  {
    
      if (state.equals(DataPackageInterface.LOCAL))
      {
        // Delete local copy
        message = LOCALWARNING;
        // If has local copy, can execute delete local
        execute = inLocal;
      }
      else if (state.equals(DataPackageInterface.METACAT))
      {
        // Delete network copy
        message = NETWORKWARNING;
        // If has network copy can delete network copy
        execute = inNetwork;
      } 
      else if (state.equals(DataPackageInterface.BOTH))
      { 
       // Delete both
       message = BOTHWARNING;
       // if has both copy can delete both.
       execute = inLocal && inNetwork;
      }
      else
      {
        Log.debug(20, "Unkown deletion command!");
      }
      
      // Make sure selected a id, and there is local pacakge
      if ( selectDocId != null && !selectDocId.equals("") && execute)
      {
        // Destroy the delete dialog
        if (deleteDialog != null)
        {
          deleteDialog.setVisible(false);
          deleteDialog.dispose();
          deleteDialog = null;
        }
        doDelete(selectDocId, openDialog);
      }
   
  }//execute

  /**
   * Using SwingWorket class to delete a local package
   *
   */
 private void doDelete(final String docid, final OpenDialogBox open) 
 {
  final SwingWorker worker = new SwingWorker() 
  {
        // A variable to indicate it reach refresh command or not
        // This is for butterfly flapping, if reach refresh, butterfly will
        // stop flapping by refresh
        boolean refreshFlag = false;
        public Object construct() 
        {
          if (morphoFrame!=null)
          {
            morphoFrame.setBusy(true);
          }
          // Create a refresh command finished
          RefreshCommand refresh = null;
          if (comeFromOpenDialog)
          {
            refresh = new RefreshCommand(open);
          }
          else
          {
            refresh = new RefreshCommand(morphoFrame);
          }
          
          try 
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            Log.debug(6, "Error in delete");
            return null;
          }
          
          // find the location of delete
                    
		      //delete the local package
          Log.debug(20, "Deleteing the package.");
          int choice = JOptionPane.YES_OPTION;
          // come from open dialog
          if (comeFromOpenDialog)
          {
             choice = JOptionPane.showConfirmDialog(open, message, 
                               "Morpho", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
          }
          else
          {
            // For morpho frame
            choice = JOptionPane.showConfirmDialog(morphoFrame, message, 
                               "Morpho", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
          }
                               
          if(choice == JOptionPane.YES_OPTION)
          {
            // this is for open dialg box or search result frame
            if ( comeFromOpenDialog || (morphoFrameType != null &&
                  morphoFrameType.equals(morphoFrame.SEARCHRESULTFRAME)))
            {
              
              dataPackage.delete(docid, state);
              refreshFlag = true;
              refresh.execute(null);
            }
            else if (morphoFrameType != null &&
                     morphoFrameType.equals(morphoFrame.DATAPACKAGEFRAME))
            {
              //Fore data package frame
              morphoFrame.setBusy(true);
              refreshFlag = true;
              dataPackage.delete(docid, state);
              refreshDataPackageFrame();
              
            }
          }
         
           return null;  
          
        }
        
        /*
         * Method to refresh a open datackage 
         */
        private void refreshDataPackageFrame()
        {
          // Distroy old frame
          UIController.getInstance().removeWindow(morphoFrame);
          morphoFrame.dispose();
          morphoFrame = null;
          // if pakcage have local and network copy and not delete both
          // reopen the package
          if ( inLocal && inNetwork && !state.equals(DataPackageInterface.BOTH))
          {
           
            // the location of data package after deleting
            String location = null;
            if (state.equals(DataPackageInterface.LOCAL))
            {
              location = DataPackageInterface.METACAT;
            }
            else
            {
              location = DataPackageInterface.LOCAL;
            }
            dataPackage.openDataPackage(location, selectDocId, null, null);
            
          }
          else
          {
            // create a new blank frame
            String title = BLANK + index;
            MorphoFrame newFrame = UIController.getInstance().addWindow(title);
            newFrame.setVisible(true);
            index++;
            
          } 
          //
        }
        
        //Runs on the event-dispatching thread.
        public void finished() 
        {
          if (morphoFrame!=null && !refreshFlag)
          {
            // Refresh will stop butterfly flapping. So here we don't need
            morphoFrame.setBusy(false);
          }
        }
    };//final
    worker.start();  //required for SwingWorker 3
  
  }
 
   /**
    * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
