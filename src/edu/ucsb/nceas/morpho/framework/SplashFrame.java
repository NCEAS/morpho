/*
		A basic implementation of the JFrame class.
*/
package edu.ucsb.nceas.dtclient;

import java.awt.*;
import javax.swing.*;

public class SplashFrame extends javax.swing.JFrame
{
	public SplashFrame()
	{
		//{{INIT_CONTROLS
		getContentPane().setLayout(new BorderLayout(0,0));
		getContentPane().setBackground(java.awt.Color.white);
		setSize(390,267);
		/* Center the Frame */
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle frameDim = getBounds();
		setLocation((screenDim.width - frameDim.width) / 2 ,
		        (screenDim.height - frameDim.height) /2);

		setVisible(false);
		JPanel3.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER,JPanel3);
		JPanel3.setBounds(0,0,390,242);
		JLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		JLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel3.setText("KDI Desktop Client");
		JPanel3.add(BorderLayout.NORTH,JLabel3);
		JLabel3.setForeground(java.awt.Color.red);
		JLabel3.setFont(new Font("Dialog", Font.PLAIN, 24));
		JLabel3.setBounds(0,0,390,28);
		JPanel3.add(JScrollPane1);
		JScrollPane1.setBounds(0,28,390,199);
		JTextArea1.setEditable(false);
		JTextArea1.setWrapStyleWord(true);
		JTextArea1.setLineWrap(true);
		JScrollPane1.getViewport().add(JTextArea1);
		JTextArea1.setBounds(0,0,387,196);
		String temp = "This material is based upon work supported by ";
		temp = temp + "the National Science Foundation under Grant No. DEB99-80154 and ";
		temp = temp + "Grant No. DBI99-04777. Also supported by the National Center for Ecological Analysis ";
		temp = temp + "and Synthesis, a Center funded by NSF (Grant #DEB-94-21535),";
		temp = temp + "the University of California - Santa Barbara, the California Resources Agency, ";
        temp = temp + "and the California Environmental Protection Agency. ";
		temp = temp + "Any opinions, findings and conclusions or recommendations expressed in this material ";
		temp = temp + "are those of the author(s) and do not necessarily reflect the views ";
		temp = temp + "of the National Science Foundation (NSF).";
		JTextArea1.setText(temp);
		JLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel8.setText("Version 0.7 - Sept 2000");
		JPanel3.add(BorderLayout.SOUTH,JLabel8);
		JLabel8.setForeground(java.awt.Color.black);
		JLabel8.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel8.setBounds(0,227,390,15);
		CloseButton.setText("Close");
		CloseButton.setActionCommand("Close");
		getContentPane().add(BorderLayout.SOUTH,CloseButton);
		CloseButton.setBounds(0,242,390,25);
		//}}

		//{{INIT_MENUS
		//}}
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		CloseButton.addActionListener(lSymAction);
		//}}
//      Example of loading icon as resource - DFH 
     try {
	//	NCEASIcon = new ImageIcon(getClass().getResource("NCEASlogo.gif"));
	//	NSFIcon = new ImageIcon(getClass().getResource("nsf_logo.gif"));
	    BFlyIcon = new ImageIcon(getClass().getResource("btrfly_lrg.gif"));
		JLabel3.setIcon(BFlyIcon);
		JLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
	 }
	 catch (Exception e) {System.out.println("Could not load icons!");}

	}

	public SplashFrame(String sTitle)
	{
		this();
		setTitle(sTitle);
	}

	public void setVisible(boolean b)
	{
	//	if (b)
	//		setLocation(50, 50);
		super.setVisible(b);
	}

	static public void main(String args[])
	{
		(new SplashFrame()).setVisible(true);
	}

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets and menu bar
		Insets insets = getInsets();
		javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
		int menuBarHeight = 0;
		if (menuBar != null)
			menuBarHeight = menuBar.getPreferredSize().height;
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JTextArea JTextArea1 = new javax.swing.JTextArea();
	javax.swing.JLabel JLabel8 = new javax.swing.JLabel();
	javax.swing.JButton CloseButton = new javax.swing.JButton();
	//}}

    javax.swing.ImageIcon BFlyIcon = new javax.swing.ImageIcon();
  //  javax.swing.ImageIcon NSFIcon = new javax.swing.ImageIcon();
	//{{DECLARE_MENUS
	//}}


	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == CloseButton)
				CloseButton_actionPerformed(event);
		}
	}

	void CloseButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		this.dispose();
			 
	}
}