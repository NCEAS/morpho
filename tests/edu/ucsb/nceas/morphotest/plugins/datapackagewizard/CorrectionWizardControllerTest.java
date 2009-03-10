package edu.ucsb.nceas.morphotest.plugins.datapackagewizard;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.datapackage.EML210Validate;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CorrectionWizardController;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morphotest.datapackage.EML210ValidateTest;

public class CorrectionWizardControllerTest extends TestCase
{
	static
	{
         Log.setDebugLevel(48);
	}
	
    /**
     * Constructor to build the test
     *
     * @param name the name of the test method
     */
    public CorrectionWizardControllerTest(String name)
    {
        super(name);
    }
    /**
     * Create a suite of tests to be run together
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new CorrectionWizardControllerTest("initialize"));
        suite.addTest(new CorrectionWizardControllerTest("testParseWithspace"));
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
    
    public void testParseWithspace()
    {
    	Vector errorList = new Vector();
    	CorrectionWizardController controller = new CorrectionWizardController(errorList);  
    }
    

}
