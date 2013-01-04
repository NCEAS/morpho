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
import java.io.File;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Command;

import edu.ucsb.nceas.morpho.dataone.EcpAuthentication;
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

		ConfigXML profile = morpho.getProfile();
		// decide whether to use password or to use certificate location directly
		String username = loginClient.getUsername();
		String password = loginClient.getPassword();
		String idp = loginClient.getIdentityProvider();
		// try to authenticate with ECP
		boolean connected = false;
		try {
			File certificateLocation = EcpAuthentication.getInstance().authenticate(idp, username, password);
			connected = morpho.getDataONEDataStoreService().logIn(certificateLocation.getAbsolutePath());
		} catch (Exception e) {
			// something didn't work...
			Log.debug(10, "Could not authenticate: " + e.getMessage());
			e.printStackTrace();
		}
		if (connected) {
			profile.set("searchnetwork", 0, "true", true);
			Log.debug(12, "LoginCommand: Login successful");
			loginClient.setLoginSuccessful(true);
			UIController controller = UIController.getInstance();
			if (controller != null) {
				controller.updateAllStatusBars();
			}
			morpho.fireConnectionChangedEvent();
			morpho.fireUsernameChangedEvent();
		} else {
			Log.debug(12, "LoginCommand: " + Language.getInstance().getMessage("LoginFailed"));
			loginClient.setLoginSuccessful(false);
		}
				
	}
}
