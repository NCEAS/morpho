/**
 *  '$RCSfile: ConnectionFrame.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-07-30 23:37:15 $'
 * '$Revision: 1.24 $'
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

import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * A graphical window for obtaining login information from the
 * user and logging into Metacat
 */
public class ConnectionFrame extends javax.swing.JDialog
{
  ClientFramework container = null;
  javax.swing.ImageIcon still = null;
  javax.swing.ImageIcon flapping = null;

  /**
   * Construct a frame and set the framework
   *
   * @param cont the container framework that created this ConnectionFrame
   */
  public ConnectionFrame(ClientFramework cont) {
    this(cont, true);
  }

  /**
   * Construct the frame
   */
  public ConnectionFrame(ClientFramework cont, boolean modal)
  {
    super((Frame)cont, modal);

    container = cont;

    // This code is automatically generated by Visual Cafe when you add
    // components to the visual environment. It instantiates and initializes
    // the components. To modify the code, only use code syntax that matches
    // what Visual Cafe can generate, or Visual Cafe may be unable to back
    // parse your Java file into its visual environment.
    //{{INIT_CONTROLS
    setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
    setTitle("Connection");
    getContentPane().setLayout(new BorderLayout(0,0));
    setSize(315,290);
    /* Center the Frame */
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2 ,
            (screenDim.height - frameDim.height) /2);
    setVisible(false);
    JLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    JLabel1.setText("Connection Dialog");
    getContentPane().add(BorderLayout.NORTH,JLabel1);
    JLabel1.setForeground(java.awt.Color.black);
    JLabel1.setFont(new Font("Dialog", Font.BOLD|Font.ITALIC, 14));
    JLabel1.setBounds(0,0,315,16);
    JPanel2.setLayout(new BorderLayout(0,0));
    getContentPane().add(BorderLayout.CENTER,JPanel2);
    JPanel2.setBounds(0,16,315,239);
    JButtonGroupPanel1.setLayout(new GridLayout(3,1,0,0));
    JPanel2.add(BorderLayout.NORTH,JButtonGroupPanel1);
    JButtonGroupPanel1.setBounds(0,0,315,87);
    JPanel3.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
    JButtonGroupPanel1.add(JPanel3);
    JPanel3.setBounds(0,0,315,29);
    Name.setText("Name");
    JPanel3.add(Name);
    Name.setForeground(java.awt.Color.black);
    Name.setFont(new Font("Dialog", Font.PLAIN, 12));
    Name.setBounds(5,7,34,15);
    NameTextField.setColumns(23);
    NameTextField.setText("Enter user name here");
    JPanel3.add(NameTextField);
    NameTextField.setBounds(44,5,253,19);
    JPanel4.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
    JButtonGroupPanel1.add(JPanel4);
    JPanel4.setBounds(0,29,315,29);
    Password.setText("Password");
    JPanel4.add(Password);
    Password.setForeground(java.awt.Color.black);
    Password.setFont(new Font("Dialog", Font.PLAIN, 12));
    Password.setBounds(5,7,56,15);
    PWTextField.setColumns(21);
    JPanel4.add(PWTextField);
    PWTextField.setBounds(66,5,231,19);
//    PWTextField.setText(container.getPassword());
    ActivityLabel.setDoubleBuffered(true);
    ActivityLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
    ActivityLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    JButtonGroupPanel1.add(ActivityLabel);
    ActivityLabel.setForeground(java.awt.Color.black);
    ActivityLabel.setBounds(0,58,315,29);
    ConnectionResultsTextArea.setText("Connection Messages will appear here");
    JPanel2.add(BorderLayout.CENTER,ConnectionResultsTextArea);
    ConnectionResultsTextArea.setBounds(0,87,315,152);
    JPanel1.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
    getContentPane().add(BorderLayout.SOUTH,JPanel1);
    JPanel1.setBounds(0,255,315,35);
    connectButton.setText("Connect");
    connectButton.setActionCommand("OK");
    connectButton.setMnemonic(KeyEvent.VK_ENTER);
    JPanel1.add(connectButton);
    connectButton.setBounds(65,5,81,25);
    DisconnectButton.setText("Disconnect");
    DisconnectButton.setActionCommand("Disconnect");
    DisconnectButton.setEnabled(false);
    JPanel1.add(DisconnectButton);
    DisconnectButton.setBounds(151,5,99,25);
    CancelButton.setText("Cancel");
    CancelButton.setActionCommand("Cancel");
    CancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
    CancelButton.setEnabled(true);
    JPanel1.add(CancelButton);
    CancelButton.setBounds(151,5,99,25);
    //}}

