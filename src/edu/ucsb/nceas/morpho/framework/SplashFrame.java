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
		setSize(384,170);
		/* Center the Frame */
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle frameDim = getBounds();
		setLocation((screenDim.width - frameDim.width) / 2 ,
		        (screenDim.height - frameDim.height) /2);

		setVisible(false);
		JLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		getContentPane().add(BorderLayout.WEST,JLabel1);
		JLabel1.setBounds(0,0,105,145);
		JLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
		getContentPane().add(BorderLayout.EAST,JLabel2);
		JLabel2.setBounds(293,0,91,145);
		JPanel3.setLayout(new GridLayout(4,1,0,0));
		getContentPane().add(BorderLayout.CENTER,JPanel3);
		JPanel3.setBackground(java.awt.Color.white);
		JPanel3.setBounds(105,0,188,145);
		JLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		JLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel3.setText("KDI");
		JPanel3.add(JLabel3);
		JLabel3.setForeground(java.awt.Color.red);
		JLabel3.setFont(new Font("Dialog", Font.PLAIN, 36));
		JLabel3.setBounds(0,0,188,36);
		JLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel6.setText("Desktop Client");
		JPanel3.add(JLabel6);
		JLabel6.setForeground(java.awt.Color.black);
		JLabel6.setFont(new Font("Dialog", Font.BOLD, 14));
		JLabel6.setBounds(0,36,188,36);
		JLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel7.setText("Created for NSF by NCEAS");
		JPanel3.add(JLabel7);
		JLabel7.setForeground(java.awt.Color.black);
		JLabel7.setBounds(0,72,188,36);
		JLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel8.setText("Version 0.5 - June 2000");
		JPanel3.add(JLabel8);
		JLabel8.setForeground(java.awt.Color.black);
		JLabel8.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel8.setBounds(0,108,188,36);
		CloseButton.setText("Close");
		getContentPane().add(BorderLayout.SOUTH,CloseButton);
		CloseButton.setBounds(0,145,384,25);
		//}}

		//{{INIT_MENUS
		//}}
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		CloseButton.addActionListener(lSymAction);
		//}}
//      Example of loading icon as resource - DFH 
     try {
		NCEASIcon = new ImageIcon(getClass().getResource("NCEASlogo.gif"));
		NSFIcon = new ImageIcon(getClass().getResource("nsf_logo.gif"));
		JLabel2.setIcon(NSFIcon);
		JLabel1.setIcon(NCEASIcon);
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
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	javax.swing.JLabel JLabel6 = new javax.swing.JLabel();
	javax.swing.JLabel JLabel7 = new javax.swing.JLabel();
	javax.swing.JLabel JLabel8 = new javax.swing.JLabel();
//	com.symantec.itools.javax.swing.icons.ImageIcon NCEASIcon = new com.symantec.itools.javax.swing.icons.ImageIcon();
//	com.symantec.itools.javax.swing.icons.ImageIcon NSFIcon = new com.symantec.itools.javax.swing.icons.ImageIcon();
	javax.swing.JButton CloseButton = new javax.swing.JButton();
	//}}

    javax.swing.ImageIcon NCEASIcon = new javax.swing.ImageIcon();
    javax.swing.ImageIcon NSFIcon = new javax.swing.ImageIcon();
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