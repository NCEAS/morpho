/**
 *  '$RCSfile: TripleCollection.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-22 17:51:05 $'
 * '$Revision: 1.7 $'
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

import edu.ucsb.nceas.morpho.framework.*;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xalan.xpath.xml.FormatterToXML;
import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import java.util.*;
import java.io.*;

/**
 * This class implements a collection of triples which make up all of the
 * relationships in a package.
 */
public class TripleCollection
{
  private Vector triples = new Vector();
  /**
   * Default Constructor
   */
  public TripleCollection()
  {
    
  }
  
  /**
   * parses an xml document and pulls any triples out of it.
   */
   public TripleCollection(File triplesFile, ClientFramework framework)
   {
     Document doc;
     try
     {
       ConfigXML config = framework.getConfiguration();
       String catalogPath = config.get("local_catalog_path", 0);
       doc = PackageUtil.getDoc(triplesFile, catalogPath);
     }
     catch(Exception e)
     {
       framework.debug(0, "error parsing " + triplesFile.getPath() + " : " +
                         e.getMessage());
       e.printStackTrace();
       return;
     }
     
     String triplePath = "//triple";
     NodeList tripleList = null;
     try
     {
       tripleList = XPathAPI.selectNodeList(doc, triplePath);
     }
     catch(Exception e)
     {
       ClientFramework.debug(0, "Error parsing triples in " + 
                                "TripleCollection.TripleCollection: " +
                                e.getMessage());
       e.printStackTrace();
     }
     
     for(int i=0; i<tripleList.getLength(); i++)
     {
       Node triple = tripleList.item(i);
       NodeList children = triple.getChildNodes();
       String sub = null;
       String rel = null;
       String obj = null;
       if(children.getLength() > 2)
       {
         for(int j=0; j<children.getLength(); j++)
         {
           Node childNode = children.item(j);
           String nodename = childNode.getNodeName().trim().toUpperCase();
           if(nodename.equals("SUBJECT"))
           {
             sub = childNode.getFirstChild().getNodeValue();
           }
           else if(nodename.equals("OBJECT"))
           {
             obj = childNode.getFirstChild().getNodeValue();
           }
           else if(nodename.equals("RELATIONSHIP"))
           {
             rel = childNode.getFirstChild().getNodeValue();
           }
         }
         
         Triple t = new Triple(sub, rel, obj);
         triples.addElement(t);
       }
     }
   }
  
  /**
   * Copy constructor.  instantiate this object from the given TripleCollection
   */
  public TripleCollection(TripleCollection tc)
  {
    this.triples = tc.getCollection();
  }
  
  /**
   * read an xml file, build the collection from any triples in the xml file.
   */
  public TripleCollection(Reader xml)
  {
    TripleParser tp = new TripleParser(xml, 
                                       "org.apache.xerces.parsers.SAXParser");
    this.triples = tp.getTriples().getCollection();
  }
  
  /**
   * create a collection of triples from a vector of Triple objects.
   */
  public TripleCollection(Vector triples)
  {
    this.triples = new Vector(triples);
  }
  
  /**
   * Add a single triple to this collection
   */
  public void addTriple(Triple triple)
  {
    this.triples.addElement(triple);
  }
  
  /**
   * returns true if the collection contains triple, false otherwise
   */
  public boolean containsTriple(Triple triple)
  {
    return triples.remove(triple);
  }
  
  /**
   * removes the specified triple from the collection and returns it.  If the
   * triple was not in the collection null is returned.
   */ 
  public Triple removeTriple(Triple triple)
  {
    boolean removed = triples.remove(triple);
    if(removed)
    {
      return triple;
    }
    else
    {
      return null;
    }
  }
  
  /**
   * returns a vector of triples with the given subject
   */
  public Vector getCollectionBySubject(String subject)
  {
    Vector trips = new Vector();
    for(int i=0; i<triples.size(); i++)
    {
      Triple trip = new Triple((Triple)triples.elementAt(i));
      if(trip.getSubject().equals(subject))
      {
        trips.addElement(trip);
      }
    }
    return trips;
  }
  
  /**
   * returns a vector of triples with the given relationship
   */
  public Vector getCollectionByRelationship(String relationship)
  {
    Vector trips = new Vector();
    for(int i=0; i<triples.size(); i++)
    {
      Triple trip = new Triple((Triple)triples.elementAt(i));
      if(trip.getRelationship().equals(relationship))
      {
        trips.addElement(trip);
      }
    }
    return trips;
  }
  
  /**
   * returns a vector of triples with the given object
   */
  public Vector getCollectionByObject(String object)
  {
    Vector trips = new Vector();
    for(int i=0; i<triples.size(); i++)
    {
      Triple trip = new Triple((Triple)triples.elementAt(i));
      if(trip.getObject().equals(object))
      {
        trips.addElement(trip);
      }
    }
    return trips;
  }
  
  /**
   * return a vector of Triple objects that represent this collection.
   */
  public Vector getCollection()
  {
    return this.triples;
  }
  
  public String toXML()
  {
    return toXML(null);
  }
  
