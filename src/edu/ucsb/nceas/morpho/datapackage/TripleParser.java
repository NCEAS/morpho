/**
 *  '$RCSfile: TripleParser.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-09-07 20:28:08 $'
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
package edu.ucsb.nceas.morpho.datapackage;

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
import java.io.Reader;

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

import com.arbortext.catalog.*;

/** 
 * A Class implementing callback bethods for the SAX parser.  This class finds
 * any triple tag in the xml file and creates a TripleCollection of Triple objects.
 */
public class TripleParser extends DefaultHandler 
{
  private Triple triple;
  private TripleCollection collection = new TripleCollection();
  String tag = "";
  boolean instart = true;
  
  public TripleParser(Reader xml, String parserName)
  {
    doInit(xml, parserName, null);
  }
  
  public TripleParser(Reader xml, String parserName, CatalogEntityResolver cer)
  {
    doInit(xml, parserName, cer);
  }
  
  private void doInit(Reader xml, String parserName, CatalogEntityResolver cer)
  {
    XMLReader parser = initializeParser(parserName);
    if (parser == null) 
    {
      System.err.println("SAX parser not instantiated properly.");
    }
    try 
    {
      if(cer != null)
      {
        parser.setEntityResolver(cer);
      }
      parser.parse(new InputSource(xml));
    } 
    catch (SAXException e) 
    {
      System.err.println("error parsing data in " + 
                         "TripleParser.TripleParser");
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
       System.err.println("Error in TripleParser.initializeParser " + 
                           e.toString());
       e.printStackTrace();
    }
    return parser;
  }
  
  public void startElement(String uri, String localName, String qName, 
                           Attributes attributes) throws SAXException
  {
    tag = localName;
    if(localName.equals("triple"))
    { //create a new triple object to hold the next triple
      triple = new Triple();
    }
    instart=true;
  }
  
  public void endElement(String uri, String localName, String qName) 
              throws SAXException
  {
    if(localName.equals("triple"))
    { //we are at the end of a triple so add the new triple to the collection
      collection.addTriple(triple);
    }
    instart=false;
  }
  
  public void characters(char[] ch, int start, int length)
  {
    if(instart)
    {
      String content = new String(ch, start, length);
      if(tag.equals("subject"))
      { //get the subject content
        triple.setSubject(content);
      }
      else if(tag.equals("relationship"))
      { //get the relationship content
        triple.setRelationship(content);
      }
      else if(tag.equals("object"))
      { //get the object content
        triple.setObject(content);
      }
    }
  }
  
  /**
   * returns the TripleCollection of all of the triples in this document
   */
  public TripleCollection getTriples()
  {
    return collection;
  }
  
  /**
   * command line test method.  the first argument is the file you want to parse
   */
  public static void main(String[] args)
  {
    //get the document name, parse it then output the
    //the doc object as text.
    if(args.length == 0)
    {
      System.out.println("usage: TripleParser <xml_file>");
      return;
    }
    
    String filename = args[0];
    
    System.out.println("Parsing " + args[0]);
    try
    {
      FileReader xml = new FileReader(new File(filename));

      //while(xml.ready())
      //  System.out.print((char)xml.read());

      TripleParser tp = new TripleParser(xml, 
			      "org.apache.xerces.parsers.SAXParser");
      System.out.println("Triples are:" );
      System.out.println(tp.getTriples().toString());
    }
    catch(Exception e)
    {
      System.out.println("error in main");
      e.printStackTrace(System.out);
    }
    System.out.println("Done parsing " + args[0]);
  }
}
