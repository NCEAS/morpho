/**
 *  '$RCSfile: MetadataObject.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-08-07 17:37:34 $'
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

import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;

import org.xml.sax.InputSource;

import org.apache.xpath.XPathAPI;

import java.util.Vector;
import java.util.Hashtable;

import edu.ucsb.nceas.morpho.util.Log;

/**
 * class that represents a data package.
 */
public class MetadataObject
{
  /**
   *   A DOM Node is the basic datastructure. This may be the root of a DOM or it
   *   may be the top level node of a subtree. Working with the Node allows several
   *   Metadata objects to share a DOM and avoids the need to move Nodes between DOMs
   */
  private Node metadataNode = null;
  
  /**
   *   paths is designed to provide a 'map' between generic paths and specific locations
   *   in a tree (Node) structure. This allows one to get items like 'name' from paths that
   *   may occur in different locations in the tree for different schemas
   */
  private Hashtable metadataPaths = null;
  
  /**
   *   specifies the general type of the grammar used to specify the schema;
   *   currently, the allowed types are "publicID", "systemID", "namespace",
   *   "rootname", and "unknown"
   */
  private String grammarType = "unknown";
  
  /**
   *  the specific grammar value; i.e. publicID, namespace value, etc
   */
   private String grammar = "";

  // class constructors -----------------------
  public MetadataObject(Node node) {
    this.metadataNode = node;
    metadataPaths = new Hashtable();
  }

  public MetadataObject(Node node, String grammartype, String grammar) {
    this.metadataNode = node;
    this.grammar = grammar;
    this.grammarType = grammartype;
    metadataPaths = new Hashtable();
  }

  public MetadataObject(Node node, Hashtable ht) {
    this.metadataNode = node;
    metadataPaths = ht;
  }

  public MetadataObject(Node node, Hashtable ht, String grammartype, String grammar) {
    this.metadataNode = node;
    metadataPaths = ht;
    this.grammar = grammar;
    this.grammarType = grammartype;
  }

  // getters and setters -----------------------
  public void setMetadataNode(Node nd) {
    metadataNode = nd;
  }
  
  public Node getMetadataNode() {
    return metadataNode; 
  }
  
  public Hashtable getMetadataPath() {
    return metadataPaths; 
  }
  
  public void setMetadataPath(Hashtable ht) {
    metadataPaths = ht;
  }
  
  public String getGenericValue(String genericName) {
    Object value = metadataPaths.get(genericName);
    String ret = null;
    if (value!=null) {
      // value is an XPath
      String path = (String)value;
      if ((path!=null)||(path.length()<1)) {
        try{
          // metadataNode is the context node
          NodeList nl = XPathAPI.selectNodeList(metadataNode, path);
          if (nl.getLength() == 0) return ret;
          // for now, just get first node value
          Node n = nl.item(0);
          Node child = n.getFirstChild();
          if (child != null) {
            String s = child.getNodeValue();
            s = s.trim();
            return s;
          } else {
            return ret;
          }
        } catch (Exception e) {
          Log.debug(5, "error in XPath node selection in MetadataObject");
        }
      }
    }
    else { return ret;}
    return ret;
  }
  
  public void setGenericValue(String genericName, String genericValue) {
    Object value = metadataPaths.get(genericName);
    if (value!=null) {
      // value is an XPath
      String path = (String)value;
      if ((path!=null)||(path.length()<1)) {
        try{
          // metadataNode is the context node
          NodeList nl = XPathAPI.selectNodeList(metadataNode, path);
          // for now, just set first node value
          Node n = nl.item(0);
          Node child = n.getFirstChild();
          if (child != null) {
            child.setNodeValue(genericValue);
          }
        } catch (Exception e) {
          Log.debug(5, "error in setting XPath node in MetadataObject");
        }
      }
    }
  }

  
}
