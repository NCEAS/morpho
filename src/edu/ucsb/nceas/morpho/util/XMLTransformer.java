/**
 *  '$RCSfile: XMLTransformer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2005-07-01 16:35:51 $'
 * '$Revision: 1.31 $'
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

import edu.ucsb.nceas.utilities.XMLUtilities;

import edu.ucsb.nceas.morpho.plugins.XSLTResolverInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.DocumentNotFoundException;

import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.Iterator;
import java.util.Properties;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.URIResolver;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXParseException;

import com.arbortext.catalog.Catalog;
import com.arbortext.catalog.CatalogEntityResolver;


/**
 *  XMLTransformer to style XML documents using XSLT
 */
public class XMLTransformer
{

//  * * * * * * * S T A T I C    V A R I A B L E S * * * * * * *

    /**
     *  XSL Transformer properties KEYS for setting properties used during
     *  transforms (i.e. KEY part of KEY/VALUE pairs)
     */

    /**
     *  SELECTED_DISPLAY_XSLPROP used to identify the display type
     */
    public static final String SELECTED_DISPLAY_XSLPROP = "displaymodule";

    /**
     *  XSLVALU_DISPLAY_DATASET is the value to display dataset
     */
    public static final String XSLVALU_DISPLAY_DATASET = "dataset";

    /**
     *  XSLVALU_DISPLAY_ENTITY is the value to display entity
     */
    public static final String XSLVALU_DISPLAY_ENTITY = "entity";

    /**
     *  XSLVALU_DISPLAY_ATTRB is the value to display attributes
     */
    public static final String XSLVALU_DISPLAY_ATTRB = "attributedetail";

    /**
     *  XSLVALU_DISPLAY_PRNT is the value to display print output
     */
    public static final String XSLVALU_DISPLAY_PRNT = "printall";

    /**
     *  SELECTED_ATTRIBS_XSLPROP used to identify selected entity when
     *   clicking on col headers
     */
    public static final String SELECTED_ENTITY_XSLPROP = "entityindex";

    /**
     *  SELECTED_ATTRIBS_XSLPROP used to identify selected attribute(s) when
     *   clicking on col headers (NOTE - identified by column index (0..n) - not
     *   by attribute ID:
     */
    public static final String SELECTED_ATTRIBS_XSLPROP = "attributeindex";

    /**
     *  used to hold a list of all module ID(s) to be suppressed as triples
     *  SUBJECTS in DataPackage metaview.
     */
    public static final String SUPPRESS_TRIPLES_SUBJECTS_XSLPROP
                                                = "suppress_subjects_identifier";

    /**
     *  used to hold a list of all module ID(s) to be suppressed as triples
     *  OBJECTS in DataPackage metaview.
     */
    public static final String SUPPRESS_TRIPLES_OBJECTS_XSLPROP
                                                = "suppress_objects_identifier";


    /**
     *  used to delimit the list of all module ID(s) to be suppressed in
     *  DataPackage metaview.
     *  @see    <ul><li>SUPPRESS_TRIPLES_SUBJECTS_XSLPROP</li>
     *              <li>SUPPRESS_TRIPLES_OBJECTS_XSLPROP</li><ul>
     */
    public static final String SUPPRESS_TRIPLES_DELIMETER
                                                = " ";

    /**
     *  xsl property used to hold package index name
     */
    public static final String PACKAGE_INDEX_NAME_XSLPROP
                                                = "package_index_name";

    /**
     *  xsl property used to hold package id
     */
    public static final String PACKAGE_ID_XSLPROP = "package_id";

    /**
     *  xsl property used to hold default css stylesheet name
     */
    public static final String DEFAULT_CSS_XSLPROP = "qformat";

    /**
     *  xsl property used to hold entity/attribute css stylesheet name
     */
    public static final String ENTITY_CSS_XSLPROP = "entitystyle";

    /**
     *  xsl property used to hold path to css stylesheets
     */
    public static final String CSS_PATH_XSLPROP = "stylePath";

    /**
     *  used to hold path extension for href links in triples.
     *  Value SHOULD INCLUDE "." - i.e. typically set to ".html" for export
     *  files, and set to empty string for links inside Morpho UI
     */
    public static final String HREF_PATH_EXTENSION_XSLPROP
                                                = "href_path_extension";


    /**
     *  used by the transform() method to get the schemaLocation (if one has
     *  been defined) from the Document root node
     */
    public static final String NAMESPACE_FOR_SCHEMA_LOCATION
                                = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     *  used by the transform() method to get the schemaLocation (if one has
     *  been defined) from the Document root node
     */
    public static final String ATTRIB_NAME_FOR_SCHEMA_LOCATION
                                = "schemaLocation";

