/**
 *        Name: DataGuideBean.java
 *     Purpose: A Class for creating a DataGuide JavaBean for use Desktop Client
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: DataGuideBean.java,v 1.8 2000-09-29 22:52:57 higgins Exp $'
 */

package edu.ucsb.nceas.querybean;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JTree;
import com.symantec.itools.javax.swing.models.StringListModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.net.*;
import java.io.*;
import java.util.*;
import com.wutka.dtd.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import com.symantec.itools.javax.swing.models.StringComboBoxModel;
import javax.swing.JTextArea;
import javax.swing.*;
import javax.swing.tree.TreePath;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import edu.ucsb.nceas.querybean.*;
import edu.ucsb.nceas.dtclient.*;

public class DataGuideBean extends java.awt.Container
{   
    String 	local_dtd_directory = null;
    PropertyResourceBundle options;
    MouseListener popupListener;
    JMenuItem ShowmenuItem;
    JMenuItem SavemenuItem;
    JMenuItem EditmenuItem;
    ImageIcon BflyStill;
    ImageIcon BflyMove;
    
    PathQueries pqs;
    LocalQuery lq;
    int levels = 9;
    public DTD dtd = null;
    StringBuffer sb; 
    public DefaultMutableTreeNode rootNode;
    DefaultMutableTreeNode selectedNode;
    public MyDefaultTreeModel treeModel;
    public JTree tree;
    TreePath tp;
//  ClientFramework container = null;
    String userName = "public";
    String passWord = "none";
    Hashtable localDocTypes;
    JTable table;
    
    public QueryBean qb;
    
    boolean local = true;
    String MetaCatServletURL;
 //   String MetaCatServletURL = "http://dev.nceas.ucsb.edu/metadata/servlet/metacat";
    
