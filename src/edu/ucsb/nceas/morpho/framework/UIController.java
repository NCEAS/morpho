/**
 *  '$RCSfile: UIController.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-21 16:54:05 $'
 * '$Revision: 1.39 $'
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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.w3c.dom.Document;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;


/**
 * The UIController handles the state of the morpho menu and toolbars so
 * that they are synchronized across the various plugins.  This is a
 * singleton class because only one instance is ever needed.  Plugins that need
 * a new frame for a window should call UIController.newWindow().  The single
 * instance of UIController can be obtained statically using getInstance().
 *
 * @author   jones
 */
public class UIController
{
    private static UIController controller;
    private static Morpho morpho;
    private static MorphoFrame currentActiveWindow;

    /** A hash of cloned GUIActions keyed on the original GUIAction */
    private static Hashtable<GUIAction, Vector<GUIAction>> guiActionClones;

    /** A hash of windows keyed on its Window menu GUIAction */
    private static Hashtable<GUIAction, MorphoFrame> windowList;

    /** A hash of windows keyed on an associated GUIAction clone */
    private static Hashtable<GUIAction, MorphoFrame> actionCloneWindowAssociation;

    //keep the record that the entity wizards are running (base on docid)
    private static HashSet<String> entityWizardRunningRecorder = null;
    //keep the record that the package (or entity) wizard are idle 
    //(user clicked finish/cancel button. cancel button is only for entity)
    private static HashSet<String>packageEntityWizardIdleRecorder = null;
    // A hashtable to store the pair: submenu-path,
    // such as synchronize - file/synchroize
    private static int count = 0; // count create how many frames
    private static boolean newDataPackageWziardWindowIsActive = false;

    private static int windowXcoord, windowYcoord;
    private static int windowXcoordUpperBound, windowYcoordUpperBound;
    private static int WIN_CASCADE_MOD_X;
    private static int WIN_CASCADE_MOD_Y;

    // Constants
    public static final String SEPARATOR_PRECEDING = "separator_preceding";
    public static final String SEPARATOR_FOLLOWING = "separator_following";
    public static final String PULL_RIGHT_MENU = "pull_right_menu";
    public static final String YES = "yes";
    public static final String MENU_PATH = "menu_path";
    

    private AbstractDataPackage wizardTempDataPackage = null;
    //private AbstractDataPackage currentPackage = null;
    //private boolean assignCurrentPackage = false;
  /**
   * Creates a new instance of UIController, but is private because this is a
   * singleton.
   *
   * @param morpho Morpho
   */
  private UIController(Morpho morpho)
    {
        this.morpho = morpho;
        windowList = new Hashtable<GUIAction, MorphoFrame>();
        actionCloneWindowAssociation = new Hashtable<GUIAction, MorphoFrame>();
        guiActionClones = new Hashtable<GUIAction, Vector<GUIAction>>();

        entityWizardRunningRecorder = new HashSet<String>();
        packageEntityWizardIdleRecorder = new HashSet<String>();
    }


  /**
   * Initialize the single instance of the UIController, creating it if needed.
   *
   * @param morpho Morpho
   * @return UIController
   */
  public static UIController initialize(Morpho morpho)
    {
        if (controller == null) {
            controller = new UIController(morpho);
        }
        return controller;
    }

    /**
     * Get the single instance of the UIController, creating it if needed.
     * If the controller has not yet been created yet (using the
     * method "initialize(Morpho)", then this method returns null.
     *
     * @returns the single instance of the UIController
     */
    public static UIController getInstance()
    {
        return controller;
    }


  /**
   * This method is called by plugins to get a new window that is an instance
   * of MorphoFrame. They can set the content of the window to a panel of
   * their choice using MorphoFrame.setMainContentPane().
   *
   * @param windowName the initial title for the window
   * @return MorphoFrame
   */
  public MorphoFrame addWindow(String windowName)
    {
        String title = "Untitled";
        if (windowName != null) {
            title = windowName;
        }
        Log.debug(30, "Adding window: " + title);
        MorphoFrame window = MorphoFrame.getInstance();
        window.setTitle(title);

        registerWindow(window);

        updateStatusBar(window.getStatusBar());

        setCurrentActiveWindow(window);
        setWindowLocation(window);
        window.toFront();
        count++;
        return window;
    }