    /**
     *  used by the transform() method to get the schemaLocation (if one has
     *  been defined) from the Document root node
     */
    public static final String ID_TO_GET_GENERIC_STYLESHEET
                                = "unidentified";



//  * * * * * * * C L A S S    V A R I A B L E S * * * * * * *

    private final String CONFIG_KEY_LOCAL_CATALOG_PATH  = "local_catalog_path";
    private final String CONFIG_KEY_GENERIC_STYLESHEET  = "genericStylesheet";
    private final String CONFIG_KEY_GENERIC_LOCATION    = "genericStylesheetLocation";
    private final String GENERIC_STYLESHEET;
    private final String GENERIC_LOCATION;

    private final  ClassLoader      classLoader;
    private static XMLTransformer   instance;
    private static String           latestDocID;
    private static XSLTResolverInterface  resolver;

    private Properties              transformerProperties;
    private ConfigXML               config;

    private XMLTransformer()
    {
        this.config = Morpho.getConfiguration();
        GENERIC_STYLESHEET = config.get(CONFIG_KEY_GENERIC_STYLESHEET, 0);
        GENERIC_LOCATION   = config.get(CONFIG_KEY_GENERIC_LOCATION, 0);

        Log.debug(30, "XMLTransformer: ClassLoader *would* have been: "
                            + this.getClass().getClassLoader().getClass().getName());

        classLoader = Morpho.class.getClassLoader();
        Log.debug(30, "XMLTransformer: ...but from Morpho, setting ClassLoader to: "
                                                + classLoader.getClass().getName());
        Thread t = Thread.currentThread();
        t.setContextClassLoader(classLoader);

        transformerProperties = new Properties();
    }