	public DataGuideBean(QueryBean qb)
	{
	    this.qb=qb;
		//{{INIT_CONTROLS
		setLayout(new GridLayout(1,2,2,2));
		setBackground(java.awt.Color.lightGray);
		setSize(635,361);
		ListPanel.setLayout(new BorderLayout(5,4));
		add(ListPanel);
		ListPanel.setBounds(0,0,316,361);
		JPanel3.setLayout(new BorderLayout(0,0));
		ListPanel.add(BorderLayout.CENTER,JPanel3);
		JPanel3.setBounds(0,0,316,361);
		JPanel1.setLayout(new BorderLayout(0,0));
		JPanel3.add(BorderLayout.WEST,JPanel1);
		JPanel1.setBounds(0,0,95,326);
		JLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel1.setText("Document Types");
		JPanel1.add(BorderLayout.NORTH, JLabel1);
		JLabel1.setBounds(0,0,95,15);
		JScrollPane1.setOpaque(true);
		JPanel1.add(BorderLayout.CENTER, JScrollPane1);
		JScrollPane1.setBounds(0,15,95,311);
		DocTypeList.setModel(stringListModel1);
		JScrollPane1.getViewport().add(DocTypeList);
		DocTypeList.setBounds(0,0,92,308);
		JPanel2.setLayout(new BorderLayout(0,0));
		JPanel3.add(BorderLayout.CENTER,JPanel2);
		JPanel2.setBounds(95,0,221,326);
		JLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel2.setText("Selected Document Structure");
		JPanel2.add(BorderLayout.NORTH, JLabel2);
		JLabel2.setBounds(0,0,221,15);
		JPanel2.add(BorderLayout.CENTER, JScrollPane2);
		JScrollPane2.setBounds(0,15,221,311);
		JPanel4.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel3.add(BorderLayout.SOUTH,JPanel4);
		JPanel4.setBounds(0,326,316,35);
		JPanel4.setVisible(false);
		LocalButton.setText("Get Local Info");
		LocalButton.setActionCommand("Get Local Info");
		JPanel4.add(LocalButton);
		LocalButton.setBounds(49,5,111,25);
		RemoteButton.setText("Get Remote");
		RemoteButton.setActionCommand("Get Remote");
		JPanel4.add(RemoteButton);
		RemoteButton.setBounds(165,5,101,25);
		QueryPanel.setLayout(new BorderLayout(0,0));
		add(QueryPanel);
		QueryPanel.setBounds(318,0,316,361);
		JPanel5.setLayout(new GridBagLayout());
		QueryPanel.add(BorderLayout.NORTH,JPanel5);
		JPanel5.setBounds(0,0,316,121);
		JLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		JLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		JLabel4.setAlignmentX(0.5F);
		JLabel4.setText("Search Criteria for Selected Node");
		JPanel5.add(JLabel4,new com.symantec.itools.awt.GridBagConstraintsD(0,0,1,1,1.0,0.0,java.awt.GridBagConstraints.CENTER,java.awt.GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),126,0));
		JLabel4.setBounds(0,0,316,15);
		JPanel6.setLayout(new BorderLayout(4,4));
		JPanel5.add(JPanel6,new com.symantec.itools.awt.GridBagConstraintsD(0,1,1,1,1.0,0.0,java.awt.GridBagConstraints.CENTER,java.awt.GridBagConstraints.HORIZONTAL,new Insets(4,4,0,4),0,5));
		JPanel6.setBounds(4,19,308,35);
		JLabel3.setText("Path");
		JPanel6.add(BorderLayout.WEST,JLabel3);
		JLabel3.setBounds(0,0,26,35);
		PathTextArea.setEditable(false);
		PathTextArea.setRows(2);
		PathTextArea.setWrapStyleWord(true);
		PathTextArea.setLineWrap(true);
		JPanel6.add(BorderLayout.CENTER,PathTextArea);
		PathTextArea.setBounds(30,0,278,35);
		JPanel7.setLayout(new BorderLayout(4,4));
		JPanel5.add(JPanel7,new com.symantec.itools.awt.GridBagConstraintsD(0,2,1,1,1.0,0.0,java.awt.GridBagConstraints.CENTER,java.awt.GridBagConstraints.HORIZONTAL,new Insets(4,0,0,0),185,0));
		JPanel7.setBounds(0,58,316,24);
		MatchTypeComboBox.setModel(stringComboBoxModel1);
		JPanel7.add(BorderLayout.WEST,MatchTypeComboBox);
		MatchTypeComboBox.setBackground(java.awt.Color.white);
		MatchTypeComboBox.setFont(new Font("Dialog", Font.PLAIN, 12));
		MatchTypeComboBox.setBounds(0,0,123,24);
		JPanel7.add(BorderLayout.CENTER,searchTextField);
		searchTextField.setBounds(127,0,189,24);
		JPanel8.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		JPanel5.add(JPanel8,new com.symantec.itools.awt.GridBagConstraintsD(0,3,1,1,1.0,0.0,java.awt.GridBagConstraints.CENTER,java.awt.GridBagConstraints.HORIZONTAL,new Insets(4,0,0,0),87,0));
		JPanel8.setBounds(0,86,316,35);
		SetButton.setText("Set");
		SetButton.setActionCommand("Set");
		JPanel8.add(SetButton);
		SetButton.setBounds(48,5,53,25);
		ClearButton.setText("Clear");
		ClearButton.setActionCommand("Clear");
		JPanel8.add(ClearButton);
		ClearButton.setBounds(106,5,65,25);
		AndRadioButton.setText("And");
		AndRadioButton.setActionCommand("And");
		JPanel8.add(AndRadioButton);
		AndRadioButton.setBounds(176,6,47,23);
		OrRadioButton.setSelected(true);
		OrRadioButton.setText("Or");
		OrRadioButton.setActionCommand("Or");
		JPanel8.add(OrRadioButton);
		OrRadioButton.setBounds(228,6,39,23);
		JPanel9.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		QueryPanel.add(BorderLayout.SOUTH,JPanel9);
		JPanel9.setBounds(0,326,316,35);
		DGSearch.setText("Search");
		DGSearch.setActionCommand("Search");
		JPanel9.add(DGSearch);
		DGSearch.setBounds(118,5,75,25);
		DGBfly.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		DGBfly.setIconTextGap(0);
		JPanel9.add(DGBfly);
		DGBfly.setBounds(198,17,0,0);
		{
			String[] tempString = new String[2];
			tempString[0] = "First Item";
			tempString[1] = "Second Item";
			stringListModel1.setItems(tempString);
		}
		//$$ stringListModel1.move(0,360);
		{
			String[] tempString = new String[6];
			tempString[0] = "contains";
			tempString[1] = "does not contain";
			tempString[2] = "is";
			tempString[3] = "is not";
			tempString[4] = "starts with";
			tempString[5] = "ends with";
			stringComboBoxModel1.setItems(tempString);
		}
		//$$ stringComboBoxModel1.move(0,362);
		MatchTypeComboBox.setSelectedIndex(0);
		//}}
	
