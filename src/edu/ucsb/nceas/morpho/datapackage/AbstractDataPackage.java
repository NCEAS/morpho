/**
 *  '$RCSfile: AbstractDataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-09-30 19:54:56 $'
 * '$Revision: 1.14 $'
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
      
      NodeList entityNodes = XMLUtilities.getNodeListWithXPath(metadataNode,entityXpath);
      //  NodeList entityNodes = XPathAPI.selectNodeList(metadataNode,entityXpath);
      if (entityNodes==null) {
        Log.debug(1,"entityList is null!");
        entityArray = null;
      } else {
        entityArray = XMLUtilities.getNodeListAsNodeArray(entityNodes);
      }
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
      NodeList enameNodes = XPathAPI.selectNodeList(entity, entityNameXpath);
      if (enameNodes==null) return "enameNodes is null !";
      Node child = enameNodes.item(entNum).getFirstChild();
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(4,"exception in getting entity name"+w.toString());
    }
    return temp;
  }

  public String getEntityNumRecords(int entNum) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entNum)+1)) {
      return "No such entity!";
    }
    Node entity = entityArray[entNum];
    String entityNumRecordsXpath = "";
    try {
      entityNumRecordsXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/numRecords")).getNodeValue();
      NodeList eNodes = XPathAPI.selectNodeList(entity, entityNumRecordsXpath);
      if (eNodes==null) return "eNodes is null !";
      Node child = eNodes.item(entNum).getFirstChild();
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(4,"exception in getting entity numRecords"+w.toString());
    }
    return temp;
  }

  public String getEntityDescription(int entNum) {
    String temp = "";
    if ((entityArray==null)||(entityArray.length<(entNum)+1)) {
      return "No such entity!";
    }
    Node entity = entityArray[entNum];
    String entityXpath = "";
    try {
      entityXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/entityDescription")).getNodeValue();
      NodeList eNodes = XPathAPI.selectNodeList(entity, entityXpath);
      if (eNodes==null) return "eNodes is null !";
      Node child = eNodes.item(entNum).getFirstChild();
      temp = child.getNodeValue();
    }
    catch (Exception w) {
      Log.debug(4,"exception in getting entity description"+w.toString());
    }
    return temp;
  }

  public Node[] getAttributeArray(int entityIndex) {
    if(entityIndex>(entityArray.length-1)){
      Log.debug(1, "entity index > number of entities");
      return null;
    }
    String attributeXpath = "";
    try{
      attributeXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/attributes")).getNodeValue();
      NodeList attributeNodes = XMLUtilities.getNodeListWithXPath(entityArray[entityIndex],attributeXpath);
      if (attributeNodes==null) {
        Log.debug(1,"attributeList is null!");
        return null;
      }
//      Node[] attr = XMLUtilities.getNodeListAsNodeArray(attributeNodes);
      return XMLUtilities.getNodeListAsNodeArray(attributeNodes);      
    }
    catch (Exception w) {
      Log.debug(4,"exception in getting attributeArray");
    }
    return null;
  }

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
      Log.debug(4,"exception in getting entity description"+w.toString());
    }
    return temp;
  }
  
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
      Log.debug(20,"exception in getting attribute dataType"+w.toString());
    }
    return temp;
  }

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
      Log.debug(20,"exception in getting attribute unit -- "+w.toString());
    }
    return temp;
  }
  
  
  public Node[] getPhysicalArray(int entityIndex) {
    if(entityIndex>(entityArray.length-1)){
      Log.debug(1, "entity index > number of entities");
      return null;
    }
    String physicalXpath = "";
    try{
      physicalXpath = (XMLUtilities.getTextNodeWithXPath(getMetadataPath(), 
          "/xpathKeyMap/contextNode[@name='entity']/physical")).getNodeValue();
      NodeList physicalNodes = XMLUtilities.getNodeListWithXPath(entityArray[entityIndex],physicalXpath);
      if (physicalNodes==null) {
        Log.debug(1,"physicalList is null!");
        return null;
      }
      return XMLUtilities.getNodeListAsNodeArray(physicalNodes);      
    }
    catch (Exception w) {
      Log.debug(4,"exception in getting physicalArray");
    }
    return null;
  }

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
      Log.debug(4,"exception in getting physical objectName description"+w.toString());
    }
    return temp;
  }
  
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
      Log.debug(20,"exception in getting physical format description --- "+w.toString());
    }
    return temp;
  }
  
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
      Log.debug(20,"exception in getting physical field delimiter"+w.toString());
    }
    return temp;
  }
  
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
      Log.debug(20,"exception in getting physical number HeaderLines"+w.toString());
    }
    return temp;
  }

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
      Log.debug(20,"exception in getting distributionArray");
    }
    return null;
  }
  
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
      Log.debug(20,"exception in getting distribution inline data: "+w.toString());
    }
    return temp;
  }
  
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
      Log.debug(20,"exception in getting distribution url: "+w.toString());
    }
    return temp;
  }
  
  public void showPackageSummary() {
    StringBuffer sb = new StringBuffer();
    sb.append("Title: "+getTitle()+"\n");
    sb.append("AccessionNumber: "+getAccessionNumber()+"\n");
    sb.append("Author: "+getAuthor()+"\n");
    getEntityArray();
    if (entityArray!=null) {
      for (int i=0;i<entityArray.length;i++) {
        sb.append("   entity "+i+" name: "+getEntityName(i)+"\n");
        sb.append("   entity "+i+" numRecords: "+getEntityNumRecords(i)+"\n");
        sb.append("   entity "+i+" description: "+getEntityDescription(i)+"\n");
        for (int j=0;j<getAttributeArray(i).length;j++) {
          sb.append("      entity "+i+" attribute "+j+"--- name: "+getAttributeName(i,j)+"\n");
          sb.append("      entity "+i+" attribute "+j+"--- unit: "+getAttributeUnit(i,j)+"\n");
          sb.append("      entity "+i+" attribute "+j+"--- dataType: "+getAttributeDataType(i,j)+"\n");
        
        }
        for (int k=0;k<getPhysicalArray(i).length;k++) {
          sb.append("   entity "+i+" physical "+k+"--- name: "+getPhysicalName(i,k)+"\n");
          sb.append("   entity "+i+" physical "+k+"--- format: "+getPhysicalFormat(i,k)+"\n");
          sb.append("   entity "+i+" physical "+k+"--- fieldDelimiter: "+getPhysicalFieldDelimiter(i,k)+"\n");
          sb.append("   entity "+i+" physical "+k+"--- numHeaderLines: "+getPhysicalNumberHeaderLines(i,k)+"\n");
          sb.append("      entity "+i+" physical "+k+"------ inline: "+getDistributionInlineData(i,k,0)+"\n");
          sb.append("      entity "+i+" physical "+k+"------ url: "+getDistributionUrl(i,k,0)+"\n");
        }
      }
    }
    Log.debug(1,sb.toString());
  }  
  
}

