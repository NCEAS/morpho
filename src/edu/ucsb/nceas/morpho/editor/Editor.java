package edu.ucsb.nceas.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.wutka.dtd.*;
import java.io.*;
import java.util.*;

import javax.swing.tree.*;
import javax.swing.event.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;



/**
 * A basic JFC 1.1 based application.
 */
//public class Editor extends javax.swing.JFrame
public class Editor extends JPanel
{
    Vector elementnames;
    public DTD dtd = null;
    StringBuffer sb; 
    StringBuffer start_buffer;
    StringBuffer start;
    StringBuffer end_buffer;
    Stack tempStack;
    int indent = 0;

    int levels = 9;
    DTDItem oldItem;
    public DefaultMutableTreeNode rootNode;
    public MyDefaultTreeModel treeModel;
    public JTree tree;
    public String xmlstring;
    
    DefaultMutableTreeNode selectedNode = null;
    
    javax.swing.JMenuItem CMmenuItem;
    javax.swing.JMenuItem menuItem;
    javax.swing.JMenuItem CardmenuItem;
    javax.swing.JMenuItem DeletemenuItem;
    javax.swing.JMenuItem DupmenuItem;
    
    
    
	public Editor()
	{
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setLayout(new BorderLayout(0,0));
		setVisible(false);
		SummaryPanel.setLayout(new BorderLayout(0,0));
		SummaryPanel.add(BorderLayout.NORTH, SummaryLabel);
		add(BorderLayout.CENTER, SummaryPanel);
		JPanel1.setLayout(new BorderLayout(0,0));
		SummaryPanel.add(BorderLayout.CENTER, JPanel1);
		//add(BorderLayout.CENTER, JPanel1);
		JPanel2.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel1.add(BorderLayout.NORTH,JPanel2);
		ParseButton.setText("Parse DTD");
		ParseButton.setActionCommand("Parse DTD");
		JPanel2.add(ParseButton);
		DTDFileName.setColumns(30);
		DTDFileName.setText("catalog/eml-dataset.dtd");
		JPanel2.add(DTDFileName);
		JButton1.setText("...");
		JButton1.setActionCommand("...");
		JPanel2.add(JButton1);
		JPanel3.setLayout(new BorderLayout(0,0));
		JPanel1.add(BorderLayout.CENTER,JPanel3);
		JPanel4.setLayout(new BorderLayout(0,0));
		JPanel3.add(BorderLayout.CENTER,JPanel4);
		JPanel5.setLayout(new BorderLayout(0,0));
		JPanel4.add(BorderLayout.WEST, JPanel5);
		JPanel6.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel5.add(BorderLayout.SOUTH, JPanel6);
		SaveButton.setText("Save");
		SaveButton.setActionCommand("Save");
		JPanel6.add(SaveButton);
		SaveButton.setBounds(0,0,35,40);
		SaveButton.setEnabled(false);
		OpenButton.setText("Open");
		OpenButton.setActionCommand("Open");
		JPanel6.add(OpenButton);
		OpenButton.setBounds(0,0,35,40);
		OpenButton.setEnabled(false);
		JPanel5.add(BorderLayout.CENTER, JScrollPane4);
		JPanel4.add(BorderLayout.CENTER, OutputScrollPanel);
		//OpenFileDialog.setMode(FileDialog.LOAD);
		//OpenFileDialog.setTitle("Open");
		//$$ OpenFileDialog.move(0,371);
		//SaveFileDialog.setMode(FileDialog.SAVE);
		//SaveFileDialog.setTitle("Save");
		//$$ SaveFileDialog.move(24,371);
		//}}
              CMmenuItem = new JMenuItem("Content Model = ");
             // menuItem.addActionListener(this);
              popup.add(CMmenuItem);
              CardmenuItem = new JMenuItem("One Element Allowed");
            //  menuItem.addActionListener(this);
              popup.add(CardmenuItem);
              popup.add(new JSeparator());
              DupmenuItem = new JMenuItem("Duplicate");
              popup.add(DupmenuItem);
              DeletemenuItem = new JMenuItem("Delete");
              popup.add(DeletemenuItem);

		//{{INIT_MENUS
		//}}

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		//MBJBEAN//this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		ParseButton.addActionListener(lSymAction);
		JButton1.addActionListener(lSymAction);
		SaveButton.addActionListener(lSymAction);
		OpenButton.addActionListener(lSymAction);
		//}}
		DeletemenuItem.addActionListener(lSymAction);
		DupmenuItem.addActionListener(lSymAction);
		
		
		rootNode = newNode("root");
		treeModel = new MyDefaultTreeModel(rootNode);
//        treeModel.addTreeModelListener(new MyTreeModelListener());

        tree = new JTree(treeModel);
		JScrollPane4.getViewport().add(tree);
   //     tree.addMouseMotionListener(mml);
   //     tree.addMouseListener(ml);
    	tree.setCellRenderer(new MyRenderer());
		
//		SymTreeSelection lSymTreeSelection = new SymTreeSelection();
//		tree.addTreeSelectionListener(lSymTreeSelection);
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
     * Creates a new instance of Editor with the given title.
     * @param sTitle the title for the new frame.
     * @see #Editor()
     */
	public Editor(String sTitle)
	{
		this();
	}
	
	/**
	 * The entry point for this application.
	 * Sets the Look and Feel to the System Look and Feel.
	 * Creates a new Editor and makes it visible.
	 */
	static public void main(String args[])
	{
		try {
		    // Add the following code if you want the Look and Feel
		    // to be set to the Look and Feel of the native system.
		    /*
		    try {
		        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		    } 
		    catch (Exception e) { 
		    }
		    */

			//Create a new instance of our application's frame, and make it visible.
			(new Editor()).setVisible(true);
		} 
		catch (Throwable t) {
			t.printStackTrace();
			//Ensure the application exits with an error condition.
			System.exit(1);
		}
	}

    /**
     * Notifies this component that it has been added to a container
     * This method should be called by <code>Container.add</code>, and 
     * not by user code directly.
     * Overridden here to adjust the size of the frame if needed.
     * @see java.awt.Container#removeNotify
     */
	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();
		
		super.addNotify();
		
		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;
		
		// Adjust size of frame according to the insets and menu bar
		javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
		int menuBarHeight = 0;
		if (menuBar != null)
		    menuBarHeight = menuBar.getPreferredSize().height;
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JPanel SummaryPanel = new javax.swing.JPanel();
	javax.swing.JLabel SummaryLabel = new javax.swing.JLabel(
           "For demonstration only: this editor will be removed.");
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JButton ParseButton = new javax.swing.JButton();
	javax.swing.JTextField DTDFileName = new javax.swing.JTextField();
	javax.swing.JButton JButton1 = new javax.swing.JButton();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel4 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel5 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel6 = new javax.swing.JPanel();
	javax.swing.JButton SaveButton = new javax.swing.JButton();
	javax.swing.JButton OpenButton = new javax.swing.JButton();
	javax.swing.JScrollPane JScrollPane4 = new javax.swing.JScrollPane();
	javax.swing.JScrollPane OutputScrollPanel = new javax.swing.JScrollPane();
	//MBJBEAN//java.awt.FileDialog OpenFileDialog = new java.awt.FileDialog(this);
	//MBJBEAN//java.awt.FileDialog SaveFileDialog = new java.awt.FileDialog(this);
	//}}

	//{{DECLARE_MENUS
	//}}
    //Create the popup menu.
    javax.swing.JPopupMenu popup = new JPopupMenu();
    
              
	void exitApplication()
	{
		try {
	    	// Beep
	    	Toolkit.getDefaultToolkit().beep();
	    	// Show a confirmation dialog
	    	int reply = JOptionPane.showConfirmDialog(this, 
	    	                                          "Do you really want to exit?", 
	    	                                          "JFC Application - Exit" , 
	    	                                          JOptionPane.YES_NO_OPTION, 
	    	                                          JOptionPane.QUESTION_MESSAGE);
			// If the confirmation was affirmative, handle exiting.
			if (reply == JOptionPane.YES_OPTION)
			{
		    	this.setVisible(false);    // hide the Frame
		    	System.exit(0);            // close the application
			}
		} catch (Exception e) {
		}
	}

	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == Editor.this)
				Editor_windowClosing(event);
		}
	}

	void Editor_windowClosing(java.awt.event.WindowEvent event)
	{
		// to do: code goes here.
			 
		Editor_windowClosing_Interaction1(event);
	}

	void Editor_windowClosing_Interaction1(java.awt.event.WindowEvent event) {
		try {
			this.exitApplication();
		} catch (Exception e) {
		}
	}

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == ParseButton)
				ParseButton_actionPerformed(event);
			else if (object == JButton1)
				JButton1_actionPerformed(event);
			else if (object == SaveButton)
				SaveButton_actionPerformed(event);
			else if (object == OpenButton)
				OpenButton_actionPerformed(event);
			else if (object == DeletemenuItem)
				Del_actionPerformed(event);
			else if (object == DupmenuItem)
				Dup_actionPerformed(event);
			
		}
	}

	void ParseButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		try
		{
			FileReader reader = new FileReader(DTDFileName.getText());
            DTDParser parser = new DTDParser(new BufferedReader(reader));
            dtd = parser.parse(true);
            elementnames = new Vector();
            Enumeration e = dtd.elements.elements();
            while (e.hasMoreElements())
            {
                DTDElement elem = (DTDElement) e.nextElement();
                elementnames.addElement(elem.name);
			}
			String root = (dtd.rootElement).name;
		    DTDElement elem = dtd.rootElement;
        NodeInfo rootNodeInfo = new NodeInfo(elem.name);
        DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(rootNodeInfo);

	    buildTree(rootTreeNode);
		treeModel.setRoot(rootTreeNode);
		treeModel.reload();
	    }
	    catch (Exception e) {}
	}


    public String getCM(String elemName) {
        String str = null;
		DTDElement elem = (DTDElement)dtd.elements.get(elemName);
		if (elem!=null) {
		    sb = new StringBuffer();
		    str = dumpDTDItem(elem.content);
		}
		return str;
    }

	
    public String dumpDTDItem(DTDItem item)
    {
        if (item == null) return "";

        if (item instanceof DTDAny)
        {
            sb.append("Any");
        }
        else if (item instanceof DTDEmpty)
        {
            sb.append("Empty");
        }
        else if (item instanceof DTDName)
        {
            sb.append(((DTDName) item).value);
        }
        else if (item instanceof DTDChoice)
        {
            sb.append("(");
            DTDItem[] items = ((DTDChoice) item).getItems();

            for (int i=0; i < items.length; i++)
            {
                if (i > 0) sb.append("|");
                dumpDTDItem(items[i]);
            }
            sb.append(")");
        }
        else if (item instanceof DTDSequence)
        {
            sb.append("(");
            DTDItem[] items = ((DTDSequence) item).getItems();

            for (int i=0; i < items.length; i++)
            {
                if (i > 0) sb.append(",");
                dumpDTDItem(items[i]);
            }
            sb.append(")");
        }
        else if (item instanceof DTDMixed)
        {
            sb.append("(");
            DTDItem[] items = ((DTDMixed) item).getItems();

            for (int i=0; i < items.length; i++)
            {
                if (i > 0) sb.append(",");
                dumpDTDItem(items[i]);
            }
            sb.append(")");
        }
        else if (item instanceof DTDPCData)
        {
            sb.append("#PCDATA");
        }

        if (item.cardinal == DTDCardinal.OPTIONAL)
        {
            sb.append("?");
        }
        else if (item.cardinal == DTDCardinal.ZEROMANY)
        {
            sb.append("*");
        }
        else if (item.cardinal == DTDCardinal.ONEMANY)
        {
            sb.append("+");
        }
        return sb.toString();
        
    }	

	void writeXML (DefaultMutableTreeNode node, String fn) {
	    File outputFile = new File(fn);
	    try {
	    FileWriter out = new FileWriter(outputFile);
        tempStack = new Stack();
	    start = new StringBuffer();
	    write_loop(node);
	    String str1 = start.toString();
	    str1 = "<?xml version=\"1.0\"?>\n"+str1;
	    
	    out.write(str1);
	    out.close();
	    }
	    catch (Exception e) {}
	}
	
	void write_loop (DefaultMutableTreeNode node) {
	    String name;
	    String end;
	    NodeInfo ni = (NodeInfo)node.getUserObject();
	    name = ni.name;
	   if (!((ni.getCardinality()).equals("NOT SELECTED"))) {
	   if (!name.equals("(CHOICE)")) {
	    start.append("<"+name+" ");  
	    
	    Enumeration keys = ni.attr.keys();
	    while (keys.hasMoreElements()) {
	        String str = (String)(keys.nextElement());
	        String val = (String)ni.attr.get(str);
	        if (!str.equals("Value")) {
	            start.append(str+"=\""+val+" ");
	        }
	    }
	    start.append(">\n");
	    if (ni.attr.get("Value")!=null) {   // text node info
	        start.append(ni.attr.get("Value"));
	    }
	    end = "</"+name+">\n";
	    tempStack.push(end);
	   }
	   }
	    Enumeration enum = node.children();
	    while (enum.hasMoreElements()) {  // process child nodes 
	        DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(enum.nextElement());
	        NodeInfo ni1 = (NodeInfo)nd.getUserObject();
	        if (ni1.name.equals("#PCDATA")) {
	            start.append((String)(ni1.attr.get("Value")));
	        }
	        else {
	            write_loop(nd);
	        }
	    }
	   if (!((ni.getCardinality()).equals("NOT SELECTED"))) {
	   if (!name.equals("(CHOICE)")) {
	    start.append((String)(tempStack.pop()));
	   }
	   }
	    
	}
        	
