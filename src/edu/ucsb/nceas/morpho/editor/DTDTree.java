/**
 *        Name: DTDTree.java
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-02-28 03:55:03 $'
 * '$Revision: 1.20 $'
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


import com.wutka.dtd.*;
import java.util.*;
import javax.swing.tree.*;
import java.io.*;

/**
 * class for creating a treeModel based on structure defined 
 * in DTD. Uses DTD parser.
 */
public class DTDTree
{
    private String DTDFileName;
    
    // assorted global variables
    Vector elementnames;
    // after parsing, root node of instance tree is 'rootNode'
    public DefaultMutableTreeNode rootNode;
    public DefaultTreeModel treeModel;
    
    public DTD dtd = null;
    StringBuffer sb; 
    StringBuffer start_buffer;
    StringBuffer start;
    StringBuffer end_buffer;
    Stack tempStack;
    int indent = 0;
    String rootElementName = null;
    DTDElement rootElement = null;

    int cntr = 0;

// the Sequence_Flag determines whether or not the parsing marks 'sequences' of content
// such sequences need to be noted when the content is something like
// ((A,B,C)|(D,E)) i.e. when there are nested, unnamed sequences
    boolean Sequence_Flag = true;
    
    // levels is how many levels deep the depth first parse is carried out
    // needs to be specified to provide stopping criteria for recursive DTDs
    int levels = 10;
    
 
  public DTDTree() {
    this.DTDFileName = "";
  }
  public DTDTree(String dtdname) {
    this.DTDFileName = dtdname;  
  
		//{{INIT_CONTROLS
		//}}
	}
  
  public void setRootElementName(String name) {
    this.rootElementName = name; 
  }
  
  /**
   *  parses a dtd file and creates a tree of document instance
   */
	public void parseDTD() {
		try {
			FileReader reader = new FileReader(DTDFileName);
      DTDParser parser = new DTDParser(new BufferedReader(reader));
      dtd = parser.parse(true);
      elementnames = new Vector();
      Enumeration e = dtd.elements.elements();
      while (e.hasMoreElements()) {
        DTDElement elem = (DTDElement) e.nextElement();
        
        if (rootElementName!=null) {
          if (elem.name.equals(rootElementName)) {
            rootElement = elem; 
          }
        }
        elementnames.addElement(elem.name);
			}
			DTDElement elem;
			if (dtd.rootElement!=null) {
			  String root = (dtd.rootElement).name;
		    elem = dtd.rootElement;
		  }
		  else {
		    elem = rootElement;
		  }
      NodeInfo rootNodeInfo = new NodeInfo(elem.name);
      rootNodeInfo.setCardinality("ONE");
      DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(rootNodeInfo);
      rootNode = rootTreeNode;
	    buildTree(rootTreeNode);
	    treeModel = new DefaultTreeModel(rootTreeNode);
	  }
	  catch (Exception e) {}
	}



public DefaultMutableTreeNode buildTree(DefaultMutableTreeNode root) {
  Vector vect = new Vector();
  Vector vvvv = new Vector();
  Vector zzzz = new Vector();
  vect.addElement(root);
//  boolean tempflag = Sequence_Flag;
  for(int i=0;i<levels;i++) {
 //   if (i==0) {Sequence_Flag = false;}
 //   else {Sequence_Flag = tempflag;}
    for (Enumeration e = vect.elements() ; e.hasMoreElements() ;) { 
      DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)e.nextElement();
      vvvv = getChildren((NodeInfo)dmtn.getUserObject(),dmtn);
      for (Enumeration ee = vvvv.elements() ; ee.hasMoreElements() ;) { 
        zzzz.addElement(ee.nextElement());
      }
    }
    vect = zzzz;
    zzzz = new Vector();
  }
  // clean up any leaf nodes
  DefaultMutableTreeNode leaf = root.getFirstLeaf();
  while (leaf != null) {
    NodeInfo ni = (NodeInfo)leaf.getUserObject();
    ni.setItem(null);
    leaf = leaf.getNextLeaf();
  }
  return root;
}

