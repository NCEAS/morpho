/**
 *  '$RCSfile: SortableTableHeaderCellRenderer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-09 01:17:32 $'
 * '$Revision: 1.1.2.2 $'
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
import javax.swing.*;
import javax.swing.table.*;

/**
 * The class for Renderer of sortable table header cell
 */
public class SortableTableHeaderCellRenderer extends DefaultTableCellRenderer
{
  // The icon for nonsorted in table header cell
  private Icon NONSORTED = null;
  // The icon for ascending order in table header cell
  private  Icon ASCENDING = null;  
  // The icon for decending order in table header cell
  private  Icon DECENDING = null;
    
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
    int index = -1;
    boolean ascending = true;
    if (table instanceof SortableJTable)
    {
      SortableJTable sortTable = (SortableJTable)table;
      index = sortTable.getSortedColumnIndex();
      ascending = sortTable.isSortedColumnAscending();
    }
    if (table != null)
    {
      JTableHeader header = table.getTableHeader();
      if (header != null)
      {
        setForeground(header.getForeground());
        setBackground(header.getBackground());
        setFont(header.getFont());
      }
    }
    Icon icon = ascending ? ASCENDING : DECENDING;
    setIcon(col == index ? icon : NONSORTED);
    setText((value == null) ? "" : value.toString());
    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    return this;
  }
}