public DefaultMutableTreeNode buildTree(DefaultMutableTreeNode root) {
    Vector vect = new Vector();
    Vector vvvv = new Vector();
    Vector zzzz = new Vector();
    vect.addElement(root);
    for(int i=0;i<levels;i++) {
        for (Enumeration e = vect.elements() ; e.hasMoreElements() ;) { 
            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)e.nextElement();
            vvvv = getChildren((NodeInfo)dmtn.getUserObject(),dmtn);
            for (Enumeration ee = vvvv.elements() ; ee.hasMoreElements() ;) { 
                zzzz.addElement(ee.nextElement());
            }
        }
        vect = zzzz;
        zzzz = new Vector();
    }
    return root;
}

// given an element, get a list of names of possible children

public Vector getChildren(NodeInfo ni, DefaultMutableTreeNode parentNode) {
    Vector vec = new Vector();
    Vector vec1 = new Vector();
    DTDElement elem = null;
    String name = ni.getName();
    if (name.equalsIgnoreCase("Any")) {
            }
    else if(name.equalsIgnoreCase("None")) {
    }
    else if(name.equalsIgnoreCase("#PCDATA")) {
    }
    else if(name.equalsIgnoreCase("(CHOICE)")) {
           Vector vec2 = new Vector();
           DTDChoice item = null;
           if (ni.getItem()) {
                item = (DTDChoice)oldItem;
           }
            DTDItem[] items = ((DTDChoice) item).getItems();
            for (int i=0; i < items.length; i++)
            {
                DTDItems(items[i],vec2);
            }
        boolean first = true;
        for (Enumeration e = vec2.elements() ; e.hasMoreElements() ;) {
            NodeInfo node = (NodeInfo)(e.nextElement());
            if (first) {
                node.setCardinality("SELECTED");
            }
            else {node.setCardinality("NOT SELECTED");}
            first = false;
		    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (node);
            parentNode.add(newNode);
            vec1.addElement(newNode);
            }
    }
      else {
        elem = (DTDElement)dtd.elements.get(name);
    }
    if (elem!=null) {
        getAttributes(ni, elem);
        DTDItems(elem.content, vec);
        for (Enumeration e = vec.elements() ; e.hasMoreElements() ;) {
          //  DTDElement el = (DTDElement)dtd.elements.get(((NodeInfo)(e.nextElement())).name);
          //  NodeInfo node = new NodeInfo(((NodeInfo)(e.nextElement())).name);
            NodeInfo node = (NodeInfo)(e.nextElement());
		    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (node);
            parentNode.add(newNode);
            vec1.addElement(newNode);
        }
    }
    return vec1;
}

