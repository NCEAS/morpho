/**
 *  '$RCSfile: IntervalRatioPanel.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2003-12-17 03:06:33 $'
 * '$Revision: 1.21 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;


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
import java.util.Vector;
import java.util.Enumeration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.Font;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;



public class IntervalRatioPanel extends JPanel implements WizardPageSubPanelAPI {

  private JLabel     unitsPickListLabel;
  private JLabel     precisionLabel;
  private JLabel     numberTypeLabel;
  private JLabel     boundsLabel;
  
  private UnitsPickList unitsPickList;
  private JTextField precisionField;
  private JComboBox  numberTypePickList;
  private CustomList boundsList;
  
  private AbstractWizardPage wizardPage;
  
  // note - order must match numberEMLVals array!
  private String[] numberTypesDisplayVals = new String[] { 
                        "NATURAL (non-zero counting numbers: 1, 2, 3..)", 
                        "WHOLE  (counting numbers & zero: 0, 1, 2, 3..)",
                        "INTEGER (+/- counting nums & zero: -2, -1, 0, 1..)", 
                        "REAL  (+/- fractions & non-fractions: -1/2, 3.14..)" 
                    };
                    
  // note - order must match numberTypesDisplayVals array!
  private String[] numberEMLVals = new String[] { 
                        "natural", 
                        "whole",
                        "integer", 
                        "real" 
                    };
										
	private String[] boundsPickListValues = new String[] {
												"<",
												"<="
										};
  
//////////////////////////////////////////////////
//
//from eml-entity.xsd:
//  
//Natural numbers
//
//The number type for this attribute consists
//of the 'natural' numbers, otherwise known as the counting numbers:
//1, 2, 3, 4, ...
//
//Whole numbers
//
//The number type for this attribute consists
//of the 'whole' numbers, which are the natural numbers plus the
//zero value: 0, 1, 2, 3, 4, ...
//
//Integer numbers
//
//The number type for this attribute consists
//of the 'integer' numbers, which are the natural numbers, plus the
//zero value, plus the negatives of the natural numbers: ..., -4, -3, 
//-2, -1, 0, 1, 2, 3, 4, ...
//
//Real numbers
//
//The number type for this attribute consists
//of the 'real' numbers, which contains both the rational numbers
//that can be expressed as fractions and the irrational numbers 
//that can not be expressed as fractions (such as the square root of 2).
//
//////////////////////////////////////////////////////

  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  /**
   *  Constructor
   *
   *  @param page the parent wizard page
   *
   *  @param nom_ord_mode can be AttributePage.MEASUREMENTSCALE_NOMINAL 
   *                  or AttributePage.MEASUREMENTSCALE_ORDINAL
   */
  public IntervalRatioPanel(AbstractWizardPage page) {
  
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

    this.setPreferredSize(dims);
    //this.setMaximumSize(dims);
    
    ////////////////////////
    unitsPickList = new UnitsPickList();
    
    JPanel pickListPanel = WidgetFactory.makePanel();
    unitsPickListLabel    = WidgetFactory.makeLabel("Standard Unit:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    pickListPanel.add(unitsPickListLabel);
    pickListPanel.add(unitsPickList);
 
    this.add(Box.createGlue());
    this.add(pickListPanel);
    
    ////////////////////////
    
    JPanel precisionPanel = WidgetFactory.makePanel();
    precisionLabel    = WidgetFactory.makeLabel("Precision:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    precisionPanel.add(precisionLabel);
    precisionField = WidgetFactory.makeOneLineTextField();
    precisionPanel.add(precisionField);

    JPanel precisionGrid = new JPanel(new GridLayout(1,2));
    precisionGrid.add(precisionPanel);
    precisionGrid.add(this.getLabel(
        WizardSettings.HTML_NO_TABLE_OPENING
        +WizardSettings.HTML_EXAMPLE_FONT_OPENING
        +"e.g: for an attribute with unit \"meter\", "
        +"a precision of \"0.1\" would be interpreted as precise to the "
        +"nearest 1/10th of a meter"
        +WizardSettings.HTML_EXAMPLE_FONT_CLOSING
        +WizardSettings.HTML_NO_TABLE_CLOSING));

    this.add(Box.createGlue());
    this.add(precisionGrid);
  
    ////////////////////////
    
    ItemListener listener = new ItemListener() {
        
          public void itemStateChanged(ItemEvent e) {

            String value = e.getItem().toString();
            Log.debug(45, "numberTypePickList state changed: " +value);
            
          }
        };

    numberTypePickList = WidgetFactory.makePickList(numberTypesDisplayVals, 
                                                    false, 0, listener);
    // using preferredSize just to ensure that the picklist displays correctly with the
    // arrow button shown properly, when the size of the attribute page is reduced(as in 
    // TextImportWizard). Without this, the picklist wouldnt appear fully. This doesnt 
    // affect the way it is displayed on a normal size attribute page.
						    
    numberTypePickList.setPreferredSize(new Dimension(200,10));
    
    JPanel numberTypePanel = WidgetFactory.makePanel();
    numberTypeLabel = WidgetFactory.makeLabel("Number Type:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    numberTypePanel.add(numberTypeLabel);
    numberTypePanel.add(numberTypePickList);

    JPanel numericDomainGrid = new JPanel(new GridLayout(1,2));
    numericDomainGrid.add(numberTypePanel);
		numericDomainGrid.add(this.getLabel(""));

		this.add(Box.createGlue());
    this.add(numericDomainGrid);
		
    ///////////////////////////
    JPanel boundsGrid = new JPanel();
		boundsGrid.setLayout(new GridLayout(1,2));
		
    JPanel boundsPanel = WidgetFactory.makePanel();

    boundsLabel = WidgetFactory.makeLabel("Bounds:", false,
						WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    boundsPanel.add(boundsLabel);
    		
    String[] colNames     = new String[] {  "Min.", "", "",
                                            "", "Max."};
		JLabel valueLabel = new JLabel("value", null, JLabel.CENTER);
		JComboBox combobox1 = WidgetFactory.makePickList(boundsPickListValues, false, 0, null);
		JComboBox combobox2 = WidgetFactory.makePickList(boundsPickListValues, false, 0, null);
		Object[] colTemplates = new Object[] {  new JTextField(),
														combobox1, 
														valueLabel, 
														combobox2,
														new JTextField()
													};
    
    boundsList = WidgetFactory.makeList(colNames, colTemplates, 2,
                                        true, false, false, true, false, false);
    boundsList.setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    boundsPanel.add(boundsList);
		
    boundsGrid.add(boundsPanel);
		boundsGrid.add(this.getLabel(""));
    
		this.add(Box.createGlue());
    this.add(boundsGrid);
		this.add(Box.createGlue());
    /////////////////

    
  
    
  }
  
 
  private static Component makeHalfSpacer() {
    
    return Box.createRigidArea(new Dimension(
                    WizardSettings.DEFAULT_SPACER_DIMS.width/2,
                    WizardSettings.DEFAULT_SPACER_DIMS.height/2));
  }
  
	private JLabel getLabel(String text) {
		
		if (text==null) text= "";
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
  
    WidgetFactory.unhiliteComponent(unitsPickListLabel);
    WidgetFactory.unhiliteComponent(precisionLabel);
    WidgetFactory.unhiliteComponent(numberTypeLabel);
    unhiliteBoundsLabel();
    precisionField.requestFocus();
  }
  

  /** 
   *  checks that the user has filled in required fields - if not, highlights 
   *  labels to draw attention to them
   *
   *  @return   boolean true if user data validated OK. false if intervention 
   *            required
   */
  public boolean validateUserInput() {
    
    // CHECK FOR AND ELIMINATE EMPTY ROWS...
    boundsList.deleteEmptyRows( CustomList.AND, 
                                new short[] { CustomList.EMPTY_STRING_TRIM,
                                              CustomList.IGNORE,
                                              CustomList.EMPTY_STRING_TRIM,
                                              CustomList.IGNORE             } );

    if (unitsPickList.getSelectedUnit().trim().equals("")) {

      WidgetFactory.hiliteComponent(unitsPickListLabel);
      
      return false;
    }
    WidgetFactory.unhiliteComponent(unitsPickListLabel);

    String precision = precisionField.getText().trim();
    if (precision.equals("") || !(WizardSettings.isFloat(precision))) {

      WidgetFactory.hiliteComponent(precisionLabel);
      precisionField.requestFocus();
      
      return false;
    }
    WidgetFactory.unhiliteComponent(precisionLabel);

    if (numberTypePickList.getSelectedItem().toString().trim().equals("")) {

      WidgetFactory.hiliteComponent(numberTypeLabel);
      
      return false;
    }
    WidgetFactory.unhiliteComponent(numberTypeLabel);

    if (!containsOnlyNumericValues(boundsList, 0, 2)) {

      WidgetFactory.hiliteComponent(boundsLabel);
      
      return false;
    }
    unhiliteBoundsLabel();

    return true; 
  }

  
  //need to set foreground, because unhilite reverts back to red foreground
  private void unhiliteBoundsLabel() {
  
    WidgetFactory.unhiliteComponent(boundsLabel);
    boundsLabel.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
  }
  
  //
  //  returns true if either column A or column B contains a non-float number
  //  list is the list to check
  //
  //  idxColA, idxColB are the indices of the 2 columns to check.
  //
  private boolean containsOnlyNumericValues(CustomList   list, 
                                            int idxColA, int idxColB) {
  
    boolean returnVal = true;
    List rowLists = list.getListOfRowLists();
    String nextColAStr = null;
    String nextColBStr = null;
  
    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;
    
      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;
    
      boolean nextColAIsNull = (nextRow.get(idxColA)==null);
      boolean nextColBIsNull = (nextRow.get(idxColB)==null);

      if (nextColAIsNull && nextColBIsNull) continue;
    
      if (!nextColAIsNull) {
        nextColAStr = (String)(nextRow.get(idxColA));
        if (!(nextColAStr.trim().equals("")) 
            && !WizardSettings.isFloat(nextColAStr)) returnVal = false;
      }
    
      if (!nextColBIsNull) {
        nextColBStr = (String)(nextRow.get(idxColB));
        if (!(nextColBStr.trim().equals("")) 
            && !WizardSettings.isFloat(nextColBStr)) returnVal = false;
      }
    }
    return returnVal;
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
  ////////////////////////////////////////////////////////
  public OrderedMap getPanelData(String xPathRoot) {

    returnMap.clear();
    
    returnMap.put(  xPathRoot + "/unit/standardUnit",
                    unitsPickList.getSelectedUnit().trim());
    
    returnMap.put(  xPathRoot + "/precision", 
                    precisionField.getText().trim());

    String numberType = numberTypePickList.getSelectedItem().toString().trim();
    String emlNumberType = null;
    if (numberType.equals(numberTypesDisplayVals[0])) {
    
      emlNumberType = numberEMLVals[0];  
    
    } else if (numberType.equals(numberTypesDisplayVals[1])) {
    
      emlNumberType = numberEMLVals[1];  
    
    } else if (numberType.equals(numberTypesDisplayVals[2])) {
    
      emlNumberType = numberEMLVals[2];  
    
    } else if (numberType.equals(numberTypesDisplayVals[3])) {
    
      emlNumberType = numberEMLVals[3];  
    }
    returnMap.put(  xPathRoot + "/numericDomain/numberType", emlNumberType);

    xPathRoot = xPathRoot + "/numericDomain/bounds[";
    int index = 0;
    List rowLists = boundsList.getListOfRowLists();
    String nextMin = null;
    String nextMax = null;
    Object nextExcl = null;
    

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
  
      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;
      
      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;
      
      index++;
      
      if (nextRow.get(0)!=null) {
      
        nextMin = (String)(nextRow.get(0));
        if (!nextMin.trim().equals("")) {
          returnMap.put(xPathRoot + index + "]/minimum", nextMin);
          nextExcl = nextRow.get(1);
					if(nextExcl instanceof Integer)
						System.out.println("GOT ELEMENT AS INTEGER");
					else if(nextExcl instanceof String)
						System.out.println("GOT ELEMENT AS String");
          if (nextExcl!=null && ((String)nextExcl).equals("<") ) {
      
            returnMap.put(xPathRoot + index + "]/minimum/@exclusive", "true");
        
          } else {
        
            returnMap.put(xPathRoot + index + "]/minimum/@exclusive", "false");
          }
        }
      }
      
      if (nextRow.get(4)!=null) {
      
        nextMax = (String)(nextRow.get(4));
        if (!nextMax.trim().equals("")) {
          returnMap.put(xPathRoot + index + "]/maximum", nextMax);
          
          nextExcl = nextRow.get(3);

          if (nextExcl!=null && ((String)nextExcl).equals("<") ) {
      
            returnMap.put(xPathRoot + index + "]/maximum/@exclusive", "true");
        
          } else {
        
            returnMap.put(xPathRoot + index + "]/maximum/@exclusive", "false");
          }
        }
      }
    }
    return returnMap;
  }
  
  /** 
   *  sets the Data in the IntervalRatio Panel. This is called by the setData() function 
   *  of AttributePage.
   
   *  @param  xPathRoot - this is the relative xPath of the current attribute
   *
   *  @param  map - Data is passed as OrderedMap of xPath-value pairs. xPaths in this map 
   *		    are absolute xPath and not the relative xPaths
   *
   **/
   
  public void setPanelData( String xPathRoot, OrderedMap map) {
	  
	String unit = (String)map.get( xPathRoot + "/unit/standardUnit");
	if(unit != null && !unit.equals(""))
		unitsPickList.setSelectedUnit(unit);
	
    	String precision = (String)map.get(  xPathRoot + "/precision");
	if(precision != null)
		precisionField.setText(precision);
	
	String type = (String)map.get( xPathRoot + "/numericDomain/numberType");
	if(type != null)
		numberTypePickList.setSelectedItem(type);
	
	int index = 1;
	while(true) {
		List row = new ArrayList();
		String min = (String)map.get(xPathRoot + "/numericDomain/bounds[" +index+ "]/minimum");
		if(min != null) {
			row.add(min);
			Boolean  excl = (Boolean)map.get(xPathRoot + "/numericDomain/bounds[" +index+ "]/minimum/@exclusive");
			if(excl != null)
				row.add("<");
			else
				row.add("<=");
		}
		else {
			row.add("");
			row.add("<");
		}
		row.add("value");
		String max = (String)map.get(xPathRoot + "/numericDomain/bounds[" +index+ "]/maximum");
		if(max != null) {
			Boolean  excl = (Boolean)map.get(xPathRoot + "/numericDomain/bounds[" +index+ "]/maximum/@exclusive");
			if(excl != null)
				row.add("<");
			else
				row.add("<=");
			row.add(max);
		}
		else {
			row.add("<");
			row.add("");
		}
		if(min == null && max == null)
			break;
		else
			boundsList.addRow(row);
		index++;
	}
	return;
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


  private final JComboBox unitTypesList  = new JComboBox();
  private final JComboBox unitsList      = new JComboBox();
  private final ComboBoxModel emptyModel = new DefaultComboBoxModel();
  private final String UNITLIST_DEFAULT  = "- Select a Unit Type -";
  
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
          
          if (unitTypesList.getSelectedIndex()==0) unitsList.setEnabled(false);
          else unitsList.setEnabled(true);

          unitsList.setModel(
                      ((UnitTypesListItem)(e.getItem())).getComboBoxModel() );
          unitsList.setSelectedIndex(0);
          unitsList.showPopup();
        }
      });
      
    setUI(unitTypesList);
    unitTypesList.setSelectedIndex(0);

    JPanel unitTypesPanel = WidgetFactory.makePanel();
    unitTypesPanel.add(unitTypesList);
    unitTypesPanel.add(WidgetFactory.makeDefaultSpacer());
    unitTypesPanel.add(WidgetFactory.makeDefaultSpacer());
    unitTypesPanel.add(WidgetFactory.makeDefaultSpacer());
    
    ///////////////////////
    unitsList.addItemListener(
      new ItemListener() {
    
        public void itemStateChanged(ItemEvent e) {

          String value = e.getItem().toString();
          Log.debug(45, "unitsList state changed: " +value);
        }
      });
    setUI(unitsList);
    
    JPanel unitsPanel = WidgetFactory.makePanel();
    unitsPanel.add(unitsList);
    unitsPanel.add(WidgetFactory.makeDefaultSpacer());
    unitsPanel.add(WidgetFactory.makeDefaultSpacer());
    unitsPanel.add(WidgetFactory.makeDefaultSpacer());

    ///////////////////////
    this.setLayout(new GridLayout(1,2));
    this.setPreferredSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    this.setMaximumSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    this.add(unitTypesPanel);
    this.add(unitsPanel);
    unitsList.setEnabled(false);
  }
  
  public String getSelectedUnit() {
  
    Object selItem = unitsList.getSelectedItem();
    if (selItem==null) return "";
    return selItem.toString();
  }
  
  public void setSelectedUnit(String unit) {
	  unitsList.setSelectedItem(unit);
	  return;
  }
  
  private UnitTypesListItem[] getUnitTypesArray() {
  
    String[] unitTypesArray = WizardSettings.getUnitDictionaryUnitTypes();
    int totUnitTypes = unitTypesArray.length;
    UnitTypesListItem[] listItemsArray = new UnitTypesListItem[totUnitTypes + 1];
    
    String[] unitsOfThisType = null;
    
    listItemsArray[0] = new UnitTypesListItem(UNITLIST_DEFAULT, 
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
  
  public void setData(int unitTypeIndex, int unitIndex) {
	
	UnitTypesListItem[] listItemsArray = getUnitTypesArray();
	if(listItemsArray == null) return;
	int length = listItemsArray.length;
	if(length < unitTypeIndex ) return;
	
	ItemListener[] itemListeners = unitTypesList.getItemListeners();
	for(int i =0; i<itemListeners.length; i++)
		unitTypesList.removeItemListener(itemListeners[i]);
	unitTypesList.setSelectedIndex(unitTypeIndex);
	unitsList.setModel( listItemsArray[unitTypeIndex].getComboBoxModel());
        unitsList.setEnabled(true);
	for(int i =0; i<itemListeners.length; i++)
		unitTypesList.addItemListener(itemListeners[i]);
	return;	  
  }
  
  public void getData(int[] indicesArray) {
	  
	  indicesArray[0] = unitTypesList.getSelectedIndex();
	  indicesArray[1] = unitsList.getSelectedIndex();
	  return;
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
    return buff.toString();
  }
}
