/**
 *       Name: VariablePanel.java
 *    Purpose: Example dynamic editor class for XMLPanel
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-12-28 04:45:50 $'
 * '$Revision: 1.2 $'
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
import javax.swing.tree.*;
import java.util.*;

/**
 * VariablePanel is an example of a very  plug-in panel to
 * be used by the XML editor. 
 */
public class VariablePanel extends JPanel
{  
    
  DefaultMutableTreeNode nd = null;  
    
  public VariablePanel(DefaultMutableTreeNode node) { 
    nd = node;

		//{{INIT_CONTROLS
		setAlignmentY(0.0F);
		setAlignmentX(0.0F);
		setLayout(new BorderLayout(0,0));
		setSize(0,0);
		JLabel1.setText("<html>This is a list of all attributes in the document</html>");
		JLabel1.setAlignmentY(0.0F);
		add(BorderLayout.NORTH,JLabel1);
		JScrollPane1.setOpaque(true);
		add(BorderLayout.WEST,JScrollPane1);
		JList1.setFixedCellWidth(300);
		JScrollPane1.getViewport().add(JList1);
		JList1.setBounds(0,0,20,40);
		//}}
    getVariableNames();
}  
	
	void getVariableNames() {
	  Vector names = new Vector();
	  Enumeration enum = nd.breadthFirstEnumeration();
	  while (enum.hasMoreElements()) {
	    nd = (DefaultMutableTreeNode)enum.nextElement();
	    NodeInfo ni = (NodeInfo)nd.getUserObject();
	    if (ni.getName().equals("attributeName")) {
        //now check if there are child TEXT nodes
        Enumeration nodes = nd.children();
        // loop over child node
        String txt ="";
        DefaultMutableTreeNode ndchild = null;
        while(nodes.hasMoreElements()) {
          ndchild = (DefaultMutableTreeNode)(nodes.nextElement());
		      NodeInfo info1 = (NodeInfo)(ndchild.getUserObject());
		      if ((info1.name).equals("#PCDATA")) {
		      txt = info1.getPCValue();
		      names.addElement(txt);
          }
        }
	    }
	  }
	  JList1.setListData(names); 
	}
	
	
	//{{DECLARE_CONTROLS
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JList JList1 = new javax.swing.JList();
	//}}
	
}
