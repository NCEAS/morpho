/**
 *  '$RCSfile: FileSystemDataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-10 21:16:20 $'
 * '$Revision: 1.3 $'
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
 * implements and the DataStoreInterface for accessing files on the local
 * file system.
 */
public class FileSystemDataStore implements DataStoreInterface
{
  private ClientFramework framework;
  ConfigXML config;
  String datadir;
  String separator;
  
  /**
   * create a new FileSystemDataStore for a ClientFramework
   */
  public FileSystemDataStore(ClientFramework cf)
  {
    this.framework = cf;
    config = framework.getConfiguration();
    Vector datadirV = config.get("local_xml_directory");
    datadir = (String)datadirV.elementAt(0);
    Vector separatorV = config.get("separator");
    separator = (String)separatorV.elementAt(0);
    separator = separator.trim();
  }
  
  /**
   * opens a file with the given name.  the name should be in the form
   * scope.accnum where the scope is unique to this machine.  The file will
   * be opened from the &lt;local_xml_directory&gt;/&lt;scope&gt;/ directory 
   * where the filename is the accnum.
   * Example: name=johnson2343.13223
   *          local_xml_directory=/usr/local/morpho/data
   *          complete file path=/usr/local/morpho/data/johnson2343/13223
   * Any characters after the first separator are assumed to be part of the 
   * accession number.  Hence the id johnson2343.13223.5 would produce 
   * the file johnson2343/13223.5
   */
  public File openFile(String name) throws FileNotFoundException
  {
    framework.debug(9, "opening files from: " + datadir);
    String path = parseId(name);
    path = datadir + "/" + path;
    File file = new File(path);
    if(!file.exists())
    {
      throw new FileNotFoundException("file " + path + " does not exist");
    }
    
    return file;
  }
  
  /**
   * Saves a file with the given name.  if the file does not exist it is created
   * The file is saved according to the name provided.   The file will
   * be saved to the &lt;local_xml_directory&gt;/&lt;scope&gt;/ directory 
   * where the filename is the accnum.
   * Example: name=johnson2343.13223
   *          local_xml_directory=/usr/local/morpho/data
   *          complete file path=/usr/local/morpho/data/johnson2343/13223
   * Any characters after the first separator are assumed to be part of the 
   * accession number.  Hence the id johnson2343.13223.5 would produce 
   * the file johnson2343/13223.5
   */
  public void saveFile(String name, Reader file)
  {
    try
    {
      String path = parseId(name);
      String dirs = path.substring(0, path.lastIndexOf("/"));
      File savefile = new File(datadir + "/" + path); //the path to the file
      File savedir = new File(datadir + "/" + dirs); //the dir part of the path
      if(!savefile.exists())
      {//if the file isn't there create it.
        try
        {
          savedir.mkdirs(); //create any directories
          savefile.createNewFile(); //create the file
        }
        catch(Exception ee)
        {
          ee.printStackTrace();
        }
      }
      
      FileWriter writer = new FileWriter(savefile); 
      while(file.ready())
      {
        writer.write(file.read()); //write out everything in the reader
      }
      writer.flush();
      writer.close();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public File newFile(String name)
  {
    return new File(datadir + "/" + parseId(name));
  }
  
  /** 
   * Parses a dotted notation id into a file path.  johnson2343.13223 becomes
   * johnson2343/13223.  Revision numbers are left on the end so
   * johnson2343.13223.2 becomes johnson2343/13223.2
   */
  private String parseId(String id) 
  {
    String path = new String();
    path = id.substring(0, id.indexOf("."));
    path += "/" + id.substring(id.indexOf(separator) + 1, id.length());
    return path;
  }
  
  public static void main(String[] args)
  {
    String filename = args[0];
    String filename2 = args[1];
    try
    {
      ClientFramework cf = new ClientFramework(new ConfigXML("./lib/config.xml"));
      FileSystemDataStore fsds = new FileSystemDataStore(cf);
      File newfile = fsds.openFile(filename);
      fsds.saveFile(filename2, new FileReader(newfile));
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    System.out.println("done");
  }
}
