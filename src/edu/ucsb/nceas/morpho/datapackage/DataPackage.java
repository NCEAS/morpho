/**
 *  '$RCSfile: DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-13 16:01:00 $'
 * '$Revision: 1.57 $'
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

import edu.ucsb.nceas.morpho.framework.XPathAPI;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ClientFramework;
import edu.ucsb.nceas.morpho.framework.DocumentNotFoundException;

import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.xalan.xslt.XSLTInputSource;
import org.apache.xalan.xslt.XSLTResultTarget;
import org.apache.xalan.xslt.XSLTProcessor;
import org.apache.xalan.xslt.XSLTProcessorFactory;

import org.apache.xalan.xpath.xml.XMLParserLiaison;
import org.apache.xalan.xpath.xml.FormatterToXML;
import org.apache.xalan.xpath.xml.TreeWalker;

import com.arbortext.catalog.Catalog;
import com.arbortext.catalog.CatalogEntityResolver;

/**
 * class that represents a data package.
 */
public class DataPackage 
{
  private ConfigXML         config;
  private ClientFramework   framework;
  private TripleCollection  triples;
  private File              tripleFile;
  private String            location;
  private String            id;
  private Document          tripleFileDom;
  private Hashtable         docAtts = new Hashtable();
  
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
  
  /**
   * Create a new data package object with an id, location and associated
   * relations.
   * @param location: the location of the file ('METACAT' or 'LOCAL')
   * @param identifier: the id of the data package.  usually the id of the
   * file that contains the triples.
   * @param relations: a vector of all relations in this package.
   * @param framework: reference to the main ClientFramework.
   */
  public DataPackage(String location, String identifier, Vector relations, 
                     ClientFramework framework)
  {
    //-open file named identifier
    //-read the triples out of it, create a triplesCollection
    //-start caching the files referenced in the triplesCollection
    //-respond to any request from the user to open a specific file
    this.framework  = framework;
    this.location   = location;
    this.id         = identifier;
    config          = framework.getConfiguration();
    
    framework.debug(11, "Creating new DataPackage Object");
    framework.debug(11, "id: " + this.id);
    framework.debug(11, "location: " + location);
    
    if(location.equals(METACAT))
    {
      try 
      {
        framework.debug(11, "opening metacat file");
        MetacatDataStore mds = new MetacatDataStore(framework);
        tripleFile = mds.openFile(this.id);
        framework.debug(11, "file opened");
      }
      catch(FileNotFoundException fnfe)
      {
        framework.debug(0,"Error in DataPackage constructor: file not found: "
                                                          +fnfe.getMessage());
        fnfe.printStackTrace();
        return;
      }
      catch(CacheAccessException cae)
      {
        framework.debug(0,"Error in DataPackage constructor: cache problem: "
                                                          +cae.getMessage());
        cae.printStackTrace();
        return;
      }
    }
    else //not metacat
    {
      try 
      {
        framework.debug(11, "opening local file");
        FileSystemDataStore fsds = new FileSystemDataStore(framework);
        tripleFile = fsds.openFile(this.id);
        framework.debug(11, "file opened");
      }
      catch(FileNotFoundException fnfe)
      {
        framework.debug(0,"Error in DataPackage constructor: file not found: "
                                                          +fnfe.getMessage());
        fnfe.printStackTrace();
        return;
      }
    }
    //create a collection holding the triples
    triples = new TripleCollection(tripleFile, framework);
    
    parseTripleFile();
  }
  
  /**
   * returns the location of the data package.  Either this.METACAT or 
   * this.LOCAL.
   */
  public String getLocation()
  {
    return location;
  }
  
  /**
   * returns the id of the head of this package (i.e. the resource file)
   */
  public String getID()
  {
    return this.id;
  }
  
  /**
   * Open a sub-element of this datapackage (for example, a Module, or a 
   * sub-tree), given its String identifier.
   * @param     identifier                  the unique identifier needed to 
   *                                        locate the desired sub-element. 
   * @return    a <code>java.io.Reader</code> to allow direct read access 
   *                                        to the source
   * @throws    DocumentNotFoundException   if document cannot be found
   * @throws    FileNotFoundException       if document cannot succesfully be 
   *                                        opened and a Reader returned
   */
  public Reader open(String identifier) throws  DocumentNotFoundException, 
                                                FileNotFoundException
  {
    File xmlFile;
    try {
      FileSystemDataStore fsds = new FileSystemDataStore(framework);
      xmlFile = fsds.openFile(identifier);
    } catch(Exception ex1) {
      try {
        MetacatDataStore mds = new MetacatDataStore(framework);
        xmlFile = mds.openFile(identifier);
      } catch(Exception ex2) {
        throw new DocumentNotFoundException("DataPackage.open(): Error opening "
                  + "selected file (CacheAccessException): "+ ex2.getMessage());
      }
    }
    return new FileReader(xmlFile);
  }
  
  /**
   * returns the dom representation of the triple file.
   */
  public Document getTripleFileDom()
  {
    return tripleFileDom;
  }
  
