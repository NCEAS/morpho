/**
 *  '$RCSfile: QueryGroup.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-10-29 23:31:35 $'
 * '$Revision: 1.7 $'
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

import edu.ucsb.nceas.morpho.framework.ClientFramework;

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
    children.addElement((Object)qgroup); 
  }

  /**
   * Add a child QueryTerm to this QueryGroup
   *
   * @param qterm the query term to be added to the list of terms
   */
  public void addChild(QueryTerm qterm) {
    children.addElement((Object)qterm); 
  }

  /**
   * Retrieve an Enumeration of query terms for this QueryGroup
   */
  public Enumeration getChildren() {
    return children.elements();
  }

  /**
   * Retrieve the operator for this QueryGroup
   */
  public String getOperator() {
    return operator;
  }
 
  /**
   * Set the operator for this QueryGroup
   */
  public void setOperator(String op) {
    operator = op;
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
        ClientFramework.debug(4, "qobject wrong type: fatal error");
      }
    }
    self.append(") \n");
    return self.toString();
  }

  /**
   * create a XML serialization of the query that this instance represents
   */
  public String toXml(int indent) {
    StringBuffer self = new StringBuffer();

    for (int i = 0; i < indent; i++) {
      self.append(" ");
    }
    self.append("<querygroup operator=\"");
    self.append(operator);
    self.append("\">\n");

    Enumeration en= getChildren();
    while (en.hasMoreElements()) {
      Object qobject = en.nextElement();
      if (qobject instanceof QueryGroup) {
        QueryGroup qg = (QueryGroup)qobject;
        self.append(qg.toXml(indent+2));
      } else if (qobject instanceof QueryTerm) {
        QueryTerm qt = (QueryTerm)qobject;
        self.append(qt.toXml(indent+2));
      } else {
        ClientFramework.debug(4, "qobject wrong type: fatal error");
      }
    }
    for (int i = 0; i < indent; i++) {
      self.append(" ");
    }
    self.append("</querygroup>\n");

    return self.toString();
  }

  /**
   * create a String description of the query that this instance represents.
   * This is a way to get the XML serialization of the query group.
   */
  public String toString() {
    return toXml(0);
  }
}
