/**
 *  '$RCSfile: ResultPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-02 21:39:02 $'
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Display a ResultSet in a table view in a panel that can be
 * embedded in a window or tab or other location
 */
public class ResultPanel extends javax.swing.JPanel
{
  private boolean DEBUG = true;

  public ResultPanel()
  {
    super();
    setLayout(new BorderLayout());
    setBackground(Color.white);
    ResultSet resultSet = new ResultSet();
    JTable table = new JTable(resultSet);
    table.setRowHeight(resultSet.getRowHeight());

    //Create the scroll pane and add the table to it. 
    JScrollPane scrollPane = new JScrollPane(table);

    //Add the scroll pane to this Panel.
    add(scrollPane, BorderLayout.CENTER);

    /*
    if (DEBUG)
    {
      table.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          printDebugData(table);
        }
      });
    }
    */
  }

/*
  private void printDebugData(JTable table)
  {
    int numRows = table.getRowCount();
    int numCols = table.getColumnCount();
      javax.swing.table.TableModel model = table.getModel();

      System.out.println("Value of data: ");
    for (int i = 0; i < numRows; i++)
    {
      System.out.print("    row " + i + ":");
      for (int j = 0; j < numCols; j++)
      {
	System.out.print("  " + model.getValueAt(i, j));
      }
      System.out.println();
    }
    System.out.println("--------------------------");
  }
*/

  public static void main(String[]args)
  {
    JFrame frame = new JFrame("SimpleTest");
    frame.setSize(new Dimension(700, 200));
    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);}
      }
    );
    ResultPanel results = new ResultPanel();
    frame.getContentPane().add(results);
    frame.pack();
    frame.setVisible(true);
  }

}
