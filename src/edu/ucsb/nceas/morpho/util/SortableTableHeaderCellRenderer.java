/**
 *  '$RCSfile: SortableTableHeaderCellRenderer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-06 01:29:42 $'
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
package edu.ucsb.nceas.morpho.util;

import java.awt.Component;
import java.awt.Font;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
//import javax.swing.table.*;

/**
 * The class for Renderer of sortable table header cell
 */
public class SortableTableHeaderCellRenderer extends DefaultTableCellRenderer
{
  // The icon for nonsorted in table header cell
  private ImageIcon NONSORTED = null;
  // The icon for ascending order in table header cell
  private  ImageIcon ASCENDING = null;  
  // The icon for decending order in table header cell
  private  ImageIcon DECENDING = null;
    
  // Font size                 
  private static final int FONTSIZE = 12;
  /**
   * Consturctor of SortableHeaderCellRender
   */
  public SortableTableHeaderCellRenderer()
  {
    ASCENDING = new ImageIcon(getClass().getResource("ascendingArrow.gif"));
    DECENDING = new ImageIcon(getClass().getResource("decendingArrow.gif"));
    //setMargin(new Insets(MARGIN, MARGIN, MARGIN, MARGIN));
    setFont(new Font("Dialog", Font.PLAIN, FONTSIZE));
    // Set text in the left of the arrow icon
    setHorizontalTextPosition(SwingConstants.LEFT);
  }
  
  /**
   * Method to return table cell render
   * @param table the JTable
   * @param value the value to assign to the cell
   * @param isSelected true if the cell is selected
   * @param isFocus true if the cell has focus
   * @param row the row of the cell to render
   * @param the column of the cell to render
   */
  public Component getTableCellRendererComponent( JTable table, Object value, 
                          boolean isSelected,boolean hasFocus, int row, int col)
  {
    // Index of sorted column
    int indexOfSortedColumn = -1;
    // sorted or not
    boolean sorted = false;
    // order of sorting
    String order = null;
    SortableJTable sortTable = null;
    ImageIcon shownIcon = null;
    // Set icon for sorted column
    if (table instanceof SortableJTable)
    { 
      // Casting
      sortTable = (SortableJTable)table;
      // Get sorted or not
      sorted = sortTable.getSorted();
      // If not sorted
      if (!sorted)
      {
        setIcon(NONSORTED);
      }
      else
      {
         // Get index of sortedColumn
        indexOfSortedColumn = sortTable.getIndexOfSortedColumn();
        // Get order of sorted
        order = sortTable.getOrderOfSortedColumn();
        if (order.equals(SortableJTable.ASCENDING))
        {
          shownIcon = ASCENDING;
        }
        else
        {
          shownIcon = DECENDING;
        }
        // set icon
        if (col == indexOfSortedColumn)
        {
          setIcon(shownIcon);
        }
        else
        {
          setIcon(NONSORTED);
        }
      }//else
    }//if 
    // Set text for header
    if (value == null)
    {
      setText("");
    }
    else
    {
      setText(value.toString());
    }
    // SetBorder
    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    return this;
  }
}

