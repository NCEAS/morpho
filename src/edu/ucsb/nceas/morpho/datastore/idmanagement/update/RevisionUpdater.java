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
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import edu.ucsb.nceas.morpho.datastore.DataStoreService;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierManager;
import edu.ucsb.nceas.morpho.datastore.idmanagement.RevisionManager;
import edu.ucsb.nceas.morpho.util.Log;


/**
 * This class will build revision property files for the existing Morpho 1.x object 
 * files. From morpho 2.0, the identifier or name couldn't tell the revision history.
 * We have a new mechanism to keep track the history. This class will put existing 
 * objects into the new mechanism.
 * @author tao
 *
 */
public class RevisionUpdater extends IdentifierFileMapUpdater {
  private static final String REVISIONFILEPREFIX = "local-store-service";
  
  private Hashtable<String, Vector<Integer>> revisionsKeeper = new Hashtable<String, Vector<Integer>>();
  
  /**
   * Update one profile for the revision history. This method will overwrite
   * the one in the parent class - IdentifierFileMapUpdater.
   * @param info - the profile which will be updated.
   * @throws Exception
   */
  protected void update(ProfileInformation info) throws Exception {
    Vector<ObjectDirectory> objectDirs = info.getRevisionDirectories();
    if(objectDirs != null) {  
      String profileDir = DataStoreService.getProfileDir(info.getProfile());
      RevisionManager manager = new RevisionManager(profileDir, REVISIONFILEPREFIX);
      for(ObjectDirectory objectDir : objectDirs) {
        File dir = objectDir.getDirectory();
        boolean isQueryDir = objectDir.isQueryDirectory();            
        try {          
            //for non-query directory, we will look up the files under the scope directories which are the child directories of this directory
            //for query directory, we will do nothing.
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
                          String name = file.getName();
                          getRevison(name);
                        }
                      }
                  }
                }
              }             
          } 
           
        } catch (Exception e) {
          String error = "RevisionUpdater.update - the generating of the revision history for the directory "+
                dir.getAbsolutePath() +" failed:\n"+e.getMessage()+
                "\nYou may ask morpho-dev@ecoinformatics.org for the help.";
          Log.debug(20, error);
          throw new Exception(error);
        }
      }
      buildRevisionsProperties(manager);
    }
  }
  
  /**
   * Parse the file name to get the revision and put it into data structure to store it.
   * If the file name doesn't match the pattern - prefix.number, it will be skipped.
   * @param fileName - the name will be parsed.
   */
  private void getRevison(String fileName) {
    if(fileName != null) {
      int index = fileName.lastIndexOf(IdentifierManager.DOT);
      //dot should exist and couldn't be the last character in the file name
      if(index != -1 && (index+1) < fileName.length()) {
        String prefix = fileName.substring(0, index+1);
        String revisionStr = fileName.substring(index+1);
        int revision =-1;
        try {
          revision = (new Integer(revisionStr)).intValue();
          Vector<Integer> revisionList = revisionsKeeper.get(prefix);
          if(revisionList == null) {
            revisionList = new Vector<Integer> ();
            revisionList.add(new Integer(revision));
            revisionsKeeper.put(prefix, revisionList);
          } else {
            revisionList.add(new Integer(revision));
          }          
        } catch (NumberFormatException e){
          Log.debug(20, "RevisionUpdate.getRevision - file "+fileName+ " doesn't match our pattern for the file name prefix.number."+
                        " So it will be skipped for building revision history");
        }
      }
    }
  }
  
  /*
   * Build the revision properties for the identifier which has more than 1 revsions.
   */
  private void buildRevisionsProperties(RevisionManager manager) throws NullPointerException {
    if(manager == null) {
      throw new NullPointerException("RevisionUpdater.buildRevisionsProperties - the RevisionManager object shouldn't be null.");
    }
    Set <Map.Entry<String, Vector<Integer>>> set = revisionsKeeper.entrySet();
    if(set != null) {
      for(Map.Entry<String, Vector<Integer>> entry : set) {
        if(entry != null) {
          String prefix = entry.getKey();
          Vector<Integer> revisions = entry.getValue();
          //only build the revision properties when revisions' length is greater than 1 (at least it has two versions).
          if(revisions != null && revisions.size() >1) {
            //sort it into ascending order
            Collections.sort(revisions);
            for(int i=0; i<revisions.size(); i++) {
              if(i==0) {
                //the first version only has obsoletedBy property
                manager.setObsoletedBy(prefix+revisions.elementAt(i), prefix+revisions.elementAt(i+1));
              } else if (i==(revisions.size()-1)) {
                //the last version only has obsoletes property
                manager.setObsoletes(prefix+revisions.elementAt(i), prefix+revisions.elementAt(i-1));
              } else {
                // the other versions have both obsoletedBy and obsoletes properties
                manager.setObsoletedBy(prefix+revisions.elementAt(i), prefix+revisions.elementAt(i+1));
                manager.setObsoletes(prefix+revisions.elementAt(i), prefix+revisions.elementAt(i-1));
              }
            }
          }
        }
      }
    }
  }

}
