/**
 *  '$RCSfile: TableCutAction.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-09-05 21:52:08 $'
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

 /**
 * Class that copies a selection from a table to the system clipboard
 * as tab delimited text
 * code is same as TableCutAction except values copied are replaced by ""
 */

package edu.ucsb.nceas.morpho.datapackage;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.*;
import edu.ucsb.nceas.morpho.util.*;


public class TableCutAction extends AbstractAction { 
	protected JTable source;

	public TableCutAction() {
	}

	public TableCutAction(JTable tbl) {
    super("Cut", null);
		setSource(tbl);
	}

	public void setSource(JTable tbl) {
		source = tbl;
	}

	public JTable getSource() {
		return source;
	}

	public void actionPerformed(ActionEvent evt) {
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
	}

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
}
