/**
 *  '$RCSfile: DataPackagePlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-27 17:55:14 $'
 * '$Revision: 1.23 $'
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
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.GUIAction;

import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;


/**
 * Class that implements the plugin for package editing
 */
public class DataPackagePlugin 
       implements PluginInterface, ServiceProvider, DataPackageInterface
{
  /** A reference to the container framework */
  private Morpho morpho = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** Store our menus and toolbars */
  private Action[] menuActions = null;
  private Action[] dataMenuActions = null;
  private Action[] toolbarActions = null;

  /**
   * Construct the plugin.  Initialize our menus and toolbars.
   */
  public DataPackagePlugin()
  {
    // Create the menus and toolbar actions, will register later
    initializeActions();
  }

  /** 
   * The plugin must store a reference to the Morpho application 
   * in order to be able to call the services available through 
   * the framework.  This is also the time to register menus
   * and toolbars with the framework.
   */
  public void initialize(Morpho morpho)
  {
    this.morpho = morpho;
    this.config = morpho.getConfiguration();
    loadConfigurationParameters();

    // Add menus, and toolbars
    UIController controller = UIController.getInstance();
    controller.addMenu("File", new Integer(1), menuActions);
    controller.addMenu("Data", new Integer(4), dataMenuActions);
    controller.addToolbarActions(toolbarActions);

    // Register Services
    try 
    {
      ServiceController services = ServiceController.getInstance();
      services.addService(DataPackageInterface.class, this);
      Log.debug(20, "Service added: DataPackageInterface.");
    } 
    catch (ServiceExistsException see) 
    {
      Log.debug(6, "Service registration failed: DataPackageInterface.");
      Log.debug(6, see.toString());
    }

    Log.debug(20, "Init DataPackage Plugin"); 
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() 
  {
    dataMenuActions = new Action[9];
    GUIAction createNewDatatable = new GUIAction("Create New Datatable...", null, null);
    GUIAction sortBySelectedColumn = new GUIAction("Sort by Selected Column", null, null);
    GUIAction insertRowAfter = new GUIAction("insert Row After Selected Row", null, null);
    GUIAction insertRowBefore = new GUIAction("insert Row Before Selected Row", null, null);
    GUIAction deleteRow = new GUIAction("Delete Selected Row", null, null);
    GUIAction insertColumnBefore = new GUIAction("insert Column Before Selected Column", null, null);
    GUIAction insertColumnAfter = new GUIAction("insert Column After Selected Column", null, null);
    GUIAction deleteColumn = new GUIAction("Delete Selected Column", null, null);
    GUIAction editColumnMetadata = new GUIAction("Edit Column Metadata", null, null);
    
    createNewDatatable.setEnabled(false);
    sortBySelectedColumn.setEnabled(false);
    insertRowAfter.setEnabled(false);
    insertRowBefore.setEnabled(false);
    deleteRow.setEnabled(false);
    insertColumnBefore.setEnabled(false);
    insertColumnAfter.setEnabled(false);
    deleteColumn.setEnabled(false);
    editColumnMetadata.setEnabled(false);
    
    dataMenuActions[0] = createNewDatatable;
    dataMenuActions[1] = sortBySelectedColumn;
    dataMenuActions[2] = insertRowAfter;
    dataMenuActions[3] = insertRowBefore;
    dataMenuActions[4] = deleteRow;
    dataMenuActions[5] = insertColumnBefore;
    dataMenuActions[6] = insertColumnAfter;
    dataMenuActions[7] = deleteColumn;
    dataMenuActions[8] = editColumnMetadata;
    // Set up the menus for the application
    menuActions = new Action[1];
    Action newItemAction = new AbstractAction("New Data Package") 
    {
      public void actionPerformed(ActionEvent e) 
      {
        Log.debug(20, "Action fired: New Data Package");
        final PackageWizardShell pws = new PackageWizardShell(morpho);
        pws.setName("Package Wizard");
        //MBJ framework.addWindow(pws);
        pws.addWindowListener(new WindowAdapter()
        {
          public void windowClosed(WindowEvent e)
          {
            //MBJ framework.removeWindow(pws);
          }
          
          public void windowClosing(WindowEvent e)
          {
            //MBJ framework.removeWindow(pws);
          }
        });
        pws.show();
      }
    };
    newItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/New16.gif")));
    newItemAction.putValue(Action.SHORT_DESCRIPTION, "New data package");
    newItemAction.putValue("menuPosition", new Integer(0));
    menuActions[0] = newItemAction;

    // Set up the toolbar for the application
    toolbarActions = new Action[1];
    toolbarActions[0] = newItemAction;
  }

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
    //we dont' need any!
  }

  public void openDataPackage(String location, String identifier, 
                              Vector relations)
  {
    Log.debug(11, "DataPackage: Got service request to open: " + 
                    identifier + " from " + location + ".");
    DataPackage dp = new DataPackage(location, identifier, 
                                     relations, morpho);
    //Log.debug(11, "location: " + location + " identifier: " + identifier +
    //                " relations: " + relations.toString());
    final DataPackageGUI gui = new DataPackageGUI(morpho, dp);
    

    MorphoFrame packageWindow = UIController.getInstance().addWindow(
                "Data Package");
    packageWindow.setVisible(true);
    
    DataViewContainerPanel dvcp = new DataViewContainerPanel(dp, gui);
    dvcp.setFramework(morpho);

    dvcp.setEntityItems(gui.entityitems);
    dvcp.setListValueHash(gui.listValueHash);
    dvcp.init();

    dvcp.setSize(packageWindow.getDefaultContentAreaSize());
    dvcp.setPreferredSize(packageWindow.getDefaultContentAreaSize());
//    dvcp.setVisible(true);
    packageWindow.setMainContentPane(dvcp);

  }
  
  /**
   * Uploads the package to metacat.  The location is assumed to be 
   * DataPackage.LOCAL
   * @param docid the id of the package to upload
   */
  public void upload(String docid, boolean updateIds) 
              throws MetacatUploadException
  {
    DataPackage dp = new DataPackage(DataPackage.LOCAL, docid, null, morpho);
    dp.upload(updateIds);
  }
  
  /**
   * Downloads the package from metacat.  The location is assumed to be
   * DataPackage.METACAT 
   * @param docid the id of the package to download
   */
  public void download(String docid)
  {
    DataPackage dp = new DataPackage(DataPackage.METACAT, docid, null, morpho);
    dp.download();
  }
  
  /**
   * Deletes the package.
   * @param docid the id of the package to download
   */
  public void delete(String docid, String location)
  {
    DataPackage dp = new DataPackage(location, docid, null, morpho);
    dp.delete(location);
  }
  
  /**
   * Exports the package.
   * @param docid the id of the package to export
   * @param path the directory to which the package should be exported.
   * @param location the location where the package is now: LOCAL, METACAT or 
   * BOTH
   */
  public void export(String docid, String path, String location)
  {
    DataPackage dp = new DataPackage(location, docid, null, morpho);
    dp.export(path);
  }
  
  /**
   * Exports the package into a zip file
   * @param docid the id of the package to export
   * @param path the directory to which the package should be exported.
   * @param location the location where the package is now: LOCAL, METACAT or 
   * BOTH
   */
  public void exportToZip(String docid, String path, String location)
  {
    DataPackage dp = new DataPackage(location, docid, null, morpho);
    try
    {
      dp.exportToZip(path);
    }
    catch(Exception e)
    {
      System.out.println("Error in DataPackage.exportToZip(): " + e.getMessage());
      e.printStackTrace();
    }
  }
  
   /**
   * This method will create a dialog for open previouse version of a 
   * datapackage
   * @param title the title of the dialog, docid will be set as tile
   * @param numOfVersion the total number of versions in this docid
   * @param morpho the morpho file
   * @param local the package is local or not
   */
  public void createOpenPreviousVersionDialog(String title, int numOfVersion,
                                              Morpho morpho, boolean local)
  {
    // Create a new open previous version dialog
    OpenPreviousDialog open = new OpenPreviousDialog(title, numOfVersion, 
                                                      morpho, local);
    // Set open dialog show
    open.setVisible(true);
  }
}
