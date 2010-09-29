/**
 *  '$RCSfile: MorphoPrefsDialog.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-02-27 23:10:50 $'
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

package edu.ucsb.nceas.morpho.framework;

import java.awt.*;
import javax.swing.*;

import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

/**
 * A simple Morpho Preferences Dialog
 */
public class MorphoPrefsDialog extends javax.swing.JDialog
{

  Morpho morpho;
  ConfigXML config;
  
	public MorphoPrefsDialog(Frame parentFrame, Morpho morpho)
	{
		super(parentFrame);
    this.morpho = morpho;
		setTitle(Language.getInstance().getMessage("MorphoPreferences"));
		setModal(true);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(560,400);
		setVisible(false);
		CenterPanel.setLayout(new GridLayout(7,1,0,0));
		getContentPane().add(BorderLayout.CENTER,CenterPanel);
		aboutLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		aboutLabel.setText(Language.getInstance().getMessage("MorphoPreferences"));
    aboutLabel.setFont(new Font("Dialog", Font.BOLD, 12));
		CenterPanel.add(aboutLabel);
		JPanel2.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		CenterPanel.add(JPanel2);
		metacatURLLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
		metacatURLLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
		metacatURLLabel.setText(Language.getInstance().getMessage("MetacatURL"));
		JPanel2.add(metacatURLLabel);
		metacatURLLabel.setForeground(java.awt.Color.black);
		metacatURLLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		metacataURLTextField.setColumns(35);
		JPanel2.add(metacataURLTextField);
		JPanel3.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		CenterPanel.add(JPanel3);
		loggingLabel.setText(Language.getInstance().getMessage("LogMessages"));
		JPanel3.add(loggingLabel);
		loggingLabel.setForeground(java.awt.Color.black);
		loggingLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		logYes.setText("yes");
		JPanel3.add(logYes);
		logNo.setText("no");
		logNo.setSelected(true);
		JPanel3.add(logNo);
		JPanel4.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		CenterPanel.add(JPanel4);
		debugLevelLabel.setText(Language.getInstance().getMessage("DebugLevel") + " (1-100)");
		JPanel4.add(debugLevelLabel);
		debugLevelLabel.setForeground(java.awt.Color.black);
		debugLevelLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		debugLevelTextField.setColumns(5);
		JPanel4.add(debugLevelTextField);
    
		JPanel5.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		CenterPanel.add(JPanel5);
		LFLabel.setText(Language.getInstance().getMessage("LookandFeel"));
		JPanel5.add(LFLabel);
		LFLabel.setForeground(java.awt.Color.black);
		LFLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		JPanel5.add(LFCombo);
    LFCombo.addItem("system L&F");
    LFCombo.addItem("kunststoff L&F");
    LFCombo.addItem("metal L&F");
    LFCombo.addItem("windows L&F");
    LFCombo.addItem("motif L&F");
    LFCombo.setSelectedIndex(0);
    
    clearButton.setText(Language.getInstance().getMessage("ClearTemporaryStorage"));
		JPanel6.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
    JPanel6.add(clearButton);
		CenterPanel.add(JPanel6);
    
		JPanel1.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		CenterPanel.add(JPanel1);

    
		ControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		getContentPane().add(BorderLayout.SOUTH,ControlPanel);
		setButton.setText(/*"Set"*/ Language.getInstance().getMessage("Set"));
		setButton.setActionCommand("OK");
		setButton.setOpaque(false);
		setButton.setMnemonic((int)'O');
		ControlPanel.add(setButton);
		cancelButton.setText(/*"Cancel"*/ Language.getInstance().getMessage("Cancel"));
		ControlPanel.add(cancelButton);
		//}}

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		setButton.addActionListener(lSymAction);
		cancelButton.addActionListener(lSymAction);
		clearButton.addActionListener(lSymAction);

		SymItem lSymItem = new SymItem();
		logYes.addItemListener(lSymItem);
		logNo.addItemListener(lSymItem);
		//}}
    config = morpho.getConfiguration();
    metacataURLTextField.setText(config.get("metacat_url",0));
    if (config.get("log_file",0).equals("true")) {
      logYes.setSelected(true);
      logNo.setSelected(false);
    }
    else {
      logYes.setSelected(false);
      logNo.setSelected(true);
    }
    debugLevelTextField.setText(config.get("debug_level",0));
    
    String lnf = config.get("lookAndFeel", 0);
    if (lnf==null) LFCombo.setSelectedIndex(0);
    else if (lnf.equalsIgnoreCase("kunststoff")) LFCombo.setSelectedIndex(1); 
    else if (lnf.equalsIgnoreCase("metal")) LFCombo.setSelectedIndex(2); 
    else if (lnf.equalsIgnoreCase("windows")) LFCombo.setSelectedIndex(3); 
    else if (lnf.equalsIgnoreCase("motif")) LFCombo.setSelectedIndex(4); 
	}

	public void setVisible(boolean b)
	{
	    if (b)
	    {
    		Rectangle bounds = (getParent()).getBounds();
    		Dimension size = getSize();
    		setLocation(bounds.x + (bounds.width - size.width)/2,
    			        bounds.y + (bounds.height - size.height)/2);
	    }

		super.setVisible(b);
	}


