package edu.ucsb.nceas.editor;

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
    {                 // customized for use of special user object NodeInfo
	    DefaultMutableTreeNode   aNode = (DefaultMutableTreeNode)path.getLastPathComponent();
        NodeInfo ni = (NodeInfo)(aNode.getUserObject());
        ni.setName((String)newValue);  // assumes that newValue is string; i.e. editor is textbox
        nodeChanged(aNode);
    }

}
