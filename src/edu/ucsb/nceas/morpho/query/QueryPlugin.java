/**
 *  '$RCSfile: QueryPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-15 18:32:29 $'
 * '$Revision: 1.75 $'
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

import edu.ucsb.nceas.morpho.framework.*;
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
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** Store our menus and toolbars */
  private Action[] menuActions = null;
  private Action[] toolbarActions = null;

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
    // Create the menus and toolbar actions, will register later
    initializeActions(); 
    // Add the menus and toolbars
    framework.addMenu("Search", new Integer(3), menuActions);
    framework.addToolbarActions(toolbarActions);

    // Create the tabbed pane for the owner queries
    createOwnerPanel();

    // Register Services
    try
    {
        framework.addService(QueryRefreshInterface.class, this);
        framework.debug(20, "Service added: QueryRefreshInterface.");
    } catch (ServiceExistsException see) {
        framework.debug(6, "Service registration failed: QueryRefreshInterface.");
        framework.debug(6, see.toString());
    }
    
        
    // Listen for changes to the connection status
    framework.addConnectionListener(this);
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() {
    // Set up the menus for the application
    menuActions = new Action[1];
  
    GUIAction searchItemAction = new GUIAction("Search...", null,
              new SearchCommand(null, framework));
    searchItemAction.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Search16.gif")));
    searchItemAction.setToolTipText("Search for data");
    searchItemAction.setMenuItemPosition(0);
    searchItemAction.setSeparatorPosition(ClientFramework.SEPARATOR_FOLLOWING);
    /*searchItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Search16.gif")));
    searchItemAction.putValue(Action.SHORT_DESCRIPTION, "Search for data");
    searchItemAction.putValue("menuPosition", new Integer(0));
    searchItemAction.putValue(Action.DEFAULT, 
                             ClientFramework.SEPARATOR_FOLLOWING);*/
    menuActions[0] = searchItemAction;
    
    GUIAction openDialogBoxAction = new GUIAction("Open", null, 
                new OpenDialogBoxCommand(framework));
    openDialogBoxAction.setSmallIcon( new ImageIcon(getClass().
                                      getResource("openButton.gif")));
    openDialogBoxAction.setToolTipText("Open...");
    
     // Set up the toolbar for the application
    toolbarActions = new Action[2];
    toolbarActions[0] = searchItemAction;
    toolbarActions[1] = openDialogBoxAction;
   
  }

  /**
   * Construct a query suitable for getting the owner documents
   */
  private String getOwnerQuery()
  {
    ConfigXML profile = framework.getProfile();
    StringBuffer searchtext = new StringBuffer();
    searchtext.append("<?xml version=\"1.0\"?>\n");
    searchtext.append("<pathquery version=\"1.0\">\n");
    String lastname = profile.get("lastname", 0);
    String firstname = profile.get("firstname", 0);
    searchtext.append("<querytitle>My Data (" + firstname + " " + lastname);
    searchtext.append(")</querytitle>\n");
    Vector returnDoctypeList = profile.get("returndoc");
    for (int i=0; i < returnDoctypeList.size(); i++) {
      searchtext.append("<returndoctype>");
      searchtext.append((String)returnDoctypeList.elementAt(i));
      searchtext.append("</returndoctype>\n");
    }
    Vector returnFieldList = profile.get("returnfield");
    for (int i=0; i < returnFieldList.size(); i++) {
      searchtext.append("<returnfield>");
      searchtext.append((String)returnFieldList.elementAt(i));
      searchtext.append("</returnfield>\n");
    }
    searchtext.append("<owner>" + framework.getUserName() + "</owner>\n");
    searchtext.append("<querygroup operator=\"UNION\">\n");
    searchtext.append("<queryterm casesensitive=\"true\" ");
    searchtext.append("searchmode=\"contains\">\n");
    searchtext.append("<value>%</value>\n");
    searchtext.append("</queryterm></querygroup></pathquery>");

    return searchtext.toString();
  }

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
  }

 
  /**
   * Implement the ConnectionListener interface so we know when to 
   * refresh queries.
   */
  public void usernameChanged(String newUsername)
  {
    framework.debug(20, "New username: " + newUsername);
    refreshOwnerPanel();
  }

  /**
   * Implement the ConnectionListener interface so we know when to 
   * refresh queries.
   */
  public void connectionChanged(boolean connected)
  {
    framework.debug(20, "Connection changed: " + 
                    (new Boolean(connected)).toString());
    refreshOwnerPanel();
  }

  /**
   * Create the owner panel with the appropriate query
   */
  private void createOwnerPanel()
  {
    // Create the tabbed pane for the owner queries
    ownerQuery = new Query(getOwnerQuery(), framework);
    ResultSet results = ownerQuery.execute();
    ownerPanel = new ResultPanel(results, true, false, null);

    // Add the content pane, menus, and toolbars
    framework.setMainContentPane(ownerPanel);

    // Reload any saved queries in the search menu
    ownerPanel.loadSavedQueries();
  }

  /**
   * Refresh the owner panel after the username has changed
   */
  private void refreshOwnerPanel()
  {
    // Create the tabbed pane for the owner queries
    ownerQuery = new Query(getOwnerQuery(), framework);
    ResultSet results = ownerQuery.execute();
    ownerPanel.setResults(results);

    // Reload any saved queries in the search menu
    ownerPanel.loadSavedQueries();
  }

  /** 
   * This method is called to refresh a query when a change is made that should
   * be propogated to the query result screens.
   */
  public void refresh()
  {
      refreshOwnerPanel();
  }
  
private void doQuery(final ResultFrame rsf, final Query query) {
  
  final SwingWorker worker = new SwingWorker() {
        ResultSet frs;
        public Object construct() {
          frs = query.execute();
        
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          rsf.setTitle(frs.getQuery().getQueryTitle());
          rsf.setName(frs.getQuery().getQueryTitle());
          
          rsf.addResultPanel(frs);
        }
    };
    worker.start();  //required for SwingWorker 3
}
  
  
}
