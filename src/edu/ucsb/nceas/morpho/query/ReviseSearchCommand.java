/**
 *  '$RCSfile: ReviseSearchCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-09-15 19:34:00 $'
 * '$Revision: 1.5.2.1 $'
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
import java.awt.event.ActionEvent;
import javax.swing.JDialog;

/**
 * Class to handle search command
 */
public class ReviseSearchCommand implements Command 
{
  /** A reference to the MophorFrame */
  private MorphoFrame morphoFrame = null;
  
  /** A reference to the Morpho application */
  private Morpho morpho = null;
  
    
  /**
   * Constructor of SearcCommand
   *
   * @param morpho the morpho application which the search command will apply
   */
  public ReviseSearchCommand(Morpho morpho)
  {
    this.morpho = morpho;
  }//SearchCommand
  
 
  
  /**
   * execute cancel command
   */    
  public void execute(ActionEvent event)
  {
    // Get the current morpho frame
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
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
      
      // Get ResultPanel
      ResultPanel resultPane = (ResultPanel) comp;
      // Get  result set of the result panel
      ResultSet results = resultPane.getResultSet();
      // Save the original identifier
      String oldIdentifier = results.getQuery().getQueryTitle();
      
      // Get SortableJTable
      SortableJTable table = resultPane.getJTable();
      boolean sorted = false;
      int index = -1;
      String order = null;
      if ( table != null)
      {
        sorted = table.getSorted();
        index = table.getIndexOfSortedColumn();
        order = table.getOrderOfSortedColumn();
      }

      // QueryDialog Create and show as modal
      QueryDialog queryDialog1 = null;
      queryDialog1 = new QueryDialog(morphoFrame, morpho);
     
      queryDialog1.setQuery(results.getQuery());
      queryDialog1.setModal(true);
      queryDialog1.show();
      if (queryDialog1.isSearchStarted()) 
      {
        Query query = queryDialog1.getQuery();
        morphoFrame.setBusy(true);
        doReviseQuery(morphoFrame, query, oldIdentifier, sorted, index, order);
      }
    }//if
      
 
  }//execute
  
  /**
   * Run revised query in another thread
   */
  private void doReviseQuery(final MorphoFrame frame, final Query myQuery, 
                             final String identifier, final boolean sorted,
                             final int index, final String order)
  {
    final SwingWorker worker = new SwingWorker()
    {
      public Object construct()
      {
        String currentIdentifier = null;
        if (myQuery != null) 
        {
          currentIdentifier = myQuery.getQueryTitle();
          ResultSet newResults = myQuery.execute();
          // null means the resultpanel would NOT be setted to a JDialog
          ResultPanel resultDisplayPanel = new ResultPanel(
           null, newResults, 12, null, morphoFrame.getDefaultContentAreaSize());
          // if previsous frame sorted, then sorted
          if (sorted)
          {
            resultDisplayPanel.sortTable(index, order);
          }
          resultDisplayPanel.setVisible(true);
          frame.setMainContentPane(resultDisplayPanel);
          frame.setMessage(newResults.getRowCount()+ " data sets found");
          
          // If the user change the identifier of the query
          // we need change the title of the frame and update window menu
          if ( currentIdentifier != null && !currentIdentifier.equals("") 
                          && !currentIdentifier.equals(identifier))
          {
            
            // Change the title of the frame and update the windows menu
            UIController.getInstance().
                          updateWindow(frame, currentIdentifier);
          }
        }
        return null;
      }//constructor
      
      //Runs on the event-dispatching thread.
      public void finished()
      {
         morphoFrame.setBusy(false);
      }//finished
    };
    worker.start();
     
  }//doReviseQuery

 
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
