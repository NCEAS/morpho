/**
 *        Name: DocFrame.java
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-06-07 23:27:42 $'
 * '$Revision: 1.19 $'
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


import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.URL;
import java.io.*;
import javax.swing.event.*;
import com.arbortext.catalog.*;
import org.xml.sax.SAXException;
import org.apache.xalan.xslt.XSLTProcessorFactory;
import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTResultTarget;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xpath.xml.*;
import java.util.PropertyResourceBundle;
import javax.swing.*;
import javax.swing.tree.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;
import edu.ucsb.nceas.morpho.framework.*;


/**
 * DocFrame is an example container for an XML editor which
 * shows combined outline and nested panel views of an XML
 * document.
 * 
 * @author higgins
 */
public class DocFrame extends javax.swing.JFrame
{
    /** counter for name */
    public static int counter = 0;
  
    /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
    ConfigXML config;
  
    EditorPlugin controller = null;
  
    File file;
    
    /* the string representation of the XML being displayed */
    String XMLTextString;
    
    /* the publicID, if available, or the systemID, if available,
     * or the rootnode name
     */
    String doctype = null;
    
    String rootnodeName = null;
    String publicIDString = null;
    String systemIDString = null;
    
    // various global variables
    public DefaultTreeModel treeModel;
    public DefaultMutableTreeNode rootNode;
    public DTDTree dtdtree;
    DefaultMutableTreeNode selectedNode;
    public JTree tree;
    StringBuffer sb; 
    StringBuffer start;
    Stack tempStack;
    
    Catalog myCatalog;
    String dtdfile;
    int numlevels = 9;
    
    boolean treeValueFlag = true;
    
    //* nodeCopy is the 'local clipboard' for node storage
    DefaultMutableTreeNode nodeCopy = null;
    
//    javax.swing.JMenuItem CMmenuItem;
    javax.swing.JMenuItem menuItem;
    javax.swing.JMenuItem CardmenuItem;
    javax.swing.JMenuItem DeletemenuItem;
    javax.swing.JMenuItem DupmenuItem;
    javax.swing.JMenuItem AttrmenuItem;
    javax.swing.JMenuItem CopymenuItem;
    javax.swing.JMenuItem ReplacemenuItem;
    
    
 /**
  * This constructor builds the contents of the DocFrame Display
  */
  
	public DocFrame()
	{
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(600,305);
		setVisible(false);
		getContentPane().add(OutputScrollPanel);
		getContentPane().add(BorderLayout.CENTER, NestedPanelScrollPanel);
		ControlPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		getContentPane().add(BorderLayout.NORTH, ControlPanel);
		reload.setText("Reload Tree");
		reload.setActionCommand("Reload Tree");
		ControlPanel.add(reload);
		DTDParse.setText("Parse DTD");
		DTDParse.setActionCommand("Parse DTD");
		ControlPanel.add(DTDParse);
		TestButton.setText("Test");
		TestButton.setActionCommand("Test");
		ControlPanel.add(TestButton);
		TestButton.setVisible(false);
		SaveXML.setText("Save XML...");
		SaveXML.setActionCommand("Save XML...");
		ControlPanel.add(SaveXML);
		EditingExit.setText("Exit Editing");
		EditingExit.setActionCommand("jbutton");
		ControlPanel.add(EditingExit);
		saveFileDialog.setMode(FileDialog.SAVE);
		saveFileDialog.setTitle("Save");
		//$$ saveFileDialog.move(0,306);
		//}}
		JSplitPane DocControlPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT
		       , OutputScrollPanel, NestedPanelScrollPanel);
		     
		DocControlPanel.setOneTouchExpandable(true);
    DocControlPanel.setDividerLocation(250); 
    getContentPane().add(BorderLayout.CENTER, DocControlPanel);

		//{{INIT_MENUS
		//}}
//    CMmenuItem = new JMenuItem("Content Model = ");
//    popup.add(CMmenuItem);
    CardmenuItem = new JMenuItem("One Element Allowed");
 
