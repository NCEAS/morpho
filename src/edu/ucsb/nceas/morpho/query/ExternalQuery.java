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
 *     Version: '$Id: ExternalQuery.java,v 1.3 2000-08-11 22:23:37 higgins Exp $'
 */

package edu.ucsb.nceas.querybean;

import java.io.*;

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
    
public ExternalQuery(InputStream is) {
    this.is = is;
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
      elementStack.push(localName);
      if (localName.equals("document")) {
            docid = "";
            docname = "";
            doctype = "";
            doctitle = "";
      }
    }
  
    public void endElement (String uri, String localName,
                            String qName) throws SAXException {
      if (localName.equals("document")) {
      String[] row = {docid, docname, doctype, doctitle};
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


}



    
