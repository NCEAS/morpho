/**
 *  '$RCSfile: AddDocumentationCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: cjones $'
 *     '$Date: 2002-09-26 01:57:53 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;

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
   */    
  public void execute(ActionEvent event)
  {   
    DataViewContainerPanel resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null)
    {
       resultPane = getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    }//if
    
    // make sure resulPanel is not null
    if ( resultPane != null)
    {
       Morpho morpho = resultPane.getFramework();
       DataPackage dataPackage = resultPane.getDataPackage();
       morphoFrame.setVisible(false);
       AddMetadataWizard amw = new AddMetadataWizard(morpho, true, 
                 dataPackage, morphoFrame, AddMetadataWizard.NOTSHOWIMPORTDATA);
       amw.setVisible(true);
    }//if
  
  }//execute
  
 

  
  /**
   * Gave a morphoFrame, get DataViewContainerPanel from it. If morphFrame 
   * doesn't contain a DataViewContainerPanel, null will be returned
   *
   * @param frame the morpho frame which contains the need to be check
   */
  public static DataViewContainerPanel 
                      getDataViewContainerPanelFromMorphoFrame(MorphoFrame frame)
  {
    if (frame == null)
    {
      return null;
    }
    // Get content of frame
    Component comp = frame.getContentComponent();
    if (comp == null)
    {
      return null;
    }
    // Make sure the comp is a result panel object
    if (comp instanceof DataViewContainerPanel)
    {
      DataViewContainerPanel panel = (DataViewContainerPanel) comp;
      return panel;
    }
    else
    {
      return null;
    }
      
  }//getDataViewContainerPanelFromMorphFrame
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
