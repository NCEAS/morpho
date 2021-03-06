/**
 *  '$RCSfile: ProfileDialog.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-08-26 03:58:27 $'
 * '$Revision: 1.30 $'
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

/**
 * A graphical window for creating a new user profile with user provided
 * information.
 */
public class ProfileDialog extends JDialog implements StateChangeListener
{
  public static final String DATADIRELEMENTNAME= "datadir";
  public static final String CACHEDIRELEMENTNAME = "cachedir";
  public static final String TEMPDIRELEMENTNAME = "tempdir";
  public static final String INCOMPLETEDIRELEMENTNAME = "incompletedir" ;
  public static final String PROFILENAMEELEMENTNAME = "profilename";
  public static final String QUERIESDIRELEMENTNAME = "queriesdir";
  public static final String DOT = ".";
  public static final String SYSTEMMETADATA = "systemmetadata";
  public static final String SYSTEMMETADATADIRNAME = DOT+ SYSTEMMETADATA;
  public static final String D1_CLIENT_CERTIFICATE_LOCATION = "D1Client.certificate.file";
  ConfigXML config;
  Morpho framework = null;
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
  JTextField profileNameField = new JTextField();
  JTextField firstNameField = new JTextField();
  JTextField lastNameField = new JTextField();
  JTextField scopeField = new JTextField();
  public static final String CORRECTIONEMLPROFILEPATH = "eml201corrected";
  public static final String IDFILEMAPUPDATEDPATH = "identifierUpdated";
  
  public static final String[] ILLEGAL_SCOPE_CHARACTERS = 
	  new String[] {" ", "&", "#", ",", ".", "+", "-", "(", ")"};

  KeyPressActionListener keyPressListener = new KeyPressActionListener();

  /**
   * Construct a dialog and set the framework
   *
   * @param cont the container framework that created this ProfileDialog
   */
  public ProfileDialog(Morpho cont) {
    this(cont, true);
  }

  /**
   * Construct the dialog
   */
  public ProfileDialog(Morpho cont, boolean modal)
  {
    super(UIController.getInstance().getCurrentActiveWindow());
    setModal(true);
    framework = cont;

    numScreens = 2;
    currentScreen = 0;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setTitle(/*"New Profile"*/ Language.getInstance().getMessage("NewProfile"));
    getContentPane().setLayout(new BoxLayout(getContentPane(),
                               BoxLayout.X_AXIS));
    setVisible(false);

    JPanel helpPanel = new JPanel();
    helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
    helpPanel.setBackground(Color.white);
    Dimension helpDim = new Dimension(250, 400);
    helpPanel.setMaximumSize(helpDim);
    helpPanel.setPreferredSize(helpDim);
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
    headLabel.setText(/*"New Profile"*/ Language.getInstance().getMessage("NewProfile"));
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
    cancelButton.setText(/*"Cancel"*/ Language.getInstance().getMessage("Cancel"));
    cancelButton.setActionCommand("Cancel");
    cancelButton.setMnemonic(KeyEvent.VK_C);
    cancelButton.setEnabled(true);
    addKeyListenerToComponent(cancelButton);
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createHorizontalStrut(8));
    previousButton = new JButton(/*"Previous"*/ Language.getInstance().getMessage("Previous"),
    								new ImageIcon( getClass().
          getResource("/toolbarButtonGraphics/navigation/Back16.gif")));
    previousButton.setHorizontalTextPosition(SwingConstants.RIGHT);
    previousButton.setMnemonic(KeyEvent.VK_P);
    addKeyListenerToComponent(previousButton);
    buttonPanel.add(previousButton);
    buttonPanel.add(Box.createHorizontalStrut(8));
    forwardIcon = new ImageIcon( getClass().
          getResource("/toolbarButtonGraphics/navigation/Forward16.gif"));
    nextButton = new JButton("Next", forwardIcon);
    nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
    nextButton.setMnemonic(KeyEvent.VK_N);
    addKeyListenerToComponent(nextButton);
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
    profileNameField.requestFocus();

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
    //this.addKeyListener(keyPressListener);

    config = framework.getConfiguration();
    
