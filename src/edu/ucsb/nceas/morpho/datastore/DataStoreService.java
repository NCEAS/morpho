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
import java.io.FileNotFoundException;
import java.io.Reader;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ProfileDialog;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * creates an abstract class for getting files from any dataStore using the same
 * methods.
 */
public abstract class DataStoreService implements DataStoreServiceInterface
{
  protected Morpho morpho;
  private ConfigXML config;
  protected String separator;
  public final static String INCOMPLETEDIR = "incomplete";
  
  /**
   * create a new FileSystemDataStore for a Morpho
   */
  public DataStoreService(Morpho morpho)
  {
    this.morpho = morpho;
    config = Morpho.getConfiguration();
    ConfigXML profile = morpho.getProfile();
    separator = profile.get("separator", 0);
    separator = separator.trim();
  }
  
  /**
   * Gets the temp directory
   * @return
   */
  protected String getTempDir() {
		ConfigXML profile = morpho.getProfile();
		String profileDirName = ConfigXML.getConfigDirectory() + File.separator
				+ config.get("profile_directory", 0) + File.separator
				+ profile.get("profilename", 0);
		String tempdir = profileDirName + File.separator
				+ profile.get("tempdir", 0);

		return tempdir;
	}
  
  /**
   * Gets the data dir directory
   * @return
   */
  protected String getDataDir() {
		ConfigXML profile = morpho.getProfile();
		String profileDirName = ConfigXML.getConfigDirectory() + File.separator
				+ config.get("profile_directory", 0) + File.separator
				+ profile.get("profilename", 0);
		String datadir = profileDirName + File.separator
				+ profile.get("datadir", 0);
		return datadir;
	}
  
  protected String getCacheDir() {
		ConfigXML profile = morpho.getProfile();
		String profileDirName = ConfigXML.getConfigDirectory() + File.separator
				+ config.get("profile_directory", 0) + File.separator
				+ profile.get("profilename", 0);
		String cachedir = profileDirName + File.separator
				+ profile.get("cachedir", 0);
		return cachedir;
	}
  
  protected String getIncompleteDir() {
		String incompletedir = null;
		ConfigXML profile = morpho.getProfile();
		String profileDirName = ConfigXML.getConfigDirectory() + File.separator
				+ config.get("profile_directory", 0) + File.separator
				+ profile.get("profilename", 0);
		String incomplete = profile.get("incompletedir", 0);
		// in case no incomplete dir in old version profile
		if (incomplete == null || incomplete.trim().equals("")) {
			incompletedir = profileDirName + File.separator + INCOMPLETEDIR;
		} else {
			incompletedir = profileDirName + File.separator + incomplete;
		}
		return incompletedir;
	}

	public void debug(int code, String message) {
		Log.debug(code, message);
	}
	
	/**
	 * Get the profile directory for a specified profile
	 * @return the profile directory path string.
	 */
	public static String getProfileDir(ConfigXML profile) {
	  return ConfigXML.getConfigDirectory()+File.separator+Morpho.getConfiguration().get("profile_directory", 0)+
	      File.separator+profile.get(ProfileDialog.PROFILENAMEELEMENTNAME, 0);
	}
	
	/**
   * Get the currently using profile directory
   * @return the profile directory path string.
   */
  public static String getProfileDir() {
    return ConfigXML.getConfigDirectory()+File.separator+Morpho.getConfiguration().get("profile_directory", 0)+
        File.separator+Morpho.thisStaticInstance.getProfile().get(ProfileDialog.PROFILENAMEELEMENTNAME, 0);
  }
  
  /** 
   * Parses a dotted notation id into a file path.  johnson2343.13223 becomes
   * johnson2343/13223.  Revision numbers are left on the end so
   * johnson2343.13223.2 becomes johnson2343/13223.2
   */
  protected String parseId(String id) 
  {
    String path = new String();
    path = id.substring(0, id.indexOf("."));
    path += "/" + id.substring(id.indexOf(separator) + 1, id.length());
    return path;
  }

  abstract public File openFile(String name) throws FileNotFoundException, 
                                                    CacheAccessException;
  abstract public File saveFile(String name, Reader file)
           throws Exception;
  abstract public File newFile(String name, Reader file)
           throws Exception;
  abstract public boolean deleteFile(String name) throws Exception;
}
