/**
 *  '$RCSfile: MetaDisplayTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-21 03:26:06 $'
 * '$Revision: 1.2 $'
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

import edu.ucsb.nceas.morpho.exception.NullArgumentException;


/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class MetaDisplayTest extends TestCase
{
    private static final String         IDENTIFIER = "1.1";
    
    private        Component            comp;
    private static MetaDisplay          display;
    private static XMLFactoryInterface  factory;
    private static ActionListener       listener;
    private static Exception            testException;
    private static JFrame               frame;
    
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
        
        System.err.println("testing getDisplay with BOGUS_ID..."); 
        getDisplay_bad_params("BOGUS_ID",   factory,    listener);
        
        System.err.println("testing getDisplay with null ID..."); 
        getDisplay_bad_params(null,         factory,    listener);
        
        System.err.println("testing getDisplay with null factory..."); 
        getDisplay_bad_params(IDENTIFIER,   null,       listener);
        
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
            display.display(IDENTIFIER);
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
            display.display(IDENTIFIER, new StringReader(TEST_XML_DOC));
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
            display.display(null, new StringReader(TEST_XML_DOC));
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
            display.display(IDENTIFIER, null);
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
    * Test if the addActionListener() function works
    */
    public void testAddActionListener()
    {
    }

    /**
    * Test if the removeActionListener() function works 
    */
    public void testRemoveActionListener()
    {
    }
    

////////////////////////////////////////////////////////////////////////////////
//                      E N D   T E S T   M E T H O D S                       //
////////////////////////////////////////////////////////////////////////////////

 
    // * * * ALL PARAMS GOOD - SHOULD WORK OK: * * *
    private void getDisplayAll_OK() {

        comp = null;
        try {
            comp = display.getDisplayComponent(IDENTIFIER, factory, listener);
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        } catch (NullArgumentException nae) {
            nae.printStackTrace();
            fail("testGetDisplayComponent() NullArgumentException: " 
                                                          + nae.getMessage());
        }
        assertNotNull(comp);
        System.err.println("testGetDisplayComponent() returned OK...");
        System.err.println("...now displaying in test frame...");
        displayInJFrame(comp);
    }
    
    
    // * * * SHOULD THROW AN EXCEPTION if ID or XMLFactory are not valid * * *
    // * * * (ActionListener may be null) * * *
    private void getDisplay_bad_params( String id, 
                                      XMLFactoryInterface f, ActionListener l) {

        comp = null;
        try {
            comp = display.getDisplayComponent(id, f, l);
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
        assertNull(comp);
    }
    

    // * * * NULL LISTENER - SHOULD NOT THROW AN ERROR * * *

    private void getDisplay_null_listener() {
    
        comp = null;
        try {
            comp = display.getDisplayComponent(IDENTIFIER, factory, null);
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        } catch (NullArgumentException nae) {
            nae.printStackTrace();
            fail("testGetDisplayComponent() NullArgumentException: " 
                                                          + nae.getMessage());
        }
        assertNotNull(comp);
    }
    
    /**
    * NOTE - this gets called before *each* *test* 
    */
    public void setUp() {}
    
    /**
    * Release any objects after tests are complete
    */
    public void tearDown() {}
    
    private void displayInJFrame(Component comp) {
        if (comp==null) fail("displayInJFrame received NULL arg");
        frame.getContentPane().add(comp);
        frame.pack();
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
        frame.setBackground(Color.magenta);
        frame.setBounds(100,100,200,200);
        frame.show();
    }
    
    private static void createXMLFactory() 
    {
        factory = new XMLFactoryInterface() {
        public Reader openAsReader(String id) throws DocumentNotFoundException {
            if (id != IDENTIFIER ) {
                throw new DocumentNotFoundException("document not found for id:"
                                                                           +id);
            }
            return new StringReader(TEST_XML_DOC);
          }
        };
    }

    private static void createActionListener() 
    {
        listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                assertNotNull(ae);
                System.out.println("\n* * * ActionListener received callback:\n"
                    +"ActionEvent = "+ae.paramString());
            }
        };
    }    
    
    private static final String TEST_XML_DOC =
        "<?xml version=\"1.0\"?>"
        + "<!DOCTYPE eml-attribute "
        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
        + "<eml-attribute>"
        + "  <identifier>brooke2.122.4</identifier>"
        + "  <attribute>"
        + "    <attributeName>field 1</attributeName>"
        + "    <attributeLabel>label for attribute 1</attributeLabel>"
        + "    <attributeDefinition>none whatsoever</attributeDefinition>"
        + "    <unit>cm</unit>"
        + "    <dataType>integer</dataType>"
        + "    <attributeDomain>"
        + "        <numericDomain>"
        + "          <minimum>2</minimum>"
        + "          <maximum>222</maximum>"
        + "        </numericDomain>"
        + "    </attributeDomain>"
        + "    <missingValueCode>~</missingValueCode>"
        + "    <precision>5</precision>"
        + "  </attribute>"
        + "  <attribute>"
        + "    <attributeName>field 2</attributeName>"
        + "    <attributeLabel>label for attribute 1</attributeLabel>"
        + "    <attributeDefinition>none whatsoever</attributeDefinition>"
        + "    <unit>cm</unit>"
        + "    <dataType>integer</dataType>"
        + "    <attributeDomain>"
        + "          <enumeratedDomain>"
        + "            <code>CD</code>"
        + "            <definition>CoDe</definition>"
        + "            <source>FIPS</source>"
        + "          </enumeratedDomain>"
        + "    </attributeDomain>"
        + "    <missingValueCode>~</missingValueCode>"
        + "    <precision>5</precision>"
        + "  </attribute>"
        + "  <attribute>"
        + "    <attributeName>field 3</attributeName>"
        + "    <attributeLabel>label for attribute 1</attributeLabel>"
        + "    <attributeDefinition>none whatsoever</attributeDefinition>"
        + "    <unit>cm</unit>"
        + "    <dataType>integer</dataType>"
        + "    <attributeDomain>"
        + "          <enumeratedDomain>"
        + "            <code> </code>"
        + "            <definition> </definition>"
        + "          </enumeratedDomain>"
        + "          <textDomain>"
        + "            <definition>textD</definition>"
        + "            <pattern>*[^~#]</pattern>"
        + "            <source>Dunno</source>"
        + "          </textDomain>"
        + "    </attributeDomain>"
        + "    <missingValueCode>^</missingValueCode>"
        + "    <precision>5</precision>"
        + "  </attribute>"
        + "</eml-attribute>";
}


