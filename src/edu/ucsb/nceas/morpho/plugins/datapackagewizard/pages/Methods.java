/**
 *  '$RCSfile: Methods.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-23 01:40:36 $'
 * '$Revision: 1.18 $'
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
import java.util.ArrayList;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

public class Methods
    extends AbstractUIPage {

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID = DataPackageWizardInterface.METHODS;
  private final String title = /*"Methods and Sampling"*/ Language.getInstance().getMessage("MethodsAndSampling");
  private final String subtitle = "";
  private final String pageNumber = "13";
  private final String EMPTY_STRING = "";

  private final String METHOD_ROOT = "methods/";
  private final String XPATH_ROOT = "/eml:eml/dataset[1]/" + METHOD_ROOT;

  private final String STUDY_REL_XPATH =
      "sampling[1]/studyExtent[1]/description[1]/para[1]";
  private final String SAMPLING_REL_XPATH =
      "sampling[1]/samplingDescription[1]/para[1]";
  private final String METHODSTEP_REL_XPATH = "methodStep[";

  private String xPathRoot = METHOD_ROOT;

  private JTextArea studyArea;
  private JTextArea sampleArea;
  private JLabel studyLabel;
  private JLabel sampleLabel;
  private JLabel warningLabel;
  private JPanel warningPanel;

  private static final Dimension FULL_LABEL_DIMS = new Dimension(700, 20);

  private final String[] colNames = {
      /*"Method Step Title"*/ Language.getInstance().getMessage("MethodStepTitle"),
      /*"Method Step Description"*/Language.getInstance().getMessage("MethodStepDescription"),
      /*"Instrumentation"*/ Language.getInstance().getMessage("Instrumentation")
      };
  private final Object[] editors = null; //makes non-directly-editable

  private CustomList methodsList;
  private boolean checkMethodStep = false;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public Methods() {
	nextPageID = DataPackageWizardInterface.ACCESS;
    init();
  }
  
  /**
   * This Constructor will check citation in onAdvanceAction
   * @param checkPersonnel
   */
  public Methods(Boolean checkMethodStep)
  {
	  this();
	  try
	  {
	    this.checkMethodStep = checkMethodStep.booleanValue();
	  }
	  catch(Exception e)
	  {
		  Log.debug(30, "couldn't get the boolean value for "+checkMethodStep);
	  }
	 	  
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
      "<p>"
      + /*"<b>Enter method step description.</b> "*/ "<b>" + Language.getInstance().getMessage("Methods.desc1_1") + "</b> "
      /*
      + "Method steps describe a "
      + "single step in the implementation of a methodology for an "
      + "experiment." 
      */
      + Language.getInstance().getMessage("Methods.desc1_2")
      + "</p>", 1);
    vbox.add(desc1);
    vbox.add(WidgetFactory.makeHalfSpacer());

    methodsList = WidgetFactory.makeList(colNames, editors, 4,
                                         true, true, false, true, true, true);

    methodsList.setBorder(new javax.swing.border.EmptyBorder(0,
        WizardSettings.PADDING, 0, 0));
    vbox.add(methodsList);

    //vbox.add(WidgetFactory.makeHalfSpacer());
    //vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel studyDesc = WidgetFactory.makeHTMLLabel(
        "<p>" 
        + /*"<b>Study extent description</b>. "*/ "<b>" + Language.getInstance().getMessage("Methods.studyDesc_1") + "</b> "
        + /*"Describe the temporal, spatial and taxonomic extent of the study.  " */ Language.getInstance().getMessage("Methods.studyDesc_2") + " "
        + /*"This information supplements the coverage information you may have provided in a previous step."*/ Language.getInstance().getMessage("Methods.studyDesc_3")
        + "</p>", 3);
    vbox.add(studyDesc);

    JPanel studyPanel = WidgetFactory.makePanel(10);
    studyLabel = WidgetFactory.makeLabel(/*" Study Extent"*/ " " + Language.getInstance().getMessage("StudyExtent"), false);
    studyPanel.add(studyLabel);

    studyArea = WidgetFactory.makeTextArea("", 3, true);
    JScrollPane jStudyScrl = new JScrollPane(studyArea);
    studyPanel.add(jStudyScrl);

    vbox.add(studyPanel);
    //vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel sampleDesc = WidgetFactory.makeHTMLLabel(
        /*"<b>Sampling description</b>. "*/ "<b>" + Language.getInstance().getMessage("sampleDesc_1") + "</b> " 
        /*+ "Describe the sampling design of the study."*/ + Language.getInstance().getMessage("sampleDesc_2") + " "
        
        /*+ " For example, you might describe the way in which treatments were "
        + "assigned to sampling units."*/
        + Language.getInstance().getMessage("sampleDesc_3")
        , 2);
    vbox.add(sampleDesc);

    JPanel samplePanel = WidgetFactory.makePanel(10);
    sampleLabel = WidgetFactory.makeLabel(/*" Sampling"*/ " " + Language.getInstance().getMessage("Sampling") , false);
    samplePanel.add(sampleLabel);

    sampleArea = WidgetFactory.makeTextArea("", 3, true);
    JScrollPane jSampleScrl = new JScrollPane(sampleArea);
    samplePanel.add(jSampleScrl);

    vbox.add(samplePanel);
    //vbox.add(WidgetFactory.makeDefaultSpacer());

    warningPanel = WidgetFactory.makePanel(1);
    warningLabel = WidgetFactory.makeLabel(
        "Warning: at least one of the three "
        + "entries is required: Last Name, Position Name or Organization", true);
    warningPanel.add(warningLabel);
    warningPanel.setVisible(false);
    setPrefMinMaxSizes(warningLabel, FULL_LABEL_DIMS);
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

	  WizardPageLibrary library = new WizardPageLibrary(null);
    MethodsPage methodsPage = (MethodsPage) library.getPage(
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

    if (selRowList == null || selRowList.size() < 4) {
      return;
    }

    Object dialogObj = selRowList.get(3);

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
      warningLabel.setText(
    		  			   /*"Method steps are required if you provide either a "
                           + "study extent or smapling description"*/
                           Language.getInstance().getMessage("Methods.Warning_1")
                           );
      warningPanel.setVisible(true);
      return false;
    }

    if ( (studyArea.getText().trim().compareTo("") == 0 &&
          sampleArea.getText().trim().compareTo("") != 0)) {

      warningLabel.setText(
    		  			   /*"Study extent is required if you provide "
                           + "sampling description"*/
    		  				Language.getInstance().getMessage("Methods.Warning_2")
                           );
      warningPanel.setVisible(true);
      return false;
    }
    if ( (studyArea.getText().trim().compareTo("") != 0 &&
          sampleArea.getText().trim().compareTo("") == 0)) {

      warningLabel.setText(
    		  			   /*"Sampling description is required if you provide "
                           + "study extent"*/
    		               Language.getInstance().getMessage("Methods.Warning_3")
                           );
      warningPanel.setVisible(true);
      return false;
    }
    
    //in correctionwizard, we need check every methodstep in methodList.
    //since it necessary that the methodstep is valid (may have whitespace string)
    if(checkMethodStep && methodsList != null)
    {
    	List methodStepRowList = methodsList.getListOfRowLists();
    	for(int i= 0; i<methodStepRowList.size(); i++)
    	{
             List singleRow = (List)methodStepRowList.get(i);
             MethodsPage page = (MethodsPage)singleRow.get(3);
             if (page == null)
             {
            	 continue;
             }
             OrderedMap map = page.getPageData("");
             boolean check= page.mapContainsRequirePath(map, "");
    		 if(check == false)
    		 {
    			 warningLabel.setText("Method step at row  "+(i+1)
                         + " misses some requried fields. Please select it and use Edit button to edit it");
                 warningPanel.setVisible(true);
                 return false;
    		 }
    	}
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
    return getPageData(XPATH_ROOT);
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

    int index = 1;
    Object nextRowObj = null;
    List nextRowList = null;
    Object nextUserObject = null;
    OrderedMap nextNVPMap = null;
    MethodsPage nextMethodsPage = null;

    List rowLists = methodsList.getListOfRowLists();
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
      if (nextRowList.size() < 4) {
        continue;
      }
      nextUserObject = nextRowList.get(3);
      if (nextUserObject == null) {
        continue;
      }

      nextMethodsPage = (MethodsPage) nextUserObject;

      nextNVPMap = nextMethodsPage.getPageData(
          rootXPath + "methodStep[" + (index++) + "]");
      returnMap.putAll(nextNVPMap);
    }

    String study = studyArea.getText().trim();
    if (study != null && !study.equals(EMPTY_STRING)) {
      returnMap.put(rootXPath + STUDY_REL_XPATH, study);
    }

    String sample = sampleArea.getText().trim();
    if (sample != null && !sample.equals(EMPTY_STRING)) {
      returnMap.put(rootXPath + SAMPLING_REL_XPATH, sample);
    }

    return returnMap;
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

  // resets all fields to blank
  private void resetBlankData() {

    studyArea.setText("");
    sampleArea.setText("");
    methodsList.removeAllRows();

  }

  public boolean setPageData(OrderedMap map, String _xPathRoot) {

    if (_xPathRoot != null && _xPathRoot.trim().length() > 0) {
      this.xPathRoot = _xPathRoot;
    }
    Log.debug(45,
            "=============Methods.setPageData() called with xPathRoot = " + xPathRoot
            + "\n Map = \n" + map);

    if (map == null || map.isEmpty()) {
      this.resetBlankData();
      return true;
    }

    List toDeleteList = new ArrayList();
    Iterator keyIt = map.keySet().iterator();
    Object nextXPathObj = null;
    String nextXPath = null;
    Object nextValObj = null;
    String nextVal = null;

    List methodstepList = new ArrayList();

    while (keyIt.hasNext()) {

      nextXPathObj = keyIt.next();
      if (nextXPathObj == null) {
        continue;
      }
      nextXPath = (String) nextXPathObj;

      nextValObj = map.get(nextXPathObj);
      nextVal = (nextValObj == null) ? "" : ( (String) nextValObj).trim();

      Log.debug(45, "Methods:  nextXPath = " + nextXPath
                + "\n nextVal   = " + nextVal);

      // remove everything up to and including the last occurrence of
      // this.xPathRoot to get relative xpaths, in case we're handling a
      // project elsewhere in the tree...
      nextXPath = nextXPath.substring(nextXPath.lastIndexOf(this.xPathRoot)
                                      + this.xPathRoot.length());

      Log.debug(45, "Methods: TRIMMED nextXPath   = " + nextXPath);

      if (nextXPath.startsWith(SAMPLING_REL_XPATH)) {

        sampleArea.setText(nextVal);
        toDeleteList.add(nextXPathObj);

      }
      else if (nextXPath.startsWith(STUDY_REL_XPATH)) {

        studyArea.setText(nextVal);
        toDeleteList.add(nextXPathObj);

      }
      else if (nextXPath.startsWith(METHODSTEP_REL_XPATH)) {

        Log.debug(45, ">>>>>>>>>> adding to methodstepList: nextXPathObj="
                  + nextXPathObj + "; nextValObj=" + nextValObj);
        addToMethodStep(nextXPathObj, nextValObj, methodstepList);
        toDeleteList.add(nextXPathObj);
      }
    }

    Iterator persIt = methodstepList.iterator();
    Object nextStepMapObj = null;
    OrderedMap nextStepMap = null;
    int methodPredicate = 1;

    methodsList.removeAllRows();
    boolean methodRetVal = true;

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
      MethodsPage nextStep = (MethodsPage) library.getPage(
          DataPackageWizardInterface.METHODS_PAGE);

      boolean checkMethod = nextStep.setPageData(nextStepMap,
                                                 this.xPathRoot
                                                 + METHODSTEP_REL_XPATH
                                                 + (methodPredicate++) + "]/");

      if (!checkMethod) {
        methodRetVal = false;
      }
      List newRow = nextStep.getSurrogate();
      newRow.add(nextStep);

      methodsList.addRow(newRow);
    }
    //check method return valuse...
    if (!methodRetVal) {

      Log.debug(20, "Methods.setPageData - Method sub-class returned FALSE");
    }

    //remove entries we have used from map:
    Iterator dlIt = toDeleteList.iterator();
    while (dlIt.hasNext()) {
      map.remove(dlIt.next());

      //if anything left in map, then it included stuff we can't handle...
    }
    boolean returnVal = map.isEmpty();

    if (!returnVal) {

      Log.debug(20, "Project.setPageData returning FALSE! Map still contains:"
                + map);
    }
    return (returnVal && methodRetVal);

  }

  private void addToMethodStep(Object nextPersonnelXPathObj,
                             Object nextPersonnelVal, List methodstepList) {

      if (nextPersonnelXPathObj == null)return;
      String nextPersonnelXPath = (String) nextPersonnelXPathObj;
      int predicate = getFirstPredicate(nextPersonnelXPath, METHODSTEP_REL_XPATH);

  // NOTE predicate is 1-relative, but List indices are 0-relative!!!
      if (predicate >= methodstepList.size()) {

        for (int i = methodstepList.size(); i <= predicate; i++) {
          methodstepList.add(new OrderedMap());
        }
      }

      if (predicate < methodstepList.size()) {
        Object nextMapObj = methodstepList.get(predicate);
        OrderedMap nextMap = (OrderedMap) nextMapObj;
        nextMap.put(nextPersonnelXPathObj, nextPersonnelVal);
      }
      else {
        Log.debug(15,
            "**** ERROR - Methods.addToMethodStep() - predicate > methodstepList.size()");
      }
    }

    private int getFirstPredicate(String xpath, String firstSegment) {

      String tempXPath
          = xpath.substring(xpath.indexOf(firstSegment) + firstSegment.length());

      return Integer.parseInt(
          tempXPath.substring(0, tempXPath.indexOf("]")));
    }
}
