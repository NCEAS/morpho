/**
 *  '$RCSfile: AddResearchProjectCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-18 06:03:17 $'
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

import java.awt.event.ActionEvent;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.transform.TransformerException;

/**
 * Class to handle add project command
 */
public class AddResearchProjectCommand implements Command {


  public AddResearchProjectCommand() {}


  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {

    resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();

    if (morphoFrame != null) resultPane = morphoFrame.getDataViewContainerPanel();


    // make sure resulPanel is not null
    if (resultPane==null) return;

    if (showProjectDialog()) {

      try {
        insertNewProject();
      } catch (Exception w) {
        Log.debug(20, "Exception trying to modify project DOM");
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
    }
    catch (ServiceNotHandledException se) {
      Log.debug(6, se.getMessage());
    }

    if (dpwPlugin == null) return false;

    projectPage = dpwPlugin.getPage(DataPackageWizardInterface.PROJECT);
    ModalDialog dialog = new ModalDialog(projectPage,
                            UIController.getInstance().getCurrentActiveWindow(),
                            UISettings.POPUPDIALOG_WIDTH,
                            UISettings.POPUPDIALOG_HEIGHT);

    return (dialog.USER_RESPONSE==ModalDialog.OK_OPTION);
  }


  private void insertNewProject() {

    OrderedMap map = projectPage.getPageData();
    AbstractDataPackage adp = resultPane.getAbstractDataPackage();

    DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
    Document doc = impl.createDocument("", "project", null);

    Node root = doc.getDocumentElement();

    try {
      XMLUtilities.getXPathMapAsDOMTree(map, root);

// how do we add this to the datapackage???
//      adp.insertCoverage(root);
      Log.debug(5, "Need to add project details to package!");

    } catch (TransformerException w) {
      Log.debug(5, "Unable to add project details to package!");
      w.printStackTrace();
    }
  }

  private MorphoFrame morphoFrame = null;
  private DataViewContainerPanel resultPane;
  private AbstractUIPage projectPage;
}
