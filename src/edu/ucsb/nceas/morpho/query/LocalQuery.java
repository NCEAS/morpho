/**
 *  '$RCSfile: LocalQuery.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-11-19 23:50:09 $'
 * '$Revision: 1.60 $'
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
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.arbortext.catalog.*;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Stack;
import javax.swing.ImageIcon;

/**
 * LocalQuery is a class designed to execute a query defined in
 * an XML document that follows the PathQuery DTD. The path based
 * query statements are converted to XPath expressions and the
 * XPath API features of Apache Xalan/Xerces are used to find
 * nodes specified by the expressions. All XML documents in a given
 * local subdirectory are scanned. The process thus mimics the database
 * search on Metacat, but is applied only to local documents.
 */
public class LocalQuery
{

  /**
   * hash table with dom objects from previously scanned local XML
   * documents; serves as a cache to avoid having to re-parse documents
   * every time an XPath search is carried out. key is filename which should
   * match document id.
   */
  private static Hashtable dom_collection;

  /**
   * The query on which this LocalQuery is based.
   */
  private Query savedQuery = null;

  /**
   * hash table which contains doctype information about each of
   * the locally stored XML documents
   */
  private static Hashtable doctype_collection;
  
  /**
   * hash table with docids as key and a Vector of package IDs
   * as the values
   */
  private static Hashtable dataPackage_collection; 
  
  /**
   * hash table with docids as key and a Vector of package IDs
   * key is the docid of the package document
   * value is a Vector of the triples for that package
   * the vector of triples contains a Hash for each table, with the keys
   * 'subject', 'relationship', and 'object'
   */
  private static Hashtable packageTriples; 
  
  /** A reference to the Morpho application */
  private Morpho morpho = null;

  /** The configuration options object reference from the Morpho framework */
  private ConfigXML config = null;
    
  /** The name of the current profile */
  private String currentProfile;
  
  /** The profile directory */
  private String profileDir;
  
  /** The directory containing all local stored metadata and data */
  private String datadir;
  
  /** The directory containing locally stored dtds */
  private String local_dtd_directory;
  
  /** The file used by the catalog system for looking up public identifiers */
  private String xmlcatalogfile; 
  
  /** The separator used in accesion numbers */
  private String separator;
 
  /** list of field to be returned from query */
  private Vector returnFields;
  
  /** list of doctypes to be searched */
  private Vector doctypes2bsearched;

  /** list of doctypes to be returned */
  private Vector dt2bReturned;
 
  /** Doctype of document currently being queried */
  private String currentDoctype;
    
  /** The folder icon for representing local storage. */
  private ImageIcon localPackageIcon = null;
  /** The folder icon for representing local storage with data. */
  private ImageIcon localPackageDataIcon = null;
    
  // create these static caches when class is first loaded
  static {
    dom_collection = new Hashtable();
    doctype_collection = new Hashtable();
    dataPackage_collection = new Hashtable();
    packageTriples = new Hashtable();
  }
    
  /**
   * Basic Constuctor for the class
   * 
   * @param query the query on which this Local query is based
   * @param morpho the Morpho framework
   */
  public LocalQuery(Query query, Morpho morpho) {
    super();
    this.savedQuery = query;
    
    localPackageIcon
      = new ImageIcon(getClass().getResource("metadata-only-small.png"));
    localPackageIcon.setDescription(ImageRenderer.PACKAGETOOLTIP);
    
    localPackageDataIcon
      = new ImageIcon(getClass().getResource("metadata+data-small.png"));
    localPackageDataIcon.setDescription(ImageRenderer.PACKAGEDATATOOLTIP);
   
  
    this.morpho = morpho;
    this.config = morpho.getConfiguration();   
  
    loadConfigurationParameters();
      
    datadir = datadir.trim();
    xmlcatalogfile = config.getConfigDirectory() + File.separator + 
                              local_dtd_directory.trim()+"/catalog"; 
  }

