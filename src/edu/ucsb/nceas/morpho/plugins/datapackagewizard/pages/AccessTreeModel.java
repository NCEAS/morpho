/*
 * %W% %E%
 *
 * Copyright 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */
package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

/**
 * FileSystemModel is a TreeTableModel representing a hierarchical file
 * system. Nodes in the FileSystemModel are TreeNodes which, when they
 * are directory nodes, cache their children to avoid repeatedly querying
 * the real file system.
 *
 */

public class AccessTreeModel
    extends AbstractTreeTableModel
    implements TreeTableModel {

  // Names of the columns.
  static protected String[] cNames = {
      "Name", "Email / Description / Distinguished Name"};

  // Types of the columns.
  static protected Class[] cTypes = {
      TreeTableModel.class, String.class};

  // The the returned file length for directories.
  public static final Integer ZERO = new Integer(0);

  public AccessTreeModel(DefaultMutableTreeNode treeNode) {
    super(treeNode);
  }

  protected Object[] getChildren(Object node) {
    DefaultMutableTreeNode treeNode = ( (DefaultMutableTreeNode) node);
    Enumeration enumeration = treeNode.children();
    Object[] children = new Object[treeNode.getChildCount()];
    int count = 0;
    while (enumeration.hasMoreElements()) {
      children[count++] = enumeration.nextElement();
    }
    return children;
  }

  //
  // The TreeModel interface
  //

  public int getChildCount(Object node) {
    Object[] children = getChildren(node);
    return (children == null) ? 0 : children.length;
  }

  public Object getChild(Object node, int i) {
    return getChildren(node)[i];
  }

  // The superclass's implementation would work, but this is more efficient.
  public boolean isLeaf(Object node) {
    DefaultMutableTreeNode treeNode = ( (DefaultMutableTreeNode) node);
    return treeNode.isLeaf();
  }

  //
  //  The TreeTableNode interface.
  //

  public int getColumnCount() {
    return cNames.length;
  }

  public String getColumnName(int column) {
    return cNames[column];
  }

  public Class getColumnClass(int column) {
    return cTypes[column];
  }

  public Object getValueAt(Object node, int column) {
    DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
    if (treeNode.getUserObject() instanceof AccessTreeNodeObject) {
      AccessTreeNodeObject treeNodeObject =
          (AccessTreeNodeObject) treeNode.getUserObject();
      switch (column) {
        case 0:
          return treeNodeObject;
        case 1:
          if (treeNodeObject.nodeType == WizardSettings.ACCESS_PAGE_GROUP) {
            if (treeNodeObject.getDescription() != null) {
              return "  " + treeNodeObject.getDescription();
            }
            if (treeNodeObject.getDN() != null) {
             return "  " + treeNodeObject.getDN();
           }
            return "";
          }
          else if (treeNodeObject.nodeType ==
                   WizardSettings.ACCESS_PAGE_USER) {
            if (treeNodeObject.getEmail() != null) {
              return "  " + treeNodeObject.getEmail();
            }
            if (treeNodeObject.getDN() != null) {
             return "  " + treeNodeObject.getDN();
           }
            return "";
          }
          else {
            return "";
          }
        case 2:
          if (treeNodeObject.nodeType == WizardSettings.ACCESS_PAGE_GROUP ||
              treeNodeObject.nodeType == WizardSettings.ACCESS_PAGE_USER) {
            if (treeNodeObject.getDN() != null) {
              return "  " + treeNodeObject.getDN();
            }
          }
          return "";
      }
    }
    else {
      switch (column) {
        case 0:
          return treeNode.toString();
        case 1:
          return "";
      }
    }
    return null;
  }
}
