/**
 *  '$RCSfile: WizardContainerFrame.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-07-28 22:15:41 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import edu.ucsb.nceas.morpho.util.Log;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener ;

import java.util.Stack;

/**
 *  provides a top-level container for AbstractWizardPage objects. The top (title) panel
 *  and bottom button panel (including the navigation buttons) are all part of 
 *  this class, with the AbstractWizardPage content being nested inside a central area
 */
public class WizardContainerFrame extends JFrame {

  private final Color TOP_PANEL_BG_COLOR = new Color(11,85,112);
  // x-dimension is ignored:
  private final Dimension TOP_PANEL_DIMS = new Dimension(100,60);
  private final int PADDING = 5;
  
  private final Font  TITLE_FONT          = new Font("Sans-Serif", Font.BOLD,  12);
  private final Color TITLE_TEXT_COLOR    = new Color(255,255,255);
  private final Font  SUBTITLE_FONT       = new Font("Sans-Serif", Font.PLAIN, 11);
  private final Color SUBTITLE_TEXT_COLOR = new Color(255,255,255);

  private final Font  BUTTON_FONT         = new Font("Sans-Serif",Font.BOLD,12);
  private final Color BUTTON_TEXT_COLOR   = new Color(51, 51, 51);

  public static final  Font  WIZARD_CONTENT_FONT = new Font("Sans-Serif",Font.PLAIN,11);
  public static final  Color WIZARD_CONTENT_TEXT_COLOR  = new Color(51, 51, 51);

  private final  Color WIZARD_CONTENT_BG_COLOR    = new Color(221, 221, 221);

  private final String FINISH_BUTTON_TEXT  = "Finish";
  private final String NEXT_BUTTON_TEXT    = "Next >";
  private final String PREV_BUTTON_TEXT    = "< Prev";
  private final String CANCEL_BUTTON_TEXT  = "Cancel";
  
  /**
   * Constructor
   */
  public WizardContainerFrame() {
  
    super();

    pageStack   = new Stack();
    pageLib = new WizardPageLibrary();
    init();
    
    this.setVisible(true);
  }

  /** 
   *  sets the wizard content for the center pane
   *
   *  @param content  the String pageID of the wizard content to be loaded into 
   *                  the center pane
   */
  public void setCurrentPage(String pageID) {
  
    Log.debug(45,"setCurrentPage called with String ID: "+pageID);
    if (pageID==null) {
      Log.debug(15,"setCurrentPage called with NULL ID");
      return;
    }
    if (!pageLib.containsPageID(pageID)) {
      Log.debug(15,"setCurrentPage: page library does NOT contain ID: "+pageID);
      return;
    }  
    
    AbstractWizardPage pageForID = pageLib.getPage(pageID);
    
    setCurrentPage(pageForID);
  }


  /** 
   *  sets the wizard content for the center pane
   *
   *  @param content the wizard content for the center pane
   */
  public void setCurrentPage(AbstractWizardPage newPage) {
  
    if (newPage==null) {
      Log.debug(45,"setCurrentPage called with NULL WizardPage");
      return;
    }
    Log.debug(45,"setCurrentPage called with WizardPage ID = "
                                                +newPage.getPageID());
    //make new page current page
    this.currentPage = newPage;

    setpageTitle(newPage.getTitle());
    setpageSubtitle(getCurrentPage().getSubtitle());

    middlePanel.removeAll();
    
    middlePanel.add(getCurrentPage(), BorderLayout.CENTER);
    getCurrentPage().setOpaque(false);
    getCurrentPage().onLoadAction();
    middlePanel.paintComponents(middlePanel.getGraphics());
    updateButtonsStatus();
  }

  /** 
   *  gets the wizard content from the center pane
   *
   *  @return the wizard content from the center pane
   */
  public AbstractWizardPage getCurrentPage() {
  
    return this.currentPage;
  }

  private void updateButtonsStatus() {

    Log.debug(45,"updateButtonsStatus called");
    
    if (getCurrentPage()==null) {
    
      prevButton.setEnabled(false);
      nextFinishButton.setEnabled(false);
      return;
      
    } else {
    
      prevButton.setEnabled(true);
      nextFinishButton.setEnabled(true);
    }
    
    // prev button enable/disable:
    if (pageStack.isEmpty() || pageStack.peek()==null) {
      prevButton.setEnabled(false);
    }
    
    // next/finish button label:
    if (getCurrentPage().getNextPageID()==null) {
      nextFinishButton.setText(FINISH_BUTTON_TEXT);
    } else {
      nextFinishButton.setText(NEXT_BUTTON_TEXT);
    }
  }
  
  
  private void init() {
  
    initContentPane();
    initTopPanel();
    initMiddlePanel();
    initBottomPanel();
    initButtons();
  }

  private void initContentPane() {
  
    contentPane = this.getContentPane();
    contentPane.setLayout(new BorderLayout());
  }


