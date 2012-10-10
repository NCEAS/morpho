package edu.ucsb.nceas.morphotest.datastore.idmanagement.update;

import edu.ucsb.nceas.morpho.datastore.idmanagement.update.IdentifierFileMapUpdater;
import edu.ucsb.nceas.morphotest.MorphoTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

public class IdentifierFileMapUpdaterTest extends MorphoTestCase{
  
  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public IdentifierFileMapUpdaterTest(String name) {
      super(name);
     
  }
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTest(new IdentifierFileMapUpdaterTest("initialize"));
      suite.addTest(new IdentifierFileMapUpdaterTest("testUpdate"));
      
      return suite;
  }
  
  /**
   * Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize() {
      assertTrue(true);
  }
  
  /**
   * Test update method
   * @throws Exception
   */
  public void testUpdate() throws Exception {
    IdentifierFileMapUpdater updater = new IdentifierFileMapUpdater();
    boolean needUpdate = updater.needUpdate();
    System.out.println("IdentifierFileMapUpdaterTest.testUpdate - the need for the update is "+needUpdate);
    updater.update();
  }
}
