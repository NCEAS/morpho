/**
 *  '$RCSfile: PartyMainPage.java,v $'
 *    Purpose: A class for Party MainPage Screen
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-03-20 02:09:44 $'
 * '$Revision: 1.21 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
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
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;

public class PartyMainPage
    extends AbstractUIPage {

  public String pageID;
  public String nextPageID;
  public String pageNumber;
  public String subtitle;
  public String description;
  public String xPathRoot;

  private final String[] colNames = {
      "Party", "Role", "Address"};
  private final Object[] editors = null; //makes non-directly-editable
  public final String title = "Dataset Associated Parties";
  public final short role;

  public static int RESPONSIBLE_PARTY_REFERENCE_COUNT = 0;
  private JLabel minRequiredLabel;
  private CustomList partiesList;
  private boolean oneOrMoreRequired;

  public PartyMainPage(short role) {
    this.role = role;
    initRole();
    init();

    // A empty row is added to responsiblePartyList if none exists.
    // This is done so that the dropdown list on PartyPage has
    // no entry as the first option
    if (WidgetFactory.responsiblePartyList.size() == 0) {
      List newRow = new ArrayList();
      newRow.add("");
      newRow.add("");
      newRow.add("");
      newRow.add("");
      WidgetFactory.responsiblePartyList.add(newRow);
    }
  }

  /**
   * Initiates various parameters of PartyMainPage based on value of variable
   * role.
   */
  private void initRole() {

    switch (role) {
      case PartyPage.CREATOR:

        oneOrMoreRequired = true;
        pageID = DataPackageWizardInterface.PARTY_CREATOR;
        nextPageID = DataPackageWizardInterface.PARTY_CONTACT;
        pageNumber = "5";
        subtitle = "Owners";
        xPathRoot = "/eml:eml/dataset/creator[";
        description =
            "<p><b>Enter information about the Owners</b>: This is information about the "
            +
            "persons or organizations certified for the data. The list of data "
            +
            "owners should include all people and organizations who should be cited "
            + "for the data. Select Add to add an owner"
            + "<br></br></p>";
        break;

      case PartyPage.CONTACT:

        oneOrMoreRequired = true;
        pageID = DataPackageWizardInterface.PARTY_CONTACT;
        nextPageID = DataPackageWizardInterface.PARTY_ASSOCIATED;
        pageNumber = "6";
        subtitle = "Contacts";
        xPathRoot = "/eml:eml/dataset/contact[";
        description =
            "<p><b>Enter information about the Contacts</b>: This is information about the "
            +
            "person or organizations who are the contacts for this dataset. This is "
            +
            "the person or institution to contact with questions about the use or "
            +
            "interpretation of a data package. This may or may not be same as the owner."
            + "<br></br></p>";
        break;

      case PartyPage.ASSOCIATED:

        oneOrMoreRequired = false;
        pageID = DataPackageWizardInterface.PARTY_ASSOCIATED;
        nextPageID = DataPackageWizardInterface.PROJECT;
        pageNumber = "7";
        subtitle = "Associated Parties";
        xPathRoot = "/eml:eml/dataset/associatedParty[";
        description =
            "<p><b>Enter information about Associated People and Organizations</b>: "
            + "This is information about the people or organizations "
            + "who should be associated with the resource. These "
            +
            "parties might play various roles in the creation or maintenance of "
            +
            "the resource, and these roles should be indicated in the \"role\" "
            + "element.<br></br></p>";
        break;

      case PartyPage.PERSONNEL:
        oneOrMoreRequired = true;
        subtitle = "Personnel";
        xPathRoot = "/eml:eml/dataset/project/personnel[";
        description =
            "<p>b>Enter information about Personnel</b>: This is information about "
            +
            "the people or organizations who should be associated with the resource. These "
            +
            "parties might play various roles in the creation or maintenance of "
            +
            "the resource, and these roles should be indicated in the \"role\" "
            + "element.<br></br></p>";
        break;

      default:
        Log.debug(5, "Unrecognized role parameter passed to PartyPage: " + role);
        return;
    }
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
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
  }

  /**
   * A method to show new Party Page Dialog
   */
  private void showNewPartyDialog() {

    PartyPage partyPage = (PartyPage) WizardPageLibrary.getPage(
        DataPackageWizardInterface.PARTY_PAGE);
    partyPage.setRole(role);
    ModalDialog wpd
        = new ModalDialog(partyPage,
                          WizardContainerFrame.getDialogParent(),
                          UISettings.POPUPDIALOG_WIDTH,
                          UISettings.POPUPDIALOG_HEIGHT);
    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

      List newRow = partyPage.getSurrogate();
      newRow.add(partyPage);
      partiesList.addRow(newRow);
      if (!partyPage.isReference) {
        DataPackageWizardInterface.responsiblePartyList.add(newRow);
        if (!partyPage.referDiffDP) {
          WidgetFactory.responsiblePartyList.add(newRow);
        }
      }
    }

    if (oneOrMoreRequired) {
      WidgetFactory.unhiliteComponent(minRequiredLabel);
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
    editPartyPage.setEditValue();
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
      if (!editPartyPage.isReference) {
        WidgetFactory.responsiblePartyList.add(newRow);
        DataPackageWizardInterface.responsiblePartyList.add(newRow);
      }
    }
  }

  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
    if (oneOrMoreRequired && partiesList.getListOfRowLists().isEmpty()) {
      showNewPartyDialog();
      // partiesPickList.getList();
    }
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

    int index = 1;
    Object nextRowObj = null;
    List nextRowList = null;
    Object nextUserObject = null;
    OrderedMap nextNVPMap = null;
    PartyPage nextPartyPage = null;

    List rowLists = partiesList.getListOfRowLists();

    if (rowLists != null && rowLists.isEmpty()) {
      return null;
    }

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      nextRowObj = it.next();
      if (nextRowObj == null) {
        continue;
      }

      nextRowList = (List) nextRowObj;
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size() < 4) {
        continue;
      }
      nextUserObject = nextRowList.get(3);
      if (nextUserObject == null) {
        continue;
      }

      nextPartyPage = (PartyPage) nextUserObject;

      if (nextPartyPage.isReference &&
          !listContains(rowLists, nextPartyPage.referedPage)) {
        continue;
      }

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
   * Checks if the list contains a PartyPage similar to the PartyPage passed in
   * the parameters.
   *
   * @param rowLists List
   * @param page PartyPage
   * @return boolean
   */
  private boolean listContains(List rowLists, PartyPage page) {
    if (rowLists == null) {
      return false;
    }

    Object nextRowObj;
    List nextRowList;
    PartyPage nextPage;
    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
      nextRowObj = it.next();
      if (nextRowObj == null) {
        continue;
      }
      nextRowList = (List) nextRowObj;
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size() < 4) {
        continue;
      }
      nextPage = (PartyPage) nextRowList.get(3);
      if (nextPage.getsalutationFieldText().equals(page.getsalutationFieldText()) &&
          nextPage.getfirstNameFieldText().equals(page.getfirstNameFieldText()) &&
          nextPage.getlastNameFieldText().equals(page.getlastNameFieldText()) &&
          nextPage.getorganizationFieldText().equals(page.
          getorganizationFieldText()) &&
          nextPage.getpositionNameFieldText().equals(page.
          getpositionNameFieldText()) &&
          nextPage.getaddress1FieldText().equals(page.getaddress1FieldText()) &&
          nextPage.getaddress2FieldText().equals(page.getaddress2FieldText()) &&
          nextPage.getcityFieldText().equals(page.getcityFieldText()) &&
          nextPage.getstateFieldText().equals(page.getstateFieldText()) &&
          nextPage.getzipFieldText().equals(page.getzipFieldText()) &&
          nextPage.getcountryFieldText().equals(page.getcountryFieldText()) &&
          nextPage.getphoneFieldText().equals(page.getphoneFieldText()) &&
          nextPage.getfaxFieldText().equals(page.getfaxFieldText()) &&
          nextPage.getemailFieldText().equals(page.getemailFieldText()) &&
          nextPage.geturlFieldText().equals(page.geturlFieldText())) {
        return true;
      }
    }

    return false;
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
   *  sets the OrderMap for this wizard page
   *
   *  @return
   */
  public void setPageData(OrderedMap data, String _xPathRoot) {}
}
