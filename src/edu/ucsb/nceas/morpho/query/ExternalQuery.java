/**
 *  '$RCSfile: ExternalQuery.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-04-27 23:03:50 $'
 * '$Revision: 1.12 $'
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

/*
*   Current MetaCat result set returns a <document> element for each 'hit'
*   in query. That <document> element has 6 fixed children: <docid>, <docname>,
*   <doctype>, <createdate>, and <updatadate>
*   Other child elements are determined by query and are returned as <param>
*   elements with a "name" attribute and a value in the text.
*/


package edu.ucsb.nceas.morpho.query;

import java.io.*;
import java.util.Vector;
import edu.ucsb.nceas.morpho.framework.*;

import org.w3c.dom.*;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import javax.swing.*;
import javax.swing.table.*;
import java.util.Stack;
import java.util.Hashtable;

public class ExternalQuery implements ContentHandler
{
    InputStream is;   
    private Stack elementStack = null;
    JTable RSTable = null;
    String[] headers = {"Doc ID", "Document Name", "Document Type", "Document Title"};
    DefaultTableModel dtm;
    String docid;
    String docname;
    String doctype;
    String doctitle;
    String paramName;
    String relationtype;
    String relationdoc;
    String relationdoctype;
    Hashtable params;
    Hashtable relations; 
                        // used to save relation doc info for each doc returned
                         // key is docid, value is a Vector of string arrays
                         // each string array is (relationtype,relationdoc,relationdoctype)
    Vector relationsVector; 
    
    Vector returnFields; // return field path names
    int num_cols_to_remove = 3;
    
public ExternalQuery(InputStream is, int cols) {
    relations = new Hashtable(); 
    this.is = is;
    ConfigXML config = new ConfigXML("lib/config.xml");
    returnFields = config.get("returnfield");
    int cnt;
    if (returnFields==null) {
        cnt = 0;
    }
    else {
        cnt = returnFields.size();
    }
    
    headers = new String[4+cnt];  // assume at least 4 fields returned
    headers[0] = "Doc ID";
    headers[1] = "Document Name";
    headers[2] = "Document Type";
    headers[3] = "Document Title";
    for (int i=0;i<cnt;i++) {
        headers[4+i] = getLastPathElement((String)returnFields.elementAt(i));
    }
    dtm = new DefaultTableModel(headers,0);
    RSTable = new JTable(dtm);
    TableColumnModel tcm = RSTable.getColumnModel();
    removeFirstNColumns(tcm,cols);
    String parserName = "org.apache.xerces.parsers.SAXParser";
    XMLReader parser = null;

    // Set up the SAX document handlers for parsing
    try {
          // Get an instance of the parser
          parser = XMLReaderFactory.createXMLReader(parserName);
          // Set the ContentHandler to this instance
          parser.setContentHandler(this);
          parser.parse(new InputSource(is));
        } catch (Exception e) {
           System.err.println(e.toString());
        }
}   
    
public ExternalQuery(InputStream is) {
    relations = new Hashtable(); 
    this.is = is;
    ConfigXML config = new ConfigXML("lib/config.xml");
    returnFields = config.get("returnfield");
    int cnt;
    if (returnFields==null) {
        cnt = 0;
    }
    else {
        cnt = returnFields.size();
    }
    
    headers = new String[4+cnt];  // assume at least 4 fields returned
    headers[0] = "Doc ID";
    headers[1] = "Document Name";
    headers[2] = "Document Type";
    headers[3] = "Document Title";
    for (int i=0;i<cnt;i++) {
        headers[4+i] = getLastPathElement((String)returnFields.elementAt(i));
    }
    dtm = new DefaultTableModel(headers,0);
    RSTable = new JTable(dtm);
    TableColumnModel tcm = RSTable.getColumnModel();
    removeFirstNColumns(tcm,num_cols_to_remove);
    String parserName = "org.apache.xerces.parsers.SAXParser";
    XMLReader parser = null;

    // Set up the SAX document handlers for parsing
    try {
          // Get an instance of the parser
          parser = XMLReaderFactory.createXMLReader(parserName);
          // Set the ContentHandler to this instance
          parser.setContentHandler(this);
          parser.parse(new InputSource(is));
        } catch (Exception e) {
           System.err.println(e.toString());
        }
}

public JTable getTable() {
    return RSTable;
}

