/**
 *  '$RCSfile: NewPackageMetadataWizard.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-27 21:36:39 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;

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
public class NewPackageMetadataWizard extends JDialog
                                      implements ActionListener
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

  JRadioButton createNew = new JRadioButton("New Description");
  JRadioButton existingFile = new JRadioButton("Open Exising Description");
  JRadioButton tableRadioButton = new JRadioButton("Table Description");
  JRadioButton fieldRadioButton = new JRadioButton("Field Description");
  JRadioButton researchRadioButton = new JRadioButton("Research Description");
  JRadioButton projectRadioButton = new JRadioButton("Project Description");
  JRadioButton softwareRadioButton = new JRadioButton("Software Description");
  JTextField fileTextField = new JTextField();
  
  /**
   * Construct a dialog and set the framework
   *
   * @param cont the container framework that created this ProfileDialog
   */
  public NewPackageMetadataWizard(ClientFramework cont) {
    this(cont, true);
  }

  /**
   * Construct the dialog
   */
  public NewPackageMetadataWizard(ClientFramework cont, boolean modal)
  {
    super((Frame)cont, modal);

    framework = cont;

    numScreens = 2;
    currentScreen = 0;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setTitle("Add New Descriptions");
    getContentPane().setLayout(new BoxLayout(getContentPane(),
                               BoxLayout.X_AXIS));
    setVisible(false);

    createNew.setSelected(true);
    ButtonGroup group1 = new ButtonGroup();
    group1.add(createNew);
    group1.add(existingFile);
    
    tableRadioButton.setSelected(true);
    tableRadioButton.setToolTipText("Information about the data tables in " +
                                    "data set.");
    fieldRadioButton.setToolTipText("Information about the fields (columns) " +
                                    "in your data tables.");
    researchRadioButton.setToolTipText("Information about the methods " + 
                                       "involved in the research represented " +
                                       "by your data set.");
    projectRadioButton.setToolTipText("Information about the overall project " +
                                      "of which your data set is a part.");
    softwareRadioButton.setToolTipText("Information about any software tools" +
                                       "produced or used in the creation " + 
                                       "of your data set.");
    ButtonGroup group2 = new ButtonGroup();
    group2.add(tableRadioButton);
    group2.add(fieldRadioButton);
    group2.add(researchRadioButton);
    group2.add(projectRadioButton);
    group2.add(softwareRadioButton);
    
    
    JPanel helpPanel = new JPanel();
    helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
    helpPanel.setBackground(Color.white);
    helpPanel.setMaximumSize(new Dimension(150,400));
    helpPanel.setPreferredSize(new Dimension(150,400));
    helpPanel.setBorder(BorderFactory.createLoweredBevelBorder());
    helpPanel.add(Box.createRigidArea(new Dimension(8,8)));
    ImageIcon logoIcon = 
              new ImageIcon(framework.getClass().getResource("logo-icon.gif"));
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
    headLabel.setText("Add New Descriptions");
    ImageIcon head = new ImageIcon(
                         framework.getClass().getResource("smallheader-bg.gif"));
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
    previousButton = new JButton("Previous", new ImageIcon( framework.getClass().
          getResource("/toolbarButtonGraphics/navigation/Back16.gif")));
    previousButton.setHorizontalTextPosition(SwingConstants.RIGHT);
    buttonPanel.add(previousButton);
    buttonPanel.add(Box.createHorizontalStrut(8));
    forwardIcon = new ImageIcon( framework.getClass().
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

    config = framework.getConfiguration();
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

    if (0 == currentScreen) 
    {
      String helpText = "<html><p>This wizard will assist you in creating or " +
                        "inserting new descriptions or data files into your " +
                        "Data Package.  You will be asked several questions " +
                        "about the type of information that you would like to" +
                        "provide then the wizard will insert your new " +
                        "information into the package.</p></html>";
      helpLabel.setText(helpText);

      screenPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(8,8,8,8),
                            ""));
      
      JLabel initLabel = new JLabel("<html><p><font color=black>What kind of " +
                                    "information would you like " +
                                    "to provide?  To add an existing file " +
                                    "on your system, select 'Existing " +
                                    "Description'. " +
                                    "To add new descriptions from scratch, " +
                                    "select 'New Description'." +
                                    "</font></p></html>");
      initLabel.setMaximumSize(new Dimension(375, 100));
      screenPanel.add(initLabel);
      screenPanel.setMaximumSize(new Dimension(400, 300));
      screenPanel.setPreferredSize(new Dimension(400, 300));
      JPanel layoutpanel = new JPanel();
      layoutpanel.setLayout(new BoxLayout(layoutpanel, BoxLayout.Y_AXIS));
      layoutpanel.add(createNew);
      layoutpanel.add(existingFile);
      screenPanel.add(layoutpanel);
      screenPanel.setLayout(new GridLayout(0,1));
    } 
    else if (1 == currentScreen) 
    {
      if(createNew.isSelected())
      {
        String helpText = "<html>Select the type of description that you " +
                          "to add. Holding your mouse over any of the " +
                          "selections will give you more information on that " +
                          "item.";
        helpLabel.setText(helpText);
        JLabel initLabel = new JLabel("<html><p><font color=black>What kind of " +
                                    "description would you like " +
                                    "to add?</font></p></html>");
        initLabel.setMaximumSize(new Dimension(375, 100));
        JPanel layoutpanel = new JPanel();
        layoutpanel.setLayout(new BoxLayout(layoutpanel, BoxLayout.Y_AXIS));
        layoutpanel.add(initLabel);
        layoutpanel.add(tableRadioButton);
        layoutpanel.add(fieldRadioButton);
        layoutpanel.add(researchRadioButton);
        layoutpanel.add(projectRadioButton);
        layoutpanel.add(softwareRadioButton);
        screenPanel.add(layoutpanel);
        screenPanel.setLayout(new GridLayout(0,1));
      }
      else
      {
        String helpText = "<html>Select a file to add to your data package." +
                          "</html>";
        helpLabel.setText(helpText);
        JLabel initLabel = new JLabel("<html><p><font color=black>Choose a " +
                                      "file.</font></p></html>");
        initLabel.setMaximumSize(new Dimension(375, 100));
        JPanel layoutpanel = new JPanel();
        //layoutpanel.setLayout(new BoxLayout(layoutpanel, BoxLayout.X_AXIS));
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(this);
        fileTextField.setColumns(20);
        screenPanel.add(initLabel);
        layoutpanel.add(fileTextField);
        layoutpanel.add(browseButton);
        screenPanel.add(layoutpanel);
        screenPanel.setLayout(new GridLayout(0,1));
      }
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
      String profileName = profilePath + File.separator + username + ".xml";
      File profileDir = new File(profilePath);
      if (!profileDir.mkdir()) {
        // Error creating the directory
        currentScreen = 0;
        layoutScreen();
        String messageText = "A profile for user \"" + username +
                             "\" already exists.  Would you like to use it?" +
                             "\n\nUse existing profile?\n";
        int result = JOptionPane.showConfirmDialog(this, messageText, 
                                                   "Use existing profile?", 
                                                   JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
          try {
            ConfigXML profile = new ConfigXML(profileName);
            // Log into metacat
            framework.setPassword(passwordField.getText());
            framework.setProfile(profile);
            framework.logIn();
  
            // Get rid of the dialog
            setVisible(false);
            dispose();
          } catch (FileNotFoundException fnf) {
            messageText = "Sorry, I tried, but it looks like that profile\n" +
                          "is corrupted.  You'll have to choose another " +
                          "username.";
            JOptionPane.showMessageDialog(this, messageText);      
          }
        } else {
          messageText = "OK, then please choose another username.\n";
          JOptionPane.showMessageDialog(this, messageText);      
        }
      } else {
        try {
          // Copy default profile to the new directory
          String defaultProfile = config.get("default_profile", 0);
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
          if (! profile.set("scope", 0, username)) {
            success = profile.insert("scope", username);
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

          // Copy sample data to the data directory
          Hashtable tokens = new Hashtable();
          tokens.put("SCOPE", username);
          String samplePath = config.get("samples_directory", 0);
          File sampleDir = new File(samplePath);
          File[] samplesList = sampleDir.listFiles();
          for (int n=0; n < samplesList.length; n++) {
            File srcFile = samplesList[n];
            if (srcFile.isFile()) {
              String destDirName = dataPath + File.separator + username;
              File destDir = new File(destDirName);
              destDir.mkdirs();
              String destName = destDirName + File.separator + 
                                srcFile.getName();
              ClientFramework.debug(9, destName);
              FileUtils.copy(srcFile.getAbsolutePath(), destName, tokens);
            }
          }
           
          // Create a metacat user
 
          // Log into metacat
          framework.setPassword(passwordField.getText());
          framework.setProfile(profile);
          framework.logIn();

          // Get rid of the dialog
          setVisible(false);
          dispose();
   
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
  
  public void actionPerformed(ActionEvent e) 
  {
    String command = e.getActionCommand();
    if(command.equals("Browse..."))
    {
      JFileChooser filechooser = new JFileChooser();
      File datafile;
      filechooser.showOpenDialog(this);
      datafile = filechooser.getSelectedFile();
      fileTextField.setText(datafile.getAbsolutePath());
    }
  }
}
