/**
 *  '$RCSfile: Temporal.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-01-09 23:03:50 $'
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

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.Iterator;
import java.util.List;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class Temporal extends AbstractWizardPage{

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID     = DataPackageWizardInterface.TEMPORAL;
  private final String nextPageID = DataPackageWizardInterface.PARTY_INTRO;
  private final String title      = "Temporal Coverage";
  private final String subtitle   = "";
  private final String xPathRoot  = "/eml:eml/dataset/coverage/temporalCoverage[";
  private final String pageNumber  = "*";

  private final String[] colNames =  {"Time Coverages"};
  private final Object[] editors  =   null; //makes non-directly-editable

  private CustomList  timespanList;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public Temporal() { init(); }



  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel vbox = this;

    vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
      "<b>Enter information about Temporal Coverage.</b> You can specify "
      +"temporal coverage, and this can be a single point in time, multiple "
      +"points in time, or a range of dates.", 3);
    vbox.add(desc);
    vbox.add(WidgetFactory.makeDefaultSpacer());
    vbox.add(WidgetFactory.makeDefaultSpacer());

    timespanList = WidgetFactory.makeList(colNames, editors, 4,
                                    true, true, false, true, true, true );

    timespanList.setBorder(new EmptyBorder(0,WizardSettings.PADDING,
                             WizardSettings.PADDING, 2*WizardSettings.PADDING));

    vbox.add(timespanList);
    vbox.add(WidgetFactory.makeDefaultSpacer());

    initActions();
  }


  /**
   *  Custom actions to be initialized for list buttons
   */
  private void initActions() {

    timespanList.setCustomAddAction(

      new AbstractAction() {

        public void actionPerformed(ActionEvent e) {

          Log.debug(45, "\nTemporal: CustomAddAction called");
          showNewTemporalDialog();
        }
      });

    timespanList.setCustomEditAction(

      new AbstractAction() {

        public void actionPerformed(ActionEvent e) {

          Log.debug(45, "\nTemporal: CustomEditAction called");
          showEditTemporalDialog();
        }
      });
  }

  private void showNewTemporalDialog() {

    TemporalPage temporalPage = (TemporalPage)WizardPageLibrary.getPage(DataPackageWizardInterface.TEMPORAL_PAGE);
    WizardPopupDialog wpd = new WizardPopupDialog(temporalPage, WizardContainerFrame.frame, false);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {

      List newRow = temporalPage.getSurrogate();
      newRow.add(temporalPage);
      timespanList.addRow(newRow);
    }
  }


  private void showEditTemporalDialog() {

    List selRowList = timespanList.getSelectedRowList();

    if (selRowList==null || selRowList.size() < 3) return;

    Object dialogObj = selRowList.get(2);

    if (dialogObj==null || !(dialogObj instanceof TemporalPage)) return;
    TemporalPage editTemporalPage = (TemporalPage)dialogObj;

    WizardPopupDialog wpd = new WizardPopupDialog(editTemporalPage, WizardContainerFrame.frame, false);
    wpd.resetBounds();
    wpd.setVisible(true);


    if (wpd.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {

      List newRow = editTemporalPage.getSurrogate();
      newRow.add(editTemporalPage);
      timespanList.replaceSelectedRow(newRow);
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
  public void onRewindAction() {}


  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty, but if so, must
   *  return true
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    return true;
  }


  /**
   *  gets the Map object that contains all the temporal/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            temporal/value paired settings for this particular wizard page
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
    TemporalPage nextTemporalPage = null;

    List rowLists = timespanList.getListOfRowLists();

    if (rowLists==null) return null;

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      nextRowObj = it.next();
      if (nextRowObj==null) continue;

      nextRowList = (List)nextRowObj;
      //column 2 is user object - check it exists and isn't null:
      if (nextRowList.size()<3)     continue;
      nextUserObject = nextRowList.get(2);
      if (nextUserObject==null) continue;

      nextTemporalPage = (TemporalPage)nextUserObject;

      nextNVPMap = nextTemporalPage.getPageData(xPathRoot + (index++) + "]");
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

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  public void setPageData(OrderedMap data) { }
}

