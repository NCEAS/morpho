/**
 *  '$RCSfile: MetacatDataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-22 21:13:36 $'
 * '$Revision: 1.16 $'
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
  
  /**
   * Constructor to create this object in conjunction with a ceartain framework.
   */
  public MetacatDataStore(ClientFramework cf)
  {
    super(cf);
    framework = cf;
  }
  
  /**
   * Opens a file from Metacat and returns a File object that represents the
   * metacat file.  If the file does not exist in the local cache, or is
   * outdated in the local cache, this method adds the new file to the cache
   * for later access.
   * @param name: the docid of the metacat file in &lt;scope&gt;.&lt;number&gt;
   * or &lt;scope&gt;.&lt;number&gt;.&lt;revision&gt; form.
   */
  public File openFile(String name) throws FileNotFoundException, 
                                           CacheAccessException
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
      framework.debug(11, "MetacatDataStore: getting cached file");
      return localfile;
    }
    else
    { //if the file is not cached, get it from metacat and cache it.
      //-get file from metacat
      //-write file to cache directory
      //-reread file to check for errors
      //-throw exception if file is an error and delete file
      //-return the file pointer if the file is not an error
      
      framework.debug(11,"MetacatDataStore: getting file from Metacat");
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
        
        int c = metacatInputReader.read();
        while(c != -1)
        {
          writer.write(c);
          c = metacatInputReader.read();
        }
        writer.flush();
        writer.close();
        
        reader = new FileReader(localfile);
        c = reader.read();
        while(c != -1)
        {
          response.append((char)c);
          c = reader.read();
        }
        String responseStr = response.toString();
        //ClientFramework.debug(11, "==========================responseStr: " + 
        //                      responseStr/*.substring(22,29)*/);
        if(responseStr.indexOf("<error>") != -1)
        {//metacat reported some error
          writer.close();
          reader.close();
          metacatInputReader.close();
          metacatInput.close();
          if(!localfile.delete())
          {
            throw new CacheAccessException("A cached file could not be " + 
                                  "deleted.  Please check your access " +
                                  "permissions on the cache directory." +
                                  "Failing to delete cached files can " +
                                  "result in erroneous operation of morpho." +
                                  "You may want to manually clear your cache " +
                                  "now.");
          }
          
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
  
  /**
   * Save an xml metadata file (which already exists) to metacat using the 
   * "update" action.  
   * This method is for xml metadata documents only do not use this method to 
   * upload binary data files.
   * @param name: the docid
   * @param file: the file to save
   * @param publicAccess: true if the file can be read by unauthenticated
   * users, false otherwise.
   */
  public File saveFile(String name, Reader file, boolean publicAccess) 
              throws MetacatUploadException
  {
    return saveFile(name, file, publicAccess, "update");
  }
  
  /**
   * Save an xml metadata file to metacat.  This method is for xml metadata 
   * documents only do not use this method to upload binary data files.
   * @param name: the docid
   * @param file: the file to save
   * @param publicAccess: true if the file can be read by unauthenticated
   * users, false otherwise.
   * @param action: the action (update or insert) to perform
   */
  private File saveFile(String name, Reader file, boolean publicAccess, 
                       String action) throws MetacatUploadException
  {//-attempt to write file to metacat
   //-if successfull, write file to cache, return pointer to that file
   //-if not successfull, throw exception, display metacat error.
    String access = "no";
    StringBuffer fileText = new StringBuffer();
    StringBuffer messageBuf = new StringBuffer();
    
    if(publicAccess)
    {
      access = "yes";
    }
    
    try
    {
      int c = file.read();
      while(c != -1)
      {
        fileText.append((char)c);
        c = file.read();
      }
      
      //System.out.println(fileText.toString());
      
      Properties prop = new Properties();
      prop.put("action", action);
      prop.put("public", access);
      prop.put("doctext", fileText.toString());
      prop.put("docid", name);
      ClientFramework.debug(11, "sending docid: " + name + " to metacat");
      ClientFramework.debug(11, "action: " + action);
      ClientFramework.debug(11, "public access: " + access);
      //ClientFramework.debug(11, "file: " + fileText.toString());
      
      InputStream metacatInput = null;
      metacatInput = framework.getMetacatInputStream(prop, true);
      InputStreamReader metacatInputReader = new InputStreamReader(metacatInput);
      
      int d = metacatInputReader.read();
      while(d != -1)
      {
        messageBuf.append((char)d);
        d = metacatInputReader.read();
      }
      
      String message = messageBuf.toString();
      ClientFramework.debug(11, "message from server: " + message);
      
      if(message.indexOf("<error>") != -1)
      {//there was an error
        metacatInputReader.close();
        metacatInput.close();
        throw new MetacatUploadException(message);
      }
      else if(message.indexOf("<success>") != -1)
      {//the operation worked
       //write the file to the cache and return the file object
        String docid = parseIdFromMessage(message);
        try
        {
          metacatInputReader.close();
          metacatInput.close();
          return openFile(docid);
        }
        catch(Exception ee)
        {
          metacatInputReader.close();
          metacatInput.close();
          ee.printStackTrace();
          return null;
        }
      }
      else
      {//something weird happened.
        throw new Exception("unexpected error in edu.ucsb.nceas.morpho." +
                            "framework.MetacatDataStore.saveFile(): " + message);
      } 
    }
    catch(Exception e)
    {
      //metacatInputReader.close();
      //metacatInput.close();
      //ClientFramework.debug(4, "Error in MetacatDataStore.saveFile(): " + 
      //                    e.getMessage());
      //e.printStackTrace();
      throw new MetacatUploadException(e.getMessage());
    }
  }
  
  /**
   * Create and save a new file to metacat using the "insert" action.
   * @param name: the id of the new file
   * @param file: the stream to the file to write to metacat
   * @param publicAccess: flag for unauthenticated read access to the new file
   * true if unauthenticated users should have read access, false otherwise
   */
  public File newFile(String name, Reader file, boolean publicAccess)
         throws MetacatUploadException
  {
    return saveFile(name, file, publicAccess, "insert");
  }
  
  public File newDataFile(String name, Reader file, boolean publicAccess)
                          throws MetacatUploadException
  {
    InputStream metacatInput;
    InputStreamReader portReader;
    StringBuffer messageBuf;
    String access = "no";
    if(publicAccess)
    {
      access = "yes";
    }
    
    Properties prop = new Properties();
    prop.put("action", "getdataport");
    prop.put("public", access);
    prop.put("docid", name);
    
    try
    {
      metacatInput = framework.getMetacatInputStream(prop, true);
      portReader = new InputStreamReader(metacatInput);
      messageBuf = new StringBuffer();
      int c = portReader.read();
      while(c != -1)
      {
        messageBuf.append((char)c);
        c = portReader.read();
      }
      portReader.close();
      metacatInput.close();
    }
    catch(Exception e)
    {
      throw new MetacatUploadException(e.getMessage());
    }
    
    String message = messageBuf.toString();
    ClientFramework.debug(11, "message from server: " + message);
    
    if(message.indexOf("<error>") != -1)
    {//there was an error
      throw new MetacatUploadException(message);
    }
    else if(message.indexOf("<port>") != -1)
    {//the operation worked
      
      String cookie = framework.getSessionCookie();
      int i1 = cookie.indexOf("JSESSIONID=");
      int i2 = cookie.indexOf(":", i1);
      //get just the session id
      cookie = cookie.substring(i1, i2);
      //////////////////////////////////////////////////////////////////////////
      //////////////////////////////////////////////////////////////////////////
      ///this isn't done!!!!!!!!!!!!!///////////////////////////////////////////
      //////////////////////////////////////////////////////////////////////////
    }
    
    return null;
  }
  
  /**
   * Test method
   */
  public static void main(String[] args)
  {
    String id = args[0];
    try
    {
      File f = new File(args[1]);
      FileReader fr = new FileReader(f);
      
      ClientFramework cf = new ClientFramework(new ConfigXML("./lib/config.xml"));
      cf.setUserName("berkley");
      cf.setPassword("");
      cf.logIn();
      MetacatDataStore mds = new MetacatDataStore(cf);
    
      //File metacatfile = mds.newFile(id, fr, true);
      File metacatfile = mds.saveFile(id, fr, true);
      ClientFramework.debug(20, "file done");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
}
