/**
 *  '$RCSfile: ConnectTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-04-25 18:59:58 $'
 * '$Revision: 1.1.2.1 $'
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

package edu.ucsb.nceas.dtclient.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.awt.Container;
import javax.swing.Action;
import edu.ucsb.nceas.dtclient.*;

/**
 * The ConnectTest class is a simple unit test of logging into and out of
 * Metacat.
 */
public class ConnectTest extends TestCase
{
  ClientFramework framework = null;
  String username = null;
  String password = null;

  /**
   * Construct the test instance
   */
  public ConnectTest(String name) 
  {
    super(name);
  }

  /** 
   * Initialize variables
   */
  public void setUp()
  {
    username = "jones";
    password = "change-to-correct-pw-or-test-will-fail";

    framework = new ClientFramework();
  }

  /** 
   * Release and dispose :-)
   */
  public void tearDown()
  {
    framework.dispose();
  }

  /**
   * Test the logIn function
   */
  public void testLogIn()
  {
    framework.setUserName(username);
    framework.setPassword(password);
    boolean connected = framework.logIn();
    assert(connected == true);
    assert(framework.isConnected() == true);
  }

  /**
   * Test the logOut function
   */
  public void testLogOut()
  {
    framework.logOut();
    assert(framework.isConnected() == false);
  }

  /**
   * Test the logIn function with invalid password
   */
  public void testInvalidLogIn()
  {
    framework.setUserName(username);
    framework.setPassword("wrongpassword");
    boolean connected = framework.logIn();
    assert(connected == false);
    assert(framework.isConnected() == false);
  }

  /**
   * Create a test suite to run all of the tests
   */
  public static Test suite() 
  {
    TestSuite suite = new TestSuite();
    suite.addTest(new ConnectTest("testLogIn"));
    suite.addTest(new ConnectTest("testLogOut"));
    suite.addTest(new ConnectTest("testInvalidLogIn"));
    return suite;
  }
}
