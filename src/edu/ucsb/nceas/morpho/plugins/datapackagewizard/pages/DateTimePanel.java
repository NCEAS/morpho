/**
 *  '$RCSfile: DateTimePanel.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-24 02:54:10 $'
 * '$Revision: 1.4 $'
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Component;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;



class DateTimePanel extends JPanel implements DialogSubPanelAPI {

  private JLabel     formatStringLabel;
  private JLabel     precisionLabel;
  
  private JTextField formatStringField;
  private JTextField precisionField;
  
  private CustomList boundsList;
  
  private AttributeDialog attributeDialog;
  
  private String[] numberTypesArray = new String[] { "natural", "whole",
                                                     "integer", "real" };
  
  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  
  
  /**
   *  Constructor
   *
   *  @param attributeDialog the parent dialog
   *
   *  @param nom_ord_mode can be AttributeDialog.MEASUREMENTSCALE_NOMINAL 
   *                  or AttributeDialog.MEASUREMENTSCALE_ORDINAL
   */
  public DateTimePanel(AttributeDialog attributeDialog) {
  
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
    
    ////////////////////////
    
    JPanel formatStringPanel = WidgetFactory.makePanel();
    formatStringLabel    = WidgetFactory.makeLabel("Format:", true);
    formatStringPanel.add(formatStringLabel);
    formatStringField = WidgetFactory.makeOneLineTextField();
    formatStringPanel.add(formatStringField);

    JPanel formatStringGrid = new JPanel(new GridLayout(1,2));
    formatStringGrid.add(formatStringPanel);
    formatStringGrid.add(WidgetFactory.makeLabel(
        "<html><font color=\"#666666\">e.g: YYYY-MM-DDTHH:MM:SS ,"
        +"&nbsp;&nbsp;YYYY-MM-DD ,&nbsp;&nbsp;hh:mm:ss.sss</font></html>", false,
        new Dimension(1000,30)) );

//    this.add(WidgetFactory.makeHalfSpacer());
    this.add(formatStringGrid);
  

    ////////////////////////
    
    JPanel precisionPanel = WidgetFactory.makePanel();
    precisionLabel    = WidgetFactory.makeLabel("Precision:", true);
    precisionPanel.add(precisionLabel);
    precisionField = WidgetFactory.makeOneLineTextField();
    precisionPanel.add(precisionField);

    JPanel precisionGrid = new JPanel(new GridLayout(1,2));
    precisionGrid.add(precisionPanel);
    precisionGrid.add(WidgetFactory.makeLabel(
        "<html>Precision of a date or time measurement, interpreted in the "
        +"smallest units represented by the datetime format."
        +"&nbsp;&nbsp;<font color=\"#666666\">e.g: 0.1 or 0.01</font></html>", false,
        new Dimension(1000,40)) );

    this.add(WidgetFactory.makeHalfSpacer());
    this.add(precisionGrid);
    this.add(WidgetFactory.makeHalfSpacer());
  

    String[] colNames     = new String[] {  "Min.", "excl?", 
                                            "Max.", "excl?"};
                                            
    Object[] colTemplates = new Object[] {  new JTextField(), new JCheckBox(), 
                                            new JTextField(), new JCheckBox()};
    ////////////////////////
    
    JPanel boundsHelpPanel = WidgetFactory.makeVerticalPanel(4);
//    new JPanel();
//    boundsHelpPanel.setLayout(new BoxLayout(boundsHelpPanel, BoxLayout.Y_AXIS));

    ////////////////////////
    
    JPanel boundsPanel = new JPanel();
    boundsPanel.setLayout(new BoxLayout(boundsPanel, BoxLayout.X_AXIS));
    
    boundsPanel.add(WidgetFactory.makeLabel("Bounds:", false));
    
    boundsList = WidgetFactory.makeList(colNames, colTemplates, 2,
                                        true, false, false, true, false, false);
    boundsList.setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS_SMALL);
    boundsPanel.add(boundsList);
    
    /////////////////

    
    boundsHelpPanel.add(boundsPanel);
    boundsHelpPanel.add(WidgetFactory.makeLabel(
        "<html><font color=\"#666666\">Check the 'excl?' box if "
        +"the bound does not include the value itself</font></html>", false,
        new Dimension(1000,22)) );
    
    
    JPanel boundsGrid = new JPanel(new GridLayout(1,2));
    boundsGrid.add(boundsHelpPanel);
    boundsGrid.add(WidgetFactory.makeLabel(
    "<html>Range of permitted values, in same date-time format as used in "
    +"the format description above. <br></br><font color=\"#666666\">e.g: if format is "
    +"\"YYYY-MM-DD\", a valid minimum would be \"2001-05-29\"</font><br></br></html>", 
    false, new Dimension(1000,35)));
    
