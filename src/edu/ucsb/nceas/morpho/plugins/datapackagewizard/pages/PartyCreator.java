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
 *     '$Date: 2003-09-04 04:27:14 $'
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

import java.util.List;

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
  private final Object[] editors  =   null; //makes non-editable

  private CustomList creatorList;
  private PartyDialog partyDialog;
  
  public PartyCreator() {
    
    init();
  }
  
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
    
    creatorList = WidgetFactory.makeList(colNames, editors, 4,
                                    true, true, false, true, true, true );
    this.add(creatorList);
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
          showPartyDialog();
        }
      });
  }
  
  private void showPartyDialog() {
    
    int row = creatorList.getSelectedRow();
    Log.debug(45, "\nPartyCreator: showPartyDialog() BEFORE - thinks selected row = "+row);
    if (row < 0) return;
    
    partyDialog 
            = new PartyDialog(WizardContainerFrame.frame, PartyDialog.CREATOR);
            
    Log.debug(45, "\nPartyCreator: showPartyDialog() AFTER - thinks selected row = "+row);
    Log.debug(45, "\nPartyCreator: showPartyDialog() thinks partyDialog.USER_RESPONSE = "+partyDialog.USER_RESPONSE);

    Log.debug(45, "\nPartyCreator: showPartyDialog() doing creatorList.removeRow("+row+")");
    creatorList.removeRow(row);

    if (partyDialog.USER_RESPONSE==PartyDialog.OK_OPTION) {
    
//      List newRow = (List)(creatorList.getListOfRowLists().get(row));
//      newRow = partyDialog.getSurrogate();
      
      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      //need to create an addrow(List) method for list
      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

//      Log.debug(45, "\nPartyCreator: showPartyDialog() newRow = "+newRow);
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
  public boolean onAdvanceAction() { return true; }
  

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

}