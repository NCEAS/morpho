/**
 *  '$RCSfile: GUIActionTest.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2002-09-27 22:15:42 $'
 * '$Revision: 1.5 $'
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
    private static StateChangeEvent eventWin1State1 = null;
    private static StateChangeEvent eventWin1State2 = null;
    private static StateChangeEvent eventWin2State1 = null;
    private static StateChangeEvent eventWin2State2 = null;

    private static MorphoFrame testWindow1;
    private static MorphoFrame testWindow2;
    
    private static Command command1;
    private static Command command2;
    
    private static GUIAction ORIG_localAction1F2T;
    private static GUIAction ORIG_localAction1T2F;
    private static GUIAction ORIG_globalAction1F2T;
    private static GUIAction ORIG_globalAction1T2F;
    
    private static GUIAction win1LocalAction1F2T;
    private static GUIAction win1LocalAction1T2F; 
    private static GUIAction win1GlobalAction1F2T; 
    private static GUIAction win1GlobalAction1T2F; 
    
    private static GUIAction win2LocalAction1F2T; 
    private static GUIAction win2LocalAction1T2F; 
    private static GUIAction win2GlobalAction1F2T; 
    private static GUIAction win2GlobalAction1T2F; 
    
    private static StateChangeMonitor monitor;
    
    
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
                Log.debug(9, "Command 1 executed. ActionEvent = "
                                                        +e.getActionCommand());
            }
        };
        assertTrue(command1 != null);

        command2 = new Command() {
            public void execute(ActionEvent e) {
                Log.debug(9, "Command 2 executed. ActionEvent = "
                                                        +e.getActionCommand());
            }
        };
        assertTrue(command2 != null);

        
        ////////////////////////////////////////////////////////////////////////
        ORIG_localAction1F2T = new GUIAction("Local Button 1", null, command2);
        
        ORIG_localAction1F2T.setCommandOnStateChange(state1, command1, 
                                                        GUIAction.EVENT_LOCAL);
        ORIG_localAction1F2T.setCommandOnStateChange(state2, command2, 
                                                        GUIAction.EVENT_LOCAL);
        ORIG_localAction1F2T.setEnabledOnStateChange(state1, false, 
                                                        GUIAction.EVENT_LOCAL);
        ORIG_localAction1F2T.setEnabledOnStateChange(state2, true, 
                                                        GUIAction.EVENT_LOCAL);

        ORIG_localAction1F2T.setMenuItemPosition(1);
        ORIG_localAction1F2T.setToolbarPosition(1);
        ORIG_localAction1F2T.setMenu("Test", 0);
        
        controller.addGuiAction(ORIG_localAction1F2T);
        
        ////////////////////////////////////////////////////////////////////////
        ORIG_localAction1T2F = new GUIAction("Local Button 2", null, command1);
        ORIG_localAction1T2F.setCommandOnStateChange(state1, command2, 
                                                        GUIAction.EVENT_LOCAL);
        ORIG_localAction1T2F.setCommandOnStateChange(state2, command1, 
                                                        GUIAction.EVENT_LOCAL);
        ORIG_localAction1T2F.setEnabledOnStateChange(state1, true, 
                                                        GUIAction.EVENT_LOCAL);
        ORIG_localAction1T2F.setEnabledOnStateChange(state2, false, 
                                                        GUIAction.EVENT_LOCAL);

        ORIG_localAction1T2F.setMenuItemPosition(2);
        ORIG_localAction1T2F.setToolbarPosition(2);
        ORIG_localAction1T2F.setMenu("Test", 0);
        
        controller.addGuiAction(ORIG_localAction1T2F);
        

        ////////////////////////////////////////////////////////////////////////
        ORIG_globalAction1F2T = new GUIAction("Global Button 1", null, command2);
        ORIG_globalAction1F2T.setCommandOnStateChange(state1, command1, 
                                                        GUIAction.EVENT_GLOBAL);
        ORIG_globalAction1F2T.setCommandOnStateChange(state2, command2, 
                                                        GUIAction.EVENT_GLOBAL);
        ORIG_globalAction1F2T.setEnabledOnStateChange(state1, false, 
                                                        GUIAction.EVENT_GLOBAL);
        ORIG_globalAction1F2T.setEnabledOnStateChange(state2, true, 
                                                        GUIAction.EVENT_GLOBAL);

        ORIG_globalAction1F2T.setMenuItemPosition(3);
        ORIG_globalAction1F2T.setToolbarPosition(3);
        ORIG_globalAction1F2T.setMenu("Test", 0);

        controller.addGuiAction(ORIG_globalAction1F2T);

        
        ////////////////////////////////////////////////////////////////////////
        ORIG_globalAction1T2F = new GUIAction("Global Button 2", null, command1);
        ORIG_globalAction1T2F.setCommandOnStateChange(state1, command2, 
                                                        GUIAction.EVENT_GLOBAL);
        ORIG_globalAction1T2F.setCommandOnStateChange(state2, command1, 
                                                        GUIAction.EVENT_GLOBAL);
        ORIG_globalAction1T2F.setEnabledOnStateChange(state1, true, 
                                                        GUIAction.EVENT_GLOBAL);
        ORIG_globalAction1T2F.setEnabledOnStateChange(state2, false, 
                                                        GUIAction.EVENT_GLOBAL);

        ORIG_globalAction1T2F.setMenuItemPosition(4);
        ORIG_globalAction1T2F.setToolbarPosition(4);
        ORIG_globalAction1T2F.setMenu("Test", 0);

        controller.addGuiAction(ORIG_globalAction1T2F);

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
        assertNotNull(testWindow1);
        assertNotNull(testWindow2);
        testWindow1.setVisible(true);
        testWindow2.setVisible(true);
        testWindow1.setBounds(10,10,640,480);
        testWindow2.setBounds(200,200,640,480);
        
        monitor = StateChangeMonitor.getInstance();
        assertTrue(monitor != null);
        
        //get instances of the actual clones added to the 
        //windows by UIController
        win1LocalAction1F2T 
            = controller.getGUIActionCloneUsedByMorphoFrame( 
                                                        ORIG_localAction1F2T,
                                                        testWindow1);
        win1LocalAction1T2F 
            = controller.getGUIActionCloneUsedByMorphoFrame( 
                                                        ORIG_localAction1T2F,
                                                        testWindow1);
        win1GlobalAction1F2T 
            = controller.getGUIActionCloneUsedByMorphoFrame( 
                                                        ORIG_globalAction1F2T,
                                                        testWindow1);
        win1GlobalAction1T2F 
            = controller.getGUIActionCloneUsedByMorphoFrame( 
                                                        ORIG_globalAction1T2F,
                                                        testWindow1);
        
        win2LocalAction1F2T 
            = controller.getGUIActionCloneUsedByMorphoFrame( 
                                                        ORIG_localAction1F2T,
                                                        testWindow2);
        win2LocalAction1T2F 
            = controller.getGUIActionCloneUsedByMorphoFrame( 
                                                        ORIG_localAction1T2F,
                                                        testWindow2);
        win2GlobalAction1F2T 
            = controller.getGUIActionCloneUsedByMorphoFrame( 
                                                        ORIG_globalAction1F2T,
                                                        testWindow2);
        win2GlobalAction1T2F 
            = controller.getGUIActionCloneUsedByMorphoFrame( 
                                                        ORIG_globalAction1T2F,
                                                        testWindow2);
        initEnables_Commands();
    }

    private static void initEnables_Commands() {
        win1LocalAction1F2T.setEnabled( true);
        win1LocalAction1T2F.setEnabled( false);
        win1GlobalAction1F2T.setEnabled(true);
        win1GlobalAction1T2F.setEnabled(false);
        
        win2LocalAction1F2T.setEnabled( true);
        win2LocalAction1T2F.setEnabled( false);
        win2GlobalAction1F2T.setEnabled(true);
        win2GlobalAction1T2F.setEnabled(false);
    
        win1LocalAction1F2T.setCommand( command2);
        win1LocalAction1T2F.setCommand( command1);
        win1GlobalAction1F2T.setCommand(command2);
        win1GlobalAction1T2F.setCommand(command1);
        
        win2LocalAction1F2T.setCommand( command2);
        win2LocalAction1T2F.setCommand( command1);
        win2GlobalAction1F2T.setCommand(command2);
        win2GlobalAction1T2F.setCommand(command1);
    
        assertNotNull(win1LocalAction1F2T.getCommand());
        assertNotNull(win1LocalAction1T2F.getCommand());
        assertNotNull(win1GlobalAction1F2T.getCommand());
        assertNotNull(win1GlobalAction1T2F.getCommand());
    
        assertNotNull(win2LocalAction1F2T.getCommand());
        assertNotNull(win2LocalAction1T2F.getCommand());
        assertNotNull(win2GlobalAction1F2T.getCommand());
        assertNotNull(win2GlobalAction1T2F.getCommand());
        doAsserts(true,false,true,false,
                  true,false,true,false,
                  2,1,2,1,      2,1,2,1 );
        
        echoEnabledStatus();
    }

    private static void echoEnabledStatus() {
    
        System.err.println("->win1LocalAction1F2T.isEnabled()="
                                            +win1LocalAction1F2T.isEnabled());
        System.err.println("->win1LocalAction1T2F.isEnabled()="
                                            +win1LocalAction1T2F.isEnabled());
        System.err.println("->win1GlobalAction1F2T.isEnabled()="
                                            +win1GlobalAction1F2T.isEnabled());
        System.err.println("->win1GlobalAction1T2F.isEnabled()="
                                            +win1GlobalAction1T2F.isEnabled());
        System.err.println("->win2LocalAction1F2T.isEnabled()="
                                            +win2LocalAction1F2T.isEnabled());
        System.err.println("->win2LocalAction1T2F.isEnabled()="
                                            +win2LocalAction1T2F.isEnabled());
        System.err.println("->win2GlobalAction1F2T.isEnabled()="
                                            +win2GlobalAction1F2T.isEnabled());
        System.err.println("->win2GlobalAction1T2F.isEnabled()="
                                            +win2GlobalAction1T2F.isEnabled());
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
        eventWin1State1 = new StateChangeEvent(testWindow1, state1); 
        eventWin1State2 = new StateChangeEvent(testWindow1, state2); 
        eventWin2State1 = new StateChangeEvent(testWindow2, state1); 
        eventWin2State2 = new StateChangeEvent(testWindow2, state2); 
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

    private static void doAsserts(
                        boolean ENABLED_w1Loc1F2T,  boolean ENABLED_w1Loc1T2F,
                        boolean ENABLED_w1Glob1F2T, boolean ENABLED_w1Glob1T2F,
                          
                        boolean ENABLED_w2Loc1F2T,  boolean ENABLED_w2Loc1T2F,
                        boolean ENABLED_w2Glob1F2T, boolean ENABLED_w2Glob1T2F,
                           
                        int     COMMAND_w1Loc1F2T,  int     COMMAND_w1Loc1T2F,
                        int     COMMAND_w1Glob1F2T, int     COMMAND_w1Glob1T2F, 
                        
                        int     COMMAND_w2Loc1F2T,  int     COMMAND_w2Loc1T2F,
                        int     COMMAND_w2Glob1F2T, int     COMMAND_w2Glob1T2F ) 
    {
                          
        assertTrue(COMMAND_w1Loc1F2T ==1 || COMMAND_w1Loc1F2T ==2);
        assertTrue(COMMAND_w1Loc1T2F ==1 || COMMAND_w1Loc1T2F ==2);
        assertTrue(COMMAND_w1Glob1F2T==1 || COMMAND_w1Glob1F2T==2);
        assertTrue(COMMAND_w1Glob1T2F==1 || COMMAND_w1Glob1T2F==2);
        assertTrue(COMMAND_w2Loc1F2T ==1 || COMMAND_w2Loc1F2T ==2);
        assertTrue(COMMAND_w2Loc1T2F ==1 || COMMAND_w2Loc1T2F ==2);
        assertTrue(COMMAND_w2Glob1F2T==1 || COMMAND_w2Glob1F2T==2);
        assertTrue(COMMAND_w2Glob1T2F==1 || COMMAND_w2Glob1T2F==2);
        
        assertTrue(win1LocalAction1F2T.isEnabled()==ENABLED_w1Loc1F2T);
        assertTrue(win1LocalAction1T2F.isEnabled()==ENABLED_w1Loc1T2F);
        assertTrue(win1GlobalAction1F2T.isEnabled()==ENABLED_w1Glob1F2T);
        assertTrue(win1GlobalAction1T2F.isEnabled()==ENABLED_w1Glob1T2F);
        
        assertTrue(win2LocalAction1F2T.isEnabled()==ENABLED_w2Loc1F2T);
        assertTrue(win2LocalAction1T2F.isEnabled()==ENABLED_w2Loc1T2F);
        assertTrue(win2GlobalAction1F2T.isEnabled()==ENABLED_w2Glob1F2T);
        assertTrue(win2GlobalAction1T2F.isEnabled()==ENABLED_w2Glob1T2F);

        assertTrue(win1LocalAction1F2T.getCommand()
                              == ((COMMAND_w1Loc1F2T==1)? command1:command2));
        assertTrue(win1LocalAction1T2F.getCommand()
                              == ((COMMAND_w1Loc1T2F==1)? command1:command2));
        assertTrue(win1GlobalAction1F2T.getCommand()
                              == ((COMMAND_w1Glob1F2T==1)? command1:command2));
        assertTrue(win1GlobalAction1T2F.getCommand()
                              == ((COMMAND_w1Glob1T2F==1)? command1:command2));

        assertTrue(win2LocalAction1F2T.getCommand()
                              == ((COMMAND_w2Loc1F2T==1)? command1:command2));
        assertTrue(win2LocalAction1T2F.getCommand()
                              == ((COMMAND_w2Loc1T2F==1)? command1:command2));
        assertTrue(win2GlobalAction1F2T.getCommand()
                              == ((COMMAND_w2Glob1F2T==1)? command1:command2));
        assertTrue(win2GlobalAction1T2F.getCommand()
                              == ((COMMAND_w2Glob1T2F==1)? command1:command2));
        echoEnabledStatus();
  }
  
  
  /**
   * Test if the state monitoring is working for the enabled state
   */
  public void testEnabled()
  {
        // check initial values bafore any state changs
        System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
        System.err.println("* * * * * * *  I N I T I A L I Z E  * * * * * * *");
        System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");
        initEnables_Commands();
        
        doAsserts(true,false,true,false,
                  true,false,true,false,
                  2,1,2,1,      2,1,2,1 );

        Log.debug(9, "Buttons initialized: (1)TFTF; (2)TFTF; Pause before first event.");

        // Post the first local state change and test if the enabled state changed
        System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
        System.err.println("* * * * * * *  Event: Win 1 State 1 * * * * * * *");
        System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");
        
        monitor.notifyStateChange(eventWin1State1);
        
        doAsserts(false,true,false,true,
                  true,false,false,true,
                  1,2,1,2,      2,1,1,2 );

        Log.debug(9, "Pause between events; buttons now (1)FTFT; (2)TFFT ?");
 

        // Post the second local state change and test if the enabled state changed
        System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
        System.err.println("* * * * * * *  Event: Win 1 State 2 * * * * * * *");
        System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");

        monitor.notifyStateChange(eventWin1State2);
        
        doAsserts(true,false,true,false,
                  true,false,true,false,
                  2,1,2,1,      2,1,2,1 );

        Log.debug(9, "Pause between events; buttons now (1)TFTF; (2)TFTF?");
        
        // check initial values bafore any state changs
        System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
        System.err.println("* * * * * * *  I N I T I A L I Z E  * * * * * * *");
        System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");

        initEnables_Commands();

        doAsserts(true,false,true,false,
                  true,false,true,false,
                  2,1,2,1,      2,1,2,1 );

        Log.debug(9, "Buttons initialized: (1)TFTF; (2)TFTF; Pause before first event.");
        

        // Post the first global state change and test if the enabled state changed
        System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
        System.err.println("* * * * * * *  Event: Win 2 State 1 * * * * * * *");
        System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");

        monitor.notifyStateChange(eventWin2State1);
        
        doAsserts(true,false,false,true,
                  false,true,false,true,
                  2,1,1,2,      1,2,1,2 );

        Log.debug(9, "Pause between events; buttons now (1)TFFT; (2)FTFT?");
        
        // Post the second globals state change and test if the enabled state changed
        System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
        System.err.println("* * * * * * *  Event: Win 2 State 2 * * * * * * *");
        System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");

        monitor.notifyStateChange(eventWin2State2);
  
        doAsserts(true,false,true,false,
                  true,false,true,false,
                  2,1,2,1,      2,1,2,1 );

        Log.debug(9, "Pause after last event; buttons now (1)TFTF; (2)TFTF?");
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
//        monitor.notifyStateChange(eventWin1State1);
//        Command current = action.getCommand();
//        assertTrue(current != null);
//        assertTrue(current == command1);
//
//        // Post the second state change and test if the command changed
//        monitor.notifyStateChange(eventWin1State2);
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
//        monitor.notifyStateChange(eventWin1State1);
//        Command current = ORIG_localAction1F2T.getCommand();
//        assertTrue(current != null);
//        assertTrue(current == command1);
//
//        Log.debug(9, "Pause between events; button now disabled?");
//        // Post the second state change and test if the command changed
//        monitor.notifyStateChange(eventWin1State2);
//        current = ORIG_localAction1F2T.getCommand();
//        assertTrue(current != null);
//        assertTrue(current == command2);
//        Log.debug(9, "Pause after second event. Button enabled again?");
  }

    public static void main(String args[]) {
        Log.getLog().setDebugLevel(51);
        junit.textui.TestRunner.run(GUIActionTest.class);
//        System.exit(0);
    }
    
