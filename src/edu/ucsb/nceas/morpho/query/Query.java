/**
 *  '$RCSfile: Query.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-07 21:14:08 $'
 * '$Revision: 1.5 $'
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

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A Class that represents a structured query, and can be 
 * constructed from an XML serialization conforming to @see pathquery.dtd. 
 * The printSQL() method can be used to print a SQL serialization of the query.
 */
public class Query extends DefaultHandler {
 
  // Query data structures
  private String queryString;
  private boolean containsExtendedSQL=false;
  private String meta_file_id;
  private String queryTitle;
  private Vector doctypeList;
  private Vector returnFieldList;
  private Vector ownerList;
  private Vector siteList;
  private QueryGroup query = null;

  private Stack elementStack;
  private Stack queryStack;
  private String currentValue;
  private String currentPathexpr;
  private String parserName = null;
  private String accNumberSeparator = null;

  /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** Flag, true if Metacat searches are performed for this query */
  private boolean searchMetacat = true;

  /** Flag, true if network searches are performed for this query */
  private boolean searchLocal = true;

  /**
   * construct an instance of the Query class 
   *
   * @param queryspec the XML representation of the query (should conform
   *                  to pathquery.dtd) as a Reader
   * @param parserName the fully qualified name of a Java Class implementing
   *                  the org.xml.sax.XMLReader interface
   */
  public Query(Reader queryspec, ClientFramework framework)
         //throws IOException 
  {
    super();
    
    // Initialize the members
    doctypeList = new Vector();
    elementStack = new Stack();
    queryStack   = new Stack();
    returnFieldList = new Vector();
    ownerList = new Vector();
    siteList = new Vector();
    this.framework = framework;
    this.config = framework.getConfiguration();

    loadConfigurationParameters();

    // Store the text of the initial query
    StringBuffer qtext = new StringBuffer();
    int len = 0;
    char[] characters = new char[512];
    try {
      while ((len = queryspec.read(characters, 0, 512)) != -1) {
        qtext.append(characters);
      }
    } catch (IOException ioe) {
      framework.debug(4, "Error reading the query.");
    }
    queryString = qtext.toString();

    // Initialize the parser and read the queryspec
    XMLReader parser = initializeParser();
    if (parser == null) {
      framework.debug(1, "SAX parser not instantiated properly.");
    }
    try {
      parser.parse(new InputSource(new StringReader(queryString.trim())));
    } catch (IOException ioe) {
      framework.debug(4, "Error reading the query during parsing.");
    } catch (SAXException e) {
      framework.debug(4, "Error parsing Query (" + 
                      e.getClass().getName() +").");
      framework.debug(4, e.getMessage());
    }
  }

  /**
   * construct an instance of the Query class 
   *
   * @param queryspec the XML representation of the query (should conform
   *                  to pathquery.dtd) as a String
   * @param parserName the fully qualified name of a Java Class implementing
   *                  the org.xml.sax.Parser interface
   */
  public Query( String queryspec, ClientFramework framework)
         //throws IOException 
  {
    this(new StringReader(queryspec), framework);
  }

