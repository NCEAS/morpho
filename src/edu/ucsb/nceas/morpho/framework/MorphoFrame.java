/**
 *  '$RCSfile: MorphoFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-08 22:19:33 $'
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

package edu.ucsb.nceas.morpho.framework;

import edu.ucsb.nceas.morpho.util.Log;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.lang.reflect.*;
import java.lang.ClassCastException;
import java.net.*;

/**
 * The MorphoFrame is a Window in the Morpho application containing the 
 * standard menus and toolbars.  Overall state of the application is
 * synchronized across MorphoFrames so that when the UI changes do a 
 * user action it is propogated to all frames as appropriate.  Each plugin
 * can create a MorphoFrame by asking the UIController for a new
 * instance.
 */
public class MorphoFrame extends JFrame 
{
  JPanel toolbarPanel    = new JPanel();
  JToolBar morphoToolbar = new JToolBar();
  JMenuBar morphoMenuBar = new JMenuBar();

  private StatusBar statusBar;
  
  /**
   * Creates a new instance of MorphoFrame
   */
  public MorphoFrame(ConfigXML config)
  {
		setJMenuBar(morphoMenuBar);
		setTitle("Morpho - Data Management for Ecologists");
		setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(0,0);
		setVisible(false);
		toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		getContentPane().add(BorderLayout.NORTH, toolbarPanel);
		morphoToolbar.setAlignmentY(0.222222F);
		toolbarPanel.add(morphoToolbar);

    // get StatusBar instance, initialize and add to interface:
    statusBar = StatusBar.getInstance();
	getContentPane().add(BorderLayout.SOUTH, statusBar);
  
    // Register listeners
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);

    pack();
  }

  /**
   * Set the content pane of the main Morpho window to display the
   * component indicated.  Note that this will replace the current content
   * pane, and so only one plugin should call this routine.
   *
   * @param comp the component to display
   */
  public void setMainContentPane(Component comp) 
  {
    // Create a panel to display the plugin if requested
    if (comp != null) {
      getContentPane().add(BorderLayout.CENTER, comp);
      comp.invalidate();
      invalidate();
    } else {
      Log.debug(5, "Component was null so I could not set it!");
    }
  }

  /**
   * close the window when requested
   */
  private void close()
  {
        this.setVisible(false);        // hide the Frame
        this.dispose();                // free the system resources
  }

  /** Listen for window closing events */
  class SymWindow extends java.awt.event.WindowAdapter
  {
    public void windowClosing(java.awt.event.WindowEvent event)
    {
      Object object = event.getSource();
      if (object == MorphoFrame.this)
          MorphoFrame_windowClosing(event);
    }
  }

  /** process window closing events */
  private void MorphoFrame_windowClosing(java.awt.event.WindowEvent event)
  {
    // to do: code goes here.
    MorphoFrame_windowClosing_Interaction1(event);
  }

  /** process window closing events */
  private void MorphoFrame_windowClosing_Interaction1(java.awt.
                                                  event.WindowEvent event)
  {
      this.close();
  }
}
