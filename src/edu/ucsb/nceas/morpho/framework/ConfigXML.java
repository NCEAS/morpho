/**
 *  '$RCSfile: ConfigXML.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2008-06-17 16:19:26 $'
 * '$Revision: 1.19 $'
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.utilities.OrderedMap;


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
 */
public class ConfigXML
{

  /**
   * root node of the in-memory DOM structure
   */
  private Node root;

  /**
   * Document node of the in-memory DOM structure
   */
  private Document doc;

  /**
   * XML file name in string form
   */
  private String fileName;

  /**
   * Print writer (output)
   */
  private Writer out;

  private static String configDirectory = ".morpho";

  /**
   * String passed to the creator is the XML config file name
   *
   * @param filename name of XML file
   */
  public ConfigXML(String filename) throws FileNotFoundException
  {
    this.fileName = filename;

    DocumentBuilder parser = Morpho.createDomParser();
    InputSource in;
    FileInputStream fs;
    fs = new FileInputStream(filename);
    in = new InputSource(fs);

    try
    {
      doc = parser.parse(in);
      fs.close();
    } catch(Exception e1) {
      Log.debug(4, "Parsing " + filename + " threw: " +
                            e1.toString());
      e1.printStackTrace();
    }
    root = doc.getDocumentElement();
  }

   /**
   * String passed to the creator is the XML config file name
   *
   * @param input stream containing the XML configuration data
   */
  public ConfigXML(InputStream configStream) throws FileNotFoundException
  {
    DocumentBuilder parser = Morpho.createDomParser();
    InputSource in;
    in = new InputSource(configStream);

    try
    {
      doc = parser.parse(in);
      configStream.close();
    } catch(Exception e1) {
      Log.debug(4, "Parsing config file threw: " +
                            e1.toString());
      e1.printStackTrace();
    }
    root = doc.getDocumentElement();
  }

  /**
   * Set up a DOM parser for reading an XML document
   *
   * @return a DOM parser object for parsing
   */
  private static DocumentBuilder createDomParser() throws Exception
  {
    DocumentBuilder parser = null;

    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      parser = factory.newDocumentBuilder();
      if (parser == null)
      {
        throw new Exception("Could not create Document parser in " +
                            "MonarchUtil.DocumentBuilder");
      }
    }
    catch (ParserConfigurationException pce)
    {
      throw new Exception("Could not create Document parser in " +
                            "MonarchUtil.DocumentBuilder: " + pce.getMessage());
    }

