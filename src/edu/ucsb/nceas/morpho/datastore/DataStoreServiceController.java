/**
 * 
 */
package edu.ucsb.nceas.morpho.datastore;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.query.LocalQuery;

/**
 * This singleton class allows callers to interact with data store services
 * for a given location.
 * 
 * @author leinfelder
 *
 */
public class DataStoreServiceController {
	
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
	
	private static DataStoreServiceController instance;
	
	private DataStoreServiceController() {
		
	}
	
	public static DataStoreServiceController getInstance() {
		if (instance == null) {
			instance = new DataStoreServiceController();
		}
		return instance;
	}
	
	
	  /**
	   * Deletes the package from the specified location
	   * @param locattion the location of the package that you want to delete
	   * use either BOTH, METACAT or LOCAL
	   */

	  public void delete(AbstractDataPackage adp, String location) throws Exception {
		boolean metacatLoc = false;
		boolean localLoc = false;
		String accnum = adp.getAccessionNumber();

		if (location.equals(METACAT) || location.equals(BOTH)) {
			metacatLoc = true;
		}
		if (location.equals(LOCAL) || location.equals(BOTH)) {
			localLoc = true;
		}
		if (localLoc) {
			FileSystemDataStore fileSysDataStore = new FileSystemDataStore(Morpho.thisStaticInstance);
			boolean localSuccess = fileSysDataStore.deleteFile(accnum);
			if (!localSuccess) {
				throw new Exception("User couldn't delete the local copy");
			}
			LocalQuery.removeFromCache(accnum);
		}
		if (metacatLoc) {
			boolean success = Morpho.thisStaticInstance.getMetacatDataStore().deleteFile(accnum);
			if (!success) {
				throw new Exception("User couldn't delete the network copy");
			}
		}
	}

}
