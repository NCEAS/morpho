/**
 *  '$RCSfile: QueryTerm.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-09-06 01:25:53 $'
 * '$Revision: 1.4 $'
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

/** 
 * A single term in a query, containing the path to be searched, the
 * value to match, and any relevant search modifiers.
 */
public class QueryTerm 
{
  private boolean casesensitive = false;
  private String searchmode = null;
  private String value = null;
  private String pathexpr = null;

  /**
   * Construct a new instance of a query term for a free text search
   * (using the value only)
   *
   * @param casesensitive flag indicating whether case is used to match
   * @param searchmode determines what kind of substring match is performed
   *        (one of starts-with|ends-with|contains|equals)
   * @param value the text value to match
   */
  public QueryTerm(boolean casesensitive, String searchmode, 
                   String value) {
    this.casesensitive = casesensitive;
    this.searchmode = searchmode;
    this.value = value;
  }

  /**
   * Construct a new instance of a query term for a structured search
   * (matching the value only for those nodes in the pathexpr)
   *
   * @param casesensitive flag indicating whether case is used to match
   * @param searchmode determines what kind of substring match is performed
   *        (one of starts-with|ends-with|contains|equals)
   * @param value the text value to match
   * @param pathexpr the hierarchical path to the nodes to be searched
   */
  public QueryTerm(boolean casesensitive, String searchmode, 
                   String value, String pathexpr) {
    this(casesensitive, searchmode, value);
    this.pathexpr = pathexpr;
  }

  /** determine if the QueryTerm is case sensitive */
  public boolean isCaseSensitive() {
    return casesensitive;
  }

  /** set if the QueryTerm is case sensitive */
  public void setCaseSensitive(boolean isCaseSensitive) {
    casesensitive = isCaseSensitive;
  }

  /** get the searchmode parameter */
  public String getSearchMode() {
    return searchmode;
  }
 
  /** set the searchmode parameter */
  public void setSearchMode(String mode) {
    searchmode = mode;
  }

  /** get the Value parameter */
  public String getValue() {
    return value;
  }

  /** set the value parameter */
  public void setValue(String val) {
    value = val;
  }

  /** get the path expression parameter */
  public String getPathExpression() {
    return pathexpr;
  }

  /** set the path expression parameter */
  public void setPathExpression(String val) {
    pathexpr = val;
  }

  /**
   * create a SQL serialization of the query that this instance represents
   */
  public String printSQL(boolean useXMLIndex) {
    StringBuffer self = new StringBuffer();

    // Uppercase the search string if case match is not important
    String casevalue = null;
    String nodedataterm = null;

    if (casesensitive) {
      nodedataterm = "nodedata";
      casevalue = value;
    } else {
      nodedataterm = "UPPER(nodedata)";
      casevalue = value.toUpperCase();
    }

    // Add appropriate wildcards to search string
    String searchvalue = null;
    if (searchmode.equals("starts-with")) {
      searchvalue = casevalue + "%";
    } else if (searchmode.equals("ends-with")) {
      searchvalue = "%" + casevalue;
    } else if (searchmode.equals("contains")) {
      searchvalue = "%" + casevalue + "%";
    } else {
      searchvalue = casevalue;
    }

    self.append("SELECT DISTINCT docid FROM xml_nodes WHERE \n");

    if (pathexpr != null) {
      self.append(nodedataterm + " LIKE " + "'" + searchvalue + "' ");
      self.append("AND parentnodeid IN ");
      // use XML Index
      if ( useXMLIndex ) {
        self.append("(SELECT nodeid FROM xml_index WHERE path LIKE " + 
                    "'" +  pathexpr + "') " );
      // without using XML Index; using nested statements instead
      } else {
        self.append(useNestedStatements(pathexpr));
      }
    } else {
      self.append(nodedataterm + " LIKE " + "'" + searchvalue + "' ");
    }

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
    self.append("<queryterm");
    self.append(" searchmode=\""+searchmode+"\"");
    self.append(" casesensitive=\""+
                (new Boolean(casesensitive).booleanValue())+"\"");
    self.append(">\n");

    for (int i = 0; i < indent+2; i++) {
      self.append(" ");
    }
    self.append( "<value>" + value + "</value>\n");
    if (pathexpr != null) {
      for (int i = 0; i < indent+2; i++) {
        self.append(" ");
      }
      self.append( "<pathexpr>" + pathexpr + "</pathexpr>\n");
    }
    for (int i = 0; i < indent; i++) {
      self.append(" ");
    }
    self.append("</queryterm>\n");

    return self.toString();
  }

  /* 
   * Constraint the query with @pathexp without using the XML Index,
   * but nested SQL statements instead. The query migth be slower.
   */
  private String useNestedStatements(String pathexpr)
  {
    StringBuffer nestedStmts = new StringBuffer();
    Vector nodes = new Vector();
    String path = pathexpr;
    int inx = 0;

    do {
      inx = path.lastIndexOf("/");
      nodes.addElement(path.substring(inx+1));
      path = path.substring(0, Math.abs(inx));
    } while ( inx > 0 );
    
    // nested statements
    int i = 0;
    for (i = 0; i < nodes.size()-1; i++) {
      nestedStmts.append("(SELECT nodeid FROM xml_nodes" + 
                         " WHERE nodename LIKE '" +
                           (String)nodes.elementAt(i) + "'" +
                         " AND parentnodeid IN ");
    }
    // for the last statement: it is without " AND parentnodeid IN "
    nestedStmts.append("(SELECT nodeid FROM xml_nodes" + 
                       " WHERE nodename LIKE '" +
                       (String)nodes.elementAt(i) + "'" );
    // node.size() number of closing brackets
    for (i = 0; i < nodes.size(); i++) {
      nestedStmts.append(")");
    }


    return nestedStmts.toString();
  }

  /**
   * create a String description of the query that this instance represents.
   * This should become a way to get the XML serialization of the query.
   */
  public String toString() {

    return this.printSQL(true);
  }
}
