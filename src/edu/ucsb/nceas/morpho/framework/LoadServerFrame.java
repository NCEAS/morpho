/**
 *        Name: LoadServerFrame.java
 *     Purpose: A Class that is the top frame for an XML_Query sample
 *		application (searchs local collection of XML files
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: LoadServerFrame.java,v 1.2 2000-09-11 23:58:20 higgins Exp $'
 */

/*
		A basic implementation of the JFrame class.
*/
package edu.ucsb.nceas.dtclient;

import java.net.*;
import java.io.*;
import java.util.*;

import java.awt.*;
import javax.swing.*;

public class LoadServerFrame extends javax.swing.JFrame
{
    ClientFramework container = null;
    String userName = "anonymous";
    String passWord = "none";
    
	public LoadServerFrame()
	{
		//{{INIT_CONTROLS
		setTitle("Load, Delete, or Update Server");
		getContentPane().setLayout(new BorderLayout(0,0));
		getContentPane().setBackground(java.awt.Color.white);
		setSize(390,270);
		/* Center the Frame */
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle frameDim = getBounds();
		setLocation((screenDim.width - frameDim.width) / 2 ,
		        (screenDim.height - frameDim.height) /2);

		setVisible(false);
		JPanel2.setLayout(new GridLayout(2,1,0,0));
		getContentPane().add(BorderLayout.NORTH,JPanel2);
		JPanel2.setBounds(0,0,390,58);
		JPanel3.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		JPanel2.add(JPanel3);
		JPanel3.setBounds(0,0,390,29);
		JLabel2.setText("Document ID: ");
		JPanel3.add(JLabel2);
		JLabel2.setForeground(java.awt.Color.black);
		JLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel2.setBounds(5,7,78,15);
		idTextField.setColumns(15);
		JPanel3.add(idTextField);
		idTextField.setBounds(88,5,165,19);
		JLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
		JLabel1.setText("(Document ID NOT required to Load Data)");
		JLabel1.setOpaque(true);
		JPanel2.add(JLabel1);
		JLabel1.setForeground(java.awt.Color.black);
		JLabel1.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel1.setBounds(0,29,390,29);
		JScrollPane1.setOpaque(true);
		getContentPane().add(BorderLayout.CENTER,JScrollPane1);
		JScrollPane1.setBounds(0,58,390,177);
		ResultTextArea.setText("Results of operation will appear here.");
		JScrollPane1.getViewport().add(ResultTextArea);
		ResultTextArea.setBounds(0,0,387,174);
		JPanel1.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		getContentPane().add(BorderLayout.SOUTH,JPanel1);
		JPanel1.setBounds(0,235,390,35);
		DBLoadButton.setToolTipText("Load Local File to Database Server");
		DBLoadButton.setText("Load");
		DBLoadButton.setActionCommand("Load");
		JPanel1.add(DBLoadButton);
		DBLoadButton.setBounds(7,5,63,25);
		DBDeleteButton.setToolTipText("Delete Document with Indicated ID");
		DBDeleteButton.setText("Delete");
		DBDeleteButton.setActionCommand("Delete");
		JPanel1.add(DBDeleteButton);
		DBDeleteButton.setBounds(75,5,71,25);
		DBUpdateButton.setToolTipText("Update Document with Indicated ID");
		DBUpdateButton.setText("Update");
		DBUpdateButton.setActionCommand("Update");
		JPanel1.add(DBUpdateButton);
		DBUpdateButton.setBounds(151,5,75,25);
		CloseButton.setText("Exit");
		CloseButton.setActionCommand("Close");
		JPanel1.add(CloseButton);
		CloseButton.setBounds(231,5,55,25);
		DocTypes.setText("DocTypes");
		DocTypes.setActionCommand("DocTypes");
		JPanel1.add(DocTypes);
		DocTypes.setBounds(291,5,91,25);
		DG.setText("DG");
		JPanel1.add(DG);
		DG.setBounds(169,35,51,25);
		//}}

		//{{INIT_MENUS
		//}}
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		CloseButton.addActionListener(lSymAction);
		DBLoadButton.addActionListener(lSymAction);
		DBDeleteButton.addActionListener(lSymAction);
		DBUpdateButton.addActionListener(lSymAction);
		DocTypes.addActionListener(lSymAction);
		DG.addActionListener(lSymAction);
		//}}
    }
	public LoadServerFrame(String sTitle)
	{
		this();
		setTitle(sTitle);
	}
	
