/**
 *  '$RCSfile: LocalToNetworkCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-10-24 00:17:38 $'
 * '$Revision: 1.14 $'
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

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
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
import edu.ucsb.nceas.morpho.util.SaveEvent;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * Class to handle upload a package to network command
 */
public class LocalToNetworkCommand implements Command 
{
    
  /** A reference to the synchronize dialog */
   private JDialog synchronizeDialog = null;
 
  /** A reference to the open dialog */
   private OpenDialogBox openDialog = null;
   
  /** A reference to the MorphoFrame */
   private MorphoFrame morphoFrame = null;
   
  /** A flag indicate the frame 'type, search result or data packag*/
   private String morphoFrameType = null;
   
  /** A refernce to the ResultPanel */
   private ResultPanel resultPane = null;
  
  /** selected docid to synchronize */
  String selectDocId = null;
  
  /** flag to indicate selected data package has local copy */
  private boolean inLocal = false;
  
  /** flag to indicate selected data package has local copy */
  private boolean inNetwork = false;
  
  /** flag to indiecate synchronize will apply to a open dialog*/
  private boolean comeFromOpenDialog = false;
  /**
   * Constructor of LocalToNetworkCommand in dialog
   * @param myOpenDialog the open dialog which will be applied synchronize
   * @param dialog a synchronize dialog need to be destroied
   * @param myFrame the parent frame of synchronize dialog
   * @param frameType the parent frame'type, search result or package
   * @param selectId the id of data package need to be synchronized
   * @param myInLocal if the datapackage has a local copy
   * @param myInNetwork if the datapackage has a network copy
   */
  public LocalToNetworkCommand(OpenDialogBox myOpenDialog, JDialog box, 
                               MorphoFrame myFrame, String frameType,
                               String selectId, boolean myInLocal, 
                               boolean myInNetwork)
  {
    if (myOpenDialog != null)
    {
      // this is for open dialog
      openDialog = myOpenDialog;
      comeFromOpenDialog = true;
    }
    else
    {
      // for morpho frame
      morphoFrameType = frameType;
    }
    synchronizeDialog = box;
    morphoFrame = myFrame;
    selectDocId = selectId;
    inLocal = myInLocal;
    inNetwork = myInNetwork;
  }//LocalToNetworkCommand
  
 
  /**
   * execute open package command
   */    
  public void execute(ActionEvent event)
  {
    if (selectDocId != null && !selectDocId.equals("") && !inNetwork && inLocal)
    {
        // If synchronize dialog is null, destory it.
        if (synchronizeDialog != null)
        {
          synchronizeDialog.setVisible(false);
          synchronizeDialog.dispose();
          synchronizeDialog = null;
        }
        doUpload(openDialog, comeFromOpenDialog, morphoFrame);
    }
   
  }//execute

  /**
   * Using SwingWorket class to open a package
   *
   */
 private void doUpload(final OpenDialogBox open, final boolean hasOpen, 
                       final MorphoFrame frame) 
 {
  final SwingWorker worker = new SwingWorker() 
  {
        DataPackageInterface dataPackage;
        
        // A variable to indicate it reach refresh command or not
        // This is for butterfly flapping, if reach refresh, butterfly will
        // stop flapping by refresh        
        boolean refreshFlag = false;
        public Object construct() {
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
          frame.setBusy(true);
          // Create a refresh command 
          RefreshCommand refresh = null; 
          if (hasOpen)
          {
            refresh = new RefreshCommand(open);
          }
          else
          {
            refresh = new RefreshCommand(frame);
          }
      
           //upload the current selection to metacat
            Log.debug(20, "Uploading package.");
            try
            {
            	
              String metacatDocid = dataPackage.upload(selectDocId);
              if (metacatDocid != null && !metacatDocid.equals(selectDocId))
              {
            	  // create a local copy for the new document.
            	  //dataPackage.download(metacatDocid);
            	  JOptionPane.showMessageDialog(morphoFrame, ""+selectDocId+ " exists in metacat and morpho assigns new docid "+
            			  metacatDocid+ " for it.", Language.getInstance().getMessage("Information"),
                          JOptionPane.INFORMATION_MESSAGE);
              }
              
              SaveEvent saveEvent = new SaveEvent(morphoFrame, StateChangeEvent.SAVE_DATAPACKAGE);
              saveEvent.setSynchronize(true);
              saveEvent.setInitialId(selectDocId);
              saveEvent.setFinalId(metacatDocid);
              saveEvent.setLocation(DataPackageInterface.NETWORK);
              StateChangeMonitor.getInstance().notifyStateChange(saveEvent);
              
              refreshFlag = true;
              
              if ( comeFromOpenDialog || (morphoFrameType != null &&
                      morphoFrameType.equals(morphoFrame.SEARCHRESULTFRAME)))
              {
                // for search result   
                refresh.execute(null);
              }
              else if (morphoFrameType != null &&
                     morphoFrameType.equals(morphoFrame.DATAPACKAGEFRAME))
              {
                // for data package frame
                refreshDataPackageFrame(metacatDocid);
              }
            }
            catch(MetacatUploadException mue)
            {
              String errorMsg = mue.getMessage();
              //if (errorMsg.indexOf("Invalid content")>-1) { // validation problem
                Log.debug(1,"Synchonization failed!");
                Log.debug(20, "MetacatUploadException: "+errorMsg);
                return null;
              
            }
          
          
        return null;  
        }

        /*
         * Method to refresh a open datackage 
         */
        private void refreshDataPackageFrame(String docid)
        {
          // the location of data package after synchronize
          String location = DataPackageInterface.BOTH;
          if (docid != null && !docid.equals(selectDocId))
          {
          	location = DataPackageInterface.NETWORK;
          }
          dataPackage.openDataPackage(location, docid, null, null, null);
          
          // Distroy old frame
          UIController.getInstance().removeWindow(frame);
          frame.dispose();
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

}