  /**
   * parses the triples file and pulls out the basic information (title, 
   * altTitle, Originators)
   */
  private void parseTripleFile()
  {
    DocumentBuilder parser = framework.createDomParser();
    Document doc;
    InputSource in;
    FileInputStream fs;
    CatalogEntityResolver cer = new CatalogEntityResolver();
    
    //get the DOM rep of the document without triples
    try
    {
      ConfigXML config = framework.getConfiguration();
      String catalogPath = config.get("local_catalog_path", 0);
      doc = PackageUtil.getDoc(tripleFile, catalogPath);
      tripleFileDom = doc;
    }
    catch (Exception e)
    {
      framework.debug(0, "error parsing " + tripleFile.getPath() + " : " +
                         e.getMessage());
      e.printStackTrace();
      return;
    }
    
    NodeList originatorNodeList = null;
    NodeList titleNodeList      = null;
    NodeList altTitleNodeList   = null;
    String originatorPath = config.get("originatorPath", 0);
    String titlePath = config.get("titlePath", 0);
    String altTitlePath = config.get("altTitlePath", 0);
    try
    {
      //find where the triples go in the file
      originatorNodeList  = XPathAPI.selectNodeList(doc, originatorPath);
      titleNodeList       = XPathAPI.selectNodeList(doc, titlePath);
      altTitleNodeList    = XPathAPI.selectNodeList(doc, altTitlePath);
      
      if(titleNodeList.getLength() == 0)
      {
        titleNodeList = XPathAPI.selectNodeList(doc, "//title");
      }
      
    }
    catch(SAXException se)
    {
      System.err.println("parseTripleFile : parse threw: " + 
                         se.toString());
    }
    
    Vector v = new Vector();
    for(int i=0; i<originatorNodeList.getLength(); i++)
    {
      Node n = originatorNodeList.item(i);
      Node child = n.getFirstChild();
      if (null != child) {
        String s = child.getNodeValue();
        if(!v.contains(s.trim()))
        {
          v.addElement(s.trim());
        }
      }
    }
    docAtts.put("originator", v);
    
    v = new Vector();
    for(int i=0; i<titleNodeList.getLength(); i++)
    {
      Node n = titleNodeList.item(i);
      String s = n.getFirstChild().getNodeValue();
      if(!v.contains(s.trim()))
      {
        v.addElement(s.trim());
      }
    }
    docAtts.put("title", v);
    
    v = new Vector();
    for(int i=0; i<altTitleNodeList.getLength(); i++)
    {
      Node n = altTitleNodeList.item(i);
      String s = n.getFirstChild().getNodeValue();
      if(!v.contains(s.trim()))
      {
        v.addElement(s.trim());
      }
    }
    docAtts.put("altTitle", v);
  }
  
  /**
   * returns a hashtable of the related files taken from the triples.  These
   * are organized so that the key of the hashtable is the type of metadata
   * (e.g. 'Entity') and the value of the hashtable is a vector of docids
   */
  public Hashtable getRelatedFiles()
  {
    Vector tripleVec = triples.getCollection();
    Hashtable filesHash = new Hashtable();
    MetacatDataStore mds = new MetacatDataStore(framework);
    FileSystemDataStore fsds = new FileSystemDataStore(framework);
    ConfigXML config = framework.getConfiguration();
    String catalogPath = //config.getConfigDirectory() + File.separator +
                                     config.get("local_catalog_path", 0);
    
    for(int i=0; i<tripleVec.size(); i++)
    {
      Triple t = (Triple)tripleVec.elementAt(i);
      String subject = t.getSubject();
      String object = t.getObject();
      File subfile;
      File objfile;
      //first parse the subject file
      if(subject.trim().equals("") || object.trim().equals(""))
      {
        continue;
      }
      
      try
      {
        //try to open the file locally, if it isn't here then try to get
        //it from metacat
        subfile = fsds.openFile(subject.trim());
      }
      catch(FileNotFoundException fnfe)
      {
        try
        {
          subfile = mds.openFile(subject.trim());
        }
        catch(FileNotFoundException fnfe2)
        {
          framework.debug(0, "File " + subject + " not found locally or " +
                             "on metacat.");
          return null;
        }
        catch(CacheAccessException cae)
        {
          framework.debug(0, "The cache could not be accessed in " +
                             "DataPackage.getRelatedFiles.");
          return null;
        }
      }
      
      try
      { //get the subject files
        FileReader fr = new FileReader(subfile);
        String xmlString = "";
        for(int j=0; j<5; j++)
        {
          xmlString += (char)fr.read();
        }
        
        String name;
        if(xmlString.equals("<?xml"))
        { //we are dealing with an xml file here.
          Document subDoc = PackageUtil.getDoc(subfile, catalogPath);
          DocumentType dt = subDoc.getDoctype();
          name = dt.getPublicId();
        }
        else
        { //this is a data file not an xml file
          name = "Data File";
        }
        
        Vector v;
        if(filesHash.containsKey(name))
        {
          v = (Vector)filesHash.remove(name);
        }
        else
        {
          v = new Vector();
        }
        
        if(!v.contains(subject))
        {
          v.addElement(subject);
        }
        filesHash.put(name, v);
        fr.close();
      }
      catch(Exception e)
      {
        framework.debug(0, "error in DataPackage.getRelatedFiles(): " + 
                           e.getMessage());
      }
      
      //now parse the object files
      try
      {
        //try to open the file locally, if it isn't here then try to get
        //it from metacat
        objfile = fsds.openFile(object.trim());
      }
      catch(FileNotFoundException fnfe)
      {
        try
        {
          objfile = mds.openFile(object.trim());
        }
        catch(FileNotFoundException fnfe2)
        {
          framework.debug(0, "File " + subject + " not found locally or " +
                             "on metacat.");
          return null;
        }
        catch(CacheAccessException cae)
        {
          framework.debug(0, "The cache could not be accessed in " +
                             "DataPackage.getRelatedFiles.");
          return null;
        }
      }
      
      try
      { //object files
        FileReader fr = new FileReader(objfile);
        String xmlString = "";
        for(int j=0; j<5; j++)
        {
          xmlString += (char)fr.read();
        }
        
        String name;
        if(xmlString.equals("<?xml"))
        { //we are dealing with a data file here.
          Document objDoc = PackageUtil.getDoc(objfile, catalogPath);
          DocumentType dt = objDoc.getDoctype();
          name = dt.getPublicId();
          
        }
        else
        { //this is a data file not an xml file
          name = "Data File";
        }
        
        Vector v;
        if(filesHash.containsKey(name))
        {
          v = (Vector)filesHash.remove(name);
        }
        else
        {
          v = new Vector();
        }
        
        if(!v.contains(object))
        {
          v.addElement(object);
        }
        filesHash.put(name, v);
        fr.close();
      }
      catch(Exception e)
      {
        framework.debug(0, "error in DataPackage.getRelatedFiles(): " + 
                           e.getMessage());
      }
    }
    return filesHash;
  }
  
