/**
 *        Name: RSFrame.java
 *     Purpose: A Class for displaying search result sets
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: RSFrame.java,v 1.4 2000-09-21 22:50:58 higgins Exp $'
 */


package edu.ucsb.nceas.querybean;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.io.*;
import java.util.*;
import edu.ucsb.nceas.dtclient.*;
import java.net.URL;

public class RSFrame extends javax.swing.JFrame
{
    edu.ucsb.nceas.metaedit.AbstractMdeBean mde;
    JTabbedPane tab;
    MouseListener popupListener;
    
    JMenuItem ShowmenuItem;
    JMenuItem SavemenuItem;
    JMenuItem EditmenuItem;
    
    public boolean local = true;
    // local or remote results?
    
    PropertyResourceBundle options;
    String MetaCatServletURL = null;
    
	public RSFrame()
	{
	    
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(745,351);
		setVisible(false);
		RS_Panel.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER,RS_Panel);
		RS_Panel.setBackground(java.awt.Color.white);
		RS_Panel.setBounds(0,0,720,164);
		JPanel1.setLayout(new BorderLayout(0,0));
		RS_Panel.add(BorderLayout.NORTH, JPanel1);
		JPanel1.setBounds(0,0,720,46);
		JPanel2.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel1.add(BorderLayout.CENTER, JPanel2);
		JPanel2.setBounds(233,0,260,46);
		JLabel11.setText("Results of Search");
		JPanel2.add(JLabel11);
		JLabel11.setForeground(java.awt.Color.black);
		JLabel11.setBounds(79,5,101,15);
		JPanel3.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel1.add(BorderLayout.WEST, JPanel3);
		JPanel3.setBounds(0,0,233,46);
		JScrollPane3.setOpaque(true);
		JPanel3.add(JScrollPane3);
		JScrollPane3.setBounds(5,5,223,33);
		QueryStringTextArea.setColumns(20);
		QueryStringTextArea.setRows(2);
		QueryStringTextArea.setText("Query Summary String");
		QueryStringTextArea.setLineWrap(true);
		JScrollPane3.getViewport().add(QueryStringTextArea);
		QueryStringTextArea.setBounds(0,0,220,30);
		JPanel4.setLayout(new GridLayout(2,1,0,0));
		JPanel1.add(BorderLayout.EAST, JPanel4);
		JPanel4.setBounds(493,0,227,46);
		JCheckBox4.setText("Refine Search (Using these Results)");
		JCheckBox4.setActionCommand("Refine Search (Using these Results)");
		JPanel4.add(JCheckBox4);
		JCheckBox4.setFont(new Font("Dialog", Font.PLAIN, 12));
		JCheckBox4.setBounds(0,0,227,23);
		RS_Panel.add(BorderLayout.CENTER, RSScrollPane);
		RSScrollPane.setBounds(0,46,720,118);
		RSScrollPane.getViewport().add(JTable1);
		JTable1.setBounds(0,0,20,40);
		//}}

		//{{INIT_MENUS
		//}}
		SymAction lSymAction = new SymAction();
		
		popupListener = new PopupListener();
		ShowmenuItem = new JMenuItem("Display Document");
		ShowmenuItem.addActionListener(lSymAction);
        popup.add(ShowmenuItem);
		EditmenuItem = new JMenuItem("Edit Document");
		EditmenuItem.addActionListener(lSymAction);
        popup.add(EditmenuItem);
		SavemenuItem = new JMenuItem("Save Document");
        popup.add(SavemenuItem);
		
		
    try {
      options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");
      MetaCatServletURL = (String)options.handleGetObject("MetaCatServletURL");
    }
    catch (Exception e) {System.out.println("Could not locate properties file!");}
		
		    JTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JTable1.addMouseListener(popupListener);
		    
