/**  '$RCSfile: NominalOrdinalPanel.java,v $'
*    Purpose: A class that handles xml messages passed by the
*       package wizard
*  Copyright: 2000 Regents of the University of California and the
*       National Center for Ecological Analysis and Synthesis
*    Authors: Chad Berkley
*    Release: @release@
*
*   '$Author: cjones $'
*     '$Date: 2004-06-24 00:08:37 $'
* '$Revision: 1.33 $'
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

import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

class NominalOrdinalPanel extends JPanel implements WizardPageSubPanelAPI {

  private JPanel currentSubPanel;
  private JPanel textSubPanel;
  private JPanel enumSubPanel;
  // the panel that contains the code definition customlist
  private JPanel enumPanel;
  private String enforcedField = "";

  private JLabel     textDefinitionLabel;
  private JTextField textDefinitionField;
  private JTextField textSourceField;
  private CustomList textPatternsList;

  private JLabel     chooseLabel;
  private JLabel     enumDefinitionLabel;
  private CustomList enumDefinitionList;
  private CustomList importedDefinitionList;

  private JLabel      codeLocationLabel;
  private JComboBox    codeLocationPickList;
  private final String[] codeLocationPicklistVals
  = { "Codes are defined here",
  "Codes are imported from another table"     };

  private JPanel tablePanel;
  private JLabel tableNameLabel;
  private JTextField tableNameTextField;
  private JButton tableNameButton;
  private LocateAction locateAction;
  private CodeDefnPanel codeImportPanel = null;

  private JCheckBox enumDefinitionFreeTextCheckBox;

  private final String[] textEnumPicklistVals
  = { "Enumerated values (belong to predefined list)",
  "Text values (free-form or matching a pattern)"     };

  private static final String TO_BE_IMPORTED = "Imported later";
  private static final String SELECT_TABLE = "--select table--";

  private static final short CODES_DEFINED_HERE = 10;
  private static final short CODES_IMPORTED = 20;
  private short codeLocationValue = CODES_DEFINED_HERE;

  private final String[] nomOrdDisplayNames = { "nominal", "ordinal" };

  double[] definedEnumColumnWidthPercentages = new double[] { 25.0, 75.0};
  double[] importedEnumColumnWidthPercentages = new double[] { 25.0, 75.0};

  private final int ENUMERATED_DOMAIN = 10;
  private final int TEXT_DOMAIN       = 20;

  private final String EMPTY_STRING = "";

  private AbstractUIPage wizardPage;

  private JComboBox domainPickList;


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  /**
  * Constructor
  *
  * @param page the parent wizard page
  */
  public NominalOrdinalPanel(AbstractUIPage page) {

    super();
    this.wizardPage = page;
    init();
  }


  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    int width = WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.width;
    int height = AttributePage.BORDERED_PANEL_TOT_ROWS
    * WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.height;

    Dimension dims = new Dimension(width, height);

    //this.setPreferredSize(dims);
    //this.setMaximumSize(dims);

    final String TEXT_HELP
    = WizardSettings.HTML_NO_TABLE_OPENING
    +"Describe a free text domain for the attribute."
    +WizardSettings.HTML_NO_TABLE_CLOSING;

    final String ENUM_HELP
    = WizardSettings.HTML_NO_TABLE_OPENING
    +"Describe any codes that are used as values of "
    +"the attribute."+WizardSettings.HTML_NO_TABLE_CLOSING;

    final JLabel helpTextLabel = getLabel(ENUM_HELP);

    ItemListener listener = new ItemListener() {

      public void itemStateChanged(ItemEvent e) {
      
        String value = e.getItem().toString();
        Log.debug(45, "PickList state changed: " +value);
      
        if (value.equals(textEnumPicklistVals[0])) { //enumerated
        
          Log.debug(45,
          nomOrdDisplayNames+"/enumeratedDomain selected");
          setTextEnumSubPanel(ENUMERATED_DOMAIN);
          helpTextLabel.setText(ENUM_HELP);
        
        } else if (value.equals(textEnumPicklistVals[1])) { //text
        
          Log.debug(45,
          nomOrdDisplayNames+"/textDomain selected");
          setTextEnumSubPanel(TEXT_DOMAIN);
          helpTextLabel.setText(TEXT_HELP);
        
        }
      
      }
    };


    domainPickList = WidgetFactory.makePickList(textEnumPicklistVals, false, 0, listener);

    // using preferredSize just to ensure that the picklist displays correctly with the
    // arrow button shown properly, when the size of the attribute page is reduced(as in
    // TextImportWizard). Without this, the picklist wouldnt appear fully. This doesnt
    // affect the way it is displayed on a normal size attribute page.
    domainPickList.setPreferredSize(new Dimension(200,10));


    JPanel pickListPanel = WidgetFactory.makePanel();
    chooseLabel = WidgetFactory.makeLabel("Choose:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    pickListPanel.add(chooseLabel);
    pickListPanel.add(domainPickList);


    JPanel measScalePanel = new JPanel();
    measScalePanel.setLayout(new GridLayout(1,2,3,0));
    measScalePanel.add(pickListPanel);

    measScalePanel.add(helpTextLabel);

    this.add(measScalePanel);

    this.add(Box.createGlue());

    textSubPanel    = getTextSubPanel();
    enumSubPanel    = getEnumSubPanel();
    currentSubPanel = enumSubPanel;

    this.add(currentSubPanel);

  }


  private void setTextEnumSubPanel(int newDomain) {

    this.remove(currentSubPanel);

    if (newDomain==ENUMERATED_DOMAIN) currentSubPanel = enumSubPanel;
    else currentSubPanel = textSubPanel;

    this.add(currentSubPanel);
    textDefinitionField.requestFocus();
    ((AttributePage)wizardPage).refreshUI();
  }


  // * * * *


  private JPanel getTextSubPanel() {

    JPanel panel
    = WidgetFactory.makeVerticalPanel(AttributePage.DOMAIN_NUM_ROWS);

    panel.add(WidgetFactory.makeHalfSpacer());

    ///////////////////////////

    JPanel topHorizPanel = new JPanel();
    topHorizPanel.setLayout(new GridLayout(1,2));

    JPanel defFieldPanel = WidgetFactory.makePanel();
    textDefinitionLabel = WidgetFactory.makeLabel("Definition:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    defFieldPanel.add(textDefinitionLabel);
    textDefinitionField = WidgetFactory.makeOneLineTextField();
    defFieldPanel.add(textDefinitionField);

    topHorizPanel.add(defFieldPanel);

    topHorizPanel.add(getLabel(
    WizardSettings.HTML_NO_TABLE_OPENING
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    +"e.g: <i>U.S. telephone numbers in the format (999) 888-7777</i>"
    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_CLOSING));

    panel.add(topHorizPanel);

    panel.add(WidgetFactory.makeHalfSpacer());

    ///////////////////////////

    JPanel middleHorizPanel = new JPanel();
    middleHorizPanel.setLayout(new GridLayout(1,2));

    JPanel srcFieldPanel = WidgetFactory.makePanel();
    srcFieldPanel.add(WidgetFactory.makeLabel("Source:", false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS));
    textSourceField = WidgetFactory.makeOneLineTextField();
    srcFieldPanel.add(textSourceField);

    middleHorizPanel.add(srcFieldPanel);

    middleHorizPanel.add(getLabel(
    WizardSettings.HTML_NO_TABLE_OPENING
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    +"e.g: <i>FIPS standard for postal abbreviations for U.S. states</i>"
    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_CLOSING));

    panel.add(middleHorizPanel);

    panel.add(WidgetFactory.makeHalfSpacer());

    ///////////////////////////

    JPanel bottomHorizPanel = new JPanel();
    bottomHorizPanel.setLayout(new GridLayout(1,2));

    Object[] colTemplates = new Object[] { new JTextField() };

    JPanel patternPanel = WidgetFactory.makePanel();
    patternPanel.add(WidgetFactory.makeLabel("Pattern(s):", false, WizardSettings.WIZARD_CONTENT_LABEL_DIMS));
    String[] colNames = new String[] { "Pattern(s) (optional):" };

    textPatternsList
    = WidgetFactory.makeList( colNames, colTemplates, 2,
    true, false, false, true, false, false);
    textPatternsList.setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    patternPanel.add(textPatternsList);

    bottomHorizPanel.add(patternPanel);

    bottomHorizPanel.add(getLabel(
    WizardSettings.HTML_NO_TABLE_OPENING
    +"Patterns "
    +"are interpreted as regular expressions constraining allowable "
    +"character sequences."
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    +"  e.g: <i>'[0-9]{3}-[0-9]{3}-[0-9]{4}' allows "
    +"only numeric digits in the pattern of US phone numbers"
    +"</i>"+WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_CLOSING));

    panel.add(bottomHorizPanel);

    return panel;
  }

  // * * * *

  private JPanel getEnumSubPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(AttributePage.DOMAIN_NUM_ROWS);

    panel.add(WidgetFactory.makeHalfSpacer());

    ///////////////////////////

    JPanel locationPanel = WidgetFactory.makePanel();
    codeLocationLabel = WidgetFactory.makeLabel("Location:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    locationPanel.add(codeLocationLabel);

    ItemListener listener = new ItemListener() {

      public void itemStateChanged(ItemEvent e) {

        String value = e.getItem().toString();
        Log.debug(45, "CodeLocationPickList state changed: " +value);
        
        if (value.equals(codeLocationPicklistVals[0])) { //user-defined
        
          if(codeLocationValue == CODES_IMPORTED) {
            tablePanel.setVisible(false);
            enumPanel.remove(importedDefinitionList);
            enumPanel.add(enumDefinitionList);
            enumPanel.invalidate();
          }
          codeLocationValue = CODES_DEFINED_HERE;
        
        } else if (value.equals(codeLocationPicklistVals[1])) { //imported
        
          if(codeLocationValue == CODES_DEFINED_HERE) {
            tablePanel.setVisible(true);
            enumPanel.remove(enumDefinitionList);
            enumPanel.add(importedDefinitionList);
            enumPanel.invalidate();
          }
          codeLocationValue = CODES_IMPORTED;
        }
      }
    };
    codeLocationPickList = WidgetFactory.makePickList(codeLocationPicklistVals, false, 0, listener);
    codeLocationPickList.setPreferredSize(new Dimension(200,10));
    locationPanel.add(codeLocationPickList);

    tablePanel = WidgetFactory.makePanel();
    tableNameLabel = getLabel("Table Name:  ");
    tableNameLabel.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    tablePanel.add(tableNameLabel);
    tableNameTextField = WidgetFactory.makeOneLineTextField(SELECT_TABLE);
    tableNameTextField.setEditable(false);
    tablePanel.add(tableNameTextField);

    locateAction = new LocateAction(this.wizardPage);
    tableNameButton = WidgetFactory.makeJButton("locate", locateAction);
    tableNameButton.setMinimumSize(new Dimension(55,17));
    tableNameButton.setMaximumSize(new Dimension(55,17));
    tableNameButton.setMargin(new Insets(0,2,1,2));
    JPanel tableNameButtonPanel = new JPanel(new BorderLayout());
    tableNameButtonPanel.add(tableNameButton, BorderLayout.CENTER);
    tableNameButtonPanel.setBorder(new EmptyBorder(0,2*WizardSettings.PADDING,
    0, WizardSettings.PADDING));

    // if no data tables are present, automatically set the import choice to be 'later'
    if(!isAnyDataTablePresent()) {
      tableNameTextField.setText(this.TO_BE_IMPORTED);
      tableNameButton.setEnabled(false);
    }

    tablePanel.add(tableNameButtonPanel);

    tablePanel.setVisible(false);

    JPanel importPanel = new JPanel();
    importPanel.setLayout(new GridLayout(1,2,3,0));
    importPanel.add(locationPanel);
    importPanel.add(tablePanel);

    panel.add(importPanel);
    panel.add(WidgetFactory.makeHalfSpacer());

    //////////////////////////////


    Object[] colTemplates
    = new Object[] { new JTextField(), new JTextField()};

    String[] colNames
    = new String[] { "Code", "Definition" };

    enumPanel = WidgetFactory.makePanel();
    enumDefinitionLabel = WidgetFactory.makeLabel("Definitions:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    enumPanel.add(enumDefinitionLabel);

    enumDefinitionList
    = WidgetFactory.makeList( colNames, colTemplates, 2,
    true, false, false, true, false, false);

    importedDefinitionList
    = WidgetFactory.makeList( colNames, colTemplates, 2,
    false, false, false, false, false, false);
    importedDefinitionList.setBorder(new EmptyBorder(0,0,1,WizardSettings.PADDING));

    enumDefinitionList.setColumnWidthPercentages(definedEnumColumnWidthPercentages);
    importedDefinitionList.setColumnWidthPercentages(importedEnumColumnWidthPercentages);

    enumDefinitionList.setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    enumPanel.add(enumDefinitionList);

    panel.add(enumPanel);

    ///////////////////////////


    JPanel helpPanel = new JPanel();
    helpPanel.setLayout(new GridLayout(1,7));

    helpPanel.add(this.getLabel(
    WizardSettings.HTML_NO_TABLE_OPENING
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    +"Example:"+WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_CLOSING));

    helpPanel.add(this.getLabel(
    WizardSettings.HTML_NO_TABLE_OPENING
    +WizardSettings.HTML_EXAMPLE_FONT_OPENING
    //+"Example: &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CA&nbsp;&nbsp;"
    +"CA"
    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +WizardSettings.HTML_NO_TABLE_CLOSING));

    helpPanel.add(this.getLabel(
    WizardSettings.HTML_NO_TABLE_OPENING
    +"<left>"+WizardSettings.HTML_EXAMPLE_FONT_OPENING
    +"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
    +"California"
    +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
    +"</left>"+WizardSettings.HTML_NO_TABLE_CLOSING));
    helpPanel.add(this.getLabel(""));
    helpPanel.add(this.getLabel(""));
    helpPanel.add(this.getLabel(""));
    helpPanel.add(this.getLabel(""));
    //panel.add(helpPanel);

    /////////////////////////////

    panel.add(WidgetFactory.makeHalfSpacer());

    enumDefinitionFreeTextCheckBox = WidgetFactory.makeCheckBox(
    "Attribute contains free-text in addition to those values listed above",
    false);

    JPanel cbPanel = WidgetFactory.makePanel();
    cbPanel.add(enumDefinitionFreeTextCheckBox);
    cbPanel.add(Box.createGlue());
    panel.add(cbPanel);

    ////
    return panel;

  }


  boolean isAnyDataTablePresent() {

    AbstractDataPackage adp = getADP();
    if(adp == null)
      return false;
    return (adp.getEntityCount() > 0);
  }


  private AbstractDataPackage getADP() {

    AbstractDataPackage adp = null;
    DataViewContainerPanel resultPane = null;

    MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null) {
      resultPane = morphoFrame.getDataViewContainerPanel();
    }//if
    // make sure resulPanel is not null
    if ( resultPane != null) {
      adp = resultPane.getAbstractDataPackage();
    }
    return adp;
  }

  private JLabel getLabel(String text) {

    if (text==null) text=EMPTY_STRING;
    JLabel label = new JLabel(text);

    label.setAlignmentX(1.0f);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    label.setBorder(BorderFactory.createMatteBorder(1,10,1,3, (Color)null));

    return label;
  }


  /**
  *  The action to be executed when the panel is displayed. May be empty
  */
  public void onLoadAction() {

    WidgetFactory.unhiliteComponent(enumDefinitionLabel);
    WidgetFactory.unhiliteComponent(textDefinitionLabel);
  }


  /**
  *  checks that the user has filled in required fields - if not, highlights
  *  labels to draw attention to them
  *
  *  @return   boolean true if user data validated OK. false if intervention
  *      required
  */
  public boolean validateUserInput() {

    if (currentSubPanel==enumSubPanel) {  //ENUMERATED

      WidgetFactory.unhiliteComponent(enumDefinitionLabel);
      WidgetFactory.unhiliteComponent(tableNameLabel);

      String loc = (String)codeLocationPickList.getSelectedItem();
      if ( loc.equals(codeLocationPicklistVals[1]) ) {
  if(tableNameTextField.getText().equals(SELECT_TABLE)) {
    WidgetFactory.hiliteComponent(tableNameLabel);
    return false;
  }
      } else {
  if (!isEnumListDataValid()) {
    WidgetFactory.hiliteComponent(enumDefinitionLabel);
    return false;
  }
      }
    } else {    ////////////////////////////TEXT

      WidgetFactory.unhiliteComponent(textDefinitionLabel);
      if (textDefinitionField.getText().trim().equals(EMPTY_STRING)) {

  WidgetFactory.hiliteComponent(textDefinitionLabel);
  textDefinitionField.requestFocus();

  return false;
      }

      // CHECK FOR AND ELIMINATE EMPTY ROWS...
      textPatternsList.deleteEmptyRows( CustomList.OR,
      new short[] {
      CustomList.EMPTY_STRING_TRIM  } );
    }


    return true;
  }



  /**
  *  gets the Map object that contains all the key/value paired
  *
  *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
  *      appended when making name/value pairs.  For example, in the
  *      following xpath:
  *
  *      /eml:eml/dataset/dataTable/attributeList/attribute[2]
  *      /measurementScale/nominal/nonNumericDomain/textDomain/definition
  *
  *      the root would be:
  *
  *        /eml:eml/dataset/dataTable/attributeList
  *        /attribute[2]/measurementScale
  *
  *      NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
  *      SQUARE BRACKETS []
  *
  *  @return   data the Map object that contains all the
  *      key/value paired settings for this particular wizard page
  */
  private OrderedMap   returnMap  = new OrderedMap();
  private StringBuffer nomOrdBuff = new StringBuffer();
  ////////////////////////////////////////////////////////
  public OrderedMap getPanelData(String xPathRoot) {

    returnMap.clear();
    
    // handle <enumeratedDomain enforced="" >
    String enforced = enforcedField.trim();
    if (enforced !=null && !enforced.equals("")) {
      returnMap.put( xPathRoot + 
      "/nonNumericDomain/enumeratedDomain[1]/@enforced", enforced);
    }

    nomOrdBuff.delete(0, nomOrdBuff.length());
    nomOrdBuff.append(xPathRoot);
    nomOrdBuff.append("/nonNumericDomain/");
    xPathRoot = nomOrdBuff.toString();

    if (currentSubPanel==enumSubPanel) {  //ENUMERATED

      if(codeLocationValue == CODES_DEFINED_HERE){
        getEnumListData(xPathRoot + "enumeratedDomain[1]", returnMap);
      } else {
        if(codeImportPanel == null)
        codeImportPanel = new CodeDefnPanel();
        OrderedMap importMap = codeImportPanel.getPanelData(xPathRoot +
        "enumeratedDomain[1]/entityCodeList");
        returnMap.putAll(importMap);
      }

      if (enumDefinitionFreeTextCheckBox.isSelected()) {

        returnMap.put(  xPathRoot + "textDomain[1]/definition",
        "Free text (unrestricted)");
        returnMap.put(xPathRoot + "textDomain[1]/pattern[1]", ".*");
      }

    } else {            //TEXT

      returnMap.put(  xPathRoot + "textDomain[1]/definition",
      textDefinitionField.getText().trim());

      int index = 1;
      List rowLists = textPatternsList.getListOfRowLists();
      String nextStr = null;

      for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

        Object nextRowObj = it.next();
        if (nextRowObj==null) continue;
        
        List nextRow = (List)nextRowObj;
        if (nextRow.size() < 1) continue;
        nextStr = (String) nextRow.get(0);
        nomOrdBuff.delete(0, nomOrdBuff.length());
        nomOrdBuff.append(xPathRoot);
        nomOrdBuff.append("textDomain[1]/pattern[");
        nomOrdBuff.append(index++);
        nomOrdBuff.append("]");
        
        returnMap.put(nomOrdBuff.toString(), nextStr);
      }

      String source = textSourceField.getText().trim();
      if (!source.equals(EMPTY_STRING)) {
        returnMap.put(  xPathRoot + "textDomain[1]/source", source);
      }
    }
    return returnMap;
  }



  // xpathRoot is up to 'enumeratedDomain' (NOT including the slash after)
  private void getEnumListData(String xpathRoot, OrderedMap resultsMap) {

    // Check This - Can we put IGNORE for second col.
    enumDefinitionList.deleteEmptyRows( CustomList.OR,
    new short[] {
      CustomList.EMPTY_STRING_TRIM,
    CustomList.IGNORE  } );


    int index=1;
    StringBuffer buff = new StringBuffer();
    List rowLists = enumDefinitionList.getListOfRowLists();
    Object srcObj = null;
    String srcStr = null;

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;

      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;

      buff.delete(0,buff.length());
      buff.append(xpathRoot);
      buff.append("/codeDefinition[");
      buff.append(index++);
      buff.append("]/");
      resultsMap.put( buff.toString() + "code",
      ((String)(nextRow.get(0))).trim());

      srcObj = nextRow.get(1);
      if(srcObj == null) continue;
      resultsMap.put( buff.toString() + "definition",
      ((String)srcObj).trim());

    }
  }

  public boolean isImportNeeded() {

    if(codeLocationValue == CODES_DEFINED_HERE)
      return false;
    if(codeImportPanel == null) return true;
    if(codeImportPanel.getTableName() == null) return true;
    return false;
  }

  //
  //  first eliminates rows that have both first and second columns empty, then
  //  check sremaining rows and returns false if either first or second column
  //  is empty
  //
  private boolean isEnumListDataValid() {

    // CHECK FOR AND ELIMINATE EMPTY ROWS. NOTE THAT ROWS WITH JUST ONE EMPTY
    // FIELD WON'T BE DELETED YET - saves user data being deleted by accident
    // if not yet complete
    enumDefinitionList.deleteEmptyRows( CustomList.AND,
    new short[] {
      CustomList.EMPTY_STRING_TRIM,
      CustomList.EMPTY_STRING_TRIM } );

    List rowLists = enumDefinitionList.getListOfRowLists();

    if (rowLists==null || rowLists.size()<1) return false;

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;

      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;

      if (nextRow.get(0)==null || nextRow.get(1)==null
  || ((String)(nextRow.get(0))).trim().equals(EMPTY_STRING)
      || ((String)(nextRow.get(1))).trim().equals(EMPTY_STRING)) return false;

    }
    return true;
  }


  /**
  *  sets the Data in the NominalOrdinal Panel. This is called by the setPageData() function
  *  of AttributePage.
  *
  *  @param  xPathRoot - this is the relative xPath of the current attribute
  *
  *  @param  map - Data is passed as OrderedMap of xPath-value pairs. xPaths in this map
  *        are absolute xPath and not the relative xPaths
  *
  **/

  public void setPanelData(String xPathRoot, OrderedMap map) {
    
    Log.debug(50, "datapackagewizard: NominalOrdinalPanel.setPanelData() " +
      "called with xPathRoot = " + xPathRoot);

    // future enhancements to the NominalOrdinalPanel dialog should map
    // the following xml attribute to an appropriate widget, and should also
    // handle multiple enumeratedDomains
    String enforced = (String)map.get(xPathRoot + "/enumeratedDomain[1]/@enforced");
    if ( enforced != null && !enforced.equals("") ) {
      enforcedField = enforced.toString();
      map.remove(xPathRoot + "enumeratedDomain[1]/@enforced");
    } else {
      enforced = (String)map.get(xPathRoot + "/enumeratedDomain/@enforced");
      if ( enforced != null && !enforced.equals("") ) {
      enforcedField = enforced.toString();
      map.remove(xPathRoot + "/enumeratedDomain/@enforced");
      }
    }

    // check for code list lookups (enumeratedDomain/entityCodeList subtree)
    boolean b1 = map.containsKey(xPathRoot + "/enumeratedDomain[1]/entityCodeList/entityReference");
    if(!b1)
      b1 = map.containsKey(xPathRoot + "/enumeratedDomain/entityCodeList/entityReference");

    // check for free text definitions (textDomain subtree)
    String defn = (String)map.get(xPathRoot + "/textDomain[1]/definition");

    if(defn == null) {
      defn = (String)map.get(xPathRoot + "/textDomain/definition");
      if(defn != null) map.remove(xPathRoot + "/textDomain/definition");
    } else {
      map.remove(xPathRoot + "/textDomain[1]/definition");
    }
    // set the checkbox if there are no pattern constraints on the text
    if(defn!=null) {

      if (defn.equals("Free text (unrestricted)")) {
        enumDefinitionFreeTextCheckBox.setSelected(true);
      } else {
        enumDefinitionFreeTextCheckBox.setSelected(false);
      }
    }

    // check for code imports

    if(b1) { // codes are imported from another table

      locateAction.setPageData(xPathRoot +  "/enumeratedDomain/entityCodeList", map);
      tablePanel.setVisible(true);
      if(codeLocationValue == CODES_DEFINED_HERE) {
        enumPanel.remove(enumDefinitionList);
        enumPanel.add(importedDefinitionList);
        enumPanel.invalidate();
      }
      codeLocationPickList.setSelectedItem(codeLocationPicklistVals[1]);
      codeLocationValue = CODES_IMPORTED;
      domainPickList.setSelectedItem(this.textEnumPicklistVals[0]);
      return;

    } else { // codes are defined here

      boolean found = setEnumListData(xPathRoot + "/enumeratedDomain[1]", map);
      if(!found)
        found = setEnumListData(xPathRoot + "/enumeratedDomain", map);
      if(found) {
        tablePanel.setVisible(false);
        if(codeLocationValue == CODES_IMPORTED) {
          enumPanel.remove(importedDefinitionList);
          enumPanel.add(enumDefinitionList);
          enumPanel.invalidate();
        }
        codeLocationValue = CODES_DEFINED_HERE;
        codeLocationPickList.setSelectedItem(codeLocationPicklistVals[0]);
        domainPickList.setSelectedItem(this.textEnumPicklistVals[0]);
        
        return;
      }
    }

    // set the textDomain/definition field value
    if (defn != null) {
      textDefinitionField.setText(defn);
      domainPickList.setSelectedItem(this.textEnumPicklistVals[1]);
    }
    setTextListData(xPathRoot + "/textDomain[1]", map);
    setTextListData(xPathRoot + "/textDomain", map);

    // set the textDomain/source field value
    String source = (String)map.get(xPathRoot + "/textDomain[1]/source");
    if (source == null) {
      source = (String)map.get(xPathRoot + "/textDomain/source");

    if(source != null) map.remove(xPathRoot + "/textDomain/source");
    } else {
      map.remove(xPathRoot + "/textDomain[1]/source");
    }

    if (source != null) {
      textSourceField.setText(source);
    }
    return;
  }




  /**
   * This function is used to set the data in the enumeration customlist. This
   * is called from the setPanelData() function
   *
   * @param xPathRoot - this is the relative xPath of the current attribute
   * @param map - Data is passed as OrderedMap of xPath-value pairs. xPaths in
   *   this map are absolute xPath and not the relative xPaths
   * @return boolean
   */
  private boolean setEnumListData(String xPathRoot, OrderedMap map) {
    int index = 1;
    boolean codePresent = false;
    while(true) {
      List row = new ArrayList();
      String code = (String)map.get(xPathRoot+"/codeDefinition[" +index+ "]/code");
      if (code!=null) map.remove(xPathRoot+"/codeDefinition[" +index+ "]/code");

      if(index == 1 && code == null) {
        code = (String)map.get(xPathRoot+"/codeDefinition/code");
        if (code!=null) map.remove(xPathRoot+"/codeDefinition/code");
      }

      if(code == null) break;

      codePresent = true;
      row.add(code);
      String defn = (String)map.get(xPathRoot+"/codeDefinition[" +index+ "]/definition");
      if (defn!=null) map.remove(xPathRoot+"/codeDefinition[" +index+ "]/definition");

      if(index == 1 && defn == null) {
        defn = (String)map.get(xPathRoot + "/codeDefinition/definition");
        if (defn!=null) map.remove(xPathRoot+"/codeDefinition/definition");
      }
      row.add(defn);
      enumDefinitionList.addRow(row);
      index++;
    }
    return codePresent;
  }

  private void setTextListData(String xPathRoot, OrderedMap map) {
    int index = 1;
    while(true) {
      List row = new ArrayList();
      String pattern = (String)map.get(xPathRoot+"/pattern[" +index+ "]");

      if (pattern!=null) map.remove(xPathRoot+"/pattern[" +index+ "]");

      if(index == 1 && pattern == null) {
        pattern = (String)map.get(xPathRoot+"/pattern");
        if (pattern!=null) map.remove(xPathRoot+"/pattern");
      }

      if(pattern == null) break;

      row.add(pattern);
      textPatternsList.addRow(row);
      index++;
    }
    return;
  }


  /** This function is to retrieve the Pattern definitions from the CustomList
  *
  *   @return - the List of Rows of Pattern. Each row is a 1 element list
  *    consisting of the Pattern
  */

  public List getTextList() {
    return this.textPatternsList.getListOfRowLists();
  }

  /* Action class for the "Locate Table" button in the import Panel of the
  Enumerated domain.
  */

  class LocateAction extends AbstractAction {

    private AbstractUIPage attributePage;
    private Dimension DIALOG_SIZE = new Dimension(500,450);
    private JDialog importDialog = null;




    LocateAction(AbstractUIPage page) {
      this.attributePage = page;
    }

    public void actionPerformed(ActionEvent ae) {

      if(codeImportPanel == null)
  codeImportPanel = new CodeDefnPanel();
      if(importDialog == null) {
  ActionListener okAction = new ActionListener() {
    public void actionPerformed(ActionEvent ae) {
      okAction();
    }
  };
  ActionListener cancelAction = new ActionListener() {
    public void actionPerformed(ActionEvent ae) {
      cancelAction();
    }
  };
  Point loc = attributePage.getLocationOnScreen();
  int xc = (int)(loc.getX() + attributePage.getWidth()/2 - DIALOG_SIZE.width/2);
  int yc = (int)(loc.getY() + attributePage.getHeight()/2 - DIALOG_SIZE.height/2);
  importDialog = WidgetFactory.makeContainerDialogNoParent(codeImportPanel, okAction, cancelAction);
  importDialog.setBounds(xc, yc, DIALOG_SIZE.width, DIALOG_SIZE.height);
      }

      importDialog.setVisible(true);

    }

    private void okAction() {

      if(codeImportPanel.validateUserInput())
        importDialog.setVisible(false);
      else
      return;
      String tableName = codeImportPanel.getTableName();
      if(tableName == null) {
        tableNameTextField.setText(TO_BE_IMPORTED);
        importedDefinitionList.removeAllRows();
      } else {
       tableNameTextField.setText(tableName);
       fillCustomList();
      }
      return;
    }

    private void cancelAction() {
      importDialog.setVisible(false);
    }




    // to fill the custom list in the attribute page with the imported values.
    // This custom list is non-editable

    private void fillCustomList() {

      List importedCodes = codeImportPanel.getColumnData();
      if(importedCodes == null)
      return;
      Iterator it = importedCodes.iterator();

      importedDefinitionList.removeAllRows();
      while(it.hasNext()) {

      List row = (List)it.next();
      importedDefinitionList.addRow(row);
      }

      importedDefinitionList.fireEditingStopped();
      importedDefinitionList.setEditable(false);
      importedDefinitionList.scrollToRow(0);
      return;

    } // end of function fillCustomList


    /**
     * Function to set the page data from the ordered map. It creates a
     * codeImport page if necessary and sets the data in that page
     *
     * @param xPath - this is the relative xPath of the current attribute
     * @param map - Data is passed as OrderedMap of xPath-value pairs. xPaths
     *   in this map are absolute xPath and not the relative xPaths
     */
    public void setPageData(String xPath, OrderedMap map) {

      if(codeImportPanel == null) codeImportPanel = new CodeDefnPanel();
      codeImportPanel.setPanelData(xPath, map);
      String name = codeImportPanel.getTableName();
      if( name == null) { // to be imported later
        tableNameTextField.setText(TO_BE_IMPORTED);
      } else {
        tableNameTextField.setText(name);
        fillCustomList();
      }
      return;
    }

  }
}
