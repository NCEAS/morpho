/**
 *       Name: XMLDisplayHandler.java
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-07-02 23:06:57 $'
 * '$Revision: 1.9 $'
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


import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.swing.tree.*;
import java.util.*;
import org.xml.sax.ext.*;

/**
 * SAX2 event handler for parsing an XML file and turning it
 * into a JTreeModel for use in editor. Specifically, this
 * class puts the XML element information into the NodeInfo
 * userobject and builds the connected set of JTreeModel nodes.
 */
// SAX2 implementation of event handler
//  Event Handler
class XMLDisplayHandler extends DefaultHandler implements LexicalHandler {
	private Stack stack;
	private DefaultTreeModel treeModel;
	private int nodeCount;
	String docname;
	String publicId;
	String systemId;

	// Constructor
	public XMLDisplayHandler (DefaultTreeModel treeModel) {
		// Create stack
		this.treeModel = treeModel;
		nodeCount = 0;
		stack = new Stack ();
	}
	
	public String getPublicId() {
	  return publicId;  
	}

	public String getDocname() {
	  return docname;  
	}
	
	public String getSystemId() {
	  return systemId;  
	}

  public void startElement (String uri, String localName,
                              String qName, Attributes atts)
           throws SAXException {
		//  Create new Node
		NodeInfo ni = new NodeInfo(localName);
		for (int i=0;i<atts.getLength();i++) {
		    ni.attr.put(atts.getLocalName(i),atts.getValue(i));
		}
		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (ni);
		if (nodeCount>0) {	
		  //  Add current Node to Node on top of Stack
		  DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) stack.peek();
		  parentNode.add (newNode);
		  treeModel.reload();
		}
		else {    // root node
		  treeModel.setRoot(newNode);
		}
		nodeCount++;
		
		//  Push current Node on top of Stack
		stack.push (newNode);
		
    }
  
    public void endElement (String uri, String localName,
                            String qName) throws SAXException {
		  if (nodeCount>1) {
		    stack.pop ();
		  }
    }
  
    public void characters(char ch[], int start, int length) {
		// Set Text of Node on top of Stack
    	String text = new String (ch, start, length);
//    	text = text.trim();
    	text = text.replace('\n', ' ');
    	if (text.length()>0) {
		    NodeInfo ni = new NodeInfo("#PCDATA");
		    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (ni);
		    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) stack.peek();
		    parentNode.add (newNode);
//		    ni.attr.put("Value", text);
        ni.setPCValue(text);
		    treeModel.reload();
		    nodeCount++;
		  }
    }

   public void startDocument() throws SAXException { 

   }

   public void endDocument() throws SAXException { }
   
   
      //
   // the next section implements the LexicalHandler interface
   //

   /** SAX Handler that receives notification of DOCTYPE. Sets the DTD */
   public void startDTD(String name, String publicId, String systemId) 
               throws SAXException {
      this.docname = name;          
      this.publicId = publicId;
      this.systemId = systemId;
   }

   /** 
    * SAX Handler that receives notification of end of DTD 
    */
   public void endDTD() throws SAXException {
    
//System.out.println("end DTD");
   }

   /** 
    * SAX Handler that receives notification of comments in the DTD
    */
   public void comment(char[] ch, int start, int length) throws SAXException {
   }

   /** 
    * SAX Handler that receives notification of the start of CDATA sections
    */
   public void startCDATA() throws SAXException {
   }

   /** 
    * SAX Handler that receives notification of the end of CDATA sections
    */
   public void endCDATA() throws SAXException {
   }

   /** 
    * SAX Handler that receives notification of the start of entities
    */
   public void startEntity(String name) throws SAXException {
   }

   /** 
    * SAX Handler that receives notification of the end of entities
    */
   public void endEntity(String name) throws SAXException {
   }

}
