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
 *     '$Date: 2003-07-30 01:09:26 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

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
  
  public final String pageID     = WizardPageLibrary.PAGE02_ID;
  public final String nextPageID = WizardPageLibrary.PAGE03_ID;
  public final String title      = "General Dataset Information:";
  public final String subtitle   = "Title and Abstract";
  
  
  public WizPage02() { init(); }
  
  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
    JTextArea desc = WidgetFactory.makeMultilineTextArea(
      "Each data package may contain multiple data tables, associated "
      +"methods, and project information.  The title and abstract are "
      +"used to provide a quick summary of the data package in terms of its "
      +"purpose and the components being described.", false);
    this.add(desc);
    
    this.add(Box.createRigidArea(defaultSpacerDims));
    
    JTextArea titleDesc = WidgetFactory.makeMultilineTextArea(
    "Enter a descriptive title for the data package as a whole. A rule of "
    +"thumb is to include organization and project scope information.", false);
    this.add(titleDesc);
    
    Box titleBox = Box.createHorizontalBox();
    titleBox.setMaximumSize(singleLineDims);
    
    JLabel titleLabel = WidgetFactory.makeLabel("Title", true);

    titleBox.add(titleLabel);
    
    JTextField titleField = WidgetFactory.makeOneLineTextField();
    titleBox.add(titleField);
    
    this.add(titleBox);
    
    this.add(Box.createRigidArea(defaultSpacerDims));
    
    ////////////////////////////////////////////////////////////////////////////
    
    JTextArea absDesc = WidgetFactory.makeMultilineTextArea(
    "Enter a descriptive abstract paragraph that summarizes the "
    +"purpose and scope of the dataset", false);

    this.add(absDesc);
        
    Box absBox = Box.createHorizontalBox();

    JLabel absLabel = WidgetFactory.makeLabel("Abstract", false);
    absLabel.setAlignmentY(SwingConstants.TOP);
    absBox.add(absLabel);
    
    JTextArea absField = WidgetFactory.makeMultilineTextArea("",true);
    absField.setRows(25);
    absField.setColumns(100);
    JScrollPane jscrl = new JScrollPane(absField);
    absBox.add(jscrl);
    this.add(absBox);
    
    this.add(Box.createRigidArea(defaultSpacerDims));
    
    this.add(Box.createHorizontalGlue());
    
//    this.add(Box.createVerticalGlue());
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
   *  or "Finish" button(last page) is pressed. May be empty
   */
  public void onAdvanceAction() {
    
  }
  
  
  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public Map getPageData() {
    
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
  
  
  private final Dimension defaultSpacerDims
                                = new Dimension(15, 15);
  private final Dimension singleLineDims
                                = WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS; 
  
//  private final Font  defaultFont
//                                = WizardSettings.WIZARD_CONTENT_FONT;
//  private final Color defaultFGColor
//                                = WizardSettings.WIZARD_CONTENT_TEXT_COLOR;
//  private final Color requiredFGColor
//                                = WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR;
//  private final Dimension defaultLabelDims
//                                = WizardSettings.WIZARD_CONTENT_LABEL_DIMS;
//  private final Dimension defaultTextFieldDims
//                                = WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS;

}