  /**
   * Run the query against the local document store
   */
  public ResultSet execute() 
  {
    // first, get a list of all packages that meet the query requirements
    Vector packageList = executeLocal(this.savedQuery.getQueryGroup());
    Vector row = null;
    Vector rowCollection = new Vector();
    
    // now build a Vector of Vectors (tablemodel)
    ResultSet rs = null;
    if (packageList != null) {
      Enumeration pl = packageList.elements();
      while (pl.hasMoreElements()) {
        String packageName = (String)pl.nextElement();
        row = createRSRow(packageName);
        rowCollection.addElement(row);
      }
      //rs = new ResultSet(savedQuery, "local", rowCollection, morpho);
      rs = new HeadResultSet(savedQuery, "local", rowCollection, morpho);
    } 

    return rs;
  }

  /**
   *  loops recursively over all files in the 'datadir'
   *  and applies XPath search
   *
   * @param xpathExpression the XPath query string
   */
  private Vector executeXPathQuery(String xpathExpression)
  {
    Vector package_IDs = new Vector();
    Node root;
    long starttime, curtime, fm;
    Log.debug(30, "(3.0) Creating DOM parser...");
    DocumentBuilder parser = morpho.createDomParser();
    Log.debug(30, "(3.1) DOM parser created...");
    // first set up the catalog system for handling locations of DTDs
    CatalogEntityResolver cer = new CatalogEntityResolver();
    String catalogPath = //config.getConfigDirectory() + File.separator +
                                                    config.get("local_catalog_path",0);     

    try {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL catalogURL = cl.getResource(catalogPath);
        
      myCatalog.parseCatalog(catalogURL.toString());
      //myCatalog.parseCatalog(xmlcatalogfile);
      cer.setCatalog(myCatalog);
    } catch (Exception e) {
      Log.debug(6,"Problem creating Catalog!" + e.toString());
    }
    parser.setEntityResolver(cer);
    // set start time variable
    starttime = System.currentTimeMillis();

    File xmldir = new File(datadir);
    Vector filevector = new Vector();
    // get a list of all files to be searched
    getFiles(xmldir, filevector);
    
    // iterate over all the files that are in the local xml directory
    for (int i=0;i<filevector.size();i++) {
      File currentfile = (File)filevector.elementAt(i);
      String filename = currentfile.getPath();
//DFH      String docid = currentfile.getParentFile().getName() + separator +
//DFH                     currentfile.getName();
      File parentFile = new File(currentfile.getParent());
      String docid = parentFile.getName() + separator + currentfile.getName();
      
      // skips subdirectories
      if (currentfile.isFile()) {
        // checks to see if doc has already been placed in DOM cache
        // if so, no need to parse again
        //Log.debug(10,"current id: "+docid);
        if (dom_collection.containsKey(docid)){
          root = ((Document)dom_collection.get(docid)).getDocumentElement();
          if (doctype_collection.containsKey(docid)) {
            currentDoctype = ((String)doctype_collection.get(docid));   
          }
        } else {
          //Log.debug(10,"parsing "+docid);
          InputSource in;
          try {
            in = new InputSource(new FileInputStream(filename));
          } catch (FileNotFoundException fnf) {
            Log.debug(6,"FileInputStream of " + filename + 
                               " threw: " + fnf.toString());
            continue;
          }
          Document current_doc = null;
          try {
            Log.debug(30, "(3.2) Starting parse...");
            current_doc = parser.parse(in);
            Log.debug(30, "(3.3) Ended parse...");
          } catch(Exception e1) {
            // Either this isn't an XML doc, or its broken, so skip it
            Log.debug(20,"Parsing error: " + filename);
            Log.debug(20,e1.toString());
            continue;
          }

          // Get the documentElement from the parser, which is what 
          // the selectNodeList method expects
          root = current_doc.getDocumentElement();
          dom_collection.put(docid,current_doc);
          String temp = getDocTypeFromDOM(current_doc);
          if (temp==null) temp = root.getNodeName();
          doctype_collection.put(docid,temp);
          currentDoctype = temp;
          if ((dt2bReturned.contains("any")) || (dt2bReturned.contains(currentDoctype))) {
              addToPackageList(root, docid);
          }
        } // end else
      
        String rootname = root.getNodeName();
        NodeList nl = null;
        try {
            // see if current doctype is in list of doctypes to be searched
          if ((doctypes2bsearched.contains("any"))
              ||(doctypes2bsearched.contains(currentDoctype))) {
             
                
            // Use the simple XPath API to obtain a node list.
 //           Log.debug(30,"starting XPathSearch: "+xpathExpression);
            boolean allHits = false;
            // there is no sense in actually returning all the text nodes
            // this, if we are searching for any text node, skip the selectNodeList
            // routine (which is time consuming) since we are going to get a 'hit'
            // no matter what
            if (xpathExpression.equals("//*[text()]")) {
              allHits = true;
            }
            else {
              nl = XPathAPI.selectNodeList(root, xpathExpression);
            }
 //           Log.debug(30,"ending XPathSearch");
            // if nl has no elements, then the document does not contain the
            // XPath expression of interest; otherwise, get the
            // corresponding dataPackage
            if ((nl != null && nl.getLength()>0)||allHits) {
              try {
                // If this docid is in any packages, record those package ids
                if (dataPackage_collection.containsKey(docid)) {
                  Vector ids = (Vector)dataPackage_collection.get(docid);
                  Enumeration q = ids.elements();
                  while (q.hasMoreElements()) {
                    Object id = q.nextElement();
                    // don't repeat elements
                    if (!package_IDs.contains(id)) {
                      package_IDs.addElement(id);
                    }
                  }
                }
              } catch (Exception rogue) {
                Log.debug(1, "Fatal error: " +
                                         "failed getting package list.");
              }
            }
          }
        } catch (Exception e2) {
          Log.debug(6,"selectNodeList threw: " + e2.toString() 
            + " perhaps your xpath didn't select any nodes");
          continue;
        }
      } else {
        Log.debug(6,"Bad input args: " + filename + ", " 
          + xpathExpression);
      }
    } // end of 'for' loop over all files
        
    curtime = System.currentTimeMillis();
    return package_IDs;
  }
  
  
  /** Determine the document type for a given file */
  private String getDoctypeFor(String docid) {
    String ret = "";
    if (doctype_collection.containsKey(docid)) {
      ret = (String)doctype_collection.get(docid);   
    }
    return ret;
  }
  
