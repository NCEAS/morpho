/**
 *  '$RCSfile: MetacatDataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-12-19 23:58:56 $'
 * '$Revision: 1.19 $'
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.ant.shaded.IOUtil;
import org.dataone.client.DataPackage;
import org.dataone.client.MNode;
import org.dataone.ore.ResourceMapFactory;
import org.dataone.service.exceptions.InsufficientResources;
import org.dataone.service.exceptions.InvalidRequest;
import org.dataone.service.exceptions.InvalidToken;
import org.dataone.service.exceptions.NotAuthorized;
import org.dataone.service.exceptions.NotFound;
import org.dataone.service.exceptions.NotImplemented;
import org.dataone.service.exceptions.ServiceFailure;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.SystemMetadata;
import org.dataone.service.util.TypeMarshaller;
import org.dspace.foresite.OREException;
import org.dspace.foresite.OREParserException;
import org.jibx.runtime.JiBXException;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierFileMap;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.utilities.Log;

/**
 * Implements the DataStoreServiceInterface to access the files on the DataOne service
 * @author tao
 *
 */
public class DataONEDataStoreService extends DataStoreService implements DataStoreServiceInterface {

  
  private static final String MNDOEURLELEMENTNAME = "dataone_mnode_baseurl";
  private static final String PATHQUERY = "pathquery";
  private static final String DOI = "doi";
  private MNode activeMNode = null;
  
  
  /**
   * Constructor. It will create a member node from the configuration file.
   * @param morpho
   */
  public DataONEDataStoreService(Morpho morpho) {
    super(morpho);
    String mNodeBaseURL = Morpho.getConfiguration().get(MNDOEURLELEMENTNAME, 0);
    activeMNode = new MNode(mNodeBaseURL);
  }
  
  /**
   * Get the active member node.
   * @return the active member node.
   */
  public MNode getActiveMNode() {
    return activeMNode;
  }
  
  
  /**
   * Set the specified node to be the active member node.
   * @param activeMNode - the node will be set.
   */
  public void setActiveMNode(MNode activeMNode) {
    this.activeMNode = activeMNode;
  }
  
  /**
   * open a file from a datastore with the id of name and return a File
   * object that represents it.  Throws FileNotFoundException if a file
   * with the id name does not exist in the datastore.  Throws IOException
   * if a there is a communications problem with the datastore.
   */
  public File openFile(String name) throws FileNotFoundException, 
                                           CacheAccessException {
    return null;
  }
  
  /**
   * save a file to the datastore with the id of name.  a file object 
   * representing the saved file is returned.  If the publicAccess boolean
   * is true, then an unauthenticated user may read the document from the 
   * data store.
   */
  public File saveFile(String name, InputStream inputStream)
    throws Exception {
    return null;
  }
  
  
  /**
   * Save a data file to the store
   * @param id
   * @param file
   * @param objectName
   * @throws Exception
   */
  public void newDataFile(String id, File file, String objectName) 
  throws Exception {
    
  }
  
  /**
   * create a new file with an id of name in the datastore and return a File
   * object that represents it.
   */
  public File newFile(String name, InputStream inputStream)
    throws Exception {
    return null;
  }
  
  /**
   * Deletes the file with given name.  returns true if the file was 
   * successfully deleted, false otherwise
   */
  public boolean deleteFile(String name) throws Exception {
    return false;
  }
  
  /**
   * Retrieve a MorphoDataPackage for the given identifier
   * @param identifier - the identifier of the ore document
   * @return the MorphoDataPackage object
   * @throws FileNotFoundException
   * @throws CacheAccessException
   */
  @Override
  public MorphoDataPackage read(String identifier) throws InvalidToken, ServiceFailure, 
             NotAuthorized, NotFound, NotImplemented, InsufficientResources, FileNotFoundException, IOException {
    File ore = getData(identifier);
    String oreContent = IOUtil.toString(new FileInputStream(ore), IdentifierFileMap.UTF8);
    DataPackage dataPackage = DataPackage.deserializePackage(oreContent);
    return null;
  }
  
  
  /*
   * Get the data - get it from the cache first.If it fails, get it from the dataone server.
   */
  private File getData(String identifier) throws InvalidToken, ServiceFailure, 
                      NotAuthorized, NotFound, NotImplemented, InsufficientResources, FileNotFoundException {
    File file = null;
    try {
      file = FileSystemDataStore.getInstance(getCacheDir()).get(identifier);
    } catch (Exception e) {
      Log.debug(30, "DataONEDataStoreService.getData - identifier "+identifier+" doesn't exist in the cache "+e.getMessage()+
                 ". So morpho will try to get it from the dataone server.");
      file = getDataFromDataONE(identifier);
    }
    return file;
  }
  
