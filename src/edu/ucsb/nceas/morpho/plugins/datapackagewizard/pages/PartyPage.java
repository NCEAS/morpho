/**
 *  '$RCSfile: PartyPage.java,v $'
 *    Purpose: A class for Party Intro Screen
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-11-18 01:42:20 $'
 * '$Revision: 1.49 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.ReferenceSelectionEvent;
import edu.ucsb.nceas.morpho.datapackage.ReferencesHandler;
import edu.ucsb.nceas.morpho.datapackage.ReferencesListener;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.dom.Node;


public class PartyPage extends AbstractUIPage {

  // define standard variables describing the page

  private final String EMPTY_STRING = "";

  private String pageID = DataPackageWizardInterface.PARTY_CREATOR;

  private final String nextPageID = EMPTY_STRING;

  private final String title = "Party Page";

  private final String subtitle = EMPTY_STRING;

  private final String pageNumber = EMPTY_STRING;

  private static final Dimension PARTY_2COL_LABEL_DIMS = new Dimension(70, 20);

  private static final Dimension PARTY_HALF_LABEL_DIMS = new Dimension(350, 20);

  private static final Dimension PARTY_FULL_LABEL_DIMS = new Dimension(700, 20);

  private String backupXPath;

  private String roleString = EMPTY_STRING;

  private final String[] ROLE_ARRAY
      = new String[] {
        EMPTY_STRING,
        "Originator",
        "Content Provider",
        "Principal Investigator",
        "Editor",
        "Publisher",
        "Processor",
        "Custodian/Steward",
        "Author",
        "Metadata Provider",
        "Distributor",
        "User"};

  // define Swing components used
  private JLabel roleLabel;

  private JComboBox rolePickList;

  private JTextField salutationField;

  private JTextField firstNameField;

  private JLabel lastNameLabel;

  private JTextField lastNameField;

  private JLabel organizationLabel;

  private JTextField organizationField;

  private JLabel positionNameLabel;

  private JPanel warningPanel;

  private JLabel warningLabel;

  private JTextField positionNameField;

  private JTextField address1Field;

  private JTextField address2Field;

  private JTextField cityField;

  private JTextField stateField;

  private JTextField zipField;

  private JTextField countryField;

  private JTextField phoneField;

  private JTextField faxField;

  private JTextField emailField;

  private JTextField urlField;

  private JPanel rolePanel;

  private JPanel middlePanel;

  private JPanel checkBoxPanel;

  private JComboBox refsDropdown;

  public JLabel pageDescriptionLabel;

  public JPanel listPanel;

  private ReferencesHandler referencesHandler;

  private final String PARTY_GENERIC_NAME = "parties";

  private final String[] REFSHANDLER_SURROGATE_STRING
      = new String[] {
        "/individualName/givenName",
        " ",
        "/individualName/surName",
        " ",
        "/organizationName",
        " ",
        "/positionName",
  };

  private boolean backupExists = false;

  protected boolean editingOriginalRef = false;

  private String referenceIdString;

  private String referencesNodeIDString;

  private boolean editingAllowed = true;
  private boolean rolePicklistShouldBeHidden = false;
  //
  private final String[] checkBoxArray
      = new String[] { "Do you want to edit the above information?" };


  /**
   *  Constructor - determines what type of dialog (what role):
   *
   * @param role short - CREATOR, CONTACT, ASSOCIATED, PERSONNEL, CITATION_AUTHOR or UNDEFINED
   */
  public PartyPage(String role) {

    pageID = role;
    referencesHandler = new ReferencesHandler(PARTY_GENERIC_NAME,
                                              REFSHANDLER_SURROGATE_STRING);
    initRolePanel();
    setRole(role);
    init();
  }


  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    pageDescriptionLabel
        = WidgetFactory.makeHTMLLabel("<font size=\"4\"><b>&nbsp;&nbsp;"
                                       + roleString + " Details</b></font>", 1);
    middlePanel = new JPanel();
    this.setLayout(new BorderLayout());
    this.add(middlePanel, BorderLayout.CENTER);

    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(pageDescriptionLabel);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    final PartyPage instance = this;

    // create itemlistener for references list

    ReferencesListener refsDropdownListener = new ReferencesListener() {

      public void referenceSelected(ReferenceSelectionEvent event) {

        String eventRefID = event.getReferenceID();

        if (eventRefID == null) {
          // If refID is null, then the user has selected blank entry (ie does not
          // want to choose a referenced value. Therefore, set party instance
          // to editable, clear out all values and remove the checkBoxPanel
          referencesNodeIDString = null;

          instance.setEditable(restoreFromPreviousValues());

          checkBoxPanel.setVisible(false);
          Log.debug(45, "ReferencesListener got NULL eventRefID");

        } else {
          // If refID is not null, a previous entry has been chosen
          Log.debug(45, "ReferencesListener got eventRefID=" + eventRefID);

          // Unhilite all the components... this is because now a valid
          // previous entered party has been selected
          WidgetFactory.unhiliteComponent(lastNameLabel);
          WidgetFactory.unhiliteComponent(organizationLabel);
          WidgetFactory.unhiliteComponent(positionNameLabel);
          warningPanel.setVisible(false);
          WidgetFactory.unhiliteComponent(warningLabel);

          String rxp = "/" + event.getSubtreeRootNodeName().trim() + "/";

          short location = event.getLocation();

          switch (location) {

            case ReferenceSelectionEvent.CURRENT_DATA_PACKAGE:

              // referredPage  was created in same DP - so current page would be
              // a reference... get reference Id, set instance non-editable,
              // set value of all fields and radio panel visible
              Log.debug(45,
                        "ReferencesListener - location=CURRENT_DATA_PACKAGE");

              referencesNodeIDString = eventRefID;

              editingAllowed
                  = instance.setPageData(event.getXPathValsMap(), rxp);
              instance.setEditable(false);

              checkBoxPanel.setVisible(true);
              Log.debug(45,
                        " ...setting page data to: " + event.getXPathValsMap());

              break;

            case ReferenceSelectionEvent.DIFFERENT_DATA_PACKAGE:

              // referredPage was not created in same DP - so current page would
              // not be a reference... set reference Id null, set referDiffDP
              // true, set instance editable as it is not a reference,
              // set value of all fields and radio panel invisible
              Log.debug(45,
                        "ReferencesListener - location=DIFFERENT_DATA_PACKAGE");

              referencesNodeIDString = null;

              editingAllowed = true;
              instance.setEditable(true);
              instance.setPageData(event.getXPathValsMap(), rxp);

              checkBoxPanel.setVisible(false);
              Log.debug(45,
                        " ...setting page data to: " + event.getXPathValsMap());

              break;

            default:
              Log.debug(45, "ReferencesListener - location NOT RECOGNIZED!! - "
                        + location);

          }
        }
      }
    };

    // listPanel - panel for showing the drop down list combo
    listPanel = WidgetFactory.makePanel(1);
    JLabel listLabel = WidgetFactory.makeLabel(
        "You can pick from one of the earlier "
        + "entries that you have made.", false);
    setPrefMinMaxSizes(listLabel, PARTY_HALF_LABEL_DIMS);
    listPanel.add(listLabel);
    listPanel.setBorder(
        new javax.swing.border.EmptyBorder(0, 12 * WizardSettings.PADDING,
                                           0, 8 * WizardSettings.PADDING));

    Container topLevelContainer = this;

    while (topLevelContainer.getParent() != null) {
      topLevelContainer = topLevelContainer.getParent();
    }
    if (!(topLevelContainer instanceof Frame))topLevelContainer = null;

    refsDropdown = referencesHandler.getJComboBox(
        UIController.getInstance().getCurrentAbstractDataPackage(),
        refsDropdownListener, (Frame)topLevelContainer);

    listPanel.add(refsDropdown);
    middlePanel.add(listPanel);

    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    // Role List
    rolePanel.setBorder(new javax.swing.border.EmptyBorder(
      0, 12 * WizardSettings.PADDING, 0, 8 * WizardSettings.PADDING));
    middlePanel.add(rolePanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    // Salutation
    JPanel salutationPanel = WidgetFactory.makePanel(1);
    salutationPanel.add(WidgetFactory.makeLabel("Salutation:", false));
    salutationField = WidgetFactory.makeOneLineTextField();
    salutationPanel.add(salutationField);
    salutationPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(salutationPanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    // First Name
    JPanel firstNamePanel = WidgetFactory.makePanel(1);
    firstNamePanel.add(WidgetFactory.makeLabel("First Name:", false));
    firstNameField = WidgetFactory.makeOneLineTextField();
    firstNamePanel.add(firstNameField);
    firstNamePanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(firstNamePanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    // Required Notification panel
    JPanel reqPanel = new JPanel();
    reqPanel.setLayout(new BoxLayout(reqPanel, BoxLayout.X_AXIS));
    JPanel reqInfoPanel = new JPanel();
    reqInfoPanel.setLayout(new BoxLayout(reqInfoPanel, BoxLayout.Y_AXIS));

    JPanel reqWarningPanel = new JPanel();
    reqWarningPanel.setLayout(new BoxLayout(reqWarningPanel, BoxLayout.Y_AXIS));
    reqWarningPanel.add(WidgetFactory.makeHalfSpacer());

    JLabel warn1Label = new JLabel(" One of");
    warn1Label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    warn1Label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    reqWarningPanel.add(warn1Label);

    JLabel warn2Label = new JLabel("    the");
    warn2Label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    warn2Label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    reqWarningPanel.add(warn2Label);

    JLabel warn3Label = new JLabel("  three");
    warn3Label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    warn3Label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    reqWarningPanel.add(warn3Label);

    JLabel warn4Label = new JLabel("required");
    warn4Label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    warn4Label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    reqWarningPanel.add(warn4Label);

    reqWarningPanel.add(WidgetFactory.makeDefaultSpacer());
    reqPanel.add(reqWarningPanel);

    JLabel bracketLabel = new JLabel("{");
    bracketLabel.setFont(new Font("Sans-Serif", Font.PLAIN, 40));
    bracketLabel.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    bracketLabel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
        3 * WizardSettings.PADDING, 0));
    reqPanel.add(bracketLabel);

    // Last Name
    JPanel lastNamePanel = WidgetFactory.makePanel(1);
    lastNameLabel = WidgetFactory.makeLabel("Last Name:", true);
    lastNamePanel.add(lastNameLabel);
    lastNameField = WidgetFactory.makeOneLineTextField();
    lastNamePanel.add(lastNameField);
    lastNamePanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
        0, 8 * WizardSettings.PADDING));
    reqInfoPanel.add(lastNamePanel);
    reqInfoPanel.add(WidgetFactory.makeHalfSpacer());

    // Organization
    JPanel organizationPanel = WidgetFactory.makePanel(1);
    organizationLabel = WidgetFactory.makeLabel("Organization:", true);
    organizationPanel.add(organizationLabel);
    organizationField = WidgetFactory.makeOneLineTextField();
    organizationPanel.add(organizationField);
    organizationPanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
        0, 8 * WizardSettings.PADDING));
    reqInfoPanel.add(organizationPanel);
    reqInfoPanel.add(WidgetFactory.makeHalfSpacer());

    // Position Name
    JPanel positionNamePanel = WidgetFactory.makePanel(1);
    positionNameLabel = WidgetFactory.makeLabel("Position Name:", true);
    positionNamePanel.add(positionNameLabel);
    positionNameField = WidgetFactory.makeOneLineTextField();
    positionNamePanel.add(positionNameField);
    positionNamePanel.setBorder(new javax.swing.border.EmptyBorder(0, 0,
        0, 8 * WizardSettings.PADDING));
    reqInfoPanel.add(positionNamePanel);
    reqInfoPanel.add(WidgetFactory.makeHalfSpacer());

    reqPanel.add(reqInfoPanel, BorderLayout.CENTER);
    middlePanel.add(reqPanel);

    // Address 1
    JPanel address1Panel = WidgetFactory.makePanel(1);
    address1Panel.add(WidgetFactory.makeLabel("Address 1:", false));
    address1Field = WidgetFactory.makeOneLineTextField();
    address1Panel.add(address1Field);
    address1Panel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(address1Panel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    // Address 2
    JPanel address2Panel = WidgetFactory.makePanel(1);
    address2Panel.add(WidgetFactory.makeLabel("Address 2:", false));
    address2Field = WidgetFactory.makeOneLineTextField();
    address2Panel.add(address2Field);
    address2Panel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(address2Panel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    // City & State
    JPanel cityStatePanel = WidgetFactory.makePanel(1);
    cityStatePanel.add(WidgetFactory.makeLabel("City:", false));
    cityField = WidgetFactory.makeOneLineTextField();
    cityStatePanel.add(cityField);
    cityStatePanel.add(WidgetFactory.makeDefaultSpacer());
    JLabel stateLabel = WidgetFactory.makeLabel("State:", false);
    setPrefMinMaxSizes(stateLabel, PARTY_2COL_LABEL_DIMS);
    cityStatePanel.add(stateLabel);
    stateField = WidgetFactory.makeOneLineTextField();
    cityStatePanel.add(stateField);
    cityStatePanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(cityStatePanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    // Zip & Country
    JPanel zipCountryPanel = WidgetFactory.makePanel(1);
    zipCountryPanel.add(WidgetFactory.makeLabel("Postal Code:", false));
    zipField = WidgetFactory.makeOneLineTextField();
    zipCountryPanel.add(zipField);
    zipCountryPanel.add(WidgetFactory.makeDefaultSpacer());
    JLabel countryLabel = WidgetFactory.makeLabel("Country:", false);
    setPrefMinMaxSizes(countryLabel, PARTY_2COL_LABEL_DIMS);
    zipCountryPanel.add(countryLabel);
    countryField = WidgetFactory.makeOneLineTextField();
    zipCountryPanel.add(countryField);
    zipCountryPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(zipCountryPanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    // Phone & Fax
    JPanel phoneFaxPanel = WidgetFactory.makePanel(1);
    phoneFaxPanel.add(WidgetFactory.makeLabel("Phone:", false));
    phoneField = WidgetFactory.makeOneLineTextField();
    phoneFaxPanel.add(phoneField);
    phoneFaxPanel.add(WidgetFactory.makeDefaultSpacer());
    JLabel faxLabel = WidgetFactory.makeLabel("Fax:", false);
    setPrefMinMaxSizes(faxLabel, PARTY_2COL_LABEL_DIMS);
    phoneFaxPanel.add(faxLabel);
    faxField = WidgetFactory.makeOneLineTextField();
    phoneFaxPanel.add(faxField);
    phoneFaxPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(phoneFaxPanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    // Email and URL
    JPanel emailUrlPanel = WidgetFactory.makePanel(1);
    emailUrlPanel.add(WidgetFactory.makeLabel("Email:", false));
    emailField = WidgetFactory.makeOneLineTextField();
    emailUrlPanel.add(emailField);
    emailUrlPanel.add(WidgetFactory.makeDefaultSpacer());
    JLabel urlLabel = WidgetFactory.makeLabel("Online URL:", false);
    setPrefMinMaxSizes(urlLabel, PARTY_2COL_LABEL_DIMS);
    emailUrlPanel.add(urlLabel);
    urlField = WidgetFactory.makeOneLineTextField();
    emailUrlPanel.add(urlField);
    emailUrlPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(emailUrlPanel);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    // Warning
    warningPanel = WidgetFactory.makePanel(1);
    warningLabel = WidgetFactory.makeLabel(
        "Warning: at least one of the three "
        + "entries is required: Last Name, Position Name or Organization", true);
    warningPanel.add(warningLabel);
    warningPanel.setVisible(false);
    setPrefMinMaxSizes(warningLabel, PARTY_FULL_LABEL_DIMS);
    warningPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(warningPanel);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    // Itemlistener for the check box
    ItemListener editCheckBoxListener = new ItemListener() {

      public void itemStateChanged(ItemEvent e) {

        Log.debug(45, "got check box command: " + e.getStateChange());

        if (e.getStateChange() == ItemEvent.DESELECTED) {

          // If the checkbox is not selected - set dialog non-editable
          instance.setEditable(false);

        } else if (e.getStateChange() == ItemEvent.SELECTED) {

          // If the checkbox is selected - need to ask the user if he wants to
          // edit the previous entry or make a copy of the previous entry
          // and edit that one
          Object[] optionArray
              = new String[] {
                "Edit original",
                "Copy original and edit",
                "Cancel"};
          JOptionPane optPane = new JOptionPane();
          optPane.setOptions(optionArray);
          optPane.setMessage(
              "Do you want to :\nEdit the original entry (and therefore change "
              +"all entries that refer to the original), or \n"
              +"Create a copy of the original and edit that?");
          optPane.createDialog(instance, "Select an option...").show();
          Object selectedValue = optPane.getValue();

          if (selectedValue == optionArray[0]) {

            // edit the previous reference
            instance.setEditable(true);
            editingOriginalRef = true;

          } else if (selectedValue == optionArray[1]) {

            // create a new copy - do not edit reference, is a reference
            // and remove reference id string
            instance.setEditable(true);
            referencesNodeIDString = null;
            // (Dan Higgins - 4/9/04) not sure the previous line is needed,
            // but the following one is to avoid reuse of the id in the copied party
            referenceIdString = null;
            editingOriginalRef = false;

          } else {

            // Cancel - remove selection from source - instance is not editable,
            // and do edit reference
            JCheckBox source = (JCheckBox)e.getSource();
            source.setSelected(false);
            instance.setEditable(false);
            editingOriginalRef = false;
          }
        }
        instance.validate();
        instance.repaint();
      }
    };

    // Check box for editing the instance - is not visible initially
    checkBoxPanel = WidgetFactory.makeCheckBoxPanel(checkBoxArray, -1,
                                                    editCheckBoxListener);
    checkBoxPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(checkBoxPanel);
    checkBoxPanel.setVisible(false);
  }


  private void initRolePanel() {

    rolePanel = WidgetFactory.makePanel(1);
    roleLabel = WidgetFactory.makeLabel("Role:", true);
  }



  /**
   * sets the role and roleString for this wizard page
   *
   * @param role short - CREATOR, CONTACT, ASSOCIATED, PERSONNEL, CITATION_AUTHOR or UNDEFINED
   */
  private void setRole(String role) {

    rolePickList = null;
    rolePanel.removeAll();

    if (role.equals(DataPackageWizardInterface.PARTY_CREATOR)) {

      roleString = "Owner";
      backupXPath = "/creator";
      rolePicklistShouldBeHidden = true;

    } else if (role.equals(DataPackageWizardInterface.PARTY_CONTACT)) {

      roleString = "Contact";
      backupXPath = "/contact";
      rolePicklistShouldBeHidden = true;

    } else if (role.equals(DataPackageWizardInterface.PARTY_CITATION_AUTHOR)) {

      roleString = "Author";
      backupXPath = "/creator";
      rolePicklistShouldBeHidden = true;

    } else if (role.equals(DataPackageWizardInterface.PARTY_ASSOCIATED)) {

      roleString = "Associated Party";
      backupXPath = "/associatedParty";

    } else if (role.equals(DataPackageWizardInterface.PARTY_PERSONNEL)) {

      roleString = "Personnel";
      backupXPath = "/personnel";
    }
    //set display name for external refs dialog...
    referencesHandler.setDisplayName(roleString);

    if (rolePicklistShouldBeHidden) return;

    ////////////////////////////////////////////////////////////////////
    // gets here only if role is *not* PARTY_CREATOR, CITATION_AUTHOR
    // or PARTY_CONTACT...
    ////////////////////////////////////////////////////////////////////
    rolePanel.add(roleLabel);
    rolePickList = WidgetFactory.makePickList(ROLE_ARRAY, true, 0,
             new ItemListener() {public void itemStateChanged(ItemEvent e) {}});
    rolePanel.add(rolePickList);
  }


  /**
   * The action sets all the fields in 'this' page editable or non-editable
   * based on boolean arguement passed
   *
   * @param editable boolean
   */
  public void setEditable(boolean editable) {

    salutationField.setEditable(editable);
    firstNameField.setEditable(editable);
    lastNameField.setEditable(editable);
    organizationField.setEditable(editable);
    positionNameField.setEditable(editable);
    address1Field.setEditable(editable);
    address2Field.setEditable(editable);
    cityField.setEditable(editable);
    stateField.setEditable(editable);
    zipField.setEditable(editable);
    countryField.setEditable(editable);
    phoneField.setEditable(editable);
    faxField.setEditable(editable);
    emailField.setEditable(editable);
    urlField.setEditable(editable);

    checkBoxPanel.setVisible(!editable && editingAllowed);
  }


  /**
   * The action sets preferred Min and Max Sizes for the Components
   *
   * @param component JComponent
   * @param dims Dimension
   */
  private void setPrefMinMaxSizes(JComponent component, Dimension dims) {
    WidgetFactory.setPrefMaxSizes(component, dims);
    component.setMinimumSize(dims);
  }


  private String getCurrentPickListRole() {

    if (rolePickList==null) return EMPTY_STRING;
    String pickListRole = (String)rolePickList.getSelectedItem();
    return (pickListRole != null) ? pickListRole.trim() : EMPTY_STRING;
  }


  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    boolean lastNameOK = false;
    boolean organizationOK = false;
    boolean positionOK = false;

    //if we have a role field, it must have a value...
    if (rolePickList!=null) {

      if (notNullAndNotEmpty(getCurrentPickListRole())) {

        WidgetFactory.unhiliteComponent(roleLabel);

      } else {

        WidgetFactory.hiliteComponent(roleLabel);
        rolePickList.requestFocus();
        return false;
      }
    }

    // is last name entered?
    String lastName = lastNameField.getText().trim();
    if (notNullAndNotEmpty(lastName)) {
      lastNameOK = true;
    }

    // if we have a salutation AND/OR a givenName, we *must* have a surName...
    if (notNullAndNotEmpty(salutationField.getText().trim())
        || notNullAndNotEmpty(firstNameField.getText().trim())) {

      if (!lastNameOK) {
        WidgetFactory.hiliteComponent(lastNameLabel);
        lastNameLabel.requestFocus();
        warningLabel.setText("Warning: If you provide a salutation and/or "
                             + "first name, a last name must also be provided");

        warningPanel.setVisible(true);
        WidgetFactory.hiliteComponent(warningLabel);
        return false;
      }
    }

    // is Organization Name entered?
    String organization = organizationField.getText().trim();
    if (notNullAndNotEmpty(organization)) {
      organizationOK = true;
    }

    // is Position entered?
    String positionName = positionNameField.getText().trim();
    if (notNullAndNotEmpty(positionName)) {
      positionOK = true;
    }

    // if neither of the three are entered, send error - otherwise
    // unhilite components and proceed.
    if (lastNameOK || organizationOK || positionOK) {

      WidgetFactory.unhiliteComponent(lastNameLabel);
      WidgetFactory.unhiliteComponent(organizationLabel);
      WidgetFactory.unhiliteComponent(positionNameLabel);
      warningPanel.setVisible(false);
      WidgetFactory.unhiliteComponent(warningLabel);

    } else {

      WidgetFactory.hiliteComponent(lastNameLabel);
      WidgetFactory.hiliteComponent(organizationLabel);
      WidgetFactory.hiliteComponent(positionNameLabel);
      warningLabel.setText("Warning: at least one of the three entries is "
                           + "required: Last Name, Position Name or Organization");
      warningPanel.setVisible(true);
      WidgetFactory.hiliteComponent(warningLabel);
      return false;
    }

    String urlstring = urlField.getText();
    if (notNullAndNotEmpty(urlstring.trim())) {
      if (!isURL(urlstring)) {
        Log.debug(1, "The Online URL '"+urlstring+"' is not valid!");
        return false;
      }
    }


    if (this.isReference()) {

      if (editingOriginalRef) {

        //save referencesNodeIDString and reset it to null, so
				//getPageData() assumes we're *not* dealing with a reference...
				String backupReferencesNodeIDString = referencesNodeIDString;
				referencesNodeIDString = null;
				
				// need to ignore the "role" while updating the original reference. Hence, we 
				// pass an ignorelist containing the xpath of the "role" node.
				List ignoreList = new ArrayList();
				ignoreList.add("/thisXPathRootWillGetReplaced/role");
				
				//values in dialog need to be written to original (referenced) party,
				referencesHandler.updateOriginalReferenceSubtree(
            UIController.getInstance().getCurrentAbstractDataPackage(),
            backupReferencesNodeIDString,
            this.getPageData("/thisXPathRootWillGetReplaced"), ignoreList);

        //now make a backup of role string
        String backupRole = getCurrentPickListRole();

        //and this party needs to be set as a "/references"
        clearAllFields();

        //now reinstate referencesNodeIDString, ready for call to getPageData
        referencesNodeIDString = backupReferencesNodeIDString;

        //and reinstate role string
        addAndSetRole(backupRole);

        Log.debug(45, "PartyPage - editingOriginalRef... referencesNodeIDString="
                  +referencesNodeIDString);
      }

    } else {

      getRefID();
    }

    rememberPreviousValues();


    Log.debug(45, "\nfinished onAdvanceAction for page ID: "+this.getPageID());

    return true;
  }

  private boolean isReference() { return referencesNodeIDString!=null; }

  /**
     /**
    *  The action to be executed when the "Prev" button is pressed. May be empty
    *  Here, it does nothing because this is just a Panel and not the outer container
    */
   public void onRewindAction() {}


  /**
   *  The action to be executed when the page is loaded
   *  Here, it does nothing because this is just a Panel and not the outer container
   */
  public void onLoadAction() {

    referencesHandler.updateJComboBox(
        UIController.getInstance().getCurrentAbstractDataPackage(),
        refsDropdown, getRefID());

    //backupExists will be false only the first
    //ever time onLoadAction() is called...
    if (!backupExists) {
      rememberPreviousValues();
    } else {
      editingAllowed = restoreFromPreviousValues();
    }

    this.setEditable(!this.isReference());
    editingOriginalRef = false;
    Log.debug(45, "PartyPage.onLoadAction() - isReference() = "+isReference());
  }


  /**
   *  gets the referenceID for this wizard page
   *
   *  @return String refID
   */
  protected String getRefID() {

    if (notNullAndNotEmpty(referenceIdString))return referenceIdString;

    AbstractDataPackage abs
        = UIController.getInstance().getCurrentAbstractDataPackage();

    if (abs == null) {
      Log.debug(45, "*** ERROR - PartyPage.getRefID() can't get AbsDataPkg");
      return EMPTY_STRING;
    }
    referenceIdString = abs.getNewUniqueReferenceID();

    return referenceIdString;
  }



  /**
   *  gets the referencesNodeIDString - ie if this party is a reference to
   * another party, this method returns the reference id value it is using to
   * "point to" that other party
   *
   *  @return String referencesNodeIDString
   */
  protected String getReferencesNodeIDString() {

    if (notNullAndNotEmpty(referencesNodeIDString)) return referencesNodeIDString;
    return EMPTY_STRING;
  }


  /**
   *  gets the Page ID for this wizard page
   *
   *  @return   the Page ID String for this wizard page
   */
  public String getPageID() {
    return this.pageID;
  }


  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() {
    return title;
  }


  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() {
    return subtitle;
  }


  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() {
    return this.nextPageID;
  }


  /**
   *  Returns the serial number of the page
   *
   *  @return the serial number of the page
   */
  public String getPageNumber() {
    return pageNumber;
  }


  /**
   *  @return a List contaiing 3 String elements - one for each column of the
   *  3-col list in which this surrogate is displayed
   *
   */
  public List getSurrogate() {

    List surrogate = new ArrayList();

    //party (first column) surrogate:
    StringBuffer partyBuff = new StringBuffer();

    String salutation = salutationField.getText().trim();
    if (notNullAndNotEmpty(salutation)) {
      partyBuff.append(salutation);
      partyBuff.append(" ");
    }

    String firstName = firstNameField.getText().trim();
    if (notNullAndNotEmpty(firstName)) {
      partyBuff.append(firstName);
      partyBuff.append(" ");
    }

    String lastName = lastNameField.getText().trim();
    if (notNullAndNotEmpty(lastName)) {
      partyBuff.append(lastName);
    }

    String positionName = positionNameField.getText().trim();
    if (notNullAndNotEmpty(positionName)) {
      appendCommaIfNeeded(partyBuff);
      partyBuff.append(positionName);
    }

    String organization = organizationField.getText().trim();
    if (notNullAndNotEmpty(organization)) {
      appendCommaIfNeeded(partyBuff);
      partyBuff.append(organization);
    }
    surrogate.add(partyBuff.toString());

    //role (second column) surrogate:
    if (rolePickList!=null) {

      StringBuffer roleBuff = new StringBuffer(roleString);
      if (notNullAndNotEmpty(getCurrentPickListRole())) {
        roleBuff.append(" (");
        roleBuff.append(getCurrentPickListRole());
        roleBuff.append(")");
      }
      surrogate.add(roleBuff.toString());
    } else {
      surrogate.add(roleString);
    }

    //address (third column) surrogate:
    StringBuffer addressBuff = new StringBuffer();

    String address1 = address1Field.getText().trim();
    if (notNullAndNotEmpty(address1)) {
      addressBuff.append(address1);
    }

    String address2 = address2Field.getText().trim();
    if (notNullAndNotEmpty(address2)) {
      appendCommaIfNeeded(addressBuff);
      addressBuff.append(address2);
    }

    String city = cityField.getText().trim();
    if (notNullAndNotEmpty(city)) {
      appendCommaIfNeeded(addressBuff);
      addressBuff.append(city);
    }

    String state = stateField.getText().trim();
    if (notNullAndNotEmpty(state)) {
      appendCommaIfNeeded(addressBuff);
      addressBuff.append(state);
    }

    String zip = zipField.getText().trim();
    if (notNullAndNotEmpty(zip)) {
      appendCommaIfNeeded(addressBuff);
      addressBuff.append(zip);
    }

    String country = countryField.getText().trim();
    if (notNullAndNotEmpty(country)) {
      appendCommaIfNeeded(addressBuff);
      addressBuff.append(country);
    }

    surrogate.add(addressBuff.toString());

    return surrogate;
  }


  // If stringbuffer does NOT end with ", ", this method will add ", "
  private void appendCommaIfNeeded(StringBuffer buff) {

    int lastIndex = buff.length() - 1;
    if (lastIndex > -1
        && !((buff.charAt(lastIndex) == ' ')
             && (buff.charAt(lastIndex - 1) == ','))) {

      buff.append(", ");
    }
  }


  // returns true if string is not null and not empty.
  // NOTE - assumes string has already been trimmed
  // of leading & trailing whitespace
  private boolean notNullAndNotEmpty(String arg) {
    //return (arg != null && !(arg.equals(EMPTY_STRING)));
	  return (!Util.isBlank(arg));
  }


  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();
  //
  public OrderedMap getPageData() {

    throw new UnsupportedOperationException(
      "PartyPage -> getPageData() method not implemented!");
  }

  /**
    *  gets the Map object that contains all the key/value paired
    *
    *  @param    rootXPath the string xpath to which this dialog's xpaths will be
    *            appended when making name/value pairs.  For example, in the
    *            xpath: /eml:eml/dataset/creator[2]/individualName/surName, the
    *            root would be /eml:eml/dataset/creator[2].
    *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
    *            SQUARE BRACKETS []
    *
    *  @return   data the Map object that contains all the
    *            key/value paired settings for this particular wizard page
    */
  public OrderedMap getPageData(String rootXPath) {

    returnMap.clear();
    String nextText = null;


    if (isReference()) {
      returnMap.put(rootXPath + "/references[1]", referencesNodeIDString);
      // added by DFH  4/10/2004
      if (editingOriginalRef && rolePickList!=null) {
        if (getCurrentPickListRole().length() > 0) {
           returnMap.put(rootXPath + "/role[1]", getCurrentPickListRole());
        }
      }
      // end DFH add
      Log.debug(45, "getPageData("+rootXPath
                +") Setting /references to " + referencesNodeIDString);

    } else {

      returnMap.put(rootXPath + "/@id", getRefID());
      Log.debug(45, "getPageData("+rootXPath
                +") setting refID /@id as " + referenceIdString);

      nextText = salutationField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/individualName/salutation[1]", nextText);
      }

      nextText = firstNameField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/individualName/givenName[1]", nextText);
      }

      nextText = lastNameField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/individualName/surName[1]", nextText);
      }

      nextText = organizationField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/organizationName[1]", nextText);
      }

      nextText = positionNameField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/positionName[1]", nextText);
      }

      int dpPredct = 1;
      nextText = address1Field.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/deliveryPoint["+dpPredct+"]", nextText);
        dpPredct++;
      }

      nextText = address2Field.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/deliveryPoint["+dpPredct+"]", nextText);
      }

      nextText = cityField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/city[1]", nextText);
      }

      nextText = stateField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/administrativeArea[1]", nextText);
      }

      nextText = zipField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/postalCode[1]", nextText);
      }

      nextText = countryField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/country[1]", nextText);
      }

      int phnPredct = 1;
      nextText = phoneField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/phone["+phnPredct+"]", nextText);
        returnMap.put(rootXPath + "/phone["+phnPredct+"]/@phonetype", "voice");
        phnPredct++;
      }

      nextText = faxField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/phone["+phnPredct+"]", nextText);
        returnMap.put(rootXPath + "/phone["+phnPredct+"]/@phonetype", "fax");
      }

      nextText = emailField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/electronicMailAddress[1]", nextText);
      }

      nextText = urlField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/onlineUrl[1]", nextText);
      }
    }

    if (!editingOriginalRef && rolePickList!=null) {

      if (getCurrentPickListRole().length() > 0) {
        returnMap.put(rootXPath + "/role[1]", getCurrentPickListRole());
      }
    }
    return returnMap;
  }


  public boolean setPageData(OrderedMap map, String rootXPath) {

    Log.debug(45,"PartyPage.setPageData() called with rootXPath = " + rootXPath
              + "\n Map = \n" + map);

    if (rootXPath != null && rootXPath.trim().length() > 0) {

      //remove any trailing slashes...
      while (rootXPath.endsWith("/")) {
        rootXPath = rootXPath.substring(0, rootXPath.length() - 1);
      }
    }

    clearAllFields();

    if (map == null) return true;

    String xpathRootNoPredicates = XMLUtilities.removeAllPredicates(rootXPath);

    Log.debug(45, "PartyPage.setPageData() xpathRootNoPredicates = "
              + xpathRootNoPredicates);

    map = keepOnlyLastPredicateInKeys(map);


    //get rid of scope attribute, if it exists
    String scope = (String)map.get(xpathRootNoPredicates + "/@scope");
    if (scope != null)  map.remove(xpathRootNoPredicates + "/@scope");


    //do role first, since it applies even if this is a reference
    getRoleFromMapAndRemove(map, xpathRootNoPredicates);


    // check if it's a reference:
    String ref = (String)map.get(xpathRootNoPredicates + "/references[1]");
    if (ref==null) ref = (String)map.get(xpathRootNoPredicates + "/references");

    Log.debug(45, "/references ref = ("+ref+")");

    if (notNullAndNotEmpty(ref)) {

      map = getMapForRefID(ref);

      if (map != null) {

        referencesNodeIDString = ref;

        //NOTE - rootXPath needs changing to match referenced node's xpath
        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
          rootXPath = (String)it.next();
          //strip leading slash(es)
          while (rootXPath.startsWith("/"))rootXPath = rootXPath.substring(1);

          int firstSlashIdx = rootXPath.indexOf("/");
          if (firstSlashIdx > -1) {
            rootXPath = rootXPath.substring(0, firstSlashIdx);
          }
          rootXPath = "/" + rootXPath;
          xpathRootNoPredicates = XMLUtilities.removeAllPredicates(rootXPath);
          Log.debug(45,
            "PartyPage.setPageData() got a referenced party; new rootXPath = "
               + xpathRootNoPredicates);

          break;
        }

        map = keepOnlyLastPredicateInKeys(map);
        getRoleFromMapAndRemove(map, xpathRootNoPredicates);
      } else {

        Log.debug(15,
                  "** ERROR: PartyPage.setPageData() - got a null map back "
                  + " from AbsDataPkg when asking for reference: " + ref);
        return false;
      }
    }

    Log.debug(45, "PartyPage.setPageData() - map with only last predicates: \n"
              + map);

    String id = (String)map.get(xpathRootNoPredicates + "/@id");
    if (id != null) {
      referenceIdString = (String)map.get(xpathRootNoPredicates + "/@id");
      map.remove(xpathRootNoPredicates + "/@id");
    } else {

      referenceIdString = this.getRefID();
    }

    String nextVal = (String)map.get(xpathRootNoPredicates
                                     + "/individualName/salutation[1]");
    if (nextVal != null) {
      salutationField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/individualName/salutation[1]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates
                              + "/individualName/givenName[1]");
    if (nextVal != null) {
      firstNameField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/individualName/givenName[1]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates
                              + "/individualName/surName[1]");
    if (nextVal != null) {
      lastNameField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/individualName/surName[1]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates + "/organizationName[1]");
    if (nextVal != null) {
      organizationField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/organizationName[1]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates + "/positionName[1]");
    if (nextVal != null) {
      positionNameField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/positionName[1]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates
                              + "/address/deliveryPoint[1]");
    if (nextVal != null) {
      address1Field.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/address/deliveryPoint[1]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates
                              + "/address/deliveryPoint[2]");
    if (nextVal != null) {
      address2Field.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/address/deliveryPoint[2]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates + "/address/city[1]");
    if (nextVal != null) {
      cityField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/address/city[1]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates
                              + "/address/administrativeArea[1]");
    if (nextVal != null) {
      stateField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/address/administrativeArea[1]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates + "/address/postalCode[1]");
    if (nextVal != null) {
      zipField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/address/postalCode[1]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates + "/address/country[1]");
    if (nextVal != null) {
      countryField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/address/country[1]");
    }

    nextVal = (String)map.get(xpathRootNoPredicates + "/phone[1]");
    String type = (String)map.get(xpathRootNoPredicates
                                  + "/phone[1]/@phonetype");

    if (nextVal != null) {
      if (type != null) {
        if (type.equals("voice"))phoneField.setText(nextVal);
        if (type.equals("fax"))faxField.setText(nextVal);
        map.remove(xpathRootNoPredicates + "/phone[1]/@phonetype");
      }
      map.remove(xpathRootNoPredicates + "/phone[1]");
    }

    nextVal = (String)map.get(xpathRootNoPredicates + "/phone[2]");
    type = (String)map.get(xpathRootNoPredicates + "/phone[2]/@phonetype");

    if (nextVal != null) {
      if (type != null) {
        if (type.equals("voice"))phoneField.setText(nextVal);
        if (type.equals("fax"))faxField.setText(nextVal);
        map.remove(xpathRootNoPredicates + "/phone[2]/@phonetype");
      }
      map.remove(xpathRootNoPredicates + "/phone[2]");
    }

    nextVal = (String)map.get(xpathRootNoPredicates
                              + "/electronicMailAddress[1]");
    if (nextVal != null) {
      emailField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/electronicMailAddress[1]");
    }
    nextVal = (String)map.get(xpathRootNoPredicates + "/onlineUrl[1]");
    if (nextVal != null) {
      urlField.setText(nextVal);
      map.remove(xpathRootNoPredicates + "/onlineUrl[1]");
    }

    //if anything left in map, then it included stuff we can't handle...
    boolean canHandleAllData = map.isEmpty();

    if (!canHandleAllData) {

      Log.debug(20,
                "PartyPage.setPageData returning FALSE! Map still contains:"
                + map);
    }

    if (isReference()) {

      editingAllowed = canHandleAllData;
      this.setEditable(false);

    } else {

      this.setEditable(true);
    }

    return canHandleAllData;

  }


  private void getRoleFromMapAndRemove(OrderedMap map, String xpathRootNoPredicates) {

    if (rolePickList != null) {
      String role = (String)map.get(xpathRootNoPredicates + "/role[1]");
      if (role != null) {
        if (addAndSetRole(role))map.remove(xpathRootNoPredicates + "/role[1]");
      }
    }
  }



  private OrderedMap keepOnlyLastPredicateInKeys(OrderedMap map) {

    OrderedMap newMap = new OrderedMap();
    Iterator it = map.keySet().iterator();

    while (it.hasNext()) {

      String key = (String)it.next();
      String val = (String)map.get(key);
      String firstPart = null;
      String lastPart = null;

      //if its the id or scope attribute, delete *all* the predicates...

      int lastOpenBracketIndex = key.lastIndexOf("[");

      if (lastOpenBracketIndex > -1
          && lastOpenBracketIndex < key.length() 
          && key.indexOf("@id") < 0
          && key.indexOf("@scope") < 0) {

        firstPart = XMLUtilities.removeAllPredicates(
            key.substring(0, lastOpenBracketIndex));

        //keep last predicate in xpath
        lastPart = key.substring(lastOpenBracketIndex);

        newMap.put(firstPart + lastPart, val);

      } else {

        newMap.put(XMLUtilities.removeAllPredicates(key), val);
      }
    }
    return newMap;
  }


  private OrderedMap getMapForRefID(String ref) {

    if (!notNullAndNotEmpty(ref)) return null;

    OrderedMap mapForRefID = null;

    //get party details from AbstractDataPackage...
    AbstractDataPackage abs
        = UIController.getInstance().getCurrentAbstractDataPackage();

    if (abs == null) {
      Log.debug(45,
                "*** ERROR - PartyPage.getMapForRefID() can't get AbstractDataPkg");
      return null;
    }

    Log.debug(45, "*** doing abs.getSubtreeAtReference("+ref+")");
    Node referencedPartyNode = abs.getSubtreeAtReference(ref);

    if (referencedPartyNode != null) {

      //first get rid of role node, since this shouldn't be part of reference
      Node[] children = XMLUtilities.getNodeListAsNodeArray(
          referencedPartyNode.getChildNodes());

      for (int idx = 0; idx < children.length; idx++) {

        if (children[idx].getNodeName().equals("role")) {

          children[idx].getParentNode().removeChild(children[idx]);
        }
      }

      mapForRefID = XMLUtilities.getDOMTreeAsXPathMap(referencedPartyNode);
    } else {
      Log.debug(45,
                "*** ERROR - PartyPage.getMapForRefID() can't get referenced party");
      return null;
    }
    Log.debug(45, "PartyPage.getMapForRefID() returning referenced map "
              + mapForRefID);

    return mapForRefID;
  }


  OrderedMap previousValuesMap = new OrderedMap();
  //
