/**
 *  '$RCSfile: MetacatDataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-03-28 23:39:59 $'
 * '$Revision: 1.33 $'
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

import javax.swing.*;
import java.awt.*;

import edu.ucsb.nceas.morpho.datapackage.*;

/**
 * implements and the DataStoreInterface for accessing files on the Metacat
 */
public class MetacatDataStore extends DataStore implements DataStoreInterface
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
    FileOutputStream fos;
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
      props.put("action", "read");
      props.put("docid", name);
      props.put("qformat", "xml");
      
      try
      {
        localdir.mkdirs(); //create any directories
      }
      catch(Exception ee)
      {
        ee.printStackTrace();
      }
      
      try
      {
//        writer = new FileWriter(localfile);
        fos = new FileOutputStream(localfile);
//        BufferedWriter bwriter = new BufferedWriter(writer);
        BufferedOutputStream bfos = new BufferedOutputStream(fos);
        InputStream metacatInput = framework.getMetacatInputStream(props);
//        InputStreamReader metacatInputReader = new InputStreamReader(metacatInput);
//        BufferedReader bmetacatInputReader = new BufferedReader(metacatInputReader);
        BufferedInputStream bmetacatInputStream = new BufferedInputStream(metacatInput);
        int x = 1;
/*        while(!bmetacatInputReader.ready())
        { //this is a stall to wait until the input stream is ready to read
          //there is probably a better way to do this.
          x++;
          if(x == 200000)
          { //200000 cycle time out.  this is so the whole program won't lock
            //up if the stream for some reason never becomes ready.
            break;
          }
        }
*/        
        int c = bmetacatInputStream.read();
        while(c != -1)
        {
          bfos.write(c);
          c = bmetacatInputStream.read();
        }
        bfos.flush();
        bfos.close();
        
        // just look for error in first 1000 bytes - DFH
        int cnt = 0;
        reader = new FileReader(localfile);
        BufferedReader breader = new BufferedReader(reader);
        c = breader.read();
        while((c != -1)&&(cnt<1000))
        {
          cnt++;  
          response.append((char)c);
          c = breader.read();
        }
        String responseStr = response.toString();
        //ClientFramework.debug(11, "==========================responseStr: " + 
        //                      responseStr/*.substring(22,29)*/);
        if(responseStr.indexOf("<error>") != -1)
        {//metacat reported some error
          bfos.close();
          breader.close();
          bmetacatInputStream.close();
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
        
        bfos.close();
        breader.close();
        bmetacatInputStream.close();
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
  public File saveFile(String name, Reader file, DataPackage dp) 
              throws MetacatUploadException
  {
    return saveFile(name, file, dp, "update", true);
  }
  
  public File saveFile(String name, Reader file, 
                       DataPackage dp, boolean checkforaccessfile) 
              throws MetacatUploadException
  {
    return saveFile(name, file, dp, "update", checkforaccessfile);
  }
  
  /**
   * Save an xml metadata file to metacat.  This method is for xml metadata 
   * documents only do not use this method to upload binary data files.
   * @param name: the docid
   * @param file: the file to save
   * @param publicAccess: true if the file can be read by unauthenssh ticated
   * users, false otherwise.
   * @param action: the action (update or insert) to perform
   */
  private File saveFile(String name, Reader file, 
                        DataPackage dp, String action, boolean checkforaccessfile) 
                       throws MetacatUploadException
  { //-attempt to write file to metacat
    //-if successfull, write file to cache, return pointer to that file
    //-if not successfull, throw exception, display metacat error.
    String access = "no";
    StringBuffer fileText = new StringBuffer();
    StringBuffer messageBuf = new StringBuffer();
    String accessFileId = null;

    BufferedReader bfile = new BufferedReader(file);
    try
    {
      /*
      int c = file.read();
      while(c != -1)
      {
        fileText.append((char)c);
        c = file.read();
      }*/
      //System.out.println(fileText.toString());
      
      
      //save a temp file so that the id can be put in the file.
      StringWriter sw = new StringWriter();
      File tempfile = new File(tempdir + "/metacat.noid");
      FileWriter fw = new FileWriter(tempfile);
      BufferedWriter bfw = new BufferedWriter(fw);
      int c = bfile.read();
      while(c != -1)
      {
        bfw.write(c); //write out everything in the reader
        sw.write(c);
        c = bfile.read();
      }
      bfw.flush();
      bfw.close();
      String filetext = insertIdInFile(tempfile, name); //put the id in
      
      if(filetext == null)
      {
        filetext = sw.toString();
      }
      
      Properties prop = new Properties();
      prop.put("action", action);
      prop.put("public", access);  //This is the old way of controlling access
      prop.put("doctext", filetext);
      prop.put("docid", name);
      ClientFramework.debug(11, "sending docid: " + name + " to metacat");
      ClientFramework.debug(11, "action: " + action);
      ClientFramework.debug(11, "public access: " + access);
      //ClientFramework.debug(11, "file: " + fileText.toString());
      
      InputStream metacatInput = null;
      metacatInput = framework.getMetacatInputStream(prop, true);
      InputStreamReader metacatInputReader = new InputStreamReader(metacatInput);
      BufferedReader bmetacatInputReader = new BufferedReader(metacatInputReader);
      
      int d = bmetacatInputReader.read();
      while(d != -1)
      {
        messageBuf.append((char)d);
        d = bmetacatInputReader.read();
      }
      
      String message = messageBuf.toString();
      ClientFramework.debug(11, "message from server: " + message);
      
      if(message.indexOf("<error>") != -1)
      {//there was an error
        bmetacatInputReader.close();
        metacatInput.close();
        throw new MetacatUploadException(message);
      }
      else if(message.indexOf("<success>") != -1)
      {//the operation worked
       //write the file to the cache and return the file object
        String docid = parseIdFromMessage(message);
        try
        {
          bmetacatInputReader.close();
          metacatInput.close();
          return openFile(docid);
        }
        catch(Exception ee)
        {
          bmetacatInputReader.close();
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
   */
  public File newFile(String name, Reader file) throws MetacatUploadException
  {
    return saveFile(name, file, null, "insert", true);
  }
  
  /**
   * Create and save a new file to metacat using the "insert" action.
   * @param name: the id of the new file
   * @param file: the stream to the file to write to metacat
   * @param publicAccess: flag for unauthenticated read access to the new file
   * true if unauthenticated users should have read access, false otherwise
   */
  public File newFile(String name, Reader file, DataPackage dp)
         throws MetacatUploadException
  {
    return saveFile(name, file, dp, "insert", true);
  }
  
  public File newFile(String name, Reader file, DataPackage dp, 
                      boolean checkforaccessfile)
         throws MetacatUploadException
  {
    return saveFile(name, file, dp, "insert", checkforaccessfile);
  }
  
  /**
   * method to create a new data file on metacat.  This method uploads the
   * given file with the given id.  It does nothing to control access or
   * link the file into packages -- those items are handled by the metadata
   * documents that are created on metacat.
   *
   * @param id the identifier to use for this file (e.g., knb.1.1).  It should be
   *           revision '1' because data files cannot be updated on metacat
   * @param file the file to upload to metacat
   */
  public void newDataFile(String id, File file) throws MetacatUploadException
  {
    try {
      InputStream metacatInput = null;;
      metacatInput = framework.sendDataFile(id, file);

      InputStreamReader returnStream = 
                        new InputStreamReader(metacatInput);
      BufferedReader breturnStream = new BufferedReader(returnStream);                  
      StringWriter sw = new StringWriter();
      int len;
      char[] characters = new char[512];
      while ((len = breturnStream.read(characters, 0, 512)) != -1) {
        sw.write(characters, 0, len);
      }
      breturnStream.close();
      String response = sw.toString();
      sw.close();
  
      if (response.indexOf("<error>") != -1) {
        throw new MetacatUploadException(response);
      } else {
        ClientFramework.debug(20, response);
      }
    } catch (Exception e) {
      throw new MetacatUploadException(e.getMessage());
    }
  }
  
  /**
   * deletes a file from metacat. returns true if the file was deleted 
   * succesfully, false otherwise.
   * @param name the name of the file to delete
   */
  public boolean deleteFile(String name)
  {
    StringBuffer messageBuf = new StringBuffer();
    Properties prop = new Properties();
    prop.put("action", "delete");
    prop.put("docid", name);
    ClientFramework.debug(11, "deleting docid: " + name + " from metacat");
    
    InputStream metacatInput = null;
    metacatInput = framework.getMetacatInputStream(prop, true);
    InputStreamReader metacatInputReader = new InputStreamReader(metacatInput);
    BufferedReader bmetacatInputReader = new BufferedReader(metacatInputReader);
    try
    {
      int d = bmetacatInputReader.read();
      while(d != -1)
      {
        messageBuf.append((char)d);
        d = bmetacatInputReader.read();
      }
    }
    catch(IOException ioe)
    {
      ClientFramework.debug(0, "Error deleting file from metacat: " + 
                            ioe.getMessage());
      return false;
    }
    
    String message = messageBuf.toString();
    ClientFramework.debug(11, "message from server: " + message);
    
    if(message.indexOf("<error>") != -1)
    { //there was an error
      try
      {
        bmetacatInputReader.close();
        metacatInput.close();
      }
      catch(Exception e)
      {}
      return false;
    }
    else if(message.indexOf("<success>") != -1)
    { //the operation worked
      try
      {
        bmetacatInputReader.close();
        metacatInput.close();
      }
      catch(Exception e)
      {}
      return true;
    }
    else
    {//something weird happened.
      return false;
    } 
  }
  
  /**
   * Test method
   */
  public static void main(String[] args)
  {
    String username = args[0];
    String password = args[1];
    try
    {
      ClientFramework.debug(20, "Initializing mds test...");
      ConfigXML config = new ConfigXML("./lib/config.xml");
      ClientFramework cf = new ClientFramework(config);
      String profileDir = config.get("profile_directory", 0);
      String profileName = profileDir + File.separator + username + 
                           File.separator + username + ".xml";
      ConfigXML profile = new ConfigXML(profileName);
      cf.setProfile(profile);
      cf.setPassword(password);
      cf.logIn();
      MetacatDataStore mds = new MetacatDataStore(cf);
    
      // Test metadata (xml) upload
      ClientFramework.debug(20, "Testing metadata upload...");
      String id = args[2];
      File f = new File(args[3]);
      FileReader fr = new FileReader(f);
      //File metacatfile = mds.newFile(id, fr, true);
      //File metacatfile = mds.saveFile(id, fr, true);
      //ClientFramework.debug(20, "XML file uploaded!");

      // Test data file upload too
      ClientFramework.debug(20, "Testing data upload...");
      id = args[4];
      f = new File(args[5]);
      mds.newDataFile(id, f);
      ClientFramework.debug(20, "Data file uploaded!");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }
 
  /* This method is designed to handle the case where there are several
   * xml metadata documents that need to be submitted to the metacat server.
   * The idea is to keep track of the success or failure of each submission
   * and retract any changes that have been successfully maide if a later
   * submission fails. The entire transaction should thus leave the server
   * version unchanged if any part of the transaction fails.
   * NamesVec should be a vector of strings with docnamed(ids)
   * readersVec should be a vector of Reader streams corresponding to the
   * ids.
   * 
   */
  
   public String saveFilesTransaction(Vector namesVec, Vector readersVec, DataPackage dp) {
        String temp = "";
        String response = "OK";
        for (int i=0;i<namesVec.size();i++) {
            String name = (String)namesVec.elementAt(i);
            Reader reader = (Reader)readersVec.elementAt(i);
            try{
                saveFile(name, reader, dp);   
            }
            catch(Exception e) {
                // oops, error; save name for return and undo previous inserts
                response = name;
                for (int j=0;j<i-1;j++) {
                    String name1 = (String)namesVec.elementAt(i);
                    boolean deleted = deleteFile(name1);
                    if (!deleted) {  // could not remove
                        temp = temp + "Could not delete" + name;    
                    }
                }
            }
            
        }
        if (temp.length()>0) response = temp;
        return response;
   }
}
