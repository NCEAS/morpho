/**
 *  '$RCSfile: HistoryTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-08-21 20:15:19 $'
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import edu.ucsb.nceas.morpho.plugins.metadisplay.History;


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
        History bogus = new History();
        assertNotNull(bogus);
        System.err.println("History() object created OK...");
        
        history = new History(ID1);
        assertNotNull(history);
        System.err.println("History(ID1) object created OK...");
    }

    public void testAdd()
    {
        //...on existing History:
        history.add(ID2);
        System.err.println("added ID2");
        history.add(ID3);
        System.err.println("added ID3");
        history.add(ID4);
        System.err.println("added ID4");

        //on a new, empty History:
        History bogus = new History();
        try {
            bogus.add(ID2);
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
        History bogus = new History();
        try {
            System.err.println("empty history toString() gives:\n"
                                                            +bogus.toString());
        } catch (Exception e) {
            fail("Exception doing add() on a new, empty History"+e);
            e.printStackTrace();
        }
    }
    
    public void testPreviewPrevious()
    {
        System.err.println("testing previewPrevious()...");
        //...on existing History:
        assertEquals(ID4,history.previewPrevious());
        
        //on a new, empty History:
        History bogus = new History();
        try {
            assertNull(bogus.previewPrevious());
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
        assertEquals(ID3,history.previewPrevious());
        
        //on a new, empty History:
        History bogus = new History();
        try {
            bogus.deletePrevious();
        } catch (Exception e) {
            fail("Exception doing deletePrevious on a new, empty History"+e);
            e.printStackTrace();
        }
    }

    public void testGetPrevious()
    {
        System.err.println("testing getPrevious()...");
        //...on existing History:
        assertEquals(ID3,history.getPrevious());
        assertEquals(ID2,history.getPrevious());
        assertEquals(ID1,history.previewPrevious());
        
        //on a new, empty History:
        History bogus = new History();
        try {
            assertNull(bogus.getPrevious());
        } catch (Exception e) {
            fail("Exception doing assertNull() on a new, empty History"+e);
            e.printStackTrace();
        }
    }
    
////////////////////////////////////////////////////////////////////////////////
//                      E N D   T E S T   M E T H O D S                       //
////////////////////////////////////////////////////////////////////////////////


    public static void main(String args[]) {
        junit.textui.TestRunner.run(HistoryTest.class);
    }
}


