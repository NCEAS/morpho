/**
 *  '$RCSfile: Morpho.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-05-09 03:01:25 $'
 * '$Revision: 1.53 $'
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

import com.sun.net.ssl.internal.ssl.*;

import edu.ucsb.nceas.itis.Itis;
import edu.ucsb.nceas.itis.ItisException;
import edu.ucsb.nceas.itis.Taxon;
import edu.ucsb.nceas.morpho.framework.*;

import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoPrefsDialog;
import edu.ucsb.nceas.morpho.framework.ConnectionListener;
import edu.ucsb.nceas.morpho.framework.HTMLBrowser;
import edu.ucsb.nceas.morpho.framework.HttpMessage;
import edu.ucsb.nceas.morpho.framework.InitialScreen;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.ProfileDialog;
import edu.ucsb.nceas.morpho.framework.SplashFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.UISettings;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.ClassCastException;
import java.lang.reflect.*;
import java.net.*;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLReader;

/**
 * Morpho is the main entry point for the Morpho application. It creates the
 * main application state and sets up the menus and toolbars for the
 * application. The framework also provides a mechanism for "plugins" to add
 * menus, toolbars, and services to the application. These plugins are
 * dynamically loaded at runtime. Plugins are classes that implement the
 * "PluginInterface" interface.
 *
 * @author   jones
 */
public class Morpho
{
    /** The version of this release of Morpho */
    public static String VERSION = "1.4.0";

    /** Constant to indicate a separator should precede an action */
    public static String SEPARATOR_PRECEDING = "separator_preceding";
    /** Constant to indicate a separator should follow an action */
    public static String SEPARATOR_FOLLOWING = "separator_following";
    /** Constant of initial morpho frame name */
    public static final String INITIALFRAMENAME = "Morpho";

    /** if windows, need to increase widthof JSplitPane divider, 
        otherwise max/min arrows don't render properly */
    private static final Integer DIVIDER_THICKNESS_FOR_MSWINDOWS=new Integer(8);
    
    // redirects standard out and err streams
    static boolean log_file = false;

    private String userName = "public";
    private String passWord = "none";
    private String metacatURL = null;
    private static ConfigXML config;
    private static ConfigXML profileConfig;
    private ConfigXML profile;
    private static boolean connected = false;
    private boolean networkStatus = false;
    private boolean sslStatus = false;

    //private Action[] fileMenuActions = null;
    //private Action[] editMenuActions = null;
    //private Action[] helpMenuActions = null;
    //private Action[] containerToolbarActions = null;
    private Vector connectionRegistry = null;
    private static final List profileAddedListenerList = new ArrayList();
    private static MorphoFrame initialFrame;
    private boolean pluginsLoaded = false;
    private String sessionCookie = null;
    private Itis itis;

    private boolean versionFlag = true;

    private URL metacatPingURL = null;
    private URLConnection urlConn = null;
    private boolean origNetworkStatus = false;
    /**
     * The polling interval, in milliSeconds, between attempts to verify that
     * MetaCat is available over the network
     */
    private final static int METACAT_PING_INTERVAL = 30000;

    /** The hardcoded XML configuration file */
    private static String configFile = "config.xml";
    private static String profileFileName = "currentprofile.xml";
    private static boolean debug = true;
    private static int debug_level = 9;
    private static Morpho thisStaticInstance;
    /** flag set to indicate that connection to metacat is busy
     *  used by doPing to avoid thread problem
     */
    public static boolean connectionBusy = false;

    /**
     * Creates a new instance of Morpho
     *
     * @param config  the configuration object for the application
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

        // NOTE: current test for SSL connection is to determine whether 
        // metacat_url is set to be "https://..." in the config.xml file.  
        // This check happens only ONCE on start-up, so if Morpho is ever 
        // revised to allow users to change metacat urls whilst it is running, 
        // we need to revise this to check more often. 
        // 05/20/02- Currently, SSL is not used, so will always be false
        sslStatus = (metacatURL.indexOf("https://") == 0);

        //create URL object to poll for metacat connectivity
        try {
            metacatPingURL = new URL(metacatURL);
        } catch (MalformedURLException mfue) {
            Log.debug(5, "unable to read or resolve Metacat URL");
        }

        // detects whether metacat is available, and if so, sets
        // networkStatus = true
        // Boolean "true" tells doPing() method this is startup, so we don't get
        // "No such service registered." exception from getServiceProvider()
        startPing();
        finishPing(true);

        //start a Timer to check periodically whether metacat remains available
        //over the network...
        Timer timer = new Timer(METACAT_PING_INTERVAL, pingActionListener);
        timer.setRepeats(true);
        timer.start();
    }

    /**
     * Set the username associated with this framework
     *
     * @param uname  The new UserName value
     */
    public void setUserName(String uname)
    {
        if (!userName.equals(uname)) {
            this.userName = uname;
            fireUsernameChangedEvent();
        }
    }

    /**
     * Set the password associated with this framework
     *
     * @param pword  The new Password value
     */
    public void setPassword(String pword)
    {
        this.passWord = pword;
    }

    /**
     * Set the profile for the currently logged in user (on startup, or when
     * switching profiles).  Pops up a Login dialog after profile is set
     *
     * @param newProfile  the profile object
     */
    public void setProfile(ConfigXML newProfile)
    {
        setProfileDontLogin(newProfile, false);
        
        if (initialFrame==null) {
            establishConnection();
        } else if(!initialFrame.isShowing()) {
            establishConnection();
        }
        fireConnectionChangedEvent();
    }

    /**
     *  Set the profile for the currently logged in user, but does not popup a 
     *  login dialog
     *
     *  @param newProfile  the profile object
     */
    public void setProfileDontLogin(ConfigXML newProfile) 
    {
        setProfileDontLogin(newProfile, true);
    }

