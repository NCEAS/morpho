/**
 *  '$RCSfile: ImageRenderer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-09 01:10:11 $'
 * '$Revision: 1.1.2.1 $'
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
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders the contents of a table cell as a JTextArea to allow for multiple
 * lines that wrap within the cell.
 */
public class ImageRenderer extends DefaultTableCellRenderer
{
 
  /**
   * Create the renderer with appropriate formatting options set
   */
  public ImageRenderer() 
  {
    super();
  }

  /**
   * Return self as rendering components, delegating the display 
   * mainly to the superclass JTextArea
   */
  public Component getTableCellRendererComponent( JTable table, Object value, 
                      boolean isSelected, boolean hasFocus, int row, int column) 
 {

   JLabel comp 
    = (JLabel)super.getTableCellRendererComponent( table, value, 
                                            isSelected, hasFocus, row, column);
   if (!(value instanceof Icon)) {
    System.out.println("##########value instanceof "+value);
    return comp;
   }
   comp.setIcon((Icon)value);
   comp.setText("");
   if (value.equals(ResultSet.localIcon))
   {
        System.out.println("local");
        comp.setToolTipText("Stored on my computer");
   }
   else if (value.equals(ResultSet.metacatIcon))
   {
        System.out.println("net");
        comp.setToolTipText("Stored on network");
    }
   
    
    return comp;
  }

 
}
