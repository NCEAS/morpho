/**
 *  '$RCSfile: Project.java,v $'
 *    Purpose: A class for showing project screen
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-23 20:02:17 $'
 * '$Revision: 1.26 $'
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Project extends AbstractUIPage {

  public final String pageID     = DataPackageWizardInterface.PROJECT;
  public final String nextPageID = DataPackageWizardInterface.USAGE_RIGHTS;

  public final String title      = "Research Project Information";
  public final String subtitle   = " ";
  public final String pageNumber = "8";

  private JPanel checkBoxPanel;
  private JPanel dataPanel;
  private JPanel noDataPanel;
  private JPanel currentPanel;

  private final String PROJECT_ROOT        = "project/";
  private final String XPATH_ROOT          = "/eml:eml/dataset[1]/" + PROJECT_ROOT;

  private final String TITLE_REL_XPATH     = "title[1]";
  private final String FUNDING_REL_XPATH   = "funding[1]/para[1]";
  private final String PERSONNEL_REL_XPATH = "personnel[";

  private String xPathRoot = PROJECT_ROOT;

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

    checkBoxPanel = WidgetFactory.makeCheckBoxPanel(buttonsText, -1, ilistener);
    checkBoxPanel.setBorder(new EmptyBorder(0, WizardSettings.PADDING,
                                          WizardSettings.PADDING,
                                          2 * WizardSettings.PADDING));
    topBox.add(checkBoxPanel);
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
       ModalDialog wpd = new ModalDialog(partyPage,
                                WizardContainerFrame.getDialogParent(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT);

       if (wpd.USER_RESPONSE==ModalDialog.OK_OPTION) {
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

       ModalDialog wpd = new ModalDialog(editPartyPage,
                                WizardContainerFrame.getDialogParent(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT, false);
       wpd.resetBounds();
       wpd.setVisible(true);

       if (wpd.USER_RESPONSE==ModalDialog.OK_OPTION) {
         List newRow = editPartyPage.getSurrogate();
         newRow.add(editPartyPage);
         partiesList.replaceSelectedRow(newRow);
       }
     }


  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

    WidgetFactory.unhiliteComponent(titleLabel);
    WidgetFactory.unhiliteComponent(minRequiredLabel);
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {}

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
      WidgetFactory.unhiliteComponent(titleLabel);

      if (partiesList.getRowCount() < 1) {
        WidgetFactory.hiliteComponent(minRequiredLabel);
        return false;
      }
      WidgetFactory.unhiliteComponent(minRequiredLabel);
    }
    return true;
  }


  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   *
   * @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData(String rootXPath) {

    if (rootXPath==null) rootXPath = XPATH_ROOT;
    rootXPath = rootXPath.trim();
    if (rootXPath.length() < 1) rootXPath = XPATH_ROOT;
    if (!rootXPath.endsWith("/")) rootXPath += "/";

    returnMap.clear();

    if (currentPanel == dataPanel) {

      if ( !(titleField.getText().trim().equals("")) ) {
        returnMap.put(rootXPath + TITLE_REL_XPATH, titleField.getText().trim());
      }

      int index = 1;
      Object  nextRowObj      = null;
      List    nextRowList     = null;
      Object  nextUserObject  = null;
      OrderedMap  nextNVPMap  = null;
      PartyPage nextPartyPage = null;

      List rowLists = partiesList.getListOfRowLists();

      if (rowLists != null && rowLists.isEmpty()) {
         return null;
       }

      for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

        nextRowObj = it.next();
        if (nextRowObj==null) continue;

        nextRowList = (List)nextRowObj;
        //column 3 is user object - check it exists and isn't null:
        if (nextRowList.size()<4)     continue;
        nextUserObject = nextRowList.get(3);
        if (nextUserObject==null) continue;

        nextPartyPage = (PartyPage)nextUserObject;

        nextNVPMap = nextPartyPage.getPageData(rootXPath + PERSONNEL_REL_XPATH
                                               + (index++) + "]");
        returnMap.putAll(nextNVPMap);
      }

      if ( !(fundingField.getText().trim().equals("")) ) {
        returnMap.put(rootXPath + FUNDING_REL_XPATH, fundingField.getText().trim());
      }
    }

    return returnMap;
  }


  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular wizard page
   *
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular wizard page
   */
  public OrderedMap getPageData() {

    return getPageData(XPATH_ROOT);
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



  public void setPageData(OrderedMap data, String _xPathRoot) {

    if (_xPathRoot!=null && _xPathRoot.trim().length() > 0) this.xPathRoot = _xPathRoot;

    JCheckBox checkBox = ((JCheckBox)(checkBoxPanel.getComponent(0)));

    if (data==null || data.isEmpty()) {

      checkBox.setSelected(false);
      this.resetBlankData();
      return;
    }
    checkBox.setSelected(true);

    Iterator keyIt = data.keySet().iterator();
    Object nextXPathObj = null;
    String nextXPath = null;
    Object nextValObj = null;
    String nextVal = null;

    List personnelList = new ArrayList();

    while (keyIt.hasNext()) {

      nextXPathObj = keyIt.next();
      if (nextXPathObj == null)continue;
      nextXPath = (String)nextXPathObj;

      nextValObj = data.get(nextXPathObj);
      nextVal = (nextValObj == null) ? "" : ((String)nextValObj).trim();

      Log.debug(45, "Project:  nextXPath = " + nextXPath
                + "\n nextVal   = " + nextVal);

      // remove everything up to and including the last occurrence of
      // this.xPathRoot to get relative xpaths, in case we're handling a
      // project elsewhere in the tree...
      nextXPath = nextXPath.substring(nextXPath.lastIndexOf(this.xPathRoot)
                                      + this.xPathRoot.length());

      Log.debug(45, "Project: TRIMMED nextXPath   = " + nextXPath);

      if (nextXPath.startsWith(TITLE_REL_XPATH)) {

        titleField.setText(nextVal);

      } else if (nextXPath.startsWith(FUNDING_REL_XPATH)) {

        fundingField.setText(nextVal);

      } else if (nextXPath.startsWith(PERSONNEL_REL_XPATH)) {

        Log.debug(45,">>>>>>>>>> adding to personnelList: nextXPathObj="
                  +nextXPathObj+"; nextValObj="+nextValObj);
        addToPersonnel(nextXPathObj, nextValObj, personnelList);
      }
    }

    Iterator persIt = personnelList.iterator();
    Object nextPersonnelMapObj = null;
    OrderedMap nextPersonnelMap = null;
    int partyPredicate = 1;

    partiesList.removeAllRows();

    while (persIt.hasNext()) {

      nextPersonnelMapObj = persIt.next();
      if (nextPersonnelMapObj == null) continue;
      nextPersonnelMap = (OrderedMap)nextPersonnelMapObj;
      if (nextPersonnelMap.isEmpty()) continue;

      PartyPage nextParty = (PartyPage)WizardPageLibrary.getPage(
          DataPackageWizardInterface.PARTY_PAGE);

      nextParty.setRole(PartyPage.PERSONNEL);

      nextParty.setPageData(nextPersonnelMap, this.xPathRoot
                            + PERSONNEL_REL_XPATH + (partyPredicate++) + "]/");

      List newRow = nextParty.getSurrogate();
      newRow.add(nextParty);

      partiesList.addRow(newRow);
    }
  }


  // resets all fields to blank
  private void resetBlankData() {

    titleField.setText("");
    fundingField.setText("");
    partiesList.removeAllRows();

  }


  private int getFirstPredicate(String xpath, String firstSegment) {

    String tempXPath
        = xpath.substring(xpath.indexOf(firstSegment) + firstSegment.length());

    return Integer.parseInt(
        tempXPath.substring(0, tempXPath.indexOf("]")));
  }


  private void addToPersonnel(Object nextPersonnelXPathObj,
                              Object nextPersonnelVal, List personnelList) {

    if (nextPersonnelXPathObj == null) return;
    String nextPersonnelXPath = (String)nextPersonnelXPathObj;
    int predicate = getFirstPredicate(nextPersonnelXPath, PERSONNEL_REL_XPATH);

// NOTE predicate is 1-relative, but List indices are 0-relative!!!
    if (predicate >= personnelList.size()) {

      for (int i = personnelList.size(); i <= predicate; i++) {
        personnelList.add(new OrderedMap());
      }
    }

    if (predicate < personnelList.size()) {
      Object nextMapObj = personnelList.get(predicate);
      OrderedMap nextMap = (OrderedMap)nextMapObj;
      nextMap.put(nextPersonnelXPathObj, nextPersonnelVal);
    } else {
      Log.debug(15,"**** ERROR - Project.addToPersonnel() - predicate > personnelList.size()");
    }
  }

}











