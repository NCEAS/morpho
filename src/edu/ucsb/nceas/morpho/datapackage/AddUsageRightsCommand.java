/**
 *  '$RCSfile: AddUsageRightsCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-03-25 21:21:44 $'
 * '$Revision: 1.5 $'
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
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
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
import edu.ucsb.nceas.morpho.framework.EditorInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.framework.EditingCompleteListener;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.UsageRights;

/**
 * Class to handle add usage command
 */
public class AddUsageRightsCommand implements Command {

  private final String DATAPACKAGE_RIGHTS_GENERIC_NAME = "intellectualRights";

  //generic name for lookup in eml listings
  private final String USAGE_SUBTREE_NODENAME = "/intellectualRights/";


  public AddUsageRightsCommand() {}


  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {

    adp = UIController.getInstance().getCurrentAbstractDataPackage();

    if (showUsageDialog()) {

      try {
        insertUsage();
        UIController.showNewPackage(adp);
      } catch (Exception w) {
        Log.debug(20, "Exception trying to modify usage DOM");
      }
    }
  }


  private boolean showUsageDialog() {

    ServiceController sc;
    DataPackageWizardInterface dpwPlugin = null;
    try {
      sc = ServiceController.getInstance();
      dpwPlugin = (DataPackageWizardInterface)sc.getServiceProvider(
          DataPackageWizardInterface.class);

    } catch (ServiceNotHandledException se) {

      Log.debug(6, se.getMessage());
      se.printStackTrace();
    }

    if (dpwPlugin == null)return false;

    usagePage = (UsageRights)dpwPlugin.getPage(DataPackageWizardInterface.
                                               USAGE_RIGHTS);

    OrderedMap existingValuesMap = null;
    usageRoot = adp.getSubtree(DATAPACKAGE_RIGHTS_GENERIC_NAME, 0);

    if (usageRoot != null) {
      existingValuesMap = XMLUtilities.getDOMTreeAsXPathMap(usageRoot);
    }
    Log.debug(45,
              "sending previous data to usage page -\n\n" + existingValuesMap);

    boolean pageCanHandleAllData
        = usagePage.setPageData(existingValuesMap, null);

    ModalDialog dialog = null;
    if (pageCanHandleAllData) {

      dialog = new ModalDialog(usagePage,
                               UIController.getInstance().
                               getCurrentActiveWindow(),
                               UISettings.POPUPDIALOG_WIDTH,
                               UISettings.POPUPDIALOG_HEIGHT);
    } else {

      UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
          DATAPACKAGE_RIGHTS_GENERIC_NAME, 0);
      return false;
    }
    return (dialog.USER_RESPONSE == ModalDialog.OK_OPTION);
  }



  private void insertUsage() {

    OrderedMap map = usagePage.getPageData(USAGE_SUBTREE_NODENAME);

    Log.debug(45, "\n insertProject() Got usage details from usage page -\n\n"
              + map.toString());

    if (map==null || map.isEmpty()) {
      Log.debug(5, "Unable to get usage details from input!");
      return;
    }

    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
    Document doc = impl.createDocument("", "intellectualRights", null);

    usageRoot = doc.getDocumentElement();


    try {
      XMLUtilities.getXPathMapAsDOMTree(map, usageRoot);

    } catch (TransformerException w) {
      Log.debug(5, "Unable to add usage details to package!");
      Log.debug(20, "TransformerException (" + w + ") calling "
                +"XMLUtilities.getXPathMapAsDOMTree(map, usageRoot) with \n"
                +"map = " + map
                +" and usageRoot = " + usageRoot);
      w.printStackTrace();
      return;
    }

    //delete old project from datapackage
    adp.deleteSubtree(DATAPACKAGE_RIGHTS_GENERIC_NAME, 0);

    // add to the datapackage
    Node check = adp.insertSubtree(DATAPACKAGE_RIGHTS_GENERIC_NAME, usageRoot, 0);

    if (check != null) {
      Log.debug(45, "added new usage details to package...");
    } else {
      Log.debug(5,
                "** ERROR: Unable to add new usage details to package... **");
    }
  }


  private Node usageRoot;
  private AbstractDataPackage adp;
  private MorphoFrame morphoFrame;
  private DataViewContainerPanel dataViewContainerPanel;
  private UsageRights usagePage;

//  private AbstractUIPage usagePage;
}
