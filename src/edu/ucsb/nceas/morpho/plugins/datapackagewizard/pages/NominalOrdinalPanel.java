/**
 *  '$RCSfile: NominalOrdinalPanel.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-10-01 04:49:01 $'
 * '$Revision: 1.6 $'
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



class NominalOrdinalPanel extends JPanel implements DialogSubPanelAPI {

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
  

  private final int ENUMERATED_DOMAIN = 10;
  private final int TEXT_DOMAIN       = 20;
  
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
  public NominalOrdinalPanel(AttributeDialog attributeDialog) {
  
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
    
    final String TEXT_HELP 
                = "<html>Describe a free text domain for the attribute.</html>";

    final String ENUM_HELP 
                = "<html>Describe any codes that are used as values of "
                +"the attribute.</html>";
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


    JComboBox domainPickList = new JComboBox(textEnumPicklistVals);
    
    domainPickList.setFont(WizardSettings.WIZARD_CONTENT_FONT);
    domainPickList.setForeground(WizardSettings.WIZARD_CONTENT_TEXT_COLOR);
    domainPickList.addItemListener(listener);
    domainPickList.setEditable(false);
    domainPickList.setSelectedIndex(0);

    
    JPanel pickListPanel = WidgetFactory.makePanel();
    chooseLabel = WidgetFactory.makeLabel("CHOOSE:", true);
    pickListPanel.add(chooseLabel);
    pickListPanel.add(domainPickList);
 
    JPanel measScalePanel = new JPanel();
    measScalePanel.setLayout(new GridLayout(1,2));
    measScalePanel.add(pickListPanel);
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
    attributeDialog.refreshUI();
  }

  
  // * * * * 


  private JPanel getTextSubPanel() {
  
    JPanel panel 
            = WidgetFactory.makeVerticalPanel(AttributeDialog.DOMAIN_NUM_ROWS);

    panel.add(WidgetFactory.makeHalfSpacer());
    
    ///////////////////////////

    JPanel topHorizPanel = new JPanel();
    topHorizPanel.setLayout(new GridLayout(1,2));

    JPanel defFieldPanel = WidgetFactory.makePanel();
    textDefinitionLabel = WidgetFactory.makeLabel("Definition:", true);
    defFieldPanel.add(textDefinitionLabel);
    textDefinitionField = WidgetFactory.makeOneLineTextField();
    defFieldPanel.add(textDefinitionField);

    topHorizPanel.add(defFieldPanel);

    topHorizPanel.add(getLabel(
        "<html><font color=\"#666666\">e.g: <i>U.S. telephone numbers in "
        +"the format (999) 888-7777</i></font></html>"));

    panel.add(topHorizPanel);

    panel.add(WidgetFactory.makeHalfSpacer());
    
    ///////////////////////////
        
    JPanel middleHorizPanel = new JPanel();
    middleHorizPanel.setLayout(new GridLayout(1,2));

    JPanel srcFieldPanel = WidgetFactory.makePanel();
    srcFieldPanel.add(WidgetFactory.makeLabel("Source:", false));
    textSourceField = WidgetFactory.makeOneLineTextField();
    srcFieldPanel.add(textSourceField);

    middleHorizPanel.add(srcFieldPanel);

    middleHorizPanel.add(getLabel(
        "<html><font color=\"#666666\">e.g: <i>FIPS standard "
        +"for postal abbreviations for U.S. states</i></font></html>"));

    panel.add(middleHorizPanel);

    panel.add(WidgetFactory.makeHalfSpacer());

    ///////////////////////////
        
    JPanel bottomHorizPanel = new JPanel();
    bottomHorizPanel.setLayout(new GridLayout(1,2));

    Object[] colTemplates = new Object[] { new JTextField() };

    JPanel patternPanel = WidgetFactory.makePanel();
    patternPanel.add(WidgetFactory.makeLabel("Pattern(s)", false));
    String[] colNames = new String[] { "Pattern(s) (optional):" };
    
    textPatternsList 
              = WidgetFactory.makeList( colNames, colTemplates, 2,
                                        true, false, false, true, false, false);
    textPatternsList.setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    patternPanel.add(textPatternsList);
    
    bottomHorizPanel.add(patternPanel);
    
    bottomHorizPanel.add(getLabel(
        "<html><font color=\"#666666\">Patterns "
        +"are interpreted as regular expressions constraining allowable "
        +"character sequences - e.g: <i>'[0-9]{3}-[0-9]{3}-[0-9]{4}' allows for "
        +"only numeric digits in the pattern of a US phone number."
        +"</i></font></html>"));
            
    panel.add(bottomHorizPanel);

    return panel;
  }
    
  // * * * * 
 
  private JPanel getEnumSubPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(AttributeDialog.DOMAIN_NUM_ROWS);

    ///////////////////////////
    panel.add(WidgetFactory.makeHalfSpacer());

    Object[] colTemplates 
        = new Object[] { new JTextField(), new JTextField(), new JTextField() };

    String[] colNames 
                  = new String[] { "Code", "Definition", "Source (optional)" };

    JPanel enumPanel = WidgetFactory.makePanel();
    enumDefinitionLabel = WidgetFactory.makeLabel("Definitions", true);
    enumPanel.add(enumDefinitionLabel);
    
    enumDefinitionList 
              = WidgetFactory.makeList( colNames, colTemplates, 2,
                                        true, false, false, true, false, false);
                                        
    enumDefinitionList.setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    enumPanel.add(enumDefinitionList);
    
    panel.add(enumPanel);
    
    ///////////////////////////

    JPanel helpPanel = new JPanel();
    helpPanel.setLayout(new GridLayout(1,5));
    
    helpPanel.add(this.getLabel(
        "<html><font color=\"#666666\">Examples:</font></html>"));
        
    helpPanel.add(this.getLabel(
        "<html><center><font color=\"#666666\">CA</font></center></html>"));
        
    helpPanel.add(this.getLabel(
        "<html><center><font color=\"#666666\">California</font></center></html>"));
    
    helpPanel.add(this.getLabel(
        "<html><center><font color=\"#666666\">FIPS U.S. state codes</font></center></html>"));

    helpPanel.add(this.getLabel(" "));

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
  private OrderedMap validationNVP = new OrderedMap();
  //
  public boolean validateUserInput() {


    if (currentSubPanel==enumSubPanel) {  //ENUMERATED
    
        validationNVP.clear();
        getEnumListData("", validationNVP);
      
        if (validationNVP==null || validationNVP.size()<1) {
          WidgetFactory.hiliteComponent(enumDefinitionLabel);
          return false;
        }

    } else {    ////////////////////////////TEXT

        if (textDefinitionField.getText().trim().equals("")) {

          WidgetFactory.hiliteComponent(textDefinitionLabel);
          textDefinitionField.requestFocus();
          
          return false;
        }
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
    
        // CHECK FOR AND ELIMINATE EMPTY ROWS...
        Object nextRowObj = it.next();
        if (nextRowObj==null) continue;
        
        List nextRow = (List)nextRowObj;
        if (nextRow.size() < 1) continue;
        
        if (nextRow.get(0)==null) continue;
        nextStr = (String)(nextRow.get(0));
        if (nextStr.trim().equals("")) continue;
        
        nomOrdBuff.delete(0, nomOrdBuff.length());
        nomOrdBuff.append(xPathRoot);
        nomOrdBuff.append("textDomain[1]/pattern[");
        nomOrdBuff.append(index++);
        nomOrdBuff.append("]");
                        
        returnMap.put(nomOrdBuff.toString(), nextStr);
      }

      String source = textSourceField.getText().trim();
      if (!source.equals("")) {
        returnMap.put(  xPathRoot + "textDomain[1]/source", source);
      }
    }
    return returnMap;
  }
  
  
  
  // xpathRoot is up to 'enumeratedDomain' (NOT including the slash after)
  private void getEnumListData(String xpathRoot, OrderedMap resultsMap) {
    
    int index=1;
    StringBuffer buff = new StringBuffer();
    List rowLists = enumDefinitionList.getListOfRowLists();
    Object srcObj = null;
    String srcStr = null;
  
    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
  
      // CHECK FOR AND ELIMINATE EMPTY ROWS...
      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;
      
      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;
      
      if (nextRow.get(0)==null || nextRow.get(1)==null) continue;
      
      buff.delete(0,buff.length());
      buff.append(xpathRoot);
      buff.append("/codeDefinition[");
      buff.append(index++);
      buff.append("]/");
      resultsMap.put( buff.toString() + "code",       
                      ((String)(nextRow.get(0))).trim());
                      
      resultsMap.put( buff.toString() + "definition", 
                      ((String)(nextRow.get(1))).trim());
                      
      srcObj = nextRow.get(2);
      if (srcObj==null) continue;
      srcStr = ((String)srcObj).trim();
      if (!srcStr.equals("")) resultsMap.put( buff.toString() + "source",
                                                  srcStr);
    }
  }
  
  
}