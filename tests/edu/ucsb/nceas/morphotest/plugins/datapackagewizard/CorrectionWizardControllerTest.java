package edu.ucsb.nceas.morphotest.plugins.datapackagewizard;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

import org.w3c.dom.Document;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import edu.ucsb.nceas.morpho.datapackage.EML210Validate;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CorrectionWizardController;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morphotest.datapackage.EML210ValidateTest;
import edu.ucsb.nceas.utilities.XMLUtilities;

public class CorrectionWizardControllerTest extends TestCase
{
	private static final String EMLFILEWITHSPACE = "tests/testfiles/eml210-whitespace.xml";
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
        suite.addTest(new CorrectionWizardControllerTest("testStartWizard"));
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
    
    public void testStartWizard() 
    {
    	try
    	{
	    	 Reader xml = new FileReader(new File(EMLFILEWITHSPACE));
	    	 Document metadata = XMLUtilities.getXMLReaderAsDOMDocument(xml);
	    	 xml.close();
	    	 xml = new FileReader(new File(EMLFILEWITHSPACE));
	  	     EML210Validate validate = new EML210Validate();
	  	     validate.parse(xml);
	  	     xml.close();
	    	 Vector errorList = validate.getInvalidPathList();   	
	    	 CorrectionWizardController controller = new CorrectionWizardController(errorList, metadata);  
	    	 controller.startWizard();
    	}
    	catch (Exception e)
    	{
    		fail("Couldn't start CorrectionWizardController "+e.getMessage());
    	}
    }
    

}
