/**
 *  '$RCSfile: Access.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-24 02:14:18 $'
 * '$Revision: 1.23 $'
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

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;

public class Access
    extends AbstractUIPage {

  public final String pageID = DataPackageWizardInterface.ACCESS;
  public final String nextPageID = DataPackageWizardInterface.SUMMARY;
  public final String pageNumber = "13";

  //////////////////////////////////////////////////////////

  public final String title = "Access Information";
  public final String subtitle = " ";

  private JPanel radioPanel;
  private final String xPathRoot = "/eml:eml/dataset/access/";

  private boolean publicReadAccess = true;
  private final String[] buttonsText = new String[] {
      "Yes, give read-only access to public",
      "No. Don't give read-only access to public"
  };

  private final String[] colNames = {
      "User", "Permissions"};
  private final Object[] editors = null;
  private CustomList accessList;

  private InputStream queryResult;
  private Document doc;
  private Node tempNode;
  private AccessTreeNodeObject nodeObject = null;
  private DefaultMutableTreeNode tempTreeNode = null;

  public static DefaultMutableTreeNode accessTreeNode = null;

  public Access() {
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    Box vBox = Box.createVerticalBox();
    vBox.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
        "<p><b>Allow read access to public for your dataset?</b> By default, "
        + "read-only access is given to the public. Do you want to give read "
        + "access to the public?</p>", 3);

    vBox.add(desc);

    ActionListener listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        Log.debug(45, "got radiobutton command: " + e.getActionCommand());
        if (e.getActionCommand().equals(buttonsText[0])) {
          publicReadAccess = true;
        }
        if (e.getActionCommand().equals(buttonsText[1])) {
          publicReadAccess = false;
        }
      }
    };

    radioPanel = WidgetFactory.makeRadioPanel(buttonsText, 0, listener);
    radioPanel.setBorder(new javax.swing.border.EmptyBorder(0, WizardSettings.
        PADDING, 0, 0));

    vBox.add(radioPanel);
    vBox.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc1 = WidgetFactory.makeHTMLLabel(
        "<p><b>Specify access rights for other people?</b> You "
        +
        "can specify access for other members of your team or any other person. "
        + "Use the table below to add, edit and "
        + "delete access rights to your datapackage.</p>", 3);
    vBox.add(desc1);

    accessList = WidgetFactory.makeList(colNames, editors, 4,
                                        true, true, false, true, true, true);
    accessList.setBorder(new EmptyBorder(0, WizardSettings.PADDING,
                                         WizardSettings.PADDING,
                                         2 * WizardSettings.PADDING));

    vBox.add(accessList);
    vBox.add(WidgetFactory.makeDefaultSpacer());

    this.add(vBox);

    initActions();
  }

  /**
   *  Custom actions to be initialized for list buttons
   */
  private void initActions() {

    final Access access = this;

    accessList.setCustomAddAction(

        new AbstractAction() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nAccess: CustomAddAction called");
        if (accessTreeNode == null) {
          accessList.setEnabled(false);
          Component parent = SwingUtilities.getRoot( (Component) accessList);
          if (parent != null && parent.isShowing()) {
            parent.setCursor(
                Cursor.getPredefinedCursor(
                Cursor.WAIT_CURSOR));
          }
          try {
            ContactMetacat contactMetacat = new ContactMetacat(access);
            contactMetacat.setMethodCall(contactMetacat.ADD);
            contactMetacat.start();
          }
          catch (Exception e1) {
            Log.debug(10, "Could not connect to Metacat server...");
            Log.debug(45, e1.toString());
          }
        }
        else {
          showNewAccessDialog();
        }
      }
    });

    accessList.setCustomEditAction(
        new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (accessTreeNode == null) {
          Component parent = SwingUtilities.getRoot( (Component) accessList);
          if (parent != null && parent.isShowing()) {
            parent.setCursor(
                Cursor.getPredefinedCursor(
                Cursor.WAIT_CURSOR));
          }
          try {
            ContactMetacat contactMetacat = new ContactMetacat(access);
            contactMetacat.setMethodCall(contactMetacat.EDIT);
            contactMetacat.start();
          }
          catch (Exception e1) {
            Log.debug(10, "Could not connect to Metacat server...");
            Log.debug(45, e1.toString());
          }
        }
        else {
          Log.debug(45, "\nAccess: CustomEditAction called");
          showEditAccessDialog();
        }
      }
    });
  }

  protected void showRefreshTree(AccessPage aPage) {
    accessTreeNode = createTree();
    aPage.refreshTree();
  }

  protected void showNewAccessDialog() {

    if (accessTreeNode == null) {
      accessList.setEnabled(true);
      Component parent = SwingUtilities.getRoot( (Component) accessList);
      if (parent != null) {
        parent.setCursor(
            Cursor.getPredefinedCursor(
            Cursor.DEFAULT_CURSOR));
      }
      accessTreeNode = createTree();
    }

    AccessPage accessPage = (AccessPage) WizardPageLibrary.getPage(
        DataPackageWizardInterface.ACCESS_PAGE);
    ModalDialog wpd = new ModalDialog(accessPage,
                                      WizardContainerFrame.getDialogParent(),
                                      UISettings.POPUPDIALOG_WIDTH,
                                      UISettings.POPUPDIALOG_HEIGHT, false);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

      List newRow = accessPage.getSurrogate();
      newRow.add(accessPage);
      accessList.addRow(newRow);
    }
  }

  protected void showEditAccessDialog() {

    if (accessTreeNode == null) {
      accessList.setEnabled(true);

      Component parent = SwingUtilities.getRoot( (Component) accessList);
      if (parent != null) {
        parent.setCursor(
            Cursor.getPredefinedCursor(
            Cursor.DEFAULT_CURSOR));
      }
      accessTreeNode = createTree();
    }

    List selRowList = accessList.getSelectedRowList();

    if (selRowList == null || selRowList.size() < 3) {
      return;
    }

    Object dialogObj = selRowList.get(2);

    if (dialogObj == null || ! (dialogObj instanceof AccessPage)) {
      return;
    }
    AccessPage editAccessPage = (AccessPage) dialogObj;

    ModalDialog wpd = new ModalDialog(editAccessPage,
                                      WizardContainerFrame.getDialogParent(),
                                      UISettings.POPUPDIALOG_WIDTH,
                                      UISettings.POPUPDIALOG_HEIGHT, false);
    wpd.resetBounds();
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

      List newRow = editAccessPage.getSurrogate();
      newRow.add(editAccessPage);
      accessList.replaceSelectedRow(newRow);
    }
  }

  private DefaultMutableTreeNode createTree() {

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

  protected void setQueryResult(InputStream queryResult) {
    this.queryResult = queryResult;
  }

  DefaultMutableTreeNode makeTree(NodeList nl, DefaultMutableTreeNode top) {
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
        }
        else if (tempNode.getNodeName().compareTo("group") == 0) {
          DefaultMutableTreeNode tempUserNode = null;

          NodeList nl2 = tempNode.getChildNodes();
          nodeObject = null;
          AccessTreeNodeObject groupNodeObject = new AccessTreeNodeObject(
              WizardSettings.ACCESS_PAGE_GROUP);
          tempTreeNode = new DefaultMutableTreeNode();

          for (int i = 0; i < nl2.getLength(); i++) {
            Node node = nl2.item(i);
            if (node.getNodeName().compareTo("groupname") == 0) {
              groupNodeObject.setDN(node.getFirstChild().getNodeValue());
            }
            else if (node.getNodeName().compareTo("description") == 0) {
              groupNodeObject.setDescription(node.getFirstChild().getNodeValue());
            }
            else if (node.getNodeName().compareTo("user") == 0) {
              NodeList nl3 = node.getChildNodes();
              nodeObject = new AccessTreeNodeObject(
                  WizardSettings.ACCESS_PAGE_USER);

              for (int j = 0; j < nl3.getLength(); j++) {
                Node node1 = nl3.item(j);
                if (node1.getNodeName().compareTo("username") == 0) {
                  nodeObject.setDN(node1.getFirstChild().getNodeValue());
                }
                else if (node1.getNodeName().compareTo("name") == 0) {
                  nodeObject.setName(node1.getFirstChild().getNodeValue());
                }
                else if (node1.getNodeName().compareTo("email") == 0) {
                  nodeObject.setEmail(node1.getFirstChild().getNodeValue());
                }
              }
              tempUserNode = new DefaultMutableTreeNode();
              tempUserNode.setUserObject(nodeObject);
              tempTreeNode.add(tempUserNode);
            }
            tempTreeNode.setUserObject(groupNodeObject);
          }

          top.add(tempTreeNode);
          done = true;
        }
        else if (tempNode.getNodeName().compareTo("user") == 0) {
          NodeList nl2 = tempNode.getChildNodes();
          nodeObject = null;

          nodeObject = new AccessTreeNodeObject(
              WizardSettings.ACCESS_PAGE_USER);

          for (int j = 0; j < nl2.getLength(); j++) {
            Node node1 = nl2.item(j);
            if (node1.getNodeName().compareTo("username") == 0) {
              nodeObject.setDN(node1.getFirstChild().getNodeValue());
            }
            else if (node1.getNodeName().compareTo("name") == 0) {
              nodeObject.setName(node1.getFirstChild().getNodeValue());
            }
            else if (node1.getNodeName().compareTo("email") == 0) {
              nodeObject.setEmail(node1.getFirstChild().getNodeValue());
            }
          }

          tempTreeNode = new DefaultMutableTreeNode();
          tempTreeNode.setUserObject(nodeObject);

          top.add(tempTreeNode);
          done = true;
        }
        else if (tempNode.hasChildNodes()) {
          tempNode = tempNode.getFirstChild();
        }
        else {
          done = true;
        }
      }
    }
    return top;
  }

  public static void refreshTree(AccessPage accessPage) {
    Access access = new Access();

    ContactMetacat contactMetacat = new ContactMetacat(access, accessPage);
    contactMetacat.setMethodCall(contactMetacat.REFRESH);
    contactMetacat.start();

  }

  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {
  }

  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty, but if so, must
   *  return true
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
    return true;
  }

  /**
   *  gets the Map object that contains all the access/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            access/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData() {

    returnMap.clear();

    int allowIndex = 1;
    int denyIndex = 1;
    Object nextRowObj = null;
    List nextRowList = null;
    Object nextUserObject = null;
    OrderedMap nextNVPMap = null;
    AccessPage nextAccessPage = null;

    if (publicReadAccess) {
      returnMap.put(xPathRoot + "@authSystem", "knb");
      returnMap.put(xPathRoot + "@order", "denyFirst");
      returnMap.put(xPathRoot + "allow[" + (allowIndex) + "]/principal",
                    "public");
      returnMap.put(xPathRoot + "allow[" + (allowIndex++) + "]/permission",
                    "read");
    }

    List rowLists = accessList.getListOfRowLists();

    if (rowLists != null && rowLists.isEmpty()) {
      return returnMap;
    }

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      nextRowObj = it.next();
      if (nextRowObj == null) {
        continue;
      }

      nextRowList = (List) nextRowObj;
      //column 2 is user object - check it exists and isn't null:
      if (nextRowList.size() < 3) {
        continue;
      }
      nextUserObject = nextRowList.get(2);
      if (nextUserObject == null) {
        continue;
      }

      nextAccessPage = (AccessPage) nextUserObject;

      if (nextAccessPage.accessIsAllow) {
        nextNVPMap = nextAccessPage.getPageData(xPathRoot + "allow[" +
                                                (allowIndex++) + "]");
      }
      else {
        nextNVPMap = nextAccessPage.getPageData(xPathRoot + "deny[" +
                                                (denyIndex++) + "]");
      }
      returnMap.putAll(nextNVPMap);
    }

    return returnMap;
  }

  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular wizard page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular wizard page
   */
  public OrderedMap getPageData(String rootXPath) {

    throw new UnsupportedOperationException(
        "getPageData(String rootXPath) Method Not Implemented");
  }

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() {
    return pageID;
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
    return nextPageID;
  }

  /**
   *  Returns the serial number of the page
   *
   *  @return the serial number of the page
   */
  public String getPageNumber() {
    return pageNumber;
  }

    public boolean setPageData(OrderedMap data, String xPathRoot) { return false; }

}

