/*  '$RCSfile: DataLocation.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-12-17 17:48:44 $'
 * '$Revision: 1.22 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.io.File;

import java.awt.Container;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.GridLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.BorderFactory;


import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;


public class DataLocation extends AbstractWizardPage {

  private final String pageID       = DataPackageWizardInterface.DATA_LOCATION;
  private String nextPageID         = DataPackageWizardInterface.TEXT_IMPORT_WIZARD;
  private final String pageNumber   = "1";

  private final String title      = "Data File Information:";
  private final String subtitle   = "Location";

  private final String FILE_LOCATOR_FIELD_FILENAME_LABEL = "File Name:";
  private final String FILE_LOCATOR_FIELD_OBJNAME_LABEL  = "Name/Title:";

  private final String INIT_FILE_LOCATOR_TEXT
                                  = "   use button to select a file -->";
  private final String FILE_LOCATOR_IMPORT_DESC_INLINE
        = WizardSettings.HTML_TABLE_LABEL_OPENING
        +"Use the \"locate\" button to locate the data file on your computer:"
        +WizardSettings.HTML_TABLE_LABEL_CLOSING;

  private final String FILE_LOCATOR_IMPORT_DESC_OFFLINE
        = WizardSettings.HTML_TABLE_LABEL_OPENING
        +"If the offline data is in a file on your computer, please locate it:"
        +WizardSettings.HTML_TABLE_LABEL_CLOSING;

  private final String NAME_BYHAND_DESC_OFFLINE
        = WizardSettings.HTML_TABLE_LABEL_OPENING
        +"Enter an identifying name in the space below&nbsp;&nbsp;"
        +WizardSettings.HTML_EXAMPLE_FONT_OPENING
        +" (e.g. a title if your data is on hardcopy, or a filename for digital media)"
        +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
        +WizardSettings.HTML_TABLE_LABEL_CLOSING;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String OBJECTNAME_XPATH
                  = "/eml:eml/dataset/dataTable/physical/objectName";
  private final String MEDIUMNAME_XPATH
                  = "/eml:eml/dataset/dataTable/physical/distribution/offline/mediumName";
  private final String INLINE_XPATH
                  = "/eml:eml/dataset/dataTable/physical/distribution/inline";
  private final String ONLINE_XPATH
                  = "/eml:eml/dataset/dataTable/physical/distribution/online";
  private final String OFFLINE_XPATH
                  = "/eml:eml/dataset/dataTable/physical/distribution/offline";

  private final String FILECHOOSER_PANEL_TITLE = "File Location:";
  
  private final String firstChoiceTitle    
                        = "Where is your data?";
  private final String[] firstChoiceLabels = new String[] {
    "CREATE  - create a new, empty data table and its metadata description.",
    "IMPORT   - import a data file into the package, and create its metadata description",
    "DESCRIBE - include only a metadata description of a web-accessible, archived or inaccessible data file"
  };

  private final String importAutoManButtonsTitle    
                        = "How do you want to enter the metadata description?";
  private final String[] importAutoManButtonsLabels = new String[] {
    "AUTOMATIC - Import data file and extract metadata description for review",
    "MANUAL       - Import data file but enter metadata description manually"
  };

  private final String describeAutoManButtonsTitle    
                        = "How do you want to enter the metadata description?";
  private final String[] describeAutoManButtonsLabels = new String[] {
    "AUTOMATIC - create metadata description by inspecting data file (but omit data file from package)",
    "MANUAL       - Enter metadata description manually"
  };

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public DataLocation() { 
  
    String inlineOnlineString 
                = Morpho.getConfiguration().get("dataLocationInlineOnline", 0);
       
    if (inlineOnlineString.equalsIgnoreCase("inline")) {
    
      INLINE_OR_ONLINE_XPATH = INLINE_XPATH;
      
    } else {
    
      INLINE_OR_ONLINE_XPATH = ONLINE_XPATH;
    }
    init();
    setLastEvent(CREATE);
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BorderLayout());

    ActionListener importQ2Listener = null;
    ActionListener describeQ2Listener = null;

    secondChoiceContainer = getBlankPanel(4);

    Box topBox = Box.createVerticalBox();

    JLabel desc = WidgetFactory.makeHTMLLabel(
       "<p>Describe and optionally include a data "
      +"table in your data package. You may create a table from "
      +"scratch and populate it using Morpho's spreadsheet-style data editor, "
      +"or you can import certain types of existing data files and use the "
      +"wizard to automatically extract much of the metadata from the data "
      +"file itself. If you "
      +"choose this option, you will be prompted to review the metadata that "
      +"is extracted and provide any required fields that can not be generated "
      +"automatically for each column.<br></br></p>" 
      +"<p>You can also choose to manually enter all of the required fields "
      +"(rather than using the metadata extractor), which is useful for "
      +"proprietary file types like Excel, or other "
      +"file types we don't yet support for extraction.</p>", 7);
    topBox.add(desc);

    final JPanel instance = this;

    ////////////////////////////////////////////////////////
    // "IMPORT - AUTO/MAN" RADIO PANEL
    // COULD BE "ONLINE" OR "INLINE"
    ////////////////////////////////////////////////////////
    
    importQ2Listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "IMPORT - AUTO/MAN ActionEvent: "+e.getActionCommand());
        //undo any hilites:
        onLoadAction();
        
        //both options require a filechooser...
        setThirdChoice(filechooserPanel);
        fileChooserWidget.getTextArea().requestFocus();

        if (e.getActionCommand().equals(importAutoManButtonsLabels[0])) {

          // IMPORT AUTOMATIC
          Log.debug(45, "IMPORT - AUTOMATIC");
          setLastEvent(IMPORT_AUTO);

        } else if (e.getActionCommand().equals(importAutoManButtonsLabels[1])) {

          // IMPORT MANUAL
          Log.debug(45, "IMPORT - MANUAL");
          setLastEvent(IMPORT_MAN);
        } 
        instance.validate();
        instance.repaint();
      }
    };


    ////////////////////////////////////////////////////////
    // "DESCRIBE - AUTO/MAN" RADIO PANEL
    // "OFFLINE"
    ////////////////////////////////////////////////////////
    
    describeQ2Listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "DESCRIBE - AUTO/MAN ActionEvent: "+e.getActionCommand());
        //undo any hilites:
        onLoadAction();
        

        if (e.getActionCommand().equals(describeAutoManButtonsLabels[0])) {

          // DESCRIBE AUTOMATIC
          Log.debug(45, "DESCRIBE - AUTOMATIC");
          setThirdChoice(filechooserPanel);
          fileChooserWidget.getTextArea().requestFocus();
          setLastEvent(DESCRIBE_AUTO);

        } else if (e.getActionCommand().equals(describeAutoManButtonsLabels[1])) {

          // DESCRIBE MANUAL
          Log.debug(45, "DESCRIBE - MANUAL");
          setThirdChoice(offlinePanel);
          objNameField.requestFocus();
          setLastEvent(DESCRIBE_MAN);
        } 
        instance.validate();
        instance.repaint();
      }
    };

    final JPanel importAutoManRadioPanel 
                                  = getRadioPanel(importAutoManButtonsTitle, 
                                                  importAutoManButtonsLabels, 
                                                  importQ2Listener, -1, true);
    final JPanel describeAutoManRadioPanel 
                                  = getRadioPanel(describeAutoManButtonsTitle, 
                                                  describeAutoManButtonsLabels, 
                                                  describeQ2Listener, -1, true);

    ////////////////////////////////////////////////////////
    // "CREATE/IMPORT/DESCRIBE" RADIO PANEL
    ////////////////////////////////////////////////////////

    ActionListener q1Listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "got includeOmitRadioPanel command: "+e.getActionCommand());

        //undo any hilites:
        onLoadAction();

        if (e.getActionCommand().equals(firstChoiceLabels[0])) { 

          // CREATE DATA

          setSecondChoice(blankPanel);
          setThirdChoice(blankPanel);
          setLastEvent(CREATE);
          
        } else if (e.getActionCommand().equals(firstChoiceLabels[1])) { 

          // IMPORT DATA

          setSecondChoice(importAutoManRadioPanel);
          setThirdChoice(blankPanel);
          setLastEvent(IMPORT);

        } else if (e.getActionCommand().equals(firstChoiceLabels[2])) { 

          // DESCRIBE DATA

//          distribXPath = OFFLINE_XPATH;
          setSecondChoice(describeAutoManRadioPanel);
          setThirdChoice(blankPanel);
          setLastEvent(DESCRIBE);
        } 
        instance.validate();
        instance.repaint();
      }
    };

    topBox.add(getRadioPanel( firstChoiceTitle, 
                              firstChoiceLabels, q1Listener, 0, true));

    topBox.add(WidgetFactory.makeDefaultSpacer());
    
    topBox.add(secondChoiceContainer);
    
//    topBox.add(WidgetFactory.makeDefaultSpacer());
    
    this.add(topBox, BorderLayout.NORTH);

    filechooserPanel  = getFilechooserPanel();
    onlinePanel  = getOnlinePanel();
    offlinePanel = getOfflinePanel();
    blankPanel  = getBlankPanel(7);

    currentSecondChoicePanel = blankPanel;
    currentThirdChoicePanel  = blankPanel;
  }

  private void setLastEvent(short eventFlag) { lastEvent = eventFlag; }
  
  private short getLastEvent() { return lastEvent; }
  
  private void setSecondChoice(JPanel newPanel) {
  
    secondChoiceContainer.remove(currentSecondChoicePanel);
    currentSecondChoicePanel = newPanel;
    secondChoiceContainer.add(currentSecondChoicePanel);
  }
  
  private void setThirdChoice(JPanel newPanel) {
  
    this.remove(currentThirdChoicePanel);
    currentThirdChoicePanel = newPanel;
    this.add(currentThirdChoicePanel, BorderLayout.CENTER);
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  FileChooserWidget  fileChooserWidget;

  private JPanel getFilechooserPanel() {

    JPanel panel =  new JPanel(new BorderLayout());

    WidgetFactory.addTitledBorder(panel, FILECHOOSER_PANEL_TITLE);

    fileChooserWidget = new FileChooserWidget(FILE_LOCATOR_FIELD_FILENAME_LABEL,
                                              FILE_LOCATOR_IMPORT_DESC_INLINE,
                                              INIT_FILE_LOCATOR_TEXT);

    panel.add(fileChooserWidget, BorderLayout.NORTH);

    return panel;
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JLabel      fileNameLabelOnline;
  private JTextField  fileNameFieldOnline;
  private JLabel      urlLabelOnline;
  private JTextField  urlFieldOnline;

  private JPanel getOnlinePanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);

    WidgetFactory.addTitledBorder(panel, "Describe Online Data");

    panel.add(WidgetFactory.makeDefaultSpacer());

    ////
    JPanel fileNamePanel = WidgetFactory.makePanel(1);

    fileNameLabelOnline = WidgetFactory.makeLabel("File Name:", true);

    fileNamePanel.add(fileNameLabelOnline);

    fileNameFieldOnline = WidgetFactory.makeOneLineTextField();
    fileNamePanel.add(fileNameFieldOnline);

    panel.add(fileNamePanel);

    panel.add(WidgetFactory.makeDefaultSpacer());

    ////
    JPanel urlPanel = WidgetFactory.makePanel(1);

    urlLabelOnline = WidgetFactory.makeLabel("URL:", true);

    urlPanel.add(urlLabelOnline);

    urlFieldOnline = WidgetFactory.makeOneLineTextField();
    urlPanel.add(urlFieldOnline);

    panel.add(urlPanel);

    panel.add(WidgetFactory.makeDefaultSpacer());

    panel.add(WidgetFactory.makeDefaultSpacer());

    panel.add(Box.createGlue());

    return panel;
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private FileChooserWidget fileLocatorWidgetOffline;
  private JLabel            objNameLabel;
  private JLabel            medNameLabel;
  private JTextField        objNameField;
  private JTextField        medNameField;

  private JPanel getOfflinePanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    WidgetFactory.addTitledBorder(panel, "Describe your data");

    panel.add(WidgetFactory.makeHTMLLabel(
      "Data may be stored on various digital media such as tapes and disks, "
      +"or printed media which can collectively be termed 'hardcopy'.", 2));

//    panel.add(WidgetFactory.makeDefaultSpacer());

    JPanel objNamePanel = WidgetFactory.makePanel();
    objNameLabel = WidgetFactory.makeLabel(
                                        FILE_LOCATOR_FIELD_OBJNAME_LABEL, true);
    objNameField = WidgetFactory.makeOneLineTextField();
    objNamePanel.add(objNameLabel);
    objNamePanel.add(objNameField);

    JPanel medNamePanel = WidgetFactory.makePanel();
    medNameLabel = WidgetFactory.makeLabel("Medium Type:", true);
    medNameField = WidgetFactory.makeOneLineTextField();
    medNamePanel.add(medNameLabel);
    medNamePanel.add(medNameField);
    
//    panel.add(WidgetFactory.makeDefaultSpacer());

    panel.add(WidgetFactory.makeHTMLLabel(
        "Briefly describe the type of medium on which this resource is distributed. "
        +WizardSettings.HTML_EXAMPLE_FONT_OPENING
        +"eg: Tape,&nbsp;&nbsp;3.5 inch Floppy Disk,&nbsp;&nbsp;hardcopy"
        +WizardSettings.HTML_EXAMPLE_FONT_CLOSING, 1));


    panel.add(medNamePanel);

    panel.add(WidgetFactory.makeDefaultSpacer());

    panel.add(WidgetFactory.makeHTMLLabel(NAME_BYHAND_DESC_OFFLINE, 1));

    panel.add(objNamePanel);

    panel.add(Box.createGlue());

    return panel;
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  private JPanel getBlankPanel(int numRows) {

    JPanel panel = WidgetFactory.makeVerticalPanel(numRows);

    return panel;
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

    WidgetFactory.unhiliteComponent(fileChooserWidget.getLabel());
    WidgetFactory.unhiliteComponent(
                              getNestedJComponent(currentSecondChoicePanel, 1));
                              
    WidgetFactory.unhiliteComponent(fileNameLabelOnline);
    WidgetFactory.unhiliteComponent(urlLabelOnline);
//    WidgetFactory.unhiliteComponent(fileLocatorWidgetOffline.getLabel());
    WidgetFactory.unhiliteComponent(objNameLabel);
    WidgetFactory.unhiliteComponent(medNameLabel);
  }


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {}

  

  //NOTE - assumes each nesting level (except the last) 
  //has only one Component, which is also a Container  
  private JComponent getNestedJComponent(JPanel jpanel, int levels) {
  
    Container container = (Container)jpanel;
    Component nextComponent = null;
    for (int i=0; i < levels - 1; i++) {
      if (container.getComponentCount()<1) {
        break;
      }
      nextComponent = container.getComponent(0);
      container = (Container)nextComponent;
    }
    if (container.getComponentCount()<1) return null;
    return (JComponent)(container.getComponent(0));
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

    if (getLastEvent()==CREATE) {
      
      //no validation required
      setNextPageID(DataPackageWizardInterface.DATA_FORMAT);

    } else if (getLastEvent()==IMPORT || getLastEvent()==DESCRIBE) {

      //second choice hasn't been made
      WidgetFactory.hiliteComponent(
                              getNestedJComponent(currentSecondChoicePanel, 1));
      return false;
      
    } else if (getLastEvent()==IMPORT_AUTO 
            || getLastEvent()==IMPORT_MAN 
            || getLastEvent()==DESCRIBE_AUTO) {

      //ensure file URL is set
      if (fileChooserWidget.getImportFileURL()==null) {

        WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
        fileChooserWidget.getButton().requestFocus();
        return false;
      }
      
      //ensure file URL is valid
      
      /////////////

      //if we've got this far, unhilite everything...
      WidgetFactory.unhiliteComponent(fileChooserWidget.getLabel());
      
//      WizardSettings.setSummaryText(WizardSettings.SUMMARY_TEXT_INLINE);

      this.importFileURL = fileChooserWidget.getImportFileURL();
      setNextPageID(DataPackageWizardInterface.TEXT_IMPORT_WIZARD);


    } else if (getLastEvent()==DESCRIBE_MAN_NODATA) {

//UNFINISHED

    } else if (getLastEvent()==DESCRIBE_MAN_OFFLINE) {

//UNFINISHED

    } else if (getLastEvent()==DESCRIBE_MAN_ONLINE) {

//UNFINISHED

    }
    
//UNFINISHED
    
    
/****
      if (fileChooserWidget.getImportFileURL()==null) {

        WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
        fileChooserWidget.getButton().requestFocus();
        return false;
      }
      WidgetFactory.unhiliteComponent(fileChooserWidget.getLabel());
      WizardSettings.setSummaryText(WizardSettings.SUMMARY_TEXT_INLINE);

      this.importFileURL = fileChooserWidget.getImportFileURL();


    } else if (distribXPath==ONLINE_XPATH) {
//  O N L I N E  /////////////////////////////////////

//      if (fileNameFieldOnline.getText().trim().equals("")) {
//
//        WidgetFactory.hiliteComponent(fileNameLabelOnline);
//        fileNameFieldOnline.requestFocus();
//        return false;
//      }
//      WidgetFactory.unhiliteComponent(fileNameLabelOnline);

//      if (urlFieldOnline.getText().trim().equals("")) {
//
//        WidgetFactory.hiliteComponent(urlLabelOnline);
//        urlFieldOnline.requestFocus();
//
//        return false;
//      }
//      WidgetFactory.unhiliteComponent(urlLabelOnline);
//      WizardSettings.setSummaryText(WizardSettings.SUMMARY_TEXT_ONLINE);
//      WizardSettings.setDataLocation(urlFieldOnline.getText().trim());

      nextPageID = onlineNextPageID;

    } else if (distribXPath==OFFLINE_XPATH) {
//  O F F L I N E  ///////////////////////////////////

      if (offlineNextPageID.equals(DataPackageWizardInterface.DATA_FORMAT)) {
      //entered by hand:

        if (medNameField.getText().trim().equals(EMPTY_STRING)) {

          WidgetFactory.hiliteComponent(medNameLabel);
          return false;
        }
        WidgetFactory.unhiliteComponent(medNameLabel);
        if (objNameField.getText().trim().equals(EMPTY_STRING)) {

          WidgetFactory.hiliteComponent(objNameLabel);
          return false;
        }
        WidgetFactory.unhiliteComponent(objNameLabel);

        this.importFileURL = null;

      } else {
      //imported by wiz:

        if (fileLocatorWidgetOffline.getImportFileURL()==null) {

          WidgetFactory.hiliteComponent(fileLocatorWidgetOffline.getLabel());
          fileLocatorWidgetOffline.getButton().requestFocus();

          return false;
        }
        WidgetFactory.unhiliteComponent(fileLocatorWidgetOffline.getLabel());

        this.importFileURL = fileLocatorWidgetOffline.getImportFileURL();
      }
      WizardSettings.setSummaryText(WizardSettings.SUMMARY_TEXT_OFFLINE);

      nextPageID = offlineNextPageID;
    }
    **********/
    return true;
  }

