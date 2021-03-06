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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.FileDataSource;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.dataone.client.CNode;
import org.dataone.client.D1Object;
import org.dataone.client.MNode;
import org.dataone.client.auth.CertificateManager;
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
import org.dataone.service.exceptions.VersionMismatch;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.Node;
import org.dataone.service.types.v1.NodeList;
import org.dataone.service.types.v1.ObjectFormatIdentifier;
import org.dataone.service.types.v1.Subject;
import org.dataone.service.types.v1.SubjectInfo;
import org.dataone.service.types.v1.SystemMetadata;
import org.dataone.service.types.v1.util.ChecksumUtil;
import org.dataone.service.types.v1_1.QueryEngineList;
import org.dataone.service.util.Constants;
import org.dataone.service.util.EncodingUtilities;
import org.dspace.foresite.OREException;
import org.dspace.foresite.OREParserException;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.dataone.EcpAuthentication;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.Entity;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.datastore.idmanagement.DataONERevisionManager;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierFileMap;
import edu.ucsb.nceas.morpho.datastore.idmanagement.RevisionManager;
import edu.ucsb.nceas.morpho.datastore.idmanagement.RevisionManagerInterface;
import edu.ucsb.nceas.morpho.exception.IllegalActionException;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.ProfileDialog;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.DataLocation;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * Implements the DataStoreServiceInterface to access the files on the DataOne service
 * @author tao
 *
 */
public class DataONEDataStoreService extends DataStoreService implements DataStoreServiceInterface {

  public static final String MNODE_URL_ELEMENT_NAME = "dataone_mnode_baseurl";
  public static final String CNODE_URL_ELEMENT_NAME = "dataone_cnode_baseurl";
  public static final String PATHQUERY = "pathquery";
  public static final String SOLRQUERYENGINE = "solr";
  
  /**
   * The currently configured Member Node
   */
  private MNode activeMNode = null;
  
