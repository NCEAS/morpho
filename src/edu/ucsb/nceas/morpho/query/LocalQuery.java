/**
 *  '$RCSfile: LocalQuery.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-30 23:00:26 $'
 * '$Revision: 1.36 $'
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

import java.io.*;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xalan.xpath.xml.FormatterToXML;
import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.arbortext.catalog.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Date;
import javax.swing.ImageIcon;

import edu.ucsb.nceas.morpho.framework.*;

/**
 * LocalQuery is a class designed to execute a query defined in
 * an XML document that follows the PathQuery DTD. The path based
 * query statements are converted to XPath expressions and the
 * XPath API features of Apache Xalan/Xerces are used to find
 * nodes specified by the expressions. All XML documents in a given
 * local subdirectory are scanned. The process thus mimics the database
 * search on Metacat, but is applied only to local documents.
 * 
 * @author higgins
 */
public class LocalQuery
{

  /**
   * hash table with dom objects from previously scanned local XML
   * documents; serves as a cache to avoid having to re-parse documents
   * every time an XPath search is carried out. key is filename which should
   * match document id.
   */
  static Hashtable dom_collection;

  /**
   * The query on which this LocalQuery is based.
   */
  private Query savedQuery = null;

  /**
   * hash table which contains doctype information about each of
   * the locally stored XML documents
   */
  static Hashtable doctype_collection;
  
  /**
   * hash table with docids as key and a Vector of package IDs
   * as the values
   */
  static Hashtable dataPackage_collection; 
  
  /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;
    
  /** The directory containing all local stored metadata */
  private String local_xml_directory;
  
  /** The directory containing locally stored dtds */
  private String local_dtd_directory;
  
  /** The file used by the catalog system for looking up public identifiers */
  private String xmlcatalogfile; 
 
  /** list of field to be returned from query */
  private Vector returnFields;
  
  /** list of doctypes to be searched */
  private Vector doctypes2bsearched;

  /** list of doctypes to be returned */
  private Vector dt2bReturned;
 
  /** Doctype of document currently being queried */
  private String currentDoctype;
    
  /** The folder icon for representing local storage. */
  private ImageIcon localIcon = null;
    
  // create these static caches when class is first loaded
  static {
    dom_collection = new Hashtable();
    doctype_collection = new Hashtable();
    dataPackage_collection = new Hashtable();
    buildPackageList();    
  }
    
  /**
   * Basic Constuctor for the class
   * 
   * @param query the query on which this Local query is based
   * @param framework the framework
   */
  public LocalQuery(Query query, ClientFramework framework) {
    super();
    this.savedQuery = query;
  
    localIcon = new ImageIcon( getClass().
                getResource("local.gif"));
  
    this.framework = framework;
    this.config = framework.getConfiguration();   
  
    loadConfigurationParameters();
      
    local_xml_directory = local_xml_directory.trim();
    xmlcatalogfile = local_dtd_directory.trim()+"/catalog"; 
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
      rs = new ResultSet(savedQuery, "local", rowCollection, framework);
    } 

