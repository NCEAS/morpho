/**
 *  '$RCSfile: DeleteTableCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-01-08 19:14:49 $'
 * '$Revision: 1.1 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.*;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;

import edu.ucsb.nceas.morpho.framework.DataPackageInterface;


import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * Class to handle import data file command
 */
public class DeleteTableCommand implements Command 
{
  /** A reference to the MophorFrame */
  private MorphoFrame morphoFrame = null;
 
  /**
   * Constructor of Import data command
   */
  public DeleteTableCommand()
  {
 
  }//RefreshCommand

  /**
   * execute refresh command
   */    
  public void execute(ActionEvent event)
  {   
    DataViewContainerPanel resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null) {
      
       resultPane = AddDocumentationCommand.
                          getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    }//if
    // make sure resulPanel is not null
    if ( resultPane != null) {
      
      final AbstractDataPackage adp = resultPane.getAbstractDataPackage();
      DataViewer dv = resultPane.getCurrentDataViewer();
      String entityId = null;
      int entityNum = resultPane.getLastTabSelected();
      if (dv!=null) {
        adp.deleteEntity(entityNum);
      }

       // ---DFH - resetting the displayed package info			
              Morpho morpho = Morpho.thisStaticInstance;
              AccessionNumber an = new AccessionNumber(morpho);
              String curid = adp.getAccessionNumber();
              String newid = null;
              if (!curid.equals("")) {
                newid = an.incRev(curid);
              } else {
                newid = an.getNextId();
              }
              adp.setAccessionNumber(newid);
              adp.setLocation("");  // we've changed it and not yet saved
              try 
              {
                ServiceController services = ServiceController.getInstance();
                ServiceProvider provider = 
                      services.getServiceProvider(DataPackageInterface.class);
                DataPackageInterface dataPackageInt = (DataPackageInterface)provider;
                dataPackageInt.openNewDataPackage(adp, null);
              }
              catch (ServiceNotHandledException snhe) 
              {
                Log.debug(6, snhe.getMessage());
              }
							morphoFrame.setVisible(false);
              UIController controller = UIController.getInstance();
              controller.removeWindow(morphoFrame);
              morphoFrame.dispose();
            }
  
      
  
  }//execute 
 
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}
