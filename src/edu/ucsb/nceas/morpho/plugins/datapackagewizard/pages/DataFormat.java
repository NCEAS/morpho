/**
 *  '$RCSfile: DataFormat.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-01 04:09:40 $'
 * '$Revision: 1.39 $'
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

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.Container;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class DataFormat extends AbstractUIPage{

  private final String pageID     = DataPackageWizardInterface.DATA_FORMAT;
  private final String pageNumber = "";

  private final String title      = "Data File Information:";
  private final String subtitle   = "File Format";

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String OTHER_LABEL = "other";
  private final String EMPTY_STRING = "";

  private final String COLUMN_MAJOR = "column";
  private final String ROW_MAJOR    = "row";

  private String orientationSimple  = COLUMN_MAJOR;
  private String orientationComplex = COLUMN_MAJOR;

  private String formatXPath;
  
  private final String PHYSICAL_XPATH
                  = "/eml:eml/dataset/dataTable/physical/";
  private final String TEXT_BASE_XPATH
                  = "/eml:eml/dataset/dataTable/physical/dataFormat/textFormat/";
  private final String SIMPLE_TEXT_XPATH  = TEXT_BASE_XPATH+"simpleDelimited/fieldDelimiter";
  private final String COMPLEX_TEXT_XPATH = TEXT_BASE_XPATH+"complex/";
  private final String PROPRIETARY_XPATH
          = "/eml:eml/dataset/dataTable/physical/dataFormat/externallyDefinedFormat/formatName";
  private final String RASTER_XPATH
          = "/eml:eml/dataset/dataTable/physical/dataFormat/binaryRasterFormat";

  private JPanel radioPanel = null;
  private JPanel simpleTextpanel = null;;
  private JPanel complexTextPanel = null;
  private JPanel proprietaryPanel = null;
  private JPanel rasterPanel = null;
  private JPanel currentPanel = null;
  private JPanel orientationSimpleTextPanel = null;
  private JPanel orientationComplexTextPanel = null;
  private JPanel simpleDelimiterCheckBoxPanel = null;
  private  JPanel proprietaryRadioPanel = null;
  private JLabel radioButtonGrpLabel = null;
 
  
  private final String[] buttonsText = new String[] {
    "Simple delimited text format (uses one or more delimiters throughout the "
    + "file).",
    "Complex text format (delimited fields, fixed width fields, and mixtures "
    + "of the two).",
    "Non-text or proprietary formatted file that is externally defined "
    + "(e.g. 'Microsoft Excel')."
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
    OTHER_LABEL
  };
  
  private static final String FIXEDWIDTHLABEL = "Fixed-Width";
  private static final String TEXTFIXED = "textFixed";
  private static final String DELIMITEDLABEL = "Delimited";
  private static final String TEXTDELIMITED = "textDelimited";
  private String[] pickListVals = new String[] {
	FIXEDWIDTHLABEL,
	DELIMITEDLABEL
  };

  private String delim_tab       = null;
  private String delim_comma     = null;
  private String delim_space     = null;
  private String delim_semicolon = null;
  private boolean delim_other    = false;
  private CustomList list;
  private WizardContainerFrame mainWizFrame;
  private boolean fromCorrectionWizard = false;
  private static final int SIMPLETEXTCHOICE = 0;
  private static final int COMPLEXTEXTCHOICE =1;
  private static final int PROPRIETARYCHOICE = 2;
  private static final int COLUMNORIENTATIONCHOICE = 0;
  private static final int ROWORIENTATIONCHOICE = 1;
  private static final int TABCHOICE = 0;
  private static final int COMMACHOICE = 1;
  private static final int SPACECHOICE = 2;
  private static final int SEMICOLONCHOICE = 3;
  private static final int OTHERCHOICE= 4;
  
  private final Map mimeMap = WizardSettings.getSupportedMIMETypesForEntity(
          WizardSettings.ENTITY_DATATABLE);
  // proprietaryButtonsText array is one elem larger than mime map, because
  // we need to add an entry for "other"...
  final String [] proprietaryButtonsText = new String[mimeMap.size() + 1];
 
 // private String fileName = "";

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public DataFormat(WizardContainerFrame mainWizFrame) {
	nextPageID       = DataPackageWizardInterface.ENTITY;
    this.mainWizFrame = mainWizFrame;
    init();
  }
  
  /**
   * Constructor for correction wizard 
   * @param fromCorrectionWizard
   */
  public DataFormat(Boolean fromCorrectionWizard)
  {
	  this.fromCorrectionWizard = fromCorrectionWizard.booleanValue();
	  init();
  }
  

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BorderLayout());

    Box topBox = Box.createVerticalBox();

    JLabel desc = WidgetFactory.makeHTMLLabel(
      "<b>Enter some information about your data file</b>.", 2);
    topBox.add(desc);
    topBox.add(WidgetFactory.makeDefaultSpacer());

    radioButtonGrpLabel
          = WidgetFactory.makeHTMLLabel("What is the format of your data?", 1);
    topBox.add(radioButtonGrpLabel);

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



    radioPanel = WidgetFactory.makeRadioPanel(buttonsText, -1, listener);

    topBox.add(radioPanel);

    topBox.add(WidgetFactory.makeDefaultSpacer());

    this.add(topBox, BorderLayout.NORTH);

    simpleTextpanel  = getSimpleTextpanel();
    complexTextPanel = getComplexTextPanel();
    proprietaryPanel = getProprietaryPanel();
