/**
 *  '$RCSfile: PartyPage.java,v $'
 *    Purpose: A class for Party Intro Screen
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-30 00:09:15 $'
 * '$Revision: 1.27 $'
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

  private final String pageID = DataPackageWizardInterface.PARTY_CREATOR;
  private final String nextPageID = "";
  private final String title = "Party Page";
  private final String subtitle = "";
  private final String pageNumber = "";

  // define variables to for party types
  public static final short CREATOR = 0;
  public static final short CONTACT = 10;
  public static final short ASSOCIATED = 20;
  public static final short PERSONNEL = 30;

  private static final Dimension PARTY_2COL_LABEL_DIMS = new Dimension(70, 20);
  private static final Dimension PARTY_HALF_LABEL_DIMS = new Dimension(350, 20);
  private static final Dimension PARTY_FULL_LABEL_DIMS = new Dimension(700, 20);

  // xpath for the this page
  private String rootXPath = "/eml:eml/dataset/creator[1]";

  private final String NAME_ROLE_SEPARATOR = ", ";

  // variables to descrive role
  private short role;
  private String roleString;
  private final String[] roleArray
      = new String[] {
      "",
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
  public JLabel desc;
  public JPanel listPanel;

  private ReferencesHandler referencesHandler;
  private ReferencesListener refsDropdownListener;
  private final String   PARTY_GENERIC_NAME = "parties";
  private final String[] REFSHANDLER_SURROGATE_STRING = new String[] {
      "/individualName/givenName",
      " ",
      "/individualName/surName",
      " - (",
      "/role",
      ")"
  };
  private String referenceIdString;
  private String referredIdString;
  public boolean isReference;
  public boolean referDiffDP = false;
  private boolean editReference;

  //
  private final String[] checkBoxArray
      = new String[] {
      "Do you want to edit "
      + "the above information?"};

  public PartyPage() {
    init();
  }


  /**
   * sets the role and roleString for this wizard page
   *
   * @param role short
   */
  public void setRole(short role) {
    this.role = role;

    switch (role) {
      case CREATOR:
        roleString = "Owner";
        break;
      case CONTACT:
        roleString = "Contact";
        break;
      case ASSOCIATED:
        roleString = "Associated Party";
        break;
      case PERSONNEL:
        roleString = "Personnel";
        break;
    }

    init();
  }

  /**
   *  gets the role string for this wizard page
   *
   *  @return   the role for this wizard page
   */
  public String getRole() {
    String roleString = null;
    switch (role) {
      case CREATOR:
        roleString = "Owner";
        break;
      case CONTACT:
        roleString = "Contact";
        break;
      case ASSOCIATED:
        roleString = getCurrentRole();
        break;
      case PERSONNEL:
        roleString = getCurrentRole();
        break;
    }
    return roleString;
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    referencesHandler = new ReferencesHandler(PARTY_GENERIC_NAME,
                                              REFSHANDLER_SURROGATE_STRING);

    desc = WidgetFactory.makeHTMLLabel("<font size=\"4\"><b>&nbsp;&nbsp;"
                                       + roleString + " Details</b></font>", 1);
    middlePanel = new JPanel();
    this.setLayout(new BorderLayout());
    this.add(middlePanel, BorderLayout.CENTER);

    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(desc);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    final PartyPage instance = this;

    // create itemlistener for references list


    refsDropdownListener = new ReferencesListener() {

      public void referenceSelected(ReferenceSelectionEvent event) {

        String eventRefID = event.getReferenceID();

        if (eventRefID==null) {
        // If refID is null, then the user has selected blank entry (ie does not
        // want to choose a referenced value. Therefore, set party instance
        // to editable, clear out all values and remove the checkBoxPanel
        isReference = false;
        referredIdString = null;

        instance.setEditable(true);
        instance.setPageData(null, null);

        checkBoxPanel.setVisible(false);
        Log.debug(45, "Setting referredIdString to null");

      } else {
        // If refID is not null, a previous entry has been chosen for

        // Unhilite all the components... this is because now a valid
        // previous entered party has been selected
        WidgetFactory.unhiliteComponent(lastNameLabel);
        WidgetFactory.unhiliteComponent(organizationLabel);
        WidgetFactory.unhiliteComponent(positionNameLabel);
        warningPanel.setVisible(false);
        WidgetFactory.unhiliteComponent(warningLabel);


        short location = event.getLocation();

        switch (location) {

          case ReferenceSelectionEvent.CURRENT_DATA_PACKAGE:

            // referredPage  was created in same DP - so current page would be
             // a reference... get reference Id, set instance non-editable,
             // set value of all fields and radio panel visible
             isReference = true;
             referredIdString = eventRefID;

             instance.setEditable(false);
             instance.setPageData(event.getXPathValsMap(), instance.rootXPath);

             checkBoxPanel.setVisible(true);
             Log.debug(45, "The referred page is not in a different DP. "
                       + "Setting referredIdString to " + referredIdString);

            break;

          case ReferenceSelectionEvent.DIFFERENT_DATA_PACKAGE:

            // referredPage was not created in same DP - so current page would
            // not be a reference... set reference Id null, set referDiffDP
            // true, set instance editable as it is not a reference,
            // set value of all fields and radio panel invisible
            isReference = false;
            referDiffDP = true;
            referredIdString = null;

            instance.setEditable(true);
            instance.setPageData(event.getXPathValsMap(), instance.rootXPath);

            checkBoxPanel.setVisible(false);
            Log.debug(45, "The referred page is in a different DP. "
                      + "Setting referredIdString to null.");

            break;

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
    listPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));

    String listValues[] = {};

//    refsDropdown = WidgetFactory.makePickList(listValues, false, 0,
//                                           ilistener);

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
    rolePickList = null;
    if (role == ASSOCIATED || role == PERSONNEL) {
      rolePanel = WidgetFactory.makePanel(1);
      roleLabel = WidgetFactory.makeLabel("Role:", true);
      rolePanel.add(roleLabel);
      rolePickList = WidgetFactory.makePickList(roleArray, true, 0,
                                                new ItemListener() {
        public void itemStateChanged(ItemEvent e) {}
      });
      rolePanel.add(rolePickList);
      rolePanel.setBorder(new javax.swing.border.EmptyBorder(0,
          12 * WizardSettings.PADDING,
          0, 8 * WizardSettings.PADDING));
      middlePanel.add(rolePanel);
      middlePanel.add(WidgetFactory.makeHalfSpacer());
    }

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
    ItemListener ilistener1 = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {

        Log.debug(45, "got radiobutton command: " + e.getStateChange());
        onLoadAction(); // ????

        if (e.getStateChange() == ItemEvent.DESELECTED) {
          // If the checkbox is not selected - set it to non-editable
          instance.setEditable(false);
        }
        else if (e.getStateChange() == ItemEvent.SELECTED) {
          // If the checkbox is selected - need to ask the user if he wants to
          // edit the previous entry or make a copy of the previous entry
          // and edit that one
          Object[] optionArray
              = new String[] {
              "Edit previous entry",
              "Copy and edit the previous Entry",
              "Cancel"};
          JOptionPane optPane = new JOptionPane();
          optPane.setOptions(optionArray);
          optPane.setMessage(
              "Do you want to edit the previous entry or do you "
              + "want to create a copy of the previous entry and edit that?");
          optPane.createDialog(instance, "Select an option...").show();
          Object selectedValue = optPane.getValue();

          if (selectedValue == optionArray[0]) {
            // edit the previous reference
            instance.setEditable(true);
            editReference = true;
          }
          else if (selectedValue == optionArray[1]) {
            // create a new copy - do not edit reference, is a reference
            // and remove reference id string
            instance.setEditable(true);
            editReference = false;
            isReference = false;
            referredIdString = null;
          }
          else {
            // Cancel - remove selection from source - instance is not editable,
            // and do edit reference
            JCheckBox source = (JCheckBox) e.getSource();
            source.setSelected(false);
            instance.setEditable(false);
            editReference = false;
          }
        }
        instance.validate();
        instance.repaint();
      }
    };

    // Check box for ediitng the instance - si not visible initially
    checkBoxPanel = WidgetFactory.makeCheckBoxPanel(checkBoxArray, -1, ilistener1);
    checkBoxPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));
    middlePanel.add(checkBoxPanel);
    checkBoxPanel.setVisible(false);