  public MorphoFrame addHiddenWindow(String windowName)
    {
        String title = "Untitled";
        if (windowName != null) {
            title = windowName;
        }
        Log.debug(30, "Adding window: " + title);
        MorphoFrame window = MorphoFrame.getHiddenInstance();
        window.setTitle(title);

        registerWindow(window);

        updateStatusBar(window.getStatusBar());

        setCurrentActiveWindow(window);
        setWindowLocation(window);
        window.toFront();
        count++;
        return window;
    }


    //sets the location of the frame to follow a cascade layout
    private void setWindowLocation(MorphoFrame window)
    {
        if (count==0) {

            initWindowCoords(window);

            Log.debug(45,"UIController.initWindowCoords(): windowXcoordUpperBound="
                                                       +windowXcoordUpperBound);
            Log.debug(45,"UIController.initWindowCoords(): windowYcoordUpperBound="
                                                       +windowYcoordUpperBound);
            Log.debug(45,"UIController.initWindowCoords(): WIN_CASCADE_MOD_X="
                                                            +WIN_CASCADE_MOD_X);
            Log.debug(45,"UIController.initWindowCoords(): WIN_CASCADE_MOD_Y="
                                                            +WIN_CASCADE_MOD_Y);
        }


        // don't do this if this is the first window after the welcome screen,
        // because we need to replace the welcome screen at the same location...
        if (count!=1) {
            if (WIN_CASCADE_MOD_X > 0) {
                windowXcoord = windowXcoordUpperBound
                    + ((  windowXcoord - windowXcoordUpperBound
                    + UISettings.WINDOW_CASCADE_X_OFFSET) % WIN_CASCADE_MOD_X);
            }
            if (WIN_CASCADE_MOD_Y > 0) {
                windowYcoord = windowYcoordUpperBound
                    + ((  windowYcoord - windowYcoordUpperBound
                    + UISettings.WINDOW_CASCADE_Y_OFFSET) % WIN_CASCADE_MOD_Y);
            }
        }

        Log.debug(45,"UIController.setWindowLocation(): ("  +windowXcoord+", "
                                                            +windowYcoord+")");
        window.setLocation(windowXcoord,windowYcoord);
    }

    /**
     * This method is called by plugins to update window that is an
     * instance of MorphoFrame.
     *
     * @param window the window will be given new name
     * @param title the new name will give to window
     */
    public void updateWindow(MorphoFrame window, String title)
    {
      // Get title from old frame
      String oldTitle = window.getTitle();
      if ((title == null) || (title.equals(oldTitle))) {
          return;
      }

      // Remove the frame from window list
      removeWindow(window);
      // Set frame new title
      window.setTitle(title);

      putFrameIntoWindowList(window);

      if (getCurrentActiveWindow()==null) {
          setCurrentActiveWindow(window);
      }
    }

    /**
     * This method is called to de-register a Window that
     * the plugin has created.  The window is removed from the "Windows"
     * menu.
     *
     * @param window the window to be removed from the framework
     */
    public void removeWindow(MorphoFrame window)
    {
        removeWindowWithoutCheckingEmpty(window);
        // Exit application if no windows remain
        if (windowList.isEmpty()) {
            morpho.exitApplication();
        }
    }

