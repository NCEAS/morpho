/**
 *  '$RCSfile: HelpMetadataIntroCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-04-14 20:59:18 $'
 * '$Revision: 1.2 $'
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

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.event.ActionEvent;


public class HelpMetadataIntroCommand implements Command {


  private final String INITIAL_URL
      = "file:///" + System.getProperty("user.dir")
        + "/docs/user/EmlDocs/eml_guidebook/eml_metadata_guide.html";


  public HelpMetadataIntroCommand() {}

  public void execute(ActionEvent ae) {

    MorphoFrame frame
        = UIController.getInstance().addWindow("Introduction to Metadata");

    HTMLBrowser viewer = new HTMLBrowser(frame);

    viewer.loadNewPage(INITIAL_URL);

    viewer.setVisible(true);
  }
}