  /**
   * returns a hashtable of vectors with the basic values in it.  Currently,
   * the basic values for the package editor are title, altTitle and 
   * originators
   */
  public Hashtable getAttributes()
  {
    return docAtts;
  }
  
  /**
   * gets the triplesCollection created by this object
   */
  public TripleCollection getTriples()
  {
    return triples;
  }
  
  /**
   * gets the file that contains the package information.
   */
  public File getTriplesFile()
  {
    return tripleFile;
  }
  
  /**
   * gets the identifier of the package  file
   */
  public String getIdentifier()
  {
    return getID();
  }
  
  /**
   * returns the access file's id from the package
   * @return returns the accession number of the access file or null if there
   * is no access file.
   */
  public String getAccessId() throws FileNotFoundException, Exception,
                                     CacheAccessException
  {
    Vector fileids = this.getAllIdentifiers();
    Vector accessFileType = config.get("accessFileType");
    for(int i=0; i<fileids.size(); i++)
    { //read each file to see if it's public id is in the access file id list
      //in the config file.
      File f;
      try
      {
        if(location.equals(DataPackage.LOCAL) || 
           location.equals(DataPackage.BOTH))
        { //open the file locally
          FileSystemDataStore fsds = new FileSystemDataStore(framework);
          f = fsds.openFile((String)fileids.elementAt(i));
        }
        else
        { // get the file from metacat
          MetacatDataStore mds = new MetacatDataStore(framework);
          f = mds.openFile((String)fileids.elementAt(i));
        }
      }
      catch(FileNotFoundException fnfe)
      {
        throw new FileNotFoundException("file: " + (String)fileids.elementAt(i) +
                                        " not found.");
      }
      catch(CacheAccessException cae)
      {
        throw cae;
      }
      
      String catalogpath = //config.getConfigDirectory() + File.separator +
                                       config.get("local_catalog_path",0);
      String publicid = null;
      
      try
      {
        FileInputStream fis = new FileInputStream(f);
        int c = fis.read();
        String s = "";
        for(int j=0; j<10; j++)
        {
          s += (char)c;
          c = fis.read();
        }
        if(s.indexOf("<?xml") != -1)
        {
          Document doc = PackageUtil.getDoc(f, catalogpath);
          DocumentType dt = doc.getDoctype();
          publicid = dt.getPublicId();
          for(int j=0; j<accessFileType.size(); j++)
          {
            String accesstype = ((String)accessFileType.elementAt(j)).trim();
            publicid = publicid.trim();
            if(accesstype.equals(publicid))
            { //this is the file we are looking for
              return (String)fileids.elementAt(i);
            }
          }
        }
      }
      catch(Exception e)
      {
        throw e;
      }
      
      
    }
    throw new FileNotFoundException("The package did not contain an access file " +
                                "(DataPackage.getAccessId()");
  }
  
  /**
   * returns a vector containing a distinct set of all of the file ids that make
   * up this package
   */
  public Vector getAllIdentifiers()
  {
    Vector v = new Vector();
    Vector trips = triples.getCollection();
    for(int i=0; i<trips.size(); i++)
    {
      String sub = ((Triple)trips.elementAt(i)).getSubject();
      String obj = ((Triple)trips.elementAt(i)).getObject();
      if(!v.contains(sub.trim()))
      {
        v.addElement(sub.trim());
      }
      
      if(!v.contains(obj.trim()))
      {
        v.addElement(obj.trim());
      }
    }
    return v;
  }
  
  /**
   * uploads the package with the default of automatically updating the ids
   * when a conflict occurs.
   */
  public DataPackage upload() throws MetacatUploadException
  {
    return upload(true);
  }
  
  /**
   * Uploads a local package to metacat
   * @return the package that was uploaded.  Note that this may have a different
   * id from the one that was told to upload.
   * @param updateIds if this is true, the upload will automatically update
   * the ids of all of the package documents if an id conflict is found.  if
   * it is false, a MetacatUploadException will be raised when an id conflict 
   * occurs
   */
  public DataPackage upload(boolean updateIds) throws MetacatUploadException
  {
    ClientFramework.debug(20, "Uploading package.");
    
    if(!location.equals(DataPackage.BOTH) && 
      !location.equals(DataPackage.METACAT))
    { //if it is not already on metacat, send it there.
      Vector ids = this.getAllIdentifiers();
      Hashtable files = new Hashtable();
      FileSystemDataStore fsds = new FileSystemDataStore(framework);
      MetacatDataStore mds = new MetacatDataStore(framework);
      for(int i=0; i<ids.size(); i++)
      { //get a file pointer to each of the files in the package
        String id = (String)ids.elementAt(i);
        try
        {
          files.put(id, fsds.openFile(id));
        }
        catch(FileNotFoundException fnfe)
        {
          framework.debug(0, "There is an error in this package, a file is " + 
                          "missing.  Missing file: " + id);
        }
      }
      
      Enumeration keys = files.keys();
      while(keys.hasMoreElements())
      { //send each file to metacat.  it's type needs to be checked to see
        //if it is metadata or data
        String key = (String)keys.nextElement();
        //get the file
        File f = (File)files.get(key);

        try
        {
          framework.debug(20, "Uploading " + key);
          AccessionNumber a = new AccessionNumber(framework);
          Vector idVec = a.getParts(key);
          String scope = (String)idVec.elementAt(0);
          String id = (String)idVec.elementAt(1);
          String rev = (String)idVec.elementAt(2);
          String sep = (String)idVec.elementAt(3);
          Integer revI = new Integer(rev);
          int revi = revI.intValue();
          
//          if(beginFile.indexOf("<?xml") != -1)
          if (!isDataFile(key)) 
          { //its an xml file
            for(int i=1; i<=revi; i++)
            { //we have to put all of the old versions in metacat first so that
              //the lineage is intact
              String accnum = scope + sep + id + sep + i;
              File g = fsds.openFile(accnum);
              if(i == 1)
              {
                mds.newFile(accnum, new FileReader(g), this);
              }
              else
              {
                mds.saveFile(accnum, new FileReader(g), this);
              }
            }
          }
          else
          { //its a data file
            mds.newDataFile(key, f);
          }
        }
        catch(Exception e)
        {
          String message = e.getMessage();
          System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>message: " + message);
          String accNumMess1 = "Accession number";
          String accNumMess2 = "is already in use.";
          String accNumMess3 = "unique constraint";
          String accNumMess4 = "violated";
          if((message.indexOf(accNumMess1) != -1 && 
             message.indexOf(accNumMess2) != -1) || 
             (message.indexOf(accNumMess3) != -1 &&
             message.indexOf(accNumMess4) != -1))
          {
            if(updateIds)
            {
              return incrementPackageIds();
            }
            else
            {
              throw new MetacatUploadException(e.getMessage());
            }
          }
          else
          {
            //framework.debug(0, "Error uploading " + key + " to metacat: " + 
            //                e.getMessage());
            //e.printStackTrace();
            throw new MetacatUploadException(e.getMessage());
          }
        }
      }
    }
    return this;
  }
  
