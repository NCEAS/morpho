/**
 *  '$RCSfile: LoginClientInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-12-13 06:00:01 $'
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

import java.awt.event.ActionEvent;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Command;


/**
 *  Interface used by LoginCommand to do callbacks to login client that called 
 *  the LoginCommand.
 */
public interface LoginClientInterface
{
  /**
   *  gets the user-entered password from the client
   *
   *  @return   the user-entered password as a String
   */
  public String getPassword();
  
  /**
   *  notifies client whether login was successful or not
   *
   *  @return   boolean flag indicating whether login was successful (true) or 
   *            not (false)
   */
  public void setLoginSuccessful(boolean success);
  
}
