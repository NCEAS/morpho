/**
 *  '$RCSfile: DataPackageGUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-12-15 20:28:31 $'
 * '$Revision: 1.98 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.EditingCompleteListener;
import edu.ucsb.nceas.morpho.framework.EditorInterface;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;
import edu.ucsb.nceas.morpho.util.Log;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

/**
 * Class that implements a GUI to edit a data package
 * 
 * Changed class so that it no longer creates GUI components
 * Now used only to get information from DataPackage for display
 * DFH - Feb 2003
 */
public class DataPackageGUI 
{
  public String authorRefLabel = "";
  public String titleRefLabel = "";
  public String accessionRefLabel = "";
  public String keywordsRefLabel = "";
  Morpho morpho;
  private ConfigXML config;
  Container contentPane;
  private DataPackage dataPackage;
  private String location = null;
  private String id = null;
  Hashtable listValueHash = new Hashtable();
  private Hashtable fileAttributes = new Hashtable();
  private static final String htmlBegin = "<html><font color=black>";
  private static final String htmlEnd = "</font></html>";
  
  String wholelabel;
  
  Vector otheritems;
  Vector dataitems;
  public Vector entityitems;

 
  public DataPackageGUI(Morpho morpho, DataPackage dp)
  {
    this.location = dp.getLocation();
    this.id = dp.getID();
    this.dataPackage = dp;
    this.morpho = morpho;
    this.config = morpho.getConfiguration();
    
    initComponents();
  }
  
  /**
   * Creates the panels and hands off tasks to other methods
   */
  private void initComponents()
  {
    //get the xml file attributes from the config file
    fileAttributes = PackageUtil.getConfigFileTypeAttributes(morpho, 
                                                             "xmlfiletype");
    
//    contentPane.setLayout(new FlowLayout());
    Vector orig = new Vector();
    String title = "No Title Provided";
    String altTitle = "No Alternate Title Provided";
    Hashtable docAtts = dataPackage.getAttributes();
    
    Vector entityDoctypeList = config.get("entitydoctype");
    Vector resourceDoctypeList = config.get("resourcedoctype");
    Vector attributeDoctypeList = config.get("attributedoctype");
    
    if(docAtts.containsKey("originator"))
    {
      orig = (Vector)docAtts.get("originator");
    }
    
    if(docAtts.containsKey("title"))
    {
      Vector v = (Vector)docAtts.get("title");
      if(v.size() != 0)
      {
        title = (String)v.elementAt(0);
      }
    }
    
    if(docAtts.containsKey("altTitle"))
    {
      Vector v = (Vector)docAtts.get("altTitle");
      if(v.size() != 0)
      {
        altTitle = (String)v.elementAt(0);
      }
    }
    
    createBasicInfoPanel();
    
    Hashtable relfiles = dataPackage.getRelatedFiles();
    otheritems = new Vector();
    dataitems = new Vector();
    entityitems = new Vector();
    Enumeration keys = relfiles.keys();
    while(keys.hasMoreElements()) 
    { //populate the list box vectors
      String key = (String)keys.nextElement();
      if(key.equals("Data File"))
      {
        Vector v = (Vector)relfiles.get(key);
        for(int i=0; i<v.size(); i++)
        {
          String eleid = (String)v.elementAt(i);
          String s = key + " (" + eleid + ")";
          dataitems.addElement(s);
        }
      }
      else if (vectorContainsString(entityDoctypeList, key))
      {
        Vector v = (Vector)relfiles.get(key);
        String spacecount = "";
        for(int i=0; i<v.size(); i++)
        {
          String eleid = (String)v.elementAt(i);
          String ss = "Entity File (" + eleid + ")";
          File xmlfile;
          try
          {
            if(dataPackage.getLocation().equals(DataPackageInterface.METACAT))
            {
              MetacatDataStore mds = new MetacatDataStore(morpho);
              xmlfile = mds.openFile(eleid);
            }
            else
            {
              FileSystemDataStore fsds = new FileSystemDataStore(morpho);
              xmlfile = fsds.openFile(eleid);
            }
          }
          catch(FileNotFoundException fnfe)
          {
            Log.debug(0, "The file specified was not found.");
            return;
          }
          catch(CacheAccessException cae)
          {
            Log.debug(0, "You do not have proper permissions to write" +
                               " to the cache.");
            return;
          }
          
          String entityNamePath = config.get("entityNamePath", 0);
          
          NodeList nl = PackageUtil.getPathContent(xmlfile, entityNamePath, 
                                                   morpho);
          Node n = null;
          for (int ii=0;ii<nl.getLength();ii++) {
            n = nl.item(ii);
            String s = n.getFirstChild().getNodeValue().trim();
            //System.out.println("node = "+s);
            spacecount += " ";
            entityitems.addElement(s + spacecount);
            listValueHash.put(s + spacecount, eleid);
          }
        }
      }
      else if (!vectorContainsString(resourceDoctypeList, key) &&
               !vectorContainsString(attributeDoctypeList, key))
      {
        Vector v = (Vector)relfiles.get(key);
        for(int i=0; i<v.size(); i++)
        {
          String eleid = (String)v.elementAt(i);
          Hashtable h = (Hashtable)fileAttributes.get(key);
          String displayName = "";
          if ((h==null)||(h.get("displaypath")==null)) {
            displayName = "";
          }
          else {
           displayName = (String)h.get("displaypath");
          }
          if ((displayName == null) || (displayName.indexOf("FIXED:") != -1))
          {  // only substring if displayName != null
            if (displayName == null) {
              otheritems.addElement(eleid.trim());
            } else {
              displayName = displayName.substring(displayName.indexOf(":") + 1,
                                                displayName.length());
              String s = displayName.trim() + " (" + eleid + ")";
              otheritems.addElement(s.trim());
            }
          }
          else
          { //read the file to get the display text from the file
            File f;
            try
            {
              f = PackageUtil.openFile(eleid, morpho);
            }
            catch(Exception e)
            {
              Log.debug(0, "File from package not found: " + 
                                    e.getMessage());
              e.printStackTrace();
              return;
            }
            if (displayName.length()>0) {
              NodeList nl = PackageUtil.getPathContent(f, displayName, morpho);
              for(int j=0; j<nl.getLength(); j++)
              {
                Node n = nl.item(j);
                String nodeContent = n.getFirstChild().getNodeValue();
                String s = nodeContent + " (" + eleid + ")";
                otheritems.addElement(s.trim());
              }
            }
          }
        }
      }
    }
    
    
  }
  
