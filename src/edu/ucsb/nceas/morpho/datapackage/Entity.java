/**
 *  '$RCSfile: Entity.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-11-18 22:49:09 $'
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
import org.w3c.dom.DOMImplementation;
import org.apache.xerces.dom.DOMImplementationImpl;

import edu.ucsb.nceas.utilities.*;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * class that represents a Entity
 */
public  class Entity
{
  /*
   *  root node of DOM tree representing entity
   *  includes a param for setting the entity root name which
   *  is needed since (in eml2) entities can have various names
   */
  private Node entRoot = null;
  
  /*
   *  the AbstractDataPackage which contains this Entity
   *  if null, it has not yet been inserted an AbstractDataPackage
   */
  private AbstractDataPackage adpContainer = null;
  
  public Entity(String entName) {
    try{        
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        Document doc = impl.createDocument("", entName, null);
        entRoot = doc.getDocumentElement();
    }
    catch (Exception e) {
      Log.debug(5, "Unable to create a DOM for this entity");
    }  
  }
  
  public Entity(String entName, OrderedMap om) {
    this(entName);
    try {
      XMLUtilities.getXPathMapAsDOMTree(om, entRoot);
    }
    catch (Exception w) {
      Log.debug(5, "Unable to add OrderMap elements to DOM");
    }
  }
  
  public Entity(Node node) {
    entRoot = node;
  }

  public Entity(Node node, AbstractDataPackage adp) {
    this(node);
    this.setContainer(adp);
  }
  
  
  public void setContainer(AbstractDataPackage adp) {
    adpContainer = adp;
  }
  
  public AbstractDataPackage getContainer() {
    return adpContainer;
  }
  
  public Node getNode() {
    return entRoot;
  }
  
}
