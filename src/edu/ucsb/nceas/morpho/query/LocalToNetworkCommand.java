/**
 *  '$RCSfile: LocalToNetworkCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-05 18:29:57 $'
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

import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
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
 * Class to handle upload a package to network command
 */
public class LocalToNetworkCommand implements Command 
{
    
  /** A reference to the dialog */
   private JDialog dialog = null;
   
  /** A reference to the MorphoFrame */
   private MorphoFrame morphoFrame = null;
   
  /** A refernce to the ResultPanel */
   private ResultPanel resultPane = null;
  
  /** selected docid to synchronize */
  String selectDocId = null;
  
  /** flag to indicate selected data package has local copy */
  private boolean inLocal = false;
  
  /** flag to indicate selected data package has local copy */
  private boolean inNetwork = false;
    
  /**
   * Constructor of LocalToNetworkCommand in dialog
   * @param dialog a synchronize dialog need to be destroied
   * @param myFrame the parent frame of synchronize dialog
   * @param selectId the id of data package need to be synchronized
   * @param myInLocal if the datapackage has a local copy
   * @param myInNetwork if the datapackage has a network copy
   */
  public LocalToNetworkCommand(JDialog box, MorphoFrame myFrame, 
                      String selectId, boolean myInLocal, boolean myInNetwork)
  {
    dialog = box;
    morphoFrame = myFrame;
    selectDocId = selectId;
    inLocal = myInLocal;
    inNetwork = myInNetwork;
  }//LocalToNetworkCommand
  
 
  /**
   * execute open package command
   */    
  public void execute()
  {
    if (selectDocId != null && !selectDocId.equals("") && !inNetwork && inLocal)
    {
        // If dialog is null, destory it.
        if (dialog != null)
        {
          dialog.setVisible(false);
          dialog.dispose();
          dialog = null;
        }
        doUpload(selectDocId, morphoFrame);
    }
   
  }//execute

  /**
   * Using SwingWorket class to open a package
   *
   */
 private void doUpload(final String docid, final MorphoFrame frame) 
 {
  final SwingWorker worker = new SwingWorker() 
  {
        // A variable to indicate it reach refresh command or not
        // This is for butterfly flapping, if reach refresh, butterfly will
        // stop flapping by refresh        
        boolean refreshFlag = false;
        public Object construct() {
          frame.setBusy(true);
          DataPackageInterface dataPackage;
          // Create a refresh command 
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
            Log.debug(6, "Error in upload");
            return null;
          }
          
          { //upload the current selection to metacat
            Log.debug(20, "Uploading package.");
            try
            {
              dataPackage.upload(docid, false);
              refreshFlag = true;
              refresh.execute();
            }
            catch(MetacatUploadException mue)
            {
              //ask the user if he is sure he wants to overwrite the package
              //if he is do it, otherwise return
              String message="A conflict has been found in one or more of the "+ 
               "identifiers \nin your package.  It is possible that you or \n" + 
               "someone else has made a change on the server that has not \n" +
               "been reflected on your local copy. If you proceed, you may \n" +
              "overwrite package information. If you proceed the identifier \n"+
               "for this package will be changed.  Are you sure you want to \n"+
               "proceed with the upload?";
              int choice = JOptionPane.YES_OPTION;
              choice = JOptionPane.showConfirmDialog(frame, message, 
                                 "Morpho", 
                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                 JOptionPane.WARNING_MESSAGE);
              if(choice == JOptionPane.YES_OPTION)
              {
                try
                {
                  dataPackage.upload(docid, true);
                  refreshFlag = true;
                  refresh.execute();
                }
                catch(MetacatUploadException mue2)
                {
                  Log.debug(0, mue2.getMessage());
                  
                }
              }
              else
              {
                
                return null;
              }
            }
          }
          
        return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() 
        {
          // refresh will Stop butterfly, so here we don't need.
          if (!refreshFlag)
          {
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
