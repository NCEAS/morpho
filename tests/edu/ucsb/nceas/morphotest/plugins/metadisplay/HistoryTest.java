/**
 *  '$RCSfile: HistoryTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-10-26 08:08:33 $'
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

import junit.framework.TestCase;

import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.plugins.metadisplay.History;

import java.util.Properties;

/**
 * A JUnit test for testing the metadisplay plugin.
 */
public class HistoryTest extends TestCase
{
    private static History history;
    private static final String         ID1 = "1.1";
    private static final String         ID2 = "2.2";
    private static final String         ID3 = "3.3";
    private static final String         ID4 = "4.4";
    
    private static final Properties     testProps = new Properties();
    
    private History bogus = new History();
    
    static{
    
        testProps.setProperty("Prop1", "val1");
        testProps.setProperty("Prop2", "val2");
        testProps.setProperty("Prop3", "val3");
    } 
    
    /**
    * Constructor to build the test
    *
    * @param name the name of the test method
    */
    public HistoryTest(String name) {  super(name); }

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

    public void testHistory()
    {
        System.err.println("constructor test");
        assertNotNull(bogus);
        System.err.println("History() object created OK...");
        
        history = new History( bogus.getNewHistoryItemInstance(ID1, testProps) );
        assertNotNull(history);
        System.err.println("History(ID1) object created OK...");
    }

    public void testAdd()
    {
        //...on existing History:
        assertNotNull(history);
        assertNotNull(bogus);
        history.add(bogus.getNewHistoryItemInstance(ID2, testProps));
        System.err.println("added ID2");
        history.add(bogus.getNewHistoryItemInstance(ID3, testProps));
        System.err.println("added ID3");
        history.add(bogus.getNewHistoryItemInstance(ID4, testProps));
        System.err.println("added ID4");

        //on a new, empty History:
        History bogus2 = new History();
        try {
            bogus2.add(bogus.getNewHistoryItemInstance(ID2, testProps));
        } catch (Exception e) {
            fail("Exception doing add() on a new, empty History"+e);
            e.printStackTrace();
        }
    }

    public void testToString()
    {
        //...on existing History:
        
        try {
            System.err.println("history.toString() gives:\n"+history.toString());
        } catch (Exception e) {
            fail("Exception doing toString() on existing History"+e);
            e.printStackTrace();
        }

        //on a new, empty History:
        History bogus3 = new History();
        try {
            System.err.println("empty history toString() gives:\n"
                                                            +bogus3.toString());
        } catch (Exception e) {
            fail("Exception doing add() on a new, empty History"+e);
            e.printStackTrace();
        }
    }
    
    public void testPreviewPrevious()
    {
        System.err.println("testing previewPrevious()...");
        //...on existing History:
        assertEquals(ID4,history.previewPreviousID());
        
        //on a new, empty History:
        History bogus4 = new History();
        try {
            assertNull(bogus4.previewPreviousID());
        } catch (Exception e) {
            fail("Exception doing assertNull() on a new, empty History"+e);
            e.printStackTrace();
        }
    }

    public void testDeletePrevious()
    {
        System.err.println("testing deletePrevious()...");
        //...on existing History:
        history.deletePrevious();
        assertEquals(ID3,history.previewPreviousID());
        
        //on a new, empty History:
        History bogus5 = new History();
        try {
            bogus5.deletePrevious();
        } catch (Exception e) {
            fail("Exception doing deletePrevious on a new, empty History"+e);
            e.printStackTrace();
        }
    }

    public void testGetPrevious()
    {
        System.err.println("testing getPrevious()...");
        //...on existing History:
//        assertEquals(ID3,history.getPrevious());
//        assertEquals(ID2,history.getPrevious());
        history.getPrevious();
        history.getPrevious();
        assertEquals(ID1,history.previewPreviousID());
        
        //on a new, empty History:
        History bogus6 = new History();
        try {
            assertNull(bogus6.getPrevious());
        } catch (Exception e) {
            fail("Exception doing assertNull() on a new, empty History"+e);
            e.printStackTrace();
        }
    }
    
////////////////////////////////////////////////////////////////////////////////
//                      E N D   T E S T   M E T H O D S                       //
////////////////////////////////////////////////////////////////////////////////


    public static void main(String args[]) {
        Log.getLog().setDebugLevel(51);
        junit.textui.TestRunner.run(HistoryTest.class);
        System.exit(0);
    }
}


