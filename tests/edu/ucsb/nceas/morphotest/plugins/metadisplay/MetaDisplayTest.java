/**
 *  '$RCSfile: MetaDisplayTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-22 22:20:59 $'
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

package edu.ucsb.nceas.morphotest.plugins.metadisplay;

import java.io.Reader;
import java.io.StringReader;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import edu.ucsb.nceas.morpho.plugins.metadisplay.MetaDisplay;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.exception.NullArgumentException;


/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class MetaDisplayTest extends TestCase
{
    private static final String         ID1 = "1.1";
    private static final String         ID2 = "2.2";
    private static final String         ID3 = "3.3";
    private static final String         ID4 = "4.4";
    
    private static MetaDisplay          display;
    private static XMLFactoryInterface  factory;
    private static ActionListener       listener;
    private static JFrame               frame;
    
    private static Component            testComponent;
    private static Exception            testException;
    private static ActionEvent          testActionEvent;
    
    //put these in a static block.  If they are in constructor, they get called 
    //before every single test, for some bizzarre reason...
    static  {
      display = new MetaDisplay();
      System.err.println("MetaDisplay object created OK...");
      assertNotNull(display);
      createXMLFactory();
      assertNotNull(factory);
      System.err.println("XMLFactory() object created OK...");
      createActionListener();
      assertNotNull(listener);
      System.err.println("ActionListener() object created OK...");
      createJFrame();
      System.err.println("JFrame() object created OK...");
    }
    
    /**
    * Constructor to build the test
    *
    * @param name the name of the test method
    */
    public MetaDisplayTest(String name) {  super(name); }

    /**
    * NOTE - this gets called before *each* *test* 
    */
    public void setUp() {
        testComponent   = null;
        testException   = null;
        testActionEvent = null;
    }
    
    /**
    * Release any objects after tests are complete
    */
    public void tearDown() {}
    

