/**
 *  '$RCSfile: HttpMessage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2005-06-14 22:07:03 $'
 * '$Revision: 1.17 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Log;

import java.io.*;
import java.net.*;
import java.util.*;

import HTTPClient.NVPair;


/**
 *   This class includes code to handle 'cookies' by looking at request and
 *   response headers. If the Sun url handlers are used, this code is used
 *   However, if the HttpClient class is used as the url handler, that class
 *   handles cookies internally and strips the information out of the headers
 *   before this class looks at them. This the cookie handler included here
 *   will never be called!   DFH - October 2003
 */

public class HttpMessage
{
  private URL servlet = null;
  private String argString = null;
  private static String cookie = null;
  private OutputStream out = null;
  private URLConnection con = null;

  public HttpMessage(URL servlet)
  {
    this.servlet = servlet;
  }

  /**
   * Performs a GET request to the previously given servlet
   * with no query string
   */
  public InputStream sendGetMessage() throws IOException
  {
    return sendGetMessage(null);
  }

  /**
   * Performs a GET request to the previously given servlet
   * Builds a query string from the supplied Properties list.
   */
  public InputStream sendGetMessage(Properties args) throws IOException
  {
    argString = "";//default

    if (args != null) {
      argString = "?" + toEncodedString(args);
    }
    URL url = new URL(servlet.toExternalForm() + argString);

    // turn off caching
    con = url.openConnection();
    con.setUseCaches(false);

    return con.getInputStream();
  }

  /**
   * Open a new post connection, preparing the request headers, including cookies
   */
  private void openPostConnection() throws IOException
  {
    // Open the connection
    con = servlet.openConnection();
    Log.debug(20, "HTTP Handler class is: " +
            con.getClass().getName());

    // Write any cookies in the request
    if (cookie != null) {
      int k = cookie.indexOf(";");
      if (k > 0) {
        cookie = cookie.substring(0, k);
      }
      con.setRequestProperty("Cookie", cookie);
    }

    // add so Metacat can determine where requests come from
    con.setRequestProperty("User-Agent", "Morpho/" + Morpho.VERSION);

    // prepare for both input and output
    con.setDoInput(true);
    con.setDoOutput(true);
    // turn off caching
    con.setUseCaches(false);
  }

  /**
   * Sends post data using multipart/form-data encoding. This method can send
   * large data files because the files are streamed directly from disk to the
   * HttpURLConnection.  Assuming that we are using the HTTClient or another
   * similar library that provides a streaming HttpURLConnection, then the
   * data is sent to the connection as it is read from disk (in contrast to the
   * default Sun HttpURLConnection that reads the whole data stream into memory
   * before sending it.
   *
   * @param args a property file containing the name-value pairs that are to be
   *             sent to the server
   * @param fileNames a property file containing the name for a formfield
   *                  that represents a file and the filename (as the property value)
   * @return the response stream that comes from the server
   * @exception IOException If any file operation fails.
   */
  public InputStream sendPostData(Properties args, Properties fileNames)
                     throws IOException
  {
    openPostConnection();

    // Prepare the parameters
    int len = args.size();
    NVPair[] opts = new NVPair[len];
    Enumeration names = args.propertyNames();
    for (int i=0; i<len; i++) {
      String name = (String)names.nextElement();
      String value = args.getProperty(name);
      opts[i] = new NVPair(name, value);
    }
    // Prepare the data files
    len = fileNames.size();
    NVPair[] data = new NVPair[len];
    Enumeration dataNames = fileNames.propertyNames();
    for (int i=0; i<len; i++) {
      String name = (String)dataNames.nextElement();
      String value = fileNames.getProperty(name);
      data[i] = new NVPair(name, value);
    }

    // Create the multipart/form-data form object
    MultipartForm myform = new MultipartForm(opts, data);

    // Set some addition request headers
    ((HttpURLConnection)con).setRequestMethod("POST");
    String ctype = myform.getContentType();
    ((HttpURLConnection)con).setRequestProperty("Content-Type", ctype);
    long contentLength = myform.getLength();
    ((HttpURLConnection)con).setRequestProperty("Content-Length",
             new Long(contentLength).toString());

    // Open the output stream and write the encoded data to it
    out = con.getOutputStream();
    myform.writeEncodedMultipartForm(out);

    // close the connection and return the response stream
    InputStream res = closePostConnection();
    return res;
  }