		//{{REGISTER_LISTENERS
		SymAction lSymAction = new SymAction();
		RemoteButton.addActionListener(lSymAction);
		LocalButton.addActionListener(lSymAction);
		SymListSelection lSymListSelection = new SymListSelection();
		DocTypeList.addListSelectionListener(lSymListSelection);
		SetButton.addActionListener(lSymAction);
		ClearButton.addActionListener(lSymAction);
		SymItem lSymItem = new SymItem();
		AndRadioButton.addItemListener(lSymItem);
		OrRadioButton.addItemListener(lSymItem);
		DGSearch.addActionListener(lSymAction);
		SymPropertyChange lSymPropertyChange = new SymPropertyChange();
		DGSearch.addPropertyChangeListener(lSymPropertyChange);
		//}}
		popupListener = new PopupListener();
		ShowmenuItem = new JMenuItem("Display Document");
		ShowmenuItem.addActionListener(lSymAction);
        popup.add(ShowmenuItem);
		EditmenuItem = new JMenuItem("Edit Document");
		EditmenuItem.addActionListener(lSymAction);
        popup.add(EditmenuItem);
		SavemenuItem = new JMenuItem("Save Document");
        popup.add(SavemenuItem);
		
		pqs = new PathQueries();
		
        try {
		    BflyStill = new ImageIcon(getClass().getResource("Btflyyel.gif"));
		    BflyMove = new ImageIcon(getClass().getResource("Btflyyel4.gif"));
		    DGBfly.setIcon(BflyStill);
		}
		catch (Exception w) {}
		rootNode = newNode("root");
		treeModel = new MyDefaultTreeModel(rootNode);

        tree = new JTree(treeModel);
		JScrollPane2.getViewport().add(tree);
    	tree.setCellRenderer(new MyRenderer());
		
		tree.setShowsRootHandles(true);
        tree.setEditable(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.putClientProperty("JTree.lineStyle", "Angled");
		SymTreeSelection lSymTreeSelection = new SymTreeSelection();
		tree.addTreeSelectionListener(lSymTreeSelection);
		
		
    try {
      options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");
      MetaCatServletURL = (String)options.handleGetObject("MetaCatServletURL");
      local_dtd_directory = (String)options.handleGetObject("local_dtd_directory");
    }
    catch (Exception e) {System.out.println("Could not locate properties file!");}
	getDocTypes();	
		
	}

	//{{DECLARE_CONTROLS
	javax.swing.JPanel ListPanel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
	javax.swing.JList DocTypeList = new javax.swing.JList();
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
	javax.swing.JScrollPane JScrollPane2 = new javax.swing.JScrollPane();
	javax.swing.JPanel JPanel4 = new javax.swing.JPanel();
	javax.swing.JButton LocalButton = new javax.swing.JButton();
	javax.swing.JButton RemoteButton = new javax.swing.JButton();
	javax.swing.JPanel QueryPanel = new javax.swing.JPanel();
	javax.swing.JPanel JPanel5 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel4 = new javax.swing.JLabel();
	javax.swing.JPanel JPanel6 = new javax.swing.JPanel();
	javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
	javax.swing.JTextArea PathTextArea = new javax.swing.JTextArea();
	javax.swing.JPanel JPanel7 = new javax.swing.JPanel();
	javax.swing.JComboBox MatchTypeComboBox = new javax.swing.JComboBox();
	javax.swing.JTextField searchTextField = new javax.swing.JTextField();
	javax.swing.JPanel JPanel8 = new javax.swing.JPanel();
	javax.swing.JButton SetButton = new javax.swing.JButton();
	javax.swing.JButton ClearButton = new javax.swing.JButton();
	javax.swing.JRadioButton AndRadioButton = new javax.swing.JRadioButton();
	javax.swing.JRadioButton OrRadioButton = new javax.swing.JRadioButton();
	javax.swing.JPanel JPanel9 = new javax.swing.JPanel();
	javax.swing.JButton DGSearch = new javax.swing.JButton();
	javax.swing.JLabel DGBfly = new javax.swing.JLabel();
	com.symantec.itools.javax.swing.models.StringListModel stringListModel1 = new com.symantec.itools.javax.swing.models.StringListModel();
	com.symantec.itools.javax.swing.models.StringComboBoxModel stringComboBoxModel1 = new com.symantec.itools.javax.swing.models.StringComboBoxModel();
	//}}
    //Create the popup menu.
        javax.swing.JPopupMenu popup = new JPopupMenu();

