/**
 *  '$RCSfile: AddAccessCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-20 18:26:05 $'
 * '$Revision: 1.7 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import java.awt.event.ActionEvent;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AccessPage;
import edu.ucsb.nceas.morpho.util.Command;

/**
 * Class to handle add access command
 */
public class RefreshAccessCommand
    implements Command {


  public RefreshAccessCommand() {}

  /**
   * execute add command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event) {
	  
	  AccessPage accessPage = new AccessPage();
	  accessPage.generateAccessTree(true);
  
  }

}
