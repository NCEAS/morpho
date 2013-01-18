/**
 *  '$RCSfile: ConnectionFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-12-16 22:42:21 $'
 * '$Revision: 1.38 $'
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.dataone.EcpAuthentication;
import edu.ucsb.nceas.morpho.dataone.IdentityProviderSelectionItem;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * A graphical window for obtaining login information from the
 * user and logging into Metacat
 */
public class ConnectionFrame  extends JDialog 
                              implements LoginClientInterface
{

  private Morpho container = null;
  private ImageIcon still = null;
  private ImageIcon flapping = null;
  JLabel activityLabel = new JLabel();
//  private JProgressBar progressBar = null;
//  private int loginCount = 0;

  /**
   * Construct a frame and set the framework
   *
   * @param cont the container framework that created this ConnectionFrame
   */
  public ConnectionFrame(Morpho cont) {
    this(cont, true);
  }

  /**
   * Construct the frame
   */
  public ConnectionFrame(Morpho cont, boolean modal)
  {
    /*
    super((Frame)cont, modal);
    */
    super(UIController.getInstance().getCurrentActiveWindow());
    this.setModal(true);
    
    container = cont;

    // This code is automatically generated by Visual Cafe when you add
    // components to the visual environment. It instantiates and initializes
    // the components. To modify the code, only use code syntax that matches
    // what Visual Cafe can generate, or Visual Cafe may be unable to back
    // parse your Java file into its visual environment.
    //{{INIT_CONTROLS
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setTitle(/*"Network Login"*/ Language.getInstance().getMessage("NetworkLogin"));
    getContentPane().setLayout(new BorderLayout(0,0));
    //setSize(315,290);
    setVisible(false);

    jPanel2.setLayout(new BorderLayout(0,0));
    getContentPane().add(BorderLayout.CENTER,jPanel2);
    jButtonGroupPanel1.setLayout(new GridLayout(5,1,0,0));
    jPanel2.add(BorderLayout.NORTH,jButtonGroupPanel1);

    JPanel instructPanel = new JPanel();
    instructPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
    jButtonGroupPanel1.add(instructPanel);
    JLabel instructLabel = new JLabel();
    instructLabel.setFont(new Font("Dialog", Font.BOLD, 12));
    instructLabel.setForeground(Color.black);
    instructLabel.setText(/*"Enter your Network password in order to log in."*/
    						Language.getInstance().getMessage("UISettings.RequirePassword")
    						);
    instructPanel.add(instructLabel);

    // identity provider dropdown
    JPanel idpPanel = WidgetFactory.makePanel();
    idpPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
    idpPanel.add(WidgetFactory.makeLabel(Language.getInstance().getMessage("Organization"), true));
    identityProviders = WidgetFactory.makePickList(EcpAuthentication.getInstance().getAvailableIdentityProviders(), true, 0, null);
    WidgetFactory.setPrefMaxSizes(identityProviders, null);
    idpPanel.add(identityProviders);
    jButtonGroupPanel1.add(idpPanel);
    
    jPanel3.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
    jButtonGroupPanel1.add(jPanel3);
    userNameLabel = WidgetFactory.makeLabel(/*"Name"*/ Language.getInstance().getMessage("Username"), true);
    jPanel3.add(userNameLabel);
    userName.setColumns(21);
    jPanel3.add(userName);
    jPanel4.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
    jButtonGroupPanel1.add(jPanel4);
    passwordLabel = WidgetFactory.makeLabel(/*"Password"*/ Language.getInstance().getMessage("Password"), true);
    jPanel4.add(passwordLabel);
    passwordTextField.setColumns(21);
    addKeyListenerToComponent(passwordTextField);
    jPanel4.add(passwordTextField);
    
    // the progress indicator
//    progressBar = new JProgressBar();
//    progressBar.setMinimum(0);
//	jButtonGroupPanel1.add(progressBar);
	    
    // the butterfly as an indicator
    activityLabel.setDoubleBuffered(true);
    activityLabel.setHorizontalTextPosition(SwingConstants.LEFT);
    activityLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    jButtonGroupPanel1.add(activityLabel);
    activityLabel.setForeground(Color.black);

    jPanel1.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
    getContentPane().add(BorderLayout.SOUTH,jPanel1);
    connectButton.setText(/*"Login"*/ Language.getInstance().getMessage("Login"));
    connectButton.setActionCommand("OK");
    connectButton.setMnemonic(KeyEvent.VK_L);
    addKeyListenerToComponent(connectButton);
    jPanel1.add(connectButton);
    connectButton.isDefaultButton();
    disconnectButton.setText(/*"Logout"*/ Language.getInstance().getMessage("Logout"));
    disconnectButton.setActionCommand("Disconnect");
    disconnectButton.setMnemonic(KeyEvent.VK_O);
    addKeyListenerToComponent(disconnectButton);
    disconnectButton.setEnabled(false);
    jPanel1.add(disconnectButton);
    cancelButton.setText(/*"Skip Login"*/ Language.getInstance().getMessage("SkipLogin"));
    cancelButton.setActionCommand("Cancel");
    cancelButton.setMnemonic(KeyEvent.VK_S);
    addKeyListenerToComponent(cancelButton);
    cancelButton.setEnabled(true);
    jPanel1.add(cancelButton);

    //}}

    //{{INIT_MENUS
    //}}
  
    //{{REGISTER_LISTENERS
        
    SymAction lSymAction = new SymAction();
    connectButton.addActionListener(lSymAction);
    disconnectButton.addActionListener(lSymAction);
    cancelButton.addActionListener(lSymAction);
    //}}
    
	// force user to enter username each time
	// see http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5791
//    if (container != null) {
//    	userName.setText(container.getUserName());
//    }

    // Example of loading icon as resource - DFH 
    try {
      still = new ImageIcon(getClass().getResource("Btfly.gif"));
      activityLabel.setIcon(still);
      flapping = new ImageIcon(getClass().getResource("Btfly4.gif"));
    } catch (Exception w) {
      Log.debug(7, "Error in loading images");
    }

    pack();
    
    updateEnableDisable();
    
    /* Center the Frame */
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2 ,
            (screenDim.height - frameDim.height) /2);
    
  }

  
  private void updateEnableDisable() {
		boolean connected = container.getDataONEDataStoreService().isConnected();
		disconnectButton.setEnabled(connected);
		connectButton.setEnabled(!connected);
		cancelButton.setEnabled(!connected);
		passwordTextField.setEnabled(!connected);
		if (passwordTextField.isEnabled()) {
			passwordTextField.requestFocus();
		}
	}
  
  /**
   * Adjust the window size
   */
  public void addNotify()
  {
    // Record the size of the window prior to calling parents addNotify.
    Dimension size = getSize();

    super.addNotify();

    if (frameSizeAdjusted)
      return;
    frameSizeAdjusted = true;

    // Adjust size of frame according to the insets and menu bar
    Insets insets = getInsets();
    JMenuBar menuBar = getRootPane().getJMenuBar();
    int menuBarHeight = 0;
    if (menuBar != null)
      menuBarHeight = menuBar.getPreferredSize().height;
    setSize(insets.left + insets.right + size.width, 
            insets.top + insets.bottom + size.height + menuBarHeight);
  }

  // Used by addNotify
  boolean frameSizeAdjusted = false;

  //{{DECLARE_CONTROLS
  JPanel jPanel2 = new JPanel();
  JPanel jButtonGroupPanel1 = new JPanel();
  JPanel jPanel3 = new JPanel();
  JComboBox identityProviders = null;
  JLabel userNameLabel = new JLabel();
  JTextField userName = WidgetFactory.makeOneLineShortTextField();
  JPanel jPanel4 = new JPanel();
  JLabel passwordLabel = new JLabel();
  JPasswordField passwordTextField = new JPasswordField();
  JTextField certificateLocationTextField = WidgetFactory.makeOneLineTextField();
  JPanel jPanel1 = new JPanel();
  JButton connectButton = new JButton();
  JButton disconnectButton = new JButton();
  JButton cancelButton = new JButton();
  KeyPressActionListener keyPressListener = new KeyPressActionListener();
  //}}

  //{{DECLARE_MENUS
  //}}

 
  /**
   * Adds listener to passed Component object
   */
  private void addKeyListenerToComponent(Component component){
    if (component!=null) {
          component.addKeyListener(keyPressListener);
    } else {
      Log.debug(10,
            "ConnectionFrame.addKeyListenerToComponent() - received NULL arg");
      return;
    }
  }
  
  
  /**
   * Listens for key events coming from the dialog.  responds to escape and 
   * enter buttons.  escape toggles the cancel button and enter toggles the
   * connect button
   */
  class KeyPressActionListener extends KeyAdapter
  {
    public KeyPressActionListener() { }
    
    public void keyPressed(KeyEvent e)
    {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        //if enter was pressed whilst a button was in focus, just click it
        if (e.getSource() instanceof JButton) {
          ((JButton)e.getSource()).doClick();
        } else {
          ActionEvent event = new ActionEvent(connectButton, 0, "OK");
          connectButton_actionPerformed(event);
        }
      } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        dispose();
      }
    }
  }

  /**
   * Listener used to detect button presses
   */
  class SymAction implements ActionListener
  {
    public void actionPerformed(ActionEvent event)
    {
      
      Object object = event.getSource();
      if (object == connectButton)
      {
        connectButton_actionPerformed(event);
      }
      else if (object == disconnectButton)
      {
        disconnectButton_actionPerformed(event);
      }
      else if (object == cancelButton)
      {
        cancelButton_actionPerformed(event);
      }
    }
  }

  /**
   * Perform actions associated with the Connect button
   */
  void connectButton_actionPerformed(ActionEvent event)
  {
	// using the butterfly
	activityLabel.setIcon(flapping);
    
    // using the progress bar
    //progressBar.setIndeterminate(true);
    
    // execute in another thread so the progress bar can be seen while we login
    final ConnectionFrame instance = this;
    SwingWorker sw = new SwingWorker() {

		@Override
		protected Object doInBackground() throws Exception {
			// do the login action, the event can be null since it is not used
			new LoginCommand(container, instance).execute(null);
			return null;
		}
		
		@Override
		protected void done() {
			// butterfly
		    activityLabel.setIcon(still);
		    
		    // progress bar
			// slowly creep up the count each time they login/attempt to
//			progressBar.setValue(loginCount++);
//			progressBar.setIndeterminate(false);
       }
    	
    };
    sw.execute();
	
    
  }
  
  /**
   * Perform actions associated with the Disconnect button
   */
  void disconnectButton_actionPerformed(ActionEvent event)
  {
    container.getDataONEDataStoreService().logOut();
    updateEnableDisable();
  }

  /**
   * Perform actions associated with the Cancel button
   */
  void cancelButton_actionPerformed(ActionEvent event)
  {
    
    ConfigXML profile = container.getProfile();
    profile.set("searchnetwork", 0, "false", true);
    profile.save();
    dispose();
  }
  
  /**
   *  gets the user-entered password from the client
   *
   *  @return   the user-entered password as a String
   */
  @Override
  public String getPassword()
  {
    return new String(passwordTextField.getPassword());
  }
  
  /**
   *  gets the username from the client
   *
   *  @return   the username as a String
   */
  @Override
  public String getUsername()
  {
    return userName.getText();
  }
  
  /**
   *  gets the IdP URL that was selected
   *
   *  @return   the IdP URL as a String
   */
  @Override
  public String getIdentityProvider() {
	  IdentityProviderSelectionItem idp = (IdentityProviderSelectionItem) identityProviders.getSelectedItem();
	  return idp.getUrl();
  }
  
  /**
   *  gets the certificate location from the client
   *
   *  @return   the user-entered certificate location as a String
   */
  public String getCertificateLocation()
  {
    return new String(certificateLocationTextField.getText());
  }
  
  /**
   *  notifies client whether login was successful or not
   *
   *  @return   boolean flag indicating whether login was successful (true) or 
   *            not (false)
   */
  public void setLoginSuccessful(boolean success)
  {
    if (success) {
      dispose();
    } else {
      Log.debug(9, 
    		  	/*"Login failed.\n"*/ Language.getInstance().getMessage("LoginFailed") + "\n"
    		  	+ /*"Please check the Caps Lock key and try again."*/ Language.getInstance().getMessage("CheckCaps")
    		  	);
      updateEnableDisable();
    }
  }
  
}
