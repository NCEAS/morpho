/**
 *  '$RCSfile: AddTemporalCovCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-01-13 22:00:56 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Temporal;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DataPackageWizardPlugin;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;

import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;
import edu.ucsb.nceas.morpho.util.XMLUtil;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.Point;

/**
 * Class to handle add temporal coverage command
 */
public class AddTemporalCovCommand
    implements Command {

  /* Flag if need to add a column*/
  private boolean infoAddFlag = false;

  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  private DataViewContainerPanel resultPane;
  private Temporal temporalPage;
  private DataViewer dataView;


  public AddTemporalCovCommand() {
  }

  /**
   * execute add command
   */
  public void execute(ActionEvent event) {

    resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();

    if (morphoFrame != null) {
      resultPane = AddDocumentationCommand.
          getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    } //if

    // make sure resulPanel is not null
    if (resultPane != null) {

      dataView = resultPane.getCurrentDataViewer();
      if (dataView != null){

        showTemporalDialog();
        if (infoAddFlag) {

          try {
              insertNewTemporal();
          } //try

          catch (Exception w) {
            Log.debug(20, "Exception trying to modify attribute DOM");
          } //catch
        }
      }

    } //if
  } //execute


  private void showTemporalDialog() {
    MorphoFrame mf = UIController.getInstance().getCurrentActiveWindow();
    Point curLoc = mf.getLocationOnScreen();
    Dimension dim = mf.getSize();
    ServiceController sc;
    DataPackageWizardPlugin dpwPlugin = null;
    try {
      sc = ServiceController.getInstance();
      dpwPlugin = (DataPackageWizardPlugin) sc.getServiceProvider(
          DataPackageWizardInterface.class);
    }
    catch (ServiceNotHandledException se) {
      Log.debug(6, se.getMessage());
    }
    if (dpwPlugin == null) {
      return;
    }
    temporalPage = (Temporal) dpwPlugin.getPage(
        DataPackageWizardInterface.TEMPORAL);
    WizardPopupDialog wpd = new WizardPopupDialog(temporalPage, mf, false);
    wpd.setSize(WizardSettings.DIALOG_WIDTH, WizardSettings.ATTR_DIALOG_HEIGHT);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == WizardPopupDialog.OK_OPTION) {
      infoAddFlag = true;
    }
    else {
      infoAddFlag = false;
    }

    return;
  }

  private void insertNewTemporal()
  {
          OrderedMap map = temporalPage.getPageData();
          AbstractDataPackage adp = dataView.getAbstractDataPackage();

          //int entityIndex = dataView.getEntityIndex();
          //adp.insertAttribute(entityIndex, attrObject, index);
          //adp.showPackageSummary();
          return;
  }

} //class CancelCommand
