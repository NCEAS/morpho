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
		setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		getContentPane().setBackground(java.awt.Color.white);
		setSize(427,355);
		/* Center the Frame */
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle frameDim = getBounds();
		setLocation((screenDim.width - frameDim.width) / 2 ,
		        (screenDim.height - frameDim.height) /2);

		setVisible(false);
		JPanel3.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER,JPanel3);
		JPanel3.setBounds(0,0,427,330);
		JPanel1.setLayout(new BorderLayout(0,0));
		JPanel3.add(BorderLayout.NORTH,JPanel1);
		JPanel1.setBounds(0,0,427,74);
		LoadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		LoadingLabel.setText("Now Loading!!! ... Please Wait");
		JPanel1.add(BorderLayout.NORTH,LoadingLabel);
		LoadingLabel.setForeground(java.awt.Color.red);
		LoadingLabel.setFont(new Font("Dialog", Font.BOLD, 14));
		LoadingLabel.setBounds(0,0,427,16);
		LoadingLabel.setVisible(false);
		JLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		JLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel3.setText("Morpho");
		JPanel1.add(BorderLayout.CENTER,JLabel3);
		JLabel3.setForeground(java.awt.Color.blue);
		JLabel3.setFont(new Font("Dialog", Font.BOLD|Font.ITALIC, 36));
		JLabel3.setBounds(0,16,427,42);
		JLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel1.setText("Data Management for Ecologists");
		JPanel1.add(BorderLayout.SOUTH,JLabel1);
		JLabel1.setForeground(java.awt.Color.black);
		JLabel1.setFont(new Font("Dialog", Font.BOLD, 14));
		JLabel1.setBounds(0,58,427,16);
		JScrollPane1.setOpaque(true);
		JPanel3.add(BorderLayout.CENTER,JScrollPane1);
		JScrollPane1.setBounds(0,74,427,241);
		JTextArea1.setEditable(false);
		JTextArea1.setWrapStyleWord(true);
		JTextArea1.setLineWrap(true);
		JScrollPane1.getViewport().add(JTextArea1);
		JTextArea1.setBounds(0,0,409,300);
		String temp = "This material is based upon work supported by ";
		temp = temp + "the National Science Foundation under Grant No. DEB99-80154 and ";
		temp = temp + "Grant No. DBI99-04777. Also supported by the National Center for Ecological Analysis ";
		temp = temp + "and Synthesis, a Center funded by NSF (Grant #DEB-94-21535),";
		temp = temp + "the University of California - Santa Barbara, ";
        temp = temp + "and the State of California. ";
		temp = temp + "Any opinions, findings and conclusions or recommendations expressed in this material ";
		temp = temp + "are those of the author(s) and do not necessarily reflect the views ";
		temp = temp + "of the National Science Foundation (NSF).";
		temp = temp + "\n\n Participating organizations include the National Center for Ecological Analysis";
        temp = temp + " and Synthesis (NCEAS), the Long Term Ecological Research Network (LTER), ";
        temp = temp + "Texas Tech University, and the San Diego Supercomputer Center";
		temp = temp + "\n\n Participating individuals include Matt Jones, Dan Higgins, Jivka Bojilova, Chad Berkley, ";
		temp = temp + "Ruldolf Nottrott, and others";
		temp = temp + "\n\nThis software is named after butterflies of the genus MORPHO which are common in ";
		temp = temp + "South America. The apparent color of this butterfly is highly dependent on the angle of ";
		temp = temp + "viewing, with the most common color being blue.";
		JTextArea1.setText("This material is based upon work supported by the National Science Foundation under Grant No. DEB99-80154 and Grant No. DBI99-04777. Also supported by the National Center for Ecological Analysis and Synthesis, a Center funded by NSF (Grant #DEB-94-21535),the University of California - Santa Barbara, and the State of California. Any opinions, findings and conclusions or recommendations expressed in this material are those of the author(s) and do not necessarily reflect the views of the National Science Foundation (NSF).\n\n Participating organizations include the National Center for Ecological Analysis and Synthesis (NCEAS), the Long Term Ecological Research Network (LTER), Texas Tech University, and the San Diego Supercomputer Center\n\n Participating individuals include Matt Jones, Dan Higgins, Jivka Bojilova, Chad Berkley, Ruldolf Nottrott, and others\n\nThis software is named after the Morpho butterfly which is common in South America. The apparent color of this butterfly is highly dependent on the angle of viewing, with the most common color being blue.");
		JLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel8.setText("Version 0.90 - Jan 2001");
		JPanel3.add(BorderLayout.SOUTH,JLabel8);
		JLabel8.setForeground(java.awt.Color.black);
		JLabel8.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel8.setBounds(0,315,427,15);
		CloseButton.setText("Close");
		CloseButton.setActionCommand("Close");
		getContentPane().add(BorderLayout.SOUTH,CloseButton);
		CloseButton.setBounds(0,330,427,25);
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
//	    BFlyIcon = new ImageIcon(getClass().getResource("morphoTitle.gif"));
	    BFlyIcon = new ImageIcon(getClass().getResource("Morphoblue.gif"));
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
	public SplashFrame(boolean showLoading)
	{
		this();
		if(showLoading) {
		    LoadingLabel.setVisible(showLoading);
		    CloseButton.setText("Loading - Please Wait");
		    }
	}
	public SplashFrame(String sTitle, boolean showLoading)
	{
		this();
		setTitle(sTitle);
		if(showLoading) {LoadingLabel.setVisible(showLoading);}
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
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JLabel LoadingLabel = new javax.swing.JLabel();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
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