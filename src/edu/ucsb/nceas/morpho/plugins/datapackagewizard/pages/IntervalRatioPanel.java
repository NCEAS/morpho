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
 *     '$Date: 2004-04-20 00:51:35 $'
 * '$Revision: 1.40 $'
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
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.transform.TransformerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

public class IntervalRatioPanel extends JPanel implements WizardPageSubPanelAPI {

  private JLabel     unitsPickListLabel;
  private JLabel     precisionLabel;
  private JLabel     numberTypeLabel;
  private JLabel     boundsLabel;

  private UnitsPickList unitsPickList;
	private UnitTypesListItem[] unitTypesListItems;
	
  private JTextField precisionField;
  private JComboBox  numberTypePickList;
  private CustomList boundsList;
	private AbstractUIPage customPage = null;
	
	//list of custom pages.. one for each custom unit defined in this attribute page
	private List customPages = new ArrayList();
	
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

  private JButton addButton, delButton;

  private AbstractUIPage parentPage;
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
   * Constructor
   *
   * @param page the parent wizard page
   */
  public IntervalRatioPanel(AbstractUIPage page) {

    super();
    this.parentPage = page;
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
    unitsPickListLabel    = WidgetFactory.makeLabel("Standard Unit:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    unitsPickList = new UnitsPickList(parentPage, unitsPickListLabel);
    /*
    JPanel pickListPanel = WidgetFactory.makePanel();

    pickListPanel.add(unitsPickListLabel);
    pickListPanel.add(unitsPickList);
    */
    this.add(Box.createGlue());
    this.add(unitsPickList);

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
    JPanel boundsGrid = new JPanel(new GridLayout(1,2));

    JPanel boundsPanel = WidgetFactory.makePanel(3);;

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
                                        false, false, false, false, false, false);

    boundsPanel.add(boundsList);

    boundsGrid.add(boundsPanel);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));

    addButton = new JButton("Add");
    addButton.setPreferredSize(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    addButton.setMaximumSize(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    addButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);

    delButton = new JButton("Delete");
    delButton.setPreferredSize(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    delButton.setMaximumSize(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    delButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    delButton.setEnabled(false);

    addButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        boundsList.fireAddAction();
        if(boundsList.getRowCount() > 0)
          delButton.setEnabled(true);
      }
    });

    delButton.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        boundsList.fireDeleteAction();
        if(boundsList.getRowCount() == 0)
          delButton.setEnabled(false);
      }
    });

    buttonPanel.add(addButton);
    buttonPanel.add(delButton);
    buttonPanel.add(Box.createVerticalGlue());

    JPanel outerButtonPanel = new JPanel();
    outerButtonPanel.setLayout(new BoxLayout(outerButtonPanel, BoxLayout.X_AXIS));
    outerButtonPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
    outerButtonPanel.add(buttonPanel);

    boundsGrid.add(outerButtonPanel);


    this.add(Box.createVerticalGlue());
    this.add(Box.createGlue());
    this.add(boundsGrid);
    //this.add(Box.createGlue());
    /////////////////




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
                                              CustomList.IGNORE,
                                              CustomList.IGNORE,
                                              CustomList.EMPTY_STRING_TRIM } );

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

    if (!containsOnlyNumericValues(boundsList, 0, 4)) {

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
		String type = this.unitsPickList.getSelectedType().trim();
		String unit = unitsPickList.getSelectedUnit().trim();
		if(WizardSettings.isCustomUnit(type, unit)) {
			/*Iterator it = customPages.iterator();
			int cnt = 1;
			while(it.hasNext()) {
				CustomUnitPage cPage = (CustomUnitPage) it.next();
				OrderedMap map = cPage.getPageData(xPathRoot + "/additionalMetadata[" + cnt + "]");
				cnt++;
				returnMap.putAll(map);
			}*/
			returnMap.put(  xPathRoot + "/unit/customUnit", unit);
			
			
		} else {
			returnMap.put(  xPathRoot + "/unit/standardUnit", unit);
		}
		
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
    boundsList.fireEditingStopped();
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
   *  sets the Data in the IntervalRatio Panel. This is called by the setData()
   *  function of AttributePage.

   *  @param  xPathRoot - this is the relative xPath of the current attribute
   *
   *  @param  map - Data is passed as OrderedMap of xPath-value pairs. xPaths in
   *							this map are absolute xPath and not the relative xPaths
   *
   **/

  public void setPanelData(String xPathRoot, OrderedMap map) {

    String unit = (String)map.get(xPathRoot + "/unit/standardUnit");
		if(unit != null) map.remove(xPathRoot + "/unit/standardUnit");
		else {
			unit = (String)map.get(xPathRoot + "/unit/customUnit");
			if(unit != null) map.remove(xPathRoot + "/unit/customUnit");
		}
		
    if (unit != null && !unit.equals("")) {
      //UnitTypesListItem[] unitTypesListItem = unitTypesListItems;
      int totUnitTypes = unitTypesListItems.length;
      String[] unitsOfThisType = null;
			for (int i = 1; i < totUnitTypes; i++) {

        unitsOfThisType = unitTypesListItems[i].getUnitsOfThisType();
        int pos = -1;
        if ((pos = isPresentInList(unit, unitsOfThisType)) >= 0) {
					unitsPickList.setSelectedUnit(i, pos);
          break;
        }
      }
      
    }

    String precision = (String)map.get(xPathRoot + "/precision");
    if (precision != null) {
      precisionField.setText(precision);
      map.remove(xPathRoot + "/precision");
    }

    String type = (String)map.get(xPathRoot + "/numericDomain/numberType");
    if (type != null) {
      numberTypePickList.setSelectedItem(type);
      map.remove(xPathRoot + "/numericDomain/numberType");
    }

    int index = 1;

    while (true) {
      List row = new ArrayList();
      Object min = map.get(xPathRoot+"/numericDomain/bounds["+index+"]/minimum");
      if (min!=null) map.remove(xPathRoot+"/numericDomain/bounds["+index+"]/minimum");

      if (index == 1 && min == null) {
        min = map.get(xPathRoot + "/numericDomain/bounds/minimum");
      }
      if (min != null) {
        row.add((String)min);
        map.remove(xPathRoot + "/numericDomain/bounds/minimum");

        Object excl = map.get(xPathRoot + "/numericDomain/bounds[" + index
                              + "]/minimum/@exclusive");
        if (excl != null) {
          map.remove(xPathRoot + "/numericDomain/bounds["
                                    + index + "]/minimum/@exclusive");
        }
        if (index == 1 && excl == null) {
          excl = map.get(xPathRoot + "/numericDomain/bounds/minimum/@exclusive");
        }

        if (excl != null) {

          map.remove(xPathRoot + "/numericDomain/bounds/minimum/@exclusive");

          if (((String)excl).equals("true"))row.add("<");
          else row.add("<=");
        }
      } else {

        row.add("");
        row.add("<");
      }
      row.add("value");
      Object max = (String)map.get(xPathRoot + "/numericDomain/bounds[" + index
                                   + "]/maximum");
      if (max!=null) map.remove(xPathRoot + "/numericDomain/bounds[" + index
                                   + "]/maximum");

      if (index == 1 && max == null)
        max = map.get(xPathRoot + "/numericDomain/bounds/maximum");
      if (max != null) {
        Object excl = map.get(xPathRoot + "/numericDomain/bounds[" + index
                              + "]/maximum/@exclusive");
        if (excl!=null) map.remove(xPathRoot + "/numericDomain/bounds[" + index
                              + "]/maximum/@exclusive");

        if (index == 1 && excl == null)
          excl = map.get(xPathRoot + "/numericDomain/bounds/maximum/@exclusive");
        if (excl != null) {

          map.remove(xPathRoot + "/numericDomain/bounds/maximum/@exclusive");

          if (((String)excl).equals("true")) row.add("<");
          else row.add("<=");
        }
        row.add((String)max);
        map.remove(xPathRoot + "/numericDomain/bounds/maximum");
      } else {
        row.add("<");
        row.add("");
      }
      if (min == null && max == null) break;
      else boundsList.addRow(row);
      index++;
    }
    boundsList.fireEditingStopped();
    return;
  }

   private int isPresentInList(String unitType, String[] unitsOfThisType) {

     // Assuming that the units of a particular type are arranged in alphabetical order
     // if not, a linear search needs to be done instead of the binary search
     return Arrays.binarySearch(unitsOfThisType, unitType);
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
  private final String UNITLIST_DEFAULT  = "- Select a Unit Type -";
  private JButton newUnit;
  private JLabel unitTypeLabel;
  private JPanel parentPanel;
  
  private JDialog customUnitDialog = null;

  

  public static final int CUSTOM_UNIT_PANEL_WIDTH = 700;
  public static final int CUSTOM_UNIT_PANEL_HEIGHT = 450;

  public UnitsPickList(JPanel parent, JLabel unitTypeLabel) {

    this.parentPanel = parent;
    this.unitTypeLabel = unitTypeLabel;
    init();
  }
  
  private void init() {

    unitTypesListItems = getUnitTypesArray();
    unitTypesList.setModel(new DefaultComboBoxModel(unitTypesListItems));

    unitTypesList.addItemListener(
      new ItemListener() {

        public void itemStateChanged(ItemEvent e) {

          String value = e.getItem().toString();
          Log.debug(45, "unitTypesList state changed: " +value);

          if (unitTypesList.getSelectedIndex()==0) unitsList.setEnabled(false);
          else unitsList.setEnabled(true);

          unitsList.setModel(
                      ((UnitTypesListItem)(e.getItem())).getComboBoxModel() );
          String utype = ((UnitTypesListItem)unitTypesList.getSelectedItem()).getOriginalUnitType();
          String preftype = WizardSettings.getPreferredType(utype);
          if (preftype!=null) {
            unitsList.setSelectedItem(preftype);
          } else {
            unitsList.setSelectedIndex(0);
          }
          if(unitsList.isShowing())
            unitsList.showPopup();
        }
      });

    setUI(unitTypesList);
    unitTypesList.setSelectedIndex(0);

    JPanel unitTypesPanel = WidgetFactory.makePanel();
    unitTypesPanel.add(unitTypeLabel);
    unitTypesPanel.add(unitTypesList);
    /*unitTypesPanel.add(WidgetFactory.makeDefaultSpacer());
    unitTypesPanel.add(WidgetFactory.makeDefaultSpacer());
    unitTypesPanel.add(WidgetFactory.makeDefaultSpacer());*/

    ///////////////////////
    unitsList.addItemListener(
      new ItemListener() {

        public void itemStateChanged(ItemEvent e) {

          String value = e.getItem().toString();
          Log.debug(45, "unitsList state changed: " +value);
        }
      });
    setUI(unitsList);

    newUnit = new JButton("Define new unit");
    newUnit.addActionListener( new ActionListener() {

      public void actionPerformed(ActionEvent ae) {

        customPage = WizardPageLibrary.getPage(DataPackageWizardInterface.CUSTOM_UNIT_PAGE);
        int dwd = UISettings.POPUPDIALOG_WIDTH - UISettings.DIALOG_SMALLER_THAN_WIZARD_BY;
				int dht = UISettings.POPUPDIALOG_HEIGHT - UISettings.DIALOG_SMALLER_THAN_WIZARD_BY;
				
				ActionListener okAction = new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						customUnitOKAction();
					}
				};
				ActionListener cancelAction = new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						customUnitDialog.setVisible(false);
					}
				};
				customUnitDialog = WidgetFactory.makeContainerDialogNoParent(customPage, okAction, cancelAction);
				
				customUnitDialog.setTitle("New Unit Definition");
				Point loc = parentPanel.getLocationOnScreen();
				int wd = parentPanel.getWidth();
				int ht = parentPanel.getHeight();
				customUnitDialog.setLocation( (int)loc.getX() + wd/2 - dwd/2, (int)loc.getY() + ht/2 - dht/2);
				customUnitDialog.setSize(dwd, dht);
				customUnitDialog.setVisible(true);
			}
    });

    JPanel unitsPanel = WidgetFactory.makePanel();
    unitsPanel.add(unitsList);
    unitsPanel.add(WidgetFactory.makeDefaultSpacer());
    unitsPanel.add(newUnit);
    unitsPanel.add(WidgetFactory.makeDefaultSpacer());
    unitsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

    ///////////////////////
    this.setLayout(new GridLayout(1,2));
    this.setPreferredSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    this.setMaximumSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    this.add(unitTypesPanel);
    this.add(unitsPanel);
    unitsList.setEnabled(false);
  }

  private void customUnitOKAction() {

    if(customPage == null) {
      return;
    }

    if(!customPage.onAdvanceAction())
      return;
		customUnitDialog.setVisible(false);
		customPages.add(customPage);
		String xPath = "/additionalMetadata";
    OrderedMap map = customPage.getPageData(xPath);
		
    String type = getUnitTypeOfNewUnit(map, xPath);
    String newUnit = getNewUnit(map, xPath);
		String SIUnit = getSIUnit(map, xPath);
    String stdType = WizardSettings.getStandardFormOfUnitType(type);
		int idx = getIndexOfStandardType(WizardSettings.getDisplayFormOfUnitType(type));
		
    if(idx < 0) {
			UnitTypesListItem item = new UnitTypesListItem(stdType, newUnit);
      UnitTypesListItem[] newArray = new UnitTypesListItem[unitTypesListItems.length + 1];
      WizardSettings.insertObjectIntoArray(unitTypesListItems, item, newArray);
      unitTypesListItems = newArray;
      unitTypesList.setModel(new DefaultComboBoxModel(unitTypesListItems));
			this.unitTypesList.setSelectedItem(item);
			this.unitsList.setSelectedItem(newUnit);
			this.unitsList.hidePopup();
			
    } else {
			// add units to existing type
			if(idx >=0 && idx < unitTypesListItems.length) {
				UnitTypesListItem item = unitTypesListItems[idx];
				item.addUnit(newUnit);
				this.unitTypesList.setSelectedItem(item);
				this.unitsList.setModel(item.getComboBoxModel());
				this.unitsList.setSelectedItem(newUnit);
				this.unitsList.hidePopup();
			}
		}
		AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
		if(adp == null) {
			
			Log.debug(7, "Error obtaining the datapackage while trying to add a custom unit!!");
		} else {
			adp.addNewUnit(stdType, newUnit);
			insertIntoDOMTree(adp, map);
		}
    return;
  }

	
	private void insertIntoDOMTree(AbstractDataPackage adp, OrderedMap map) {
		
		DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
		Document doc = impl.createDocument("", "additionalMetadata", null);
		Node metadataRoot = doc.getDocumentElement();
		try {
			XMLUtilities.getXPathMapAsDOMTree(map, metadataRoot);
			
		}
		catch (TransformerException w) {
			Log.debug(5, "Unable to add addtmetadata details to package!");
			Log.debug(15, "TransformerException (" + w + ") calling "
			+ "XMLUtilities.getXPathMapAsDOMTree(map, metadataRoot) with \n"
			+ "map = " + map
			+ " and methodRoot = " + metadataRoot);
			w.printStackTrace();
			return;
		}
		Node check1 = adp.appendAdditionalMetadata(metadataRoot);
		if (check1 != null) {
			Log.debug(45, "added new addt metadata details to package...");
		} else {
			Log.debug(45, "cldnt added new metadata details to package...");
		}
		
	}
	
  private String getUnitTypeOfNewUnit( OrderedMap map, String xPath) {

    String t = (String) map.get(xPath + "/unitList/unit[1]/@unitType");
    return t;
  }

  private String getNewUnit(OrderedMap map, String xPath) {

    String unit = (String) map.get(xPath + "/unitList/unit[1]/@name");
    return unit;
	}
	
	private String getSIUnit(OrderedMap map, String xPath) {
		
		return (String)map.get(xPath + "/unitList/unit[1]/@parentSI");
	}
	
	private int getIndexOfStandardType(String unitType) {
		
		for(int i = 0; i < unitTypesListItems.length; i++) {
			
			String type = unitTypesListItems[i].toString();
			if(type.equals(unitType)) return i;
			
		}
		return -1;
		
	}
	
	
  private boolean isNewType(OrderedMap map, String xPath) {

    String t = (String) map.get(xPath + "/unitList/unitType[1]/@name");
    if(t == null) return false;
    // check if the given type has already been defined
		return true;
  }
	
	public String getSelectedUnit() {

    Object selItem = unitsList.getSelectedItem();
    if (selItem==null) return "";
    return selItem.toString();
  }
	
	public String getSelectedType() {

    Object selItem = this.unitTypesList.getSelectedItem();
    if (selItem==null) return "";
    return selItem.toString();
  }
	
	public String getSelectedSIUnit() {

    Object selItem = this.unitTypesList.getSelectedItem();
    if (selItem==null) return "";
		return WizardSettings.getPreferredType(((UnitTypesListItem)selItem).getOriginalUnitType());
  }
	
  public void setSelectedUnit(int typePos, int unitPos) {
		
		unitTypesList.setSelectedIndex(typePos);
    unitsList.setEnabled(true);
    unitsList.setSelectedIndex(unitPos);
    return;
  }

  private UnitTypesListItem[] getUnitTypesArray() {
		
		String[] unitTypesArray = WizardSettings.getUnitDictionaryUnitTypes();
		String[] totTypesArray = unitTypesArray;
		
		String[] customTypesArray = new String[0];
		AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
		if(adp != null) {
			customTypesArray = adp.getUnitDictionaryCustomUnitTypes();
			if(customTypesArray != null && customTypesArray.length > 0) {
				List totUnits = new ArrayList();
				int k = 0;
				for(k = 0; k < unitTypesArray.length; k++) {
					totUnits.add(unitTypesArray[k]);
				}
				for(int l = 0; l < customTypesArray.length; l++) {
					if (Arrays.binarySearch(unitTypesArray, customTypesArray[l]) < 0) {
						totUnits.add(customTypesArray[l]);
					}
				}
				String newArr[] = new String[totUnits.size()];
				newArr = (String[]) totUnits.toArray(newArr);
				Arrays.sort(newArr);
				totTypesArray = newArr;
			}
		}
		
    int totUnitTypes = totTypesArray.length;
    UnitTypesListItem[] listItemsArray = new UnitTypesListItem[totUnitTypes + 1];

    String[] unitsOfThisType = null;
		String[] customUnitsOfThisType = null;
		
		listItemsArray[0] = new UnitTypesListItem(UNITLIST_DEFAULT,
                                              new String[] {""});
		
		String prevType = "";
		for (int i=0; i < totUnitTypes; i++) {
			
			String type = totTypesArray[i];
			// to avoid duplicate unit types
			//if(type.equals(prevType)) continue;
			
			if(Arrays.binarySearch(unitTypesArray, type) >= 0) { // original unit Type
				
				unitsOfThisType
				= WizardSettings.getUnitDictionaryUnitsOfType(totTypesArray[i]);
				
				// could have custom units in it
				if(Arrays.binarySearch(customTypesArray, totTypesArray[i]) >= 0) {
					customUnitsOfThisType = adp.getUnitDictionaryUnitsOfType(totTypesArray[i]);
					String newArr[] = new String[unitsOfThisType.length + customUnitsOfThisType.length];
					int k = 0;
					for(k = 0; k < unitsOfThisType.length; k++)
						newArr[k] = unitsOfThisType[k];
					for(int l = 0; l < customUnitsOfThisType.length; l++)
						newArr[k++] = customUnitsOfThisType[l];
					
					Arrays.sort(newArr);
					unitsOfThisType = newArr;
				}
				listItemsArray[i + 1] = new UnitTypesListItem(  totTypesArray[i],
				unitsOfThisType);
			} else {
				
				unitsOfThisType = adp.getUnitDictionaryUnitsOfType(totTypesArray[i]);
				listItemsArray[i + 1] = new UnitTypesListItem(  totTypesArray[i],
				unitsOfThisType);
			}
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
		
		unitTypesList.setSelectedIndex(unitTypeIndex);
		unitsList.setModel( listItemsArray[unitTypeIndex].getComboBoxModel());
		unitsList.setEnabled(true);
		unitsList.setSelectedIndex(unitIndex);
		unitsList.setPopupVisible(false);
		
		return;
	}

  public void getData(int[] indicesArray) {

    indicesArray[0] = unitTypesList.getSelectedIndex();
    indicesArray[1] = unitsList.getSelectedIndex();
    return;
  }
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
class UnitTypesListItem  implements Comparable{

  private ComboBoxModel model;
  private String        unitType;
  private String        unitTypeDisplayString;
  private String[]			unitsOfThisType;

	public UnitTypesListItem(String unitType, String oneUnitOfThisType) {
			
			this.unitType = unitType;
			String[] units = new String[1];
			units[0] = oneUnitOfThisType;
			this.unitsOfThisType = units;
			unitTypeDisplayString = WizardSettings.getDisplayFormOfUnitType(unitType);
      model = new DefaultComboBoxModel(unitsOfThisType);
    }
	
  public UnitTypesListItem(String unitType, String[] unitsOfThisType) {

    this.unitType = unitType;
    unitTypeDisplayString = WizardSettings.getDisplayFormOfUnitType(unitType);
    this.unitsOfThisType = unitsOfThisType;
    model = new DefaultComboBoxModel(unitsOfThisType);
  }

  public ComboBoxModel getComboBoxModel()   { return this.model;   }

  public String toString() { return unitTypeDisplayString; }

  public void addUnit(String newUnit) {
		
		if(Arrays.binarySearch(unitsOfThisType, newUnit) >= 0) return;
		String[] newArr = new String[unitsOfThisType.length + 1];
    WizardSettings.insertObjectIntoArray(unitsOfThisType, newUnit, newArr);
    unitsOfThisType = newArr;
    model = new DefaultComboBoxModel(unitsOfThisType);
		
  }
	
	public String[] getUnitsOfThisType() { return this.unitsOfThisType; }
	
  public int compareTo(Object o) {

    return unitTypeDisplayString.compareTo( ((UnitTypesListItem)o).toString());
  }
	
	public String getOriginalUnitType() {
		return this.unitType;
	}
}