    /*
     * A method to remove a window and the method without checking window list
     * if it is empty
     */
    private void removeWindowWithoutCheckingEmpty(MorphoFrame window)
    {
      Log.debug(50, "Removing window.");

        // Look up the Action for this window
        GUIAction currentAction = null;
        Enumeration<GUIAction> keys = windowList.keys();
        while (keys.hasMoreElements()) {
            currentAction = keys.nextElement();
            MorphoFrame savedWindow =
                (MorphoFrame)windowList.get(currentAction);
            if (savedWindow == window) {
                break;
            } else {
                currentAction = null;
            }
        }

        // Remove the action from the GUIAction list
        removeGuiAction(currentAction);

        // If the window is the currentActiveWindow, change it
        if (getCurrentActiveWindow()==window) {
            setCurrentActiveWindow(null);
        }

        int cnt = 0;
        // need to remove all reference to window from actionCloneWindowAssociation
        GUIAction currentAction1 = null;
        Enumeration<GUIAction> keys1 = actionCloneWindowAssociation.keys();
        Vector<GUIAction> matches = new Vector<GUIAction>();
        while (keys1.hasMoreElements()) {
          currentAction1 = keys1.nextElement();
          MorphoFrame sWindow = windowList.get(currentAction1);
          if (sWindow == window) {
            matches.addElement(currentAction1);
         Log.debug(10,"found matching window!"+cnt++);
          }
        }
        Enumeration<GUIAction> en = matches.elements();
        while (en.hasMoreElements()) {
          actionCloneWindowAssociation.remove(en.nextElement());
        Log.debug(10,"removed window"+cnt++);
        }

        // Remove the window from the windowList
        try {
            windowList.remove(currentAction);
          Log.debug(10,"removed from window list");
            System.gc();
        } catch(NullPointerException npe2) {
            Log.debug(20, "Window already removed from registry.");
        }


    }

    /**
     * Method to remove all windows in window list
     */
    public void removeAllWindows()
    {
      Enumeration<MorphoFrame> frameList = windowList.elements();
      while (frameList.hasMoreElements())
      {
        MorphoFrame frame = frameList.nextElement();
        removeWindowWithoutCheckingEmpty(frame);
        frame.dispose();
        frame = null;
      }
      // set count 0
      count = 0;
    }

    /*
     * remove all 'clean' frames and return a vector of 'dirty' frames
     */
    public Vector removeCleanWindows() {
      Vector res = new Vector();
      Enumeration<MorphoFrame> frameList = windowList.elements();
      while (frameList.hasMoreElements())
      {
        MorphoFrame frame = frameList.nextElement();
        if (frame.isDirty()) {
          res.addElement(frame);
        } else {
          removeWindowWithoutCheckingEmpty(frame);
          frame.dispose();
          frame = null;
        }
      }
    count = res.size();
    return res;
    }

    /**
     * Add a menu item and optionally a toolbar button by registering
     * an instance of GUIAction.  The GUIAction will be cloned once for
     * each of the MorphoFrame instances that currently exist or that are
     * created.
     *
     * @param action the GUIAction instance describing the menu item
     */
    public void addGuiAction(GUIAction action)
    {
        // for each window, clone the action, add it to the vector of
        // clones for this GUIAction, add it to the clone/window
        // association hash, and send the clone to the window
        Vector<GUIAction> cloneList = new Vector<GUIAction>();
        guiActionClones.put(action, cloneList);
        Enumeration<MorphoFrame> windows = windowList.elements();
        while (windows.hasMoreElements()) {
            MorphoFrame window = windows.nextElement();
            GUIAction clone = action.cloneAction();
            cloneList.addElement(clone);
            actionCloneWindowAssociation.put(clone, window);
            window.addGuiAction(clone);
        }
    }

    /**
     * Remove a menu item and its toolbar button for a particular
     * instance of GUIAction.
     *
     * @param action the GUIAction instance to be removed
     */
    public void removeGuiAction(GUIAction action)
    {
    	if (guiActionClones != null && action != null)
    	{
            Vector<GUIAction> cloneList = guiActionClones.get(action);
            guiActionClones.remove(action);
            for (int i=0; i< cloneList.size(); i++) {
               GUIAction clone = cloneList.elementAt(i);
               MorphoFrame window = getMorphoFrameContainingGUIAction(clone);
               window.removeGuiAction(clone);
            }
    	}
    }