	public LoadServerFrame(ClientFramework cf) {
	    this();
	    container = cf;
	    userName = cf.userName;
	    passWord = cf.passWord;
	}

	public void setVisible(boolean b)
	{
	//	if (b)
	//		setLocation(50, 50);
		super.setVisible(b);
	}

	static public void main(String args[])
	{
		(new LoadServerFrame()).setVisible(true);
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
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	javax.swing.JTextField idTextField = new javax.swing.JTextField();
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JTextArea ResultTextArea = new javax.swing.JTextArea();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JButton DBLoadButton = new javax.swing.JButton();
	javax.swing.JButton DBDeleteButton = new javax.swing.JButton();
	javax.swing.JButton DBUpdateButton = new javax.swing.JButton();
	javax.swing.JButton CloseButton = new javax.swing.JButton();
	javax.swing.JButton DocTypes = new javax.swing.JButton();
	javax.swing.JButton DG = new javax.swing.JButton();
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
			else if (object == DBLoadButton)
				DBLoadButton_actionPerformed(event);
			else if (object == DBDeleteButton)
				DBDeleteButton_actionPerformed(event);
			else if (object == DBUpdateButton)
				DBUpdateButton_actionPerformed(event);
			else if (object == DocTypes)
				DocTypes_actionPerformed(event);
			else if (object == DG)
				DG_actionPerformed(event);
		}
	}

	void CloseButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		this.dispose();
			 
	}

	void DBLoadButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		StringBuffer txt = new StringBuffer();
		try {
			// saveFileDialog Show the FileDialog
			container.openFileDialog.setVisible(true);
		} catch (Exception e) {}
		String file = container.openFileDialog.getFile();
		if (file!=null) {
		    int x;
		try {
		    file = container.openFileDialog.getDirectory() + file;
		    FileReader fr = new FileReader(file);
		    while((x=fr.read())!=-1) {
		        txt.append((char)x);
		    }
		    fr.close();
		    }
		    catch (Exception e) {}
		}
		LogIn();
	    try {
            System.err.println("Trying: " + container.MetaCatServletURL);
//            System.out.println("User = " + userName);
//            System.out.println("Pasword = " + passWord);
		    URL url = new URL(container.MetaCatServletURL);
		    HttpMessage msg = new HttpMessage(url);
		    Properties prop = new Properties();
		    prop.put("action","insert");
		    prop.put("doctext",txt.toString());
		    
		    
		    InputStream in = msg.sendPostMessage(prop);
		    
//		    OutputTextArea.setText(msg.contype+"\n");
		    txt = new StringBuffer();
		    int x;
		    try {
		    while((x=in.read())!=-1) {
		        txt.append((char)x);
		    }
		    }
		    catch (Exception e) {}
		    String txt1 = txt.toString();
		    System.out.println(txt1);
		    
            ResultTextArea.setText(txt1);
            this.toFront();
        LogOut();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
	}
	
public void LogIn() {
      Properties prop = new Properties();
       prop.put("action","Login Client");

      // Now try to write the document to the database
      try {
        PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
        String MetaCatServletURL =(String)options.handleGetObject("MetaCatServletURL");     // DFH
        System.err.println("Trying: " + MetaCatServletURL);
        URL url = new URL(MetaCatServletURL);
        HttpMessage msg = new HttpMessage(url);
            prop.put("username", userName);
            prop.put("password",passWord);
        InputStream returnStream = msg.sendPostMessage(prop);
	    StringWriter sw = new StringWriter();
	    int c;
	    while ((c = returnStream.read()) != -1) {
           sw.write(c);
        }
        returnStream.close();
        String res = sw.toString();
        sw.close();
        System.out.println(res);
			 
      } catch (Exception e) {
        System.out.println("Error logging into system");
      }
}

