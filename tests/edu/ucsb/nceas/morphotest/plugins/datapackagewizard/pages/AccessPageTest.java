package edu.ucsb.nceas.morphotest.plugins.datapackagewizard.pages;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AccessPage;
import edu.ucsb.nceas.morphotest.MorphoTestCase;
import edu.ucsb.nceas.morphotest.datastore.DataONEDataStoreServiceTest;

public class AccessPageTest extends MorphoTestCase {
  
  public AccessPageTest(String name) {
    super(name);
  }
  public static Test suite() throws Exception {
    TestSuite suite = new TestSuite();
    suite.addTest(new AccessPageTest("initialize"));
    suite.addTest(new AccessPageTest("testPage"));
    return suite;
  }
  
  /** Check that the testing framework is functioning properly with 
   * a trivial assertion.
   */
  public void initialize() {
     assertTrue(true);
  }
  
  public void testPage() throws Exception {
   //AccessPage.getSubjectInfoFromFile();
  }
 

}
