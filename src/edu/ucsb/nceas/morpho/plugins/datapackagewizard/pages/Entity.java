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
 *     '$Date: 2003-09-10 00:54:36 $'
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
    this.add(WidgetFactory.makeDefaultSpacer());
    
    ////////////////////////////////////////////////////////////////////////////
    
    JLabel entityDesc = WidgetFactory.makeHTMLLabel(
    "Enter a descriptive paragraph that describes the entity, its type, and "
    +"relevant information about the data in the entity.<br></br>Example: "
    +"Species abundance data for 1996 at the VCR LTER site", 3);
    
    this.add(entityDesc);

    this.add(WidgetFactory.makeDefaultSpacer());
        
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
    
    attributeList = WidgetFactory.makeList(colNames, editors, 4,
                                    true, true, false, true, true, true );
    
    this.add(attributeList);
    
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
    
    EntityDialog keywordsDialog 
                              = new EntityDialog(WizardContainerFrame.frame);

    if (keywordsDialog.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {
    
      List newRow = keywordsDialog.getSurrogate();
      newRow.add(keywordsDialog);
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
      if (nextRowList.size()<3)     continue;
      nextUserObject = nextRowList.get(2);
      if (nextUserObject==null) continue;
      
      nextEntityDialog = (EntityDialog)nextUserObject;
      
      nextNVPMap = nextEntityDialog.getPageData(xPathRoot + (index++) + "]");
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

  private JTextField thesaurusField;
  private JLabel kwLabel;
  private CustomList kwList;
  private String[] kwTypeArray = new String[]{  "discipline",
                                                "place",
                                                "stratum", 
                                                "taxonomic",
                                                "temporal",
                                                "thematic" };
  
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
    
    JLabel desc = WidgetFactory.makeHTMLLabel(
                      "<font size=\"4\"><b>Define Keyword Set:</b></font>", 1);
    middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
    
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    
    middlePanel.add(desc);
    
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    
    ////
    JPanel thesaurusPanel = WidgetFactory.makePanel(1);
    thesaurusPanel.add(WidgetFactory.makeLabel("Thesaurus name:", false));
    thesaurusField = WidgetFactory.makeOneLineTextField();
    thesaurusPanel.add(thesaurusField);
    middlePanel.add(thesaurusPanel);
        
    middlePanel.add(WidgetFactory.makeDefaultSpacer());
    middlePanel.add(WidgetFactory.makeDefaultSpacer());

    ////
    JPanel kwPanel = WidgetFactory.makePanel(4);
    kwLabel = WidgetFactory.makeLabel("Entity:", true);
    kwPanel.add(kwLabel);
    
    middlePanel.add(kwPanel);
  } 
  
  
  /** 
   *  The action to be executed when the "OK" button is pressed. If no onAdvance 
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false 
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
   
    if (kwList.getRowCount() < 1) {
  
      WidgetFactory.hiliteComponent(kwLabel);
      return false;
    }
    return true; 
  }


  /**
   *  @return a List contaiing 2 String elements - one for each column of the 
   *  2-col list in which this surrogate is displayed
   *
   */
  private final StringBuffer surrogateBuff = new StringBuffer();
  //
  public List getSurrogate() {

    WidgetFactory.unhiliteComponent(kwLabel);
    
    List surrogate = new ArrayList();
    
    //thesaurus (first column) surrogate:
    String thesaurus   = thesaurusField.getText().trim();
    if (thesaurus==null) thesaurus = "";
    surrogate.add(thesaurus);

    
    //keywords (second column) surrogate:
    surrogateBuff.delete(0, surrogateBuff.length());
    List rowLists = kwList.getListOfRowLists();
    boolean firstKW = true;
    String  nextKW  = null;
  
    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
  
      // CHECK FOR AND ELIMINATE EMPTY ROWS...
      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;
      
      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;
      
      if (nextRow.get(0)==null) continue;
      nextKW = ((String)(nextRow.get(0))).trim();
      
      if (nextKW.equals("")) continue;
      
      if (firstKW) firstKW = false;
      else surrogateBuff.append(", ");

      surrogateBuff.append(nextKW);
    }
    
    surrogate.add(surrogateBuff.toString());

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

    returnMap.putAll(getKWListAsNVP(xPathRoot));
    
    String thesaurus = thesaurusField.getText().trim();
    if (thesaurus!=null && !thesaurus.equals("")) {
      returnMap.put(xPathRoot + "/keywordThesaurus", thesaurus);
    }
    
    return returnMap;
  }

  
  private final OrderedMap listResultsMap    = new OrderedMap();
  private final StringBuffer listResultsBuff = new StringBuffer();
  //
  private OrderedMap getKWListAsNVP(String xPathRoot) {
  
    listResultsMap.clear();
    
    int index=0;
    List rowLists = kwList.getListOfRowLists();
    String nextKW = null;
    String nextKWType = null;
  
    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
  
      // CHECK FOR AND ELIMINATE EMPTY ROWS...
      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;
      
      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;
      
      if (nextRow.get(0)==null) continue;
      nextKW = ((String)(nextRow.get(0))).trim();
      
      if (nextKW.equals("")) continue;
      
      listResultsBuff.delete(0,listResultsBuff.length());
      listResultsBuff.append(xPathRoot);
      listResultsBuff.append("/keyword[");
      listResultsBuff.append(++index);
      listResultsBuff.append("]");
      listResultsMap.put(listResultsBuff.toString(), nextKW);
    
      if (nextRow.get(1)==null) continue;
      nextKWType = ((String)(nextRow.get(1))).trim();
    
      if (nextKWType.equals("")) continue;
    
      listResultsBuff.delete(0,listResultsBuff.length());
      listResultsBuff.append(xPathRoot);
      listResultsBuff.append("/keyword[");
      listResultsBuff.append(index);
      listResultsBuff.append("]@keywordType");
      listResultsMap.put(listResultsBuff.toString(), nextKWType);
    }
    return listResultsMap;
  }
  
}