  /**
   * ping interval for network status
   * UI checks this very frequently, but it usually is not changing so rapidly
   * Cache it to avoid unnecessary repeated calls
   * @see getNetworkStatus() 
   */
  private boolean networkStatus = false;
  private long pingInterval = 1000 * 30 ; // half minute
  private Date lastPing = null;
  
  
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
  private void init() {
    String mNodeBaseURL = getMNodeURL();
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
   * Retrieve the configured MN URL
   * @return
   */
  public String getMNodeURL() {
	  String mnURL = config.get(DataONEDataStoreService.MNODE_URL_ELEMENT_NAME, 0);
	  return mnURL;
  }
  
  /**
   * Retrieve the configured CN URL
   * @return
   */
  public String getCNodeURL() {
	  String cnURL = config.get(DataONEDataStoreService.CNODE_URL_ELEMENT_NAME, 0);
	  return cnURL;
  }
  
  /**
   * Set the configured CN URL
   * @param cnURL
   */
  public void setCNodeURL(String cnURL) {
	  config.set(DataONEDataStoreService.CNODE_URL_ELEMENT_NAME, 0, cnURL, true);
	  config.save();
  }
  
  /**
   * Set the specified node URL to be the active member node.
   * @param nodeBaseServiceUrl - the node will be set.
   */
  public void setMNodeURL(String nodeBaseServiceUrl) {
	  config.set(DataONEDataStoreService.MNODE_URL_ELEMENT_NAME, 0, nodeBaseServiceUrl, true);
	  config.save();
	  
	  // make sure the MNode reflects this change
	  activeMNode = new MNode(nodeBaseServiceUrl);
	  
	  // reset ping status
	  lastPing = null;
  }
  
  /**
   * Set the specified id as the mn node id.
   * Do we need to save the id to the configuration file.
   * @param mnId
   */
  public void setMNodeId(String mnId) {
      if(activeMNode != null) {
          activeMNode.setNodeId(mnId);
      }
  }
  
  /**
   * open a file from a datastore with the id of name and return a File
   * object that represents it.  Throws FileNotFoundException if a file
   * with the id name does not exist in the datastore.  Throws IOException
   * if a there is a communications problem with the datastore.
   */
  public File openFile(String identifier) throws FileNotFoundException, 
                                           CacheAccessException {
	  File file = null;
	  try {
		  InputStream data = getDataFromDataONE(identifier);
		  FileSystemDataStore.getInstance(getCacheDir()).set(identifier, data);
		  file = FileSystemDataStore.getInstance(getCacheDir()).get(identifier);
	  } catch (Exception e) {
		  CacheAccessException cae = new CacheAccessException(e.getMessage());
		  cae.initCause(e);
		  throw cae;
	}
	  return file;
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
   * NOTE: this has temporarily been replaced by the the EML-based read method
   * which does not use ORE objects to construct the package
   */
  public MorphoDataPackage readByORE(String identifier) throws InvalidToken, ServiceFailure, 
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
      //byte[] metadata;
      for (Identifier scienceMetadataId : mdMap.keySet()) {
        //System.out.println("The scienceMetadataId is ======= "+scienceMetadataId.getValue());
        //dp.addAndDownloadData(scienceMetadataId);
        DataONEDataSource metadataSource = new DataONEDataSource(activeMNode, scienceMetadataId);
        //metadata = IOUtils.toByteArray(getDataFromDataONE(scienceMetadataId.getValue()));
        AbstractDataPackage adp = DataPackageFactory.getDataPackage(new BufferedReader(new InputStreamReader(metadataSource.getInputStream(),IdentifierFileMap.UTF8)));
        //adp.setData(metadata);
        adp.setDataSource(metadataSource);
        adp.setSystemMetadata(getSystemMetadataFromDataONE(scienceMetadataId.getValue()));
        adp.setLocation(DataPackageInterface.NETWORK);
        dp.addData(adp);
        dp.setAbstractDataPackage(adp);
        List<Identifier> dataIdentifiers = mdMap.get(scienceMetadataId);
        //System.out.println("The data id list size is === "+dataIdentifiers.size());
        for (Identifier dataId : dataIdentifiers) {
          //System.out.println("the data id is ==== "+dataId.getValue());
          Entity entity = adp.getEntity(dataId.getValue());
          if (entity != null) {
            //byte[] data = IOUtils.toByteArray(getDataFromDataONE(dataId.getValue()));
            //entity.setData(data);
            DataONEDataSource dataSource = new DataONEDataSource(activeMNode, dataId);
            entity.setDataSource(dataSource);
            entity.setSystemMetadata(getSystemMetadataFromDataONE(dataId.getValue()));
            dp.addData(entity);
          } else {
            throw new OREException("DataONEDataStoreService.read - the data object "+dataId.getValue()+" in the ORE document "+
                                    "couldn't be found in the science metadata document.");
          }
        
        }
      }
    }
    return dp;
  }
  
