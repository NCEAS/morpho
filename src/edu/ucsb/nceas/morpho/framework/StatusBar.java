/**
 *  '$RCSfile: StatusBar.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-10-12 01:21:27 $'
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

import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.Box;
import javax.swing.ToolTipManager;

/**
 *  A graphical status bar for displaying information about network and login 
 *  status.  Tells the user whether Metacat is reachable over the network, 
 *  whether the user is logged into metacat, and whether the connection to 
 *  Metacat is via SSL or insecure HTTP.
 */
public class StatusBar extends JPanel
{
  // C O N S T A N T S    - do these need to be in a properties file??

  private final String CONN_YES_ICONPATH 
                                = "connect_yes.gif";
  private final String CONN_NO_ICONPATH 
                                = "connect_no.gif";
  private final String LOGIN_YES_ICONPATH 
                                = "login_yes.gif";
  private final String LOGIN_NO_ICONPATH 
                                = "login_no.gif";
  private final String SSL_YES_ICONPATH 
                                = "ssl_yes.gif";
  private final String SSL_NO_ICONPATH 
                                = "ssl_no.gif";

  private final String CONN_YES_TEXT 
                                = "network connection is available";
  private final String CONN_NO_TEXT 
                                = "network connection NOT available";
  private final String LOGIN_YES_TEXT 
                                = "logged into network";
  private final String LOGIN_NO_TEXT 
                                = "NOT logged into network";
  private final String SSL_YES_TEXT 
                                = "using secure connection";
  private final String SSL_NO_TEXT 
                                = "NOT using secure connection";
  
  //color and font for status bar text message 
  private final Color TEXT_COLOR  = new Color(102,102,153);
  
  private final Font TEXT_FONT    = new Font("Dialog", Font.PLAIN , 12);

  //width of padding space between icons
  private static final int PADDING  = 5;

  //width of padding space to right of icons 
  // - used to avoid resize handle on Mac OSX
  private static final int RHS_PADDING  = 15;
  //Ultimately - could test to see if this is MAC L&F, and if not, set this to 0
              
  /**
   *  Constructor - creates the StatusBar and initializes its state
   */
  public StatusBar() 
  { 
      initializeComponents(); 
      buildStatusBar();
  }

  /**
   * Set up the status icons and their labels, and init them all to "no"
   */ 
  private void initializeComponents()
  {
    connect_YesIcon = new ImageIcon(getClass().getResource(CONN_YES_ICONPATH));
    connect_NoIcon  = new ImageIcon(getClass().getResource(CONN_NO_ICONPATH));
    login_YesIcon   = new ImageIcon(getClass().getResource(LOGIN_YES_ICONPATH));
    login_NoIcon    = new ImageIcon(getClass().getResource(LOGIN_NO_ICONPATH));
    ssl_YesIcon     = new ImageIcon(getClass().getResource(SSL_YES_ICONPATH));
    ssl_NoIcon      = new ImageIcon(getClass().getResource(SSL_NO_ICONPATH));

    messageLabel       = new JLabel("");
    connectStatusLabel = new JLabel();
    loginStatusLabel   = new JLabel();
    sslStatusLabel     = new JLabel();

    messageLabel.setForeground( TEXT_COLOR );
    messageLabel.setFont(TEXT_FONT);

    setConnectStatus(false);
    setLoginStatus(false);
    setSSLStatus(false);
  }


  /**
   * Set up the status icons on the status bar
   */ 
  private void buildStatusBar()
  {
    this.setLayout(new BorderLayout());

    this.add(BorderLayout.WEST, Box.createHorizontalStrut(PADDING));
    this.add(BorderLayout.CENTER, messageLabel);

    JPanel iconPanel = new JPanel();
    iconPanel.add(connectStatusLabel);
    iconPanel.add(Box.createHorizontalStrut(PADDING));
    iconPanel.add(loginStatusLabel);
    iconPanel.add(Box.createHorizontalStrut(PADDING));
    iconPanel.add(sslStatusLabel);
    iconPanel.add(Box.createHorizontalStrut(RHS_PADDING));
    this.add(BorderLayout.EAST, iconPanel);
    
    ToolTipManager.sharedInstance().registerComponent(connectStatusLabel);
    ToolTipManager.sharedInstance().registerComponent(loginStatusLabel);
    ToolTipManager.sharedInstance().registerComponent(sslStatusLabel);
    ToolTipManager.sharedInstance().setInitialDelay(10);
    ToolTipManager.sharedInstance().setDismissDelay(1000);
  }


  /**
   * Sets the "connection status" icon 
   *
   *  @param status - boolean true for "yes", boolean false for "no"
   *
   */ 
  public void setConnectStatus(boolean status)
  {
    if (status) {
      connectStatusLabel.setIcon(connect_YesIcon);
      connectStatusLabel.setToolTipText(CONN_YES_TEXT);
    } else {
      connectStatusLabel.setIcon(connect_NoIcon);
      connectStatusLabel.setToolTipText(CONN_NO_TEXT);
    }
  }
  
  /**
   * Sets the "login status" icon 
   *
   *  @param status - boolean true for "yes", boolean false for "no"
   *
   */ 
  public void setLoginStatus(boolean status)
  {
    if (status) {
      loginStatusLabel.setIcon(login_YesIcon);
      loginStatusLabel.setToolTipText(LOGIN_YES_TEXT);
    } else {
      loginStatusLabel.setIcon(login_NoIcon);
      loginStatusLabel.setToolTipText(LOGIN_NO_TEXT);
    }
  }
  
  /**
   * Sets the "ssl status" icon 
   *
   *  @param status - boolean true for "yes", boolean false for "no"
   *
   */ 
  public void setSSLStatus(boolean status)
  {
    if (status) {
      sslStatusLabel.setIcon(ssl_YesIcon);
      sslStatusLabel.setToolTipText(SSL_YES_TEXT);
    } else {
      sslStatusLabel.setIcon(ssl_NoIcon);
      sslStatusLabel.setToolTipText(SSL_NO_TEXT);
    }
  }
    
  /**
   * Sets the text message to display 
   *
   *  @param message - message - String to display
   *
   */ 
  public void setMessage(String message)
  {
    if (message==null) message="";
    messageLabel.setText(message);
  }  
  
  /**
   * Gets the text message currently being displayed
   *
   *  @return message - String currently being displayed
   *
   */ 
  public String getMessage()
  {
    return messageLabel.getText();
  } 

  
  //  C L A S S   V A R I A B L E S 
  
  private JLabel            messageLabel;
  private JLabel            connectStatusLabel;
  private JLabel            loginStatusLabel;
  private JLabel            sslStatusLabel;
  
  private ImageIcon         connect_YesIcon;
  private ImageIcon         connect_NoIcon;
  private ImageIcon         login_YesIcon;
  private ImageIcon         login_NoIcon;
  private ImageIcon         ssl_YesIcon;
  private ImageIcon         ssl_NoIcon;
}
