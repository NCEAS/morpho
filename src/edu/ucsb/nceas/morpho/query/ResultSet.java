/**
 *  '$RCSfile: ResultSet.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-03 01:51:58 $'
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

import java.io.InputStream;

import javax.swing.table.AbstractTableModel;
import javax.swing.ImageIcon;

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

/**
 * A ResultSet encapsulates the list of results returned from either a
 * local query or a Metacat query. It contains a reference to its
 * original query, so the result set can be refreshed by re-running the query.
 */
public class ResultSet extends AbstractTableModel implements ContentHandler
{
  /** store a private copy of the Query run to create this resultset */
  private Query query = null;

  /** Store each row of the result set as a row in a Vector */
  Vector resultsVector = null;

  //private InputStream is;   
  private Stack elementStack = null;
  //private JTable RSTable = null;
  private String[] headers = {"Doc ID", "Document Name", 
                              "Document Type", "Document Title"};
  //private DefaultTableModel dtm;
  private String docid;
  private String docname;
  private String doctype;
  private String doctitle;
  private String paramName;
  private String relationtype;
  private String relationdoc;
  private String relationdoctype;
  private Hashtable params;
  private Hashtable relations; 
  // used to save relation doc info for each doc returned
  // key is docid, value is a Vector of string arrays
  // each string array is (relationtype,relationdoc,relationdoctype)
  private Vector relationsVector; 
  private Vector returnFields; // return field path names
  private int num_cols_to_remove = 3;

  private ImageIcon folder = null; //folder = new ImageIcon(
                    //getClass().getResource("Btflyyel.gif"));
  private String[] columnNames = { "First Name",
                                   "Last Name", "Sport",
                                   "# of Years", "Vegetarian"
  };

  private Object[][] data = {
    {"Mary", "Campione",
     "Snowboarding", new Integer(5), new Boolean(false)},
    {"Alison", "Huml",
     "Rowing", new Integer(3), new Boolean(true)},
    {"Kathy", "Walrath",
     "Chasing toddlers", new Integer(2), new Boolean(false)},
    {"Mark", "Andrews",
     "Speed reading", new Integer(20), new Boolean(true)},
    {"Angela", "Lih",
     "Teaching high school", new Integer(4), new Boolean(false)}
  };

  /**
   * Construct a ResultSet instance given a query object and a
   * InputStream that represents an XML encoding of the results.
   */
  public ResultSet(Query query, InputStream resultsXMLStream)
  {
    this.query = query;

    resultsVector = new Vector();
    relations = new Hashtable(); 

    folder = new ImageIcon( getClass().getResource("Btflyyel.gif"));

    ConfigXML config = new ConfigXML("lib/config.xml");
    returnFields = config.get("returnfield");
    int cnt;
    if (returnFields==null) {
        cnt = 0;
    } else {
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
/*
    dtm = new DefaultTableModel(headers,0);
    RSTable = new JTable(dtm);
    TableColumnModel tcm = RSTable.getColumnModel();
    removeFirstNColumns(tcm,num_cols_to_remove);
*/
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
      System.err.println(e.toString());
    }
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
    //return data[row][col];
    Vector rowVector = (Vector)resultsVector.get(row);
    return rowVector.get(col);
  }

  /**
   * Return the Class for each column so that they can be
   * rendered correctly.
   */
  public Class getColumnClass(int c)
  {
    return getValueAt(0, c).getClass();
  }


/*
*   Current MetaCat result set returns a <document> element for each 'hit'
*   in query. That <document> element has 6 fixed children: <docid>, <docname>,
*   <doctype>, <createdate>, and <updatadate>
*   Other child elements are determined by query and are returned as <param>
*   elements with a "name" attribute and a value in the text.
*/


  /*
  public ResultSet(InputStream is, int cols) {
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
    
  public ResultSet(InputStream is) {
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
  */

  /*
  public JTable getTable() {
    return RSTable;
  }
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
                          String qName) throws SAXException 
  {
    if (localName.equals("relation")) {
      String[] rel = new String[3];
      rel[0] = relationtype;
      rel[1] = relationdoc;
      rel[2] = relationdoctype;
      relationsVector.addElement(rel);
    }
    if (localName.equals("document")) {
      int cnt = 0;
      if (returnFields != null) {
        cnt = returnFields.size();
      }

      Vector row = new Vector();
      //String[] row = new String[4+cnt];

      row.add(folder);
      row.add(docid);
      row.add(docname);
      row.add(doctype);
/*
      row[0] = docid;
      row[1] = docname;
      row[2] = doctype;
*/
      // for cases where there is no doctitle
      if (!doctitle.equals("")) {
        row.add(doctitle);
        //row[3] = doctitle;
      } else { 
        row.add(docid);
        //row[3] = docid;
      }  
      for (int i=0;i<cnt;i++) {
        row.add((String)(params.get(returnFields.elementAt(i))));
        //row[4+i] = (String)(params.get(returnFields.elementAt(i)));   
      }
      if (relationsVector.size()>0) {
        relations.put(docid, relationsVector);
      }
        
      resultsVector.add(row);
    }
    String leaving = (String)elementStack.pop();
  }
  
  public void characters(char ch[], int start, int length) 
  {
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
      if (params.containsKey(paramName)) {  // key already in hash table
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

  public void endDocument() throws SAXException 
  { 
  }

  public void ignorableWhitespace(char[] cbuf, int start, int len) 
  { 
  }

  public void skippedEntity(String name) throws SAXException 
  { 
  }

  public void processingInstruction(String target, String data) 
              throws SAXException 
  { 
  }

  public void startPrefixMapping(String prefix, String uri) 
              throws SAXException 
  { 
  }

  public void endPrefixMapping(String prefix) throws SAXException 
  { 
  }

  public void setDocumentLocator (Locator locator) 
  { 
  }


  // use to get the last element in a path string
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
