package edu.ucsb.nceas.morphotest.framework;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.framework.IdentifierUpdaterFrame;
import edu.ucsb.nceas.morphotest.MorphoTestCase;


/** 
 * A junit test class for the IdFileMapUpdaterFrame class.
 */
public class IdentifierUpdaterFrameTest extends MorphoTestCase {
  
  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public IdentifierUpdaterFrameTest(String name) {
      super(name);
  }
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTest(new IdentifierUpdaterFrameTest("initialize"));
      suite.addTest(new IdentifierUpdaterFrameTest("testRun"));
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
    IdentifierUpdaterFrame frame = new IdentifierUpdaterFrame();
    frame.run();
  }
  

}
