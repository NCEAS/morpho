/**
 *  '$RCSfile: QueryGroup.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-03 01:51:58 $'
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

import java.util.Vector;
import java.util.Enumeration;

/** 
 * A group of terms and other groups in a query, allowing arbitrarily
 * nested hierarchiies that can be combined using union or intersection.
 */
public class QueryGroup 
{
  private String operator = null;  // indicates how query terms are combined
  private Vector children = null;  // the list of query terms and groups

  /** 
   * construct a new QueryGroup 
   *
   * @param operator the boolean conector used to connect query terms 
   *                    in this query group
   */
  public QueryGroup(String operator) {
    this.operator = operator;
    children = new Vector();
  }

  /** 
   * Add a child QueryGroup to this QueryGroup
   *
   * @param qgroup the query group to be added to the list of terms
   */
  public void addChild(QueryGroup qgroup) {
    children.add((Object)qgroup); 
  }

  /**
   * Add a child QueryTerm to this QueryGroup
   *
   * @param qterm the query term to be added to the list of terms
   */
  public void addChild(QueryTerm qterm) {
    children.add((Object)qterm); 
  }

  /**
   * Retrieve an Enumeration of query terms for this QueryGroup
   */
  public Enumeration getChildren() {
    return children.elements();
  }
 
  /**
   * create a SQL serialization of the query that this instance represents
   */
  public String printSQL(boolean useXMLIndex) {
    StringBuffer self = new StringBuffer();
    boolean first = true;

    self.append("(");

    Enumeration en= getChildren();
    while (en.hasMoreElements()) {
      Object qobject = en.nextElement();
      if (first) {
        first = false;
      } else {
        self.append(" " + operator + " ");
      }
      if (qobject instanceof QueryGroup) {
        QueryGroup qg = (QueryGroup)qobject;
        self.append(qg.printSQL(useXMLIndex));
      } else if (qobject instanceof QueryTerm) {
        QueryTerm qt = (QueryTerm)qobject;
        self.append(qt.printSQL(useXMLIndex));
      } else {
        System.err.println("qobject wrong type: fatal error");
      }
    }
    self.append(") \n");
    return self.toString();
  }

  /**
   * create a String description of the query that this instance represents.
   * This should become a way to get the XML serialization of the query.
   */
  public String toString() {
    StringBuffer self = new StringBuffer();

    self.append("  (Query group operator=" + operator + "\n");
    Enumeration en= getChildren();
    while (en.hasMoreElements()) {
      Object qobject = en.nextElement();
      self.append(qobject);
    }
    self.append("  )\n");
    return self.toString();
  }
}
