/**
 *  '$RCSfile: PartyIntro.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-08-03 22:28:15 $'
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

import java.util.Map;

import javax.swing.JLabel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;

public class PartyIntro extends AbstractWizardPage{

  public final String pageID     = WizardPageLibrary.PARTY_INTRO;
  public final String nextPageID = WizardPageLibrary.USAGE_RIGHTS;
  public final String title      = "General Dataset Information:";
  public final String subtitle   = "Responsible Parties";
  

  public PartyIntro() {
    
    init();
  }
  
  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    
    JLabel desc = WidgetFactory.makeHTMLLabel(
    "<p>A data package may have a number of parties involved, which "
    +"generally include roles such as 'creator' and other 'associated parties' "
    +"('principal investigator','author', etc.). </p><br></br>"
    +"<p>Each party may be an individual person, an organization, or potentially "
    +"a named position within an organization.  Types of parties involved "
    +"often range from authors and analysts to technicians and funding "
    +"organizations. </p><br></br>"
    +"<p>The following parties must be described when creating a data package: "
    +"</p><br></br><ul>"
    +"<li>Creator: The full name of the person, organization, or position who "
    +"created the resource. The list of creators for a resource represent the "
    +"people and organizations who should be cited for the resource. "
    +"<br></br>Example: "
    +"For a book, the creators are its authors. <br></br></li>"
    +"<li>Contact:  contains contact information for this dataset. This is the "
    +"person or institution to contact with questions about the use or "
    +"interpretation of a data set. <br></br></li>"
    +"<li>Associated Party: provides the full name of other people, organizations, "
    +"or positions who should be associated with the resource. These parties "
    +"might play various roles in the creation or maintenance of the resource, "
    +"and these roles should be indicated in the \"role\" element. "
    +"<br></br>Example: "
    +"The technician who collected the data.<br></br></li>", 10);
    
    
    this.setLayout(new BorderLayout());
    this.add(desc, BorderLayout.CENTER);
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

  
  
  
  private final Font  defaultFont    
                              = WizardSettings.WIZARD_CONTENT_FONT;
  private final Color defaultFGColor 
                              = WizardSettings.WIZARD_CONTENT_TEXT_COLOR;
}