/**
 *  '$RCSfile: DeleteTableCommand.java,v $'
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
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.DataLocation;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.ReplaceDataPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.TextImportEntity;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;

import javax.activation.FileDataSource;

import org.dataone.service.types.v1.Identifier;


/**
 * Class to handle import data file command
 */
public class ReplaceDataCommand implements Command {
	/** A reference to the MophorFrame */
	private MorphoFrame morphoFrame = null;
	private ReplaceDataPage replaceDataPage;

	/**
	 * Constructor
	 */
	public ReplaceDataCommand() {

	}

	/**
	 * replace data file
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {
		DataViewContainerPanel resultPane = null;
		morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		if (morphoFrame != null) {
			resultPane = morphoFrame.getDataViewContainerPanel();
		}// if
		
		// make sure resulPanel is not null
		if (resultPane != null) {

			MorphoDataPackage mdp = resultPane.getMorphoDataPackage();
			final AbstractDataPackage adp = mdp.getAbstractDataPackage();
			int entityIndex = resultPane.getLastTabSelected();
				
			// show the dialog
			if (showDialog()) {
			
				OrderedMap dataTableMap = replaceDataPage.getPageData();
				
				// get the file they selected
				String dataFilePath = (String) dataTableMap.get(DataLocation.ONLINE_URL_XPATH);
				// increment revision if we have the id
				String currentOnlineUrl = adp.getDistributionUrl(entityIndex, 0, 0);
				String currentId = null;
				if (currentOnlineUrl.startsWith(DataLocation.URN_ROOT)) {
					currentId = DataLocation.getFileNameFromURL(currentOnlineUrl);
				}
				//save the data to local cache
				File dataFile = new File(dataFilePath);
				String nexDocId = DataStoreServiceController.getInstance().generateIdentifier(null, DataPackageInterface.LOCAL);
				try {
					Morpho.thisStaticInstance.getLocalDataStoreService().saveTempDataFile(nexDocId, new FileInputStream(dataFile));
				} catch (Exception e) {
					Log.debug(1, "Error saving replacement data file!");
					e.printStackTrace();
					return;
				}
				
				//clear out the old distribution info
				adp.removePhysicalDistribution(entityIndex, 0, 0);
				
				// set the new information
				adp.setDistributionUrl(entityIndex, 0, 0, DataLocation.URN_ROOT + nexDocId);
				String format = new FileDataSource(dataFile).getContentType();
				adp.setPhysicalFormat(entityIndex, 0, format);
				
				// set revision history
				boolean useNewId = replaceDataPage.getNewId();
				if (!useNewId) {
					Identifier oldIdentifier = new Identifier();
					oldIdentifier.setValue(currentId);
					adp.getEntity(entityIndex).getSystemMetadata().setObsoletes(oldIdentifier);
				}
				
				// NOTE: this path includes "dataTable" as the entity, but since we don't actually use it in the EML,
				// it's not a problem for otherEntity and other entity types
				String objectName = (String) dataTableMap.get(DataLocation.OBJECTNAME_XPATH);
				adp.setPhysicalName(entityIndex, 0, objectName);
				String entityName = (String) dataTableMap.get(TextImportEntity.xPathRoot + "entityName");
				if (entityName == null || entityName.length() < 1) {
					entityName = objectName;
				}
				adp.setEntityName(entityIndex, entityName);
				String numRecS = (String) dataTableMap.get(TextImportEntity.xPathRoot + "numberOfRecords");
				adp.setEntityNumRecords(entityIndex, numRecS);
				String sizeS = (String) dataTableMap.get(TextImportEntity.xPathRoot + "physical/size");
				adp.setPhysicalSize(entityIndex, 0, sizeS);
				
				// refresh the window
				adp.setLocation(""); // we've changed it and not yet saved
				mdp.setAbstractDataPackage(adp);
				try {
					ServiceController services = ServiceController.getInstance();
					ServiceProvider provider = 
						services.getServiceProvider(DataPackageInterface.class);
					DataPackageInterface dataPackageInt = (DataPackageInterface) provider;
					dataPackageInt.openNewDataPackage(mdp, null);
				} catch (ServiceNotHandledException snhe) {
					Log.debug(6, snhe.getMessage());
				}
				morphoFrame.setVisible(false);
				UIController controller = UIController.getInstance();
				controller.removeWindow(morphoFrame);
				morphoFrame.dispose();
			}	
		}

	}// execute
	
	private boolean showDialog() {
		ServiceController sc;
		DataPackageWizardInterface dpwPlugin = null;
		try {
			sc = ServiceController.getInstance();
			dpwPlugin = (DataPackageWizardInterface) sc.getServiceProvider(DataPackageWizardInterface.class);

		} catch (ServiceNotHandledException se) {
			Log.debug(6, se.getMessage());
			se.printStackTrace();
		}
		if (dpwPlugin == null) {
			return false;
		}
		replaceDataPage = 
			(ReplaceDataPage) dpwPlugin.getPage(DataPackageWizardInterface.REPLACE_DATA_LOCATION);
		// do we want to populate with existing info?
		boolean pageCanHandleAllData = true;
		ModalDialog dialog = null;
		if (pageCanHandleAllData) {

			dialog = 
				new ModalDialog(
						replaceDataPage, 
						UIController.getInstance().getCurrentActiveWindow(), 
						UISettings.POPUPDIALOG_WIDTH,
						UISettings.POPUPDIALOG_HEIGHT);

			return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
		}
		return false;
	}

}
