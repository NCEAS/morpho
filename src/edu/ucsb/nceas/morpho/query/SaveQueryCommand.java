/**
 *  '$RCSfile: SaveQueryCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: cjones $'
 *     '$Date: 2002-09-26 01:57:53 $'
 * '$Revision: 1.7 $'
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
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.Hashtable;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JMenuItem;

/**
 * Class to handle savequery command
 */
public class SaveQueryCommand implements Command 
{
  
  /** A reference to the Morpho application */
  private Morpho morpho = null;
  
  /** A static hash listing all of the search menu query Actions by id */
  private static Hashtable savedQueriesList = null;
  
  /** A reference to the MorphoFrame */
  private MorphoFrame morphoFrame = null;
  
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
      if (identifier == null) 
      {
        //identifier = a.getNextId();
        DataPackageInterface dataPackage = null;
        try 
        {
          ServiceController services = ServiceController.getInstance();
          ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
          dataPackage = (DataPackageInterface)provider;
        } 
        catch (ServiceNotHandledException snhe) 
        {
          Log.debug(6, "Error in save query: "+snhe);
        }
        identifier = dataPackage.getNextId(morpho);
        
        query.setIdentifier(identifier);
      }
      
      try 
      {
        Log.debug(10, "Saving query to disk...");
        query.save();
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
    // See if the query list is null, and initialize it if so
    if (savedQueriesList == null) {
      savedQueriesList = new Hashtable();
    }

    // Add a menu item in the UIController to execute this query, but only
    // if the menu item doesn't already exist, which is determined
    // by seeing if the query identifier is in the static list of queries
    if (! savedQueriesList.containsKey(query.getIdentifier())) {
      Action[] menuActions = new Action[1];
      // Create a RunSaveQueryCommand
      RunSavedQueryCommand command = new RunSavedQueryCommand(query);
      // Create a GUIAction to run saved query
      GUIAction savedSearchItemAction = 
                new GUIAction(query.getQueryTitle(), null,command);
     
      savedSearchItemAction.setToolTipText("Execute saved search");
      menuActions[0] = savedSearchItemAction;
      UIController.getInstance().
                              addMenu("Search", new Integer(-1), menuActions);
      savedQueriesList.put(query.getIdentifier(), savedSearchItemAction);
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
      savedQueriesList = new Hashtable();
    }

    // Make sure the list is empty (because this may be called when the
    // profile is being switched)
    if (!savedQueriesList.isEmpty()) {
      // QueryPlugin.NUMBEROFACTIONINSEARCH is exclude the saved quries in 
      // search menu. So the total number in search menu is saved queris size
      // plus QueryPlugin.NUMBEROFACTIONINSEARCH(search, refresh ...)
      int numOfItemInSearch =
                savedQueriesList.size()+QueryPlugin.NUMBEROFACTIONINSEARCH;
      
      for(int i=numOfItemInSearch-1;i>QueryPlugin.NUMBEROFACTIONINSEARCH-1; i--) 
      {
        // Clear the search menu too 
        UIController.getInstance().removeMenuItem("Search", i);
      }
      savedQueriesList = new Hashtable();
    }

    // Look in the profile queries directory and load any pathquery docs
    ConfigXML config = myMorpho.getConfiguration();
    ConfigXML profile = myMorpho.getProfile();
    String queriesDirName = config.getConfigDirectory() + File.separator +
                            config.get("profile_directory", 0) +
                            File.separator +
                            config.get("current_profile", 0) +
                            File.separator +
                            profile.get("queriesdir", 0);
   
    File queriesDir = new File(queriesDirName);
    if (queriesDir.exists()) {
//DFH      File[] queriesList = queriesDir.listFiles();
      File[] queriesList = listFiles(queriesDir);
      for (int n=0; n < queriesList.length; n++) {
        File queryFile = queriesList[n];
        if (queryFile.isFile()) {
          try {
            FileReader xml = new FileReader(queryFile);
            Query newQuery = new Query(xml, myMorpho);
            addQueryToMenu(newQuery);
          } catch (FileNotFoundException fnf) {
            Log.debug(9, "Poof. The query disappeared.");
          }
        }
      }
    }
    Log.debug(20, "Finished loading saved queries.");
  }
  
  /** List the file in a dir */
  private static File[] listFiles(File dir) 
  {
    String[] fileStrings = dir.list();
    int len = fileStrings.length;
    File[] list = new File[len];
    for (int i=0; i<len; i++) 
    {
        list[i] = new File(dir, fileStrings[i]);    
    }
    return list;
  }
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
