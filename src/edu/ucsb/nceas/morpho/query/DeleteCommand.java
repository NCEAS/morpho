/**
 *  '$RCSfile: DeleteCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-28 17:48:18 $'
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
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Class to handle delete local copy command
 */
public class DeleteCommand implements Command 
{
    
  /** A reference to the dialog */
   private OpenDialogBox dialog = null;
   
  /** A reference to the MorphoFrame */
   private MorphoFrame morphoFrame = null;
   
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
  boolean execute = false;
 
  /**
   * Constructor of DeleteCommand
   * @param dialog a delete command will be happened at this dialog 
   * @param myState which deletion will happend, local, network or both
   */
  public DeleteCommand(OpenDialogBox box, String myState)
  {
    dialog = box;
    state = myState;
  
  }//LocalToNetworkCommand
  
 
  /**
   * execute delete local package command
   */    
  public void execute()
  {
    // Get frame and resultpanel depen on different situation
    if (dialog != null)
    {
      // This command will apply to a dialog
      morphoFrame = dialog.getParentFrame();
      resultPane = dialog.getResultPanel();
    }
    else
    {
      // If the command would not applyto a dialog, moreFrame will be set to be
      // current active morphoFrame
      morphoFrame = UIController.getInstance().getCurrentActiveWindow();
      resultPane = RefreshCommand.getResultPanelFromMorphoFrame(morphoFrame);
    }
    
    if (resultPane != null)
    {
      selectDocId = resultPane.getSelectedId();
      boolean inNetwork = resultPane.getMetacatLocation();
      boolean inLocal = resultPane.getLocalLocation();
      
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
        doDelete(selectDocId, morphoFrame, dialog);
      }
    }//if
    
  }//execute

  /**
   * Using SwingWorket class to delete a local package
   *
   */
 private void doDelete(final String docid, final MorphoFrame frame, 
                                                      final OpenDialogBox box) 
 {
  final SwingWorker worker = new SwingWorker() 
  {
        public Object construct() 
        {
          frame.setBusy(true);
          DataPackageInterface dataPackage;
          // Create a refresh command 
          RefreshCommand refresh = new RefreshCommand(box);
          try 
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            Log.debug(6, "Error in upload");
            return null;
          }
          
          // find the location of delete
                    
		      //delete the local package
          Log.debug(20, "Deleteing the package.");
          int choice = JOptionPane.YES_OPTION;
          choice = JOptionPane.showConfirmDialog(box, message, 
                               "Morpho", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
                               
          if(choice == JOptionPane.YES_OPTION)
          {
            dataPackage.delete(docid, state);
            refresh.execute();
          }
          
           return null;  
          
        }

        //Runs on the event-dispatching thread.
        public void finished() 
        {
          // Stop butterfly
          frame.setBusy(false);
        }
    };//final
    worker.start();  //required for SwingWorker 3
  
  }
 
   /**
    * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