    /**
     * return the MorphoFrame that contains the GUIAction clone provided
     *
     * @param clone       the GUIAction clone for which the parent MorphoFrame
     *                    will be returned
     * @return            the MorphoFrame which is the parent of this GUIAction
     */
    public static MorphoFrame getMorphoFrameContainingGUIAction(GUIAction clone)
    {
        if (clone==null || actionCloneWindowAssociation==null) return null;
        return actionCloneWindowAssociation.get(clone);
    }

    /**
     *  This method can be used if you have the original GUIAction instance and
     *  want to get the existing clone of it that was put into a particular
     *  MorphoFrame by UIController.
     .
     *
     * @param action      the GUIAction whose clone is sought
     * @param frame       the MorphoFrame in which the clone is nested
     * @return            the GUIAction object that is a clone of the passed
     *                    GUIAction, and that is nested inside the passed
     *                    MorphoFrame
     */
    public static GUIAction getGUIActionCloneUsedByMorphoFrame(GUIAction action,
                                                              MorphoFrame frame)
    {
        if (action==null || frame==null) return null;

        //given the action, get its list of clones

        //guiActionClones is a hashtable where each key is the original
        //GUIAction instance, and its corresponding value is a Vector of clones
        Vector<GUIAction> cloneList = guiActionClones.get(action);

        //cloneList therefore holds all possible clones of the passed GUIAction
        for (int i=0; i< cloneList.size(); i++) {
            GUIAction clone = cloneList.elementAt(i);
            //actionCloneWindowAssociation is a hash each clone (the keys) and the
            //window that holds it (the values)

            //therefore check each clone in turn to see if it belongs to
            //passed MorphoFrame:
            if (actionCloneWindowAssociation.get(clone)==frame) {
                return clone;
            }
        }
        return null;
    }

    /**
     * refresh all visible the windows that are in the windowList.
     */
    public void refreshWindows()
    {
        Enumeration<MorphoFrame> windows = windowList.elements();
        while (windows.hasMoreElements()) {
            windows.nextElement().validate();
        }
    }

    /**
     * updates status bar in response to changes in connection parameters
     */
    public void updateAllStatusBars()
    {
        Enumeration<MorphoFrame> windows = windowList.elements();
        while (windows.hasMoreElements()) {
            StatusBar statusBar = windows.nextElement().getStatusBar();
            updateStatusBar(statusBar);
        }
    }

    /**
     * set currently active window
     *
     *  @param window the currently active MorphoFrame window
     */
    public void setCurrentActiveWindow(MorphoFrame window)
    {
        currentActiveWindow = window;
    }

    /**
     * get currently active window
     *
     *  @return  the currently active MorphoFrame window
     */
    public MorphoFrame getCurrentActiveWindow()
    {
        return currentActiveWindow;
    }



    /**
     * get AbstractDataPackage from currently active window
     *
     *  @return  the AbstractDataPackage from the currently active MorphoFrame
     * window; returns null if current window is null, or if current window does
     * not contain an AbstractDataPackage
     */
    public MorphoDataPackage getCurrentAbstractDataPackage() {
    
	      if (isWizardRunning() && newDataPackageWziardWindowIsActive) {
	        // *temporary* AbstractDataPackage that is used to store wizard data
	        // (for references use) while the wizard is running and the window is active.
	        Log.debug(50, "\n\n***********************************"
	                  + "getCurrentAbstractDataPackage() -"
	                  + " isWizardRunning() == true. pkg = \n"
	                  + this.wizardTempDataPackage);
	        MorphoDataPackage mdp = new MorphoDataPackage();
	        mdp.setAbstractDataPackage(wizardTempDataPackage);
	        return mdp;
	      }
	
	      MorphoFrame morphoFrame = this.getCurrentActiveWindow();
	
	      if (morphoFrame == null) {
	
	        Log.debug(20, "UIController.getCurrentAbstractDataPackage() - "
	                  +"morphoFrame==null, returning NULL");
	        return null;
	      }
	      Log.debug(50, "\n\n========================="
            + "getCurrentAbstractDataPackage() from morpho frame!!!");
	      return morphoFrame.getMorphoDataPackage();
  
  }
    
