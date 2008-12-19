/**
 *  '$RCSfile: MetacatDataStore.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-12-19 23:58:56 $'
 * '$Revision: 1.19 $'
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

package edu.ucsb.nceas.morpho.datastore;

import java.io.*;
import java.util.*;

import javax.swing.*;
import java.awt.*;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * implements and the DataStoreInterface for accessing files on the Metacat
 */
public class MetacatDataStore extends DataStore implements DataStoreInterface
{
  private Morpho morpho;
  
  /**
   * Constructor to create this object in conjunction with a ceartain morpho.
   */
  public MetacatDataStore(Morpho morpho)
  {
    super(morpho);
    this.morpho = morpho;
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
    
    if((localfile.exists())&&(localfile.length()>0))
    { //if the file is cached locally, read it from the hard drive
      Log.debug(11, "MetacatDataStore: getting cached file");
      return localfile;
    }
    else
    { // if the filelength is zero, delete it
      if (localfile.length()==0) {
        localfile.delete();
      }
      
      //if the file is not cached, get it from metacat and cache it.
      //-get file from metacat
      //-write file to cache directory
      //-reread file to check for errors
      //-throw exception if file is an error and delete file
      //-return the file pointer if the file is not an error
      
      Log.debug(11,"MetacatDataStore: getting file from Metacat");
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
        fos = new FileOutputStream(localfile);
        BufferedOutputStream bfos = new BufferedOutputStream(fos);
        InputStream metacatInput = morpho.getMetacatInputStream(props);
        // set here because previous line call to getMetacatInputStream will set
        // to false
        Morpho.connectionBusy = true;

        BufferedInputStream bmetacatInputStream = new BufferedInputStream(metacatInput);
        int c = bmetacatInputStream.read();
        while(c != -1)
        {
          /* the following checks for values of 'c' >127 and <32 are driven by the
             fact that metacat can return xml documents with special characters in this 
             range which cause parsing problems. This code 'filters' the values into xml
             character references ('&#xxxx;'). This is only appropriate for XML streams.
             Binary data should be called using the 'openDataFile' method.
          */
          if (c>127) {
            bfos.write('&');
            bfos.write('#');
            int h = c/100;
            int t = (c-h*100)/10;
            int o = c-h*100-t*10;
            bfos.write(Character.forDigit(h,10));
            bfos.write(Character.forDigit(t,10));
            bfos.write(Character.forDigit(o,10));
            bfos.write(';');   
            Log.debug(40, "char > 127!");
          }
          else if (c<32) {
            if ((c==9)||(c==10)||(c==13)) {
              bfos.write(c);
            }
          }
          else {
            bfos.write(c);
          }
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
                                          "current Metacat system: ");
        }
        
        bfos.close();
        breader.close();
        bmetacatInputStream.close();
        metacatInput.close();
        Morpho.connectionBusy = false;
        return localfile;
      }
      catch (FileNotFoundException mde) {
        throw mde;
      }
      catch(Exception e)
      {
        e.printStackTrace();
        Morpho.connectionBusy = false;
        return null;
      }
    }
  }

  /**
   * Opens a file from Metacat and returns a File object that represents the
   * metacat file.  If the file does not exist in the local cache, or is
   * outdated in the local cache, this method adds the new file to the cache
   * for later access.
   *
   * differs from 'openFile' in that no filtering is done on special characters
   * This is needed for binary data files.
   *
   * @param name: the docid of the metacat file in &lt;scope&gt;.&lt;number&gt;
   * or &lt;scope&gt;.&lt;number&gt;.&lt;revision&gt; form.
   */
  public File openDataFile(String name) throws FileNotFoundException, 
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
    
    if((localfile.exists())&&(localfile.length()>0))
    { //if the file is cached locally, read it from the hard drive
      Log.debug(11, "MetacatDataStore: getting cached file");
      return localfile;
    }
    else
    { // if the filelength is zero, delete it
      if (localfile.length()==0) {
        localfile.delete();
      }
      
      //if the file is not cached, get it from metacat and cache it.
      //-get file from metacat
      //-write file to cache directory
      //-reread file to check for errors
      //-throw exception if file is an error and delete file
      //-return the file pointer if the file is not an error
      
      Log.debug(11,"MetacatDataStore: getting file from Metacat");
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
        fos = new FileOutputStream(localfile);
        BufferedOutputStream bfos = new BufferedOutputStream(fos);
        InputStream metacatInput = morpho.getMetacatInputStream(props);
        // set here because previous line call to getMetacatInputStream will set
        // to false
        Morpho.connectionBusy = true;

        BufferedInputStream bmetacatInputStream = new BufferedInputStream(metacatInput);
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
                                          "current Metacat system: ");
        }
        
        bfos.close();
        breader.close();
        bmetacatInputStream.close();
        metacatInput.close();
        Morpho.connectionBusy = false;
        return localfile;
      }
      catch (FileNotFoundException mde) {
        throw mde;
      }
      catch(Exception e)
      {
        e.printStackTrace();
        Morpho.connectionBusy = false;
        return null;
      }
    }
  }
  
  
  
  /**
   * Gets the id(with revision) status on Metacat: 
   * CONFLICT: docid exist, but revision is less than the one in metacat
   * UPDATE: docid exist, but revision is greater than the on in metacat
   * NONEXIST: docid not exist all no all.
   *
   * @param name: the docid of the metacat file in &lt;scope&gt;.&lt;number&gt;
   * or &lt;scope&gt;.&lt;number&gt;.&lt;revision&gt; form.
   */
  public String status(String name)
  {
	String status  = DataStoreInterface.NONEXIST;
    String path = parseId(name);
    String dirs = path.substring(0, path.lastIndexOf("/")); 
    File localfile = new File(cachedir + "/" + path); //the path to the file
    File localdir = new File(cachedir + "/" + dirs); //the dir part of the path
    
    if((localfile.exists())&&(localfile.length()>0))
    { //if the file is cached locally, read it from the hard drive
      Log.debug(30, "MetacatDataStore: cached file exists and docid is used "+name);
      return DataStoreInterface.CONFLICT;
    }
    else
    {    
    	 // if the filelength is zero, delete it
	      if (localfile.length()==0) 
	      {
	        localfile.delete();
	      }
         //    Gets metacat url from configuration
		if (Morpho.thisStaticInstance != null && Morpho.thisStaticInstance.getNetworkStatus())
		{
			String metacatURL = Morpho.thisStaticInstance.getMetacatURLString();
			String result = null;
		    Properties lastVersionProp = new Properties();
		    lastVersionProp.put("action", "getrevisionanddoctype");
		   
		    lastVersionProp.put("docid", name);
		    Morpho.connectionBusy = true;
		    try
		    {
			    result = Morpho.thisStaticInstance.getMetacatString(lastVersionProp);
			    Morpho.connectionBusy = false;
			    Log.debug(30, "the result is ============= "+result);
			    // Get version
			    if (result != null)
			    {
			    	int index = result.indexOf("<error>");
			    	if (index == -1)
			    	{
			    		// if have this id, but metacat version is less than the version in name,
			    		// we consider this id doesn't exist
			    		int index1 = result.indexOf(";");
			    		String versionStr = result.substring(0, index1);
			    		index1 = name.lastIndexOf(".");
			    		String versionStrFromName = name.substring(index1+1);
			    		Log.debug(30, "version from name is "+versionStrFromName +
		    					 " and version from metacat is "+versionStr);
			    		try
			    		{
			    			int  versionFromMetacat = (new Integer(versionStr)).intValue();
			    			int versionFromName = (new Integer(versionStrFromName)).intValue();
			    			Log.debug(30, "version from name is "+versionFromName +
			    					 " and version from metacat is "+versionFromMetacat);
			    			if (versionFromName > versionFromMetacat)
			    			{
			    				status = DataStoreInterface.UPDATE;
			    			}
			    			else
			    			{
			    				status = DataStoreInterface.CONFLICT;
			    			}
			    		}
			    		catch(Exception e)
			    		{
			    			Log.debug(20, "Couldn't transfer version string "+versionStr +" into integer");
			    		}
			    		
			    	}
			    	else
			    	{
			    		// if have error tag, this means docid doesn't exist
			    		status = DataStoreInterface.NONEXIST;
			    	}
			    }
		    }
		    catch(Exception e)
		    {
		    	Morpho.connectionBusy = false;	    
		   }
		}
    }
    Log.debug(30, "The docid "+name + " status in metacat is "+status);
	return status;
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
  public File saveFile(String name, Reader file) 
              throws MetacatUploadException
  {
    return saveFile(name, file, "update", true);
  }
  
 
  
  public File saveFile(String name, Reader file, 
                       boolean checkforaccessfile) 
              throws MetacatUploadException
  {
    return saveFile(name, file, "update", checkforaccessfile);
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
                        String action, boolean checkforaccessfile) 
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
      bfile.close();
      String filetext = sw.toString();
      
      Log.debug(30, "filelength is:"+filetext.length());
      if (filetext.length()==0) return null;
      
      Properties prop = new Properties();
      prop.put("action", action);
      prop.put("public", access);  //This is the old way of controlling access
      prop.put("doctext", filetext);
      prop.put("docid", name);
      Log.debug(11, "sending docid: " + name + " to metacat");
      Log.debug(11, "action: " + action);
      Log.debug(11, "public access: " + access);
      //Log.debug(11, "file: " + fileText.toString());
      
      InputStream metacatInput = null;
      metacatInput = morpho.getMetacatInputStream(prop, true);
      // set here because previous line call to getMetacatInputStream will set
      // to false
      Morpho.connectionBusy = true;
      
      InputStreamReader metacatInputReader = new InputStreamReader(metacatInput);
      BufferedReader bmetacatInputReader = new BufferedReader(metacatInputReader);
      
      int d = bmetacatInputReader.read();
      while(d != -1)
      {
        messageBuf.append((char)d);
        d = bmetacatInputReader.read();
      }
      
      String message = messageBuf.toString();
      Log.debug(11, "message from server: " + message);
      
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
          Morpho.connectionBusy = false;
          return openFile(docid);
        }
        catch(Exception ee)
        {
          bmetacatInputReader.close();
          metacatInput.close();
          ee.printStackTrace();
          Morpho.connectionBusy = false;
          return null;
        }
      }
      else
      {//something weird happened.
        Morpho.connectionBusy = false;
        throw new Exception("unexpected error in edu.ucsb.nceas.morpho." +
                            ".datastore.MetacatDataStore.saveFile(): " + message);
      } 
    }
    catch(Exception e)
    {
      Morpho.connectionBusy = false;
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
    return saveFile(name, file, "insert", true);
  }
  
  
  public File newFile(String name, Reader file, 
                      boolean checkforaccessfile)
         throws MetacatUploadException
  {
    return saveFile(name, file,"insert", checkforaccessfile);
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
   * @param objectName the object name associated with the file
   */
  public void newDataFile(String id, File file, String objectName) throws MetacatUploadException
  {
    try {
      if (file.length()>0) {
        System.out.println("id:"+id+"  filelength:"+file.length());
        InputStream metacatInput = null;;
        metacatInput = morpho.sendDataFile(id, file, objectName);

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
          Log.debug(20, response);
        }
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
    Log.debug(11, "deleting docid: " + name + " from metacat");
    
    InputStream metacatInput = null;
    metacatInput = morpho.getMetacatInputStream(prop, true);
    Morpho.connectionBusy = true;
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
      Log.debug(0, "Error deleting file from metacat: " + 
                            ioe.getMessage());
      Morpho.connectionBusy = false;
      return false;
    }
    
    String message = messageBuf.toString();
    Log.debug(11, "message from server: " + message);
    
    if(message.indexOf("<error>") != -1)
    { //there was an error
      try
      {
        bmetacatInputReader.close();
        metacatInput.close();
      }
      catch(Exception e)
      {}
      Morpho.connectionBusy = false;
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
      Morpho.connectionBusy = false;
      return true;
    }
    else
    {//something weird happened.
      Morpho.connectionBusy = false;
      return false;
    } 
  }


  /**
   * sets access to a document in metacat. 
   * @param docid id to set permission for
   * @param principal user for permission
   * @param permission the permission being set (read/write)
   * @param permType allow/deny
   * @param permOrder denyFirst/allowFirst
   * @return true if successful
   */
  public boolean setAccess(String docid, String principal, String permission, String permType, String permOrder)
  {
    StringBuffer messageBuf = new StringBuffer();
    Properties prop = new Properties();
    prop.put("action", "setaccess");
    prop.put("docid", docid);
    prop.put("principal", principal);
    prop.put("permission", permission);
    prop.put("permType", permType);
    prop.put("permOrder", permOrder);

    Log.debug(11, "setting access for docid: " + docid + " on metacat");
    
    InputStream metacatInput = null;
    metacatInput = morpho.getMetacatInputStream(prop, true);
    Morpho.connectionBusy = true;
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
      Log.debug(0, "Error setting access in metacat: " + 
                            ioe.getMessage());
      Morpho.connectionBusy = false;
      return false;
    }
    
    String message = messageBuf.toString();
    Log.debug(11, "message from server: " + message);
    
    if(message.indexOf("<error>") != -1)
    { //there was an error
      try
      {
        bmetacatInputReader.close();
        metacatInput.close();
      }
      catch(Exception e)
      {}
      Morpho.connectionBusy = false;
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
      Morpho.connectionBusy = false;
      return true;
    }
    else
    {//something weird happened.
      Morpho.connectionBusy = false;
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
      Log.debug(20, "Initializing mds test...");
      ConfigXML config = new ConfigXML("./lib/config.xml");
      Morpho morpho = new Morpho(config);
      String profileDir = config.get("profile_directory", 0);
      String profileName = profileDir + File.separator + username + 
                           File.separator + username + ".xml";
      ConfigXML profile = new ConfigXML(profileName);
      morpho.setProfile(profile);
      morpho.setPassword(password);
      morpho.logIn();
      MetacatDataStore mds = new MetacatDataStore(morpho);
    
      // Test metadata (xml) upload
      Log.debug(20, "Testing metadata upload...");
      String id = args[2];
      File f = new File(args[3]);
      FileReader fr = new FileReader(f);
      //File metacatfile = mds.newFile(id, fr, true);
      //File metacatfile = mds.saveFile(id, fr, true);
      //Log.debug(20, "XML file uploaded!");

      // Test data file upload too
      Log.debug(20, "Testing data upload...");
      id = args[4];
      f = new File(args[5]);
      mds.newDataFile(id, f, null);
      Log.debug(20, "Data file uploaded!");
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
  
   /*public String saveFilesTransaction(Vector namesVec, Vector readersVec) {
        String temp = "";
        String response = "OK";
        for (int i=0;i<namesVec.size();i++) {
            String name = (String)namesVec.elementAt(i);
            Reader reader = (Reader)readersVec.elementAt(i);
            try{
                saveFile(name, reader);   
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
   }*/
}
