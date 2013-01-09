/**
 *  '$RCSfile: AddTitleAbstractCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
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

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.dataone.service.types.v1.ReplicationPolicy;
import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.ReplicationPolicyPage;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;

/**
 * Class to handle add title and abstract command
 */
public class EditReplicationPolicyCommand implements Command,
		DataPackageWizardListener {

	private MorphoDataPackage mdp;
	private ReplicationPolicyPage replicationPolicyPage;

	public EditReplicationPolicyCommand() {
	}

	/**
	 * execute add command
	 * 
	 * @param event
	 *            ActionEvent
	 */
	public void execute(ActionEvent event) {

		// Check if the eml document is the current version before editing it.
		MorphoFrame frame = UIController.getInstance().getCurrentActiveWindow();
		EMLTransformToNewestVersionDialog dialog = null;
		try {
			dialog = new EMLTransformToNewestVersionDialog(frame, this);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (dialog.getUserChoice() == JOptionPane.NO_OPTION) {
			// if user choose not transform it, stop the action.
			Log.debug(
					2,
					Language.getInstance().getMessage("EMLDocumentIsNotTheLatestVersion_1")
							+ " "
							+ Language.getInstance().getMessage("EMLDocumentIsNotTheLatestVersion_2")
							+ "!");
			return;
		}

	}

	/**
	 * Method from DataPackageWizardListener. When correction wizard finished,
	 * it will show the dialog.
	 */
	public void wizardComplete(Node newDOM, String autoSavedID) {
		mdp = UIController.getInstance().getCurrentAbstractDataPackage();

		if (showDialog()) {

			try {
				// set the replication policy in the SM
				ReplicationPolicy replicationPolicy = replicationPolicyPage.getReplicationPolicy();
				mdp.getAbstractDataPackage().getSystemMetadata().setReplicationPolicy(replicationPolicy);
				//indicate that this is a change to the package so it can be saved
				// TODO: save SM independently from EML file
				mdp.getAbstractDataPackage().setLocation("");
				UIController.showNewPackage(mdp);
			} catch (Exception w) {
				Log.debug(15, "Exception trying to modify replication policy: "
						+ w);
				w.printStackTrace();
				Log.debug(5, "Unable to add replication policy details!");
			}
		}
	}

	/**
	 * Method from DataPackageWizardListener. Do nothing.
	 */
	public void wizardCanceled() {
		Log.debug(45, "Correction wizard cancled");

	}

	/**
	 * Method from DataPackageWizardListener. Do nothing.
	 * 
	 */
	public void wizardSavedForLater() {
		Log.debug(45, "Correction wizard was saved for later usage");
	}

	private boolean showDialog() {

		replicationPolicyPage = new ReplicationPolicyPage();

		AbstractDataPackage adp = mdp.getAbstractDataPackage();

		replicationPolicyPage.setReplicationPolicy(adp.getSystemMetadata().getReplicationPolicy());
		boolean pageCanHandleAllData = true;

		ModalDialog dialog = null;
		if (pageCanHandleAllData) {
			dialog = new ModalDialog(
					replicationPolicyPage, 
					UIController.getInstance().getCurrentActiveWindow(),
					UISettings.POPUPDIALOG_WIDTH, 
					UISettings.POPUPDIALOG_HEIGHT);
		} else {
			return false;
		}

		return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
	}

}
