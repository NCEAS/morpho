/**
 *  '$RCSfile: GUIAction.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-09-02 20:49:47 $'
 * '$Revision: 1.9 $'
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

package edu.ucsb.nceas.morpho.util;

import edu.ucsb.nceas.morpho.framework.UIController;
import javax.swing.Icon;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.Hashtable;

/**
 *  Class       GUIAction
 *  This class extends javax.swing.AbstractAction in order to provide basic
 *  "Action" functionality for the GUI.  Instances of this class are passed 
 *  as arguments to Swing component constructors, in order to facilitate 
 *  encapsulation and sharing of functionality (eg multiple ways of accessing 
 *  the same command, such as from a drop-down menu and from a toolbar button).
 *  The Swing component generally adds this object to itself as 
 *  an ActionListener automatically.
 */
public class GUIAction extends AbstractAction implements StateChangeListener
{

    /** 
     *  Constructor
     *
     *  @param name the display name of this action, as used in menus
     *  @param icon the default icon associated with this action
     *  @param cmd the Command object that will have its execute() method
     *  called by this object's actionPerformed() method
     */
    public GUIAction ( String name, Icon icon, Command cmd ) {
        super(name, icon);
        defaultIcon = icon;
        command=cmd;
        setEnabled(true);
        enabledList = new Hashtable();
        commandList = new Hashtable();
    }

    /** 
     *  gets the default Icon for this action object
     *
     *  @return Icon the default icon for this action object
     */
    public Icon getDefaultIcon(){
        return defaultIcon;
    }
    
    /**
     * sets the text to be displayed as a mouse-over tooltip
     *
     * @param toolTipText the tooltip text to be displayed
     */
    public void setToolTipText(String toolTipText) {
        super.putValue(AbstractAction.SHORT_DESCRIPTION, toolTipText);
    }
    
    /** 
     *  sets the small Icon to be displayed on menus
     *
     *  @param smallIcon the Icon to be displayed on menus etc
     */
    public void setSmallIcon(Icon smallIcon) {
        super.putValue(AbstractAction.SMALL_ICON, smallIcon);
    }
   
    /** 
     *  sets the action pull right submenu
     *
     *  @param flag true of false the action is sub menu
     */
    public void setSubMenu(boolean flag) {
        if (flag)
        {
          super.putValue(UIController.PULL_RIGHT_MENU, UIController.YES);
        }
    }
    
    /** 
     *  sets the action path for sub menu or sub menuitem.
     *  Note: sub menu and sub menuitem for this sub menu has same path
     *  for example In file menu, it has a submenu delete and delete has
     *  3 menuitems - delete local, delete network and delete both.
     *  The path for delete submenu is "file/delete", the path for the 3 
     *  menuitems is "file/delete" too.
     *
     *  @param path the su
     */
    public void setSubMenuPath(String path) {
          super.putValue(UIController.MENU_PATH, path);
     
    }
   
    /** 
     *  Sets the menu and its position for this action
     *
     *  @param menuName the name of the menu in which to embed this action
     *  @param position the position of the menu
     */
    public void setMenu(String menuName, int position) {
        this.menuName = menuName;
        this.menuPosition = position;
    }
    
    /** 
     *  Get the menu name for this action
     *
     * @return the name of the menu for this action
     */
    public String getMenuName() {
        return menuName;
    }
    
    /** 
     *  Get the menu position for this action
     *
     * @return the position of the menu for this action
     */
    public int getMenuPosition() {
        return menuPosition;
    }
    
    /** 
     *  sets the menu item position for this action
     *
     *  @param position the position of memu item
     */
    public void setMenuItemPosition(int position) {
        super.putValue("menuPosition", new Integer(position));
    }
    
    /** 
     *  Get the menu item position for this action
     *
     */
    public int getMenuItemPosition() {
        Integer position = (Integer) super.getValue("menuPosition");
        return position.intValue();
    }
    
    /** 
     *  sets the separator for this action
     *
     *  @param position the position separator of memu item, follow or precding
     */
    public void setSeparatorPosition(String position) {
      super.putValue(DEFAULT, position);
    }
    
    /** 
     *  Get the separator position for this action
     *
     */
    public String getSeparatorPosition() {
        String position = (String) super.getValue(DEFAULT);
        return position;
    }
    
  
    /**
     * actionPerformed() method required by ActionListener interface
     *
     * @param actionEvent the event generated by the component
     */
    public void actionPerformed(ActionEvent actionEvent) {
        command.execute();
    }
    
    /**
     * Get the command in this action object
     */
    public Command getCommand()
    {
      return command;
    }//getCommand

    /**
     * Set the command in this action object
     */
    public void setCommand(Command cmd)
    {
      this.command = cmd;
    }//setCommand

    /**
     * Set the state of this action to the enabled value if a state change
     * matching changedState occurs.
     *
     * @param changedState the name of the state change
     * @param enabled boolean value indicating whether the action should 
     *                be enabled
     */
    public void setEnabledOnStateChange(String changedState, boolean enabled)
    {
        enabledList.put(changedState, new Boolean(enabled));
        StateChangeMonitor.getInstance().addStateChangeListener(changedState,
                (StateChangeListener)this);
    }

    /**
     * Set the state of this action to the enabled value if a state change
     * matching changedState occurs.
     *
     * @param changedState the name of the state change
     * @param command Command that should be set upon a state change
     */
    public void setCommandOnStateChange(String changedState, Command command)
    {
        commandList.put(changedState, command);
        StateChangeMonitor.getInstance().addStateChangeListener(changedState,
                (StateChangeListener)this);
    }

    /**
     * Handle a state change.  This results in a change to the enabled state
     * of the action, or a change in the command, depending on which 
     * state changes have been registered for this action previously.
     *
     * @param event the StateChangeEvent that has occurred
     */
    public void handleStateChange(StateChangeEvent event)
    {
        String changedState = event.getChangedState();

        // Check and handle the enabled state changes
        if (enabledList.containsKey(changedState)) {
            boolean enabled = 
                ((Boolean)enabledList.get(changedState)).booleanValue();
            setEnabled(enabled);
        }

        // Check and handle the command state changes
        if (commandList.containsKey(changedState)) {
            Command newCommand = (Command)commandList.get(changedState);
            setCommand(newCommand);
        }
    }

// * * *  V A R I A B L E S  * * *

    private Command command;
    private Icon defaultIcon;
    private String menuName;
    private int menuPosition;
    private Hashtable enabledList; 
    private Hashtable commandList; 
}
