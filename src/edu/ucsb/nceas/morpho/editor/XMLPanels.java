
/**
 *       Name: XMLPanels.java
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-05-23 18:40:39 $'
 * '$Revision: 1.8 $'
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

/**
 * XMLPanels is an alternative view of the TreeModel data
 * structure. Rather than the JTree outline view, this
 * class creates a set of nested panels for showing the
 * hierarchy. Included is support for dynamically loaded
 * 'custom' editor that can be assigned at run time for any
 * special nodes in the hierarchy. An XMLPanels object is
 * usually associated with a JTree view which serves as an
 * 'outline' and selecting any node in the outline displays the
 * nested panel (or custom editor) view of that node and its
 * children. Tree leaves are shown as text input boxes that
 * are labeled with the element name. Editing the text box thus
 * serves as editing text in the original hierarchy. The
 * class is designed to look like a form to the user. One can
 * enter text into the textboxes and then press tab to move to
 * the next box. The display scrolls as the user moves to a
 * textbox out of view.
 * 
 * @author higgins
 */
package edu.ucsb.nceas.morpho.editor;

import java.awt.*;
import javax.swing.*;
import java.util.Hashtable;
import javax.swing.tree.*;
import java.util.Enumeration;
import java.lang.reflect.*;

 
public class XMLPanels extends Component
{
 
 public JPanel topPanel;
 public DefaultMutableTreeNode doc;
// public MyDefaultTreeModel treeModel = null;
 public DefaultTreeModel treeModel = null;
 
 // nodeMap will store the tree node associated with each textfield
 Hashtable nodeMap;
 
    //
    // Constructors
    //

    /** Default constructor. */
    public XMLPanels() {
        this(null);
        }

    /** Constructs a panel tree with the specified root. */
    public XMLPanels(DefaultMutableTreeNode node) {
        this.doc = node;
        nodeMap = new Hashtable();  // textfield key mapped to node
        init();
    }
    
//    public void setTreeModel(MyDefaultTreeModel tm) {
    public void setTreeModel(DefaultTreeModel tm) {
        treeModel = tm;
    }
    
    /**
     */
    void init(){
        topPanel = new JPanel();
        NodeInfo info = (NodeInfo)(doc.getUserObject());
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(info.name),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
		topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.Y_AXIS));    
         // is there anything to do?
         if (doc == null) { return; }

 /*        // iterate over children of this node
         Enumeration nodes = doc.children();
         while(nodes.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)(nodes.nextElement()); 
            doPanels(node, topPanel);
         }
 */
        doPanels(doc,topPanel);
    }
    
    void doPanels(DefaultMutableTreeNode node, JPanel panel) {
    // panel is the surrounding panel for this node
    
    // check to see if there is a special editor for this node
      NodeInfo inf = (NodeInfo)(node.getUserObject());
      String temp = (String)inf.attr.get("editor");
      if (temp!=null) {
        try {
            Object[] Args = new Object[] {node};
            Class[] ArgsClass = new Class[] {DefaultMutableTreeNode.class};
            Class componentDefinition = Class.forName(temp);
            Constructor ArgsConstructor = componentDefinition.getConstructor(ArgsClass);
            Object obj = createObject(ArgsConstructor,Args);
            
            // obj should be a component that can be added to a container (e.g. a descendent
            // of JPanel) with a constructor that takes a node as an argument
            if (obj!=null) {
                panel.add((Component)obj);    
            }
        } 
        catch (ClassNotFoundException e) {
          System.out.println(e);
      } catch (NoSuchMethodException e) {
          System.out.println(e);
      }
      }
      else{
        panel.add(getDataPanel(node));
         // iterate over children of this node
         Enumeration nodes = node.children();
         // loop over child node
         while(nodes.hasMoreElements()) {
            DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(nodes.nextElement());
		    NodeInfo info = (NodeInfo)(nd.getUserObject());
            if (!((info.name).equals("#PCDATA"))) {
            JPanel new_panel = new JPanel();
            new_panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(info.name),
          //      BorderFactory.createLineBorder(Color.black),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
                ));
		    new_panel.setLayout(new BoxLayout(new_panel,BoxLayout.Y_AXIS));    
            panel.add(new_panel);
            doPanels(nd, new_panel);
            }
         }
      }
        
    }
    
    JPanel getDataPanel(DefaultMutableTreeNode node) {
//        JPanel jp = new JPanel(new BorderLayout(0,0));
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp,BoxLayout.Y_AXIS));
        jp.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel jp1 = new JPanel();
        jp1.setLayout(new BoxLayout(jp1,BoxLayout.Y_AXIS));
        jp1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel jp2 = new JPanel();
        jp2.setLayout(new BoxLayout(jp2,BoxLayout.Y_AXIS));
        jp2.setAlignmentX(Component.LEFT_ALIGNMENT);
        jp1.setMaximumSize(new Dimension(600,30));
        jp2.setMaximumSize(new Dimension(600,30));
