/**
 *  '$RCSfile: ResultSet.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-22 18:02:51 $'
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

  /** store a private copy of the LocalQuery run to create this resultset */
  private LocalQuery savedLocalQuery = null;

  /** Store each row of the result set as a row in a Vector */
  private Vector resultsVector = null;

  /**
   * used to save relation doc info for each doc returned
   * key is docid, value is a Vector of string arrays
   * each string array is (relationtype,relationdoc,relationdoctype)
   */
  private Vector relationsVector; 

  /**
   * a list of the desired return fields from the configuration file. 
   *
   * NOTE: This info should really come from the query so that it can 
   * vary by query.
   */
  private Vector returnFields; // return field path names

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
  private String relationtype;
  private String relationdoc;
  private String relationdoctype;
  private Hashtable params;
  private Hashtable relations; 

  /** The folder icon for representing local storage. */
  private ImageIcon folder = null;

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
    relations = new Hashtable(); 

    folder = new ImageIcon( getClass().getResource("Btflyyel.gif"));

    this.framework = framework;
    this.config = framework.getConfiguration();   
    returnFields = config.get("returnfield");
  
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
      framework.debug(9, e.toString());
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
    returnFields = config.get("returnfield");
  
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
    return folder.getIconHeight();
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
      Vector rowVector = (Vector)resultsVector.get(row);
      value = rowVector.get(col);
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      String emptyString = "";
      value = null;
      //framework.debug(9, "No such row or column: row: " + row +
                         //" col: " + col);
    } catch (NullPointerException npe) {
      String emptyString = "";
      value = emptyString;
      //framework.debug(9, "Error getting value at: row: " + row +
                         //" col: " + col);
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
      //framework.debug(9, "Error getting class for col: " + c);
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
      relationsVector = new Vector();
      params = new Hashtable();
    }

    // Reset the variables for each relation within a document
    if (localName.equals("relation")) {
      relationtype = "";
      relationdoc = "";
      relationdoctype = "";
    }
  }
  
  /**
   * SAX handler callback that is called upon the end of an
   * element when parsing an XML document.
   */
  public void endElement (String uri, String localName,
                          String qName) throws SAXException 
  {
    if (localName.equals("relation")) {
      String[] rel = new String[3];
      rel[0] = relationtype;
      rel[1] = relationdoc;
      rel[2] = relationdoctype;
      relationsVector.addElement(rel);

    } else if (localName.equals("document")) {
      int cnt = 0;
      if (returnFields != null) {
        cnt = returnFields.size();
      }

      Vector row = new Vector();

      // Display the right icon for the data package
      row.add(folder);

      // Then display requested fields in requested order
      for (int i=0; i < cnt; i++) {
        row.add((String)(params.get(returnFields.elementAt(i))));
      }

      // Then display additional default fields
      row.add(createdate);
      row.add(updatedate);
      row.add(docid);
      row.add(docname);
      row.add(doctype);
      row.add(new Boolean(isLocal));
      row.add(new Boolean(isMetacat));

      if (relationsVector.size() > 0) {
        relations.put(docid, relationsVector);
      }
        
      resultsVector.add(row);
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
    } else if (currentTag.equals("relationtype")) {
      relationtype = inputString;
    } else if (currentTag.equals("relationdoc")) {
      relationdoc = inputString;
    } else if (currentTag.equals("relationdoctype")) {
      relationdoctype = inputString;
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
   * Return the package relations for the whole result set
   */
  public Hashtable getRelations() {
    return relations; 
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
   * Open a given row of the result set using a delegated handler class
   */
  public void openResultRecord(int row)
  {
    framework.debug(9, "Opening row: " + row);
    int numHeaders = headers.length;
    String docid = null;
    boolean openLocal = false;
    boolean openMetacat = false;
    try {
      Vector rowVector = (Vector)resultsVector.get(row);
      docid = (String)rowVector.get(numHeaders+2);
      openLocal = ((Boolean)rowVector.get(numHeaders+5)).booleanValue();
      openMetacat = ((Boolean)rowVector.get(numHeaders+6)).booleanValue();
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      docid = null;
    } catch (NullPointerException npe) {
      docid = null;
    }

    String location = null;
    if (openLocal) {
      location = "local";
      //framework.debug(9, "Opening local copy of " + docid);
    } else if (openMetacat) {
      location = "metacat";
      //framework.debug(9, "Opening metacat copy of " + docid);
    }

    try {
      ServiceProvider provider = 
                      framework.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataStore = (DataPackageInterface)provider;
      dataStore.openDataPackage(location, docid, relationsVector);
    } catch (ServiceNotHandledException snhe) {
      framework.debug(6, snhe.getMessage());
    }
  }

  /**
   * Merge a ResultSet onto this one using the docid as the join column
   */
  public void merge(ResultSet r2)
  {
    framework.debug(9, "Simple merge, no comparison done yet!");
    Vector r2Rows = r2.getResultsVector();
    Enumeration ee = r2Rows.elements();
    while (ee.hasMoreElements()) {
      Vector row = (Vector)ee.nextElement();
      resultsVector.addElement(row);
    }
  }
}
