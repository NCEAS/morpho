/**
 *  '$RCSfile: PackageUtil.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-11-01 18:45:13 $'
 * '$Revision: 1.12 $'
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
  public static NodeList getPathContent(File f, Vector paths, 
                                        ClientFramework framework)
  {
    for(int i=0; i<paths.size(); i++)
    {
      String s = (String)paths.elementAt(i);
      NodeList nl = getPathContent(f, s, framework);
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
   * parses file with the dom parser and returns a dom Document
   * @param file the file to create the document from
   * @param catalogPath the path to the catalog where the files doctype info
   * can be found.
   */
  public static Document getDoc(File file, String catalogPath) throws 
                                                               SAXException, 
                                                               Exception
  {
    DOMParser parser = new DOMParser();
    Document doc;
    InputSource in;
    FileInputStream fs;
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try 
    {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      //ConfigXML config = framework.getConfiguration();
      //String catalogPath = config.get("local_catalog_path", 0);
      myCatalog.parseCatalog(catalogPath);
      cer.setCatalog(myCatalog);
    } 
    catch (Exception e) 
    {
      ClientFramework.debug(11, "Problem creating Catalog in " +
                   "packagewizardshell.handleFinishAction!" + e.toString());
      throw new Exception(e.getMessage());
    }
    
    parser.setEntityResolver(cer);
    
    try
    { //parse the wizard created file without the triples
      fs = new FileInputStream(file);
      in = new InputSource(fs);
    }
    catch(FileNotFoundException fnf)
    {
      fnf.printStackTrace();
      throw new Exception(fnf.getMessage());
    }
    try
    {
      parser.parse(in);
      fs.close();
    }
    catch(Exception e1)
    {
      throw new Exception(e1.getMessage());
    }
    //get the DOM rep of the document without triples
    doc = parser.getDocument();
    
    return doc;
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

  }

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
  
  /**
   * opens a file on metacat or local.  It defaults to local if it is on both.
   * @param name the name of the file
   * @param framework the framework object that is currently running.
   */
  public static File openFile(String name, ClientFramework framework) 
                                                  throws FileNotFoundException,
                                                         CacheAccessException
  {
    return openFile(name, null, framework);
  }
  
  /**
   * figures out a files location if it is not known and opens it.  the default
   * is to open it from the local system, if it is in both places.  
   * @param name the file to open
   * @param location the location of the file.  set to null if the location is 
   * unknown
   * @param framework the framework object that is currently running.
   */
  public static File openFile(String name, String location, 
                              ClientFramework framework) throws 
                                                         FileNotFoundException,
                                                         CacheAccessException
  {
    File f;
    if(location == null)
    {
      try
      {
        FileSystemDataStore fsds = new FileSystemDataStore(framework);
        f = fsds.openFile(name);
        return f;
      }
      catch(FileNotFoundException fnfe)
      {
        try
        {
          MetacatDataStore mds = new MetacatDataStore(framework);
          f = mds.openFile(name);
          return f;
        }
        catch(FileNotFoundException fnfe2)
        {
          throw new FileNotFoundException(fnfe2.getMessage());
        }
        catch(CacheAccessException cae2)
        {
          throw new CacheAccessException(cae2.getMessage());
        }
      }
    }
    else
    {
      try
      {
        if(location.equals(DataPackage.LOCAL) || 
           location.equals(DataPackage.BOTH))
        {
          FileSystemDataStore fsds = new FileSystemDataStore(framework);
          f = fsds.openFile(name);
          return f;
        }
        else
        {
          MetacatDataStore mds = new MetacatDataStore(framework);
          f = mds.openFile(name);
          return f;
        }
      }
      catch(FileNotFoundException fnfe3)
      {
        throw new FileNotFoundException(fnfe3.getMessage());
      }
      catch(CacheAccessException cae3)
      {
        throw new CacheAccessException(cae3.getMessage());
      }
    }
  }
  
  /**
   * gets the editor context and returns it
   */
  public static EditorInterface getEditor(ClientFramework framework)
  {
    try
    {
      EditorInterface editor;
      ServiceProvider provider = 
                      framework.getServiceProvider(EditorInterface.class);
      editor = (EditorInterface)provider;
      return editor;
    }
    catch(ServiceNotHandledException snhe)
    {
      framework.debug(0, "Could not capture the editor in PackageUtil." +
                         "getEditor(): " + snhe.getMessage());
      return null;
    }
  }
  
  /**
   * returns the string representation of a file
   */
  public static String getStringFromFile(File xmlFile)
  {
    StringBuffer sb = new StringBuffer();
    try
    {
      FileReader fr = new FileReader(xmlFile);
      int c = fr.read();
      while(c != -1)
      {
        sb.append((char)c);
        c = fr.read();
      }
//DFH      sb.append((char)c);
      fr.close();
      return sb.toString();
    }
    catch(Exception e)
    {
      ClientFramework.debug(0, "Error reading file in PackageUtil." +
                               "getStringFromFile(): " + e.getMessage());
      return null;
    }
  }
  
  /**
   * method to add a collection of triples to a triples file.  this method
   * searches for any triples already in the file and appends the new
   * ones after the existing ones.  
   * @param triples the collection of triples to add
   * @param dataPackage the package that you want to add the triples to
   * @param framework the framework object that is currently running.
   */
  public static String addTriplesToTriplesFile(TripleCollection triples,
                                               DataPackage dataPackage, 
                                               ClientFramework framework)
  {
    String triplesTag = framework.getConfiguration().get("triplesTag", 0);
    File packageFile = dataPackage.getTriplesFile();
    Document doc;
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
      ClientFramework.debug(9, "Problem creating Catalog in " +
                   "PackageUtil.updateTriplesFile" + 
                   e.toString());
    }
    
    parser.setEntityResolver(cer);
    
    try
    { //parse the wizard created file with existing triples
      fs = new FileInputStream(packageFile);
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
      System.err.println("File: " + packageFile.getPath() + " : parse threw: " + 
                         e1.toString());
    }
    //get the DOM rep of the document with existing triples
    doc = parser.getDocument();
    NodeList tripleNodeList = triples.getNodeList();
    NodeList docTriplesNodeList = null;
    
    try
    {
      //find where the triples go in the file
      docTriplesNodeList = XPathAPI.selectNodeList(doc, triplesTag);
    }
    catch(SAXException se)
    {
      System.err.println("file: " + packageFile.getPath() + " : parse threw: " + 
                         se.toString());
    }
    
    Node docNode = doc.getDocumentElement();
    for(int j=0; j<tripleNodeList.getLength(); j++)
    { //add the triples to the appropriate position in the file
      Node n = doc.importNode(tripleNodeList.item(j), true);
      int end = docTriplesNodeList.getLength() - 1;
      Node triplesNode = docTriplesNodeList.item(end);
      Node parent = triplesNode.getParentNode();
      if(triplesNode.getNextSibling() == null)
      {
        parent.appendChild(n);
      }
      else
      {
        parent.insertBefore(n, triplesNode.getNextSibling());
      }
    }
    
    String docString = PackageUtil.printDoctype(doc);
    docString += PackageUtil.print(doc.getDocumentElement());
    return docString;
  }
  
  /**
   * method to delete triples with a specified string from the triples file 
   * @param searchstring the string to search for in the triples.  when this
   * string is found the entire triple to which it belongs is deleted.
   * @param dataPackage the package that you want to delete the triples from
   * @param framework the framework object that is currently running.
   */
  public static String deleteTriplesInTriplesFile(String searchstring,
                                                  DataPackage dataPackage, 
                                                  ClientFramework framework)
  {
    String triplesTag = framework.getConfiguration().get("triplesTag", 0);
    File packageFile = dataPackage.getTriplesFile();
    Document doc;
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
      ClientFramework.debug(9, "Problem creating Catalog in " +
                   "PackageUtil.updateTriplesFile" + 
                   e.toString());
    }
    
    parser.setEntityResolver(cer);
    
    try
    { //parse the wizard created file with existing triples
      fs = new FileInputStream(packageFile);
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
      System.err.println("File: " + packageFile.getPath() + " : parse threw: " + 
                         e1.toString());
    }
    //get the DOM rep of the document with existing triples
    doc = parser.getDocument();
    NodeList docTriplesNodeList = null;
    
    try
    {
      //find all the triples
      docTriplesNodeList = XPathAPI.selectNodeList(doc, triplesTag);
    }
    catch(SAXException se)
    {
      System.err.println("file: " + packageFile.getPath() + " : parse threw: " + 
                         se.toString());
    }
    
    for(int i=0; i<docTriplesNodeList.getLength(); i++)
    {
      Node triple = docTriplesNodeList.item(i);   //the triple node
      Node parent = triple.getParentNode();       //the triples parent
      NodeList children = triple.getChildNodes(); //the triples children
      
      for(int j=0; j<children.getLength(); j++)
      {
        Node n = children.item(j);
        if(n.getNodeName().equals("subject") ||
           n.getNodeName().equals("object"))
        {
          Node o = n.getFirstChild();
          if(o.getNodeValue().trim().equals(searchstring))
          { //found a match delete the triple
            NodeList countTriples = null;
            try
            {
              countTriples = XPathAPI.selectNodeList(doc, triplesTag);
            }
            catch(SAXException se2)
            {
              System.err.println("file: " + packageFile.getPath() + 
                                 " : parse threw: " + 
                                 se2.toString());
            }
            
            if(countTriples.getLength() == 1)
            { //this is the last triple. if it is a match we don't want to 
              //delete the last one, just the content of the s, r and o
              for(int k=0; k<children.getLength(); k++)
              {
                Node lastchild = children.item(k);
                if(lastchild.getNodeName().equals("subject") || 
                   lastchild.getNodeName().equals("relationship") ||
                   lastchild.getNodeName().equals("object"))
                {
                  lastchild.getFirstChild().setNodeValue(" ");
                }
              }
            }
            else
            {
              parent.removeChild(triple);
            }
          }
        }
      }
    }
    
    String docString = PackageUtil.printDoctype(doc);
    docString += PackageUtil.print(doc.getDocumentElement());
    return docString;
  }
  
  /**
   * gets the file types from the config file and hashes them by a specified
   * attribute
   * @param framework the client framework that is currently active
   * @param hashby a key from the attributes to hash the table by.  note
   * that this must be one of the required fields or else the hashtable 
   * will try to hash values to null
   */
  public static Hashtable getConfigFileTypeAttributes(ClientFramework framework,
                                                      String hashby)
  {
    Hashtable returnhash = new Hashtable();
    NodeList filetypes = 
          framework.getConfiguration().getPathContent("//newxmlfiletypes/file");
    for(int i=0; i<filetypes.getLength(); i++)
    {
      Node n = filetypes.item(i);
      NodeList children = n.getChildNodes();
      Hashtable h = new Hashtable();
      for(int j=0; j<children.getLength(); j++)
      {
        Node n2 = children.item(j);
        String nodename = n2.getNodeName();
        if(nodename.equals("label"))
        {
          h.put("label", n2.getFirstChild().getNodeValue());
        }
        else if(nodename.equals("xmlfiletype"))
        {
          h.put("xmlfiletype", n2.getFirstChild().getNodeValue());
        }
        else if(nodename.equals("tooltip"))
        {
          h.put("tooltip", n2.getFirstChild().getNodeValue());
        }
        else if(nodename.equals("name"))
        {
          h.put("name", n2.getFirstChild().getNodeValue());
        }
        else if(nodename.equals("relatedto"))
        {
          h.put("relatedto", n2.getFirstChild().getNodeValue());
        }
        else if(nodename.equals("rootnode"))
        {
          h.put("rootnode", n2.getFirstChild().getNodeValue());
        }
        else if(nodename.equals("displaypath"))
        {
          h.put("displaypath", n2.getFirstChild().getNodeValue());
        }
        else if(nodename.equals("editexisting"))
        {
          h.put("editexisting", n2.getFirstChild().getNodeValue());
        }
        else if(nodename.equals("visible"))
        {
          h.put("visible", n2.getFirstChild().getNodeValue());
        }
        else if(nodename.equals("idpath"))
        {
          h.put("idpath", n2.getFirstChild().getNodeValue());
        }
      }
      returnhash.put(h.get(hashby), h);
    }
    return returnhash;
  }
}