  /**
   * find the next unused id then give each file in the package a new id
   * starting with the next unused one.  All of the ids within the package
   * files must also be updated, including the id tags and the triples.
   * @return a new package with the new ids.
   */
  private DataPackage incrementPackageIds()
  {
    Vector fileIds = getAllIdentifiers();
    Hashtable updatedIds = new Hashtable();
    Hashtable updatedFiles = new Hashtable();
    AccessionNumber accNum = new AccessionNumber(framework);
    String newId = "";
    for(int i=0; i<fileIds.size(); i++)
    {
      String fileId = (String)fileIds.elementAt(i);
      StringBuffer sb = new StringBuffer();
      if(!fileId.equals(this.id))
      { //we want to save the package file for last so we can update all 
        //of the ids in the triples.
        newId = accNum.getNextId();
        updatedIds.put(fileId, newId); //keep a record of what we changed
        
        if (!isDataFile(fileId)) {  // not a datafile; string use OK
          try
          {
            File f = PackageUtil.openFile(fileId, framework);
            FileInputStream fis = new FileInputStream(f);
            int c = fis.read();
            while(c != -1)
            { //read the file into a stringbuffer
              sb.append((char)c);
              c = fis.read();
            }
          }
          catch(Exception e)
          {
            framework.debug(0, "Error reading file " + fileId + " in package." +
                          "DataPackage.incrementPackageIds()");
          }
          String fileString = sb.toString();
          fileString = replaceTextInString(fileString, fileId, newId);
          FileSystemDataStore fsds = new FileSystemDataStore(framework);
          MetacatDataStore mds = new MetacatDataStore(framework);
      
          //save the file
          fsds.newFile(newId, new StringReader(fileString)); //new local file
          try
          {
            mds.newFile(newId, new StringReader(fileString)); //new metacat file
          }
          catch(MetacatUploadException mue)
          {
            framework.debug(0, "Error uploading file " + fileId + " to metacat" +
                        " in DataPackage.incrementPackageIds(): " +
                        mue.getMessage());
          }
        }
      
        else {  // it is a datafile
          FileSystemDataStore fsds = new FileSystemDataStore(framework);
          MetacatDataStore mds = new MetacatDataStore(framework);
      
          //save the file
          File f = null;
          try {
            f = PackageUtil.openFile(fileId, framework);
            FileInputStream fis = new FileInputStream(f);
            fsds.newDataFile(newId, fis); //new local file
            fis.close();
            
          }
          catch (Exception w) {
            framework.debug(0, "Problem writing new local data file");
          }
          
          try
          {
            mds.newDataFile(newId, f); //new metacat file
          }
          catch(MetacatUploadException mue)
          {
            framework.debug(0, "Error uploading file " + newId + " to metacat" +
                        " in DataPackage.incrementPackageIds(): " +
                        mue.getMessage());
          }
        }
      }
    } // end loop over files in package
    
    // now deal with the package file itself
    StringBuffer sb = new StringBuffer();
    try
    {
      File packageFile = getTriplesFile();
      FileInputStream fis = new FileInputStream(packageFile);
      int c = fis.read();
      while(c != -1)
      {
        sb.append((char)c);
        c = fis.read();
      }
    }
    catch(FileNotFoundException fnfe)
    {
      framework.debug(0, "Error finding package file in DataPackage." +
                      "incrementPackageIds(): " + fnfe.getMessage());
    }
    catch(IOException ioe)
    {
      framework.debug(0, "Error reading package file in DataPackage." +
                      "incrementPackageIds(): " + ioe.getMessage());
    }
    
    String packageFileString = sb.toString();
    //update the triples file
    Enumeration oldids = updatedIds.keys();
    while(oldids.hasMoreElements())
    { //replace all of the old ids with the new ids from files in the package
      String oldid = (String)oldids.nextElement();
      String newid = (String)updatedIds.get(oldid);
      packageFileString = replaceTextInString(packageFileString,
                                              oldid, newid);
    }
    //replace the id of the package file itself.
    String newPackageId = accNum.getNextId();
    packageFileString = replaceTextInString(packageFileString,
                                            this.id, newPackageId);
//    updatedFiles.put(newPackageId, packageFileString);

    FileSystemDataStore fsds = new FileSystemDataStore(framework);
    MetacatDataStore mds = new MetacatDataStore(framework);
      
    //save the file
    fsds.newFile(newPackageId, new StringReader(packageFileString)); //new local file
    try
    {
      mds.newFile(newPackageId, new StringReader(packageFileString)); //new metacat file
    }
    catch(MetacatUploadException mue)
    {
      framework.debug(0, "Error uploading file " + newPackageId + " to metacat" +
                        " in DataPackage.incrementPackageIds(): " +
                        mue.getMessage());
    }

/*
//------      
    //now we should have the complete package updated.  we need to go through
    //and save all of the files.
    Enumeration packageids = updatedFiles.keys();
    while(packageids.hasMoreElements())
    {
      String fileid = (String)packageids.nextElement();
      String filestring = (String)updatedFiles.get(fileid);
      FileSystemDataStore fsds = new FileSystemDataStore(framework);
      MetacatDataStore mds = new MetacatDataStore(framework);
      
      //save the files
      fsds.newFile(fileid, new StringReader(filestring)); //new local file
      try
      {
        mds.newFile(fileid, new StringReader(filestring)); //new metacat file
      }
      catch(MetacatUploadException mue)
      {
        framework.debug(0, "Error uploading file " + fileid + " to metacat" +
                        " in DataPackage.incrementPackageIds(): " +
                        mue.getMessage());
      }
    }
 //-------- 
*/    
    //create a new package
    DataPackage dp = new DataPackage(this.location, newPackageId, null, 
                                     framework);
    this.delete(this.location);
    return dp;
  }
  
