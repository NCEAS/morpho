/**
 *       Name: PartyPanel.java
 *    Purpose: Example dynamic editor class for XMLPanel
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-01-21 04:41:15 $'
 * '$Revision: 1.6 $'
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
package edu.ucsb.nceas.morpho.editor;

import java.util.Enumeration;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

/**
 * PartyPanel is an example of a special panel editor for
 * use with the DocFrame class. It is designed to
 *
 * @author higgins
 */
public class PartyPanel extends JPanel
{

  private static final Dimension PARTY_2COL_LABEL_DIMS = new Dimension(70,20);

  private short  role;
  private String roleString;
  private JLabel salutationLabel;
  private JLabel     roleLabel;
  private JTextField roleField;
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

  DefaultMutableTreeNode nd = null;
  DefaultMutableTreeNode nd1 = null;

  public PartyPanel(DefaultMutableTreeNode node) {
    nd = node;
    JPanel jp = this;
    jp.setLayout(new BoxLayout(jp,BoxLayout.Y_AXIS));
    jp.setAlignmentX(Component.LEFT_ALIGNMENT);
		NodeInfo info = (NodeInfo)(nd.getUserObject());
    jp.setMaximumSize(new Dimension(500,500));
    init(jp);
    jp.setVisible(true);
  }

  private void init(JPanel panel) {

    ////
    JPanel salutationPanel = WidgetFactory.makePanel(1);
    salutationLabel = WidgetFactory.makeLabel("Salutation:", false);
    salutationPanel.add(salutationLabel);
    salutationField = WidgetFactory.makeOneLineTextField();
    salutationField.setText(getValue(nd, "salutation"));
    salutationField.addFocusListener(new dfhFocus());
    salutationPanel.add(salutationField);
    panel.add(salutationPanel);

    ////
    JPanel firstNamePanel = WidgetFactory.makePanel(1);
    firstNamePanel.add(WidgetFactory.makeLabel("First Name:", false));
    firstNameField = WidgetFactory.makeOneLineTextField();
    firstNameField.setText(getValue(nd, "givenName"));
    firstNameField.addFocusListener(new dfhFocus());
    firstNamePanel.add(firstNameField);
    panel.add(firstNamePanel);

    ////
    JPanel lastNamePanel = WidgetFactory.makePanel(1);
    lastNameLabel = WidgetFactory.makeLabel("Last Name:", true);
    lastNamePanel.add(lastNameLabel);
    lastNameField = WidgetFactory.makeOneLineTextField();
    lastNameField.setText(getValue(nd, "surName"));
    lastNameField.addFocusListener(new dfhFocus());
    lastNamePanel.add(lastNameField);
    panel.add(lastNamePanel);

    ////
    JPanel organizationPanel = WidgetFactory.makePanel(1);
    organizationLabel = WidgetFactory.makeLabel("Organization:", true);
    organizationPanel.add(organizationLabel);
    organizationField = WidgetFactory.makeOneLineTextField();
    organizationField.setText(getValue(nd, "organizationName"));
    organizationField.addFocusListener(new dfhFocus());
    organizationPanel.add(organizationField);
    panel.add(organizationPanel);

    ////
    JPanel positionNamePanel = WidgetFactory.makePanel(1);
    positionNameLabel = WidgetFactory.makeLabel("Position Name:", true);
    positionNamePanel.add(positionNameLabel);
    positionNameField = WidgetFactory.makeOneLineTextField();
    positionNameField.setText(getValue(nd, "positionName"));
    positionNameField.addFocusListener(new dfhFocus());
    positionNamePanel.add(positionNameField);
    panel.add(positionNamePanel);

    ////
    JPanel address1Panel = WidgetFactory.makePanel(1);
    address1Panel.add(WidgetFactory.makeLabel("Address 1:", false));
    address1Field = WidgetFactory.makeOneLineTextField();
    address1Field.setText(getValue(nd, "deliveryPoint"));
    address1Field.addFocusListener(new dfhFocus());
    address1Panel.add(address1Field);
    panel.add(address1Panel);

    ////
    JPanel address2Panel = WidgetFactory.makePanel(1);
    address2Panel.add(WidgetFactory.makeLabel("Address 2:", false));
    address2Field = WidgetFactory.makeOneLineTextField();
    // need to figure out how to handle multiple addresses
    address2Panel.add(address2Field);
    panel.add(address2Panel);

    ////
    JPanel cityStatePanel = WidgetFactory.makePanel(1);
    cityStatePanel.add(WidgetFactory.makeLabel("City:", false));
    cityField = WidgetFactory.makeOneLineTextField();
    cityField.setText(getValue(nd, "city"));
    cityField.addFocusListener(new dfhFocus());
    cityStatePanel.add(cityField);
    cityStatePanel.add(WidgetFactory.makeDefaultSpacer());
    JLabel stateLabel = WidgetFactory.makeLabel("State:", false);
    setPrefMinMaxSizes(stateLabel, PARTY_2COL_LABEL_DIMS);
    cityStatePanel.add(stateLabel);
    stateField = WidgetFactory.makeOneLineTextField();
    stateField.setText(getValue(nd, "administrativeArea"));
    stateField.addFocusListener(new dfhFocus());
    cityStatePanel.add(stateField);
    panel.add(cityStatePanel);

    ////
    JPanel zipCountryPanel = WidgetFactory.makePanel(1);
    zipCountryPanel.add(WidgetFactory.makeLabel("Postal Code:", false));
    zipField = WidgetFactory.makeOneLineTextField();
    zipField.setText(getValue(nd, "postalCode"));
    zipField.addFocusListener(new dfhFocus());
    zipCountryPanel.add(zipField);
    zipCountryPanel.add(WidgetFactory.makeDefaultSpacer());
    JLabel countryLabel = WidgetFactory.makeLabel("Country:", false);
    setPrefMinMaxSizes(countryLabel, PARTY_2COL_LABEL_DIMS);
    zipCountryPanel.add(countryLabel);
    countryField = WidgetFactory.makeOneLineTextField();
    countryField.setText(getValue(nd, "country"));
    countryField.addFocusListener(new dfhFocus());
    zipCountryPanel.add(countryField);
    panel.add(zipCountryPanel);


    ////
    JPanel phoneFaxPanel = WidgetFactory.makePanel(1);
    phoneFaxPanel.add(WidgetFactory.makeLabel("Phone:", false));
    phoneField = WidgetFactory.makeOneLineTextField();
    phoneField.setText(getValue(nd, "phone"));
    phoneField.addFocusListener(new dfhFocus());
    phoneFaxPanel.add(phoneField);
    phoneFaxPanel.add(WidgetFactory.makeDefaultSpacer());
    JLabel faxLabel = WidgetFactory.makeLabel("Fax:", false);
    setPrefMinMaxSizes(faxLabel, PARTY_2COL_LABEL_DIMS);
    phoneFaxPanel.add(faxLabel);
    faxField = WidgetFactory.makeOneLineTextField();
    faxField.setText(getValue(nd, "phone", "phonetype", "facsimile"));
    phoneFaxPanel.add(faxField);
    panel.add(phoneFaxPanel);


    ////
    JPanel emailUrlPanel = WidgetFactory.makePanel(1);
    emailUrlPanel.add(WidgetFactory.makeLabel("Email:", false));
    emailField = WidgetFactory.makeOneLineTextField();
    emailField.setText(getValue(nd, "electronicMailAddress"));
    emailField.addFocusListener(new dfhFocus());
    emailUrlPanel.add(emailField);
    emailUrlPanel.add(WidgetFactory.makeDefaultSpacer());
    JLabel urlLabel = WidgetFactory.makeLabel("Online URL:", false);
    setPrefMinMaxSizes(urlLabel, PARTY_2COL_LABEL_DIMS);
    emailUrlPanel.add(urlLabel);
    urlField = WidgetFactory.makeOneLineTextField();
    urlField.setText(getValue(nd, "onlineUrl"));
    urlField.addFocusListener(new dfhFocus());
    emailUrlPanel.add(urlField);
    panel.add(emailUrlPanel);
  }



