/**
 *       Name: XMLTreeCellRenderer.java
 *    Purpose: Uses the ImageIcon stored in a UserObject
 *             as the icon of TreeCellNode
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-03-17 04:16:28 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

/**
 * This class is a simple extension of the DefaultTreeCell
 * Renderer that uses an ImageIcon stored in the NodeInfo
 * UserObject of a tree node as the icon of a node when it is
 * displayed in a tree. Using an icon from the UserObject allows
 * each node to have its own icon which can be dynamically
 * changed.
 *
 * @author higgins
 */
public class AccessTreeCellRenderer extends javax.swing.tree.DefaultTreeCellRenderer
{

        /**
         * Constructor
         */
        public AccessTreeCellRenderer()
        {
    }

    /**
     * required method for a TreeCellRenderer
     * uses icon in userobject for each nodeinstead of
     * an icon which is based on whether node is leaf or branch.
     *
     * @param tree
     * @param value
     * @param sel
     * @param expanded
     * @param leaf
     * @param row
     * @param hasFocus
     */
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
            if (((DefaultMutableTreeNode)(value)).getUserObject()==null) return this;
            AccessTreeNodeObject ni= null;
            try {
              ni = (AccessTreeNodeObject)((DefaultMutableTreeNode)(value)).getUserObject();
            } catch (Exception w) {
              return this;
            }
            if (ni!=null) {
              if(ni.nodeType ==  WizardSettings.ACCESS_PAGE_AUTHSYS){
                ImageIcon curicon = new ImageIcon(getClass()
                                                  .getResource("authsys.gif"));
                if (curicon != null) {
                  setIcon(curicon);
                }
              } else if(ni.nodeType ==  WizardSettings.ACCESS_PAGE_GROUP){
                ImageIcon curicon = new ImageIcon(getClass()
                                                  .getResource("group.gif"));
                if (curicon != null) {
                  setIcon(curicon);
                }
              } else if(ni.nodeType ==  WizardSettings.ACCESS_PAGE_USER){
                ImageIcon curicon = new ImageIcon(getClass()
                                                  .getResource("user.gif"));
                if (curicon != null) {
                  setIcon(curicon);
                }
              }
            }

                    return this;
        }


}
