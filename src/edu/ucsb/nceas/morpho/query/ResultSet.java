/**
 *  '$RCSfile: ResultSet.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2008-11-27 00:47:17 $'
 * '$Revision: 1.50 $'
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
public class ResultSet extends AbstractTableModel implements ColumnSortableTableModel
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
  public static final int PACKAGEICONINDEX = 0;

  /** Store the index of titl in resultsVector */
  public static final int TITLEINDEX = 1;

  /** Store the index of surname in resultsVector */
  public static final int SURNAMEINDEX = 2;

  /** Store the index of keywords in resultsVector */
  public static final int KEYWORDSINDEX = 3;

  /** Store the index of createdate in resultsVector */
  public static final int CREATEDATEINDEX = 4;

  /** Store the index of update in resultsVector */
  public static final int UPDATEDATEINDEX = 5;

  /** Store the index of docid in resultsVector */
  public static final int DOCIDINDEX = 6;

  /** Store the index of doc name in resultsVector */
  public static final int DOCNAMEINDEX = 7;

  /** Store the index of doc type in resultsVector */
  public static final int DOCTYPEINDEX = 8;

  /** Store the index of islocal in resultsVector */
  public static final int ISLOCALINDEX = 9;

  /** Store the index of ismetacat in resultsVector */
  public static final int ISMETACATINDEX = 10;

  /** Store the index of triple in resultsVector*/
  public static final int TRIPLEINDEX =11;

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
      ResultsetHandler handler = new ResultsetHandler(morpho, source);
      parser = Morpho.createSaxParser(handler, null);
      Log.debug(30, "(2.43) Creating result set ...");
      // Set the ContentHandler to this instance
      parser.parse(new InputSource(new InputStreamReader(resultsXMLStream)));
      SynchronizeVector parseResult = handler.getSynchronizeVector();
      resultsVector =parseResult.getVector();
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
    if (r2 != null)
    {
      // Step through all of the rows of the results in r2 and
      // see if there is a docid match
      Vector r2Rows = r2.getResultsVector();
      merge(r2Rows);

    }
  }

  /**
  * Merge a vector onto this one using the docid as the join column
  */
  public void merge(Vector r2Rows)
  {
    // Create a hash of our docids for easy comparison
      Hashtable docidList = new Hashtable();
      int numColumns = getColumnCount();
      for (int i=0; i < getRowCount(); i++) {
        Vector rowVector = (Vector)resultsVector.elementAt(i);
        String currentDocid = (String)rowVector.elementAt(DOCIDINDEX);
        docidList.put(currentDocid, new Integer(i));
      }

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

     sortVector(resultsVector, col, order);
  }//sortTableColumn

  protected void sortVector(Vector vector, int col, String order)
  {
    boolean sort = false;
    boolean ascending = false;

   // look up sort and ascending
   if (order.equals(SortableJTable.ASCENDING))
   {
     sort = true;
     ascending = true;
   }
   else if (order.equals(SortableJTable.DECENDING))
   {
     sort = true;
     ascending = false;
   }
   else if (order.equals(SortableJTable.NONORDERED))
   {
     sort = false;
   }
   // sorting
   if (sort)
   {

     //look up index in result vector
     int resultColIndex = lookupResultsVectorIndex(col);
     // sort the result vector
     Collections.sort(vector,
                   new CellComparator(resultColIndex, ascending));

   }

  }
}