public void LogOut() {
      Properties prop = new Properties();
       prop.put("action","Logout");

      // Now try to write the document to the database
      try {
        PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
        String MetaCatServletURL =(String)options.handleGetObject("MetaCatServletURL");     // DFH
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
			 
      } catch (Exception e) {
        System.out.println("Error logging out of system");
      }
}
	
	
	

	void DBDeleteButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		String id = idTextField.getText();
		if (!id.equals("")) {
		LogIn();
	    try {
            System.err.println("Trying: " + container.MetaCatServletURL);
		    URL url = new URL(container.MetaCatServletURL);
		    HttpMessage msg = new HttpMessage(url);
		    Properties prop = new Properties();
		    prop.put("action","delete");
		    prop.put("docid",id);
		    
		    
		    InputStream in = msg.sendPostMessage(prop);
		    
//		    OutputTextArea.setText(msg.contype+"\n");
		    StringBuffer txt = new StringBuffer();
		    int x;
		    try {
		    while((x=in.read())!=-1) {
		        txt.append((char)x);
		    }
		    }
		    catch (Exception e) {}
		    String txt1 = txt.toString();
		    System.out.println(txt1);
		    
            ResultTextArea.setText(txt1);
            this.toFront();
        LogOut();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		}
		else {
		   ResultTextArea.setText("Please Enter Document ID"); 
		}
			 
	}

	void DBUpdateButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		String id = idTextField.getText();
		if (!id.equals("")) {
		LogIn();
	    try {
            System.err.println("Trying: " + container.MetaCatServletURL);
		    URL url = new URL(container.MetaCatServletURL);
		    HttpMessage msg = new HttpMessage(url);
		    Properties prop = new Properties();
		    prop.put("action","update");
		    prop.put("docid",id);
		    
		    
		    InputStream in = msg.sendPostMessage(prop);
		    
//		    OutputTextArea.setText(msg.contype+"\n");
		    StringBuffer txt = new StringBuffer();
		    int x;
		    try {
		    while((x=in.read())!=-1) {
		        txt.append((char)x);
		    }
		    }
		    catch (Exception e) {}
		    String txt1 = txt.toString();
		    System.out.println(txt1);
		    
            ResultTextArea.setText(txt1);
            this.toFront();
        LogOut();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
		}
		else {
		   ResultTextArea.setText("Please Enter Document ID"); 
		}			 
	}

	void DocTypes_actionPerformed(java.awt.event.ActionEvent event)
	{
		LogIn();
	    try {
            System.err.println("Trying: " + container.MetaCatServletURL);
		    URL url = new URL(container.MetaCatServletURL);
		    HttpMessage msg = new HttpMessage(url);
		    Properties prop = new Properties();
		    prop.put("action","getdoctypes");
		    
		    
		    InputStream in = msg.sendPostMessage(prop);
		    
		    StringBuffer txt = new StringBuffer();
		    int x;
		    try {
		    while((x=in.read())!=-1) {
		        txt.append((char)x);
		    }
		    }
		    catch (Exception e) {}
		    String txt1 = txt.toString();
		    System.out.println(txt1);
		    
            ResultTextArea.setText(txt1);
            this.toFront();
        LogOut();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
			 
	}

	void DG_actionPerformed(java.awt.event.ActionEvent event)
	{
		LogIn();
	    try {
            System.err.println("Trying: " + container.MetaCatServletURL);
		    URL url = new URL(container.MetaCatServletURL);
		    HttpMessage msg = new HttpMessage(url);
		    Properties prop = new Properties();
		    prop.put("action","getdataguide");
		    prop.put("doctype",idTextField.getText());
		    
		    InputStream in = msg.sendPostMessage(prop);
		    
		    StringBuffer txt = new StringBuffer();
		    int x;
		    try {
		    while((x=in.read())!=-1) {
		        txt.append((char)x);
		    }
		    }
		    catch (Exception e) {}
		    String txt1 = txt.toString();
		    System.out.println(txt1);
		    
            ResultTextArea.setText(txt1);
            this.toFront();
        LogOut();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
	}
}
