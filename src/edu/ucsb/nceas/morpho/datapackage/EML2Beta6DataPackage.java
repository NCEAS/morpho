/**
 *  '$RCSfile: EML2Beta6DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-11-20 20:40:20 $'
 * '$Revision: 1.5 $'
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
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;

import edu.ucsb.nceas.utilities.*;

import java.util.Vector;
import java.util.Hashtable;
import java.io.*;
import javax.swing.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;

/**
 * class that represents a data package. This class is abstract. Specific datapackages
 * e.g. eml2, beta6., etc extend this abstact class
 */
public  class EML2Beta6DataPackage extends AbstractDataPackage
{
  private Morpho            morpho;
  private TripleCollection  triples;
  private File              tripleFile;

  
  public void serialize() {
    
  }
  
  public void load(String location, String identifier, Morpho morpho) {
    this.morpho = morpho;
    this.location = location;
    this.config = morpho.getConfiguration();
    id = identifier;
    //read the file containing the triples - usually the datapackage file:
    try {
      tripleFile = getFileWithID(identifier, morpho);
      triples = new TripleCollection(tripleFile, morpho);
    } catch (Throwable t) {
      //already handled in getFileWithID() method, 
      //so just abandon this instance:
      Log.debug(1, "Unable to get tripleFile!!!");
      return;
    }
//Log.debug(1,"ready to create eml2 file");
    exportToEml2("eml2test");

    File packagefile = new File("eml2test");
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
   
    String sourcePath = "metadata";
    File savedirSub = new File(sourcePath);
    savedirSub.mkdirs();
    Hashtable dataFileNameMap = getMapBetweenDataIdAndDataFileName();
    Vector files = getAllIdentifiers();
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
    
    JOptionPane.showMessageDialog(null,
                    "Conversion to EML2 Complete ! ");
    
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
  
  private String getAccessFileIdForDataPackage() {
    String temp = getAccessFileId(this.id); 
    return temp;
  }

    
  private File getFileType(String id, String typeString) {
    String catalogPath = config.get("local_catalog_path", 0);
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
        FileReader fr = new FileReader(subfile);
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
      if (name.indexOf(typeString)>-1)   // i.e. PublicId contains typeString
      {
        return subfile;
      } else {
        subfile = null;
      }
      return subfile;
  }

    
}

