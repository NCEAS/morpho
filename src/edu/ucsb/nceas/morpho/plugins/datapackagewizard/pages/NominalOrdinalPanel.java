/*  '$RCSfile: NominalOrdinalPanel.java,v $'
*    Purpose: A class that handles xml messages passed by the
*             package wizard
*  Copyright: 2000 Regents of the University of California and the
*             National Center for Ecological Analysis and Synthesis
*    Authors: Chad Berkley
*    Release: @release@
*
*   '$Author: sambasiv $'
*     '$Date: 2003-12-17 03:06:33 $'
* '$Revision: 1.13 $'
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
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Enumeration;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Component;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;



class NominalOrdinalPanel extends JPanel implements WizardPageSubPanelAPI {
	
	
	
	
	
	private JPanel currentSubPanel;
	private JPanel textSubPanel;
	private JPanel enumSubPanel;
	
	private JLabel     textDefinitionLabel;
	private JTextField textDefinitionField;
	private JTextField textSourceField;
	private CustomList textPatternsList;
	
	private JLabel     chooseLabel;
	private JLabel     enumDefinitionLabel;
	private CustomList enumDefinitionList;
	
	private JCheckBox enumDefinitionFreeTextCheckBox;
	
	private final String[] textEnumPicklistVals
	= { "Enumerated values (belong to predefined list)", 
	"Text values (free-form or matching a pattern)"     };
	
	private final String[] nomOrdDisplayNames = { "nominal", "ordinal" };
	
	double[] enumColumnWidthPercentages = new double[] { 25.0, 75.0};
	
	private final int ENUMERATED_DOMAIN = 10;
	private final int TEXT_DOMAIN       = 20;
	
	private final String EMPTY_STRING = "";
	
	private AbstractWizardPage wizardPage;
	
	private JComboBox domainPickList;
	
	// * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	
	
	/**
	*  Constructor
	*
	*  @param page the parent wizard page
	*
	*  @param nom_ord_mode can be AttributePage.MEASUREMENTSCALE_NOMINAL 
	*                  or AttributePage.MEASUREMENTSCALE_ORDINAL
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
		
		this.setPreferredSize(dims);
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
		
		
		domainPickList = new JComboBox(textEnumPicklistVals);
		
		domainPickList.setFont(WizardSettings.WIZARD_CONTENT_FONT);
		domainPickList.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
		domainPickList.addItemListener(listener);
		domainPickList.setEditable(false);
		domainPickList.setSelectedIndex(0);
		 
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
		
		/*JPanel helpTextPanel = WidgetFactory.makePanel();
		helpTextPanel.add(WidgetFactory.makeDefaultSpacer());
		helpTextPanel.add(helpTextLabel);*/
		
		measScalePanel.add(helpTextLabel);
		
		this.add(measScalePanel);
		
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
		+WizardSettings.HTML_EXAMPLE_FONT_OPENING
		+"Patterns "
		+"are interpreted as regular expressions constraining allowable "
		+"character sequences - e.g: <i>'[0-9]{3}-[0-9]{3}-[0-9]{4}' allows "
		+"only numeric digits in the pattern of US phone numbers"
		+"</i>"+WizardSettings.HTML_EXAMPLE_FONT_CLOSING
		+WizardSettings.HTML_NO_TABLE_CLOSING));
		
		panel.add(bottomHorizPanel);
		
		return panel;
	}
	
	// * * * * 
	
	private JPanel getEnumSubPanel() {
		
		JPanel panel = WidgetFactory.makeVerticalPanel(AttributePage.DOMAIN_NUM_ROWS);
		
		///////////////////////////
		panel.add(WidgetFactory.makeHalfSpacer());
		
		Object[] colTemplates 
		= new Object[] { new JTextField(), new JTextField()};
		
		String[] colNames 
		= new String[] { "Code", "Definition" };
		
		JPanel enumPanel = WidgetFactory.makePanel();
		enumDefinitionLabel = WidgetFactory.makeLabel("Definitions:", true, WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
		enumPanel.add(enumDefinitionLabel);
		
		enumDefinitionList 
		= WidgetFactory.makeList( colNames, colTemplates, 2,
		true, false, false, true, false, false);
		
		enumDefinitionList.setColumnWidthPercentages(enumColumnWidthPercentages);
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
		panel.add(helpPanel);
		
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
			
			if (!isEnumListDataValid()) {
				WidgetFactory.hiliteComponent(enumDefinitionLabel);
				return false;
			}
			
		} else {    ////////////////////////////TEXT
			
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
		WidgetFactory.unhiliteComponent(enumDefinitionLabel);
		WidgetFactory.unhiliteComponent(textDefinitionLabel);
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
			
			getEnumListData(xPathRoot + "enumeratedDomain[1]", returnMap);
			
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
			
			/*srcObj = nextRow.get(2);
			if (srcObj==null) continue;
			srcStr = ((String)srcObj).trim();
			if (!srcStr.equals(EMPTY_STRING)) resultsMap.put( buff.toString() + "source",
				srcStr);
			*/
		}
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
		
		
		setEnumListData(xPathRoot + "/enumeratedDomain[1]", map);
		
		String freeText = (String)map.get(xPathRoot + "/textDomain[1]/definition");
		if(freeText != null && freeText.equals("Free text (unrestricted)"))
			enumDefinitionFreeTextCheckBox.setSelected(true);
		else
			enumDefinitionFreeTextCheckBox.setSelected(false);
		
		String defn = (String)map.get(xPathRoot + "/textDomain[1]/definition");
		if(defn != null)
			textDefinitionField.setText(defn);
		setTextListData(xPathRoot + "/textDomain[1]", map);
		String source = (String)map.get(xPathRoot + "/textDomain[1]/source");
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
			Log.debug(15,"Peru: in setEnumListData, for path="+xPathRoot+"/codeDefinition[" +index+ "]/code - got=" + code);
			if(code == null)
				break;
			row.add(code);
			String defn = (String)map.get(xPathRoot+"/codeDefinition[" +index+ "]/definition");
			row.add(defn);
			String src = (String)map.get(xPathRoot+"/codeDefinition[" +index+ "]/source");
			row.add(src);
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
}