    public void startElement (String uri, String localName,
                              String qName, Attributes atts)
           throws SAXException {
      if (localName.equalsIgnoreCase("param")) {
        paramName = atts.getValue("name");
      }
      else {
        paramName = null;
      }
      elementStack.push(localName);
      if (localName.equals("document")) {
            docid = "";
            docname = "";
            doctype = "";
            doctitle = "";
            paramName = "";
            relationsVector = new Vector();
            params = new Hashtable();
      }
      if (localName.equals("relation")) {
            relationtype = "";
            relationdoc = "";
            relationdoctype = "";
      }
    }
  
    public void endElement (String uri, String localName,
                            String qName) throws SAXException {
      if (localName.equals("relation")) {
        String[] rel = new String[3];
        rel[0] = relationtype;
        rel[1] = relationdoc;
        rel[2] = relationdoctype;
        relationsVector.addElement(rel);
      }
      if (localName.equals("document")) {
        int cnt = 0;
        if (returnFields!=null) cnt = returnFields.size();
        String[] row = new String[4+cnt];
 //     String[] row = {docid, docname, doctype, doctitle};
        row[0] = docid;
        row[1] = docname;
        row[2] = doctype;
        if (!doctitle.equals("")) {
            row[3] = doctitle;
        }
        else { row[3] = docid;}  // for cases where there is no doctitle
        for (int i=0;i<cnt;i++) {
            row[4+i] = (String)(params.get(returnFields.elementAt(i)));   
        }
        if (relationsVector.size()>0) {
            relations.put(docid, relationsVector);
        }
        
      dtm.addRow(row);
      }
      String leaving = (String)elementStack.pop();
    }
  
    public void characters(char ch[], int start, int length) {
      String inputString = new String(ch, start, length);
      String currentTag = (String)elementStack.peek();
      if (currentTag.equals("docid")) {
          docid = inputString;
      }
      if (currentTag.equals("docname")) {
          docname = inputString;
      }
      if (currentTag.equals("doctype")) {
          doctype = inputString;
      }
      if (currentTag.equals("doctitle")) {
          doctitle = inputString;
      }
      if (currentTag.equals("param")) {
          String val = inputString;
          if (params.containsKey(paramName)) {  // key already in hash table 2/9/01 DFH
              String cur = (String)params.get(paramName);
              val = cur + "; " + val;
          }
          params.put(paramName, val);  
      }
      if (currentTag.equals("relationtype")) {
          relationtype = inputString;
      }
      if (currentTag.equals("relationdoc")) {
          relationdoc = inputString;
      }
      if (currentTag.equals("relationdoctype")) {
          relationdoctype = inputString;
      }
      
    }

   public void startDocument() throws SAXException { 
     elementStack = new Stack();
   }

   public void endDocument() throws SAXException { }
   public void ignorableWhitespace(char[] cbuf, int start, int len) { }
   public void skippedEntity(String name) throws SAXException { }
   public void processingInstruction(String target, String data) throws SAXException { }
   public void startPrefixMapping(String prefix, String uri) throws SAXException { }
   public void endPrefixMapping(String prefix) throws SAXException { }
   public void setDocumentLocator (Locator locator) { }


// use to get the last element in a path string
   private String getLastPathElement(String str) {
        String last = "";
        int ind = str.lastIndexOf("/");
        if (ind==-1) {
           last = str;     
        }
        else {
           last = str.substring(ind+1);     
        }
        return last;
   }
   
  private void removeTableColumn( TableColumnModel tcm, int index) {
        int cnt = tcm.getColumnCount();
        if (index<cnt) {
            TableColumn tc = tcm.getColumn(index);
            tcm.removeColumn(tc);
        }
  }
  
  private void removeFirstNColumns(TableColumnModel tcm, int n) {
        // n is the number of leading columns to remove
        for (int i=0;i<n;i++) {
            removeTableColumn(tcm,0);
        }
  }
   
  public Hashtable getRelations() {
    return relations; 
  }
   
}



    
