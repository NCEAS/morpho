/**
 *  '$RCSfile: DataPackagePlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-26 20:18:20 $'
 * '$Revision: 1.34 $'
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
import edu.ucsb.nceas.morpho.framework.ButterflyFlapCoordinator;
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

  /** Constant int for data menu position */
  public static final int DATAMENUPOSITION = 3;
  
  /** Constant int for edit menu position */
  public static final int EDITMENUPOSITION = 1;
  /**
   * Construct the plugin.  Initialize our menus and toolbars.
   */
  public DataPackagePlugin()
  {
    
  }
  
  /**
   * Construct of the puglin which will be used in datapackage itself
   *
   * @param morpho the morpho for this application
   */
  public DataPackagePlugin(Morpho morpho)
  {
    this.morpho = morpho;
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
    // Create the menus and toolbar actions, will register later
    initializeActions();
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
    UIController controller = UIController.getInstance();
    
    // For edit menu
    GUIAction copy = new GUIAction("Copy", null, null);
    copy.setToolTipText("Copy value in data table cells");
    copy.setMenuItemPosition(0);
    copy.setMenu("Edit", EDITMENUPOSITION);
    controller.addGuiAction(copy);
    
    GUIAction cut = new GUIAction("Cut", null, null);
    cut.setToolTipText("Cut value in data table cells");
    cut.setMenuItemPosition(1);
    cut.setMenu("Edit", EDITMENUPOSITION);
    controller.addGuiAction(cut);
    
    GUIAction paste = new GUIAction("paste", null, null);
    paste.setToolTipText("paste value in data table cells");
    paste.setMenuItemPosition(2);
    paste.setMenu("Edit", EDITMENUPOSITION);
    controller.addGuiAction(paste);
    
    copy.setEnabled(false);
    cut.setEnabled(false);
    paste.setEnabled(false);
    
    // For data menu
    int i = 0; // postition for menu item in data menu
    
    GUIAction addDocumentation = new GUIAction("Add Documentation...", null, 
                                          new AddDocumentationCommand());
    addDocumentation.setToolTipText("Add a XML documentation");
    addDocumentation.setMenuItemPosition(i);
    addDocumentation.setMenu("Data", DATAMENUPOSITION);
    controller.addGuiAction(addDocumentation);
    
    i = i+1;
    GUIAction createNewDatatable = new GUIAction("Create New Datatable...", null,
                                                      new ImportDataCommand());
    createNewDatatable.setToolTipText("Add a new table");
    createNewDatatable.setMenuItemPosition(i);
    createNewDatatable.setMenu("Data", DATAMENUPOSITION);
    createNewDatatable.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    controller.addGuiAction(createNewDatatable);
    
    i= i+2; // separator will take a position so add 2
    GUIAction sortBySelectedColumn = 
                        new GUIAction("Sort by Selected Column", null, null);
    sortBySelectedColumn.setToolTipText("Sort table by selected column");
    sortBySelectedColumn.setMenuItemPosition(i);
    sortBySelectedColumn.setMenu("Data", DATAMENUPOSITION);
    sortBySelectedColumn.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    controller.addGuiAction(sortBySelectedColumn);
    
    i = i+2;
    GUIAction insertRowAfter = 
            new GUIAction("Insert Row After Selected Row", null, null);
    insertRowAfter.setToolTipText("Insert a row after selected row");
    insertRowAfter.setMenuItemPosition(i);
    insertRowAfter.setMenu("Data", DATAMENUPOSITION);
    controller.addGuiAction(insertRowAfter);
    
    i = i+1;
    GUIAction insertRowBefore = 
                  new GUIAction("Insert Row Before Selected Row", null, null);
    insertRowBefore.setToolTipText("Insert a row before selected row");
    insertRowBefore.setMenuItemPosition(i);
    insertRowBefore.setMenu("Data", DATAMENUPOSITION);
    controller.addGuiAction(insertRowBefore);
    
    i = i+1;
    GUIAction deleteRow = new GUIAction("Delete Selected Row", null, null);
    deleteRow.setToolTipText("Delete a selected row");
    deleteRow.setMenuItemPosition(i);
    deleteRow.setMenu("Data", DATAMENUPOSITION);
    deleteRow.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    controller.addGuiAction(deleteRow);
    
    i = i+2;
    GUIAction insertColumnAfter = new 
            GUIAction("Insert Column After Selected Column", null, null);
    insertColumnAfter.setToolTipText("Insert a column after selected column");
    insertColumnAfter.setMenuItemPosition(i);
    insertColumnAfter.setMenu("Data", DATAMENUPOSITION);
    controller.addGuiAction(insertColumnAfter);
       
    i = i+1;
    GUIAction insertColumnBefore = 
        new GUIAction("Insert Column Before Selected Column", null, null);
    insertColumnBefore.setToolTipText("Insert a column before selected column");
    insertColumnBefore.setMenuItemPosition(i);
    insertColumnBefore.setMenu("Data", DATAMENUPOSITION);
    controller.addGuiAction(insertColumnBefore);
    
    i = i+1;
    GUIAction deleteColumn = new 
                          GUIAction("Delete Selected Column", null, null);
    deleteColumn.setToolTipText("Delete a selected column");
    deleteColumn.setMenuItemPosition(i);
    deleteColumn.setMenu("Data", DATAMENUPOSITION);
    deleteColumn.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    controller.addGuiAction(deleteColumn);
    
    i = i+2;
    GUIAction editColumnMetadata = 
                          new GUIAction("Edit Column Metadata", null, null);
    editColumnMetadata.setToolTipText("Edit selected column metadata");
    editColumnMetadata.setMenuItemPosition(i);
    editColumnMetadata.setMenu("Data", DATAMENUPOSITION);
    //editColumnMetadata.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    controller.addGuiAction(editColumnMetadata);
    
    addDocumentation.setEnabled(false);
    createNewDatatable.setEnabled(false);
    sortBySelectedColumn.setEnabled(false);
    insertRowAfter.setEnabled(false);
    insertRowBefore.setEnabled(false);
    deleteRow.setEnabled(false);
    insertColumnBefore.setEnabled(false);
    insertColumnAfter.setEnabled(false);
    deleteColumn.setEnabled(false);
    editColumnMetadata.setEnabled(false);
    
    // create new data package menu in file menu
    GUIAction createNewDataPackage = new GUIAction("New Datapackage...", null, 
                                  new CreateNewDataPackageCommand(morpho));
    createNewDataPackage.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/New16.gif")));
    createNewDataPackage.setToolTipText("Create a new data package");
    createNewDataPackage.setMenuItemPosition(1);
    createNewDataPackage.setMenu("File", 0);
    createNewDataPackage.setToolbarPosition(5);
    controller.addGuiAction(createNewDataPackage);
    
  }

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
    //we dont' need any!
  }

  public void openDataPackage(String location, String identifier, 
                       Vector relations, ButterflyFlapCoordinator coordinator)
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
    
    // Stop butterfly flapping for old window.
    packageWindow.setBusy(true);
    if (coordinator != null)
    {
      coordinator.stopFlap();
    }
    
    DataViewContainerPanel dvcp = new DataViewContainerPanel(dp, gui);
    dvcp.setFramework(morpho);

    dvcp.setEntityItems(gui.getEntityitems());
    dvcp.setListValueHash(gui.listValueHash);
    dvcp.init();

    dvcp.setSize(packageWindow.getDefaultContentAreaSize());
    dvcp.setPreferredSize(packageWindow.getDefaultContentAreaSize());
//    dvcp.setVisible(true);
    packageWindow.setMainContentPane(dvcp);
    packageWindow.setBusy(false);
  }
  
  /**
   * Uploads the package to metacat.  The location is assumed to be 
   * DataPackageInterface.LOCAL
   * @param docid the id of the package to upload
   */
  public void upload(String docid, boolean updateIds) 
              throws MetacatUploadException
  {
    DataPackage dp = new DataPackage(DataPackageInterface.LOCAL, docid, null, morpho);
    dp.upload(updateIds);
  }
  
  /**
   * Downloads the package from metacat.  The location is assumed to be
   * DataPackageInterface.METACAT 
   * @param docid the id of the package to download
   */
  public void download(String docid)
  {
    DataPackage dp = new DataPackage(DataPackageInterface.METACAT, docid, null, morpho);
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
  
   /**
   * returns the next local id from the config file
   * returns null if configXML was unable to increment the id number
   *
   * @param morpho the morpho file
   */
  public String getNextId(Morpho morpho)
  {
    String identifier = null;
    AccessionNumber accession = new AccessionNumber(morpho);
    identifier = accession.getNextId();
    return identifier;
  }
}
