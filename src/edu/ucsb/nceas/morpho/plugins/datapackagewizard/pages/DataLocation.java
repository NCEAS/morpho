/**
 *  '$RCSfile: DataLocation.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-08-03 22:27:13 $'
 * '$Revision: 1.1 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.awt.BorderLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.BorderFactory;


import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;


public class DataLocation extends AbstractWizardPage{

  private final String pageID     = WizardPageLibrary.INTRODUCTION;
  private String nextPageID       = WizardPageLibrary.GENERAL;
  private String nextPageGenerateID = WizardPageLibrary.TEXT_IMPORT_WIZARD;
  private String nextPageByHandID   = WizardPageLibrary.DATA_FORMAT;
  
  private final String title      = "Data File Information:";
  private final String subtitle   = "Location";
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JPanel inlinePanel;
  private JPanel onlinePanel;
  private JPanel offlinePanel;
  private JPanel noDataPanel;
  private JPanel currentPanel;
  private JLabel radioLabel;
  private final String[] buttonsText = new String[] {
      "Import the data into your new package file",
      "Link to online data using a URL",
      "Enter information about the data, but do not make the data itself available",
      "Do not describe or include any data at this time"
    };
  
  private final String[] genHandButtonsText = new String[] {
      "Generate the information automatically from the online data file (ASCII files only)",
      "Enter the information by hand"
    };

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  public DataLocation() { init(); }
  
  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BorderLayout());
    
    Box topBox = Box.createVerticalBox();
    
    JLabel desc = WidgetFactory.makeHTMLLabel(
      "Select a data file to include or describe in your package. You can "
      +"choose to import the data into your package, reference it externally "
      +"using an online URL, or not include the data at all (in which case you "
      +"may still include information about its format and structure if you wish "
      +"to do so).", 3);
    topBox.add(desc);
    topBox.add(WidgetFactory.makeDefaultSpacer());

    radioLabel = WidgetFactory.makeHTMLLabel("Select a location:", 1);
    topBox.add(radioLabel);

    final JPanel instance = this;
    
    ActionListener listener = new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
        
        Log.debug(45, "got radiobutton command: "+e.getActionCommand());
        
        if (e.getActionCommand().equals(buttonsText[0])) {
          
          if (currentPanel!=null) instance.remove(currentPanel);
          currentPanel = inlinePanel;
          instance.add(inlinePanel, BorderLayout.CENTER);
          
        } else if (e.getActionCommand().equals(buttonsText[1])) {
        
          if (currentPanel!=null) instance.remove(currentPanel);
          currentPanel = onlinePanel;
          instance.add(onlinePanel, BorderLayout.CENTER);
          
        } else if (e.getActionCommand().equals(buttonsText[2])) {
        
          if (currentPanel!=null) instance.remove(currentPanel);
          currentPanel = offlinePanel;
          instance.add(offlinePanel, BorderLayout.CENTER);
          
        } else if (e.getActionCommand().equals(buttonsText[3])) {
        
          if (currentPanel!=null) instance.remove(currentPanel);
          currentPanel = noDataPanel;
          instance.add(noDataPanel, BorderLayout.CENTER);
          
        }
        instance.validate();
        instance.repaint();
      }
    };
    
    
    JPanel radioPanel = WidgetFactory.makeRadioPanel(buttonsText, -1, listener);
    
    topBox.add(radioPanel);
    
    topBox.add(WidgetFactory.makeDefaultSpacer());
    
    this.add(topBox, BorderLayout.NORTH);
    
    inlinePanel  = getInlinePanel();
    onlinePanel  = getOnlinePanel();
    offlinePanel = getOfflinePanel();
    noDataPanel  = getNoDataPanel();

    currentPanel = null;

  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private JPanel getInlinePanel() {
    
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
    panel.setBackground(java.awt.Color.red);
    panel.setOpaque(true);
    
    return panel;
  }
  
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private JLabel      fileNameLabel;
  private JTextField  fileNameField;
  private JLabel      urlLabel;
  private JTextField  urlField;
  
  private JPanel getOnlinePanel() {
    
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
    WidgetFactory.addTitledBorder(panel, buttonsText[1]);
    
    panel.add(WidgetFactory.makeDefaultSpacer());
  
    ////
    JPanel fileNamePanel = WidgetFactory.makePanel(1);
    
    fileNameLabel = WidgetFactory.makeLabel("File Name:", true);

    fileNamePanel.add(fileNameLabel);
    
    fileNameField = WidgetFactory.makeOneLineTextField();
    fileNamePanel.add(fileNameField);
    
    panel.add(fileNamePanel);
    
    panel.add(WidgetFactory.makeDefaultSpacer());
    
    ////
    JPanel urlPanel = WidgetFactory.makePanel(1);
    
    urlLabel = WidgetFactory.makeLabel("URL:", true);

    urlPanel.add(urlLabel);
    
    urlField = WidgetFactory.makeOneLineTextField();
    urlPanel.add(urlField);
    
    panel.add(urlPanel);
    
    panel.add(WidgetFactory.makeDefaultSpacer());
    
    panel.add(WidgetFactory.makeHTMLLabel(
      "How would you like to enter the information describing "
      +"the format and structure of the data?", 1));
  
    panel.add(getGeneratedOrByHandRadioPanel());
    
    panel.add(WidgetFactory.makeDefaultSpacer());
    
    panel.add(Box.createGlue());
    
    return panel;
  }
  
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private JPanel getOfflinePanel() {
    
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
    panel.setBackground(java.awt.Color.blue);
    panel.setOpaque(true);
  
    return panel;
  }
  
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  private JPanel getNoDataPanel() {
    
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
  
  
  
    return panel;
  }
  
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

//    titleField.requestFocus();
    WidgetFactory.unhiliteComponent(radioLabel);
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
    
    if (currentPanel==null)  {
      
      WidgetFactory.hiliteComponent(radioLabel);
      return false;
    }
//    if (titleField.getText().trim().equals("")) {
//      
//      WidgetFactory.hiliteComponent(titleLabel);
//      titleField.requestFocus();
//      return false;
//    }
    return true;
  }
  
  
  /**
   *  gets the OrderedMap object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the OrderedMap object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData() {
    
    returnMap.clear();
    
//    returnMap.put("/eml:eml/dataset/title[1]", titleField.getText().trim());
//    
//    if ( !(absField.getText().trim().equals("")) ) {
//      
//      returnMap.put("/eml:eml/dataset/abstract/section/para[1]", 
//                    absField.getText().trim());
//    }
    return returnMap;
  }
  
  
  
  private JPanel getGeneratedOrByHandRadioPanel() {
  
    return WidgetFactory.makeRadioPanel(genHandButtonsText, 0, 
      new ActionListener() {
    
        public void actionPerformed(ActionEvent e) {
      
          Log.debug(45, "got radiobutton command: "+e.getActionCommand());
      
          if (e.getActionCommand().equals(genHandButtonsText[0])) {
        
            nextPageID = nextPageGenerateID;
        
          } else if (e.getActionCommand().equals(genHandButtonsText[1])) {
      
            nextPageID = nextPageByHandID;
          }
        }
      });
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