  /**
   * Determine if the string s is contained within the Vector v
   */
  private boolean vectorContainsString(Vector v, String s) {
    boolean foundMatch = false;
    for (int i=0; i < v.size(); i++) {
      if (s.equals((String)v.elementAt(i))) {
        foundMatch = true;
      }
    }
    return foundMatch;
  }
  
  /**
   * Method to get entityitems. In this method, the empty element will remove
   */
  public Vector getEntityitems()
  {
    if (entityitems!= null && entityitems.size() == 1)
    {
      Object obj = entityitems.elementAt(0);
      if (obj instanceof String)
      {
        String str =(String)obj;
        str.trim();
        if ( str.equals("") || str.equals(" ")) 
        {
          return null;
        }//if
      }//if
    }//if
    return entityitems;
  }
  /**
   * creates the basicinfopanel
   */
  public void createBasicInfoPanel()
  {
    String authorList = "";
    
    String idPath = config.get("datasetIdPath", 0);
    String shortNamePath = config.get("datasetShortNamePath", 0);
    String titlePath = config.get("datasetTitlePath", 0);
    String abstractPath = config.get("datasetAbstractPath", 0);
    String keywordPath = config.get("datasetKeywordPath", 0);
    String originatorPath = config.get("datasetOriginatorPath", 0);
    
    
    Document doc = dataPackage.getTripleFileDom();
    
    NodeList idNL = null;
    NodeList shortNameNL = null;
    NodeList titleNL = null;
    NodeList abstractNL = null;
    NodeList keywordNL = null;
    NodeList originatorNL = null;
    
    try
    {
    //get the node lists from the document to fill in the data
      idNL = XPathAPI.selectNodeList(doc, idPath);
      shortNameNL = XPathAPI.selectNodeList(doc, shortNamePath);
      titleNL = XPathAPI.selectNodeList(doc, titlePath);
      abstractNL = XPathAPI.selectNodeList(doc, abstractPath);
      keywordNL = XPathAPI.selectNodeList(doc, keywordPath);
      originatorNL = XPathAPI.selectNodeList(doc, originatorPath);
    }
    catch(Exception e)
    {
      Log.debug(0, "Error selecting nodes from package file.");
      e.printStackTrace();
    }
    
    //get the data from the nodes
    wholelabel = "<html><font color=black>";
    String id = "";
    String shortName = "";
    String title = "";
    String abstractS = "";
    String keywords = ""; 
    
    if(idNL != null && idNL.getLength() != 0)
    {
      id = idNL.item(0).getFirstChild().getNodeValue();
    }
    if(shortNameNL != null && shortNameNL.getLength() != 0)
    {
      shortName = shortNameNL.item(0).getFirstChild().getNodeValue();
    }
    if(titleNL != null)
    {
      title = titleNL.item(0).getFirstChild().getNodeValue();
    }
    if(abstractNL != null && abstractNL.getLength() != 0)
    {
      NodeList children = abstractNL.item(0).getChildNodes();
      for(int i=0; i<children.getLength(); i++)
      {
        Node n = children.item(i);
        if(n.getNodeName().equals("paragraph"))
        {
          String nodeval = getTextValue(n);
//          String nodeval = n.getFirstChild().getNodeValue();
          if(nodeval.trim().equals(""))
          {
            abstractS += "";
          }
          else
          {
            abstractS += "<p>" + getTextValue(n) + "</p>";
//            abstractS += "<p>" + n.getFirstChild().getNodeValue() + "</p>";
          }
        }
      }
    }
    if(keywordNL != null && keywordNL.getLength() != 0)
    {
      for(int i=0; i<keywordNL.getLength(); i++)
      { //get the keywords and concat them into one string
        String keyword = keywordNL.item(i).getFirstChild().getNodeValue();
        keywords += keyword;
        if(i != keywordNL.getLength() - 1)
        {
          keywords += ", ";
        }
      }
    }
    
    wholelabel += htmlize(id, "ID") + htmlize(title, "Title") + 
                  htmlize(shortName, "Short Name") + 
                  htmlize(keywords, "Keywords") + 
                  htmlize(abstractS, "Abstract");
    
    titleRefLabel = title;
    accessionRefLabel = "Accession Number " + id;
    keywordsRefLabel = " Keywords: " + keywords;
    
    String originators = "<br><b>Originator(s)</b><br>";
    String name = "";
    String orgname = "";
    String address = "";
    String phone = "";
    String email = "";
    String web = "";
    String role = "";
    
    for(int i=0; i<originatorNL.getLength(); i++)
    {
      name = "";
      orgname = "";
      address = "";
      phone = "";
      email = "";
      web = "";
      role = "";
        
      Node node = originatorNL.item(i);
      NodeList origChildren = node.getChildNodes();
      for(int k=0; k<origChildren.getLength(); k++)
      {
        Node n = origChildren.item(k);
        String nodename = n.getNodeName().trim();
        if(nodename.equals("individualName"))
        {
          NodeList children = n.getChildNodes();
          String firstName = "";
          String lastName = "";
          for(int j=0; j<children.getLength(); j++)
          {
            if(children.item(j).getNodeName().trim().equals("givenName"))
            {
              firstName = getTextValue(children.item(j));
//              firstName = children.item(j).getFirstChild().getNodeValue().trim();
            }
            else if(children.item(j).getNodeName().trim().equals("surName"))
            {
              lastName = getTextValue(children.item(j));
//              lastName = children.item(j).getFirstChild().getNodeValue().trim();
            }
          }
          name = firstName + " " + lastName;
        }
      }
                     
      authorList = authorList + name + ", ";          
    }
    authorRefLabel = authorList;
    
  }
  
  
  
  /**
   * puts correct html tags on the string provided.  if the string is null
   * or empty it returns an empty string
   * @param s the string to htmlize
   */
  private static String htmlize(String s)
  {
    return htmlize(s, null);
  }
  
  /**
   * puts correct html tags on the string provided.  if the string is null
   * or empty it returns an empty string
   * @param s the string to htmlize
   * @param label the label to add to the string
   */
  private static String htmlize(String s, String label)
  {
    if(s == null || s.trim().equals(""))
    {
      return "";
    }
    else if(label != null)
    {
      return "<b>" + label + "</b>: " + s + "<br>";
    }
    else
    {
      return s + "<br>";
    }
  }
  
    
  
  
  
  
  private String getTextValue(Node nd) {
    String val = "";
    Node child = nd.getFirstChild();
    if (child!=null) {
      val = child.getNodeValue().trim();
    }
    return val;
  }
  
}
