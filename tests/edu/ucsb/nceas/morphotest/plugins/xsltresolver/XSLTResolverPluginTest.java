/**
 *  '$RCSfile: XSLTResolverPluginTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-23 23:04:07 $'
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

package edu.ucsb.nceas.morphotest.plugins.xsltresolver;

import java.io.Reader;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Enumeration;

import junit.framework.TestCase;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.IOUtil;

import edu.ucsb.nceas.morpho.plugins.XSLTResolverInterface;
import edu.ucsb.nceas.morpho.plugins.xsltresolver.XSLTResolverPlugin;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceController;



/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class XSLTResolverPluginTest extends TestCase
{
    private static XSLTResolverInterface  resolver;
    private static XSLTResolverPlugin     resolverPlugin;
    private static Hashtable              testMappings;
    
    static {
        testMappings = new Hashtable();
        initMappings();
    }
    
    /**
    * Constructor to build the test
    *
    * @param name the name of the test method
    */
    public XSLTResolverPluginTest(String name) {  
        super(name);
        resolverPlugin = new XSLTResolverPlugin();
    }

    /**
    * NOTE - this gets called before *each* *test* 
    */
    public void setUp() {
    }
    
    /**
    * Release any objects after tests are complete
    */
    public void tearDown() {}
    

////////////////////////////////////////////////////////////////////////////////
//                    S T A R T   T E S T   M E T H O D S                     //
////////////////////////////////////////////////////////////////////////////////

    public void testInitialize()
    {
        assertNotNull(resolverPlugin);
        System.err.println("plugin initialize() test");
        try {
            resolverPlugin.initialize(null);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception doing initialize(): "+e);
        }
        System.err.println("done initialize() - now trying to get as Service");
        try {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                      services.getServiceProvider(XSLTResolverInterface.class);
            resolver = (XSLTResolverInterface)provider;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception doing getServiceProvider(): "+e);
        }
        assertNotNull(resolver);
        System.err.println("Got as Service OK");
    }

    public void testGetXSLTStylesheetReader()
    {
        Reader xslt     = null;
        String docType  = null;
        
        System.err.println("testing with NULL doctype...");
        try {
          xslt = resolver.getXSLTStylesheetReader(null);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception doing getXSLTStylesheetReader(NULL): "+e);
        }
        assertNotNull(xslt);
        try {
            assertTrue(
                IOUtil.getAsStringBuffer(xslt, true).toString()
                    .indexOf((String)testMappings.get("genericStylesheet"))>0);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception doing getAsStringBuffer(): "+e);
        }
                    
        System.err.println("testing with UNKNOWN doctype...");
        try {
          xslt = resolver.getXSLTStylesheetReader("UNKNOWN");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception doing getXSLTStylesheetReader(UNKNOWN): "+e);
        }
        assertNotNull(xslt);
        try {
            assertTrue(
                IOUtil.getAsStringBuffer(xslt, true).toString()
                    .indexOf((String)testMappings.get("genericStylesheet"))>0);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception doing getAsStringBuffer(): "+e);
        }

        System.err.println("testing with valid doctypes...");
        Enumeration mappingkeys = testMappings.keys();
        while (mappingkeys.hasMoreElements()) {
            docType = (String)mappingkeys.nextElement();
            assertNotNull(docType);
            try {
              xslt = resolver.getXSLTStylesheetReader(docType);
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exception doing getXSLTStylesheetReader("+docType+"): "+e);
            }
            assertNotNull(xslt);
            System.err.println("got xslt value "+xslt+" for docType "+docType);
            try {
                assertTrue(IOUtil.getAsStringBuffer(xslt, true).toString()
                                .indexOf((String)testMappings.get(docType))>0);
            } catch (IOException e) {
                e.printStackTrace();
                fail("Exception doing getAsStringBuffer(): "+e);
            }
        }
    }

    
////////////////////////////////////////////////////////////////////////////////
//                      E N D   T E S T   M E T H O D S                       //
////////////////////////////////////////////////////////////////////////////////

    private static void initMappings() {
    
        testMappings.put(
              "genericStylesheet",
              "generic.xsl"
            );
        testMappings.put(
              "-//NCEAS//eml-dataset//EN",
              "eml-dataset-2.0.0beta4.xsl"
            );
        testMappings.put(
              "-//NCEAS//eml-dataset-2.0//EN",
              "eml-dataset-2.0.0beta4.xsl"
            );
        testMappings.put(
              "-//NCEAS//eml-resource//EN",
              "eml-dataset-2.0.0beta4.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-dataset-2.0.0beta4//EN",
              "eml-dataset-2.0.0beta4.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-access-2.0.0beta6//EN",
              "eml-access-2.0.0beta6.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN",
              "eml-attribute-2.0.0beta6.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-constraint-2.0.0beta6//EN",
              "eml-constraint-2.0.0beta6.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-coverage-2.0.0beta6//EN",
              "eml-coverage-2.0.0beta6.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-dataset-2.0.0beta6//EN",
              "eml-dataset-2.0.0beta6.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-entity-2.0.0beta6//EN",
              "eml-entity-2.0.0beta6.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-literature-2.0.0beta6//EN",
              "eml-literature-2.0.0beta6.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-physical-2.0.0beta6//EN",
              "eml-physical-2.0.0beta6.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-project-2.0.0beta6//EN",
              "eml-project-2.0.0beta6.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-protocol-2.0.0beta6//EN",
              "eml-protocol-2.0.0beta6.xsl"
            );
        testMappings.put(
              "-//ecoinformatics.org//eml-software-2.0.0beta6//EN",
              "eml-software-2.0.0beta6.xsl"
            );
    }

    public static void main(String args[]) {
        Log.getLog().setDebugLevel(51);
        junit.textui.TestRunner.run(XSLTResolverPluginTest.class);
        System.exit(0);
    }
}


