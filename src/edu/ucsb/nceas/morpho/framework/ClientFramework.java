/**
 *  '$RCSfile: ClientFramework.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-04-23 23:10:40 $'
 * '$Revision: 1.31.2.10 $'
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

package edu.ucsb.nceas.dtclient;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.lang.reflect.*;
import java.lang.ClassCastException;
import com.symantec.itools.javax.swing.borders.LineBorder;

/**
 * The ClientFramework is the main entry point for the Morpho application. It
 * creates the main application frame and sets up the menus and toolbars for
 * the application.  The framework also provides a mechanism for "plugins"
 * to add menus, toolbars, and services to the application. These plugins
 * are dynamically loaded at runtime. Plugins are classes that implement the
 * "PluginInterface" interface.
 */
public class ClientFramework extends javax.swing.JFrame 
                             implements PluginInterface
{
  /** Constant to indicate a spearator should precede an action */
  public static String SEPARATOR_PRECEDING = "TRUE";

  String userName = "public";
  String passWord = "none";
  private static boolean debug = true;
  static int debug_level = 0;
  // redirects standard out and err streams
  static boolean log_file = false;
  String xmlcatalogfile = null;
  String MetaCatServletURL = null;
  ConfigXML config;
  boolean connected = false;
  edu.ucsb.nceas.querybean.LocalQuery lq = null;
  Hashtable menuList = null;
  Action[] fileMenuActions = null;
  Action[] editMenuActions = null;
  Action[] helpMenuActions = null;
  Action[] containerToolbarActions = null;
  Hashtable servicesRegistry = null;
  Hashtable windowsRegistry = null;
  ClientFramework framework = null;
  JTable table;

  public ClientFramework()
  {
    // Create the list of menus for use by the framework and plugins
    menuList = new Hashtable();

    // Create the hash for services
    servicesRegistry = new Hashtable();

    // Create the hash for windows
    windowsRegistry = new Hashtable();

    // This code is automatically generated by Visual Cafe when you add
    // components to the visual environment. It instantiates and initializes
    // the components. To modify the code, only use code syntax that matches
    // what Visual Cafe can generate, or Visual Cafe may be unable to back
    // parse your Java file into its visual environment.
    //{{INIT_CONTROLS
    setJMenuBar(JMenuBar1);
    setTitle("Morpho - Data Management for Ecologists");
    setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout(0, 0));
    setSize(775, 550);
    setVisible(false);

    saveFileDialog.setMode(FileDialog.SAVE);
    saveFileDialog.setTitle("Save");
    openFileDialog.setMode(FileDialog.LOAD);
    openFileDialog.setTitle("Open");
    ToolBarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    getContentPane().add(BorderLayout.NORTH, ToolBarPanel);
    ToolBarPanel.setBounds(0, 0, 744, 36);
    JToolBar1.setAlignmentY(0.222222F);
    ToolBarPanel.add(JToolBar1);
    JToolBar1.setBounds(0, 0, 834, 36);

    ContentPanel.setLayout(new BorderLayout(0, 0));
    getContentPane().add(BorderLayout.CENTER, ContentPanel);
    ContentPanel.setBounds(0, 0, 0, 0);
    JTabbedPane1.setToolTipText("Select tab of interest");
    ContentPanel.add(BorderLayout.CENTER, JTabbedPane1);
    JTabbedPane1.setBounds(0, 0, 0, 0);

    //}}

    //{{INIT_MENUS
    //}}

    //{{REGISTER_LISTENERS
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);
    //}}

    // Get the configuration file information
    try
    {
      config = new ConfigXML("config.xml");
      String local_dtd_directory = config.get("local_dtd_directory", 0);
      xmlcatalogfile = local_dtd_directory + "/catalog";
      MetaCatServletURL = config.get("MetaCatServletURL", 0);
      debug_level = (new Integer(config.get("debug_level", 0))).intValue();
      debug(9, "Debug_level set to: " + debug_level);
    }
    catch(Exception e)
    {
      System.out.println("Could not locate properties file!");
    }

    // Set up the framework's menus and toolbars, and services
    initializeActions();
    loadPluginMenusAndToolbars(this);
    this.registerServices();

    // Load all of the plugins, their menus, and toolbars
    loadPlugins();
  }

  /**
   * Load all of the plugins specified in the configuration file. The plugins
   * are classes that implement the PluginInterface interface.
   */
  private void loadPlugins()
  {
    // Get the list of plugins to load from the config file
    Vector plugins = config.get("plugin");

    // Dynamically load the plugins and their associated menus and toolbars
    try
    {
      for (Enumeration q = plugins.elements(); q.hasMoreElements();)
      {
        // Start by creating the new bean plugin
	PluginInterface plugin = (PluginInterface)
	                createObject((String) (q.nextElement()));

        // Set a reference to the framework in the Plugin
        plugin.setFramework(this);

	// Create a panel to display the plugin if requested
        Container newTab = plugin.registerTabPane();
        if (newTab != null) 
        {
	  JTabbedPane1.addTab(newTab.getName(), newTab);
        }

        // Allow the plugin to add menus and toolbar items
        loadPluginMenusAndToolbars(plugin);

        // Allow the plugin to register services it can perform
        plugin.registerServices();
      }
    }
    catch(ClassCastException cce)
    {
      debug(5, "Error loading plugin: wrong class!");
    }
  }

  /**
   * Load the menus and toolboxes for a particular plugin
   */
  private void loadPluginMenusAndToolbars(PluginInterface plugin)
  {
    // Get the list of menus from the plugin components
    String menus[] = plugin.registerMenus();

    // Loop through the menus and create them
    for (int i=0; i < menus.length; i++) {
    String currentMenuName = menus[i];

    JMenu currentMenu = null;
    // Check if the menu exists already here
    if (menuList.containsKey(currentMenuName)) {
      currentMenu = (JMenu)menuList.get(currentMenuName);
    } else {
      currentMenu = new JMenu(); 
      currentMenu.setText(currentMenuName);
      currentMenu.setActionCommand(currentMenuName);
      //currentMenu.setMnemonic((int)'H');
      JMenuBar1.add(currentMenu);
      menuList.put(currentMenuName, currentMenu);
    }

    // Get the menu items (Actions) and add them to the menus
    Action menuActions[] = plugin.registerMenuActions(currentMenuName);
    if (menuActions != null) {
      for (int j=0; j < menuActions.length; j++) {
        Action currentAction = menuActions[j];
        String hasDefaultSep = (String)currentAction.getValue(Action.DEFAULT);
        if (currentMenuName.equals("File")) {
	  // Insert File menu items above the "Exit" item and separator
          int pos = currentMenu.getMenuComponentCount() - 2;
          if (pos < 0) {
            pos = 0;
          }
          if (hasDefaultSep != null &&
            hasDefaultSep.equals(SEPARATOR_PRECEDING)) {
            currentMenu.insertSeparator(pos++);
          }
          currentMenu.insert(currentAction, pos);
          } else {
	    // Append everything else at the bottom of the menu
            if (hasDefaultSep != null &&
              hasDefaultSep.equals(SEPARATOR_PRECEDING)) {
              currentMenu.addSeparator();
            }
            currentMenu.add(currentAction);
          }
        }
      }
    }

    // Get the toolbar Actions and add them to the toolbar
    Action toolbarActions[] = plugin.registerToolbarActions();
    if (toolbarActions != null) {
      for (int j=0; j < toolbarActions.length; j++) {
        Action currentAction = toolbarActions[j];
        JToolBar1.add(currentAction);
      }
    }
  }

  /**
   * This method is called by plugins to register a Window that
   * the plugin has created.  The window is listed in the "Windows"
   * menu by name.
   *
   * @param window the window to be added to the framework
   */
  public void addWindow(JFrame window)
  {
    String windowName = window.getName();
    if (!windowsRegistry.containsValue(window)) {
      debug(7, "Adding window: " + windowName);
      Action windowAction = new AbstractAction(windowName) {
        public void actionPerformed(ActionEvent e) {
          debug(9, "Selected window.");
          JMenuItem source = (JMenuItem)e.getSource();
          JFrame window = (JFrame)windowsRegistry.get(source);
          window.toFront();
        }
      };
      windowAction.putValue(Action.SHORT_DESCRIPTION, "Select Window");
      JMenu windowMenu = (JMenu)menuList.get("Window");
      JMenuItem windowMenuItem = windowMenu.add(windowAction);
      windowsRegistry.put(windowMenuItem, window);
    }
  }

  /**
   * This method is called by plugins to de-register a Window that
   * the plugin has created.  The window is removed from the "Windows"
   * menu.
   *
   * @param window the window to be removed from the framework
   */
  public void removeWindow(JFrame window)
  {
    debug(9, "Removing window.");
    JMenuItem menuItem = null;
    JMenu windowMenu = (JMenu)menuList.get("Window");
    Enumeration keys = windowsRegistry.keys();
    while (keys.hasMoreElements())
    {
      menuItem = (JMenuItem)keys.nextElement();
      JFrame savedWindow = (JFrame)windowsRegistry.get(menuItem);
      if (savedWindow == window)
      {
        break;
      } else {
        menuItem = null;
      }
    } 
    windowMenu.remove(menuItem);
    windowsRegistry.remove(menuItem);
  }

  /**
   * This method is called by plugins to register a particular service that
   * the plugin can perform.  The service is identified by a serviceName
   * which must be globally unique within the runtime environment of the
   * Morpho appliaction.  If a plugin tries to register a service under
   * a name that is already used, the addService method will generate an 
   * exception.
   *
   * @param serviceName the application unique identifier for the service
   * @param serviceProvider a reference to the object providing the service
   * @throws ServiceExistsException
   */
  public void addService(String serviceName, PluginInterface serviceProvider)
              throws ServiceExistsException
  {
    if (servicesRegistry.containsKey(serviceName)) {
      throw (new ServiceExistsException(serviceName));
    } else {
      debug(7, "Adding service: " + serviceName);
      servicesRegistry.put(serviceName, serviceProvider);
    }
  }

  /**
   * This method is called by plugins to request a particular service that
   * the plugin can perform.  The service request is encapsulated in a
   * ServiceRequest object.  The plugin receives directly the return data
   * in a ServiceResponse object.
   *
   * @param request the service request and associated data
   * @throws ServiceNotHandledException
   */
  public void requestService(ServiceRequest request)
              throws ServiceNotHandledException
  {
    String serviceName = request.getServiceName();
    if (servicesRegistry.containsKey(serviceName)) {
      PluginInterface serviceHandler = 
                      (PluginInterface)servicesRegistry.get(serviceName);
      serviceHandler.handleServiceRequest(request);
    } else {
      throw (new ServiceNotHandledException("Service does not exist: " +
                                            serviceName));
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
   * The plugin must store a reference to the ClientFramework 
   * in order to be able to call the services available through 
   * the framework
   */
  public void setFramework(ClientFramework cf) 
  {
    framework = cf;
  }

  /**
   * This method is called on component initialization to generate a list
   * of the names of the menus, in display order, that the component wants
   * added to the framework.  If a menu already exists (from another component
   * or the framework itself), the order will be determined by the earlier
   * registration of the menu.
   */
  public String[] registerMenus() {
    String listOfMenus[] = new String[4];
    listOfMenus[0] = "File";
    listOfMenus[1] = "Edit";
    listOfMenus[2] = "Window";
    listOfMenus[3] = "Help";
    return listOfMenus;
  }

  /**
   * The plugin must return the Actions that should be associated 
   * with a particular menu. They will be appended onto the bottom of the menu
   * in most cases.
   */
  public Action[] registerMenuActions(String menu) {
    Action actionList[] = null;
    if (menu.equals("File")) {
      actionList = fileMenuActions;
    } else if (menu.equals("Edit")) {
      actionList = editMenuActions;
    } else if (menu.equals("Help")) {
      actionList = helpMenuActions;
    }
    return actionList;
  }

  /**
   * The plugin must return the list of Actions to be associated with the
   * toolbar for the framework. 
   */ 
  public Action[] registerToolbarActions() {
    return containerToolbarActions;;
  }

  /**
   * This method is called by the framework when the plugin should 
   * register any services that it handles.  The plugin should then
   * call the framework's 'addService' method for each service it can
   * handle.
   */
  public void registerServices()
  {
    debug(9, "Entered ClientFramework::registerServices");
    try {
      this.addService("LogService", this);
    } catch (ServiceExistsException see) {
      debug(6, "Service registration failed for LogService.");
      debug(6, see.toString());
    }
  }

  /**
   * This method is called by the framework when the plugin should 
   * register a UI tab pane that is to be incorporated into the main
   * user interface.
   */
  public Container registerTabPane()
  {
    return null;
  }

  /**
   * This is the general dispatch method that is called by the framework
   * whenever a plugin is expected to handle a service request.  The
   * details of the request and data for the request are contained in
   * the ServiceRequest object.
   *
   * @param request request details and data
   */
  public void handleServiceRequest(ServiceRequest request) 
              throws ServiceNotHandledException
  {
    String serviceName = request.getServiceName();
    if (serviceName.equals("LogService")) {
      String message = (String)request.getDataObject("Message");
      debug(1, message);
    } else {
      throw (new ServiceNotHandledException(serviceName));
    }
  }

  /**
   * This method is called by a service provider that is handling 
   *  a service request that originated with the plugin.  Data
   * from the ServiceRequest is handed back to the source plugin in
   * the ServiceResponse object.
   *
   * @param response response details and data
   */
  public void handleServiceResponse(ServiceResponse response)
  {
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() {
    // FILE MENU ACTIONS
    fileMenuActions = new Action[2];

    Action exitItemAction = new AbstractAction("Exit") {
      public void actionPerformed(ActionEvent e) {
        exitApplication();
      }
    };
    exitItemAction.putValue(Action.SHORT_DESCRIPTION, "Exit Morpho");
    exitItemAction.putValue(Action.DEFAULT, SEPARATOR_PRECEDING);
    fileMenuActions[0] = exitItemAction;

    Action connectItemAction = new AbstractAction("Connect...") {
      public void actionPerformed(ActionEvent e) {
        ConnectionFrame cf = new ConnectionFrame();
        cf.setVisible(true);
      }
    };
    connectItemAction.putValue(Action.SHORT_DESCRIPTION, "Log In");
    fileMenuActions[1] = connectItemAction;

    // EDIT MENU ACTIONS
    editMenuActions = new Action[4];
    Action cutItemAction = new AbstractAction("Cut") {
      public void actionPerformed(ActionEvent e) {
        debug(9, "Cut requested.");
      }
    };
    cutItemAction.putValue(Action.SHORT_DESCRIPTION, 
                  "Cut the selection and put it on the Clipboard");
    cutItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().getResource("cut.gif")));
    cutItemAction.setEnabled(false);
    editMenuActions[0] = cutItemAction;

    Action copyItemAction = new AbstractAction("Copy") {
      public void actionPerformed(ActionEvent e) {
        debug(9, "Copy requested.");
      }
    };
    copyItemAction.putValue(Action.SHORT_DESCRIPTION, 
                  "Copy the selection and put it on the Clipboard");
    copyItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().getResource("copy.gif")));
    copyItemAction.setEnabled(false);
    editMenuActions[1] = copyItemAction;

    Action pasteItemAction = new AbstractAction("Paste") {
      public void actionPerformed(ActionEvent e) {
        debug(9, "Paste requested.");
      }
    };
    pasteItemAction.putValue(Action.SHORT_DESCRIPTION, 
                  "Paste the selection.");
    pasteItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().getResource("paste.gif")));
    pasteItemAction.setEnabled(false);
    editMenuActions[2] = pasteItemAction;

    Action prefsItemAction = new AbstractAction("Preferences...") {
      public void actionPerformed(ActionEvent e) {
        debug(9, "Preferences requested. GUI not yet implemented!");
      }
    };
    prefsItemAction.putValue(Action.SHORT_DESCRIPTION, 
                  "Open the Preferences dialog.");
    prefsItemAction.putValue(Action.DEFAULT, SEPARATOR_PRECEDING);
    editMenuActions[3] = prefsItemAction;

    // HELP MENU ACTIONS
    helpMenuActions = new Action[2];
    Action aboutItemAction = new AbstractAction("About...") {
      public void actionPerformed(ActionEvent e) {
        SplashFrame sf = new SplashFrame();
        sf.setVisible(true);
      }
    };
    aboutItemAction.putValue(Action.SHORT_DESCRIPTION, "About Morpho");
    aboutItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().getResource("about.gif")));
    helpMenuActions[0] = aboutItemAction;

    Action testServiceAction = new AbstractAction("Test Log Service") {
      public void actionPerformed(ActionEvent e) {
        testLogService();
      }
    };
    testServiceAction.putValue(Action.SHORT_DESCRIPTION, "Test Logging");
    testServiceAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().getResource("about.gif")));
    helpMenuActions[1] = testServiceAction;

    // Set up the toolbar for the application
    containerToolbarActions = new Action[3];
    containerToolbarActions[0] = cutItemAction;
    containerToolbarActions[1] = copyItemAction;
    containerToolbarActions[2] = pasteItemAction;
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
    {
      return;
    }

    frameSizeAdjusted = true;

    // Adjust size of frame according to the insets and menu bar
    javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
    int menuBarHeight = 0;
    if (menuBar != null)
    {
      menuBarHeight = menuBar.getPreferredSize().height;
    }
    Insets insets = getInsets();

    setSize(insets.left + insets.right + size.width,
            insets.top + insets.bottom + size.height + menuBarHeight);
  }

  // Used by addNotify
  boolean frameSizeAdjusted = false;

  //{{DECLARE_CONTROLS
  java.awt.FileDialog saveFileDialog = new java.awt.FileDialog(this);
  java.awt.FileDialog openFileDialog = new java.awt.FileDialog(this);
  javax.swing.JPanel ToolBarPanel = new javax.swing.JPanel();
  javax.swing.JToolBar JToolBar1 = new javax.swing.JToolBar();

  javax.swing.JPanel ContentPanel = new javax.swing.JPanel();
  javax.swing.JTabbedPane JTabbedPane1 = new javax.swing.JTabbedPane();

  javax.swing.JLabel UnderConstruction = new javax.swing.JLabel();
  javax.swing.JLabel dataPict = new javax.swing.JLabel();
  javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
  com.symantec.itools.javax.swing.borders.LineBorder lineBorder1 =
    new com.symantec.itools.javax.swing.borders.LineBorder();
  javax.swing.JMenuBar JMenuBar1 = new javax.swing.JMenuBar();

  //}}

  //{{DECLARE_MENUS
  //}}

  void exitApplication()
  {
    try
    {
      // Beep
      Toolkit.getDefaultToolkit().beep();
      // Show a confirmation dialog
      int reply = JOptionPane.showConfirmDialog(this,
						"Do you really want to exit?",
						"Morpho - Exit",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
      // If the confirmation was affirmative, handle exiting.
      if (reply == JOptionPane.YES_OPTION)
      {
	LogOut();
	this.setVisible(false);	// hide the Frame
	this.dispose();		// free the system resources
	System.exit(0);		// close the application
      }
    }
    catch(Exception e)
    {
    }
  }

  void testLogService()
  {
    ServiceRequest req = new ServiceRequest((PluginInterface)this,
                                            "LogService");
    req.addDataObject("Message", "Holy cow, batman!");
    try {
      this.requestService(req);
    } catch (ServiceNotHandledException snhe) {
      debug(1, snhe.toString());
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

  void ClientFramework_windowClosing_Interaction1(java.awt.
						  event.WindowEvent event)
  {
    try
    {
      this.exitApplication();
    }
    catch(Exception e)
    {
    }
  }

  /**
   *  use to dynamically create an object from its name at run time
   *  uses reflection
   */
  private Object createObject(String className)
  {
    Object object = null;
    try
    {
      Class classDefinition = Class.forName(className);
      object = classDefinition.newInstance();
    }
    catch(InstantiationException e)
    {
      debug(1, e.toString());
    }
    catch(IllegalAccessException e)
    {
      debug(1, e.toString());
    }
    catch(ClassNotFoundException e)
    {
      debug(1, e.toString());
    }
    return object;
  }

  /**
   * Log into metacat
   */
  public void LogIn()
  {
    Properties prop = new Properties();
    prop.put("action", "Login Client");

    // Now contact metacat
      try
    {
      String MetaCatServletURL = config.get("MetaCatServletURL", 0);
        debug(9, "Trying: " + MetaCatServletURL);
      URL url = new URL(MetaCatServletURL);
      HttpMessage msg = new HttpMessage(url);
        prop.put("username", userName);
        prop.put("password", passWord);
      InputStream returnStream = msg.sendPostMessage(prop);
      StringWriter sw = new StringWriter();
      int c;
      while ((c = returnStream.read()) != -1)
      {
	sw.write(c);
      }
      returnStream.close();
      String res = sw.toString();
      sw.close();
      debug(5, res);

    }
    catch(Exception e)
    {
      debug(1, "Error logging into system");
    }
  }

  /**
   * Log out of metacat
   */
  public void LogOut()
  {
    Properties prop = new Properties();
    prop.put("action", "Logout");

    // Now try to write the document to the database
    try
    {
      String MetaCatServletURL = config.get("MetaCatServletURL", 0);
        debug(9, "Trying: " + MetaCatServletURL);
      URL url = new URL(MetaCatServletURL);
      HttpMessage msg = new HttpMessage(url);
      InputStream returnStream = msg.sendPostMessage(prop);
      StringWriter sw = new StringWriter();
      int c;
      while ((c = returnStream.read()) != -1)
      {
	sw.write(c);
      }
      returnStream.close();
      String res = sw.toString();
      sw.close();
    }
    catch(Exception e)
    {
      debug(1, "Error logging out of system");
    }
  }

  /**
   * Print debugging messages based on severity level, where severity level 1
   * are the most critical and severity level 9 the most trivial messages.
   * Setting the debug_level to 0 in the configuration file turns all messages
   * off.
   *
   * @param severity the severity of the debug message
   * @param message the message to log
   */
  public void debug (int severity, String message)
  {
    if (debug) {
      if (debug_level > 0 && severity <= debug_level) {
        System.err.println(message);
      }
    }
  } 

  /**
   * The entry point for this application.
   * Sets the Look and Feel to the System Look and Feel.
   * Creates a new JFrame1 and makes it visible.
   */
  static public void main(String args[])
  {
    try
    {
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

      // Create a new instance of our application's frame
      ClientFramework clf = new ClientFramework();

      Date expiration = new Date(101, 5, 1);
      Date warning = new Date(101, 4, 1);
      Date now = new Date();
      if (now.after(expiration))
      {
	clf.debug(1, "This beta version of Morpho has expired! " +
           "See http://knb.ecoinformatics.org/ for a newer version.");
	JOptionPane.showMessageDialog(null,
           "This beta version of Morpho has expired! " +
           "See http://knb.ecoinformatics.org/ for a newer version.");
	System.exit(1);
      }
      else
      {
	if (now.after(warning))
	{
	  clf.debug(1, "This beta version of Morpho will expire on " +
            "May 1, 2001. See http://knb.ecoinformatics.org/ for a " +
            "newer version.");
	  JOptionPane.showMessageDialog(null,
            "This beta version of Morpho will expire on May 1, 2001. " +
            "See http://knb.ecoinformatics.org/ for a newer version.");
	}

        // make the ClientFramework visible.
	clf.setVisible(true);
	sf.dispose();
        // ConnectionFrame cf = new ConnectionFrame(clf);
        // cf.setVisible(true);
	ConfigXML config = new ConfigXML("config.xml");
	String log_file_setting = config.get("log_file", 0);
	if (log_file_setting != null)
	{
	  if (log_file_setting.equalsIgnoreCase("true"))
	  {
	    log_file = true;
	  }
	  else
	  {
	    log_file = false;
	  }
	}
	if (log_file)
	{
	  FileOutputStream err = new FileOutputStream("stderr.log");
	  // Constructor PrintStream(OutputStream) has been deprecated.
	  PrintStream errPrintStream = new PrintStream(err);
	  System.setErr(errPrintStream);
	  System.setOut(errPrintStream);
	}
      }

    }
    catch(Throwable t)
    {
      t.printStackTrace();
      //Ensure the application exits with an error condition.
      System.exit(1);
    }
  }

}
