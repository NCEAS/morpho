/**
 *       Name: ConfigXML.java
 *    Purpose: Used to store various information for application
 *             configuration in an XML file
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-04-25 18:54:32 $'
 * '$Revision: 1.1.2.3 $'
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

package edu.ucsb.nceas.dtclient;


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
import com.arbortext.catalog.*;
import java.io.*;
import java.util.Vector;
import java.util.Hashtable;

/**
 * This class is designed to store configuration information in
 * an XML file. The concept is similar to that of a Properties
 * file except that using the XML format allows for a hierarchy
 * of properties and repeated properties.
 * 
 * All 'keys' are element names, while values are always stored
 * as XML text nodes. The XML file is parsed and stored in 
 * memory as a DOM object. 
 * 
 * Note that nodes are specified by node tags rather than paths
 * 
 * @author Dan Higgins
 * @version 1.0
 */
public class ConfigXML
{

  /**
   * root node of the in-memory DOM structure
   */
  public Node root;

  /**
   * Document node of the in-memory DOM structure
   */
  public Document doc;

  /**
   * XML file name in string form
   */
  public String fileName;

  /**
   * Print writer (output)
   */
  protected PrintWriter out;


  /**
   * String passed to the creator is the XML config file name
   * 
   * @param filename name of XML file
   */
  public ConfigXML(String filename)
  {
    fileName = filename;
    //   out = new PrintWriter(new OutputStreamWriter(System.out));

    DOMParser parser = new DOMParser();
    File XMLConfigFile = new File(filename);
    InputSource in;
    FileInputStream fs;
      try
    {
      fs = new FileInputStream(filename);
      in = new InputSource(fs);
    }
    catch(FileNotFoundException fnf)
    {
      System.err.println("FileInputStream of " + filename + " threw: " +
			 fnf.toString());
      //        fnf.printStackTrace();
      return;
    }
    try
    {
      parser.parse(in);
      fs.close();
    }
    catch(Exception e1)
    {
      System.err.println("Parsing " + filename + " threw: " + e1.toString());
      e1.printStackTrace();
    }
    doc = parser.getDocument();
    root = doc.getDocumentElement();


    //{{INIT_CONTROLS
    //}}
  }

  /**
   * Gets the value(s) corresponding to a key string (i.e. the 
   * value(s) for a named parameter.
   * 
   * @param key 'key' is element name.
   * @return Returns a Vector of strings because
   * may have repeated elements
   */
  public Vector get(String key)
  {
    NodeList nl = doc.getElementsByTagName(key);
    Vector result = new Vector();
    if (nl.getLength() < 1)
    {
      return result;
    }
    for (int i = 0; i < nl.getLength(); i++)
    {
      Node cn = nl.item(i).getFirstChild(); // assume 1st child is text node
      if ((cn != null) && (cn.getNodeType() == Node.TEXT_NODE))
      {
        String temp = cn.getNodeValue();
        result.addElement(temp.trim());
      }
    }
    return result;
  }

  /**
   * Gets the value(s) corresponding to a key string (i.e. the 
   * value(s) for a named parameter.
   * 
   * @param key 'key' is element name.
   * @param i zero based index of elements with the name stored in key
   * @return String value of the ith element with name in 'key'
   */


  /*
   * get the ith (zero-based) string correponding to key
   */
  public String get(String key, int i)
  {
    NodeList nl = doc.getElementsByTagName(key);
    String result = null;
    if (nl.getLength() < 1)
    {
      return result;
    }
    if (nl.getLength() < i)
    {
      return result;
    }
    Node cn = nl.item(i).getFirstChild(); // assume 1st child is text node
    if ((cn != null) && (cn.getNodeType() == Node.TEXT_NODE))
    {
      result = (cn.getNodeValue().trim());
    }
    return result;
  }