  /**
   * returns a string representation of this collection in xml format.
   * the xml looks like this:
   * <pre>
   * &lt;triple&gt;&lt;subject&gt;some content 1&lt;/subject&gt;
   * &lt;relationship&gt;some content 2&lt;/relationship&gt;&lt;object&gt;
   * some content 3&lt;/object&gt;&lt;/triple&gt;&lt;triple&gt;
   * &lt;subject&gt;some content 1&lt;/subject&gt;&lt;relationship&gt;
   * some content 2&lt;/relationship&gt;&lt;object&gt;some content 3
   * &lt;/object&gt;&lt;/triple&gt;
   * .....
   * &lt;triple&gt;&lt;subject&gt;some content 1&lt;/subject&gt;
   * &lt;relationship&gt;some content 2&lt;/relationship&gt;&lt;object&gt;
   * some content 3&lt;/object&gt;&lt;/triple&gt;
   * </pre>
   */
  public String toXML(String root)
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\"?>");
    if(root != null)
    {
      sb.append("<" + root + ">");
    }
    
    for(int i=0; i<triples.size(); i++)
    {
      sb.append(((Triple)triples.elementAt(i)).toXML());
    }
    
    if(root != null)
    {
      sb.append("</" + root + ">");
    }
    
    return sb.toString();
  }
  
  public NodeList getNodeList()
  {
    DOMParser parser = new DOMParser();
    InputSource in;
    in = new InputSource(new StringReader(toXML("triples")));
    try
    {
      parser.parse(in);
    }
    catch(Exception e1)
    {
      System.err.println("triples: parse threw: " + 
                         e1.toString());
      //e1.printStackTrace();
    }
    Document doc = parser.getDocument();
    return doc.getElementsByTagName("triple");
  }
  
  /**
   * returns a nicely formatted string representation of this collection in xml
   * the xml looks like this:
   * <pre>
   * &lt;triple&gt;
   *   &lt;subject&gt;some content 1&lt;/subject&gt;
   *   &lt;relationship&gt;some content 2&lt;/relationship&gt;
   *   &lt;object&gt;some content 3&lt;/object&gt;
   * &lt;/triple&gt;
   * &lt;triple&gt;
   *   &lt;subject&gt;some content 1&lt;/subject&gt;
   *   &lt;relationship&gt;some content 2&lt;/relationship&gt;
   *   &lt;object&gt;some content 3&lt;/object&gt;
   * &lt;/triple&gt;
   * .....
   * &lt;triple&gt;
   *   &lt;subject&gt;some content 1&lt;/subject&gt;
   *   &lt;relationship&gt;some content 2&lt;/relationship&gt;
   *   &lt;object&gt;some content 3&lt;/object&gt;
   * &lt;/triple&gt;
   * </pre>
   * without the formatting (i.e. no indents or extra spaces)
   */
  public String toFormatedXML()
  {
    StringBuffer sb = new StringBuffer();
    for(int i=0; i<triples.size(); i++)
    {
      sb.append(((Triple)triples.elementAt(i)).toFormatedXML());
    }
    return sb.toString();
  }
  
  /**
   * return this collection as a string
   */
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("[");
    for(int i=0; i<triples.size(); i++)
    {
      Triple t = new Triple((Triple)triples.elementAt(i));
      sb.append(t.toString());
      if(i != triples.size()-1)
      {
        sb.append(",");
      }
    }
    sb.append("]");
    return sb.toString();
  }
  
  public static void main(String[] args)
  {
    if(args.length == 0)
    {
      System.out.println("usage: TripleCollection <xml_file>");
      return;
    }
    
    String filename = args[0];
    
    try
    {
      FileReader xml = new FileReader(new File(filename));
      TripleCollection tc = new TripleCollection(xml);
      System.out.println("Triples are:" );
      System.out.println(tc.toString());
      //test the getCollectionBy methods
      Vector v = tc.getCollectionBySubject("1.s");
      System.out.println(v.toString());
      v = tc.getCollectionByObject("2.o");
      System.out.println(v.toString());
      v = tc.getCollectionByRelationship("1.r");
      System.out.println(v.toString());
      //test addTriple
      Triple t = new Triple("3.s", "3.r", "3.o");
      tc.addTriple(t);
      System.out.println(tc.toString());
      //test removeTriple
      Triple u = tc.removeTriple(t);
      System.out.println(u.toString());
      System.out.println(tc.toString());
      Triple x = new Triple();
      u = tc.removeTriple(x);
      if(u != null)
        System.out.println(u.toString());
      else
        System.out.println("u == null");
      System.out.println(tc.toString());
      //check containsTriple
      tc.addTriple(t);
      if(tc.containsTriple(t))
        System.out.println("it's there");
      else
        System.out.println("it's not");
      tc.removeTriple(t);
      if(tc.containsTriple(t))
        System.out.println("it's there");
      else
        System.out.println("it's not");
      //test toXML
      tc.addTriple(t);
      System.out.println(tc.toXML());
      System.out.println(tc.toFormatedXML());
    }
    catch(Exception e)
    {
      System.out.println("error in main");
      e.printStackTrace(System.out);
    }
    System.out.println("Done with " + args[0]);
  }
}