    /**
     * Specifies weather the new data package wizard window is active.
     * Note: morpho only allow one new data package wizard run.
     * @param active true if the wizard window is active.
     */
    public void setNewDataPackageWizardWindowIsActive(boolean active)
    {
      //Log.debug(30, "~~~~~~~~~~~~~The status of Activating the New Data Package Wizard is "+active); 
      newDataPackageWziardWindowIsActive = active;
    }
    
    /**
     * Set a AbstractDataPackage as currentPackage;
     * @param currentPackage
     */
    /*public void setCurrentAbstractDataPackage(AbstractDataPackage currentPackage)
    {
    	this.currentPackage = currentPackage;
    }*/
    
    /**
     * If set this to true, we will get the current package through setCurrentAbstractDataPackage
     * @param assignPackage
     */
    /*public void setAssignPackage(boolean assignPackage)
    {
    	this.assignCurrentPackage = assignPackage;
    }*/

		/**
     * get AbstractDataPackage from currently active window. Even if a wizard is running
		 *	currently, it returns the ADP of the currently open package for which the wizard is 
		 *	running
     *
     *  @return  the AbstractDataPackage from the currently active MorphoFrame
     * window; returns null if current window is null, or if current window does
     * not contain an AbstractDataPackage
     */
    public AbstractDataPackage getCurrentExistingAbstractDataPackage() {

      MorphoFrame morphoFrame = this.getCurrentActiveWindow();

      if (morphoFrame == null) {

        Log.debug(20, "UIController.getCurrentAbstractDataPackage() - "
                  +"morphoFrame==null, returning NULL");
        return null;
      }
      return morphoFrame.getMorphoDataPackage().getAbstractDataPackage();
    }
		
		
    /**
     * called by DataPackageWizardPlugin whenever wizard starts running, and is
     * passed a temporary AbstractDataPackage that is used to store wizard data
     * (for references use) - this ADP will be returned by any calls to
     * getCurrentAbstractDataPackage() while the wizard is running. <em>NOTE
     * that this datapackage will be discarded when the wizard is done, because it
     * may not be complete. It is not used to create the new datapackage!!<em>
     * When the wizard ends, it should call
     *
     * @param tempDataPackage AbstractDataPackage -
     * a temporary AbstractDataPackage that is used to store wizard data
     * (for references use) - this ADP will be returned by any calls to
     * getCurrentAbstractDataPackage() while the wizard is running. <em>NOTE
     * that this datapackage will be discarded when the wizard is done, because it
     * may not be complete. It is not used to create the new datapackage!!<em>
     */
    public void setWizardIsRunning(AbstractDataPackage tempDataPackage) {

      this.wizardTempDataPackage = tempDataPackage;
    }
    
    /**
     * Add a docid to entityWizardRunningRecorder
     * @param docid the id will be added
     */
    public void addDocidToEntityWizardRunningRecorder(String docid)
    {
      if(docid != null && !docid.trim().equals(""))
      {
        entityWizardRunningRecorder.add(docid);
      }
    }
    
    /**
     * Remove a docid from entityWizardRunningRecorder
     * @param docid the id will be removed
     */
    public void removeDocidFromEntityWizardRunningRecorder(String docid)
    {
      if(docid != null && !docid.trim().equals(""))
      {
        entityWizardRunningRecorder.remove(docid);
      }
    }
    
    /**
     * Check if an entity wizard is running for this docid
     * @param docid the docid which will be check
     * @return true if there is an entity wizard is running against the docid
     */
    public boolean isEntityWizardRunning(String docid)
    {
      return entityWizardRunningRecorder.contains(docid);
    }
    