////////////////////////////////////////////////////////////////////////////////
//                    S T A R T   T E S T   M E T H O D S                     //
////////////////////////////////////////////////////////////////////////////////

    /**
    * Check testGetDisplayComponent() works.
    */
    public void testGetDisplayComponent()
    {
        System.err.println("testing getDisplay with valid params..."); 
        getDisplayAll_OK();
        display.removeActionListener(listener);
        
        System.err.println("testing getDisplay with BOGUS_ID..."); 
        getDisplay_bad_params("BOGUS_ID",   factory,    listener);
        display.removeActionListener(listener);
        
        System.err.println("testing getDisplay with null ID..."); 
        getDisplay_bad_params(null,         factory,    listener);
        display.removeActionListener(listener);
        
        System.err.println("testing getDisplay with null factory..."); 
        getDisplay_bad_params(ID1,   null,       listener);
        display.removeActionListener(listener);
        
        System.err.println("testing getDisplay with null listener..."); 
        getDisplay_null_listener();
    }
    

    /** 
     *  Test the display(String id) and display(String id, Reader xmldoc) 
     *  functions
     */
    public void testDisplay()
    {
        //display(String id) * * * * * * * * * * * * * * * * * * * * * * * * * *
        try {
            display.display(ID3);
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        }
        System.err.println("testDisplay(id) completed OK...");
        doSleep(1);
        
        //null id - should throw DocumentNotFoundException: * * * * * 
        try {
            display.display(null);
        } catch (DocumentNotFoundException dnfe) {
            System.out.println("OK - testDisplay(null) DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
            testException = dnfe;
        }
        assertNotNull(testException);
        testException = null;
        doSleep(1);

        //display(String id, Reader xmldoc) * * * * * * * * * * * * * * * * * * 
        try {
            display.display(ID4, new StringReader(TEST_XML_DOC_4));
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        } catch (NullArgumentException nae) {
            nae.printStackTrace();
            fail("testGetDisplayComponent() NullArgumentException: " 
                                                          + nae.getMessage());
        }
        System.err.println("testDisplay(id,Reader) completed OK...");
        doSleep(1);
        
        //null id - not allowed - should throw NullArgumentException:* * * * * *
        try {
            display.display(null, new StringReader(TEST_XML_DOC_1));
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        } catch (NullArgumentException nae) {
            System.out.println("OK - testDisplay(NULL, Reader) NullArgumentException: " 
                                                          + nae.getMessage());
            testException = nae;
        }
        assertNotNull(testException);
        testException = null;
        doSleep(1);

        //null Reader - should be handled gracefully - display blank document* *
        try {
            display.display(ID1, null);
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        } catch (NullArgumentException nae) {
            nae.printStackTrace();
            fail("testGetDisplayComponent() NullArgumentException: " 
                                                          + nae.getMessage());
        }
        System.err.println("testDisplay(id,NULL) completed OK...");
        doSleep(1);
    }
    /** 
     *  Test the redisplay() function
     */
    public void testRedisplay()
    {
        try {
            display.redisplay();
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        }
        System.err.println("testRedisplay() completed OK...");
        doSleep(1);
    }
    
    /**
    * Test if the removeActionListener() function works 
    */
    public void testRemoveActionListener()
    {
        assertNull(testActionEvent);
        display.removeActionListener(listener);
        try {
            display.redisplay();    
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testAddActionListener() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        }
        assertNull(testActionEvent);
        testActionEvent = null;
    }
    
    /**
    * Test if the addActionListener() function works
    */
    public void testAddActionListener()
    {
        assertNull(testActionEvent);
        display.addActionListener(listener);
        //assert should happen in callback to actionPerformed():
        try {
            display.redisplay();    
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testAddActionListener() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        }
        assertNotNull(testActionEvent);
        testActionEvent = null;
    }


////////////////////////////////////////////////////////////////////////////////
//                      E N D   T E S T   M E T H O D S                       //
////////////////////////////////////////////////////////////////////////////////

 
    // * * * ALL PARAMS GOOD - SHOULD WORK OK: * * *
    private void getDisplayAll_OK() {

        testComponent = null;
        try {
            testComponent = display.getDisplayComponent(ID1, factory, listener);
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        } catch (NullArgumentException nae) {
            nae.printStackTrace();
            fail("testGetDisplayComponent() NullArgumentException: " 
                                                          + nae.getMessage());
        }
        assertNotNull(testComponent);
        System.err.println("testGetDisplayComponent() returned OK...");
        System.err.println("...now displaying in test frame...");
        displayInJFrame(testComponent);
    }
    
    
    // * * * SHOULD THROW AN EXCEPTION if ID or XMLFactory are not valid * * *
    // * * * (ActionListener may be null) * * *
    private void getDisplay_bad_params( String id, 
                                      XMLFactoryInterface f, ActionListener l) {

        testComponent = null;
        try {
            testComponent = display.getDisplayComponent(id, f, l);
        } catch (DocumentNotFoundException dnfe) {
            System.out.println("OK - testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
            testException = dnfe;
        } catch (NullArgumentException nae) {
            System.out.println("OK - testGetDisplayComponent() NullArgumentException: " 
                                                          + nae.getMessage());
            testException = nae;
        }
        assertNotNull(testException);
        testException = null;
        assertNull(testComponent);
    }
    

    // * * * NULL LISTENER - SHOULD NOT THROW AN ERROR * * *

    private void getDisplay_null_listener() {
    
        testComponent = null;
        try {
            testComponent = display.getDisplayComponent(ID2, factory, null);
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        } catch (NullArgumentException nae) {
            nae.printStackTrace();
            fail("testGetDisplayComponent() NullArgumentException: " 
                                                          + nae.getMessage());
        }
        assertNotNull(testComponent);
        System.err.println("testGetDisplayComponent(null-listener) returned OK...");
        System.err.println("...now displaying in test frame...");
        displayInJFrame(testComponent);
    }
    
    private void displayInJFrame(Component comp) {
        if (comp==null) fail("displayInJFrame received NULL arg");
        frame.getContentPane().add(comp);
        frame.pack();
        frame.show();
        doSleep(1);
    }
    
    //pause briefly so we can see UI before test exits...
    private void doSleep(long seconds) {
        try  { 
            Thread.sleep(seconds*1000);
        } catch(InterruptedException ie)  { 
            System.err.println("Thread interrupted!"); 
        }
    }
    
    private static void createJFrame() {
        frame = new JFrame("MetaDisplayTest");
        frame.setBounds(100,100,200,200);
    }
    
    private static void createXMLFactory() 
    {
        factory = new XMLFactoryInterface() {
        public Reader openAsReader(String id) throws DocumentNotFoundException {
            if      (id.equals(ID1)) return new StringReader(TEST_XML_DOC_1);
            else if (id.equals(ID2)) return new StringReader(TEST_XML_DOC_2);
            else if (id.equals(ID3)) return new StringReader(TEST_XML_DOC_3);
            else if (id.equals(ID4)) return new StringReader(TEST_XML_DOC_4);
            else throw new DocumentNotFoundException("document not found for id:"
                                                                           +id);
          }
        };
    }

    private static void createActionListener() 
    {
        listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                assertNotNull(ae);
                testActionEvent = ae;
                System.out.println("\n* * * ActionListener received callback:\n"
                    +"ActionEvent = "+ae.paramString());
            }
        };
    }   
    
     
    private static final String TEST_XML_DOC_1 =
         "<html><head></head>\n<body bgcolor=\"#ff0000\">\n"
        +"<h1>TEST DOCUMENT 1</h1></body></html>";
        
    private static final String TEST_XML_DOC_2 =
         "<html><head></head>\n<body bgcolor=\"#00ff00\">\n"
        +"<h1>TEST DOCUMENT 2</h1></body></html>";
        
    private static final String TEST_XML_DOC_3 =
         "<html><head></head>\n<body bgcolor=\"#0000ff\">\n"
        +"<h1>TEST DOCUMENT 3</h1></body></html>";
    
    private static final String TEST_XML_DOC_4 =
         "<html><head></head>\n<body bgcolor=\"#ffff00\">\n"
        +"<h1>TEST DOCUMENT 4</h1></body></html>";
                
    