    //
    //  Set the profile for the currently logged in user, but does not popup a 
    //  login dialog
    //
    //  @param newProfile  the profile object
    //
    //  @param doFireConnectionChangedEvent boolean flag to tell method whether 
    //                          to do a <code>fireConnectionChangedEvent</code>;
    //                          mainly used by calls from the above 
    //                          "setProfile(ConfigXML newProfile)" method, which 
    //                          already does its own fireConnectionChangedEvent, 
    //                          so needs to disable that call here.
    //
    private void setProfileDontLogin(ConfigXML newProfile, 
                                    boolean doFireConnectionChangedEvent)
    {
        this.profile = newProfile;

        // Load basic profile information
        String profilename = profile.get("profilename", 0);
        String scope = profile.get("scope", 0);
        String dn = profile.get("dn", 0);
        //setUserName(username);
        Log.debug(20, "Setting username to dn: " + dn);
        setUserName(dn);

        if (!profileConfig.set("current_profile", 0, profilename)) {
            boolean success = profileConfig.insert("current_profile",
                    profilename);
        }
        profileConfig.save();
        setLastID(scope);
        
        if (doFireConnectionChangedEvent) fireConnectionChangedEvent();
    }
    
    /**
     * Set the profile associated with this framework based on its name
     *
     * @param newProfileName  the name of the new profile for the framework
     */
    public void setProfile(String newProfileName)
    {
        setProfile(newProfileName, true);
    }
    
    /**
     * Set the profile associated with this framework based on its name, but 
     * does not popup a login dialog
     *
     * @param newProfileName  the name of the new profile for the framework
     */
    public void setProfileDontLogin(String newProfileName)
    {
        setProfile(newProfileName, false);
    }
    
    // Set the profile associated with this framework based on its name, and 
    // either pops up a login dialog or does not, depending on "doLogin" flag
    private void setProfile(String newProfileName, boolean doLogin)
    {
        String profileDir = config.getConfigDirectory() + File.separator +
                config.get("profile_directory", 0);
        String currentProfile = getCurrentProfileName();
        if (!newProfileName.equals(currentProfile)) {
            String newProfilePath = profileDir + File.separator + 
                newProfileName + File.separator + newProfileName + ".xml";
            try {
                ConfigXML newProfile = new ConfigXML(newProfilePath);
                if (doLogin) setProfile(newProfile);
                else setProfileDontLogin(newProfile);
            } catch (FileNotFoundException fnf) {
                Log.debug(5, "Profile not found!");
            }
        }
    }

    /**
     * Send a request to Metacat
     *
     * @param prop           the properties to be sent to Metacat
     * @param requiresLogin  indicates whether a valid connection is required
     *                       for the operation
     * @return               InputStream as returned by Metacat
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
     * Gets the SessionCookie attribute of the Morpho object
     *
     * @return   The SessionCookie value
     */
    public String getSessionCookie()
    {
        return sessionCookie;
    }

    /**
     * Send a request to Metacat
     *
     * @param prop  the properties to be sent to Metacat
     * @return      InputStream as returned by Metacat
     */
    synchronized public InputStream getMetacatInputStream(Properties prop)
    {   connectionBusy = true;
        InputStream returnStream = null;
        // Now contact metacat and send the request

        /*
            Note:  The reason that there are three try statements all executing
            the same code is that there is a problem with the initial connection
            using the HTTPClient protocol handler.  These try statements make 
            sure that a connection is made because it gives each connection a 
            2nd and 3rd chance to work before throwing an error.
            THIS IS A TOTAL HACK.  THIS NEEDS TO BE LOOKED INTO AFTER THE BETA1
            RELEASE OF MORPHO!!!  cwb (7/24/01)
          */
        try {
            Log.debug(20, "Sending data to: " + metacatURL);
            URL url = new URL(metacatURL);
            HttpMessage msg = new HttpMessage(url);
            returnStream = msg.sendPostMessage(prop);
            sessionCookie = msg.getCookie();
           connectionBusy = false;
           return returnStream;
        } catch (Exception e) {
            try {
                Log.debug(20, "Sending data (again) to : " + metacatURL);
                URL url = new URL(metacatURL);
                HttpMessage msg = new HttpMessage(url);
                returnStream = msg.sendPostMessage(prop);
                sessionCookie = msg.getCookie();
                connectionBusy = false;
                return returnStream;
            } catch (Exception e2) {
                try {
                    Log.debug(20, "Sending data (again)(again) to: " + 
                        metacatURL);
                    URL url = new URL(metacatURL);
                    HttpMessage msg = new HttpMessage(url);
                    returnStream = msg.sendPostMessage(prop);
                    sessionCookie = msg.getCookie();
                    connectionBusy = false;
                    return returnStream;
                } catch (Exception e3) {
                    Log.debug(1, "Fatal error sending data to Metacat: " + 
                        e3.getMessage());
                    e.printStackTrace(System.err);
                }
            }
        }
        connectionBusy = false;
        return returnStream;
    }

    /**
     * Send a request to Metacat
     *
     * @param prop           the properties to be sent to Metacat
     * @param requiresLogin  indicates whether a valid connection is required
     *      for the operation
     * @return               a string as returned by Metacat
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
     * @param prop  the properties to be sent to Metacat
     * @return      a string as returned by Metacat
     */
    public String getMetacatString(Properties prop)
    {
        String response = null;

        // Now contact metacat and send the request
        try {
            InputStreamReader returnStream =
                    new InputStreamReader(getMetacatInputStream(prop));
            StringWriter sw = new StringWriter();
            int len;
            char[] characters = new char[512];
            while ((len = returnStream.read(characters, 0, 512)) != -1) {
                sw.write(characters, 0, len);
            }
            returnStream.close();
            response = sw.toString();
            sw.close();
        } catch (Exception e) {
            Log.debug(1, "Fatal error sending data to Metacat.");
        }
        return response;
    }

    /**
     * Get the username associated with this framework
     *
     * @return    The UserName value
     * @returns   String the username
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * get password associated with this framework
     *
     * @return   The Password value
     */
    public String getPassword()
    {
        return passWord;
    }

