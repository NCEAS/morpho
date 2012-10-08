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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.UUID;

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
  private String directory = null;
  private IdentifierFileMap idFileMap = null;
  private File storeDirectory = null;
  
  
  /**
   * Constructor.
   * @param directory - the file path of the data store.
   * @throws FileNotFoundException
   * @throws IOException
   * @throws UnsupportedCharsetException
   * @throws IllegalCharsetNameException
   * @throws NullPointerException
   * @throws IllegalArgumentException
   */
  public FileSystemDataStore(String directory) throws FileNotFoundException, 
                    IOException, UnsupportedCharsetException, IllegalCharsetNameException, 
                    NullPointerException, IllegalArgumentException {
   init(directory);
  }
  
  /*
   * Initialize the IdentifierFileMap object
   */
  private void init(String directory) throws FileNotFoundException, 
  IOException, UnsupportedCharsetException, IllegalCharsetNameException, 
  NullPointerException, IllegalArgumentException {
    if(directory == null || directory.trim().equals("")) {
      throw new NullPointerException("FileSystemDataStore.init - the specified file path for the store shouldn't be null or blank.");
    }
    this.directory = directory;
    this.storeDirectory = new File(this.directory);
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
   * Get the OutputStream object for a specified identifier.
   * @param identifier - the specified identifier.
   * @return the source of the data associated with the identifier
   * @throws IdentifierNotFoundException
   * @throws FileNotFoundException
   */
  public InputStream get(String identifier) 
                  throws IdentifierNotFoundException, FileNotFoundException {
    return new FileInputStream(idFileMap.getFile(identifier));
  }
  
  
  /**
   * Store an InputStream object (data) into the store with the specified identifier.
   * @param identifier - the identifier of the data object
   * @param data - the source to get the data
   * @throws FileNotFoundException
   * @throws IOException
   */
  public void set(String identifier, InputStream data) throws FileNotFoundException, IOException, NullPointerException{
    if(identifier == null ) {
      throw new NullPointerException("FileSystemDataStrore.set - the specified identifier shouldn't be null.");
    }
    if(data == null ) {
      throw new NullPointerException("FileSystemDataStrore.set - the InputStream object of the data shouldn't be null.");
    }
    
    BufferedInputStream buffData = null;
    FileOutputStream out = null;
    BufferedOutputStream buffOut = null;
    try {
    //String fileName = generateFileName(identifier);
      buffData = new BufferedInputStream(data);
      File file = generateFile(identifier);
      out = new FileOutputStream(file);
      buffOut = new BufferedOutputStream(out);
       
      int d = -1;
      while((d =buffData.read()) != -1) {
        buffOut.write(d); //write out everything in the reader    
      }
      buffOut.flush();
      idFileMap.setMap(identifier, file);
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
  
  /*
   * Generate a unique file name for the identifier.
   */
  private File generateFile(String identifier) throws IOException {
    
    //String fileName = UUID.randomUUID().toString();
    //String fileName = (new Integer(identifier.hashCode())).toString();
    File file = File.createTempFile(MORPHO, "", storeDirectory);
    return file;
  }
  
  /**
   * Get the file path of the data store.
   * @return the file path as a String object
   */
  public String getDirectory() {
    return this.directory;
  }
  
  /**
   * Change the file path of the data store
   * @param directory - the file path of the data store
   * @throws FileNotFoundException
   * @throws IOException
   * @throws UnsupportedCharsetException
   * @throws IllegalCharsetNameException
   * @throws NullPointerException
   * @throws IllegalArgumentException
   */
  public void setDirectory(String directory) throws FileNotFoundException, 
                     IOException, UnsupportedCharsetException, IllegalCharsetNameException, 
                     NullPointerException, IllegalArgumentException {
    init(directory);
  }
  
}