    /**
     * Check if any entity wizard is running on morpho
     * @return true if there is any entity wizard is running
     */
    public boolean isAnyEntityWizardRunning()
    {
      if(entityWizardRunningRecorder != null)
      {
        return !entityWizardRunningRecorder.isEmpty();
      }
      else
      {
        return false;
      }
    }
    
    /**
     * Adds a docid to idle wizard recorder. It happens when
     * user click finish/cancel button. Now the wizard is not running, but
     * tracing document still exists
     * @param docid the docid which has a idle wizard
     */
    public void addDocidToIdleWizardRecorder(String docid)
    {
      //Log.debug(5, "adds docid "+docid+" from idle wizard recorder");
      if(docid != null && !docid.trim().equals(""))
      {      
        packageEntityWizardIdleRecorder.add(docid);
      }
    }
    
    /**
     * Removes a docid from the idle wizard recorder
     * @param docid the docid 
     */
    public void removeDocidFromIdleWizardRecorder(String docid)
    {
      //Log.debug(5, "removes docid "+docid+" from idle wizard recorder");
      if(docid != null && !docid.trim().equals(""))
      {       
        packageEntityWizardIdleRecorder.remove(docid);
      }
    }
    
    /**
     * Is the ldle wizard recorder has the docid
     * @param docid
     * @return true if the idle wizard recorder has it
     */
    public boolean isWizardIdle(String docid)
    {
      return packageEntityWizardIdleRecorder.contains(docid);
    }
    


    /**
     * called by DataPackageWizardPlugin whenever wizard starts running, and is
     * passed a temporary AbstractDataPackage that is used to store wizard data
     * (for references use) - this ADP will be returned by any calls to
     * getCurrentAbstractDataPackage() while the wizard is running. <em>NOTE
     * that this datapackage will be discarded when the wizard is done, because it
     * may not be complete. It is not used to create the new datapackage!!<em>
     * When the wizard ends, it should call
     *
     * @param tempDataPackage AbstractDataPackage -
     * a temporary AbstractDataPackage that is used to store wizard data
     * (for references use) - this ADP will be returned by any calls to
     * getCurrentAbstractDataPackage() while the wizard is running. <em>NOTE
     * that this datapackage will be discarded when the wizard is done, because it
     * may not be complete. It is not used to create the new datapackage!!<em>
     */
    public void setWizardNotRunning() {

      this.wizardTempDataPackage = null;
    }

    /**
     * called by DataPackageWizardPlugin whenever wizard starts running, and is
     * passed a temporary AbstractDataPackage that is used to store wizard data
     * (for references use) - this ADP will be returned by any calls to
     * getCurrentAbstractDataPackage() while the wizard is running
     *
     * @return the AbstractDataPackage from the currently active MorphoFrame
     *   window; returns null if current window is null, or if current window
     *   does not contain an AbstractDataPackage
     * @param tempDataPackage AbstractDataPackage
     */
    public boolean isWizardRunning() {

      return (this.wizardTempDataPackage != null);
    }
    
    /**
     * Gets the package id of the wizard temp data package. If wizard is not running,
     * null will be returned.
     * @return the package id
     */
    public String getRunningWizardPackageID()
    {
      String packageID = null;
      if(this.wizardTempDataPackage != null)
      {
        packageID =this.wizardTempDataPackage.getAccessionNumber();
      }
      return packageID;
    }


    /**
     * get Morpho
     *
     * @return Morpho
     */
    public static Morpho getMorpho() {

      return morpho;
    }


