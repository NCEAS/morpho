/**
 *  '$RCSfile: DataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-07-24 22:05:25 $'
 * '$Revision: 1.8 $'
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

package edu.ucsb.nceas.morpho.datastore;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.*;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Log;

import java.io.*;
import java.util.*;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import com.arbortext.catalog.*;

/**
 * creates an abstract class for getting files from any dataStore using the same
 * methods.
 */
public abstract class DataStore implements DataStoreInterface
{
  private Morpho morpho;
  private ConfigXML config;
  protected String datadir;
  protected String separator;
  protected String cachedir;
  protected String tempdir;
  
  /**
   * create a new FileSystemDataStore for a Morpho
   */
  public DataStore(Morpho morpho)
  {
    this.morpho = morpho;
    config = morpho.getConfiguration();
    ConfigXML profile = morpho.getProfile();
    String profileDirName = config.getConfigDirectory() + File.separator +
                            config.get("profile_directory", 0) + 
                            File.separator +
                            profile.get("profilename", 0);
    datadir = profileDirName + File.separator + profile.get("datadir", 0);
    tempdir = profileDirName + File.separator + profile.get("tempdir", 0);
    cachedir = profileDirName + File.separator + profile.get("cachedir", 0);
    separator = profile.get("separator", 0);
    separator = separator.trim();
  }
  
  /**
   * Gets the data dir directory
   * @return
   */
  public String getDataDir()
  {
	  return datadir;
  }
  
  public void debug(int code, String message)
  {
    Log.debug(code, message);
  }
  
  /** 
   * Parses a dotted notation id into a file path.  johnson2343.13223 becomes
   * johnson2343/13223.  Revision numbers are left on the end so
   * johnson2343.13223.2 becomes johnson2343/13223.2
   */
  protected String parseId(String id) 
  {
    String path = new String();
    path = id.substring(0, id.indexOf("."));
    path += "/" + id.substring(id.indexOf(separator) + 1, id.length());
    return path;
  }
  
  /**
   * parses the id of a file from the message that metacat returns
   */
  protected String parseIdFromMessage(String message)
  {
    int docidIndex = message.indexOf("<docid>") + 1;
    int afterDocidIndex = docidIndex + 6;
    String docid = message.substring(afterDocidIndex, 
                                     message.indexOf("<", afterDocidIndex));
    debug(11, "docid in parseIdFromMessage: " + docid);
    return docid;
  }
  
  /**
   * inserts a documents id into a specified path in the document.  the document
   * is returned as a string
   * @param file the file to insert the id into
   * @param id the id to insert into the file.
   */
  protected String insertIdInFile(File file, String id)
  {
    String catalogPath = //config.getConfigDirectory() + File.separator +
                                     config.get("local_catalog_path", 0);
    Document doc;
    try
    {
      doc = PackageUtil.getDoc(file, catalogPath);
    }
    catch(Exception e)
    {
      debug(20, "Error getting a DOM rep. of file.  This is probably a data " +
            "file: " + 
            file.getPath() + " : " +
            e.getMessage());
      return null;
    }
    DocumentType dt = doc.getDoctype();
    String doctype = dt.getPublicId();
    Hashtable docatts = PackageUtil.getConfigFileTypeAttributes(morpho, 
                                                                "xmlfiletype");
    Hashtable h = (Hashtable)docatts.get(doctype);
    
    if (h==null) return null;
    if (h.get("idpath")==null) return null;
    
    String idpath = (String)h.get("idpath");
    
    NodeList idNL;
    try
    {
      idNL = XPathAPI.selectNodeList(doc, idpath);
    }
    catch(Exception ee)
    {
      debug(0, "Error XPath searching file: " + file.getPath() + " : " +
            ee.getMessage());
      return null;
    }
    
    String idNodeName = idpath.substring(idpath.lastIndexOf("/") + 1, 
                                         idpath.length()).trim();
    debug(19, "idNodeName: " + idNodeName);
    if(idNL.getLength() != 0)
    { //the path exists, change the value
      for(int i=0; i<idNL.getLength(); i++)
      {
        Node n = idNL.item(i);
        String nname = n.getNodeName();
        if(nname.equals(idNodeName))
        {
          if (n.getFirstChild()!=null) {
            n.getFirstChild().setNodeValue(id);
          }
          else {            // add a text node to idNodeName
            Node txtnode =  doc.createTextNode(id);
            n.appendChild(txtnode);
          }
        }
      }
    }
    
    return PackageUtil.printDoctype(doc) + 
           PackageUtil.print(doc.getDocumentElement());
  }
  
  abstract public File openFile(String name) throws FileNotFoundException, 
                                                    CacheAccessException;
  abstract public File saveFile(String name, Reader file)
           throws Exception;
  abstract public File newFile(String name, Reader file)
           throws Exception;
  abstract public boolean deleteFile(String name) throws Exception;
}