// given an element, get a list of names of possible children

private Vector getChildren(NodeInfo ni, DefaultMutableTreeNode parentNode) {
  Vector vec = new Vector();
  Vector vec1 = new Vector();
  DTDElement elem = null;
  String name = ni.getName();
  if (name.equalsIgnoreCase("Any")) {
    
  }
  else if(name.equalsIgnoreCase("None")) {
    
  }
  else if(name.equalsIgnoreCase("#PCDATA")) {
    
  }
  else if(name.startsWith("(SEQUENCE)")) {
    Vector vec2 = new Vector();
    DTDSequence item = null;
    if (ni.getItem()!=null) {
      item = (DTDSequence)ni.getItem();
      ni.setItem(null);  // no sense keeping the item information in the userObject after this point
    }
    DTDItem[] items = ((DTDSequence) item).getItems();
    for (int i=0; i < items.length; i++)  {
      DTDItems(items[i],vec2,false);
    }
    for (Enumeration e = vec2.elements() ; e.hasMoreElements() ;) {
      NodeInfo node = (NodeInfo)(e.nextElement());
		  DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node);
      parentNode.add(newNode);
      vec1.addElement(newNode);
    }
  }
  
  else if(name.startsWith("(CHOICE)")) {
    Vector vec2 = new Vector();
    DTDChoice item = null;
    if (ni.getItem()!=null) {
      item = (DTDChoice)ni.getItem();
      ni.setItem(null); // no sense keeping the item information in the userObject after this point
    }
    DTDItem[] items = ((DTDChoice) item).getItems();
    for (int i=0; i < items.length; i++)  {
      DTDItems(items[i],vec2,true);
    }
    boolean first = true;
    if ((ni.getCardinality().equalsIgnoreCase("ONE"))) {
      for (Enumeration e = vec2.elements() ; e.hasMoreElements() ;) {
        NodeInfo node = (NodeInfo)(e.nextElement());
        if (first) {
          //node.setCardinality("SELECTED");
          node.setChoice(true);
          node.setSelected(true);
        }
        else 
        {  //{node.setCardinality("NOT SELECTED");}
          node.setChoice(true);
          node.setSelected(false);    
        }
        first = false;
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node);
        parentNode.add(newNode);
        vec1.addElement(newNode);
      }
    }
    else {
      for (Enumeration e = vec2.elements() ; e.hasMoreElements() ;) {
        NodeInfo node = (NodeInfo)(e.nextElement());
        node.setCardinality(ni.getCardinality());
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node);
        parentNode.add(newNode);
        vec1.addElement(newNode);
      }
    }

  }
  else {
    elem = (DTDElement)dtd.elements.get(name);
  }
  if (elem!=null) {
    getAttributes(ni, elem);
    DTDItems(elem.content, vec, false);
    for (Enumeration e = vec.elements() ; e.hasMoreElements() ;) {
      //  DTDElement el = (DTDElement)dtd.elements.get(((NodeInfo)(e.nextElement())).name);
      //  NodeInfo node = new NodeInfo(((NodeInfo)(e.nextElement())).name);
      NodeInfo node = (NodeInfo)(e.nextElement());
		  DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (node);
      parentNode.add(newNode);
      vec1.addElement(newNode);
    }
  }
  return vec1;
}