    return rs;
  }

  /**
   *  loops recursively over all files in the 'local_xml_directory'
   *  and applies XPath search
   *
   * @param xpathExpression the XPath query string
   */
  private Vector executeXPathQuery(String xpathExpression)
  {
    Vector package_IDs = new Vector();
    Node root;
    long starttime, curtime, fm;
    DOMParser parser = new DOMParser();
    // first set up the catalog system for handling locations of DTDs
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      myCatalog.parseCatalog(xmlcatalogfile);
      cer.setCatalog(myCatalog);
    } catch (Exception e) {
      ClientFramework.debug(9,"Problem creating Catalog!" + e.toString());
    }
    parser.setEntityResolver(cer);
    // set start time variable
    starttime = System.currentTimeMillis();
    StringWriter sw = new StringWriter();

    File xmldir = new File(local_xml_directory);
    Vector filevector = new Vector();
    // get a list of all files to be searched
    getFiles(xmldir, filevector);
  
    // iterate over all the files that are in the local xml directory
    for (int i=0;i<filevector.size();i++) {
      File currentfile = (File)filevector.elementAt(i);
      String filename = currentfile.getPath();
      /*ClientFramework.debug(9, "Searching local file: " + filename);*/
      
      // skips subdirectories
      if (currentfile.isFile()) {
          // checks to see if doc has already been placed in DOM cache
          // if so, no need to parse again
        if (dom_collection.containsKey(filename)){
          root = ((Document)dom_collection.get(filename)).getDocumentElement();
          if (doctype_collection.containsKey(filename)) {
            currentDoctype = ((String)doctype_collection.get(filename));   
          }
        } else {
          InputSource in;
          try {
            in = new InputSource(new FileInputStream(filename));
          } catch (FileNotFoundException fnf) {
            ClientFramework.debug(9,"FileInputStream of " + filename + 
                               " threw: " + fnf.toString());
            //fnf.printStackTrace();
            continue;
          }
          try {
            parser.parse(in);
          }
          catch(Exception e1) {
            ClientFramework.debug(9,"Parsing " + filename + 
                               " threw: " + e1.toString());
            //e1.printStackTrace();
            continue;
          }

          // Get the documentElement from the parser, which is what 
          // the selectNodeList method expects
          Document current_doc = parser.getDocument();
          root = parser.getDocument().getDocumentElement();
          dom_collection.put(filename,current_doc);
          String temp = getDocTypeFromDOM(current_doc);
          if (temp==null) temp = root.getNodeName();
          doctype_collection.put(filename,temp);
          currentDoctype = temp;
        } // end else
      
        String rootname = root.getNodeName();
        NodeList nl = null;
        try {
            // see if current doctype is in list of doctypes to be searched
          if ((doctypes2bsearched.contains("any"))
                ||(doctypes2bsearched.contains(currentDoctype))) {
                 
              // Use the simple XPath API to obtain a node list.
              nl = XPathAPI.selectNodeList(root, xpathExpression);
              // if nl has no elements, then the document does not contain the
              // XPath expression of interest; otherwise, get the
              // corresponding dataPackage
              if (nl.getLength()>0) {
                Vector ids = getPackageID(getLastPathElement(filename));
                Enumeration q = ids.elements();
                while (q.hasMoreElements()) {
                  Object id = q.nextElement();
                  // don't repeat elements
                  if (!package_IDs.contains(id)) {
                    package_IDs.addElement(id);
                  }
                }
              }
          }
        }
        catch (Exception e2)
        {
          ClientFramework.debug(9,"selectNodeList threw: " + e2.toString() 
            + " perhaps your xpath didn't select any nodes");
          // e2.printStackTrace();
          continue;
        }
      }
      else
      {
        ClientFramework.debug(9,"Bad input args: " + filename + ", " 
          + xpathExpression);
      }
   } // end of 'for' loop over all files
        
 
    curtime = System.currentTimeMillis();
    return package_IDs;
  }
  
  
  /** Determine the document type for a given file */
  private String getDoctypeFor(String filename) {
    String ret = "";
    if (doctype_collection.containsKey(filename)) {
      ret = (String)doctype_collection.get(filename);   
    }
    return ret;
  }
  
  /** Create a row vector that matches that needed for the ResultSet vector */
  private Vector createRSRow(String filename) {
    File fn = new File(local_xml_directory, filename);
    String fullfilename = fn.getPath();
    Vector rss = new Vector();
    // add icon
    rss.addElement(localIcon);

    for (int i=0;i<returnFields.size();i++) {
      String tmp = (String)returnFields.elementAt(i);  
      rss.addElement(getValueForPath(tmp,fullfilename));   
    }
    File fl = new File(fullfilename);
    Date creationDate = new Date(fl.lastModified());
    String date = creationDate.toString();
    rss.addElement(date);                                 // create date
    rss.addElement(date);                                 // update date
    rss.addElement(filename);                             // docid
    Document doc = (Document)dom_collection.get(fullfilename);
    String docname = doc.getNodeName();
    rss.addElement(docname);                              // docname
    String temp = (String)doctype_collection.get(fullfilename);
    rss.addElement(temp);                                 // doctype
    rss.addElement(new Boolean(true));                    // isLocal
    rss.addElement(new Boolean(false));                   // isMetacat
    // need to add the triple list vector, but the current
    // data structure differs from the one in ResultSet so need
    // to decide on a common structure
    //rss.addElement(new Vector());                       // tripleList

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
      pathstring = "//*/"+pathstring;
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
      ClientFramework.debug(9,"Error in getValueForPath method");
    }
    return val;    
  }
  
  /**
   * Given a DOM document node, this method returns the DocType
   * as a String
   */
  private static String getDocTypeFromDOM(Document doc){
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
   private static void getFiles(File directoryFile, Vector vec) {
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
 
   
   /** use to get the last element in a path string */
   static private String getLastPathElement(String str) {
        String last = "";
        String sep = System.getProperty("file.separator");
        int ind = str.lastIndexOf(sep);
        if (ind==-1) {
           last = str;     
        }
        else {
           last = str.substring(ind+1);     
        }
        return last;
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
        } else if (searchMode.equals("matches-exactly")) {
          xpath = xpath+"[translate(text(),"
            +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\")=\""
            +value+"\"]";
        } 
      } else {
        if (searchMode.equals("starts-with")) {
          xpath = xpath+"[starts-with(text(),\""+value+"\")]";    
        } else if (searchMode.equals("ends-with")) {
          xpath = xpath+"[contains(text(),\""+value+"\")]";
                          // not correct - fix later
        } else if (searchMode.equals("contains")) {
          xpath = xpath+"[contains(text(),\""+value+"\")]";
        } else if (searchMode.equals("matches-exactly")) {
          xpath = xpath+"[text()=\""+value+"\"]";
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
            Object obj = currentResults.get(i);
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
    String searchLocalString = config.get("searchlocal", 0);
    //searchLocal = (new Boolean(searchLocalString)).booleanValue();
    returnFields = config.get("returnfield");
    doctypes2bsearched = config.get("doctype");
    dt2bReturned = config.get("returndoc");
    local_dtd_directory = config.get("local_dtd_directory", 0);
    local_xml_directory = config.get("local_xml_directory", 0);
  }

  /** Main routine for testing */
  static public void main(String[] args) 
  {
    if (args.length < 1) {
      ClientFramework.debug(9, "Wrong number of arguments!!!");
      ClientFramework.debug(9, "USAGE: java LocalQuery <xmlfile>");
      return;
    } else {
      int i = 0;
      boolean useXMLIndex = true;
      String xmlfile  = args[i];

      try {
        ClientFramework cf = new ClientFramework(
                              new ConfigXML("lib/config.xml"));
        
        FileReader xml = new FileReader(new File(xmlfile));
        Query query = new Query(xml, cf);
         
        LocalQuery qspec = new LocalQuery(query, cf);
        
        //Vector test = qspec.executeLocal(qspec.query);
        ResultSet rs = qspec.execute();
         
       } catch (IOException e) {
         ClientFramework.debug(9, e.getMessage());
       }
     }
  }
   
 /** 
  * routine to get the package(s) that any document is contained in 
  * returns a vector since a document can be in multiple packages
  * current just returns itself
  */
  private Vector getPackageID(String docid) {
    Vector ret = (Vector)dataPackage_collection.get(docid);
    //ret.addElement(docid);  // temp
    return ret;
  }

 /** 
  * build hashtable of package elements called by buildPackageList
  */
  static private void addToPackageList(Node nd, String fn) {
    String subject = "";
    String object = "";
    Node root = nd;
    NodeList nl = null;
    String xpathExpression = "//triple";
    try{
      nl = XPathAPI.selectNodeList(root, xpathExpression);
    }
    catch (Exception ee) {
      ClientFramework.debug(9, "Error in building PackageList!");  
    }
    if ((nl!=null)&&(nl.getLength()>0)) {
      for (int m=0;m<nl.getLength();m++) {
      NodeList nlchildren = (nl.item(m)).getChildNodes();
        for (int n=0;n<nlchildren.getLength();n++) {
          if (nlchildren.item(n).getNodeType()!=Node.TEXT_NODE) {
            if ((nlchildren.item(n)).getLocalName().equalsIgnoreCase("subject"))
            {
              nd = nlchildren.item(n).getFirstChild();
              subject = nd.getNodeValue().trim();
            }
            if ((nlchildren.item(n)).getLocalName().equalsIgnoreCase("object"))
            {
              nd = nlchildren.item(n).getFirstChild();
              object = nd.getNodeValue().trim();   
            }
          }
        }
        // add subject to the collection
        if (dataPackage_collection.containsKey(subject)) {    
          // already in collection
          Vector curvec = (Vector)dataPackage_collection.get(subject);
          curvec.addElement(fn);
        }
        else {  // new
          Vector vec = new Vector();
          vec.addElement(fn); 
          dataPackage_collection.put(subject, vec);
        }
        // add object to the collection
        if (dataPackage_collection.containsKey(object)) {    
          // already in collection
          Vector curvec = (Vector)dataPackage_collection.get(object);
          curvec.addElement(fn);
        }
        else {  // new
          Vector vec = new Vector();
          vec.addElement(fn); 
          dataPackage_collection.put(object, vec);
        }
      }
    }
  }
 
  /** 
   * Builds package list by looping over all files
   * in the local XML directory
   * Uses XPath to find all <triple> elements and builds list
   */
  private static void buildPackageList() 
  {
    Node root;
    long starttime, curtime, fm;
    DOMParser parser = new DOMParser();
    // first set up the catalog system for handling locations of DTDs
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      myCatalog.parseCatalog("./lib/catalog/catalog");
      cer.setCatalog(myCatalog);
    } catch (Exception e) {
      ClientFramework.debug(9, "Problem creating Catalog!" + e.toString());
    }
    parser.setEntityResolver(cer);
    // set start time variable
    starttime = System.currentTimeMillis();
    StringWriter sw = new StringWriter();

    File xmldir = new File("./XMLworkFolder");
    Vector filevector = new Vector();
    // get a list of all files to be searched
    getFiles(xmldir, filevector);
  
    // iterate over all the files that are in the local xml directory
    for (int i=0;i<filevector.size();i++) {
      File currentfile = (File)filevector.elementAt(i);
      String filename = currentfile.getPath();
      
      // skips subdirectories
      if (currentfile.isFile()) {
        // checks to see if doc has already been placed in DOM cache
        // if so, no need to parse again
        if (dom_collection.containsKey(filename)){
          root = ((Document)dom_collection.get(filename)).getDocumentElement();
        } else {
          InputSource in;
          try {
            in = new InputSource(new FileInputStream(filename));
          } catch (FileNotFoundException fnf) {
            ClientFramework.debug(9, "FileInputStream of " + filename + 
                               " threw: " + fnf.toString());
            continue;
          }
          try {
            parser.parse(in);
          }
          catch(Exception e1) {
            ClientFramework.debug(9, "Parsing " + filename + 
                               " threw: " + e1.toString());
            continue;
          }

          // Get the documentElement from the parser, which is what 
          // the selectNodeList method expects
          Document current_doc = parser.getDocument();
          root = parser.getDocument().getDocumentElement();
          dom_collection.put(filename,current_doc);
          String temp = getDocTypeFromDOM(current_doc);
          if (temp==null) temp = root.getNodeName();
          doctype_collection.put(filename,temp);
        } // end else
      
        addToPackageList(root, getLastPathElement(filename));  
      
      } else {
        ClientFramework.debug(9, "Bad input args: " + filename + ", " );
      }
    } // end of 'for' loop over all files
        
    curtime = System.currentTimeMillis();
    ClientFramework.debug(9, "Build Package List Completed");
  }
}
