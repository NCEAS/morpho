/**
 *  '$RCSfile: StoreStateChangeEvent.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-10-01 00:18:17 $'
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

import java.util.Vector;
/**
 * In order to handle local event, we need to compare the parent of event's 
 * source and GUIAction'parent. But if an event happened before the source 
 * added to a container, this will cause the parent of event' source is null.
 * So we need this mechanism to store the event, then broadcast late.
 */
public interface StoreStateChangeEvent
{
  /**
   * This method is called if a StateChangeEvent need to be stored
   * @param event the StateChangeEvent needed to be stored
   */
  public void storingStateChangeEvent(StateChangeEvent event);
  
  /**
   * Get the  stored state change event.
   */
  public Vector getStoredStateChangeEvent();
  
  /**
   * Broadcast the stored StateChangeEvent
   */
  public void broadcastStoredStateChangeEvent();
}
