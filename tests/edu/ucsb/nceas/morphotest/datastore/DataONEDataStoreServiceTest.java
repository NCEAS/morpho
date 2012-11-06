/**  '$RCSfile: EML201DocumentCorrectorTest.java,v $'
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
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataONEDataStoreService;
import edu.ucsb.nceas.morphotest.MorphoTestCase;

/**
 * A junit test class for DataStoreService
 * @author tao
 *
 */
public class DataONEDataStoreServiceTest extends MorphoTestCase {
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() throws Exception {
      TestSuite suite = new TestSuite();
      suite.addTest(new DataONEDataStoreServiceTest("initialize"));
      suite.addTest(new DataONEDataStoreServiceTest("testExists"));
      return suite;
  }
  
  /**
   * Constructor a test
   * @param name the name of test
   */
  public DataONEDataStoreServiceTest(String name) {
    super(name);
  }
  
  /**
   * Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize() {
      assertTrue(true);
  }
  
 
  public void testExists() throws Exception {
    String id = "ornl.mstmip.benchmark.global.gpp.modis.01";
    String id2= "tao.1";
    DataONEDataStoreService service = new DataONEDataStoreService(Morpho.thisStaticInstance);
    assertTrue("the id "+id+" should exist in the server ", service.exists(id));
    assertFalse("The id "+id2+" shouldn't exist in the server", service.exists(id2));
    
  }
}

