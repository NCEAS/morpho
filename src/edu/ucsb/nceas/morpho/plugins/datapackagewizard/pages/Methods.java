/**
 *  '$RCSfile: Methods.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-24 02:14:18 $'
 * '$Revision: 1.10 $'
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

import java.util.Iterator;
import java.util.List;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import java.awt.Dimension;
import javax.swing.JComponent;

public class Methods
    extends AbstractUIPage {

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID = DataPackageWizardInterface.METHODS;
  private final String nextPageID = DataPackageWizardInterface.ACCESS;
  private final String title = "Methods and Sampling";
  private final String subtitle = "";
  private final String xPathRoot = "/eml:eml/dataset/methods";
  private final String pageNumber = "12";
  private final String EMPTY_STRING = "";

  private JTextArea studyArea;
  private JTextArea sampleArea;
  private JLabel studyLabel;
  private JLabel sampleLabel;
  private JLabel warningLabel;
  private JPanel warningPanel;

  private static final Dimension PARTY_FULL_LABEL_DIMS = new Dimension(700, 20);

  private final String[] colNames = {
      "Method step title", "Method step description"};
  private final Object[] editors = null; //makes non-directly-editable

  private CustomList methodsList;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public Methods() {
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel vbox = this;

    vbox.add(WidgetFactory.makeDefaultSpacer());
    JLabel desc1 = WidgetFactory.makeHTMLLabel(
        "<b>Enter Method Step Description</b>", 1);
    vbox.add(desc1);
    vbox.add(WidgetFactory.makeHalfSpacer());

    methodsList = WidgetFactory.makeList(colNames, editors, 4,
                                         true, true, false, true, true, true);

    methodsList.setBorder(new javax.swing.border.EmptyBorder(0,
        WizardSettings.PADDING, 0, 0));
    vbox.add(methodsList);

    vbox.add(WidgetFactory.makeHalfSpacer());
    vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel studyDesc = WidgetFactory.makeHTMLLabel(
        "<b>Study Extent Description</b> Describe the temporal, spatial and "
        + "taxonomic extent of the study, supplementing the information on "
        +
        "coverage provided earlier. For example, if the temporal coverage of the"
        + " data is 1990-2000, you might provide details about any years that "
        + "were missed or the months in which sampling occurred.", 3);
    vbox.add(studyDesc);

    JPanel studyPanel = WidgetFactory.makePanel(10);
    studyLabel = WidgetFactory.makeLabel(" Study Extent", false);
    studyPanel.add(studyLabel);

    studyArea = WidgetFactory.makeTextArea("", 3, true);
    JScrollPane jStudyScrl = new JScrollPane(studyArea);
    studyPanel.add(jStudyScrl);

    vbox.add(studyPanel);
    vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel sampleDesc = WidgetFactory.makeHTMLLabel(
        "<b>Sampling Description</b> Describe the sampling design of the study."
        + " For example, you might describe the way in which treatments were "
        + "assigned to sampling units.", 2);
    vbox.add(sampleDesc);

    JPanel samplePanel = WidgetFactory.makePanel(10);
    sampleLabel = WidgetFactory.makeLabel(" Sampling", false);
    samplePanel.add(sampleLabel);

    sampleArea = WidgetFactory.makeTextArea("", 3, true);
    JScrollPane jSampleScrl = new JScrollPane(sampleArea);
    samplePanel.add(jSampleScrl);

    vbox.add(samplePanel);
    vbox.add(WidgetFactory.makeDefaultSpacer());

    warningPanel = WidgetFactory.makePanel(1);
    warningLabel = WidgetFactory.makeLabel(
        "Warning: at least one of the three "
        + "entries is required: Last Name, Position Name or Organization", true);
    warningPanel.add(warningLabel);
    warningPanel.setVisible(false);
    setPrefMinMaxSizes(warningLabel, PARTY_FULL_LABEL_DIMS);
    warningPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        12 * WizardSettings.PADDING,
        0, 8 * WizardSettings.PADDING));

    vbox.add(warningPanel);

    initActions();
  }

  /**
   *  Custom actions to be initialized for list buttons
   */
  private void initActions() {

    methodsList.setCustomAddAction(

        new AbstractAction() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nMethods: CustomAddAction called");
        showNewMethodsDialog();
      }
    });

    methodsList.setCustomEditAction(

        new AbstractAction() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nMethods: CustomEditAction called");
        showEditMethodsDialog();
      }
    });
  }

  private void showNewMethodsDialog() {

    MethodsPage methodsPage = (MethodsPage) WizardPageLibrary.getPage(
        DataPackageWizardInterface.METHODS_PAGE);
    ModalDialog wpd = new ModalDialog(methodsPage,
                                      WizardContainerFrame.getDialogParent(),
                                      UISettings.POPUPDIALOG_WIDTH,
                                      UISettings.POPUPDIALOG_HEIGHT
                                      , false);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

      List newRow = methodsPage.getSurrogate();
      newRow.add(methodsPage);
      methodsList.addRow(newRow);
    }
  }

  private void showEditMethodsDialog() {

    List selRowList = methodsList.getSelectedRowList();

    if (selRowList == null || selRowList.size() < 3) {
      return;
    }

    Object dialogObj = selRowList.get(2);

    if (dialogObj == null || ! (dialogObj instanceof MethodsPage)) {
      return;
    }
    MethodsPage editMethodsPage = (MethodsPage) dialogObj;

    ModalDialog wpd = new ModalDialog(editMethodsPage,
                                      WizardContainerFrame.getDialogParent(),
                                      UISettings.POPUPDIALOG_WIDTH,
                                      UISettings.POPUPDIALOG_HEIGHT
                                      , false);
    wpd.resetBounds();
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE == ModalDialog.OK_OPTION) {

      List newRow = editMethodsPage.getSurrogate();
      newRow.add(editMethodsPage);
      methodsList.replaceSelectedRow(newRow);
    }
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
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

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

    if (methodsList.getRowCount() == 0 &&
        (studyArea.getText().trim().compareTo("") != 0 ||
         sampleArea.getText().trim().compareTo("") != 0)) {
      // method is requried
      warningLabel.setText("Method steps are required if you provide either a "
                           +"study extent or smapling description");
      warningPanel.setVisible(true);
      return false;
    }

    if ( (studyArea.getText().trim().compareTo("") == 0 &&
          sampleArea.getText().trim().compareTo("") != 0)) {

      warningLabel.setText("Study extent is required if you provide "
                           +"sampling description");
      warningPanel.setVisible(true);
      return false;
    }
    if ( (studyArea.getText().trim().compareTo("") != 0 &&
          sampleArea.getText().trim().compareTo("") == 0)) {

      warningLabel.setText("Sampling description is required if you provide "
                           +"study extent");
      warningPanel.setVisible(true);
      return false;
    }

    warningPanel.setVisible(false);
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

  //
  public OrderedMap getPageData() {

    returnMap.clear();

    int index = 1;
    Object nextRowObj = null;
    List nextRowList = null;
    Object nextUserObject = null;
    OrderedMap nextNVPMap = null;
    MethodsPage nextMethodsPage = null;

    List rowLists = methodsList.getListOfRowLists();
    if (rowLists != null && rowLists.isEmpty()) {
      return null;
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

      nextMethodsPage = (MethodsPage) nextUserObject;

      nextNVPMap = nextMethodsPage.getPageData(
          "/eml:eml/dataset/methods/methodStep/description/section[" + (index++) +
          "]");
      returnMap.putAll(nextNVPMap);
    }

    String study = studyArea.getText().trim();
    if (study != null && !study.equals(EMPTY_STRING)) {
      returnMap.put(xPathRoot + "/sampling/studyExtent/description/para", study);
    }

    String sample = sampleArea.getText().trim();
    if (sample != null &&!sample.equals(EMPTY_STRING)) {
      returnMap.put(xPathRoot + "/sampling/samplingDescription/para", sample);
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

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


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