  /**
   * used to set a value corresponding to 'key'; value is changed
   * in DOM structure in memory
   * 
   * @param key 'key' is element name.
   * @param i index in set of elements with 'key' name
   * @param value new value to be inserted in ith key
   * @return boolean true if the operation succeeded
   */
  public boolean set(String key, int i, String value)
  {
    boolean result = false;
    NodeList nl = doc.getElementsByTagName(key);
    if (nl.getLength() <= i)
    {
      result = false;
      //System.out.println("Error setting XMLConfig value: index too large");
    }
    else
    {
      Node cn = nl.item(i).getFirstChild(); // assumed to be a text node
      if ((cn != null) && (cn.getNodeType() == Node.TEXT_NODE))
      {
        cn.setNodeValue(value);
      }
      result = true;
    }
    return result;
  }

  /**
   * Inserts another node before the first element with
   * the name contained in 'key', otherwise appends it
   * to the end of the config file (last element in root node)
   * 
   * @param key element name which will be duplicated
   * @param value value for new element
   * @return boolean true if the operation succeeded
   */
  public boolean insert(String key, String value)
  {
    boolean result = false;

    // Create the new element, with its text value child
    Node newElem = doc.createElement(key);
    Node newText = doc.createTextNode(value);
    newElem.appendChild(newText);

    // Determine if there are existing elements of the same name
    NodeList nl = doc.getElementsByTagName(key);

    // If so, insert new element before existing
    if (nl.getLength() > 0)
    {
      Node nnn = nl.item(0);
      Node parent = nnn.getParentNode();
      //insert newElem before nnn
      parent.insertBefore(newElem, nnn);
      result = true;
    } 
    // Otherwise, append new element to end of root
    else 
    {
      root.appendChild(newElem);
      result = true;
    }
    return result;
  }

  /**
   * Add a child node to the specified parent
   * 
   * @param parentName name of parent element
   * @param i index of parent element
   * @param childName element name of new child
   * @param value value of new child
   */
  public void addChild(String parentName, int i, String childName, String value)
  {
    NodeList nl = doc.getElementsByTagName(parentName);
    if (nl.getLength() > 0)
    {
      if (nl.getLength() <= i)
      {
        System.out.println("Error setting XMLConfig value: index too large");
      }
      else
      {
        Node parent = nl.item(i);
        Node newElem = doc.createElement(childName);
        Node newText = doc.createTextNode(value);
        //add text to element
        newElem.appendChild(newText);
        //add newElem to parent
        parent.appendChild(newElem);
      }
    }
  }

  /**
   * deletes indicated node
   * 
   * @param nodeName node tag
   * @param i node index
   */
  public void removeNode(String nodeName, int i)
  {
    NodeList nl = doc.getElementsByTagName(nodeName);
    if (nl.getLength() > 0)
    {
      if (nl.getLength() <= i)
      {
        System.out.println("Error setting XMLConfig value: index too large");
      }
      else
      {
        Node nnn = nl.item(i);
        Node parent = nnn.getParentNode();
        parent.removeChild(nnn);
      }
    }
  }


  /**
   * removes all children of the specified parent
   * 
   * @param parentName Name of parent node
   * @param i index of parent node
   */
  public void removeChildren(String parentName, int i)
  {
    NodeList nl = doc.getElementsByTagName(parentName);
    if (nl.getLength() > 0)
    {
      if (nl.getLength() <= i)
      {
        System.out.println("Error setting XMLConfig value: index too large");
      }
      else
      {
        Node parent = nl.item(i);
        NodeList nlchildren = parent.getChildNodes();
        int numchildren = nlchildren.getLength();
        for (int k = 0; k < numchildren; k++)
        {
          Node temp = nlchildren.item(0);
          parent.removeChild(temp);
        }
      }
    }
  }

