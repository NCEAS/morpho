package edu.ucsb.nceas.editor;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

import edu.ucsb.nceas.dtclient.PluginInterface;

public class EditorBean extends Container implements PluginInterface
{
  Editor me;
  Action[] menuActions = null;
  Action[] toolbarActions = null;

  public EditorBean()
  {
    //{{INIT_CONTROLS
    setLayout(new BorderLayout(0,0));
    //}}
    me = new Editor();
    me.setVisible(true);
    me.invalidate();
    add(BorderLayout.CENTER,me);
    setVisible(true);
   
    initializeActions();
  }

  /**
   * This method is called on component initialization to generate a list
   * of the names of the menus, in display order, that the component wants
   * added to the framework.  If a menu already exists (from another component
   * or the framework itself), the order will be determined by the earlier
   * registration of the menu.
   */
  public String[] registerMenus() {
    String menuList[] = new String[2];
    menuList[0] = "File";
    menuList[1] = "Test";
    return menuList;
  }

  /**
   * The plugin must return the Actions that should be associated 
   * with a particular menu. They will be appended onto the bottom of the menu
   * in most cases.
   */
  public Action[] registerMenuActions(String menu) {
    Action actionList[] = null;
    if (menu.equals("Test") || menu.equals("File")) {
      actionList = menuActions;
    }
    return actionList;
  }

  /**
   * The plugin must return the list of Actions to be associated with the
   * toolbar for the framework. 
   */ 
  public Action[] registerToolbarActions() {
    return toolbarActions;;
  }

  /**
   * Set up the actions for menus and toolbars
   */
  private void initializeActions() {
    // Set up the menus for the application
    menuActions = new Action[1];
    Action testItemAction = new AbstractAction("Test Me") {
      public void actionPerformed(ActionEvent e) {
        System.out.println("Action fired: Test Me :-)");
      }
    };
    menuActions[0] = testItemAction;

    // Set up the toolbar for the application
    toolbarActions = new Action[1];
    Action testToolbarAction = new AbstractAction("Wow") {
      public void actionPerformed(ActionEvent e) {
        System.out.println("Action fired: Wow :-)");
      }
    };
    toolbarActions[0] = testToolbarAction;
  }

  public static void main(String argv[])
  {
    class DriverFrame extends javax.swing.JFrame implements ActionListener
    {
      EditorBean editorBean;
      public DriverFrame()
      {
        addWindowListener(
          new java.awt.event.WindowAdapter()
          {
            public void windowClosing(java.awt.event.WindowEvent event)
            {
              dispose();    // free the system resources
              System.exit(0); // close the application
            }
          }
        );
        getContentPane().setLayout(new BorderLayout(0,0));
        setSize(700,450);
        editorBean = new EditorBean();
        getContentPane().add(BorderLayout.CENTER,editorBean);

        // Create a menu bar and add it to the top edge of the frame
        JMenuBar menuBar;
        menuBar = new JMenuBar();

        // Need to dynamically create the menus here based on the Actions
        // NOT YET IMPLEMENTED -- COPY CODE FROM ClientFramework

        // Add the menubar to the window
        getContentPane().add(BorderLayout.NORTH,menuBar);
      }
      
      public void actionPerformed (ActionEvent event) {
        if(event.getActionCommand().equals("Quit")) {
          System.exit(0);
        }
      }
    }
    new DriverFrame().show();
  }
}
