/**
 *  '$RCSfile: RefreshCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-20 21:15:06 $'
 * '$Revision: 1.1 $'
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
import edu.ucsb.nceas.morpho.util.*;
import java.awt.Component;
import javax.swing.JDialog;

/**
 * Class to handle search command
 */
public class RefreshCommand implements Command 
{
  /** A reference to the MophorFrame */
  private MorphoFrame morphoFrame = null;
  
  /** A reference to the Morpho application */
  //private Morpho morpho = null;
  
  /**
   * Constructor of SearcCommand
   */
  public RefreshCommand()
  {
    
  }//SearchCommand
  
  /**
   * Set a morphoFrame to it
   *
   * @param myMorphoFrame the morpho frame will be set to this object
   */
  public void setMorphoFrame(MorphoFrame myFrame)
  {
    morphoFrame = myFrame;
  }//setMorphoFrame
  
  /**
   * execute cancel command
   */    
  public void execute()
  {
    // make sure the morphoFrame is not null
    if ( morphoFrame == null)
    {
       Log.debug(5, "Morpho frame was null so I could refresh it!");
    }//if
    
    // Make sure the main panel is result panel
    Component comp = morphoFrame.getContentComponent();
    Query myQuery = null;
    if (comp != null && comp instanceof ResultPanel)
    {
      ResultPanel resultPane = (ResultPanel) comp;
      myQuery = resultPane.getResultSet().getQuery();
      doQuery(myQuery);
    }//if
      
 
  }//execute

  /**
   * Run the search query again 
   */
  private void doQuery(final Query query) 
  {
  
    final SwingWorker worker = new SwingWorker() 
    {
        ResultSet results;
        public Object construct() 
        {
          results = query.execute();
        
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() 
        {
          ResultPanel resultDisplayPanel = new ResultPanel(
              results,12, null, morphoFrame.getDefaultContentAreaSize());
          resultDisplayPanel.setVisible(true);
          morphoFrame.setMainContentPane(resultDisplayPanel);
          morphoFrame.setMessage(results.getRowCount() + " data sets found");
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