    //{{INIT_MENUS
    //}}
  
    //{{REGISTER_LISTENERS
    this.addKeyListener(keyPressListener);
    SymAction lSymAction = new SymAction();
    connectButton.addActionListener(lSymAction);
    DisconnectButton.addActionListener(lSymAction);
    CancelButton.addActionListener(lSymAction);
    //}}
    
    if (container!=null) {
      NameTextField.setText(container.getUserName());
      DisconnectButton.setEnabled(container.isConnected());
      if (container.isConnected()) {
        ConnectionResultsTextArea.setText("Currently logged in.");
      } else {
        ConnectionResultsTextArea.setText("Not logged in.");
      }
    }

    // Example of loading icon as resource - DFH 
    try {
      still = new javax.swing.ImageIcon(getClass().getResource("Btfly.gif"));
      ActivityLabel.setIcon(still);
      flapping = new javax.swing.ImageIcon(getClass().getResource("Btfly4.gif"));
    } catch (Exception w) {
      ClientFramework.debug(7, "Error in loading images");
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
    javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
    int menuBarHeight = 0;
    if (menuBar != null)
      menuBarHeight = menuBar.getPreferredSize().height;
    setSize(insets.left + insets.right + size.width, 
            insets.top + insets.bottom + size.height + menuBarHeight);
  }

  // Used by addNotify
  boolean frameSizeAdjusted = false;

  //{{DECLARE_CONTROLS
  javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
  javax.swing.JPanel JPanel2 = new javax.swing.JPanel();
  javax.swing.JPanel JButtonGroupPanel1 = new javax.swing.JPanel();
  javax.swing.JPanel JPanel3 = new javax.swing.JPanel();
  javax.swing.JLabel Name = new javax.swing.JLabel();
  javax.swing.JTextField NameTextField = new javax.swing.JTextField();
  javax.swing.JPanel JPanel4 = new javax.swing.JPanel();
  javax.swing.JLabel Password = new javax.swing.JLabel();
  javax.swing.JPasswordField PWTextField = new javax.swing.JPasswordField();
  javax.swing.JLabel ActivityLabel = new javax.swing.JLabel();
  javax.swing.JTextArea ConnectionResultsTextArea = new javax.swing.JTextArea();
  javax.swing.JPanel JPanel1 = new javax.swing.JPanel();
  javax.swing.JButton connectButton = new javax.swing.JButton();
  javax.swing.JButton DisconnectButton = new javax.swing.JButton();
  javax.swing.JButton CancelButton = new javax.swing.JButton();
  KeyPressActionListener keyPressListener = new KeyPressActionListener();
  //}}

  //{{DECLARE_MENUS
  //}}

  /**
   * Listens for key events coming from the dialog.  responds to escape and 
   * enter buttons.  escape toggles the cancel button and enter toggles the
   * connect button
   */
  class KeyPressActionListener extends java.awt.event.KeyAdapter
  {
    public KeyPressActionListener()
    {
      
    }
    
    public void keyPressed(KeyEvent e)
    {
      if(e.getKeyCode() == KeyEvent.VK_ENTER)
      {
        java.awt.event.ActionEvent event = new 
                             java.awt.event.ActionEvent(connectButton, 0, "OK");
        connectButton_actionPerformed(event);
      }
      else if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
      {
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
    ConnectionResultsTextArea.setText("Working...");
     
    Thread worker = new Thread() 
    {
      public void run() 
      {
        if (container!=null) {
          //container.setUserName(NameTextField.getText());
          container.setPassword(PWTextField.getText());
          container.setProfile(NameTextField.getText());
        }

        final boolean connected = container.logIn();
                                           
        SwingUtilities.invokeLater(new Runnable() 
        {
          public void run() 
          {
            if (connected) {
              dispose();
            } else {
              ConnectionResultsTextArea.setText("Login failed.\n" + 
                    "Please check the Caps Lock key and try again.");
              DisconnectButton.setEnabled(false);
              ActivityLabel.setIcon(still);
            }
          }
        });
      }
    };
    worker.start();
  }
  
  /**
   * Perform actions associated with the Disconnect button
   */
  void DisconnectButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    container.logOut();
    ConnectionResultsTextArea.setText("You are no longer connected.");
    DisconnectButton.setEnabled(false);
  }

  /**
   * Perform actions associated with the Disconnect button
   */
  void CancelButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    container.setProfile(NameTextField.getText());
    
    ConfigXML profile = container.getProfile();
    profile.set("searchmetacat", 0, "false");
    dispose();
  }
}
