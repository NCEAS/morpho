/**
 *  '$RCSfile: PartyPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2003-12-30 17:08:47 $'
 * '$Revision: 1.8 $'
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

import java.util.ArrayList;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.utilities.OrderedMap;

public class PartyPage extends AbstractWizardPage {

  private final String pageID     = DataPackageWizardInterface.ATTRIBUTE_PAGE;
  private final String nextPageID = "";
  private final String title      = "Attribute Page";
  private final String subtitle   = "";
  private final String pageNumber = "";

  public static final short CREATOR    = 0;
  public static final short CONTACT    = 10;
  public static final short ASSOCIATED = 20;
  public static final short PERSONNEL  = 30;

  private static final Dimension PARTY_2COL_LABEL_DIMS = new Dimension(70,20);
  private static final Dimension PARTY_HALF_LABEL_DIMS = new Dimension(350,20);
  private static final Dimension PARTY_FULL_LABEL_DIMS = new Dimension(700,20);

  private final String xPathRoot = "/eml:eml/dataset/creator[1]";

  private short  role;
  private String roleString;
  private JLabel     roleLabel;
  private JComboBox  rolePickList;
  private JTextField salutationField;
  private JTextField firstNameField;
  private JLabel     lastNameLabel;
  private JTextField lastNameField;
  private JLabel     organizationLabel;
  private JTextField organizationField;
  private JLabel     positionNameLabel;
  private JPanel     warningPanel;
  private JLabel     warningLabel;
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

  private final String[] roleArray
                              = new String[]{ "",
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

  private int currentRoleIndex;
  private String currentRole;
  public boolean isReference;

  public PartyPage() {
  }

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

  public String getRole(){
    String roleString = null;
    switch (role) {
      case CREATOR:
        roleString = "Owner";
        break;
      case CONTACT:
        roleString = "Contact";
        break;
      case ASSOCIATED:
        roleString = (String)rolePickList.getSelectedItem();
        break;
      case PERSONNEL:
        roleString = (String)rolePickList.getSelectedItem();
        break;
    }
    return roleString;
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    JLabel desc = WidgetFactory.makeHTMLLabel("<font size=\"4\"><b>&nbsp;&nbsp;"
                                              +roleString+" Details</b></font>", 1);
    middlePanel = new JPanel();
    this.setLayout( new BorderLayout());
    this.add(middlePanel, BorderLayout.CENTER);

    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(desc);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    ////
    rolePickList = null;
    if (role == ASSOCIATED || role == PERSONNEL) {
      rolePanel = WidgetFactory.makePanel(1);
      roleLabel = WidgetFactory.makeLabel("Role:", true);
      rolePanel.add(roleLabel);
      rolePickList = WidgetFactory.makePickList(roleArray, true, 0,
            new ItemListener(){ public void itemStateChanged(ItemEvent e) {}});
      rolePanel.add(rolePickList);
      rolePanel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
          0,8*WizardSettings.PADDING));
      middlePanel.add(rolePanel);
      middlePanel.add(WidgetFactory.makeHalfSpacer());
    }

    ////
    JPanel salutationPanel = WidgetFactory.makePanel(1);
    salutationPanel.add(WidgetFactory.makeLabel("Salutation:", false));
    salutationField = WidgetFactory.makeOneLineTextField();
    salutationPanel.add(salutationField);
    salutationPanel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));
    middlePanel.add(salutationPanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
    JPanel firstNamePanel = WidgetFactory.makePanel(1);
    firstNamePanel.add(WidgetFactory.makeLabel("First Name:", false));
    firstNameField = WidgetFactory.makeOneLineTextField();
    firstNamePanel.add(firstNameField);
    firstNamePanel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));
    middlePanel.add(firstNamePanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
//    JPanel reqPanel = new JPanel();
//    reqPanel.setLayout( new BorderLayout());
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

//  reqWarningPanel.add(WidgetFactory.makeHalfSpacer());
    reqWarningPanel.add(WidgetFactory.makeDefaultSpacer());
    reqPanel.add(reqWarningPanel);

    JLabel bracketLabel = new JLabel("{");
    bracketLabel.setFont(new Font("Sans-Serif", Font.PLAIN, 40));
    bracketLabel.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    bracketLabel.setBorder(new javax.swing.border.EmptyBorder(0,0,
        3*WizardSettings.PADDING,0));
    reqPanel.add(bracketLabel);
    ////

    ////
    JPanel lastNamePanel = WidgetFactory.makePanel(1);
    lastNameLabel = WidgetFactory.makeLabel("Last Name:", true);
    lastNamePanel.add(lastNameLabel);
    lastNameField = WidgetFactory.makeOneLineTextField();
    lastNamePanel.add(lastNameField);
    lastNamePanel.setBorder(new javax.swing.border.EmptyBorder(0,0,
        0,8*WizardSettings.PADDING));
    reqInfoPanel.add(lastNamePanel);
    reqInfoPanel.add(WidgetFactory.makeHalfSpacer());

    ////
    JPanel organizationPanel = WidgetFactory.makePanel(1);
    organizationLabel = WidgetFactory.makeLabel("Organization:", true);
    organizationPanel.add(organizationLabel);
    organizationField = WidgetFactory.makeOneLineTextField();
    organizationPanel.add(organizationField);
    organizationPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,
        0,8*WizardSettings.PADDING));
    reqInfoPanel.add(organizationPanel);
    reqInfoPanel.add(WidgetFactory.makeHalfSpacer());

    ////
    JPanel positionNamePanel = WidgetFactory.makePanel(1);
    positionNameLabel = WidgetFactory.makeLabel("Position Name:", true);
    positionNamePanel.add(positionNameLabel);
    positionNameField = WidgetFactory.makeOneLineTextField();
    positionNamePanel.add(positionNameField);
    positionNamePanel.setBorder(new javax.swing.border.EmptyBorder(0,0,
        0,8*WizardSettings.PADDING));
    reqInfoPanel.add(positionNamePanel);
    reqInfoPanel.add(WidgetFactory.makeHalfSpacer());

    reqPanel.add(reqInfoPanel, BorderLayout.CENTER);
    middlePanel.add(reqPanel);

    ////
    JPanel address1Panel = WidgetFactory.makePanel(1);
    address1Panel.add(WidgetFactory.makeLabel("Address 1:", false));
    address1Field = WidgetFactory.makeOneLineTextField();
    address1Panel.add(address1Field);
    address1Panel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));
    middlePanel.add(address1Panel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
    JPanel address2Panel = WidgetFactory.makePanel(1);
    address2Panel.add(WidgetFactory.makeLabel("Address 2:", false));
    address2Field = WidgetFactory.makeOneLineTextField();
    address2Panel.add(address2Field);
    address2Panel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));
    middlePanel.add(address2Panel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
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
    cityStatePanel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));
    middlePanel.add(cityStatePanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
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
    zipCountryPanel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));
    middlePanel.add(zipCountryPanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
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
    phoneFaxPanel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));
    middlePanel.add(phoneFaxPanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
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
    emailUrlPanel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));
    middlePanel.add(emailUrlPanel);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    ////
    warningPanel = WidgetFactory.makePanel(1);
    warningLabel = WidgetFactory.makeLabel("Warning: at least one of the three "
      +"entries is required: Last Name, Position Name or Organization", true);
    warningPanel.add(warningLabel);
    warningPanel.setVisible(false);
    setPrefMinMaxSizes(warningLabel, PARTY_FULL_LABEL_DIMS);
    warningPanel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));
    middlePanel.add(warningPanel);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    JPanel listPanel = WidgetFactory.makePanel(1);
    JLabel listLabel = WidgetFactory.makeLabel("You can pick one from one of the earlier "
                                           +"entries that you have made.", false);
    setPrefMinMaxSizes(listLabel, PARTY_HALF_LABEL_DIMS);
    listPanel.add(listLabel);
    listPanel.setBorder(new javax.swing.border.EmptyBorder(0,12*WizardSettings.PADDING,
        0,8*WizardSettings.PADDING));

    final PartyPage instance = this;
    ItemListener ilistener = new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        JComboBox source = (JComboBox)e.getSource();
        if(source.getSelectedIndex() == 0){
          isReference = false;
          instance.setEditable(true);
          instance.setValue(null);
        } else {
          isReference = true;
          int index = source.getSelectedIndex();
          instance.setEditable(false);
          List currentList = (List)WidgetFactory.responsiblePartyList.get(index);
          instance.setValue((PartyPage)currentList.get(3));
        }
      }
    };

    String listValues[] = {};
    JComboBox listCombo = WidgetFactory.makePickList(listValues,false, 0, ilistener);
    for (int count=0; count < WidgetFactory.responsiblePartyList.size(); count++){
      List rowList = (List)WidgetFactory.responsiblePartyList.get(count);
      String name = (String)rowList.get(0);
      String role = (String)rowList.get(1);
      String row = "";
      if (name != ""){
        row = name + ", " + role;
      }
      listCombo.addItem(row);
    }
    listPanel.add(listCombo);
    middlePanel.add(listPanel);
  }

  private void setValue(PartyPage Page) {
    if(Page == null){
      if(rolePickList != null) rolePickList.setSelectedIndex(0);
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
    } else {
      if(rolePickList != null) {
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

  public void setEditable(boolean editable) {
    if(rolePickList != null) rolePickList.setEditable(editable);
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


  private void setPrefMinMaxSizes(JComponent component, Dimension dims) {

    WidgetFactory.setPrefMaxSizes(component, dims);
    component.setMinimumSize(dims);
  }


  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    boolean lastNameOK      = false;
    boolean organizationOK  = false;
    boolean positionOK      = false;

    //if we have a role field, it must have a value...
    if (role==ASSOCIATED || role==PERSONNEL) {
      currentRole = (String)rolePickList.getSelectedItem();

      if (notNullAndNotEmpty(currentRole.trim())) {

        WidgetFactory.unhiliteComponent(roleLabel);

      } else {

        WidgetFactory.hiliteComponent(roleLabel);
        return false;
      }
    }

    String lastName = lastNameField.getText().trim();
    if (notNullAndNotEmpty(lastName)) lastNameOK = true;

    // if we have a salutation AND/OR a givenName, we *must* have a surName...
    if (   notNullAndNotEmpty(salutationField.getText().trim())
        || notNullAndNotEmpty(firstNameField.getText().trim()) ) {

      if (!lastNameOK) {

        WidgetFactory.hiliteComponent(lastNameLabel);
        warningPanel.setVisible(true);
        WidgetFactory.hiliteComponent(warningLabel);
        return false;
      }
    }

    String organization = organizationField.getText().trim();
    if (notNullAndNotEmpty(organization)) organizationOK = true;

    String positionName = positionNameField.getText().trim();
    if (notNullAndNotEmpty(positionName)) positionOK = true;

    if (lastNameOK || organizationOK || positionOK) {

      WidgetFactory.unhiliteComponent(lastNameLabel);
      WidgetFactory.unhiliteComponent(organizationLabel);
      WidgetFactory.unhiliteComponent(positionNameLabel);
      return true;

    } else {

      WidgetFactory.hiliteComponent(lastNameLabel);
      WidgetFactory.hiliteComponent(organizationLabel);
      WidgetFactory.hiliteComponent(positionNameLabel);
      warningPanel.setVisible(true);
      WidgetFactory.hiliteComponent(warningLabel);
      return false;
    }
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onRewindAction() {
  }

  /**
   *  The action to be executed when the page is loaded
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onLoadAction() {
  }

  /**
   *  gets the salutationField for this wizard page
   *
   *  @return   the salutationField String for this wizard page
   */
  public String getsalutationFieldText() { return this.salutationField.getText();}

  /**
   *  gets the firstNameField for this wizard page
   *
   *  @return   the firstNameField String for this wizard page
   */
  public String getfirstNameFieldText() { return this.firstNameField.getText();}

  /**
   *  gets the lastNameField for this wizard page
   *
   *  @return   the lastNameField String for this wizard page
   */
  public String getlastNameFieldText() { return this.lastNameField.getText();}

  /**
   *  gets the urlField for this wizard page
   *
   *  @return  the urlField String for this wizard page
   */
  public String geturlFieldText() { return this.urlField.getText();}

  /**
   *  gets the positionNameField for this wizard page
   *
   *  @return   the positionNameField String for this wizard page
   */
  public String getpositionNameFieldText() { return this.positionNameField.getText();}

  /**
   *  gets the cityField for this wizard page
   *
   *  @return   the cityField String for this wizard page
   */
  public String getcityFieldText() { return this.cityField.getText();}

  /**
   *  gets the faxField for this wizard page
   *
   *  @return   the faxField String for this wizard page
   */
  public String getfaxFieldText() { return this.faxField.getText();}

  /**
   *  gets the zipField for this wizard page
   *
   *  @return   the zipField String for this wizard page
   */
  public String getzipFieldText() { return this.zipField.getText();}

  /**
   *  gets the stateField for this wizard page
   *
   *  @return   the stateField String for this wizard page
   */
  public String getstateFieldText() { return this.stateField.getText();}

  /**
   *  gets the emailField for this wizard page
   *
   *  @return   the emailField String for this wizard page
   */
  public String getemailFieldText() { return this.emailField.getText();}

  /**
   *  gets the organizationFieldText() for this wizard page
   *
   *  @return   the organizationField String for this wizard page
   */
  public String getorganizationFieldText() { return this.organizationField.getText();}

  /**
   *  gets the countryField for this wizard page
   *
   *  @return   the countryField String for this wizard page
   */
  public String getcountryFieldText() { return this.countryField.getText();}

  /**
   *  gets the phoneField for this wizard page
   *
   *  @return   the phoneField String for this wizard page
   */
  public String getphoneFieldText() { return this.phoneField.getText();}

  /**
   *  gets the address1Field for this wizard page
   *
   *  @return   the address1Field String for this wizard page
   */
  public String getaddress1FieldText() { return this.address1Field.getText();}

  /**
   *  gets the address2Field for this wizard page
   *
   *  @return   the address2Field String for this wizard page
   */
  public String getaddress2FieldText() { return this.address2Field.getText();}

  /**
   *  gets the Page ID for this wizard page
   *
   *  @return   the Page ID String for this wizard page
   */
  public String getPageID() { return this.pageID;}

  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() { return title; }

  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() { return subtitle; }

  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() { return this.nextPageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  /**
   *  @return a List contaiing 3 String elements - one for each column of the
   *  3-col list in which this surrogate is displayed
   *
   */
  public List getSurrogate() {

    List surrogate = new ArrayList();

    //party (first column) surrogate:
    StringBuffer partyBuff = new StringBuffer();

    String salutation   = salutationField.getText().trim();
    if (notNullAndNotEmpty(salutation)) {
      partyBuff.append(salutation);
      partyBuff.append(" ");
    }

    String firstName    = firstNameField.getText().trim();
    if (notNullAndNotEmpty(firstName)) {
      partyBuff.append(firstName);
      partyBuff.append(" ");
    }

    String lastName     = lastNameField.getText().trim();
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
    if (role==ASSOCIATED || role==PERSONNEL) {
      surrogate.add(roleString + " ("+currentRole.trim()+")");
    } else {
      surrogate.add(roleString);
    }

    //address (third column) surrogate:
    StringBuffer addressBuff = new StringBuffer();

    String address1   = address1Field.getText().trim();
    if (notNullAndNotEmpty(address1)) {
      addressBuff.append(address1);
    }

    String address2   = address2Field.getText().trim();
    if (notNullAndNotEmpty(address2)) {
      appendCommaIfNeeded(addressBuff);
      addressBuff.append(address2);
    }

    String city   = cityField.getText().trim();
    if (notNullAndNotEmpty(city)) {
      appendCommaIfNeeded(addressBuff);
      addressBuff.append(city);
    }

    String state   = stateField.getText().trim();
    if (notNullAndNotEmpty(state)) {
      appendCommaIfNeeded(addressBuff);
      addressBuff.append(state);
    }

    String zip   = zipField.getText().trim();
    if (notNullAndNotEmpty(zip)) {
      appendCommaIfNeeded(addressBuff);
      addressBuff.append(zip);
    }

    String country   = countryField.getText().trim();
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
    if ( lastIndex > -1
          && !(     (buff.charAt(lastIndex)==' ')
                &&  (buff.charAt(lastIndex-1)==',') ) ) {

      buff.append(", ");
    }
  }

  // returns true if string is not null and not empty.
  // NOTE - assumes string has already been trimmed
  // of leading & trailing whitespace
  private boolean notNullAndNotEmpty(String arg) {
    return (arg!=null && !(arg.equals("")));
  }

  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
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
    return getPageData(xPathRoot);
  }

  public OrderedMap getPageData(String xPathRoot) {

    returnMap.clear();
    String nextText = null;

    nextText = salutationField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/individualName/salutation", nextText);
    }

    nextText = firstNameField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/individualName/givenName", nextText);
    }

    nextText = lastNameField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/individualName/surName", nextText);
    }

    nextText = organizationField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/organizationName", nextText);
    }

    nextText = positionNameField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/positionName", nextText);
    }

    nextText = address1Field.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/address/deliveryPoint[1]", nextText);
    }

    nextText = address2Field.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/address/deliveryPoint[2]", nextText);
    }

    nextText = cityField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/address/city", nextText);
    }

    nextText = stateField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/address/administrativeArea", nextText);
    }

    nextText = zipField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/address/postalCode", nextText);
    }

    nextText = countryField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/address/country", nextText);
    }

    nextText = phoneField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/phone[1]", nextText);
      returnMap.put(xPathRoot + "/phone[1]/@phonetype", "voice");
    }

    nextText = faxField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/phone[2]", nextText);
      returnMap.put(xPathRoot + "/phone[2]/@phonetype", "fax");
    }

    nextText = emailField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/electronicMailAddress", nextText);
    }

    nextText = urlField.getText().trim();
    if (notNullAndNotEmpty(nextText)) {
      returnMap.put(xPathRoot + "/onlineUrl", nextText);
    }

    if (role==ASSOCIATED||role==PERSONNEL) {
      nextText = currentRole.trim();
      if (notNullAndNotEmpty(nextText)) {
        returnMap.put(xPathRoot + "/role", nextText);
      }
    }
    return returnMap;
  }

  public void setPageData(OrderedMap data) {}
}