  /**
   * replaces s1 with s2 in text and returns the new string
   */
  private String replaceTextInString(String text, String s1, String s2)
  {
    int s1Index = text.indexOf(s1);
    while(s1Index != -1)
    { 
      String begin = text.substring(0, s1Index);
      String end = text.substring(s1Index + s1.length(), 
                                        text.length());
      begin += s2; //add the new text
      //put the strings back together again.
      text = begin + end;
      s1Index = text.indexOf(s1);
    }
    return text;
  }
  
  /**
   * Downloads a metacat package to the local disk
   */
  public void download()
  {
    ClientFramework.debug(20, "Downloading package.");
    
    if(!location.equals(DataPackage.BOTH) && 
      !location.equals(DataPackage.LOCAL))
    { //if it is not already on the local disk, get it and put it there.
      Vector ids = this.getAllIdentifiers();
      Hashtable files = new Hashtable();
      FileSystemDataStore fsds = new FileSystemDataStore(framework);
      MetacatDataStore mds = new MetacatDataStore(framework);
      for(int i=0; i<ids.size(); i++)
      { //get a file pointer to each of the files in the package
        String id = (String)ids.elementAt(i);
        try
        {
          files.put(id, mds.openFile(id));
        }
        catch(FileNotFoundException fnfe)
        {
          framework.debug(0, "There is an error in this package, a file is " + 
                          "missing.  Missing file: " + id);
        }
        catch(CacheAccessException cae)
        {
          framework.debug(0, "The cache is locked.  Unlock it to continue.");
        }
      }
      
      Enumeration keys = files.keys();
      while(keys.hasMoreElements())
      { //get each file from metacat.  it's type needs to be checked to see
        //if it is metadata or data
        String key = (String)keys.nextElement();
        //get the file
        File f = (File)files.get(key);
        String beginFile = "";
        //check its type
        try
        {
          FileInputStream fis = new FileInputStream(f);
          for(int i=0; i<10; i++)
          { //read 10 bytes of the file
            //if they contain the text '<?xml' assume that it is a metadata file
            //this is a bad assumption but it works for now...
            beginFile += (char)fis.read();
          }
          fis.close();  //DFH
        }
        catch(Exception e)
        {
          framework.debug(0, "Error reading file in package.");
        }
        
        try
        {
          framework.debug(20, "Downloading " + key);
          AccessionNumber a = new AccessionNumber(framework);
          Vector idVec = a.getParts(key);
          String scope = (String)idVec.elementAt(0);
          String id = (String)idVec.elementAt(1);
          String rev = (String)idVec.elementAt(2);
          String sep = (String)idVec.elementAt(3);
          Integer revI = new Integer(rev);
          int revi = revI.intValue();
          
          if(beginFile.indexOf("<?xml") != -1)
          { //its an xml file
            for(int i=1; i<=revi; i++)
            { //we have to get all of the old versions from metacat first so that
              //the lineage is intact
              String accnum = scope + sep + id + sep + i;
              File g = mds.openFile(accnum);
              if(i == 1)
              {
                fsds.newFile(accnum, new FileReader(g));
              }
              else
              {
                fsds.saveFile(accnum, new FileReader(g));
              }
            }
          }
          else
          { //its a data file
            //fsds.newFile(key, new FileReader(f));
 //           fsds.saveDataFile(key, new FileReader(f));   //DFH
            fsds.saveDataFile(key, new FileInputStream(f));
          }
        }
        catch(Exception e)
        {
          framework.debug(0, "Error downloading " + key + " from metacat: " + 
                          e.getMessage());
          e.printStackTrace();
        }
      }
    }
  }
  
  /**
   * Deletes the package from the specified location
   * @param locattion the location of the package that you want to delete
   * use either DataPackage.BOTH, DataPackage.METACAT or DataPackage.LOCAL 
   */
  public void delete(String location)
  {
    MetacatDataStore mds = new MetacatDataStore(framework);
    FileSystemDataStore fsds = new FileSystemDataStore(framework);
    Vector v = this.getAllIdentifiers();
    boolean metacatLoc = false;
    boolean localLoc = false;
    if(location.equals(DataPackage.METACAT) || 
       location.equals(DataPackage.BOTH))
    {
      metacatLoc = true;
    }
    if(location.equals(DataPackage.LOCAL) ||
       location.equals(DataPackage.BOTH))
    {
      localLoc = true;
    }
    
    for(int i=0; i<v.size(); i++)
    { //loop through and delete all of the files in the package
      
      if(localLoc)
      {
        String delfile = (String)v.elementAt(i);
        String rev = delfile.substring(delfile.lastIndexOf(".")+1, 
                                       delfile.length());
        int revi = (new Integer(rev)).intValue();
        for(int j=1; j<=revi; j++)
        { //we have to make sure that we delete all of the revisions or else
          //the package will still be found in a query
          String acc = delfile.substring(0, delfile.lastIndexOf("."));
          fsds.deleteFile(acc + "." + j);
        }
      }
      
      if(metacatLoc)
      {
        mds.deleteFile((String)v.elementAt(i));
      }
    }
  }
  
