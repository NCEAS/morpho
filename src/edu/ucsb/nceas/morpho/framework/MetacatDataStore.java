/**
 *  '$RCSfile: MetacatDataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-11 21:51:05 $'
 * '$Revision: 1.4 $'
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
public class MetacatDataStore extends DataStore
                              implements DataStoreInterface
{
  private ClientFramework framework;
  
  public MetacatDataStore(ClientFramework cf)
  {
    super(cf);
    framework = cf;
  }
  
  public File openFile(String name) throws FileNotFoundException
  {
    String path = parseId(name);
    String dirs = path.substring(0, path.lastIndexOf("/"));
    StringBuffer response = new StringBuffer();
    FileWriter writer;
    FileReader reader;
    
    File localfile = new File(cachedir + "/" + path); //the path to the file
    File localdir = new File(cachedir + "/" + dirs); //the dir part of the path
    
    if(localfile.exists())
    { //if the file is cached locally, read it from the hard drive
      debug(9, "getting cached file");
      return localfile;
    }
    else
    { //if the file is not cached, get it from metacat and cache it.
      //-get file from metacat
      //-write file to cache directory
      //-reread file to check for errors
      //-throw exception if file is an error and delete file
      //-return the file pointer if the file is not an error
      
      debug(9, "getting file from Metacat");
      Properties props = new Properties();
      props.setProperty("action", "read");
      props.setProperty("docid", name);
      props.setProperty("qformat", "xml");
      
      try
      {
        localdir.mkdirs(); //create any directories
        localfile.createNewFile(); //create the file
      }
      catch(Exception ee)
      {
        ee.printStackTrace();
      }
      
      try
      {
        writer = new FileWriter(localfile);
        InputStream metacatInput = framework.getMetacatInputStream(props);
        InputStreamReader metacatInputReader = new InputStreamReader(metacatInput);
        
        while(!metacatInputReader.ready())
        { //this is a stall to wait until the input stream is ready to read
          //there is probably a better way to do this.
          int x = 1;
        }
            
        while(metacatInputReader.ready())
        {
          writer.write(metacatInputReader.read());
        }
        writer.flush();
        writer.close();
        
        reader = new FileReader(localfile);
        while(reader.ready())
        {
          response.append((char)reader.read());
        }
        String responseStr = response.toString();
        //System.out.println("responseStr: " + responseStr/*.substring(22,29)*/);
        if(responseStr.substring(22, 29).equals("<error>"))
        {
          writer.close();
          reader.close();
          metacatInputReader.close();
          metacatInput.close();
          if(localfile.delete())
            System.out.println("file deleted");
          else
            System.out.println("file not deleted");
          
          throw new FileNotFoundException(name + " does not exist on your " +
                                          "current Metacat system: " + 
                                          response.toString());
        }
        
        writer.close();
        reader.close();
        metacatInputReader.close();
        metacatInput.close();
        
        return localfile;
      }
      catch(Exception e)
      {
        e.printStackTrace();
        return null;
      }
      
    }
  }
  
  public File saveFile(String name, Reader file)
  {
    return new File(name);
  }
  
  public File newFile(String name)
  {
    return new File(name);
  }
  
  public static void main(String[] args)
  {
    String id = args[0];
    ClientFramework cf = new ClientFramework(new ConfigXML("./lib/config.xml"));
    MetacatDataStore mds = new MetacatDataStore(cf);
    try
    {
      File metacatfile = mds.openFile(id);
      System.out.println("file done");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}