  private void setPrefMinMaxSizes(JComponent component, Dimension dims) {

    WidgetFactory.setPrefMaxSizes(component, dims);
    component.setMinimumSize(dims);
  }

  /**
   *  This method searches for a descendent of the specified node
   *  with the specified name and returns the text value.
   */
  private String getValue(DefaultMutableTreeNode node, String name) {
    String ret = null;
    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
     if (nodeName.equals(name)) {
        DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
        NodeInfo tni = (NodeInfo)tnode.getUserObject();
        ret = tni.getPCValue();
        return ret;
      }
    }
    return ret;
  }

    /**
   *  This method searches for a descendent of the specified node
   *  with the specified name, and the specified attribute with the given name
   *  and returns the text value.
   */
  private String getValue(DefaultMutableTreeNode node, String name, String attrName, String attrVal) {
    String ret = null;
    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
     if (nodeName.equals(name)) {
        if ((ni.attr.containsKey(attrName))&&(((String)(ni.attr.get(attrName))).equals(attrVal))) {
          DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
          NodeInfo tni = (NodeInfo)tnode.getUserObject();
          ret = tni.getPCValue();
          return ret;
        }
      }
    }
    return ret;
  }


  /**
   *  This method searches for a descendent of the specified node
   *  with the specified name and sets the text value.
   */
  private void setValue(DefaultMutableTreeNode node, String name, String val) {
    String ret = null;
    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
     if (nodeName.equals(name)) {
        DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
        NodeInfo tni = (NodeInfo)tnode.getUserObject();
        tni.setPCValue(val);
        break;
      }
    }
  }

  /**
   *  This method searches for a descendent of the specified node
   *  with the specified name, specified attribute name & value
   *  and and sets the text value.
   */
  private void setValue(DefaultMutableTreeNode node, String name, String val, String attrName, String attrVal) {
    String ret = null;
    Enumeration enum = node.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
     if (nodeName.equals(name)) {
       if ((ni.attr.containsKey(attrName))&&(((String)(ni.attr.get(attrName))).equals(attrVal))) {
         DefaultMutableTreeNode tnode = (DefaultMutableTreeNode)nd.getFirstChild();
         NodeInfo tni = (NodeInfo)tnode.getUserObject();
         tni.setPCValue(val);
        break;
       }
      }
    }
  }


