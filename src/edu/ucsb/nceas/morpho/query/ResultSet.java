/**
 *  '$RCSfile: ResultSet.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-04-03 04:29:07 $'
 * '$Revision: 1.45 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.*;

import java.io.*;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import java.util.Collections;

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
public class ResultSet extends AbstractTableModel implements ContentHandler,
                                                        ColumnSortableTableModel
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

  /** A reference to the Morpho */
  private Morpho morpho = null;

  /** The configuration options object reference from Morpho */
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
  // A hash table to store mapping between column name and resultSVector Index
  private Hashtable mapColumnNameAndVectorIndex = new Hashtable();

  /** The icon for representing local storage. */
  public static ImageIcon localIcon = null;
  /** The icon for representing metacat storage. */
  public static ImageIcon metacatIcon = null;
  /** the icon for blank, nothing there */
  public static ImageIcon blankIcon = null;
  /** The icon for representing package */
  public static ImageIcon packageIcon = null;
  /** The icon for representing pakcage and data file */
  public static ImageIcon packageDataIcon = null;

  /** The icon for representing both local and metacat storage. */
  //private ImageIcon bothIcon = null;
  /** The icon for representing local storage with data. */
  //private ImageIcon localDataIcon = null;
  /** The icon for representing metacat storage with data. */
  //private ImageIcon metacatDataIcon = null;
  /** The icon for representing both local and metacat storage with data. */
  //private ImageIcon bothDataIcon = null;

  /** Store the index of package icon in resultsVector */
  protected static final int PACKAGEICONINDEX = 0;

  /** Store the index of titl in resultsVector */
  protected static final int TITLEINDEX = 1;

  /** Store the index of surname in resultsVector */
  protected static final int SURNAMEINDEX = 2;

  /** Store the index of keywords in resultsVector */
  protected static final int KEYWORDSINDEX = 3;

  /** Store the index of createdate in resultsVector */
  protected static final int CREATEDATEINDEX = 4;

  /** Store the index of update in resultsVector */
  protected static final int UPDATEDATEINDEX = 5;

  /** Store the index of docid in resultsVector */
  protected static final int DOCIDINDEX = 6;

  /** Store the index of doc name in resultsVector */
  protected static final int DOCNAMEINDEX = 7;

  /** Store the index of doc type in resultsVector */
  protected static final int DOCTYPEINDEX = 8;

  /** Store the index of islocal in resultsVector */
  protected static final int ISLOCALINDEX = 9;

  /** Store the index of ismetacat in resultsVector */
  protected static final int ISMETACATINDEX = 10;

  /** Store the index of triple in resultsVector*/
  protected static final int TRIPLEINDEX =11;

  /** Store the height fact for table row height */
  private static final int HEIGHTFACTOR = 2;

  /** global for accumulating characters in SAX parser */
  private String accumulatedCharacters = null;

  /**
   * Construct a ResultSet instance from a vector of vectors;
   * for use with LocalQuery
   */
  public ResultSet(Query query, String source, Vector vec, Morpho morpho) {

    initIcons();
    init(query, source, morpho);
    initMapping();
    this.resultsVector = vec;
  }

  /**
   * Construct a ResultSet instance given a query object and a
   * InputStream that represents an XML encoding of the results.
   */
  public ResultSet( Query query, String source,
                    InputStream resultsXMLStream, Morpho morpho) {

    initIcons();
    init(query, source, morpho);
    initMapping();
    Log.debug(30, "(2.41) Creating result set ...");
     resultsVector = new Vector();

    // Parse the incoming XML stream and extract the data
    XMLReader parser = null;
    // Set up the SAX document handlers for parsing
    try {
      // Get an instance of the parser
      parser = Morpho.createSaxParser((ContentHandler)this, null);
      Log.debug(30, "(2.43) Creating result set ...");
      // Set the ContentHandler to this instance
      parser.parse(new InputSource(new InputStreamReader(resultsXMLStream)));
      Log.debug(30, "(2.44) Creating result set ...");
    } catch (Exception e) {
      Log.debug(30, "(2.431) Exception creating result set ...");
      Log.debug(6, "(2.432) " + e.toString());
      Log.debug(30, "(2.433) Exception is: " + e.getClass().getName());
    }

  }


  // common initialization functionality for constructors
  private void init(Query query, String source, Morpho morpho) {

    this.savedQuery   = query;
    this.morpho       = morpho;
    this.config       = morpho.getConfiguration();
    ConfigXML profile = morpho.getProfile();
//    returnFields      = profile.get("returnfield");
    returnFields      = config.get("returnfield");

    if (source.equals("local")) {
      isLocal = true;
      isMetacat = false;
    } else if (source.equals("metacat")) {
      isLocal = false;
      isMetacat = true;
    }

    // Set up the headers
    createTableHeader();
    //int cnt = (returnFields==null)? 0 : returnFields.size();
    //int numberFixedHeaders = 1;
    //headers = new String[numberFixedHeaders+cnt];
    //headers[0] = " "; // This is for the icon column;
                      // *NOTE* we *must* use a space here, *NOT* an empty
                      // string ("") - otherwise header height is set too
                      // small in windows L&F
    //for (int i=0;i<cnt;i++) {
      //headers[1+i] = getLastPathElement((String)returnFields.elementAt(i));
    //}
  }

  //initialize icons - called from constructor
  private void initIcons() {

    localIcon
      = new ImageIcon(getClass().getResource("local-package-small.png"));
    localIcon.setDescription(ImageRenderer.LOCALTOOLTIP);
    metacatIcon
      = new ImageIcon(getClass().getResource("network-package-small.png"));
    metacatIcon.setDescription(ImageRenderer.METACATTOOLTIP);
    blankIcon
      = new ImageIcon(getClass().getResource("blank.gif"));
    blankIcon.setDescription(ImageRenderer.BLANK);
    packageIcon
      = new ImageIcon(getClass().getResource("metadata-only-small.png"));
    packageIcon.setDescription(ImageRenderer.PACKAGETOOLTIP);
    packageDataIcon
      = new ImageIcon(getClass().getResource("metadata+data-small.png"));
    packageDataIcon.setDescription(ImageRenderer.PACKAGEDATATOOLTIP);
    /*bothIcon
      = new ImageIcon(getClass().getResource("local+network-metadata.gif"));
    localDataIcon
      = new ImageIcon(getClass().getResource("local-metadata+data.gif"));
    metacatDataIcon
      = new ImageIcon(getClass().getResource("network-metadata+data.gif"));
    bothDataIcon
    =new ImageIcon(getClass().getResource("local+network-metadata+data.gif"));*/
  }

  /*
   * Initial mapping bewteen coloumn name and vector index
   */
  private void initMapping()
  {
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.HASDATA, new Integer(PACKAGEICONINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.TITLE, new Integer(TITLEINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.DOCID, new Integer(DOCIDINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.SURNAME, new Integer(SURNAMEINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.KEYWORDS, new Integer(KEYWORDSINDEX));
    mapColumnNameAndVectorIndex.put(
               QueryRefreshInterface.LASTMODIFIED, new Integer(UPDATEDATEINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.LOCAL, new Integer(ISLOCALINDEX));
    mapColumnNameAndVectorIndex.put(
                  QueryRefreshInterface.NET, new Integer(ISMETACATINDEX));

  }

  /**
   *  get the resultsVector
   */
  public Vector getResultsVector() {
    return this.resultsVector;
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
    if (localIcon != null)
    {
      int height = (localIcon.getIconHeight())*HEIGHTFACTOR;
      return height ;
    }
    else
    {
      return 1;
    }
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
    return value;
  }

  /**
   * Get the morpho attribute
   * @return Morpho
   */
  public Morpho getMorpho()
  {
    return morpho;
  }

  /**
   * This method will change the clonum name of model
   * @param anotherHeader String[]
   */
  public void setHeader(String [] anotherHeader)
  {
    headers = new String[anotherHeader.length];
    for (int i= 0; i < headers.length; i++)
    {
      headers[i] = anotherHeader[i];
    }
  }

  /**
   * This method will set a mapping table
   * @param hash Hashtable
   */
  public void setMapping(Hashtable hash)
  {
    mapColumnNameAndVectorIndex = hash;
  }

  /**
   * Lookup an array to find resultsVector index for header index
   *  header index              resultVector index
   *      0                       PACKAGEICONEX(0)
   *      1                       TITLEINDEX(1)
   *      2                       DOCIDINDEX(6)
   *      3                       SURNAMEINDEX(2)
   *      4                       KEYWORKDINDEX(3)
   *      5                       UPDATEDATEINDEX(5)
   *      6                       ISLOCALINDEX(9)
   *      7                       ISMETACATINDEX(10)
   */
  public int lookupResultsVectorIndex(int headerIndex)
  {
    // get the column name (head)
    String head = headers[headerIndex];
    return lookupResultsVectorIndex(head);

  }//lookupResultsVectorIndex

  public int lookupResultsVectorIndex(String headName)
 {
   int vectorIndex = -1;
   Object obj = mapColumnNameAndVectorIndex.get(headName);
   if (obj != null)
   {
      Integer value = (Integer)obj;
      vectorIndex = value.intValue();
   }
   Log.debug(30, "the index of vector is " + vectorIndex + " for " + headName);
   return vectorIndex;

 }//lookupResultsVectorIndex


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
   * Create the header for table model
   * The header include a package icon, title, documentid,  surname, keywords
   * last modified, local and net icon.
   */
  private void createTableHeader()
  {
    int cnt = (returnFields==null)? 0 : returnFields.size();
    // DFH - using the number of returnFields to setup the table creates problems
    // (especialy with column percent size array in 'ResultPanel' class)
    // And we may want to have returnFields that are not displayed (e.g. a field
    // to indicated whether data is included with the package).
    // Thus, for now, just fix the 'cnt' variable
    cnt = 3;
    int numberFixedHeaders = 5;
    headers = new String[numberFixedHeaders+cnt];
    headers[0] = QueryRefreshInterface.HASDATA;
    headers[1] = QueryRefreshInterface.TITLE;
    headers[2] = QueryRefreshInterface.DOCID;// This for third column header
    // SurName
    headers[3] = QueryRefreshInterface.SURNAME;
    // Keyworkds
    headers[4] = QueryRefreshInterface.KEYWORDS;
    headers[5]= QueryRefreshInterface.LASTMODIFIED;
    headers[6]= QueryRefreshInterface.LOCAL;
    headers[7]= QueryRefreshInterface.NET;
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
    accumulatedCharacters = "";
  }

   /**
   * SAX handler callback that is called upon the end of an
   * element when parsing an XML document.
   */
  public void endElement (String uri, String localName,
                          String qName) throws SAXException
  {
    setRSValues(localName);
    if (localName.equals("triple")) {
      tripleList.addElement(triple);

    } else if (localName.equals("document")) {
      int cnt = 0;
      if (returnFields != null) {
        cnt = returnFields.size();
    // DFH - using the number of returnFields to setup the table creates problems
    // (especialy with column percent size array in 'ResultPanel' class)
    // And we may want to have returnFields that are not displayed (e.g. a field
    // to indicated whether data is included with the package).
    // Thus, for now, just fix the 'cnt' variable
        cnt = 3;
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
      // hasData has now been set properly for those docs with triples
      // still need to consider eml2.0 docs where there are no triples
// in order to handle both datapackages with triples and those (e.g. eml2) without
// a returnField which looks for 'entityName' fields has been included in the
// query. For some docs this returnField will not be present. Thus, only the first 3
// of the return fields are added to the ResultSet table. [The 'entityName' returnField
// MUST be listed AFTER title, surname, and keyword returnFields]
      if (params.containsKey("entityName")) hasData = true;

      if (hasData) {
        row.addElement(packageDataIcon);
      } else {
        row.addElement(packageIcon);
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
    accumulatedCharacters = accumulatedCharacters + inputString;

    /*
    // this code is commented out to allow an accumulation of characters
    // due to multiple calls to this method which sometimes occur - DFH 5/1/2003
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
        val = cur + " " + val;
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
    */
  }

  private void setRSValues(String currentTag) {
    String inputString = accumulatedCharacters.trim(); // added by higgins to remove extra white space 7/11/01
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
        val = cur + " " + val;
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
      Log.debug(1, "array index out of bounds");
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
      docid = (String)rowVector.elementAt(DOCIDINDEX);
      openLocal = ((Boolean)rowVector.elementAt(ISLOCALINDEX)).booleanValue();
      openMetacat =
                ((Boolean)rowVector.elementAt(ISMETACATINDEX)).booleanValue();
      //rowTriples = (Vector)rowVector.get(numHeaders+7);
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      Log.debug(1, "array index out of bounds");
      docid = null;
    } catch (NullPointerException npe) {
      Log.debug(1, "null pointer exception");
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
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider =
                      services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      dataPackage.openDataPackage(location, docid, rowTriples, null, null);
    } catch (ServiceNotHandledException snhe) {
      Log.debug(6, snhe.getMessage());
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
        String currentDocid = (String)rowVector.elementAt(DOCIDINDEX);
        docidList.put(currentDocid, new Integer(i));
      }

      // Step through all of the rows of the results in r2 and
      // see if there is a docid match
      Vector r2Rows = r2.getResultsVector();
      Enumeration ee = r2Rows.elements();
      while (ee.hasMoreElements()) {
        Vector row = (Vector)ee.nextElement();
        String currentDocid = (String)row.elementAt(DOCIDINDEX);
        // if docids match, change the icon and location flags
        if (docidList.containsKey(currentDocid)) {
          int rowIndex = ((Integer)docidList.get(currentDocid)).intValue();
          Vector originalRow = (Vector)resultsVector.elementAt(rowIndex);

          // Determine which icon to use based on the current setting
          ImageIcon currentIcon
            = (ImageIcon)originalRow.elementAt(PACKAGEICONINDEX);

          if ((currentIcon.getDescription()).
                          equals(packageDataIcon.getDescription())) {
            //originalRow.setElementAt(bothDataIcon, 0);

            originalRow.setElementAt(packageDataIcon, PACKAGEICONINDEX);
          } else {

            //originalRow.setElementAt(bothIcon, 0);
            originalRow.setElementAt(packageIcon, PACKAGEICONINDEX);
          }
          //originalRow.setElementAt(new Boolean(true), numColumns+5);
          originalRow.setElementAt(new Boolean(true), ISLOCALINDEX);
          //originalRow.setElementAt(new Boolean(true), numColumns+6);
          originalRow.setElementAt(new Boolean(true), ISMETACATINDEX);
        } else {
          resultsVector.addElement(row);
        }
      }
    }
  }

  /**
   * Get a reference to the Morpho application framework
   */
  public Morpho getFramework()
  {
    return this.morpho;
  }


  /**
   * Method implements from SortTableModel. To make sure a col can be sort
   * or not. We decide it always be sortable.
   * @param col, the index of column which need to be sorted
   * @param order, the sort order
   */
  public void sortTableByColumn(int col, String order)
  {


  }//sortColumn
}
