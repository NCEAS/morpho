/**
 *  Name: ExternalQuery.java
 *  Institution: National Center for Ecological Analysis and Synthesis
 *  Copyright: 1998, 1999 The Regents of the University of California and
 *         National Center for Ecological Analysis and Synthesis,
 *         University of California, Santa Barbara 1999.
 *         All rights reserved.
 * 
 *  Authors: Dan Higgins
 *
 *  
 *     Version: '$Id: ExternalQuery.java,v 1.5 2000-11-20 23:27:08 higgins Exp $'
 */

/*
*   Current MetaCat result set returns a <document> element for each 'hit'
*   in query. That <document> element has 6 fixed children: <docid>, <docname>,
*   <doctype>, <createdate>, and <updatadate>
*   Other child elements are determined by query and are returned as <param>
*   elements with a "name" attribute and a value in the text.
*/


package edu.ucsb.nceas.querybean;

import java.io.*;
import java.util.Vector;
import edu.ucsb.nceas.dtclient.*;

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
    Hashtable params;
    Vector returnFields; // return field path names
    
public ExternalQuery(InputStream is) {
    this.is = is;
    ConfigXML config = new ConfigXML("config.xml");
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
      elementStack.push(localName);
      if (localName.equals("document")) {
            docid = "";
            docname = "";
            doctype = "";
            doctitle = "";
            paramName = "";
            params = new Hashtable();
      }
    }
  
    public void endElement (String uri, String localName,
                            String qName) throws SAXException {
      if (localName.equals("document")) {
        int cnt = 0;
        if (returnFields!=null) cnt = returnFields.size();
        String[] row = new String[4+cnt];
 //     String[] row = {docid, docname, doctype, doctitle};
        row[0] = docid;
        row[1] = docname;
        row[2] = doctype;
        row[3] = doctitle;
        for (int i=0;i<cnt;i++) {
            row[4+i] = (String)(params.get(returnFields.elementAt(i)));   
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
          params.put(paramName, inputString);  
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
   
   
   
   
}



    
