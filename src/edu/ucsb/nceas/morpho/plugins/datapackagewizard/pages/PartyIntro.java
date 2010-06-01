/**
 *  '$RCSfile: PartyIntro.java,v $'
 *    Purpose: A class for Party Intro Screen
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.17 $'
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

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.utilities.OrderedMap;

import javax.swing.BoxLayout;
import javax.swing.JLabel;


public class PartyIntro extends AbstractUIPage{

  public final String pageID = DataPackageWizardInterface.PARTY_INTRO;
  public final String title = /*"People and Organizations"*/ Language.getInstance().getMessages("PeopleAndOrganizations");
  public final String subtitle = "";
  public final String pageNumber = "4";

  public PartyIntro() {
	nextPageID = DataPackageWizardInterface.PARTY_CREATOR_PAGE;
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.add(WidgetFactory.makeDefaultSpacer());

    JLabel desc = WidgetFactory.makeHTMLLabel(
        /*
        "<p><b>Identify the people and organizations responsible for the "
        + "data.</b> "
        + "In the next few screens you will need to provide the following "
        + "information:</p><br></br>"
        + "<li><b>Owner:</b> The person or organization who is credited with "
        + "creating the data. <br></br></li>"
        + "<li><b>Contact:</b> The primary person or organization to contact "
        + "with questions regarding the use or interpretation of the data "
        + "package.  <br></br></li>"
        + "<li><b>Associated parties:</b> These are people or organizations "
        + "that are in some way responsible for the data.  They may have "
        + "assisted in collection of or maintenance of the data or "
        + "they may have created documentation for the "
        + "data.<br></br></li>",
        */
    	"<p><b>" + Language.getInstance().getMessages("PeopleAndOrganizations.desc_1") +"</b>"	
    	+ Language.getInstance().getMessages("PeopleAndOrganizations.desc_2") + ":</p><br></br>"
    	+ "<li><b>" + Language.getInstance().getMessages("Owner")+ " : </b>" + Language.getInstance().getMessages("PeopleAndOrganizations.desc.Owner") +"<br></br></li>"
    	+ "<li><b>" + Language.getInstance().getMessages("Contact")+ " : </b>" + Language.getInstance().getMessages("PeopleAndOrganizations.desc.Contact") +"<br></br></li>"
    	+ "<li><b>" + Language.getInstance().getMessages("AssociatedParties")+ " : </b>" + Language.getInstance().getMessages("PeopleAndOrganizations.desc.AssociatedParties") +"<br></br></li>"
    	,13);

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
  public boolean onAdvanceAction() {
    return true;
  }

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
  public String getPageID() {
    return pageID;
  }

  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() {
    return title;
  }

  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() {
    return subtitle;
  }

  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() {
    return nextPageID;
  }

  /**
   *  Returns the serial number of the page
   *
   *  @return the serial number of the page
   */
  public String getPageNumber() {
    return pageNumber;
  }

  /**
   *  sets the OrderMap for this wizard page
   *
   *  @return
   */
    public boolean setPageData(OrderedMap data, String xPathRoot) { return false; }
}
