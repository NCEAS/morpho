/**
 *  '$RCSfile: InitialScreen.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-12-11 06:22:21 $'
 * '$Revision: 1.1 $'
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
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JLabel;
//import javax.swing.ImageIcon;

import edu.ucsb.nceas.morpho.Morpho;

import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;

import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;


/**
 *  A panel that contains the initial welcome screen.  The screen comprises 3 
 *  "control" boxes - one for displaying and manipulating the currently-active 
 *  profile, one for logging into the network, and one for providing shortcuts 
 *  to common initial actions: create new package, open existing package, search 
 *  for package)
 */
public class InitialScreen extends JPanel 
//                                         implements MouseListener,
//                                                    ChangeListener, 
//                                                    StateChangeListener, 
//                                                    StoreStateChangeEvent,
//                                                    EditingCompleteListener
{

    private final Morpho morpho;
  
    public InitialScreen(Morpho morpho)
    {
        this.setLayout(new BorderLayout(0,0));
        this.morpho = morpho;
        init();
        addLeftPanels();
        addRightPicture();
    }
    
    private void init() {
    
        this.setBackground(UISettings.INIT_SCRN_MAIN_BG_COLOR);
        this.setPreferredSize(UISettings.INIT_SCRN_MAIN_DIMS);
        this.setOpaque(true);
    }
    
    private void addLeftPanels()
    {
        Box leftPanelLayoutBox = Box.createHorizontalBox();
        
        Box leftPanelContainer = Box.createVerticalBox();
        
        LeftPanel profilePanel = new LeftPanel(wrapTitleHTML("Current Profile: "),
                                    UISettings.INIT_SCRN_PROFILE_PANEL_HEIGHT);
        LeftPanel loginPanel  = new LeftPanel(wrapTitleHTML("Network Status:"), 
                                    UISettings.INIT_SCRN_LOGIN_PANEL_HEIGHT);
        LeftPanel dataPanel   = new LeftPanel(wrapTitleHTML("Work With Your Data..."), 
                                    UISettings.INIT_SCRN_DATA_PANEL_HEIGHT);

        leftPanelContainer.add(
            Box.createVerticalStrut(UISettings.INIT_SCRN_LEFT_PANELS_PADDING));
        leftPanelContainer.add(profilePanel);
        leftPanelContainer.add(
            Box.createVerticalStrut(UISettings.INIT_SCRN_LEFT_PANELS_PADDING));
        leftPanelContainer.add(loginPanel);
        leftPanelContainer.add(
            Box.createVerticalStrut(UISettings.INIT_SCRN_LEFT_PANELS_PADDING));
        leftPanelContainer.add(dataPanel);
        leftPanelContainer.add(Box.createVerticalGlue());
        
        leftPanelLayoutBox.add(
            Box.createHorizontalStrut(UISettings.INIT_SCRN_LEFT_PANELS_PADDING));
        leftPanelLayoutBox.add(leftPanelContainer);
        leftPanelLayoutBox.add(
            Box.createHorizontalStrut(UISettings.INIT_SCRN_LEFT_PANELS_PADDING));
            
        this.add(leftPanelLayoutBox, BorderLayout.WEST);
        
        populateProfilePanel(profilePanel);
        populateLoginPanel(loginPanel);
        populateDataPanel(dataPanel);
    }
    
    private void populateProfilePanel(LeftPanel panel) 
    {
    }
    
    private void populateLoginPanel(LeftPanel panel) 
    {
    }
    
    private void populateDataPanel(LeftPanel panel) 
    {
        //get handles to plugins so we can then get Command objects...
        ServiceProvider providr = null;
        ServiceController services = ServiceController.getInstance();
        
        DataPackageInterface dataPackagePlugin = null;
        try {
              providr = services.getServiceProvider(DataPackageInterface.class);
              dataPackagePlugin = (DataPackageInterface)providr;
        } catch (ServiceNotHandledException snhe) {
              Log.debug(6, snhe.getMessage());
        }
        
        QueryRefreshInterface queryPlugin = null;
        try {
              providr = services.getServiceProvider(QueryRefreshInterface.class);
              queryPlugin = (QueryRefreshInterface)providr;
        } catch (ServiceNotHandledException snhe) {
              Log.debug(6, snhe.getMessage());
        }
        
        //now get Command objects:
        
        Command newPkgCmd  = null;
        Command openPkgCmd = null;
        Command searchCmd  = null;
        try {
            newPkgCmd = dataPackagePlugin.getCommandObject(
                                DataPackageInterface.NEW_DATAPACKAGE_COMMAND);
        } catch (ClassNotFoundException cnfe) {
            Log.debug(6, "InitialScreen.java - Command not found \n"
                +"while getting NEW_DATAPACKAGE_COMMAND: " + cnfe.getMessage());
        }
        try {
            openPkgCmd = queryPlugin.getCommandObject(
                                QueryRefreshInterface.OPEN_DATAPACKAGE_COMMAND);
        } catch (ClassNotFoundException cnfe) {
            Log.debug(6, "InitialScreen.java - Command not found \n"
                +"while getting OPEN_DATAPACKAGE_COMMAND: " + cnfe.getMessage());
        }
        try {
            searchCmd = queryPlugin.getCommandObject(
                                QueryRefreshInterface.SEARCH_COMMAND);
        } catch (ClassNotFoundException cnfe) {
            Log.debug(6, "InitialScreen.java - Command not found \n"
                +"while getting SEARCH_COMMAND: " + cnfe.getMessage());
        }
        
        GUIAction newAction 
                    = new GUIAction(UISettings.NEW_DATAPACKAGE_LINK_TEXT,
                                    UISettings.NEW_DATAPACKAGE_ICON, newPkgCmd);
        newAction.setRolloverSmallIcon(
                                    UISettings.NEW_DATAPACKAGE_ICON_ROLLOVER);
        panel.addToRow1(new HyperlinkButton(newAction));
        panel.addToRow1(Box.createHorizontalGlue());

        GUIAction openAction 
                    = new GUIAction(UISettings.OPEN_DATAPACKAGE_LINK_TEXT,
                                    UISettings.OPEN_DATAPACKAGE_ICON,openPkgCmd);
        openAction.setRolloverSmallIcon(
                                    UISettings.OPEN_DATAPACKAGE_ICON_ROLLOVER);
        panel.addToRow2(new HyperlinkButton(openAction));
        panel.addToRow2(Box.createHorizontalGlue());

        GUIAction searchAction 
                    = new GUIAction(UISettings.SEARCH_LINK_TEXT,
                                    UISettings.SEARCH_ICON, searchCmd);
        searchAction.setRolloverSmallIcon(
                                    UISettings.SEARCH_ICON_ROLLOVER);
        panel.addToRow3( new HyperlinkButton(searchAction));
        panel.addToRow3(Box.createHorizontalGlue());
    }


    //convenience method to add HTML tags to the passed text    
    // <html>**UISettings.INIT_SCRN_LEFT_PANELS_TITLE_FONT_HTML** 
    // (e.g. <font color="#ffffff" size="2">)
    // **textToWrap**
    // </font></html>    
    StringBuffer buff = new StringBuffer();
    //
    private String wrapTitleHTML(String textToWrap) {

       buff.delete(0,buff.length());
       buff.append("<html><p align=\"left\">");
       buff.append(UISettings.INIT_SCRN_LEFT_PANELS_TITLE_FONT_HTML);
       buff.append(textToWrap);
       buff.append("</font></p></html>");
       return buff.toString();
    }

    
    private void addRightPicture() 
    {
    
    }
    
    
    /**
     *  This class represents one of the panels to the left of the initial 
     *  screen.  The panel can be visualized as a 4-row, 1-column table, 
     *  where the first row is a colored titlebar, beneath which are three rows 
     *  for "content".  ie:
     *
     *  <pre>
     *  |-----------------------------------|
     *  |******* colored title bar *********|
     *  |-----------------------------------|
     *  |  first content row                |
     *  |-----------------------------------|
     *  |  second  content row              |
     *  |-----------------------------------|
     *  |  third  content row               |
     *  |-----------------------------------|
     *  <pre>
     *
     */
    class LeftPanel extends JPanel
    {
        private JLabel  titleLabel;
        private Box     topBox, middleBox, bottomBox;
            
        /**
         *  constructor (see Class explanation above)
         *
         *  @param title the title to add to the title bar
         *  @param height the height of the entire panel in pixels
         */
        public LeftPanel(String title, int height) {
            super(new BorderLayout());
            Dimension thisDim 
                = new Dimension(UISettings.INIT_SCRN_LEFT_PANELS_WIDTH, height);
                
            this.setBackground(UISettings.INIT_SCRN_LEFT_PANELS_BG_COLOR);
            this.setMinimumSize(thisDim);
            this.setPreferredSize(thisDim);
            this.setMaximumSize(thisDim);
            this.setOpaque(true);

            titleLabel = new JLabel(title);
            titleLabel.setMinimumSize(
                            UISettings.INIT_SCRN_LEFT_PANELS_TITLE_DIMS);
            titleLabel.setPreferredSize(
                            UISettings.INIT_SCRN_LEFT_PANELS_TITLE_DIMS);
            titleLabel.setMaximumSize(
                            UISettings.INIT_SCRN_LEFT_PANELS_TITLE_DIMS);
            titleLabel.setBackground(
                            UISettings.INIT_SCRN_LEFT_PANELS_TITLE_BG_COLOR);
            titleLabel.setOpaque(true);
            
            this.add(titleLabel, BorderLayout.NORTH);
            
            Box vertBox = Box.createVerticalBox();
            this.add(vertBox, BorderLayout.CENTER);
            
            topBox    = Box.createHorizontalBox();
            middleBox = Box.createHorizontalBox();
            bottomBox = Box.createHorizontalBox();
            
            vertBox.add(Box.createVerticalGlue());
            vertBox.add(topBox);
            vertBox.add(Box.createVerticalGlue());
            vertBox.add(middleBox);
            vertBox.add(Box.createVerticalGlue());
            vertBox.add(bottomBox);
            vertBox.add(Box.createVerticalGlue());
        }
        
        public void setTitle(String title) {
        
            titleLabel.setText(title);
            this.validate();
        }
        
        /**
         *  add passed Component to top box (see explanation above)
         *
         *  @param comp the Component to add
         */
        public void addToRow1(Component comp) { topBox.add(comp);    }
        
        /**
         *  add passed Component to middle box (see explanation above)
         *
         *  @param comp the Component to add
         */
        public void addToRow2(Component comp) { middleBox.add(comp); }
        
        /**
         *  add passed Component to bottom box (see explanation above)
         *
         *  @param comp the Component to add
         */
        public void addToRow3(Component comp) { bottomBox.add(comp); }
        
    }
    
    public static void main(String args[])
    {
        javax.swing.JFrame testFrame 
                        = new javax.swing.JFrame("Morpho Initial Screen Test");
        
        testFrame.getContentPane().add(new InitialScreen(null));
        // Register listeners
        testFrame.addWindowListener(
            new WindowAdapter() {
                public void windowClosing(WindowEvent event) { System.exit(0); }
            });
        
        testFrame.setLocation(200,200);
        testFrame.setVisible(true);
        testFrame.pack();
    }
}