/**
 *  '$RCSfile: DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-07 20:34:04 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;
import java.util.*;
import java.io.*;

/**
 * This class implements the PluginInterface in edu.ucsb.nceas.morpho.framework.
 * It implements a plugin for morpho to handle data packages
 */
public class DataPackage implements PluginInterface
{
  Hashtable packagecomponents = new Hashtable();
  //this hashtable has all of the references of ids to filenames (or urls in
  //the case of a file on metacat).  
  IdContainer accNum;
  
  public DataPackage()
  {
    
  }
  
  public void initialize(ClientFramework cf)
  {
    accNum = new IdContainer(cf);
    cf.debug(9, "Init DataPackage Plugin"); 
  }
  
}
