/**
 *  '$RCSfile: QueryPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-10-17 22:56:06 $'
 * '$Revision: 1.100 $'
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

package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ConnectionListener;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.util.*;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class QueryPlugin implements PluginInterface, ConnectionListener,
                                    ServiceProvider, QueryRefreshInterface
{
  /** A reference to the container framework */
  private Morpho morpho = null;

  /** The configuration options object reference from the morpho */
  private ConfigXML config = null;

  /** Query used to find data owned by the user */
  private Query ownerQuery = null;

  /** Tabbed panel that displays the data owned by the user */
  private ResultPanel ownerPanel = null;
  
  /**
   * Construct the query plugin.  Initialize our one tab for the 
   * plugin plus any menus and toolbars.
   */
  public QueryPlugin()
  {
      
  }

  /** 
   * The plugin must store a reference to the Morpho 
   * in order to be able to call the services available through 
   * the framework.  This is also the time to register menus
   * and toolbars with the framework.
   */
  public void initialize(Morpho morpho)
  {
    this.morpho = morpho;
    this.config = morpho.getConfiguration();
    //loadConfigurationParameters();
   
    // Create the menus and toolbar actions, will register later
    initializeActions(); 

    // Register Services
    try
    {
        ServiceController services = ServiceController.getInstance();
        services.addService(QueryRefreshInterface.class, this);
        Log.debug(20, "Service added: QueryRefreshInterface.");
    } catch (ServiceExistsException see) {
        Log.debug(6, "Service registration failed: QueryRefreshInterface.");
        Log.debug(6, see.toString());
    }
    
        
    // Listen for changes to the connection status
    morpho.addConnectionListener(this);
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() 
  {

    UIController controller = UIController.getInstance();

    // Action for search
    GUIAction searchItemAction = new GUIAction("Search...", null,
                                        new SearchCommand(null, morpho));
    searchItemAction.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Search16.gif")));
    searchItemAction.setToolTipText("Search for data");
    searchItemAction.setMenuItemPosition(0);
    searchItemAction.setMenu("Search", 2);
    searchItemAction.setToolbarPosition(2);
    controller.addGuiAction(searchItemAction);
    
    //searchItemAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    // Action for refresh
    RefreshCommand refreshCommand = new RefreshCommand();
    GUIAction refreshItemAction = 
                    new GUIAction("Refresh", null, refreshCommand);
    refreshItemAction.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Refresh16.gif")));
    refreshItemAction.setToolTipText("Refresh...");
    refreshItemAction.setMenuItemPosition(1);
    refreshItemAction.setMenu("Search", 2);
    refreshItemAction.setToolbarPosition(3);
    refreshItemAction.setEnabled(false);  //default
    refreshItemAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME, 
                            true, GUIAction.EVENT_LOCAL);
//    refreshItemAction.setEnabledOnStateChange(
//                            StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME, 
//                            false, GUIAction.EVENT_LOCAL);
    refreshItemAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME, 
                            false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(refreshItemAction);
    
    //refreshItemAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    // Action for save query
    SaveQueryCommand saveCommand = new SaveQueryCommand(morpho);
    GUIAction saveQueryItemAction = 
                    new GUIAction("Save Search", null, saveCommand);
    saveQueryItemAction.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Save16.gif")));
    saveQueryItemAction.setToolTipText("Save search");
    saveQueryItemAction.setMenuItemPosition(2);
    saveQueryItemAction.setMenu("Search", 2);
    saveQueryItemAction.setToolbarPosition(4);
    saveQueryItemAction.setEnabled(false);  //default
    saveQueryItemAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME, 
                            true, GUIAction.EVENT_LOCAL);
//    saveQueryItemAction.setEnabledOnStateChange(
//                            StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME, 
//                            false, GUIAction.EVENT_LOCAL);
    saveQueryItemAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME, 
                            false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(saveQueryItemAction);
        
    // RevisedSearch action
    GUIAction reviseSearchItemAction = new GUIAction("Revise Search", null,
                                            new ReviseSearchCommand(morpho));
    reviseSearchItemAction.setSmallIcon(new ImageIcon(getClass().
           getResource("revisesearch16.gif")));
    reviseSearchItemAction.setToolTipText("Revise search");
    reviseSearchItemAction.setMenuItemPosition(3);
    reviseSearchItemAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    reviseSearchItemAction.setMenu("Search", 2);
    reviseSearchItemAction.setToolbarPosition(5);
    reviseSearchItemAction.setEnabled(false);  //default
    reviseSearchItemAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME, 
                            true, GUIAction.EVENT_LOCAL);
//    reviseSearchItemAction.setEnabledOnStateChange(
//                            StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME, 
//                            false, GUIAction.EVENT_LOCAL);
    reviseSearchItemAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME, 
                            false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(reviseSearchItemAction);

    // Load user saved the query to search menu too
    saveCommand.loadSavedQueries();
    
    // Open dialog box action
    GUIAction openDialogBoxAction = new GUIAction("Open...", null, 
                          new OpenDialogBoxCommand(morpho));
    openDialogBoxAction.setSmallIcon( new ImageIcon(getClass().
                  getResource("/toolbarButtonGraphics/general/Open16.gif")));
    openDialogBoxAction.setMenuItemPosition(2);
    openDialogBoxAction.setToolTipText("Open...");
    openDialogBoxAction.setMenu("File", 0);
    openDialogBoxAction.setToolbarPosition(1);
    controller.addGuiAction(openDialogBoxAction);
    
    // Open a data package action
