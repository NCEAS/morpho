/**
 *  '$RCSfile: MyDefaultTreeModel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-04-27 23:03:51 $'
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

package edu.ucsb.nceas.morpho.query;

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
