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
 *     '$Date: 2004-03-17 21:13:01 $'
 * '$Revision: 1.21 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;

import edu.ucsb.nceas.morpho.plugins.TextImportListener;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.TextImportWizardEml2;

import edu.ucsb.nceas.utilities.OrderedMap;
import java.io.File;


public class ImportWizard extends     AbstractUIPage
                          implements  TextImportListener {

  public final String pageID     = DataPackageWizardInterface.TEXT_IMPORT_WIZARD;
  public String nextPageID = DataPackageWizardInterface.SUMMARY;
  public final String pageNumber = "";

  public final String title      = "Data Package Wizard";
  public final String subtitle   = "Import Data/Information";

  private WizardContainerFrame mainWizFrame;
  private OrderedMap resultsMap;

  private boolean importCompletedOK = false;

  private TextImportWizardEml2 importWizFrame;

  public ImportWizard(WizardContainerFrame mainWizFrame) {

    this.mainWizFrame = mainWizFrame;
    init();
  }

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

      AbstractUIPage locationPage
          = WizardPageLibrary.getPage(DataPackageWizardInterface.DATA_LOCATION);
      File dataFileObj = ((DataLocation)locationPage).getDataFile();

      importWizFrame = new TextImportWizardEml2(dataFileObj, this, mainWizFrame);

      importWizFrame.setBounds(mainWizFrame.getX(), mainWizFrame.getY(),
                           mainWizFrame.getWidth(), mainWizFrame.getHeight());

      if (importWizFrame.startImport()) {

        importWizFrame.setVisible(true);
        mainWizFrame.setVisible(false);

      } else {
        importCanceled();
      }

    } else {
      // if import has completed OK, we don't need to be on this
      // page - go straight back to previous page
      importCanceled();
    }
  }

  /** TextImportListener interface
   * This method is called when editing is complete
   *
   * @param om is the OrderedMap returned by the TIW
   */
  public void importComplete(OrderedMap om) {

    resultsMap = om;
    cleanUp();
    mainWizFrame.nextAction();
    importCompletedOK = true;
  }

  /** TextImportListener interface
   * this method handles canceled editing
   */
  public void importCanceled() {

    cleanUp();
    importCompletedOK = false;
    mainWizFrame.cancelAction();

//    mainWizFrame.previousAction();
  }

  private void cleanUp() {

    if (importWizFrame!=null) {
      mainWizFrame.setBounds(importWizFrame.getX(),     importWizFrame.getY(),
                        importWizFrame.getWidth(), importWizFrame.getHeight());
    }

    mainWizFrame.setVisible(true);

    if (importWizFrame!=null) {
      importWizFrame.setVisible(false);
      importWizFrame.dispose();
      importWizFrame = null;
    }
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   */
  public void onRewindAction() {

    //never used
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

    //never used
    return true;
  }

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

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  public void setPageData(OrderedMap data) { }
}
