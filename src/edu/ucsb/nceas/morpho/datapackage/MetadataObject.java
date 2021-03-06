/**
 *  '$RCSfile: MetadataObject.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2008-04-11 18:54:01 $'
 * '$Revision: 1.13 $'
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

import java.util.Map;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.dataone.client.D1Object;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * class that represents a metadata object.
 */
public class MetadataObject extends D1Object
{
  /**
   * Document node of the in-memory DOM structure
   */
  protected Document doc;
  /**
   * root node of the in-memory DOM structure
   */
  static protected Node root;

  /**
   *   A DOM Node is the basic datastructure. This may be the root of a DOM or it
   *   may be the top level node of a subtree. Working with the Node allows several
   *   Metadata objects to share a DOM and avoids the need to move Nodes between DOMs
   */
  protected Node metadataNode = null;

  /**
   *   paths is designed to provide a 'map' between generic paths and specific locations
   *   in a tree (Node) structure. This allows one to get items like 'name' from paths that
   *   may occur in different locations in the tree for different schemas
   *   It was decided to use a DOM structure for storing this information since this
   *   is more general than a map and tools for getting data from a DOM are already in
   *   use (see XMLUtilites)
   */
  protected Node metadataPathNode = null;

  /**
   *   specifies the general type of the grammar used to specify the schema;
   *   currently, the allowed types are "publicID", "systemID", "namespace",
   *   "rootname", and "unknown"
   */
  protected String grammarType = "unknown";

  /**
   *  the specific grammar value; i.e. publicID, namespace value, etc
   */
   protected String grammar = "";

  // class constructors -----------------------
  public MetadataObject() {
  }

  public MetadataObject(Node node) {
    this.metadataNode = node;
  }

  public MetadataObject(Node node, String grammartype, String grammar) {
    this.metadataNode = node;
    this.grammar = grammar;
    this.grammarType = grammartype;
  }

  public MetadataObject(Node node, Node nd) {
    this.metadataNode = node;
    metadataPathNode = nd;
  }

  public MetadataObject(Node node, Node nd, String grammartype, String grammar) {
    this.metadataNode = node;
    metadataPathNode = nd;
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

  public Node getMetadataPath() {
    return metadataPathNode;
  }

  public String getGrammar() {
	  return grammar;
  }
	
  public void setGrammar(String grammar) {
	  this.grammar = grammar;
  }
	
  public void setMetadataPath(Node nd) {
    metadataPathNode = nd;
  }
  
  public Document getDocument(){
    return doc;
  }

  public String getGenericValue(String genericName) {
    Node value = null;
    try{
      Log.debug(20, "genericName: "+genericName);
      value = XMLUtilities.getTextNodeWithXPath(getMetadataPath(), genericName);
      if (value==null) return "";
    }
    catch (Exception e) {
      Log.debug(1, "Error in getGenericValue: "+e.toString());
    }
    String ret = "";
    if (value!=null) {
      // value is an XPath
      String path = value.getNodeValue();
      if ((path!=null)&&(path.length()>1)) {
        Log.debug(30,"path: "+path);
        try{
          // metadataNode is the context node
          try {
              Log.debug(30, " Document root node has " +
                        metadataNode.getAttributes().getLength() +
                        " attributes");
          } catch (Exception ex) {}

          NodeList nl = XPathAPI.selectNodeList(metadataNode, path);
          if (nl.getLength() == 0) {
              Log.debug(30, "no nodes for " + path + " in " + metadataNode.toString());
              return ret;
          }
          // for now, just get first node value
          Node n = nl.item(0);
          Node child = n.getFirstChild();
          if (child != null) {
            String s = child.getNodeValue();
            if (s==null) return "";
            s = s.trim();
            return s;
          } else {
            return ret;
          }
        } catch (Exception e) {
          Log.debug(5, "error in XPath node selection in MetadataObject (getGenericValue)"+
          "--- genericName: "+genericName);
        }
      }
    }
    else { return ret;}
    return ret;
  }

  public String getXPathValue(String path) {
    String ret = "";
      if ((path!=null)&&(path.length()>1)) {
        //Log.debug(30,"path: "+path);
        try{
          // metadataNode is the context node
          NodeList nl = XPathAPI.selectNodeList(metadataNode, path);
          if (nl.getLength() == 0) return ret;
          // for now, just get first node value
          Node n = nl.item(0);
          Node child = n.getFirstChild();
          if (child != null) {
            String s = child.getNodeValue();
            if (s==null) return "";
            s = s.trim();
            return s;
          } else {
            return ret;
          }
        } catch (Exception e) {
          Log.debug(5, "error in XPath node selection in MetadataObject (getXPathValue)");
        }
      }
    return ret;
  }

  public void setGenericValue(String genericName, String genericValue) {
    Object value = null;
    try{
      value = XMLUtilities.getTextNodeWithXPath(metadataPathNode, genericName);
    }
    catch (Exception e) {
      Log.debug(10, "Error in getGenericValue: "+e.toString());
    }

    Log.debug(30, genericName + " maps to " + value);
    if (value!=null) {
      // value is an XPath
      String path = (String)((Node)value).getNodeValue();
      if ((path!=null)||(path.length()<1)) {
        try{
          Log.debug(30, "node path is " + path);
          // metadataNode is the context node
          NodeList nl = XPathAPI.selectNodeList(metadataNode, path);
          // for now, just set first node value
          Node n = nl.item(0);

          if (n == null) {
              // this is bad
              Log.debug(5, "Unable to get node at " + path);
          }

          Node child = n.getFirstChild();
          if (child != null) {
            Log.debug(30, "setting value to " + genericValue);
            child.setNodeValue(genericValue);
          }
        } catch (Exception e) {
          Log.debug(30, "error in setting XPath node in MetadataObject: " + e.toString());
          e.printStackTrace();
        }
      }
    }
  }

  static public void main(String args[]) {

    try{
      root = XMLUtilities.getXMLAsDOMTreeRootNode("/eml200KeymapConfig.xml");
      Map mp = XMLUtilities.getDOMTreeAsXPathMap(root);
      System.out.println(mp.toString());
      // assumed XPath is in args[0]; evaluation of XPath expression can result in a variety of
      // object types; check i it is a NodeList
      XObject xobj = XPathAPI.eval(root, args[0]);
      Log.debug(1,"XObject evaluated: Type ="+xobj.getType());
      if (xobj.getType()==XObject.CLASS_BOOLEAN) {
          Log.debug(1,"XPath evaluation results in a BOOLEAN!"+"     val: "+xobj.bool());
      }
      if (xobj.getType()==XObject.CLASS_NODESET) {
        NodeList nl = XPathAPI.selectNodeList(root, args[0]);
//      if (nl.getLength() == 0)  Log.debug(1,"NodeLIst has zero length");
        Log.debug(1,"Number of nodes: "+nl.getLength());
          // for now, just get first node value
        Node n = nl.item(0);
        Node child = n.getFirstChild();
        if (child != null) {
          String s = child.getNodeValue();
          s = s.trim();
          Log.debug(1,"Value is: "+s);
        } else {
          Log.debug(1,"No value!!!");
        }
      }
      } catch (Exception e) {
          Log.debug(5, "error in XPath node selection in MetadataObject"+e.toString());
      }
  }

  public String toString() {

    return XMLUtilities.getDOMTreeAsString(metadataNode);
  }

}
