/**
 *  '$RCSfile: GUIAction.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-01 00:17:41 $'
 * '$Revision: 1.1 $'
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

import javax.swing.Icon;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

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
public class GUIAction extends AbstractAction {

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
      * actionPerformed() method required by ActionListener interface
      *
      * @param actionEvent the event generated by the component
 */
    public void actionPerformed(ActionEvent actionEvent) {
        command.execute();
    }

        
// * * *  V A R I A B L E S  * * *

    private Command command;
    private Icon defaultIcon;
    
}
