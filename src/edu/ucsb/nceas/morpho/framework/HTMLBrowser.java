/**
 *        Name: HTMLBrowser.java
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-09-27 20:07:22 $'
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

package edu.ucsb.nceas.morpho.framework;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.html.*;
import javax.swing.event.*;
import java.net.*;
import java.util.Stack;

/**
 * This class is a 'quick and dirty' HTML browser written in
 * Java. It is designed as a simple means of displaying HTML
 * help files within a Java application (Thus avoiding the 
 * problem of launching a local browser in a platform independent
 * manner.)
 */
public class HTMLBrowser extends javax.swing.JFrame
{
  private static final Cursor waitCursor = 
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	private static final Cursor defaultCursor = 
					Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private static final Cursor handCursor = 
					Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
 	private boolean loadingPage = false;
 	
 	Stack pageList;

  
	public HTMLBrowser()
	{
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setTitle("HTML Help Browser");
		setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(600,450);
		setVisible(false);
		saveFileDialog.setMode(FileDialog.SAVE);
		saveFileDialog.setTitle("Save");
		//$$ saveFileDialog.move(24,336);
		openFileDialog.setMode(FileDialog.LOAD);
		openFileDialog.setTitle("Open");
		//$$ openFileDialog.move(0,336);
		JPanel1.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER, JPanel1);
		JScrollPane1.setOpaque(true);
		JPanel1.add(BorderLayout.CENTER,JScrollPane1);
		HTMLPane.setEditable(false);
		JScrollPane1.getViewport().add(HTMLPane);
		HTMLPane.setBounds(0,0,609,299);
		ControlsPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel1.add(BorderLayout.SOUTH,ControlsPanel);
		BackButton.setText("<Back");
		BackButton.setActionCommand("<Back");
		ControlsPanel.add(BackButton);
		URLTextField.setText("file:///C:/Morpho/docs/index.html");
		URLTextField.setColumns(40);
		ControlsPanel.add(URLTextField);
		LoadButton.setText("Load");
		LoadButton.setActionCommand("Load");
		ControlsPanel.add(LoadButton);
		//}}

		//{{INIT_MENUS
		//}}

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		LoadButton.addActionListener(lSymAction);
		BackButton.addActionListener(lSymAction);
		//}}
		
		pageList = new Stack();
		
		// Listener for hypertext events
		HTMLPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				// Ignore hyperlink events if the frame is busy
				if (loadingPage == true) {
					return;
				}
				if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					JEditorPane sp = (JEditorPane)evt.getSource();
					if (evt instanceof HTMLFrameHyperlinkEvent) {
						HTMLDocument doc = (HTMLDocument)sp.getDocument();
						doc.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent)evt);
					} else {
						loadNewPage(evt.getURL());
					}
				} else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
					HTMLPane.setCursor(handCursor);
				} else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
					HTMLPane.setCursor(defaultCursor);
				}
			}
		});
    String url = System.getProperty("user.dir");
    url = "file:///" + url + "/docs/user/index.html";
    URLTextField.setText(url);

		pageList.push(url);
		try {
		  HTMLPane.setPage(url);
		}
		catch (Exception e) {
		  System.out.println("Problem loading URL");
		}

	}
	
	public void loadNewPage(Object page) {
		URL url;
		try {
			if (page instanceof URL) {
				url = (URL)page;
			} else {
				url = new URL((String)page);
			}
			pageList.push(url);  
			HTMLPane.setPage(url);  
		}
		catch (Exception e) {
			  System.out.println("Problem loading new page!"); 
			}
  
	}

    /**
     * Creates a new instance of JFrame1 with the given title.
     * @param sTitle the title for the new frame.
     * @see #JFrame1()
     */
	public HTMLBrowser(String sTitle)
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
			(new HTMLBrowser()).setVisible(true);
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
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JEditorPane HTMLPane = new javax.swing.JEditorPane();
	javax.swing.JPanel ControlsPanel = new javax.swing.JPanel();
	javax.swing.JButton BackButton = new javax.swing.JButton();
	javax.swing.JTextField URLTextField = new javax.swing.JTextField();
	javax.swing.JButton LoadButton = new javax.swing.JButton();
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
	    	                                          "HTML Browser - Exit" , 
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
			if (object == HTMLBrowser.this)
				HTMLBrowser_windowClosing(event);
		}
	}

	void HTMLBrowser_windowClosing(java.awt.event.WindowEvent event)
	{
		// to do: code goes here.
			 
		HTMLBrowser_windowClosing_Interaction1(event);
	}

	void HTMLBrowser_windowClosing_Interaction1(java.awt.event.WindowEvent event) {
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
			if (object == LoadButton)
				LoadButton_actionPerformed(event);
			else if (object == BackButton)
				BackButton_actionPerformed(event);
			
			
		}
	}

	void LoadButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		String url = URLTextField.getText();
		pageList.push(url);
		try {
		  HTMLPane.setPage(url);
		}
		catch (Exception e) {
		  System.out.println("Problem loading URL");
		}
	}
	
	
	

	void BackButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		pageList.pop();
		Object url = pageList.pop();
		loadNewPage(url);	 
	}
}
