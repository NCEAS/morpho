/**
 *  '$RCSfile: OpenPreviousVersionCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-10-01 23:26:18 $'
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


import java.awt.event.ActionEvent;
import java.util.List;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * Class to handle open a previous version package command
 */
public class OpenPreviousVersionCommand implements Command 
{
    
  /** A reference to the dialog */
   private OpenDialogBox dialog = null;
   
  /** A reference to the MorphoFrame */
   private MorphoFrame morphoFrame = null;
   
  /** A refernce to the ResultPanel */
   private ResultPanel resultPane = null;
   
  /** A refernce to the Morpho */
  private Morpho morpho = null;
   
  
   /** packageName, docid without version */
   String packageName = null;
   /** version number */
   List<String> versions = null;
   /** DataPackage in network */
   //boolean metacatLoc = false;
   /** DataPackage in local */
   boolean localLoc = false;
  
   /** reference to datapackage interface*/
   DataPackageInterface dataPackage = null;
    
  /**
   * Constructor of OpenPreviousVersionCommand
   * @param dialog a open previous version command will be happened 
   * at this dialog 
   * @param myMorpho the morpho reference
   */
  public OpenPreviousVersionCommand(OpenDialogBox box, Morpho myMorpho)
  {
    dialog = box;
    morpho = myMorpho;
  
  }//LocalToNetworkCommand
  
 
  /**
   * execute delete local package command
   */    
  public void execute(ActionEvent event)
  {
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
    // If dialog is null, get resultPanel from dialog
    if (dialog != null)
    {
      // This command will apply to a dialog
      morphoFrame = dialog.getParentFrame();
      resultPane = dialog.getResultPanel();
     
    }
    // If dialog is null, get resultPanel from current active window
    else
    {
      // If the command would not applyto a dialog, moreFrame will be set to be
      // current active morphoFrame
      morphoFrame = UIController.getInstance().getCurrentActiveWindow();
      resultPane = RefreshCommand.getResultPanelFromMorphoFrame(morphoFrame);
     }
    
    if (resultPane != null)
    {
      packageName = resultPane.getPackageName();
      versions = resultPane.getPreviousVersions();
      //metacatLoc = resultPane.getMetacatLocation();
      String localStatus = resultPane.getLocalStatus();
      if(localStatus != null && (localStatus.equals(DataPackageInterface.LOCAL) ||
          localStatus.equals(QueryRefreshInterface.LOCALAUTOSAVEDINCOMPLETE)||
          localStatus.equals(QueryRefreshInterface.LOCALUSERSAVEDINCOMPLETE)))
      {
        localLoc = true;
      }
      else
      {
        localLoc = false;
      }
      
    
    }//if
    else
    {
      //Try a data pakcage frame
      String docId = dataPackage.getDocIdFromMorphoFrame(morphoFrame);
      localLoc = DataStoreServiceController.getInstance().exists(docId, DataPackageInterface.LOCAL);
      packageName = docId; // just use the given id as title
      try {
    	  String location = UIController.getInstance().getCurrentAbstractDataPackage().getAbstractDataPackage().getLocation();
    	  versions = DataStoreServiceController.getInstance().getAllRevisions(docId, location);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      
    }
    
    // Make sure selected a id, and there is local pacakge
    if ( packageName != null && !packageName.equals("") && versions != null)
    {
        // If it is dialog, destroied it 
        if ( dialog != null)
        {
          dialog.setVisible(false);
          dialog.dispose();
          dialog = null;
        }
        doOpenPreviousVersion
                          (packageName, versions, morpho, localLoc, morphoFrame);
    }
    
  }//execute

  /**
   * Using SwingWorket class to delete a local package
   *
   */
 private void doOpenPreviousVersion(final String name, final List<String> identifiers,
          final Morpho myMorpho, final boolean inLocal, final MorphoFrame frame) 
 {
  final SwingWorker worker = new SwingWorker() 
  {
        public Object construct() 
        {
          if (frame!= null)
          {
            frame.setBusy(true);
          }
       
          // create dialog for openning previous version
          dataPackage.createOpenPreviousVersionDialog(name, identifiers, myMorpho,inLocal);
          return null;  
          
        }

        //Runs on the event-dispatching thread.
        public void finished() 
        {
          // Stop butterfly
          if ( frame != null)
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
