package edu.ucsb.nceas.morphotest.framework;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.framework.IdFileMapUpdaterFrame;
import edu.ucsb.nceas.morphotest.MorphoTestCase;


/** 
 * A junit test class for the IdFileMapUpdaterFrame class.
 */
public class IdFileMapUpdaterFrameTest extends MorphoTestCase {
  
  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public IdFileMapUpdaterFrameTest(String name) {
      super(name);
  }
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTest(new IdFileMapUpdaterFrameTest("initialize"));
      suite.addTest(new IdFileMapUpdaterFrameTest("testRun"));
      return suite;
  }
  
  /**
   * Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize() {
      assertTrue(true);
  }
  
  public void testRun() throws Exception {
    IdFileMapUpdaterFrame frame = new IdFileMapUpdaterFrame();
    frame.run();
  }
  

}
