/**
 *        Name: LocalQuery.java
 *     Purpose: A Class that carries out queries of local XML docuements
 *		 (searchs local collection of XML files)
 *   Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *     Authors: Dan Higgins
 *
 *     Version: '$Id: LocalQuery.java,v 1.17 2001-01-15 02:23:21 higgins Exp $'
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
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import com.arbortext.catalog.*;
import java.util.Vector;
// import java.util.PropertyResourceBundle;
import java.util.Hashtable;
import java.util.Enumeration;

import edu.ucsb.nceas.dtclient.*;

public class LocalQuery extends Thread 
{
    
    static Hashtable dom_collection;
    static Hashtable doctype_collection;
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
    Vector returnFields;
    Vector doctypes2bsearched;
    Vector dt2bReturned;
    String currentDoctype;
    int numcolsdelete = 3;
    
    static {
        dom_collection = new Hashtable();
        doctype_collection = new Hashtable();
    }
    
public LocalQuery() {
    this(null);
    ConfigXML config = new ConfigXML("config.xml");
    returnFields = config.get("returnfield");
    doctypes2bsearched = config.get("doctype");
    dt2bReturned = config.get("returndoc");
    local_dtd_directory = config.get("local_dtd_directory", 0);
    local_xml_directory = config.get("local_xml_directory", 0);
    int cnt;
    if (returnFields==null) {
        cnt = 0;
    }
    else {
        cnt = returnFields.size();
    }
    
    headers = new String[4+cnt];  // assume at least 4 fields returned
    headers[0] = "Doc ID";
    headers[1] = "Document Name";
    headers[2] = "Document Type";
    headers[3] = "Document Title";
    for (int i=0;i<cnt;i++) {
        headers[4+i] = getLastPathElement((String)returnFields.elementAt(i));
    }
    
        dtm = new DefaultTableModel(headers,0);
        RSTable = new JTable(dtm);
        TableColumnModel tcm = RSTable.getColumnModel();
        removeFirstNColumns(tcm,numcolsdelete);
        
 //   PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
 //   local_dtd_directory =(String)options.handleGetObject("local_dtd_directory");     // DFH
 //   local_xml_directory =(String)options.handleGetObject("local_xml_directory");     // DFH
    local_xml_directory = local_xml_directory.trim();
    xmlcatalogfile = local_dtd_directory.trim()+"/catalog"; 
    xpathExpression = "//*[contains(text(),\"NCEAS\")]";
    xmlFileFolder = local_xml_directory.trim();

		//{{INIT_CONTROLS
		//}}
	}

public LocalQuery(String xpathstring) {
    ConfigXML config = new ConfigXML("config.xml");
    returnFields = config.get("returnfield");
    doctypes2bsearched = config.get("doctype");
    dt2bReturned = config.get("returndoc");
    local_dtd_directory = config.get("local_dtd_directory", 0);
    local_xml_directory = config.get("local_xml_directory", 0);
    int cnt;
    if (returnFields==null) {
        cnt = 0;
    }
    else {
        cnt = returnFields.size();
    }
    
    headers = new String[4+cnt];  // assume at least 4 fields returned
    headers[0] = "Doc ID";
    headers[1] = "Document Name";
    headers[2] = "Document Type";
    headers[3] = "Document Title";
    for (int i=0;i<cnt;i++) {
        headers[4+i] = getLastPathElement((String)returnFields.elementAt(i));
    }
        dtm = new DefaultTableModel(headers,0);
        RSTable = new JTable(dtm);
        TableColumnModel tcm = RSTable.getColumnModel();
        removeFirstNColumns(tcm,numcolsdelete);

//    PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
//    local_dtd_directory =(String)options.handleGetObject("local_dtd_directory");     // DFH
//    local_xml_directory =(String)options.handleGetObject("local_xml_directory");     // DFH
    local_xml_directory = local_xml_directory.trim();
    xmlcatalogfile = local_dtd_directory.trim()+"/catalog"; 
//    xpathExpression = "//*[contains(text(),\"NCEAS\")]";
    xmlFileFolder = local_xml_directory.trim();
    this.xpathExpression = xpathstring;
}

public LocalQuery(String xpathstring, JButton button) {
    ConfigXML config = new ConfigXML("config.xml");
    returnFields = config.get("returnfield");
    doctypes2bsearched = config.get("doctype");
    dt2bReturned = config.get("returndoc");
    local_dtd_directory = config.get("local_dtd_directory", 0);
    local_xml_directory = config.get("local_xml_directory", 0);
    int cnt;
    if (returnFields==null) {
        cnt = 0;
    }
    else {
        cnt = returnFields.size();
    }
    
    headers = new String[4+cnt];  // assume at least 4 fields returned
    headers[0] = "Doc ID";
    headers[1] = "Document Name";
    headers[2] = "Document Type";
    headers[3] = "Document Title";
    for (int i=0;i<cnt;i++) {
        headers[4+i] = getLastPathElement((String)returnFields.elementAt(i));
    }
    Halt = button;
        dtm = new DefaultTableModel(headers,0);
        RSTable = new JTable(dtm);
        TableColumnModel tcm = RSTable.getColumnModel();
        removeFirstNColumns(tcm,numcolsdelete);

//    PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
//    local_dtd_directory =(String)options.handleGetObject("local_dtd_directory");     // DFH
//    local_xml_directory =(String)options.handleGetObject("local_xml_directory");     // DFH
    local_xml_directory = local_xml_directory.trim();
    xmlcatalogfile = local_dtd_directory.trim()+"/catalog"; 
//    xpathExpression = "//*[contains(text(),\"NCEAS\")]";
    xmlFileFolder = local_xml_directory.trim();
    this.xpathExpression = xpathstring;
}
    
public LocalQuery(String[] xpathstrings, boolean and_flag, JButton button) {
    ConfigXML config = new ConfigXML("config.xml");
    returnFields = config.get("returnfield");
    doctypes2bsearched = config.get("doctype");
    dt2bReturned = config.get("returndoc");
    local_dtd_directory = config.get("local_dtd_directory", 0);
    local_xml_directory = config.get("local_xml_directory", 0);
    int cnt;
    if (returnFields==null) {
        cnt = 0;
    }
    else {
        cnt = returnFields.size();
    }
    
    headers = new String[4+cnt];  // assume at least 4 fields returned
    headers[0] = "Doc ID";
    headers[1] = "Document Name";
    headers[2] = "Document Type";
    headers[3] = "Document Title";
    for (int i=0;i<cnt;i++) {
        headers[4+i] = getLastPathElement((String)returnFields.elementAt(i));
    }
    Halt = button;
        dtm = new DefaultTableModel(headers,0);
        RSTable = new JTable(dtm);
        TableColumnModel tcm = RSTable.getColumnModel();
        removeFirstNColumns(tcm,numcolsdelete);

//    PropertyResourceBundle options = (PropertyResourceBundle)PropertyResourceBundle.getBundle("client");  // DFH
//    local_dtd_directory =(String)options.handleGetObject("local_dtd_directory");     // DFH
//    local_xml_directory =(String)options.handleGetObject("local_xml_directory");     // DFH
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
                if (doctype_collection.containsKey(filename)) {
                    currentDoctype = ((String)doctype_collection.get(filename));   
                }
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
                String temp = getDocTypeFromDOM(current_doc);
                if (temp==null) temp = root.getNodeName();
                doctype_collection.put(filename,temp);
                currentDoctype = temp;
              }     
                String rootname = root.getNodeName();
                NodeList nl = null;
                try {
                   boolean search_flag = true;
                // first see if current doctype is in list of doctypes to be searched
                 if ((doctypes2bsearched.contains("any"))||(doctypes2bsearched.contains(currentDoctype))) {
                 }
                 else {
                    search_flag = false;
                 }
                if (search_flag) { 
                
                if (xpathExpressions == null) {   // a single xpath expression
                if (stopFlag) break;
                    // Use the simple XPath API to select a node.
                    nl = XPathAPI.selectNodeList(root, xpathExpression);
     /*               String[] rss = new String[4];
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
      */
                  if (nl.getLength()>0) {
                    addRowsToTable(filename);
                  }
                    
                }
                else {   // multiple expressions are handled here
                    if (stopFlag) break;
                    if (!Andflag) {
                        for (int k=0;k<xpathExpressions.length;k++) {
                            if (xpathExpressions[k].length()>0) {
                                xpathExpression = xpathExpressions[k];
                            nl = XPathAPI.selectNodeList(root, xpathExpression);
            /*                String[] rss = new String[4];
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
             */                   
                  if (nl.getLength()>0) {
                    addRowsToTable(filename);
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
     /*               if (nl!=null) {
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
       */
       
                  if (nl.getLength()>0) {
                    addRowsToTable(filename);
                  }
                }
                
            }
          }
          
        } // end multiple expression 'else'
    
        
                }  
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
	
  private void addRowsToTable(String hitfilename) {
    Vector temp = getResultSetDocs(hitfilename);
    for (Enumeration e = temp.elements();e.hasMoreElements();) {
        String fn = (String)e.nextElement();
        String[] row = createRSRow(fn);
        dtm.addRow(row);
    }   
  }
  
  // given the filename of a doc where a 'hit' has occured, return a vector of names of related docs	
  private Vector getResultSetDocs(String filename) {
    Vector result = new Vector();
    String currentDoctype = getDoctypeFor(filename);
    if (!dt2bReturned.contains("any")) {
    //first see if the current doc type is in return list
    if (dt2bReturned.contains(currentDoctype)) {
        result.addElement(filename);   
    }
    // now check if objects of relationship are of types to be returned
    Vector objs = getRelationshipObjects(filename);
    for (Enumeration e=objs.elements();e.hasMoreElements();) {
        String obj = (String)e.nextElement();
        if (dt2bReturned.contains(obj)) {
            result.addElement(obj);          
        }
    // now check if subjects of each object are of types to be returned i.e. backtracking
        Vector subs = getRelationshipSubjects(obj);  
        for (Enumeration w=objs.elements();w.hasMoreElements();) {
            String sub = (String)w.nextElement();
            if (dt2bReturned.contains(sub)) {
                result.addElement(sub);          
            }
        }
    }
    }
    else { // no dt2bReturned types
        result.addElement(filename);   
    }
  return result;  
  }

	private String getDoctypeFor(String filename) {
	    String ret = "";
	    if (doctype_collection.containsKey(filename)) {
	        ret = (String)doctype_collection.get(filename);   
	    }
	return ret;
	}
	
	private Vector getRelationshipSubjects(String obj) {
	    Vector ret = new Vector();
	    
	    return ret;
	}
	
	private Vector getRelationshipObjects(String sub) {
	    Vector ret = new Vector();
	    
	    return ret;
	}
	
	private String[] createRSRow(String filename) {
	    int cols = 4 + returnFields.size();
	    String[] rss = new String[cols];
	    rss[0] = filename;                         //ID
	    rss[1] = getLastPathElement(filename);     //docname
	    rss[2] = currentDoctype;                   // doctype
	    
	    rss[3] =  getValueForPath("//*/title",filename);  // title
	    if (rss[3].equals("")) { rss[3] = filename; }
	    for (int i=0;i<returnFields.size();i++) {
	        rss[4+i] = getValueForPath((String)returnFields.elementAt(i),filename);   
	    }
	return rss;
	}
	
	private String getValueForPath(String pathstring, String filename) {
	    String val = "";
	    try{
	    // assume that the filename file has already been parsed
        if (dom_collection.containsKey(filename)){
            Node doc = ((Document)dom_collection.get(filename)).getDocumentElement();
            NodeList nl = null;
            nl = XPathAPI.selectNodeList(doc, pathstring);
            if ((nl!=null)&&(nl.getLength()>0)) {
                Node cn = nl.item(0).getFirstChild();  // assume 1st child is text node
                    if ((cn!=null)&&(cn.getNodeType()==Node.TEXT_NODE)) {
                        val = cn.getNodeValue().trim();
                    }
                else { val="";}
            }
        }
        }
        catch (Exception e){System.out.println("Error in getValueForPath method");}
	return val;    
	}
	
	private String getDocTypeFromDOM(Document doc){
	    String ret = null;
	        DocumentType ddd = doc.getDoctype();
	        ret = ddd.getPublicId();
	        if (ret==null) {
	            ret = ddd.getSystemId();
	            if (ret==null){
	                ret = ddd.getName();   
	            }
	        }

	return ret;
	}
	
	
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
 
   private void addToRS(String filename) {
        Vector rsdocs = getResultSetDocs(filename);
        for (Enumeration e=rsdocs.elements();e.hasMoreElements();) {
            String name = (String)e.nextElement();
            String[] rowdata = createRSRow(name);
            dtm.addRow(rowdata);   
        }
   }
   
// use to get the last element in a path string
   private String getLastPathElement(String str) {
        String last = "";
        int ind = str.lastIndexOf("\\");
        if (ind==-1) {
           last = str;     
        }
        else {
           last = str.substring(ind+1);     
        }
        return last;
   }
 
  
  private void removeTableColumn( TableColumnModel tcm, int index) {
        int cnt = tcm.getColumnCount();
        if (index<cnt) {
            TableColumn tc = tcm.getColumn(index);
            tcm.removeColumn(tc);
        }
  }
  
  private void removeFirstNColumns(TableColumnModel tcm, int n) {
        // n is the number of leading columns to remove
        for (int i=0;i<n;i++) {
            removeTableColumn(tcm,0);
        }
  }
   
}
