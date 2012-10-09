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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.swing.Timer;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AbstractDataPackage;
import edu.ucsb.nceas.morpho.datapackage.DataPackageFactory;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ConnectionFrame;
import edu.ucsb.nceas.morpho.framework.HttpMessage;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;

/**
 * implements and the DataStoreInterface for accessing files on the Metacat
 */
public class MetacatDataStoreService extends DataStoreService implements DataStoreServiceInterface
{
  private Morpho morpho;
  
  private String metacatURL = null;
  private boolean networkStatus = false;
  private boolean sslStatus = false;
  private URL metacatPingURL = null;
  private URLConnection urlConn = null;
  private boolean origNetworkStatus = false;
  
  private static final String AUTHENTICATEERROR = "peer not authenticated";
  /**
   * The polling interval, in milliSeconds, between attempts to verify that
   * MetaCat is available over the network
   */
  private final static int METACAT_PING_INTERVAL = 30000;

  /** flag set to indicate that connection to metacat is busy
   *  used by doPing to avoid thread problem
   */
  public boolean connectionBusy = false;
  
  private boolean connected = false;
  
  /**
   * Constructor to create this object in conjunction with a ceartain morpho.
   */
  public MetacatDataStoreService(Morpho morpho)
  {
    super(morpho);
    this.morpho = morpho;
    
    String metacatURL = Morpho.getConfiguration().get("metacat_url", 0);
    setMetacatURL(metacatURL);
    
 // NOTE: current test for SSL connection is to determine whether
    // metacat_url is set to be "https://..." in the config.xml file.
    // This check happens only ONCE on start-up, so if Morpho is ever
    // revised to allow users to change metacat urls whilst it is running,
    // we need to revise this to check more often.
    // 05/20/02- Currently, SSL is not used, so will always be false
    sslStatus = (metacatURL.indexOf("https://") == 0);
    

    //create URL object to poll for metacat connectivity
    try {
        metacatPingURL = new URL(metacatURL);
    } catch (MalformedURLException mfue) {
        Log.debug(5, "unable to read or resolve Metacat URL");
    }

    // detects whether metacat is available, and if so, sets
    // networkStatus = true
    // Boolean "true" tells doPing() method this is startup, so we don't get
    // "No such service registered." exception from getServiceProvider()
    boolean startup = true;
    startPing(startup);
    finishPing(startup);

    //start a Timer to check periodically whether metacat remains available
    //over the network...
    Timer timer = new Timer(METACAT_PING_INTERVAL, pingActionListener);
    timer.setRepeats(true);
    timer.start();
  }
  
  /**
   * Retrieve an AbstractDataPackage for the given identifier
   * @param identifier
   * @return
   * @throws FileNotFoundException
   * @throws CacheAccessException
   */
  @Override
  public AbstractDataPackage read(String identifier) throws FileNotFoundException, CacheAccessException {
		
	  File file = openFile(identifier);
	  Reader in = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
	  return DataPackageFactory.getDataPackage(in); 
  }
  
  /** Create a new connection to metacat */
  public void establishConnection()
  {
      if (networkStatus) {
          ConnectionFrame cf = new ConnectionFrame(morpho);
          cf.setVisible(true);
      } else {
          morpho.getProfile().set("searchmetacat", 0, "false");
          Log.debug(6,
          			/*"No network connection available - can't log in"*/
          		   Language.getInstance().getMessage("Morpho.NoNetworkConnection")
          			);
      }
  }
  
