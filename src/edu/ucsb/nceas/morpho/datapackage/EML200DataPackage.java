/**
 *  '$RCSfile: EML200DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-12-01 22:57:25 $'
 * '$Revision: 1.12 $'
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
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
import com.arbortext.catalog.*;
import java.io.*;
import org.xml.sax.InputSource;
import org.apache.xpath.XPathAPI;
import java.util.Vector;
import java.util.Hashtable;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.utilities.*;


/**
 * class that represents a data package. This class is abstract. Specific datapackages
 * e.g. eml2, beta6., etc extend this abstact class
 */
public  class EML200DataPackage extends AbstractDataPackage
{
  public void serialize(String location) {
    Morpho morpho = Morpho.thisStaticInstance;
    String temp = XMLUtilities.getDOMTreeAsString(getMetadataNode(), false);
    StringReader sr = new StringReader(temp);
      if((location.equals(AbstractDataPackage.LOCAL))||
                 (location.equals(AbstractDataPackage.BOTH))) {
        FileSystemDataStore fsds = new FileSystemDataStore(morpho);  
        fsds.saveFile(getAccessionNumber(),sr);
      }
      if((location.equals(AbstractDataPackage.METACAT))||
                 (location.equals(AbstractDataPackage.BOTH))) {
        MetacatDataStore mds = new MetacatDataStore(morpho);  
      try{
        mds.saveFile(getAccessionNumber(),sr);
      } catch(Exception e) {
          Log.debug(5,"Problem with saving to metacat in EML200DataPackage!");
        }
      }
  }
  
  public void load(String location, String identifier, Morpho morpho) {
    this.location   = location;
    this.id         = identifier;
    this.config     = Morpho.getConfiguration();

    Log.debug(20, "Creating new DataPackage Object");
    Log.debug(20, "id: " + this.id);
    Log.debug(20, "location: " + location);
    
    morpho = Morpho.thisStaticInstance;
    
    fileSysDataStore  = new FileSystemDataStore(morpho);
    metacatDataStore  = new MetacatDataStore(morpho);
    File packagefile;
    try {
      packagefile = getFileWithID(this.id, morpho);
     } catch (Throwable t) {
      //already handled in getFileWithID() method, 
      //so just abandon this instance:
      return;
    }
    
    DocumentBuilder parser = Morpho.createDomParser();
    InputSource in;
    FileInputStream fs;
    Document doc = null;
    try
    {
    if (packagefile==null) Log.debug(1, "packagefile is NULL!");
      fs = new FileInputStream(packagefile);
      in = new InputSource(fs);

      doc = parser.parse(in);
      fs.close();
   } catch(Exception e1) {
      Log.debug(4, "Parsing threw: " + e1.toString());
      e1.printStackTrace();
    }
    if (doc==null) Log.debug(1, "doc is NULL!");
    metadataNode = doc.getDocumentElement();  // the root Node
    try{
      metadataPathNode = XMLUtilities.getXMLAsDOMTreeRootNode("/lib/eml200KeymapConfig.xml");
    }
    catch (Exception e2) {
      Log.debug(4, "getting DOM for Paths threw error: " + e2.toString());
      e2.printStackTrace();
    }
  }
  
  /**
   *  override method in AbstractDataPackage to get all authors and combine name fields
   */
  public String getAuthor() {
    String temp = "";
    String surNameXpath = "/eml:eml/dataset/creator/individualName/surName";
    String givenNameXpath = "/eml:eml/dataset/creator/individualName/givenName";
    String salutationXpath = "/eml:eml/dataset/creator/individualName/salutation";

    NodeList authorNodes = null;
    try{
      authorNodes = XMLUtilities.getNodeListWithXPath(metadataNode, surNameXpath);
    }
    catch (Exception w) {
      Log.debug(4, "Problem with getting Nodelist");
    }
    if (authorNodes==null) return "";  // no authors
    int numAuthors = authorNodes.getLength();
    
    String surName = "";
    String givenName = "";
    String salutation = "";
    for (int i=1;i<numAuthors+1;i++) {
      surName = getXPathValue("("+surNameXpath +")["+i+"]");
      givenName = getXPathValue("("+givenNameXpath +")["+i+"]");
      salutation = getXPathValue("("+salutationXpath+")["+i+"]");
      if (temp.length()>0) temp = temp + ", ";
      temp = temp + salutation + " " + givenName + " " + surName;
    }
    return temp;
  }
}

