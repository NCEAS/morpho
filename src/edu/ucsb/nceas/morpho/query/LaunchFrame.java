/**
 *       Name: LaunchFrame.java
 *    Purpose: Used to test QueryDialog
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-05-18 23:17:59 $'
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
import javax.swing.*;
import com.symantec.itools.javax.swing.JToolBarSeparator;
import com.symantec.itools.javax.swing.icons.ImageIcon;

import edu.ucsb.nceas.morpho.framework.*;

/**
 * A test window for launching the QueryDialog
 */
public class LaunchFrame extends javax.swing.JFrame
{
	public LaunchFrame()
	{
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setTitle("JFC Application");
		setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(271,121);
		setVisible(false);
		JPanel1.setLayout(null);
		getContentPane().add(BorderLayout.CENTER, JPanel1);
		JButton1.setText("Show Query");
		JButton1.setActionCommand("Show Query");
		JPanel1.add(JButton1);
		JButton1.setBounds(84,21,111,27);
		//}}

		//{{INIT_MENUS
		//}}

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		JButton1.addActionListener(lSymAction);
		//}}
	}

    /**
     * Creates a new instance of JFrame1 with the given title.
     * @param sTitle the title for the new frame.
     * @see #JFrame1()
     */
	public LaunchFrame(String sTitle)
	{
		this();
		setTitle(sTitle);
	}
	
	/**
	 * The entry point for this application.
	 * Sets the Look and Feel to the System Look and Feel.
	 * Creates a new JFrame1 and makes it visible.
	 */
	static public void main(String args[])
	{
		try {
		    // Add the following code if you want the Look and Feel
		    // to be set to the Look and Feel of the native system.
		    /*
		    try {
		        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    } 
		    catch (Exception e) { 
		    }
		    */

			//Create a new instance of our application's frame, and make it visible.
			(new LaunchFrame()).setVisible(true);
		} 
		catch (Throwable t) {
			t.printStackTrace();
			//Ensure the application exits with an error condition.
			System.exit(1);
		}
	}

    /**
     * Notifies this component that it has been added to a container
     * This method should be called by <code>Container.add</code>, and 
     * not by user code directly.
     * Overridden here to adjust the size of the frame if needed.
     * @see java.awt.Container#removeNotify
     */
	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();
		
		super.addNotify();
		
		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;
		
		// Adjust size of frame according to the insets and menu bar
		javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
		int menuBarHeight = 0;
		if (menuBar != null)
		    menuBarHeight = menuBar.getPreferredSize().height;
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JButton JButton1 = new javax.swing.JButton();
	//}}

	//{{DECLARE_MENUS
	//}}

	void exitApplication()
	{
		try {
	    	// Beep
	    	Toolkit.getDefaultToolkit().beep();
	    	// Show a confirmation dialog
	    	int reply = JOptionPane.showConfirmDialog(this, 
	    	                                          "Do you really want to exit?", 
	    	                                          "JFC Application - Exit" , 
	    	                                          JOptionPane.YES_NO_OPTION, 
	    	                                          JOptionPane.QUESTION_MESSAGE);
			// If the confirmation was affirmative, handle exiting.
			if (reply == JOptionPane.YES_OPTION)
			{
		    	this.setVisible(false);    // hide the Frame
		    	this.dispose();            // free the system resources
		    	System.exit(0);            // close the application
			}
		} catch (Exception e) {
		}
	}

	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == LaunchFrame.this)
				LaunchFrame_windowClosing(event);
		}
	}

	void LaunchFrame_windowClosing(java.awt.event.WindowEvent event)
	{
		// to do: code goes here.
			 
		LaunchFrame_windowClosing_Interaction1(event);
	}

	void LaunchFrame_windowClosing_Interaction1(java.awt.event.WindowEvent event) {
		try {
			this.exitApplication();
		} catch (Exception e) {
		}
	}

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == JButton1)
				JButton1_actionPerformed(event);
		}
	}

	void JButton1_actionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
			 
		JButton1_actionPerformed_Interaction1(event);
	}

	void JButton1_actionPerformed_Interaction1(java.awt.event.ActionEvent event)
	{
		try {
			// QueryDialog Create and show as non-modal
			{
				QueryDialog QueryDialog1 = new QueryDialog(this,new ClientFramework(
				        new ConfigXML("lib/config.xml")));
				QueryDialog1.setModal(false);
				QueryDialog1.show();
			}
		} catch (java.lang.Exception e) {
		}
	}
}