  /**
   * Returns true if the parsed query contains and extended xml query 
   * (i.e. there is at least one &lt;returnfield&gt; in the pathquery document)
   */
  public boolean containsExtendedSQL()
  {
    if(containsExtendedSQL)
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  /**
   * Accessor method to return a vector of the extended return fields as
   * defined in the &lt;returnfield&gt; tag in the pathquery dtd.
   */
  public Vector getReturnFieldList()
  {
    return this.returnFieldList; 
  }

  /**
   * Accessor method to return the title of this Query
   */
  public String getQueryTitle()
  {
    return queryTitle; 
  }

  /**
   * Set up the SAX parser for reading the XML serialized query
   */
  private XMLReader initializeParser() {
    XMLReader parser = null;

    // Set up the SAX document handlers for parsing
    try {

      // Get an instance of the parser
      parser = XMLReaderFactory.createXMLReader(parserName);

      // Set the ContentHandler to this instance
      parser.setContentHandler(this);

      // Set the error Handler to this instance
      parser.setErrorHandler(this);

    } catch (Exception e) {
       framework.debug(1, "Error in Query.initializeParser " + e.toString());
    }

    return parser;
  }

  /**
   * callback method used by the SAX Parser when the start tag of an 
   * element is detected. Used in this context to parse and store
   * the query information in class variables.
   */
  public void startElement (String uri, String localName, 
                            String qName, Attributes atts) 
         throws SAXException {
    BasicNode currentNode = new BasicNode(localName);
    // add attributes to BasicNode here
    if (atts != null) {
      int len = atts.getLength();
      for (int i = 0; i < len; i++) {
        currentNode.setAttribute(atts.getLocalName(i), atts.getValue(i));
      }
    }

    elementStack.push(currentNode); 
    if (currentNode.getTagName().equals("querygroup")) {
      QueryGroup currentGroup = new QueryGroup(
                                currentNode.getAttribute("operator"));
      if (query == null) {
        query = currentGroup;
      } else {
        QueryGroup parentGroup = (QueryGroup)queryStack.peek();
        parentGroup.addChild(currentGroup);
      }
      queryStack.push(currentGroup);
    }
  }

  /**
   * callback method used by the SAX Parser when the end tag of an 
   * element is detected. Used in this context to parse and store
   * the query information in class variables.
   */
  public void endElement (String uri, String localName,
                          String qName) throws SAXException {
    BasicNode leaving = (BasicNode)elementStack.pop(); 
    if (leaving.getTagName().equals("queryterm")) {
      boolean isCaseSensitive = (new Boolean(
              leaving.getAttribute("casesensitive"))).booleanValue();
      QueryTerm currentTerm = null;
      if (currentPathexpr == null) {
        currentTerm = new QueryTerm(isCaseSensitive,
                      leaving.getAttribute("searchmode"),currentValue);
      } else {
        currentTerm = new QueryTerm(isCaseSensitive,
                      leaving.getAttribute("searchmode"),currentValue,
                      currentPathexpr);
      }
      QueryGroup currentGroup = (QueryGroup)queryStack.peek();
      currentGroup.addChild(currentTerm);
      currentValue = null;
      currentPathexpr = null;
    } else if (leaving.getTagName().equals("querygroup")) {
      QueryGroup leavingGroup = (QueryGroup)queryStack.pop();
    }
  }

  /**
   * callback method used by the SAX Parser when the text sequences of an 
   * xml stream are detected. Used in this context to parse and store
   * the query information in class variables.
   */
  public void characters(char ch[], int start, int length) {

    String inputString = new String(ch, start, length);
    BasicNode currentNode = (BasicNode)elementStack.peek(); 
    String currentTag = currentNode.getTagName();
    if (currentTag.equals("meta_file_id")) {
      meta_file_id = inputString;
    } else if (currentTag.equals("querytitle")) {
      queryTitle = inputString;
    } else if (currentTag.equals("value")) {
      currentValue = inputString;
    } else if (currentTag.equals("pathexpr")) {
      currentPathexpr = inputString;
    } else if (currentTag.equals("returndoctype")) {
      doctypeList.add(inputString);
    } else if (currentTag.equals("returnfield")) {
      returnFieldList.add(inputString);
      containsExtendedSQL = true;
    } else if (currentTag.equals("owner")) {
      ownerList.add(inputString);
    } else if (currentTag.equals("site")) {
      siteList.add(inputString);
    }
  }


  /**
   * create a SQL serialization of the query that this instance represents
   */
  public String printSQL(boolean useXMLIndex) {
    StringBuffer self = new StringBuffer();

    self.append("SELECT docid,docname,doctype,");
    self.append("date_created, date_updated, rev ");
    self.append("FROM xml_documents WHERE docid IN (");

    // This determines the documents that meet the query conditions
    self.append(query.printSQL(useXMLIndex));

    self.append(") ");
 
    // Add SQL to filter for doctypes requested in the query
    // This is an implicit OR for the list of doctypes
    if (!doctypeList.isEmpty()) {
      boolean firstdoctype = true;
      self.append(" AND ("); 
      Enumeration en = doctypeList.elements();
      while (en.hasMoreElements()) {
        String currentDoctype = (String)en.nextElement();
        if (firstdoctype) {
           firstdoctype = false;
           self.append(" doctype = '" + currentDoctype + "'"); 
        } else {
          self.append(" OR doctype = '" + currentDoctype + "'"); 
        }
      }
      self.append(") ");
    }
    
    // Add SQL to filter for owners requested in the query
    // This is an implicit OR for the list of owners
    if (!ownerList.isEmpty()) {
      boolean first = true;
      self.append(" AND ("); 
      Enumeration en = ownerList.elements();
      while (en.hasMoreElements()) {
        String current = (String)en.nextElement();
        if (first) {
           first = false;
           self.append(" user_owner = '" + current + "'"); 
        } else {
          self.append(" OR user_owner = '" + current + "'"); 
        }
      }
      self.append(") ");
    }

    // Add SQL to filter for sites requested in the query
    // This is an implicit OR for the list of sites
    if (!siteList.isEmpty()) {
      boolean first = true;
      self.append(" AND ("); 
      Enumeration en = siteList.elements();
      while (en.hasMoreElements()) {
        String current = (String)en.nextElement();
        if (first) {
           first = false;
           self.append(" SUBSTR(docid, 1, INSTR(docid, '" +
               accNumberSeparator + "')-1) = '" + current + "'"); 
        } else {
          self.append(" OR SUBSTR(docid, 1, INSTR(docid, '" +
               accNumberSeparator + "')-1) = '" + current + "'"); 
        }
      }
      self.append(") ");
    }
    return self.toString();
  }
  
  /**
   * This method prints sql based upon the &lt;returnfield&gt; tag in the
   * pathquery document.  This allows for customization of the 
   * returned fields
   * @param doclist the list of document ids to search by
   */
  public String printExtendedSQL(String doclist)
  {  
    StringBuffer self = new StringBuffer();
    self.append("select xml_nodes.docid, xml_index.path, xml_nodes.nodedata ");
    self.append("from xml_index, xml_nodes where xml_index.nodeid=");
    self.append("xml_nodes.parentnodeid and (xml_index.path like '");
    boolean firstfield = true;
    //put the returnfields into the query
    //the for loop allows for multiple fields
    for(int i=0; i<returnFieldList.size(); i++)
    {
      if(firstfield)
      {
        firstfield = false;
        self.append((String)returnFieldList.elementAt(i));
        self.append("' ");
      }
      else
      {
        self.append("or xml_index.path like '");
        self.append((String)returnFieldList.elementAt(i));
        self.append("' ");
      }
    }
    self.append(") AND xml_nodes.docid in (");
    //self.append(query.printSQL());
    self.append(doclist);
    self.append(")");
    self.append(" AND xml_nodes.nodetype = 'TEXT'");

    return self.toString();
  }
  
  public static String printRelationSQL(String docid)
  {
    StringBuffer self = new StringBuffer();
    self.append("select subject, relationship, object, subdoctype, ");
    self.append("objdoctype from xml_relation ");
    self.append("where subject like '").append(docid).append("'");
    return self.toString();
  }
   
  /**
   * Prints sql that returns all relations in the database.
   */
  public static String printPackageSQL()
  {
    StringBuffer self = new StringBuffer();
    self.append("select z.nodedata, x.nodedata, y.nodedata from ");
    self.append("(select nodeid, parentnodeid from xml_index where path like ");
    self.append("'package/relation/subject') s, (select nodeid, parentnodeid ");
    self.append("from xml_index where path like ");
    self.append("'package/relation/relationship') rel, ");
    self.append("(select nodeid, parentnodeid from xml_index where path like ");
    self.append("'package/relation/object') o, ");
    self.append("xml_nodes x, xml_nodes y, xml_nodes z ");
    self.append("where s.parentnodeid = rel.parentnodeid ");
    self.append("and rel.parentnodeid = o.parentnodeid ");
    self.append("and x.parentnodeid in rel.nodeid ");
    self.append("and y.parentnodeid in o.nodeid ");
    self.append("and z.parentnodeid in s.nodeid ");
    //self.append("and z.nodedata like '%");
    //self.append(docid);
    //self.append("%'");
    return self.toString();
  }
  
  /**
   * Prints sql that returns all relations in the database that were input
   * under a specific docid
   * @param docid the docid to search for.
   */
  public static String printPackageSQL(String docid)
  {
    StringBuffer self = new StringBuffer();
    self.append("select z.nodedata, x.nodedata, y.nodedata from ");
    self.append("(select nodeid, parentnodeid from xml_index where path like ");
    self.append("'package/relation/subject') s, (select nodeid, parentnodeid ");
    self.append("from xml_index where path like ");
    self.append("'package/relation/relationship') rel, ");
    self.append("(select nodeid, parentnodeid from xml_index where path like ");
    self.append("'package/relation/object') o, ");
    self.append("xml_nodes x, xml_nodes y, xml_nodes z ");
    self.append("where s.parentnodeid = rel.parentnodeid ");
    self.append("and rel.parentnodeid = o.parentnodeid ");
    self.append("and x.parentnodeid in rel.nodeid ");
    self.append("and y.parentnodeid in o.nodeid ");
    self.append("and z.parentnodeid in s.nodeid ");
    self.append("and z.docid like '").append(docid).append("'");
    
    return self.toString();
  }
  
  /**
   * Returns all of the relations that has a certain docid in the subject
   * or the object.
   * 
   * @param docid the docid to search for
   */
  public static String printPackageSQL(String subDocidURL, String objDocidURL)
  {
    StringBuffer self = new StringBuffer();
    self.append("select z.nodedata, x.nodedata, y.nodedata from ");
    self.append("(select nodeid, parentnodeid from xml_index where path like ");
    self.append("'package/relation/subject') s, (select nodeid, parentnodeid ");
    self.append("from xml_index where path like ");
    self.append("'package/relation/relationship') rel, ");
    self.append("(select nodeid, parentnodeid from xml_index where path like ");
    self.append("'package/relation/object') o, ");
    self.append("xml_nodes x, xml_nodes y, xml_nodes z ");
    self.append("where s.parentnodeid = rel.parentnodeid ");
    self.append("and rel.parentnodeid = o.parentnodeid ");
    self.append("and x.parentnodeid in rel.nodeid ");
    self.append("and y.parentnodeid in o.nodeid ");
    self.append("and z.parentnodeid in s.nodeid ");
    self.append("and (z.nodedata like '");
    self.append(subDocidURL);
    self.append("' or y.nodedata like '");
    self.append(objDocidURL);
    self.append("')");
    return self.toString();
  }
  
  public static String printGetDocByDoctypeSQL(String docid)
  {
    StringBuffer self = new StringBuffer();

    self.append("SELECT docid,docname,doctype,");
    self.append("date_created, date_updated ");
    self.append("FROM xml_documents WHERE docid IN (");
    self.append(docid).append(")");
    return self.toString();
  }
  
  /**
   * create a String description of the query that this instance represents.
   * This should become a way to get the XML serialization of the query.
   */
  public String toString() {
    return "meta_file_id=" + meta_file_id + "\n" + query;
  }

  private InputStream queryMetacat()
  {
    InputStream queryResult = null;

    Properties prop = new Properties();
    prop.put("action", "squery");
    prop.put("query", queryString);
    prop.put("qformat", "xml");
    try
    {
      queryResult = framework.getMetacatInputStream(prop);
    }
    catch(Exception w)
    {
      framework.debug(1, "Error in submitting structured query");
      framework.debug(1, w.getMessage());
    }

    return queryResult;
  }

  public ResultSet execute()
  {
    ResultSet results = null;

    // TODO: Run these queries in parallel threads

    // if appropriate, query metacat
    ResultSet metacatResults = null;
    if (searchMetacat) {
      metacatResults = new ResultSet(this, "metacat", 
                                     queryMetacat(), framework);
    }

    // if appropriate, query locally
    ResultSet localResults = null;
    if (searchLocal) {
      //ResultSet localResults = new ResultSet(this, "local", 
                                     //queryLocal(), framework);
    }

    // merge the results -- currently unimplemented!
    //results = metacatResults.merge(localResults);
    results = metacatResults;

    // return the merged results
    return results;
  }

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
    parserName = config.get("saxparser", 0);
    accNumberSeparator = config.get("accNumberSeparator", 0);
    String searchMetacatString = config.get("searchmetacat", 0);
    searchMetacat = (new Boolean(searchMetacatString)).booleanValue();
    String searchLocalString = config.get("searchlocal", 0);
    searchLocal = (new Boolean(searchLocalString)).booleanValue();
  }

