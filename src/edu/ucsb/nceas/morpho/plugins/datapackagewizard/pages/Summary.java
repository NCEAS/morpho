/**
 *  '$RCSfile: Summary.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-10-01 18:22:42 $'
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

import java.util.Map;

import javax.swing.JLabel;

import javax.swing.BoxLayout;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;

public class Summary extends AbstractWizardPage {

  public final String pageID     = WizardPageLibrary.SUMMARY;
  public final String nextPageID = null;
  public final String title      = "Data Package Wizard";
  public final String subtitle   = "Summary";
  private JLabel desc2;

  public Summary() {

    init();
  }
  
  
  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    
    JLabel desc1 = WidgetFactory.makeHTMLLabel(
    "<p>This wizard has now collected all the information that is required to "
    +"create your data package.</p><br></br>", 2);

    
    desc2 = WidgetFactory.makeHTMLLabel(
    "<p><b>"+WizardSettings.getSummaryText()+"</b></p><br></br>", 2);

    
    JLabel desc3 = WidgetFactory.makeHTMLLabel(
    "<p>You can press the \""+WizardSettings.FINISH_BUTTON_TEXT+"\" button to "
    +"create your new data, or you can use the \""
    +WizardSettings.PREV_BUTTON_TEXT+"\" button to return to previous pages "
    +"and change your settings.</p>", 2);

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(desc1);
    this.add(desc2);
    this.add(desc3);
  }

  
  /** 
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
    
    desc2.setText( WizardSettings.HTML_TABLE_LABEL_OPENING
                  +"<p><b>"+WizardSettings.getSummaryText()
                  +this.getDataLocation()
                  +"</b></p><br></br>"
                  +WizardSettings.HTML_TABLE_LABEL_OPENING);
  }  
  
  private String getDataLocation() {
   
    String summaryText = WizardSettings.getSummaryText();
    if (summaryText!=null 
            && (   summaryText.equals(WizardSettings.SUMMARY_TEXT_ONLINE) 
                || summaryText.equals(WizardSettings.SUMMARY_TEXT_INLINE)) ) {
                
      String loc = WizardSettings.getDataLocation();
      if (loc!=null) return "<span style=\"text-decoration: underline;\">"+loc
                                                                    +"</span>";
    }
    return "";
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