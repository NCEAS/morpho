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
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.dataone.client.D1Object;
import org.dataone.client.DataPackage;
import org.dataone.client.MNode;
import org.dataone.ore.ResourceMapFactory;
import org.dataone.service.exceptions.IdentifierNotUnique;
import org.dataone.service.exceptions.InsufficientResources;
import org.dataone.service.exceptions.InvalidRequest;
import org.dataone.service.exceptions.InvalidSystemMetadata;
import org.dataone.service.exceptions.InvalidToken;
import org.dataone.service.exceptions.NotAuthorized;
import org.dataone.service.exceptions.NotFound;
import org.dataone.service.exceptions.NotImplemented;
import org.dataone.service.exceptions.ServiceFailure;
import org.dataone.service.exceptions.UnsupportedType;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.SystemMetadata;
import org.dataone.service.util.TypeMarshaller;
import org.dspace.foresite.OREException;
import org.dspace.foresite.OREParserException;
import org.jibx.runtime.JiBXException;

import com.sun.org.apache.xml.internal.serializer.utils.Utils;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.Entity;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.datastore.idmanagement.DataONERevisionManager;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierFileMap;
import edu.ucsb.nceas.morpho.datastore.idmanagement.RevisionManager;
import edu.ucsb.nceas.morpho.exception.IllegalActionException;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.utilities.Log;

/**
 * Implements the DataStoreServiceInterface to access the files on the DataOne service
 * @author tao
 *
 */
public class DataONEDataStoreService extends DataStoreService implements DataStoreServiceInterface {

  
  private static final String MNDOEURLELEMENTNAME = "dataone_mnode_baseurl";
  private static final String CREATE = "create";
  private static final String UPDATe = "update";
  private static final String PATHQUERY = "pathquery";
  private static final String DOI = "doi";
  private static MNode activeMNode = null;
  
  
  /**
   * Constructor. It will create a member node from the configuration file.
   * @param morpho
   */
  public DataONEDataStoreService(Morpho morpho) {
    super(morpho);
    init();
  }
  