//    } else if (distribXPath==NODATA_XPATH) {
// //  N O   D A T A  ///////////////////////////////////
//
//      WizardSettings.setSummaryText(WizardSettings.SUMMARY_TEXT_NODATA);
//      nextPageID = DataPackageWizardInterface.SUMMARY;


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

    if (getLastEvent()==CREATE) {
      
      //COULD BE "ONLINE" WITH URN, OR "INLINE"
      distribXPath = INLINE_OR_ONLINE_XPATH;
      
      //need to create 1 col 1 row table here and include it
//UNFINISHED

    } else if (getLastEvent()==IMPORT_AUTO)  {
    
      //COULD BE "ONLINE" WITH URN, OR "INLINE"
      distribXPath = INLINE_OR_ONLINE_XPATH;
      
      // import wizard does the rest - no values needed here.

    } else if (getLastEvent()==IMPORT_MAN)  {
    
      //COULD BE "ONLINE" WITH URN, OR "INLINE"
      distribXPath = INLINE_OR_ONLINE_XPATH;
      
      //need to get the file here and include it
//UNFINISHED
      
    } else if (getLastEvent()==DESCRIBE_AUTO)  {
    
// COULD BE "ONLINE", "OFFLINE" OR UNAVAILABLE 
//UNFINISHED

    } else if (getLastEvent()==DESCRIBE_MAN_ONLINE)  {
    
// COULD BE "ONLINE", "OFFLINE" OR UNAVAILABLE 
//UNFINISHED

    }
    
    
