/**
 *  '$RCSfile: ReferenceSelectionEvent.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-30 04:57:02 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.utilities.OrderedMap;


/**
 * Used when ReferencesHandler class has detected a selection event
 * in the list of available IDs and has obtained the relevant referenced data,
 * either from the current datapackage, or from a remote package.  The local/
 * remote information, along with the full OrderedMap of xpath/value pairs, is
 * encapsulated in an instance of this ReferenceSelectionEvent class and
 * returned to the registered ReferencesListeners
 */
public class ReferenceSelectionEvent {

  public static final short UNDEFINED              = 0;
  public static final short CURRENT_DATA_PACKAGE   = 10;
  public static final short DIFFERENT_DATA_PACKAGE = 20;

  private OrderedMap referencedData;
  private String refID;
  private short location;

  public ReferenceSelectionEvent() {}

  public ReferenceSelectionEvent(String refID, short location, OrderedMap referencedData) {

    this.setReferenceID(refID);
    this.setLocation(location);
    this.setXPathValsMap(referencedData);
  }

  /**
   * Set referenced id string
   *
   * @param refID String
   */
  public void setReferenceID(String refID) {

    this.refID = refID;
  }


  /**
   * get referenced id string
   *
   * @return refID String
   */
  public String getReferenceID() {

    return this.refID;
  }


  /**
   * Set location of original metadata to be referenced
   *
   * @param location short value - ReferenceSelectionEvent.CURRENT_DATA_PACKAGE
   * or ReferenceSelectionEvent.DIFFERENT_DATA_PACKAGE
   */
  public void setLocation(short location) {

    this.location = location;
  }


  /**
   * get location of original metadata to be referenced
   *
   * @return location short value - ReferenceSelectionEvent.CURRENT_DATA_PACKAGE
   * or ReferenceSelectionEvent.DIFFERENT_DATA_PACKAGE
   */
  public short getLocation() {

    return this.location;
  }


  /**
   * Set OrderedMap of xpath/value pairs
   *
   * @param referencedData OrderedMap
   */
  public void setXPathValsMap(OrderedMap referencedData) {

    this.referencedData = referencedData;
  }


  /**
   * Get OrderedMap of xpath/value pairs
   *
   * @return OrderedMap
   */
  public OrderedMap getXPathValsMap() {

    return this.referencedData;
  }

}
