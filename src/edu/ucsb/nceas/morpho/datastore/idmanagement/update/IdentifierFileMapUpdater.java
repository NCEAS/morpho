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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataStoreService;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierFileMap;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierManager;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ProfileDialog;
import edu.ucsb.nceas.morpho.util.Log;


/**
 * In Morpho 1.*, the map between a identifier and a file name is transparent. You can
 * determine the file name from the identifier. From Morpho 2.0, the map is opaque. Morpho
 * maintains a text file for the map. Morpho can only look up this text file to get the file 
 * name for a specified identifier. So we should have this updater to harvest the existing map 
 * in the old system and put them into the text file. This updater only needs to be run once.
 * @author tao
 *
 */
public class IdentifierFileMapUpdater {
  private Vector<ProfileInformation> profileInformationList = null;
  /**
   * Constructor
   */
  public IdentifierFileMapUpdater()
  {
    profileInformationList = new Vector<ProfileInformation> ();
  }
  
  /**
   * Determine if morpho needs to run the update for generate the id-filename map
   * @return true if morpho needs to run the update; false otherwise.
   */
  public boolean needUpdate() throws FileNotFoundException, NullPointerException {
    boolean need = false;
  //Gets every profile name
    String[] profileList = Morpho.thisStaticInstance.getProfilesList();
    if (profileList != null)
    {
      
      for (int i=0; i<profileList.length; i++)
      {
        String profileName = profileList[i];
        //morpho.setProfileDontLogin(profileName, 30);
        ConfigXML profile = getProfile(profileName);
        if (profile != null)
        {
         ProfileInformation information = new ProfileInformation(profile);
         if(information.getUpdatedStatus() == false) {
           need = true;
           profileInformationList.add(information);
         }
         
        }
      }
    }
    return need;
  }
  
  /**
   * Create the id-filename map for the existing morpho 1.* profile directories. If the morpho
   * has been updated, this method will do nothing.
   */
  public void update() {
    for(ProfileInformation info : profileInformationList) {
      try {
       update(info);
       addFlagToProfile(info);
      } catch (Exception e) {
        //The reason we catch the generic exception is we don't want the failure on
        //one profile disrupt the entire updating 
        Log.debug(8,e.getMessage());
        removeFlagFromProfile(info);
        continue;
      }
      
    }
    
  }
  
  
  /**
   * Update one profile for id-file mapping.
   * @param info - the profile which will be updated.
   * @throws Exception
   */
  protected void update(ProfileInformation info) throws Exception{
    Vector<ObjectDirectory> objectDirs = info.getIdFileMappingDirectories();
    if(objectDirs != null) {  
        for(ObjectDirectory objectDir : objectDirs) {
          File dir = objectDir.getDirectory();
          boolean isQueryDir = objectDir.isQueryDirectory();            
          try {
            IdentifierFileMap map = new IdentifierFileMap(dir);
            //for non-query directory, we will look up the files under the scope directories which are the child directories of this directory
            //for query directory, we will look up the files directory under the query directory
            if (!isQueryDir) {
              File[] scopeDirs = dir.listFiles();
              if(scopeDirs != null) {
                for(int i=0; i<scopeDirs.length; i++) {
                  File scopeDir = scopeDirs[i];
                  //only look up the files under the scope dir
                  if(scopeDir.isDirectory()) {
                      File[] fileList = scopeDir.listFiles();
                      if(fileList != null) {
                        for(int j=0; j<fileList.length; j++) {
                          File file = fileList[j];
                          String id = scopeDir.getName()+IdentifierManager.DOT+file.getName();
                          map.setMap(id, file);
                        }
                      }
                  }
                }
              }             
            } else {
              File[] queryFiles = dir.listFiles();
              if(queryFiles != null) {
                for(File queryFile : queryFiles) {
                  if(queryFile != null && queryFile.exists() && queryFile.isFile() && !queryFile.isHidden()) {
                      String id = queryFile.getName();
                      map.setMap(id, queryFile);
                  }
                 
                }
              }
            }
           
          } catch (Exception e) {
            String error = "IdentifierFileMapUpdater.update - the generating of the id-filename mapping property for the directory "+
                dir.getAbsolutePath() +" failed:\n"+e.getMessage()+
                "\nYou may ask morpho-dev@ecoinformatics.org for the help.";
            Log.debug(11, error);
            throw new Exception(error);
          }
        }
      
      
    }
  }
  
  /**
   * Add a tag to indicate the update has been done to specified profile.
   * @param info - the profile will be modified.
   */
  private void addFlagToProfile(ProfileInformation info) {
      //need to remove the path from the file first
      info.getProfile().removeNode(ProfileDialog.IDFILEMAPUPDATEDPATH, 0);
      info.getProfile().insert(ProfileDialog.IDFILEMAPUPDATEDPATH, "true");
      info.getProfile().save();
   
  }
  
  /**
   * Remove the tag to indicate the update has been done to specified profile.
   * @param info
   */
  private void removeFlagFromProfile(ProfileInformation info) {
    info.getProfile().removeNode(ProfileDialog.IDFILEMAPUPDATEDPATH, 0);
    info.getProfile().save();
  }
  
  /*
   * Get a profile for given profile name. If couldn't find this file, null will be returned.
   */
  private ConfigXML getProfile(String profileName) throws FileNotFoundException
  {
    ConfigXML profile = null;   
    String profileDir = DataStoreService.getProfilesParentDir();
    String newProfilePath = profileDir + File.separator +
                      profileName + File.separator + profileName + ".xml";
    profile = new ConfigXML(newProfilePath); 
    return profile;
  }

}
