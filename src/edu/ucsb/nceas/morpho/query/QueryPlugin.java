/**
 *  '$RCSfile: QueryPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-11 00:38:26 $'
 * '$Revision: 1.86 $'
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

  /** Store our menus and toolbars */
  private Action[] menuActions = null;
  private Action[] toolbarActions = null;
  private Action[] fileMenuActions = null;

  /** Query used to find data owned by the user */
  private Query ownerQuery = null;

  /** Tabbed panel that displays the data owned by the user */
  private ResultPanel ownerPanel = null;
  
  /** The number of actions in search menu, exclude saved queries */
  public static final int NUMBEROFACTIONINSEARCH = 4;

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
    // Add the menus and toolbars
    // Add open action in file menu
    UIController controller = UIController.getInstance();
    controller.addMenu("File", new Integer(1),fileMenuActions);
    // Add search menu
    controller.addMenu("Search", new Integer(3), menuActions);
    controller.addToolbarActions(toolbarActions);

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
  private void initializeActions() {
    // Set up the search menus for the application
    menuActions = new Action[NUMBEROFACTIONINSEARCH];
   
    // Action for search
    GUIAction searchItemAction = new GUIAction("Search...", null,
                                        new SearchCommand(null, morpho));
    searchItemAction.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Search16.gif")));
    searchItemAction.setToolTipText("Search for data");
    searchItemAction.setMenuItemPosition(0);
    
    //searchItemAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    // Action for refresh
    RefreshCommand refreshCommand = new RefreshCommand(null);
    GUIAction refreshItemAction = 
                    new GUIAction("Refresh", null, refreshCommand);
    refreshItemAction.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Refresh16.gif")));
    refreshItemAction.setToolTipText("Refresh...");
    refreshItemAction.setMenuItemPosition(1);
    
    //refreshItemAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    // Action for save query
    SaveQueryCommand saveCommand = new SaveQueryCommand(morpho);
    GUIAction saveQueryItemAction = 
                    new GUIAction("Save search", null, saveCommand);
    saveQueryItemAction.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Save16.gif")));
    saveQueryItemAction.setToolTipText("Save search");
    saveQueryItemAction.setMenuItemPosition(2);
        
    // RevisedSearch action
    GUIAction reviseSearchItemAction = new GUIAction("Revise search", null,
                                            new ReviseSearchCommand(morpho));
    reviseSearchItemAction.setSmallIcon(new ImageIcon(getClass().
           getResource("revisesearch16.gif")));
    reviseSearchItemAction.setToolTipText("Revise search");
    reviseSearchItemAction.setMenuItemPosition(3);
    reviseSearchItemAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    // Put actions into array which will be added into search menu
    menuActions[0] = searchItemAction;
    menuActions[1] = refreshItemAction;
    menuActions[2] = saveQueryItemAction;
    menuActions[3] = reviseSearchItemAction;
    
    // Load user saved the query to search menu too
    saveCommand.loadSavedQueries();
    
    // Open dialog box action
    GUIAction openDialogBoxAction = new GUIAction("Open", null, 
                          new OpenDialogBoxCommand(morpho));
    openDialogBoxAction.setSmallIcon( new ImageIcon(getClass().
                  getResource("/toolbarButtonGraphics/general/Open16.gif")));
    openDialogBoxAction.setMenuItemPosition(0);
    openDialogBoxAction.setToolTipText("Open...");
        
    // Synchronize action
    GUIAction synchronizeAction = new GUIAction("Synchronize...", null,
                          new OpenSynchronizeDialogCommand());
    synchronizeAction.setMenuItemPosition(6);
    synchronizeAction.setToolTipText("Synchronize...");
    synchronizeAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    
    // DeleteDialogAction
    GUIAction deleteDialogAction = new GUIAction("Delete...", null,
                                          new OpenDeleteDialogCommand());
    deleteDialogAction.setMenuItemPosition(8);
    deleteDialogAction.setToolTipText("Delete...");
    deleteDialogAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    
    // Export action
    GUIAction exportAction = new GUIAction("Export...", null, 
                            new ExportCommand(null, ExportCommand.REGULAR));
    exportAction.setMenuItemPosition(10);
    exportAction.setToolTipText("Export data package...");
    
    // Export to zip action
    GUIAction exportZipAction = new GUIAction("Export to Zip...", null,
                             new ExportCommand(null, ExportCommand.ZIP));
    exportZipAction.setMenuItemPosition(11);
    exportZipAction.setToolTipText("Export data package into zip file...");
    exportZipAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    
     // Set up the toolbar for the application
    toolbarActions = new Action[5];
    toolbarActions[0] = openDialogBoxAction;
    toolbarActions[1] = searchItemAction;
    toolbarActions[2] = refreshItemAction;
    toolbarActions[3] = saveQueryItemAction;
    toolbarActions[4] = reviseSearchItemAction;
    
    //Set up action for file menu
    fileMenuActions = new Action[5];
    fileMenuActions[0] = openDialogBoxAction;
    fileMenuActions[1] = synchronizeAction;
    fileMenuActions[2] = deleteDialogAction;
    fileMenuActions[3] = exportAction;
    fileMenuActions[4] = exportZipAction;
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
    RefreshCommand refresh = new RefreshCommand(null);
    refresh.execute();
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
