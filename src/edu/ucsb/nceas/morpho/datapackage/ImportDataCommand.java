/**
 *  '$RCSfile: ImportDataCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-12-02 22:11:39 $'
 * '$Revision: 1.5 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.*;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;

/**
 * Class to handle import data file command
 */
public class ImportDataCommand implements Command 
{
  /** A reference to the MophorFrame */
  private MorphoFrame morphoFrame = null;
 
  /**
   * Constructor of Import data command
   */
  public ImportDataCommand()
  {
 
  }//RefreshCommand

  /**
   * execute refresh command
   */    
  public void execute(ActionEvent event)
  {   
    DataViewContainerPanel resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null)
    {
       resultPane = AddDocumentationCommand.
                          getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    }//if
    // make sure resulPanel is not null
    if ( resultPane != null)
    {
      DataPackage dp = resultPane.getDataPackage();
      AbstractDataPackage adp = resultPane.getAbstractDataPackage();
      DataViewer dv = resultPane.getCurrentDataViewer();
      String entityId = null;
      if (dv!=null) {
        entityId = dv.getEntityFileId();
      }
      if (dp!=null) {
        if ((dp.hasDataFile(entityId))||(entityId==null)) {
          Morpho morpho = resultPane.getFramework();
          DataPackage dataPackage = resultPane.getDataPackage();
          AddMetadataWizard amw = new AddMetadataWizard(morpho, true, 
                   dataPackage, morphoFrame, AddMetadataWizard.NOTSHOWMETADATA);
          amw.showImportDataScreen();
          morphoFrame.setVisible(false);
        }
        else {
          // Log.debug(1,"No Data Branch");
        (  new NewDataFile(morphoFrame, dp, resultPane.getFramework(),
                         entityId)).setVisible(true);
        }
      } else { // new AbstractDataPackage/Wizard calls will go here
        Log.debug(1, "Cslls back to the second part of the Wizard will happen here!");
      }
    }//if
  
  }//execute
  
 

  
 
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