    /**
     *  Used to get a shared instance of the <code>XMLTransformer</code>
     *
     *  @return a shared instance of the <code>XMLTransformer</code>
     */
    public static XMLTransformer getInstance()
    {
        if (instance==null) instance=new XMLTransformer();

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
        Log.debug(50,"XMLTransformer.transform(Reader xmlDocReader) called");
        validateInputParam(xmlDocReader,  "XML document reader");

        Document doc = null;
        try {
            doc = getAsDOMDocument(xmlDocReader);
        } catch (IOException e) {
            throwIOException(e, "IOException");
        } catch (SAXException e) {
            throwIOException(e, "SAXException");
        } catch (FactoryConfigurationError e) {
            throwIOException(e, "FactoryConfigurationError");
        } catch (ParserConfigurationException e) {
            throwIOException(e, "ParserConfigurationException");
        }
        return transform(doc);
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
    public Reader transform(Reader xmlDocReader, Reader xslStyleSheet, String xsltLocation)
                                                              throws IOException
    {
        Log.debug(50,"XMLTransformer.transform(Reader, Reader) called");
        validateInputParam(xmlDocReader,  "XML document reader");
        validateInputParam(xslStyleSheet, "XSL stylesheet reader");
        Document doc = null;
        try {
            doc = getAsDOMDocument(xmlDocReader);
        } catch (IOException e) {
            throwIOException(e, "IOException");
        } catch (SAXException e) {
            throwIOException(e, "SAXException");
        } catch (FactoryConfigurationError e) {
            throwIOException(e, "FactoryConfigurationError");
        } catch (ParserConfigurationException e) {
            throwIOException(e, "ParserConfigurationException");
        }
        return transform(doc, xslStyleSheet, xsltLocation);
    }


    /**
     *  transforms the passed XML document, using a "generic" stylesheet, whose
     *  name is obtained from the config.xml file
     *
     *  @param domDoc       A <code>javax.xml.parsers.Document</code>
     *                      containing the XML document to be styled.
     *
     *  @return             A <code>java.io.Reader</code> to allow reading of
     *                      the (character-based) results of styling the XML
     *                      document
     *
     *  @throws IOException if there are problems transforming the Document
     */
    public Reader transform(Document domDoc) throws IOException
    {
        Log.debug(50,"XMLTransformer.transform(Reader xmlDocReader) called");
        validateInputParam(domDoc,        "XML DOM Document");

        Element rootNode = domDoc.getDocumentElement();
        Log.debug(50,"domDoc is: "+XMLUtilities.getDOMTreeAsString(rootNode));

        String identifier = null;

        //first try to get public DOCTYPE:
        if (domDoc.getDoctype()!=null) {
          identifier = domDoc.getDoctype().getPublicId();
          Log.debug(50,"getPublicId() gives: "+identifier);
        }
        //if this is null, then try to get schemaLocation:
        if (identifier==null || identifier.trim().equals("")) {
          identifier = rootNode.getAttributeNS(NAMESPACE_FOR_SCHEMA_LOCATION,
                                               ATTRIB_NAME_FOR_SCHEMA_LOCATION);
          // since schema location string may contain multiple substrings
          // separated by spaces, we take only the first of these substrings:
          if ((identifier!=null) && ( !identifier.trim().equals(""))) {
            identifier = identifier.trim().substring(0, identifier.indexOf(" "));
            Log.debug(50,"getAttributeNS schemaLocation is: "+identifier);
          }
        }

        //if this is null, then try to get namespace of root node:
        if (identifier==null || identifier.trim().equals("")) {

          identifier = rootNode.getNamespaceURI();
          Log.debug(50,"rootNode.getNamespaceURI() gives: "+identifier);
        }

        //finally, if this is null, use generic stylesheet:
        if (identifier==null || identifier.trim().equals("")) {

          identifier = ID_TO_GET_GENERIC_STYLESHEET;
          Log.debug(50,"no identifier - requesting generic stylesheet");
        }


        return transform( domDoc, getStyleSheetReader(identifier), getStyleSheetLocation(identifier));
    }



    /**
     *  Uses the stylesheet provided, to apply XSLT to the XML DOM Document
     *  provided
     *
     *  @param domDoc         A <code>javax.xml.parsers.Document</code>
     *                        containing the XML document to be styled.
     *
     *  @param xslStyleSheet  A <code>java.io.Reader</code> to allow reading of
     *                      the XSL stylesheet to be used
     *
     *  @return               A <code>java.io.Reader</code> to allow reading of
     *                        the results of styling the XML document
     *
     *  @throws IOException if there are problems reading either of the Readers
     */
    public Reader transform(Document domDoc, Reader xslStyleSheet, String xsltLocation)
                                                              throws IOException
    {
        Log.debug(50,"XMLTransformer.transform(Document, Reader) called");
        validateInputParam(domDoc,        "XML DOM Document");
        validateInputParam(xslStyleSheet, "XSL stylesheet reader");

        return doTransform( getAsDOMSource(domDoc), xslStyleSheet, xsltLocation);
    }

    //common method used to do transforms
    private synchronized Reader doTransform(Source source,
                                        Reader xslStyleSheet, String xsltLocation) throws IOException
    {
        Log.debug(50,"--> XMLTransformer.doTransform(Source, Reader) called;"
                        +"\n    Source = "       +source
                        +"\n    xslStyleSheet = "+xslStyleSheet);

        CharArrayWriter outputWriter  = new CharArrayWriter();

        TransformerFactory tFactory = TransformerFactory.newInstance();
        tFactory.setURIResolver(new CustomURIResolver(xsltLocation));

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
            outputWriter.write(msg.toCharArray(),0,msg.length());
            e.printStackTrace(new PrintWriter(outputWriter));
        }
        transformer.setErrorListener(new CustomErrorListener());
        Enumeration propertyNames = getTransformerPropertyNames();

        while (propertyNames.hasMoreElements()) {

            String nextProp = (String)propertyNames.nextElement();
            transformer.setParameter(nextProp, getTransformerProperty(nextProp));
        }

        try {
            Log.debug(50,"XMLTransformer doing transformer.transform...");

            transformer.transform(  source,
                                    new StreamResult(outputWriter));
            Log.debug(50,"XMLTransformer DONE transformer.transform!");
        } catch (TransformerException e) {
            String msg
                = "XMLTransformer.transform(): Error transforming document."
                                +" Nested TransformerException="+e.getMessage();
            e.printStackTrace();
            Log.debug(12, msg);
            outputWriter.write(msg.toCharArray(),0,msg.length());
            e.printStackTrace(new PrintWriter(outputWriter));
        } catch (Exception e) {
            String msg
                = "XMLTransformer.transform(): Unrecognized Error transforming"
                                +" document: "+e.getMessage();
            e.printStackTrace();
            Log.debug(12, msg);
            outputWriter.write(msg.toCharArray(),0,msg.length());
            e.printStackTrace(new PrintWriter(outputWriter));
        } finally {
            outputWriter.flush();
            outputWriter.close();
        }
        return new StringReader(outputWriter.toString());
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


    //returns a new DOMSource based on the passed DOM Document
    private DOMSource getAsDOMSource(Document domDoc)
    {
        return new DOMSource(domDoc); //, domDoc.getDoctype().getSystemId());
    }


    //  Create a DOM Document to enable the transformer to access the Reader
    //  - Necessary because we need to set the entity resolver for this source,
    //  which isn't possible if we just use a StreamSource to read the Reader
    private synchronized Document getAsDOMDocument(Reader xmlDocReader)
                                            throws  IOException,
                                                    SAXException,
                                                    FactoryConfigurationError,
                                                    ParserConfigurationException
    {
        InputSource source = new InputSource(xmlDocReader);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);

        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        docBuilder.setErrorHandler(new CustomErrorHandler());

        return docBuilder.parse(source);
    }


