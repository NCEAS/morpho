/**
 *  '$RCSfile: QueryPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-02 21:39:02 $'
 * '$Revision: 1.52 $'
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
import java.util.Hashtable;
import java.util.Properties;

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

  /** Tabbed panel that contains the data owned by the user */
  private ResultPanel ownerPanel = null;

  /**
   * Construct the query plugin.  Initialize our one tab for the 
   * plugin plus any menus and toolbars.
   */
  public QueryPlugin()
  {
    // Create the tabbed pane for the owner queries
    ownerPanel = new ResultPanel();
    ownerPanel.setName("My Data");

    // Create the menus and toolbar actions
    initializeActions();
  }

  /** 
   * The plugin must store a reference to the ClientFramework 
   * in order to be able to call the services available through 
   * the framework
   */
  public void initialize(ClientFramework cf)
  {
    this.framework = cf;
    this.config = framework.getConfiguration();
    loadConfigurationParameters();
    framework.setMainContentPane(ownerPanel);
    framework.addMenu("Search", new Integer(3), menuActions);
    framework.addToolbarActions(toolbarActions);
  }

  /**
   * This method is called on component initialization to generate a list
   * of the names of the menus, indexed by display position, that the component 
   * wants added to the framework.  If a menu already exists (from another 
   * component or the framework itself), the position will be determined by 
   * the earlier registration of the menu.
   */
  public Hashtable registerMenus() {
    Hashtable menuList = new Hashtable();
    menuList.put(new Integer(3), "Search");
    return menuList;
  }

  /**
   * The plugin must return the Actions that should be associated 
   * with a particular menu. They will be appended onto the bottom of the menu
   * in most cases.
   */
  public Action[] registerMenuActions(String menu) {
    Action actionList[] = null;
    if (menu.equals("Search")) {
      actionList = menuActions;
    }
    return actionList;
  }

  /**
   * The plugin must return the list of Actions to be associated with the
   * toolbar for the framework. 
   */
  public Action[] registerToolbarActions() {
    return toolbarActions;;
  }

  /**
   * This method is called by the framework when the plugin should 
   * register a UI tab pane that is to be incorporated into the main
   * user interface.
   */
  public Component registerTabPane()
  {
    return ownerPanel;
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
/*
  void TestSearch_actionPerformed(java.awt.event.ActionEvent event)
  {
    framework.debug(9, "Current user: " + framework.getUserName());
    getOwnerDocs(framework.getUserName());
  }

  public void getOwnerDocs(String name)
  {
    String searchtext = "<?xml version=\"1.0\"?>\n";
      searchtext = searchtext + "<pathquery version=\"1.0\">\n";
      searchtext = searchtext + "<owner>" + name + "</owner>\n";
      searchtext = searchtext + "<querygroup operator=\"UNION\">\n";
      searchtext = searchtext + "<queryterm casesensitive=\"true\" " +
                                "searchmode=\"contains\">\n";
      searchtext = searchtext + "<value>%</value>\n";
      searchtext = searchtext + "</queryterm></querygroup></pathquery>";
      squery_submitToDatabase_all(searchtext);
  }
*/
/*
  void ShowMenuItem_actionPerformed(java.awt.event.ActionEvent event)
  {
    int sel = table.getSelectedRow();
    if (sel > -1)
    {
      String filename = (String) table.getModel().getValueAt(sel, 0);
      File file = new File(filename);
      DocFrame df = new DocFrame(file);
        df.setVisible(true);
        df.writeInfo();
        // df.setDoctype("eml-dataset");
    }
  }

  void EditMenuItem_actionPerformed(java.awt.event.ActionEvent event)
  {
    int selectedRow = table.getSelectedRow();
    if (selectedRow > -1)
    {
      String filename = (String) table.getModel().getValueAt(selectedRow, 0);
      File temp = new File(filename);
      if (mde != null)
      {
        mde.openDocument(temp);
        ownerPanel.setSelectedIndex(0);
      }
      else
      {
        framework.debug(1, "mde is null in RSFrame class");
      }
    }
  }
*/
  public void squery_submitToDatabase(String queryXML)
  {
    Properties prop = new Properties();
    prop.put("action", "squery");
    prop.put("query", queryXML);
    prop.put("qformat", "xml");
    try
    {
      InputStream in = framework.getMetacatInputStream(prop, true);

      ExternalQuery rq = new ExternalQuery(in);
      RSFrame rs = new RSFrame("Results of Catalog Search");
      //rs.setEditor(mde);
      //rs.setTabbedPane(ownerPanel);
      rs.setVisible(true);
      rs.local = false;
      JTable ttt = rq.getTable();
      TableModel tm = ttt.getModel();
      rs.JTable1.setModel(tm);
      rs.JTable1.setColumnModel(ttt.getColumnModel());
      rs.relations = rq.getRelations();
      rs.pack();

      in.close();
    }
    catch(Exception w)
    {
      framework.debug(1, "Error in submitting structured query");
    }
  }

  // this method varies from squery_submitToDatabase only in setting 
  // the ExternalQuery class to build a table that shows all return columns
  public void squery_submitToDatabase_all(String queryXML)
  {
    Properties prop = new Properties();
    prop.put("action", "squery");
    prop.put("query", queryXML);

    prop.put("returndoc", "-//NCEAS//resource//EN");

    prop.put("qformat", "xml");
    try
    {
      InputStream in = framework.getMetacatInputStream(prop, true);

      ExternalQuery rq = new ExternalQuery(in, 0);  // the difference is here!
      RSFrame rs = new RSFrame("Results of Catalog Search");
      //rs.setEditor(mde);
      //rs.setTabbedPane(ownerPanel);
      rs.setVisible(true);
      rs.local = false;
      JTable ttt = rq.getTable();
      TableModel tm = ttt.getModel();
      rs.JTable1.setModel(tm);
      rs.JTable1.setColumnModel(ttt.getColumnModel());
      rs.relations = rq.getRelations();
      rs.pack();

      in.close();
    }
    catch(Exception w)
    {
      framework.debug(1, "Error in submitting structured query");
    }
  }

  public void simplequery_submitToDatabase(String query)
  {
    Properties prop = new Properties();
    prop.put("action", "query");
    prop.put("anyfield", query);
    prop.put("qformat", "xml");
    try
    {
      InputStream in = framework.getMetacatInputStream(prop, true);
      ExternalQuery rq = new ExternalQuery(in);
      RSFrame rs = new RSFrame("Results of Catalog Search");
      //rs.setEditor(mde);
      //rs.setTabbedPane(ownerPanel);
      rs.setVisible(true);
      rs.local = false;
      JTable ttt = rq.getTable();
      TableModel tm = ttt.getModel();
      rs.JTable1.setModel(tm);
      rs.relations = rq.getRelations();
      rs.pack();

      in.close();
    }
    catch(Exception w)
    {
      framework.debug(1, "Error in submitting simple query");
    }
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
