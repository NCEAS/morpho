/**
 *        Name: ClientFramework.java
 *     Purpose: A Class that is the top frame for an XML_Query sample
 *		application (searchs local collection of XML files
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: ClientFramework.java,v 1.26 2000-10-13 17:35:54 higgins Exp $'
 */

package edu.ucsb.nceas.dtclient;
 
 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.symantec.itools.javax.swing.JToolBarSeparator;
import com.symantec.itools.javax.swing.JButtonGroupPanel;
import com.symantec.itools.javax.swing.models.StringListModel;
import com.symantec.itools.javax.swing.models.StringTreeModel;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.lang.reflect.*;
import java.lang.ClassCastException;
import com.symantec.itools.javax.swing.borders.LineBorder;

//import edu.ucsb.nceas.querybean.*;
//import edu.ucsb.nceas.metaedit.*;
/**
 * A basic JFC 1.1 based application.
 */
public class ClientFramework extends javax.swing.JFrame
{
  // following static block will reset the mdehome config variable to the
  // current working dir if it is currently set to 'settocwd'
  // to be used when first installed
    
    static{
	  try{
		Properties mdeprops = new Properties();
        FileInputStream in = new FileInputStream("mde.cfg");
        mdeprops.load(in);
        in.close();
        String mdehome = (String)mdeprops.get("mdehome");
	    if (mdehome.equals("settocwd")) {
	        String cwd = (String)System.getProperty("user.dir");
	        mdeprops.put("mdehome",cwd);
            FileOutputStream out = new FileOutputStream("mde.cfg");
            mdeprops.save(out, "--set mdehome=settocwd to set to current directory--");
            out.close();
        }
      }
      catch (Exception e){}
        
    }
    String userName = "public";
    String passWord = "none";
    static boolean log_file = false; // redirects standard out and err streams
    String xmlcatalogfile = null;
    String MetaCatServletURL = null;
    PropertyResourceBundle options = null;
    boolean connected = false;
    edu.ucsb.nceas.querybean.LocalQuery lq = null;
    Hashtable menuList = null;

    // String[] searchmode = {"contains","contains-not","is","is-not","starts-with","ends-with"};
    JTable table;


