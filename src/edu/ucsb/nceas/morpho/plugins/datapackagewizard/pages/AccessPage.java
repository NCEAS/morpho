/**
 *  '$RCSfile: AccessPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-03-17 04:15:11 $'
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


import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.ArrayList;
import java.util.List;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JButton;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class AccessPage extends AbstractWizardPage {

  private final String pageID     = DataPackageWizardInterface.ACCESS_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title      = "Access Page";
  private final String subtitle   = "";
  private final String EMPTY_STRING = "";


  private JPanel middlePanel;
  private JPanel topPanel;
  private JPanel selectionInfoPanel;
  private JPanel currentPanel;
  private JPanel leftPanel;
  private JTextField dnField;
  private JLabel dnLabel;
  private JLabel introLabel;
  private JLabel descLabel, accessDesc1, accessDesc2;
  private String userAccessType   = new String("  Allow");
  private String userAccess       = new String("  Read");
  private JComboBox typeComboBox;
  private JComboBox accessComboBox;
  private JScrollPane treeView;

  private final String[] accessTypeText = new String[] {
    "  Allow",
    "  Deny"
  };

  private final String[] accessText = new String[] {
    "  Read",
    "  Read & Write",
    "  Read, Write & Change Permissions",
    "  All"
  };

  public boolean accessIsAllow = true;
  private final String xPathRoot  = "/eml:eml/dataset/access";

  public AccessPage() {
          init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BorderLayout());

    topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.add(WidgetFactory.makeDefaultSpacer());
    topPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        4*WizardSettings.PADDING,0,0));
    JLabel desc = WidgetFactory.makeHTMLLabel(
                      "<font size=\"4\"><b>Define Access:</b></font>", 1);
    topPanel.add(desc);
    topPanel.add(WidgetFactory.makeHalfSpacer());
    JLabel introLabel = WidgetFactory.makeHTMLLabel(
                      "<b>Select a user or group from the list below:</b>", 1);
    topPanel.add(introLabel);

    this.add(topPanel, BorderLayout.NORTH);

    treeView = createTree();
    leftPanel = new JPanel();
    leftPanel.setLayout(new BorderLayout());

    if(treeView != null){
      leftPanel.add(treeView, BorderLayout.CENTER);

      ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Log.debug(45, "got action performed command from Referesh button");
          Access.refreshTree();

          treeView = createTree();
          if(treeView != null){
            leftPanel.remove(treeView);
            leftPanel.add(treeView, BorderLayout.CENTER);
            leftPanel.revalidate();
            leftPanel.repaint();
          }
        }
      };

      JButton refreshButton = WidgetFactory.makeJButton("Refresh",
          actionListener);
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(refreshButton);
      leftPanel.add(buttonPanel, BorderLayout.SOUTH);
    } else {
      leftPanel.add(WidgetFactory.makeLabel("Unable to retrieve access tree"
                                            +" from server", true,
                                            new java.awt.Dimension(220,100)));
      introLabel.setText("  Enter a full distinguished name below");
    }

    leftPanel.setBorder(new javax.swing.border.EmptyBorder(5*WizardSettings.PADDING,
        4*WizardSettings.PADDING,4*WizardSettings.PADDING,4*WizardSettings.PADDING));
    this.add(leftPanel, BorderLayout.WEST);


    middlePanel = new JPanel();
    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));

    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    JPanel dnPanel = WidgetFactory.makePanel(1);
    dnLabel = WidgetFactory.makeLabel("Distinguished Name", false);
    dnPanel.add(dnLabel);
    dnField = WidgetFactory.makeOneLineTextField();
    dnField.setBackground(java.awt.Color.white);
    dnPanel.add(dnField);
    dnPanel.setBorder(new javax.swing.border.EmptyBorder(0,WizardSettings.PADDING,0,
        0));

    if(treeView == null){
      middlePanel.add(dnPanel);
    }

    selectionInfoPanel = new JPanel();
    selectionInfoPanel.setLayout(new BorderLayout());
    currentPanel = WidgetFactory.makeVerticalPanel(2);
    selectionInfoPanel.add(currentPanel);
    middlePanel.add( selectionInfoPanel);

    ItemListener accessTypeListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Log.debug(45, "got itemStateChanged command in access type list");

        if (e.getItem().toString().compareTo(accessTypeText[0]) == 0) {
          userAccessType = "Allow";
          accessIsAllow = true;
        } else if (e.getItem().toString().compareTo(accessTypeText[1]) == 0) {
          userAccessType = "Deny";
          accessIsAllow = false;
        }
      }
    };

    descLabel = WidgetFactory.makeHTMLLabel(
        "<b>&nbsp;Define access control for this user/group:</b>", 1);
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(descLabel);
    middlePanel.add(WidgetFactory.makeHalfSpacer());

    typeComboBox = WidgetFactory.makePickList(accessTypeText, false,
                                    0, accessTypeListener);

    accessDesc1 = WidgetFactory.makeLabel("      this user", false);
    accessDesc2 = WidgetFactory.makeLabel("   access", false);

    ItemListener accessListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Log.debug(45, "got itemStateChanged command in access list");

        if (e.getItem().toString().compareTo(accessText[0]) == 0) {
          userAccess = "Read";
        } else if (e.getItem().toString().compareTo(accessText[2]) == 0) {
          userAccess = "Write";
        } else if (e.getItem().toString().compareTo(accessText[3]) == 0) {
          userAccess = "All";
        }
      }
    };

    accessComboBox = WidgetFactory.makePickList(accessText, false, 0,
        accessListener);

    if(treeView!=null){
      typeComboBox.setEnabled(false);
      accessComboBox.setEnabled(false);
    } else {
      typeComboBox.setEnabled(true);
      accessComboBox.setEnabled(true);
    }

    JPanel typePanel = new JPanel();

    typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.X_AXIS));
    typePanel.setBorder(new javax.swing.border.EmptyBorder(0,2*WizardSettings.PADDING,
        0,0));

    typePanel.add(typeComboBox);
    typePanel.add(accessDesc1);
    typePanel.add(accessComboBox);
    typePanel.add(accessDesc2);

    middlePanel.add(typePanel);

    JPanel accessDefinitionPanel = new JPanel();
    accessDefinitionPanel.setLayout(new BorderLayout());

    JLabel accessDefinitionLabel = WidgetFactory.makeHTMLLabel(
        "<b>&nbsp;Description of access levels:</b>"
        +"<ul><li>Read: Able to view data package.</li>"
        +"<li>Read & Write: Able to view and modify data package.</li>"
        +"<li>Read, Write & Change Permissions: Able to view and modify "
        +"datapackage, and modify access permissions.</li>"
        +"<li>All: Able to do everything.</li></ul>", 4);

    accessDefinitionPanel.add(accessDefinitionLabel, BorderLayout.CENTER);
    middlePanel.add(accessDefinitionPanel);
    middlePanel.setBorder(new javax.swing.border.EmptyBorder(0,4*WizardSettings.PADDING,
        3*WizardSettings.PADDING,8*WizardSettings.PADDING));

    this.add(middlePanel, BorderLayout.CENTER);
  }


  private JScrollPane createTree(){
    if(Access.accessTreeNode != null){
      final JTree accessTree = new JTree(Access.accessTreeNode);

      accessTree.getSelectionModel().setSelectionMode(
                                TreeSelectionModel.SINGLE_TREE_SELECTION);

      accessTree.setCellRenderer(new AccessTreeCellRenderer());

      accessTree.addTreeSelectionListener(new TreeSelectionListener(){
        public void valueChanged(TreeSelectionEvent e) {

          DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                      accessTree.getLastSelectedPathComponent();


          if(node != null && node.getUserObject() instanceof AccessTreeNodeObject){
            selectionInfoPanel.remove(currentPanel);

            if(((AccessTreeNodeObject) node.getUserObject()).nodeType ==
               WizardSettings.ACCESS_PAGE_GROUP){

              dnField.setText( ( (AccessTreeNodeObject) node.getUserObject()).
                            getDN());
              currentPanel = getGroupInfoPanel((AccessTreeNodeObject)
                                       node.getUserObject());

              typeComboBox.setEnabled(true);
              accessComboBox.setEnabled(true);

              selectionInfoPanel.add(currentPanel, BorderLayout.CENTER);
            } else if(((AccessTreeNodeObject) node.getUserObject()).nodeType ==
                      WizardSettings.ACCESS_PAGE_USER){

              dnField.setText( ( (AccessTreeNodeObject) node.getUserObject()).
                            getDN());
              currentPanel = getUserInfoPanel((AccessTreeNodeObject)
                                       node.getUserObject());
              typeComboBox.setEnabled(true);
              accessComboBox.setEnabled(true);

              selectionInfoPanel.add(currentPanel, BorderLayout.CENTER);
             } else {
               dnField.setText("");
               typeComboBox.setEnabled(false);
               accessComboBox.setEnabled(false);
             }

            selectionInfoPanel.revalidate();
            selectionInfoPanel.repaint();

          } else {
            dnField.setText("");
          }
        }
      });
      JScrollPane treeView = new JScrollPane(accessTree);
      treeView.setPreferredSize(new java.awt.Dimension(200,200));
      return treeView;
    }

    return null;
  }

  private JPanel getGroupInfoPanel(AccessTreeNodeObject nodeObject){

    JPanel panel = WidgetFactory.makeVerticalPanel(2);

    panel.add(WidgetFactory.makeDefaultSpacer());

    JPanel desc = WidgetFactory.makePanel(1);
    JLabel descLabel = WidgetFactory.makeHTMLLabel("<b>Group Information:</b>",
                                                   1);
    desc.add(descLabel);
    panel.add(desc);
    panel.add(WidgetFactory.makeHalfSpacer());


    JPanel username = WidgetFactory.makePanel(1);
    JLabel nameLabel = WidgetFactory.makeLabel(" Name:", false);
    username.add(nameLabel);

    JTextField nameField = WidgetFactory.makeOneLineTextField();
    nameField.setEditable(false);
    nameField.setBackground(java.awt.Color.white);
    nameField.setText(" " + nodeObject.toString());
    username.add(nameField);

    panel.add(username);
    panel.add(WidgetFactory.makeHalfSpacer());

    JPanel organization = WidgetFactory.makePanel(1);
    JLabel orgLabel = WidgetFactory.makeLabel(" Organization:", false);
    organization.add(orgLabel);

    JTextField orgField = WidgetFactory.makeOneLineTextField();
    orgField.setEditable(false);
    orgField.setBackground(java.awt.Color.white);
    String value = nodeObject.getDN();
    if(value != null && value.indexOf("o=")>0){
      value = value.substring(value.indexOf("o=") + 2);
      value = value.substring(0, value.indexOf(","));
    } else {
      value = "";
    }
    orgField.setText(" " + value);
    organization.add(orgField);

    panel.add(organization);
    panel.add(WidgetFactory.makeHalfSpacer());

    JPanel groupDesc = WidgetFactory.makePanel(1);

    JLabel groupDescLabel = WidgetFactory.makeLabel(" Group Description:", false);
    groupDesc.add(groupDescLabel);

    JTextField descField = WidgetFactory.makeOneLineTextField();
    descField.setEditable(false);
    if(nodeObject.getDescription() != null)
      descField.setText(" " + nodeObject.getDescription());
    descField.setBackground(java.awt.Color.white);
    groupDesc.add(descField);

    panel.add(groupDesc);
    panel.add(WidgetFactory.makeHalfSpacer());

    panel.setBorder(new javax.swing.border.EmptyBorder(0,WizardSettings.PADDING,
        0,WizardSettings.PADDING));

    return panel;
  }


  private JPanel getUserInfoPanel(AccessTreeNodeObject nodeObject){
    JPanel panel = WidgetFactory.makeVerticalPanel(2);

    panel.add(WidgetFactory.makeDefaultSpacer());

    JPanel desc = WidgetFactory.makePanel(1);
    JLabel descLabel = WidgetFactory.makeHTMLLabel("<b>User Information:</b>",
                                                   1);
    desc.add(descLabel);
    panel.add(desc);
    panel.add(WidgetFactory.makeHalfSpacer());

    JPanel username = WidgetFactory.makePanel(1);
    JLabel nameLabel = WidgetFactory.makeLabel(" Name:", false);
    username.add(nameLabel);

    JTextField nameField = WidgetFactory.makeOneLineTextField();
    nameField.setEditable(false);
    nameField.setBackground(java.awt.Color.white);
    nameField.setText(" " + nodeObject.toString());
    username.add(nameField);

    panel.add(username);
    panel.add(WidgetFactory.makeHalfSpacer());

    JPanel organization = WidgetFactory.makePanel(1);
    JLabel orgLabel = WidgetFactory.makeLabel(" Organization:", false);
    organization.add(orgLabel);

    JTextField orgField = WidgetFactory.makeOneLineTextField();
    orgField.setEditable(false);
    orgField.setBackground(java.awt.Color.white);
    String value = nodeObject.getDN();
    if(value != null && value.indexOf("o=")>0){
      value = value.substring(value.indexOf("o=") + 2);
      value = value.substring(0, value.indexOf(","));
    } else {
      value = "";
    }
    orgField.setText(" " + value);
    organization.add(orgField);

    panel.add(organization);
    panel.add(WidgetFactory.makeHalfSpacer());

    JPanel email = WidgetFactory.makePanel(1);
    JLabel emailLabel = WidgetFactory.makeLabel(" Email:", false);
    email.add(emailLabel);

    JTextField emailField = WidgetFactory.makeOneLineTextField();
    emailField.setEditable(false);
    if(nodeObject.getEmail() != null)
      emailField.setText(" " + nodeObject.getEmail());
    emailField.setBackground(java.awt.Color.white);
    email.add(emailField);

    panel.add(email);
    panel.add(WidgetFactory.makeHalfSpacer());

    panel.setBorder(new javax.swing.border.EmptyBorder(0,WizardSettings.PADDING,
        0,WizardSettings.PADDING));
    return panel;
 }

   /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
    if(dnField.getText().trim().equals(EMPTY_STRING)){
      WidgetFactory.hiliteComponent(dnLabel);
      return false;
    }
    return true;
  }


  /**
   *  @return a List contaiing 2 String elements - one for each column of the
   *  2-col list in which this surrogate is displayed
   *
   */
  private final StringBuffer surrogateBuff = new StringBuffer();
  //
  public List getSurrogate() {

    List surrogate = new ArrayList();

    // Get the value of the DN
    surrogate.add(" " + dnField.getText().trim());

    // Get access given to the user
    surrogate.add(" " + userAccessType + "   " + userAccess);

    return surrogate;
  }


  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
   *            appended when making name/value pairs.  For example, in the
   *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
   *            root would be /eml:eml/dataset/keywordSet[2]
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

    returnMap.put(xPathRoot + "/principal", dnField.getText().trim());

    returnMap.put(xPathRoot + "/permission", userAccess.toLowerCase());

    return returnMap;
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
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  public void setPageData(OrderedMap data) {}
}
