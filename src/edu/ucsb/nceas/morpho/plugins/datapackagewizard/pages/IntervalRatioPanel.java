/**
 *  '$RCSfile: IntervalRatioPanel.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-18 01:26:21 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DialogSubPanelAPI;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.utilities.OrderedMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Component;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;



class IntervalRatioPanel extends JPanel implements DialogSubPanelAPI {

  private JLabel     unitsPickListLabel;
  private JLabel     precisionLabel;
  private JLabel     numberTypeLabel;
  
  private JTextField precisionField;
  private JComboBox  numberTypePickList;
  
  private AttributeDialog attributeDialog;
  
  private String[] numberTypesArray = new String[] { "natural", "whole",
                                                     "integer", "real" };
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  /**
   *  Constructor
   *
   *  @param attributeDialog the parent dialog
   *
   *  @param nom_ord_mode can be AttributeDialog.MEASUREMENTSCALE_NOMINAL 
   *                  or AttributeDialog.MEASUREMENTSCALE_ORDINAL
   */
  public IntervalRatioPanel(AttributeDialog attributeDialog) {
  
    super();
    this.attributeDialog = attributeDialog;
    init();
  } 
  
  
  private void init() {
  
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  
    int width = WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.width;
    int height = AttributeDialog.BORDERED_PANEL_TOT_ROWS 
                  * WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS.height;

    Dimension dims = new Dimension(width, height);

    this.setPreferredSize(dims);
    this.setMaximumSize(dims);
    
    ////////////////////////
    UnitsPickList unitsPickList = new UnitsPickList();
    
    JPanel pickListPanel = WidgetFactory.makePanel();
    unitsPickListLabel    = WidgetFactory.makeLabel("Standard Unit:", true);
    pickListPanel.add(unitsPickListLabel);
    pickListPanel.add(unitsPickList);
 
    JPanel pickListGrid = new JPanel(new GridLayout(1,2));
    pickListGrid.add(pickListPanel);
    pickListGrid.add(WidgetFactory.makeDefaultSpacer() );

    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(pickListGrid);
    
    ////////////////////////
    
    JPanel precisionPanel = WidgetFactory.makePanel();
    precisionLabel    = WidgetFactory.makeLabel("Precision:", true);
    precisionPanel.add(precisionLabel);
    precisionField = WidgetFactory.makeOneLineTextField();
    precisionPanel.add(precisionField);

    JPanel precisionGrid = new JPanel(new GridLayout(1,2));
    precisionGrid.add(precisionPanel);
    precisionGrid.add(WidgetFactory.makeHTMLLabel(
        "<font color=\"#666666\">e.g: for an attribute with unit \"meter\", "
        +"a precision of \"0.1\" would be interpreted as precise to the "
        +"nearest 1/10th of a meter</font>", 2) );

    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(precisionGrid);
  
    ////////////////////////
    
    ItemListener listener = new ItemListener() {
        
          public void itemStateChanged(ItemEvent e) {

            String value = e.getItem().toString();
            Log.debug(45, "numberTypePickList state changed: " +value);
            
          }
        };

    numberTypePickList = WidgetFactory.makePickList(numberTypesArray, 
                                                    false, 0, listener);
    
    JPanel numberTypePanel = WidgetFactory.makePanel();
    numberTypeLabel    = WidgetFactory.makeLabel("Number Type:", true);
    numberTypePanel.add(numberTypeLabel);
    numberTypePanel.add(numberTypePickList);

    JPanel numberTypeGrid = new JPanel(new GridLayout(1,2));
    numberTypeGrid.add(numberTypePanel);
    numberTypeGrid.add(WidgetFactory.makeHTMLLabel(
        "<font color=\"#666666\">e.g: for an attribute with unit \"meter\", "
        +"a precision of \"0.1\" would be interpreted as precise to the "
        +"nearest 1/10th of a meter</font>", 2) );

    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(numberTypeGrid);
  
    ////////////////////////
    
  }
  
 
  private static Component makeHalfSpacer() {
    
    return Box.createRigidArea(new Dimension(
                    WizardSettings.DEFAULT_SPACER_DIMS.width/2,
                    WizardSettings.DEFAULT_SPACER_DIMS.height/2));
  }
  

  /** 
   *  checks that the user has filled in required fields - if not, highlights 
   *  labels to draw attention to them
   *
   *  @return   boolean true if user data validated OK. false if intervention 
   *            required
   */
  private OrderedMap validationNVP = new OrderedMap();
  //
  public boolean validateUserInput() {


    
//    validationNVP.clear();
//    getEnumListData("", validationNVP);
//  
//    if (validationNVP==null || validationNVP.size()<1) {
//      WidgetFactory.hiliteComponent(enumDefinitionLabel);
//      return false;
//    }
//
//
//    if (precisionField.getText().trim().equals("")) {
//
//      WidgetFactory.hiliteComponent(textDefinitionLabel);
//      precisionField.requestFocus();
//      
//      return false;
//    }
//    WidgetFactory.unhiliteComponent(enumDefinitionLabel);
//    WidgetFactory.unhiliteComponent(textDefinitionLabel);
    return true; 
  }



  /** 
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be 
   *            appended when making name/value pairs.  For example, in the 
   *            following xpath: 
   *
   *            /eml:eml/dataset/dataTable/attributeList/attribute[2]
   *            /measurementScale/nominal/nonNumericDomain/textDomain/definition
   *
   *            the root would be:
   *
   *              /eml:eml/dataset/dataTable/attributeList
   *                                /attribute[2]/measurementScale
   *
   *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN 
   *            SQUARE BRACKETS []
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap   returnMap  = new OrderedMap();
  private StringBuffer nomOrdBuff = new StringBuffer();
  ////////////////////////////////////////////////////////
  public OrderedMap getPanelData(String xPathRoot) {

    returnMap.clear();

//    nomOrdBuff.delete(0, nomOrdBuff.length());
//
//    nomOrdBuff.append(xPathRoot);
//    nomOrdBuff.append("/");
//    nomOrdBuff.append(nomOrdDisplayNames[nom_ord_mode]);
//    nomOrdBuff.append("/nonNumericDomain/");
//    
//    xPathRoot = nomOrdBuff.toString();
//    
//    if (currentSubPanel==enumSubPanel) {  //ENUMERATED
//      
//      getEnumListData(xPathRoot + "enumeratedDomain[1]", returnMap);
//      
//      if (enumDefinitionFreeTextCheckBox.isSelected()) {
//        
//        returnMap.put(  xPathRoot + "textDomain[1]/definition",
//                        "Free text (unrestricted)");
//        returnMap.put(xPathRoot + "textDomain[1]/pattern[1]", ".*");
//      }
//    
//    } else {                              //TEXT
//            
//      returnMap.put(  xPathRoot + "textDomain[1]/definition",
//                      precisionField.getText().trim());
//      
//      int index = 1;
//      List rowLists = textPatternsList.getListOfRowLists();
//      String nextStr = null;
//    
//      for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
//    
//        // CHECK FOR AND ELIMINATE EMPTY ROWS...
//        Object nextRowObj = it.next();
//        if (nextRowObj==null) continue;
//        
//        List nextRow = (List)nextRowObj;
//        if (nextRow.size() < 1) continue;
//        
//        if (nextRow.get(0)==null) continue;
//        nextStr = (String)(nextRow.get(0));
//        if (nextStr.trim().equals("")) continue;
//        
//        nomOrdBuff.delete(0, nomOrdBuff.length());
//        nomOrdBuff.append(xPathRoot);
//        nomOrdBuff.append("textDomain[1]/pattern[");
//        nomOrdBuff.append(index++);
//        nomOrdBuff.append("]");
//                        
//        returnMap.put(nomOrdBuff.toString(), nextStr);
//      }
//
//      String source = textSourceField.getText().trim();
//      if (!source.equals("")) {
//        returnMap.put(  xPathRoot + "textDomain[1]/source", source);
//      }
//    }
    return returnMap;
  }
}



//******************************************************************************
//******************************************************************************
//******************************************************************************
//******************************************************************************
//******************************************************************************
//******************************************************************************

/**
 *  Custom widget comprising two JComboBoxes side-by-side. Selecting a unitType 
 *  in the first combo box will cause the second box to be populated with units 
 *  of that unitType
 */

