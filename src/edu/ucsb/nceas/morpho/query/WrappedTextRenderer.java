/**
 *  '$RCSfile: WrappedTextRenderer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-05 01:29:55 $'
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

package edu.ucsb.nceas.morpho.query;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 * Renders the contents of a table cell as a JTextArea to allow for multiple
 * lines that wrap within the cell.
 */
public class WrappedTextRenderer extends JTextArea
                                 implements TableCellRenderer
{
  /** the amount of space surronding text in the text area */
  private int margin = 5;

  /**
   * Create the renderer with appropriate formatting options set
   */
  public WrappedTextRenderer(int fontSize) {
    super();
    setMargin(new Insets(margin, margin, margin, margin));
    setLineWrap(true);
    setWrapStyleWord(true);
    setFont(new Font(null, Font.PLAIN, fontSize));
  }

  /**
   * Return self as rendering components, delegating the display 
   * mainly to the superclass JTextArea
   */
  public Component getTableCellRendererComponent( JTable table, Object value, 
                                boolean isSelected, boolean hasFocus,
                                int row, int column) {
    setText((String)value);
    if (isSelected) {
      setBackground(table.getSelectionBackground());
      setForeground(table.getSelectionForeground());
    } else {
      setBackground(table.getBackground());
      setForeground(table.getForeground());
    }
    return this;
  }

  /**
   * Get the preferred size for this text area
   */
  public Dimension getPreferredSize()
  {
    int assumedAverageCharacterWidth = 7;
    Dimension size = super.getPreferredSize();
    size.width = getText().length()*assumedAverageCharacterWidth;
    return size;
  }
}
