/**
 *  '$RCSfile: MorphoFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-05 18:21:11 $'
 * '$Revision: 1.7 $'
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

import edu.ucsb.nceas.morpho.util.Log;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.ClassCastException;
import java.lang.reflect.*;
import java.net.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;

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
    private JToolBar morphoToolbar;
    private StatusBar statusBar;
    private ProgressIndicator indicator;
    private Component contentComponent;
    private Dimension screenSize;
    private Dimension windowSize;
    private Dimension contentAreaSize;
    private final MorphoFrame instance;
    private static int menuBarHeight = 0;
    private boolean busyFlag =false;

    // Constants (probably should be set in a property file, but what the hell)
    private static final int MAX_WINDOW_WIDTH = 1024;
    private static final int MAX_WINDOW_HEIGHT = 768;
    private static final int SCREEN_PADDING = 50;

    /**
     * Creates a new instance of MorphoFrame, but is private because 
     * getInstance() should be used to obtain new MorphoFrame instances
     */
    private MorphoFrame()
    {
        super("Morpho - Data Management for Ecologists");
        setVisible(false);
        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);

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

        // Set up the toolbar
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
    
    private int getMenuHeight() {
        if (menuBarHeight <= 0) {
            menuBarHeight 
             = (int)UIController.createMenuBar().getPreferredSize().getHeight();
        }
        return menuBarHeight;
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
     * Add new toolbar actions by adding all of the components that
     * are currently not on our toolbar.
     */
    public void addToolbarActions(Vector toolbarList)
    {
        int toolbarActionCount = toolbarList.size();
        int componentCount = morphoToolbar.getComponentCount();
        if ((toolbarActionCount > 0) && (toolbarActionCount > componentCount)) {
            for (int i=componentCount; i < toolbarActionCount; i++) {
                Action currentAction = (Action)toolbarList.elementAt(i);
                JButton toolButton = morphoToolbar.add(currentAction);
                String toolTip  = 
                    (String)currentAction.getValue(Action.SHORT_DESCRIPTION);
                if (toolTip != null) {
                    toolButton.setToolTipText(toolTip);
                }
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
        indicator.setBusy(isBusy);
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
    }

    /**
     * Determine the default sizes for various window components based on
     * the screen size
     */
    private void calculateDefaultSizes()
    {
        // determine screen size
        Toolkit tk = Toolkit.getDefaultToolkit();
        screenSize = tk.getScreenSize();
        Log.debug(50, "Screen size (w, h): (" + screenSize.getWidth() +
                ", " + screenSize.getHeight() + ")");

        // determine default window size
        double windowWidth;
        double windowHeight;
        if (screenSize.getWidth() >= MAX_WINDOW_WIDTH) {
            windowWidth = MAX_WINDOW_WIDTH - SCREEN_PADDING;
        } else {
            windowWidth = screenSize.getWidth() - SCREEN_PADDING;
        }
        if (screenSize.getHeight() >= MAX_WINDOW_HEIGHT) {
            windowHeight = MAX_WINDOW_HEIGHT - SCREEN_PADDING;
        } else {
            windowHeight = screenSize.getHeight() - SCREEN_PADDING;
        }
        windowSize = new Dimension((int)windowWidth, (int)windowHeight);
        Log.debug(50, "Window size (w, h): (" + windowSize.getWidth() +
                ", " + windowSize.getHeight() + ")");

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
}
