/**
 *  '$RCSfile: SearchCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-17 01:30:11 $'
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

package edu.ucsb.nceas.morpho.query;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.util.*;
import javax.swing.JDialog;

/**
 * Class to handle search command
 */
public class SearchCommand implements Command 
{
  /** A reference to the JDialogBox */
  private JDialog dialogBox = null;
  
  /** A reference to the Morpho application */
  private Morpho morpho = null;
  
  /**
   * Constructor of SearcCommand
   * @param myFrame the morpho which the cancel command will apply
   */
  public SearchCommand(JDialog myDialogBox, Morpho morpho)
  {
    dialogBox = myDialogBox;
    this.morpho = morpho;
  }//SearchCommand
  
  
  /**
   * execute cancel command
   */    
  public void execute()
  {
    // Hide and destory the dialogBox
    if ( dialogBox != null)
    {
      dialogBox.setVisible(false);
      dialogBox.dispose();
      dialogBox = null;
    }//if
    // QueryDialog Create and show as modal
    QueryDialog queryDialog = new QueryDialog(morpho);
    queryDialog.setModal(true);
    queryDialog.show();
    if (queryDialog.isSearchStarted()) 
    {
      Query query = queryDialog.getQuery();
      if (query != null) 
      {
        ResultSet rs = null;
        ResultFrame rsf = new ResultFrame(morpho, rs);
        rsf.addWorking();
        doQuery(rsf, query);

      }//if
    }//if
  }//execute

  /**
   * Run the search query
   */
  private void doQuery(final ResultFrame rsf, final Query query) 
  {
  
    final SwingWorker worker = new SwingWorker() 
    {
        ResultSet frs;
        public Object construct() 
        {
          frs = query.execute();
        
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() 
        {
          
          rsf.setTitle(frs.getQuery().getQueryTitle());
          rsf.setName(frs.getQuery().getQueryTitle());
          
          rsf.addResultPanel(frs);
        }
    };
    worker.start();  //required for SwingWorker 3
  }//doQuery
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
