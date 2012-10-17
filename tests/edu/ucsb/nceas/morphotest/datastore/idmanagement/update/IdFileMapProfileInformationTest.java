package edu.ucsb.nceas.morphotest.datastore.idmanagement.update;

import java.io.File;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.datastore.idmanagement.update.IdFileMapProfileInformation;
import edu.ucsb.nceas.morphotest.MorphoTestCase;


public class IdFileMapProfileInformationTest extends MorphoTestCase {
  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public IdFileMapProfileInformationTest(String name) {
      super(name);
     
  }
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTest(new IdFileMapProfileInformationTest("initialize"));
      suite.addTest(new IdFileMapProfileInformationTest("testGetStatusAndDirectories"));
      
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
   * Test the getUpdateStatus and the getObjectDirectories methods
   */
  public void testGetStatusAndDirectories() {
    IdFileMapProfileInformation info = new IdFileMapProfileInformation(profile);
    System.out.println("The profile being added the id-file mapping is "+info.getUpdatedStatus());
    Vector<File> list = info.getIdFileMappingDirectories();
    for(File file : list) {
      System.out.println("The directories needs to be updated for id-file mapping are "+file.getAbsolutePath());
    }
    
    Vector<File> revisionList = info.getRevisionDirectories();
    for(File file : revisionList) {
      System.out.println("The directories needs to be updated for revisions are "+file.getAbsolutePath());
    }
  }

}