    /**
     * Determines if the framework has a valid login
     *
     * @return   boolean true if connected to Metacat, false otherwise
     */
    public static boolean isConnected()
    {
        return connected;
    }

    /**
     * Determines if the framework is using an ssl connection
     *
     * @return   boolean true if using SSL, false otherwise
     */
    public boolean getSslStatus()
    {
        return sslStatus;
    }

    /**
     * Determine whether a network connection is available before trying to open
     * a socket, since this would cause an error
     *
     * @return   boolean true if the network is reachable
     */
    public boolean getNetworkStatus()
    {
        return networkStatus;
    }

    /**
     * Get the configuration object associated with the framework. Plugins use
     * this object to get and set persistent configuration parameters.
     *
     * @return   ConfigXML the configuration object
     */
    public static ConfigXML getConfiguration()
    {
        if (config==null)  {
            try {
                initializeConfiguration();    
            } catch (FileNotFoundException fnfe) {
                Log.debug(5, "Configuration file not found!");
                fnfe.printStackTrace();
            }
        }
        return config;
    }

    /**
     * Get the profile ConfigXML for the currently logged in user.
     *
     * @returns   ConfigXML the profile object
     */
    public ConfigXML getProfile()
    {
        return profile;
    }

    /**
     * Get the profile name for the currently logged in user.
     *
     * @return  String representation of current profile name
     */
    public static String getCurrentProfileName()
    {
        return profileConfig.get("current_profile", 0);
    }
    
    /**
     * Look up the synonyms of a taxon from ITIS, and return the list of names
     *
     * @param taxonName
     * @return           vector of the names of synonym taxa
     */
    public Vector getTaxonSynonyms(String taxonName)
    {
        // Initialize the itis query system the first time through
        if (itis == null) {
            itis = new Itis();
        }

        Vector synonymList = new Vector();

        // Look up the name we were passed
        Log.debug(20, "Searching ITIS for synonyms of: " + taxonName);
        try {
            long newTsn = itis.findTaxonTsn(taxonName);
            if (newTsn > 0) {
                try {
                    Vector synonyms = itis.getSynonymTsnList(newTsn);
                    for (int i = 0; i < synonyms.size(); i++) {
                        long synonymTsn = 
                            ((Long)synonyms.elementAt(i)).longValue();
                        Taxon synonymTaxon = itis.getTaxon(synonymTsn);
                        synonymList.addElement(
                            synonymTaxon.getScientificName());
                    }
                } catch (ItisException ie) {
                    Log.debug(20, "Problem with ITIS lookup for: " + taxonName);
                }
            }
        } catch (ItisException iesearch) {
            Log.debug(20, "Taxon not found in ITIS: " + taxonName);
        }
        return synonymList;
    }

    /**
     * returns true if the JVM version is 1.3 or greater
     *
     * @return   The JavaVersionFlag value
     */
    public boolean getJavaVersionFlag()
    {
        return this.versionFlag;
    }

    /** 
     * Exit the application, asking the user if they are sure 
     */
    public void exitApplication()
    {
        try {
            // Really need to check for dirty
            // documents and exit quickly if nothing is to be done.

            logOutExit();
            config.save();
            System.exit(0);
            // close the application
        } catch (Exception e) {
        }
    }

