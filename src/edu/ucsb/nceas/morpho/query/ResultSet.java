/**
 *  '$RCSfile: ResultSet.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-12-08 07:13:35 $'
 * '$Revision: 1.25 $'
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

import edu.ucsb.nceas.morpho.framework.*;

import java.io.*;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.table.*;
import javax.swing.table.AbstractTableModel;

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

/**
 * A ResultSet encapsulates the list of results returned from either a
 * local query or a Metacat query. It contains a reference to its
 * original query, so the result set can be refreshed by re-running the query.
 * Current MetaCat query returns a <document> element for each 'hit'
 * in query. That <document> element has 5 fixed children: <docid>, <docname>,
 * <doctype>, <createdate>, and <updatadate>.
 * Other child elements are determined by query and are returned as <param>
 * elements with a "name" attribute and the value as the content.
 */
public class ResultSet extends AbstractTableModel implements ContentHandler
{
  /** store a private copy of the Query run to create this resultset */
  private Query savedQuery = null;

  /** Store each row of the result set as a row in a Vector */
  protected Vector resultsVector = null;

  /**
   * a list of the desired return fields from the configuration file. 
   *
   * NOTE: This info should really come from the query so that it can 
   * vary by query.
   */
  private Vector returnFields;

  /** Flag indicating whether the results are from a local query */
  private boolean isLocal = false;

  /** Flag indicating whether the results are from a metacat query */
  private boolean isMetacat = false;

  /** A reference to the framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  // this group of variables are temporary vars that are used while 
  // parsing the XML stream.  Ultimately the data ends up in the
  // resultsVector above
  private Stack elementStack = null;
  private String[] headers;
  private String docid;
  private String docname;
  private String doctype;
  private String createdate;
  private String updatedate;
  private String paramName;
  private Hashtable params;
  /**
   * used to save package info for each doc returned during SAX parsing
   * Hashtable has up to five fields with the following String keys:
   * subject, subjectdoctype, relationship, object, objectdoctype
   */
  private Hashtable triple; 
  /** a collection of triple Hashtables, used during SAX parsing */
  private Vector tripleList;

  /** The icon for representing local storage. */
  private ImageIcon localIcon = null;
  /** The icon for representing metacat storage. */
  private ImageIcon metacatIcon = null;
  /** The icon for representing both local and metacat storage. */
  private ImageIcon bothIcon = null;
  /** The icon for representing local storage with data. */
  private ImageIcon localDataIcon = null;
  /** The icon for representing metacat storage with data. */
  private ImageIcon metacatDataIcon = null;
  /** The icon for representing both local and metacat storage with data. */
  private ImageIcon bothDataIcon = null;

  /**
   * Construct a ResultSet instance given a query object and a
   * InputStream that represents an XML encoding of the results.
   */
  public ResultSet(Query query, String source, InputStream resultsXMLStream,
                   ClientFramework cf)
  {
    this.savedQuery = query;
    this.framework = cf;

    if (source.equals("local")) {
      isLocal = true;
      isMetacat = false;
    } else if (source.equals("metacat")) {
      isLocal = false;
      isMetacat = true;
    }

    resultsVector = new Vector();

    localIcon = new ImageIcon( getClass().getResource("local-metadata.gif"));
    metacatIcon = new ImageIcon( getClass().getResource("network-metadata.gif"));
    bothIcon = new ImageIcon( 
            getClass().getResource("local+network-metadata.gif"));
    localDataIcon = new ImageIcon( 
            getClass().getResource("local-metadata+data.gif"));
    metacatDataIcon = new ImageIcon( 
            getClass().getResource("network-metadata+data.gif"));
    bothDataIcon = new ImageIcon( 
            getClass().getResource("local+network-metadata+data.gif"));

    this.framework = framework;
    this.config = framework.getConfiguration();   
    ConfigXML profile = framework.getProfile();
    returnFields = profile.get("returnfield");
  
    int cnt;
    if (returnFields==null) {
        cnt = 0;
    } else {
        cnt = returnFields.size();
    }
    
    // Set up the headers
    // assume at least 5 fixed fields returned, plus add an icon column
    int numberFixedHeaders = 1;
    headers = new String[numberFixedHeaders+cnt];  
    headers[0] = "";  // This is for the icon
    for (int i=0;i<cnt;i++) {
      headers[1+i] = getLastPathElement((String)returnFields.elementAt(i));
    }
    /*headers[cnt+1] = "Created";*/
    /*headers[cnt+2] = "Updated";*/
    /*headers[cnt+3] = "Doc ID";*/
    /*headers[cnt+4] = "Document Name";*/
    /*headers[cnt+5] = "Document Type";*/

    // Parse the incoming XML stream and extract the data
    String parserName = "org.apache.xerces.parsers.SAXParser";
    XMLReader parser = null;

    // Set up the SAX document handlers for parsing
    try {
      // Get an instance of the parser
      parser = XMLReaderFactory.createXMLReader(parserName);
      // Set the ContentHandler to this instance
      parser.setContentHandler(this);
      parser.parse(new InputSource(resultsXMLStream));
    } catch (Exception e) {
      framework.debug(6, e.toString());
    }
  }


