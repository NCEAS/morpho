/**
 *  '$RCSfile: AddMethodCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-03-30 21:34:33 $'
 * '$Revision: 1.2 $'
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

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import javax.xml.transform.TransformerException;

import java.awt.event.ActionEvent;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Class to handle add method command
 */
public class AddMethodCommand
    implements Command {

  //generic name for lookup in eml listings
  private final String DATAPACKAGE_METHOD_GENERIC_NAME = "methods";

  //generic name for lookup in eml listings
  private final String METHOD_SUBTREE_NODENAME = "/methods/";

  public AddMethodCommand() {}

  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {

    adp = UIController.getInstance().getCurrentAbstractDataPackage();

    if (showMethodDialog()) {

      try {
        insertMethod();
        UIController.showNewPackage(adp);
      }
      catch (Exception w) {
        Log.debug(15, "Exception trying to modify method DOM: " + w);
        w.printStackTrace();
        Log.debug(5, "Unable to add method details!");
      }
    }
  }

  private boolean showMethodDialog() {

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

    methodPage = dpwPlugin.getPage(DataPackageWizardInterface.METHODS);

    OrderedMap existingValuesMap = null;
    methodRoot = adp.getSubtree(DATAPACKAGE_METHOD_GENERIC_NAME, 0);

    if (methodRoot != null) {
      existingValuesMap = XMLUtilities.getDOMTreeAsXPathMap(methodRoot);
    }
    Log.debug(45,
              "sending previous data to methodPage -\n\n" + existingValuesMap);

    boolean pageCanHandleAllData
        = methodPage.setPageData(existingValuesMap, METHOD_SUBTREE_NODENAME);

    ModalDialog dialog = null;
    if (pageCanHandleAllData) {

      dialog = new ModalDialog(methodPage,
                               UIController.getInstance().
                               getCurrentActiveWindow(),
                               UISettings.POPUPDIALOG_WIDTH,
                               UISettings.POPUPDIALOG_HEIGHT);
    }
    else {

      UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
          DATAPACKAGE_METHOD_GENERIC_NAME, 0);
      return false;
    }

    return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
  }

  private void insertMethod() {

    OrderedMap map = methodPage.getPageData(METHOD_SUBTREE_NODENAME);

    Log.debug(45, "\n insertMethod() Got method details from Methods page -\n"
              + map.toString());

    if (map == null || map.isEmpty()) {
      Log.debug(5, "Unable to get method details from input!");
      return;
    }

    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
    Document doc = impl.createDocument("", "methods", null);

    methodRoot = doc.getDocumentElement();

    try {
      XMLUtilities.getXPathMapAsDOMTree(map, methodRoot);

    }
    catch (TransformerException w) {
      Log.debug(5, "Unable to add method details to package!");
      Log.debug(15, "TransformerException (" + w + ") calling "
                + "XMLUtilities.getXPathMapAsDOMTree(map, methodRoot) with \n"
                + "map = " + map
                + " and methodRoot = " + methodRoot);
      w.printStackTrace();
      return;
    }
    //delete old method from datapackage
    adp.deleteSubtree(DATAPACKAGE_METHOD_GENERIC_NAME, 0);

    // add to the datapackage
    Node check = adp.insertSubtree(DATAPACKAGE_METHOD_GENERIC_NAME, methodRoot,
                                   0);

    if (check != null) {
      Log.debug(45, "added new method details to package...");
    }
    else {
      Log.debug(5, "** ERROR: Unable to add new method details to package **");
    }
  }

  private Node methodRoot;
  private AbstractDataPackage adp;
  private AbstractUIPage methodPage;
}
