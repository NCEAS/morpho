/**
 *  '$RCSfile: MetacatDataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-10 15:36:02 $'
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

import java.io.*;
import java.util.*;

/**
 * implements and the DataStoreInterface for accessing files on the Metacat
 */
public class MetacatDataStore implements DataStoreInterface
{
  private ClientFramework framework;
  
  public MetacatDataStore(ClientFramework cf)
  {
    this.framework = cf;
  }
  
  public File openFile(String name)
  {
    return new File(name);
  }
  
  public void saveFile(String name, File file)
  {
    
  }
  
  public File newFile(String name)
  {
    return new File(name);
  }
}
