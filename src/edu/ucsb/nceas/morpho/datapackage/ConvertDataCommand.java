/**
 *  '$Id$'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author$'
 *     '$Date$'
 * '$Revision$'
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
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.EntityWizardListener;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.DataLocation;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.ImportedTextFile;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import org.w3c.dom.Node;

/**
 * Class to handle import data file command
 */
public class ConvertDataCommand implements Command, DataPackageWizardListener 
{
  /** A reference to the MorphoFrame */
  private MorphoFrame morphoFrame = null;

  /**
   * Constructor
   */
  public ConvertDataCommand()
  {

  }


  /**
   * execute the command
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
	 * Method from DataPackageWizardListener. When correction wizard finished,
	 * it will show the dialog.
	 */
	public void wizardComplete(Node newDOM, String autoSavedID) {
		DataViewContainerPanel resultPane = null;
		// Get the frame again since EMLTransformer may generate a new one
		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		if (morphoFrame != null) {
			resultPane = morphoFrame.getDataViewContainerPanel();
		}
		// make sure resulPanel is not null
		if (resultPane != null) {
			final AbstractDataPackage adp = resultPane.getAbstractDataPackage();

			if (adp == null) {
				Log.debug(5, "Data package is null or not found, cannot convert entity!");
				return;
			}
			final int nextEntityIndex = adp.getEntityCount();
			Log.debug(30, "the index of starting eneity will be " + nextEntityIndex);
			
			int currentEntityIndex = resultPane.getLastTabSelected();
			String otherEntityURL = adp.getDistributionUrl(currentEntityIndex, 0, 0);
			String otherEntityDocid = DataLocation.getFileNameFromURL(otherEntityURL);
			FileSystemDataStore fds = new FileSystemDataStore(Morpho.thisStaticInstance);
			MetacatDataStore mds = new MetacatDataStore(Morpho.thisStaticInstance);
			File otherEntityFile = null;
			try {
				// get from metacat only if we have to
				if (adp.getLocation().equals(AbstractDataPackage.METACAT)) {
					otherEntityFile = mds.openFile(otherEntityDocid);
				} else {
					otherEntityFile = fds.openFile(otherEntityDocid);
				}
			} catch (Exception e) {
				Log.debug(5, "Cannot locate otherEntity data file: " + otherEntityURL);
				e.printStackTrace();
				return;
			}
			
			// carry on from here
			ImportedTextFile dataTextFile = new ImportedTextFile(otherEntityFile);	
			EntityWizardListener dataPackageWizardListener = 
				new EntityWizardListener(adp, nextEntityIndex, morphoFrame);

			WizardContainerFrame dpWiz = new WizardContainerFrame(
					IncompleteDocSettings.ENTITYWIZARD, morphoFrame);
			dpWiz.setEntityIndex(nextEntityIndex);
			dpWiz.initialAutoSaving();
			dpWiz.setImportedDataTextFile(dataTextFile);
			dpWiz.setDataPackageWizardListener(dataPackageWizardListener);
			dpWiz.setBounds(WizardSettings.WIZARD_X_COORD,
					WizardSettings.WIZARD_Y_COORD, WizardSettings.WIZARD_WIDTH,
					WizardSettings.WIZARD_HEIGHT);
			dpWiz.setCurrentPage(WizardSettings.ENTITY_WIZ_FIRST_PAGE_ID);
			dpWiz.setShowPageCountdown(false);
			dpWiz.setTitle("Convert other entity to data");
			dpWiz.setVisible(true);

		}

	}
  
  /**
   * Method from DataPackageWizardListener. Do nothing.
   */
  public void wizardCanceled()
  {
	  Log.debug(45, "Convert otherEntity wizard cancled");
	  
  }
  
  /**
   *  Method from DataPackageWizardListener. Do nothing.
   *
   */
  public void wizardSavedForLater()
  {
    Log.debug(45, "Convert otherEntity wizard was saved for later usage");
  }


}
