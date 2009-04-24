/**
 *  '$RCSfile: AddEntityAccessCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 22:03:01 $'
 * '$Revision: 1.7 $'
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

import javax.swing.JOptionPane;
import javax.xml.transform.TransformerException;

import java.awt.event.ActionEvent;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
//import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * Class to handle add access command
 */
public class AddEntityAccessCommand implements Command, DataPackageWizardListener {

	// generic name for lookup in eml listings
	private final String DATAPACKAGE_ACCESS_GENERIC_NAME = "entityAccess";

	// generic name for lookup in eml listings
	private final String ACCESS_SUBTREE_NODENAME = "/access/";

	public AddEntityAccessCommand() {
	}

	/**
	 * execute add command
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {
		 //Check if the eml document is the current version before editing it.
		  MorphoFrame frame = UIController.getInstance().getCurrentActiveWindow();
		  EMLTransformToNewestVersionDialog dialog = null;
		  try
		  {
			  dialog = new EMLTransformToNewestVersionDialog(frame, this);
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

		
	}
	
	/**
	   * Method from DataPackageWizardListener.
	   * When correction wizard finished, it will show the dialog.
	   */
	  public void wizardComplete(Node newDOM)
	  {
		  int entityIndex = 0;

			DataViewContainerPanel resultPane = null;
			MorphoFrame morphoFrame = 
				UIController.getInstance().getCurrentActiveWindow();
			if (morphoFrame != null) {
				resultPane = morphoFrame.getDataViewContainerPanel();
			}
			
			// make sure resulPanel is not null
			if (resultPane != null) {
				entityIndex = resultPane.getLastTabSelected();
			}
			adp = UIController.getInstance().getCurrentAbstractDataPackage();

			if (showAccessDialog(entityIndex)) {

				try {
					insertAccess(entityIndex);
					UIController.showNewPackage(adp);
				} catch (Exception w) {
					Log.debug(15, "Exception trying to modify access DOM: " + w);
					w.printStackTrace();
					Log.debug(5, "Unable to add access details!");
				}
			}
	  }
	  
	  /**
	   * Method from DataPackageWizardListener. Do nothing.
	   */
	  public void wizardCanceled()
	  {
		  Log.debug(45, "Correction wizard cancled");
		  
	  }

	private boolean showAccessDialog(int entityIndex) {

		ServiceController sc;
		DataPackageWizardInterface dpwPlugin = null;
		try {
			sc = ServiceController.getInstance();
			dpwPlugin = (DataPackageWizardInterface) sc
					.getServiceProvider(DataPackageWizardInterface.class);

		} catch (ServiceNotHandledException se) {

			Log.debug(6, se.getMessage());
			se.printStackTrace();
		}
		if (dpwPlugin == null) {
			return false;
		}

		accessPage = dpwPlugin.getPage(DataPackageWizardInterface.ENTITY_ACCESS);

		OrderedMap existingValuesMap = null;
		try {
			accessRoot = adp.getEntityAccess(entityIndex, 0, 0);
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (accessRoot != null) {
			existingValuesMap = XMLUtilities.getDOMTreeAsXPathMap(accessRoot);
		}
		Log.debug(45, "sending previous data to accessPage -\n\n"
				+ existingValuesMap);

		boolean pageCanHandleAllData = accessPage.setPageData(
				existingValuesMap, ACCESS_SUBTREE_NODENAME);

		ModalDialog dialog = null;
		//if (false) {
		if (pageCanHandleAllData) {

			dialog = new ModalDialog(accessPage, UIController.getInstance()
					.getCurrentActiveWindow(), UISettings.POPUPDIALOG_WIDTH,
					UISettings.POPUPDIALOG_HEIGHT);
		} else {

//			UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
//					"/eml:eml/dataset/dataTable[" + entityIndex + "]/physical/distribution/access",
//					0);
			UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
					null,
					0);
			return false;
		}

		return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
	}

	private void insertAccess(int entityIndex) {

		OrderedMap map = accessPage.getPageData(ACCESS_SUBTREE_NODENAME);

		Log.debug(45,
				"\n insertAccess() Got access details from Access page");

		if (map == null || map.isEmpty()) {
			Log.debug(30, "removing access rules from data entity!");
			adp.setEntityAccess(entityIndex, 0, 0, null);
			return;
		}

		DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
		Document doc = impl.createDocument("", "access", null);

		accessRoot = doc.getDocumentElement();

		try {
			XMLUtilities.getXPathMapAsDOMTree(map, accessRoot);

		} catch (TransformerException w) {
			Log.debug(5, "Unable to add access details to package!");
			Log
					.debug(
							15,
							"TransformerException ("
									+ w
									+ ") calling "
									+ "XMLUtilities.getXPathMapAsDOMTree(map, accessRoot) with \n"
									+ "map = " + map + " and accessRoot = "
									+ accessRoot);
			return;
		}
		
		adp.setEntityAccess(entityIndex, 0, 0, accessRoot);

		Log.debug(45, "added new access details to package...");
		
	}

	private Node accessRoot;
	private AbstractDataPackage adp;
	private AbstractUIPage accessPage;
}
