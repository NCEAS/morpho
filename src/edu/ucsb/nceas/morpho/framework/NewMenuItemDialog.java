/**
 *        Name: NewMenuItem.java
 *     Purpose: this class displays a dialog allowing user to add items to
 *     a menu. Items added can then be launched from menu.
 *    
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: NewMenuItemDialog.java,v 1.1 2000-05-31 15:37:07 higgins Exp $'
 */

package edu.ucsb.nceas.dtclient;


import java.awt.*;
import javax.swing.*;

/**
 * A basic implementation of the JDialog class.
 */
public class NewMenuItemDialog extends javax.swing.JDialog
{
    JMenu LaunchMenu;
	public NewMenuItemDialog(Frame parentFrame, JMenu lm)
	{
		super(parentFrame);
        LaunchMenu = lm;
    
		//{{INIT_CONTROLS
		getContentPane().setLayout(null);
		setSize(430,281);
		setVisible(false);
		JLabel1.setText("Add Menu Item to Launch Application");
		getContentPane().add(JLabel1);
		JLabel1.setForeground(java.awt.Color.black);
		JLabel1.setBounds(102,6,258,30);
		getContentPane().add(ItemText);
		ItemText.setBounds(24,42,384,28);
		JLabel2.setText("Menu Item Text");
		getContentPane().add(JLabel2);
		JLabel2.setForeground(java.awt.Color.black);
		JLabel2.setBounds(24,66,90,24);
		getContentPane().add(ExecuteText);
		ExecuteText.setBounds(24,120,384,30);
		JLabel3.setText("Execute Command String");
		getContentPane().add(JLabel3);
		JLabel3.setForeground(java.awt.Color.black);
		JLabel3.setBounds(24,150,192,24);
		OK_Button.setText("OK");
		OK_Button.setActionCommand("OK");
		getContentPane().add(OK_Button);
		OK_Button.setBounds(336,186,74,24);
		Cancel.setText("Cancel");
		Cancel.setActionCommand("Cancel");
		getContentPane().add(Cancel);
		Cancel.setBounds(336,222,76,24);
		//}}
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		Cancel.addActionListener(lSymAction);
		OK_Button.addActionListener(lSymAction);
		//}}
	}
    
	//{{DECLARE_CONTROLS
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JTextField ItemText = new javax.swing.JTextField();
	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	javax.swing.JTextField ExecuteText = new javax.swing.JTextField();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	javax.swing.JButton OK_Button = new javax.swing.JButton();
	javax.swing.JButton Cancel = new javax.swing.JButton();
	//}}

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == Cancel)
				Cancel_actionPerformed(event);
			else if (object == OK_Button)
				OKButton_actionPerformed(event);
		}
	}

	void Cancel_actionPerformed(java.awt.event.ActionEvent event)
	{
		this.setVisible(false);
			 
	}

	void OKButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		Action newaction = new MyAction(ItemText.getText(),ExecuteText.getText());
		LaunchMenu.add(newaction);
		this.setVisible(false);
			 
	}
}