    /**
     * sends a data file to the metacat using "multipart/form-data" encoding
     *
     * @param id    the id to assign to the file on metacat (e.g., knb.1.1)
     * @param file  the file to send
     * @return      the response stream from metacat
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
        try {
            //FileInputStream data = new FileInputStream(file);

            Log.debug(20, "Sending data to: |" + metacatURL + "|");
            URL url = new URL(metacatURL.trim());
            HttpMessage msg = new HttpMessage(url);
            Properties args = new Properties();
            args.put("action", "upload");
            args.put("docid", id);

            Properties dataStreams = new Properties();
            String filename = file.getAbsolutePath();
            Log.debug(20, "Sending data file: " + filename);
            dataStreams.put("datafile", filename);

            /*
            Note:  The reason that there are three try statements all executing
            the same code is that there is a problem with the initial connection
            using the HTTPClient protocol handler.  These try statements make 
            sure that a connection is made because it gives each connection a 
            2nd and 3rd chance to work before throwing an error.
            THIS IS A TOTAL HACK.  THIS NEEDS TO BE LOOKED INTO AFTER THE BETA1
            RELEASE OF MORPHO!!!  cwb (7/24/01)
              */
            try {
                returnStream = msg.sendPostData(args, dataStreams);
            } catch (Exception ee) {
                try {
                    returnStream = msg.sendPostData(args, dataStreams);
                } catch (Exception eee) {
                    try {
                        returnStream = msg.sendPostData(args, dataStreams);
                    } catch (Exception eeee) {
                        throw new Exception(eeee.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.debug(1, "Fatal error sending binary data to Metacat: " +
                    e.getMessage());
            e.printStackTrace(System.err);
        }
        return returnStream;
    }

    /**
     * Log into metacat.
     *
     * @return   boolean true if the attempt to log in succeeded
     */
    public boolean logIn()
    {
        Properties prop = new Properties();
        prop.put("action", "login");
        prop.put("qformat", "xml");
        Log.debug(20, "Logging in using uid: " + userName);
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
            UIController controller = UIController.getInstance();
            if (controller != null) {
                controller.updateAllStatusBars();
            }
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
            passWord = "none";
            // get rid of existing password info
            Properties prop = new Properties();
            prop.put("action", "logout");
            prop.put("qformat", "xml");

            String response = getMetacatString(prop);
            doLogoutCleanup();
        }
    }


    /** 
     * Log out of metacat when exiting.
     */
    public void logOutExit()
    {
        if (connected) {
            passWord = "none";
            // get rid of existing password info
            Properties prop = new Properties();
            prop.put("action", "logout");
            prop.put("qformat", "xml");

            String response = getMetacatString(prop);
            HttpMessage.setCookie(null);
            connected = false;

        }
    }

    /**
     * This method is called by plugins to register a listener for changes in
     * the Connection status. Any change in the username, password, or other
     * connect change will trigger notification.
     *
     * @param listener  a reference to the object to be notified of changes
     * @throws ServiceExistsException
     */
    public void addConnectionListener(ConnectionListener listener)
    {
        if (!connectionRegistry.contains(listener)) {
            Log.debug(20, "Adding listener: " + listener.toString());
            connectionRegistry.addElement(listener);
        }
    }

    /** Description of the Method */
    public void checkJavaVersion()
    {
        String ver = System.getProperty("java.version");
        int pos1 = ver.indexOf(".");
        int pos2 = ver.indexOf(".", pos1 + 1);
        String ver0 = ver.substring(0, pos1);
        String ver1 = ver.substring(pos1 + 1, pos2);
        int iver0 = (new Integer(ver0)).intValue();
        int iver1 = (new Integer(ver1)).intValue();
        if ((iver0 == 1) && (iver1 < 3)) {
            versionFlag = false;
            JOptionPane.showMessageDialog(null,
                    "Version " + ver + " of the Java Virtual Machine(JVM) " +
                    "is currently in use.\n" +
                    "Although most of Morpho will operate using early " +
                    "versions of the JVM,\n" +
                    "Version 1.3 or greater is required for all " +
                    "functions to work properly!");
        } else {
            if (System.getProperty("os.name").equalsIgnoreCase("Linux") 
                        && ver.compareTo("1.4") < 0)
            {
                JOptionPane.showMessageDialog(null,
                    "You are currently using version " + ver + " of the Java "
                    +"Virtual Machine(JVM) on a Linux system.\n\n"
                    +"Unfortunately, Morpho's initial \"Welcome\" screen may "
                    +"not display correctly with this configuration. \n\n"
                    +"Please note that all other parts of Morpho will still "
                    +"operate correctly, and you can access all the \n"
                    +"welcome screen functions (change profile, login, "
                    +"new/open/search packages) from Morpho's menus.\n\n"
                    +"If possible, we recommend installing JVM version 1.4 or "
                    +"later, which is available with the Morpho distribution");
            }
        }
    }

    /**
     * The entry point for this application. Sets the Look and Feel to the
     * System Look and Feel. Creates a new JFrame1 and makes it visible.
     *
     * @param args  Description of Parameter
     */
    public static void main(String args[])
    {
        try {
          SplashFrame sf = new SplashFrame(true);
          sf.setVisible(true);            
      
            // Set system property to use HTTPClient or ssl protocol
            // System.setProperty("java.protocol.handler.pkgs","HTTPClient");

            java.net.URL.setURLStreamHandlerFactory(
                new java.net.URLStreamHandlerFactory()
                {
                    public java.net.URLStreamHandler createURLStreamHandler(
                        final String protocol)
                    {
                        if ("http".equals(protocol)) {
                            try {
                                URLStreamHandler urlsh = 
                                    new HTTPClient.http.Handler();
                                return urlsh;
                            } catch (Exception e) {
                                System.out.println(
                                    "Error setting URL StreamHandler!");
                                return null;
                            }
                        }
                        return null;
                    }
                });

            // Set the keystore used
            System.setProperty("javax.net.ssl.trustStore", "./lib/morphocacerts");

            // add provider for SSL support
            java.security.Security.addProvider(
                new com.sun.net.ssl.internal.ssl.Provider());



            //initialize the config
            initializeConfiguration();

            // Set up logging, possibly to a file as appropriate
            initializeLogging(config);

            // Create a new instance of our application
            Morpho morpho = new Morpho(config);
            thisStaticInstance = morpho;

            // Set the version number
            //VERSION = config.get("version", 0);

            
            // set to the Look and Feel of the native system.
            setLookAndFeel(config.get("lookAndFeel", 0));


            // Load the current profile and log in
            morpho.loadProfile(morpho);

            // Set up the Service Controller
            ServiceController services = ServiceController.getInstance();

            // Set up the User Interface controller (UIController)
            UIController controller = UIController.initialize(morpho);

            // Add the default menus and toolbars
             morpho.initializeActions();

            // Load all of the plugins, their menus, and toolbars
            morpho.loadPlugins();


            //Create a frame with a welcome screen until a plugin takes over
             makeWelcomeWindow();
                
            //get rid of the splash window
            sf.dispose();
            
        } catch (Throwable t) {
            t.printStackTrace();
            //Ensure the application exits with an error condition.
            System.exit(1);
        }
    }

    /**
     * Set up a SAX parser for reading an XML document
     *
     * @param contentHandler  object to be used for parsing the content
     * @param errorHandler    object to be used for handling errors
     * @return                a SAX XMLReader object for parsing
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
                parser.setFeature("http://xml.org/sax/features/namespaces", 
                    true);
                Log.debug(30, "Parser created is: " +
                        parser.getClass().getName());
            } else {
                Log.debug(9, "Unable to create SAX parser!");
            }

            // Set the ContentHandler to the provided object
            if (null != contentHandler) {
                parser.setContentHandler(contentHandler);
            } else {
                Log.debug(3,
                        "No content handler for SAX parser!");
            }

            // Set the error Handler to the provided object
            if (null != errorHandler) {
                parser.setErrorHandler(errorHandler);
            }
        } catch (Exception e) {
            Log.debug(1, "Failed to create SAX parser:\n" +
                    e.toString());
        }

        return parser;
    }

    /**
     * Set up a DOM parser for reading an XML document
     *
     * @return   a DOM parser object for parsing
     */
    public static DocumentBuilder createDomParser()
    {
        DocumentBuilder parser = null;

        try {
            //ClassLoader cl = Thread.currentThread().getContextClassLoader();
            ClassLoader cl = Morpho.class.getClassLoader();
            Log.debug(30, "Current ClassLoader is: " +
                    cl.getClass().getName());
            Thread t = Thread.currentThread();
            t.setContextClassLoader(cl);
            DocumentBuilderFactory factory = 
                DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newDocumentBuilder();
            if (parser != null) {
                Log.debug(30, "Parser created is: " +
                        parser.getClass().getName());
            } else {
                Log.debug(9, "Unable to create DOM parser!");
            }
        } catch (ParserConfigurationException pce) {
            Log.debug(9, "Exception while creating DOM parser!");
            Log.debug(10, pce.getMessage());
        }

        return parser;
    }

    /**
     * Sets the LastID attribute of the Morpho object
     *
     * @param scope  The new LastID value
     */
    private void setLastID(String scope)
    {
        //MB 05-21-02: if (connected && networkStatus) {
        // only execute if connected to avoid hanging when there is
        // no network connection
        if (networkStatus) {
            String id = getLastID(scope);
            if (id != null) {
                int num = (new Integer(id)).intValue();
                String curval = profile.get("lastId", 0);
                int curnum = (new Integer(curval)).intValue();
                if (curnum < num) {
                    num = num + 1;
                    // required because Metacat does not return the latest id
                    id = (new Integer(num)).toString();
                    profile.set("lastId", 0, id);
                    profile.save();
                }
            }
        }
    }

    /**
     * Gets the LastID attribute of the Morpho object
     *
     * @param scope  Description of Parameter
     * @return       The LastID value
     */
    private String getLastID(String scope)
    {
        String result = null;
        Properties lastIDProp = new Properties();
        lastIDProp.put("action", "getlastdocid");
        lastIDProp.put("scope", scope);
        String temp = getMetacatString(lastIDProp);
        /*
            if successful temp should be of the form
            <?xml version="1.0"?>
            <lastDocid>
            <scope>fegraus</scope>
            <docid>fegraus.53.1</docid>
            </lastDocid>
          */
        if (temp != null) {
            int ind1 = temp.indexOf("<docid>");
            int ind2 = temp.indexOf("</docid>");
            if ((ind1 > 0) && (ind2 > 0)) {
                result = temp.substring(ind1 + 7, ind2);
                if (!result.equals("null")) {
                    // now remove the version and header parts of the id
                    result = result.substring(0, result.lastIndexOf("."));
                    result = result.substring(result.indexOf(".") + 1, 
                        result.length());
                } else {
                    result = null;
                }
            }
        }
        return result;
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
        try {
            for (Enumeration q = plugins.elements(); q.hasMoreElements(); ) {

                // Start by creating the new plugin
                PluginInterface plugin = (PluginInterface)
                        createObject((String)(q.nextElement()));

                // Set a reference to the framework in the Plugin
                plugin.initialize(this);
            }
            pluginsLoaded = true;
        } catch (ClassCastException cce) {
            Log.debug(5, "Error loading plugin: wrong class!");
        }
    }

    /** Set up the actions for menus and toolbars for the application */
    private void initializeActions()
    {

        UIController controller = UIController.getInstance();

        // FILE MENU ACTIONS
        Command connectCommand = new Command() {
            public void execute(ActionEvent e) {
                establishConnection();
            }
        };
        GUIAction connectItemAction = 
            new GUIAction("Login/Logout", null, connectCommand);
        connectItemAction.setToolTipText("Login/Logout...");
        connectItemAction.setMenuItemPosition(5);
        connectItemAction.setSeparatorPosition(SEPARATOR_PRECEDING);
        connectItemAction.setMenu("File", 0);
        controller.addGuiAction(connectItemAction);


        Command profileCommand = new CreateNewProfileCommand();

        GUIAction profileItemAction = 
            new GUIAction("New profile...", null, profileCommand);
        profileItemAction.setToolTipText("New Profile...");
        profileItemAction.setMenuItemPosition(6);
        profileItemAction.setMenu("File", 0);
        controller.addGuiAction(profileItemAction);

        Command switchCommand = new Command() {
            public void execute(ActionEvent e) {
                switchProfile();
            }
        };
        GUIAction switchItemAction =
            new GUIAction("Switch profile...", null, switchCommand);
        switchItemAction.setToolTipText("Switch Profile...");
        switchItemAction.setMenuItemPosition(7);
        switchItemAction.setSeparatorPosition(SEPARATOR_FOLLOWING);
        switchItemAction.setMenu("File", 0);
        controller.addGuiAction(switchItemAction);

        Command prefsCommand = new Command() {
            public void execute(ActionEvent e) {
                setPreferences();
            }
        };
        GUIAction prefsItemAction =
            new GUIAction("Set preferences...", null, prefsCommand);
        prefsItemAction.setToolTipText("Set Preferences...");
        prefsItemAction.setMenuItemPosition(8);
        prefsItemAction.setSeparatorPosition(SEPARATOR_FOLLOWING);
        prefsItemAction.setMenu("File", 0);
        controller.addGuiAction(prefsItemAction);
        

        Command exitCommand = new Command() {
            public void execute(ActionEvent event) {
                exitApplication();
            }
        };
        GUIAction exitItemAction = new GUIAction("Exit", null, exitCommand);
        exitItemAction.putValue(Action.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke("control Q"));
        exitItemAction.setToolTipText("Exit Morpho");
        exitItemAction.setSeparatorPosition(SEPARATOR_PRECEDING);
        exitItemAction.putValue("menuPosition", new Integer(20));
        exitItemAction.setMenu("File", 0);
        controller.addGuiAction(exitItemAction);

/*
        Action prefsItemAction =
            new AbstractAction("Preferences...")
            {
                public void actionPerformed(ActionEvent e)
                {
                    Log.debug(9, "Preferences dialog not yet implemented!");
                }
            };
        prefsItemAction.putValue(Action.SHORT_DESCRIPTION,
                "Open the Preferences dialog.");
        prefsItemAction.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource(
                "/toolbarButtonGraphics/general/Preferences16.gif")));
        prefsItemAction.putValue(Action.DEFAULT, SEPARATOR_PRECEDING);
        prefsItemAction.putValue("menuPosition", new Integer(5));
        prefsItemAction.setEnabled(false);
*/
        // HELP MENU ACTIONS
        Command aboutCommand = new Command() {
            public void execute(ActionEvent event) {
                SplashFrame sf = new SplashFrame();
                sf.setVisible(true);
            }
        };
        GUIAction aboutItemAction = 
            new GUIAction("About...", null, aboutCommand);
        aboutItemAction.putValue(Action.SHORT_DESCRIPTION, "About Morpho");
        aboutItemAction.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/About16.gif")));
        aboutItemAction.putValue("menuPosition", new Integer(1));
        aboutItemAction.setMenu("Help", 5);
        controller.addGuiAction(aboutItemAction);

        Command helpCommand = new HelpCommand();
        GUIAction helpItemAction = 
            new GUIAction("Help...", null, helpCommand);
        helpItemAction.putValue(Action.SHORT_DESCRIPTION, "Morpho Help");
        helpItemAction.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/Help16.gif")));
        helpItemAction.putValue("menuPosition", new Integer(2));
        helpItemAction.setMenu("Help", 5);
        controller.addGuiAction(helpItemAction);
    }

    /** Create a new connection to metacat */
    private void establishConnection()
    {
        if (networkStatus) {
            ConnectionFrame cf = new ConnectionFrame(this);
            cf.setVisible(true);
        } else {
            profile.set("searchmetacat", 0, "false");
            Log.debug(6, "No network connection available - can't log in");
        }
    }

    /** Create a new profile */
    
    private static void createNewProfile()
    {
        String previousProfileName = getCurrentProfileName();
        ProfileDialog dialog = new ProfileDialog(thisStaticInstance);
        dialog.setVisible(true);
        if (previousProfileName!=getCurrentProfileName()) {
            fireProfileAdded();
        }
    }

    /** Switch profiles (from one existing profile to another) */
    private void switchProfile()
    {
        logOut();
        String currentProfile = getCurrentProfileName();

        String[] profilesList = getProfilesList();

        int selection = 0;
        for (selection = 0; selection < profilesList.length; selection++) {
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
                Log.debug(9, "No change in profile.");
            } else {
                setProfile(newProfile);
                // close all old windows and initial a new one
                cleanUpFrames();
                Log.debug(9, "New profile is: " + newProfile);
            }
        }
    }

