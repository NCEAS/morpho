/**
 *  '$RCSfile: SavePackageCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-18 02:21:40 $'
 * '$Revision: 1.4 $'
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

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;


/**
 * Class to Save an AbstractDataPackage to Metacat and/or Local
 * File system
 */
public class SavePackageCommand implements Command
{
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  /** A reference to the AbstractDataPackage to be saved */
  private AbstractDataPackage adp = null;


  /**
   * Constructor of OpenExportDialogCommand
   */
  public SavePackageCommand()
  {

  }//SavePackageCommand

  /**
   * Constructor of OpenExportDialogCommand
   *
   * @param myDialog the open dialog box will be applied this command
   */
  public SavePackageCommand(AbstractDataPackage adp)
  {
    this.adp = adp;
  }//SavePackageCommand

  /**
   * execute the save datapackage command
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
      adp = dvcp.getAbstractDataPackage();
    }
    new SaveDialog(adp);

  }//execute


  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class OpenDialogBoxCommand
