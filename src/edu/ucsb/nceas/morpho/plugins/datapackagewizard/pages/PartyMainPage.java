/**
 *  '$RCSfile: PartyMainPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2003-11-25 18:03:10 $'
 * '$Revision: 1.2 $'
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

import java.util.List;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.AbstractAction;


import java.awt.event.ActionEvent;

import java.awt.BorderLayout;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.utilities.OrderedMap;

public class PartyMainPage extends AbstractWizardPage{

  public String pageID;
  public String nextPageID;
  
  private final String[] colNames =  {"Party", "Role", "Address"};
  private final Object[] editors  =   null; //makes non-directly-editable
  public final String title      = "Dataset Associated Parties:";
  public final short role;
  
  public String subtitle;
  public String description;
  public String xPathRoot;
  
  private JLabel      minRequiredLabel;
  private CustomList  partiesList;
  private boolean     oneOrMoreRequired;
    
  public PartyMainPage(short role) { 

    this.role = role;
    initRole();
    init(); 
  }
  
  private void initRole() { 
  
    switch (role) {
  
      case PartyPage.CREATOR:
      
        oneOrMoreRequired = true;
        pageID     = DataPackageWizardInterface.PARTY_CREATOR;
        nextPageID = DataPackageWizardInterface.PARTY_CONTACT;
        subtitle = "Creators";
        xPathRoot = "/eml:eml/dataset/creator[";
        description =
        "<p>CREATOR: The full name of the person, organization or position who "
        +"created the resource.  The list of creators for a resource represent "
        +"the people and organizations who should be cited for the resource"
        +"<br></br></p>";
        break;
        
      case PartyPage.CONTACT:

        oneOrMoreRequired = true;
        pageID     = DataPackageWizardInterface.PARTY_CONTACT;
        nextPageID = DataPackageWizardInterface.PARTY_ASSOCIATED;
        subtitle = "Contacts";
        xPathRoot = "/eml:eml/dataset/contact[";
        description =
         "<p>CONTACT:  contains contact information for this dataset. This is "
        +"the person or institution to contact with questions about the use or "
        +"interpretation of a data set."
        +"<br></br></p>";
        break;
        
      case PartyPage.ASSOCIATED:

        oneOrMoreRequired = false;
        pageID     = DataPackageWizardInterface.PARTY_ASSOCIATED;
        nextPageID = DataPackageWizardInterface.USAGE_RIGHTS;
        subtitle = "Associated Parties";
        xPathRoot = "/eml:eml/dataset/associatedParty[";
        description =
         "<p>ASSOCIATED PARTY: the full names of other people, organizations, "
        +"or positions who should be associated with the resource. These "
        +"parties might play various roles in the creation or maintenance of "
        +"the resource, and these roles should be indicated in the \"role\" "
        +"element.<br></br></p>";
        break;
        
      default:
        Log.debug(5, "Unrecognized role parameter passed to PartyPage: "+role);
        return;
    }
  }

  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    
    this.setLayout(new BorderLayout());
  
    JLabel desc = WidgetFactory.makeHTMLLabel(description, 3);
    this.add(desc, BorderLayout.NORTH);
    
    JPanel vPanel = WidgetFactory.makeVerticalPanel(6);
    
    if (oneOrMoreRequired) {
      minRequiredLabel = WidgetFactory.makeLabel(
                                "One or more "+subtitle+" must be defined:", true, 
                                WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
      vPanel.add(minRequiredLabel);
    }
        
    partiesList = WidgetFactory.makeList(colNames, editors, 4,
                                    true, true, false, true, true, true );

    vPanel.add(WidgetFactory.makeDefaultSpacer());

    vPanel.add(partiesList);
    
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
  
  private void showNewPartyDialog() {
    
    PartyPage partyPage = (PartyPage)WizardPageLibrary.getPage(DataPackageWizardInterface.PARTY_PAGE);
    partyPage.setRole(role);
    WizardPopupDialog wpd = new WizardPopupDialog(partyPage, WizardContainerFrame.frame);
    if (wpd.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {
    
      List newRow = partyPage.getSurrogate();
      newRow.add(partyPage);
      partiesList.addRow(newRow);
    } 
    
    if (oneOrMoreRequired) WidgetFactory.unhiliteComponent(minRequiredLabel);
  }
  

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
  public void onLoadAction() {}
  
  
  /** 
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {
  
    if (oneOrMoreRequired) WidgetFactory.unhiliteComponent(minRequiredLabel);
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
  //
  public OrderedMap getPageData() {
  
    returnMap.clear();
    
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
      
      nextNVPMap = nextPartyPage.getPageData(xPathRoot + (index++) + "]");
      returnMap.putAll(nextNVPMap);
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

  public void setPageData(OrderedMap data) { }
}