	/*
	 * Gets next version from metacat. "getrevisionanddoctype" method of Metacat API will
	 * return the max version of metacat. So we should increase 1 to get next version number.
	 * If couldn't connect metacat or metacat doesn't have this docid, 1 will be returned.
	 */
	public int getNextRevisionNumber(String identifier)
	{
		int version = AbstractDataPackage.ORIGINAL_REVISION;
		String semiColon = ";";
		//String docid = null;
		//Gets metacat url from configuration
		if (morpho != null && getNetworkStatus() && identifier != null)
		{
			String result = null;
		    Properties lastVersionProp = new Properties();
		    lastVersionProp.put("action", "getrevisionanddoctype");
		    //docid = getPackageId();
		    lastVersionProp.put("docid", identifier);
		    result = getMetacatString(lastVersionProp);
		    Log.debug(30, "the result is ============= "+result);
		    // Get version
		    if (result != null)
		    {
		    	int index = result.indexOf(semiColon);
		    	if (index != -1)
		    	{
		    		String versionStr = result.substring(0, index);
		    		try
		    		{
		    			version = (new Integer(versionStr).intValue());
		    			//increase 1 to get next version
		    			version = version +1;
		    		}
		    		catch(Exception e)
		    		{
		    			Log.debug(20, "Couldn't transfer version string "+versionStr +" into integer");
		    		}
		    	}
		    }
		    
		}
		Log.debug(30, "Next version for doicd " +identifier+" in metacat is "+version);
		return version;
	}
  
  /**
   * Send a request to Metacat
   *
   * @param prop  the properties to be sent to Metacat
   * @return      InputStream as returned by Metacat
   */
  synchronized private InputStream getMetacatInputStream(Properties prop)
  {   connectionBusy = true;
      InputStream returnStream = null;
      // Now contact metacat and send the request

      /*
          Note:  The reason that there are three try statements all executing
          the same code is that there is a problem with the initial connection
          using the HTTPClient protocol handler.  These try statements make
          sure that a connection is made because it gives each connection a
          2nd and 3rd chance to work before throwing an error.
          THIS IS A TOTAL HACK.  THIS NEEDS TO BE LOOKED INTO AFTER THE BETA1
          RELEASE OF MORPHO!!!  cwb (7/24/01)
        */
      try {
          Log.debug(20, "Sending data to: " + metacatURL);
          URL url = new URL(metacatURL);
          HttpMessage msg = new HttpMessage(url);
          returnStream = msg.sendPostData(prop);
         connectionBusy = false;
         return returnStream;
      } catch (Exception e) {
          try {
              Log.debug(20, "Sending data (again) to : " + metacatURL);
              URL url = new URL(metacatURL);
              HttpMessage msg = new HttpMessage(url);
              returnStream = msg.sendPostData(prop);
              connectionBusy = false;
              return returnStream;
          } catch (Exception e2) {
              try {
                  Log.debug(20, "Sending data (again)(again) to: " +
                      metacatURL);
                  URL url = new URL(metacatURL);
                  HttpMessage msg = new HttpMessage(url);
                  returnStream = msg.sendPostData(prop);
                  connectionBusy = false;
                  return returnStream;
              } catch (Exception e3) {
                  Log.debug(1, "Fatal error sending data to Metacat: " +
                      e3.getMessage());
                  e.printStackTrace(System.err);
              }
          }
      }
      connectionBusy = false;
      return returnStream;
  }