    CopymenuItem = new JMenuItem("Copy Node & Children");
    popup.add(CopymenuItem);
    ReplacemenuItem = new JMenuItem("Replace Selected Node from Clipboard");
    //ReplacemenuItem.setEnabled(false);
    popup.add(ReplacemenuItem);
    popup.add(new JSeparator());
    popup.add(CardmenuItem);
    popup.add(new JSeparator());
    DupmenuItem = new JMenuItem("Duplicate");
    popup.add(DupmenuItem);
    DeletemenuItem = new JMenuItem("Delete");
    popup.add(DeletemenuItem);
    popup.add(new JSeparator());
    AttrmenuItem = new JMenuItem("Edit Attributes");
    popup.add(AttrmenuItem);
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		SymChange lSymChange = new SymChange();
		reload.addActionListener(lSymAction);
		DTDParse.addActionListener(lSymAction);
		TestButton.addActionListener(lSymAction);
		SaveXML.addActionListener(lSymAction);
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		EditingExit.addActionListener(lSymAction);
		//}}
		DeletemenuItem.addActionListener(lSymAction);
		DupmenuItem.addActionListener(lSymAction);
		AttrmenuItem.addActionListener(lSymAction);
		CopymenuItem.addActionListener(lSymAction);
		ReplacemenuItem.addActionListener(lSymAction);
    //Create the popup menu.
    javax.swing.JPopupMenu popup = new JPopupMenu();
		
		rootNode = newNode("Configuration");
		treeModel = new DefaultTreeModel(rootNode);

    tree = new JTree(treeModel);
		OutputScrollPanel.getViewport().add(tree);
    tree.setCellRenderer(new XMLTreeCellRenderer());
		
		tree.setShowsRootHandles(true);
    tree.setEditable(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setShowsRootHandles(true);
    tree.putClientProperty("JTree.lineStyle", "Angled");
		
		SymTreeSelection lSymTreeSelection = new SymTreeSelection();
		tree.addTreeSelectionListener(lSymTreeSelection);
	
		MouseListener popupListener = new PopupListener();
    tree.addMouseListener(popupListener);
    
    
	}

	/**
	 *  Constructor which adds a title string to the Frame
	 */
	 
	public DocFrame(String sTitle)
	{
		this();
		setTitle(sTitle);
	}

  /** 
   *  Constructor which adds a title and passes the xml to
   *  display as a string; puts XML into tree
   */
   
	public DocFrame(String sTitle, String doctext)
	{
		this();
		setTitle(sTitle);
		XMLTextString = doctext;
		putXMLintoTree();
    tree.setSelectionRow(0);
    
    if (dtdfile!=null) {
		  dtdtree = new DTDTree(dtdfile);
		  dtdtree.setRootElementName(rootnodeName);
		  dtdtree.parseDTD();
		
	    rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
		  treeUnion(rootNode,dtdtree.rootNode);
		}
		treeModel.reload();
		tree.setModel(treeModel);
    tree.setSelectionRow(0);
    
	}
	
	public DocFrame(File file)
	{
	    this();
	    this.file = file;
	}

	public DocFrame(ClientFramework cf, String sTitle, String doctext) 
	{
	    this(sTitle, doctext);
	    this.framework = cf;
	    counter++;
	    setName("XMLEditor"+counter);
	}
	
	
	public void setFile(File f) {
	    file = f;
	}
	
	public void setDoctype(String doctype) {
	    this.doctype = doctype;
	}   

	public void setVisible(boolean b)
	{
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
	}

	static public void main(String args[])
	{
		(new DocFrame()).setVisible(true);
	}

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets and menu bar
		Insets insets = getInsets();
		javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
		int menuBarHeight = 0;
		if (menuBar != null)
			menuBarHeight = menuBar.getPreferredSize().height;
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JScrollPane OutputScrollPanel = new javax.swing.JScrollPane();
	javax.swing.JScrollPane NestedPanelScrollPanel = new javax.swing.JScrollPane();
	javax.swing.JPanel ControlPanel = new javax.swing.JPanel();
	javax.swing.JButton reload = new javax.swing.JButton();
	javax.swing.JButton DTDParse = new javax.swing.JButton();
	javax.swing.JButton TestButton = new javax.swing.JButton();
	javax.swing.JButton SaveXML = new javax.swing.JButton();
	javax.swing.JButton EditingExit = new javax.swing.JButton();
	java.awt.FileDialog saveFileDialog = new java.awt.FileDialog(this);
	//}}

