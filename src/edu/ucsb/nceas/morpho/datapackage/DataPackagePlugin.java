/**
 *  '$RCSfile: DataPackagePlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-02-24 20:47:38 $'
 * '$Revision: 1.76 $'
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
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.framework.ButterflyFlapCoordinator;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.UISettings;

import java.util.Vector;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;


/**
 * Class that implements the plugin for package editing
 */
public class DataPackagePlugin
       implements PluginInterface, ServiceProvider, DataPackageInterface
{
  /** A reference to the container framework */
  private Morpho morpho = null;

  /** Constant int for data menu position */
  public static final int DATAMENUPOSITION = 3;

  /** Constant int for edit menu position */
  public static final int EDITMENUPOSITION = 1;

  /** String for accelerator key */
  public static final String COPYKEY  = "control c";
  public static final String CUTKEY   = "control x";
  public static final String PASTEKEY = "control v";
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
   * The plugin must store a reference to the Morpho application in order to be
   * able to call the services available through the framework. This is also the
   * time to register menus and toolbars with the framework.
   *
   * @param morpho Morpho
   */
  public void initialize(Morpho morpho)
  {
    this.morpho = morpho;
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

    // Save dialog box action
    GUIAction saveAction = new GUIAction("Save...",
                                              UISettings.SAVE_ICON,
                                              new SavePackageCommand());
    saveAction.setMenuItemPosition(4);
    saveAction.setToolTipText("Save...");
    saveAction.setMenu("File", 0);
    saveAction.setToolbarPosition(1);
    saveAction.setEnabled(false);
    saveAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    saveAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(saveAction);

    GUIAction revertAction = new GUIAction("Revert",
                                              null,
                                              new RevertCommand());
    revertAction.setMenuItemPosition(5);
    revertAction.setToolTipText("Revert");
    revertAction.setMenu("File", 0);
    revertAction.setEnabled(false);
/*
    revertAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    revertAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
*/
    controller.addGuiAction(revertAction);



    // For edit menu
    GUIAction copy = new GUIAction("Copy", null, new TableCopyCommand());
    copy.setToolTipText("Copy value in data table cells");
    copy.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Copy16.gif")));
    copy.setAcceleratorKeyString(COPYKEY);
    copy.setMenuItemPosition(0);
    copy.setMenu("Edit", EDITMENUPOSITION);
    copy.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    copy.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    copy.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    copy.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);

    controller.addGuiAction(copy);

    GUIAction cut = new GUIAction("Cut", null, new TableCutCommand());
    cut.setToolTipText("Cut value in data table cells");
    cut.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Cut16.gif")));
    cut.setAcceleratorKeyString(CUTKEY);
    cut.setMenuItemPosition(1);
    cut.setMenu("Edit", EDITMENUPOSITION);
    cut.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    cut.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    cut.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    cut.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(cut);

    GUIAction paste = new GUIAction("Paste", null, new TablePasteCommand());
    paste.setToolTipText("Paste value in data table cells");
    paste.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Paste16.gif")));
    paste.setAcceleratorKeyString(PASTEKEY);
    paste.setMenuItemPosition(2);
    paste.setMenu("Edit", EDITMENUPOSITION);
   /*
    paste.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
   */
    paste.setEnabledOnStateChange(
                      StateChangeEvent.CLIPBOARD_HAS_DATA_TO_PASTE,
                      true, GUIAction.EVENT_LOCAL);
    paste.setEnabledOnStateChange(
                      StateChangeEvent.CLIPBOARD_HAS_NO_DATA_TO_PASTE,
                      false, GUIAction.EVENT_LOCAL);
    paste.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    paste.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    paste.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
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
    addDocumentation.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                            true, GUIAction.EVENT_LOCAL);
    addDocumentation.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addDocumentation);

        i = i+1;
    GUIAction createNewDatatable = new GUIAction("Create/Import New Datatable...", null,
                                                      new ImportDataCommand());
    createNewDatatable.setToolTipText("Add a new table");
    createNewDatatable.setMenuItemPosition(i);
    createNewDatatable.setMenu("Data", DATAMENUPOSITION);
    createNewDatatable.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                            true, GUIAction.EVENT_LOCAL);
    createNewDatatable.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(createNewDatatable);


    i = i+1;
    GUIAction deleteDatatable = new GUIAction("Delete Current Datatable", null,
                                                      new DeleteTableCommand());
    deleteDatatable.setToolTipText("Remove the currently displayed table");
    deleteDatatable.setMenuItemPosition(i);
    deleteDatatable.setMenu("Data", DATAMENUPOSITION);
    deleteDatatable.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    deleteDatatable.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    deleteDatatable.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    deleteDatatable.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    deleteDatatable.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(deleteDatatable);

    i= i+2; // separator will take a position so add 2
    GUIAction addTemporalCovColumn = new GUIAction("Add Temporal Information..",
                                           null, new AddTemporalCovCommand());
    addTemporalCovColumn.setToolTipText("Add temporal coverage information");
    addTemporalCovColumn.setMenuItemPosition(i);
    addTemporalCovColumn.setMenu("Data", DATAMENUPOSITION);
    addTemporalCovColumn.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    addTemporalCovColumn.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    addTemporalCovColumn.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    addTemporalCovColumn.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    addTemporalCovColumn.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addTemporalCovColumn);


    i= i+2; // separator will take a position so add 2
    GUIAction sortBySelectedColumn = new GUIAction("Sort by Selected Column",
                                           null, new SortDataTableCommand());
    sortBySelectedColumn.setToolTipText("Sort table by selected column");
    sortBySelectedColumn.setMenuItemPosition(i);
    sortBySelectedColumn.setMenu("Data", DATAMENUPOSITION);
    sortBySelectedColumn.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    sortBySelectedColumn.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    sortBySelectedColumn.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    sortBySelectedColumn.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    sortBySelectedColumn.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(sortBySelectedColumn);

    i = i+2;
    GUIAction insertRowAfter = new GUIAction("Insert Row After Selection",
                            null, new InsertRowCommand(InsertRowCommand.AFTER));
    insertRowAfter.setToolTipText("Insert a row after selected row");
    insertRowAfter.setMenuItemPosition(i);
    insertRowAfter.setMenu("Data", DATAMENUPOSITION);
    insertRowAfter.setAcceleratorKeyString("control I");
    insertRowAfter.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    insertRowAfter.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    insertRowAfter.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    insertRowAfter.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(insertRowAfter);

    i = i+1;
    GUIAction insertRowBefore = new GUIAction("Insert Row Before Selection",
                           null, new InsertRowCommand(InsertRowCommand.BEFORE));
    insertRowBefore.setToolTipText("Insert a row before selected row");
    insertRowBefore.setMenuItemPosition(i);
    insertRowBefore.setMenu("Data", DATAMENUPOSITION);
    insertRowBefore.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    insertRowBefore.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    insertRowBefore.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    insertRowBefore.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(insertRowBefore);

    i = i+1;
    GUIAction deleteRow = new GUIAction("Delete Selected Row", null,
                              new DeleteRowCommand());
    deleteRow.setToolTipText("Delete a selected row");
    deleteRow.setMenuItemPosition(i);
    deleteRow.setMenu("Data", DATAMENUPOSITION);
    deleteRow.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    deleteRow.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    deleteRow.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    deleteRow.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    deleteRow.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(deleteRow);

    i = i+2;
    GUIAction insertColumnAfter = new GUIAction("Insert Column After Selection",
                    null, new InsertColumnCommand(InsertColumnCommand.AFTER));
    insertColumnAfter.setToolTipText("Insert a column after selected column");
    insertColumnAfter.setMenuItemPosition(i);
    insertColumnAfter.setMenu("Data", DATAMENUPOSITION);
    insertColumnAfter.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    insertColumnAfter.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    insertColumnAfter.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    insertColumnAfter.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(insertColumnAfter);

    i = i+1;
    GUIAction insertColumnBefore =
                  new GUIAction("Insert Column Before Selection", null,
                           new InsertColumnCommand(InsertColumnCommand.BEFORE));
    insertColumnBefore.setToolTipText("Insert a column before selected column");
    insertColumnBefore.setMenuItemPosition(i);
    insertColumnBefore.setMenu("Data", DATAMENUPOSITION);
    insertColumnBefore.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    insertColumnBefore.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    insertColumnBefore.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    insertColumnBefore.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(insertColumnBefore);

    i = i+1;
    GUIAction deleteColumn = new GUIAction("Delete Selected Column", null,
                                  new DeleteColumnCommand());
    deleteColumn.setToolTipText("Delete a selected column");
    deleteColumn.setMenuItemPosition(i);
    deleteColumn.setMenu("Data", DATAMENUPOSITION);
    deleteColumn.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    deleteColumn.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    deleteColumn.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    deleteColumn.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    deleteColumn.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(deleteColumn);

    i = i+2;
    GUIAction editColumnMetadata = new GUIAction("Edit Column Metadata", null,
                                      new EditColumnMetaDataCommand());
    editColumnMetadata.setToolTipText("Edit selected column metadata");
    editColumnMetadata.setMenuItemPosition(i);
    editColumnMetadata.setMenu("Data", DATAMENUPOSITION);
    //editColumnMetadata.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    editColumnMetadata.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                            true, GUIAction.EVENT_LOCAL);
    editColumnMetadata.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    editColumnMetadata.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(editColumnMetadata);

    addDocumentation.setEnabled(false);
    createNewDatatable.setEnabled(false);
    deleteDatatable.setEnabled(false);
    addTemporalCovColumn.setEnabled(false);
    sortBySelectedColumn.setEnabled(false);
    insertRowAfter.setEnabled(false);
    insertRowBefore.setEnabled(false);
    deleteRow.setEnabled(false);
    insertColumnBefore.setEnabled(false);
    insertColumnAfter.setEnabled(false);
    deleteColumn.setEnabled(false);
    editColumnMetadata.setEnabled(false);

    // create new data package menu in file menu
    GUIAction createNewDataPackage = new GUIAction("New Datapackage...",
                                      UISettings.NEW_DATAPACKAGE_ICON,
                                      new CreateNewDataPackageCommand(morpho));
    createNewDataPackage.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/New16.gif")));
    createNewDataPackage.setToolTipText("Create a new data package");
    createNewDataPackage.setMenuItemPosition(1);
    createNewDataPackage.setMenu("File", 0);
    createNewDataPackage.setToolbarPosition(0);
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
                       Vector relations, ButterflyFlapCoordinator coordinator,
                       String doctype)
  {
    AbstractDataPackage adp = null;
    Log.debug(11, "DataPackage: Got service request to open: " +
                    identifier + " from " + location + ".");
      boolean metacat = false;
      boolean local = false;
      if ((location.equals(DataPackageInterface.METACAT))||
               (location.equals(DataPackageInterface.BOTH))) metacat = true;
      if ((location.equals(DataPackageInterface.LOCAL))||
               (location.equals(DataPackageInterface.BOTH))) local = true;
      adp = DataPackageFactory.getDataPackage(identifier, metacat, local);
    //Log.debug(11, "location: " + location + " identifier: " + identifier +
    //                " relations: " + relations.toString());


    long starttime = System.currentTimeMillis();
    final MorphoFrame packageWindow = UIController.getInstance().addWindow(
                "Data Package: "+identifier);
    packageWindow.setBusy(true);
    packageWindow.setVisible(true);


    packageWindow.addWindowListener(
                new WindowAdapter() {
                public void windowActivated(WindowEvent e)
                {
                    Log.debug(50, "Processing window activated event");
                    if (hasClipboardData(packageWindow)){
                      StateChangeMonitor.getInstance().notifyStateChange(
                        new StateChangeEvent(packageWindow,
                          StateChangeEvent.CLIPBOARD_HAS_DATA_TO_PASTE));
                    }
                    else {
                      StateChangeMonitor.getInstance().notifyStateChange(
                        new StateChangeEvent(packageWindow,
                          StateChangeEvent.CLIPBOARD_HAS_NO_DATA_TO_PASTE));
                }
                }
            });


    // Stop butterfly flapping for old window.
    //packageWindow.setBusy(true);
    if (coordinator != null)
    {
      coordinator.stopFlap();
    }
    long stoptime = System.currentTimeMillis();
    Log.debug(20,"ViewContainer startUp time: "+(stoptime-starttime));

    long starttime1 = System.currentTimeMillis();

    DataViewContainerPanel dvcp = null;
    dvcp = new DataViewContainerPanel(adp);
    dvcp.setFramework(morpho);

    dvcp.init();
    long stoptime1 = System.currentTimeMillis();
    Log.debug(20,"DVCP startUp time: "+(stoptime1-starttime1));

    dvcp.setSize(packageWindow.getDefaultContentAreaSize());
    dvcp.setPreferredSize(packageWindow.getDefaultContentAreaSize());
//    dvcp.setVisible(true);
    packageWindow.setMainContentPane(dvcp);

    // Broadcast stored event int dvcp
    dvcp.broadcastStoredStateChangeEvent();

    // Create another evnets too
    StateChangeMonitor monitor = StateChangeMonitor.getInstance();
//    String packageLocation = dp.getLocation();
    String packageLocation = location;
    if (packageLocation.equals(DataPackageInterface.BOTH))
    {
      // open a synchronize package
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_SYNCHRONIZED));
    }
    else
    {
      // open a unsynchronize pakcage
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_UNSYNCHRONIZED));
    }

    {
      // open a single version package
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_NO_VERSIONS));
    }
    monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME));
    packageWindow.setBusy(false);
  }


  /*
   *  This method is to be used to display a newly created AbstractDataPackage
   *  location and identifier have not yet been established
   */
  public void openNewDataPackage(AbstractDataPackage adp, ButterflyFlapCoordinator coordinator)
  {
    Log.debug(11, "DataPackage: Got service request to open a newly created AbstractDataPackage");
    boolean metacat = false;
    boolean local = false;

    long starttime = System.currentTimeMillis();
    final MorphoFrame packageWindow = UIController.getInstance().addWindow(
                "Data Package: "+adp.getAccessionNumber());
    packageWindow.setBusy(true);
    packageWindow.setVisible(true);


    packageWindow.addWindowListener(
                new WindowAdapter() {
                public void windowActivated(WindowEvent e)
                {
                    Log.debug(50, "Processing window activated event");
                    if (hasClipboardData(packageWindow)){
                      StateChangeMonitor.getInstance().notifyStateChange(
                        new StateChangeEvent(packageWindow,
                          StateChangeEvent.CLIPBOARD_HAS_DATA_TO_PASTE));
                    }
                    else {
                      StateChangeMonitor.getInstance().notifyStateChange(
                        new StateChangeEvent(packageWindow,
                          StateChangeEvent.CLIPBOARD_HAS_NO_DATA_TO_PASTE));
                }
                }
            });


    // Stop butterfly flapping for old window.
    //packageWindow.setBusy(true);
    if (coordinator != null)
    {
      coordinator.stopFlap();
    }
    long stoptime = System.currentTimeMillis();
    Log.debug(20,"ViewContainer startUp time: "+(stoptime-starttime));

    long starttime1 = System.currentTimeMillis();

    DataViewContainerPanel dvcp = null;
    dvcp = new DataViewContainerPanel(adp);
    dvcp.setFramework(morpho);
    dvcp.init();
    long stoptime1 = System.currentTimeMillis();
    Log.debug(20,"DVCP startUp time: "+(stoptime1-starttime1));

    dvcp.setSize(packageWindow.getDefaultContentAreaSize());
    dvcp.setPreferredSize(packageWindow.getDefaultContentAreaSize());
//    dvcp.setVisible(true);
    packageWindow.setMainContentPane(dvcp);

    // Broadcast stored event int dvcp
    dvcp.broadcastStoredStateChangeEvent();

    // Create another events too
    StateChangeMonitor monitor = StateChangeMonitor.getInstance();
      // open a unsynchronize pakcage
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_UNSYNCHRONIZED));

      // open a single version package
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_NO_VERSIONS));

    monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME));
    packageWindow.setBusy(false);
  }


  /**
   * Uploads the package to metacat. The location is assumed to be
   * DataPackageInterface.LOCAL
   *
   * @param docid the id of the package to upload
   * @param updateIds boolean
   * @throws MetacatUploadException
   * @return String
   */
  public String upload(String docid, boolean updateIds)
              throws MetacatUploadException
  {
    AbstractDataPackage adp = DataPackageFactory.getDataPackage(docid, false, true);
                      // metacat flag is false; local is true
    AbstractDataPackage newadp = adp.upload(docid, updateIds);
    return newadp.getPackageId();
  }

  /**
   * Downloads the package from metacat.  The location is assumed to be
   * DataPackageInterface.METACAT
   * @param docid the id of the package to download
   */
  public void download(String docid)
  {
//    DataPackage dp = new DataPackage(DataPackageInterface.METACAT, docid, null, morpho, true);
//    dp.download();
    AbstractDataPackage adp = DataPackageFactory.getDataPackage(docid, true, false);
                      // metacat flag is true; local is false
    AbstractDataPackage newadp = adp.download(docid);
  }


  /**
   * Deletes the package.
   *
   * @param docid the id of the package to download
   * @param location String
   */
  public void delete(String docid, String location)
  {
//    DataPackage dp = new DataPackage(location, docid, null, morpho, false);
//    dp.delete(location);
    boolean metacat = false;
    boolean local = false;
    if (location.equals(AbstractDataPackage.METACAT)) metacat = true;
    if (location.equals(AbstractDataPackage.LOCAL)) local = true;
    if (location.equals(AbstractDataPackage.BOTH)) {
      metacat = true;
      local = true;
    }
    AbstractDataPackage adp = DataPackageFactory.getDataPackage(docid, metacat, local);
    if (adp!=null) {
      adp.delete(location);
    }
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
//    DataPackage dp = new DataPackage(location, docid, null, morpho, false);
//    dp.export(path);
    boolean local = false;
    boolean metacat = false;
    if (location.equals(AbstractDataPackage.LOCAL)) {
      local = true;
    }
    else if (location.equals(AbstractDataPackage.METACAT)) {
      metacat = true;
    }
    else if (location.equals(AbstractDataPackage.BOTH)) {
      local = true;
      metacat = true;
    }
    AbstractDataPackage adp = DataPackageFactory.getDataPackage(docid, metacat, local);
    adp.export(path);
  }

  /**
   * Exports the package to eml2
   * @param docid the id of the package to export
   * @param path the directory to which the package should be exported.
   * @param location the location where the package is now: LOCAL, METACAT or
   * BOTH
   */
  public void exportToEml2(String docid, String path, String location)
  {
    DataPackage dp = new DataPackage(location, docid, null, morpho, false);
    dp.exportToEml2(path);
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
    boolean local = false;
    boolean metacat = false;
    if (location.equals(AbstractDataPackage.LOCAL)) {
      local = true;
    }
    else if (location.equals(AbstractDataPackage.METACAT)) {
      metacat = true;
    }
    else if (location.equals(AbstractDataPackage.BOTH)) {
      local = true;
      metacat = true;
    }
    AbstractDataPackage adp = DataPackageFactory.getDataPackage(docid, metacat, local);
    adp.exportToZip(path);


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
   * returns the next local id from the config file returns null if configXML
   * was unable to increment the id number
   *
   * @param morpho the morpho file
   * @return String
   */
  public String getNextId(Morpho morpho)
  {
    String identifier = null;
    AccessionNumber accession = new AccessionNumber(morpho);
    identifier = accession.getNextId();
    return identifier;
  }


  /**
   * Method to get docid from a given morpho frame
   *
   * @param morphoFrame the morphoFrame which contains a datapackage
   * @return String
   */
  public String getDocIdFromMorphoFrame(MorphoFrame morphoFrame)
  {
    String docid = null;
    AbstractDataPackage adp = getAbstractDataPackageFromMorphoFrame(morphoFrame);
    docid = adp.getPackageId();
    Log.debug(50, "docid is: "+ docid);
    return docid;
  }


  /**
   * Method to determine a data package which in a morpho frame if is in local
   *
   * @param morphoFrame the morpho frame containing the data package
   * @return boolean
   */
  public boolean isDataPackageInLocal(MorphoFrame morphoFrame)
  {
    String location = null;
    boolean flagInLocal = false;
     DataViewContainerPanel dvcp = AddDocumentationCommand.
                               getDataViewContainerPanelFromMorphoFrame(morphoFrame);
     AbstractDataPackage adp = dvcp.getAbstractDataPackage();

       location = adp.getLocation();


      if (location.equals(DataPackageInterface.LOCAL) ||
         location.equals(DataPackageInterface.BOTH))
      {
        flagInLocal = true;
        Log.debug(50, "docid is in local");
      }//if
    return flagInLocal;
  }


  /**
   * Method to determine a data package which in a morpho frame if is in network
   *
   * @param morphoFrame the morpho frame containing the data package
   * @return boolean
   */
  public boolean isDataPackageInNetwork(MorphoFrame morphoFrame)
  {
    String location = null;
    boolean flagInNetwork = false;
     DataViewContainerPanel dvcp = AddDocumentationCommand.
                               getDataViewContainerPanelFromMorphoFrame(morphoFrame);
     AbstractDataPackage adp = dvcp.getAbstractDataPackage();
       location = adp.getLocation();

      if (location.equals(DataPackageInterface.METACAT) ||
         location.equals(DataPackageInterface.BOTH))
      {
        flagInNetwork = true;
        Log.debug(50, "docid is in network");
      }//if
    return flagInNetwork;
  }


  /*
   * Method to get package in a given morphoFrame. If the morpho frame doesn't
   * contain a datapackage, null will be returned
   */
  private AbstractDataPackage getAbstractDataPackageFromMorphoFrame(MorphoFrame morphoFrame)
  {
    AbstractDataPackage data = null;
    DataViewContainerPanel resultPane = null;

    if (morphoFrame != null)
    {
       resultPane = AddDocumentationCommand.
                          getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    }//if

    // make sure resulPanel is not null
    if (resultPane != null)
    {
       data = resultPane.getAbstractDataPackage();
    }//if
    return data;
  }//getDataPackageFromMorphoFrame

  private boolean hasClipboardData(Component c) {
    boolean ret = true;
 		Transferable t = c.getToolkit().getSystemClipboard().getContents(null);
    if (t==null) {
      ret = false;
    }
    else{
      String sel = "";
      try{
        sel = (String)t.getTransferData(DataFlavor.stringFlavor);
      }
      catch (Exception e) {
        Log.debug(40, "Problem getting data from clipboard");
        ret = false;
      }
      if ((sel==null)||(sel.length()<1)) ret = false;
    }
    return ret;
  }


  /**
   * return an instance of a Command object, identified by one of the integer
   * constants defined above
   *
   * @param commandIdentifier integer constant identifying the command Options
   *   include:<ul> <li>NEW_DATAPACKAGE_COMMAND</li> </ul>
   * @throws ClassNotFoundException
   * @return Command
   */
  public Command getCommandObject(int commandIdentifier)
                                                  throws ClassNotFoundException
  {
    switch (commandIdentifier) {

        case DataPackageInterface.NEW_DATAPACKAGE_COMMAND:
            return new CreateNewDataPackageCommand(morpho);
        case DataPackageInterface.NEW_DATA_TABLE_COMMAND:
            return new ImportDataCommand();
        default:
            ClassNotFoundException e
                                = new ClassNotFoundException("command with ID="
                                            +commandIdentifier+" not found");
            e.fillInStackTrace();
            throw e;
    }
  }


}//DataPackagePlugin
