/**
 *  '$RCSfile: StatusBar.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-05-21 21:52:52 $'
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

//import java.net.*;
//import java.util.*;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Component;
import javax.swing.*;


/**
 *  A graphical status bar for displaying information about network and login 
 *  status.  Tells the user whether Metacat is reachable over the network, 
 *  whether the user is logged into metacat, and wheter the connection to 
 *  Metacat is via SSL or insecure HTTP
 */
public class StatusBar extends JPanel
{
  // C O N S T A N T S    - do these need to be in a properties file??

  private final String CONN_YES_ICONPATH 
                                = "connect_yes.gif";
  private final String CONN_NO_ICONPATH 
                                = "connect_no.gif";
//  private final String CONN_YES_ICONPATH 
//                                = "/toolbarButtonGraphics/general/Cut16.gif";
//  private final String CONN_NO_ICONPATH 
//                                = "/toolbarButtonGraphics/general/Cut16.gif";
  private final String LOGIN_YES_ICONPATH 
                                = "login_yes.gif";
  private final String LOGIN_NO_ICONPATH 
                                = "login_no.gif";
  private final String SSL_YES_ICONPATH 
                                = "ssl_yes.gif";
  private final String SSL_NO_ICONPATH 
                                = "ssl_no.gif";

  private final String CONN_YES_TEXT 
                                = "metacat connection is available";
  private final String CONN_NO_TEXT 
                                = "metacat connection NOT available";
  private final String LOGIN_YES_TEXT 
                                = "logged into metacat";
  private final String LOGIN_NO_TEXT 
                                = "NOT logged into metacat";
  private final String SSL_YES_TEXT 
                                = "using secure connection";
  private final String SSL_NO_TEXT 
                                = "NOT using secure connection";

                              
  private final Color TEXT_COLOR = new Color(102,102,153);
              
  private static final int STATUSBAR_WIDTH  = 600;
  private static final int STATUSBAR_HEIGHT = 22;


  
  /**
   *  private Constructor - singleton class - we want only one StatusBar  
   *  instance to exist, since they should all show the same status
   *
   *  @param cf   a pointer to the client framework
   *
   *  @throws     java.lang.Exception if a null argument is received
   */
  private StatusBar(ClientFramework cf) throws Exception {
    
    if (cf==null) { 
      throw new Exception(
                "StatusBar.getInstance() received an invalid (NULL) argument "); 
    } else {
      this.clientFramework = cf;
      initializeComponents();
    }
  }

  
  /**
   * Get a pointer to the single instance of the StatusBar
   *
   *  @param cf   a reference to the main ClientFramework object
   *
   *  @return     a pointer to the single instance of the StatusBar
   *
   *  @throws     java.lang.Exception if a null argument is received
   */ 
  public static StatusBar getInstance(ClientFramework cf) throws Exception {
    
    if (statusBar==null) { 
      statusBar = new StatusBar(cf); 
      buildStatusBar();
    }
    return statusBar;
  }
  
  
  /**
   * Set up the status icons on the status bar
   */ 
  private static void buildStatusBar(){

    statusBar.setPreferredSize(STATUSBAR_DIMENSIONS);
    statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
    
    statusBar.add(Box.createHorizontalStrut(10));
    statusBar.add(messageLabel);
    statusBar.add(Box.createHorizontalGlue());
    statusBar.add(Box.createHorizontalStrut(10));
    statusBar.add(connectStatusLabel);
    statusBar.add(Box.createHorizontalStrut(10));
    statusBar.add(loginStatusLabel);
    statusBar.add(Box.createHorizontalStrut(10));
    statusBar.add(sslStatusLabel);
    statusBar.add(Box.createHorizontalStrut(10));
  }

  /**
   * Set up the status icons and their labels, and init them all to "no"
   */ 
  private void initializeComponents(){
    
    STATUSBAR_DIMENSIONS = new Dimension(STATUSBAR_WIDTH, STATUSBAR_HEIGHT);
    
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
    
    messageLabel.setFont(new Font("Dialog", Font.PLAIN , 12));
    
    setConnectStatus(false);
    setLoginStatus(false);
    setSSLStatus(false);
  }
  
  /**
   * Sets the "connection status" icon 
   *
   *  @param status - boolean true for "yes", boolean false for "no"
   *
   */ 
  public void setConnectStatus(boolean status){
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
  public void setLoginStatus(boolean status){
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
  public void setSSLStatus(boolean status){
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
  public void setMessage(String message){
    if (message==null) message="";
    messageLabel.setText(message);
  }  
  
  /**
   * Gets the text message currently being displayed
   *
   *  @return message - String currently being displayed
   *
   */ 
  public String getMessage(){
    return messageLabel.getText();
  } 

  
  //  C L A S S   V A R I A B L E S 
  
  private ClientFramework   clientFramework;
  private static StatusBar  statusBar;
  
  private static JLabel     messageLabel;
  private static JLabel     connectStatusLabel;
  private static JLabel     loginStatusLabel;
  private static JLabel     sslStatusLabel;
  private static Dimension  STATUSBAR_DIMENSIONS;
  
  private ImageIcon         connect_YesIcon;
  private ImageIcon         connect_NoIcon;
  private ImageIcon         login_YesIcon;
  private ImageIcon         login_NoIcon;
  private ImageIcon         ssl_YesIcon;
  private ImageIcon         ssl_NoIcon;
}

