/**
 *  '$RCSfile: XMLTransformerTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-12 02:49:44 $'
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

package edu.ucsb.nceas.morphotest.util;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.util.IOUtil;
import edu.ucsb.nceas.morpho.util.XMLTransformer;

import edu.ucsb.nceas.morpho.plugins.XSLTResolverInterface;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.xsltresolver.XSLTResolverPlugin;

import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import edu.ucsb.nceas.morpho.util.Log;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import org.xml.sax.EntityResolver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;     

import com.arbortext.catalog.Catalog;
import com.arbortext.catalog.CatalogEntityResolver;

/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class XMLTransformerTest extends TestCase
{

  private static Exception testException;
  private final String CONFIG_KEY_LOCAL_CATALOG_PATH  = "local_catalog_path";
  private ConfigXML config = Morpho.getConfiguration();
  private static XSLTResolverPlugin xsltResolverPlugin;
  
  static {
      xsltResolverPlugin = new XSLTResolverPlugin();
      // Start by creating the new plugin
      PluginInterface plugin = (PluginInterface)xsltResolverPlugin;

      // Set a reference to the framework in the Plugin
      plugin.initialize(null);
  }

  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public XMLTransformerTest(String name)
  {
    super(name);
  }

  /**
   * Establish a testing framework by initializing appropriate objects
   */
  public void setUp() {}

  /**
   * Release any objects after tests are complete
   */
  public void tearDown() {}

  /**
   * Create a suite of tests to be run together
   */
  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new XMLTransformerTest("initialize"));
    suite.addTest(new XMLTransformerTest("testTransform"));
    return suite;
  }

  /**
   * Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize()
  {
      assertTrue(true);
  }

 

////////////////////////////////////////////////////////////////////////////////
//                    S T A R T   T E S T   M E T H O D S                     //
////////////////////////////////////////////////////////////////////////////////

    /**
     * Check transform() works.
     */
    public void testTransform() 
    {
        testException = null;
        Reader result = null;
        
        XMLTransformer transformer = XMLTransformer.getInstance();
        assertNotNull(transformer);
        
        System.out.println("testing with null XML reader..."); 
        try {
            result = transformer.transform((Reader)null, getXSL_testDoc());
        } catch (IOException e) {
            System.out.println("OK: testTransform() IOException: " 
                                                            + e.getMessage());
            testException = e;
        }
        assertNotNull(testException);
        assertNull(result);
        
        testException = null;
        result = null;

        System.out.println("testing with null XSL reader..."); 
        try {
            result = transformer.transform(getXML_testDoc(), null);
        } catch (IOException e) {
            System.out.println("OK: testTransform() IOException: " 
                                                            + e.getMessage());
            testException = e;
        }
        assertNotNull(testException);
        assertNull(result);
        
        testException = null;
        result = null;

        
        System.out.println("testing with null XML DOM Document..."); 
        try {
            result = transformer.transform((Document)null, getXSL_testDoc());
        } catch (IOException e) {
            System.out.println("OK: testTransform() IOException: " 
                                                            + e.getMessage());
            testException = e;
        }
        assertNotNull(testException);
        assertNull(result);
        
        testException = null;
        result = null;
        
        
        System.out.println("testing with XML Reader only, should use generic stylesheet"); 
        try {
            result = transformer.transform(getXML_testDoc());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected testTransform() IOException: " + e.getMessage());
        }
        assertNotNull(result);
        try {
            assertEquals(
                IOUtil.getAsStringBuffer(getHTML_generic_resultDoc(),true).toString(),
                IOUtil.getAsStringBuffer(result,true).toString() );
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException trying to read returned Readers "+e);
        }
        testException = null;
        result = null;

        System.out.println("testing with XML Reader and XSL Reader..."); 
        try {
            result = transformer.transform(getXML_testDoc(), getXSL_testDoc());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected testTransform() IOException: " + e.getMessage());
        }
        assertNotNull(result);
        try {
            assertEquals(
                IOUtil.getAsStringBuffer(getHTML_resultDoc(),true).toString(),
                IOUtil.getAsStringBuffer(result,true).toString() );
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException trying to read returned Readers "+e);
        }
        testException = null;
        result = null;

      System.out.println("testing with XML DOM Document only, should use generic stylesheet"); 
      try {
          result = transformer.transform(getAsDOMDocument(getXML_testDoc()));
      } catch (IOException e) {
          e.printStackTrace();
          fail("Unexpected testTransform() IOException: " + e.getMessage());
      }
      assertNotNull(result);
      try {
          assertEquals(
              IOUtil.getAsStringBuffer(getHTML_generic_resultDoc(),true).toString(),
              IOUtil.getAsStringBuffer(result,true).toString() );
      } catch (IOException e) {
          e.printStackTrace();
          fail("IOException trying to read returned Readers "+e);
      }
      testException = null;
      result = null;
      
      System.out.println("testing with XML DOM Document and XSL Reader..."); 
      try {
          result = transformer.transform(getAsDOMDocument(getXML_testDoc()), getXSL_testDoc());
      } catch (IOException e) {
          e.printStackTrace();
          fail("Unexpected testTransform() IOException: " + e.getMessage());
      }
      assertNotNull(result);
      try {
          assertEquals(
              IOUtil.getAsStringBuffer(getHTML_resultDoc(),true).toString(),
              IOUtil.getAsStringBuffer(result,true).toString() );
      } catch (IOException e) {
          e.printStackTrace();
          fail("IOException trying to read returned Readers "+e);
      }
      testException = null;
      result = null;

      

    }


    public static void main(String args[]) {
        Log.getLog().setDebugLevel(51);
        junit.textui.TestRunner.run(XMLTransformerTest.class);
        System.exit(0);
    }

    //  Create a DOM Document to enable the transformer to access the Reader
    //  - Necessary because we need to set the entity resolver for this source, 
    //  which isn't possible if we just use a StreamSource to read the Reader
    private synchronized Document getAsDOMDocument(Reader xmlDocReader) 
    {
        Document doc = null;
        
        try {
            InputSource source = new InputSource(xmlDocReader);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
        
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            docBuilder.setEntityResolver(getEntityResolver());
            docBuilder.setErrorHandler(new CustomErrorHandler());
        
            doc = docBuilder.parse(source);
            
        } catch (IOException e) {
            lazyThrow(e, "IOException");
        } catch (SAXException e) {
            lazyThrow(e, "SAXException");
        } catch (FactoryConfigurationError e) {
            lazyThrow(e, "FactoryConfigurationError");
        } catch (ParserConfigurationException e) {
            lazyThrow(e, "ParserConfigurationException");
        }
        return doc;
    }
    
    //get entity resolver instance
    private EntityResolver getEntityResolver() 
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
        return (EntityResolver)catalogEntResolver;
    }
    
    private void lazyThrow(Throwable e, String type)
    {
        String msg 
            = "\nXMLTransformer.doTransform(): getting DOM document. "
                +"Nested "+type+" = "+e.getMessage()+"\n";
        e.fillInStackTrace();
        e.printStackTrace();
        Log.debug(12, msg);
        fail("Unexpected "+type+e.getMessage());
    }
    
    private Reader getXML_testDoc()  {
        return new StringReader(
              "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE eml-attribute "
            + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
            + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
            + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
            + "<eml-attribute>"
            + "<identifier>TESTDOC_1</identifier>"
            + "<attribute>"
            + "  <attributeName>field 1</attributeName>"
            + "  <attributeLabel>label for attribute 1</attributeLabel>"
            + "  <attributeDefinition>none whatsoever</attributeDefinition>"
            + "  <unit>cm</unit>"
            + "  <dataType>integer</dataType>"
            + "  <attributeDomain>"
            + "      <numericDomain>"
            + "        <minimum>2</minimum>"
            + "        <maximum>222</maximum>"
            + "      </numericDomain>"
            + "  </attributeDomain>"
            + "  <missingValueCode>~</missingValueCode>"
            + "  <precision>5</precision>"
            + "</attribute>"
            + "</eml-attribute>");
    }
    
    private  Reader getXSL_testDoc() {
        return new StringReader(
             "<?xml version=\"1.0\"?>"
            +"<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
            +"  <xsl:output method=\"html\" encoding=\"iso-8859-1\"/>"
            +"  <xsl:template match=\"/\">"
            +"    <html>"
            +"      <head>"
            +"      </head>"
            +"      <body>"
            +"        <center>"
            +"          <h1>Attribute structure description</h1>"
            +"          <h3>Ecological Metadata Language</h3>"
            +"        </center>"
            +"          <xsl:value-of select=\"eml-attribute/identifier\"/>"
            +"      </body>"
            +"    </html>"
            +"  </xsl:template>"
            +"</xsl:stylesheet>");
    }
    
    private static final Reader getHTML_resultDoc() {
        return new StringReader(
             "<html>\r\n"
            +"<head>\r\n"
            +"<META http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\r\n"
            +"</head>\r\n"
            +"<body>\r\n"
            +"<center>\r\n"
            +"<h1>Attribute structure description</h1>\r\n"
            +"<h3>Ecological Metadata Language</h3>\r\n"
            +"</center>"
            +"TESTDOC_1"
            +"</body>\r\n"
            +"</html>\r\n");
    }
    
    private static final Reader getHTML_generic_resultDoc() {
        return new StringReader(
             "<html>\r\n"
            +"<head>\r\n"
            +"<META http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\r\n"
            +"</head>\r\n"
            +"<body>\r\n"
            +"<center>\r\n"
            +"<h1>Ecological Metadata Language</h1>\r\n"
            +"</center>\r\n"
            +"<table align=\"center\" cellpadding=\"5\" border=\"1\" width=\"90%\">\r\n"
            +"<tr>\r\n"
            +"<td><b>Element Name</b></td><td><b>Value</b></td>\r\n"
            +"</tr>\r\n"
            +"<tr>\r\n"
            +"<td>eml-attribute</td><td>TESTDOC_1  field 1  label for attribute 1  none whatsoever  cm  integer                2        222          ~  5</td>\r\n"
            +"</tr>\r\n"
            +"</table>\r\n"
            +"</body>\r\n"
            +"</html>\r\n");
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
    
}