//    this.add(WidgetFactory.makeHalfSpacer());
    this.add(boundsGrid);
  
    ////////////////////////

  }
  
 
  private static Component makeHalfSpacer() {
    
    return Box.createRigidArea(new Dimension(
                    WizardSettings.DEFAULT_SPACER_DIMS.width/2,
                    WizardSettings.DEFAULT_SPACER_DIMS.height/2));
  }
  

  

  /** 
   *  The action to be executed when the panel is displayed. May be empty
   */
  public void onLoadAction() {
  
    WidgetFactory.unhiliteComponent(formatStringLabel);
    WidgetFactory.unhiliteComponent(precisionLabel);
  }
  
  /** 
   *  checks that the user has filled in required fields - if not, highlights 
   *  labels to draw attention to them
   *
   *  @return   boolean true if user data validated OK. false if intervention 
   *            required
   */
  public boolean validateUserInput() {

    if (formatStringField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(formatStringLabel);
      formatStringField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(formatStringLabel);

    if (precisionField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(precisionLabel);
      precisionField.requestFocus();
      return false;
    }
    WidgetFactory.unhiliteComponent(precisionLabel);
    
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
  ////////////////////////////////////////////////////////
  public OrderedMap getPanelData(String xPathRoot) {

    returnMap.clear();
   
    returnMap.put(  xPathRoot + "/formatString", 
                    formatStringField.getText().trim());

    returnMap.put(  xPathRoot + "/dateTimePrecision", 
                    precisionField.getText().trim());
                
    returnMap.put(  xPathRoot + "/dateTimeDomain", "");
    
    xPathRoot = xPathRoot + "/dateTimeDomain/bounds[";
    
    int index = 0;
    List rowLists = boundsList.getListOfRowLists();
    String nextMin = null;
    String nextMax = null;
    Object nextExcl = null;
  
    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
  
      // CHECK FOR AND ELIMINATE EMPTY ROWS...
      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;
      
      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;
      
      boolean minIsNull = (nextRow.get(0)==null);
      boolean maxIsNull = (nextRow.get(2)==null);

      if (minIsNull && maxIsNull) continue;
      
      index++;
      
      if (!minIsNull) {
      
        nextMin = (String)(nextRow.get(0));
        if (!nextMin.trim().equals("")) {
          returnMap.put(xPathRoot + index + "]/minimum", nextMin);

          nextExcl = nextRow.get(1);
          if (nextExcl!=null && ((Boolean)nextExcl).booleanValue()) {
      
            returnMap.put(xPathRoot + index + "]/minimum/@exclusive", "true");
        
          } else {
        
            returnMap.put(xPathRoot + index + "]/minimum/@exclusive", "false");
          }
        }
      }
      
      if (!maxIsNull) {
      
        nextMax = (String)(nextRow.get(2));
        if (!nextMax.trim().equals("")) {
          returnMap.put(xPathRoot + index + "]/maximum", nextMax);

          nextExcl = nextRow.get(3);
          if (nextExcl!=null && ((Boolean)nextExcl).booleanValue()) {
      
            returnMap.put(xPathRoot + index + "]/maximum/@exclusive", "true");
        
          } else {
        
            returnMap.put(xPathRoot + index + "]/maximum/@exclusive", "false");
          }
        }
      }
    }
    return returnMap;
  }
}
