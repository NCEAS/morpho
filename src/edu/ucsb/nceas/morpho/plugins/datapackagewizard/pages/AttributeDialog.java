/**
 *  '$RCSfile: AttributeDialog.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-17 00:35:44 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.DialogSubPanelAPI;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.utilities.OrderedMap;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;

import java.util.List;
import java.util.ArrayList;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Component;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;



class AttributeDialog extends WizardPopupDialog {

  public static final int BORDERED_PANEL_TOT_ROWS = 9;
  public static final int DOMAIN_NUM_ROWS = 8;
  
  private JTextField attribNameField;
  
  private JLabel kwLabel;
  private JLabel attribNameLabel;
  private JLabel attribDefinitionLabel;
  private JLabel measScaleLabel;
  private JPanel currentPanel;
//  private JPanel currentNominalSubPanel;
//  private JPanel currentOrdinalSubPanel;
//  private JPanel nominalTextSubPanel;
//  private JPanel nominalEnumSubPanel;
//  private JPanel ordinalTextSubPanel;
//  private JPanel ordinalEnumSubPanel;
      
  private JPanel nominalPanel;
  private JPanel ordinalPanel;
  private JPanel intervalPanel;
  private JPanel ratioPanel;
  private JPanel dateTimePanel;
  
  
  private String measurementScale;
  private String nominalDomain;

//  private JLabel[]     nomOrdTextDefinitionLabel = new JLabel[2];
//  private JTextField[] nomOrdTextDefinitionField = new JTextField[2];
//  private JTextField[] nomOrdTextSourceField     = new JTextField[2];
//  private CustomList[] nomOrdTextPatternsList    = new CustomList[2];
//  
//  private JLabel[]     nomOrdEnumDefinitionLabel = new JLabel[2];
//  private CustomList[] nomOrdEnumDefinitionList  = new CustomList[2];
//
//  private JCheckBox[] nomOrdEnumDefinitionFreeTextCheckBox = new JCheckBox[2];
  
  private JTextArea attribDefinitionField;
  private final String[] buttonsText  
      = {
          "<html><table width=\"100%\"><tr><td valign=\"top\" width=\"100%\">"
          +"NOMINAL:&nbsp;&nbsp;&nbsp;numbers have been assigned only for "
          +"categorizing a variable. <font color=\"#666666\">"
          +"e.g: assigning 1 for male and 2 for female</font>"
          +"</td></tr></table></html>",
          
          "<html><table width=\"100%\"><tr><td valign=\"top\" width=\"100%\">"
          +"ORDINAL:&nbsp;&nbsp;&nbsp;can determine the order of categories, "
          +"but not magnitude of their differences. <font color=\"#666666\">"
          +"e.g: a ranking system: 1=good, 2=fair, 3=poor.</font>"
          +"</td></tr></table></html>",
          
          "<html><table width=\"100%\"><tr><td valign=\"top\" width=\"100%\">"
          +"INTERVAL:&nbsp;&nbsp;data which consist of equidistant points on a " 
          +"scale. <font color=\"#666666\">e.g: Celsius scale (no "
          +"natural zero point; 20C is not twice as hot as 10C)</font>"
          +"</td></tr></table></html>",
          
          "<html><table width=\"100%\"><tr><td valign=\"top\" width=\"100%\">"
          +"RATIO:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;data which "
          +"has equidistant points <b>and</b> a meaningful zero point. "
          +"<font color=\"#666666\">e.g: length in meters</font>"
          +"</td></tr></table></html>",
          
          "<html><table width=\"100%\"><tr><td valign=\"top\" width=\"100%\">"
          +"DATE-TIME: values that comply with the Gregorian calendar "
          +"system. <font color=\"#666666\">e.g:  2002-10-14T09:13:45</font>"
          +"</td></tr></table></html>"
        };

//  private final String[] textEnumPicklistVals
//                      = { "Enumerated values (belong to a pre-defined list)", 
//                          "Text values (free-form or matching a pattern)"     };
//
//                                        

  private final String[] measScaleElemNames = { "nominal",
                                                "ordinal",
                                                "interval",
                                                "ratio",
                                                "dateTime" };

  // these must correspond to indeces of measScaleElemNames array
  public static final int MEASUREMENTSCALE_NOMINAL = 0;
  public static final int MEASUREMENTSCALE_ORDINAL = 1;

  // 
  public AttributeDialog(JFrame parent) { 
  
    super(parent); 
    
    init();
    this.setVisible(true);
  }
  
  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    
    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    
    middlePanel.add(WidgetFactory.makeHTMLLabel(
              "<font size=\"4\"><b>Define Attribute or Column:</b></font>", 1));
    
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    
    ////
    JPanel attribNamePanel = WidgetFactory.makePanel(1);
    attribNameLabel = WidgetFactory.makeLabel("Attribute name:", true);
    attribNamePanel.add(attribNameLabel);
    attribNameField = WidgetFactory.makeOneLineTextField();
    attribNamePanel.add(attribNameField);
    middlePanel.add(attribNamePanel);
        
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    ////////////////////////////////////////////////////////////////////////////
    
    middlePanel.add(WidgetFactory.makeHTMLLabel(
    "Define the contents of the attribute (or column) precisely, "
    +"so that a data user could interpret the attribute accurately.<br></br>"
    +"<font color=\"666666\">&nbsp;&nbsp;[Example(s):&nbsp;&nbsp;&nbsp;"
    +"\"spden\" is the number of individuals of all macro invertebrate species "
    +"found in the plot]</font>", 2));

    JPanel attribDefinitionPanel = WidgetFactory.makePanel(2);

    attribDefinitionLabel = WidgetFactory.makeLabel("Definition", true);
    attribDefinitionLabel.setVerticalAlignment(SwingConstants.TOP);
    attribDefinitionLabel.setAlignmentY(SwingConstants.TOP);
    attribDefinitionPanel.add(attribDefinitionLabel);
    
    attribDefinitionField = WidgetFactory.makeTextArea("", 3, true);
    JScrollPane jscrl = new JScrollPane(attribDefinitionField);
    attribDefinitionPanel.add(jscrl);
    middlePanel.add(attribDefinitionPanel);
 
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
  
    ////
    ActionListener listener = new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
        
        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        //undo any hilites:
        
        if (e.getActionCommand().equals(buttonsText[0])) {
          
          setMeasurementScaleUI(nominalPanel);
          setMeasurementScale(measScaleElemNames[0]);
                    
        } else if (e.getActionCommand().equals(buttonsText[1])) {
        
          setMeasurementScaleUI(ordinalPanel);
          setMeasurementScale(measScaleElemNames[1]);
                  
          
        } else if (e.getActionCommand().equals(buttonsText[2])) {
        
          setMeasurementScaleUI(intervalPanel);
          setMeasurementScale(measScaleElemNames[2]);
                  
          
        } else if (e.getActionCommand().equals(buttonsText[3])) {
        
          setMeasurementScaleUI(ratioPanel);
          setMeasurementScale(measScaleElemNames[3]);
                  
        } else if (e.getActionCommand().equals(buttonsText[4])) {
        
          setMeasurementScaleUI(dateTimePanel);
          setMeasurementScale(measScaleElemNames[4]);
                    
        }
      }
    };

    measScaleLabel = WidgetFactory.makeLabel(
                                "Select and define a Measurement Scale:", true, 
                                WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
                                
    middlePanel.add(measScaleLabel);
    
    JPanel radioPanel = WidgetFactory.makeRadioPanel(buttonsText, -1, listener);

    middlePanel.add(radioPanel);
    
    currentPanel  = getEmptyPanel();
    
    middlePanel.add(currentPanel);
    
    nominalPanel  = getNomOrdPanel(MEASUREMENTSCALE_NOMINAL);
    ordinalPanel  = getNomOrdPanel(MEASUREMENTSCALE_ORDINAL);
    intervalPanel = getIntervalPanel();
    ratioPanel    = getRatioPanel();
    dateTimePanel = getDateTimePanel();
    
  } 

  private void setMeasurementScale(String scale) {

    this.measurementScale = scale;
  }

  private void setMeasurementScaleUI(JPanel panel) {

    middlePanel.remove(currentPanel);
    currentPanel = panel;
    middlePanel.add(currentPanel);
    middlePanel.validate();
    middlePanel.repaint();
  }
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private JPanel getEmptyPanel() {
    
    JPanel panel = WidgetFactory.makeVerticalPanel(BORDERED_PANEL_TOT_ROWS);
    
    panel.add(WidgetFactory.makeDefaultSpacer());

    return panel;
  }
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  // nom_ord can be MEASUREMENTSCALE_NOMINAL or MEASUREMENTSCALE_ORDINAL
  private NominalOrdinalPanel getNomOrdPanel(int nom_ord) {
  
    return new NominalOrdinalPanel(this, nom_ord);
  }
    

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  private JPanel getIntervalPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(BORDERED_PANEL_TOT_ROWS);
    
    WidgetFactory.addTitledBorder(panel, measScaleElemNames[2]);
    
    panel.add(WidgetFactory.makeDefaultSpacer());
    
//    JComboBox pickList = WidgetFactory.makePickList(pickListVals, false, 1, 
//    
//        new ItemListener() {
//        
//          public void itemStateChanged(ItemEvent e) {
//
//            Log.debug(45, "got PickList state changed; src = "
//                                          +e.getSource().getClass().getName());
//          }
//        });
//    
//    Object[] colTemplates = new Object[] { pickList, new JTextField() };
//
//    String[] colNames = new String[] { 
//      "Fixed-Width or Delimited?", 
//      "Width or Delimiter Character:" 
//    };
//    
//                                    
//    list = WidgetFactory.makeList(colNames, colTemplates, 4,
//                                  true, false, false, true, true, true);
//    
//    panel.add(list);
 
    ////
    return panel;
  }
    
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  private JPanel getRatioPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(BORDERED_PANEL_TOT_ROWS);
    
    WidgetFactory.addTitledBorder(panel, measScaleElemNames[3]);
    
    panel.add(WidgetFactory.makeDefaultSpacer());
    ////
    return panel;
  }
    
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  private JPanel getDateTimePanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(BORDERED_PANEL_TOT_ROWS);
    
    WidgetFactory.addTitledBorder(panel, measScaleElemNames[4]);
    
    panel.add(WidgetFactory.makeDefaultSpacer());
    ////
    return panel;
  }
    
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *


  /**
   *  calls validate() and repaint() on the middle panel
   */
  public void refreshUI() {
  
    middlePanel.validate();
    middlePanel.repaint();
  }
  
  

  /** 
   *  The action to be executed when the "OK" button is pressed. If no onAdvance 
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false 
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    if (attribNameField.getText().trim().equals("")) {
  
      WidgetFactory.hiliteComponent(attribNameLabel);
      attribNameField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(attribNameLabel);
    
    if (attribDefinitionField.getText().trim().equals("")) {
  
      WidgetFactory.hiliteComponent(attribDefinitionLabel);
      attribDefinitionField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(attribDefinitionLabel);
    
    if (measurementScale==null) {
  
      WidgetFactory.hiliteComponent(measScaleLabel);
      return false;
    }
    WidgetFactory.unhiliteComponent(measScaleLabel);
    
    return ((DialogSubPanelAPI)currentPanel).validateUserInput();
  }


  
  /**
   *  @return a List contaiing 2 String elements - one for each column of the 
   *  2-col list in which this surrogate is displayed
   *
   */
  public List getSurrogate() {

    WidgetFactory.unhiliteComponent(attribDefinitionLabel);

    List surrogate = new ArrayList();

    //attribName (first column) surrogate:
    String attribName   = attribNameField.getText().trim();
    if (attribName==null) attribName = "";
    surrogate.add(attribName);


    //attribDefinition (second column) surrogate:
    String attribDefinition   = attribDefinitionField.getText().trim();
    if (attribDefinition==null) attribDefinition = "";
    surrogate.add(attribDefinition);


    //measurementScale (third column) surrogate:
    if (measurementScale==null) measurementScale = "";
    surrogate.add(measurementScale);

    return surrogate;
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
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap   returnMap     = new OrderedMap();
//  private StringBuffer measScaleBuff = new StringBuffer();
  ////////////////////////////////////////////////////////
  public OrderedMap getPageData(String xPathRoot) {

    returnMap.clear();

    String attribName = attribNameField.getText().trim();
    if (attribName!=null && !attribName.equals("")) {
      returnMap.put(xPathRoot + "/attributeName", attribName);
    }


    String attribDef = attribDefinitionField.getText().trim();
    if (attribDef!=null && !attribDef.equals("")) {
      returnMap.put(xPathRoot + "/attributeDefinition", attribDef);
    }

    if (measurementScale!=null && !measurementScale.equals("")) {
    
      returnMap.putAll(
                    ((DialogSubPanelAPI)currentPanel).getPanelData(xPathRoot) );
    
    
//      measScaleBuff.delete(0, measScaleBuff.length);
//      if (measurementScale.equals(measScaleElemNames[0])) {
//      
//        //nominal:
//        
//      } else if (measurementScale.equals(measScaleElemNames[1])){
//        
//        //ordinal:
//        
//      } else if (measurementScale.equals(measScaleElemNames[2])){
//        
//        //interval:
//      
//        
//      } else if (measurementScale.equals(measScaleElemNames[3])){
//        
//        //ratio:
//      
//        
//      } else if (measurementScale.equals(measScaleElemNames[4])){
//        
//        //datetime:
//      
//      }
      
//      returnMap.put(xPathRoot + "/"+measurementScale", measurementScale);
    }


    return returnMap;
  }
}