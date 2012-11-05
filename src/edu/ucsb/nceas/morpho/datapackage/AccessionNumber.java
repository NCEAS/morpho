/**
 *  '$RCSfile: AccessionNumber.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-08-01 02:48:55 $'
 * '$Revision: 1.18 $'
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

import java.util.Vector;

import edu.ucsb.nceas.morpho.Morpho;

/**
 * Class that implements Accession Number utility functions for morpho
 */
public class AccessionNumber 
{

  private static AccessionNumber instance = null;
  
  private AccessionNumber() {}

  public static AccessionNumber getInstance() {
	  if (instance == null) {
		  instance = new AccessionNumber();
	  }
	  return instance;
  }
  
  /**
   * Returns a vector with all components of the accession number.  The vector
   * looks like:
   * [scope, id, rev, separator]
   * ex: [nceas, 5, 2, .]
   * @param id the id to return the parts of
   */
  public Vector<String> getParts(String id)
  {
    String separator = Morpho.thisStaticInstance.getProfile().get("separator", 0);
    String scope = id.substring(0, id.indexOf(separator));
    String idpart = id.substring(id.indexOf(separator)+1, 
                                 id.lastIndexOf(separator));
    String rev = id.substring(id.lastIndexOf(separator) + 1, id.length());
    Vector<String> v = new Vector<String>();
    v.addElement(scope);
    v.addElement(idpart);
    v.addElement(rev);
    v.addElement(separator);
    return v;
  }

}
