/**
 *       Name: XMLTreeCellRenderer.java
 *    Purpose: Used to store various information for application
 *             configuration in an XML file
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-05-04 21:37:43 $'
 * '$Revision: 1.1 $'
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


import javax.swing.tree.*;
import java.beans.*;
import java.awt.*;
import javax.swing.JTree;
import javax.swing.ImageIcon;

public class XMLTreeCellRenderer extends javax.swing.tree.DefaultTreeCellRenderer
{
	public XMLTreeCellRenderer()
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