//UNFINISHED
    
    
    
/*****
//  N O   D A T A  /////////////////////////////////////
    if (distribXPath==null || distribXPath==NODATA_XPATH) {

      // if no data, return empty Map:
      return returnMap;

    } else if (distribXPath==INLINE_XPATH)  {
//  I N L I N E  /////////////////////////////////////

      //nothing needs doing - inport wizard gets these values

    } else if (distribXPath==ONLINE_XPATH)  {
//  O N L I N E  /////////////////////////////////////

//      returnMap.put(OBJECTNAME_XPATH, fileNameFieldOnline.getText().trim());
//      returnMap.put(distribXPath + "/url", urlFieldOnline.getText().trim());

    } else if (distribXPath==OFFLINE_XPATH)  {
//  O F F L I N E  ///////////////////////////////////

      if (nextPageID==DataPackageWizardInterface.DATA_FORMAT) {
        //entered by hand:

        returnMap.put(OBJECTNAME_XPATH, objNameField.getText().trim());
        returnMap.put(MEDIUMNAME_XPATH, medNameField.getText().trim());

      } else {
        //entered by import wizard:

        // import wizard does all this - no values needed here.
      }
    }
****/
    return returnMap;
  }


  private void setNextPageID(String id)  { nextPageID = id; }


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

  /**
   *  Returns the full path of the data file that the user has elected to import
   *  - may be null if not set
   *
   *  @return the full String path of the data file that the user has elected to
   *  import. May be null if not set
   */
  public String  getImportFileURL() { return this.importFileURL; }

  /**
   *  Returns the proposed location of the data in terms of eml-distribution:
   *
   *  @return the proposed location of the data - WizardSettings.INLINE,
   *          WizardSettings.ONLINE, WizardSettings.OFFLINE or
   *          WizardSettings.NODATA
   */
  public short  getDataLocation() { 
  
    if  (distribXPath==INLINE_XPATH)  return WizardSettings.INLINE;
    if  (distribXPath==ONLINE_XPATH)  return WizardSettings.ONLINE;
    if  (distribXPath==OFFLINE_XPATH) return WizardSettings.OFFLINE;
    return WizardSettings.NODATA;
  }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  public void setPageData(OrderedMap data) { }

  private JPanel getRadioPanel(String title, String[] buttonLabels, 
                   ActionListener listener, int selected, boolean hiliteReqd) {
  
    final JPanel panel = WidgetFactory.makeVerticalPanel(buttonLabels.length+1);
    panel.add(WidgetFactory.makeHTMLLabel(title, 1, hiliteReqd));
    panel.add(WidgetFactory.makeRadioPanel(buttonLabels, selected, listener));
    return panel;    
  }
  
