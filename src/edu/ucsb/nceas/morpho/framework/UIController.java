/**
 *  '$RCSfile: UIController.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-26 21:44:39 $'
 * '$Revision: 1.19 $'
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

package edu.ucsb.nceas.morpho.framework;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.Container;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * The UIController handles the state of the morpho menu and toolbars so
 * that they are synchronized across the various plugins.  This is a 
 * singleton class because only one instance is ever needed.  Plugins that need
 * a new frame for a window should call UIController.newWindow().  The single 
 * instance of UIController can be obtained statically using getInstance().
 *
 * @author   jones
 */
public class UIController
{
    private static UIController controller;
    private static Morpho morpho;
    private static MorphoFrame currentActiveWindow;

    /** A hash of cloned GUIActions keyed on the original GUIAction */
    private static Hashtable guiActionClones;

    /** A hash of windows keyed on its Window menu GUIAction */
    private static Hashtable windowList;

    /** A hash of windows keyed on an associated GUIAction clone */
    private static Hashtable actionCloneWindowAssociation;

    private static Vector orderedMenuList;
    private static Hashtable orderedMenuActions;
    private static Vector toolbarList;
    // A hashtable to store the pair: submenu-path, 
    // such as synchronize - file/synchroize
    private static Hashtable subMenuAndPath;
    private static int count = 0; // count create how many frames

    // Constants
    public static final String SEPARATOR_PRECEDING = "separator_preceding";
    public static final String SEPARATOR_FOLLOWING = "separator_following";
    public static final String PULL_RIGHT_MENU = "pull_right_menu";
    public static final String YES = "yes";
    public static final String MENU_PATH = "menu_path";

    /**
     * Creates a new instance of UIController, but is private because this
     * is a singleton.
     */
    private UIController(Morpho morpho)
    {
        this.morpho = morpho;
        windowList = new Hashtable();
        actionCloneWindowAssociation = new Hashtable();
        guiActionClones = new Hashtable();

        orderedMenuList = new Vector();
        orderedMenuActions = new Hashtable();
        toolbarList = new Vector();
        subMenuAndPath = new Hashtable();
    }

    /**
     * Initialize the single instance of the UIController, 
     * creating it if needed.
     */
    public static UIController initialize(Morpho morpho)
    {
        if (controller == null) {
            controller = new UIController(morpho);
        }
        return controller;
    }

    /**
     * Get the single instance of the UIController, creating it if needed.
     * If the controller has not yet been created yet (using the 
     * method "initialize(Morpho)", then this method returns null.
     *
     * @returns the single instance of the UIController
     */
    public static UIController getInstance()
    {
        return controller;
    }

    /**
     * This method is called by plugins to get a new window that is an
     * instance of MorphoFrame.  They can set the content of the window
     * to a panel of their choice using MorphoFrame.setMainContentPane().
     *
     * @param windowName the initial title for the window
     */
    public MorphoFrame addWindow(String windowName)
    {
        String title = "Untitled";
        if (windowName != null) {
            title = windowName;
        }
        Log.debug(30, "Adding window: " + title);
        MorphoFrame window = MorphoFrame.getInstance();
        window.setTitle(title);

        registerWindow(window);

        updateStatusBar(window.getStatusBar());
        
        // If initial  window morpho in the window list, remove it
        if ( count == 1)// create the second frame
        {
          Enumeration frameList = windowList.elements();
          while (frameList.hasMoreElements())
          {
            MorphoFrame frame = (MorphoFrame)frameList.nextElement();
            if ((frame.getTitle()).equals(Morpho.INITIALFRAMENAME))
            {
              removeWindow(frame);
              frame.dispose();
              frame = null;
            }
          }
        }
        
        if (getCurrentActiveWindow()==null) {
            setCurrentActiveWindow(window);
        }
      
        count++;
        return window;
    }
    
    /**
     * This method is called by plugins to update window that is an
     * instance of MorphoFrame.  
     *
     * @param window the window will be given new name
     * @param title the new name will give to window
     */
    public void updateWindow(MorphoFrame window, String title)
    { 
      // Get title from old frame
      String oldTitle = window.getTitle();
      if ((title == null) || (title.equals(oldTitle))) {
          return;
      }
      
      // Remove the frame from window list
      removeWindow(window);
      // Set frame new title
      window.setTitle(title);
      
      registerWindow(window);

      if (getCurrentActiveWindow()==null) {
          setCurrentActiveWindow(window);
      }
    }
      
