/**
 *  '$RCSfile: StateChangeListener.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-29 23:41:31 $'
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

/**
 * Any object that is interested in the status of the morpho framework
 * state changed events should implement this interface and register with the
 * StateChangeMonitor to be notified of changes.
 */
public interface StateChangeListener
{
  /**
   * This method is called if there is a change in the state of the 
   * application. 
   *
   * @param event the StateChangeEvent indicating what state changed
   */
  public void handleStateChange(StateChangeEvent event);
}
