/**
 *  '$RCSfile: OpenPackageCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-10-14 05:14:55 $'
 * '$Revision: 1.12 $'
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

import edu.ucsb.nceas.morpho.framework.ButterflyFlapCoordinator;
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

/**
 * Class to handle open package command
 */
public class OpenPackageCommand implements Command, ButterflyFlapCoordinator 
{
  /** the doctype */ 
  private String doctype = "";
  
  /** A reference to the resultPanel */
   private ResultPanel resultPanel = null;
  
  /** A reference to the dialog */
   private OpenDialogBox open = null;
   
  /** A refernce to the MorphoFrame (for butterfly) */
   private MorphoFrame frame = null;
   
  /**
   * Constructor of SearcCommand
   * @param dialog the open dialog where the open package command happend  
   */
  public OpenPackageCommand(OpenDialogBox dialog)
  {
    open = dialog;
   
  }//OpenPackageCommand
  
  
  /**
   * execute open package command
   */    
  public void execute(ActionEvent event)
  {
    // if Resulpanel's parent is morphoframe, frame value will be set the parent
    // of resultpanel
    if ( open == null)
    {
      frame = UIController.getInstance().getCurrentActiveWindow();
      resultPanel = RefreshCommand.getResultPanelFromMorphoFrame(frame);
    }
    else
    {
      //ResultPanel is in oepn dialog box, frame will be the parent of dialog 
      frame = open.getParentFrame();
      resultPanel = open.getResultPanel();
    }
      
    if (resultPanel != null)
    {
      String selectDocId = resultPanel.getSelectedId();
      boolean metacatLoc = resultPanel.getMetacatLocation();
      boolean localLoc = resultPanel.getLocalLocation();
      String location = null;
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
      this.doctype = resultPanel.getDocType();
      
      // close the openDialogBox
      if ( open != null)
      {
        open.setVisible(false);
        open.dispose();
        open = null;
      }
      // Open the pakcage
      doOpenPackage(selectDocId, location, frame, this);
     
    }
    
  }//execute

  /**
   * Using SwingWorket class to open a package
   *
   */
  private void doOpenPackage(final String docid, final String location,  
           final MorphoFrame morphoFrame, final OpenPackageCommand command)
  {
    final SwingWorker worker = new SwingWorker()
    {
      public Object construct()
      {
        startFlap();
        
        try 
        {
          ServiceController services = ServiceController.getInstance();
          ServiceProvider provider = 
                      services.getServiceProvider(DataPackageInterface.class);
          DataPackageInterface dataPackage = (DataPackageInterface)provider;
          dataPackage.openDataPackage(location, docid, null, command, doctype);
        }
        catch (ServiceNotHandledException snhe) 
        {
          Log.debug(6, snhe.getMessage());
        }
        return null;
      }//constructor
      
      public void finished()
      {
        stopFlap();
      }//finish
    };//final
    worker.start();
    
  }//doOpenPakcage
  

  /**
   * Method returns the doctype
   */
  public String getDocType() {
    return doctype;
  }
  
  /**
   * Method implements from ButterflyFlapCoordinator
   */
  public void startFlap()
  {
    frame.setBusy(true);
    frame.setEnabled(false);
  }
  
  /**
   * Method implements from ButterflyFlapCoordinator
   */
  public void stopFlap()
  {
    frame.setEnabled(true);
    frame.setBusy(false);
  }  
   /**
    * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
