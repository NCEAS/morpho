/**
 *  '$RCSfile: Introduction.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-24 02:54:10 $'
 * '$Revision: 1.7 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;

public class Introduction extends AbstractWizardPage {

  public final String pageID     = WizardPageLibrary.INTRODUCTION;
//  public final String nextPageID = WizardPageLibrary.DATA_LOCATION;
//  
  public final String nextPageID = WizardPageLibrary.GENERAL;
//////////////////////////////////////////////////////////

  public final String title      = "Welcome to the Data Package Wizard";
  public final String subtitle   = " ";
  

  public Introduction() {
    
    init();
  }
  
  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    
    JLabel desc = WidgetFactory.makeHTMLLabel(
    "<p>This wizard will walk you through the steps of creating a data package. "
    +"A number of components will be created, some of which are required, others "
    +"not. Required fields are shown in red, whereas optional fields are shown "
    +"in black.</p><br></br>"
    +"<p>The following items will be needed to create a data package:</p><br></br><ul> "
    +"<li>General Information: <br></br>Title, Abstract, Keywords, Associated People, and "
    +"Usage Rights<br></br></li>"
    +"<li>Data Entity Information: <br></br>What is the structure of your data tables, and "
    +"what are the column definitions, units, delimiters, etc.<br></br></li>"
    +"<li>Method Information:  <br></br>What procedures were employed during data "
    +"collection, during post processing, and during quality control steps?<br></br></li>"
    +"<li>Coverage Information: <br></br>What are temporal, geographic, and taxonomic "
    +"extents of the the data being described?<br></br></li>"
    +"<li>Access Information: <br></br>Who will have access to the data, and what "
    +"permissions will they be granted?<br></br></li>"
    +"<li>Distribution Information: <br></br>Are the data accessible online, or via "
    +"offline medium (i.e. CDROM, etc.)?<br></br></li></ul>", 10);

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

}