  /*
   * Get the data - get it from the cache first.If it fails, get it from the dataone server.
   */
  private SystemMetadata getSystemMetadata(String identifier)  throws InvalidToken, ServiceFailure, 
                                             NotAuthorized, NotFound, NotImplemented, InsufficientResources {  
    SystemMetadata sysMeta = null;
    try {
      File file = FileSystemDataStore.getInstance(getSystemMetadataDir(getCacheDir())).get(identifier);
      sysMeta = TypeMarshaller.unmarshalTypeFromStream(SystemMetadata.class, new FileInputStream(file));
    } catch (Exception e) {
      Log.debug(30, "DataONEDataStoreService.getSystemMetadataFromDataONE - can't get the system metadata for identifier "+identifier+" from the cache: "+e.getMessage()+
                 ".\nSo morpho will try to get it from the dataone server.");
      sysMeta = getSystemMetadataFromDataONE(identifier);
    }
    return sysMeta;
    
  }
  
  /*
   * Get the data object from the dataone network and cache it.
   */
  private File getDataFromDataONE(String identifier) throws InvalidToken, ServiceFailure, 
                             NotAuthorized, NotFound, NotImplemented, InsufficientResources, FileNotFoundException {
    File file = null;
    Identifier pid = new Identifier();
    pid.setValue(identifier);
    InputStream respond = activeMNode.get(pid);
    //cache it
    file = LocalDataStoreService.saveFile(identifier, respond, getCacheDir());
    return file;
  }
  
  /*
   * Get the system metadata from the dataone for the id. It also caches it.
   */
  private SystemMetadata getSystemMetadataFromDataONE(String identifier) throws InvalidToken, ServiceFailure, 
                                             NotAuthorized, NotFound, NotImplemented, InsufficientResources {   
    Identifier pid = new Identifier();
    pid.setValue(identifier);
    SystemMetadata systemMetadata = activeMNode.getSystemMetadata(pid);
    //cache it
    File tmp = null;
    FileOutputStream output = null;
    try {
      tmp = File.createTempFile("tmp-systemmeta", null);
      output = new FileOutputStream(tmp);
      TypeMarshaller.marshalTypeToOutputStream(systemMetadata, output);
      LocalDataStoreService.saveFile(identifier, new FileInputStream(tmp), getCacheDir());
    } catch (Exception e) {
      Log.debug(20, "DataOneDataStoreService.getSystemMetadataFromDataone - Morpho couldn't cache the system metadata for id "+identifier+" because "+e.getMessage());
    } finally {
      try {
        if(output != null) {
          output.close();
        }
        if(tmp != null) {
          tmp.delete();
        }
      } catch (IOException e) {
        Log.debug(30, "DataONEDataStoreService.getSystemMetadataFromDataONE - couldn't remove the tmp file which contains the system metadata since "+e.getMessage());
      }
      
    }
    
    return systemMetadata;
  }
  
  /**
   * Save the MorphoDataPackage object into the local data store.
   * @param mdp - the object will be saved
   * @return the identifier of saved object
   * TODO: need to implement it.
   */
  @Override
  public String save(MorphoDataPackage mdp) throws Exception {
    String identifier = null;
    return identifier;
  }
  
  /**
   * Does this id exist in Dataone network
   * @param identifier - the specified identifier.
   * @return true if it exists; false otherwise.
   */
  public boolean exists(String identifier) throws ServiceFailure, NotImplemented, NotAuthorized, InvalidToken {
    boolean existing = true;
    try {
      Identifier id = new Identifier();
      id.setValue(identifier);
      activeMNode.getSystemMetadata(id);
    } catch (NotFound e) {
      existing = false;
    }
    return existing;
  }
  
  /**
   * Delete given ADP from the dataone member node.
   */
  @Override
  public boolean delete(MorphoDataPackage mdp) throws InvalidToken, ServiceFailure, 
                                            NotAuthorized, NotFound, NotImplemented {
    boolean success = false;
    Identifier oreId = mdp.getPackageId();
    activeMNode.delete(oreId);
    Identifier metadataId = mdp.getAbstractDataPackage().getIdentifier();
    activeMNode.delete(metadataId);
    Set<Identifier> identifiers = mdp.identifiers();
    if(identifiers != null && identifiers.size() > 0 ) {
      for(Identifier identifier : identifiers) {
        if(!identifier.equals(oreId) && !identifier.equals(metadataId)) {
          activeMNode.delete(identifier);
        }

      }
      success = true;
    }
    return success;
  }
  
  /**
   * Generate identifier from the dataone member node.
   * @return
   */
  @Override
  public String generateIdentifier(String fragment) throws InvalidToken, ServiceFailure, NotAuthorized,
                                            NotImplemented, InvalidRequest {
    String scheme = DOI;
    Identifier identifier = activeMNode.generateIdentifier(scheme, fragment);
    return identifier.getValue();
  }
  
  /** Send the given query to the Dataone member node, get back the XML InputStream
   * @param the SOLR query 
   * @return the result of the query
   */
  @Override
  public InputStream query(String query) throws InvalidToken, ServiceFailure, NotAuthorized, 
                                         InvalidRequest, NotImplemented, NotFound {
    return activeMNode.query(PATHQUERY, query);
  }
 
}
