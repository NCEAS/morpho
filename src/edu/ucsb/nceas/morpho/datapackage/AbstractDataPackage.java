/**
 *  '$RCSfile: AbstractDataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-12-10 18:36:17 $'
 * '$Revision: 1.38 $'
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
 
 
 /*
 ----- A description of how this class is to be used-----
 
 New DataPackage class for Morpho for handling EML2 and other metadata content standards

It is desired to create a new class for representing 'generic' dataPackage objects, with 
EML2 being the immediate goal. It is desired, however, to avoid being too specific so 
that changes in standards can easily be configured without major code re-writes. This
memo attempts to describe the current design.



A simplified class diagram is shown below for discussion purposes.


MetadataObject  <----------------  AbstractDataPackage  <---------- EML200DataPackage
                                                        <---------- NBIIBioDataPackage       <----DataPackageFactory
                                                        <---------- EML2Beta6DataPackage            


The base class is 'MetadataObject'. Basically, this class just a DOM structure containing
 the metadata for a defined schema (the 'schemaGrammer'(i.e. doctype) is also a member 
 variable for the class) . There is also a member called the 'xpathKeyMap'. This is 
 supposed to be a reference to a set of mappings between generic concepts (e.g. the 
 package name) and the specific DOM xpath to the node in the specific DOM that contains 
 the actual concept. This map is stored in a properties file of some type (e.g. an 
 XML file) that is read at run time. Thus, minor changes in a schema can be handled 
 by just updating this properties file rather than changing the code.

Now, the 'AbstractDataPackage' class extends the very general purpose 'MetadataObject' 
class and is meant to be used specifically for representing dataPackages of different 
types. The class is call 'Abstract...' because there are certain actions (like 
'load' and 'serialize' that a specific to the schemaGrammar. Thus the 
'AbstractDataPackage' class is extended by various schema specific classes such 
as the three shown above (i.e. EML200DataPackage, NBIIBioDataPackage, and 
EML2Beta6DataPackage). Note that the xpathKeyMap used is different for each of 
these specific package classes.

Finally, the DataPackageFactory class is used to create a new datapackage object f
rom a supplied DOM or from a docID of a document on metacat or stored locally. 
A factory method is needed so that it can determine just what schema is desired 
and which of the specific package classes should be used to create the object. 
Once created, however, methods in the AbstractDataPackage that are generic can 
be used to get information stored in the package.
            

xpathKeyMap
	Consider now how the xpathKeyMap works. An example in XML format for eml200 
  is reproduced below. It should be noted that this example is organized as 
  a set of 'contextNode' elements. The 'package' contextNode corresponds to 
  the root of datapackage DOM while other contextNodes, like 'entity' refer 
  to some node in the dom other than the root. The contextNode serves as the 
  point of departure for XPath searches. The concept allows for relative 
  searche - e.g. one can give paths relative to the entity context node.

	An example of xpathKeyMap use is the problem of finding the "accessionNumber" 
  for a generic metadata schema. The document below has an 'accessionNumber' 
  element under the 'package' contextNode. It's value for eml2 is seen to be 
  '/eml:eml/@packageId'. ONe first looks up this value in the xpathKeyMap and 
  then applies the xpath to the eml2 dataPackage dom. We have thus added a level 
  of indirection where specific paths are looked up in the xpathKeyMap using 
  generic path names.

	As another example, one would look at the 'name' element under the 'entity' 
  contextNode to get an entity name. In this case the relative path is simply 
  'entityName'. But how does one get the actual entity contextNode where the 
  relative path starts? In this example, the higher level 'entities' element 
  under the package contextNode is an xpath that will return a NodeSet of 
  entity nodes in the eml2 dom. Each of these nodes is a starting point for the 
  entity information (i.e. the root of the entity subtree).


<?xml version="1.0"?>  
<xpathKeyMap schemaGrammar="eml2.0.0">
<!-- element name is key, element value is Xpath for this grammar -->
  <contextNode name="package"> 
    <entities>/eml:eml/dataset/dataTable</entities> 
    <title>/eml:eml/dataset/title</title>
    <author>/eml:eml/dataset/creator/individualName/surName</author>
    <accessionNumber>/eml:eml/@packageId</accessionNumber>
    <keywords>/eml:eml/dataset/keywordSet/keyword</keywords>    
  </contextNode>
  <!-- Xpaths for entity values are defined as relative to top node of entity -->
  <contextNode name="entity"> 
    <name>entityName</name>
    <numRecords>numberOfRecords</numRecords>
    <entityDescription>entityDescription</entityDescription >
    <physical>physical</physical>
    <attributes>attributeList/attribute</attributes>
  </contextNode>
  <contextNode name="attribute"> 
    <name>attributeName</name>
    <dataType>storageType</dataType>
    <isText>count(measurementScale/nominal|measurementScale/ordinal)!=0</isText>
    <isDate>count(measurementScale/datetime)!=0</isDate>
  </contextNode>
  <contextNode name="physical"> 
    <name>objectName</name>
    <fieldDelimiter>dataFormat/textFormat/simpleDelimited/fieldDelimiter</fieldDelimiter>
    <numberHeaderLines>dataFormat/textFormat/numHeaderLines</numberHeaderLines>
    <size>size</size>
    <format>dataFormat/externallyDefinedFormat/formatName</format>
    <isText>count(dataFormat/textFormat)!=0</isText>  
    <distribution>distribution</distribution>
  </contextNode>
  <contextNode name="distribution">
    <isOnline>count(online/url)!=0</isOnline>
    <url>online/url</url>
    <isInline>count(inline)!=0</isInline>
    <inline>inline</inline>
  </contextNode>
</xpathKeyMap>
 
 */

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.IOUtil;
import edu.ucsb.nceas.morpho.util.XMLTransformer;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;


