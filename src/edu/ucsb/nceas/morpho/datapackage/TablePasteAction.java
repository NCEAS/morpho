/**
 *  '$RCSfile: TablePasteAction.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-09-05 18:16:59 $'
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
 * Class that implements the ability to paste data (e.g. data copied from
 * Excel) into a table
 */

package edu.ucsb.nceas.morpho.datapackage;
import java.awt.*;
import java.util.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class TablePasteAction extends AbstractAction {
	protected JTable target;

	public TablePasteAction() {
    super("Paste", null);
	}

	public TablePasteAction(JTable tbl) {
    super("Paste", null);
		setTarget(tbl);
	}

	public void setTarget(JTable tbl) {
		target = tbl;
	}

	public JTable getTarget() {
		return target;
	}

	public void actionPerformed(ActionEvent evt) {

		JTable tbl = target;
		if (tbl == null) {
			tbl = (JTable)evt.getSource();
		}

		Transferable t =
			tbl.getToolkit().getSystemClipboard().getContents(null);
		if (t == null) {
			// No usable content
			return;
		}

		try {
			String sel = (String)t.getTransferData(DataFlavor.stringFlavor);

			// Break the selection into rows and then into columns.
			StringTokenizer rowTokenizer = new StringTokenizer(sel, "\n");
			int rowCount = rowTokenizer.countTokens();
			String[][] data = new String[rowCount][];

			// One pass of this loop for each row
			for (int i = 0; i < rowCount; i++) {
				String row = rowTokenizer.nextToken();
				StringTokenizer colTokenizer = new StringTokenizer(row, "\t");
				int colCount = colTokenizer.countTokens();
				String[] cols = new String[colCount];
				data[i] = cols;

				// One pass of this loop for each column
				for (int j = 0; j < colCount; j++) {
					cols[j] = colTokenizer.nextToken();
				}
			}

			// Install the data in the table
			applySelection(tbl, data);
      ((AbstractTableModel)(tbl.getModel())).fireTableStructureChanged();
		} catch (Exception ex) {
			// Happens when clipboard content is not a string
		}
	}

	// Handles the entire selection as a two-dimensional array.
	// Subclasses may override this to provide custom processing.
	protected void applySelection(JTable tbl, String[][] data) {
		int row = tbl.getSelectedRow();
		int col = tbl.getSelectedColumn();
		int selRows = tbl.getSelectedRowCount();
		int selCols = tbl.getSelectedColumnCount();
		int dataRows = data.length;
		int dataCols = (dataRows > 0 ? data[0].length : 0);

		// If only one cell is selected, allow the paste
		// to operate on the part of the table at and
		// below and to the right of that cell. Otherwise,
		// bound the paste to the selected area
		if (selRows == 1 && selCols == 1) {
			selRows = tbl.getRowCount() - row;
			selCols = tbl.getColumnCount() - col;
		}

		// Limit the paste operation to the selection,
		// or to the table, whichever is the smaller.
		if (dataRows > selRows) {
			dataRows = selRows;
		}
		if (dataCols > selCols) {
			dataCols = selCols;
		}

		// Apply each item in turn
		for (int i = 0; i < dataRows; i++) {
			for (int j = 0; j < dataCols; j++) {
            	setValueAt(tbl, row + i, col + j, data[i][j]);
			}
		}

	}

	// Sets the value for a given row and column.
	// Subclasses may override this to carry out the
	// appropriate conversions for specific TableModels.
	protected void setValueAt(JTable tbl, int row, int column, String value) {
		tbl.setValueAt(value, row, column);
	}
}
