package edu.ucsb.nceas.morphotest.framework;

import edu.ucsb.nceas.morpho.framework.EMLTransformToNewestVersionDialog;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;

import java.io.FileNotFoundException;
import java.net.URLStreamHandler;
import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A junit test class for testing class EMLTransformToNewestVersionDialog.
 * @author tao
 *
 */
public class EMLTransformToNewestVersionDialogTest extends TestCase
{
	 /**
	   * Constructor to build the test
	   *
	   * @param name the name of the test method
	   */
	  public EMLTransformToNewestVersionDialogTest(String name)
	  {
	    super(name);
	  }

	  /**
	   * Establish a testing framework by initializing appropriate objects
	   */
	  public void setUp()
	  { 

	  }

	  /**
	   * Release any objects after tests are complete
	   */
	  public void tearDown()
	  {
	  }

	  /**
	   * Create a suite of tests to be run together
	   */
	  public static Test suite()
	  {
	    TestSuite suite = new TestSuite();
	    suite.addTest(new EMLTransformToNewestVersionDialogTest("initialize"));
	    suite.addTest(new EMLTransformToNewestVersionDialogTest("testActions"));
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
	   * Check the if the action buttons works.
	   */
	  public void testActions()
	  {
		  MorphoFrame frame = MorphoFrame.getInstance();
		  EMLTransformToNewestVersionDialog dialog = new EMLTransformToNewestVersionDialog(frame);
	  }


}