// returns a vector of NodeInfo objects of allowed child element 
public void DTDItems(DTDItem item, Vector vec) {
        if (item == null) return;

        if (item instanceof DTDAny)
        {
            NodeInfo ni = new NodeInfo("Any");
            ni.setCardinality(getCardinality(item));
            vec.addElement(ni);
        }
        else if (item instanceof DTDEmpty)
        {
            NodeInfo ni = new NodeInfo("Empty");
            ni.setCardinality(getCardinality(item));
            vec.addElement(ni);
        }
        else if (item instanceof DTDName)
        {
            NodeInfo ni = new NodeInfo(((DTDName) item).value);
            ni.setCardinality(getCardinality(item));
            vec.addElement(ni);
        }
        else if (item instanceof DTDChoice)
        {
            DTDItem[] items = ((DTDChoice) item).getItems();
            if (items.length>1) {
                oldItem = item;
                NodeInfo ni = new NodeInfo("(CHOICE)");
                ni.setCardinality(getCardinality(item));
                ni.setItem(true);
                vec.addElement(ni);
            }
            else {

//            DTDItem[] items = ((DTDChoice) item).getItems();
            for (int i=0; i < items.length; i++)
                {
                    DTDItems(items[i],vec);
                }
            }
        }
        else if (item instanceof DTDSequence)
        {
 //           NodeInfo ni = new NodeInfo("(Sequence)");
 //           ni.setCardinality("NONE");
 //           vec.addElement(ni);
            DTDItem[] items = ((DTDSequence) item).getItems();
            for (int i=0; i < items.length; i++)
            {
                DTDItems(items[i],vec);
            }
        }
        else if (item instanceof DTDMixed)
        {
            DTDItem[] items = ((DTDMixed) item).getItems();

            for (int i=0; i < items.length; i++)
            {
               DTDItems(items[i],vec);
            }
        }
        else if (item instanceof DTDPCData)
        {
            NodeInfo ni = new NodeInfo("#PCDATA");
            ni.attr.put("Value", "text");
            ni.setCardinality(getCardinality(item));
            vec.addElement(ni);
        }

}

