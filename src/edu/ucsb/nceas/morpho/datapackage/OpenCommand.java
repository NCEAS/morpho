/**
 *  '$RCSfile: SavePackageCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2008-06-20 23:44:14 $'
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

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;


/**
 * Class to open an AbstractDataPackage from Metacat and/or Local by a given id
 * File system
 */
public class OpenCommand implements Command
{
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  /** A reference to the AbstractDataPackage to be saved */
  private MorphoDataPackage mdp = null;
  
  /** A flag indicating whether to display newly saved package */
  private boolean showPackageFlag = true;

  /**
   * Constructor of OpenCommand
   */
  public OpenCommand()
  {

  }//OpenCommand

  /**
   * Constructor of OpenCommand
   *
   */
  public OpenCommand(MorphoDataPackage mdp)
  {
    this.mdp = mdp;
  }//OpenCommand
  
  /**
   *  constructor with boolean to determine if open package is displayed
   */
  public OpenCommand(MorphoDataPackage mdp, boolean showPackageFlag)
  {
    this.mdp = mdp;
    this.showPackageFlag = showPackageFlag;
  }
   

  /**
   * execute the open datapackage command
   */
  public void execute(ActionEvent event)
  {
    DataViewContainerPanel dvcp = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null)
    {
       dvcp = morphoFrame.getDataViewContainerPanel();
    }//if
    if (dvcp!=null) {
      dvcp.saveDataChanges();  // needed to flag datatable changes
      mdp = dvcp.getMorphoDataPackage();
    }
    new SaveDialog(mdp, showPackageFlag);

  }//execute


  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class OpenDialogBoxCommand