  /**
   * Construct a ResultSet instance from a vector of vectors;
   * for use with LocalQuery
   */
  public ResultSet(Query query, String source, Vector vec, ClientFramework cf)
  {
    this.savedQuery = query;
    this.framework = cf;

    if (source.equals("local")) {
      isLocal = true;
      isMetacat = false;
    } else if (source.equals("metacat")) {
      isLocal = false;
      isMetacat = true;
    }

    this.framework = framework;
    this.config = framework.getConfiguration();   
    ConfigXML profile = framework.getProfile();
    returnFields = profile.get("returnfield");
  
    int cnt;
    if (returnFields==null) {
        cnt = 0;
    } else {
        cnt = returnFields.size();
    }

    // Set up the headers
    int numberFixedHeaders = 1;
    headers = new String[numberFixedHeaders+cnt];  
    headers[0] = "";  // This is for the icon
    for (int i=0;i<cnt;i++) {
      headers[1+i] = getLastPathElement((String)returnFields.elementAt(i));
    }

    this.resultsVector = vec;
  }


  /**
   *  get the resultsVector
   */
  public Vector getResultsVector() {
    return resultsVector;
  }
  
  /**
   *  set the resultsVector
   */
  public void setResultsVector(Vector rv) {
    this.resultsVector = rv;
  }

  /**
   * Return the number of columns in this result set
   */
  public int getColumnCount()
  {
    return headers.length;
  }

  /**
   * Return the number of records in this result set
   */
  public int getRowCount()
  {
    return resultsVector.size();
  }

  /**
   * Return the correct row height for table rows
   */
  public int getRowHeight()
  {
    return localIcon.getIconHeight();
  }

  /**
   * Determine the name of a column by its index
   */
  public String getColumnName(int col)
  {
    return headers[col];
  }

  /**
   * Determine the value of a column by its row and column index
   */
  public Object getValueAt(int row, int col)
  {
    Object value = null;
    try {
      Vector rowVector = (Vector)resultsVector.elementAt(row);
      value = rowVector.elementAt(col);
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      String emptyString = "";
      value = null;
    } catch (NullPointerException npe) {
      String emptyString = "";
      value = emptyString;
    }
    return value;
  }

  /**
   * Return the Class for each column so that they can be
   * rendered correctly.
   */
  public Class getColumnClass(int c)
  {
    Class currentClass = null;
    try {
      currentClass = this.getValueAt(0, c).getClass();
    } catch (NullPointerException npe) {
      try {
        currentClass = Class.forName("java.lang.String");
      } catch (ClassNotFoundException cnfe) {
      }
    }
    return currentClass;
  }

  /**
   * SAX handler callback that is called upon the start of an
   * element when parsing an XML document.
   */
  public void startElement (String uri, String localName,
                            String qName, Attributes atts)
                            throws SAXException 
  {
    if (localName.equalsIgnoreCase("param")) {
      paramName = atts.getValue("name");
    } else {
      paramName = null;
    }

    elementStack.push(localName);

    // Reset the variables for each document
    if (localName.equals("document")) {
      docid = "";
      docname = "";
      doctype = "";
      createdate = "";
      updatedate = "";
      paramName = "";
      params = new Hashtable();
      tripleList = new Vector();
    }

    // Reset the variables for each relation within a document
    else if (localName.equals("triple")) {
      triple = new Hashtable(); 
    }
  }
  