    private void setPreferences()
    {
      MorphoFrame mf = UIController.getInstance().getCurrentActiveWindow();
		  MorphoPrefsDialog MorphoPrefsDialog1 = new MorphoPrefsDialog(mf, this);
	    MorphoPrefsDialog1.setModal(true);
			MorphoPrefsDialog1.setVisible(true);
      
    }
    
    public String[] getProfilesList() 
    {
        String profileDirName = config.getConfigDirectory() + File.separator +
                config.get("profile_directory", 0);
        File profileDir = new File(profileDirName);
        String profilesList[] = null;
        if (profileDir.isDirectory()) {

            // Get vector of profiles to be displayed
            return profileDir.list();
        } else {
            // This is an error
            Log.debug(3, "Error: Can not switch profiles.\n " +
                    "profile_directory is not a directory.");
        }
        return null;
    }    
    
    /*
     * This method will close all frames and show a blank frame
     */
   private void cleanUpFrames()
   {
     // Get ui constroller
     UIController controller = UIController.getInstance();
     // Close other window
     controller.removeAllWindows();     
     // Add a new startup window
     makeWelcomeWindow();
   }
   
   
     /*
      * This method creates an initial frame with welcome screen content
      */
    private static void makeWelcomeWindow()
    {
      UIController controller = UIController.getInstance();
      
      initialFrame = controller.addWindow(INITIALFRAMENAME);
     
      initialFrame.setMainContentPane(new InitialScreen(thisStaticInstance,
                                                             initialFrame));
     
      initialFrame.setSize((int)UISettings.DEFAULT_WINDOW_WIDTH,
                          (int)UISettings.DEFAULT_WINDOW_HEIGHT);
      initialFrame.setVisible(true);
    }

