/**
 *        Name: XMLTreeEdit.java
 *     Purpose: this class allows the TEXT nodes of the Java tree to
 *     be edited 'in-place'. 
 *    
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: XMLTreeEditor.java,v 1.1 2000-05-31 15:37:07 higgins Exp $'
 */

package edu.ucsb.nceas.dtclient;

import java.io.Serializable;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import java.util.*;
import java.awt.event.*;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Hashtable;

public class XMLTreeEditor extends DefaultCellEditor {
    private DOMTree tree;
    private Node nd;   // current DOM node
    
    public XMLTreeEditor(DOMTree tree, JTextField tf) {
        super(tf);
        this.tree = tree;
    }
  
   public Node getNode() {
        return nd;
   }
   
    public boolean isCellEditable(EventObject e) {
        boolean rv = false;   //return value
        if (e instanceof MouseEvent) {
            MouseEvent me = (MouseEvent)e;
            if(me.getClickCount()==3) {
                TreePath path = tree.getPathForLocation(me.getX(), me.getY());
                DefaultMutableTreeNode treenode = (DefaultMutableTreeNode)path.getLastPathComponent();
                nd = tree.getNode(treenode);   // note: this is a DOM node
                if(nd.getNodeType()==Node.TEXT_NODE) {
                    rv = true;
                }
                else {rv = false;}
            }
        }
                return rv;
    }
}
