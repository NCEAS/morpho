/**
 *  '$RCSfile: ReviseSearchCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-22 00:09:48 $'
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
  public void execute()
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
      // Set butterfy busy
      morphoFrame.setBusy(true);
      // Get ResultPanel
      ResultPanel resultPane = (ResultPanel) comp;
      // Get  result set of the result panel
      ResultSet results = resultPane.getResultSet();
      // Save the original identifier
      String identifier = results.getQuery().getIdentifier();

      // QueryDialog Create and show as modal
      QueryDialog queryDialog1 = null;
      queryDialog1 = new QueryDialog(morphoFrame, morpho);
     
      queryDialog1.setQuery(results.getQuery());
      queryDialog1.setModal(true);
      queryDialog1.show();
      if (queryDialog1.isSearchStarted()) 
      {
        Query query = queryDialog1.getQuery();
        if (query != null) 
        {
          //query.setIdentifier(identifier);
          ResultSet newResults = query.execute();
          ResultPanel resultDisplayPanel = new ResultPanel(
              newResults, 12, null, morphoFrame.getDefaultContentAreaSize());
          resultDisplayPanel.setVisible(true);
          morphoFrame.setMainContentPane(resultDisplayPanel);
          morphoFrame.setMessage(newResults.getRowCount()+ " data sets found");
          
          // If the user change the identifier of the query
          // we need change the title of the frame and update window menu
          String currentIdentifier = query.getIdentifier();
          if ( currentIdentifier != null && currentIdentifier.equals("") 
                           && !currentIdentifier.equals(identifier))
          {
            // Change the title of the frame
            morphoFrame.setTitle(currentIdentifier);
            // Update the windows menu
          }
          morphoFrame.setBusy(false);
        }
      }
    }//if
      
 
  }//execute

 
  
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
