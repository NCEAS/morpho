/**
 *  '$RCSfile: ConnectionListener.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-09 16:44:56 $'
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

package edu.ucsb.nceas.morpho.framework;

/**
 * Any object that is interested in the status of the morpho framework
 * connection should implement this interface and register with the
 * framework to be notified of changes, including changes to the 
 * username and connection status.
 */
public interface ConnectionListener
{
  /**
   * This method is called if there is a change in the connection
   * status. 
   *
   * @param isConnected boolean true if the framework is now connected
   */
  public void connectionChanged(boolean isConnected);

  /**
   * This method is called if there is a change in the username.
   *
   * @param username the new username as it now has been set
   */
  public void usernameChanged(String username);
}