    /**
     *  adds a name/value pair to the <code>Properties</code> object containing
     *  the properties to be set for the transformer
     *
     *  @param key        the key to be used
     *
     *  @param value      the corresponding value to be set
     */
    public void addTransformerProperty(String key, String value)
    {
        if (key==null || value==null) return;
        if (this.transformerProperties.contains(key)) return;
        this.transformerProperties.setProperty(key, value);
    }

    /**
     *  removes a name/value pair from the <code>Properties</code> object
     *  containing the properties to be set for the transformer
     *
     *  @param key        the key of the entry to be removed
     */
    public void removeTransformerProperty(String key)
    {
        if (key==null) return;
        this.transformerProperties.remove(key);
    }

    /**
     *  removes <em>all</em> name/value pairs from the <code>Properties</code>
     *  object containing the properties to be set for the transformer
     */
    public void removeAllTransformerProperties()
    {
        this.transformerProperties.clear();
    }

    /**
     *  The <code>Properties</code> object passed to this method will be used to
     *  set <em>all</em> the name/value pairs describing the properties to be
     *  set for the transformer.  The new settings will supersede any previous
     *  settings
     *
     *  @param newProps     the <code>Properties</code> containing all the
     *                      object name/value pairs describing the properties to
     *                      be set for the transformer
     */
    public void setTransformerProperties(Properties newProps)
    {
        this.transformerProperties = newProps;
    }

    /**
     *  Returns <em>all</em> name/value pairs describing the properties to be
     *  set for the transformer, in the form of a <code>Properties</code> object
     *
     *  *NOTE* this method returns a clone of the Properties, not a reference to
     *  the Properties object itself!
     *
     *  @return             A clone of the <code>Properties</code> containing
     *                      object name/value pairs describing the properties to
     *                      be set for the transformer
     */
    public Properties getCurrentTransformerProperties()
    {
        Properties returnProps = new Properties();
        if (this.transformerProperties.isEmpty()) return returnProps;
        Iterator keys = this.transformerProperties.keySet().iterator();
        String nextKey = null;
        while (keys.hasNext()) {
            nextKey = String.valueOf(keys.next());
            returnProps.setProperty(String.valueOf(nextKey),
                              this.transformerProperties.getProperty(nextKey));
        }
        Log.debug(50,
        "XMLTransformer.getCurrentTransformerProperties() returning "+returnProps);
        return returnProps;
    }


    /**
     *  Gets an <code>Enumeration</code> containing the names (keys of all the
     *  properties to be set for the transformer
     *
     *  @return             an <code>Enumeration</code> containing the names
     */
    public Enumeration getTransformerPropertyNames()
    {
        return this.transformerProperties.propertyNames();
    }

    /**
     *  Gets the <code>String</code> value of the Property corresponding to the
     *  key passed in
     *
     *  @return             the <code>String</code> value of the Property
     *                      corresponding to the key passed in
     */
    public String getTransformerProperty(String key)
    {
        if (key==null) return null;
        return this.transformerProperties.getProperty(key);
    }



    private void throwIOException(Throwable e, String type) throws IOException
    {
        String msg
            = "\nXMLTransformer - IOException. "
                +"Nested "+type+" = "+e.getMessage()+"\n";
        e.printStackTrace();
        Log.debug(12, msg);
        IOException ioe = new IOException(msg);
        ioe.fillInStackTrace();
        throw ioe;
    }

