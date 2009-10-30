/**
 *  '$RCSfile: AbstractDataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-05-03 01:49:02 $'
 * '$Revision: 1.143 $'
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


package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;
import edu.ucsb.nceas.morpho.datastore.DataStoreInterface;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DocidIncreaseDialog;
import edu.ucsb.nceas.morpho.util.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;
import edu.ucsb.nceas.morpho.query.LocalQuery;
import edu.ucsb.nceas.morpho.util.IOUtil;
import edu.ucsb.nceas.morpho.util.IncompleteDocSettings;
import edu.ucsb.nceas.morpho.util.LoadDataPath;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.ModifyingPageDataInfo;
import edu.ucsb.nceas.morpho.util.XMLTransformer;
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.utilities.XMLUtilities;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.*;



/**
 * <p>class that represents a data package. This class is abstract. Specific datapackages
 * e.g. eml2, beta6., etc extend this abstact class</p>
 *
 * <p>actually, only the load and serialize methods are abstract.
 * A number of other concrete utlity methods are included in this class.
 * Note that this class extends the MetadataObject class. The essense of
 * this class is thus the DOM metadataNode (which contains references to all the Nodes
 * in the DOM and the metadataPathNode. The metadataPathNode references an XML structure
 * which maps generic DataPackage information to specific paths for the grammar being
 * considered. Thus, handling changes in the grammar of eml2, for example, should just
 * require one to create a new map from generic nodes to the new specific ones.</p>
 *
 *   ----- A description of how this class is to be used-----
 *
 *   <p>New DataPackage class for Morpho for handling EML2 and other metadata content standards</p>
 *
 * <p>It is desired to create a new class for representing 'generic' dataPackage objects, with
 *  EML2 being the immediate goal. It is desired, however, to avoid being too specific so
 *  that changes in standards can easily be configured without major code re-writes. This
 *  memo attempts to describe the current design.</p>
 *
 *
 *
 *  <p>A simplified class diagram is shown below for discussion purposes.</p>
 *
 * <pre>
 *  MetadataObject  <----------------  AbstractDataPackage  <---------- EML200DataPackage
 *  <---------- NBIIBioDataPackage       <----DataPackageFactory
 *  <---------- EML2Beta6DataPackage
 * </pre>
 *
 *  <p>The base class is 'MetadataObject'. Basically, this class just a DOM structure containing
 *   the metadata for a defined schema (the 'schemaGrammer'(i.e. doctype) is also a member
 *   variable for the class) . There is also a member called the 'xpathKeyMap'. This is
 *   supposed to be a reference to a set of mappings between generic concepts (e.g. the
 *   package name) and the specific DOM xpath to the node in the specific DOM that contains
 *   the actual concept. This map is stored in a properties file of some type (e.g. an
 *   XML file) that is read at run time. Thus, minor changes in a schema can be handled
 *   by just updating this properties file rather than changing the code.</p>
 *
 *  <p>Now, the 'AbstractDataPackage' class extends the very general purpose 'MetadataObject'
 *  class and is meant to be used specifically for representing dataPackages of different
 *  types. The class is call 'Abstract...' because there are certain actions (like
 *  'load' and 'serialize' that a specific to the schemaGrammar. Thus the
 *  'AbstractDataPackage' class is extended by various schema specific classes such
 *  as the three shown above (i.e. EML200DataPackage, NBIIBioDataPackage, and
 *  EML2Beta6DataPackage). Note that the xpathKeyMap used is different for each of
 *  these specific package classes.</p>
 *
 *  <p>Finally, the DataPackageFactory class is used to create a new datapackage object f
 *  rom a supplied DOM or from a docID of a document on metacat or stored locally.
 *  A factory method is needed so that it can determine just what schema is desired
 *  and which of the specific package classes should be used to create the object.
 *  Once created, however, methods in the AbstractDataPackage that are generic can
 *  be used to get information stored in the package.</p>
 *
 *
 *  <p>xpathKeyMap</p>
 *   <p>Consider now how the xpathKeyMap works. An example in XML format for eml200
 *  is reproduced below. It should be noted that this example is organized as
 *  a set of 'contextNode' elements. The 'package' contextNode corresponds to
 *  the root of datapackage DOM while other contextNodes, like 'entity' refer
 *  to some node in the dom other than the root. The contextNode serves as the
 *  point of departure for XPath searches. The concept allows for relative
 *  searche - e.g. one can give paths relative to the entity context node.</p>
 *
 *   <p>An example of xpathKeyMap use is the problem of finding the "accessionNumber"
 *  for a generic metadata schema. The document below has an 'accessionNumber'
 *  element under the 'package' contextNode. It's value for eml2 is seen to be
 *  '/eml:eml/@packageId'. ONe first looks up this value in the xpathKeyMap and
 *  then applies the xpath to the eml2 dataPackage dom. We have thus added a level
 *  of indirection where specific paths are looked up in the xpathKeyMap using
 *  generic path names.</p>
 *
 *   <p>As another example, one would look at the 'name' element under the 'entity'
 *  contextNode to get an entity name. In this case the relative path is simply
 *  'entityName'. But how does one get the actual entity contextNode where the
 *  relative path starts? In this example, the higher level 'entities' element
 *  under the package contextNode is an xpath that will return a NodeSet of
 *  entity nodes in the eml2 dom. Each of these nodes is a starting point for the
 *  entity information (i.e. the root of the entity subtree).</p>
 * <pre>
 *  <code>
 *  &lt;?xml version="1.0"?&gt;
 *  &lt;xpathKeyMap schemaGrammar="eml2.0.0"&gt;
 *  &lt;!-- element name is key, element value is Xpath for this grammar --&gt;
 *  &lt;contextNode name="package"&gt;
 *    &lt;entities&gt;/eml:eml/dataset/dataTable&lt;/entities&gt;
 *    &lt;title&gt;/eml:eml/dataset/title&lt;/title&gt;
 *    &lt;author&gt;/eml:eml/dataset/creator/individualName/surName&lt;/author&gt;
 *    &lt;accessionNumber&gt;/eml:eml/@packageId&lt;/accessionNumber&gt;
 *    &lt;keywords&gt;/eml:eml/dataset/keywordSet/keyword&lt;/keywords&gt;
 *  &lt;/contextNode&gt;
 *  &lt;!-- Xpaths for entity values are defined as relative to top node of entity --&gt;
 *  &lt;contextNode name="entity"&gt;
 *    &lt;name&gt;entityName&lt;/name&gt;
 *    &lt;numRecords&gt;numberOfRecords&lt;/numRecords&gt;
 *    &lt;entityDescription&gt;entityDescription&lt;/entityDescription &gt;
 *    &lt;physical&gt;physical&lt;/physical&gt;
 *    &lt;attributes&gt;attributeList/attribute&lt;/attributes&gt;
 *  &lt;/contextNode&gt;
 *  &lt;contextNode name="attribute"&gt;
 *    &lt;name&gt;attributeName&lt;/name&gt;
 *    &lt;dataType&gt;storageType&lt;/dataType&gt;
 *    &lt;isText&gt;count(measurementScale/nominal|measurementScale/ordinal)!=0&lt;/isText&gt;
 *    &lt;isDate&gt;count(measurementScale/datetime)!=0&lt;/isDate&gt;
 *  &lt;/contextNode&gt;
 *  &lt;contextNode name="physical"&gt;
 *    &lt;name&gt;objectName&lt;/name&gt;
 *    &lt;fieldDelimiter&gt;dataFormat/textFormat/simpleDelimited/fieldDelimiter&lt;/fieldDelimiter&gt;
 *    &lt;numberHeaderLines&gt;dataFormat/textFormat/numHeaderLines&lt;/numberHeaderLines&gt;
 *    &lt;size&gt;size&lt;/size&gt;
 *    &lt;format&gt;dataFormat/externallyDefinedFormat/formatName&lt;/format&gt;
 *    &lt;isText&gt;count(dataFormat/textFormat)!=0&lt;/isText&gt;
 *    &lt;distribution&gt;distribution&lt;/distribution&gt;
 *  &lt;/contextNode&gt;
 *  &lt;contextNode name="distribution"&gt;
 *    &lt;isOnline&gt;count(online/url)!=0&lt;/isOnline&gt;
 *    &lt;url&gt;online/url&lt;/url&gt;
 *    &lt;isInline&gt;count(inline)!=0&lt;/isInline&gt;
 *    &lt;inline&gt;inline&lt;/inline&gt;
 *  &lt;/contextNode&gt;
 *  &lt;/xpathKeyMap&gt;
 *  </code></pre>
 */
