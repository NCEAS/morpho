/**
 *  '$RCSfile: FileSystemDataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2009-02-06 21:26:34 $'
 * '$Revision: 1.13 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Log;

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
   * create a new FileSystemDataStore for a Morpho
   */
  public FileSystemDataStore(Morpho morpho)
  {
    super(morpho);
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
    String path = parseId(name);
    path = datadir + "/" + path;
    File file = new File(path);
    if(!file.exists())
    {
      throw new FileNotFoundException("file " + path + " does not exist");
    }
    
    return file;
  }
  
  
  public File saveFile(String name, Reader file)
  {
    return saveFile(name, file, datadir);
  }
  
  public File saveTempFile(String name, Reader file)
  {
    return saveFile(name, file, tempdir);
  }
  
  public File openTempFile(String name) throws FileNotFoundException
  {
    Log.debug(21, "opening "+name+" from temp dir - temp: "+tempdir);
    File file = new File(tempdir+"/"+name);
    if(!file.exists())
    {
      throw new FileNotFoundException("file " + tempdir + "/" + name + " does not exist");
    }
    
    return file;
  }

  public File saveDataFile(String name, Reader file)
  {
    return saveDataFile(name, file, datadir);
  }
  
  public File saveTempDataFile(String name, Reader file)
  {
    return saveDataFile(name, file, tempdir);
  }

  public File saveDataFile(String name, InputStream file)
  {
    return saveDataFile(name, file, datadir);
  }
  
  public File saveTempDataFile(String name, InputStream file)
  {
    return saveDataFile(name, file, tempdir);
  }
  
  /**
   * Save an input stream into a file in incomplete dir with the given name.
   * @param name
   * @param file
   * @return
   */
		  
  public File saveIncompleteDataFile(String name, InputStream file)
  {
    return saveDataFile(name, file, incompletedir);
  }
  
  /**
   * Save a reader into a file in incomplete dir with a given name
   * @param name  name of the file
   * @param file  source of the file
   * @return
   */
  public File saveIncompleteDataFile(String name, Reader file)
  {
    return saveDataFile(name, file, incompletedir);
  }
 
  
  /**
   * Check if the given docid exists in local file system.
   * @param docid 
   * @return exists or not
   */
  public String status(String docid)
  {
	  String status = DataStoreInterface.NONEXIST;
	  String path = parseId(docid);
      String dirs = path.substring(0, path.lastIndexOf("/"));
      File savefile = new File(datadir + "/" + path); //the path to the file
      if(savefile.exists())
      {
        status = DataStoreInterface.CONFLICT;
      }
      Log.debug(30, "The docid "+docid +" local status is "+status);
	  return status;
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
  public File saveFile(String name, Reader file, String rootDir)
  {
	BufferedWriter bwriter = null; 
	BufferedReader bsr = null;
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
        }
        catch(Exception ee)
        {
          ee.printStackTrace();
        }
      }
      bsr = new BufferedReader(file);
      if (charset != null) {
    	  bwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savefile), charset));
      }
      else {
    	  bwriter = new BufferedWriter(new FileWriter(savefile));
      }
      int d = bsr.read();
      while(d != -1)
      {
        bwriter.write(d); //write out everything in the reader
        d = bsr.read();
      }
      bsr.close();
      bwriter.flush();
      bwriter.close();
      return savefile;
    }
    catch(Exception e)
    {
      if (bwriter != null)
      {
    	  try
    	  {
    		if (bsr != null) bsr.close();
    	    if (bwriter != null) bwriter.close();
    	  }
    	  catch(Exception ie)
    	  {
    		  ie.printStackTrace();
    	  }
      }
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
  public File newFile(String name, Reader file)
  {
    return saveFile(name, file, datadir);
  }
  
  
  public File newDataFile(String name, InputStream is) {
    return saveDataFile(name, is, datadir);
  }
  
  /**
   * deletes a file from the local file system. returns true if the file is
   * successfully deleted, false otherwise.
   * @param name the name of the file to delete
   */
   public boolean deleteFile(String name)
   {
     String path = parseId(name);
     String filePath = datadir + "/" +path;
     //System.out.println("the deleted file path will be !"+filePath+"!");
     File delfile = new File(filePath); //the path to the file
     //System.out.println("the file exists "+delfile.exists());
     //System.out.println("the applicate read file "+delfile.canRead());
     //System.out.println("the application write file "+delfile.canWrite());
     
     //SecurityManager manager = new SecurityManager();
     boolean success = false;
     
     try
     {
    	 //manager.checkRead(filePath);
    	 //manager.checkWrite(filePath);
    	 //manager.checkDelete(filePath);
    	 //Thread.sleep(5000);
    	 //System.out.println("the canonical path is "+delfile.getCanonicalPath());
    	 //delfile.close();
    	 //this is not a good way to call system.gc there. but it works. otherwise
    	 // delete() wouldn't work on windows xp
    	 //System.gc();
    	 success = delfile.delete();
    	 
     }
     catch(Exception e)
     {
    	 //System.out.println("got an exception in deleting the local file");
    	 e.printStackTrace();
     }
     System.out.println("the success value is "+success);
     return success;
   }
   
   /**
    * deletes a file from incomplete dir in the local file system. returns true if the file is
    * successfully deleted, false otherwise.
    * @param name the name of the file to delete
    */
    public boolean deleteInCompleteFile(String name)
    {
      String path = parseId(name);
      String filePath = incompletedir + "/" +path;
      
      File delfile = new File(filePath); //the path to the file
    
      boolean success = false;
      
      try
      {
     	
     	 success = delfile.delete();
     	 
      }
      catch(Exception e)
      {
     	 //System.out.println("got an exception in deleting the local file");
     	 e.printStackTrace();
      }
      Log.debug(30, "the success value for deleting incomplete file "+name+" is "+success);
      return success;
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
        Morpho morpho = new Morpho(new ConfigXML("./lib/config.xml"));
        FileSystemDataStore fsds = new FileSystemDataStore(morpho);
        File newfile = fsds.openFile(filename);
        fsds.saveFile(filename2, new FileReader(newfile));
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
        Morpho morpho = new Morpho(new ConfigXML("./lib/config.xml"));
        FileSystemDataStore fsds = new FileSystemDataStore(morpho);
        File newfile = new File(filename);
        fsds.saveFile(filename2, new FileReader(newfile));
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
    Log.debug(20, "done");
  }
  
 /**
  *  A variant of saveFile designed for use with Data Files.
  *  This avoids the writing of files to Strings that is in saveFile
  *  and allows for very large data files. (i.e. no file is put entirely
  *  in memory)
  */
  public File saveDataFile(String name, Reader file, String rootDir)
  {
    BufferedWriter bwriter = null;
    BufferedReader bfile = null;
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
        }
        catch(Exception ee)
        {
          ee.printStackTrace();
        }
      }
      
      //save a temp file so that the id can be put in the file.
      bfile = new BufferedReader(file);
      bwriter = new BufferedWriter(new FileWriter(savefile));
      int d = bfile.read();
      while(d != -1)
      {
        bwriter.write(d); //write out everything in the reader
        d = bfile.read();
      }
      bfile.close();
      bwriter.flush();
      bwriter.close();
      return savefile;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
    finally {
      try{
        if (bfile!=null) bfile.close();
        if (bwriter!=null) bwriter.flush();
        if (bwriter!=null) bwriter.close();
      }
      catch (Exception t) {}
    }

  }

 /**
  *  A variant of saveFile designed for use with Data Files.
  *  This avoids the writing of files to Strings that is in saveFile
  *  and allows for very large data files. (i.e. no file is put entirely
  *  in memory) This version uses an InputStream rather than a Reader to
  *  avoid problems with binary file corruption
  */
  public File saveDataFile(String name, InputStream file, String rootDir)
  {
    BufferedInputStream bfile = null;
    BufferedOutputStream bos = null;
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
        }
        catch(Exception ee)
        {
          ee.printStackTrace();
        }
      }
      
      bfile = new BufferedInputStream(file);
      bos = new BufferedOutputStream(new FileOutputStream(savefile));
      int d = bfile.read();
      while(d != -1)
      {
        bos.write(d); //write out everything in the reader
        d = bfile.read();
      }
      bos.flush();
      bos.close();
      bfile.close();
      return savefile;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
    finally {
      try{
        if (bfile!=null) bfile.close();
        if (bos!=null) bos.close();
      }
      catch (Exception r) {}
    }
  }
  
}