  /*
   * Initialize the mnode.
   */
  private static void init() {
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
             NotAuthorized, NotFound, NotImplemented, InsufficientResources, FileNotFoundException, 
             IOException, OREException, URISyntaxException, OREParserException, InvalidRequest{
    //File ore = getData(identifier);
    InputStream ore = getDataFromDataONE(identifier);
    String oreContent = IOUtils.toString(ore, IdentifierFileMap.UTF8);
    //DataPackage dataPackage = DataPackage.deserializePackage(oreContent);
    Map<Identifier, Map<Identifier, List<Identifier>>> packageMap = 
        ResourceMapFactory.getInstance().parseResourceMap(oreContent);

    MorphoDataPackage dp = new MorphoDataPackage();
    Identifier packageId = new Identifier();
    packageId.setValue(identifier);
    dp.setPackageId(packageId);
    if (packageMap != null && !packageMap.isEmpty()) {

      // Get and store the package Identifier in a new DataPackage
      Identifier pid = packageMap.keySet().iterator().next();

      // Get the Map of metadata/data identifiers
      Map<Identifier, List<Identifier>> mdMap = packageMap.get(pid);
      dp.setMetadataMap(mdMap);
      if(mdMap.keySet().size() >1) {
        //the ore document has more than one metadata, throw a exception
        throw new OREException("DataONEDataStoreService.read - There are more than one metadata objects in the package "+
                                    identifier+" which Morpho currently doesn't support.");
      }
      // parse the metadata/data identifiers and store the associated objects if they are accessible
      byte[] metadata;
      for (Identifier scienceMetadataId : mdMap.keySet()) {
        //dp.addAndDownloadData(scienceMetadataId);
        metadata = IOUtils.toByteArray(getDataFromDataONE(scienceMetadataId.getValue()));
        AbstractDataPackage adp = DataPackageFactory.getDataPackage(new StringReader(new String(metadata,IdentifierFileMap.UTF8)));
        adp.setSystemMetadata(getSystemMetadataFromDataONE(scienceMetadataId.getValue()));
        dp.addData(adp);
        dp.setAbstractDataPackage(adp);
        List<Identifier> dataIdentifiers = mdMap.get(scienceMetadataId);
        for (Identifier dataId : dataIdentifiers) {
          byte[] data = IOUtils.toByteArray(getDataFromDataONE(dataId.getValue()));
          D1Object object = new D1Object();
          object.setData(data);
          object.setSystemMetadata(getSystemMetadataFromDataONE(dataId.getValue()));
        }
      }
    }
    return dp;
  }
  
  
  /*
   * Get the data - get it from the cache first.If it fails, get it from the dataone server.
   */
  /*private File getData(String identifier) throws InvalidToken, ServiceFailure, 
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
  }*/
  
  /*
   * Get the data - get it from the cache first.If it fails, get it from the dataone server.
   */
  /*private SystemMetadata getSystemMetadata(String identifier)  throws InvalidToken, ServiceFailure, 
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
    
  }*/
  
  /*
   * Get the data object from the dataone network.
   */
  private InputStream getDataFromDataONE(String identifier) throws InvalidToken, ServiceFailure, 
                             NotAuthorized, NotFound, NotImplemented, InsufficientResources, FileNotFoundException {
    //File file = null;
    Identifier pid = new Identifier();
    pid.setValue(identifier);
    InputStream respond = activeMNode.get(pid);
    //cache it
    //file = LocalDataStoreService.saveFile(identifier, respond, getCacheDir());
    return respond;
  }
  
  /*
   * Get the system metadata from the dataone for the id. It also caches it.
   */
  public static SystemMetadata getSystemMetadataFromDataONE(String identifier) throws InvalidToken, ServiceFailure, 
                                             NotAuthorized, NotFound, NotImplemented, InsufficientResources {
    if(activeMNode == null ) {
      init();
    }
    Identifier pid = new Identifier();
    pid.setValue(identifier);
    SystemMetadata systemMetadata = activeMNode.getSystemMetadata(pid);
    //cache it
    /*File tmp = null;
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
      
    }*/
    
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
    Identifier oreId = mdp.getPackageId();
    Identifier metadataId = mdp.getAbstractDataPackage().getIdentifier();
    Set<Identifier> identifiers = mdp.identifiers();
    //save data objects first
    if(identifiers != null && identifiers.size() > 0 ) {
      for(Identifier identifier : identifiers) {
        if(!identifier.equals(oreId) && !identifier.equals(metadataId)) {
          save(mdp.get(identifier));
        }
      }
      
    }
    //save metadata then
    save(mdp.get(metadataId));
    //save ore document finally
    D1Object oreD1Object = new D1Object();
    oreD1Object.setData((mdp.serializePackage()).getBytes(IdentifierFileMap.UTF8));
    oreD1Object.setSystemMetadata(mdp.getSystemMetadata());
    save(oreD1Object);
    return oreId.getValue();
  }
  
  /*
   * Save a d1Ojbect to the DataONE network. 
   */
  private void save(D1Object d1Object) throws NullPointerException, InvalidToken, ServiceFailure, NotAuthorized, 
              NotFound, NotImplemented, InsufficientResources, IllegalActionException, IllegalArgumentException, 
               ConfigurationException, IOException, IdentifierNotUnique, InvalidRequest, InvalidSystemMetadata, UnsupportedType {
    String action = null;
    
    if(d1Object == null) {
      throw new NullPointerException("DataONEDataStoreService.save - the D1Object which will be saved can't be null");
    }
    SystemMetadata sysMeta = d1Object.getSystemMetadata();
    if(sysMeta == null) {
      throw new NullPointerException("DataONEDataStoreService.save - the D1Object which will be saved can't have the null SystemMetadata");
    }
    Identifier identifier = d1Object.getIdentifier();
    if(identifier == null || identifier.getValue() == null || identifier.getValue().trim().equals("")) {
      throw new NullPointerException("DataONEDataStoreService.save - the D1Object which will be saved can't have a null identifier");
    }
    boolean hasObsoletes = adjustsObsoletes(sysMeta);
    adjustObsoletedBy(sysMeta);
    if(!hasObsoletes) {
      //create action
      activeMNode.create(d1Object.getIdentifier(), new ByteArrayInputStream(d1Object.getData()), sysMeta);
    } else {
      //update action
      activeMNode.update(sysMeta.getObsoletes(), new ByteArrayInputStream(d1Object.getData()), d1Object.getIdentifier(), sysMeta);
    }
  }
  
  
  /**
   * Adjust the Obsoletes object in the specified SystemMetadata object according the existence of the obsoletes id.
   * @param sysMeta
   * @return true if the Obsoletes object is kept; false else.
   * @throws ServiceFailure
   * @throws NotImplemented
   * @throws NotAuthorized
   * @throws InvalidToken
   * @throws IllegalArgumentException
   * @throws ConfigurationException
   * @throws IOException
   */
  private boolean adjustsObsoletes(SystemMetadata sysMeta) throws ServiceFailure, NotImplemented, NotAuthorized, 
                                         InvalidToken, IllegalArgumentException, ConfigurationException, IOException{
    boolean hasObsoletes = false;
    Identifier obsoletes = sysMeta.getObsoletes();
    if(obsoletes != null) {
      if(obsoletes.getValue() != null && !obsoletes.getValue().equals("")) {
        if(exists(obsoletes.getValue())) {
          //the obsoletes identifier exists in the dataone, so no change.
          hasObsoletes = true;
        } else {
          // Thought the obsoletes doesn't exist, it is possible in this scenario:
          //users has local version local.1.1, local.1.2, and local 1.3. It published
          //local.1.1 and now is publishing the 1.3. Since 1.2 wasn't published, so 1.2 doesn't exist in dataone.
          // But the local 1.1 does. So we should modify the obsoletes from 1.2 to 1.1
          List<String> olderVersions = RevisionManager.getInstance(getProfileDir(), DataPackageInterface.LOCAL).getOlderRevisions(obsoletes.getValue());
          String obsoletesId = exists(olderVersions);
          if(obsoletesId != null) {
            hasObsoletes = true;
            //modify the obsolete object value
            obsoletes.setValue(obsoletesId);
          }
        }
      }
    }
    if(!hasObsoletes) {
      //if there is no obsoletes, set it to null.
      sysMeta.setObsoletes(null);
    }
    return hasObsoletes;
  }
  
  /**
   * Adjust the obsoletedBy object. Generally, we don't allow an older version of object created (having obsoletedBy fields).
   * But, there is an exception : the obsoletedBy id doesn't exist in the dataone and there are no newer versions (base on the local revision chain) 
   * than the obsoletedBy id in the dataone network. The reason we don't use the dataone network revision chains is that the the obsoletedBy id doesn't exist
   * in the dataone network and there is no way for us to track down.
   * @param sysMeta
   * @throws ServiceFailure
   * @throws NotImplemented
   * @throws NotAuthorized
   * @throws InvalidToken
   * @throws IllegalActionException
   * @throws IllegalArgumentException
   * @throws ConfigurationException
   * @throws IOException
   */
  private void adjustObsoletedBy(SystemMetadata sysMeta) throws ServiceFailure, NotImplemented, NotAuthorized, 
                                         InvalidToken, IllegalActionException, IllegalArgumentException, 
                                         ConfigurationException, IOException {
    Identifier obsoletedBy = sysMeta.getObsoletedBy();
    if(obsoletedBy != null) {
      if (obsoletedBy.getValue() != null && !obsoletedBy.getValue().equals("")) {
        if(exists(obsoletedBy.getValue())) {
          throw new IllegalActionException("DataONEDataStoreService.adjustObsoletedBy - morpho doesn't allow to insert an older version to the DataONE network since "+
                                          " the object is obsoleted by "+obsoletedBy.getValue() +" in the system metadata and the id "+obsoletedBy.getValue()+" exists in the network");
        } else {
          // Thought the obsoletedBy doesn't exist, it is possible in this scenario:
          //users has local version local.1.1, local.1.2, and local 1.3. It published
          //local.1.3 and now is publishing the 1.1. The obsoletedBy for 1.1 is 1.2. Since 1.2 wasn't published, 1.2 doesn't
          //exist in the dataone network. However, 1.3 does exist which is newer version than 1.1. So we shouldn't allow 1.1 to
          //be published.
          List<String> newerVersions = RevisionManager.getInstance(getProfileDir(), DataPackageInterface.LOCAL).getNewerRevisions(obsoletedBy.getValue());
          String obsoletedById = exists(newerVersions);
          if(obsoletedById != null) {
            throw new IllegalActionException("DataONEDataStoreService.adjustObsoletedBy - morpho doesn't allow to insert an older version to the DataONE network since "+
                " the object is obsoleted by "+obsoletedBy.getValue() +" in the system metadata and a newer version of "+obsoletedBy.getValue()+" - "+obsoletedById+ " exists in the network");
          }
        }
      }
    }
    sysMeta.setObsoletedBy(null);
  }
  
  /*
   * Find any id in an array existing in the dataONE. If null is returned, we couldn't find it. Otherwise, the found
   * identifier will be returned.
   */
  private String exists(List<String> identifiers) throws ServiceFailure, NotImplemented, NotAuthorized, InvalidToken {
    String existedIdentifier = null;
    if(identifiers != null) {
      for(int i=0; i<identifiers.size(); i++) {
        boolean existing = exists(identifiers.get(i));
          if (existing) {
            existedIdentifier = identifiers.get(i);
            break;
          }
        }
      }
    return existedIdentifier;
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
