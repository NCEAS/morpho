/**
 *  '$RCSfile: DataFormat.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-08-04 23:19:08 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.awt.BorderLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.BorderFactory;


import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;


public class DataFormat extends AbstractWizardPage{

  private final String pageID     = WizardPageLibrary.DATA_FORMAT;
  private String nextPageID       = null;
  
  private final String title      = "Data File Information:";
  private final String subtitle   = "File Format";
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private String orientationSimple;
  private String orientationComplex;
  
  private final String COLUMN_MAJOR = "column";
  private final String ROW_MAJOR    = "row";
  
  private String formatXPath;
  private final String TEXT_BASE_XPATH
                  = "/eml:eml/dataset/datatable/physical/dataFormat/textFormat/";
  private final String SIMPLE_TEXT_XPATH  = TEXT_BASE_XPATH+"simpleDelimited/fieldDelimiter";
  private final String COMPLEX_TEXT_XPATH = TEXT_BASE_XPATH+"complex/";
  private final String PROPRIETARY_XPATH  
          = "/eml:eml/dataset/datatable/physical/dataFormat/externallyDefinedFormat/formatName";
  private final String RASTER_XPATH       
          = "/eml:eml/dataset/datatable/physical/dataFormat/binaryRasterFormat";
  
  private JPanel simpleTextpanel;
  private JPanel complexTextPanel;
  private JPanel proprietaryPanel;
  private JPanel rasterPanel;
  private JPanel currentPanel;
  private JLabel desc2;
  private final String[] buttonsText = new String[] {
      "Simple delimited text format (uses one of a series of delimiters to indicate the ends of fields)",
      "Complex text format (delimited fields, fixed width fields, and mixtures of the two, possibly with single records being distributed across multiple physical lines)",
      "Non-text or proprietary formatted object that is externally defined (e.g. 'Microsoft Excel')",
      "Binary raster image file"
    };
  
  private final String[] orientButtonsText = new String[] {
      "Columns",
      "Rows"
    };
    
  private final String[] delimiterCheckBoxesText = new String[] {
    "tab",
    "comma",
    "space",
    "semicolon",
    "other"
  };
  
  private String delim_tab       = null;
  private String delim_comma     = null;
  private String delim_space     = null;
  private String delim_semicolon = null;
  private String delim_other     = null;
  
    
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  public DataFormat() { init(); }
  
  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BorderLayout());
    
    Box topBox = Box.createVerticalBox();
    
    JLabel desc = WidgetFactory.makeHTMLLabel(
      "Enter some information about your data file. Required fields are "
      +"highlighted", 2);
    topBox.add(desc);
    topBox.add(WidgetFactory.makeDefaultSpacer());

    desc2 = WidgetFactory.makeHTMLLabel("What is the format of your data?", 1);
    topBox.add(desc2);

    final JPanel instance = this;
    
    ActionListener listener = new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
        
        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        //undo any hilites:
        onLoadAction();
        
        if (e.getActionCommand().equals(buttonsText[0])) {
          
          instance.remove(currentPanel);
          currentPanel = simpleTextpanel;
          formatXPath = SIMPLE_TEXT_XPATH;
          instance.add(simpleTextpanel, BorderLayout.CENTER);
          
        } else if (e.getActionCommand().equals(buttonsText[1])) {
        
          instance.remove(currentPanel);
          currentPanel = complexTextPanel;
          formatXPath = COMPLEX_TEXT_XPATH;
          instance.add(complexTextPanel, BorderLayout.CENTER);
          
        } else if (e.getActionCommand().equals(buttonsText[2])) {
        
          instance.remove(currentPanel);
          currentPanel = proprietaryPanel;
          formatXPath = PROPRIETARY_XPATH;
          instance.add(proprietaryPanel, BorderLayout.CENTER);
          
        } else if (e.getActionCommand().equals(buttonsText[3])) {
        
          instance.remove(currentPanel);
          currentPanel = rasterPanel;
          formatXPath = RASTER_XPATH;
          instance.add(rasterPanel, BorderLayout.CENTER);
          
        }
        instance.validate();
        instance.repaint();
      }
    };
    
    
    
    JPanel radioPanel = WidgetFactory.makeRadioPanel(buttonsText, -1, listener);
    
    topBox.add(radioPanel);
    
    topBox.add(WidgetFactory.makeDefaultSpacer());
    
    this.add(topBox, BorderLayout.NORTH);
    
    simpleTextpanel  = getSimpleTextpanel();
    complexTextPanel = getComplexTextPanel();
    proprietaryPanel = getProprietaryPanel();
    rasterPanel      = getRasterPanel();

    currentPanel = getEmptyPanel();
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private JLabel delimiterLabel;
  
  private JPanel getSimpleTextpanel() {
    
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
    WidgetFactory.addTitledBorder(panel, buttonsText[0]);
    
    panel.add(WidgetFactory.makeDefaultSpacer());
  
    ////
    panel.add(WidgetFactory.makeHTMLLabel("Data Attributes are arranged in:", 1));

    JPanel orientationPanel = WidgetFactory.makePanel(2);

    JLabel spacerLabel = WidgetFactory.makeLabel("", false);
    
    orientationPanel.add(spacerLabel);
    
    orientationPanel.add(getOrientationRadioPanel());
    
    panel.add(orientationPanel);
    
    panel.add(WidgetFactory.makeDefaultSpacer());

    ////
    panel.add(WidgetFactory.makeHTMLLabel(
      "Define one or more delimiters used to indicate the ends of fields:", 1));

    JPanel delimiterPanel = WidgetFactory.makePanel(8);

    delimiterLabel = WidgetFactory.makeLabel("Delimiter(s)", true);
    
    delimiterPanel.add(delimiterLabel);
    
    delimiterPanel.add(getDelimiterCheckBoxPanel());
    
    panel.add(delimiterPanel);
    
    panel.add(WidgetFactory.makeDefaultSpacer());
    
    panel.add(Box.createGlue());

    return panel;
  }
  
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private JLabel      fileNameLabelOnline;
  private JTextField  fileNameFieldOnline;
  private JLabel      urlLabelOnline;
  private JTextField  urlFieldOnline;
  
  
  private JPanel getComplexTextPanel() {
    
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
//    WidgetFactory.addTitledBorder(panel, buttonsText[1]);
//    
//    panel.add(WidgetFactory.makeDefaultSpacer());
//  
//    ////
//    JPanel fileNamePanel = WidgetFactory.makePanel(1);
//    
//    fileNameLabelOnline = WidgetFactory.makeLabel("File Name:", true);
//
//    fileNamePanel.add(fileNameLabelOnline);
//    
//    fileNameFieldOnline = WidgetFactory.makeOneLineTextField();
//    fileNamePanel.add(fileNameFieldOnline);
//    
//    panel.add(fileNamePanel);
//    
//    panel.add(WidgetFactory.makeDefaultSpacer());
//    
//    ////
//    JPanel urlPanel = WidgetFactory.makePanel(1);
//    
//    urlLabelOnline = WidgetFactory.makeLabel("URL:", true);
//
//    urlPanel.add(urlLabelOnline);
//    
//    urlFieldOnline = WidgetFactory.makeOneLineTextField();
//    urlPanel.add(urlFieldOnline);
//    
//    panel.add(urlPanel);
//    
//    panel.add(WidgetFactory.makeDefaultSpacer());
//    
//    panel.add(WidgetFactory.makeHTMLLabel(
//      "How would you like to enter the information describing "
//      +"the format and structure of the data?", 1));
//  
//    panel.add(getOrientationRadioPanel());
//    
//    panel.add(WidgetFactory.makeDefaultSpacer());
//    
//    panel.add(Box.createGlue());
//    
    return panel;
  }
  
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private JLabel      fileNameLabelOffline;
  private JTextField  fileNameFieldOffline; 
  
  private JPanel getProprietaryPanel() {
    
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
//    WidgetFactory.addTitledBorder(panel, buttonsText[2]);
//    
//    panel.add(WidgetFactory.makeDefaultSpacer());
//  
//    ////
//    JPanel fileNamePanel = WidgetFactory.makePanel(1);
//    
//    fileNameLabelOffline = WidgetFactory.makeLabel("File Name:", true);
//
//    fileNamePanel.add(fileNameLabelOffline);
//    
//    fileNameFieldOffline = WidgetFactory.makeOneLineTextField();
//    fileNamePanel.add(fileNameFieldOffline);
//    
//    panel.add(fileNamePanel);
//        
//    panel.add(WidgetFactory.makeDefaultSpacer());
//    
//    panel.add(WidgetFactory.makeHTMLLabel(
//      "How would you like to enter the information describing "
//      +"the format and structure of the data?", 1));
//  
//    panel.add(getOrientationRadioPanel());
//    
//    panel.add(WidgetFactory.makeDefaultSpacer());
//    
//    panel.add(Box.createGlue());
  
    return panel;
  }
  
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  private JPanel getRasterPanel() {
    
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
  
    return panel;
  }
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  private JPanel getEmptyPanel() {
    
    return WidgetFactory.makeVerticalPanel(7);
  }
  
  


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

    WidgetFactory.unhiliteComponent(desc2);
    WidgetFactory.unhiliteComponent(fileNameLabelOnline);
    WidgetFactory.unhiliteComponent(urlLabelOnline);
    WidgetFactory.unhiliteComponent(fileNameLabelOffline);
    
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
    
    if (formatXPath==null || currentPanel==null)  {

      return false;

    } else if (formatXPath==SIMPLE_TEXT_XPATH) {

    if (delim_tab==null && delim_comma==null && delim_space==null 
                        && delim_semicolon==null && delim_other==null) {
      
      WidgetFactory.hiliteComponent(delimiterLabel);
      return false;
    }
    

//    } else if (formatXPath==COMPLEX_TEXT_XPATH) {
//
//      if (fileNameFieldOnline.getText().trim().equals("")) {
//      
//        WidgetFactory.hiliteComponent(fileNameLabelOnline);
//        fileNameFieldOnline.requestFocus();
//        return false;
//      }
//    
//      if (urlFieldOnline.getText().trim().equals("")) {
//      
//        WidgetFactory.hiliteComponent(urlLabelOnline);
//        urlFieldOnline.requestFocus();
//        return false;
//      }
//      
//    } else if (formatXPath==PROPRIETARY_XPATH) {
//    
//    
//      if (fileNameFieldOffline.getText().trim().equals("")) {
//      
//        WidgetFactory.hiliteComponent(fileNameLabelOffline);
//        fileNameFieldOffline.requestFocus();
//        return false;
//      }
//    
//    } else if (formatXPath==RASTER_XPATH) {
//    
//    
    }
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
    
    if (formatXPath==null || formatXPath==RASTER_XPATH) {
    
      // if no data, return empty Map:
      return returnMap;
    
    } else if (formatXPath==SIMPLE_TEXT_XPATH)  {
  
      returnMap.put(TEXT_BASE_XPATH+"attributeOrientation", orientationSimple);
      
      int index=0;
      
      if (delim_tab!=null) returnMap.put(SIMPLE_TEXT_XPATH+"["+(index++)+"]", 
                                                                    delim_tab);
    
      if (delim_comma!=null) returnMap.put(SIMPLE_TEXT_XPATH+"["+(index++)+"]", 
                                                                    delim_comma);
    
      if (delim_space!=null) returnMap.put(SIMPLE_TEXT_XPATH+"["+(index++)+"]", 
                                                                    delim_space);
    
      if (delim_semicolon!=null) returnMap.put(SIMPLE_TEXT_XPATH+"["+(index++)+"]", 
                                                                delim_semicolon);
    
      if (delim_other!=null) returnMap.put(SIMPLE_TEXT_XPATH+"["+(index++)+"]", 
                                                                    delim_other);
    
//    } else if (formatXPath==COMPLEX_TEXT_XPATH)  {
//    
//      returnMap.put(OBJECTNAME_XPATH, fileNameFieldOnline.getText().trim());
//      returnMap.put(formatXPath, urlFieldOnline.getText().trim());
//      
//    } else if (formatXPath==PROPRIETARY_XPATH)  {
//    
//      returnMap.put(OBJECTNAME_XPATH, fileNameFieldOffline.getText().trim());
    }
    return returnMap;
  }
  
  
  
  private JPanel getDelimiterCheckBoxPanel() {
  
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
    final JTextField otherDelimTextField = WidgetFactory.makeOneLineTextField();
    
    JPanel cbPanel = WidgetFactory.makeCheckBoxPanel(delimiterCheckBoxesText, -1,
      new ItemListener() {
    
        public void itemStateChanged(ItemEvent e) {

          String cmd = ( (JCheckBox)(e.getSource()) ).getActionCommand();
          int stateChange = e.getStateChange();
          
          Log.debug(45, "got checkBox state changed: "+cmd
                                +"; type of state change = "
                +((stateChange==ItemEvent.SELECTED)? "SELECTED": "UNSELECTED"));

          if (cmd.indexOf(delimiterCheckBoxesText[0])==0) {
        
            delim_tab       = (stateChange==ItemEvent.SELECTED)? "\t" : null;
        
          } else if (cmd.indexOf(delimiterCheckBoxesText[1])==0) {
      
            delim_comma     = (stateChange==ItemEvent.SELECTED)? "," : null;
            
          } else if (cmd.indexOf(delimiterCheckBoxesText[2])==0) {
      
            delim_space     = (stateChange==ItemEvent.SELECTED)? " " : null;
            
          } else if (cmd.indexOf(delimiterCheckBoxesText[3])==0) {

            delim_semicolon = (stateChange==ItemEvent.SELECTED)? ";" : null;

          } else if (cmd.indexOf(delimiterCheckBoxesText[4])==0) {

            delim_other     = (stateChange==ItemEvent.SELECTED)? 
                                                    otherDelimTextField.getText()
                                                  : null;
          }
        }
      });
    panel.add(cbPanel);
    
    otherDelimTextField.setPreferredSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);  
    otherDelimTextField.setMaximumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);  
    JPanel otherPanel = new JPanel();
    otherPanel.setLayout(new BoxLayout(otherPanel, BoxLayout.X_AXIS));
    otherPanel.add(otherDelimTextField);
    otherPanel.add(Box.createGlue());
    panel.add(otherPanel);
    
    return panel;
  }
  
  
  
  private JPanel getOrientationRadioPanel() {
  
    final String finalXPath = formatXPath;
    
    return WidgetFactory.makeRadioPanel(orientButtonsText, 0,
      new ActionListener() {
    
        public void actionPerformed(ActionEvent e) {
      
          Log.debug(45, "got radiobutton command: "+e.getActionCommand());
      
          if (e.getActionCommand().equals(orientButtonsText[0])) {
        
            if (finalXPath==SIMPLE_TEXT_XPATH)  setOrientationSimple(COLUMN_MAJOR);
            if (finalXPath==COMPLEX_TEXT_XPATH) setOrientationComplex(COLUMN_MAJOR);
        
          } else if (e.getActionCommand().equals(orientButtonsText[1])) {
      
            if (finalXPath==SIMPLE_TEXT_XPATH)  setOrientationSimple(ROW_MAJOR);
            if (finalXPath==COMPLEX_TEXT_XPATH) setOrientationComplex(ROW_MAJOR);
          }
        }
      });
  }
  
  
  private void setOrientationSimple(String orient) {
  
    orientationSimple = orient;
  }

  
  private void setOrientationComplex(String orient) {
  
    orientationComplex = orient;
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