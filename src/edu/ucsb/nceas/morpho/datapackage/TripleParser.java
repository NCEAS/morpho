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
 *     '$Date: 2001-05-04 15:23:36 $'
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

/** 
 * A Class implementing callback bethods for the SAX parser.  This class finds
 * any triple tag in the xml file and creates a TripleCollection of Triple objects.
 */
public class TripleParser extends DefaultHandler 
{
  private Triple triple;
  private TripleCollection collection = new TripleCollection();
  String tag;
  
  public TripleParser(FileReader xml, String parserName)
  {
    XMLReader parser = initializeParser(parserName);
    if (parser == null) 
    {
      System.err.println("SAX parser not instantiated properly.");
    }
    try 
    {
      //Reader r = is.getCharacterStream();
      //while(r.ready())
      //  System.out.print((char)r.read());
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
    if(localName.equals("triple"))
    {
      triple = new Triple();
    }
    else if(localName.equals("subject"))
    {
      tag = "subject";
    }
    else if(localName.equals("relationship"))
    {
      tag = "relationship";
    }
    else if(localName.equals("object"))
    {
      tag = "object";
    }
  }
  
  public void endElement(String uri, String localName, String qName) 
              throws SAXException
  {
    if(localName.equals("triple"))
    {
      collection.addTriple(triple);
    }
  }
  
  public void characters(char[] ch, int start, int length)
  {
    String content = new String(ch, start, length);
    if(tag.equals("subject"))
    {
      triple.setSubject(content);
    }
    else if(tag.equals("relationship"))
    {
      triple.setRelationship(content);
    }
    else if(tag.equals("object"))
    {
      triple.setObject(content);
    }
  }
  
  public TripleCollection getTriples()
  {
    return collection;
  }
  
  public static void main(String[] args)
  {
    //get the document name, parse it then output the
    //the doc object as text.
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
