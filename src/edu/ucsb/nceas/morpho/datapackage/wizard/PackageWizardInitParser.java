/**
 *  '$RCSfile: PackageWizardInitParser.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-09 16:47:01 $'
 * '$Revision: 1.1 $'
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
public class PackageWizardInitParser extends DefaultHandler 
{
  private String currentTag = new String();
  private Vector dtd = new Vector();
  
  /**
   * @param xml a FileReader object that reprents a stream of XML
   * @param parserName the fully specifified parser name to be used in 
   * processing
   */
  
  public PackageWizardInitParser(FileReader xml, String parserName)
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
  {
    currentTag = localName;
    if(currentTag.equals("dtd"))
    {
      AttributesImpl atts = new AttributesImpl(attributes);
      int len = atts.getLength();
      Hashtable atthash = new Hashtable();
      for(int i=0; i<len; i++)
      { //get each of the attributes on the dtd object and put them
        //into the hashtable
        String locName = atts.getLocalName(i);
        String val = atts.getValue(i);
        atthash.put(locName, val);
      }
      dtd.addElement(atthash);
    }
  }
  
  public Vector getDtd()
  {
    return this.dtd;
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

      PackageWizardInitParser pwp = new PackageWizardInitParser(xml, 
			      "org.apache.xerces.parsers.SAXParser");
      System.out.println("Doc is: " );
      System.out.println(pwp.getDtd().toString());
    }
    catch(Exception e)
    {
      System.out.println("error in main");
      e.printStackTrace(System.out);
    }
    System.out.println("Done parsing " + args[0]);
  }
}
