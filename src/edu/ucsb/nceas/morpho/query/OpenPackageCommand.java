/**
 *  '$RCSfile: OpenPackageCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-26 00:46:36 $'
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

package edu.ucsb.nceas.morpho.query;
import  edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.util.*;
import javax.swing.JDialog;

/**
 * Class to handle open package command
 */
public class OpenPackageCommand implements Command 
{
    
  /** A reference to the resultPanel */
   private ResultPanel resultPanel = null;
  
  /** A reference to the dialog */
   private OpenDialogBox open = null;
   
  /** A refernce to the MorphoFrame (for butterfly) */
   private MorphoFrame frame = null;
   
  /**
   * Constructor of SearcCommand
   * @param myResultPanel the result panel which the openpackage 
   */
  public OpenPackageCommand(ResultPanel myResultPanel)
  {
    resultPanel = myResultPanel;
    open = resultPanel.getDialog();
    // if Resulpanel's parent is morphoframe, frame value will be set the parent
    // of resultpanel
    if ( open == null)
    {
      frame = UIController.getInstance().getCurrentActiveWindow();
    }
    else
    {
      //ResultPanel is in oepn dialog box, frame will be the parent of dialog 
      frame =open.getParentFrame();
    }
  }//OpenPackageCommand
  
  
  /**
   * execute open package command
   */    
  public void execute()
  {
     doOpenPackage(resultPanel,frame, open);
     open = null;
    
  }//execute

  /**
   * Using SwingWorket class to open a package
   *
   */
  private void doOpenPackage(final ResultPanel results, 
        final MorphoFrame morphoFrame, final OpenDialogBox box)
  {
    final SwingWorker worker = new SwingWorker()
    {
      public Object construct()
      {
        morphoFrame.setBusy(true);
        resultPanel.doOpenDataPackage();
        return null;
      }//constructor
      
      public void finished()
      {
         // close the openDialogBox
        if ( box!= null)
        {
          box.setVisible(false);
          box.dispose();
        }
        morphoFrame.setBusy(false);
      }//finish
    };//final
    worker.start();
    
  }//doOpenPakcage
   /**
    * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
