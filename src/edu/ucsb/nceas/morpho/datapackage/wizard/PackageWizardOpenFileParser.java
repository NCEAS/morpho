/**
 *  '$RCSfile: PackageWizardOpenFileParser.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-22 22:04:32 $'
 * '$Revision: 1.2 $'
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
package edu.ucsb.nceas.morpho.datapackage.wizard;

import java.sql.*;
import java.util.Stack;
import java.util.Vector;
import java.util.Enumeration; 
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Hashtable;
import java.io.FileReader;
import java.io.Reader;
import java.io.File;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.InputSource;

/** 
 * A Class implementing callback bethods for the SAX parser to
 * call when processing the XML messages from the Package Wizard
 */
public class PackageWizardOpenFileParser extends DefaultHandler 
{
  private String currentTag = new String();
  private String path = new String();
  private Vector paths = new Vector();
  private Hashtable pathcontent = new Hashtable();
  private StringBuffer configFile = new StringBuffer();
  private Vector configFileV = new Vector();
  boolean instart = true;
  
  /**
   * @param xml a FileReader object that reprents a stream of XML
   * @param parserName the fully specifified parser name to be used in 
   * processing
   */
  
  public PackageWizardOpenFileParser(Reader xml, String parserName)
  {
    XMLReader parser = initializeParser(parserName);
    if (parser == null) 
    {
      System.err.println("SAX parser not instantiated properly.");
    }
    try 
    {
      parser.parse(new InputSource(xml));
    } 
    catch (SAXException e) 
    {
      System.err.println("error parsing data in " + 
                         "PackageWizardParser.PackageWizardParser");
      System.err.println(e.getMessage());
      e.printStackTrace(System.out);
    }
    catch (IOException ioe)
    {
      System.out.println("IO Exception: ");
      ioe.printStackTrace(System.out);
    }
  }
  
  private XMLReader initializeParser(String parserName) 
  {
    XMLReader parser = null;
    // Set up the SAX document handlers for parsing
    try 
    {
      // Get an instance of the parser
      parser = XMLReaderFactory.createXMLReader(parserName);
      // Set the ContentHandler to this instance
      parser.setContentHandler(this);
      // Set the error Handler to this instance
      parser.setErrorHandler(this);
    } 
    catch (Exception e) 
    {
       System.err.println("Error in PackageWizardParser.initializeParser " + 
                           e.toString());
       e.printStackTrace();
    }
    return parser;
  }
  
  public void startElement(String uri, String localName, String qName, 
                           Attributes attributes) throws SAXException
  { //add the new tag to the path
    currentTag = localName;
    path += "/" + currentTag;
    //System.out.println("path1: " + path);
    instart = true;
  }
  
  public void endElement(String uri, String localName, String qName) 
              throws SAXException
  { //remove the closed tag from the path
    if(path.indexOf(localName) > 0)
    {
      path = path.substring(0, path.indexOf(localName) - 1);
    }
    //System.out.println("path2: " + path);
    instart = false;
  }
  
  public void characters(char[] ch, int start, int length)
  { //add the path and the content to the hash
    String content = new String(ch, start, length);
    if(instart && !content.trim().equals(""))
    { //here we add a text box
      StringBuffer newline = new StringBuffer();
      
      newline.append("<textbox field=\"").append(currentTag.trim());
      newline.append("\" defaulttext=\"").append(content.trim()).append("\"/>\n");
      configFile.append(newline.toString());
      configFileV.addElement(newline.toString());
      
      while(pathcontent.containsKey(path))
      {
        path += " ";
      }
      //System.out.println("path: " + path + "| content: " + content + "|");
      pathcontent.put(path, content.trim());
    }
    else if(instart && content.trim().equals(""))
    { //here we add a group
      StringBuffer newline = new StringBuffer();
      newline.append("<group field=\"").append(currentTag.trim());
      newline.append("\">\n");
      configFile.append(newline.toString());
      configFileV.addElement(newline.toString());
    }
    
    if(instart)
    {
      paths.addElement(path);
    }
  }
  
  public Hashtable getHash()
  {
    return pathcontent;
  }
  
  public Vector getPaths()
  {
    return paths;
  }
  
  public String getConfigFile()
  {
    return configFile.toString();  
  }
  
  public Vector getConfigFileV()
  {
    return configFileV;
  }
  
  public String getField(String line)
  {
    int fieldindex = line.indexOf("field") + 7;
    String name = line.substring(fieldindex, line.indexOf("\"", fieldindex+1));
    return name;
  }
  
  public String addAttributes(String line, Hashtable atts)
  {
    String attributes = new String();
    Enumeration keys = atts.keys();
    while(keys.hasMoreElements())
    {
      String key = (String)keys.nextElement();
      if(!key.equals("defaulttext") && !key.equals("field"))
      attributes += key + "=\"" + atts.get(key) + "\" ";
    }
    //System.out.println("attributes: " + attributes);
    //System.out.println("line: " + line);
    
    int endindex = line.indexOf("/>");
    String tempString = line.substring(0, endindex);
    tempString += " " + attributes + "/>";
    System.out.println("newline: " + tempString);
    return tempString;
  }
  
  public static void main(String[] args)
  {
    //get the document name, parse it then output the
    //the doc object as text.
    if(args.length == 0)
    {
      System.out.println("usage: PackageWizardParser <xml_file>");
      return;
    }
    
    String filename = args[0];
    System.out.println("Parsing " + args[0]);
    try
    {
      FileReader xml = new FileReader(new File(filename));

      //while(xml.ready())
      //  System.out.print((char)xml.read());

      PackageWizardOpenFileParser pwp = new PackageWizardOpenFileParser(xml, 
			      "org.apache.xerces.parsers.SAXParser");
      System.out.println("Hashtable is: " );
      //System.out.println(pwp.getHash().toString());
      Hashtable h = pwp.getHash();
      Enumeration keys = h.keys();
      while(keys.hasMoreElements())
      {
        String key = (String)keys.nextElement();
        System.out.print(key + "| : |" );
        System.out.print(h.get(key) + "|");
        System.out.println();
      }
      
      System.out.println("paths:");
      //System.out.println(pwp.getPaths().toString());
      Vector pathsV = pwp.getPaths();
      for(int i=0; i<pathsV.size(); i++)
      {
        System.out.println(pathsV.elementAt(i));
      }
    }
    catch(Exception e)
    {
      System.out.println("error in main");
      e.printStackTrace(System.out);
    }
    System.out.println("Done parsing " + args[0]);
  }
}
