/**
 *       Name: JFrame1.java
 *    Purpose: Used to store various information for application
 *             configuration in an XML file
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-06-01 17:53:06 $'
 * '$Revision: 1.2 $'
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


/**
 *  This is a TEMPORARY top level class just for the purpose
 *  of starting up the DocFrame class with an XML document
 * selected from the File System
 * IT WILL BE COMPLETELY REPLACED
 */

package edu.ucsb.nceas.morpho.editor;
 
 
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import com.symantec.itools.javax.swing.JToolBarSeparator;
import com.symantec.itools.javax.swing.icons.ImageIcon;

/**
 * A basic JFC 1.1 based application.
 */
public class edit_plugin extends javax.swing.JFrame
{
	public edit_plugin()
	{
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setJMenuBar(JMenuBar1);
		setTitle("edit_plugin");
		setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(488,309);
		setVisible(false);
		saveFileDialog.setMode(FileDialog.SAVE);
		saveFileDialog.setTitle("Save");
		//$$ saveFileDialog.move(24,336);
		openFileDialog.setMode(FileDialog.LOAD);
		openFileDialog.setTitle("Open");
		//$$ openFileDialog.move(0,336);
		JPanel2.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		getContentPane().add(BorderLayout.NORTH, JPanel2);
		JToolBar1.setAlignmentY(0.222222F);
		JPanel2.add(JToolBar1);
		DisplayButton.setText("Display");
		DisplayButton.setActionCommand("Display");
		DisplayButton.setDefaultCapable(false);
		JToolBar1.add(DisplayButton);
		JPanel1.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER, JPanel1);
		JScrollPane1.setOpaque(true);
		JPanel1.add(BorderLayout.CENTER, JScrollPane1);
		JTextArea1.setText("Use the \'Open\' menu to open an XML document in this window. Then click on the \'Display\' button to edit that XML document.");
		JTextArea1.setLineWrap(true);
		JTextArea1.setWrapStyleWord(true);
		JScrollPane1.getViewport().add(JTextArea1);
		JTextArea1.setBounds(0,0,485,281);
		//$$ JMenuBar1.move(168,312);
		fileMenu.setText("File");
		fileMenu.setActionCommand("File");
		fileMenu.setMnemonic((int)'F');
		JMenuBar1.add(fileMenu);
		newItem.setEnabled(false);
		newItem.setText("New");
		newItem.setActionCommand("New");
		newItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK));
		newItem.setMnemonic((int)'N');
		fileMenu.add(newItem);
		openItem.setText("Open...");
		openItem.setActionCommand("Open...");
		openItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
		openItem.setMnemonic((int)'O');
		fileMenu.add(openItem);
		fileMenu.add(JSeparator1);
		exitItem.setText("Exit");
		exitItem.setActionCommand("Exit");
		exitItem.setMnemonic((int)'X');
		fileMenu.add(exitItem);
		//}}

		//{{INIT_MENUS
		//}}

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		openItem.addActionListener(lSymAction);
		exitItem.addActionListener(lSymAction);
		DisplayButton.addActionListener(lSymAction);
		//}}
		startup();
	}

    /**
     * Creates a new instance of JFrame1 with the given title.
     * @param sTitle the title for the new frame.
     * @see #JFrame1()
     */
	public edit_plugin(String sTitle)
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
			(new edit_plugin()).setVisible(true);
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
	java.awt.FileDialog saveFileDialog = new java.awt.FileDialog(this);
	java.awt.FileDialog openFileDialog = new java.awt.FileDialog(this);
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JToolBar JToolBar1 = new javax.swing.JToolBar();
	javax.swing.JButton DisplayButton = new javax.swing.JButton();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JTextArea JTextArea1 = new javax.swing.JTextArea();
	javax.swing.JMenuBar JMenuBar1 = new javax.swing.JMenuBar();
	javax.swing.JMenu fileMenu = new javax.swing.JMenu();
	javax.swing.JMenuItem newItem = new javax.swing.JMenuItem();
	javax.swing.JMenuItem openItem = new javax.swing.JMenuItem();
	javax.swing.JSeparator JSeparator1 = new javax.swing.JSeparator();
	javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem();
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
			if (object == edit_plugin.this)
				edit_plugin_windowClosing(event);
		}
	}

	void edit_plugin_windowClosing(java.awt.event.WindowEvent event)
	{
		// to do: code goes here.
			 
		edit_plugin_windowClosing_Interaction1(event);
	}

	void edit_plugin_windowClosing_Interaction1(java.awt.event.WindowEvent event) {
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
			if (object == openItem)
				openItem_actionPerformed(event);
			if (object == exitItem)
				exitItem_actionPerformed(event);
			
			if (object == DisplayButton)
				DisplayButton_actionPerformed(event);
			
		}
	}

	void openItem_actionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
			 
		openItem_actionPerformed_Interaction1(event);
	}

	void openItem_actionPerformed_Interaction1(java.awt.event.ActionEvent event) {
		try {
			// openFileDialog Show the FileDialog
			openFileDialog.setVisible(true);
			String file = openFileDialog.getFile();
			if (file!=null) {
			     file = openFileDialog.getDirectory()+file;
                try{
                    FileReader in = new FileReader(file);
                    StringWriter out = new StringWriter();
                    int c;
                    while ((c = in.read()) != -1) {
                    out.write(c);
                }
             in.close();
             out.close();
             JTextArea1.setText(out.toString());
        
             }
	        catch (Exception e) {}
			     
	        }
	    }
	    catch (Exception w) {}
	}

	void exitItem_actionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
			 
		exitItem_actionPerformed_Interaction1(event);
	}

	void exitItem_actionPerformed_Interaction1(java.awt.event.ActionEvent event) {
		try {
			this.exitApplication();
		} catch (Exception e) {
		}
	}

	void DisplayButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		String xmltext = JTextArea1.getText();
		if (!xmltext.equals("")) {
		    DocFrame newdoc = new DocFrame("XMLDisplay",xmltext);
		    newdoc.setVisible(true);
		}
			 
	}
	
	
	void startup() {
                try{
                    FileReader in = new FileReader("config.xml");
                    StringWriter out = new StringWriter();
                    int c;
                    while ((c = in.read()) != -1) {
                    out.write(c);
                }
             in.close();
             out.close();
             JTextArea1.setText(out.toString());
        
             }
	        catch (Exception e) {}
	    
	}
}
