/**
 *  '$RCSfile: DeleteColumnCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-27 03:51:00 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.event.ActionEvent;
import java.awt.Point;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Class to handle delete  a cloumn command
 */
public class DeleteColumnCommand implements Command 
{
  
  
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;

  
  /**
   * Constructor of Import data command
   */
  public DeleteColumnCommand()
  {
    
  }

  /**
   * execute insert command
   */    
  public void execute(ActionEvent event)
  {   
    DataViewContainerPanel resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null)
    {
       resultPane = AddDocumentationCommand.
                          getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    }//if
    
    // make sure resulPanel is not null
    if (resultPane != null)
    {
       DataViewer dataView = resultPane.getCurrentDataViewer();
       if (dataView != null)
       {
         // Get parameters and run it
         JTable jtable=dataView.getDataTable();
         PersistentTableModel ptmodel=(PersistentTableModel)jtable.getModel();
         PersistentVector vector=dataView.getPV();
         Document attributeDocoumnet=dataView.getAttributeDoc();
         Vector columnLabels=dataView.getColumnLabels();
         deleteColumn(jtable, ptmodel, vector, attributeDocoumnet,columnLabels);
       }
       
    }//if
  
  }//execute
  
  
  
  /* Method to insert a new column into table */
  private void deleteColumn(JTable table, PersistentTableModel ptm, 
                           PersistentVector pv, Document attributeDoc, 
                           Vector column_labels)
  {  
    int sel = table.getSelectedColumn();
    if (sel>-1) 
    {
      // remove the attribute node associated with the column
      NodeList nl = attributeDoc.getElementsByTagName("attribute");
      Node deleteNode = nl.item(sel);
      Node root = attributeDoc.getDocumentElement();
      root.removeChild(deleteNode);
          
      column_labels.removeElementAt(sel);
      ptm.deleteColumn(sel);
      pv = ptm.getPersistentVector();
      ptm.fireTableStructureChanged();    
    }
  
  }//insetColumn

 
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
