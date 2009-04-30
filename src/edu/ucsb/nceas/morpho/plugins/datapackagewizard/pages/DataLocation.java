/*  '$RCSfile: DataLocation.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-30 23:29:34 $'
 * '$Revision: 1.45 $'
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
import edu.ucsb.nceas.morpho.datapackage.AccessionNumber;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Base64;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;


public class DataLocation extends AbstractUIPage {

  private final String pageID       = DataPackageWizardInterface.DATA_LOCATION;
  private final String pageNumber   = "1";

  private final String title      = "New Data Table Wizard";
  private final String subtitle   = "Data Location";
  private final String HARDDRIVE = "hard drive";

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String FILE_LOCATOR_FIELD_FILENAME_LABEL = "File Name:";
  private final String FILE_LOCATOR_FIELD_OBJNAME_LABEL  = "Name/Title:";

  private final String INIT_FILE_LOCATOR_TEXT
                                  = "   use button to select a file -->";
  private final String FILE_LOCATOR_IMPORT_DESC_INLINE
        = WizardSettings.HTML_TABLE_LABEL_OPENING
        +"Use the \"locate\" button to locate the data file on your computer:"
        +WizardSettings.HTML_TABLE_LABEL_CLOSING;

  private final String URN_ROOT = "ecogrid://knb/";

  private final String OBJECTNAME_XPATH
      = "/eml:eml/dataset/dataTable/physical/objectName";

  private final String MEDIUMNAME_XPATH
      = "/eml:eml/dataset/dataTable/physical/distribution/offline/mediumName";

  private final String INLINE_XPATH
      = "/eml:eml/dataset/dataTable/physical/distribution/inline";

  private final String ONLINE_URL_XPATH
      = "/eml:eml/dataset/dataTable/physical/distribution/online/url";

  private final String FILECHOOSER_PANEL_TITLE = "File Location:";

  private final String Q1_TITLE = "What do you want to do?";

  private final String[] Q1_LABELS = new String[] {
    "CREATE - Create a new, empty data table.",
    "IMPORT - Import a data file into the package.",
    "DESCRIBE - Include only the data file documentation (but not the data "
    + "file itself) in the package."
  };

  private final static int CREATE_CHOICE = 0;
  private final static int IMPORT_CHOICE = 1;
  private final static int DESCRIBE_CHOICE = 2;

  private final String Q2_TITLE_IMPORT
                        = "How do you want to enter the documentation for "
                        + "the data?";
  private final String[] Q2_LABELS_IMPORT = new String[] {
    "AUTOMATIC - Import the data file and extract the documentation for review.",
    "MANUAL - Import the data file but enter the documentation manually."
  };

  private final String Q2_TITLE_DESCRIBE
                        = "How do you want to enter the documentation for "
                        + "the data?";
  private final String[] Q2_LABELS_DESCRIBE = new String[] {
    "AUTOMATIC - Create the documentation by inspecting the data file (but "
    + "omit the data file from the package).",
    "MANUAL - Enter the documentation manually."
  };

  private final String Q3_TITLE = "Data Location?";

  private final String[] Q3_LABELS = new String[] {
    "Not available",
    "Online URL",
    "Archived"
  };

  private final Dimension Q3_RADIOPANEL_DIMS = new Dimension(120, 300);

  private WizardContainerFrame mainWizFrame;

  // first radio Panel - CREATE/IMPORT/DESCRIBE
  private JPanel mainRadioPanel;

  private JPanel q2RadioPanel_import;
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  public DataLocation(WizardContainerFrame mainWizFrame) {

	nextPageID         = DataPackageWizardInterface.TEXT_IMPORT_WIZARD;
    this.mainWizFrame = mainWizFrame;

    INLINE_OR_ONLINE = WizardSettings.ONLINE;

    String inOnString
                = Morpho.getConfiguration().get("dataLocationInlineOnline", 0);

    if ( inOnString!=null && inOnString.equalsIgnoreCase("inline")) {
      INLINE_OR_ONLINE = WizardSettings.INLINE;
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

    ActionListener q2Listener_import = null;
    ActionListener q2Listener_describe = null;

    Box topBox = Box.createVerticalBox();

    JLabel desc = WidgetFactory.makeHTMLLabel(
       "<p><b>Describe and optionally include a data "
      +"table in your data package.</b> You may create a table from "
      +"scratch and populate it using Morpho's spreadsheet-style data editor, "
      +"or you can import certain types of existing data files and use the "
      +"wizard to automatically extract much of the documentation from the data "
      +"file itself. If you "
      +"choose the second option, you will be prompted to review the "
      +"information that "
      +"is extracted and provide any required fields that can not be generated "
      +"automatically.<br></br></p>"
      +"<p>You can also choose to manually enter all of the required fields "
      +"(rather than using the metadata extractor), which is useful for "
      +"proprietary file types like Excel, or other "
      +"file types that are not yet supported.</p>", 7);
    topBox.add(desc);

    final JPanel instance = this;

    ////////////////////////////////////////////////////////
    // QUESTION 2 - "IMPORT - AUTO/MAN" RADIO PANEL
    // COULD BE "ONLINE" OR "INLINE"
    ////////////////////////////////////////////////////////

    q2Listener_import = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "IMPORT - AUTO/MAN ActionEvent: "+e.getActionCommand());
        //undo any hilites:
        onLoadAction();

        //both options require a filechooser...
        setQ3(filechooserPanel);
        fileChooserWidget.getTextArea().requestFocus();

        if (e.getActionCommand().equals(Q2_LABELS_IMPORT[0])) {

          // IMPORT AUTOMATIC
          Log.debug(45, "IMPORT - AUTOMATIC");
          setLastEvent(IMPORT_AUTO);

        } else if (e.getActionCommand().equals(Q2_LABELS_IMPORT[1])) {

          // IMPORT MANUAL
          Log.debug(45, "IMPORT - MANUAL");
          setLastEvent(IMPORT_MAN);
        }
        instance.validate();
        instance.repaint();
      }
    };


    ////////////////////////////////////////////////////////
    // QUESTION 2 - "DESCRIBE - AUTO/MAN" RADIO PANEL
    // (NO DISTRIBUTION, "ONLINE" OR "OFFLINE")
    ////////////////////////////////////////////////////////

    q2Listener_describe = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "DESCRIBE - AUTO/MAN ActionEvent: "+e.getActionCommand());
        //undo any hilites:
        onLoadAction();


        if (e.getActionCommand().equals(Q2_LABELS_DESCRIBE[0])) {

          // DESCRIBE AUTOMATIC
          Log.debug(45, "DESCRIBE - AUTOMATIC");
          setQ3(filechooserPanel);
          fileChooserWidget.getTextArea().requestFocus();
          setLastEvent(DESCRIBE_AUTO);

        } else if (e.getActionCommand().equals(Q2_LABELS_DESCRIBE[1])) {

          // DESCRIBE MANUAL
          Log.debug(45, "DESCRIBE - MANUAL");
          setQ3(q3Widget);
          objNameField.requestFocus();
          setLastEvent(DESCRIBE_MAN_NODATA);  //on Q3 panel, "Not Available" is
                                              //selected by default
        }
        instance.validate();
        instance.repaint();
      }
    };

    q2RadioPanel_import
                                = getRadioPanel(Q2_TITLE_IMPORT,
                                                Q2_LABELS_IMPORT,
                                                q2Listener_import, -1, true);
    final JPanel q2RadioPanel_describe
                                = getRadioPanel(Q2_TITLE_DESCRIBE,
                                                Q2_LABELS_DESCRIBE,
                                                q2Listener_describe, -1, true);


    ////////////////////////////////////////////////////////
    // QUESTION 1 - "CREATE/IMPORT/DESCRIBE" RADIO PANEL
    ////////////////////////////////////////////////////////

    ActionListener q1Listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "got includeOmitRadioPanel command: "+e.getActionCommand());

        //undo any hilites:
        onLoadAction();
        deselectRadioGroup(q2RadioPanel_import);
        deselectRadioGroup(q2RadioPanel_describe);

        if (e.getActionCommand().equals(Q1_LABELS[0])) {

          // CREATE DATA

          setQ2(blankPanel);
          setQ3(blankPanel);
          setLastEvent(CREATE);

        } else if (e.getActionCommand().equals(Q1_LABELS[1])) {

          // IMPORT DATA

          setQ2(q2RadioPanel_import);
          setQ3(blankPanel);
          setLastEvent(IMPORT);

        } else if (e.getActionCommand().equals(Q1_LABELS[2])) {

          // DESCRIBE DATA

          setQ2(q2RadioPanel_describe);
          setQ3(blankPanel);
          setLastEvent(DESCRIBE);
        }
        instance.validate();
        instance.repaint();
      }
    };

    mainRadioPanel = getRadioPanel( Q1_TITLE, Q1_LABELS, q1Listener, 0, true);

    topBox.add(mainRadioPanel);

    topBox.add(WidgetFactory.makeDefaultSpacer());

    secondChoiceContainer = WidgetFactory.makeVerticalPanel(4);

    thirdChoiceContainer  = WidgetFactory.makeVerticalPanel(6);

    topBox.add(secondChoiceContainer);

    this.add(topBox, BorderLayout.NORTH);    

    filechooserPanel  = getFilechooserPanel();
    onlinePanel  = getOnlinePanel();
    offlinePanel = getOfflinePanel();
    nodataPanel = getNoDataPanel();
    blankPanel  = WidgetFactory.makeVerticalPanel(7);
    
    q3Widget = new ThirdChoiceWidget();
    this.add(thirdChoiceContainer, BorderLayout.CENTER);
    
    currentSecondChoicePanel = blankPanel;
    currentThirdChoicePanel  = blankPanel;
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

  private JLabel      fileNameLabelNoData;
  private JTextField  fileNameFieldNoData;

  private JPanel getNoDataPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);

    WidgetFactory.addTitledBorder(panel, Q3_LABELS[0]);

    panel.add(WidgetFactory.makeDefaultSpacer());
    panel.add(WidgetFactory.makeDefaultSpacer());

    ////
    JPanel fileNamePanel = WidgetFactory.makePanel(1);

    fileNameLabelNoData = WidgetFactory.makeLabel("File Name:", true);

    fileNamePanel.add(fileNameLabelNoData);

    fileNameFieldNoData = WidgetFactory.makeOneLineTextField();
    fileNamePanel.add(fileNameFieldNoData);

    panel.add(fileNamePanel);

   
    
    panel.add(WidgetFactory.makeDefaultSpacer());

    panel.add(WidgetFactory.makeDefaultSpacer());

    panel.add(Box.createGlue());

    return panel;
  }

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JLabel      fileNameLabelOnline;
  private JTextField  fileNameFieldOnline;
  private JLabel      urlLabelOnline;
  private JTextField  urlFieldOnline;

  private JPanel getOnlinePanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);

    WidgetFactory.addTitledBorder(panel, Q3_LABELS[1]);

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

  private JLabel            objNameLabel;
  private JLabel            medNameLabel;
  private JTextField        objNameField;
  private JTextField        medNameField;

  private JPanel getOfflinePanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    WidgetFactory.addTitledBorder(panel, Q3_LABELS[2]);

    panel.add(WidgetFactory.makeHTMLLabel(
      "Archived data may be stored on digital media (tapes, disks), "
      +"or printed media (hardcopy).", 1));

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

    panel.add(WidgetFactory.makeHalfSpacer());

    panel.add(WidgetFactory.makeHTMLLabel(
                    "Type of medium on which data is distributed. "
                    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
                    +"eg: Tape,&nbsp;3.5 inch Floppy Disk,&nbsp;hardcopy"
                    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING, 1));


    panel.add(medNamePanel);

    panel.add(WidgetFactory.makeHalfSpacer());

    panel.add(WidgetFactory.makeHTMLLabel(
                    "Enter an identifying name in the space below&nbsp;"
                    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
                    +" eg a title for hardcopy, or a filename for digital media"
                    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING, 1));

    panel.add(objNamePanel);

    panel.add(Box.createGlue());

    return panel;
  }


  private void deselectRadioGroup(JPanel radioPanel)  {

    Component buttonPanelComp = radioPanel.getComponent(1);
    Container buttonPanel     = (Container)buttonPanelComp;
    Component dummyButtonComp = buttonPanel.getComponent(0);
    JRadioButton dummyButton = (JRadioButton)dummyButtonComp;
    dummyButton.setSelected(true);
  }


  private void setLastEvent(short eventFlag) { lastEvent = eventFlag; }


  private short getLastEvent() { return lastEvent; }


  private void setQ2(JPanel newPanel) {

    secondChoiceContainer.remove(currentSecondChoicePanel);
    currentSecondChoicePanel = newPanel;
    secondChoiceContainer.add(currentSecondChoicePanel);
  }


  private void setQ3(JPanel newPanel) {

    thirdChoiceContainer.remove(currentThirdChoicePanel);
    currentThirdChoicePanel = newPanel;
    thirdChoiceContainer.add(currentThirdChoicePanel);
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
    WidgetFactory.unhiliteComponent(objNameLabel);
    WidgetFactory.unhiliteComponent(medNameLabel);

    String prevPageID = mainWizFrame.getPreviousPageID();
    if(prevPageID != null && prevPageID.equals(DataPackageWizardInterface.CODE_IMPORT_PAGE)) {

      // allow only import
      Container radioPanel = (Container)mainRadioPanel.getComponent(1);
      Container middlePanel = (Container) radioPanel.getComponent(1);

      // for CREATE
      JRadioButton jrb = (JRadioButton)middlePanel.getComponent(CREATE_CHOICE);
      jrb.setEnabled(false);
      // for DESCRIBE
      jrb = (JRadioButton)middlePanel.getComponent(DESCRIBE_CHOICE);
      jrb.setEnabled(false);

      // select the IMPORT_CHOICE
      jrb = (JRadioButton)middlePanel.getComponent(IMPORT_CHOICE);
      jrb.setSelected(true);
      setQ2(q2RadioPanel_import);
      setQ3(blankPanel);
      setLastEvent(IMPORT);


    }
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

    /**
     CREATE               - inline/online;  wizard does: man data + man metadata - no TIW
     IMPORT
     IMPORT_AUTO          - inline/online;  wizard does: auto data + auto metadata - File->TIW
     IMPORT_MAN           - inline/online;  wizard does: auto data + man metadata - no TIW
     DESCRIBE
     DESCRIBE_AUTO        - offline/online; wizard does: no data + auto metadata - File->TIW
     DESCRIBE_MAN
     DESCRIBE_MAN_NODATA  - nodata;         wizard does: no data + man metadata - no TIW
     DESCRIBE_MAN_ONLINE  - online;         wizard does: no data + man metadata - no TIW
     DESCRIBE_MAN_OFFLINE - offline;        wizard does: no data + man metadata - no TIW
    **/

    File dataFileObj = null;

    switch (getLastEvent()) {

      case CREATE:
        //no validation required
        distribution = INLINE_OR_ONLINE;
        //create a new empty datafile
        File emptyDataFile = null;
        try {
          emptyDataFile = File.createTempFile("~deleteme", null);
          FileWriter fileWriter = new FileWriter(emptyDataFile);
          fileWriter.write("");
        } catch (IOException ex) {
          Log.debug(1, "error - cannot create a new empty data file!");
          ex.printStackTrace();
          return false;
        }
        setDataFile(emptyDataFile);
        setNextPageID(DataPackageWizardInterface.DATA_FORMAT);
        break;
        //////

      case IMPORT:
      case DESCRIBE:
        //second choice hasn't been made
        WidgetFactory.hiliteComponent(
            getNestedJComponent(currentSecondChoicePanel, 1));
        return false;
        //////

      case IMPORT_AUTO:
        dataFileObj = validateDataFileSelection();
        if (dataFileObj==null) return false;
        setDataFile(dataFileObj);
        distribution = INLINE_OR_ONLINE;
        setNextPageID(DataPackageWizardInterface.TEXT_IMPORT_WIZARD);
//      WizardSettings.setSummaryText(WizardSettings.?????????);
        break;
        //////

      case IMPORT_MAN:
        dataFileObj = validateDataFileSelection();
        if (dataFileObj==null) return false;
        setDataFile(dataFileObj);
        distribution = INLINE_OR_ONLINE;
        setNextPageID(DataPackageWizardInterface.DATA_FORMAT);
//      WizardSettings.setSummaryText(WizardSettings.?????????);
        break;
        //////

      case DESCRIBE_AUTO:
        dataFileObj = validateDataFileSelection();
        if (dataFileObj==null) return false;
        setDataFile(dataFileObj);
        // assume it's offline for now - when we start supporting auto-metadata
        // generation form online files, this will change
        distribution = WizardSettings.OFFLINE;
        setNextPageID(DataPackageWizardInterface.TEXT_IMPORT_WIZARD);
//      WizardSettings.setSummaryText(WizardSettings.?????????);
        break;
        //////

      case DESCRIBE_MAN:
        //should never be called, since next question defaults to "no data"
        break;
        //////

      case DESCRIBE_MAN_NODATA:
        // go directly to last page
//        WizardSettings.setSummaryText(WizardSettings.?????????);
    	  if (fileNameFieldNoData.getText().trim().equals("")) {
              WidgetFactory.hiliteComponent(fileNameLabelNoData);
              fileNameFieldNoData.requestFocus();
              return false;
            }
        distribution = WizardSettings.NODATA;
        setDataFile(null);
        setNextPageID(DataPackageWizardInterface.DATA_FORMAT);
        break;
        //////

      case DESCRIBE_MAN_ONLINE:
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

        distribution = WizardSettings.ONLINE;
        setDataFile(null);
        setNextPageID(DataPackageWizardInterface.DATA_FORMAT);
//      WizardSettings.setSummaryText(WizardSettings.?????????);
//      WizardSettings.setDataLocation(urlFieldOnline.getText().trim());
        break;
        //////

      case DESCRIBE_MAN_OFFLINE:

        //if (medNameField.getText().trim().equals(EMPTY_STRING)) {
    	if(Util.isBlank(medNameField.getText())){
          WidgetFactory.hiliteComponent(medNameLabel);
          return false;
        }
        WidgetFactory.unhiliteComponent(medNameLabel);

        if (objNameField.getText().trim().equals(EMPTY_STRING)) {
          WidgetFactory.hiliteComponent(objNameLabel);
          return false;
        }
        WidgetFactory.unhiliteComponent(objNameLabel);

        distribution = WizardSettings.OFFLINE;
        setDataFile(null);
        setNextPageID(DataPackageWizardInterface.DATA_FORMAT);
        break;
        //////
    }
    return true;
  }


  private void setDataFile(File file) { this.dataFileObj = file; }


  /**
   * returns data file object
   *
   * @return data file object
   */
  public File getDataFile() { return this.dataFileObj; }

  private File validateDataFileSelection() {

    //ensure file URL is set
    String fileURL = fileChooserWidget.getImportFileURL();
    if (fileURL == null) {
      WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
      fileChooserWidget.getButton().requestFocus();
      return null;
    }
    WidgetFactory.unhiliteComponent(fileChooserWidget.getLabel());

    //ensure file URL is valid

  //UNFINISHED - ultimately need to handle remote urls (http, ftp etc?)
    File fileObj = null;
    try {
      fileObj = new File(fileURL.trim());
    } catch (Exception ex) {
      Log.debug(1, "error - cannot read your data file!");
      ex.printStackTrace();
      WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
      fileChooserWidget.getButton().requestFocus();
      return null;
    }

    if (!fileObj.exists()) {
      Log.debug(1, "error - your data file appears to have been moved or deleted!");
      WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
      fileChooserWidget.getButton().requestFocus();
      return null;
    }

    if (!fileObj.isFile()) {
      Log.debug(1, "error - selected location is a directory, not a file!");
      WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
      fileChooserWidget.getButton().requestFocus();
      return null;
    }

    if (!fileObj.canRead()) {
      Log.debug(1, "error - cannot read your data file (permissions problem?)");
      WidgetFactory.hiliteComponent(fileChooserWidget.getLabel());
      fileChooserWidget.getButton().requestFocus();
      return null;
    }
    return fileObj;
  }

  /**
   *  gets the OrderedMap object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the OrderedMap object that contains all the
   *            key/value paired settings for this particular wizard page   *
   * -----------------
   *
   * KEY:
   * inline/online-urn  - means data would have either "inline" distribution
   * elements, or would be a separate file (managed by morpho/metacat),
   * referenced by a URN (ecogrid://etc...), in which case its distribution
   * would be "online". This is currently a preference in config.xml, and
   * defaults to online with URN
   *
   * online-url  - distrbution is also online, but the data file is a
   * web-accessible url (eg at http://www.myhost.com/mypath/etc/ or similar),
   * not an ecogrid:// urn.
   *
   * -----------------
   *
   * CREATE
   * has entities, distribution - inline/online-urn - package viewer displays
   * data table
   *
   * IMPORT_AUTO
   * has entities, distribution - inline/online-urn - package viewer displays
   * data table
   *
   * IMPORT_MAN
   * has entities, distribution - inline/online-urn - package viewer displays
   * data table
   *
   *
   * DESCRIBE_AUTO
   * has entities, distribution - offline - package viewer displays no data
   * table, or maybe just column headers?
   * (note - near-future morpho version may also have distribution as
   * online-url)
   *
   * DESCRIBE_MAN_NODATA  ("data not available" - description only)
   * has entities, *no* distribution elements - package viewer displays no
   * data table, or maybe just column headers?
   *
   * DESCRIBE_MAN_ONLINE
   * has entities, distribution - online-url - package viewer displays no
   * data table, or maybe just column headers?
   * (note - near-future morpho version may follow url and pull data table
   * for display in data viewer?)
   *
   * DESCRIBE_MAN_OFFLINE
   * has entities, distribution - offline - package viewer displays no data
   * table, or maybe just column headers?
   *
   */
  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData() {

    returnMap.clear();

    switch (distribution) {

      case WizardSettings.ONLINE:
    	  if (getDataFile() != null && !Util.isBlank(getDataFile().getName().trim()))
          {
             returnMap.put(OBJECTNAME_XPATH, getDataFile().getName());
          }
    	  else if(!Util.isBlank(fileNameFieldOnline.getText().trim()))
    	  {
    		  returnMap.put(OBJECTNAME_XPATH, fileNameFieldOnline.getText().trim());
    	  }
          else
          {
        	  returnMap.put(OBJECTNAME_XPATH, WizardSettings.UNAVAILABLE);
          }
        if (getDataFile() != null) {
          // if datafile exists, it's a local file referenced by a URN
          String dataFileID = saveDataFileAsTemp(getDataFile());
          returnMap.put(ONLINE_URL_XPATH, URN_ROOT + dataFileID);
        } else {
          // if no datafile, it's an online URL
          returnMap.put(ONLINE_URL_XPATH, urlFieldOnline.getText().trim());
        }
        break;
        //////

      case WizardSettings.INLINE:
        String encoded = encodeAsBase64(getDataFile());
        returnMap.put(INLINE_XPATH, encoded);
        break;
        //////

      case WizardSettings.OFFLINE:
    	if(getLastEvent() == DESCRIBE_AUTO)
    	{
    		if(getDataFile() != null)
        	{
                returnMap.put(OBJECTNAME_XPATH, getDataFile().getName());
        	}
        	else
        	{
        		returnMap.put(OBJECTNAME_XPATH, WizardSettings.UNAVAILABLE);
        	}
        	returnMap.put(MEDIUMNAME_XPATH, HARDDRIVE);
        
    	}
    	else
    	{
    		if(!Util.isBlank(objNameField.getText().trim()))
        	{
                returnMap.put(OBJECTNAME_XPATH, objNameField.getText().trim());
        	}
        	else
        	{
        		returnMap.put(OBJECTNAME_XPATH, WizardSettings.UNAVAILABLE);
        	}
        	
        	if(!Util.isBlank(medNameField.getText().trim()))
        	{
                returnMap.put(MEDIUMNAME_XPATH, medNameField.getText().trim());
        	}
        	else
        	{
        		returnMap.put(MEDIUMNAME_XPATH, WizardSettings.UNAVAILABLE);
        	}
    	}    	
        break;
        //////

      case WizardSettings.NODATA:
    	  //if no data, then miss out the distribution elements altogether. But we need
    	  // object name  	  
    	  if (!Util.isBlank( fileNameFieldNoData.getText().trim()))
          {
             returnMap.put(OBJECTNAME_XPATH,  fileNameFieldNoData.getText().trim());
          }
          else
          {
        	  returnMap.put(OBJECTNAME_XPATH, WizardSettings.UNAVAILABLE);
          }
    	
    	 
      
    }
    return returnMap;
  }


  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular wizard page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular wizard page
   */
  public OrderedMap getPageData(String rootXPath) {

    throw new UnsupportedOperationException(
      "getPageData(String rootXPath) Method Not Implemented");
  }


  /*
   * create a new id,
   * assign id to the data file and save a copy with that id as the
   * name
   */
  private String saveDataFileAsTemp(File f) {
    AccessionNumber an = new AccessionNumber(Morpho.thisStaticInstance);
    String id = an.getNextId();
    FileSystemDataStore fds = new FileSystemDataStore(Morpho.thisStaticInstance);
    try {
      fds.saveTempDataFile(id, new FileInputStream(f));
    } catch (Exception w) {
      Log.debug(1, "error in TIW saving temp data file!");
    }
    return id;
  }


  /*
   *  this method converts the input file to a byte array and then
   *  encodes it as a Base64 string
   */
  private String encodeAsBase64(File f) {
    byte[] b = null;
    long len = f.length();
    if (len > 200000) { // choice of 200000 is arbitrary - DFH
      Log.debug(1, "Data file is too long to be put 'inline'!");
      return null;
    }
    try {
      FileReader fsr = new FileReader(f);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      int chr = 0;
      while ((chr = fsr.read()) != -1) {
        baos.write(chr);
      }
      fsr.close();
      baos.close();
      b = baos.toByteArray();
    } catch (Exception e) {Log.debug(1, "Problem encoding data as Base64!");
    }
    String enc = Base64.encode(b);
    return enc;
  }






  //private void setNextPageID(String id)  { nextPageID = id; }


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
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

    public boolean setPageData(OrderedMap data, String xPathRoot) { return false; }

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
  private JPanel nodataPanel;
  private JPanel blankPanel;
  private JPanel secondChoiceContainer;
  private JPanel currentSecondChoicePanel;
  private JPanel thirdChoiceContainer;
  private JPanel currentThirdChoicePanel;
  private JPanel q3Widget;
  private short  INLINE_OR_ONLINE;
  private File   dataFileObj;

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
  private short distribution;

  private final String EMPTY_STRING         = "";