  /**
   * launches the XML tree editor, rooted at the subtree node denoted by the
   * passed string and index, to edit the datapackage associated with the
   * current MorphoFrame
   *
   * @param subtreeRootNodeName String
   * @param subtreeRootIndex int (zero-relative)
   */
  public void launchEditorAtSubtreeForCurrentFrame(
      String subtreeRootNodeName, int subtreeRootIndex) {

    EditorInterface editor = null;
    try {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider =
          services.getServiceProvider(EditorInterface.class);
      editor = (EditorInterface)provider;
    } catch (Exception ee) {
      Log.debug(0, "Error acquiring editor plugin: " + ee.getMessage());
      ee.printStackTrace();
      return;
    }
    DataViewContainerPanel panel = null;
    AbstractDataPackage adp = null;
    MorphoFrame frame = this.getCurrentActiveWindow();

    if (frame != null)panel = frame.getDataViewContainerPanel();
    if (panel != null)adp = panel.getMorphoDataPackage().getAbstractDataPackage();

    Document thisdoc = adp.getMetadataNode().getOwnerDocument();
    String id = adp.getAccessionNumber();
    String location = adp.getLocation();

    editor.openEditor(thisdoc, id, location, panel,
                      subtreeRootNodeName, subtreeRootIndex);
  }


  /**
   * Register a window by creating an action and adding it to the
   * list of windows for the application. All existing windows are
   * updated with new menus that reflect the menu change.
   *
   * @param window the window which should be added
   */
  private void registerWindow(MorphoFrame window) {
    if (window == null) {
      Log.debug(50, "Window is null, create failed!");
        }
        // clone all of the existing GUIActions for this new window
        Enumeration<GUIAction> actionList = guiActionClones.keys();
        while (actionList.hasMoreElements()) {
            GUIAction action = actionList.nextElement();
            Log.debug(50, "Cloning action: " + action.toString());
            GUIAction clone = action.cloneAction();
            Vector<GUIAction> cloneList = guiActionClones.get(action);
            cloneList.addElement(clone);
            actionCloneWindowAssociation.put(clone, window);
            Log.debug(50, "Clone menu name is: " + clone.getMenuName());
            window.addGuiAction(clone);
        }

        putFrameIntoWindowList(window);
    }
     
      /*
      * Method to put a morpho frame into window list
      */
     private void putFrameIntoWindowList(MorphoFrame window)
      {
         // add a new menu item for this window by greating a GUIAction for it
        String title = window.getTitle();
        Command command = new Command() {
            public void execute(ActionEvent e) {
                JMenuItem source = (JMenuItem)e.getSource();
                Action firedAction = source.getAction();
                GUIAction original =
                    ((GUIAction)firedAction).getOriginalAction();
                MorphoFrame window1 =
                    (MorphoFrame)windowList.get(original);
                window1.toFront();
            }
        };
        GUIAction action = new GUIAction(title, null, command);
        action.setMenu(Morpho.WINDOW_MENU_LABEL, Morpho.WINDOWMENUPOSITION);
        action.setToolTipText("Select Window");
        windowList.put(action, window);
        addGuiAction(action);
      }
     
    /**
     * Updates a single StatusBar to reflect the current network state
     *
     * @param statusBar the status bar to be updated
     */
    private void updateStatusBar(StatusBar statusBar)
    {
        statusBar.setConnectStatus(morpho.getDataONEDataStoreService().getNetworkStatus());
        statusBar.setLoginStatus(morpho.getDataONEDataStoreService().isConnected() &&
                morpho.getDataONEDataStoreService().getNetworkStatus());
        statusBar.setSSLStatus(morpho.getDataONEDataStoreService().getSslStatus());
    }


