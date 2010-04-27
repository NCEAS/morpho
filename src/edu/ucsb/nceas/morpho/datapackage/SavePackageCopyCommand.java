/**
 *  '$RCSfile: SavePackageCopyCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-07-31 00:55:43 $'
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
import edu.ucsb.nceas.morpho.util.SaveEvent;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.Morpho;
import java.awt.Component;


/**
 * Class to Save an AbstractDataPackage to Local File system
 */
public class SavePackageCopyCommand implements Command
{
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  /** A reference to the AbstractDataPackage to be saved */
  private AbstractDataPackage adp = null;

  /** A flag indicating whether to display newly saved package */
  private boolean showPackageFlag = true;

  /**
   * Constructor of SavePackageCommand
   */
  public SavePackageCopyCommand()
  {

  }//SavePackageCommand

  /**
   * Constructor of SavePackageCommand
   *
   */
  public SavePackageCopyCommand(AbstractDataPackage adp)
  {
    this.adp = adp;
  }//SavePackageCommand

  /**
   *  constructor with boolean to determine if saved package is displayed
   */
  public SavePackageCopyCommand(AbstractDataPackage adp, boolean showPackageFlag)
  {
    this.adp = adp;
    this.showPackageFlag = showPackageFlag;
  }


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

    Morpho morpho = Morpho.thisStaticInstance;
    String location = adp.getLocation();
    String oldid = adp.getPackageId();

    AccessionNumber an = new AccessionNumber(morpho);
    String nextid = an.getNextId();
    adp.setAccessionNumber(nextid);

    try{
      adp.serialize(AbstractDataPackage.LOCAL);
      adp.setLocation(AbstractDataPackage.LOCAL);
      adp.serializeData(AbstractDataPackage.LOCAL);
      SaveEvent saveEvent = new SaveEvent(morphoFrame, StateChangeEvent.SAVE_DATAPACKAGE);
      saveEvent.setInitialId(oldid);
      saveEvent.setFinalId(nextid);
      saveEvent.setLocation(AbstractDataPackage.LOCAL);
      saveEvent.setDuplicate(true);
      StateChangeMonitor.getInstance().notifyStateChange(saveEvent);
    } catch (MetacatUploadException e){
      Log.debug(5, "Problem Saving Datapackage Locally");
    }

    if (showPackageFlag) {
      UIController.showNewPackage(adp);
    }
    else {
      MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
      morphoFrame.setVisible(false);
      UIController controller = UIController.getInstance();
      controller.removeWindow(morphoFrame);
      morphoFrame.dispose();
    }

    try
    {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider =
                  services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      dataPackage.openDataPackage(location, oldid, null, null, null);
    }
    catch (ServiceNotHandledException snhe)
    {
      Log.debug(6, snhe.getMessage());
    }


  }//execute


  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class OpenDialogBoxCommand