//    public void testEnabled()
//    {
//          // check initial values bafore any state changs
//          System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
//          System.err.println("* * * * * * *  I N I T I A L I Z E  * * * * * * *");
//          System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");
//          initEnables();
//          echoEnabledStatus();
//          
//          assertTrue( ORIG_localAction1F2T.isEnabled());
//          assertTrue(!ORIG_localAction1T2F.isEnabled());
//          assertTrue( ORIG_globalAction1F2T.isEnabled());
//          assertTrue(!ORIG_globalAction1T2F.isEnabled());
//  
//          // Post the first local state change and test if the enabled state changed
//          System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
//          System.err.println("* * * * * * *  Event: Win 1 State 1 * * * * * * *");
//          System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");
//          monitor.notifyStateChange(eventWin1State1);
//          
//          echoEnabledStatus();
//                  
//          assertTrue(!ORIG_localAction1F2T.isEnabled());
//          assertTrue( ORIG_localAction1T2F.isEnabled());
//          assertTrue(!ORIG_globalAction1F2T.isEnabled());
//          assertTrue( ORIG_globalAction1T2F.isEnabled());
//  
//          // Post the second local state change and test if the enabled state changed
//          System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
//          System.err.println("* * * * * * *  Event: Win 1 State 2 * * * * * * *");
//          System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");
//          monitor.notifyStateChange(eventWin1State2);
//          
//          echoEnabledStatus();
//  
//          assertTrue( ORIG_localAction1F2T.isEnabled());
//          assertTrue(!ORIG_localAction1T2F.isEnabled());
//          assertTrue( ORIG_globalAction1F2T.isEnabled());
//          assertTrue(!ORIG_globalAction1T2F.isEnabled());
//          
//          
//          
//          // check initial values bafore any state changs
//          System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
//          System.err.println("* * * * * * *  I N I T I A L I Z E  * * * * * * *");
//          System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");
//          initEnables();
//          echoEnabledStatus();
//          
//          assertTrue( ORIG_localAction1F2T.isEnabled());
//          assertTrue(!ORIG_localAction1T2F.isEnabled());
//          assertTrue( ORIG_globalAction1F2T.isEnabled());
//          assertTrue(!ORIG_globalAction1T2F.isEnabled());
//  
//          // Post the first global state change and test if the enabled state changed
//          System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
//          System.err.println("* * * * * * *  Event: Win 2 State 1 * * * * * * *");
//          System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");
//          monitor.notifyStateChange(eventWin2State1);
//          
//          echoEnabledStatus();
//          
//          assertTrue( ORIG_localAction1F2T.isEnabled());
//          assertTrue(!ORIG_localAction1T2F.isEnabled());
//          assertTrue(!ORIG_globalAction1F2T.isEnabled());
//          assertTrue( ORIG_globalAction1T2F.isEnabled());
//          
//          // Post the second globals state change and test if the enabled state changed
//          System.err.println("\n* * * * * * * * * * * * * * * * * * * * * * * * *");
//          System.err.println("* * * * * * *  Event: Win 2 State 2 * * * * * * *");
//          System.err.println("* * * * * * * * * * * * * * * * * * * * * * * * *\n");
//          monitor.notifyStateChange(eventWin2State2);
//          
//          echoEnabledStatus();
//          
//          assertTrue( ORIG_localAction1F2T.isEnabled());
//          assertTrue(!ORIG_localAction1T2F.isEnabled());
//          assertTrue( ORIG_globalAction1F2T.isEnabled());
//          assertTrue(!ORIG_globalAction1T2F.isEnabled());
//    }
    
}
