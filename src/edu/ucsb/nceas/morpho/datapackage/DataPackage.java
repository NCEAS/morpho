/**
 *  '$RCSfile: DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-13 22:21:27 $'
 * '$Revision: 1.18 $'
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
  
  /**
   * used to signify that this package is located on a metacat server
   */
  public static final String METACAT = "metacat";
  /**
   * used to signify that this package is located locally
   */
  public static final String LOCAL = "local";
  
  /*
  public DataPackage()
  {
    
  }*/
  
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
    if(relations != null)
    { //if the relations are provided don't reparse the document
      triples = new TripleCollection(relations);
    }
    else
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
    else if(location.equals(LOCAL))
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
      doc = getDoc(tripleFile, catalogPath);
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
   * parses file with the dom parser and returns a dom Document
   */
  public static Document getDoc(File file, String catalogPath) throws 
                                                               SAXException, 
                                                               Exception
  {
    DOMParser parser = new DOMParser();
    Document doc;
    InputSource in;
    FileInputStream fs;
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try 
    {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      //ConfigXML config = framework.getConfiguration();
      //String catalogPath = config.get("local_catalog_path", 0);
      myCatalog.parseCatalog(catalogPath);
      cer.setCatalog(myCatalog);
    } 
    catch (Exception e) 
    {
      ClientFramework.debug(11, "Problem creating Catalog in " +
                   "packagewizardshell.handleFinishAction!" + e.toString());
      throw new Exception(e.getMessage());
    }
    
    parser.setEntityResolver(cer);
    
    try
    { //parse the wizard created file without the triples
      fs = new FileInputStream(file);
      in = new InputSource(fs);
    }
    catch(FileNotFoundException fnf)
    {
      fnf.printStackTrace();
      throw new Exception(fnf.getMessage());
    }
    try
    {
      parser.parse(in);
      fs.close();
    }
    catch(Exception e1)
    {
      throw new Exception(e1.getMessage());
    }
    //get the DOM rep of the document without triples
    doc = parser.getDocument();
    
    return doc;
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
      {
        FileReader fr = new FileReader(subfile);
        String xmlString = "";
        for(int j=0; j<5; j++)
        {
          xmlString += (char)fr.read();
        }
        
        String name;
        if(xmlString.equals("<?xml"))
        { //we are dealing with a data file here.
          Document subDoc = getDoc(subfile, catalogPath);
          DocumentTypeImpl dt = (DocumentTypeImpl)subDoc.getDoctype();
          name = dt.getName();
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
      {
        FileReader fr = new FileReader(objfile);
        String xmlString = "";
        for(int j=0; j<5; j++)
        {
          xmlString += (char)fr.read();
        }
        
        String name;
        if(xmlString.equals("<?xml"))
        { //we are dealing with a data file here.
          Document objDoc = getDoc(objfile, catalogPath);
          DocumentTypeImpl dt = (DocumentTypeImpl)objDoc.getDoctype();
          name = dt.getName();
          
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
  
/*
  public static void main(String[] args)
  {
    String filename = args[0];
    String location = args[1];
    String action = args[2];
    System.out.println("location: " + location);
    System.out.println("id: " + filename);
    System.out.println("action: " + action);
    if(action.equals("read"))
    {
      ClientFramework cf = new ClientFramework(new ConfigXML("./lib/config.xml"));
      DataPackage dp = new DataPackage(location, filename, null, cf);
    }
    else if(action.equals("write"))
    {
      
    }
  }
*/
}
