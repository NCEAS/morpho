/**
 *  '$RCSfile: MorphoFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-08-17 21:50:33 $'
 * '$Revision: 1.15.2.2 $'
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

import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * The MorphoFrame is a Window in the Morpho application containing the standard
 * menus and toolbars. Overall state of the application is synchronized across
 * MorphoFrames so that when the UI changes do a user action it is propogated to
 * all frames as appropriate. Each plugin can create a MorphoFrame by asking the
 * UIController for a new instance.
 *
 * @author   jones
 */
public class MorphoFrame extends JFrame
{
    private JMenuBar menuBar;
    private TreeMap menuList;
    private TreeMap menuActions;
    private TreeMap toolbarActions;
    private JToolBar morphoToolbar;
    private StatusBar statusBar;
    private ProgressIndicator indicator;
    private Component contentComponent;
    private Dimension contentAreaSize;
    private final MorphoFrame instance;
    private static int menuBarHeight = 0;
    private boolean busyFlag =false;

    // A string indicating which frame it is 
    public static final String SEARCHRESULTFRAME = "searchResultFrame";
    public static final String DATAPACKAGEFRAME = "dataPackageFrame";
  

    /**
     * Creates a new instance of MorphoFrame, but is private because 
     * getInstance() should be used to obtain new MorphoFrame instances
     */
    private MorphoFrame()
    {
        super("Morpho - Data Management for Ecologists");
        setVisible(false);
        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(UISettings.FRAME_AND_TASKBAR_ICON);
        JLayeredPane layeredPane = getLayeredPane();
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(0, 0));

        // Set up the progress indicator
        ImageIcon bfly = null;
        ImageIcon flapping = null;
        try {
            bfly = new javax.swing.ImageIcon(
                    getClass().getResource("Btfly.gif"));
            flapping = new javax.swing.ImageIcon(
                    getClass().getResource("Btfly4.gif"));
        } catch (Exception w) {
            Log.debug(9, "Error in loading images");
        }
        indicator = new ProgressIndicator(bfly, flapping);
        layeredPane.add(indicator, JLayeredPane.DEFAULT_LAYER);

        // Set up the menu bar
        menuList = new TreeMap();
	    menuActions = new TreeMap();
        menuBar = new JMenuBar();
        setMenuBar(menuBar);

        // Set up the toolbar
        toolbarActions = new TreeMap();
        int indicatorHeight = (int)indicator.getSize().getHeight();
        int menuHeight = getMenuHeight();
        int toolbarHeight = indicatorHeight - menuHeight;
        Log.debug(50, "(indicator, menu, tool) = (" + indicatorHeight + "," + 
                menuHeight + "," + toolbarHeight + ")");
        morphoToolbar = new JToolBar();
        morphoToolbar.setFloatable(false);
        morphoToolbar.setPreferredSize(new Dimension(1,toolbarHeight));
        getContentPane().add(BorderLayout.NORTH, morphoToolbar);

        // Set up and add a StatusBar
        statusBar = new StatusBar();
        getContentPane().add(BorderLayout.SOUTH, statusBar);

        // Register listeners
        this.addWindowListener(
            new WindowAdapter() {
                public void windowActivated(WindowEvent e) 
                {
                    Log.debug(50, "Processing window activated event");
                    UIController controller = UIController.getInstance();
                    controller.setCurrentActiveWindow(instance);
                    controller.refreshWindows();
                    indicator.repaint();
                } 
                public void windowClosing(WindowEvent event)
                {
                    Object object = event.getSource();
                    if (object == MorphoFrame.this) {
                        close();
                    }
                }
            });
        this.addComponentListener(
            new ComponentAdapter() { 
                public void componentResized (ComponentEvent e) 
                {
                    updateProgressIndicatorLocation();
                } 
            }); 

