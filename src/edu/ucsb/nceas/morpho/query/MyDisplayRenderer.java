/**
 *        Name: MyRenderer.java
 *     Purpose: A Class for creating a DataGuide JavaBean for use Desktop Client
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: MyDisplayRenderer.java,v 1.1 2000-09-21 22:50:58 higgins Exp $'
 */

package edu.ucsb.nceas.querybean;

import javax.swing.tree.*;
import java.beans.*;
import java.awt.*;
import javax.swing.JTree;
import javax.swing.ImageIcon;

public class MyDisplayRenderer extends javax.swing.tree.DefaultTreeCellRenderer
{
	public MyDisplayRenderer()
	{
    }
    public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {

            super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);
            NodeInfo ni = (NodeInfo)((DefaultMutableTreeNode)(value)).getUserObject();                
            if (ni!=null) {
                ImageIcon curicon = ni.getIcon();
                if (curicon!=null) {
                    setIcon(curicon); 
                    }
            } 

                    return this;
        }


}