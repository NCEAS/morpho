/**
 *  '$RCSfile: ToolTippedSortableJTable.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-13 00:22:13 $'
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
package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.util.ColumnSortableTableModel;
import edu.ucsb.nceas.morpho.util.SortableJTable;
import java.awt.Component;
import java.awt.event.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.util.*;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * A classs represent sortable table
 */
public class ToolTippedSortableJTable extends SortableJTable
{
  
 /**
  * Constructor of ToolTippedSortableJTable
  * @param model ColumnSortableTableModel
  */
  public ToolTippedSortableJTable(ColumnSortableTableModel model)
  {
    super(model);
  
  }

  /**
  * Constructor of ToolTippedSortableJTable
  * @param model ColumnSortableTableModel
  * @param colModel colModel for table
  */
  public ToolTippedSortableJTable(ColumnSortableTableModel model,
    TableColumnModel colModel)
  {
    super(model, colModel);
   
  }
  
  /**
   * In order to only show tool tion of the cell which is elided,
   * getToolTipText will be overwritten
   * @param event mouseevent for getting tool tip
   */
  public String getToolTipText(MouseEvent event) 
  {
    // String to store the tool tip
    String tip = null;
    // Get the tool tip by calling parent method
    tip = super.getToolTipText(event);
    // Width of cell
    int widthOfCell = 0;
    // Width of string in the cell
    int widthOfString = 0;
      
    // if it is icon tip just return it
    if ( tip.equals(ImageRenderer.LOCALTOOLTIP) 
                              || tip.equals(ImageRenderer.METACATTOOLTIP))
    {
      return tip;
    }
    else //It is for stirng
    {
      // Get the event happened points
      Point p = event.getPoint();
      // Locate the renderer under the event location
      int hitColumnIndex = columnAtPoint(p);
      int hitRowIndex = rowAtPoint(p);
      // Get the column of the point
      TableColumn column = this.getColumnModel().getColumn(hitColumnIndex);
      // Get the width of the column
      widthOfCell = column.getWidth();
     
      // Caculate the length of string in the cell
      widthOfString = caculateStringLength(tip);
      // if width of cell is shoter than width of tip rendered by table renderer
      if (widthOfCell <= (widthOfString+2))
      {
        return tip;
      }//if
      else
      {
        tip = null;
        return tip;
      }//else
    }//else
  }//getTooLTipText
  
  /**
   * Caculate the length of String
   */
  private int caculateStringLength(String str)
  {
    // Store the width of string
    int width = 0;
    
    // if Str is not null or empty, caculate the width
    if ( str != null || !str.equals(""))
    {
      // Font for string
      Font font 
      =((DefaultTableCellRenderer) getDefaultRenderer(String.class)).getFont();
      // Get FontMatrices base the lable
      FontMetrics metrics = getFontMetrics(font);
      // Get width of the string base on this FontMetrics
      width = metrics.stringWidth(str);
      
    }//if
    
    return width;
      
  }//caculateStringLenth
  

}//ToolTippedSortableJTable

