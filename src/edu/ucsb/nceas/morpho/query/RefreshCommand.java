/**
 *  '$RCSfile: RefreshCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-04 23:28:56 $'
 * '$Revision: 1.8 $'
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
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.Component;
import javax.swing.JDialog;

/**
 * Class to handle search command
 */
public class RefreshCommand implements Command 
{
  /** A reference to the MophorFrame */
  private MorphoFrame morphoFrame = null;
  
  /** A reference to a dialog */
  private OpenDialogBox dialog = null;
  
  /** A flag to indicate if refresh morpho is specified */
  private boolean specify = false;
  
    
  /**
   * Constructor of SearcCommand
   * @param specify a morphoframe need to be refreshed
   */
  public RefreshCommand(MorphoFrame box)
  {
    if (box != null)
    {
      morphoFrame = box;
      specify = true;
    }
  }//SearchCommand
  

  
  /**
   * execute cancel command
   */    
  public void execute()
  {
    ResultPanel resultPane = null;
    if (!specify)
    {
      // If not sepcify a frame, moreFrame will be set to be
      // current active morphoFrame
      morphoFrame = UIController.getInstance().getCurrentActiveWindow();
      
    }
    
    // make sure the morphoFrame is not null
    if ( morphoFrame == null)
    {
       Log.debug(5, "Morpho frame was null so I could refresh it!");
    }//if
    
    resultPane = getResultPanelFromMorphoFrame(morphoFrame);
    
    // make sure resulPanel is not null
    if ( resultPane != null)
    {
      
      Query myQuery = null;
      myQuery = resultPane.getResultSet().getQuery();
      if (myQuery != null)
      {
        
        morphoFrame.setBusy(true);
        doQuery(myQuery);
      }//if
    }//if
  
  }//execute
  
  /**
   * Gave a morphoFrame, get resultpanel from it. If morphFrame doesn't contain
   * a resultPanel, null will be returned
   *
   * @param frame the morpho frame which contains the result panel
   */
  public static ResultPanel getResultPanelFromMorphoFrame(MorphoFrame frame)
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
    if (comp instanceof ResultPanel)
    {
      ResultPanel resultPane = (ResultPanel) comp;
      return resultPane;
    }
    else
    {
      return null;
    }
      
  }//getResulPanelFromMorphFrame
  
  /**
   * Run the search query again 
   */
  private void doQuery(final Query query) 
  {
  
    final SwingWorker worker = new SwingWorker() 
    {
        ResultSet results;
        ResultPanel resultDisplayPanel = null;
        public Object construct() 
        {
          
          results = query.execute();
          
          // The size of resultpanel for morpho frame
          resultDisplayPanel = new ResultPanel(
              null, results, 12, null, morphoFrame.getDefaultContentAreaSize());
          resultDisplayPanel.setVisible(true); 
          morphoFrame.setMainContentPane(resultDisplayPanel);
          morphoFrame.setMessage(results.getRowCount() + " data sets found");
          
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() 
        {
          
          morphoFrame.setBusy(false);
        }
    };
    worker.start();  //required for SwingWorker 3
  }//doQuery
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
