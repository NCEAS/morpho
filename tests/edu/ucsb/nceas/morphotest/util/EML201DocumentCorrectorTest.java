/**
 *  '$RCSfile: EML201DocumentCorrectorTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-06-25 23:39:01 $'
 * '$Revision: 1.1 $'
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
	private String docid="tao.12104.1";
	static {
        try {
            File configDir = new File(ConfigXML.getConfigDirectory());
            File configFile = new File(configDir, "config.xml");
            config = new ConfigXML(configFile.getAbsolutePath());
            morpho = new Morpho(config);
        } catch (IOException ioe) {
          fail("Test failed, couldn't create config.");
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

}
