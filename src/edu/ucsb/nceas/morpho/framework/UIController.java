/**
 *  '$RCSfile: UIController.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-14 21:45:57 $'
 * '$Revision: 1.1.2.2 $'
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

    private static Hashtable windowsRegistry;
    private static Hashtable menuList;
    private static Vector menuOrder;

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
        windowsRegistry = new Hashtable();
        menuList = new Hashtable();
        menuOrder = new Vector();
    }

    /**
     * Get the single instance of the UIController, creating it if needed.
     */
    public static UIController getInstance(Morpho morpho)
    {
        if (controller == null) {
            controller = new UIController(morpho);
        }
        return controller;
    }

    /**
     * Get the single instance of the UIController, creating it if needed.
     */
    public static UIController getInstance()
    {
        if (controller == null) {
            controller = new UIController(null);
        }
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
        MorphoFrame window = new MorphoFrame();
        window.setTitle(title);
        Action windowAction = new AbstractAction(title) {
            public void actionPerformed(ActionEvent e) {
                JMenuItem source = (JMenuItem)e.getSource();
                MorphoFrame window1 = (MorphoFrame)windowsRegistry.get(source);
                window1.toFront();
            }
        };
        windowAction.putValue(Action.SHORT_DESCRIPTION, "Select Window");
        JMenu windowMenu = (JMenu)menuList.get("Window");
        if (windowMenu != null) {
            JMenuItem windowMenuItem = windowMenu.add(windowAction);
            windowsRegistry.put(windowMenuItem, window);
        }

        window.setMenuBar(createMenuBar());
        //updateMenuBar();

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
        Log.debug(20, "Removing window.");
        JMenuItem menuItem = null;
        JMenu windowMenu = (JMenu)menuList.get("Window");
        Enumeration keys = windowsRegistry.keys();
        while (keys.hasMoreElements()) {
            menuItem = (JMenuItem)keys.nextElement();
            MorphoFrame savedWindow = 
                (MorphoFrame)windowsRegistry.get(menuItem);
            if (savedWindow == window) {
                break;
            } else {
                menuItem = null;
            }
        } 
        
        try {
            windowMenu.remove(menuItem);
        } catch(NullPointerException npe) {
            Log.debug(20, "Window already removed from menu.");
        }
        
        try {
            windowsRegistry.remove(menuItem);
        } catch(NullPointerException npe2) {
            Log.debug(20, "Window already removed from registry.");
        }

        // Exit application if no windows remain
        if (windowsRegistry.isEmpty()) {
            morpho.exitApplication();
        }
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
        JMenu currentMenu = null;
        // Check if the menu exists already here, otherwise create it
        if (menuList.containsKey(menuName)) {
            currentMenu = (JMenu)menuList.get(menuName);
        } else {
            currentMenu = new JMenu(); 
            currentMenu.setText(menuName);
            currentMenu.setActionCommand(menuName);
            menuList.put(menuName, currentMenu);
            if (menuPosition.intValue() < menuOrder.size()) {
                menuOrder.insertElementAt(currentMenu,menuPosition.intValue());
            } else {
                menuOrder.addElement(currentMenu);
            }

            updateMenuBar();
        }
    
        // Get the menu items (Actions) and add them to the menu
        if (menuActions != null) {
            for (int j=0; j < menuActions.length; j++) {
                Action currentAction = menuActions[j];
                JMenuItem currentItem = null;
                String hasDefaultSep = 
                    (String)currentAction.getValue(Action.DEFAULT);
                Integer itemPosition = 
                    (Integer)currentAction.getValue("menuPosition");
                int menuPos = 
                    (itemPosition != null) ? itemPosition.intValue() : -1;
        
                if (menuPos >= 0) {
                    // Insert menus at the specified position
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
    
    /**
     * This method is called by plugins to remove a menu item from
     * a menu based on the index of the menu item.
     *
     * @param menuName the name of the menu from which to remove the item
     * @param index the  position of the menu item to remove
     */
    public void removeMenuItem(String menuName, int index)
    {
        JMenu currentMenu = null;
        // Check if the menu exists, and if so, remove the item
        if (menuList.containsKey(menuName)) {
            currentMenu = (JMenu)menuList.get(menuName);
            Log.debug(20, "Removing menu item: " + menuName + 
                    " (" + index + ")");
            currentMenu.remove(index);
        }
    }

    /**
     * Update the mennu bar by rebuilding it when a new menu is added
     * to the list.
     */
    private void updateMenuBar()
    {
        // Notify all of the windows of the changed menubar
        Enumeration windows = windowsRegistry.elements();
        while (windows.hasMoreElements()) {
            JMenuBar newMenuBar = createMenuBar();
            MorphoFrame currentWindow = (MorphoFrame)windows.nextElement();
            currentWindow.setMenuBar(newMenuBar);
            Log.debug(50, "Updated menu for window: " + 
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
        for (int j=0; j < menuOrder.size(); j++) {
            JMenu currentMenu = (JMenu)menuOrder.get(j);
            newMenuBar.add(currentMenu);
        }
        return newMenuBar;
    }
}
