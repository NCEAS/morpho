/**
 *  '$RCSfile: DataPackageInterface.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-09 20:29:10 $'
 * '$Revision: 1.2 $'
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
   * This method is called to open a data package that resides either
   * locally (location=local) or on a Metacat server (location=metacat).
   *
   * @param location indicates the location from which to open the data
   * @param identifier the unique identifier to use to open the data
   */
  public void openDataPackage(String location, String identifier, 
                              Vector relations);
}