    public ClientFramework()
    {
      // Create the list of menus for use by the framework and plugins
      menuList = new Hashtable();
//		setmdehome();
        
	    try{
//      Example of loading icon as resource - DFH 
		ImageIcon xxx = new ImageIcon(getClass().getResource("new.gif"));
		newButton.setIcon(xxx);
		newItem.setIcon(xxx);
		xxx = new ImageIcon(getClass().getResource("open.gif"));
		openButton.setIcon(xxx);
		openItem.setIcon(xxx);
		xxx = new ImageIcon(getClass().getResource("save.gif"));
		saveButton.setIcon(xxx);
		saveItem.setIcon(xxx);
		xxx = new ImageIcon(getClass().getResource("cut.gif"));
		cutButton.setIcon(xxx);
		cutItem.setIcon(xxx);
		xxx = new ImageIcon(getClass().getResource("copy.gif"));
		copyButton.setIcon(xxx);
		copyItem.setIcon(xxx);
		xxx = new ImageIcon(getClass().getResource("paste.gif"));
		pasteButton.setIcon(xxx);
		pasteItem.setIcon(xxx);
		xxx = new ImageIcon(getClass().getResource("about.gif"));
		aboutButton.setIcon(xxx);
		aboutItem.setIcon(xxx);
		xxx = new ImageIcon(getClass().getResource("saveserver.gif"));
		saveserverButton.setIcon(xxx);
		xxx = new ImageIcon(getClass().getResource("datafiles.gif"));
		dataPict.setIcon(xxx);
	    }
	    catch (Exception e) {}
		// This code is automatically generated by Visual Cafe when you add
		// components to the visual environment. It instantiates and initializes
		// the components. To modify the code, only use code syntax that matches
		// what Visual Cafe can generate, or Visual Cafe may be unable to back
		// parse your Java file into its visual environment.
		//{{INIT_CONTROLS
		setJMenuBar(JMenuBar1);
		setTitle("MORPHO - Data Management for Ecologists");
		setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(775,550);
		setVisible(false);
		saveFileDialog.setMode(FileDialog.SAVE);
		saveFileDialog.setTitle("Save");
		//$$ saveFileDialog.move(24,336);
		openFileDialog.setMode(FileDialog.LOAD);
		openFileDialog.setTitle("Open");
		//$$ openFileDialog.move(0,336);
		JPanel2.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		getContentPane().add(BorderLayout.NORTH, JPanel2);
		JPanel2.setBounds(0,0,744,36);
		JToolBar1.setAlignmentY(0.222222F);
		JPanel2.add(JToolBar1);
		JToolBar1.setBounds(0,0,834,36);
		newButton.setDefaultCapable(false);
		newButton.setToolTipText("Create a new document");
		newButton.setMnemonic((int)'N');
		JToolBar1.add(newButton);
		newButton.setBounds(16,11,35,11);
		openButton.setDefaultCapable(false);
		openButton.setToolTipText("Open an existing document");
		openButton.setMnemonic((int)'O');
		JToolBar1.add(openButton);
		openButton.setBounds(51,11,35,11);
		saveButton.setDefaultCapable(false);
		saveButton.setToolTipText("Save the active document");
		saveButton.setMnemonic((int)'S');
		JToolBar1.add(saveButton);
		saveButton.setBounds(86,11,35,11);
		
		saveserverButton.setDefaultCapable(false);
		saveserverButton.setToolTipText("Load, Delete, or Update Information on Server");
		JToolBar1.add(saveserverButton);
		saveserverButton.setBounds(121,11,35,11);
		
		JToolBar1.add(JToolBarSeparator1);
		JToolBarSeparator1.setBounds(156,9,10,5);
		cutButton.setDefaultCapable(false);
		cutButton.setToolTipText("Cut the selection and put it on the Clipboard");
		cutButton.setMnemonic((int)'T');
		JToolBar1.add(cutButton);
		cutButton.setBounds(166,11,35,11);
		copyButton.setDefaultCapable(false);
		copyButton.setToolTipText("Copy the selection and put it on the Clipboard");
		copyButton.setMnemonic((int)'C');
		JToolBar1.add(copyButton);
		copyButton.setBounds(201,11,35,11);
		pasteButton.setDefaultCapable(false);
		pasteButton.setToolTipText("Insert Clipboard contents");
		pasteButton.setMnemonic((int)'P');
		JToolBar1.add(pasteButton);
		pasteButton.setBounds(236,11,35,11);
		JToolBar1.add(JToolBarSeparator2);
		JToolBarSeparator2.setBounds(271,9,10,5);
		aboutButton.setDefaultCapable(false);
		aboutButton.setToolTipText("Display program information, version number and copyright");
		aboutButton.setMnemonic((int)'A');
		JToolBar1.add(aboutButton);
		aboutButton.setBounds(281,11,35,11);
		JToolBar1.add(JToolBarSeparator3);
		JToolBarSeparator3.setBounds(316,9,10,5);
		ExpertCheckBox.setText("ExpertMode");
		ExpertCheckBox.setToolTipText("Shows additional \'Search\' functions when selected");
		JToolBar1.add(ExpertCheckBox);
		ExpertCheckBox.setBackground(new java.awt.Color(204,204,204));
		ExpertCheckBox.setFont(new Font("Dialog", Font.PLAIN, 10));
		ExpertCheckBox.setBounds(326,11,82,21);
		JToolBar1.add(JToolBarSeparator4);
		JToolBarSeparator4.setBounds(408,9,10,5);
		ToolBarSearchText.setColumns(12);
		ToolBarSearchText.setText("%");
		JToolBar1.add(ToolBarSearchText);
		ToolBarSearchText.setFont(new Font("Dialog", Font.PLAIN, 12));
		ToolBarSearchText.setBounds(418,2,132,32);
		queryButton.setDefaultCapable(false);
		queryButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		queryButton.setText("Quick Search");
		queryButton.setActionCommand("Find Text");
		JToolBar1.add(queryButton);
		queryButton.setFont(new Font("Dialog", Font.PLAIN, 10));
		queryButton.setBounds(550,11,97,23);
		LocalSearchCheckBox.setSelected(true);
		LocalSearchCheckBox.setToolTipText("When selected, locally stored documents are searched");
		LocalSearchCheckBox.setText("Local Search");
		LocalSearchCheckBox.setActionCommand("Local Search");
		JToolBar1.add(LocalSearchCheckBox);
		LocalSearchCheckBox.setFont(new Font("Dialog", Font.PLAIN, 10));
		LocalSearchCheckBox.setBounds(647,11,87,21);
		CatalogSearchCheckBox.setToolTipText("When selected, catalog on remote server is searched during queries");
		CatalogSearchCheckBox.setText("Catalog Search");
		CatalogSearchCheckBox.setActionCommand("Catalog Search");
		JToolBar1.add(CatalogSearchCheckBox);
		CatalogSearchCheckBox.setFont(new Font("Dialog", Font.PLAIN, 10));
		CatalogSearchCheckBox.setBounds(734,11,98,21);
		ExpertCheckBox.setActionCommand("ExpertMode");
		JPanel1.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER, JPanel1);
		JPanel1.setBounds(0,0,0,0);
		JTabbedPane1.setToolTipText("Select tab of interest");
		JPanel1.add(BorderLayout.CENTER, JTabbedPane1);
		JTabbedPane1.setBounds(0,0,0,0);
		EditorPanel.setLayout(new BorderLayout(0,0));
		JTabbedPane1.add(EditorPanel);
		EditorPanel.setBounds(0,0,0,0);
		EditorPanel.setVisible(false);
		EditorPanel.add(BorderLayout.CENTER,mdeBean1);
		QueryPanel.setLayout(new BorderLayout(0,0));
		JTabbedPane1.add(QueryPanel);
		QueryPanel.setBounds(0,0,0,0);
		QueryPanel.setVisible(false);
		queryBean1.setExpertMode(false);
		QueryPanel.add(BorderLayout.CENTER,queryBean1);
		DataViewerPanel.setLayout(new BorderLayout(0,0));
		JTabbedPane1.add(DataViewerPanel);
		DataViewerPanel.setBounds(0,0,0,0);
		DataViewerPanel.setVisible(false);
		UnderConstruction.setText("Under Construction!!! (NOT Functional)");
		UnderConstruction.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		DataViewerPanel.add(BorderLayout.NORTH,UnderConstruction);
		UnderConstruction.setForeground(java.awt.Color.red);
		UnderConstruction.setFont(new Font("Dialog", Font.BOLD, 20));
		UnderConstruction.setBounds(0,0,0,0);
		DataViewerPanel.add(BorderLayout.CENTER,dataPict);
		dataPict.setBounds(0,0,20,40);
		JLabel1.setText("Eventually this tab will display an assortment of tools for viewing and manipulating data collections (i.e. metadata and data)");
		DataViewerPanel.add(BorderLayout.SOUTH,JLabel1);
		JLabel1.setBounds(0,0,20,40);
		JTabbedPane1.setSelectedComponent(EditorPanel);
		JTabbedPane1.setSelectedIndex(0);
		JTabbedPane1.setTitleAt(0,"");
		JTabbedPane1.setTitleAt(1,"");
		JTabbedPane1.setTitleAt(2,"");
		//$$ lineBorder1.move(240,481);
		//$$ stringListModel1.move(72,406);
		//$$ stringComboBoxModel1.move(48,481);
		//$$ stringComboBoxModel2.move(72,481);
		//$$ JMenuBar1.move(168,312);
		fileMenu.setText("File");
		fileMenu.setActionCommand("File");
		fileMenu.setMnemonic((int)'F');
		JMenuBar1.add(fileMenu);
		newItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.Event.CTRL_MASK));
		newItem.setText("New");
		newItem.setActionCommand("New");
		newItem.setMnemonic((int)'N');
		fileMenu.add(newItem);
		openItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.Event.CTRL_MASK));
		openItem.setText("Open...");
		openItem.setActionCommand("Open...");
		openItem.setMnemonic((int)'O');
		fileMenu.add(openItem);
		saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.Event.CTRL_MASK));
		saveItem.setText("Save");
		saveItem.setActionCommand("Save");
		saveItem.setMnemonic((int)'S');
		fileMenu.add(saveItem);
		saveAsItem.setText("Save As...");
		saveAsItem.setActionCommand("Save As...");
		saveAsItem.setMnemonic((int)'A');
		fileMenu.add(saveAsItem);
		SaveToDatabase.setText("Save To Database...");
		SaveToDatabase.setActionCommand("Save To Database...");
		fileMenu.add(SaveToDatabase);
		PreviewXML.setText("Preview XML");
		PreviewXML.setActionCommand("Preview XML");
		fileMenu.add(PreviewXML);
		fileMenu.add(JSeparator1);
		ConnectMenuItem.setText("Connect...");
		ConnectMenuItem.setActionCommand("Connect...");
		fileMenu.add(ConnectMenuItem);
		fileMenu.add(JSeparator3);
		exitItem.setText("Exit");
		exitItem.setActionCommand("Exit");
		exitItem.setMnemonic((int)'X');
		fileMenu.add(exitItem);
		editMenu.setText("Edit");
		editMenu.setActionCommand("Edit");
		editMenu.setMnemonic((int)'E');
		JMenuBar1.add(editMenu);
		cutItem.setEnabled(false);
		cutItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.Event.CTRL_MASK));
		cutItem.setText("Cut");
		cutItem.setActionCommand("Cut");
		cutItem.setMnemonic((int)'T');
		editMenu.add(cutItem);
		copyItem.setEnabled(false);
		copyItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.Event.CTRL_MASK));
		copyItem.setText("Copy");
		copyItem.setActionCommand("Copy");
		copyItem.setMnemonic((int)'C');
		editMenu.add(copyItem);
		pasteItem.setEnabled(false);
		pasteItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.Event.CTRL_MASK));
		pasteItem.setText("Paste");
		pasteItem.setActionCommand("Paste");
		pasteItem.setMnemonic((int)'P');
		editMenu.add(pasteItem);
		editMenu.add(JSeparator2);
		OptionsMenuItem.setText("Options...");
		OptionsMenuItem.setActionCommand("Options...");
		editMenu.add(OptionsMenuItem);
		WindowsMenu.setText("Windows");
		WindowsMenu.setActionCommand("Windows");
		JMenuBar1.add(WindowsMenu);
		ElementChoiceMenuItem.setSelected(true);
		ElementChoiceMenuItem.setText("Element Choice");
		ElementChoiceMenuItem.setActionCommand("Element Choice");
		WindowsMenu.add(ElementChoiceMenuItem);
		ElementTextMenuItem.setSelected(true);
		ElementTextMenuItem.setText("Element Text");
		ElementTextMenuItem.setActionCommand("Element Text");
		WindowsMenu.add(ElementTextMenuItem);
		helpMenu.setText("Help");
		helpMenu.setActionCommand("Help");
		helpMenu.setMnemonic((int)'H');
		JMenuBar1.add(helpMenu);
		aboutItem.setText("About...");
		aboutItem.setActionCommand("About...");
		aboutItem.setMnemonic((int)'A');
		helpMenu.add(aboutItem);
		//}}

                // Update the menu list withthe statically created menus
                menuList.put("File", fileMenu);
                menuList.put("Edit", editMenu);
                menuList.put("Windows", WindowsMenu);
                menuList.put("Help", helpMenu);

                // Set the tab titles -- these should be dynamically set
                // from the client.properties rather than hardcoded
		JTabbedPane1.setSelectedComponent(QueryPanel);
		JTabbedPane1.setSelectedIndex(1);
		JTabbedPane1.setTitleAt(0,"Edit Document");
		JTabbedPane1.setTitleAt(1,"Search Document");
		JTabbedPane1.setTitleAt(2,"Browse Data");

                // Load all of the plugins, their menus, and toolbars
                loadPlugins();

		//{{INIT_MENUS
		//}}

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		openItem.addActionListener(lSymAction);
		saveItem.addActionListener(lSymAction);
		exitItem.addActionListener(lSymAction);
		aboutItem.addActionListener(lSymAction);
		openButton.addActionListener(lSymAction);
		saveButton.addActionListener(lSymAction);
		saveserverButton.addActionListener(lSymAction);
		aboutButton.addActionListener(lSymAction);
		SymItem lSymItem = new SymItem();
		queryButton.addActionListener(lSymAction);
		ToolBarSearchText.addActionListener(lSymAction);
		newItem.addActionListener(lSymAction);
		saveAsItem.addActionListener(lSymAction);
		SaveToDatabase.addActionListener(lSymAction);
		PreviewXML.addActionListener(lSymAction);
		OptionsMenuItem.addActionListener(lSymAction);
		SymChange lSymChange = new SymChange();
		JTabbedPane1.addChangeListener(lSymChange);
		LocalSearchCheckBox.addItemListener(lSymItem);
		CatalogSearchCheckBox.addItemListener(lSymItem);
		ExpertCheckBox.addItemListener(lSymItem);
		ConnectMenuItem.addActionListener(lSymAction);
		newButton.addActionListener(lSymAction);
		//}}
		// Get the configuration file information
    try {
      options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");
      String local_dtd_directory =(String)options.handleGetObject("local_dtd_directory");     // DFH
      xmlcatalogfile = local_dtd_directory+"/catalog"; 
      MetaCatServletURL = (String)options.handleGetObject("MetaCatServletURL");
    }
    catch (Exception e) {System.out.println("Could not locate properties file!");}
		JTabbedPane1_stateChanged(null);
    queryBean1.setEditor(mdeBean1);
	queryBean1.setTabbedPane(JTabbedPane1);	
	
	}

  /**
   * Load all of the plugins specified in the configuration file as
   * new tabs in the main components pane.  This routine also loads the
   * menus and toolboxes for the plugins.
   */
  private void loadPlugins() {

    // Dynamically load the plugins and their associated
    // menus and toolbars

    // First, create the new bean plugin
    try {
      PluginInterface plugin = (PluginInterface)
        createObject("edu.ucsb.nceas.editor.EditorBean");

      // Create a panel to contain the plugin
      JPanel pluginPanel = new JPanel();
      pluginPanel.setLayout(new BorderLayout(0,0));
      JTabbedPane1.add(pluginPanel);
      pluginPanel.setVisible(false);
      pluginPanel.add(BorderLayout.CENTER,(Container)plugin);
  
      // Set the tab title (should get dynamically from the client.properties
      JTabbedPane1.setTitleAt(3,"Demo Editor");

      // Get the list of menus from the plugin components
//      String menus[] = plugin.registerMenus();
  
      // Loop through the menus and create them
//     for (int i=0; i < menus.length; i++) {
//        String currentMenuName = menus[i];
  
//        JMenu currentMenu = null;
        // Check if the menu exists already here
//        if (menuList.containsKey(currentMenuName)) {
//          currentMenu = (JMenu)menuList.get(currentMenuName);
//        } else {
//          currentMenu = new JMenu(); 
//          currentMenu.setText(currentMenuName);
//          currentMenu.setActionCommand(currentMenuName);
          //currentMenu.setMnemonic((int)'H');
//          JMenuBar1.add(currentMenu);
//          menuList.put(currentMenuName, currentMenu);
//        }
  
        // Get the menu items (Actions) and add them to the menus
 //       Action menuActions[] = 
 //         plugin.registerMenuActions(currentMenuName);
 //       for (int j=0; j < menuActions.length; j++) {
 //         Action currentAction = menuActions[j];
//          if (currentMenuName.equals("File")) {
            // Insert File menu items above the "Exit" item and separator
//            int pos = currentMenu.getMenuComponentCount() - 2;
//            if (pos < 0) {
//              pos = 0;
//            }
//            currentMenu.insert(currentAction, pos);
//          } else {
            // Append everything else at the bottom of the menu
//            currentMenu.add(currentAction);
//          }
//        }
//      }

      // Get the toolbar Actions and add them to the toolbar
//      Action toolbarActions[] = plugin.registerToolbarActions();
//      for (int j=0; j < toolbarActions.length; j++) {
//        Action currentAction = toolbarActions[j];
//        JToolBar1.add(currentAction);
//      }

    } catch (ClassCastException cce) {
      System.err.println("Error loading plugin: wrong class!");
    }
  }

    /**
     * Creates a new instance of JFrame1 with the given title.
     * @param sTitle the title for the new frame.
     * @see #JFrame1()
     */
	public ClientFramework(String sTitle)
	{
		this();
		setTitle(sTitle);
	}
	
	/**
	 * The entry point for this application.
	 * Sets the Look and Feel to the System Look and Feel.
	 * Creates a new JFrame1 and makes it visible.
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
            SplashFrame sf = new SplashFrame(true);
            sf.setVisible(true);
			//Create a new instance of our application's frame, and make it visible.
			ClientFramework clf = new ClientFramework();
			clf.setVisible(true);
			sf.dispose();
			ConnectionFrame cf = new ConnectionFrame(clf);
			cf.setVisible(true);
            PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
            String log_file_setting =(String)options.handleGetObject("log_file");     // DFH
			if (log_file_setting!=null)
			{ if(log_file_setting.equalsIgnoreCase("true")) {
			    log_file=true;
			  }
			  else {log_file=false; }
			}
			if (log_file) {
            FileOutputStream err = new FileOutputStream("stderr.log");
            // Constructor PrintStream(OutputStream) has been deprecated.
            PrintStream errPrintStream = new PrintStream(err);
            System.setErr(errPrintStream);
            System.setOut(errPrintStream);
            }
 
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
	java.awt.FileDialog saveFileDialog = new java.awt.FileDialog(this);
	java.awt.FileDialog openFileDialog = new java.awt.FileDialog(this);
	javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
	javax.swing.JToolBar JToolBar1 = new javax.swing.JToolBar();
	javax.swing.JButton newButton = new javax.swing.JButton();
	javax.swing.JButton openButton = new javax.swing.JButton();
	javax.swing.JButton saveButton = new javax.swing.JButton();
	javax.swing.JButton saveserverButton = new javax.swing.JButton();
	com.symantec.itools.javax.swing.JToolBarSeparator JToolBarSeparator1 = new com.symantec.itools.javax.swing.JToolBarSeparator();
	javax.swing.JButton cutButton = new javax.swing.JButton();
	javax.swing.JButton copyButton = new javax.swing.JButton();
	javax.swing.JButton pasteButton = new javax.swing.JButton();
	com.symantec.itools.javax.swing.JToolBarSeparator JToolBarSeparator2 = new com.symantec.itools.javax.swing.JToolBarSeparator();
	javax.swing.JButton aboutButton = new javax.swing.JButton();
	com.symantec.itools.javax.swing.JToolBarSeparator JToolBarSeparator3 = new com.symantec.itools.javax.swing.JToolBarSeparator();
	javax.swing.JCheckBox ExpertCheckBox = new javax.swing.JCheckBox();
	com.symantec.itools.javax.swing.JToolBarSeparator JToolBarSeparator4 = new com.symantec.itools.javax.swing.JToolBarSeparator();
	javax.swing.JTextField ToolBarSearchText = new javax.swing.JTextField();
	javax.swing.JButton queryButton = new javax.swing.JButton();
	javax.swing.JCheckBox LocalSearchCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox CatalogSearchCheckBox = new javax.swing.JCheckBox();
	javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
	javax.swing.JTabbedPane JTabbedPane1 = new javax.swing.JTabbedPane();
	javax.swing.JPanel EditorPanel = new javax.swing.JPanel();
	javax.swing.JPanel QueryPanel = new javax.swing.JPanel();
	javax.swing.JPanel DataViewerPanel = new javax.swing.JPanel();
	javax.swing.JLabel UnderConstruction = new javax.swing.JLabel();
	javax.swing.JLabel dataPict = new javax.swing.JLabel();
	javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
	com.symantec.itools.javax.swing.borders.LineBorder lineBorder1 = new com.symantec.itools.javax.swing.borders.LineBorder();
	javax.swing.JMenuBar JMenuBar1 = new javax.swing.JMenuBar();
	javax.swing.JMenu fileMenu = new javax.swing.JMenu();
	javax.swing.JMenuItem newItem = new javax.swing.JMenuItem();
	javax.swing.JMenuItem openItem = new javax.swing.JMenuItem();
	javax.swing.JMenuItem saveItem = new javax.swing.JMenuItem();
	javax.swing.JMenuItem saveAsItem = new javax.swing.JMenuItem();
	javax.swing.JMenuItem SaveToDatabase = new javax.swing.JMenuItem();
	javax.swing.JMenuItem PreviewXML = new javax.swing.JMenuItem();
	javax.swing.JSeparator JSeparator1 = new javax.swing.JSeparator();
	javax.swing.JMenuItem ConnectMenuItem = new javax.swing.JMenuItem();
	javax.swing.JSeparator JSeparator3 = new javax.swing.JSeparator();
	javax.swing.JMenuItem exitItem = new javax.swing.JMenuItem();
	javax.swing.JMenu editMenu = new javax.swing.JMenu();
	javax.swing.JMenuItem cutItem = new javax.swing.JMenuItem();
	javax.swing.JMenuItem copyItem = new javax.swing.JMenuItem();
	javax.swing.JMenuItem pasteItem = new javax.swing.JMenuItem();
	javax.swing.JSeparator JSeparator2 = new javax.swing.JSeparator();
	javax.swing.JMenuItem OptionsMenuItem = new javax.swing.JMenuItem();
	javax.swing.JMenu WindowsMenu = new javax.swing.JMenu();
	javax.swing.JCheckBoxMenuItem ElementChoiceMenuItem = new javax.swing.JCheckBoxMenuItem();
	javax.swing.JCheckBoxMenuItem ElementTextMenuItem = new javax.swing.JCheckBoxMenuItem();
	javax.swing.JMenu helpMenu = new javax.swing.JMenu();
	javax.swing.JMenuItem aboutItem = new javax.swing.JMenuItem();
	//}}
	edu.ucsb.nceas.metaedit.AbstractMdeBean mdeBean1 = (edu.ucsb.nceas.metaedit.AbstractMdeBean)createObject("edu.ucsb.nceas.metaedit.mdeBean");
    
	edu.ucsb.nceas.querybean.AbstractQueryBean queryBean1 = (edu.ucsb.nceas.querybean.AbstractQueryBean)createObject("edu.ucsb.nceas.querybean.QueryBean");

	//{{DECLARE_MENUS
	//}}

	void exitApplication()
	{
		try {
	    	// Beep
	    	Toolkit.getDefaultToolkit().beep();
	    	// Show a confirmation dialog
	    	int reply = JOptionPane.showConfirmDialog(this, 
	    	                                          "Do you really want to exit?", 
	    	                                          "MORPHO - Exit" , 
	    	                                          JOptionPane.YES_NO_OPTION, 
	    	                                          JOptionPane.QUESTION_MESSAGE);
			// If the confirmation was affirmative, handle exiting.
			if (reply == JOptionPane.YES_OPTION)
			{
			    LogOut();
		    	this.setVisible(false);    // hide the Frame
		    	this.dispose();            // free the system resources
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
			if (object == ClientFramework.this)
				ClientFramework_windowClosing(event);
		}
	}

	void ClientFramework_windowClosing(java.awt.event.WindowEvent event)
	{
		// to do: code goes here.
			 
		ClientFramework_windowClosing_Interaction1(event);
	}

	void ClientFramework_windowClosing_Interaction1(java.awt.event.WindowEvent event) {
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
			if (object == openItem)
				openItem_actionPerformed(event);
			else if (object == saveItem)
				saveItem_actionPerformed(event);
			else if (object == exitItem)
				exitItem_actionPerformed(event);
			else if (object == aboutItem)
				aboutItem_actionPerformed(event);
			else if (object == openButton)
				openButton_actionPerformed(event);
			else if (object == saveButton)
				saveButton_actionPerformed(event);
			else if (object == saveserverButton)
				saveserverButton_actionPerformed(event);
			else if (object == aboutButton)
				aboutButton_actionPerformed(event);
			if (object == queryButton)
				queryButton_actionPerformed(event);
			else if (object == ToolBarSearchText)
				ToolBarSearchText_actionPerformed(event);
			else if (object == newItem)
				newItem_actionPerformed(event);
			else if (object == saveAsItem)
				saveAsItem_actionPerformed(event);
			else if (object == SaveToDatabase)
				SaveToDatabase_actionPerformed(event);
			else if (object == PreviewXML)
				PreviewXML_actionPerformed(event);
			else if (object == OptionsMenuItem)
				OptionsMenuItem_actionPerformed(event);
			else if (object == ConnectMenuItem)
				ConnectMenuItem_actionPerformed(event);
			else if (object == newButton)
				newButton_actionPerformed(event);
			
			
		}
	}

	void openItem_actionPerformed(java.awt.event.ActionEvent event)
	{
		//openItem_actionPerformed_Interaction1(event);
		mdeBean1.openDocument();
	}

	void openItem_actionPerformed_Interaction1(java.awt.event.ActionEvent event) {
		try {
			// openFileDialog Show the FileDialog
			openFileDialog.setVisible(true);
		} catch (Exception e) {
		}
	}

	void saveItem_actionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
		//saveItem_actionPerformed_Interaction1(event);
		mdeBean1.saveDocument();
	}


	void exitItem_actionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
			 
		exitItem_actionPerformed_Interaction1(event);
	}

	void exitItem_actionPerformed_Interaction1(java.awt.event.ActionEvent event) {
		try {
			this.exitApplication();
		} catch (Exception e) {
		}
	}

	void aboutItem_actionPerformed(java.awt.event.ActionEvent event)
	{
				SplashFrame sf = new SplashFrame();
				sf.setVisible(true);
		
	}

	void openButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
			 
		openButton_actionPerformed_Interaction1(event);
	}

	void openButton_actionPerformed_Interaction1(java.awt.event.ActionEvent event) {
		mdeBean1.openDocument();
	}

	void saveserverButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		saveserverButton_actionPerformed_Interaction1(event);
	}

	void saveserverButton_actionPerformed_Interaction1(java.awt.event.ActionEvent event) {
            SubmitDialog sd = new SubmitDialog(this);
            sd.setVisible(true);
    }
    

	void saveButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		mdeBean1.saveDocument();
	}


	void aboutButton_actionPerformed(java.awt.event.ActionEvent event)
	{
				SplashFrame sf = new SplashFrame();
				sf.setVisible(true);
		
	}

	class SymItem implements java.awt.event.ItemListener
	{
		public void itemStateChanged(java.awt.event.ItemEvent event)
		{
			Object object = event.getSource();
			if (object == LocalSearchCheckBox)
				LocalSearchCheckBox_itemStateChanged(event);
			else if (object == CatalogSearchCheckBox)
				CatalogSearchCheckBox_itemStateChanged(event);
			else if (object == ExpertCheckBox)
				ExpertCheckBox_itemStateChanged(event);
			
		}
	}

	void queryButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		if (ToolBarSearchText.getText()!="") {
		    JTabbedPane1.setSelectedIndex(1);
		    queryBean1.searchFor(ToolBarSearchText.getText());
		}
	}
	
	void ToolBarSearchText_actionPerformed(java.awt.event.ActionEvent event)
	{
		if (ToolBarSearchText.getText()!="") {
//		    JTabbedPane1.setSelectedIndex(1);
		    queryBean1.searchFor(ToolBarSearchText.getText());
		}
	}

	void newItem_actionPerformed(java.awt.event.ActionEvent event)
	{
		mdeBean1.newDocument();
			 
	}

	void saveAsItem_actionPerformed(java.awt.event.ActionEvent event)
	{
		mdeBean1.saveDocumentAs();
			 
	}

	void SaveToDatabase_actionPerformed(java.awt.event.ActionEvent event)
	{
        SubmitDialog sd = new SubmitDialog(this);
        sd.setVisible(true);
	}

	void PreviewXML_actionPerformed(java.awt.event.ActionEvent event)
	{
		mdeBean1.previewXMLFile();
	}

	void OptionsMenuItem_actionPerformed(java.awt.event.ActionEvent event)
	{
		mdeBean1.showOptions();;
	}

	class SymChange implements javax.swing.event.ChangeListener
	{
		public void stateChanged(javax.swing.event.ChangeEvent event)
		{
			Object object = event.getSource();
			if (object == JTabbedPane1)
				JTabbedPane1_stateChanged(event);
		}
	}

	void JTabbedPane1_stateChanged(javax.swing.event.ChangeEvent event)
	{
		if (JTabbedPane1.getSelectedIndex()==0) {
		    ElementChoiceMenuItem.setEnabled(true);
		    ElementTextMenuItem.setEnabled(true);
		    newItem.setEnabled(true);
		    openItem.setEnabled(true);
		    saveItem.setEnabled(true);
		    SaveToDatabase.setEnabled(true);
		    PreviewXML.setEnabled(true);
		    newButton.setEnabled(true);
		    openButton.setEnabled(true);
		    saveButton.setEnabled(true);
//		    saveserverButton.setEnabled(true);
		}
		if (JTabbedPane1.getSelectedIndex()==1) {
		    ElementChoiceMenuItem.setEnabled(false);
		    ElementTextMenuItem.setEnabled(false);
		    newItem.setEnabled(false);
		    openItem.setEnabled(false);
		    saveItem.setEnabled(false);
		    SaveToDatabase.setEnabled(false);
		    PreviewXML.setEnabled(false);
		    newButton.setEnabled(false);
		    openButton.setEnabled(false);
		    saveButton.setEnabled(false);
//		    saveserverButton.setEnabled(false);
		}
		if (JTabbedPane1.getSelectedIndex()==2) {
		    ElementChoiceMenuItem.setEnabled(false);
		    ElementTextMenuItem.setEnabled(false);
		    newItem.setEnabled(false);
		    openItem.setEnabled(false);
		    saveItem.setEnabled(false);
		    SaveToDatabase.setEnabled(false);
		    PreviewXML.setEnabled(false);
		    newButton.setEnabled(false);
		    openButton.setEnabled(false);
		    saveButton.setEnabled(false);
//		    saveserverButton.setEnabled(false);
		}
			 
	}
	
// use to dynamically create an object from its name at run time
// uses reflection
	static Object createObject(String className) {
        Object object = null;
        try {
            Class classDefinition = Class.forName(className);
            object = classDefinition.newInstance();
            } catch (InstantiationException e) {
                    System.out.println(e);
            } catch (IllegalAccessException e) {
                    System.out.println(e);
            } catch (ClassNotFoundException e) {
                    System.out.println(e);
            }
           return object;
    }
    
public void LogIn() {
      Properties prop = new Properties();
       prop.put("action","Login Client");

      // Now try to write the document to the database
      try {
        PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
        String MetaCatServletURL =(String)options.handleGetObject("MetaCatServletURL");     // DFH
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
        PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
        String MetaCatServletURL =(String)options.handleGetObject("MetaCatServletURL");     // DFH
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
    
    

	void LocalSearchCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
		queryBean1.setSearchLocal(LocalSearchCheckBox.isSelected());
	}
	
	
	
	

	void CatalogSearchCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
        queryBean1.setSearchNetwork(CatalogSearchCheckBox.isSelected());
	}

	void ExpertCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
	    queryBean1.setExpertMode(ExpertCheckBox.isSelected());
	}

	void ConnectMenuItem_actionPerformed(java.awt.event.ActionEvent event)
	{
			ConnectionFrame cf = new ConnectionFrame(this);
			cf.setVisible(true);
	}

	void newButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		mdeBean1.newDocument();
	}
	
	
	
}
