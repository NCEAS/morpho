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
 *     '$Date: 2004-03-17 21:13:01 $'
 * '$Revision: 1.4 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Methods extends AbstractUIPage{

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID     = DataPackageWizardInterface.METHODS;
  private final String nextPageID = DataPackageWizardInterface.ACCESS;
  private final String title      = "Methods";
  private final String subtitle   = "";
  private final String xPathRoot  = "/eml:eml/dataset/methods";
  private final String pageNumber  = "12";

  private JTextArea studyField;
  private JTextArea sampleField;
  private JLabel    titleLabel;
  private JLabel    studyLabel;
  private JLabel    sampleLabel;
  private JTextField titleField;

  private final String[] colNames =  {"Method Description"};
  private final Object[] editors  =   null; //makes non-directly-editable

  private CustomList  methodsList;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public Methods() { init(); }



  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel vbox = this;

    vbox.add(WidgetFactory.makeHalfSpacer());

    JLabel titleDesc = WidgetFactory.makeHTMLLabel(
        "<b>Enter title of the method</b> ", 1);
    vbox.add(titleDesc);

    JPanel titlePanel = WidgetFactory.makePanel(1);

    titleLabel = WidgetFactory.makeLabel(" Title", false);
    titlePanel.add(titleLabel);

    titleField = WidgetFactory.makeOneLineTextField();
    titlePanel.add(titleField);

    titlePanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(titlePanel);

    vbox.add(WidgetFactory.makeDefaultSpacer());
    JLabel desc1 = WidgetFactory.makeHTMLLabel(
      "<b>Enter Method Description</b>", 1);
    vbox.add(desc1);
    vbox.add(WidgetFactory.makeHalfSpacer());

    methodsList = WidgetFactory.makeList(colNames, editors, 4,
                                    true, true, false, true, true, true );

    methodsList.setBorder(new EmptyBorder(0,WizardSettings.PADDING,
                             WizardSettings.PADDING, 4*WizardSettings.PADDING));

    vbox.add(methodsList);
    vbox.add(WidgetFactory.makeHalfSpacer());
    vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel studyDesc = WidgetFactory.makeHTMLLabel(
        "<b>Description of Study Extent</b> Describe the temporal, spatial and "
        +"taxonomic extent of the study, supplementing the information on "
        +"coverage provided above. For example, if the temporal coverage of the"
        +" data is 1990-2000, you might provide details about any years that "
        +"were missed or the months in which sampling occurred.", 3);
    vbox.add(studyDesc);
    vbox.add(WidgetFactory.makeHalfSpacer());

    JPanel studyPanel = WidgetFactory.makePanel(4);
    studyLabel = WidgetFactory.makeLabel(" Study Extent", false);
    studyPanel.add(studyLabel);

    studyField = WidgetFactory.makeTextArea("", 4, true);
    JScrollPane jStudyScrl = new JScrollPane(studyField);
    studyPanel.add(jStudyScrl);

    studyPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(studyPanel);
    vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel sampleDesc = WidgetFactory.makeHTMLLabel(
        "<b>Sampling Description</b> Describe the sampling design of the study."
        +" For example, you might describe the way in which treatments were "
        +"assigned to sampling units.", 2);
    vbox.add(sampleDesc);
    vbox.add(WidgetFactory.makeHalfSpacer());

    JPanel samplePanel = WidgetFactory.makePanel(4);
    sampleLabel = WidgetFactory.makeLabel(" Description", false);
    samplePanel.add(sampleLabel);

    sampleField = WidgetFactory.makeTextArea("", 4, true);
    JScrollPane jSampleScrl = new JScrollPane(sampleField);
    samplePanel.add(jSampleScrl);

    samplePanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(samplePanel);
    vbox.add(WidgetFactory.makeDefaultSpacer());

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

    MethodsPage methodsPage = (MethodsPage)WizardPageLibrary.getPage(DataPackageWizardInterface.METHODS_PAGE);
    WizardPopupDialog wpd = new WizardPopupDialog(methodsPage, false);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {

      List newRow = methodsPage.getSurrogate();
      newRow.add(methodsPage);
      methodsList.addRow(newRow);
    }
  }


  private void showEditMethodsDialog() {

    List selRowList = methodsList.getSelectedRowList();

    if (selRowList==null || selRowList.size() < 1) return;

    Object dialogObj = selRowList.get(1);

    if (dialogObj==null || !(dialogObj instanceof MethodsPage)) return;
    MethodsPage editMethodsPage = (MethodsPage)dialogObj;

    WizardPopupDialog wpd = new WizardPopupDialog(editMethodsPage, false);
    wpd.resetBounds();
    wpd.setVisible(true);


    if (wpd.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {

      List newRow = editMethodsPage.getSurrogate();
      newRow.add(editMethodsPage);
      methodsList.replaceSelectedRow(newRow);
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
    Object  nextRowObj      = null;
    List    nextRowList     = null;
    Object  nextUserObject  = null;
    OrderedMap  nextNVPMap  = null;
    MethodsPage nextMethodsPage = null;



    String title = titleField.getText().trim();

    if (title!=null) {
      if (title.length()<1) return null;
      returnMap.put(xPathRoot + "/methodStep/description/section/title", title);
    }

    List rowLists = methodsList.getListOfRowLists();
    if (rowLists==null) return null;

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      nextRowObj = it.next();
      if (nextRowObj==null) continue;

      nextRowList = (List)nextRowObj;
      //column 2 is user object - check it exists and isn't null:
      if (nextRowList.size()<1)     continue;
      nextUserObject = nextRowList.get(1);
      if (nextUserObject==null) continue;

      nextMethodsPage = (MethodsPage)nextUserObject;

      nextNVPMap = nextMethodsPage.getPageData("/eml:eml/dataset/methods/methodStep/description/section/para[" + (index++) + "]");
      returnMap.putAll(nextNVPMap);
    }

    String study = studyField.getText().trim();
    if (study!=null) {
      returnMap.put(xPathRoot + "/sampling/studyExtent/description/para", study);
    }

    String sample = sampleField.getText().trim();
    if (sample!=null) {
      returnMap.put(xPathRoot + "/sampling/samplingDescription/para", sample);
    }

    return returnMap;
  }




  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


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


