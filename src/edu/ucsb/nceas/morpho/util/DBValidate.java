/**
 *  '$RCSfile: DBValidate.java,v $'
 *    Purpose: A Class that validates XML documents
 *             This class is designed to be 'parser independent
 *             i.e. it uses only org.xml.sax classes
 *             It is tied to SAX 2.0 methods
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins, Matt Jones
 *    Release: @release@
 * 
 *   '$Author: higgins $'
 *     '$Date: 2002-12-18 20:32:12 $'
 * '$Revision: 1.1 $'
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
package edu.ucsb.nceas.morpho.util;


import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.sql.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import com.arbortext.catalog.*;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * Name: DBValidate.java
 *       Purpose: A Class that validates XML documents
 * 			   This class is designed to be parser independent
 *    			   i.e. it uses only org.xml.sax classes
 * 			   It is tied to SAX 2.0 methods
 *     Copyright: 2000 Regents of the University of California and the
 *                National Center for Ecological Analysis and Synthesis
 *                April 28, 2000
 *    Authors: Dan Higgins, Matt Jones
 */
public class DBValidate {
    
  static int WARNING = 0;
  static int ERROR=1;
  static int FATAL_ERROR=2;
  
  /** A reference to the Morpho application */
  private Morpho morpho = null;

  /** The configuration options object reference from the Morpho framework */
  private ConfigXML config = null;
  

  XMLReader parser;
  ErrorStorer ef;
  String xml_doc; // document to be parsed
    
  /** Construct a new validation object */
  public DBValidate(String parserName) {

    try {
      // Get an instance of the parser
      parser = XMLReaderFactory.createXMLReader(parserName);
      parser.setFeature("http://xml.org/sax/features/validation",true);
      //parser.setValidationMode(true);     // Oracle
    } catch (Exception e) {
      Log.debug(20, "Could not create parser in DBValidate.DBValidate");
    }
  }
    
  /** Construct a new validation object using an OASIS catalog file */
  public DBValidate(String parserName, Morpho morpho)  {
    this(parserName);
    this.morpho = morpho;
    this.config = morpho.getConfiguration();   
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      String catalogPath = // config.getConfigDirectory() + File.separator +
                                       config.get("local_catalog_path", 0);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL catalogURL = cl.getResource(catalogPath);
        
      myCatalog.parseCatalog(catalogURL.toString());
      cer.setCatalog(myCatalog);
    } catch (Exception e) {
      Log.debug(20, "Problem creating Catalog in DBValidate.DBValidate");
    }

