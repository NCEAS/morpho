/**
 *        Name: QueryConfigDialog.java
 *     Purpose: A Class for interactively changing Query Parameters
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: QueryConfigDialog.java,v 1.5 2001-04-26 00:10:06 jones Exp $'
 */
/*
		A basic implementation of the JDialog class.
*/

package edu.ucsb.nceas.querybean;

import java.awt.*;
import javax.swing.*;
import javax.swing.JTree;
import javax.swing.tree.*;
import javax.swing.event.*;
import com.symantec.itools.javax.swing.models.StringListModel;
import edu.ucsb.nceas.dtclient.*;
import java.util.*;
import java.io.*;
import java.net.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import com.wutka.dtd.*;

public class QueryConfigDialog extends javax.swing.JDialog
{
    ConfigXML config;
    int levels = 9;
    StringBuffer sb; 
    PathQueries pqs;
    TreePath tp;
    public JTree tree;
    String MetaCatServletURL;
    String local_dtd_directory;
    public DTD dtd = null;
    public DefaultMutableTreeNode rootNode;
    DefaultMutableTreeNode selectedNode;
    public MyDefaultTreeModel treeModel;
    String pathText;
    DefaultListModel ReturnFieldModel;
    
    
	public QueryConfigDialog(Frame parent)
	{
		super(parent);
		
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(673,433);
		setVisible(false);
		getContentPane().add(BorderLayout.CENTER,JTabbedPane1);
		JTabbedPane1.setBounds(0,0,673,398);
		DoctypePanel.setLayout(new BorderLayout(0,0));
		JTabbedPane1.add(DoctypePanel);
		DoctypePanel.setBounds(2,27,668,368);
		DoctypePanel.setVisible(false);
		JPanel1.setLayout(new BorderLayout(0,0));
		DoctypePanel.add(BorderLayout.WEST,JPanel1);
		JPanel1.setBounds(0,0,149,368);
		JLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel1.setText("Available Document Types");
		JPanel1.add(BorderLayout.NORTH,JLabel1);
		JLabel1.setBounds(0,0,149,15);
		JScrollPane1.setOpaque(true);
		JPanel1.add(BorderLayout.CENTER,JScrollPane1);
		JScrollPane1.setBounds(0,15,149,353);
		doctypeList1.setModel(stringListModel1);
		JScrollPane1.getViewport().add(doctypeList1);
		doctypeList1.setBounds(0,0,146,350);
		JPanel2.setLayout(new GridLayout(2,1,0,0));
		DoctypePanel.add(BorderLayout.CENTER,JPanel2);
		JPanel2.setBounds(149,0,519,368);
		JPanel3.setLayout(new BorderLayout(0,0));
		JPanel2.add(JPanel3);
		JPanel3.setBounds(0,0,519,184);
		JPanel5.setLayout(new GridBagLayout());
		JPanel3.add(BorderLayout.WEST,JPanel5);
		JPanel5.setBounds(0,0,49,184);
		ToSearchButton.setText("->");
		ToSearchButton.setActionCommand("To Search >>>");
		JPanel5.add(ToSearchButton,new com.symantec.itools.awt.GridBagConstraintsD(0,0,1,1,1.0,1.0,java.awt.GridBagConstraints.CENTER,java.awt.GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		ToSearchButton.setFont(new Font("MonoSpaced", Font.BOLD, 12));
		ToSearchButton.setBounds(0,78,49,27);
		JPanel6.setLayout(new BorderLayout(0,0));
		JPanel3.add(BorderLayout.CENTER,JPanel6);
		JPanel6.setBounds(49,0,421,184);
		JLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel2.setText("DocTypes to be Searched");
		JPanel6.add(BorderLayout.NORTH,JLabel2);
		JLabel2.setBounds(0,0,421,15);
		JScrollPane2.setOpaque(true);
		JPanel6.add(BorderLayout.CENTER,JScrollPane2);
		JScrollPane2.setBounds(0,15,421,169);
		JScrollPane2.getViewport().add(SearchList);
		SearchList.setBounds(0,0,418,166);
		JPanel10.setLayout(new GridBagLayout());
		JPanel3.add(BorderLayout.EAST,JPanel10);
		JPanel10.setBounds(470,0,49,184);
		AllButton1.setText("All");
		AllButton1.setActionCommand("All");
		JPanel10.add(AllButton1,new com.symantec.itools.awt.GridBagConstraintsD(0,0,1,1,1.0,0.0,java.awt.GridBagConstraints.CENTER,java.awt.GridBagConstraints.NONE,new Insets(20,0,0,0),0,0));
		AllButton1.setBounds(0,89,49,25);
		JPanel4.setLayout(new BorderLayout(0,0));
		JPanel2.add(JPanel4);
		JPanel4.setBounds(0,184,519,184);
		JPanel7.setLayout(new GridBagLayout());
		JPanel4.add(BorderLayout.WEST,JPanel7);
		JPanel7.setBounds(0,0,54,184);
		ToReturnButton.setText("->");
		ToReturnButton.setActionCommand("To Return >>>");
		JPanel7.add(ToReturnButton,new com.symantec.itools.awt.GridBagConstraintsD(0,0,1,1,1.0,1.0,java.awt.GridBagConstraints.CENTER,java.awt.GridBagConstraints.HORIZONTAL,new Insets(0,2,0,3),0,0));
		ToReturnButton.setFont(new Font("MonoSpaced", Font.BOLD, 12));
		ToReturnButton.setBounds(2,78,49,27);
		JPanel8.setLayout(new BorderLayout(0,0));
		JPanel4.add(BorderLayout.CENTER,JPanel8);
		JPanel8.setBounds(54,0,416,184);
		JLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel3.setText("DocTypes to be Returned");
		JPanel8.add(BorderLayout.NORTH,JLabel3);
		JLabel3.setBounds(0,0,416,15);
		JScrollPane3.setOpaque(true);
		JPanel8.add(BorderLayout.CENTER,JScrollPane3);
		JScrollPane3.setBounds(0,15,416,169);
		JScrollPane3.getViewport().add(ReturnList);
		ReturnList.setBounds(0,0,413,166);
		JPanel11.setLayout(new GridBagLayout());
		JPanel4.add(BorderLayout.EAST, JPanel11);
		JPanel11.setBounds(470,0,49,184);
		AllButton2.setText("All");
		AllButton2.setActionCommand("All");
		JPanel11.add(AllButton2, new com.symantec.itools.awt.GridBagConstraintsD(0,0,1,1,1.0,0.0,java.awt.GridBagConstraints.CENTER,java.awt.GridBagConstraints.NONE,new Insets(20,0,0,0),0,0));
		AllButton2.setBounds(0,89,49,25);
		ClearButton.setText("Clear");
		ClearButton.setActionCommand("Clear");
		JPanel20.add(ClearButton);
		ClearButton.setBounds(0,0,65,25);
		FieldtypePanel.setLayout(new BorderLayout(0,0));
		JTabbedPane1.add(FieldtypePanel);
		FieldtypePanel.setBounds(2,27,668,368);
		FieldtypePanel.setVisible(false);
		JPanel13.setLayout(new GridLayout(1,2,0,0));
		FieldtypePanel.add(BorderLayout.WEST,JPanel13);
		JPanel13.setBounds(0,0,298,368);
		JPanel12.setLayout(new BorderLayout(0,0));
		JPanel13.add(JPanel12);
		JPanel12.setBounds(0,0,149,368);
		JLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel4.setText("Available Document Types");
		JPanel12.add(BorderLayout.NORTH, JLabel4);
		JLabel4.setBounds(0,0,149,15);
		JScrollPane4.setOpaque(true);
		JPanel12.add(BorderLayout.CENTER, JScrollPane4);
		JScrollPane4.setBounds(0,15,149,353);
		doctypeList2.setModel(stringListModel1);
		JScrollPane4.getViewport().add(doctypeList2);
		doctypeList2.setBounds(0,0,146,350);
		JPanel14.setLayout(new BorderLayout(0,0));
		JPanel13.add(JPanel14);
		JPanel14.setBounds(149,0,149,368);
		JLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel5.setText("Available Fields");
		JPanel14.add(BorderLayout.NORTH, JLabel5);
		JLabel5.setBounds(0,0,149,15);
		JPanel14.add(BorderLayout.CENTER, TreeScrollPane);
		TreeScrollPane.setBounds(0,15,149,353);
		JPanel15.setLayout(new BorderLayout(0,0));
		FieldtypePanel.add(BorderLayout.CENTER, JPanel15);
		JPanel15.setBounds(298,0,370,368);
		JLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel6.setText("Query Result Fields");
		JPanel15.add(BorderLayout.NORTH, JLabel6);
		JLabel6.setBounds(0,0,370,15);
		JPanel16.setLayout(new BorderLayout(0,0));
		JPanel15.add(BorderLayout.CENTER,JPanel16);
		JPanel16.setBounds(0,15,370,353);
		JPanel17.setLayout(new GridBagLayout());
		JPanel16.add(BorderLayout.WEST,JPanel17);
		JPanel17.setBounds(0,0,59,353);
		JButton1.setText("->");
		JButton1.setActionCommand("->");
		JPanel17.add(JButton1,new com.symantec.itools.awt.GridBagConstraintsD(0,0,1,1,1.0,1.0,java.awt.GridBagConstraints.CENTER,java.awt.GridBagConstraints.HORIZONTAL,new Insets(0,5,0,5),0,0));
		JButton1.setFont(new Font("MonoSpaced", Font.BOLD, 12));
		JButton1.setBounds(5,163,49,27);
		JPanel18.setLayout(new BorderLayout(0,0));
		JPanel16.add(BorderLayout.CENTER,JPanel18);
		JPanel18.setBounds(59,0,311,353);
		JPanel19.setLayout(new BorderLayout(0,0));
		JPanel18.add(BorderLayout.CENTER,JPanel19);
		JPanel19.setBounds(0,0,246,353);
		JScrollPane6.setOpaque(true);
		JPanel19.add(BorderLayout.WEST,JScrollPane6);
		JScrollPane6.setBounds(0,0,259,353);
		JScrollPane6.getViewport().add(ReturnFieldList);
		ReturnFieldList.setBounds(0,0,256,350);
		JPanel20.setAlignmentX(0.0F);
		JPanel20.setLayout(new BoxLayout(JPanel20,BoxLayout.Y_AXIS));
		JPanel18.add(BorderLayout.EAST, JPanel20);
		JPanel20.setBounds(246,0,65,353);
		JTabbedPane1.setSelectedComponent(DoctypePanel);
		JTabbedPane1.setSelectedIndex(0);
		JTabbedPane1.setTitleAt(0,"Set Query DocTypes");
		JTabbedPane1.setTitleAt(1,"Set Query Fields");
		JPanel9.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
		getContentPane().add(BorderLayout.SOUTH,JPanel9);
		JPanel9.setBounds(0,398,673,35);
		OKButton.setText("OK");
		OKButton.setActionCommand("OK");
		JPanel9.add(OKButton);
		OKButton.setBounds(453,5,51,25);
		CancelButton.setText("Cancel");
		CancelButton.setActionCommand("Cancel");
		JPanel9.add(CancelButton);
		CancelButton.setBounds(509,5,73,25);
		DefaultButton.setText("Defaults");
		JPanel9.add(DefaultButton);
		DefaultButton.setBounds(587,5,81,25);
		{
			String[] tempString = new String[2];
			tempString[0] = "test";
			tempString[1] = "test1";
			stringListModel1.setItems(tempString);
		}
		//$$ stringListModel1.move(0,444);
		//}}
		config = new ConfigXML("lib/config.xml");
		Vector vvv = config.get("localdoctypename");
		Vector searchVec = config.get("doctype");
		Vector returnVec = config.get("returndoc");
		Vector returnFieldVec = config.get("returnfield");
		MetaCatServletURL = config.get("MetaCatServletURL", 0);
		local_dtd_directory = config.get("local_dtd_directory",0);
		
		
		rootNode = newNode("root");
		treeModel = new MyDefaultTreeModel(rootNode);

        tree = new JTree(treeModel);
		TreeScrollPane.getViewport().add(tree);
    	tree.setCellRenderer(new MyRenderer());
		
		tree.setShowsRootHandles(true);
        tree.setEditable(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.putClientProperty("JTree.lineStyle", "Angled");
		SymTreeSelection lSymTreeSelection = new SymTreeSelection();
		tree.addTreeSelectionListener(lSymTreeSelection);

        // fetching server doc types
        
	    try {
            System.err.println("Trying: " + MetaCatServletURL);
		    URL url = new URL(MetaCatServletURL);
		    HttpMessage msg = new HttpMessage(url);
		    Properties prop = new Properties();
		    prop.put("action","getdoctypes");
		    
		    
		    InputStream in = msg.sendPostMessage(prop);
		    
        XMLList xmll = new XMLList(in,"doctype");
        Vector vec1 = xmll.getListVector();
        for (Enumeration e = vec1.elements() ; e.hasMoreElements() ;) {
            vvv.addElement(e.nextElement());
        }
        
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(this,"Error getting doctypes from remote catalog system!");
		}
		
		
		doctypeList1.setListData(vvv);
		doctypeList2.setListData(vvv);
		SearchList.setListData(searchVec);
		ReturnFieldModel = new DefaultListModel();
		for (Enumeration e = returnFieldVec.elements() ; e.hasMoreElements() ;) {
            ReturnFieldModel.addElement(e.nextElement()); 
        }

		ReturnFieldList.setModel(ReturnFieldModel);
		ReturnList.setListData(returnVec);
//		ReturnFieldList.setListData(returnFieldVec);
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		ToSearchButton.addActionListener(lSymAction);
		ToReturnButton.addActionListener(lSymAction);
		AllButton1.addActionListener(lSymAction);
		AllButton2.addActionListener(lSymAction);
		ClearButton.addActionListener(lSymAction);
		SymListSelection lSymListSelection = new SymListSelection();
		doctypeList2.addListSelectionListener(lSymListSelection);
		JButton1.addActionListener(lSymAction);
		OKButton.addActionListener(lSymAction);
		DefaultButton.addActionListener(lSymAction);
		CancelButton.addActionListener(lSymAction);
		//}}
	}

	public QueryConfigDialog()
	{
		this((Frame)null);
	}

	public QueryConfigDialog(String sTitle)
	{
		this();
		setTitle(sTitle);
	}

	public void setVisible(boolean b)
	{
		if (b)
			setLocation(50, 50);
		super.setVisible(b);
	}

	static public void main(String args[])
	{
		(new QueryConfigDialog()).setVisible(true);
	}

	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();

		super.addNotify();

		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;

		// Adjust size of frame according to the insets
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	//{{DECLARE_CONTROLS
	javax.swing.JTabbedPane JTabbedPane1 = new javax.swing.JTabbedPane();
	javax.swing.JPanel DoctypePanel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JList doctypeList1 = new javax.swing.JList();
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel5 = new javax.swing.JPanel();
	javax.swing.JButton ToSearchButton = new javax.swing.JButton();
	javax.swing.JPanel JPanel6 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	javax.swing.JScrollPane JScrollPane2 = new javax.swing.JScrollPane();
	javax.swing.JList SearchList = new javax.swing.JList();
	javax.swing.JPanel JPanel10 = new javax.swing.JPanel();
	javax.swing.JButton AllButton1 = new javax.swing.JButton();
	javax.swing.JPanel JPanel4 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel7 = new javax.swing.JPanel();
	javax.swing.JButton ToReturnButton = new javax.swing.JButton();
	javax.swing.JPanel JPanel8 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	javax.swing.JScrollPane JScrollPane3 = new javax.swing.JScrollPane();
	javax.swing.JList ReturnList = new javax.swing.JList();
	javax.swing.JPanel JPanel11 = new javax.swing.JPanel();
	javax.swing.JButton AllButton2 = new javax.swing.JButton();
	javax.swing.JPanel FieldtypePanel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel13 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel12 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel4 = new javax.swing.JLabel();
	javax.swing.JScrollPane JScrollPane4 = new javax.swing.JScrollPane();
	javax.swing.JList doctypeList2 = new javax.swing.JList();
	javax.swing.JPanel JPanel14 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel5 = new javax.swing.JLabel();
	javax.swing.JScrollPane TreeScrollPane = new javax.swing.JScrollPane();
	javax.swing.JPanel JPanel15 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel6 = new javax.swing.JLabel();
	javax.swing.JPanel JPanel16 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel17 = new javax.swing.JPanel();
	javax.swing.JButton JButton1 = new javax.swing.JButton();
	javax.swing.JPanel JPanel18 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel19 = new javax.swing.JPanel();
	javax.swing.JScrollPane JScrollPane6 = new javax.swing.JScrollPane();
	javax.swing.JList ReturnFieldList = new javax.swing.JList();
	javax.swing.JPanel JPanel20 = new javax.swing.JPanel();
	javax.swing.JButton ClearButton = new javax.swing.JButton();
	javax.swing.JPanel JPanel9 = new javax.swing.JPanel();
	javax.swing.JButton OKButton = new javax.swing.JButton();
	javax.swing.JButton CancelButton = new javax.swing.JButton();
	javax.swing.JButton DefaultButton = new javax.swing.JButton();
	com.symantec.itools.javax.swing.models.StringListModel stringListModel1 = new com.symantec.itools.javax.swing.models.StringListModel();
	//}}


	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == ToSearchButton)
				ToSearchButton_actionPerformed(event);
			else if (object == ToReturnButton)
				ToReturnButton_actionPerformed(event);
			else if (object == AllButton1)
				AllButton1_actionPerformed(event);
			else if (object == AllButton2)
				AllButton2_actionPerformed(event);
			else if (object == ClearButton)
				ClearButton_actionPerformed(event);
			else if (object == JButton1)
				JButton1_actionPerformed(event);
			else if (object == OKButton)
				OKButton_actionPerformed(event);
			else if (object == DefaultButton)
				DefaultButton_actionPerformed(event);
			else if (object == CancelButton)
				CancelButton_actionPerformed(event);
		}
	}