class UnitsPickList extends JPanel {


  private final JComboBox unitTypesList = new JComboBox();
  private final JComboBox unitsList     = new JComboBox();

  public UnitsPickList() {
  
    init();
  }
  
  
  private void init() {
  
    unitTypesList.setModel(new DefaultComboBoxModel(getUnitTypesArray()));
    
    unitTypesList.addItemListener(
      new ItemListener() {
    
        public void itemStateChanged(ItemEvent e) {

          String value = e.getItem().toString();
          Log.debug(45, "unitTypesList state changed: " +value);
          unitsList.setModel(
                        ((UnitTypesListItem)(e.getItem())).getComboBoxModel() );
          unitsList.setSelectedIndex(0);
        }
      });
      
    setUI(unitTypesList);
    unitTypesList.setSelectedIndex(0);
    
    unitsList.addItemListener(
      new ItemListener() {
    
        public void itemStateChanged(ItemEvent e) {

          String value = e.getItem().toString();
          Log.debug(45, "unitsList state changed: " +value);
          
        }
      });
    setUI(unitsList);
    
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(unitTypesList);
    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(unitsList);
  }
  
  public String getSelectedUnit() {
  
    return unitsList.getSelectedItem().toString();
  }
  
  
  private UnitTypesListItem[] getUnitTypesArray() {
  
    String[] unitTypesArray = WizardSettings.getUnitDictionaryUnitTypes();
    int totUnitTypes = unitTypesArray.length;
    UnitTypesListItem[] listItemsArray = new UnitTypesListItem[totUnitTypes + 1];
    
    String[] unitsOfThisType = null;
    
    listItemsArray[0] = new UnitTypesListItem("- Select a Unit Type -", 
                                              new String[] {""});

    for (int i=0; i < totUnitTypes; i++) {
    
      unitsOfThisType 
              = WizardSettings.getUnitDictionaryUnitsOfType(unitTypesArray[i]);
              
      listItemsArray[i + 1] = new UnitTypesListItem(  unitTypesArray[i], 
                                                      unitsOfThisType);
    }
    return listItemsArray;
  }
  
  
  private void setUI(JComboBox list) {
  
    list.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    list.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    list.setEditable(false);
  }
}

