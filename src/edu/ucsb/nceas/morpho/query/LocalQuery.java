/**
 *  '$RCSfile: LocalQuery.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-05-11 23:12:12 $'
 * '$Revision: 1.21 $'
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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import com.arbortext.catalog.*;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Stack;

import edu.ucsb.nceas.morpho.framework.*;

public class LocalQuery extends DefaultHandler
{
  /** The string representation of the pathquery (XML format) */
  private String queryString;
  
  /** A reference to the container framework */
  private ClientFramework framework = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;
    
    static Hashtable dom_collection;
    static Hashtable doctype_collection;
    String local_xml_directory;
    String local_dtd_directory;
    String xmlcatalogfile; 
//    String xpathExpression;
    String xmlFileFolder;
    String[] headers = {"File Name", "Document Type", "Node Name", "Text"};
    Vector returnFields;
    Vector doctypes2bsearched;
    Vector dt2bReturned;
    String currentDoctype;
    
    
  boolean Andflag = false;
  boolean AndResultFlag;

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
   
  static {
    dom_collection = new Hashtable();
    doctype_collection = new Hashtable();
  }
    
public LocalQuery(Reader queryspec, ClientFramework framework) {
  super();
 // this.framework = framework;
  this.config = framework.getConfiguration();   

  loadConfigurationParameters();
      
  local_xml_directory = local_xml_directory.trim();
  xmlcatalogfile = local_dtd_directory.trim()+"/catalog"; 
  xmlFileFolder = local_xml_directory.trim();

    // Initialize the members
    doctypeList = new Vector();
    elementStack = new Stack();
    queryStack   = new Stack();
    returnFieldList = new Vector();
    ownerList = new Vector();
    siteList = new Vector();
    this.framework = framework;
    this.config = framework.getConfiguration();


    // Store the text of the initial query
    StringBuffer qtext = new StringBuffer();
    int len = 0;
    char[] characters = new char[512];
    try {
      while ((len = queryspec.read(characters, 0, 512)) != -1) {
        qtext.append(characters);
        characters = new char[512];
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
   * construct an instance of the LocalQuery class 
   *
   * @param queryspec the XML representation of the query (should conform
   *                  to pathquery.dtd) as a String
   * @param parserName the fully qualified name of a Java Class implementing
   *                  the org.xml.sax.Parser interface
   */
  public LocalQuery( String queryspec, ClientFramework framework)
         //throws IOException 
  {
    this(new StringReader(queryspec), framework);
  }

public void setXmlcatalogfile(String str) {
    this.xmlcatalogfile = str;
}

public void setXmlFileFolder(String str) {
    this.xmlFileFolder = str;
}

    
    
    
    
void queryAll(String xpathExpression) {
  Node root;
  long starttime, curtime, fm;
  DOMParser parser = new DOMParser();
  CatalogEntityResolver cer = new CatalogEntityResolver();
  try {
    System.out.println("xmlcatalogfile is: "+xmlcatalogfile);
    Catalog myCatalog = new Catalog();
    System.out.println("new catalog created!");
    myCatalog.loadSystemCatalogs();
    System.out.println("loadSystemCatalogs completed!");
    myCatalog.parseCatalog(xmlcatalogfile);
    cer.setCatalog(myCatalog);
  }
  catch (Exception e) {System.out.println("Problem creating Catalog!" + e.toString());}
  parser.setEntityResolver(cer);
  starttime = System.currentTimeMillis();
  StringWriter sw = new StringWriter();

  File xmldir = new File(local_xml_directory);
  Vector filevector = new Vector();
  getFiles(xmldir, filevector);
  
  // iterate over all the files that are in the local xml directory
  for (int i=0;i<filevector.size();i++) {
    File currentfile = (File)filevector.elementAt(i);
    String filename = currentfile.getPath();

    if (currentfile.isFile()) {
          // above line skips subdirectories
      if (dom_collection.containsKey(filename)){
        // checks to see if doc has already been placed in DOM cache
        root = ((Document)dom_collection.get(filename)).getDocumentElement();
        if (doctype_collection.containsKey(filename)) {
          currentDoctype = ((String)doctype_collection.get(filename));   
        }
      }
      else {
        InputSource in;
          try {
            in = new InputSource(new FileInputStream(filename));
          }
          catch (FileNotFoundException fnf) {
            System.err.println("FileInputStream of " + filename + " threw: " + fnf.toString());
            fnf.printStackTrace();
            return;
          }
          try {
            parser.parse(in);
          }
          catch(Exception e1) {
            System.err.println("Parsing " + filename + " threw: " + e1.toString());
            e1.printStackTrace();
            continue;
          }

      // Get the documentElement from the parser, which is what the selectNodeList method expects
          Document current_doc = parser.getDocument();
          root = parser.getDocument().getDocumentElement();
          dom_collection.put(filename,current_doc);
          String temp = getDocTypeFromDOM(current_doc);
          if (temp==null) temp = root.getNodeName();
          doctype_collection.put(filename,temp);
          currentDoctype = temp;
      }
      
      String rootname = root.getNodeName();
      NodeList nl = null;
      try {
        boolean search_flag = true;
          // first see if current doctype is in list of doctypes to be searched
        if ((doctypes2bsearched.contains("any"))
                ||(doctypes2bsearched.contains(currentDoctype))) {
                 
        }
        else {
          search_flag = false;
        }
        if (search_flag) { 
                
          // Use the simple XPath API to obtain a node list.
          nl = XPathAPI.selectNodeList(root, xpathExpression);

        
        }  
      }
      catch (Exception e2)
      {
        System.err.println("selectNodeList threw: " + e2.toString() 
          + " perhaps your xpath didn't select any nodes");
        e2.printStackTrace();
        return;
      }
    }
    else
    {
      System.out.println("Bad input args: " + filename + ", " + xpathExpression);
    }
    
        }
        
 
      curtime = System.currentTimeMillis();
      System.out.println("total time: "+(int)(curtime-starttime));
    }
  
  
 // public void run() {
 //   queryAll();  
 // }
    
  private void addRowsToTable(String hitfilename) {
    Vector temp = getResultSetDocs(hitfilename);
    for (Enumeration e = temp.elements();e.hasMoreElements();) {
        String fn = (String)e.nextElement();
        String[] row = createRSRow(fn);
//        dtm.addRow(row);
    }   
  }
  
  // given the filename of a doc where a 'hit' has occured, return a vector of names of related docs	
  private Vector getResultSetDocs(String filename) {
    Vector result = new Vector();
    String currentDoctype = getDoctypeFor(filename);
    if (!dt2bReturned.contains("any")) {
    //first see if the current doc type is in return list
    if (dt2bReturned.contains(currentDoctype)) {
        result.addElement(filename);   
    }
    // now check if objects of relationship are of types to be returned
    Vector objs = getRelationshipObjects(filename);
    for (Enumeration e=objs.elements();e.hasMoreElements();) {
        String obj = (String)e.nextElement();
        if (dt2bReturned.contains(obj)) {
            result.addElement(obj);          
        }
    // now check if subjects of each object are of types to be returned i.e. backtracking
        Vector subs = getRelationshipSubjects(obj);  
        for (Enumeration w=objs.elements();w.hasMoreElements();) {
            String sub = (String)w.nextElement();
            if (dt2bReturned.contains(sub)) {
                result.addElement(sub);          
            }
        }
    }
    }
    else { // no dt2bReturned types
        result.addElement(filename);   
    }
  return result;  
  }

	private String getDoctypeFor(String filename) {
	    String ret = "";
	    if (doctype_collection.containsKey(filename)) {
	        ret = (String)doctype_collection.get(filename);   
	    }
	return ret;
	}
	
	private Vector getRelationshipSubjects(String obj) {
	    Vector ret = new Vector();
	    
	    return ret;
	}
	
	private Vector getRelationshipObjects(String sub) {
	    Vector ret = new Vector();
	    
	    return ret;
	}
	
	private String[] createRSRow(String filename) {
	    int cols = 4 + returnFields.size();
	    String[] rss = new String[cols];
	    rss[0] = filename;                         //ID
	    rss[1] = getLastPathElement(filename);     //docname
	    rss[2] = currentDoctype;                   // doctype
	    
	    rss[3] =  getValueForPath("//*/title",filename);  // title
	    if (rss[3].equals("")) { rss[3] = filename; }
	    for (int i=0;i<returnFields.size();i++) {
	        rss[4+i] = getValueForPath((String)returnFields.elementAt(i),filename);   
	    }
	return rss;
	}
	
	private String getValueForPath(String pathstring, String filename) {
	    String val = "";
	    try{
	    // assume that the filename file has already been parsed
        if (dom_collection.containsKey(filename)){
            Node doc = ((Document)dom_collection.get(filename)).getDocumentElement();
            NodeList nl = null;
            nl = XPathAPI.selectNodeList(doc, pathstring);
            if ((nl!=null)&&(nl.getLength()>0)) {
                Node cn = nl.item(0).getFirstChild();  // assume 1st child is text node
                    if ((cn!=null)&&(cn.getNodeType()==Node.TEXT_NODE)) {
                        val = cn.getNodeValue().trim();
                    }
                else { val="";}
            }
        }
        }
        catch (Exception e){System.out.println("Error in getValueForPath method");}
	return val;    
	}
	
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
	
	
   // given a directory, return a vector of files it contains
   // including subdirectories
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
 
   private void addToRS(String filename) {
        Vector rsdocs = getResultSetDocs(filename);
        for (Enumeration e=rsdocs.elements();e.hasMoreElements();) {
            String name = (String)e.nextElement();
            String[] rowdata = createRSRow(name);
//            dtm.addRow(rowdata);   
        }
   }
   
// use to get the last element in a path string
   private String getLastPathElement(String str) {
        String last = "";
        int ind = str.lastIndexOf("\\");
        if (ind==-1) {
           last = str;     
        }
        else {
           last = str.substring(ind+1);     
        }
        return last;
   }
 
 //------------------------------------------------------------------------
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
 
  
// -------------------------------------------------------------------------
// given a QueryTerm, construct a XPath expression
//
String QueryTermToXPath(QueryTerm qt) {
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
	}
	// is path absolute or relative
	else if (pathExpression.startsWith("/")) {      // absolute
    xpath =	 pathExpression;
	}
	else {
    xpath = "//*"+pathExpression;
	}
  if ((value.equals("%"))||(value.equals("*"))) {
		// wild card text search case
    xpath = xpath+"[text()]";
		return xpath;
	}
	else {
	  if (!caseSensitive) { // use translate function to convert text() to lowercase
        	// check on searchMode
		if (searchMode.equals("starts-with")) {
      xpath = xpath+"[starts-with(translate(text(),"
          +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\"),"
          +value+")]";		
		}
		else if (searchMode.equals("ends-with")) {
      xpath = xpath+"[contains(translate(text(),"
          +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\"),"
          +value+")]";
            // not correct - fix later
		}
		else if (searchMode.equals("contains")) {
      xpath = xpath+"[contains(translate(text(),"
          +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\"),"
          +value+")]";
		}
		else if (searchMode.equals("matches-exactly")) {
      xpath = xpath+"[translate(text(),"
        +"\"ABCDEFGHIJKLMNOPQRSTUVWXYZ\",\"abcdefghijklmnopqrstuvwxyz\")="
        +value+"]";
		} 
	    }
  else {
		if (searchMode.equals("starts-with")) {
      xpath = xpath+"[starts-with(text(),"+value+")]";		
		}
		else if (searchMode.equals("ends-with")) {
      xpath = xpath+"[contains(text(),"+value+")]";
                        // not correct - fix later
		}
		else if (searchMode.equals("contains")) {
      xpath = xpath+"[contains(text(),"+value+")]";
		}
		else if (searchMode.equals("matches-exactly")) {
      xpath = xpath+"[text()="+value+"]";
		} 
  }

	}
return xpath;
}               



// executes a single xpath query and returns a set of result objects
// in a Vector; a null return indicates that no datacollections have been found
Vector executeXPathQuery(String xpath) {
  Vector ret = new Vector();
  ret.addElement(xpath);
  System.out.println(xpath);
  return ret;
}

// local Query execution 
// recursive handling of QueryTerms 
// Vector res is the result set when started
// it is used here to pass current results for recursion
Vector executeLocal(QueryGroup qg, Vector res) { 
	Vector rs = res;
	Vector currentResults = null;

	Enumeration children = qg.getChildren();
	while (children.hasMoreElements()) {
    Object child = children.nextElement();
		if (child instanceof QueryTerm) {
      String xpath = QueryTermToXPath((QueryTerm)child);
      currentResults = executeXPathQuery(xpath);
      if (currentResults==null) System.out.println("current is null!");
			if ((currentResults==null)&&(qg.getOperator().equalsIgnoreCase("intersect"))) {
                         	// exit loop since one the results sets is null
				rs = null;
				break;
			}
			// add these results to previous ones
			if (qg.getOperator().equalsIgnoreCase("intersect")) {
				if (rs==null) rs = (Vector)currentResults.clone(); // 1st time
				Vector temp = (Vector)rs.clone();
				rs.removeAllElements();
        Enumeration w = currentResults.elements();
				while(w.hasMoreElements()) {
					Object obj = w.nextElement();
					if (temp.contains(obj)) {
            rs.addElement(obj);
					}	
				}
			}
			if (qg.getOperator().equalsIgnoreCase("union")) {
				if (rs==null) rs = (Vector)currentResults.clone(); // 1st time
        Enumeration q = currentResults.elements();
				while(q.hasMoreElements()) {
					Object obj = q.nextElement();
					if (!rs.contains(obj)) {
            rs.addElement(obj);
					}	
				}
			}
		}
		else {  // QueryGroup
      executeLocal((QueryGroup)child, rs);
		}
	}
return rs;
}   

  /**
   * Load the configuration parameters that we need
   */
  private void loadConfigurationParameters()
  {
    parserName = config.get("saxparser", 0);
    accNumberSeparator = config.get("accNumberSeparator", 0);
    String searchLocalString = config.get("searchlocal", 0);
//    searchLocal = (new Boolean(searchLocalString)).booleanValue();
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
      System.err.println("Wrong number of arguments!!!");
      System.err.println("USAGE: java LocalQuery <xmlfile>");
      return;
    } else {
      int i = 0;
      boolean useXMLIndex = true;
      String xmlfile  = args[i];

      try {
        ClientFramework cf = new ClientFramework(
                              new ConfigXML("lib/config.xml"));
        
        FileReader xml = new FileReader(new File(xmlfile));
         
        LocalQuery qspec = new LocalQuery(xml, cf);
        
        Vector test = qspec.executeLocal(qspec.query, null);
        Enumeration www = test.elements();
        while (www.hasMoreElements()) {
          System.out.println((String)www.nextElement());  
        }
         
       } catch (IOException e) {
         System.err.println(e.getMessage());
       }
     }
  }
   
   
}
