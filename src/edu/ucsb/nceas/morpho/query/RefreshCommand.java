/**
 *  '$RCSfile: RefreshCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-04-13 06:17:17 $'
 * '$Revision: 1.14.6.1 $'
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
import edu.ucsb.nceas.morpho.util.SortableJTable;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import java.awt.Component;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;

/**
 * Class to handle refresh command
 */
public class RefreshCommand implements Command
{
  /** A reference to the MophorFrame */
  private MorphoFrame morphoFrame = null;

  /** A reference to a dialog */
  private OpenDialogBox dialog = null;

  /** A flag to indicate if a morpho frame is specified  need to be refresh */
  private boolean specifyMorpho = false;

  /** A flag to indicate if a OpenDialog is specified need to be refresh */
  private boolean specifyOpenDialog = false;

  /**
   * Constructor of refreshCommand
   * There is no parameter, means it will refresh current active morpho frame
   */
  public RefreshCommand()
  {

  }//RefreshCommand

  /**
   * Constructor of RefreshCommand
   * @param box specify a morphoframe need to be refreshed
   */
  public RefreshCommand(MorphoFrame box)
  {
    if (box != null)
    {
      morphoFrame = box;
      specifyMorpho = true;
    }
  }//RefreshCommand

 /**
   * Constructor of RefreshCommand
   * @param myOpenDialog specify a Open dialog need to be refreshed
   */
  public RefreshCommand(OpenDialogBox myOpenDialog)
  {
    if (myOpenDialog != null)
    {
      dialog = myOpenDialog;
      specifyOpenDialog = true;
    }
  }//RefreshCommand


  /**
   * execute refresh command
   */
  public void execute(ActionEvent event)
  {
    ResultPanel resultPane = null;
    if (specifyOpenDialog)
    {
      // for a dialog
      morphoFrame = dialog.getParentFrame();
      resultPane = dialog.getResultPanel();

    }//if
    else if (specifyMorpho)
    {
      // for a specify morpho frame
      resultPane = getResultPanelFromMorphoFrame(morphoFrame);
    }//else if
    else
    {
      // If not sepcify a frame, moreFrame will be set to be
      // current active morphoFrame
      morphoFrame = UIController.getInstance().getCurrentActiveWindow();
      if (morphoFrame != null)
      {
         resultPane = getResultPanelFromMorphoFrame(morphoFrame);
      }//if
    }//else

    // make sure resulPanel is not null
    if ( resultPane != null)
    {
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

      Query myQuery = null;
      myQuery = resultPane.getResultSet().getQuery();
      if (myQuery != null)
      {
        doQuery(myQuery, specifyOpenDialog, sorted, index, order);
      }//if
    }//if

  }//execute


  /**
   * Run the search query again
   */
  private void doQuery(Query query, boolean forOpenDialog,
                        boolean sort, int index, String order)
  {

     // The size of resultpanel for morpho frame
     if (!forOpenDialog)
     {
       // last false means not send a statechange event
       SearchCommand.doQuery(morphoFrame, query, sort, index, order, false);
     }//if
     else
     {
       Morpho morphoInQuery = query.getMorpho();
       Vector vector = new Vector();
       String source ="";
       HeadResultSet results = new HeadResultSet(
                                       query, source, vector, morphoInQuery);
       ResultPanel resultDisplayPanel = new ResultPanel(dialog, results, null);
       resultDisplayPanel.setVisible(true);
       dialog.setResultPanel(resultDisplayPanel);
       StateChangeEvent event = null;
       boolean showSearchNumber = false;
       query.displaySearchResult(morphoFrame, resultDisplayPanel, sort,
                             index, order, showSearchNumber, event);

    }//esle




  }//doQuery

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
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class CancelCommand
