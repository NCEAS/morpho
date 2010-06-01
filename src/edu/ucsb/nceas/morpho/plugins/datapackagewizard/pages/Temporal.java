/**
 *  '$RCSfile: Temporal.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.31 $'
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
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
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
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

/**
 * <p>This is the page that shows the list of previously-added temporal
 * coverages, from where the user can use the add or edit buttons to bring up
 * the dialog</p>
 */
public class Temporal extends AbstractUIPage{


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID     = DataPackageWizardInterface.TEMPORAL;
  private final String title      = /*"Temporal Coverage"*/ Language.getInstance().getMessages("TemporalCoverage");
  private final String subtitle   = "";
  private final String xPathRoot  = "/eml:eml/dataset/coverage/temporalCoverage[";
  private final String pageNumber  = "11";

  private final String[] colNames =  {/*"Time Coverages"*/ Language.getInstance().getMessages("TimeCoverages")};
  private final Object[] editors  =   null; //makes non-directly-editable


  private CustomList  timespanList;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public Temporal() 
  { 
	  nextPageID = DataPackageWizardInterface.TAXONOMIC;
	  init(); 
  }


  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel vbox = this;

    vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
      /*"<b>Enter information about temporal coverage.</b> "*/
      "<b>" + Language.getInstance().getMessages("Temporal.desc_1") + "</b> "
      /*
      +"Temporal coverage "
      +"can be specified as a single point in time, multiple points in time, "
      +"or a range thereof. "
      */
      + Language.getInstance().getMessages("Temporal.desc_2")
      , 3);
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


  /**
   *  Function for showing New Temporal Dialogue. This funtion is used
   *  by the CustomList widget
   */
  private void showNewTemporalDialog() {

	 WizardPageLibrary library = new WizardPageLibrary(null);
    TemporalPage temporalPage = (TemporalPage)library.getPage(DataPackageWizardInterface.TEMPORAL_PAGE);
    ModalDialog wpd = new ModalDialog(temporalPage,
                                WizardContainerFrame.getDialogParent(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT, false);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE==ModalDialog.OK_OPTION) {

      List newRow = temporalPage.getSurrogate();
      newRow.add(temporalPage);
      timespanList.addRow(newRow);
    }
  }


  /**
   *  Function for editing the Temporal Dialogue already entered
   *  in the CustomList. This funtion is used by the CustomList widget
   */
  private void showEditTemporalDialog() {

    List selRowList = timespanList.getSelectedRowList();

    if (selRowList==null || selRowList.size() < 2) return;

    Object dialogObj = selRowList.get(1);

    if (dialogObj==null || !(dialogObj instanceof TemporalPage)) return;
    TemporalPage editTemporalPage = (TemporalPage)dialogObj;

    ModalDialog wpd = new ModalDialog(editTemporalPage,
                                WizardContainerFrame.getDialogParent(),
                                UISettings.POPUPDIALOG_WIDTH,
                                UISettings.POPUPDIALOG_HEIGHT
, false);
    wpd.resetBounds();
    wpd.setVisible(true);


    if (wpd.USER_RESPONSE==ModalDialog.OK_OPTION) {

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



  private OrderedMap returnMap = new OrderedMap();

  /**
   * gets the Map object that contains all the temporal/value paired settings
   * for this particular wizard page
   *
   * @ param xPath String that is appended to all keys in the map returned
   * @return data the Map object that contains all the temporal/value paired
   *   settings for this particular wizard page
   * @param xPath String
   */
  public OrderedMap getPageData(String xPath) {

    returnMap.clear();

    int     index           = 1;
    Object  nextRowObj      = null;
    List    nextRowList     = null;
    Object  nextUserObject  = null;

    OrderedMap   nextNVPMap        = null;
    TemporalPage nextTemporalPage  = null;

    List rowLists = timespanList.getListOfRowLists();
    if (rowLists != null && rowLists.isEmpty()) {
      return null;
    }

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      nextRowObj = it.next();
      if (nextRowObj==null) continue;

      nextRowList = (List)nextRowObj;
      //column 2 is user object - check it exists and isn't null:
      if (nextRowList.size()<2)     continue;
      nextUserObject = nextRowList.get(1);
      if (nextUserObject==null) continue;

      nextTemporalPage = (TemporalPage)nextUserObject;

      nextNVPMap = nextTemporalPage.getPageData(xPath + (index++) + "]");
      returnMap.putAll(nextNVPMap);
    }
    System.out.println("TemporalPage returning - " + returnMap.toString());
    // clear the list so that next time old variables dont show up again.
    //timespanList.removeAllRows();
    return returnMap;
  }


  /**
   *  gets the Map object that contains all the temporal/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            temporal/value paired settings for this particular wizard page
   */
  public OrderedMap getPageData() {

    return getPageData(xPathRoot);
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

 // assume that 'data' is an orderedMap for a single temporalCov subtree
  public boolean setPageData(OrderedMap data, String xPathRoot) {
    if (xPathRoot.equals("removeAllRows")) {
      timespanList.removeAllRows();
    }
    boolean res = true;
    WizardPageLibrary library = new WizardPageLibrary(null);
    TemporalPage temporalPage = (TemporalPage)library.getPage(
      DataPackageWizardInterface.TEMPORAL_PAGE);
    data.remove("/temporalCoverage/@scope");
    data.remove("/temporalCoverage/@id");
    boolean flag = temporalPage.setPageData(data, "/temporalCoverage");
    if(!flag) res = false;
    List newRow = temporalPage.getSurrogate();
    newRow.add(temporalPage);
    timespanList.addRow(newRow);
    return res;
  }
}

