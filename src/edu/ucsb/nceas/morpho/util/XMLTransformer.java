/**
 *  '$RCSfile: XMLTransformer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-05 01:02:56 $'
 * '$Revision: 1.4 $'
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

package edu.ucsb.nceas.morpho.util;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;

import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.PipedReader;
import java.io.PipedWriter;

import java.net.URL;
//import java.net.MalformedURLException;
//import java.sql.*;
//import java.util.Stack;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXSource;
//
//import org.apache.xerces.parsers.DOMParser;
//import org.w3c.dom.Attr;
//import org.w3c.dom.NamedNodeMap;
//import org.w3c.dom.NodeList;
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.w3c.dom.DocumentType;
//import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
//import org.apache.xerces.dom.DocumentTypeImpl;
//import org.apache.xpath.XPathAPI;
//import org.w3c.dom.NamedNodeMap;
//
//import org.w3c.dom.Document;
//import org.w3c.dom.Node;
//import org.w3c.dom.Element;
import org.xml.sax.SAXException;     
import org.xml.sax.XMLReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.ContentHandler;

import com.arbortext.catalog.Catalog;
import com.arbortext.catalog.CatalogEntityResolver;

//
//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *



/**
 *  XMLTransformer to style XML documents using XSLT
 */
public class XMLTransformer extends DefaultHandler
{
    private final String SAX_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    private final String CONFIG_KEY_LOCAL_CATALOG_PATH = "local_catalog_path";
    
    private static XMLTransformer   instance;
    private static XMLReader        xmlReader;
    
    private EntityResolver          entityResolver;
    private ConfigXML               config;
    
    
    private XMLTransformer() 
    {
        this.config = Morpho.getConfiguration();
        initEntityResolver();
        try {
            initXMLReader();
        } catch (SAXException se) {
            Log.debug(9,"XMLTransformer: error initializing XMLReader " + se);
            se.printStackTrace();
        }
    }
    
    public static XMLTransformer getInstance() 
    {
        if (instance==null) {
            instance=new XMLTransformer();
        }
        return instance;
    }
    
    /**
     *  transforms the passed XML document according to a stylesheet reference 
     *  data contained within it
     *
     *  @param xmlDocument  A <code>java.io.Reader</code> to allow reading of
     *                      the XML document to be styled.  Note XML doc must 
     *                      contain a stylesheet reference so this class can 
     *                      determine which stylesheet to use
     *
     *  @return             A <code>java.io.Reader</code> to allow reading of
     *                      the (character-based) results of styling the XML 
     *                      document
     *
     *  @throws IOException if there are problems reading the Reader
     */
    public Reader transform(Reader xmlDocument) throws IOException
    {
        throw new IOException("XMLTransformer: method not implemented!");
    }
    
    /**
     *  Uses the stylesheet provided, to apply XSLT to the XML document provided
     *
     *  @param xmlDocument  A <code>java.io.Reader</code> to allow reading of
     *                      the XML document to be styled.
     *
     *  @param xslStyleSheet  A <code>java.io.Reader</code> to allow reading of
     *                      the XSL stylesheet to be used
     *
     *  @return             A <code>java.io.Reader</code> to allow reading of
     *                      the results of styling the XML document
     *
     *  @throws IOException if there are problems reading either of the Readers
     */
    public Reader transform(Reader xmlDocument, Reader xslStyleSheet)
                                                            throws IOException
    {
        validateInputParam(xmlDocument,   "XML document reader");
        validateInputParam(xslStyleSheet, "XSL stylesheet reader");

//        PipedWriter pipedWriter = new PipedWriter();
        StringWriter pipedWriter = new StringWriter();

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = null;

        try {
            transformer = tFactory.newTransformer(new StreamSource(xslStyleSheet));
        } catch (TransformerConfigurationException e) {
            String msg 
                = "XMLTransformer.transform(): getting Transformer instance. "
                    +"Nested TransformerConfigurationException="+e.getMessage();
            Log.debug(12, msg);
            pipedWriter.write(msg.toCharArray(),0,msg.length());
            e.printStackTrace(new PrintWriter(pipedWriter));
        }
        
//      transformer.setOutputProperty(OutputKeys.METHOD , outputDocType);
//      transformer.setParameter("qformat", qformat);

        try {
            transformer.transform(  getAsSaxSource(xmlDocument), 
                                    new StreamResult(pipedWriter));

//            transformer.transform(  new StreamSource(xmlDocument), 
//                                    new StreamResult(stringWriter));
        } catch (TransformerException e) {
            String msg 
                = "XMLTransformer.transform(): Error transforming document."
                                +" Nested TransformerException="+e.getMessage();
            Log.debug(12, msg);
            pipedWriter.write(msg.toCharArray(),0,msg.length());
            e.printStackTrace(new PrintWriter(pipedWriter));
        }
//        return new PipedReader(pipedWriter);
        return new StringReader(pipedWriter.toString());
    }
    
