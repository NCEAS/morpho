/**
 *  '$RCSfile: QueryPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-03 22:21:03 $'
 * '$Revision: 1.55 $'
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class QueryPlugin implements PluginInterface
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

    // Create the tabbed pane for the owner queries
    ownerQuery = new Query(getOwnerQuery(), framework);
    ResultSet results = ownerQuery.execute();
    ownerPanel = new ResultPanel(results);
    ownerPanel.setName("My Data");

    // Add the content pane, menus, and toolbars
    framework.setMainContentPane(ownerPanel);
    framework.addMenu("Search", new Integer(3), menuActions);
    framework.addToolbarActions(toolbarActions);
  }

  /**
   * This method is called by the framework when the plugin should 
   * register any services that it handles.  The plugin should then
   * call the framework's 'addService' method for each service it can
   * handle.
   */
  public void registerServices()
  {
  }
    
  /**
   * This is the general dispatch method that is called by the framework
   * whenever a plugin is expected to handle a service request.  The
   * details of the request and data for the request are contained in
   * the ServiceRequest object.
   *
   * @param request request details and data
   */
  public void handleServiceRequest(ServiceRequest request)
              throws ServiceNotHandledException
  {
  } 
  
  /**
   * This method is called by a service provider that is handling 
   *  a service request that originated with the plugin.  Data
   * from the ServiceRequest is handed back to the source plugin in
   * the ServiceResponse object.
   *
   * @param response response details and data
   */
  public void handleServiceResponse(ServiceResponse response)
  {
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() {
    // Set up the menus for the application
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
  }

  /**
   * Construct a query suitable for getting the owner documents
   */
  private String getOwnerQuery()
  {
    StringBuffer searchtext = new StringBuffer();
    searchtext.append("<?xml version=\"1.0\"?>\n");
    searchtext.append("<pathquery version=\"1.0\">\n");
    searchtext.append("<querytitle>My Data</querytitle>\n");
    Vector returnDoctypeList = config.get("returndoc");
    for (int i=0; i < returnDoctypeList.size(); i++) {
      searchtext.append("<returndoctype>");
      searchtext.append((String)returnDoctypeList.get(i));
      searchtext.append("</returndoctype>\n");
    }
    Vector returnFieldList = config.get("returnfield");
    for (int i=0; i < returnFieldList.size(); i++) {
      searchtext.append("<returnfield>");
      searchtext.append((String)returnFieldList.get(i));
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
    //MetaCatServletURL = config.get("MetaCatServletURL", 0);
    framework.debug(9, "No config params to load.");
  }
}