    return parser;
  }

  /**
   * Gets the value(s) corresponding to a key string (i.e. the
   * value(s) for a named parameter.
   *
   * @param key 'key' is element name.
   * @return Returns a Vector of strings because may have repeated elements
   */
  public Vector<String> get(String key)
  {
    NodeList nl = doc.getElementsByTagName(key);
    Vector<String> result = new Vector<String>();
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
   *  Gets the document for this DOM
   */
  public Document getDocument() {
    return doc;
  }

  /**
   *  Gets the root Node for this DOM
   */
  public Node getRoot() {
    return root;
  }

  /**
   * Gets the value(s) corresponding to a key string (i.e. the
   * value(s) for a named parameter.
   *
   * @param key 'key' is element name.
   * @param i zero based index of elements with the name stored in key
   * @return String value of the ith element with name in 'key'
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
    if ((cn != null) && (cn.getNodeType() == Node.TEXT_NODE) && cn.getNodeValue() != null)
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
	  return set(key, i, value, false);
  }
  
  /**
   * used to set a value corresponding to 'key'; value is changed
   * in DOM structure in memory
   *
   * @param key 'key' is element name.
   * @param i index in set of elements with 'key' name
   * @param value new value to be inserted in ith key
   * @param insertMissing will add the property if it does not exist
   * @return boolean true if the operation succeeded
   */
  public boolean set(String key, int i, String value, boolean insertMissing)
  {
    boolean result = false;
    NodeList nl = doc.getElementsByTagName(key);
    if (nl.getLength() <= i) {
    	// add the property if it is not present already and we are instructed to
    	if (insertMissing) {
    		result = insert(key, value);
    	} else {
    		result = false;
    	}
    } else {
      Node cn = nl.item(i).getFirstChild(); // assumed to be a text node
      if (cn == null) {
        // No text node, so append one with the value
        Node newText = doc.createTextNode(value);
        nl.item(i).appendChild(newText);
      } else if (cn.getNodeType() == Node.TEXT_NODE) {
        // found the text node, so change its value
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
    if (nl.getLength() > 0) {
      Node nnn = nl.item(0);
      Node parent = nnn.getParentNode();
      //insert newElem before nnn
      parent.insertBefore(newElem, nnn);
      result = true;

    // Otherwise, append new element to end of root
    } else {
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
        Log.debug(7, "Error setting XMLConfig value: " +
                                 "index too large");
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
        Log.debug(7, "Error setting XMLConfig value: " +
                                 "index too large");
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
        Log.debug(7, "Error setting XMLConfig value: " +
                                 "index too large");
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
  public Map<String, String> getHashtable(String parentName, String keyName,
                                String valueName)
  {
    String keyval = "";
    String valval = "";
    Map<String, String> ht = new LinkedHashMap<String, String>();
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
 * @throws IOException 
   */
  public void save()
  {
    try {
		saveDOM(root);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }

  /**
   * This method wraps the 'print' method to send DOM back to the
   * XML document (file) that was used to create the DOM. i.e.
   * this method saves changes to disk
   *
   * @param nd node (usually the document root)
   */
  public void saveDOM(Node nd) throws IOException
  {
    File outfile = new File(fileName);
   if (!outfile.canWrite()) {
  Log.debug(5, "Cannot Save configuration information to "+fileName+ " !");
    }
   else {
    try
    {
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), Charset.forName("UTF-8")));
    }
    catch(Exception e)
    {
    }
    out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    print(nd, out);
    out.close();
   }
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
  public void print(Node node, Writer out) throws IOException
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

      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?\n>");
      print(((Document) node).getDocumentElement(), out);
      out.flush();
      break;
    }

      // print element with attributes
    case Node.ELEMENT_NODE:
    {
      out.write('<');
      out.write(node.getNodeName());
      Attr attrs[] = sortAttributes(node.getAttributes());
      for (int i = 0; i < attrs.length; i++)
      {
        Attr attr = attrs[i];
        out.write(' ');
        out.write(attr.getNodeName());
        out.write("=\"");
        out.write(XMLUtil.normalize(attr.getNodeValue()));
        out.write('"');
      }
      out.write('>');
      NodeList children = node.getChildNodes();
      if (children != null)
      {
        int len = children.getLength();
        for (int i = 0; i < len; i++)
        {
          print(children.item(i), out);
        }
      }
      break;
    }

      // handle entity reference nodes
    case Node.ENTITY_REFERENCE_NODE:
    {
      out.write('&');
      out.write(node.getNodeName());
      out.write(';');

      break;
    }

      // print cdata sections
    case Node.CDATA_SECTION_NODE:
    {
      out.write("<![CDATA[");
      out.write(node.getNodeValue());
      out.write("]]>");

      break;
    }

      // print text
    case Node.TEXT_NODE:
    {
      out.write(XMLUtil.normalize(node.getNodeValue()));
      break;
    }

      // print processing instruction
    case Node.PROCESSING_INSTRUCTION_NODE:
    {
      out.write("<?");
      out.write(node.getNodeName());
      String data = node.getNodeValue();
      if (data != null && data.length() > 0)
      {
        out.write(' ');
        out.write(data);
      }
      out.write("?>");
      break;
    }
    }

    if (type == Node.ELEMENT_NODE)
    {
      out.write("</");
      out.write(node.getNodeName());
      out.write(">\n");
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


  /*
   *  utility routine to return the value(s) of a node defined by
   *  a specified XPath
   */
  public Vector<String> getValuesForPath(String pathstring) {
    Vector<String> val = new Vector<String>();
    if (!pathstring.startsWith("/")) {
      pathstring = "//*/"+pathstring;
    }
    try{
      NodeList nl = null;
      nl = XPathAPI.selectNodeList(doc, pathstring);
      if ((nl!=null)&&(nl.getLength()>0)) {
        // loop over node list is needed if node is repeated
        for (int k=0;k<nl.getLength();k++) {
          Node cn = nl.item(k).getFirstChild();  // assume 1st child is text node
          if ((cn!=null)&&(cn.getNodeType()==Node.TEXT_NODE)) {
            String temp = cn.getNodeValue().trim();
            val.addElement(temp);
          }
        }
      }
    } catch (Exception e) {
      Log.debug(4, "Error in getValueForPath method");
    }
    return val;
  }

    /**
   * gets the content of a tag in a given xml file with the given path
   * @param path the path to get the content from
   */
  public NodeList getPathContent(String path)
  {
    try
    {
      NodeList docNodeList = XPathAPI.selectNodeList(doc, path);
      return docNodeList;
    }
    catch(TransformerException se)
    {
      System.err.println(se.toString());
      return null;
    }
  }

  /**
   * Determine the home directory in which configuration files should be located
   *
   * @returns String name of the path to the configuration directory
   */
  public static String getConfigDirectory() {
    return System.getProperty("user.home") + File.separator + configDirectory;
  }
  
  public static void setConfigDirectory(String dir) {
	  configDirectory = dir;
  }

  /**
   * sorts records in the config.xml file based on the passed stylesheet
   * @param xsltStylesheet the stylesheet to sort the config file
   * @throws Exception
   */
  public void sort(String xsltStylesheet) throws Exception
  {
    StringWriter resultWriter = new StringWriter();
    String result;

    //create the transformer
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer(
      new StreamSource(new StringReader(xsltStylesheet)));
    transformer.transform(new DOMSource(root),
      new StreamResult(resultWriter));
    result = resultWriter.toString();
    //write out the dom with the new order
    DocumentBuilder parser = createDomParser();
    InputSource in  = new InputSource(new StringReader(result));
    doc = parser.parse(in);
    //reassign root to the new order
    root = doc.getDocumentElement();
  }

  /**
   * returns the config file as a string
   */
  public String toString()
  {
    StringWriter sw = new StringWriter();
	try {
		print(root, new BufferedWriter(sw));
	} catch (IOException e) {
		e.printStackTrace();
	}
    return sw.toString();
  }
}
