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

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.dataone.service.exceptions.NotFound;
import org.dataone.service.exceptions.VersionMismatch;
import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.SystemMetadata;
import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.dataone.AccessPolicyConverter;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
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


/**
 * Class to handle add access command
 */
public class AddEntityAccessCommand implements Command, DataPackageWizardListener {

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
				Log.debug(2,
					/*"The current EML document is not the latest version."*/ Language.getInstance().getMessage("EMLDocumentIsNotTheLatestVersion_1") + " "
					+/*" You should transform it first!"*/ Language.getInstance().getMessage("EMLDocumentIsNotTheLatestVersion_2") + "!"
					);
				return;
		 }

	}
	
	/**
	   * Method from DataPackageWizardListener.
	   * When correction wizard finished, it will show the dialog.
	   */
	  public void wizardComplete(Node newDOM, String autoSavedID)
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
			mdp = UIController.getInstance().getCurrentAbstractDataPackage();

			if (showAccessDialog(entityIndex)) {
				
				AbstractDataPackage adp = mdp.getAbstractDataPackage();
				Entity entity = adp.getEntity(entityIndex);
				String identifier = entity.getIdentifier().getValue();
				String message = "Could not set Access Policy for " + identifier;
				boolean success = false;
				try {
					// get the access rule from the page
					OrderedMap map = accessPage.getPageData(ACCESS_SUBTREE_NODENAME);
					AccessPolicy accessPolicy = null;
					// set the access policy in the system metadata
					if (map != null) {
						accessPolicy = AccessPolicyConverter.getAccessPolicyFromOrderedMap(map);
					} else {
						// TODO: distinguish between this and "inheriting" access
					}
					entity.getSystemMetadata().setAccessPolicy(accessPolicy);
					
					// save the access policy to the correct location
					success = DataStoreServiceController.getInstance().setAccessPolicy(entity, adp.getLocation());
					if (success) {
						message = "Successfully set Access Policy for " + identifier;
					}
				} catch (NotFound e) {
					message = identifier + " not found on the Coordinating Node. Cannot set Access Policy until it has been synchronized. Please try again later.";
					e.printStackTrace();
					success = false;
				} catch (VersionMismatch e) {
					message = e.getMessage();
					//message = identifier + " exists on the Coordinating Node with a newer serialVersion. Please try again later.";
					e.printStackTrace();
					success = false;
				} catch (Exception w) {
					message = "Error modifying Access Policy: " + w.getMessage();
					w.printStackTrace();
					success = false;
				}	
				
				// show message 
				Log.debug(5, message);
				
				// refresh if we changed the EML
				if (success) {
					// edit EML once if there is an access block for it
					Node existingAccess = adp.getEntityAccess(entityIndex, 0, 0);
					if (existingAccess !=  null) {
						// delete existing access from EML
						adp.setEntityAccess(entityIndex, 0, 0, null);
						// mark as unsaved because we have edited the EML
						UIController.showNewPackage(mdp);
					}
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
	  
	  /**
	   *  Method from DataPackageWizardListener. Do nothing.
	   *
	   */
	  public void wizardSavedForLater()
	  {
	    Log.debug(45, "Correction wizard was saved for later usage");
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
		AbstractDataPackage adp = mdp.getAbstractDataPackage();
		Entity entity = adp.getEntity(entityIndex);
		if(entity != null) {
		  SystemMetadata sysMeta = entity.getSystemMetadata();
		  AccessPolicy accessPolicy = sysMeta.getAccessPolicy();
		  try {
		    existingValuesMap = AccessPolicyConverter.getOrderMapFromAccessPolicy(accessPolicy, "");
		  } catch (Exception e) {
		    Log.debug(20, "Can't get the OrderedMap from the AccessPolicy");
		  }
		}
		//System.out.println(existingValuesMap.toString());
		/*try {
			accessRoot = adp.getEntityAccess(entityIndex, 0, 0);
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (accessRoot != null) {
			existingValuesMap = XMLUtilities.getDOMTreeAsXPathMap(accessRoot);
		}*/
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
	
	private MorphoDataPackage mdp;
	private AbstractUIPage accessPage;
}