   //initialize values for window locations
   private void initWindowCoords(MorphoFrame frame)
   {
        int xQttyTopLeft, yQttyTopLeft, qttyTopLeft;
        int xQttyLoRt,    yQttyLoRt,    qttyLoRt;
        int fWidth  = frame.getWidth();
        int fHeight = frame.getHeight();

        windowXcoord = ((UISettings.CLIENT_SCREEN_WIDTH  - fWidth) / 2);
        windowYcoord = ((UISettings.CLIENT_SCREEN_HEIGHT
                       - UISettings.TASKBAR_HEIGHT - fHeight) / 2);

        // UPPER LEFT LIMIT:
        //how many frames can we fit between centered frame and
        //top left of screen?
        xQttyTopLeft = windowXcoord/UISettings.WINDOW_CASCADE_X_OFFSET;
        yQttyTopLeft = windowYcoord/UISettings.WINDOW_CASCADE_Y_OFFSET;
        //which one (x or y) is the minimum (i.e. limiting) number?
        qttyTopLeft  = (xQttyTopLeft > yQttyTopLeft)? yQttyTopLeft : xQttyTopLeft;

        //so these are the top-leftmost coords for any window:
        windowXcoordUpperBound = windowXcoord
                    - (qttyTopLeft * UISettings.WINDOW_CASCADE_X_OFFSET);
        windowYcoordUpperBound = windowYcoord
                    - (qttyTopLeft * UISettings.WINDOW_CASCADE_Y_OFFSET);

        // LOWER RIGHT LIMIT:
        //how many frames can we fit between centered frame and
        //bottom right of screen (minus task-bar area)?
        int loRightXcoord = windowXcoord + fWidth;
        int loRightYcoord = windowYcoord + fHeight;
        xQttyLoRt = (UISettings.CLIENT_SCREEN_WIDTH  - loRightXcoord)
                                        /UISettings.WINDOW_CASCADE_X_OFFSET;
        yQttyLoRt = (UISettings.CLIENT_SCREEN_HEIGHT - loRightYcoord)
                                        /UISettings.WINDOW_CASCADE_Y_OFFSET;
        //which one (x or y) is the minimum (i.e. limiting) number?
        qttyLoRt  = (xQttyLoRt > yQttyLoRt)? yQttyLoRt : xQttyLoRt;

        int modInt = qttyTopLeft + qttyLoRt;

        WIN_CASCADE_MOD_X = modInt * UISettings.WINDOW_CASCADE_X_OFFSET;
        WIN_CASCADE_MOD_Y = modInt * UISettings.WINDOW_CASCADE_Y_OFFSET;

        //do this because algorithm will add cascade offset next:
        if (WIN_CASCADE_MOD_X > 0) {
            windowXcoord -= UISettings.WINDOW_CASCADE_X_OFFSET;
        }
        if (WIN_CASCADE_MOD_Y > 0) {
            windowYcoord -= UISettings.WINDOW_CASCADE_Y_OFFSET;
        }
   }


  /**
   * method to display the new package after changes have been made
   * with no change in location
   * @param adp AbstractDataPackage
   */
  public static void showNewPackageNoLocChange(MorphoDataPackage mdp) {
    showNewPackage_base(mdp);   
  }

  /**
   * method to display the new package after changes have been made
   *
   * @param adp AbstractDataPackage
   */
  public static void showNewPackage(MorphoDataPackage mdp) {
	  AbstractDataPackage adp = mdp.getAbstractDataPackage();
    adp.setLocation("");
    mdp.setAbstractDataPackage(adp);
    showNewPackage_base(mdp);   
  }
  
  private static void showNewPackage_base(MorphoDataPackage mdp) {
    Point pos = null;
    Dimension size = null;
    MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if(morphoFrame != null) {
        pos = morphoFrame.getLocation();
        size = morphoFrame.getSize();
    }
    
    try {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider =
                services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      dataPackage.openHiddenNewDataPackage(mdp, null);
      UIController controller = UIController.getInstance();
      MorphoFrame newMorphoFrame = controller.getCurrentActiveWindow();
      if(pos != null) {
          newMorphoFrame.setLocation(pos);
      }
      if(size != null) {
          newMorphoFrame.setSize(size);
      }
      
      newMorphoFrame.setVisible(true);
      if(morphoFrame != null) {
          morphoFrame.setVisible(false);

          controller.removeWindow(morphoFrame);
          morphoFrame.dispose();
      }
     
    }
    catch (ServiceNotHandledException snhe) {
      Log.debug(6, snhe.getMessage());
      if(morphoFrame != null) {
          morphoFrame.setVisible(true);
      }
     
    }
  }

}
