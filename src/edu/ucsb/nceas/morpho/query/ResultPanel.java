/**
 *  '$RCSfile: ResultPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-05 01:29:55 $'
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

package edu.ucsb.nceas.morpho.query;

import java.awt.*;
import java.awt.event.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 * Display a ResultSet in a table view in a panel that can be
 * embedded in a window or tab or other location
 */
public class ResultPanel extends JPanel
{
  private ResultSet results = null;

  /**
   * Construct a new ResultPanel and display the result set
   *
   * @param results the result listing to display
   */
  public ResultPanel(ResultSet results)
  {
    this(results, 12);
  }

  /**
   * Construct a new ResultPanel and display the result set
   *
   * @param results the result listing to display
   * @param fontSize the fontsize for the cells of the table
   */
  public ResultPanel(ResultSet results, int fontSize)
  {
    super();
    this.results = results;
    setLayout(new BorderLayout());
    setBackground(Color.white);

    if (results != null) {
      // Set up the Header panel with a title
      JLabel titleLabel = new JLabel(results.getQuery().getQueryTitle());
      JPanel headerPanel = new JPanel();
      //headerPanel.setBackground(Color.white);
      headerPanel.add(titleLabel);
      add(headerPanel, BorderLayout.NORTH);
  
      // Set up the results table
      JTable table = new JTable(results);
      WrappedTextRenderer stringRenderer = new WrappedTextRenderer(fontSize);
      stringRenderer.setRows(5);
      table.setRowHeight((int)(stringRenderer.getPreferredSize().getHeight()));
      //table.setRowHeight(results.getRowHeight());
      table.setDefaultRenderer(String.class, stringRenderer);
      initColumnSizes(table, results);
  
      //Create the scroll pane and add the table to it. 
      JScrollPane scrollPane = new JScrollPane(table);
  
      //Add the scroll pane to this Panel.
      add(scrollPane, BorderLayout.CENTER);
      
      /*
        table.addMouseListener(new MouseAdapter()
        {
          public void mouseClicked(MouseEvent e)
          {
            printDebugData(table);
          }
        });
      */
    }
  }

  /*
   * This method picks good column sizes.
   * If all column heads are wider than the column's cells' 
   * contents, then you can just use column.sizeWidthToFit().
   */
  private void initColumnSizes(JTable table, ResultSet results) {
    TableColumn column = null;
    Component comp = null;
    Component hcomp = null;
    int headerWidth = 0;
    int cellWidth = 0;
    Object[] longValues = null;;

    for (int i = 0; i < results.getColumnCount(); i++) {
      column = table.getColumnModel().getColumn(i);
      hcomp = (Component)column.getHeaderRenderer();
      if (hcomp != null) {
        headerWidth = hcomp.getPreferredSize().width;
      }

      comp = table.getDefaultRenderer(results.getColumnClass(i)).
                 getTableCellRendererComponent(
                 table, results.getValueAt(1, i),
                 false, false, 0, i);
      cellWidth = comp.getPreferredSize().width;
      System.err.println("Column (Width): " + i + 
                         " (" + cellWidth + "/" + headerWidth + ")");
      column.setPreferredWidth(Math.max(headerWidth, cellWidth));
      if (cellWidth < 100 && i > 0) {
        column.setMinWidth(100);
      }
    }
  } 
}
