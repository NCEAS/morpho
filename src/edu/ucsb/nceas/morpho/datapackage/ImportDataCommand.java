/**
 *  '$RCSfile: ImportDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-12-15 21:03:04 $'
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
public class ImportDataCommand implements Command 
{
  /** A reference to the MophorFrame */
  private MorphoFrame morphoFrame = null;
 
  /**
   * Constructor of Import data command
   */
  public ImportDataCommand()
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
      
//DFH      DataPackage dp = resultPane.getDataPackage();
      final AbstractDataPackage adp = resultPane.getAbstractDataPackage();
      DataViewer dv = resultPane.getCurrentDataViewer();
      String entityId = null;
      
      if (dv!=null) {
        entityId = dv.getEntityFileId();
      }
    // new AbstractDataPackage/Wizard calls will go here

        Log.debug(20, "Action fired: Entity Wizard");
        DataPackageWizardInterface dpw = null;
        try {
          ServiceController services = ServiceController.getInstance();
          ServiceProvider provider = 
             services.getServiceProvider(DataPackageWizardInterface.class);
          dpw = (DataPackageWizardInterface)provider;
        
        } catch (ServiceNotHandledException snhe) {
      
          Log.debug(6, snhe.getMessage());
        }
  
        dpw.startEntityWizard(
        
          new DataPackageWizardListener() {
      
            public void wizardComplete(Node newDOM) {
          
              Log.debug(45, "\n\n********** Entity Wizard finished: DOM:");
              Log.debug(45, XMLUtilities.getDOMTreeAsString(newDOM, false));
              Log.debug(30,"Entity Wizard complete - creating Entity object..");

// DFH --- Note: newDOM is root node (eml:eml), not the entity node              
              Node entNode = null;             
              String entityXpath = "";
              try{
                entityXpath = (XMLUtilities.getTextNodeWithXPath(adp.getMetadataPath(), 
                       "/xpathKeyMap/contextNode[@name='package']/entities")).getNodeValue();
                NodeList entityNodes = XMLUtilities.getNodeListWithXPath(newDOM,
                         entityXpath);
                entNode = entityNodes.item(0);
              }
              catch (Exception w) {
                Log.debug(5, "Error in trying to get entNode in ImportDataCommand");
              }
                
                
                //              Entity entity = new Entity(newDOM);
              Entity entity = new Entity(entNode);

              Log.debug(30,"Adding Entity object to AbstractDataPackage..");
              adp.addEntity(entity);

       // ---DFH			
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
  
            public void wizardCanceled() {
    
              Log.debug(45, "\n\n********** Wizard canceled!");
            }
          });
      
    }//if
  
  }//execute 
 
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