////////////////////////////////////////////////////////////////////////////////
// variables and non-editable constants
////////////////////////////////////////////////////////////////////////////////
  
  private JPanel filechooserPanel;
  private JPanel onlinePanel;
  private JPanel offlinePanel;
  private JPanel blankPanel;
  private JPanel firstChoicePanel;
  private JPanel secondChoiceContainer;
  private JPanel currentSecondChoicePanel;
  private JPanel currentThirdChoicePanel;
  private String importFileURL;
  private String distribXPath;
  private String INLINE_OR_ONLINE_XPATH;
  
  //status flags
  private final short CREATE                = 0;
  private final short IMPORT                = 10;
  private final short IMPORT_AUTO           = 12;
  private final short IMPORT_MAN            = 14;
  private final short DESCRIBE              = 20;
  private final short DESCRIBE_AUTO         = 22;
  private final short DESCRIBE_MAN          = 24;
  private final short DESCRIBE_MAN_NODATA   = 30;
  private final short DESCRIBE_MAN_ONLINE   = 32;
  private final short DESCRIBE_MAN_OFFLINE  = 34;
  
  private short lastEvent;
  
  private final String EMPTY_STRING         = ""; 
  private final String NODATA_XPATH  = EMPTY_STRING;
}
  


  
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////


class FileChooserWidget extends JPanel {

