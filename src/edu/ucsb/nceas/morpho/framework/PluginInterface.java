/**
 *       Name: PluginInterface.java
 *    Purpose: An interface representing the methods that all plugin
 *             components must implement
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Matt Jones
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-04-17 01:07:47 $'
 * '$Revision: 1.2.2.1 $'
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

package edu.ucsb.nceas.dtclient;

import javax.swing.Action;

/**
 * All component plugins that are to be included in the ClientFramework
 * must implement this interface so that the services of the ClientFramework
 * such as menu and toolbar actions, and session control, are available to
 * the plugin component
 */
public interface PluginInterface
{

  /**
   * This method is called on component initialization to generate a list
   * of the names of the menus, in display order, that the component wants
   * added to the framework.  If a menu already exists (from another component
   * or the framework itself), the order will be determined by the earlier
   * registration of the menu.
   */
  public String[] registerMenus();

  /**
   * The plugin must return the Actions that should be associated 
   * with a particular menu. They will be appended onto the bottom of the menu
   * in most cases.
   */
  public Action[] registerMenuActions(String menu);

  /**
   * The plugin must return the list of Actions to be associated with the
   * toolbar for the framework. 
   */
  public Action[] registerToolbarActions();


  /** 
   * The plugin must be able to get an instance of ClientFramework
   */
  public void setContainer(Object o);
}