  /**
   * Exports a package to a zip file at the given path
   * @param path the path to export the zip file to
   */
  public void exportToZip(String path) throws Exception
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
      throw e;
    }
  }
    
  /**
   * exports a package to a given path
   * @param path the path to which this package should be exported.
   */
  public void export(String path)
  {
    ClientFramework.debug(20, "exporting...");
    ClientFramework.debug(20, "path: " + path);
    ClientFramework.debug(20, "id: " + id);
    ClientFramework.debug(20, "location: " + location);
    Vector fileV = new Vector(); //vector of all files in the package
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackage.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackage.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackage.LOCAL))
    {
      localloc = true;
    }
    
    //get a list of the files and save them to the new location. if the file
    //is a data file, save it with its original name.
    try
    {
      FileSystemDataStore fsds = new FileSystemDataStore(framework);
      MetacatDataStore mds = new MetacatDataStore(framework);
      String packagePath = path + "/" + id + ".package";
      String sourcePath = packagePath + "/metadata";
      File savedir = new File(packagePath);
      File savedirSub = new File(sourcePath);
      savedir.mkdirs(); //create the new directories
      savedirSub.mkdirs();
      
      Vector files = getAllIdentifiers();
      for(int i=0; i<files.size(); i++)
      { //save one file at a time
        File f = new File(sourcePath + "/" + (String)files.elementAt(i));
        File openfile = null;
        if(localloc)
        { //get the file locally and save it
          openfile = fsds.openFile((String)files.elementAt(i));
        }
        else if(metacatloc)
        { //get the file from metacat
          openfile = mds.openFile((String)files.elementAt(i));
        }
        
        fileV.addElement(openfile);
        FileInputStream fis = new FileInputStream(openfile);
        BufferedInputStream bfis = new BufferedInputStream(fis);
        FileOutputStream fos = new FileOutputStream(f);
        BufferedOutputStream bfos = new BufferedOutputStream(fos);
        int c = bfis.read();
        while(c != -1)
        { //copy the files to the source directory
          bfos.write(c);
          c = bfis.read();
        }
        bfos.flush();
        bfis.close();
        bfos.close();
      }
      
      //copy the data file to the root of the package with its original name
      //if there is a data file
      /*
      Vector triplesV = triples.getCollection();
      String dataFileName = null;
      String dataFileId = null;
      for(int i=0; i<triplesV.size(); i++)
      {
        Triple triple = (Triple)triplesV.elementAt(i);
        String relationship = triple.getRelationship();
        if(relationship.indexOf("isDataFileFor") != -1)
        {
          int lparenindex = relationship.indexOf("(");
          dataFileName = relationship.substring(lparenindex + 1, 
                                                relationship.length() - 1);
          dataFileId = triple.getSubject();
          File datafile = new File(sourcePath + "/" + dataFileId);
          File realdatafile = new File(packagePath + "/" + dataFileName);
          FileInputStream fis = new FileInputStream(datafile);
          FileOutputStream fos = new FileOutputStream(realdatafile);
          int c = fis.read();
          while(c != -1)
          { //copy the data file with its real name
            fos.write(c);
            c = fis.read();
          }
        }
      }
      */
      
      //create a html file from all of the metadata
      StringBuffer htmldoc = new StringBuffer();
      ConfigXML config = framework.getConfiguration();
      htmldoc.append("<html><head></head><body>");
      for(int i=0; i<fileV.size(); i++)
      {
        FileInputStream fis = new FileInputStream((File)fileV.elementAt(i));
        String header = "";
        for(int j=0; j<10; j++)
        {
          header += (char)fis.read();
        }
        fis.close();
        if(header.indexOf("<?xml") != -1)
        { //this is an xml file so we can transform it.
          //transform each file individually then concatenate all of the 
          //transformations together.
          CatalogEntityResolver cer = new CatalogEntityResolver();
          try 
          {
            Catalog myCatalog = new Catalog();
            myCatalog.loadSystemCatalogs();
            String catalogPath = //config.getConfigDirectory() + File.separator +
                                             config.get("local_catalog_path", 0);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL catalogURL = cl.getResource(catalogPath);
        
            myCatalog.parseCatalog(catalogURL.toString());
            //myCatalog.parseCatalog(catalogPath);
            cer.setCatalog(myCatalog);
          } 
          catch (Exception e) 
          {
            ClientFramework.debug(9, "Problem creating Catalog in " +
                         "DataPackage.export" + 
                         e.toString());
          }
          
          htmldoc.append("<h2>");
          htmldoc.append(getIdFromPath(((File)fileV.elementAt(i)).toString()));
          htmldoc.append("</h2>");
          //do the actual transform
          XSLTProcessor processor = XSLTProcessorFactory.getProcessor();
          XMLParserLiaison pl = processor.getXMLProcessorLiaison();
          pl.setEntityResolver(cer);
          fis = new FileInputStream((File)fileV.elementAt(i));
          XSLTInputSource xis = new XSLTInputSource(fis);
          StringWriter docstring = new StringWriter();
          String stylesheet = config.get("genericStylesheet", 0);
          ClassLoader cl = this.getClass().getClassLoader();
          BufferedReader sis = new BufferedReader(new InputStreamReader(
                                            cl.getResourceAsStream(stylesheet)));
          processor.process(xis,
                            new XSLTInputSource(sis),
                            new XSLTResultTarget(docstring));
          htmldoc.append(docstring.toString());
          htmldoc.append("<br><br><hr><br><br>");
        }
        else
        { //this is a data file so we should link to it in the html
          Vector triplesV = triples.getCollection();
          htmldoc.append("<a href=\"");
          String dataFileName = null;
          String datafileid = getIdFromPath(((File)fileV.elementAt(i)).toString());
          
          for(int j=0; j<triplesV.size(); j++)
          {
            Triple triple = (Triple)triplesV.elementAt(j);
            String relationship = triple.getRelationship();
            String subject = triple.getSubject();
            if(subject.trim().equals(datafileid.trim()))
            {
              if(relationship.indexOf("isDataFileFor") != -1)
              { //get the name of the data file.
                int lparenindex = relationship.indexOf("(");
                dataFileName = relationship.substring(lparenindex + 1, 
                                                      relationship.length() - 1);
                htmldoc.append("./metadata/").append(datafileid).append("\">");
                htmldoc.append("Data File: ");
                htmldoc.append(dataFileName).append("</a><br>");
              }
            }
          }
          htmldoc.append("<br><hr><br>");
        }
      }
      htmldoc.append("</body></html>");
      File htmlfile = new File(packagePath + "/metadata.html");
      FileOutputStream fos = new FileOutputStream(htmlfile);
      BufferedOutputStream bfos = new BufferedOutputStream(fos);
      StringReader sr = new StringReader(htmldoc.toString());
      int c = sr.read();
      while(c != -1)
      {
        bfos.write(c);
        c = sr.read();
      }
      bfos.flush();
      bfos.close();
    }
    catch(Exception e)
    {
      System.out.println("Error in DataPackage.export(): " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  /**
   * returns the id of a file based on the path.  this method assumes that 
   * the file is being stored
   */
  private String getIdFromPath(String path)
  {
    char sep = '/';
    if(path.indexOf("\\") != -1)
    { //check for windows separator
      sep = '\\';
    }
    int lastSep = path.lastIndexOf(sep);
    String num = path.substring(lastSep+1, path.length());
    int secondToLastSep = lastSep - 1;
    char c = path.charAt(secondToLastSep--);
    while(c != sep)
    {
      secondToLastSep--;
      c = path.charAt(secondToLastSep);
    }
    String scope = path.substring(secondToLastSep+1, lastSep);
    return scope + "." + num;
  }
  
 /** 
  * Checks a file to see if it is a text file by looking for bytes containing '0'
  */
   private boolean isTextFile(File file) 
   { 
     boolean text = true; 
     int res; 
     int cnt = 0;
     int maxcnt = 2000; // only check this many bytes to avoid performance problems
     try 
     { 
       FileInputStream in = new FileInputStream(file); 
       while (((res = in.read())>-1) &&(cnt<maxcnt))
       { 
         cnt++;
         if (res==0) text = false; 
       } 
     } 
     catch (Exception e) {} 
     return text; 
   } 
   
  /*
   * find out if the doc with the given id is a data doc
   * using the triple relationship
   */
   public boolean isDataFile(String id) {
      boolean res = false;
      Vector triplesV = triples.getCollection();
      for(int i=0; i<triplesV.size(); i++)
      {
        Triple triple = (Triple)triplesV.elementAt(i);
        String subject = triple.getSubject();
        if (subject.trim().equals(id.trim())) {
          String relationship = triple.getRelationship();
          if(relationship.indexOf("isDataFileFor") != -1) {
            res = true;
            return res;
          }
        }
      }
      return res;
   }
  
  /*
   * find out if there are datafiles associated with the given entity ID
   */
  public boolean hasDataFile(String entityID) {
    boolean result = false;
      Vector triplesV = triples.getCollection();
      // first find out if there are ANY data files
      for(int i=0; i<triplesV.size(); i++)
      {
        Triple triple = (Triple)triplesV.elementAt(i);
        String relationship = triple.getRelationship();
        if(relationship.indexOf("isDataFileFor") != -1)
        {
          String dataFileID = triple.getSubject();
          // now see if entity file points to this data file
          for (int j=0; j<triplesV.size();j++) {
            Triple tripleA = (Triple)triplesV.elementAt(j);
            if ((tripleA.getSubject().equals(entityID))&&(tripleA.getObject().equals(dataFileID))) {
              result = true;  
            }
          }
        }
      }
    return result;
  }
 
  public boolean isDataFileText(String entityID) {
    boolean result = false;
    File fl = getDataFile(entityID);
    if (fl!=null) {
      result = isTextFile(fl);
    }
    else result = false;
    return result;
  }
  
  
  public String getDataFileName(String entityID) {
    String dataFileName = null;
    boolean result = false;
      Vector triplesV = triples.getCollection();
      // first find out if there are ANY data files
      for(int i=0; i<triplesV.size(); i++)
      {
        Triple triple = (Triple)triplesV.elementAt(i);
        String relationship = triple.getRelationship();
        if(relationship.indexOf("isDataFileFor") != -1)
        {
          int lparenindex = relationship.indexOf("(");
          dataFileName = relationship.substring(lparenindex + 1, 
                                                relationship.length() - 1);
          String dataFileID = triple.getSubject();
          // now see if entity file points to this data file
          for (int j=0; j<triplesV.size();j++) {
            Triple tripleA = (Triple)triplesV.elementAt(j);
            if ((tripleA.getSubject().equals(entityID))&&(tripleA.getObject().equals(dataFileID))) {
              result = true;  
            }
          }
        }
      }
    String resultString = null;
    if (result) resultString = dataFileName;
    return resultString;
  }
  
  public File getPhysicalFile(String entityID) {
    File physicalfile = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackage.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackage.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackage.LOCAL))
    {
      localloc = true;
    }
    // assume that entity is the object;
    // ie eml-physical is related to entity
    Vector triplesV = triples.getCollectionByObject(entityID) ;
    for (int i=0;i<triplesV.size();i++) {
        Triple triple = (Triple)triplesV.elementAt(i);
        String sub = triple.getSubject();
         physicalfile = getFileType(sub, "physical");
         if (physicalfile!=null) return physicalfile;
         
    }
    
    return physicalfile;
  }
  
  public File getAttributeFile(String entityID) {
    File attributefile = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackage.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackage.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackage.LOCAL))
    {
      localloc = true;
    }
    // assume that entity is the object;
    // ie eml-attribute is related to entity
    Vector triplesV = triples.getCollectionByObject(entityID) ;
    for (int i=0;i<triplesV.size();i++) {
        Triple triple = (Triple)triplesV.elementAt(i);
        String sub = triple.getSubject();
         attributefile = getFileType(sub, "attribute");
         if (attributefile!=null) return attributefile;
    }

    
    return attributefile;
  }

  /**
   * get the id of the access doc for the indicated id
   */
  public String getAccessFileId(String id) {
    File accessfile = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackage.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackage.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackage.LOCAL))
    {
      localloc = true;
    }
    // assume that id is the object;
    // ie eml-access is related to id
    Vector triplesV = triples.getCollectionByObject(id) ;
    for (int i=0;i<triplesV.size();i++) {
        Triple triple = (Triple)triplesV.elementAt(i);
        String sub = triple.getSubject();
         accessfile = getFileType(sub, "access");
         if (accessfile!=null) return sub.trim();
    }
    
    return "unknown";
  }

  public String getAccessFileIdForDataPackage() {
    String temp = getAccessFileId(this.id); 
    return temp;
  }

  public File getDataFile(String entityID) {
    File datafile = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackage.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackage.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackage.LOCAL))
    {
      localloc = true;
    }
      Vector triplesV = triples.getCollection();
      // first find out if there are ANY data files
      for(int i=0; i<triplesV.size(); i++)
      {
        Triple triple = (Triple)triplesV.elementAt(i);
        String relationship = triple.getRelationship();
        FileSystemDataStore fsds = new FileSystemDataStore(framework);
        MetacatDataStore mds = new MetacatDataStore(framework);
        if(relationship.indexOf("isDataFileFor") != -1)
        {
          String dataFileID = triple.getSubject();
          // now see if entity file points to this data file
          for (int j=0; j<triplesV.size();j++) {
            Triple tripleA = (Triple)triplesV.elementAt(j);
            if ((tripleA.getSubject().equals(entityID))&&(tripleA.getObject().equals(dataFileID))) {
              if(localloc)
              { //get the file locally and save it
                try {
                  datafile = fsds.openFile(dataFileID);
                }
                catch(Exception e)
                {
                  System.out.println("Error in DataPackage.getDataFile(): " + e.getMessage());
                  e.printStackTrace();
                }
              }
              else if(metacatloc)
              { //get the file from metacat
                try {
                  datafile = mds.openFile(dataFileID);
                }
                catch(Exception e)
                {
                  System.out.println("Error in DataPackage.getDataFile(): " + e.getMessage());
                  e.printStackTrace();
                }
              }
              
//              File realdatafile = new File(packagePath + "/" + dataFileName);
//              realdatafile.createNewFile();
//              FileInputStream fis = new FileInputStream(datafile);
//              FileOutputStream fos = new FileOutputStream(realdatafile);
//              int c = fis.read();
//              while(c != -1)
//              { //copy the data file with its real name
//                fos.write(c);
//                c = fis.read();
//              }
              
            }
          }
        }
      }
    return datafile;
  } 


  public String getDataFileID(String entityID) {
    String dataFileID = "";
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackage.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackage.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackage.LOCAL))
    {
      localloc = true;
    }
      Vector triplesV = triples.getCollection();
      // first find out if there are ANY data files
      for(int i=0; i<triplesV.size(); i++)
      {
        Triple triple = (Triple)triplesV.elementAt(i);
        String relationship = triple.getRelationship();
        FileSystemDataStore fsds = new FileSystemDataStore(framework);
        MetacatDataStore mds = new MetacatDataStore(framework);
        if(relationship.indexOf("isDataFileFor") != -1)
        {
          String dFileID = triple.getSubject();
          // now see if entity file points to this data file
          for (int j=0; j<triplesV.size();j++) {
            Triple tripleA = (Triple)triplesV.elementAt(j);
            if ((tripleA.getSubject().equals(entityID))&&(tripleA.getObject().equals(dFileID))) {
              if(localloc)
              { //get the file locally and save it
                try {
                  dataFileID = dFileID;
                }
                catch(Exception e)
                {
                  System.out.println("Error in DataPackage.getDataFile(): " + e.getMessage());
                  e.printStackTrace();
                }
              }
              else if(metacatloc)
              { //get the file from metacat
                try {
                  dataFileID = dFileID;
                }
                catch(Exception e)
                {
                  System.out.println("Error in DataPackage.getDataFile(): " + e.getMessage());
                  e.printStackTrace();
                }
              }
                            
            }
          }
        }
      }
    return dataFileID;
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

  
  private File getFileType(String id, String typeString) {
    MetacatDataStore mds = new MetacatDataStore(framework);
    FileSystemDataStore fsds = new FileSystemDataStore(framework);
    ConfigXML config = framework.getConfiguration();
    String catalogPath = //config.getConfigDirectory() + File.separator +
                                     config.get("local_catalog_path", 0);
    File subfile;
    String name = "unknown";
      try
      {
        //try to open the file locally, if it isn't here then try to get
        //it from metacat
        subfile = fsds.openFile(id.trim());
      }
      catch(FileNotFoundException fnfe)
      {
        try
        {
          subfile = mds.openFile(id.trim());
        }
        catch(FileNotFoundException fnfe2)
        {
          framework.debug(0, "File " + id + " not found locally or " +
                             "on metacat.");
          return null;
        }
        catch(CacheAccessException cae)
        {
          framework.debug(0, "The cache could not be accessed in " +
                             "DataPackage.getRelatedFiles.");
          return null;
        }
      }
      
      try
      { 
        FileReader fr = new FileReader(subfile);
        String xmlString = "";
        for(int j=0; j<5; j++)
        {
          xmlString += (char)fr.read();
        }
        
        if(xmlString.equals("<?xml"))
        { //we are dealing with an xml file here.
          Document subDoc = PackageUtil.getDoc(subfile, catalogPath);
          DocumentType dt = subDoc.getDoctype();
          name = dt.getPublicId();
        }
        else
        { //this is a data file not an xml file
          name = "Data File";
        } 
      }
      catch (Exception ww) {}
      System.out.println("Name: "+name);
      if (name.indexOf(typeString)>-1)   // i.e. PublicId contains typeString
      {
        return subfile;
      } else {
        subfile = null;
      }
      return subfile;
  }
}
