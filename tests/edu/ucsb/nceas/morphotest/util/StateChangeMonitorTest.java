/**
 *  '$RCSfile: StateChangeMonitorTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-28 00:26:50 $'
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

package edu.ucsb.nceas.morphotest.util;

import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * A JUnit test for testing the StateChange monitoring and notification
 */
public class StateChangeMonitorTest extends TestCase
{

    private static final String state = "TEST_EVENT";
    private static final String state2 = "TEST_EVENT2";

  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public StateChangeMonitorTest(String name)
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
    suite.addTest(new StateChangeMonitorTest("initialize"));
    suite.addTest(new StateChangeMonitorTest("testListener"));
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
   * Test if the state monitoring is working
   */
  public void testListener()
  {
        StateChangeEvent event = new StateChangeEvent(null, state); 
        assertTrue(event != null);

        ListenerAdapter listener = new ListenerAdapter();
        assertTrue(listener != null);

        StateChangeMonitor monitor = StateChangeMonitor.getInstance();
        assertTrue(monitor != null);

        // Register and test a listener
        monitor.addStateChangeListener(state, (StateChangeListener)listener);
        monitor.notifyStateChange(event);

        assertTrue(listener.lastState.equals(state));
        assertTrue(listener.lastSource == this);

        // Post an event for which we are not listening
        StateChangeEvent event2 = new StateChangeEvent(null, state2); 
        assertTrue(event2 != null);

        monitor.notifyStateChange(event2);
        assertTrue(listener.lastState.equals(state));

        // Now listen for state2 and post it again
        monitor.addStateChangeListener(state2, (StateChangeListener)listener);
        monitor.notifyStateChange(event2);
          
        assertTrue(listener.lastState.equals(state2));
        assertTrue(listener.lastSource == this);

        // Now remove the state listener and post it, we shouldn't be notified
        monitor.removeStateChangeListener(state, (StateChangeListener)listener);
        monitor.notifyStateChange(event);

        assertTrue(listener.lastState.equals(state2));
        assertTrue(listener.lastSource == this);
  }

  /**
   * An inner class to test StateChangeListener
   */
  public class ListenerAdapter implements StateChangeListener
  {
      protected String lastState = null;
      protected Object lastSource = null;

      public void handleStateChange(StateChangeEvent event)
      {
          lastState = event.getChangedState();
          lastSource = event.getSource();
      }
  }
}
