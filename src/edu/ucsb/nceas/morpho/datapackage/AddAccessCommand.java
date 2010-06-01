/**
 *  '$RCSfile: AddAccessCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-20 18:26:05 $'
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
//import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.Language;
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
import edu.ucsb.nceas.utilities.XMLUtilities;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

/**
 * Class to handle add access command
 */
public class AddAccessCommand
    implements Command, DataPackageWizardListener {

  //generic name for lookup in eml listings
  private final String DATAPACKAGE_ACCESS_GENERIC_NAME = "access";

  //generic name for lookup in eml listings
  private final String ACCESS_SUBTREE_NODENAME = "/access/";

  public AddAccessCommand() {}

  /**
   * execute add command
   *
   * @param event ActionEvent
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
		  Log.debug(5, e.getLocalizedMessage());
		  return;
	  }
	  if (dialog.getUserChoice() == JOptionPane.NO_OPTION)
	 {
		   // if user choose not transform it, stop the action.
			Log.debug(2,
					/*"The current EML document is not the latest version."*/ Language.getInstance().getMessages("EMLDocumentIsNotTheLatestVersion_1") + " "
					+/*" You should transform it first!"*/ Language.getInstance().getMessages("EMLDocumentIsNotTheLatestVersion_2") + "!"
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
	  adp = UIController.getInstance().getCurrentAbstractDataPackage();

	    if (showAccessDialog()) {

	      try {
	        insertAccess();
	        UIController.showNewPackage(adp);
	      }
	      catch (Exception w) {
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
  
  /**
   *  Method from DataPackageWizardListener. Do nothing.
   *
   */
  public void wizardSavedForLater()
  {
    Log.debug(45, "Correction wizard was saved for later usage");
  }

  private boolean showAccessDialog() {

    ServiceController sc;
    DataPackageWizardInterface dpwPlugin = null;
    try {
      sc = ServiceController.getInstance();
      dpwPlugin = (DataPackageWizardInterface) sc.getServiceProvider(
          DataPackageWizardInterface.class);

    }
    catch (ServiceNotHandledException se) {

      Log.debug(6, se.getMessage());
      se.printStackTrace();
    }
    if (dpwPlugin == null) {
      return false;
    }

    accessPage = dpwPlugin.getPage(DataPackageWizardInterface.ACCESS);

    OrderedMap existingValuesMap = null;
    accessRoot = adp.getSubtree(DATAPACKAGE_ACCESS_GENERIC_NAME, 0);

    if (accessRoot != null) {
      existingValuesMap = XMLUtilities.getDOMTreeAsXPathMap(accessRoot);
    }
    Log.debug(45,
              "sending previous data to accessPage -\n\n" + existingValuesMap);

    boolean pageCanHandleAllData
        = accessPage.setPageData(existingValuesMap, ACCESS_SUBTREE_NODENAME);

    ModalDialog dialog = null;
    if (pageCanHandleAllData) {

      dialog = new ModalDialog(accessPage,
                               UIController.getInstance().
                               getCurrentActiveWindow(),
                               UISettings.POPUPDIALOG_WIDTH,
                               UISettings.POPUPDIALOG_HEIGHT);
    }
    else {

      UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
          DATAPACKAGE_ACCESS_GENERIC_NAME, 0);
      return false;
    }

    return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
  }

  private void insertAccess() {

    OrderedMap map = accessPage.getPageData(ACCESS_SUBTREE_NODENAME);

    Log.debug(45, "\n insertAccess() Got access details from Access page -\n"
              + map);

    if (map == null || map.isEmpty()) {
    	Log.debug(30, "removing access rules from top level!");
    	adp.deleteSubtree(DATAPACKAGE_ACCESS_GENERIC_NAME, 0);
        return;
    }

    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
    Document doc = impl.createDocument("", "access", null);

    accessRoot = doc.getDocumentElement();

    try {
      XMLUtilities.getXPathMapAsDOMTree(map, accessRoot);

    }
    catch (TransformerException w) {
      Log.debug(5, "Unable to add access details to package!");
      Log.debug(15, "TransformerException (" + w + ") calling "
                + "XMLUtilities.getXPathMapAsDOMTree(map, accessRoot) with \n"
                + "map = " + map
                + " and accessRoot = " + accessRoot);
      return;
    }
    //delete old access from datapackage
    adp.deleteSubtree(DATAPACKAGE_ACCESS_GENERIC_NAME, 0);

    // add to the datapackage
    Node check = adp.insertSubtree(DATAPACKAGE_ACCESS_GENERIC_NAME, accessRoot,
                                   0);

    if (check != null) {
      Log.debug(45, "added new access details to package...");
    }
    else {
      Log.debug(5, "** ERROR: Unable to add new access details to package **");
    }
  }

  private Node accessRoot;
  private AbstractDataPackage adp;
  private AbstractUIPage accessPage;
}