  /** Create a row vector that matches that needed for the ResultSet vector */
  private Vector createRSRow(String docid) {
    int firstSep = docid.indexOf(separator);
    String filename = docid.substring(0,firstSep) + File.separator + 
                      docid.substring(firstSep+1, docid.length());
    File fn = new File(datadir, filename);
    String fullfilename = fn.getPath();

    // Get the triples for this package
    Vector tripleList = (Vector)packageTriples.get(docid);

    // Create the result row
    Vector rss = new Vector();

    // Display the right icon for the data package
    boolean hasData = false;
    if (tripleList != null) {
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
    }
    if (hasData) {
        rss.addElement(localPackageDataIcon);
    } else {
        rss.addElement(localPackageIcon);
    }

    for (int i=0;i<returnFields.size();i++) {
      String fieldName = (String)returnFields.elementAt(i);  
      rss.addElement(getValueForPath(fieldName,docid));   
    }
    File fl = new File(fullfilename);
    // Create a time stamp for modified date. So local and metacat will have
    // same format
    Timestamp creationDate = new Timestamp(fl.lastModified()); 
    String date = creationDate.toString();
    rss.addElement(date);                                 // create date
    rss.addElement(date);                                 // update date
    rss.addElement(docid);                                // docid
    Document doc = (Document)dom_collection.get(docid);
    String docname = doc.getNodeName();
    rss.addElement(docname);                              // docname
    String thisDoctype = (String)doctype_collection.get(docid);
    rss.addElement(thisDoctype);                          // doctype
    rss.addElement(new Boolean(true));                    // isLocal
    rss.addElement(new Boolean(false));                   // isMetacat
    // Note that this tripleList does not contain the types of the
    // subject and objects identified inthe triple, so it differs
    // from the tripleList generated for metacat results
    if (tripleList != null) {
        rss.addElement(tripleList);                       // tripleList
    }

    return rss;
  }
  