	public static void main(String argv[])
	{
		class DriverFrame extends javax.swing.JFrame
		{
			public DriverFrame()
			{
				addWindowListener(new java.awt.event.WindowAdapter()
				{
					public void windowClosing(java.awt.event.WindowEvent event)
					{
						dispose();	  // free the system resources
						System.exit(0); // close the application
					}
				});
				getContentPane().setLayout(new BorderLayout(0,0));
				setSize(700,500);
				getContentPane().add(new DataGuideBean(null));
			}
		}

		new DriverFrame().show();
	}

    void create_localDocTypes() {
        localDocTypes = new Hashtable();
        localDocTypes.put("resource","resource.dtd");
        localDocTypes.put("eml-dataset","eml-dataset.dtd");
        localDocTypes.put("eml-access","eml-access.dtd");
        localDocTypes.put("eml-context","eml-context.dtd");
        localDocTypes.put("eml-file","eml-file.dtd");
        localDocTypes.put("eml-status","eml-status.dtd");
        localDocTypes.put("eml-software","eml-software.dtd");
        localDocTypes.put("eml-supplement","eml-supplement.dtd");
        localDocTypes.put("eml-variable","eml-variable.dtd");
    }

    void getLocalDocTypes() {
        create_localDocTypes();
        Enumeration keys = localDocTypes.keys();
        Vector vec = new Vector();
        while (keys.hasMoreElements()) {
            vec.addElement((String)keys.nextElement());
        } 
        DocTypeList.setListData(vec);
    }



public void LogIn() {
      Properties prop = new Properties();
       prop.put("action","Login Client");

      // Now try to write the document to the database
      try {
  //      PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
 //       String MetaCatServletURL =(String)options.handleGetObject("MetaCatServletURL");     // DFH
        System.err.println("Trying: " + MetaCatServletURL);
        URL url = new URL(MetaCatServletURL);
        HttpMessage msg = new HttpMessage(url);
            prop.put("username", userName);
            prop.put("password",passWord);
        InputStream returnStream = msg.sendPostMessage(prop);
	    StringWriter sw = new StringWriter();
	    int c;
	    while ((c = returnStream.read()) != -1) {
           sw.write(c);
        }
        returnStream.close();
        String res = sw.toString();
        sw.close();
        System.out.println(res);
			 
      } catch (Exception e) {
        System.out.println("Error logging into system");
      }
}

public void LogOut() {
      Properties prop = new Properties();
       prop.put("action","Logout");

      // Now try to write the document to the database
      try {
 //       PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
 //       String MetaCatServletURL =(String)options.handleGetObject("MetaCatServletURL");     // DFH
        System.err.println("Trying: " + MetaCatServletURL);
        URL url = new URL(MetaCatServletURL);
        HttpMessage msg = new HttpMessage(url);
        InputStream returnStream = msg.sendPostMessage(prop);
	    StringWriter sw = new StringWriter();
	    int c;
	    while ((c = returnStream.read()) != -1) {
           sw.write(c);
        }
        returnStream.close();
        String res = sw.toString();
        sw.close();
 //       System.out.println(res);
			 
      } catch (Exception e) {
        System.out.println("Error logging out of system");
      }
}
	
	
	

	void getDocTypes()
	{
	// first get local doctypes
        create_localDocTypes();
        Enumeration keys = localDocTypes.keys();
        Vector vec = new Vector();
        while (keys.hasMoreElements()) {
            vec.addElement((String)keys.nextElement());
        } 
        DocTypeList.setListData(vec);  // put local doctypes in list while 
        // fetching server doc types
        
//		LogIn();
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
            vec.addElement(e.nextElement());
        }
        