	//{{DECLARE_CONTROLS
	javax.swing.JPanel CenterPanel = new javax.swing.JPanel();
	javax.swing.JLabel aboutLabel = new javax.swing.JLabel();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JLabel metacatURLLabel = new javax.swing.JLabel();
	javax.swing.JTextField metacataURLTextField = new javax.swing.JTextField();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JLabel loggingLabel = new javax.swing.JLabel();
	javax.swing.JRadioButton logYes = new javax.swing.JRadioButton();
	javax.swing.JRadioButton logNo = new javax.swing.JRadioButton();
	javax.swing.JPanel JPanel4 = new javax.swing.JPanel();
	javax.swing.JLabel debugLevelLabel = new javax.swing.JLabel();
	javax.swing.JTextField debugLevelTextField = new javax.swing.JTextField();
	javax.swing.JPanel JPanel5 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel6 = new javax.swing.JPanel();
	javax.swing.JLabel LFLabel = new javax.swing.JLabel();
	javax.swing.JComboBox LFCombo = new javax.swing.JComboBox();

	javax.swing.JPanel ControlPanel = new javax.swing.JPanel();
	javax.swing.JButton setButton = new javax.swing.JButton();
	javax.swing.JButton cancelButton = new javax.swing.JButton();
  javax.swing.JButton clearButton = new javax.swing.JButton();
	//}}

	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == MorphoPrefsDialog.this)
				morphoPrefsDialog_windowClosing(event);
		}
	}

	void morphoPrefsDialog_windowClosing(java.awt.event.WindowEvent event)
	{
		// to do: code goes here.
			 
		morphoPrefsDialog_windowClosing_Interaction1(event);
	}

	void morphoPrefsDialog_windowClosing_Interaction1(java.awt.event.WindowEvent event) {
		try {
			// morphoPrefsDialog Hide the JAboutDialog
			this.setVisible(false);
		} catch (Exception e) {
		}
	}
	
	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == setButton) {
				setButton_actionPerformed(event);
      }
			else if (object == cancelButton) {
				cancelButton_actionPerformed(event);
      }
			else if (object == clearButton) {
				int opt = JOptionPane.showConfirmDialog(
						UIController.getInstance().getCurrentActiveWindow(),
			            Language.getInstance().getMessage("MorphoPrefs.clear.confirm1") + //"Are you sure you want to delete the temporary files?" +
			            "\n " +
			            Language.getInstance().getMessage("MorphoPrefs.clear.confirm2"), //"Before deleting them, you should make sure there is no crashed wizard and no wizard is running in Morpho.",
			            Language.getInstance().getMessage("MorphoPrefs.clear.title"), //"DO YOU WANT TO CONTINUE?",
			            JOptionPane.YES_NO_OPTION);
			   if (opt == JOptionPane.NO_OPTION) 
			   {
			        return;
			    }
	            Morpho.thisStaticInstance.cleanCache();
	            Morpho.thisStaticInstance.cleanTemp();
	            JOptionPane.showMessageDialog(
	            		UIController.getInstance().getCurrentActiveWindow(), 
	            		Language.getInstance().getMessage("MorphoPrefs.clear.emptied"), //"Temporary Storage Areas Emptied!",
	            		null, 
	            		JOptionPane.INFORMATION_MESSAGE);
				}
		}
	}

	void setButton_actionPerformed(java.awt.event.ActionEvent event)
	{
    config.set("metacat_url", 0, metacataURLTextField.getText());
    if (logYes.isSelected()) {
      config.set("log_file",0,"true");
    }
    else {
      config.set("log_file",0,"false");
    }
    String debugLevelS = debugLevelTextField.getText();
    try {
      Integer iii = new Integer(debugLevelS);
      config.set("debug_level",0,debugLevelS);                
    }
    catch (Exception e) {
       config.set("debug_level",0,"20"); 
       Log.debug(20, "Debug Level is NOT an integer! Reset to a value of 20.");
    }
    
    String lnf = "";
    if (LFCombo.getSelectedIndex()==0) lnf = "";
    else if (LFCombo.getSelectedIndex()==1) lnf = "kunststoff" ; 
    else if (LFCombo.getSelectedIndex()==2) lnf = "metal" ; 
    else if (LFCombo.getSelectedIndex()==3) lnf = "windows" ; 
    else if (LFCombo.getSelectedIndex()==4) lnf = "motif" ; 
    config.set("lookAndFeel",0,lnf);

    config.save();
    
    morpho.setMetacatURLString(config.get("metacat_url", 0));

    morpho.initializeLogging(config);
    // need to add  Look and Feel support
    morpho.setLookAndFeel(config.get("lookAndFeel", 0));

		setVisible(false);
    this.dispose();
	}
  
	void cancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		setVisible(false);
    this.dispose();
			 
	}

  	class SymItem implements java.awt.event.ItemListener
	{
		public void itemStateChanged(java.awt.event.ItemEvent event)
		{
			Object object = event.getSource();
			if (object == logYes) {
				if(logYes.isSelected()) logNo.setSelected(false);
      }
			if (object == logNo) {
				if(logNo.isSelected()) logYes.setSelected(false);
      }
		}
	}

  
}
