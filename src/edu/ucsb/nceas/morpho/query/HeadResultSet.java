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
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
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
  //private Vector headResultsVector = null;



  /**
   * Construct a HeadResultSet instance given a query object and a
   * InputStream that represents an XML encoding of the results.
   */
  public HeadResultSet(Query query, String localStatus, String metacatStatus,
                       InputStream resultsXMLStream, Morpho morpho)
  {
    super(query, localStatus, metacatStatus, resultsXMLStream, morpho);
    //consolidateResults();
  }


  /**
   * Construct a HeadResultSet instance from a vector of vectors;
   * for use with LocalQuery
   */
  public HeadResultSet(Query query,
                       Vector vec, Morpho morpho)
  {
    super(query, vec, morpho);
    //consolidateResults();

  }


  /**
   * Return the number of records in this result set
   */
  /*public int getRowCount()
  {
    return headResultsVector.size();
  }*/

  /**
   *  get the resultsVector
   */
  /*public Vector getResultsVector() {
    return headResultsVector;
  }*/

  /**
   *  Set results vector
   * @param vector Vector
   */
  /*public void setResultsVector(Vector vector)
  {
    headResultsVector = vector;
  }*/

  /**
   * Determine the value of a column by its row and column index
   */
  public Object getValueAt(int row, int col)
  {

    Object value = null;
    try {
      //Vector rowVector = (Vector)headResultsVector.elementAt(row);
      Vector rowVector = (Vector)resultsVector.elementAt(row);
      // The oder of header is different to resultsVector, so we need a
      // conversion
      value = rowVector.elementAt(lookupResultsVectorIndex(col));

      // Add local icons
      if (col == 6)
      {
        
        String localStatus = (String)value;
        if (localStatus != null && localStatus.equals(QueryRefreshInterface.LOCALCOMPLETE))
        {
          // If is local, the value will be a local icon
          value = localIcon;
        }//if
        else if(localStatus != null && localStatus.equals(QueryRefreshInterface.LOCALUSERSAVEDINCOMPLETE))
        {
          // If there isnot local, value is empty String
          value = localUserSavedIncompleteIcon;
        }
        else if(localStatus != null && localStatus.equals(QueryRefreshInterface.LOCALAUTOSAVEDINCOMPLETE))
        {
          value = localAutoSavedIncompleteIcon;
        }
        else
        {
          value = blankIcon;
        }
          
          
      }//if

      // Add network icons
      if (col == 7)
      {
        // cast value to Boolean object
        String netStatus = (String)value;
        if (netStatus != null && netStatus.equals(QueryRefreshInterface.NETWWORKCOMPLETE))
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
  * Merge a ResultSet onto this one using the docid as the join column.
  * Merging also conslidate results
  */
  public void mergeWithMetacatResults(Vector metacatVector)
  {
    mergeWithCompleteDocResultVectors(metacatVector);
  }

   /**
   * Method implements from SortTableModel. To make sure a col can be sort
   * or not. We decide it always be sortable.
   * @param col, the index of column which need to be sorted
   * @param order, the sort order
   */
  public void sortTableByColumn(int col, String order)
  {
    sortVector(resultsVector, col, order);
  }//sortColumn
}