    //returns a new Reader to access the stylesheet
    private Reader getStyleSheetReader(String identifier) throws IOException
    {
        try {
            getXSLTResolverService();
        } catch(ServiceNotHandledException ee) {
          Log.debug(0, "Error acquiring XSLT Resolver plugin: " + ee);
          ee.printStackTrace();
          return new InputStreamReader(
                          classLoader.getResourceAsStream(GENERIC_STYLESHEET));
        }
        Reader xsltReader = null;
        try {
            xsltReader = resolver.getXSLTStylesheetReader(identifier);
        } catch (DocumentNotFoundException d) {
            String msg
                = "XMLTransformer.getStyleSheetReader(): "
                    +"Nested DocumentNotFoundException = "+d.getMessage()+"\n";
            d.printStackTrace();
            Log.debug(12, msg);
            IOException ioe = new IOException(msg);
            ioe.fillInStackTrace();
            throw ioe;
        }
        return xsltReader;
    }

    //returns the location to access the stylesheet
    private String getStyleSheetLocation(String identifier)
    {
      try {
        getXSLTResolverService();
      } catch(ServiceNotHandledException ee) {
        Log.debug(0, "Error acquiring XSLT Resolver plugin: " + ee);
        ee.printStackTrace();
        return GENERIC_LOCATION;
      }
      return resolver.getXSLTStylesheetLocation(identifier);
    }

    private XSLTResolverInterface getXSLTResolverService()
                                              throws ServiceNotHandledException
    {
        if (resolver==null) {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider =
                      services.getServiceProvider(XSLTResolverInterface.class);
            resolver = (XSLTResolverInterface)provider;
        }
        return resolver;
    }


////////////////////////////////////////////////////////////////////////////////
//////////////            I N N E R   C L A S S E S          ///////////////////
////////////////////////////////////////////////////////////////////////////////



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
            //do popup
            Log.debug(2, "Error reading documentation files;\n"
                                                      + exception.getMessage());
            //send more info to log
            Log.debug(12, "XMLTransformer$CustomErrorHandler: "
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
            //do popup
            Log.debug(2, "Error reading documentation files;\n"
                                                      + exception.getMessage());
            //send more info to log
            Log.debug(12, "\n* * * XMLTransformer$CustomErrorListener: "
                                    + errMessage+": " + exception.getMessage());
            exception.fillInStackTrace();
            throw ((TransformerException)exception);
        }
    }


    class CustomURIResolver implements URIResolver
    {
        private final String STYLESHEET_LOCATION;
        private final StringBuffer resolution;

        protected CustomURIResolver()
        {
            STYLESHEET_LOCATION = config.get(CONFIG_KEY_GENERIC_LOCATION, 0);
            resolution = new StringBuffer();
        }

        protected CustomURIResolver(String location)
        {
            STYLESHEET_LOCATION = location;
            resolution = new StringBuffer();
        }

        public Source resolve(String href, String base)
                                                    throws TransformerException
        {
            Log.debug(50,"CustomURIResolver.resolve() received href="+href
                                                        +" and base="+base);
            resolution.delete(0,resolution.length());
            resolution.append(STYLESHEET_LOCATION);
            resolution.append("/");
            resolution.append(href);

            InputStream stream
                      = classLoader.getResourceAsStream(resolution.toString());
            Log.debug(50,"CustomURIResolver.resolve() returning StreamSource \n"
                                        +"for InputStream = "+stream
                                        +"\nfor path = "+resolution.toString());
            return new StreamSource(stream);
        }
    }



    class InnerCatalog extends Catalog
    {
        public String resolvePublic(String publicID, String systemID)
                                      throws MalformedURLException, IOException
        {
            String resolution = super.resolvePublic(publicID, systemID);
            Log.debug(50,"InnerCatalog.resolvePublic(): "+resolution);
            XMLTransformer.latestDocID = resolution;
            return resolution;
        }
    }

}


////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//////////////             S P A R E    P A R T S            ///////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    //  Create a SAXSource to enable the transformer to access the Reader
    //  - Necessary because we need to set the entity resolver for this source,
    //  which isn't possible if we just use a StreamSource to read the Reader
//    private SAXSource getAsSAXSource(Reader xmlDocReader) throws SAXException
//    {
//        InputSource source = new InputSource(xmlDocReader);
//        SAXSource saxSource = new SAXSource(source);
//        XMLReader xmlReader = XMLReaderFactory.createXMLReader(
//                                    config.get(CONFIG_KEY_SAX_PARSER_NAME, 0));
//        xmlReader.setFeature("http://xml.org/sax/features/validation",true);
//        xmlReader.setContentHandler(this);
//        xmlReader.setEntityResolver(getEntityResolver());
//        xmlReader.setErrorHandler(new CustomErrorHandler());
//        saxSource.setXMLReader(xmlReader);
//        return saxSource;
//    }
