/**
 *  '$RCSfile: MetaDisplayTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-20 00:38:02 $'
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

package edu.ucsb.nceas.morphotest.plugins.metadisplay;

import java.io.Reader;
import java.io.StringReader;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

import edu.ucsb.nceas.morpho.plugins.*;
import edu.ucsb.nceas.morpho.plugins.metadisplay.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class MetaDisplayTest extends TestCase
{
    private MetaDisplay         display;
    private XMLFactoryInterface factory;
    private ActionListener      listener;
    private static final String IDENTIFIER = "1.1";
    
    
    /**
    * Constructor to build the test
    *
    * @param name the name of the test method
    */
    public MetaDisplayTest(String name)
    {
        super(name);
        createDisplay();
        createXMLFactory();
        createActionListener();
    }

    private void createDisplay() 
    {
        display = new MetaDisplay();
        assertNotNull(display);
        System.err.println("createDisplay() exiting OK...");
    }

    private void createXMLFactory() 
    {
        factory = new XMLFactoryInterface() {
            public Reader openAsReader(String id) {
                //for testing, ignore id and just send 
                //back a copy of the XML test doc
                return new StringReader(TEST_XML_DOC);
            }
        };
        assertNotNull(factory);
        System.err.println("createXMLFactory() exiting OK...");
    }

    private void createActionListener() 
    {
        listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                assertNotNull(ae);
                System.out.println("\n* * * ActionListener received callback:\n"
                    +"ActionEvent = "+ae.paramString());
            }
        };
        assertNotNull(listener);
        System.err.println("createActionListener() exiting OK...");
    }

    /**
    * Check testGetDisplayComponent() works.
    */
    public void testGetDisplayComponent()
    {
        Component comp = null;
        try {
            comp = display.getDisplayComponent(IDENTIFIER, factory, listener);
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        }
        assertNotNull(comp);
        System.err.println("testGetDisplayComponent() returned OK...");
        System.err.println("...now displaying in test frame...");
        assertNotNull(displayInJFrame(comp));
        try  { 
            Thread.sleep(5000);
        } catch(InterruptedException ie)  { 
            System.err.println("Thread interrupted!"); 
        }
    }

    /**
    * Test if the logIn() function works given a valid username and password
    */
    public void testDisplay()
    {
//        display(String IDENTIFIER, Reader XMLDocument) 
//                                          throws DocumentNotFoundException
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
    
    /**
    * NOTE - this gets called before *each* *test* 
    */
    public void setUp() {}
    
    /**
    * Release any objects after tests are complete
    */
    public void tearDown() {}
    
    public JFrame displayInJFrame(Component comp) {
        if (comp==null) fail("displayInJFrame received NULL arg");
        JFrame frame = new JFrame("MetaDisplayTest");
        frame.setBackground(Color.magenta);
        frame.getContentPane().add(comp);
        frame.setBounds(200,200,300,500);
        frame.show();
        return frame;
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


