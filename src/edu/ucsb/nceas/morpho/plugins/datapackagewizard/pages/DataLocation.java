/*  '$RCSfile: DataLocation.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2003-12-03 02:38:49 $'
 * '$Revision: 1.18 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.io.File;

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

  private final String pageID     = DataPackageWizardInterface.DATA_LOCATION;
  private String nextPageID       = DataPackageWizardInterface.TEXT_IMPORT_WIZARD;
  private final String pageNumber = "13";

  private final String title      = "Data File Information:";
  private final String subtitle   = "Location";

  private final String EMPTY_STRING = "";
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

  private final String FILE_LOCATOR_BYHAND_DESC_OFFLINE
        = WizardSettings.HTML_TABLE_LABEL_OPENING
        +"Enter an identifying name in the space below&nbsp;&nbsp;"
        +WizardSettings.HTML_EXAMPLE_FONT_OPENING
        +" (e.g. a title if your data is on hardcopy, or a filename for digital media)"
        +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
        +WizardSettings.HTML_TABLE_LABEL_CLOSING;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final AbstractWizardPage instance = this;
  private String distribXPath;
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
  private final String NODATA_XPATH  = EMPTY_STRING;

  private String inlineNextPageID  = DataPackageWizardInterface.TEXT_IMPORT_WIZARD;
  private String onlineNextPageID  = DataPackageWizardInterface.DATA_FORMAT;
  private String offlineNextPageID = DataPackageWizardInterface.TEXT_IMPORT_WIZARD;

  private JPanel inlinePanel;
  private JPanel onlinePanel;
  private JPanel offlinePanel;
  private JPanel noDataPanel;
  private JPanel currentPanel;
  private JLabel radioLabel;
  private String importFilePath;

  private final String[] buttonsText = new String[] {
      "INLINE  - Import the data into your new package file",
      "ONLINE  - Link to online data using a URL",
      "OFFLINE - Enter information about the data, but do not make the data itself available",
      "NO DATA - Do not describe or include any data at this time"
    };

  private final String[] genHandButtonsText = new String[] {
      "Generate the information automatically from a data file on your computer (text files only)",
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

        //undo any hilites:
        onLoadAction();

        if (e.getActionCommand().equals(buttonsText[0])) {

          instance.remove(currentPanel);
          currentPanel = inlinePanel;
          distribXPath = INLINE_XPATH;
          instance.add(inlinePanel, BorderLayout.CENTER);

        } else if (e.getActionCommand().equals(buttonsText[1])) {

          instance.remove(currentPanel);
          currentPanel = onlinePanel;
          distribXPath = ONLINE_XPATH;
          instance.add(onlinePanel, BorderLayout.CENTER);
          fileNameFieldOnline.requestFocus();

        } else if (e.getActionCommand().equals(buttonsText[2])) {

          instance.remove(currentPanel);
          currentPanel = offlinePanel;
          distribXPath = OFFLINE_XPATH;
          instance.add(offlinePanel, BorderLayout.CENTER);

        } else if (e.getActionCommand().equals(buttonsText[3])) {

          instance.remove(currentPanel);
          currentPanel = noDataPanel;
          distribXPath = NODATA_XPATH;
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

    currentPanel = noDataPanel;
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  FileLocatorWidget  fileLocatorWidgetInline;

  private JPanel getInlinePanel() {

    JPanel panel =  new JPanel(new BorderLayout());

    WidgetFactory.addTitledBorder(panel, buttonsText[0]);

    fileLocatorWidgetInline = new FileLocatorWidget(
                                              FILE_LOCATOR_FIELD_FILENAME_LABEL,
                                              FILE_LOCATOR_IMPORT_DESC_INLINE,
                                              INIT_FILE_LOCATOR_TEXT);

    panel.add(fileLocatorWidgetInline, BorderLayout.NORTH);

    return panel;
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JLabel      fileNameLabelOnline;
  private JTextField  fileNameFieldOnline;
  private JLabel      urlLabelOnline;
  private JTextField  urlFieldOnline;

  private JPanel getOnlinePanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);

    WidgetFactory.addTitledBorder(panel, buttonsText[1]);

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

  private FileLocatorWidget fileLocatorWidgetOffline;
  private JLabel            objNameLabel;
  private JLabel            medNameLabel;
  private JTextField        objNameField;
  private JTextField        medNameField;

  private JPanel getOfflinePanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    WidgetFactory.addTitledBorder(panel, buttonsText[2]);

    panel.add(WidgetFactory.makeHTMLLabel(
      "Offline data may be stored on various digital media such as tapes and "
      +"disks, or printed media which can collectively be termed 'hardcopy'."
      +"<br></br><br></br>How would you like to enter the information describing "
      +"the format and structure of the data?", 4));

    panel.add(WidgetFactory.makeDefaultSpacer());

    final JPanel offlineSubPanel =  new JPanel(new BorderLayout());


    ////////// IMPORT WIZ:

    fileLocatorWidgetOffline = new FileLocatorWidget(
                                              FILE_LOCATOR_FIELD_FILENAME_LABEL,
                                              FILE_LOCATOR_IMPORT_DESC_OFFLINE,
                                              INIT_FILE_LOCATOR_TEXT);


    ////////// BY HAND:

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

    final JPanel byHandPanelOffline = WidgetFactory.makeVerticalPanel(4);

    byHandPanelOffline.add(WidgetFactory.makeHTMLLabel(
        "Briefly describe the type of medium on which this resource is distributed. "
        +WizardSettings.HTML_EXAMPLE_FONT_OPENING
        +"eg: Tape,&nbsp;&nbsp;3.5 inch Floppy Disk,&nbsp;&nbsp;hardcopy"
        +WizardSettings.HTML_EXAMPLE_FONT_CLOSING, 1));

    byHandPanelOffline.add(medNamePanel);

    byHandPanelOffline.add(WidgetFactory.makeDefaultSpacer());

    byHandPanelOffline.add(WidgetFactory.makeHTMLLabel(
                                          FILE_LOCATOR_BYHAND_DESC_OFFLINE, 1));

    byHandPanelOffline.add(objNamePanel);
    /////////////

    offlineSubPanel.add(fileLocatorWidgetOffline, BorderLayout.NORTH);

    panel.add(
      WidgetFactory.makeRadioPanel(genHandButtonsText, 0,
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equals(genHandButtonsText[0])) {
              setNextPageID(DataPackageWizardInterface.TEXT_IMPORT_WIZARD);
              offlineSubPanel.removeAll();
              offlineSubPanel.add(fileLocatorWidgetOffline, BorderLayout.NORTH);
              offlineSubPanel.validate();
              offlineSubPanel.repaint();

            } else if (e.getActionCommand().equals(genHandButtonsText[1])) {

              setNextPageID(DataPackageWizardInterface.DATA_FORMAT);
              offlineSubPanel.removeAll();
              offlineSubPanel.add(byHandPanelOffline, BorderLayout.CENTER);
              offlineSubPanel.validate();
              offlineSubPanel.repaint();
            }
          }
        }));

    panel.add(offlineSubPanel);
    panel.add(Box.createGlue());

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

    WidgetFactory.unhiliteComponent(radioLabel);
    WidgetFactory.unhiliteComponent(fileLocatorWidgetInline.getLabel());
    WidgetFactory.unhiliteComponent(fileNameLabelOnline);
    WidgetFactory.unhiliteComponent(urlLabelOnline);
    WidgetFactory.unhiliteComponent(fileLocatorWidgetOffline.getLabel());
    WidgetFactory.unhiliteComponent(objNameLabel);
    WidgetFactory.unhiliteComponent(medNameLabel);
  }


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {}


  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty, but if so, must
   *  return true
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    if (distribXPath==null || currentPanel==null)  {

      WidgetFactory.hiliteComponent(radioLabel);
      return false;

    } else if (distribXPath==INLINE_XPATH) {
//  I N L I N E  /////////////////////////////////////

      if (fileLocatorWidgetInline.getImportFilePath()==null) {

        WidgetFactory.hiliteComponent(fileLocatorWidgetInline.getLabel());
        fileLocatorWidgetInline.getButton().requestFocus();
        return false;
      }
      WidgetFactory.unhiliteComponent(fileLocatorWidgetInline.getLabel());
      WizardSettings.setSummaryText(WizardSettings.SUMMARY_TEXT_INLINE);

      this.importFilePath = fileLocatorWidgetInline.getImportFilePath();

      nextPageID = inlineNextPageID;

    } else if (distribXPath==ONLINE_XPATH) {
//  O N L I N E  /////////////////////////////////////

      if (fileNameFieldOnline.getText().trim().equals("")) {

        WidgetFactory.hiliteComponent(fileNameLabelOnline);
        fileNameFieldOnline.requestFocus();
        return false;
      }
      WidgetFactory.unhiliteComponent(fileNameLabelOnline);

      if (urlFieldOnline.getText().trim().equals("")) {

        WidgetFactory.hiliteComponent(urlLabelOnline);
        urlFieldOnline.requestFocus();

        return false;
      }
      WidgetFactory.unhiliteComponent(urlLabelOnline);
      WizardSettings.setSummaryText(WizardSettings.SUMMARY_TEXT_ONLINE);
      WizardSettings.setDataLocation(urlFieldOnline.getText().trim());

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

        this.importFilePath = null;

      } else {
      //imported by wiz:

        if (fileLocatorWidgetOffline.getImportFilePath()==null) {

          WidgetFactory.hiliteComponent(fileLocatorWidgetOffline.getLabel());
          fileLocatorWidgetOffline.getButton().requestFocus();

          return false;
        }
        WidgetFactory.unhiliteComponent(fileLocatorWidgetOffline.getLabel());

        this.importFilePath = fileLocatorWidgetOffline.getImportFilePath();
      }
      WizardSettings.setSummaryText(WizardSettings.SUMMARY_TEXT_OFFLINE);

      nextPageID = offlineNextPageID;

    } else if (distribXPath==NODATA_XPATH) {
//  N O   D A T A  ///////////////////////////////////

      WizardSettings.setSummaryText(WizardSettings.SUMMARY_TEXT_NODATA);
      nextPageID = DataPackageWizardInterface.ACCESS;
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

//  N O   D A T A  /////////////////////////////////////
    if (distribXPath==null || distribXPath==NODATA_XPATH) {

      // if no data, return empty Map:
      return returnMap;

    } else if (distribXPath==INLINE_XPATH)  {
//  I N L I N E  /////////////////////////////////////

      //nothing needs doing - inport wizard gets these values

    } else if (distribXPath==ONLINE_XPATH)  {
//  O N L I N E  /////////////////////////////////////

      returnMap.put(OBJECTNAME_XPATH, fileNameFieldOnline.getText().trim());
      returnMap.put(distribXPath + "/url", urlFieldOnline.getText().trim());

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
    return returnMap;
  }


  private void setNextPageID(String id) {

    if (distribXPath==INLINE_XPATH)  inlineNextPageID  = id;
    if (distribXPath==ONLINE_XPATH)  onlineNextPageID  = id;
    if (distribXPath==OFFLINE_XPATH) offlineNextPageID = id;
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

  /**
   *  Returns the full path of the data file that the user has elected to import
   *  - may be null if not set
   *
   *  @return the full String path of the data file that the user has elected to
   *  import. May be null if not set
   */
  public String  getImportFilePath() { return this.importFilePath; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  public void setPageData(OrderedMap data) { }

}


class FileLocatorWidget extends JPanel {

  private final       String EMPTY_STRING = "";
  private JLabel     fileNameLabel;
  private JLabel     descLabel;
  private JTextField fileNameField;
  private JButton    fileNameButton;
  private String     importFilePath;

  public FileLocatorWidget(String label, String descText, String initialText) {

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
                    fileNameField.setText(getImportFilePath());
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

  protected String  getImportFilePath() { return this.importFilePath; }

  protected void setDescription(String desc) {

    if (desc==null) desc = EMPTY_STRING;
    descLabel.setText(desc);
  }

  protected void setImportFilePath(String filePath) {

    this.importFilePath = filePath;
  }
}