public abstract class AbstractDataPackage extends MetadataObject
                                          implements XMLFactoryInterface {
  protected String location = "";
  protected String id;
  protected ConfigXML config;
  protected File dataPkgFile;
  protected FileSystemDataStore fileSysDataStore;
  protected MetacatDataStore metacatDataStore;

	protected static Map  customUnitDictionaryUnitsCacheMap = new HashMap();
	private static String[] customUnitDictionaryUnitTypesArray = new String[0];

  // added by D Higgins on 29 Aug 2005
  // these variables store the most recent AttributeArray so it does not
  // need to be retreived again. This was added for use in tables with very
  // large numbers of columns. It greatly speeds up retreiving colmn header names
  // and other table header info. - Dan Higgins
  private Node[] lastAttributeArray = null; //DFH
  private int lastEntityIndex = -1;
  //boolean serializeDataAtBothLocation = false;
  private Hashtable original_new_id_map = new Hashtable(); // store the map between new data id and old data id
  //store the index of entity which data file has unsaved change (dirty)
  private Vector dirtyEntityIndexList = new Vector(); 

  /*
   *  If the AbstractDataPackage is created by opening an existing document,
   *  the id of the document being opened is stored here. Note that the id is also
   *  assumed stored inside the xml document, but there are cases where the two
   *  ids do not agree! (even though they should). This value remains null if there
   *  is no intial ID
   */
  protected String initialId = null;

  protected Entity[] entityArray;

  private final String HTMLEXTENSION = ".html";
  private final String METADATAHTML = "metadata";
  private final String EXPORTSYLE = "export";
  private final int ORIGINAL_REVISION = 1;
  private boolean serializeLocalSuccess = false;
  private boolean serializeMetacatSuccess = false;
  //private boolean identifierChangedInMetacatSerialization = false;
  //private boolean identifierChangedInLocalSerialization = false;
  private boolean dataIDChanged = false;
  private boolean packageIDChanged = false;
  protected static final String INSERTMETACAT = "insert";
  protected static final String UPDATEMETACAT = "update";
  protected static final String DONOTHMETACAT = "donothing";
  private static final String REPLACE = "replace";
  private static final String DELETE   = "delete";
  protected String autoSavedID = null; //file id which stores auto saving doc
  protected String completionStatus = null; 
  public  static final String COMPLETED = "completed";
  public  static final String INCOMPLETE_NEWPACKAGEWIZARD = "incomplete-new-pakcage-wizard";
  public  static final String INCOMPLETE_TEXTIMPORTWIZARD  = "incomplete-import-text-wizard";

  /**
   * This abstract method turns the datapackage into a form (e.g. string) that
   * can be saved in the file system or metacat. Actual implementation is done
   * in classes specific to grammar
   *
   * @param location String
   *
   * @return   true if there is no indicated problem; false, otherwise
   */
  abstract public void serialize(String location) throws MetacatUploadException;


  /**
   * This abstract method loads a datapackage from metacat or the local file
   * system based on an identifier. Basic action is to create a DOM and assign
   * it to the underlying MetadataObject. Actual implementation is done in
   * classes specific to grammar
   *
   * @param location String
   * @param identifier String
   * @param morpho Morpho
   */
  abstract public void load(String location, String identifier, Morpho morpho);

  /**
    * This abstract method loads a datapackage from an XML character stream.
    * Basic action is to create a DOM and assign
    * it to the underlying MetadataObject. Actual implementation is done in
    * classes specific to grammar
    *
    * @param in InputSource
    */
   abstract public void load(InputSource in);


  /**
   * Copies the AbstractDataPackage with the indicated id from the local file
   * store to Metacat
   *
   * @param id String
   * @throws MetacatUploadException
   * @return AbstractDataPackage
   */
  abstract public AbstractDataPackage upload(String id, boolean updatePackageId) throws
      MetacatUploadException;


  /**
   * Copies the AbstractDataPackage with the indicated id from metacat to the
   * local file store
   *
   * @param id String
   * @return AbstractDataPackage
   */
  abstract public AbstractDataPackage download(String id);


  /**
   *  This method follows the pointer stored in 'references' node to return the
   *  DOM node referred to by 'references'
   *  This is really specific to eml2; thus just declared as abstract here
   *  and implemented in the EML200DataPackage class.
   */
  public abstract Node getReferencedNode(Node node);

  /**
   * used to signify that this package is located on a metacat server
   */
  public static final String METACAT = "metacat";

  /**
   * used to signify that this package is located locally
   */
  public static final String LOCAL = "local";

  /**
   * used to signify that this package is stored on metacat and locally.
   */
  public static final String BOTH = "localmetacat";

  // util to read the file from either FileSystemDataStore or MetacatDataStore
  protected File getFileWithID(String ID, Morpho morpho) throws Throwable {

    File returnFile = null;
    if (location.equals(METACAT)) {
      try {
        Log.debug(11, "opening metacat file");
        if (metacatDataStore == null) {
          metacatDataStore = new MetacatDataStore(morpho);
        }
        dataPkgFile = metacatDataStore.openFile(ID);
        Log.debug(11, "metacat file opened");

      }
      catch (FileNotFoundException fnfe) {

        Log.debug(0, "Error in DataPackage.getFileFromDataStore(): "
                  + "metacat file not found: " + fnfe.getMessage());
        fnfe.printStackTrace();
        throw fnfe.fillInStackTrace();

      }
      catch (CacheAccessException cae) {

        Log.debug(0, "Error in DataPackage.getFileFromDataStore(): "
                  + "metacat cache problem: " + cae.getMessage());
        cae.printStackTrace();
        throw cae.fillInStackTrace();
      }
    }
    else { //not metacat
      try {
        Log.debug(11, "opening local file");
        if (fileSysDataStore == null) {
          fileSysDataStore = new FileSystemDataStore(morpho);
        }
        dataPkgFile = fileSysDataStore.openFile(ID);
        Log.debug(11, "local file opened");

      }
      catch (FileNotFoundException fnfe) {

        Log.debug(0, "Error in DataPackage.getFileFromDataStore(): "
                  + "local file not found: " + fnfe.getMessage());
        fnfe.printStackTrace();
        throw fnfe.fillInStackTrace();
      }
    }
    return dataPkgFile;
  }

  /**
   *  sets the initialId variable
   */
  public void setInitialId(String initId) {
    this.initialId = initId;
  }
  
  /**
   *  Gets the scope of id. For example, it will return jones if the docid is jones.1.1.
   *  It will return the substring from 0 to the second last .
   *  null will be returned if no scope found.
   * @return the scope of this id
   */
  public String getIdScope()
  {
	  return getIdScope(initialId);
  }
  
  /*
   *  Gets the scope of given id. For example, it will return jones if the docid is jones.1.1.
   *  It will return the substring from 0 to the second last .
   *  null will be returned if no scope found.
   * @return the scope of this id
   */
  private String getIdScope(String id)
  {
	  String scope = null;
	  if (id != null)
	  {
		  int index =id.lastIndexOf(".");
		  if (index != -1)
		  {
		      scope = id.substring(0, index);
		      if (scope != null)
		      {
		    	  index = scope.lastIndexOf(".");
		    	  if (index != -1)
		    	  {
		    		  scope = scope.substring(0,index);
		    	  }
		    	  else
		    	  {
		    		  scope = null;
		    	  }
		      }
		  }
	  }
	  return scope;
  }
  

  /**
   *  gets the initialId variable
   */
  public String getInitialId() {
    return initialId;
  }


  /**
   * Method to return the location
   *
   * @return String
   */
  public String getLocation() {
    return location;
  }


  /**
   * Method to set the location
   *
   * @param location String
   */
  public void setLocation(String location) {
    this.location = location;
  }


  /**
   * convenience method to get the DataPackage title
   *
   * @return String
   */
  public String getTitle() {
    String temp = getGenericValue(
        "/xpathKeyMap/contextNode[@name='package']/title");
    return temp;
  }


  /**
   * convenience method to get the DataPackage author May be overridden for
   * specific package types to give better response (e.g. in eml2, folds
   * together several elements and authors)
   *
   * @return String
   */
  public String getAuthor() {
    String temp = "";
    temp = getGenericValue("/xpathKeyMap/contextNode[@name='package']/author");
    return temp;
  }


  /**
   * convenience method to retrieve accession number from DOM
   *
   * @return String
   */
  public String getAccessionNumber() {
    String initId = getInitialId();
    String temp = getGenericValue(
        "/xpathKeyMap/contextNode[@name='package']/accessionNumber");
    if (initId != null) {
      if (!initId.equals(temp)) {
        Log.debug(10,"Internal Id DOES NOT match Storage Id!!!");
      }
      temp = initId;
    }

    return temp;
  }


  /**
   * convenience method to retrieve packageID from DOM synonym for
   * getAccessionNumber
   *
   * @return String
   */
  public String getPackageId() {
    String temp = "";
    temp = getAccessionNumber();
    return temp;
  }


  /**
   *
   */
  public String getPackageIdFromDOM() {
      String emlXpath = "/eml:eml/";
      NodeList nodes = null;
      try {
          nodes = XMLUtilities.getNodeListWithXPath(getMetadataNode(), emlXpath);
      } catch (Exception w) {
        //  Log.debug(30, "Problem with getting NodeList for " + emlXpath);
      }

      if (nodes == null) {
          return null; // no eml:eml
      }

      if (nodes.getLength() > 0) {
          NamedNodeMap atts = nodes.item(0).getAttributes();
          String packageId = atts.getNamedItem("packageId").getNodeValue();
          return packageId;
      }
      return "";
  }

  /**
   * convenience method to set accession number from DOM
   *
   * @param id String
   */
  public void setAccessionNumber(String id) {
    setGenericValue("/xpathKeyMap/contextNode[@name='package']/accessionNumber", id);
    setInitialId(null);
  }


  /**
   * convenience method for getting package keywords
   *
   * @return String
   */
  public String getKeywords() {
    String temp = "";
    NodeList keywordsNodes = null;
    String keywordsXpath = "";
    try {
      keywordsXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/keywords")).getNodeValue();
      keywordsNodes = XMLUtilities.getNodeListWithXPath(metadataNode,
          keywordsXpath);
      if (keywordsNodes == null) {
        return "";
      }
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting keyword");
    }
    int numKeywords = keywordsNodes.getLength();
    String kw = "";
    for (int i = 1; i < numKeywords + 1; i++) {
      kw = getXPathValue("(" + keywordsXpath + ")[" + i + "]");
      if (temp.length() > 0) {
        temp = temp + ", ";
      }
      temp = temp + kw;
    }
    return temp;
  }

  /**
   * convenience method to check if requested subtree exists (as identified
   * by genericName String and int index).
   *
   * @param genericName String
   * @param index int
   * @return returns true if requested subtree exists (as identified
   * by genericName String and int index). Returns false otherwise
   */
  public boolean subtreeExists(String genericName, int index) {

    return (null != this.getSubtree(genericName, index));
  }


  /**
   * returns cloned root Node of subtree identified by genericName String and int
   * index; returns null if not found
   * NOTE: the cloned subtree is a new node. That new node is copied to a new
   * Document object and made the root of the new document
   *
   * @param genericName String
   * @param index int
   * @return  cloned root Node of subtree, or null if not found
   */
  public Node getSubtree(String genericName, int index) {
    NodeList nodelist = null;
    String genericNamePath = "";
    try{
      genericNamePath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/"+ genericName)).getNodeValue();
      Log.debug(40, "=================the genericNamePath in getSubtree() is "+genericNamePath);
      nodelist = XMLUtilities.getNodeListWithXPath(metadataNode,
          genericNamePath);

      if ((nodelist == null)||(nodelist.getLength()==0)) {

        Log.debug(50, "AbstractDataPackage.getSubtree() - no nodes found of "
                  +"type \n /xpathKeyMap/contextNode[@name='package']/"
                  + genericName +"\n returning NULL");
        return null;
      }
      else {
        if (index<nodelist.getLength()) {
          // create a deep cloned version
          Node deepClone = (nodelist.item(index)).cloneNode(true);
          DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
          Document doc = impl.createDocument("", "tempRoot", null);
          Node importedClone = doc.importNode(deepClone, true);
          Node tempRoot = doc.getDocumentElement();
          doc.replaceChild(importedClone, tempRoot);
          return importedClone;
        } else {

          Log.debug(50, "AbstractDataPackage.getSubtree() - index was "
                    +"greater than number of available nodes; returning NULL");
          return null;
        }
      }
    } catch (Exception e) {
      Log.debug(30, "Exception in getSubtree!"+e.getMessage());
    }
    return null;
  }




  /**
   * returns a List of cloned root Nodes of subtrees identified by genericName
   * String; returns null if not found
   * NOTE: the cloned subtrees are new nodes. Each new node is copied to a new
   * Document object and made the root of the new document
   *
   * @param genericName String
   * @return List containing the cloned root Nodes of subtrees, or an empty list
   * if none found. Never returns null
   */
  public List getSubtrees(String genericName) {

    List returnList = new ArrayList();

    NodeList nodelist = null;
    String genericNamePath = "";
    try {
      genericNamePath = (XMLUtilities.getTextNodeWithXPath(
        getMetadataPath(), "/xpathKeyMap/contextNode[@name='package']/"
        + genericName)).getNodeValue();
      nodelist = XMLUtilities.getNodeListWithXPath(metadataNode,
                                                   genericNamePath);

    } catch (Exception e) {
      Log.debug(50, "Exception in getSubtree!");
    }
    if ((nodelist == null) || (nodelist.getLength() == 0)) {

      Log.debug(50, "AbstractDataPackage.getSubtrees() - no nodes found of "
                + "type \n /xpathKeyMap/contextNode[@name='package']/"
                + genericName + "\n returning empty List");

    } else {

    	returnList = getSubtree(nodelist);
    }
    return returnList;
  }
  
  /**
   * returns a List of cloned root Nodes of subtrees for a given list
   *  returns empty list if the given node list is empty or null
   * NOTE: the cloned subtrees are new nodes. Each new node is copied to a new
   * Document object and made the root of the new document
   *
   * @param nodelist the given subtree node list
   * @return List containing the cloned root Nodes of subtrees, or an empty list
   * if none found. Never returns null
   */
  public List getSubtree(NodeList nodelist)
  {
	  List returnList = new ArrayList();
	  if ((nodelist == null) || (nodelist.getLength() == 0)) {

	      Log.debug(50, "AbstractDataPackage.getSubtrees() - the pass node list is empty"+
	    		               " and returning empty List");

	    } else {

	      for (int index = 0; index < nodelist.getLength(); index++) {
	        // create deep cloned versions
	        Node deepClone = (nodelist.item(index)).cloneNode(true);
	        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
	        Document doc = impl.createDocument("", "tempRoot", null);
	        Node importedClone = doc.importNode(deepClone, true);
	        Node tempRoot = doc.getDocumentElement();
	        doc.replaceChild(importedClone, tempRoot);
	        returnList.add(importedClone);
	      }
	    }
	  return returnList;
  }



  /**
   * returns cloned root Node of subtree identified by the passed unique String
   * refID; returns null if not found
   *
   * @param refID unique String refID
   * @return  cloned root Node of subtree, or null if refID not found
   */
  abstract public Node getSubtreeAtReference(String refID);

  /**
   * returns pointer to root Node of subtree identified by the passed unique
   * String refID; returns null if not found
   *
   * @param refID unique String refID
   * @return  pointer to root Node of subtree, or null if refID not found
   */
  abstract public Node getSubtreeAtReferenceNoClone(String refID);

  /**
   * replaces subtree identified by the passed unique String refID; returns null
   * if not found. Note that the new subtree will be given the same refID as the
   * subtree it replaces, even if the newSubtreeRoot node has a different id set
   *
   * @param refID unique String refID. Note that the new subtree will be given
   *   the same refID as the subtree it replaces, even if the newSubtreeRoot
   *   node has a different id set
   * @param newSubtreeRoot Node
   * @return root Node of new subtree, or null if refID not found
   */
  abstract public Node replaceSubtreeAtReference(String refID,
                                                 Node newSubtreeRoot);

  /**
   * returns a List of pointers to subtrees that reference (the subtree
   * identified by) the passed refID.
   * More formally, returns a List of pointers to subtree root Nodes, where each
   * subtree root Node contains a "references" child-node, and the content
   * String of the references child-node matches the unique String refID passed
   * to this method; returns an empty List if none found. Should never return
   * null;
   *
   * @param refID unique String refID
   * @return List of pointers to subtrees that reference the subtree identified
   * by the passed refID. Returns an empty List if none found. Should never
   * return null;
   */
  abstract public List getSubtreesThatReference(String refID);


  /**
   * <em>DELETES ALL</em> subtrees identified by genericName String; returns
   * null if none found - <em>USE WITH CARE!!</em>
   *
   * @param genericName String
   * @return List of deleted subtrees; empty List if not found. SHOULD NEVER
   * RETURN null
   */
  public List deleteAllSubtrees(String genericName) {

    NodeList nodelist = null;
    List returnList = new ArrayList();
    String genericNamePath = "";
    try {
      genericNamePath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
                                   "/xpathKeyMap/contextNode[@name='package']/"
                                   + genericName)).getNodeValue();

      nodelist = XMLUtilities.getNodeListWithXPath(metadataNode,
                                                   genericNamePath);

      if (nodelist == null) return returnList;

      int startIdx = nodelist.getLength() - 1;

      for (int index = startIdx; index > -1; index--) {

        Node node = nodelist.item(index);
        Node parnode = node.getParentNode();
        if (parnode == null) return returnList;
        returnList.add(parnode.removeChild(node));
      }

    } catch (Exception e) {
      Log.debug(15, "Exception in deleteAllSubtrees!" + e);
      e.printStackTrace();
      returnList.clear();
      return returnList;
    }
    return returnList;
  }




  /**
   * returns root Node of subtree identified by genericName String and int
   * index; returns null if not found
   *
   * @param genericName String
   * @param index int
   * @return  uncloned root Node of subtree, or null if not found
   */
  public Node getSubtreeNoClone(String genericName, int index) {
    NodeList nodelist = null;
    String genericNamePath = "";
    try{
      genericNamePath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/"+ genericName)).getNodeValue();
      nodelist = XMLUtilities.getNodeListWithXPath(metadataNode,
          genericNamePath);
      if ((nodelist == null)||(nodelist.getLength()==0)) {
        return null;
      }
      else {
        if (index<nodelist.getLength()) {
          // create a deep cloned version
          Node unClone = nodelist.item(index);
          return unClone;
        } else {
          return null;
        }
      }
    } catch (Exception e) {
      Log.debug(50, "Exception in getSubtreeNoClone!");
    }
    return null;
  }


  /**
   * returns a <code>java.util.List</code> containing IDs of all the rootnodes
   * of all the subtrees identified by the passed unique String.  Returns an
   * empty string if none found. <em>NOTE - should never return null</em>
   *
   * @param genericName string identifying nodes - e.g. "parties"
   * @return  a <code>java.util.List</code> containing IDs of all the rootnodes
   * of all the subtrees identified by the passed unique String.  Returns an
   * empty List if none found. <em>NOTE - should never return null</em>
   */
  public List getIDsForNodesWithName(String genericName) {

    List returnList = new ArrayList();

    String IDXpath = "";
    NodeList IDNodes;
    try {
      Node textNode = XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/"+genericName);
      if(textNode == null) {
        Log.debug(40, "Unable to find text node with the given name!");
        return returnList;
      }
      IDXpath = textNode.getNodeValue();
      // example: genericName = 'parties'
      IDNodes = XMLUtilities.getNodeListWithXPath(metadataNode, IDXpath);
      if (IDNodes == null) {
        Log.debug(40, "IDs returnList is null!");
        return returnList;
      }
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting IDsForNodes");
      w.printStackTrace();
      return returnList;
    }
    // add an ID string to the returnList for each Node in the NodeList
    for (int i=0;i<IDNodes.getLength();i++) {
      Element node = (Element)IDNodes.item(i);
      String IDValue = node.getAttribute("id");
      //Log.debug(1,"tagName: "+node.getTagName()+"---IDValue: "+IDValue);
      if (!IDValue.equals("")) {
        returnList.add(IDValue);
      }
    }
    return returnList;
  }

  /**
   * returns a new refID that is guaranteed to be unique within this datapackage
   *
   * @return String a new refID that is guaranteed to be unique within this
   * datapackage
   */
  public synchronized String getNewUniqueReferenceID() {

    String newID = null;

    do {

      newID = String.valueOf(System.currentTimeMillis());

    } while (getSubtreeAtReference(newID)!=null);

    return newID;
  }


  /**
   * inserts subtree rooted at Node, at location identified by genericName
   * String and int index. Returns root Node of inserted subtree, or null if
   * target location not found, so caller can determine whether insertion was
   * successful
   *
   * @param genericName String
   * @param Node subtree root Node
   * @param index int (zero relative). NOTE that subtree will be inserted before
   * this index if a subtree already exists there
   * @return root Node of inserted subtree, or null if target location not
   * found, so caller can determine whether insertion was successful
   */
  public Node insertSubtree(String genericName, Node subtreeRootNode, int index) {

    Document thisDom = getMetadataNode().getOwnerDocument();
    Node newSubtree = thisDom.importNode(subtreeRootNode, true); // 'true' imports children
    Node subTreeNode = getSubtreeNoClone(genericName, 0);
    if (subTreeNode == null) {  // no current subtree node
      try{
        NodeList insertionList = XMLUtilities.getNodeListWithXPath(getMetadataPath(),
            "/xpathKeyMap/insertionList[@name='"+genericName+"']/prevNode");
        if (insertionList==null) {
          // check if there is a node next to the generic node
          insertionList = XMLUtilities.getNodeListWithXPath(getMetadataPath(),
            "/xpathKeyMap/insertionList[@name='"+genericName+"']/nextNode");
          if (insertionList==null) {
            Log.debug(15, "\n** Error in AbstractDataPackage insertSubtree():\n"
                + "XMLUtilities.getNodeListWithXPath() returned NULL "
                + "for xpath: /xpathKeyMap/insertionList[@name='"
                + genericName + "']/prevNode and "
                + "/xpathKeyMap/insertionList[@name='" + genericName
                + "']/nextNode");
            return null;
          } else {
            for (int i=0;i<insertionList.getLength();i++) {
              Node nd = insertionList.item(i);
              String path = (nd.getFirstChild()).getNodeValue();
              NodeList temp = XMLUtilities.getNodeListWithXPath(metadataNode, path);
              if ((temp!=null)&&(temp.getLength()>0)) {
                Log.debug(40, "found: "+path);
                Node nextNode = temp.item(0);
                Node par = nextNode.getParentNode();
                par.insertBefore(newSubtree, nextNode);
                return newSubtree;
              }
            } //
          }
        }
        for (int i=0;i<insertionList.getLength();i++) {
          Node nd = insertionList.item(i);
          String path = (nd.getFirstChild()).getNodeValue();
          NodeList temp = XMLUtilities.getNodeListWithXPath(metadataNode, path);
          if ((temp!=null)&&(temp.getLength()>0)) {
            Log.debug(40, "found: "+path);
            Node prevNode = temp.item(temp.getLength()-1);
            Document doc = prevNode.getOwnerDocument();
            Node nextNode = prevNode.getNextSibling();
            Node par = prevNode.getParentNode();
            if (nextNode==null) { // no next sibling
              par.appendChild(newSubtree);
            } else {
              par.insertBefore(newSubtree, nextNode);
            }
            return newSubtree;
          }
        } //
      }
      catch (Exception w) {
        Log.debug(15, "Error in 'insertSubtree method in AbstractDataPackage");
        w.printStackTrace();
        return null;
      }
    } else {  // node exists
      Node subTreeNodeParent = subTreeNode.getParentNode();
      if (subTreeNodeParent==null) return null;
      // find out how many nodes already exist
      Node stn = subTreeNode;
      int count = 0;
      while(stn !=  null) {
        count++;
        stn = getSubtreeNoClone(genericName, count);
      }
      count--;
      if (count<index) {
        stn = getSubtreeNoClone(genericName, count);
        Node nnode = stn.getNextSibling();
        if (nnode!=null) {
          subTreeNodeParent.insertBefore(newSubtree,nnode);
        } else {
          subTreeNodeParent.appendChild(newSubtree);
        }
      } else {
        stn = getSubtreeNoClone(genericName, index);
          subTreeNodeParent.insertBefore(newSubtree,stn);
      }
      return newSubtree;
    }
    return null;
  }



  /**
   * deletes subtree at location identified by genericName and index; returns
   * root Node of deleted subtree, or null if not found, so caller can determine
   * whether call was successful
   *
   * @param genericName String
   * @param index int (zero relative)
   * @return root Node of deleted subtree, or null if subtree not found, so
   * caller can determine whether insertion was successful
   */
  public Node deleteSubtree(String genericName, int index) {

    NodeList nodelist = null;
    String genericNamePath = "";
    try{
      genericNamePath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/"+ genericName)).getNodeValue();
      Log.debug(40, "=================the genericNamePath in deleteSubtree() is "+genericNamePath);
      nodelist = XMLUtilities.getNodeListWithXPath(metadataNode,
          genericNamePath);
      if ((nodelist == null)||(nodelist.getLength()==0)) {
        return null;
      }
      else {
        if (index<nodelist.getLength()) {

          Node node = nodelist.item(index);
          Node parnode = node.getParentNode();
          if (parnode==null) return null;
          return parnode.removeChild(node);
        } else {
          return null;
        }
      }
    } catch (Exception e) {
      Log.debug(15, "Exception in deleteSubtree! "+e);
      e.printStackTrace();
      return null;
    }
  }
  
  public void deletePacakgeWizardIncompleteDocInfo()
  {
	    NodeList nodelist = null;
	   
	    String genericNamePath = IncompleteDocSettings.SLASH+IncompleteDocSettings.EML+
	    IncompleteDocSettings.SLASH+IncompleteDocSettings.ADDITIONALMETADATA+IncompleteDocSettings.SLASH+
	    IncompleteDocSettings.METADATA+IncompleteDocSettings.SLASH+IncompleteDocSettings.INCOMPLETE+
	    IncompleteDocSettings.PACKAGEWIZARD;
	    try 
	    {
	      
	      nodelist = XMLUtilities.getNodeListWithXPath(metadataNode,
	                                                   genericNamePath);

	      if (nodelist == null) return;

	      int startIdx = nodelist.getLength() - 1;

	      for (int index = startIdx; index > -1; index--) {

	        Node node = nodelist.item(index);
	        Node parnode = node.getParentNode();
	      
	      }

	    }
	    catch (Exception e) 
	    {
	      Log.debug(15, "Exception in deletePacakgeWizardIncompleteDocInfo!" + e);
	      e.printStackTrace();
	      
	    }
	   
  }
  
  /**
   * inserts subtree rooted at Node, at location identified by MofiyingPageDataInfo
   * object. Returns root Node of inserted subtree, or null if
   * target location not found, so caller can determine whether insertion was
   * successful.
   * Note: this is only be used in Correction wizard and we have any assumption
   * that the node doesn't existed (failed to replace, then insert). We should user it
   * very carefully.
   *
   * @param ModifyingPageDataInfo info has the info where to insert.
   * @param Node subtree root Node
   * @return root Node of inserted subtree, or null if target location not
   * found, so caller can determine whether insertion was successful
   */
  public Node insertSubtree(ModifyingPageDataInfo info, Node subtreeRootNode) 
  {
   
	  if(info == null || subtreeRootNode == null)
	  {
		  Log.debug(30, "the ModifyingPageDataInfo object or the inserting node is null");
		  return null;
	  }
     
      try
      {
    	boolean previous = true;
    	Vector nodeList = info.getPrevNodeList();
    	//determine to use which list, previousList or nextList
    	if (nodeList == null && nodeList.isEmpty())
    	{
    		//previous list is empty, try nextList
    		nodeList = info.getNextNodeList();
    		if(nodeList != null && !nodeList.isEmpty())
    		{
    			//set previous to false (using nextList)
    			previous = false;
    		}
    		else
    		{
    			// no previous or next list. return null
    			 Log.debug(15, "\n** Error in AbstractDataPackage insertSubtree():\n"
    		                + "There is no neither prevNodeList or nextNodeList in lib/xpath-wizard-map.xml "
    		                +"for this ModifyingPageDataInfo"+info.getDocumentName());
    		            return null;
    		}
    	}
         
  	  Vector loadPathObjList = info.getLoadExistingDataPath();
	  if (loadPathObjList != null)
	  {
		  Document thisDom = getMetadataNode().getOwnerDocument();
	      Node newSubtree = thisDom.importNode(subtreeRootNode, true); // 'true' imports children
		  Log.debug(46, "In loadPathObjectList is not null branch in handleSubtree method");
		  Node parentNode = metadataNode; // start from document root
		  for(int i=0; i<loadPathObjList.size(); i++)
		  {
			  int lastIndexOfPathList = loadPathObjList.size() -1;
			  LoadDataPath pathObj = (LoadDataPath)loadPathObjList.elementAt(i);
			  if (pathObj != null)
			  {
				  String path = pathObj.getPath();
				  int position = pathObj.getPosition();
				  Log.debug(45, "Handle path "+path+" with position "+position +" in insertSubTree method");
				  NodeList nodelist = XMLUtilities.getNodeListWithXPath(parentNode, path);
				  if(nodelist != null && nodelist.getLength() >0)
				  {
				    	//we still find the the subtree exit, we should reset parent node and contiue go head.
			    		parentNode = nodelist.item(position);			    		
				    	if(i == lastIndexOfPathList)
				    	{
				    		//this is the deep subtree and it still have value. So we can't insert it (we only insert the subtree doesn' exist).
				    		Log.debug(30, "find the deepest subtree for path "+path +" still has value. We don't need to insert the new subtee");
				    		return null;
				    	}
				    	
				    }
				    else
				    {
				    	// at this level, the subtree doesn't exist. This means we can insert the subtree now
				    	for (int k=0;k<nodeList.size();k++) 
				    	{
  				             String  pathFromFile = (String)nodeList.elementAt(k);

				              NodeList temp = XMLUtilities.getNodeListWithXPath(parentNode, pathFromFile);
				              if ((temp!=null)&&(temp.getLength()>0)) {
				                Log.debug(40, "found the exist path: "+pathFromFile);
				                if (!previous)
				                {
				                  Log.debug(40, "In using nextNodeList path");
				                  Node nextNode = temp.item(0);
				                  Node par = nextNode.getParentNode();
				                  par.insertBefore(newSubtree, nextNode);
				                  return newSubtree;
				                }
				                else
				                {
				                	 Log.debug(40, "In using previousNodeList path"); 
				                     Node prevNode = temp.item(temp.getLength()-1);
				                     Document doc = prevNode.getOwnerDocument();
				                     Node nextNode = prevNode.getNextSibling();
				                     Node par = prevNode.getParentNode();
				                     if (nextNode==null) 
				                     { // no next sibling
				                       par.appendChild(newSubtree);
				                     }
				                     else 
				                     {
				                       par.insertBefore(newSubtree, nextNode);
				                     }
				                     return newSubtree;
				                }
				              }
				         } //
				    }
			
				     				  
			  }
		  }
	    }     
      }
      catch (Exception w) 
      {
        Log.debug(15, "Error in 'insertSubtree method in AbstractDataPackage");
        w.printStackTrace();
        return null;
      }
     
      return null;
  }
   
  
  /**
   * Replace a subtree base on the given information. If not success, null will return
   * @param info
   * @param newSubTreeRootNode
   * @return
   */
  public Node replaceSubTree(ModifyingPageDataInfo info, Node newSubTreeRootNode)
  {
	  return handleSubTree(info, newSubTreeRootNode, REPLACE);
  }
  
  /**
   * Delete a subtree base on the given information. If not success, null will return
   * @param info
   * @return
   */
  public Node deleteSubTree(ModifyingPageDataInfo info)
  {
	  return handleSubTree(info, null, DELETE);
  }

  /*
   * replaces or delete subtree at location identified by ModifyingPageDataInfo; returns
   * root Node of replaced subtree, or null if not found, so caller can
   * determine whether replace was successful
   * ModifyingPageDataInfo has a vector of LoadDataPath objects. LoadDataPath has the path and
   * position information. We can use those info to identify the subtree.
   * Note: if we have more than one LoadDataPath, e.g. /eml:eml/dataset/dataTable and position 0
   * and ./attributeList/attribute and position 2. This means dataTable is the first one (postion 0) in the 
   * /eml:eml/dataset/dataTable node list. The position 2 of the second one is the position of ./attributeList/attribute
   * under the dataTable node. So the second one's information is about the children of first one. 
   * @param info ModifyingPageDataInfo
   * @param newSubtreeRootNode the new subtree root Node
   * @param action delete or repalace
   * @return root Node of replaced subtree, or null if subtree not found, so
   * caller can determine whether replace was successful
   */
  private Node handleSubTree(ModifyingPageDataInfo info, Node newSubTreeRootNode, String action) 
  {
	  
      Node node = null;
      if(action == null || (newSubTreeRootNode == null && action.equals(REPLACE)))
      {
    	  return node;
      }
      if(info != null)
      {
    	  Document thisDom = getMetadataNode().getOwnerDocument();
    	  Node newSubTree = null;
    	  if(action.equals(REPLACE))
    	  {
    		  newSubTree = thisDom.importNode(newSubTreeRootNode, true); // 'true' imports children
    	  }
    	  Vector loadPathObjList = info.getLoadExistingDataPath();
		  if (loadPathObjList != null)
		  {
			  Log.debug(46, "In loadPathObjectList is not null branch in handleSubtree method");
			  Node parentNode = metadataNode; // start from document root
			  for(int i=0; i<loadPathObjList.size(); i++)
			  {
				  int lastIndexOfPathList = loadPathObjList.size() -1;
				  LoadDataPath pathObj = (LoadDataPath)loadPathObjList.elementAt(i);
				  if (pathObj != null)
				  {
					  String path = pathObj.getPath();
					  int position = pathObj.getPosition();
					  Log.debug(45, "Handle path "+path+" with position "+position +" in handleSubTree method");
					  try
					  {
					    NodeList nodelist = XMLUtilities.getNodeListWithXPath(parentNode, path);
					    if(nodelist != null && nodelist.getLength() >0)
					    {
					    	if(i != lastIndexOfPathList)
					    	{
					    		//this is not the deepest subtree, we should reset parent node and contiue go head.
					    		parentNode = nodelist.item(position);
					    		
					    	}
					    	else
					    	{
					    		//this is the deep subtree and we should replace it.
					    		Log.debug(30, "find the deepest subtree for path "+path +" with position "+position);
					    		Node targetNode = nodelist.item(position);
					    		Node parnode = null;
					    		if(targetNode != null)
					    		{
					    		  Log.debug(45, "Getting the parent node of node "+targetNode);
					              parnode = targetNode.getParentNode();
					              if (parnode !=null) 
						          {
					            	  if(action.equals(REPLACE))
					            	  {
						            	  Log.debug(35, "Replace an old node by the new node "+newSubTree);
						            	  Node replaceNode =parnode.replaceChild(newSubTree, targetNode);
						            	  if (replaceNode != null)
						            	  {
						            		  Log.debug(35, "Successfully replace the old node "+targetNode+" with the new node  "+newSubTree);
						            		  node = replaceNode;
						            	  }
					            	  }
					            	  else if(action.equals(DELETE))
					            	  {
					            		  Log.debug(35, "Replace an old node by the new node "+newSubTree);
						            	  Node deleteNode =parnode.removeChild(targetNode);
						            	  if (deleteNode != null)
						            	  {
						            		  Log.debug(35, "Successfully delete the old node "+targetNode);
						            		  node = deleteNode;
						            	  }
					            	  }
						          }
						           
					    		}
					    		
					           
					    	}
					    }
					    else
					    {
					    	Log.debug(30, "Node list for path "+path+" is null or empty");
					    	return node;
					    }
					  }
					  catch(Exception e)
					  {
						  Log.debug(15, "couldn't "+action+" the subtree "+e.getMessage());
					  }
					     				  
				  }
			  }
		  }
		
	  }
	  return node;
      
  }


  /**
   * replaces subtree at location identified by genericName and index; returns
   * root Node of replaced subtree, or null if not found, so caller can
   * determine whether replace was successful
   *
   * @param genericName String
   * @param newSubtreeRootNode the new subtree root Node
   * @param index int (zero relative)
   * @return root Node of replaced subtree, or null if subtree not found, so
   * caller can determine whether replace was successful
   */
  public Node replaceSubtree(String genericName, Node newSubtreeRootNode,
                             int index) {

    //delete subtree to be replaced
    Node deleted = this.deleteSubtree(genericName, index);

    Node added = null;

    //if deletion worked, then add new subtree
    if (deleted != null) {
      //if node identified by genericName and index already exists,
      //insertSubtree() adds node before existing node..
      added = this.insertSubtree(genericName, newSubtreeRootNode, index);
    } else {
      Log.debug(15, "** ERROR in replaceSubtree! - delete was unsuccessful. "
                +"Are you sure the node exists? - returning NULL");
      return null;
    }

    // if add wasn't successful, return deleted subtree to its former location
    if (added == null) {

      Log.debug(15, "** ERROR in replaceSubtree! - add was unsuccessful "
                + "- trying to undo delete...");

      added = this.insertSubtree(genericName, deleted, index);

      if (added != null) Log.debug(15, "** ...undo was successful...");
      else               Log.debug(15, "** ...UNDO FAILED - DATAPACKAGE NOT "
                                   +"RETURNED TO ORIGINAL STATE!!!...");
      Log.debug(15, "** ...finally, returning NULL");
      return null;
    }
    return added;
  }



  /**
   * checks to see if the package has a coverage element at the package level
   * returns a Node if it finds one; otherwise, null
   */
  public Node getCoverageNode() {
    NodeList covNodes = null;
    String covXpath = "";
    try {
      covXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/coverage")).getNodeValue();
      covNodes = XMLUtilities.getNodeListWithXPath(metadataNode,
          covXpath);
      if (covNodes == null) {
        return null;
      }
      else {
        return covNodes.item(0);
      }
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting coverageNode");
    }
    return null;
  }

  /**
   * gets a list of geographic nodes
   */
  public NodeList getGeographicNodeList() {
    NodeList geoNodes = null;
    String geoXpath = "";
    try {
      geoXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/geographicCoverage")).getNodeValue();
      geoNodes = XMLUtilities.getNodeListWithXPath(metadataNode,
          geoXpath);
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting geoNodeLIst");
    }
    return geoNodes;
  }

  /**
   *  remove all the geographicNodes
   */
   public void removeGeographicNodes() {
     NodeList gList = getGeographicNodeList();
     if (gList==null) return;
     for (int i=0;i<gList.getLength();i++) {
       Node node = gList.item(i);
       Node par = node.getParentNode();
       par.removeChild(node);
     }
   }


    /**
   * gets a list of temporal nodes
   */
  public NodeList getTemporalNodeList() {
    NodeList tempNodes = null;
    String tempXpath = "";
    try {
      tempXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/temporalCoverage")).getNodeValue();
      tempNodes = XMLUtilities.getNodeListWithXPath(metadataNode,
          tempXpath);
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting tempoNodeLIst");
    }
    return tempNodes;
  }

    /**
   *  remove all the temporalNodes
   */
   public void removeTemporalNodes() {
     NodeList tList = getTemporalNodeList();
     if (tList==null) return;
     for (int i=0;i<tList.getLength();i++) {
       Node node = tList.item(i);
       Node par = node.getParentNode();
       par.removeChild(node);
     }
   }


    /**
   * gets a list of taxonomic nodes
   */
  public NodeList getTaxonomicNodeList() {
    NodeList taxNodes = null;
    String taxXpath = "";
    try {
      taxXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/taxonomicCoverage")).getNodeValue();
      taxNodes = XMLUtilities.getNodeListWithXPath(metadataNode,
          taxXpath);
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting taxoNodeLIst");
    }
    return taxNodes;
  }

    /**
   *  remove all the taxonomicNodes
   */
   public void removeTaxonomicNodes() {
     NodeList tList = getTaxonomicNodeList();
     if (tList==null) return;
     for (int i=0;i<tList.getLength();i++) {
       Node node = tList.item(i);
       Node par = node.getParentNode();
       par.removeChild(node);
     }
   }

  /**
   *  insert a coverage subtree (geographic, temporal, or taxonomic)
   *  under the package level coverage node
   *  'covSubtree' is assumed to be a DOM subtree of the proper type
   *  to be added under a coverage node
   */
  public void insertCoverage(Node covSubtree) {
    Document thisDom = getMetadataNode().getOwnerDocument();
    Node newCovSubtree = thisDom.importNode(covSubtree, true); // 'true' imports children
//    Node newCovSubtree = null;

    Node covNode = getCoverageNode();
    if (covNode == null) {  // no current coverage node
      try{
        NodeList insertionList = XMLUtilities.getNodeListWithXPath(getMetadataPath(),
            "/xpathKeyMap/insertionList[@name='coverage']/prevNode");
          for (int i=0;i<insertionList.getLength();i++) {
          Node nd = insertionList.item(i);
          String path = (nd.getFirstChild()).getNodeValue();
          NodeList temp = XMLUtilities.getNodeListWithXPath(metadataNode, path);
          if ((temp!=null)&&(temp.getLength()>0)) {
            Log.debug(40, "found: "+path);
            Node prevNode = temp.item(0);
            Document doc = prevNode.getOwnerDocument();
            Node newCov = doc.createElement("coverage");
            Node nextNode = prevNode.getNextSibling();
            Node par = prevNode.getParentNode();
            if (nextNode==null) { // no next sibling
              par.appendChild(newCov);
            } else {
              par.insertBefore(newCov, nextNode);
            }
            newCov.appendChild(newCovSubtree);
            return;
          }
        }
      }
      catch (Exception w) {
        Log.debug(1, "Error in 'insertCoverage method in AbstractDataPackage");
      }
    } else {  // node exists
      covNode.appendChild(newCovSubtree);
    }
  }


  /*
   *  This method finds all the entities in the package and builds an array of
   *  'entity' nodes in the package dom. One could create an 'Entity' class descending from
   *  Metadata object, but this offers no obvious advantage over simply saving this node array
   *  as one of the members of AbstractDataPackage
   */
  public Entity[] getEntityArray() {
    if (entityArray != null) {
      return entityArray;
    }
    String entityXpath = "";
    try {
      entityXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/entities")).getNodeValue();

      NodeList entityNodes = XMLUtilities.getNodeListWithXPath(metadataNode,
          entityXpath);
      //  NodeList entityNodes = XPathAPI.selectNodeList(metadataNode,entityXpath);
      if (entityNodes == null) {
        Log.debug(20, "entityList is null!");
        entityArray = null;
      }
      else {
        Node[] entityArrayNodes = XMLUtilities.getNodeListAsNodeArray(
            entityNodes);
        for (int j=0;j<entityArrayNodes.length;j++) {
          entityArrayNodes[j] = getReferencedNode(entityArrayNodes[j]);
        }

        entityArray = new Entity[entityArrayNodes.length];
        for (int i = 0; i < entityArrayNodes.length; i++) {
          entityArray[i] = new Entity(entityArrayNodes[i], this);
        }
      }
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting entityArray");
      w.printStackTrace();
      return null;
    }
    return entityArray;
  }


  /**
   * This method retrieves entityName information, given the index of the entity
   * in the entityNode array
   *
   * @param entNum int
   * @return String
   */
  public String getEntityName(int entNum) {
    String temp = "";
    if(entNum < 0)
        return "No such entity!";
    if ( (entityArray == null) || (entityArray.length < (entNum) + 1)) {
      return "No such entity!";
    }
    Node entity = (entityArray[entNum]).getNode();
    String entityNameXpath = "";
    try {
      entityNameXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='entity']/name")).getNodeValue();
      NodeList enameNodes = XPathAPI.selectNodeList(entity, entityNameXpath);
      if (enameNodes == null) {
        return "enameNodes is null !";
      }
      Node child = enameNodes.item(0).getFirstChild();
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting entity name" + w.toString());
    }
    return temp;
  }


  /**
   * This method retrieves entity index information, given the name of the entity
   * in the entityNode array
   *
   * @param entName String - name of the Entity whose index in the entity array is
   *													required
   * @return int	the index of the entity in the entity array. If that entity is not
   *							found, -1 is returned
   */
  public int getEntityIndex(String entName) {
    String temp = "";
    if ( (entityArray == null)) {
      return -1;
    }

    for(int i = 0; i < entityArray.length; i++)
    {
      Node entity = (entityArray[i]).getNode();
      String entityNameXpath = "";
      try {
        entityNameXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
        "/xpathKeyMap/contextNode[@name='entity']/name")).getNodeValue();
        NodeList enameNodes = XPathAPI.selectNodeList(entity, entityNameXpath);
        if (enameNodes == null) {
          continue;
        }
        Node child = enameNodes.item(0).getFirstChild();
        temp = child.getNodeValue();
      }
      catch (Exception w) {
        Log.debug(50, "exception in getting entity name" + w.toString());
        continue;
      }
      if(temp.equals(entName))
        return i;
    }

    return -1;
  }

  /**
   * This method retrieves the number of records in thr entity, given the index
   * of the entity in the entityNode array
   *
   * @param entNum int
   * @return String
   */
  public String getEntityNumRecords(int entNum) {
    String temp = "";
    if ( (entityArray == null) || (entityArray.length < (entNum) + 1)) {
      return "No such entity!";
    }
    Node entity = (entityArray[entNum]).getNode();
    String entityNumRecordsXpath = "";
    try {
      entityNumRecordsXpath = (XMLUtilities.getTextNodeWithXPath(
          getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='entity']/numRecords")).getNodeValue();
      NodeList eNodes = XPathAPI.selectNodeList(entity, entityNumRecordsXpath);
      if (eNodes == null) {
        return "eNodes is null !";
      }
      Node child = eNodes.item(0).getFirstChild();
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting entity numRecords" + w.toString());
    }
    return temp;
  }

  /**
   * This method gets the count of the entities present in the package
   *
   * @return int the number of entities in the package
   */
  public int getEntityCount() {
    if(entityArray == null) return 0;
    return entityArray.length;
  }

  /**
   * This method gets the count of the attributes present in a particular entity
   *
   * @param  entityIndex the index of the entity whose attribute count is desired
   * @return int the number of attributes in that entity
   */
  public int getAttributeCountForAnEntity(int entityIndex) {

    if(entityIndex < 0)
        return 0;

    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return 0;
    }
    Node[] attributes = getAttributeArray(entityIndex);
    if (attributes == null)
      return 0;
    return attributes.length;
  }

  /**
   * This method sets the number of records in the entity, given the index of
   * the entity in the entityNode array
   *
   * @param entNum int
   * @param numRecS String
   */
  public void setEntityNumRecords(int entNum, String numRecS) {
    if ( (entityArray == null) || (entityArray.length < (entNum) + 1)) {
      Log.debug(20, "No such entity!");
      return;
    }
    Node entity = (entityArray[entNum]).getNode();
    String entityNumRecordsXpath = "";
    try {
      entityNumRecordsXpath = (XMLUtilities.getTextNodeWithXPath(
          getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='entity']/numRecords")).getNodeValue();
      NodeList eNodes = XPathAPI.selectNodeList(entity, entityNumRecordsXpath);
      if (eNodes == null) {
        return;
      }
      Node child = eNodes.item(0).getFirstChild();
      child.setNodeValue(numRecS);
    }
    catch (Exception w) {
      Log.debug(50, "exception in setting entity numRecords" + w.toString());
    }
  }


  /**
   * This method retrieves the entity Description, given the index of the entity
   * in the entityNode array
   *
   * @param entNum int
   * @return String
   */
  public String getEntityDescription(int entNum) {
    String temp = "";
    if(entNum < 0)
        return "No such entity!";

    if ( (entityArray == null) || (entityArray.length < (entNum) + 1)) {
      return "No such entity!";
    }
    Node entity = (entityArray[entNum]).getNode();
    String entityXpath = "";
    try {
      entityXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='entity']/entityDescription")).
          getNodeValue();
      NodeList eNodes = XPathAPI.selectNodeList(entity, entityXpath);
      if (eNodes == null) {
        return "eNodes is null !";
      }
      Node child = eNodes.item(0).getFirstChild();
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting entity description" + w.toString());
    }
    return temp;
  }




  /**
   * This method retrieves the entity ID, given the index of the entity
   * in the entityNode array
   *
   * @param entNum the entity Index of the entity whose ID is required
   * @return String the entity ID
   */
  public String getEntityID(int entNum) {
    String id = "";
    if(entNum < 0)
        return "";
    if ( (entityArray == null) || (entityArray.length < (entNum) + 1)) {
      return "";
    }
    Node entity = (entityArray[entNum]).getNode();
    NamedNodeMap nnm = entity.getAttributes();
    if(nnm !=null) {
      Node idNode = nnm.getNamedItem("id");
      if(idNode != null)
        id = idNode.getNodeValue();
      else {
        Log.debug(45, "No ID Attributes for the given entity " +
                getEntityName(entNum));
      }
    }else {
      Log.debug(45, "No Attributes for the given entity : "+getEntityName(entNum));
    }

    return id;
  }

  /**
   * This method sets the entity ID, given the index of the entity
   * in the entityNode array and the ID. Caller must ensure that the ID is
   * unique. No check for uniqueness is made here.
   *
   * Though the DataPackage Wizard automatically assigns an ID to all entities,
   * it is possible that some old data sets may not have the ID assigned to
   * them. For such cases, this method provides a way to set an ID
   *
   * @param entNum the entity Index of the entity whose ID is required
   * @param ID the unique ID for this entity
   *
   */

  public void setEntityID(int entNum, String ID) {

    if(entNum < 0)
      return;
    if ( (entityArray == null) || (entityArray.length < (entNum) + 1)) {
      return;
    }
    Node entity = (entityArray[entNum]).getNode();
    NamedNodeMap nnm = entity.getAttributes();
    if(nnm !=null) {
      Node idNode = nnm.getNamedItem("id");
      if(idNode != null) {
        idNode.setNodeValue(ID);
        return;
      }
      else {
        Log.debug(45, "No ID Attributes for the given entity " +
                getEntityName(entNum) + " - Adding the ID Attribute");
      }
    }else {
      Log.debug(45, "No Attributes for the given entity : "+getEntityName(entNum) + " - Adding ID Attribute");
    }
    ((Element)entity).setAttribute("id", ID);

  }


  /**
   * This method deletes the indexed entity from the DOM
   *
   * @param entNum int
   */
  public void deleteEntity(int entNum) {
    if ( (entityArray == null) || (entityArray.length < (entNum) + 1)) {
      Log.debug(20, "Unable to find entity at index");
      return;
    }
    Node entity = (entityArray[entNum]).getNode();
    Node parent = entity.getParentNode();
    parent.removeChild(entity);
    Entity[] newEntityArray = new Entity[entityArray.length-1];

    int newCount=0;
    for(int count=0; count < entityArray.length; count++){
      if(count != entNum)
         newEntityArray[newCount++] = entityArray[count];
    }
    entityArray = newEntityArray;
  }

	/**
	* This method deletes the indexed entity from the DOM
	*
	* @param entNum int
	*/
	public void deleteAllEntities() {
		if ( (entityArray == null) || (entityArray.length < 1)) {
			Log.debug(20, "Unable to find any entities");
			return;
		}
		Node parent = (entityArray[0]).getNode().getParentNode();
		for(int i = 0; i < entityArray.length; i++) {

			Node entity = (entityArray[i]).getNode();
			parent.removeChild(entity);
		}
		entityArray = null;
	}



  /**
   * This method automatically adds an entity in the DOM at the next available
   * position
   *
   * @param entity Entity
   */
  public void addEntity(Entity entity) {
    if (entityArray == null) {
      insertEntity(entity, 0);
      // since it is the new entity, so it has unsaved  data
      addDirtyEntityIndex(0);
    }
    else {
      insertEntity(entity, entityArray.length);
      // since it is the new entity, so it has unsaved  data
      addDirtyEntityIndex(entityArray.length-1);
    }
  }
  
  /**
   * Add a node which contains entity information into the data package
   * @param node usually get from entity wizard.
   */
  public void addEntity(Node node)
  {
	  Log.debug(30,"Adding Entity object to AbstractDataPackage..");
	  Entity entity = generateEntityFromNode(node);
	  if(entity != null)
	  {
         addEntity(entity);
	  }
  }
  
  /**
   * Replace an entity by another one in node format at given position.
   * If we couldn't find existed entity in the given position, the new entity will be added to. 
   * @param node
   * @param index
   */
  public void replaceEntity(Node node, int index)
  {
	  
	  Entity entity = generateEntityFromNode(node);
	
	  if(entity != null)
	  {
		  
		  if ( (entityArray == null) || (entityArray.length < index + 1)) 
		  {
		      Log.debug(20, "Unable to find entity at index, so we just add it in AbstractDataPackage.replaceEntity");
		      addEntity(node);
		  }
		  else
		  {
			  Log.debug(20, "in replace branch in AbstractDataPackage.replaceEntity");
			  deleteEntity(index);
			  insertEntity(entity, index);
		      addDirtyEntityIndex(index);		    
		  }
	  }
		
  }
  
 /*
  * Generate an Entity object base on given node (usually get from entity wizard)
  */
  private Entity generateEntityFromNode(Node node)
  {
	  Entity entity = null;
	  if(node != null)
	  {
		   // DFH --- Note: newDOM is root node (eml:eml), not the entity node
	      Node entNode = null;
	      String entityXpath = "";
	      try{
	        entityXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
	        "/xpathKeyMap/contextNode[@name='package']/entities")).getNodeValue();
	        NodeList entityNodes = XMLUtilities.getNodeListWithXPath(node,
	        entityXpath);
	        entNode = entityNodes.item(0);
	        
	        
	      }
	      catch (Exception w) {
	        Log.debug(20, "Error in trying to get entNode in ImportDataCommand");
	      }
	      if(entNode != null)
	      {
             entity = new Entity(entNode);
             // there may be some additionalMetadata in the newDOM
             // e.g. some info about consequtive delimiters
             // so should add this to the end of the adp
             try{
               NodeList ameta = XMLUtilities.getNodeListWithXPath(node, "/eml:eml/additionalMetadata");
               if (ameta!=null) {
                 for (int i=0;i<ameta.getLength();i++) {
                   Node ametaNode = ameta.item(i);
                   Node movedNode = (getMetadataNode().getOwnerDocument()).importNode(ametaNode, true);
                   getMetadataNode().appendChild(movedNode);
                 }
               }
             }
             catch (Exception ee) {
               Log.debug(30, "Error in trying to copy additionalMetadata"+ee.getMessage());
               entity = null;
             }
	      }
	  }
      return entity;
     
  }


  /**
   * This method inserts an entity in the DOM at the indexed position
   *
   * @param entity Entity
   * @param pos int
   */
  public void insertEntity(Entity entity, int pos) {
    Document thisDom = getMetadataNode().getOwnerDocument();
    Node newEntityNode = thisDom.importNode(entity.getNode(), true); // 'true' imports children
    // now have to figure out where to insert this cloned node and its children
    // First consider case where there are other entities
    if ( (entityArray != null) && (entityArray.length > 0)) {
      if (entityArray.length > pos) {
        Node par = ( (entityArray[pos]).getNode()).getParentNode();
        par.insertBefore(newEntityNode, (entityArray[pos]).getNode());
        // now in DOM; need to insert in EntityArray
        //Commenting out these lines and adding a call to getEntityArray below
        //Entity[] newEntArray = new Entity[entityArray.length + 1];
        //for (int i = 0; i < pos; i++) {
        //  newEntArray[i] = entityArray[i];
        //}
        //newEntArray[pos] = new Entity(newEntityNode, this);
        //for (int i = pos + 1; i < entityArray.length; i++) {
        //  newEntArray[i] = entityArray[i];
        //}
        //entityArray = newEntArray;

      }
      else { // insert at end of other entities
        Node par1 = ( (entityArray[0]).getNode()).getParentNode();
        par1.appendChild(newEntityNode);
        // now in DOM; need to insert in EntityArray
        // Commenting out these lines and adding a call to getEntityArray below
        //Entity[] newEntArray = new Entity[entityArray.length + 1];
        //for (int i = 0; i < entityArray.length; i++) {
        //  newEntArray[i] = entityArray[i];
        //}
        //newEntArray[entityArray.length] = new Entity(newEntityNode, this);
        //entityArray = newEntArray;
      }
      entityArray = null;
      entityArray = getEntityArray();
    }
    // must handle case where there are no existing entities!!!
    else {
      Node entityPar = null;
      String temp = "";
      try {
        Node tempNode = XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
            "/xpathKeyMap/contextNode[@name='package']/entityParent");
        temp = tempNode.getNodeValue();
        entityPar = XMLUtilities.getNodeWithXPath(getMetadataNode(), temp);
      }
      catch (Exception w) {
        Log.debug(20, "Error adding new entity!");
        return;
      }
      entityPar.appendChild(newEntityNode);
      Entity[] newEntArray = new Entity[1];
      newEntArray[0] = new Entity(newEntityNode, this);
      entityArray = newEntArray;
    }
  }

  /**
   *  This method creates an array of attribute Nodes for
   *  the indexed entity in the entityNode array.
   *  Note that the attribute array is created as needed rather
   *  than stored as a class member.
   *  Aug 2005 - class modified to save the last retreived array; this is used
   *  to enhance performance in building table displays with large number of columns
   */
  public Node[] getAttributeArray(int entityIndex) {
    if ((entityIndex==lastEntityIndex)&&(lastAttributeArray!=null)) return lastAttributeArray;
    if(entityIndex < 0)
        return null;

    if (entityIndex > (entityArray.length - 1)) {
      Log.debug(15, "entity index > number of entities");
      return null;
    }
    String attributeXpath = "";
    try {
      attributeXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='entity']/attributes")).getNodeValue();
      NodeList attributeNodes = XMLUtilities.getNodeListWithXPath( (entityArray[
          entityIndex]).getNode(), attributeXpath);
      if (attributeNodes == null) {
        Log.debug(15, "attributeList is null for entity " + entityIndex);
        return null;
      }
      Node[] attr = XMLUtilities.getNodeListAsNodeArray(attributeNodes);
      for (int i=0;i<attr.length;i++) {
        attr[i] = getReferencedNode(attr[i]);
      }
      lastAttributeArray = attr;
      lastEntityIndex = entityIndex;
      return attr;
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting attributeArray");
    }
    return null;
  }

  /**
   *  This method deletes the indexed attribute from the indexed
   *  entity
   */
  public void deleteAttribute(int entityIndex, int attributeIndex) {
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      Log.debug(20, "No such entity!");
      return;
    }
    Node[] attributes = getAttributeArray(entityIndex);
    if ( (attributes == null) || (attributes.length < 1)) {
      Log.debug(20, "No such attribute!");
      return;
    }
    Node attrNode = attributes[attributeIndex];
    Node parent = attrNode.getParentNode();
    parent.removeChild(attrNode);
    // reset saved values
    lastEntityIndex = -1;
    lastAttributeArray = null;
  }

  public Node appendAdditionalMetadata(Node addtMetadata) {
    Document thisDom = getMetadataNode().getOwnerDocument();
		Node rootNode = thisDom.getDocumentElement();
		Node newMetadataNode = thisDom.importNode(addtMetadata, true); // 'true' imports children
		if(rootNode == null) {
			return null;
		}
		return rootNode.appendChild(newMetadataNode);

	}

	// method to load custom units that the user had defined and are stored in the
	// 'additionalMetadata' subtree
	public void loadCustomUnits() {

		Document thisDom = getMetadataNode().getOwnerDocument();
		Node rootNode = thisDom.getDocumentElement();
		NodeList nodeList = rootNode.getChildNodes();
		Log.debug(40, "in loadCustom data, initial size = " + this.customUnitDictionaryUnitsCacheMap.keySet().size());
		for(int i = 0; i < nodeList.getLength(); i++) {

			Node child = nodeList.item(i);
			if(child.getNodeName().equals("additionalMetadata")) {
				OrderedMap map = XMLUtilities.getDOMTreeAsXPathMap(child);
				Log.debug(40, "got Map as - " + map.toString());
				extractUnits(map, "/additionalMetadata");
			}
		}
		Log.debug(40, "Extracted units -- \n");
		Iterator it = customUnitDictionaryUnitsCacheMap.keySet().iterator();
		while(it.hasNext()) {
			String key = (String) it.next();
			String[] arr = (String[])customUnitDictionaryUnitsCacheMap.get(key);
			Log.debug(40, key);
			for(int j = 0; j < arr.length; j++)
				Log.debug(40, "\t" + arr[j]);
		}
		Log.debug(40, "End of Extracted units -- \n");
	}


	protected void extractUnits(OrderedMap map, String xPath) {

		xPath += "/unitList[1]";
		int cnt = 1;
		while(true) {

			String name = (String)map.get(xPath + "/unit[" + cnt + "]/@name");
			if(name == null) break;
			String type = (String)map.get(xPath + "/unit[" + cnt + "]/@unitType");
			addNewUnit(type, name);
			cnt++;
		}
	}

	public String[] getUnitDictionaryCustomUnitTypes() {

		return customUnitDictionaryUnitTypesArray;
		/*int len = customUnitDictionaryUnitsCacheMap.keySet().size();
		String[] returnArr = new String[len];
		Iterator it = customUnitDictionaryUnitsCacheMap.keySet().iterator();
		int i = 0;
		while(it.hasNext()) {
			returnArr[i++] = (String)it.next();
		}
		return returnArr;*/
	}

	public String[] getUnitDictionaryUnitsOfType(String type) {

		String[] ret = (String[]) customUnitDictionaryUnitsCacheMap.get(type);
		if(ret == null) return new String[0];
		return ret;
	}

	public static void insertObjectIntoArray( Object[] arr, Object value, Object[] newArr) {

		int idx = Arrays.binarySearch(arr, value);
		int pos = -(idx + 1);
		int i = 0;
		for(i = 0 ;i < pos; i++)
			newArr[i] = arr[i];
		newArr[i] = value;
		for(int j = pos; j < arr.length;j++)
			newArr[j+1] = arr[j];

		return;
	}

	public boolean isNewCustomUnit(String type, String unit) {

		boolean newT = customUnitDictionaryUnitsCacheMap.containsKey(type);
		if(newT) {
			String[] units = (String[])customUnitDictionaryUnitsCacheMap.get(type);
			if(units == null) return true;
			if(Arrays.binarySearch(units, unit) >= 0) return false;
			else return true;
		}
		return true;

	}

	public void addNewUnit(String unitType, String unit) {

		int idx = Arrays.binarySearch(customUnitDictionaryUnitTypesArray, unitType);
		if(idx < 0) {
			String[] newArray = new String[customUnitDictionaryUnitTypesArray.length + 1];
			insertObjectIntoArray(customUnitDictionaryUnitTypesArray, unitType, newArray);
			customUnitDictionaryUnitTypesArray = newArray;
			String units[] = new String[1];
			units[0] = unit;
			customUnitDictionaryUnitsCacheMap.put(unitType, units);
		} else {
			String[] units = (String[]) customUnitDictionaryUnitsCacheMap.get(unitType);
			int idx1 = Arrays.binarySearch(units, unit);
			if(idx1 >= 0) return;
			String[] newUnitArr = new String[units.length + 1];
			insertObjectIntoArray(units, unit, newUnitArr);
			customUnitDictionaryUnitsCacheMap.put(unitType, newUnitArr);
		}

	}






  /**
   *  This method inserts an attribute at the indexed position
   *  in the indexed entity
   */
  public void insertAttribute(int entityIndex, Attribute newAttr, int attrIndex) {
    Node newAttrNode = newAttr.getNode();
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      Log.debug(20, "No such entity!");
      return;
    }
    Document thisDom = getMetadataNode().getOwnerDocument();
    Node newAttributeNode = thisDom.importNode(newAttrNode, true); // 'true' imports children
    Node[] attributes = getAttributeArray(entityIndex);
    if ( (attributes == null) || (attributes.length < 1)) {
      // currently there are NO attributes, so ignore attrIndex
      // and just insert
      Node attributeParent = null;
      String temp = "";
      try {
        temp = getGenericValue(
            "/xpathKeyMap/contextNode[@name='entity']/attributeParent");
        attributeParent = XMLUtilities.getNodeWithXPath( (entityArray[
            entityIndex]).getNode(), temp);
      }
      catch (Exception w) {
        Log.debug(20, "Error adding new attribute!");
        return;
      }
      if (attributeParent != null) {
        attributeParent.appendChild(newAttributeNode);
        //Note: attribute array is dynamically generated, so we don't need to update it here

      }
      else {
        // parent node does not exist in the current dom!!
        // temp has a path in string form with nodes separated by '/'s
        // assume that entity node DOES exist
        Log.debug(1, "Problem: no attribute parent !!");
      }
      return;
    }

    // there are current attributes, so must insert in proper location
    Node currentAttr = null;
    if (attrIndex > attributes.length - 1) {
      currentAttr = attributes[attributes.length - 1];
    }
    else {
      currentAttr = attributes[attrIndex];
    }
    if (attrIndex <= (attributes.length - 1)) {
      Node parent = currentAttr.getParentNode();
      parent.insertBefore(newAttributeNode, currentAttr);
    }
    else {
      // just put at end of current attributes
      Node parent = currentAttr.getParentNode();
      parent.appendChild(newAttributeNode);
    }
    // reset saved values
    lastEntityIndex = -1;
    lastAttributeArray = null;

  }

  /*
   *  This method retreives the attribute name at attributeIndex for
   *  the given entityIndex. i.e. getAttributeName(0,1) would return
   *  the first attribute name for the zeroth entity (indices are ) based)
   */
  public String getAttributeName(int entityIndex, int attributeIndex) {
    String temp = "";
    if(entityIndex < 0)
        return "No such entity!";
    if(attributeIndex < 0)
        return "no attributes!";

    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return "No such entity!";
    }
    Node[] attributes = getAttributeArray(entityIndex);
    if ( (attributes == null) || (attributes.length < (attributeIndex + 1))) {
      return "no attributes!";
    }
    Node attribute = attributes[attributeIndex];
    String attrXpath = "";
    try {
      attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='attribute']/name")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(attribute, attrXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting entity description" + w.toString());
    }
    return temp;
  }

  /**
   *  This method retreives the attribute index given an Attribute Name and the
   * 	index of the entity that the attribute is present in.
   * 	@param entityIndex  the index of the entity thats contains the given attribute
   *	@param attributeName the name of the attribute whose index is required
   *	@return int the index of the attribute in the given array. Returns -1 if the
   *					attribute is not found in the entity.
   *
   */

  public int getAttributeIndex(int entityIndex, String attributeName) {

    String temp = "";
    if(entityIndex < 0)
        return -1;

    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return -1;
    }
    Node[] attributes = getAttributeArray(entityIndex);
    if ( (attributes == null) || (attributes.length < 1)) {
      return -1;
    }
    for(int i = 0; i < attributes.length; i++) {

      Node attribute = attributes[i];
      String attrXpath = "";
      try {
        attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
        "/xpathKeyMap/contextNode[@name='attribute']/name")).getNodeValue();
        NodeList aNodes = XPathAPI.selectNodeList(attribute, attrXpath);
        if (aNodes == null) {
          continue;
        }
        Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
        temp = child.getNodeValue();
      }
      catch (Exception w) {
        Log.debug(50, "exception in getting entity description" + w.toString());
        continue;
      }
      if(temp.equals(attributeName))
        return i;
    }
    return -1;
  }

  /*
   *  This method retreives the attribute ID at attributeIndex for
   *  the given entityIndex. i.e. getAttributeID(0,1) would return
   *  the first attribute ID for the zeroth entity (indices are ) based)
   */
  public String getAttributeID(int entityIndex, int attributeIndex) {
    String id = "";
    if(entityIndex < 0)
        return "";
    if(attributeIndex < 0)
        return "";

    if ( (entityArray == null) || (entityArray.length < (entityIndex + 1))  ) {
      return "";
    }
    Node[] attributes = getAttributeArray(entityIndex);
    if ( (attributes == null) || (attributes.length < (attributeIndex + 1))) {
      return "";
    }
    Node attribute = attributes[attributeIndex];
    NamedNodeMap nnm = attribute.getAttributes();
    if(nnm !=null) {
      Node idNode = nnm.getNamedItem("id");
      if(idNode != null)
        id = idNode.getNodeValue();
      else {
        Log.debug(45, "No ID Attributes for the given column " +
                getAttributeName(entityIndex, attributeIndex));

      }
    }else {
      Log.debug(45, "No attributes for the given column : " +
            getAttributeName(entityIndex, attributeIndex));
    }
    return id;
  }

  /*
   *  This method retreives the attribute datatype at attributeIndex for
   *  the given entityIndex. i.e. getAttributeDataType(0,1) would return
   *  the first attribute datatype for the zeroth entity (indices are 0 based)
   */
  public String getAttributeDataType(int entityIndex, int attributeIndex) {
    String temp = "";
    if(entityIndex < 0)
        return "No such entity!";
    if(attributeIndex < 0)
        return "no attributes!";

    if ( (entityArray == null) || (entityArray.length < (entityIndex + 1))) {
      return "No such entity!";
    }
    Node[] attributes = getAttributeArray(entityIndex);
    if ( (attributes == null) || (attributes.length < (attributeIndex + 1))) {
      return "no attributes!";
    }
    Node attribute = attributes[attributeIndex];
    String attrXpath = "";
    try {
      // first see if there is a datatype node
      attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='attribute']/dataType")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(attribute, attrXpath);
      if (aNodes.getLength() > 0) {
        Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
        temp = child.getNodeValue();
        return temp;
      }

      // see if datatype is text
      attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='attribute']/isText")).getNodeValue();
      XObject xobj = XPathAPI.eval(attribute, attrXpath);
      boolean val = xobj.bool();
      if (val) {
        return "text";
      }
      // not text, try another xpath; check if Date
      attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='attribute']/isDate")).getNodeValue();