        // Size the window properly
        pack();
        instance = this;
    }
    
    /**
     * Create a new instance and set its default size
     */
    public static MorphoFrame getInstance() {
        MorphoFrame window = new MorphoFrame();
        window.calculateDefaultSizes();
        window.addDefaultContentArea();
        return window;
    }

    /**
     * Set the content pane of the main Morpho window to display the component
     * indicated. Note that this will replace the current content pane, and so
     * only one plugin should call this routine.
     *
     * @param comp  the component to display
     */
    public void setMainContentPane(Component comp)
    {
        // Create a panel to display the plugin if requested
        if (comp != null) {
            Container contentPane = getContentPane();
            if (contentComponent != null) {
                contentPane.remove(contentComponent);
            }
            contentComponent = comp;
            contentPane.add(BorderLayout.CENTER, comp);
            pack();
        } else {
            Log.debug(5, "Component was null so I could not set it!");
        }
    }
    
    /**
     * Get the contentComponent of MorphoFrame
     */
    public Component getContentComponent()
    {
      return contentComponent;
    }

    /**
     * Set the menu bar when it needs to be changed.  This is mainly called by
     * the UIController when it is managing the menus.
     */
    public void setMenuBar(JMenuBar newMenuBar)
    {
        this.setJMenuBar(newMenuBar);
        this.getLayeredPane().invalidate();
    }

    /**
     * Add a GUIAction to the menu and toolbar for this frame. 
     * If the menu already exists, the actions are added to it.
     * Each time an action is added, it is stored in the appropriate
     * menu and toolbar lists (menuList, menuActions, toolbarActions)
     * in the proper order and then the menus and toolbars are rebuilt
     * from these data structures.
     *
     * @param action the action to be added to the menus and toolbar
     */
    public void addGuiAction(GUIAction action)
    {
        String menuName = action.getMenuName();
        int menuPosition = action.getMenuPosition();
        JMenu currentMenu = null;
        Integer currentPosition = null;
        boolean menuExists = false;
        // Check if a menu already exists
        Iterator menuPositions = menuList.keySet().iterator();
        while (menuPositions.hasNext()) {
            currentPosition = (Integer)menuPositions.next();
            currentMenu = (JMenu)menuList.get(currentPosition);
            if (currentMenu != null ) {
                String currentMenuName = currentMenu.getText();
                if (currentMenuName.equals(menuName)) {
                    menuExists = true;
                    break;
                }
            }
        }
        
        // If not, add a new menu with that name in the right position
        if (!menuExists) {
            currentMenu = new JMenu(menuName);
            menuList.put(new Integer(menuPosition), currentMenu);
            TreeMap actionList = new TreeMap();
            menuActions.put(menuName, actionList);
            // Rebuild a new menu bar
            JMenuBar newBar = new JMenuBar();
            Iterator menuPositionList = menuList.keySet().iterator();
            while (menuPositionList.hasNext()) {
                Integer position = (Integer)menuPositionList.next();
                JMenu menu = (JMenu)menuList.get(position);
                newBar.add(menu);
            }
            setMenuBar(newBar);
        }
        
        // add the action to the list in which it belongs
        int menuPos = action.getMenuItemPosition();
        Integer menuPosInteger = new Integer(menuPos);
        TreeMap actionList = (TreeMap)menuActions.get(menuName);
        Vector actionVector = null;
        if (actionList.containsKey(menuPosInteger)) {
            actionVector = (Vector)actionList.get(menuPosInteger);
        } else {
            actionVector = new Vector();
            actionList.put(menuPosInteger, actionVector);
        }
        actionVector.add(action);
        rebuildMenu(currentMenu, actionList);

        // add the action to the toolbar if its position > 0
	int toolbarPosition = action.getToolbarPosition();
        if (toolbarPosition >= 0) {

            Integer position = new Integer(toolbarPosition);
            Vector toolbarActionVector = null;
            if (toolbarActions.containsKey(position)) {
                toolbarActionVector = (Vector)toolbarActions.get(position);
            } else {
                toolbarActionVector = new Vector();
                toolbarActions.put(position, toolbarActionVector);
            }
            toolbarActionVector.add(action);
            rebuildToolbar();
        }
    }

    /**
     * Remove a GUIAction from the menu and toolbar for this frame. 
     *
     * @param action the action to be removed from the menus and toolbar
     */
    public void removeGuiAction(GUIAction action)
    {
        // Remove the action from the menus
        Iterator menus = menuList.values().iterator();
        while (menus.hasNext()) {
            JMenu currentMenu = (JMenu)menus.next();
            String currentMenuName = currentMenu.getText();
            TreeMap actionList = (TreeMap)menuActions.get(currentMenuName); 
            Iterator actionListVectors = actionList.values().iterator();
            while (actionListVectors.hasNext()) {
                Vector actionVector = (Vector)actionListVectors.next();
                if (actionVector.contains(action)) {
                    actionVector.remove(action);
                    rebuildMenu(currentMenu, actionList);
                }  
            }
        }
        
        // Remove the action from the toolbar if present
        Iterator toolbarVectors = toolbarActions.values().iterator();
        while (toolbarVectors.hasNext()) {
            Vector actionVector = (Vector)toolbarVectors.next();
            if (actionVector.contains(action)) {
                actionVector.remove(action);
                rebuildToolbar();
            }
        }
    }

    /**
     * Set the ProgressIndicator to either the busy or notBusy state.
     *
     * @param isBusy boolean value indidcating whether the indicator should
     *               be marked as busy
     */
    public void setBusy(boolean isBusy)
    {
      // Only isBusy is different to current status(busyFlag, it change status
      if(busyFlag ^ isBusy)
      {
        indicator.setBusyFlag(isBusy);
        SwingUtilities.invokeLater(indicator);
        busyFlag = isBusy;
      }
    }
    
    /**
     * Set the StatusBar to display a message
     *
     * @param message the message to display in the StatusBar
     */
    public void setMessage(String message)
    {
        statusBar.setMessage(message);
    }

    /**
     * Returns the default size that the content area should be on this
     * screen.  This is determined by considering the screen size, the sizes
     * of the window insets, and sizes of internal components of the
     * MorphoFrame such as the ProgressIndicator and StatusBar.
     */
    public Dimension getDefaultContentAreaSize()
    {
        return contentAreaSize;
    }

    /**
     * Get the StatusBar to update its status
     *
     * @returns the StatusBar instance for this window
     */
    protected StatusBar getStatusBar()
    {
        return statusBar;
    }

    /**
     * Rebuild a menu based on the set of GUIActions that belong in the
     * menu.
     */
    private void rebuildMenu(JMenu currentMenu, TreeMap actionList)
    {
        currentMenu.removeAll();
        Iterator actionPositionList = actionList.keySet().iterator();
        while (actionPositionList.hasNext()) {
            Integer position = (Integer)actionPositionList.next();
            Vector actionVector = (Vector)actionList.get(position);

            for (int i=0; i<actionVector.size(); i++) {
                GUIAction action = (GUIAction)actionVector.elementAt(i);
                String hasDefaultSep = action.getSeparatorPosition();
                // Append everything else at the bottom of the menu
                if (hasDefaultSep != null &&
                    hasDefaultSep.equals(UIController.SEPARATOR_PRECEDING)) {
                    currentMenu.addSeparator();
                }
                JMenuItem currentItem = currentMenu.add(action);
                currentItem.setAccelerator(
                    (KeyStroke)action.getValue(Action.ACCELERATOR_KEY));
                if (hasDefaultSep != null &&
                    hasDefaultSep.equals(UIController.SEPARATOR_FOLLOWING)) {
                    currentMenu.addSeparator();
                }
            }
        }
    }
    
    /**
     * Rebuild the toolbar based on the set of GUIActions that belong in the
     * toolbar.
     */
    private void rebuildToolbar()
    {
        morphoToolbar.removeAll();
        Iterator actionPositionList = toolbarActions.keySet().iterator();
        while (actionPositionList.hasNext()) {
            Integer position = (Integer)actionPositionList.next();
            Vector actionVector = (Vector)toolbarActions.get(position);
            for (int i=0; i<actionVector.size(); i++) {
                GUIAction action = (GUIAction)actionVector.elementAt(i);
                JButton toolButton = morphoToolbar.add(action);
                String toolTip  = action.getToolTipText();
                if (toolTip != null) {
                    toolButton.setToolTipText(toolTip);
                }
            }
        }
    }

    /** 
     * Properly locate the progress indicator
     * when the window size changes
     */
    private void updateProgressIndicatorLocation()
    {
        Log.debug(50, "Resized Window"); 
        Dimension indicatorSize = indicator.getSize();
        Dimension cpSize = getContentPane().getSize();
        indicator.setLocation(
                (int) (cpSize.getWidth() - indicatorSize.getWidth()), 0);
    }

    /** 
     * close the window when requested 
     */
    private void close()
    {
        this.setVisible(false);
        UIController controller = UIController.getInstance();
        controller.removeWindow(this);
        this.dispose();

        Component comp = getContentComponent();
        if ((DataViewContainerPanel.class).isInstance(comp)) {
          ((DataViewContainerPanel)comp).dispose();
        }
        comp = null;
        System.gc();

    }

    /**
     * Determine the default sizes for various window components based on
     * the screen size
     */
    private void calculateDefaultSizes()
    {
        Log.debug(50,"Screen size (w,h): ("+UISettings.CLIENT_SCREEN_WIDTH+", " 
                                           +UISettings.CLIENT_SCREEN_HEIGHT+")");
        double windowWidth  = UISettings.DEFAULT_WINDOW_WIDTH;
        double windowHeight = UISettings.DEFAULT_WINDOW_HEIGHT;
        // determine default content size
        Insets insets = getInsets();
        Log.debug(50, "Insets (t, b, l, r): (" + insets.top + ", " +
                insets.bottom + ", " + insets.left + ", " + insets.right + ")");
        double indicatorHeight = indicator.getSize().getHeight();
        double statusHeight = statusBar.getSize().getHeight();
        double contentWidth = windowWidth - insets.left - insets.right;
        double contentHeight = windowHeight - insets.top - insets.bottom -
            indicatorHeight - statusHeight;
        contentAreaSize = new Dimension((int)contentWidth, (int)contentHeight);
        Log.debug(50, "Content size (w, h): (" + contentAreaSize.getWidth() +
                ", " + contentAreaSize.getHeight() + ")");
    }

    /**
     * Create a content pane with the default size
     */
    private void addDefaultContentArea()
    {
        // Put in a default content area that is blank
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        Component hstrut = Box.createHorizontalStrut(
                (int)contentAreaSize.getWidth());
        Component vstrut = Box.createVerticalStrut(
                (int)contentAreaSize.getHeight());
        content.add(hstrut);
        content.add(vstrut);
        content.setBackground(Color.darkGray);
        setMainContentPane(content);
    }

    /**
     * Return the height of the menu bar for use in layout calculations.
     *
     * @return the height of a menu bar
     */
    private int getMenuHeight() {
        if (menuBarHeight <= 0) {
            JMenuBar testBar = new JMenuBar();
            JMenu testMenu = new JMenu("Test");
            testBar.add(testMenu);
            menuBarHeight = (int)testBar.getPreferredSize().getHeight();
        }
        return menuBarHeight;
    }    
    
}
