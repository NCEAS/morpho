/**
 *  '$RCSfile: RevertCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2005-05-27 21:23:49 $'
 * '$Revision: 1.6 $'
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
import org.w3c.dom.Node;
import edu.ucsb.nceas.morpho.util.Log;


/**
 * Class to Revert to current tab to original data sets
 */
public class RevertCommand implements Command
{
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  /** A reference to the AbstractDataPackage to be saved */
  private AbstractDataPackage adp = null;


  /**
   * Constructor of RevertCommand
   */
  public RevertCommand()
  {

  }


  /**
   * Constructor of RevertCommand
   *
   * @param adp the open dialog box will be applied this command
   */
  public RevertCommand(AbstractDataPackage adp)
  {

  }


  /**
   * execute the revert command
   *
   * @param event ActionEvent
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
      adp = morphoFrame.getAbstractDataPackage();
      boolean local = false;
      boolean metacat = false;
      if(adp.getLocation().equals(AbstractDataPackage.LOCAL)){
        local = true;
      } else if(adp.getLocation().equals(AbstractDataPackage.METACAT)){
        metacat = true;
      } else {
        local = true;
        metacat = true;
      }

      AbstractDataPackage tempAdp = null;
      Entity[] entArray = null;
      DataViewer dv = dvcp.getCurrentDataViewer();
      int entityIndex = dv.getEntityIndex();

      try {
        tempAdp = DataPackageFactory.getDataPackage(adp.getAccessionNumber(),
            metacat, local);
      } catch(Exception e){
        Log.debug(5,"Check if the datapackage was saved.");
      }

      try{
        if(tempAdp!=null){
          String entityId = adp.getEntityID(entityIndex);
          int tempEntityIndex = -1;
          entArray = tempAdp.getEntityArray();
          for(int count=0; count < entArray.length; count++){
            if (tempAdp.getEntityID(count).equals(entityId)) {
              tempEntityIndex = count;
              break;
            }
          }

          if (tempEntityIndex > -1) {
              // column has been added or deleted
              adp.deleteEntity(entityIndex);
              adp.insertEntity(entArray[tempEntityIndex], entityIndex);
          } else {
            throw (new Exception());
          }
        }

       }catch(Exception e){
        Log.debug(5,"Unable to revert entity to saved version. Check if the entity was saved.");
      }
      dv.init();
    }
  }//execute


  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class OpenDialogBoxCommand
