/**
 *  '$RCSfile: ExportCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-12-10 23:05:18 $'
 * '$Revision: 1.15 $'
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
import edu.ucsb.nceas.morpho.datapackage.DataPackage;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JOptionPane;

/**
 * Class to handle export package (both regular and zip) command
 */
public class ExportCommand implements Command 
{
    
  /** A reference to the export dialog */
   private ExportDialog exportDialog = null;

   /** A reference to the dialog */
   private OpenDialogBox dialog = null;
   
  /** A reference to the MorphoFrame */
   private MorphoFrame morphoFrame = null;
   
  /** A refernce to the ResultPanel */
   private ResultPanel resultPane = null;
   
   DataPackageInterface dataPackage = null;
   
  /** Constant String to show state of delete */
  public static final String ZIP = "ZIP";
  public static final String REGULAR = "REGULAR";
  public static final String TOEML2 = "TOEML2";
 
  /** Constant String for zip file extension*/
  private String ZIPEXTENSION = "zip";
  
  /** fromat of the export */
  private String format = null;
  /** selected docid to delete */
  String selectDocId = null;
  /** DataPackage in network */
  boolean metacatLoc = false;
  /** DataPackage in local */
  boolean localLoc = false;
  
  String location = null;
  
    
  /**
   * Constructor of ExportCommand
   * @param dialog a export command will be happened at this dialog 
   * @param myFormat the format for export, regular or zip
   */
  public ExportCommand(OpenDialogBox box, String myFormat, ExportDialog exportDialog)
  {
    this.exportDialog = exportDialog;
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
    else if (myFormat.equals(TOEML2))
    {
       format = TOEML2;
    }
    else
    {
       Log.debug(20, "Unkown export format!");
    }
    
  
  }//LocalToNetworkCommand
  
 
  /**
   * execute delete local package command
   */    
  public void execute(ActionEvent event)
  { 
    (new CancelCommand(exportDialog)).execute(null);
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
    else
    {
      //Try if it is datapackage frame
      selectDocId = dataPackage.getDocIdFromMorphoFrame(morphoFrame);
      metacatLoc = dataPackage.isDataPackageInNetwork(morphoFrame);
      localLoc = dataPackage.isDataPackageInLocal(morphoFrame);
       // Make sure selected a id, and there is local pacakge
      if ( selectDocId != null && !selectDocId.equals(""))
      {
        doExport(selectDocId, morphoFrame);
      }//if
    }

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
          else if (format.equals(TOEML2))
          {
            //exportDatasetToEml2(docid);
            Log.debug(1, "No longer applicable!!!");
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
    if (dialog == null)
     {
      result = filechooser.showSaveDialog(morphoFrame);
    }
    else
    {
      result = filechooser.showSaveDialog(dialog);
    }
   
    
//    exportDir = filechooser.getCurrentDirectory();
    exportDir = filechooser.getSelectedFile();

    if (result==JFileChooser.APPROVE_OPTION) {
      //now we know where to export the files to, so export them.
      if (location==null) {
        location = getLocation();
      }
       //export it.
      dataPackage.export(id, exportDir.toString(), location);
    }
  }

  /*
   * exports the datapackage to a different location
   */
  private void exportDatasetToEml2(String id)
  {
    String curdir = System.getProperty("user.dir");
    curdir = curdir + System.getProperty("file.separator") + id + "_eml2.xml";
    File eml2File = new File(curdir);
    JFileChooser filechooser = new JFileChooser(curdir);
    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    filechooser.setDialogTitle("Export Datapackage to EML2 File");
    filechooser.setApproveButtonText("Export");
    filechooser.setApproveButtonMnemonic('E');
    filechooser.setApproveButtonToolTipText("Choose a file name to export " +
                                            "this Datapackage to.");
    filechooser.setSelectedFile(eml2File);                                        
    
    File exportDir;
      // Choose the parent of savedialog
    int result;
    if (dialog == null)
    {
      result = filechooser.showSaveDialog(morphoFrame);
    }
    else
    {
      result = filechooser.showSaveDialog(dialog);
    }
   
    exportDir = filechooser.getSelectedFile();
    if (result==JFileChooser.APPROVE_OPTION) {
      //now we know where to export the files to, so export them.
      if ( exportDir != null)
      {
        // Check the file name if it has .zip extension
        String fileName = exportDir.getAbsolutePath();
        if (fileName != null)
        {
          String location = getLocation();
          //export it.
          dataPackage.exportToEml2(id, fileName, location);
        }
      }
    }

  }

  
  /*
   * exports the datapackage to a different location in a zip file
   */
  private void exportDatasetToZip(String id)
  {
    // Set zip file filter
    FileFilter zipFilter = new FileFilter()
    {
      public boolean accept(File f)
      {
        boolean flag = false;
        if ( f != null)
        {
          if (f.isDirectory())
          {
            flag = true;
          }
          else
          {
            String extention = getFileExtension(f);
            if (extention != null && extention.equalsIgnoreCase(ZIPEXTENSION))
            {
              flag = true;
            }//if
          }//else
        }//if
        return flag;
      }// accept
      
      public String getDescription()
      {
        return "Zip(*.zip, *.ZIP)";
      }//getDescription
      
      private String getFileExtension(File f) 
      {
	      if(f != null) 
        {
	        String filename = f.getName();
	        int i = filename.lastIndexOf(".");
	        if(i>0 && i<filename.length()-1) 
          {
		        return filename.substring(i+1).toLowerCase();
	        }//if
	       }//if
	      return null;
      }//getFileExtention
    };//FileFilter
    
    String curdir = System.getProperty("user.dir");
    curdir = curdir + System.getProperty("file.separator") + id + ".zip";
    File zipFile = new File(curdir);
    JFileChooser filechooser = new JFileChooser(curdir);
    filechooser.addChoosableFileFilter(zipFilter);
    filechooser.setFileFilter(zipFilter);
    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    filechooser.setDialogTitle("Export Datapackage to Selected Zip File");
    filechooser.setApproveButtonText("Export");
    filechooser.setApproveButtonMnemonic('E');
    filechooser.setApproveButtonToolTipText("Choose a file to export " +
                                            "this Datapackage to.");
    filechooser.setSelectedFile(zipFile);                                        
    
    File exportDir;
      // Choose the parent of savedialog
    int result;
    if (dialog == null)
    {
      result = filechooser.showSaveDialog(morphoFrame);
    }
    else
    {
      result = filechooser.showSaveDialog(dialog);
    }
   
    exportDir = filechooser.getSelectedFile();
    if (result==JFileChooser.APPROVE_OPTION) {
      //now we know where to export the files to, so export them.
      String location = getLocation();
      if ( exportDir != null)
      {
        // Check the file name if it has .zip extension
        String fileName = exportDir.getAbsolutePath();
        if (fileName != null)
        {
          String lowerCaseName =fileName.toLowerCase();
          String fileExtention ="."+ ZIPEXTENSION;
          // if not end with ".zip", then add it
          if (!lowerCaseName.endsWith(fileExtention))
          {
            fileName = fileName + fileExtention;
          }
          Log.debug(30, "fileName is: "+fileName);
          
          //export it.
          dataPackage.exportToZip(id, fileName, location);
        }
      }
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
      else {
        location = "";
      }
      return location;
   }
 
   /**
    * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
