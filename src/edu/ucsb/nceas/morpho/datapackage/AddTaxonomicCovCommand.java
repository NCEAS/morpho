/**
 *  '$RCSfile: AddTaxonomicCovCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Perumal Sambasivam
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-03-16 19:20:51 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.Taxonomic;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DataPackageWizardPlugin;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;

import java.awt.event.ActionEvent;


/**
 * Class to handle addition of taxonomic coverage to the data package
 */
public class AddTaxonomicCovCommand implements Command {

  /* Flag if need to add coverage info*/
  private boolean infoAddFlag = false;

  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  private DataViewContainerPanel resultPane;
  private Taxonomic taxonomicPage;
  private DataViewer dataView;

  public AddTaxonomicCovCommand() {
  }

  /**
   * execute add command
   */
  public void execute(ActionEvent event) {
		
    showTaxonomicDialog();
    
  }


  private void showTaxonomicDialog() {

    MorphoFrame mf = UIController.getInstance().getCurrentActiveWindow();

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

    taxonomicPage = (Taxonomic) dpwPlugin.getPage(
        DataPackageWizardInterface.TAXONOMIC);
    WizardPopupDialog wpd = new WizardPopupDialog(taxonomicPage, mf, false);

    wpd.setSize(WizardSettings.DIALOG_WIDTH, WizardSettings.DIALOG_HEIGHT);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == WizardPopupDialog.OK_OPTION) {
      
    }
    else {
      
    }

    return;
  }
	
}