//      XMLUtilities.xPathEvalTypeTest(attribute, attrXpath);
      xobj = XPathAPI.eval(attribute, attrXpath);
      val = xobj.bool();
      if (val) {
        return "date";
      }
      // not text or date, must be a number
      attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='attribute']/numberType")).
          getNodeValue();

      aNodes = XPathAPI.selectNodeList(attribute, attrXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting attribute dataType" + w.toString());
    }
    return temp;
  }

  /*
   *  This method retreives the attribute unit at attributeIndex for
   *  the given entityIndex. i.e. getAttributeUnit(0,1) would return
   *  the first attribute unit for the zeroth entity (indices are ) based)
   */
  public String getAttributeUnit(int entityIndex, int attributeIndex) {
    String temp = "";
    if(entityIndex < 0)
        return "No such entity!";
    if(attributeIndex < 0)
        return "no attributes!";
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return "No such entity!";
    }
    Node[] attributes = getAttributeArray(entityIndex);
    if ( (attributes == null) || (attributes.length < (attributeIndex + 1))) {
      return "no attributes!";
    }
    Node attribute = attributes[attributeIndex];
    String attrXpath = "";
    try {
      attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='attribute']/unit")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(attribute, attrXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      if (aNodes.getLength() < 1) {
        return "";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue().trim();
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting attribute unit -- " + w.toString());
    }
    return temp;
  }

  /*
   *  This method creates an array of Nodes which contain 'physical'
   *  information for the indexed entity. [Note that usually there
   *  would only be a single 'physical' node in the dom, but multiple
   *  physical representations of an entity are allowed in eml2, for example]
   */
  public Node[] getPhysicalArray(int entityIndex) {
    if (entityIndex > (entityArray.length - 1)) {
      Log.debug(1, "entity index > number of entities");
      return null;
    }
    String physicalXpath = "";
    try {
      physicalXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='entity']/physical")).getNodeValue();
      NodeList physicalNodes = XMLUtilities.getNodeListWithXPath( (entityArray[
          entityIndex]).getNode(), physicalXpath);
      if (physicalNodes == null) {
        Log.debug(1, "physicalList is null!");
        return null;
      }
      return XMLUtilities.getNodeListAsNodeArray(physicalNodes);
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting physicalArray");
    }
    return null;
  }

  /**
   *  This method returns the name of indexed physical object for the
   *  indicated entity index.
   */
  public String getPhysicalName(int entityIndex, int physicalIndex) {
    String temp = "";
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ( (physicals == null) || (physicals.length < 1)) {
      return "no physicals!";
    }
    if (physicalIndex > (physicals.length - 1)) {
      return "physical index too large!";
    }
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/name")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,
                "exception in getting physical objectName description" + w.toString());
    }
    return temp;
  }

  /**
   *  This method returns the physocal format. Note that only text
   *  formats are displayed by Morpho. The format is for the physical
   *  object identified by 'physicalIndex' of the entity with the
   *  indicated 'entityIndex'
   */
  public String getPhysicalFormat(int entityIndex, int physicalIndex) {
    String temp = "";
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ( (physicals == null) || (physicals.length < 1)) {
      return "no physicals!";
    }
    if (physicalIndex > (physicals.length - 1)) {
      return "physical index too large!";
    }
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      // first see if the format is text
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/isText")).getNodeValue();
//      XMLUtilities.xPathEvalTypeTest(physical, physXpath);

      XObject xobj = XPathAPI.eval(physical, physXpath);
      if (xobj == null) {
        Log.debug(1, "null");

      }
      boolean val = xobj.bool();
      if (val) {
        return "text";
      }
      // not text, try another xpath
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/format")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,
                "exception in getting physical format description --- " + w.toString());
    }
    return temp;
  }

  /**
   *  This method returns the size for the indexed entity and
   *  physical object.
   */
  public String getPhysicalSize(int entityIndex, int physicalIndex) {
    String temp = "";
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ( (physicals == null) || (physicals.length < 1)) {
      return "no physicals!";
    }
    if (physicalIndex > (physicals.length - 1)) {
      return "physical index too large!";
    }
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/size")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting physical size" + w.toString());
    }
    return temp;
  }

  /**
   *  This method sets the size for the indexed entity and
   *  physical object.
   */
  public void setPhysicalSize(int entityIndex, int physicalIndex, String sizeS) {
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      Log.debug(20, "No such entity!");
      return;
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ( (physicals == null) || (physicals.length < 1)) {
      return;
    }
    if (physicalIndex > (physicals.length - 1)) {
      return;
    }
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/size")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes == null) {
        return;
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      child.setNodeValue(sizeS);
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting physical size" + w.toString());
    }
  }

  /**
   *  This method returns the FieldDelimiter for the indexed entity and
   *  physical object. An empty string is returned when there is no
   *  meaningful fieldDelimiter (e.g. not a text format)
   */
  public String getPhysicalFieldDelimiter(int entityIndex, int physicalIndex) {
    String temp = "";
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ( (physicals == null) || (physicals.length < 1)) {
      return "no physicals!";
    }
    if (physicalIndex > (physicals.length - 1)) {
      return "physical index too large!";
    }
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/fieldDelimiter")).
          getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,
                "exception in getting physical field delimiter" + w.toString());
    }
    return temp;
  }

  /**
   * returns boolean indicating whether repeated delimiters should be ignored
   *
   */
   abstract public boolean ignoreConsecutiveDelimiters(int entityIndex, int physicalIndex);

  /**
   *  This method sets the FieldDelimiter for the indexed entity and
   *  physical object.
   */
  public void setPhysicalFieldDelimiter(int entityIndex, int physicalIndex,
                                        String delim) {
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      Log.debug(20, "No such entity!");
      return;
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ( (physicals == null) || (physicals.length < 1)) {
      return;
    }
    if (physicalIndex > (physicals.length - 1)) {
      return;
    }
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/fieldDelimiter")).
          getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes == null) {
        return;
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      child.setNodeValue(delim);
    }
    catch (Exception w) {
      Log.debug(50,
                "exception in getting physical field delimiter" + w.toString());
    }
  }

  /**
   *  This method returns the number of header lines for the indexed entity and
   *  physical object. An empty string is returned when there is no
   *  meaningful numHeaderLines (e.g. not a text format)
   */
  public String getPhysicalNumberHeaderLines(int entityIndex, int physicalIndex) {
    String temp = "";
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ( (physicals == null) || (physicals.length < 1)) {
      return "no physicals!";
    }
    if (physicalIndex > (physicals.length - 1)) {
      return "physical index too large!";
    }
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/numberHeaderLines")).
          getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,
                "exception in getting physical number HeaderLines" + w.toString());
    }
    return temp;
  }

  /**
   *  This method returns the encoding method for the indexed entity and
   *  physical object. An empty string is returned when there is no
   *  encoding information
   *  It is assumed that the encoding given here describes the inline data
   *  when data is stored inline (DFH)
   */
  public String getEncodingMethod(int entityIndex, int physicalIndex) {
    String temp = "";
    if(entityIndex < 0)
        return "No such entity!";
    if(physicalIndex < 0)
        return "no such physical!";
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ( (physicals == null) || (physicals.length < 1)) {
      return "no physicals!";
    }
    if (physicalIndex > (physicals.length - 1)) {
      return "physical index too large!";
    }
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/encodingMethod")).
          getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting physical encodingMethod" + w.toString());
    }
    return temp;
  }

  /**
   *  This method returns the compression method for the indexed entity and
   *  physical object. An empty string is returned when there is no
   *  encoding information
   *  It is assumed that the compression given here describes the inline data
   *  when data is stored inline (DFH)
   */
  public String getCompressionMethod(int entityIndex, int physicalIndex) {
    String temp = "";
    if ( (entityArray == null) || (entityArray.length < (entityIndex) + 1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ( (physicals == null) || (physicals.length < 1)) {
      return "no physicals!";
    }
    if (physicalIndex > (physicals.length - 1)) {
      return "physical index too large!";
    }
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/compressionMethod")).
          getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,
                "exception in getting physical compressionMethod" + w.toString());
    }
    return temp;
  }

  /**
   *  This method creates an array of 'distribution' nodes, following
   *  the eml2 model of a subtree with information about the distribution
   *  of the actual data being characterized by the metadata.
   *  Multiple distribution subtrees are allowed in eml2; thus a Node
   *  array is returned (although usually only one distribution node is
   *  expected). Characterized by the entity and physical indicesS
   */
  public Node[] getDistributionArray(int entityIndex, int physicalIndex) {
    Node[] physNodes = getPhysicalArray(entityIndex);
    if (physNodes == null) {
      return null;
    }
    if (physicalIndex > (physNodes.length - 1)) {
      Log.debug(1, "physical index > number of physical objects");
      return null;
    }
    String distributionXpath = "";
    try {
      distributionXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='physical']/distribution")).
          getNodeValue();
      NodeList distributionlNodes = XMLUtilities.getNodeListWithXPath(physNodes[
          physicalIndex], distributionXpath);
      if (distributionlNodes == null) {
        Log.debug(20, "distributionList is null!");
        return null;
      }
      return XMLUtilities.getNodeListAsNodeArray(distributionlNodes);
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting distributionArray");
    }
    return null;
  }

  /**
   *  This method returns 'inline' data as a String for the indexed entity,
   *  physical object, and distribution object. Usually one would try to avoid
   *  large inline data collections because it will make DOMs hard to handle
   */
  public String getDistributionInlineData(int entityIndex, int physicalIndex,
                                          int distIndex) {
    String temp = "";
    Node[] distNodes = getDistributionArray(entityIndex, physicalIndex);
    if (distNodes == null) {
      return temp;
    }
    if (distIndex > distNodes.length - 1) {
      return temp;
    }
    Node distNode = distNodes[distIndex];
    String distXpath = "";
    try {
      distXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='distribution']/inline")).
          getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(distNode, distXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue().trim();
    }
    catch (Exception w) {
      Log.debug(50,
                "exception in getting distribution inline data: " + w.toString());
    }
    return temp;
  }

  /**
   *  This method returns the url for data as a String for the indexed entity,
   *  physical object, and distribution object. Returns an empty string if there
   *  is no url pointing to the data, or data is not referenced.
   */
  public String getDistributionUrl(int entityIndex, int physicalIndex,
                                   int distIndex) {
    String temp = "";
    Node[] distNodes = getDistributionArray(entityIndex, physicalIndex);
    if (distNodes == null) {
      return temp;
    }
    if (distIndex > distNodes.length - 1) {
      return temp;
    }
    Node distNode = distNodes[distIndex];
    String distXpath = "";
    try {
      distXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='distribution']/url")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(distNode, distXpath);
      if (aNodes == null) {
        return "aNodes is null !";
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      temp = child.getNodeValue().trim();
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting distribution url: " + w.toString());
    }
    return temp;
  }

  /**
   *  This method sets the url for data as a String for the indexed entity,
   *  physical object, and distribution object.    */
  public void setDistributionUrl(int entityIndex, int physicalIndex,
                                 int distIndex, String urlS) {
    String temp = "";
    Node[] distNodes = getDistributionArray(entityIndex, physicalIndex);
    if (distNodes == null) {
      // this is the case where no distribution info exists; must create the subtree
      try {
        String entityPar = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
            "/xpathKeyMap/contextNode[@name='package']/entityParent")).
            getNodeValue();
        String physicalXpath = (XMLUtilities.getTextNodeWithXPath(
            getMetadataPath(),
            "/xpathKeyMap/contextNode[@name='entity']/physical")).getNodeValue();
        String distributionXpath = (XMLUtilities.getTextNodeWithXPath(
            getMetadataPath(),
            "/xpathKeyMap/contextNode[@name='physical']/distribution")).
            getNodeValue();
        String urlxpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
            "/xpathKeyMap/contextNode[@name='distribution']/url")).getNodeValue();
        Node entnode = entityArray[entityIndex].getNode();
        String distxpath = entityPar + "/" + entnode.getNodeName() + "[" +
            (entityIndex + 1) + "]/"
            + physicalXpath + "/" + distributionXpath + "/" + urlxpath;
