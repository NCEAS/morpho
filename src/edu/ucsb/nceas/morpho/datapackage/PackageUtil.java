/**
 *  '$RCSfile: PackageUtil.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-22 16:14:55 $'
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

import org.apache.xerces.parsers.DOMParser;
import org.apache.xalan.xpath.xml.FormatterToXML;
import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.apache.xerces.dom.DocumentTypeImpl;

import edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;

import java.io.*;
import java.util.*;
import java.lang.*;

import com.arbortext.catalog.*;

/**
 * This class contains static utility methods that are used throughtout the
 * other morpho.datapackage.* classes.
 */
public class PackageUtil
{
  /**
   * gets the content of a tag in a given xml file with the given path
   * @param f the file to parse
   * @param path the path to get the content from
   * @param framework a framework object that has a valid config file
   */
  public static NodeList getPathContent(File f, String path, 
                                        ClientFramework framework)
  {
    if(f == null)
    {
      return null;
    }
   
    DOMParser parser = new DOMParser();
    InputSource in;
    FileInputStream fs;
    
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try 
    {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      ConfigXML config = framework.getConfiguration();
      String catalogPath = config.get("local_catalog_path", 0);
      myCatalog.parseCatalog(catalogPath);
      cer.setCatalog(myCatalog);
    } 
    catch (Exception e) 
    {
      ClientFramework.debug(11, "Problem creating Catalog in " +
                   "packagewizardshell.handleFinishAction!" + e.toString());
    }
    
    parser.setEntityResolver(cer);
    
    try
    { 
      fs = new FileInputStream(f);
      in = new InputSource(fs);
    }
    catch(FileNotFoundException fnf)
    {
      fnf.printStackTrace();
      return null;
    }
    
    try
    {
      parser.parse(in);
      fs.close();
    }
    catch(Exception e1)
    {
      System.err.println("File: " + f.getPath() + " : parse threw: " + 
                         e1.toString());
      return null;
    }
    
    Document doc = parser.getDocument();
    
    try
    {
      NodeList docNodeList = XPathAPI.selectNodeList(doc, path);
      return docNodeList;
    }
    catch(SAXException se)
    {
      System.err.println("file: " + f.getPath() + " : parse threw: " + 
                         se.toString());
      return null;
    }
  }
  
  /**
   * This method can 'print' any DOM subtree. Specifically it is
   * set (by means of 'out') to write the in-memory DOM to the
   * same XML file that was originally read. Action thus saves
   * a new version of the XML doc.  Adapted from configXML.java.
   * 
   * @param node node usually set to the 'doc' node for complete XML file
   * re-write
   */
  public static String print(Node node)
  {
    StringBuffer sb = new StringBuffer();
    // is there anything to do?
    if (node == null)
    {
      return null;
    }

    int type = node.getNodeType();
    switch (type)
    {
      // print document
    case Node.DOCUMENT_NODE:
    {

      sb.append("<?xml version=\"1.0\"?>");
      print(((Document) node).getDocumentElement());
      //sb.flush();
      break;
    }

      // print element with attributes
    case Node.ELEMENT_NODE:
    {
      sb.append('<');
      sb.append(node.getNodeName());
      Attr attrs[] = sortAttributes(node.getAttributes());
      for (int i = 0; i < attrs.length; i++)
      {
        Attr attr = attrs[i];
        sb.append(' ');
        sb.append(attr.getNodeName());
        sb.append("=\"");
        sb.append(normalize(attr.getNodeValue()));
        sb.append('"');
      }
      sb.append('>');
      NodeList children = node.getChildNodes();
      if (children != null)
      {
        int len = children.getLength();
        for (int i = 0; i < len; i++)
        {
          sb.append(print(children.item(i)));
        }
      }
      break;
    }

      // handle entity reference nodes
    case Node.ENTITY_REFERENCE_NODE:
    {
      sb.append('&');
      sb.append(node.getNodeName());
      sb.append(';');

      break;
    }

      // print cdata sections
    case Node.CDATA_SECTION_NODE:
    {
      sb.append("<![CDATA[");
      sb.append(node.getNodeValue());
      sb.append("]]>");

      break;
    }

      // print text
    case Node.TEXT_NODE:
    {
      sb.append(normalize(node.getNodeValue()));
      break;
    }

      // print processing instruction
    case Node.PROCESSING_INSTRUCTION_NODE:
    {
      sb.append("<?");
      sb.append(node.getNodeName());
      String data = node.getNodeValue();
      if (data != null && data.length() > 0)
      {
        sb.append(' ');
        sb.append(data);
      }
      sb.append("?>");
      break;
    }
    }

    if (type == Node.ELEMENT_NODE)
    {
      sb.append("</");
      sb.append(node.getNodeName());
      sb.append('>');
    }

    //sb.flush();
    return sb.toString();
  }
  
  /** Returns a sorted list of attributes. Taken from configXML.java*/
  protected static Attr[] sortAttributes(NamedNodeMap attrs)
  {

    int len = (attrs != null) ? attrs.getLength() : 0;
    Attr array[] = new Attr[len];
    for (int i = 0; i < len; i++)
    {
      array[i] = (Attr) attrs.item(i);
    }
    for (int i = 0; i < len - 1; i++)
    {
      String name = array[i].getNodeName();
      int index = i;
      for (int j = i + 1; j < len; j++)
      {
        String curName = array[j].getNodeName();
        if (curName.compareTo(name) < 0)
        {
          name = curName;
          index = j;
        }
      }
      if (index != i)
      {
        Attr temp = array[i];
        array[i] = array[index];
        array[index] = temp;
      }
    }

    return (array);

  } // sortAttributes(NamedNodeMap):Attr[]

  /** Normalizes the given string. Taken from configXML.java*/
  protected static String normalize(String s)
  {
    StringBuffer str = new StringBuffer();

    int len = (s != null) ? s.length() : 0;
    for (int i = 0; i < len; i++)
    {
      char ch = s.charAt(i);
      switch (ch)
      {
      case '<':
      {
        str.append("&lt;");
        break;
      }
        case '>':
      {
        str.append("&gt;");
        break;
      }
      case '&':
      {
        str.append("&amp;");
        break;
      }
      case '"':
      {
        str.append("&quot;");
        break;
      }
      case '\r':
      case '\n':
      {
        // else, default append char
      }
      default:
      {
        str.append(ch);
      }
      }
    }

    return (str.toString());
  } 
  
  /** 
   * prints out the doctype part of and xml document.  this can be appended to 
   * the output from print().
   * @param doc the dom of the document to print the doctype for
  */
  public static String printDoctype(Document doc)
  {
    DocumentTypeImpl dt = (DocumentTypeImpl)doc.getDoctype();
    String publicid = dt.getPublicId();
    String systemid = dt.getSystemId();
    String nameid = dt.getName();
    String docString = "<?xml version=\"1.0\"?>";
    docString += "\n<!DOCTYPE " + nameid +  " PUBLIC \"" + publicid + 
                 "\" \"" + systemid + "\">\n";
    return docString;
  }
}
