/**
 *  '$RCSfile: FileSystemDataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2009-02-06 21:26:34 $'
 * '$Revision: 1.13 $'
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
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.dataone.client.D1Object;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.ObjectFormatIdentifier;
import org.dataone.service.types.v1.SystemMetadata;
import org.dataone.service.types.v1.util.ChecksumUtil;
import org.dataone.service.util.TypeMarshaller;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.datapackage.DocidConflictHandler;
import edu.ucsb.nceas.morpho.datapackage.Entity;
import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierFileMap;
import edu.ucsb.nceas.morpho.datastore.idmanagement.RevisionManager;
import edu.ucsb.nceas.morpho.datastore.idmanagement.RevisionManagerInterface;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.query.Query;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLUtil;

/**
 * implements and the DataStoreInterface for accessing files on the local
 * file system.
 */
public class LocalDataStoreService extends DataStoreService
                                 implements DataStoreServiceInterface
{
	public final static String INCOMPLETEDIR = "incomplete";
	public static final String TEMP = "temporary";
	private static final String TEMPIDNAME = "lastTempId";
	
	// store the map between new data id and old data id
	private Hashtable<String, String> original_new_id_map = new Hashtable<String, String>(); 
	
	/**
	* create a new FileSystemDataStore for a Morpho
	*/
	public LocalDataStoreService(Morpho morpho) {
		super(morpho);
	}
  
	/**
	 * Gets the data dir directory
	 * 
	 * @return
	 */
	private String getDataDir() {
		ConfigXML profile = morpho.getProfile();
		String datadir = getProfileDir(profile) + File.separator + profile.get("datadir", 0);
		return datadir;
	}
	
	private String getIncompleteDir() {
		String incompletedir = null;
		ConfigXML profile = morpho.getProfile();
		String incomplete = profile.get("incompletedir", 0);
		// in case no incomplete dir in old version profile
		if (incomplete == null || incomplete.trim().equals("")) {
			incomplete = INCOMPLETEDIR;
		}
		incompletedir = getProfileDir(profile) + File.separator + incomplete;
		return incompletedir;
	}
	
	private String getQueriesDir() {
		ConfigXML profile = morpho.getProfile();
		String dir = getProfileDir(profile) + File.separator + profile.get("queriesdir", 0);
		return dir;
	}
	
  
  /**
   * Get an AbstractDataPackage for the given identifier
   * @param identifier
   * @return
   * @throws FileNotFoundException
   */
  @Override
  public MorphoDataPackage read(String identifier) throws Exception {
		
	  File file = openFile(identifier);
	  Reader in = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
	  AbstractDataPackage adp = DataPackageFactory.getDataPackage(in);
	  SystemMetadata sysMeta = getSystemMetadata(identifier);
	  if (sysMeta != null) {
		  adp.setSystemMetadata(sysMeta);
	  }
	  adp.setLocation(DataPackageInterface.LOCAL);
	  loadData(adp);
	  MorphoDataPackage mdp = new MorphoDataPackage();
	  mdp.setAbstractDataPackage(adp);
	  return mdp; 
  }
  
  private void loadData(AbstractDataPackage adp) throws Exception {
	  
	  List<Identifier> dataIds = new ArrayList<Identifier>();;
	  if (adp.getEntityArray() != null) {
			int entityIndex = 0;
			for (Entity entity: adp.getEntityArray()) {
				String URLinfo = adp.getDistributionUrl(entityIndex, 0, 0);
				String dataId = AbstractDataPackage.getUrlInfo(URLinfo);
				Identifier identifier = new Identifier();
				identifier.setValue(dataId);
				dataIds.add(identifier);
				byte[] data = IOUtils.toByteArray(new FileInputStream(openFile(identifier.getValue())));
				entity.setData(data);
				// set the SM if we have it
				SystemMetadata sysmeta = getSystemMetadata(identifier.getValue());
				if (sysmeta != null) {
					entity.setSystemMetadata(sysmeta);
				} else {
					// dupe the EML SystemMetadata for the data
					try {
						PropertyUtils.copyProperties(entity.getSystemMetadata(), adp.getSystemMetadata());
						entity.getSystemMetadata().setIdentifier(identifier);
						ObjectFormatIdentifier dataFormatId = new ObjectFormatIdentifier();
						dataFormatId.setValue(adp.getPhysicalFormat(entityIndex, 0));
						entity.getSystemMetadata().setFormatId(dataFormatId);
						Checksum dataChecksum = ChecksumUtil.checksum(entity.getData(), entity.getSystemMetadata().getChecksum().getAlgorithm());
						entity.getSystemMetadata().setChecksum(dataChecksum);
						entity.getSystemMetadata().setSize(BigInteger.valueOf(entity.getData().length));
					} catch (Exception e) {
						Log.debug(10, "Error setting SystemMetadata for entity " + entityIndex);
						e.printStackTrace();
					}
				}
			}
	  }
	  
  }
  
  /**
   * opens a data or metadata file with the given identifier. 
   */
  public File openFile(String identifier) throws FileNotFoundException {
		
		File file = null;
		try {
			file = FileSystemDataStore.getInstance(getDataDir()).get(identifier);
		} catch (Exception e) {
			FileNotFoundException fnfe = new FileNotFoundException(e.getMessage());
			fnfe.initCause(e);
			throw fnfe;
		}
		return file;
	}
  
  /**
   * opens a file with the given name from incomplete dir.  the name should be in the form
   * scope.accnum where the scope is unique to this machine.  The file will
   * be opened from the &lt;datadir&gt;/&lt;scope&gt;/ directory 
   * where the filename is the accnum.
   * Example: 
   *    name=johnson2343.13223
   *    datadir=data
   *    complete path=/usr/local/morpho/profiles/johnson/incomplete/johnson2343/13223
   * Any characters after the first separator are assumed to be part of the 
   * accession number.  Hence the id johnson2343.13223.5 would produce 
   * the file johnson2343/13223.5
   * @param name
   * @return
   * @throws FileNotFoundException
   */
  public File openIncompleteFile(String identifier) throws FileNotFoundException {
	  Log.debug(21, "opening " + identifier + " from incomplete dir - temp: " + getIncompleteDir());
		File file = null;
		try {
			file = FileSystemDataStore.getInstance(getIncompleteDir()).get(identifier);
		} catch (Exception e) {
			FileNotFoundException fnfe = new FileNotFoundException(e.getMessage());
			fnfe.initCause(e);
			throw fnfe;
		}
		return file;
	}
  
  
	public File saveFile(String name, InputStream inputStream) {
		return saveFile(name, inputStream, getDataDir());
	}
	
	  /**
	 * Save an XML serialized version of the query in the profile directory
	 */
	public void saveQuery(Query query) throws IOException {

		String id = query.getIdentifier();
		try {
			InputStream inputStream = new ByteArrayInputStream(query.toXml().getBytes("UTF-8"));
			FileSystemDataStore.getInstance(getQueriesDir()).set(id, inputStream);
		} catch (Exception e) {
			IOException ioe = new IOException(e.getMessage());
			ioe.initCause(e);
			throw ioe;
		}
	}

	/**
	 * returns a listing of all the saved query identifiers
	 * @return
	 */
	public List<String> getQueryIdentifiers() {
		List<String> identifiers = null;
		try {
			identifiers = FileSystemDataStore.getInstance(getQueriesDir()).getIdentifiers();
		} catch (Exception e) {
			e.printStackTrace();
			Log.debug(6, e.getMessage());
			return null;
		}
		return identifiers;
	}
	
  public File openQueryFile(String identifier) throws FileNotFoundException {

		File file = null;
		try {
			file = FileSystemDataStore.getInstance(getQueriesDir()).get(identifier);
		} catch (Exception e) {
			FileNotFoundException fnfe = new FileNotFoundException(e.getMessage());
			fnfe.initCause(e);
			throw fnfe;
		}
		return file;
	}
  
  public File openTempFile(String identifier) throws FileNotFoundException {
		Log.debug(21, "opening " + identifier + " from temp dir - temp: " + getTempDir());
		File file = null;
		try {
			file = FileSystemDataStore.getInstance(getTempDir()).get(identifier);
		} catch (Exception e) {
			FileNotFoundException fnfe = new FileNotFoundException(e.getMessage());
			fnfe.initCause(e);
			throw fnfe;
		}
		return file;
	}

  public void newDataFile(String name, File file, String objectName) throws FileNotFoundException
  {
    saveFile(name, new FileInputStream(file), getDataDir());
  }
  
  public File saveTempDataFile(String name, InputStream file)
  {
    return saveFile(name, file, getTempDir());
  }
  
  /**
   * Save an input stream into a file in incomplete dir with the given name.
   * @param name
   * @param file
   * @return
   */
		  
  public File saveIncompleteDataFile(String name, InputStream file)
  {
    return saveFile(name, file, getIncompleteDir());
  }
  
  /**
	 * Save a stream into a file in incomplete dir with a given name
	 * 
	 * @param name the identifier for the object
	 * @param inputStream source of the content
	 * @return the File representing the content
	 */
	public File saveIncompleteFile(String name, InputStream inputStream) {
		return saveFile(name, inputStream, getIncompleteDir());
	}
 
  /**
	 * Check if the given docid exists in local file system.
	 * 
	 * @param docid
	 * @return exists or not
	 */
	public String status(String docid) {
		String status = DataStoreServiceInterface.NONEXIST;
		File savefile = null;
		try {
			savefile = openFile(docid);
		} catch (FileNotFoundException e) {
			// does not exist
		}
		if (savefile != null && savefile.exists()) {
			status = DataStoreServiceInterface.CONFLICT;
		}
		Log.debug(30, "The docid " + docid + " local status is " + status);
		return status;
	}
  
  /**
	 * Does this id exist locally
	 * @param identifier
	 * @return
	 */
	public boolean exists(String identifier) {
		
		String status = DataStoreServiceInterface.NONEXIST;
		status = status(identifier);
		return !status.equals(DataStoreServiceInterface.NONEXIST);
		
	}
	
	/**
	 * Save an incomplete data package locally. This is a special case.
	 * @param mdp
	 * @return
	 * @throws Exception
	 */
	public String saveIncomplete(MorphoDataPackage mdp) throws Exception {
		return save(mdp, DataPackageInterface.INCOMPLETE);
	}
	
	/**
	 * Save the MorphoDataPackage object into the local data store.
	 * @param mdp - the object will be saved
	 * @return the identifier of saved object
	 */
	@Override
	public String save(MorphoDataPackage mdp) throws Exception {
		return save(mdp, DataPackageInterface.LOCAL);
		
	}
	
	/**
	 * Save the MorphoDataPackage object into the local data store.
	 * @param mdp - the object will be saved
	 * @return the identifier of saved object
	 */
	private String save(MorphoDataPackage mdp, String location) throws Exception {
		
		// save the data first
		boolean dataStatus = serializeData(mdp, location);
		
		AbstractDataPackage adp = mdp.getAbstractDataPackage();

		// only continue if that was successful
		if (!dataStatus) {
			adp.setSerializeLocalSuccess(false);
			return null;
		}

		// now save the metadata
		adp.setSerializeLocalSuccess(false);
		adp.setSerializeMetacatSuccess(false);
		adp.setPackageIDChanged(false);
		
		// To check if this update or insert action
		String identifier = adp.getAccessionNumber();
		boolean exists = this.exists(identifier);
		
		// does the identifier exist already?
		if (exists) {
			Log.debug(30, "object already exists locally with this identifier: " + identifier);
			// TODO: is this frame needed or can we just increase the revision silently?
			DocidConflictHandler identifierConflictHandler = new DocidConflictHandler(identifier, DataPackageInterface.LOCAL);
			String choice = identifierConflictHandler.showDialog();
			// Log.debug(5, "choice is "+choice);
			if (choice != null) {
				String origId = identifier;
				if (choice.equals(DocidConflictHandler.INCREASEID)) {
					// generate a new identifier - separate from the original chain
					String scope = Morpho.thisStaticInstance.getProfile().get("scope", 0);
					identifier = generateIdentifier(scope);
				} else {
					// get next identifier (really just the same as generating a new one) 
					String nextIdentifier = getNextIdentifier(identifier);
					Log.debug(30, "Original identifier: " + identifier + ", next revision: " + nextIdentifier);
					// record this in revision manager
					getRevisionManager().setObsoletes(nextIdentifier, identifier);
					identifier = nextIdentifier;
				}
				adp.setAccessionNumber(identifier);
				adp.setPackageIDChanged(true);
				
				// make sure the mdp has the latest ADP object/identifier
				Identifier originalIdentifier = new Identifier();
				originalIdentifier.setValue(origId);
				mdp.remove(originalIdentifier);
				mdp.addData(adp);
				
			} else {
				// canceled the save, so just return now
				return identifier;
			}
		} else {
			Log.debug(30, "object identifier does not exist locally");
			// since it is saving a new package, no need to do anything
			adp.setAccessionNumber(identifier);
			adp.setPackageIDChanged(false);

		}
		
		// now save doc to local file system, either for real or in incomplete directory
		String temp = XMLUtil.getDOMTreeAsString(adp.getMetadataNode().getOwnerDocument());
		InputStream stringStream = new ByteArrayInputStream(temp.getBytes(Charset.forName("UTF-8")));
		File savedFile = null;

		if (location.equals(DataPackageInterface.INCOMPLETE)) {
			Log.debug(30, "Serialize metadata into incomplete dir with docid " + identifier);
			savedFile = saveIncompleteFile(identifier, stringStream);
		} else {
			savedFile = saveFile(adp.getAccessionNumber(), stringStream);
		}
		
		// success?
		if (savedFile != null) {
			adp.setSerializeLocalSuccess(true);
		} else {
			// probably should not continue at this point
			adp.setSerializeLocalSuccess(false);
			return identifier;
		}
		
		// save the SM for the science metadata
		saveSystemMetadata(adp.getSystemMetadata());
		
		// check if we have a package
	    Set<Identifier> identifiers = mdp.identifiers();
	    //return now if we do not have data packages to save as ORE
	    if (identifiers == null || identifiers.size() == 1) {
			return adp.getAccessionNumber();
	    }
	    
		// save ore document finally
		Identifier oreId = new Identifier();
	    oreId.setValue("resourceMap_" + adp.getIdentifier().getValue());
	    D1Object oreD1Object = new D1Object();
	    mdp.setPackageId(oreId);
	    oreD1Object.setData((mdp.serializePackage()).getBytes(IdentifierFileMap.UTF8));
	    
	    // generate ORE SM for the save
	    SystemMetadata resourceMapSysMeta = new SystemMetadata();
	    PropertyUtils.copyProperties(resourceMapSysMeta, adp.getSystemMetadata());
	    resourceMapSysMeta.setIdentifier(oreId);
	    Checksum oreChecksum = ChecksumUtil.checksum(new ByteArrayInputStream(oreD1Object.getData()), resourceMapSysMeta.getChecksum().getAlgorithm());
		resourceMapSysMeta.setChecksum(oreChecksum);
	    ObjectFormatIdentifier formatId = new ObjectFormatIdentifier();
	    formatId.setValue("http://www.openarchives.org/ore/terms");
		resourceMapSysMeta.setFormatId(formatId);
		resourceMapSysMeta.setSize(BigInteger.valueOf(oreD1Object.getData().length));
		
		// set the revision graph
		resourceMapSysMeta.setObsoletes(null);
		resourceMapSysMeta.setObsoletedBy(null);

		// this is just weird to set in two different places
	    mdp.setSystemMetadata(resourceMapSysMeta);
	    oreD1Object.setSystemMetadata(mdp.getSystemMetadata());
	    
	    // save the ORE
	    saveSystemMetadata(oreD1Object.getSystemMetadata());
	    saveFile(mdp.getPackageId().getValue(), new ByteArrayInputStream(oreD1Object.getData()));
		
	    // return the ORE package id. (not yet used)
		return oreId.getValue();
		
	}
	
	private boolean serializeData(MorphoDataPackage mdp, String dataDestination) throws Exception {
		Log.debug(30, "serilaize data =====================");
		AbstractDataPackage adp = mdp.getAbstractDataPackage();
		adp.getEntityArray();
		// Log.debug(1, "About to check entityArray!");
		if (adp.getEntityArray() == null) {
			Log.debug(30, "Entity array is null, no need to serialize data");
			return true; // there is no data!
		}

		adp.setDataIDChanged(false);
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

					// do we have any revisions for this object
					List<String> revisions = getRevisionManager().getAllRevisions(docid);
					if (revisions != null) {
						// is it in the revision history?
						boolean inRevisions = revisions.contains(docid);
						if (inRevisions) {
							// is it the latest revision?
							String latestRevision = revisions.get(revisions.size() - 1);
							if (docid != latestRevision) {
								// prompt to get the latest revision before saving an update?
								Log.debug(5, "Should not update data identifier: " + docid + " because it is not the latest revsion (" + latestRevision + ")");
								return false;
							}
							// otherwise we can continue
						}
					}
					
					// if docid exists, we need to update the revision
					String originalIdentifier = docid;
					boolean updatedId = false;
					if (exists && isDirty) {
						// get the next identifier in this series
						docid = getNextIdentifier(docid);
						// save the old one for reference later
						original_new_id_map.put(docid, originalIdentifier);
						// save the revision history
						getRevisionManager().setObsoletes(docid, originalIdentifier);
						Identifier obsoletes = new Identifier();
						obsoletes.setValue(originalIdentifier);
						entity.getSystemMetadata().setObsoletes(obsoletes);
						// we changed the identifier
						updatedId = true;
						Log.debug(30, "The identifier "
								+ originalIdentifier
								+ " exists and has unsaved data. The identifier for next revision is " + docid );

					}

					// save the data if it is new or has changes
					boolean status = false;
					if (isDirty || updatedId || !exists) {
						if (dataDestination.equals(DataPackageInterface.INCOMPLETE)) {
							handleIncompleteDir(docid);
						} else {
							status = handleLocal(docid);
							// this was a failure, return now
							if (!status) {
								return false;
							}
						}
						// save the SM
						saveSystemMetadata(entity.getSystemMetadata());
					}

					// reset the map after finishing save. There is no need for
					// this pair after saving
					original_new_id_map = new Hashtable<String, String>();
					
					// newDataFile must have worked; thus update the package
					String urlinfo = "ecogrid://knb/" + docid;
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
	
	/**
	 * Saves the entity into local system
	 */
	private boolean handleLocal(String docid) {
		Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~handle local " + docid);
		File dataFile = null;
		String oldDocid = null;
		// if morpho serialized data into local store and docid was changed,
		// we need to get the old docid and find the it in temp dir
		if (!original_new_id_map.isEmpty()) {
			// System.out.println("the key is "+urlinfo);
			// System.out.println("the hashtable is "+original_new_id_map);
			// Log.debug(1,
			// "~~~~~~~~~~~~~~~~~~~~~~change id in local serialization ");
			oldDocid = (String) original_new_id_map.get(docid);
			Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~the id from map is " + oldDocid);

		}
		// if oldDocid is null, that means docid change. So we set old docid to
		// be the current id
		if (oldDocid == null) {
			oldDocid = docid;
		}
		Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~eventually old id is  " + oldDocid);
		
		try {
			// try to get the temp file for old id
			dataFile = openTempFile(oldDocid);
		} catch (Exception qq) {
			// try to open incomplete file
			try {
				dataFile = openIncompleteFile(oldDocid);
			} catch (Exception e) {
				// if a datafile is on metacat and one wants to save locally
				try {
					// open from network
					dataFile = DataStoreServiceController.getInstance().openFile(oldDocid, DataPackageInterface.NETWORK);
				} catch (Exception qqq) {
					// some other problem has occurred
					Log.debug(5, "Some problem with saving local data files has occurred! " + qqq.getMessage());
					qq.printStackTrace();
					return false;
				}
			}
		}
		
		// open old file name (if no file change, the old file name will be as same as docid).
		try {
			newDataFile(docid, dataFile, oldDocid);
		} catch (FileNotFoundException e) {
			// some other problem has occurred
			Log.debug(5, "Problem with saving local data file: " + docid + ", Error: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		
		// if we get here it was successful
		return true;
	}
	
	/**
	 * Save the data file in the incomplete dir
	 */
	private void handleIncompleteDir(String docid) {
		Log.debug(30, "~~~~~~~~~~~~~~~~~~~~~~handle incomplete " + docid);
		File dataFile = null;

		try {
			dataFile = openFile(docid);
			Log.debug(30, "Docid " + docid
							+ " exist in data dir in AbstractDataPackage.handleIncompleteDir");
			return;
		} catch (Exception m) {
			try {
				dataFile = openTempFile(docid);
				InputStream dfis = new FileInputStream(dataFile);
				saveIncompleteDataFile(docid, dfis);
				dfis.close();
			} catch (Exception qq) {
				// if a datafile is on metacat and user wants to save locally
				try {
					// open old file name (if no file change, the old file name will be as same as docid).
					InputStream dfis = new FileInputStream(dataFile);
					saveIncompleteDataFile(docid, dfis);
					dfis.close();
				} catch (Exception qqq) {
					// some other problem has occured
					Log.debug(5, "Some problem with saving local data files has occurred! "
									+ qqq.getMessage());
					qq.printStackTrace();
				}// end catch
			}
		}

	}
	
	/**
	 * Performs a local query
	 * TODO: refactor LocalQuery/Query code to conform to this interface?
	 */
	@Override
	public InputStream query(String query) {
		
		return null;
	}
  
  /**
   * Saves a file with the given name.  if the file does not exist it is created
   * The file is saved according to the name provided.   The file will
   * be saved to the &lt;datadir&gt;/&lt;scope&gt;/ directory 
   * where the filename is the accnum.
   * Example: 
   *    name=johnson2343.13223
   *    datadir=data
   *    complete path=/usr/local/morpho/profiles/johnson/data/johnson2343/13223
   * Any characters after the first separator are assumed to be part of the 
   * accession number.  Hence the id johnson2343.13223.5 would produce 
   * the file johnson2343/13223.5
   */
  public static File saveFile(String name, InputStream inputStream, String rootDir) {

		File file = null;
		try {
			FileSystemDataStore.getInstance(rootDir).set(name, inputStream);
			file = FileSystemDataStore.getInstance(rootDir).get(name);
		} catch (Exception e) {
			Log.debug(6, e.getMessage());
		}
		return file;

	}
  
  /**
   * Get the file associated with the identifier from the specified object directory.
   * @param identifier - identifier of the object.
   * @param objectDir - the specified the directory.
   * @return the file associated with the identifier.
   * @throws FileNotFoundException
   */
  public static File open(String identifier, String objectDir) throws FileNotFoundException{
    File file = null;
    try {
      file = FileSystemDataStore.getInstance(objectDir).get(identifier);
    } catch (Exception e) {
      FileNotFoundException fnfe = new FileNotFoundException(e.getMessage());
      fnfe.initCause(e);
      throw fnfe;
    }
    return file;
  }
  
  /**
   * returns a File object in the local repository.
   * @param name: the id of the file
   * @param file: the stream to the file
   */
  public File newFile(String name, InputStream inputStream)
  {
    return saveFile(name, inputStream, getDataDir());
  }
  
  
  public File newDataFile(String name, InputStream is) {
    return saveFile(name, is, getDataDir());
  }
  
  /**
	 * deletes a file from the local file system. returns true if the file is
	 * successfully deleted, false otherwise.
	 * 
	 * @param name
	 *            the name of the file to delete
	 */
	public boolean deleteFile(String name) {
		boolean success = false;
		try {
			success = FileSystemDataStore.getInstance(getDataDir()).delete(name);
			//success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.debug(30, "Delete success value is: " + success);
		return success;
	}
   
   /**
    * deletes a file from incomplete dir in the local file system. returns true if the file is
    * successfully deleted, false otherwise.
    * @param name the name of the file to delete
    */
    public boolean deleteInCompleteFile(String identifier) {
		

		boolean success = false;
		try {
			success = FileSystemDataStore.getInstance(getIncompleteDir()).delete(identifier);
			//success = true;
		} catch (Exception e) {
			//System.out.println("got an exception in deleting the local file");
			e.printStackTrace();
		}
		Log.debug(30, "the success value for deleting incomplete file " + identifier + " is " + success);
		return success;
	}
    
	/**
	 * Delete the auto-saved files for given abstract data package.
	 * @param adp
	 */
	public void deleteAutoSavedFile(AbstractDataPackage adp) {
		if (adp != null) {
			// delete the incomplete file
			String autoSavedID = adp.getAutoSavedD();
			if (autoSavedID != null) {
				deleteInCompleteFile(autoSavedID);
				adp.setAutoSavedID(null);
				// delete the data file too
				deleteDataFilesInIncompleteFolder(adp);
			}
		}
	}
    
	/**
	 * Deletes all associated data files in incomplete dir
	 */
	public void deleteDataFilesInIncompleteFolder(AbstractDataPackage adp) {
		if (adp.getEntityArray() != null) {
			for (int i = 0; i < adp.getEntityArray().length; i++) {
				String URLinfo = adp.getDistributionUrl(i, 0, 0);
				String protocol = AbstractDataPackage.getUrlProtocol(URLinfo);
				if (protocol != null && protocol.equals(AbstractDataPackage.ECOGRID)) {
					String docid = AbstractDataPackage.getUrlInfo(URLinfo);
					Log.debug(30, "handle data file  with index " + i + "" + docid);
					deleteInCompleteFile(docid);
				}
			}
		}
	}
  
	/**
	 * Delete the given ADP from the local store
	 * @param adp
	 * @throws FileNotFoundException
	 */
	@Override
	public boolean delete(MorphoDataPackage mdp) throws FileNotFoundException {
		
		String identifier = mdp.getAbstractDataPackage().getAccessionNumber();
		
		// TODO: delete other parts of the ADP
		return deleteFile(identifier);
		
		
	}
	
  
  /**
	 * returns an id for the local store for the current scope
	 * 
	 */
  	@Override
	public synchronized String generateIdentifier(String fragment) {
		int lastid = -1;
		String separator = Morpho.thisStaticInstance.getProfile().get("separator", 0);
		
		// Get last id found
		lastid = getLastDocid(fragment);
		
		// scope.X
		String identifier = fragment + separator + lastid;

		// set to the next in local for he next time we call this
		lastid++;
		if (!Morpho.thisStaticInstance.getProfile().set("lastId", 0, String.valueOf(lastid))) {
			Log.debug(1, "Error incrementing the locally stored docid");
			identifier = null;
		} else {
			Morpho.thisStaticInstance.getProfile().save();
			Log.debug(30, "the next id is " + identifier);
		}
		Log.debug(30, "generated  local identifier: " + identifier);

		return identifier;
	}
  
  /**
	 * Gets the max local id for given fragment in current profile.
	 */
	private int getLastDocid(String fragment) {

		int lastid = 1;
		String lastidS = morpho.getProfile().get("lastId", 0);
		try {
			lastid = Integer.parseInt(lastidS);
		} catch (Exception e) {
			Log.debug(30, "couldn't get lastid from profile");
		}
		Log.debug(30, "the last id from profile " + lastid);

		// check if we have used that one, find one we haven't
		String identifier = null;
		boolean inUse = false;
		do {
			lastid++;
			identifier = fragment + "." + lastid;
			inUse = exists(identifier);
		} while (inUse);
		
		return lastid;
	}
	
	  /**
	   * Gets the next available temp id from profile file.
	   * @return the next available id
	   */
	  public synchronized String getNextTempID()
	  {
	    long startID = 1;
	    long lastid = -1;
	    //Gets last id from profile
	    String lastidS = Morpho.thisStaticInstance.getProfile().get(TEMPIDNAME, 0);
	    String separator = Morpho.thisStaticInstance.getProfile().get("separator", 0);
	    try
	    {
	        lastid = (new Long(lastidS)).longValue();
	    }
	    catch(Exception e)
	    {
	      Log.debug(30, "couldn't get lastid for temp from profile");
	      lastid = startID;
	    }
	    String identifier = TEMP + separator + lastid;
	    lastid++;
	    String s = "" + lastid;
	    if (!Morpho.thisStaticInstance.getProfile().set(TEMPIDNAME, 0, s))
	    {
	      
	      boolean success = Morpho.thisStaticInstance.getProfile().insert(TEMPIDNAME, s);
	      if (success) {
	    	  Morpho.thisStaticInstance.getProfile().save();
	    	  return identifier + ".1"; 
	      }
	      else
	      {
	        Log.debug(1, "Error incrementing the accession number id");
	        return null;
	      }
	    }
	    else
	    {
	    	Morpho.thisStaticInstance.getProfile().save();
	      Log.debug(30, "the next id is "+identifier+".1");
	      return identifier + ".1"; 
	    }
	  }
  
  /**
   * Gets a data file from all local source. It will looks file in data dir, then in temporary dir, 
   * finally the incomplete dir
   * @param doicd
   * @return
   */
  public File getDataFileFromAllLocalSources(String docid) throws FileNotFoundException
  {
     File file = null;  
     if(docid != null && !docid.equals(""))
     {
       try
       {
         //try data file dir
         file = openFile(docid);
       }
       catch(Exception e)
       {
         try
         {
           //try temp file dir
           file = openTempFile(docid);
           //Log.debug(5, "from temp");
         }
         catch(Exception ee)
         {
           try
           {
             file = openIncompleteFile(docid);
           }
           catch(Exception eee)
           {
             throw new FileNotFoundException(eee.getMessage());
           }
         }
         
       }
     }
     if(file == null)
     {
       throw new FileNotFoundException("Couldn't find docid "+docid+" in morpho file system");
     }
     return file;
  }
  
  
  /**
	 * Get a list of all current identifiers for local store Location can be
	 * QueryRefreshInterface.LOCALINCOMPLETEPACKAGE for listing current
	 * incomplete identifiers
	 * 
	 * @param location
	 *            optional incomplete
	 *            (QueryRefreshInterface.LOCALINCOMPLETEPACKAGE) specifier
	 * @return list of identifiers
	 */
	public List<String> getAllIdentifiers(String location) {
		List<String> identifiers = null;
		try {
			if (location != null && location.equals(QueryRefreshInterface.LOCALINCOMPLETEPACKAGE)) {
				identifiers = FileSystemDataStore.getInstance(getIncompleteDir()).getIdentifiers();
			} else {
				identifiers = FileSystemDataStore.getInstance(getDataDir()).getIdentifiers();
			}
			// only get the latest revision
			identifiers = getLatestVersion(identifiers);
		} catch (Exception e) {
			e.printStackTrace();
			Log.debug(6, e.getMessage());
			return null;
		}

		return identifiers;
	}
	
  
	
	 /**
	 * modify list to only contain latest version as reported by 
	 * the RevisionManager
	 * @throws Exception 
	 */
	private List<String> getLatestVersion(List<String> identifiers) throws Exception {

		Vector<String> returnVector = null;

		if (identifiers != null) {
			returnVector = new Vector<String>();
			for (int i = 0; i < identifiers.size(); i++) {
				String identifier = identifiers.get(i);
				String obsoletedBy = getRevisionManager().getObsoletedBy(identifier);
				if (obsoletedBy == null) {
					returnVector.add(identifier);
				}	
			}
		}
		return returnVector;
	}
  
  /**
	 * Current implementation does not inspect the given identifier for a "fragment"
	 * and will simply use a new identifier for the current scope.
	 * @throws Exception 
	 */
	public String getNextIdentifier(String identifier) throws Exception {
		// default the fragment to be the current scope
		String fragment = Morpho.thisStaticInstance.getProfile().get("scope", 0);
		
		// TODO: can we glean anything about the current identifier?
		if (false) {
			// get a meaningful fragment?
		}
		
		// all we can do now is generate an identifier....
		String nextIdentifier = generateIdentifier(fragment);
		return nextIdentifier;
	}
	
	/**
	 * retrieve the SystemMetadata associated with this identifier
	 * @param identifier
	 * @return
	 * @throws Exception
	 */
	public SystemMetadata getSystemMetadata(String identifier) throws Exception {
		
		File file = null;
		SystemMetadata sysMeta= null;
		try {
			file = FileSystemDataStore.getInstance(getSystemMetadataDir(getDataDir())).get(identifier);
			if (file != null) {
				sysMeta = TypeMarshaller.unmarshalTypeFromFile(SystemMetadata.class, file);
			}
		} catch (Exception e) {
			// nope
			Log.debug(30, "SystemMetadata file not found for identifier: " + identifier);
		}
		
		return sysMeta;
		
	}
	
	/**
	 * save the SystemMetadata associated with this identifier
	 * @param identifier
	 * @return
	 * @throws Exception
	 */
	private boolean saveSystemMetadata(SystemMetadata sysMeta) throws Exception {
		
		File file = null;
		String identifier = null;
		try {
			identifier = sysMeta.getIdentifier().getValue();
			FileSystemDataStore.getInstance(getSystemMetadataDir(getDataDir())).set(identifier, null);
			file = FileSystemDataStore.getInstance(getSystemMetadataDir(getDataDir())).get(identifier);
			if (file != null) {
				TypeMarshaller.marshalTypeToOutputStream(sysMeta, new FileOutputStream(file));
			}
		} catch (Exception e) {
			// nope
			Log.debug(10, "SystemMetadata could not be saved for identifier: " + identifier);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get the RevisionManager
	 * @return the RevisionManager being used
	 */
	public RevisionManagerInterface getRevisionManager() {
		RevisionManager revisionManager = null;
		try {
			revisionManager = RevisionManager.getInstance(getProfileDir(), DataPackageInterface.LOCAL);
		} catch (Exception e) {
			Log.debug(6, "Could not get RevisionManager: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
		return revisionManager;
	}
	 
	/**
	 * Serialize data into morpho locally when user imports an external EML file
	 * to morpho. This method will be called in ImportEMLFileCommand. This
	 * method only serialize local file (on distrubition url) to morpho. It
	 * wouldn't serialize any http, ftp and ecogrid url into morpho
	 */
	public void serializeDataInImportExternalEMLFile(AbstractDataPackage adp) {
		if (adp.getEntityArray() == null) {
			Log.debug(30,"Entity array is null, no need to serialize data in AbstractDataPackage.serializeDataInImportExternalEMLFile()");
			return; // there is no data!
		}
		for (int i = 0; i < adp.getEntityArray().length; i++) {
			String URLinfo = adp.getDistributionUrl(i, 0, 0);
			String protocol = AbstractDataPackage.getUrlProtocol(URLinfo);
			if (!AbstractDataPackage.isProtocolInList(protocol)) {
				if (protocol != null && protocol.equalsIgnoreCase(AbstractDataPackage.FILE)) {
					// this is a file protocol. we may try to remove the file
					// protocol and to see if it is a local file
					URLinfo = AbstractDataPackage.removeFileProtocol(URLinfo);
				}
				// This is not online url. It may be a local file.
				File localFile = null;
				try {
					localFile = new File(URLinfo);
				} catch (Exception e) {
					Log.debug(30, "The online url " + URLinfo
							+ " couldn't be found in file stystem since "
							+ e.getMessage());
					return;
				}
				if (localFile != null && localFile.isFile()) {
					// now we copy the file into morpho
					String identifier = DataStoreServiceController.getInstance().generateIdentifier(DataPackageInterface.LOCAL);
					try {
						String objectName = adp.getEntityName(i);
						newDataFile(identifier, localFile, objectName);
						// now we can modify the online url in metadata.
						String url = AbstractDataPackage.ECOGRID + "://knb/" + identifier;
						adp.setDistributionUrl(i, 0, 0, url);

					} catch (Exception e) {
						Log.debug(30, "couldn't serialize local file "
										+ localFile.getAbsolutePath()
										+ " into morpho "
										+ " in AbstractDataPacakge.serializeDataInImportExternalEMLFile");
					}
				}
			}
		}
	}
	
	/**
	   * Test method
	   */
	  public static void main(String[] args)
	  {
	    String filename = args[0];
	    String filename2 = args[1];
	    String action = args[2];
	    if(action.equals("test"))
	    {
	      try
	      {
	        Morpho morpho = new Morpho(new ConfigXML("./lib/config.xml"));
	        LocalDataStoreService fsds = new LocalDataStoreService(morpho);
	        File newfile = fsds.openFile(filename);
	        fsds.saveFile(filename2, new FileInputStream(newfile));
	      }
	      catch(Exception e)
	      {
	        e.printStackTrace();
	      }
	    }
	    else if(action.equals("save"))
	    {
	      try
	      {
	        Morpho morpho = new Morpho(new ConfigXML("./lib/config.xml"));
	        LocalDataStoreService fsds = new LocalDataStoreService(morpho);
	        File newfile = new File(filename);
	        fsds.saveFile(filename2, new FileInputStream(newfile));
	      }
	      catch(Exception e)
	      {
	        e.printStackTrace();
	      }
	    }
	    Log.debug(20, "done");
	  }
}
