/**
 *  '$RCSfile: OpenPreviousVersionCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-09-15 19:34:00 $'
 * '$Revision: 1.2.4.1 $'
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
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

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
   int version = -1;
   /** DataPackage in network */
   boolean metacatLoc = false;
   /** DataPackage in local */
   boolean localLoc = false;
  
  
    
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
      version = resultPane.getVersion();
      metacatLoc = resultPane.getMetacatLocation();
      localLoc = resultPane.getLocalLocation();
  
      // Make sure selected a id, and there is local pacakge
      if ( packageName != null && !packageName.equals("") && version != -1)
      {
        
        // If it is dialog, destroied it 
        if ( dialog != null)
        {
          dialog.setVisible(false);
          dialog.dispose();
          dialog = null;
        }
        doOpenPreviousVersion
                          (packageName, version, morpho, localLoc, morphoFrame);
      }
    }//if
    
  }//execute

  /**
   * Using SwingWorket class to delete a local package
   *
   */
 private void doOpenPreviousVersion(final String name, final int vers,
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
             
           DataPackageInterface dataPackage;
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
            return null;
          }
          // create dialog for openning previous version
          dataPackage.createOpenPreviousVersionDialog
                                                (name, vers, myMorpho,inLocal);
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