    /**
     * This method is called to de-register a Window that
     * the plugin has created.  The window is removed from the "Windows"
     * menu.
     *
     * @param window the window to be removed from the framework
     */
    public void removeWindow(MorphoFrame window)
    {
        Log.debug(50, "Removing window.");

        // Look up the Action for this window
        GUIAction currentAction = null;
        Enumeration keys = windowList.keys();
        while (keys.hasMoreElements()) {
            currentAction = (GUIAction)keys.nextElement();
            MorphoFrame savedWindow = 
                (MorphoFrame)windowList.get(currentAction);
            if (savedWindow == window) {
                break;
            } else {
                currentAction = null;
            }
        } 
        
        // Remove the action from the GUIAction list
        removeGuiAction(currentAction);
        
        // If the window is the currentActiveWindow, change it
        if (getCurrentActiveWindow()==window) {
            setCurrentActiveWindow(null);
        }

        // Remove the window from the windowList
        try {
            windowList.remove(currentAction);
        } catch(NullPointerException npe2) {
            Log.debug(20, "Window already removed from registry.");
        }

        // Exit application if no windows remain
        if (windowList.isEmpty()) {
            morpho.exitApplication();
        }
    }
/*
    public void addGuiAction(GUIAction action)
    {
        // for each window, clone the action, add it to the vector of
        // clones for this GUIAction, add it to the clone/window
        // association hash, and send the clone to the window
        Vector cloneList = new Vector();
        guiActionClones.put(action, cloneList);
        Enumeration windows = windowList.elements();
        while (windows.hasMoreElements()) {
            MorphoFrame window = (MorphoFrame)windows.nextElement();
            GUIAction clone = action.cloneAction();
            cloneList.addElement(clone);
            actionCloneWindowAssociation.put(clone, window);
            window.addGuiAction(clone);
        }
    }

    public void removeGuiAction(GUIAction action)
    {
        Vector cloneList = (Vector)guiActionClones.get(action);
        guiActionClones.remove(action);
        for (int i=0; i< cloneList.size(); i++) {
            GUIAction clone = (GUIAction)cloneList.elementAt(i);
            MorphoFrame window = 
                (MorphoFrame)actionCloneWindowAssociation.get(clone);
            window.removeGuiAction(clone);
        }
    }
*/

    /**
     * Add a menu item and optionally a toolbar button by registering
     * an instance of GUIAction.  The GUIAction will be cloned once for
     * each of the MorphoFrame instances that currently exist or that are
     * created.
     *
     * @param action the GUIAction instance describing the menu item
     */
    public void addGuiAction(GUIAction action)
    {
        // for each window, clone the action, add it to the vector of
        // clones for this GUIAction, add it to the clone/window
        // association hash, and send the clone to the window
        Vector cloneList = new Vector();
        guiActionClones.put(action, cloneList);
        Enumeration windows = windowList.elements();
        while (windows.hasMoreElements()) {
            MorphoFrame window = (MorphoFrame)windows.nextElement();
            GUIAction clone = action.cloneAction();
            cloneList.addElement(clone);
            actionCloneWindowAssociation.put(clone, window);
            window.addGuiAction(clone);
        }
    }

    /**
     * Remove a menu item and its toolbar button for a particular
     * instance of GUIAction.  
     *
     * @param action the GUIAction instance to be removed
     */
    public void removeGuiAction(GUIAction action)
    {
        Vector cloneList = (Vector)guiActionClones.get(action);
        guiActionClones.remove(action);
        for (int i=0; i< cloneList.size(); i++) {
            GUIAction clone = (GUIAction)cloneList.elementAt(i);
            MorphoFrame window = getMorphoFrameContainingGUIAction(clone);
            window.removeGuiAction(clone);
        }
    }
    
