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
package edu.ucsb.nceas.morphotest.datastore.idmanagement;


import java.io.File;
import java.io.IOException;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.idmanagement.IdentifierManager;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morphotest.MorphoTestCase;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
public class IdManagerTest extends MorphoTestCase
{

  
  
  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public IdManagerTest(String name)
  {
      super(name);
  }
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite()
  {
      TestSuite suite = new TestSuite();
      suite.addTest(new IdManagerTest("initialize"));
      suite.addTest(new IdManagerTest("testGenerateLocalId"));
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
   * Test the generate local id test
   */
  public void testGenerateLocalId() throws Exception
  {
   IdentifierManager manager = IdentifierManager.getInstance(profile);
   String id = manager.generatLocalId();
   System.out.println("The local id is "+id);
   assertTrue(id.contains("urn:id:"));
  }
}
