/**
 *  '$RCSfile: HeadResultSet.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2005-02-22 23:21:51 $'
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

package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.*;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
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
                       InputStream resultsXMLStream, Morpho morpho)
  {
    super(query, source, resultsXMLStream, morpho);
    consolidateResults();
  }


  /**
   * Construct a HeadResultSet instance from a vector of vectors;
   * for use with LocalQuery
   */
  public HeadResultSet(Query query, String source,
                       Vector vec, Morpho morpho)
  {
    super(query, source, vec, morpho);
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
   *  get the resultsVector
   */
  public Vector getResultsVector() {
    return headResultsVector;
  }

  /**
   *  Set results vector
   * @param vector Vector
   */
  public void setResultsVector(Vector vector)
  {
    headResultsVector = vector;
  }

  /**
   * Determine the value of a column by its row and column index
   */
  public Object getValueAt(int row, int col)
  {

    Object value = null;
    try {
      Vector rowVector = (Vector)headResultsVector.elementAt(row);
      // The oder of header is different to resultsVector, so we need a
      // conversion
      value = rowVector.elementAt(lookupResultsVectorIndex(col));

      // Add icon rather than ture or false value to col6 and col7
      if (col == 6)
      {
        // cast value to Boolean object
        Boolean isLocally = (Boolean)value;
        if (isLocally.booleanValue())
        {
          // If is local, the value will be a local icon
          value = localIcon;
        }//if
        else
        {
          // If there isnot local, value is empty String
          value = blankIcon;
        }//else
      }//if

      // Add icon for col6 and col7
      if (col == 7)
      {
        // cast value to Boolean object
        Boolean isNet = (Boolean)value;
        if (isNet.booleanValue())
        {
          // If is local, the value will be a local icon
          value = metacatIcon;
        }//if
        else
        {
          // If there isnot local, value is empty String
          value = blankIcon;
        }//else
      }//if

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
      Vector rowVector = (Vector)headResultsVector.elementAt(row);
      openResultRecord(rowVector);
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      Log.debug(1, "array index out of bounds");
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
  * Merge a ResultSet onto this one using the docid as the join column
  */
  public void merge(Vector vector2)
  {
    super.merge(vector2);
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
    Hashtable maxRevHash = new Hashtable();
    Hashtable maxRevRow = new Hashtable();

    for (int i=0; i<resultsVector.size(); i++) {
      // Get the row, and its docid, parse out the rev #
      Vector rowVector = null;
      String docid = null;
      String family = null;
      String rev = null;
      Integer currentRev = null;
      Integer maxRev = null;
      try
      {

        rowVector = (Vector)resultsVector.elementAt(i);
        docid = (String)rowVector.elementAt(DOCIDINDEX);
        family = docid.substring(0, docid.lastIndexOf("."));
        rev = docid.substring(docid.lastIndexOf(".")+1);
        currentRev = new Integer(rev);
        maxRev = (Integer)maxRevHash.get(family);
      }
      catch (Exception e)
      {}
      int currentRevint = 0;
      int maxRevint = 0;
      if (currentRev!=null) {
        currentRevint = currentRev.intValue();
      }
      if (maxRev!=null) {
        maxRevint = maxRev.intValue();
      }

      // save the highest rev
//DFH      if (maxRev == null || (currentRev.compareTo(maxRev) > 0)) {
      if ((maxRev == null || (currentRevint>maxRevint)) && family != null) {
        // Store the familyid + current rev in a hash
        maxRevHash.put(family, currentRev);
        // Store the familyid + row for the current highest rev in a hash
        maxRevRow.put(family, rowVector);
      }
    }

    // Create the new consolidated vector of rows
//DFH    headResultsVector = new Vector(maxRevRow.values());
      headResultsVector = new Vector();
      Enumeration enumeration = maxRevRow.elements();
      while (enumeration.hasMoreElements()) {
          headResultsVector.addElement(enumeration.nextElement());
      }

  }

   /**
   * Method implements from SortTableModel. To make sure a col can be sort
   * or not. We decide it always be sortable.
   * @param col, the index of column which need to be sorted
   * @param order, the sort order
   */
  public void sortTableByColumn(int col, String order)
  {
    sortVector(headResultsVector, col, order);
  }//sortColumn
}
