/**
 *  '$RCSfile: SaveQueryCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-10-23 22:48:21 $'
 * '$Revision: 1.11 $'
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.Action;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * Class to handle savequery command
 */
public class SaveQueryCommand implements Command 
{
  
  /** A reference to the Morpho application */
  private Morpho morpho = null;
  
  /** A static hash listing all of the search menu query Actions by id */
  private static Hashtable<String, GUIAction> savedQueriesList = null;
  
  /** A reference to the MorphoFrame */
  private MorphoFrame morphoFrame = null;
  
  // Start index for saved query in search menu
  private static final int STARTINDEXFORSAVEDQUERY = 6; 
  
  /**
   * Constructor of SearcCommand
   * @param morpho the morpho application which the savequery command will apply
   */
  public SaveQueryCommand(Morpho morpho)
  {
    this.morpho = morpho;
  }//SearchCommand
  
  
  
  /**
   * execute savequery command
   */    
  public void execute(ActionEvent event)
  {
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    // make sure the morphoFrame is not null
    if ( morphoFrame == null)
    {
       Log.debug(5, "Morpho frame was null so I could refresh it!");
    }//if
    
     // Make sure the main panel is result panel
    Component comp = morphoFrame.getContentComponent();
    Query query = null;
    if (comp != null && comp instanceof ResultPanel)
    {
      morphoFrame.setBusy(true);
      ResultPanel resultPane = (ResultPanel) comp;
      query = resultPane.getResultSet().getQuery();
     
      // Serialize the query in the profiles directory
      
      String identifier = query.getIdentifier();
      if (identifier == null) {
    	  // get a local identifier
        identifier = DataStoreServiceController.getInstance().generateIdentifier(null, DataPackageInterface.LOCAL);
        query.setIdentifier(identifier);
      }
      
      try 
      {
        Log.debug(10, "Saving query to disk...");
        Morpho.thisStaticInstance.getLocalDataStoreService().saveQuery(query);
        Log.debug(10, "Adding query to menu...");
        addQueryToMenu(query);
        morphoFrame.setBusy(false);
      }
      catch (IOException ioe) 
      {
        Log.debug(6, "Failed to save query: I/O error.");
      }
    }//if
  }//execute

  /**
   * Add a new menu item to the Search menu for the query
   *
   * @param query the query to be added to the Search menu
   */
  private static void addQueryToMenu(final Query query)
  {
    int index = STARTINDEXFORSAVEDQUERY;// Start index for saved query in search menu
    // See if the query list is null, and initialize it if so
    if (savedQueriesList == null) {
      savedQueriesList = new Hashtable<String, GUIAction>();
    }

    // Add a menu item in the UIController to execute this query, but only
    // if the menu item doesn't already exist, which is determined
    // by seeing if the query identifier is in the static list of queries
    if (! savedQueriesList.containsKey(query.getIdentifier())) {
      // Create a RunSaveQueryCommand
      RunSavedQueryCommand command = new RunSavedQueryCommand(query);
      // Create a GUIAction to run saved query
      GUIAction savedSearchItemAction = 
                new GUIAction(query.getQueryTitle(), null,command);
      savedSearchItemAction.setMenu(QueryPlugin.SEARCH_MENU_LABEL, QueryPlugin.SEARCHMENUPOSITION);  
      savedSearchItemAction.setMenuItemPosition(index);  
      savedSearchItemAction.setToolTipText("Execute saved search");
      UIController.getInstance().addGuiAction(savedSearchItemAction);
      savedQueriesList.put(query.getIdentifier(), savedSearchItemAction);
      index++;
    } else {
      // The menu already exists, so update its title and query object
      GUIAction savedQueryAction = 
             (GUIAction)savedQueriesList.get(query.getIdentifier());
      // Upate query
      if (savedQueryAction.getCommand() instanceof RunSavedQueryCommand)
      {
        RunSavedQueryCommand queryCommand = 
                (RunSavedQueryCommand) savedQueryAction.getCommand();
        queryCommand.setQuery(query);
        savedQueryAction.setCommand(queryCommand);
        // Update query title
        savedQueryAction.putValue(Action.NAME, query.getQueryTitle());
      }
        //savedQueryAction.putValue("SAVED_QUERY_OBJ", query);
    }
  }
  
  /**
   * Load the saved queries into the Search menu so that the user can launch
   * any queries they saved from previosu sessions.
   */
  public void loadSavedQueries()
  {
    loadSavedQueries(morpho);
  }
  
  /**
   * Load the saved queries into the Search menu so that the user can launch
   * any queries they saved from previosu sessions.
   * @param myMorpho the Morpho controls the saved queries
   */
  public static void loadSavedQueries(Morpho myMorpho)
  {
    Log.debug(20, "Loading saved queries...");
    // See if the query list is null, and initialize it if so
    if (savedQueriesList == null) {
      savedQueriesList = new Hashtable<String, GUIAction>();
    }

    // Make sure the list is empty (because this may be called when the
    // profile is being switched)
    if (!savedQueriesList.isEmpty()) {
      UIController controller = UIController.getInstance();
      Enumeration<GUIAction> queryActions = savedQueriesList.elements();
      while (queryActions.hasMoreElements()) {
          GUIAction action = queryActions.nextElement();
          controller.removeGuiAction(action);
      }
      savedQueriesList = new Hashtable<String, GUIAction>();
    }

    // Look for saved queries and load any pathquery docs
      List<String> queries = Morpho.thisStaticInstance.getLocalDataStoreService().getQueryIdentifiers();
      for (String queryId: queries) {
    	  try {
	        File queryFile = Morpho.thisStaticInstance.getLocalDataStoreService().openQueryFile(queryId);
	        Reader xml = new InputStreamReader(new FileInputStream(queryFile), Charset.forName("UTF-8"));
	        Query newQuery = new Query(xml, myMorpho);
	        addQueryToMenu(newQuery);
	      } catch (FileNotFoundException fnf) {
	        Log.debug(9, "Poof. The query disappeared.");
	      }
      }
    
    Log.debug(20, "Finished loading saved queries.");
  }
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
