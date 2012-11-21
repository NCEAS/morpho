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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * A graphical window for obtaining login information from the
 * user and logging into Metacat
 */
public class ConnectionFrame  extends JDialog 
                              implements LoginClientInterface
{

  Morpho container = null;
  javax.swing.ImageIcon still = null;
  ImageIcon flapping = null;

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
    JLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    JLabel1.setText(/*"Network Login"*/ Language.getInstance().getMessage("NetworkLogin"));
    getContentPane().add(BorderLayout.NORTH,JLabel1);
    JLabel1.setForeground(java.awt.Color.black);
    JLabel1.setFont(new Font("Dialog", Font.BOLD, 14));

    JPanel2.setLayout(new BorderLayout(0,0));
    getContentPane().add(BorderLayout.CENTER,JPanel2);
    JButtonGroupPanel1.setLayout(new GridLayout(4,1,0,0));
    JPanel2.add(BorderLayout.NORTH,JButtonGroupPanel1);

    JPanel instructPanel = new JPanel();
    instructPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
    JButtonGroupPanel1.add(instructPanel);
    JLabel instructLabel = new JLabel();
    instructLabel.setFont(new Font("Dialog", Font.BOLD, 12));
    instructLabel.setForeground(java.awt.Color.black);
    instructLabel.setText(/*"Enter your Network password in order to log in."*/
    						Language.getInstance().getMessage("UISettings.RequirePassword")
    						);
    instructPanel.add(instructLabel);

    JPanel3.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
    JButtonGroupPanel1.add(JPanel3);
    Name.setText(/*"Name"*/ Language.getInstance().getMessage("Name"));
    JPanel3.add(Name);
    Name.setForeground(java.awt.Color.black);
    Name.setFont(new Font("Dialog", Font.PLAIN, 12));
    JPanel3.add(NameLabel);
    JPanel4.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
    JButtonGroupPanel1.add(JPanel4);
    Password.setText(/*"Password"*/ Language.getInstance().getMessage("Password"));
    JPanel4.add(Password);
    Password.setForeground(java.awt.Color.black);
    Password.setFont(new Font("Dialog", Font.PLAIN, 12));
    PWTextField.setColumns(21);
    addKeyListenerToComponent(PWTextField);
    JPanel4.add(PWTextField);
    ActivityLabel.setDoubleBuffered(true);
    ActivityLabel.setHorizontalTextPosition(SwingConstants.LEFT);
    ActivityLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    JButtonGroupPanel1.add(ActivityLabel);
    ActivityLabel.setForeground(java.awt.Color.black);

    JPanel1.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
    getContentPane().add(BorderLayout.SOUTH,JPanel1);
    connectButton.setText(/*"Login"*/ Language.getInstance().getMessage("Login"));
    connectButton.setActionCommand("OK");
    connectButton.setMnemonic(KeyEvent.VK_L);
    addKeyListenerToComponent(connectButton);
    JPanel1.add(connectButton);
    connectButton.isDefaultButton();
    DisconnectButton.setText(/*"Logout"*/ Language.getInstance().getMessage("Logout"));
    DisconnectButton.setActionCommand("Disconnect");
    DisconnectButton.setMnemonic(KeyEvent.VK_O);
    addKeyListenerToComponent(DisconnectButton);
    DisconnectButton.setEnabled(false);
    JPanel1.add(DisconnectButton);
    CancelButton.setText(/*"Skip Login"*/ Language.getInstance().getMessage("SkipLogin"));
    CancelButton.setActionCommand("Cancel");
    CancelButton.setMnemonic(KeyEvent.VK_S);
    addKeyListenerToComponent(CancelButton);
    CancelButton.setEnabled(true);
    JPanel1.add(CancelButton);

    //}}

    //{{INIT_MENUS
    //}}
  
    //{{REGISTER_LISTENERS
        
    SymAction lSymAction = new SymAction();
    connectButton.addActionListener(lSymAction);
    DisconnectButton.addActionListener(lSymAction);
    CancelButton.addActionListener(lSymAction);
    //}}
    
    if (container!=null) {
      NameLabel.setText(container.getUserName());
    }

    // Example of loading icon as resource - DFH 
    try {
      still = new ImageIcon(getClass().getResource("Btfly.gif"));
      ActivityLabel.setIcon(still);
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
		DisconnectButton.setEnabled(connected);
		connectButton.setEnabled(!connected);
		CancelButton.setEnabled(!connected);
		PWTextField.setEnabled(!connected);
		if (PWTextField.isEnabled()) {
			PWTextField.requestFocus();
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
  JLabel JLabel1 = new JLabel();
  JPanel JPanel2 = new JPanel();
  JPanel JButtonGroupPanel1 = new JPanel();
  JPanel JPanel3 = new JPanel();
  JLabel Name = new JLabel();
  JLabel NameLabel = new JLabel();
  JPanel JPanel4 = new JPanel();
  JLabel Password = new JLabel();
  JPasswordField PWTextField = new JPasswordField();
  JTextField certificateLocationTextField = new JTextField();
  JLabel ActivityLabel = new JLabel();
  JPanel JPanel1 = new JPanel();
  JButton connectButton = new JButton();
  JButton DisconnectButton = new JButton();
  JButton CancelButton = new JButton();
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
  class KeyPressActionListener extends java.awt.event.KeyAdapter
  {
    public KeyPressActionListener() { }
    
    public void keyPressed(KeyEvent e)
    {
      if(e.getKeyCode() == KeyEvent.VK_ENTER) {
        
        //if enter was pressed whilst a button was in focus, just click it
        if (e.getSource() instanceof JButton){
          ((JButton)e.getSource()).doClick();
        } else {
          java.awt.event.ActionEvent event = new 
                             java.awt.event.ActionEvent(connectButton, 0, "OK");
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
  class SymAction implements java.awt.event.ActionListener
  {
    public void actionPerformed(java.awt.event.ActionEvent event)
    {
      
      Object object = event.getSource();
      if (object == connectButton)
      {
        connectButton_actionPerformed(event);
      }
      else if (object == DisconnectButton)
      {
        DisconnectButton_actionPerformed(event);
      }
      else if (object == CancelButton)
      {
        CancelButton_actionPerformed(event);
      }
    }
  }

  /**
   * Perform actions associated with the Connect button
   */
  void connectButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    ActivityLabel.setIcon(flapping);
    ActivityLabel.invalidate();
    JPanel2.validate();
    JPanel2.paint(JPanel2.getGraphics());
    
    new LoginCommand(container, this).execute(event);
  }
  
  /**
   * Perform actions associated with the Disconnect button
   */
  void DisconnectButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    container.getDataONEDataStoreService().logOut();
    updateEnableDisable();
  }

  /**
   * Perform actions associated with the Cancel button
   */
  void CancelButton_actionPerformed(java.awt.event.ActionEvent event)
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
  public String getPassword()
  {
    return new String(PWTextField.getPassword());
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
//      DisconnectButton.setEnabled(false);
      updateEnableDisable();
      ActivityLabel.setIcon(still);
    }
  }
  
}
