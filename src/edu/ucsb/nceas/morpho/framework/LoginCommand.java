/**
 *  '$RCSfile: LoginCommand.java,v $'
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

import javax.swing.SwingUtilities;

import edu.ucsb.nceas.morpho.Morpho;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Command;

import edu.ucsb.nceas.morpho.framework.LoginClientInterface;


/**
 * Class to handle login
 */
public class LoginCommand implements Command 
{

  private Morpho                morpho;
  private LoginClientInterface  loginClient;
  
  /**
   *  Constructor
   *
   *  @param morpho       reference to Morpho instance
   *
   *  @param loginClient  a poiner back to the client that is calling this 
   *                      <code>Command</code>; must implement the
   *                      <code>LoginClientInterface</code>
   */
  public LoginCommand(Morpho morpho, LoginClientInterface loginClient) {
  
    this.morpho = morpho;
    this.loginClient = loginClient;
  }


  /**
  * execute command
  */
  public void execute(ActionEvent event) {
    Thread worker = new Thread() {
      public void run() {
        if (morpho!=null) {
          morpho.setPassword(loginClient.getPassword());
        }

        final boolean connected = morpho.logIn();

        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if (connected) {
              ConfigXML profile = morpho.getProfile();
              profile.set("searchmetacat", 0, "true");
              profile.save();
              Log.debug(12, "LoginCommand: Login successful");
              loginClient.setLoginSuccessful(true);
            } else {
              Log.debug(12, "LoginCommand: Login failed");
              loginClient.setLoginSuccessful(false);
            }
          }
        });
      }
    };
    worker.start();
  }
}
