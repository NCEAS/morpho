/**
 *  '$RCSfile: PathQueries.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-04-27 23:03:51 $'
 * '$Revision: 1.5 $'
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
// This class is designed to handle a collection of unique queries

import java.util.*;
import java.io.*;


public class PathQueries
{
    Vector queries;
 
    public PathQueries() {
        queries = new Vector();    
    }
    
    public int count() {
        return queries.size();
    }
    
    public boolean contains(PathQuery pq) {
        boolean flag = false;
        for (int i=0;i<count();i++) {
            PathQuery test = (PathQuery)queries.elementAt(i);
            if (test.equals(pq)) {
                flag = true;
                break;
            }
        }
        return flag;
    }
    
    // insert will add a PathQuery to the collection IF it is NOT already there!
    public void insert(PathQuery pq) {
        if(!contains(pq)) {
            queries.addElement(pq);
        }
    }
  
    // delete specified PathQuery
    public void delete(PathQuery pq) {
        for (int i=0;i<count();i++) {
            PathQuery test = (PathQuery)queries.elementAt(i);
            if (test.equals(pq)) {
                queries.removeElementAt(i);
                break;
            }
        }
    }
    
    
    // delete PathQueries with specified doctype
    public void delete_doctype(String dt) {
        for (int i=0;i<count();i++) {
            PathQuery test = (PathQuery)queries.elementAt(i);
            if ((test.docType).equals(dt)) {
                queries.removeElementAt(i);
                break;
            }
        }
    }

    // delete PathQueries with specified path
    public void delete_path(String path) {
        for (int i=0;i<count();i++) {
            PathQuery test = (PathQuery)queries.elementAt(i);
            if ((test.path).equals(path)) {
                queries.removeElementAt(i);
                break;
            }
        }
    }

    // delete PathQueries with specified path and doctype
    public void delete(String doctype, String path) {
        for (int i=0;i<count();i++) {
            PathQuery test = (PathQuery)queries.elementAt(i);
            if (((test.path).equals(path))&&(test.docType.equals(doctype))) {
                queries.removeElementAt(i);
                break;
            }
        }
    }
    
    // return a Vector of PathQuery objects which have a specific doc type
    public Vector getPQforDocType(String docType) {
        Vector pq = new Vector();
        for (int i=0;i<count();i++) {
            PathQuery test = (PathQuery)queries.elementAt(i);
            if (test.docTypeEquals(docType)) {
                pq.addElement(test);
            }
        }
        return pq;
    }
    
    //get PathQuery for a specific path; return null if no match
    public PathQuery getPQforPath(String path) {
        PathQuery pq = null;
        for (int i=0;i<count();i++) {
            PathQuery test = (PathQuery)queries.elementAt(i);
            if ((test.path).equals(path)) {
                pq = test;
                break;
            }
        }
        return pq;
    }
  
    public String buildXMLQuery(boolean andflag) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<pathquery version=\"1.0\">\n");
        sb.append("<meta_file_id>DG Query</meta_file_id>\n");
        sb.append("<querytitle>DG Query</querytitle>\n");
        String op = "UNION";
        if (andflag) {
            op = "INTERSECT"; }
        sb.append("<querygroup operator=\""+op+"\">\n");

        for (int i=0;i<count();i++) {
            PathQuery test = (PathQuery)queries.elementAt(i);
            String mode = getmode(test.type);
            sb.append("<queryterm casesensitive=\"false\" searchmode=\""+mode+"\">\n");
            sb.append("<value>"+test.matchText+"</value>\n");
            sb.append("<pathexpr>"+test.path+"</pathexpr>\n");
            sb.append("</queryterm>\n");
        }
        sb.append("</querygroup>\n");
        sb.append("</pathquery>");
        
/*		try{
		    String out = sb.toString();
		    StringReader sr = new StringReader(out);
		    File pathFile = new File("lib/pathFile.xml");
		    FileWriter fw = new FileWriter(pathFile);
            int c;
            while ((c = sr.read()) != -1) {
                fw.write(c);
            }
            sr.close();
            fw.close();
        }
        catch (Exception z) {}
*/        
//        System.out.println(out);
        
        
        return sb.toString();
    }
    
    public String[] getXPathArray() {
        String[] xp = new String[count()];
        for (int i=0;i<count();i++) {
            PathQuery test = (PathQuery)queries.elementAt(i);
            xp[i] = test.getXPath();
        }
        return xp;
    }
    
    
    
    private String getmode(String type) {
        String out = "contains";
        if (type.equals("is")) out="matches-exactly";
        if (type.equals("starts with")) out="starts-with";
        if (type.equals("ends with")) out="ends-with";
        return out;
    }
    
    
    
}