//******************************************************************************
//******************************************************************************
//******************************************************************************


/**
 *  container class to hold an array of units of a single unit type (as listed 
 *  in the eml unit dictionary). Has a toString() method that returns an 
 *  appropriate entry for the drop-down list.
 */
class UnitTypesListItem  {

  private ComboBoxModel model;
  private String        unitType;
  private String        unitTypeDisplayString;
  
  public UnitTypesListItem(String unitType, String[] unitsOfThisType) { 
  
    this.unitType = unitType;
    unitTypeDisplayString = getUnitTypeDisplayString();
    model = new DefaultComboBoxModel(unitsOfThisType);
  }

  public ComboBoxModel getComboBoxModel()   { return this.model;   }

  public String toString() { return unitTypeDisplayString; }
  
  
  private String getUnitTypeDisplayString() {
  
    if (unitType==null || unitType.trim().equals("")) return "";

    StringBuffer buff = new StringBuffer();
    
    final char SPACE = ' ';
    
    int length = unitType.length();
    
    char[] originalUnitTypeChars      = new char[length];
    unitType.getChars(0, length, originalUnitTypeChars, 0);

    char[] upperCaseUnitTypeChars = new char[length];
    unitType.toUpperCase().getChars(0, length, upperCaseUnitTypeChars, 0);
    
    //make first char uppercase:
    buff.append(upperCaseUnitTypeChars[0]);
    
    for (int i=1; i<length; i++) {

      //if it's an uppercase letter, add a space before it:
      if (originalUnitTypeChars[i]==upperCaseUnitTypeChars[i]) {
      
        buff.append(SPACE);
      }
      buff.append(originalUnitTypeChars[i]);
    }
    Log.debug(45, "initStringRepresentation() created: "+buff.toString()
                                                    +" from: "+unitType);
    return buff.toString();
  }
}