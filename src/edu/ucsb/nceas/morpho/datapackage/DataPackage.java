/**
 *  '$RCSfile: DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-07 21:14:07 $'
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

import edu.ucsb.nceas.morpho.framework.*;

import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;

public class DataPackage 
       implements PluginInterface, ServiceProvider, DataPackageInterface
{
  /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** Store our menus and toolbars */
  private Action[] menuActions = null;
  private Action[] toolbarActions = null;

  /**
   * This hashtable has all of the references of ids to filenames (or urls in
   * the case of a file on metacat).  
   */
  Hashtable packagecomponents = new Hashtable();

  /** WHAT IS THIS FOR, CHAD? */
  IdContainer accNum;

  /**
   * Construct the plugin.  Initialize our menus and toolbars.
   */
  public DataPackage()
  {
    // Create the menus and toolbar actions, will register later
    initializeActions();
  }

  /** 
   * The plugin must store a reference to the ClientFramework 
   * in order to be able to call the services available through 
   * the framework.  This is also the time to register menus
   * and toolbars with the framework.
   */
  public void initialize(ClientFramework cf)
  {
    this.framework = cf;
    this.config = framework.getConfiguration();
    loadConfigurationParameters();

    // Create the IdContainer
    accNum = new IdContainer(cf);

    // Add menus, and toolbars
    //framework.addMenu("File", new Integer(1), menuActions);
    //framework.addToolbarActions(toolbarActions);

    // Register Services
    try {
      framework.addService(DataPackageInterface.class, this);
      framework.debug(6, "Service added: DataPackageInterface.");
    } catch (ServiceExistsException see) {
      framework.debug(6, "Service registration failed: DataPackageInterface.");
      framework.debug(6, see.toString());
    }

    cf.debug(9, "Init DataPackage Plugin"); 
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() {
    // Set up the menus for the application
/*
    menuActions = new Action[3];
    Action searchItemAction = new AbstractAction("Search...") {
      public void actionPerformed(ActionEvent e) {
        framework.debug(1, "Action fired: Search Dialog :-)");
      }
    };
    searchItemAction.putValue(Action.SHORT_DESCRIPTION, "Search for data");
    searchItemAction.putValue("menuPosition", new Integer(0));
    menuActions[0] = searchItemAction;

    Action reviseItemAction = new AbstractAction("Revise...") {
      public void actionPerformed(ActionEvent e) {
        framework.debug(1, "Action fired: Revise Search :-)");
      }
    };
    reviseItemAction.putValue(Action.SHORT_DESCRIPTION, 
                              "Revise current search");
    reviseItemAction.putValue("menuPosition", new Integer(1));
    menuActions[1] = reviseItemAction;

    Action refreshItemAction = new AbstractAction("Refresh") {
      public void actionPerformed(ActionEvent e) {
        framework.debug(1, "Action fired: Refresh Search :-)");
      }
    };
    refreshItemAction.putValue(Action.SHORT_DESCRIPTION, 
                              "Refresh search results");
    refreshItemAction.putValue("menuPosition", new Integer(2));
    refreshItemAction.putValue(Action.DEFAULT, 
                               ClientFramework.SEPARATOR_PRECEDING);
    menuActions[2] = refreshItemAction;

    // Set up the toolbar for the application
    toolbarActions = new Action[3];
    toolbarActions[0] = searchItemAction;
    toolbarActions[1] = reviseItemAction;
    toolbarActions[2] = refreshItemAction;
*/
  }

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
  }


  public void openDataPackage(String location, String identifier)
  {
    framework.debug(9, "Got service request to open: " + identifier +
                    " from " + location + ".");
  }
}
