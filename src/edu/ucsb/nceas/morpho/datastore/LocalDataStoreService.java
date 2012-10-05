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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * implements and the DataStoreInterface for accessing files on the local
 * file system.
 */
public class LocalDataStoreService extends DataStoreService
                                 implements DataStoreServiceInterface
{
	
	  public static final String TEMP = "temporary";
	  private static final String TEMPIDNAME = "lastTempId";
  /**
   * create a new FileSystemDataStore for a Morpho
   */
  public LocalDataStoreService(Morpho morpho)
  {
    super(morpho);
  }
  
  /**
   * Get an AbstractDataPackage for the given identifier
   * @param identifier
   * @return
   * @throws FileNotFoundException
   */
  @Override
  public AbstractDataPackage read(String identifier) throws Exception {
		
	  File file = openFile(identifier);
	  Reader in = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
	  return DataPackageFactory.getDataPackage(in); 
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
    path = getDataDir() + "/" + path;
    File file = new File(path);
    if(!file.exists())
    {
      throw new FileNotFoundException("file " + path + " does not exist");
    }
    
    return file;
  }
  
  /**
   * opens a file with the given name from incomplete dir.  the name should be in the form
   * scope.accnum where the scope is unique to this machine.  The file will
   * be opened from the &lt;datadir&gt;/&lt;scope&gt;/ directory 
   * where the filename is the accnum.
   * Example: 
   *    name=johnson2343.13223
   *    datadir=data
   *    complete path=/usr/local/morpho/profiles/johnson/incomplete/johnson2343/13223
   * Any characters after the first separator are assumed to be part of the 
   * accession number.  Hence the id johnson2343.13223.5 would produce 
   * the file johnson2343/13223.5
   * @param name
   * @return
   * @throws FileNotFoundException
   */
  public File openIncompleteFile(String name) throws FileNotFoundException
  {
	   String path = parseId(name);
	    path = getIncompleteDir() + "/" + path;
	    File file = new File(path);
	    if(!file.exists())
	    {
	      throw new FileNotFoundException("file " + path + " does not exist");
	    }	    
	    return file;
  }
  
  
  public File saveFile(String name, Reader file)
  {
    return saveFile(name, file, getDataDir());
  }
  
  public File openTempFile(String name) throws FileNotFoundException
  {
    Log.debug(21, "opening "+name+" from temp dir - temp: " + getTempDir());
    String path = parseId(name);
    File file = new File(getTempDir() + "/" + path);
    if(!file.exists())
    {
      throw new FileNotFoundException("file " + getTempDir() + "/" + name + " does not exist");
    }
    
    return file;
  }

  public File saveDataFile(String name, InputStream file)
  {
    return saveDataFile(name, file, getDataDir());
  }
  
  public File saveTempDataFile(String name, InputStream file)
  {
    return saveDataFile(name, file, getTempDir());
  }
  
  /**
   * Save an input stream into a file in incomplete dir with the given name.
   * @param name
   * @param file
   * @return
   */
		  
  public File saveIncompleteDataFile(String name, InputStream file)
  {
    return saveDataFile(name, file, getIncompleteDir());
  }
  
  /**
   * Save a reader into a file in incomplete dir with a given name
   * @param name  name of the file
   * @param file  source of the file
   * @return
   */
  public File saveIncompleteFile(String name, Reader file)
  {
    return saveFile(name, file, getIncompleteDir());
  }
 
  
  /**
   * Check if the given docid exists in local file system.
   * @param docid 
   * @return exists or not
   */
  public String status(String docid)
  {
	  String status = DataStoreServiceInterface.NONEXIST;
	  String path = parseId(docid);
      File savefile = new File(getDataDir() + "/" + path); //the path to the file
      if(savefile.exists())
      {
        status = DataStoreServiceInterface.CONFLICT;
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
  private File saveFile(String name, Reader file, String rootDir)
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
      bwriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(savefile), Charset.forName("UTF-8")));
      
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
    return saveFile(name, file, getDataDir());
  }
  
  
  public File newDataFile(String name, InputStream is) {
    return saveDataFile(name, is, getDataDir());
  }
  
  /**
   * deletes a file from the local file system. returns true if the file is
   * successfully deleted, false otherwise.
   * @param name the name of the file to delete
   */
   public boolean deleteFile(String name)
   {
     String path = parseId(name);
     String filePath = getDataDir() + "/" +path;
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
    public boolean deleteInCompleteFile(String name) {
		String path = parseId(name);
		String filePath = getIncompleteDir() + "/" + path;
		File delfile = new File(filePath); // the path to the file

		boolean success = false;
		try {
			success = delfile.delete();
		} catch (Exception e) {
			//System.out.println("got an exception in deleting the local file");
			e.printStackTrace();
		}
		Log.debug(30, "the success value for deleting incomplete file " + name + " is " + success);
		return success;
	}
    
	/**
	 * Delete the auto-saved files for given abstract data package.
	 * @param adp
	 */
	public void deleteAutoSavedFile(AbstractDataPackage adp) {
		if (adp != null) {
			// delete the incomplete file
			String autoSavedID = adp.getAutoSavedD();
			if (autoSavedID != null) {
				deleteInCompleteFile(autoSavedID);
				adp.setAutoSavedID(null);
				// delete the data file too
				deleteDataFilesInIncompleteFolder(adp);
			}
		}
	}
    
	/**
	 * Deletes all associated data files in incomplete dir
	 */
	public void deleteDataFilesInIncompleteFolder(AbstractDataPackage adp) {
		if (adp.getEntityArray() != null) {
			for (int i = 0; i < adp.getEntityArray().length; i++) {
				String URLinfo = adp.getDistributionUrl(i, 0, 0);
				String protocol = AbstractDataPackage.getUrlProtocol(URLinfo);
				if (protocol != null && protocol.equals(AbstractDataPackage.ECOGRID)) {
					String docid = AbstractDataPackage.getUrlInfo(URLinfo);
					Log.debug(30, "handle data file  with index " + i + "" + docid);
					Morpho.thisStaticInstance.getLocalDataStoreService().deleteInCompleteFile(docid);
				}
			}
		}
	}
  
	/**
	 * Delete the given ADP from the local store
	 * @param adp
	 * @throws FileNotFoundException
	 */
	@Override
	public boolean delete(AbstractDataPackage adp) throws FileNotFoundException {
		
		String identifier = adp.getAccessionNumber();
		
		// TODO: delete other parts of the ADP
		return deleteFile(identifier);
		
		
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
        LocalDataStoreService fsds = new LocalDataStoreService(morpho);
        File newfile = fsds.openFile(filename);
        fsds.saveFile(filename2, new InputStreamReader(new FileInputStream(newfile), Charset.forName("UTF-8")));
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
        LocalDataStoreService fsds = new LocalDataStoreService(morpho);
        File newfile = new File(filename);
        fsds.saveFile(filename2, new InputStreamReader(new FileInputStream(newfile), Charset.forName("UTF-8")));
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
  *  in memory) This version uses an InputStream rather than a Reader to
  *  avoid problems with binary file corruption
  */
  private File saveDataFile(String name, InputStream file, String rootDir)
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
  
  
  /**
   * Gets a metadata file from all local source. It will looks file in data dir, then in temporary dir, 
   * finally the incomplete dir
   * @param doicd
   * @return
   */
  public File getMetadataFileFromAllLocalSources(String docid) throws FileNotFoundException
  {
    return getDataFileFromAllLocalSources(docid);
  }
  
  /**
	 * returns an id for the given location
	 * for the current scope
	 * 
	 */
	public synchronized String generateIdentifier() {
		int lastid = -1;
		String separator = Morpho.thisStaticInstance.getProfile().get("separator", 0);
		String scope = Morpho.thisStaticInstance.getProfile().get("scope", 0);
		// Get last id f
		lastid = getLastDocid(scope);
		if (lastid > 0) {
			// in order to get next id, this number should be increase 1
			lastid++;
		}

		// scope.docid
		String identifier = scope + separator + lastid + separator + 1;

		// set to the next in local for he next time we call this
		lastid++;
		if (!Morpho.thisStaticInstance.getProfile().set("lastId", 0, String.valueOf(lastid))) {
			Log.debug(1, "Error incrementing the locally stored docid");
			identifier = null;
		} else {
			Morpho.thisStaticInstance.getProfile().save();
			Log.debug(30, "the next id is " + identifier + ".1");
			identifier = identifier + ".1";
		}
		Log.debug(30, "generated  local identifier: " + identifier);

		return identifier;
	}
  
  /**
	 * Gets the max local id for given scope in current the profile. The local
	 * file's names look like 100.1, 102.1... under scope dir. In this case, 102
	 * will be returned.
	 */
	private int getLastDocid(String scope) {
		int docid = 0;
		int maxDocid = 0;
		String currentProfile = morpho.getProfile().get("profilename", 0);
		ConfigXML config = Morpho.getConfiguration();
		String profileDir = ConfigXML.getConfigDirectory() + File.separator
				+ config.get("profile_directory", 0) + File.separator
				+ currentProfile;
		String datadir = profileDir + File.separator
				+ morpho.getProfile().get("datadir", 0) + File.separator
				+ scope;
		datadir = datadir.trim();
		Log.debug(30, "the data dir is ===== " + datadir);
		File directoryFile = new File(datadir);
		File[] files = directoryFile.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File currentfile = files[i];
				if (currentfile != null && currentfile.isFile()) {
					String fileName = currentfile.getName();
					Log.debug(50, "the file name in dir is " + fileName);
					if (fileName != null) {
						fileName = fileName.substring(0, fileName.indexOf("."));
						Log.debug(50, "the file name after removing revision in dir is " + fileName);
						try {
							docid = new Integer(fileName).intValue();
							if (docid > maxDocid) {
								maxDocid = docid;
							}
						} catch (NumberFormatException nfe) {
							Log.debug(30, "Not loading file with invalid name");
						}
					}
				}
			}
		}
		Log.debug(30, "The max docid in local file system for scope " + scope + " is " + maxDocid);

		int lastid = -1;
		String lastidS = morpho.getProfile().get("lastId", 0);
		try {
			lastid = Integer.parseInt(lastidS);
		} catch (Exception e) {
			Log.debug(30, "couldn't get lastid from profile");
		}
		Log.debug(30, "the last id from profile " + lastid);

		// choose the larger of the two
		maxDocid = Math.max(maxDocid, lastid);

		return maxDocid;
	}
	
	  /**
	   * Gets the next available temp id from profile file.
	   * @return the next available id
	   */
	  public synchronized String getNextTempID()
	  {
	    long startID = 1;
	    long lastid = -1;
	    //Gets last id from profile
	    String lastidS = Morpho.thisStaticInstance.getProfile().get(TEMPIDNAME, 0);
	    String separator = Morpho.thisStaticInstance.getProfile().get("separator", 0);
	    try
	    {
	        lastid = (new Long(lastidS)).longValue();
	    }
	    catch(Exception e)
	    {
	      Log.debug(30, "couldn't get lastid for temp from profile");
	      lastid = startID;
	    }
	    String identifier = TEMP + separator + lastid;
	    lastid++;
	    String s = "" + lastid;
	    if (!Morpho.thisStaticInstance.getProfile().set(TEMPIDNAME, 0, s))
	    {
	      
	      boolean success = Morpho.thisStaticInstance.getProfile().insert(TEMPIDNAME, s);
	      if (success) {
	    	  Morpho.thisStaticInstance.getProfile().save();
	    	  return identifier + ".1"; 
	      }
	      else
	      {
	        Log.debug(1, "Error incrementing the accession number id");
	        return null;
	      }
	    }
	    else
	    {
	    	Morpho.thisStaticInstance.getProfile().save();
	      Log.debug(30, "the next id is "+identifier+".1");
	      return identifier + ".1"; 
	    }
	  }
  
  /**
   * Gets metadata file from both local and metacata source
   * @param docid
   * @return
   */
  public File getMetadataFileFromAllSources(String docid) throws FileNotFoundException
  {
    File file = null;  
    if(docid != null && !docid.equals(""))
    {
      try
      {
        //try local resrouce
        file = getMetadataFileFromAllLocalSources(docid);
      }
      catch(Exception e)
      {
        try
        {
          file = Morpho.thisStaticInstance.getMetacatDataStoreService().openFile(docid);
        }
        catch(Exception ee)
        {
          throw new FileNotFoundException("Couldn't find docid "+docid+" in metacat");
        }
        
      }
    }
    if(file == null)
    {
      throw new FileNotFoundException("Couldn't find docid "+docid+" in morpho file system");
    }
    return file;
  }
  
  /**
   * Gets a data file from all local source. It will looks file in data dir, then in temporary dir, 
   * finally the incomplete dir
   * @param doicd
   * @return
   */
  public File getDataFileFromAllLocalSources(String docid) throws FileNotFoundException
  {
     File file = null;  
     if(docid != null && !docid.equals(""))
     {
       try
       {
         //try data file dir
         file = openFile(docid);
       }
       catch(Exception e)
       {
         try
         {
           //try temp file dir
           file = openTempFile(docid);
           //Log.debug(5, "from temp");
         }
         catch(Exception ee)
         {
           try
           {
             file = openIncompleteFile(docid);
           }
           catch(Exception eee)
           {
             throw new FileNotFoundException(eee.getMessage());
           }
         }
         
       }
     }
     if(file == null)
     {
       throw new FileNotFoundException("Couldn't find docid "+docid+" in morpho file system");
     }
     return file;
  }
  
  /**
   * Gets data file from both local and metacata source
   * @param docid
   * @return
   */
  public File getDataFileFromAllSources(String docid) throws FileNotFoundException
  {
    File file = null;  
    if(docid != null && !docid.equals(""))
    {
      try
      {
        //try local resrouce
        file = getDataFileFromAllLocalSources(docid);
      }
      catch(Exception e)
      {
        try
        {
          file = Morpho.thisStaticInstance.getMetacatDataStoreService().openDataFile(docid);
        }
        catch(Exception ee)
        {
          throw new FileNotFoundException("Couldn't find docid "+docid+" in metacat");
        }
        
      }
    }
    if(file == null)
    {
      throw new FileNotFoundException("Couldn't find docid "+docid+" in morpho file system");
    }
    return file;
  }
  
  /**
	 * Gets next revisons from loca. This method will look at the profile/scope dir and figure
	 * it out the maximum revision. This number will be  increase 1 to get the next revision number.
	 * If local file system doesn't have this docid, 1 will be returned
	 */
	public int getNextRevisionNumber(String identifier)
	{
		int version = AbstractDataPackage.ORIGINAL_REVISION;
		String dataDir = null;
		if (identifier == null)
		{
			return version;
		}
		//Get Data dir
		// intialize filesystem datastore if it is null
		dataDir = getDataDir();
		
		String targetDocid = getDocIdPart(identifier);
		Log.debug(30, "the data dir is "+dataDir);
		if (dataDir != null && targetDocid != null)
		{
			//Gets scope name
			String scope = null;
			scope = getIdScope(identifier);
			Log.debug(30, "the scope from id is "+scope);
			File scopeFiles = new File(dataDir+File.separator+scope);
			if (scopeFiles.isDirectory())
			{
				File[] list = scopeFiles.listFiles();
				//Finds docid and revision part. The file name in above list will look 
				//like 56.1. We will go through all files and find the maximum revision of
				//same docid
				if (list != null)
				{
					for (int i=0; i<list.length; i++)
					{
						File file = list[i];
						if (file != null)
						{
							String name = file.getName();
							if (name != null)
							{
								int index = name.lastIndexOf(".");
								if (index != -1)
								{
								   String docid = name.substring(0,index);
								   if (docid != null && docid.equals(targetDocid))
								   {
									   String versionStr = name.substring(index+1);
									   if (versionStr != null)
									   {
										   try
										   {
											   int newVersion = (new Integer(versionStr)).intValue();
											   if (newVersion > version)
											   {
												   version = newVersion;
											   }
										   }
										   catch(Exception e)
										   {
											   Log.debug(30, "couldn't find the version part in file name "+name);
										   }
									   }
								   }
								}
							}
							
						}
					}
				}
				
			}
		}
		//if we found a maximum revsion, we should increase 1 to get the next revsion
		if (version != AbstractDataPackage.ORIGINAL_REVISION)
		{
			version++;
		}
		Log.debug(30, "The next version for docid " + identifier + " in local file system is "+version);
		return version;
	}
	  
	  /*
	   *  Gets the scope of given id. For example, it will return jones if the docid is jones.1.1.
	   *  It will return the substring from 0 to the second last .
	   *  null will be returned if no scope found.
	   * @return the scope of this id
	   */
	  private static String getIdScope(String id)
	  {
		  String scope = null;
		  if (id != null)
		  {
			  int index =id.lastIndexOf(".");
			  if (index != -1)
			  {
			      scope = id.substring(0, index);
			      if (scope != null)
			      {
			    	  index = scope.lastIndexOf(".");
			    	  if (index != -1)
			    	  {
			    		  scope = scope.substring(0,index);
			    	  }
			    	  else
			    	  {
			    		  scope = null;
			    	  }
			      }
			  }
		  }
		  return scope;
	  }
	
	/*
	 * Get docid part of an identifier. For example, it will return 30 if package id is jones.30.1
	 * It will return the substring from the second last dot to the last dot.
	 * null will return if no docid part found
	 */
	private static String getDocIdPart(String identifier)
	{
	  String docid = null;
	  //String identifier = null;
	  /*if (initialId != null)
	  {
	      identifier = initialId;
		  
	  }
	  else
	  {
		  identifier = getPackageId();
	  }*/
	  Log.debug(30, "The identifier which need be got docid part is "+identifier);
	  if (identifier != null)
	  {
		  int index =identifier.lastIndexOf(".");
		  if (index != -1)
		  {
		      docid = identifier.substring(0, index);
		      if (docid != null)
		      {
		    	  index = docid.lastIndexOf(".");
		    	  if (index != -1 )
		    	  {
		    		  docid = docid.substring(index+1);
		    		  
		    	  }
		    	  else
		    	  {
		    		  docid = null;
		    	  }
		      }
		  }
	  }
	  Log.debug(30, "the docid part is "+docid);
	  return docid;
	}
	
	/**
	 * Serialize data into morpho locally when user imports an external EML file
	 * to morpho. This method will be called in ImportEMLFileCommand. This
	 * method only serialize local file (on distrubition url) to morpho. It
	 * wouldn't serialize any http, ftp and ecogrid url into morpho
	 */
	public void serializeDataInImportExternalEMLFile(AbstractDataPackage adp) {
		if (adp.getEntityArray() == null) {
			Log.debug(30,"Entity array is null, no need to serialize data in AbstractDataPackage.serializeDataInImportExternalEMLFile()");
			return; // there is no data!
		}
		for (int i = 0; i < adp.getEntityArray().length; i++) {
			String URLinfo = adp.getDistributionUrl(i, 0, 0);
			String protocol = AbstractDataPackage.getUrlProtocol(URLinfo);
			if (!AbstractDataPackage.isProtocolInList(protocol)) {
				if (protocol != null && protocol.equalsIgnoreCase(AbstractDataPackage.FILE)) {
					// this is a file protocol. we may try to remove the file
					// protocol and to see if it is a local file
					URLinfo = AbstractDataPackage.removeFileProtocol(URLinfo);
				}
				// This is not online url. It may be a local file.
				File localFile = null;
				try {
					localFile = new File(URLinfo);
				} catch (Exception e) {
					Log.debug(30, "The online url " + URLinfo
							+ " couldn't be found in file stystem since "
							+ e.getMessage());
					return;
				}
				if (localFile != null && localFile.isFile()) {
					// now we copy the file into morpho
					String identifier = DataStoreServiceController.getInstance().generateIdentifier(DataPackageInterface.LOCAL);
					try {
						InputStream dfis = new FileInputStream(localFile);
						Morpho.thisStaticInstance.getLocalDataStoreService().saveDataFile(identifier, dfis);
						// now we can modify the online url in metadata.
						String url = AbstractDataPackage.ECOGRID + "://knb/" + identifier;
						adp.setDistributionUrl(i, 0, 0, url);

					} catch (Exception e) {
						Log.debug(30, "couldn't serialize local file "
										+ localFile.getAbsolutePath()
										+ " into morpho "
										+ " in AbstractDataPacakge.serializeDataInImportExternalEMLFile");
					}
				}
			}
		}
	}
}