class ContactMetacat
    extends Thread {

  Runnable runnable;
  Access access;
  AccessPage accessPage;
  InputStream queryResult;
  public int ADD = 1;
  public int EDIT = 2;
  public int REFRESH = 4;

  int methodCallID;

  public ContactMetacat(Access access) {
    this.access = access;
    this.accessPage = null;
  }

  public ContactMetacat(Access access, AccessPage accessPage) {
    this.access = access;
    this.accessPage = accessPage;
  }

  public void run() {
    Properties prop = new Properties();
    prop.put("action", "getprincipals");

    Morpho morpho = Morpho.thisStaticInstance;
    try {
      queryResult = null;
//      if (morpho.isConnected()) {
        queryResult = morpho.getMetacatInputStream(prop);
  //    }
      access.setQueryResult(queryResult);

      if (methodCallID == ADD) {
        access.showNewAccessDialog();
      }
      else if (methodCallID == EDIT) {
        access.showEditAccessDialog();
      }
      else if (methodCallID == REFRESH) {
        access.showRefreshTree(accessPage);
      }
    }
    catch (Exception w) {
      Log.debug(10, "Error in retrieving User list from Metacat server.");
      Log.debug(45, w.getMessage());
    }
  }

  /**
   * setMethodCall
   *
   * @param i int
   */
  public void setMethodCall(int methodCallID) {
    this.methodCallID = methodCallID;
  }
}
