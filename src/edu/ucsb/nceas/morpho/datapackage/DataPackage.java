/**
 *  '$RCSfile: DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-08-31 22:40:01 $'
 * '$Revision: 1.29 $'
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

import edu.ucsb.nceas.morpho.framework.*;

import java.util.*;
import java.io.*;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xalan.xpath.xml.FormatterToXML;
import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.apache.xerces.dom.DocumentTypeImpl;

import com.arbortext.catalog.*;

/**
 * class that represents a data package.
 */
public class DataPackage 
{
  private ConfigXML config;
  private TripleCollection triples;
  private File tripleFile = null;
  private String identifier;
  private Hashtable docAtts = new Hashtable();
  private ClientFramework framework;
  private String location = null;
  private String id = null;
  private Document tripleFileDom;
  
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
  
  /**
   * Create a new data package object with an id, location and associated
   * relations.
   * @param location: the location of the file ('METACAT' or 'LOCAL')
   * @param identifier: the id of the data package.  usually the id of the
   * file that contains the triples.
   * @param relations: a vector of all relations in this package.
   */
  public DataPackage(String location, String identifier, Vector relations, 
                     ClientFramework framework)
  {
    //-open file named identifier
    //-read the triples out of it, create a triplesCollection
    //-start caching the files referenced in the triplesCollection
    //-respond to any request from the user to open a specific file
    this.framework = framework;
    config = framework.getConfiguration();
    this.location = location;
    this.id = identifier;
    
    framework.debug(11, "Creating new DataPackage Object");
    framework.debug(11, "id: " + identifier);
    framework.debug(11, "location: " + location);
    
    this.identifier = identifier;
    /*if(relations != null)
    { //if the relations are provided don't reparse the document
      triples = new TripleCollection(relations);
    }
    else
    I'm not relying on the relations right now because I think they are not
    always returned correctly from metacat.
    */
    { //since the relations were not provided we need to parse them out of
      //the document.
      File triplesFile;
      try
      {
        if(location.equals(METACAT))
        {
          MetacatDataStore mds = new MetacatDataStore(framework);
          triplesFile = mds.openFile(identifier);
        }
        else
        {
          FileSystemDataStore fsds = new FileSystemDataStore(framework);
          triplesFile = fsds.openFile(identifier);
        }
      }
      catch(Exception e)
      {
        framework.debug(0, "Error in DataPackage.DataPackage: " + 
                           e.getMessage());
        e.printStackTrace();
        return;
      }
      
      triples = new TripleCollection(triplesFile, framework);
    }
    
    if(location.equals(METACAT))
    {
      framework.debug(11, "opening metacat file");
      MetacatDataStore mds = new MetacatDataStore(framework);
      try
      {
        tripleFile = mds.openFile(identifier);
        FileReader tripleFileReader = new FileReader(tripleFile);
        TripleCollection triples = new TripleCollection(tripleFileReader);
      }
      catch(FileNotFoundException fnfe)
      {
        fnfe.printStackTrace();
      }
      catch(CacheAccessException cae)
      {
        cae.printStackTrace();
      }
    }
    else
    {
      framework.debug(11, "opening local file");
      FileSystemDataStore fsds = new FileSystemDataStore(framework);
      try
      {
        File resourcefile = fsds.openFile(identifier);
        framework.debug(11, "file opened");
        tripleFile = resourcefile;
      }
      catch(FileNotFoundException fnfe)
      {
        fnfe.printStackTrace();
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
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
    DOMParser parser = new DOMParser();
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
    NodeList titleNodeList = null;
    NodeList altTitleNodeList = null;
    String originatorPath = config.get("originatorPath", 0);
    //System.out.println("originatorPath: " + originatorPath);
    String titlePath = config.get("titlePath", 0);
    //System.out.println("titlePath: " + titlePath);
    String altTitlePath = config.get("altTitlePath", 0);
    //System.out.println("alttitlePath: " + altTitlePath);
    try
    {
      //find where the triples go in the file
      originatorNodeList = XPathAPI.selectNodeList(doc, originatorPath);
      titleNodeList = XPathAPI.selectNodeList(doc, titlePath);
      altTitleNodeList = XPathAPI.selectNodeList(doc, altTitlePath);
      
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
      String s = n.getFirstChild().getNodeValue();
      if(!v.contains(s.trim()))
      {
        v.addElement(s.trim());
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
    String catalogPath = config.get("local_catalog_path", 0);
    
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
          DocumentTypeImpl dt = (DocumentTypeImpl)subDoc.getDoctype();
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
          DocumentTypeImpl dt = (DocumentTypeImpl)objDoc.getDoctype();
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
    return this.identifier;
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
        v.add(sub.trim());
      }
      
      if(!v.contains(obj.trim()))
      {
        v.add(obj.trim());
      }
    }
    return v;
  }
  
  /**
   * Uploads a local package to metacat
   */
  public void upload()
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
        }
        catch(Exception e)
        {
          framework.debug(0, "Error reading file in package.");
        }
        
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
          
          if(beginFile.indexOf("<?xml") != -1)
          { //its an xml file
            for(int i=1; i<=revi; i++)
            { //we have to put all of the old versions in metacat first so that
              //the lineage is intact
              String accnum = scope + sep + id + sep + i;
              File g = fsds.openFile(accnum);
              if(i == 1)
              {
                mds.newFile(accnum, new FileReader(g), true);
              }
              else
              {
                mds.saveFile(accnum, new FileReader(g), true);
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
          framework.debug(0, "Error uploading " + key + " to metacat: " + 
                          e.getMessage());
          e.printStackTrace();
        }
      }
    }
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
                fsds.newFile(accnum, new FileReader(g), true);
              }
              else
              {
                fsds.saveFile(accnum, new FileReader(g), true);
              }
            }
          }
          else
          { //its a data file
            fsds.newFile(key, new FileReader(f), true);
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
    Vector v = getAllIdentifiers();
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
        fsds.deleteFile((String)v.elementAt(i));
      }
      
      if(metacatLoc)
      {
        mds.deleteFile((String)v.elementAt(i));
      }
    }
  }
  
  /**
   * exports a package to a given path
   * @param path the path to which this package should be exported.
   */
  public void export(String path)
  {
    ClientFramework.debug(20, "exporting....................................");
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
      String sourcePath = packagePath + "/source";
      File savedir = new File(packagePath);
      File savedirSub = new File(sourcePath);
      savedir.mkdirs(); //create the new directories
      savedirSub.mkdirs();
      
      Vector files = getAllIdentifiers();
      for(int i=0; i<files.size(); i++)
      { //save one file at a time
        File f = new File(sourcePath + "/" + (String)files.elementAt(i));
        File openfile = null;
        f.createNewFile();
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
        FileOutputStream fos = new FileOutputStream(f);
        int c = fis.read();
        while(c != -1)
        { //copy the files to the source directory
          fos.write(c);
          c = fis.read();
        }
        fos.flush();
        fis.close();
        fos.close();
      }
      
      //copy the data file to the root of the package with its original name
      //if there is a data file
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
          realdatafile.createNewFile();
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
      /*
      //create a generic html file from all of the metadata
      String htmlhead = "<html><head><title>Data Package Summary</title></head>";
      htmlhead += "<body><table>";
      String html = "";
      for(int i=0; i<fileV.size(); i++)
      {
        FileInputStream fis = new FileInputStream((File)fileV.elementAt(i));
        String header = "";
        for(int j=0; j<10; j++)
        {
          header += (char)fis.read();
        }
        fis.close();
        System.out.println("==========================header: " + header);
        if(header.indexOf("<?xml") != -1)
        { //this is an xml file so we can transform it.
          String catalogPath = config.get("local_catalog_path", 0);
          Document convdoc = PackageUtil.getDoc((File)fileV.elementAt(i), 
                                                 catalogPath);  
          html += "--\n" + dft(convdoc, html, 0);
        }
      }
      htmlhead += html;
      htmlhead += "\n</table></body></html>";
      System.out.println(htmlhead);*/
    }
    catch(Exception e)
    {
      System.out.println("Error in DataPackage.export(): " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  /**
   * method to do a depth first traversal of a dom tree and return an html rep
   * of the tree in a table.
   */
  private String dft(Node n, String html, int depth)
  {
    short nodetype = n.getNodeType();
    System.out.println("nodetype: " + nodetype);
    try
    {
    if(n.getFirstChild().getNodeValue() != null)
    {
      
      String retstr = "<tr><td>";
      for(int i=0; i<depth; i++)
      {
        retstr += "&nbsp;";
      }
      retstr += n.getNodeName() + "</td><td>";
      retstr += n.getFirstChild().getNodeValue();
      retstr += "</td></tr>";
      html += retstr;
    }
    }
    catch(Exception e){}
    
    Node firstchild = n.getFirstChild();
    if(firstchild == null)
    {
      System.out.println("returning null");
      return html;
    }
    else
    {
      System.out.println("recursing");
      return dft(firstchild, html, depth++);
    }
  }
}
