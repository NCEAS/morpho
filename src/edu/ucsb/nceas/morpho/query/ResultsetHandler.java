package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;
import java.lang.Runnable;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ResultsetHandler implements ContentHandler, Runnable
{
  private Stack elementStack = null;
  private String[] headers;
  private String docid;
  private String docname;
  private String doctype;
  private String createdate;
  private String updatedate;
  private String paramName;
  private Hashtable params;
  private Morpho morpho;
  private Hashtable triple;
  // a collection of triple Hashtables, used during SAX parsing
  private Vector tripleList;
  private Vector returnFields;
  // global for accumulating characters in SAX parser
  private String accumulatedCharacters = null;
  //Flag indicating whether the results are from a local query
  private boolean isLocal = false;
  // Flag indicating whether the results are from a metacat query
  private boolean isMetacat = false;
  //Vetor to store the data
  private SynchronizeVector resultsVector;
  //input stream
  private InputStream resultsXMLStream;
  // source the location of search, local or network
  private String source;
  // indicator if the parsing is done;
  private boolean isdone = false;


  /**
   *  A contruct will be used in another thread
   * @param resultsXMLStream InputStream
   * @param resultsVector SynchronizeVector
   * @param morpho Morpho
   * @param source String  the location of search, local or network
   */
  public ResultsetHandler(InputStream resultsXMLStream,
                          SynchronizeVector resultsVector,
                          Morpho morpho, String source)
  {
     if (resultsXMLStream == null)
     {
       isdone= true;
     }
     this.resultsXMLStream = resultsXMLStream;
     this.resultsVector = resultsVector;
     this.morpho = morpho;
     this.source = source;
     init();
  }

  /**
   * A constructor to set up a ContentHanler
   * @param morpho Morpho  contains configration info
   * @param source String  the location of search, local or network
   */
  public ResultsetHandler(Morpho morpho, String source)
 {
    resultsVector = new SynchronizeVector();
    this.morpho = morpho;
    this.source = source;
    init();
 }

 /*
  * Method to init return fields and result set location
  */
 private void init()
 {
   ConfigXML config = morpho.getConfiguration();
   returnFields = config.get("returnfield");
   if (source.equals("local"))
   {
     isLocal = true;
     isMetacat = false;
   }
   else if (source.equals("metacat"))
   {
     isLocal = false;
     isMetacat = true;
   }

 }


  public void run()
  {
    // Parse the incoming XML stream and extract the data
    XMLReader parser = null;
    // Set up the SAX document handlers for parsing
    if (!isdone)
    {
    try {
     // Get an instance of the parser
     parser = Morpho.createSaxParser((ContentHandler)this, null);
     Log.debug(30, "(2.43) Creating result set ...");
     // Set the ContentHandler to this instance
     parser.parse(new InputSource(new InputStreamReader(resultsXMLStream)));
     Log.debug(30, "(2.44) Creating result set ...");
    } catch (Exception e) {
     isdone = true;
     Log.debug(30, "(2.431) Exception creating result set ...");
     Log.debug(6, "(2.432) " + e.toString());
     Log.debug(30, "(2.433) Exception is: " + e.getClass().getName());
    }
   }//if
  }

  /**
   * Method to get synchorinze vector
   * @return SynchronizeVector
   */
  public SynchronizeVector getSynchronizeVector()
  {
    return resultsVector;
  }

  /**
   * A method to see if the parsing is done
   * @return boolean
   */
  public boolean isDone()
  {
    // parsing is finished and every data was send out
    if (isdone && resultsVector.isEmpty())
    {
      return true;
    }
    else
    {
      return false;
    }

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
       row.addElement(ResultSet.packageDataIcon);
     } else {
       row.addElement(ResultSet.packageIcon);
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
     resultsVector.addVector(row);
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


 }

 private void setRSValues(String currentTag) {
   String inputString = accumulatedCharacters.trim();
   // added by higgins to remove extra white space 7/11/01
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
 public void startDocument() throws SAXException
 {
   elementStack = new Stack();
 }

 /** Unused SAX handler */
 public void endDocument() throws SAXException
 {
   isdone = true;
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


}
