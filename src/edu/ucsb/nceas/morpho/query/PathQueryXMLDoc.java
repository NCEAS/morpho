/**
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-25 01:36:58 $'
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

// use to create a XML doc for a pathquery
// use 'start_querygroup to begin a group
// use add_queryterm repeatedly as needed
// and then 'end_querygroup'
// repeat adding groups if needed
// end with 'end_query'

package edu.ucsb.nceas.morpho.query;


import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration;
import edu.ucsb.nceas.morpho.framework.*;

/**
 * PathQueryXML is a utility class that helps build
 * a valid pathQuery XML document. One uses it by creating
 * a new instance of the class and then adding QueryTerms
 * and QueryGroups to build a complete query.
 * 
 * @author higgins
 */
public class PathQueryXMLDoc
{
  
  
  /** The configuration options object reference from the framework */
  private ConfigXML config = null;
  
  /** various variable used to construct pathQuery document */
  private String Intro = "<?xml version=\"1.0\"?>\n<pathquery version=\"1.0\">\n";
  private String querytitle="query_title";
  private String meta_file_id="meta_file_id";
    
  private StringBuffer gStart = new StringBuffer();
  private StringBuffer gEnd = new StringBuffer();
  private Stack endStack  = new Stack();
    
  private StringBuffer qXML;
  private StringBuffer qHeader;
  private StringBuffer qBody;
  private StringBuffer qEnd;
    
  private boolean caseflag = true;
    
  private Vector searchDocTypes;
  private Vector returnDocTypes;
  private Vector returnFields;
    
public PathQueryXMLDoc(ConfigXML config) {
  this.config = config;   
  searchDocTypes = config.get("doctype");
  returnDocTypes = config.get("returndoc");
  returnFields = config.get("returnfield");
  meta_file_id = (new Long(System.currentTimeMillis())).toString();
  build_header();
  build_end();
  qBody = new StringBuffer();
}

public void set_querytitle(String title) {
    this.querytitle = title;
    build_header();
}
public void set_meta_file_id(String id) {
    this.meta_file_id = id;
    build_header();
}

public void set_caseflag(boolean flg) {
    this.caseflag = flg;    
}

// use to end a querygroup and 'pop' up a level
public void end_querygroup() {
    if (!endStack.isEmpty()) {
        gStart.append((String)endStack.pop());
    }
}

public void add_querygroup(String oper) {  // only AND and OR allow for oper
    String op = "UNION";
    if (oper.equalsIgnoreCase("INTERSECT")) op = "INTERSECT";
    if (!endStack.isEmpty()) {
        gStart.append((String)endStack.pop());
    }
    gStart.append("<querygroup operator=\""+op+"\">\n");
    endStack.push("</querygroup>\n");
}

public void add_querygroup_asChild(String oper) {  
    String op = "UNION";
    if (oper.equalsIgnoreCase("INTERSECT")) op = "INTERSECT";
    gStart.append("<querygroup operator=\""+op+"\">\n");
    endStack.push("</querygroup>\n");
}

public void add_returndoctype(String val) {
    String rdt = "<returndoctype>"+val+"</returndoctype>" ;
    gStart.append(rdt);
}

public void add_queryterm(String value, String path) {
  String query = "  <queryterm casesensitive=\"false\" searchmode=\"contains\">\n";
  query = query + "   <value>"+value+"</value>\n";
  if (!path.equals("//*")) {
    query = query + "   <pathexpr>"+path+"</pathexpr>\n";
  }
  query = query + "  </queryterm>\n";
  gStart.append(query);
}
public void add_queryterm(String value, String path, String mode, boolean casesensitive) {
    String cs = "true";
    if (!casesensitive) cs = "false"; 
    String query = "  <queryterm casesensitive=\""+cs+"\" searchmode=\""+mode+"\">\n";
    query = query + "   <value>"+value+"</value>\n";
    if (!path.equals("//*")) {
        query = query + "   <pathexpr>"+path+"</pathexpr>\n";
    }
    query = query + "  </queryterm>\n";
    gStart.append(query);
}

public void end_query() {
    qXML = new StringBuffer(qHeader.toString());
    while (!endStack.isEmpty())
    {
        gStart.append((String)endStack.pop());
    }
    qXML.append(gStart);
    qXML.append(gEnd);
    qXML.append(qEnd);
}

public void end_query_plus(String op) {
    qXML = new StringBuffer(qHeader.toString());
    qXML.append("<querygroup operator=\""+op+"\">\n");
    qXML.append(qBody);
    qXML.append("</querygroup>\n");
    qXML.append(qEnd);
    
}

public String get_XML() {
    return qXML.toString();
}

private void build_header() {
    qHeader = new StringBuffer(Intro);
    qHeader.append("<meta_file_id>"+meta_file_id+"</meta_file_id>\n");
    qHeader.append("<querytitle>"+querytitle+"</querytitle>\n");
    
    for (Enumeration e = searchDocTypes.elements() ; e.hasMoreElements() ;) {
         qHeader.append("<doctype>"+e.nextElement()+"</doctype>\n"); 
     }
    if (!returnDocTypes.contains("any")) { 
    for (Enumeration e = returnDocTypes.elements() ; e.hasMoreElements() ;) {
         qHeader.append("<returndoctype>"+e.nextElement()+"</returndoctype>\n"); 
     }
    }
    for (Enumeration e = returnFields.elements() ; e.hasMoreElements() ;) {
         qHeader.append("<returnfield>"+e.nextElement()+"</returnfield>\n"); 
     }
    
    
}

private void build_end() {
    qEnd = new StringBuffer("</pathquery>");   
}



	//{{DECLARE_CONTROLS
	//}}
}
