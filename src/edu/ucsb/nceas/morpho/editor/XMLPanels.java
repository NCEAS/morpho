package edu.ucsb.nceas.editor;

import java.awt.*;
import javax.swing.*;
import java.util.Hashtable;
import javax.swing.tree.*;
import java.util.Enumeration;


/**
 * Class designed for creating a set of nested panels corresponding
 * to Java tree structure
 *
 * @author Dan Higgins
 */
 
public class XMLPanels extends Component
{
 
 public JPanel topPanel;
 public DefaultMutableTreeNode doc;
 public MyDefaultTreeModel treeModel = null;
 
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
        nodeMap = new Hashtable();  // textfield ket mapped to node
        init();
    }
    
    public void setTreeModel(MyDefaultTreeModel tm) {
        treeModel = tm;
    }
    
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
    
    JPanel getDataPanel(DefaultMutableTreeNode node) {
//        JPanel jp = new JPanel(new BorderLayout(0,0));
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp,BoxLayout.Y_AXIS));
        JPanel jp1 = new JPanel(new BorderLayout(0,0));
        JPanel jp2 = new JPanel(new BorderLayout(0,0));
        jp1.setMaximumSize(new Dimension(1000,40));
        jp2.setMaximumSize(new Dimension(1000,40));
//        jp.add(BorderLayout.NORTH,jp1);
//        jp.add(BorderLayout.CENTER,jp2);
        jp.add(jp1);
        jp.add(jp2);
		NodeInfo info = (NodeInfo)(node.getUserObject());
        JLabel jl = new JLabel(info.name);
        jp1.add(BorderLayout.WEST,jl);
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
                name.append(" ");
                name.append(str);
                name.append("=");
                name.append(val);
 //               name.append("\"");
        }
 
 //           name.append('>');
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
  //       NodeList children = node.getChildNodes();
  //       int len = (children != null) ? children.getLength() : 0;
         // loop over child node
  //       String txt ="";
  //       for (int i = 0; i < len; i++) {
  //          Node nd = children.item(i);
  //          if(nd.getNodeType()==Node.TEXT_NODE) {
  //              txt = txt + nd.getNodeValue().trim();    
  //          }
            // if mixed content, text is concatenated into a single string
            if (txt.length()>0) {
            JTextField jtf1 = new JTextField();
 //           JTextArea jtf1 = new JTextArea();
//            jtf1.setLineWrap(true);
//            jtf1.setWrapStyleWord(true);
            jp2.add(BorderLayout.CENTER,jtf1);
            nodeMap.put(jtf1,nd);  // for use in saving changes to text
            jtf1.addActionListener(new dfhAction());
            jtf1.addFocusListener(new dfhFocus());
            if (txt.equals("text")) { txt = ""; }
            jtf1.setText(txt);
            }
         }
        }
        
        
        
        return jp;
        
    }
    
    // get pixels from any component inside topPanel to top of topPanel
    int pixelsFromTop(Component comp) {
        int dist = 0;
        Component parent = comp.getParent(); 
        dist = dist + comp.getY();
        while (parent!=topPanel) {
            comp = parent;
            parent = comp.getParent(); 
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
				    System.out.println(((JTextField)object).getText());
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
				    int dist = pixelsFromTop((Component)object);
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
    
}
