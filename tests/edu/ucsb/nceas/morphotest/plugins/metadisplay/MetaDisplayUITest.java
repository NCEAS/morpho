/**
 *  '$RCSfile: MetaDisplayUITest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-24 00:41:34 $'
 * '$Revision: 1.3 $'
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

import junit.framework.TestCase;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.plugins.metadisplay.MetaDisplayUI;


/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class MetaDisplayUITest extends TestCase
{
    
    private static MetaDisplayUI        testUI;

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
    
        testUI = new MetaDisplayUI(MetaDisplayTestResources.getTestMetaDisplay());
        assertNotNull(testUI);
        MetaDisplayTestResources.displayInJFrame(testUI,1,(TestCase)this);
    }

    /** 
     *  Test the testSetHTML() function
     */
    public void testSetHTML()
    {
        try {
            testUI.setHTML(MetaDisplayTestResources.TEST_XML_DOC_1);
        } catch (Exception e) {
            e.printStackTrace();
            fail("testSetHTML() Exception: "+ e.getMessage());
        }
        System.err.println("testSetHTML() completed OK...");
        MetaDisplayTestResources.doSleep(1);
        try {
            testUI.setHTML(MetaDisplayTestResources.TEST_XML_DOC_2);
        } catch (Exception e) {
            e.printStackTrace();
            fail("testSetHTML() Exception: "+ e.getMessage());
        }
        System.err.println("testSetHTML() completed OK...");
        MetaDisplayTestResources.doSleep(1);
        try {
            testUI.setHTML(MetaDisplayTestResources.TEST_XML_DOC_3);
        } catch (Exception e) {
            e.printStackTrace();
            fail("testSetHTML() Exception: "+ e.getMessage());
        }
        System.err.println("testSetHTML() completed OK...");
        MetaDisplayTestResources.doSleep(1);
    }
    
    public static void main(String args[]) {
        Log.getLog().setDebugLevel(51);
        junit.textui.TestRunner.run(MetaDisplayUITest.class);
    }

}


