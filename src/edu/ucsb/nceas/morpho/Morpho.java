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

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.Application;

import edu.ucsb.nceas.itis.Itis;
import edu.ucsb.nceas.itis.ItisException;
import edu.ucsb.nceas.itis.Taxon;
import edu.ucsb.nceas.morpho.dataone.EcpAuthentication;
import edu.ucsb.nceas.morpho.datastore.DataONEDataStoreService;
import edu.ucsb.nceas.morpho.datastore.LocalDataStoreService;
import edu.ucsb.nceas.morpho.framework.BackupMorphoDataFrame;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ConnectionListener;
import edu.ucsb.nceas.morpho.framework.CorrectEML201DocsFrame;
import edu.ucsb.nceas.morpho.framework.HelpCommand;
import edu.ucsb.nceas.morpho.framework.IdentifierUpdaterFrame;
import edu.ucsb.nceas.morpho.framework.InitialScreen;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.MorphoGuideCommand;
import edu.ucsb.nceas.morpho.framework.MorphoPrefsDialog;
import edu.ucsb.nceas.morpho.framework.ProfileAddedListener;
import edu.ucsb.nceas.morpho.framework.ProfileDialog;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.framework.SplashFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.Util;


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
    public static String VERSION = "2.0.0";

    /** Constant to indicate a separator should precede an action */
    public static String SEPARATOR_PRECEDING = "separator_preceding";
    /** Constant to indicate a separator should follow an action */
    public static String SEPARATOR_FOLLOWING = "separator_following";
    /** Constant of initial morpho frame name */
    public static final String INITIALFRAMENAME = "Morpho";
    
    /** Constant int for File menu position */
    public static final int FILEMENUPOSITION = 0;
    
    /** Constant String for File menu label */
    public static final String FILE_MENU_LABEL = /*"File"*/ Language.getInstance().getMessage("File") ;
    
    /** Constant int for Window menu position */
    public static final int WINDOWMENUPOSITION = 50;
    
    /** Constant String for Window menu label */
    public static final String WINDOW_MENU_LABEL = /*"Window"*/ Language.getInstance().getMessage("Window");
    
    /** Constant int for Help menu position */
    public static final int HELPMENUPOSITION = 60;
    
    /** Constant String for Help menu label */
    public static final String HELP_MENU_LABEL = /*"Help"*/ Language.getInstance().getMessage("Help");


    /** if windows, need to increase widthof JSplitPane divider,
        otherwise max/min arrows don't render properly */
    private static final Integer DIVIDER_THICKNESS_FOR_MSWINDOWS=new Integer(8);

    // redirects standard out and err streams
    static boolean log_file = false;

    private static ConfigXML config;
    private static ConfigXML profileConfig;
    private ConfigXML profile;

    //private Action[] fileMenuActions = null;
    //private Action[] editMenuActions = null;
    //private Action[] helpMenuActions = null;
    //private Action[] containerToolbarActions = null;
    private Vector connectionRegistry = null;
    private static final List profileAddedListenerList = new ArrayList();
    private static MorphoFrame initialFrame;
    private boolean pluginsLoaded = false;
    private Itis itis;

    private boolean versionFlag = true;

    /** The hardcoded XML configuration file */
    private static String configFile = "config.xml";
    public static final String ACCESS_FILE_NAME = "dataone-subject-list.xml";
    private static String profileFileName = "currentprofile.xml";
    private static boolean debug = true;
    private static int debug_level = 9;
    private final static String LIBDIR = "lib/";
    public static Morpho thisStaticInstance;
    
    // for interacting with the local store service
    private LocalDataStoreService lds = null;
    
    // for interacting with the DataONE services
    private DataONEDataStoreService dds = null;

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
        initMacApplication();

        // Create the connection registry
        connectionRegistry = new Vector();
        
        
    }
    
    /**
     * Get a reference to the DataONEDataStoreService
     * @return
     */
    public DataONEDataStoreService getDataONEDataStoreService() {
    	return dds;
    }
    
    public void setDataONEDataStoreService(DataONEDataStoreService dds) {
    	this.dds = dds;
    }
    
    /**
     * Get a reference to the LocalDataStoreService
     * TODO: remove for support on a per-profile basis
     * @return
     */
    public LocalDataStoreService getLocalDataStoreService() {
    	return lds;
    }
    
    public void setLocalDataStoreService(LocalDataStoreService lds) {
    	this.lds = lds;
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

        fireConnectionChangedEvent();
    }

    /**
    *  Set the profile for the currently logged in user
    *  but does not popup a login dialog
    *  @param newProfile  the profile object
    *  @param doFireConnectionChangedEvent boolean flag to tell method whether
    *                          to do a <code>fireConnectionChangedEvent</code>;
    *                          mainly used by calls from the above
    *                          "setProfile(ConfigXML newProfile)" method, which
    *                          already does its own fireConnectionChangedEvent,
    *                          so needs to disable that call here.
    *
    */
    public void setProfileDontLogin(ConfigXML newProfile,
                                    boolean doFireConnectionChangedEvent)
    {
        this.profile = newProfile;

        // load the certificate/identity for the given profile
        fireUsernameChangedEvent();

        // Load basic profile information
        String profilename = profile.get("profilename", 0);
        if (!profileConfig.set("current_profile", 0, profilename)) {
            boolean success = profileConfig.insert("current_profile", profilename);
        }
        profileConfig.save();

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
        String profileDir = ConfigXML.getConfigDirectory() + File.separator +
                config.get("profile_directory", 0);
        String currentProfile = getCurrentProfileName();
        if (!newProfileName.equals(currentProfile)) {
            String newProfilePath = profileDir + File.separator +
                newProfileName + File.separator + newProfileName + ".xml";
            try {
                ConfigXML newProfile = new ConfigXML(newProfilePath);
                if (doLogin) {
                	setProfile(newProfile);
                }
                else {
                	setProfileDontLogin(newProfile, true);
                }
            } catch (FileNotFoundException fnf) {
                Log.debug(5, "Profile not found!");
            }
        }
    }

    private void deleteProfile(String profileName)
    {
        String profileDir = ConfigXML.getConfigDirectory() + File.separator +
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
      String profileDir = ConfigXML.getConfigDirectory() + File.separator +
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
      String profileDir = ConfigXML.getConfigDirectory() + File.separator +
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
            JOptionPane.showMessageDialog(initialFrame,
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
                JOptionPane.showMessageDialog(initialFrame,
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
     * Alter the application bar to show Morpho's "about" window
     * For cross-platform compilation, we include the stubs
     * @see:
     * https://developer.apple.com/library/mac/#/legacy/mac/library/samplecode/AppleJavaExtensions/Introduction/Intro.html
     */
    private void initMacApplication() {
    	try {
	    	Application a = Application.getApplication();
	        AboutHandler handler = new AboutHandler() {
				@Override
				public void handleAbout(AboutEvent event) {
	                SplashFrame sf = new SplashFrame();
	                sf.setVisible(true);
				}
	        };
			a.setAboutHandler(handler);
		} catch (Exception e) {
			// this only works in OS X and any errors can be ignored
			Log.debug(20, "Cannot load OS X application customizations");
			e.printStackTrace();
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
          
            //check for override config dir
            if (args.length > 0) {
            	String dir = args[0];
            	ConfigXML.setConfigDirectory(dir);
            }
            
            //initialize the config
            initializeConfiguration();

            // Set up logging, possibly to a file as appropriate
            initializeLogging(config);
            
            // set up access list
            initializeAccessList();
   	         
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
            
            // create the local data store
            morpho.setLocalDataStoreService(new LocalDataStoreService(morpho));
            
            // create the remote DataONE data store
            morpho.setDataONEDataStoreService(new DataONEDataStoreService(morpho));
         
            // Load the current profile and log in
            morpho.loadProfile(morpho);
            
            // Correct the invalid eml 201 documents
            CorrectEML201DocsFrame correctFrame = new CorrectEML201DocsFrame(morpho);
            correctFrame.doCorrection();
            //we may change the profile content in the previous step, so morpho needs to reload it.
            morpho.loadProfile(morpho);
            
            // Create id-filename map for the morpho 1.x object directories
            IdentifierUpdaterFrame updateFrame = new IdentifierUpdaterFrame();
            updateFrame.run();
            //we may change the profile content in the previous step, so morpho needs to reload it.
            morpho.loadProfile(morpho);
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

    /**
     * This method is for junit tests to create a morpho instance
     */
    public static void createMorphoTestInstance() {
      try{
        initializeConfiguration();
        // Create a new instance of our application
        Morpho morpho = new Morpho(config);
        // Load the current profile and log in
        thisStaticInstance = morpho;
        morpho.loadProfile(morpho);     
      } catch (Exception e) {
        Log.debug(10,"error creating Morpho Instance");
      }
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
                EcpAuthentication.getInstance().establishConnection();
            }
        };
        GUIAction connectItemAction =
            new GUIAction(/*"Login/Logout"*/ Language.getInstance().getMessage("Login/Logout"),
            		null, connectCommand);
        connectItemAction.setToolTipText("Login/Logout...");
        connectItemAction.setMenuItemPosition(11);
        connectItemAction.setSeparatorPosition(SEPARATOR_PRECEDING);
        connectItemAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
        controller.addGuiAction(connectItemAction);


        Command profileCommand = new CreateNewProfileCommand();

        GUIAction profileItemAction =
            new GUIAction(/*"New profile..."*/ Language.getInstance().getMessage("NewProfile"),
            		null, profileCommand);
        profileItemAction.setToolTipText("New Profile...");
        profileItemAction.setMenuItemPosition(12);
        profileItemAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
        controller.addGuiAction(profileItemAction);

        Command switchCommand = new Command() {
            public void execute(ActionEvent e) {
                switchProfile();
            }
        };
        GUIAction switchItemAction =
            new GUIAction(/*"Switch profile..."*/ Language.getInstance().getMessage("SwitchProfile"),
            		null, switchCommand);
        switchItemAction.setToolTipText("Switch Profile...");
        switchItemAction.setMenuItemPosition(13);
        switchItemAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
        controller.addGuiAction(switchItemAction);
        
        Command removeCommand = new Command() {
            public void execute(ActionEvent e) {
                removeProfile();
            }
        };
        GUIAction removeProfileAction =
            new GUIAction(/*"Remove profile..."*/ Language.getInstance().getMessage("RemoveProfile"),
            		null, removeCommand);
        removeProfileAction.setToolTipText("Remove Profile...");
        removeProfileAction.setMenuItemPosition(14);
        removeProfileAction.setSeparatorPosition(SEPARATOR_FOLLOWING);
        removeProfileAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
        controller.addGuiAction(removeProfileAction);

        Command prefsCommand = new Command() {
            public void execute(ActionEvent e) {
                setPreferences();
            }
        };
        GUIAction prefsItemAction =
            new GUIAction(/*"Set preferences..."*/ Language.getInstance().getMessage("SetPreferences"),
            		null, prefsCommand);
        prefsItemAction.setToolTipText("Set Preferences...");
        prefsItemAction.setMenuItemPosition(15);
        prefsItemAction.setSeparatorPosition(SEPARATOR_FOLLOWING);
        prefsItemAction.setMenu(FILE_MENU_LABEL, FILEMENUPOSITION);
        controller.addGuiAction(prefsItemAction);
        

        Command exitCommand = new Command() {
            public void execute(ActionEvent event) {
                exitApplication();
            }
        };
        GUIAction exitItemAction = new GUIAction(/*"Exit"*/ Language.getInstance().getMessage("Exit"),
        		null, exitCommand);
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
            new GUIAction(/*"About..."*/ Language.getInstance().getMessage("About") + Language.getInstance().getMessage("..."),
            		null, aboutCommand);
        aboutItemAction.putValue(Action.SHORT_DESCRIPTION, "About Morpho");
        aboutItemAction.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/About16.gif")));
        aboutItemAction.putValue("menuPosition", new Integer(1));
        aboutItemAction.setMenu(HELP_MENU_LABEL, HELPMENUPOSITION);
        controller.addGuiAction(aboutItemAction);
        
        Command guideCommand = new MorphoGuideCommand();
        GUIAction guideItemAction =
            new GUIAction(/*"Morpho User Guide..."*/ Language.getInstance().getMessage("MorphoUserGuide"),
            		null, guideCommand);
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
            new GUIAction(/*"Intro to Metadata..."*/ Language.getInstance().getMessage("IntroToMetadata"),
            		null, mdIntroCommand);
        mdIntroItemAction.putValue(Action.SHORT_DESCRIPTION, "Intro to Metadata");
        mdIntroItemAction.putValue(Action.SMALL_ICON,
                new ImageIcon(getClass().
                getResource("/toolbarButtonGraphics/general/Help16.gif")));
        mdIntroItemAction.putValue("menuPosition", new Integer(3));
        mdIntroItemAction.setMenu(HELP_MENU_LABEL, HELPMENUPOSITION);
        controller.addGuiAction(mdIntroItemAction);

        Command mdEMLSpecCommand = new HelpCommand("eml_index");
        GUIAction mdEMLSpecItemAction =
            new GUIAction(/*"EML Specifications..."*/ Language.getInstance().getMessage("EMLSpecifications"),
            		null, mdEMLSpecCommand);
        mdEMLSpecItemAction.putValue(Action.SHORT_DESCRIPTION, "EML Specifications");
        mdEMLSpecItemAction.putValue(Action.SMALL_ICON,
            new ImageIcon(getClass().
            getResource("/toolbarButtonGraphics/general/Help16.gif")));
        mdEMLSpecItemAction.putValue("menuPosition", new Integer(4));
        mdEMLSpecItemAction.setMenu(HELP_MENU_LABEL, HELPMENUPOSITION);
        controller.addGuiAction(mdEMLSpecItemAction);

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
    	// TODO: logout?
        //dds.logOut();
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
                /*"Select from existing profiles:"*/ Language.getInstance().getMessage("SelectExistingProfile") + ":",
                /*"Input"*/ Language.getInstance().getMessage("Input"),
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
      UIController.getInstance().updateAllStatusBars();

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
     * Fire off notifications for all of the registered ConnectionListeners when
     * the connection status changes.
     */
    public void fireConnectionChangedEvent()
    {
        for (int i = 0; i < connectionRegistry.size(); i++) {
            ConnectionListener listener =
                    (ConnectionListener)connectionRegistry.elementAt(i);
            if (listener != null) {
                listener.connectionChanged(dds.isConnected());
            }
        }
    }


    /**
     * Fire off notifications for all of the registered ConnectionListeners when
     * the username is changed.
     */
    public void fireUsernameChangedEvent()
    {

        for (int i = 0; i < connectionRegistry.size(); i++) {
            ConnectionListener listener =
                    (ConnectionListener)connectionRegistry.elementAt(i);
            if (listener != null) {
                listener.usernameChanged(dds.getUserName());
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
    
    private static void fileCopy(File src, File dest) throws Exception {
    	FileInputStream input = new FileInputStream(src);
        FileOutputStream output = new FileOutputStream(dest);
        byte buf[] = new byte[4096];
        int len = 0;
        while ((len = input.read(buf, 0, 4096)) != -1) {
            output.write(buf, 0, len);
        }
        input.close();
        output.close();
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

    public static void initializeAccessList() {
    	try {
	    	File accessFile = new File( ConfigXML.getConfigDirectory() + "/" + ACCESS_FILE_NAME);
	    	if (!accessFile.exists()) {
	        	File sourceAccessFile = new File( LIBDIR + "/" + ACCESS_FILE_NAME);
	        	fileCopy(sourceAccessFile, accessFile);
	    	}
    	} catch(Exception e) {
    		Log.debug(5, "Could not initialize access list: " + e.getMessage());
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
                Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(profileFile), Charset.forName("UTF-8")));
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
                JOptionPane.showMessageDialog(
                	initialFrame,
                    /*"You must create a profile in order to configure Morpho correctly. "*/
                	Language.getInstance().getMessage("Morpho.CreateProfileWarning_1") + " \n"
                	/*+"Please restart Morpho and try again."*/
                	+ Language.getInstance().getMessage("Morpho.CreateProfileWarning_2")
                	);
                exitApplication();
            }
        } else {
            String profileName = profileDir + File.separator +
                currentProfile + File.separator +
                currentProfile + ".xml";
            ConfigXML profile = new ConfigXML(profileName);
            profile.set("searchnetwork", 0, "false", true);
            setProfileDontLogin(profile, true);
        }
    }

    /** Switch profiles (from one existing profile to another) */
	private void removeProfile()
	{
    dds.logOut();
    String currentProfile = getCurrentProfileName();

    String[] allProfilesList = getProfilesList();
    List<String> profileList = new ArrayList<String>();
    for (int selection = 0; selection < allProfilesList.length; selection++) {
        if (currentProfile.equals(allProfilesList[selection])) {
            continue;
        }
        profileList.add(allProfilesList[selection]);
    }

    // Pop up a dialog with the choices
    MorphoFrame frame = UIController.getInstance().getCurrentActiveWindow();
    String selectedProfile = (String)JOptionPane.showInputDialog(frame,
            /*"Select profile to delete:"*/ Language.getInstance().getMessage("SelectProfileToDelete") + ":",
            /*"Input"*/ Language.getInstance().getMessage("SelectProfileToDelete"),
            JOptionPane.INFORMATION_MESSAGE, null,
            profileList.toArray(), 0);

    // double check that we are not deleting what we shouldn't
    if (null != selectedProfile) {
        if (currentProfile.equals(selectedProfile)) {
            Log.debug(0, "Cannot delete current profile!");
        } else {
        	int deleteContents = 
        		JOptionPane.showConfirmDialog(
        				frame, 
        				/*"Are you sure you want to delete this profile: "*/ Language.getInstance().getMessage("Warning.DeleteProfile_1") 
        				+ "\n"  
        				+ selectedProfile 
        				+ "\n" 
        				+/*"\nALL data will be discarded."*/ Language.getInstance().getMessage("Warning.DeleteProfile_2") + "\n" 
        				+/*"\nThis action is not undoable."*/ Language.getInstance().getMessage("Warning.DeleteProfile_3"), 
        				"DESTRUCTIVE ACTION!", JOptionPane.YES_NO_OPTION);
        	if (deleteContents == JOptionPane.YES_OPTION) {
	            deleteProfile(selectedProfile);
	            // close all old windows
	            cleanUpFrames();
	            Log.debug(9, 
	            		  /*"Removed profile: "*/ Language.getInstance().getMessage("Warning.DeleteProfile_4") + ": "
	            		  + selectedProfile);
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

