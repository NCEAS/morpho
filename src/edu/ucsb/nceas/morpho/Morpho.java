/**
 *  '$RCSfile: Morpho.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-08 00:11:29 $'
 * '$Revision: 1.1.2.1 $'
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

package edu.ucsb.nceas.morpho;

import edu.ucsb.nceas.morpho.framework.*;

import edu.ucsb.nceas.itis.Itis;
import edu.ucsb.nceas.itis.ItisException;
import edu.ucsb.nceas.itis.Taxon;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer; //required in addition to import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.lang.reflect.*;
import java.lang.ClassCastException;
import java.net.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.sun.net.ssl.internal.ssl.*;
/**
 * Morpho is the main entry point for the Morpho application. It
 * creates the main application state and sets up the menus and toolbars for
 * the application.  The framework also provides a mechanism for "plugins"
 * to add menus, toolbars, and services to the application. These plugins
 * are dynamically loaded at runtime. Plugins are classes that implement the
 * "PluginInterface" interface.
 */
public class Morpho 
{
  /** The version of this release of Morpho */
  public static String VERSION = "0.0.0";

  /** The hardcoded XML configuration file */
  private static String configFile = "config.xml";

  /** Constant to indicate a spearator should precede an action */
  public static String SEPARATOR_PRECEDING = "separator_preceding";
  public static String SEPARATOR_FOLLOWING = "separator_following";

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
  private boolean networkStatus = false;
  private boolean sslStatus = false;

  private Action[] fileMenuActions = null;
  private Action[] editMenuActions = null;
  private Action[] helpMenuActions = null;
  private Action[] containerToolbarActions = null;
  private Vector connectionRegistry = null;
  private boolean pluginsLoaded = false;
  private String sessionCookie = null;
  private Itis itis;  
  
  private boolean versionFlag = true;  //Java 1.3 or greater

