/**
 *  '$RCSfile: PathQuery.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-04-27 23:03:51 $'
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

package edu.ucsb.nceas.morpho.query;


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
