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

import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.w3c.dom.Document;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;

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
    
    int opt = JOptionPane.showConfirmDialog(morphoFrame,
            /*"Are you sure that you want to delete the selected data column?"*/ Language.getInstance().getMessage("DeleteColumnWarning"),
            /*"DO YOU WANT TO CONTINUE?"*/ Language.getInstance().getMessage("Warning_Continue"),
            JOptionPane.YES_NO_OPTION);
   if (opt == JOptionPane.NO_OPTION) 
   {
        return;
    }

    // make sure resulPanel is not null
    if (resultPane != null)
    {
       DataViewer dataView = resultPane.getCurrentDataViewer();
       if (dataView != null)
       {
         // Get parameters and run it
         MorphoDataPackage mdp = dataView.getMorphoDataPackage();
         int entityIndex = dataView.getEntityIndex();
         JTable jtable=dataView.getDataTable();
         PersistentTableModel ptmodel=(PersistentTableModel)jtable.getModel();
         PersistentVector vector=dataView.getPV();
         Document attributeDocument=dataView.getAttributeDoc();
         Vector columnLabels=dataView.getColumnLabels();
         deleteColumn(jtable, ptmodel, vector, mdp, entityIndex, columnLabels);
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
                           PersistentVector pv, MorphoDataPackage mdp,
                           int entityIndex, Vector column_labels)
  {
	  int viewIndex = table.getSelectedColumn();
	  int sel =  table.getColumnModel().getColumn(viewIndex).getModelIndex();
    if (sel>-1)
    {
    	AbstractDataPackage adp = mdp.getAbstractDataPackage();
      adp.deleteAttribute(entityIndex, sel);

      column_labels.removeElementAt(sel);
      ptm.deleteColumn(sel);
      pv = ptm.getPersistentVector();

      ptm.fireTableStructureChanged();

    }
  }

  /**
   * could also have undo functionality; disabled for now
   */
  // public void undo();

}//class DeleteColumnCommand