//Log.debug(1,"Distribution path: "+distxpath);
        // there is a problem in the above logic for creating a path if all the
        // entity nodes are not to the same type (e.g. not all are 'dataTable')
        // the index is incorrect in that case - DFH
        XMLUtilities.addTextNodeToDOMTree(getMetadataNode(), distxpath, urlS);
      }
      catch (Exception w) {
        Log.debug(6,
                  "error inside serDistributionUrl method in adp" + w.toString());
      }
      return;
    }
    if (distIndex > distNodes.length - 1) {
      return;
    }
    Node distNode = distNodes[distIndex];
    String distXpath = "";
    try {
      distXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='distribution']/url")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(distNode, distXpath);
      if (aNodes == null) {
        Log.debug(10, "aNodes is null !");
        return;
      }
      Node child = aNodes.item(0).getFirstChild(); // get first ?; (only 1?)
      child.setNodeValue(urlS);
    }
    catch (Exception w) {
      Log.debug(50, "exception in setting distribution url: " + w.toString());
    }
  }
  
  /**
   *  This method returns the access for data for the indexed entity,
   *  physical object, and distribution object. 
   */
  public Node getEntityAccess(int entityIndex, int physicalIndex,
                                   int distIndex) {
    Node temp = null;
    Node[] distNodes = getDistributionArray(entityIndex, physicalIndex);
    if (distNodes == null) {
      return temp;
    }
    if (distIndex > distNodes.length - 1) {
      return temp;
    }
    Node distNode = distNodes[distIndex];
    String distXpath = "";
    try {
      distXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='distribution']/access")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(distNode, distXpath);
      if (aNodes == null) {
        return temp;
      }
      temp = aNodes.item(0);
    }
    catch (Exception w) {
      Log.debug(50, "exception in getting entity access: " + w.toString());
    }
    return temp;
  }
  
  /**
   *  This method sets the access for data for the indexed entity,
   *  physical object, and distribution object.    */
  public void setEntityAccess(int entityIndex, int physicalIndex,
                                 int distIndex, Node accessNode) {

	  Document thisDom = getMetadataNode().getOwnerDocument();
	  Node newAccessSubtree = null;
	  if (accessNode != null) {
		  newAccessSubtree = thisDom.importNode(accessNode, true); // 'true' imports children
	  }
	    
	Node[] distNodes = getDistributionArray(entityIndex, physicalIndex);
    if (distNodes == null) {
      // this is the case where no distribution info exists; must create the subtree
      try {
        String entityPar = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
            "/xpathKeyMap/contextNode[@name='package']/entityParent")).
            getNodeValue();
        String physicalXpath = (XMLUtilities.getTextNodeWithXPath(
            getMetadataPath(),
            "/xpathKeyMap/contextNode[@name='entity']/physical")).getNodeValue();
        String distributionXpath = (XMLUtilities.getTextNodeWithXPath(
            getMetadataPath(),
            "/xpathKeyMap/contextNode[@name='physical']/distribution")).
            getNodeValue();
        String accessxpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
            "/xpathKeyMap/contextNode[@name='distribution']/access")).getNodeValue();
        Node entnode = entityArray[entityIndex].getNode();
        String distxpath = entityPar + "/" + entnode.getNodeName() + "[" +
            (entityIndex + 1) + "]/"
            + physicalXpath + "/" + distributionXpath + "/" + accessxpath;
        //Log.debug(1,"Distribution path: "+accessxpath);
        // there is a problem in the above logic for creating a path if all the
        // entity nodes are not to the same type (e.g. not all are 'dataTable')
        // the index is incorrect in that case - DFH
        XMLUtilities.addNodeToDOMTree(getMetadataNode(), distxpath, newAccessSubtree);
      }
      catch (Exception w) {
        Log.debug(6,
                  "error inside set entity access method in adp" + w.toString());
      }
      return;
    }
    if (distIndex > distNodes.length - 1) {
      return;
    }
    Node distNode = distNodes[distIndex];
    String distXpath = "";
    try {
      distXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='distribution']/access")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(distNode, distXpath);
      if (aNodes == null) {
        Log.debug(10, "aNodes is null !");
        return;
      }
      if (newAccessSubtree != null) {
	      if (aNodes.getLength() > 0) {
		      Node child = aNodes.item(0); // get first ?; (only 1?)
		      child.getParentNode().replaceChild(newAccessSubtree, child);
	      }
	      else {
	    	  distNode.appendChild(newAccessSubtree);
	      }
      }
      else {
    	  //delete it
    	  if (aNodes.getLength() > 0) {
		      Node child = aNodes.item(0); // get first ?; (only 1?)
		      child.getParentNode().removeChild(child);
	      }
      }
    }
    catch (Exception w) {
      Log.debug(5, "exception in setting entity access: " + w.toString());
    }
  }

  /*
   * This method loops through all the entities in a package and checks for
   * url references to data files (i.e. data external to the data package).
   * Both metatcat and local file stores are checked to see if the data has
   * already been saved. If not, the temp directory is checked. Note that it
   * is assumed that the data file has been assigned an id and stored in the
   * temp directory if it has not been saved to one of the stores
   *
   * It has been assumed that the 'location' has been set to point to the
   * place where the data is to be saved.
   */
  public void serializeData(String dataDestination)  {
	Log.debug(30, "serilaize data =====================");
    File dataFile = null;
    Morpho morpho = Morpho.thisStaticInstance;
    FileSystemDataStore fsds = new FileSystemDataStore(morpho);
    MetacatDataStore mds = new MetacatDataStore(morpho);
    getEntityArray();
    //Log.debug(1, "About to check entityArray!");
    if (entityArray == null) {
      Log.debug(30, "Entity array is null, no need to serialize data");
      return; // there is no data!
    }
    if (dataDestination == null)
    {
    	Log.debug(30, "User didn't specify the data destination");
    	return;
    }
    
    setDataIDChanged(false);
    for (int i = 0; i < entityArray.length; i++) {
      String protocol = getUrlProtocol(i);
      String objectName = getPhysicalName(i, 0);
      Log.debug(25, "object name is ===================== "+ objectName);
      if(protocol != null && protocol.equals("ecogrid:") ) {
    	  
        String docid = getUrlInfo(i);
        Log.debug(30, "handle data file  with index "+i+ ""+docid);
        //if (urlinfo != null)
        //{
        	//String docid = getDocidFromEcoGridURL(urlinfo);
    	if (docid != null)
    	{
    		boolean isDirty = containsDirtyEntityIndex(i);
    		Log.debug(30, "url "+docid+" with index "+i+" is dirty "+isDirty);
    		// Detects if docid conflict occurs
	        //boolean updateFlag = !(version.equals("1"));
	        boolean existFlag = false;
	        String statusInMetacat = null;
	        String statusInLocal = null;
	        String conflictLocation = null;
	        //Check to see if id confilct or not
	        if((dataDestination.equals(AbstractDataPackage.METACAT))) 
	        {
	    	    statusInMetacat = mds.status(docid);
	    	    Log.debug(30, "docid "+docid+ " status in metacat is "+statusInMetacat);
	    	    if (statusInMetacat != null && statusInMetacat.equals(DataStoreInterface.CONFLICT))
	    	    {
	    	    	conflictLocation = DocidIncreaseDialog.METACAT;
	    	    	existFlag = true;
	    	    }
	        }
	        else if((dataDestination.equals(AbstractDataPackage.LOCAL))) 
	        {
	        	statusInLocal = fsds.status(docid);
	        	Log.debug(30, "docid "+docid+ " status in local is "+statusInLocal);
	        	if (statusInLocal != null && statusInLocal.equals(DataStoreInterface.CONFLICT))
	    	    {
	    	    	conflictLocation = DocidIncreaseDialog.LOCAL;
	    	    	existFlag = true;
	    	    }
	        }
	        else if (dataDestination.equals(AbstractDataPackage.BOTH))
	        {
	    	    statusInMetacat = mds.status(docid);
	        	statusInLocal = fsds.status(docid);
	        	Log.debug(30, "docid "+docid+ " status in local is "+statusInLocal +" and status in metacat is"+statusInMetacat);
        		if (statusInMetacat != null && statusInLocal != null && 
        				statusInLocal.equals(DataStoreInterface.CONFLICT) && statusInMetacat.equals(DataStoreInterface.CONFLICT))
	        	{
	        			conflictLocation =  DocidIncreaseDialog.LOCAL + " and "+ DocidIncreaseDialog.METACAT;
	        			existFlag = true;
	        		    //this.setIdentifierChangedInLocalSerialization(true);
	        		    //this.setIdentifierChangedInMetacatSerialization(true);
	        	}
	        	else if (statusInMetacat != null  && statusInMetacat.equals(DataStoreInterface.CONFLICT))
	        	{
	        			conflictLocation =  DocidIncreaseDialog.METACAT;
	        			existFlag = true;
	        			//this.setIdentifierChangedInMetacatSerialization(true);
	        	}
	        	else if (statusInLocal != null && statusInLocal.equals(DataStoreInterface.CONFLICT))
	        	{
	        			conflictLocation =  DocidIncreaseDialog.LOCAL;
	        			existFlag = true;
	        			//this.setIdentifierChangedInLocalSerialization(true);
	        	}
	        	
	        }
	      
	        if (existFlag && isDirty)
	        {
	        	 // If docid conflict and the entity is dirty, we need to
		        // pop-out an window to change the docid
	        	Log.debug(30, "The docid "+docid+" exists and has unsaved data. So increase docid for it");
	        	docid = handleDataIdConfiction(docid, conflictLocation);
	        	
	        	
	        }
	        else if(existFlag)
	        {
	        	// if docid conflict, but entity wasn't changed, do nothing (skip the saving steps)
	        	Log.debug(30, "The docid "+docid+" exists and but was NOT changed. So skip serializedata");
	        	continue;
	        }
	        
	        // reset urlinfo with new docid (if docid was not changed, the url will still be same).
	        
	        // urlinfo should be the id in a string
	        if (dataDestination.equals(LOCAL))  {
	          handleLocal(docid);
	        }
	        else if (dataDestination.equals(METACAT)) {
	          handleMetacat(docid, objectName);
	        }
	        else if (dataDestination.equals(BOTH)) {
	        	//Log.debug(1, "~~~~~~~~~~~~~~~~~~~~~~set bothLoation true ");
	          //serializeDataAtBothLocation =true;
	          handleBoth(docid, objectName);
	        }
	        // reset the map after finishing save. There is no need for this pair after saving
	        original_new_id_map = new Hashtable();
	        
	        // newDataFile must have worked; thus update the package
	        String urlinfo = "ecogrid://knb/"+docid;
	        setDistributionUrl(i, 0, 0, urlinfo);
	        //File was saved successfully, we need to remove the index from the vector.
	        if (isDirty)
    		{
    		    removeDirtyEntityIndex(i);
    		}
    	}
        //}
      }
    }
   
    
    //Log.debug(1, "~~~~~~~~~~~~~~~~~~~~~~set bothLoation false ");
    //serializeDataAtBothLocation =false;
  }
   
  /*
   * Strips docid (e.g tao.1.1) out from ecogrid url (e.g. ecogrid://knb/tao.1.1).
   * Null will be returned if docid couldn't be found.
   */
   private String getDocidFromEcoGridURL(String ecogridURL)
   {
	   String docid = null;
	   Log.debug(30, "the ecogridURL is "+ecogridURL);
	   if (ecogridURL != null)
	   {
		   int index = ecogridURL.lastIndexOf("/");
		   index ++;
		   if (index < ecogridURL.length())
		   {
		      docid = ecogridURL.substring(index);
		   }
	   }
	   Log.debug(30, "the docid stripped from ecogridURL is "+docid);
	   return docid;
   }

  /*
   * Saves the entity into local system
   */
  private void handleLocal(String docid) {
	Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~handle local "+docid);
    File dataFile = null;
    String oldDocid = null;
    Morpho morpho = Morpho.thisStaticInstance;
    FileSystemDataStore fds = new FileSystemDataStore(morpho);
    // if morpho serilaize data into local or metacat, and docid was changed, 
    // we need to get the old docid and find the it in temp dir
    if (!original_new_id_map.isEmpty())
    {
    	//System.out.println("the key is "+urlinfo);
    	//System.out.println("the hashtable is "+original_new_id_map);
    	//Log.debug(1, "~~~~~~~~~~~~~~~~~~~~~~change id in local serialization ");
    	oldDocid = (String)original_new_id_map.get(docid);
    	Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~the id from map is " +oldDocid);  	
    	
      }
      // if oldDocid is null, that means docid change. So we set old docid to be the current id
      if (oldDocid == null)
      {
    	  oldDocid = docid;
      }
      Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~eventually old id is  " +oldDocid); 
      ConfigXML profile = morpho.getProfile();
      String separator = profile.get("separator", 0);
      separator = separator.trim();
      String temp = new String();
      temp = oldDocid.substring(0, oldDocid.indexOf(separator));
      temp += "/" +
            oldDocid.substring(oldDocid.indexOf(separator) + 1, oldDocid.length());
      Log.debug(30, "The temp file path is "+temp);
      try {
           dataFile = fds.openTempFile(temp);
          //open old file name (if no file change, the old file name will be as same as docid).
           InputStream dfis = new FileInputStream(dataFile);
          //Log.debug(1, "ready to save: urlinfo: "+urlinfo);
          fds.saveDataFile(docid, dfis);
          // the temp file has been saved; thus delete
          dfis.close();
//          dataFile.delete();
      }catch (Exception qq) {
        // if a datafile is on metacat and one wants to save locally
        try{
          MetacatDataStore mds = new MetacatDataStore(morpho);
          //open old file name (if no file change, the old file name will be as same as docid).
          dataFile = mds.openDataFile(oldDocid);
          InputStream dfis = new FileInputStream(dataFile);
          fds.saveDataFile(docid, dfis);
          dfis.close();
        }catch (Exception qqq) {
          // some other problem has occured
          Log.debug(5, "Some problem with saving local data files has occurred! "+qqq.getMessage());
          qq.printStackTrace();
        }//end catch
      }
    
  }
  
  
  
  /*
   * Saves data files to Metacat
   */
  private void handleMetacat(String docid, String objectName) {
		Log.debug(30, "----------------------------------------handle metacat "+docid);
	    File dataFile = null;
	    File metacatDataFile = null;
	    String oldDocid = null;
	    boolean sourceFromTemp = true;
	    Morpho morpho = Morpho.thisStaticInstance;
		 FileSystemDataStore fds = new FileSystemDataStore(morpho);
		 MetacatDataStore mds = new MetacatDataStore(morpho);
	    ConfigXML profile = morpho.getProfile();
	    String separator = profile.get("separator", 0);
	    separator = separator.trim();
	    String temp = new String();
	    if (!original_new_id_map.isEmpty())
	    {
	    	//System.out.println("the key is "+urlinfo);
	    	//System.out.println("the hashtable is "+original_new_id_map);
	    	//Log.debug(1, "~~~~~~~~~~~~~~~~~~~~~~change id in local serialization ");
	    	oldDocid = (String)original_new_id_map.get(docid);
	    	Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~the id from map is " +oldDocid);  	
	    	
	      }
	      // if oldDocid is null, that means docid change. So we set old docid to be the current id
	      if (oldDocid == null)
	      {
	    	  oldDocid = docid;
	      }
	    temp = oldDocid.substring(0, oldDocid.indexOf(separator));
	    temp = temp + "/" +
	        oldDocid.substring(oldDocid.indexOf(separator) + 1, oldDocid.length());
	    // Check where is the source data file (from temp or from data file)
	    try
	    {
	    	dataFile = fds.openTempFile(temp);
	    }
	    catch(FileNotFoundException e)
	    {
	    	try
	    	{
	    	    dataFile =  fds.openFile(oldDocid);
	    	    sourceFromTemp = false;
	    	}
	    	catch(Exception ee)
	    	{
	    		Log.debug(5, "Couldn't find "+oldDocid+" in local system, so morpho couldn't upload it to metacat");
	    	    return;
	    	}   	
	    }
	  
	    uploadDataFileToMetacat(docid, dataFile, objectName, sourceFromTemp, mds);
	
	  }

  
  
  /*
   * Loads the data file to metacat
   */
  private void uploadDataFileToMetacat(String identifier, File dataFile, String objectName, boolean fromTemp, 
                                                         MetacatDataStore mds)
  {
	        try
	          {
		          InputStream dfis = new FileInputStream(dataFile);
		          try
		          {
	                 mds.newDataFile(identifier, dataFile, objectName);
		          }
		          catch (MetacatUploadException mue) 
		          {
		            //Gets some error from metacat
		        	  Log.debug(5, "Some problem with saving data files has occurred! "+mue.getMessage());
		          	 
		          }
	              // the temp file has been saved; thus delete
		          if (fromTemp)
		          {
	                  dataFile.delete();
		          }
	          } 
              catch (Exception qqq) 
              {
                 // some other problem has occured
                 Log.debug(5, "Some problem with saving data files has occurred! "+qqq.getMessage());
                 qqq.printStackTrace();
              }   
      
  }
  
  /*
   * If the docid is revision 1, automatically increase data file identifier number without notifying user.
   * If the docid is bigger than revision 1, user will be asked to make a chioce: increasing docid or increasing revision.
   */
  private String handleDataIdConfiction(String identifier, String conflictLocation) 
  {
	   Morpho morpho = Morpho.thisStaticInstance;
	    String version = null;
	    int revision = -1;
	    String scope = null;
	    boolean update = true;
	    String originalIdentifier = null;
	    if (identifier != null)
	    {
	    	originalIdentifier = identifier;
	    	// get revision number
		    int lastperiod = identifier.lastIndexOf(".");
		    if (lastperiod>-1) {
		      version = identifier.substring(lastperiod+1, identifier.length());
		      scope = identifier.substring(0, lastperiod);
		      Log.debug(55, "scope: "+scope+"---version: "+version);
		    }
		  try
		  {
			  revision = (new Integer(version).intValue());
			  if (revision == 1)
			  {
				  update = false;
			  }
		  }
		  catch(Exception e)
		  {
			  Log.debug(5, "Couldn't find the revison in docid "+identifier +" since "+e.getMessage());
		  }
		  
		  //if it is update, we need give user options to choose: increase docid or revision number
		  if (update)
		  {
			  DocidIncreaseDialog docidIncreaseDialog = new DocidIncreaseDialog(identifier, conflictLocation);
		      String choice = docidIncreaseDialog.getUserChoice();
		      if (choice != null && choice.equals(DocidIncreaseDialog.INCEASEID))
		      {
		            update =false;
		      }
		      else
		      {
		    	  update = true;
		      }
		  }
		  
		  // decides docid base on user choice
		  if (!update)
		  {
		     AccessionNumber an = new AccessionNumber(morpho);
	         identifier = an.getNextId();
		  }
		  else
		  {
			  int newRevision = this.getNextRevisionNumber(identifier);
	    	   identifier = scope+"."+newRevision;
		  }
		  // store the new id and original id into a map. 
		  // So when morpho know the new id when it serialize
          original_new_id_map.put(identifier,originalIdentifier);
          setDataIDChanged(true);
	  
	  }
	   Log.debug(30, "======================new identifier is "+identifier);
	   return identifier;
  }
  
  /*
   * Returns a checksum of the given file. 0 will be return if some bad thing happens
   */
  private long getFileCheckSum(File file)
  {
	  long checksum = 0;
	  try {
	        // Compute Adler-32 checksum
	        CheckedInputStream cis = new CheckedInputStream(
	            new FileInputStream(file), new Adler32());
	        byte[] tempBuf = new byte[128];
	        while (cis.read(tempBuf) >= 0) 
	        {
	        	
	        }
	        checksum = cis.getChecksum().getValue();
	    } 
	    catch (IOException e) 
	    {
	        Log.debug(30, "Couldn't get the checksum value of the file" +file.getName());
	    }
	    Log.debug(30, "The checksum of " +file.getName()+" is "+checksum);
	    return checksum;
  }


  private void handleBoth(String docid, String objectName) 
  {
    handleLocal(docid);
    handleMetacat(docid, objectName);   
  }

  private String getUrlInfo(int entityIndex) {
    String urlinfo = getDistributionUrl(entityIndex, 0, 0);
    // assumed that urlinfo is of the form 'protocol://systemname/localid/other'
    // protocol is probably 'ecogrid'; system name is 'knb'
    // we just want the local id here
    int indx2 = urlinfo.indexOf("//");
    if (indx2 > -1) {
      urlinfo = urlinfo.substring(indx2 + 2);
      // now start should be just past the '//'
    }
    indx2 = urlinfo.indexOf("/");
    if (indx2 > -1) {
    urlinfo = urlinfo.substring(indx2 + 1);
    //now should be past the system name
    }
    indx2 = urlinfo.indexOf("/");
    if (indx2 > -1) {
      urlinfo = urlinfo.substring(0, indx2);
      // should have trimmed 'other'
    }
    if (urlinfo.length() == 0) {
      return "";
    }
    return urlinfo;
}

  private String getUrlProtocol(int entityIndex) {
    String urlinfo = getDistributionUrl(entityIndex, 0, 0);
    int indx2 = urlinfo.indexOf("//");
    if (indx2<0) return "";
    return urlinfo.substring(0,indx2);
  }

  /**
   * exports a package to a given path
   * @param path the path to which this package should be exported.
   */
  public void export(String path) {
    Log.debug(20, "exporting...");
    Log.debug(20, "path: " + path);
    Log.debug(20, "id: " + id);
    Log.debug(20, "location: " + location);
    File f = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if (location.equals(BOTH)) {
      localloc = true;
      metacatloc = true;
    }
    else if (location.equals(METACAT)) {
      metacatloc = true;
    }
    else if (location.equals(LOCAL)) {
      localloc = true;
    }
    else {
      Log.debug(1, "Package has not been saved! Unable to export!");
      return;
    }

    //  get a list of the files and save them to the new location. if the file
    //  is a data file, save it with its original name.
    //  With the use of AbstractDataPackage, there is only a single metadata doc
    //  and we will use the DOM; may be multiple data files, however
    String packagePath = path + "/" + id + ".package";
    String sourcePath = packagePath + "/metadata";
    String cssPath = packagePath + "/export";
    String dataPath = packagePath + "/data";
    File savedir = new File(packagePath);
    File savedirSub = new File(sourcePath);
    File cssdirSub = new File(cssPath);
    File savedirDataSub = new File(dataPath);
    savedir.mkdirs(); //create the new directories
    savedirSub.mkdirs();
    cssdirSub.mkdirs();
    savedirDataSub.mkdirs();
    StringBuffer[] htmldoc = new StringBuffer[2]; //DFH

    // for css
    try
    {
      InputStream input = this.getClass().getResourceAsStream("/style/CSS/export.css");
      InputStreamReader styleSheetReader = new InputStreamReader(input);
      //FileReader styleSheetReader = new FileReader(styleSheetSource);
      StringBuffer buffer = IOUtil.getAsStringBuffer(styleSheetReader, true);
      // Create a wrter
      String fileName = cssPath + "/export.css";
      FileWriter writer = new FileWriter(fileName);
      IOUtil.writeToWriter(buffer, writer, true);
    }
    catch (Exception e)
    {
      Log.debug(30, "Error in copying css: "+e.getMessage());
    }

    // for metadata file
    f = new File(sourcePath + "/" + id);

    File openfile = null;
    try {
      if (localloc) { //get the file locally and save it
        openfile = fileSysDataStore.openFile(id);
      }
      else if (metacatloc) { //get the file from metacat
        openfile = metacatDataStore.openFile(id);
      }
      FileInputStream fis = new FileInputStream(openfile);
      BufferedInputStream bfis = new BufferedInputStream(fis);
      FileOutputStream fos = new FileOutputStream(f);
      BufferedOutputStream bfos = new BufferedOutputStream(fos);
      int c = bfis.read();
      while (c != -1) { //copy the files to the source directory
        bfos.write(c);
        c = bfis.read();
      }
      bfos.flush();
      bfis.close();
      bfos.close();

      // css file
/*      File outputCSSFile = new File(cssPath + "/export.css");
//      File inputCSSFile = new File(getClass().getResource("/style/export.css"));
//      FileInputStream inputCSS = new FileInputStream(inputCSSFile);
      FileInputStream inputCSS = (FileInputStream) getClass().getResource("/style/export.css");
      BufferedInputStream inputBufferedCSS = new BufferedInputStream(inputCSS);
      FileOutputStream outputCSS = new FileOutputStream(outputCSSFile);
      BufferedOutputStream outputBufferedCSS = new BufferedOutputStream(
          outputCSS);
      c = inputBufferedCSS.read();
      while (c != -1) { //copy the files to the source directory
        outputBufferedCSS.write(c);
        c = inputBufferedCSS.read();
      }
      outputBufferedCSS.flush();
      inputBufferedCSS.close();
      outputBufferedCSS.close();
*/
      // for html
      Reader xmlInputReader = null;
      Reader result = null;
      StringBuffer tempPathBuff = new StringBuffer();
      xmlInputReader = new FileReader(openfile);

      XMLTransformer transformer = XMLTransformer.getInstance();
      // add some property for style sheet
      transformer.removeAllTransformerProperties();
      transformer.addTransformerProperty(
          XMLTransformer.SELECTED_DISPLAY_XSLPROP,
          XMLTransformer.XSLVALU_DISPLAY_PRNT);
      transformer.addTransformerProperty(
          XMLTransformer.HREF_PATH_EXTENSION_XSLPROP, HTMLEXTENSION);
      transformer.addTransformerProperty(
          XMLTransformer.PACKAGE_ID_XSLPROP, id);
      transformer.addTransformerProperty(
          XMLTransformer.PACKAGE_INDEX_NAME_XSLPROP, METADATAHTML);
      transformer.addTransformerProperty(
          XMLTransformer.DEFAULT_CSS_XSLPROP, EXPORTSYLE);
      transformer.addTransformerProperty(
          XMLTransformer.ENTITY_CSS_XSLPROP, EXPORTSYLE);
      transformer.addTransformerProperty(
          XMLTransformer.CSS_PATH_XSLPROP, ".");
      try {
        result = transformer.transform(xmlInputReader);
      }
      catch (IOException e) {
        e.printStackTrace();
        Log.debug(9, "Unexpected Error Styling Document: " + e.getMessage());
        e.fillInStackTrace();
        throw e;
      }
      finally {
        xmlInputReader.close();
      }
      transformer.removeAllTransformerProperties();

      try {
        htmldoc[0] = IOUtil.getAsStringBuffer(result, true);
        //"true" closes Reader after reading
      }
      catch (IOException e) {
        e.printStackTrace();
        Log.debug(9, "Unexpected Error Reading Styled Document: "
                  + e.getMessage());
        e.fillInStackTrace();
        throw e;
      }

      tempPathBuff.delete(0, tempPathBuff.length());

      tempPathBuff.append(packagePath);
      tempPathBuff.append("/");
      tempPathBuff.append(METADATAHTML);
      tempPathBuff.append(HTMLEXTENSION);
      saveToFile(htmldoc[0], new File(tempPathBuff.toString()));

    }
    catch (Exception w) {
      w.printStackTrace();
      Log.debug(9, "Unexpected Error Reading Styled Document: "
                + w.getMessage());
    }

    exportDataFiles(savedirDataSub.getAbsolutePath());
    JOptionPane.showMessageDialog(null,
                                  "Package export is complete ! ");
  }

  /**
   * Exports a package to a zip file at the given path
   * @param path the path to export the zip file to
   */
  public void exportToZip(String path) {
    try {
      //export the package in an uncompressed format to the temp directory
      //then zip it up and save it to the specified path
      String tempdir = config.getConfigDirectory() + File.separator +
          config.get("tempDir", 0);
      export(tempdir + "/tmppackage");
      File zipfile = new File(path);
      FileOutputStream fos = new FileOutputStream(zipfile);
      ZipOutputStream zos = new ZipOutputStream(fos);
      String temppackdir = tempdir + "/tmppackage/" + id + ".package";
      File packdirfile = new File(temppackdir);
      String[] dirlist = packdirfile.list();
      String packdir = id + ".package";
      //zos.putNextEntry(new ZipEntry(packdir));
      for (int i = 0; i < dirlist.length; i++) {
        String entry = temppackdir + "/" + dirlist[i];
        ZipEntry ze = new ZipEntry(packdir + "/" + dirlist[i]);
        File entryFile = new File(entry);
        if (!entryFile.isDirectory()) {
          ze.setSize(entryFile.length());
          zos.putNextEntry(ze);
          FileInputStream fis = new FileInputStream(entryFile);
          int c = fis.read();
          while (c != -1) {
            zos.write(c);
            c = fis.read();
          }
          zos.closeEntry();
        }
      }
      // for data file
      String dataPackdir = packdir + "/data";
      String tempDatapackdir = temppackdir + "/data";
      File dataFile = new File(tempDatapackdir);
      String[] dataFileList = dataFile.list();
      if (dataFileList != null) {
        for (int i = 0; i < dataFileList.length; i++) {
          String entry = tempDatapackdir + "/" + dataFileList[i];
          ZipEntry ze = new ZipEntry(dataPackdir + "/" + dataFileList[i]);
          File entryFile = new File(entry);
          if (!entryFile.isDirectory())
          {
	          ze.setSize(entryFile.length());
	          zos.putNextEntry(ze);
	          FileInputStream fis = new FileInputStream(entryFile);
	          int c = fis.read();
	          while (c != -1) {
	            zos.write(c);
	            c = fis.read();
	          }
	          zos.closeEntry();
          }
          else
          {
        	  String[] secondaryDataFileList = entryFile.list();
        	  if (secondaryDataFileList != null)
        	  {
        		  for (int j=0; j<secondaryDataFileList.length; j++)
        		  {
        			  String secondaryEntry = entry+"/"+secondaryDataFileList[j];
        			  ZipEntry secondaryZE = new ZipEntry(dataPackdir + "/" + dataFileList[i]+"/"+secondaryDataFileList[j]);
        			  File secondaryEntryFile = new File (secondaryEntry);
        			  secondaryZE.setSize(secondaryEntryFile.length());
        			  zos.putNextEntry(secondaryZE);
        			  FileInputStream fis = new FileInputStream(secondaryEntryFile);
        			  int c = fis.read();
        			  while (c != -1)
        			  {
        				  zos.write(c);
        				  c = fis.read();
        			  }
        			  zos.closeEntry();
        		  }
        	  }
          }
        }
      }

      // for css
      try
      {
        String cssPath = packdir + "/export";
        InputStream input = this.getClass().getResourceAsStream("/style/CSS/export.css");
        InputStreamReader styleSheetReader = new InputStreamReader(input);
        //FileReader styleSheetReader = new FileReader(styleSheetSource);
        StringBuffer buffer = IOUtil.getAsStringBuffer(styleSheetReader, true);
        // Create a wrter
        ZipEntry ze = new ZipEntry(cssPath + "/export.css");
        ze.setSize(buffer.length());
        zos.putNextEntry(ze);
        int count = 0;
        int c = buffer.charAt(count);
        while (c != -1) {
          zos.write(c);
          count++;
          c = buffer.charAt(count);
        }
        zos.closeEntry();
      }
      catch (Exception e)
      {
        Log.debug(30, "Error in copying css: "+e.getMessage());
      }

      packdir += "/metadata";
      temppackdir += "/metadata";
      File sourcedir = new File(temppackdir);
      File[] sourcefiles = listFiles(sourcedir);
      for (int i = 0; i < sourcefiles.length; i++) {
        File f = sourcefiles[i];

        ZipEntry ze = new ZipEntry(packdir + "/" + f.getName());
        ze.setSize(f.length());
        zos.putNextEntry(ze);
        FileInputStream fis = new FileInputStream(f);
        int c = fis.read();
        while (c != -1) {
          zos.write(c);
          c = fis.read();
        }
        zos.closeEntry();
      }
      zos.flush();
      zos.close();
    }
    catch (Exception e) {
      Log.debug(5, "Exception in exporting to zip file (AbstractDataPackage)" +e.getMessage());
    }
  }


  /**
   * copies all the data files in a package to a directory indicated
   * by 'path'. Files are given the original file name, if available
   */
  public void exportDataFiles(String path) {
    String origFileName;
    File dataFile = null;
    Morpho morpho = Morpho.thisStaticInstance;
    FileSystemDataStore fds = new FileSystemDataStore(morpho);
    MetacatDataStore mds = new MetacatDataStore(morpho);
    getEntityArray();
    if (entityArray == null) {
      Log.debug(20, "there is no data!");
      return; // there is no data!
    }
    // assume the package has been saved so that location is either LOCAL or METACAT
    for (int i = 0; i < entityArray.length; i++) {
      String urlinfo = getDistributionUrl(i, 0, 0);
      // assumed that urlinfo is of the form 'protocol://systemname/localid/other'
      // protocol is probably 'ecogrid'; system name is 'knb'
      // we just want the local id here
      int indx2 = urlinfo.indexOf("//");
      if (indx2 > -1) {
        urlinfo = urlinfo.substring(indx2 + 2);
        // now start should be just past the '//'
      }
      indx2 = urlinfo.indexOf("/");
      if (indx2 > -1) {
        urlinfo = urlinfo.substring(indx2 + 1);
        //now should be past the system name
      }
      indx2 = urlinfo.indexOf("/");
      if (indx2 > -1) {
        urlinfo = urlinfo.substring(0, indx2);
        // should have trimmed 'other'
      }
      if (urlinfo.length() == 0) {
        continue;
      }
      // if we reach here, urlinfo should be the id in a string
      // now try to get the original filename
      origFileName = getPhysicalName(i, 0);
      if (origFileName.trim().equals("")) {  // original file name missing
        origFileName = urlinfo;
      }
      try {
        if ( (location.equals(LOCAL)) || (location.equals(BOTH))) {
          dataFile = fds.openFile(urlinfo);
        }
        else if (location.equals(METACAT)) {
          dataFile = mds.openFile(urlinfo);
        }
      }
      catch (FileNotFoundException fnf) {
        // if the datfile has NOT been located, a FileNotFoundException will be thrown.
        // this indicates that the datafile with the url has NOT been saved
        // the datafile should be stored in the profile temp dir
        //Log.debug(1, "FileNotFoundException");
        ConfigXML profile = morpho.getProfile();
        String separator = profile.get("separator", 0);
        separator = separator.trim();
        String temp = new String();
        temp = urlinfo.substring(0, urlinfo.indexOf(separator));
        temp += "/" +
            urlinfo.substring(urlinfo.indexOf(separator) + 1, urlinfo.length());
        try {
          dataFile = fds.openTempFile(temp);
        }
        catch (Exception ex) {
          Log.debug(5, "Some problem while writing data files has occurred!");
          ex.printStackTrace();
        }
      }
      catch (Exception q) {
        // some other problem has occured
        Log.debug(5, "Some problem with saving data files has occurred!");
        q.printStackTrace();
      }
      // now copy dataFile
      try{
        String dataPath = path + "/" + urlinfo;
        File saveDatadir = new File(dataPath);
        saveDatadir.mkdirs(); //create the new directory

        String fosname = dataPath + "/" + origFileName;
        FileInputStream fis = new FileInputStream(dataFile);
        FileOutputStream fos = new FileOutputStream(fosname);
        BufferedInputStream bis = new BufferedInputStream(fis);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        int d = bis.read();
        while(d != -1)
        {
          bos.write(d); //write out everything in the reader
          d = bis.read();
        }
        bis.close();
        bos.flush();
        bos.close();
      }
      catch (Exception f) {
        Log.debug(20, "Error exporting data file! (AbstractDataPackage)");
      }

    }
  }



  //save the StringBuffer to the File path specified
  private void saveToFile(StringBuffer buff, File outputFile) throws
      IOException {
    FileWriter fileWriter = new FileWriter(outputFile);
    IOUtil.writeToWriter(buff, fileWriter, true);
  }

  private File[] listFiles(File dir) {
    String[] fileStrings = dir.list();
    int len = fileStrings.length;
    File[] list = new File[len];
    for (int i = 0; i < len; i++) {
      list[i] = new File(dir, fileStrings[i]);
    }
    return list;
  }

  /**
   * Deletes the package from the specified location
   * @param locattion the location of the package that you want to delete
   * use either BOTH, METACAT or LOCAL
   */

  public void delete(String location) throws Exception {
    boolean metacatLoc = false;
    boolean localLoc = false;
    if(location.equals(METACAT) ||
       location.equals(BOTH))
    {
      metacatLoc = true;
    }
    if(location.equals(LOCAL) ||
       location.equals(BOTH))
    {
      localLoc = true;
    }
    String accnum = getAccessionNumber();
    if (localLoc) {
      boolean localSuccess = fileSysDataStore.deleteFile(accnum);
      if (!localSuccess)
      {
        throw new Exception("User couldn't delete the local copy");
      }
      LocalQuery.removeFromCache(accnum);
    }
    if (metacatLoc) {
     boolean success = metacatDataStore.deleteFile(accnum);
     if (!success)
     {
       throw new Exception("User couldn't delete the network copy");
     }
    }
  }

  /**
   *  This method displays a summary of Package information by
   *  calling the various utility methods defined in this class.
   *  Primary use is for debugging.
   */
  public void showPackageSummary() {
    boolean sizelimit = true; // set to false to display all attributes
    StringBuffer sb = new StringBuffer();
    sb.append("Title: " + getTitle() + "\n");
    sb.append("AccessionNumber: " + getAccessionNumber() + "\n");
    sb.append("Author: " + getAuthor() + "\n");
    sb.append("keywords: " + getKeywords() + "\n");
    getEntityArray();
    if (entityArray != null) {
      for (int i = 0; i < entityArray.length; i++) {
        sb.append("   entity " + i + " name: " + getEntityName(i) + "\n");
        sb.append("   entity " + i + " numRecords: " + getEntityNumRecords(i) +
                  "\n");
        sb.append("   entity " + i + " description: " + getEntityDescription(i) +
                  "\n");
        int maxattr = getAttributeArray(i).length;
        if (maxattr > 8) {
          maxattr = 8;
        }
        for (int j = 0; j < maxattr; j++) {
          sb.append("      entity " + i + " attribute " + j + "--- name: " +
                    getAttributeName(i, j) + "\n");
          sb.append("      entity " + i + " attribute " + j + "--- unit: " +
                    getAttributeUnit(i, j) + "\n");
          sb.append("      entity " + i + " attribute " + j + "--- dataType: " +
                    getAttributeDataType(i, j) + "\n");

        }
        for (int k = 0; k < getPhysicalArray(i).length; k++) {
          sb.append("   entity " + i + " physical " + k + "--- name: " +
					getPhysicalName(i, k) + "\n");
					sb.append("   entity " + i + " physical " + k + "--- format: " +
					getPhysicalFormat(i, k) + "\n");
					sb.append("   entity " + i + " physical " + k +
					"--- fieldDelimiter: " + getPhysicalFieldDelimiter(i, k) +
					"\n");
					sb.append("   entity " + i + " physical " + k +
					"--- numHeaderLines: " + getPhysicalNumberHeaderLines(i, k) +
					"\n");
					sb.append("   entity " + i + " physical " + k +
					"------ compression: " + getCompressionMethod(i, k) + "\n");
					sb.append("   entity " + i + " physical " + k + "------ encoding: " +
					getEncodingMethod(i, k) + "\n");
					sb.append("      entity " + i + " physical " + k + "------ inline: " +
					getDistributionInlineData(i, k, 0) + "\n");
					sb.append("      entity " + i + " physical " + k + "------ url: " +
					getDistributionUrl(i, k, 0) + "\n");
				}
			}
		}
		Log.debug(1, sb.toString());
	}

	// methods to implement the XMLFactoryInterface
	public Reader openAsReader(String id) throws DocumentNotFoundException {
		return null;
	}

	public Document openAsDom(String id) {
		// ignore the id and just return the dom for this instance
		return (this.getMetadataNode()).getOwnerDocument();
	}

	// Code Import stuff

	private List toBeImported = null;
	private int toBeImportedCount = 0;

	private List lastImportedAttributes;
	private String lastImportedEntityName;
	private Vector lastImportedDataSet;
	private Entity[] originalEntities = null;


	public int addAttributeForImport(String entityName, String attributeName,
	String scale, OrderedMap omap, String xPath,
	boolean newTable) {
 
		List t = new ArrayList();
		t.add(entityName);
		t.add(attributeName);
		t.add(scale);
		t.add(omap);
		t.add(xPath);
		t.add(new Boolean(newTable));
		if (toBeImported == null) {
			toBeImported = new ArrayList();
			toBeImportedCount = 0;
		}
		toBeImported.add(t);
		toBeImportedCount++;
		Log.debug(10,
		"==========Adding Attr to Import - (" + entityName + ", " + attributeName +
		") ; count = " + toBeImportedCount);
		return toBeImportedCount-1;//return index of the new object in the list
	}


	public String getCurrentImportEntityName() {

		if (toBeImportedCount == 0) {
			return null;
		}
		List t = (List) toBeImported.get(0);
		if (t == null) {
			return null;
		}
		return (String) t.get(0);
	}

	public String getCurrentImportAttributeName() {

		if (toBeImportedCount == 0) {
			return null;
		}
		List t = (List) toBeImported.get(0);
		if (t == null) {
			return null;
		}
		return (String) t.get(1);
	}

	public String getCurrentImportScale() {
		if (toBeImportedCount == 0) {
			return null;
		}
		List t = (List) toBeImported.get(0);
		if (t == null) {
			return null;
		}
		return (String) t.get(2);
	}

	public OrderedMap getCurrentImportMap() {
		if (toBeImportedCount == 0) {
			return null;
		}
		List t = (List) toBeImported.get(0);
		if (t == null) {
			return null;
		}
		return (OrderedMap) t.get(3);
	}

	public OrderedMap getSecondImportMap() {
		if (toBeImportedCount < 2) {
			return null;
		}
		List t = (List) toBeImported.get(1);
		if (t == null) {
			return null;
		}
		return (OrderedMap) t.get(3);
	}

	public String getCurrentImportXPath() {
		if (toBeImportedCount == 0) {
			return null;
		}
		List t = (List) toBeImported.get(0);
		if (t == null) {
			return null;
		}
		return (String) t.get(4);
	}

	public boolean isCurrentImportNewTable() {
		if (toBeImportedCount == 0) {
			return false;
		}
		List t = (List) toBeImported.get(0);
		if (t == null) {
			return false;
		}
		return ( (Boolean) t.get(5)).booleanValue();
	}

	public int getAttributeImportCount() {
		return toBeImportedCount;
	}

	public void removeFirstAttributeForImport() {
		if (toBeImportedCount == 0) {
			return;
		}
		toBeImported.remove(0);
		toBeImportedCount--;
	}
	
	/**
	 * Remove the attribute from the list at the specified index
	 * @param index
	 */
	public void removeLastAttributeForImport() 
	{
		if (toBeImportedCount == 0) 
		{
			Log.debug(15, ""+
					toBeImportedCount +"is 0 and we will remove anything in AbstractDataPackage.removeLastAttributeForImport");
			return;
		}
		int index = toBeImportedCount -1;
		toBeImported.remove(index);
		toBeImportedCount--;
		Log.debug(32, "========Remove the attribute from AttriubteImport list in AbstractDataPackage at index "+index);
	}

	public void setLastImportedEntity(String name) {
		lastImportedEntityName = name;
	}

	public void setLastImportedDataSet(Vector data) {
		lastImportedDataSet = data;
	}

	public void setLastImportedAttributes(List attr) {
		lastImportedAttributes = attr;
	}

	public String getLastImportedEntity() {
		return lastImportedEntityName;
	}

	public List getLastImportedAttributes() {
		return lastImportedAttributes;
	}

	public Vector getLastImportedDataSet() {
		return lastImportedDataSet;
	}

	public Entity[] getOriginalEntityArray() {
		return this.originalEntities;
	}

	public void setOriginalEntityArray(Entity[] arr) {
		this.originalEntities = arr;
	}

	public void clearAllAttributeImports() {

		this.originalEntities = null;
		toBeImported = null;
		toBeImportedCount = 0;
		lastImportedAttributes = null;
		lastImportedEntityName = null;
		lastImportedDataSet = null;

	}
	
	/**
	 * Gets next revision for this doc. It will check revisions in both metacat and local file system.
	 * 1 will be return if no pre-revision exists.
	 * @return  the next revision number
	 */
	public int getNextRevisionNumber(String docid)
	{
		int version = ORIGINAL_REVISION;
		int localNextRevision = getNextRevisionNumberFromLocal(docid);
		int metacatNextRevision = getNextRevisionNumberFromMetacat(docid);
		//choose the bigger one as the next revision
		if (localNextRevision > metacatNextRevision)
		{
			version = localNextRevision;
		}
		else
		{
			version = metacatNextRevision;
		}
		Log.debug(30, "The final next revision is "+version);
		return version;
	}
	
	/*
	 * Gets next revisons from metacat. This method will look at the profile/scope dir and figure
	 * it out the maximum revision. This number will be  increase 1 to get the next revision number.
	 * If local file system doesn't have this docid, 1 will be returned
	 */
	private int getNextRevisionNumberFromLocal(String identifier)
	{
		int version = ORIGINAL_REVISION;
		String dataDir = null;
		if (identifier == null)
		{
			return version;
		}
		//Get Data dir
		// intialize filesystem datastore if it is null
		if (this.fileSysDataStore == null)
		{
			this.fileSysDataStore = new FileSystemDataStore(Morpho.thisStaticInstance);
		}
		
		if (this.fileSysDataStore != null)
		{
		     dataDir = this.fileSysDataStore.getDataDir();
		}
		else if (this.metacatDataStore != null)
		{
			dataDir = this.metacatDataStore.getDataDir();
		}
		
		String targetDocid = getDocIdPart(identifier);
		Log.debug(30, "the data dir is "+dataDir);
		if (dataDir != null && targetDocid != null)
		{
			//Gets scope name
			String scope = null;
			scope = getIdScope(identifier);
			Log.debug(30, "the scope from id is "+scope);
			File scopeFiles = new File(dataDir+File.separator+scope);
			if (scopeFiles.isDirectory())
			{
				File[] list = scopeFiles.listFiles();
				//Finds docid and revision part. The file name in above list will look 
				//like 56.1. We will go through all files and find the maximum revision of
				//same docid
				if (list != null)
				{
					for (int i=0; i<list.length; i++)
					{
						File file = list[i];
						if (file != null)
						{
							String name = file.getName();
							if (name != null)
							{
								int index = name.lastIndexOf(".");
								if (index != -1)
								{
								   String docid = name.substring(0,index);
								   if (docid != null && docid.equals(targetDocid))
								   {
									   String versionStr = name.substring(index+1);
									   if (versionStr != null)
									   {
										   try
										   {
											   int newVersion = (new Integer(versionStr)).intValue();
											   if (newVersion > version)
											   {
												   version = newVersion;
											   }
										   }
										   catch(Exception e)
										   {
											   Log.debug(30, "couldn't find the version part in file name "+name);
										   }
									   }
								   }
								}
							}
							
						}
					}
				}
				
			}
		}
		//if we found a maximum revsion, we should increase 1 to get the next revsion
		if (version != ORIGINAL_REVISION)
		{
			version++;
		}
		Log.debug(30, "The next version for docid "+getPackageId()+ " in local file system is "+version);
		return version;
	}
	
	/*
	 * Get docid part of an identifier. For example, it will return 30 if package id is jones.30.1
	 * It will return the substring from the second last dot to the last dot.
	 * null will return if no docid part found
	 */
	private String getDocIdPart(String identifier)
	{
	  String docid = null;
	  //String identifier = null;
	  /*if (initialId != null)
	  {
	      identifier = initialId;
		  
	  }
	  else
	  {
		  identifier = getPackageId();
	  }*/
	  Log.debug(30, "The identifier which need be got docid part is "+identifier);
	  if (identifier != null)
	  {
		  int index =identifier.lastIndexOf(".");
		  if (index != -1)
		  {
		      docid = identifier.substring(0, index);
		      if (docid != null)
		      {
		    	  index = docid.lastIndexOf(".");
		    	  if (index != -1 )
		    	  {
		    		  docid = docid.substring(index+1);
		    		  
		    	  }
		    	  else
		    	  {
		    		  docid = null;
		    	  }
		      }
		  }
	  }
	  Log.debug(30, "the docid part is "+docid);
	  return docid;
	}
	
	/*
	 * Gets next version from metacat. "getrevisionanddoctype" method of Metacat API will
	 * return the max version of metacat. So we should increase 1 to get next version number.
	 * If couldn't connect metacat or metacat doesn't have this docid, 1 will be returned.
	 */
	private int getNextRevisionNumberFromMetacat(String identifier)
	{
		int version = ORIGINAL_REVISION;
		String semiColon = ";";
		//String docid = null;
		//Gets metacat url from configuration
		if (Morpho.thisStaticInstance != null && Morpho.thisStaticInstance.getNetworkStatus() && identifier != null)
		{
			String metacatURL = Morpho.thisStaticInstance.getMetacatURLString();
			String result = null;
		    Properties lastVersionProp = new Properties();
		    lastVersionProp.put("action", "getrevisionanddoctype");
		    //docid = getPackageId();
		    lastVersionProp.put("docid", identifier);
		    result = Morpho.thisStaticInstance.getMetacatString(lastVersionProp);
		    Log.debug(30, "the result is ============= "+result);
		    // Get version
		    if (result != null)
		    {
		    	int index = result.indexOf(semiColon);
		    	if (index != -1)
		    	{
		    		String versionStr = result.substring(0, index);
		    		try
		    		{
		    			version = (new Integer(versionStr).intValue());
		    			//increase 1 to get next version
		    			version = version +1;
		    		}
		    		catch(Exception e)
		    		{
		    			Log.debug(20, "Couldn't transfer version string "+versionStr +" into integer");
		    		}
		    	}
		    }
		    
		}
		Log.debug(30, "Next version for doicd " +identifier+" in metacat is "+version);
		return version;
	}
	 /**
	   * Gets the status of serializing metadata to local
	   * @return
	   */
	  public boolean getSerializeLocalSuccess()
	  {
		  return serializeLocalSuccess;
	  }
	  
	  /**
	   * Sets the status of serializing metdata to local
	   * @param success
	   */
	  public void setSerializeLocalSuccess(boolean success)
	  {
		  this.serializeLocalSuccess = success;
	  }
	  
	  /**
	   * Gets the status of serializing metadata to metacat
	   * @return
	   */
	  public boolean getSerializeMetacatSuccess()
	  {
		  return this.serializeMetacatSuccess;
	  }
	  
	  /**
	   * Sets the status of serializing metdata to metacat
	   * @param success
	   */
	  public void setSerializeMetacatSuccess(boolean success)
	  {
		  this.serializeMetacatSuccess = success;
	  }
	  
	
	  /**
	   * Gets the value of dataIDChanged
	   * @return
	   */
	  public boolean getDataIDChanged()
	  {
		  return this.dataIDChanged;
	  }
	  
	  /**
	   * Sets the value of dataIDChanged
	   * @param changed
	   */
	  public void setDataIDChanged(boolean changed)
	  {
		  this.dataIDChanged = changed;
	  }
	  
	  /**
	   * Gets the value of packageIDChanged
	   * @return
	   */
	  public boolean getPackageIDChanged()
	  {
		  return this.packageIDChanged;
	  }
	  
	  /**
	   * Sets the value of package id changed.
	   * @param changed
	   */
	  public void setPackageIDChanged(boolean changed)
	  {
		  this.packageIDChanged = changed;
	  }
	  
	  /**
	   * Adds the index of a dirty entity (has unsave change) into the package
	   * @param index   the index of a dirty entity
	   */
	  public void addDirtyEntityIndex(int index)
	  {
		  if (!this.dirtyEntityIndexList.contains(index))
		  {
		    this.dirtyEntityIndexList.add(index);
		  }
	  }
	  
	  /*
	   * Tests if the specified index is a component in this dirtyEntityIndexList vector
	   */
	  private boolean containsDirtyEntityIndex(int index)
	  {
		  return this.dirtyEntityIndexList.contains(index);
	  }
	  
	  /*
	   * Removes the sepcified index from this dirtyEntityIndexList vector
	   */
	  private void removeDirtyEntityIndex(int index)
	  {
		  this.dirtyEntityIndexList.removeElement(index);
	  }
	  
	  /**
	   * Set the auto saved id for this package
	   * @param autoSavingID
	   */
	  public void setAutoSavedID(String autoSavedID)
	  {
		  this.autoSavedID = autoSavedID;
	  }
	  
	  /**
	   * Get the auto saved id for this package
	   * @return
	   */
	  public String getAutoSavedD()
	  {
		  return this.autoSavedID;
	  }
	  
	  /**
	   * Gets attribute column name from a map
	   * @param map
	   * @param xPath
	   * @return
	   */
	  public static String getAttributeColumnName(OrderedMap map, String xPath) {

		    Object o1 = map.get(xPath + "/attributeName");
		    if(o1 == null) return "";
		    else return (String) o1;
		  }
     
	  /**
	   * Gets measurement scale from a given map
	   * @param map
	   * @param xPath
	   * @return
	   */
	 public static String getMeasurementScale(OrderedMap map, String xPath) {

		    Object o1 = map.get(xPath + "/measurementScale/nominal/nonNumericDomain/enumeratedDomain[1]/codeDefinition[1]/code");
		    if(o1 != null) return "Nominal";
		    boolean b1 = map.containsKey(xPath + "/measurementScale/nominal/nonNumericDomain/enumeratedDomain[1]/entityCodeList/entityReference");
		    if(b1) return "Nominal";
		    o1 = map.get(xPath + "/measurementScale/nominal/nonNumericDomain/textDomain[1]/definition");
		    if(o1 != null) return "Nominal";

		    o1 = map.get(xPath + "/measurementScale/ordinal/nonNumericDomain/enumeratedDomain[1]/codeDefinition[1]/code");
		    if(o1 != null) return "Ordinal";
		    b1 = map.containsKey(xPath + "/measurementScale/ordinal/nonNumericDomain/enumeratedDomain[1]/entityCodeList/entityReference");
		    if(b1) return "Ordinal";
		    o1 = map.get(xPath + "/measurementScale/ordinal/nonNumericDomain/textDomain[1]/definition");
		    if(o1 != null) return "Ordinal";

		    o1 = map.get(xPath + "/measurementScale/interval/unit/standardUnit");
		    if(o1 != null) return "Interval";
				o1 = map.get(xPath + "/measurementScale/interval/unit/customUnit");
		    if(o1 != null) return "Interval";
				
		    o1 = map.get(xPath + "/measurementScale/ratio/unit/standardUnit");
		    if(o1 != null) return "Ratio";
				o1 = map.get(xPath + "/measurementScale/ratio/unit/customUnit");
		    if(o1 != null) return "Ratio";

		    o1 = map.get(xPath + "/measurementScale/dateTime/formatString");
		    if(o1 != null) return "Datetime";

		    return "";
		  }
	    
	    /**
	     * Determines if this need a import.
	     * @param map
	     * @param xPath
	     * @param mScale
	     * @return
	     */
		public static boolean isImportNeeded(OrderedMap map, String xPath, String mScale) {
			
			mScale = mScale.toLowerCase();
			if(!(mScale.equals("nominal") || mScale.equals("ordinal"))) return false;
			String path = xPath + "/measurementScale/" + mScale + "/nonNumericDomain/enumeratedDomain[1]/entityCodeList/entityReference";
			boolean present = map.containsKey(path);
			if(!present) return false;
			String o = (String)map.get(path);
			if(o == null || o.trim().equals("")) return true;
			return false;
		}

}

