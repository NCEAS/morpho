/**
 *  '$RCSfile: ResultFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-17 01:30:11 $'
 * '$Revision: 1.6 $'
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

import edu.ucsb.nceas.morpho.Morpho;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

/**
 * Display a ResultSet in a table view in window using an embedded
 * ResultPanel to construct the table.
 */
public class ResultFrame extends JFrame
{
  /** The panel used to display the results in a table */
  private ResultPanel resultDisplayPanel = null;

  /** A reference to the framework */
  Morpho morpho;
 
  ImageIcon flapping;
  
  /**
   * Construct a new ResultFrame and display the result set
   *
   * @param results the result listing to display
   */
  public ResultFrame(Morpho morpho, ResultSet results)
  {
    this(morpho, results, 12);
  }

  /**
   * Construct a new ResultFrame and display the result set
   *
   * @param results the result listing to display
   * @param fontSize the fontsize for the cells of the table
   */
  public ResultFrame(Morpho morpho, ResultSet results, int fontSize)
  {
    super();
    this.morpho = morpho;
    
       
    
    if (results!=null) {
      super.setTitle(results.getQuery().getQueryTitle());
      setName(results.getQuery().getQueryTitle());
    }
    setSize(700,600);
    setBackground(Color.white);
    
    try {
      flapping = new javax.swing.ImageIcon(getClass().getResource("Btfly4.gif"));
    } catch (Exception w) {
        System.out.println("Error in loading images");
    }
    

    // Create the result panel and add it to the frame
    if (results!=null) {
      resultDisplayPanel = new ResultPanel(results,true,true,fontSize, null);
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(resultDisplayPanel, BorderLayout.CENTER);
    }

    // Register a listener to watch for the window to close
    addWindowListener(new CloseListener());

    // Add to the frameworks list of windows, and show the frame
    //MBJframework.addWindow(this);
  
    setVisible(true);
  }

  /**
   * Hide the frame, dispose of it, notify the framework.
   */
  private void closeFrame()
  {
    //framework.removeWindow(this);
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
    //framework.removeWindow(this);
    //framework.addWindow(this);
  }

  public void addResultPanel(ResultSet results) {
      resultDisplayPanel = new ResultPanel(results, true, true, 12, null);
      resultDisplayPanel.setVisible(true);
      getContentPane().removeAll();
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(resultDisplayPanel, BorderLayout.CENTER);
      this.validate();
  }
  
  public void addWorking() {
      getContentPane().removeAll();
      getContentPane().setLayout(new BorderLayout());
      JLabel working = new JLabel("Working !!!");
      working.setForeground(Color.red);
      working.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 18));
      working.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
      working.setIcon(flapping);
      
      getContentPane().add(working, BorderLayout.CENTER);
      this.validate();
  }

}

