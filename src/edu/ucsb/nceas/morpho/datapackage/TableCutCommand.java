/**
 *  '$RCSfile: TableCutCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-27 16:12:15 $'
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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.AbstractTableModel;
import org.w3c.dom.Document;


/**
 * Class to handle table paste command
 */
public class TableCutCommand implements Command 
{
  /* target table*/
  protected JTable source;
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;
  
  /**
   * Constructor of paste command
   */
  public TableCutCommand()
  {
    
  }

  
	public void setSource(JTable tbl) {
		source = tbl;
	}

	public JTable getSource() {
		return source;
	}

  /**
   * execute paste command
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
       if (dataView != null && dataView.getTextFlag())
       {
         // Get parameters and run it
         JTable jtable=dataView.getDataTable();
         if (jtable != null)
         {
          setSource(jtable);
          cut(event);
         }
       }
       
    }//if
  
  }//execute
  
  
  
  /* Method to insert a new row into table */
  private void cut(ActionEvent evt)
  {  
   JTable tbl = source;

		if (tbl == null) {
			// Use event source
			tbl = (JTable)evt.getSource();
		}
    
		
		int minRow = -1;
		int maxRow = -1;
		int minCol = -1;
		int maxCol = -1;

		ListSelectionModel rowModel = tbl.getSelectionModel();
		ListSelectionModel colModel = tbl.getColumnModel().getSelectionModel();

		if (tbl.getCellSelectionEnabled()) {
			minRow = rowModel.getMinSelectionIndex();
			maxRow = rowModel.getMaxSelectionIndex();
			minCol = colModel.getMinSelectionIndex();
			maxCol = colModel.getMaxSelectionIndex();
		} else if (tbl.getRowSelectionAllowed()) {
			minRow = rowModel.getMinSelectionIndex();
			maxRow = rowModel.getMaxSelectionIndex();
			minCol = 0;
			maxCol = tbl.getColumnCount() - 1;
		} else if (tbl.getColumnSelectionAllowed()) {
			minRow = 0;
			maxRow = tbl.getRowCount() - 1;
 			minCol = colModel.getMinSelectionIndex();
			maxCol = colModel.getMaxSelectionIndex();
		}

		if (minRow == -1 || minCol == -1) {
			// No selections!
			return;
		}

		boolean isEmpty = true;
		StringBuffer sel = new StringBuffer();
		for (int row = minRow; row <= maxRow; row++) {
			for (int col = minCol; col <= maxCol; col++) {
				if (tbl.isCellSelected(row, col)) {
					if (col == minCol && row != minRow) {
						sel.append('\n');
					} else if (col != minCol) {
						sel.append('\t');
					}
					sel.append(getValueAt(tbl, row, col));
          clearValueAt(tbl, row, col);
					isEmpty = false;
				}
			}
		}

    // the following only work on tables with tableModels that implement
    // AbstrctTableModel interface; without it an update of the display
    // will not occur
    ((AbstractTableModel)(tbl.getModel())).fireTableStructureChanged();
    
    
		if (!isEmpty) {
			tbl.getToolkit().getSystemClipboard().setContents(
					new StringSelection(sel.toString()), null);
		}
  }//insert row

 

	// Gets the value for a given row and column.
	// Subclasses may override this to carry out the
	// appropriate conversions for specific TableModels.
	protected String getValueAt(JTable tbl, int row, int col) {
		return tbl.getValueAt(row, col).toString();
	}
  
  // Sets the value for a given row and column to an empty string.
	// Subclasses may override this to carry out the
	// appropriate conversions for specific TableModels.
	protected void clearValueAt(JTable tbl, int row, int col) {
		tbl.setValueAt("",row, col) ;
	}

  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