    /**
     * return the MorphoFrame that contains the GUIAction provided
     *
     * @param action      the GUIAction for which the parent MorphoFrame will be 
     *                    returned
     * @return            the MorphoFrame which is the parent of this GUIAction
     */
    public static MorphoFrame getMorphoFrameContainingGUIAction(GUIAction action) 
    {
        if (action==null || actionCloneWindowAssociation==null) return null;
        return (MorphoFrame)actionCloneWindowAssociation.get(action);
    }

    /**
     * refresh all visible the windows that are in the windowList.
     */
    public void refreshWindows()
    {
        Enumeration windows = windowList.elements();
        while (windows.hasMoreElements()) {
            ((MorphoFrame)windows.nextElement()).validate();
        }
    }

    /**
     * updates status bar in response to changes in connection parameters
     */
    public void updateAllStatusBars()
    {
        Enumeration windows = windowList.elements();
        while (windows.hasMoreElements()) {
            StatusBar statusBar = 
                ((MorphoFrame)windows.nextElement()).getStatusBar();
            updateStatusBar(statusBar);
        }
    }

    /**
     * set currently active window
     *
     *  @param window the currently active MorphoFrame window
     */
    public void setCurrentActiveWindow(MorphoFrame window)
    {
        currentActiveWindow = window;
    }
    
    /**
     * get currently active window
     *
     *  @return  the currently active MorphoFrame window
     */
    public MorphoFrame getCurrentActiveWindow()
    {
        return currentActiveWindow;
    }
    
    /**
     * get Morpho
     */
    public static Morpho getMorpho()
    {
      return morpho;
    }
    /**
     * Register a window by creating an action and adding it to the
     * list of windows for the application. All existing windows are
     * updated with new menus that reflect the menu change.
     *
     * @param window the window which should be added
     */
    private void registerWindow(MorphoFrame window)
    {
        if (window == null) {
          Log.debug(50, "Window is null, create failed!");
        }
        // clone all of the existing GUIActions for this new window
        Enumeration actionList = guiActionClones.keys();
        while (actionList.hasMoreElements()) {
            GUIAction action = (GUIAction)actionList.nextElement();
            Log.debug(50, "Cloning action: " + action.toString());
            GUIAction clone = action.cloneAction();
            Vector cloneList = (Vector)guiActionClones.get(action);
            cloneList.addElement(clone);
            actionCloneWindowAssociation.put(clone, window);
            Log.debug(50, "Clone menu name is: " + clone.getMenuName());
            window.addGuiAction(clone);
        }

        // add a new menu item for this window by greating a GUIAction for it
        String title = window.getTitle();
        Command command = new Command() {
            public void execute(ActionEvent e) {
                JMenuItem source = (JMenuItem)e.getSource();
                Action firedAction = source.getAction();
                GUIAction original = 
                    ((GUIAction)firedAction).getOriginalAction();
                MorphoFrame window1 = 
                    (MorphoFrame)windowList.get(original);
                window1.toFront();
            }
        };
        GUIAction action = new GUIAction(title, null, command);
        action.setMenu("Window", 4);
        action.setToolTipText("Select Window");
        windowList.put(action, window);
        addGuiAction(action);
    }

    /**
     * Updates a single StatusBar to reflect the current network state
     *
     * @param statusBar the status bar to be updated
     */
    private void updateStatusBar(StatusBar statusBar)
    {
        statusBar.setConnectStatus(morpho.getNetworkStatus());
        statusBar.setLoginStatus(morpho.isConnected() && 
                morpho.getNetworkStatus());
        statusBar.setSSLStatus(morpho.getSslStatus());
    }

    /**
     * Create a new menubar for use in a window
     */
    private static JMenuBar createMenuBar()
    {
        /*
        Log.debug(50, "Creating menu bar for window...");
        JMenuBar newMenuBar = new JMenuBar();
        for (int j=0; j < orderedMenuList.size(); j++) {
            String menuName = (String)orderedMenuList.get(j);
            JMenu currentMenu = new JMenu(menuName);
            newMenuBar.add(currentMenu);

            // Add all of the actions for this menu from its Vector
            createMenuItems(menuName, currentMenu);
        }
        return newMenuBar;
        */
        return null;
    }

