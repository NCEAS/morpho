/**
 *  '$RCSfile: OpenDialogBoxCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-17 01:30:11 $'
 * '$Revision: 1.3 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.*;
import javax.swing.JDialog;

/**
 * Class to handle Open a dialog box command
 */
public class OpenDialogBoxCommand implements Command 
{
  
  /** A reference to Morpho application */
  private Morpho morpho = null;
  
  /** A reference to the owner query*/
  private Query ownerQuery = null;
  
  /**
   * Constructor of SearcCommand
   *
   * @param morpho the Morpho app to which the cancel command will apply
   */
  public OpenDialogBoxCommand(Morpho morpho, Query myQuery)
  {
    this.morpho = morpho;
    ownerQuery = myQuery;
    
   }//OpenDialogBoxCommand
  
  
  /**
   * execute cancel command
   */    
  public void execute()
  {
    OpenDialogBox open = null;
    open = new OpenDialogBox(morpho, ownerQuery);
    open.setVisible(true);
   
  }//execute

  
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class OpenDialogBoxCommand