//    rasterPanel      = getRasterPanel();

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

    JLabel spacerLabel = WidgetFactory.makeLabel(EMPTY_STRING, false);

    orientationPanel.add(spacerLabel);
    
    orientationSimpleTextPanel = getOrientationRadioPanel();
    
    orientationPanel.add(orientationSimpleTextPanel);

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

  private JLabel listLabel;

  private JPanel getComplexTextPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);

    WidgetFactory.addTitledBorder(panel, buttonsText[1]);

    ////
    panel.add(WidgetFactory.makeHTMLLabel("Data Attributes are arranged in:", 1));

    JPanel orientationPanel = WidgetFactory.makePanel(2);

    JLabel spacerLabel = WidgetFactory.makeLabel(EMPTY_STRING, false);

    orientationPanel.add(spacerLabel);

    orientationComplexTextPanel = getOrientationRadioPanel();
    
    orientationPanel.add(orientationComplexTextPanel);

    panel.add(orientationPanel);

    ////
    panel.add(WidgetFactory.makeDefaultSpacer());

    listLabel = WidgetFactory.makeHTMLLabel(
                        "Define the delimited fields and/or fixed width fields "
                        +"that describe how the data is structured:", 1);

    panel.add(listLabel);


    JComboBox pickList = WidgetFactory.makePickList(pickListVals, false, 0,

        new ItemListener() {

          public void itemStateChanged(ItemEvent e) {

            Log.debug(45, "got PickList state changed; src = "
                                          +e.getSource().getClass().getName());
          }
        });

    Object[] colTemplates = new Object[] { pickList, new JTextField() };

    String[] colNames = new String[] {
      "Fixed-Width or Delimited?",
      "Width or Delimiter Character:"
    };

    list = WidgetFactory.makeList(colNames, colTemplates, 4,
                                  true, false, false, true, true, true);
    panel.add(list);

    return panel;
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JLabel      proprietaryLabel;
  private String      proprietaryText;
  private JTextField  otherProprietaryTextField;

  private JPanel getProprietaryPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);

    WidgetFactory.addTitledBorder(panel, buttonsText[2]);

    panel.add(WidgetFactory.makeDefaultSpacer());

    

   

    int i = 0;

    for (Iterator it = mimeMap.keySet().iterator(); it.hasNext(); ) {

      Object nextObj = it.next();
      if (nextObj==null) continue;
      proprietaryButtonsText[i++] = ((String)nextObj).trim();
    }

    otherProprietaryTextField = WidgetFactory.makeOneLineTextField();
    Dimension dim = new Dimension(
                          WizardSettings.DIALOG_WIDTH/2,
                          WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS.height);
    otherProprietaryTextField.setPreferredSize(dim);
    otherProprietaryTextField.setMaximumSize(  dim);

    ActionListener listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        //undo any hilites:
        onLoadAction();
        if (e.getActionCommand().equals(OTHER_LABEL)) {

          setProprietaryText(OTHER_LABEL, mimeMap);

        } else {

          setProprietaryText(e.getActionCommand(), mimeMap);
        }
      }};

    proprietaryButtonsText[i] = OTHER_LABEL;

    ////
    JPanel proprietaryPanel = new JPanel();
    proprietaryPanel.setLayout(new BoxLayout(proprietaryPanel,
                                              BoxLayout.Y_AXIS));

    proprietaryLabel = WidgetFactory.makeLabel("Format:", true);
    JPanel leftJustifyPanel = WidgetFactory.makePanel(1);
    leftJustifyPanel.add(proprietaryLabel);
    leftJustifyPanel.add(Box.createGlue());
    proprietaryPanel.add(leftJustifyPanel);

    final int INITIAL_SELECTION = 0;

    proprietaryRadioPanel = WidgetFactory.makeRadioPanel( proprietaryButtonsText,
                                                      INITIAL_SELECTION,
                                                      listener);
    setProprietaryText(proprietaryButtonsText[INITIAL_SELECTION], mimeMap);

    JPanel radioJustifyPanel = new JPanel();
    radioJustifyPanel.setLayout(new BoxLayout(radioJustifyPanel,
                                              BoxLayout.X_AXIS));
    radioJustifyPanel.add(WidgetFactory.makeLabel(EMPTY_STRING, false));
    radioJustifyPanel.add(proprietaryRadioPanel);
    proprietaryPanel.add(radioJustifyPanel);

    JPanel otherJustifyPanel = new JPanel();
    otherJustifyPanel.setLayout(new BoxLayout(otherJustifyPanel,
                                              BoxLayout.X_AXIS));
    otherJustifyPanel.setPreferredSize(WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
    otherJustifyPanel.setMaximumSize(WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
    otherJustifyPanel.add(WidgetFactory.makeLabel(EMPTY_STRING, false));
    otherJustifyPanel.add(otherProprietaryTextField);
    otherJustifyPanel.add(Box.createGlue());
    proprietaryPanel.add(otherJustifyPanel);

    JPanel otherHelpJustifyPanel = new JPanel();
    otherHelpJustifyPanel.setLayout(new BoxLayout(otherHelpJustifyPanel,
                                                  BoxLayout.X_AXIS));
    otherHelpJustifyPanel.add(WidgetFactory.makeLabel(EMPTY_STRING, false));
    JLabel help = WidgetFactory.makeHTMLLabel(
      WizardSettings.HTML_EXAMPLE_FONT_OPENING
      +"If your format does not appear in the above list, select \""
      +OTHER_LABEL+"\" and enter a description in the field "
      +"above. <br>This would preferably be in the form of a standard MIME "
      +"type (e.g: application/msword ), but if you do not know the MIME type, "
      +"enter a text description"+WizardSettings.HTML_EXAMPLE_FONT_CLOSING, 4);

    Dimension helpDim = new Dimension(
                      WizardSettings.DIALOG_WIDTH -
                            2*WizardSettings.WIZARD_CONTENT_LABEL_DIMS.width,
                      3*WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.height);

    help.setPreferredSize(helpDim);
    help.setMaximumSize(helpDim);

    Dimension helpPanelDim = new Dimension(
                      WizardSettings.DIALOG_WIDTH,
                      3*WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.height);

    otherHelpJustifyPanel.setPreferredSize(helpPanelDim);
    otherHelpJustifyPanel.setMaximumSize(helpPanelDim);

    otherHelpJustifyPanel.add(help);
    otherHelpJustifyPanel.add(Box.createGlue());

    proprietaryPanel.add(otherHelpJustifyPanel);

    panel.add(proprietaryPanel);

    panel.add(WidgetFactory.makeDefaultSpacer());
    panel.add(Box.createGlue());

    return panel;
  }


  //
  //  sets the value of the proprietaryString variable - based on a the passed
  //  String param which is the selected radiobutton label. mimeMap is the Map
  //  containing the radiobutton labels as keys and the MIME types as values
  //
  private void setProprietaryText(String labelString, Map mimeMap) {

    if (labelString==null) return;

    //if it's "other", set text to empty string:
    if (labelString.equals(OTHER_LABEL)) {

      otherProprietaryTextField.setEnabled(true);
      otherProprietaryTextField.requestFocus();
      proprietaryText = EMPTY_STRING;

    } else {

      otherProprietaryTextField.setEnabled(false);
      proprietaryText = ((String)(mimeMap.get(labelString.trim()))).trim();
    }
  }
  
  /*
   * When we get a delimiter from eml, we need to select check box accordingly in UI
   */
  private void selectSimpleDelimiterCheckBox(String delimiter) throws Exception
  {
	  if(delimiter != null)
	  {
		  if(delimiter.equals(WizardSettings.HEX_VALUE_TAB))
		  {
			  Log.debug(35, "click on tab box in DataFormat selectSimpleDelimiterCheckBox");
			  selectSimpleDelimiterCheckBox(TABCHOICE);
		  }
		  else if (delimiter.equals(","))
		  {
			  Log.debug(35, "click on comma box in DataFormat selectSimpleDelimiterCheckBox");
			  selectSimpleDelimiterCheckBox(COMMACHOICE);
		  }
		  else if (delimiter.equals(WizardSettings.HEX_VALUE_SPACE))
		  {
			  Log.debug(35, "click on space box in DataFormat selectSimpleDelimiterCheckBox");
			  selectSimpleDelimiterCheckBox(SPACECHOICE);
		  }
		  else if(delimiter.equals(";"))
		  {
			  Log.debug(35, "click on semicolon box in DataFormat selectSimpleDelimiterCheckBox");
			  selectSimpleDelimiterCheckBox(SEMICOLONCHOICE);
		  }
		  else
		  {
			  Log.debug(35, "click on other box in DataFormat selectSimpleDelimiterCheckBox");
			  selectSimpleDelimiterCheckBox(OTHERCHOICE);
			  otherDelimTextFieldSimple.setEnabled(true);
			  otherDelimTextFieldSimple.setText(delimiter);
		  }
		  
	
			
	  }
  }
  
  /*
   * Selects the check box base on index in simple delimiter check box panel
   */
  private void selectSimpleDelimiterCheckBox(int index) throws Exception
  {
	  if (simpleDelimiterCheckBoxPanel != null)
	  {
		try
		{
		    JCheckBox box = (JCheckBox) simpleDelimiterCheckBoxPanel.getComponent(index);
		    box.setSelected(true);
		}
		catch(Exception e)
		{
			throw e;
		}
	  }
	  else
	  {
		  throw new Exception("The simpleDelimiter check box is null in DataFormat.selectSimpeDelimterCheckBox");
	  }
  }
  
  /*
   * Click radio button for first question.
   * 0 - simple delimiter text
   * 1 -  complex delimiter text 
   * 2 -  non-text proprietary 
   */
  private void clickDataFormatRadioButton(int index) throws Exception
  {
	     clickRadionButton(radioPanel, index);
  }
  
  /*
   * Click the radio button for data orientation.
   * 0 - column orientation
   * 1 - row orientation
   */
  private void clickDataOrientationButtonInSimpleTextFormat(String orient) throws Exception
  {
	  clickDataOrientationButton(orientationSimpleTextPanel, orient);
  }
  
  /*
   * Click the radio button for data orientation.
   * 0 - column orientation
   * 1 - row orientation
   */
  private void clickDataOrientationButtonInComplexTextFormat(String orient) throws Exception
  {
	  clickDataOrientationButton(orientationComplexTextPanel, orient);
  }
  
  /*
   * Click the radio button base on orientation.
   * column - click 0
   * row - click 1
   */
  private void clickDataOrientationButton(JPanel container, String orientation) throws Exception
  {
	  if(orientation != null && orientation.equals(COLUMN_MAJOR))
	  {
		  clickRadionButton(container, COLUMNORIENTATIONCHOICE);
	  }
	  else if(orientation != null && orientation.equals(ROW_MAJOR))
	  {
		  clickRadionButton(container, ROWORIENTATIONCHOICE);
	  }
	  else
	  {
		  throw new Exception("Morpho couldn't understand the orienation "+orientation);
	  }
		   
  }
  
  /*
   * Click button in radio panel
   */
  private void clickRadionButton(JPanel container, int index) throws Exception
  {
	  if(container != null)
	  {
		 try
		 {
		     Container radio = (Container)container.getComponent(1);
		     JRadioButton button = (JRadioButton)radio.getComponent(index);
		     button.doClick();
		 }
		 catch(Exception e)
		 {
			 throw e;
		 }
	  }
	  else
	  {
		 Log.debug(10, "The Radion button container is null and we couldn't click it in DataFormat.clickRadioButton");
	     throw new Exception("The Radion button container is null and we couldn't click it in DataFormat.clickRadioButton");
	  }
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
	 boolean flag = false;
	if (!fromCorrectionWizard)
	{
       AbstractUIPage prevPage = mainWizFrame.getPreviousPage();
       flag = ((DataLocation)prevPage).isCreateChoice();
	}
    //fileName = ((DataLocation)prevPage).getFileName();
    Container middlePanel = (Container) radioPanel.getComponent(1);
    JRadioButton jrb = (JRadioButton)middlePanel.getComponent(1);
    if (flag) {
      jrb.setEnabled(false);
    } else {
      jrb.setEnabled(true);
    }
    WidgetFactory.unhiliteComponent(radioButtonGrpLabel);
    WidgetFactory.unhiliteComponent(proprietaryLabel);
    WidgetFactory.unhiliteComponent(listLabel);
    WidgetFactory.unhiliteComponent(otherProprietaryTextField);
    otherProprietaryTextField.setForeground(
                                      WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
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

      WidgetFactory.hiliteComponent(radioButtonGrpLabel);
      return false;

    } else if (formatXPath==SIMPLE_TEXT_XPATH) {

      if (delim_tab==null && delim_comma==null && delim_space==null
                          && delim_semicolon==null && delim_other==false) {

        WidgetFactory.hiliteComponent(delimiterLabel);
        return false;
      }
      if (delim_other==true) {

        String otherTxt = otherDelimTextFieldSimple.getText();
        //if (otherTxt==null || otherTxt.equals(EMPTY_STRING)) {
        if (Util.isBlank(otherTxt)) {

          WidgetFactory.hiliteComponent(otherDelimTextFieldSimple);
          otherDelimTextFieldSimple.requestFocus();
          return false;
        }
      }


    } else if (formatXPath==COMPLEX_TEXT_XPATH) {

      list.fireEditingStopped();

      OrderedMap listNVP = getCmplxDelimListAsNVP();

      if (listNVP==null || listNVP.size()<1) {
        WidgetFactory.hiliteComponent(listLabel);
        return false;
      }
      if (!listContainsOnlyPosNumericWidths()) {
        WidgetFactory.hiliteComponent(listLabel);
        return false;
      }
      WidgetFactory.unhiliteComponent(listLabel);


    } else if (formatXPath==PROPRIETARY_XPATH) {

        if (proprietaryText==null || proprietaryText.equals(EMPTY_STRING)) {

          //gets here only if user has selected "OTHER" - so need to get entered
          //value
          proprietaryText = otherProprietaryTextField.getText().trim();

          //if actual values is still empty string, user hasn't entered anything
          //if (proprietaryText.equals(EMPTY_STRING)) {
          if (Util.isBlank(proprietaryText)) {

            WidgetFactory.hiliteComponent(otherProprietaryTextField);
            return false;
          }
        }

//  } else if (formatXPath==RASTER_XPATH) {

    }
    return true;
  }



  //
  //  returns false if any column 0 entry is "fixed width" and its corresponding
  //  col 1 entry is not a number
  //
  private boolean listContainsOnlyPosNumericWidths() {

    boolean returnVal = true;
    List rowLists = list.getListOfRowLists();
    String nextWidthStr = null;

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;

      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;

      boolean nextCol0IsNull = (nextRow.get(0)==null);
      boolean nextCol1IsNull = (nextRow.get(1)==null);

      if (nextCol0IsNull || nextCol1IsNull) continue;

      if (nextRow.get(0).equals(pickListVals[0])) {  // fixed width...
        nextWidthStr = (String)(nextRow.get(1));

        if (!(nextWidthStr.trim().equals(EMPTY_STRING))) {

          if (!WizardSettings.isFloat(nextWidthStr)) returnVal = false;
          else {
            returnVal = (Float.parseFloat(nextWidthStr) > 0);
          }
        }
      }
    }
    return returnVal;
  }




  private OrderedMap listResultsMap = new OrderedMap();
  //
  private OrderedMap getCmplxDelimListAsNVP() {

    listResultsMap.clear();

    // CHECK FOR AND ELIMINATE EMPTY ROWS...
    list.deleteEmptyRows( CustomList.OR,
                          new short[] { CustomList.NULL,
                                        CustomList.EMPTY_STRING_NOTRIM } );

    int fixedIndex					= 1;
    int delimitedIndex				= 1;
    StringBuffer buff       = new StringBuffer();
    List rowLists           = list.getListOfRowLists();
    String fixedDelimStr    = null;

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      Object nextRowObj = it.next();

      if (nextRowObj==null) continue;

      List nextRow = (List)nextRowObj;

      if (nextRow.size() < 1) continue;

      if (nextRow.get(0).equals(pickListVals[0])) {

        fixedDelimStr = "textFixed[" + (fixedIndex++) + "]/fieldWidth";

      } else if (nextRow.get(0).equals(pickListVals[1])) {

        fixedDelimStr = "textDelimited[" + (delimitedIndex++) + "]/fieldDelimiter";

      }

      String nextVal = (String)(nextRow.get(1));

      // substitute hex values for tabs and spaces:
      if (nextVal.equals("\\t")) nextVal = WizardSettings.HEX_VALUE_TAB;
      if (nextVal.equals(" ")) nextVal  = WizardSettings.HEX_VALUE_SPACE;

      buff.delete(0,buff.length());
      buff.append(COMPLEX_TEXT_XPATH);
      buff.append(fixedDelimStr);
      listResultsMap.put(buff.toString(), nextVal);
    }

    return listResultsMap;

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
      //returnMap.put(PHYSICAL_XPATH+"objectName",fileName);
      /*if(!Util.isBlank(fileName))
      {
           returnMap.put(PHYSICAL_XPATH+"objectName",fileName);
      }
      else
      {
       	 returnMap.put(PHYSICAL_XPATH+"objectName",WizardSettings.UNAVAILABLE);
      }*/
      returnMap.put(TEXT_BASE_XPATH+"attributeOrientation", orientationSimple);

      int index=1;
      StringBuffer buff = new StringBuffer();

      if (delim_tab!=null) {

        buff.delete(0,buff.length());
        buff.append(SIMPLE_TEXT_XPATH);
        buff.append("[");
        buff.append(index++);
        buff.append("]");
        returnMap.put(buff.toString(), delim_tab);
      }

      if (delim_comma!=null) {

        buff.delete(0,buff.length());
        buff.append(SIMPLE_TEXT_XPATH);
        buff.append("[");
        buff.append(index++);
        buff.append("]");
        returnMap.put(buff.toString(), delim_comma);
      }

      if (delim_space!=null) {

        buff.delete(0,buff.length());
        buff.append(SIMPLE_TEXT_XPATH);
        buff.append("[");
        buff.append(index++);
        buff.append("]");
        returnMap.put(buff.toString(), delim_space);
      }

      if (delim_semicolon!=null) {

        buff.delete(0,buff.length());
        buff.append(SIMPLE_TEXT_XPATH);
        buff.append("[");
        buff.append(index++);
        buff.append("]");
        returnMap.put(buff.toString(), delim_semicolon);
      }

      if (delim_other==true) {

        buff.delete(0,buff.length());
        buff.append(SIMPLE_TEXT_XPATH);
        buff.append("[");
        buff.append(index++);
        buff.append("]");
        returnMap.put(buff.toString(), otherDelimTextFieldSimple.getText());
         // do not ".trim()"!!!
      }


    } else if (formatXPath==COMPLEX_TEXT_XPATH)  {
      /*if(!Util.isBlank(fileName))
      {
        returnMap.put(PHYSICAL_XPATH+"objectName",fileName);
      }
      else
      {
    	 returnMap.put(PHYSICAL_XPATH+"objectName",WizardSettings.UNAVAILABLE);
      }*/
      returnMap.put(TEXT_BASE_XPATH+"attributeOrientation", orientationComplex);

      returnMap.putAll(getCmplxDelimListAsNVP());

    } else if (formatXPath==PROPRIETARY_XPATH)  {
      //returnMap.put(PHYSICAL_XPATH+"objectName",fileName);
      /*if(!Util.isBlank(fileName))
      {
           returnMap.put(PHYSICAL_XPATH+"objectName",fileName);
      }
      else
      {
       	 returnMap.put(PHYSICAL_XPATH+"objectName",WizardSettings.UNAVAILABLE);
      }*/
      returnMap.put(PROPRIETARY_XPATH, proprietaryText);

//    } else if (formatXPath==RASTER_XPATH) {

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

  private final JTextField otherDelimTextFieldSimple
                                          = WidgetFactory.makeOneLineTextField();

  private JPanel getDelimiterCheckBoxPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);

    simpleDelimiterCheckBoxPanel = WidgetFactory.makeCheckBoxPanel(delimiterCheckBoxesText, -1,
      new ItemListener() {

        public void itemStateChanged(ItemEvent e) {

          String cmd = ( (JCheckBox)(e.getSource()) ).getActionCommand();
          int stateChange = e.getStateChange();

          Log.debug(45, "got checkBox state changed: "+cmd
                                +"; type of state change = "
                +((stateChange==ItemEvent.SELECTED)? "SELECTED": "UNSELECTED"));

          if (cmd.indexOf(delimiterCheckBoxesText[0])==0) {

            delim_tab       = (stateChange==ItemEvent.SELECTED)?
                                          WizardSettings.HEX_VALUE_TAB : null;

          } else if (cmd.indexOf(delimiterCheckBoxesText[1])==0) {

            delim_comma     = (stateChange==ItemEvent.SELECTED)? "," : null;

          } else if (cmd.indexOf(delimiterCheckBoxesText[2])==0) {

            delim_space     = (stateChange==ItemEvent.SELECTED)?
                                          WizardSettings.HEX_VALUE_SPACE : null;

          } else if (cmd.indexOf(delimiterCheckBoxesText[3])==0) {

            delim_semicolon = (stateChange==ItemEvent.SELECTED)? ";" : null;

          } else if (cmd.indexOf(delimiterCheckBoxesText[4])==0) {

            delim_other = (stateChange==ItemEvent.SELECTED);
            otherDelimTextFieldSimple.setEnabled(delim_other);
            otherDelimTextFieldSimple.requestFocus();

          }
        }
      });
    panel.add(simpleDelimiterCheckBoxPanel);

    otherDelimTextFieldSimple.setPreferredSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    otherDelimTextFieldSimple.setMaximumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    otherDelimTextFieldSimple.setEnabled(false);
    JPanel otherPanel = new JPanel();
    otherPanel.setLayout(new BoxLayout(otherPanel, BoxLayout.X_AXIS));
    otherPanel.add(otherDelimTextFieldSimple);
    otherPanel.add(Box.createGlue());
    panel.add(otherPanel);

    return panel;
  }



  private JPanel getOrientationRadioPanel() {

    return WidgetFactory.makeRadioPanel(orientButtonsText, 0,
      new ActionListener() {

        public void actionPerformed(ActionEvent e) {

          Log.debug(45, "got radiobutton command: "+e.getActionCommand());

          if (e.getActionCommand().equals(orientButtonsText[0])) {

            setOrientation(COLUMN_MAJOR);

          } else if (e.getActionCommand().equals(orientButtonsText[1])) {

            setOrientation(ROW_MAJOR);
          }
        }
      });
  }


  private void setOrientation(String orient) {

    if (formatXPath.equals(SIMPLE_TEXT_XPATH))  orientationSimple  = orient;
    if (formatXPath.equals(COMPLEX_TEXT_XPATH)) orientationComplex = orient;
  }



  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return pageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

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
   * Set the data in ordered map format to this page. 
   * It is always return false. (TODO)
   */
  public boolean setPageData(OrderedMap data, String xPathRoot) 
  {
	  boolean success = false;
	  Log.debug(35, "In DataFormat.setPageData, the xpathRoot is "+xPathRoot+" and map is "+data.toString() );
	  if(data == null && xPathRoot == null)
	  {
		  Log.debug(30, "The map or xpathroot is null in DataFormat.setPageData and return false");
		  return success;
	  }
	  Vector keyNeedToDelete = new Vector();
	  formatXPath = findFormatType(data, xPathRoot);
	  Iterator keyIt = data.keySet().iterator();
	   String nextXPath = null;
	   String nextVal = null;
	  if (formatXPath.equals(SIMPLE_TEXT_XPATH))
	  {
		  try
		  {
			  clickDataFormatRadioButton(SIMPLETEXTCHOICE);
			  while(keyIt.hasNext())
			  {
				 nextXPath = (String)keyIt.next();
			     if(nextXPath == null)
			     {
			    	 continue;
			     }
			     else if(nextXPath.equals(xPathRoot+ "/textFormat/attributeOrientation"))
			     {
				   nextVal = (String)data.get(nextXPath);
				   Log.debug(35, "DataFormat.setPageData - in simple text format to set Orientaion to "+nextVal);
			       setOrientation(nextVal);
			       clickDataOrientationButtonInSimpleTextFormat(nextVal);
			       
			     }
			     else if(nextXPath.startsWith(xPathRoot+"/textFormat/simpleDelimited/fieldDelimiter"))
			     {
			    	 nextVal = (String)data.get(nextXPath);
			    	 Log.debug(35, "DataFormat.setPageData - in simple text format to get field delimiter "+nextVal);
			    	 selectSimpleDelimiterCheckBox(nextVal);
			     }			     
			     keyNeedToDelete.add(nextXPath);			     
			  }
		  }
		  catch(Exception e)
		  {
			  Log.debug(5, "Couldn't popluate metadata into DataForat page since "+e.getMessage());
		      return success;
		  }
		  success = this.removeAllDataInMap(data, keyNeedToDelete);
		  return success;
	  }
	  else if(formatXPath.equals(COMPLEX_TEXT_XPATH))
	  {		
		  try
		  {
			  clickDataFormatRadioButton(COMPLEXTEXTCHOICE);
			  list.removeAllRows();
			  while(keyIt.hasNext())
			  {
				 nextXPath = (String)keyIt.next();
			     if(nextXPath == null)
			     {
			    	 continue;
			     }
			     else if(nextXPath.equals(xPathRoot+ "/textFormat/attributeOrientation"))
			     {
				   nextVal = (String)data.get(nextXPath);
				   Log.debug(35, "DataFormat.setPageData - in complex text format to set Orientaion to "+nextVal);
			       setOrientation(nextVal);
			       clickDataOrientationButtonInComplexTextFormat(nextVal);
			       
			     }
			     else if(nextXPath.startsWith(xPathRoot+"/textFormat/complex/"+TEXTFIXED))
			     {
			    	 nextVal = (String)data.get(nextXPath);
			    	 Log.debug(35, "DataFormat.setPageData - in complex text format to get textFixed value "+nextVal);
			    	 addRowToComplexFormatList(FIXEDWIDTHLABEL, nextVal);
			    	
			    	
			     }
			     else if(nextXPath.startsWith(xPathRoot+"/textFormat/complex/"+TEXTDELIMITED))
		    	 {
			    	 nextVal = (String)data.get(nextXPath);
		    		 Log.debug(35, "DataFormat.setPageData - in complex text format to get textFixed value "+nextVal);
		    		 addRowToComplexFormatList(DELIMITEDLABEL, nextVal);
		    	 }
			     else
		    	 {
		    		 throw new Exception("Morpho couldn't understander the complex format "+nextXPath);
		    	 }
			     keyNeedToDelete.add(nextXPath);			     
			  }
		  }
		  catch(Exception e)
		  {
			  Log.debug(5, "Couldn't popluate metadata into DataForat page since "+e.getMessage());
		      return success;
		  }
		  success = removeAllDataInMap(data, keyNeedToDelete);
		  return success;
	  }
	  else if(formatXPath.equals(PROPRIETARY_XPATH))
	  {
		  try
		  {
			  clickDataFormatRadioButton(PROPRIETARYCHOICE);
			  while(keyIt.hasNext())
			  {
				 nextXPath = (String)keyIt.next();
			     if(nextXPath == null)
			     {
			    	 continue;
			     }
			     else if(nextXPath.equals(xPathRoot+ "/externallyDefinedFormat/formatName"))
			     {
                    nextVal = (String)data.get(nextXPath);
                    Log.debug(35, "DataFormat.setPageData - in external format the format name is "+nextVal);
			        setProprietaryCheckBox(nextVal);	       
			     }
			     keyNeedToDelete.add(nextXPath);			     
			  }
		  }
		  catch(Exception e)
		  {
			  Log.debug(5, "Couldn't popluate metadata into DataForat page since "+e.getMessage());
		      return success;
		  }
		  success = this.removeAllDataInMap(data, keyNeedToDelete);
		  return success;
	  }
	  else
	  {
		  Log.debug(30, "Couldn't find the data format type with xpathroot "+xPathRoot+" in the map"+data);
		  return false;
	  } 
  }
  
  /*
   * Sets the check box for given proprietary name
   */
  private void setProprietaryCheckBox(String proprietaryName) throws Exception
  {
	  if (mimeMap != null && proprietaryName != null)
	  {
		
			  Iterator it = mimeMap.keySet().iterator();
			  int index = 0;
			  while( it.hasNext() ) 
			  {

			      String key = (String)it.next();
			      String value = (String)mimeMap.get(key);
			      if(value != null && value.trim().equals(proprietaryName.trim()))
			      {
			    	  Log.debug(30, "We found the label "+key+" for mime type "+value);
			    	  otherProprietaryTextField.setEnabled(false);
			    	  clickRadionButton(this.proprietaryRadioPanel, index);
			          proprietaryText = value;
			    	  return;
			      }
			      index++;
			  }
			  //it is other field.
			  Log.debug(30, "We found don't find a label for mime type "+proprietaryName+". It clicks other button.");
			  clickRadionButton(proprietaryRadioPanel, mimeMap.size());
			  otherProprietaryTextField.setEnabled(true);
			  otherProprietaryTextField.setText(proprietaryName);
		      proprietaryText = proprietaryName;
		
	  }
	  else
	  {
		  throw new Exception("Morpho couldn't get mine-type configuration for data format");
	  }
  }
  
  /*
   * Add a row to complext format list. It either be fixed-width or delimited
   */
  private void addRowToComplexFormatList(String path, String value) throws Exception
  {
	  if(path != null && value != null)
	  {
		  List newRow = new ArrayList();
		  
		  if(path.equals(FIXEDWIDTHLABEL))
		  {
             try
             {
            	 Float num =Float.parseFloat(value);
            	 if(num <0)
            	 {
            		 throw new Exception("The value for fixed text "+value+" couldn't be less than 0");
            	 }
             }
             catch(Exception e)
             {
            	 throw new Exception("The value for fixed text "+value+"is  not a number");
             }
			 newRow.add(FIXEDWIDTHLABEL);  
		  }
		  else if(path.equals(DELIMITEDLABEL))
		  {
			  newRow.add(DELIMITEDLABEL);
			  if (value.equals(WizardSettings.HEX_VALUE_TAB)) 
			  {
				  value = "\\t";
			  }
			  else if (value.equals(WizardSettings.HEX_VALUE_SPACE)) 
		      {
		    	  value  = " ";
		      }
		  }
		  else
		  {
			  throw new Exception ("Morpho couldn't recognize the complex format "+path);
		  }
		  newRow.add(value);
		  list.addRow(newRow);
	  }
	  else
	  {
		  throw new Exception("Could add a row to complex format list in DataLocation since the key/value is null ");
	  }
  }
  
  /*
   * Removes the key which stored in keyList from the map.
   * After removing, it returns true if map is empty.
   */
  private boolean removeAllDataInMap(OrderedMap map, Vector keyList)
  {
	  boolean success = false;
	  if(map == null)
	  {
		  success = true;
		  return success;
	  }
	  else
	  {
		  if(keyList == null)
		  {
			  return success;
		  }
		  else
		  {
			  for(int i=0; i<keyList.size(); i++)
			  {
				  String key = (String)keyList.elementAt(i);
				  if(key != null)
				  {
					  map.remove(key);
				  }
			  }
			  success = map.isEmpty();
		  }
	  }
	  if(success)
	  {
		  Log.debug(35, "Map in DataFormat.setPageData was removed completely!");
	  }
	  else
	  {
		  Log.debug(35, "Map in DataFormat.setPageData couldn't be removed completely. It still has "+map.toString());
	  }
	  return success;
  }
  
  /*
   * Find out the text format type base on the data in the given ordered map
   */
  private String findFormatType(OrderedMap map, String xPath) {

	    ///// check for simple text
	    if(map != null)
	    {
	    	Iterator keyIt = map.keySet().iterator();
	    	while(keyIt.hasNext())
			{
				 String nextPath = (String)keyIt.next();
			     if(nextPath == null)
			     {
			    	 continue;
			     }
			     else if(nextPath.startsWith(xPath + "/textFormat/simpleDelimited"))
			     {
			    	 Object o1 = map.get(nextPath);
			 	     if(o1 != null) 
			 	     {
			 	    	 return SIMPLE_TEXT_XPATH;
			 	     }
			     }
			     else if (nextPath.startsWith(xPath + "/textFormat/complex"))
			     {
			    	 Object o1 = map.get(nextPath);
			 	     if(o1 != null) 
			 	     {
			 	    	 return COMPLEX_TEXT_XPATH;
			 	     }
			     }
			     else if (nextPath.startsWith(xPath + "/externallyDefinedFormat"))
			     {
			    	 Object o1 = map.get(nextPath);
			 	     if(o1 != null) 
			 	     {
			 	    	 return PROPRIETARY_XPATH;
			 	     }
			     }
			     
			 }
	    	 return "";
	    }
	    else
	    {
	    	return "";
	    }
	 }

}
