/**
 *        Name: DOMTree.java
 *     Purpose: see notices below - this class creates a Java tree from
 *     DOM  - modified version of Apache XML example
 *    NOTE: This class should be parser independent
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: DOMTree.java,v 1.1 2000-05-31 15:37:07 higgins Exp $'
 */
 
 
 
/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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

package edu.ucsb.nceas.dtclient;


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
        

        //
        // Constructors
        //

        /** Default constructor. */
        public Model() {
            this(null);
            }

        /** Constructs a model from the specified document. */
        public Model(Document document) {
            super(new DefaultMutableTreeNode());
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

        //
        // Private methods
        //

        /** Builds the tree. */
        private void buildTree() {
            
            // is there anything to do?
            if (document == null) { return; }

            // iterate over children of this node
            NodeList nodes = document.getChildNodes();
            int len = (nodes != null) ? nodes.getLength() : 0;
            MutableTreeNode root = (MutableTreeNode)getRoot();
            for (int i = 0; i < len; i++) {
                Node node = nodes.item(i);
                switch (node.getNodeType()) {
                    case Node.DOCUMENT_NODE: {
                        root = insertDocumentNode(node, root);
                        break;
                        }
                        
                    case Node.DOCUMENT_TYPE_NODE: {
                        insertDocumentTypeNode(node,root);
                        break;
                        }

                    case Node.ELEMENT_NODE: {
                        insertElementNode(node, root);
                        break;
                        }

                    default: // ignore

                    } // switch

                } // for 

            } // buildTree()

        /** Inserts a node and returns a reference to the new node. */
        private MutableTreeNode insertNode(String what, MutableTreeNode where) {

            MutableTreeNode node = new DefaultMutableTreeNode(what);
            insertNodeInto(node, where, where.getChildCount());
            return node;

            } // insertNode(Node,MutableTreeNode):MutableTreeNode
            
        /** Inserts the documentType node. */
        private MutableTreeNode insertDocumentTypeNode(Node what, MutableTreeNode where) {
            MutableTreeNode treeNode = insertNode("<"+what.getNodeName()+'>', where);
            nodeMap.put(treeNode, what);
            
            // gather up attributes and children nodes
            NodeList children = what.getChildNodes();
            int len = (children != null) ? children.getLength() : 0;
            for (int i = 0; i < len; i++) {
                Node node = children.item(i);
                switch (node.getNodeType()) {
                    case Node.CDATA_SECTION_NODE: { 
                       insertCDataSectionNode( node, treeNode ); //Add a Section Node
                       break;
                      }
                    case Node.TEXT_NODE: {
                        insertTextNode(node, treeNode);
                        break;
                        }
                    case Node.ELEMENT_NODE: {
                        insertElementNode(node, treeNode);
                        break;
                        }
                    }
                }
            
            
            return treeNode;
            }
            
        /** Inserts the document node. */
        private MutableTreeNode insertDocumentNode(Node what, MutableTreeNode where) {
            MutableTreeNode treeNode = insertNode("<"+what.getNodeName()+'>', where);
            nodeMap.put(treeNode, what);
            return treeNode;
            }

        /** Inserts an element node. */
        private MutableTreeNode insertElementNode(Node what, MutableTreeNode where) {

            // build up name
            StringBuffer name = new StringBuffer();
            name.append('<');
            name.append(what.getNodeName());
            NamedNodeMap attrs = what.getAttributes();
            int attrCount = (attrs != null) ? attrs.getLength() : 0;
            for (int i = attrCount-1; i >-1; i--) {
                Node attr = attrs.item(i);
                name.append(' ');
                name.append(attr.getNodeName());
                name.append("=\"");
                name.append(attr.getNodeValue());
                name.append('"');
                }
            name.append('>');

            // insert element node
            
            MutableTreeNode element = insertNode(name.toString(), where);
            nodeMap.put(element, what);
            
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
                    case Node.ENTITY_REFERENCE_NODE: {
                        insertEntityRefernceNode(node, element);
                        break;
                        }
                    }
                }

            return element;

            } // insertElementNode(Node,MutableTreeNode):MutableTreeNode

        /** Inserts a text node. */
        private MutableTreeNode insertTextNode(Node what, MutableTreeNode where) {
            String value = what.getNodeValue().trim();
            if (value.length() > 0) {
                MutableTreeNode treeNode = insertNode(value, where);
                nodeMap.put(treeNode, what);            
                return treeNode;
                }
            return null;
            }

        /** Inserts an Entity_Reference node. */
        private MutableTreeNode insertEntityRefernceNode(Node what, MutableTreeNode where) {
            String value = "ER:"+what.getNodeName().trim();
            if (value.length() > 0) {
                MutableTreeNode treeNode = insertNode(value, where);
                nodeMap.put(treeNode, what);
                Node EntityValueNode = what.getFirstChild();
                String val1 = EntityValueNode.getNodeValue().trim();
                if (val1.length() > 0) {
                    MutableTreeNode mtn = insertNode(val1, where);
                }
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
            MutableTreeNode treeNode = insertNode(CSectionBfr.toString(), where);
            nodeMap.put(treeNode, what);            
            return treeNode;
            }
         return null;
         }


      } // class Model



    } // class DOMTree
