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
 *     '$Date: 2003-08-08 16:48:50 $'
 * '$Revision: 1.21.18.1 $'
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
import java.util.Enumeration;
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
 * Content information from a DTD/schema can thus be stored along with actual
 * XML element data
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
    
    // value that is returned for PCDATA
    String PCDataValue;
    
    // allowed values - ONE, ZERO to MANY, ONE to MANY, OPTIONAL
    String cardinality = "ONE"; 
        
    // attributes of associated XML element
    Hashtable attr;

    // used with DTD parser with 'Choice' Elements
    Object Item = null;
    
    /** name of special class used to display this node */
    String editor = null;

    /** name of special class used to display rootnode */
    String rooteditor = null;
    
    /** help string for this node */
    String help = null;
    
    /** indicates whether this node is a CHOICE node */
    boolean choice_flag;
    
    
    /** indicates whether this node is SELECTED 
     *  only meaningful if choice_flag is true
     */
    boolean selected_flag;
    
    /** indicates whether this node is a CHECKBOX
     *  this is a selected node whose parent CHOICE element
     *  is repeatable, meaning that multiple choice can occur.
     *
     *  only meaningful if choice_flag is true
     */
    boolean checkbox_flag;
    
    
   /**
    * flag to indicate that this nodeInfo object really is an XML attribute of its parent
    * node. Needed so that xml attributes can be displayed just kike other PCDATA node
    * but system can put then back as xml attributes when a tree is serialized
    */
   boolean xml_attribute = false;

    /**
     *  indicates the 'visibility level'. This parameter is
     *  used to indicate whether nodes should be included in the
     *  tree display. Default is '0' indicatng that a node should always
     *  appear. Higher levels indicate less importance (i.e. all levels
     *  above some threshold may be ignored)
     */
     int nodeVisLevel = 0;
 /**
  * creates a new NodeInfo object with the indicated name.
  * 
  * @param name name is the text that will appear when a TreeNode which has been
  * assigned a NodeInfo object with 'name' as the UserObject;
  */
 public NodeInfo(String name) {
    attr = new Hashtable();
    this.name = name;
    this.iconName  = "red.gif";
    setIcon("red.gif");
    choice_flag = false;
    selected_flag = false;
    checkbox_flag = false;
 }
 
  public String toString() {
    if (name.equalsIgnoreCase("#PCDATA")) {
 //     return ((String)attr.get("Value"));
      return PCDataValue;
    }
    else {
      if (name.indexOf("CHOICE")>-1) {
        return "(CHOICE)";
      }
      else if (name.indexOf("SEQUENCE")>-1) {
        return "(SEQUENCE)";
      }
      else {
        return name;
      }
    }
  }
  
  public void setNodeVisLevel(int nvl) {
    this.nodeVisLevel = nvl;
  }

  public int getNodeVisLevel() {
    return nodeVisLevel;
  }
  
  public void setXMLAttribute (boolean val) {
    xml_attribute = val;
  }
  
  public boolean isXMLAttribute() {
    return xml_attribute;
  }
  
  public void setCardinality(String card) {
    this.cardinality = card;
       
  if(card.equalsIgnoreCase("ONE")) {
    setIcon("red.gif");    
  }
  if(card.equalsIgnoreCase("OPTIONAL")) {
    setIcon("yellow.gif");    
  }
  if(card.equalsIgnoreCase("ZERO to MANY")) {
    setIcon("green.gif");    
  }
  if(card.equalsIgnoreCase("ONE to MANY")) {
    setIcon("blue.gif");    
  }
  if (choice_flag && selected_flag) {
    setIcon("sel.gif");  
  }
  if (choice_flag && !selected_flag) {
    setIcon("unsel.gif");
  }
  
  }
  
  
  // Accesors
  public boolean isChoice() {
    return choice_flag;  
  }
  
  public void setChoice(boolean flg) {
    this.choice_flag = flg;
  }

  public void setCheckboxFlag(boolean flg) {
    this.checkbox_flag = flg;
    if (choice_flag && selected_flag && !checkbox_flag) {
        setIcon("sel.gif");  
    }
    if (choice_flag && selected_flag && checkbox_flag) {
        setIcon("checkedBox.gif");  
    }
    if (choice_flag && !selected_flag && !checkbox_flag) {
        setIcon("unsel.gif");  
    }
    if (choice_flag && !selected_flag && checkbox_flag) {
        setIcon("uncheckedBox.gif");  
    }
  }
  
  public boolean isCheckbox() {
    return checkbox_flag;
  }
  
  public boolean isSelected() {
    return selected_flag;  
  }
  
  public void setSelected(boolean flg) {
    this.selected_flag = flg;
    if (choice_flag && selected_flag && !checkbox_flag) {
        setIcon("sel.gif");  
    }
    if (choice_flag && selected_flag && checkbox_flag) {
        setIcon("checkedBox.gif");  
    }
    if (choice_flag && !selected_flag && !checkbox_flag) {
        setIcon("unsel.gif");  
    }
    if (choice_flag && !selected_flag && checkbox_flag) {
        setIcon("uncheckedBox.gif");  
    }
  }
  
  public String getHelp() {
    return help;
  }
  public void setHelp(String hlp) {
    this.help = hlp;
  }
  
  public String getEditor() {
    return editor;
  }
  public void setEditor(String edt) {
    this.editor = edt;
  }

  public String getRootEditor() {
    return rooteditor;
  }
  public void setRootEditor(String redt) {
    this.rooteditor = redt;
  }
  
  
  public void setPCValue(String val) {
    PCDataValue = val; 
  }
  
  public String getPCValue() {
    return PCDataValue; 
  }
  public String getCardinality() {
    return cardinality;    
  }
    
  public Object getItem() {
    return Item;
  }
  
  public void setItem(Object o) {
    Item = o;
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
      ImageIcon temp = new ImageIcon(getClass().getResource(name));
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
  
  
  public NodeInfo cloneNodeInfo() {
    NodeInfo clone = new NodeInfo(this.name);
    clone.cardinality = this.cardinality;
    clone.iconName = this.iconName;
    clone.PCDataValue = this.PCDataValue;
    clone.Item = this.Item;
    clone.editor = this.editor;
    clone.choice_flag = this.choice_flag;
    clone.selected_flag = this.selected_flag;
    clone.rooteditor = this.rooteditor;
    clone.help = this.help;
    Enumeration enum = this.attr.keys();
    while (enum.hasMoreElements()) {
        Object kk = enum.nextElement();
        Object val = this.attr.get(kk);
        clone.attr.put(kk, val);    
    }
    
    return clone;
  }
  
}