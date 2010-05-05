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
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.DataLocation;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.OtherEntityPage;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.transform.TransformerException;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Class to handle import data file command
 */
public class ImportOtherEntityCommand implements Command {
	/** A reference to the MophorFrame */
	private MorphoFrame morphoFrame = null;
	private OtherEntityPage replaceDataPage;

	/**
	 * Constructor
	 */
	public ImportOtherEntityCommand() {

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

			final AbstractDataPackage adp = resultPane.getAbstractDataPackage();
			DataViewer dv = resultPane.getCurrentDataViewer();
			int entityIndex = adp.getEntityCount();
			// do we have a data panel already?
			if (dv != null) {
				
				// show the dialog
				if (showDialog()) {
				
					OrderedMap dataTableMap = replaceDataPage.getPageData();
					
					// get the local file
					String dataFilePath = (String) dataTableMap.get(OtherEntityPage.ONLINE_URL_XPATH);

					//save the data to local cache
					File dataFile = new File(dataFilePath);
					String nexDocId = saveDataFileAsTemp(dataFile, null);
					dataTableMap.put(OtherEntityPage.ONLINE_URL_XPATH, DataLocation.URN_ROOT + nexDocId);
					
					// put it in the dom
					 DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
					 Document doc = impl.createDocument("", "otherEntity", null);

					 Element otherEntityRoot = doc.getDocumentElement();

					    try {
					      XMLUtilities.getXPathMapAsDOMTree(dataTableMap, otherEntityRoot);
					      Entity newEntity = new Entity(otherEntityRoot);
						  adp.insertEntity(newEntity, entityIndex);
					    }
					    catch (TransformerException w) {
					      Log.debug(5, "Unable to add otherEntity to package!");
					      w.printStackTrace();
					      return;
					    }
					
					// refresh the window
					adp.setLocation(""); // we've changed it and not yet saved
					try {
						ServiceController services = ServiceController.getInstance();
						ServiceProvider provider = 
							services.getServiceProvider(DataPackageInterface.class);
						DataPackageInterface dataPackageInt = (DataPackageInterface) provider;
						dataPackageInt.openNewDataPackage(adp, null);
					} catch (ServiceNotHandledException snhe) {
						Log.debug(6, snhe.getMessage());
					}
					morphoFrame.setVisible(false);
					UIController controller = UIController.getInstance();
					controller.removeWindow(morphoFrame);
					morphoFrame.dispose();
				}	
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
			(OtherEntityPage) dpwPlugin.getPage(DataPackageWizardInterface.OTHER_ENTITY);
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
	
	  /*
	   * increment revision or create a new id,
	   * assign id to the data file and save it with that id
	   */
	  private String saveDataFileAsTemp(File f, String currentId) {
	    AccessionNumber an = new AccessionNumber(Morpho.thisStaticInstance);
	    if (currentId  == null) {
	    	currentId = an.getNextId();
	    } else {
	    	currentId = an.incRev(currentId);
	    }
	    FileSystemDataStore fds = new FileSystemDataStore(Morpho.thisStaticInstance);
	    try {
	      fds.saveTempDataFile(currentId, new FileInputStream(f));
	    } catch (Exception w) {
	      Log.debug(1, "Error saving replacement data file!");
	    }
	    return currentId;
	  }

}
