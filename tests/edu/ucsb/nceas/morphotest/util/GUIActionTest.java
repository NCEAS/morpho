/**
 *  '$RCSfile: GUIActionTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-27 01:13:37 $'
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

import java.io.File;
import java.io.IOException;

import java.awt.event.ActionEvent;

/**
 * A JUnit test for testing the GUIAction menu and toolbar system
 */
public class GUIActionTest extends TestCase
{

    private static final String state1 = "TEST_EVENT1";
    private static final String state2 = "TEST_EVENT2";
    private static Morpho morpho;
    private static StateChangeEvent localState1Event = null;
    private static StateChangeEvent localState2Event = null;
    private static StateChangeEvent globalState1Event = null;
    private static StateChangeEvent globalState2Event = null;

    private static MorphoFrame testWindow1;
    private static MorphoFrame testWindow2;
    
    private static Command command1;
    private static Command command2;
    
    private static GUIAction localAction1F2T;
    private static GUIAction localAction1T2F;
    private static GUIAction globalAction1F2T;
    private static GUIAction globalAction1T2F;
    
    static {
    
        ConfigXML config = null;
        try {
            File configDir = new File(ConfigXML.getConfigDirectory());
            File configFile = new File(configDir, "config.xml");
            config = new ConfigXML(configFile.getAbsolutePath());
            morpho = new Morpho(config);
        } catch (IOException ioe) {
          fail("Test failed, couldn't create config.");
        }

        UIController controller = UIController.initialize(morpho);

        command1 = new Command() {
            public void execute(ActionEvent e) {
                Log.debug(9, "Command 1 executed. ActionEvent = "+e);
            }
        };
        assertTrue(command1 != null);

        command2 = new Command() {
            public void execute(ActionEvent e) {
                Log.debug(9, "Command 2 executed. ActionEvent = "+e);
            }
        };
        assertTrue(command2 != null);

        
        ////////////////////////////////////////////////////////////////////////
        localAction1F2T = new GUIAction("Local Button 1", null, command2);
        localAction1F2T.setCommandOnStateChange(state1, command1, 
                                                        GUIAction.EVENT_LOCAL);
        localAction1F2T.setCommandOnStateChange(state2, command2, 
                                                        GUIAction.EVENT_LOCAL);
        localAction1F2T.setEnabledOnStateChange(state1, false, 
                                                        GUIAction.EVENT_LOCAL);
        localAction1F2T.setEnabledOnStateChange(state2, true, 
                                                        GUIAction.EVENT_LOCAL);

        localAction1F2T.setMenuItemPosition(1);
        localAction1F2T.setToolbarPosition(1);
        localAction1F2T.setMenu("Test", 0);
        
        controller.addGuiAction(localAction1F2T);

        
        ////////////////////////////////////////////////////////////////////////
        localAction1T2F = new GUIAction("Local Button 2", null, command1);
        localAction1T2F.setCommandOnStateChange(state1, command2, 
                                                        GUIAction.EVENT_LOCAL);
        localAction1T2F.setCommandOnStateChange(state2, command1, 
                                                        GUIAction.EVENT_LOCAL);
        localAction1T2F.setEnabledOnStateChange(state1, true, 
                                                        GUIAction.EVENT_LOCAL);
        localAction1T2F.setEnabledOnStateChange(state2, false, 
                                                        GUIAction.EVENT_LOCAL);

        localAction1T2F.setMenuItemPosition(2);
        localAction1T2F.setToolbarPosition(2);
        localAction1T2F.setMenu("Test", 0);
        
        controller.addGuiAction(localAction1T2F);
        

        ////////////////////////////////////////////////////////////////////////
        globalAction1F2T = new GUIAction("Global Button 1", null, command2);
        globalAction1F2T.setCommandOnStateChange(state1, command1, 
                                                        GUIAction.EVENT_GLOBAL);
        globalAction1F2T.setCommandOnStateChange(state2, command2, 
                                                        GUIAction.EVENT_GLOBAL);
        globalAction1F2T.setEnabledOnStateChange(state1, false, 
                                                        GUIAction.EVENT_GLOBAL);
        globalAction1F2T.setEnabledOnStateChange(state2, true, 
                                                        GUIAction.EVENT_GLOBAL);

        globalAction1F2T.setMenuItemPosition(3);
        globalAction1F2T.setToolbarPosition(3);
        globalAction1F2T.setMenu("Test", 0);

        controller.addGuiAction(globalAction1F2T);

        
        ////////////////////////////////////////////////////////////////////////
        globalAction1T2F = new GUIAction("Global Button 2", null, command1);
        globalAction1T2F.setCommandOnStateChange(state1, command2, 
                                                        GUIAction.EVENT_GLOBAL);
        globalAction1T2F.setCommandOnStateChange(state2, command1, 
                                                        GUIAction.EVENT_GLOBAL);
        globalAction1T2F.setEnabledOnStateChange(state1, true, 
                                                        GUIAction.EVENT_GLOBAL);
        globalAction1T2F.setEnabledOnStateChange(state2, false, 
                                                        GUIAction.EVENT_GLOBAL);

        globalAction1T2F.setMenuItemPosition(4);
        globalAction1T2F.setToolbarPosition(4);
        globalAction1T2F.setMenu("Test", 0);

        controller.addGuiAction(globalAction1T2F);

        initEnables();
        
        GUIAction exitAction = new GUIAction("EXIT", null, 
            new Command() {
                public void execute(ActionEvent e) {
                    System.exit(0);
                }
            });

        exitAction.setMenuItemPosition(5);
        exitAction.setToolbarPosition(5);
        exitAction.setMenu("Test", 0);

        controller.addGuiAction(exitAction);

        testWindow1 = controller.addWindow("Test Window 1");
        testWindow2 = controller.addWindow("Test Window 2");
    }

