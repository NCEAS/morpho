/**
 *  '$RCSfile: ImageRenderer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-14 16:05:56 $'
 * '$Revision: 1.1.2.4 $'
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
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renders the contents of a table image cell as a JLable with icon 
 */
public class ImageRenderer extends DefaultTableCellRenderer
{
 
  public static final String LOCALTOOLTIP = "Stored on my computer";
  public static final String METACATTOOLTIP = "Stored on network";
  public static final String BLANK = "Blank";
  public static final String PACKAGETOOLTIP = "Documentation only";
  public static final String PACKAGEDATATOOLTIP = "Data & documentation";
  /**
   * Create the renderer with appropriate formatting options set
   */
  public ImageRenderer() 
  {
    super();
  }

  /**
   * return a JLable as compent
   */
  public Component getTableCellRendererComponent( JTable table, Object value, 
                      boolean isSelected, boolean hasFocus, int row, int column) 
 {
   // Use a JLable as return value from super class
   JLabel comp 
    = (JLabel)super.getTableCellRendererComponent( table, value, 
                                            isSelected, hasFocus, row, column);
   // Make sure it is a icon cell
   if (!(value instanceof ImageIcon)) 
   {
    
    return comp;
   }//if
   // Set the icon to the label
   ImageIcon valueIcon = (ImageIcon)value;
   comp.setIcon(valueIcon);
   // Set text is empty
   comp.setText("");
   // Set icon in the center
   comp.setHorizontalAlignment(SwingConstants.CENTER);
   if ((valueIcon.getDescription())
                                .equals((ResultSet.localIcon).getDescription()))
   {
     // Add tooltip for localIcon
     comp.setToolTipText(LOCALTOOLTIP);
   }//if
   else if ((valueIcon.getDescription())
                          .equals((ResultSet.metacatIcon).getDescription()))
   {
     // Add tooltip for metacat icon
     comp.setToolTipText(METACATTOOLTIP);
   }//else
   else if ((valueIcon.getDescription())
                                .equals((ResultSet.blankIcon).getDescription()))
   {
     // Add null to blank icon
     comp.setToolTipText(null);
   }//else
   else if ((valueIcon.getDescription())
                              .equals((ResultSet.packageIcon).getDescription()))
   {
     // Add tool tip for package icon
     comp.setToolTipText(PACKAGETOOLTIP);
   }//else
   else if ((valueIcon.getDescription())
                         .equals((ResultSet.packageDataIcon).getDescription()))
   {
     // Add tool tip for package data icon
     comp.setToolTipText(PACKAGEDATATOOLTIP);
   }//else
   
   
    return comp;
  }

 
}
