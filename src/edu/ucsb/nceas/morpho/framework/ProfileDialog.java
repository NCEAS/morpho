/**
 *  '$RCSfile: ProfileDialog.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-06-11 02:13:38 $'
 * '$Revision: 1.3 $'
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
import java.awt.event.*;
import javax.swing.*;

/**
 * A graphical window for creating a new user profile with user provided
 * information.
 */
public class ProfileDialog extends JDialog
{
  ConfigXML config;
  ClientFramework framework = null;
  /** the total number of screens to be processed */
  int numScreens;
  /** the screen currently displaying (indexed from 0 to numScreens-1) */
  int currentScreen;

  JLabel helpLabel = new JLabel();
  JPanel screenPanel = null;
  JButton previousButton = null;
  JButton nextButton = null;
  JButton cancelButton = new JButton();

  ImageIcon forwardIcon = null;
  JTextField firstNameField = new JTextField();
  JTextField lastNameField = new JTextField();
  JTextField usernameField = new JTextField();
  JPasswordField passwordField = new JPasswordField();
  JPasswordField passwordField2 = new JPasswordField();
  JTextField constructionField = new JTextField();

  /**
   * Construct a dialog and set the framework
   *
   * @param cont the container framework that created this ProfileDialog
   */
  public ProfileDialog(ClientFramework cont) {
    this(cont, true);
  }

  /**
   * Construct the dialog
   */
  public ProfileDialog(ClientFramework cont, boolean modal)
  {
    super((Frame)cont, modal);

    framework = cont;

    numScreens = 2;
    currentScreen = 0;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setTitle("New Profile");
    getContentPane().setLayout(new BoxLayout(getContentPane(),
                               BoxLayout.X_AXIS));
    setVisible(false);

    JPanel helpPanel = new JPanel();
    helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
    helpPanel.setBackground(Color.white);
    helpPanel.setMaximumSize(new Dimension(150,400));
    helpPanel.setPreferredSize(new Dimension(150,400));
    helpPanel.setBorder(BorderFactory.createLoweredBevelBorder());
    helpPanel.add(Box.createRigidArea(new Dimension(8,8)));
    ImageIcon logoIcon = 
              new ImageIcon(getClass().getResource("logo-icon.gif"));
    JLabel imageLabel = new JLabel();
    imageLabel.setIcon(logoIcon);
    helpPanel.add(imageLabel);
    helpLabel.setText(
              "<html>This is a whole bunch of help text.</html>");
    helpLabel.setForeground(Color.black);
    helpLabel.setFont(new Font("Dialog", Font.BOLD, 12));
    helpLabel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    helpLabel.setVerticalAlignment(SwingConstants.TOP);
    helpPanel.add(helpLabel);
    getContentPane().add(helpPanel);

    getContentPane().add(Box.createRigidArea(new Dimension(8,8)));

    JPanel entryPanel = new JPanel();
    entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
    entryPanel.add(Box.createVerticalStrut(8));

    JLabel headLabel = new JLabel();
    headLabel.setText("New Profile");
    ImageIcon head = new ImageIcon(
                         getClass().getResource("smallheader-bg.gif"));
    headLabel.setIcon(head);
    headLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    headLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headLabel.setAlignmentY(Component.LEFT_ALIGNMENT);
    headLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    headLabel.setForeground(Color.black);
    headLabel.setFont(new Font("Dialog", Font.BOLD, 14));
    headLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    JPanel headPanel = new JPanel();
    headPanel.setLayout(new FlowLayout());
    headPanel.add(headLabel);
    entryPanel.add(headPanel);
    entryPanel.add(Box.createVerticalStrut(8));

    screenPanel = new JPanel();
    screenPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    entryPanel.add(screenPanel);
    entryPanel.add(Box.createVerticalGlue());
    entryPanel.add(Box.createRigidArea(new Dimension(8,8)));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(Box.createHorizontalGlue());
    cancelButton.setText("Cancel");
    cancelButton.setActionCommand("Cancel");
    cancelButton.setEnabled(true);
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createHorizontalStrut(8));
    previousButton = new JButton("Previous", new ImageIcon( getClass().
          getResource("/toolbarButtonGraphics/navigation/Back16.gif")));
    previousButton.setHorizontalTextPosition(SwingConstants.RIGHT);
    buttonPanel.add(previousButton);
    buttonPanel.add(Box.createHorizontalStrut(8));
    forwardIcon = new ImageIcon( getClass().
          getResource("/toolbarButtonGraphics/navigation/Forward16.gif"));
    nextButton = new JButton("Next", forwardIcon);
    nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
    nextButton.setEnabled(true);
    buttonPanel.add(nextButton);
    buttonPanel.add(Box.createHorizontalStrut(8));

