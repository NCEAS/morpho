/**
 *  '$RCSfile: Query.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-10-23 22:48:21 $'
 * '$Revision: 1.19 $'
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
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
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
 
  /** flag determining whether extended query terms are present */
  private boolean containsExtendedSQL=false;
  /** Identifier for this query document */
  private String meta_file_id;
  /** Title of this query */
  private String queryTitle;
  /** List of document types to be returned using package back tracing */
  private Vector returnDocList;
  /** List of document types to be searched */
  private Vector filterDocList;
  /** List of fields to be returned in result set */
  private Vector returnFieldList;
  /** List of users owning documents to be searched */
  private Vector ownerList;
  /** List of sites/scopes used to constrain search */
  private Vector siteList;
  /** The root query group that contains the recursive query constraints */
  private QueryGroup rootQG = null;

  // Query data structures used temporarily during XML parsing
  private Stack elementStack;
  private Stack queryStack;
  private String currentValue;
  private String currentPathexpr;
  private String parserName = null;
  private String accNumberSeparator = null;

  /** A reference to the Morpho application */
  private Morpho morpho = null;

  /** The configuration options object reference from Morpho */
  private ConfigXML config = null;

  /** Flag, true if Metacat searches are performed for this query */
  private boolean searchMetacat = true;

  /** Flag, true if network searches are performed for this query */
  private boolean searchLocal = true;

  /**
   * construct an instance of the Query class from an XML Stream
   *
   * @param queryspec the XML representation of the query (should conform
   *                  to pathquery.dtd) as a Reader
   * @param morpho the Morpho application in which this Query is run
   */
  public Query(Reader queryspec, Morpho morpho)
  {
    this(morpho);
    
    // Initialize temporary variables
    elementStack = new Stack();
    queryStack   = new Stack();

    // Initialize the parser and read the queryspec
    XMLReader parser = Morpho.createSaxParser((ContentHandler)this, 
            (ErrorHandler)this);

    if (parser == null) {
      Log.debug(1, "SAX parser not instantiated properly.");
    }
    try {
      //parser.parse(new InputSource(new StringReader(queryString.trim())));
      parser.parse(new InputSource(queryspec));
    } catch (IOException ioe) {
      Log.debug(4, "Error reading the query during parsing.");
    } catch (SAXException e) {
      Log.debug(4, "Error parsing Query (" + 
                      e.getClass().getName() +").");
      Log.debug(4, e.getMessage());
    }
  }

  /**
   * construct an instance of the Query class from an XML String
   *
   * @param queryspec the XML representation of the query (should conform
   *                  to pathquery.dtd) as a String
   * @param morpho the Morpho application which this Query is run
   */
  public Query( String queryspec, Morpho morpho)
  {
    this(new StringReader(queryspec), morpho);
  }

  /**
   * construct an instance of the Query class, manually setting the Query
   * constraints rather that readin from an XML stream
   *
   * @param morpho the Morpho application in which this Query is run
   */
  public Query(Morpho morpho)
  {
    // Initialize the members
    returnDocList = new Vector();
    filterDocList = new Vector();
    returnFieldList = new Vector();
    ownerList = new Vector();
    siteList = new Vector();
    this.morpho = morpho;
    this.config = morpho.getConfiguration();

    loadConfigurationParameters();
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
   * Accessor method to return the identifier of this Query
   */
  public String getIdentifier()
  {
    return meta_file_id; 
  }
  
  /**
   * method to set the identifier of this query
   */
  public void setIdentifier(String id) {
    this.meta_file_id = id;
  }
   
  /**
   * Accessor method to return the title of this Query
   */
  public String getQueryTitle()
  {
    return queryTitle; 
  }
  
  /**
   * method to set the title of this query
   */
  public void setQueryTitle(String title) 
  {
    this.queryTitle = title;
  }
   
  /**
   * Accessor method to return a vector of the return document types as
   * defined in the &lt;returndoctype&gt; tag in the pathquery dtd.
   */
  public Vector getReturnDocList()
  {
    return this.returnDocList; 
  }

  /**
   * method to set the list of return docs of this query
   */
  public void setReturnDocList(Vector returnDocList) 
  {
    this.returnDocList = returnDocList;
  }
   
  /**
   * Accessor method to return a vector of the filter doc types as
   * defined in the &lt;filterdoctype&gt; tag in the pathquery dtd.
   */
  public Vector getFilterDocList()
  {
    return this.filterDocList; 
  }

  /**
   * method to set the list of filter docs of this query
   */
  public void setFilterDocList(Vector filterDocList) 
  {
    this.filterDocList = filterDocList;
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
   * method to set the list of fields to be returned by this query
   */
  public void setReturnFieldList(Vector returnFieldList) 
  {
    this.returnFieldList = returnFieldList;
  }
   
  /**
   * Accessor method to return a vector of the owner fields as
   * defined in the &lt;owner&gt; tag in the pathquery dtd.
   */
  public Vector getOwnerList()
  {
    return this.ownerList; 
  }

  /**
   * method to set the list of owners used to constrain this query
   */
  public void setOwnerList(Vector ownerList) 
  {
    this.ownerList = ownerList;
  }
   
  /**
   * Accessor method to return a vector of the site fields as
   * defined in the &lt;site&gt; tag in the pathquery dtd.
   */
  public Vector getSiteList()
  {
    return this.siteList; 
  }

  /**
   * method to set the list of sites used to constrain this query
   */
  public void setSiteList(Vector siteList) 
  {
    this.siteList = siteList;
  }
   
  /**
   * determine if we should search metacat
   */
  public boolean getSearchMetacat()
  {
    return searchMetacat;
  }
   
  /**
   * method to set searchMetacat
   */
  public void setSearchMetacat(boolean searchMetacat) 
  {
    this.searchMetacat = searchMetacat;
  }

  /**
   * determine if we should search locally
   */
  public boolean getSearchLocal()
  {
    return searchLocal;
  }
   
  /**
   * method to set searchLocal
   */
  public void setSearchLocal(boolean searchLocal) {
    this.searchLocal = searchLocal;
  }

  /**
   * get the QueryGroup used to express query constraints
   */
  public QueryGroup getQueryGroup()
  {
    return rootQG;
  }
   
  /**
   * set the QueryGroup used to express query constraints
   */
  public void setQueryGroup(QueryGroup qg) 
  {
    this.rootQG = qg;
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
      if (rootQG == null) {
        Log.debug(30, "Created root query group.");
        rootQG = currentGroup;
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
      returnDocList.addElement(inputString);
    } else if (currentTag.equals("filterdoctype")) {
      filterDocList.addElement(inputString);
    } else if (currentTag.equals("returnfield")) {
      returnFieldList.addElement(inputString);
      containsExtendedSQL = true;
    } else if (currentTag.equals("owner")) {
      ownerList.addElement(inputString);
    } else if (currentTag.equals("site")) {
      siteList.addElement(inputString);
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
    self.append(rootQG.printSQL(useXMLIndex));

    self.append(") ");
 
    // Add SQL to filter for doctypes requested in the query
    // This is an implicit OR for the list of doctypes
    if (!filterDocList.isEmpty()) {
      boolean firstdoctype = true;
      self.append(" AND ("); 
      Enumeration en = filterDocList.elements();
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
   * create a XML  serialization of the query that this instance represents
   */
  public String toXml() {
    StringBuffer self = new StringBuffer();

    self.append("<?xml version=\"1.0\"?>\n");
    self.append("<pathquery version=\"1.2\">\n");

    // The identifier
    if (meta_file_id != null) { 
      self.append("  <meta_file_id>"+meta_file_id+"</meta_file_id>\n"); 
    }

    // The query title
    if (queryTitle != null) { 
      self.append("  <querytitle>"+queryTitle+"</querytitle>\n"); 
    }

    // Add XML for the return doctype list
    if (!returnDocList.isEmpty()) {
      Enumeration en = returnDocList.elements();
      while (en.hasMoreElements()) {
        String currentDoctype = (String)en.nextElement();
        self.append("  <returndoctype>"+currentDoctype+"</returndoctype>\n"); 
      }
    }
    
    // Add XML for the filter doctype list
    if (!filterDocList.isEmpty()) {
      Enumeration en = filterDocList.elements();
      while (en.hasMoreElements()) {
        String currentDoctype = (String)en.nextElement();
        self.append("  <filterdoctype>"+currentDoctype+"</filterdoctype>\n"); 
      }
    }
    
    // Add XML for the return field list
    if (!returnFieldList.isEmpty()) {
      Enumeration en = returnFieldList.elements();
      while (en.hasMoreElements()) {
        String current = (String)en.nextElement();
        self.append("  <returnfield>"+current+"</returnfield>\n"); 
      }
    }
    
    // Add XML for the owner list
    if (!ownerList.isEmpty()) {
      Enumeration en = ownerList.elements();
      while (en.hasMoreElements()) {
        String current = (String)en.nextElement();
        self.append("  <owner>"+current+"</owner>\n"); 
      }
    }

    // Add XML for the site list
    if (!siteList.isEmpty()) {
      Enumeration en = siteList.elements();
      while (en.hasMoreElements()) {
        String current = (String)en.nextElement();
        self.append("  <site>"+current+"</site>\n"); 
      }
    }

    // Print the QueryGroup XML
    self.append(rootQG.toXml(2));
 
    // End the query
    self.append("</pathquery>\n");
 
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
    return toXml();
  }

  /** Send the query to metacat, get back the XML resultset */
  private InputStream queryMetacat()
  {
    Log.debug(30, "(2.1) Executing metacat query...");
    InputStream queryResult = null;

    Properties prop = new Properties();
    prop.put("action", "squery");
    prop.put("query", toXml());
    prop.put("qformat", "xml");
    try
    {
      queryResult = morpho.getMetacatInputStream(prop);
    }
    catch(Exception w)
    {
      Log.debug(1, "Error in submitting structured query");
      Log.debug(1, w.getMessage());
    }

    Log.debug(30, "(2.3) Metacat output is:\n" + queryResult);
    Log.debug(30, "(2.4) Done Executing metacat query...");
    return queryResult;
  }

  /**
   * Run the query against the local data store and metacat, depending
   * on how the searchMetacat and searchLocal flags are set.  If both
   * local and metacat searches are run, merge the results into a single
   * ResultSet and return it.
   *
   * @returns ResultSet the results of the query(s)
   */
  public ResultSet execute()
  {
    ResultSet results = null;

    // TODO: Run these queries in parallel threads

    Log.debug(30, "(1) Executing result set...");
    // if appropriate, query metacat
    ResultSet metacatResults = null;
    if (searchMetacat) {
      Log.debug(30, "(2) Executing metacat query...");
      metacatResults = new HeadResultSet(this, "metacat", 
                                     queryMetacat(), morpho);
    }

    Log.debug(30, "(2.5) Executing result set...");
    // if appropriate, query locally
    ResultSet localResults = null;
    if (searchLocal) {
      Log.debug(30, "(3) Executing local query...");
      LocalQuery lq = new LocalQuery(this, morpho);
      localResults = lq.execute();
    }

    // merge the results if needed, and return the right result set
    if (!searchLocal) {
      results = metacatResults;
    } else if (!searchMetacat) {
      results = localResults;
    } else {  
      // must merge results
      metacatResults.merge(localResults);
      results = metacatResults;
    }
    // return the merged results
    return results;
  }

  /**
   * Save an XML serialized version of the query in the profile directory
   */
  public void save() throws IOException
  {
    ConfigXML profile = morpho.getProfile();
    String queriesDirName = config.getConfigDirectory() + File.separator +
                            config.get("profile_directory", 0) +
                            File.separator +
                            profile.get("profilename", 0) +
                            File.separator +
                            profile.get("queriesdir", 0); 
    File queriesDir = new File(queriesDirName);
    if (!queriesDir.exists()) {
      queriesDir.mkdirs();
    }
    File queryFile = new File(queriesDir, getIdentifier());
//DFH    boolean isNew = queryFile.createNewFile();
    FileWriter output = new FileWriter(queryFile);
    output.write(this.toXml());
    output.close();
  }

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
    ConfigXML profile = morpho.getProfile();
    parserName = config.get("saxparser", 0);
    accNumberSeparator = profile.get("separator", 0);
    String searchMetacatString = profile.get("searchmetacat", 0);
    searchMetacat = (new Boolean(searchMetacatString)).booleanValue();
    String searchLocalString = profile.get("searchlocal", 0);
    searchLocal = (new Boolean(searchLocalString)).booleanValue();
  }

  /** Main routine for testing */
  static public void main(String[] args) 
  {
     if (args.length < 1) {
       Log.debug(1, "Wrong number of arguments!!!");
       Log.debug(1, "USAGE: java Query [-noindex] <xmlfile>");
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
         Morpho morpho = new Morpho(new ConfigXML("lib/config.xml"));
         FileReader xml = new FileReader(new File(xmlfile));
         
         Query qspec = new Query(xml, morpho);
         Log.debug(9, qspec.toXml());
/*
         InputStreamReader returnStream =
                       new InputStreamReader( qspec.queryMetacat());

         int len = 0;
         char[] characters = new char[512];
         while ((len = returnStream.read(characters, 0, 512)) != -1) {
	   System.out.print(characters);
         }
*/
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
         Log.debug(4, e.getMessage());
       }
     }
  }
}