  /**
   * Retrieve a MorphoDataPackage for the given identifier
   * @param identifier - the identifier of the ore document
   * @return the MorphoDataPackage object
   * @throws FileNotFoundException
   * @throws CacheAccessException
   */
  @Override
  public MorphoDataPackage read(String identifier) throws InvalidToken,
			ServiceFailure, NotAuthorized, NotFound, NotImplemented,
			InsufficientResources, FileNotFoundException, IOException,
			OREException, URISyntaxException, OREParserException,
			InvalidRequest {

	  // construct the package
	  // TODO: handle OREs as packages
		MorphoDataPackage mdp = new MorphoDataPackage();
		Identifier packageId = new Identifier();
		packageId.setValue(identifier);
		//mdp.setPackageId(packageId);

		// parse the metadata/data identifiers and store the associated objects
		// if they are accessible
		//byte[] metadata;
		// the science metadata id is the package id in this case
		Identifier scienceMetadataId = packageId;
		DataONEDataSource metadataSource = new DataONEDataSource(activeMNode, scienceMetadataId);
		//metadata = IOUtils.toByteArray(getDataFromDataONE(scienceMetadataId.getValue()));
		AbstractDataPackage adp = DataPackageFactory.getDataPackage(new BufferedReader(new InputStreamReader(metadataSource.getInputStream(),IdentifierFileMap.UTF8)));
		//adp.setData(metadata);
		adp.setDataSource(metadataSource);
		adp.setSystemMetadata(getSystemMetadataFromDataONE(scienceMetadataId.getValue()));
		adp.setLocation(DataPackageInterface.NETWORK);
		mdp.addData(adp);
		mdp.setAbstractDataPackage(adp);
		
		// construct the data entities
		List<Identifier> dataIds = new ArrayList<Identifier>();;
		if (adp.getEntityArray() != null) {
			int entityIndex = 0;
			for (Entity entity: adp.getEntityArray()) {
			    String dataId = null;
			    try {
			        String URLinfo = adp.getDistributionUrl(entityIndex, 0, 0);
	                dataId = AbstractDataPackage.getUrlInfo(URLinfo);
	                Identifier dataIdentifier = new Identifier();
	                dataIdentifier.setValue(dataId);
	                DataONEDataSource dataSource = new DataONEDataSource(activeMNode, dataIdentifier);
	                dataIds.add(dataIdentifier);
	                if (entity != null) {
	                    //byte[] data = IOUtils.toByteArray(getDataFromDataONE(dataIdentifier.getValue()));
	                    //entity.setData(data);
	                    entity.setDataSource(dataSource);
	                    entity.setSystemMetadata(getSystemMetadataFromDataONE(dataIdentifier.getValue()));
	                    mdp.addData(entity);
	                } else {
	                    throw new OREException(
	                            "DataONEDataStoreService.read - the data object "
	                                    + dataIdentifier.getValue()
	                                    + "could not be found on the system");
	                }
			    } catch (Exception e) {
			       Log.debug(7, "Morpho couldn't read the entity "+dataId+" from the network:\n"+e.getMessage()); 
			    }
				
			}
		}
		
		return mdp;
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
  public SystemMetadata getSystemMetadataFromDataONE(String identifier) throws InvalidToken, ServiceFailure, 
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
	 * 
	 * @param mdp - the object will be saved
	 * @return the identifier of saved object TODO: need to implement it.
	 */
	@Override
	public String save(MorphoDataPackage mdp) throws Exception {
		AbstractDataPackage adp = mdp.getAbstractDataPackage();
		if (adp == null) {
			throw new IllegalActionException(
					"DataONEDataStoreService.save - users is trying save an Morpho data package without setting metadata - the AbstractDatapackage");
		}
		
		// check if we are logged in first
		if (!isConnected()) {
			EcpAuthentication.getInstance().establishConnection();
		}
		if (!isConnected()) {
			return null;
		}
		
		Identifier metadataId = adp.getIdentifier();

		// save data objects first
		Set<Identifier> identifiers = mdp.identifiers();
		if (identifiers != null && identifiers.size() > 0) {
			serializeData(mdp);
		}
		
		// now save
		D1Object metadataD1Object = mdp.get(metadataId);
		save(metadataD1Object);
		adp.setSerializeMetacatSuccess(true);

		// return now if we do not have data packages to save as ORE
		/*if (identifiers == null || identifiers.size() == 1) {
			return metadataId.getValue();
		}*/
		 //return now if we do not have data packages to save as ORE
        /*if (!hasEntity(mdp)) {
            return adp.getAccessionNumber();
        }*/
		//System.out.println("dataone has resrouce map!!!!!!!!!!!!!!!!!!============================");
		// save ore document finally
		Identifier oreId = new Identifier();
		oreId.setValue(RESOURCE_MAP_ID_PREFIX + metadataId.getValue());
		D1Object oreD1Object = new D1Object();
		mdp.setPackageId(oreId);
		//System.out.println("the serilziae page is ===== "+mdp.serializePackage());
		//save cache first
		File oreCacheFile = Morpho.thisStaticInstance.getLocalDataStoreService().saveCacheDataFile(oreId.getValue(), new ByteArrayInputStream((mdp.serializePackage()).getBytes(IdentifierFileMap.UTF8)));
		//oreD1Object.setData((mdp.serializePackage()).getBytes(IdentifierFileMap.UTF8));
		oreD1Object.setDataSource(new FileDataSource(oreCacheFile));

		// generate ORE SM for the save
		SystemMetadata resourceMapSysMeta = new SystemMetadata();
		PropertyUtils.copyProperties(resourceMapSysMeta, adp
				.getSystemMetadata());
		resourceMapSysMeta.setIdentifier(oreId);
		Checksum oreChecksum = ChecksumUtil.checksum(oreD1Object.getDataSource().getInputStream(), resourceMapSysMeta.getChecksum()
				.getAlgorithm());
		resourceMapSysMeta.setChecksum(oreChecksum);
		ObjectFormatIdentifier formatId = new ObjectFormatIdentifier();
		formatId.setValue("http://www.openarchives.org/ore/terms");
		resourceMapSysMeta.setFormatId(formatId);
		resourceMapSysMeta.setSize(BigInteger.valueOf(oreCacheFile.length()));

		// set the revision graph
		resourceMapSysMeta.setObsoletes(null);
		resourceMapSysMeta.setObsoletedBy(null);
		// assume naming convention for ORE maps to obsolete the old one
		// see: http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5798
		Identifier obsoleteMetadataId = adp.getSystemMetadata().getObsoletes();
		if (obsoleteMetadataId != null) {
			Identifier obsoleteOreId = new Identifier();
			obsoleteOreId.setValue(lookupResourceMapId(obsoleteMetadataId.getValue()));
			resourceMapSysMeta.setObsoletes(obsoleteOreId);
		}

		// this is just weird to set in two different places
		mdp.setSystemMetadata(resourceMapSysMeta);
		oreD1Object.setSystemMetadata(mdp.getSystemMetadata());
		save(oreD1Object);
		return oreId.getValue();
	}

	private boolean serializeData(MorphoDataPackage mdp) throws Exception {
		AbstractDataPackage adp = mdp.getAbstractDataPackage();
		if (adp.getEntityArray() == null) {
			Log.debug(30, "Entity array is null, no need to serialize data");
			return true; 
		}

		for (int i = 0; i < adp.getEntityArray().length; i++) {
			Entity entity = adp.getEntity(i);
			String URLinfo = adp.getDistributionUrl(i, 0, 0);
			String protocol = AbstractDataPackage.getUrlProtocol(URLinfo);
			String objectName = adp.getPhysicalName(i, 0);
			Log.debug(25, "object name is ===================== " + objectName);
			if (protocol != null && protocol.equals(AbstractDataPackage.ECOGRID)) {

				String docid = AbstractDataPackage.getUrlInfo(URLinfo);
				Log.debug(30, "handle data file  with index " + i + "" + docid);
				if (docid != null) {
					boolean isDirty = adp.containsDirtyEntityIndex(i);
					Log.debug(30, "url " + docid + " with index " + i + " is dirty " + isDirty);
					
					// check if the object exists already
					boolean exists = exists(docid);

					// save the data if it is new or has changes
					if (isDirty || !exists) {
						save(entity);
						//we need to update the identifier information in the DataPackage object
                        if (entity.getPreviousId() == null) {
                            mdp.addData(entity);
                            LocalDataStoreService.addEntityIdToResourceMap(mdp, docid);
                        } else {
                            mdp.updateIdentifier(entity.getPreviousId(), docid);
                        }
					}

					
					// newDataFile must have worked; thus update the package
					String urlinfo = DataLocation.URN_ROOT + docid;
					adp.setDistributionUrl(i, 0, 0, urlinfo);
					// File was saved successfully, we need to remove the dirty flag
					if (isDirty) {
						adp.removeDirtyEntityIndex(i);
					}
				}
			}
		}
		return true;
	}
	
	
  
  /*
   * Save a d1Ojbect to the DataONE network. 
   */
  private void save(D1Object d1Object) throws NullPointerException, InvalidToken, ServiceFailure, NotAuthorized, 
              NotFound, NotImplemented, InsufficientResources, IllegalActionException, IllegalArgumentException, 
               ConfigurationException, IOException, IdentifierNotUnique, InvalidRequest, InvalidSystemMetadata, UnsupportedType {
    
    if (d1Object == null) {
      throw new NullPointerException("DataONEDataStoreService.save - the D1Object which will be saved can't be null");
    }
    SystemMetadata sysMeta = d1Object.getSystemMetadata();
	
    if (sysMeta == null) {
        throw new NullPointerException("DataONEDataStoreService.save - the D1Object which will be saved can't have the null SystemMetadata");
      }
    
    // NOTE: the submitter may not be the official rightsHolderr, but we want to set a value here that will not lock out the user
    String rightsHolderDN = CertificateManager.getInstance().getSubjectDN(CertificateManager.getInstance().loadCertificate());
    Subject rightsHolder = new Subject();
    rightsHolder.setValue(rightsHolderDN);
    sysMeta.setRightsHolder(rightsHolder);
    
    // make sure serial version is set to something
    BigInteger serialVersion = sysMeta.getSerialVersion();
    if (serialVersion == null) {
    	sysMeta.setSerialVersion(BigInteger.ZERO);
    }
    
    Identifier identifier = d1Object.getIdentifier();
    if(identifier == null || identifier.getValue() == null || identifier.getValue().trim().equals("")) {
      throw new NullPointerException("DataONEDataStoreService.save - the D1Object which will be saved can't have a null identifier");
    }
    boolean hasObsoletes = adjustsObsoletes(sysMeta);
    adjustObsoletedBy(sysMeta);
    if(!hasObsoletes) {
      //create action
      activeMNode.create(d1Object.getIdentifier(), d1Object.getDataSource().getInputStream(), sysMeta);
    } else {
      //update action
      activeMNode.update(sysMeta.getObsoletes(), d1Object.getDataSource().getInputStream(), d1Object.getIdentifier(), sysMeta);
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
    // check if there is an ORE package
    Identifier oreId = mdp.getPackageId();
    Identifier metadataId = mdp.getAbstractDataPackage().getIdentifier();
    if (oreId != null) {
    	activeMNode.archive(oreId);
    } else {
        //String oreIdStr = RESOURCE_MAP_ID_PREFIX + metadataId.getValue();
        String oreIdStr = lookupResourceMapId(metadataId.getValue());
        if(exists(oreIdStr)) {
            oreId = new Identifier();
            oreId.setValue(oreIdStr);
            activeMNode.archive(oreId);
        }
    }
    
    activeMNode.archive(metadataId);
    Set<Identifier> identifiers = mdp.identifiers();
    if(identifiers != null && identifiers.size() > 0 ) {
      for(Identifier identifier : identifiers) {
        if(identifier != null) {
            if((oreId != null &&!identifier.equals(oreId)) && !identifier.equals(metadataId)) {
                try {
                    activeMNode.archive(identifier);
                } catch (Exception e) {
                    Log.debug(8, "Morpho couldn't delete the data object "+identifier.getValue()+" in the data package because\n"+e.getMessage());
                }
              
            }
        }
        

      }
      
    }
    success = true;
    return success;
  }
  
  /**
	 * Generate identifier from the dataone member node.
	 * 
	 * @return
	 */
	@Override
	public String generateIdentifier(String scheme, String fragment) throws InvalidToken,
			ServiceFailure, NotAuthorized, NotImplemented, InvalidRequest {
		Identifier identifier = activeMNode.generateIdentifier(scheme, fragment);
		return identifier.getValue();
	}
  
	/**
	 * Send the given query to the Dataone member node, get back the InputStream.
	 * This use the default query engine "pathquery".
	 * @param the query
	 * @return the result of the query
	 */
	@Override
	public InputStream query(String query) throws InvalidToken, ServiceFailure,
			NotAuthorized, InvalidRequest, NotImplemented, NotFound {
		return query(query, PATHQUERY);
	}
	
	/**
     * Send the given query to the Dataone member node as the specified query engine, get back the InputStream.
     * @param the query
     * @param engine the name of the query engine
     * @return the result of the query
     */
    public InputStream query(String query, String engine) throws InvalidToken, ServiceFailure,
            NotAuthorized, InvalidRequest, NotImplemented, NotFound {

        boolean engineSupported = isQueryEngineSupported(engine); 
        if (!engineSupported) {
            Log.debug(5, "The '" + engine + "' syntax is not supported on this MN");
            return null;
        }
        // made it here so we submit the query
        String encodedQuery = EncodingUtilities.encodeUrlPathSegment(query);
        return activeMNode.query(engine, encodedQuery);
    }

	/**
	 * Check if the MN supports the default queryEngine
	 * @param engineToUse
	 * @return
	 */
	public boolean isQueryEngineSupported() {
		// check if this MN supports our query syntax
		return isQueryEngineSupported(PATHQUERY);
		
	}
	
	/**
	 * If the MN supports the specified engine
	 * @param engineToUse the name of query engine
	 * @return true if it supports; else false.
	 */
	public boolean isQueryEngineSupported(String engineToUse) {
	    boolean engineSupported = false;
	    if(engineToUse != null && !engineToUse.trim().equals("")) {
	        try {
	            QueryEngineList queryEngines = activeMNode.listQueryEngines();
	            if (queryEngines != null && queryEngines.getQueryEngineList() != null) {
	                for (String qe: queryEngines.getQueryEngineList()) {
	                    if (qe.equalsIgnoreCase(engineToUse)) {
	                        engineSupported = true;
	                        break;
	                    }
	                }
	            }
	        } catch (Exception e) {
	            engineSupported = false;
	        }
	    }
        return engineSupported;
	}

	@Override
	public RevisionManagerInterface getRevisionManager() {
		// TODO: migrate into this class
		return DataONERevisionManager.getInstance();
	}
	
	/**
	 * Determines if the framework has a valid certificate
	 * @return boolean true if the client certificate is valid (a proxy for "connected")
	 */
	public boolean isConnected() {
		boolean isValidCert = false;
		String clientCertificateLocation = Morpho.thisStaticInstance.getProfile().get(ProfileDialog.D1_CLIENT_CERTIFICATE_LOCATION, 0);
//		if (clientCertificateLocation != null && clientCertificateLocation.length() == 0) {
//			clientCertificateLocation = null;
//		}
        CertificateManager.getInstance().setCertificateLocation(clientCertificateLocation);
		X509Certificate clientCertificate = CertificateManager.getInstance().loadCertificate();
		if (clientCertificate != null) {
			try {
				clientCertificate.checkValidity();
				isValidCert = true;
			} catch (CertificateExpiredException e) {
				e.printStackTrace();
				isValidCert = false;
			} catch (CertificateNotYetValidException e) {
				e.printStackTrace();
				isValidCert = false;
			}
		}
		
		return isValidCert;
		
	}
	
	/**
     * Get the username associated with this framework
     *
     * @return    The UserName value
     * @returns   String the username
     */
    public String getUserName()
    {
        String subjectDN = null;
        // if we have a valid certificate, use it to find the username
        if (this.isConnected()) {
	        String clientCertificateLocation = Morpho.thisStaticInstance.getProfile().get(ProfileDialog.D1_CLIENT_CERTIFICATE_LOCATION, 0);
//	        if (clientCertificateLocation != null && clientCertificateLocation.length() == 0) {
//				clientCertificateLocation = null;
//			}
	        CertificateManager.getInstance().setCertificateLocation(clientCertificateLocation);
	        X509Certificate clientCert = CertificateManager.getInstance().loadCertificate();
			if (clientCert != null) {
				subjectDN = CertificateManager.getInstance().getSubjectDN(clientCert);
			}
        } else {
        	this.logOut();
        }
        String userName = (subjectDN != null) ? subjectDN : Constants.SUBJECT_PUBLIC;
		return userName;
    }
	
	/**
	 * Determines if the framework is using an ssl connection
	 * 
	 * @return boolean true if using SSL, false otherwise
	 */
	public boolean getSslStatus() {
		// check if we are using https, which we have to for DataONE!
		// TODO: consider using this icon for something more meaningful, like whether or not we true the server
		boolean usingSSL = false;
		try {
			URL mnURL = new URL(activeMNode.getNodeBaseServiceUrl());
			String protocol = mnURL.getProtocol();
			if (protocol.equalsIgnoreCase("https")) {
				usingSSL = true;	
			}
		} catch (Exception e) {
			Log.debug(10, "Error checking SSL status: " + e.getMessage());
			e.printStackTrace();
			usingSSL = false;
		}
		
		return usingSSL;
	}
	
	public boolean logIn(String certificateLocation) {
		// load the configured certificate
		Morpho.thisStaticInstance.getProfile().set(ProfileDialog.D1_CLIENT_CERTIFICATE_LOCATION, 0, certificateLocation, true);
		Morpho.thisStaticInstance.getProfile().save();
		CertificateManager.getInstance().setCertificateLocation(certificateLocation);
	      
		return isConnected();
	}
	
	/**
	 * Log out by deleting the client certificate and configuring the 
	 * client certificate location to null.
	 * We delete the file because the certificate may be valid for hours after it 
	 * has been issued and we want to ensure that it is not misused.
	 * @return
	 */
	public boolean logOut() {
		// remove the certificate file
		String certificateLocation = Morpho.thisStaticInstance.getProfile().get(ProfileDialog.D1_CLIENT_CERTIFICATE_LOCATION, 0);
		if (certificateLocation != null) {
			File certFile = new File(certificateLocation);
			if (certFile.exists()) {
				certFile.delete();
			}
		}
		
		// clear out the certificate config
		Morpho.thisStaticInstance.getProfile().set(ProfileDialog.D1_CLIENT_CERTIFICATE_LOCATION, 0, "", true);
		Morpho.thisStaticInstance.getProfile().save();

		return true;
	}
	
	  /**
	 * Determine whether a network connection is available
	 * Calls to the MN are limited by the ping interval
	 * to avoid superfluous calls to ping().
	 * 
	 * @return boolean true if the network is reachable
	 */
	public boolean getNetworkStatus() {
		// only do this periodically
		if (lastPing == null || lastPing.getTime() - System.currentTimeMillis() > pingInterval) {
			Date pingDate = null;
			try {
			    String clientCertificateLocation = Morpho.thisStaticInstance.getProfile().get(ProfileDialog.D1_CLIENT_CERTIFICATE_LOCATION, 0);
			    CertificateManager.getInstance().setCertificateLocation(clientCertificateLocation);
				pingDate = activeMNode.ping();
			} catch (Exception e) {
				Log.debug(30, "Could not ping MN: " + e.getMessage());
			}
			networkStatus = (pingDate != null);
			lastPing = Calendar.getInstance().getTime();
		}
		return networkStatus;
	}
 
	/**
	 * Retrieve a Node list from the CN.
	 * Uses the currently configured CN if the cnURL
	 * parameter is null.
	 * @param optional cnURL to use in cases where a value has not
	 * been committed to the configuration file
	 * @return list of nodes registered in the CN
	 */
	public List<Node> getNodes(String cnURL) {
		try {
			if (cnURL == null || cnURL.length() == 0) {
				cnURL = getCNodeURL();
			}
			CNode cNode = new CNode(cnURL);
			NodeList nodes = cNode.listNodes();
			return nodes.getNodeList();
		} catch (Exception e) {
			Log.debug(10, "Could not look up nodes from CN: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
		
	}
	
	/**
	 * Set the ReplicationPolicy CN 
	 * Note that this can fail if called before the CN is able to synchronize
	 * content with the MN that hosts the original object.
	 * In this case, the call will have to be made again at a later time.
	 * @param sysMeta the SystemMetadata that contains the ReplicationPolicy to be used
	 * @return true if successful
	 * @throws VersionMismatch 
	 * @throws InvalidToken 
	 * @throws InvalidRequest 
	 * @throws ServiceFailure 
	 * @throws NotAuthorized 
	 * @throws NotFound 
	 * @throws NotImplemented 
	 */
	public boolean setReplicationPolicy(SystemMetadata sysMeta) throws NotImplemented, NotFound, NotAuthorized, ServiceFailure, InvalidRequest, InvalidToken, VersionMismatch {
		boolean result = false;
		
		// check if we are connected first
		if (!isConnected()) {
			EcpAuthentication.getInstance().establishConnection();
		}
		if (!isConnected()) {
			return false;
		}
		
		String cnURL = getCNodeURL();
		CNode cNode = new CNode(cnURL);
		
		// TODO: have a better refresh/merge solution
		long serialVersion = 0;
		//if (sysMeta.getSerialVersion() != null) {
		if (false) {	
			serialVersion = sysMeta.getSerialVersion().longValue();
		} else {
			// get the latest serialVersion from CN
			Log.debug(20, "Looking up SystemMetadata.serialVersion from CN before setting ReplicationPolicy for: " + sysMeta.getIdentifier().getValue());
			SystemMetadata cnSysMeta = cNode.getSystemMetadata(sysMeta.getIdentifier());
			serialVersion = cnSysMeta.getSerialVersion().longValue();
		}
		result = cNode.setReplicationPolicy(sysMeta.getIdentifier(), sysMeta.getReplicationPolicy(), serialVersion);
		
		return result;
	}
	
	/**
	 * Set the AccessPolicy on the CN 
	 * Note that this can fail if called before the CN is able to synchronize
	 * content with the MN that hosts the original object.
	 * In this case, the call will have to be made again at a later time.
	 * @param sysMeta the SystemMetadata that contains the AccessPolicy to be used
	 * @param sysMeta
	 * @return
	 * @throws NotImplemented 
	 * @throws NotFound 
	 * @throws NotAuthorized 
	 * @throws ServiceFailure 
	 * @throws InvalidToken 
	 * @throws VersionMismatch 
	 * @throws InvalidRequest 
	 */
	public boolean setAccessPolicy(SystemMetadata sysMeta) throws InvalidToken, ServiceFailure, NotAuthorized, NotFound, NotImplemented, InvalidRequest, VersionMismatch {
		boolean result = false;
		
		// check if we are logged in first
		if (!isConnected()) {
			EcpAuthentication.getInstance().establishConnection();
		}
		if (!isConnected()) {
			return false;
		}
				
		String cnURL = getCNodeURL();
		CNode cNode = new CNode(cnURL);
		
		String identifier = sysMeta.getIdentifier().getValue();
		// TODO: have a better refresh/merge solution
		long serialVersion = 0;
		//if (sysMeta.getSerialVersion() != null) {
		if (false) {
			serialVersion = sysMeta.getSerialVersion().longValue();
		} else {
			// get the latest serialVersion from CN
			Log.debug(20, "Looking up SystemMetadata.serialVersion from CN before setting ReplicationPolicy for: " + identifier);
			SystemMetadata cnSysMeta = cNode.getSystemMetadata(sysMeta.getIdentifier());
			serialVersion = cnSysMeta.getSerialVersion().longValue();
		}
		result = cNode.setAccessPolicy(sysMeta.getIdentifier(), sysMeta.getAccessPolicy(), serialVersion);
		
		// try ORE if we can find it
		String objectFormatType = cNode.getFormat(sysMeta.getFormatId()).getFormatType();
		if (objectFormatType.equalsIgnoreCase("METADATA")) {
			//String oreIdentifier = DataStoreService.RESOURCE_MAP_ID_PREFIX + identifier;
		    String oreIdentifier = lookupResourceMapId(identifier);
			SystemMetadata oreSystemMetadata = null;
			try {
				oreSystemMetadata = this.getSystemMetadataFromDataONE(oreIdentifier);
			} catch (Exception e) {
				Log.debug(20, "Could not find related ORE object: " + oreIdentifier);
				e.printStackTrace();
			}
			// set the access policy and save it
			if (oreSystemMetadata != null) {
				oreSystemMetadata.setAccessPolicy(sysMeta.getAccessPolicy());
				result = result &&  this.setAccessPolicy(oreSystemMetadata);
			}
		}
		
		return result;
	}
	
	/**
	 * Get the all identity information from the specified cnURL. If the cnURL is null,
	 * the configured CN will be used.
	 * @param cnURL
	 * @return
	 * @throws InvalidRequest
	 * @throws ServiceFailure
	 * @throws InvalidToken
	 * @throws NotAuthorized
	 * @throws NotImplemented
	 */
	public SubjectInfo getAllIdentityInfo(String cnURL) throws InvalidRequest,
			ServiceFailure, InvalidToken, NotAuthorized, NotImplemented {
		SubjectInfo info = null;
		if (cnURL == null || cnURL.length() == 0) {
			cnURL = getCNodeURL();
		}
		CNode cNode = new CNode(cnURL);
		String query = null;
		String status = null;
		int start = -1;
		int count = -1;
		info = cNode.listSubjects(query, status, start, count);
		return info;
	}
	
	
}