	//{{DECLARE_MENUS
	//}}
	//Create the popup menu.
  javax.swing.JPopupMenu popup = new JPopupMenu();

	
public void writeInfo() {
  try{
    FileReader in = new FileReader(file);
    StringWriter out = new StringWriter();
    int c;
    while ((c = in.read()) != -1) {
        out.write(c);
    }
    in.close();
    out.close();
    XMLTextString = out.toString();
		putXMLintoTree();
    tree.setSelectionRow(0);
    }
	catch (Exception e) {;}
    
}

class SymAction implements java.awt.event.ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == DeletemenuItem)
				Del_actionPerformed(event);
			else if (object == DupmenuItem)
				Dup_actionPerformed(event);
			else if (object == AttrmenuItem)
				Attr_actionPerformed(event);
			else if (object == CopymenuItem)
				Copy_actionPerformed(event);
			else if (object == ReplacemenuItem)
				Replace_actionPerformed(event);
			else if (object == reload)
				reload_actionPerformed(event);
			else if (object == DTDParse)
				DTDParse_actionPerformed(event);
			else if (object == TestButton)
				TestButton_actionPerformed(event);
			else if (object == SaveXML)
				SaveXML_actionPerformed(event);
			else if (object == EditingExit)
				EditingExit_actionPerformed(event);
		}
}

		
void putXMLintoTree() {
  if (XMLTextString!=null) {
    CatalogEntityResolver cer = new CatalogEntityResolver();
    ConfigXML config = new ConfigXML("lib/config.xml");
    String local_dtd_directory =config.get("local_dtd_directory",0);     
    String local_xml_directory =config.get("local_xml_directory",0);     
            
    String xmlcatalogfile = local_dtd_directory+"/catalog"; 
    try {
      myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      myCatalog.parseCatalog(xmlcatalogfile);
      cer.setCatalog(myCatalog);
    }
    catch (Exception e) {System.out.println("Problem creating Catalog!");}
    try {
      StringReader sr = new StringReader(XMLTextString);
      String parserName = "org.apache.xerces.parsers.SAXParser";
      XMLReader parser = null;
      // Get an instance of the parser
      parser = XMLReaderFactory.createXMLReader(parserName);
      XMLDisplayHandler mh = new XMLDisplayHandler(treeModel);
      parser.setContentHandler(mh);
      parser.setProperty("http://xml.org/sax/properties/lexical-handler",mh);
      
	    parser.setEntityResolver(cer);
	    InputSource is = new InputSource(sr);

      parser.parse(is);
      DefaultMutableTreeNode rt = (DefaultMutableTreeNode)treeModel.getRoot();
      if (mh.getPublicId()!=null) {
        doctype = mh.getPublicId();
        publicIDString = doctype;
      }
      else if (mh.getSystemId()!=null) {
        doctype = mh.getSystemId(); 
      }
      else {
        doctype = ((NodeInfo)rt.getUserObject()).toString();
      }
      rootnodeName = mh.getDocname();
      System.out.println("doctype = " + doctype);
      String temp = myCatalog.resolvePublic(doctype,null);
      if (temp!=null) {
        if (temp.startsWith("file:")) {
          temp = temp.substring(5,temp.length());
        }
        System.out.println("cat out: "+temp);
        dtdfile = temp;
        systemIDString = temp;
      }
      } 
      catch (Exception e) { 
        System.err.println(e.toString());
      }
    }
}

	public DefaultMutableTreeNode newNode (Object name) {
	    NodeInfo ni = new NodeInfo(name.toString());
	    DefaultMutableTreeNode node = new DefaultMutableTreeNode(ni);
	    return node;
	}

	public void setTreeValueFlag(boolean flg) {
	 treeValueFlag = flg; 
	}
	
	class SymTreeSelection implements javax.swing.event.TreeSelectionListener
	{
		public  void valueChanged(javax.swing.event.TreeSelectionEvent event)
		{
			Object object = event.getSource();
			if (object == tree)
				tree_valueChanged(event);
		}
	}

	 void tree_valueChanged(javax.swing.event.TreeSelectionEvent event)
	{
	  if (treeValueFlag) {
	    TreePath tp = event.getNewLeadSelectionPath();
	    if (tp!=null) {
	    Object ob = tp.getLastPathComponent();
	    DefaultMutableTreeNode node = null;
	    if (ob!=null) {node =(DefaultMutableTreeNode)ob;}
         selectedNode = node;

         NodeInfo ni = (NodeInfo)node.getUserObject();
         
         if ((ni.getCardinality().equals("NOT SELECTED"))
                  ||(ni.getCardinality().equals("SELECTED"))) {
            for (Enumeration eee = (node.getParent()).children();eee.hasMoreElements();) {
                DefaultMutableTreeNode nnn = (DefaultMutableTreeNode)eee.nextElement();
                NodeInfo ni1 = (NodeInfo)nnn.getUserObject();
                ni1.setCardinality("NOT SELECTED");
            }
            ni.setCardinality("SELECTED");
            tree.invalidate();
            OutputScrollPanel.repaint();
         }
         
         
         XMLPanels xp = new XMLPanels(node);
         xp.setTreeModel(treeModel);
         xp.setContainer(this);
         xp.setTree(tree);
         NestedPanelScrollPanel.getViewport().add(xp.topPanel);
         xp.invalidate();
         NestedPanelScrollPanel.repaint();
      }
		}
		treeValueFlag = true;
		
	}  

	class SymChange implements javax.swing.event.ChangeListener
	{
		public void stateChanged(javax.swing.event.ChangeEvent event)
		{
		}
	}
	
	class PopupListener extends MouseAdapter {
    // on the Mac, popups are triggered on mouse pressed, while mouseReleased triggers them
    // on the PC; use the trigger flag to record a trigger, but do not show popup until the
    // mouse released event
    boolean trigger = false;
    public void mousePressed(MouseEvent e) {
      // maybeShowPopup(e);
      if (e.isPopupTrigger()) {
        trigger = true;
      }  
    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
      if ((e.isPopupTrigger())||(trigger)) {
        trigger = false;
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        tree.setSelectionPath(selPath);
        if (selectedNode!=null) {
          NodeInfo ni = (NodeInfo)selectedNode.getUserObject();
          CardmenuItem.setText("Number: "+ni.getCardinality());
//          String temp = (String)ni.attr.get("copyNode");
//          if ((temp!=null)&&(temp.equalsIgnoreCase("false"))){
          if (ni.getCardinality().equalsIgnoreCase("ONE")) {
            DupmenuItem.setEnabled(false);
            DeletemenuItem.setEnabled(false);
          }
          else {
            DupmenuItem.setEnabled(true); 
            DeletemenuItem.setEnabled(true);
          }
          if (ni.getCardinality().equalsIgnoreCase("OPTIONAL")) {
            DupmenuItem.setEnabled(false);
          }
//          temp = (String)ni.attr.get("deleteNode");
//          if ((temp!=null)&&(temp.equalsIgnoreCase("false"))){
//            DeletemenuItem.setEnabled(false);   
//          }
//          else {
//            DeletemenuItem.setEnabled(true); 
//          }
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
              
  }	
	
	
public DefaultMutableTreeNode deepNodeCopy(DefaultMutableTreeNode node) {
  DefaultMutableTreeNode newnode = null; 
  try{
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ObjectOutputStream s = new ObjectOutputStream(out);
    s.writeObject(node);
    s.flush();
        
    // now read it
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    ObjectInputStream os = new ObjectInputStream(in);
    newnode = (DefaultMutableTreeNode)os.readObject();
    }
    catch (Exception e) {
      System.out.println("Exception in creating copy of node!");
    }
  return newnode;
}

void Copy_actionPerformed(java.awt.event.ActionEvent event) {
  TreePath tp = tree.getSelectionPath();
	if (tp!=null) {
	  Object ob = tp.getLastPathComponent();
	  DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
    nodeCopy = deepNodeCopy(node); 
  }
}
void Replace_actionPerformed(java.awt.event.ActionEvent event) {
  TreePath tp = tree.getSelectionPath();
	if (tp!=null) {
	  Object ob = tp.getLastPathComponent();
	  DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
	  DefaultMutableTreeNode localcopy = deepNodeCopy(nodeCopy);
	  // simple node comparison
	  String nodename = ((NodeInfo)node.getUserObject()).getName();
	  if (nodeCopy!=null) {
	    String savenodename = ((NodeInfo)localcopy.getUserObject()).getName();
	    if (nodename.equals(savenodename)) {
	      DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent(); 
	      int indx = parent.getIndex(node);
	      parent.insert(localcopy, indx+1);
	      parent.remove(indx);
	      tree.expandPath(tp);
	      treeModel.reload(parent);
	      tree.setSelectionPath(tp);
	    }
	  }
  }
}
	
	
void Attr_actionPerformed(java.awt.event.ActionEvent event) {
  TreePath tp = tree.getSelectionPath();
	if (tp!=null) {
	  Object ob = tp.getLastPathComponent();
	  DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
//	  Hashtable attr = ((NodeInfo)node.getUserObject()).attr;
	  String title = "Attributes of "+ ((NodeInfo)node.getUserObject()).getName();
    AttributeEditDialog aed = new AttributeEditDialog(this,title,node);
    aed.show();
  }
}
	
void Dup_actionPerformed(java.awt.event.ActionEvent event) {
  TreePath tp = tree.getSelectionPath();
	if (tp!=null) {
	  Object ob = tp.getLastPathComponent();
	  DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
	  DefaultMutableTreeNode par = (DefaultMutableTreeNode)node.getParent();
	  int iii = par.getIndex(node);
	  DefaultMutableTreeNode newnode = deepNodeCopy(node);
	  if (((NodeInfo)newnode.getUserObject()).getCardinality().equalsIgnoreCase("SELECTED")) {
	     ((NodeInfo)newnode.getUserObject()).setCardinality("NOT SELECTED");
	  }
	  tree.expandPath(tp);
	  par.insert(newnode,iii+1);
	  treeModel.reload(par);
	  tree.setSelectionPath(tp);
	}
}

void Del_actionPerformed(java.awt.event.ActionEvent event) {
  int selRow = -1;
  TreePath currentSelection = tree.getSelectionPath();
  int[] selRows = tree.getSelectionRows();
  if ((selRows!=null)&&(selRows.length>0)) {
    selRow = selRows[0];
  }
  if (currentSelection != null) {
    DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
    NodeInfo ni = (NodeInfo)currentNode.getUserObject();
    String curNodeName = ni.getName();
    int cnt = 0;
    MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
    if (parent != null) {
      Enumeration eee = parent.children();
      while (eee.hasMoreElements()) {
        DefaultMutableTreeNode cn = (DefaultMutableTreeNode)eee.nextElement();
        NodeInfo ni1 = (NodeInfo)cn.getUserObject();
        String name = ni1.getName();
        if (name.equals(curNodeName)) {
          cnt++;
        }
      }
    }
    if ((parent != null)) {
      if (cnt>1) {
        treeModel.removeNodeFromParent(currentNode);
      }
      else {
        if ((ni.getCardinality().equalsIgnoreCase("OPTIONAL")) 
        || (ni.getCardinality().equalsIgnoreCase("ZERO to MANY")) ) {
          treeModel.removeNodeFromParent(currentNode);
        }
      }
      if (selRow>0) {
        tree.setSelectionRow(selRow-1);
      }
      return;
    }
  } 

  // Either there was no selection, or the root was selected.
  Toolkit.getDefaultToolkit().beep();
}	
	

void reload_actionPerformed(java.awt.event.ActionEvent event)
	{
		treeModel.reload();
		tree.setModel(treeModel);
		
	}
	

	void DTDParse_actionPerformed(java.awt.event.ActionEvent event)
	{
//		dtdtree = new DTDTree(dtdfile);
//		dtdtree.parseDTD();
		tree.setModel(dtdtree.treeModel);
	}

/*
 * write the tree starting at the indicated node to a file 'fn'
 */
	void writeXML (DefaultMutableTreeNode node, String fn) {
	  File outputFile = new File(fn);
	  try {
	    FileWriter out = new FileWriter(outputFile);
      tempStack = new Stack();
	    start = new StringBuffer();
	    write_loop(node);
	    String str1 = start.toString();
	    
	    String doctype = "";
	    if (publicIDString!=null) {
	      String rootNodeName = ((NodeInfo)node.getUserObject()).getName();
	      String temp = "";
	      if (publicIDString!=null) temp = "\""+publicIDString+"\"";
	      String temp1 = "";
	      if (systemIDString!=null) temp1 = "\"file:///"+systemIDString+"\"";
	      doctype = "<!DOCTYPE "+rootNodeName+" PUBLIC "+temp+" "+temp1+">\n";
	    }
	    str1 = "<?xml version=\"1.0\"?>\n"+doctype+str1;
	    
	    out.write(str1);
	    out.close();
	  }
	  catch (Exception e) {}
	}

/*
 * write the tree starting at the indicated node to a file 'fn'
 */
	String writeXMLString (DefaultMutableTreeNode node) {
      tempStack = new Stack();
	    start = new StringBuffer();
	    write_loop(node);
	    String str1 = start.toString();
	    
	    String doctype = "";
	    if (publicIDString!=null) {
	      String rootNodeName = ((NodeInfo)node.getUserObject()).getName();
	      String temp = "";
	      if (publicIDString!=null) temp = "\""+publicIDString+"\"";
	      String temp1 = "";
	      if (systemIDString!=null) temp1 = "\"file:///"+systemIDString+"\"";
	      doctype = "<!DOCTYPE "+rootNodeName+" PUBLIC "+temp+" "+temp1+">\n";
	    }
	    str1 = "<?xml version=\"1.0\"?>\n"+doctype+str1;
	    
  return str1;
	}
	
	
	/*
	 * recursive routine to create xml output
	 */
	void write_loop (DefaultMutableTreeNode node) {
	  String name;
	  String end;
	  NodeInfo ni = (NodeInfo)node.getUserObject();
	  name = ni.name;
	  if (!((ni.getCardinality()).equals("NOT SELECTED"))) {
	    // completely ignore NOT SELECTED nodes AND their children
	    if (!name.equals("(CHOICE)")) {
	      // ignore (CHOICE) nodes but process their children
	      start.append("<"+name+" ");  
	    
	      Enumeration keys = (ni.attr).keys();
	      while (keys.hasMoreElements()) {
	        String str = (String)(keys.nextElement());
	        String val = (String)((ni.attr).get(str));
	        start.append(str+"=\""+val+"\" ");
	      }
	      start.append(">\n");
	      if (ni.getPCValue()!=null) {   // text node info
	        start.append(ni.getPCValue());
	      }
	      end = "</"+name+">\n";
	      tempStack.push(end);
	    }
	  //}
	  Enumeration enum = node.children();
	  while (enum.hasMoreElements()) {  // process child nodes 
	    DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(enum.nextElement());
	    NodeInfo ni1 = (NodeInfo)nd.getUserObject();
	    if (ni1.name.equals("#PCDATA")) {
	      start.append(ni1.getPCValue());
	    }
	    else {
	      write_loop(nd);
	    }
	  }
	    if (!name.equals("(CHOICE)")) {
	      start.append((String)(tempStack.pop()));
	    }
	  }
	}
	
	
/* -----------------------------------------------
	 * code for combining the content of two DefaultMutableTreeNode trees
	 * inputTree is modified based on content of template tree
	 * template may be based on DTD and thus provides info like cardinality
	 * It is assumed that nodes use NodeInfo user objects
	 */
	 
/*
 * modify input tree by adding info in template
 * input and template are root nodes of trees
 * input tree will be modified using template
 */
void treeUnion(DefaultMutableTreeNode input, DefaultMutableTreeNode template) {
  DefaultMutableTreeNode tNode;
  DefaultMutableTreeNode nd2;
  DefaultMutableTreeNode pqw;
  DefaultMutableTreeNode qw = null;
  // first check to see if root nodes have same names
  if (!compareNodes(input, template)) {
    System.out.println( "Root nodes do not match!!!");
  }
  else {
    // root nodes match, so start comparing children
    Vector nextLevelInputNodes;
    Vector nextLevelTemplateNodes;
    Vector currentLevelInputNodes = new Vector();
    currentLevelInputNodes.addElement(input);
    Vector currentLevelTemplateNodes = new Vector();
    currentLevelTemplateNodes.addElement(template);
    for (int j=0;j<numlevels;j++) {
      nextLevelInputNodes = new Vector();
      for (Enumeration enum = currentLevelInputNodes.elements();enum.hasMoreElements();) {
        DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
        for (Enumeration qq = nd.children();qq.hasMoreElements();) {
          DefaultMutableTreeNode nd1 = (DefaultMutableTreeNode)qq.nextElement();
          nextLevelInputNodes.addElement(nd1);
        }
      }
      nextLevelTemplateNodes = new Vector();
      for (Enumeration enum1 = currentLevelTemplateNodes.elements();enum1.hasMoreElements();) {
        DefaultMutableTreeNode ndt = (DefaultMutableTreeNode)enum1.nextElement();
        for (Enumeration qq1 = ndt.children();qq1.hasMoreElements();) {
          DefaultMutableTreeNode ndt1 = (DefaultMutableTreeNode)qq1.nextElement();
          nextLevelTemplateNodes.addElement(ndt1);
        }
      }
      // now have a list of all elements in input and template trees at the level being processed
      // loop over all the template nodes at the 'next' level
      Enumeration enum = nextLevelTemplateNodes.elements();
      while (enum.hasMoreElements()) {
        tNode = (DefaultMutableTreeNode)enum.nextElement();
        Vector hits = getMatches(tNode, nextLevelInputNodes);
        // merge hits with template node
        Enumeration en1 = hits.elements();
        while (en1.hasMoreElements()) {
          DefaultMutableTreeNode tempnode = (DefaultMutableTreeNode)en1.nextElement();
          mergeNodes(tempnode, tNode);  
        }
        // Here we need to add nodes that are 'missing'
        // go to parent of tnode; find matching nodes in input at same level; add children
        DefaultMutableTreeNode newnode = null;
        if (hits.size()==0) {
            DefaultMutableTreeNode ptNode = (DefaultMutableTreeNode)tNode.getParent();
            int index = ptNode.getIndex(tNode);
            Vector parent_hits = getMatches(ptNode, currentLevelInputNodes);
            Enumeration en2 = parent_hits.elements();
            while (en2.hasMoreElements()) {
              DefaultMutableTreeNode ind = (DefaultMutableTreeNode)en2.nextElement();
              newnode = deepNodeCopy(tNode);
 //             newnode = (DefaultMutableTreeNode)tNode.clone();
              ind.insert(newnode,index);
//              nextLevelInputNodes.addElement(newnode);
            }
            
          if (((NodeInfo)tNode.getUserObject()).getName().equals("(CHOICE)")) {
            // in this case, one of the 'children' of the CHOICE node probably exists
            // in the Info nodes for this level
            int indx1 = -1;
            Enumeration q = tNode.children();
            while (q.hasMoreElements()) {
              DefaultMutableTreeNode nd1 = (DefaultMutableTreeNode)q.nextElement();
              Enumeration ww = newnode.children();
              while (ww.hasMoreElements()) {
                qw = (DefaultMutableTreeNode)ww.nextElement();
                if (simpleCompareNodes(nd1,qw)) {
                  indx1 = newnode.getIndex(qw);
                  newnode.remove(qw);
                }
              }
              Vector choice_hits = simpleGetMatches(nd1, nextLevelInputNodes);
              Enumeration qq = choice_hits.elements();
              while (qq.hasMoreElements()) {
                nd2 = (DefaultMutableTreeNode)qq.nextElement();
                newnode.insert(nd2, indx1);
              }
            }
          }
            
        }
     // recalculate nextLevelInput  
      nextLevelInputNodes = new Vector();
      for (Enumeration enumrecalc = currentLevelInputNodes.elements();enumrecalc.hasMoreElements();) {
        DefaultMutableTreeNode ndrecalc = (DefaultMutableTreeNode)enumrecalc.nextElement();
        for (Enumeration qqrecalc = ndrecalc.children();qqrecalc.hasMoreElements();) {
          DefaultMutableTreeNode nd1recalc = (DefaultMutableTreeNode)qqrecalc.nextElement();
          nextLevelInputNodes.addElement(nd1recalc);
        }
      }

      }
      currentLevelInputNodes = nextLevelInputNodes;
      currentLevelTemplateNodes = nextLevelTemplateNodes;
    }  // end levels loop
  } //end else
}
  

/*
 * return a Vector containing all the nodes in a Vector that match 'match'
 */
Vector getMatches(DefaultMutableTreeNode match, Vector vec) {
  Vector matches = new Vector();
  Enumeration enum = vec.elements();
  while (enum.hasMoreElements()) {
    DefaultMutableTreeNode tn = (DefaultMutableTreeNode)enum.nextElement();
    if ( compareNodes(tn,match)) {
      matches.addElement(tn);  
    }
  }
  return matches;
}

Vector simpleGetMatches(DefaultMutableTreeNode match, Vector vec) {
  Vector matches = new Vector();
  Enumeration enum = vec.elements();
  while (enum.hasMoreElements()) {
    DefaultMutableTreeNode tn = (DefaultMutableTreeNode)enum.nextElement();
    if ( simpleCompareNodes(tn,match)) {
      matches.addElement(tn);  
    }
  }
  return matches;
}

	 
boolean compareNodes(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2) {
  boolean ret = false;
  String node1Str = pathToString(node1);
  String node2Str = pathToString(node2);
  if (node1Str.equals(node2Str)) ret = true;
//  NodeInfo node1ni = (NodeInfo)node1.getUserObject();
//  NodeInfo node2ni =  (NodeInfo)node2.getUserObject();
//  if (node1ni.getName().equals(node2ni.getName())) ret = true;
  return ret;
}

boolean simpleCompareNodes(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2) {
  boolean ret = false;
  NodeInfo node1ni = (NodeInfo)node1.getUserObject();
  NodeInfo node2ni =  (NodeInfo)node2.getUserObject();
  if (node1ni.getName().equals(node2ni.getName())) ret = true;
  return ret;
}


String pathToString(DefaultMutableTreeNode node) {
  StringBuffer sb = new StringBuffer();
  TreeNode[] tset = node.getPath();
  for (int i=0;i<tset.length;i++) {
    String temp = ((NodeInfo)((DefaultMutableTreeNode)tset[i]).getUserObject()).getName();
    sb.append(temp+"/");
  }
  return sb.toString();
}

void mergeNodes(DefaultMutableTreeNode input, DefaultMutableTreeNode template) {
  if (compareNodes(input,template)) {
    NodeInfo inputni = (NodeInfo)input.getUserObject();
    NodeInfo templateni = (NodeInfo)template.getUserObject();
    inputni.setCardinality(templateni.getCardinality());
  }
}
//------------------------------------------------------------	 

	void TestButton_actionPerformed(java.awt.event.ActionEvent event)
	{
	  rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
		treeUnion(rootNode,dtdtree.rootNode);
	}
	

	void SaveXML_actionPerformed(java.awt.event.ActionEvent event)
	{
			saveFileDialog.setVisible(true);
			String file = saveFileDialog.getFile();
			if (file!=null) {
			  file = saveFileDialog.getDirectory()+file;
	      rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
		    writeXML(rootNode,file);
		  }
			 
	}

	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosed(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == DocFrame.this)
				DocFrame_windowClosed(event);
		}
	}

	void DocFrame_windowClosed(java.awt.event.WindowEvent event)
	{
			  if (framework!=null) {
			    framework.removeWindow(this);  
			  }
		    	this.setVisible(false);    // hide the Frame
	//	    	this.dispose();            // free the system resources
	}
	
	public void setController(EditorPlugin con) {
	    this.controller = con;
	}
	
	

	void EditingExit_actionPerformed(java.awt.event.ActionEvent event)
	{
		this.hide();
	  String xmlout = writeXMLString(rootNode);
		controller.fireEditingCompleteEvent(this, xmlout);
	}
}