/**
 *  '$RCSfile: AccessionNumber.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-17 01:30:11 $'
 * '$Revision: 1.10 $'
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

import edu.ucsb.nceas.morpho.datapackage.wizard.*;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.XPathAPI;
import edu.ucsb.nceas.morpho.util.Log;
import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
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

import com.arbortext.catalog.*;

/**
 * Class that implements Accession Number utility functions for morpho
 */
public class AccessionNumber 
{
  private Morpho morpho;
  private ConfigXML profile;
  
  public AccessionNumber(Morpho morpho)
  {
    this.morpho = morpho;
    profile = morpho.getProfile();
  }

  /**
   * returns the next local id from the config file
   * returns null if configXML was unable to increment the id number
   */
  public synchronized String getNextId()
  {
    String scope = profile.get("scope", 0);
    String lastidS = profile.get("lastId", 0);
    int lastid = (new Integer(lastidS)).intValue();
    String separator = profile.get("separator", 0);
    
    if(scope.trim().equals("USERNAME"))
    { //this keyword means to use the username for the scope
      String username = profile.get("username", 0);
      scope = username;
    }
    
    String identifier = scope + separator + lastid;
    lastid++;
    String s = "" + lastid;
    if(!profile.set("lastId", 0, s))
    {
      Log.debug(1, "Error incrementing the accession number id");
      return null;
    }
    else
    {
      profile.save();
      return identifier + ".1"; 
    }
  }
  
  /**
   * returns an id with an incremented id.
   */
  public String incRev(String id)
  {
    return incRev(id, true);
  }
  
  /**
   * parses id and adds a revision number to it.  if addOne is true and there is
   * already a revision number then one is added to the existing revision number
   * @param id the id to parse
   * @param addOne true adds one to a existing revision number false does not.
   */
  public String incRev(String id, boolean addOne)
  { 
    String sep = profile.get("separator", 0);
    int count = 0;
    for(int i=0; i<id.length(); i++)
    {
      if(id.charAt(i) == sep.trim().charAt(0))
      {
        count++;
      }
    }
    
    if(count == 1)
    {
      return id + ".1";
    }
    
    int revIndex = id.lastIndexOf(".");
    String revNumStr = id.substring(revIndex + 1, id.length());
    Integer revNum = new Integer(revNumStr);
    int rev = revNum.intValue();
    if(addOne)
    {
      rev++;
    }
    return id.substring(0, revIndex) + "." + rev;
  }
  
  public String incRevInTriples(File xmlfile, String oldid, String newid)
  {
    Vector oldids = new Vector();
    oldids.addElement(oldid);
    Vector newids = new Vector();
    newids.addElement(newid);
    return incRevInTriples(xmlfile, oldids, newids);
  }
  
  /**
   * searches an xml file for triples.  If it finds oldid it increments its
   * revision number with the newid provided.
   * @param xmlfile the xml file that you want to search
   * @param oldid the id that you want incremented.
   * @param newid the id that you want oldid to be replaced with
   * @return returns the newly created triples file with the updated triples
   */
  public String incRevInTriples(File xmlfile, Vector oldid, Vector newid)
  {
    System.out.println("oldid: " + oldid.toString() + " newid: " + newid.toString());
    DocumentBuilder parser = Morpho.createDomParser();
    Document doc;
    InputSource in;
    FileInputStream fs;
    CatalogEntityResolver cer = new CatalogEntityResolver();
    
    //get the DOM rep of the document without triples
    try
    {
      ConfigXML config = morpho.getConfiguration();
      String catalogPath = //config.getConfigDirectory() + File.separator +
                                       config.get("local_catalog_path", 0);
      doc = PackageUtil.getDoc(xmlfile, catalogPath);
    }
    catch (Exception e)
    {
      Log.debug(0, "error parsing " + xmlfile.getPath() + " : " +
                         e.getMessage());
      e.printStackTrace();
      return null;
    }
    
    NodeList tripleList = null;
    String triplePath = "//triple";
    
    try
    {
      //find where the triples go in the file
      tripleList = XPathAPI.selectNodeList(doc, triplePath);
    }
    catch(SAXException se)
    {
      System.err.println("incRevInTriples() : parse threw: " + 
                         se.toString());
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
          if(nodename.equals("SUBJECT") || nodename.equals("OBJECT"))
          {
            String nodeval;
            try
            {
              nodeval = childNode.getFirstChild().getNodeValue().trim();
            }
            catch(NullPointerException npe)
            {
              continue;
            }
            //System.out.println("node found: " + nodeval + " oldid: " + oldid.trim());
            if(/*nodeval.equals(oldid.trim())*/oldid.contains(nodeval.trim()))
            {
              String newidS = "";
              for(int k=0; k<newid.size(); k++)
              {
                newidS = (String)newid.elementAt(k);
                if(nodeval.trim().equals(oldid.elementAt(k)))
                {
                  break;
                }
              }
              System.out.println("replacing: " + nodeval + " with " + newidS);
              childNode.getFirstChild().setNodeValue(newidS);
            }
          }
        }
      }
    }
    
    return PackageUtil.printDoctype(doc) + 
           PackageUtil.print(doc.getDocumentElement());
  }
  
  /**
   * Returns a vector with all components of the accession number.  The vector
   * looks like:
   * [scope, id, rev, separator]
   * ex: [nceas, 5, 2, .]
   * @param id the id to return the parts of
   */
  public Vector getParts(String id)
  {
    String separator = profile.get("separator", 0);
    String scope = id.substring(0, id.indexOf(separator));
    String idpart = id.substring(id.indexOf(separator)+1, 
                                 id.lastIndexOf(separator));
    String rev = id.substring(id.lastIndexOf(separator) + 1, id.length());
    Vector v = new Vector();
    v.addElement(scope);
    v.addElement(idpart);
    v.addElement(rev);
    v.addElement(separator);
    return v;
  }
}
