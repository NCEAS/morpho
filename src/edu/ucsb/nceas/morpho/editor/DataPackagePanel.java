/**
 *       Name: DataPackagePanel.java
 *    Purpose: Example dynamic editor class for XMLPanel
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-07-13 17:28:59 $'
 * '$Revision: 1.2.2.1 $'
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
 * DataPackagePanel is an example of a plug-in panel to
 * be used by the XML editor. 
 */
public class DataPackagePanel extends JPanel
{  
    
  DefaultMutableTreeNode nd = null;  
    
  public DataPackagePanel(DefaultMutableTreeNode node) { 
    nd = node;

		//{{INIT_CONTROLS
		setAlignmentY(0.0F);
		setAlignmentX(0.0F);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setSize(281,185);
		JLabel1.setText(" Data Package Container");
		add(JLabel1);
		JLabel1.setForeground(java.awt.Color.black);
		JLabel1.setFont(new Font("Dialog", Font.BOLD, 14));
		JLabel2.setText(" ");
		add(JLabel2);
		TitleLabel.setText("Title:");
		add(TitleLabel);
		TitleLabel.setForeground(java.awt.Color.black);
		TitleLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		OriginatorLabel.setText("Originator(s):");
		add(OriginatorLabel);
		OriginatorLabel.setForeground(java.awt.Color.black);
		OriginatorLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		KeywordLabel.setText("Key Words:");
		add(KeywordLabel);
		KeywordLabel.setForeground(java.awt.Color.black);
		KeywordLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		JLabel3.setText(" ");
		add(JLabel3);
		//}}
		getInfo();
}  
	
/*	void getVariableNames() {
	  Vector names = new Vector();
	  Enumeration enum = nd.breadthFirstEnumeration();
	  while (enum.hasMoreElements()) {
	    nd = (DefaultMutableTreeNode)enum.nextElement();
	    NodeInfo ni = (NodeInfo)nd.getUserObject();
	    if (ni.getName().equals("variable_name")) {
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
*/	

  void getInfo() {
    String txt;
    String orig = "";
    String keywords = "";
	  Enumeration enum = nd.breadthFirstEnumeration();
	  while (enum.hasMoreElements()) {
	    nd = (DefaultMutableTreeNode)enum.nextElement();
	    NodeInfo ni = (NodeInfo)nd.getUserObject();
	    if (ni.getName().equalsIgnoreCase("title")) {
        //now check if there are child TEXT nodes
        Enumeration nodes = nd.children();
        // loop over child node
        txt ="";
        DefaultMutableTreeNode ndchild = null;
        while(nodes.hasMoreElements()) {
          ndchild = (DefaultMutableTreeNode)(nodes.nextElement());
		      NodeInfo info1 = (NodeInfo)(ndchild.getUserObject());
		      if ((info1.name).equals("#PCDATA")) {
		        txt = info1.getPCValue();
		        TitleLabel.setText("Title: "+txt);
          }
        }
	    }
	    else if (ni.getName().equalsIgnoreCase("surName")) {
        //now check if there are child TEXT nodes
        Enumeration nodes = nd.children();
        // loop over child node
        txt ="";
        DefaultMutableTreeNode ndchild = null;
        while(nodes.hasMoreElements()) {
          ndchild = (DefaultMutableTreeNode)(nodes.nextElement());
		      NodeInfo info1 = (NodeInfo)(ndchild.getUserObject());
		      if ((info1.name).equals("#PCDATA")) {
		        txt = info1.getPCValue();
		        if (orig.length()>0) {
		          orig = orig+"; "+txt;  
		        }
		        else {
		          orig = txt; 
		        }
		        OriginatorLabel.setText("Originator(s): "+orig);
          }
        }
	    }
	    else if (ni.getName().equalsIgnoreCase("keyword")) {
        //now check if there are child TEXT nodes
        Enumeration nodes = nd.children();
        // loop over child node
        txt ="";
        DefaultMutableTreeNode ndchild = null;
        while(nodes.hasMoreElements()) {
          ndchild = (DefaultMutableTreeNode)(nodes.nextElement());
		      NodeInfo info1 = (NodeInfo)(ndchild.getUserObject());
		      if ((info1.name).equals("#PCDATA")) {
		        txt = info1.getPCValue();
		        if (keywords.length()>0) {
		          keywords = keywords+"; "+txt;  
		        }
		        else {
		          keywords = txt; 
		        }
		        KeywordLabel.setText("Keywords: "+keywords);
          }
        }
	    }
	  }
  }
	
	//{{DECLARE_CONTROLS
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	javax.swing.JLabel TitleLabel = new javax.swing.JLabel();
	javax.swing.JLabel OriginatorLabel = new javax.swing.JLabel();
	javax.swing.JLabel KeywordLabel = new javax.swing.JLabel();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	//}}
	
}
