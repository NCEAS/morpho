/**
 *  '$RCSfile: SortableJTable.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-16 16:49:01 $'
 * '$Revision: 1.2 $'
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
package edu.ucsb.nceas.morpho.util;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * A classs represent sortable table
 */
public class SortableJTable extends JTable implements MouseListener
{
  // Index of column which need to be sorted
  protected int sortedColumnIndex = -1;
  // a column sorted by ascending or not
  protected boolean sortedColumnAscending = true;
  
 /**
  * Constructor of SortableJTable
  * @param model ColumnSortableTableModel
  */
  public SortableJTable(ColumnSortableTableModel model)
  {
    super(model);
    initSortHeader();
  }

  /**
  * Constructor of SortableJTable
  * @param model ColumnSortableTableModel
  * @param colModel colModel for table
  */
  public SortableJTable(ColumnSortableTableModel model,
    TableColumnModel colModel)
  {
    super(model, colModel);
    initSortHeader();
  }

  /**
   * Initialize sortable
   * set header renderer as SortableTableHeaderCellRender
   * add mouseListener to the header 
   */
  private void initSortHeader()
  {
    JTableHeader header = getTableHeader();
    header.setDefaultRenderer(new SortableTableHeaderCellRenderer());
    header.addMouseListener(this);
  }
  
  /**
   * Get the index of Sorted column
   */
  public int getSortedColumnIndex()
  {
    return sortedColumnIndex;
  } 
  
  /**
   * Get the sorted table oder statuts
   */
  public boolean isSortedColumnAscending()
  {
    return sortedColumnAscending;
  }
  
  /**
   * Mouse click event handler
   */
  public void mouseClicked(MouseEvent event) 
  {
    TableColumnModel colModel = getColumnModel();
    int index = colModel.getColumnIndexAtX(event.getX());
    int modelIndex = colModel.getColumn(index).getModelIndex();
    ColumnSortableTableModel model = (ColumnSortableTableModel)getModel();
  
     // toggle ascension, if already sorted
     if (sortedColumnIndex == index)
     {
        sortedColumnAscending = !sortedColumnAscending;
     }
     sortedColumnIndex = index;
     // Sort table
     model.sortTableByColumn(modelIndex, sortedColumnAscending);
     validate();
  }
  public void mouseReleased(MouseEvent event)
  {}
  
  public void mousePressed(MouseEvent event) {}
  public void mouseEntered(MouseEvent event) {}
  public void mouseExited(MouseEvent event) {}
  
}

