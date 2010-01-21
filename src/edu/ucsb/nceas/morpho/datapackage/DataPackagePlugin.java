/**
 *  '$RCSfile: DataPackagePlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 22:03:01 $'
 * '$Revision: 1.113 $'
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
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.framework.ButterflyFlapCoordinator;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
//import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.NewPackageWizardListener;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.plugins.IncompleteDocInfo;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.morpho.util.XMLTransformer;
import edu.ucsb.nceas.morpho.plugins.WizardPageInfo;

import java.util.Vector;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Class that implements the plugin for package editing
 */
public class DataPackagePlugin
       implements PluginInterface, ServiceProvider, DataPackageInterface
{
  /** A reference to the container framework */
  private Morpho morpho = null;

  public static final String EDIT_MENU_LABEL = "Edit";
  public static final String METADATA_MENU_LABEL = "Documentation";
  public static final String DATA_MENU_LABEL = "Data";

  /** Constant int for documentation menu position */
  public static final int DOCUMENTATIONMENUPOSITION = 30;

  /** Constant int for data menu position */
  public static final int DATAMENUPOSITION = 40;

  /** Constant int for edit menu position */
  public static final int EDITMENUPOSITION = 10;

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
    saveAction.setMenu(Morpho.FILE_MENU_LABEL, Morpho.FILEMENUPOSITION);
    saveAction.setToolbarPosition(2);
    saveAction.setEnabled(false);
    saveAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    saveAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(saveAction);

    // Save dialog box action
    GUIAction saveCopyAction = new GUIAction("Save Duplicate...",
                                              UISettings.DUPLICATE_ICON,
                                              new SavePackageCopyCommand());
    saveCopyAction.setMenuItemPosition(5);
    saveCopyAction.setToolTipText("Duplicate this data package and save locally...");
    saveCopyAction.setMenu(Morpho.FILE_MENU_LABEL, Morpho.FILEMENUPOSITION);
    saveCopyAction.setToolbarPosition(3);
    saveCopyAction.setEnabled(false);
    saveCopyAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    saveCopyAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(saveCopyAction);
    
    Command importEMLDocument = new ImportEMLFileCommand();
    GUIAction importEMLDocumentAction =
      new GUIAction("Import...", null, importEMLDocument);
    importEMLDocumentAction.setToolTipText("Import an EML Document...");
    importEMLDocumentAction.setMenuItemPosition(15);
    //importEMLDocumentAction.setSeparatorPosition(SEPARATOR_FOLLOWING);
    importEMLDocumentAction.setMenu(Morpho.FILE_MENU_LABEL, Morpho.FILEMENUPOSITION);
    controller.addGuiAction(importEMLDocumentAction);


    GUIAction revertTab = new GUIAction("Revert Entity to Saved Version",
        null, new RevertCommand());
    revertTab.setToolTipText("Revert to original data shown in current tab");
    revertTab.setMenuItemPosition(0);
    revertTab.setMenu(EDIT_MENU_LABEL, EDITMENUPOSITION);
    revertTab.setEnabled(false);
    revertTab.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    revertTab.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    revertTab.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    revertTab.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(revertTab);

    GUIAction revertAll = new GUIAction("Revert All Entities to Saved Version",
        null, new RevertAllCommand());
    revertAll.setToolTipText("Revert to original data shown in all tabs");
    revertAll.setMenuItemPosition(1);
    revertAll.setMenu(EDIT_MENU_LABEL, EDITMENUPOSITION);
    revertAll.setEnabled(false);
    revertAll.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    revertAll.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    revertAll.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    revertAll.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_NONEDITABLE_ENTITY_DATAPACKAGE_FRAME,
                   false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(revertAll);

    GUIAction cut = new GUIAction("Cut", null, new TableCutCommand());
    cut.setToolTipText("Cut value in data table cells");
    cut.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Cut16.gif")));
    cut.setAcceleratorKeyString(CUTKEY);
    cut.setMenuItemPosition(2);
    cut.setSeparatorPosition(Morpho.SEPARATOR_PRECEDING);
    cut.setMenu(EDIT_MENU_LABEL, EDITMENUPOSITION);
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

    // For edit menu
    GUIAction copy = new GUIAction("Copy", null, new TableCopyCommand());
    copy.setToolTipText("Copy value in data table cells");
    copy.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Copy16.gif")));
    copy.setAcceleratorKeyString(COPYKEY);
    copy.setMenuItemPosition(3);
    copy.setMenu(EDIT_MENU_LABEL, EDITMENUPOSITION);
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


    GUIAction paste = new GUIAction("Paste", null, new TablePasteCommand());
    paste.setToolTipText("Paste value in data table cells");
    paste.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Paste16.gif")));
    paste.setAcceleratorKeyString(PASTEKEY);
    paste.setMenuItemPosition(4);
    paste.setMenu(EDIT_MENU_LABEL, EDITMENUPOSITION);
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

    int i = 0; // postition for menu item
    GUIAction addDocumentation = new GUIAction("Add/Edit Documentation...", null,
                                          new AddDocumentationCommand());
    addDocumentation.setToolTipText("Add/Edit XML documentation...");
    addDocumentation.setMenuItemPosition(i);
    addDocumentation.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addDocumentation.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                            true, GUIAction.EVENT_LOCAL);
    addDocumentation.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addDocumentation);


    i++;
    GUIAction viewDocumentation = new GUIAction("View Documentation...", null,
                                          new ViewDocumentationCommand());
    viewDocumentation.setToolTipText("View Documentation as HTML...");
    viewDocumentation.setMenuItemPosition(i);
    viewDocumentation.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    viewDocumentation.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    viewDocumentation.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                            true, GUIAction.EVENT_LOCAL);
    viewDocumentation.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(viewDocumentation);

    i++;
    GUIAction addTitleAbstractAction = new GUIAction(
        "Title & Abstract...",
        null, new AddTitleAbstractCommand());
    addTitleAbstractAction.setToolTipText("Edit Title & Abstract...");
    addTitleAbstractAction.setMenuItemPosition(i);
    addTitleAbstractAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addTitleAbstractAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    addTitleAbstractAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addTitleAbstractAction);

    i++;
    GUIAction addKeywordAction = new GUIAction("Keywords...",
        null, new AddKeywordCommand());
    addKeywordAction.setToolTipText("Add, edit or delete Keywords...");
    addKeywordAction.setMenuItemPosition(i);
    addKeywordAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addKeywordAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    addKeywordAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addKeywordAction);

    i++;
    GUIAction addCreatorAction = new GUIAction("Owners...",
        null, new AddCreatorCommand());
    addCreatorAction.setToolTipText("Add, edit or delete Owners...");
    addCreatorAction.setMenuItemPosition(i);
    addCreatorAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addCreatorAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    addCreatorAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addCreatorAction);

    i++;
    GUIAction addContactAction = new GUIAction("Contacts...",
        null, new AddContactCommand());
    addContactAction.setToolTipText("Add, edit or delete Contacts...");
    addContactAction.setMenuItemPosition(i);
    addContactAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addContactAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    addContactAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addContactAction);

    i++;
    GUIAction addAdditionalPartyAction = new GUIAction("Associated Parties...",
        null, new AddAdditionalPartyCommand());
    addAdditionalPartyAction.setToolTipText(
        "Add, edit or delete Associated Parties...");
    addAdditionalPartyAction.setMenuItemPosition(i);
    addAdditionalPartyAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addAdditionalPartyAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    addAdditionalPartyAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addAdditionalPartyAction);

    i++;
    GUIAction addResearchProjectAction = new GUIAction(
        "Research Project...",
        null, new AddResearchProjectCommand());
    addResearchProjectAction.setToolTipText("Edit Research Project details...");
    addResearchProjectAction.setMenuItemPosition(i);
    addResearchProjectAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addResearchProjectAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    addResearchProjectAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addResearchProjectAction);

    i++;
    GUIAction addUsageRightsAction = new GUIAction(
        "Usage Rights...",
        null, new AddUsageRightsCommand());
    addUsageRightsAction.setToolTipText("Edit Usage Rights...");
    addUsageRightsAction.setMenuItemPosition(i);
    addUsageRightsAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addUsageRightsAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    addUsageRightsAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addUsageRightsAction);


    i++;
    GUIAction addGeographicCovAction = new GUIAction("Geographic Coverage...",
                                           null, new AddGeographicCovCommand());
    addGeographicCovAction.setToolTipText("Geographic Coverage...");
    addGeographicCovAction.setMenuItemPosition(i);
    addGeographicCovAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addGeographicCovAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    addGeographicCovAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                            true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addGeographicCovAction);

    i++;
    GUIAction addTemporalCovAction = new GUIAction("Temporal Coverage...",
                                           null, new AddTemporalCovCommand());
    addTemporalCovAction.setToolTipText("Temporal Coverage...");
    addTemporalCovAction.setMenuItemPosition(i);
    addTemporalCovAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addTemporalCovAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    addTemporalCovAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                            true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addTemporalCovAction);

    i++;
    GUIAction addTaxonomicCovAction = new GUIAction("Taxonomic Coverage...",
                                           null, new AddTaxonomicCovCommand());
    addTaxonomicCovAction.setToolTipText("Taxonomic Coverage...");
    addTaxonomicCovAction.setMenuItemPosition(i);
    addTaxonomicCovAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addTaxonomicCovAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    addTaxonomicCovAction.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                            true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addTaxonomicCovAction);

    i++;
    GUIAction addMethodAction = new GUIAction(
        "Methods...",
        null, new AddMethodCommand());
    addMethodAction.setToolTipText("Edit Methods...");
    addMethodAction.setMenuItemPosition(i);
    addMethodAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addMethodAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    addMethodAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addMethodAction);

    i++;
    GUIAction addAccessAction = new GUIAction(
        "Access Information...",
        null, new AddAccessCommand());
    addAccessAction.setToolTipText("Add, edit or delete Access Permissions...");
    addAccessAction.setMenuItemPosition(i);
    addAccessAction.setMenu(METADATA_MENU_LABEL, DOCUMENTATIONMENUPOSITION);
    addAccessAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    addAccessAction.setEnabledOnStateChange(
        StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
        true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(addAccessAction);

    // For data menu
    i = 0; // postition for menu item in data menu


    GUIAction createNewDatatable = new GUIAction("Create/Import New Data Table...", null,
                                                      new ImportDataCommand());
    createNewDatatable.setToolTipText("Add a new table");
    createNewDatatable.setMenuItemPosition(i);
    createNewDatatable.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
    createNewDatatable.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                            true, GUIAction.EVENT_LOCAL);
    createNewDatatable.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(createNewDatatable);


    i = i+1;
    GUIAction deleteDatatable = new GUIAction("Delete Current Data Table", null,
                                                      new DeleteTableCommand());
    deleteDatatable.setToolTipText("Remove the currently displayed table");
    deleteDatatable.setMenuItemPosition(i);
    deleteDatatable.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
    //deleteDatatable.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
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
    
    i = i+1;
    GUIAction editDatatableAccess = new GUIAction("Edit Data Table Access", null,
                                                      new AddEntityAccessCommand());
    editDatatableAccess.setToolTipText("Edit Access rights for currently displayed table");
    editDatatableAccess.setMenuItemPosition(i);
    editDatatableAccess.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
    editDatatableAccess.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    editDatatableAccess.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_EDITABLE_ENTITY_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    editDatatableAccess.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_SEARCH_RESULT_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    editDatatableAccess.setEnabledOnStateChange(
                            StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME,
                            false, GUIAction.EVENT_LOCAL);
    editDatatableAccess.setEnabledOnStateChange(
                   StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME,
                   true, GUIAction.EVENT_LOCAL);
    controller.addGuiAction(editDatatableAccess);


    i= i+2; // separator will take a position so add 2
    GUIAction sortBySelectedColumn = new GUIAction("Sort by Selected Column",
                                           null, new SortDataTableCommand());
    sortBySelectedColumn.setToolTipText("Sort table by selected column");
    sortBySelectedColumn.setMenuItemPosition(i);
    sortBySelectedColumn.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
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
    insertRowAfter.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
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
    insertRowBefore.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
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
    deleteRow.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
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
    insertColumnAfter.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
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
    insertColumnBefore.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
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
    deleteColumn.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
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
    GUIAction editColumnMetadata = new GUIAction("Edit Column "+METADATA_MENU_LABEL, null,
                                      new EditColumnMetaDataCommand());
    editColumnMetadata.setToolTipText("Edit selected column "+METADATA_MENU_LABEL);
    editColumnMetadata.setMenuItemPosition(i);
    editColumnMetadata.setMenu(DATA_MENU_LABEL, DATAMENUPOSITION);
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
		viewDocumentation.setEnabled(false);
    createNewDatatable.setEnabled(false);
    deleteDatatable.setEnabled(false);
    editDatatableAccess.setEnabled(false);
    addTitleAbstractAction.setEnabled(false);
    addKeywordAction.setEnabled(false);
    addCreatorAction.setEnabled(false);
    addContactAction.setEnabled(false);
    addAdditionalPartyAction.setEnabled(false);
    addResearchProjectAction.setEnabled(false);
    addUsageRightsAction.setEnabled(false);
    addGeographicCovAction.setEnabled(false);
    addTemporalCovAction.setEnabled(false);
    addTaxonomicCovAction.setEnabled(false);
    addMethodAction.setEnabled(false);
    addAccessAction.setEnabled(false);
    sortBySelectedColumn.setEnabled(false);
    insertRowAfter.setEnabled(false);
    insertRowBefore.setEnabled(false);
    deleteRow.setEnabled(false);
    insertColumnBefore.setEnabled(false);
    insertColumnAfter.setEnabled(false);
    deleteColumn.setEnabled(false);
    editColumnMetadata.setEnabled(false);

    // create new data package menu in file menu
    GUIAction createNewDataPackage = new GUIAction("New Data Package...",
                                      UISettings.NEW_DATAPACKAGE_ICON,
                                      new CreateNewDataPackageCommand(morpho));
    createNewDataPackage.setSmallIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/New16.gif")));
    createNewDataPackage.setToolTipText("Create a new data package");
    createNewDataPackage.setMenuItemPosition(1);
    createNewDataPackage.setMenu(Morpho.FILE_MENU_LABEL, Morpho.FILEMENUPOSITION);
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
  
  
  /**
   * Opens an incomplete data package from incomplete dir.
   * Note: opening an incomplete data package from data dir will use openDataPackage method.
   * @param identifier
   * @param coordinator the coordinator for butterfly flapping
   */
  public void openIncompleteDataPackage(String identifier, ButterflyFlapCoordinator coordinator)
  {
	  AbstractDataPackage adp = null;
	  adp =DataPackageFactory.getDataPackageFromIncompeteDir(identifier);
	  /*if(adp.getCompletionStatus().equals(IncompleteDocSettings.INCOMPLETE_PACKAGE_WIZARD))
	  {
	    adp.setAccessionNumber(NewPackageWizardListener.TEMPORARYID);
	  }*/  
	  openIncompleteDataPackage(adp, coordinator);
  }
  
  /*
   * Opens an incomplete data pacakge base on an AbstractDataPackage 
   */
  private void openIncompleteDataPackage(AbstractDataPackage adp, ButterflyFlapCoordinator coordinator)
  {
	  ServiceController sc;
	    DataPackageWizardInterface dpwPlugin = null;
	    try {
	      if(adp == null)
	      {
	    	  Log.debug(5, "The data package which will be opened is null!");
    		  return;
	      }
	      sc = ServiceController.getInstance();
	      //EML200DataPackage eml200 = (EML200DataPackage)adp;	     
	      dpwPlugin = (DataPackageWizardInterface) sc.getServiceProvider(DataPackageWizardInterface.class);
	      dpwPlugin.loadIncompleteDocument(adp);

	    }
	    catch (ServiceNotHandledException se) {

	      Log.debug(6, se.getMessage());
	      se.printStackTrace();
	    }
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

    String completionStatus = adp.getCompletionStatus();
    if(completionStatus != null && !completionStatus.equals(AbstractDataPackage.COMPLETED))
    {
      //open incomplete data package.
      openIncompleteDataPackage(adp, coordinator);
      return;
    }
    //open complete data package.
    long starttime = System.currentTimeMillis();
    final MorphoFrame packageWindow = UIController.getInstance().addWindow(
                "Data Package: "+identifier);
    packageWindow.setBusy(true);
    packageWindow.setVisible(true);

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

    // figure out whether there may be multiple versions, based on identifier
    int lastDot = identifier.lastIndexOf(".");
    String verNum = identifier.substring(lastDot+1,identifier.length());
    if (verNum.equals("1")) {
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_NO_VERSIONS));
    }
    else {
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_VERSIONS));
    }

    monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME));
    adp.loadCustomUnits();
	packageWindow.setBusy(false);
	
	final DataViewContainerPanel dvcpReference = dvcp;
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
                public void windowClosed(WindowEvent e) {
                	//remove the listeners
                	StateChangeMonitor.getInstance().removeStateChangeListener(StateChangeEvent.SELECT_DATATABLE_COLUMN, dvcpReference);
                	StateChangeMonitor.getInstance().removeStateChangeListener(StateChangeEvent.METAVIEWER_HISTORY_BACK, dvcpReference);
                }
            });
    
	//check if we should upgrade eml to the newest version
	try
	{
		DataPackageWizardListener listener = null; // we don't need it do any thing. so pass null to it.
		EMLTransformToNewestVersionDialog dialog = new EMLTransformToNewestVersionDialog(packageWindow, listener);
	}
	catch(Exception e)
	{
		Log.debug(20, "Couldn't upgrade eml to the newest version");
	}
		
  }
  
 
