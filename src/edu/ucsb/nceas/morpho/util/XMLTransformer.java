/**
 *  '$RCSfile: XMLTransformer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-11 15:53:45 $'
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
import java.io.InputStreamReader;

import java.net.URL;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Document;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;     
import org.xml.sax.ErrorHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.arbortext.catalog.Catalog;
import com.arbortext.catalog.CatalogEntityResolver;


/**
 *  XMLTransformer to style XML documents using XSLT
 */
public class XMLTransformer extends DefaultHandler
{

//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    private final String CONFIG_KEY_SAX_PARSER_NAME     = "saxparser";
    private final String CONFIG_KEY_LOCAL_CATALOG_PATH  = "local_catalog_path";
    private final String CONFIG_KEY_GENERIC_STYLESHEET  = "genericStylesheet";
    
    private final  ClassLoader      classLoader;
    private static XMLTransformer   instance;
    private static String           latestDocID;
    
    private XMLReader               xmlReader;
    private EntityResolver          entityResolver;
    private ConfigXML               config;


    private XMLTransformer() 
    {
        this.config = Morpho.getConfiguration();
        classLoader = this.getClass().getClassLoader();
        initEntityResolver();
//        try {
//            initXMLReader();
//        } catch (SAXException se) {
//            Log.debug(9,"XMLTransformer: error initializing XMLReader " + se);
//            se.printStackTrace();
//        }
    }
    
    /**
     *  Used to get a shared instance of the <code>XMLTransformer</code>
     *
     *  @return a shared instance of the <code>XMLTransformer</code>
     */    
    public static XMLTransformer getInstance()
    {
        if (instance==null) {
            instance=new XMLTransformer();
        }
        return instance;
    }
    
    /**
     *  transforms the passed XML document, using a "generic" stylesheet, whose
     *  name is obtained from the config.xml file
     *
     *  @param xmlDocReader A <code>java.io.Reader</code> to allow reading of
     *                      the XML document to be styled.  
     *
     *  @return             A <code>java.io.Reader</code> to allow reading of
     *                      the (character-based) results of styling the XML 
     *                      document
     *
     *  @throws IOException if there are problems reading the Reader
     */
    public Reader transform(Reader xmlDocReader) throws IOException
    {
        Log.debug(50,"XMLTransformer.transform(Reader xmlDocReader) called;\n"
                        +"xmlDocReader = "+xmlDocReader);            
        String stylesheet = config.get(CONFIG_KEY_GENERIC_STYLESHEET, 0);
        Reader xslInputReader 
            = new InputStreamReader(classLoader.getResourceAsStream(stylesheet));
        Reader returnReader = transform(xmlDocReader, xslInputReader);
        
        return returnReader;
    }
    
