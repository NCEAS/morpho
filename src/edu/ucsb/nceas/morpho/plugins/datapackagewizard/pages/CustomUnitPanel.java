/**
 *  '$RCSfile: CustomUnitPanel.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-03-19 18:11:52 $'
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

import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AddDocumentationCommand;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ItemListener;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Container;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractAction;


public class CustomUnitPanel extends JPanel implements WizardPageSubPanelAPI 
{
	
	private static final String TOP_LABEL_STRING = "Enter the type, definition and name of the new unit. You can either select an existing Unit Type or create a new Unit Type by defining it in terms of the fundamental unit types.";
	
	private JLabel typeNameLabel;
	private JComboBox typeNameComboBox;
	private static final String TYPE_NAME_LABEL = WizardSettings.HTML_NO_TABLE_OPENING + "Select the category that the unit belongs to. You can also define a new category if needed." + WizardSettings.HTML_NO_TABLE_CLOSING; 
	
	private JLabel typeDefnLabel;
	private CustomList typeDefnList;
	private static final String TYPE_DEFN_LABEL = WizardSettings.HTML_NO_TABLE_OPENING + "Define the unit category as a product of the basic unit types available. Specify the unit type and the power it is raised to. <br>" + 
	WizardSettings.HTML_EXAMPLE_FONT_OPENING + "e.g:&nbsp; for a unit type like 'velocity', which is given by meterPerSecond, the definition would be - <br>" + "Unit = Length &nbsp;&nbsp;&nbsp; Power = 1 <br>Unit = Time &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Power = -1" + WizardSettings.HTML_EXAMPLE_FONT_CLOSING + WizardSettings.HTML_NO_TABLE_CLOSING;
	
	private JLabel unitNameLabel;
	private JTextField unitNameField;
	private static final String UNIT_NAME_LABEL = WizardSettings.HTML_NO_TABLE_OPENING + "Specify the name of the unit. " + WizardSettings.HTML_EXAMPLE_FONT_OPENING + "e.g:&nbsp; meterPerSecond" + WizardSettings.HTML_EXAMPLE_FONT_CLOSING + WizardSettings.HTML_NO_TABLE_CLOSING;
	
	private JLabel SIunitNameLabel;
	private JTextField SIunitNameField;
	private static final String SI_UNIT_NAME_LABEL = WizardSettings.HTML_NO_TABLE_OPENING + "Specify the SI unit for this Unit Type. " + WizardSettings.HTML_EXAMPLE_FONT_OPENING + "e.g:&nbsp; meter" + WizardSettings.HTML_EXAMPLE_FONT_CLOSING + WizardSettings.HTML_NO_TABLE_CLOSING;
	
	private JLabel unitFactorLabel;
	private JTextField unitFactorField;
	private static final String UNIT_FACTOR_LABEL = WizardSettings.HTML_NO_TABLE_OPENING + "Define the multiplier to convert the given unit to the SI unit. " + WizardSettings.HTML_EXAMPLE_FONT_OPENING + "e.g:&nbsp; 1" + WizardSettings.HTML_EXAMPLE_FONT_CLOSING + WizardSettings.HTML_NO_TABLE_CLOSING;
	
	private JLabel unitDescLabel;
	private JTextField unitDescField;
	
	private String[] unitTypes;
	private String[] basicUnitTypes;
	private String currentUnitTypeSelected = "";
	private JPanel parentPanel;
	
	CustomUnitPanel(JPanel parent) {
		
		this.parentPanel = parent;
		init();
	}
	
	private void init() {
		
		String[] tempUnitTypes = WizardSettings.getUnitDictionaryUnitTypes();
		
		
		basicUnitTypes = WizardSettings.getUnitDictionaryBasicUnitTypes();
		createDisplayString(basicUnitTypes);
		
		unitTypes = new String[tempUnitTypes.length + 1];
		unitTypes[0] = "";
		for(int i = 0; i < tempUnitTypes.length; i++)
			unitTypes[i+1] = WizardSettings.getUnitTypeDisplayString(tempUnitTypes[i]);
		
		tempUnitTypes = null;
		
		typeNameLabel = WidgetFactory.makeLabel("Unit Type", true);
		typeNameComboBox = new JComboBox(unitTypes);
		typeNameComboBox.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				String selItem = (String)ie.getItem();
				if(currentUnitTypeSelected.equals(selItem))
					return;
				currentUnitTypeSelected = selItem;
				setDefnListValue();
			}
		}); 
		typeNameComboBox.setEditable(true);
		
		typeDefnLabel = WidgetFactory.makeLabel("Unit Type Defintion", false);
		JComboBox cbox = new JComboBox(basicUnitTypes);
		cbox.setEditable(false);
		JTextField jtf = new JTextField("1");
		Object[] colObjects = new Object[2];
		colObjects[0] = cbox;
		colObjects[1] = jtf;
		String[] colHeaders = new String[] {"Unit", "Power"};
		double[] colWidths = new double[] {75.0, 25.0};
		typeDefnList = new CustomList(colHeaders, colObjects, -1, true, false, false, true, true, true);
		typeDefnList.setColumnWidthPercentages(colWidths);
		typeDefnList.setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS_SMALL);
		
		unitNameLabel = WidgetFactory.makeLabel("Unit Name", true);
		unitNameField = WidgetFactory.makeOneLineTextField();
		
		SIunitNameLabel = WidgetFactory.makeLabel("SI Unit", true);
		SIunitNameField = WidgetFactory.makeOneLineTextField();
		
		unitFactorLabel = WidgetFactory.makeLabel("Multiplication Factor", false);
		unitFactorField = WidgetFactory.makeOneLineTextField();
		
		unitDescLabel = WidgetFactory.makeLabel("Description", false);
		unitDescField = WidgetFactory.makeOneLineTextField();
		
		setLayout(new BorderLayout());
		
		JLabel topLabel = WidgetFactory.makeHTMLLabel(TOP_LABEL_STRING, 2);
		this.add(topLabel, BorderLayout.NORTH);
		
		////////////////
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.add(WidgetFactory.makeDefaultSpacer());
		
		JPanel typeNamePanel = WidgetFactory.makePanel();
		typeNamePanel.add(typeNameLabel);
		JPanel typeNameGrid = new JPanel(new GridLayout(1,2));
		JPanel outerComboboxPanel = WidgetFactory.makeVerticalPanel(2);
		outerComboboxPanel.add(Box.createGlue());
		outerComboboxPanel.add(typeNameComboBox);
		outerComboboxPanel.add(Box.createGlue());
		typeNameGrid.add(outerComboboxPanel);
		typeNameGrid.add(getLabel(TYPE_NAME_LABEL));
		typeNamePanel.add(typeNameGrid);
		
		centerPanel.add(typeNamePanel);
		centerPanel.add(WidgetFactory.makeDefaultSpacer());
		
		/////////////////
		
		JPanel typeDefnPanel = WidgetFactory.makePanel();
		typeDefnPanel.add(typeDefnLabel);
		JPanel typeDefnGrid = new JPanel(new GridLayout(1,2));
		typeDefnGrid.add(typeDefnList);
		typeDefnGrid.add(getLabel(TYPE_DEFN_LABEL));
		typeDefnPanel.add(typeDefnGrid);
		centerPanel.add(typeDefnPanel);
		centerPanel.add(WidgetFactory.makeDefaultSpacer());
		
		/////////////////
		
		JPanel unitNamePanel = WidgetFactory.makePanel();
		unitNamePanel.add(unitNameLabel);
		JPanel unitNameGrid = new JPanel(new GridLayout(1,2));
		unitNameGrid.add(unitNameField);
		unitNameGrid.add(getLabel(UNIT_NAME_LABEL));
		unitNamePanel.add(unitNameGrid);
		centerPanel.add(unitNamePanel);
		centerPanel.add(WidgetFactory.makeDefaultSpacer());
		
		/////////////////
		
		JPanel SIunitNamePanel = WidgetFactory.makePanel();
		SIunitNamePanel.add(SIunitNameLabel);
		JPanel SIunitNameGrid = new JPanel(new GridLayout(1,2));
		SIunitNameGrid.add(SIunitNameField);
		SIunitNameGrid.add(getLabel(SI_UNIT_NAME_LABEL));
		SIunitNamePanel.add(SIunitNameGrid);
		centerPanel.add(SIunitNamePanel);
		centerPanel.add(WidgetFactory.makeDefaultSpacer());
		
		/////////////////
		
		JPanel unitFactorPanel = WidgetFactory.makePanel();
		unitFactorPanel.add(unitFactorLabel);
		JPanel unitFactorGrid = new JPanel(new GridLayout(1,2));
		unitFactorGrid.add(unitFactorField);
		unitFactorGrid.add(getLabel(UNIT_FACTOR_LABEL));
		unitFactorPanel.add(unitFactorGrid);
		centerPanel.add(unitFactorPanel);
		centerPanel.add(WidgetFactory.makeDefaultSpacer());
		
		/////////////////
		
		JPanel unitDescPanel = WidgetFactory.makePanel();
		unitDescPanel.add(unitDescLabel);
		JPanel unitDescGrid = new JPanel(new GridLayout(1,2));
		unitDescGrid.add(unitDescField);
		unitDescGrid.add(new JLabel(""));
		unitDescPanel.add(unitDescGrid);
		centerPanel.add(unitDescPanel);
		centerPanel.add(WidgetFactory.makeDefaultSpacer());
		
		/////////////////
		
		this.add(centerPanel, BorderLayout.CENTER);
		
	}
	
	private void setDefnListValue() {
		
		typeDefnList.removeAllRows();
		String unitType = (String)typeNameComboBox.getSelectedItem();
		if(unitType.trim().equals("")) return;
		
		// check if its a basic unit type.. if so, nothing to be done
		List bTypes = Arrays.asList(basicUnitTypes);
		if(bTypes.contains(unitType)) {
			
			SIunitNameField.setText(WizardSettings.getPreferredType(unitType.toLowerCase()));
			typeDefnList.setEnabled(false);
			SIunitNameField.setEditable(false);
			return;
		}
		
		List types = Arrays.asList(unitTypes);
		if(types.contains(unitType)) { 
			SIunitNameField.setText(WizardSettings.getPreferredType(unitType.toLowerCase()));
			SIunitNameField.setEditable(false);
			typeDefnList.setEnabled(false);
			// retreive the defn from the emlUnitDictionary file
			List defns = WizardSettings.getDefinitionsForUnitType(unitType);
			if(defns == null || defns.size() == 0) {
				Log.debug(12, " Got defn = " + defns);
				return;
			}
			typeDefnList.setEnabled(true);
			Log.debug(12, " Got defn = " + defns.size());
			Iterator it = defns.iterator();
			while(it.hasNext()) {
				List row = (List)it.next();
				typeDefnList.addRow(row);
			}
			typeDefnList.setEnabled(false);
			
		} else {
			
			SIunitNameField.setText("");
			SIunitNameField.setEditable(true);
			typeDefnList.setEnabled(true);
		}
		return;
	}
	
	
	
	private void createDisplayString(String[] unitTypes) {
		
		for(int i = 0; i < unitTypes.length; i++) {
			StringBuffer buff = new StringBuffer(unitTypes[i]);
			buff.setCharAt(0, Character.toUpperCase(buff.charAt(0)));
			unitTypes[i] = buff.toString();
		}
	}
	
	private JLabel getLabel(String text) {

    if (text==null) text="";
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
		
	}

  /** 
   *  checks that the user has filled in required fields - if not, highlights 
   *  labels to draw attention to them
   *
   *  @return   boolean true if user data validated OK. false if intervention 
   *            required
   */
  public boolean validateUserInput() {
		
		String typeName = (String)typeNameComboBox.getSelectedItem();
		if(typeName == null || typeName.trim().equals("")) {
			WidgetFactory.hiliteComponent(typeNameLabel);
		}
		WidgetFactory.unhiliteComponent(typeNameLabel);
		
		List defnRows = this.typeDefnList.getListOfRowLists();
		if(defnRows != null && defnRows.size() > 0) {
			
			List units = new ArrayList();
			Iterator it = defnRows.iterator();
			int cnt = 0;
			while(it.hasNext()) {
				List row = (List)it.next();
				String unit = (String)row.get(0);
				if(units.contains(unit)) {
					WidgetFactory.hiliteComponent(typeDefnLabel);
					JOptionPane.showMessageDialog(this, "Units cannot be repeated. Accumulate all terms with same unit into one term", "Error", JOptionPane.ERROR_MESSAGE);
					typeDefnList.editCellAt(cnt, 0);
					return false;
				}
				units.add(unit);
				try {
					double pwr = Double.parseDouble((String)row.get(1));
				} catch(Exception e) {
					WidgetFactory.hiliteComponent(typeDefnLabel);
					JOptionPane.showMessageDialog(this, "Power of a unit should be a number", "Error", JOptionPane.ERROR_MESSAGE);
					typeDefnList.editCellAt(cnt, 1);
					return false;
				}
				cnt++;
			}
		}
		WidgetFactory.unhiliteComponent(typeDefnLabel);
		
		String unitName = this.unitNameField.getText();
		if(unitName.trim().equals("")) {
			
			WidgetFactory.hiliteComponent(unitNameLabel);
			return false;
		}
		
		String[] existingUnits = WizardSettings.getUnitDictionaryUnitsOfType(typeName);
		if(existingUnits != null && existingUnits.length > 0) {
			List list = Arrays.asList(existingUnits);
			if(list.contains(unitName)) {
				WidgetFactory.hiliteComponent(unitNameLabel);
				JOptionPane.showMessageDialog(this, "The specified unit type already has a unit by the same name", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		WidgetFactory.unhiliteComponent(unitNameLabel);
		
		if(typeDefnList.isEnabled()) {
			String SIunitName = this.SIunitNameField.getText();
			if(SIunitName.trim().equals("")) {
				
				WidgetFactory.hiliteComponent(SIunitNameLabel);
				return false;
			}
		}
		WidgetFactory.unhiliteComponent(SIunitNameLabel);
		
		String factor = this.unitFactorField.getText();
		if(!factor.trim().equals("")) {
			
			try {
				double f = Double.parseDouble(factor);
			} catch(Exception e) {
				WidgetFactory.hiliteComponent(unitFactorLabel);
				JOptionPane.showMessageDialog(this, "The multiplication factor can only be a number", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		WidgetFactory.unhiliteComponent(unitFactorLabel);
		
		return true;
	}

  /** 
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be 
   *            appended when making name/value pairs.  For example, in the 
   *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the 
   *            root would be /eml:eml/dataset/keywordSet[2]
   *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN 
   *            SQUARE BRACKETS []
   *
   *  @return   data the OrderedMap object that contains all the
   *            key/value paired settings for this particular panel
   */
  public OrderedMap getPanelData(String xPathRoot) {
		
		OrderedMap map = new OrderedMap();
		
		String xpath = "/unitList";
		
		String unitType = (String) this.typeNameComboBox.getSelectedItem();
		if(typeDefnList.isEnabled()) {
			// new unit type
			map.put(xpath + "/unitType[1]/@id", unitType);
			map.put(xpath + "/unitType[1]/@name", unitType);
			List rows = this.typeDefnList.getListOfRowLists();
			Iterator it = rows.iterator();
			int cnt = 1;
			while(it.hasNext()) {
				List row = (List) it.next();
				map.put(xpath + "/unitType[1]/dimension[" + cnt +"]/@name", (String) row.get(0));
				map.put(xpath + "/unitType[1]/dimension[" + cnt +"]/@power", (String) row.get(1));
				cnt++;
			}
		}
		
		String unitName = this.unitNameField.getText();
		String factor = this.unitFactorField.getText();
		if(factor.trim().equals(""))
			factor = "1";
		String desc = this.unitDescField.getText();
		String SIunitName = this.SIunitNameField.getText();
		
		int cnt = 1;
		//create SI unit first if necessary
		if(typeDefnList.isEnabled() && !SIunitName.equals(unitName)) {
			map.put(xpath + "/unit[" + cnt +"]/@id", SIunitName);
			map.put(xpath + "/unit[" + cnt +"]/@name", SIunitName);
			map.put(xpath + "/unit[" + cnt +"]/@unitType", unitType);
			map.put(xpath + "/unit[" + cnt +"]/@multiplierToSI", "1");
			map.put(xpath + "/unit[" + cnt +"]/description", desc);
			cnt++;
		}
		
		map.put(xpath + "/unit[" + cnt +"]/@id", unitName);
		map.put(xpath + "/unit[" + cnt +"]/@name", unitName);
		map.put(xpath + "/unit[" + cnt +"]/@unitType", unitType);
		map.put(xpath + "/unit[" + cnt +"]/@multiplierToSI", factor);
		map.put(xpath + "/unit[" + cnt +"]/description", desc);
		
		
		return map;
	}
	
	
	/**
	*	  sets the data in the sub panel using the key/values paired Map object
	*
	*  @param    xPathRoot the string xpath to which this dialog's xpaths will be 
  *            appended when making name/value pairs.  For example, in the 
  *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the 
  *            root would be /eml:eml/dataset/keywordSet[2]
  *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN 
  *            SQUARE BRACKETS []
	*  @param  map - OrderedMap of xPath-value pairs. xPaths in this map
	*		    		are absolute xPath and not the relative xPaths
	*
	**/

	public void setPanelData(String xPathRoot, OrderedMap map) {
		
		// not required for this panel
	}
	
}