//    private static final String TEST_XML_DOC_1 =
//        "<?xml version=\"1.0\"?>"
//        + "<!DOCTYPE eml-attribute "
//        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
//        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
//        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
//        + "<eml-attribute>"
//        + "  <identifier> * * * * TESTDOC 1 1 1 1 1 * * * * </identifier>"
//        + "</eml-attribute>";
//    
//    private static final String TEST_XML_DOC_2 =
//        "<?xml version=\"1.0\"?>"
//        + "<!DOCTYPE eml-attribute "
//        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
//        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
//        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
//        + "<eml-attribute>"
//        + "  <identifier> * * * * TESTDOC 2 2 2 2 2 * * * * </identifier>"
//        + "  <identifier> * * * * TESTDOC 2 2 2 2 2 * * * * </identifier>"
//        + "</eml-attribute>";
//        
//    private static final String TEST_XML_DOC_3 =
//        "<?xml version=\"1.0\"?>"
//        + "<!DOCTYPE eml-attribute "
//        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
//        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
//        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
//        + "<eml-attribute>"
//        + "  <identifier> * * * * TESTDOC 3 3 3 3 3 * * * * </identifier>"
//        + "  <identifier> * * * * TESTDOC 3 3 3 3 3 * * * * </identifier>"
//        + "  <identifier> * * * * TESTDOC 3 3 3 3 3 * * * * </identifier>"
//        + "</eml-attribute>";
//        
//    private static final String TEST_XML_DOC_4 =
//        "<?xml version=\"1.0\"?>"
//        + "<!DOCTYPE eml-attribute "
//        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
//        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
//        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
//        + "<eml-attribute>"
//        + "  <identifier> * * * * TESTDOC 4 4 4 4 4 * * * * </identifier>"
//        + "  <identifier> * * * * TESTDOC 4 4 4 4 4 * * * * </identifier>"
//        + "  <identifier> * * * * TESTDOC 4 4 4 4 4 * * * * </identifier>"
//        + "  <identifier> * * * * TESTDOC 4 4 4 4 4 * * * * </identifier>"
//        + "</eml-attribute>";
    

//    private static final String TEST_XML_DOC_ORIG =
//        "<?xml version=\"1.0\"?>"
//        + "<!DOCTYPE eml-attribute "
//        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
//        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
//        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
//        + "<eml-attribute>"
//        + "  <identifier>brooke2.122.4</identifier>"
//        + "  <attribute>"
//        + "    <attributeName>field 1</attributeName>"
//        + "    <attributeLabel>label for attribute 1</attributeLabel>"
//        + "    <attributeDefinition>none whatsoever</attributeDefinition>"
//        + "    <unit>cm</unit>"
//        + "    <dataType>integer</dataType>"
//        + "    <attributeDomain>"
//        + "        <numericDomain>"
//        + "          <minimum>2</minimum>"
//        + "          <maximum>222</maximum>"
//        + "        </numericDomain>"
//        + "    </attributeDomain>"
//        + "    <missingValueCode>~</missingValueCode>"
//        + "    <precision>5</precision>"
//        + "  </attribute>"
//        + "  <attribute>"
//        + "    <attributeName>field 2</attributeName>"
//        + "    <attributeLabel>label for attribute 1</attributeLabel>"
//        + "    <attributeDefinition>none whatsoever</attributeDefinition>"
//        + "    <unit>cm</unit>"
//        + "    <dataType>integer</dataType>"
//        + "    <attributeDomain>"
//        + "          <enumeratedDomain>"
//        + "            <code>CD</code>"
//        + "            <definition>CoDe</definition>"
//        + "            <source>FIPS</source>"
//        + "          </enumeratedDomain>"
//        + "    </attributeDomain>"
//        + "    <missingValueCode>~</missingValueCode>"
//        + "    <precision>5</precision>"
//        + "  </attribute>"
//        + "  <attribute>"
//        + "    <attributeName>field 3</attributeName>"
//        + "    <attributeLabel>label for attribute 1</attributeLabel>"
//        + "    <attributeDefinition>none whatsoever</attributeDefinition>"
//        + "    <unit>cm</unit>"
//        + "    <dataType>integer</dataType>"
//        + "    <attributeDomain>"
//        + "          <enumeratedDomain>"
//        + "            <code> </code>"
//        + "            <definition> </definition>"
//        + "          </enumeratedDomain>"
//        + "          <textDomain>"
//        + "            <definition>textD</definition>"
//        + "            <pattern>*[^~#]</pattern>"
//        + "            <source>Dunno</source>"
//        + "          </textDomain>"
//        + "    </attributeDomain>"
//        + "    <missingValueCode>^</missingValueCode>"
//        + "    <precision>5</precision>"
//        + "  </attribute>"
//        + "</eml-attribute>";


    public static void main(String args[]) {
        Log.getLog().setDebugLevel(51);
        junit.textui.TestRunner.run(MetaDisplayTest.class);
        System.exit(0);
    }
}