    /**
     *  Uses the stylesheet provided, to apply XSLT to the XML document provided
     *
     *  @param xmlDocument  A <code>java.io.Reader</code> to allow reading of
     *                      the XML document to be styled.
     *
     *  @param directory    A <code>XSLLookupInterface</code> to allow this
     *                      transformer to get the required XSL document, given
     *                      the DocType in the XML doc to be styled
     *
     *  @return             A <code>java.io.Reader</code> to allow reading of
     *                      the results of styling the XML document
     *
     *  @throws IOException if there are problems reading the Reader
     *  @throws DocumentNotFoundException if XSLLookupInterface does not return 
     *  a valid reference to a suitable XSL stylesheet
     */
//    public Reader transform(Reader xmlDocument, XSLLookupInterface directory)
//                                  throws IOException, DocumentNotFoundException
//    {
//        return new StringReader("XMLTransformer: method not implemented!");
//    }

    private void validateInputParam(Object param, String paramDescription) 
                                                            throws IOException 
    {

        if (param==null) {
            String errMsg = "XMLTransformer received NULL parameter: "
                                                            +paramDescription;
            IOException exception 
                = new IOException(errMsg);
            exception.fillInStackTrace();
            Log.debug(12,errMsg);
            throw exception;
        }
    }
    
    
    private void initEntityResolver() 
    {
        CatalogEntityResolver catalogEntResolver = new CatalogEntityResolver();
        try {
            Catalog catalog = new Catalog();
            catalog.loadSystemCatalogs();
            String catalogPath = config.get(CONFIG_KEY_LOCAL_CATALOG_PATH, 0);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            URL catalogURL = cl.getResource(catalogPath);

            catalog.parseCatalog(catalogURL.toString());
            catalogEntResolver.setCatalog(catalog);
        } catch (Exception e) {
            Log.debug(9,"XMLTransformer: error creating Catalog "+e.toString());
        }
        this.entityResolver = (EntityResolver)catalogEntResolver;
    }
    
    private SAXSource getAsSaxSource(Reader xmlDocument) 
    {   
        InputSource source = new InputSource(xmlDocument);
        SAXSource saxSource = new SAXSource(source);
        saxSource.setXMLReader(this.xmlReader);
        return saxSource;
    }
    
    /* Set up the SAX parser for reading the XML serialized ACL */
    private void initXMLReader() throws SAXException 
    {
      // Get an instance of the xmlReader
      xmlReader = XMLReaderFactory.createXMLReader(SAX_PARSER_NAME);
  
      xmlReader.setFeature("http://xml.org/sax/features/validation", true);
        
      xmlReader.setContentHandler((ContentHandler)this);
  
      xmlReader.setEntityResolver(getEntityResolver());
  
//      xmlReader.setErrorHandler((ErrorHandler)this);
    }
    
    
    /**
     *  Sets the <code>EntityResolver</code> for the XML parser that will be 
     *  used by this transformer.  This is used to resolve PUBLIC and SYSTEM 
     *  DOCIDs
     *
     *  @param resolver     the <code>EntityResolver</code> to be used
     */
    public void setEntityResolver(EntityResolver resolver) 
    {
        this.entityResolver = resolver;
    }

    /**
     *  Gets the <code>EntityResolver</code> from the XML parser that will be 
     *  used by this transformer.  This is used to resolve PUBLIC and SYSTEM 
     *  DOCIDs
     *
     *  @return             the <code>EntityResolver</code> to be used
     */
    public EntityResolver getEntityResolver() 
    {
        return this.entityResolver;
    }
}