  /**
   *  utility routine to return the value of a node defined by
   *  a specified XPath; used to build result set from local
   *  queries
   */
  private String getValueForPath(String pathstring, String filename) {
    String val = "";
    if (!pathstring.startsWith("/")) {
      pathstring = "//"+pathstring;
    }
    try{
      // assume that the filename file has already been parsed
      if (dom_collection.containsKey(filename)){
        Node doc = ((Document)dom_collection.get(filename)).
                   getDocumentElement();
        NodeList nl = null;
        nl = XPathAPI.selectNodeList(doc, pathstring);
        if ((nl!=null)&&(nl.getLength()>0)) {
          // loop over node list is needed if node is repeated; eg key words
          for (int k=0;k<nl.getLength();k++) {
            // assume 1st child is text node
            Node cn = nl.item(k).getFirstChild();  
            if ((cn!=null)&&(cn.getNodeType()==Node.TEXT_NODE)) {
              String temp = cn.getNodeValue().trim();
              if (val.length()>0) val = "; "+val; 
              val = temp + val;
            }
          }
        }
      }
    } catch (Exception e){
      Log.debug(6,"Error in getValueForPath method");
    }
    return val;
  }
  
  /**
   * Given a DOM document node, this method returns the DocType
   * as a String
   */
  private String getDocTypeFromDOM(Document doc){
    String ret = null;
    DocumentType ddd = doc.getDoctype();
    ret = ddd.getPublicId();
    if (ret==null) {
      ret = ddd.getSystemId();
      if (ret==null){
        ret = ddd.getName();   
      }
    }
    return ret;
  }
  
   /** 
    * given a directory, return a vector of files it contains
    * including subdirectories
    */
   private void getFiles(File directoryFile, Vector vec) {
      String[] files = directoryFile.list();
      
      for (int i=0;i<files.length;i++)
        {
            String filename = files[i];
            File currentfile = new File(directoryFile, filename);
            if (currentfile.isDirectory()) {
                getFiles(currentfile,vec);  // recursive call to subdirecctories
            }
            if (currentfile.isFile()) {
                vec.addElement(currentfile);   
            }
        }
   }
 
  /**
   * given a QueryTerm, construct a XPath expression
   */
  private String QueryTermToXPath(QueryTerm qt) {
    String xpath;
    boolean caseSensitive = qt.isCaseSensitive();
    String searchMode = qt.getSearchMode();
    String value = qt.getValue();
    if (!caseSensitive) {
      value = value.toLowerCase();
    }
    String pathExpression = qt.getPathExpression();
  
    // construct path part of XPath
    if (pathExpression==null) {
      xpath = "//*"; 
    } else if (pathExpression.startsWith("/")) {
      // path is absolute 
      xpath =   pathExpression;
    } else {
      // path is relative 
      xpath = "//"+pathExpression;
    }
  
    // wild card text search case
    if ((value==null) || (value.equals("%")) || (value.equals("*")) ||
        (value.equals("")) ) {
      xpath = xpath+"[text()]";
      return xpath;
    } else {
      if (!caseSensitive) { 
        // use translate function to convert text() to lowercase
        // check on searchMode
        if (searchMode.equals("starts-with")) {
          xpath = xpath+"[starts-with(translate(text(),"
            +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\"),\""
            +value+"\")]";    
        } else if (searchMode.equals("ends-with")) {
          xpath = xpath+"[contains(translate(text(),"
            +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\"),\""
            +value+"\")]";
          // not correct - fix later
        } else if (searchMode.equals("contains")) {
          xpath = xpath+"[contains(translate(text(),"
            +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\"),\""
            +value+"\")]";
        } else if (searchMode.equals("equals")) {
          xpath = xpath+"[translate(text(),"
            +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\")=\""
            +value+"\"]";
        } else if (searchMode.equals("less-than")) {
          xpath = xpath+"[number(text()) < "+value+"]";
        } else if (searchMode.equals("greater-than")) {
          xpath = xpath+"[number(text()) > "+value+"]";
        } else if (searchMode.equals("greater-than-equals")) {
          xpath = xpath+"[number(text()) >= "+value+"]";
        } else if (searchMode.equals("less-than-equals")) {
          xpath = xpath+"[number(text()) <= "+value+"]";
        } 
      } else {
        if (searchMode.equals("starts-with")) {
          xpath = xpath+"[starts-with(text(),\""+value+"\")]";    
        } else if (searchMode.equals("ends-with")) {
          xpath = xpath+"[contains(text(),\""+value+"\")]";
                          // not correct - fix later
        } else if (searchMode.equals("contains")) {
          xpath = xpath+"[contains(text(),\""+value+"\")]";
        } else if (searchMode.equals("equals")) {
          xpath = xpath+"[text()=\""+value+"\"]";
        } else if (searchMode.equals("less-than")) {
          xpath = xpath+"[number(text()) < "+value+"]";
        } else if (searchMode.equals("greater-than")) {
          xpath = xpath+"[number(text()) > "+value+"]";
        } else if (searchMode.equals("greater-than-equals")) {
          xpath = xpath+"[number(text()) >= "+value+"]";
        } else if (searchMode.equals("less-than-equals")) {
          xpath = xpath+"[number(text()) <= "+value+"]";
        } 
      }
    }
    return xpath;
  }               


