/**
 *  '$RCSfile: ExportCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-29 00:52:22 $'
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
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Class to handle export package (both regular and zip) command
 */
public class ExportCommand implements Command 
{
    
  /** A reference to the dialog */
   private OpenDialogBox dialog = null;
   
  /** A reference to the MorphoFrame */
   private MorphoFrame morphoFrame = null;
   
  /** A refernce to the ResultPanel */
   private ResultPanel resultPane = null;
   
  /** Constant String to show state of delete */
  public static final String ZIP = "ZIP";
  public static final String REGULAR = "REGULAR";
 
  
  /** fromat of the export */
  private String format = null;
  /** selected docid to delete */
  String selectDocId = null;
  /** DataPackage in network */
  boolean metacatLoc = false;
  /** DataPackage in local */
  boolean localLoc = false;
  
  
    
  /**
   * Constructor of ExportCommand
   * @param dialog a export command will be happened at this dialog 
   * @param myFormat the format for export, regular or zip
   */
  public ExportCommand(OpenDialogBox box, String myFormat)
  {
    dialog = box;
    // Decide export format
    if (myFormat.equals(ZIP))
    {
      // zip format
      format = ZIP;
       
    }
    else if (myFormat.equals(REGULAR))
    {
       // regular format
       format = REGULAR;
    } 
    else
    {
       Log.debug(20, "Unkown export format!");
    }
    
  
  }//LocalToNetworkCommand
  
 
  /**
   * execute delete local package command
   */    
  public void execute()
  {
     // Get morphoframe and resultPanel if dialog is not null
    if (dialog != null)
    {
     // This command will apply to a dialog
      morphoFrame = dialog.getParentFrame();
      resultPane = dialog.getResultPanel();
     
    }
    else// If dialog is null, this means the resulpanel is in a morphoframe
    {
      // current active morphoFrame
      morphoFrame = UIController.getInstance().getCurrentActiveWindow();
      resultPane = RefreshCommand.getResultPanelFromMorphoFrame(morphoFrame);
    } 
    
    if (resultPane != null)
    {
      
      selectDocId = resultPane.getSelectedId();
      metacatLoc = resultPane.getMetacatLocation();
      localLoc = resultPane.getLocalLocation();
       // Make sure selected a id, and there is local pacakge
      if ( selectDocId != null && !selectDocId.equals(""))
      {
        // Destroy the dialog
        if (dialog != null)
        {
          dialog.setVisible(false);
          dialog.dispose();
          dialog = null;
        }
        doExport(selectDocId, morphoFrame);
        
      }//if
    }//if

  }//execute

  /**
   * Using SwingWorket class to export package
   *
   */
 private void doExport(final String docid, final MorphoFrame frame) 
 {
  final SwingWorker worker = new SwingWorker() 
  {
        public Object construct() 
        {
          if (frame!= null)
          {
            frame.setBusy(true);
          }
             
          Log.debug(20, "Exporting dataset");
          if (format.equals(REGULAR))
          {
            exportDataset(docid);
          }
          else if (format.equals(ZIP))
          {
            exportDatasetToZip(docid);
          }
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
  
  /*
   * exports the datapackage to a different location
   */
  private void exportDataset(String id)
  {
    String curdir = System.getProperty("user.dir");
    JFileChooser filechooser = new JFileChooser(curdir);
//    filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    filechooser.setDialogTitle("Export Datapackage to Selected Directory");
    filechooser.setApproveButtonText("Export");
    filechooser.setApproveButtonMnemonic('E');
    filechooser.setApproveButtonToolTipText("Choose a directory to export " +
                                            "this Datapackage to.");
    filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
   
 
    File exportDir;
    // Choose the parent of savedialog
    int result;
    result = filechooser.showSaveDialog(morphoFrame);
   
    
    exportDir = filechooser.getCurrentDirectory();
    if (result==JFileChooser.APPROVE_OPTION) {
      //now we know where to export the files to, so export them.
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
        return;
      }
    
      String location = getLocation();
       //export it.
      dataPackage.export(id, exportDir.toString(), location);
    }
  }
  
  /*
   * exports the datapackage to a different location in a zip file
   */
  private void exportDatasetToZip(String id)
  {
    String curdir = System.getProperty("user.dir");
    curdir = curdir + System.getProperty("file.separator") + id + ".zip";
    File zipFile = new File(curdir);
    JFileChooser filechooser = new JFileChooser(curdir);
    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    filechooser.setDialogTitle("Export Datapackage to Selected Zip File");
    filechooser.setApproveButtonText("Export");
    filechooser.setApproveButtonMnemonic('E');
    filechooser.setApproveButtonToolTipText("Choose a file to export " +
                                            "this Datapackage to.");
    filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    //filechooser.setSelectedFile(zipFile);                                        
    //filechooser.updateUI();
    File exportDir;
      // Choose the parent of savedialog
    int result;
    result = filechooser.showSaveDialog(morphoFrame);
   
    exportDir = filechooser.getSelectedFile();
    if (result==JFileChooser.APPROVE_OPTION) {
      //now we know where to export the files to, so export them.
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
        return;
      }
    
      String location = getLocation();
    
      //export it.
      dataPackage.exportToZip(id, exportDir.toString(), location);
    }
  }
  
  /*
   * Determine the location of data package
   */
   private String getLocation()
   {
     String location = null;
     //figure out where this thing is.
      if(metacatLoc && localLoc)
      {
        location = DataPackageInterface.BOTH;
      }
      else if(metacatLoc && !localLoc)
      {
        location = DataPackageInterface.METACAT;
      }
      else if(!metacatLoc && localLoc)
      {
        location = DataPackageInterface.LOCAL;
      }
      return location;
   }
 
   /**
    * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