    entryPanel.add(buttonPanel);
    entryPanel.add(Box.createVerticalStrut(8));
    getContentPane().add(entryPanel);
    getContentPane().add(Box.createHorizontalGlue());
    getContentPane().add(Box.createHorizontalStrut(8));

    // Layout the first screen
    layoutScreen();

    // Size and center the frame
    pack();
    setResizable(false);
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2 ,
            (screenDim.height - frameDim.height) /2);

    // register a listener for ActionEvents
    ActionHandler myActionHandler = new ActionHandler();
    previousButton.addActionListener(myActionHandler);
    nextButton.addActionListener(myActionHandler);
    cancelButton.addActionListener(myActionHandler);

    config = new ConfigXML("lib/config.xml");
  }

  /**
   * Listener used to detect button presses
   */
  private class ActionHandler implements ActionListener
  {
    public void actionPerformed(ActionEvent event)
    {
      Object object = event.getSource();
      if (object == previousButton) {
        previousButtonHandler(event);
      } else if (object == nextButton) {
        nextButtonHandler(event);
      } else if (object == cancelButton) {
        cancelButtonHandler(event);
      }
    }
  }

  /**
   * Perform actions associated with the Previous button
   */
  private void previousButtonHandler(ActionEvent event)
  {
    if (currentScreen > 0) {
      currentScreen--;
      layoutScreen();
    }
  }
  
  /**
   * Perform actions associated with the Next button
   */
  private void nextButtonHandler(ActionEvent event)
  {
    if (currentScreen == numScreens-1) {
      createProfile();
    } else {
      currentScreen++;
      layoutScreen();
    }
  }

  /**
   * Perform actions associated with the Disconnect button
   */
  private void cancelButtonHandler(ActionEvent event)
  {
    dispose();
  }

  /**
   * Lay out labels and fields in an orderly grid.
   */
  private void addLabelTextRows(JLabel[] labels,
                                JTextField[] textFields,
                                GridBagLayout gridbag,
                                Container container) {
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.EAST;
    int numLabels = labels.length;

    for (int i = 0; i < numLabels; i++) {
      c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
      c.fill = GridBagConstraints.NONE;      //reset to default
      c.weightx = 0.0;                       //reset to default
      c.anchor = GridBagConstraints.EAST;
      gridbag.setConstraints(labels[i], c);
      container.add(labels[i]);

      c.gridwidth = GridBagConstraints.REMAINDER;     //end row
      //c.fill = GridBagConstraints.HORIZONTAL; // fill remaining space
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.WEST;
      gridbag.setConstraints(textFields[i], c);
      container.add(textFields[i]);
    }
  }

  /** 
   * Put the new components on the screen and redraw.
   */
  private void layoutScreen()
  {
    screenPanel.removeAll();

    GridBagLayout gridbag = new GridBagLayout();
    screenPanel.setLayout(gridbag);

    if (0 == currentScreen) {
      String helpText = "<html><p>Enter your first and last name, " +
                        "a username of your choosing, your organization, " +
                        "your desired password, and " +
                        "the same password again (for verification).</p>" +
                        "<p>This information will be used to store your " +
                        "data on your computer in a location that is " +
                        "distinct from other Morpho users.</p></html>";
      helpLabel.setText(helpText);

      screenPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(8,8,8,8),
                            "Basic Information"));
      JLabel firstNameLabel = new JLabel();
      JLabel lastNameLabel = new JLabel();
      JLabel usernameLabel = new JLabel();
      JLabel passwordLabel = new JLabel();
      JLabel passwordLabel2 = new JLabel();
      firstNameLabel.setText("First name: ");
      lastNameLabel.setText("Last name: ");
      usernameLabel.setText("Username: ");
      passwordLabel.setText("Password: ");
      passwordLabel2.setText("Password again: ");
      firstNameLabel.setForeground(Color.black);
      lastNameLabel.setForeground(Color.black);
      usernameLabel.setForeground(Color.black);
      passwordLabel.setForeground(Color.black);
      passwordLabel2.setForeground(Color.black);
      firstNameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      lastNameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      usernameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      passwordLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      passwordLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
      firstNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      lastNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      passwordLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
  
      firstNameField.setColumns(15);
      lastNameField.setColumns(15);
      usernameField.setColumns(15);
      passwordField.setColumns(10);
      passwordField2.setColumns(10);
  
      JLabel[] labels = {firstNameLabel, lastNameLabel, usernameLabel,
                         passwordLabel, passwordLabel2};
      JTextField[] textFields = {firstNameField, lastNameField, usernameField,
                         passwordField, passwordField2};
      addLabelTextRows(labels, textFields, gridbag, screenPanel);
    } else if (1 == currentScreen) {
      String helpText = "<html>Enter your contact information.  This " +
                        "data will be used to pre-fill in metadata " +
                        "fields when you are entering data, and so will " +
                        "increase entry efficiency.</html>";
      helpLabel.setText(helpText);
      screenPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(8,8,8,8),
                            "Contact Information"));
      JLabel constructionLabel = new JLabel("Not yet implemented.");
      constructionLabel.setForeground(Color.black);
      constructionLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      constructionField.setColumns(15);
      JLabel[] labels = {constructionLabel};
      JTextField[] textFields = {constructionField};
      addLabelTextRows(labels, textFields, gridbag, screenPanel);
    }

    // Set button states properly
    if (0 == currentScreen) {
      previousButton.setEnabled(false);
    } else {
      previousButton.setEnabled(true);
    }

    if (currentScreen == numScreens-1) {
      nextButton.setEnabled(true);
      nextButton.setText("Finished");
      nextButton.setIcon(null);
    } else {
      nextButton.setEnabled(true);
      nextButton.setText("Next");
      nextButton.setIcon(forwardIcon);
    }

    // Repaint the screen
    screenPanel.validate();
    screenPanel.paint(screenPanel.getGraphics());
  }

  /**
   * Validate the contents of required and critical fields before actually
   * creating the profile.  If there are problems, return false, otherwise true
   *
   * @returns boolean true if fields are valid, false otherwise
   */
  private boolean validateFieldContents()
  {
    boolean fieldsAreValid = true;
    if (usernameField.getText() == null || 
        usernameField.getText().equals("")) {
      fieldsAreValid = false;
    }

    if (!passwordField.getText().equals(passwordField2.getText())) {
      fieldsAreValid = false;
    }
    return fieldsAreValid;
  }

  /**
   * Process the data and create a new profile.
   */
  private void createProfile()
  {
    if (validateFieldContents()) {
      // Create a profile directory
      String profileDirName = config.get("profile_directory", 0);
      File profileDirFile = new File(profileDirName);
      if (!profileDirFile.exists()) {
        if (!profileDirFile.mkdir()) {
          // Error creating the directory
          currentScreen = 0;
          layoutScreen();
          String messageText = "Error creating the profiles directory.\n";
          JOptionPane.showMessageDialog(this, messageText);      
        }
      }
      String username = usernameField.getText();
      String profilePath = profileDirName + File.separator + username;
      File profileDir = new File(profilePath);
      if (!profileDir.mkdir()) {
        // Error creating the directory
        currentScreen = 0;
        layoutScreen();
        String messageText = "A profile for user \"" + username +
                             "\" already exists.  Please choose another " +
                             "username.\n";
        JOptionPane.showMessageDialog(this, messageText);      
      } else {
        try {
          // Copy default options to that directory
          String defaultProfile = config.get("default_profile", 0);
          String profileName = profilePath + File.separator + username + ".xml";
          FileUtils.copy(defaultProfile, profileName);

          // Store the collected information in the profile
          ConfigXML profile = new ConfigXML(profileName);
          boolean success = false;
          if (! profile.set("username", 0, username)) {
            success = profile.insert("username", username);
          }
          if (! profile.set("firstname", 0, firstNameField.getText())) {
            success = profile.insert("firstname", firstNameField.getText());
          }
          if (! profile.set("lastname", 0, lastNameField.getText())) {
            success = profile.insert("lastname", lastNameField.getText());
          }

          profile.save();

          // Create our directories for user data
          String dataDirName = profile.get("datadir", 0);
          String dataPath = profilePath + File.separator + dataDirName;
          File dataDir = new File(dataPath);
          success = dataDir.mkdir();

          String cacheDirName = profile.get("cachedir", 0);
          String cachePath = profilePath + File.separator + cacheDirName;
          File cacheDir = new File(cachePath);
          success = cacheDir.mkdir();

          String tempDirName = profile.get("tempdir", 0);
          String tempPath = profilePath + File.separator + tempDirName;
          File tempDir = new File(tempPath);
          success = tempDir.mkdir();

          // Create a metacat user 
  
          // Get rid of the dialog
          setVisible(false);
          dispose();
   
          // Log into metacat
          framework.setProfile(profile);
          framework.setPassword(passwordField.getText());
          framework.logIn();

        } catch (IOException ioe) {
          currentScreen = 0;
          layoutScreen();
          String messageText = "Error creating profile for user \"" + 
                               username + "\".  Please try again.\n";
          JOptionPane.showMessageDialog(this, messageText);      
        }
      }
    } else {
      currentScreen = 0;
      layoutScreen();
      String messageText = "Some required information was invalid.\n\n" +
                           "Please check that you have provided a\n" +
                           "username and that the two passwords match.\n";
      JOptionPane.showMessageDialog(this, messageText);      
    }
  }
}
