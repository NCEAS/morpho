/**
 *  '$RCSfile: PartyCreator.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-05 22:29:54 $'
 * '$Revision: 1.5 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.utilities.OrderedMap;

public class PartyCreator extends AbstractWizardPage{

  public final String pageID     = WizardPageLibrary.PARTY_CREATOR;
  public final String nextPageID = WizardPageLibrary.USAGE_RIGHTS;
  public final String title      = "Dataset Associated Parties:";
  public final String subtitle   = "Creators";
  
  private final String[] colNames =  {"Party", "Role", "Address"};
  private final Object[] editors  =   null; //makes non-directly-editable

  private JLabel minRequiredLabel;
  private CustomList creatorList;
    
  public PartyCreator() { init(); }
  
  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    
    this.setLayout(new BorderLayout());
  
    JLabel desc = WidgetFactory.makeHTMLLabel(
      "<p>CREATOR: The full name of the person, organization or position who "
      +"created the resource.  The list of creators for a resource represent "
      +"the people and organizations who should be cited for the resource"
      +"<br></br></p>", 3);
    this.add(desc, BorderLayout.NORTH);
    
    JPanel vPanel = WidgetFactory.makeVerticalPanel(6);
    
    minRequiredLabel = WidgetFactory.makeLabel(
                              "A minimum of 1 creator must be defined:", true, 
                              WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
    vPanel.add(minRequiredLabel);
    
    creatorList = WidgetFactory.makeList(colNames, editors, 4,
                                    true, true, false, true, true, true );

    vPanel.add(WidgetFactory.makeDefaultSpacer());

    vPanel.add(creatorList);
    
    this.add(vPanel, BorderLayout.CENTER);
    
    initActions();
  }

  
  /** 
   *  Custom actions to be initialized for list buttons
   */
  private void initActions() {
  
    creatorList.setCustomAddAction( 
      
      new AbstractAction() {
    
        public void actionPerformed(ActionEvent e) {
      
          Log.debug(45, "\nPartyCreator: CustomAddAction called");
          showNewPartyDialog();
        }
      });
  
    creatorList.setCustomEditAction( 
      
      new AbstractAction() {
    
        public void actionPerformed(ActionEvent e) {
      
          Log.debug(45, "\nPartyCreator: CustomEditAction called");
          showEditPartyDialog();
        }
      });
  }
  
  private void showNewPartyDialog() {
    
    PartyDialog partyDialog 
            = new PartyDialog(WizardContainerFrame.frame, PartyDialog.CREATOR);

    if (partyDialog.USER_RESPONSE==PartyDialog.OK_OPTION) {
    
      List newRow = partyDialog.getSurrogate();
      newRow.add(partyDialog);
      creatorList.addRow(newRow);
    }
    WidgetFactory.unhiliteComponent(minRequiredLabel);
  }
  

  private void showEditPartyDialog() {
    
    List selRowList = creatorList.getSelectedRowList();
    
    if (selRowList==null || selRowList.size() < 4) return;
    
    Object dialogObj = selRowList.get(3);
    
    if (dialogObj==null || !(dialogObj instanceof PartyDialog)) return;
    PartyDialog editPartyDialog = (PartyDialog)dialogObj;

    editPartyDialog.resetBounds();
    editPartyDialog.setVisible(true);
    
    if (editPartyDialog.USER_RESPONSE==PartyDialog.OK_OPTION) {
    
      List newRow = editPartyDialog.getSurrogate();
      newRow.add(editPartyDialog);
      creatorList.replaceSelectedRow(newRow);
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
  
    WidgetFactory.unhiliteComponent(minRequiredLabel);
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
  
    if (creatorList.getRowCount() < 1) {
    
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
    PartyDialog nextPartyDialog = null;
    
    List rowLists = creatorList.getListOfRowLists();
    
    if (rowLists==null) return null;
    
    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
    
      nextRowObj = it.next();
      if (nextRowObj==null) continue;
      
      nextRowList = (List)nextRowObj;
      //column 3 is user object - check it exists and isn't null:
      if (nextRowList.size()<4)     continue;
      nextUserObject = nextRowList.get(3);
      if (nextUserObject==null) continue;
      
      nextPartyDialog = (PartyDialog)nextUserObject;
      
      nextNVPMap = nextPartyDialog.getPageData("/eml:eml/dataset/creator["
                                                                +(index++)+"]");
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

}