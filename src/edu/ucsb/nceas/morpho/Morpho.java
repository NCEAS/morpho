/**
 *  '$RCSfile: Morpho.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-06-02 16:43:05 $'
 * '$Revision: 1.105 $'
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

import edu.ucsb.nceas.itis.Itis;
import edu.ucsb.nceas.itis.ItisException;
import edu.ucsb.nceas.itis.Taxon;
import edu.ucsb.nceas.morpho.datapackage.DataPackagePlugin;
import edu.ucsb.nceas.morpho.datastore.DataStore;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.framework.BackupMorphoDataFrame;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ConnectionFrame;
import edu.ucsb.nceas.morpho.framework.ConnectionListener;
import edu.ucsb.nceas.morpho.framework.CorrectEML201DocsFrame;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.HelpCommand;
import edu.ucsb.nceas.morpho.framework.HelpMetadataIntroCommand;
import edu.ucsb.nceas.morpho.framework.HttpMessage;
import edu.ucsb.nceas.morpho.framework.InitialScreen;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.MorphoGuideCommand;
import edu.ucsb.nceas.morpho.framework.MorphoPrefsDialog;
import edu.ucsb.nceas.morpho.framework.ProfileAddedListener;
import edu.ucsb.nceas.morpho.framework.ProfileDialog;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.framework.SplashFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.XMLReader;

import com.ice.tar.TarArchive;
import com.ice.tar.TarEntry;

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
    public static String VERSION = "1.7.0";

    /** Constant to indicate a separator should precede an action */
    public static String SEPARATOR_PRECEDING = "separator_preceding";
    /** Constant to indicate a separator should follow an action */
    public static String SEPARATOR_FOLLOWING = "separator_following";
    /** Constant of initial morpho frame name */
    public static final String INITIALFRAMENAME = "Morpho";
    
    /** Constant int for File menu position */
    public static final int FILEMENUPOSITION = 0;
    
    /** Constant String for File menu label */
    public static final String FILE_MENU_LABEL = "File";
    
    /** Constant int for Window menu position */
    public static final int WINDOWMENUPOSITION = 50;
    
    /** Constant String for Window menu label */
    public static final String WINDOW_MENU_LABEL = "Window";
    
    /** Constant int for Help menu position */
    public static final int HELPMENUPOSITION = 60;
    
    /** Constant String for Help menu label */
    public static final String HELP_MENU_LABEL = "Help";


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
    private static final String AUTHENTICATEERROR = "peer not authenticated";
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
    private final static String LIBDIR = "lib/";
    private final static String TRUSTKEYSTORE = "truststore";
    private static String keystorePass = "changeit";
    private static String userKeystore = "";
    public static Morpho thisStaticInstance;
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
        boolean startup = true;
        startPing(startup);
        finishPing(startup);

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

    private void deleteProfile(String profileName)
    {
        String profileDir = config.getConfigDirectory() + File.separator +
                config.get("profile_directory", 0);
        String profilePath = 
        	profileDir + File.separator + profileName;
        File profileFile = new File(profilePath);
        Util.deleteDirectory(profileFile);
    }
    
    /**
     *  delete all files in cache
     */
    public void cleanCache() {
      String profileDir = config.getConfigDirectory() + File.separator +
                config.get("profile_directory", 0);
      String cacheDir = profileDir + File.separator + getCurrentProfileName() +
                         File.separator + "cache";
      File cacheDirFile = new File(cacheDir);
      String[] cacheList = cacheDirFile.list();
      for (int i=0;i<cacheList.length;i++) {
        File f = new File(cacheDir + File.separator + cacheList[i]);
        if (f.isDirectory()) {
          String[] fList = f.list();
          for (int j=0;j<fList.length;j++) {
            File ff = new File(f.getAbsolutePath() + File.separator + fList[j]);
            ff.delete();
          }
        }
        f.delete();
      }
    }

    /**
     *  delete all files in temp
     */
    public void cleanTemp() {
      String profileDir = config.getConfigDirectory() + File.separator +
                config.get("profile_directory", 0);
      String cacheDir = profileDir + File.separator + getCurrentProfileName() +
                         File.separator + "temp";
      File cacheDirFile = new File(cacheDir);
      String[] cacheList = cacheDirFile.list();
      for (int i=0;i<cacheList.length;i++) {
        File f = new File(cacheDir + File.separator + cacheList[i]);
        if (f.isDirectory()) {
          String[] fList = f.list();
          for (int j=0;j<fList.length;j++) {
            File ff = new File(f.getAbsolutePath() + File.separator + fList[j]);
            ff.delete();
          }
        }
        f.delete();
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
   * @param taxonName String
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
            UIController controller = UIController.getInstance();
            Vector dirty = controller.removeCleanWindows();
            if (dirty.size()<1) {
            // close the application
              logOutExit();
              config.save();
              System.exit(0);
            } else {
              for (int i=0;i<dirty.size();i++) {
                MorphoFrame frame = (MorphoFrame)dirty.elementAt(i);
                frame.close();
              }
            }
        } catch (Exception e) {
        }
    }

    /**
     * sends a data file to the metacat using "multipart/form-data" encoding
     *
     * @param id    the id to assign to the file on metacat (e.g., knb.1.1)
     * @param file  the file to send
     * @param objectName  the object name associate with the file
     * @return      the response stream from metacat
     */
    public InputStream sendDataFile(String id, File file, String objectName)
    {
        String retmsg = "";
        String filename = null;
        InputStream returnStream = null;
        File newFile = null;

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
            // use object name to replace the meaningless name such as 12.2
            if (objectName != null && !Util.isBlank(objectName))
            {
            	FileSystemDataStore store = new FileSystemDataStore(thisStaticInstance);
            	String tmpDir = store.getTempDir();
            	newFile = new File(tmpDir, objectName);
            	FileInputStream input = new FileInputStream(file);
            	FileOutputStream out = new FileOutputStream(newFile);
            	byte[] c = new byte[3*1024];
            	int read = input.read(c);
            	while (read !=-1)
            	{
            		out.write(c, 0, read);
            		read = input.read(c);
            	}
            	input.close();
            	out.close();
            	filename = newFile.getAbsolutePath();
            }
            else
            {
                filename = file.getAbsolutePath();
            }
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
        finally
        {
        	try
        	{
        		
        		if(newFile != null)
        		{
        			Log.debug(40, "delete file===============");
        			newFile.delete();
        		}
        	}
        	catch(Exception e)
        	{
        		 Log.debug(20, "============couldn't delete the new file ");
        	}
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
//        if ((iver0 == 1) && (iver1 < 3)) {
        if (iver1 < 4) {
            versionFlag = false;
            JOptionPane.showMessageDialog(null,
                    "Version " + ver + " of the Java Virtual Machine(JVM) " +
                    "is currently in use.\n" +
                    "Although most of Morpho will operate using early " +
                    "versions of the JVM,\n" +
                    "Version 1.4 or greater is recommended for all " +
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

/*    JOptionPane.showMessageDialog(null,
                    "Warning!!! This version of Morpho is 'ALPHA' code.\n" +
                    "\n" +
                    "This means that it is very fragile and known to\n" +
                    "contain errors. Please do not expect flawless operation.\n");
*/

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
                        else if ("https".equals(protocol)) {
                            try {
                                URLStreamHandler urlsh =
                                    new HTTPClient.https.Handler();
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


            //check for override config dir
            if (args.length > 0) {
            	String dir = args[0];
            	ConfigXML.setConfigDirectory(dir);
            }
            
            //initialize the config
            initializeConfiguration();

            // Set up logging, possibly to a file as appropriate
            initializeLogging(config);
            
             // setup keystore
            initializeKeyStore();
             //set up properties of 
   		    System.setProperty("javax.net.ssl.trustStore", userKeystore);
   	        System.setProperty("javax.net.ssl.trustStorePassword", keystorePass);
   	        System.setProperty("security.provider.3", "com.sun.net.ssl.internal.ssl.Provider");
   	        //System.setProperty("javax.net.debug","all");
   	        //System.setProperty("java.security.policy","/home/rzheva/test/java.policy"); 

   	         
            // Create a new instance of our application
            Morpho morpho = new Morpho(config);
            thisStaticInstance = morpho;

            // Set the version number
            //VERSION = config.get("version", 0);


            // set to the Look and Feel of the native system.
            setLookAndFeel(config.get("lookAndFeel", 0));
            
            // back up .morpho dir when backup_dot_morpho_dir is not false and 
   	        // current version of backup file doesn't exist.
   	         BackupMorphoDataFrame backupFrame = new BackupMorphoDataFrame(morpho);
   	         backupFrame.doBackup();

   	        // Set up the User Interface controller (UIController)
             UIController controller = UIController.initialize(morpho);
             
            // Load the current profile and log in
            morpho.loadProfile(morpho);
            
            // Correct the invalid eml 201 documents
            CorrectEML201DocsFrame correctFrame = new CorrectEML201DocsFrame(morpho);
            correctFrame.doCorrection();
            
            // Set up the Service Controller
            ServiceController services = ServiceController.getInstance();

            

            // Add the default menus and toolbars
             morpho.initializeActions();

            // Load all of the plugins, their menus, and toolbars
            morpho.loadPlugins();


            //Create a frame with a welcome screen until a plugin takes over
             makeWelcomeWindow();

            //get rid of the splash window
            sf.dispose();
            
            //check if there are crashed documents
            checkCrashedDocuments();


        } catch (Throwable t) {
            t.printStackTrace();
            //Ensure the application exits with an error condition.
            System.exit(1);
        }
    }

    public static void createMorphoInstance() {
      try{
        initializeConfiguration();
        // Create a new instance of our application
        Morpho morpho = new Morpho(config);
        // Load the current profile and log in
        morpho.loadProfile(morpho);
        thisStaticInstance = morpho;
      } catch (Exception e) {
        Log.debug(10,"error creating Morpho Instance");
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
        //if (networkStatus) {
            String id = getLastID(scope);
            if (id != null) {
                long num = (new Long(id)).longValue();
                String curval = profile.get("lastId", 0);
                long curnum = (new Long(curval)).longValue();
                if (curnum <= num) {
                    num = num + 1;
                    // required because Metacat does not return the latest id
                    id = (new Long(num)).toString();
                    profile.set("lastId", 0, id);
                    profile.save();
                }
            }
       //}
    }

    /**
     * Gets the LastID attribute of the Morpho object. It will go through both local and
     * metacat system
     *
     * @param scope  Description of Parameter
     * @return       The LastID value
     */
    public String getLastID(String scope)
    {
    	
        String result = null;
        String temp  =  null;
        Properties lastIDProp = new Properties();
        lastIDProp.put("action", "getlastdocid");
        lastIDProp.put("scope", scope);
         if (networkStatus)
         {
             temp= getMetacatString(lastIDProp);
         }
        Log.debug(30, "the last id from metacat ===== "+temp);
        //localMaxDocid will be 54 if the biggest file name is 54.2
        int localMaxDocid = getMaxLocalId(scope);
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
                    int metacatId = 0;
                    try 
            		{
            			metacatId = (new Integer(result).intValue());
            		} 
            		catch (NumberFormatException nfe) 
            		{
	                    Log.debug(30, "Last id from metacat is not integer.");
	                }
            		//choose the bigger one between local and metacat
                    if (metacatId < localMaxDocid )
                    {
                        result = ""+localMaxDocid;
                    }
                } 
                else 
                {
                	// no metacat lastid branch
                	if (localMaxDocid > 0)
                	{
                		//we have the maxid in local file, so use it.
                		result = ""+localMaxDocid;
                	}
                	else
                	{
                         result = null;
                	}
                }
            }
        }
        // add a code to handle somehow we can't get result from metacat
       if (result == null)
        {
        	// no metacat lastid branch
        	if (localMaxDocid > 0)
        	{
        		//we have the maxid in local file, so use it.
        		result = ""+localMaxDocid;
        	}
        	else
        	{
                 result = null;
        	}
        }
        Log.debug(30, "Final Last id is "+result);
        return result;
    }
    
    /*
     * Gets the max local id for given scope in current the profile.  The local file's names look like 100.1, 102.1... under scope dir.
     * In this case, 102 will be returned.
     */
    private int getMaxLocalId(String scope)
    {
    	    int docid =0;
    	    int maxDocid =0;
    	    String currentProfile = profile.get("profilename", 0);
  		    String separator = profile.get("separator", 0);
  		    ConfigXML config = getConfiguration();
  		    String profileDir = config.getConfigDirectory() + File.separator +
  		                       config.get("profile_directory", 0) + File.separator +
  		                       currentProfile;
  		    String datadir = profileDir + File.separator + profile.get("datadir", 0)+File.separator+scope;
  		    datadir = datadir.trim();
  		    Log.debug(30, "the data dir is ===== "+datadir);
  		    File directoryFile = new File(datadir);
    	    File[] files = directoryFile.listFiles();
    	    if (files != null)
    	    {
    	      for (int i=0;i<files.length;i++)
    	        {
    	            File currentfile = files[i];   	            
    	            if (currentfile != null && currentfile.isFile()) {  	                
    	                	String fileName = currentfile.getName();
    	                	Log.debug(50, "the file name in dir is "+fileName);
    	                	if (fileName != null)
    	                	{
    	                		fileName = fileName.substring(0, fileName.indexOf("."));
    	                		Log.debug(50, "the file name after removing revision in dir is "+fileName);
    	                		try 
    	                		{
    	                			docid = (new Integer(fileName).intValue());
    	                			if (docid > maxDocid)
    	                			{
    	                				maxDocid = docid;
    	                			}
    	                		} 
    	                		catch (NumberFormatException nfe) 
    	                		{
    	    	                    Log.debug(30, "Not loading file with invalid name");
    	    	                }
    	                	
    	                	}
    	            }
    	        }
    	    }
    	Log.debug(30, "The max docid in local file system for scope "+scope+" is "+maxDocid);      
    	return maxDocid;
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
        connectItemAction.setMenuItemPosition(9);
        connectItemAction.setSeparatorPosition(SEPARATOR_PRECEDING);
        connectItemAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
        controller.addGuiAction(connectItemAction);


        Command profileCommand = new CreateNewProfileCommand();

        GUIAction profileItemAction =
            new GUIAction("New profile...", null, profileCommand);
        profileItemAction.setToolTipText("New Profile...");
        profileItemAction.setMenuItemPosition(10);
        profileItemAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
        controller.addGuiAction(profileItemAction);

        Command switchCommand = new Command() {
            public void execute(ActionEvent e) {
                switchProfile();
            }
        };
        GUIAction switchItemAction =
            new GUIAction("Switch profile...", null, switchCommand);
        switchItemAction.setToolTipText("Switch Profile...");
        switchItemAction.setMenuItemPosition(11);
        switchItemAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
        controller.addGuiAction(switchItemAction);
        
        Command removeCommand = new Command() {
            public void execute(ActionEvent e) {
                removeProfile();
            }
        };
        GUIAction removeProfileAction =
            new GUIAction("Remove profile...", null, removeCommand);
        removeProfileAction.setToolTipText("Remove Profile...");
        removeProfileAction.setMenuItemPosition(12);
        removeProfileAction.setSeparatorPosition(SEPARATOR_FOLLOWING);
        removeProfileAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
        controller.addGuiAction(removeProfileAction);

        Command prefsCommand = new Command() {
            public void execute(ActionEvent e) {
                setPreferences();
            }
        };
        GUIAction prefsItemAction =
            new GUIAction("Set preferences...", null, prefsCommand);
        prefsItemAction.setToolTipText("Set Preferences...");
        prefsItemAction.setMenuItemPosition(13);
        prefsItemAction.setSeparatorPosition(SEPARATOR_FOLLOWING);
        prefsItemAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
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
        exitItemAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
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
        aboutItemAction.setMenu(HELP_MENU_LABEL, HELPMENUPOSITION);
        controller.addGuiAction(aboutItemAction);
        
        Command guideCommand = new MorphoGuideCommand();
        GUIAction guideItemAction =
            new GUIAction("Morpho User Guide...", null, guideCommand);
        guideItemAction.putValue(Action.SHORT_DESCRIPTION, "Morpho User Guide");
        guideItemAction.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/Help16.gif")));
        guideItemAction.putValue("menuPosition", new Integer(2));
        guideItemAction.setMenu(HELP_MENU_LABEL, HELPMENUPOSITION);
        controller.addGuiAction(guideItemAction);

        /*Command helpCommand = new HelpCommand();
        GUIAction helpItemAction =
            new GUIAction("Morpho User Guide...", null, helpCommand);
        helpItemAction.putValue(Action.SHORT_DESCRIPTION, "Morpho User Guide");
        helpItemAction.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/Help16.gif")));
        helpItemAction.putValue("menuPosition", new Integer(2));
        helpItemAction.setMenu("Help", 6);
        controller.addGuiAction(helpItemAction);*/

        Command mdIntroCommand = new HelpCommand("metadata");
        GUIAction mdIntroItemAction =
            new GUIAction("Intro to Metadata...", null, mdIntroCommand);
        mdIntroItemAction.putValue(Action.SHORT_DESCRIPTION, "Intro to Metadata");
        mdIntroItemAction.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/Help16.gif")));
        mdIntroItemAction.putValue("menuPosition", new Integer(3));
        mdIntroItemAction.setMenu(HELP_MENU_LABEL, HELPMENUPOSITION);
        controller.addGuiAction(mdIntroItemAction);

        Command mdEMLSpecCommand = new HelpCommand("eml_index");
        GUIAction mdEMLSpecItemAction =
            new GUIAction("EML Specifications...", null, mdEMLSpecCommand);
        mdEMLSpecItemAction.putValue(Action.SHORT_DESCRIPTION, "EML Specifications");
        mdEMLSpecItemAction.putValue(Action.SMALL_ICON,
            new ImageIcon(getClass().
            getResource("/toolbarButtonGraphics/general/Help16.gif")));
        mdEMLSpecItemAction.putValue("menuPosition", new Integer(4));
        mdEMLSpecItemAction.setMenu(HELP_MENU_LABEL, HELPMENUPOSITION);
        controller.addGuiAction(mdEMLSpecItemAction);

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
        MorphoFrame frame = UIController.getInstance().getCurrentActiveWindow();
        String newProfile = (String)JOptionPane.showInputDialog(frame,
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
      // need to recheck the ssl status
      sslStatus = (metacatURL.indexOf("https://") == 0);
      UIController.getInstance().updateAllStatusBars();
      // when preference change, the lastID should be change too.
      // since the remote server may have different max docid
      String scope = profile.get("scope", 0);
      setLastID(scope);
     //create URL object to poll for metacat connectivity since the metaca may be changed.
      try {
          metacatPingURL = new URL(metacatURL);
      } catch (MalformedURLException mfue) {
          Log.debug(5, "unable to read or resolve Metacat URL");
      }

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
    
    
    /*
     * Check if there is any crashed documents. If there are, display the list of them
     */
    private static void checkCrashedDocuments()
    {
    	 try 
         {
           ServiceController services = ServiceController.getInstance();
           ServiceProvider provider = 
                       services.getServiceProvider(QueryRefreshInterface.class);
           QueryRefreshInterface queryService = (QueryRefreshInterface)provider;
           queryService.listCrashedDocument(initialFrame);
         }
         catch (ServiceNotHandledException snhe) 
         {
           Log.debug(6, snhe.getMessage());
         }
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
        if (UIController.getInstance()!= null)
        {
           UIController.getInstance().updateAllStatusBars();
        }
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


  /**
   * Set metacat URL string
   *
   * @param mURL String
   */
  public void setMetacatURLString(String mURL)
    {
        metacatURL = mURL;
    }


  /**
   * Get metacat URL string
   *
   * @return String
   */
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
                    startPing(isStartUp);
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
    private void startPing(boolean isStartup)
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
        	if(isStartup && ioe.getMessage().contains(AUTHENTICATEERROR))
        	{
               Log.debug(5, " - Unable to open network connection to Metacat: "+ioe.getMessage());
        	}
        	else
        	{
        		Log.debug(55, " - unable to open network connection to Metacat");
        	}
            networkStatus = false;
            if (profile != null) {
                profile.set("searchmetacat", 0, "false");
            }
        } catch (NullPointerException npe) {
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
                	//When got the network connection, we should reset last docid, since
                	//remote metacat may have bigger docid number
                	if (profile != null)
                	{
                		Log.debug(55, "reset lastid when network is avaliable");
                	    String scope = profile.get("scope", 0);
                        setLastID(scope);
                	}
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
    
    /*
     * Set up the keystore file during the startup.
     * Keystore in the lib dir will be copied to ~/.morpho dir if the keystored 
     * doesn't exist in the ~/.morpho dir. If the file is there, nothing will be done currently.
     * In the next step, we should merge the two keystore if the ~/.morpho already has the keystore
     */
    private static void initializeKeyStore()
    {
    	//this method will be called after initializeConfiguration(), 
    	// so we wouldn't worry about creating configDir (~/.morpho)
    	try
    	{
    		 userKeystore = ConfigXML.getConfigDirectory()+"/"+TRUSTKEYSTORE;
    		 File userStore = new File(userKeystore);
    		 if (!userStore.exists()) 
    		 {
    			 // ~/.morpho doesn't has the keystore file, copy it.
                 File morphoKeystore = new File(LIBDIR+TRUSTKEYSTORE);
                 FileInputStream input = new FileInputStream(morphoKeystore);
                 FileOutputStream output = new FileOutputStream(userStore);
                 byte buf[] = new byte[4096];
                 int len = 0;
                 while ((len = input.read(buf, 0, 4096)) != -1) {
                     output.write(buf, 0, len);
                 }
                 input.close();
                 output.close();

             } 
    		 else 
    		 {
                 // now we do nothing
    			 //TODO we need a smart mechanism to merge the two keystore
             }
    		 
    	}
    	catch(Exception e)
    	{
    		Log.debug(5, "You have to run morpho without secure connection to metacat since "+e.getMessage()
    				           +".\n You may use file|setup preference menu to change the metacat url from \"https\" to \"http\"");
    	}
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
        Log.setDebugLevel(debug_level);
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
     * Gets the flag of disable saving incomplete doc.
     * @return true if we want to disable the incomplete saving feature
     */
    public static boolean getFlagofDisableSavingIncompleteDoc()
    {
      boolean disable = false;
      try
      {
        disable = (new Boolean(config.get("disableSavingIncompleteDoc", 0))).booleanValue();
      }
      catch(Exception e)
      {
        Log.debug(20, "Couldn't get the flag of disable incomplete saving and default value false will be applied");
      }
      return disable;
    }
    
  


  /**
   * Set up the profile properties during startup
   *
   * @throws FileNotFoundException
   * @param morpho Morpho
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

    /** Switch profiles (from one existing profile to another) */
	private void removeProfile()
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
    MorphoFrame frame = UIController.getInstance().getCurrentActiveWindow();
    String newProfile = (String)JOptionPane.showInputDialog(frame,
            "Select from existing profiles:", "Input",
            JOptionPane.INFORMATION_MESSAGE, null,
            profilesList, profilesList[selection]);

    // Set the new profile to the one selected if it is different
    if (null != newProfile) {
        if (currentProfile.equals(newProfile)) {
            Log.debug(0, "Cannot delete current profile!");
        } else {
        	int deleteContents = 
        		JOptionPane.showConfirmDialog(
        				frame, 
        				"Are you sure you want to delete this profile?" +
        				"\nALL data will be discarded." +
        				"\nThis action is not undoable.", 
        				"DESTRUCTIVE ACTION!", JOptionPane.YES_NO_OPTION);
        	if (deleteContents == JOptionPane.YES_OPTION) {
	            deleteProfile(newProfile);
	            // close all old windows
	            cleanUpFrames();
	            Log.debug(9, "Removed profile: " + newProfile);
        	}
        }
    }
}

	public static class CreateNewProfileCommand implements Command {
        public void execute(ActionEvent e) {
            createNewProfile();
        }
    };

}

