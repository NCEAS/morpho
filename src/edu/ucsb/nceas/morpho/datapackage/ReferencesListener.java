/**
 *  '$RCSfile: ReferencesListener.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2004-03-29 06:07:47 $'
 * '$Revision: 1.1 $'
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


public interface ReferencesListener {

  /**
   * called back when ReferencesHandler class has detected a selection event
   * in the list of available IDs and has obtained the relevant referenced data,
   * either from the current datapackage, or from a remote package.  This local/
   * remote information, along with the full OrderedMap of xpath/value pairs, is
   * returned in the ReferenceSelectionEvent wrapper object.
   *
   * @param e ReferenceSelectionEvent
   */
  public void referenceSelected(ReferenceSelectionEvent e);

}