class dfhAction implements java.awt.event.ActionListener
{
  public void actionPerformed(java.awt.event.ActionEvent event)
    {
      Object object = event.getSource();
      if (object instanceof JTextArea) {
        NodeInfo info = (NodeInfo)(nd1.getUserObject());
        info.setPCValue(((JTextArea)object).getText());
      }
    }
}

class dfhFocus extends java.awt.event.FocusAdapter {
  public void focusLost(java.awt.event.FocusEvent event)
    {
      Object object = event.getSource();
      if (object == salutationField) {
        String val = salutationField.getText();
        setValue(nd, "salutation", val);
      }
       else if (object == firstNameField) {
        String val = firstNameField.getText();
        setValue(nd, "givenName", val);
      }
      else if (object == lastNameField) {
        String val = lastNameField.getText();
        setValue(nd, "surName", val);
      }
      else if (object == organizationField) {
        String val = organizationField.getText();
        setValue(nd, "organizationName", val);
      }
      else if (object == positionNameField) {
        String val = positionNameField.getText();
        setValue(nd, "positionName", val);
      }
      else if (object == address1Field) {
        String val = address1Field.getText();
        setValue(nd, "deliveryPoint", val);
      }
      else if (object == cityField) {
        String val = cityField.getText();
        setValue(nd, "city", val);
      }
      else if (object == stateField) {
        String val = stateField.getText();
        setValue(nd, "administrativeArea", val);
      }
      else if (object == zipField) {
        String val = zipField.getText();
        setValue(nd, "postalCode", val);
      }
      else if (object == countryField) {
        String val = countryField.getText();
        setValue(nd, "country", val);
      }
      else if (object == phoneField) {
        String val = phoneField.getText();
        setValue(nd, "phone", val);
      }
      else if (object == faxField) {
        String val = faxField.getText();
        setValue(nd, "phone", val);	      //NEED to handle attribute to determine voice or fax
      }
      else if (object == emailField) {
        String val = emailField.getText();
        setValue(nd, "electronicMailAddress", val);
      }
      else if (object == urlField) {
        String val = urlField.getText();
        setValue(nd, "onlineUrl", val);
      }
    }

  public void focusGained(java.awt.event.FocusEvent event)
    {
      Object object = event.getSource();
      if (object instanceof JTextArea) {
      }
    }
}

}
