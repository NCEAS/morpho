/**
 *  '$RCSfile: HttpMessage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-07-13 17:29:01 $'
 * '$Revision: 1.11.2.1 $'
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
import java.net.*;
import java.util.*;

public class HttpMessage
{
  public String contype;
  private URL servlet = null;
  private String argString = null;
  private static String cookie = null;

  public HttpMessage(URL servlet)
  {
    this.servlet = servlet;
  }

  // Performs a GET request to the previously given servlet
  // with no query string
  public InputStream sendGetMessage() throws IOException
  {
    return sendGetMessage(null);
  }

  //Performs a GET request to the previously given servlet
  // Builds a query string from the supplied Properties list.
  public InputStream sendGetMessage(Properties args) throws IOException
  {
    argString = "";//default

    if (args != null)
    {
      argString = "?" + toEncodedString(args);
    }
    URL url = new URL(servlet.toExternalForm() + argString);

    // turn off caching
    URLConnection con = url.openConnection();
    con.setUseCaches(false);
    contype = con.getContentType();

    return con.getInputStream();
  }

  //Performs a POST request to the previously given servlet
  //with no query string
  public InputStream sendPostMessage() throws IOException
  {
    return sendPostMessage(null);
  }

  //Builds post data from the supplied properties list
  public InputStream sendPostMessage(Properties args) throws IOException
  {
    argString = "";//default
    if (args != null)
    {
      argString = toEncodedString(args);
    }
    URLConnection con = servlet.openConnection();
    if (cookie != null)
    {
      int k = cookie.indexOf(";");
      if (k > 0)
      {
        cookie = cookie.substring(0, k);
      }
      con.setRequestProperty("Cookie", cookie);
      // add 10/26/00 by DFH so Metacat can determine where request come from
      con.setRequestProperty("User-Agent", "MORPHO");
    }
    //prepare for both input and output
    con.setDoInput(true);
    con.setDoOutput(true);
    //turn off caching
    con.setUseCaches(false);

    //work around a Netscape bug
    //con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    //Write the arguments as post data
    DataOutputStream out = new DataOutputStream(con.getOutputStream());
    out.writeBytes(argString);
    out.flush();
    contype = con.getContentType();
    String temp = con.getHeaderField("Set-Cookie");
    if (temp != null)
    {
      cookie = temp;
      int k = cookie.indexOf(";");
      if (k > 0)
      {
        cookie = cookie.substring(0, k);
      }
    }
    out.close();

    return con.getInputStream();
  }

  public String getArgString()
  {
    String argString1 = argString;
    if (!argString1.startsWith("?"))
    {
      argString1 = "?" + argString1;
    }
    return argString1;
  }

  //Converts a Properties list to a URL-encoded query string    
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
