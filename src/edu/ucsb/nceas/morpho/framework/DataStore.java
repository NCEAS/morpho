/**
 *  '$RCSfile: DataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-12 23:09:36 $'
 * '$Revision: 1.5 $'
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

package edu.ucsb.nceas.morpho.framework;

import java.io.*;
import java.util.*;

/**
 * creates an abstract class for getting files from any dataStore using the same
 * methods.
 */
public abstract class DataStore implements DataStoreInterface
{
  private ClientFramework framework;
  private ConfigXML config;
  protected String datadir;
  protected String separator;
  protected String cachedir;
  protected String tempdir;
  
  /**
   * create a new FileSystemDataStore for a ClientFramework
   */
  public DataStore(ClientFramework cf)
  {
    this.framework = cf;
    config = framework.getConfiguration();
    ConfigXML profile = framework.getProfile();
    String profileDirName = config.get("profile_directory", 0) + 
                            File.separator +
                            config.get("current_profile", 0);
    datadir = profileDirName + File.separator + profile.get("datadir", 0);
    tempdir = profileDirName + File.separator + profile.get("tempdir", 0);
    cachedir = profileDirName + File.separator + profile.get("cachedir", 0);
    separator = profile.get("separator", 0);
    separator = separator.trim();
  }
  
  public void debug(int code, String message)
  {
    framework.debug(code, message);
  }
  
  /**
   * parses id and adds a revision number to it.  if there is already a revision
   * number it adds one to it and returns the new id.  if addOne is true and the
   * then one is added to the existing revision number 
   */
  protected String incRev(String id, boolean addOne)
  {
    ConfigXML config = framework.getConfiguration();
    String sep = config.get("separator", 0);
    int count = 0;
    for(int i=0; i<id.length(); i++)
    {
      if(id.charAt(i) == sep.trim().charAt(0))
      {
        count++;
      }
    }
    
    if(count == 1)
    {
      return id + ".1";
    }
    
    int revIndex = id.lastIndexOf(".");
    String revNumStr = id.substring(revIndex + 1, id.length());
    Integer revNum = new Integer(revNumStr);
    int rev = revNum.intValue();
    if(addOne)
    {
      rev++;
    }
    return id.substring(0, revIndex) + "." + rev;
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
  
  protected String parseIdFromMessage(String message)
  {
    int docidIndex = message.indexOf("<docid>") + 1;
    int afterDocidIndex = docidIndex + 6;
    String docid = message.substring(afterDocidIndex, 
                                     message.indexOf("<", afterDocidIndex));
    debug(9, "docid in parseIdFromMessage: " + docid);
    return docid;
  }
  
  abstract public File openFile(String name) throws FileNotFoundException, 
                                                    CacheAccessException;
  abstract public File saveFile(String name, Reader file, boolean publicAccess)
           throws Exception;
  abstract public File newFile(String name, Reader file, boolean publicAccess)
           throws Exception;
}
