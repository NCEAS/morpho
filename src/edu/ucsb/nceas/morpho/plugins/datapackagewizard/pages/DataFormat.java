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
 *     '$Date: 2004-01-07 02:02:17 $'
 * '$Revision: 1.26 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class DataFormat extends AbstractWizardPage{

  private final String pageID     = DataPackageWizardInterface.DATA_FORMAT;
  private String nextPageID       = DataPackageWizardInterface.ENTITY;
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
  private final String TEXT_BASE_XPATH
                  = "/eml:eml/dataset/dataTable/physical/dataFormat/textFormat/";
  private final String SIMPLE_TEXT_XPATH  = TEXT_BASE_XPATH+"simpleDelimited/fieldDelimiter";
  private final String COMPLEX_TEXT_XPATH = TEXT_BASE_XPATH+"complex/";
  private final String PROPRIETARY_XPATH
          = "/eml:eml/dataset/dataTable/physical/dataFormat/externallyDefinedFormat/formatName";
  private final String RASTER_XPATH
          = "/eml:eml/dataset/dataTable/physical/dataFormat/binaryRasterFormat";

  private JPanel simpleTextpanel;
  private JPanel complexTextPanel;
  private JPanel proprietaryPanel;
  private JPanel rasterPanel;
  private JPanel currentPanel;
  private JLabel radioButtonGrpLabel;
  private final String[] buttonsText = new String[] {
      "Simple delimited text format (uses one of a series of delimiters to indicate the ends of fields)",
      "Complex text format (delimited fields, fixed width fields, and mixtures of the two)",
      "Non-text or proprietary formatted object that is externally defined (e.g. 'Microsoft Excel')"
//      ,"Binary raster image file"
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

  private String[] pickListVals = new String[] {
    "Fixed-Width",
    "Delimited"
  };

  private String delim_tab       = null;
  private String delim_comma     = null;
  private String delim_space     = null;
  private String delim_semicolon = null;
  private boolean delim_other    = false;
  private CustomList list;

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



    JPanel radioPanel = WidgetFactory.makeRadioPanel(buttonsText, -1, listener);

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

  private JLabel listLabel;

  private JPanel getComplexTextPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);

    WidgetFactory.addTitledBorder(panel, buttonsText[1]);

    ////
    panel.add(WidgetFactory.makeHTMLLabel("Data Attributes are arranged in:", 1));

    JPanel orientationPanel = WidgetFactory.makePanel(2);

    JLabel spacerLabel = WidgetFactory.makeLabel(EMPTY_STRING, false);

    orientationPanel.add(spacerLabel);

    orientationPanel.add(getOrientationRadioPanel());

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

    final Map mimeMap = WizardSettings.getSupportedMIMETypesForEntity(
                                              WizardSettings.ENTITY_DATATABLE);

    // proprietaryButtonsText array is one elem larger than mime map, because
    // we need to add an entry for "other"...
    final String [] proprietaryButtonsText = new String[mimeMap.size() + 1];

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

    JPanel radioPanel = WidgetFactory.makeRadioPanel( proprietaryButtonsText,
                                                      INITIAL_SELECTION,
                                                      listener);
    setProprietaryText(proprietaryButtonsText[INITIAL_SELECTION], mimeMap);

    JPanel radioJustifyPanel = new JPanel();
    radioJustifyPanel.setLayout(new BoxLayout(radioJustifyPanel,
                                              BoxLayout.X_AXIS));
    radioJustifyPanel.add(WidgetFactory.makeLabel(EMPTY_STRING, false));
    radioJustifyPanel.add(radioPanel);
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


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  private JPanel getEmptyPanel() {

    return WidgetFactory.makeVerticalPanel(7);
  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

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
        if (otherTxt==null || otherTxt.equals(EMPTY_STRING)) {

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
          if (proprietaryText.equals(EMPTY_STRING)) {

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

    int predicateIndex      = 1;
    StringBuffer buff       = new StringBuffer();
    List rowLists           = list.getListOfRowLists();
    String fixedDelimStr    = null;

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      Object nextRowObj = it.next();

      if (nextRowObj==null) continue;

      List nextRow = (List)nextRowObj;

      if (nextRow.size() < 1) continue;

      if (nextRow.get(0).equals(pickListVals[0])) {

        fixedDelimStr = "textFixed/fieldWidth";

      } else if (nextRow.get(0).equals(pickListVals[1])) {

        fixedDelimStr = "textDelimited/fieldDelimiter";

      }

      String nextVal = (String)(nextRow.get(1));

      // substitute hex values for tabs and spaces:
      if (nextVal.equals("\\t")) nextVal = WizardSettings.HEX_VALUE_TAB;
      if (nextVal.equals(" ")) nextVal  = WizardSettings.HEX_VALUE_SPACE;

      buff.delete(0,buff.length());
      buff.append(COMPLEX_TEXT_XPATH);
      buff.append(fixedDelimStr);
      buff.append("[");
      buff.append(predicateIndex++);
      buff.append("]");
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

      returnMap.put(TEXT_BASE_XPATH+"attributeOrientation", orientationComplex);

      returnMap.putAll(getCmplxDelimListAsNVP());

    } else if (formatXPath==PROPRIETARY_XPATH)  {

      returnMap.put(PROPRIETARY_XPATH, proprietaryText);

//    } else if (formatXPath==RASTER_XPATH) {

    }
    return returnMap;
  }


  private final JTextField otherDelimTextFieldSimple
                                          = WidgetFactory.makeOneLineTextField();

  private JPanel getDelimiterCheckBoxPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);

    JPanel cbPanel = WidgetFactory.makeCheckBoxPanel(delimiterCheckBoxesText, -1,
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
    panel.add(cbPanel);

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

    if (formatXPath==SIMPLE_TEXT_XPATH)  orientationSimple  = orient;
    if (formatXPath==COMPLEX_TEXT_XPATH) orientationComplex = orient;
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

  public void setPageData(OrderedMap data) { }
}
