package edu.ucsb.nceas.morphotest.datastore.idmanagement;

import java.io.File;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.datastore.DataStoreService;
import edu.ucsb.nceas.morpho.datastore.idmanagement.DataONERevisionManager;
import edu.ucsb.nceas.morpho.datastore.idmanagement.RevisionManager;
import edu.ucsb.nceas.morphotest.MorphoTestCase;

public class DataONERevisionManagerTest extends MorphoTestCase {
  String id1= "ornl.mstmip.benchmark.global.gpp.modis.01";
  String id2= "ornl.mstmip.benchmark.global.gpp.modis.02";
  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public DataONERevisionManagerTest(String name) {
      super(name);

  }
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTest(new DataONERevisionManagerTest("initialize"));
      suite.addTest(new DataONERevisionManagerTest("testRevisions"));   
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
   * Test the revision methods
   * @throws Exception
   */
  public void testRevisions() throws Exception {
    DataONERevisionManager manager = DataONERevisionManager.getInstance();
    String obsoletes = manager.getObsoletes(id1);
    assertTrue("the id "+id1 +" should obsoletes null.", (obsoletes == null));
    String obsoletedBy = manager.getObsoletedBy(id1);
    assertTrue("the id "+id1+" is obsoleted by "+id2, obsoletedBy.equals(id2));
    String latestVersion = manager.getLatestRevision(id1);
    assertTrue("the latest version for id "+id1+ " should be "+id2, latestVersion.equals(id2));
    List <String> list = manager.getAllRevisions(id1);
    assertTrue("the first element in the all versions list should be "+id2, list.get(0).equals(id2));
    assertTrue("the second element in the all versions list should be "+id1, list.get(1).equals(id1));
    obsoletes = manager.getObsoletes(id2);
    assertTrue("the id "+id2 +" should obsoletes id "+id1, (obsoletes.equals(id1)));
    obsoletedBy = manager.getObsoletedBy(id2);
    assertTrue("the id "+id2+" is obsoleted by null", obsoletedBy ==null);
    latestVersion = manager.getLatestRevision(id2);
    assertTrue("the latest version for id "+id2+ " should be "+id2, latestVersion.equals(id2));
    list = manager.getAllRevisions(id2);
    assertTrue("the first element in the all versions list should be "+id2, list.get(0).equals(id2));
    assertTrue("the second element in the all versions list should be "+id1, list.get(1).equals(id1));
    
  }

}