    /**
     * use to dynamically create an object from its name at run time uses
     * reflection
     *
     * @param className  Description of Parameter
     * @return           Description of the Returned Value
     */
    private Object createObject(String className)
    {
        Object object = null;
        try {
            Class classDefinition = Class.forName(className);
            object = classDefinition.newInstance();
        } catch (InstantiationException e) {
            Log.debug(1, e.toString());
        } catch (IllegalAccessException e) {
            Log.debug(1, e.toString());
        } catch (ClassNotFoundException e) {
            Log.debug(1, e.toString());
        }
        return object;
    }

    /**
     * cleanup routine called by logout() and by MetacatPinger thread Keeps all
     * this stuff in one place so as not repeat code
     */
    private void doLogoutCleanup()
    {
        HttpMessage.setCookie(null);
        connected = false;
        UIController.getInstance().updateAllStatusBars();
        fireConnectionChangedEvent();
    }

    /**
     * Fire off notifications for all of the registered ConnectionListeners when
     * the connection status changes.
     */
    private void fireConnectionChangedEvent()
    {
        for (int i = 0; i < connectionRegistry.size(); i++) {
            ConnectionListener listener =
                    (ConnectionListener)connectionRegistry.elementAt(i);
            if (listener != null) {
                listener.connectionChanged(isConnected());
            }
        }
    }


    /**
     * Fire off notifications for all of the registered ConnectionListeners when
     * the username is changed.
     */
    private void fireUsernameChangedEvent()
    {
        
        for (int i = 0; i < connectionRegistry.size(); i++) {
            ConnectionListener listener =
                    (ConnectionListener)connectionRegistry.elementAt(i);
            if (listener != null) {
                listener.usernameChanged(getUserName());
            }
        }
    }

    /**
     * Fire off notifications for all of the registered ProfileAddedListeners 
     * when a new profile is added.
     */
    private static void fireProfileAdded()
    {
        
        Iterator it = profileAddedListenerList.iterator();
        while (it.hasNext()) {
            ProfileAddedListener listener = (ProfileAddedListener)it.next();
            if (listener == null) continue;
            listener.profileAdded(getCurrentProfileName());
        }
    }

