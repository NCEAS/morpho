/**
 *  Name: XMLList.java
 *  Institution: National Center for Ecological Analysis and Synthesis
 *  Copyright: 1998, 1999 The Regents of the University of California and
 *         National Center for Ecological Analysis and Synthesis,
 *         University of California, Santa Barbara 1999.
 *         All rights reserved.
 * 
 *  Authors: Dan Higgins
 *
 *  
 *     Version: '$Id: XMLList.java,v 1.1 2000-08-22 19:16:09 higgins Exp $'
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

import java.util.Stack;
import java.util.Vector;

public class XMLList implements ContentHandler
{
    InputStream is;
    Vector listVector;
    String ListItem;
    Stack elementStack;
    
public XMLList(InputStream is) {
    this.is = is;
    
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

public XMLList(InputStream is, String listitem) {
    this.is = is;
    ListItem = listitem;
    listVector = new Vector();
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

public Vector getListVector() {
    return listVector;
}

public void setListItem(String item) {
      ListItem = item; 
}
   
    public void startElement (String uri, String localName,
                              String qName, Attributes atts)
           throws SAXException {
      elementStack.push(localName);
    }
  
    public void endElement (String uri, String localName,
                            String qName) throws SAXException {
      String leaving = (String)elementStack.pop();
    }
  
    public void characters(char ch[], int start, int length) {
  
      String inputString = new String(ch, start, length);
      String currentTag = (String)elementStack.peek();
      if (currentTag.equals(ListItem)) {
        if(!(inputString.trim().endsWith(".xsl"))) {   // filter out xsl docs
          listVector.add(inputString.trim());
        }
      }
    }

   public void startDocument() throws SAXException { 
     elementStack = new Stack();
     listVector = new Vector();
   }

   public void endDocument() throws SAXException { }
   public void ignorableWhitespace(char[] cbuf, int start, int len) { }
   public void skippedEntity(String name) throws SAXException { }
   public void processingInstruction(String target, String data) throws SAXException { }
   public void startPrefixMapping(String prefix, String uri) throws SAXException { }
   public void endPrefixMapping(String prefix) throws SAXException { }
   public void setDocumentLocator (Locator locator) { }
    
    
    
}