import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.*;
import org.apache.xpath.NodeSet;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.apache.xpath.objects.XObject;

import edu.ucsb.nceas.utilities.*;

import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.io.*;
import java.util.Vector;
import javax.swing.*;
/**
 * class that represents a data package. This class is abstract. Specific datapackages
 * e.g. eml2, beta6., etc extend this abstact class
 *
 * actually, only the load and serialize methods are abstract.
 * A number of other concrete utlity methods are included in this class.
 * Note that this class extends the MetadataObject class. The essense of
 * this class is thus the DOM metadataNode (which contains references to all the Nodes
 * in the DOM and the metadataPathNode. The metadataPathNode references an XML structure
 * which maps generic DataPackage information to specific paths for the grammar being
 * considered. Thus, handling changes in the grammar of eml2, for example, should just 
 * require one to create a new map from generic nodes to the new specific ones. 
 */
public abstract class AbstractDataPackage extends MetadataObject
                                          implements XMLFactoryInterface
{
  protected String location = "";
  protected String id;
  protected ConfigXML config;
  protected File dataPkgFile;
  protected FileSystemDataStore fileSysDataStore;
  protected MetacatDataStore  metacatDataStore;
  
  protected Entity[] entityArray; 
  
  private final String              HTMLEXTENSION = ".html";
  private final String              METADATAHTML = "metadata";
  private final String CONFIG_KEY_STYLESHEET_LOCATION = "stylesheetLocation";
  private final String CONFIG_KEY_MCONFJAR_LOC   = "morphoConfigJarLocation";
  private final String EXPORTSYLE ="export";
  private final String EXPORTSYLEEXTENSION =".css";


  /**
   *  This abstract method turns the datapackage into a form (e.g. string) that can
   *  be saved in the file system or metacat. Actual implementation is done in classes
   *  specific to grammar
   */
  abstract public void serialize(String location);
  
  /**
   *  This abstract method loads a datapackage from metacat or the local file
   *  system based on an identifier. Basic action is to create a DOM and assign it
   *  to the underlying MetadataObject. Actual implementation is done in classes
   *  specific to grammar
   */
  abstract public void load(String location, String identifier, Morpho morpho);
  
  /**
   *   Copies the AbstractDataPackage with the indicated id from the local
   *   file store to Metacat
   */
  abstract public AbstractDataPackage upload(String id) throws MetacatUploadException;
  
  /**
   *   Copies the AbstractDataPackage with the indicated id from metacat
   *   to the local file store
   */
  abstract public AbstractDataPackage download(String id);
  
  /**
   * used to signify that this package is located on a metacat server
   */
  public static final String METACAT  = "metacat";
  /**
   * used to signify that this package is located locally
   */
  public static final String LOCAL    = "local";
  /**
   * used to signify that this package is stored on metacat and locally.
   */
  public static final String BOTH     = "localmetacat";

    // util to read the file from either FileSystemDataStore or MetacatDataStore
  protected File getFileWithID(String ID, Morpho morpho) throws Throwable {
    
    File returnFile = null;
    if(location.equals(METACAT)) {
      try {
        Log.debug(11, "opening metacat file");
        if (metacatDataStore==null) {
          metacatDataStore = new MetacatDataStore(morpho);
        }
        dataPkgFile = metacatDataStore.openFile(ID);
        Log.debug(11, "metacat file opened");
      
      } catch(FileNotFoundException fnfe) {

        Log.debug(0,"Error in DataPackage.getFileFromDataStore(): "
                                +"metacat file not found: "+fnfe.getMessage());
        fnfe.printStackTrace();
        throw fnfe.fillInStackTrace();

      } catch(CacheAccessException cae) {
    
        Log.debug(0,"Error in DataPackage.getFileFromDataStore(): "
                                +"metacat cache problem: "+cae.getMessage());
        cae.printStackTrace();
        throw cae.fillInStackTrace();
      }
    } else {  //not metacat
      try {
        Log.debug(11, "opening local file");
        if (fileSysDataStore==null) {
          fileSysDataStore = new FileSystemDataStore(morpho);
        }
        dataPkgFile = fileSysDataStore.openFile(ID);
        Log.debug(11, "local file opened");
      
      } catch(FileNotFoundException fnfe) {
    
        Log.debug(0,"Error in DataPackage.getFileFromDataStore(): "
                                +"local file not found: "+fnfe.getMessage());
        fnfe.printStackTrace();
        throw fnfe.fillInStackTrace();
      }
    }
    return dataPkgFile;  
  }

 /**
  *  Method to return the location
  */
  public String getLocation() {
    return location;
  }

 /**
  *  Method to set the location
  */
  public void setLocation(String location) {
    this.location = location;
  }

 /**
   *  convenience method to get the DataPackage title
   */
  public String getTitle() {
    String temp = getGenericValue("/xpathKeyMap/contextNode[@name='package']/title");
    return temp;
  }
  /**
   *  convenience method to get the DataPackage author
   *  May be overridden for specific package types to give better response
   *  (e.g. in eml2, folds together several elements and authors)
   */
  public String getAuthor() {
    String temp = "";
    temp = getGenericValue("/xpathKeyMap/contextNode[@name='package']/author");    
    return temp;
  }
  
  /**
   *  convenience method to retrieve accession number from DOM
   */
  public String getAccessionNumber() {
    String temp = "";
    temp = getGenericValue("/xpathKeyMap/contextNode[@name='package']/accessionNumber");    
    return temp;
  }
  
  /**
   *  convenience method to retrieve packageID from DOM
   *  synonym for getAccessionNumber
   */
  public String getPackageId() {
    String temp = "";
    temp = getAccessionNumber();    
    return temp;
  }
  
    /**
   *  convenience method to set accession number from DOM
   */
  public void setAccessionNumber(String id) {
    setGenericValue("/xpathKeyMap/contextNode[@name='package']/accessionNumber", id);    
  }

  
  /**
   *  convenience method for getting package keywords
   */
  public String getKeywords() {
    String temp = "";
    NodeList keywordsNodes = null;
    String keywordsXpath = "";
    try {
      keywordsXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='package']/keywords")).getNodeValue();
      keywordsNodes = XMLUtilities.getNodeListWithXPath(metadataNode, keywordsXpath);
      if (keywordsNodes==null) return "";
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting keyword");
    }
    int numKeywords = keywordsNodes.getLength();
    String kw = "";
    for (int i=1;i<numKeywords+1;i++) {
      kw = getXPathValue("("+keywordsXpath +")["+i+"]");
      if (temp.length()>0) temp = temp + ", ";
      temp = temp + kw;
    }
    return temp;
  }
  
  /*
   *  This method finds all the entities in the package and builds an array of 
   *  'entity' nodes in the package dom. One could create an 'Entity' class descending from
   *  Metadata object, but this offers no obvious advantage over simply saving this node array
   *  as one of the members of AbstractDataPackage
   */
  public Entity[] getEntityArray() {
    if (entityArray!=null) return entityArray;
    String entityXpath = "";
    try{
      entityXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='package']/entities")).getNodeValue();
      
      NodeList entityNodes = XMLUtilities.getNodeListWithXPath(metadataNode,entityXpath);
      //  NodeList entityNodes = XPathAPI.selectNodeList(metadataNode,entityXpath);
      if (entityNodes==null) {
        Log.debug(20,"entityList is null!");
        entityArray = null;
      } else {       
        Node[] entityArrayNodes = XMLUtilities.getNodeListAsNodeArray(entityNodes);
        entityArray = new Entity[entityArrayNodes.length];
        for (int i=0;i<entityArrayNodes.length;i++) {
          entityArray[i] = new Entity(entityArrayNodes[i], this);
        }
      }
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting entityArray");
      return null;
    }
    return entityArray;
  }

  /**
   *  This method retrieves entityName information, given the index of the entity
   *  in the entityNode array
   */
  public String getEntityName(int entNum) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entNum)+1)) {
      return "No such entity!";
    }
    Node entity = (entityArray[entNum]).getNode();
    String entityNameXpath = "";
    try {
      entityNameXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/name")).getNodeValue();
      NodeList enameNodes = XPathAPI.selectNodeList(entity, entityNameXpath);
      if (enameNodes==null) return "enameNodes is null !";
      Node child = enameNodes.item(0).getFirstChild();
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting entity name"+w.toString());
    }
    return temp;
  }

  /**
   *  This method retrieves the number of records in thr entity,
   *  given the index of the entity in the entityNode array
   */
  public String getEntityNumRecords(int entNum) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entNum)+1)) {
      return "No such entity!";
    }
    Node entity = (entityArray[entNum]).getNode();
    String entityNumRecordsXpath = "";
    try {
      entityNumRecordsXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/numRecords")).getNodeValue();
      NodeList eNodes = XPathAPI.selectNodeList(entity, entityNumRecordsXpath);
      if (eNodes==null) return "eNodes is null !";
      Node child = eNodes.item(0).getFirstChild();
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting entity numRecords"+w.toString());
    }
    return temp;
  }
  
  /**
   *  This method sets the number of records in the entity,
   *  given the index of the entity in the entityNode array
   */
  public void setEntityNumRecords(int entNum, String numRecS) {
    if ((entityArray==null)||(entityArray.length<(entNum)+1)) {
      Log.debug(20, "No such entity!");
      return;
    }
    Node entity = (entityArray[entNum]).getNode();
    String entityNumRecordsXpath = "";
    try {
      entityNumRecordsXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/numRecords")).getNodeValue();
      NodeList eNodes = XPathAPI.selectNodeList(entity, entityNumRecordsXpath);
      if (eNodes==null) return;
      Node child = eNodes.item(0).getFirstChild();
      child.setNodeValue(numRecS);
    }
    catch (Exception w) {
      Log.debug(50,"exception in setting entity numRecords"+w.toString());
    }
  }

  /**
   *  This method retrieves the entity Description,
   *  given the index of the entity in the entityNode array
   */
  public String getEntityDescription(int entNum) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entNum)+1)) {
      return "No such entity!";
    }
    Node entity = (entityArray[entNum]).getNode();
    String entityXpath = "";
    try {
      entityXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/entityDescription")).getNodeValue();
      NodeList eNodes = XPathAPI.selectNodeList(entity, entityXpath);
      if (eNodes==null) return "eNodes is null !";
      Node child = eNodes.item(0).getFirstChild();
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting entity description"+w.toString());
    }
    return temp;
  }
  
  /**
   *  This method deletes the indexed entity from the DOM
   */
  public void deleteEntity(int entNum) {
    if ((entityArray==null)||(entityArray.length<(entNum)+1)) {
      Log.debug(20, "Unable to find entity at index");
      return;
    }
    Node entity = (entityArray[entNum]).getNode();
    Node parent = entity.getParentNode();
    parent.removeChild(entity);	  
  }

  
  /**
   *  This method automatically adds an entity in the DOM at the next 
   *  available position
   */
  public void addEntity(Entity entity) {

    if (entityArray==null) { 
    
      insertEntity(entity, 0);
    
    } else {
    
      insertEntity(entity, entityArray.length); 
    }
  }  
  
  /**
   *  This method inserts an entity in the DOM at the indexed position
   */
  public void insertEntity(Entity entity, int pos) {
    Document thisDom = getMetadataNode().getOwnerDocument();
    Node newEntityNode = thisDom.importNode(entity.getNode(), true); // 'true' imports children
    // now have to figure out where to insert this cloned node and its children
    // First consider case where there are other entities
    if ((entityArray!=null)&&(entityArray.length>0)) {
      if (entityArray.length>pos) {
				Node par = ((entityArray[pos]).getNode()).getParentNode();
				par.insertBefore(newEntityNode,(entityArray[pos]).getNode());
				// now in DOM; need to insert in EntityArray
			  Entity[] newEntArray = new Entity[entityArray.length + 1];
				for (int i=0; i<pos; i++) {
					newEntArray[i] = entityArray[i];
				}
				newEntArray[pos] = new Entity(newEntityNode, this);
				for (int i=pos+1; i<entityArray.length; i++) {
					newEntArray[i] = entityArray[i];
				}
			  entityArray = newEntArray;
			}
			else {  // insert at end of other entities
				Node par1 = ((entityArray[0]).getNode()).getParentNode();
				par1.appendChild(newEntityNode);
				// now in DOM; need to insert in EntityArray
			  Entity[] newEntArray = new Entity[entityArray.length + 1];
				for (int i=0; i<entityArray.length; i++) {
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
        temp = getGenericValue("/xpathKeyMap/contextNode[@name='package']/entityParent"); 
			  entityPar = XMLUtilities.getNodeWithXPath(getMetadataNode(), temp);
			} catch (Exception w) {
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
    if(entityIndex>(entityArray.length-1)){
      Log.debug(1, "entity index > number of entities");
      return null;
    }
    String attributeXpath = "";
    try{
      attributeXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/attributes")).getNodeValue();
      NodeList attributeNodes = XMLUtilities.getNodeListWithXPath((entityArray[entityIndex]).getNode(),attributeXpath);
      if (attributeNodes==null) {
        Log.debug(1,"attributeList is null!");
        return null;
      }
//      Node[] attr = XMLUtilities.getNodeListAsNodeArray(attributeNodes);
      return XMLUtilities.getNodeListAsNodeArray(attributeNodes);      
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting attributeArray");
    }
    return null;
  }
	
	/**
	 *  This method deletes the indexed attribute from the indexed
	 *  entity
	 */
	public void deleteAttribute(int entityIndex, int attributeIndex) {
	  if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      Log.debug(20, "No such entity!");
			return;
    }
		Node[] attributes = getAttributeArray(entityIndex);
    if ((attributes==null)||(attributes.length<1)) {
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
	  if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      Log.debug(20, "No such entity!");
			return;
    }
    Document thisDom = getMetadataNode().getOwnerDocument();
    Node newAttributeNode = thisDom.importNode(newAttrNode, true); // 'true' imports children
		Node[] attributes = getAttributeArray(entityIndex);
    if ((attributes==null)||(attributes.length<1)) {
      // currently there are NO attributes, so ignore attrIndex
			// and just insert
			Node attributeParent = null;
      String temp = "";
			try {
        temp = getGenericValue("/xpathKeyMap/contextNode[@name='entity']/attributeParent"); 
			  attributeParent = XMLUtilities.getNodeWithXPath((entityArray[entityIndex]).getNode(), temp);
			} catch (Exception w) {
				Log.debug(20, "Error adding new attribute!");
			  return;
			}
			if (attributeParent!=null) {
			  attributeParent.appendChild(newAttributeNode);
			  //Note: attribute array is dynamically generated, so we don't need to update it here
			} else {
				// parent node does not exist in the current dom!!
				// temp has a path in string form with nodes separated by '/'s
				// assume that entity node DOES exist
	      Log.debug(1,"Problem: no attribute parent !!");				
		  }
			return;
	  }
		
		// there are current attributes, so must insert in proper location
    Node currentAttr = null;
    if (attrIndex>attributes.length-1) {
		  currentAttr = attributes[attributes.length-1];
    } else {
		  currentAttr = attributes[attrIndex];
    }
		if (attrIndex<(attributes.length-1)) {
			Node parent = currentAttr.getParentNode();
			parent.insertBefore(newAttributeNode, currentAttr);
		} else {
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
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      return "No such entity!";
    }
    Node[] attributes = getAttributeArray(entityIndex);
    if ((attributes==null)||(attributes.length<1)) return "no attributes!";
    Node attribute = attributes[attributeIndex];
    String attrXpath = "";
    try {
      attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='attribute']/name")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(attribute, attrXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting entity description"+w.toString());
    }
    return temp;
  }
  
  /*
   *  This method retreives the attribute datatype at attributeIndex for
   *  the given entityIndex. i.e. getAttributeDataType(0,1) would return
   *  the first attribute datatype for the zeroth entity (indices are 0 based)
   */
  public String getAttributeDataType(int entityIndex, int attributeIndex) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      return "No such entity!";
    }
    Node[] attributes = getAttributeArray(entityIndex);
    if ((attributes==null)||(attributes.length<1)) return "no attributes!";
    Node attribute = attributes[attributeIndex];
    String attrXpath = "";
    try {
      // first see if there is a datatype node
      attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='attribute']/dataType")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(attribute, attrXpath);
      if (aNodes.getLength()>0) {
        Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
        temp = child.getNodeValue();
        return temp;
      }
      
      // see if datatype is text
      attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='attribute']/isText")).getNodeValue();
      XObject xobj = XPathAPI.eval(attribute, attrXpath);
      boolean val = xobj.bool();
      if (val){
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
          "/xpathKeyMap/contextNode[@name='attribute']/numberType")).getNodeValue();
      
      aNodes = XPathAPI.selectNodeList(attribute, attrXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting attribute dataType"+w.toString());
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
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      return "No such entity!";
    }
    Node[] attributes = getAttributeArray(entityIndex);
    if ((attributes==null)||(attributes.length<1)) return "no attributes!";
    Node attribute = attributes[attributeIndex];
    String attrXpath = "";
    try {
      attrXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='attribute']/unit")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(attribute, attrXpath);
      if (aNodes==null) return "aNodes is null !";
      if (aNodes.getLength()<1) return "";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue().trim();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting attribute unit -- "+w.toString());
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
    if(entityIndex>(entityArray.length-1)){
      Log.debug(1, "entity index > number of entities");
      return null;
    }
    String physicalXpath = "";
    try{
      physicalXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/physical")).getNodeValue();
      NodeList physicalNodes = XMLUtilities.getNodeListWithXPath((entityArray[entityIndex]).getNode(),physicalXpath);
      if (physicalNodes==null) {
        Log.debug(1,"physicalList is null!");
        return null;
      }
      return XMLUtilities.getNodeListAsNodeArray(physicalNodes);      
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting physicalArray");
    }
    return null;
  }

  /**
   *  This method returns the name of indexed physical object for the
   *  indicated entity index.
   */
  public String getPhysicalName(int entityIndex, int physicalIndex) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ((physicals==null)||(physicals.length<1)) return "no physicals!";
    if (physicalIndex>(physicals.length-1)) return "physical index too large!";
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/name")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting physical objectName description"+w.toString());
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
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ((physicals==null)||(physicals.length<1)) return "no physicals!";
    if (physicalIndex>(physicals.length-1)) return "physical index too large!";
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      // first see if the format is text
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/isText")).getNodeValue();
//      XMLUtilities.xPathEvalTypeTest(physical, physXpath);

      XObject xobj = XPathAPI.eval(physical, physXpath);
      if (xobj==null) Log.debug(1,"null");

      boolean val = xobj.bool();
      if (val){
        return "text";
      } 
      // not text, try another xpath
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/format")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting physical format description --- "+w.toString());
    }
    return temp;
  }

  /**
   *  This method returns the size for the indexed entity and
   *  physical object. 
   */
  public String getPhysicalSize(int entityIndex, int physicalIndex) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ((physicals==null)||(physicals.length<1)) return "no physicals!";
    if (physicalIndex>(physicals.length-1)) return "physical index too large!";
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/size")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting physical size"+w.toString());
    }
    return temp;
  }

    /**
   *  This method sets the size for the indexed entity and
   *  physical object. 
   */
  public void setPhysicalSize(int entityIndex, int physicalIndex, String sizeS) {
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      Log.debug(20, "No such entity!");
      return;
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ((physicals==null)||(physicals.length<1)) return;
    if (physicalIndex>(physicals.length-1)) return;
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/size")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes==null) return;
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      child.setNodeValue(sizeS);
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting physical size"+w.toString());
    }
  }

  
  /**
   *  This method returns the FieldDelimiter for the indexed entity and
   *  physical object. An empty string is returned when there is no
   *  meaningful fieldDelimiter (e.g. not a text format)
   */
  public String getPhysicalFieldDelimiter(int entityIndex, int physicalIndex) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ((physicals==null)||(physicals.length<1)) return "no physicals!";
    if (physicalIndex>(physicals.length-1)) return "physical index too large!";
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/fieldDelimiter")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting physical field delimiter"+w.toString());
    }
    return temp;
  }
  
  /**
   *  This method sets the FieldDelimiter for the indexed entity and
   *  physical object. 
   */
  public void setPhysicalFieldDelimiter(int entityIndex, int physicalIndex, String delim) {
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      Log.debug(20, "No such entity!");
      return;
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ((physicals==null)||(physicals.length<1)) return;
    if (physicalIndex>(physicals.length-1)) return;
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/fieldDelimiter")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes==null) return;
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      child.setNodeValue(delim);
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting physical field delimiter"+w.toString());
    }
  }

  /**
   *  This method returns the number of header lines for the indexed entity and
   *  physical object. An empty string is returned when there is no
   *  meaningful numHeaderLines (e.g. not a text format)
   */
  public String getPhysicalNumberHeaderLines(int entityIndex, int physicalIndex) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ((physicals==null)||(physicals.length<1)) return "no physicals!";
    if (physicalIndex>(physicals.length-1)) return "physical index too large!";
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/numberHeaderLines")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting physical number HeaderLines"+w.toString());
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
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ((physicals==null)||(physicals.length<1)) return "no physicals!";
    if (physicalIndex>(physicals.length-1)) return "physical index too large!";
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/encodingMethod")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting physical encodingMethod"+w.toString());
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
    if ((entityArray==null)||(entityArray.length<(entityIndex)+1)) {
      return "No such entity!";
    }
    Node[] physicals = getPhysicalArray(entityIndex);
    if ((physicals==null)||(physicals.length<1)) return "no physicals!";
    if (physicalIndex>(physicals.length-1)) return "physical index too large!";
    Node physical = physicals[physicalIndex];
    String physXpath = "";
    try {
      physXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/compressionMethod")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(physical, physXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting physical compressionMethod"+w.toString());
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
    if (physNodes==null) return null;
    if(physicalIndex>(physNodes.length-1)){
      Log.debug(1, "physical index > number of physical objects");
      return null;
    }
    String distributionXpath = "";
    try{
      distributionXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='physical']/distribution")).getNodeValue();
      NodeList distributionlNodes = XMLUtilities.getNodeListWithXPath(physNodes[physicalIndex],distributionXpath);
      if (distributionlNodes==null) {
        Log.debug(1,"distributionList is null!");
        return null;
      }
      return XMLUtilities.getNodeListAsNodeArray(distributionlNodes);      
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting distributionArray");
    }
    return null;
  }
  
  /**
   *  This method returns 'inline' data as a String for the indexed entity,
   *  physical object, and distribution object. Usually one would try to avoid
   *  large inline data collections because it will make DOMs hard to handle
   */
  public String getDistributionInlineData(int entityIndex, int physicalIndex, int distIndex) {
    String temp = "";
    Node[] distNodes = getDistributionArray(entityIndex, physicalIndex);
    if (distIndex>distNodes.length-1) return temp;
    Node distNode = distNodes[distIndex];
    String distXpath = "";
    try {
      distXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='distribution']/inline")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(distNode, distXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue().trim();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting distribution inline data: "+w.toString());
    }
    return temp;
  }

  /**
   *  This method returns the url for data as a String for the indexed entity,
   *  physical object, and distribution object. Returns an empty string if there
   *  is no url pointing to the data, or data is not referenced.
   */  
  public String getDistributionUrl(int entityIndex, int physicalIndex, int distIndex) {
    String temp = "";
    Node[] distNodes = getDistributionArray(entityIndex, physicalIndex);
    if (distIndex>distNodes.length-1) return temp;
    Node distNode = distNodes[distIndex];
    String distXpath = "";
    try {
      distXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='distribution']/url")).getNodeValue();
      NodeList aNodes = XPathAPI.selectNodeList(distNode, distXpath);
      if (aNodes==null) return "aNodes is null !";
      Node child = aNodes.item(0).getFirstChild();  // get first ?; (only 1?)
      temp = child.getNodeValue().trim();
    }
    catch (Exception w) {
      Log.debug(50,"exception in getting distribution url: "+w.toString());
    }
    return temp;
  }
  
  /*
   * This method loops through all the entities in a package and checks for
   * url references to data files (i.e. data external to the data package).
   * Both metatcat and local file stores are checked to see if the data has
   * already been saved. If not, the temp directory is checked. Note that it
   * is assumed that the data file has been assigned an id and stored in the
   * temp directory if it has not been saved to one of the stores
   */
  public void serializeData() {
    File dataFile = null;
    Morpho morpho = Morpho.thisStaticInstance;
    FileSystemDataStore fds = new FileSystemDataStore(morpho);
    MetacatDataStore mds = new MetacatDataStore(morpho); 
//Log.debug(1, "About to check entityArray!");        
    if (entityArray==null) return;  // there is no data!
    for (int i=0;i<entityArray.length;i++) {
      String urlinfo = getDistributionUrl(i, 0,0);
      // assumed that urlinfo is of the form 'protocol://systemname/localid/other'
      // protocol is probably 'ecogrid'; system name is 'knb'
      // we just want the local id here
      int indx2 = urlinfo.indexOf("//");
      if (indx2>-1) urlinfo = urlinfo.substring(indx2+2);
      // now start should be just past the '//'
      indx2 = urlinfo.indexOf("/");
      if (indx2>-1) urlinfo = urlinfo.substring(indx2+1);
      //now should be past the system name
      indx2 = urlinfo.indexOf("/");
      if (indx2>-1) urlinfo = urlinfo.substring(0,indx2);
      // should have trimmed 'other'
      if (urlinfo.length()==0) return;
      // if we reach here, urlinfo should be the id in a string
      try{ 
        if ((location.equals(LOCAL))||(location.equals(BOTH))) {
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
        temp += "/" + urlinfo.substring(urlinfo.indexOf(separator) + 1, urlinfo.length());
        try{ 
          dataFile = fds.openTempFile(temp); 
          InputStream dfis = new FileInputStream(dataFile);   
          if ((location.equals(LOCAL))||(location.equals(BOTH))) {
//Log.debug(1, "ready to save: urlinfo: "+urlinfo);
            fds.saveDataFile(urlinfo, dfis);
            // the temp file has been saved; thus delete
            dfis.close();
            dataFile.delete();
          }
          else if ((location.equals(METACAT))||(location.equals(BOTH))) {
            mds.newDataFile(temp, dataFile);
            // the temp file has been saved; thus delete
            dataFile.delete();
           }
        }
        catch (Exception ex) {
          Log.debug(5,"Some problem while writing data files has occurred!");
        }
      }
      catch (Exception q) {
        // some other problem has occured
        Log.debug(5,"Some problem with saving data files has occurred!");
      }
    }
  }
  
  /**
   * exports a package to a given path
   * @param path the path to which this package should be exported.
   */
  public void export(String path)
  {
    Log.debug(20, "exporting...");
    Log.debug(20, "path: " + path);
    Log.debug(20, "id: " + id);
    Log.debug(20, "location: " + location);
    File f = null;
    Vector fileV = new Vector(); //vector of all files in the package
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(LOCAL))
    {
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
    String dataPath = packagePath + "/data";
    File savedir = new File(packagePath);
    File savedirSub = new File(sourcePath);
    File savedirDataSub = new File(dataPath);
    savedir.mkdirs(); //create the new directories
    savedirSub.mkdirs();
    StringBuffer[] htmldoc = new StringBuffer[2]; //DFH
    
    // for metadata file
    f = new File(sourcePath + "/" + id);

    File openfile = null;
    try{
      if(localloc) { //get the file locally and save it
        openfile = fileSysDataStore.openFile(id);
      }
      else if(metacatloc) { //get the file from metacat
          openfile = metacatDataStore.openFile(id);
      }
      FileInputStream fis = new FileInputStream(openfile);
      BufferedInputStream bfis = new BufferedInputStream(fis);
      FileOutputStream fos = new FileOutputStream(f);
      BufferedOutputStream bfos = new BufferedOutputStream(fos);
      int c = bfis.read();
      while(c != -1) { //copy the files to the source directory
        bfos.write(c);
        c = bfis.read();
      }
      bfos.flush();
      bfis.close();
      bfos.close();

      // for html
      Reader        xmlInputReader  = null;
      Reader        result          = null;
      StringBuffer  tempPathBuff    = new StringBuffer();
      xmlInputReader = new FileReader(openfile);
            
      XMLTransformer transformer = XMLTransformer.getInstance();
      // add some property for style sheet
      transformer.removeAllTransformerProperties();
      transformer.addTransformerProperty(
                    XMLTransformer.HREF_PATH_EXTENSION_XSLPROP, HTMLEXTENSION);
      transformer.addTransformerProperty(
                    XMLTransformer.PACKAGE_ID_XSLPROP,          id);
      transformer.addTransformerProperty(
                    XMLTransformer.PACKAGE_INDEX_NAME_XSLPROP,  METADATAHTML);
      transformer.addTransformerProperty(
                    XMLTransformer.DEFAULT_CSS_XSLPROP,         EXPORTSYLE);
      transformer.addTransformerProperty(
                    XMLTransformer.ENTITY_CSS_XSLPROP,          EXPORTSYLE);
      transformer.addTransformerProperty(
                    XMLTransformer.CSS_PATH_XSLPROP,            ".");
      try {
        result = transformer.transform(xmlInputReader);
      } catch (IOException e) {
        e.printStackTrace();
        Log.debug(9,"Unexpected Error Styling Document: "+e.getMessage());
        e.fillInStackTrace();
        throw e;
      } finally {
          xmlInputReader.close();
      }
      transformer.removeAllTransformerProperties();
            
      try {
        htmldoc[0] = IOUtil.getAsStringBuffer(result, true); 
        //"true" closes Reader after reading
      } catch (IOException e) {
        e.printStackTrace();
        Log.debug(9,"Unexpected Error Reading Styled Document: "
                                                   +e.getMessage());
        e.fillInStackTrace();
        throw e;
      }
      
        tempPathBuff.delete(0,tempPathBuff.length());
        
        tempPathBuff.append(packagePath);
        tempPathBuff.append("/");
        tempPathBuff.append(METADATAHTML);
        tempPathBuff.append(HTMLEXTENSION);
        saveToFile(htmldoc[0], new File(tempPathBuff.toString()));

    } catch (Exception w) {
        w.printStackTrace();
        Log.debug(9,"Unexpected Error Reading Styled Document: "
                                                   +w.getMessage());
    }
    
    JOptionPane.showMessageDialog(null,
                    "Package export is complete ! ");
  }

    /**
   * Exports a package to a zip file at the given path
   * @param path the path to export the zip file to
   */
  public void exportToZip(String path)
  {
    try
    {
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
      for(int i=0; i<dirlist.length; i++)
      {
        String entry = temppackdir + "/" + dirlist[i];
        ZipEntry ze = new ZipEntry(packdir + "/" + dirlist[i]);
        File entryFile = new File(entry);
        if(!entryFile.isDirectory())
        {
          ze.setSize(entryFile.length());
          zos.putNextEntry(ze);
          FileInputStream fis = new FileInputStream(entryFile);
          int c = fis.read();
          while(c != -1)
          {
            zos.write(c);
            c = fis.read();
          }
          zos.closeEntry();
        }
      }
      // for data file
      String dataPackdir = packdir +"/data";
      String tempDatapackdir = temppackdir +"/data";
      File dataFile = new File(tempDatapackdir);
      String[] dataFileList = dataFile.list();
      if (dataFileList != null)
      {
        for(int i=0; i<dataFileList.length; i++)
        {
          String entry = tempDatapackdir + "/" + dataFileList[i];
          ZipEntry ze = new ZipEntry(dataPackdir + "/" + dataFileList[i]);
          File entryFile = new File(entry);
          ze.setSize(entryFile.length());
          zos.putNextEntry(ze);
          FileInputStream fis = new FileInputStream(entryFile);
          int c = fis.read();
          while(c != -1)
          {
            zos.write(c);
            c = fis.read();
          }
          zos.closeEntry();
        }
      }
      packdir += "/metadata";
      temppackdir += "/metadata";
      File sourcedir = new File(temppackdir);
      File[] sourcefiles = listFiles(sourcedir);
      for(int i=0; i<sourcefiles.length; i++)
      {
        File f = sourcefiles[i];
        
        ZipEntry ze = new ZipEntry(packdir + "/" + f.getName());
        ze.setSize(f.length());
        zos.putNextEntry(ze);
        FileInputStream fis = new FileInputStream(f);
        int c = fis.read();
        while(c != -1)
        {
          zos.write(c);
          c = fis.read();
        }
        zos.closeEntry();
      }
      zos.flush();
      zos.close();
    }
    catch(Exception e)
    {
      Log.debug(5, "Exception in exporting to zip file (AbstractDataPackage)");
    }
  }

  //save the StringBuffer to the File path specified
  private void saveToFile(StringBuffer buff, File outputFile) throws IOException
  {
    FileWriter fileWriter = new FileWriter(outputFile);
    IOUtil.writeToWriter(buff, fileWriter, true);
  }
  
  private File[] listFiles(File dir) {
    String[] fileStrings = dir.list();
    int len = fileStrings.length;
    File[] list = new File[len];
    for (int i=0; i<len; i++) {
        list[i] = new File(dir, fileStrings[i]);    
    }
    return list;
  }
  
  /**
   *  This method displays a summary of Package information by
   *  calling the various utility methods defined in this class.
   *  Primary use is for debugging.
   */
  public void showPackageSummary() {
    boolean sizelimit = true; // set to false to display all attributes
    StringBuffer sb = new StringBuffer();
    sb.append("Title: "+getTitle()+"\n");
    sb.append("AccessionNumber: "+getAccessionNumber()+"\n");
    sb.append("Author: "+getAuthor()+"\n");
    sb.append("keywords: "+getKeywords()+"\n");
    getEntityArray();
    if (entityArray!=null) {
      for (int i=0;i<entityArray.length;i++) {
        sb.append("   entity "+i+" name: "+getEntityName(i)+"\n");
        sb.append("   entity "+i+" numRecords: "+getEntityNumRecords(i)+"\n");
        sb.append("   entity "+i+" description: "+getEntityDescription(i)+"\n");
        int maxattr = getAttributeArray(i).length;
        if (maxattr>8) maxattr=8;
        for (int j=0;j<maxattr;j++) {
          sb.append("      entity "+i+" attribute "+j+"--- name: "+getAttributeName(i,j)+"\n");
          sb.append("      entity "+i+" attribute "+j+"--- unit: "+getAttributeUnit(i,j)+"\n");
          sb.append("      entity "+i+" attribute "+j+"--- dataType: "+getAttributeDataType(i,j)+"\n");
        
        }
        for (int k=0;k<getPhysicalArray(i).length;k++) {
          sb.append("   entity "+i+" physical "+k+"--- name: "+getPhysicalName(i,k)+"\n");
          sb.append("   entity "+i+" physical "+k+"--- format: "+getPhysicalFormat(i,k)+"\n");
          sb.append("   entity "+i+" physical "+k+"--- fieldDelimiter: "+getPhysicalFieldDelimiter(i,k)+"\n");
          sb.append("   entity "+i+" physical "+k+"--- numHeaderLines: "+getPhysicalNumberHeaderLines(i,k)+"\n");
          sb.append("   entity "+i+" physical "+k+"------ compression: "+getCompressionMethod(i,k)+"\n");
          sb.append("   entity "+i+" physical "+k+"------ encoding: "+getEncodingMethod(i,k)+"\n");
          sb.append("      entity "+i+" physical "+k+"------ inline: "+getDistributionInlineData(i,k,0)+"\n");
          sb.append("      entity "+i+" physical "+k+"------ url: "+getDistributionUrl(i,k,0)+"\n");
        }
      }
    }
    Log.debug(1,sb.toString());
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

