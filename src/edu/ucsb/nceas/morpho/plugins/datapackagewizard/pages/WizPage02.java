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
 *     '$Date: 2003-07-29 16:56:07 $'
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

import java.util.Map;

import javax.swing.JLabel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
//import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JPanel;

import java.awt.Font;
import java.awt.Dimension;
import java.awt.Color;

//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Insets;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;


public class WizPage02 extends AbstractWizardPage{

  public final String pageID     = WizardPageLibrary.PAGE02_ID;
  public final String nextPageID = WizardPageLibrary.PAGE03_ID;
  public final String title      = "General Dataset Information:";
  public final String subtitle   = "Title and Abstract";
  

  public WizPage02() {
    
    init();
  }
  
  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//    
    this.add(Box.createVerticalGlue());
//    
    JLabel desc = new JLabel(
      "<html><table width=100% align=top><tr><td valign=top align=left> "
      +"<p>Each data package may contain multiple data tables, associated "
      +"methods, and project information.  The title and abstract are "
      +"used to provide a quick summary of the data package in terms of its "
      +"purpose and the components being described.</p></td></tr></table></html>");
    desc.setFont(defaultFont);
    desc.setForeground(defaultFGColor);
    desc.setHorizontalAlignment(SwingConstants.LEFT);
    
    this.add(desc);
    
    this.add(Box.createVerticalStrut(15));
    
    JLabel titleDesc = new JLabel(
      "<html><table width=100% align=top><tr><td valign=top align=left> "
      +"<p>Enter a descriptive title for the data package as a whole. "
      +"A rule of thumb is to include organization and project scope "
      +"information.</p></td></tr></table></html>");
    titleDesc.setFont(defaultFont);
    titleDesc.setForeground(defaultFGColor);
    titleDesc.setHorizontalAlignment(SwingConstants.LEFT);
    
    this.add(titleDesc);

    JPanel titleBox = new JPanel();
    titleBox.setPreferredSize(defaultLabelDims);

    JLabel titleLabel = new JLabel("Title");
    titleLabel.setPreferredSize(defaultLabelDims);
    titleLabel.setFont(defaultFont);
    titleLabel.setForeground(requiredFGColor);
    titleBox.add(titleLabel);

    JTextField titleField = new JTextField();
    titleField.setPreferredSize(defaultTextFieldDims);
    titleField.setFont(defaultFont);
    titleField.setForeground(requiredFGColor);
    titleBox.add(titleField);
    
    this.add(titleBox);
    
    this.add(Box.createVerticalStrut(15));

////////////////////////////////////////////////////////////////////////////

    JLabel absDesc = new JLabel(
      "<html><table width=100% align=top><tr><td valign=top align=left> "
      +"<p>Enter a descriptive abstract paragraph that summarizes the "
      +"purpose and scope of the dataset.</p></td></tr></table></html>");
    absDesc.setFont(defaultFont);
    absDesc.setForeground(defaultFGColor);
    absDesc.setHorizontalAlignment(SwingConstants.LEFT);
    
    this.add(absDesc);
    
    this.add(Box.createVerticalStrut(15));

    JPanel absBox = new JPanel();
    JLabel absLabel = new JLabel("Abstract");
    absLabel.setPreferredSize(defaultLabelDims);
    absLabel.setFont(defaultFont);
    absLabel.setForeground(defaultFGColor);
    absBox.add(absLabel);

    JTextField absField = new JTextField();
    absField.setPreferredSize(defaultLabelDims);
    absField.setFont(defaultFont);
    absField.setForeground(requiredFGColor);
    absBox.add(absField);
    
    this.add(absBox);
    

    this.add(Box.createVerticalGlue());

//
//      GridBagConstraints constraints = new GridBagConstraints();
//      GridBagLayout gridbag = new GridBagLayout();
//      this.setLayout(gridbag);
//
//      constraints.fill = GridBagConstraints.BOTH;
//      constraints.weightx = 1.0;
//      constraints.gridwidth = GridBagConstraints.REMAINDER;
//
//      JLabel desc = new JLabel(
//        "<html><table width=100% align=top><tr><td valign=top align=left> "
//        +"<p>Each data package may contain multiple data tables, associated "
//        +"methods, and project information.  The title and abstract are "
//        +"used to provide a quick summary of the data package in terms of its "
//        +"purpose and the components being described.</p></td></tr></table></html>");
//      desc.setFont(defaultFont);
//      desc.setForeground(defaultFGColor);
//      desc.setHorizontalAlignment((int)(JLabel.LEFT_ALIGNMENT));
//    
//      gridbag.setConstraints(desc, c);
//      
//      this.add(desc);
//
//
//      constraints.gridwidth = GridBagConstraints.RELATIVE;
//      JTextField name = new JTextField("Name:", 25);
//      gridbag.setConstraints(name, c);
//      this.add(name);
//
//      constraints.gridwidth = GridBagConstraints.REMAINDER;
//
//      JTextField addr = new JTextField("Address:", 25);
//      gridbag.setConstraints(addr, c);
//      this.add(addr);
//
//      JTextArea comments = new JTextArea(3, 25);
//      comments.setEditable(true);
//      comments.setText("Comments:");
//      gridbag.setConstraints(comments, c);
//      this.add(comments);
//
//
//      constraints.insets = new Insets(15, 0, 0, 0);
//      constraints.fill=GridBagConstraints.NONE;
//      constraints.gridwidth = GridBagConstraints.RELATIVE;
//      JButton OK = new JButton("OK");
//      gridbag.setConstraints(OK, c);
//      this.add(OK);
//
//      constraints.gridheight=1;
//      JButton cancel = new JButton("Cancel");
//      gridbag.setConstraints(cancel, c);
//      this.add(cancel);
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

  
  
  
  private final Font  defaultFont    
                          = WizardSettings.WIZARD_CONTENT_FONT;
  private final Color defaultFGColor 
                          = WizardSettings.WIZARD_CONTENT_TEXT_COLOR;
  private final Color requiredFGColor 
                          = WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR;
  private final Dimension defaultLabelDims 
                          = WizardSettings.WIZARD_CONTENT_LABEL_DIMS;
  private final Dimension defaultTextFieldDims 
                          = WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS;
}