  /**
   * Creates a new instance of Morpho
   */
  public Morpho(ConfigXML config)
  {
    this.config = config;
    this.profile = null;
    checkJavaVersion();

    // Create the connection registry
    connectionRegistry = new Vector();

    // Get the configuration file information needed by the framework
    loadConfigurationParameters();

    // NOTE: current test for SSL connection is to determine whether metacat_url 
    // is set to be "https://..." in the config.xml file.  This check happens 
    // only ONCE on start-up, so if Morpho is ever revised to allow users to 
    // change metacat urls whilst it is running, we need to revise this to check 
    // more often. 05/20/02- Currently, SSL is not used, so will always be false
    sslStatus = ( metacatURL.indexOf("https://") == 0 );
    //metacatURL is iniatilized in the loadConfigurationParameters() call, above

    //create URL object to poll for metacat connectivity
    try {
      metacatPingURL = new URL(metacatURL);
    } catch (MalformedURLException mfue){
      debug(5, "unable to read or resolve Metacat URL");
    }
    
    /*
    //detects whether metacat is available, and if so, sets networkStatus = true
    // Boolean "true" tells doPing() method this is startup, so we don't get 
    // "No such service registered." exception from getServiceProvider()
    doPing(true);  
    updateStatusBar();

    //start a Timer to check periodically whether metacat remains available
    //over the network...
    Timer timer = new Timer(METACAT_PING_INTERVAL, pingActionListener);
    timer.setRepeats(true);
    timer.start();
    */
    
    // Set up the framework's menus and toolbars, and services
    initializeActions();
  }

  
  /**
   * Load all of the plugins specified in the configuration file. The plugins
   * are classes that implement the PluginInterface interface.
   */
  private void loadPlugins()
  {
    /*
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
      Vector sortedmenus = sortValues(menuOrder);
      Enumeration qqq = sortedmenus.elements();
      while (qqq.hasMoreElements()) {
        JMenu currentMenu = (JMenu)qqq.nextElement();
        morphoMenuBar.add(currentMenu);
      }
      
      pluginsLoaded = true;
    }
    catch(ClassCastException cce)
    {
      debug(5, "Error loading plugin: wrong class!");
    }
    */
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() {
    /*
    // FILE MENU ACTIONS
    fileMenuActions = new Action[4];

    Action exitItemAction = new AbstractAction("Exit") {
      public void actionPerformed(ActionEvent e) {
        exitApplication();
      }
    };
    exitItemAction.putValue(Action.ACCELERATOR_KEY, 
                            KeyStroke.getKeyStroke("control Q"));
    exitItemAction.putValue(Action.SHORT_DESCRIPTION, "Exit Morpho");
    exitItemAction.putValue(Action.DEFAULT, SEPARATOR_PRECEDING);
    exitItemAction.putValue("menuPosition", new Integer(-1));
    fileMenuActions[0] = exitItemAction;

    Action connectItemAction = new AbstractAction("Login...") {
      public void actionPerformed(ActionEvent e) {
        establishConnection();
      }
    };
    connectItemAction.putValue(Action.SHORT_DESCRIPTION, "Login");
    connectItemAction.putValue("menuPosition", new Integer(0));
    connectItemAction.putValue(Action.DEFAULT, SEPARATOR_PRECEDING);
    fileMenuActions[1] = connectItemAction;

    Action profileItemAction = new AbstractAction("New profile...") {
      public void actionPerformed(ActionEvent e) {
        createNewProfile();
      }
    };
    profileItemAction.putValue(Action.SHORT_DESCRIPTION, "New Profile");
    profileItemAction.putValue("menuPosition", new Integer(1));
    fileMenuActions[2] = profileItemAction;

    Action switchItemAction = new AbstractAction("Switch profile...") {
      public void actionPerformed(ActionEvent e) {
        switchProfile();
      }
    };
    switchItemAction.putValue(Action.SHORT_DESCRIPTION, "Switch Profile");
    switchItemAction.putValue("menuPosition", new Integer(2));
    fileMenuActions[3] = switchItemAction;

    addMenu("File", new Integer(1), fileMenuActions);

    // EDIT MENU ACTIONS
    editMenuActions = new Action[4];
    Action cutItemAction = new AbstractAction("Cut") {
      public void actionPerformed(ActionEvent e) {
        debug(9, "Cut is not yet implemented.");
      }
    };
    cutItemAction.putValue(Action.ACCELERATOR_KEY, 
                            KeyStroke.getKeyStroke("control X"));
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
        debug(9, "Copy is not yet implemented.");
      }
    };
    copyItemAction.putValue(Action.ACCELERATOR_KEY, 
                            KeyStroke.getKeyStroke("control C"));
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
        debug(9, "Paste is not yet implemented.");
      }
    };
    pasteItemAction.putValue(Action.ACCELERATOR_KEY, 
                            KeyStroke.getKeyStroke("control P"));
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
        debug(9, "Preferences dialog not yet implemented!");
      }
    };
    prefsItemAction.putValue(Action.SHORT_DESCRIPTION, 
                  "Open the Preferences dialog.");
    prefsItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
    prefsItemAction.putValue(Action.DEFAULT, SEPARATOR_PRECEDING);
    prefsItemAction.putValue("menuPosition", new Integer(5));
    prefsItemAction.setEnabled(false);
    editMenuActions[3] = prefsItemAction;

    addMenu("Edit", new Integer(2), editMenuActions);

    addMenu("Window", new Integer(6));

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
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/About16.gif")));
    aboutItemAction.putValue("menuPosition", new Integer(1));
    
    Action helpItemAction = new AbstractAction("Help...") {
      public void actionPerformed(ActionEvent e) {
        HTMLBrowser hb = new HTMLBrowser();
        hb.setVisible(true);
 //       SplashFrame sf = new SplashFrame();
 //       sf.setVisible(true);
      }
    };
    helpItemAction.putValue(Action.SHORT_DESCRIPTION, "Morpho Help");
    helpItemAction.putValue(Action.SMALL_ICON, 
                    new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/general/Help16.gif")));
    helpItemAction.putValue("menuPosition", new Integer(2));
 
    helpMenuActions[0] = aboutItemAction;
    helpMenuActions[1] = helpItemAction;
    addMenu("Help", new Integer(9), helpMenuActions);

    // Set up the toolbar for the application
    containerToolbarActions = new Action[3];
    containerToolbarActions[0] = cutItemAction;
    containerToolbarActions[1] = copyItemAction;
    containerToolbarActions[2] = pasteItemAction;
    addToolbarActions(containerToolbarActions);
    */
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
      
      // Really need to check for dirty
      // documents and exit quickly if nothing is to be done.

      logOutExit();
      config.save();
      System.exit(0);                // close the application
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
    if (networkStatus) {
      ConnectionFrame cf = new ConnectionFrame(this);
      cf.setVisible(true);
    } else {
      profile.set("searchmetacat", 0, "false");
      debug(6, "No MetaCat connection available - can't log in");
    }
  }

  /** 
   * Create a new profile
   */
  private void createNewProfile()
  {
    ProfileDialog dialog = new ProfileDialog(this);
    dialog.setVisible(true);
  }

  /** 
   * Switch profiles (from one existing profile to another)
   */
  private void switchProfile()
  {
    logOut();
    String currentProfile = config.get("current_profile", 0);
    String profileDirName = config.getConfigDirectory() + File.separator + 
                                          config.get("profile_directory", 0);
    File profileDir = new File(profileDirName);
    String profilesList[] = null;
    int selection = 0;
    if (profileDir.isDirectory()) {

        // Get vector of profiles to be displayed
        profilesList = profileDir.list();
        for (selection=0; selection < profilesList.length; selection++) {
            if (currentProfile.equals(profilesList[selection])) {
                break;
            }
        }

        // Pop up a dialog with the choices
        String newProfile = (String)JOptionPane.showInputDialog(null,
                                "Select from existing profiles:", "Input",
                                JOptionPane.INFORMATION_MESSAGE, null,
                                profilesList, profilesList[selection]);

        // Set the new profile to the one selected if it is different
        if (null != newProfile) {
            if (currentProfile.equals(newProfile)) {
                Morpho.debug(9, "No change in profile.");
            } else {
                setProfile(newProfile);
                Morpho.debug(9, "New profile is: " + newProfile);
            }
        }
    } else {
        // This is an error
        Morpho.debug(3, "Error: Can not switch profiles.\n " +
                "profile_directory is not a directory.");
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
   * attempts to connect a socket, returns null if it is not successful
   * returns the connected socket if it is successful.
   */
  private static Socket getSocket(String host, int port)
  {
    Socket s = null;
    try
    {
      s = new Socket(host, port);
      //we could create a socket on this port so the port is not available
      //System.out.println("socket connnected");
      return s;
    }
    catch(UnknownHostException u)
    {
      System.out.println("unknown host in DataFileUploadInterface.getSocket");
    }
    catch(IOException i)
    {
      //an ioexception is thrown if the port is not in use
      //System.out.println("socket not connected");
      return s;
    }
    return s;
  }
  
  /**
   * sends a data file to the metacat using "multipart/form-data" encoding
   *
   * @param id the id to assign to the file on metacat (e.g., knb.1.1)
   * @param file the file to send
   * @return the response stream from metacat
   */
  public InputStream sendDataFile(String id, File file) 
  {
    String retmsg = "";
    InputStream returnStream = null;

    if (!connected) {
      // Ask the user to connect
      establishConnection();
    }

    // Now contact metacat and send the request
    try
    {
      //FileInputStream data = new FileInputStream(file);

      Morpho.debug(20, "Sending data to: |" + metacatURL + "|");
      URL url = new URL(metacatURL.trim());
      HttpMessage msg = new HttpMessage(url);
      Properties args = new Properties();
      args.put("action", "upload");
      args.put("docid", id);

      Properties dataStreams = new Properties();
      String filename = file.getAbsolutePath();
      Morpho.debug(20, "Sending data file: " + filename);
      dataStreams.put("datafile", filename);
      
      /*
       * Note:  The reason that there are three try statements all executing
       * the same code is that there is a problem with the initial connection
       * using the HTTPClient protocol handler.  These try statements make sure
       * that a connection is made because it gives each connection a 2nd and
       * 3rd chance to work before throwing an error.  
       * THIS IS A TOTAL HACK.  THIS NEEDS TO BE LOOKED INTO AFTER THE BETA1
       * RELEASE OF MORPHO!!!  cwb (7/24/01)
       */
      try
      {
        returnStream = msg.sendPostData(args, dataStreams);
      }
      catch(Exception ee)
      {
        try
        {
          returnStream = msg.sendPostData(args, dataStreams);
        }
        catch(Exception eee)
        {
          try
          {
            returnStream = msg.sendPostData(args, dataStreams);
          }
          catch(Exception eeee)
          {
            throw new Exception(eeee.getMessage());
          }
        }
      }
    } catch(Exception e) {
      Morpho.debug(1, "Fatal error sending binary data to Metacat: " + 
                            e.getMessage());
      e.printStackTrace(System.err);
    }
    return returnStream;
  }
  
  public String getSessionCookie()
  {
    return sessionCookie;
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
    
    /*
     * Note:  The reason that there are three try statements all executing
     * the same code is that there is a problem with the initial connection
     * using the HTTPClient protocol handler.  These try statements make sure
     * that a connection is made because it gives each connection a 2nd and
     * 3rd chance to work before throwing an error.  
     * THIS IS A TOTAL HACK.  THIS NEEDS TO BE LOOKED INTO AFTER THE BETA1
     * RELEASE OF MORPHO!!!  cwb (7/24/01)
     */
    try
    {
      debug(20, "Sending data to: " + metacatURL);
      URL url = new URL(metacatURL);
      HttpMessage msg = new HttpMessage(url);
      returnStream = msg.sendPostMessage(prop);
      sessionCookie = msg.getCookie();
      return returnStream;
    }
    catch(Exception e)
    {
      try
      {
        debug(20, "Sending data (again) to : " + metacatURL);
        URL url = new URL(metacatURL);
        HttpMessage msg = new HttpMessage(url);
        returnStream = msg.sendPostMessage(prop);
        sessionCookie = msg.getCookie();
        return returnStream;
      }
      catch(Exception e2)
      {
        try
        {
          debug(20, "Sending data (again)(again) to: " + metacatURL);
          URL url = new URL(metacatURL);
          HttpMessage msg = new HttpMessage(url);
          returnStream = msg.sendPostMessage(prop);
          sessionCookie = msg.getCookie();
          return returnStream;
        }
        catch(Exception e3)
        {
          debug(1, "Fatal error sending data to Metacat: " + e3.getMessage());
          e.printStackTrace(System.err);
        }
      }
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
    debug(20, "Logging in using uid: " + userName);
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
      updateStatusBar();
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
      passWord = "none";   // get rid of existing password info
      Properties prop = new Properties();
      prop.put("action", "logout");
      prop.put("qformat", "xml");
  
      String response = getMetacatString(prop);
      doLogoutCleanup();
    }
  }
  
  /**
   *  cleanup routine called by logout() and by MetacatPinger thread
   *  Keeps all this stuff in one place so as not repeat code
   */
  private void doLogoutCleanup() {
    HttpMessage.setCookie(null);
    connected = false;
    updateStatusBar();
    fireConnectionChangedEvent();
  }
  
  /**
   *  updates status bar in response to changes in connection parameters
   */
  private void updateStatusBar() {
    /*
    debug(19,"updateStatusBar() called; networkStatus = "+networkStatus);
    statusBar.setConnectStatus(networkStatus);
    statusBar.setLoginStatus  (connected && networkStatus);
    statusBar.setSSLStatus    (sslStatus);
    
    statusBar.setMessage(makeStatusBarMessage());
    
    statusBar.validate();
    */
  }
  
  
  private String makeStatusBarMessage() {
    /*
    if (networkStatus) {
      if (connected)  return STATUSBAR_MSG_LOGGED_IN + metacatURL;
      else            return STATUSBAR_MSG_NET_OK_NOT_LOGGED_IN + metacatURL;
    }
    return STATUSBAR_MSG_NO_NET_NOT_LOGGED_IN + metacatURL;
    */ return null;
  }

  
  /**
   *  allows other classes to determine whether network connection to metacat is 
   *  available before trying to contact it, since this would cause an error
   */
  public boolean isMetacatAvailable() 
  { 
    return networkStatus; 
  }
  
  
  /**
   * Log out of metacat when exiting
   */
  public void logOutExit()
  {
    if (connected) {
      passWord = "none";   // get rid of existing password info
      Properties prop = new Properties();
      prop.put("action", "logout");
      prop.put("qformat", "xml");
  
      String response = getMetacatString(prop);
      HttpMessage.setCookie(null);
      connected = false;

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
   *  get password associated with this framework
   */
   public String getPassword() {
      return passWord;
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
      debug(20, "Adding listener: " + listener.toString());
      connectionRegistry.addElement(listener);
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
                         (ConnectionListener)connectionRegistry.elementAt(i);
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
                         (ConnectionListener)connectionRegistry.elementAt(i);
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
   * @param newProfile the profile object
   */
  public void setProfile(ConfigXML newProfile)
  {
    this.profile = newProfile;

    // Load basic profile information
    String profilename = profile.get("profilename", 0);
    String scope = profile.get("scope", 0);
    String dn = profile.get("dn", 0);
    //setUserName(username);
    debug(20, "Setting username to dn: " + dn);
    setUserName(dn);

    if (! config.set("current_profile", 0, profilename)) {
      boolean success = config.insert("current_profile", profilename);
    }
    config.save();
    establishConnection();
    setLastID(scope);
    fireConnectionChangedEvent();
  }

  /**
   * Set the profile associated with this framework based on its name
   *
   * @param newProfileName the name of the new profile for the framework
   */
  public void setProfile(String newProfileName)
  {
    String profileDir = config.getConfigDirectory() + File.separator +
                                 config.get("profile_directory", 0);
    String currentProfile = config.get("current_profile", 0);
    if (!newProfileName.equals(currentProfile)) {
      String newProfilePath = profileDir + File.separator + newProfileName + 
                              File.separator + newProfileName + ".xml";
      try {
        ConfigXML newProfile = new ConfigXML(newProfilePath);
        setProfile(newProfile);
      } catch (FileNotFoundException fnf) {
        Morpho.debug(5, "Profile not found!");
      }
    }
  } 

  /**
   * Look up the synonyms of a taxon from ITIS, and return the list of names
   *
   * @param taxonName
   * @return vector of the names of synonym taxa
   */
  public Vector getTaxonSynonyms(String taxonName)
  {
    // Initialize the itis query system the first time through
    if (itis == null) {
      itis = new Itis();
    }

    Vector synonymList = new Vector();

    // Look up the name we were passed
    Morpho.debug(20, "Searching ITIS for synonyms of: " + taxonName);
    try {
      long newTsn = itis.findTaxonTsn(taxonName);
      if (newTsn > 0) {
        try {
          Vector synonyms = itis.getSynonymTsnList(newTsn);
          for (int i=0; i < synonyms.size(); i++) {
            long synonymTsn = ((Long)synonyms.elementAt(i)).longValue();
            Taxon synonymTaxon = itis.getTaxon(synonymTsn);
            synonymList.addElement(synonymTaxon.getScientificName());
          }
        } catch (ItisException ie) {
          Morpho.debug(20, "Problem with ITIS lookup for: " + taxonName);
        }
      }
    } catch (ItisException iesearch) {
      Morpho.debug(20, "Taxon not found in ITIS: " + taxonName);
    }
    return synonymList;
  }

  /**
   * Print debugging messages based on severity level, where severity level 1
   * are the most critical and higher numbers are more trivial messages.
   * Messages with severity 1 to 4 will result in an error dialog box for the
   * user to inspect.  Those with severity 5-9 result in a warning dialog
   * box for the user to inspect.  Those with severity greater than 9 are
   * printed only to standard error.
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
        // Show a dialog for severe errors
        if (severity < 5) {
          JOptionPane.showMessageDialog(null, message, "Error!",
                                        JOptionPane.ERROR_MESSAGE);
        } else if (severity < 10) {
          JOptionPane.showMessageDialog(null, message, "Warning!",
                                        JOptionPane.WARNING_MESSAGE);
        }

        // Everything gets printed to standard error
        System.err.println(message);
      }
    }
  } 

  /**
   * Load configuration parameters from the config file as needed
   */
  private void loadConfigurationParameters()
  {
    metacatURL = config.get("metacat_url", 0);
    String temp_uname = config.get("username", 0);
    userName = (temp_uname != null) ? temp_uname : "public";
    debug_level = (new Integer(config.get("debug_level", 0))).intValue();
    debug(20, "Debug_level set to: " + debug_level);
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
     // Set system property to use HTTPClient or ssl protocol
     // System.setProperty("java.protocol.handler.pkgs","HTTPClient");
     
     java.net.URL.setURLStreamHandlerFactory(new java.net.URLStreamHandlerFactory() {
         public java.net.URLStreamHandler createURLStreamHandler(final String protocol) {
         if ("http".equals(protocol)) {
           try { 
             URLStreamHandler urlsh = new HTTPClient.http.Handler(); 
              return urlsh; 
           }
           catch (Exception e) {
             System.out.println("Error setting URL StreamHandler!");
             return null;
           }           
         }
         return null;
         }
     });
   
 
      // Set the keystore used
      System.setProperty("javax.net.ssl.trustStore", "./lib/morphocacerts");


      // add provider for SSL support
      java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
      
      // Make sure the config directory exists
      File configurationFile = null;
      File configDir = null;
      configDir = new File(ConfigXML.getConfigDirectory());
      if (!configDir.exists()) {
        if (!configDir.mkdir()) {
          Morpho.debug(1, "Failed to create config directory");
          System.exit(0);
        }
      }
     
      // Make sure the config file has been copied to the proper directory
      try {
        configurationFile = new File(configDir, configFile);
        if (configurationFile.createNewFile() || configurationFile.length() == 0) {
          FileOutputStream out = new FileOutputStream(configurationFile);
          ClassLoader cl = Thread.currentThread().getContextClassLoader();
          InputStream configInput = cl.getResourceAsStream(configFile);
          if (configInput == null) {
            Morpho.debug(1, "Could not find default configuration file.");
            System.exit(0);
          }
          byte buf[] = new byte[4096];
          int len = 0;
          while ((len = configInput.read(buf, 0, 4096)) != -1) {
            out.write(buf, 0, len);
          }
          configInput.close();
          out.close();
        }
      } catch (IOException ioe) {
        Morpho.debug(1, "Error copying config: " + ioe.getMessage());
        Morpho.debug(1, ioe.getClass().getName());
        ioe.printStackTrace(System.err);
        System.exit(0);
      }
      
      // Open the configuration file
      //ConfigXML config = new ConfigXML(configFile);
      ConfigXML config = new ConfigXML(configurationFile.getAbsolutePath());
      
      // Create a new instance of our application
      Morpho clf = new Morpho(config);

      // Set the version number
      VERSION = config.get("version", 0);
      
      // set to the Look and Feel of the native system.
      setLookAndFeel(config.get("lookAndFeel",0)); 

      // Show the Splash frame
      SplashFrame sf = new SplashFrame(true);
      sf.setVisible(true);

      // Check expiration date
      Date expiration = new Date(102, 5, 1);
      Date warning = new Date(102, 4, 15);
      Date now = new Date();
 //     if (now.after(expiration))  // removed for release version
      if (false) {
        clf.debug(1, "This version of Morpho has expired! " +
           "See http://knb.ecoinformatics.org/ for a newer version.");
           JOptionPane.showMessageDialog(null,
           "This version of Morpho has expired!\n" +
           "See http://knb.ecoinformatics.org/ for a newer version.");
           System.exit(1);
      } else {
 //       if (now.after(warning))
        if (false) {
          clf.debug(1, "This version of Morpho will expire on " +
            "April 1, 2002. See http://knb.ecoinformatics.org/ for a " +
            "newer version.");
          JOptionPane.showMessageDialog(null,
            "This version of Morpho will expire on April 1, 2002.\n" +
            "See http://knb.ecoinformatics.org/ for a newer version.");
        }

        // Load the current profile and log in
        String profileDir = ConfigXML.getConfigDirectory() + File.separator +
                                     config.get("profile_directory", 0);
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
          PrintStream errPrintStream = new PrintStream(err);
          System.setErr(errPrintStream);
          System.setOut(errPrintStream);
        }

        // Load all of the plugins, their menus, and toolbars
        clf.loadPlugins();

        // make the Morpho visible.
        sf.dispose();
        
        debug(1, "Finished main, what to do now?");
        System.exit(0);
      }
    } catch(Throwable t) {
      t.printStackTrace();
      //Ensure the application exits with an error condition.
      System.exit(1);
    }
  }
  
  private String getLastID(String scope) {
    String result = null;
    Properties lastIDProp = new Properties();
    lastIDProp.put("action","getlastdocid");
    lastIDProp.put("scope",scope);
    String temp = getMetacatString(lastIDProp);
    /*
      if successful temp should be of the form
      <?xml version="1.0"?>
      <lastDocid>
        <scope>fegraus</scope>
        <docid>fegraus.53.1</docid>
      </lastDocid>
    */
    if (temp!=null) {
      int ind1 = temp.indexOf("<docid>");
      int ind2 = temp.indexOf("</docid>");
      if ((ind1>0)&&(ind2>0)) {
        result = temp.substring(ind1+7, ind2);
        if(!result.equals("null"))
        {
          // now remove the version and header parts of the id
          result = result.substring(0,result.lastIndexOf("."));
          result = result.substring(result.indexOf(".")+1,result.length());
        }
        else
        {
          result = null;
        }
      }
    }
    return result;
  }
  
  private void setLastID(String scope) {
    //MB 05-21-02: if (connected && networkStatus) {
    if (networkStatus) {   // only execute if connected to avoid hanging when there is no network connection
      String id = getLastID(scope);
      if (id!=null) {
        int num = (new Integer(id)).intValue();
        String curval = profile.get("lastId", 0);
        int curnum = (new Integer(curval)).intValue();
        if (curnum<num) {
          num = num + 1;  // required because Metacat does not return the latest id
          id = (new Integer(num)).toString();
          profile.set("lastId",0,id);
          profile.save();
        }
      }
    }
  }
 
// takes a hashtable where the key is an Integer and returns a Vector of hashtable 
// values sorted by key values
// this is a quick hack!!! DFH
  private Vector sortValues(Hashtable hash) {
    // assume that there are no more that 20 values in the hash
    // and return only the first 20 (i.e. 0 - 19
    Vector sorted = new Vector();
    for (int i=0;i<20;i++) {
        Integer iii = new Integer(i);
        Enumeration www = hash.keys();
        while (www.hasMoreElements()) {
            Object thiskey = www.nextElement();
            if (iii.equals(thiskey))  {
                sorted.addElement(hash.get(thiskey));  
            }
        }
    }
   return sorted;   
  }

 public void checkJavaVersion() {
      String ver = System.getProperty("java.version");
      int pos1 = ver.indexOf(".");
      int pos2 = ver.indexOf(".",pos1+1);
      String ver0 = ver.substring(0,pos1);
      String ver1 = ver.substring(pos1+1,pos2);
      int iver0 = (new Integer(ver0)).intValue();
      int iver1 = (new Integer(ver1)).intValue();
      if ((iver0==1)&&(iver1<3)) {
        versionFlag = false;
        JOptionPane.showMessageDialog(null,
           "Version "+ver+" of the Java Virtual Machine(JVM) is currently in use.\n" +
           "Although most of Morpho will operate using early versions of the JVM,\n"+
           "Version 1.3 or greater is required for all functions to work properly!");  
      }  
 }
 
 /**
  * returns true if the JVM version is 1.3 or greater
  */
 public boolean getJavaVersionFlag() {
    return this.versionFlag;   
 }
  
  /**
   * Set up a SAX parser for reading an XML document
   *
   * @param contentHandler object to be used for parsing the content
   * @param errorHandler object to be used for handling errors
   * @return a SAX XMLReader object for parsing
   */
  public static XMLReader createSaxParser(ContentHandler contentHandler, 
          ErrorHandler errorHandler) 
  {
    XMLReader parser = null;

    // Set up the SAX document handlers for parsing
    try {

      // Get an instance of the parser
      SAXParserFactory spfactory = SAXParserFactory.newInstance();
      SAXParser saxp = spfactory.newSAXParser();
      parser = saxp.getXMLReader();

      if (parser != null) {
          parser.setFeature("http://xml.org/sax/features/namespaces", true);
          Morpho.debug(30, "Parser created is: " +
                  parser.getClass().getName());
      } else {
          Morpho.debug(9, "Unable to create SAX parser!");
      }

      // Set the ContentHandler to the provided object
      if (null != contentHandler) {
        parser.setContentHandler(contentHandler);
      } else {
        Morpho.debug(3, 
                "No content handler for SAX parser!");
      }

      // Set the error Handler to the provided object
      if (null != errorHandler) {
        parser.setErrorHandler(errorHandler);
      }

    } catch (Exception e) {
       Morpho.debug(1, "Failed to create SAX parser:\n" + 
               e.toString());
    }

    return parser;
  }

  /**
   * Set up a DOM parser for reading an XML document
   *
   * @return a DOM parser object for parsing
   */
  public static DocumentBuilder createDomParser() 
  {
    DocumentBuilder parser = null;

    try {
        //ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ClassLoader cl = Morpho.class.getClassLoader();
        Morpho.debug(30, "Current ClassLoader is: " +
                cl.getClass().getName());
        Thread t = Thread.currentThread();
        t.setContextClassLoader(cl);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newDocumentBuilder();
        if (parser != null) {
            Morpho.debug(30, "Parser created is: " +
                    parser.getClass().getName());
        } else {
            Morpho.debug(9, "Unable to create DOM parser!");
        }
    } catch (ParserConfigurationException pce) {
            Morpho.debug(9, "Exception while creating DOM parser!");
            Morpho.debug(10, pce.getMessage());
    }

    return parser;
  }
  
  /**
   *  This ActionListener is notified by the swing.Timer every 
   *  METACAT_PING_INTERVAL milliSeconds, upon which it tries to contact the 
   *  Metacat defined by "metacatURL"
   */
  ActionListener pingActionListener = new ActionListener() {
    public void actionPerformed(ActionEvent e){ doPing(); }
  };


  /**
   *  sets networkStatus to boolean true if metacat connection can be made
   *  @param isStartUp - set to boolean "true" when calling for first time, so 
   *  we don't get "No such service registered." exception from 
   *  getServiceProvider()
   *  
   */
  private URL           metacatPingURL    = null;
  private URLConnection urlConn           = null;
  private boolean       origNetworkStatus = false;

  //overload to give default functionality; boolean flag needed only at startup
  private void doPing() { doPing(false); }
  
  private void doPing(final boolean isStartUp) {

    final SwingWorker sbUpdater = new SwingWorker() {

      public Object construct() {
        //check if metacat can be reached:
        origNetworkStatus = networkStatus;
        try {
          urlConn = metacatPingURL.openConnection();
          urlConn.connect();
          networkStatus = (urlConn.getDate() > 0L);
        } catch (IOException ioe) {
          debug(19, " - unable to open network connection to Metacat");
          networkStatus = false;
          if (profile!=null) profile.set("searchmetacat", 0, "false");
        }
        return null; //return value not used by this program
      }

      //Runs on the event-dispatching thread.
      public void finished() {
        debug(21,"doPing() called - network available?? - "+networkStatus);
        if (origNetworkStatus != networkStatus) {
          //if lost connection, can't log out, but can still do cleanup
          if (!networkStatus) {
            profile.set("searchmetacat", 0, "false");
            doLogoutCleanup();
          } else {
            if (!isStartUp) {
              //update package list
              /*
              try {
                ServiceProvider provider 
                              = getServiceProvider(QueryRefreshInterface.class);
                ((QueryRefreshInterface)provider).refresh();
              } catch (ServiceNotHandledException snhe) {
                debug(6, snhe.getMessage());
              }
              */
            }
            //update status bar
            updateStatusBar();
          }
        }
      }
    };
    sbUpdater.start(); 
  }
  
  /**
   *   set look & feel to system default
   **/
  private static void setLookAndFeel(String lnf){
    try {
      if (lnf!=null) {
        if (lnf.equalsIgnoreCase("kunststoff")) {
          debug(19,"kunststoff - loading");
          try{
            Class classDefinition = Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
            LookAndFeel test = (LookAndFeel)classDefinition.newInstance();
            UIManager.setLookAndFeel(test);
          }
          catch (ClassNotFoundException www) {
            debug(19,"couldn't set L&F to kunststoff - using Java default");
            return;
          }
        }
        else if (lnf.equalsIgnoreCase("metal")) {          
          UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        else if (lnf.equalsIgnoreCase("windows")) {          
          UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        else if (lnf.equalsIgnoreCase("motif")) {          
          UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        }
        else {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
      }
    else {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    } catch (Exception e) { 
      debug(19,"couldn't set L&F to native - using Java default"); 
    }
  }
}
