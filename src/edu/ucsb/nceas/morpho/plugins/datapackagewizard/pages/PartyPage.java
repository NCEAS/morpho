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
 *     '$Date: 2003-11-26 17:54:20 $'
 * '$Revision: 1.4 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JComboBox;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.utilities.OrderedMap;

public class PartyPage extends AbstractWizardPage {

  private final String pageID     = DataPackageWizardInterface.ATTRIBUTE_PAGE;
  private final String nextPageID = "";
  private final String title      = "Attribute Page";
  private final String subtitle   = "";

  public static final short CREATOR    = 0;
  public static final short CONTACT    = 10;
  public static final short ASSOCIATED = 20;
  public static final short PERSONNEL  = 30;

  private static final Dimension PARTY_2COL_LABEL_DIMS = new Dimension(70,20);
  private static final Dimension PARTY_FULL_LABEL_DIMS = new Dimension(800,20);

  private final String xPathRoot = "/eml:eml/dataset/creator[1]";

  private short  role;
  private String roleString;
  private JLabel     roleLabel;
  private JTextField roleField;    /*Commented out to replace field with combo list*/
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

  private JPanel middlePanel;

  private final String[] roleArray
                              = new String[]{ "",
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

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    JLabel desc = WidgetFactory.makeHTMLLabel("<font size=\"4\"><b>"
                                              +roleString+" Details</b></font>", 1);
    middlePanel = new JPanel();
    this.setLayout( new BorderLayout());
    this.add(middlePanel, BorderLayout.CENTER);

    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    middlePanel.add(desc);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    ////
    if (role == ASSOCIATED || role == PERSONNEL) {
      JPanel rolePanel = WidgetFactory.makePanel(1);
      roleLabel = WidgetFactory.makeLabel("Role:", true);
      rolePanel.add(roleLabel);
    //  roleField = WidgetFactory.makeOneLineTextField();
      rolePickList = WidgetFactory.makePickList(roleArray, false, 0,
            new ItemListener(){ public void itemStateChanged(ItemEvent e) {}});
      rolePanel.add(rolePickList);
      middlePanel.add(rolePanel);
      middlePanel.add(WidgetFactory.makeHalfSpacer());
    }

    ////
    JPanel salutationPanel = WidgetFactory.makePanel(1);
    salutationPanel.add(WidgetFactory.makeLabel("Salutation:", false));
    salutationField = WidgetFactory.makeOneLineTextField();
    salutationPanel.add(salutationField);
    middlePanel.add(salutationPanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
    JPanel firstNamePanel = WidgetFactory.makePanel(1);
    firstNamePanel.add(WidgetFactory.makeLabel("First Name:", false));
    firstNameField = WidgetFactory.makeOneLineTextField();
    firstNamePanel.add(firstNameField);
    middlePanel.add(firstNamePanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
    JPanel lastNamePanel = WidgetFactory.makePanel(1);
    lastNameLabel = WidgetFactory.makeLabel("Last Name:", true);
    lastNamePanel.add(lastNameLabel);
    lastNameField = WidgetFactory.makeOneLineTextField();
    lastNamePanel.add(lastNameField);
    middlePanel.add(lastNamePanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
    JPanel organizationPanel = WidgetFactory.makePanel(1);
    organizationLabel = WidgetFactory.makeLabel("Organization:", true);
    organizationPanel.add(organizationLabel);
    organizationField = WidgetFactory.makeOneLineTextField();
    organizationPanel.add(organizationField);
    middlePanel.add(organizationPanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
    JPanel positionNamePanel = WidgetFactory.makePanel(1);
    positionNameLabel = WidgetFactory.makeLabel("Position Name:", true);
    positionNamePanel.add(positionNameLabel);
    positionNameField = WidgetFactory.makeOneLineTextField();
    positionNamePanel.add(positionNameField);
    middlePanel.add(positionNamePanel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
    JPanel address1Panel = WidgetFactory.makePanel(1);
    address1Panel.add(WidgetFactory.makeLabel("Address 1:", false));
    address1Field = WidgetFactory.makeOneLineTextField();
    address1Panel.add(address1Field);
    middlePanel.add(address1Panel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    ////
    JPanel address2Panel = WidgetFactory.makePanel(1);
    address2Panel.add(WidgetFactory.makeLabel("Address 2:", false));
    address2Field = WidgetFactory.makeOneLineTextField();
    address2Panel.add(address2Field);
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
    middlePanel.add(emailUrlPanel);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    ////
    warningPanel = WidgetFactory.makePanel(1);
    warningLabel = WidgetFactory.makeLabel("Warning: Either one of the three "
                                             +"enteries is required: Last Name, Position Name or Organization", true);
    warningPanel.add(warningLabel);
    warningPanel.setVisible(false);
    setPrefMinMaxSizes(warningLabel, PARTY_FULL_LABEL_DIMS);
    middlePanel.add(warningPanel);

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
      currentRoleIndex = rolePickList.getSelectedIndex();
      currentRole = (String)rolePickList.getItemAt(currentRoleIndex);

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
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
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
