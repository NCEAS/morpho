/**
 *  '$RCSfile: NetworkToLocalCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-28 18:19:10 $'
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
 * Class to handle download a package from network command
 */
public class NetworkToLocalCommand implements Command 
{
    
  /** A reference to the dialog */
   private OpenDialogBox dialog = null;
   
  /** A reference to the MorphoFrame */
   private MorphoFrame morphoFrame = null;
   
  /** A refernce to the ResultPanel */
   private ResultPanel resultPane = null;
  
    
  /**
   * Constructor of NetworkToLocalCommand in dialog
   * @param dialog a upload command will be happened at this dialog 
   */
  public NetworkToLocalCommand(OpenDialogBox box)
  {
    dialog = box;
   
  }//LocalToNetworkCommand
  
 
  /**
   * execute open package command
   */    
  public void execute()
  {
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
    // make sure the resultPane is not null
    if ( resultPane != null)
    {
      String selectDocId = resultPane.getSelectedId();
      boolean inNetwork = resultPane.getMetacatLocation();
      boolean inLocal = resultPane.getLocalLocation();
      
      // Make sure selected a id, and there no package in metacat
      if ( selectDocId != null && !selectDocId.equals("") && !inLocal)
      {
        doDownload(selectDocId, morphoFrame, dialog);
      }
    }//if
    
  }//execute

  /**
   * Using SwingWorket class to download a package
   *
   */
 private void doDownload(final String docid, final MorphoFrame frame, 
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
          //download the current selection to the local disk
          Log.debug(20, "Downloading package.");
          dataPackage.download(docid);
          refresh.execute();
          
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