  /*          ListSelectionModel rowSM = JTable1.getSelectionModel();
            rowSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()) return;
                    
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) {
                        System.out.println("No rows are selected.");
                    } else {
                        if (local) {
                        int selectedRow = lsm.getMinSelectionIndex();
                        String filename = (String)JTable1.getModel().getValueAt(selectedRow, 0);
                        File file = new File("xmlfiles/"+filename);
                        DocFrame df = new DocFrame(file);
                        df.setVisible(true);
                        df.writeInfo();
                        }
                        else //assume data is from remote server
                        {
                            int selectedRow = lsm.getMinSelectionIndex();
                            String qtext1 = (String)JTable1.getModel().getValueAt(selectedRow, 0);
                                   // assumes that docid is in first column of table
	                        String respType = "xml";
	//                        if (HTMLOutput1.isSelected()) respType = "html";
	                        try {
		                        URL url = new URL(MetaCatServletURL);
		                        HttpMessage msg = new HttpMessage(url);
		                        Properties prop = new Properties();
		                        prop.put("action","getdocument");
		                        prop.put("docid",qtext1);
		                        prop.put("qformat",respType);
		    
		    
		                        InputStream in = msg.sendPostMessage(prop);
		                        String message_sent = MetaCatServletURL+msg.getArgString();
		    
		                        //OutputTextArea.setText(msg.contype+"\n");
		                        StringBuffer txt = new StringBuffer();
		                        int x;
		                        try {
		                        while((x=in.read())!=-1) {
		                            txt.append((char)x);
		                           }
		                        }
		                        catch (Exception f) {}
		                        String txt1 = txt.toString();
		                        DocFrame df = new DocFrame("Selected Document", txt1);
                                df.setVisible(true);
		                        in.close();
		                    }
		                catch (Exception ee) {
		                    ee.printStackTrace();
		                    }                        
                        }
                    }
                }
            });
		
	*/	
	}


    public void setEditor (edu.ucsb.nceas.metaedit.AbstractMdeBean mde) {
       this.mde = mde; 
    }
    public void setTabbedPane (JTabbedPane tp) {
       this.tab = tp; 
    }
    public void setLocal(boolean loc) {
        local = loc;
    }

	public RSFrame(String sTitle)
	{
		this();
		setTitle(sTitle);
	}

	public void setVisible(boolean b)
	{
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
	}

	static public void main(String args[])
	{
		(new RSFrame()).setVisible(true);
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
	javax.swing.JPanel RS_Panel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel11 = new javax.swing.JLabel();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JScrollPane JScrollPane3 = new javax.swing.JScrollPane();
	javax.swing.JTextArea QueryStringTextArea = new javax.swing.JTextArea();
	javax.swing.JPanel JPanel4 = new javax.swing.JPanel();
	javax.swing.JCheckBox JCheckBox4 = new javax.swing.JCheckBox();
	javax.swing.JScrollPane RSScrollPane = new javax.swing.JScrollPane();
	javax.swing.JTable JTable1 = new javax.swing.JTable();
	//}}
    //Create the popup menu.
        javax.swing.JPopupMenu popup = new JPopupMenu();

	//{{DECLARE_MENUS
	//}}

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == ShowmenuItem) 
				ShowMenuItem_actionPerformed(event);
	        else if (object == EditmenuItem)
	            EditMenuItem_actionPerformed(event);
		}
	}

	void ShowMenuItem_actionPerformed(java.awt.event.ActionEvent event)
	{
	   int selectedRow = JTable1.getSelectedRow();
	   if (local) {
	    if (selectedRow>-1) {
            String filename = (String)JTable1.getModel().getValueAt(selectedRow, 0);
            File file = new File(filename);
            DocFrame df = new DocFrame(file);
            df.setVisible(true);
            df.writeInfo();
            df.setDoctype("eml-dataset");
            
	    }
	   }
	   else {
            String qtext1 = (String)JTable1.getModel().getValueAt(selectedRow, 0);
                    // assumes that docid is in first column of table
	        String respType = "xml";
	        try {
		        URL url = new URL(MetaCatServletURL);
		        HttpMessage msg = new HttpMessage(url);
		        Properties prop = new Properties();
		        prop.put("action","getdocument");
		        prop.put("docid",qtext1);
		        prop.put("qformat",respType);
		    
		    
		        InputStream in = msg.sendPostMessage(prop);
		        String message_sent = MetaCatServletURL+msg.getArgString();
		        
//		        File tmp = new File("tmp/");
//		        if (!tmp.exists()) {
//		            tmp.mkdir();
//		        }
//		        File temp = new File("tmp/temp.xml");
//		        FileWriter fw = new FileWriter(temp);
		        StringBuffer txt = new StringBuffer();
		        int x;
		        try {
		        while((x=in.read())!=-1) {
		             txt.append((char)x);
//		             fw.write(x);
		            }
//		            fw.close();
//		            if (mde!=null) {
//		                mde.openDocument(temp);
//		                tab.setSelectedIndex(0);
//		            }
//		            else {System.out.println("mde is null in RSFrame class");}
		        }
		        catch (Exception f) {}
		        String txt1 = txt.toString();
		        DocFrame df = new DocFrame("Selected Document", txt1);
                df.setVisible(true);
		        in.close();
		        }
		        catch (Exception ee) {
		           ee.printStackTrace();
		        }                        
	   }
	}
	void EditMenuItem_actionPerformed(java.awt.event.ActionEvent event)
	{
	   int selectedRow = JTable1.getSelectedRow();
	   if (local) {
	    if (selectedRow>-1) {
            String filename = (String)JTable1.getModel().getValueAt(selectedRow, 0);
            File temp = new File(filename);
		            if (mde!=null) {
		                mde.openDocument(temp);
		                tab.setSelectedIndex(0);
		            }
		            else {System.out.println("mde is null in RSFrame class");}
	    }
	   }
	   else {
            String qtext1 = (String)JTable1.getModel().getValueAt(selectedRow, 0);
                    // assumes that docid is in first column of table
	        String respType = "xml";
	        try {
		        URL url = new URL(MetaCatServletURL);
		        HttpMessage msg = new HttpMessage(url);
		        Properties prop = new Properties();
		        prop.put("action","getdocument");
		        prop.put("docid",qtext1);
		        prop.put("qformat",respType);
		    
		    
		        InputStream in = msg.sendPostMessage(prop);
		        String message_sent = MetaCatServletURL+msg.getArgString();
		        
		        File tmp = new File("tmp/");
		        if (!tmp.exists()) {
		            tmp.mkdir();
		        }
		        File temp = new File("tmp/temp.xml");
		        FileWriter fw = new FileWriter(temp);
		        int x;
		        try {
		        while((x=in.read())!=-1) {
		             fw.write(x);
		            }
		            fw.close();
		            if (mde!=null) {
		                mde.openDocument(temp);
		                tab.setSelectedIndex(0);
		            }
		            else {System.out.println("mde is null in RSFrame class");}
		        }
		        catch (Exception f) {}
		        in.close();
		        }
		        catch (Exception ee) {
		           ee.printStackTrace();
		        }                        
	   }
	}


    class PopupListener extends MouseAdapter {
              public void mousePressed(MouseEvent e) {
                  maybeShowPopup(e);
              }

              public void mouseReleased(MouseEvent e) {
                  maybeShowPopup(e);
              }

              private void maybeShowPopup(MouseEvent e) {
                  if (e.isPopupTrigger()) {
                     popup.show(e.getComponent(), e.getX(), e.getY());
                  }
                      
              }
    }

}