/**
 *        Name: PluginInterface.java
 *     Purpose: An interface representing the methods that all plugin
 *		components must implement
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Matt Jones
 *
 *     Version: '$Id: PluginInterface.java,v 1.2 2000-11-30 19:45:04 higgins Exp $'
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
  * added by DFH, Nov 2000
  */
  public void setContainer(Object o);
}