  /** Main routine for testing */
  static public void main(String[] args) 
  {
     if (args.length < 1) {
       System.err.println("Wrong number of arguments!!!");
       System.err.println("USAGE: java Query [-noindex] <xmlfile>");
       return;
     } else {
       int i = 0;
       boolean useXMLIndex = true;
       if ( args[i].equals( "-noindex" ) ) {
         useXMLIndex = false;
         i++;
       }
       String xmlfile  = args[i];

       try {
         ClientFramework cf = new ClientFramework(
                              new ConfigXML("lib/config.xml"));
         FileReader xml = new FileReader(new File(xmlfile));
         
         Query qspec = new Query(xml, cf);
         InputStreamReader returnStream =
                       new InputStreamReader( qspec.queryMetacat());

         int len = 0;
         char[] characters = new char[512];
         while ((len = returnStream.read(characters, 0, 512)) != -1) {
	   System.out.print(characters);
         }

/*
         ResultSet results = qspec.execute();

         JFrame frame = new JFrame("SimpleTest");
         frame.setSize(new Dimension(700, 200));
         frame.addWindowListener(new WindowAdapter()
         {
           public void windowClosing(WindowEvent e)
           {
             System.exit(0);}
           }
         );
         ResultPanel resultsPanel = new ResultPanel(results);
         frame.getContentPane().add(resultsPanel);
         frame.pack();
         resultsPanel.invalidate();
         frame.setVisible(true);
*/
       } catch (IOException e) {
         System.err.println(e.getMessage());
       }
     }
  }
}
