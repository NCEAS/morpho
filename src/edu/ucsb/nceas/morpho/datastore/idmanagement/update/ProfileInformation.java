/**
 *        Name: AttributeEditDialog.java
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2005-02-22 23:21:51 $'
 * '$Revision: 1.9 $'
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
package edu.ucsb.nceas.morpho.datastore.idmanagement.update;

import java.io.File;
import java.util.Vector;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataStoreService;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ProfileDialog;



/**
 * This class represents an object containing the information for the updating the
 * existing legacy Morpho 1.* system for one profile. Those information includes
 * if the profile has been updated and the list of directories which should be updated
 * @author tao
 *
 */
public class ProfileInformation {
  private ConfigXML profile = null;
  private boolean updated = false;
  private Vector<File> idFileMappingDirectories = new Vector<File>();
  private Vector<File> revisionDirectories = new Vector<File>();
  
  /**
   * Constructor. It will parse the profile and get the information.
   * @param profile - the profile which will be parsed.
   */
  public ProfileInformation(ConfigXML profile) throws NullPointerException {
    if(profile == null) {
      throw new NullPointerException("IdFileMapProfileInformation.IdFileMapProfileInformation - the parameter profile can't be null");
    }
    this.profile = profile;
    parse();
  }
  
  /*
   * Parse the profile 
   */
  private void parse() {
    if(profile != null)
    {
      String updatedElementValue = profile.get(ProfileDialog.IDFILEMAPUPDATEDPATH, 0);
      if (updatedElementValue != null)
      {
          updated = (new Boolean(updatedElementValue)).booleanValue();
      }
      else
      {
        // There is no updated element in the profile. So it hasn't been updated
        updated = false;
      }
      
      if(!updated) {
        Vector list = profile.getValuesForPath(ProfileDialog.DATADIRELEMENTNAME);
        handleDirectoryList(list, idFileMappingDirectories);
        handleDirectoryList(list, revisionDirectories);
        list = profile.getValuesForPath(ProfileDialog.CACHEDIRELEMENTNAME);
        handleDirectoryList(list, idFileMappingDirectories);
        list = profile.getValuesForPath(ProfileDialog.INCOMPLETEDIRELEMENTNAME);
        handleDirectoryList(list, idFileMappingDirectories);
        handleDirectoryList(list, revisionDirectories);
        list = profile.getValuesForPath(ProfileDialog.TEMPDIRELEMENTNAME);
        handleDirectoryList(list, idFileMappingDirectories);
        handleDirectoryList(list, revisionDirectories);
      }
    }
  }
  
  /*
   * Handle the specified list of object directory. If the directory is an absolute path,
   * put it into the objectDirectories. If the directory is a relative path, append it to the
   * profile directory and put it into the objetDirectories.
   */
  private void handleDirectoryList(Vector list, Vector<File> targetDirectories) {
    if(list != null) {
      for(int i=0; i<list.size(); i++) {
        String dir = (String)list.elementAt(i);
        if(dir != null && !dir.trim().equals("")) {
          File fileDir = new File(dir);
          if(!fileDir.isAbsolute()) {
            String profileDirectory = DataStoreService.getProfileDir(profile);
            fileDir = new File(profileDirectory, File.separator+dir);
          } 
          //we only handle existing directory
          if(fileDir.exists() && fileDir.isDirectory()) {
           
            targetDirectories.add(fileDir);
            
          }
        }
      }
    }
  }
  
  /**
   * Get the status of updating from the profile
   * @return true if the the profile has been updated; false else. 
   */
  public boolean getUpdatedStatus() {
    return updated;
  }
  
  
  /**
   * Get the list of the object directories which should be updated for the id-file mapping.
   * @return the list of object directories 
   */
  public Vector<File> getIdFileMappingDirectories() {
    return idFileMappingDirectories;
  }
  
  /**
   * Get the list of directories which should be updated for the revisions history.
   * @return the list of directories which will be updated for the revision history.
   */
  public Vector<File> getRevisionDirectories() {
    return revisionDirectories;
  }
  
  /**
   * Get the specified profile.
   * @return the profile.
   */
  public ConfigXML getProfile() {
    return profile;
  }
}
