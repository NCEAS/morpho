/**
 *  '$RCSfile: TestFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-08-06 20:03:19 $'
 * '$Revision: 1.1.2.1 $'
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

package edu.ucsb.nceas.morpho.datapackage;

/*
		A basic implementation of the JFrame class for testing the PersistentTableModel class
    Used for preliminary testing only!!!
*/

import java.awt.*;
import javax.swing.*;
import java.util.*;

public class TestFrame extends javax.swing.JFrame
{
  javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JTable table = new javax.swing.JTable();

  JButton SaveButton;
  JButton AddRowButton;
  JButton insertRowAfterButton;
  JButton deleteRowButton;
  JButton addColumnButton;
  JButton insertColumnButton;
  JButton deleteColumnButton;
  
  PersistentTableModel ptm;
  PersistentVector pv;
	public TestFrame()
	{
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(850,550);
		setVisible(false);
		getContentPane().add(BorderLayout.CENTER,JScrollPane1);
		JScrollPane1.getViewport().add(table);
	//	table.setBounds(312,0,712,0);
    JPanel jp = new JPanel();
    getContentPane().add(BorderLayout.NORTH,jp);
    SaveButton = new JButton("Save");
    AddRowButton = new JButton("Add Row");
    insertRowAfterButton = new JButton("Insert Row After");
    deleteRowButton = new JButton("Delete Row");
    addColumnButton = new JButton("Add Column");
    insertColumnButton = new JButton("Insert Column");
    deleteColumnButton = new JButton("Delete Column");
    
    jp.add(SaveButton);
    jp.add(AddRowButton);
    jp.add(insertRowAfterButton);
    jp.add(deleteRowButton);
    jp.add(addColumnButton);
    jp.add(insertColumnButton);
    jp.add(deleteColumnButton);
    
	  pv = new PersistentVector();
    //***********************************
    // Note hard coded file!!!
		//pv.init("C:/VisualCafe/Projects/PersistentVector/test.txt");
		pv.init("./test.txt");
    
    //***********************************
    
    ptm = new PersistentTableModel(pv);
		table.setModel(ptm);
    table.setColumnSelectionAllowed(true);
    table.setRowSelectionAllowed(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  (table.getTableHeader()).setReorderingAllowed(false);

    SymAction lSymAction = new SymAction();
		SaveButton.addActionListener(lSymAction);
    AddRowButton.addActionListener(lSymAction);
    insertRowAfterButton.addActionListener(lSymAction);
    deleteRowButton.addActionListener(lSymAction);    
    addColumnButton.addActionListener(lSymAction); 
    insertColumnButton.addActionListener(lSymAction); 
    deleteColumnButton.addActionListener(lSymAction);
    
    setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
    
    SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);

	}
  
  class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == SaveButton)
				SaveButton_actionPerformed(event);
       if (object == AddRowButton)
				AddRowButton_actionPerformed(event);
       if (object == insertRowAfterButton)
				insertRowAfterButton_actionPerformed(event);
       if (object == deleteRowButton)
				deleteRowButton_actionPerformed(event);
       if (object == addColumnButton)
				addColumnButton_actionPerformed(event);
       if (object == insertColumnButton)
				insertColumnButton_actionPerformed(event);
       if (object == deleteColumnButton)
				deleteColumnButton_actionPerformed(event);

		}
	}

  void deleteColumnButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    int sel = table.getSelectedColumn();
    if (sel>-1) {
      ptm.deleteColumn(sel);
      pv = ptm.getPersistentVector();
    }
  }


  void insertColumnButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    int sel = table.getSelectedColumn();
      if (sel>-1) {
        ptm.insertColumn(sel); 
        pv = ptm.getPersistentVector();
      }
  }

  void addColumnButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    ptm.addColumn();
  }

  
	void insertRowAfterButton_actionPerformed(java.awt.event.ActionEvent event)
	{
    int sel = table.getSelectedRow();
    if (sel>-1) {
      Vector blanks = new Vector();
      blanks.addElement(" \t");
      blanks.addElement(" \t");
      blanks.addElement(" \t");
      ptm.insertRow(sel+1, blanks);	 
    }
	}
  
	void deleteRowButton_actionPerformed(java.awt.event.ActionEvent event)
	{
    int sel = table.getSelectedRow();
    if (sel>-1) {
     ptm.deleteRow(sel);	 
    }
	}
  
	void SaveButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		ptm.saveAsFile("output.txt");
	}

	void AddRowButton_actionPerformed(java.awt.event.ActionEvent event)
	{
      Vector blanks = new Vector();
      blanks.addElement(" \t");
      blanks.addElement(" \t");
      blanks.addElement(" \t");
      ptm.addRow(blanks);	 
	}

  
	public TestFrame(String sTitle)
	{
		this();
		setTitle(sTitle);
	}

	static public void main(String args[])
	{
      TestFrame tf = new TestFrame();
		  tf.setVisible(true);
	}

	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == TestFrame.this)
				TestFrame_windowClosing(event);
		}
	}

	void TestFrame_windowClosing(java.awt.event.WindowEvent event)
	{
	  pv.delete();		 
	}


}