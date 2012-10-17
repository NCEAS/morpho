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
package edu.ucsb.nceas.morpho.datastore.idmanagement.update;

import java.io.File;

/**
 * Represent a file directory will be updated. It has a flag to indicate if this
 * is a query directory 
 * @author tao
 *
 */
public class ObjectDirectory {
  private File directory = null;
  private boolean isQueryDirectory = false;
  
  /**
   * Get the file directory.
   * @return the directory.
   */
  public File getDirectory() {
    return directory;
  }
  
  /**
   * Set the directory.
   * @param directory - the directory will be set.
   */
  public void setDirectory(File directory) {
    this.directory = directory;
  }
  
  /**
   * This is a query directory or not.
   * @return true if it is; otherwise false.
   */
  public boolean isQueryDirectory() {
    return isQueryDirectory;
  }
  
  /**
   * Set the directory to be a query directory of not.
   * @param isQueryDirectory - true if this is a query directory.
   */
  public void setQueryDirectory(boolean isQueryDirectory) {
    this.isQueryDirectory = isQueryDirectory;
  }
  
}