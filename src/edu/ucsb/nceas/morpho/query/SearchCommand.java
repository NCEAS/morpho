/**
 *  '$RCSfile: SearchCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-15 18:34:45 $'
 * '$Revision: 1.3 $'
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
 * Class to handle search command
 */
public class SearchCommand implements Command 
{
  /** A reference to the JDialogBox */
  private JDialog dialogBox = null;
  
  /** A reference to the clientframework*/
  private ClientFramework frame = null;
  
  /**
   * Constructor of SearcCommand
   * @param myFrame the frame which the cancel command will apply
   */
  public SearchCommand(JDialog myDialogBox, ClientFramework myFrame)
  {
    dialogBox = myDialogBox;
    frame = myFrame;
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
      dialogBox = null;
    }//if
    // QueryDialog Create and show as modal
    QueryDialog queryDialog = new QueryDialog(frame);
    queryDialog.setModal(true);
    queryDialog.show();
    if (queryDialog.isSearchStarted()) 
    {
      Query query = queryDialog.getQuery();
      if (query != null) 
      {
        ResultSet rs = null;
        ResultFrame rsf = new ResultFrame(frame, rs);
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
