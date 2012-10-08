/**
 *  '$RCSfile: EML201DocumentCorrectorTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-09-25 22:56:13 $'
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
package edu.ucsb.nceas.morphotest.datastore;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.datastore.DataStoreService;
import edu.ucsb.nceas.morphotest.MorphoTestCase;

/**
 * A junit test class for DataStoreService
 * @author tao
 *
 */
public class DataStoreServiceTest extends MorphoTestCase {
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() throws Exception {
      TestSuite suite = new TestSuite();
      suite.addTest(new DataStoreServiceTest("initialize"));
      suite.addTest(new DataStoreServiceTest("testGetProflieDir"));
      suite.addTest(new DataStoreServiceTest("testGetCurrentProfileDir"));
      return suite;
  }
  
  /**
   * Constructor a test
   * @param name the name of test
   */
  public DataStoreServiceTest(String name) {
    super(name);
  }
  
  /**
   * Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize() {
      assertTrue(true);
  }
  
  /**
   * Test the getProfileDir(profile) method
   */
  public void testGetProflieDir() {
    String path =DataStoreService.getProfileDir(profile); 
    System.out.println("The profile directory is "+path);
    assertTrue("The pofile path should be "+"/Users/tao/.morpho/profiles/tao", 
                        path.equals("/Users/tao/.morpho/profiles/tao"));
  }
  
  /**
   * Test the getProfileDir() method
   */
  public void testGetCurrentProfileDir() {
    String path =DataStoreService.getProfileDir(); 
    System.out.println("The profile directory is "+path);
    assertTrue("The current pofile path should be "+"/Users/tao/.morpho/profiles/tao", 
                        path.equals("/Users/tao/.morpho/profiles/tao"));
  }
}