    private static void initEnables() {
        localAction1F2T.setEnabled(true);
        localAction1T2F.setEnabled(false);
        globalAction1F2T.setEnabled(true);
        globalAction1T2F.setEnabled(false);
    }
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
        localState1Event  = new StateChangeEvent(testWindow1, state1); 
        localState2Event  = new StateChangeEvent(testWindow1, state2); 
        globalState1Event = new StateChangeEvent(testWindow2, state1); 
        globalState2Event = new StateChangeEvent(testWindow2, state2); 
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
        suite.addTest(new GUIActionTest("testVisual"));
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

        // check initial values bafore any state changs
        initEnables();
        
        assertTrue( localAction1F2T.isEnabled());
        assertTrue(!localAction1T2F.isEnabled());
        assertTrue( globalAction1F2T.isEnabled());
        assertTrue(!globalAction1T2F.isEnabled());

        // Post the first local state change and test if the enabled state changed
        monitor.notifyStateChange(localState1Event);
        
//        assertTrue(!localAction1F2T.isEnabled());
        assertTrue( localAction1T2F.isEnabled());
        assertTrue(!globalAction1F2T.isEnabled());
        assertTrue( globalAction1T2F.isEnabled());

        // Post the second local state change and test if the enabled state changed
        monitor.notifyStateChange(localState2Event);
        
        assertTrue( localAction1F2T.isEnabled());
        assertTrue(!localAction1T2F.isEnabled());
        assertTrue( globalAction1F2T.isEnabled());
        assertTrue(!globalAction1T2F.isEnabled());
        
        
        
        // check initial values bafore any state changs
        initEnables();
        
        assertTrue( localAction1F2T.isEnabled());
        assertTrue(!localAction1T2F.isEnabled());
        assertTrue( globalAction1F2T.isEnabled());
        assertTrue(!globalAction1T2F.isEnabled());

        // Post the first global state change and test if the enabled state changed
        monitor.notifyStateChange(globalState1Event);
        
        assertTrue( localAction1F2T.isEnabled());
        assertTrue(!localAction1T2F.isEnabled());
        assertTrue(!globalAction1F2T.isEnabled());
        assertTrue( globalAction1T2F.isEnabled());
        
        // Post the second globals state change and test if the enabled state changed
        monitor.notifyStateChange(globalState2Event);
        
        assertTrue( localAction1F2T.isEnabled());
        assertTrue(!localAction1T2F.isEnabled());
        assertTrue( globalAction1F2T.isEnabled());
        assertTrue(!globalAction1T2F.isEnabled());
  }

  /**
   * Test if the state monitoring is working for the command switching
   */
  public void testCommand()
  {
//        StateChangeMonitor monitor = StateChangeMonitor.getInstance();
//        assertTrue(monitor != null);
//
//        Command command1 = new Command() {
//            public void execute(ActionEvent e) {
//                Log.debug(9, "Command 1 executed. ActionEvent = "+e);
//            }
//        };
//        assertTrue(command1 != null);
//
//        Command command2 = new Command() {
//            public void execute(ActionEvent e) {
//                Log.debug(9, "Command 2 executed. ActionEvent = "+e);
//            }
//        };
//        assertTrue(command2 != null);
//
//        GUIAction action = new GUIAction("Test", null, command2);
//        action.setCommandOnStateChange(state1, command1, GUIAction.EVENT_LOCAL);
//        action.setCommandOnStateChange(state2, command2, GUIAction.EVENT_LOCAL);
//
//        // Post the first state change and test if the command changed
//        monitor.notifyStateChange(localState1Event);
//        Command current = action.getCommand();
//        assertTrue(current != null);
//        assertTrue(current == command1);
//
//        // Post the second state change and test if the command changed
//        monitor.notifyStateChange(localState2Event);
//        current = action.getCommand();
//        assertTrue(current != null);
//        assertTrue(current == command2);
  }

  /**
   * Test if the state monitoring is working for the command switching
   */
  public void testVisual()
  {
//        StateChangeMonitor monitor = StateChangeMonitor.getInstance();
//        assertTrue(monitor != null);
//
//        testWindow1.setVisible(true);
//
//        Log.debug(9, "Button enabled.  Pause before first event.");
//        // Post the first state change and test if the command changed
//        monitor.notifyStateChange(localState1Event);
//        Command current = localAction1F2T.getCommand();
//        assertTrue(current != null);
//        assertTrue(current == command1);
//
//        Log.debug(9, "Pause between events; button now disabled?");
//        // Post the second state change and test if the command changed
//        monitor.notifyStateChange(localState2Event);
//        current = localAction1F2T.getCommand();
//        assertTrue(current != null);
//        assertTrue(current == command2);
//        Log.debug(9, "Pause after second event. Button enabled again?");
  }

    public static void main(String args[]) {
        Log.getLog().setDebugLevel(51);
        junit.textui.TestRunner.run(GUIActionTest.class);
//        System.exit(0);
    }
}
