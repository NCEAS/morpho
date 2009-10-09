/**
 *  '$RCSfile: ImportDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 22:03:01 $'
 * '$Revision: 1.22 $'
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
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
//import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to handle import data file command
 */
public class ImportDataCommand implements Command, DataPackageWizardListener 
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
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event)
  {
    
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();

    //Check if the eml document is the current version before editing it.
    EMLTransformToNewestVersionDialog dialog = null;
	  try
	  {
		  dialog = new EMLTransformToNewestVersionDialog(morphoFrame, this);
	  }
	  catch(Exception e)
	  {
		  return;
	  }
	  if (dialog.getUserChoice() == JOptionPane.NO_OPTION)
	 {
		   // if user choose not transform it, stop the action.
			Log.debug(2,"The current EML document is not the latest version. You should transform it first!");
			return;
	 }
	

  }//execute
  
  /**
   * Method from DataPackageWizardListener.
   * When correction wizard finished, it will show the dialog.
   */
  public void wizardComplete(Node newDOM, String autoSavedID)
  {
	  DataViewContainerPanel resultPane = null;
	//Get the frame again since EMLTransformer may generate a new one
		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
	    if (morphoFrame != null) {

	       resultPane = morphoFrame.getDataViewContainerPanel();
	    }//if
	    // make sure resulPanel is not null
	    if ( resultPane != null) {

	//DFH      DataPackage dp = resultPane.getDataPackage();
	      final AbstractDataPackage adp = resultPane.getAbstractDataPackage();
	      int nextEntityIndex =0;
	      if(adp!= null)
	      {
	    	  //since index of eneity array in AbstractDataPackage start as 0.
		      // so the next index of entity is the length of the entity array.
	    	  nextEntityIndex = adp.getEntityCount();
	      }
	      Log.debug(30, "the index of starting eneity will be "+nextEntityIndex);
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

	            public void wizardComplete(Node newDOM, String autoSavedID) {

	              if(newDOM != null) {

	                Log.debug(30,"Entity Wizard complete - creating Entity object..");
	                adp.addEntity(newDOM);
	                adp.setLocation("");  // we've changed it and not yet saved

	              }

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
	          }, nextEntityIndex);

	    }//if

  }
  
  /**
   * Method from DataPackageWizardListener. Do nothing.
   */
  public void wizardCanceled()
  {
	  Log.debug(45, "Correction wizard cancled");
	  
  }


  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class CancelCommand