    /**
     * Create new menu items for a particular menu
     */
    private static void createMenuItems(String menuName, JMenu currentMenu)
    {
        Vector currentActions = (Vector)orderedMenuActions.get(menuName);
        registerActionToMenu(currentMenu, currentActions); 
        Log.debug(50, "Creating menu items for: " + menuName + " (" +
                currentActions.size() + " actions)");
       
    }
    
     /**
     * Create new menu items for a particular menu
     */
    private static void createMenuItemsCopy(String menuName, JMenu currentMenu)
    {
        Vector currentActions = (Vector)orderedMenuActions.get(menuName);
        Log.debug(20, "Creating menu items for: " + menuName + " (" +
                currentActions.size() + " actions)");
        for (int j=0; j < currentActions.size(); j++) {
            Action currentAction = (Action)currentActions.elementAt(j);

            JMenuItem currentItem = null;
            String hasDefaultSep = 
            (String)currentAction.getValue(Action.DEFAULT);
            Integer itemPosition = 
                (Integer)currentAction.getValue("menuPosition");
            int menuPos = 
                (itemPosition != null) ? itemPosition.intValue() : -1;

            menuPos = -1; 
            if (menuPos >= 0) {
                // Insert menus at the specified position
                Log.debug(50, "Inserting Action as menu item.");
                int menuCount = currentMenu.getMenuComponentCount();
                if (menuPos > menuCount) {
                    menuPos = menuCount;
                }
            
                if (hasDefaultSep != null &&
                    hasDefaultSep.equals(SEPARATOR_PRECEDING)) {
                    currentMenu.insertSeparator(menuPos++);
                }
                currentItem = currentMenu.insert(currentAction, menuPos);
                currentItem.setAccelerator(
                    (KeyStroke)currentAction.getValue(
                    Action.ACCELERATOR_KEY));
                if (hasDefaultSep != null &&
                    hasDefaultSep.equals(SEPARATOR_FOLLOWING)) {
                    menuPos++;
                    currentMenu.insertSeparator(menuPos);
                }
            } else {
                // Append everything else at the bottom of the menu
                Log.debug(50, "Appending Action as menu item.");
                if (hasDefaultSep != null &&
                    hasDefaultSep.equals(SEPARATOR_PRECEDING)) {
                    currentMenu.addSeparator();
                }
                currentItem = currentMenu.add(currentAction);
                currentItem.setAccelerator(
                    (KeyStroke)currentAction.getValue(
                    Action.ACCELERATOR_KEY));
                if (hasDefaultSep != null &&
                    hasDefaultSep.equals(SEPARATOR_FOLLOWING)) {
                    currentMenu.addSeparator();
                }
            }
        }
    }
    
