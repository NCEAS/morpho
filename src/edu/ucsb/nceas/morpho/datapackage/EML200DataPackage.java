/**
 *  '$RCSfile: EML200DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-12-24 08:27:12 $'
 * '$Revision: 1.20 $'
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
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * class that represents a data package. This class is abstract. Specific datapackages
 * e.g. eml2, beta6., etc extend this abstact class
 */
public  class EML200DataPackage extends AbstractDataPackage
{
  // serialize to the indicated location
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
        if ((this.getLocation().equals(AbstractDataPackage.METACAT))||
           (this.getLocation().equals(AbstractDataPackage.BOTH)))
        {
          mds.saveFile(getAccessionNumber(),sr);
        } // originally came from metacat; thus update
        else
        {
          mds.newFile(getAccessionNumber(),sr);
        }// not currently on metacat

      } catch(Exception e) {
          Log.debug(5,"Problem with saving to metacat in EML200DataPackage!");
        }
      }
  }

  /*
   *  This method loops over the entities associated witht the package and
   *  finds associated datafile id referenced in distribution url
   */
  private Vector getAssociatedDataFiles() {
    String urlinfo = null;
    Vector res = new Vector();
    Entity[] ents = getEntityArray();
    // if package has no data
    if(ents == null)
      return res;
    for (int i=0;i<ents.length;i++) {
      if (getDistributionUrl(i, 0,0).length()>0) {  // there is a url link
        urlinfo = getDistributionUrl(i, 0,0);
        if (urlinfo.startsWith("ecogrid://")) {
          // assumed that urlinfo is of the form 'ecogrid://systemname/localid/other'
          // system name is 'knb'
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
          if (urlinfo.length()>0) {
            // if we reach here, urlinfo should be the locvalid in a string
            res.addElement(urlinfo);
          }
        }
      }
    }
    return res;
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
    this.doc = doc;  // set the MetadataObject doc
    try{
      metadataPathNode = XMLUtilities.getXMLAsDOMTreeRootNode("/lib/eml200KeymapConfig.xml");
    }
    catch (Exception e2) {
      Log.debug(4, "getting DOM for Paths threw error: " + e2.toString());
      e2.printStackTrace();
    }
  }


  /**
   * override method in AbstractDataPackage to get all authors and combine name
   * fields
   *
   * @return String
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

  public AbstractDataPackage upload(String id) throws MetacatUploadException {
    Morpho morpho = Morpho.thisStaticInstance;
    load(AbstractDataPackage.LOCAL, id, morpho);
    serialize(AbstractDataPackage.METACAT);
    serializeData();
    return this;
  }

  public AbstractDataPackage download(String id) {
    Morpho morpho = Morpho.thisStaticInstance;
    //load(AbstractDataPackage.METACAT, id, Morpho.thisStaticInstance);
    serialize(AbstractDataPackage.LOCAL);
    // now download the associated data files
    Vector idlist = getAssociatedDataFiles();
    Enumeration e = idlist.elements();
    MetacatDataStore mds = new MetacatDataStore(morpho);
    FileSystemDataStore fds = new FileSystemDataStore(morpho);
    while (e.hasMoreElements()) {
      String curid = (String)e.nextElement();
      try{
        File datafile = mds.openFile(curid);
        FileReader fr = new FileReader(datafile);
        fds.saveFile(curid, fr);
      }
      catch (Exception q3) {
        Log.debug(5,"Exception opening datafile from metacat and saving locally");
      }
    }
    return this;
  }
}

