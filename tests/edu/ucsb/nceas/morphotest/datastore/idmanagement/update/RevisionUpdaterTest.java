package edu.ucsb.nceas.morphotest.datastore.idmanagement.update;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.datastore.idmanagement.update.RevisionUpdater;
import edu.ucsb.nceas.morphotest.MorphoTestCase;

public class RevisionUpdaterTest extends MorphoTestCase {
  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public RevisionUpdaterTest(String name) {
      super(name);
     
  }
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTest(new RevisionUpdaterTest("initialize"));
      suite.addTest(new RevisionUpdaterTest("testUpdate"));
      
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
    RevisionUpdater updater = new RevisionUpdater();
    boolean needUpdate = updater.needUpdate();
    System.out.println("RevisionUpdaterTest.testUpdate - the need for the update is "+needUpdate);
    updater.update();
  }

}
