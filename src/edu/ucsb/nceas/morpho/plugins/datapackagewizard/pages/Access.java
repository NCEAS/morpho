/**
 *  '$RCSfile: Access.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-03-04 03:51:15 $'
 * '$Revision: 1.12 $'
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

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CustomList;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPopupDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.Iterator;
import java.util.List;

public class Access extends AbstractWizardPage {

  public final String pageID     = DataPackageWizardInterface.ACCESS;
  public final String nextPageID = DataPackageWizardInterface.SUMMARY;
  public final String pageNumber = "13";

  //////////////////////////////////////////////////////////

  public final String title      = "Access Information";
  public final String subtitle   = " ";

  private JPanel radioPanel;
  private final String xPathRoot  = "/eml:eml/dataset/access/";

  private boolean publicReadAccess = true;
  private final String[] buttonsText = new String[] {
      "YES",
      "NO"
  };

  private final String[] colNames =  {"User", "Permissions"};
  private final Object[] editors  =   null;
  private CustomList  accessList;

  public Access() {
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    Box vBox = Box.createVerticalBox();
    vBox.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
        "<p><b>Allow public viewing access to your dataset?</b> Access to your "
        +"data can be controlled using this screen. By default, "
        +"read and write access is given to your username and read-only access is "
        +"given to the public. Do you want to give read access to the public?</p>", 3);

    vBox.add(desc);

    ActionListener listener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        Log.debug(45, "got radiobutton command: "+e.getActionCommand());
        if (e.getActionCommand().equals(buttonsText[0])) {
          publicReadAccess = true;
        }
        if (e.getActionCommand().equals(buttonsText[1])) {
          publicReadAccess = false;
        }
     }
   };

    radioPanel = WidgetFactory.makeRadioPanel(buttonsText, 0, listener);
    vBox.add(radioPanel);
    vBox.add(WidgetFactory.makeDefaultSpacer());
    vBox.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc1 = WidgetFactory.makeHTMLLabel(
    "<p><b>Specify access for other people related to the data package?</b> You "
    +"can specify access for other members of your team or any other person "
    +"related to the data package. Use the table below to add, edit and "
    +"delete access rights to your datapackage.</p>", 3);
    vBox.add(desc1);

    accessList = WidgetFactory.makeList(colNames, editors, 4,
                                   true, true, false, true, true, true );
    accessList.setBorder(new EmptyBorder(0,WizardSettings.PADDING,
                            WizardSettings.PADDING, 2*WizardSettings.PADDING));

    vBox.add(accessList);
    vBox.add(WidgetFactory.makeDefaultSpacer());

    this.add(vBox);

    initActions();
  }

  /**
   *  Custom actions to be initialized for list buttons
   */
  private void initActions() {

    accessList.setCustomAddAction(

      new AbstractAction() {

        public void actionPerformed(ActionEvent e) {

          Log.debug(45, "\nAccess: CustomAddAction called");
          showNewAccessDialog();
        }
      });

    accessList.setCustomEditAction(

      new AbstractAction() {

        public void actionPerformed(ActionEvent e) {

          Log.debug(45, "\nAccess: CustomEditAction called");
          showEditAccessDialog();
        }
      });
  }

  private void showNewAccessDialog() {

    AccessPage accessPage = (AccessPage)WizardPageLibrary.getPage(DataPackageWizardInterface.ACCESS_PAGE);
    WizardPopupDialog wpd = new WizardPopupDialog(accessPage, WizardContainerFrame.frame, false);
    wpd.setVisible(true);

    if (wpd.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {

      List newRow = accessPage.getSurrogate();
      newRow.add(accessPage);
      accessList.addRow(newRow);
    }
  }


  private void showEditAccessDialog() {

    List selRowList = accessList.getSelectedRowList();

    if (selRowList==null || selRowList.size() < 3) return;

    Object dialogObj = selRowList.get(2);

    if (dialogObj==null || !(dialogObj instanceof AccessPage)) return;
    AccessPage editAccessPage = (AccessPage)dialogObj;

    WizardPopupDialog wpd = new WizardPopupDialog(editAccessPage, WizardContainerFrame.frame, false);
    wpd.resetBounds();
    wpd.setVisible(true);


    if (wpd.USER_RESPONSE==WizardPopupDialog.OK_OPTION) {

      List newRow = editAccessPage.getSurrogate();
      newRow.add(editAccessPage);
      accessList.replaceSelectedRow(newRow);
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
    return true;
  }


  /**
   *  gets the Map object that contains all the access/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            access/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData() {

    returnMap.clear();

    int allowIndex = 1;
    int denyIndex = 1;
    Object  nextRowObj      = null;
    List    nextRowList     = null;
    Object  nextUserObject  = null;
    OrderedMap  nextNVPMap  = null;
    AccessPage nextAccessPage = null;


    if(publicReadAccess){
      returnMap.put(xPathRoot + "@authSystem", "knb");
      returnMap.put(xPathRoot + "@order", "denyFirst");
      returnMap.put(xPathRoot + "allow[" + (allowIndex) + "]/principal", "public");
      returnMap.put(xPathRoot + "allow[" + (allowIndex++) + "]/permission", "read");
    }

    List rowLists = accessList.getListOfRowLists();

    if (rowLists==null) return null;

       for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

         nextRowObj = it.next();
         if (nextRowObj==null) continue;

         nextRowList = (List)nextRowObj;
         //column 2 is user object - check it exists and isn't null:
         if (nextRowList.size()<3)     continue;
         nextUserObject = nextRowList.get(2);
         if (nextUserObject==null) continue;

         nextAccessPage = (AccessPage)nextUserObject;

         if(nextAccessPage.accessIsAllow){
           nextNVPMap = nextAccessPage.getPageData(xPathRoot + "allow[" + (allowIndex++) + "]");
         } else {
           nextNVPMap = nextAccessPage.getPageData(xPathRoot + "deny[" + (denyIndex++) + "]");
         }
         returnMap.putAll(nextNVPMap);
       }

    return returnMap;
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

  /**
     *  Returns the serial number of the page
     *
     *  @return the serial number of the page
     */
  public String getPageNumber() { return pageNumber; }

  public void setPageData(OrderedMap data) { }
}
