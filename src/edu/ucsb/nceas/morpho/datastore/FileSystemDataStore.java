/**
 *  '$RCSfile: DataStoreInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-12-19 23:58:56 $'
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
package edu.ucsb.nceas.morpho.datastore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierFileMap;
import edu.ucsb.nceas.morpho.exception.IdentifierNotFoundException;


/**
 * A class represents a data store. The Data store is specified as a file path string.
 * It can provide and save data.
 * @author tao
 *
 */
public class FileSystemDataStore {
  private static final String MORPHO = "Morpho-";
  private IdentifierFileMap idFileMap = null;
  private File storeDirectory = null;
  
  /**
   * For managing multiple data stores on a per-profile basis
   */
  private static Map<String, FileSystemDataStore> dataStores = new HashMap<String, FileSystemDataStore>();
  
  /**
   * Returns an instance of FSDS for the given directory
   * If one has already been instantiated, we use that one, otherewise
   * a new FSDS is created for the given directory
   * @param directory
   * @return
   * @throws Exception
   */
  public static FileSystemDataStore getInstance(String directory) throws Exception {
	  FileSystemDataStore fsds = dataStores.get(directory);
	  if (fsds == null) {
		  fsds = new FileSystemDataStore(directory);
		  dataStores.put(directory, fsds);
	  }
	  return fsds;
  }
  
  /**
   * Private constructor to encourage centralized instance management
   * @param directory - the file path of the data store.
   * @throws FileNotFoundException
   * @throws IOException
   * @throws UnsupportedCharsetException
   * @throws IllegalCharsetNameException
   * @throws NullPointerException
   * @throws IllegalArgumentException
   */
  private FileSystemDataStore(String directory) throws FileNotFoundException, 
                    IOException, UnsupportedCharsetException, IllegalCharsetNameException, 
                    NullPointerException, IllegalArgumentException {
   init(directory);
  }
  
  /**
   * Initialize the IdentifierFileMap object
   */
  private void init(String directory) throws FileNotFoundException, 
  IOException, UnsupportedCharsetException, IllegalCharsetNameException, 
  NullPointerException, IllegalArgumentException {
    if(directory == null || directory.trim().equals("")) {
      throw new NullPointerException("FileSystemDataStore.init - the specified file path for the store shouldn't be null or blank.");
    }
    this.storeDirectory = new File(directory);
    if(!this.storeDirectory.exists()) {
      boolean success = this.storeDirectory.mkdirs();
      if(!success) {
        throw new IOException("FileSystemDataStore.init - morpho can't create the directory at the location "+storeDirectory.getAbsolutePath());
      }
    }
    if(!this.storeDirectory.isDirectory()) {
      throw new IllegalArgumentException("FileSystemDataStore.init - the specified file path for the store should be a directory. "+
                                          "However, "+storeDirectory.getAbsolutePath()+" is not a directory.");
      
    }
    idFileMap = new IdentifierFileMap(this.storeDirectory);
  }
  
  /**
   * Retrieve the list of all managed identifiers for the store
   * This is a pass-through method for the backing id-file map
   * @return the list of identifiers for the managed files
   */
  public List<String> getIdentifiers() {
	  return idFileMap.getIdentifiers();
  }
  
  /**
   * Get the File object for a specified identifier.
   * NOTE: original design used InputStream as the return, but Morpho uses File objects extensively
   * (We can consider going back to InputStream if we have time)
   * @param identifier - the specified identifier.
   * @return the source of the data associated with the identifier
   * @throws IdentifierNotFoundException
   * @throws FileNotFoundException
   */
  public File get(String identifier) 
                  throws IdentifierNotFoundException, FileNotFoundException {
    //return new FileInputStream(idFileMap.getFile(identifier));
    return idFileMap.getFile(identifier);

  }
  
  /**
   * Store an InputStream object (data) into the store with the specified identifier.
   * @param identifier - the identifier of the data object
   * @param data - the source to get the data
   * @throws FileNotFoundException
   * @throws IOException
   */
  public synchronized void set(String identifier, InputStream data) throws FileNotFoundException, IOException, NullPointerException{
    if (identifier == null) {
      throw new NullPointerException("FileSystemDataStrore.set - the specified identifier shouldn't be null.");
    }
    
    File file = generateFile(identifier);
    
    // allow null data so we can get a handle on the file for writing to it later
    if (data != null ) {
        
	    BufferedInputStream buffData = null;
	    FileOutputStream out = null;
	    BufferedOutputStream buffOut = null;
	    try {
	    //String fileName = generateFileName(identifier);
	      buffData = new BufferedInputStream(data);
	      out = new FileOutputStream(file);
	      buffOut = new BufferedOutputStream(out);
	       
	      int d = -1;
	      byte[] b = new byte[1024*100];
	      while((d =buffData.read(b)) != -1) {
	        buffOut.write(b,0,d); //write out everything in the reader    
	      }
	      buffOut.flush();
	    } finally {
	      
	      if(buffData != null) {
	        buffData.close();
	      }
	      if(data != null ) {
	        data.close();
	      }
	      
	      if(buffOut != null) {
	        buffOut.close();
	      }
	      if(out != null) {
	        out.close();
	      }
	  }     
    }
    idFileMap.setMap(identifier, file);
 
  }
  
  /**
   * Generate a unique file name for the identifier. If there is already a file associated 
   * with the identifier, the file will be returned.
   */
  private File generateFile(String identifier) throws IOException {
    
    //String fileName = UUID.randomUUID().toString();
    //String fileName = (new Integer(identifier.hashCode())).toString();
    File file = null;
    try {
      file = idFileMap.getFile(identifier);
    } catch (Exception e) {
      file = File.createTempFile(MORPHO, "", storeDirectory);
    }
    return file;
  }
  
  /**
   * Get the file path of the data store.
   * @return the file path as a String object
   */
  public String getDirectory() {
    return this.storeDirectory.getAbsolutePath();
  }
  
 
  /**
   * Remove the file associated with the specified identifier; also remove the
   * identifier from the id-file mapping.
   * @param identifier - the identifier will be removed.
   * @return true if the removing succeeded; false else.
   * @throws IdentifierNotFoundException
   * @throws FileNotFoundException
   * @throws IOException
   */
  public synchronized boolean delete(String identifier) throws IdentifierNotFoundException, 
                                    FileNotFoundException, IOException {
    File file = get(identifier);
    boolean success = file.delete();
    if(success) {
      idFileMap.remove(identifier);
    }
    return success;
  }
  
}