  /**
   * SAX handler callback that is called upon the end of an
   * element when parsing an XML document.
   */
  public void endElement (String uri, String localName,
                          String qName) throws SAXException 
  {
    if (localName.equals("triple")) {
      tripleList.addElement(triple);

    } else if (localName.equals("document")) {
      int cnt = 0;
      if (returnFields != null) {
        cnt = returnFields.size();
      }

      Vector row = new Vector();

      // Display the right icon for the data package
      boolean hasData = false;
      Enumeration tripleEnum = tripleList.elements();
      while (tripleEnum.hasMoreElements()) {
          Hashtable currentTriple = (Hashtable)tripleEnum.nextElement();
          if (currentTriple.containsKey("relationship")) {
              String rel = (String)currentTriple.get("relationship");
              if (rel.indexOf("isDataFileFor") != -1) {
                  hasData = true;
              }
          }
      }
      if (hasData) {
        row.addElement(metacatDataIcon);
      } else {
        row.addElement(metacatIcon);
      }

      // Then display requested fields in requested order
      for (int i=0; i < cnt; i++) {
        row.addElement((String)(params.get(returnFields.elementAt(i))));
      }

      // Then store additional default fields
      row.addElement(createdate);
      row.addElement(updatedate);
      row.addElement(docid);
      row.addElement(docname);
      row.addElement(doctype);
      row.addElement(new Boolean(isLocal));
      row.addElement(new Boolean(isMetacat));
      row.addElement(tripleList);
        
      // Add this document row to the list of results
      resultsVector.addElement(row);
    }
    String leaving = (String)elementStack.pop();
  }
  
  /**
   * SAX handler callback that is called for character content of an
   * element when parsing an XML document.
   */
  public void characters(char ch[], int start, int length) 
  {
    String inputString = new String(ch, start, length);
    inputString = inputString.trim(); // added by higgins to remove extra white space 7/11/01
    String currentTag = (String)elementStack.peek();
    if (currentTag.equals("docid")) {
      docid = inputString;
    } else if (currentTag.equals("docname")) {
      docname = inputString;
    } else if (currentTag.equals("doctype")) {
      doctype = inputString;
    } else if (currentTag.equals("createdate")) {
      createdate = inputString;
    } else if (currentTag.equals("updatedate")) {
      updatedate = inputString;
    } else if (currentTag.equals("param")) {
      String val = inputString;
      if (params.containsKey(paramName)) {  // key already in hash table
        String cur = (String)params.get(paramName);
        val = cur + "; " + val;
      }
      params.put(paramName, val);  
    } else if (currentTag.equals("subject")) {
      triple.put("subject", inputString);
    } else if (currentTag.equals("subjectdoctype")) {
      triple.put("subjectdoctype", inputString);
    } else if (currentTag.equals("relationship")) {
      triple.put("relationship", inputString);
    } else if (currentTag.equals("object")) {
      triple.put("object", inputString);
    } else if (currentTag.equals("objectdoctype")) {
      triple.put("objectdoctype", inputString);
    }
  }

  /**
   * SAX handler callback that is called when an XML document 
   * is initially parsed.
   */
  public void startDocument() throws SAXException { 
    elementStack = new Stack();
  }

  /** Unused SAX handler */
  public void endDocument() throws SAXException 
  { 
  }

  /** Unused SAX handler */
  public void ignorableWhitespace(char[] cbuf, int start, int len) 
  { 
  }

  /** Unused SAX handler */
  public void skippedEntity(String name) throws SAXException 
  { 
  }

  /** Unused SAX handler */
  public void processingInstruction(String target, String data) 
              throws SAXException 
  { 
  }

  /** Unused SAX handler */
  public void startPrefixMapping(String prefix, String uri) 
              throws SAXException 
  { 
  }

  /** Unused SAX handler */
  public void endPrefixMapping(String prefix) throws SAXException 
  { 
  }

  /** Unused SAX handler */
  public void setDocumentLocator (Locator locator) 
  { 
  }