//  String previousRootXPath = null;
  //
  private void rememberPreviousValues() {

    previousValuesMap.clear();
    previousValuesMap = this.getPageData(backupXPath);
//    previousRootXPath = rootXPath;
    backupExists = true;
    Log.debug(45, "\n\nPartyPage.rememberPreviousValues() remembering: \n "
              +"\nMap = "+previousValuesMap);
  }


  private boolean restoreFromPreviousValues() {

    Log.debug(45, "\n\nPartyPage.restoreFromPreviousValues() restoring: \n "
              +"\nMap = "+previousValuesMap);
    if (previousValuesMap.isEmpty()) return true;

    boolean canHandleAllData = this.setPageData(previousValuesMap,
                                                backupXPath);
    return canHandleAllData;
  }


  private boolean addAndSetRole(String role) {

    if (rolePickList==null) return false;
    if (rolePicklistShouldBeHidden) return true;

    //quick way to check if role string is already in list:
    //first select it...
    rolePickList.setSelectedItem(role);

    //...then see if it's selected, and if not, add it
    if (rolePickList.getSelectedItem() != role) rolePickList.addItem(role);

    rolePickList.setSelectedItem(role);

    return true;
  }

  private void clearAllFields() {

    if (rolePickList!=null) rolePickList.setSelectedItem(EMPTY_STRING);
    salutationField.setText(EMPTY_STRING);
    firstNameField.setText(EMPTY_STRING);
    lastNameField.setText(EMPTY_STRING);
    organizationField.setText(EMPTY_STRING);
    positionNameField.setText(EMPTY_STRING);
    address1Field.setText(EMPTY_STRING);
    address2Field.setText(EMPTY_STRING);
    cityField.setText(EMPTY_STRING);
    stateField.setText(EMPTY_STRING);
    zipField.setText(EMPTY_STRING);
    countryField.setText(EMPTY_STRING);
    phoneField.setText(EMPTY_STRING);
    faxField.setText(EMPTY_STRING);
    emailField.setText(EMPTY_STRING);
    urlField.setText(EMPTY_STRING);
  }

  private boolean isURL(String urltext) {
    boolean ret = true;
    try{
      URL url = new URL(urltext);
      if (url==null) ret = false;
    }
    catch (Exception e) {
      return false;
    }
    return ret;
  }
}


