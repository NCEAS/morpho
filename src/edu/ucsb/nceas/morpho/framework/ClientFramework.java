/**
 *  '$RCSfile: ClientFramework.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-12 23:09:36 $'
 * '$Revision: 1.51 $'
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

package edu.ucsb.nceas.morpho.framework;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.lang.reflect.*;
import java.lang.ClassCastException;

/**
 * The ClientFramework is the main entry point for the Morpho application. It
 * creates the main application frame and sets up the menus and toolbars for
 * the application.  The framework also provides a mechanism for "plugins"
 * to add menus, toolbars, and services to the application. These plugins
 * are dynamically loaded at runtime. Plugins are classes that implement the
 * "PluginInterface" interface.
 */
public class ClientFramework extends javax.swing.JFrame 
{
  /** The hardcoded XML configuration file */
  private static String configFile = "lib/config.xml";

  /** Constant to indicate a spearator should precede an action */
  public static String SEPARATOR_PRECEDING = "separator_preceding";

  private String userName = "public";
  private String passWord = "none";
  private static boolean debug = true;
  private static int debug_level = 9;
  // redirects standard out and err streams
  static boolean log_file = false;
  private String metacatURL = null;
  private ConfigXML config;
  private ConfigXML profile;
  private boolean connected = false;
  private Hashtable menuList = null;
  private TreeMap menuOrder = null;
  private Action[] fileMenuActions = null;
  private Action[] editMenuActions = null;
  private Action[] helpMenuActions = null;
  private Action[] containerToolbarActions = null;
  private Hashtable servicesRegistry = null;
  private Hashtable windowsRegistry = null;
  private Vector connectionRegistry = null;
  private boolean pluginsLoaded = false;

  // Used by addNotify
  boolean frameSizeAdjusted = false;

  //{{DECLARE_CONTROLS
  javax.swing.JPanel toolbarPanel = new javax.swing.JPanel();
  javax.swing.JToolBar morphoToolbar = new javax.swing.JToolBar();
  javax.swing.JMenuBar morphoMenuBar = new javax.swing.JMenuBar();
  //}}

  //{{DECLARE_MENUS
  //}}

  /**
   * Creates a new instance of ClientFramework with the given title.
   * @param sTitle the title for the new frame.
   * @see #JFrame1()
   */
  public ClientFramework(String sTitle, ConfigXML config)
  {
    this(config);
    setTitle(sTitle);
  }

  /**
   * Creates a new instance of ClientFramework
   * @see #JFrame1()
   */
  public ClientFramework(ConfigXML config)
  {
    this.config = config;
    this.profile = null;

    // Create the list of menus for use by the framework and plugins
    menuList = new Hashtable();
    menuOrder = new TreeMap();

    // Create the hash for services
    servicesRegistry = new Hashtable();

    // Create the hash for windows
    windowsRegistry = new Hashtable();

    // Create the connection registry
    connectionRegistry = new Vector();

    // This code is automatically generated by Visual Cafe when you add
    // components to the visual environment. It instantiates and initializes
    // the components. To modify the code, only use code syntax that matches
    // what Visual Cafe can generate, or Visual Cafe may be unable to back
    // parse your Java file into its visual environment.
    //{{INIT_CONTROLS
    setJMenuBar(morphoMenuBar);
    setTitle("Morpho - Data Management for Ecologists");
    setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout(0, 0));
    setVisible(false);

    toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
    getContentPane().add(BorderLayout.NORTH, toolbarPanel);
    morphoToolbar.setAlignmentY(0.222222F);
    toolbarPanel.add(morphoToolbar);
    //}}

    //{{INIT_MENUS
    //}}

