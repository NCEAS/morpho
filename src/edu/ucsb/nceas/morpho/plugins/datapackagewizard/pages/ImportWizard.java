/**
 *  '$RCSfile: ImportWizard.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-13 05:40:53 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;

import java.util.Map;

import javax.swing.JLabel;

import java.awt.BorderLayout;

import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.TextImportWizard;
import edu.ucsb.nceas.morpho.framework.TextImportListener;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.Morpho;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;

public class ImportWizard extends     AbstractWizardPage 
                              implements  TextImportListener {

  public final String pageID     = WizardPageLibrary.TEXT_IMPORT_WIZARD;
  
  public final String nextPageID = WizardPageLibrary.SUMMARY;
  
  public final String title      = "Data Package Wizard";
  public final String subtitle   = "Import Data/Information";
  
  private OrderedMap resultsMap;
  
  private final String CONFIG_FILE_PATH 
      = System.getProperty("user.home") + java.io.File.separator 
                                                        + ".morpho/config.xml";
                        
  public ImportWizard() {
    
    init();
  }
  
  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {
  
// HACK - TEXT IMPORT WIZARD NEEDS MORPHO TO GET CONFIG
// STILL DOESN'T WORK
    try {
      Morpho mofo = new Morpho(new ConfigXML(CONFIG_FILE_PATH));
    } catch (Exception e) {}
  }

  
  /** 
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
    
// SAMPLE CODE:    
    String fileTextName = CONFIG_FILE_PATH;
//    if (!fileTextField.getText().equals("")) fileTextName = fileTextField.getText(); 
//    if (!includeDataFileCheckBox.isSelected()) fileTextField.setText("");
    
    TextImportWizard importWiz = new TextImportWizard(fileTextName, this);
  }
  

  /** TextImportListener interface
   * This method is called when editing is complete
   *
   * @param xmlString is the edited XML in String format
   */
  public void importComplete(OrderedMap om) {

    resultsMap = om;
  }
  
  
  /** TextImportListener interface
   * this method handles canceled editing
   */
  public void importCanceled() {
  
    Log.debug(1, "TO DO: NEED TO GO BACK A PAGE HERE!!");
  
  }
  
  
  
  /** 
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {
    
  }
  
  
  /** 
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty, but if so, must 
   *  return true
   *
   *  @return boolean true if wizard should advance, false if not 
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() { return true; }
  

  /** 
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public OrderedMap getPageData() {
  
    return resultsMap;
  }
  
  
  

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