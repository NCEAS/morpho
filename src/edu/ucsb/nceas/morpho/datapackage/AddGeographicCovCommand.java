/**
 *  '$RCSfile: AddGeographicCovCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-18 00:23:33 $'
 * '$Revision: 1.3 $'
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

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.util.Iterator;
import java.util.Set;

import java.awt.event.ActionEvent;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import edu.ucsb.nceas.morpho.util.UISettings;

/**
 * Class to handle add temporal coverage command
 */
public class AddGeographicCovCommand implements Command {

  /* Flag if need to add coverage info*/
  private boolean infoAddFlag = false;

  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  private DataViewContainerPanel resultPane;
  private AbstractUIPage geographicPage;

  public AddGeographicCovCommand() {
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
      resultPane = AddDocumentationCommand.
          getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    }

    // make sure resulPanel is not null
    if (resultPane != null) {

      showGeographicDialog();
      if (infoAddFlag) {

        try {
          insertNewGeographic();
        }
        catch (Exception w) {
          Log.debug(20, "Exception trying to modify coverage DOM");
        }
      }

    }
  }


  private void showGeographicDialog() {

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

    geographicPage = dpwPlugin.getPage(
        DataPackageWizardInterface.GEOGRAPHIC);
    ModalDialog wpd = new ModalDialog(geographicPage,
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

  private void insertNewGeographic() {

    covRoot = null;
    map = geographicPage.getPageData();
    adp = resultPane.getAbstractDataPackage();

    mapSet = map.keySet();
    mapSetIt = mapSet.iterator();

    while(mapSetIt.hasNext()){
      key = mapSetIt.next();
      newMap = new OrderedMap();

      int i = key.toString().indexOf("[");
      String keySt = key.toString().substring(0,i) + key.toString().substring(i+3);

      newMap.put(keySt,map.get(key));

      if(key.toString().indexOf("startDateTime") > 0){
        key = mapSetIt.next();

        i = key.toString().indexOf("[");
        keySt = key.toString().substring(0,i) + key.toString().substring(i+3);

        newMap.put(keySt,map.get(key));
      }

      try {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        Document doc = impl.createDocument("", "temporalCoverage", null);

        covRoot = doc.getDocumentElement();
        XMLUtilities.getXPathMapAsDOMTree(newMap, covRoot);

        adp.insertCoverage(covRoot);
      }
      catch (Exception w) {
        Log.debug(5, "Unable to add OrderMap elements to DOM");
        w.printStackTrace();
      }
    }

    return;
  }

}