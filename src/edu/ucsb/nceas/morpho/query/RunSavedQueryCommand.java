/**
 *  '$RCSfile: RunSavedQueryCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-04-13 06:17:17 $'
 * '$Revision: 1.2.6.1 $'
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
import java.awt.event.ActionEvent;
import javax.swing.JDialog;

/**
 * Class to handle click a saved query menu item
 */
public class RunSavedQueryCommand implements Command
{

  /** A reference to the query */
  private Query query;


  /**
   * Constructor of SearcCommand
   * @param myQuery the query stored in this save query menu item
   */
  public RunSavedQueryCommand(Query myQuery)
  {
    query = myQuery;
  }//SearchCommand

  /**
   * Set query to this object
   *
   * @param myQuery the new query for the RunSaveQueryCommand
   */
  public void setQuery(Query myQuery)
  {
    query = myQuery;
  }

  /**
   * Get query from the object
   */
  public Query getQuery()
  {
    return query;
  }


  /**
   * execute cancel command
   */
  public void execute(ActionEvent event)
  {

      if (query != null)
      {
        MorphoFrame resultWindow = UIController.getInstance().addWindow(
                query.getQueryTitle());
        resultWindow.setBusy(true);
        resultWindow.setVisible(true);
        //SearchCommand.doQuery(resultWindow, query);
        //true means sored, 5 means sored column index
        // second true means send state change event or not.
        SearchCommand.doQuery(resultWindow, query, true, 5,
                              SortableJTable.DECENDING, true);
      }//if
      else
      {
         Log.debug(6, "There is no query associate this menu item!");
      }

  }//execute


  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class RunSavedQueryCommand