        DocTypeList.setListData(vec);
//        LogOut();
		}
		catch (Exception e) {
		    JOptionPane.showMessageDialog(this,"Error getting doctypes from remote catalog system!");
//		    e.printStackTrace();
		}
			 
	}

	void getDGfromServer(String doctype)
	{
//		LogIn();
	    try {
            System.err.println("Trying: " + MetaCatServletURL);
		    URL url = new URL(MetaCatServletURL);
		    HttpMessage msg = new HttpMessage(url);
		    Properties prop = new Properties();
		    prop.put("action","getdataguide");
		    prop.put("doctype",doctype);
		    
		    InputStream in = msg.sendPostMessage(prop);
		    
/*		    StringBuffer txt = new StringBuffer();
		    int x;
		    try {
		    while((x=in.read())!=-1) {
		        txt.append((char)x);
		    }
		    }
		    catch (Exception e) {}
		    String txt1 = txt.toString();
		    System.out.println(txt1);
*/
//
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
//
//        LogOut();
		}
		catch (Exception e) {
		    e.printStackTrace();
		}
	}


	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == RemoteButton)
				RemoteButton_actionPerformed(event);
			else if (object == LocalButton)
				LocalButton_actionPerformed(event);
			else if (object == SetButton)
				SetButton_actionPerformed(event);
			else if (object == ClearButton)
				ClearButton_actionPerformed(event);
			else if (object == DGSearch)
				DGSearch_actionPerformed(event);
			else if (object == ShowmenuItem) 
				ShowMenuItem_actionPerformed(event);
			else if (object == EditmenuItem) 
				EditMenuItem_actionPerformed(event);
		}
	}

	void ShowMenuItem_actionPerformed(java.awt.event.ActionEvent event)
	{
	   int sel = table.getSelectedRow();
	   if (sel>-1) {
            String filename = (String)table.getModel().getValueAt(sel, 0);
            File file = new File(filename);
            DocFrame df = new DocFrame(file);
            df.setVisible(true);
            df.writeInfo();
	   }
	}

	void EditMenuItem_actionPerformed(java.awt.event.ActionEvent event)
	{
	   int selectedRow = table.getSelectedRow();
	    if (selectedRow>-1) {
            String filename = (String)table.getModel().getValueAt(selectedRow, 0);
            File temp = new File(filename);
	//	            if (mde!=null) {
	//	                mde.openDocument(temp);
	//	                tabbedPane.setSelectedIndex(0);
	//	            }
	//	            else {System.out.println("mde is null in RSFrame class");}
	    }
 	}



	void RemoteButton_actionPerformed(java.awt.event.ActionEvent event)
	{
	    local = false;
		getDocTypes();
		
			 
	}

	void LocalButton_actionPerformed(java.awt.event.ActionEvent event)
	{
	    local = true;
		getLocalDocTypes();
		
		
			 
	}

	class SymListSelection implements javax.swing.event.ListSelectionListener
	{
		public void valueChanged(javax.swing.event.ListSelectionEvent event)
		{
			Object object = event.getSource();
			if (object == DocTypeList)
				DocTypeList_valueChanged(event);
		}
	}

	void DocTypeList_valueChanged(javax.swing.event.ListSelectionEvent event)
	{
		try
		{
		    String file = (String)localDocTypes.get(DocTypeList.getSelectedValue());
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
	            String doctype = (String)(DocTypeList.getSelectedValue());
	            getDGfromServer(doctype);
	        
	       }
		    
		}
        catch (Exception e) {}
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
            String path = "";
            for (int i=0;i<tn.length;i++) {
                NodeInfoDG nni = (NodeInfoDG)((DefaultMutableTreeNode)tn[i]).getUserObject();
                path = path + "/" + nni.getName();
            }
            PathQuery pq = pqs.getPQforPath(path);
            if (pq!=null) {
		        String type = pq.type;
		        String mt = pq.matchText;
//		        String path = pq.path;
		        String doctype = pq.docType;
		        
		        NodeInfoDG xxx = (NodeInfoDG)newNode.getUserObject();
		        xxx.setIcon("redsq.gif");
		        xxx.type = type;
		        xxx.matchText = mt;
            }
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
//		    if (node!=null) {
//		    }
            Object[] ob = tp.getPath();
            String path = "";
            for (int i=0;i<tp.getPathCount();i++) {
                NodeInfoDG ni = (NodeInfoDG)((DefaultMutableTreeNode)ob[i]).getUserObject();
                path = path + "/" + ni.getName();
            }
            PathTextArea.setText(path);
	    }  
	}
	

	void SetButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		String type = (String)MatchTypeComboBox.getSelectedItem();
		String mt = searchTextField.getText();
		String path = PathTextArea.getText();
		String doctype = (String)DocTypeList.getSelectedValue();
		PathQuery old = pqs.getPQforPath(path);
		if (old!=null) {pqs.delete(old);}
	    PathQuery pq = new PathQuery(doctype, path, type, mt);
		pqs.insert(pq);
		System.out.println("Number of PathQuery objects = " + pqs.count());
		
		NodeInfoDG ni = (NodeInfoDG)selectedNode.getUserObject();
		ni.setIcon("redsq.gif");
//		String type = (String)MatchTypeComboBox.getSelectedItem();
		    ni.type = type;
		    ni.matchText = mt;
