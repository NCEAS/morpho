/**
 *       Name: LocalDocTypesPanel.java
 *    Purpose: Example of pluggable editor for part of a XML editor
 *             this class creates a 2 column table that allows
 *             the display and editing of pairs of configuration variables.
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-05-31 22:43:00 $'
 * '$Revision: 1.4 $'
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
import javax.swing.table.*;
import javax.swing.event.*;

public class LocalDocTypesPanel extends JPanel implements TableModelListener
{
    
    public JTable table = null;
    public DefaultTableModel dtm;
    public DefaultMutableTreeNode node;
    public Hashtable ht;
    public Hashtable htnodes;
    
 // nodeMap will store the tree node associated with each textfield
    public Hashtable nodeMap;
    
    javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
   
    /** Default constructor. */
    public LocalDocTypesPanel() {
        this(null);
        }
    
    public LocalDocTypesPanel(DefaultMutableTreeNode nd) {
        this.node = nd;
        nodeMap = new Hashtable();  // textfield key mapped to node
        init();
    }
    
    public void init() {
        ht = getElements(node);
        
		setAlignmentY(0.0F);
		setAlignmentX(0.0F);
        
        setMinimumSize(new Dimension(600,200));
        setMaximumSize(new Dimension(600,200));
        setPreferredSize(new Dimension(600,200));
        setLayout(new BorderLayout(0,0));
        add(BorderLayout.CENTER,JScrollPane1);
        
        String[] headers = new String[2];
        headers[0] = "Local Document Type Name";
        headers[1] = "Local Document Type DTD";
        dtm = new DefaultTableModel(headers,0);
        dtm.addTableModelListener(this);
        table = new JTable(dtm);
        JScrollPane1.getViewport().add(table);
        
        Vector names = (Vector)ht.get("localdoctypename");
        Vector dtds = (Vector)ht.get("localdoctypedtd");
        String[] row = new String[2];
        Enumeration fff = dtds.elements();
        for (Enumeration eee = names.elements();eee.hasMoreElements();) {
            row[0] = (String)eee.nextElement();
            row[1] = (String)fff.nextElement();
            dtm.addRow(row);
        }
        for (int i=0;i<20;i++) {
            row[0] = "";
            row[1] = "";
            dtm.addRow(row);
        }
        
    }
    
/**
 *  given a DefaultMutableTreeNode, build a Hashtable of all elements
 *  that have text i.e. #PCDATA children
 */
 
 public Hashtable getElements(DefaultMutableTreeNode topnode) {
    Hashtable ht = new Hashtable();
    htnodes = new Hashtable(); 
    getElementInfo(topnode,ht, htnodes);
    // debug
    Enumeration eee = ht.keys();
    while(eee.hasMoreElements()) {
        String key = (String)eee.nextElement();
        Vector vvv = (Vector)ht.get(key);
        String obj = "";
        for (Enumeration q=vvv.elements();q.hasMoreElements();) {
            obj = obj+(String)q.nextElement();
        }
 //       System.out.println(key+" = "+obj);   
    }
    
    return ht;
 }
 
 public void getElementInfo(DefaultMutableTreeNode node, Hashtable ht, Hashtable htnodes) {
    getData(node, ht, htnodes);
    Enumeration nodes = node.children();
    // loop over child node
    while(nodes.hasMoreElements()) {
        DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(nodes.nextElement());
		NodeInfo info = (NodeInfo)(nd.getUserObject());
        if (!((info.name).equals("#PCDATA"))) {
            getElementInfo(nd,ht,htnodes);    
        }
    }
 }
 
 public void getData(DefaultMutableTreeNode node, Hashtable ht, Hashtable htnodes) {
    Enumeration nodes = node.children();
    // loop over child node
    String txt ="";
    while(nodes.hasMoreElements()) {
        DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(nodes.nextElement());
		NodeInfo info1 = (NodeInfo)(nd.getUserObject());
		if ((info1.name).equals("#PCDATA")) {
		    txt = info1.getPCValue();
        }
        if (txt.length()>0) {
            NodeInfo info2 = (NodeInfo)(node.getUserObject());
            String key = info2.name;
            if (ht.containsKey(key)) {
                Vector ob = (Vector)ht.get(key);
                Vector nds = (Vector)htnodes.get(key);
                ob.addElement(txt);
                nds.addElement(nd);
            }
            else {
                Vector vec = new Vector();
                vec.addElement(txt);
                ht.put(key,vec);
                Vector nds = new Vector();
                nds.addElement(nd);
                htnodes.put(key,nds);
            }
        }
    }
 }

    
 public String getValue(String nodeName) {
    String ret = "";
    Vector v = (Vector)ht.get(nodeName);
    if (v!=null) {
        String tmp = (String)v.firstElement();
        if (tmp!=null) ret = tmp;
    }
    return ret;
 }

 public DefaultMutableTreeNode getNode(String nodeName) {
    DefaultMutableTreeNode ret = null;
    Vector v = (Vector)htnodes.get(nodeName);
    if (v!=null) {
        DefaultMutableTreeNode tmp = (DefaultMutableTreeNode)v.firstElement();
        if (tmp!=null) ret = tmp;
    }
    return ret;
 }
 

 
 
	class dfhFocus1 extends java.awt.event.FocusAdapter
	{
		public void focusLost(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
				    DefaultMutableTreeNode nd = (DefaultMutableTreeNode)nodeMap.get(object);
		            NodeInfo info = (NodeInfo)(nd.getUserObject());
                    info.setPCValue(((JTextField)object).getText());
//				    System.out.println(((JTextField)object).getText());
//				    if (treeModel!=null) {
//				        treeModel.reload();
//				    }
				}
		}
		
		public void focusGained(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
//				    int dist = pixelsFromTop((JComponent)object);
	//			    System.out.println("Distance = "+dist);
//				    topPanel.scrollRectToVisible(new Rectangle(0,dist,50,50));
				}
		}
	}

 
// move table information to nodes 
public void TableDataToNodes() {
    node.removeAllChildren();
    int cnt = dtm.getRowCount();
    for (int i=0;i<cnt;i++) {
        String c0 = (String)dtm.getValueAt(i, 0);
        String c1 = (String)dtm.getValueAt(i, 1);
        if ((c0.length()>0)&&(c1.length()>0)) {
            NodeInfo ni = new NodeInfo("localdoc");
		    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (ni);
		    node.add(newNode);
		    
		    NodeInfo ni0 = new NodeInfo("localdoctypename");
		    DefaultMutableTreeNode newNode0 = new DefaultMutableTreeNode (ni0);
		    newNode.add(newNode0);
		    
		    NodeInfo tx0 = new NodeInfo("#PCDATA");
		    tx0.setPCValue(c0);
		    DefaultMutableTreeNode newNode2 = new DefaultMutableTreeNode (tx0);
		    newNode0.add(newNode2);
		    
		    NodeInfo ni1 = new NodeInfo("localdoctypedtd");
		    DefaultMutableTreeNode newNode1 = new DefaultMutableTreeNode (ni1);
		    newNode.add(newNode1);

		    NodeInfo tx1 = new NodeInfo("#PCDATA");
		    tx1.setPCValue(c1);
		    DefaultMutableTreeNode newNode3 = new DefaultMutableTreeNode (tx1);
		    newNode1.add(newNode3);
        }
    }
}

 
 public void tableChanged(TableModelEvent e) {
        TableDataToNodes();   
 }
 
 
}