    parser.setEntityResolver(cer);
  }

  /** 
   * validate an xml document against its DTD
   *
   * @param doc the filename of the document to validate
   */
  public boolean validate(String doc) {
    xml_doc = doc;    
    ef = new ErrorStorer();
    ef.resetErrors();
    parser.setErrorHandler(ef);
    try {
      parser.parse((createURL(xml_doc)).toString());
    } catch (IOException e) {
      System.out.println("IOException:Could not parse :" + xml_doc +
                         " from DBValidate.validate");
      ParseError eip = null;
      eip = new ParseError("",0,0,
                "IOException:Could not parse :"+xml_doc);
      if (ef.errorNodes == null)  ef.errorNodes = new Vector();
      ef.errorNodes.addElement(eip);
        
    } catch (Exception e) {} 

      // {System.out.println("Exception parsing:Could not parse :"+xml_doc);} 
    
    if (ef != null && ef.getErrorNodes()!=null && 
      ef.getErrorNodes().size() > 0 ) {
      return false; 
    } else {
      return true;
    }
  }
    
  /** 
   * validate an xml document against its DTD
   *
   * @param xmldoc the String containing the xml document to validate
   */
  public boolean validateString(String xmldoc) {
    // string is actual XML here, NOT URL or file name    
    ef = new ErrorStorer();
    ef.resetErrors();
    parser.setErrorHandler(ef);
      
    InputSource is = new InputSource(new StringReader(xmldoc));
    try {
      parser.parse(is);
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
      Log.debug(20, "Error in parsing in DBValidate.validateString");
    }

    if (ef != null && ef.getErrorNodes()!=null && 
      ef.getErrorNodes().size() > 0 ) {
      return false; 
    } else {
      return true;
    }
  }
    
  /** provide a list of errors from the validation process */
  public String returnErrors() {
    StringBuffer errorstring = new StringBuffer();
    errorstring.append("<?xml version=\"1.0\" ?>\n");
    if (ef != null && ef.getErrorNodes()!=null && 
        ef.getErrorNodes().size() > 0 ) {
      Vector errors = ef.getErrorNodes();
      errorstring.append("<validationerrors>\n");
      for (Enumeration e = errors.elements() ; e.hasMoreElements() ;) {
        errorstring.append(
                      ((ParseError)(e.nextElement())).toXML());
      }
      errorstring.append("</validationerrors>\n");
    } else {
      errorstring.append("<valid />\n");
    }
    return errorstring.toString();
  }
              
  /** Create a URL object from either a URL string or a plain file name. */
  private URL createURL(String name) throws Exception {
    try {
      URL u = new URL(name);
      return u;
    } catch (MalformedURLException ex) {
    }
    URL u = new URL("file:" + new File(name).getAbsolutePath());
    return u;
  }    

  /** 
   * main method for testing 
   * <p>
   * Usage: java DBValidate <xmlfile or URL>
   */
  public static void main(String[] args) {

    if (args.length != 1) {
      System.out.println("Usage: java DBValidate <xmlfile or URL>");
      System.exit(0);
    }

  }

    
  /**
   * ErrorStorer has been revised here to simply create a Vector of 
   * ParseError objects
   *
   */
  class ErrorStorer implements ErrorHandler {

    //
    // Data
    //
    Vector errorNodes = null;
        
    /**
     * Constructor
     */
    public ErrorStorer() {
    }

    /**
     * The client is is allowed to get a reference to the Hashtable,
     * and so could corrupt it, or add to it...
     */
    public Vector getErrorNodes() {
        return errorNodes;
    }

    /**
     * The ParseError object for the node key is returned.
     * If the node doesn't have errors, null is returned.
     */
    public Object getError() {
        if (errorNodes == null)
            return null;
        return errorNodes;
    }
        
    /**
     * Reset the error storage.
     */
    public void resetErrors() {
        if (errorNodes != null)
        errorNodes.removeAllElements();
    }
    
    /***/
    public void warning(SAXParseException ex) {
        handleError(ex, WARNING);
    }

    public void error(SAXParseException ex) {
        handleError(ex, ERROR);
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        handleError(ex, FATAL_ERROR);
    }
        
    private void handleError(SAXParseException ex, int type) {
      // System.out.println("!!! handleError: "+ex.getMessage());

      if (errorNodes == null) {
        errorNodes = new Vector();
      }

      ParseError eip = null;
      eip = new ParseError(ex.getSystemId(), ex.getLineNumber(),
                           ex.getColumnNumber(), ex.getMessage());
        
      // put it in the Hashtable.
      errorNodes.addElement(eip);
    }
        
  }
    
  /**
   * The ParseError class wraps up all the error info from
   * the ErrorStorer's error method.
   *
   * @see ErrorStorer
   */
  class ParseError extends Object {

    //
    // Data
    //

    String fileName;
    int lineNo;
    int charOffset;
    String msg;

    /**
     * Constructor
     */
    public ParseError(String fileName, int lineNo, int charOffset, String msg) {
      this.fileName=fileName;
      this.lineNo=lineNo;
      this.charOffset=charOffset;
      this.msg=msg;
    }

    //
    // Getters...
    //
    public String getFileName() { return fileName; }
    public int getLineNo() { return lineNo; }
    public int getCharOffset() { return charOffset;}
    public String getMsg() { return msg; }
    public void setMsg(String s) { msg = s; }

    /** Return the error message as an xml fragment */
    public String toXML() {
      StringBuffer err = new StringBuffer();
      err.append("<error>\n");
      err.append("<filename>").append(getFileName()).append("</filename>\n");
      err.append("<line>").append(getLineNo()).append("</line>\n");
      err.append("<offset>").append(getCharOffset()).append("</offset>\n");
      err.append("<message>").append(getMsg()).append("</message>\n");
      err.append("</error>\n");
      return err.toString();
    }
  }
}
