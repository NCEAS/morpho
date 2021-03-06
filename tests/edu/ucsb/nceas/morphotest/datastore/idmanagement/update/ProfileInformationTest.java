package edu.ucsb.nceas.morphotest.datastore.idmanagement.update;

import java.io.File;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.datastore.idmanagement.update.ObjectDirectory;
import edu.ucsb.nceas.morpho.datastore.idmanagement.update.ProfileInformation;
import edu.ucsb.nceas.morphotest.MorphoTestCase;


public class ProfileInformationTest extends MorphoTestCase {
  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public ProfileInformationTest(String name) {
      super(name);
     
  }
  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() {
      TestSuite suite = new TestSuite();
      suite.addTest(new ProfileInformationTest("initialize"));
      suite.addTest(new ProfileInformationTest("testGetStatusAndDirectories"));
      
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
    ProfileInformation info = new ProfileInformation(profile);
    System.out.println("The profile being added the id-file mapping is "+info.getUpdatedStatus());
    Vector<ObjectDirectory> list = info.getIdFileMappingDirectories();
    for(ObjectDirectory file : list) {
      System.out.println("The directories needs to be updated for id-file mapping are "+file.getDirectory().getAbsolutePath());
    }
    
    Vector<ObjectDirectory> revisionList = info.getRevisionDirectories();
    for(ObjectDirectory file : revisionList) {
      System.out.println("The directories needs to be updated for revisions are "+file.getDirectory().getAbsolutePath());
    }
  }

}
