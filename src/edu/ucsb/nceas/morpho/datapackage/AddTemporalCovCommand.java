/**
 *  '$RCSfile: AddTemporalCovCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-03-24 23:37:19 $'
 * '$Revision: 1.10 $'
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
import org.w3c.dom.NodeList;

/**
 * Class to handle add temporal coverage command
 */
public class AddTemporalCovCommand implements Command {

  /* Flag if need to add coverage info*/
  private boolean infoAddFlag = false;

  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  private DataViewContainerPanel resultPane;
  private AbstractUIPage temporalPage;

  private AbstractDataPackage adp;

  public AddTemporalCovCommand() {
  }


  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {

    adp = UIController.getInstance().getCurrentAbstractDataPackage();

    resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();

    if (morphoFrame != null) {
      resultPane =  morphoFrame.getDataViewContainerPanel();
    }

    // make sure resulPanel is not null
    if (resultPane != null) {

      showTemporalDialog();
      if (infoAddFlag) {

        try {
          insertNewTemporal();
        }
        catch (Exception w) {
          Log.debug(20, "Exception trying to modify coverage DOM");
        }
      }

    }
  }


  private void showTemporalDialog() {

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

    temporalPage = dpwPlugin.getPage(
        DataPackageWizardInterface.TEMPORAL);
        
        
    ModalDialog wpd = new ModalDialog(temporalPage,
                                UIController.getInstance().getCurrentActiveWindow(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT, false);
    insertCurrentData();
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

  private void insertNewTemporal() {

    covRoot = null;
    map = temporalPage.getPageData("/coverage/temporalCoverage[");
    adp = resultPane.getAbstractDataPackage();

      try {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        Document doc = impl.createDocument("", "coverage", null);

        covRoot = doc.getDocumentElement();
        XMLUtilities.getXPathMapAsDOMTree(map, covRoot);
        // now the covRoot node may have a number of temporalCoverage children
        NodeList kids = covRoot.getChildNodes();
        for (int i=0;i<kids.getLength();i++) {
          Node kid = kids.item(i);
          adp.insertCoverage(kid);          
        }
      }
      catch (Exception w) {
        Log.debug(5, "Unable to add OrderMap elements to DOM");
        w.printStackTrace();
      }


    return;
  }

  private void insertCurrentData() {
    NodeList tempList = adp.getTemporalNodeList();
    if (tempList==null) return;
    for (int i=0;i<tempList.getLength();i++) {
       // create a new TemporalPage and add surrogate to list
       OrderedMap tempMap = XMLUtilities.getDOMTreeAsXPathMap(tempList.item(i));
//  Log.debug(1, "tempMap: "+tempMap);
       temporalPage.setPageData(tempMap, "");
    }
    adp.removeTemporalNodes();
  }
  
}
