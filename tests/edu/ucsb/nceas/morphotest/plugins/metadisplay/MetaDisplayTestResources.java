/**
 *  '$RCSfile: MetaDisplayTestResources.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-13 19:12:20 $'
 * '$Revision: 1.6 $'
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

import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

import junit.framework.TestCase;

import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.xsltresolver.XSLTResolverPlugin;

import edu.ucsb.nceas.morpho.plugins.metadisplay.MetaDisplay;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;

import edu.ucsb.nceas.morpho.plugins.metadisplay.MetaDisplay;

import edu.ucsb.nceas.morpho.exception.NullArgumentException;


/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class MetaDisplayTestResources  
{
    public static final String TEST_DOC_1 = "1.1";
    public static final String TEST_DOC_2 = "2.2";
    public static final String TEST_DOC_3 = "3.3";
    public static final String TEST_DOC_4 = "4.4";

    public static final JFrame                  frame;
    public static final XMLFactoryAdapter       factory;
    public static final MetaDisplay             metaDisplay;
    static final JLabel topArea;
    static final JLabel dataArea;
    private static XSLTResolverPlugin xsltResolverPlugin;
    
    static {
        xsltResolverPlugin = new XSLTResolverPlugin();
        // Start by creating the new plugin
        PluginInterface plugin = (PluginInterface)xsltResolverPlugin;
  
        // Set a reference to the framework in the Plugin
        plugin.initialize(null);

        frame = new JFrame("MetaDisplay Test");
        topArea = new JLabel("Top Area");
        dataArea = new JLabel("Data Area");
        initFrame();
        factory = new XMLFactoryAdapter();
        metaDisplay = new MetaDisplay();
        try {
            metaDisplay.setFactory(factory);
        } catch (NullArgumentException nae) { 
            System.err.println("Problem setting test MetaDisplay XMLFactory: "+nae);
        }
    }    


    private MetaDisplayTestResources() {}

    public static void initFrame() {
        frame.setBounds(100,100,700,700);
        frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent event) { System.exit(0); }});
        frame.getContentPane().setLayout(new BorderLayout(5,5));
        topArea.setBackground(Color.darkGray);
        topArea.setOpaque(true);
        topArea.setPreferredSize(new Dimension(100,200));
        dataArea.setBackground(Color.darkGray);
        dataArea.setOpaque(true);
        dataArea.setPreferredSize(new Dimension(500,500));
        frame.getContentPane().add(topArea, BorderLayout.NORTH);
        frame.getContentPane().add(dataArea, BorderLayout.WEST);
    } 
    
    public static void displayInJFrame(Component comp, 
                                          int secondsDelay, TestCase caller)
    {
        if (comp==null) {
            caller.fail("MetaDisplayTestResources.displayInJFrame() "
                                        +"received NULL Component to display!");
        }
        frame.getContentPane().removeAll();
        frame.getContentPane().add(topArea, BorderLayout.NORTH);
        frame.getContentPane().add(dataArea, BorderLayout.WEST);
        frame.getContentPane().add(comp, BorderLayout.EAST);
        frame.pack();
        frame.show();
        doSleep(secondsDelay);
    }

    //pause briefly so we can see UI before test exits...
    public static void doSleep(int seconds)
    {
        try { 
            Thread.sleep(seconds*1000L);
        } catch(InterruptedException ie) { 
            System.err.println("Thread interrupted!"); 
        }
    }


    public static MetaDisplay getTestMetaDisplay()
    {
        return metaDisplay;
    }        

    public static XMLFactoryInterface getXMLFactory()
    {
        return factory;
    }        

// * * * * * * *      S T A T I C   T E S T    D A T A     * * * * * * * * * * *


    public static final String TEST_XML_DOC_1 =
          "<?xml version=\"1.0\"?>"
        + "<!DOCTYPE eml-attribute "
        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
        + "<eml-attribute>"
        + "<identifier>"+TEST_DOC_1+"</identifier>"
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
        + "</eml-attribute>";

    public static final String TEST_XML_DOC_2 =
          "<?xml version=\"1.0\"?>"
        + "<!DOCTYPE eml-attribute "
        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
        + "<eml-attribute>"
        + "<identifier>"+TEST_DOC_2+"</identifier>"
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
        + "</eml-attribute>";
        
    public static final String TEST_XML_DOC_3 =
          "<?xml version=\"1.0\"?>"
        + "<!DOCTYPE eml-attribute "
        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
        + "<eml-attribute>"
        + "<identifier>"+TEST_DOC_3+"</identifier>"
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
        + "</eml-attribute>";


    public static final String TEST_XML_DOC_4 =
          "<?xml version=\"1.0\"?>"
        + "<!DOCTYPE eml-attribute "
        + "PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" "
        + "\"file://jar:file:/C:/DEV/ecoinfo/MORPHO_ROOT/CVS_SOURCE/morpho/lib/"
        + "morpho-config.jar!/catalog/eml-attribute-2.0.0.beta6e.dtd\">"
        + "<eml-attribute>"
        + "<identifier>"+TEST_DOC_4+"</identifier>"
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
        + "</eml-attribute>";

}
 

//bare-bones test implementation
class XMLFactoryAdapter implements XMLFactoryInterface
{
    public Reader openAsReader(String id) throws DocumentNotFoundException
    {
        if (id.equals(MetaDisplayTestResources.TEST_DOC_1)) {
            return new StringReader(MetaDisplayTestResources.TEST_XML_DOC_1);
        } else if (id.equals(MetaDisplayTestResources.TEST_DOC_2)) {
            return new StringReader(MetaDisplayTestResources.TEST_XML_DOC_2);
        } else if (id.equals(MetaDisplayTestResources.TEST_DOC_3)) {  
            return new StringReader(MetaDisplayTestResources.TEST_XML_DOC_3);
        } else if (id.equals(MetaDisplayTestResources.TEST_DOC_4)) {
            return new StringReader(MetaDisplayTestResources.TEST_XML_DOC_4);
        } else { 
            throw new DocumentNotFoundException("document not found for id:"+id);
        }
    }
}


// K E E P    T H I S :::::::::::

//        "<html><head></head>\n<body bgcolor=\"#00ff00\">\n"
//        +"<h1>TEST DOCUMENT 2</h1><br>"
//        +"<a href=\""+TEST_DOC_3+"\">test link</a></body></html>";



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

