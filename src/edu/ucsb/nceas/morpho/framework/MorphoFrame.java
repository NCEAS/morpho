/**
 *  '$RCSfile: MorphoFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-15 22:44:36 $'
 * '$Revision: 1.1.2.10 $'
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
    private JMenuBar morphoMenuBar;
    private JPanel toolbarPanel;
    private JToolBar morphoToolbar;
    private StatusBar statusBar;
    private ProgressIndicator indicator;
    private Dimension screenSize;
    private Dimension windowSize;
    private Dimension contentAreaSize;

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
        //setTitle("Morpho - Data Management for Ecologists");

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
        //int menuHeight = (int)morphoMenuBar.getSize().getHeight();
        int menuHeight = 24;
        int toolbarHeight = indicatorHeight - menuHeight;
        Log.debug(10, "(indicator, menu, tool) = (" + indicatorHeight + "," + 
                menuHeight + "," + toolbarHeight + ")");
        toolbarPanel = new JPanel();
        //toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.X_AXIS));
        morphoToolbar = new JToolBar();
//        morphoToolbar.add(Box.createVerticalStrut(toolbarHeight));
        morphoToolbar.add(Box.createRigidArea(new Dimension(1,toolbarHeight)));
        //toolbarPanel.add(morphoToolbar);

        Action cutItemAction = new AbstractAction("Cut") {
            public void actionPerformed(ActionEvent e) {
                Log.debug(9, "Cut is not yet implemented.");
            }
        };
        cutItemAction.putValue(Action.SMALL_ICON, 
                        new ImageIcon(getClass().
            getResource("/toolbarButtonGraphics/general/Cut16.gif")));
        JButton toolButton = morphoToolbar.add(cutItemAction);
        //toolButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolButton = morphoToolbar.add(cutItemAction);
        //toolButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolButton = morphoToolbar.add(cutItemAction);
        //toolButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolButton = morphoToolbar.add(cutItemAction);
        //toolButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        morphoToolbar.add(
            Box.createHorizontalGlue());
        toolButton = morphoToolbar.add(cutItemAction);
        //toolbarPanel.add(
        morphoToolbar.add(
            Box.createHorizontalStrut((int)(indicator.getSize().getWidth())));
        //getContentPane().add(BorderLayout.NORTH, toolbarPanel);
        getContentPane().add(BorderLayout.NORTH, morphoToolbar);

        // Set up and add a StatusBar
        statusBar = StatusBar.getInstance();
        getContentPane().add(BorderLayout.SOUTH, statusBar);

        // Register listeners
        this.addWindowListener(
            new WindowAdapter() {
                public void windowActivated(WindowEvent e) 
                {
                    Log.debug(50, "Processing window activated event");
                    UIController.getInstance().refreshWindows();
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
            getContentPane().add(BorderLayout.CENTER, comp);
            //comp.invalidate();
            //invalidate();
            pack();
        } else {
            Log.debug(5, "Component was null so I could not set it!");
        }
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
     * Set the ProgressIndicator to either the busy or notBusy state.
     *
     * @param isBusy boolean value indidcating whether the indicator should
     *               be marked as busy
     */
    public void setBusy(boolean isBusy)
    {
        indicator.setBusy(isBusy);
    }

    /**
     * Set the StatusBar to display a message
     *
     * @param message the message to display in the StatusBar
     */
    public void setStatusMessage(String message)
    {
        // Not implemented yet because StatusBar is currently a singleton
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
