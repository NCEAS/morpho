/**
 *  '$RCSfile: AddGeographicCovCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-02-04 00:38:33 $'
 * '$Revision: 1.12 $'
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
import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
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
import javax.swing.JOptionPane;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Class to handle add geographic coverage command
 */
public class AddGeographicCovCommand implements Command {

  /* Flag if need to add coverage info*/
  private boolean infoAddFlag = false;

  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  private DataViewContainerPanel resultPane;
  private AbstractUIPage geographicPage;

  private AbstractDataPackage adp;
  
  public AddGeographicCovCommand() {
  }


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
		  dialog = new EMLTransformToNewestVersionDialog(frame);
	  }
	  catch(Exception e)
	  {
		  return;
	  }
	  if (dialog.getUserChoice() == JOptionPane.NO_OPTION)
	 {
		   // if user choose not transform it, stop the action.
			Log.debug(2,"The current EML document is not the latest version. You should transform it first!");
			return;
	 }
    adp = UIController.getInstance().getCurrentAbstractDataPackage();

    resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();

    if (morphoFrame != null) {
      resultPane = morphoFrame.getDataViewContainerPanel();
    }

    // make sure resulPanel is not null
    if (resultPane != null) {

      showGeographicDialog();
      if (infoAddFlag) {

        try {
					// inserting new, so remove old
					adp.removeGeographicNodes();
          insertNewGeographic();
          UIController.showNewPackage(adp);
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

    geographicPage = dpwPlugin.getPage(DataPackageWizardInterface.GEOGRAPHIC);
        
    ModalDialog wpd = new ModalDialog(geographicPage,
                                UIController.getInstance().getCurrentActiveWindow(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT, false);

    wpd.setSize(UISettings.POPUPDIALOG_WIDTH, UISettings.POPUPDIALOG_HEIGHT);
    // note that the location of 'insertCurrentData' is significant
    // it must occur after the creation of the ModalDialog so that 'onLoadAction' is fired
    // and the list is emptied
    boolean pageCanHandleAllData = insertCurrentData();
    if (pageCanHandleAllData) {
      wpd.setSize(UISettings.POPUPDIALOG_WIDTH, UISettings.POPUPDIALOG_HEIGHT);
      wpd.setVisible(true);

      if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {
        infoAddFlag = true;
      }
      else {
        infoAddFlag = false;
      }
    } else {
      UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(
          "coverage", 0);      
    }

    return;
  }

  private Node covRoot;
  private Set mapSet;
  private Iterator mapSetIt;
  private OrderedMap map;

  private void insertNewGeographic() {

    covRoot = null;
    map = geographicPage.getPageData("/coverage/geographicCoverage[");
    adp = resultPane.getAbstractDataPackage();


    try {
      DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
      Document doc = impl.createDocument("", "coverage", null);

      covRoot = doc.getDocumentElement();
      XMLUtilities.getXPathMapAsDOMTree(map, covRoot);
      // now the covRoot node may have a number of geographicCoverage children
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

  private boolean insertCurrentData() {
    boolean res = true;
    NodeList geoList = adp.getGeographicNodeList();
    if (geoList==null) return true;
    for (int i=0;i<geoList.getLength();i++) {
       // create a new GeographicPage and add surrogate to list
       OrderedMap geoMap = XMLUtilities.getDOMTreeAsXPathMap(geoList.item(i));
       boolean flag = true;
      if (i==0) {
         flag = geographicPage.setPageData(geoMap, "removeAllRows");
      } else {
         flag = geographicPage.setPageData(geoMap, "");
      }
       if (!flag) res = false;
    }
    return res;
  }
}