  /**
   * Get the last element in a path string
   */
  private String getLastPathElement(String str) {
    String last = "";
    int ind = str.lastIndexOf("/");
    if (ind==-1) {
      last = str;     
    } else {
      last = str.substring(ind+1);     
    }
    return last;
  }
   
  /**
   * Get the query that was used to construct these results
   */
  public Query getQuery() {
    return savedQuery; 
  }
  
  /**
   * Set the query that was used to construct these results
   * (for use by LocalQuery)
   */
  public void setQuery(Query query) {
    this.savedQuery = query;
  }
 
  /**
   * Open a given row index of the result set using a delegated handler class
   */
  public void openResultRecord(int row)
  {
    try {
      Vector rowVector = (Vector)resultsVector.elementAt(row);
      openResultRecord(rowVector);
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      ClientFramework.debug(1, "array index out of bounds");
    }
  }

  /**
   * Open a given row of the result set using a delegated handler class
   */
  protected void openResultRecord(Vector rowVector)
  {
    int numHeaders = headers.length;
    String docid = null;
    boolean openLocal = false;
    boolean openMetacat = false;
    Vector rowTriples = null;
    try {
      docid = (String)rowVector.elementAt(numHeaders+2);
      openLocal = ((Boolean)rowVector.elementAt(numHeaders+5)).booleanValue();
      openMetacat = ((Boolean)rowVector.elementAt(numHeaders+6)).booleanValue();
      //rowTriples = (Vector)rowVector.get(numHeaders+7);
/*    // DEBUGGING output to determine if the triples Hash is correct
      for (int j=0; j < rowTriples.size(); j++) {
        Hashtable currentTriple = (Hashtable)rowTriples.get(j);
        Enumeration en = currentTriple.keys();
        while (en.hasMoreElements()) {
          String key = (String)en.nextElement(); 
          framework.debug(9, key + " => " + (String)(currentTriple.get(key)) );
        }
      }
*/
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      ClientFramework.debug(1, "array index out of bounds");
      docid = null;
    } catch (NullPointerException npe) {
      ClientFramework.debug(1, "null pointer exception");
      docid = null;
    }

    String location = "";
    if (openLocal) {
      location = "local";
    }
    
    if (openMetacat) {
      location += "metacat";
    }
    
    location = location.trim();

    try {
      ServiceProvider provider = 
                      framework.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      dataPackage.openDataPackage(location, docid, rowTriples);
    } catch (ServiceNotHandledException snhe) {
      framework.debug(6, snhe.getMessage());
    }
  }

  /**
   * Merge a ResultSet onto this one using the docid as the join column
   */
  public void merge(ResultSet r2)
  {
    if (r2 != null) {
      // Create a hash of our docids for easy comparison
      Hashtable docidList = new Hashtable();
      int numColumns = getColumnCount();
      for (int i=0; i < getRowCount(); i++) {
        Vector rowVector = (Vector)resultsVector.elementAt(i);
        String currentDocid = (String)rowVector.elementAt(numColumns+2);
        docidList.put(currentDocid, new Integer(i));
      }
  
      // Step through all of the rows of the results in r2 and
      // see if there is a docid match
      Vector r2Rows = r2.getResultsVector();
      Enumeration ee = r2Rows.elements();
      while (ee.hasMoreElements()) {
        Vector row = (Vector)ee.nextElement();
        String currentDocid = (String)row.elementAt(numColumns+2);
        // if docids match, change the icon and location flags
        if (docidList.containsKey(currentDocid)) {
          int rowIndex = ((Integer)docidList.get(currentDocid)).intValue();
          Vector originalRow = (Vector)resultsVector.elementAt(rowIndex);

          // Determine which icon to use based on the current setting
          ImageIcon currentIcon = (ImageIcon)originalRow.elementAt(0);
          if (currentIcon == metacatDataIcon) {
            originalRow.setElementAt(bothDataIcon, 0);
          } else {
            originalRow.setElementAt(bothIcon, 0);
          }
          originalRow.setElementAt(new Boolean(true), numColumns+5);
          originalRow.setElementAt(new Boolean(true), numColumns+6);
        } else {
          resultsVector.addElement(row);
        }
      }
    }
  }

  /**
   * Get a reference to the framework
   */
  public ClientFramework getFramework()
  {
    return this.framework;
  }
}
