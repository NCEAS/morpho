/**
 *  '$RCSfile: DataPackageInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-09-17 17:39:43 $'
 * '$Revision: 1.23 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.util.Command;

import java.io.File;
import java.io.Reader;
import java.util.Vector;

import org.w3c.dom.Document;

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
   * used to specify incomplete location
   */
  public static final String INCOMPLETE = "incompleteDir";

  
  /**
	 * used to signify that this package is not stored.
	 */
	public static final String TEMPLOCATION = "";

  /**
   * Denotes an instance of the CreateNewDataPackageCommand object
   */
  public static final int NEW_DATAPACKAGE_COMMAND  = 10;

  /**
   * Denotes an instance of the ImportDataCommand object
   */
  public static final int NEW_DATA_TABLE_COMMAND   = 20;

  /**
   * This method is called to open a data package that resides either locally
   * (location=local) or on a Metacat server (location=metacat).
   *
   * @param adp indicates the location from which to open the data
   * @param coordinator the coordinator for butterfly flapping
   */
  public MorphoFrame openNewDataPackage( AbstractDataPackage adp,
                                  ButterflyFlapCoordinator coordinator);
  
  /**
   * Display a newly created abstract data package
   * @param adp  the data package will be display
   * @param coordinator the coordinator for butterfly flapping
   * @param visible if this frame visible
   * @return a MorphoFrame which displays the data package
   */
  public MorphoFrame openNewDataPackage(AbstractDataPackage adp, ButterflyFlapCoordinator coordinator, boolean visible);
  /**
   *  same as openNewDataPackage except the frame is notVisible
   */                                 
  public void openHiddenNewDataPackage( AbstractDataPackage adp,
                                  ButterflyFlapCoordinator coordinator);

  /**
   * This method is called to open a data package that resides either locally
   * (location=local) or on a Metacat server (location=metacat).
   *
   * @param location indicates the location from which to open the data
   * @param identifier the unique identifier to use to open the data
   * @param relations Vector
   * @param coordinator the coordinator for butterfly flapping
   * @param doctype String
   */
  public void openDataPackage(String location, String identifier,
                    Vector relations, ButterflyFlapCoordinator coordinator,
                    String doctype);
  
  /**
   * Opens an incomplete data package
   * @param identifier he unique identifier to use to open the data
   * @param coordinator the coordinator for butterfly flapping
   */
  public void openIncompleteDataPackage(String identifier, ButterflyFlapCoordinator coordinator);


  /**
   * This method is called to upload a datapackage that is currently stored
   * locally to metacat and return a new id or orignal id.
   *
   * @param docid the id of the package to upload
   * @throws MetacatUploadException
   * @return String
   */
  public String upload(String docid)
              throws MetacatUploadException;

  /**
   * This method is called to download a datapackage from metacat to the local
   * disk.
   * @param docid the docid of the package to download
   * @return the new docid (if the docid conflict happens, the new docid may different to the old one)
   */
  public String download(String docid);

  /**
   * This method is called to delete a datapackage from metacat or the local
   * disk.
   * @param docid the id of the package to delete
   * @param location the location to delete it from.  we do not necessarilly want
   * to delete the package from metacat and the local disk.  the user could just
   * want to delete it in one of those places.
   */
  public void delete(String docid, String location) throws Exception;


  /**
   * This method exports an entire data package to a given location.
   *
   * @param docid the docid of the package to export
   * @param path the location to export it to
   * @param location the current location of the package: METACAT, LOCAL or BOTH
   */
  public void export(String docid, String path, String location);


  /**
   * This method exports an entire data package to a given location in a zip
   * file.
   *
   * @param docid the docid of the package to export
   * @param path the location to export it to
   * @param location the current location of the package: METACAT, LOCAL or BOTH
   */
  public void exportToZip(String docid, String path, String location);


  /**
   * This method exports an entire data package to a given location to eml2
   *
   * @param docid the docid of the package to export
   * @param path the location to export it to
   * @param location the current location of the package: METACAT, LOCAL or BOTH
   */
  //public void exportToEml2(String docid, String path, String location);

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
   * returns the next local id from the config file returns null if configXML
   * was unable to increment the id number
   *
   * @param morpho the morpho file
   * @return String
   */
  public String getNextId(Morpho morpho);


  /**
   * Method to get docid from a given morpho frame
   *
   * @param morphoFrame the morphoFrame which contains a datapackage
   * @return String
   */
  public String getDocIdFromMorphoFrame(MorphoFrame morphoFrame);


  /**
   * Method to determine a data package which in a morpho frame if is in local
   *
   * @param morphoFrame the morpho frame containing the data package
   * @return boolean
   */
  public boolean isDataPackageInLocal(MorphoFrame morphoFrame);
  
  
  /**
   * Method to get a Document node as a representation of data package for
   * given id and location
   * @param docid the identifier of the package
   * @param location the location of the package
   * @return
   */
  public Document getDocumentNode(String docid, String location);


  /**
   * Method to determine a data package which in a morpho frame if is in network
   *
   * @param morphoFrame the morpho frame containing the data package
   * @return boolean
   */
  public boolean isDataPackageInNetwork(MorphoFrame morphoFrame);


  /**
   * return an instance of a Command object, identified by one of the integer
   * constants defined above
   *
   * @param commandIdentifier integer constant identifying the command
   *   Currently only one option:<ul> <li>NEW_DATAPACKAGE_COMMAND</li> </ul>
   * @throws ClassNotFoundException
   * @return Command
   */
  public Command getCommandObject(int commandIdentifier)
                                                throws ClassNotFoundException;
  
  /**
   * Save the incomplete xml document into local file system
   * @param docid if xml doesn't have id, this given id will be used as package id
   * @param xml the source of xml
   * @return id of the saved data package
   */
  public void saveIncompleteDocumentForLater(String docid, Reader xml) throws Exception;
  
  /**
   * Export a data package to files with Biological Data Profile format
   * @param outputFile the output file 
   * @param styleSheetLocation the style sheet location
   * @param docid the docid of the data package
   * @param documentLocation the document location 
   */
  public void exportToBDP(File outputFile, String styleSheetLocation, String docid, String documentLocation) throws Exception;

}
