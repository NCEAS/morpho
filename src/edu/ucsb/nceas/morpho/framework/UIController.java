/**
 *  '$RCSfile: UIController.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-14 00:17:28 $'
 * '$Revision: 1.1.2.1 $'
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

import edu.ucsb.nceas.morpho.util.Log;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

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

    private static Hashtable windowsRegistry;
    private static Hashtable menuList;

    /**
     * Creates a new instance of UIController, but is private because this
     * is a singleton.
     *
     * @param config  Description of Parameter
     */
    private UIController()
    {
        windowsRegistry = new Hashtable();
        menuList = new Hashtable();
    }

    /**
     * Get the single instance of the UIController, creating it if needed.
     */
    public static UIController getInstance()
    {
        if (controller == null) {
            controller = new UIController();
        }
        return controller;
    }

    /**
     * This method is called by plugins to get a new window that is an
     * instance of MorphoFrame.  They can set the content of the window
     * to a panel of their choice using MorphoFrame.setMainContentPane().
     *
     * @param windowTitle the initial title for the window
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
    }
}
