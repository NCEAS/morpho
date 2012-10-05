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
package edu.ucsb.nceas.morpho.datastore.idmanagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Properties;

import edu.ucsb.nceas.morpho.exception.IdentifierNotFoundException;


/**
 * This class represents a mapping between an identifier and a local file path.
 * The local file path is the relative path string to the object store directory.
 * The mapping is stored in java property file. This is one-to-one relationship.
 * But morpho can store an object to different place, e.g., cache directory and data directory, 
 * each directory has to have its own mapping property file. The mapping file is placed in the
 * directory which stores objects and its name is .directory-name.poperties
 * @author tao
 *
 */
public class IdentifierFileMap {
  private static final String UTF8 = "UTF-8";
  private static final String PROPERTYFILESUFFIX = ".properties";
  private Properties mappingProperties = null;
  private File mappingPropertyFile = null;
  private File objectDir = null;

  /**
   * Constructor
   * @param objectDir the directory will store the objects.
   */
  public IdentifierFileMap(File objectDir) throws FileNotFoundException, 
                     IOException, UnsupportedCharsetException, IllegalCharsetNameException, 
                     NullPointerException, IllegalArgumentException{
    if(objectDir == null ) {
      throw new NullPointerException("IdentifierFileMap.IdentifierFileMap - the specified direcotry to store the objects is null!");
    }
    this.objectDir = objectDir;
    if(!this.objectDir.exists()) {
      this.objectDir.mkdirs();
    }
    if(!this.objectDir.isDirectory()) {
      throw new IllegalArgumentException("IdentifierFileMap.IdentifierFileMap - the specified object directory should be directory rathe than a file "
                                         +objectDir.getAbsolutePath());
      
    }
    this.mappingPropertyFile = new File(this.objectDir, File.separator+"."+this.objectDir.getName()+PROPERTYFILESUFFIX);
    
    if(!this.mappingPropertyFile.exists()) {
      this.mappingPropertyFile.createNewFile();
    }
    mappingProperties = new Properties();
    mappingProperties.load(new InputStreamReader(new FileInputStream(this.mappingPropertyFile), Charset.forName(UTF8)));
  }
  
  /**
   * Fetch the mapping file for the specified id
   * @param idenditifer the specified id
   * @return the OutputStream object which maps the identifier
   */
  public synchronized File getFile(String identifier) 
                throws IdentifierNotFoundException, FileNotFoundException {
    File file = null;
    String fileName = mappingProperties.getProperty(identifier);
    if(fileName == null) {
      throw new IdentifierNotFoundException("IdentifierFileMap.getFile - the identifier "+identifier+" couldn't be found "+
               "in the morpho storage. This maybe is caused by a corrupted or deleted property file " +
               "in /your-home/.morpho directory.");
    } else {
      file = new File(objectDir,File.separator+fileName);
      if(!file.exists()) {
        throw new FileNotFoundException("IdentifierFileMap.getFile - the file "+fileName+" associated with identifier "+identifier+
                                        "doesn't exist.");
      }
      
    }
    return file;
    
  }
  
  /**
   * Set the map between an identifier and a specified file
   * @param identifier the identifier associated with the file
   * @param file the file associated the identifier
   */
  public synchronized void setMap(String identifier, File file) throws FileNotFoundException, 
  IOException, UnsupportedCharsetException, IllegalCharsetNameException, NullPointerException,IllegalArgumentException{
    if( identifier != null && file != null ) {
      String absoluteFilePath = file.getAbsolutePath();
      String absoluteObjectDirPath = objectDir.getAbsolutePath();
      if(!absoluteFilePath.startsWith(absoluteObjectDirPath)) {
        throw new IllegalArgumentException("IdentifierFileMap.setMap - the file "+absoluteFilePath+
            " is not in the datastore (directory) "+absoluteObjectDirPath);
      }
      // we can't use file.getName directly since the file can be in a subdirectory
      int index = absoluteFilePath.indexOf(absoluteObjectDirPath);
      String fileName = absoluteFilePath.substring(absoluteObjectDirPath.length());
      if(fileName.startsWith(File.separator)) {
        if(fileName.length() <= 1) {
          throw new IllegalArgumentException("IdentifierFileMap.setMap - the file "+absoluteFilePath+
              " is not in the datastore (directory) "+absoluteObjectDirPath);
        }
        fileName = fileName.substring(1);
      }
      mappingProperties.setProperty(identifier, fileName);
      mappingProperties.store(new OutputStreamWriter(new FileOutputStream(mappingPropertyFile), Charset.forName(UTF8)), "");
    } else if (identifier == null) {
      throw new NullPointerException("IdentifierFileMap.setMap - can't map the identifier having the null value with a file name");
    } else if (file == null) {
      throw new NullPointerException("IdentifierFileMap.setMap - can't map the file having the null value with an identifier "+identifier);
    }
    
  }
  
  
  /**
   * Remove the map for the specified identifier
   * @param identifier the identifier which will be removed
   */
  public synchronized void remove(String identifier) throws FileNotFoundException, 
                    IOException, UnsupportedCharsetException, IllegalCharsetNameException {
    mappingProperties.remove(identifier);
    mappingProperties.store(new OutputStreamWriter(new FileOutputStream(mappingPropertyFile), Charset.forName(UTF8)), "");
  }


}