//    GUIAction openPackageAction = new GUIAction("Open Package", null,
//                            new OpenPackageCommand(null));
//    openPackageAction.setMenuItemPosition(6);
//    openPackageAction.setToolTipText("Open a package...");
//    openPackageAction.setMenu("File", 0);
//    controller.addGuiAction(openPackageAction);
    
    // Create a OpenPreviousVersion action
    GUIAction openPreviousAction = new GUIAction("Open Previous Version",null,
                                new OpenPreviousVersionCommand(null, morpho));
    openPreviousAction.setMenuItemPosition(3);
    openPreviousAction.setToolTipText("Open a previous version...");
    openPreviousAction.setMenu("File", 0);
    openPreviousAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME_VERSIONS, 
                      true, GUIAction.EVENT_LOCAL);
    openPreviousAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_SELECTED_VERSIONS, 
                      true, GUIAction.EVENT_LOCAL);
    openPreviousAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME_NO_VERSIONS, 
                      false, GUIAction.EVENT_LOCAL);
    openPreviousAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_SELECTED_NO_VERSIONS, 
                      false, GUIAction.EVENT_LOCAL);
    openPreviousAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_NONSELECTED, 
                      false, GUIAction.EVENT_LOCAL);
    openPreviousAction.setEnabled(false);
    controller.addGuiAction(openPreviousAction);
    
    // Synchronize action
    GUIAction synchronizeAction = new GUIAction("Synchronize...", null,
                          new OpenSynchronizeDialogCommand());
    synchronizeAction.setMenuItemPosition(9);
    synchronizeAction.setToolTipText("Synchronize...");
    synchronizeAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    synchronizeAction.setMenu("File", 0);
    synchronizeAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME_UNSYNCHRONIZED, 
                      true, GUIAction.EVENT_LOCAL);
    synchronizeAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_SELECTED_UNSYNCHRONIZED, 
                      true, GUIAction.EVENT_LOCAL);
    synchronizeAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME_SYNCHRONIZED, 
                      false, GUIAction.EVENT_LOCAL);
    synchronizeAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_SELECTED_SYNCHRONIZED, 
                      false, GUIAction.EVENT_LOCAL);
    synchronizeAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_NONSELECTED, 
                      false, GUIAction.EVENT_LOCAL);
    synchronizeAction.setEnabled(false);
    controller.addGuiAction(synchronizeAction);
    
    // DeleteDialogAction
    GUIAction deleteDialogAction = new GUIAction("Delete...", null,
                                          new OpenDeleteDialogCommand());
    deleteDialogAction.setMenuItemPosition(11);
    deleteDialogAction.setToolTipText("Delete...");
    deleteDialogAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    deleteDialogAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME, 
                      true, GUIAction.EVENT_LOCAL);
    deleteDialogAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_SELECTED, 
                      true, GUIAction.EVENT_LOCAL);
    deleteDialogAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_NONSELECTED, 
                      false, GUIAction.EVENT_LOCAL);
    deleteDialogAction.setEnabled(false);
    deleteDialogAction.setMenu("File", 0);
    
    controller.addGuiAction(deleteDialogAction);
    
    // Export action
    GUIAction exportAction = new GUIAction("Export...", null, 
                            new ExportCommand(null, ExportCommand.REGULAR));
    exportAction.setMenuItemPosition(13);
    exportAction.setToolTipText("Export data package...");
    exportAction.setMenu("File", 0);
    exportAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME, 
                      true, GUIAction.EVENT_LOCAL);
    exportAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_SELECTED, 
                      true, GUIAction.EVENT_LOCAL);
    exportAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_NONSELECTED, 
                      false, GUIAction.EVENT_LOCAL);
    exportAction.setEnabled(false);
    controller.addGuiAction(exportAction);
    
    // Export to zip action
    GUIAction exportZipAction = new GUIAction("Export to Zip...", null,
                             new ExportCommand(null, ExportCommand.ZIP));
    exportZipAction.setMenuItemPosition(14);
    exportZipAction.setToolTipText("Export data package into zip file...");
    exportZipAction.setMenu("File", 0);
    exportZipAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME, 
                      true, GUIAction.EVENT_LOCAL);
    exportZipAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_SELECTED, 
                      true, GUIAction.EVENT_LOCAL);
    exportZipAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_NONSELECTED, 
                      false, GUIAction.EVENT_LOCAL);
    exportZipAction.setEnabled(false);
    controller.addGuiAction(exportZipAction);
    
  }

  /**
   * Implement the ConnectionListener interface so we know when to 
   * refresh queries.
   */
  public void usernameChanged(String newUsername)
  {
    Log.debug(20, "New username: " + newUsername);
    refresh();
  }

  /**
   * Implement the ConnectionListener interface so we know when to 
   * refresh queries.
   */
  public void connectionChanged(boolean connected)
  {
    Log.debug(20, "Connection changed: " + 
                    (new Boolean(connected)).toString());
    refresh();
  }

  /** 
   * This method is called to refresh a query when a change is made that should
   * be propogated to the query result screens.
   */ 
  public void refresh()
  {
    RefreshCommand refresh = new RefreshCommand();
    refresh.execute(null);
  }
  
  /**
   * This method implements from QueryRefreshInterface. It will be called when
   * user change the profile and will update the save queries in search menu
   * 
   * @param newMorpho new Morpho object after switch profile
   */
  public void updateSavedQueryMenuItems(Morpho newMorpho)
  {
    SaveQueryCommand.loadSavedQueries(newMorpho);
  }//updateSaveQuery
   
}
