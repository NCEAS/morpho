package edu.ucsb.nceas.querybean;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.swing.tree.*;
import java.util.*;


// SAX2 implementation of event handler
//  Event Handler
class myHandler extends DefaultHandler {
	private Stack stack;
	private DefaultTreeModel treeModel;
	private int nodeCount;
	// Constructor
	public myHandler (DefaultTreeModel treeModel) {
		// Create stack
		this.treeModel = treeModel;
		nodeCount = 0;
		stack = new Stack ();
	}
	
    public void startElement (String uri, String localName,
                              String qName, Attributes atts)
           throws SAXException {
		//  Create new Node
		NodeInfoDG ni = new NodeInfoDG(localName);
		for (int i=0;i<atts.getLength();i++) {
		    ni.attr.put(atts.getLocalName(i),atts.getValue(i));
		}
		DefaultMutableTreeNode newNode = 
			new DefaultMutableTreeNode (ni);
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
    	text = text.trim();
    	if (text.length()>0) {
		    NodeInfoDG ni = new NodeInfoDG("#PCDATA");
		    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (ni);
		    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) stack.peek();
		    parentNode.add (newNode);
		    ni.attr.put("Value", text);
		    treeModel.reload();
		    nodeCount++;
		}
    }

   public void startDocument() throws SAXException { 

   }

   public void endDocument() throws SAXException { }
}
