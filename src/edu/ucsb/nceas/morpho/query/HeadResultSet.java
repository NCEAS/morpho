/**
 *  '$RCSfile: HeadResultSet.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-06-15 09:02:33 $'
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

package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.framework.*;

import java.io.InputStream;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;


/**
 * A HeadResultSet encapsulates the list of results returned from either a
 * local query or a Metacat query, but only presents the most recent revision
 * of a document as part of the Table Model. 
 */
public class HeadResultSet extends ResultSet
{
  /** Store the most recent revision in a vector */
  private Vector headResultsVector = null;

  /**
   * Construct a HeadResultSet instance given a query object and a
   * InputStream that represents an XML encoding of the results.
   */
  public HeadResultSet(Query query, String source, 
                       InputStream resultsXMLStream, ClientFramework cf)
  {
    super(query, source, resultsXMLStream, cf);
    consolidateResults();
  }


  /**
   * Construct a HeadResultSet instance from a vector of vectors;
   * for use with LocalQuery
   */
  public HeadResultSet(Query query, String source, 
                       Vector vec, ClientFramework cf)
  {
    super(query, source, vec, cf);
    consolidateResults();
  }


  /**
   * Return the number of records in this result set
   */
  public int getRowCount()
  {
    return headResultsVector.size();
  }

  /**
   * Determine the value of a column by its row and column index
   */
  public Object getValueAt(int row, int col)
  {
    Object value = null;
    try {
      Vector rowVector = (Vector)headResultsVector.get(row);
      value = rowVector.get(col);
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      String emptyString = "";
      value = null;
    } catch (NullPointerException npe) {
      String emptyString = "";
      value = emptyString;
    }
    return value;
  }

  /**
   * Open a given row index of the result set using a delegated handler class
   */
  public void openResultRecord(int row)
  {
    try {
      Vector rowVector = (Vector)headResultsVector.get(row);
      openResultRecord(rowVector);
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      ClientFramework.debug(1, "array index out of bounds");
    }
  }

  /**
   * Merge a ResultSet onto this one using the docid as the join column
   */
  public void merge(ResultSet r2)
  {
    super.merge(r2);
    consolidateResults();
  }

  /**
   * Consolidate the results Vector to produce a new Vector with only the
   * most recent revision of each document in the Vector. Warning: this 
   * implementation doesn't preserve sort order of the results
   */
  private void consolidateResults() 
  {
    int numHeaders = getColumnCount();
    HashMap maxRevHash = new HashMap();
    HashMap maxRevRow = new HashMap();
    for (int i=0; i<resultsVector.size(); i++) {
      // Get the row, and its docid, parse out the rev #
      Vector rowVector = (Vector)resultsVector.get(i);
      String docid = (String)rowVector.get(numHeaders+2);
      String family = docid.substring(0, docid.lastIndexOf("."));
      String rev = docid.substring(docid.lastIndexOf(".")+1);
      Integer currentRev = new Integer(rev);
      Integer maxRev = (Integer)maxRevHash.get(family);

      // save the highest rev
      if (maxRev == null || (currentRev.compareTo(maxRev) > 0)) {
        // Store the familyid + current rev in a hash
        maxRevHash.put(family, currentRev);
        // Store the familyid + row for the current highest rev in a hash
        maxRevRow.put(family, rowVector);
      }
    }
   
    // Create the new consolidated vector of rows
    headResultsVector = new Vector(maxRevRow.values());
  }
}
