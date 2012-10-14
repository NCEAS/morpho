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
  private Vector<IdFileMapProfileInformation> profileInformationList = null;
  /**
   * Constructor
   */
  public IdentifierFileMapUpdater()
  {
    profileInformationList = new Vector<IdFileMapProfileInformation> ();
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
         IdFileMapProfileInformation information = new IdFileMapProfileInformation(profile);
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
    for(IdFileMapProfileInformation info : profileInformationList) {
      try {
        Vector<File> objectDirs = info.getObjectDirectories();
        if(objectDirs != null) {
          
            for(File dir : objectDirs) {
                IdentifierFileMap map = new IdentifierFileMap(dir);

              try {
                File[] scopeDirs = dir.listFiles();
                if(scopeDirs != null) {
                  for(int i=0; i<scopeDirs.length; i++) {
                    File scopeDir = scopeDirs[i];
                    try {
                      
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
                    }catch (Exception e) {
                      Log.debug(11,"IdentifierFileMapUpdater.update - the generating of the id-filename map for the director "+
                          scopeDir.getAbsolutePath() +" failed:\n"+e.getMessage()+
                          "\nYou may ask morpho-dev@ecoinformatics.org for the help.");
                    }
                  }
                }
              } catch (Exception e) {
                Log.debug(11,"IdentifierFileMapUpdater.update - the generating of the id-filename map for the director "+
                    dir.getAbsolutePath() +" failed:\n"+e.getMessage()+
                    "\nYou may ask morpho-dev@ecoinformatics.org for the help.");
              }
            }
          
          
        }
       
        //need to remove the path from the file first
        info.getProfile().removeNode(ProfileDialog.IDFILEMAPUPDATEDPATH, 0);
        info.getProfile().insert(ProfileDialog.IDFILEMAPUPDATEDPATH, "true");
        info.getProfile().save();
      } catch (Exception e) {
        //The reason we catch the generic exception is we don't want the failure on
        //one profile disrupt the entire updating 
        Log.debug(8,"IdentifierFileMapUpdater.update - the generating of the id-filename map for profile "+
                  info.getProfile().get(ProfileDialog.PROFILENAMEELEMENTNAME, 0) +" failed:\n"+e.getMessage()+
                  "\nYou may ask morpho-dev@ecoinformatics.org for the help.");
        continue;
      }
      
    }
    
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
