/**
 *  '$RCSfile: GUIActionTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-09-06 07:12:16 $'
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

package edu.ucsb.nceas.morphotest.util;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * A JUnit test for testing the GUIAction menu and toolbar system
 */
public class GUIActionTest extends TestCase
{

    private static final String state1 = "TEST_EVENT1";
    private static final String state2 = "TEST_EVENT2";
    private Morpho morpho;
    StateChangeEvent event1 = null;
    StateChangeEvent event2 = null;

  /**
   * Constructor to build the test
   *
   * @param name the name of the test method
   */
  public GUIActionTest(String name)
  {
    super(name);
  }

  /**
   * Establish a testing framework by initializing appropriate objects
   */
  public void setUp()
  {
      event1 = new StateChangeEvent(this, state1); 
      event2 = new StateChangeEvent(this, state2); 
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
    suite.addTest(new GUIActionTest("initialize"));
    suite.addTest(new GUIActionTest("testEnabled"));
    suite.addTest(new GUIActionTest("testCommand"));
    //suite.addTest(new GUIActionTest("testVisual"));
    suite.addTest(new GUIActionTest("testAddGUIAction"));
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
   * Test if the state monitoring is working for the enabled state
   */
  public void testEnabled()
  {
        StateChangeMonitor monitor = StateChangeMonitor.getInstance();
        assertTrue(monitor != null);

        Command command1 = new Command() {
            public void execute(ActionEvent event) {
                Log.debug(20, "Test command executed.");
            }
        };

        GUIAction action = new GUIAction("Test", null, command1);
        action.setEnabledOnStateChange(state1, false);
        action.setEnabledOnStateChange(state2, true);

        // Post the first state change and test if the enabled state changed
        monitor.notifyStateChange(event1);
        assertTrue(!action.isEnabled());

        // Post the second state change and test if the enabled state changed
        monitor.notifyStateChange(event2);
        assertTrue(action.isEnabled());
  }

  /**
   * Test if the state monitoring is working for the command switching
   */
  public void testCommand()
  {
        StateChangeMonitor monitor = StateChangeMonitor.getInstance();
        assertTrue(monitor != null);

        Command command1 = new Command() {
            public void execute(ActionEvent event) {
                Log.debug(9, "Command 1 executed.");
            }
        };
        assertTrue(command1 != null);

        Command command2 = new Command() {
            public void execute(ActionEvent event) {
                Log.debug(9, "Command 2 executed.");
            }
        };
        assertTrue(command2 != null);

        GUIAction action = new GUIAction("Test", null, command2);
        action.setCommandOnStateChange(state1, command1);
        action.setCommandOnStateChange(state2, command2);

        // Post the first state change and test if the command changed
        monitor.notifyStateChange(event1);
        Command current = action.getCommand();
        assertTrue(current != null);
        assertTrue(current == command1);

        // Post the second state change and test if the command changed
        monitor.notifyStateChange(event2);
        current = action.getCommand();
        assertTrue(current != null);
        assertTrue(current == command2);
  }

  /**
   * Test if the state monitoring is working for the command switching
   */
  public void testVisual()
  {

        ConfigXML config = null;
        try {
            File configDir = new File(ConfigXML.getConfigDirectory());
            File configFile = new File(configDir, "config.xml");
            config = new ConfigXML(configFile.getAbsolutePath());
            morpho = new Morpho(config);
        } catch (IOException ioe) {
          fail("Test failed, couldn't create config.");
        }

        StateChangeMonitor monitor = StateChangeMonitor.getInstance();
        assertTrue(monitor != null);

        Command command1 = new Command() {
            public void execute(ActionEvent event) {
                Log.debug(9, "Command 1 executed.");
            }
        };
        assertTrue(command1 != null);

        Command command2 = new Command() {
            public void execute(ActionEvent event) {
                Log.debug(9, "Command 2 executed.");
            }
        };
        assertTrue(command2 != null);

        GUIAction action = new GUIAction("Test", null, command2);
        action.setCommandOnStateChange(state1, command1);
        action.setCommandOnStateChange(state2, command2);
        action.setEnabledOnStateChange(state1, false);
        action.setEnabledOnStateChange(state2, true);

        GUIAction[] toolbarActions = new GUIAction[1];
        toolbarActions[0] = action;
        UIController controller = UIController.initialize(morpho);
        controller.addToolbarActions(toolbarActions);
        controller.addMenu("Window", new Integer(5));
        MorphoFrame test = controller.addWindow("Test Window");
        test.setVisible(true);

        Log.debug(9, "Button enabled.  Pause before first event.");
        // Post the first state change and test if the command changed
        monitor.notifyStateChange(event1);
        Command current = action.getCommand();
        assertTrue(current != null);
        assertTrue(current == command1);

        Log.debug(9, "Pause between events; button now disabled?");
        // Post the second state change and test if the command changed
        monitor.notifyStateChange(event2);
        current = action.getCommand();
        assertTrue(current != null);
        assertTrue(current == command2);
        Log.debug(9, "Pause after second event. Button enabled again?");
  }

  /**
   * Test if the state monitoring is working for the command switching
   */
  public void testAddGUIAction()
  {

        ConfigXML config = null;
        try {
            File configDir = new File(ConfigXML.getConfigDirectory());
            File configFile = new File(configDir, "config.xml");
            config = new ConfigXML(configFile.getAbsolutePath());
            morpho = new Morpho(config);
        } catch (IOException ioe) {
          fail("Test failed, couldn't create config.");
        }

        StateChangeMonitor monitor = StateChangeMonitor.getInstance();
        assertTrue(monitor != null);

        Command command1 = new Command() {
            public void execute(ActionEvent event) {
                Log.debug(9, "Command 1 executed.");
            }
        };
        assertTrue(command1 != null);

        Command command2 = new Command() {
            public void execute(ActionEvent event) {
                Log.debug(9, "Command 2 executed.");
            }
        };
        assertTrue(command2 != null);

        GUIAction action = new GUIAction("Test", null, command2);
        action.setMenu("Testing", 0);
        action.setCommandOnStateChange(state1, command1);
        action.setCommandOnStateChange(state2, command2);
        action.setEnabledOnStateChange(state1, false);
        action.setEnabledOnStateChange(state2, true);

        UIController controller = UIController.initialize(morpho);
        MorphoFrame test = controller.addWindow("Test Window");
        controller.addGuiAction(action);
        test.setVisible(true);

        Log.debug(9, "Button enabled.  Pause before first event.");
        // Post the first state change and test if the command changed
        monitor.notifyStateChange(event1);
        Command current = action.getCommand();
        assertTrue(current != null);
        assertTrue(current == command1);

        Log.debug(9, "Pause between events; button now disabled?");
        // Post the second state change and test if the command changed
        monitor.notifyStateChange(event2);
        current = action.getCommand();
        assertTrue(current != null);
        assertTrue(current == command2);
        Log.debug(9, "Pause after second event. Button enabled again?");

        GUIAction action2 = new GUIAction("Test2", null, command1);
        action.setMenu("Testing", 0);
        action.setCommandOnStateChange(state1, command1);
        action.setCommandOnStateChange(state2, command2);
        action.setEnabledOnStateChange(state1, false);
        action.setEnabledOnStateChange(state2, true);
        controller.addGuiAction(action2);

        Log.debug(9, "Second action added to menu. Pause before removing it.");
        controller.removeGuiAction(action2);
        Log.debug(9, "Second action removed.");
  }
}
