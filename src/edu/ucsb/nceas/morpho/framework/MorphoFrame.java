/**
 *  '$RCSfile: MorphoFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-14 00:17:28 $'
 * '$Revision: 1.1.2.4 $'
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

    /**
     * Creates a new instance of MorphoFrame
     *
     */
    public MorphoFrame()
    {
        setVisible(false);
        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);

        JLayeredPane layeredPane = getLayeredPane();
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(0, 0));
        setTitle("Morpho - Data Management for Ecologists");

        // Set up the menu bar
        morphoMenuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        fileMenu.add(newItem);
        morphoMenuBar.add(fileMenu);
        setJMenuBar(morphoMenuBar);

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
        int menuHeight = (int)morphoMenuBar.getSize().getHeight();
        menuHeight = 24;
        int toolbarHeight = indicatorHeight - menuHeight;
        Log.debug(10, "(indicator, menu, tool) = (" + indicatorHeight + "," + 
                menuHeight + "," + toolbarHeight + ")");
        toolbarPanel = new JPanel();
        //toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.X_AXIS));
        morphoToolbar = new JToolBar();
        morphoToolbar.add(Box.createVerticalStrut(toolbarHeight));
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

        // Put in a default content area that is blank
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        Component hstrut = Box.createHorizontalStrut(600);
        Component vstrut = Box.createVerticalStrut(800);
        content.add(hstrut);
        content.add(vstrut);
        content.setBackground(Color.darkGray);
        setMainContentPane(content);

        // Set up and add a StatusBar
        statusBar = StatusBar.getInstance();
        getContentPane().add(BorderLayout.SOUTH, statusBar);

        // Register listeners
        this.addWindowListener(
            new WindowAdapter() {
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
            comp.invalidate();
            invalidate();
        } else {
            Log.debug(5, "Component was null so I could not set it!");
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
    }
}

