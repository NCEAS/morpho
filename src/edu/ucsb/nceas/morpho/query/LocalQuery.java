/**
 *  '$RCSfile: LocalQuery.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: anderson $'
 *     '$Date: 2005-11-28 22:22:40 $'
 * '$Revision: 1.69 $'
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
import edu.ucsb.nceas.morpho.datastore.DataStore;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
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
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
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
   * vector which contains documents that should not be searched
   * mainly, these are data documents; docs are added to the vector
   * when xml parsing fails, so system doesn't bother to try
   * parsing again
   */
  private static Vector doNotParse_collection = new Vector();
  private static Vector doNotParse_incomplete_collection = new Vector();

  /**
   * hash table with dom objects from previously scanned local XML
   * documents; serves as a cache to avoid having to re-parse documents
   * every time an XPath search is carried out. key is filename which should
   * match document id.
   */
  public static Hashtable dom_collection;
  //storing the  dom tree from in incomplete dir
  private static Hashtable dom_incomplete_collection;

  /**
   * The query on which this LocalQuery is based.
   */
  private Query savedQuery = null;

  /**
   * hash table which contains doctype information about each of
   * the locally stored XML documents
   */
  private static Hashtable doctype_collection;
  private static Hashtable doctype_incomplete_collection;
  

  /**
   * hash table with docids as key and a Vector of package IDs
   * as the values
   */
  private static Hashtable dataPackage_collection;
  private static Hashtable dataPackage_incomplete_collection;
 

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
  
  /* the directory contains the incomplete documents*/
  private String incompleteDir;

  // create these static caches when class is first loaded
  static {
    dom_collection = new Hashtable();
    dom_incomplete_collection = new Hashtable();
    doctype_collection = new Hashtable();
    doctype_incomplete_collection = new Hashtable();
    dataPackage_collection = new Hashtable();
    dataPackage_incomplete_collection = new Hashtable();
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
    
    //use the given doctypes for return
    dt2bReturned = query.getReturnDocList();

    datadir = datadir.trim();
    xmlcatalogfile = config.getConfigDirectory() + File.separator +
                              local_dtd_directory.trim()+"/catalog";
  }

  
  /**
   * Run the query against the local document store
   */
  public ResultSet execute()
  {
	   File xmldir = new File(datadir);
	   Vector filevector = new Vector();
	    // get a list of all files to be searched
	    getFiles(xmldir, filevector);
      return execute(filevector, datadir);
  }
  
  /**
   * Run the query against local incomplete doc
   * @return
   */
  public ResultSet executeInInCompleteDoc()
  {
	  File xmldir = new File(incompleteDir);
	  Vector fileVector = new Vector();
	  getFiles(xmldir, fileVector);
	  return execute(fileVector, incompleteDir);
  }
  
  /*
   * Run the query against the local document store
   */
  private ResultSet execute(Vector filevector, String fromDataDir)
  {
    // first, get a list of all packages that meet the query requirements
    Vector packageList = executeLocal(this.savedQuery.getQueryGroup(), filevector, fromDataDir);
    Vector row = null;
    Vector rowCollection = new Vector();

    // now build a Vector of Vectors (tablemodel)
    ResultSet rs = null;
    if (packageList != null) {
      Enumeration pl = packageList.elements();
      while (pl.hasMoreElements()) {
        String packageName = (String)pl.nextElement();
        row = createRSRow(packageName, fromDataDir);
        rowCollection.addElement(row);
      }
      //rs = new ResultSet(savedQuery, "local", rowCollection, morpho);
      rs = new HeadResultSet(savedQuery, rowCollection, morpho);
    }

    return rs;
  }

  /**
   *  loops recursively over all files in the 'datadir'
   *  and applies XPath search
   *
   * @param xpathExpression the XPath query string
   */
  private Vector executeXPathQuery(String xpathExpression, Vector filevector, Hashtable domCollection,
                                                   Vector doNotParseCollection, Hashtable docTypeCollection, Hashtable dataPackageCollection)
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

    //File xmldir = new File(datadir);
    //Vector filevector = new Vector();
    // get a list of all files to be searched
    //getFiles(xmldir, filevector);

    // iterate over all the files that are in the local xml directory
    for (int i=0;i<filevector.size();i++) {
      File currentfile = (File)filevector.elementAt(i);
      String filename = currentfile.getPath();
//DFH      String docid = currentfile.getParentFile().getName() + separator +
//DFH                     currentfile.getName();
      File parentFile = new File(currentfile.getParent());
      String docid = parentFile.getName() + separator + currentfile.getName();

      // skips subdirectories and docs in doNotParse collection
      if ((currentfile.isFile())&&(!doNotParseCollection.contains(docid))) {
        // checks to see if doc has already been placed in DOM cache
        // if so, no need to parse again
        //Log.debug(10,"current id: "+docid);
        if (domCollection.containsKey(docid)){
          root = ((Document)domCollection.get(docid)).getDocumentElement();
          if (docTypeCollection.containsKey(docid)) {
            currentDoctype = ((String)docTypeCollection.get(docid));
          }
        } else {
        //  Log.debug(10,"parsing "+docid);
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
            doNotParseCollection.addElement(docid);
            continue;
          }

          // Get the documentElement from the parser, which is what
          // the selectNodeList method expects
          root = current_doc.getDocumentElement();
          domCollection.put(docid,current_doc);
          String temp = getDocTypeFromDOM(current_doc);
          if (temp==null) temp = root.getNodeName();
          docTypeCollection.put(docid,temp);
          currentDoctype = temp;
        } // end else
        
        if ((dt2bReturned.contains("any")) || (dt2bReturned.contains(currentDoctype))) {
            addToPackageList(root, docid, dataPackageCollection);
        }
        
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
            // thus, if we are searching for any text node, skip the selectNodeList
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
                if (dataPackageCollection.containsKey(docid)) {
                	String doctype = (String) docTypeCollection.get(docid);
                	if (dt2bReturned.contains(doctype)) {
		                  Vector ids = (Vector)dataPackageCollection.get(docid);
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
 //       Log.debug(6,"Bad input args: " + filename + ", "
 //         + xpathExpression);
      }
    } // end of 'for' loop over all files

    curtime = System.currentTimeMillis();
    return package_IDs;
  }



  /** Create a row vector that matches that needed for the ResultSet vector */
  private Vector createRSRow(String docid, String sourceDirectory) {
    int firstSep = docid.indexOf(separator);
    String filename = docid.substring(0,firstSep) + File.separator +
                      docid.substring(firstSep+1, docid.length());
    File fn = new File(sourceDirectory, filename);
    String fullfilename = fn.getPath();
    String localStatus = QueryRefreshInterface.LOCALCOMPLETE;
    Hashtable domCollection = null;
    Hashtable doctypeCollection = null;
    if(sourceDirectory != null && sourceDirectory.equals(incompleteDir))
    {
      domCollection = dom_incomplete_collection;
      doctypeCollection = doctype_incomplete_collection;
      String traceValue = getValueForPath(IncompleteDocSettings.TRACINGCHANGEPATH, fullfilename, incompleteDir);
      //Log.debug(5, "traceValue on LocalQuery.createRSRow is "+traceValue);
      if(traceValue != null && traceValue.equals(IncompleteDocSettings.TRUE))
      {
        localStatus = QueryRefreshInterface.LOCALAUTOSAVEDINCOMPLETE;
      }
      else
      {
        localStatus = QueryRefreshInterface.LOCALUSERSAVEDINCOMPLETE;
      }
    }
    else
    {
      domCollection = dom_collection;
      doctypeCollection = doctype_collection;
    }

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
    // now consider eml2 case where there are no triples
    String ent = getValueForPath("entityName", docid, sourceDirectory);
    if (ent.length()>0) hasData = true;

    if (hasData) {
        rss.addElement(localPackageDataIcon);
    } else {
        rss.addElement(localPackageIcon);
    }

//DFH    for (int i=0;i<returnFields.size();i++) {
// in order to handle both datapackages with triples and those (e.g. eml2) without
// a returnField which looks for 'entityName' fields has been included in the
// query. For some docs this returnField will not be present. Thus, only the first 3
// of the return fields are added to the ResultSet table. [The 'entityName' returnField
// MUST be listed AFTER title, surname, and keyword returnFields]
    for (int i=0;i<3;i++) {
      String fieldName = (String)returnFields.elementAt(i);
      rss.addElement(getValueForPath(fieldName,docid, sourceDirectory));
    }
    File fl = new File(fullfilename);
    // Create a time stamp for modified date. So local and metacat will have
    // same format
    Timestamp creationDate = new Timestamp(fl.lastModified());
    String date = creationDate.toString();
    rss.addElement(date);                                 // create date
    rss.addElement(date);                                 // update date
    rss.addElement(docid);                                // docid
    Document doc = (Document)domCollection.get(docid);
    String docname = doc.getNodeName();
    rss.addElement(docname);                              // docname
    String thisDoctype = (String)doctypeCollection.get(docid);
    rss.addElement(thisDoctype);                          // doctype
    //rss.addElement(new Boolean(true));                    // isLocal
    //rss.addElement(new Boolean(false));                   // isMetacat
    rss.addElement(localStatus);                    // isLocal
    rss.addElement(QueryRefreshInterface.NONEXIST); // isMetacat
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
  private String getValueForPath(String pathstring, String filename, String sourceDir) {
    String val = "";
    if (!pathstring.startsWith("/")) {
      pathstring = "//"+pathstring;
    }
    Hashtable domCollection = null;
    if(sourceDir != null && sourceDir.equals(incompleteDir))
    {
      domCollection = dom_incomplete_collection;
    }
    else
    {
      domCollection = dom_collection;
    }
    Log.debug(40, "filename in getValueForpath is "+filename);
    try{
      // assume that the filename file has already been parsed
      if (domCollection.containsKey(filename)){
        Node doc = ((Document)domCollection.get(filename)).
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
   * Modified to return the namespace if there is no dtd doctype
   */
  private String getDocTypeFromDOM(Document doc){
    String ret = null;
    DocumentType ddd = doc.getDoctype();
    if (ddd==null) {
      // most likely reason that we get here is that the document is based on a schema
      // i.e. a namespace rather than a dtd; thus look for NS info
      Node root = doc.getDocumentElement();
      String temp = root.getNamespaceURI();
      if (temp!=null) {
        return temp;
      } else {
        return ret;
      }
    }
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
                try {
                    Double d = Double.valueOf(filename);
                    vec.addElement(currentfile);
                } catch (NumberFormatException nfe) {
                    Log.debug(30, "Not loading file with invalid name: " + filename);
                }
            }
        }
        getLatestVersion(vec);
   }

  /**
   *  modify list to only contain latest version as indicated by a trailing
   *  version number
   *  This is to reduce the search time by avoiding older versions
   */
  private void getLatestVersion(Vector vec) {
      
    String dot = ".";
    if(vec != null)
    {
      Hashtable<String, Integer> maxVersions = new Hashtable<String, Integer>();
      for(int i=0; i<vec.size();i++)
      {
        File file = (File)(vec.elementAt(i));
        if(file != null)
        {
          String name1 = file.getAbsolutePath();
          try
          {
            int periodloc = name1.lastIndexOf(dot);
            String namestart = name1.substring(0,periodloc);
            if(namestart != null)
            {
              String vernum = name1.substring(periodloc+1,name1.length());
              Integer intVer = new Integer(vernum);
              if(maxVersions.contains(namestart))
              {
                Integer currentMax = maxVersions.get(namestart);
                if(currentMax != null && currentMax.intValue() > intVer.intValue())
                {
                  //we already stored a greater version, so skip this one
                  continue;
                }
              }
              maxVersions.put(namestart, intVer);
            }
          }
          catch(Exception e)
          {
            continue;
          }
        }
      }
      //put maxVersions into the new vector
      Enumeration enumeration = maxVersions.keys();
      vec.removeAllElements();
      while (enumeration.hasMoreElements()) {
          String nameStart = (String)enumeration.nextElement();
          if(nameStart != null)
          {
            Integer version  = maxVersions.get(nameStart);
            if(version != null)
            {
              vec.add(new File(nameStart+dot+version.toString()));
            }
          }
      }
    }
    /*Vector newvec = new Vector();
    for (int j=0;j<vec.size();j++) {
      File file = (File)vec.elementAt(j);
      String name = file.getName();
      newvec.addElement(name);
    }
    // now string ids are in newvec
    int cnt = 0;
    for (int k=0;k<newvec.size();k++) {
      String name1 = (String)(newvec.elementAt(k));
      int periodloc = name1.lastIndexOf(".");
      String namestart = name1.substring(0,periodloc);
      String vernum = name1.substring(periodloc+1,name1.length());
      Integer Intver = new Integer(vernum);
      int iver = Intver.intValue();
      String newstr = namestart+"."+(iver+1);
      if (newvec.contains(newstr)) {
        vec.removeElementAt(k - cnt);
        cnt++;
      }
    }*/
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
    String textSelector = "text()";
    // attribute nodes do not have text nodes
    if (pathExpression != null && pathExpression.contains("@")) {
    	textSelector = ".";
    }
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
      xpath = xpath+"["+textSelector+"]";
      return xpath;
    } else {
      if (!caseSensitive) {
        // use translate function to convert text() to lowercase
        // check on searchMode
        if (searchMode.equals("starts-with")) {
          xpath = xpath+"[starts-with(translate("+textSelector+","
            +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\"),\""
            +value+"\")]";
        } else if (searchMode.equals("ends-with")) {
          xpath = xpath+"[contains(translate("+textSelector+","
            +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\"),\""
            +value+"\")]";
          // not correct - fix later
        } else if (searchMode.equals("contains")) {
          xpath = xpath+"[contains(translate("+textSelector+","
            +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\"),\""
            +value+"\")]";
        } else if (searchMode.equals("equals")) {
          xpath = xpath+"[translate("+textSelector+","
            +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\")=\""
            +value+"\"]";
        } else if (searchMode.equals("less-than")) {
          xpath = xpath+"[number("+textSelector+") < "+value+"]";
        } else if (searchMode.equals("greater-than")) {
          xpath = xpath+"[number("+textSelector+") > "+value+"]";
        } else if (searchMode.equals("greater-than-equals")) {
          xpath = xpath+"[number("+textSelector+") >= "+value+"]";
        } else if (searchMode.equals("less-than-equals")) {
          xpath = xpath+"[number("+textSelector+") <= "+value+"]";
        }
      } else {
        if (searchMode.equals("starts-with")) {
          xpath = xpath+"[starts-with("+textSelector+",\""+value+"\")]";
        } else if (searchMode.equals("ends-with")) {
          xpath = xpath+"[contains("+textSelector+",\""+value+"\")]";
                          // not correct - fix later
        } else if (searchMode.equals("contains")) {
          xpath = xpath+"[contains("+textSelector+",\""+value+"\")]";
        } else if (searchMode.equals("equals")) {
          xpath = xpath+"["+textSelector+"=\""+value+"\"]";
        } else if (searchMode.equals("less-than")) {
          xpath = xpath+"[number("+textSelector+") < "+value+"]";
        } else if (searchMode.equals("greater-than")) {
          xpath = xpath+"[number("+textSelector+") > "+value+"]";
        } else if (searchMode.equals("greater-than-equals")) {
          xpath = xpath+"[number("+textSelector+") >= "+value+"]";
        } else if (searchMode.equals("less-than-equals")) {
          xpath = xpath+"[number("+textSelector+") <= "+value+"]";
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
  private Vector executeLocal(QueryGroup qg, Vector filevector, String sourceDir)
  {
    Vector combined = null;
    Vector currentResults = null;

    if (qg == null) {
    	return new Vector();
    }
    Enumeration children = qg.getChildren();
    while (children.hasMoreElements()) {
      Object child = children.nextElement();
      if (child instanceof QueryTerm) {
        String xpath = QueryTermToXPath((QueryTerm)child);
        if(sourceDir != null && sourceDir.equals(incompleteDir))
        {
          currentResults = executeXPathQuery(xpath, filevector, dom_incomplete_collection,
                                  doNotParse_incomplete_collection, doctype_incomplete_collection, dataPackage_incomplete_collection);
        }
        else
        {
          currentResults = executeXPathQuery(xpath, filevector,dom_collection,
                   doNotParse_collection, doctype_collection, dataPackage_collection);
        }
        
      } else {  // QueryGroup
        currentResults = executeLocal((QueryGroup)child, filevector, sourceDir);
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
    returnFields = config.get("returnfield");
    doctypes2bsearched = config.get("doctype");
    dt2bReturned = config.get("returndoc");
    local_dtd_directory = config.get("local_dtd_directory", 0);
    separator = profile.get("separator", 0);
    String incomplete = profile.get("incompletedir", 0);
    //in case no incomplete dir in old version profile
    if(incomplete == null || incomplete.trim().equals(""))
    {
    	incompleteDir = profileDir+ File.separator + DataStore.INCOMPLATEDIR;
    }
    else
    {
    	incompleteDir = profileDir + File.separator + incomplete;
    }
  }

  /*
   * remove the indicated doc from all local caches
   */
  public static void removeFromCache(String docid) {
    dom_collection.remove(docid);
    doctype_collection.remove(docid);
    dataPackage_collection.remove(docid);
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
  private void addToPackageList(Node docNode, String packageDocid, Hashtable dataPackageCollection) {
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
        if (dataPackageCollection.containsKey(packageDocid)) {
          // already in collection
          // don't do anything
        } else {  // new
          Vector vec = new Vector();
          vec.addElement(packageDocid);
          dataPackageCollection.put(packageDocid, vec);
        }

        // add subject to the collection
        if (dataPackageCollection.containsKey(subject)) {
          // already in collection
          Vector curvec = (Vector)dataPackageCollection.get(subject);
          curvec.addElement(packageDocid);
        } else {  // new
          Vector vec = new Vector();
          vec.addElement(packageDocid);
          dataPackageCollection.put(subject, vec);
        }

        // add object to the collection
        if (dataPackageCollection.containsKey(object)) {
          // already in collection
          Vector curvec = (Vector)dataPackageCollection.get(object);
          curvec.addElement(packageDocid);
        } else {  // new
          Vector vec = new Vector();
          vec.addElement(packageDocid);
          dataPackageCollection.put(object, vec);
        }
      }
    }

    // add the packageDocid itself
    // needed here to handle case where packageDoc does NOT contain triple (e.g. eml2)
    if (dataPackageCollection.containsKey(packageDocid)) {
      // already in collection
      // don't do anything
    } else {  // new
      Vector vec = new Vector();
      vec.addElement(packageDocid);
      dataPackageCollection.put(packageDocid, vec);
    }

    // Add the tripleList to the static cache of tripleLists
    if (!packageTriples.containsKey(packageDocid)) {
      packageTriples.put(packageDocid, tripleList);
    }
  }
}
