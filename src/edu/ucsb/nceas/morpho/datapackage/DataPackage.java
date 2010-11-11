/**
 *  '$RCSfile: DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-12-17 01:29:46 $'
 * '$Revision: 1.120 $'
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.arbortext.catalog.CatalogEntityResolver;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;
import edu.ucsb.nceas.morpho.query.LocalQuery;
import edu.ucsb.nceas.morpho.util.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.util.IOUtil;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLTransformer;

/**
 * class that represents a data package.
 */
public class DataPackage implements XMLFactoryInterface
{
  private ConfigXML         config;
  private Morpho            morpho;
  private TripleCollection  triples;
  private File              tripleFile;
  private File              dataPkgFile;
  private String            location;
  private String            id;
  private Document          tripleFileDom;
  private Hashtable         docAtts = new Hashtable();

  private final FileSystemDataStore fileSysDataStore;
  private final MetacatDataStore    metacatDataStore;
  private final String              HTMLEXTENSION = ".html";
  private final String              METADATAHTML = "metadata";
  private final String CONFIG_KEY_CSS_LOCATION = "emlCSSLocation";
  private final String CONFIG_KEY_MCONFJAR_LOC   = "morphoConfigJarLocation";
  private final String EXPORTSYLE ="export";
  private final String EXPORTSYLEEXTENSION =".css";

  Hashtable entity2PhysicalFile;
  Hashtable entity2AttributeFile;
  Hashtable entity2DataFile;



  /**
   * Create a new data package object with an id, location and associated
   * relations.
   * @param location: the location of the file ('METACAT' or 'LOCAL')
   * @param identifier: the id of the data package.  usually the id of the
   * file that contains the triples.
   * @param relations: a vector of all relations in this package.
   * @param morpho: reference to the main Morpho application.
   */
  public DataPackage(String location, String identifier, Vector relations,
                     Morpho morpho, boolean accessCheckFlag)
  {
    entity2PhysicalFile = new Hashtable();
    entity2AttributeFile = new Hashtable();
    entity2DataFile = new Hashtable();
    //-open file named identifier
    //-read the triples out of it, create a triplesCollection
    //-start caching the files referenced in the triplesCollection
    //-respond to any request from the user to open a specific file
    this.morpho  = morpho;
    this.location   = location;
    this.id         = identifier;
    this.config     = morpho.getConfiguration();

    Log.debug(11, "Creating new DataPackage Object");
    Log.debug(11, "id: " + this.id);
    Log.debug(11, "location: " + location);


    fileSysDataStore  = new FileSystemDataStore(morpho);
    metacatDataStore  = new MetacatDataStore(morpho);

    //read the file containing the triples - usually the datapackage file:
    try {
      tripleFile = getFileWithID(this.id);
    } catch (Throwable t) {
      //already handled in getFileWithID() method,
      //so just abandon this instance:
      return;
    }
    //initialize global "triples" variable to hold collection of triples:
    initTriplesCollection();
    //parse triples file and get basic information (title, Originators etc)
    parseTripleFile();
    if (accessCheckFlag) {
      checkTriplesForAccess();
    }
  }


