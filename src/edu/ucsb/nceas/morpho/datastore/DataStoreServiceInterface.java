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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import edu.ucsb.nceas.morpho.datapackage.MorphoDataPackage;

/**
 * creates an interface for getting files from any dataStore using the same
 * methods.
 */
public interface DataStoreServiceInterface
{
	
   // status of given id in metacat:
   static final String CONFLICT    = "conflict"; //docid exist, but revision is less than the one in metacat
   static final String UPDATE       = "update";//docid exist, but revision is greater than the on in metacat
   static final String NONEXIST    =  "nonexist";//docid not exist all no all.
   
  /**
   * open a file from a datastore with the id of name and return a File
   * object that represents it.  Throws FileNotFoundException if a file
   * with the id name does not exist in the datastore.  Throws IOException
   * if a there is a communications problem with the datastore.
   */
  public File openFile(String name) throws FileNotFoundException, 
                                           CacheAccessException;
  
  /**
   * save a file to the datastore with the id of name.  a file object 
   * representing the saved file is returned.  If the publicAccess boolean
   * is true, then an unauthenticated user may read the document from the 
   * data store.
   */
  public File saveFile(String name, InputStream inputStream)
		throws Exception;
  
  
  /**
   * Save a data file to the store
   * @param id
   * @param file
   * @param objectName
   * @throws Exception
   */
  public void newDataFile(String id, File file, String objectName) 
	throws Exception;
  
  /**
   * create a new file with an id of name in the datastore and return a File
   * object that represents it.
   */
  public File newFile(String name, InputStream inputStream)
		throws Exception;
  
  /**
   * Save a new data file to the store
   * @param id
   * @param file
   * @param objectName
   * @throws Exception
   */
//  public File newDataFile(String id, File file, String objectName) 
//		throws Exception;
  
  /**
   * Deletes the file with given name.  returns true if the file was 
   * successfully deleted, false otherwise
   */
  public boolean deleteFile(String name) throws Exception;
  
  public boolean delete(MorphoDataPackage mdp) throws Exception;
  
  public MorphoDataPackage read(String identifier) throws Exception;
  
  public String generateIdentifier() throws Exception;
  
  public boolean exists(String identifer) throws Exception;
  
  public InputStream query(String query) throws Exception;

}
