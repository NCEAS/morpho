/**
 *  '$RCSfile: ToolTippedTextRenderer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-14 16:47:56 $'
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

package edu.ucsb.nceas.morpho.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Renders the contents of a table cell with tool tip
 */
/*public class WrappedTextRenderer extends JTextArea
                                 implements TableCellRenderer*/
 public class ToolTippedTextRenderer extends DefaultTableCellRenderer
 {
  
  /**
   * Create the renderer with appropriate formatting options set
   */
  public ToolTippedTextRenderer(int fontSize) {
    super();
  
    setFont(new Font("Dialog", Font.PLAIN, fontSize));
  }

  /**
   * Return self as rendering components
   *
   */
  public Component getTableCellRendererComponent( JTable table, Object value, 
                                boolean isSelected, boolean hasFocus,
                                int row, int column) 
  {
    
    super.getTableCellRendererComponent
                            (table, value, isSelected, hasFocus,row, column);
      
    if (isSelected) {
      this.setBackground(table.getSelectionBackground());
      this.setForeground(table.getSelectionForeground());
    } else {
      this.setBackground(table.getBackground());
      this.setForeground(table.getForeground());
    }
    // add tooltip 
    if ( value != null )
    {
      setToolTipText(value.toString());
    }
    
    return this;
  }

 
}
