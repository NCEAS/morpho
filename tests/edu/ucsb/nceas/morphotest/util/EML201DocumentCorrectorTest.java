/**
 *  '$RCSfile: EML201DocumentCorrectorTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-09-25 22:56:13 $'
 * '$Revision: 1.3 $'
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
package edu.ucsb.nceas.morphotest.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.EML201DocumentCorrector;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
public class EML201DocumentCorrectorTest extends TestCase
{
	
	private static Morpho morpho;
	private static ConfigXML config = null;
	private static String docid="tao.12104.1";
	static {
        try {
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
            String lastId = profile.get("lastId", 0);
            String scope = profile.get("scope", 0);
            String datadirName = profile.get("datadir", 0);
            String datadir = profileDirName+File.separator+datadirName+File.separator+scope;
            //System.out.println("the data dir is ======== "+datadir);
            copyFileToDataDir(datadir+File.separator+lastId+".1");
            docid = scope+"."+lastId+".1";
        } catch (IOException ioe) {
          fail("Test failed, couldn't create config."+ioe.getMessage());
        }
	}

        UIController controller = UIController.initialize(morpho);
       
        /**
         * Constructor to build the test
         *
         * @param name the name of the test method
         */
        public EML201DocumentCorrectorTest(String name)
        {
            super(name);
        }
        /**
         * Create a suite of tests to be run together
         */
        public static Test suite()
        {
            TestSuite suite = new TestSuite();
            suite.addTest(new EML201DocumentCorrectorTest("initialize"));
            suite.addTest(new EML201DocumentCorrectorTest("testCorrectDocument"));
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
        
        public void testCorrectDocument()
        {
        	try
        	{
        	   EML201DocumentCorrector corrector = new EML201DocumentCorrector(docid);
        	   corrector.correctDocument();
        	}
        	catch(Exception e)
        	{
        		fail("Failed to correct the document because of the exception: " + e.getMessage());
        	}
        }
        
        /*
         * Copy the incorrect eml201 file from test file to data file dir
         */
        static private void copyFileToDataDir(String destinationFile) throws IOException
        {
        	 File source = new File("./tests/testfiles/eml201-reference-system.xml");
        	 FileReader reader = new FileReader(source);
        	 File destination = new File(destinationFile);
        	 if (destination.exists())
        	 {
        		 destination.delete();
        	 }
        	 FileWriter writer = new FileWriter(destination);
        	 char[] cbuf = new char[1024];
        	  int size = reader.read(cbuf);
        	 while (size != -1)
        	 {
        		 writer.write(cbuf, 0, size);
        		 size = reader.read(cbuf);
        	 }
        	 writer.close();
        	 reader.close();
        }

}
