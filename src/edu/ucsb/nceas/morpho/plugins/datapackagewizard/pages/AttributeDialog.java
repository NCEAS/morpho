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
 *     '$Date: 2003-09-11 17:58:13 $'
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


import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.utilities.OrderedMap;

import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import java.util.List;
import java.util.ArrayList;

import java.awt.Dimension;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;



class AttributeDialog extends WizardPopupDialog {

  private JTextField attribNameField;
  
  private JLabel kwLabel;
  private JLabel attribNameLabel;
  private JLabel attribDefinitionLabel;
  private JLabel measScaleLabel;
  private JPanel currentPanel;
    
  private JPanel nominalPanel;
  private JPanel ordinalPanel;
  private JPanel intervalPanel;
  private JPanel ratioPanel;
  private JPanel dateTimePanel;
  private JPanel measScalePanel;
  
  private String measurementScale;
  
  private JTextArea attribDefinitionField;
  private final String[] buttonsText  = {
                                          "Nominal",
                                          "Ordinal",
                                          "Interval",
                                          "Ratio",
                                          "DateTime"
                                        };
  private final String[] measScaleElemNames 
                                      = {
                                          "/nominal",
                                          "/ordinal",
                                          "/interval",
                                          "/ratio",
                                          "/dateTime"
                                        };


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
    
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    
    middlePanel.add(WidgetFactory.makeHTMLLabel(
              "<font size=\"4\"><b>Define Column or Attribute:</b></font>", 1));
    
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
    "Enter a paragraph that gives a precise definition of the attribute or "
    +"table-column. Explain the contents of the attribute fully so that a data "
    +"user could interpret the attribute accurately.<br></br>"
    +"<font color=\"666666\">&nbsp;&nbsp;[Example(s):&nbsp;&nbsp;&nbsp;"
    +"\"spden\" is the number of individuals of all macro invertebrate species "
    +"found in the plot]</font>", 4));

    JPanel attribDefinitionPanel = WidgetFactory.makePanel(3);

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
    
    measScalePanel = WidgetFactory.makePanel(7);
    
    JPanel radioPanel = WidgetFactory.makeRadioPanel(buttonsText, -1, listener);

    Dimension radioPanelDims = new Dimension( 
                (int)(WizardSettings.WIZARD_CONTENT_LABEL_DIMS.getWidth()) + 50,
                (int)(radioPanel.getPreferredSize().getHeight()));
                           
    WidgetFactory.setPrefMaxSizes(radioPanel, radioPanelDims);
     
    measScalePanel.add(radioPanel);
      
    currentPanel  = WidgetFactory.makeVerticalPanel(7);
    
    measScalePanel.add(currentPanel);

    middlePanel.add(measScalePanel);
    currentPanel.setBackground(java.awt.Color.green);
    currentPanel.setOpaque(true);
    
    nominalPanel  = getNominalPanel();
    ordinalPanel  = getOrdinalPanel();
    intervalPanel = getIntervalPanel();
    ratioPanel    = getRatioPanel();
    dateTimePanel = getDateTimePanel();
  } 

  private void setMeasurementScale(String scale) {

    this.measurementScale = scale;
  }

  private void setMeasurementScaleUI(JPanel panel) {

    measScalePanel.remove(currentPanel);
    currentPanel   = panel;
    measScalePanel.add(currentPanel);
    middlePanel.validate();
    middlePanel.repaint();
  }
  
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private JLabel listLabel;
  
  
  private JPanel getNominalPanel() {
    
    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
    WidgetFactory.addTitledBorder(panel, buttonsText[0]);
    
    ////
    panel.add(WidgetFactory.makeDefaultSpacer());
  
    listLabel = WidgetFactory.makeHTMLLabel("#########", 1);
    
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
  
    return panel;
  }
  
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  private JPanel getOrdinalPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
    WidgetFactory.addTitledBorder(panel, buttonsText[1]);
    
    ////
    return panel;
  }
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  private JPanel getIntervalPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
    WidgetFactory.addTitledBorder(panel, buttonsText[2]);
    
    ////
    return panel;
  }
    
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  private JPanel getRatioPanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
    WidgetFactory.addTitledBorder(panel, buttonsText[3]);
    
    ////
    return panel;
  }
    
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  private JPanel getDateTimePanel() {

    JPanel panel = WidgetFactory.makeVerticalPanel(7);
    
    WidgetFactory.addTitledBorder(panel, buttonsText[4]);
    
    ////
    return panel;
  }
    
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  
  
  
  
  
  
  

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

    return true; 
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
  private OrderedMap returnMap = new OrderedMap();
  //
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

    return returnMap;
  }
}