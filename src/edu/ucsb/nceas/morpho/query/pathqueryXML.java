/**
 *        Name: pathqueryXML.java
 *     Purpose: A Class for creating pathQuery XML documents
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: pathqueryXML.java,v 1.5 2000-11-20 17:44:38 higgins Exp $'
 */


// use to create a XML doc for a pathquery
// use 'start_querygroup to begin a group
// use add_queryterm repeatedly as needed
// and then 'end_querygroup'
// repeat adding groups if needed
// end with 'end_query'

package edu.ucsb.nceas.querybean;


import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration;
import edu.ucsb.nceas.dtclient.*;

public class pathqueryXML
{
    String Intro = "<?xml version=\"1.0\"?>\n<pathquery version=\"1.0\">\n";
    String querytitle="query_title";
    String meta_file_id="meta_file_id";
    
    StringBuffer gStart = new StringBuffer();
    StringBuffer gEnd = new StringBuffer();
    Stack endStack  = new Stack();
    
    StringBuffer qXML;
    StringBuffer qHeader;
    StringBuffer qBody;
    StringBuffer qEnd;
    
    boolean caseflag = true;
    
    Vector searchDocTypes;
    Vector returnDocTypes;
    Vector returnFields;
    
public pathqueryXML() {
    ConfigXML config = new ConfigXML("config.xml");
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

/*
public void start_querygroup(String oper) {  // only AND and OR allow for oper
    String op = "UNION";
    if (oper.equalsIgnoreCase("INTERSECT")) op = "INTERSECT";
    gStart = new StringBuffer("<querygroup operator=\""+op+"\">\n");
}
*/

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

public void add_querygroup_asChild(String oper) {  // only AND and OR allow for oper
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
    String query = "  <queryterm casesensitive=\"true\" searchmode=\"contains\">\n";
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
    for (Enumeration e = returnDocTypes.elements() ; e.hasMoreElements() ;) {
         qHeader.append("<returndoc>"+e.nextElement()+"</returndoc>\n"); 
     }
    for (Enumeration e = returnFields.elements() ; e.hasMoreElements() ;) {
         qHeader.append("<returnfield>"+e.nextElement()+"</returnfield>\n"); 
     }
    
    
}

private void build_end() {
    qEnd = new StringBuffer("</pathquery>");   
}



}