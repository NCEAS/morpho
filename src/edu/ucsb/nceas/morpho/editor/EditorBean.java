package edu.ucsb.nceas.editor;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

import edu.ucsb.nceas.metaedit.*;

public class EditorBean extends AbstractMdeBean
{
  Editor me;
  public EditorBean()
  {
    //{{INIT_CONTROLS
    setLayout(new BorderLayout(0,0));
    //setSize(750,450);
    //}}
    me = new Editor();
    me.setVisible(true);
    me.invalidate();
    add(BorderLayout.CENTER,me);
    setVisible(true);
  }

/*
  public void openDocument() {
    me.openDocument();   
  }
  public void openDocument(File f) {
    me.openDocument(f);   
  }
  public void newDocument() {
    me.newDocument();   
  }
  public void saveDocument() {
    me.currentDocument.save();  
  }
  public void saveDocumentAs() {
    me.currentDocument.saveAs();   
  }
  public void saveDocumentToDatabase() {
    me.currentDocument.saveToDatabase();   
  }
  public void previewXMLFile() {
    me.previewXMLFile();   
  }
  public void showOptions() {
    me.showOptions(); 
  }
  public void set_eChoiceVisible(boolean flg) {
    me.eChoice.setVisible(flg); 
  }
  public void set_inputer(boolean flg) {
    me.inputer.setVisible(flg); 
  }
*/   

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
        //Container cp = getContentPane();
        //getRootPane().setMenuBar(menuBar);

        // Add the menus to the menubar
        JMenu menu;
        JMenuItem item;
        JCheckBoxMenuItem checkBoxMenuItem;

        // FILE menu
        menu = new JMenu("File");
        // ADD ACCELERATOR KEYS FOR EACH OF THE MENUS
        item = new JMenuItem("New...");
        item.addActionListener(this);
        menu.add(item);
    
        item = new JMenuItem("Open...");
        item.addActionListener(this);
        menu.add(item);
    
        item = new JMenuItem("Save...");
        menu.add(item);
        item.addActionListener(this);
    
        item = new JMenuItem("Save As...");
        menu.add(item);
        item.addActionListener(this);
    
        item = new JMenuItem("Save to Database...");
        menu.add(item);
        item.addActionListener(this);
    
        item = new JMenuItem("Preview XML...");
        menu.add(item);
        item.addActionListener(this);
    
        menu.add(new JSeparator());
        item = new JMenuItem("Quit");
        menu.add(item);
        item.addActionListener(this);
        menuBar.add(menu);

        // EDIT menu
        menu = new JMenu("Edit");
        // ADD CUT/COPY/PASTE PLACEHOLDERS, DISABLED
        menu.add(new JSeparator());
        item = new JMenuItem("Find");
        item.setEnabled(false);
        menu.add(item);
        item.addActionListener(this);
        menu.add(new JSeparator());
        item = new JMenuItem("Options...");
        menu.add(item);
        item.addActionListener(this);
        menuBar.add(menu);
    
        // WINDOW menu
        menu = new JMenu("Window");
        checkBoxMenuItem = new JCheckBoxMenuItem("Element Choice");
        checkBoxMenuItem.setState(true);
        menu.add(checkBoxMenuItem);
        checkBoxMenuItem.addActionListener(this);
        menuBar.add(menu);
        checkBoxMenuItem = new JCheckBoxMenuItem("Element Text");
        checkBoxMenuItem.setState(true);
        menu.add(checkBoxMenuItem);
        checkBoxMenuItem.addActionListener(this);
        menuBar.add(menu);
    
        // HELP menu
        menu = new JMenu("Help");
        item = new JMenuItem("About...");
        item.setEnabled(false);
        menu.add(item);
        item.addActionListener(this);
        menuBar.add(menu);
        getContentPane().add(BorderLayout.NORTH,menuBar);
      }
      
      public void actionPerformed (ActionEvent event) {
        if(event.getActionCommand().equals("Quit")) {
          System.exit(0);
        }  else if (event.getActionCommand().equals("Open...")) {
          editorBean.openDocument();
        } else if (event.getActionCommand().equals("New...")) {
          editorBean.newDocument();
        } else if (event.getActionCommand().equals("Save...")) {
          editorBean.saveDocument();
        } else if (event.getActionCommand().equals("Save As...")) {
          editorBean.saveDocumentAs();
        } else if (event.getActionCommand().equals("Save to Database...")) {
          System.err.println("Saving to database...");
          editorBean.saveDocumentToDatabase();
        } else if (event.getActionCommand().equals("Preview XML...")) {
          editorBean.previewXMLFile();
        } else if (event.getActionCommand().equals("Options...")) {
          editorBean.showOptions();
        } else if (event.getActionCommand().equals("About...")) {
          // Function yet to be implemented.
          // doAboutMenu();
        } else if (event.getActionCommand().equals("Element Choice")) {
          if (((JCheckBoxMenuItem)(event.getSource())).getSelectedObjects() == null) {
            editorBean.set_eChoiceVisible(false);
          } else {
            editorBean.set_eChoiceVisible(true);
          }
        } else if(event.getActionCommand().equals("Element Text")) {
          if (((JCheckBoxMenuItem)(event.getSource())).getSelectedObjects() == null) {
            editorBean.set_inputer(false);
          } else {
            editorBean.set_inputer(true);
          }
        }
      }
    }
    new DriverFrame().show();
  }
}