//        jp.add(BorderLayout.NORTH,jp1);
//        jp.add(BorderLayout.CENTER,jp2);
        jp.add(jp1);
        jp.add(jp2);
		NodeInfo info = (NodeInfo)(node.getUserObject());
        JLabel jl = new JLabel(info.name);
        jp1.add(jl);
 //       JTextField jtf1 = new JTextField();
 //       jp2.add(BorderLayout.CENTER,jtf1);
 
//        if (node.getNodeType()==Node.ELEMENT_NODE) {
        if (true) {
            StringBuffer name = new StringBuffer();
//            name.append('<');
//            name.append(node.getNodeName());
//            name.append(info.name);
            
	    Enumeration keys = info.attr.keys();
	    while (keys.hasMoreElements()) {
	        String str = (String)(keys.nextElement());
	        String val = (String)info.attr.get(str);
 //               name.append(' ');
                name.append(str);
                name.append("=\"");
                name.append(val);
                name.append('"');
        }
 
//            name.append('>');
            jl.setText(name.toString());
            //now check if there are child TEXT nodes
 
         Enumeration nodes = node.children();
         // loop over child node
            String txt ="";
         while(nodes.hasMoreElements()) {
            DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(nodes.nextElement());
		    NodeInfo info1 = (NodeInfo)(nd.getUserObject());
		    if ((info1.name).equals("#PCDATA")) {
		        txt = (String)(info1.attr).get("Value");
         }
            if (txt.length()>0) {
            JTextField jtf1 = new JTextField();
            jtf1.setMaximumSize(new Dimension(600,30));
            jtf1.setPreferredSize(new Dimension(600,30));
//            JTextArea jtf1 = new JTextArea();
//            jtf1.setEditable(false);
//            jtf1.setLineWrap(true);
//            jtf1.setWrapStyleWord(true);
            jp2.add(jtf1);
            nodeMap.put(jtf1,nd);  // for use in saving changes to text
//            jtf1.addActionListener(new dfhAction());
            jtf1.addFocusListener(new dfhFocus());
            if (txt.equals("text")) { txt = ""; }
            jtf1.setText(txt);
            }
         }
        }
        
        
        
        return jp;
        
    }
    
    // get pixels from any component inside topPanel to top of topPanel
    int pixelsFromTop(JComponent comp) {
        int dist = 0;
        JComponent parent = (JComponent)comp.getParent(); 
        dist = dist + comp.getY();
        while (parent!=topPanel) {
            comp = parent;
            parent = (JComponent)comp.getParent(); 
            dist = dist + comp.getY();            
        }
        return dist;
    }
    
class dfhAction implements java.awt.event.ActionListener
{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
				    DefaultMutableTreeNode nd = (DefaultMutableTreeNode)nodeMap.get(object);
		            NodeInfo info = (NodeInfo)(nd.getUserObject());
                    info.attr.put("Value",((JTextField)object).getText());
		//		    System.out.println(((JTextField)object).getText());
				    if (treeModel!=null) {
				        treeModel.reload();
				    }
				}
		}
}

	class dfhFocus extends java.awt.event.FocusAdapter
	{
		public void focusLost(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
				    DefaultMutableTreeNode nd = (DefaultMutableTreeNode)nodeMap.get(object);
		            NodeInfo info = (NodeInfo)(nd.getUserObject());
                    info.attr.put("Value",((JTextField)object).getText());
//				    System.out.println(((JTextField)object).getText());
				    if (treeModel!=null) {
				        treeModel.reload();
				    }
				}
		}
		
		public void focusGained(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
				    int dist = pixelsFromTop((JComponent)object);
	//			    System.out.println("Distance = "+dist);
				    topPanel.scrollRectToVisible(new Rectangle(0,dist,50,50));
				}
		}
	}
	
	

/*
	class dfhFocus extends java.awt.event.FocusAdapter
	{
		public void focusLost(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
			if (object instanceof JTextField)
				{
				    DefaultMutableTreeNode nd = (DefaultMutableTreeNode)nodeMap.get(object);
		            NodeInfo info = (NodeInfo)(nd.getUserObject());
				    ((JTextField)object).setText((String)info.attr.get("Value"));				    
	//			    System.out.println(((JTextField)object).getText());
				}
		}
	}
*/
   
    
public static Object createObject(Constructor constructor, Object[] arguments) {
//      System.out.println ("Constructor: " + constructor.toString());
      Object object = null;
      try {
        object = constructor.newInstance(arguments);
//        System.out.println ("Object: " + object.toString());
        return object;
      } catch (InstantiationException e) {
          System.out.println(e);
      } catch (IllegalAccessException e) {
          System.out.println(e);
      } catch (IllegalArgumentException e) {
          System.out.println(e);
      } catch (InvocationTargetException e) {
          System.out.println(e);
      }
      return object;
   }
    
}