/**
 *       Name: DOMTree.java
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-10-07 02:00:04 $'
 * '$Revision: 1.3 $'
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
 *
 *   NOTE: This class is a slightly modified version of sample code provided
 *   with XercesJ, the Java parser from Apache. Information about licensing
 *   in the original code is reproduced below.
 */

 /*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package edu.ucsb.nceas.morpho.editor;

import java.io.Serializable;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.utilities.*;

import java.util.Hashtable;

/**
 * Displays a DOM document in a tree control.
 *
 * @author  Andy Clark, IBM
 * @version
 */
public class DOMTree
    extends JTree 
    {

    //
    // Constructors
    //

    /** Default constructor. */
    public DOMTree() {
        this(null);
        }

    /** Constructs a tree with the specified document. */
    public DOMTree(Document document) {
        super(new Model());

        // set tree properties
        setRootVisible(false);

        // set properties
        setDocument(document);

        } // <init>()

    //
    // Public methods
    //

    /** Sets the document. */
    public void setDocument(Document document) {
        ((Model)getModel()).setDocument(document);
        expandRow(0);
        }

    /** Returns the document. */
    public Document getDocument() {
        return ((Model)getModel()).getDocument();
        }

    /** get the org.w3c.Node for a MutableTreeNode. */
    public Node getNode(Object treeNode) {
        return ((Model)getModel()).getNode(treeNode);
    }

    //
    // Classes
    //

    /**
     * DOM tree model.
     *
     * @author  Andy Clark, IBM
     * @version
     */
    static class Model 
        extends DefaultTreeModel
        implements Serializable
        {

        //
        // Data
        //

        /** Document. */
        private Document document;
        /** Node Map. */
        private Hashtable nodeMap = new Hashtable();
        
        /**
         *  An inverse nodeMap for getting treeNode from node
         */
         private Hashtable invNodeMap = new Hashtable();

        //
        // Constructors
        //

        /** Default constructor. */
        public Model() {
            this(null);
            }

        /** Constructs a model from the specified document. */
        public Model(Document document) {
            super(new DefaultMutableTreeNode(new NodeInfo("")));   //DFH
            setDocument(document);
            }

        //
        // Public methods
        //

        /** Sets the document. */
        public synchronized void setDocument(Document document) {

            // save document
            this.document = document;

            // clear tree and re-populate
            ((DefaultMutableTreeNode)getRoot()).removeAllChildren();
            nodeMap.clear();
            invNodeMap.clear();
            buildTree();
            fireTreeStructureChanged(this, new Object[] { getRoot() }, new int[0], new Object[0]);

            } // setDocument(Document)

        /** Returns the document. */
        public Document getDocument() {
            return document;
            }

        /** get the org.w3c.Node for a MutableTreeNode. */
        public Node getNode(Object treeNode) {
            return (Node)nodeMap.get(treeNode);
        }

        /** get a MutableTreeNode from the org.w3c.Node - DFH */
        public Object getInvNode(Node node) {
            return (Object)invNodeMap.get(node);
        }

        //
        // Private methods
        //

        /** Builds the tree. */
        private void buildTree() {
            
            // is there anything to do?
            if (document == null) { return; }
            Node rnode = document.getDocumentElement();
            // iterate over children of this node
            NodeList nodes = rnode.getChildNodes();
            int len = (nodes != null) ? nodes.getLength() : 0;
            MutableTreeNode root = (MutableTreeNode)getRoot();
            MutableTreeNode rt = buildRootNode(rnode, root);
            setRoot(rt);
            for (int i = 0; i < len; i++) {
                Node node = nodes.item(i);
                switch (node.getNodeType()) {
                    case Node.DOCUMENT_NODE: {
                        root = insertDocumentNode(node, rt);
                        break;
                        }

                    case Node.ELEMENT_NODE: {
                        insertElementNode(node, rt);
                        break;
                        }

                    default: // ignore

                    } // switch

                } // for 

            } // buildTree()

        /** Inserts a node and returns a reference to the new node. */
        private MutableTreeNode insertNode(String what, MutableTreeNode where, Hashtable ht) {

            NodeInfo ni = new NodeInfo(what);   //DFH
            if (ht!=null) {
              ni.attr = ht;
            }
            MutableTreeNode node = new DefaultMutableTreeNode(ni);  //DFH
            insertNodeInto(node, where, where.getChildCount());
            return node;

            } // insertNode(Node,MutableTreeNode):MutableTreeNode

        /** Inserts a text node and returns a reference to the new node(DFH). */
        private MutableTreeNode insertTNode(String what, MutableTreeNode where) {

            NodeInfo ni = new NodeInfo("#PCDATA");   //DFH
            ni.setPCValue(what);
            MutableTreeNode node = new DefaultMutableTreeNode(ni);  //DFH
            insertNodeInto(node, where, where.getChildCount());
            return node;

            } // insertNode(Node,MutableTreeNode):MutableTreeNode

            
        /** Inserts the document node. */
        private MutableTreeNode insertDocumentNode(Node what, MutableTreeNode where) {
            MutableTreeNode treeNode = insertNode("<"+what.getNodeName()+'>', where, null);
            nodeMap.put(treeNode, what);
            invNodeMap.put(what, treeNode);
            return treeNode;
            }

        /** Inserts an element node. */
        private MutableTreeNode insertElementNode(Node what, MutableTreeNode where) {

            // build up name
            StringBuffer name = new StringBuffer();
//            name.append('<');
            name.append(what.getNodeName());
            NamedNodeMap attrs = what.getAttributes();
            Hashtable ht = new Hashtable();
            int attrCount = (attrs != null) ? attrs.getLength() : 0;
            for (int i = 0; i < attrCount; i++) {
                Node attr = attrs.item(i);
                ht.put(attr.getNodeName(),attr.getNodeValue());
            }
//            name.append('>');

            // insert element node
            
            MutableTreeNode element = insertNode(name.toString(), where, ht);
            nodeMap.put(element, what);
            invNodeMap.put(what, element);
            
            // gather up attributes and children nodes
            NodeList children = what.getChildNodes();
            int len = (children != null) ? children.getLength() : 0;
            for (int i = 0; i < len; i++) {
                Node node = children.item(i);
                switch (node.getNodeType()) {
                    case Node.CDATA_SECTION_NODE: { 
                       insertCDataSectionNode( node, element ); //Add a Section Node
                       break;
                      }
                    case Node.TEXT_NODE: {
                        insertTextNode(node, element);
                        break;
                        }
                    case Node.ELEMENT_NODE: {
                        insertElementNode(node, element);
                        break;
                        }
                    }
                }

            return element;

            } // insertElementNode(Node,MutableTreeNode):MutableTreeNode

        /** build root node. */
        private MutableTreeNode buildRootNode(Node what, MutableTreeNode where) {

            // build up name
            StringBuffer name = new StringBuffer();
            name.append(what.getLocalName());
            NamedNodeMap attrs = what.getAttributes();
            Hashtable ht = new Hashtable();
            int attrCount = (attrs != null) ? attrs.getLength() : 0;
            for (int i = 0; i < attrCount; i++) {
                Node attr = attrs.item(i);
                ht.put(attr.getNodeName(),attr.getNodeValue());
            }

            
            nodeMap.put(where, what);
            invNodeMap.put(what, where);

            NodeInfo ni = new NodeInfo(name.toString());   //DFH
            if (ht!=null) {
              ni.attr = ht;
            }
            MutableTreeNode node = new DefaultMutableTreeNode(ni);  //DFH
            
            return node;

        } 
            
            
        /** Inserts a text node. */
        private MutableTreeNode insertTextNode(Node what, MutableTreeNode where) {
            String value = what.getNodeValue().trim();
            if (value.length() > 0) {
                MutableTreeNode treeNode = insertTNode(value, where);
                nodeMap.put(treeNode, what); 
                invNodeMap.put(what, treeNode);
                return treeNode;
                }
            return null;
            }

        
      /** Inserts a CData Section Node. */
      private MutableTreeNode insertCDataSectionNode(Node what, MutableTreeNode where) {
         StringBuffer CSectionBfr = new StringBuffer();         
         //--- optional --- CSectionBfr.append( "<![CDATA[" );
         CSectionBfr.append( what.getNodeValue() );
         //--- optional --- CSectionBfr.append( "]]>" );
         if (CSectionBfr.length() > 0) {
            MutableTreeNode treeNode = insertNode(CSectionBfr.toString(), where, null);
            nodeMap.put(treeNode, what);
            invNodeMap.put(what, treeNode);
            return treeNode;
            }
         return null;
         }


      } // class Model



    } // class DOMTree
