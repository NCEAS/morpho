/*  '$RCSfile: NominalOrdinalPanel.java,v $'
*    Purpose: A class that handles xml messages passed by the
*             package wizard
*  Copyright: 2000 Regents of the University of California and the
*             National Center for Ecological Analysis and Synthesis
*    Authors: Chad Berkley
*    Release: @release@
*
*   '$Author: sambasiv $'
*     '$Date: 2004-02-04 02:25:51 $'
* '$Revision: 1.17 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageSubPanelAPI;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;

import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.AddDocumentationCommand;
import edu.ucsb.nceas.morpho.datapackage.DataViewContainerPanel;
import edu.ucsb.nceas.morpho.Morpho;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Base64;


class NominalOrdinalPanel extends JPanel implements WizardPageSubPanelAPI {
	
	
	
	
	
	private JPanel currentSubPanel;
	private JPanel textSubPanel;
	private JPanel enumSubPanel;
	// the panel that contains the code definition customlist
	private JPanel enumPanel;
	
	private JLabel     textDefinitionLabel;
	private JTextField textDefinitionField;
	private JTextField textSourceField;
	private CustomList textPatternsList;
	
	private JLabel     chooseLabel;
	private JLabel     enumDefinitionLabel;
	private CustomList enumDefinitionList;
	private CustomList importedDefinitionList;
	
	private JLabel			codeLocationLabel;
	private JComboBox		codeLocationPickList;
	private final String[] codeLocationPicklistVals
	= { "Codes are defined here",
	"Codes are imported from another table"     };
	
	private JPanel tablePanel;
	private JLabel tableNameLabel;
	private JTextField tableNameTextField;
	private JButton tableNameButton;
	private LocateAction locateAction;
	private CodeImportPanel codeImportPanel = null;
	
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
	
	private AbstractWizardPage wizardPage;
	
	private JComboBox domainPickList;
	
	
	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	
	
	/**
	* Constructor
	*
	* @param page the parent wizard page
	*/
	public NominalOrdinalPanel(AbstractWizardPage page) {
		
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
	*            required
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
		
		nomOrdBuff.delete(0, nomOrdBuff.length());
		
		nomOrdBuff.append(xPathRoot);
		nomOrdBuff.append("/nonNumericDomain/");
		
		xPathRoot = nomOrdBuff.toString();
		
		if (currentSubPanel==enumSubPanel) {  //ENUMERATED
			
			if(codeLocationValue == CODES_DEFINED_HERE)
				getEnumListData(xPathRoot + "enumeratedDomain[1]", returnMap);
				else {
					
					OrderedMap importMap = codeImportPanel.getPageData(xPathRoot +
					"enumeratedDomain[1]/entityCodeList");
					returnMap.putAll(importMap);
				}
				
				
				if (enumDefinitionFreeTextCheckBox.isSelected()) {
					
					returnMap.put(  xPathRoot + "textDomain[1]/definition",
					"Free text (unrestricted)");
					returnMap.put(xPathRoot + "textDomain[1]/pattern[1]", ".*");
				}
				
				
		} else {                              //TEXT
			
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
		if(codeImportPanel.getTableName() == null)
			return true;
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
			CustomList.EMPTY_STRING_TRIM,
		CustomList.IGNORE  } );
		
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
	*  sets the Data in the NominalOrdinal Panel. This is called by the setData() function
	*  of AttributePage.
	*
	*  @param  xPathRoot - this is the relative xPath of the current attribute
	*
	*  @param  map - Data is passed as OrderedMap of xPath-value pairs. xPaths in this map
	*		    are absolute xPath and not the relative xPaths
	*
	**/
	
	public void setPanelData(String xPathRoot, OrderedMap map) {
		
		// check for taxonomic lookups first
		boolean b1 = map.containsKey(xPathRoot + "/enumeratedDomain[1]/entityCodeList/entityReference");
		if(!b1)
			b1 = map.containsKey(xPathRoot + "/enumeratedDomain/entityCodeList/entityReference");
		
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
			
		} else { // codes are defined here
			
			setEnumListData(xPathRoot + "/enumeratedDomain[1]", map);
			setEnumListData(xPathRoot + "/enumeratedDomain", map);
			tablePanel.setVisible(false);
			if(codeLocationValue == CODES_IMPORTED) {
				enumPanel.remove(importedDefinitionList);
				enumPanel.add(enumDefinitionList);
				enumPanel.invalidate();
			}
			codeLocationValue = CODES_DEFINED_HERE;
			codeLocationPickList.setSelectedItem(codeLocationPicklistVals[0]);
		}
		
		String freeText = (String)map.get(xPathRoot + "/textDomain[1]/definition");
		if(freeText == null)
			freeText = (String)map.get(xPathRoot + "/textDomain/definition");
		if(freeText != null && freeText.equals("Free text (unrestricted)"))
			enumDefinitionFreeTextCheckBox.setSelected(true);
		else
			enumDefinitionFreeTextCheckBox.setSelected(false);
		
		String defn = (String)map.get(xPathRoot + "/textDomain[1]/definition");
		if(defn == null)
			defn = (String)map.get(xPathRoot + "/textDomain/definition");
			if(defn != null) {
				textDefinitionField.setText(defn);
				domainPickList.setSelectedItem(this.textEnumPicklistVals[1]);
			}
			setTextListData(xPathRoot + "/textDomain[1]", map);
			setTextListData(xPathRoot + "/textDomain", map);
			String source = (String)map.get(xPathRoot + "/textDomain[1]/source");
			if(source == null)
				source = (String)map.get(xPathRoot + "/textDomain/source");
			if(source != null)
				textSourceField.setText(source);
			return;
	}
	
	/**
	*	This function is used to set the data in the enumeration customlist.
	*	This is called from the setPanelData() function
	*  	@param  xPathRoot - this is the relative xPath of the current attribute
	*
	* 	@param  map - Data is passed as OrderedMap of xPath-value pairs. xPaths in this map
	*		    are absolute xPath and not the relative xPaths
	*/
	
	private void setEnumListData(String xPathRoot, OrderedMap map) {
		int index = 1;
		while(true) {
			List row = new ArrayList();
			String code = (String)map.get(xPathRoot+"/codeDefinition[" +index+ "]/code");
			
			if(index == 1 && code == null)
				code = (String)map.get(xPathRoot+"/codeDefinition/code");
			
			if(code == null)
				break;
			row.add(code);
			String defn = (String)map.get(xPathRoot+"/codeDefinition[" +index+ "]/definition");
			if(index == 1 && defn == null)
				defn = (String)map.get(xPathRoot+"/codeDefinition/definition");
			row.add(defn);
			enumDefinitionList.addRow(row);
			index++;
		}
		return;
	}
	
	private void setTextListData(String xPathRoot, OrderedMap map) {
		int index = 1;
		while(true) {
			List row = new ArrayList();
			String pattern = (String)map.get(xPathRoot+"/pattern[" +index+ "]");
			if(index == 1 && pattern == null)
				pattern = (String)map.get(xPathRoot+"/pattern");
			
			if(pattern == null)
				break;
			row.add(pattern);
			textPatternsList.addRow(row);
			index++;
		}
		return;
	}
	
	
	
	/** This function is to retrieve the Pattern definitions from the CustomList
	*
	*   @return - the List of Rows of Pattern. Each row is a 1 element list
	*		consisting of the Pattern
	*/
	
	public List getTextList() {
		return this.textPatternsList.getListOfRowLists();
	}
	
	/* Action class for the "Locate Table" button in the import Panel of the
	Enumerated domain.
	*/
	
	class LocateAction extends AbstractAction {
		
		private AbstractWizardPage attributePage;
		private Dimension DIALOG_SIZE = new Dimension(500,450);
		private JDialog importDialog = null;
		private JPanel buttonsPanel = null;
		private final Font  BUTTON_FONT = new Font("Sans-Serif",Font.PLAIN,11);
		private final Color BUTTON_TEXT_COLOR = new Color(51, 51, 51);
		private final Color BG_COLOR = new Color(11,85,112);
		
		
		
		LocateAction(AbstractWizardPage page) {
			this.attributePage = page;
		}
		
		public void actionPerformed(ActionEvent ae) {
			
			if(codeImportPanel == null)
				codeImportPanel = new CodeImportPanel();
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
				importDialog = getContainerDialog(codeImportPanel, okAction, cancelAction);
			}
		
			importDialog.setVisible(true);
			
		}
			
		private void okAction() {
			
			if(codeImportPanel.onAdvanceAction())
				importDialog.setVisible(false);
			else
				return;
			String tableName = codeImportPanel.getTableName();
			if(tableName == null) {
				tableNameTextField.setText(TO_BE_IMPORTED);
				importedDefinitionList.removeAllRows();
			}
			else {
				tableNameTextField.setText(tableName);
				fillCustomList();
			}
			return;
			
		}
		
		private void cancelAction() {
			importDialog.setVisible(false);
		}
		
	
		private JDialog getContainerDialog(JPanel centerPanel, ActionListener okListener, 
						ActionListener cancelListener) 
		{
			
			JDialog dialog = new JDialog();
			Container c = dialog.getContentPane();
			c.setLayout(new BorderLayout());
			
			Point loc = attributePage.getLocationOnScreen();
			int xc = (int)(loc.getX() + attributePage.getWidth()/2 - DIALOG_SIZE.width/2);
			int yc = (int)(loc.getY() + attributePage.getHeight()/2 - DIALOG_SIZE.height/2);
			dialog.setBounds(xc, yc, DIALOG_SIZE.width, DIALOG_SIZE.height);
			dialog.setModal(true);
			dialog.setVisible(false);
			
			buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
			buttonsPanel.add(Box.createHorizontalGlue());
			buttonsPanel.setOpaque(false);
			
			buttonsPanel.setBorder(
			BorderFactory.createMatteBorder(2, 0, 0, 0, BG_COLOR));
			c.add(buttonsPanel, BorderLayout.SOUTH);
			
			JButton okButton  = new JButton("OK");
			okButton.addActionListener(okListener);
			okButton.setForeground(BUTTON_TEXT_COLOR);
			okButton.setFont(BUTTON_FONT);
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(cancelListener);
			cancelButton.setForeground(BUTTON_TEXT_COLOR);
			cancelButton.setFont(BUTTON_FONT);
			
			buttonsPanel.add(okButton);
			buttonsPanel.add(Box.createHorizontalStrut(WizardSettings.PADDING));
			buttonsPanel.add(cancelButton);
			buttonsPanel.add(Box.createHorizontalStrut(WizardSettings.PADDING));
			
			c.add(centerPanel, BorderLayout.CENTER);
			return dialog;
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
		*	Function to set the page data from the ordered map. It creates a 
		*		codeImport page if necessary and sets the data in that page
		* @param  xPathRoot - this is the relative xPath of the current 
		*										attribute
		* @param  map - Data is passed as OrderedMap of xPath-value pairs. 
		*								xPaths in this map are absolute xPath and not the 
		*								relative xPaths
		*/
		
		public void setPageData(String xPath, OrderedMap map) {
			
			if(codeImportPanel == null)
				codeImportPanel = new CodeImportPanel();
			codeImportPanel.setPageData(xPath, map);
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


/* 
This a page used for importing codes from another table. It gives the user
the option of importing it later or identifing the table and columns of an
already present table.
If the user chooses to import it later, the returned OrdereMap contains
null values for the entityCodeList/entityReference. The presence of this
key in the OrderedMap with a null value indicates that we need to import the 
data table at the end of the current operation. Thus, whenever an
AttributePage is used anywhere in Morpho, we need to check if some data
table has to be imported after that.
*/

class CodeImportPanel extends JPanel {
	
	/*private final String pageID     = DataPackageWizardInterface.CODE_IMPORT_PAGE;
	private final String nextPageID = "";
	private final String pageNumber = ""; */
	private final String title      = "Import Codes and Definitions";
	private final String subtitle   = "";
	
	public short USER_RESPONSE;
	public static final short OK_OPTION      = 10;
	public static final short CANCEL_OPTION  = 20;
	
	
	private String[] importChoiceText = {"Import the definitions table into Morpho later",
	"The definitions table has already been included in this package"};
	
	private JLabel choiceLabel;
	private JPanel radioPanel;
	private JPanel definitionsPanel;
	
	
	private JPanel tableNamePanel;
	private JPanel namePanel;
	private JLabel nameLabel;
	private JComboBox namePickList;
	
	private JPanel tableCodePanel;
	private JPanel codePanel;
	private JLabel codeLabel;
	private JComboBox codePickList;
	
	private JPanel tableDefnPanel;
	private JPanel defnPanel;
	private JLabel defnLabel;
	private JComboBox defnPickList;
	
	private JPanel buttonsPanel;
	private JButton okButton;
	private JButton cancelButton;
	
	private String[] entityNames = null;
	private String[] attrNames = null;
	
	private short selectedImportChoice = 0;
	
	private static final int IMPORT_LATER = 1;
	private static final int IMPORT_DONE  = 2;
	private static final String ID_XPATH = "attribute/@id";
	private static final int MAX_IMPORTED_ROWS_DISPLAYED = 10;
	private static final String TRUNCATE_STRING = "--other codes not displayed--";
	
	private int currentEntityIndexSelected = -1;
	
	private int entityIdx = -1;
	private String currentEntityID = null;
	private String codeAttributeID = null;
	private String defnAttributeID = null;
	
	private static Node[] attributeArray = null;
	
	// AbstractDataPackage of the current package
	private AbstractDataPackage adp = null;
	// DataViewContainerPanel of current package 
	private DataViewContainerPanel resultPane = null;
	
	private ItemListener namePickListListener;
	
	// flag to indicate if the panel is to only allow the user to define codes,
	// without giving the option of importing it later.
	
	private boolean onlyDefnPanel = false;
	
	CodeImportPanel(){
		
		onlyDefnPanel = false;
		init();
	}
	
	CodeImportPanel(boolean onlyDefinitionsPanel) {
		
		onlyDefnPanel = onlyDefinitionsPanel;
		if(onlyDefnPanel)
			selectedImportChoice = IMPORT_DONE;
		init();
	}
	
	private void init() {
		
		// gets the Abstract Data Package and sets it to the member variable 'adp'
		getADP();
		
		setLayout(new BorderLayout());
		
		add(WidgetFactory.makeDefaultSpacer());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		
		choiceLabel = WidgetFactory.makeHTMLLabel("Select one of the following",1,true);
		topPanel.add(choiceLabel);
		add(WidgetFactory.makeDefaultSpacer());
		
		ActionListener listener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				Log.debug(45, "got radiobutton command: "+e.getActionCommand());
				
				if (e.getActionCommand().equals(importChoiceText[0])) {
					selectedImportChoice = IMPORT_LATER;
					definitionsPanel.setVisible(false);
				} else if (e.getActionCommand().equals(importChoiceText[1])) {
					selectedImportChoice = IMPORT_DONE;
					definitionsPanel.setVisible(true);
				}
			}
		};
		
		radioPanel = WidgetFactory.makeRadioPanel(importChoiceText, -1, listener);
		
		topPanel.add(radioPanel);
		
		if(onlyDefnPanel == false)
			add(topPanel, BorderLayout.NORTH);
		
		
		definitionsPanel = getDefinitionsPanel();
		add(definitionsPanel, BorderLayout.CENTER);
		if(onlyDefnPanel == false)
			definitionsPanel.setVisible(false);
		
	}
	
	
	
	private JPanel getDefinitionsPanel() {
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3,1,0, 5));
		
		entityNames = getEntityNames();
		attrNames = new String[] {};
		
		JPanel tableNamePanel = new JPanel(new GridLayout(1,2, 3, 0));
		
		namePickListListener = new ItemListener() {
			
			public void itemStateChanged(ItemEvent e) {
				
				int index = ((JComboBox)e.getSource()).getSelectedIndex();
				String name = (String)((JComboBox)e.getSource()).getSelectedItem();
				System.out.println("PickList state changed: " + name);
				if(index == currentEntityIndexSelected)
					return;
				currentEntityIndexSelected = index;
				Log.debug(45, "PickList state changed: " +
				(String)((JComboBox)e.getSource()).getItemAt(index));
				
				
				if (index == 0) { 
					codePickList.setEnabled(false);
					defnPickList.setEnabled(false);
				} else {
					if(adp == null)
						getADP();
					if(adp == null) {
						
						Log.debug(15, "Unable to obtain the AbstractDataPackage in the CodeImportPanel");
						return;
					}
					entityIdx = adp.getEntityIndex(name);
					attrNames = getAttributeNames(entityIdx);
					currentEntityID = getEntityID(entityIdx);
					if(currentEntityID == null || currentEntityID.trim().length() ==0) {
						String newId = WizardSettings.getUniqueID();
						adp.setEntityID(entityIdx, newId);
						currentEntityID = newId;
						Log.debug(15, "Entity does not have an ID ! Assigning it a new ID of " + newId);
					}
					codePickList.setEnabled(true);
					defnPickList.setEnabled(true);
					codePickList.setModel(new DefaultComboBoxModel(attrNames));
					defnPickList.setModel(new DefaultComboBoxModel(attrNames));
					
				}
			}
		};
		
		namePanel = WidgetFactory.makePanel();
		nameLabel = WidgetFactory.makeLabel("Data table:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		namePanel.add(nameLabel);
		namePickList = WidgetFactory.makePickList( entityNames, false, 0, namePickListListener);
		namePanel.add(namePickList);
		
		tableNamePanel.add(namePanel);
		tableNamePanel.add(getLabel(
		WizardSettings.HTML_NO_TABLE_OPENING
		+"Choose the data table that contains the codes and their definition"
		+WizardSettings.HTML_NO_TABLE_CLOSING));
		panel.add(tableNamePanel);
		
		JPanel tableCodePanel = new JPanel(new GridLayout(1,2, 3, 0));
		
		codePanel = WidgetFactory.makePanel();
		codeLabel = WidgetFactory.makeLabel("Codes:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		codePanel.add(codeLabel);
		codePickList = WidgetFactory.makePickList( attrNames, false, 0, null);
		codePanel.add(codePickList);
		
		tableCodePanel.add(codePanel);
		tableCodePanel.add(getLabel(
		WizardSettings.HTML_NO_TABLE_OPENING
		+"Choose the column in the data table that contains the codes"
		+WizardSettings.HTML_NO_TABLE_CLOSING));
		panel.add(tableCodePanel);
		
		JPanel tableDefnPanel = new JPanel(new GridLayout(1,2, 3, 0));
		
		defnPanel = WidgetFactory.makePanel();
		defnLabel = WidgetFactory.makeLabel("Definitions:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		defnPanel.add(defnLabel);
		defnPickList = WidgetFactory.makePickList( attrNames, false, 0, null);
		defnPanel.add(defnPickList);
		
		tableDefnPanel.add(defnPanel);
		tableDefnPanel.add(getLabel(
		WizardSettings.HTML_NO_TABLE_OPENING
		+"Choose the column in the data table that contains the definitions for the codes"
		+WizardSettings.HTML_NO_TABLE_CLOSING));
		panel.add(tableDefnPanel);
		
		codePickList.setEnabled(false);
		defnPickList.setEnabled(false);
		
		return panel;
	}
	
	private void getADP() {
		
		MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
		if (morphoFrame != null) {
			resultPane = AddDocumentationCommand.
			getDataViewContainerPanelFromMorphoFrame(morphoFrame);
		}//if
		// make sure resulPanel is not null
		if ( resultPane != null) {
			adp = resultPane.getAbstractDataPackage();
		}
	}
	
	private String[] getEntityNames() {
		
		if(entityNames != null)
			return entityNames;
		
		ArrayList names = new ArrayList();
		
		if(adp == null)
			getADP();
		
		if(adp != null) {
			for(int i = 0; i < adp.getEntityCount(); i++) {
				names.add(adp.getEntityName(i));
			}
		} else {
			Log.debug(15, "Error - Unable to get the AbstractDataPackage in CodeImportPanel. ");
		}
		
		int cnt = names.size();
		String[] entNames = new String[cnt + 1];
		entNames[0] = "--Select data table--";
		int i = 1;
		for(Iterator it = names.iterator(); it.hasNext();)
			entNames[i++] = (String)it.next(); 
		
		return entNames;
	}
	
	/** 
	*	Function to retrieve the selected table name from where the enumerated 
	*	codes are imported. If the user chooses the option of importing the data
	*	table later, this function returns null.   
	*/
	
	public String getTableName() {
		if(selectedImportChoice == IMPORT_LATER || entityNames == null)
			return null;
		if(namePickList.getSelectedIndex() == 0)
			return null;
		return (String)namePickList.getSelectedItem();
	}
	
	public void setTableName(String name) {
		entityNames = new String[2];
		entityNames[0] = "--Select data table--";
		entityNames[1] = name;
		namePickList.setModel(new DefaultComboBoxModel(entityNames));
		//if(namePickListListener != null)
			//namePickList.removeItemListener(namePickListListener);
		
		/*if(adp != null) {
			int idx = adp.getEntityIndex(name);
			if(idx == -1) {
				Log.debug(10, "Could not get Entity index for the new table in the CodeImportPanel. Error setting the reference IDs");
				return;
			}
			this.currentEntityID = adp.getEntityID(idx);
		} else {
			Log.debug(10, "Abstract Data Package is null in the CodeImportPanel. Error setting the reference IDs");
		}*/
	}
	
	public void setAttributes(List attr) {
		
		int cnt = attr.size();
		attrNames = new String[cnt + 1];
		attrNames[0] = "--Select A Column--";
		int i = 1;
		for(Iterator it = attr.iterator(); it.hasNext() && i < (cnt+1) ; )
			attrNames[i++] =  (String) it.next();
		
		codePickList.setModel(new DefaultComboBoxModel(attrNames));
		defnPickList.setModel(new DefaultComboBoxModel(attrNames));
		
		//codePickList.setEnabled(true);
		//defnPickList.setEnabled(true);
	}
	
	/*
	*	Function to retrieve the ID of the selected table from where the enumerated 
	*	codes are imported.    
	*/
	
	private String getEntityID(int entityIndex) {
		String id = "";
		id = adp.getEntityID(entityIndex);
		Log.debug(45, "Entity ID for entityIndex = " + entityIndex + " is " + id);
		return id;
	}
	
	/** 
	*	Function to retrieve the selected entity Index   
	*/
	
	public int getSelectedEntityIndex() {
		return entityIdx;
	}
	
	
	private String[] getAttributeNames(int entityIndex) {
		ArrayList names = new ArrayList();
		if(adp != null) {
			int num = adp.getAttributeCountForAnEntity(entityIndex);
			for(int i = 0; i < num; i++) {
				names.add(adp.getAttributeName(entityIndex, i));
			}
		}
		int cnt = names.size();
		String[] attrs = new String[cnt+1];
		attrs[0] = "--Select A Column--";
		int i = 1;
		for(Iterator it = names.iterator(); it.hasNext();)
			attrs[i++] = (String)it.next(); 
		
		return attrs;
	}
	
	
	
	/**
	*  gets the unique ID for this wizard page
	*
	*  @return   the unique ID String for this wizard page
	*/
	/*
	public String getPageID() {
		return "";//this.pageID;
	}*/
	
	/**
	*  gets the title for this wizard page
	*
	*  @return   the String title for this wizard page
	*/
	public String getTitle() {
		return this.title;
	}
	
	
	/**
	*  gets the subtitle for this wizard page
	*
	*  @return   the String subtitle for this wizard page
	*/
	public String getSubtitle() {
		return this.subtitle;
	}
	
	/**
	*  Returns the ID of the page that the user will see next, after the "Next"
	*  button is pressed. If this is the last page, return value must be null
	*
	*  @return the String ID of the page that the user will see next, or null if
	*  this is te last page
	*/
	/* 
	public String getNextPageID() {
		return this.nextPageID;
	}*/
	
	/**
	*  Returns the serial number of the page
	*
	*  @return the serial number of the page
	*/
	/*
	public String getPageNumber() {
		return this.pageNumber;
	} */
	
	
	/**
	*  The action to be executed when the page is displayed. May be empty
	*/
	/*
	public void onLoadAction() {
	} */
	
	
	/**
	*  The action to be executed when the "Prev" button is pressed. May be empty
	*
	*/
	/*
	public void onRewindAction() {
	} */
	
	/**
	*  The action to be executed when the "Next" button (pages 1 to last-but-one)
	*  or "Finish" button(last page) is pressed. May be empty
	*
	*  @return boolean true if wizard should advance, false if not
	*          (e.g. if a required field hasn't been filled in)
	*/
	public boolean onAdvanceAction() {
		
		if(selectedImportChoice != IMPORT_LATER && selectedImportChoice != IMPORT_DONE) {
			WidgetFactory.hiliteComponent(choiceLabel);
			return false;
		}
		WidgetFactory.unhiliteComponent(choiceLabel);
		
		if(selectedImportChoice == IMPORT_LATER)
			return true;
		
		if(namePickList.getSelectedIndex() < 1) {
			WidgetFactory.hiliteComponent(nameLabel);
			return false;
		}
		WidgetFactory.unhiliteComponent(nameLabel);
		
		if(codePickList.getSelectedIndex() < 1) {
			WidgetFactory.hiliteComponent(codeLabel);
			return false;
		}
		WidgetFactory.unhiliteComponent(codeLabel);
		
		if(defnPickList.getSelectedIndex() < 1) {
			WidgetFactory.hiliteComponent(defnLabel);
			return false;
		}
		WidgetFactory.unhiliteComponent(defnLabel);
		
		int codeIndex = codePickList.getSelectedIndex() - 1;
		int defnIndex = defnPickList.getSelectedIndex() - 1;
		
		if(adp != null) {
			
			codeAttributeID = adp.getAttributeID(entityIdx, codeIndex);
			
			// the attribute has no ID !! This should never happen for data tables
			// created using the new DPW. But if we encounter such a situation, a
			// new ID is assigned and added to the attribute.
			
			if(codeAttributeID.trim() == "") {  
				Log.debug(15, "Attribute " + 
				adp.getAttributeName(entityIdx,	codeIndex) + "has no ID; assigning one now");
				
				codeAttributeID = WizardSettings.getUniqueID();
				assignIDToAttribute(codeIndex, codeAttributeID);
			}
			Log.debug(45, "Code AttributeID = " + codeAttributeID);
			
			defnAttributeID = adp.getAttributeID(entityIdx,	defnPickList.getSelectedIndex() - 1);
			
			// the attribute has no ID !! This should never happen for data tables
			// created using the new DPW. But if we encounter such a situation, a
			// new ID is assigned and added to the attribute.
			if(defnAttributeID.trim() == "") {  
				Log.debug(15, "Attribute " + 
				adp.getAttributeName(entityIdx,	defnIndex) + " has no ID; assigning one now");
				defnAttributeID = WizardSettings.getUniqueID();
				assignIDToAttribute(defnIndex, defnAttributeID);
			}
			Log.debug(45, "Defn AttributeID = " + defnAttributeID);
			
		} else {
			Log.debug(15, "No AbstractDataPackage found! Hence IDs could not be retrieved!");
		}
		return true;
	}
	
	
	private void assignIDToAttribute(int attrIndex, String value) {
		
		if(adp == null)
			Log.debug(15, "Abstract Data Package is null ! Cant assign ID to attribute");
		if(attributeArray == null)
			attributeArray = adp.getAttributeArray(entityIdx);
		
		if(attributeArray[attrIndex] == null) {
			Log.debug(15, " attribute node itself is null; Cant assign ID to it");
			return;
		} else {
			Log.debug(45, "attribute node =" + attributeArray[attrIndex].getNodeName() + ";"
			+ attributeArray[attrIndex].getNodeValue());
		}
		
		NamedNodeMap map = attributeArray[attrIndex].getAttributes();
		Node oldIdNode = map.getNamedItem("id");
		
		if(oldIdNode != null) {
			attributeArray[attrIndex].removeChild(oldIdNode);
			oldIdNode.setNodeValue(value);
			attributeArray[attrIndex].appendChild(oldIdNode);
		} else {
			Log.debug(45, "Attribute element has no 'id' attribute. Adding it.");
			((Element)attributeArray[attrIndex]).setAttribute("id", value);
		}
		adp.setLocation("");
		return;
	}
	
	
	/**
	*  gets the Map object that contains all the key/value paired
	*  settings for this particular wizard page
	*
	*  @return   data the Map object that contains all the
	*            key/value paired settings for this particular wizard page
	*/
	public OrderedMap getPageData() {
		
		return getPageData("");
	}
	
	/**
	*  gets the Map object that contains all the key/value paired
	*  settings for this particular wizard page, given a prefix xPath 
	*
	*	@param xPath the xPath that needs to be prepended to all keys that are
	*								inserted in the map
	*  @return   data the Map object that contains all the
	*            key/value paired settings for this particular wizard page
	*/
	
	public OrderedMap getPageData(String xPath) {
		
		OrderedMap map = new OrderedMap();
		System.out.println("In CIP, values = "+this.currentEntityID + ", "+ this.codeAttributeID + ", " + this.defnAttributeID);
		map.put(xPath + "/entityReference", this.currentEntityID);
		map.put(xPath + "/valueAttributeReference", this.codeAttributeID);
		map.put(xPath + "/definitionAttributeReference", this.defnAttributeID);
		return map;
	}
	
	
	/**
	*  sets the fields in the wizard page using the Map object
	*  that contains all the key/value paired
	*
	*  @param   data the Map object that contains all the
	*            key/value paired settings for this particular wizard page
	*/
	public void setPageData(OrderedMap data) {
		boolean b1 = data.containsKey(AttributeSettings.Nominal_xPath + "/enumeratedDomain/entityCodeList/entityReference");
		
		if(b1) { // check if its Nominal
			
			setPageData(AttributeSettings.Nominal_xPath + "/enumeratedDomain/entityCodeList", data);
			
		} else { // check for Ordinal
			
			b1 = data.containsKey(AttributeSettings.Ordinal_xPath + "/enumeratedDomain/entityCodeList/entityReference");
			
			if(b1) {
				setPageData(AttributeSettings.Ordinal_xPath + "/enumeratedDomain/entityCodeList", data);
			}
			
		}
		return;
	}
	
	/**
	*  sets the fields in the wizard page using the Map object
	*  that contains all the key/value paired and the relative xPath to be used
	*
	*	@param 	xPath	the relative xPath of the keys
	*  @param   data the Map object that contains all the
	*            key/value paired settings for this particular wizard page
	*/
	
	public void setPageData(String xPath, OrderedMap data) {
		
		Object o1 = data.get(xPath + "/entityReference");
		Container c = (Container)(radioPanel.getComponent(1));
		
		if( o1 == null) {
			JRadioButton jrb = (JRadioButton)c.getComponent(0);
			jrb.setSelected(true);
			selectedImportChoice = IMPORT_LATER;
			definitionsPanel.setVisible(false);
			return;
			
		} else {
			
			JRadioButton jrb = (JRadioButton)c.getComponent(1);
			jrb.setSelected(true);
			selectedImportChoice = IMPORT_DONE;
			definitionsPanel.setVisible(true);
		}
		
		currentEntityID = (String)o1;
		
		entityNames = getEntityNames();
		for(int i = 0; i < entityNames.length; i++) {
			String ID = getEntityID(i);
			if(ID.equals(currentEntityID)) {
				namePickList.setSelectedIndex(i + 1);
				
				break;
			}
		}
		
		codeAttributeID = (String)data.get(xPath + "/valueAttributeReference");
		defnAttributeID = (String)data.get(xPath + "/definitionAttributeReference");
		
		boolean codeSelected = false;
		boolean defnSelected = false;
		
		for(int j = 1; j < codePickList.getItemCount(); j++) {
			String attrID = adp.getAttributeID(entityIdx, j -1);
			if(attrID.equals(codeAttributeID)) {
				codePickList.setSelectedIndex(j);
				codeSelected = true;
			}
			if(attrID.equals(defnAttributeID)) {
				defnPickList.setSelectedIndex(j);
				defnSelected = true;
			}
			if(codeSelected  && defnSelected) 
				break;
		}
		return;
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
	*	Function to retrieve the imported data from the columns selected by the 
	*	user. The selected table is read and the necessary two columns are 
	*	extracted and returned as a List of rows. Each row is a list of two elements
	*
	* @return list the list of rows containing only the selected two columns
	*/
	
	public List getColumnData() {
		
		File entityFile = null;
		
		if(adp == null) {
			Log.debug(15, "Abstract Data Package is null. Cannot fill customlist with the imported codes");
			return null;
		}
		
		int entityIndex = entityIdx;
		String inline = adp.getDistributionInlineData(entityIndex, 0,0);
		Morpho morpho = resultPane.getFramework();
		
		if (inline.length()>0) {  // there is inline data
			
			String encMethod = adp.getEncodingMethod(entityIndex, 0);
			if ((encMethod.indexOf("Base64")>-1)||(encMethod.indexOf("base64")>-1)||
			(encMethod.indexOf("Base 64")>-1)||(encMethod.indexOf("base 64")>-1)) {
				// is Base64
				byte[] decodedData = Base64.decode(inline);
				ByteArrayInputStream bais = new ByteArrayInputStream(decodedData);
				InputStreamReader isr = new InputStreamReader(bais);
				FileSystemDataStore fds3 = new FileSystemDataStore(morpho);
				entityFile = fds3.saveTempDataFile(adp.getAccessionNumber(), isr);
			}
			else {
				// is assumed to be text
				FileSystemDataStore fds2 = new FileSystemDataStore(morpho);
				StringReader sr2 = new StringReader(inline);
				entityFile = fds2.saveTempDataFile(adp.getAccessionNumber(), sr2);
			}
		} else if (adp.getDistributionUrl(entityIndex, 0,0).length()>0) {
			
			String urlinfo = adp.getDistributionUrl(entityIndex, 0,0);
			// assumed that urlinfo is of the form 'protocol://systemname/localid/other'
			// protocol is probably 'ecogrid'; system name is 'knb'
			// we just want the local id here
			int indx2 = urlinfo.lastIndexOf("/");
			if(indx2 == -1) {
				Log.debug(15, "Distribution URL is not in the right format! So data couldnt be retrieved");
				return null;
			}
			urlinfo = urlinfo.substring(indx2 +1);
			if (urlinfo.length()==0) {
				Log.debug(15, "Distribution URL is not in the right format! So data couldnt be retrieved");
				return null;
			}
			// we now have the id
			try{
				String loc = adp.getLocation();
				if ((loc.equals(adp.LOCAL))||(loc.equals(adp.BOTH))) {
					FileSystemDataStore fds = new FileSystemDataStore(morpho);
					entityFile = fds.openFile(urlinfo);
				}
				else if (loc.equals(adp.METACAT)) {
					MetacatDataStore mds = new MetacatDataStore(morpho);
					entityFile = mds.openFile(urlinfo);
				}
				else if (loc.equals("")) {  // just created the package; not yet saved!!!
					try{
						// first try looking in the profile temp dir
						ConfigXML profile = morpho.getProfile();
						String separator = profile.get("separator", 0);
						separator = separator.trim();
						FileSystemDataStore fds = new FileSystemDataStore(morpho);
						String temp = new String();
						temp = urlinfo.substring(0, urlinfo.indexOf(separator));
						temp += "/" + urlinfo.substring(urlinfo.indexOf(separator) + 1, urlinfo.length());
						entityFile = fds.openTempFile(temp);
					}
					catch (Exception q1) {
						// oops - now try locally
						try{
							FileSystemDataStore fds = new FileSystemDataStore(morpho);
							entityFile = fds.openFile(urlinfo);
						}
						catch (Exception q2) {
							// now try metacat
							try{
								MetacatDataStore mds = new MetacatDataStore(morpho);
								entityFile = mds.openFile(urlinfo);
							}
							catch (Exception q3) {
								// give up!
								Log.debug(15,"Exception opening datafile after trying all sources!");
								return null;
							}
						}
					}
				}
			}
			catch (Exception q) {
				Log.debug(15,"Exception opening file!");
				q.printStackTrace();
			}
		}
		else if (adp.getDistributionArray(entityIndex, 0)==null) {
			// case where there is no distribution data in the package
			
			Log.debug(5, "The selected entity has NO distribution information!");
			return null;
		}
		
		if(entityFile == null) {
			Log.debug(15, "Unable to get the selected entity's data file!");
			return null;
		}
		
		int codeIndex = codePickList.getSelectedIndex() - 1;
		int defnIndex = defnPickList.getSelectedIndex() - 1;
		String numHeaders = adp.getPhysicalNumberHeaderLines(entityIndex, 0);
		int numHeaderLines = 0;
		try {
			if(numHeaders != null)
				numHeaderLines = Integer.parseInt(numHeaders);
		} catch(Exception e) {
		}
		
		List data = readTwoColumnsFromFile(entityFile, codeIndex, defnIndex, numHeaderLines);
		
		return data;
		
	} // end of getColumns
	
	private List readTwoColumnsFromFile(File file, int firstCol, int secondCol, int numHeaderLines) {
		
		List result = new ArrayList();
		String line;
		int entityIndex = entityIdx;
		String field_delimiter = adp.getPhysicalFieldDelimiter(entityIndex, 0);
		String delimiter = getDelimiterString(field_delimiter);
		boolean ignoreConsequtiveDelimiters = false;
		boolean orderReversed = false;
		if(firstCol > secondCol)
			orderReversed = true;
		String token, oldToken = "";
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(file));
			int linecnt = 0;
			while( (line = br.readLine()) != null) {
				linecnt++;
				if(linecnt <= numHeaderLines) 
					continue;
				if(line.trim().equals(""))
					continue;
				List row = new ArrayList();
				
				if(result.size() >= MAX_IMPORTED_ROWS_DISPLAYED) {
					int space = TRUNCATE_STRING.indexOf(" ");
					row.add(TRUNCATE_STRING.substring(0, space));
					row.add(TRUNCATE_STRING.substring(space + 1));
					result.add(row);
					break;
				}
				
				if (ignoreConsequtiveDelimiters) {
					StringTokenizer st = new StringTokenizer(line, delimiter, false);
					int cnt = -1;
					while( st.hasMoreTokens() ) {
						token = st.nextToken().trim();
						cnt++;
						if(cnt == firstCol) {
							if(orderReversed) {
								row.add(0, token);
								break;
							} else {
								row.add(token);
							}
						} 
						if (cnt == secondCol) {
							row.add(token);
							if(!orderReversed) break;
						}
					} // end of while
					result.add(row);
					continue;
				}
				else { // not consecutive delimiters
					int cnt = -1;
					StringTokenizer st = new StringTokenizer(line, delimiter, true);
					while( st.hasMoreTokens() ) {
						token = st.nextToken().trim();
						if (! (delimiter.indexOf(token) > -1) ) {
							cnt++;
							if(cnt == firstCol) {
								if(orderReversed) {
									row.add(0, token);
									break;
								} else {
									row.add(token);
								}
							} 
							if (cnt == secondCol) {
								row.add(token);
								if(!orderReversed) break;
							}
							
						}
						else {
							if ((delimiter.indexOf(oldToken) > -1) && (delimiter.indexOf(token) > -1)) {
								cnt++;
								if(cnt == firstCol) {
									if(orderReversed) {
										row.add(0, "");
										break;
									} else {
										row.add("");
									}
								} 
								if (cnt == secondCol) {
									row.add("");
									if(!orderReversed) break;
								}
							}
						}
						oldToken = token;
					}
				} // end of else
				
				result.add(row);
			} // end of while
			
		} // end of try bolck
		catch(Exception e) {
			Log.debug(15, "Exception in reading the data File: " + e);
			
		}
		return result;
		
	}// end of function readTwoColumnsFromFile
	
	private String getDelimiterString(String field_delimiter) {
		String str = "";
		String temp = field_delimiter.trim();
		if (temp.startsWith("#x")) {
			temp = temp.substring(2);
			if (temp.equals("0A")) str = "\n";
			if (temp.equals("09")) str = "\t";
			if (temp.equals("20")) str = " ";
		}
		else {
			str = temp;
		}
		return str;
	}
	
}

