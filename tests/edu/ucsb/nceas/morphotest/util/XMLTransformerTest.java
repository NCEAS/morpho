/**
 *  '$RCSfile: XMLTransformerTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-02 16:52:35 $'
 * '$Revision: 1.1 $'
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

import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.ucsb.nceas.morpho.util.XMLTransformer;

import edu.ucsb.nceas.morpho.util.Log;


/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class XMLTransformerTest extends TestCase
{

  private static Exception testException;

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
            result = transformer.transform(null, XSL_TESTDOC);
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
            result = transformer.transform(XML_TESTDOC, null);
        } catch (IOException e) {
            System.out.println("OK: testTransform() IOException: " 
                                                            + e.getMessage());
            testException = e;
        }
        assertNotNull(testException);
        assertNull(result);
        
        testException = null;
        result = null;

        System.out.println("testing with proper XML *and* XSL reader..."); 
        try {
            result = transformer.transform(XML_TESTDOC, XSL_TESTDOC);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unexpected testTransform() IOException: " + e.getMessage());
        }
        assertNotNull(result);
        assertEquals(result, HTML_RESULTDOC);        
        testException = null;
        result = null;
    }


    public static void main(String args[]) {
        Log.getLog().setDebugLevel(51);
        junit.textui.TestRunner.run(XMLTransformerTest.class);
    }


    private static final Reader XML_TESTDOC = new StringReader(
        "<?xml version=\"1.0\"?>"
        + "<!DOCTYPE eml-attribute "
        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
        + "<eml-attribute>"
        + "  <identifier> * * * * TESTDOC 1 1 1 1 1 * * * * </identifier>"
        + "</eml-attribute>");
    
    private static final Reader XSL_TESTDOC = new StringReader(
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
//        +"        <xsl:for-each select=\"eml-attribute/attribute\">"
//        +"          <xsl:value-of select=\"precision\"/>&#160;</td>"
//        +"        </xsl:for-each>"
        +"      </body>"
        +"    </html>"
        +"  </xsl:template>"
        +"</xsl:stylesheet>");
    
    private static final Reader HTML_RESULTDOC = new StringReader(
        "<?xml version=\"1.0\"?>"
        + "<!DOCTYPE eml-attribute "
        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
        + "<eml-attribute>"
        + "  <identifier> * * * * TESTDOC 1 1 1 1 1 * * * * </identifier>"
        + "</eml-attribute>");
    
}