////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////



  class ThirdChoiceWidget extends JPanel {

    private JPanel q3RadioPanel;
    //private JPanel blankChoicePanel;
    private JPanel currentChoicePanel;

//    public ThirdChoiceWidget(String Q3_TITLE, String[] Q3_LABELS, short[] Q3_EVENTS) {
    public ThirdChoiceWidget() {

      init();
    }

    private void init() {

      this.setLayout(new BorderLayout());

      //blankChoicePanel = WidgetFactory.makePanel(5);

      final JPanel instance = this;

      ////////////////////////////////////////////////////////
      // QUESTION 3 - "NOT AVAILABLE/ONLINE/ARCHIVED" RADIO
      // PANEL (NO DISTRIBUTION, "ONLINE" OR "OFFLINE")
      ////////////////////////////////////////////////////////

      ActionListener q3Listener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {

          Log.debug(45, "QUESTION 3 - NOT AVAILABLE/ONLINE/ARCHIVED: "+e.getActionCommand());

          if (e.getActionCommand().equals(Q3_LABELS[0])) {

            // NOT AVAILABLE
            Log.debug(45, "NOT AVAILABLE");
            setChoicePanel(nodataPanel);
            fileNameFieldNoData.requestFocus();
            setLastEvent(DESCRIBE_MAN_NODATA);

          } else if (e.getActionCommand().equals(Q3_LABELS[1])) {

            // ONLINE
            Log.debug(45, "ONLINE");
            setChoicePanel(onlinePanel);
            fileNameFieldOnline.requestFocus();
            setLastEvent(DESCRIBE_MAN_ONLINE);

          } else if (e.getActionCommand().equals(Q3_LABELS[2])) {

            // ARCHIVED
            Log.debug(45, "ARCHIVED");
            setChoicePanel(offlinePanel);
            objNameField.requestFocus();
            setLastEvent(DESCRIBE_MAN_OFFLINE);
          }
          instance.validate();
          instance.repaint();
        }
      };

      JPanel q3RadioPanel = new JPanel();

      q3RadioPanel.setLayout(new BoxLayout(q3RadioPanel, BoxLayout.Y_AXIS));

      q3RadioPanel.setPreferredSize(Q3_RADIOPANEL_DIMS);
      q3RadioPanel.setMaximumSize(Q3_RADIOPANEL_DIMS);

      q3RadioPanel.add(WidgetFactory.makeLabel(Q3_TITLE, true));
      q3RadioPanel.add(WidgetFactory.makeRadioPanel(Q3_LABELS, 0, q3Listener));
      q3RadioPanel.add(Box.createGlue());

      currentChoicePanel = nodataPanel;

      this.add(q3RadioPanel, BorderLayout.WEST);
      this.add(currentChoicePanel, BorderLayout.CENTER);
    }


    private void setChoicePanel(JPanel panel) {

      this.remove(1);
      currentChoicePanel = panel;
      this.add(currentChoicePanel, BorderLayout.CENTER);
    }
  }


  protected boolean isCreateChoice() {
    Container radioPanel = (Container)mainRadioPanel.getComponent(1);
    Container middlePanel = (Container) radioPanel.getComponent(1);
    // for CREATE
    JRadioButton jrb = (JRadioButton)middlePanel.getComponent(CREATE_CHOICE);
    if (jrb.isSelected()) return true;
    return false;
  }

  protected String getFileName() {
    String ret = "";
    File file = getDataFile();
    if (file!=null) {
      ret = file.getName();
    }
    return ret;
  }


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

  static private File lastDataDir = null;

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
                String homedir = System.getProperty("user.home");
                String osname = System.getProperty("os.name");
                if (lastDataDir==null) {
                  fc.setCurrentDirectory(new File(userdir));
                  if (osname.indexOf("Window")>-1) {  // a windows os
                    File mydocs = new File(homedir+File.separator+"My Documents");
                    if (mydocs.exists()) {
                      fc.setCurrentDirectory(mydocs);
                    }
                  }
                } else {  // use previous dataDirectory
                  fc.setCurrentDirectory(lastDataDir);
                }
                int returnVal = fc.showOpenDialog(WizardContainerFrame.frame);
                File file = null;
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                  file = fc.getSelectedFile();

                  if (file!=null) {
                    lastDataDir = new File(file.getParent());
                    setImportFileURL(file.getAbsolutePath());
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

  protected void setImportFileURL(String filePath) {

    this.importFileURL = filePath;
  }
}
