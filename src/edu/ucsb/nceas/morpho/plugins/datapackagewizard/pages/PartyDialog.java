/**
 *  '$RCSfile: PartyDialog.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-06 04:20:38 $'
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

import java.awt.BorderLayout;
import java.awt.Dimension;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.utilities.OrderedMap;

public class PartyDialog extends WizardPopupDialog {

  public static final short CREATOR    = 0;
  public static final short CONTACT    = 10;
  public static final short ASSOCIATED = 20;
  
  private static final Dimension PARTY_2COL_LABEL_DIMS = new Dimension(70,20);
  
  private short  role;
  private String roleString;
  private JTextField salutationField;
  private JTextField firstNameField;
  private JLabel     lastNameLabel;
  private JTextField lastNameField;
  private JLabel     organizationLabel;
  private JTextField organizationField;
  private JLabel     positionNameLabel;
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

  public PartyDialog(JFrame parent, short role) {
    
    super(parent);
    
    this.role = role;
    
    switch (role) {
    
      case CREATOR:
        roleString = "Creator";
        break;
      case CONTACT:
        roleString = "Contact";
        break;
      case ASSOCIATED:
        roleString = "Associated Party";
        break;
    }
    init();
    this.setVisible(true);
  }
  
  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    
    JLabel desc = WidgetFactory.makeHTMLLabel("<font size=\"4\"><b>"
                                              +roleString+" Details</b></font>", 1);
    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    middlePanel.add(desc);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    
    ////
    JPanel salutationPanel = WidgetFactory.makePanel(1);
    salutationPanel.add(WidgetFactory.makeLabel("Salutation:", false));
    salutationField = WidgetFactory.makeOneLineTextField();
    salutationPanel.add(salutationField);
    middlePanel.add(salutationPanel);
    
    ////
    JPanel firstNamePanel = WidgetFactory.makePanel(1);
    firstNamePanel.add(WidgetFactory.makeLabel("First Name:", false));
    firstNameField = WidgetFactory.makeOneLineTextField();
    firstNamePanel.add(firstNameField);
    middlePanel.add(firstNamePanel);
    
    ////
    JPanel lastNamePanel = WidgetFactory.makePanel(1);
    lastNameLabel = WidgetFactory.makeLabel("Last Name:", true);
    lastNamePanel.add(lastNameLabel);
    lastNameField = WidgetFactory.makeOneLineTextField();
    lastNamePanel.add(lastNameField);
    middlePanel.add(lastNamePanel);
    
    ////
    JPanel organizationPanel = WidgetFactory.makePanel(1);
    organizationLabel = WidgetFactory.makeLabel("Organization:", true);
    organizationPanel.add(organizationLabel);
    organizationField = WidgetFactory.makeOneLineTextField();
    organizationPanel.add(organizationField);
    middlePanel.add(organizationPanel);
    
    ////
    JPanel positionNamePanel = WidgetFactory.makePanel(1);
    positionNameLabel = WidgetFactory.makeLabel("Position Name:", true);
    positionNamePanel.add(positionNameLabel);
    positionNameField = WidgetFactory.makeOneLineTextField();
    positionNamePanel.add(positionNameField);
    middlePanel.add(positionNamePanel);
    
    ////
    JPanel address1Panel = WidgetFactory.makePanel(1);
    address1Panel.add(WidgetFactory.makeLabel("Address 1:", false));
    address1Field = WidgetFactory.makeOneLineTextField();
    address1Panel.add(address1Field);
    middlePanel.add(address1Panel);
  
    ////
    JPanel address2Panel = WidgetFactory.makePanel(1);
    address2Panel.add(WidgetFactory.makeLabel("Address 2:", false));
    address2Field = WidgetFactory.makeOneLineTextField();
    address2Panel.add(address2Field);
    middlePanel.add(address2Panel);
    
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
     
    String lastName = lastNameField.getText().trim();
    if (lastName!=null && !(lastName.equals(""))) lastNameOK = true;
  
    String organization = organizationField.getText().trim();
    if (organization!=null && !(organization.equals(""))) organizationOK = true;
  
    String positionName = positionNameField.getText().trim();
    if (positionName!=null && !(positionName.equals(""))) positionOK = true;
    
    if (lastNameOK || organizationOK || positionOK) {
    
      WidgetFactory.unhiliteComponent(lastNameLabel);
      WidgetFactory.unhiliteComponent(organizationLabel);
      WidgetFactory.unhiliteComponent(positionNameLabel);
      return true;
      
    } else {
    
      WidgetFactory.hiliteComponent(lastNameLabel);
      WidgetFactory.hiliteComponent(organizationLabel);
      WidgetFactory.hiliteComponent(positionNameLabel);
    }
    return false;
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
    
    String salutation   = salutationField.getText().trim();
    if (salutation!=null) {
      partyBuff.append(salutation);
      partyBuff.append(" ");
    }
    
    String firstName    = firstNameField.getText().trim();
    if (firstName!=null) {
      partyBuff.append(firstName);
      partyBuff.append(" ");
    }
    
    String lastName     = lastNameField.getText().trim();
    if (lastName!=null) {
      partyBuff.append(lastName);
    }
  
    String positionName = positionNameField.getText().trim();
    if (positionName!=null) {
      if (partyBuff.length()>0 && hasNoTrailingComma(partyBuff)) partyBuff.append(", ");
      partyBuff.append(positionName);
    }

    String organization = organizationField.getText().trim();
    if (organization!=null) {
      if (partyBuff.length()>0 && hasNoTrailingComma(partyBuff)) partyBuff.append(", ");
      partyBuff.append(organization);
    }
    surrogate.add(partyBuff.toString());

    
    //role (second column) surrogate:
    surrogate.add(roleString);
    

    //address (third column) surrogate:
    StringBuffer addressBuff = new StringBuffer();
    
    String address1   = address1Field.getText().trim();
    if (address1!=null) {
      addressBuff.append(address1);
    }
    
    String address2   = address1Field.getText().trim();
    if (address2!=null) {
      if (addressBuff.length()>0 && hasNoTrailingComma(addressBuff)) addressBuff.append(", ");
      addressBuff.append(address2);
    }
    
    String city   = cityField.getText().trim();
    if (city!=null) {
      if (addressBuff.length()>0 && hasNoTrailingComma(addressBuff)) addressBuff.append(", ");
      addressBuff.append(city);
    }
    
    String state   = stateField.getText().trim();
    if (state!=null) {
      if (addressBuff.length()>0 && hasNoTrailingComma(addressBuff)) addressBuff.append(", ");
      addressBuff.append(state);
    }
    
    String zip   = zipField.getText().trim();
    if (zip!=null) {
      if (addressBuff.length()>0 && hasNoTrailingComma(addressBuff)) addressBuff.append(", ");
      addressBuff.append(zip);
    }
    
    String country   = countryField.getText().trim();
    if (country!=null) {
      if (addressBuff.length()>0 && hasNoTrailingComma(addressBuff)) addressBuff.append(", ");
      addressBuff.append(country);
    }
    
    surrogate.add(addressBuff.toString());

    return surrogate;
  }

  // returns true if stringbuffer does NOT end with ", "
  private boolean hasNoTrailingComma(StringBuffer buff) {
  
    return !(buff.lastIndexOf(", ")==buff.length() - 2);
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
  public OrderedMap getPageData(String xPathRoot) {
  
    returnMap.clear();
    String nextText = null;
    
    nextText = salutationField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/individualName/salutation", nextText);
    }
    
    nextText = firstNameField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/individualName/givenName", nextText);
    }
    
    nextText = lastNameField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/individualName/surName", nextText);
    }
    
    nextText = organizationField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/organizationName", nextText);
    }
    
    nextText = positionNameField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/positionName", nextText);
    }
    
    nextText = address1Field.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/address/deliverypoint[1]", nextText);
    }
    
    nextText = address2Field.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/address/deliverypoint[2]", nextText);
    }
    
    nextText = cityField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/address/city", nextText);
    }
    
    nextText = stateField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/address/administrativeArea", nextText);
    }
    
    nextText = zipField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/address/postalCode", nextText);
    }
    
    nextText = countryField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/address/country", nextText);
    }
    
    nextText = phoneField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/phone[1]", nextText);
      returnMap.put(xPathRoot + "/phone[1]@phonetype", "voice");
    }
    
    nextText = faxField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/phone[2]", nextText);
      returnMap.put(xPathRoot + "/phone[2]@phonetype", "fax");
    }
    
    nextText = emailField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/electronicMailAddress", nextText);
    }
    
    nextText = urlField.getText().trim();
    if (nextText!=null && !(nextText.equals(""))) {
      returnMap.put(xPathRoot + "/onlineUrl", nextText);
    }
    return returnMap;
  }
  
}
