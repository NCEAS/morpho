/**
 *  '$RCSfile: ImportWizard.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2003-11-25 18:03:10 $'
 * '$Revision: 1.10 $'
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
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;

import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Container;

import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.TextImportWizard;
import edu.ucsb.nceas.morpho.framework.TextImportWizardEml2;
import edu.ucsb.nceas.morpho.framework.TextImportListener;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.UIController;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;


public class ImportWizard extends     AbstractWizardPage 
                          implements  TextImportListener {

  public final String pageID     = DataPackageWizardInterface.TEXT_IMPORT_WIZARD;
  
  public final String nextPageID = DataPackageWizardInterface.SUMMARY;
  
  public final String title      = "Data Package Wizard";
  public final String subtitle   = "Import Data/Information";
  
  private final String IMPORT_WIZ_TITLE = "Text Import Wizard";
  
  private OrderedMap resultsMap;
  
  // TextImportWizardEml2 is the new Text Import Wizard for EML2.0.0 standard. 
  // The old TextImportWizard is however still retained in the module
  private TextImportWizardEml2 importWiz;
  
  private TextImportListener listener;
  private boolean importCompletedOK = false;
  
  private JDialog importWizDialog;
  
  public ImportWizard() { init(); }
  
  /** 
   * initialize method does frame-specific design - i.e. adding the widgets that 
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {}

  /** 
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
    
    // if import hasn't completed OK (i.e. first visit, or returning after 
    // a "cancel"), start up the import wizard.
    if (!importCompletedOK) {

      AbstractWizardPage locationPage = WizardPageLibrary.getPage(DataPackageWizardInterface.DATA_LOCATION);

      String fileTextName = ((DataLocation)locationPage).getImportFilePath();
                
      importWiz = new TextImportWizardEml2(fileTextName, this);
      
      importWiz.setVisible(false);
      
      final JFrame parent = WizardContainerFrame.frame;
      final int xcoord = ( parent.getX() + parent.getWidth()/2 ) 
                                                - WizardSettings.DIALOG_WIDTH/2;
      final int ycoord = ( parent.getY() + parent.getHeight()/2 ) 
                                                - WizardSettings.DIALOG_HEIGHT/2;
      
      importWizDialog = new JDialog(parent, IMPORT_WIZ_TITLE, true);
                                                
      importWizDialog.getContentPane().add(importWiz.getContentPane());

      importWizDialog.setBounds(xcoord, ycoord, WizardSettings.DIALOG_WIDTH, 
                                                WizardSettings.DIALOG_HEIGHT);

      importWizDialog.setVisible(true);

    } else {
      // if import has completed OK, we don't need to be on this 
      // page - go straight back to previous page
      importCanceled();
    }
  }

  /** TextImportListener interface
   * This method is called when editing is complete
   *
   * @param xmlString is the edited XML in String format
   */
  public void importComplete(OrderedMap om) {

    if (importWiz!=null) importWiz.setVisible(false);
    resultsMap = om;
    listener.importComplete(om);
    importCompletedOK = true; 
    if (importWizDialog!=null) {
      importWizDialog.setVisible(false);
      importWizDialog.dispose();
    }
    importWizDialog = null;
    importWiz = null;
  }
  
  /** TextImportListener interface
   * this method handles canceled editing
   */
  public void importCanceled() {
  
    if (importWiz!=null) importWiz.setVisible(false);
    if (importWizDialog!=null) {
      importWizDialog.setVisible(false);
      importWizDialog.dispose();
    }
    listener.importCanceled();
    importCompletedOK = false; 
    importWizDialog = null;
    importWiz = null;
  }
  
  /** 
   *  sets TextImportListener to be called when this class gets a callback from 
   *  the Text Import Wizard (i.e. the call gets passed on)
   *
   *  @param listener TextImportListener to be called when this class gets a 
   *                  callback from the Text Import Wizard (i.e. the call gets 
   *                  passed on)
   */
  public void setTextImportListener(TextImportListener listener) {
  
    this.listener = listener;
  }
  
  /** 
   *  The action to be executed when the "Prev" button is pressed. May be empty
   */
  public void onRewindAction() {}
  
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
  public OrderedMap getPageData() { return resultsMap; }
  
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

  public void setPageData(OrderedMap data) { }
}
