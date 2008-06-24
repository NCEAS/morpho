/**
 *  '$RCSfile: ObjectCacheTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-06-24 22:13:16 $'
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

package edu.ucsb.nceas.morphotest;

import edu.ucsb.nceas.morpho.util.*;

import java.io.FileNotFoundException;
import java.net.URLStreamHandler;
import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A JUnit test for testing the connection capabilities of the framework.
 */
public class ObjectCacheTest extends TestCase
{

  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public ObjectCacheTest(String name)
  {
    super(name);
  }

  /**
   * Establish a testing framework by initializing appropriate objects
   */
  public void setUp()
  { 

  }

  /**
   * Release any objects after tests are complete
   */
  public void tearDown()
  {
  }

  /**
   * Create a suite of tests to be run together
   */
  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new ObjectCacheTest("initialize"));
    suite.addTest(new ObjectCacheTest("testCache"));
    return suite;
  }

  /**
   * Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize()
  {
      assertTrue(true);
  }

  /**
   * Test 
   */
  public void testCache()
  {
    Storage aaa = new Storage("AAA");
    Storage bbb = new Storage("BBB"); 
    ObjectCache oc = new ObjectCache();
    oc.putObject("AAA", aaa);
    oc.putObject("BBB", bbb);      
    for (int i=0; i<20; i++) {
      oc.putObject("CC"+i, new Storage("CC"+i));
    }
    Log.debug(1, "oc size is: "+oc.getSize());    
    Enumeration enumration = oc.getKeys();
    while (enumration.hasMoreElements()) {
      System.out.println("keys: "+ enumration.nextElement());
    }
    assertTrue(oc.getSize()>2);
    assertTrue(((Storage)oc.getObject("AAA")).getName().equals("AAA"));  
    System.gc();  
    Log.debug(1, "oc size is: "+oc.getSize());   
    // ObjectCache should have 2 objects since AAA and BBB are in scope
    // and thus cannot be released
    assertTrue(oc.getSize()==2);
  }


}

class Storage {       

  char[] symbols = new char[3000];

  String name = "";

  public Storage(String name) {    
    this.name = name;
  }  

  public String getName() {
    return name;
  }

}