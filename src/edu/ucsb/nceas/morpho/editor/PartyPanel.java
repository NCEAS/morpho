/**
 *       Name: PartyPanel.java
 *    Purpose: Example dynamic editor class for XMLPanel
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-03-02 23:05:02 $'
 * '$Revision: 1.12 $'
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

import java.util.Enumeration;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTree;
import javax.swing.tree.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


import edu.ucsb.nceas.morpho.editor.DocFrame;

import edu.ucsb.nceas.utilities.*;

import java.util.Iterator;

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.PartyPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMImplementation;
import org.apache.xerces.dom.DOMImplementationImpl;

/**
 * PartyPanel is an example of a special panel editor for
 * use with the DocFrame class. It is designed to
 *
 * @author higgins
 */
public class PartyPanel extends JPanel
{

  SymFocus aSymFocus = new SymFocus();
  AbstractWizardPage awp2;
  DefaultMutableTreeNode node;
  Node domNode;
  String nname;
  
  public PartyPanel(DefaultMutableTreeNode node) {
    this.node = node;
    final DefaultMutableTreeNode fnode = node;
    JPanel jp = this;
    jp.setLayout(new BoxLayout(jp,BoxLayout.Y_AXIS));
    jp.setAlignmentX(Component.LEFT_ALIGNMENT);
    jp.setMaximumSize(new Dimension(800,600));
    final AbstractWizardPage awp = WizardPageLibrary.getPage(DataPackageWizardInterface.PARTY_PAGE);
    ((PartyPage)awp).desc.setVisible(false);
    ((PartyPage)awp).listPanel.setVisible(false);
    ((PartyPage)awp).setMaximumSize(new Dimension(800,400));
    jp.add(awp);
    awp2 = awp;
    setFocusLostForAllSubcomponents(awp); 

    DocFrame df = DocFrame.currentDocFrameInstance;
    domNode = df.writeToDOM(node);
    // domNode is now the DOM tree equivalent of the original JTree subtree in node
    // The parts of this DOM tree that are NOT handled by the attribute panel need to be preserved
    // so that when data from the panel is merged back, information is not lost.
    final OrderedMap om = XMLUtilities.getDOMTreeAsXPathMap(domNode);
    NodeInfo ni = (NodeInfo)node.getUserObject();
    nname = ni.getName();
    ((PartyPage)awp).setXPathRoot("/"+nname);
    awp.setPageData(om);

    JPanel controlsPanel = new JPanel();
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        final Document doc = domNode.getOwnerDocument();
        try{
//        Log.debug(1, "InDOM: "+ XMLUtilities.getDOMTreeAsString(domNode));
          // data from the panel is merged back into the DOM tree here
          XMLUtilities.getXPathMapAsDOMTree(awp.getPageData(), domNode);
//        Log.debug(1, "OutDOM: "+ XMLUtilities.getDOMTreeAsString(domNode));
          JTree domtree = new DOMTree(doc);
          DefaultMutableTreeNode root = (DefaultMutableTreeNode)domtree.getModel().getRoot();
          DefaultMutableTreeNode parent = (DefaultMutableTreeNode)fnode.getParent();
          int index = parent.getIndex(fnode);
          parent.remove(index);
          parent.insert(root, index);
          DocFrame df1 = DocFrame.currentDocFrameInstance;
					DefaultMutableTreeNode fn = df1.findTemplateNodeByName(nname);
					if (fn!=null) {
					  df1.treeUnion(root, fn);
					}
					df1.addXMLAttributeNodes(root);
					df1.setAttributeNames(root);
					NodeInfo nir = (NodeInfo)(root.getUserObject());
					nir.setChoice(false);
					nir.setCheckboxFlag(false);
					(df1.treeModel).reload();
        } catch (Exception e) {
          Log.debug(5, "Problem in PartyPanel");
        }
      }
    });
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
				awp.setPageData(om);
      }
    });
    controlsPanel.add(saveButton);
    controlsPanel.add(cancelButton);
//    jp.add(controlsPanel);
    
    
    jp.setVisible(true);
  }

  void saveAction() {
    Document doc = domNode.getOwnerDocument();
    try{
//        Log.debug(1, "InDOM: "+ XMLUtilities.getDOMTreeAsString(domNode));
          // data from the panel is merged back into the DOM tree here
          stripChildren(domNode);
          XMLUtilities.getXPathMapAsDOMTree(awp2.getPageData(), domNode);
//        Log.debug(1, "OutDOM: "+ XMLUtilities.getDOMTreeAsString(domNode));
          JTree domtree = new DOMTree(doc);
          DefaultMutableTreeNode root = (DefaultMutableTreeNode)domtree.getModel().getRoot();
          DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
          int index = parent.getIndex(node);
          parent.remove(index);
          parent.insert(root, index);
          DocFrame df1 = DocFrame.currentDocFrameInstance;
					DefaultMutableTreeNode fn = df1.findTemplateNodeByName(nname);
          node = root;
					if (fn!=null) {
					  df1.treeUnion(root, fn);
					}
					df1.addXMLAttributeNodes(root);
					df1.setAttributeNames(root);
					NodeInfo nir = (NodeInfo)(root.getUserObject());
					nir.setChoice(false);
					nir.setCheckboxFlag(false);
					(df1.treeModel).reload();
    } catch (Exception e) {
          Log.debug(5, "Problem in PartyPanel");
    }
  }
  
	class SymFocus extends java.awt.event.FocusAdapter
	{
		public void focusLost(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
      if (object instanceof JRadioButton) {
//        Log.debug(10, "RadioButton");
        setFocusLostForAllSubcomponents(awp2);
      }
			saveAction();
		}
    
		public void focusGained(java.awt.event.FocusEvent event) {
      DocFrame df = DocFrame.currentDocFrameInstance;
      df.setTreeValueFlag(false);
			TreePath tp = new TreePath(node.getPath());
			df.tree.setSelectionPath(tp);
			df.tree.scrollPathToVisible(tp);
    }
	}

	public void setFocusLostForAllSubcomponents(Container panel) {
	    Vector ret = getAllComponents(panel);
//		System.out.println("Total number of components: "+ret.size());
		for (int i=0; i<ret.size();i++) {
		  Component temp = (Component)(ret.elementAt(i));
      temp.removeFocusListener(aSymFocus);
		  temp.addFocusListener(aSymFocus);   
		}
	}
	
	public Vector getAllComponents(Container panel) {
	  Vector ret = new Vector();  
	  Component[] cont = panel.getComponents();
	  // this is the loop over the top level container;
	  // containers within this container may hold additional components
	  for (int i=0;i<cont.length;i++) {
	    ret.addElement(cont[i]);
	  }
	  getChildComponents(cont, ret);
	  return ret;
	}
	
	private void getChildComponents(Component[] comps, Vector vec) {
	  for (int i=0;i<comps.length;i++) {
	    Component[] innercomp = ((Container)comps[i]).getComponents();
	    for (int j=0;j<innercomp.length;j++) {
	      vec.addElement(innercomp[j]);
	    }
	    if (innercomp.length>0) {
	      getChildComponents(innercomp, vec);
	    }
	  }
	}

  
  private void stripChildren(Node nd) {
    Node kid = nd.getFirstChild();
    while(kid!=null) {
      nd.removeChild(kid);
      kid = nd.getFirstChild();
    }
  }

}

