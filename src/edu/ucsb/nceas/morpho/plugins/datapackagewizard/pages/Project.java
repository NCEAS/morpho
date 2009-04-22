/**
 *  '$RCSfile: Project.java,v $'
 *    Purpose: A class for showing project screen
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-22 04:37:47 $'
 * '$Revision: 1.48 $'
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
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

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

import org.w3c.dom.Node;

public class Project extends AbstractUIPage {

  public final String pageID     = DataPackageWizardInterface.PROJECT;

  public final String title      = "Research Project Information";
  public final String subtitle   = " ";
  public final String pageNumber = "8";

  private JPanel checkBoxPanel;
  private JPanel dataPanel;
  private JPanel noDataPanel;
  private JPanel currentPanel;
  private JPanel minRequireLabelPanel = WidgetFactory.makeVerticalPanel(3);
 

  private final String PROJECT_ROOT        = "project/";
  private final String XPATH_ROOT          = "/eml:eml/dataset[1]/" + PROJECT_ROOT;

  private final String TITLE_REL_XPATH     = "title[1]";
  private final String FUNDING_REL_XPATH   = "funding[1]/para[1]";
  private final String PERSONNEL_REL_XPATH = "personnel";
  private final String DATAPACKAGE_PERSONNEL_GENERIC_NAME = "personnel";

  private String xPathRoot = PROJECT_ROOT;

  private final String[] buttonsText = new String[] {
      "This project is part of a larger umbrella research project."
  };

  private JLabel      titleLabel;
  private JTextField  titleField;
  private JLabel      fundingLabel;
  private JTextField  fundingField;
  private JLabel minRequiredLabel;
  private CustomList  partiesList;
  private final String[] colNames =  {"Party", "Role", "Address"};
  private final Object[] editors  =   null; //makes non-directly-editable
  private boolean checkPersonnel = false;



  public Project() {
	nextPageID = DataPackageWizardInterface.USAGE_RIGHTS;
    init();
  }
  
  /**
   * This Constructor will check personnel contains required the path
   * @param checkPersonnel
   */
  public Project(Boolean checkPersonnel)
  {
	  this();
	  try
	  {
	    this.checkPersonnel = checkPersonnel.booleanValue();
	  }
	  catch(Exception e)
	  {
		  Log.debug(30, "couldn't get the boolean value for "+checkPersonnel);
	  }
	 	  
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
        "<b>Is your project part of a larger umbrella research project?</b> "
        +"Data may be collected as part of a large research program with many "
        +"sub-projects or they may be associated with a single, independent "
        +"investigation. For example, a large NSF grant may provide funds for "
        +"several primary investigators to collect data at various locations. ",
        4);

    topBox.add(WidgetFactory.makeHalfSpacer());
    topBox.add(desc);


    final JPanel instance = this;
    ItemListener checkBoxListener = new ItemListener(){
      public void itemStateChanged(ItemEvent e) {
        Log.debug(45, "got checkBox command: "+e.getStateChange());
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

    checkBoxPanel
        = WidgetFactory.makeCheckBoxPanel(buttonsText, -1, checkBoxListener);
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
      "<b>Enter the funding sources that support this project.</b> This"
      + " may include agency names and grant or contract numbers.", 2);

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
      "<b>Enter the personnel information</b>. The full name of the people or "
      +"organizations responsible for the project.", 2);
    panel.add(desc);
    minRequiredLabel = WidgetFactory.makeLabel(
                                " One or more Personnel must be defined:", true,
                                WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
    minRequireLabelPanel.add(minRequiredLabel);
    panel.add(minRequireLabelPanel);
    //vPanel.add(minRequiredLabel);
    JPanel vPanel = WidgetFactory.makeVerticalPanel(9);
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

    partiesList.setCustomDeleteAction(

        new AbstractAction() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "\nResearchProjInfo: CustomDeleteAction called");
        deleteParty(((CustomList)e.getSource()));
      }
    });
  }





  /**
   *
   */
  private void showNewPartyDialog() {

    PartyPage partyPage
        = (PartyPage)WizardPageLibrary.getPage(
        DataPackageWizardInterface.PARTY_PERSONNEL);

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
                                                       DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                                       DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                                         DataPackageWizardInterface.PARTY_PERSONNEL);

    }
    WidgetFactory.unhiliteComponent(minRequiredLabel);
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
               map, "/" + DATAPACKAGE_PERSONNEL_GENERIC_NAME);
         }
       }
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

         if (editPartyPage.editingOriginalRef) {

           //have been editing an original reference via another party's dialog, so
           //if the original ref is in this current page's list, update its
           //PartyPage object before we write it to DOM...
           updateOriginalRefPartyPage(editPartyPage);
         }
         //update datapackage...
         DataPackageWizardPlugin.updateDOMFromPartiesList(partiesList,
                                                          DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                                          DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                                         DataPackageWizardInterface.PARTY_PERSONNEL);

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



         Node retval
             = ReferencesHandler.deleteOriginalReferenceSubtree(adp, page.getRefID());

         if (retval==null) {

           //this means that the deleteOriginalReferenceSubtree() method didn't
           //delete the subtree from the dom, so we have to do it ourselves...
           partiesList.removeRow(partiesList.getSelectedRowIndex());

           DataPackageWizardPlugin.updateDOMFromPartiesList(partiesList,
                                                            DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                                            DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                                         DataPackageWizardInterface.PARTY_PERSONNEL);

         }

       }
       Log.debug(45, "AFTER: adp=" + adp);

       //Do not update datapackage as we do for add/edit, because we've already
       //manipulated the DOM directly

       DataPackageWizardPlugin.updatePartiesListFromDOM(partiesList,
                                                 DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                                 DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                                         DataPackageWizardInterface.PARTY_PERSONNEL);
     }



  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

    DataPackageWizardPlugin.updatePartiesListFromDOM(partiesList,
                                              DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                              DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                                         DataPackageWizardInterface.PARTY_PERSONNEL);
    WidgetFactory.unhiliteComponent(titleLabel);
    WidgetFactory.unhiliteComponent(minRequiredLabel);
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {

    DataPackageWizardPlugin.updateDOMFromPartiesList(partiesList,
                                   DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                   DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                   DataPackageWizardInterface.PARTY_PERSONNEL);
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
	  
	  if (checkPersonnel)
	  {
		  // check personnel has all requirement path if needed
		  boolean check = checkPartiesList(partiesList, this.xPathRoot+ PERSONNEL_REL_XPATH,
	              DataPackageWizardInterface.PARTY_PERSONNEL);
		  if(check==false)
		  {
			  WidgetFactory.hiliteComponent(minRequiredLabel);
			  validate();
			  return check;
		  }
		  WidgetFactory.unhiliteComponent(minRequiredLabel);
	  }
    if (currentPanel == dataPanel) {

      //if (titleField.getText().trim().equals("")) {
      if (Util.isBlank(titleField.getText())) {
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
    DataPackageWizardPlugin.updateDOMFromPartiesList(partiesList,
                                   DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                   DATAPACKAGE_PERSONNEL_GENERIC_NAME,
                                   DataPackageWizardInterface.PARTY_PERSONNEL);

    return true;
  }


  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   *
   * @return   map the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData(String rootXPath) {

    if (rootXPath==null) rootXPath = XPATH_ROOT;
    rootXPath = rootXPath.trim();
    if (rootXPath.length() < 1) rootXPath = XPATH_ROOT;
    if (!rootXPath.endsWith("/")) rootXPath += "/";

    returnMap.clear();
//    updatePartiesListFromDOM();

    if (currentPanel == dataPanel) {

      returnMap.put(rootXPath + TITLE_REL_XPATH, titleField.getText().trim());

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

        nextNVPMap = nextPartyPage.getPageData(rootXPath + PERSONNEL_REL_XPATH + "["
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
   * @return map the Map object that contains all the key/value paired
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



  public boolean setPageData(OrderedMap map, String _xPathRoot) {

    if (_xPathRoot!=null && _xPathRoot.trim().length() > 0) this.xPathRoot = _xPathRoot;
    Log.debug(40, "Map at the begining of setPageData with xpathRoot" +_xPathRoot+map.toString());
    JCheckBox checkBox = ((JCheckBox)(checkBoxPanel.getComponent(0)));

    checkBox.setSelected(true);

    if (map==null || map.isEmpty()) {
      this.resetBlankData();
      return true;
    }

    List toDeleteList = new ArrayList();
    Object nextXPathObj = null;
    String nextXPath = null;
    Object nextValObj = null;
    String nextVal = null;

    Iterator keyIt = map.keySet().iterator();
    List personnelList = new ArrayList();

    while (keyIt.hasNext()) {

      nextXPathObj = keyIt.next();
      if (nextXPathObj == null)continue;
      nextXPath = (String)nextXPathObj;

      nextValObj = map.get(nextXPathObj);
      nextVal = (nextValObj == null) ? "" : ((String)nextValObj).trim();

      // remove everything up to and including the last occurrence of
      // this.xPathRoot to get relative xpaths, in case we're handling a
      // project elsewhere in the tree...
      nextXPath = nextXPath.substring(nextXPath.lastIndexOf(this.xPathRoot)
                                      + this.xPathRoot.length());

      Log.debug(45, "Project: TRIMMED nextXPath   = " + nextXPath);

      if (nextXPath.startsWith(TITLE_REL_XPATH)) {

        titleField.setText(nextVal);
        toDeleteList.add(nextXPathObj);

      } else if (nextXPath.startsWith(FUNDING_REL_XPATH)) {

        fundingField.setText(nextVal);
        toDeleteList.add(nextXPathObj);

      } else if (nextXPath.startsWith(PERSONNEL_REL_XPATH + "[")) {

        Log.debug(45,">>>>>>>>>> adding to personnelList: nextXPathObj="
                  +nextXPathObj+"; nextValObj="+nextValObj);
        addToPersonnel(nextXPathObj, nextValObj, personnelList);
        toDeleteList.add(nextXPathObj);

      } else if (nextXPath.startsWith("@scope")) {

        //get rid of scope attribute, if it exists
        toDeleteList.add(nextXPathObj);
      }
    }

    boolean partyRetVal
        = DataPackageWizardPlugin.populatePartiesList(partiesList,
                                                      personnelList,
                                                      this.xPathRoot
                                                      + PERSONNEL_REL_XPATH,
                                                      DataPackageWizardInterface.
                                                      PARTY_PERSONNEL);

    //check party return values...
    if (!partyRetVal) {
      Log.debug(20, "Project.setPageData - Party sub-class returned FALSE");
    }

    //remove entries we have used from map:
    Iterator dlIt = toDeleteList.iterator();
    while (dlIt.hasNext()) map.remove(dlIt.next());

    //if anything left in map, then it included stuff we can't handle...
    boolean returnVal = map.isEmpty();

    if (!returnVal) {

      Log.debug(20, "Project.setPageData returning FALSE! Map still contains:"
                + map);
    }
    return (returnVal && partyRetVal);
  }

   
    /*
     * Check if there is party which doesn't have required fields.
     */
	private  boolean checkPartiesList(CustomList partiesCustomList,
	          String partyXPathRoot,
	          String pageType) {
	    String message1 = "Personnel(s) at row(s) ";
	    String message2 ="";
	    String message3 =  " miss(es) some required fields. Please edit it(them)";
		OrderedMap nextPersonnelMap = null;
		boolean partyRetVal = true;
		
		if (!partyXPathRoot.startsWith("/")) 
		{
			partyXPathRoot = "/" + partyXPathRoot;
		}
		if (!partyXPathRoot.endsWith("["))
		{
			partyXPathRoot = partyXPathRoot + "[";
		}
		
		 boolean first = true;
		 int index = 1;
		 String comma = ",";
		 for (Iterator it = partiesList.getListOfRowLists().iterator(); it.hasNext(); ) 
		 {

	         List nextRowList = (List)it.next();
	         //column 3 is user object - check it exists and isn't null:
	         if (nextRowList.size() < 4)continue;
	         PartyPage nextPage = (PartyPage)nextRowList.get(3);
	         if (nextPage == null)
	         {
	        	 continue;
	         }
	         OrderedMap map= nextPage.getPageData("");
	         boolean check = nextPage.mapContainsRequirePath(map, "", pageType);
	         if(check == false)
	         {
	        	 if(!first)
	        	 {
	        		 message2 = message2+comma+index;
	        	 }
	        	 else
	        	 {
	        		 message2=message2+index;
	        		 first = false;
	        	 }
	        	 partyRetVal = false;
	         }
	         index++;
		
		 }
		if(partyRetVal == false)
		{
			  minRequireLabelPanel.remove(minRequiredLabel);
       	      minRequiredLabel = WidgetFactory.makeLabel(
                    message1+message2+message3, true,
                    WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
       	      minRequireLabelPanel.add(minRequiredLabel);
		}
		
	    return partyRetVal;
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
    int predicate = getFirstPredicate(nextPersonnelXPath, PERSONNEL_REL_XPATH + "[");

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