  /**
   * Send a request to Metacat
   *
   * @param prop  the properties to be sent to Metacat
   * @return      a string as returned by Metacat
   */
  private String getMetacatString(Properties prop)
  {
      String response = null;

      // Now contact metacat and send the request
      try {
          InputStreamReader returnStream =
                  new InputStreamReader(getMetacatInputStream(prop));
          StringWriter sw = new StringWriter();
          int len;
          char[] characters = new char[512];
          while ((len = returnStream.read(characters, 0, 512)) != -1) {
              sw.write(characters, 0, len);
          }
          returnStream.close();
          response = sw.toString();
          sw.close();
      } catch (Exception e) {
          Log.debug(1, "Fatal error sending data to Metacat.");
      }
      return response;
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
    Reader reader;
    
    File localfile = new File(getCacheDir() + "/" + path); //the path to the file
    File localdir = new File(getCacheDir() + "/" + dirs); //the dir part of the path
    
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
        InputStream metacatInput = getMetacatInputStream(props);
        
        // set here because previous line call to getMetacatInputStream will set
        // to false
        connectionBusy = true;

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
        reader = new InputStreamReader(new FileInputStream(localfile), Charset.forName("UTF-8"));
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
        connectionBusy = false;
        return localfile;
      }
      catch (FileNotFoundException mde) {
        throw mde;
      }
      catch(Exception e)
      {
        e.printStackTrace();
        connectionBusy = false;
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
    Reader reader;
    
    File localfile = new File(getCacheDir() + "/" + path); //the path to the file
    File localdir = new File(getCacheDir() + "/" + dirs); //the dir part of the path
    
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
        InputStream metacatInput = getMetacatInputStream(props);
        // set here because previous line call to getMetacatInputStream will set
        // to false
        connectionBusy = true;

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
        reader = new InputStreamReader(new FileInputStream(localfile), Charset.forName("UTF-8"));
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
        connectionBusy = false;
        return localfile;
      }
      catch (FileNotFoundException mde) {
        throw mde;
      }
      catch(Exception e)
      {
        e.printStackTrace();
        connectionBusy = false;
        return null;
      }
    }
  }
  
  /** Send the given query to Metacat, get back the XML resultset
   * @param query the pathquery string for "squery" action
   * @return the XML resultset for the query
   */
  @Override
  public InputStream query(String query)
  {
    Log.debug(30, "(2.1) Executing metacat query...");
    InputStream queryResult = null;

    Properties prop = new Properties();
    prop.put("action", "squery");
    prop.put("query", query);
    prop.put("qformat", "xml");
    try
    {
      queryResult = morpho.getMetacatDataStoreService().getMetacatInputStream(prop);
    }
    catch(Exception w)
    {
      Log.debug(1, "Error in submitting structured query");
      Log.debug(1, w.getMessage());
    }

    Log.debug(30, "(2.3) Metacat output is:\n" + queryResult);
    Log.debug(30, "(2.4) Done Executing metacat query...");
    return queryResult;
  }
  
  /**
   * Look up the principal (accounts) from Metacat
   * @return
   */
  public InputStream getPrincipals() {
	  Properties prop = new Properties();
	  prop.put("action", "getprincipals");

      InputStream queryResult = getMetacatInputStream(prop);
      
      return queryResult;
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
	String status  = DataStoreServiceInterface.NONEXIST;
    String path = parseId(name);
    String dirs = path.substring(0, path.lastIndexOf("/")); 
    File localfile = new File(getCacheDir() + "/" + path); //the path to the file
    File localdir = new File(getCacheDir() + "/" + dirs); //the dir part of the path
    
    if((localfile.exists())&&(localfile.length()>0))
    { //if the file is cached locally, read it from the hard drive
      Log.debug(30, "MetacatDataStore: cached file exists and docid is used "+name);
      return DataStoreServiceInterface.CONFLICT;
    }
    else
    {    
    	 // if the filelength is zero, delete it
	      if (localfile.length()==0) 
	      {
	        localfile.delete();
	      }
         //    Gets metacat url from configuration
		if (Morpho.thisStaticInstance != null && getNetworkStatus())
		{
			String result = null;
		    Properties lastVersionProp = new Properties();
		    lastVersionProp.put("action", "getrevisionanddoctype");
		   
		    lastVersionProp.put("docid", name);
		    connectionBusy = true;
		    try
		    {
			    result = getMetacatString(lastVersionProp);
			    connectionBusy = false;
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
			    				status = DataStoreServiceInterface.UPDATE;
			    			}
			    			else
			    			{
			    				status = DataStoreServiceInterface.CONFLICT;
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
			    		status = DataStoreServiceInterface.NONEXIST;
			    	}
			    }
		    }
		    catch(Exception e)
		    {
		    	connectionBusy = false;	    
		   }
		}
    }
    Log.debug(30, "The docid "+name + " status in metacat is "+status);
	return status;
}
  
  /**
	 * Does this id exist in Metacat?
	 * @param identifier
	 * @return
	 */
	public boolean exists(String identifier) {
		
		String status = DataStoreServiceInterface.NONEXIST;
		status = status(identifier);
		return !status.equals(DataStoreServiceInterface.NONEXIST);
		
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
    StringBuffer messageBuf = new StringBuffer();

    BufferedReader bfile = new BufferedReader(file);
    try
    {
      //save a temp file so that the id can be put in the file.
      StringWriter sw = new StringWriter();
      File tempfile = new File(getTempDir() + "/metacat.noid");
      Writer fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempfile), Charset.forName("UTF-8")));
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
      metacatInput = getMetacatInputStream(prop, true);
      // set here because previous line call to getMetacatInputStream will set
      // to false
      connectionBusy = true;
      
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
          connectionBusy = false;
          return openFile(docid);
        }
        catch(Exception ee)
        {
          bmetacatInputReader.close();
          metacatInput.close();
          ee.printStackTrace();
          connectionBusy = false;
          return null;
        }
      }
      else
      {//something weird happened.
        connectionBusy = false;
        throw new Exception("unexpected error in edu.ucsb.nceas.morpho." +
                            ".datastore.MetacatDataStore.saveFile(): " + message);
      } 
    }
    catch(Exception e)
    {
      connectionBusy = false;
      throw new MetacatUploadException(e.getMessage());
    }
  }

	/**
	 * parses the id of a file from the message that metacat returns
	 */
	private String parseIdFromMessage(String message) {
		int docidIndex = message.indexOf("<docid>") + 1;
		int afterDocidIndex = docidIndex + 6;
		String docid = message.substring(afterDocidIndex, message.indexOf("<", afterDocidIndex));
		debug(11, "docid in parseIdFromMessage: " + docid);
		return docid;
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
        metacatInput = sendDataFile(id, file, objectName);

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
   * Delete given ADP from MDS
   */
  @Override
  public boolean delete(AbstractDataPackage adp) {
	  // TODO: do more delete of data objects?
	  return this.deleteFile(adp.getAccessionNumber());
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
    metacatInput = getMetacatInputStream(prop, true);
    connectionBusy = true;
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
      connectionBusy = false;
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
      connectionBusy = false;
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
      connectionBusy = false;
      return true;
    }
    else
    {//something weird happened.
      connectionBusy = false;
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
    metacatInput = getMetacatInputStream(prop, true);
    connectionBusy = true;
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
      connectionBusy = false;
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
      connectionBusy = false;
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
      connectionBusy = false;
      return true;
    }
    else
    {//something weird happened.
      connectionBusy = false;
      return false;
    } 
  }

  /**
	 * Gets the last docid from metacat system
	 * 
	 * @param scope the docid scope
	 * @return the last docid for given scope, withour revision or scope
	 */
	private int getLastDocid(String scope) {

		String result = null;
		String temp = null;
		int metacatId = 0;

		Properties lastIDProp = new Properties();
		lastIDProp.put("action", "getlastdocid");
		lastIDProp.put("scope", scope);
		temp = getMetacatString(lastIDProp);

		Log.debug(30, "the last id from metacat ===== " + temp);
		if (temp != null) {
			int ind1 = temp.indexOf("<docid>");
			int ind2 = temp.indexOf("</docid>");
			if ((ind1 > 0) && (ind2 > 0)) {
				result = temp.substring(ind1 + 7, ind2);
				if (!result.equals("null")) {
					// now remove the version and scope parts of the id
					result = result.substring(0, result.lastIndexOf("."));
					result = result.substring(result.indexOf(".") + 1, result.length());
					try {
						// double check that it is a number
						metacatId = (new Integer(result).intValue());
					} catch (NumberFormatException nfe) {
						Log.debug(30, "Last id from metacat: '" + result + "' is not integer.");
					}

				}

			}
		}
		
		return metacatId;

	}
	
	/**
	 * Generate identifer from Metacat store
	 * @return
	 */
	@Override
	public String generateIdentifier() {
		String identifier = null;
		String separator = Morpho.thisStaticInstance.getProfile().get("separator", 0);
		String scope = Morpho.thisStaticInstance.getProfile().get("scope", 0);
		// Get last id
		int lastid = getLastDocid(scope);
		if (lastid > -1) {
			// in order to get next id, this number should be increase 1
			lastid++;

			// scope.docid
			identifier = scope + separator + lastid + separator + 1;
		}
		
		return identifier;
	}
  
