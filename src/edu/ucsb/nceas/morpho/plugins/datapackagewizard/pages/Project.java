/**
 *  '$RCSfile: Project.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2003-12-03 02:38:49 $'
 * '$Revision: 1.3 $'
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
import edu.ucsb.nceas.morpho.util.Log;

import java.util.Map;
import java.util.List;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DataPackageWizardPlugin;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomPickList;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Project extends AbstractWizardPage {

  public final String pageID     = DataPackageWizardInterface.PROJECT;
  public final String nextPageID = DataPackageWizardInterface.GENERAL;
//////////////////////////////////////////////////////////

  public final String title      = "Research Project Information";
  public final String subtitle   = "Enter Project Information";
  public final String pageNumber = "2.1";

  private JLabel      titleLabel;
  private JTextField  titleField;
  private JLabel      fundingLabel;
  private JTextField  fundingField;
  private JLabel      urlLabelOnline;
  private JTextField  urlFieldOnline;
  private JLabel minRequiredLabel;
  private CustomList  partiesList;
  private CustomPickList partiesPickList;
  private final String[] colNames =  {"Party", "Role", "Address"};
  private final Object[] editors  =   null; //makes non-directly-editable

  public Project() {

    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // title
    this.add(WidgetFactory.makeDefaultSpacer());
    JPanel titlePanel = WidgetFactory.makePanel(1);
    JLabel titleDesc = WidgetFactory.makeHTMLLabel(
       "<b>Enter the title of the project.</b> ", 1);
    this.add(titleDesc);
    titleLabel = WidgetFactory.makeLabel("Title", true);
    titlePanel.add(titleLabel);
    titleField = WidgetFactory.makeOneLineTextField();
    titlePanel.add(titleField);
    this.add(titlePanel);
    this.add(WidgetFactory.makeDefaultSpacer());

    // funding
    JPanel fundingPanel = WidgetFactory.makePanel(1);
    JLabel fundingDesc = WidgetFactory.makeHTMLLabel(
      "<b>Enter the funding source(s) that supported data collection.</b> The "
      +"funding is used to provide information about funding sources for the "
      +"project such as agency name, grant and contract numbers.", 3);

    this.add(fundingDesc);
    fundingLabel = WidgetFactory.makeLabel("Funding Source", false);
    fundingPanel.add(fundingLabel);
    fundingField = WidgetFactory.makeOneLineTextField();
    fundingPanel.add(fundingField);
    this.add(fundingPanel);


    this.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
      "<b>Enter the Personals information</b> The full name of the person or organization for the "
      +"project such as agency name, grant and contact numbers.(DESCRIPTION??)", 2);
    this.add(desc);

    JPanel vPanel = WidgetFactory.makeVerticalPanel(14);

    minRequiredLabel = WidgetFactory.makeLabel(
                                "One or more Personals must be defined:", true,
                                WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
    vPanel.add(minRequiredLabel);

    partiesList = WidgetFactory.makeList(colNames, editors, 8,
                                    true, true, false, true, true, true );

    vPanel.add(WidgetFactory.makeDefaultSpacer());

    vPanel.add(partiesList);

    partiesPickList = new CustomPickList();

    vPanel.add(WidgetFactory.makeDefaultSpacer());

    vPanel.add(partiesPickList);

    this.add(vPanel);

    initActions();
  }


  /**
   *
   */
  private void initActions() {

    partiesList.setCustomAddAction(

        new AbstractAction() {  public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nResearchProjInfo: CustomAddAction called");
        showNewPartyDialog();
      }
    });

    partiesList.setCustomEditAction(

        new AbstractAction() {  public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nResearchProjInfo: CustomEditAction called");
        showEditPartyDialog();
      }
    });

    partiesPickList.setCustomAddAction(

        new AbstractAction() {  public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nPartyPage: CustomAddAction called");
        showAddPartyDialog();
      }
    });

  }


  /**
   *
   */
  private void showNewPartyDialog() {
    PartyPage partyPage = (PartyPage)WizardPageLibrary.getPage(DataPackageWizardInterface.PARTY_PAGE);
    partyPage.setRole(PartyPage.PERSONNEL);
    WizardPopupDialog wpd = new WizardPopupDialog(partyPage, WizardContainerFrame.frame);
    if (wpd.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {
      List newRow = partyPage.getSurrogate();
      newRow.add(partyPage);
      partiesList.addRow(newRow);
      partiesPickList.addRow(newRow);
    }

    WidgetFactory.unhiliteComponent(minRequiredLabel);
  }


  /**
   *
   */
  private void showEditPartyDialog() {
    List selRowList = partiesList.getSelectedRowList();
    if (selRowList==null || selRowList.size() < 4) return;
    Object dialogObj = selRowList.get(3);

    if (dialogObj==null || !(dialogObj instanceof PartyPage)) return;
    PartyPage editPartyPage = (PartyPage)dialogObj;
    WizardPopupDialog wpd = new WizardPopupDialog(editPartyPage, WizardContainerFrame.frame, false);
    wpd.resetBounds();
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {

      List newRow = editPartyPage.getSurrogate();
      newRow.add(editPartyPage);
      partiesList.replaceSelectedRow(newRow);
    }
  }

  /**
   *
   */
  private void showAddPartyDialog() {
    List selRowList = partiesPickList.getSelectedRowList();
    if (selRowList==null || selRowList.size() < 4) return;
    Object dialogObj = selRowList.get(3);

    if (dialogObj==null || !(dialogObj instanceof PartyPage)) return;
    PartyPage editPartyPage = (PartyPage)dialogObj;
    editPartyPage.modifyEditPage(PartyPage.PERSONNEL);
    WizardPopupDialog wpd = new WizardPopupDialog(editPartyPage, WizardContainerFrame.frame, false);
    wpd.resetBounds();
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {
      List newRow = editPartyPage.getSurrogate();
      newRow.add(editPartyPage);
      partiesList.addRow(newRow);
      partiesPickList.addRow(newRow);
    }

    WidgetFactory.unhiliteComponent(minRequiredLabel);
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
    if (titleField.getText().trim().equals("")) {
      WidgetFactory.hiliteComponent(titleLabel);
      titleField.requestFocus();
      return false;
    }

    if (partiesList.getRowCount() < 1) {
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
  public OrderedMap getPageData() {

    return null;
  }




  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return pageID; }

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
  public String getNextPageID() { return nextPageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  public void setPageData(OrderedMap data) { }
}
