/**
 *  '$RCSfile: DeleteColumnCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-18 02:21:40 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;

import java.util.Vector;

import java.awt.event.ActionEvent;

import javax.swing.JTable;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Class to handle delete  a column command
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
   *
   * @param event ActionEvent
   */
  public void execute(ActionEvent event)
  {
    DataViewContainerPanel resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null)
    {
       resultPane =  morphoFrame.getDataViewContainerPanel();
    }//if

    // make sure resulPanel is not null
    if (resultPane != null)
    {
       DataViewer dataView = resultPane.getCurrentDataViewer();
       if (dataView != null)
       {
         // Get parameters and run it
         AbstractDataPackage adp = dataView.getAbstractDataPackage();
         int entityIndex = dataView.getEntityIndex();
         JTable jtable=dataView.getDataTable();
         PersistentTableModel ptmodel=(PersistentTableModel)jtable.getModel();
         PersistentVector vector=dataView.getPV();
         Document attributeDocument=dataView.getAttributeDoc();
         Vector columnLabels=dataView.getColumnLabels();
         deleteColumn(jtable, ptmodel, vector, adp, entityIndex, columnLabels);
         dataView.setPV(ptmodel.getPersistentVector());
       }

    }//if

  }//execute


  /**
   * Method to delete a column into table eml2.0.0 version (Morpho 1.5 +)
   *
   * @param table JTable
   * @param ptm PersistentTableModel
   * @param pv PersistentVector
   * @param adp AbstractDataPackage
   * @param entityIndex int
   * @param column_labels Vector
   */
  private void deleteColumn(JTable table, PersistentTableModel ptm,
                           PersistentVector pv, AbstractDataPackage adp,
                           int entityIndex, Vector column_labels)
  {
    int sel = table.getSelectedColumn();
    if (sel>-1)
    {
      adp.deleteAttribute(entityIndex, sel);

      column_labels.removeElementAt(sel);
      ptm.deleteColumn(sel);
      pv = ptm.getPersistentVector();

      ptm.fireTableStructureChanged();

    }
  }


  /**
   * Method to delete a column into table This is the method used for Morpho
   * version 1.4 and earlier
   *
   * @param table JTable
   * @param ptm PersistentTableModel
   * @param pv PersistentVector
   * @param attributeDoc Document
   * @param column_labels Vector
   */
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

  }//deleteColumn


  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class DeleteColumnCommand