/**
   * sets access to a document in metacat. 
   * @param docid id to set permission for
   * @param accessBlock eml-access XML block
   * @return true if successful
   */
  public boolean setAccess(String docid, String accessBlock)
  {
    StringBuffer messageBuf = new StringBuffer();
    Properties prop = new Properties();
    prop.put("action", "setaccess");
    prop.put("docid", docid);
    prop.put("accessBlock", accessBlock);

    Log.debug(11, "setting accessBlock for docid: " + docid + " on metacat");
    
    InputStream metacatInput = null;
    metacatInput = getMetacatInputStream(prop, true);
    connectionBusy = true;
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
      Log.debug(0, "Error setting accessBlock in metacat: " + 
                            ioe.getMessage());
      connectionBusy = false;
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
      connectionBusy = false;
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
      connectionBusy = false;
      return true;
    }
    else
    {//something weird happened.
      connectionBusy = false;
      return false;
    } 
  }

/**
   * gets access control block for a document in metacat. 
   * @param docid id to set permission for
   * @return String with XML access block
   */
  public String getAccess(String docid)
  {
    StringBuffer messageBuf = new StringBuffer();
    Properties prop = new Properties();
    prop.put("action", "getaccesscontrol");
    prop.put("docid", docid);
    
    Log.debug(11, "getting access for docid: " + docid + " on metacat");
    
    InputStream metacatInput = null;
    metacatInput = getMetacatInputStream(prop, true);
    connectionBusy = true;
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
      Log.debug(0, "Error getting access in metacat: " + 
                            ioe.getMessage());
      connectionBusy = false;
      return null;
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
      connectionBusy = false;
      return null;
    }
    else if(message.indexOf("access>") != -1)
    { //the operation worked
      try
      {
        bmetacatInputReader.close();
        metacatInput.close();
      }
      catch(Exception e)
      {}
      connectionBusy = false;
      return message;
    }
    else
    {//something weird happened.
      connectionBusy = false;
      return null;
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
      
      MetacatDataStoreService mds = new MetacatDataStoreService(morpho);
      mds.logIn();
      
      // Test metadata (xml) upload
      Log.debug(20, "Testing metadata upload...");
      String id = args[2];
      File f = new File(args[3]);
      Reader fr = new InputStreamReader(new FileInputStream(f), Charset.forName("UTF-8"));
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
 
  /**
   * Send a request to Metacat
   *
   * @param prop           the properties to be sent to Metacat
   * @param requiresLogin  indicates whether a valid connection is required
   *                       for the operation
   * @return               InputStream as returned by Metacat
   */
  public InputStream getMetacatInputStream(Properties prop,
          boolean requiresLogin)
  {
      if (requiresLogin) {
          if (!connected) {
              // Ask the user to connect
              establishConnection();
          }
      }
      return getMetacatInputStream(prop);
  }
  
  /**
   * Determines if the framework has a valid login
   *
   * @return   boolean true if connected to Metacat, false otherwise
   */
  public boolean isConnected()
  {
      return connected;
  }
  
  /**
   * Determines if the framework is using an ssl connection
   *
   * @return   boolean true if using SSL, false otherwise
   */
  public boolean getSslStatus()
  {
      return sslStatus;
  }
  
  /**
   * Determine whether a network connection is available before trying to open
   * a socket, since this would cause an error
   *
   * @return   boolean true if the network is reachable
   */
  public boolean getNetworkStatus()
  {
      return networkStatus;
  }
  
  public void setMetacatURL(String metacatURL) {
	  this.metacatURL = metacatURL;
	  // need to recheck the ssl status
      sslStatus = (metacatURL.indexOf("https://") == 0);
      //create URL object to poll for metacat connectivity since the metaca may be changed.
      try {
          metacatPingURL = new URL(metacatURL);
      } catch (MalformedURLException mfue) {
          Log.debug(5, "unable to read or resolve Metacat URL");
      }
  }
  
  public String getMetacatURL() {
	  return metacatURL;
  }
  
  /**
   * sends a data file to the metacat using "multipart/form-data" encoding
   *
   * @param id    the id to assign to the file on metacat (e.g., knb.1.1)
   * @param file  the file to send
   * @param objectName  the object name associate with the file
   * @return      the response stream from metacat
   */
  private InputStream sendDataFile(String id, File file, String objectName)
  {
      String filename = null;
      InputStream returnStream = null;
      File newFile = null;

      if (!connected) {
          // Ask the user to connect
          establishConnection();
      }

      // Now contact metacat and send the request
      try {
          //FileInputStream data = new FileInputStream(file);

          Log.debug(20, "Sending data to: |" + metacatURL + "|");
          URL url = new URL(metacatURL.trim());
          HttpMessage msg = new HttpMessage(url);
          Properties args = new Properties();
          args.put("action", "upload");
          args.put("docid", id);

          Properties dataStreams = new Properties();
          // use object name to replace the meaningless name such as 12.2
          if (objectName != null && !Util.isBlank(objectName))
          {
          	String tmpDir = Morpho.getConfiguration().get("tempDir", 0);
          	newFile = new File(tmpDir, objectName);
          	FileInputStream input = new FileInputStream(file);
          	FileOutputStream out = new FileOutputStream(newFile);
          	byte[] c = new byte[3*1024];
          	int read = input.read(c);
          	while (read !=-1)
          	{
          		out.write(c, 0, read);
          		read = input.read(c);
          	}
          	input.close();
          	out.close();
          	filename = newFile.getAbsolutePath();
          }
          else
          {
              filename = file.getAbsolutePath();
          }
          Log.debug(20, "Sending data file: " + filename);
          dataStreams.put("datafile", filename);

          /*
          Note:  The reason that there are three try statements all executing
          the same code is that there is a problem with the initial connection
          using the HTTPClient protocol handler.  These try statements make
          sure that a connection is made because it gives each connection a
          2nd and 3rd chance to work before throwing an error.
          THIS IS A TOTAL HACK.  THIS NEEDS TO BE LOOKED INTO AFTER THE BETA1
          RELEASE OF MORPHO!!!  cwb (7/24/01)
            */
          try {
              returnStream = msg.sendPostData(args, dataStreams);
          } catch (Exception ee) {
              try {
                  returnStream = msg.sendPostData(args, dataStreams);
              } catch (Exception eee) {
                  try {
                      returnStream = msg.sendPostData(args, dataStreams);
                  } catch (Exception eeee) {
                      throw new Exception(eeee.getMessage());
                  }
              }
          }
      } catch (Exception e) {
          Log.debug(1, "Fatal error sending binary data to Metacat: " +
                  e.getMessage());
          e.printStackTrace(System.err);
      }
      finally
      {
      	try
      	{
      		
      		if(newFile != null)
      		{
      			Log.debug(40, "delete file===============");
      			newFile.delete();
      		}
      	}
      	catch(Exception e)
      	{
      		 Log.debug(20, "============couldn't delete the new file ");
      	}
      }
      return returnStream;
  }
  
  /**
   * Log into metacat.
   *
   * @return   boolean true if the attempt to log in succeeded
   */
  public boolean logIn()
  {
      Properties prop = new Properties();
      prop.put("action", "login");
      prop.put("qformat", "xml");
      Log.debug(20, "Logging in using uid: " + morpho.getUserName());
      prop.put("username", morpho.getUserName());
      prop.put("password", morpho.getPassword());

      // Now contact metacat
      String response = getMetacatString(prop);
      boolean wasConnected = connected;
      if (response.indexOf("<login>") != -1) {
          connected = true;
      } else {
          HttpMessage.setCookie(null);
          connected = false;
      }

      if (wasConnected != connected) {
          UIController controller = UIController.getInstance();
          if (controller != null) {
              controller.updateAllStatusBars();
          }
          morpho.fireConnectionChangedEvent();
      }

      return connected;
  }

  /**
   * Log out of metacat
   */
  public void logOut()
  {
      if (connected) {
    	  morpho.setPassword("none");
          // get rid of existing password info
          Properties prop = new Properties();
          prop.put("action", "logout");
          prop.put("qformat", "xml");

          String response = getMetacatString(prop);
          doLogoutCleanup();
      }
  }
  
  /**
   * Log out of metacat when exiting.
   */
  public void logOutExit()
  {
      if (connected) {
          morpho.setPassword("none");
          // get rid of existing password info
          Properties prop = new Properties();
          prop.put("action", "logout");
          prop.put("qformat", "xml");

          String response = getMetacatString(prop);
          HttpMessage.setCookie(null);
          connected = false;

      }
  }
  
  /**
   * cleanup routine called by logout() and by MetacatPinger thread Keeps all
   * this stuff in one place so as not repeat code
   */
  private void doLogoutCleanup()
  {
      HttpMessage.setCookie(null);
      connected = false;
      if (UIController.getInstance()!= null)
      {
         UIController.getInstance().updateAllStatusBars();
      }
      morpho.fireConnectionChangedEvent();
  }
  
  /**
   * overload to give default functionality; boolean flag needed only at
   * startup
   */
  private void doPing()
  {
      doPing(false);
  }

  /**
   * Sets networkStatus to boolean true if metacat connection can be made
   *
   * @param isStartUp  - set to boolean "true" when calling for first time, so
   *      we don't get "No such service registered." exception from
   *      getServiceProvider()
   */
  private void doPing(final boolean isStartUp)
  {
    if (!connectionBusy) {
      final SwingWorker sbUpdater =
          new SwingWorker()
          {
              public Object construct()
              {
                  startPing(isStartUp);
                  return null;
                  //return value not used by this program
              }

              //Runs on the event-dispatching thread.
              public void finished()
              {
                  finishPing(isStartUp);
              }
          };
      sbUpdater.start();
    }
  }

  /**
   * Start the ping operation. At startup this is called in the main
   * application thread, but later it is used in a distinct thread to keep the
   * application responsive.
   */
  private void startPing(boolean isStartup)
  {
      //check if metacat can be reached:
      origNetworkStatus = networkStatus;
      try {
          Log.debug(55, "Determining net status ...");
          urlConn = metacatPingURL.openConnection();
          urlConn.connect();
          networkStatus = (urlConn.getDate() > 0L);
          Log.debug(55, "... which is: " + networkStatus);
      } catch (IOException ioe) {
      	if(isStartup && ioe.getMessage().contains(AUTHENTICATEERROR))
      	{
             Log.debug(5, " - Unable to open network connection to Metacat: "+ioe.getMessage());
      	}
      	else
      	{
      		Log.debug(55, " - unable to open network connection to Metacat");
      	}
          networkStatus = false;
          if (morpho.getProfile() != null) {
        	  morpho.getProfile().set("searchmetacat", 0, "false");
          }
      } catch (NullPointerException npe) {
            Log.debug(55, " - unable to open network connection to Metacat");
            networkStatus = false;
            if (morpho.getProfile() != null) {
            	morpho.getProfile().set("searchmetacat", 0, "false");
            }
        }
  }

  /**
   * Finish the ping operation. At startup this is called in the main
   * application thread, but later it is used in a distinct thread to keep the
   * application responsive.
   *
   * @param isStartUp  set to true if this is the startup sequence before
   *                   plugins have been loaded
   */
  private void finishPing(boolean isStartUp)
  {
      Log.debug(55, "doPing() called - network available?? - " +
              networkStatus);
      if (origNetworkStatus != networkStatus) {
          //if lost connection, can't log out, but can still do cleanup
          if (!networkStatus) {
              morpho.getProfile().set("searchmetacat", 0, "false");
              doLogoutCleanup();
          } else {
              if (!isStartUp) {
                  //update package list
                  /*
                      try {
                      ServiceProvider provider
                      = getServiceProvider(QueryRefreshInterface.class);
                      ((QueryRefreshInterface)provider).refresh();
                      } catch (ServiceNotHandledException snhe) {
                      Log.debug(6, snhe.getMessage());
                      }
                    */
              	//When got the network connection, we should reset last docid, since
              	//remote metacat may have bigger docid number
              	if (morpho.getProfile() != null)
              	{
              		Log.debug(55, "reset lastid when network is avaliable");
              	    String scope = morpho.getProfile().get("scope", 0);
              	  morpho.setLastID(scope);
              	}
              }
              //update status bar
          }
          if (!isStartUp) {
              UIController.getInstance().updateAllStatusBars();
          }
      }
  }

  /**
   * This ActionListener is notified by the swing.Timer every
   * METACAT_PING_INTERVAL milliSeconds, upon which it tries to contact the
   * Metacat defined by "metacatURL"
   */
  ActionListener pingActionListener =
      new ActionListener()
      {
          public void actionPerformed(ActionEvent e)
          {
              doPing();
          }
      };
}