  private final       String EMPTY_STRING = "";
  private JLabel     fileNameLabel;
  private JLabel     descLabel;
  private JTextField fileNameField;
  private JButton    fileNameButton;
  private String     importFileURL;

  public FileChooserWidget(String label, String descText, String initialText) {

    super();
    init();
    if (initialText==null) initialText = EMPTY_STRING;
    fileNameField.setText(initialText);
    setDescription(descText);
    setLabelText(label);
  }

  private void init() {

    this.setLayout(new GridLayout(3,1));

    descLabel = WidgetFactory.makeHTMLLabel(EMPTY_STRING, 1);
    this.add(descLabel);

    ////
    JPanel fileNamePanel = WidgetFactory.makePanel();
    fileNamePanel.setLayout(new BorderLayout());
    fileNamePanel.setMaximumSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);

    fileNameLabel = WidgetFactory.makeLabel(EMPTY_STRING, true);

    fileNameLabel.setBackground(Color.lightGray);
    fileNamePanel.add(fileNameLabel, BorderLayout.WEST);

    fileNameField = WidgetFactory.makeOneLineTextField();
    fileNameField.setEnabled(false);
    fileNameField.setEditable(false);
    fileNameField.setDisabledTextColor(
                                      WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    fileNamePanel.add(fileNameField, BorderLayout.CENTER);

    fileNameButton = WidgetFactory.makeJButton("locate...",

            new ActionListener() {

              public void actionPerformed(ActionEvent e) {

                final JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Select a data file to import...");
                String userdir = System.getProperty("user.dir");
                fc.setCurrentDirectory(new File(userdir));
                int returnVal = fc.showOpenDialog(WizardContainerFrame.frame);
                File file = null;
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                  file = fc.getSelectedFile();

                  if (file!=null) {

                    setImportFilePath(file.getAbsolutePath());
                    fileNameField.setText(getImportFileURL());
                  }
                }
              }
            });

    fileNamePanel.add(fileNameButton, BorderLayout.EAST);

    this.add(fileNamePanel);
  }

  protected JLabel  getLabel()  { return this.fileNameLabel; }

  protected void setLabelText(String text) { this.fileNameLabel.setText(text); }

  protected JButton getButton() { return this.fileNameButton; }

  protected JTextField getTextArea() { return this.fileNameField; }

  protected String  getImportFileURL() { return this.importFileURL; }

  protected void setDescription(String desc) {

    if (desc==null) desc = EMPTY_STRING;
    descLabel.setText(desc);
  }

  protected void setImportFilePath(String filePath) {

    this.importFileURL = filePath;
  }
}
