/**
 *  '$RCSfile: FileSystemDataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-14 17:03:47 $'
 * '$Revision: 1.14 $'
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
public class FileSystemDataStore extends DataStore
                                 implements DataStoreInterface
{
  /**
   * create a new FileSystemDataStore for a ClientFramework
   */
  public FileSystemDataStore(ClientFramework cf)
  {
    super(cf);
  }
  
  /**
   * opens a file with the given name.  the name should be in the form
   * scope.accnum where the scope is unique to this machine.  The file will
   * be opened from the &lt;datadir&gt;/&lt;scope&gt;/ directory 
   * where the filename is the accnum.
   * Example: 
   *    name=johnson2343.13223
   *    datadir=data
   *    complete path=/usr/local/morpho/profiles/johnson/data/johnson2343/13223
   * Any characters after the first separator are assumed to be part of the 
   * accession number.  Hence the id johnson2343.13223.5 would produce 
   * the file johnson2343/13223.5
   */
  public File openFile(String name) throws FileNotFoundException
  {
    //debug(11, "opening files from: " + datadir);
    String path = parseId(name);
    path = datadir + "/" + path;
    File file = new File(path);
    if(!file.exists())
    {
      throw new FileNotFoundException("file " + path + " does not exist");
    }
    
    return file;
  }
  
  public File saveFile(String name, Reader file, boolean publicAccess)
  {
    return saveFile(name, file, publicAccess, datadir);
  }
  
  public File saveTempFile(String name, Reader file)
  {
    return saveFile(name, file, false, tempdir);
  }
  
  /**
   * Saves a file with the given name.  if the file does not exist it is created
   * The file is saved according to the name provided.   The file will
   * be saved to the &lt;datadir&gt;/&lt;scope&gt;/ directory 
   * where the filename is the accnum.
   * Example: 
   *    name=johnson2343.13223
   *    datadir=data
   *    complete path=/usr/local/morpho/profiles/johnson/data/johnson2343/13223
   * Any characters after the first separator are assumed to be part of the 
   * accession number.  Hence the id johnson2343.13223.5 would produce 
   * the file johnson2343/13223.5
   */
  public File saveFile(String name, Reader file, boolean publicAccess, 
                       String rootDir)
  {
    try
    {
      String path = parseId(name);
      String dirs = path.substring(0, path.lastIndexOf("/"));
      File savefile = new File(rootDir + "/" + path); //the path to the file
      File savedir = new File(rootDir + "/" + dirs); //the dir part of the path
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
      int c = file.read();
      while(c != -1)
      {
        writer.write(c); //write out everything in the reader
        c = file.read();
      }
      writer.flush();
      writer.close();
      return savefile;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * returns a File object in the local repository.
   * @param name: the id of the file
   * @param file: the stream to the file
   * @param publicAccess: flag for unauthenticated read access to the file.
   * true if anauthenticated users can read the file, false otherwise.
   */
  public File newFile(String name, Reader file, boolean publicAccess)
  {
    return saveFile(name, file, publicAccess);
  }
  
  /**
   * Test method
   */
  public static void main(String[] args)
  {
    String filename = args[0];
    String filename2 = args[1];
    String action = args[2];
    if(action.equals("test"))
    {
      try
      {
        ClientFramework cf = new ClientFramework(new ConfigXML("./lib/config.xml"));
        FileSystemDataStore fsds = new FileSystemDataStore(cf);
        File newfile = fsds.openFile(filename);
        fsds.saveFile(filename2, new FileReader(newfile), true);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    else if(action.equals("save"))
    {
      try
      {
        ClientFramework cf = new ClientFramework(new ConfigXML("./lib/config.xml"));
        FileSystemDataStore fsds = new FileSystemDataStore(cf);
        File newfile = new File(filename);
        fsds.saveFile(filename2, new FileReader(newfile), true);
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    ClientFramework.debug(20, "done");
  }
}