    StateChangeMonitor.getInstance().addStateChangeListener(StateChangeEvent.ACCESS_LIST_MODIFIED, this);
    
  }

  /**
   * Listens for key events coming from the dialog.  responds to escape and
   * enter buttons.  escape toggles the cancel button and enter toggles the
   * next button
   */
  class KeyPressActionListener extends java.awt.event.KeyAdapter
  {
    public KeyPressActionListener()
    {
    }

    public void keyPressed(KeyEvent e)
    {
	    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        //if enter was pressed whilst a button was in focus, just click it
        if (e.getSource() instanceof JButton){
          ((JButton)e.getSource()).doClick();
        } else {
          java.awt.event.ActionEvent event = new
                         java.awt.event.ActionEvent(nextButton, 0, "Next");
          nextButtonHandler(event);
        }
      } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        dispose();
        java.awt.event.ActionEvent event = new
                       java.awt.event.ActionEvent(cancelButton, 0, "Cancel");
        cancelButtonHandler(event);
      }
    }
  }


  /**
   * Adds listener to all components contained in passed array
   */
  private void addKeyListenerToComponents(Component[] components){
    if (components!=null){
      for (int i=0;i<components.length;i++){
        addKeyListenerToComponent(components[i]);
      }
    } else {
      Log.debug(10,
            "ProfileDialog.addKeyListenerToComponents() - received null array");
      return;
    }
  }

   /**
   * Adds listener to passed Component object
   */
  private void addKeyListenerToComponent(Component component){
    if (component!=null) {
          component.addKeyListener(keyPressListener);
    } else {
      Log.debug(10,
              "ProfileDialog.addKeyListenerToComponent() - received NULL arg");
      return;
    }
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
                                JComponent[] components,
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
      gridbag.setConstraints(components[i], c);
      container.add(components[i]);
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
      String helpText = "<html><p>"
    	  				/*+ "Enter the name for this profile and your first and last name."*/ 
                        + Language.getInstance().getMessage("BasicInformation.helpText")
                        + "</p></html>";
      helpLabel.setText(helpText);

      screenPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(8,8,8,8),
                            /*"Basic Information"*/ Language.getInstance().getMessage("BasicInformation")));
      JLabel profileNameLabel = new JLabel();
      JLabel firstNameLabel = new JLabel();
      JLabel lastNameLabel = new JLabel();
      profileNameLabel.setText(/*"Name of profile: "*/ Language.getInstance().getMessage("NameOfProfile") + ": ");
      firstNameLabel.setText(/*"First name: "*/ Language.getInstance().getMessage("FirstName") + ": ");
      lastNameLabel.setText(/*"Last name: "*/ Language.getInstance().getMessage("LastName") + ": ");
      profileNameLabel.setForeground(Color.black);
      firstNameLabel.setForeground(Color.black);
      lastNameLabel.setForeground(Color.black);
      profileNameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      firstNameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      lastNameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      profileNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      firstNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      lastNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);

      profileNameField.setColumns(15);
      firstNameField.setColumns(15);
      lastNameField.setColumns(15);

      JLabel[] labels = {profileNameLabel, firstNameLabel, lastNameLabel};
      JTextField[] textFields = {profileNameField, firstNameField,
                        lastNameField};
      addLabelTextRows(labels, textFields, gridbag, screenPanel);
      addKeyListenerToComponents(textFields);
    } else if (1 == currentScreen) {
      String helpText = 
    	  "<html><p>"
    	  /*+ "Enter a short identifier prefix for this profile. "*/ 
    	  + Language.getInstance().getMessage("DataPackageIdentification.helpText_1")
    	  /*
    	  + " All data packages you create under this profile will bear this identifier prefix. "
    	  */
    	  + "<br>" + Language.getInstance().getMessage("DataPackageIdentification.helpText_2")
    	  /*
          + " For example, using the prefix "
          + "\'jane_doe\', data packages will have names like jane_doe.1.1, "
          + "jane_doe.2.1, etc." 
          */
    	  + " " + Language.getInstance().getMessage("DataPackageIdentification.helpText_3")
          + "</p></html>";
      helpLabel.setText(helpText);
      screenPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(8,8,8,8),
                            /*"Data Package Identification"*/ Language.getInstance().getMessage("DataPackageIdentification")));
      JLabel scopeLabel = new JLabel(/*"Identifier prefix: "*/ Language.getInstance().getMessage("IdentifierPrefix") + ": ");
      JLabel noteLabel = new JLabel(/*"Note: "*/ Language.getInstance().getMessage("Note") + ": ");
      JLabel illegalCharactersLabel = new JLabel("Illegal characters: ");
      noteLabel.setForeground(Color.black);
      noteLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      illegalCharactersLabel.setForeground(Color.black);
      illegalCharactersLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      JLabel space1 = new JLabel(" ");
      scopeLabel.setForeground(Color.black);
      scopeLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
      scopeField.setColumns(15);
      JLabel noteContent= new JLabel(
    		  							/*"Please do not use \"temporary\" because it is a reserved word."*/
    		  							Language.getInstance().getMessage("DataPackageIdentification.Note")
    		  						);
      JLabel illegalContent = new JLabel(getIllegalScopeCharacterNote());
      JLabel space2 = new JLabel(" ");
      JLabel[] labels = {scopeLabel, space1, noteLabel, illegalCharactersLabel};
      JComponent[] textFields = {scopeField, space2, noteContent, illegalContent};
      addLabelTextRows(labels, textFields, gridbag, screenPanel);
      addKeyListenerToComponents(textFields);
      scopeField.requestFocus();
    }

    // Set button states properly
    if (0 == currentScreen) {
      previousButton.setEnabled(false);
    } else {
      previousButton.setEnabled(true);
    }

    if (currentScreen == numScreens-1) {
      nextButton.setEnabled(true);
      nextButton.setText(/*"Finished"*/ Language.getInstance().getMessage("Finish"));
      nextButton.setIcon(null);
    } else {
      nextButton.setEnabled(true);
      nextButton.setText(/*"Next"*/ Language.getInstance().getMessage("Next"));
      nextButton.setIcon(forwardIcon);
    }
    nextButton.isDefaultButton();

    // Repaint the numScreens
    screenPanel.validate();
    screenPanel.paint(screenPanel.getGraphics());
  }

  private String getIllegalScopeCharacterNote() {
	  StringBuffer sb = new StringBuffer();
	  for (String ch: ILLEGAL_SCOPE_CHARACTERS) {
		  sb.append("'");
		  sb.append(ch);
		  sb.append("', ");
	  }
	  sb.delete(sb.lastIndexOf(","), sb.length());
	  return sb.toString();
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
    if (profileNameField.getText() == null ||
        profileNameField.getText().equals("")) {
      fieldsAreValid = false;
    }

    if (scopeField.getText() == null || scopeField.getText().length() == 0) {
      fieldsAreValid = false;
    } else {
	    for (String ch: ILLEGAL_SCOPE_CHARACTERS) {
	    	if (scopeField.getText().contains(ch)) {
	    		fieldsAreValid = false;
	    		break;
	    	}
	    }
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
      String profileDirName = ConfigXML.getConfigDirectory() + File.separator +
                                            config.get("profile_directory", 0);
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
      String profileName = profileNameField.getText();
      String scope = scopeField.getText();
      String profilePath = profileDirName + File.separator + profileName;
      String profileFileName = profilePath + File.separator +
                        profileName + ".xml";
      File profileDir = new File(profilePath);
      if (!profileDir.mkdir()) {
        // Error creating the directory
        currentScreen = 0;
        layoutScreen();
        String messageText = "A profile named \"" + profileName +
                             "\" already exists.  Would you like to use it?" +
                             "\n\nUse existing profile?\n";
        int result = JOptionPane.showConfirmDialog(this, messageText,
                                                   "Use existing profile?",
                                                   JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
          try {
            ConfigXML profile = new ConfigXML(profileFileName);
            // Log into metacat
            framework.setProfile(profile);

            // Get rid of the dialog
            setVisible(false);
            dispose();
          } catch (FileNotFoundException fnf) {
            messageText = "Sorry, I tried, but it looks like that profile\n" +
                          "is corrupted.  You'll have to choose another " +
                          "profile name.";
            JOptionPane.showMessageDialog(this, messageText);
          }
        } else {
          messageText = "OK, then please choose another profile name.\n";
          JOptionPane.showMessageDialog(this, messageText);
        }
      } else {
        try {
          // Copy default profile to the new directory
          String defaultProfile = config.get("default_profile", 0);
          //FileUtils.copy(defaultProfile, profileFileName);
          ClassLoader cl = this.getClass().getClassLoader();
          InputStreamReader in = new InputStreamReader(
                                      cl.getResourceAsStream(defaultProfile));
          Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(profileFileName)), Charset.forName("UTF-8")));
          int len;
          char[] buffer =  new char[512];
          while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
          }
          in.close();
          out.close();

          // Store the collected information in the profile
          ConfigXML profile = new ConfigXML(profileFileName);
          boolean success = false;
          if (! profile.set(PROFILENAMEELEMENTNAME, 0, profileName)) {
            success = profile.insert(PROFILENAMEELEMENTNAME, profileName);
          }
          if (! profile.set("firstname", 0, firstNameField.getText())) {
            success = profile.insert("firstname", firstNameField.getText());
          }
          if (! profile.set("lastname", 0, lastNameField.getText())) {
            success = profile.insert("lastname", lastNameField.getText());
          }
          if (! profile.set("scope", 0, scope)) {
            success = profile.insert("scope", scope);
          }
          if (! profile.set(CORRECTIONEMLPROFILEPATH, 0, "true")) {
              success = profile.insert(CORRECTIONEMLPROFILEPATH, "true");
          }
          if (! profile.set(IDFILEMAPUPDATEDPATH, 0, "true")) {
              success = profile.insert(IDFILEMAPUPDATEDPATH, "true");
          }
          profile.save();

          // Create our directories for user data
          String dataDirName = profile.get(DATADIRELEMENTNAME, 0);
          String dataPath = profilePath + File.separator + dataDirName;
          File dataDir = new File(dataPath);
          success = dataDir.mkdir();
          File dataSysMetaDir = new File(dataDir, File.separator+SYSTEMMETADATADIRNAME);
          success = dataSysMetaDir.mkdir();

          String cacheDirName = profile.get(CACHEDIRELEMENTNAME, 0);
          String cachePath = profilePath + File.separator + cacheDirName;
          File cacheDir = new File(cachePath);
          success = cacheDir.mkdir();
          File cacheSysMetaDir = new File(cacheDir, File.separator+SYSTEMMETADATADIRNAME);
          success = cacheSysMetaDir.mkdir();

          String tempDirName = profile.get(TEMPDIRELEMENTNAME, 0);
          String tempPath = profilePath + File.separator + tempDirName;
          File tempDir = new File(tempPath);
          success = tempDir.mkdir();
          File tempSysMetaDir = new File(tempDir, File.separator+SYSTEMMETADATADIRNAME);
          success = tempSysMetaDir.mkdir();
          
          String incompleteDirName = profile.get(INCOMPLETEDIRELEMENTNAME, 0);
          String incompletePath = profilePath + File.separator + incompleteDirName;
          File incompleteDir = new File(incompletePath);
          success = incompleteDir.mkdir();
          File incompleteSysMetaDir = new File(incompleteDir, File.separator+SYSTEMMETADATADIRNAME);
          success = incompleteSysMetaDir.mkdir();

          // set the profile to this new one
          framework.setProfile(profile);
          
          // Copy sample data to the data directory for this new (now current!) profile
          // TODO: retrieve from -config.jar (MBJ)
          String samplePath = config.get("samples_directory", 0);
          File sampleDir = new File(samplePath);
          File[] samplesList = listFiles(sampleDir);
          for (int n=0; n < samplesList.length; n++) {
            File srcFile = samplesList[n];
            if (srcFile.isFile()) {
            	// use the file name as the identifier - it's the best we have in this case
            	String identifier = srcFile.getName();
            	InputStream is = new FileInputStream(srcFile);
				Morpho.thisStaticInstance.getLocalDataStoreService().newFile(identifier, is );
				Log.debug(20, identifier);
            }
          }
          
          //stop listening
          StateChangeMonitor.getInstance().removeStateChangeListener(StateChangeEvent.ACCESS_LIST_MODIFIED, this);

          // Get rid of the dialog
          setVisible(false);
          dispose();

        } catch (IOException ioe) {
          currentScreen = 0;
          layoutScreen();
          String messageText = "Error creating profile named \"" +
                               profileName + "\".  Please try again.\n";
          JOptionPane.showMessageDialog(this, messageText);
        }
      }
    } else {
      currentScreen = 0;
      layoutScreen();
      String messageText = "Some required information was invalid.\n\n" +
                           "Please check that you have provided a\n" +
                           "profile name, a user name, an organization,\n" +
                           "and an identifer prefix (the following characters are not permitted: "
                           + getIllegalScopeCharacterNote() 
                           + ").\n";
      JOptionPane.showMessageDialog(this, messageText);
    }
  }

  private File[] listFiles(File dir) {
    String[] fileStrings = dir.list();
    int len = fileStrings.length;
    File[] list = new File[len];
    for (int i=0; i<len; i++) {
        list[i] = new File(dir, fileStrings[i]);
    }
    return list;
  }

  public void handleStateChange(StateChangeEvent event) {
	if (event.getChangedState().equals(StateChangeEvent.ACCESS_LIST_MODIFIED)) {
		layoutScreen();
	}
	
}



}
