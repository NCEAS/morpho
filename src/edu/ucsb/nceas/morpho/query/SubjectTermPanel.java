/**
 *       Name: TextQueryTermPanel.java
 *    Purpose: subpanel repeated in QueryDialog
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-05-17 15:43:09 $'
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
package edu.ucsb.nceas.morpho.query;


import java.awt.*;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.*;


/**
 * This panel contains all the elements for a single text
 * query term. It is repeated for multiple combined queries.
 * It is essential a query component that can return a search
 * term, a search type (e.g. contains, matches, etc.), and
 * some information about the path (e.g. all, title, etc.)
 * 
 * @author higgins
 */
public class TextQueryTermPanel extends javax.swing.JComponent
{

	/**
	 * Constructor
	 */
	public TextQueryTermPanel()
	{
		//{{INIT_CONTROLS
		setLayout(new BorderLayout(0,0));
//		setMaximumSize(new Dimension(685, 60));
		setBackground(java.awt.Color.lightGray);
		setSize(685,58);
		SetChoicesPanel.setAlignmentX(0.496933F);
		SetChoicesPanel.setLayout(new BoxLayout(SetChoicesPanel,BoxLayout.Y_AXIS));
		add(BorderLayout.CENTER,SetChoicesPanel);
		QueryTermPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		SetChoicesPanel.add(QueryTermPanel);
		TitleCheckBox.setText("Title");
		TitleCheckBox.setActionCommand("Title");
		TitleCheckBox.setSelected(true);
		TitleCheckBox.setEnabled(false);
		QueryTermPanel.add(TitleCheckBox);
		AbstractCheckBox.setText("Abstract");
		AbstractCheckBox.setActionCommand("Abstract");
		AbstractCheckBox.setSelected(true);
		AbstractCheckBox.setEnabled(false);
		QueryTermPanel.add(AbstractCheckBox);
		KeyWordsCheckBox.setText("Key Words");
		KeyWordsCheckBox.setActionCommand("Key Words");
		KeyWordsCheckBox.setSelected(true);
		KeyWordsCheckBox.setEnabled(false);
		QueryTermPanel.add(KeyWordsCheckBox);
		AllCheckBox.setText("All");
		AllCheckBox.setActionCommand("All");
		AllCheckBox.setSelected(true);
		QueryTermPanel.add(AllCheckBox);
		QueryTermPanel.add(QueryTypeComboBox);
		QueryTypeComboBox.setBackground(java.awt.Color.white);
		TextLabel.setText("Subject");
		QueryTermPanel.add(TextLabel);
		TextLabel.setForeground(java.awt.Color.black);
		TextLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		TextValueBox.setColumns(20);
		QueryTermPanel.add(TextValueBox);
		QueryTermHelpPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		SetChoicesPanel.add(QueryTermHelpPanel);
		HelpLabel.setText("Check boxes indicate metadata areas to be searched; \'Subject\' indicates text to be found");
		QueryTermHelpPanel.add(HelpLabel);
		//}}
		QueryTypeComboBox.addItem("contains");
		QueryTypeComboBox.addItem("starts-with");
		QueryTypeComboBox.addItem("ends-with");
		QueryTypeComboBox.addItem("matches-exactly");
		QueryTypeComboBox.setSelectedIndex(0);
		
		SetChoicesPanel.setBorder(BorderFactory.createLineBorder(Color.black));
	
		//{{REGISTER_LISTENERS
		SymItem lSymItem = new SymItem();
		AllCheckBox.addItemListener(lSymItem);
		//}}
	}

	//{{DECLARE_CONTROLS
	javax.swing.JPanel SetChoicesPanel = new javax.swing.JPanel();
	javax.swing.JPanel QueryTermPanel = new javax.swing.JPanel();
	javax.swing.JCheckBox TitleCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox AbstractCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox KeyWordsCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox AllCheckBox = new javax.swing.JCheckBox();
	javax.swing.JComboBox QueryTypeComboBox = new javax.swing.JComboBox();
	javax.swing.JLabel TextLabel = new javax.swing.JLabel();
	javax.swing.JTextField TextValueBox = new javax.swing.JTextField();
	javax.swing.JPanel QueryTermHelpPanel = new javax.swing.JPanel();
	javax.swing.JLabel HelpLabel = new javax.swing.JLabel();
	//}}

	/**
	 * for testing
	 * 
	 * @param argv
	 */
	public static void main(String argv[])
	{
		class DriverFrame extends java.awt.Frame
		{
			public DriverFrame()
			{
				addWindowListener(new java.awt.event.WindowAdapter()
				{
					public void windowClosing(java.awt.event.WindowEvent event)
					{
						dispose();	  // free the system resources
						System.exit(0); // close the application
					}
				});
				setLayout(null);
				setSize(400,300);
				add(new TextQueryTermPanel());
			}
		}

		new DriverFrame().show();
	}

/**
 * get the text that is to be seached for.
 */
/*
 * returns the value string; i.e. the text to be searched for
 */
public String getValue() {
  String ret = TextValueBox.getText();
  return ret;
}

/**
 * return the value of the combobox which contains the
 * search mode (i.e. contains, starts-with, etc.)
 */
/*
 * returns the searchMode
 * i.e. (contains|starts-with|ends-with|matches-exactly)
 */
 public String getSearchMode() {
    String ret = (String)QueryTypeComboBox.getSelectedItem();
 return ret;
 }

/**
 * returns state of 'All' checkbox
 */
/*
 * get state of searchAll check box
 */
 public boolean getAllState() {
    return AllCheckBox.isSelected();
 }
 
/**
 * returns state of Title checkbox
 */
/*
 * get state of searchTitle check box
 */
 public boolean getTitleState() {
    return TitleCheckBox.isSelected();
 }

/**
 * returns state of Abstract checkbox
 */
/*
 * get state of searchAbstract check box
 */
 public boolean getAbstractState() {
    return AbstractCheckBox.isSelected();
 }

/**
 * returns state of Keywords check box
 */
/*
 * get state of searchKeyWords check box
 */
 public boolean getKeyWordsState() {
    return KeyWordsCheckBox.isSelected();
 }
	class SymItem implements java.awt.event.ItemListener
	{
		public void itemStateChanged(java.awt.event.ItemEvent event)
		{
			Object object = event.getSource();
			if (object == AllCheckBox)
				AllCheckBox_itemStateChanged(event);
		}
	}

	/**
	 * used to change the enabled state of checkbox
	 * 
	 * @param event
	 */
	void AllCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
		if (AllCheckBox.isSelected()) {
		  TitleCheckBox.setEnabled(false);
		  AbstractCheckBox.setEnabled(false);
		  KeyWordsCheckBox.setEnabled(false);
		}
		else {
		  TitleCheckBox.setEnabled(true);
		  AbstractCheckBox.setEnabled(true);
		  KeyWordsCheckBox.setEnabled(true);
		}
	}
	
	
}