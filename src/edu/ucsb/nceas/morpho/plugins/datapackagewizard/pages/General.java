/**
 *  '$RCSfile: General.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-24 02:14:18 $'
 * '$Revision: 1.15 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;

import javax.swing.JLabel;
import javax.swing.BoxLayout;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;


public class General extends AbstractUIPage{

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID     = DataPackageWizardInterface.GENERAL;
  private final String nextPageID = DataPackageWizardInterface.KEYWORDS;
  private final String title      = "Title and Abstract";
  private final String subtitle   = "";
  public final String pageNumber  = "2";

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private JTextField  titleField;
  private JTextArea   absField;
  private JLabel      titleLabel;

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  public General() { init(); }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   * are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    JPanel vbox = this;

    vbox.add(WidgetFactory.makeHalfSpacer());

    JLabel titleDesc = WidgetFactory.makeHTMLLabel(
        "<b>Enter the title of the data package.</b> The title field provides a "
      +"description of the data that is long enough to differentiate it from "
      +"other similar data. e.g. vernal pool amphibian density data, Isla Vista, "
      +"CA USA, 1990-1996", 3);
    vbox.add(titleDesc);

    JPanel titlePanel = WidgetFactory.makePanel(1);

    titleLabel = WidgetFactory.makeLabel(" Title:", true);
    titlePanel.add(titleLabel);

    titleField = WidgetFactory.makeOneLineTextField();
    titlePanel.add(titleField);

    titlePanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(titlePanel);

    vbox.add(WidgetFactory.makeDefaultSpacer());

    ////////////////////////////////////////////////////////////////////////////

    JLabel absDesc = WidgetFactory.makeHTMLLabel(
    "<b>Enter an abstract that describes the data package.</b> This abstract is "
    +"a paragraph or more that describes the particular data that are being "
    +"documented. Describ the objectives of the study, key aspects of the study "
    +"design or methods, etc.", 3);
    vbox.add(absDesc);

    JPanel abstractPanel = WidgetFactory.makePanel();

    JLabel absLabel = WidgetFactory.makeLabel(" Abstract:", false);
    absLabel.setVerticalAlignment(SwingConstants.TOP);
    absLabel.setAlignmentY(SwingConstants.TOP);
    abstractPanel.add(absLabel);

    absField = WidgetFactory.makeTextArea("", 15, true);
    JScrollPane jscrl = new JScrollPane(absField);
    abstractPanel.add(jscrl);

    abstractPanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));
    vbox.add(abstractPanel);

    vbox.add(WidgetFactory.makeDefaultSpacer());
    vbox.add(WidgetFactory.makeDefaultSpacer());

  }


  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *



  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {
    WidgetFactory.unhiliteComponent(titleLabel);
    if(titleField.getText().length() == 0){
      titleField.requestFocus();
    }
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

    if (titleField.getText().trim().equals("")) {

      WidgetFactory.hiliteComponent(titleLabel);
      titleField.requestFocus();
      return false;
    }
    return true;
  }


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

    returnMap.put("/eml:eml/dataset/title[1]", titleField.getText().trim());

    if ( !(absField.getText().trim().equals("")) ) {

      returnMap.put("/eml:eml/dataset/abstract/para[1]",
                    absField.getText().trim());
    }
    return returnMap;
  }



  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular wizard page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular wizard page
   */
  public OrderedMap getPageData(String rootXPath) {

    throw new UnsupportedOperationException(
      "getPageData(String rootXPath) Method Not Implemented");
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

  public boolean setPageData(OrderedMap data, String xPathRoot) { return false; }
}
