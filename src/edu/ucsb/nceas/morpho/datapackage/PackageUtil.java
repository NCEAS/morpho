/**
 *  '$RCSfile: PackageUtil.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-12-15 20:28:31 $'
 * '$Revision: 1.26 $'
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.arbortext.catalog.Catalog;
import com.arbortext.catalog.CatalogEntityResolver;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierManager;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.EditorInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.query.LocalQuery;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.XMLUtil;

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
                                        Morpho morpho)
  {
    for(int i=0; i<paths.size(); i++)
    {
      String s = (String)paths.elementAt(i);
      NodeList nl = getPathContent(f, s, morpho);
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
   * @param morpho a morpho object that has a valid config file
   */
  public static NodeList getPathContent(File f, String path, 
                                        Morpho morpho)
  {
    Document doc;
    try{
      doc = getDoc(f, morpho);
    }
    catch(Exception e1) {
          System.err.println("File: " + f.getPath() + " : parse threw (1): " + 
                         e1.toString());
      return null;    }
    
    try
    {
      long start_time_xpath = System.currentTimeMillis();
      NodeList docNodeList = XPathAPI.selectNodeList(doc, path);
      long stop_time = System.currentTimeMillis();
 //     Log.debug(10,"Time for prenode search: "+(start_time_xpath-start_time));
 //     Log.debug(10,"Time for nodesearch: "+(stop_time-start_time_xpath));
      return docNodeList;
    }
    catch(TransformerException se)
    {
      System.err.println("File: " + f.getPath() + " : parse threw (2): " + 
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
    long start_time = System.currentTimeMillis();
    String fileName = file.getName();
    String parent = file.getParent();
    int lastsep = parent.lastIndexOf(File.separator);
    parent = parent.substring(lastsep+1);
    fileName = parent + IdentifierManager.DOT + fileName;
    if (LocalQuery.dom_collection.containsKey(fileName)) {
      return ((Document)LocalQuery.dom_collection.get(fileName));
    }
            
    DocumentBuilder parser = Morpho.createDomParser();
    Document doc;
    InputSource in;
//    FileInputStream fs;
    Reader fs;
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try 
    {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      //ConfigXML config = morpho.getConfiguration();
      //String catalogPath = config.getConfigDirectory() + File.separator +
                                        //config.get("local_catalog_path", 0);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL catalogURL = cl.getResource(catalogPath);
        
      myCatalog.parseCatalog(catalogURL.toString());
      //myCatalog.parseCatalog(catalogPath);
      cer.setCatalog(myCatalog);
    } 
    catch (Exception e) 
    {
      Log.debug(11, "Problem creating Catalog in " +
                   "packagewizardshell.handleFinishAction!" + e.toString());
      throw new Exception(e.getMessage());
    }
    
    parser.setEntityResolver(cer);
    
    try
    { //parse the wizard created file without the triples
//      fs = new FileInputStream(file);
      fs = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
      in = new InputSource(fs);
    }
    catch(FileNotFoundException fnf)
    {
      fnf.printStackTrace();
      throw new Exception(fnf.getMessage());
    }
    try
    {
      doc = parser.parse(in);
      fs.close();
    }
    catch(Exception e1)
    {
      throw new Exception(e1.getMessage());
    }
    long stop_time = System.currentTimeMillis();
    Log.debug(10,"Time for getDoc: "+(stop_time-start_time));

    return doc;
  }
 

  /**
   * parses file with the dom parser and returns a dom Document
   * @param file the file to create the document from
   * @param morpho the top level Morpho class
   */
  public static Document getDoc(File file, Morpho morpho) throws 
                                                               SAXException, 
                                                               Exception
  {
    DocumentBuilder parser = Morpho.createDomParser();
    Document doc;
    InputSource in;
//    FileInputStream fs;
    Reader fs;
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try 
    {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      ConfigXML config = morpho.getConfiguration();
      String catalogPath = config.get("local_catalog_path", 0);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL catalogURL = cl.getResource(catalogPath);
        
      myCatalog.parseCatalog(catalogURL.toString());
      //myCatalog.parseCatalog(catalogPath);
      cer.setCatalog(myCatalog);
    } 
    catch (Exception e) 
    {
      Log.debug(11, "Problem creating Catalog in " +
                   "packagewizardshell.handleFinishAction!" + e.toString());
      throw new Exception(e.getMessage());
    }
    
    parser.setEntityResolver(cer);
    
    try
    { //parse the wizard created file without the triples
//      fs = new FileInputStream(file);
      fs = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
      in = new InputSource(fs);
    }
    catch(FileNotFoundException fnf)
    {
      fnf.printStackTrace();
      throw new Exception(fnf.getMessage());
    }
    try
    {
      doc = parser.parse(in);
      fs.close();
    }
    catch(Exception e1)
    {
      throw new Exception(e1.getMessage());
    }
    
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

      sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
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
        sb.append(XMLUtil.normalize(attr.getNodeValue()));
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
      sb.append(XMLUtil.normalize(node.getNodeValue()));
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

  
  /** 
   * prints out the doctype part of and xml document.  this can be appended to 
   * the output from print().
   * @param doc the dom of the document to print the doctype for
  */
  public static String printDoctype(Document doc)
  {
    DocumentType dt = doc.getDoctype();
    String publicid = dt.getPublicId();
    String systemid = dt.getSystemId();
    String nameid = dt.getName();
    String docString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    docString += "\n<!DOCTYPE " + nameid +  " PUBLIC \"" + publicid + 
                 "\" \"" + systemid + "\">\n";
    return docString;
  }
  
  /**
   * gets the editor context and returns it
   */
  public static EditorInterface getEditor(Morpho morpho)
  {
    try
    {
      ServiceController services = ServiceController.getInstance();
      EditorInterface editor;
      ServiceProvider provider = 
                      services.getServiceProvider(EditorInterface.class);
      editor = (EditorInterface)provider;
      return editor;
    }
    catch(ServiceNotHandledException snhe)
    {
      Log.debug(0, "Could not capture the editor in PackageUtil." +
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
      Reader fr = new InputStreamReader(new FileInputStream(xmlFile), Charset.forName("UTF-8"));
      int c = fr.read();
        while(c != -1)
      {
        sb.append((char)c);
        c = fr.read();
      }
      fr.close();
      return sb.toString();
    }
    catch(Exception e)
    {
      Log.debug(0, "Error reading file in PackageUtil." +
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
   * @param morpho the morpho object that is currently running.
   */
  /*public static String addTriplesToTriplesFile(TripleCollection triples,
                                               DataPackage dataPackage, 
                                               Morpho morpho)
  {
    String triplesTag = morpho.getConfiguration().get("triplesTag", 0);
    File packageFile = dataPackage.getTriplesFile();
    Document doc = null;
    DocumentBuilder parser = Morpho.createDomParser();
    InputSource in;
    FileInputStream fs;
    
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try 
    {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      ConfigXML config = morpho.getConfiguration();
      String catalogPath = //config.getConfigDirectory() + File.separator +
                                       config.get("local_catalog_path", 0);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL catalogURL = cl.getResource(catalogPath);
        
      myCatalog.parseCatalog(catalogURL.toString());
      //myCatalog.parseCatalog(catalogPath);
      cer.setCatalog(myCatalog);
    } 
    catch (Exception e) 
    {
      Log.debug(9, "Problem creating Catalog in " +
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
      doc = parser.parse(in);
      fs.close();
    }
    catch(Exception e1)
    {
      System.err.println("File: " + packageFile.getPath() + " : parse threw (3): " + 
                         e1.toString());
    }
    //get the DOM rep of the document with existing triples
    NodeList tripleNodeList = triples.getNodeList();
    NodeList docTriplesNodeList = null;
    
    try
    {
      //find where the triples go in the file
      docTriplesNodeList = XPathAPI.selectNodeList(doc, triplesTag);
    }
    catch(TransformerException se)
    {
      System.err.println("File: " + packageFile.getPath() + " : parse threw (4): " + 
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
  }*/

  /**
   * method to add a collection of triples to a triples string.  this method
   * searches for any triples already in the string and appends the new
   * ones after the existing ones.  
   * @param triples the collection of triples to add
   * @param dataPackageString the package that you want to add the triples to
   * @param morpho the morpho object that is currently running.
   */
  public static String addTriplesToTriplesString(TripleCollection triples,
                                               String dataPackageString, 
                                               Morpho morpho)
  {
    String triplesTag = morpho.getConfiguration().get("triplesTag", 0);
    Document doc = null;
    DocumentBuilder parser = Morpho.createDomParser();
    InputSource in;
    StringReader sr;
    
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try 
    {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      ConfigXML config = morpho.getConfiguration();
      String catalogPath = //config.getConfigDirectory() + File.separator +
                                       config.get("local_catalog_path", 0);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL catalogURL = cl.getResource(catalogPath);
        
      myCatalog.parseCatalog(catalogURL.toString());
      //myCatalog.parseCatalog(catalogPath);
      cer.setCatalog(myCatalog);
    } 
    catch (Exception e) 
    {
      Log.debug(9, "Problem creating Catalog in " +
                   "PackageUtil.updateTriplesFile" + 
                   e.toString());
    }
    
    parser.setEntityResolver(cer);
    
    try
    { //parse the wizard created file with existing triples
      sr = new StringReader(dataPackageString);
      in = new InputSource(sr);
    }
    catch(Exception fnf)
    {
      fnf.printStackTrace();
      return null;
    }
    try
    {
      doc = parser.parse(in);
      sr.close();
    }
    catch(Exception e1)
    {
      System.err.println(e1.toString());
    }
    //get the DOM rep of the document with existing triples
    NodeList tripleNodeList = triples.getNodeList();
    NodeList docTriplesNodeList = null;
    
    try
    {
      //find where the triples go in the file
      docTriplesNodeList = XPathAPI.selectNodeList(doc, triplesTag);
    }
    catch(TransformerException se)
    {
      System.err.println(se.toString());
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
   * @param morpho the morpho object that is currently running.
   */
  /*public static String deleteTriplesInTriplesFile(String searchstring,
                                                  DataPackage dataPackage, 
                                                  Morpho morpho)
  {
    String triplesTag = morpho.getConfiguration().get("triplesTag", 0);
    File packageFile = dataPackage.getTriplesFile();
    Document doc = null;
    DocumentBuilder parser = Morpho.createDomParser();
    InputSource in;
    FileInputStream fs;
    
    CatalogEntityResolver cer = new CatalogEntityResolver();
    try 
    {
      Catalog myCatalog = new Catalog();
      myCatalog.loadSystemCatalogs();
      ConfigXML config = morpho.getConfiguration();
      String catalogPath = //config.getConfigDirectory() + File.separator +
                                       config.get("local_catalog_path", 0);
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL catalogURL = cl.getResource(catalogPath);
        
      myCatalog.parseCatalog(catalogURL.toString());
      //myCatalog.parseCatalog(catalogPath);
      cer.setCatalog(myCatalog);
    } 
    catch (Exception e) 
    {
      Log.debug(9, "Problem creating Catalog in " +
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
      doc = parser.parse(in);
      fs.close();
    }
    catch(Exception e1)
    {
      System.err.println("File: " + packageFile.getPath() + " : parse threw (5): " + 
                         e1.toString());
    }
    NodeList docTriplesNodeList = null;
    
    try
    {
      //find all the triples
      docTriplesNodeList = XPathAPI.selectNodeList(doc, triplesTag);
    }
    catch(TransformerException se)
    {
      System.err.println("File: " + packageFile.getPath() + " : parse threw (6): " + 
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
            catch(TransformerException se2)
            {
              System.err.println("File: " + packageFile.getPath() + 
                                 " : parse threw (7): " + 
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
  }*/
  
  /**
   * gets the file types from the config file and hashes them by a specified
   * attribute
   * @param morpho the client morpho that is currently active
   * @param hashby a key from the attributes to hash the table by.  note
   * that this must be one of the required fields or else the hashtable 
   * will try to hash values to null
   */
  public static Hashtable getConfigFileTypeAttributes(Morpho morpho,
                                                      String hashby)
  {
    Hashtable returnhash = new Hashtable();
    NodeList filetypes = 
          morpho.getConfiguration().getPathContent("//newxmlfiletypes/file");
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
  
  //-----------------------------------------
  /**
   * Save the DOM doc as a file
   */
  static public void save(String filename, Document doc, String doctype, Morpho morpho)
  {
    saveDOM(filename, doc, doctype, morpho);
  }

  /**
   * This method wraps the 'print' method to send DOM back to the
   * XML document (file) that was used to create the DOM. i.e.
   * this method saves changes to disk
   * 
   * @param nd node (usually the document root)
   */
  static public void saveDOM(String fileName, Document doc, String doctype, Morpho morpho)
  { 
    Writer out = null;
    Node nd = doc.getDocumentElement();
    File outfile = new File(fileName);
    try
    {
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), Charset.forName("UTF-8")));
      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      String dt = doctype;
      if (doctype==null) dt = "";
      out.write(dt);
      print(nd, out);
      out.close(); 
    }
    catch(Exception e) {
    	e.printStackTrace();
    }
  }

  /**
   * This method can 'print' any DOM subtree. Specifically it is
   * set (by means of 'out') to write the in-memory DOM  
   * Action thus saves a new version of the XML doc
   * 
   * @param node node usually set to the 'doc' node for complete XML file
   * re-write
   */
  static private void print(Node node, Writer out) throws IOException
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

      out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
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
      out.write('>');
    }

    out.flush();

  } // print(Node)


  
  
}
