/**
 *       Name: NodeInfo.java
 *    Purpose: Used to store various information for application
 *             configuration in an XML file
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-05-23 23:41:51 $'
 * '$Revision: 1.8 $'
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

package edu.ucsb.nceas.morpho.editor;

import java.util.Hashtable;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.*;
import java.io.*;
import com.wutka.dtd.*;

/**
 * NodeInfo is a class used as a UserObject for a specialized JTree model. It is 
 * basically the container for all information about the node in the tree data
 * structure. This includes the node text and icon. When the tree is used to show
 * the hierarchy of an XML document, it also contains all all information in the
 * attributes of the XML node, as well as other information like the cardinality
 * of the node.
 * 
 * @author higgins
 */
public class NodeInfo implements Serializable
{
    static Hashtable icons;
    static { icons = new Hashtable(); }
    
    // name is the string that appears when NodeInfo is associated with tree node
    String name;
    
    // iconName is string used to get icon from icons Hashtable
    String iconName;
    
    // allowed values - ONE, ZERO to MANY, ONE to MANY, OPTIONAL
    String cardinality = "ONE"; 
        
    // attributes of associated XML element
    Hashtable attr;

 /**
  * creates a new NodeInfo object with the indicated name.
  * 
  * @param name name is the text that will appear when a TreeNode which has been
  * assigned a NodeInfo object with 'name' as the UserObject;
  */
 public NodeInfo(String name) {
    attr = new Hashtable();
    this.name = name;
    this.iconName = null;
 }
 
  public String toString() {
    if (name.equalsIgnoreCase("#PCDATA")) {
      return ((String)attr.get("Value"));
    }
    else {return name;}
  }
  
  public void setCardinality(String card) {
    this.cardinality = card;
/*       
  if(card.equalsIgnoreCase("ONE")) {
    setIcon("greensq.gif");    
  }
  if(card.equalsIgnoreCase("OPTIONAL")) {
    setIcon("greenqumark.gif");    
  }
  if(card.equalsIgnoreCase("ZERO to MANY")) {
    setIcon("greenasterisk.gif");    
  }
  if(card.equalsIgnoreCase("ONE to MANY")) {
    setIcon("greenplus.gif");    
  }
  if(card.equalsIgnoreCase("SELECTED")) {
    setIcon("sel.gif");    
  }
  if(card.equalsIgnoreCase("NOT SELECTED")) {
    setIcon("unsel.gif");    
  }
 */       
  }
  
  public String getCardinality() {
    return cardinality;    
  }
    
    
  public void setName (String name) {
    this.name = name;
  }
    
  public String getName() {
    return name;
  }
  
  public void setIcon(String name) {
    iconName = name;
    if (!icons.containsKey(name)) {    //see if icon is not already in hashtable
      ImageIcon temp = new ImageIcon("icons"+System.getProperty("file.separator")+name);
      icons.put(name,temp);
    }
  }
  
  public ImageIcon getIcon() {
    if (iconName!=null) {
      if (icons.containsKey(iconName)) {
        return (ImageIcon)(icons.get(iconName));
      }
      else { return null; }
    }
    return null;
    }
    
  public String getIconName() {
    return iconName;
  }
}