    //{{REGISTER_LISTENERS
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);
    //}}


    // Get the configuration file information needed by the framework
    loadConfigurationParameters();

    // Set up the framework's menus and toolbars, and services
    initializeActions();
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
        plugin.initialize(this);
      }

      // After all plugins have a chance to add their menus, create the
      // menu bar so that the menus are created in the right order
      Set menusInOrder = menuOrder.entrySet();
      Iterator it = menusInOrder.iterator();
      while (it.hasNext()) {
        JMenu currentMenu = (JMenu)((Map.Entry)it.next()).getValue();
        morphoMenuBar.add(currentMenu);
      }
      
      pluginsLoaded = true;
    }
    catch(ClassCastException cce)
    {
      debug(5, "Error loading plugin: wrong class!");
    }
  }

  /**
   * Set the content pane of the main Morpho window to display the
   * component indicated.  Note that this will replace the current content
   * pane, and so only one plugin should call this routine.
   *
   * @param comp the component to display
   */
  public void setMainContentPane(Component comp) 
  {
    // Create a panel to display the plugin if requested
    if (comp != null) {
      getContentPane().add(BorderLayout.CENTER, comp);
      comp.invalidate();
      invalidate();
    } else {
      debug(9, "Component was null so I could not set it!");
    }
  }

  /**
   * This method is called by plugins to register a menu that
   * the plugin wants created, but that currently has no items.
   *
   * @param menuName the name of the menu to be added to the framework
   * @param menuPosition the position of the menu to be added to the framework
   */
  public void addMenu(String menuName, Integer menuPosition)
  {
    addMenu(menuName, menuPosition, null);
  }

  /**
   * This method is called by plugins to register a menu and its
   * associated Actions. If the menu already exists, the actions
   * are added to it.
   *
   * @param menuName the name of the menu to which to add the action
   * @param menuPosition the  position of the menu on the menu bar
   * @param menuActions an array of Actions to be added to the menu
   */
  public void addMenu(String menuName, Integer menuPosition, 
                      Action[] menuActions)
  {
    JMenu currentMenu = null;
    // Check if the menu exists already here, otherwise create it
    if (menuList.containsKey(menuName)) {
      currentMenu = (JMenu)menuList.get(menuName);
    } else {
      currentMenu = new JMenu(); 
      currentMenu.setText(menuName);
      currentMenu.setActionCommand(menuName);
      //currentMenu.setMnemonic((int)'H');
      menuList.put(menuName, currentMenu);
      menuOrder.put(menuPosition, currentMenu);

      // After the initial plugin loading, menus can only be appended
      // to the end of the menu bar
      if (pluginsLoaded) {
        morphoMenuBar.add(currentMenu);
      }
    }

    // Get the menu items (Actions) and add them to the menu
    if (menuActions != null) {
      for (int j=0; j < menuActions.length; j++) {
        Action currentAction = menuActions[j];
        String hasDefaultSep = (String)currentAction.getValue(Action.DEFAULT);
        Integer itemPosition = (Integer)currentAction.getValue("menuPosition");
        int menuPos = (itemPosition != null) ? itemPosition.intValue() : -1;

        if (menuPos >= 0) {
          // Insert menus at the specified position
          int menuCount = currentMenu.getMenuComponentCount();
          if (menuPos > menuCount) {
            menuPos = menuCount;
          }
          
          if (hasDefaultSep != null &&
            hasDefaultSep.equals(SEPARATOR_PRECEDING)) {
            currentMenu.insertSeparator(menuPos++);
          }
          currentMenu.insert(currentAction, menuPos);
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

  /**
   * This method is called by plugins to register a toolbar Action. 
   *
   * @param toolbarActions an array of Actions to be added to the toolbar
   */
  public void addToolbarActions(Action[] toolbarActions)
  {
    if (toolbarActions != null) {
      for (int j=0; j < toolbarActions.length; j++) {
        Action currentAction = toolbarActions[j];
        morphoToolbar.add(currentAction);
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
          JFrame window1 = (JFrame)windowsRegistry.get(source);
          window1.toFront();
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
   * the plugin can perform.  The service is identified by the class 
   * of an interface that the service implements.
   *
   * @param serviceInterface the interface representing this service
   * @param provider a reference to the object providing the service
   * @throws ServiceExistsException
   */
  public void addService(Class serviceInterface, ServiceProvider provider)
              throws ServiceExistsException
  {
    if (servicesRegistry.containsKey(serviceInterface)) {
      throw (new ServiceExistsException(serviceInterface.getName()));
    } else {
      debug(7, "Adding service: " + serviceInterface.getName());
      servicesRegistry.put(serviceInterface, provider);
    }
  }

  /**
   * This method is called by plugins to determine if a particular 
   * service has been registered and is available.
   *
   * @param serviceInterface the service interface desired
   * @returns boolean true if the service exists, false otherwise
   */
  public boolean checkForService(Class serviceInterface)
  {
    if (servicesRegistry.containsKey(serviceInterface)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * This method is called by plugins to get a reference to an
   * object that implements a particular interface
   *
   * @param serviceInterface the service interface desired
   * @returns ServiceProvider a reference to the objecy providing the service
   */
  public ServiceProvider getServiceProvider(Class serviceInterface)
         throws ServiceNotHandledException
  {
    if (servicesRegistry.containsKey(serviceInterface)) {
      return (ServiceProvider)servicesRegistry.get(serviceInterface);
    } else {
      throw (new ServiceNotHandledException("No such service registered."));
    }
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() {
    // FILE MENU ACTIONS
    fileMenuActions = new Action[3];

    Action exitItemAction = new AbstractAction("Exit") {
      public void actionPerformed(ActionEvent e) {
        exitApplication();
      }
    };
    exitItemAction.putValue(Action.SHORT_DESCRIPTION, "Exit Morpho");
    exitItemAction.putValue(Action.DEFAULT, SEPARATOR_PRECEDING);
    exitItemAction.putValue("menuPosition", new Integer(-1));
    fileMenuActions[0] = exitItemAction;

    Action connectItemAction = new AbstractAction("Connect...") {
      public void actionPerformed(ActionEvent e) {
        establishConnection();
      }
    };
    connectItemAction.putValue(Action.SHORT_DESCRIPTION, "Log In");
    connectItemAction.putValue("menuPosition", new Integer(0));
    fileMenuActions[1] = connectItemAction;

    Action profileItemAction = new AbstractAction("New profile...") {
      public void actionPerformed(ActionEvent e) {
        createNewProfile();
      }
    };
    profileItemAction.putValue(Action.SHORT_DESCRIPTION, "New Profile");
    profileItemAction.putValue("menuPosition", new Integer(1));
    fileMenuActions[2] = profileItemAction;

    addMenu("File", new Integer(1), fileMenuActions);

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
                    new ImageIcon(getClass().
          getResource("/toolbarButtonGraphics/general/Cut16.gif")));
    cutItemAction.putValue("menuPosition", new Integer(1));
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
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Copy16.gif")));
    copyItemAction.putValue("menuPosition", new Integer(2));
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
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Paste16.gif")));
    pasteItemAction.putValue("menuPosition", new Integer(3));
    pasteItemAction.setEnabled(false);
    editMenuActions[2] = pasteItemAction;

    Action prefsItemAction = new AbstractAction("Preferences...") {
      public void actionPerformed(ActionEvent e) {
        debug(9, "Preferences requested. GUI not yet implemented!");
      }
    };
    prefsItemAction.putValue(Action.SHORT_DESCRIPTION, 
                  "Open the Preferences dialog.");
    prefsItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
    prefsItemAction.putValue(Action.DEFAULT, SEPARATOR_PRECEDING);
    prefsItemAction.putValue("menuPosition", new Integer(5));
    editMenuActions[3] = prefsItemAction;

    addMenu("Edit", new Integer(2), editMenuActions);

    addMenu("Window", new Integer(6));

    // HELP MENU ACTIONS
    helpMenuActions = new Action[1];
    Action aboutItemAction = new AbstractAction("About...") {
      public void actionPerformed(ActionEvent e) {
        SplashFrame sf = new SplashFrame();
        sf.setVisible(true);
      }
    };
    aboutItemAction.putValue(Action.SHORT_DESCRIPTION, "About Morpho");
    aboutItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/About16.gif")));
    aboutItemAction.putValue("menuPosition", new Integer(1));
    helpMenuActions[0] = aboutItemAction;
/*
    Action testServiceAction = new AbstractAction("Test Service") {
      public void actionPerformed(ActionEvent e) {
        testService();
      }
    };
    testServiceAction.putValue(Action.SHORT_DESCRIPTION, "Test");
    testServiceAction.putValue(Action.DEFAULT, SEPARATOR_PRECEDING);
    testServiceAction.putValue("menuPosition", new Integer(2));
    helpMenuActions[1] = testServiceAction;
*/
    addMenu("Help", new Integer(9), helpMenuActions);

    // Set up the toolbar for the application
    containerToolbarActions = new Action[3];
    containerToolbarActions[0] = cutItemAction;
    containerToolbarActions[1] = copyItemAction;
    containerToolbarActions[2] = pasteItemAction;
    addToolbarActions(containerToolbarActions);
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

  /**
   * Exit the application, asking the user if they are sure
   */
  private void exitApplication()
  {
    try
    {
      // Beep
      Toolkit.getDefaultToolkit().beep();
/*
      // MBJ -- This dialog isn;t needed.  Really need to check for dirty
      // documents and exit quickly if nothing is to be done.

      // Show a confirmation dialog
      int reply = JOptionPane.showConfirmDialog(this,
                                                "Do you really want to exit?",
                                                "Morpho - Exit",
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE);
      // If the confirmation was affirmative, handle exiting.
      if (reply == JOptionPane.YES_OPTION)
      {
*/
        this.setVisible(false);        // hide the Frame
        logOut();
        config.save();
        this.dispose();                // free the system resources
        System.exit(0);                // close the application
/*
      }
*/
    }
    catch(Exception e)
    {
    }
  }

  /** 
   * Create a new connection to metacat
   */
  private void establishConnection()
  {
    ConnectionFrame cf = new ConnectionFrame(this);
    cf.setVisible(true);
  }

  /** 
   * Create a new profile
   */
  private void createNewProfile()
  {
    ProfileDialog dialog = new ProfileDialog(this);
    dialog.setVisible(true);
  }

  /** Listen for window closing events */
  class SymWindow extends java.awt.event.WindowAdapter
  {
    public void windowClosing(java.awt.event.WindowEvent event)
    {
      Object object = event.getSource();
      if (object == ClientFramework.this)
          ClientFramework_windowClosing(event);
    }
  }

  /** process window closing events */
  private void ClientFramework_windowClosing(java.awt.event.WindowEvent event)
  {
    // to do: code goes here.
    ClientFramework_windowClosing_Interaction1(event);
  }

  /** process window closing events */
  private void ClientFramework_windowClosing_Interaction1(java.awt.
                                                  event.WindowEvent event)
  {
    try {
      this.exitApplication();
    } catch(Exception e) {
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
   * Send a request to Metacat
   *
   * @param prop the properties to be sent to Metacat
   * @param requiresLogin indicates whether a valid connection is required
                          for the operation
   * @return InputStream as returned by Metacat
   */
  public InputStream getMetacatInputStream(Properties prop, 
                                           boolean requiresLogin)
  {
    if (requiresLogin) {
      if (!connected) {
        // Ask the user to connect
        establishConnection();
      }
    }
    return getMetacatInputStream(prop);
  }

  /**
   * Send a request to Metacat
   *
   * @param prop the properties to be sent to Metacat
   * @return InputStream as returned by Metacat
   */
  public InputStream getMetacatInputStream(Properties prop)
  {
    InputStream returnStream = null;
    // Now contact metacat and send the request
    try
    {
      //String metacatURL = config.get("MetaCatServletURL", 0);
      debug(9, "Sending data to: " + metacatURL);
      URL url = new URL(metacatURL);
      HttpMessage msg = new HttpMessage(url);
      returnStream = msg.sendPostMessage(prop);
    }
    catch(Exception e)
    {
      debug(1, "Fatal error sending data to Metacat.");
    }
    return returnStream;
  }

  /**
   * Send a request to Metacat
   *
   * @param prop the properties to be sent to Metacat
   * @param requiresLogin indicates whether a valid connection is required
                          for the operation
   * @return a string as returned by Metacat
   */
  public String getMetacatString(Properties prop, boolean requiresLogin)
  {
    if (requiresLogin) {
      if (!connected) {
        // Ask the user to connect
        establishConnection();
      }
    }
    return (String)getMetacatString(prop);
  }

  /**
   * Send a request to Metacat
   *
   * @param prop the properties to be sent to Metacat
   * @return a string as returned by Metacat
   */
  public String getMetacatString(Properties prop)
  {
    String response = null;

    // Now contact metacat and send the request
    try
    {
      InputStreamReader returnStream = 
                        new InputStreamReader(getMetacatInputStream(prop));
      StringWriter sw = new StringWriter();
      int len;
      char[] characters = new char[512];
      while ((len = returnStream.read(characters, 0, 512)) != -1)
      {
        sw.write(characters, 0, len);
      }
      returnStream.close();
      response = sw.toString();
      sw.close();
      debug(5, response);
    }
    catch(Exception e)
    {
      debug(1, "Fatal error sending data to Metacat.");
    }
    return response;
  }

  /**
   * Log into metacat
   */
  public boolean logIn()
  {
    Properties prop = new Properties();
    prop.put("action", "login");
    prop.put("qformat", "xml");
    prop.put("username", userName);
    prop.put("password", passWord);

    // Now contact metacat
    String response = getMetacatString(prop);
    boolean wasConnected = connected;
    if (response.indexOf("<login>") != -1) {
      connected = true;
    } else {
      HttpMessage.setCookie(null);
      connected = false;
    }

    if (wasConnected != connected) {
      fireConnectionChangedEvent();
    }

    return connected;
  }

  /**
   * Log out of metacat
   */
  public void logOut()
  {
    if (connected) {
      Properties prop = new Properties();
      prop.put("action", "logout");
      prop.put("qformat", "xml");
  
      String response = getMetacatString(prop);
      HttpMessage.setCookie(null);
      connected = false;

      fireConnectionChangedEvent();
    }
  }

  /**
   * Set the username associated with this framework
   *
   * @param the new username for the framework
   */
  public void setUserName(String uname)
  {
    if (!userName.equals(uname)) {
      this.userName = uname;
      boolean success = config.set("username", 0, uname);
      if (!success)
      {
        config.insert("username", uname);
      }

      fireUsernameChangedEvent();
    }
  } 

  /**
   * Get the username associated with this framework
   *
   * @returns String the username
   */
  public String getUserName()
  {
    return userName;
  } 

  /**
   * Set the password associated with this framework
   *
   * @param the new password for the framework
   */
  public void setPassword(String pword)
  {
    this.passWord = pword;
  } 

  /**
   * Determines if the framework has a valid login
   *
   * @returns boolean true if connected, false otherwise
   */
  public boolean isConnected()
  {
    return connected;
  } 

  /**
   * This method is called by plugins to register a listener for 
   * changes in the Connection status.  Any change in the username,
   * password, or other connect change will trigger notification.
   *
   * @param listener a reference to the object to be notified of changes
   * @throws ServiceExistsException
   */
  public void addConnectionListener(ConnectionListener listener)
  {
    if (!connectionRegistry.contains(listener)) {
      debug(7, "Adding listener: " + listener.toString());
      connectionRegistry.add(listener);
    }
  }

  /**
   * Fire off notifications for all of the registered ConnectionListeners
   * when the connection status changes.
   */
  private void fireConnectionChangedEvent() 
  {
    for (int i=0; i < connectionRegistry.size(); i++) {
      ConnectionListener listener = 
                         (ConnectionListener)connectionRegistry.get(i);
      if (listener != null) {
        listener.connectionChanged(isConnected());
      }
    }
  }

  /**
   * Fire off notifications for all of the registered ConnectionListeners
   * when the username is changed.
   */
  private void fireUsernameChangedEvent() 
  {
    for (int i=0; i < connectionRegistry.size(); i++) {
      ConnectionListener listener = 
                         (ConnectionListener)connectionRegistry.get(i);
      if (listener != null) {
        listener.usernameChanged(getUserName());
      }
    }
  }

  /**
   * Get the configuration object associated with the framework.  Plugins use
   * this object to get and set persistent configuration parameters.
   *
   * @returns ConfigXML the configuration object
   */
  public ConfigXML getConfiguration()
  {
    return config;
  } 

  /**
   * Get the profile for the currently logged in user.  
   *
   * @returns ConfigXML the profile object
   */
  public ConfigXML getProfile()
  {
    return profile;
  } 

  /**
   * Set the profile for the currently logged in user
   * (on startup, or when switching profiles).
   *
   * @param profile the profile object
   */
  public void setProfile(ConfigXML profile)
  {
    this.profile = profile;

    // Load basic profile information
    String username = profile.get("username", 0);
    setUserName(username);

    if (! config.set("current_profile", 0, username)) {
      boolean success = config.insert("current_profile", username);
    }
    config.save();

    // Notify plugins that the profile changed
    // Not yet implemented
  } 

  /**
   * returns the next local id from the config file
   * returns null if configXML was unable to increment the id number
   */
  public String getNextId()
  {
    String scope = config.get("scope", 0);
    String lastidS = config.get("lastId", 0);
    int lastid = (new Integer(lastidS)).intValue();
    String separator = config.get("separator", 0);
    
    if(scope.trim().equals("USERNAME"))
    { //this keyword means to use the username for the scope
      String username = config.get("username", 0);
      scope = username;
    }
    
    String identifier = scope + separator + lastid;
    lastid++;
    String s = "" + lastid;
    if(!config.set("lastId", 0, s))
    {
      debug(0, "Error incrementing the accession number id");
      return null;
    }
    else
    {
      return identifier + ".1"; 
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
  public static void debug(int severity, String message)
  {
    if (debug) {
      if (debug_level > 0 && severity <= debug_level) {
        System.err.println(message);
      }
    }
  } 

  /**
   * Load configuration parameters from the config file as needed
   */
  private void loadConfigurationParameters()
  {
    metacatURL = config.get("MetaCatServletURL", 0);
    String temp_uname = config.get("username", 0);
    userName = (temp_uname != null) ? temp_uname : "public";
    debug_level = (new Integer(config.get("debug_level", 0))).intValue();
    debug(9, "Debug_level set to: " + debug_level);
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

      // Open the configuration file
      ConfigXML config = new ConfigXML(configFile);

      // Create a new instance of our application's frame
      ClientFramework clf = new ClientFramework(config);

      Date expiration = new Date(101, 12, 1);
      Date warning = new Date(101, 11, 1);
      Date now = new Date();
      if (now.after(expiration))
      {
        clf.debug(1, "This beta version of Morpho has expired! " +
           "See http://knb.ecoinformatics.org/ for a newer version.");
           JOptionPane.showMessageDialog(null,
           "This beta version of Morpho has expired!\n" +
           "See http://knb.ecoinformatics.org/ for a newer version.");
           System.exit(1);
      }
      else
      {
        if (now.after(warning))
        {
          clf.debug(1, "This beta version of Morpho will expire on " +
            "Dec 1, 2001. See http://knb.ecoinformatics.org/ for a " +
            "newer version.");
          JOptionPane.showMessageDialog(null,
            "This beta version of Morpho will expire on Dec 1, 2001.\n" +
            "See http://knb.ecoinformatics.org/ for a newer version.");
        }


        sf.dispose();

        // Load the current profile and log in
        String profileDir = config.get("profile_directory", 0);
        String currentProfile = config.get("current_profile", 0);
        if (currentProfile == null) {
          ProfileDialog dialog = new ProfileDialog(clf);
          dialog.setVisible(true);
          // Make sure they actually created a profile
          if (clf.getProfile() == null) {
            JOptionPane.showMessageDialog(null,
            "You must create a profile in order to configure Morpho  \n" +
            "correctly.  Please restart Morpho and try again.");
            clf.exitApplication();
          }
        } else {
          String profileName = profileDir + File.separator + currentProfile + 
                        File.separator + currentProfile + ".xml";
          ConfigXML profile = new ConfigXML(profileName);
          clf.setProfile(profile);
          clf.establishConnection();          
        }

        // Set up logging as appropriate
        String log_file_setting = config.get("log_file", 0);
        if (log_file_setting != null) {
          if (log_file_setting.equalsIgnoreCase("true")) {
            log_file = true;
          } else {
            log_file = false;
          }
        }
        if (log_file) {
          FileOutputStream err = new FileOutputStream("stderr.log");
          // Constructor PrintStream(OutputStream) has been deprecated.
          PrintStream errPrintStream = new PrintStream(err);
          System.setErr(errPrintStream);
          System.setOut(errPrintStream);
        }

        // Load all of the plugins, their menus, and toolbars
        clf.loadPlugins();

        // make the ClientFramework visible.
        clf.pack();
        clf.setVisible(true);

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