    /**
     * Register a array actions to a menu. This method using recursion to handle
     * pull right submenu.
     */
    private static void registerActionToMenu(JMenu currentMenu, Vector actions)
    {
      boolean pullRightMenuFlag = false;// flag for a pull right menu
      JMenu currentPullRightMenu = null;// for a pull right menu
      Vector subMenuActions = null; // Store the actions for submenu
      JMenuItem currentItem = null;// for a menuitem if it is not a pull menu
      // Make sure the meun and vector is valid
      if (currentMenu == null || actions.size() == 0)
      {
        return;
      }
      for (int j=0; j < actions.size(); j++) 
      {
        Action currentAction = (Action)actions.elementAt(j);
         // To check the action if it is a pull right menu
        String pullRightMenu =(String)currentAction.getValue(PULL_RIGHT_MENU);
        if (pullRightMenu !=null && pullRightMenu.equals(YES))
        {
          Log.debug(50, "in submenu ");
          // Get the action's path
          String pullRightMenuPath = (String)currentAction.getValue(MENU_PATH);
          Log.debug(50, "Pull right Menu path: "+pullRightMenuPath);
          // check the sub menu path if it exsit
          /*if (pullRightMenuPath == null || pullRightMenuPath.equals("") ||
                            isSubMenuPathExisted(pullRightMenuPath))
          {
            continue;
          }*/
          pullRightMenuFlag = true;
          // This is pull right menu and create a new JMenu
          currentPullRightMenu = new JMenu(currentAction);
          // initialize subMenuActions
          subMenuActions = new Vector();
          // Get every subactions for this menu, the subaction menu path
          // should contains the jmenu path
          for ( int i =0; i< actions.size(); i++)
          {
            Action current = (Action)actions.elementAt(i);
            String actionMenuPath = (String)current.getValue(MENU_PATH);
            // If action path start will pull right menu path
            // this mean this action is a subaction of this JMenu
            if (actionMenuPath!=null && current!=currentAction &&
                                  actionMenuPath.startsWith(pullRightMenuPath))
            {
                 Log.debug(50, "Action path : "+actionMenuPath);
                 subMenuActions.add(current);
            }//if
          }//for
          // if it is new, register the subMenu and path
          subMenuAndPath.put(currentPullRightMenu, pullRightMenuPath);
       }//if
       else
       {
         // Handle MenuItem
         // Get the path the action
         // Get the action's path
         String actionPath = (String)currentAction.getValue(MENU_PATH);
         // Get the path of the JMenu - parameter of this mehtod
         String paramterMenuPath = (String)subMenuAndPath.get(currentMenu);
         // If MenuItem's path is not equals the menu's path, this means
         // this menu item is not add to this menu skip it.
         if (paramterMenuPath != null && actionPath != null &&
              !paramterMenuPath.equals(actionPath))
         {
           continue;
         }//if
       }//else
          
       String hasDefaultSep = (String)currentAction.getValue(Action.DEFAULT);
       Integer itemPosition = (Integer)currentAction.getValue("menuPosition");
      
       int menuPos = (itemPosition != null) ? itemPosition.intValue() : -1;
      
       //menuPos = -1; 
      
       if (menuPos >= 0) 
       {
         // Insert menus at the specified position
         Log.debug(50, "Inserting Action as menu item.");
         int menuCount = currentMenu.getMenuComponentCount();
         if (menuPos > menuCount) {
            menuPos = menuCount;
         }
         if (hasDefaultSep != null && hasDefaultSep.equals(SEPARATOR_PRECEDING)) 
         {
           currentMenu.insertSeparator(menuPos);
           menuPos++;
         }
         // If it is pull right menu recall this method
         if (pullRightMenuFlag)
         {
           registerActionToMenu(currentPullRightMenu, subMenuActions);
           // Add submenu to the menu
           currentItem =currentMenu.insert(currentPullRightMenu, menuPos);
           
         }
         else
         {
           currentItem = currentMenu.insert(currentAction, menuPos);
           currentItem.setAccelerator(
                    (KeyStroke)currentAction.getValue(
                    Action.ACCELERATOR_KEY));
         }
         if (hasDefaultSep != null && hasDefaultSep.equals(SEPARATOR_FOLLOWING)) 
         {
           menuPos++;
           currentMenu.insertSeparator(menuPos);
         }
      } //if
      else
      {
         // Append everything else at the bottom of the menu
         Log.debug(50, "Appending Action as menu item.");
         if (hasDefaultSep != null && hasDefaultSep.equals(SEPARATOR_PRECEDING)) 
         {
           currentMenu.addSeparator();
         }
         if (pullRightMenuFlag)
         {
           registerActionToMenu(currentPullRightMenu, subMenuActions);
           // Add submenu to the menu
           currentItem = currentMenu.add(currentPullRightMenu);
           
         }
         else
         {
           currentItem = currentMenu.add(currentAction);
           currentItem.setAccelerator(
                    (KeyStroke)currentAction.getValue(
                    Action.ACCELERATOR_KEY));
         }
         if (hasDefaultSep != null && hasDefaultSep.equals(SEPARATOR_FOLLOWING)) 
         {
           currentMenu.addSeparator();
         }
      }//else
    }//for
  }//registerActionToMenu
  
   /**
    * Check a sub menu path already in the subMenuAndPath hashtable
    */
   private static boolean isSubMenuPathExisted(String path)
   {
       boolean flag = false;
       Enumeration menuPath = subMenuAndPath.elements();
       while (menuPath.hasMoreElements()) 
       {
            String existedPath = (String)menuPath.nextElement();
            if (existedPath.equals(path))
            {
              flag = true;
              break;
            }//if
       }//if
       return flag;
   }//isSubMenuPathExisted
}
