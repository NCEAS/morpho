/**
 *  '$RCSfile: ToolPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-10-30 18:58:23 $'
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

package edu.ucsb.nceas.morpho.framework;


import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.*;

/**
 * Class that implements the plugin for package editing
 */
public class ToolPlugin 
       implements PluginInterface, ServiceProvider
{
  /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** Store our menus and toolbars */
  private Action[] menuActions = null;
  private Action[] toolbarActions = null;

  /**
   * Construct the plugin.  Initialize our menus and toolbars.
   */
  public ToolPlugin()
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

    // Add menus, and toolbars // MBJ removed because it isn't needed in the UI
    //framework.addMenu("File", new Integer(1), menuActions);
    //framework.addToolbarActions(toolbarActions);

    // Register Services
/*    try 
    {
      framework.addService(DataPackageInterface.class, this);
      framework.debug(20, "Service added: DataPackageInterface.");
    } 
    catch (ServiceExistsException see) 
    {
      framework.debug(6, "Service registration failed: DataPackageInterface.");
      framework.debug(6, see.toString());
    }
*/
    cf.debug(20, "Init Tool Plugin"); 
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() 
  {
    // Set up the menus for the application
    menuActions = new Action[1];
    Action textItemAction = new AbstractAction("Extract Descriptions from Text-based Table...") 
    {
      public void actionPerformed(ActionEvent e) 
      {
        framework.debug(20, "Action fired: New ToolPlugin");
          (new TextImportWizard(null, null)).setVisible(true);
      }

    };
    textItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/table/ColumnInsertAfter16.gif")));
    textItemAction.putValue(Action.SHORT_DESCRIPTION, "Extract Descriptions from Text-based Table");
    textItemAction.putValue("menuPosition", new Integer(3));
    menuActions[0] = textItemAction;

    // Set up the toolbar for the application
    toolbarActions = new Action[1];
    toolbarActions[0] = textItemAction;
  }

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
    //we dont' need any!
  }
}
