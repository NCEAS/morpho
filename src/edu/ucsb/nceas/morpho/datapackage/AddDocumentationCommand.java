/**
 *  '$RCSfile: AddDocumentationCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-24 02:14:18 $'
 * '$Revision: 1.10 $'
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

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;

import java.awt.event.ActionEvent;

/**
 * Class to handle add documentation command
 */
public class AddDocumentationCommand implements Command
{
  /** A reference to the MophorFrame */
  private MorphoFrame morphoFrame = null;

  /**
   * Constructor of refreshCommand
   * There is no parameter, means it will refresh current active morpho frame
   */
  public AddDocumentationCommand()
  {

  }//RefreshCommand


  /**
   * execute refresh command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event)
  {
    UIController.getInstance().launchEditorAtSubtreeForCurrentFrame(null, 0);
  }




  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class CancelCommand