private void getAttributes(NodeInfo ni, DTDElement el) {
        Enumeration attrs = el.attributes.elements();
        while (attrs.hasMoreElements()) {
            DTDAttribute attr = (DTDAttribute) attrs.nextElement();
            getAttribute(ni, attr);
        }
}     
private void getAttribute(NodeInfo ni, DTDAttribute attr) {
        sb = new StringBuffer();
        
        if (attr.type instanceof String)
        {
       //       sb.append(attr.defaultValue);
        }
        else if (attr.type instanceof DTDEnumeration)
        {
            sb.append("(");
            String[] items = ((DTDEnumeration) attr.type).getItems();

            for (int i=0; i < items.length; i++)
            {
                if (i > 0) sb.append(",");
                sb.append(items[i]);
            }
            sb.append(")");
        }
        else if (attr.type instanceof DTDNotationList)
        {
            sb.append("Notation (");
            String[] items = ((DTDNotationList) attr.type).getItems();

            for (int i=0; i < items.length; i++)
            {
                if (i > 0) sb.append(",");
                sb.append(items[i]);
            }
            sb.append(")");
        }

        if (attr.decl != null)
        {
      //      sb.append(" "+attr.decl.name);
        }

        if (attr.defaultValue != null)
        {
            sb.append(attr.defaultValue);
        }
        sb.append("\" ");
        ni.attr.put(attr.name,sb.toString());  
}

