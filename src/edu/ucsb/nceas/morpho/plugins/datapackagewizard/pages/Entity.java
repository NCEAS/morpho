/**
 *  '$RCSfile: Entity.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-10 04:22:36 $'
 * '$Revision: 1.2 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.utilities.OrderedMap;

import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.AbstractAction;
import javax.swing.SwingConstants;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class Entity extends AbstractWizardPage{
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  private final String pageID     = WizardPageLibrary.ENTITY;
  private final String nextPageID = WizardPageLibrary.SUMMARY;
  private final String title      = "Data Information:";
  private final String subtitle   = "Table (Entity)";
  private final String xPathRoot  = "/eml:eml/dataset/dataTable[1]";
  
  private final String[] colNames =  {"Attribute Name", 
                                      "Attribute Definition", 
                                      "Measurement Scale"};
  private final Object[] editors  =   null; //makes non-directly-editable
  
  private JTextField  entityNameField;  
  private JTextArea   entityDescField;
  private JLabel      entityNameLabel;
  private CustomList  attributeList;
  private JLabel      attributesLabel;
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public Entity() { init(); }
  
  
  
  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    this.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc1 = WidgetFactory.makeHTMLLabel(
      "Enter some information about the data table contained in your file. "
      +"If you have more than one data table, additional tables may be added "
      +"after you create your data package. Required fields are highlighted. ",
                                                                             2);
    this.add(desc1);
    
    this.add(WidgetFactory.makeDefaultSpacer());
    this.add(WidgetFactory.makeDefaultSpacer());
    
    ///
    JPanel attributePanel = WidgetFactory.makePanel(1);
    
    entityNameLabel = WidgetFactory.makeLabel("Table name:", true);

    attributePanel.add(entityNameLabel);
    
    entityNameField = WidgetFactory.makeOneLineTextField();
    attributePanel.add(entityNameField);
    
    this.add(attributePanel);
    
    this.add(WidgetFactory.makeDefaultSpacer());
    
    ////////////////////////////////////////////////////////////////////////////
    
    JLabel entityDesc = WidgetFactory.makeHTMLLabel(
    "Enter a paragraph that describes the table or entity, its type, and "
    +"relevant information about the data that it contains.<br></br>"
    +"<font color=\"666666\">&nbsp;&nbsp;[Example:&nbsp;&nbsp;&nbsp;Species "
    +"abundance data for 1996 at the VCR LTER site]</font>", 3);
    
    this.add(entityDesc);

    JPanel entityDescPanel = WidgetFactory.makePanel();

    JLabel entityLabel = WidgetFactory.makeLabel("Description", false);
    entityLabel.setVerticalAlignment(SwingConstants.TOP);
    entityLabel.setAlignmentY(SwingConstants.TOP);
    entityDescPanel.add(entityLabel);
    
    entityDescField = WidgetFactory.makeTextArea("", 6, true);
    JScrollPane jscrl = new JScrollPane(entityDescField);
    entityDescPanel.add(jscrl);
    this.add(entityDescPanel);
    
    ////////////////////////////////////////////////////////////////////////////
    this.add(WidgetFactory.makeDefaultSpacer());
    
    this.add(WidgetFactory.makeHTMLLabel(
                      "One or more attributes (columns) must be defined:", 1));
    
    JPanel attribsPanel = WidgetFactory.makePanel();

    attributesLabel = WidgetFactory.makeLabel("Attributes", true);
    attribsPanel.add(attributesLabel);
    
    attributeList = WidgetFactory.makeList(colNames, editors, 4,
                                    true, true, false, true, true, true );
    attribsPanel.add(attributeList);
    
    this.add(attribsPanel);

    initActions();
  }

  
  /** 
   *  Custom actions to be initialized for list buttons
   */
  private void initActions() {
  
    attributeList.setCustomAddAction( 
      
      new AbstractAction() {
    
        public void actionPerformed(ActionEvent e) {
      
          Log.debug(45, "\nEntity: CustomAddAction called");
          showNewEntityDialog();
        }
      });
  
    attributeList.setCustomEditAction( 
      
      new AbstractAction() {
    
        public void actionPerformed(ActionEvent e) {
      
          Log.debug(45, "\nEntity: CustomEditAction called");
          showEditEntityDialog();
        }
      });
  }
  
  private void showNewEntityDialog() {
    
    EntityDialog entityDialog = new EntityDialog(WizardContainerFrame.frame);

    if (entityDialog.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {
    
      List newRow = entityDialog.getSurrogate();
      newRow.add(entityDialog);
      attributeList.addRow(newRow);
    }
    WidgetFactory.unhiliteComponent(attributesLabel);
  }
  

  private void showEditEntityDialog() {
    
    List selRowList = attributeList.getSelectedRowList();
    
    if (selRowList==null || selRowList.size() < 3) return;
    
    Object dialogObj = selRowList.get(2);
    
    if (dialogObj==null || !(dialogObj instanceof EntityDialog)) return;
    EntityDialog editEntityDialog = (EntityDialog)dialogObj;

    editEntityDialog.resetBounds();
    editEntityDialog.setVisible(true);
    
    if (editEntityDialog.USER_RESPONSE==EntityDialog.OK_OPTION) {
    
      List newRow = editEntityDialog.getSurrogate();
      newRow.add(editEntityDialog);
      attributeList.replaceSelectedRow(newRow);
    }
  }

  
  

  
  
  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {

    entityNameField.requestFocus();
  }
  
  
  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {
    
    WidgetFactory.unhiliteComponent(attributesLabel);
  }
  
  
  /** 
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty, but if so, must 
   *  return true
   *
   *  @return boolean true if wizard should advance, false if not 
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
    
    if (entityNameField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(entityNameLabel);
      return false;
    }
    
    if (attributeList.getRowCount() < 1) {

      WidgetFactory.hiliteComponent(attributesLabel);
      return false;
    }
    return true; 
  }
  
  
  /** 
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  
  private OrderedMap returnMap = new OrderedMap();
  //
  public OrderedMap getPageData() {
  
    returnMap.clear();
    
    int index = 1;
    Object  nextRowObj      = null;
    List    nextRowList     = null;
    Object  nextUserObject  = null;
    OrderedMap  nextNVPMap  = null;
    EntityDialog nextEntityDialog = null;
    
    List rowLists = attributeList.getListOfRowLists();
    
    if (rowLists==null) return null;
    
    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
    
      nextRowObj = it.next();
      if (nextRowObj==null) continue;
      
      nextRowList = (List)nextRowObj;
      //column 2 is user object - check it exists and isn't null:
      if (nextRowList.size()<4)     continue;
      nextUserObject = nextRowList.get(3);
      if (nextUserObject==null) continue;
      
      nextEntityDialog = (EntityDialog)nextUserObject;
      
      nextNVPMap = nextEntityDialog.getPageData(xPathRoot 
                                + "/attributeList/attribute["+(index++) + "]");
      returnMap.putAll(nextNVPMap);
    }
    return returnMap;
  }
  
  
  
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() { return pageID; }
  
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
  public String getNextPageID() { return nextPageID; }
}




////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//                        D I A L O G    C L A S S
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////


class EntityDialog extends WizardPopupDialog {

  private JTextField attribNameField;
  private JTextField measScaleField;
  
  private JLabel kwLabel;
  private JLabel attribNameLabel;
  private JLabel attribDefinitionLabel;
  private JLabel measScaleLabel;
  
  private String measurementScale;
  
  private JTextArea attribDefinitionField;
  private final String[] buttonsText = new String[] {
      "Nominal",
      "Ordinal",
      "Interval",
      "Ratio",
      "DateTime"
    };
  
  public EntityDialog(JFrame parent) { 
  
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
    +"found in the plot]</font>", 3));

    JPanel attribDefinitionPanel = WidgetFactory.makePanel();

    attribDefinitionLabel = WidgetFactory.makeLabel("Definition", true);
    attribDefinitionLabel.setVerticalAlignment(SwingConstants.TOP);
    attribDefinitionLabel.setAlignmentY(SwingConstants.TOP);
    attribDefinitionPanel.add(attribDefinitionLabel);
    
    attribDefinitionField = WidgetFactory.makeTextArea("", 4, true);
    JScrollPane jscrl = new JScrollPane(attribDefinitionField);
    attribDefinitionPanel.add(jscrl);
    middlePanel.add(attribDefinitionPanel);
 
  
    ////
    measScaleLabel = WidgetFactory.makeLabel(
                                "Select and define a Measurement Scale:", true, 
                                WizardSettings.WIZARD_CONTENT_TEXTFIELD_DIMS);
    middlePanel.add(measScaleLabel);
    
    JPanel measScalePanel = WidgetFactory.makePanel(5);
    measScaleField = WidgetFactory.makeOneLineTextField();
    measScalePanel.add(measScaleField);
    middlePanel.add(measScalePanel);
    
    final JPanel instance = middlePanel;
    
    ActionListener listener = new ActionListener() {
      
      public void actionPerformed(ActionEvent e) {
        
        Log.debug(45, "got radiobutton command: "+e.getActionCommand());

        //undo any hilites:
        
        if (e.getActionCommand().equals(buttonsText[0])) {
          
//          instance.remove(currentPanel);
//          currentPanel = inlinePanel;
//          distribXPath = INLINE_XPATH;
//          instance.add(inlinePanel, BorderLayout.CENTER);

          setMeasurementScale("/nominal");
                    
        } else if (e.getActionCommand().equals(buttonsText[1])) {
        
          setMeasurementScale("/ordinal");
                  
          
        } else if (e.getActionCommand().equals(buttonsText[2])) {
        
          setMeasurementScale("/interval");
                  
          
        } else if (e.getActionCommand().equals(buttonsText[3])) {
        
          setMeasurementScale("/ratio");
                  
        } else if (e.getActionCommand().equals(buttonsText[3])) {
        
          setMeasurementScale("/dateTime");
                    
        }
        instance.validate();
        instance.repaint();
      }
    };
    
    
    JPanel radioPanel = WidgetFactory.makeRadioPanel(buttonsText, -1, listener);
    
    middlePanel.add(radioPanel);
      
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

  } 
  
  private void setMeasurementScale(String scale) {
  
    this.measurementScale = scale;
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
//    String measurementScale   = measurementScaleField.getText().trim();
    String measurementScale   = "";
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