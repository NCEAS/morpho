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
 *     '$Date: 2004-02-24 17:38:09 $'
 * '$Revision: 1.14 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.Iterator;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Project extends AbstractWizardPage {

  public final String pageID     = DataPackageWizardInterface.PROJECT;
  public final String nextPageID = DataPackageWizardInterface.USAGE_RIGHTS;
//////////////////////////////////////////////////////////

  public final String title      = "Research Project Information";
  public final String subtitle   = " ";
  public final String pageNumber = "8";

  private JPanel dataPanel;
  private JPanel noDataPanel;
  private JPanel currentPanel;

  private final String xPathRoot  = "/eml:eml/dataset/project/";

  private final String[] buttonsText = new String[] {
      "This project is part of a larger, umbrella research project"
  };


  public Project() {
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */

  private void init() {

    this.setLayout(new BorderLayout());
    Box topBox = Box.createVerticalBox();

    //topBox.add(WidgetFactory.makeHalfSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
        "<b>Is your project part of a larger, umbrella research project?</b> "
        +"Data may be collected as part of a large research program with many "
        +"sub-projects or they may be associated with a single, independent "
        +"investigation. For example, a large NSF grant may provide funds for "
        +"several PIs to collect data at various locations. In this case it is "
        +"important to be able to reference sub-projects to the larger project.", 4);

    topBox.add(WidgetFactory.makeHalfSpacer());
    topBox.add(desc);


    final JPanel instance = this;
    ItemListener ilistener = new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        Log.debug(45, "got radiobutton command: "+e.getStateChange());
        onLoadAction();
        if (e.getStateChange() == ItemEvent.DESELECTED) {
          instance.remove(currentPanel);
          currentPanel = noDataPanel;
          instance.add(noDataPanel, BorderLayout.CENTER);
        } else if (e.getStateChange() == ItemEvent.SELECTED) {
          instance.remove(currentPanel);
          currentPanel = dataPanel;
          instance.add(dataPanel, BorderLayout.CENTER);
        }
        instance.validate();
        instance.repaint();
      }
    };

    JPanel radioPanel = WidgetFactory.makeCheckBoxPanel(buttonsText, -1, ilistener);
    topBox.add(radioPanel);
    topBox.add(WidgetFactory.makeHalfSpacer());

    this.add(topBox, BorderLayout.NORTH);
    dataPanel = getDataPanel();
    noDataPanel  = getNoDataPanel();
    currentPanel = noDataPanel;
  }

  /**
   *
   */
  private JLabel      titleLabel;
  private JTextField  titleField;
  private JLabel      fundingLabel;
  private JTextField  fundingField;
  private JLabel minRequiredLabel;
  private CustomList  partiesList;
  private final String[] colNames =  {"Party", "Role", "Address"};
  private final Object[] editors  =   null; //makes non-directly-editable

  private JPanel getDataPanel() {
    JPanel panel = WidgetFactory.makeVerticalPanel(6);
    WidgetFactory.addTitledBorder(panel, "Enter Project Information");
    //panel.add(WidgetFactory.makeDefaultSpacer());
    ////
    JPanel titlePanel = WidgetFactory.makePanel(1);
    JLabel titleDesc = WidgetFactory.makeHTMLLabel(
       "<b>Enter the title of the project.</b> ", 1);
    panel.add(titleDesc);
    titleLabel = WidgetFactory.makeLabel(" Title", true);
    titlePanel.add(titleLabel);
    titleField = WidgetFactory.makeOneLineTextField();
    titlePanel.add(titleField);
    titlePanel.setBorder(new javax.swing.border.EmptyBorder(0,
        0,0,5*WizardSettings.PADDING));
    panel.add(titlePanel);
    panel.add(WidgetFactory.makeHalfSpacer());
    JPanel fundingPanel = WidgetFactory.makePanel(1);
    JLabel fundingDesc = WidgetFactory.makeHTMLLabel(
      "<b>Enter the funding source(s) that supported data collection.</b> The "
      +"funding is used to provide information about funding sources for the "
      +"project such as agency name, grant and contract numbers.", 2);

    panel.add(fundingDesc);
    fundingLabel = WidgetFactory.makeLabel(" Funding Source", false);
    fundingPanel.add(fundingLabel);
    fundingField = WidgetFactory.makeOneLineTextField();
    fundingPanel.add(fundingField);
    fundingPanel.setBorder(new EmptyBorder(0,0,0,
                                           5*WizardSettings.PADDING));
    panel.add(fundingPanel);
    panel.add(WidgetFactory.makeHalfSpacer());
    ////
    JLabel desc = WidgetFactory.makeHTMLLabel(
      "<b>Enter the Personnel information</b> The full name of the person(s) or "
      +"organization(s) responsible for the project, such as agency name, grant "
      +"and contact numbers.", 2);
    panel.add(desc);
    JPanel vPanel = WidgetFactory.makeVerticalPanel(9);
    minRequiredLabel = WidgetFactory.makeLabel(
                                " One or more Personnel must be defined:", true,
                                WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
    vPanel.add(minRequiredLabel);
    partiesList = WidgetFactory.makeList(colNames, editors, 6,
                                    true, true, false, true, true, true );
    partiesList.setBorder(new EmptyBorder(0,WizardSettings.PADDING, WizardSettings.PADDING,
                         3*WizardSettings.PADDING));

 //   vPanel.add(WidgetFactory.makeDefaultSpacer());
    vPanel.add(partiesList);
    panel.add(vPanel);
    //panel.add(WidgetFactory.makeDefaultSpacer());
    panel.add(Box.createGlue());
    initActions();
    return panel;
  }


  /**
   *
   * @return a blank JPanel
   */
  private JPanel getNoDataPanel() {
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    return panel;
  }

  /**
   *
   */
  private void initActions() {
      partiesList.setCustomAddAction(
        new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            Log.debug(45, "\nResearchProjInfo: CustomAddAction called");
            showNewPartyDialog();
          }
        });

      partiesList.setCustomEditAction(
        new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            Log.debug(45, "\nResearchProjInfo: CustomEditAction called");
            showEditPartyDialog();
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
         if(!partyPage.isReference){
           WidgetFactory.responsiblePartyList.add(newRow);
         }
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

    if (currentPanel == dataPanel) {
      if (titleField.getText().trim().equals("")) {
        WidgetFactory.hiliteComponent(titleLabel);
        titleField.requestFocus();
        return false;
      }

      if (partiesList.getRowCount() < 1) {
        WidgetFactory.hiliteComponent(minRequiredLabel);
        return false;
      }
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

    if (currentPanel == dataPanel) {
      if ( !(titleField.getText().trim().equals("")) ) {
        returnMap.put(xPathRoot + "title", titleField.getText().trim());
      }

      int index = 1;
      Object  nextRowObj      = null;
      List    nextRowList     = null;
      Object  nextUserObject  = null;
      OrderedMap  nextNVPMap  = null;
      PartyPage nextPartyPage = null;

      List rowLists = partiesList.getListOfRowLists();

      if (rowLists==null) return null;

      for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

        nextRowObj = it.next();
        if (nextRowObj==null) continue;

        nextRowList = (List)nextRowObj;
        //column 3 is user object - check it exists and isn't null:
        if (nextRowList.size()<4)     continue;
        nextUserObject = nextRowList.get(3);
        if (nextUserObject==null) continue;

        nextPartyPage = (PartyPage)nextUserObject;

        nextNVPMap = nextPartyPage.getPageData(xPathRoot + "personnel[" + (index++) + "]");
        returnMap.putAll(nextNVPMap);
      }

      if ( !(fundingField.getText().trim().equals("")) ) {
        returnMap.put(xPathRoot + "funding/para", fundingField.getText().trim());
      }
    }
    return returnMap;
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
