/**
 *        Name: AttributeEditDialog.java
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-07-13 17:28:59 $'
 * '$Revision: 1.5.2.1 $'
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

package edu.ucsb.nceas.morpho.editor;



import java.awt.*;
import javax.swing.*;
import java.util.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * This is a simple dialog which allows the viewing/editing of
 * the attributes of an XML element as stored in the DocFrame
 * JTree XML editor. The viewer is a two column table with the
 * attibute name in the first column and the attribute value
 * in the second column. Attributes can be edited or added
 * simply by entering values in the table. NO CHECKING FOR THE
 * VALIDITY OF ATTRIBUTES IS CARRIED OUT!!!
 * 
 * @author higgins
 */
public class AttributeEditDialog extends javax.swing.JDialog //implements TableModelListener
{
  public DefaultTableModel dtm;
  public JTable table = null;
  public DefaultMutableTreeNode node;
  
	public AttributeEditDialog(Frame parent)
	{
		super(parent);
		
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(329,176);
		setVisible(false);
		getContentPane().add(BorderLayout.CENTER, AttributeScrollPane);
		ControlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
		getContentPane().add(BorderLayout.SOUTH, ControlPanel);
		CancelButton.setText("Cancel");
		CancelButton.setActionCommand("Cancel");
		ControlPanel.add(CancelButton);
		SaveButton.setText("Save");
		SaveButton.setActionCommand("Save");
		ControlPanel.add(SaveButton);
		//}}
		
		    String[] headers = new String[2];
        headers[0] = "Attribute Name";
        headers[1] = "Attribute Value";
        dtm = new DefaultTableModel(headers,0);
//        dtm.addTableModelListener(this);
        table = new JTable(dtm);
        AttributeScrollPane.getViewport().add(table);

  
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		CancelButton.addActionListener(lSymAction);
		SaveButton.addActionListener(lSymAction);
		//}}
	}
  
  /** 
   *   dialog is given a DefaultMutableTreeNode (nd) with the attribute data
   *   to be displayed
   */
	public AttributeEditDialog(Frame parent, String title, DefaultMutableTreeNode nd) {
    this(parent);
    setTitle(title);
    this.node = nd;
    Hashtable attr = ((NodeInfo)node.getUserObject()).attr;
    String[] row = new String[2];
    Enumeration enum = attr.keys();
    while (enum.hasMoreElements()) {
      row[0] = (String)enum.nextElement();
      row[1] = (String)attr.get(row[0]);
      dtm.addRow(row);
    }
    for (int i=0;i<20;i++) {
      row[0] = "";
      row[1] = "";
      dtm.addRow(row);
    }
    
	}

	public AttributeEditDialog()
	{
		this((Frame)null);
	}

	public AttributeEditDialog(String sTitle)
	{
		this();
		setTitle(sTitle);
	}

	static public void main(String args[])
	{
		(new AttributeEditDialog()).setVisible(true);
	}

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JScrollPane AttributeScrollPane = new javax.swing.JScrollPane();
	javax.swing.JPanel ControlPanel = new javax.swing.JPanel();
	javax.swing.JButton CancelButton = new javax.swing.JButton();
	javax.swing.JButton SaveButton = new javax.swing.JButton();
	//}}



// public void tableChanged(TableModelEvent e) {
// 
// }


	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == CancelButton)
				CancelButton_actionPerformed(event);
			else if (object == SaveButton)
				SaveButton_actionPerformed(event);
		}
	}

	void CancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
			 
		CancelButton_actionPerformed_Interaction1(event);
	}

	void CancelButton_actionPerformed_Interaction1(java.awt.event.ActionEvent event)
	{
		try {
			// AttributeEditDialog Hide the AttributeEditDialog
			this.setVisible(false);
		} catch (java.lang.Exception e) {
		}
	}

	void SaveButton_actionPerformed(java.awt.event.ActionEvent event)
	{
	  TableCellEditor tce = table.getCellEditor();
	  if (tce!=null) {
	    tce.stopCellEditing();  
	  }
    int cnt = dtm.getRowCount();
    Hashtable newattr = new Hashtable();
    for (int i=0;i<cnt;i++) {
        String name = (String)dtm.getValueAt(i, 0);
        String value = (String)dtm.getValueAt(i, 1);
        if ((name.length()>0)&&(value.length()>0)) {
          newattr.put(name, value);  
        }
    }
    NodeInfo ni = (NodeInfo)node.getUserObject();
    ni.attr = newattr;
		try {
			this.setVisible(false);
		} catch (java.lang.Exception e) {
		}
	}
	
}