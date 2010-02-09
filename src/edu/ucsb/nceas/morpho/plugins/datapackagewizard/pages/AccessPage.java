/**
 *  '$RCSfile: AccessPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.28 $'
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.ProgressBarThread;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.utilities.OrderedMap;

public class AccessPage
    extends AbstractUIPage {

  private final String pageID = DataPackageWizardInterface.ACCESS_PAGE;
  private final String pageNumber = "";
  private final String title = "Access Page";
  private final String subtitle = "";
  private final String EMPTY_STRING = "";

  protected JTree accessTree;
  private JPanel bottomPanel;
  private JPanel topPanel;
  private JPanel middlePanel;
  protected JTextField dnField;
  private JButton refreshButton;
  private JLabel warnLabel;
  private JLabel introLabel;
  private JLabel accessDesc1, accessDesc2;
  private String userAccessType = new String("  Allow");
  protected String userAccess = new String("Read");
  protected JComboBox typeComboBox;
  protected JComboBox accessComboBox;
  private JScrollPane accessTreePane;
  protected AccessProgressThread pbt = null;
  private String accessListFilePath = "./lib/accesslist.xml";
  public JTreeTable treeTable = null;
  private QueryMetacatThread queryMetacat = null;
  private boolean queryMetacatCancelled;
  private String userDN = null;
  private String userName = null;
  private String userEmail = null;
  private String userOrg = null;
  private boolean alreadyGeneratedfromDocumentationMenu = false;

  private ConfigXML config;
  private Vector orgList;

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
  private String xPathRoot = "/eml:eml/access";

  public void setQueryMetacatCancelled(boolean queryMetacatCancelled) {
    this.queryMetacatCancelled = queryMetacatCancelled;
  }

  public AccessPage() {
	nextPageID = "";
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
    topPanel.add(WidgetFactory.makeHalfSpacer());
    topPanel.setBorder(new javax.swing.border.EmptyBorder(
        0, 4 * WizardSettings.PADDING, 0, 0));
    JLabel desc = WidgetFactory.makeHTMLLabel(
        "<font size=\"4\"><b>Define Access:</b></font>", 1);
    topPanel.add(desc);
    topPanel.add(WidgetFactory.makeHalfSpacer());
    introLabel = WidgetFactory.makeHTMLLabel(
        "<b>Select a user or group from the list below:</b>", 1);
    topPanel.add(introLabel);
    this.add(topPanel, BorderLayout.NORTH);
    ///////////////////////////////////////////////////////


    // Define the middle panel which has the  accessTree ....
    middlePanel = new JPanel();
    middlePanel.setLayout(new BorderLayout());

    middlePanel.setBorder(new javax.swing.border.EmptyBorder(
        5 * WizardSettings.PADDING, 6 * WizardSettings.PADDING,
        4 * WizardSettings.PADDING, 5 * WizardSettings.PADDING));
    this.add(middlePanel, BorderLayout.CENTER);

    /// Define bottom panel
    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

    bottomPanel.add(WidgetFactory.makeHalfSpacer());
    JPanel accessDefinitionPanel = new JPanel();
    accessDefinitionPanel.setLayout(new BorderLayout());

    JLabel accessDefinitionLabel = WidgetFactory.makeHTMLLabel(
        "<b>&nbsp;Description of access levels:</b>"
        + "<ul><li>Read: Able to view data package.</li>"
        + "<li>Read & Write: Able to view and modify data package.</li>"
        + "<li>Read, Write & Change Permissions: Able to view and modify "
        + "datapackage, and modify access permissions.</li>"
        + "<li>All: Able to do everything (this is the same as Read, Write "
        + "& Change Permissions)</li></ul>", 5);

    warnLabel = WidgetFactory.makeLabel(EMPTY_STRING, true);

    accessDefinitionPanel.add(accessDefinitionLabel, BorderLayout.CENTER);
    accessDefinitionPanel.add(warnLabel, BorderLayout.SOUTH);
    bottomPanel.add(accessDefinitionPanel);
    bottomPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        4 * WizardSettings.PADDING,
        3 * WizardSettings.PADDING, 8 * WizardSettings.PADDING));

    this.add(bottomPanel, BorderLayout.SOUTH);

    if (Access.accessTreeNode != null &&
        Access.accessTreeMetacatServerName.compareTo(Morpho.thisStaticInstance.
        getMetacatURLString()) == 0) {

      displayTree(Access.accessTreeNode);
    }

    config = Morpho.getConfiguration();
    accessListFilePath = ConfigXML.getConfigDirectory() + "/" + Morpho.ACCESS_FILE_NAME;
    orgList = config.get("organization");
  }

  /**
   * Generates Access.accessTreeNode ... the algorithm followed is the following:
   * 0. show a progress bar with text at the bottom showing which step
   *    is being performed and a cancel button .. if cancel button is pressed,
   *    thing on step 4 are performed....
   * 1. try to read accessListFilePath and find if there is an entry for current
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

  public void generateAccessTree(boolean force) {
    Document doc = getDocumentFromFile();
    if (doc == null || force) {
      pbt = new AccessProgressThread(this);
      pbt.start();

      pbt.setCustomCancelAction(
          new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          Log.debug(45, "\nAccessPage: CustomCancelAction called");
          cancelGetDocumentFromMetacat();
        }
      });

      getDocumentFromMetacat();
    } else {
      DefaultMutableTreeNode treeNode = getTreeFromDocument(doc);
      displayTree(treeNode);
    }
  }

  protected Document getDocumentFromFile() {

    ConfigXML accessXML = null;

    try {
      accessXML = new ConfigXML(accessListFilePath);

      Document doc = accessXML.getDocument();
      NodeList nl = doc.getElementsByTagName("server");
      if (nl.getLength() < 1) {
        Log.debug(45, "No server nodes found in " + accessListFilePath);
        return null;
      }

      Node cn = null;
      Node serverNode = null;

      for (int i = 0; i < nl.getLength(); i++) {
        cn = nl.item(i).getFirstChild(); // assume 1st child is text node
        if ( (cn != null) && (cn.getNodeType() == Node.TEXT_NODE) &&
            cn.getNodeValue().compareTo(
            Morpho.thisStaticInstance.getMetacatURLString()) == 0) {
          serverNode = cn;
          continue;
        }
      }

      if (serverNode == null) {
        Log.debug(45,
            "No server nodes found with current metacat server name " +
            "found in " + accessListFilePath);
        return null;
      }

      serverNode = serverNode.getParentNode().getParentNode();

      Node deepClone = serverNode.cloneNode(true);
      DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
      Document tempDoc = impl.createDocument(EMPTY_STRING, "principals", null);
      Node importedClone = tempDoc.importNode(deepClone, true);
      Node tempRoot = tempDoc.getDocumentElement();
      tempRoot.appendChild(importedClone);

      return tempDoc;
    }
    catch (FileNotFoundException e) {
      Log.debug(10, accessListFilePath + " not found");
      Log.debug(45, "Exception in AccessPage class in getDocumentfromFile(). "
          + "Exception:" + e.getClass());
      Log.debug(45, e.getMessage());

      // creating accessListFilePath....
      try {
        String xmlSource =
            "<?xml version=\"1.0\"?>\n<accesslist></accesslist>\n";
        byte buf[] = xmlSource.getBytes();
        OutputStream f1 = new FileOutputStream(accessListFilePath);
        f1.write(buf);
        f1.close();
      }
      catch (Exception e1) {
        Log.debug(10, "Unable to create " + accessListFilePath);
        Log.debug(45,
            "Exception in AccessPage class in getDocumentfromFile(). "
            + "Exception:" + e1.getClass());
        Log.debug(45, e1.getMessage());
      }

      return null;
    }
    catch (Exception e) {
      Log.debug(45, "Exception in AccessPage class in getDocumentfromFile(). "
          + "Exception:" + e.getClass());
      Log.debug(45, e.getMessage());
      return null;
    }
  }

  protected void getDocumentFromMetacat() {
    if (pbt != null) {
      pbt.setProgressBarString(
          "Contacting Metacat Server for Access information....");
    }

    queryMetacatCancelled = false;
    queryMetacat = new QueryMetacatThread(this);
    queryMetacat.start();

    return;
  }

  private void insertDocInAccessList(Document doc) {

    ConfigXML accessXML = null;
    boolean fileExists = false;

    try {
      accessXML = new ConfigXML(accessListFilePath);
      fileExists = true;
    }
    catch (FileNotFoundException e) {
      Log.debug(10, accessListFilePath + " not found.");
      Log.debug(45, "Exception in AccessPage class in getDocumentfromFile(). "
          + "Exception:" + e.getClass());
      Log.debug(45, e.getMessage());

    }

    try {
      if (!fileExists) {
        // creating accessListFilePath....
        try {
          String xmlSource =
              "<?xml version=\"1.0\"?>\n<accesslist></accesslist>\n";
          byte buf[] = xmlSource.getBytes();
          OutputStream f1 = new FileOutputStream(accessListFilePath);
          f1.write(buf);
          f1.close();
        }
        catch (Exception e1) {
          Log.debug(10, "Unable to create " + accessListFilePath);
          Log.debug(45,
              "Exception in AccessPage class in getDocumentfromFile(). "
              + "Exception:" + e1.getClass());
          Log.debug(45, e1.getMessage());
        }

        accessXML = new ConfigXML(accessListFilePath);
      }

      Document doc1 = accessXML.getDocument();
      NodeList nl = doc1.getElementsByTagName("server");
      if (nl.getLength() < 1) {
        Log.debug(45, "No server nodes found in " + accessListFilePath
            + " Inserting new entry for current document in the document");
        insertNewEntryInAccessList(accessXML, doc);
        return;
      }

      Node cn = null;
      Node serverNode = null;

      for (int i = 0; i < nl.getLength(); i++) {
        cn = nl.item(i).getFirstChild(); // assume 1st child is text node
        if ( (cn != null) && (cn.getNodeType() == Node.TEXT_NODE) &&
            cn.getNodeValue().compareTo(
            Morpho.thisStaticInstance.getMetacatURLString()) == 0) {
          serverNode = cn;
          continue;
        }
      }

      if (serverNode == null) {
        insertNewEntryInAccessList(accessXML, doc);
      } else {
        modifyOldEntryInAccessList(accessXML, doc);
      }

    }
    catch (Exception e) {
      Log.debug(10,
          "Exception in AccessPage class in insertDocInAccessList(). "
          + "Exception:" + e.getClass());
      Log.debug(10, e.getMessage());
    }
  }

  private void insertNewEntryInAccessList(ConfigXML accessXML, Document doc) {
    Log.debug(10, "Inserting a new entry in " + accessListFilePath);

    Document doc1 = accessXML.getDocument();
    Node node = doc1.getFirstChild();

    Node result = doc1.createElement("result");
    Node server = doc1.createElement("server");
    Node serverName = doc1.createTextNode(
        Morpho.thisStaticInstance.getMetacatURLString());

    Node principalNode = doc.getDocumentElement();

    if (principalNode.getNodeName().compareTo("principals") != 0) {
      return;
    }

    Node deepClone = principalNode.cloneNode(true);
    Node principals = doc1.importNode(deepClone, true);

    server.appendChild(serverName);
    result.appendChild(server);
    result.appendChild(principals);
    node.appendChild(result);

    accessXML.save();
    
    StateChangeMonitor.getInstance().notifyStateChange(new StateChangeEvent(this, StateChangeEvent.ACCESS_LIST_MODIFIED));
  }

  private void modifyOldEntryInAccessList(ConfigXML accessXML, Document doc) {

    Log.debug(10, "Modifying an old entry in " + accessListFilePath);

    Document doc1 = accessXML.getDocument();

    Node node = doc1.getFirstChild();

    Node result = doc1.createElement("result");
    Node server = doc1.createElement("server");
    Node serverName = doc1.createTextNode(
        Morpho.thisStaticInstance.getMetacatURLString());

    Node principalNode = doc.getDocumentElement();

    if (principalNode.getNodeName().compareTo("principals") != 0) {
      return;
    }

    NodeList nl = doc1.getElementsByTagName("server");

    for (int count = 0; count < nl.getLength(); count++) {
      Node tempNode = nl.item(count);
      String value = tempNode.getFirstChild().getNodeValue();
      if (value != null && value.compareTo(
          Morpho.thisStaticInstance.getMetacatURLString()) == 0) {
        Node listNode = tempNode.getParentNode();
        listNode.getParentNode().removeChild(listNode);
      }
    }

    Node deepClone = principalNode.cloneNode(true);
    Node principals = doc1.importNode(deepClone, true);

    server.appendChild(serverName);
    result.appendChild(server);
    result.appendChild(principals);
    node.appendChild(result);

    accessXML.save();
    
    StateChangeMonitor.getInstance().notifyStateChange(new StateChangeEvent(this, StateChangeEvent.ACCESS_LIST_MODIFIED));

  }

  protected void parseInputStream(InputStream queryResult) {
    pbt.setProgressBarString(
        "Creating Access tree from information received....");

    try {
      DocumentBuilder parser = Morpho.createDomParser();
      Document doc = parser.parse(queryResult);

      DefaultMutableTreeNode treeNode = getTreeFromDocument(doc);
      insertDocInAccessList(doc);
      displayTree(treeNode);
    }
    catch (Exception e) {
      Log.debug(10, "Unable to parse the reply from Metacat server.");
      Log.debug(10, "Exception in AccessPage class in parseInputStream()."
          + "Exception: " + e.getClass());
      Log.debug(10, e.getMessage());
      //// File is not on harddisk and data is not avaiable from
      //// display a dn field to be entered by user...
      if (Access.accessTreeNode != null &&
          Access.accessTreeMetacatServerName.compareTo(Morpho.
          thisStaticInstance.
          getMetacatURLString()) == 0) {
        Log.debug(10,
            "Retrieving access information from Metacat server failed. "
            + "Displaying the old access information.");
        displayTree(Access.accessTreeNode);
      } else {
        displayDNPanel();
      }
    }

    pbt.exitProgressBarThread();

    // save doc to the file

  }

  protected void displayDNPanel() {
    JPanel panel = null;

    panel = WidgetFactory.makePanel(1);
    JLabel dnLabel = WidgetFactory.makeLabel("Distinguished Name", false);
    panel.add(dnLabel);
    dnField = WidgetFactory.makeOneLineTextField();
    dnField.setBackground(java.awt.Color.white);
    if (userDN != null) {
      dnField.setText(userDN);
    }
    panel.add(dnField);
    panel.setBorder(new javax.swing.border.EmptyBorder(
        0, WizardSettings.PADDING,
        0, 4 * WizardSettings.PADDING));

    middlePanel.add(panel, BorderLayout.CENTER);
    middlePanel.add(getAccessControlPanel(true, "Retrieve the user list ..."),
        BorderLayout.SOUTH);
    introLabel.setText("Specify a Distinguished Name in text field below:");
    middlePanel.revalidate();
    middlePanel.repaint();

    typeComboBox.setEnabled(true);
    accessComboBox.setEnabled(true);

  }

  private JPanel getAccessControlPanel(boolean withRefreshLink,
      String refreshString) {

    accessDesc1 = WidgetFactory.makeLabel(" selected user(s)", false);
    accessDesc2 = WidgetFactory.makeLabel("   access", false);

    // define item listener for allow-deny list....
    ItemListener accessTypeListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Log.debug(45, "got itemStateChanged command in access type list");

        if (e.getItem().toString().compareTo(accessTypeText[0]) == 0) {
          userAccessType = "  Allow";
          accessIsAllow = true;
        } else if (e.getItem().toString().compareTo(accessTypeText[1]) == 0) {
          userAccessType = "  Deny ";
          accessIsAllow = false;
        }
      }
    };

    typeComboBox = WidgetFactory.makePickList(accessTypeText, false,
        0, accessTypeListener);
    typeComboBox.setEnabled(false);
    if (userAccessType.compareTo("  Deny ") == 0) {
      typeComboBox.setSelectedIndex(1);
    }

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
    if (userAccess.compareTo("Read & Write") == 0) {
      accessComboBox.setSelectedIndex(1);
    } else if (userAccess.compareTo("Read, Write & Change Permissions") == 0) {
      accessComboBox.setSelectedIndex(2);
    } else if (userAccess.compareTo("All") == 0) {
      accessComboBox.setSelectedIndex(3);
    }

    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
    controlPanel.setBorder(new javax.swing.border.EmptyBorder(5 *
        WizardSettings.PADDING, 0, 0, 0));

    controlPanel.add(typeComboBox);
    controlPanel.add(accessDesc1);
    controlPanel.add(accessComboBox);
    controlPanel.add(accessDesc2);

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(WidgetFactory.makeHalfSpacer(), BorderLayout.NORTH);

    if (withRefreshLink) {
      final AccessPage accessP = this;
      GUIAction refreshListAction
          = new GUIAction(refreshString, null,
          new Command() {

        public void execute(ActionEvent ae) {
          Log.debug(45, "got action performed command from Referesh button");

          refreshButton.setEnabled(false);
          typeComboBox.setEnabled(false);
          accessComboBox.setEnabled(false);

          middlePanel.removeAll();
          middlePanel.revalidate();
          middlePanel.repaint();

          pbt = new AccessProgressThread(accessP);
          pbt.start();
          pbt.setCustomCancelAction(
              new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
              Log.debug(45, "\nAccess: CustomAddAction called");
              cancelGetDocumentFromMetacat();
            }
          });
          getDocumentFromMetacat();
        }
      });

      /// define and add refresh tree button....
      refreshButton = new HyperlinkButton(refreshListAction);

      panel.add(refreshButton, BorderLayout.EAST);
    }
    panel.add(controlPanel, BorderLayout.SOUTH);
    return panel;
  }

  protected void cancelGetDocumentFromMetacat() {

    if (Access.accessTreeNode != null &&
        Access.accessTreeMetacatServerName.compareTo(Morpho.
        thisStaticInstance.getMetacatURLString()) == 0) {
      Log.debug(10,
          "Retrieving access information from Metacat server cancelled. "
          + "Using the old access tree.");
      displayTree(Access.accessTreeNode);
    } else {
      displayDNPanel();
    }

    setQueryMetacatCancelled(true);
  }

  /**
   * Checks if treenode is present - if present, creates a
   * ScrollPane and sends back the scrollpane... otherwise sends back
   * null.
   */

  protected void displayTree(DefaultMutableTreeNode treeNode) {
    accessTreePane = null;

    // clear out any other components on the screen...
    dnField = null;
    middlePanel.removeAll();

    if (treeNode != null) {
      treeTable = new JTreeTable(new AccessTreeModel(treeNode));
      treeTable.getTree().setCellRenderer(new AccessTreeCellRenderer());

      accessTreePane = new JScrollPane(treeTable);
      accessTreePane.setPreferredSize(new java.awt.Dimension(500, 500));

      if (userDN != null) {

        int rowCount = treeTable.getRowCount();
        for (int count = rowCount; count > 0; count--) {
          treeTable.expandIt(count - 1);
        }

        boolean dnFound = false;

        rowCount = treeTable.getRowCount();
        for (int count = 0; count < rowCount && !dnFound; count++) {
          Object o = treeTable.getValueAt(count, 0);
          if (o instanceof AccessTreeNodeObject) {
            AccessTreeNodeObject nodeOb = (AccessTreeNodeObject) o;
            if ( (nodeOb.nodeType == WizardSettings.ACCESS_PAGE_GROUP ||
                nodeOb.nodeType == WizardSettings.ACCESS_PAGE_USER) &&
                nodeOb.getDN().compareTo(userDN) == 0) {
              treeTable.setRowSelectionInterval(count, count);
              treeTable.scrollRectToVisible(treeTable.getCellRect( ( (count - 5 >
                  0) ? count - 5 : 0), 0, true));
              dnFound = true;
            }
          }
        }
        if (!dnFound) {
          displayDNPanel();
          return;
        }
      }
    }

    if (accessTreePane != null) {
      middlePanel.add(accessTreePane, BorderLayout.CENTER);
      middlePanel.add(getAccessControlPanel(true, "Refresh the user list..."),
          BorderLayout.SOUTH);
    } else {
      displayDNPanel();

      return;
    }

    middlePanel.revalidate();
    middlePanel.repaint();

    typeComboBox.setEnabled(true);
    accessComboBox.setEnabled(true);
  }

  protected DefaultMutableTreeNode getTreeFromDocument(Document doc) {
    DefaultMutableTreeNode treeNode = null;

    DefaultMutableTreeNode topNode =
        new DefaultMutableTreeNode("Access Tree                        ");
    NodeList nl = null;

    if (doc != null) {
      nl = doc.getElementsByTagName("authSystem");

      if (nl != null) {
        createSubTree(nl, topNode);
      }
      treeNode = topNode;
    }

    if (treeNode != null) {
      Access.accessTreeNode = treeNode;
      Access.accessTreeMetacatServerName = Morpho.thisStaticInstance.
          getMetacatURLString();
    } else {
      Log.debug(1, "Unable to retrieve access tree. "
          + "The old list will be displayed again");
    }

    return Access.accessTreeNode;
  }

  DefaultMutableTreeNode createSubTree(NodeList nl,
      DefaultMutableTreeNode top) {
    Node tempNode;
    AccessTreeNodeObject nodeObject = null;
    DefaultMutableTreeNode tempTreeNode = null;
    ArrayList userList = new ArrayList();
    String org = null;

    for (int count = 0; count < nl.getLength(); count++) {
      tempNode = nl.item(count);
      boolean done = false;

      while (!done) {
        if (tempNode.getNodeName().compareTo("authSystem") == 0) {
          nodeObject = new AccessTreeNodeObject(
              tempNode.getAttributes().getNamedItem("URI").getNodeValue(),
              WizardSettings.ACCESS_PAGE_AUTHSYS);

          if (tempNode.getAttributes().getNamedItem("organization") != null) {
            org = tempNode.getAttributes().getNamedItem("organization").getNodeValue();
            nodeObject.setOrganization(org);

            // check if the organization name exsists in the list of orgs in
            // the config.xml....
            if(!orgList.contains(org)){
              config.insert("organization",org);
            }
          }

          tempTreeNode = new DefaultMutableTreeNode();
          tempTreeNode.setUserObject(nodeObject);

          tempTreeNode = createSubTree(tempNode.getChildNodes(), tempTreeNode);

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
              groupNodeObject.setDescription(node.getFirstChild().
                  getNodeValue());
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
                } else if (node1.getNodeName().compareTo("organization") == 0) {
                  if (node1.getFirstChild().getNodeValue().compareTo("null") !=
                      0) {
                    nodeObject.setOrganization(node1.getFirstChild().
                        getNodeValue());
                  } else {
                    String value = nodeObject.getDN();
                    if (value != null && value.indexOf("o=") > -1) {
                      value = value.substring(value.indexOf("o=") + 2);
                      value = value.substring(0, value.indexOf(","));
                    } else {
                      value = EMPTY_STRING;
                    }
                    nodeObject.setOrganization(value);
                  }
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
            } else if (node1.getNodeName().compareTo("organization") == 0) {
              if (node1.getFirstChild().getNodeValue().compareTo("null") != 0) {
                nodeObject.setOrganization(node1.getFirstChild().
                    getNodeValue());
              } else {
                String value = nodeObject.getDN();
                if (value != null && value.indexOf("o=") > -1) {
                  value = value.substring(value.indexOf("o=") + 2);
                  if (value.indexOf(",") > -1) {
                    value = value.substring(0, value.indexOf(","));
                  }
                } else {
                  value = EMPTY_STRING;
                }
                nodeObject.setOrganization(value);
              }
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

  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
    if (dnField == null) {

      int[] i = treeTable.getSelectedRows();
      if (i.length == 0) {
        warnLabel.setText("Warning: Invalid input. Please make a selection.");
        return false;
      }
      for (int j = 0; j < i.length; j++) {
        Object o = treeTable.getValueAt(i[j], 0);
        if (o instanceof AccessTreeNodeObject) {
          AccessTreeNodeObject nodeOb = (AccessTreeNodeObject) o;
          if (nodeOb.nodeType == WizardSettings.ACCESS_PAGE_GROUP ||
              nodeOb.nodeType == WizardSettings.ACCESS_PAGE_USER) {
            warnLabel.setText(EMPTY_STRING);

            if (userDN != null) {
              alreadyGeneratedfromDocumentationMenu = true;
              userDN = nodeOb.getDN();
            }

            return true;
          } else {
            warnLabel.setText(
                "Warning: Invalid input. Please select a user or a group.");
            return false;
          }
        }
      }
    } else {
      if (dnField.getText().trim().compareTo(EMPTY_STRING) != 0) {
        warnLabel.setText(EMPTY_STRING);
        if (userDN != null) {
          alreadyGeneratedfromDocumentationMenu = true;
          userDN = dnField.getText().trim();
        }
        return true;
      } else {
        warnLabel.setText(
            "Warning: Distinguished Name field can not be empty.");
      }
    }
    return false;
  }

  /**
   *  @return a List contaiing 2 String elements - one for each column of the
   *  2-col list in which this surrogate is displayed
   *
   */

  public List getSurrogate() {

    List surrogate = new ArrayList();

    // Get the value of the DN
    if (dnField == null) {
      if (treeTable != null) {
        int[] i = treeTable.getSelectedRows();
        for (int j = 0; j < i.length; j++) {
          Object o = treeTable.getValueAt(i[j], 0);
          if (o instanceof AccessTreeNodeObject) {
            AccessTreeNodeObject nodeOb = (AccessTreeNodeObject) o;
            if (nodeOb.nodeType == WizardSettings.ACCESS_PAGE_GROUP) {
              List sub_surrogate = new ArrayList();
              sub_surrogate.add(" " + nodeOb.toString().trim());

              String value = nodeOb.getDN();
              if (value != null && value.indexOf("o=") > 0) {
                value = value.substring(value.indexOf("o=") + 2);
                value = value.substring(0, value.indexOf(","));
              } else {
                value = EMPTY_STRING;
              }
              sub_surrogate.add(" " + value);
              if (nodeOb.getDescription() != null &&
                  nodeOb.getDescription().compareTo(EMPTY_STRING) != 0) {
                sub_surrogate.add(" " + nodeOb.getDescription().trim());
              } else {
                sub_surrogate.add(EMPTY_STRING);
              }
              // Get access given to the user
              sub_surrogate.add(" " + userAccessType + "   " +
                  userAccess.trim());
              surrogate.add(sub_surrogate);
            } else if (nodeOb.nodeType == WizardSettings.ACCESS_PAGE_USER) {
              List sub_surrogate = new ArrayList();
              sub_surrogate.add(" " + nodeOb.toString().trim());
              if (nodeOb.getOrganization() != null &&
                  nodeOb.getOrganization().compareTo(EMPTY_STRING) != 0) {
                sub_surrogate.add(" " + nodeOb.getOrganization().trim());
              } else {
                sub_surrogate.add(EMPTY_STRING);
              }
              if (nodeOb.getEmail() != null &&
                  nodeOb.getEmail().compareTo(EMPTY_STRING) != 0) {
                sub_surrogate.add(" " + nodeOb.getEmail().trim());
              } else {
                sub_surrogate.add(EMPTY_STRING);
              }
              // Get access given to the user
              sub_surrogate.add(" " + userAccessType + "   " +
                  userAccess.trim());
              surrogate.add(sub_surrogate);
            }
          }
        }
      }
    } else {
      List sub_surrogate = new ArrayList();

      if (userName == null) {
        String userOrg = dnField.getText().trim();
        if (userOrg != null && userOrg.indexOf("o=") > 0) {
          userOrg = userOrg.substring(userOrg.indexOf("o=") + 2);
          userOrg = userOrg.substring(0, userOrg.indexOf(","));
        } else {
          userOrg = EMPTY_STRING;
        }
        sub_surrogate.add(" " + dnField.getText().trim());
        sub_surrogate.add(" " + userOrg);
        sub_surrogate.add(" ");
      } else {
        sub_surrogate.add(userName);
        sub_surrogate.add(" " + userOrg);
        sub_surrogate.add(userEmail);
      }
      // Get access given to the user
      sub_surrogate.add(" " + userAccessType + "   " + userAccess.trim());
      surrogate.add(sub_surrogate);
    }

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

    if (dnField == null) {
      int[] i = treeTable.getSelectedRows();
      for (int j = 0; j < i.length; j++) {
        Object o = treeTable.getValueAt(i[j], 0);
        if (o instanceof AccessTreeNodeObject) {
          AccessTreeNodeObject nodeOb = (AccessTreeNodeObject) o;
          returnMap.put(xPathRoot + "/principal[" + (j + 1) + "]",
              nodeOb.getDN());
        }
      }
    } else {
      returnMap.put(xPathRoot + "/principal", dnField.getText().trim());
    }

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
    if (userDN != null && !alreadyGeneratedfromDocumentationMenu) {
      // only do this if
      if (Access.accessTreeNode == null ||
          Access.accessTreeMetacatServerName.compareTo(Morpho.
          thisStaticInstance.
          getMetacatURLString()) != 0) {
        /**
         * accessTreePane is null... so we have to generate Access.accessTreeNode
         */
        generateAccessTree(false);
      } else if (Access.accessTreeNode != null &&
          Access.accessTreeMetacatServerName.compareTo(Morpho.
          thisStaticInstance.
          getMetacatURLString()) == 0) {

        displayTree(Access.accessTreeNode);
      }
    } else if (Access.accessTreeNode == null ||
          Access.accessTreeMetacatServerName.compareTo(Morpho.
          thisStaticInstance.
          getMetacatURLString()) != 0) {
      generateAccessTree(false);
    }
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

  public boolean isQueryMetacatCancelled() {
    return queryMetacatCancelled;
  }

  public boolean setPageData(OrderedMap map, String xPathRoot) {
    Log.debug(45,
        "AccessPage.setPageData() called with xPathRoot = " + xPathRoot
        + "\n Map = \n" + map);

    if (xPathRoot != null && xPathRoot.trim().length() > 0) {
      this.xPathRoot = xPathRoot;
    }

    int access = 0;

    if (xPathRoot.indexOf("allow") > -1) {
      userAccessType = "  Allow";
    } else {
      userAccessType = "  Deny ";
    }

    List toDeleteList = new ArrayList();
    Iterator keyIt = map.keySet().iterator();
    Object nextXPathObj = null;
    String nextXPath = null;
    Object nextValObj = null;
    String nextVal = null;

    while (keyIt.hasNext()) {

      nextXPathObj = keyIt.next();
      if (nextXPathObj == null) {
        continue;
      }
      nextXPath = (String) nextXPathObj;

      nextValObj = map.get(nextXPathObj);
      nextVal = (nextValObj == null) ? "" : ( (String) nextValObj).trim();

      Log.debug(45, "Access:  nextXPath = " + nextXPath
          + "\n nextVal   = " + nextVal);

      // remove everything up to and including the last occurrence of
      // this.xPathRoot to get relative xpaths, in case we're handling a
      // project elsewhere in the tree...
      nextXPath = nextXPath.substring(nextXPath.lastIndexOf(this.xPathRoot)
          + this.xPathRoot.length() + 1);

      Log.debug(45, "Access: TRIMMED nextXPath   = " + nextXPath);

      if (nextXPath.startsWith("permission") ||
          nextXPath.startsWith("/permission")) {
        if (nextVal.compareTo("read") == 0) {
          access = access | 1;
        } else if (nextVal.compareTo("write") == 0) {
          access = access | 2;
        } else if (nextVal.compareTo("changePermission") == 0) {
          access = access | 4;
        } else if (nextVal.compareTo("all") == 0) {
          access = access | 8;
        } else {
          Log.debug(20, "Unknown access type received in setPageData() in " +
              "AccessPage.java");
        }
        toDeleteList.add(nextXPathObj);
      } else if (nextXPath.startsWith("principal") ||
          nextXPath.startsWith("/principal")) {
        if (userDN == null) {
          userDN = (String) nextValObj;
          ConfigXML accessXML = null;
          try {
            accessXML = new ConfigXML(accessListFilePath);

            Vector username = accessXML.getValuesForPath("username[.='" +
                (String) nextValObj + "']/../name");
            if (username.size() > 0) {
              userName = (String) username.get(0);
            }
            Vector useremail = accessXML.getValuesForPath("username[.='" +
                (String) nextValObj + "']/../email");
            if (useremail.size() > 0) {
              userEmail = (String) useremail.get(0);
            }
            Vector userorg = accessXML.getValuesForPath("username[.='" +
                (String) nextValObj + "']/../organization");
            if (userorg.size() > 0) {
              userOrg = (String) userorg.get(0);
            }

          }
          catch (Exception e) {

          }

        } else {
          Log.debug(10, "AccessPage.setPageData returning FALSE! Principal "
              + "contains multiple DNs. Multiple are not "
              + "supported in Access Wizard screen yet.");
          return false;
        }
        toDeleteList.add(nextXPathObj);
      }
    }

    Log.debug(45, "Access type found to be" + access);
    if (access == 3) {
      userAccess = "Read & Write";
    } else if (access == 7) {
      userAccess = "Read, Write & Change Permissions";
    } else if (access == 8) {
      userAccess = "All";
    }

    //remove entries we have used from map:
    Iterator dlIt = toDeleteList.iterator();
    while (dlIt.hasNext()) {
      map.remove(dlIt.next());
    }

    //if anything left in map, then it included stuff we can't handle...
    boolean returnVal = map.isEmpty();

    if (!returnVal) {
      Log.debug(10,
          "AccessPage.setPageData returning FALSE! Map still contains:"
          + map);
    }

    displayDNPanel();
    return returnVal;
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

        accessPage.dnField.setText( ( (AccessTreeNodeObject) node.
            getUserObject()).
            getDN());

        accessPage.typeComboBox.setEnabled(true);
        accessPage.accessComboBox.setEnabled(true);
      } else if ( ( (AccessTreeNodeObject) node.getUserObject()).nodeType ==
          WizardSettings.ACCESS_PAGE_USER) {

        accessPage.dnField.setText( ( (AccessTreeNodeObject) node.
            getUserObject()).
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


    // get the ModalDialog which parent of accessPage shown...
    // the JDialog will be tied to this Dialog
    Component parentDialog = accessPage.getParent();
    while ( (parentDialog != null) && !(parentDialog instanceof ModalDialog)) {
      parentDialog = parentDialog.getParent();
    }
    this.setParentDialog( (JDialog) parentDialog);

    // progress bar will be showing soon ... the access tree node can
    // now be contacted
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
      if (morpho.getNetworkStatus()) {
        queryResult = morpho.getMetacatInputStream(prop);
      }
      if (!accessPage.isQueryMetacatCancelled()) {
        accessPage.parseInputStream(queryResult);
      }
    }
    catch (Exception w) {
      Log.debug(10, "Error in retrieving User list from Metacat server.");
      Log.debug(45, w.getMessage());

      if (Access.accessTreeNode != null &&
          Access.accessTreeMetacatServerName.compareTo(Morpho.
          thisStaticInstance.getMetacatURLString()) == 0) {
        Log.debug(10,
            "Retrieving access information from Metacat server failed. "
            + "Using the old access tree.");
        accessPage.displayTree(Access.accessTreeNode);
      } else {
        accessPage.displayDNPanel();
      }
      //// File is not on harddisk and data is not avaiable from
      //// display a dn field to be entered by user...
    }
  }

}

/**
 *
 * Code for debugging dom trees made in between
 * processing of xml files and doms
 *
 * try {
 * // Prepare the DOM document for writing
 * Source source = new DOMSource(tempDoc);
 *
 * // Prepare the output file
 * File file = new File("./lib/ls.xml");
 * Result result = new StreamResult(file);
 *
 * // Write the DOM document to the file
 * Transformer xformer = TransformerFactory.newInstance().newTransformer();
 * xformer.transform(source, result);
 *
 *}
 *catch (TransformerConfigurationException e) {
 *}
 *catch (TransformerException e) {
 *}
 *
 */