private String getCardinality(DTDItem item) {
if (item.cardinal==DTDCardinal.NONE) return "ONE";
if (item.cardinal==DTDCardinal.OPTIONAL) return "OPTIONAL";
if (item.cardinal==DTDCardinal.ZEROMANY) return "ZERO to MANY";
if (item.cardinal==DTDCardinal.ONEMANY) return "ONE to MANY";
return "ONE";
}

/* *************************************************************** */	
	void JButton1_actionPerformed(java.awt.event.ActionEvent event)
	{
/*
		OpenFileDialog.setVisible(true);
		String filename = OpenFileDialog.getFile();
		if (filename!=null) {
		    DTDFileName.setText(OpenFileDialog.getDirectory()+filename);
		}
*/
	}
	
	public DefaultMutableTreeNode newNode (Object name) {
	    NodeInfo ni = new NodeInfo(name.toString());
	    DefaultMutableTreeNode node = new DefaultMutableTreeNode(ni);
	    return node;
	}
	
    class PopupListener extends MouseAdapter {
              public void mousePressed(MouseEvent e) {
                  maybeShowPopup(e);
              }

              public void mouseReleased(MouseEvent e) {
                  maybeShowPopup(e);
              }

              private void maybeShowPopup(MouseEvent e) {
                  if (e.isPopupTrigger()) {
                       TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                       tree.setSelectionPath(selPath);
                       if (selectedNode!=null) {
                        NodeInfo ni = (NodeInfo)selectedNode.getUserObject();
                        CMmenuItem.setText("Content: "+getCM(ni.getName()));
                        CardmenuItem.setText("Number: "+ni.getCardinality());
                      }
                     popup.show(e.getComponent(), e.getX(), e.getY());
                      
                  }
              }
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
	    TreePath tp = event.getNewLeadSelectionPath();
	    if (tp!=null) {
	    Object ob = tp.getLastPathComponent();
	    DefaultMutableTreeNode node = null;
	    if (ob!=null) {node =(DefaultMutableTreeNode)ob;}
		if (node!=null) {
		    selectedNode = node;
/*		NodeInfo info = (NodeInfo)(node.getUserObject());
		StringBuffer sb = new StringBuffer();
		sb.append(info.getName()+"\n");
		sb.append("Cardinality = "+info.getCardinality()+"\n");
		for (Enumeration e = info.attr.keys() ; e.hasMoreElements() ;) {
		    String atr = (String)e.nextElement();
            sb.append((atr + " = " + (String)info.attr.get(atr)+"\n"));
     }
            

		
		NodeInfoTextArea.setText(sb.toString());
 */
         
         NodeInfo ni = (NodeInfo)node.getUserObject();
         
         if (ni.getCardinality().equals("NOT SELECTED")) {
            for (Enumeration eee = (node.getParent()).children();eee.hasMoreElements();) {
                DefaultMutableTreeNode nnn = (DefaultMutableTreeNode)eee.nextElement();
                NodeInfo ni1 = (NodeInfo)nnn.getUserObject();
                ni1.setCardinality("NOT SELECTED");
            }
            ni.setCardinality("SELECTED");
         }
         treeModel.reload(node);
         XMLPanels xp = new XMLPanels(node);
         xp.setTreeModel(treeModel);
         OutputScrollPanel.getViewport().add(xp.topPanel);
 
		}
		}
		
	}  
    
	void SaveButton_actionPerformed(java.awt.event.ActionEvent event)
	{
/*
	    SaveFileDialog.setVisible(true);
	    String filename = SaveFileDialog.getFile();
	    if (filename!=null) {
	        writeXML((DefaultMutableTreeNode)(treeModel.getRoot()),filename);
	//	    String str1 = start.toString();
	//	    xmlstring = "<?xml version=\"1.0\"?>\n"+str1;
	//	    JTextArea1.setText(xmlstring);
		}	 
*/
	}

	void OpenButton_actionPerformed(java.awt.event.ActionEvent event)
	{
/*
	OpenFileDialog.setVisible(true);
	String filename = OpenFileDialog.getFile();
	if (filename!=null) {
	   
        try {
          String parserName = "org.apache.xerces.parsers.SAXParser";
          XMLReader parser = null;
          FileReader fr = new FileReader(filename);
          // Get an instance of the parser
          parser = XMLReaderFactory.createXMLReader(parserName);
          myHandler mh = new myHandler(treeModel);
          parser.setContentHandler(mh);
          parser.parse(new InputSource(fr));
        } catch (Exception e) {
          System.err.println(e.toString());
        }
	}
*/
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
	
	

	void Dup_actionPerformed(java.awt.event.ActionEvent event)
	{
	    TreePath tp = tree.getSelectionPath();
	    if (tp!=null) {
	    Object ob = tp.getLastPathComponent();
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
	    DefaultMutableTreeNode par = (DefaultMutableTreeNode)node.getParent();
	    int iii = par.getIndex(node);
	    DefaultMutableTreeNode newnode = deepNodeCopy(node);
	    tree.expandPath(tp);
	    par.insert(newnode,iii);
	    treeModel.reload(par);
	    }
			 
	}

	void Del_actionPerformed(java.awt.event.ActionEvent event)
	{
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) {
                treeModel.removeNodeFromParent(currentNode);
                return;
            }
        } 

        // Either there was no selection, or the root was selected.
        Toolkit.getDefaultToolkit().beep();
		
			 
	}
	
	
}