//    getPartyList();
  }


  private void updateRefsDropdown() {


  }


//
//  /**
//   * The action removes all previous entries in party list and adds all entries
//   * in WidgetFactory.partyRefsListForAllPkgs to the list. So in way it refreshes
//   * the list.
//   */
//  private void getPartyList() {
//
//    // remove all previous items
//    refsDropdown.removeAllItems();
//
//    // get all elements from WidgetFactory and add them to responsiblePartyList
//    for (int count = 0; count < WidgetFactory.partyRefsListForAllPkgs.size();
//         count++) {
//      List rowList = (List) WidgetFactory.partyRefsListForAllPkgs.get(count);
//      String name = (String) rowList.get(0);
//      String role = (String) rowList.get(1);
//      String row = "";
//      if (name != "") {
//        row = name + NAME_ROLE_SEPARATOR + role;
//      }
//      refsDropdown.addItem(row);
//    }
//  }



//  /**
//   * The action checks if the referredPage was created in present DP.
//   *
//   * @return PartyPage Object if referredPage was created in same DP. Otherwise
//   *   returns null
//   * @param referredPage PartyPage
//   */
//  private boolean partyInSameDP(PartyPage referredPage) {
//
//    Log.debug(45, "\n(((((((((((((( partyInSameDP() called ))))))))))))))))))))))))))))))))))))))))))");
//    Log.debug(45, "\n(((((((((((((( referredPage = " + referredPage);
//
// //    List dpList = WidgetFactory.getPartyRefsListForCurrentPkg();
// //
// //    Iterator it = dpList.iterator();
// //    while (it.hasNext()) {
// //      List row = (List)it.next();
// //      PartyPage page = (PartyPage)row.get(3);
//
//    OrderedMap map = getMapForRefID(referredPage.getRefID());
//
//    //if adp returns null, then this ID doesn't exist in the current adp, so it
//    //must be in a different adp
//    if (map == null) return false;
//
//    PartyPage page = (PartyPage)WizardPageLibrary.getPage(
//                                         DataPackageWizardInterface.PARTY_PAGE);
//    if (page.setPageData(map, referredPage.getRootXPath())) {
//
//
//    }
//    if (referredPage.getsalutationFieldText().equals(page.
//                                                     getsalutationFieldText()) &&
//        referredPage.getfirstNameFieldText().equals(page.getfirstNameFieldText()) &&
//        referredPage.getlastNameFieldText().equals(page.getlastNameFieldText()) &&
//        referredPage.getorganizationFieldText().equals(page.
//                                                       getorganizationFieldText()) &&
//        referredPage.getpositionNameFieldText().equals(page.
//                                                       getpositionNameFieldText()) &&
//        referredPage.getaddress1FieldText().equals(page.getaddress1FieldText()) &&
//        referredPage.getaddress2FieldText().equals(page.getaddress2FieldText()) &&
//        referredPage.getcityFieldText().equals(page.getcityFieldText()) &&
//        referredPage.getstateFieldText().equals(page.getstateFieldText()) &&
//        referredPage.getzipFieldText().equals(page.getzipFieldText()) &&
//        referredPage.getcountryFieldText().equals(page.getcountryFieldText()) &&
//        referredPage.getphoneFieldText().equals(page.getphoneFieldText()) &&
//        referredPage.getfaxFieldText().equals(page.getfaxFieldText()) &&
//        referredPage.getemailFieldText().equals(page.getemailFieldText()) &&
//        referredPage.geturlFieldText().equals(page.geturlFieldText())) {
//      return true;
//    }
// //    }
//    Log.debug(45, "\n(((((((((((((( partyInSameDP() NO MATCH FOUND!! ))))))))))))))))))))))))))))))))))))))))))\n\n");
//    return false;
//  }


  /**
   * The action sets the value of all Fields in 'this' page equal to values of
   * corresponding fields in PartyPage passed as arguement
   *
   * @param Page PartyPage
   */
  private void setValue(PartyPage Page) {
    if (Page == null) {
      // if Page is null, that means clear out all the values.
      if (rolePickList != null) {
        rolePickList.setSelectedIndex(0);
      }
      salutationField.setText("");
      firstNameField.setText("");
      lastNameField.setText("");
      organizationField.setText("");
      positionNameField.setText("");
      address1Field.setText("");
      address2Field.setText("");
      cityField.setText("");
      stateField.setText("");
      zipField.setText("");
      countryField.setText("");
      phoneField.setText("");
      faxField.setText("");
      emailField.setText("");
      urlField.setText("");
    }
    else {
      // if Page is no null - copy all the values.
      if (rolePickList != null) {
        rolePickList.addItem(Page.getRole());
        rolePickList.setSelectedItem(Page.getRole());
        rolePickList.setEditable(true);
      }
      salutationField.setText(Page.getsalutationFieldText());
      firstNameField.setText(Page.getfirstNameFieldText());
      lastNameField.setText(Page.getlastNameFieldText());
      organizationField.setText(Page.getorganizationFieldText());
      positionNameField.setText(Page.getpositionNameFieldText());
      address1Field.setText(Page.getaddress1FieldText());
      address2Field.setText(Page.getaddress2FieldText());
      cityField.setText(Page.getcityFieldText());
      stateField.setText(Page.getstateFieldText());
      zipField.setText(Page.getzipFieldText());
      countryField.setText(Page.getcountryFieldText());
      phoneField.setText(Page.getphoneFieldText());
      faxField.setText(Page.getfaxFieldText());
      emailField.setText(Page.getemailFieldText());
      urlField.setText(Page.geturlFieldText());
    }
  }


  /**
   * The action sets all the fields in 'this' page editable or non-editable
   * based on boolean arguement passed
   *
   * @param editable boolean
   */
  public void setEditable(boolean editable) {
    if (rolePickList != null) {
      rolePickList.setEditable(editable);
    }
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
  }


  protected String getRootXPath() {
    return this.rootXPath;
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


  private String getCurrentRole() {

     String role = (String) rolePickList.getSelectedItem();
     return (role!=null)? role.trim() : "";
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
    if (role == ASSOCIATED || role == PERSONNEL) {

      if (notNullAndNotEmpty(getCurrentRole())) {

        WidgetFactory.unhiliteComponent(roleLabel);

      }
      else {

        WidgetFactory.hiliteComponent(roleLabel);
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

    }
    else {

      WidgetFactory.hiliteComponent(lastNameLabel);
      WidgetFactory.hiliteComponent(organizationLabel);
      WidgetFactory.hiliteComponent(positionNameLabel);
      warningLabel.setText("Warning: at least one of the three entries is "
                           +
                           "required: Last Name, Position Name or Organization");
      warningPanel.setVisible(true);
      WidgetFactory.hiliteComponent(warningLabel);
      return false;
    }

//    // if we are going to edit a previous reference and referredPage is not null,
//    // edit the values of fields in previous page....
//    if (editReference && referredPage != null) {
//      String nextText = salutationField.getText().trim();
//      referredPage.salutationField.setText(nextText);
//
//      nextText = firstNameField.getText().trim();
//      referredPage.firstNameField.setText(nextText);
//
//      nextText = lastNameField.getText().trim();
//      referredPage.lastNameField.setText(nextText);
//
//      nextText = organizationField.getText().trim();
//      referredPage.organizationField.setText(nextText);
//
//      nextText = positionNameField.getText().trim();
//      referredPage.positionNameField.setText(nextText);
//
//      nextText = address1Field.getText().trim();
//      referredPage.address1Field.setText(nextText);
//
//      nextText = address2Field.getText().trim();
//      referredPage.address2Field.setText(nextText);
//
//      nextText = cityField.getText().trim();
//      referredPage.cityField.setText(nextText);
//
//      nextText = stateField.getText().trim();
//      referredPage.stateField.setText(nextText);
//
//      nextText = zipField.getText().trim();
//      referredPage.zipField.setText(nextText);
//
//      nextText = countryField.getText().trim();
//      referredPage.countryField.setText(nextText);
//
//      nextText = phoneField.getText().trim();
//      referredPage.phoneField.setText(nextText);
//
//      nextText = faxField.getText().trim();
//      referredPage.faxField.setText(nextText);
//
//      nextText = emailField.getText().trim();
//      referredPage.emailField.setText(nextText);
//
//      nextText = urlField.getText().trim();
//      referredPage.urlField.setText(nextText);
//    }

    addToPartyRefsLists();

    return true;
  }


  /**
   * If appropriate, adds this party object to
   * WidgetFactory.partyRefsListForAllPkgs
   */
  protected void addToPartyRefsLists() {

    Log.debug(45, "addToPartyRefsLists() called - ");

    if (!this.isReference) {

      Log.debug(45,
                "this id NOT a reference - adding to partyRefsListForAllPkgs...");

      List newRow = this.getSurrogate();
      newRow.add(this);

      if (!this.referDiffDP) WidgetFactory.partyRefsListForAllPkgs.add(newRow);
    }
  }


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
      UIController.getInstance().getCurrentAbstractDataPackage(), refsDropdown);
  }

  /**
   *  gets the referenceID for this wizard page
   *
   *  @return String refID
   */
  protected String getRefID() {
    if (!notNullAndNotEmpty(referenceIdString)) {
      referenceIdString = "ResponsibleParty." +
          PartyMainPage.RESPONSIBLE_PARTY_REFERENCE_COUNT++;
    }

    return referenceIdString;
  }


  /**
   * The action is called from PartyMainPage. It gets updated values of all
   * fields of referredPage and sets it in 'this' Page. Also it updates Party
   * list irrespective of whether this is a referred page or not.
   */
