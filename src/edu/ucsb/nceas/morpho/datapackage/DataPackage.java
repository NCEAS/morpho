/**
 *  '$RCSfile: DataPackage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-17 23:30:28 $'
 * '$Revision: 1.10 $'
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

import java.util.*;
import java.io.*;

public class DataPackage 
{
  TripleCollection triples = new TripleCollection();
  
  /**
   * create a nonnull, yet contentless dataPackage object
   */
  public DataPackage()
  {
    
  }
  
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
    //-open file named identifier
    //-read the triples out of it, create a triplesCollection
    //-start caching the files referenced in the triplesCollection
    //-respond to any request from the user to open a specific file
    
    framework.debug(9, "Creating new DataPackage Object");
    framework.debug(9, "id: " + identifier);
    framework.debug(9, "location: " + location);
    
    if(location.equals("metacat"))
    {
      framework.debug(9, "opening metacat file");
      MetacatDataStore mds = new MetacatDataStore(framework);
      try
      {
        File tripleFile = mds.openFile(identifier);
        FileReader tripleFileReader = new FileReader(tripleFile);
        TripleCollection triples = new TripleCollection(tripleFileReader);
      }
      catch(FileNotFoundException fnfe)
      {
        
      }
      catch(CacheAccessException cae)
      {
        
      }
    }
    else if(location.equals("local"))
    {
      framework.debug(9, "opening local file");
      FileSystemDataStore fsds = new FileSystemDataStore(framework);
      try
      {
        File resourcefile = fsds.openFile(identifier);
        framework.debug(9, "file opened");
        FileReader reader = new FileReader(resourcefile);
        while(reader.ready())
        {
          System.out.print((char)reader.read());
        }
      }
      catch(FileNotFoundException fnfe)
      {
        fnfe.printStackTrace();
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  public static void main(String[] args)
  {
    String filename = args[0];
    String location = args[1];
    String action = args[2];
    System.out.println("location: " + location);
    System.out.println("id: " + filename);
    System.out.println("action: " + action);
    if(action.equals("read"))
    {
      ClientFramework cf = new ClientFramework(new ConfigXML("./lib/config.xml"));
      DataPackage dp = new DataPackage(location, filename, null, cf);
    }
    else if(action.equals("write"))
    {
      
    }
  }
}
