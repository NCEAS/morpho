/**
 *  '$RCSfile: DataPackageInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-10-02 20:28:27 $'
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

import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.Morpho;
import javax.swing.Action;
import java.awt.Component;
import java.util.Hashtable;
import java.util.Vector;

/**
 * All component plugins that handle access to data packages should implement
 * this interface and register themselves as a service provider for the
 * interface withthe framework.
 */
public interface DataPackageInterface
{
  
  /**
   * used to signify that this package is located on a metacat server
   */
  public static final String METACAT  = "metacat";
  /**
   * used to signify that this package is located locally
   */
  public static final String LOCAL    = "local";
  /**
   * used to signify that this package is stored on metacat and locally.
   */
  public static final String BOTH     = "localmetacat";

  /** 
   * This method is called to open a data package that resides either
   * locally (location=local) or on a Metacat server (location=metacat).
   *
   * @param location indicates the location from which to open the data
   * @param identifier the unique identifier to use to open the data
   * @param coordinator the coordinator for butterfly flapping
   */
  public void openDataPackage(String location, String identifier, 
                    Vector relations, ButterflyFlapCoordinator coordinator);
  /**
   * This method is called to upload a datapackage that is currently stored
   * locally to metacat and return a new id or orignal id.
   * @param docid the id of the package to upload
   * @param updateIds boolean to tell whether to automatically update ids
   * when a conflict is found.
   */
  public String upload(String docid, boolean updatedIds) 
              throws MetacatUploadException;
  
  /**
   * This method is called to download a datapackage from metacat to the local
   * disk.
   * @param docid the docid of the package to download
   */
  public void download(String docid);
  
  /**
   * This method is called to delete a datapackage from metacat or the local
   * disk.
   * @param docid the id of the package to delete
   * @param location the location to delete it from.  we do not necessarilly want
   * to delete the package from metacat and the local disk.  the user could just
   * want to delete it in one of those places.
   */
  public void delete(String docid, String location);
  
  /**
   * This method exports an entire data package to a given location.
   * @param id the docid of the package to export
   * @param path the location to export it to
   * @param location the current location of the package: METACAT, LOCAL or BOTH
   */
  public void export(String docid, String path, String location);
  
   /**
   * This method exports an entire data package to a given location in a zip file.
   * @param id the docid of the package to export
   * @param path the location to export it to
   * @param location the current location of the package: METACAT, LOCAL or BOTH
   */
  public void exportToZip(String docid, String path, String location);
  
  /**
   * This method will create a dialog for open previouse version of a 
   * datapackage
   * @param title the title of the dialog, docid will be set as tile
   * @param numOfVersion the total number of versions in this docid
   * @param morpho the morpho file
   * @param local the package is local or not
   */
  public void createOpenPreviousVersionDialog(String title, int numOfVersion,
                                              Morpho morpho, boolean local);
                                              
  /**
   * returns the next local id from the config file
   * returns null if configXML was unable to increment the id number
   *
   * @param morpho the morpho file
   */
  public String getNextId(Morpho morpho);
  
  /**
   * Method to get docid from a given morpho frame
   *
   * @param morphoFrame  the morphoFrame which contains a datapackage
   */
  public String getDocIdFromMorphoFrame(MorphoFrame morphoFrame);
  
  /**
   * Method to determine a data package which in a morpho frame if is in local
   *
   * @param morphoFrame  the morpho frame containing the data package
   */
  public boolean isDataPackageInLocal(MorphoFrame morphoFrame);
  
  /**
   * Method to determine a data package which in a morpho frame if is in network
   *
   * @param morphoFrame  the morpho frame containing the data package
   */
  public boolean isDataPackageInNetwork(MorphoFrame morphoFrame);
  
}
