/**
 *       Name: OpenDialogBox.java
 *    Purpose: Visual display for OpenDialogBox
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @Jing Tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-08 00:13:34 $'
 * '$Revision: 1.1.2.2 $'
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
package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.util.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * A dialog box for user to open a datapakcage
 */
public class OpenDialogBox extends JDialog
{
  /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** the reference to mediator */
  private ResultPanelAndFrameMediator mediator;
 

  //{{DECLARE_CONTROLS
  private JButton openButton = null;
  private JButton cancelButton = null;
  private JButton searchButton = null;
  //}}
  
  // ResultSet and result panel for the owner
  ResultSet results = null;
  ResultPanel ownerPanel = null;
  
  /**
   * Construct a new instance of the query dialog
   *
   * @param framework A reference to the client framework 
   */
  public OpenDialogBox(ClientFramework framework, 
                                  ResultPanelAndFrameMediator myMediator)
  {
    this((Frame)framework, framework, myMediator);
  }
  
  /**
   * Construct a new instance of the query dialog
   *
   * @param parent The parent frame for this dialog
   * @param framework A reference to the client framework 
   */
  public OpenDialogBox(Frame parent, ClientFramework framework, 
                      ResultPanelAndFrameMediator myMediator)
  {
    super(parent);
    this.framework = framework;
    this.config = framework.getConfiguration();
    this.mediator = myMediator;
    
    setSize(800, 600);
    setTitle("Open");
    getContentPane().setLayout(new BorderLayout(0, 0));
    getContentPane().setBackground(Color.white);
    
    JPanel resultPanel = new JPanel();
    resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
    
    getContentPane().add(BorderLayout.CENTER, resultPanel);
    resultPanel.add(Box.createVerticalStrut(8));
    // Get owner resultset and onwer panel
    createOwnerPanel();
    
    // Add owner panel to the resulPanel
    resultPanel.add(ownerPanel);
    
    // Create a margin panel
    JPanel marginPanel = new JPanel();
    marginPanel.setLayout(new BoxLayout(marginPanel, BoxLayout.Y_AXIS));
    getContentPane().add(BorderLayout.SOUTH, marginPanel);
    // Add the margin between controlButtonsPanel and resultPanel
    marginPanel.add(Box.createVerticalStrut(8));
  
    
    // Create a controlbuttionPanel
    JPanel controlButtonsPanel = new JPanel();
    controlButtonsPanel.setLayout(new BoxLayout(controlButtonsPanel,
                                  BoxLayout.X_AXIS));
    controlButtonsPanel.add(Box.createHorizontalStrut(8));
    
    // Search button
    GUIAction searchAction = new GUIAction("Search...", new ImageIcon
       (getClass().getResource("/toolbarButtonGraphics/general/Search16.gif")),
       new SearchCommand(this, framework));
    searchAction.setToolTipText("Switch to search system to open packages from" 
                                + " the whole network");
  
    searchButton = new JButton(searchAction);
    // Set text on the left of icon        
    searchButton.setHorizontalTextPosition(SwingConstants.LEFT);
    controlButtonsPanel.add(searchButton);
    
    controlButtonsPanel.add(Box.createHorizontalGlue());
    
    // Open button
    GUIAction openAction = new GUIAction("Open", null, 
                                          new OpenPackageCommand(ownerPanel));    
    openButton = new JButton(openAction);   
    // Registor open button to mediator
    mediator.registerOpenButton(openButton);
    // After registor resultPanel and open button, init mediator
    mediator.init();
    controlButtonsPanel.add(openButton);
    
    controlButtonsPanel.add(Box.createHorizontalStrut(8));
    
    //Cancel button
    GUIAction cancelAction = new GUIAction("Cancel", null, 
                                                      new CancelCommand(this));
    cancelButton = new JButton(cancelAction);
    controlButtonsPanel.add(cancelButton);
    controlButtonsPanel.add(Box.createHorizontalStrut(8));
    
    // Add controlButtonsPanel to marginPanel
    marginPanel.add(controlButtonsPanel);
    // Add the margin between controlButtonPanel to the bottom line
    marginPanel.add(Box.createVerticalStrut(10));
   
    // Add a keyPressActionListener
    this.addKeyListener(new KeyPressActionListener());
    setVisible(false);
    String str = getOwnerQuery();
  }

 
  /**
   * Listens for key events coming from the dialog.  responds to escape and 
   * enter buttons.  escape toggles the cancel button and enter toggles the
   * Search button
   */
  class KeyPressActionListener extends java.awt.event.KeyAdapter
  {
    public KeyPressActionListener()
    {
    }
    