  // util to read the file from either FileSystemDataStore or MetacatDataStore
  private File getFileWithID(String ID) throws Throwable {

    File returnFile = null;
    if(location.equals(DataPackageInterface.METACAT)) {
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

  //initialize the global "triples" variable to hold the collection of triples
  private void initTriplesCollection()  {
    triples = new TripleCollection(tripleFile, morpho);
  }


  //check if identifier actually points to a valid
  //sub-element (module, subtree etc)
  private boolean idExists(String identifier) {
    return getAllIdentifiers().contains(identifier);
  }


  // Given a String identifier which points to a sub-element of this datapackage
  // (for example, a Module, or a sub-tree), verify that it exists, and if so
  // retrieve it from either metacat or local storage
  // @param     identifier                  the unique identifier needed to
  //                                        locate the desired sub-element.
  // @return    a <code>java.io.File</code>
  // @throws    DocumentNotFoundException   if document cannot be found, or
  //                                        if document cannot succesfully be
  //                                        opened and a Reader returned
  private File openAsFile(String identifier) throws  DocumentNotFoundException
  {
    //first check if this identifier points to a
    //valid sub-element (module or subtree)
    if (idExists(identifier)==false)  {
      throw new DocumentNotFoundException("DataPackage.open(): "
                            +"Element with ID: "+identifier+" does not exist");
    }
    //if so, now get the sub-element and return it
    File elementSrcFile = null;
    try {
      elementSrcFile = getFileWithID(identifier);
    } catch (Throwable t) {
      throw new DocumentNotFoundException("DataPackage.open(): "
                            +"Error opening element with ID: "+identifier);
    }
    return elementSrcFile;
  }

  /**
   *  ----------------- REQUIRED BY XMLFactoryInterface  -----------------------
   *
   * Open a sub-element of this datapackage (for example, a Module, or a
   * sub-tree), given its String identifier.
   * @param     identifier                  the unique identifier needed to
   *                                        locate the desired sub-element.
   * @return    a <code>java.io.Reader</code> to allow direct read access
   *
   *  @throws DocumentNotFoundException if id does not point to a document, or
   *          if requested document exists but cannot be accessed.
   */
  Properties params = new Properties();

  public Reader openAsReader(String identifier) throws DocumentNotFoundException
  {
    int             paramIndex, eqIndex;
    String          paramString, nextToken;
    StringTokenizer tokenizer;

    //if it contains a question mark, look for "name=val" pairs:
    paramIndex = identifier.indexOf("?");
    if (paramIndex>=0) {
      params.clear();
      paramString = identifier.substring(1 + paramIndex);
      tokenizer   = new StringTokenizer(paramString,"&");
      while (tokenizer.hasMoreTokens()) {
        nextToken = tokenizer.nextToken();
        eqIndex   = nextToken.indexOf("=");
        //split on "=" and put name, value into Properties object
        if (eqIndex>=0) {
          params.setProperty( nextToken.substring(0,  eqIndex),
                              nextToken.substring(1 + eqIndex) );
          Log.debug(44,"DataPackage.openAsReader() got uri params: "+nextToken);
        }
      }
      //strip params from end so next part of method can
      //open file denoted by "identifier"
      identifier = identifier.substring(0,paramIndex);
    }

    Reader reader = null;
    try {
        reader = new InputStreamReader(new FileInputStream(openAsFile(identifier)), Charset.forName("UTF-8"));
    } catch (Exception ioe) {
        Log.debug(12, "Error instantiating reader "+ioe.getMessage());
        DocumentNotFoundException dnfe =  new DocumentNotFoundException(
         "DataPackage.openAsReader() - Nested Exception: " + ioe);
        dnfe.fillInStackTrace();
        throw dnfe;
    }


    return reader;
  }

  /**
   *   This method is needed by the XMLFactoryInterface
	 *   In this case, null is always returned.
	 */
	public Document openAsDom(String id) {
		return null;
	}

    /**
   * Open a sub-element of this datapackage (for example, a Module, or a
   * sub-tree), given its String identifier.
   * @param     identifier                  the unique identifier needed to
   *                                        locate the desired sub-element.
   * @return    a <code>java.io.InputStream</code> to allow direct read access
   *                                        to the source
   * @throws    DocumentNotFoundException   if document cannot be found
   * @throws    FileNotFoundException       if document cannot succesfully be
   *                                        opened and an InputStream returned
   */
  public InputStream openAsInputStream(String identifier)
                        throws  DocumentNotFoundException, FileNotFoundException
  {
    return new FileInputStream(openAsFile(identifier));
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
    DocumentBuilder parser = morpho.createDomParser();
    Document doc;
    InputSource in;
    FileInputStream fs;
    CatalogEntityResolver cer = new CatalogEntityResolver();

    //get the DOM rep of the document without triples
    try
    {
//      ConfigXML config = morpho.getConfiguration();
      String catalogPath = config.get("local_catalog_path", 0);
      doc = PackageUtil.getDoc(tripleFile, catalogPath);
      tripleFileDom = doc;
    }
    catch (Exception e)
    {
      Log.debug(0, "error parsing " + tripleFile.getPath() + " : " +
                         e.getMessage());
      e.printStackTrace();
      return;
    }

    NodeList originatorNodeList = null;
    NodeList titleNodeList      = null;
    NodeList altTitleNodeList   = null;
    String originatorPath = config.get("originatorPath", 0);
    String titlePath      = config.get("titlePath", 0);
    String altTitlePath   = config.get("altTitlePath", 0);
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
    catch(TransformerException se)
    {
      System.err.println("parseTripleFile : parse threw: " + se.toString());
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
//    ConfigXML config = morpho.getConfiguration();
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
        subfile = fileSysDataStore.openFile(subject.trim());
      }
      catch(FileNotFoundException fnfe)
      {
        try
        {
          subfile = metacatDataStore.openFile(subject.trim());
        }
        catch(FileNotFoundException fnfe2)
        {
          Log.debug(0, "File " + subject + " not found locally or " +
                             "on metacat.");
          return null;
        }
        catch(CacheAccessException cae)
        {
          Log.debug(0, "The cache could not be accessed in "
                                              + "DataPackage.getRelatedFiles.");
          return null;
        }
      }

      try
      { //get the subject files
        Reader fr = new InputStreamReader(new FileInputStream(subfile), Charset.forName("UTF-8"));
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
        Log.debug(10, "error in DataPackage.getRelatedFiles():(subjects) "
                                                              + e.getMessage());
      }

      //now parse the object files
      try
      {
        //try to open the file locally, if it isn't here then try to get
        //it from metacat
        objfile = fileSysDataStore.openFile(object.trim());
      }
      catch(FileNotFoundException fnfe)
      {
        try
        {
          objfile = metacatDataStore.openFile(object.trim());
        }
        catch(FileNotFoundException fnfe2)
        {
          Log.debug(0, "File " + subject + " not found locally or " +
                             "on metacat.");
          return null;
        }
        catch(CacheAccessException cae)
        {
          Log.debug(0, "The cache could not be accessed in "
                                              + "DataPackage.getRelatedFiles.");
          return null;
        }
      }

      try
      { //object files
        Reader fr = new InputStreamReader(new FileInputStream(objfile), Charset.forName("UTF-8"));
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
        Log.debug(10, "error in DataPackage.getRelatedFiles():objects " +
                           e.getMessage());
      }
    }
    return filesHash;
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
   * returns the id of the head of this package (i.e. the resource file)
   */
  public String getID()
  {
    return this.id;
  }

  /**
   * Method to determine if the this pakcage has mutiple versions
   */
  public boolean hasMutipleVersions()
  {
      boolean flag = false;
      int versions = 0;
      int index = id.lastIndexOf(".");
      String ver = id.substring(index+1,id.length());
      versions = (new Integer(ver)).intValue();
      if (versions>1)
      {
        flag = true;
      }
      return flag;
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
        if(location.equals(DataPackageInterface.LOCAL) ||
           location.equals(DataPackageInterface.BOTH))
        { //open the file locally
          f = fileSysDataStore.openFile((String)fileids.elementAt(i));
        }
        else
        { // get the file from metacat
          f = metacatDataStore.openFile((String)fileids.elementAt(i));
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
        fis.close();
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
    // often access control needs to be first
    // so lets go ahead and put it 1st in this vector
    String accessID = getAccessFileIdForDataPackage();
    accessID = accessID.trim();
    int accessLoc = v.indexOf(accessID);
    if (accessLoc>-1) {
      // first remove access element
      v.removeElementAt(accessLoc);
      // now put it at start of vector
      v.insertElementAt(accessID,0);
    }

    return v;
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
   * Deletes the package from the specified location
   * @param locattion the location of the package that you want to delete
   * use either DataPackageInterface.BOTH, DataPackageInterface.METACAT or DataPackageInterface.LOCAL
   */
  public void delete(String location)
  {
    Vector v = this.getAllIdentifiers();
    boolean metacatLoc = false;
    boolean localLoc = false;
    if(location.equals(DataPackageInterface.METACAT) ||
       location.equals(DataPackageInterface.BOTH))
    {
      metacatLoc = true;
    }
    if(location.equals(DataPackageInterface.LOCAL) ||
       location.equals(DataPackageInterface.BOTH))
    {
      localLoc = true;
    }

    for(int i=0; i<v.size(); i++)
    { //loop through and delete all of the files in the package
      try{
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
            fileSysDataStore.deleteFile(acc + "." + j);
            LocalQuery.removeFromCache(acc + "." + j);
          }
        }

        if(metacatLoc)
        {
          metacatDataStore.deleteFile((String)v.elementAt(i));
        }
      }
      catch (Exception w) {
        // just go to next file
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
      throw e;
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
    Vector fileV = new Vector(); //vector of all files in the package
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackageInterface.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
    {
      localloc = true;
    }

    //get a list of the files and save them to the new location. if the file
    //is a data file, save it with its original name.

    String packagePath = path + "/" + id + ".package";
    String sourcePath = packagePath + "/metadata";
    String dataPath = packagePath + "/data";
    File savedir = new File(packagePath);
    File savedirSub = new File(sourcePath);
    File savedirDataSub = new File(dataPath);
    savedir.mkdirs(); //create the new directories
    savedirSub.mkdirs();
    Hashtable dataFileNameMap = getMapBetweenDataIdAndDataFileName();
    Vector files = getAllIdentifiers();
    StringBuffer[] htmldoc = new StringBuffer[files.size()];
    for(int i=0; i<files.size(); i++)
    {
      try
      {
       //save one file at a time
        // Get docid for the vector
        String docid = (String)files.elementAt(i);
        // Get the hasth table between docid and data file name
        File f = null;
        // if it is data file user filename to replace docid
        if (dataFileNameMap.containsKey(docid))
        {
          savedirDataSub.mkdirs();
          String dataFile = (String)dataFileNameMap.get(docid);
          dataFile =trimFullPathFromFileName(dataFile);
          f = new File(dataPath + "/" + dataFile);
        }
        else
        {
          // for metadata file
          f = new File(sourcePath + "/" + docid);
        }
        File openfile = null;
        if(localloc)
        { //get the file locally and save it
          openfile = fileSysDataStore.openFile(docid);
        }
        else if(metacatloc)
        { //get the file from metacat
          openfile = metacatDataStore.openFile(docid);
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

        // for html
        Reader        xmlInputReader  = null;
        Reader        result          = null;
        StringBuffer  tempPathBuff    = new StringBuffer();

        if (!dataFileNameMap.containsKey(docid))
        { //this is an xml file so we can transform it.
          //transform each file individually then concatenate all of the
          //transformations .

            xmlInputReader = new InputStreamReader(new FileInputStream(openfile), Charset.forName("UTF-8"));

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
              htmldoc[i] = IOUtil.getAsStringBuffer(result, true);
              //"true" closes Reader after reading
            } catch (IOException e) {
              e.printStackTrace();
              Log.debug(9,"Unexpected Error Reading Styled Document: "
                                                              +e.getMessage());
              e.fillInStackTrace();
              throw e;
            }
        } else {
          //this is a data file so we should link to it in the html
          htmldoc[i] = new StringBuffer("<html><head></head><body>");
          htmldoc[i].append("<h2>Data File: ");
          htmldoc[i].append(docid);
          htmldoc[i].append("</h2>");

          htmldoc[i].append("<a href=\"");
          String dataFileName = null;
          dataFileName = (String)dataFileNameMap.get(docid);
          dataFileName = trimFullPathFromFileName(dataFileName);
          htmldoc[i].append("./data/").append(dataFileName).append("\">");
          htmldoc[i].append("Data File: ");
          htmldoc[i].append(dataFileName).append("</a><br>");

          htmldoc[i].append("<br><hr><br>");

          htmldoc[i].append("</body></html>");
        }   //end if...else for xml or data file

        tempPathBuff.delete(0,tempPathBuff.length());

        tempPathBuff.append(packagePath);
        tempPathBuff.append("/");
        if (id.equals(docid))
        {
          tempPathBuff.append(METADATAHTML);
        }
        else
        {
          tempPathBuff.append(docid);
        }
        tempPathBuff.append(HTMLEXTENSION);
        saveToFile(htmldoc[i], new File(tempPathBuff.toString()));

      }
      catch(Exception e)
      {
        System.out.println("Error in DataPackage.export(): " + e.getMessage());
        e.printStackTrace();
      }
    }//for
    // export style sheet
    exportStyleSheet(packagePath);

  }


  /**
   * transforms a package to eml2; first exports the metadata
   * @param path the path to which this package should be exported.
   */
  public void exportToEml2(String path)
  {
    Log.debug(20, "exporting...");
    Log.debug(20, "path: " + path);
    Log.debug(20, "id: " + id);
    Log.debug(20, "location: " + location);
    Vector fileV = new Vector(); //vector of all files in the package
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackageInterface.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
    {
      localloc = true;
    }

    //get a list of the files and save them to the new location. if the file
    //is a data file, save it with its original name.

//    String packagePath = path + "/" + id + ".package";
//    String sourcePath = packagePath + "/metadata";
    String sourcePath = "metadata";
//    String dataPath = packagePath + "/data";
//    File savedir = new File(packagePath);
    File savedirSub = new File(sourcePath);
//    File savedirDataSub = new File(dataPath);
//    savedir.mkdirs(); //create the new directories
    savedirSub.mkdirs();
    Hashtable dataFileNameMap = getMapBetweenDataIdAndDataFileName();
    Vector files = getAllIdentifiers();
    StringBuffer[] htmldoc = new StringBuffer[files.size()];
    for(int i=0; i<files.size(); i++)
    {
      try
      {
       //save one file at a time
        // Get docid for the vector
        String docid = (String)files.elementAt(i);
        // Get the hash table between docid and data file name
        File f = null;
        // if it is data file user filename to replace docid
        if (dataFileNameMap.containsKey(docid))
        {
//          savedirDataSub.mkdirs();
//          String dataFile = (String)dataFileNameMap.get(docid);
//          dataFile =trimFullPathFromFileName(dataFile);
//          f = new File(dataPath + "/" + dataFile);
        }
        else
        {
          // for metadata file
          f = new File(sourcePath + "/" + docid);
        }
        File openfile = null;
        if(localloc)
        { //get the file locally and save it
          openfile = fileSysDataStore.openFile(docid);
        }
        else if(metacatloc)
        { //get the file from metacat
          openfile = metacatDataStore.openFile(docid);
        }

        if (f!=null) {
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
      }
      catch(Exception e)
      {
        System.out.println("Error in DataPackage.exportToEml2(): " + e.getMessage());
        e.printStackTrace();
      }
    }//for
    try{
      EMLConvert.outputfileName = path;
      // when the package is on metacat, one wants to use a url pointing to the
      // current metacat. When the package is just local, pass a file url
      String murl = morpho.getMetacatURLString();
      if (!metacatloc) {
        murl = "file://"+id;
      }
      EMLConvert.doTransform("metadata/"+id, murl);
    }
    catch (Exception ee) {
        System.out.println("Error in EMLConvert: " + ee.getMessage());
        ee.printStackTrace();
    }
    // now delete the temp files
    String[] filelist = savedirSub.list();
    for (int k=0;k<filelist.length;k++) {
      File nf = new File(savedirSub, filelist[k]);
      nf.delete();
    }
    savedirSub.delete();

    JOptionPane.showMessageDialog(UIController.getInstance().getCurrentActiveWindow(),
                    "Conversion to EML2 Complete ! ");

  }

  /*
   * A method to trim the full path of data file name.(old version of morpho
   * will have full path in the name)
   * We assume the full path name either only has "/" or "\"
   */
  private String trimFullPathFromFileName(String fileName)
  {
    String onlyFileName = fileName;// assing fileName to onlyFileName;
    String slash = "/";
    String backSlash = "\\";
    if (fileName == null || fileName.equals(""))
    {
      return fileName;
    }
    int size               = fileName.length();
    int lastBackSlashIndex = fileName.lastIndexOf(backSlash);
    int lastSlashIndex     = fileName.lastIndexOf(slash);
    // If have a backslash, we think it has a full path, only get part
    // from the last back slash
    if (lastBackSlashIndex != -1)
    {
      onlyFileName = fileName.substring(lastBackSlashIndex+1, size);
      return onlyFileName;
    }
    // If have a slash, we think it has a full path, only get part
    // from the last slash
    if (lastSlashIndex != -1)
    {
      onlyFileName = fileName.substring(lastSlashIndex+1, size);
      return onlyFileName;
    }
    // If already no path, just return fileName
    return onlyFileName;
  }
  //save the StringBuffer to the File path specified
  private void saveToFile(StringBuffer buff, File outputFile) throws IOException
  {
    Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), Charset.forName("UTF-8")));
    IOUtil.writeToWriter(buff, fileWriter, true);
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

  /*
   * Method to get the map between data docid to data file name (oringinal)
   */
  private Hashtable getMapBetweenDataIdAndDataFileName()
  {
    Hashtable map = new Hashtable();
    Vector triplesV = triples.getCollection();
    int i = 1;
    String dataFileName = null;
    for(int j=0; j<triplesV.size(); j++)
    {
      Triple triple = (Triple)triplesV.elementAt(j);
      String relationship = triple.getRelationship();
      String subject = triple.getSubject();
      if(relationship.indexOf("isDataFileFor") != -1)
      {
        //get the name of the data file.
        int lparenindex = relationship.indexOf("(");
        dataFileName = relationship.substring(lparenindex + 1,
                                                     relationship.length() - 1);
        dataFileName = trimFullPathFromFileName(dataFileName);
        if (dataFileName != null)
        {
          // check file name if conflic
          if (map.containsValue(dataFileName))
          {
            dataFileName =appendFileNameNumber(dataFileName, i);
            i++;
          }//if
          map.put(subject, dataFileName);
        }//if
      }//if
    }//for
    return map;
  }

  /*
   * A method to append file name a number, not in extension
   */
  private String appendFileNameNumber(String fileName, int number)
  {
    int index = -1;
    String dot = ".";
    String extension = null;
    String prefix = null;
    if (fileName == null || fileName.equals(""))
    {
      fileName = ""+number;
      return fileName;
    }
    int size = fileName.length();
    index = fileName.lastIndexOf(dot);

    if (index == -1)
    {
      // no extension
      fileName = fileName+number;
      return fileName;
    }
    else
    {
      extension = fileName.substring(index+1, size);
      prefix = fileName.substring(0, index);
      fileName = prefix + number + dot + extension;
      return fileName;
    }

  }

  /*
   * A method to export a style sheet - export.css which will be used in html
   */
  private void exportStyleSheet(String path)
  {
    String styleSheetSource = getStylePath();
    // create a reader
    try
    {
      InputStream input = this.getClass().getResourceAsStream(styleSheetSource);
      InputStreamReader styleSheetReader = new InputStreamReader(input);
      //FileReader styleSheetReader = new FileReader(styleSheetSource);
      StringBuffer buffer = IOUtil.getAsStringBuffer(styleSheetReader, true);
      // Create a wrter
      String fileName = path + "/"+ EXPORTSYLE + EXPORTSYLEEXTENSION;
      Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), Charset.forName("UTF-8")));
      IOUtil.writeToWriter(buffer, writer, true);
    }
    catch (Exception e)
    {
      Log.debug(30, "Error in exportStyleSheet: "+e.getMessage());
    }

  }
  /*
   * returns string representation of full path to style directory in
   * Morpho-Config.jar.  All names are in config.xml file
   */
  private String getStylePath()
  {
        StringBuffer pathBuff = new StringBuffer();
        pathBuff.append("/");
        pathBuff.append(config.get(CONFIG_KEY_CSS_LOCATION, 0));
        pathBuff.append("/");
        pathBuff.append(EXPORTSYLE);
        pathBuff.append(EXPORTSYLEEXTENSION);
        Log.debug(50,"DataPackage.getFullStylePath() returning: "
                                                          +pathBuff.toString());
        return pathBuff.toString();
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


  /**
   *    returns the original text name of the datafile, which for EML beta6 is
   *    contained in the triples relationship string, inside parentheses)
   *
   *    @param entityID the ID of the entity for which the datafile text name is
   *                    sought
   *
   *    @return         the original text name of the datafile (as a String)
   */

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
              return dataFileName;
            }
          }
        }
      }
    String resultString = null;
    if (result) resultString = dataFileName;
    return resultString;
  }

  public File getPhysicalFile(String entityID) {
     if (entity2PhysicalFile.containsKey(entityID)) {
      return ((File)entity2PhysicalFile.get(entityID));
    }
   File physicalfile = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackageInterface.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
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
         if (physicalfile!=null) {
           if (!entity2PhysicalFile.contains(entityID)) {
             entity2PhysicalFile.put(entityID, physicalfile);
           }
           return physicalfile;
         }
    }
    return physicalfile;
  }

  public String getPhysicalFileId(String entityID) {
    File physicalfile = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackageInterface.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
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
         if (physicalfile!=null) return sub;

    }
    return null;
  }


  public File getAttributeFile(String entityID) {
    if (entity2AttributeFile.containsKey(entityID)) {
      return ((File)entity2AttributeFile.get(entityID));
    }
    File attributefile = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackageInterface.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
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
         if (attributefile!=null) {
           if (!entity2AttributeFile.contains(entityID)) {
             entity2AttributeFile.put(entityID, attributefile);
           }
           return attributefile;
         }
    }

    return attributefile;
  }


    public String getAttributeFileId(String entityID) {
    File attributefile = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackageInterface.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
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
         if (attributefile!=null) return sub;
    }


    return null;
  }

  /**
   * get the id of the access doc for the indicated id
   */
  public String getAccessFileId(String id) {
    File accessfile = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackageInterface.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
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
    if (entity2DataFile.containsKey(entityID)) {
      return ((File)entity2DataFile.get(entityID));
    }
    File datafile = null;
    boolean localloc = false;
    boolean metacatloc = false;
    if(location.equals(DataPackageInterface.BOTH))
    {
      localloc = true;
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
    {
      localloc = true;
    }
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
              if(localloc)
              { //get the file locally and save it
                try {
                  datafile = fileSysDataStore.openFile(dataFileID);
                  if (!entity2DataFile.contains(entityID)) {
                    entity2DataFile.put(entityID, datafile);
                  }
                  return datafile;

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
                  datafile = metacatDataStore.openFile(dataFileID);
                  if (!entity2DataFile.contains(entityID)) {
                    entity2DataFile.put(entityID, datafile);
                  }
                  return datafile;
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
    return datafile;
  }


  /**
   *    returns the ID (i.e. the dentifier/version number from the subject/object of
   *    the triples) for the data file
   *
   *    @param entityID the ID of the entity for which the datafile ID is sought
   *
   *    @return         the ID (as a String)
   */
  public String getDataFileID(String entityID) {
    String dataFileID = "";
      Vector triplesV = triples.getCollection();
      // first find out if there are ANY data files
      for(int i=0; i<triplesV.size(); i++)
      {
        Triple triple = (Triple)triplesV.elementAt(i);
        String relationship = triple.getRelationship();
        if(relationship.indexOf("isDataFileFor") != -1)
        {
          String dFileID = triple.getSubject();
          // now see if entity file points to this data file
          for (int j=0; j<triplesV.size();j++) {
            Triple tripleA = (Triple)triplesV.elementAt(j);
            if ((tripleA.getSubject().equals(entityID))&&(tripleA.getObject().equals(dFileID))) {
               try {
                  dataFileID = dFileID;
                  return dataFileID;
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
    return dataFileID;
  }

  public File getFileFromId(String id) {
    File xmlFile = null;
    try
    {
      if(location.equals(DataPackageInterface.LOCAL) ||
         location.equals(DataPackageInterface.BOTH))
      {
        FileSystemDataStore fsds = new FileSystemDataStore(morpho);
        xmlFile = fsds.openFile(id);
        return xmlFile;
      }
      else
      {
        MetacatDataStore mds = new MetacatDataStore(morpho);
        xmlFile = mds.openFile(id);
        return xmlFile;
      }
    }
    catch(FileNotFoundException fnfe)
    {
      Log.debug(0, "Error reading file : " + xmlFile + " " +
                         fnfe.getMessage() + "---File NOT found.");
    }
    catch(CacheAccessException cae)
    {
      Log.debug(0, "Error reading file : " + xmlFile + " " +
                         cae.getMessage() + "---Cache could not be accessed");
    }
    return null;
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
//    ConfigXML config = morpho.getConfiguration();
/*    if (getFileTypeHash.containsKey(id)) {
      if ((File)getFileTypeHash.get(id)!=nullfile) {
        System.out.println("hash hit 1");
        return ((File)getFileTypeHash.get(id));
      }
      else {
        System.out.println("hash hit null");
        return null;
      }
    }
*/
    String catalogPath = //config.getConfigDirectory() + File.separator +
                                     config.get("local_catalog_path", 0);
    File subfile;
    String name = "unknown";
      try
      {
        //try to open the file locally, if it isn't here then try to get
        //it from metacat
        subfile = fileSysDataStore.openFile(id.trim());
      }
      catch(FileNotFoundException fnfe)
      {
        try
        {
          subfile = metacatDataStore.openFile(id.trim());
        }
        catch(FileNotFoundException fnfe2)
        {
          Log.debug(0, "File " + id + " not found locally or " +
                             "on metacat.");
          return null;
        }
        catch(CacheAccessException cae)
        {
          Log.debug(0, "The cache could not be accessed in " +
                             "DataPackage.getRelatedFiles.");
          return null;
        }
      }

      try
      {
        Reader fr = new InputStreamReader(new FileInputStream(subfile), Charset.forName("UTF-8"));
        String xmlString = "";
        for(int j=0; j<5; j++)
        {
          xmlString += (char)fr.read();
        }
        fr.close();
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
 //     System.out.println("Name: "+name);
      if (name.indexOf(typeString)>-1)   // i.e. PublicId contains typeString
      {
//        getFileTypeHash.put(id, subfile);
        return subfile;
      } else {
        subfile = null;
      }
      // save in hash
/*      if (subfile !=null) {
        getFileTypeHash.put(id, subfile);
      }
      else {
        getFileTypeHash.put(id, nullfile);
      }
*/
      return subfile;
  }

  public void checkTriplesForAccess() {
    String accessId = null;
    try{
      accessId = getAccessId();
    }
    catch (Exception e) {}
    Vector ids = getAllIdentifiers();
    Vector missingIds = new Vector();
    String temp = "";
    if (accessId!=null) {
      for (int i=0;i<ids.size();i++) {
        String obj = (String)ids.elementAt(i);
        temp = temp + obj + "::";
        if (!subjectAndObjectCheck(accessId, obj)) {
          missingIds.addElement(obj);
        }
      }
    }
    else {
      // case where there is no access control file for package
      Log.debug(11, "No access file in package!!!!");
    }
    if (missingIds.size()>0) {
      int choice = JOptionPane.showConfirmDialog(UIController.getInstance().getCurrentActiveWindow(),
                   "This package is missing some access\n"+
                   "control information, possibly\n"+
                   "because it was created using an\n"+
                   "earlier version of Morpho.\n"+
                   "Would you like to update the\n"+
                   "package with the missing\n"+
                   "information (recommended)?\n\n"+
                   "(Changes will appear the next\n"+
                   "time the package is opened.)",
                   "Access Information Missing ",
                   JOptionPane.YES_NO_OPTION,
                   JOptionPane.WARNING_MESSAGE);
      if (choice == JOptionPane.YES_OPTION) {
        fixMissingAccess(accessId, missingIds);
      }
    }
  }

  /* should return true if a triple exists with the indicated
   * subject and object
   */
  private boolean subjectAndObjectCheck(String sub, String obj) {
    boolean res = false;
    TripleCollection tc = getTriples();
    Vector triples = tc.getCollection();
    for (int i=0;i<triples.size();i++) {
      Triple tpl = (Triple)triples.elementAt(i);
      if ((sub.equals(tpl.getSubject())) &&
      (obj.equals(tpl.getObject()))) {
        res = true;
        return res;
      }
    }
    return res;
  }

  private void fixMissingAccess(String accessId, Vector missingIds) {
    String docString;
    TripleCollection newTriples = new TripleCollection();
    for (int i=0;i<missingIds.size();i++) {
      Triple tr = new Triple(accessId, "provides access control rules for",
                                       (String)missingIds.elementAt(i));
      newTriples.addTriple(tr);
    }
    AccessionNumber a = new AccessionNumber(morpho);
    FileSystemDataStore fsds = new FileSystemDataStore(morpho);
    docString = PackageUtil.addTriplesToTriplesFile(newTriples, this,
                                                      morpho);
    File newDPTempFile;
    //get a new id for the package file
    String dataPackageId = a.incRev(this.getID());
    try
    { //this handles the package file
      //save a temp file with the new id
      newDPTempFile = fsds.saveTempFile(dataPackageId,
                                    new StringReader(docString));
      //inc the revision of the new Package file in the triples
      docString = a.incRevInTriples(newDPTempFile, this.getID(),
                                    dataPackageId);
      //save new temp file that has the right id and the id inced in the triples
      newDPTempFile = fsds.saveTempFile(dataPackageId,
                                    new StringReader(docString));
    }
    catch(Exception e)
    {
      Log.debug(0, "Error saving file(5): " + e.getMessage());
      e.printStackTrace();
      return;
    }
    String location = this.getLocation();
    boolean locMetacat = false;
    boolean locLocal = false;
    if(location.equals(DataPackageInterface.LOCAL) ||
       location.equals(DataPackageInterface.BOTH))
    {
      locLocal = true;
    }

    if(location.equals(DataPackageInterface.METACAT) ||
       location.equals(DataPackageInterface.BOTH))
    {
      locMetacat = true;
    }

    if(locLocal) {
      try
      { //save the new package file
        fsds.saveFile(dataPackageId, new InputStreamReader(new FileInputStream(newDPTempFile), Charset.forName("UTF-8")));
      }
      catch(Exception e)
      {
        Log.debug(0, "Error saving file(3): " + e.getMessage());
        e.printStackTrace();
        return;
      }
    }
    if(locMetacat) {
      MetacatDataStore mds = new MetacatDataStore(morpho);
      try
      {
        mds.saveFile(dataPackageId, new InputStreamReader(new FileInputStream(newDPTempFile), Charset.forName("UTF-8")));
      }
      catch(Exception e)
      {
        Log.debug(0, "Error saving file(2): " + e.getMessage());
        e.printStackTrace();
        return;
      }
    }

  }
}
