/**
 *  '$RCSfile: QueryPlugin.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2005-07-11 18:04:26 $'
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

package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.framework.ConnectionListener;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.ColumnSortableTableModel;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.UISettings;

import javax.swing.ImageIcon;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15


public class QueryPlugin implements PluginInterface, ConnectionListener,
                                    ServiceProvider, QueryRefreshInterface
{

    /**
     *Import Language into Morpho
     *by pstango 2010/03/15 
     */
  public static Language lan = new Language();
	
    /** Constant int for Window menu position */
    public static final int SEARCHMENUPOSITION = 20;
    
    /** Constant String for Window menu label */
    public static final String SEARCH_MENU_LABEL = /*"Search"*/ lan.getMessages("Search");
	
	/** A reference to the container framework */
    private static Morpho morpho = null;

  /**
   * Construct the query plugin.  Initialize our one tab for the
   * plugin plus any menus and toolbars.
   */
  public QueryPlugin() {}


  /**
   * The plugin must store a reference to the Morpho in order to be able to call
   * the services available through the framework. This is also the time to
   * register menus and toolbars with the framework.
   *
   * @param morpho Morpho
   */
  public void initialize(Morpho morpho)
  {
    this.morpho = morpho;

    // Create the menus and toolbar actions, will register later
    initializeActions();

    // Register Services
    try {
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
    GUIAction searchItemAction = new GUIAction(/*"Search..."*/ lan.getMessages("Search"),
                                               UISettings.SEARCH_ICON,
                                               new SearchCommand(null, morpho));
    searchItemAction.setToolTipText("Search for data");
    searchItemAction.setMenuItemPosition(0);
    searchItemAction.setMenu(SEARCH_MENU_LABEL, SEARCHMENUPOSITION);
    searchItemAction.setToolbarPosition(4);
    controller.addGuiAction(searchItemAction);

    //searchItemAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    // Action for refresh
    RefreshCommand refreshCommand = new RefreshCommand();
    GUIAction refreshItemAction
            = new GUIAction(/*"Refresh..."*/ lan.getMessages("Refresh"),
            		UISettings.REFRESH_ICON, refreshCommand);
    refreshItemAction.setToolTipText("Refresh");
    refreshItemAction.setMenuItemPosition(1);
    refreshItemAction.setMenu(SEARCH_MENU_LABEL, SEARCHMENUPOSITION);
    refreshItemAction.setToolbarPosition(5);
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
    GUIAction saveQueryItemAction
              = new GUIAction(/*"Save Search"*/ lan.getMessages("SaveSearch"),
            		  UISettings.SAVE_QUERY_ICON, saveCommand);
    saveQueryItemAction.setToolTipText("Save search");
    saveQueryItemAction.setMenuItemPosition(2);
    saveQueryItemAction.setMenu(SEARCH_MENU_LABEL, SEARCHMENUPOSITION);
    saveQueryItemAction.setToolbarPosition(6);
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
    GUIAction reviseSearchItemAction = new GUIAction(/*"Revise Search"*/ lan.getMessages("ReviseSearch"), 
    		null, new ReviseSearchCommand(morpho));
    reviseSearchItemAction.setSmallIcon(new ImageIcon(getClass().
           getResource("revisesearch16.gif")));
    reviseSearchItemAction.setToolTipText("Revise search");
    reviseSearchItemAction.setMenuItemPosition(3);
    reviseSearchItemAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    reviseSearchItemAction.setMenu(SEARCH_MENU_LABEL, SEARCHMENUPOSITION);
    reviseSearchItemAction.setToolbarPosition(7);
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
    GUIAction openDialogBoxAction = new GUIAction(/*"Open..."*/ lan.getMessages("Open"),
                                              UISettings.OPEN_DATAPACKAGE_ICON,
                                              new OpenDialogBoxCommand(morpho));
    openDialogBoxAction.setMenuItemPosition(2);
    openDialogBoxAction.setToolTipText("Open...");
    openDialogBoxAction.setMenu(Morpho.FILE_MENU_LABEL, Morpho.FILEMENUPOSITION);
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
    GUIAction openPreviousAction = new GUIAction(/*"Open Previous Version"*/ lan.getMessages("OpenPreviousVersion"),
    		null, new OpenPreviousVersionCommand(null, morpho));
    openPreviousAction.setMenuItemPosition(3);
    openPreviousAction.setToolTipText("Open a previous version...");
    openPreviousAction.setMenu(Morpho.FILE_MENU_LABEL, Morpho.FILEMENUPOSITION);
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
    GUIAction synchronizeAction = new GUIAction(/*"Synchronize..."*/ lan.getMessages("Synchronize"), 
    		null, new OpenSynchronizeDialogCommand());
    synchronizeAction.setMenuItemPosition(14);
    synchronizeAction.setToolTipText("Synchronize...");
    synchronizeAction.setSeparatorPosition(Morpho.SEPARATOR_FOLLOWING);
    synchronizeAction.setMenu(Morpho.FILE_MENU_LABEL, Morpho.FILEMENUPOSITION);
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
    GUIAction deleteDialogAction = new GUIAction(/*"Delete..."*/ lan.getMessages("Delete"),
    		null, new OpenDeleteDialogCommand());
    deleteDialogAction.setMenuItemPosition(13);
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
    deleteDialogAction.setMenu(Morpho.FILE_MENU_LABEL, Morpho.FILEMENUPOSITION);

    controller.addGuiAction(deleteDialogAction);

    // Export action
    GUIAction exportAction = new GUIAction(/*"Export..."*/ lan.getMessages("Export"),
    		null,
//                            new ExportCommand(null, ExportCommand.REGULAR));
                             new OpenExportDialogCommand());
    exportAction.setMenuItemPosition(16);
    exportAction.setToolTipText("Export data package...");
    exportAction.setMenu(Morpho.FILE_MENU_LABEL, Morpho.FILEMENUPOSITION);
    exportAction.setEnabledOnStateChange(
                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
                      true, GUIAction.EVENT_LOCAL);
    exportAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_SELECTED,
                      true, GUIAction.EVENT_LOCAL);
    exportAction.setEnabledOnStateChange(
                      StateChangeEvent.SEARCH_RESULT_NONSELECTED,
                      false, GUIAction.EVENT_LOCAL);
    exportAction.setEnabledOnStateChange(
        StateChangeEvent.CHOOSE_COMPLETE_DATAPACKAGE, 
        true, GUIAction.EVENT_LOCAL);
    exportAction.setEnabledOnStateChange(
        StateChangeEvent.CHOOSE_INCOMPLETE_DATAPACKAGE,
        false, GUIAction.EVENT_LOCAL);
    exportAction.setEnabled(false);
    controller.addGuiAction(exportAction);

    // Export to zip action
//    GUIAction exportZipAction = new GUIAction("Export to Zip...", null,
   //                             new ExportCommand(null, ExportCommand.ZIP));
//                             new OpenExportDialogCommand());
//    exportZipAction.setMenuItemPosition(14);
//    exportZipAction.setToolTipText("Export data package into zip file...");
//    exportZipAction.setMenu("File", 0);
//    exportZipAction.setEnabledOnStateChange(
//                      StateChangeEvent.CREATE_DATAPACKAGE_FRAME,
//                      true, GUIAction.EVENT_LOCAL);
//    exportZipAction.setEnabledOnStateChange(
//                      StateChangeEvent.SEARCH_RESULT_SELECTED,
//                      true, GUIAction.EVENT_LOCAL);
//    exportZipAction.setEnabledOnStateChange(
//                      StateChangeEvent.SEARCH_RESULT_NONSELECTED,
//                      false, GUIAction.EVENT_LOCAL);
//    exportZipAction.setEnabled(false);
//    controller.addGuiAction(exportZipAction);

  }


  /**
   * Implement the ConnectionListener interface so we know when to refresh
   * queries.
   *
   * @param newUsername String
   */
  public void usernameChanged(String newUsername)
  {
    Log.debug(20, "New username: " + newUsername);
    refresh();
  }


  /**
   * Implement the ConnectionListener interface so we know when to refresh
   * queries.
   *
   * @param connected boolean
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


  /**
   * return an instance of a Command object, identified by one of the integer
   * constants defined above
   *
   * @param commandIdentifier integer constant identifying the command Options
   *   include:<ul> <li>OPEN_DATAPACKAGE_COMMAND</li> <li>SEARCH_COMMAND</li>
   *   </ul>
   * @throws ClassNotFoundException
   * @return Command
   */
  public Command getCommandObject(int commandIdentifier)
                                                throws ClassNotFoundException
  {
    switch (commandIdentifier) {

        case QueryRefreshInterface.OPEN_DATAPACKAGE_COMMAND:

            return new OpenDialogBoxCommand(morpho);

        case QueryRefreshInterface.SEARCH_COMMAND:

            return new SearchCommand(null, morpho);

        default:
            ClassNotFoundException e
                                = new ClassNotFoundException("command with ID="
                                            +commandIdentifier+" not found");
            e.fillInStackTrace();
            throw e;
    }
  }


  /**
   * performs a local query to get the documents owned by the user, as
   * identified by the current profile
   * @return AbstractTableModel containing results
   * @param headNames the select column names
   * @param deletedDocid  the docid should be delted from the vector
   * (@see edu.ucsb.nceas.morpho.query.ResultSet)
   */
  public ColumnSortableTableModel doOwnerQueryForCurrentUser(String[] headNames,
                                                    String deletedDocid){

    OpenDialogBoxCommand odbCmd = new OpenDialogBoxCommand(morpho);
    Query ownerQuery = new Query(odbCmd.getOwnerQuery(), morpho);
    LocalQuery localOwnerQuery = new LocalQuery(ownerQuery, morpho);
    HeadResultSet originalSet = (HeadResultSet)localOwnerQuery.execute();
    Vector results = originalSet.getResultsVector();
    Vector newResults = new Vector();
    if (deletedDocid != null && !deletedDocid.equals(""))
    {
      //go through the vector
      for (int i=0; i<results.size(); i++)
      {
         Vector rowVector = (Vector)results.elementAt(i);
         String docid =(String) rowVector.elementAt(ResultSet.DOCIDINDEX);
         if (docid != null && docid.equals(deletedDocid))
         {
           continue;
         }//if
         else
         {
           newResults.add(rowVector);
         }//else
      }//for

    }//if
    else
    {
        newResults = results;
    }
    originalSet.setResultsVector(newResults);
    // reset the header name
    originalSet.setHeader(headNames);

    return (ColumnSortableTableModel) originalSet;
  }
  
  
  
  /**
   * List all crashed the document in a dialog box
   * @param parent the parent frame of the dialog box
   */
  public void listCrashedDocument(MorphoFrame parent)
  {
	  //First we will check if there are docs in incomplete dir.
	  FileSystemDataStore fileStore = new FileSystemDataStore(morpho);
	  //if(fileStore.hasIncompleteFile())
	  {
		  OpenDialogBoxCommand odbCmd = new OpenDialogBoxCommand(morpho);
		  Query ownerQuery = new Query(odbCmd.getOwnerQuery(), morpho);
		  OpenCrashedDocDialogBox openBox = new OpenCrashedDocDialogBox(parent, Morpho.thisStaticInstance, ownerQuery);
	  }
  }


}
