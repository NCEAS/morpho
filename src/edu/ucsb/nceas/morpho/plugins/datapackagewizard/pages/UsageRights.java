/**
 *  '$RCSfile: UsageRights.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-03-03 01:39:36 $'
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

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.utilities.OrderedMap;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;



public class UsageRights extends AbstractWizardPage{

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID     = DataPackageWizardInterface.USAGE_RIGHTS;
  private final String nextPageID = DataPackageWizardInterface.GEOGRAPHIC;
  private final String pageNumber = "9";
  private final String title      = "Usage Rights";
  private final String subtitle   = "";

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JTextArea   usageField;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public UsageRights() { init(); }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel vbox = this;

    vbox.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
      "<b>Enter a paragraph that describes the intended usage rights of the "
      +"data package.</b> Each Data Package may have intellectual rights "
      +"associated with the dataset.  You may declare that the data package is "
      +"in now in the public domain, or that there are certain ethical "
      +"restrictions in using the data.", 3);
    vbox.add(desc);

    vbox.add(WidgetFactory.makeDefaultSpacer());
    vbox.add(WidgetFactory.makeDefaultSpacer());

    //JLabel titleDesc = WidgetFactory.makeHTMLLabel(
    //    "", 2);
    //vbox.add(titleDesc);
    //vbox.add(WidgetFactory.makeDefaultSpacer());

    JPanel usagePanel = WidgetFactory.makePanel();

    JLabel usageLabel = WidgetFactory.makeLabel("Usage Rights:", false);
    usagePanel.add(usageLabel);

    usageField = WidgetFactory.makeTextArea("", 10, true);

    JScrollPane jscrl = new JScrollPane(usageField);

    usagePanel.add(jscrl);
    usagePanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));

    vbox.add(usagePanel);

    vbox.add(WidgetFactory.makeDefaultSpacer());
    vbox.add(WidgetFactory.makeDefaultSpacer());
    vbox.add(WidgetFactory.makeDefaultSpacer());

  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *



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
  public boolean onAdvanceAction() { return true; }


  /**
   *  gets the OrderedMap object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the OrderedMap object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();

  public OrderedMap getPageData() {

    returnMap.clear();

    if ( !(usageField.getText().trim().equals("")) ) {

      returnMap.put("/eml:eml/dataset/intellectualRights/para[1]",
                    usageField.getText().trim());
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