  /**
   * Sends post data using url encoding.  This method is used most of the time
   * and is for typical paramameter lists where the data is not extensive.
   *
   * @param args a property file containing the name-value pairs that are to be
   *             sent to the server
   * @return the response stream that comes from the server
   * @exception IOException If any file operation fails.
   */
  public InputStream sendPostData(Properties args) throws IOException
  {
    openPostConnection();
    out = new DataOutputStream(con.getOutputStream());
    Enumeration names = args.propertyNames();
    while (names.hasMoreElements()) {
      String name = (String)names.nextElement();
      String value = args.getProperty(name);
      sendNameValuePair(name, value);
      if (names.hasMoreElements()) {
        ((DataOutputStream)out).writeBytes("&");
        out.flush();
      }
    }
    InputStream res = closePostConnection();
    return res;
  }

  /**
   * Utility method to URL encode and send a single name-value pair
   */
  private void sendNameValuePair(String name, String data) throws IOException
  {
	  // do not log passwords
	  // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=4687
	  if (name.indexOf("password") > -1) {
		  Log.debug(15, "Name: " + name + " => " + "*****");
	  } else {
		  Log.debug(15, "Name: " + name + " => " + data);
	  }
    ((DataOutputStream)out).writeBytes(URLEncoder.encode(name));
    ((DataOutputStream)out).writeBytes("=");
    ((DataOutputStream)out).writeBytes(URLEncoder.encode(data));
    out.flush();
  }

  /**
   * Clean up the post connection, save any cookies, close the output stream
   *
   * @return the response stream that comes from the server
   * @exception IOException If any file operation fails.
   */
  private InputStream closePostConnection() throws IOException
  {
    // Open the response stream
    InputStream response;
    response = con.getInputStream();
    // Read any cookies in the response
    String temp = con.getHeaderField("Set-Cookie");
    if (temp != null) {
      cookie = temp;
      int k = cookie.indexOf(";");
      if (k > 0) {
        cookie = cookie.substring(0, k);
      }
    }
    out.close();
    // Return the response stream
    return response;
  }

  /**
   * Performs a POST request with no query parameters
   */
  public InputStream sendPostMessage() throws IOException
  {
    return sendPostMessage(null);
  }

  /**
   * Sends post data using url encoding.  This method is used most of the time
   * and is for typical paramameter lists where the data is not extensive.
   *
   * @param args a property file containing the name-value pairs that are to be
   *             sent to the server
   * @return the response stream that comes from the server
   * @exception IOException If any file operation fails.
   * @see #sendPostData(Properties args)
   * @deprecated Replaced by #sendPostData(Properties args)
   */
  public InputStream sendPostMessage(Properties args) throws IOException
  {
    return sendPostData(args);
  }

  /**
   * Converts a Properties list to a URL-encoded query string
   */
  private String toEncodedString(Properties args)
  {
    StringBuffer buf = new StringBuffer();
    Enumeration names = args.propertyNames();
    while (names.hasMoreElements())
    {
      String name = (String) names.nextElement();
      String value = args.getProperty(name);
        buf.append(URLEncoder.encode(name) + "=" + URLEncoder.encode(value));
      if (names.hasMoreElements())
          buf.append("&");
    }
    return buf.toString();
  }

  /**
   * return the cookie that this message object contains
   */
  public static String getCookie()
  {
    return cookie;
  }

  /**
   * return the cookie that this message object contains
   */
  public static void setCookie(String newCookie)
  {
    cookie = newCookie;
  }
}
