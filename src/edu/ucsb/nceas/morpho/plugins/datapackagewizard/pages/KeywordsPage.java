/**
 *  '$RCSfile: KeywordsPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2003-11-19 01:42:19 $'
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
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.utilities.OrderedMap;

import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;

import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.BorderLayout;

public class KeywordsPage extends AbstractWizardPage {

  private final String pageID     = DataPackageWizardInterface.KEYWORDS_PAGE;
  private final String nextPageID = "";
  private final String title      = "Keywords Page";
  private final String subtitle   = "";
  
  private final String EMPTY_STRING = "";
  private JTextField thesaurusField;
  private JComboBox  kwTypePickList;
  private JLabel kwLabel;
  private CustomList kwList;
  private JPanel middlePanel;
  private final String[] kwTypeArray 
                            = new String[]{ EMPTY_STRING,
                                            "place",
                                            "stratum", 
                                            "taxonomic",
                                            "temporal",
                                            "theme" };
  private final String xPathRoot  = "/eml:eml/dataset/keywordSet[1]";
  
  public KeywordsPage() { 
	  init();
  }
  
  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
    
    middlePanel = new JPanel();
    this.setLayout( new BorderLayout());
    this.add(middlePanel, BorderLayout.CENTER);
    
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
    JPanel kwPanel = WidgetFactory.makePanel(8);
    kwLabel = WidgetFactory.makeLabel("Keywords:", true);
    kwPanel.add(kwLabel);
    
    kwTypePickList = WidgetFactory.makePickList(kwTypeArray, false, 0, 
            new ItemListener(){ public void itemStateChanged(ItemEvent e) {}});
            
    kwList = WidgetFactory.makeList(new String[]{ "Keyword",
                                                  "Keyword Type (optional)" }, 
                                    new Object[]{ new JTextField(), 
                                                  kwTypePickList }, 
                                    4, true, false, false, true, true, true );
    kwPanel.add(kwList);
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
   
    Map listNVP = getKWListAsNVP(EMPTY_STRING);
    if (listNVP==null || listNVP.size() < 1) {
  
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
    if (thesaurus==null) thesaurus = EMPTY_STRING;
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
      
      if (nextKW.equals(EMPTY_STRING)) continue;
      
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
  public OrderedMap getPageData() {
	  return getPageData(xPathRoot);
  }
	  
  public OrderedMap getPageData(String xPathRoot) {
  
    returnMap.clear();

    returnMap.putAll(getKWListAsNVP(xPathRoot));
    
    String thesaurus = thesaurusField.getText().trim();
    if (thesaurus!=null && !thesaurus.equals(EMPTY_STRING)) {
      returnMap.put(xPathRoot + "/keywordThesaurus", thesaurus);
    }
    
    return returnMap;
  }

  
  private final OrderedMap listResultsMap    = new OrderedMap();
  private final StringBuffer listResultsBuff = new StringBuffer();
  //
  private OrderedMap getKWListAsNVP(String xPathRoot) {
  
    listResultsMap.clear();
    
    // CHECK FOR AND ELIMINATE EMPTY ROWS...
    kwList.deleteEmptyRows( CustomList.OR, 
                            new short[] {  CustomList.EMPTY_STRING_TRIM  } );
    
    int rowNumber       = -1;
    int predicateIndex  = 0;
    List rowLists       = kwList.getListOfRowLists();
    String nextKWType   = null;
  

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {
  
      rowNumber++;
      // CHECK FOR AND ELIMINATE EMPTY ROWS...
      Object nextRowObj = it.next();
      if (nextRowObj==null) continue;
      
      List nextRow = (List)nextRowObj;
      if (nextRow.size() < 1) continue;
      
      listResultsBuff.delete(0,listResultsBuff.length());
      listResultsBuff.append(xPathRoot);
      listResultsBuff.append("/keyword[");
      listResultsBuff.append(++predicateIndex);
      listResultsBuff.append("]");
      listResultsMap.put(listResultsBuff.toString(), 
                          ((String)(nextRow.get(0))).trim());
    
      if (nextRow.get(1)==null) continue;
      nextKWType = ((String)(nextRow.get(1))).trim();
    
      if (nextKWType.equals(EMPTY_STRING)) continue;
    
      listResultsBuff.delete(0,listResultsBuff.length());
      listResultsBuff.append(xPathRoot);
      listResultsBuff.append("/keyword[");
      listResultsBuff.append(predicateIndex);
      listResultsBuff.append("]/@keywordType");
      listResultsMap.put(listResultsBuff.toString(), nextKWType);
    }
    
    return listResultsMap;
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
 
  public void setPageData(OrderedMap data) {} 
}