/**
 *  '$RCSfile: ConnectTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-02 22:09:35 $'
 * '$Revision: 1.4 $'
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

package edu.ucsb.nceas.morpho.framework.tests;

import edu.ucsb.nceas.morpho.framework.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A JUnit test for testing the connection capabilities of the framework.
 */
public class ConnectTest extends TestCase
{
  // These need to be set to a valid metacat account for the test to work
  private static String configFile = "lib/config.xml";
  private static String username = "jones";
  private static String password = "change-to-valid-password";

  ClientFramework framework = null;

  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public ConnectTest(String name)
  {
    super(name);
  }

  /**
   * Establish a testing framework by initializing appropriate objects
   */
  public void setUp()
  {
    ConfigXML config = new ConfigXML(configFile);
    framework = new ClientFramework(config);
    framework.setUserName(username);
    framework.setPassword(password);
  }

  /**
   * Release any objects after tests are complete
   */
  public void tearDown()
  {
    framework.dispose();
  }

  /**
   * Create a suite of tests to be run together
   */
  public static Test suite()
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new ConnectTest("testValidLogin"));
    suite.addTest(new ConnectTest("testLogout"));
    suite.addTest(new ConnectTest("testInvalidLogin"));
    return suite;
  }

  /**
   * Test if the logIn() function works given a valid username and password
   */
  public void testValidLogin()
  {
    boolean connected = framework.logIn();
    assert(connected == true);
    assert(framework.isConnected() == true);
  }

  /**
   * Test if the logOut() function works
   */
  public void testLogout()
  {
    framework.logOut();
    assert(framework.isConnected() == false);
  }

  /**
   * Test if the logIn() function works given an invalid username and password
   */
  public void testInvalidLogin()
  {
    framework.setPassword("garbage");
    boolean connected = framework.logIn();
    assert(connected == false);
    assert(framework.isConnected() == false);
  }
}