/**
 * Display a newly created abstract data package
 * @param adp  the data package will be display
 * @param coordinator the coordinator 
 * @param visible if this frame visible
 * @return a MorphoFrame which displays the data package
 */
  public MorphoFrame openNewDataPackage(AbstractDataPackage adp, ButterflyFlapCoordinator coordinator, boolean visible)
  {
    return openNewDataPackageFrame(adp, coordinator, visible);
  }
  
  /**
   * Display a newly created abstract data package
   * @param adp  the data package will be display
   * @param coordinator the coordinator 
   * @return a MorphoFrame which displays the data package
   */
    public MorphoFrame openNewDataPackage(AbstractDataPackage adp, ButterflyFlapCoordinator coordinator)
    {
      boolean visible = true;
      return openNewDataPackageFrame(adp, coordinator, visible);
    }

  
  /*
   *  This method is to be used to display a newly created AbstractDataPackage
   *  location and identifier have not yet been established
   */
  private MorphoFrame openNewDataPackageFrame(AbstractDataPackage adp, ButterflyFlapCoordinator coordinator, boolean visible)
  {
    Log.debug(11, "DataPackage: Got service request to open a newly created AbstractDataPackage");
    boolean metacat = false;
    boolean local = false;

    long starttime = System.currentTimeMillis();
    final MorphoFrame packageWindow = UIController.getInstance().addWindow(
                "Data Package: "+adp.getAccessionNumber());
    if(visible)
    {
      packageWindow.setBusy(true);
      packageWindow.setVisible(true);
    }
   

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

    // figure out whether there may be multiple versions, based on identifier
    String identifier = adp.getAccessionNumber();
    int lastDot = identifier.lastIndexOf(".");
    String verNum = identifier.substring(lastDot+1,identifier.length());
    if (verNum.equals("1")) {
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_NO_VERSIONS));
    }
    else {
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_VERSIONS));
    }

    monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME));
    adp.loadCustomUnits();
  if(visible)
  {
    packageWindow.setBusy(false);
  }
	
	final DataViewContainerPanel dvcpReference = dvcp;
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
            public void windowClosed(WindowEvent e) {
            	//remove the listeners
            	StateChangeMonitor.getInstance().removeStateChangeListener(StateChangeEvent.SELECT_DATATABLE_COLUMN, dvcpReference);
            	StateChangeMonitor.getInstance().removeStateChangeListener(StateChangeEvent.METAVIEWER_HISTORY_BACK, dvcpReference);
            }
        });
	    
	return packageWindow;
  }

   /*
   *  This method is to be used to display a newly created AbstractDataPackage
   *  location and identifier have not yet been established
   *
   *  This window will have a visibility of false!
   */
  public void openHiddenNewDataPackage(AbstractDataPackage adp, ButterflyFlapCoordinator coordinator)
  {
    Log.debug(11, "DataPackage: Got service request to open a newly created AbstractDataPackage");
    boolean metacat = false;
    boolean local = false;

    long starttime = System.currentTimeMillis();
    final MorphoFrame packageWindow = UIController.getInstance().addHiddenWindow(
                "Data Package: "+adp.getAccessionNumber());
    packageWindow.setBusy(true);


    


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
    dvcp.setVisible(true);
    try
    {
        Thread.sleep(1000);
    }
    catch(Exception e)
    {
    	Log.debug(30,"Couldn't sleep the thread");
    }
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

    // figure out whether there may be multiple versions, based on identifier
    String identifier = adp.getAccessionNumber();
    int lastDot = identifier.lastIndexOf(".");
    String verNum = identifier.substring(lastDot+1,identifier.length());
    if (verNum.equals("1")) {
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_NO_VERSIONS));
    }
    else {
      monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME_VERSIONS));
    }

    monitor.notifyStateChange(
                 new StateChangeEvent(
                 dvcp,
                 StateChangeEvent.CREATE_DATAPACKAGE_FRAME));
    packageWindow.setBusy(false);
    
    final DataViewContainerPanel dvcpReference = dvcp;
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
            public void windowClosed(WindowEvent e) {
            	//remove the listeners
            	StateChangeMonitor.getInstance().removeStateChangeListener(StateChangeEvent.SELECT_DATATABLE_COLUMN, dvcpReference);
            	StateChangeMonitor.getInstance().removeStateChangeListener(StateChangeEvent.METAVIEWER_HISTORY_BACK, dvcpReference);
            }
            
        });

    UIController.getInstance().setCurrentActiveWindow(packageWindow);
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
    if (newadp != null)
    {
         return newadp.getPackageId();
    }
    else
    {
    	return null;
    }
    
  }

  /**
   * Downloads the package from metacat.  The location is assumed to be
   * DataPackageInterface.METACAT
   * @param docid the id of the package to download
   */
  public String download(String docid)
  {
//    DataPackage dp = new DataPackage(DataPackageInterface.METACAT, docid, null, morpho, true);
//    dp.download();
    AbstractDataPackage adp = DataPackageFactory.getDataPackage(docid, true, false);
                      // metacat flag is true; local is false
    AbstractDataPackage newadp = adp.download(docid);
    if (newadp != null)
    {
         return newadp.getPackageId();
    }
    else
    {
    	return null;
    }
  }


  /**
   * Deletes the package.
   *
   * @param docid the id of the package to download
   * @param location String
   */
  public void delete(String docid, String location) throws Exception
  {
//    DataPackage dp = new DataPackage(location, docid, null, morpho, false);
//    dp.delete(location);
    if(location != null && location.equals(QueryRefreshInterface.LOCALINCOMPLETEPACKAGE))
    {
      FileSystemDataStore store = new FileSystemDataStore(Morpho.thisStaticInstance);
      File incompleteFile = store.openIncompleteFile(docid);
      AbstractDataPackage adp = DataPackageFactory.getDataPackage(incompleteFile);
      if(adp != null)
      {
        adp.deleteDataFilesInIncompleteFolder();
      }
      store.deleteInCompleteFile(docid);      
    }
    else
    {
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
     DataViewContainerPanel dvcp = morphoFrame.getDataViewContainerPanel();
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
     DataViewContainerPanel dvcp = morphoFrame.getDataViewContainerPanel();
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
  
  /**
   * Method to get a Document node as a representation of data package for
   * given id and location
   * @param docid the identifier of the package
   * @param location the location of the package
   * @return
   */
  public Document getDocumentNode(String docid, String location)
  {
    Document doc = null; 
    AbstractDataPackage adp = getAbstractDataPackage(docid, location);
    if(adp != null)
    {
      doc = adp.getDocument();
    }
    return doc;
  }
  
  /*
   * Gets the abstract dataPackage from given docid and location
   */
  private AbstractDataPackage getAbstractDataPackage(String docid, String location)
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
    return adp;
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
       resultPane = morphoFrame.getDataViewContainerPanel();
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
  
  /**
   * Save the incomplete xml document into local file system
   * @param docid if xml doesn't have id, this given id will be used as package id
   * @param xml the source of xml
   * @return the id of saved data package.
   */
  public void saveIncompleteDocumentForLater(String docid,  Reader xml) throws Exception
  {
    Log.debug(30, "given docid is "+docid +" in DataPackagePlugin.saveIncompleteDocumentForLater");
    EML200DataPackage adp = (EML200DataPackage)DataPackageFactory.getDataPackage(xml, false, true);
    ((EML200DataPackage)adp).setEMLVersion(EML200DataPackage.LATEST_EML_VER);
    adp.setAccessionNumber(docid);
    adp.serializeIncompleteData();
    adp.removeTracingChangeElement();
    adp.serializeIncompleteMetadata();
    //Util.deleteAutoSavedFile(autoSavedID);
    //return adp.getAccessionNumber();
  }
  
  /**
   * Export a data package to files with Biological Data Profile format
   * @param outputFile the output file 
   * @param styleSheetLocation the style sheet location
   * @param docid the docid of the data package
   * @param documentLocation the document location 
   */
  public void exportToBDP(File outputFile, String styleSheetLocation, String docid, String documentLocation) throws Exception
  {
    //Log.debug(5, "exporting to BDP hasn't done yet");
    //get output file writer
    FileWriter outputFileWriter = null;
      
    //get style sheet reader
    File styleSheetFile = new File(styleSheetLocation);
    FileReader styleSheetReader = null;
    File styleSheetDir = null;
    try
    {
      styleSheetReader = new FileReader(styleSheetFile);
      styleSheetDir = styleSheetFile.getParentFile();
    }
    catch(Exception e)
    {
      //Log.debug(5, "Morpho couldn't find a style sheet file at location "+styleSheetLocation);
      //return; 
      throw new Exception("Morpho couldn't find a style sheet file at location "+styleSheetLocation);
      
    }
      
    AbstractDataPackage dataPackage = getAbstractDataPackage(docid, documentLocation);
    if(dataPackage == null)
    {
      throw new Exception("Morpho couldn't open the data package with docid "+docid+" at the location "+documentLocation);
    }
    XMLTransformer transformer = XMLTransformer.getInstance();
    //since BDP only support one entity, we have to separately 
    int size = dataPackage.getEntityCount();
    if( size <= 1 )
    {
      doTransform(transformer, dataPackage.getDocument(), styleSheetReader, styleSheetDir.getAbsolutePath(), outputFile);
    }
    else
    {
      //we have to handle multiple entities by different file name.
      FileName name = new FileName(outputFile);
      String fileName = name.getFileName();
      String extension = name.getExtension();
      if(fileName != null)
      {
        //store the entity node into an array
        //note: entityArrary[0] contains the last entity.
        Node[] entityArray = new Node[size];
        for(int i=0; i<size; i++)
        {
          
          entityArray[i] = dataPackage.deleteLastEntity();
        }
        //now, datapackage doesn't have any entity
        for(int i=0; i<size; i++)
        {
          Node node = entityArray[size-i-1];
          if(node != null)
          {
            Entity entity = new Entity(node);
            dataPackage.insertEntity(entity, 0);
            File newOutPutFile = new File(fileName+i+extension);          
            styleSheetReader = new FileReader(styleSheetFile);
            doTransform(transformer, dataPackage.getDocument(), styleSheetReader, styleSheetDir.getAbsolutePath(), newOutPutFile);
            dataPackage.deleteEntity(0);
          }
        }
      }
      else
      {
        throw new Exception("The output file name is null");
      }
    }
    
  }
  
  /*
   * Transform a document to another metadata format and write it to an output file
   */
  private void doTransform(XMLTransformer transformer, Document emlDoc, 
      Reader styleSheetReader, String xslLocation, File outputFile) throws Exception
  {
    Reader anotherMetadataReader = transformer.transform(emlDoc, styleSheetReader, xslLocation);
    FileWriter outputFileWriter = new FileWriter(outputFile);
    char[] chartArray = new char[4*1024];
    int index = anotherMetadataReader.read(chartArray);
    while(index != -1)
    {
      outputFileWriter.write(chartArray, 0, index);
      outputFileWriter.flush();
      index = anotherMetadataReader.read(chartArray);
    }
    anotherMetadataReader.close();
    outputFileWriter.close();
  }


  /*
   * Represents a file name in two parts: file name and extension.
   * For instance, file name "example.xml" will be split into example and .xml
   * file name "example" will be splict into example and "".
   */
  private class FileName
  {
    private String fileName = null;
    private String extension = "";
    private File file = null;
    private static final String DOT = ".";
    
    /**
     * Constructor will parse the file name
     * @param file
     */
    public FileName(File file)
    {
        this.file = file;
        parse();
     
    }
    
    /*
     * Parse the file name into two parts
     */
    private void parse()
    {
      if(file != null)
      {
        String name = file.getName();
        Log.debug(30, "file name in DataPkcagPlugin. parser() "+ name);
        String path = file.getParent();
        Log.debug(30, "file path in DataPkcagPlugin. parser() "+ path);
        int position = name.lastIndexOf(DOT);
        if(position != -1)
        {
          fileName = path+File.separator+name.substring(0, position);
          extension = name.substring(position);
        }
        else
        {
          fileName = path+File.separator+name;
          extension = "";
        }
      }
    }
    
    /**
     * Gets file name part
     * @return
     */
    public String getFileName()
    {
      return this.fileName;
    }
    
    /**
     * Gets the extension part
     * @return
     */
    public String getExtension()
    {
      return this.extension;
    }
  }
}//DataPackagePlugin
