/**
 *  '$RCSfile: Introduction.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2003-12-30 17:08:47 $'
 * '$Revision: 1.14 $'
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

import javax.swing.JLabel;
import javax.swing.BoxLayout;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.AbstractWizardPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;

public class Introduction extends AbstractWizardPage {

  public final String pageID     = DataPackageWizardInterface.INTRODUCTION;
  public final String nextPageID = DataPackageWizardInterface.GENERAL;
  public final String pageNumber = "1";

//////////////////////////////////////////////////////////

  public final String title      = "Welcome to the Data Package Wizard";
  public final String subtitle   = " ";


  public Introduction() {

    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
    "<p>If you do not have a basic understanding of metadata and "
    +"related concepts you should start by reading the <a>Ecological Metadata "
    +"Language (EML) Guide</a>. It provides background information on metadata "
    +"and contains metadata creation examples. This wizard creates a <i>Data "
    +"Package</i> that consists of the metadata that describes your data and the "
    +"data itself. The wizard uses a subset of EML to describe your data. If "
    +"additional metadata are needed to adequately document your data, use "
    +"<i>Morpho's EML Editor</i>. Before beginning you should have your data "
    +"(electronic or hardcopy format), and the following types of metadata "
    +"information in front of you: </p><br></br>"
    +"<li><b>Title and abstract</b><br></br> <br></br></li>"
    +"<li><b>Keywords</b> <br></br> <br></br></li>"
    +"<li><b>People and Organizations</b><br></br> <br></br></li>"
    +"<li><b>Project Information</b><br></br> <br></br></li>"
    +"<li><b>Data Usage Rights</b><br></br> <br></br></li></ul>", 20);

    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(desc);
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
  public boolean onAdvanceAction() { return true; }


  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public OrderedMap getPageData() {

    return null;
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
