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
 *     '$Date: 2004-04-06 03:16:15 $'
 * '$Revision: 1.12 $'
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
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
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
import javax.swing.JDialog;
import java.awt.Container;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import java.awt.Dimension;
import edu.ucsb.nceas.morpho.util.UISettings;
import java.awt.Rectangle;
import java.awt.Component;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import java.awt.Color;
import java.util.Properties;
import edu.ucsb.nceas.morpho.Morpho;
import java.io.InputStream;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.JComponent;

public class AccessPage
    extends AbstractUIPage {

  private final String pageID = DataPackageWizardInterface.ACCESS_PAGE;
  private final String nextPageID = "";
  private final String pageNumber = "";
  private final String title = "Access Page";
  private final String subtitle = "";
  private final String EMPTY_STRING = "";

  protected JTree accessTree;
  private JPanel bottomPanel;
  private JPanel topPanel;
  protected JPanel accessControlPanel;
  protected JPanel currentPanel;
  private JPanel middlePanel;
  private JPanel dnPanel;
  protected JTextField dnField;
  private JButton refreshButton;
  private JLabel dnLabel;
  private JLabel accessDesc1, accessDesc2;
  private String userAccessType = new String("  Allow");
  private String userAccess = new String("Read");
  protected JComboBox typeComboBox;
  protected JComboBox accessComboBox;
  protected JScrollPane accessTreePane;
  private JTextField nameField;
  private JTextField orgField;
  private JTextField descField;
  private boolean readFile = true;
  protected AccessProgressThread pbt = null;

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
  private final String xPathRoot = "/eml:eml/dataset/access";

  public AccessPage() {
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BorderLayout());

    // Defining the top most panel.....
    topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.add(WidgetFactory.makeDefaultSpacer());
    topPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        4 * WizardSettings.PADDING, 0, 0));
    JLabel desc = WidgetFactory.makeHTMLLabel(
        "<font size=\"4\"><b>Define Access:</b></font>", 1);
    topPanel.add(desc);
    topPanel.add(WidgetFactory.makeHalfSpacer());
    JLabel introLabel = WidgetFactory.makeHTMLLabel(
        "<b>Select a user or group from the list below:</b>", 1);
    topPanel.add(introLabel);

    this.add(topPanel, BorderLayout.NORTH);
    ///////////////////////////////////////////////////////


    // Define the left panel which has the  accessTree ....
    middlePanel = new JPanel();
    middlePanel.setLayout(new BorderLayout());

    accessControlPanel = refreshButtonPanel();

    if ( (accessTreePane = getAccessTreePane(Access.accessTreeNode)) != null) {
      middlePanel.add(accessTreePane, BorderLayout.CENTER);

      middlePanel.add(accessControlPanel, BorderLayout.SOUTH);
    } else {
      /// Please wait..... the code is retrieving the JTree....
      //middlePanel.add(WidgetFactory.makeLabel("Unable to retrieve access tree"
      //                                      + " from server", true,
      //                                      new java.awt.Dimension(220, 100)));
      //introLabel.setText("  Enter a full distinguished name below");
    }

    middlePanel.setBorder(new javax.swing.border.EmptyBorder(5 *
        WizardSettings.PADDING,
        6 * WizardSettings.PADDING, 4 * WizardSettings.PADDING,
        5 * WizardSettings.PADDING));
    this.add(middlePanel, BorderLayout.CENTER);

    /// Define middle panel
    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

    bottomPanel.add(WidgetFactory.makeDefaultSpacer());

    // Define the panel for entering distinguished name and
    // make it invisible by default
    dnPanel = WidgetFactory.makePanel(1);
    dnLabel = WidgetFactory.makeLabel("Distinguished Name", false);
    dnPanel.add(dnLabel);
    dnField = WidgetFactory.makeOneLineTextField();
    dnField.setBackground(java.awt.Color.white);
    dnPanel.add(dnField);
    dnPanel.setVisible(false);
    dnPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        WizardSettings.PADDING, 0,
        0));
    bottomPanel.add(dnPanel);

    JPanel accessDefinitionPanel = new JPanel();
    accessDefinitionPanel.setLayout(new BorderLayout());

    JLabel accessDefinitionLabel = WidgetFactory.makeHTMLLabel(
        "<b>&nbsp;Description of access levels:</b>"
        + "<ul><li>Read: Able to view data package.</li>"
        + "<li>Read & Write: Able to view and modify data package.</li>"
        + "<li>Read, Write & Change Permissions: Able to view and modify "
        + "datapackage, and modify access permissions.</li>"
        + "<li>All: Able to do everything.</li></ul>", 5);

    accessDefinitionPanel.add(accessDefinitionLabel, BorderLayout.CENTER);
    bottomPanel.add(accessDefinitionPanel);
    bottomPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        4 * WizardSettings.PADDING,
        3 * WizardSettings.PADDING, 8 * WizardSettings.PADDING));

    this.add(bottomPanel, BorderLayout.SOUTH);

    if (accessTreePane == null) {
      /**
       * accessTreePane is null... so we have to generate Access.accessTreeNode
       */
      pbt = new AccessProgressThread(this);
      pbt.start();
    }
  }

  private JPanel getAccessControlPanel() {

    accessDesc1 = WidgetFactory.makeLabel("   the user(users)", false);
    accessDesc2 = WidgetFactory.makeLabel("   access", false);

    // define item listener for allow-deny list....
    ItemListener accessTypeListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Log.debug(45, "got itemStateChanged command in access type list");

        if (e.getItem().toString().compareTo(accessTypeText[0]) == 0) {
          userAccessType = "  Allow";
          accessIsAllow = true;
        } else if (e.getItem().toString().compareTo(accessTypeText[1]) == 0) {
          userAccessType = "  Deny";
          accessIsAllow = false;
        }
      }
    };

    typeComboBox = WidgetFactory.makePickList(accessTypeText, false,
        0, accessTypeListener);
    typeComboBox.setEnabled(false);

    ItemListener accessListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Log.debug(45, "got itemStateChanged command in access list");

        if (e.getItem().toString().compareTo(accessText[0]) == 0) {
          userAccess = "Read";
        } else if (e.getItem().toString().compareTo(accessText[1]) == 0) {
          userAccess = "Read & Write";
        } else if (e.getItem().toString().compareTo(accessText[2]) == 0) {
          userAccess = "Read, Write & Change Permissions";
        } else if (e.getItem().toString().compareTo(accessText[3]) == 0) {
          userAccess = "All";
        }
      }
    };

    accessComboBox = WidgetFactory.makePickList(accessText, false, 0,
        accessListener);
    accessComboBox.setEnabled(false);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setBorder(new javax.swing.border.EmptyBorder(5 *
        WizardSettings.PADDING,0, 0, 0));

    panel.add(typeComboBox);
    panel.add(accessDesc1);
    panel.add(accessComboBox);
    panel.add(accessDesc2);

    return panel;
  }

  private JPanel refreshButtonPanel() {
    // Define action listener for refresh button.....

    JPanel panel = new JPanel();

    final AccessPage accessP = this;
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Log.debug(45, "got action performed command from Referesh button");

        if (accessTreePane != null) {
          middlePanel.remove(accessTreePane);
          middlePanel.revalidate();
          middlePanel.repaint();
        }
        refreshButton.setEnabled(false);
        typeComboBox.setEnabled(false);
        accessComboBox.setEnabled(false);

        Access.refreshTree(accessP);

      }
    };

    /// define and add refresh tree....
    refreshButton = WidgetFactory.makeJButton("Refresh the Access List",
        actionListener);
    setPrefMinMaxSizes(refreshButton, new Dimension(170,30));
    panel.setLayout(new BorderLayout());
    panel.add(WidgetFactory.makeHalfSpacer(), BorderLayout.NORTH);
    panel.add(refreshButton, BorderLayout.EAST);
    panel.add(getAccessControlPanel(), BorderLayout.SOUTH);
    return panel;
  }


  /**
   *  The action sets prefered Min and Max Sizes for the Components
   *
   *  @return
   */
  private void setPrefMinMaxSizes(JComponent component, Dimension dims) {
    WidgetFactory.setPrefMaxSizes(component, dims);
    component.setMinimumSize(dims);
  }

  /**
   * Generates Access.accessTreeNode ... the algorithm followed is the following:
   * 0. show a progress bar with text at the bottom showing which step
   *    is being performed and a cancel button .. if cancel button is pressed,
   *    thing on step 4 are performed....
   * 1. try to read accesslist.xml and find if there is an entry for current
   *    metacat server name...
   * 2. if there is an entry generate the dom for the <result></result> and
   *    send it domToTreeNode() funtion.
   * 3. If not, contact metacat server with action=getprincipals...
   * 4. If metacat server is not available, set appropriate text in middlePanel,
   *    show dnPanel, make other required changes in panel and
   *    close the progressbar
   * 5. If metacat server is available, get the result... store it in file..
   *    if it in the file already then delete the old entry and write
   *    the file again...
   *
   *  @return
   */
  protected void generateAccessTreeNode() {
    Log.debug(10, "Inside generate");

    pbt.setProgressBarString("Trying to retieve access tree from harddisk");

    pbt.setProgressBarString(
        "Contacting Metacat Server for Access information....");

    QueryMetacatThread cm = new QueryMetacatThread(this);
    cm.start();
  }

  protected void setTree(InputStream queryResult) {

    pbt.setProgressBarString(
        "Creating Access tree from information received....");

    DefaultMutableTreeNode treeNode = createTree(queryResult);

    Access.accessTreeNode = treeNode;

    accessTreePane = getAccessTreePane(treeNode);

    if (accessTreePane != null) {
      typeComboBox.setEnabled(true);
      accessComboBox.setEnabled(true);

      middlePanel.add(accessTreePane, BorderLayout.CENTER);
      middlePanel.add(accessControlPanel, BorderLayout.SOUTH);

      middlePanel.revalidate();
      middlePanel.repaint();
    } else {
      middlePanel.add(WidgetFactory.makeLabel("Unable to retrieve access tree"
          + " from server", true,
          new java.awt.Dimension(220, 100)));
      dnPanel.setVisible(true);

      typeComboBox.setEnabled(true);
      accessComboBox.setEnabled(true);
    }

    pbt.exitProgressBarThread();
  }

  private DefaultMutableTreeNode createTree(InputStream queryResult) {
    Document doc;

    DefaultMutableTreeNode top =
        new DefaultMutableTreeNode("Access Tree                        ");

    NodeList nl = null;

    if (queryResult != null) {
      DocumentBuilder parser = Morpho.createDomParser();

      try {
        doc = parser.parse(queryResult);
        nl = doc.getElementsByTagName("authSystem");
      }
      catch (Exception e) {
        Log.debug(10, "Exception in parsing result set from Metacat...");
        Log.debug(45, e.toString());
        return null;
      }

      if (nl != null) {
        makeTree(nl, top);
      }
      return top;
    }
    return null;
  }

  DefaultMutableTreeNode makeTree(NodeList nl, DefaultMutableTreeNode top) {
    Node tempNode;
    AccessTreeNodeObject nodeObject = null;
    DefaultMutableTreeNode tempTreeNode = null;
    ArrayList userList = new ArrayList();

    for (int count = 0; count < nl.getLength(); count++) {
      tempNode = nl.item(count);
      boolean done = false;

      while (!done) {
        if (tempNode.getNodeName().compareTo("authSystem") == 0) {
          nodeObject = new AccessTreeNodeObject(
              tempNode.getAttributes().getNamedItem("URI").getNodeValue(),
              WizardSettings.ACCESS_PAGE_AUTHSYS);

          tempTreeNode = new DefaultMutableTreeNode();
          tempTreeNode.setUserObject(nodeObject);

          tempTreeNode = makeTree(tempNode.getChildNodes(), tempTreeNode);

          top.add(tempTreeNode);
          done = true;
        } else if (tempNode.getNodeName().compareTo("group") == 0) {
          DefaultMutableTreeNode tempUserNode = null;

          NodeList nl2 = tempNode.getChildNodes();
          nodeObject = null;
          AccessTreeNodeObject groupNodeObject = new AccessTreeNodeObject(
              WizardSettings.ACCESS_PAGE_GROUP);
          tempTreeNode = new DefaultMutableTreeNode();
          ArrayList userInGroupList = new ArrayList();
          for (int i = 0; i < nl2.getLength(); i++) {
            Node node = nl2.item(i);
            if (node.getNodeName().compareTo("groupname") == 0) {
              groupNodeObject.setDN(node.getFirstChild().getNodeValue());
            } else if (node.getNodeName().compareTo("description") == 0) {
              groupNodeObject.setDescription(node.getFirstChild().getNodeValue());
            } else if (node.getNodeName().compareTo("user") == 0) {
              NodeList nl3 = node.getChildNodes();
              nodeObject = new AccessTreeNodeObject(
                  WizardSettings.ACCESS_PAGE_USER);

              for (int j = 0; j < nl3.getLength(); j++) {
                Node node1 = nl3.item(j);
                if (node1.getNodeName().compareTo("username") == 0) {
                  nodeObject.setDN(node1.getFirstChild().getNodeValue());
                } else if (node1.getNodeName().compareTo("name") == 0) {
                  nodeObject.setName(node1.getFirstChild().getNodeValue());
                } else if (node1.getNodeName().compareTo("email") == 0) {
                  nodeObject.setEmail(node1.getFirstChild().getNodeValue());
                }
              }
              userInGroupList.add(nodeObject);
            }
            tempTreeNode.setUserObject(groupNodeObject);
          }

          Collections.sort(userInGroupList);
          Iterator it = userInGroupList.iterator();

          while (it.hasNext()) {
            nodeObject = (AccessTreeNodeObject) it.next();
            tempUserNode = new DefaultMutableTreeNode();
            tempUserNode.setUserObject(nodeObject);
            tempTreeNode.add(tempUserNode);
          }
          top.add(tempTreeNode);
          done = true;
        } else if (tempNode.getNodeName().compareTo("user") == 0) {
          NodeList nl2 = tempNode.getChildNodes();
          nodeObject = null;

          nodeObject = new AccessTreeNodeObject(
              WizardSettings.ACCESS_PAGE_USER);

          for (int j = 0; j < nl2.getLength(); j++) {
            Node node1 = nl2.item(j);
            if (node1.getNodeName().compareTo("username") == 0) {
              nodeObject.setDN(node1.getFirstChild().getNodeValue());
            } else if (node1.getNodeName().compareTo("name") == 0) {
              nodeObject.setName(node1.getFirstChild().getNodeValue());
            } else if (node1.getNodeName().compareTo("email") == 0) {
              nodeObject.setEmail(node1.getFirstChild().getNodeValue());
            }
          }

          userList.add(nodeObject);
          done = true;
        } else if (tempNode.hasChildNodes()) {
          tempNode = tempNode.getFirstChild();
        } else {
          done = true;
        }
      }
    }

    Collections.sort(userList);
    Iterator it = userList.iterator();
    while (it.hasNext()) {
      nodeObject = (AccessTreeNodeObject) it.next();
      tempTreeNode = new DefaultMutableTreeNode();
      tempTreeNode.setUserObject(nodeObject);
      top.add(tempTreeNode);
    }

    return top;
  }

  public void refreshTree() {
    accessTreePane = getAccessTreePane(Access.accessTreeNode);
    refreshButton.setEnabled(true);

    if (accessTreePane != null) {
      if (dnField.getText().trim().compareTo("") != 0) {
        typeComboBox.setEnabled(true);
        accessComboBox.setEnabled(true);
      }

      middlePanel.add(accessTreePane, BorderLayout.CENTER);
      middlePanel.add(accessControlPanel, BorderLayout.SOUTH);
      middlePanel.revalidate();
      middlePanel.repaint();
    } else {
      middlePanel.add(WidgetFactory.makeLabel("Unable to retrieve access tree"
          + " from server", true,
          new java.awt.Dimension(220, 100)));
      dnPanel.setVisible(true);

      typeComboBox.setEnabled(true);
      accessComboBox.setEnabled(true);
    }
  }

  /**
   * Checks if Access.accessTreeNode is present - if present, creates a
   * ScrollPane and sends back the scrollpane... otherwise sends back
   * null.
   */

  protected JScrollPane getAccessTreePane(DefaultMutableTreeNode treeNode) {
    if (treeNode != null) {
      // create JTree and set selection model, cell rendered
      // and treeselection listener....
      //accessTree = new JTree(treeNode);
      //accessTree.getSelectionModel().setSelectionMode(
      //    TreeSelectionModel.SINGLE_TREE_SELECTION);
      //accessTree.setCellRenderer(new AccessTreeCellRenderer());
      //accessTree.addTreeSelectionListener(new TreeSelectionAction(this));

      // create JScrollPane and return it
      JTreeTable treeTable = new JTreeTable(new AccessTreeModel(treeNode));

      JScrollPane accessTreePane = new JScrollPane(treeTable);
      accessTreePane.setPreferredSize(new java.awt.Dimension(500, 500));

      return accessTreePane;
    }

    // no accessTreenNode found....
    return null;
  }

  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
    if (dnField.getText().trim().equals(EMPTY_STRING)) {
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

  public List getSurrogate() {

    List surrogate = new ArrayList();

    // Get the value of the DN
    if (accessTreePane != null) {
      if (nameField.getText() != null) {
        surrogate.add(" " + nameField.getText().trim());
      } else {
        surrogate.add(" ");
      }
      if (orgField.getText() != null) {
        surrogate.add(" " + orgField.getText().trim());
      } else {
        surrogate.add(" ");
      }
      if (descField.getText() != null) {
        surrogate.add(" " + descField.getText().trim());
      } else {
        surrogate.add(" ");
      }
    } else {
      surrogate.add(" " + dnField.getText().trim());
      surrogate.add(" ");
      surrogate.add(" ");
    }

    // Get access given to the user
    surrogate.add(" " + userAccessType + "   " + userAccess.trim());

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

    if (userAccess.compareTo("Read") == 0) {
      returnMap.put(xPathRoot + "/permission", "read");
    } else if (userAccess.compareTo("Read & Write") == 0) {
      returnMap.put(xPathRoot + "/permission[1]", "read");
      returnMap.put(xPathRoot + "/permission[2]", "write");
    } else if (userAccess.compareTo("Read, Write & Change Permissions") == 0) {
      returnMap.put(xPathRoot + "/permission[1]", "read");
      returnMap.put(xPathRoot + "/permission[2]", "write");
      returnMap.put(xPathRoot + "/permission[3]", "changePermission");
    } else if (userAccess.compareTo("All") == 0) {
      returnMap.put(xPathRoot + "/permission", "all");
    }
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

  public boolean setPageData(OrderedMap data, String xPathRoot) {
    return true;
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//                       TreeSelectionAction  Class
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class TreeSelectionAction
    implements TreeSelectionListener {

  AccessPage accessPage = null;

  public TreeSelectionAction(AccessPage accessPage) {
    this.accessPage = accessPage;
  }

  public void valueChanged(TreeSelectionEvent e) {

    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        accessPage.accessTree.getLastSelectedPathComponent();

    if (node != null &&
        node.getUserObject() instanceof AccessTreeNodeObject) {

      if ( ( (AccessTreeNodeObject) node.getUserObject()).nodeType ==
          WizardSettings.ACCESS_PAGE_GROUP) {

        accessPage.dnField.setText( ( (AccessTreeNodeObject) node.getUserObject()).
            getDN());

        accessPage.typeComboBox.setEnabled(true);
        accessPage.accessComboBox.setEnabled(true);
      } else if ( ( (AccessTreeNodeObject) node.getUserObject()).nodeType ==
          WizardSettings.ACCESS_PAGE_USER) {

        accessPage.dnField.setText( ( (AccessTreeNodeObject) node.getUserObject()).
            getDN());
        accessPage.typeComboBox.setEnabled(true);
        accessPage.accessComboBox.setEnabled(true);
      } else {
        accessPage.dnField.setText("");
        accessPage.typeComboBox.setEnabled(false);
        accessPage.accessComboBox.setEnabled(false);
      }

    } else {
      accessPage.dnField.setText("");
    }
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//                     AccessProgressThread Class
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class AccessProgressThread
    extends ProgressBarThread {

  private AccessPage accessPage = null;

  // Constructor accessPage
  public AccessProgressThread(AccessPage accessPage) {
    super();
    this.accessPage = accessPage;
  }

  public void run() {

    // wait for accessPage to show....
    while (!accessPage.isShowing()) {
      ;
    }

    // get the ModalDialog which parent of accessPage shown...
    // the JDialog will be tied to this Dialog
    Component parentDialog = accessPage.getParent();
    while (! (parentDialog instanceof ModalDialog)) {
      parentDialog = parentDialog.getParent();
    }
    this.setParentDialog( (JDialog) parentDialog);

    // progress bar will be showing soon ... the access tree node can
    // now be contacted
    accessPage.generateAccessTreeNode();

    super.run();

  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//                     QueryMetacatThread  Class
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class QueryMetacatThread
    extends Thread {

  AccessPage accessPage;
  InputStream queryResult;

  public QueryMetacatThread(AccessPage accessPage) {
    this.accessPage = accessPage;
  }

  public void run() {
    Properties prop = new Properties();
    prop.put("action", "getprincipals");

    Morpho morpho = Morpho.thisStaticInstance;
    try {
      queryResult = null;
      //if (morpho.isConnected()) {
      queryResult = morpho.getMetacatInputStream(prop);
      accessPage.setTree(queryResult);
      // }
    }
    catch (Exception w) {
      Log.debug(10, "Error in retrieving User list from Metacat server.");
      Log.debug(45, w.getMessage());
      w.printStackTrace();
    }
  }

}