  /**
   * local Query execution, with recursive handling of QueryGroups
   *
   * @param qg the QueryGroup containing query parameters
   */
  private Vector executeLocal(QueryGroup qg) 
  {
    Vector combined = null;
    Vector currentResults = null;
  
    Enumeration children = qg.getChildren();
    while (children.hasMoreElements()) {
      Object child = children.nextElement();
      if (child instanceof QueryTerm) {
        String xpath = QueryTermToXPath((QueryTerm)child);
        currentResults = executeXPathQuery(xpath);
      } else {  // QueryGroup
        currentResults = executeLocal((QueryGroup)child);
      }
 
      if ((currentResults==null) &&
          (qg.getOperator().equalsIgnoreCase("intersect"))) {
        // exit loop since one the results sets is null
        combined = null;
        break;
      }

      // add these results to previous ones
      if (qg.getOperator().equalsIgnoreCase("intersect")) {
        if (combined == null) {
          combined = (Vector)currentResults.clone(); // 1st time
        } else {
          Vector original = (Vector)combined.clone();
          combined = new Vector();
          for (int i = 0; i < currentResults.size(); i++) {
            Object obj = currentResults.elementAt(i);
            if (original.contains(obj)) {
              combined.addElement(obj);
            } else {
            }
          }
        }
      } else if (qg.getOperator().equalsIgnoreCase("union")) {
        if (combined==null) {
          combined = (Vector)currentResults.clone(); // 1st time
        } else {
          Enumeration q = currentResults.elements();
          while(q.hasMoreElements()) {
            String temp = (String)q.nextElement();
            if (!combined.contains(temp)) {
              combined.addElement(temp);
            }  
          }
        }
      }
    }
    return combined;
  }   

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
    ConfigXML profile = morpho.getProfile();
    currentProfile = profile.get("profilename", 0);
    profileDir = config.getConfigDirectory() + File.separator +
                       config.get("profile_directory", 0) + File.separator +
                       currentProfile;
    datadir = profileDir + File.separator + profile.get("datadir", 0);
    String searchLocalString = profile.get("searchlocal", 0);
    //searchLocal = (new Boolean(searchLocalString)).booleanValue();
    returnFields = profile.get("returnfield");
    doctypes2bsearched = profile.get("doctype");
    dt2bReturned = profile.get("returndoc");
    local_dtd_directory = config.get("local_dtd_directory", 0);
    separator = profile.get("separator", 0);
  }

  /** Main routine for testing */
  public static void main(String[] args) 
  {
    if (args.length < 1) {
      Log.debug(1, "Wrong number of arguments!!!");
      Log.debug(1, "USAGE: java LocalQuery <xmlfile>");
      return;
    } else {
      int i = 0;
      boolean useXMLIndex = true;
      String xmlfile  = args[i];

      try {
        Morpho morpho = new Morpho(
                              new ConfigXML("lib/config.xml"));
        
        FileReader xml = new FileReader(new File(xmlfile));
        Query query = new Query(xml, morpho);
         
        LocalQuery qspec = new LocalQuery(query, morpho);
        
        //Vector test = qspec.executeLocal(qspec.query);
        ResultSet rs = qspec.execute();
         
       } catch (IOException e) {
         Log.debug(1, e.getMessage());
       }
     }
  }
   
 /** 
  * build hashtable of package elements called by buildPackageList
  */
  private void addToPackageList(Node docNode, String packageDocid) {
    String subject = "";
    String relationship = "";
    String object = "";
    Node currentNode = null;
    NodeList nl = null;
    String xpathExpression = "//triple";

    // Initialize a new list of triples for this package
    Vector tripleList = new Vector();
    
    try{
      nl = XPathAPI.selectNodeList(docNode, xpathExpression);
    } catch (Exception ee) {
      Log.debug(6, "Error in building PackageList!");  
    }

    // Check if we got a match for triple nodes
    if ((nl!=null)&&(nl.getLength()>0)) {
      // Loop across all of the triple nodes
      for (int m=0;m<nl.getLength();m++) {

        // Create a hash to contain the triple information in this triple
        Hashtable triple = new Hashtable();  

        // Look for the subject, object, and relationship nodes
        NodeList nlchildren = (nl.item(m)).getChildNodes();
        for (int n=0;n<nlchildren.getLength();n++) {
          if (nlchildren.item(n).getNodeType()!=Node.TEXT_NODE) {
            if ((nlchildren.item(n)).getLocalName().equalsIgnoreCase("subject"))
            {
              currentNode = nlchildren.item(n).getFirstChild();
              subject = currentNode.getNodeValue().trim();
              triple.put("subject", subject);
            }
            if ((nlchildren.item(n)).getLocalName().equalsIgnoreCase("relationship"))
            {
              currentNode = nlchildren.item(n).getFirstChild();
              relationship = currentNode.getNodeValue().trim();   
              triple.put("relationship", relationship);
            }
            if ((nlchildren.item(n)).getLocalName().equalsIgnoreCase("object"))
            {
              currentNode = nlchildren.item(n).getFirstChild();
              object = currentNode.getNodeValue().trim();   
              triple.put("object", object);
            }
          }
        }

        // Add the triple to the tripleList for this package
        tripleList.addElement(triple);

        // add the packageDocid itself
        if (dataPackage_collection.containsKey(packageDocid)) {    
          // already in collection
          // don't do anything
        } else {  // new
          Vector vec = new Vector();
          vec.addElement(packageDocid); 
          dataPackage_collection.put(packageDocid, vec);
        }

        // add subject to the collection
        if (dataPackage_collection.containsKey(subject)) {    
          // already in collection
          Vector curvec = (Vector)dataPackage_collection.get(subject);
          curvec.addElement(packageDocid);
        } else {  // new
          Vector vec = new Vector();
          vec.addElement(packageDocid); 
          dataPackage_collection.put(subject, vec);
        }
        
        // add object to the collection
        if (dataPackage_collection.containsKey(object)) {    
          // already in collection
          Vector curvec = (Vector)dataPackage_collection.get(object);
          curvec.addElement(packageDocid);
        } else {  // new
          Vector vec = new Vector();
          vec.addElement(packageDocid); 
          dataPackage_collection.put(object, vec);
        }
      }
    }

    // Add the tripleList to the static cache of tripleLists
    if (!packageTriples.containsKey(packageDocid)) {
      packageTriples.put(packageDocid, tripleList);
    }
  }
}
