/**
 *        Name: MyDefaultTreeModel.java
 *     Purpose: A Class for creating a DataGuide JavaBean for use Desktop Client
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: MyDefaultTreeModel.java,v 1.1 2000-08-22 19:16:09 higgins Exp $'
 */

package edu.ucsb.nceas.querybean;
import javax.swing.tree.*;

public class MyDefaultTreeModel extends javax.swing.tree.DefaultTreeModel
{
	public MyDefaultTreeModel(TreeNode root)
	{
		super(root);
	}
	public MyDefaultTreeModel(TreeNode root, boolean asksAllowsChildren)
	{
		super(root, asksAllowsChildren);
	}
    public void valueForPathChanged(TreePath path, Object newValue)
    {                 // customized for use of special user object NodeInfoDG
	    DefaultMutableTreeNode   aNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        NodeInfoDG ni = (NodeInfoDG)(aNode.getUserObject());
        ni.setName((String)newValue);  // assumes that newValue is string; i.e. editor is textbox
        nodeChanged(aNode);
    }

}