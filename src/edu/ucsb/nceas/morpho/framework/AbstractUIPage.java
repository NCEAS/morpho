/**
 *  '$RCSfile: AbstractUIPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-18 00:23:33 $'
 * '$Revision: 1.2 $'
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

package edu.ucsb.nceas.morpho.framework;


import javax.swing.JPanel;
import edu.ucsb.nceas.utilities.OrderedMap;


/**
 *  Class       AbstractUIPage
 *
 */
public abstract class AbstractUIPage extends JPanel {

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public abstract String getPageID();


  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public abstract String getTitle();


  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public abstract String getSubtitle();


  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public abstract String getNextPageID();


  /**
   *  Returns the serial number of the page
   *
   *  @return the serial number of the page
   */
  public abstract String getPageNumber();


  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public abstract void onLoadAction();


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public abstract void onRewindAction();


  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public abstract boolean onAdvanceAction();


  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular wizard page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public abstract OrderedMap getPageData();


  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular wizard page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular wizard page
   */
  public abstract OrderedMap getPageData(String rootXPath);


  /**
   *  sets the fields in the waird page using the Map object
   *  that contains all the key/value paired
   *
   *  @param   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  public abstract void setPageData(OrderedMap data);

}