	void ToSearchButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		SearchList.setListData(doctypeList1.getSelectedValues());
	}

	void ToReturnButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		ReturnList.setListData(doctypeList1.getSelectedValues());
	}

	void AllButton1_actionPerformed(java.awt.event.ActionEvent event)
	{
	    Vector allvector = new Vector();
	    allvector.addElement("any");
		SearchList.setListData(allvector);
	}

	void AllButton2_actionPerformed(java.awt.event.ActionEvent event)
	{
	    Vector allvector = new Vector();
	    allvector.addElement("any");
		ReturnList.setListData(allvector);
	}

	void ClearButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		ReturnFieldModel.removeAllElements();
	}

	class SymListSelection implements javax.swing.event.ListSelectionListener
	{
		public void valueChanged(javax.swing.event.ListSelectionEvent event)
		{
			Object object = event.getSource();
			if (object == doctypeList2)
				doctypeList2_valueChanged(event);
		}
	}

	void doctypeList2_valueChanged(javax.swing.event.ListSelectionEvent event)
	{
		try
		{
		    String selection = (String)doctypeList2.getSelectedValue();
		    String file = selection + ".dtd"; // this is a hack and should be correctd
		                                      // so that dtd name is found from config file
		    File dtdfile = new File(local_dtd_directory+System.getProperty("file.separator")+file);
		    if (dtdfile.exists()) {
			    FileReader reader = new FileReader(local_dtd_directory+System.getProperty("file.separator")+file);
                DTDParser parser = new DTDParser(new BufferedReader(reader));
                dtd = parser.parse(true);
			    String root = (dtd.rootElement).name;
       
                NodeInfoDG rootNodeInfo = new NodeInfoDG(root);
                DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(rootNodeInfo);

	            buildTree(rootTreeNode);
		        treeModel.setRoot(rootTreeNode);
		        treeModel.reload();
		    }
	       else {
	            String doctype = (String)(doctypeList2.getSelectedValue());
	            getDGfromServer(doctype);
	       }
		}
        catch (Exception e) {
            System.out.println("Error creating tree");
            e.printStackTrace();
            }
	   }


	void getDGfromServer(String doctype)
	{
	    try {
            System.err.println("Trying: " + MetaCatServletURL);
		    URL url = new URL(MetaCatServletURL);
		    HttpMessage msg = new HttpMessage(url);
		    Properties prop = new Properties();
		    prop.put("action","getdataguide");
		    prop.put("doctype",doctype);
		    
		    InputStream in = msg.sendPostMessage(prop);
		    
        try {
            String parserName = "org.apache.xerces.parsers.SAXParser";
            XMLReader parser = null;
          // Get an instance of the parser
          parser = XMLReaderFactory.createXMLReader(parserName);
          myHandler mh = new myHandler(treeModel);
          parser.setContentHandler(mh);
          parser.parse(new InputSource(in));
        } catch (Exception e) {
           System.err.println(e.toString());
        }
		}
		catch (Exception e) {
		    e.printStackTrace();
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
            vvvv = getChildren((NodeInfoDG)dmtn.getUserObject(),dmtn);
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

public Vector getChildren(NodeInfoDG ni, DefaultMutableTreeNode parentNode) {
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
    else {
        elem = (DTDElement)dtd.elements.get(name);
    }
    if (elem!=null) {
        getAttributes(ni, elem);
        DTDItems(elem.content, vec);
        for (Enumeration e = vec.elements() ; e.hasMoreElements() ;) {
          //  DTDElement el = (DTDElement)dtd.elements.get(((NodeInfoDG)(e.nextElement())).name);
          //  NodeInfoDG node = new NodeInfoDG(((NodeInfoDG)(e.nextElement())).name);
            NodeInfoDG node = (NodeInfoDG)(e.nextElement());
		    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (node);
            parentNode.add(newNode);
            // now check to see if this path has search information attached
            TreeNode[] tn = newNode.getPath();
            vec1.addElement(newNode);
        }
    }
    return vec1;
}

// returns a vector of NodeInfoDG objects of allowed child element 
public void DTDItems(DTDItem item, Vector vec) {
        if (item == null) return;

        if (item instanceof DTDAny)
        {
            NodeInfoDG ni = new NodeInfoDG("Any");
            ni.setCardinality(getCardinality(item));
            vec.addElement(ni);
        }
        else if (item instanceof DTDEmpty)
        {
            NodeInfoDG ni = new NodeInfoDG("Empty");
            ni.setCardinality(getCardinality(item));
            vec.addElement(ni);
        }
        else if (item instanceof DTDName)
        {
            NodeInfoDG ni = new NodeInfoDG(((DTDName) item).value);
            ni.setCardinality(getCardinality(item));
            vec.addElement(ni);
        }
        else if (item instanceof DTDChoice)
        {
            DTDItem[] items = ((DTDChoice) item).getItems();

            for (int i=0; i < items.length; i++)
            {
                DTDItems(items[i],vec);
            }
        }
        else if (item instanceof DTDSequence)
        {
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
 /*           NodeInfoDG ni = new NodeInfoDG("#PCDATA");
            ni.attr.put("Value", "text");
            ni.setCardinality(getCardinality(item));
            vec.addElement(ni);
 */
        }

}
	
	
private void getAttributes(NodeInfoDG ni, DTDElement el) {
        Enumeration attrs = el.attributes.elements();
        while (attrs.hasMoreElements()) {
            DTDAttribute attr = (DTDAttribute) attrs.nextElement();
            getAttribute(ni, attr);
        }
}     

	
private void getAttribute(NodeInfoDG ni, DTDAttribute attr) {
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
    if (item.cardinal==DTDCardinal.NONE) return "NONE";
    if (item.cardinal==DTDCardinal.OPTIONAL) return "OPTIONAL";
    if (item.cardinal==DTDCardinal.ZEROMANY) return "ZEROMANY";
    if (item.cardinal==DTDCardinal.ONEMANY) return "ONEMANY";
    return "NONE";
}

	public DefaultMutableTreeNode newNode (Object name) {
	    NodeInfoDG ni = new NodeInfoDG(name.toString());
	    DefaultMutableTreeNode node = new DefaultMutableTreeNode(ni);
	    return node;
	}
	class SymTreeSelection implements javax.swing.event.TreeSelectionListener
	{
		public  void valueChanged(javax.swing.event.TreeSelectionEvent event)
		{
			Object object = event.getSource();
			if (object == tree) {
				tree_valueChanged(event);
			}
		}
	}

	void tree_valueChanged(javax.swing.event.TreeSelectionEvent event)
	{
	    tp = event.getNewLeadSelectionPath();
	    if (tp!=null) {
	        Object ob1 = tp.getLastPathComponent();
	        if (ob1!=null) {selectedNode =(DefaultMutableTreeNode)ob1;}
            Object[] ob = tp.getPath();
            String path = "";
            for (int i=0;i<tp.getPathCount();i++) {
                NodeInfoDG ni = (NodeInfoDG)((DefaultMutableTreeNode)ob[i]).getUserObject();
                path = path + "/" + ni.getName();
            }
            pathText = null;
            if (selectedNode!=null) {
                if (treeModel.isLeaf(selectedNode)) {
                    pathText = path;
                }
            }
            
	    }  
	}


    void saveJListItems(JList jlist, String parentname, String elementname) {
        config.removeChildren(parentname, 0);
        ListModel lm = jlist.getModel();
        int num = lm.getSize();
        for (int i=0;i<num;i++) {
            String listelem = (String)lm.getElementAt(i);
            config.addChild(parentname, 0, elementname, listelem);
        }
    }
    
    void getDefaultListValues() {
		Vector searchVec = config.get("doctype_default");
		Vector returnVec = config.get("returndoc_default");
		Vector returnFieldVec = config.get("returnfield_default");
		SearchList.setListData(searchVec);
		ReturnFieldModel = new DefaultListModel();
		for (Enumeration e = returnFieldVec.elements() ; e.hasMoreElements() ;) {
            ReturnFieldModel.addElement(e.nextElement()); 
        }

		ReturnFieldList.setModel(ReturnFieldModel);
		ReturnList.setListData(returnVec);
        
    }

	void JButton1_actionPerformed(java.awt.event.ActionEvent event)
	{
	    ReturnFieldModel.addElement(pathText);			 
	}

	void OKButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		saveJListItems(SearchList,"doctypes_searched","doctype");
		saveJListItems(ReturnList,"doctypes_returned","returndoc");
		saveJListItems(ReturnFieldList,"fields_returned","returnfield");
		config.saveDOM(config.doc);
		this.dispose();	 
	}

	void DefaultButton_actionPerformed(java.awt.event.ActionEvent event)
	{
        getDefaultListValues();
		config.saveDOM(config.doc);
        
    }

	void CancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		this.dispose();
			 
	}
}