  /**
   * Assume that there is some parent node which has a subset of
   * child nodes that are repeated e.g. 
   * <parent>
   *    <name>xxx</name>
   *    <value>qqq</value>
   *    <name>yyy</value>
   *    <value>www</value>
   *    ...
   * </parent>
   *
   * this method will return a Hashtable of names-values of parent
   */
  public Hashtable getHashtable(String parentName, String keyName,
                                String valueName)
  {
    String keyval = "";
    String valval = "";
    Hashtable ht = new Hashtable();
    NodeList nl = doc.getElementsByTagName(parentName);
    if (nl.getLength() > 0)
    {
      // always use the first parent
      NodeList children = nl.item(0).getChildNodes();
      if (children.getLength() > 0)
      {
        for (int j = 0; j < children.getLength(); j++)
        {
          Node cn = children.item(j);
          if ((cn.getNodeType() == Node.ELEMENT_NODE)
              && (cn.getNodeName().equalsIgnoreCase(keyName)))
          {
            Node ccn = cn.getFirstChild();        // assumed to be a text node
            if ((ccn != null) && (ccn.getNodeType() == Node.TEXT_NODE))
            {
              keyval = ccn.getNodeValue();
            }
          }
          if ((cn.getNodeType() == Node.ELEMENT_NODE)
              && (cn.getNodeName().equalsIgnoreCase(valueName)))
          {
            Node ccn = cn.getFirstChild();        // assumed to be a text node
            if ((ccn != null) && (ccn.getNodeType() == Node.TEXT_NODE))
            {
              valval = ccn.getNodeValue();
              ht.put(keyval, valval);
            }
          }
        }
      }
    }
    return ht;
  }

  /**
   * Save the configuration file
   */
  public void save()
  {
    saveDOM(root);
  }

  /**
   * This method wraps the 'print' method to send DOM back to the
   * XML document (file) that was used to create the DOM. i.e.
   * this method saves changes to disk
   * 
   * @param nd node (usually the document root)
   */
  public void saveDOM(Node nd)
  {
    try
    {
      out = new PrintWriter(new FileWriter(fileName));
    }
    catch(Exception e)
    {
    }
    out.println("<?xml version=\"1.0\"?>");
    print(nd);
    out.close();
  }

  /**
   * This method can 'print' any DOM subtree. Specifically it is
   * set (by means of 'out') to write the in-memory DOM to the
   * same XML file that was originally read. Action thus saves
   * a new version of the XML doc
   * 
   * @param node node usually set to the 'doc' node for complete XML file
   * re-write
   */
  public void print(Node node)
  {

    // is there anything to do?
    if (node == null)
    {
      return;
    }

    int type = node.getNodeType();
    switch (type)
    {
      // print document
    case Node.DOCUMENT_NODE:
    {

      out.println("<?xml version=\"1.0\"?>");
      print(((Document) node).getDocumentElement());
      out.flush();
      break;
    }

      // print element with attributes
    case Node.ELEMENT_NODE:
    {
      out.print('<');
      out.print(node.getNodeName());
      Attr attrs[] = sortAttributes(node.getAttributes());
      for (int i = 0; i < attrs.length; i++)
      {
        Attr attr = attrs[i];
        out.print(' ');
        out.print(attr.getNodeName());
        out.print("=\"");
        out.print(normalize(attr.getNodeValue()));
        out.print('"');
      }
      out.print('>');
      NodeList children = node.getChildNodes();
      if (children != null)
      {
        int len = children.getLength();
        for (int i = 0; i < len; i++)
        {
          print(children.item(i));
        }
      }
      break;
    }

      // handle entity reference nodes
    case Node.ENTITY_REFERENCE_NODE:
    {
      out.print('&');
      out.print(node.getNodeName());
      out.print(';');

      break;
    }

      // print cdata sections
    case Node.CDATA_SECTION_NODE:
    {
      out.print("<![CDATA[");
      out.print(node.getNodeValue());
      out.print("]]>");

      break;
    }

      // print text
    case Node.TEXT_NODE:
    {
      out.print(normalize(node.getNodeValue()));
      break;
    }

      // print processing instruction
    case Node.PROCESSING_INSTRUCTION_NODE:
    {
      out.print("<?");
      out.print(node.getNodeName());
      String data = node.getNodeValue();
      if (data != null && data.length() > 0)
      {
        out.print(' ');
        out.print(data);
      }
      out.print("?>");
      break;
    }
    }

    if (type == Node.ELEMENT_NODE)
    {
      out.print("</");
      out.print(node.getNodeName());
      out.print('>');
    }

    out.flush();

  } // print(Node)

  /** Returns a sorted list of attributes. */
  protected Attr[] sortAttributes(NamedNodeMap attrs)
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

  /** Normalizes the given string. */
  protected String normalize(String s)
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

  } // normalize(String):String

  //{{DECLARE_CONTROLS
  //}}
}