    /**
     *  Uses the stylesheet provided, to apply XSLT to the XML document provided
     *
     *  @param xmlDocReader  A <code>java.io.Reader</code> to allow reading of
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
    public Reader transform(Reader xmlDocReader, 
                                        Reader xslStyleSheet) throws IOException
    {
        Log.debug(50,"XMLTransformer.transform(Reader xmlDocReader, "
                        +"Reader xslStyleSheet) called;\n"
                        +"xmlDocReader = " +xmlDocReader            
                        +"xslStyleSheet = "+xslStyleSheet);            
        validateInputParam(xmlDocReader,   "XML document reader");
        validateInputParam(xslStyleSheet, "XSL stylesheet reader");
        return doTransform(xmlDocReader, xslStyleSheet);
    }
    
    //common method used to do transforms
    private synchronized Reader doTransform(Reader xmlDocReader, 
                                        Reader xslStyleSheet) throws IOException
    {
        PipedReader returnReader = new PipedReader();
        PipedWriter pipedWriter  = new PipedWriter();
        returnReader.connect(pipedWriter);

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer 
                    = tFactory.newTransformer(new StreamSource(xslStyleSheet));
        } catch (TransformerConfigurationException e) {
            String msg 
                = "XMLTransformer.transform(): getting Transformer instance. "
                    +"Nested TransformerConfigurationException="+e.getMessage();
            e.printStackTrace();
            Log.debug(12, msg);
            pipedWriter.write(msg.toCharArray(),0,msg.length());
            e.printStackTrace(new PrintWriter(pipedWriter));
        }
        
        transformer.setErrorListener(new CustomErrorListener());

        Document domDoc = null;
        try {
            domDoc = getDOMDocument(xmlDocReader);
        } catch (IOException e) {
            lazyThrow(e, "IOException");
        } catch (SAXException e) {
            lazyThrow(e, "SAXException");
        } catch (FactoryConfigurationError e) {
            lazyThrow(e, "FactoryConfigurationError");
        } catch (ParserConfigurationException e) {
            lazyThrow(e, "ParserConfigurationException");
        }
       
        System.err.println("@ @ @ @ @ domDoc public = "+domDoc.getDoctype().getPublicId());
        System.err.println("@ @ @ @ @ domDoc root elem = "+domDoc.getDocumentElement().getTagName());
        
        
//        try {
//            Log.debug(50,"XMLTransformer doing transformer.transform...");
//
//            transformer.transform(  getAsDOMSource(xmlDocReader),
//                                    new StreamResult(pipedWriter));

//            transformer.transform(  getAsSaxSource(xmlDocReader),
//                                    new StreamResult(pipedWriter));
//            Log.debug(50,"XMLTransformer DONE transformer.transform!");            
//        } catch (TransformerException e) {
//            String msg
//                = "XMLTransformer.transform(): Error transforming document."
//                                +" Nested TransformerException="+e.getMessage();
//            e.printStackTrace();
//            Log.debug(12, msg);
//            pipedWriter.write(msg.toCharArray(),0,msg.length());
//            e.printStackTrace(new PrintWriter(pipedWriter));
//        } catch (Exception e) {
//            String msg
//                = "XMLTransformer.transform(): Unrecognized Error transforming"
//                                +" document: "+e.getMessage();
//            e.printStackTrace();
//            Log.debug(12, msg);
//            pipedWriter.write(msg.toCharArray(),0,msg.length());
//            e.printStackTrace(new PrintWriter(pipedWriter));
//        } finally {
//            pipedWriter.flush();
//            pipedWriter.close();
//        }
        return returnReader;
    }


    //ensure passed parameter is non-null.  If is null, throws IOException, 
    //using "paramDescription" in exception message
    private void validateInputParam(Object param, String paramDescription)
                                                            throws IOException 
    {
        if (param==null) {
            String errMsg = "XMLTransformer received NULL parameter: "
                                                            +paramDescription;
            IOException exception = new IOException(errMsg);
            exception.fillInStackTrace();
            Log.debug(12,errMsg);
            throw exception;
        }
    }
    

    //initialize entity resolver class variable 
    private void initEntityResolver() 
    {
        CatalogEntityResolver catalogEntResolver = new CatalogEntityResolver();
        try {
            Catalog catalog = new InnerCatalog();
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

    
//    //  Create a SAXSource to enable the transformer to access the Reader
//    //  - Necessary because we need to set the entity resolver for this source, 
//    //  which isn't possible if we just use an InputSource
//    private SAXSource getAsSaxSource(Reader xmlDocReader) 
//    {   
//        InputSource source = new InputSource(xmlDocReader);
//        SAXSource saxSource = new SAXSource(source);
//        saxSource.setXMLReader(this.xmlReader);
//        return saxSource;
//    }

    
    private synchronized Document getDOMDocument(Reader inputXMLReader) 
                                            throws  IOException,
                                                    SAXException,
                                                    FactoryConfigurationError, 
                                                    ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        docBuilder.setEntityResolver(getEntityResolver());
        docBuilder.setErrorHandler(new CustomErrorHandler());
        InputSource source = new InputSource(inputXMLReader);
        Log.debug(50,"XMLTransformer: InputSource = "+source.toString());
        return docBuilder.parse(source);
    }
    
    
//    /* Set up the SAX parser for reading the XML serialized ACL */
//    private void initXMLReader() throws SAXException
//    {
//      // Get an instance of the xmlReader
//        xmlReader = XMLReaderFactory.createXMLReader(
//                                    config.get(CONFIG_KEY_SAX_PARSER_NAME, 0));
//        xmlReader.setFeature("http://xml.org/sax/features/validation", true);
//        xmlReader.setContentHandler(this);
//        xmlReader.setEntityResolver(getEntityResolver());
//        xmlReader.setErrorHandler(new CustomErrorHandler());
//    }

    
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
    
    
    class CustomErrorHandler implements ErrorHandler
    {   
        public void error(SAXParseException exception) throws SAXException
        {
            handleException(exception, "SAX ERROR");
        }
        
