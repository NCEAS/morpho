/**
 *  '$RCSfile: ResultFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-07-13 17:29:03 $'
 * '$Revision: 1.3.2.1 $'
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

import edu.ucsb.nceas.morpho.framework.ClientFramework;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Display a ResultSet in a table view in window using an embedded
 * ResultPanel to construct the table.
 */
public class ResultFrame extends JFrame
{
  /** The panel used to display the results in a table */
  private ResultPanel resultDisplayPanel = null;

  /** A reference to the framework */
  ClientFramework framework;

  /**
   * Construct a new ResultFrame and display the result set
   *
   * @param results the result listing to display
   */
  public ResultFrame(ClientFramework cf, ResultSet results)
  {
    this(cf, results, 12);
  }

  /**
   * Construct a new ResultFrame and display the result set
   *
   * @param results the result listing to display
   * @param fontSize the fontsize for the cells of the table
   */
  public ResultFrame(ClientFramework cf, ResultSet results, int fontSize)
  {
    super();
    this.framework = cf;

    super.setTitle(results.getQuery().getQueryTitle());
    setName(results.getQuery().getQueryTitle());
    setSize(700,600);
    setBackground(Color.white);

    // Create the result panel and add it to the frame
    resultDisplayPanel = new ResultPanel(results, true, true, fontSize);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(resultDisplayPanel, BorderLayout.CENTER);

    // Register a listener to watch for the window to close
    addWindowListener(new CloseListener());

    // Add to the frameworks list of windows, and show the frame
    framework.addWindow(this);
    setVisible(true);
  }

  /**
   * Hide the frame, dispose of it, notify the framework.
   */
  private void closeFrame()
  {
    framework.removeWindow(this);
    setVisible(false);
    dispose();
  }

  /**
   * Listen for window closing events
   */
  private class CloseListener extends WindowAdapter
  {
    public void windowClosing(WindowEvent event)
    {
      Object object = event.getSource();
      if (object == ResultFrame.this)
        closeFrame();
    }
  }
 
  /**
   * Override setTitle to update the name and framework menus as well
   */
  public void setTitle(String title) 
  {
    super.setTitle(title);
    setName(title);
    framework.removeWindow(this);
    framework.addWindow(this);
  }
}
