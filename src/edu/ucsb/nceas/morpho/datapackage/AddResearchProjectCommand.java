/**
 *  '$RCSfile: AddResearchProjectCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-20 00:44:55 $'
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

/**
 * Class to handle add project command
 */
public class AddResearchProjectCommand implements Command {

  private final String DATAPACKAGE_PROJECT_GENERIC_NAME = "project";

  public AddResearchProjectCommand() {}


  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {

    dataViewContainerPanel = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();

    if (morphoFrame == null) {

      Log.debug(20, "AddResearchProjectCommand - morphoFrame==null");
      Log.debug(5, "Unable to open project details!");
      return;
    }
    dataViewContainerPanel = morphoFrame.getDataViewContainerPanel();

    if (dataViewContainerPanel==null) {

      Log.debug(20, "AddResearchProjectCommand - dataViewContainerPanel==null");
      Log.debug(5, "Unable to open project details!");
      return;
    }
    adp = dataViewContainerPanel.getAbstractDataPackage();

    if (showProjectDialog()) {

      try {
        insertProject();
      } catch (Exception w) {
        Log.debug(20, "Exception trying to modify project DOM: "+w);
        w.printStackTrace();
        Log.debug(5, "Unable to add project details!");
      }
    }
  }


  private boolean showProjectDialog() {

    ServiceController sc;
    DataPackageWizardInterface dpwPlugin = null;
    try {
      sc = ServiceController.getInstance();
      dpwPlugin = (DataPackageWizardInterface) sc.getServiceProvider(
          DataPackageWizardInterface.class);

    } catch (ServiceNotHandledException se) {

        Log.debug(6, se.getMessage());
        se.printStackTrace();
    }
    if (dpwPlugin == null) return false;

    projectPage = dpwPlugin.getPage(DataPackageWizardInterface.PROJECT);

    OrderedMap existingValuesMap = null;
    projectRoot = adp.getSubtree(DATAPACKAGE_PROJECT_GENERIC_NAME, 0);

    if (projectRoot!=null) {
      existingValuesMap = XMLUtilities.getDOMTreeAsXPathMap(projectRoot);
    }
    Log.debug(45, "sending previous data to projectPage -\n\n" + existingValuesMap);

    projectPage.setPageData(existingValuesMap, null);

    ModalDialog dialog = new ModalDialog(projectPage,
                            UIController.getInstance().getCurrentActiveWindow(),
                            UISettings.POPUPDIALOG_WIDTH,
                            UISettings.POPUPDIALOG_HEIGHT);

    return (dialog.USER_RESPONSE==ModalDialog.OK_OPTION);
  }


  private void insertProject() {

    OrderedMap map = projectPage.getPageData("/");

Log.debug(45, "got project details from Project page -\n\n" + map.toString());

    if (map==null || map.isEmpty()) {
      Log.debug(5, "Unable to get project details from input!");
      return;
    }
    if (projectRoot==null) {
      DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
      Document doc = impl.createDocument("", "project", null);

      projectRoot = doc.getDocumentElement();

    } else {

      XMLUtilities.removeAllChildren(projectRoot);
    }

    try {
Log.debug(45, "adding project details to package -\n\n" + map);

      XMLUtilities.getXPathMapAsDOMTree(map, projectRoot);

    } catch (TransformerException w) {
      Log.debug(5, "Unable to add project details to package!");
      Log.debug(20, "TransformerException (" + w + ") calling "
                +"XMLUtilities.getXPathMapAsDOMTree(map, projectRoot) with \n"
                +"map = " + map
                +" and projectRoot = " + projectRoot);
      w.printStackTrace();
    }
    // add to the datapackage
    adp.insertSubtree(DATAPACKAGE_PROJECT_GENERIC_NAME, projectRoot, 0);

Log.debug(45, "added project details to package -\n\n"
        + XMLUtilities.getDOMTreeAsString(projectRoot));
  }


  private Node projectRoot;
  private AbstractDataPackage adp;
  private MorphoFrame morphoFrame;
  private DataViewContainerPanel dataViewContainerPanel;
  private AbstractUIPage projectPage;
}