        public void fatalError(SAXParseException exception) throws SAXException 
        {
            handleException(exception, "SAX FATAL ERROR");
        }
        
        public void warning(SAXParseException exception) throws SAXException 
        {
            handleException(exception, "SAX WARNING");
        }
        
        private void handleException(SAXParseException exception,
                                        String errMessage) throws SAXException
        {
            Log.debug(9, "XMLTransformer$CustomErrorHandler: " 
                                    + errMessage+": " + exception.getMessage());
            exception.fillInStackTrace();
            throw ((SAXException)exception);
        }
    }
    

    class CustomErrorListener implements ErrorListener
    {   
        public void error(TransformerException exception) 
                                                    throws TransformerException
        {
            handleException(exception, "TRANSFORMATION ERROR");
        }
        
        public void fatalError(TransformerException exception) 
                                                    throws TransformerException 
        {
            handleException(exception, "TRANSFORMATION FATAL ERROR");
        }
        
        public void warning(TransformerException exception) 
                                                    throws TransformerException 
        {
            handleException(exception, "TRANSFORMATION WARNING");
        }
        
        private void handleException(TransformerException exception,
                                String errMessage) throws TransformerException
        {
            Log.debug(9, "\n* * * XMLTransformer$CustomErrorListener: " 
                                    + errMessage+": " + exception.getMessage());
            exception.fillInStackTrace();
            throw ((TransformerException)exception);
        }
    }


    class InnerCatalog extends Catalog 
    {
        public String resolvePublic(String publicID, String systemID) 
                                      throws MalformedURLException, IOException
        {
            String resolution = super.resolvePublic(publicID, systemID);
            Log.debug(50,"InnerCatalog.resolvePublic(): "+resolution);
//            URL testURL = new URL("jar:file:/D:/_PROJECTS_/_ N C E A S _/MORPHO_ROOT/CVS_CHECKOUTS/morpho/lib/morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd");
//            URL resolvedURL = new URL(resolution);
//            Log.debug(50,"InnerCatalog.resolvedURL.getFile: "+resolvedURL.getFile());
//            Log.debug(50,"InnerCatalog.testURL.getFile:     "+testURL);
//            Log.debug(50,"InnerCatalog.resolvePublic():sameFile?: "+resolvedURL.sameFile(testURL));
            XMLTransformer.latestDocID = resolution;
            return resolution;

//            <listdoctypes>
//              <entitydoctype>-//ecoinformatics.org//eml-entity-2.0.0beta6//EN</entitydoctype>
//              <resourcedoctype>-//ecoinformatics.org//eml-dataset-2.0.0beta6//EN</resourcedoctype>
//              <attributedoctype>-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN</attributedoctype>
//              <entitydoctype>-//ecoinformatics.org//eml-entity-2.0.0beta4//EN</entitydoctype>
//              <resourcedoctype>-//ecoinformatics.org//eml-dataset-2.0.0beta4//EN</resourcedoctype>
//              <attributedoctype>-//ecoinformatics.org//eml-attribute-2.0.0beta4//EN</attributedoctype>
//            </listdoctypes>
        }
    }
    
    private void lazyThrow(Throwable e, String type) throws IOException 
    {
        String msg 
            = "XMLTransformer.doTransform(): getting DOM document. "
                +"Nested "+type+" = "+e.getMessage()+"\n";
        e.fillInStackTrace();
        e.printStackTrace();
        Log.debug(12, msg);
        throw new IOException(msg);
    }
    
}


    /**
     *  Uses the stylesheet provided, to apply XSLT to the XML document provided
     *
     *  @param xmlDocReader  A <code>java.io.Reader</code> to allow reading of
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
//    public Reader transform(Reader xmlDocReader, XSLLookupInterface directory)
//                                  throws IOException, DocumentNotFoundException
//    {
//        return new StringReader("XMLTransformer: method not implemented!");
//    }