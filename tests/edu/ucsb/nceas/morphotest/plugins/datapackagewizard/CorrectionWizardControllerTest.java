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
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.DataPackagePlugin;
import edu.ucsb.nceas.morpho.datapackage.EML210Validate;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.CorrectionWizardController;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morphotest.datapackage.EML210ValidateTest;
import edu.ucsb.nceas.utilities.XMLUtilities;

public class CorrectionWizardControllerTest extends TestCase
{
	private static final String EMLFILEWITHSPACE = "tests/testfiles/eml210-whitespace.xml";
	
	private static Morpho morpho;
	private static ConfigXML config = null;
	private static String docid="tao.12104.1";
	static {
        try {
        	Log.setDebugLevel(47);
            File configDir = new File(ConfigXML.getConfigDirectory());
            File configFile = new File(configDir, "config.xml");
            config = new ConfigXML(configFile.getAbsolutePath());
            File currentProfileLocation = new File(configDir, "currentprofile.xml");
            ConfigXML currentProfileConfig = new ConfigXML(currentProfileLocation.getAbsolutePath());
            String currentProfileName = currentProfileConfig.get("current_profile", 0);
            String profileDirName = config.getConfigDirectory()+
    		File.separator+config.get("profile_directory", 0)+
    		File.separator+currentProfileName;
            //System.out.println("the profile dir is "+profileDirName);
            File profileLocation = new File(profileDirName, currentProfileName+".xml");
            ConfigXML profile = new ConfigXML(profileLocation.getAbsolutePath());
            ServiceController services = ServiceController.getInstance();
            morpho = new Morpho(config);
            UIController controller = UIController.initialize(morpho);
            DataPackagePlugin plug = new DataPackagePlugin(morpho);
            plug.initialize(morpho);
        } catch (IOException ioe) {
          fail("Test failed, couldn't create config."+ioe.getMessage());
        }
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
	    	 Thread.sleep(15000);
    	}
    	catch (Exception e)
    	{
    		fail("Couldn't start CorrectionWizardController "+e.getMessage());
    	}
    }
    

}
