/**
 *  '$RCSfile: PartyMainPage.java,v $'
 *    Purpose: A class for Party MainPage Screen
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-04-11 17:40:13 $'
 * '$Revision: 1.38 $'
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
import edu.ucsb.nceas.morpho.datapackage.ReferencesHandler;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DataPackageWizardPlugin;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.w3c.dom.Node;
import edu.ucsb.nceas.utilities.XMLUtilities;

public class PartyMainPage
    extends AbstractUIPage {

  private String pageID;
  private String nextPageID;
  private String pageNumber;
  private String subtitle;
  private String description;
  private String xPathRoot;
  private String DATAPACKAGE_PARTY_GENERIC_NAME;

  private final String[] colNames = { "Party", "Role", "Address" };
  private final Object[] editors = null; //makes non-directly-editable
  public final String title = "People or Organizations Associated With This Data Package";
  public final String role;

  private JLabel minRequiredLabel;
  private CustomList partiesList;
  private boolean oneOrMoreRequired;


  /**
   *  Constructor - determines what type of dialog (what role):
   *
   * @param role short - PartyPage.CREATOR, PartyPage.CREATOR,
   * PartyPage.PERSONNEL or PartyPage.ASSOCIATED
   */
  public PartyMainPage(String role) {

    this.role = role;
    initRole();
    init();
  }


  /**
   * Initiates various parameters of PartyMainPage based on value of variable
   * role.
   */
  private void initRole() {

    if (role.equals(DataPackageWizardInterface.PARTY_CREATOR)) {

      oneOrMoreRequired = true;
      pageID = DataPackageWizardInterface.PARTY_CREATOR_PAGE;
      nextPageID = DataPackageWizardInterface.PARTY_CONTACT_PAGE;
      pageNumber = "5";
      subtitle = "Owners";
      xPathRoot = "/eml:eml/dataset/creator[";
      DATAPACKAGE_PARTY_GENERIC_NAME = "creator";
      description =
          "<p><b>Enter information about the Owners</b>: This is information "
          + "about the persons or organizations certified as data owners "
          + "(e.g. the principal investigator(s) of the project). "
          + "The list of data owners should include all people and "
          + "organizations who should be cited "
          + "for the data. Select Add to add an owner."
          + "<br></br></p>";

    } else if (role.equals(DataPackageWizardInterface.PARTY_CONTACT)) {

      oneOrMoreRequired = true;
      pageID = DataPackageWizardInterface.PARTY_CONTACT_PAGE;
      nextPageID = DataPackageWizardInterface.PARTY_ASSOCIATED_PAGE;
      pageNumber = "6";
      subtitle = "Contacts";
      xPathRoot = "/eml:eml/dataset/contact[";
      DATAPACKAGE_PARTY_GENERIC_NAME = "contact";
      description =
        "<p><b>Enter information about contacts</b>. This is information "
        + "about the people or organizations who would be contacted with "
        + "questions about the use or interpretation of a data package. "
        + "<br></br></p>";

    } else if (role.equals(DataPackageWizardInterface.PARTY_ASSOCIATED)) {

      oneOrMoreRequired = false;
      pageID = DataPackageWizardInterface.PARTY_ASSOCIATED_PAGE;
      nextPageID = DataPackageWizardInterface.PROJECT;
      pageNumber = "7";
      subtitle = "Associated Parties";
      xPathRoot = "/eml:eml/dataset/associatedParty[";
      DATAPACKAGE_PARTY_GENERIC_NAME = "associatedParty";
      description =
          "<p><b>Enter associated parties information</b>.  These are persons "
          + "or organizations functionally associated with the dataset. "
          + "Enter the nature of the relationship in the role field. "
          + "For example, the person who maintains the database is an "
          + "associated party with the role of 'custodian'.<br></br><p>";

//    } else if (role.equals(DataPackageWizardInterface.PARTY_PERSONNEL)) {
//
//      oneOrMoreRequired = true;
//      subtitle = "Personnel";
//      xPathRoot = "/eml:eml/dataset/project/personnel[";
//      description =
//          "<p>b>Enter information about Personnel</b>: This is information about "
//          +
//          "the people or organizations who should be associated with the resource. These "
//          +
//          "parties might play various roles in the creation or maintenance of "
//          +
//          "the resource, and these roles should be indicated in the \"role\" "
//          + "element.<br></br></p>";

    } else {

      Log.debug(5, "Unrecognized role parameter passed to PartyPage: " + role);
      return;
    }
  }

  /**   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BorderLayout());

    JLabel desc = WidgetFactory.makeHTMLLabel(description, 4);
    this.add(desc, BorderLayout.NORTH);

    JPanel vPanel = WidgetFactory.makeVerticalPanel(6);

    if (oneOrMoreRequired) {
      minRequiredLabel = WidgetFactory.makeLabel(
          " One or more " + subtitle + " must be defined:", true,
          WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
      vPanel.add(minRequiredLabel);
    }

    partiesList = WidgetFactory.makeList(colNames, editors, 4,
                                         true, true, false, true, true, true);
    partiesList.setBorder(new EmptyBorder(0, WizardSettings.PADDING,
                                          WizardSettings.PADDING,
                                          2 * WizardSettings.PADDING));

    vPanel.add(WidgetFactory.makeDefaultSpacer());

    vPanel.add(partiesList);

    vPanel.add(WidgetFactory.makeDefaultSpacer());

    this.add(vPanel, BorderLayout.CENTER);

    initActions();
  }

  /**
   *  Custom actions to be initialized for list buttons
   */
  private void initActions() {

    partiesList.setCustomAddAction(

        new AbstractAction() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nPartyPage: CustomAddAction called");
        showNewPartyDialog();
      }
    });

    partiesList.setCustomEditAction(

        new AbstractAction() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nPartyPage: CustomEditAction called");
        showEditPartyDialog();
      }
    });

    partiesList.setCustomDeleteAction(

        new AbstractAction() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nPartyPage: CustomDeleteAction called");
        deleteParty(((CustomList)e.getSource()));
      }
    });
  }


  /**
   * A method to show new Party Page Dialog
   */
  private void showNewPartyDialog() {

    PartyPage partyPage = (PartyPage)WizardPageLibrary.getPage(role);

    ModalDialog wpd = new ModalDialog(partyPage,
                                      WizardContainerFrame.getDialogParent(),
                                      UISettings.POPUPDIALOG_WIDTH,
                                      UISettings.POPUPDIALOG_HEIGHT);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

      List newRow = partyPage.getSurrogate();
      newRow.add(partyPage);
      partiesList.addRow(newRow);


      if (partyPage.editingOriginalRef) {

        //have been editing an original reference via another party's dialog, so
        //if the original ref is in this current page's list, update its
        //PartyPage object before we write it to DOM...
        updateOriginalRefPartyPage(partyPage);
      }
     //update datapackage...
      updateDOMFromListOfPages();
    }
    if (oneOrMoreRequired)WidgetFactory.unhiliteComponent(minRequiredLabel);
  }


  private void updateDOMFromListOfPages() {

    //update datapackage...
    List nextRowList = null;
    List pagesList = new ArrayList();
    AbstractUIPage nextPage = null;

    for (Iterator it = partiesList.getListOfRowLists().iterator(); it.hasNext(); ) {

      nextRowList = (List)it.next();
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size() < 4)continue;
      nextPage = (AbstractUIPage)nextRowList.get(3);
      if (nextPage == null)continue;
      pagesList.add(nextPage);
    }
    DataPackageWizardPlugin.deleteExistingAndAddPageDataToDOM(
        UIController.getInstance().getCurrentAbstractDataPackage(),
        pagesList, DATAPACKAGE_PARTY_GENERIC_NAME,
        DATAPACKAGE_PARTY_GENERIC_NAME);


    updateListFromDOM();
  }




  //have been editing an original reference via another party's dialog, so
  //if the original ref is in this current page's list, update its
  //PartyPage object before we write it to DOM...
  private void updateOriginalRefPartyPage(PartyPage partyPage) {
    String originalRefID = partyPage.getReferencesNodeIDString();
    AbstractDataPackage adp
        = UIController.getInstance().getCurrentAbstractDataPackage();
    if (adp == null) {
      Log.debug(15, "\npackage from UIController is null");
      Log.debug(5, "ERROR: cannot update!");
      return;
    }

    List nextRowList = null;
    PartyPage nextPage = null;

    for (Iterator it = partiesList.getListOfRowLists().iterator(); it.hasNext(); ) {

      nextRowList = (List)it.next();
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size() < 4)continue;
      nextPage = (PartyPage)nextRowList.get(3);
      if (nextPage == partyPage) continue; //DFH (don't add the page that has just been added
      if (nextPage == null)continue;
      if (nextPage.getRefID().equals(originalRefID)) {

        Node root = adp.getSubtreeAtReference(originalRefID);

        OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(root);
        Log.debug(45,
                  "updateOriginalRefPartyPage() got a match with ID: "
                  + originalRefID+"; map = "+map);

        if (map == null || map.isEmpty())return;

        boolean checkParty = nextPage.setPageData(
            map, "/" + DATAPACKAGE_PARTY_GENERIC_NAME);
Log.debug(1, "HALT:" + "\n Map = \n" + map);            
      }
    }
  }



  /**
   * A method to edit exsisting Party Page dialog
   */
  private void showEditPartyDialog() {

    List selRowList = partiesList.getSelectedRowList();

    if (selRowList == null || selRowList.size() < 4) return;

    Object dialogObj = selRowList.get(3);

    if (dialogObj == null || ! (dialogObj instanceof PartyPage)) return;

    PartyPage editPartyPage = (PartyPage) dialogObj;

    ModalDialog wpd = new ModalDialog(editPartyPage,
                                      WizardContainerFrame.getDialogParent(),
                                      UISettings.POPUPDIALOG_WIDTH,
                                      UISettings.POPUPDIALOG_HEIGHT, false);
    wpd.resetBounds();
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {
      List newRow = editPartyPage.getSurrogate();
      newRow.add(editPartyPage);
      partiesList.replaceSelectedRow(newRow);


      if (editPartyPage.editingOriginalRef) {

        //have been editing an original reference via another party's dialog, so
        //if the original ref is in this current page's list, update its
        //PartyPage object before we write it to DOM...
        updateOriginalRefPartyPage(editPartyPage);
      }
      //update datapackage...
      updateDOMFromListOfPages();
    }
  }



  private void deleteParty(CustomList list) {

    if (list==null) {
      Log.debug(15, "**ERROR: deleteParty() received NULL CustomList");
      return;
    }
    AbstractDataPackage adp
        = UIController.getInstance().getCurrentAbstractDataPackage();
    if (adp == null) {
      Log.debug(15, "\npackage from UIController is null");
      Log.debug(5, "ERROR: cannot delete!");
      return;
    }
    Log.debug(45, "BEFORE: adp=" + adp);
    List[] deletedRows = list.getSelectedRows();
    int userObjIdx = deletedRows[0].size() - 1;

    for (int i = 0; i < deletedRows.length; i++) {

      PartyPage page = (PartyPage)(deletedRows[i].get(userObjIdx));

      ReferencesHandler.deleteOriginalReferenceSubtree(adp, page.getRefID());
    }
    Log.debug(45, "AFTER: adp=" + adp);

    //Do not update datapackage as we do for add/edit, because we've already
    //manipulated the DOM directly

    if (oneOrMoreRequired)WidgetFactory.unhiliteComponent(minRequiredLabel);
    updateListFromDOM();
  }


  private void updateListFromDOM() {

    AbstractDataPackage adp
        = UIController.getInstance().getCurrentAbstractDataPackage();
    if (adp == null) {
      Log.debug(15, "\npackage from UIController is null");
      Log.debug(5, "ERROR: cannot update!");
      return;
    }

    List personnelList = adp.getSubtrees(DATAPACKAGE_PARTY_GENERIC_NAME);
    Log.debug(45, "updateListFromDOM - personnelList.size() = "
              + personnelList.size());

    List personnelOrderedMapList = new ArrayList();

    for (Iterator it = personnelList.iterator(); it.hasNext(); ) {

      personnelOrderedMapList.add(
          XMLUtilities.getDOMTreeAsXPathMap((Node)it.next()));
    }

    populatePartiesList(personnelOrderedMapList,
                        "/"+DATAPACKAGE_PARTY_GENERIC_NAME + "[");
  }


  //personnelXPathRoot looks like:
  //      /contact[
  private boolean populatePartiesList(List personnelOrderedMapList,
                                      String personnelXPathRoot) {

    Iterator persIt = personnelOrderedMapList.iterator();
    OrderedMap nextPersonnelMap = null;
    int partyPredicate = 1;

    partiesList.removeAllRows();
    boolean partyRetVal = true;

    while (persIt.hasNext()) {

      nextPersonnelMap = (OrderedMap)persIt.next();
      if (nextPersonnelMap == null || nextPersonnelMap.isEmpty()) continue;

      PartyPage nextParty = (PartyPage)WizardPageLibrary.getPage(
//          DataPackageWizardInterface.PARTY_PERSONNEL);
          this.role);

      boolean checkParty = nextParty.setPageData(nextPersonnelMap,
                                                 personnelXPathRoot
                                                 + (partyPredicate++) + "]");

      if (!checkParty)partyRetVal = false;
      List newRow = nextParty.getSurrogate();
      newRow.add(nextParty);

      partiesList.addRow(newRow);
    }
    return partyRetVal;
  }



  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

    partiesList.focusAddButton();
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {

    if (oneOrMoreRequired) {
      WidgetFactory.unhiliteComponent(minRequiredLabel);
    }
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

    if (oneOrMoreRequired && partiesList.getRowCount() < 1) {

      WidgetFactory.hiliteComponent(minRequiredLabel);
      return false;
    }
    return true;
  }

  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */

  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData() {

    returnMap.clear();
    updateListFromDOM();

    int index = 1;
    List nextRowList = null;
    OrderedMap nextNVPMap = null;
    PartyPage nextPartyPage = null;

    for (Iterator it = partiesList.getListOfRowLists().iterator(); it.hasNext(); ) {

      nextRowList = (List) it.next();
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size() < 4) continue;
      nextPartyPage = (PartyPage) nextRowList.get(3);
      if (nextPartyPage == null) continue;

      nextNVPMap = nextPartyPage.getPageData(xPathRoot + (index++) + "]");
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


  /**
   * sets the OrderMap for this wizard page
   *
   * @return boolean
   * @param data OrderedMap
   * @param xPathRoot String
   */
  public boolean setPageData(OrderedMap data, String xPathRoot) { return false; }
}
