/**
 *  '$RCSfile: WizPage02.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-07-30 19:44:01 $'
 * '$Revision: 1.6 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.Map;

import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Rectangle;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;


public class WizPage02 extends AbstractWizardPage{
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private final String pageID     = WizardPageLibrary.PAGE02_ID;
  private final String nextPageID = null;
  private final String title      = "General Dataset Information:";
  private final String subtitle   = "Title and Abstract";
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private JTextField  titleField;  
  private JTextArea   absField;
  private JLabel      titleLabel;
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public WizPage02() { init(); }
  JPanel titlePanel;
  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel vbox = this;
    
    vbox.add(WidgetFactory.makeDefaultSpacer());

    JTextArea desc = WidgetFactory.makeTextArea(
      "Each data package may contain multiple data tables, associated "
      +"methods, and project information.  The title and abstract are "
      +"used to provide a quick summary of the data package in terms of its "
      +"purpose and the components being described.", 2, false);
    JPanel descPanel = WidgetFactory.makePanel(2);
    descPanel.add(desc);
    vbox.add(descPanel);
    
    vbox.add(WidgetFactory.makeDefaultSpacer());
    vbox.add(WidgetFactory.makeDefaultSpacer());
    
    JTextArea titleDesc = WidgetFactory.makeTextArea(
        "Enter a descriptive title for the data package as a whole. A rule of "
        +"thumb is to include organization and project scope information.",
        1, false);
    JPanel tdPanel = WidgetFactory.makePanel(1);
    tdPanel.add(titleDesc);
    vbox.add(tdPanel);
    
    titlePanel = WidgetFactory.makePanel(1);
    
    titleLabel = WidgetFactory.makeLabel("Title", true);

    titlePanel.add(titleLabel);
    
    titleField = WidgetFactory.makeOneLineTextField();
    titlePanel.add(titleField);
    
    vbox.add(titlePanel);
    
    vbox.add(WidgetFactory.makeDefaultSpacer());
    vbox.add(WidgetFactory.makeDefaultSpacer());
    
    ////////////////////////////////////////////////////////////////////////////
    
    JTextArea absDesc = WidgetFactory.makeTextArea(
    "Enter a descriptive abstract paragraph that summarizes the "
    +"purpose and scope of the dataset", 1, false);
    JPanel absPanel = WidgetFactory.makePanel(1);
    absPanel.add(absDesc);
    vbox.add(absPanel);
        
    JPanel abstractPanel = WidgetFactory.makePanel();

    JLabel absLabel = WidgetFactory.makeLabel("Abstract", false);
    absLabel.setAlignmentY(SwingConstants.TOP);
    abstractPanel.add(absLabel);
    
    absField = WidgetFactory.makeTextArea("", 18, true);
    JScrollPane jscrl = new JScrollPane(absField);
    abstractPanel.add(jscrl);
    vbox.add(abstractPanel);
    
    vbox.add(WidgetFactory.makeDefaultSpacer());
    
  }
  
  
  
  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

    WidgetFactory.unhiliteComponent(titleLabel);
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
  public boolean onAdvanceAction() {
    
    if (titleField.getText().trim().equals("")) {
      
      WidgetFactory.hiliteComponent(titleLabel);
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

  public OrderedMap getPageData() {
    
    returnMap.clear();
    
    returnMap.put("/eml:eml/dataset/title[1]", titleField.getText().trim());
    
    if ( !(absField.getText().trim().equals("")) ) {
      
      returnMap.put("/eml:eml/dataset/abstract/section/para[1]", 
                    absField.getText().trim());
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