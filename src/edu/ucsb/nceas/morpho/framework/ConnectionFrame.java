/**
 *        Name: ConnectionFrame.java
 *     Purpose: A Class that connects the user to the networked KDI
 *     system
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: ConnectionFrame.java,v 1.13 2001-01-09 23:55:12 higgins Exp $'
 */

package edu.ucsb.nceas.dtclient;

import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import com.symantec.itools.javax.swing.JButtonGroupPanel;
import com.symantec.itools.javax.swing.borders.LineBorder;
import com.symantec.itools.javax.swing.icons.ImageIcon;
public class ConnectionFrame extends javax.swing.JFrame
{
    ConfigXML config;
    String MetaCatServletURL;
    ClientFramework container = null;
    javax.swing.ImageIcon still = null;
    javax.swing.ImageIcon flapping = null;
	public ConnectionFrame()
	{
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
		setTitle("Connection");
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(315,290);
		/* Center the Frame */
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle frameDim = getBounds();
		setLocation((screenDim.width - frameDim.width) / 2 ,
		        (screenDim.height - frameDim.height) /2);
		setVisible(false);
		JLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel1.setText("Connection Dialog");
		getContentPane().add(BorderLayout.NORTH,JLabel1);
		JLabel1.setForeground(java.awt.Color.black);
		JLabel1.setFont(new Font("Dialog", Font.BOLD|Font.ITALIC, 14));
		JLabel1.setBounds(0,0,315,16);
		JPanel2.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER,JPanel2);
		JPanel2.setBounds(0,16,315,239);
		JButtonGroupPanel1.setLayout(new GridLayout(3,1,0,0));
		JPanel2.add(BorderLayout.NORTH,JButtonGroupPanel1);
		JButtonGroupPanel1.setBounds(0,0,315,87);
		JPanel3.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		JButtonGroupPanel1.add(JPanel3);
		JPanel3.setBounds(0,0,315,29);
		Name.setText("Name");
		JPanel3.add(Name);
		Name.setForeground(java.awt.Color.black);
		Name.setFont(new Font("Dialog", Font.PLAIN, 12));
		Name.setBounds(5,7,34,15);
		NameTextField.setColumns(23);
		NameTextField.setText("Enter user name here");
		JPanel3.add(NameTextField);
		NameTextField.setBounds(44,5,253,19);
		JPanel4.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		JButtonGroupPanel1.add(JPanel4);
		JPanel4.setBounds(0,29,315,29);
		Password.setText("Password");
		JPanel4.add(Password);
		Password.setForeground(java.awt.Color.black);
		Password.setFont(new Font("Dialog", Font.PLAIN, 12));
		Password.setBounds(5,7,56,15);
		PWTextField.setColumns(21);
		JPanel4.add(PWTextField);
		PWTextField.setBounds(66,5,231,19);
		ActivityLabel.setDoubleBuffered(true);
		ActivityLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
		ActivityLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		JButtonGroupPanel1.add(ActivityLabel);
		ActivityLabel.setForeground(java.awt.Color.black);
		ActivityLabel.setBounds(0,58,315,29);
		ConnectionResultsTextArea.setText("Connection Messages will appear here");
		JPanel2.add(BorderLayout.CENTER,ConnectionResultsTextArea);
		ConnectionResultsTextArea.setBounds(0,87,315,152);
		JPanel1.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		getContentPane().add(BorderLayout.SOUTH,JPanel1);
		JPanel1.setBounds(0,255,315,35);
		connectButton.setText("Connect");
		connectButton.setActionCommand("OK");
		JPanel1.add(connectButton);
		connectButton.setBounds(65,5,81,25);
		DisconnectButton.setText("Disconnect");
		DisconnectButton.setActionCommand("Disconnect");
		DisconnectButton.setEnabled(false);
		JPanel1.add(DisconnectButton);
		DisconnectButton.setBounds(151,5,99,25);
		//}}

		//{{INIT_MENUS
		//}}
	
