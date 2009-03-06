/**
 *  '$RCSfile: EML210ValidateTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-06 21:37:37 $'
 * '$Revision: 1.2 $'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ucsb.nceas.morphotest.datapackage;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Vector;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.EML210Validate;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.EML201DocumentCorrector;
import edu.ucsb.nceas.morpho.util.Log;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public class EML210ValidateTest extends TestCase
{
	
	private static Morpho morpho;
	private static ConfigXML config = null;
	private static final String EMLFILEWITHSPACE = "tests/testfiles/eml210-whitespace.xml";
	
	static {
        try {
            File configDir = new File(ConfigXML.getConfigDirectory());
            File configFile = new File(configDir, "config.xml");
            config = new ConfigXML(configFile.getAbsolutePath());
            File currentProfileLocation = new File(configDir, "currentprofile.xml");
            Log.setDebugLevel(40);
            /*ConfigXML currentProfileConfig = new ConfigXML(currentProfileLocation.getAbsolutePath());
            String currentProfileName = currentProfileConfig.get("current_profile", 0);
            String profileDirName = config.getConfigDirectory()+
    		File.separator+config.get("profile_directory", 0)+
    		File.separator+currentProfileName;
            //System.out.println("the profile dir is "+profileDirName);
            File profileLocation = new File(profileDirName, currentProfileName+".xml");
            ConfigXML profile = new ConfigXML(profileLocation.getAbsolutePath());
            String lastId = profile.get("lastId", 0);
            String scope = profile.get("scope", 0);
            String datadirName = profile.get("datadir", 0);
            String datadir = profileDirName+File.separator+datadirName+File.separator+scope;
            //System.out.println("the data dir is ======== "+datadir);
            copyFileToDataDir(datadir+File.separator+lastId+".1");
            docid = scope+"."+lastId+".1";*/
        } catch (IOException ioe) {
          fail("Test failed, couldn't create config."+ioe.getMessage());
        }
	}

        //UIController controller = UIController.initialize(morpho);
        
        
       
        /**
         * Constructor to build the test
         *
         * @param name the name of the test method
         */
        public EML210ValidateTest(String name)
        {
            super(name);
        }
        /**
         * Create a suite of tests to be run together
         */
        public static Test suite()
        {
            TestSuite suite = new TestSuite();
            suite.addTest(new EML210ValidateTest("initialize"));
            suite.addTest(new EML210ValidateTest("testParseWithspace"));
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
        	try
        	{
        	  Reader xml = new FileReader(new File(EMLFILEWITHSPACE));
        	   EML210Validate validate = new EML210Validate();
        	   validate.parse(xml);
        	   assertFalse("Failed to determine the invalid path is not empty", validate.invalidPathIsEmpty());
        	   Vector errorPath = validate.getInvalidPathList();
        	   
        	   int size = errorPath.size();
        	   for (int i=0; i<size; i++)
        	   {
        		   String path = (String)errorPath.elementAt(i);
        		   if (i==0)
        		   {
        			   assertTrue("Couldn't find white space path /eml:eml/access/allow/principal", path.equals("/eml:eml/access/allow/principal"));
        		   }
        		   else if (i==1)
        		   {
        			   assertTrue("Could find whitespace path /eml:eml/dataset/title", path.equals("/eml:eml/dataset/title"));
        		   }
        		   else
        		   {
        			   fail("find more whitespace path than expect: " + path);
        		   }
        		
        	   }
        	   xml.close();
        	}
        	catch(Exception e)
        	{
        		fail("Failed to get valide element path: " + e.getMessage());
        	}
        }
        

}
