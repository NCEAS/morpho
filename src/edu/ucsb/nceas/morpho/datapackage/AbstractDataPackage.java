/**
 *  '$RCSfile: AbstractDataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-09-19 04:16:58 $'
 * '$Revision: 1.4 $'
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
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.*;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import edu.ucsb.nceas.utilities.*;

import java.io.*;
/**
 * class that represents a data package. This class is abstract. Specific datapackages
 * e.g. eml2, beta6., etc extend this abstact class
 */
public abstract class AbstractDataPackage extends MetadataObject
{
  protected String location;
  protected String id;
  protected ConfigXML config;
  protected File dataPkgFile;
  protected FileSystemDataStore fileSysDataStore;
  protected MetacatDataStore  metacatDataStore;
  
  protected Node[] entityArray; 

  abstract void serialize();
  
  abstract void load(String location, String identifier, Morpho morpho);
  
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
  protected File getFileWithID(String ID) throws Throwable {
    
    File returnFile = null;
    if(location.equals(METACAT)) {
      try {
        Log.debug(11, "opening metacat file");
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
      Log.debug(4,"exception in getting keyword");
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
  
  
  
  public void getEntityArray() {
    String entityXpath = "";
    try{
      entityXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='package']/entities")).getNodeValue();
  Log.debug(1,"entityXpath: "+entityXpath);   
      
//  NodeList entityNodes = XMLUtilities.getNodeListWithXPath(metadataNode,entityXpath);
  NodeList entityNodes = XPathAPI.selectNodeList(metadataNode,entityXpath);
  Log.debug(1,"entityNode name: "+entityNodes.item(0).getNodeName());
  if (entityNodes==null) Log.debug(1,"entityList is null!");
      entityArray = XMLUtilities.getNodeListAsNodeArray(entityNodes);
  Log.debug(1, "entityarray[0]: "+entityArray[0]);    
    }
    catch (Exception w) {
      Log.debug(4,"exception in getting entityArray");
    }
  }

  public String getEntityName(int entNum) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entNum)+1)) {
      return "No such entity!";
    }
    Node entity = entityArray[entNum];
    String entityNameXpath = "";
    try {
      entityNameXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/name")).getNodeValue();
    Log.debug(1, "entityPath: "+  entityNameXpath);    
      NodeList enameNodes = XPathAPI.selectNodeList(entity, entityNameXpath, metadataNode);
      if (enameNodes==null) return "XXXX";
      Node child = enameNodes.item(entNum).getFirstChild();
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(4,"exception in getting entity name"+w.toString());
    }
    return temp;
  }
}