// returns a vector of NodeInfo objects of allowed child element 
private void DTDItems(DTDItem item, Vector vec, boolean flg) {
  if (item == null) return;

  if (item instanceof DTDAny)  {
    NodeInfo ni = new NodeInfo("Any");
    ni.setCardinality(getCardinality(item));
    vec.addElement(ni);
  }
  else if (item instanceof DTDEmpty) {
    NodeInfo ni = new NodeInfo("Empty");
    ni.setCardinality(getCardinality(item));
    vec.addElement(ni);
  }
  else if (item instanceof DTDName) {
    NodeInfo ni = new NodeInfo(((DTDName) item).value);
    ni.setCardinality(getCardinality(item));
    vec.addElement(ni);
  }
  else if (item instanceof DTDChoice) {
    DTDItem[] items = ((DTDChoice) item).getItems();
    if (items.length>1) {
      cntr++;
      NodeInfo ni = new NodeInfo("(CHOICE)"+cntr);
      ni.setItem(item);
      ni.setCardinality(getCardinality(item));
      vec.addElement(ni);
    }
    else {

//            DTDItem[] items = ((DTDChoice) item).getItems();
      for (int i=0; i < items.length; i++) {
        DTDItems(items[i],vec, false);
      }
    }
  }
  else if (item instanceof DTDSequence) {
    if ( ((Sequence_Flag)&&(flg)) ||
        ((Sequence_Flag)&&(!getCardinality(item).equals("ONE")))
        )
        
         {
            cntr++;
            NodeInfo ni = new NodeInfo("(SEQUENCE)"+cntr);
            ni.setItem(item);
            ni.setCardinality(getCardinality(item));
            vec.addElement(ni);
    }
    else {
          DTDItem[] items = ((DTDSequence) item).getItems();
          for (int i=0; i < items.length; i++) {
            DTDItems(items[i],vec,false);
          }
    }
  }
  else if (item instanceof DTDMixed) {
    DTDItem[] items = ((DTDMixed) item).getItems();

    for (int i=0; i < items.length; i++) {
      DTDItems(items[i],vec,false);
    }
  }
  else if (item instanceof DTDPCData) {
    NodeInfo ni = new NodeInfo("#PCDATA");
    ni.setPCValue(" ");
    ni.setCardinality(getCardinality(item));
    vec.addElement(ni);
  }
}

private void getAttributes(NodeInfo ni, DTDElement el) {
  Enumeration attrs = el.attributes.elements();
  while (attrs.hasMoreElements()) {
    DTDAttribute attr = (DTDAttribute) attrs.nextElement();
    getAttribute(ni, attr);
  }
}  


// currently set to return only 'REQUIRED' attributes with specified Default value
private void getAttribute(NodeInfo ni, DTDAttribute attr) {
  sb = new StringBuffer();
  if (attr.getDecl().equals(DTDDecl.REQUIRED)) {
   if (attr.defaultValue!=null) {
      sb.append(attr.defaultValue);
      ni.attr.put(attr.name,sb.toString());  
   }
   else if (attr.type instanceof DTDEnumeration) {
    // arbitrarily pick first choice
    String[] items = ((DTDEnumeration) attr.type).getItems();
    sb.append(items[0]);
    ni.attr.put(attr.name,sb.toString());
   }
  }
/*
  if (attr.type instanceof String) {
       //       sb.append(attr.defaultValue);
  }
  else if (attr.type instanceof DTDEnumeration) {
    sb.append("(");
    String[] items = ((DTDEnumeration) attr.type).getItems();

    for (int i=0; i < items.length; i++) {
      if (i > 0) sb.append(",");
      sb.append(items[i]);
    }
    sb.append(")");
  }
  else if (attr.type instanceof DTDNotationList) {
    sb.append("Notation (");
    String[] items = ((DTDNotationList) attr.type).getItems();

    for (int i=0; i < items.length; i++) {
      if (i > 0) sb.append(",");
      sb.append(items[i]);
    }
    sb.append(")");
  }

  if (attr.decl != null) {
      //      sb.append(" "+attr.decl.name);
  }

  if (attr.defaultValue != null) {
    sb.append(attr.defaultValue);
  }
//  sb.append("\" ");

*/
}

private String getCardinality(DTDItem item) {
  if (item.cardinal==DTDCardinal.NONE) return "ONE";
  if (item.cardinal==DTDCardinal.OPTIONAL) return "OPTIONAL";
  if (item.cardinal==DTDCardinal.ZEROMANY) return "ZERO to MANY";
  if (item.cardinal==DTDCardinal.ONEMANY) return "ONE to MANY";
return "ONE";
}

	//{{DECLARE_CONTROLS
	//}}
}