/**
 *  '$RCSfile: MetaDisplayUITest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-22 22:20:59 $'
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

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.plugins.metadisplay.MetaDisplayUI;


/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class MetaDisplayUITest extends TestCase
{
    
    private static MetaDisplayUI        testUI;
    private static JFrame               frame;

    static {
        createJFrame();
        assertNotNull(frame);
    }
    /**
    * Constructor to build the test
    *
    * @param name the name of the test method
    */
    public MetaDisplayUITest(String name) {  super(name); }

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

    public void testMetaDisplayUI() {
    
        testUI = new MetaDisplayUI();
        assertNotNull(testUI);
        displayInJFrame(testUI);
    }

    /** 
     *  Test the display(String id) and display(String id, Reader xmldoc) 
     *  functions
     */
    public void testSetHTML()
    {
        try {
            testUI.setHTML(TEST_HTML_RED);
        } catch (Exception e) {
            e.printStackTrace();
            fail("testSetHTML() Exception: "+ e.getMessage());
        }
        System.err.println("testSetHTML() completed OK...");
        doSleep(2);
        try {
            testUI.setHTML(TEST_HTML_GREEN);
        } catch (Exception e) {
            e.printStackTrace();
            fail("testSetHTML() Exception: "+ e.getMessage());
        }
        System.err.println("testSetHTML() completed OK...");
        doSleep(2);
        try {
            testUI.setHTML(TEST_HTML_BLUE);
        } catch (Exception e) {
            e.printStackTrace();
            fail("testSetHTML() Exception: "+ e.getMessage());
        }
        System.err.println("testSetHTML() completed OK...");
        doSleep(2);
    }
    
////////////////////////////////////////////////////////////////////////////////
//                      E N D   T E S T   M E T H O D S                       //
////////////////////////////////////////////////////////////////////////////////

   private static void createJFrame() {
        frame = new JFrame("MetaDisplayUITest");
        frame.setBackground(Color.pink);
        frame.setBounds(600,150,200,200);
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

    public static void main(String args[]) {
        Log.getLog().setDebugLevel(51);
        junit.textui.TestRunner.run(MetaDisplayUITest.class);
        System.exit(0);
    }

    private static final String TEST_HTML_RED =
         "<html><head></head>\n<body bgcolor=\"#CC0000\">\n"
        +"<h1>Test Doc 1</h1></body></html>";
        
    private static final String TEST_HTML_GREEN =
         "<html><head></head>\n<body bgcolor=\"#00CC00\">\n"
        +"<h1>Test Doc 2</h1></body></html>";
        
    private static final String TEST_HTML_BLUE =
         "<html><head></head>\n<body bgcolor=\"#0000CC\">\n"
        +"<h1>Test Doc 3</h1></body></html>";
}


