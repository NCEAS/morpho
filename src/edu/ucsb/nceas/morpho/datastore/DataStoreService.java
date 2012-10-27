/**
 *  '$RCSfile: DataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2009-02-06 21:26:34 $'
 * '$Revision: 1.10 $'
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

import java.io.File;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ProfileDialog;

/**
 * creates an abstract class for getting files from any dataStore using the same
 * methods.
 */
public abstract class DataStoreService implements DataStoreServiceInterface
{
  protected Morpho morpho;
  protected ConfigXML config;
  
  /**
   * create a new FileSystemDataStore for a Morpho
   */
  public DataStoreService(Morpho morpho)
  {
    this.morpho = morpho;
    config = Morpho.getConfiguration();

  }
  
  /**
   * Gets the temp directory
   * @return
   */
  protected String getTempDir() {
		ConfigXML profile = morpho.getProfile();
		String tempdir = getProfileDir(profile) + File.separator + profile.get("tempdir", 0);
		return tempdir;
	}
	
	/**
	 * Get the profile directory for a specified profile
	 * @return the profile directory path string.
	 */
	public static String getProfileDir(ConfigXML profile) {
	  return getProfilesParentDir()+
	      File.separator+profile.get(ProfileDialog.PROFILENAMEELEMENTNAME, 0);
	}
	
	/**
   * Get the currently using profile directory
   * @return the profile directory path string.
   */
  public static String getProfileDir() {
    return getProfilesParentDir() + File.separator+
        Morpho.thisStaticInstance.getProfile().get(ProfileDialog.PROFILENAMEELEMENTNAME, 0);
  }
  
  
  /**
   * Get the the directory which contains all profiles directories
   * @return the path of directory which contains all profiles directories
   */
  public static String getProfilesParentDir() {
    return ConfigXML.getConfigDirectory()+File.separator+Morpho.getConfiguration().get("profile_directory", 0);
  }
  
  
  /*
   * Get the cache directory for the current profile.
   */
  protected String getCacheDir() {
    ConfigXML profile = morpho.getProfile();
    String profileDirName = ConfigXML.getConfigDirectory() + File.separator
        + config.get("profile_directory", 0) + File.separator
        + profile.get("profilename", 0);
    String cachedir = profileDirName + File.separator
        + profile.get("cachedir", 0);
    return cachedir;
  }
  
  /*
   * Get the system metadata directory associated with the object directory.
   * This method doesn't guarantee the existing of the system metadata directory. 
   */
  protected String getSystemMetadataDir(String objectDir) {
    return objectDir+File.separator+ProfileDialog.SYSTEMMETADATADIRNAME;
  }

}