//		    ni.matchText = searchTextField.getText();
	    treeModel.nodeChanged(selectedNode);
		tree.expandPath(tp.getParentPath());
	}

	void ClearButton_actionPerformed(java.awt.event.ActionEvent event)
	{

		NodeInfoDG ni = (NodeInfoDG)selectedNode.getUserObject();
		ni.setIcon("");
        ni.type = null;
        ni.matchText = null;
        searchTextField.setText("");
	    treeModel.nodeChanged(selectedNode);
		tree.expandPath(tp.getParentPath());
		
            Object[] ob = tp.getPath();
            String path = "";
            for (int k=0;k<tp.getPathCount();k++) {
                NodeInfoDG nn = (NodeInfoDG)((DefaultMutableTreeNode)ob[k]).getUserObject();
                path = path + "/" + nn.getName();
            }
            PathTextArea.setText(path);
            
		String type = (String)MatchTypeComboBox.getSelectedItem();
		String mt = searchTextField.getText();
		String pth = PathTextArea.getText();
		String doctype = (String)DocTypeList.getSelectedValue();
//	    PathQuery pq = new PathQuery(doctype, pth, type, mt);
		pqs.delete(doctype,pth);
		System.out.println("Number of PathQuery objects = " + pqs.count());
            
	}

	class SymItem implements java.awt.event.ItemListener
	{
		public void itemStateChanged(java.awt.event.ItemEvent event)
		{
			Object object = event.getSource();
			if (object == AndRadioButton)
				AndRadioButton_itemStateChanged(event);
			else if (object == OrRadioButton)
				OrRadioButton_itemStateChanged(event);
		}
	}

	void AndRadioButton_itemStateChanged(java.awt.event.ItemEvent event)
	{
		if(AndRadioButton.isSelected()) {
		      OrRadioButton.setSelected(false); 
		}
			 
	}

	void OrRadioButton_itemStateChanged(java.awt.event.ItemEvent event)
	{
		if(OrRadioButton.isSelected()) {
		      AndRadioButton.setSelected(false); 
		}
	}

	void DGSearch_actionPerformed(java.awt.event.ActionEvent event)
	{  
	    if (DGSearch.getText().equalsIgnoreCase("Halt")) {
	        if (lq!=null) {
	            lq.setStopFlag();
	            lq = null;
	        }
	        DGSearch.setText("Search");
	    }
	    else {
	    
		String[] xpath = pqs.getXPathArray();
		String xpatharray = "";
		for (int k=0;k<xpath.length;k++) {
		    xpatharray = xpatharray+xpath[k]+"\n";
		}
        lq = new LocalQuery(xpath, AndRadioButton.isSelected(), DGSearch);
		     table = lq.getRSTable();
		     
             table.addMouseListener(popupListener);
		     
		     table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
/*            ListSelectionModel rowSM = table.getSelectionModel();
            rowSM.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    //Ignore extra messages.
                    if (e.getValueIsAdjusting()) return;
                    
                    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
                    if (lsm.isSelectionEmpty()) {
                        System.out.println("No rows are selected.");
                    } else {
                        int selectedRow = lsm.getMinSelectionIndex();
                        String filename = (String)table.getModel().getValueAt(selectedRow, 0);
                        File file = new File("xmlfiles/"+filename);
                        DocFrame df = new DocFrame(file);
                        df.setVisible(true);
                        df.writeInfo();
                        
                    }
                }
            });
 */       
		     
	//	     RSFrame rs = new RSFrame();
		     qb.RSScrollPane2.getViewport().add(table);
		     qb.QueryStringTextArea2.setText(xpatharray);
//		     rs.setVisible(true);
            lq.start();
	    }
		String xml = pqs.buildXMLQuery(AndRadioButton.isSelected());
//	    LogIn();
	    qb.squery_submitToDatabase(xml);
//	    LogOut();
	    
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
                     popup.show(e.getComponent(), e.getX(), e.getY());
                  }
                      
              }
    }
	
	

	class SymPropertyChange implements java.beans.PropertyChangeListener
	{
		public void propertyChange(java.beans.PropertyChangeEvent event)
		{
			Object object = event.getSource();
			if (object == DGSearch)
				DGSearch_propertyChange(event);
		}
	}

	void DGSearch_propertyChange(java.beans.PropertyChangeEvent event)
	{
		if(DGSearch.getText().equals("Search")) {
	     DGBfly.setIcon(BflyStill);
		}
		else {
	     DGBfly.setIcon(BflyMove);
		}	 
	}
}