/**
 *  '$RCSfile: PackageWizardParser.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-10-29 23:34:47 $'
 * '$Revision: 1.5 $'
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
public class PackageWizardParser extends DefaultHandler 
{
  private String currentTag = new String();
  private XMLElement doc;
  private XMLElement groupObj;
  private XMLElement textObj;
  private Stack groupStack = new Stack();
  private String doctype = null;
  private String dtd = null;
  private String root = null;
  private boolean rootFlag = true;
  
  /**
   * @param xml a FileReader object that reprents a stream of XML
   * @param parserName the fully specifified parser name to be used in 
   * processing
   */
  
  public PackageWizardParser(FileReader xml, String parserName)
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
  
  /**
   * This method is called whenever a new start tag is encountered.  It sorts
   * the elements and puts them into their structured XMLElement object.
   */
  public void startElement(String uri, String localName, String qName, 
                           Attributes attributes) throws SAXException
  {
    /*
    basic algorithm:  
    -if(group element)
    {
      -find group elements
      -record group element information into group obj
      -push group obj onto stack
    }
    -if(text element)
    { 
      -pop groupobj from stack
      -record text attribute info
      -insert text info into group obj content vector
      -push group obj back on stack.
    }
    -if(combo element)
    {
      -pop groupobj from stack
      -record combo attribute info
      -insert combo (text) obj into group obj content vector
      -push group obj back on stack
    }
    -if(item element)
    {
      -pop group obj from stack
      -get last combo (text) element from content vector
      -get item information
      -put item info into combo element's content vector
      -put the combo element back in the group obj content vector
      -push the group obj back onto the stack
    }
    */
    //System.out.println("Start " + localName);
    AttributesImpl atts = new AttributesImpl(attributes);
    currentTag = localName;
    if(currentTag.equals("wizard"))
    {
      groupObj = new XMLElement();
      groupObj.name = currentTag;
      if(atts != null)
      {
        int len = atts.getLength();
        for(int i=0; i<len; i++)
        { //get each of the attributes on the group object and put them
          //into the hashtable of the groupObj
          String locName = atts.getLocalName(i);
          String val = atts.getValue(i);
          if(locName.equals("doctype"))
          {
            doctype = val;
          }
          else if(locName.equals("dtd"))
          {
            dtd = val;
          }
          
          groupObj.attributes.put(locName, val);
        }
        groupStack.push(groupObj);
      }
    }
    else if(currentTag.equals("group"))
    { 
      groupObj = new XMLElement();
      groupObj.name = currentTag;
      if(atts != null)
      {
        int len = atts.getLength();
        for(int i=0; i<len; i++)
        { //get each of the attributes on the group object and put them
          //into the hashtable of the groupObj
          String locName = atts.getLocalName(i);
          String val = atts.getValue(i);
          if(locName.equals("field") && rootFlag)
          {
            root = val;
            rootFlag = false;
          }
          groupObj.attributes.put(locName, val);
        }
        groupStack.push(groupObj);
        //System.out.println("pushing groupObj");
      }
    }
    else if(currentTag.equals("textbox") || currentTag.equals("combobox"))
    {
      XMLElement tempgroup = new XMLElement();
      //System.out.println("popping tempgroup in textbox||combobox");
      tempgroup = (XMLElement)groupStack.pop();
      textObj = new XMLElement();
      textObj.name = currentTag;
      if(atts != null)
      {
        int len = atts.getLength();
        for(int i=0; i<len; i++)
        { //get each of the attributes on the group object and put them
          //into the hashtable of the groupObj 
          String locName = atts.getLocalName(i);
          String val = atts.getValue(i);
          textObj.attributes.put(locName, val);
        }
        tempgroup.content.addElement(textObj);
        groupStack.push(tempgroup);
        //System.out.println("pushing tempgroup");
      }
    }
    else if(currentTag.equals("item"))
    {    
      //System.out.println("in item");
      XMLElement tempgroup = new XMLElement();
      //System.out.println("popping tempgroup in item");
      tempgroup = (XMLElement)groupStack.pop();
      textObj = new XMLElement();
//      textObj = (XMLElement)tempgroup.content.remove(tempgroup.content.size()-1);
      textObj = (XMLElement)tempgroup.content.elementAt(tempgroup.content.size()-1);
      tempgroup.content.removeElementAt(tempgroup.content.size()-1);
      XMLElement item = new XMLElement();
      item.name = currentTag;
      if(atts != null)
      {
        int len = atts.getLength();
        for(int i=0; i<len; i++)
        { //get each of the attributes on the group object and put them
          //into the hashtable of the groupObj
          String locName = atts.getLocalName(i);
          String val = atts.getValue(i);
          item.attributes.put(locName, val);
        }
      }
      textObj.content.addElement(item);
      tempgroup.content.addElement(textObj);
      groupStack.push(tempgroup);
    }
  }
  
  /**
   
   */
  public void endElement(String uri, String localName, String qName) 
              throws SAXException
  {
    //System.out.println("end " + localName);
    /*
      Basic algorithm:
      -if(group element)
      {
        -pop a group element from stack G1
        -pop another group element from the stack G2
        -insert G1 in G2's content vector
        -push g2
      }
      -if(wizard element)
      {
        -pop off the last element
        -assign it to doc
      }
    */
    currentTag = localName;
    if(currentTag.equals("group"))
    { 
      XMLElement g1 = new XMLElement((XMLElement)groupStack.pop());
      XMLElement g2 = new XMLElement((XMLElement)groupStack.pop());
      g2.content.addElement(g1);
      groupStack.push(g2);
    }
    else if(currentTag.equals("wizard"))
    { //note that at this point there should only be one group element
      //left on the stack...the original wizard group that was pushed.
      //so pop it off and this is the whole document object that 
      //should be returned.
      doc = new XMLElement((XMLElement)groupStack.pop());
    }
  }
  
  /**
   * Returns the document as an XMLElement object.
   */
  public XMLElement getDoc()
  {
    return doc;  
  }
  
  /**
   * Returns the path to the dtd that this config file represents 
   */
  public String getDtd()
  {
    return dtd;
  }
  
  /**
   * Returns the public identifier of the doctype that this config file 
   * represents
   */
  public String getDoctype()
  {
    return doctype;
  }
  
  /**
   * Returns the public identifier of the doctype that this config file 
   * represents
   */
  public String getRoot()
  {
    return root;
  }
  
  /**
   * prints a string representation of the doc object
   */
  public void printDoc(XMLElement e)
  {
    System.out.println(e.name + ":" + e.attributes.toString());
    printDocRec(e, new String());
  }
  
  private void printDocRec(XMLElement e, String spaces)
  {
    spaces += "  ";
    for(int i=0; i<e.content.size(); i++)
    {
      XMLElement temp = (XMLElement)e.content.elementAt(i);
      System.out.println(spaces + temp.name + ":" + temp.attributes.toString());
      printDocRec((XMLElement)e.content.elementAt(i), spaces);
    }
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

      PackageWizardParser pwp = new PackageWizardParser(xml, 
			      "org.apache.xerces.parsers.SAXParser");
      System.out.println("Doc is: " );
      pwp.printDoc(pwp.getDoc());
    }
    catch(Exception e)
    {
      System.out.println("error in main");
      e.printStackTrace(System.out);
    }
    System.out.println("Done parsing " + args[0]);
  }
}
