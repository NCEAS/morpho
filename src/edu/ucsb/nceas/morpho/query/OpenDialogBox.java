/**
 *       Name: OpenDialogBox.java
 *    Purpose: Visual display for OpenDialogBox
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @Jing Tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-26 00:48:23 $'
 * '$Revision: 1.14 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
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
import javax.swing.WindowConstants;
import javax.swing.SwingConstants;

/**
 * A dialog box for user to open a datapakcage
 */
public class OpenDialogBox extends JDialog
{
  /** A reference to the container Morpho application */
  private Morpho morpho = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** the reference to mediator */
  private ResultPanelAndFrameMediator mediator = null;
  
  /** the reference to the owner query */
  private Query ownerQuery = null;
  
  /** the reference to the parent of dialog */
  private MorphoFrame parentFrame = null;
  
 
  //{{DECLARE_CONTROLS
  private JButton openButton = null;
  private JButton cancelButton = null;
  private JButton searchButton = null;
  //}}
  
  // ResultSet and result panel for the owner
  private ResultSet results = null;
  private ResultPanel ownerPanel = null;
  
  // DIMENTION Factor to parent
  private static final double DIMENSIONFACTOR = 0.9;
  private static final int PADDINGWIDTH = 8;
  
  /**
   * Construct a new instance of the query dialog
   *
   * @param parent  The parent frame for this dialog
   * @param morpho  A reference to the Morpho application
   * @param myQuery a Query to get the user's own packages
   */
  public OpenDialogBox(MorphoFrame parent, Morpho morpho, Query myQuery)
  {
    super(parent);
    this.parentFrame = parent;
    this.morpho = morpho;
    this.config = morpho.getConfiguration();
    this.mediator = new ResultPanelAndFrameMediator();
    this.ownerQuery = myQuery;
  
    // Set OpenDialog size depent on parent size
    int parentWidth = parent.getWidth();
    int parentHeight = parent.getHeight();
    int dialogWidth = 780;
    int dialogHeight = 500;
    setSize(dialogWidth, dialogHeight);
    
    // Set location of dialog, it shared same center of parent
    double parentX = parent.getLocation().getX();
    double parentY = parent.getLocation().getY();
    double centerX = parentX + 0.5 * parentWidth;
    double centerY = parentY + 0.5 * parentHeight;
    int dialogX = (new Double(centerX - 0.5 * dialogWidth)).intValue();
    int dialogY = (new Double(centerY - 0.5 * dialogHeight)).intValue();
    setLocation(dialogX, dialogY);
    
    setTitle("Open");
    // Set the default close operation is dispose
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    // Set the border layout as layout
    getContentPane().setLayout(new BorderLayout(0, 0));

    // Create padding
    getContentPane().add(BorderLayout.NORTH, 
                                      Box.createVerticalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.EAST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.WEST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
   
    // Create result panel
    createOwnerPanel();
    ownerPanel.setBackground(Color.white);
    getContentPane().add(BorderLayout.CENTER, ownerPanel);
    
    // Create bottom box
    Box bottomBox = Box.createVerticalBox();
    getContentPane().add(BorderLayout.SOUTH, bottomBox);
    //Create padding between result panel and Contorl button box
    bottomBox.add(Box.createVerticalStrut(PADDINGWIDTH));

    // Create a controlbuttionBox
    Box controlButtonsBox = Box.createHorizontalBox();
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));
    
    // Search button
    GUIAction searchAction = new GUIAction("Search...", new ImageIcon
       (getClass().getResource("/toolbarButtonGraphics/general/Search16.gif")),
       new SearchCommand(this, morpho));
    searchAction.setToolTipText("Switch to search system to open packages from" 
                                + " the whole network");
  
    searchButton = new JButton(searchAction);
    // Set text on the left of icon        
    searchButton.setHorizontalTextPosition(SwingConstants.LEFT);
    controlButtonsBox.add(searchButton);
    
    controlButtonsBox.add(Box.createHorizontalGlue());
    
    // Open button
    GUIAction openAction = new GUIAction("Open", null, 
                                  new OpenPackageCommand(ownerPanel));    
    openButton = new JButton(openAction);   
    // Registor open button to mediator
    mediator.registerOpenButton(openButton);
    // After registor resultPanel and open button, init mediator
    mediator.init();
    controlButtonsBox.add(openButton);
    
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));
    
    //Cancel button
    GUIAction cancelAction = new GUIAction("Cancel", null, 
                                                      new CancelCommand(this));
    cancelButton = new JButton(cancelAction);
    controlButtonsBox.add(cancelButton);
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));
    
    // Add controlButtonsBox to bottomBox
    bottomBox.add(controlButtonsBox);
    // Add the margin between controlButtonPanel to the bottom line
    bottomBox.add(Box.createVerticalStrut(10));
   
    // Add a keyPressActionListener
    this.addKeyListener(new KeyPressActionListener());
    setVisible(false);
   
  }

  /**
   * Method to get the parent morphoFrame of dialog
   */
  public MorphoFrame getParentFrame()
  {
    return parentFrame;
  }//getParent
 
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
    results = ownerQuery.execute();
    ownerPanel = new ResultPanel(this, results, mediator);
    
   }// createOwnerPanel
  
  

  
  public static void main(String [] argus)
  {
    // Create the clientFramework object
    File configurationFile = null;
    File configDir = null;
    String configFile = "config.xml";
    configDir = new File(ConfigXML.getConfigDirectory());
    
    try {
        
        configurationFile = new File(configDir, configFile);
        if(configurationFile.createNewFile() || configurationFile.length() == 0) 
        {
          FileOutputStream out = new FileOutputStream(configurationFile);
          ClassLoader cl = Thread.currentThread().getContextClassLoader();
          InputStream configInput = cl.getResourceAsStream(configFile);
          if (configInput == null) {
            Log.debug(1, "Could not find default configuration file.");
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
        Log.debug(1, "Error copying config: " + ioe.getMessage());
        Log.debug(1, ioe.getClass().getName());
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
      Morpho morpho = new Morpho(config);
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
      morpho.setProfile(profile);
      //OpenDialogBox open = new OpenDialogBox(clf);
      //open.setVisible(true);
  }
 
}
