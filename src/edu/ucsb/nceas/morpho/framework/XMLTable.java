/**
 *        Name: XMLTable.java
 *     Purpose: this class displays XML DOM node and its children
 *     as a table. 
 *    
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: XMLTable.java,v 1.1 2000-05-31 15:37:07 higgins Exp $'
 */

package edu.ucsb.nceas.dtclient;

import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import javax.swing.table.*;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class designed for creating a table from XML document
 * Given a DOM node, the node determines a table
 * the number of rows in the table is equal to the number
 * of child elements of original node.
 * Children of each child (i.e. grandchildren of the original
 * node) determine the columns. Table with have number of cols
 * that equals maximum number of children of any child node.
 * Data to be placed in each cell is assumed to be in TEXT node under
 * Element node. Attributes ignored for now.
 * 
 * @author Dan Higgins
 */
 
 public class XMLTable {
    Node topNode;
    JTable table;
    Vector rowvectors;
    int numrows;
    int numcols;
    Vector rownames;
    String[] headers = {"Document ID", "Document Name", "Document Type", "Title"};
    
    //
    // Constructors
    //

    /** Default constructor. */
    public XMLTable() {
        this(null);
        }

    /** Constructs a table with the specified document. */
    public XMLTable(Node document) {
        this.topNode = document;
        init();
    }
    
    void init() {
         // is there anything to do?
         if (topNode == null) { return; }

         
         // iterate over children of this node
         NodeList nodes = topNode.getChildNodes();
         int len = (nodes != null) ? nodes.getLength() : 0;
         // loop over child node
         numrows = 0;
         numcols = 0;
         rowvectors = new Vector(); // vector of vectors, one for each row
         rownames = new Vector();
         for (int i = 0; i < len; i++) {
            Node node = nodes.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE) {
                numrows++;
                Vector cellvalues = new Vector();  // vector with one element per cell in a goven row
                int tempcols = 0;
                // for now ignore TEXT data associated with these node
                // TEXT is actually row labels?
                rownames.addElement(getText(node));  // save if needed
                // now get children of each child
                NodeList gchilds = node.getChildNodes();
                int glen = (gchilds != null) ? gchilds.getLength() : 0;
                //loop over grandchildren
                for (int j = 0; j < glen; j++) {
                    Node gnode = gchilds.item(j);
                    if (gnode.getNodeType()==Node.ELEMENT_NODE) {
                        tempcols++;
                        // now get text for each of the grandchild nodes
                        String txt = getText(gnode);
                        cellvalues.addElement(txt);    
                    } // end if loop
                    if (tempcols>numcols) numcols=tempcols; // find largest number of cols
                } // end j loop
            rowvectors.addElement(cellvalues);   
            } //end of if loop
            
         } // end of i loop
         if (numcols==0) {
         if (rownames.size()>0) {
            numcols = 1;
            rowvectors = new Vector();
            for (int i=0;i<rownames.size();i++) {
                Vector vvv = new Vector();
                vvv.addElement(rownames.elementAt(i));
                rowvectors.addElement(vvv);
            }
         }
         }
        
    } // init end
    
    /* get the text associated with a node (which is a child node in DOM
     * due to possibility of mixed content
     */
    private String getText(Node nd) {
        String txt = "";
        NodeList nodes = nd.getChildNodes();
        int len = (nodes != null) ? nodes.getLength() : 0;
        for (int k = 0; k<len; k++) {
            Node node = nodes.item(k);
            if (node.getNodeType()==Node.TEXT_NODE) {
                txt = txt + node.getNodeValue().trim()+ " "; // concatenates multiple text nodes    
            }
        } // end k loop
        return (txt.trim());        
    }
    
    private Object getTableData (int row, int col) {
           Vector rrr = (Vector)rowvectors.elementAt(row);
           if (rrr.size()<=col) {
                return "";
           }
           else {
                return (rrr.elementAt(col));
           }
    }
    
    private String getheaders(int kkk) {
    return headers[kkk];   
    }
    
    JTable createTable() {
        
        TableModel dataModel = new AbstractTableModel() {
            public int getColumnCount() { return numcols; }
            public int getRowCount() { return numrows;}
            public Object getValueAt(int row, int col) { return getTableData(row,col); }
            public String getColumnName(int c) {return getheaders(c);}
      }; 
      JTable table = new JTable(dataModel);
      return table;
    }
    
    
    
 }
