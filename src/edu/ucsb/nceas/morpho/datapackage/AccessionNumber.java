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
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierManager;

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
	 * Parses id and increments revision number
	 * 
	 * @param id the id to parse and increment revision
	 */
	public String incRev(String id) {
		String sep = Morpho.thisStaticInstance.getProfile().get("separator", 0);
		int count = 0;
		for (int i = 0; i < id.length(); i++) {
			if (id.charAt(i) == sep.trim().charAt(0)) {
				count++;
			}
		}

		// no rev was given, so we use 1
		if (count == 1) {
			return id + sep + "1";
		}

		// we have all three parts and can parse  them
		Vector<String> idParts = getParts(id);
		
		String revNumStr = idParts.get(2);
		Integer revNum = new Integer(revNumStr);
		int rev = revNum.intValue();
		rev++;
		return idParts.get(0) + sep + idParts.get(1) + sep + rev;
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
  
  /**
   * Method to get only the beginning (scope and id parts) of
   * a given full id
   * @param fullId the accessionNumber to strip the rev off of
   * @return scope+separator+id
   */
  public String getIdNoRev(String fullId) {
	  Vector<String> idVec = getParts(fullId);
      String scope = (String)idVec.elementAt(0);
      String id = (String)idVec.elementAt(1);
      //String rev = (String)idVec.elementAt(2);
      String sep = (String)idVec.elementAt(3);
      return scope + sep + id;
  }
  
  /**
   * @deprecated included in AccessionNumber class for easier removal
   * Given a docid, caculate the number of its previous versions
   * @param docId,  docId need to caculate previouse version
   */
  public int getNumberOfPrevVersions(String docId)
  {
      int prevVersions = 0;
      int iii = docId.lastIndexOf(IdentifierManager.DOT);
      String ver = docId.substring(iii+1,docId.length());
      prevVersions = (new Integer(ver)).intValue();
      prevVersions = prevVersions - 1;
      return prevVersions;
  }

}
