/**
 *  '$RCSfile: DataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-11 21:51:05 $'
 * '$Revision: 1.1 $'
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
  
  /**
   * create a new FileSystemDataStore for a ClientFramework
   */
  public DataStore(ClientFramework cf)
  {
    this.framework = cf;
    config = framework.getConfiguration();
    Vector datadirV = config.get("local_xml_directory");
    datadir = (String)datadirV.elementAt(0);
    Vector separatorV = config.get("separator");
    separator = (String)separatorV.elementAt(0);
    separator = separator.trim();
    Vector cachedirV = config.get("local_cache_directory");
    cachedir = (String)cachedirV.elementAt(0);
  }
  
  public void debug(int code, String message)
  {
    framework.debug(code, message);
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
  
  abstract public File openFile(String name) throws FileNotFoundException;
  abstract public File saveFile(String name, Reader file);
  abstract public File newFile(String name);
}
