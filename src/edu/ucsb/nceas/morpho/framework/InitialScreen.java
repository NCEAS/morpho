/**
 *  '$RCSfile: InitialScreen.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-12-13 19:51:15 $'
 * '$Revision: 1.6 $'
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

import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

import edu.ucsb.nceas.morpho.Morpho;

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
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
{

    private final Morpho        morpho;
    private final MorphoFrame   parentFrame;
    private final JLabel        currentProfileLDAPLabel = new JLabel();
    
    private HyperlinkButton logoutLink;
    private Command         logoutCommand;
    private LeftPanel       profilePanel;
    private LeftPanel       loginPanel;
    
    
    private boolean prevLoginStatus = true; //initially set to true so login  
                                            //panel will be updated - see
                                            // updateLoginStatus() method 
    
    public InitialScreen(Morpho morpho, MorphoFrame parentFrame)
    {
        this.setLayout(new BorderLayout(0,0));
        this.morpho      = morpho;
        this.parentFrame = parentFrame;
        init();
        addLeftPanels();
    }
    
    private void init() {
    
        this.setBackground(UISettings.INIT_SCRN_MAIN_BG_COLOR);
        this.setOpaque(true);
    }
    
    private void addLeftPanels()
    {
        Box leftPanelLayoutBox = Box.createHorizontalBox();
        
        Box leftPanelContainer = Box.createVerticalBox();
        
        profilePanel = new LeftPanel(
                            UISettings.INIT_SCRN_PANELS_PROFILE_TITLE_TEXT_OPEN
                           +UISettings.INIT_SCR_PANEL_TITLE_HILITE_FONT_OPEN
                           +morpho.getCurrentProfileName()
                           +UISettings.INIT_SCR_PANEL_TITLE_HILITE_FONT_CLOSE
                           +UISettings.INIT_SCRN_PANELS_TITLE_CLOSE,
                            UISettings.INIT_SCRN_PROFILE_PANEL_HEIGHT);
        loginPanel   = new LeftPanel(
                            UISettings.INIT_SCRN_PANELS_LOGIN_TITLE_TEXT_OPEN
                            +UISettings.INIT_SCRN_PANELS_TITLE_CLOSE,
                            UISettings.INIT_SCRN_LOGIN_PANEL_HEIGHT);
        LeftPanel dataPanel    = new LeftPanel( 
                            UISettings.INIT_SCRN_PANELS_DATA_TITLE_TEXT_OPEN
                            +UISettings.INIT_SCRN_PANELS_TITLE_CLOSE,
                            UISettings.INIT_SCRN_DATA_PANEL_HEIGHT);
        leftPanelContainer.add(
            Box.createVerticalStrut(UISettings.INIT_SCRN_LEFT_PANELS_PADDING));
        leftPanelContainer.add(profilePanel);
        leftPanelContainer.add(Box.createVerticalStrut(
                                    UISettings.INIT_SCRN_LEFT_PANELS_PADDING));
        leftPanelContainer.add(loginPanel);
        leftPanelContainer.add(Box.createVerticalStrut(
                                    UISettings.INIT_SCRN_LEFT_PANELS_PADDING));
        leftPanelContainer.add(dataPanel);
        leftPanelContainer.add(Box.createVerticalGlue());
        
        leftPanelLayoutBox.add(Box.createHorizontalStrut(
                                    UISettings.INIT_SCRN_LEFT_PANELS_PADDING));
        leftPanelLayoutBox.add(leftPanelContainer);
        leftPanelLayoutBox.add(Box.createHorizontalStrut(
                                    UISettings.INIT_SCRN_LEFT_PANELS_PADDING));
            
        this.add(leftPanelLayoutBox, BorderLayout.WEST);
        
        populateProfilePanel(profilePanel);
        populateLoginPanel(loginPanel);
        populateDataPanel(dataPanel);
    }
    
    private void populateProfilePanel(final LeftPanel panel)
    {
        // ROW 1 ///////////////////////////////////////////////////////////////
        
        panel.addToRow1(currentProfileLDAPLabel);
        panel.addToRow1(Box.createHorizontalGlue());
        
        // ROW 2 ///////////////////////////////////////////////////////////////
        
        //LABEL:
        JLabel changeProfileLabel = new JLabel(
                                    UISettings.CHANGE_PROFILE_LABEL_TEXT);
        setSizes(changeProfileLabel,UISettings.INIT_SCRN_LEFT_PANELS_LABELDIMS);
        panel.addToRow2(changeProfileLabel);
        
        //PICKLIST:
        String[] profileStrings =  morpho.getProfilesList();
        
        final JComboBox profilePicker = new JComboBox(profileStrings);
        
        for (int sel = 0; sel < profileStrings.length; sel++) {
            if (morpho.getCurrentProfileName().equals(profileStrings[sel])) {
                profilePicker.setSelectedIndex(sel);
                break;
            }
        }

        panel.addToRow2(profilePicker);
        setSizes(profilePicker, UISettings.INIT_SCRN_LEFT_PANELS_PICKLISTDIMS);

        profilePicker.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logoutCommand.execute(e);
                morpho.setProfileDontLogin(
                                    (String)profilePicker.getSelectedItem());
                updateLDAPStatus();
            }
        });

        panel.addToRow2(Box.createHorizontalGlue());

        // ROW 3 ///////////////////////////////////////////////////////////////
        Command newProfileCmd = new Morpho.CreateNewProfileCommand();
        
        GUIAction newProfileAction 
                    = new GUIAction(UISettings.NEW_PROFILE_LINK_TEXT,
                                    UISettings.NEW_PROFILE_ICON, newProfileCmd);
        newProfileAction.setRolloverSmallIcon(
                                    UISettings.NEW_PROFILE_ICON_ROLLOVER);
        HyperlinkButton newProfileLink = new HyperlinkButton(newProfileAction);
        setSizes(newProfileLink,UISettings.INIT_SCR_LINKBUTTON_DIMS);
        panel.addToRow3(newProfileLink);
        panel.addToRow3(Box.createHorizontalGlue());
        
        //DO INITIAL UPDATES:
        updateLDAPStatus();
    }
    
    private void setSizes(JComponent comp, Dimension dims)
    {
        comp.setPreferredSize(dims);
        comp.setMinimumSize(dims);
        comp.setMaximumSize(dims);
    }
    
    
    
    private void populateLoginPanel(final LeftPanel panel) 
    {
        final JButton loginButton = new JButton(); 
 
        // ROW 1 ///////////////////////////////////////////////////////////////

        final JLabel loginMessageLabel = new JLabel();
        panel.addToRow1(loginMessageLabel);
        panel.addToRow1(Box.createHorizontalGlue());
        
        // ROW 2 ///////////////////////////////////////////////////////////////

        final JLabel loginHeaderLabel = new JLabel();
        panel.addToRow2(loginHeaderLabel);
        panel.addToRow2(Box.createHorizontalGlue());

        // ROW 3 ///////////////////////////////////////////////////////////////

        //LABEL:
        final JLabel passwordLabel = new JLabel(UISettings.PASSWORD_LABEL_TEXT);
        setSizes(passwordLabel,UISettings.INIT_SCRN_LEFT_PANELS_LABELDIMS);
        //////////////
       
        //PASSWORD FIELD:
        final JPasswordField passwordField = new JPasswordField();
        setSizes(passwordField,UISettings.INIT_SCRN_LEFT_PANELS_PICKLISTDIMS);
        passwordField.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    loginButton.getAction().actionPerformed(null); 
                }
            });
 
 
        //////////////
        
        
        //LOGOUT LINK:
        logoutCommand = new Command(){
            public void execute(ActionEvent e) {
                morpho.logOut();
                updateLoginStatus(  loginMessageLabel, 
                                    loginHeaderLabel, passwordLabel, 
                                    passwordField, loginButton);
            }};
        GUIAction logoutAction = new GUIAction( UISettings.LOGOUT_LINK_TEXT, 
                                                UISettings.LOGOUT_ICON, 
                                                logoutCommand);
        logoutAction.setRolloverSmallIcon(UISettings.LOGOUT_ICON_ROLLOVER);
        logoutLink = new HyperlinkButton(logoutAction);
        setSizes(logoutLink,UISettings.INIT_SCR_LINKBUTTON_DIMS);
        //////////////

        
        //LOGIN BUTTON:
        GUIAction loginAction = new GUIAction(
        
            UISettings.INIT_SCR_LOGIN_BUTTON_TEXT, 
            null,
            new LoginCommand(morpho, 
                new LoginClientInterface() {
                        
                    public String getPassword()
                    {
                        parentFrame.setBusy(true);
                        return new String(passwordField.getPassword());
                    }

                    public void setLoginSuccessful(boolean success)
                    {
                        parentFrame.setBusy(false);
                        if (success) {
                            updateLoginStatus(  loginMessageLabel, 
                                                loginHeaderLabel, passwordLabel, 
                                                passwordField, loginButton);
                        } else {
                            Log.debug(9, "Login failed.\n" + 
                              "Please check the Caps Lock key and try again.");
                        }
                    }
                }));
        loginButton.setAction(loginAction);
        loginButton.setContentAreaFilled(false); 
        loginButton.setRolloverEnabled(true);
        loginButton.setHorizontalAlignment(SwingConstants.CENTER);
        loginButton.setMargin(new Insets(0,0,0,0));
        //////////////

        //ADD CONNECTIONLISTENER SO WE CAN RESPOND TO CHANGES:
        morpho.addConnectionListener(
            new ConnectionListener(){
        
                public void connectionChanged(boolean isConnected)
                {
                    updateLoginStatus(  loginMessageLabel, 
                                        loginHeaderLabel, passwordLabel, 
                                        passwordField, loginButton);
                }
              
                public void usernameChanged(String username)
                {
                    updateLDAPStatus();
                }
            });
            
 
        //DO INITIAL UPDATES:
        updateLoginStatus(  loginMessageLabel, loginHeaderLabel, 
                            passwordLabel, passwordField, loginButton);
    }
    
    
    private void updateLDAPStatus() 
    {
        profilePanel.setTitle(  
                         UISettings.INIT_SCRN_PANELS_PROFILE_TITLE_TEXT_OPEN
                        +UISettings.INIT_SCR_PANEL_TITLE_HILITE_FONT_OPEN
                        +morpho.getCurrentProfileName()
                        +UISettings.INIT_SCR_PANEL_TITLE_HILITE_FONT_CLOSE
                        +UISettings.INIT_SCRN_PANELS_TITLE_CLOSE);
        currentProfileLDAPLabel.setText(  
                         UISettings.INIT_SCR_PANEL_LITE_FONT_OPEN
                        +"("+morpho.getUserName()+")"
                        +UISettings.INIT_SCR_PANEL_LITE_FONT_CLOSE);
    }
    
    
    private void updateLoginStatus( JLabel          loginMessageLabel,
                                    JLabel          loginHeaderLabel, 
                                    JLabel          passwordLabel, 
                                    JPasswordField  passwordField, 
                                    JButton         loginButton)
    {
        loginPanel.setTitle(  
                         UISettings.INIT_SCRN_PANELS_LOGIN_TITLE_TEXT_OPEN
                        +UISettings.INIT_SCR_PANEL_TITLE_HILITE_FONT_OPEN
                        +((morpho.isConnected())? 
                                        UISettings.INIT_SCR_LOGGED_IN_STATUS :
                                        UISettings.INIT_SCR_LOGGEDOUT_STATUS)
                        +UISettings.INIT_SCR_PANEL_TITLE_HILITE_FONT_CLOSE
                        +UISettings.INIT_SCRN_PANELS_TITLE_CLOSE);

        loginMessageLabel.setText(  
                        UISettings.INIT_SCR_PANEL_LITE_FONT_OPEN
                        +((morpho.isConnected())? 
                                        UISettings.INIT_SCR_LOGGED_IN_MESSAGE :
                                        UISettings.INIT_SCR_LOGIN_MESSAGE)
                        +UISettings.INIT_SCR_PANEL_LITE_FONT_CLOSE);

        //if no change, don't need to update panel    
        if (morpho.isConnected() == prevLoginStatus) return;
        
        if (morpho.isConnected()) {
            loginPanel.clearRow3();
            loginPanel.addToRow3(logoutLink);
            loginPanel.addToRow3(Box.createHorizontalGlue());
            loginHeaderLabel.setText("");
            prevLoginStatus = true;
        } else {
            loginPanel.clearRow3();
            loginPanel.addToRow3(passwordLabel);
            passwordField.setText("");       
            loginPanel.addToRow3(passwordField);
            loginPanel.addToRow3(Box.createHorizontalStrut(10));
            loginPanel.addToRow3(loginButton);
            loginPanel.addToRow3(Box.createHorizontalGlue());
            loginHeaderLabel.setText(UISettings.INIT_SCR_LOGIN_HEADER);
            prevLoginStatus = false;
        }
                        
    }
    
    
    private void populateDataPanel(final LeftPanel panel) 
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
        
        // ROW 1 ///////////////////////////////////////////////////////////////
        GUIAction newAction 
                    = new GUIAction(UISettings.NEW_DATAPACKAGE_LINK_TEXT,
                                    UISettings.NEW_DATAPACKAGE_ICON, newPkgCmd);
        newAction.setRolloverSmallIcon(
                                    UISettings.NEW_DATAPACKAGE_ICON_ROLLOVER);
        HyperlinkButton newLink = new HyperlinkButton(newAction);
        setSizes(newLink,UISettings.INIT_SCR_LINKBUTTON_DIMS);
        panel.addToRow1(newLink);
        panel.addToRow1(Box.createHorizontalGlue());

        // ROW 2 ///////////////////////////////////////////////////////////////
        GUIAction openAction 
                    = new GUIAction(UISettings.OPEN_DATAPACKAGE_LINK_TEXT,
                                    UISettings.OPEN_DATAPACKAGE_ICON,openPkgCmd);
        openAction.setRolloverSmallIcon(
                                    UISettings.OPEN_DATAPACKAGE_ICON_ROLLOVER);
        HyperlinkButton openLink = new HyperlinkButton(openAction);
        setSizes(openLink,UISettings.INIT_SCR_LINKBUTTON_DIMS);
        panel.addToRow2(openLink);
        panel.addToRow2(Box.createHorizontalGlue());

        // ROW 3 ///////////////////////////////////////////////////////////////
        GUIAction searchAction 
                    = new GUIAction(UISettings.SEARCH_LINK_TEXT,
                                    UISettings.SEARCH_ICON, searchCmd);
        searchAction.setRolloverSmallIcon(
                                    UISettings.SEARCH_ICON_ROLLOVER);

        HyperlinkButton searchLink = new HyperlinkButton(searchAction);
        setSizes(searchLink,UISettings.INIT_SCR_LINKBUTTON_DIMS);
        panel.addToRow3(searchLink);
        panel.addToRow3(Box.createHorizontalGlue());
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
            setSizes(this,thisDim);
            this.setOpaque(true);

            titleLabel = new JLabel(title);
            setSizes(titleLabel,UISettings.INIT_SCRN_LEFT_PANELS_TITLE_DIMS);
            titleLabel.setBackground(
                            UISettings.INIT_SCRN_LEFT_PANELS_TITLE_BG_COLOR);
            titleLabel.setOpaque(true);
            
            this.add(titleLabel, BorderLayout.NORTH);
            
            Box vertBox = Box.createVerticalBox();
            
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
            
            //add padding at each side:
            Box horizBox = Box.createHorizontalBox();
            horizBox.add(Box.createHorizontalStrut(10));
            horizBox.add(vertBox);
            horizBox.add(Box.createHorizontalStrut(10));
            this.add(horizBox, BorderLayout.CENTER);
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
         *  remove all Components from top box
         *
         *  @param comp the Component to add
         */
        public void clearRow1() 
        { 
            topBox.removeAll();
            topBox.validate();
        }
        
        /**
         *  add passed Component to middle box (see explanation above)
         *
         *  @param comp the Component to add
         */
        public void addToRow2(Component comp) { middleBox.add(comp); }
        
        
        /**
         *  remove all Components from middle box
         *
         *  @param comp the Component to add
         */
        public void clearRow2() 
        { 
            middleBox.removeAll();
            middleBox.validate();
        }
        
        
        /**
         *  add passed Component to bottom box (see explanation above)
         *
         *  @param comp the Component to add
         */
        public void addToRow3(Component comp) { bottomBox.add(comp); }
        
    
        /**
         *  remove all Components from bottom box
         *
         *  @param comp the Component to add
         */
        public void clearRow3() 
        { 
            bottomBox.removeAll();
            bottomBox.validate();
        }
    
    
    }
    
    
    /**
     *  overrides JPanel's paintComponent() method to paint graphic behind 
     *  3 panels on left of initial screen. Starts with a call to 
     *  <code>super.paintComponent(g)</code>
     */
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        
        g.drawImage(UISettings.INIT_SCR_BACKGROUND, 0, 0, this);
    }
}