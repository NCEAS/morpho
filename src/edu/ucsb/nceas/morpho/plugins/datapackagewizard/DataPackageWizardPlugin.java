/**
 *  '$RCSfile: DataPackageWizardPlugin.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-02-24 20:54:11 $'
 * '$Revision: 1.22 $'
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

import edu.ucsb.nceas.utilities.XMLUtilities;

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyMainPage;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;

import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.editor.*;


import org.w3c.dom.Node;


/**
 *  Main controller class for creating and starting a Data Package Wizard Plugin
 */

public class DataPackageWizardPlugin implements PluginInterface,
                                                ServiceProvider,
                                                DataPackageWizardInterface {

  private static WizardContainerFrame dpWiz;

  /**
   *  Constructor
   */
  public DataPackageWizardPlugin() {

    dpWiz = new WizardContainerFrame();
    dpWiz.setVisible(false);
  }


  /**
   *  Required by PluginInterface; called automatically at runtime
   *
   *  @param morpho    a reference to the <code>Morpho</code>
   */
  public void initialize(Morpho morpho) {

    try {
      ServiceController services = ServiceController.getInstance();
      services.addService(DataPackageWizardInterface.class, this);
      Log.debug(20, "Service added: DataPackageWizardInterface.");

    } catch (ServiceExistsException see) {
      Log.debug(6, "Service registration failed: DataPackageWizardInterface");
      Log.debug(6, see.toString());
    }
  }


  /**
   *  Required by DataPackageWizardInterface:
   *  method to start the Package wizard
   *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Package Wizard has finished
   */
  public void startPackageWizard(DataPackageWizardListener listener) {
    startWizardAtPage(WizardSettings.PACKAGE_WIZ_FIRST_PAGE_ID, true, listener,
                      "New Datapackage Wizard");
	responsiblePartyList.clear();
	PartyMainPage.RESPONSIBLE_PARTY_REFERENCE_COUNT = 0;
  }


  /**
   *  Required by DataPackageWizardInterface:
   *  method to start the Entity wizard
   *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Entity Wizard has finished
   */
  public void startEntityWizard(DataPackageWizardListener listener) {

    startWizardAtPage(WizardSettings.ENTITY_WIZ_FIRST_PAGE_ID, false, listener,
                      "New Datatable Wizard");
  }
	
	/**
   *  method to start the wizard at a given page
   *
	 *	@param pageID	 the ID of the page from where the wizard is to be started
	 *
   *  @param listener the <code>DataPackageWizardListener</code> to be called
   *                  back when the Entity Wizard has finished
   */
	 
	 
	public void startWizardAtPage(String pageID, DataPackageWizardListener listener, String title) {
		
		startWizardAtPage(pageID, false, listener, title);
	}
	
  private void startWizardAtPage(String pageID, boolean showPageCount,
                        DataPackageWizardListener listener, String frameTitle) {

    dpWiz.setDataPackageWizardListener(listener);
    dpWiz.setBounds(
                  WizardSettings.WIZARD_X_COORD, WizardSettings.WIZARD_Y_COORD,
                  WizardSettings.WIZARD_WIDTH,   WizardSettings.WIZARD_HEIGHT );
    dpWiz.setCurrentPage(pageID);
    dpWiz.setShowPageCountdown(showPageCount);
    dpWiz.setTitle(frameTitle);
    dpWiz.setVisible(true);
  }



  /**
   *  returns the WizardPage with the corresponding pageID provided
   *
   *  @param pageID the String pageID for the WizardPage to be returned
   *
   *  @return  the corresponding WizardPage with this ID
   */
  public AbstractWizardPage getPage(String pageID) {

    return WizardPageLibrary.getPage(pageID);
  }

  // for testing/development
  public static void main(String[] args) {

  // TEXT IMPORT WIZARD NEEDS MORPHO TO GET CONFIG
    Morpho.main(null);
    ///////////////////////

    Log.setDebugLevel(55);
    DataPackageWizardPlugin plugin = new DataPackageWizardPlugin();
    //plugin.initialize(Morpho.thisStaticInstance);
    plugin.startPackageWizard(
      new DataPackageWizardListener() {

        public void wizardComplete(Node newDOM) {
        Log.debug(1,"Wizard complete - Will now create an AbstractDataPackage..");
          AbstractDataPackage dp = DataPackageFactory.getDataPackage(newDOM);

         Log.debug(1,"AbstractDataPackage complete - Will now show in an XML Editor..");
         Node domnode = dp.getMetadataNode();
          DocFrame df = new DocFrame();
          df.setVisible(true);
          df.initDoc(null, domnode, null, null);

          Log.debug(45, "\n\n********** Wizard finished: DOM:");
          Log.debug(45, XMLUtilities.getDOMTreeAsString(newDOM, false));

        }

        public void wizardCanceled() {

          Log.debug(45, "\n\n********** Wizard canceled!");
          System.exit(0);
        }
      }
    );
    dpWiz.setVisible(true);
  }
}
