/**
 *        Name: PathQuery.java
 *     Purpose: A Class for creating a DataGuide JavaBean for use Desktop Client
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: PathQuery.java,v 1.1 2000-08-22 19:16:09 higgins Exp $'
 */

package edu.ucsb.nceas.querybean;


public class PathQuery
{
    public String docType;
    public String path;
    public String type;
    public String matchText;
    
 public PathQuery(String docType, String path, String type, String matchText) {
    this.docType = docType;
    this.path = path;
    this.type = type;
    this.matchText = matchText;
 }
 
 public boolean equals(PathQuery pq) {
    if (    (docType.equals(pq.docType)) &&
            (path.equals(pq.path)) &&
            (type.equals(pq.type)) &&
            (matchText.equals(pq.matchText))
       )  
       {
            return true;
       }
       else {return false;}
 }
 
 public boolean docTypeEquals(String dt) {
    if (docType.equals(dt)) {
        return true; }
    else {return false;}
 }

    
public String getXPath(){
    String match = matchText;
    
    if(type.equalsIgnoreCase("contains")) {
        return (path+"[contains(text(),\""+match+"\")]");
    }
    else if (type.equalsIgnoreCase("does not contain")) {
        return (path+"[not(contains(text(),\""+match+"\"))]");
    }
    else if (type.equalsIgnoreCase("is")) {
        return (path+"[text() = \""+match+"\"]");
    }
    else if (type.equalsIgnoreCase("is not")) {
        return (path+"[text() != \""+match+"\"]");
    }
    else if(type.equalsIgnoreCase("starts with")) {
        return (path+"[starts-with(text(),\""+match+"\")]");
    }
    
    else return (path+"[contains(text(),\""+match+"\")]");
}
    
    
    
}