    /**
     * Add a ProfileAddedListener to listen for new profile additions.
     *
     *  @param listener the <code>ProfileAddedListener</code> that is being 
     *                  registered to receive callbacks
     */
    public void addProfileAddedListener(ProfileAddedListener listener)
    {
        if (listener!=null) profileAddedListenerList.add(listener);
    }


    /** Load configuration parameters from the config file as needed */
    private void loadConfigurationParameters()
    {
        metacatURL = config.get("metacat_url", 0);
        String temp_uname = config.get("username", 0);
        userName = (temp_uname != null) ? temp_uname : "public";
    }

        /** Set metacat URL string */
    public void setMetacatURLString(String mURL)
    {
        metacatURL = mURL;
    }
        /** Get metacat URL string */
    public String getMetacatURLString()
    {
        return metacatURL;
    }

    
    /**
     * Takes a hashtable where the key is an Integer and returns a 
     * Vector of hashtable values sorted by key values.
     * this is a quick hack!!! DFH
     *
     * @param hash  the data to sort
     * @return      sorted version of the hash table
     */
    private Vector sortValues(Hashtable hash)
    {
        // assume that there are no more that 20 values in the hash
        // and return only the first 20 (i.e. 0 - 19
        Vector sorted = new Vector();
        for (int i = 0; i < 20; i++) {
            Integer iii = new Integer(i);
            Enumeration www = hash.keys();
            while (www.hasMoreElements()) {
                Object thiskey = www.nextElement();
                if (iii.equals(thiskey)) {
                    sorted.addElement(hash.get(thiskey));
                }
            }
        }
        return sorted;
    }

    /**
     * overload to give default functionality; boolean flag needed only at
     * startup
     */
    private void doPing()
    {
        doPing(false);
    }

    /**
     * Sets networkStatus to boolean true if metacat connection can be made
     *
     * @param isStartUp  - set to boolean "true" when calling for first time, so
     *      we don't get "No such service registered." exception from
     *      getServiceProvider()
     */
    private void doPing(final boolean isStartUp)
    {
      if (!connectionBusy) {
        final SwingWorker sbUpdater =
            new SwingWorker()
            {
                public Object construct()
                {
                    startPing();
                    return null;
                    //return value not used by this program
                }

                //Runs on the event-dispatching thread.
                public void finished()
                {
                    finishPing(isStartUp);
                }
            };
        sbUpdater.start();
      }
    }

    /**
     * Start the ping operation. At startup this is called in the main
     * application thread, but later it is used in a distinct thread to keep the
     * application responsive.
     */
    private void startPing()
    {
        //check if metacat can be reached:
        origNetworkStatus = networkStatus;
        try {
            Log.debug(55, "Determining net status ...");
            urlConn = metacatPingURL.openConnection();
            urlConn.connect();
            networkStatus = (urlConn.getDate() > 0L);
            Log.debug(55, "... which is: " + networkStatus);
        } catch (IOException ioe) {
            Log.debug(55, " - unable to open network connection to Metacat");
            networkStatus = false;
            if (profile != null) {
                profile.set("searchmetacat", 0, "false");
            }
        }
    }

