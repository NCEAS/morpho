/**
 *  '$RCSfile: EML2Beta6DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: anderson $'
 *     '$Date: 2006-02-06 19:37:13 $'
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.plugins.IncompleteDocInfo;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.triple.Triple;
import edu.ucsb.nceas.utilities.triple.TripleCollection;

/**
 * class that represents a data package. This class is abstract. Specific datapackages
 * e.g. eml2, beta6., etc extend this abstact class
 */
public class EML2Beta6DataPackage extends AbstractDataPackage
{
  private Morpho            morpho;
  private TripleCollection  triples;
  private File              tripleFile;


  public EML2Beta6DataPackage() {
    morpho = Morpho.thisStaticInstance;
  }

  public void load(org.xml.sax.InputSource in) {
      // TODO: maybe want to implement (not sarcasm)
      Log.debug(15, "Loading from XML is not supported by EML2Beta6DataPackage");

  }
  
  /**
   * Not implemented
   * @return String
   */
    @Override
  public String getXMLNamespace() {
      return null;
  }
  
  /**
   * returns a vector containing a distinct set of all of the file ids that make
   * up this package
   *
   * @return Vector
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


  /**
   * get the id of the access doc for the indicated id
   *
   * @param id String
   * @return String
   */
  public String getAccessFileId(String id) {
    File accessfile = null;
    
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
    	  subfile = DataStoreServiceController.getInstance().openFile(id.trim(), getLocation());
      }
      catch(Exception fnfe)
      {
        try
        {
      	  subfile = DataStoreServiceController.getInstance().openFile(id.trim(), DataPackageInterface.NETWORK);
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
      if (name.indexOf(typeString)>-1)   // i.e. PublicId contains typeString
      {
        return subfile;
      } else {
        subfile = null;
      }
      return subfile;
  }


  /**
   * This method follows the pointer stored in 'references' node to return the
   * DOM node referred to by 'references' This is really specific to eml2; thus
   * just returns input
   *
   * @param node Node
   * @return Node
   */
  public Node getReferencedNode(Node node) {
    return node;
  }

  /**
   * returns cloned root Node of subtree identified by the passed unique String
   * refID; returns null if not found
   *
   * @param refID unique String refID
   * @return  cloned root Node of subtree, or null if refID not found
   */
  public Node getSubtreeAtReference(String refID) {
    return null;
  }

  /**
   * returns pointer to root Node of subtree identified by the passed unique
   * String refID; returns null if not found
   *
   * @param refID unique String refID
   * @return  pointer to root Node of subtree, or null if refID not found
   */
  public Node getSubtreeAtReferenceNoClone(String refID) {

    throw new java.lang.UnsupportedOperationException(
      "EML2Beta6DataPackage - method not implemented - "
     +"getSubtreeAtReferenceNoClone()");
  }

 public boolean ignoreConsecutiveDelimiters(int entityIndex, int physicalIndex) {
   return false;
 }


  /**
   * replaces subtree identified by the passed unique String refID; returns null
   * if not found. Note that the new subtree will be given the same refID as the
   * subtree it replaces, even if the newSubtreeRoot node has a different id set
   *
   * @param refID unique String refID. Note that the new subtree will be given
   *   the same refID as the subtree it replaces, even if the newSubtreeRoot
   *   node has a different id set
   * @param newSubtreeRoot Node
   * @return root Node of new subtree, or null if refID not found
   */
  public Node replaceSubtreeAtReference(String refID, Node newSubtreeRoot) {

    throw new java.lang.UnsupportedOperationException(
      "EML2Beta6DataPackage - method not implemented - "
     +"replaceSubtreeAtReference()");
  }


  /**
   * returns a List of pointers to subtrees that reference (the subtree
   * identified by) the passed refID.
   * More formally, returns a List of pointers to subtree root Nodes, where each
   * subtree root Node contains a "references" child-node, and the content
   * String of the references child-node matches the unique String refID passed
   * to this method; returns an empty List if none found. Should never return
   * null;
   *
   * @param refID unique String refID
   * @return List of pointers to subtrees that reference the subtree identified
   * by the passed refID. Returns an empty List if none found. Should never
   * return null;
   */

  public List getSubtreesThatReference(String refID) {

    throw new java.lang.UnsupportedOperationException(
      "EML2Beta6DataPackage - method not implemented - "
     +"getSubtreesThatReferences()");
  }
  
  /**
   * Read the import attribute information from incomplete additionMetadata part.
   */
  public void readImportAttributeInfoFromIncompleteDocInEntityWizard() throws Exception
  {
	  throw new java.lang.UnsupportedOperationException(
		      "EML2Beta6DataPackage - method not implemented - "
		     +"readImportAttributeInfoFromIncompleteDoc()");
  }
  
  
  
  /**
   * Removes the information on additional metadata for incomplete entity
   */
  public void removeInfoForIncompleteEntity()
  {
	  throw new java.lang.UnsupportedOperationException(
		      "EML2Beta6DataPackage - method not implemented - "
		     +"removeInfoForIncompleteEntity()");  
  }
  
  
  /**
   * Removes the information on additional metadata for incomplete code-definition
   * 
   */
  public void removeInfoForIncompleteCodeDef()
  {
    throw new java.lang.UnsupportedOperationException(
          "EML2Beta6DataPackage - method not implemented - "
         +"removeInfoForIncompleteCodeDef()");  
  }
 
  /**
   * Read the import attribute information from incomplete additionMetadata part for code-definition wizard
   */
  public void readImportAttributeInfoFromIncompleteDocInCodeDefWizard() throws Exception
  {
    throw new java.lang.UnsupportedOperationException(
        "EML2Beta6DataPackage - method not implemented - "
       +"readImportAttributeInfoFromIncompleteDocInCodeDefWizard()");  
  }
  
  /**
   * Read the import attribute information from incomplete additionMetadata part.
   * @return the IncompleteDocInfo contains the info morpho needs.
   */
  public IncompleteDocInfo readIncompleteDocInformation() throws Exception
  {
    throw new java.lang.UnsupportedOperationException(
        "EML2Beta6DataPackage - method not implemented - "
       +"readIncompleteDocInformation()");  
  }
  
  /**
   * Gets the status of the completion of this package 
   * @return three status - completed, incomplete(new package wizard) or incomplete(text import wizard)
   */
  public String getCompletionStatus()
  {
    throw new java.lang.UnsupportedOperationException(
        "EML2Beta6DataPackage - method not implemented - "
       +"getCompletionStatus()");  
  }
  
  /**
   * Removes the information on additional metadata for incomplete data package
   */
  public void removeInfoForIncompletePackage()
  {
    throw new java.lang.UnsupportedOperationException(
        "EML2Beta6DataPackage - method not implemented - "
       +"removeInfoForIncompletePackage");  
  }
  
  /**
   *If this package for tracing the change
   * @return true if it is for tracing the change
   */
  public boolean isTracingChange()
  {
    throw new java.lang.UnsupportedOperationException(
        "EML2Beta6DataPackage - method not implemented - "
       +"isTracingChange");  
  }
  
  /**
   * Remove the tracingChangeElement from package tree.
   */
  public void removeTracingChangeElement()
  {
    throw new java.lang.UnsupportedOperationException(
        "EML2Beta6DataPackage - method not implemented - "
       +"removeTracingChangeElement");  
  }
  
}