  private void initTopPanel() {
  
    Log.debug(45,"WizardContainerFrame starting init()");
    
    titleLabel = new JLabel("");
    titleLabel.setFont(TITLE_FONT);
    titleLabel.setForeground(TITLE_TEXT_COLOR);
    titleLabel.setBorder(new EmptyBorder(PADDING,0,PADDING,0));
    
    subtitleLabel = new JLabel("");
    subtitleLabel.setFont(SUBTITLE_FONT);
    subtitleLabel.setForeground(SUBTITLE_TEXT_COLOR);
    subtitleLabel.setBorder(new EmptyBorder(PADDING,0,PADDING,0));
    
    topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.setPreferredSize(TOP_PANEL_DIMS);
    topPanel.setBorder(new EmptyBorder(0,3*PADDING,0,3*PADDING));
    topPanel.setBackground(TOP_PANEL_BG_COLOR);
    topPanel.setOpaque(true);
    topPanel.add(titleLabel);
    topPanel.add(subtitleLabel);
    contentPane.add(topPanel, BorderLayout.NORTH);
    
  }
  
  private void initMiddlePanel() {
  
    middlePanel = new JPanel();
    middlePanel.setLayout(new BorderLayout());
    middlePanel.setBackground(WIZARD_CONTENT_BG_COLOR);
//    middlePanel.setOpaque(false);
//    middlePanel.setBorder(new EmptyBorder(PADDING,3*PADDING,PADDING,3*PADDING));
    contentPane.add(middlePanel, BorderLayout.CENTER);
  }
  
  private void initBottomPanel() {
  
    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
    bottomPanel.add(Box.createHorizontalGlue());
    bottomPanel.setOpaque(false);

    bottomPanel.setBorder(
                  BorderFactory.createMatteBorder(2, 0, 0, 0, TOP_PANEL_BG_COLOR));
//    bottomPanel.setBorder(new EmptyBorder(PADDING,3*PADDING,3*PADDING,PADDING));
    contentPane.add(bottomPanel, BorderLayout.SOUTH);
  }
  
  private void initButtons()  {
  
    prevButton        = addButton(PREV_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        previousAction();
      }
    });
    nextFinishButton  = addButton(NEXT_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        nextFinishAction();
      }
    });
    cancelButton      = addButton(CANCEL_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelAction();
      }
    });
  }

  /** 
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. It's up to the content to know 
   *  whether it's the last page or not
   */
  private void nextFinishAction() {
    
    Log.debug(45,"nextFinishAction called");
    
    getCurrentPage().onAdvanceAction();
    
    if (getCurrentPage().getNextPageID()==null) {
      //this is the last page - user just pressed "finish"
      this.setVisible(false);
      
      return;
    }
    
    //put current page on stack
    Log.debug(45,"setCurrentPage pushing currentPage to Stack ("
                                            +getCurrentPage().getPageID()+")");
    pageStack.push(this.getCurrentPage());
    
    String nextPgID = getCurrentPage().getNextPageID();
    Log.debug(45,"nextFinishAction - next page ID is: "+nextPgID);
    
    setCurrentPage(pageLib.getPage(nextPgID));
  }
  

  /** 
   *  The action to be executed when the "Prev" button is pressed
   */
  private void previousAction() {
    
    if (pageStack.isEmpty()) return;
    
    AbstractWizardPage previousPage = (AbstractWizardPage)pageStack.pop();
    if (previousPage==null) {
      Log.debug(15,"previousAction - popped a NULL page from stack");
      return;
    }
    Log.debug(45,"previousAction - popped page with ID: "
                                      +previousPage.getPageID()+" from stack");
    
    getCurrentPage().onRewindAction();
    
    setCurrentPage(previousPage);
  }
    
    
  /** 
   *  The action to be executed when the "Cancel" button is pressed
   */
  private void cancelAction() {
    this.setVisible(false);
  }

  /** 
   *  sets the main title for this page
   *  @param newTitle the page title
   */
  private void setpageTitle(String newTitle) {
    if (newTitle==null) newTitle=" ";
    titleLabel.setText(newTitle);
  }

  /** 
   *  sets the main subtitle for this page
   *
   *  @param newSubTitle the main subtitle
   */
  private void setpageSubtitle(String newSubTitle) {
    if (newSubTitle==null) newSubTitle=" ";
    subtitleLabel.setText(newSubTitle);
  }

  /** 
   *  adds a button with a specified title and action to the bottom panel
   *  @param title text to be shown on the button
   *  @param actionListener the ActionListener that will respond 
   *                       to the button press
   */
  private JButton addButton(String title, ActionListener actionListener) {
  
    JButton button = new JButton(title);
    button.setForeground(BUTTON_TEXT_COLOR);
    button.setFont(BUTTON_FONT);
    if (actionListener!=null) button.addActionListener(actionListener);
    bottomPanel.add(button);
    bottomPanel.add(Box.createHorizontalStrut(PADDING));
    return button;
  }


  // * * *  V A R I A B L E S  * * * * * * * * * * * * * * * * * * * * * * * * * *

  private Container contentPane;
  private JPanel topPanel;
  private JPanel middlePanel;
  private JPanel bottomPanel;
  private JLabel titleLabel, subtitleLabel;
  private JButton nextFinishButton;
  private JButton prevButton;
  private JButton cancelButton;
  private AbstractWizardPage currentPage;
  private Stack pageStack;
  private WizardPageLibrary pageLib;
}
