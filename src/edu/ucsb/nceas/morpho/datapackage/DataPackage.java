/**
 *  '$RCSfile: DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-10 03:59:12 $'
 * '$Revision: 1.7 $'
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

import java.util.Hashtable;
import java.util.Vector;

public class DataPackage 
{
  TripleCollection triples = new TripleCollection();
  /**
   * Create a new data package object with an id, location and associated
   * relations.
   * @param location: the location of the file (server or local)
   * @param identifier: the id of the data package.  usually the id of the
   * file that contains the triples.
   * @param relations: a vector of all relations in this package.
   */
  public DataPackage(String location, String identifier, Vector relations, 
                     ClientFramework framework)
  {
    framework.debug(9, "Creating new DataPackage Object");
    framework.debug(9, "id: " + identifier);
    framework.debug(9, "location: " + location);
    /*for(int i=0; i<relations.size(); i++)
    {
      
      System.out.print(((String[])relations.elementAt(i))[0] + " ");
      System.out.print(((String[])relations.elementAt(i))[1] + " ");
      System.out.print(((String[])relations.elementAt(i))[2] + " ");
      System.out.println();
    }*/
    getTriples(identifier, location);
  }
  
  /**
   * get the file with the given id and parse any triples out of it.
   */
  private void getTriples(String id, String location)
  {
    
  }
}
