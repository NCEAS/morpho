/**
 *  '$RCSfile: UIController.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-22 16:34:51 $'
 * '$Revision: 1.4 $'
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
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.Container;
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
    private static Hashtable windowList;
    private static Vector orderedMenuList;
    private static Hashtable orderedMenuActions;
    private static Vector toolbarList;

    // Constants
    public static final String SEPARATOR_PRECEDING = "separator_preceding";
    public static final String SEPARATOR_FOLLOWING = "separator_following";

    /**
     * Creates a new instance of UIController, but is private because this
     * is a singleton.
     */
    private UIController(Morpho morpho)
    {
        this.morpho = morpho;
        windowList = new Hashtable();
        orderedMenuList = new Vector();
        orderedMenuActions = new Hashtable();
        toolbarList = new Vector();
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
        //MorphoFrame window = new MorphoFrame();
        MorphoFrame window = MorphoFrame.getInstance();
        window.setTitle(title);
        Action windowAction = new AbstractAction(title) {
            public void actionPerformed(ActionEvent e) {
                JMenuItem source = (JMenuItem)e.getSource();
                Action firedAction = source.getAction();
                MorphoFrame window1 = (MorphoFrame)windowList.get(firedAction);
                window1.toFront();
            }
        };
        windowAction.putValue(Action.SHORT_DESCRIPTION, "Select Window");
        windowList.put(windowAction, window);
        Vector windowMenuActions = (Vector)orderedMenuActions.get("Window");
        windowMenuActions.addElement(windowAction);

        updateWindowMenus();
        window.addToolbarActions(toolbarList);

        if (getCurrentActiveWindow()==null) setCurrentActiveWindow(window);
        return window;
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
        Action currentAction = null;
        Enumeration keys = windowList.keys();
        while (keys.hasMoreElements()) {
            currentAction = (Action)keys.nextElement();
            MorphoFrame savedWindow = 
                (MorphoFrame)windowList.get(currentAction);
            if (savedWindow == window) {
                break;
            } else {
                currentAction = null;
            }
        } 
        
        // Remove the action from the menu vector in orderedMenuActions
        Vector windowMenuActions = (Vector)orderedMenuActions.get("Window");
        try {
            windowMenuActions.remove(currentAction);
        } catch(NullPointerException npe) {
            Log.debug(20, "Window already removed from menu.");
        }
        
        // If the window is the currentActiveWindow, change it
        if (getCurrentActiveWindow()==window) setCurrentActiveWindow(null);

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

        updateWindowMenus();
    }

    /**
     * This method is called by plugins to register a menu that
     * the plugin wants created, but that currently has no items.
     *
     * @param menuName the name of the menu to be added to the framework
     * @param menuPosition the position of the menu to be added to the framework
     */
    public void addMenu(String menuName, Integer menuPosition)
    {
        addMenu(menuName, menuPosition, null);
    }

    /**
     * This method is called by plugins to register a menu and its
     * associated Actions. If the menu already exists, the actions
     * are added to it.
     *
     * @param menuName the name of the menu to which to add the action
     * @param menuPosition the  position of the menu on the menu bar
     * @param menuActions an array of Actions to be added to the menu
     */
    public void addMenu(String menuName, Integer menuPosition, 
                        Action[] menuActions)
    {
        Vector currentActions = null;
        // Check if the menu exists already here, otherwise create it
        if (orderedMenuList.contains(menuName)) {
            currentActions = (Vector)orderedMenuActions.get(menuName);
        } else {
            currentActions = new Vector();
            orderedMenuActions.put(menuName, currentActions);
            if (menuPosition.intValue() < orderedMenuList.size()) {
                orderedMenuList.insertElementAt(menuName,
                        menuPosition.intValue());
            } else {
                orderedMenuList.addElement(menuName);
            }
        }
    
        // Get the menu items (Actions) and add them to the menu
        if (menuActions != null) {
            for (int j=0; j < menuActions.length; j++) {
                Action currentAction = menuActions[j];
                
                Integer itemPosition = 
                    (Integer)currentAction.getValue("menuPosition");
                int menuPos = 
                    (itemPosition != null) ? itemPosition.intValue() : -1;
        
                if (menuPos >= 0) {
                    // Insert menus at the specified position
                    int menuCount = currentActions.size();
                    if (menuPos > menuCount) {
                        menuPos = menuCount;
                    }
                    currentActions.insertElementAt(currentAction, menuPos);
                } else {
                    // Append everything else at the bottom of the menu
                    currentActions.addElement(currentAction);
                }
            }
        }

        updateWindowMenus();
    }
    
    /**
     * This method is called by plugins to remove a menu item from
     * a menu based on the index of the menu item.
     *
     * @param menuName the name of the menu from which to remove the item
     * @param index the  position of the menu item to remove
     */
    public void removeMenuItem(String menuName, int index)
    {
        Log.debug(50, "Removing menu item: " + menuName + " (" + index + ")");
        // Check if the menu exists, and if so, remove the item
        if (orderedMenuList.contains(menuName)) {
            Vector currentActions = (Vector)orderedMenuActions.get(menuName);
            currentActions.remove(index);
            updateWindowMenus();
        }
    }

    /**
    * This method is called by plugins to register a toolbar Action. 
    *
    * @param toolbarActions an array of Actions to be added to the toolbar
    */
    public void addToolbarActions(Action[] toolbarActions)
    {
        if (toolbarActions != null) {
            for (int j=0; j < toolbarActions.length; j++) {
                Action currentAction = toolbarActions[j];
                toolbarList.add(currentAction);
            }
            updateWindowToolbars();
        }
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
     * Update the menu bar by rebuilding it when a new menu is added
     * to the list.
     */
    private void updateWindowMenus()
    {
        // Notify all of the windows of the changed menubar
        Enumeration windows = windowList.elements();
        while (windows.hasMoreElements()) {
            JMenuBar newMenuBar = createMenuBar();
            MorphoFrame currentWindow = (MorphoFrame)windows.nextElement();
            currentWindow.setMenuBar(newMenuBar);
            Log.debug(50, "Updated menu for window: " + 
                    currentWindow.getTitle());
        }
    }

    /**
     * Update the toolbars for each of the windows when a new action is added.
     */
    private void updateWindowToolbars()
    {
        // Notify all of the windows of the changed toolbar
        Enumeration windows = windowList.elements();
        while (windows.hasMoreElements()) {
            MorphoFrame currentWindow = (MorphoFrame)windows.nextElement();
            currentWindow.addToolbarActions(toolbarList);
            Log.debug(50, "Updated toolbar for window: " + 
                    currentWindow.getTitle());
        }
    }

    /**
     * Create a new menubar for use in a window
     */
    private JMenuBar createMenuBar()
    {
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
    }

    /**
     * Create new menu items for a particular menu
     */
    private void createMenuItems(String menuName, JMenu currentMenu)
    {
        Vector currentActions = (Vector)orderedMenuActions.get(menuName);
        Log.debug(50, "Creating menu items for: " + menuName + " (" +
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
}
