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
 *     '$Date: 2003-09-17 01:52:13 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DialogSubPanelAPI;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.utilities.OrderedMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
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

  private JLabel     unitPickListLabel;
  private JLabel     precisionLabel;
  private JTextField precisionField;
  private JTextField textSourceField;
  private CustomList textPatternsList;
  
  private AttributeDialog attributeDialog;
  
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
    
    ItemListener listener = new ItemListener() {
        
          public void itemStateChanged(ItemEvent e) {

            String value = e.getItem().toString();
            Log.debug(45, "PickList state changed: " +value);
            
          }
        };


    ////////////////////////
    JComboBox unitPickList = new JComboBox(unitPicklistVals);
    
    unitPickList.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    unitPickList.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    unitPickList.addItemListener(listener);
    unitPickList.setEditable(false);
    unitPickList.setSelectedIndex(0);

    JPanel pickListPanel = WidgetFactory.makePanel();
    unitPickListLabel    = WidgetFactory.makeLabel("Standard Unit:", true);
    pickListPanel.add(unitPickListLabel);
    pickListPanel.add(unitPickList);
 
    JPanel pickListGrid = new JPanel(new GridLayout(1,2));
    pickListGrid.add(pickListPanel);
    pickListGrid.add(WidgetFactory.makeHTMLLabel(
        "<font color=\"#666666\"> </font>", 1) );

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
  
  private final String[] unitPicklistVals
                  = { "dimensionless",
                      "second",
                      "meter",
                      "kilogram",
                      "kelvin",
                      "coulomb",
                      "ampere",
                      "mole",
                      "candela",
                      "number",
                      "cubicMeter",
                      "nominalMinute",
                      "nominalHour",
                      "nominalDay",
                      "nominalWeek",
                      "nominalYear",
                      "nominalLeapYear",
                      "nanogram",
                      "microgram",
                      "milligram",
                      "centigram",
                      "decigram",
                      "gram",
                      "dekagram",
                      "hectogram",
                      "megagram",
                      "tonne",
                      "pound",
                      "ton",
                      "celsius",
                      "fahrenheit",
                      "nanometer",
                      "micrometer",
                      "micron",
                      "millimeter",
                      "centimeter",
                      "decimeter",
                      "dekameter",
                      "hectometer",
                      "kilometer",
                      "megameter",
                      "angstrom",
                      "inch",
                      "Foot_US",
                      "foot",
                      "Foot_Gold_Coast",
                      "fathom",
                      "nauticalMile",
                      "yard",
                      "Yard_Indian",
                      "Link_Clarke",
                      "Yard_Sears",
                      "mile",
                      "nanosecond",
                      "microsecond",
                      "millisecond",
                      "centisecond",
                      "decisecond",
                      "dekasecond",
                      "hectosecond",
                      "kilosecond",
                      "megasecond",
                      "minute",
                      "hour",
                      "kiloliter",
                      "microliter",
                      "milliliter",
                      "liter",
                      "gallon",
                      "quart",
                      "bushel",
                      "cubicInch",
                      "pint",
                      "radian",
                      "degree",
                      "grad",
                      "megahertz",
                      "kilohertz",
                      "hertz",
                      "millihertz",
                      "newton",
                      "joule",
                      "calorie",
                      "britishThermalUnit",
                      "footPound",
                      "lumen",
                      "lux",
                      "becquerel",
                      "gray",
                      "sievert",
                      "katal",
                      "henry",
                      "megawatt",
                      "kilowatt",
                      "watt",
                      "milliwatt",
                      "megavolt",
                      "kilovolt",
                      "volt",
                      "millivolt",
                      "farad",
                      "ohm",
                      "ohmMeter",
                      "siemen",
                      "weber",
                      "tesla",
                      "pascal",
                      "megapascal",
                      "kilopascal",
                      "atmosphere",
                      "bar",
                      "millibar",
                      "kilogramsPerSquareMeter",
                      "gramsPerSquareMeter",
                      "milligramsPerSquareMeter",
                      "kilogramsPerHectare",
                      "tonnePerHectare",
                      "poundsPerSquareInch",
                      "kilogramPerCubicMeter",
                      "milliGramsPerMilliLiter",
                      "gramsPerLiter",
                      "milligramsPerCubicMeter",
                      "microgramsPerLiter",
                      "milligramsPerLiter",
                      "gramsPerCubicCentimeter",
                      "gramsPerMilliliter",
                      "gramsPerLiterPerDay",
                      "litersPerSecond",
                      "cubicMetersPerSecond",
                      "cubicFeetPerSecond",
                      "squareMeter",
                      "are",
                      "hectare",
                      "squareKilometers",
                      "squareMillimeters",
                      "squareCentimeters",
                      "acre",
                      "squareFoot",
                      "squareYard",
                      "squareMile",
                      "litersPerSquareMeter",
                      "bushelsPerAcre",
                      "litersPerHectare",
                      "squareMeterPerKilogram",
                      "metersPerSecond",
                      "metersPerDay",
                      "feetPerDay",
                      "feetPerSecond",
                      "feetPerHour",
                      "yardsPerSecond",
                      "milesPerHour",
                      "milesPerSecond",
                      "milesPerMinute",
                      "centimetersPerSecond",
                      "millimetersPerSecond",
                      "centimeterPerYear",
                      "knots",
                      "kilometersPerHour",
                      "metersPerSecondSquared",
                      "waveNumber",
                      "cubicMeterPerKilogram",
                      "cubicMicrometersPerGram",
                      "amperePerSquareMeter",
                      "amperePerMeter",
                      "molePerCubicMeter",
                      "molarity",
                      "molality",
                      "candelaPerSquareMeter",
                      "metersSquaredPerSecond",
                      "metersSquaredPerDay",
                      "feetSquaredPerDay",
                      "kilogramsPerMeterSquaredPerSecond",
                      "gramsPerCentimeterSquaredPerSecond",
                      "gramsPerMeterSquaredPerYear",
                      "gramsPerHectarePerDay",
                      "kilogramsPerHectarePerYear",
                      "kilogramsPerMeterSquaredPerYear",
                      "molesPerKilogram",
                      "molesPerGram",
                      "millimolesPerGram",
                      "molesPerKilogramPerSecond",
                      "nanomolesPerGramPerSecond",
                      "kilogramsPerSecond",
                      "tonnesPerYear",
                      "gramsPerYear",
                      "numberPerMeterSquared",
                      "numberPerKilometerSquared",
                      "numberPerMeterCubed",
                      "metersPerGram",
                      "numberPerGram",
                      "gramsPerGram",
                      "microgramsPerGram",
                      "cubicCentimetersPerCubicCentimeters" };
  
}