/**
 *  '$RCSfile: AddMethodCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-03-22 19:27:20 $'
 * '$Revision: 1.1 $'
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

import java.util.Iterator;
import java.util.Set;

import java.awt.event.ActionEvent;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Class to handle add method command
 */
public class AddMethodCommand implements Command {

  /* Flag if need to add coverage info*/
  private boolean infoAddFlag = false;

  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  private DataViewContainerPanel resultPane;
  private AbstractUIPage methodPage;

  public AddMethodCommand() {
  }


  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {

    resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();

    if (morphoFrame != null) {
      resultPane =  morphoFrame.getDataViewContainerPanel();
    }

    // make sure resulPanel is not null
    if (resultPane != null) {

      showMethodDialog();
      if (infoAddFlag) {

        try {
          insertNewMethod();
        }
        catch (Exception w) {
          Log.debug(20, "Exception trying to modify method DOM");
        }
      }

    }
  }


  private void showMethodDialog() {

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

    if (dpwPlugin == null) {
      return;
    }

    methodPage = dpwPlugin.getPage(
        DataPackageWizardInterface.METHODS);
    ModalDialog wpd = new ModalDialog(methodPage,
                                UIController.getInstance().getCurrentActiveWindow(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT, false);

    wpd.setSize(UISettings.POPUPDIALOG_WIDTH, UISettings.POPUPDIALOG_HEIGHT);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {
      infoAddFlag = true;
    }
    else {
      infoAddFlag = false;
    }

    return;
  }

  private Node covRoot;
  private Set mapSet;
  private Iterator mapSetIt;
  private Object key;
  private OrderedMap map, newMap;
  AbstractDataPackage adp;

  private void insertNewMethod() {

    return;
  }

}
