/**
 *  '$RCSfile: PartyMainPage.java,v $'
 *    Purpose: A class for Party MainPage Screen
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.45 $'
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
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.util.Iterator;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.w3c.dom.Node;
import java.util.ArrayList;

public class PartyMainPage
    extends AbstractUIPage {

  private String pageID;
  //private String nextPageID;
  private String pageNumber;
  private String subtitle;
  private String description;
  private String xPathRoot;
  private String DATAPACKAGE_PARTY_GENERIC_NAME;
  private String DATAPACKAGE_PARTY_REL_XPATH;

  private final String[] colNames = {
      "Party", "Role", "Address"};
  private final Object[] editors = null; //makes non-directly-editable
  public final String title =
      "People or Organizations Associated With This Data Package";
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
      DATAPACKAGE_PARTY_REL_XPATH = "/creator[";
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
      DATAPACKAGE_PARTY_REL_XPATH = "/contact[";
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
      DATAPACKAGE_PARTY_REL_XPATH = "/associatedParty[";
      description =
          "<p><b>Enter associated parties information</b>.  These are persons "
          + "or organizations functionally associated with the dataset. "
          + "Enter the nature of the relationship in the role field. "
          + "For example, the person who maintains the database is an "
          + "associated party with the role of 'custodian'.<br></br><p>";

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
        deleteParty( ( (CustomList) e.getSource()));
      }
    });
  }

  /**
   * A method to show new Party Page Dialog
   */
  private void showNewPartyDialog() {

	  WizardPageLibrary library = new WizardPageLibrary(null);
    PartyPage partyPage = (PartyPage) library.getPage(role);

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
      DataPackageWizardPlugin.updateDOMFromPartiesList(partiesList,
          DATAPACKAGE_PARTY_GENERIC_NAME,
          DATAPACKAGE_PARTY_GENERIC_NAME,
          role);
    }
    if (oneOrMoreRequired) {
      WidgetFactory.unhiliteComponent(minRequiredLabel);
    }
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

      nextRowList = (List) it.next();
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size() < 4) {
        continue;
      }
      nextPage = (PartyPage) nextRowList.get(3);
      if (nextPage == partyPage) {
        continue; //DFH (don't add the page that has just been added
      }
      if (nextPage == null) {
        continue;
      }
      if (nextPage.getRefID().equals(originalRefID)) {

        Node root = adp.getSubtreeAtReference(originalRefID);

        OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(root);
        Log.debug(45,
            "updateOriginalRefPartyPage() got a match with ID: "
            + originalRefID + "; map = " + map);

        if (map == null || map.isEmpty()) {
          return;
        }

        boolean checkParty = nextPage.setPageData(
            map, "/" + DATAPACKAGE_PARTY_GENERIC_NAME);
      }
    }
  }

  /**
   * A method to edit exsisting Party Page dialog
   */
  private void showEditPartyDialog() {

    List selRowList = partiesList.getSelectedRowList();

    if (selRowList == null || selRowList.size() < 4) {
      return;
    }

    Object dialogObj = selRowList.get(3);

    if (dialogObj == null || ! (dialogObj instanceof PartyPage)) {
      return;
    }

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
      DataPackageWizardPlugin.updateDOMFromPartiesList(partiesList,
          DATAPACKAGE_PARTY_GENERIC_NAME,
          DATAPACKAGE_PARTY_GENERIC_NAME,
          role);

    }
  }

  private void deleteParty(CustomList list) {

    if (list == null) {
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

      PartyPage page = (PartyPage) (deletedRows[i].get(userObjIdx));

      Node retval
          = ReferencesHandler.deleteOriginalReferenceSubtree(adp, page.getRefID());

      if (retval == null) {

        //this means that the deleteOriginalReferenceSubtree() method didn't
        //delete the subtree from the dom, so we have to do it ourselves...
        partiesList.removeRow(partiesList.getSelectedRowIndex());

        DataPackageWizardPlugin.updateDOMFromPartiesList(partiesList,
            DATAPACKAGE_PARTY_GENERIC_NAME,
            DATAPACKAGE_PARTY_GENERIC_NAME,
            role);
      }
    }
    Log.debug(45, "AFTER: adp=" + adp);

    //Do not update datapackage as we do for add/edit, because we've already
    //manipulated the DOM directly
    //updateDOMFromPartiesList();

    if (oneOrMoreRequired) {
      WidgetFactory.unhiliteComponent(minRequiredLabel);
    }
    DataPackageWizardPlugin.updatePartiesListFromDOM(partiesList,
        DATAPACKAGE_PARTY_GENERIC_NAME,
        DATAPACKAGE_PARTY_GENERIC_NAME,
        role);
  }

  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

    DataPackageWizardPlugin.updatePartiesListFromDOM(partiesList,
        DATAPACKAGE_PARTY_GENERIC_NAME,
        DATAPACKAGE_PARTY_GENERIC_NAME,
        role);
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

    //update datapackage...
    DataPackageWizardPlugin.updateDOMFromPartiesList(partiesList,
        DATAPACKAGE_PARTY_GENERIC_NAME,
        DATAPACKAGE_PARTY_GENERIC_NAME,
        role);
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

    //update datapackage...
    DataPackageWizardPlugin.updateDOMFromPartiesList(partiesList,
        DATAPACKAGE_PARTY_GENERIC_NAME,
        DATAPACKAGE_PARTY_GENERIC_NAME,
        role);

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
    return getPageData(xPathRoot);
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

    returnMap.clear();
    DataPackageWizardPlugin.updatePartiesListFromDOM(partiesList,
        DATAPACKAGE_PARTY_GENERIC_NAME,
        DATAPACKAGE_PARTY_GENERIC_NAME,
        role);

    int index = 1;
    List nextRowList = null;
    OrderedMap nextNVPMap = null;
    PartyPage nextPartyPage = null;

    for (Iterator it = partiesList.getListOfRowLists().iterator(); it.hasNext(); ) {

      nextRowList = (List) it.next();
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size() < 4) {
        continue;
      }
      nextPartyPage = (PartyPage) nextRowList.get(3);
      if (nextPartyPage == null) {
        continue;
      }

      nextNVPMap = nextPartyPage.getPageData(rootXPath + (index++) + "]");
      returnMap.putAll(nextNVPMap);
    }
    return returnMap;
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
  public boolean setPageData(OrderedMap map, String _xPathRoot) {
    if (_xPathRoot != null && _xPathRoot.trim().length() > 0) {
      this.xPathRoot = _xPathRoot;
    }

    if (map == null || map.isEmpty()) {
      partiesList.removeAllRows();
      return true;
    }

    List toDeleteList = new ArrayList();
    Iterator keyIt = map.keySet().iterator();
    Object nextXPathObj = null;
    String nextXPath = null;
    Object nextValObj = null;
    String nextVal = null;

    List partyList = new ArrayList();

    while (keyIt.hasNext()) {

      nextXPathObj = keyIt.next();
      if (nextXPathObj == null) {
        continue;
      }
      nextXPath = (String) nextXPathObj;

      nextValObj = map.get(nextXPathObj);
      nextVal = (nextValObj == null) ? "" : ( (String) nextValObj).trim();

      Log.debug(45, "Party:  nextXPath = " + nextXPath
          + "\n nextVal   = " + nextVal);

      if (nextXPath.startsWith(DATAPACKAGE_PARTY_REL_XPATH)) {

        Log.debug(45, ">>>>>>>>>> adding to partysetList: nextXPathObj="
            + nextXPathObj + "; nextValObj=" + nextValObj);
        addToPartySet(nextXPathObj, nextValObj, partyList);
        toDeleteList.add(nextXPathObj);
      }
    }

    Iterator persIt = partyList.iterator();
    Object nextStepMapObj = null;
    OrderedMap nextStepMap = null;
    int partyPredicate = 1;

    partiesList.removeAllRows();
    boolean partyRetVal = true;

    while (persIt.hasNext()) {

      nextStepMapObj = persIt.next();
      if (nextStepMapObj == null) {
        continue;
      }
      nextStepMap = (OrderedMap) nextStepMapObj;

      if (nextStepMap.isEmpty()) {
        continue;
      }
      WizardPageLibrary library = new WizardPageLibrary(null);
      PartyPage nextStep = (PartyPage) library.getPage(role);

      boolean checkMethod = nextStep.setPageData(map,
          "/" + DATAPACKAGE_PARTY_GENERIC_NAME);

      if (!checkMethod) {
        partyRetVal = false;
      }
      List newRow = nextStep.getSurrogate();
      newRow.add(nextStep);

      partiesList.addRow(newRow);
    }

    //check method return valuse...
    if (!partyRetVal) {

      Log.debug(20, "PartyMainPage.setPageData - Method sub-class returned FALSE");
    }

    //remove entries we have used from map:
    Iterator dlIt = toDeleteList.iterator();
    while (dlIt.hasNext()) {
      map.remove(dlIt.next());

      //if anything left in map, then it included stuff we can't handle...
    }
    boolean returnVal = map.isEmpty();

    if (!returnVal) {

      Log.debug(20, "PartyMainPage.setPageData returning FALSE! Map still contains:"
          + map);
    }
    return (returnVal && partyRetVal);
  }

  private void addToPartySet(Object nextPersonnelXPathObj,
      Object nextPersonnelVal, List partyList) {

    if (nextPersonnelXPathObj == null) {
      return;
    }
    String nextPersonnelXPath = (String) nextPersonnelXPathObj;
    int predicate = getFirstPredicate(nextPersonnelXPath,
        DATAPACKAGE_PARTY_REL_XPATH);

    // NOTE predicate is 1-relative, but List indices are 0-relative!!!
    if (predicate >= partyList.size()) {
      for (int i = partyList.size(); i <= predicate; i++) {
        partyList.add(new OrderedMap());
      }
    }

    if (predicate < partyList.size()) {
      Object nextMapObj = partyList.get(predicate);
      OrderedMap nextMap = (OrderedMap) nextMapObj;
      nextMap.put(nextPersonnelXPathObj, nextPersonnelVal);
    } else {
      Log.debug(15,
          "**** ERROR - PartyMainPage.addPartySet() - predicate >"
          + " partySet.size()");
    }
  }

  private int getFirstPredicate(String xpath, String firstSegment) {

    String tempXPath
        = xpath.substring(xpath.indexOf(firstSegment) + firstSegment.length());

    return Integer.parseInt(
        tempXPath.substring(0, tempXPath.indexOf("]")));
  }

}
