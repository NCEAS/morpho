/**
 *  '$RCSfile: CreateNewDataPackageCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-11-21 22:33:45 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.wizard.PackageWizardShell;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DataPackageWizardPlugin;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.w3c.dom.Node;
import edu.ucsb.nceas.morpho.editor.*;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * Class to handle create new data package command
 */
public class CreateNewDataPackageCommand implements Command 
{
  
  private Morpho morpho = null;
  /**
   * Constructor of CreateNewDataPackageCommand
   * @param morpho the morpho will apply to this command
   */
  public CreateNewDataPackageCommand(Morpho morpho)
  {
    this.morpho = morpho;
 
  }//RefreshCommand
  
  
  
  
  /**
   * execute create data package  command
   */    
  public void execute(ActionEvent event)
  {   
    Log.debug(20, "Action fired: New Data Package");
    DataPackageWizardPlugin plugin = new DataPackageWizardPlugin();
    //plugin.initialize(Morpho.thisStaticInstance);
    plugin.startWizard(
      new DataPackageWizardListener() {
      
        public void wizardComplete(Node newDOM) {
        Log.debug(30,"Wizard complete - Will now create an AbstractDataPackage..");
          AbstractDataPackage adp = DataPackageFactory.getDataPackage(newDOM, "eml:eml");
//          dp.serialize();
         Log.debug(30,"AbstractDataPackage complete - Will now show in an XML Editor..");
         Node domnode = adp.getMetadataNode();

         try 
         {
           ServiceController services = ServiceController.getInstance();
           ServiceProvider provider = 
                      services.getServiceProvider(DataPackageInterface.class);
           DataPackageInterface dataPackage = (DataPackageInterface)provider;
           dataPackage.openNewDataPackage(adp, null, "eml:eml");
         }
         catch (ServiceNotHandledException snhe) 
         {
           Log.debug(6, snhe.getMessage());
         }

         
//          DocFrame df = new DocFrame();
//          df.setVisible(true);
//          df.initDoc(null, domnode);
        
          Log.debug(45, "\n\n********** Wizard finished: DOM:");
          Log.debug(45, XMLUtilities.getDOMTreeAsString(newDOM, false));
//          System.exit(0);
        }

        public void wizardCanceled() {
      
          Log.debug(45, "\n\n********** Wizard canceled!");
          System.exit(0);
        }
      }
    );
  }//execute
  
 

 
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
