/**
 *  '$RCSfile: DeleteCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-05 18:24:01 $'
 * '$Revision: 1.6 $'
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
   private JDialog dialog = null;
   
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
  private boolean execute = false;
  
  /** flag to indicate selected data package has local copy */
  private boolean inLocal = false;
  
  /** flag to indicate selected data package has local copy */
  private boolean inNetwork = false;
 
  /**
   * Constructor of DeleteCommand
   * @param dialog a delete dialog need to be destroied
   * @param myFrame the parent frame of delete dialog
   * @param selectId the id of data package need to be deleted
   * @param myState which deletion will happend, local, network or both
   * @param myInLocal if the datapackage has a local copy
   * @param myInNetwork if the datapackage has a network copy
   */
  public DeleteCommand(JDialog box, MorphoFrame myFrame, String myState,
                      String selectId,  boolean myInLocal, boolean myInNetwork)
  {
    dialog = box;
    morphoFrame = myFrame;
    selectDocId = selectId;
    state = myState;
    inLocal = myInLocal;
    inNetwork = myInNetwork;
  
  }//LocalToNetworkCommand
  
 
  /**
   * execute delete local package command
   */    
  public void execute()
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
        // Destroy the dialog
        if (dialog != null)
        {
          dialog.setVisible(false);
          dialog.dispose();
          dialog = null;
        }
      
        doDelete(selectDocId, morphoFrame);
      }
   
  }//execute

  /**
   * Using SwingWorket class to delete a local package
   *
   */
 private void doDelete(final String docid, final MorphoFrame frame) 
 {
  final SwingWorker worker = new SwingWorker() 
  {
        // A variable to indicate it reach refresh command or not
        // This is for butterfly flapping, if reach refresh, butterfly will
        // stop flapping by refresh
        boolean refreshFlag = false;
        public Object construct() 
        {
          if (frame!=null)
          {
            frame.setBusy(true);
          }
          DataPackageInterface dataPackage;
          // Create a refresh command finished
          RefreshCommand refresh = new RefreshCommand(frame);
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
          choice = JOptionPane.showConfirmDialog(frame, message, 
                               "Morpho", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
                               
          if(choice == JOptionPane.YES_OPTION)
          {
            dataPackage.delete(docid, state);
            refreshFlag = true;
            refresh.execute();
          }
         
           return null;  
          
        }

        //Runs on the event-dispatching thread.
        public void finished() 
        {
          if (frame!=null && !refreshFlag)
          {
            // Refresh will stop butterfly flapping. So here we don't need
            frame.setBusy(false);
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
