/**
 *       Name: AttributePanel.java
 *    Purpose: Example dynamic editor class for XMLPanel
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-02-17 18:53:47 $'
 * '$Revision: 1.3 $'
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
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTree;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.ucsb.nceas.morpho.editor.DocFrame;

import edu.ucsb.nceas.utilities.*;

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributePage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.DOMImplementation;
import org.apache.xerces.dom.DOMImplementationImpl;

/**
 * AttributePanel is an example of a special panel editor for
 * use with the DocFrame class. It is designed to
 *
 * @author higgins
 */
public class AttributePanel extends JPanel
{

  public AttributePanel(DefaultMutableTreeNode node) {
    final DefaultMutableTreeNode fnode = node;
    JPanel jp = this;
    jp.setLayout(new BoxLayout(jp,BoxLayout.Y_AXIS));
    jp.setAlignmentX(Component.LEFT_ALIGNMENT);
    jp.setMaximumSize(new Dimension(800,600));
    final AbstractWizardPage awp = WizardPageLibrary.getPage(DataPackageWizardInterface.ATTRIBUTE_PAGE);
    jp.add(awp);
     
    DocFrame df = DocFrame.currentDocFrameInstance;
    final Node domNode = df.writeToDOM(node);
    
    final OrderedMap om = XMLUtilities.getDOMTreeAsXPathMap(domNode);
    
    ((AttributePage)awp).setXPathRoot("/attribute");
    awp.setPageData(om);
 
    JPanel controlsPanel = new JPanel();
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();  //Xerces specific
        final Document doc = domNode.getOwnerDocument();
        try{
//          Log.debug(1, "InDOM: "+ XMLUtilities.getDOMTreeAsString(domNode));
          XMLUtilities.getXPathMapAsDOMTree(awp.getPageData(), domNode);
//          Log.debug(1, "OutDOM: "+ XMLUtilities.getDOMTreeAsString(domNode));
          JTree domtree = new DOMTree(doc);
          DefaultMutableTreeNode root = (DefaultMutableTreeNode)domtree.getModel().getRoot();
          DefaultMutableTreeNode parent = (DefaultMutableTreeNode)fnode.getParent();
          int index = parent.getIndex(fnode);
          parent.remove(index);
          parent.insert(root, index);
          DocFrame df1 = DocFrame.currentDocFrameInstance;
					DefaultMutableTreeNode fn = df1.findTemplateNodeByName("attribute");
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
          Log.debug(5, "Problem in AttributePanel");
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
    jp.add(controlsPanel);
    
    jp.setVisible(true);
    
  }

}