    /**
     * Finish the ping operation. At startup this is called in the main
     * application thread, but later it is used in a distinct thread to keep the
     * application responsive.
     *
     * @param isStartUp  set to true if this is the startup sequence before
     *                   plugins have been loaded
     */
    private void finishPing(boolean isStartUp)
    {
        Log.debug(55, "doPing() called - network available?? - " +
                networkStatus);
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
                        Log.debug(6, snhe.getMessage());
                        }
                      */
                }
                //update status bar
            }
            if (!isStartUp) {
                UIController.getInstance().updateAllStatusBars();
            }
        }
    }

    /**
     * This ActionListener is notified by the swing.Timer every
     * METACAT_PING_INTERVAL milliSeconds, upon which it tries to contact the
     * Metacat defined by "metacatURL"
     */
    ActionListener pingActionListener =
        new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doPing();
            }
        };

    /**
     * set look & feel to system default
     *
     * @param lnf  The new LookAndFeel value
     */
    public static void setLookAndFeel(String lnf)
    {
        try {
            if (lnf != null) {
                if (lnf.equalsIgnoreCase("kunststoff")) {
                    Log.debug(19, "kunststoff - loading");
                    try {
                        Class classDefinition = Class.forName(
                            "com.incors.plaf.kunststoff.KunststoffLookAndFeel");
                        LookAndFeel test = 
                            (LookAndFeel)classDefinition.newInstance();
                        UIManager.setLookAndFeel(test);
                    } catch (ClassNotFoundException www) {
                        Log.debug(19, 
                            "Couldn't set L&F to kunststoff. " +
                            "Using Java default");
                        return;
                    }
                } else if (lnf.equalsIgnoreCase("metal")) {
                    UIManager.setLookAndFeel(
                        UIManager.getCrossPlatformLookAndFeelClassName());
                } else if (lnf.equalsIgnoreCase("windows")) {
                    UIManager.setLookAndFeel(
                        "com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                        maybeSetMSWindowsJSplitPaneDividerThickness();
                } else if (lnf.equalsIgnoreCase("motif")) {
                    UIManager.setLookAndFeel(
                        "com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                } else {
                    UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
                        maybeSetMSWindowsJSplitPaneDividerThickness();
                }
            } else {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
                    maybeSetMSWindowsJSplitPaneDividerThickness();
            }

        } catch (Exception e) {
            Log.debug(19, "couldn't set L&F to native - using Java default");
        }
    }

    private static void maybeSetMSWindowsJSplitPaneDividerThickness() {

        if (UIManager.getSystemLookAndFeelClassName().indexOf(
                                                "WindowsLookAndFeel")<0) return;
        UIManager.getLookAndFeelDefaults().put("SplitPane.dividerSize",
                                               DIVIDER_THICKNESS_FOR_MSWINDOWS);
//        UIManager.getLookAndFeelDefaults().put("SplitPane.background",
//                       UISettings.CUSTOM_GRAY);
    }
    /**
     * Attempts to connect a socket, returns null if it is not successful
     * returns the connected socket if it is successful.
     *
     * @param host  the fully qualified host name for the connection
     * @param port  the port number for the connection
     * @return      The Socket value
     */
    private static Socket getSocket(String host, int port)
    {
        Socket s = null;
        try {
            s = new Socket(host, port);
            return s;
        } catch (UnknownHostException u) {
            System.out.println("unknown host in " +
                "DataFileUploadInterface.getSocket");
        } catch (IOException i) {
            //an ioexception is thrown if the port is not in use
            return s;
        }
        return s;
    }
    
    /**
     * Set up the config properties during startup
     *
     * @throws FileNotFoundException
     */
    private static void initializeConfiguration() throws FileNotFoundException 
    {
        // Make sure the config directory exists
        File configurationFile  = null;
        File configDir = new File(ConfigXML.getConfigDirectory());
        if (!configDir.exists()) {
            if (!configDir.mkdir()) {
                Log.debug(1, "Failed to create config directory");
                System.exit(0);
            }
        }

        // Make sure the config file has been copied to the proper directory
        boolean copyConfig = false;
        boolean saveOldVersion = false;
        String configVersion = null;
        try {
            // Determine if the config needs to be created or upgraded
            configurationFile = new File(configDir, configFile);
            if (configurationFile.createNewFile() 
                    || configurationFile.length() == 0) {
                copyConfig = true;
            } else {
                saveOldVersion = true;
                config = new ConfigXML(configurationFile.getAbsolutePath());
                configVersion = config.get("version", 0);
                if (!configVersion.equals(VERSION)) {
                    copyConfig = true;
                }
            }

            // If the config file is out-of-date, replace it,
            // keeping a copy of the old version
            if (copyConfig) {

                // Save the old version
                if (saveOldVersion) {
                    String extension = ".saved";
                    if (configVersion != null) {
                        extension = "." + configVersion;
                    }
                    File savedConfigFile = new File(
                            configurationFile.getAbsolutePath() + extension); 
                    configurationFile.renameTo(savedConfigFile);
                }

                // Create a new config file from the jar file copy
                configurationFile.createNewFile();
                FileOutputStream out = new FileOutputStream(configurationFile);
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                InputStream configInput =  cl.getResourceAsStream(configFile);
                if (configInput == null) {
                    Log.debug(1, "Could not find default configuration file.");
                    System.exit(0);
                }
                byte buf[] = new byte[4096];
                int len = 0;
                while ((len = configInput.read(buf, 0, 4096)) != -1) {
                    out.write(buf, 0, len);
                }
                configInput.close();
                out.close();
                
                // Open the new configuration file
                config = new ConfigXML(configurationFile.getAbsolutePath());
            }
        } catch (IOException ioe) {
            Log.debug(1, "Error copying config: " + ioe.getMessage());
            Log.debug(1, ioe.getClass().getName());
            System.exit(1);
        }
    }
    
    
    /**
     * Set up the logging system during startup
     *
     * @param config  the configuration object for the application
     */
    public static void initializeLogging(ConfigXML config)
    {
        Log log = Log.getLog();
        debug_level = (new Integer(config.get("debug_level", 0))).intValue();
        log.setDebugLevel(debug_level);
        String log_file_setting = config.get("log_file", 0);
        if (log_file_setting != null) {
            if (log_file_setting.equalsIgnoreCase("true")) {
                log_file = true;
            } else {
                log_file = false;
            }
        }
        if (log_file) {
            try {
                FileOutputStream err = new FileOutputStream("stderr.log");
                PrintStream errPrintStream = new PrintStream(err);
                System.setErr(errPrintStream);
                System.setOut(errPrintStream);
            } catch (FileNotFoundException fnfe) {
                Log.debug(10, "Warning: Failure to redirect log to a file.");
            }
        }
    }
                
    /**
     * Set up the profile properties during startup
     *
     * @throws FileNotFoundException
     */
    private void loadProfile(Morpho morpho) throws FileNotFoundException 
    {
        // Check if the profileConfig file exists, create it if needed
        File configDir = new File(ConfigXML.getConfigDirectory());
        File profileFile = null;
        try {
            profileFile = new File(configDir, profileFileName);
            if (profileFile.createNewFile() 
                    || profileFile.length() == 0) {
                FileWriter out = new FileWriter(profileFile);
                out.write("<current_profile></current_profile>\n");
                out.close();
            }
        } catch (IOException ioe) {
            Log.debug(1, "Error creating profile marker: " + ioe.getMessage());
            Log.debug(1, ioe.getClass().getName());
            System.exit(1);
        }

        // Open the profileConfig file
        profileConfig = new ConfigXML(profileFile.getAbsolutePath());

        // Load the current profile and log in
        String profileDir = ConfigXML.getConfigDirectory() + 
            File.separator + config.get("profile_directory", 0);
        String currentProfile = getCurrentProfileName();
        if (currentProfile == null) {
            ProfileDialog dialog = new ProfileDialog(morpho);
            dialog.setVisible(true);
            // Make sure they actually created a profile
            if (getProfile() == null) {
                JOptionPane.showMessageDialog(null,
                    "You must create a profile in order " +
                    "to configure Morpho  \n" +
                    "correctly.  Please restart Morpho " +
                    "and try again.");
                exitApplication();
            }
        } else {
            String profileName = profileDir + File.separator + 
                currentProfile + File.separator + 
                currentProfile + ".xml";
            ConfigXML profile = new ConfigXML(profileName);
            profile.set("searchmetacat",0,"false");
            setProfileDontLogin(profile);
        }
    }
    
    public static class CreateNewProfileCommand implements Command {
        public void execute(ActionEvent e) {
            createNewProfile();
        }
    };
    
}

