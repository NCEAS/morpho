package edu.ucsb.nceas.editor;

import javax.swing.tree.*;
import java.beans.*;
import java.awt.*;
import javax.swing.JTree;
import javax.swing.ImageIcon;

public class MyRenderer extends javax.swing.tree.DefaultTreeCellRenderer
{
	public MyRenderer()
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
