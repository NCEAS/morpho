/**
 *  '$RCSfile: AbstractDataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-03-27 00:48:24 $'
 * '$Revision: 1.80 $'
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
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;
import edu.ucsb.nceas.morpho.query.LocalQuery;
import edu.ucsb.nceas.morpho.util.IOUtil;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLTransformer;
import edu.ucsb.nceas.utilities.XMLUtilities;

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
import java.util.List;
import java.util.ArrayList;



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
  abstract Node getReferencedNode(Node node);

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
    if (initId!=null) {
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
          return deepClone;
        } else {

          Log.debug(50, "AbstractDataPackage.getSubtree() - index was "
                    +"greater than number of available nodes; returning NULL");
          return null;
        }
      }
    } catch (Exception e) {
      Log.debug(50, "Exception in getSubtree!");
    }
    return null;
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
      Log.debug(50, "Exception in getSSubtreeNoClone!");
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
      IDXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(),
          "/xpathKeyMap/contextNode[@name='package']/"+genericName)).getNodeValue();
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
   * inserts subtree rooted at Node, at location identified by genericName
   * String and int index. Returns root Node of inserted subtree, or null if
   * target location not found, so caller can determine whether insertion was
   * successful
   *
   * @param genericName String
   * @param Node subtree root Node
   * @param index int
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
        for (int i=0;i<insertionList.getLength();i++) {
          Node nd = insertionList.item(i);
          String path = (nd.getFirstChild()).getNodeValue();
          NodeList temp = XMLUtilities.getNodeListWithXPath(metadataNode, path);
          if ((temp!=null)&&(temp.getLength()>0)) {
            Log.debug(40, "found: "+path);
            Node prevNode = temp.item(0);
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
        Log.debug(1, "Error in 'insertSubtree method in AbstractDataPackage");
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
   * @param index int
   * @return root Node of deleted subtree, or null if subtree not found, so
   * caller can determine whether insertion was successful
   */
  public Node deleteSubtree(String genericName, int index) {
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
          Node node = nodelist.item(index);
          Node parnode = node.getParentNode();
          if (parnode==null) return null;
          return parnode.removeChild(node);
        } else {
          return null;
        }
      }
    } catch (Exception e) {
      Log.debug(50, "Exception in deleteSubtree!");
    }
    return null;

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
   *  remove all the geographicNodes
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
    }
    else {
      insertEntity(entity, entityArray.length);
    }
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
        Entity[] newEntArray = new Entity[entityArray.length + 1];
        for (int i = 0; i < pos; i++) {
          newEntArray[i] = entityArray[i];
        }
        newEntArray[pos] = new Entity(newEntityNode, this);
        for (int i = pos + 1; i < entityArray.length; i++) {
          newEntArray[i] = entityArray[i];
        }
        entityArray = newEntArray;
      }
      else { // insert at end of other entities
        Node par1 = ( (entityArray[0]).getNode()).getParentNode();
        par1.appendChild(newEntityNode);
        // now in DOM; need to insert in EntityArray
        Entity[] newEntArray = new Entity[entityArray.length + 1];
        for (int i = 0; i < entityArray.length; i++) {
          newEntArray[i] = entityArray[i];
        }
        newEntArray[entityArray.length] = new Entity(newEntityNode, this);
        entityArray = newEntArray;
      }
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
   */
  public Node[] getAttributeArray(int entityIndex) {

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

  /*
   * This method loops through all the entities in a package and checks for
   * url references to data files (i.e. data external to the data package).
   * Both metatcat and local file stores are checked to see if the data has
   * already been saved. If not, the temp directory is checked. Note that it
   * is assumed that the data file has been assigned an id and stored in the
   * temp directory if it has not been saved to one of the stores
   */
  public void serializeData() throws MetacatUploadException {
    File dataFile = null;
    Morpho morpho = Morpho.thisStaticInstance;
    FileSystemDataStore fds = new FileSystemDataStore(morpho);
    MetacatDataStore mds = new MetacatDataStore(morpho);
    getEntityArray();
    //Log.debug(1, "About to check entityArray!");
    if (entityArray == null) {
      Log.debug(30, "Entity array is null!");
      return; // there is no data!
    }
    for (int i = 0; i < entityArray.length; i++) {
      String protocol = getUrlProtocol(i);
      if(protocol.equals("ecogrid:")) {
        String urlinfo = getUrlInfo(i);
        // urlinfo should be the id in a string
        if (location.equals(LOCAL))  {
          handleLocal(urlinfo);
        }
        else if (location.equals(METACAT)) {
          handleMetacat(urlinfo, i);
        }
        else if (location.equals(BOTH)) {
          handleBoth(urlinfo, i);
        }
      }
    }
  }


  private void handleLocal(String urlinfo) {
    File dataFile = null;
    Morpho morpho = Morpho.thisStaticInstance;
    FileSystemDataStore fds = new FileSystemDataStore(morpho);
    try {
      dataFile = fds.openFile(urlinfo);
    } catch (FileNotFoundException fnf) {
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
        InputStream dfis = new FileInputStream(dataFile);
          //Log.debug(1, "ready to save: urlinfo: "+urlinfo);
          fds.saveDataFile(urlinfo, dfis);
          // the temp file has been saved; thus delete
          dfis.close();
//          dataFile.delete();
      }catch (Exception qq) {
        // some other problem has occured
        Log.debug(5, "Some problem with saving local data files has occurred!");
        qq.printStackTrace();
      }//end catch
    }
  }

  private void handleMetacat(String urlinfo, int entityIndex) {
    File dataFile = null;
    Morpho morpho = Morpho.thisStaticInstance;
    FileSystemDataStore fds = new FileSystemDataStore(morpho);
    MetacatDataStore mds = new MetacatDataStore(morpho);
    try {
      dataFile = mds.openFile(urlinfo);
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
      temp = temp + "/" +
          urlinfo.substring(urlinfo.indexOf(separator) + 1, urlinfo.length());
      try {
        dataFile = fds.openTempFile(temp);
        InputStream dfis = new FileInputStream(dataFile);
        try{
          mds.newDataFile(urlinfo, dataFile);
            // the temp file has been saved; thus delete
          dataFile.delete();
        } catch (MetacatUploadException mue) {
          // if we reach here, most likely there has been a problem saving the datafile
          // on metacat because the id is already in use
          // so, get a new id
          AccessionNumber an = new AccessionNumber(morpho);
          String newid = an.getNextId();
          // now try saving with the new id
          try{
            mds.newDataFile(newid, dataFile);
            dataFile.delete();
            // newDataFile must have worked; thus update the package
            setDistributionUrl(entityIndex, 0, 0, newid);
            String newPackageId = an.getNextId();
            setAccessionNumber(newPackageId);
            serialize(AbstractDataPackage.METACAT);
            if(location.equals(BOTH)) {  // save new package locally
              serialize(AbstractDataPackage.LOCAL);
            }
          } catch (MetacatUploadException mue1) {
            Log.debug(5, "Problem saving data to metacat\n"+
                           mue1.getMessage());
            throw new MetacatUploadException("ERROR SAVING DATA TO METACAT! "
                          +mue1.getMessage());
          }
        }
      }
      catch (Exception qq) {
        // some other problem has occured
        Log.debug(5, "Some problem with saving data files has occurred!");
        qq.printStackTrace();
      }
    }
    catch (Exception ww) {
        // some other problem has occured
        Log.debug(5, "Some other problem with saving data files has occurred!");
        ww.printStackTrace();
    }
  }


  private void handleBoth(String urlinfo, int entityIndex) {
    File dataFile = null;
    Morpho morpho = Morpho.thisStaticInstance;
    FileSystemDataStore fds = new FileSystemDataStore(morpho);
    MetacatDataStore mds = new MetacatDataStore(morpho);
    handleLocal(urlinfo);
    handleMetacat(urlinfo, entityIndex);
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
      InputStream input = this.getClass().getResourceAsStream("/style/export.css");
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

      // for css
      try
      {
        String cssPath = packdir + "/export";
        InputStream input = this.getClass().getResourceAsStream("/style/export.css");
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
      Log.debug(5, "Exception in exporting to zip file (AbstractDataPackage)");
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
        return;
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
        String fosname = path + "/" + origFileName;
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
}

