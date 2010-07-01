/**
 *  '$RCSfile: CustomUnitPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.5 $'
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
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AddDocumentationCommand;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
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
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractAction;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

public class CustomUnitPage extends AbstractUIPage { 

	private final String pageID     = DataPackageWizardInterface.CUSTOM_UNIT_PAGE;
  private final String pageNumber = "";
  private final String title      = /*"Custom Unit Definition"*/ Language.getInstance().getMessage("CustomUnitDefinition");
  private final String subtitle   = "";
	
	
	private static final String TOP_LABEL_STRING = 
					/*"Enter a name and an optional description for your custom unit."*/
					Language.getInstance().getMessage("CustomUnitpage.TOP_LABEL_STRING")
					;
	
	private static final String CATEGORY_HEAD_LABEL = 
					/*"What category does the new unit belong to?"*/
					Language.getInstance().getMessage("CustomUnitpage.CATEGORY_HEAD_LABEL")
					;
	
	
	private final String[] categoryLabels = {	/*"One of the existing unit types"*/ Language.getInstance().getMessage("CustomUnitpage.categoryLabels_1"), 
												/*"A new custom unit type"*/ Language.getInstance().getMessage("CustomUnitpage.categoryLabels_2")
												};
																						
	private final Color disabledBackgroundColor = new Color(192, 192, 192);
	
	private JLabel typeNameLabel;
	private JComboBox typeNameComboBox;
	private static final String UNIT_TYPE_LABEL = 
						/*"Select the unit type that the new unit belongs to"*/
						Language.getInstance().getMessage("CustomUnitpage.UNIT_TYPE_LABEL")
						;
	
	private JLabel existingTypeDefnLabel;
	private CustomList existingTypeDefnList;
	private static final String EXISTING_TYPE_DEFN_LABEL = 
					WizardSettings.HTML_NO_TABLE_OPENING 
					+ /*"Definition for the selected unit category. "*/ Language.getInstance().getMessage("CustomUnitpage.EXISTING_TYPE_DEFN_LABEL_1") + " "
					+/*"It is represented as a product of the basic unit types and an exponential (power) factor."*/ Language.getInstance().getMessage("CustomUnitpage.EXISTING_TYPE_DEFN_LABEL_2")  
					+ "<br>" 
					+ WizardSettings.HTML_EXAMPLE_FONT_CLOSING + WizardSettings.HTML_NO_TABLE_CLOSING;
	
	
	private JLabel newTypeNameLabel;
	private JTextField newTypeNameField;
	private static final String NEW_TYPE_NAME_HEADER_LABEL = 
						/*"Enter the name of the new unit type"*/ Language.getInstance().getMessage("CustomUnitpage.NEW_TYPE_NAME_HEADER_LABEL");
	
	private JLabel newTypeDefnLabel;
	private CustomList newTypeDefnList;
	private static final String NEW_TYPE_DEFN_HEADER_LABEL = 
		/*"Provide a definition for this custom unit, in terms of the basic unit types shown," +
		" and an exponential (power) factor"*/
		Language.getInstance().getMessage("CustomUnitpage.NEW_TYPE_DEFN_HEADER_LABEL")
		;
	
	private static final String NEW_TYPE_DEFN_EXPL_LABEL =
		WizardSettings.HTML_NO_TABLE_OPENING + 
		WizardSettings.HTML_EXAMPLE_FONT_OPENING 
		+ /*"e.g:&nbsp; "*/ Language.getInstance().getMessage("e.g") + ":&nbsp; " 
		/*
		+"for a unit type like 'velocity', which is given by meterPerSecond, the definition would be - " 
		*/
		+ Language.getInstance().getMessage("CustomUnitpage.NEW_TYPE_DEFN_EXPL_LABEL") + " -"
		+"<br>" 
		+ "Unit = Length &nbsp;&nbsp;&nbsp; Power = 1 <br>Unit = Time &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Power = -1" 
		+ WizardSettings.HTML_EXAMPLE_FONT_CLOSING + WizardSettings.HTML_NO_TABLE_CLOSING;
	
	private JLabel unitNameLabel;
	private JTextField unitNameField;
	private static final String UNIT_NAME_LABEL = 
				WizardSettings.HTML_NO_TABLE_OPENING 
				+ /*"Specify the name of the unit. "*/ Language.getInstance().getMessage("CustomUnitpage.UNIT_NAME_LABEL") + " "
				+ WizardSettings.HTML_EXAMPLE_FONT_OPENING 
				+ /*"e.g*/ Language.getInstance().getMessage("e.g") +":&nbsp; " + /*"meterPerSecond"*/ Language.getInstance().getMessage("CustomUnitpage.meterPerSecond")
				+ WizardSettings.HTML_EXAMPLE_FONT_CLOSING + WizardSettings.HTML_NO_TABLE_CLOSING;
	
	private JLabel unitDescLabel;
	private JTextArea unitDescField;
	private static final String UNIT_DEFN_LABEL = 
				WizardSettings.HTML_NO_TABLE_OPENING 
				+ /*"Provide a description of the unit. "*/ Language.getInstance().getMessage("CustomUnitpage.UNIT_DEFN_LABEL_1") +" "
				+ WizardSettings.HTML_EXAMPLE_FONT_OPENING 
				+ /*"e.g*/ Language.getInstance().getMessage("e.g") + ":&nbsp; " + /*"SI unit of velocity"*/ Language.getInstance().getMessage("CustomUnitpage.UNIT_DEFN_LABEL_2")  
				+ WizardSettings.HTML_EXAMPLE_FONT_CLOSING + WizardSettings.HTML_NO_TABLE_CLOSING;
	
	private JLabel existingSIunitNameLabel;
	private JTextField existingSIunitNameField;
	private static final String EXISTING_SI_UNIT_NAME_LABEL = 
				WizardSettings.HTML_NO_TABLE_OPENING 
				+ /*"SI unit for the selected unit category. "*/ Language.getInstance().getMessage("CustomUnitpage.EXISTING_SI_UNIT_NAME_LABEL") 
				+ WizardSettings.HTML_NO_TABLE_CLOSING;
	
	private JLabel newSIunitNameLabel;
	private JComboBox newSIunitNameBox;
	private static final String NEW_SI_UNIT_HEADER_LABEL = 
		/*"Choose the SI unit for this unit type and optionally define the multiplier to convert the new unit to the chosen SI unit"*/
		Language.getInstance().getMessage("CustomUnitpage.NEW_SI_UNIT_HEADER_LABEL")
		;
	
	private static final String SI_UNIT_NAME_LABEL = 
				WizardSettings.HTML_NO_TABLE_OPENING 
				+ /*"Specify the SI unit for this Unit Type. "*/ Language.getInstance().getMessage("CustomUnitpage.SI_UNIT_NAME_LABEL") +" "
				+ WizardSettings.HTML_EXAMPLE_FONT_OPENING 
				+ /*"e.g*/ Language.getInstance().getMessage("e.g") + ":&nbsp; " +  /*"meter"*/ Language.getInstance().getMessage("meter") 
				
				+ WizardSettings.HTML_EXAMPLE_FONT_CLOSING + WizardSettings.HTML_NO_TABLE_CLOSING;
	
	private JLabel existingUnitFactorLabel;
	private JTextField existingUnitFactorField;
	private JLabel newUnitFactorLabel;
	private JTextField newUnitFactorField;
	private static final String UNIT_FACTOR_LABEL = 
				WizardSettings.HTML_NO_TABLE_OPENING 
				+ //"Define the multiplier to convert this unit to the SI unit. " +
				 WizardSettings.HTML_EXAMPLE_FONT_OPENING 
				 + /*"e.g*/ Language.getInstance().getMessage("e.g") + ":&nbsp; " +  "0.001" 
				 + WizardSettings.HTML_EXAMPLE_FONT_CLOSING + WizardSettings.HTML_NO_TABLE_CLOSING;
	
	
	private String[] unitTypes;
	private String[] basicUnitTypes;
	private String currentUnitTypeSelected = "";
	
	private int currentCategorySelection = -1;
	private JPanel existingTypePanel;
	private JPanel newTypePanel = new JPanel();
	private JPanel categoryRadioPanel;
	private JLabel categoryLabel;
	private JPanel centerPanel = new JPanel();
	private JPanel middleExistingTypePanel;
	private AbstractDataPackage adp = null;
	
	public CustomUnitPage(WizardContainerFrame frame) {
		nextPageID = "";
		if(frame != null)
		{
		    this.adp = frame.getAbstractDataPackage();
		}
		else
		{
			this.adp = UIController.getInstance().getCurrentAbstractDataPackage();
		}
		init();
	}
	
	private void init() {
		
		String[] tempUnitTypes = WizardSettings.getUnitDictionaryUnitTypes();
		String[] customTypes = new String[0];
		//AbstractDataPackage adp = UIController.getInstance().getCurrentAbstractDataPackage();
		if(adp != null) {
			customTypes = adp.getUnitDictionaryCustomUnitTypes();
		}
		basicUnitTypes = WizardSettings.getUnitDictionaryBasicUnitTypes();
		createDisplayString(basicUnitTypes);
		String[] SIUnits = WizardSettings.getSIUnits();
		
		List unitTypeList = new ArrayList();
		unitTypeList.add("");
		for(int i = 0; i < tempUnitTypes.length; i++)
			unitTypeList.add(WizardSettings.getDisplayFormOfUnitType(tempUnitTypes[i]));
		for(int i = 0; i<customTypes.length; i++) {
			if(!unitTypeList.contains(customTypes[i]))
				unitTypeList.add(WizardSettings.getDisplayFormOfUnitType(customTypes[i]));
		}
		
		unitTypes = new String[unitTypeList.size()];
		unitTypes = (String[])unitTypeList.toArray(unitTypes);
		tempUnitTypes = null;
		
		setLayout(new BorderLayout());
		
		//////////////////
		//// top Panel
		JPanel topPanel = WidgetFactory.makeVerticalPanel(-1);
		this.add(topPanel, BorderLayout.NORTH);
		
		//// top label
		JLabel topLabel = getLabel(TOP_LABEL_STRING, false);
		topPanel.add(topLabel);
		topPanel.add(WidgetFactory.makeDefaultSpacer());
		
		//// unit name
		unitNameLabel = WidgetFactory.makeLabel(/*"Unit Name:"*/ Language.getInstance().getMessage("UnitName") + ":" ,
												true);
		unitNameField = WidgetFactory.makeOneLineTextField();
		JPanel unitNamePanel = WidgetFactory.makePanel();
		unitNamePanel.add(unitNameLabel);
		JPanel unitNameGrid = new JPanel(new GridLayout(1,2));
		unitNameGrid.add(unitNameField);
		unitNameGrid.add(getLabel(UNIT_NAME_LABEL));
		unitNamePanel.add(unitNameGrid);
		topPanel.add(unitNamePanel);
		topPanel.add(WidgetFactory.makeDefaultSpacer());
		
		//// unit description
		unitDescLabel = WidgetFactory.makeLabel(/*"Description:"*/ Language.getInstance().getMessage("Description") +":" ,
												false);
		unitDescField = WidgetFactory.makeTextArea("", 3, true);
		JPanel unitDescPanel = WidgetFactory.makePanel();
		unitDescPanel.add(unitDescLabel);
		JPanel unitDescGrid = new JPanel(new GridLayout(1,2));
		unitDescGrid.add(new JScrollPane(unitDescField));
		unitDescGrid.add(getLabel(UNIT_DEFN_LABEL));
		unitDescPanel.add(unitDescGrid);
		topPanel.add(unitDescPanel);
		topPanel.add(WidgetFactory.makeDefaultSpacer());
		
		////////////////
		//// center Panel
		
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		this.add(centerPanel, BorderLayout.CENTER);
		
		//// category question
		JPanel categoryPanel = WidgetFactory.makeVerticalPanel(-1);
    categoryLabel = getLabel(CATEGORY_HEAD_LABEL, true);
		ActionListener categoryListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				
				if(ae.getActionCommand().equals(categoryLabels[0])) {
					if(currentCategorySelection == 0) return;
					if(currentCategorySelection == 1) centerPanel.remove(newTypePanel);
					centerPanel.add(existingTypePanel);
					centerPanel.setMaximumSize(centerPanel.getPreferredSize());
					centerPanel.setMinimumSize(centerPanel.getPreferredSize());
					currentCategorySelection = 0;
					refreshUI();
				} else {
					if(currentCategorySelection == 1) return;
					if(currentCategorySelection == 0) centerPanel.remove(existingTypePanel);
					centerPanel.add(newTypePanel);
					centerPanel.setMaximumSize(centerPanel.getPreferredSize());
					centerPanel.setMinimumSize(centerPanel.getPreferredSize());
					currentCategorySelection = 1;
					refreshUI();
				}
			}
		};
		categoryRadioPanel = WidgetFactory.makeRadioPanel(categoryLabels, -1, categoryListener);
		categoryPanel.add(categoryLabel);
		categoryPanel.add(categoryRadioPanel);
		categoryPanel.setMaximumSize(categoryPanel.getPreferredSize());
    categoryPanel.setMinimumSize(categoryPanel.getPreferredSize());

		centerPanel.add(categoryPanel);
		
		/////////////////
		//// existing unit type panel
		
		existingTypePanel = WidgetFactory.makeVerticalPanel(-1);
		JLabel unitTypeLabel = getLabel(UNIT_TYPE_LABEL, true);
		existingTypePanel.add(WidgetFactory.makeDefaultSpacer());
		existingTypePanel.add(unitTypeLabel);
		
		// type name combobox
		typeNameLabel = WidgetFactory.makeLabel(/*"Unit Type:"*/ Language.getInstance().getMessage("UnitType") + ":",
												true);
		typeNameComboBox = new JComboBox(unitTypes);
		typeNameComboBox.addItemListener( new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				String selItem = (String)ie.getItem();
				if(currentUnitTypeSelected.equals(selItem))
					return;
				if(currentUnitTypeSelected.trim().length() == 0) {
					existingTypePanel.add(middleExistingTypePanel);
					existingTypePanel.add(Box.createGlue());
					existingTypePanel.setMaximumSize(existingTypePanel.getPreferredSize());
					existingTypePanel.setMinimumSize(existingTypePanel.getPreferredSize());
					refreshUI();
					
				} else if (selItem.length() == 0) {
					existingTypePanel.remove(middleExistingTypePanel);
					existingTypePanel.setMaximumSize(existingTypePanel.getPreferredSize());
					existingTypePanel.setMinimumSize(existingTypePanel.getPreferredSize());
					refreshUI();
				}
				if(selItem.length() > 0) existingTypeNameChanged(selItem);
				currentUnitTypeSelected = selItem;
			}
		}); 
		JPanel typeNamePanel = WidgetFactory.makePanel();
		typeNamePanel.add(typeNameLabel);
		JPanel typeNameGrid = new JPanel(new GridLayout(1,2));
		typeNameGrid.add(typeNameComboBox);
		typeNameGrid.add(getLabel(""));
		typeNamePanel.add(typeNameGrid);
		
		existingTypePanel.add(typeNamePanel);
		existingTypePanel.add(WidgetFactory.makeDefaultSpacer());
		
		existingTypePanel.setMaximumSize(existingTypePanel.getPreferredSize());
		existingTypePanel.setMinimumSize(existingTypePanel.getPreferredSize());
		
		/////////////////
		// existing type middle Panel - appears after the user selects a type
		middleExistingTypePanel = WidgetFactory.makeVerticalPanel(-1);
		
		// defn for existing types
		
		String[] colHeaders = new String[] {/*"Unit"*/ Language.getInstance().getMessage("Unit"),
											/*"Power"*/ Language.getInstance().getMessage("Power")
											};
		double[] colWidths = new double[] {75.0, 25.0};
		Object[] existingColObjects = new Object[2];
		JTextField text1 = new JTextField();
		JTextField text2 = new JTextField();
		text1.setBackground(this.disabledBackgroundColor);
		text2.setBackground(this.disabledBackgroundColor);
		text1.setEditable(false);
		text2.setEditable(false);
		existingColObjects[0] = text1;
		existingColObjects[1] = text2;
		existingTypeDefnList = new CustomList(colHeaders, existingColObjects, -1, false, false, false, false, false, false);
		existingTypeDefnList.setColumnWidthPercentages(colWidths);
		existingTypeDefnList.setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS_SMALL);
		existingTypeDefnList.setEnabled(false);
		existingTypeDefnList.setBackground(this.disabledBackgroundColor);
		existingTypeDefnList.setMaximumSize(WidgetFactory.getDimForNumberOfLines(4));
		existingTypeDefnList.setPreferredSize(WidgetFactory.getDimForNumberOfLines(4));
		//existingTypeDefnList.setMinimumSize(WidgetFactory.getDimForNumberOfLines(4));
		
		JPanel typeDefnPanel = WidgetFactory.makePanel();
		existingTypeDefnLabel = WidgetFactory.makeLabel(/*"Unit Type Defintion:"*/ Language.getInstance().getMessage("UnitTypeDefintion") + ":"
														, false);
		typeDefnPanel.add(existingTypeDefnLabel);
		JPanel typeDefnGrid = new JPanel(new GridLayout(1,2));
		typeDefnGrid.add(existingTypeDefnList);
		typeDefnGrid.add(getLabel(EXISTING_TYPE_DEFN_LABEL));
		typeDefnPanel.add(typeDefnGrid);
		middleExistingTypePanel.add(typeDefnPanel);
		middleExistingTypePanel.add(WidgetFactory.makeDefaultSpacer());
		middleExistingTypePanel.add(Box.createGlue());
		
		/////////////////
		// SI unit for existing panel
		
		JPanel SIunitNamePanel = WidgetFactory.makePanel();
		existingSIunitNameLabel = WidgetFactory.makeLabel(/*"SI Unit:"*/ Language.getInstance().getMessage("SI_Unit") + ":",
															false);
		existingSIunitNameField = WidgetFactory.makeOneLineTextField();
		existingSIunitNameField.setEditable(false);
		existingSIunitNameField.setBackground(disabledBackgroundColor);
		SIunitNamePanel.add(existingSIunitNameLabel);
		JPanel SIunitNameGrid = new JPanel(new GridLayout(1,2));
		SIunitNameGrid.add(existingSIunitNameField);
		SIunitNameGrid.add(getLabel(EXISTING_SI_UNIT_NAME_LABEL));
		SIunitNamePanel.add(SIunitNameGrid);
		middleExistingTypePanel.add(SIunitNamePanel);
		middleExistingTypePanel.add(WidgetFactory.makeDefaultSpacer());
		middleExistingTypePanel.add(Box.createGlue());
		
		/////////////////
		
		JPanel unitFactorPanel = WidgetFactory.makePanel();
		existingUnitFactorLabel = WidgetFactory.makeLabel(/*"Multiplier:"*/ Language.getInstance().getMessage("Multiplier") + ":",
															false);
		existingUnitFactorField = WidgetFactory.makeOneLineTextField();
		unitFactorPanel.add(existingUnitFactorLabel);
		JPanel unitFactorGrid = new JPanel(new GridLayout(1,2));
		unitFactorGrid.add(existingUnitFactorField);
		unitFactorGrid.add(getLabel(UNIT_FACTOR_LABEL));
		unitFactorPanel.add(unitFactorGrid);
		middleExistingTypePanel.add(unitFactorPanel);
		middleExistingTypePanel.add(WidgetFactory.makeDefaultSpacer());
		middleExistingTypePanel.add(Box.createGlue());
		
		/////////////////
		/////////////////
		// new type Panel
		
		newTypePanel = WidgetFactory.makeVerticalPanel(-1);
		JLabel newUnitTypeLabel = getLabel(NEW_TYPE_NAME_HEADER_LABEL, true);
		newTypePanel.add(WidgetFactory.makeDefaultSpacer());
		newTypePanel.add(newUnitTypeLabel);
		newTypePanel.add(WidgetFactory.makeHalfSpacer());
		newTypePanel.add(Box.createGlue());
		
		//////////////////
		//// new type name textfield
		
		newTypeNameLabel = WidgetFactory.makeLabel(/*"Unit Type:"*/ Language.getInstance().getMessage("UnitType") + ":",
													 true);
		newTypeNameField = WidgetFactory.makeOneLineTextField();
		JPanel newTypeNamePanel = WidgetFactory.makePanel();
		newTypeNamePanel.add(newTypeNameLabel);
		JPanel newTypeNameGrid = new JPanel(new GridLayout(1,2));
		newTypeNameGrid.add(newTypeNameField);
		newTypeNameGrid.add(getLabel(""));
		newTypeNamePanel.add(newTypeNameGrid);
		newTypePanel.add(newTypeNamePanel);
		//newTypePanel.add(WidgetFactory.makeDefaultSpacer());
		newTypePanel.add(WidgetFactory.makeHalfSpacer());
		newTypePanel.add(Box.createGlue());
		
		/////////////////
		//// new type defn customlist
		Object[] newColObjects = new Object[2];
		JComboBox basicTypeCombobox = new JComboBox(basicUnitTypes);
		basicTypeCombobox.setEditable(false);
		JTextField jtf = new JTextField("1");
		newColObjects[0] = basicTypeCombobox;
		newColObjects[1] = jtf;
		newTypeDefnList = new CustomList(colHeaders, newColObjects, -1, true, false, false, true, false, false);
		newTypeDefnList.setColumnWidthPercentages(colWidths);
		newTypeDefnList.setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS_SMALL);
		newTypeDefnList.setMaximumSize(WidgetFactory.getDimForNumberOfLines(4));
		newTypeDefnList.setPreferredSize(WidgetFactory.getDimForNumberOfLines(4));
		newTypeDefnList.fireAddAction();
		
		JPanel newTypeDefnTopPanel = WidgetFactory.makeVerticalPanel(-1);
		JLabel newUnitDefnTopLabel = getLabel(NEW_TYPE_DEFN_HEADER_LABEL, true);
		newTypeDefnTopPanel.add(newUnitDefnTopLabel);
		newTypeDefnTopPanel.add(WidgetFactory.makeHalfSpacer());
		
		JPanel newTypeDefnPanel = WidgetFactory.makePanel();
		newTypeDefnLabel = WidgetFactory.makeLabel(/*"Unit Type Defintion:"*/ Language.getInstance().getMessage("UnitTypeDefintion") + ":",
													true);
		newTypeDefnPanel.add(newTypeDefnLabel);
		JPanel newTypeDefnGrid = new JPanel(new GridLayout(1,2));
		newTypeDefnGrid.add(newTypeDefnList);
		newTypeDefnGrid.add(getLabel(NEW_TYPE_DEFN_EXPL_LABEL));
		newTypeDefnPanel.add(newTypeDefnGrid);
		
		newTypeDefnTopPanel.add(newTypeDefnPanel);
		newTypePanel.add(newTypeDefnTopPanel);
		//newTypePanel.add(WidgetFactory.makeDefaultSpacer());
		newTypePanel.add(WidgetFactory.makeHalfSpacer());
		newTypePanel.add(Box.createGlue());
		
		/////////////////
		//// SI unit for new panel
		
		JPanel newSIunitNameTopPanel = WidgetFactory.makeVerticalPanel(-1);
		JLabel newSIUnitNameTopLabel = getLabel(NEW_SI_UNIT_HEADER_LABEL, true);
		newSIunitNameTopPanel.add(newSIUnitNameTopLabel);
		newSIunitNameTopPanel.add(WidgetFactory.makeHalfSpacer());
		
		JPanel newSIunitNamePanel = WidgetFactory.makePanel();
		newSIunitNameLabel = WidgetFactory.makeLabel(/*"SI Unit:"*/ Language.getInstance().getMessage("SI_Unit") + ":",
														true);
		newSIunitNameBox = new JComboBox(SIUnits);
		newSIunitNamePanel.add(newSIunitNameLabel);
		JPanel newSIunitNameGrid = new JPanel(new GridLayout(1,2));
		newSIunitNameGrid.add(newSIunitNameBox);
		newSIunitNameGrid.add(getLabel(EXISTING_SI_UNIT_NAME_LABEL));
		newSIunitNamePanel.add(newSIunitNameGrid);
		newSIunitNameTopPanel.add(newSIunitNamePanel);
		newTypePanel.add(newSIunitNameTopPanel);
		//newTypePanel.add(WidgetFactory.makeDefaultSpacer());
		newTypePanel.add(WidgetFactory.makeHalfSpacer());
		newTypePanel.add(Box.createGlue());
		
		/////////////////
		//// multiplier field for new type panel
		
		JPanel newUnitFactorPanel = WidgetFactory.makePanel();
		newUnitFactorLabel = WidgetFactory.makeLabel(/*"Multiplier:"*/ Language.getInstance().getMessage("Multiplier") + ":" ,
														false);
		newUnitFactorField = WidgetFactory.makeOneLineTextField();
		newUnitFactorPanel.add(newUnitFactorLabel);
		JPanel newUnitFactorGrid = new JPanel(new GridLayout(1,2));
		newUnitFactorGrid.add(newUnitFactorField);
		newUnitFactorGrid.add(getLabel(UNIT_FACTOR_LABEL));
		newUnitFactorPanel.add(newUnitFactorGrid);
		newTypePanel.add(newUnitFactorPanel);
		//newTypePanel.add(WidgetFactory.makeDefaultSpacer());
		newTypePanel.add(WidgetFactory.makeHalfSpacer());
		newTypePanel.add(Box.createGlue());
		
		
		/////////////////
		//centerPanel.add(Box.createGlue());
		this.add(centerPanel, BorderLayout.CENTER);
		
	}
	
	private void refreshUI() {
		
		centerPanel.validate();
    centerPanel.repaint();
  }
	
	private void existingTypeNameChanged(String selItem) {
		
		setDefnListValue(selItem);
	}
	
	private void setDefnListValue(String unitType) {
		
		if(unitType.trim().equals("")) return;
		existingTypeDefnList.removeAllRows();
		
		existingSIunitNameField.setText(WizardSettings.getPreferredType( WizardSettings.getStandardFormOfUnitType(unitType) ));
			
		// check if its a basic unit type.. if so, nothing to be done
		List bTypes = Arrays.asList(basicUnitTypes);
		if(bTypes.contains(unitType)) {
			
			return;
		}
		
		List types = Arrays.asList(unitTypes);
		if(types.contains(unitType)) { 
			
			// retreive the defn from the emlUnitDictionary file
			List defns = WizardSettings.getDefinitionsForUnitType(unitType);
			if(defns == null || defns.size() == 0) {
				Log.debug(12, " Got defn = " + defns);
				return;
			}
			Log.debug(12, " Got defn = " + defns.size());
			Iterator it = defns.iterator();
			while(it.hasNext()) {
				List row = (List)it.next();
				existingTypeDefnList.addRow(row);
			}
			
		}
		existingTypeDefnList.scrollToRow(0);
		existingTypeDefnList.setSelectedRows(new int[]{});
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
	
	private JLabel getLabel(String text, boolean hilite) {
		
		text = WizardSettings.HTML_TABLE_LABEL_OPENING + text + WizardSettings.HTML_TABLE_LABEL_CLOSING;
		JLabel label = new JLabel(text);
		label.setAlignmentX(1.0f);
    label.setFont(WizardSettings.WIZARD_CONTENT_FONT);
		Dimension dim = WidgetFactory.getDimForNumberOfLines(1);
		label.setMaximumSize(dim);
		label.setMinimumSize(dim);
		label.setPreferredSize(dim);
		label.setBorder(BorderFactory.createMatteBorder(0,1,0,1, (Color)null));
		if (hilite) label.setForeground(WizardSettings.WIZARD_CONTENT_REQD_TEXT_COLOR);
    else label.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    
		return label;
	}
	
	
	/**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onRewindAction() {
  }

  /**
   *  The action to be executed when the page is loaded
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onLoadAction() {
  }

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return this.pageID;}

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
  public String getNextPageID() { return this.nextPageID; }

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  
	
	private int getSelectedRadioIndex(JPanel radioPanel) {
		
		Container c = (Container)(radioPanel.getComponent(1));
		int cnt = c.getComponentCount();
		for(int i = 0; i<cnt; i++) {
			JRadioButton jrb = (JRadioButton)c.getComponent(i);
			if(jrb.isSelected()) return i;
		}
		return -1;
	}
	
  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
		
		String unitName = this.unitNameField.getText();
		if(unitName.trim().equals("")) {
			WidgetFactory.hiliteComponent(unitNameLabel);
			this.unitNameField.requestFocus();
			return false;
		}
		WidgetFactory.unhiliteComponent(unitNameLabel);
		
		int category = getSelectedRadioIndex(this.categoryRadioPanel);
		if(category == -1) {
			WidgetFactory.hiliteComponent(categoryLabel);
			return false;
		}
		WidgetFactory.unhiliteComponent(categoryLabel);
		
		if(category == 0) {
			
			String type = (String)this.typeNameComboBox.getSelectedItem();
			if(type.trim().equals("")) {
				WidgetFactory.hiliteComponent(this.typeNameLabel);
				return false;
			}
			WidgetFactory.unhiliteComponent(this.typeNameLabel);
			
			return true;
		
		} else {
			
			String unitType = this.newTypeNameField.getText();
			if(unitType.trim().equals("")) {
				WidgetFactory.hiliteComponent(this.newTypeNameLabel);
				this.newTypeNameField.requestFocus();
				return false;
			}
			WidgetFactory.unhiliteComponent(this.newTypeNameLabel);
			
			List rows = this.newTypeDefnList.getListOfRowLists();
			if(rows == null || rows.size() == 0) {
				WidgetFactory.hiliteComponent(this.newTypeDefnLabel);
				return false;
			}
			int cnt = -1;
			Iterator it = rows.iterator();
			while(it.hasNext()) {
				cnt++;
				List row = (List)it.next();
				String power = (String)row.get(1);
				try {
					Double.parseDouble(power);
				} catch(Exception e) {
					if(power.trim().length() > 0) {
						JOptionPane.showMessageDialog(this, "Power of a unit in the definition list has to be a number", "Error", JOptionPane.ERROR_MESSAGE);
					}
					WidgetFactory.hiliteComponent(this.newTypeDefnLabel);
					this.newTypeDefnList.editCellAt(cnt, 1);
					return false;
				}
			}
			WidgetFactory.unhiliteComponent(this.newTypeDefnLabel);
			
			return true;
		}
		
	}
	/**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular UI page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular UI page
   */
  public OrderedMap getPageData() {
		// not supported
		throw new UnsupportedOperationException(
      "getPageData(String rootXPath) Method Not Implemented in Custom Unit Page");
	}
	
  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular UI page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular UI page
   */
  public OrderedMap getPageData(String xPathRoot) {
		
		OrderedMap map = new OrderedMap();
		String unit = this.unitNameField.getText();
		String desc = this.unitDescField.getText();
		if (desc.trim().equals("")) desc = unit; 
		xPathRoot += "/unitList";
		
		int category = this.getSelectedRadioIndex(this.categoryRadioPanel);
		
		if(category == 0) { // existing unit type
			
			map.put(xPathRoot + "/unit[1]/@id", unit);
			map.put(xPathRoot + "/unit[1]/@name", unit);
			map.put(xPathRoot + "/unit[1]/description", desc);
			
			String type = (String)this.typeNameComboBox.getSelectedItem();
			type = WizardSettings.getStandardFormOfUnitType(type);
			String multiplier = this.existingUnitFactorField.getText();
			String SIUnit = this.existingSIunitNameField.getText();
			if(multiplier.trim().equals("")) multiplier = "";
						
			map.put(xPathRoot + "/unit[1]/@unitType", type);
			map.put(xPathRoot + "/unit[1]/@parentSI", SIUnit);
			map.put(xPathRoot + "/unit[1]/@multiplierToSI", multiplier);
			
		} else { //new type
			
			String type = (String)this.newTypeNameField.getText();
			type = WizardSettings.getStandardFormOfUnitType(type);
			map.put(xPathRoot + "/unitType[1]/@id", type);
			map.put(xPathRoot + "/unitType[1]/@name", type);
			
			List defns = this.newTypeDefnList.getListOfRowLists();
			Iterator it = defns.iterator();
			int cnt = 1;
			while(it.hasNext()) {
				List row = (List)it.next();
				String name = (String)row.get(0);
				String power = (String)row.get(1);
				map.put(xPathRoot + "/unitType[1]/dimension[" + cnt + "]/@name", name);
				if(!power.equals("1")) map.put(xPathRoot + "/unitType[1]/dimension[" + cnt + "]/@power", power);
				cnt++;
			}
			
			map.put(xPathRoot + "/unit[1]/@id", unit);
			map.put(xPathRoot + "/unit[1]/@name", unit);
			map.put(xPathRoot + "/unit[1]/description", desc);
			
			String multiplier = this.newUnitFactorField.getText();
			String SIUnit = (String)this.newSIunitNameBox.getSelectedItem();
			if(multiplier.trim().equals("")) multiplier = "";
			
			map.put(xPathRoot + "/unit[1]/@unitType", type);
			map.put(xPathRoot + "/unit[1]/@parentSI", SIUnit);
			map.put(xPathRoot + "/unit[1]/@multiplierToSI", multiplier);
			
		}
		
		return map;
	}
	
	

  /**
   * sets the fields in the UI page using the Map object that contains all
   * the key/value paired
   *
   * @param data the Map object that contains all the key/value paired settings
   *   for this particular UI page
   * @param rootXPath the String that represents the "root" of the XPath to the
   *   content of this widget, INCLUDING PREDICATES. example - if this is a
   *   "Party" widget, being used for the second "Creator" entry in a list,
   *   then xPathRoot = "/eml:eml/dataset[1]/creator[2]
   * @return boolean true if this page can handle all the data passed in the
   * OrderedMap, false if not. <em>NOTE that the setPageData() method should
   * still complete its work and fill out all the UI values, even if it is
   * returning false</em>
   */
  public boolean setPageData(OrderedMap data, String rootXPath) {
		
		// not required for this page
		return true;
	}
	
}
