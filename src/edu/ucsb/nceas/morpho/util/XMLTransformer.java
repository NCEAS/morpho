/**
 *  '$RCSfile: XMLTransformer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-06 23:09:48 $'
 * '$Revision: 1.8 $'
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
import java.net.MalformedURLException;

import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import org.xml.sax.XMLReader;
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
    
    private static XMLTransformer   instance;
    
    private XMLReader               xmlReader;
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
        String stylesheet = config.get("genericStylesheet", 0);
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
    public synchronized Reader transform(Reader xmlDocument, Reader xslStyleSheet)
                                                            throws IOException
    {
        validateInputParam(xmlDocument,   "XML document reader");
        validateInputParam(xslStyleSheet, "XSL stylesheet reader");
Log.debug(50,"# # XMLTransformer # # transform called;  xmlDocument = "+xmlDocument);            
Log.debug(50,"# # XMLTransformer # # transform called;  xslStyleSheet = "+xslStyleSheet);            

        PipedReader returnReader = new PipedReader();
        PipedWriter pipedWriter  = new PipedWriter();
        returnReader.connect(pipedWriter);
Log.debug(50,"# # XMLTransformer # # pipedWriter = "+pipedWriter);            

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer 
                    = tFactory.newTransformer(new StreamSource(xslStyleSheet));
Log.debug(50,"# # XMLTransformer # # transformer = "+transformer);            
        } catch (TransformerConfigurationException e) {
            String msg 
                = "XMLTransformer.transform(): getting Transformer instance. "
                    +"Nested TransformerConfigurationException="+e.getMessage();
            e.printStackTrace();
            Log.debug(12, msg);
            pipedWriter.write(msg.toCharArray(),0,msg.length());
            e.printStackTrace(new PrintWriter(pipedWriter));
        }

        try {
Log.debug(50,"# # XMLTransformer # # doing transformer.transform...");            
            transformer.transform(  getAsSaxSource(xmlDocument),
                                    new StreamResult(pipedWriter));
Log.debug(50,"# # XMLTransformer # # DONE transformer.transform!");            
        } catch (TransformerException e) {
            String msg
                = "XMLTransformer.transform(): Error transforming document."
                                +" Nested TransformerException="+e.getMessage();
            e.printStackTrace();
            Log.debug(12, msg);
            pipedWriter.write(msg.toCharArray(),0,msg.length());
            e.printStackTrace(new PrintWriter(pipedWriter));
        } catch (Exception e) {
            String msg
                = "XMLTransformer.transform(): Unrecognized Error transforming"
                                +" document: "+e.getMessage();
            e.printStackTrace();
            Log.debug(12, msg);
            pipedWriter.write(msg.toCharArray(),0,msg.length());
            e.printStackTrace(new PrintWriter(pipedWriter));
        } finally {
Log.debug(50,"# # XMLTransformer # # 'finally' block");            
            pipedWriter.flush();
            pipedWriter.close();
        }
Log.debug(50,"# # XMLTransformer # # returnReader = "+returnReader);            
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

    
    //  Create a SAXSource to enable the transformer to access the Reader
    //  - Necessary because we need to set the entity resolver for this source, 
    //  which isn't possible if we just use an InputSource
    private SAXSource getAsSaxSource(Reader xmlDocument) 
    {   
Log.debug(50,"# # XMLTransformer # # ...getAsSaxSource recvd XML doc Reader: "+xmlDocument);            

        InputSource source = new InputSource(xmlDocument);
Log.debug(50,"# # XMLTransformer # # ...getAsSaxSource created InputSource: "+source);            
 
        SAXSource saxSource = new SAXSource(source);
Log.debug(50,"# # XMLTransformer # # ...getAsSaxSource created saxSource: "+saxSource);            

        saxSource.setXMLReader(this.xmlReader);
Log.debug(50,"# # XMLTransformer # # ...getAsSaxSource did setXMLReader; result of GETXMLReader: "
                                                      +saxSource.getXMLReader());            
        return saxSource;
    }

    
    /* Set up the SAX parser for reading the XML serialized ACL */
    private void initXMLReader() throws SAXException
    {
      // Get an instance of the xmlReader
        xmlReader = XMLReaderFactory.createXMLReader(
                                    config.get(CONFIG_KEY_SAX_PARSER_NAME, 0));
        xmlReader.setFeature("http://xml.org/sax/features/validation", true);
        xmlReader.setContentHandler((ContentHandler)this);
        xmlReader.setEntityResolver(getEntityResolver());
        xmlReader.setErrorHandler(new CustomErrorHandler());
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
    
    class InnerCatalog extends Catalog 
    {
        public String resolvePublic(String publicID, String systemID) 
                                      throws MalformedURLException, IOException
        {
            String resolution = super.resolvePublic(publicID, systemID);
            Log.debug(50,"InnerCatalog.resolvePublic(): "+resolution);
            URL testURL = new URL("jar:file:/D:/_PROJECTS_/_ N C E A S _/MORPHO_ROOT/CVS_CHECKOUTS/morpho/lib/morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd");
            URL resolvedURL = new URL(resolution);
            Log.debug(50,"InnerCatalog.resolvedURL.getFile: "+resolvedURL.getFile());
            Log.debug(50,"InnerCatalog.testURL.getFile:     "+testURL);
            Log.debug(50,"InnerCatalog.resolvePublic():sameFile?: "+resolvedURL.sameFile(testURL));
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