		//{{REGISTER_LISTENERS
		SymItem lSymItem = new SymItem();
		SymAction lSymAction = new SymAction();
		connectButton.addActionListener(lSymAction);
		DisconnectButton.addActionListener(lSymAction);
		//}}
		config = new ConfigXML("config.xml");
		MetaCatServletURL = config.get("MetaCatServletURL", 0);
		
//      Example of loading icon as resource - DFH 
     try {
		still = new javax.swing.ImageIcon(getClass().getResource("Btfly.gif"));
		ActivityLabel.setIcon(still);
		flapping = new javax.swing.ImageIcon(getClass().getResource("Btfly4.gif"));
	 }
	 catch (Exception w) {
	    System.out.println("Error in loading images");
	 }
    }
	public ConnectionFrame(String sTitle)
	{
		this();
		setTitle(sTitle);
	}
	
	public ConnectionFrame(ClientFramework cont) {
	    this();
	    container = cont;
	    if (container!=null) {
		    DisconnectButton.setEnabled(container.connected);
		}
	    
	}

	public void setVisible(boolean b)
	{
		if (b)
//			setLocation(50, 50);
		super.setVisible(b);
	}

	static public void main(String args[])
	{
		(new ConnectionFrame()).setVisible(true);
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
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	com.symantec.itools.javax.swing.JButtonGroupPanel JButtonGroupPanel1 = new com.symantec.itools.javax.swing.JButtonGroupPanel();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JLabel Name = new javax.swing.JLabel();
	javax.swing.JTextField NameTextField = new javax.swing.JTextField();
	javax.swing.JPanel JPanel4 = new javax.swing.JPanel();
	javax.swing.JLabel Password = new javax.swing.JLabel();
	javax.swing.JPasswordField PWTextField = new javax.swing.JPasswordField();
	javax.swing.JLabel ActivityLabel = new javax.swing.JLabel();
	javax.swing.JTextArea ConnectionResultsTextArea = new javax.swing.JTextArea();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JButton connectButton = new javax.swing.JButton();
	javax.swing.JButton DisconnectButton = new javax.swing.JButton();
	//}}

	//{{DECLARE_MENUS
	//}}


	class SymItem implements java.awt.event.ItemListener
	{
		public void itemStateChanged(java.awt.event.ItemEvent event)
		{
		}
	}

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == connectButton)
				connectButton_actionPerformed(event);
			else if (object == DisconnectButton)
				DisconnectButton_actionPerformed(event);
			
		}
	}

	void connectButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		ActivityLabel.setIcon(flapping);
		ActivityLabel.invalidate();
		JPanel2.validate();
		JPanel2.paint(JPanel2.getGraphics());
	   ConnectionResultsTextArea.setText("Working...");
	   
	 Thread worker = new Thread() {
	    public void run() {
      Properties prop = new Properties();
        prop.put("action","login");
        prop.put("qformat","xml");
      // Now try to write the document to the database
      try {
        System.err.println("Trying: " + MetaCatServletURL);
        URL url = new URL(MetaCatServletURL);
        HttpMessage msg = new HttpMessage(url);
        if (container!=null) {
            container.userName = NameTextField.getText();
            container.queryBean1.setUserName(container.userName);
            container.passWord = PWTextField.getText();
            container.queryBean1.setPassWord(container.userName);
            
        }
            prop.put("username", NameTextField.getText());
            prop.put("password", PWTextField.getText());
        
        InputStream returnStream = msg.sendPostMessage(prop);
	    StringWriter sw = new StringWriter();
	    container.connected = true;
	    int c;
	    while ((c = returnStream.read()) != -1) {
           sw.write(c);
        }
        sw.flush();
        sw.close();
        returnStream.close();
        final String res = sw.toString();
        sw.close();
        SwingUtilities.invokeLater(new Runnable() {
        public void run() {
        if (res.indexOf("success")>=0) {
		dispose();
        }
        else {
                    ConnectionResultsTextArea.setText(res);
		            ActivityLabel.setIcon(still);
		            // a failure to login should result in username being set to 'public'
                    if (container!=null) {
                    container.userName = "public";
                    container.queryBean1.setUserName(container.userName);
                    container.passWord = "none";
                    container.queryBean1.setPassWord(container.userName);
            
        }
		            
		        }
        }
		        });
        
      } catch (Exception e) {
		JOptionPane.showMessageDialog(null,"Error logging into server! ");
		dispose();
//        System.out.println("Error logging into system");
//        e.getMessage();
      }
     	
		} // end of run
	 };
	 worker.start();
	}
	
	
	
public void LogOut() {
      Properties prop = new Properties();
       prop.put("action","Logout");

      // Now try to write the document to the database
      try {
        System.err.println("Trying: " + MetaCatServletURL);
        URL url = new URL(MetaCatServletURL);
        HttpMessage msg = new HttpMessage(url);
        InputStream returnStream = msg.sendPostMessage(prop);
	    StringWriter sw = new StringWriter();
	    int c;
	    while ((c = returnStream.read()) != -1) {
           sw.write(c);
        }
        returnStream.close();
        String res = sw.toString();
        sw.close();
 //       System.out.println(res);
        JOptionPane.showMessageDialog(this,"You have closed the connection to the remote server.");
		 
      } catch (Exception e) {
  //      System.out.println("Error logging out of system");
            JOptionPane.showMessageDialog(this,"Error logging out of system");
      }
}
	

	void DisconnectButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		LogOut();
	    DisconnectButton.setEnabled(false);
	}
	
	public void enableDisconnect() {
	    DisconnectButton.setEnabled(true);
	}
}