/**
 *  '$RCSfile: GUIAction.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-16 20:11:33 $'
 * '$Revision: 1.22 $'
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

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;

import java.util.Enumeration;
import java.util.Hashtable;

import java.awt.Component;
import java.awt.MenuComponent;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

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
     *  Public constant to denote that a GUIAction should consume events
     *  originating only within the same frame as the GUIAction itself
     */
     public static final int EVENT_LOCAL = 100;

    /**
     *  Public constant to denote that a GUIAction should consume events
     *  originating from any frame, not just its own
     */
     public static final int EVENT_GLOBAL = 200;


     private static final String ROLLOVER_SMALL_ICON = "rolloverSmallIcon";
     private static final String ROLLOVER_TEXT_LABEL = "rolloverTextLabel";


    /**
     *  Constructor
     *
     *  @param name the display name of this action, as used in menus
     *  @param icon the default icon associated with this action (equivalent to
     *              the icon set by the setSmallIcon() method)
     *  @param cmd the Command object that will have its execute() method
     *  called by this object's actionPerformed() method
     */
    public GUIAction ( String name, Icon icon, Command cmd ) {
        super(name, icon);
        Log.debug(50, "Creating GUIAction: " + name + " " + this.toString());
        defaultIcon = icon;
        command=cmd;
        menuPosition = -1;
        toolbarPosition = -1;
        setEnabled(true);
        enabledList = new MappingsTable();
        commandList = new MappingsTable();
    }


  /**
   * Make a clone of the GUIAction instance.
   *
   * @return GUIAction
   */
  public GUIAction cloneAction()
    {
        GUIAction clone = null;
        try {
            clone = (GUIAction)super.clone();
        } catch (CloneNotSupportedException cnse) {
            Log.debug(1, "Fatal error: cloning operation not supported.");
        }

        // Establish that this is a clone by including a reference to the
        // original GUIAction from which this clone was made
        clone.setOriginalAction(this);

        // Need to set the other relevant properties here as well
        // MBJ Not completed yet
        clone.setTextLabel(getTextLabel());
        clone.setCommand(getCommand());
        clone.setDefaultIcon(getDefaultIcon());
        clone.setSmallIcon(getSmallIcon());
        clone.setEnabled(isEnabled());
        clone.setSeparatorPosition(getSeparatorPosition());
        clone.setMenu(getMenuName(), getMenuPosition());
        clone.setAcceleratorKey(getAcceleratorKey());
        clone.setRolloverSmallIcon(getRolloverSmallIcon());
        clone.setRolloverTextLabel(getRolloverTextLabel());
        // Clone the enabled list
        Enumeration enabledKeys = enabledList.keys();
        while (enabledKeys.hasMoreElements()) {
            String key = (String)enabledKeys.nextElement();
            String changedState = new String(key);
            boolean enabled
                      = ((Boolean)enabledList.getNewState(key)).booleanValue();
            clone.setEnabledOnStateChange(changedState, enabled,
                                                enabledList.getRespondsTo(key));
        }
        // Clone the command list
        Enumeration commandKeys = commandList.keys();
        while (commandKeys.hasMoreElements()) {
            String key = (String)commandKeys.nextElement();
            String changedState = new String(key);
            Command command = (Command)commandList.getNewState(key);
            clone.setCommandOnStateChange(changedState, command,
                                                commandList.getRespondsTo(key));
        }

        return clone;
    }


  /**
   * sets the text for the display label
   *
   * @param name the icon to be used by default
   */
  public void setTextLabel(String name)
   {
       super.putValue(AbstractAction.NAME, name);
   }

    /**
     *  gets the text for the display label
     *
     *  @return String representing the name of the action
     */
    public String getTextLabel(){
        return (String)super.getValue(AbstractAction.NAME);
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
     * Set the default icon.
     *
     * @param icon the icon to be used by default
     */
    public void setDefaultIcon(Icon icon)
    {
        this.defaultIcon = icon;
    }

    /**
     *  gets the text for the tooltip
     *
     *  @return String representing the tooltip text
     */
    public String getToolTipText(){
        return (String)super.getValue(AbstractAction.SHORT_DESCRIPTION);
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
     *  sets the *rollover* small Icon to be displayed on toolbar buttons
     *
     *  @param rolloverSmallIcon the Icon to be displayed on menus etc
     */
    public void setRolloverSmallIcon(Icon rolloverSmallIcon) {
        super.putValue(ROLLOVER_SMALL_ICON, rolloverSmallIcon);
    }


    /**
     *  gets the *rollover* small Icon to be displayed on toolbar buttons
     *
     *  @return rolloverSmallIcon the rollover Icon to be displayed on menus etc
     */
    public Icon getRolloverSmallIcon() {
        return (Icon)super.getValue(ROLLOVER_SMALL_ICON);
    }


    /**
     *  sets the *rollover* text label to be displayed on toolbar buttons
     *  (mainly used for HTML text, where only the color or font face changes)
     *
     *  @param rolloverTextLabel the label to be displayed on rollover
     */
    public void setRolloverTextLabel(String rolloverTextLabel) {
        super.putValue(ROLLOVER_TEXT_LABEL, rolloverTextLabel);
    }


    /**
     *  gets the *rollover* text label to be displayed on toolbar buttons
     *  (mainly used for HTML text, where only the color or font face changes)
     *
     *  @return rolloverTextLabel the label to be displayed on rollover
     */
    public String getRolloverTextLabel() {
        return (String)super.getValue(ROLLOVER_TEXT_LABEL);
    }


    /**
     *  sets the small Icon to be displayed on toolbar buttons & menus. *NOTE*
     *  that this icon is typiucally set by passing it to the *constructor*
     *
     *  @param smallIcon the Icon to be displayed on menus etc
     */
    public void setSmallIcon(Icon smallIcon) {
        super.putValue(AbstractAction.SMALL_ICON, smallIcon);
    }


    /**
     *  Get the small Icon to be displayed on menus
     *
     *  @return the Icon to be displayed on menus etc
     */
    public Icon getSmallIcon() {
        return (Icon)super.getValue(AbstractAction.SMALL_ICON);
    }

    /**
     * set a accelerator key for this action
     * @param keyString  the accelerator key string
     */
    public void setAcceleratorKeyString(String keyString)
    {
      if (keyString != null)
      {
        super.putValue(AbstractAction.ACCELERATOR_KEY,
                       KeyStroke.getKeyStroke(keyString));
      }
    }


  /**
   * Method to get accelerator key
   *
   * @return KeyStroke
   */
  public KeyStroke getAcceleratorKey()
    {
      return (KeyStroke)super.getValue(AbstractAction.ACCELERATOR_KEY);
    }


  /**
   * Method to set accelerator key
   *
   * @Param key the accelerator key
   * @param key KeyStroke
   */
  public void setAcceleratorKey(KeyStroke key)
    {
      if (key != null)
      {
        super.putValue(AbstractAction.ACCELERATOR_KEY, key);
      }
    }//

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
   * Get the menu item position for this action
   *
   * @return int
   */
  public int getMenuItemPosition() {
        Integer position = (Integer) super.getValue("menuPosition");
        int menuPos = (position != null) ? position.intValue() : -1;
        return menuPos;
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
   * Get the separator position for this action
   *
   * @return String
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
        command.execute(actionEvent);
    }


  /**
   * Get the command in this action object
   *
   * @return Command
   */
  public Command getCommand()
    {
      return command;
    }//getCommand


  /**
   * Set the command in this action object
   *
   * @param cmd Command
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
   * @param enabled boolean value indicating whether the action should be
   *   enabled
   * @param respondTo int value indicating whether the change should occur in
   *   response only to events originating within the same frame as this
   *   GUIAction's container (GUIAction.EVENT_LOCAL) or in response to all
   *   events, irrespective of their source (GUIAction.EVENT_GLOBAL).
   */
  public void setEnabledOnStateChange(String changedState,
                                                boolean enabled, int respondTo)
    {

        enabledList.put(
                  changedState, new Boolean(enabled), respondTo);
        StateChangeMonitor.getInstance().addStateChangeListener(changedState,
                (StateChangeListener)this);
    }

    /**
     * Set the state of this action to the enabled value if a state change
     * matching changedState occurs.
     *
     * @param changedState the name of the state change
     * @param command Command that should be set upon a state change
     * @param respondTo       int value indicating whether the change should
     *                        occur in response only to events originating
     *                        within the same frame as this GUIAction's
     *                        container (GUIAction.EVENT_LOCAL) or in response
     *                        to all events, irrespective of their source
     *                        (GUIAction.EVENT_GLOBAL).
     */
    public void setCommandOnStateChange(String changedState,
                                                  Command command,int respondTo)
    {
        commandList.put(changedState, command, respondTo);
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

        if (enabledList.containsKey(changedState)) {
            if ( enabledList.getRespondsTo(changedState)==EVENT_LOCAL
                && !isLocalEvent(event)) {
                Log.debug(50,"GUIAction.handleStateChange: event in enabledList"
                                    +" but not of this frame");
            } else {
                // Check and handle the event enabled state changes
                boolean enabled = ((Boolean)enabledList
                                    .getNewState(changedState)).booleanValue();
                setEnabled(enabled);
            }
        }

        if (commandList.containsKey(changedState)) {
            if (commandList.getRespondsTo(changedState)==EVENT_LOCAL
                && !isLocalEvent(event)) {
                Log.debug(50,"GUIAction.handleStateChange: event in commandList"
                                    +" but not of this frame");
            } else {
                // Check and handle the command state changes
                Command newCommand
                              = (Command)commandList.getNewState(changedState);
                setCommand(newCommand);
            }
        }
    }

    /**
     * Get the position of this action on the toolbar.  If it is less than
     * zero, the item is not shown on the toolbar.
     *
     * @return the position of the action on the toolbar
     */
    public int getToolbarPosition()
    {
        return this.toolbarPosition;
    }

    /**
     * Set the position of this action on the toolbar.  If it is less than
     * zero, the item is not shown on the toolbar.
     *
     * @param position the position of the action on the toolbar
     */
    public void setToolbarPosition(int position)
    {
        this.toolbarPosition = position;
    }

    /**
     * Get the original action from which a clone was made.
     *
     * @return the original GUIAction that created the clone, or null if it is
     *         not actually a clone
     */
    public GUIAction getOriginalAction()
    {
        return this.originalAction;
    }

    /**
     * Add a reference to the original action from which a clone was made
     * when a clone is being created.
     *
     * @param action the originating action from which this clone was made
     */
    private void setOriginalAction(GUIAction action)
    {
        this.originalAction = action;
    }

    public static MorphoFrame getMorphoFrameAncestor(Component c)
    {
        Object parent = null;
        if (c instanceof MorphoFrame) {
            return (MorphoFrame)c;

        } else {

            parent = c.getParent();
            if ((parent==null) || (parent instanceof MorphoFrame)) {
                return (MorphoFrame)parent;
            }
        }
        return getMorphoFrameAncestor((Component)parent);
    }

    //returns true if event originated in same MorphoFrame as this GUIAction
    private boolean isLocalEvent(StateChangeEvent event)
    {
        Object source = event.getSource();
        if (source==null) {
            Log.debug(52, "GUIAction.isLocalEvent: got event with NULL source");
            return false;
        }
        MorphoFrame eventAncestor = null;

        if (source instanceof Component) {
            eventAncestor = getMorphoFrameAncestor((Component)source);

        } else if (source instanceof MenuComponent) {
            eventAncestor = getMorphoFrameAncestor(
                        (Component)( ( (MenuComponent)source ).getParent()) );
        } else {
          return false;
        }
        MorphoFrame thisAncestor
                        = UIController.getMorphoFrameContainingGUIAction(this);
        Log.debug(52,"\n# # GUIAction.isLocalEvent: "+
                "GUIAction name:: "+this.getTextLabel()+
                 " thisAncestor="+thisAncestor +
                 " And!!!! event name: "+event.getChangedState()+
                 " comparing eventAncestor=" +eventAncestor);
        Log.debug(52,"\n# # result = "+( eventAncestor==thisAncestor ));
        return ( eventAncestor==thisAncestor );
    }

// * * *  V A R I A B L E S  * * *

    private Command command;
    private Icon defaultIcon;
    private String menuName;
    private int menuPosition;
    private int toolbarPosition;
    private MappingsTable enabledList;
    private MappingsTable commandList;
    private GUIAction originalAction;

// * * *  I N N E R  C L A S S  * * *

    class MappingsTable
    {
        private final int MAX_COLUMNS = 2;
        private Hashtable table;


        public Object getNewState(String key) { return getColumn(key,1); }

        public int getRespondsTo(String key) {

            return ((Integer)getColumn(key,2)).intValue();
        }

        public void put(String key, Object newState, int respondsTo)
        {
            if (!table.containsKey(key))  {

                table.put(  key,
                            new Object[] { newState, new Integer(respondsTo) });
            }
        }

        // * * * * * * COULD PULL THESE OUT INTO A UTIL CLASS: * * * * * * * *

        MappingsTable() { table = new Hashtable(); }

        public Enumeration keys() { return table.keys(); }

        public boolean containsKey(Object key) { return table.containsKey(key);}

        //colNum is one-based - imagine a table where col. zero is the key
        private Object getColumn(Object key, int colNum)
        {
            if (!table.containsKey(key))  {
                Log.debug(50, "MappingsTable: invalid key" + key);
                return null;
            } else if (colNum > MAX_COLUMNS || colNum < 1) {
                Log.debug(50, "MappingsTable: invalid colNum" + colNum);
                Log.debug(50, "(max allowed = " + (MAX_COLUMNS) + ")");
                return null;
            }
            return ( (Object[])table.get(key) )[colNum-1];
        }

        public void put(Object key, Object[] params)
        {
            if (params.length > MAX_COLUMNS) {
                Log.debug(50, "MappingsTable: too many params: "+params.length);
                return;
            } else if (params.length < MAX_COLUMNS) {
                Log.debug(50, "MappingsTable: insufficient params; padding out "
                                                                +params.length);
                Object[] newArray = new Object[MAX_COLUMNS];
                for (int i=0;i<params.length; i++) {
                    newArray[i] = params[i];
                }
                params = newArray;
            }
            table.put(key, params);
        }
    }
}
