/**
 *  '$RCSfile: SearchCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-04-19 20:44:50 $'
 * '$Revision: 1.18 $'
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
import edu.ucsb.nceas.morpho.util.SortableJTable;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

import java.util.Vector;
import java.awt.event.ActionEvent;

/**
 * Class to handle search command
 */
public class SearchCommand implements Command
{
  /** A reference to the JDialogBox */
  private OpenDialogBox dialogBox = null;

  /** A reference to the Morpho application */
  private Morpho morpho = null;


  /**
   * Constructor of SearcCommand
   * @param myDialogBox the dialogbox has a search button
   * @param morpho the morpho application which the search command will apply
   */
  public SearchCommand(OpenDialogBox myDialogBox, Morpho morpho)
  {
    dialogBox = myDialogBox;
    this.morpho = morpho;

  }//SearchCommand


  /**
   * execute cancel command
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event)
  {
    // Hide and destory the dialogBox
    MorphoFrame morphoFrame = null;
    if ( dialogBox != null)
    {
      morphoFrame = dialogBox.getParentFrame();
      dialogBox.setVisible(false);
      dialogBox.dispose();
      dialogBox = null;
    }//if
    else
    {
      morphoFrame=UIController.getInstance().getCurrentActiveWindow();
    }//else
     // QueryDialog Create and show as modal
    if (morphoFrame != null)
    {
      QueryDialog queryDialog = new QueryDialog(morphoFrame, morpho);
      queryDialog.setModal(true);
      queryDialog.show();
      if (queryDialog.isSearchStarted())
      {
        Query query = queryDialog.getQuery();
        if (query != null)
        {
           MorphoFrame box = UIController.getInstance().addWindow(query.getQueryTitle());
           // first true is sorted or not, 5 is sorted column index, second true
          // is send event of not
          doQuery(box, query, true, 5, SortableJTable.DECENDING, true);
        }//if
      }//if
    }//if
  }//execute


  /**
   * Run the search query
   *
   * @param resultWindow MorphoFrame
   * @param query Query
   */
  public static void doQuery(MorphoFrame resultWindow, Query query,
                             boolean sorted, int sortedIndex, String sortedOder,
                             boolean sendEvent)
  {
     Morpho morphoInQuery = query.getMorpho();
     resultWindow.setVisible(true);
     Vector vector = new Vector();
     //String source ="";
     HeadResultSet results = new HeadResultSet(
                                         query, vector, morphoInQuery);
     //boolean listCrashedDoc = false;
     ResultPanel resultDisplayPanel = new ResultPanel(
     null,results, 12, null, resultWindow.getDefaultContentAreaSize());
     resultDisplayPanel.setVisible(true);
     resultWindow.setMainContentPane(resultDisplayPanel);
     boolean showSearchNumber = true;
     StateChangeEvent event = null;
     if (sendEvent)
     {
       event = new StateChangeEvent(resultDisplayPanel,
                                 StateChangeEvent.CREATE_SEARCH_RESULT_FRAME);

     }
     query.displaySearchResult(resultWindow, resultDisplayPanel, sorted,
                               sortedIndex, sortedOder, showSearchNumber, event);

  }//doQuery

  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class CancelCommand

