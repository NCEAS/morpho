/**
 *  '$RCSfile: UsageRights.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-24 02:14:18 $'
 * '$Revision: 1.16 $'
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

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;



public class UsageRights extends AbstractUIPage{

  // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

  private final String pageID     = DataPackageWizardInterface.USAGE_RIGHTS;
  private final String nextPageID = DataPackageWizardInterface.GEOGRAPHIC;
  private final String pageNumber = "9";
  private final String title      = "Usage Rights";
  private final String subtitle   = "";

  private final String USAGE_ROOT      = "intellectualRights/";
  private final String XPATH_ROOT      = "/eml:eml/dataset[1]/" + USAGE_ROOT;
  private final String PARA_REL_XPATH  = "para[1]";

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

    JPanel usagePanel = WidgetFactory.makePanel();

    JLabel usageLabel = WidgetFactory.makeLabel("Usage Rights:", false);
    usagePanel.add(usageLabel);

    usageField = WidgetFactory.makeTextArea("", 18, true);

    JScrollPane jscrl = new JScrollPane(usageField);

    usagePanel.add(jscrl);
    usagePanel.setBorder(new javax.swing.border.EmptyBorder(0,0,0,5*WizardSettings.PADDING));

    vbox.add(usagePanel);

    vbox.add(WidgetFactory.makeHalfSpacer());
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

    return getPageData(XPATH_ROOT);
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

    if (rootXPath==null) rootXPath = "";
    rootXPath = rootXPath.trim();
    if (!rootXPath.endsWith("/")) rootXPath += "/";

    returnMap.clear();

    if ( !(usageField.getText().trim().equals("")) ) {

       returnMap.put(rootXPath + PARA_REL_XPATH, usageField.getText().trim());
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



  public boolean setPageData(OrderedMap data, String _xPathRoot) {

    if (_xPathRoot != null && _xPathRoot.trim().length() > 0) {

      throw new java.lang.IllegalArgumentException("METHOD IGNORES XPATHROOT!");
    }

    Log.debug(45,
              "UsageRights.setPageData() called with _xPathRoot = " + _xPathRoot
              + "\n Map = \n" + data);

    if (data == null || data.isEmpty()) {
      usageField.setText("");
      return true;
    }
    Iterator it = data.keySet().iterator();
    String nextXPath = null;
    String endingXPath = null;
    String nextVal = null;

    while (it.hasNext()) {

      nextXPath = (String)it.next();
      if (nextXPath==null) continue;

      nextVal = (String)data.get(nextXPath);
      if (nextVal==null) nextVal = "";

      // remove everything up to and including the last occurrence of
      // USAGE_ROOT to get relative xpaths, in case we're handling a
      // project elsewhere in the tree...
      endingXPath = nextXPath.substring(nextXPath.lastIndexOf(USAGE_ROOT)
                                  + USAGE_ROOT.length());

      if (endingXPath.startsWith(PARA_REL_XPATH)) {

        usageField.setText(nextVal);

      } else {

        Log.debug(20, "UsageRights: found xpath I can't handle: "+nextXPath);
        return false;
      }
    }
    return true;
  }
}
