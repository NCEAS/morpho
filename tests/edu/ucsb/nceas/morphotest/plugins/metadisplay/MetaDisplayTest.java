/**
 *  '$RCSfile: MetaDisplayTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-24 00:41:34 $'
 * '$Revision: 1.5 $'
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import junit.framework.TestCase;

import edu.ucsb.nceas.morpho.plugins.metadisplay.MetaDisplay;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import edu.ucsb.nceas.morpho.plugins.XMLFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.exception.NullArgumentException;


/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class MetaDisplayTest extends TestCase
{
    private static MetaDisplay          display;
    private static ActionListener       listener;
    private static Component            testComponent;
    private static Exception            testException;
    private static ActionEvent          testActionEvent;
    
    //put these in a static block.  If they are in constructor, they get called 
    //before every single test, for some bizzarre reason...
    static  {
      display = new MetaDisplay();
      System.err.println("MetaDisplay object created OK...");
      assertNotNull(display);
      
      createActionListener();
      assertNotNull(listener);
      System.err.println("ActionListener() object created OK...");
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
        getDisplay_bad_params("BOGUS_ID", 
                            MetaDisplayTestResources.getXMLFactory(), listener);
        display.removeActionListener(listener);
        
        System.err.println("testing getDisplay with null ID..."); 
        getDisplay_bad_params(null, 
                            MetaDisplayTestResources.getXMLFactory(), listener);
        display.removeActionListener(listener);
        
        System.err.println("testing getDisplay with null factory..."); 
        getDisplay_bad_params(MetaDisplayTestResources.TEST_DOC_1,null,listener);
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
            display.display(MetaDisplayTestResources.TEST_DOC_3);
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        }
        System.err.println("testDisplay(id) completed OK...");
        MetaDisplayTestResources.doSleep(1);
        
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

        //display(String id, Reader xmldoc) * * * * * * * * * * * * * * * * * * 
        try {
            display.display(MetaDisplayTestResources.TEST_DOC_4, 
                    new StringReader(MetaDisplayTestResources.TEST_XML_DOC_4));
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
        MetaDisplayTestResources.doSleep(1);
        
        //null id - not allowed - should throw NullArgumentException:* * * * * *
        try {
            display.display(null, 
                    new StringReader(MetaDisplayTestResources.TEST_XML_DOC_1));
        } catch (DocumentNotFoundException dnfe) {
            dnfe.printStackTrace();
            fail("testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
        } catch (NullArgumentException nae) {
            System.out.println("OK: testDisplay(NULL, Rdr) NullArgumentException: " 
                                                          + nae.getMessage());
            testException = nae;
        }
        assertNotNull(testException);
        testException = null;

        //null Reader - should be handled gracefully - display blank document* *
        try {
            display.display(MetaDisplayTestResources.TEST_DOC_1, null);
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
        MetaDisplayTestResources.doSleep(1);
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
        MetaDisplayTestResources.doSleep(1);
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
            testComponent = display.getDisplayComponent(
                            MetaDisplayTestResources.TEST_DOC_1, 
                            MetaDisplayTestResources.getXMLFactory(), listener);
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
        MetaDisplayTestResources.displayInJFrame(testComponent,1,(TestCase)this);
    }


    // * * * SHOULD THROW AN EXCEPTION if ID or XMLFactory are not valid * * *
    // * * * (ActionListener may be null) * * *
    private void getDisplay_bad_params( String id, 
                                      XMLFactoryInterface f, ActionListener l) {

        testComponent = null;
        try {
            testComponent = display.getDisplayComponent(id, f, l);
        } catch (DocumentNotFoundException dnfe) {
            System.out.println("OK: testGetDisplayComponent() DocumentNotFoundException: " 
                                                          + dnfe.getMessage());
            testException = dnfe;
        } catch (NullArgumentException nae) {
            System.out.println("OK: testGetDisplayComponent() NullArgumentException: " 
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
            testComponent = display.getDisplayComponent(
                                MetaDisplayTestResources.TEST_DOC_2, 
                                MetaDisplayTestResources.getXMLFactory(), null);
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
        MetaDisplayTestResources.displayInJFrame(testComponent,1,(TestCase)this);
    }

    private static void createActionListener() 
    {
        listener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                assertNotNull(ae);
                testActionEvent = ae;
                String eventType = "UNKNOWN!";
                switch (ae.getID())  {
                    case MetaDisplayInterface.NAVIGATION_EVENT:
                        eventType ="NAVIGATION_EVENT";
                        break;
                    case MetaDisplayInterface.HISTORY_BACK_EVENT:
                        eventType ="NAVIGATION_EVENT";
                        break;
                    case MetaDisplayInterface.CLOSE_EVENT:
                        eventType ="NAVIGATION_EVENT";
                        break;
                    case MetaDisplayInterface.EDIT_BEGIN_EVENT:
                        eventType ="NAVIGATION_EVENT";
                        break;
                }
                System.out.println(
                     "\n\n* * * ActionListener received callback: * * *"
                    +"\n  ID = "            +ae.getID()
                    +"  ( "+eventType+" )"
                    +"\n  actionCommand = " +ae.getActionCommand()
                    +"\n* * * * * * * * * * * * * * * * * * * * * * *\n\n");
            }
        };
    }   

    public static void main(String args[]) {
        Log.getLog().setDebugLevel(51);
        junit.textui.TestRunner.run(MetaDisplayTest.class);
    }
}