//  protected void setEditValue() {
//
//    // update the party list because it might be changed since the last time
//    // this page was created .. doesnt work thought at present
//    // hence TODO
//    //getPartyList();
//
//    if (isReference && referredPage != null) {
//
//      String nextText = referredPage.salutationField.getText().trim();
//      salutationField.setText(nextText);
//
//      nextText = referredPage.firstNameField.getText().trim();
//      firstNameField.setText(nextText);
//
//      nextText = referredPage.lastNameField.getText().trim();
//      lastNameField.setText(nextText);
//
//      nextText = referredPage.organizationField.getText().trim();
//      organizationField.setText(nextText);
//
//      nextText = referredPage.positionNameField.getText().trim();
//      positionNameField.setText(nextText);
//
//      nextText = referredPage.address1Field.getText().trim();
//      address1Field.setText(nextText);
//
//      nextText = referredPage.address2Field.getText().trim();
//      address2Field.setText(nextText);
//
//      nextText = referredPage.cityField.getText().trim();
//      cityField.setText(nextText);
//
//      nextText = referredPage.stateField.getText().trim();
//      stateField.setText(nextText);
//
//      nextText = referredPage.zipField.getText().trim();
//      zipField.setText(nextText);
//
//      nextText = referredPage.countryField.getText().trim();
//      countryField.setText(nextText);
//
//      nextText = referredPage.phoneField.getText().trim();
//      phoneField.setText(nextText);
//
//      nextText = referredPage.faxField.getText().trim();
//      faxField.setText(nextText);
//
//      nextText = referredPage.emailField.getText().trim();
//      emailField.setText(nextText);
//
//      nextText = referredPage.urlField.getText().trim();
//      urlField.setText(nextText);
//    }
//  }

  /**
   *  gets the salutationField for this wizard page
   *
   *  @return   the salutationField String for this wizard page
   */
  protected String getsalutationFieldText() {
    return this.salutationField.getText();
  }

  /**
   *  gets the firstNameField for this wizard page
   *
   *  @return   the firstNameField String for this wizard page
   */
  protected String getfirstNameFieldText() {
    return this.firstNameField.getText();
  }

  /**
   *  gets the lastNameField for this wizard page
   *
   *  @return   the lastNameField String for this wizard page
   */
  protected String getlastNameFieldText() {
    return this.lastNameField.getText();
  }

  /**
   *  gets the urlField for this wizard page
   *
   *  @return  the urlField String for this wizard page
   */
  protected String geturlFieldText() {
    return this.urlField.getText();
  }

  /**
   *  gets the positionNameField for this wizard page
   *
   *  @return   the positionNameField String for this wizard page
   */
  protected String getpositionNameFieldText() {
    return this.positionNameField.getText();
  }

  /**
   *  gets the cityField for this wizard page
   *
   *  @return   the cityField String for this wizard page
   */
  protected String getcityFieldText() {
    return this.cityField.getText();
  }

  /**
   *  gets the faxField for this wizard page
   *
   *  @return   the faxField String for this wizard page
   */
  protected String getfaxFieldText() {
    return this.faxField.getText();
  }

  /**
   *  gets the zipField for this wizard page
   *
   *  @return   the zipField String for this wizard page
   */
  protected String getzipFieldText() {
    return this.zipField.getText();
  }

  /**
   *  gets the stateField for this wizard page
   *
   *  @return   the stateField String for this wizard page
   */
  protected String getstateFieldText() {
    return this.stateField.getText();
  }

  /**
   *  gets the emailField for this wizard page
   *
   *  @return   the emailField String for this wizard page
   */
  protected String getemailFieldText() {
    return this.emailField.getText();
  }

  /**
   *  gets the organizationFieldText() for this wizard page
   *
   *  @return   the organizationField String for this wizard page
   */
  protected String getorganizationFieldText() {
    return this.organizationField.getText();
  }

  /**
   *  gets the countryField for this wizard page
   *
   *  @return   the countryField String for this wizard page
   */
  protected String getcountryFieldText() {
    return this.countryField.getText();
  }

  /**
   *  gets the phoneField for this wizard page
   *
   *  @return   the phoneField String for this wizard page
   */
  protected String getphoneFieldText() {
    return this.phoneField.getText();
  }

  /**
   *  gets the address1Field for this wizard page
   *
   *  @return   the address1Field String for this wizard page
   */
  protected String getaddress1FieldText() {
    return this.address1Field.getText();
  }

  /**
   *  gets the address2Field for this wizard page
   *
   *  @return   the address2Field String for this wizard page
   */
  protected String getaddress2FieldText() {
    return this.address2Field.getText();
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
    if (role == ASSOCIATED || role == PERSONNEL) {
      StringBuffer roleBuff = new StringBuffer(roleString);
      if (notNullAndNotEmpty(getCurrentRole())) {
        roleBuff.append(" (");
        roleBuff.append(getCurrentRole());
        roleBuff.append(")");
      }
      surrogate.add(roleBuff.toString());
    }
    else {
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
        && ! ( (buff.charAt(lastIndex) == ' ')
              && (buff.charAt(lastIndex - 1) == ','))) {

      buff.append(", ");
    }
  }

  // returns true if string is not null and not empty.
  // NOTE - assumes string has already been trimmed
  // of leading & trailing whitespace
  private boolean notNullAndNotEmpty(String arg) {
    return (arg != null && !(arg.equals("")));
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
  private OrderedMap returnMap = new OrderedMap();

  //
  public OrderedMap getPageData() {
    return getPageData(rootXPath);
  }

  public OrderedMap getPageData(String rootXPath) {

    returnMap.clear();
    String nextText = null;

    if (isReference) {
      returnMap.put(rootXPath + "/references", referredIdString);
      Log.debug(45, "Setting reference to " + referredIdString);
    }
    else {

      if (notNullAndNotEmpty(referenceIdString)) {
        returnMap.put(rootXPath + "/@id", referenceIdString);
        Log.debug(45, "Setting reference as " + referenceIdString);
      }

      nextText = salutationField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/individualName/salutation", nextText);
      }

      nextText = firstNameField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/individualName/givenName", nextText);
      }

      nextText = lastNameField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/individualName/surName", nextText);
      }

      nextText = organizationField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/organizationName", nextText);
      }

      nextText = positionNameField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/positionName", nextText);
      }

      nextText = address1Field.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/deliveryPoint[1]", nextText);
      }

      nextText = address2Field.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/deliveryPoint[2]", nextText);
      }

      nextText = cityField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/city", nextText);
      }

      nextText = stateField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/administrativeArea", nextText);
      }

      nextText = zipField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/postalCode", nextText);
      }

      nextText = countryField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/address/country", nextText);
      }

      nextText = phoneField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/phone[1]", nextText);
        returnMap.put(rootXPath + "/phone[1]/@phonetype", "voice");
      }

      nextText = faxField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/phone[2]", nextText);
        returnMap.put(rootXPath + "/phone[2]/@phonetype", "fax");
      }

      nextText = emailField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/electronicMailAddress", nextText);
      }

      nextText = urlField.getText().trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(rootXPath + "/onlineUrl", nextText);
      }
    }

    if (role == ASSOCIATED || role == PERSONNEL) {

      if (getCurrentRole()!=null && getCurrentRole().length() > 0) {
        returnMap.put(rootXPath + "/role", getCurrentRole());
      }
    }

    return returnMap;
  }



  public boolean setPageData(OrderedMap map, String _rootXPath) {

    Log.debug(45,
              "PartyPage.setPageData() called with _rootXPath = " + _rootXPath
              + "\n Map = \n" + map);

    checkBoxPanel.setVisible(false);

    if (_rootXPath!=null && _rootXPath.trim().length() > 0) this.rootXPath =
                                                                     _rootXPath;

    String xpathRootNoPredicates
        = XMLUtilities.removeAllPredicates(this.rootXPath);

    while (xpathRootNoPredicates.endsWith("/")) {
      xpathRootNoPredicates
          = xpathRootNoPredicates.substring(0,
                                            xpathRootNoPredicates.length() - 1);
    }
    Log.debug(45,
        "PartyPage.setPageData() XMLUtilities.removeAllPredicates(rootXPath) = "
              + xpathRootNoPredicates);

    map = removeAllButLastPredicatesFromMapKeys(map);

    Log.debug(45,
        "PartyPage.setPageData() after removeAllButLastPredicatesFromMapKeys. map = \n"
              + map);

    isReference = false;

    // check if it's a reference:
    String ref = (String)map.get(xpathRootNoPredicates + "/references");

    if (notNullAndNotEmpty(ref)) {

      map = getMapForRefID(ref);

      if (map != null) {

        isReference = true;
        this.setEditable(false);
        checkBoxPanel.setVisible(true);
      } else {

        Log.debug(15, "** ERROR: PartyPage.setPageData() - got a null map back "
                 +" from AbsDataPkg when asking for reference: "+ref);
        return false;
      }
    }

    String id = (String)map.get(xpathRootNoPredicates + "/@id");
    if (id != null) {
      referenceIdString = (String)map.get(xpathRootNoPredicates + "/@id");
      map.remove(xpathRootNoPredicates + "/@id");
    } else {

      referenceIdString = this.getRefID();
    }

    //role
    if (role == ASSOCIATED || role == PERSONNEL) {
      String role = (String)map.get(xpathRootNoPredicates + "/role[1]");
      if (role != null) {
        rolePickList.addItem(role);
        rolePickList.setSelectedItem(role);
        map.remove(xpathRootNoPredicates + "/role[1]");
      }
    }

    String name = (String)map.get(xpathRootNoPredicates
                                  + "/individualName/salutation[1]");
    if (name != null) {
      salutationField.setText(name);
      map.remove(xpathRootNoPredicates  + "/individualName/salutation[1]");
    }
    name = (String)map.get(xpathRootNoPredicates + "/individualName/givenName[1]");
    if (name != null) {
      firstNameField.setText(name);
      map.remove(xpathRootNoPredicates  + "/individualName/givenName[1]");
    }
    name = (String)map.get(xpathRootNoPredicates + "/individualName/surName[1]");
    if (name != null) {
      lastNameField.setText(name);
      map.remove(xpathRootNoPredicates  + "/individualName/surName[1]");
    }
    name = (String)map.get(xpathRootNoPredicates + "/organizationName[1]");
    if (name != null) {
      organizationField.setText(name);
      map.remove(xpathRootNoPredicates  + "/organizationName[1]");
    }
    name = (String)map.get(xpathRootNoPredicates + "/positionName[1]");
    if (name != null) {
      positionNameField.setText(name);
      map.remove(xpathRootNoPredicates  + "/positionName[1]");
    }
    name = (String)map.get(xpathRootNoPredicates + "/address/deliveryPoint[1]");
    if (name != null) {
      address1Field.setText(name);
      map.remove(xpathRootNoPredicates  + "/address/deliveryPoint[1]");
    }
    name = (String)map.get(xpathRootNoPredicates + "/address/deliveryPoint[2]");
    if (name != null) {
      address2Field.setText(name);
      map.remove(xpathRootNoPredicates  + "/address/deliveryPoint[2]");
    }
    name = (String)map.get(xpathRootNoPredicates + "/address/city[1]");
    if (name != null) {
      cityField.setText(name);
      map.remove(xpathRootNoPredicates  + "/address/city[1]");
    }
    name = (String)map.get(xpathRootNoPredicates
                           + "/address/administrativeArea[1]");
    if (name != null) {
      stateField.setText(name);
      map.remove(xpathRootNoPredicates  + "/address/administrativeArea[1]");
    }
    name = (String)map.get(xpathRootNoPredicates + "/address/postalCode[1]");
    if (name != null) {
      zipField.setText(name);
      map.remove(xpathRootNoPredicates  + "/address/postalCode[1]");
    }
    name = (String)map.get(xpathRootNoPredicates + "/address/country[1]");
    if (name != null) {
      countryField.setText(name);
      map.remove(xpathRootNoPredicates  + "/address/country[1]");
    }

    name = (String)map.get(xpathRootNoPredicates + "/phone[1]");
    String type = (String)map.get(xpathRootNoPredicates + "/phone[1]/@phonetype");
    if (type!=null) map.remove(xpathRootNoPredicates + "/phone[1]/@phonetype");

    if (name != null) {
      map.remove(xpathRootNoPredicates  + "/phone[1]");
      if (type.equals("voice")) phoneField.setText(name);
    }

    name = (String)map.get(xpathRootNoPredicates + "/phone[2]");
    type = (String)map.get(xpathRootNoPredicates + "/phone[2]/@phonetype");
    if (type!=null) map.remove(xpathRootNoPredicates + "/phone[2]/@phonetype");

    if (name != null) {
      map.remove(xpathRootNoPredicates  + "/phone[2]");
      if (type.equals("fax")) faxField.setText(name);
    }
    name = (String)map.get(xpathRootNoPredicates + "/electronicMailAddress[1]");
    if (name != null) {
      emailField.setText(name);
      map.remove(xpathRootNoPredicates + "/electronicMailAddress[1]");
    }
    name = (String)map.get(xpathRootNoPredicates + "/onlineUrl[1]");
    if (name != null) {
      urlField.setText(name);
      map.remove(xpathRootNoPredicates + "/onlineUrl[1]");
    }

    //if anything left in map, then it included stuff we can't handle...
     boolean returnVal = map.isEmpty();

     if (!returnVal) {

       Log.debug(20,
                 "PartyPage.setPageData returning FALSE! Map still contains:"
                 + map);
     }
     return returnVal;

  }

  private OrderedMap removeAllButLastPredicatesFromMapKeys(OrderedMap map) {

    OrderedMap newMap = new OrderedMap();
    Iterator it = map.keySet().iterator();
    while(it.hasNext()) {
      String key = (String) it.next();
      String val = (String)map.get(key);
      String firstPart = null;
      String lastPart  = null;

      int lastOpenBracketIndex = key.lastIndexOf("[");

      if (lastOpenBracketIndex > -1 && lastOpenBracketIndex < key.length()) {

        firstPart = key.substring(0, lastOpenBracketIndex);
        //keep last predicate in xpath
        lastPart  = key.substring(lastOpenBracketIndex);
      }

      firstPart = XMLUtilities.removeAllPredicates(firstPart);

      newMap.put(firstPart + lastPart, val);
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
    Node referencedPartyNode = abs.getSubtreeAtReference(ref);

    if (referencedPartyNode != null) {
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

}
