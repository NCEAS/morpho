/**
 *  '$RCSfile: CancelCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-09-06 07:12:16 $'
 * '$Revision: 1.5.2.1 $'
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

package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.Window;
import java.awt.event.ActionEvent;

/**
 * Class to handle cancel command
 */
public class CancelCommand implements Command 
{
  /** A reference to the window */
  private Window window = null;
  
  /**
   * Constructor of CancelCommand
   * @param myWindow the window which the cancel command will apply
   */
  public CancelCommand(Window myWindow)
  {
    window = myWindow;
  }//CancelCommand
  
  
  /**
   * execute cancel command
   */    
  public void execute(ActionEvent event)
  {
     window.setVisible(false);
     // Destory the object
     window.dispose();
     window = null;
  }//execute

  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
