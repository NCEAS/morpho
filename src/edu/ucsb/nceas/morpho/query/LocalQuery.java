/**
 *        Name: LocalQuery.java
 *     Purpose: A Class that carries out queries of local XML docuements
 *		 (searchs local collection of XML files)
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: LocalQuery.java,v 1.10 2000-11-20 17:44:38 higgins Exp $'
 */

package edu.ucsb.nceas.querybean;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xalan.xpath.xml.FormatterToXML;
import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import com.arbortext.catalog.*;
import java.util.Vector;
import java.util.PropertyResourceBundle;
import java.util.Hashtable;

public class LocalQuery extends Thread 
{
    
    static Hashtable dom_collection;
    String local_xml_directory;
    String local_dtd_directory;
    String xmlcatalogfile; 
    String xpathExpression;
    String[] xpathExpressions = null;   // use for multiple queries
    boolean Andflag = false;           // how to combine multiple queries (Or is default)
    String xmlFileFolder;
    JTable RSTable = null;
    String[] headers = {"File Name", "Document Type", "Node Name", "Text"};
    DefaultTableModel dtm;
    JButton Halt = null; //supplied by calling routine
    String Haltname;
    boolean stopFlag = false;
    boolean AndResultFlag;
    
    static {dom_collection = new Hashtable();}
    
public LocalQuery() {
    this(null);
        dtm = new DefaultTableModel(headers,0);
        RSTable = new JTable(dtm);
    PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
    local_dtd_directory =(String)options.handleGetObject("local_dtd_directory");     // DFH
    local_xml_directory =(String)options.handleGetObject("local_xml_directory");     // DFH
    local_xml_directory = local_xml_directory.trim();
    xmlcatalogfile = local_dtd_directory.trim()+"/catalog"; 
    xpathExpression = "//*[contains(text(),\"NCEAS\")]";
    xmlFileFolder = local_xml_directory.trim();

		//{{INIT_CONTROLS
		//}}
	}

public LocalQuery(String xpathstring) {
        dtm = new DefaultTableModel(headers,0);
        RSTable = new JTable(dtm);
    PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
    local_dtd_directory =(String)options.handleGetObject("local_dtd_directory");     // DFH
    local_xml_directory =(String)options.handleGetObject("local_xml_directory");     // DFH
    local_xml_directory = local_xml_directory.trim();
    xmlcatalogfile = local_dtd_directory.trim()+"/catalog"; 
//    xpathExpression = "//*[contains(text(),\"NCEAS\")]";
    xmlFileFolder = local_xml_directory.trim();
    this.xpathExpression = xpathstring;
}

public LocalQuery(String xpathstring, JButton button) {
    Halt = button;
        dtm = new DefaultTableModel(headers,0);
        RSTable = new JTable(dtm);
    PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
    local_dtd_directory =(String)options.handleGetObject("local_dtd_directory");     // DFH
    local_xml_directory =(String)options.handleGetObject("local_xml_directory");     // DFH
    local_xml_directory = local_xml_directory.trim();
    xmlcatalogfile = local_dtd_directory.trim()+"/catalog"; 
//    xpathExpression = "//*[contains(text(),\"NCEAS\")]";
    xmlFileFolder = local_xml_directory.trim();
    this.xpathExpression = xpathstring;
}
    
public LocalQuery(String[] xpathstrings, boolean and_flag, JButton button) {
    Halt = button;
        dtm = new DefaultTableModel(headers,0);
        RSTable = new JTable(dtm);
    PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
    local_dtd_directory =(String)options.handleGetObject("local_dtd_directory");     // DFH
    local_xml_directory =(String)options.handleGetObject("local_xml_directory");     // DFH
    local_xml_directory = local_xml_directory.trim();
    xmlcatalogfile = local_dtd_directory.trim()+"/catalog"; 
//    xpathExpression = "//*[contains(text(),\"NCEAS\")]";
    xmlFileFolder = local_xml_directory.trim();
    this.xpathExpressions = xpathstrings;
    setAndFlag(and_flag);
}

public void setAndFlag(boolean flg) {
    Andflag = flg;   
}

public void setSearch(String match){
    xpathExpression = "//*[contains(text(),\""+match+"\")]";
}

public void setSearch(String type, String match){
    if(type.equalsIgnoreCase("contains")) {
        xpathExpression = "//*[contains(text(),\""+match+"\")]";
    }
    else if (type.equalsIgnoreCase("doesn't contain")) {
        xpathExpression = "//*[not(contains(text(),\""+match+"\"))]";
    }
    else if (type.equalsIgnoreCase("is")) {
        xpathExpression = "//*[text() = \""+match+"\"]";
    }
    else if (type.equalsIgnoreCase("is not")) {
        xpathExpression = "//*[not(text() = \""+match+"\")]";
    }
    else if(type.equalsIgnoreCase("starts with")) {
        xpathExpression = "//*[starts-with(text(),\""+match+"\")]";
    }
    
    else xpathExpression = "//*[contains(text(),\""+match+"\")]";
}

public static String getPath(String type, String match){
    if(type.equalsIgnoreCase("contains")) {
        return ("//*[contains(text(),\""+match+"\")]");
    }
    else if (type.equalsIgnoreCase("doesn't contain")) {
        return ("//*[not(contains(text(),\""+match+"\"))]");
    }
    else if (type.equalsIgnoreCase("is")) {
        return ("//*[text() = \""+match+"\"]");
    }
    else if (type.equalsIgnoreCase("is not")) {
        return ("//*[not(text() = \""+match+"\")]");
    }
    else if(type.equalsIgnoreCase("starts with")) {
        return ("//*[starts-with(text(),\""+match+"\")]");
    }
    
    else return ("//*[contains(text(),\""+match+"\")]");
}

public void setXmlcatalogfile(String str) {
    this.xmlcatalogfile = str;
}

public void setXmlFileFolder(String str) {
    this.xmlFileFolder = str;
}

public void setXpathExpression(String str) {
    this.xpathExpression = str;
}
    
public JTable getRSTable() {
    return RSTable;
}
    
    
    
void queryAll()
	{
	    Node root;
        long starttime, curtime, fm;
        if (Halt!=null) {
            Haltname = Halt.getText();
            Halt.setText("Halt");
        }
        DOMParser parser = new DOMParser();
        CatalogEntityResolver cer = new CatalogEntityResolver();
        try {
          System.out.println("xmlcatalogfile is: "+xmlcatalogfile);
          Catalog myCatalog = new Catalog();
            System.out.println("new catalog created!");
            myCatalog.loadSystemCatalogs();
            System.out.println("loadSystemCatalogs completed!");
            myCatalog.parseCatalog(xmlcatalogfile);
            cer.setCatalog(myCatalog);
        }
        catch (Exception e) {System.out.println("Problem creating Catalog!" + e.toString());}
        parser.setEntityResolver(cer);
        starttime = System.currentTimeMillis();
	    StringWriter sw = new StringWriter();
        
        
	    File xmldir = new File(local_xml_directory);
	    Vector filevector = new Vector();
	    getFiles(xmldir, filevector);
	    
//	    String[] files = xmldir.list();
	    
	    for (int i=0;i<filevector.size();i++)
        {
//            String filename = files[i];
//            File currentfile = new File(xmldir, filename);
            File currentfile = (File)filevector.elementAt(i);
            String filename = currentfile.getPath();
            if (stopFlag) break;
            if (currentfile.isFile())
            
            {
              if (dom_collection.containsKey(filename)){
                root = ((Document)dom_collection.get(filename)).getDocumentElement();
              }
              else {
                InputSource in;
                try
                {
                    in = new InputSource(new FileInputStream(filename));
                }
                catch (FileNotFoundException fnf)
                {
                    System.err.println("FileInputStream of " + filename + " threw: " + fnf.toString());
                    fnf.printStackTrace();
                    return;
                }
                try
                    {
                    parser.parse(in);
                    }
                catch(Exception e1)
                    {
                        System.err.println("Parsing " + filename + " threw: " + e1.toString());
                        e1.printStackTrace();
                        continue;
                    }

      // Get the documentElement from the parser, which is what the selectNodeList method expects
                Document current_doc = parser.getDocument();
//                Node root = parser.getDocument().getDocumentElement();
                root = parser.getDocument().getDocumentElement();
                dom_collection.put(filename,current_doc);
              }     
                String rootname = root.getNodeName();
                NodeList nl = null;
                try
                {
                if (xpathExpressions == null) {   // a single xpath expression
                if (stopFlag) break;
                    // Use the simple XPath API to select a node.
                    nl = XPathAPI.selectNodeList(root, xpathExpression);
                    String[] rss = new String[4];
                    for (int ii=0;ii<nl.getLength();ii++) {
                        if (stopFlag) break;
                        rss[0] = filename;
                        rss[1] = rootname;
                        rss[2] = nl.item(ii).getNodeName();
                        Node cn = nl.item(ii).getFirstChild();  // assume 1st child is text node
                        if ((cn!=null)&&(cn.getNodeType()==Node.TEXT_NODE)) {
                            rss[3] = cn.getNodeValue().trim();
                        }
                        else { rss[3]="";}
                    dtm.addRow(rss);
                    }
                }
                else {   // multiple expressions are handled here
                    if (stopFlag) break;
                    if (!Andflag) {
                        for (int k=0;k<xpathExpressions.length;k++) {
                            if (xpathExpressions[k].length()>0) {
                                xpathExpression = xpathExpressions[k];
                            nl = XPathAPI.selectNodeList(root, xpathExpression);
                            String[] rss = new String[4];
                            for (int ii=0;ii<nl.getLength();ii++) {
                                if (stopFlag) break;
                                rss[0] = filename;
                                rss[1] = rootname;
                                rss[2] = nl.item(ii).getNodeName();
                                Node cn = nl.item(ii).getFirstChild();  // assume 1st child is text node
                                if ((cn!=null)&&(cn.getNodeType()==Node.TEXT_NODE)) {
                                    rss[3] = cn.getNodeValue().trim();
                                }
                                else { rss[3]="";}
                                dtm.addRow(rss);
                            }
                            }
                        }
                    } // end !Andflag
                    if (Andflag) {   //handle "AND" here
                        AndResultFlag = true;
                        NodeList[] nls = new NodeList[xpathExpressions.length];
                        for (int k=0;k<xpathExpressions.length;k++) {
                            if (xpathExpressions[k].length()>0) {
                                xpathExpression = xpathExpressions[k];
                                nl = XPathAPI.selectNodeList(root, xpathExpression);
                                if (nl.getLength()==0) {  // one of search conditions failed
                                    AndResultFlag = false;
                                  //  break;
                                }
                            nls[k] = nl;
                            }
                        } // end xpathExpressions loop
            if (AndResultFlag) {   
                for (int kkk=0;kkk<xpathExpressions.length;kkk++) {
                    nl = nls[kkk];
                    if (nl!=null) {
                    String[] rss = new String[4];
                    for (int ii=0;ii<nl.getLength();ii++) {
                        if (stopFlag) break;
                            rss[0] = filename;
                            rss[1] = rootname;
                            rss[2] = nl.item(ii).getNodeName();
                            Node cn = nl.item(ii).getFirstChild();  // assume 1st child is text node
                            if ((cn!=null)&&(cn.getNodeType()==Node.TEXT_NODE)) {
                                rss[3] = cn.getNodeValue().trim();
                            }
                            else { rss[3]="";}
                            dtm.addRow(rss);
                    }
                    }
                }
                
            }
          }
          
        } // end multiple expression 'else'
    
        
        
      }
      catch (Exception e2)
      {
        System.err.println("selectNodeList threw: " + e2.toString() + " perhaps your xpath didn't select any nodes");
        e2.printStackTrace();
        return;
      }
    }
    else
    {
      System.out.println("Bad input args: " + filename + ", " + xpathExpression);
    }
    
        }
        
 
    if (Halt!=null) {
        Halt.setText(Haltname);
    }
      curtime = System.currentTimeMillis();
      System.out.println("total time: "+(int)(curtime-starttime));
    }
  
  public void setStopFlag() {
    stopFlag = true;
  } 
  
  public void run() {
    queryAll();  
  }
    
	//{{DECLARE_CONTROLS
	//}}
	
	
   // given a directory, return a vector of files it contains
   // including subdirectories
   private void getFiles(File directoryFile, Vector vec) {
	    String[] files = directoryFile.list();
	    
	    for (int i=0;i<files.length;i++)
        {
            String filename = files[i];
            File currentfile = new File(directoryFile, filename);
            if (currentfile.isDirectory()) {
                getFiles(currentfile,vec);  // recursive call to subdirecctories
            }
            if (currentfile.isFile()) {
                vec.addElement(currentfile);   
            }
        }
   }
   
}