    public void keyPressed(KeyEvent e)
    {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        java.awt.event.ActionEvent event = new 
                       java.awt.event.ActionEvent(openButton, 0, "Search");
        
      } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        dispose();
        java.awt.event.ActionEvent event = new 
                       java.awt.event.ActionEvent(cancelButton, 0, "Cancel");
        
      }
    }
  }

 /**
   * Create the owner JTable with the appropriate query
   */
  private void createOwnerPanel()
  {
    // Create the tabbed pane for the owner queries
    Query ownerQuery = new Query(getOwnerQuery(), framework);
    results = ownerQuery.execute();
    ownerPanel = new ResultPanel(results, false, false, mediator);
    
   }// createOwnerPanel
  
   /**
   * Construct a query suitable for getting the owner documents
   */
  private String getOwnerQuery()
  {
    ConfigXML profile = framework.getProfile();
    StringBuffer searchtext = new StringBuffer();
    searchtext.append("<?xml version=\"1.0\"?>\n");
    searchtext.append("<pathquery version=\"1.0\">\n");
    String lastname = profile.get("lastname", 0);
    String firstname = profile.get("firstname", 0);
    searchtext.append("<querytitle>My Data (" + firstname + " " + lastname);
    searchtext.append(")</querytitle>\n");
    Vector returnDoctypeList = profile.get("returndoc");
    for (int i=0; i < returnDoctypeList.size(); i++) {
      searchtext.append("<returndoctype>");
      searchtext.append((String)returnDoctypeList.elementAt(i));
      searchtext.append("</returndoctype>\n");
    }
    Vector returnFieldList = profile.get("returnfield");
    for (int i=0; i < returnFieldList.size(); i++) {
      searchtext.append("<returnfield>");
      searchtext.append((String)returnFieldList.elementAt(i));
      searchtext.append("</returnfield>\n");
    }
    searchtext.append("<owner>" + framework.getUserName() + "</owner>\n");
    searchtext.append("<querygroup operator=\"UNION\">\n");
    searchtext.append("<queryterm casesensitive=\"true\" ");
    searchtext.append("searchmode=\"contains\">\n");
    searchtext.append("<value>%</value>\n");
    searchtext.append("</queryterm></querygroup></pathquery>");
    return searchtext.toString();
  }
  

  
  public static void main(String [] argus)
  {
    // Create the clientFramework object
    File configurationFile = null;
    File configDir = null;
    String configFile = "config.xml";
    configDir = new File(ConfigXML.getConfigDirectory());
    ResultPanelAndFrameMediator myMediator = new ResultPanelAndFrameMediator() ;
   
    try {
        
        configurationFile = new File(configDir, configFile);
        if(configurationFile.createNewFile() || configurationFile.length() == 0) 
        {
          FileOutputStream out = new FileOutputStream(configurationFile);
          ClassLoader cl = Thread.currentThread().getContextClassLoader();
          InputStream configInput = cl.getResourceAsStream(configFile);
          if (configInput == null) {
         ClientFramework.debug(1, "Could not find default configuration file.");
            System.exit(0);
          }
          byte buf[] = new byte[4096];
          int len = 0;
          while ((len = configInput.read(buf, 0, 4096)) != -1) {
            out.write(buf, 0, len);
          }
          configInput.close();
          out.close();
        }
      } catch (IOException ioe) {
        ClientFramework.debug(1, "Error copying config: " + ioe.getMessage());
        ClientFramework.debug(1, ioe.getClass().getName());
        ioe.printStackTrace(System.err);
        System.exit(0);
      }
      
      // Open the configuration file
      //ConfigXML config = new ConfigXML(configFile);
      ConfigXML config = null;
      try
      {
        config = new ConfigXML(configurationFile.getAbsolutePath());
      }
      catch (FileNotFoundException e)
      {
        e.printStackTrace(System.err);
      }
      // Create a new instance of our application's frame
      ClientFramework clf = new ClientFramework(config);
      String profileDir = ConfigXML.getConfigDirectory() + File.separator +
                                     config.get("profile_directory", 0);
      String currentProfile = config.get("current_profile", 0);
      String profileName = profileDir + File.separator + currentProfile + 
                        File.separator + currentProfile + ".xml";
      ConfigXML profile = null;
      try
      {
        profile = new ConfigXML(profileName);
      }
      catch (FileNotFoundException e)
      {
        e.printStackTrace(System.err);
      }
      clf.setProfile(profile);
      OpenDialogBox open = new OpenDialogBox(clf, myMediator);
      open.show();
    
  }
 
}
