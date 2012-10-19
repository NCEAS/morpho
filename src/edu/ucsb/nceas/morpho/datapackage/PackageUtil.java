/**
 *  '$RCSfile: PackageUtil.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-12-15 20:28:31 $'
 * '$Revision: 1.26 $'
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.arbortext.catalog.Catalog;
import com.arbortext.catalog.CatalogEntityResolver;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * This class contains static utility methods that are used throughtout the
 * other *.morpho.datapackage.* classes.
 */
public class PackageUtil
{
  
  /**
   * Takes in a vector of paths and searches for each of the paths until a node
   * is found that matches the paths.  It returns the node of the first path 
   * in the vector that it matches.  if none of the paths in the vector match
   * it returns null
   */
  public static NodeList getPathContent(File f, Vector<String> paths, Morpho morpho)
  {
    for(int i=0; i<paths.size(); i++)
    {
      String s = paths.elementAt(i);
      NodeList nl = getPathContent(f, s, morpho);
      if(nl != null && nl.getLength() != 0)
      {
        return nl;
      }
    }
    return null;
  }
  
  /**
   * gets the content of a tag in a given xml file with the given path
   * @param f the file to parse
   * @param path the path to get the content from
   * @param morpho a morpho object that has a valid config file
   */
  private static NodeList getPathContent(File f, String path, Morpho morpho)
  {
    Document doc;
    try{
      doc = getDoc(f, morpho);
    }
    catch(Exception e1) {
          System.err.println("File: " + f.getPath() + " : parse threw (1): " + 
                         e1.toString());
      return null;    }
    
    try
    {
      long start_time_xpath = System.currentTimeMillis();
      NodeList docNodeList = XPathAPI.selectNodeList(doc, path);
      long stop_time = System.currentTimeMillis();
 //     Log.debug(10,"Time for prenode search: "+(start_time_xpath-start_time));
 //     Log.debug(10,"Time for nodesearch: "+(stop_time-start_time_xpath));
      return docNodeList;
    }
    catch(TransformerException se)
    {
      System.err.println("File: " + f.getPath() + " : parse threw (2): " + 
                         se.toString());
      return null;
    }
  }
  
  /**
   * parses file with the dom parser and returns a dom Document
   * @param file the file to create the document from
   * @param catalogPath the path to the catalog where the files doctype info
   * can be found.
   */
  public static Document getDoc(File file, String catalogPath) throws 
                                                               SAXException, 
                                                               Exception
  {
    long start_time = System.currentTimeMillis();
            
    DocumentBuilder parser = Morpho.createDomParser();
    Document doc;
    InputSource in;
//    FileInputStream fs;
    Reader fs;
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try 
    {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      //ConfigXML config = morpho.getConfiguration();
      //String catalogPath = config.getConfigDirectory() + File.separator +
                                        //config.get("local_catalog_path", 0);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL catalogURL = cl.getResource(catalogPath);
        
      myCatalog.parseCatalog(catalogURL.toString());
      //myCatalog.parseCatalog(catalogPath);
      cer.setCatalog(myCatalog);
    } 
    catch (Exception e) 
    {
      Log.debug(11, "Problem creating Catalog in " +
                   "packagewizardshell.handleFinishAction!" + e.toString());
      throw new Exception(e.getMessage());
    }
    
    parser.setEntityResolver(cer);
    
    try
    { //parse the wizard created file without the triples
//      fs = new FileInputStream(file);
      fs = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
      in = new InputSource(fs);
    }
    catch(FileNotFoundException fnf)
    {
      fnf.printStackTrace();
      throw new Exception(fnf.getMessage());
    }
    try
    {
      doc = parser.parse(in);
      fs.close();
    }
    catch(Exception e1)
    {
      throw new Exception(e1.getMessage());
    }
    long stop_time = System.currentTimeMillis();
    Log.debug(10,"Time for getDoc: "+(stop_time-start_time));

    return doc;
  }
 

  /**
   * parses file with the dom parser and returns a dom Document
   * @param file the file to create the document from
   * @param morpho the top level Morpho class
   */
  private static Document getDoc(File file, Morpho morpho) throws 
                                                               SAXException, 
                                                               Exception
  {
    DocumentBuilder parser = Morpho.createDomParser();
    Document doc;
    InputSource in;
//    FileInputStream fs;
    Reader fs;
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try 
    {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      ConfigXML config = Morpho.getConfiguration();
      String catalogPath = config.get("local_catalog_path", 0);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL catalogURL = cl.getResource(catalogPath);
        
      myCatalog.parseCatalog(catalogURL.toString());
      //myCatalog.parseCatalog(catalogPath);
      cer.setCatalog(myCatalog);
    } 
    catch (Exception e) 
    {
      Log.debug(11, "Problem creating Catalog in " +
                   "packagewizardshell.handleFinishAction!" + e.toString());
      throw new Exception(e.getMessage());
    }
    
    parser.setEntityResolver(cer);
    
    try
    { //parse the wizard created file without the triples
//      fs = new FileInputStream(file);
      fs = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
      in = new InputSource(fs);
    }
    catch(FileNotFoundException fnf)
    {
      fnf.printStackTrace();
      throw new Exception(fnf.getMessage());
    }
    try
    {
      doc = parser.parse(in);
      fs.close();
    }
    catch(Exception e1)
    {
      throw new Exception(e1.getMessage());
    }
    
    return doc;
  }



}
