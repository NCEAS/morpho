/**
 *  '$RCSfile: DataPackageWizardPlugin.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-07-29 16:56:07 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import edu.ucsb.nceas.morpho.Morpho;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;


/**
 *  Main controller class for creating and starting a Data Package Wizard Plugin
 */

public class DataPackageWizardPlugin implements DataPackageWizardInterface,
                                                PluginInterface, 
                                                ServiceProvider {


  private WizardContainerFrame wizardContainerFrame;

  /**
  *  Constructor
  */
  public DataPackageWizardPlugin() {
  }



  /**
  *  Required by PluginInterface; called automatically at runtime
  *
  *  @param morpho    a reference to the <code>Morpho</code>
  */
  public void initialize(Morpho morpho) {
    wizardContainerFrame = new WizardContainerFrame();
    wizardContainerFrame.setVisible(false);
    try {
      ServiceController services = ServiceController.getInstance();
      services.addService(DataPackageWizardInterface.class, this);
      Log.debug(20, "Service added: DataPackageWizardInterface.");
    } 
    catch (ServiceExistsException see) {
      Log.debug(6, "Service registration failed: DataPackageWizardInterface");
      Log.debug(6, see.toString());
    }
  }

  /**
   *  Required by DataPackageWizardInterface:
   *  method to start the wizard
   */
  public void startWizard() {

    wizardContainerFrame.setBounds(
    WizardSettings.WIZARD_X_COORD, WizardSettings.WIZARD_Y_COORD, 
    WizardSettings.WIZARD_WIDTH,   WizardSettings.WIZARD_HEIGHT );
    wizardContainerFrame.setVisible(true);
  }
}
