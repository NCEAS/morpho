/**
 *  '$RCSfile: PluginInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-07-13 17:29:01 $'
 * '$Revision: 1.6.2.1 $'
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

import javax.swing.Action;
import java.awt.Component;
import java.util.Hashtable;

/**
 * All component plugins that are to be included in the ClientFramework
 * must implement this interface so that the services of the ClientFramework
 * such as menu and toolbar actions, and session control, are available to
 * the plugin component.
 */
public interface PluginInterface
{

  /** 
   * This method is called after the Plugin has been created in order
   * to allow the plugin a chance to initialize variables, create menus
   * and toolbars, and store a reference to the framework.  The plugin 
   * must store a reference to the ClientFramework in order to be able 
   * to call the services available through the framework.
   */
  public void initialize(ClientFramework cf);
}
