/**
 *  '$RCSfile: PackageWizardShell.java,v $'
 *    Purpose: A class that creates a custom data entry form that is made
 *             by parsing an xml file.  XML is then produced from the form
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-01 01:09:02 $'
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

package edu.ucsb.nceas.morpho.datapackage.wizard;

import edu.ucsb.nceas.morpho.framework.*;
import javax.swing.*;
import javax.swing.border.*; 
import java.io.*;
import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;

public class PackageWizardShell extends javax.swing.JFrame
                                implements ActionListener
{
  private ClientFramework framework;
  private PackageWizard visiblePackageWizard;
  private int frameWizardIndex = 0;
  private int currentFrame = 0;
  private Vector frames;
  private Vector previousFrames = new Vector();
  private Vector frameWizards = new Vector();
  private Hashtable frameObjects;
  private boolean getdataFlag = false;
  private boolean lastFrameFlag = false;
  private boolean getdataVisibleFlag = false;
  private boolean previousFlag = false;
  
  //visual components
  private Container contentPane;
  private JPanel descriptionPanel;
  private JTextArea descriptionText;
  private JPanel wizardFrame;
  private JButton previous;
  private JButton next;
  private JTextField fileTextField = new JTextField();
  private JPanel getFilePanel = new JPanel();
  private JPanel donePanel = new JPanel();
  private JCheckBox openCheckBox;
  private JButton saveToMetacatButton;
  private JPanel activeWizardPanel = new JPanel();
  
  public PackageWizardShell()
  {
    setTitle("Data Package Wizard");
    initComponents();
    pack();
    setSize(600, 500);
  }
  
  public PackageWizardShell(ClientFramework cf)
  {
    framework = cf;
    setTitle("Data Package Wizard");
    initComponents();
    pack();
    setSize(600, 550);
  }
  
  private void initComponents()
  {
    ConfigXML config = framework.getConfiguration();
    Vector saxparserV = config.get("saxparser");
    String saxparser = (String)saxparserV.elementAt(0);
    Vector packageWizardConfigV = config.get("packageWizardConfig");
    String wizardFile = (String)packageWizardConfigV.elementAt(0);
    
    PackageWizardShellParser pwsp = null;
    
    try
    {
      File xmlfile = new File(wizardFile);
      FileReader xml = new FileReader(xmlfile);
      pwsp = new PackageWizardShellParser(xml, saxparser);
      //System.out.println("frames: " + pwsp.getFrames().toString());
      //System.out.println("mainframe: " + pwsp.getMainFrame());
    }
    catch(Exception e)
    {
      framework.debug(1, "error reading or parsing file in " + 
                      "PackageWizardShell.initComponents().");
      e.printStackTrace();
    }
    
    contentPane = getContentPane();
    JPanel mainWizardFrame = createWizardFrame();
    contentPane.add(mainWizardFrame);
    //get the context of the middle frame (not the button frame) so that we
    //can add the first wizard frame to it.
    descriptionPanel = (JPanel)mainWizardFrame.getComponent(0);
    changeDescription("Enter your contact information and " +
                      "basic data package information here.");
    
    wizardFrame = (JPanel)mainWizardFrame.getComponent(1);
    //get the location of the main wizard frame config file, parse it
    //and have it draw itself into the topWizardFrame
    
    frames = pwsp.getFrames();
    System.out.println("frames: " + frames);
    for(int i=0; i<frames.size(); i++)
    {
      Hashtable frame = (Hashtable)frames.elementAt(i);
      JPanel framePanel = new JPanel();
      WizardFrameContainer wfc = new WizardFrameContainer(framePanel);
      wfc.description = (String)frame.get("description");
      System.out.println("description: " + wfc.description);
      if(frame.containsKey("GETDATA"))
      {
        JButton chooseFileButton = new JButton("Browse...");
        fileTextField = new JTextField();
        fileTextField.setColumns(25);
        framePanel.add(fileTextField);
        framePanel.add(chooseFileButton);
        wfc.textfield = fileTextField;
        wfc.type = "GETDATA";
      }
      else
      {
        PackageWizard pw = new PackageWizard(framework, framePanel, (String)frame.get("path"));
        wfc.wizard = pw;
        wfc.type = "WIZARD";
      }
      frameWizards.addElement(wfc);
    }
    
    wizardFrame.add(((WizardFrameContainer)frameWizards.elementAt(frameWizardIndex)).panel);
    frameWizardIndex++;
  }
  
  /**
   * Changes the description in the description panel at the top of the wizard
   */
  private void changeDescription(String desc)
  {
    descriptionText = new JTextArea(desc);
    descriptionText.setPreferredSize(new Dimension(580,40));
    descriptionText.setLineWrap(true);
    descriptionText.setWrapStyleWord(true);
    descriptionText.setEnabled(false);
    descriptionPanel.removeAll();
    descriptionPanel.add(new JScrollPane(descriptionText));
  }
  
  /**
   * returns the current description in the description panel
   */
  private String getDescription()
  {
    return descriptionText.getText();
  }
  
  /**
   * Handles the action when the user presses the 'next' button.
   */
  private void handleNextAction()
  {
    //remove the current panel
    //display the next panel
    wizardFrame.removeAll();
    System.out.println("adding frame");
    
    WizardFrameContainer nextContainer = (WizardFrameContainer)frameWizards.elementAt(frameWizardIndex);
    changeDescription(nextContainer.description);
    wizardFrame.add(nextContainer.panel);
    System.out.println("description: " + nextContainer.description);
    nextContainer.panel.setVisible(true);
    contentPane.doLayout();
    frameWizardIndex++;
  }
  
  /**
   * Handles the action when the user presses the 'previous' button
   */
  private void handlePreviousAction()
  {
    //wizardFrame.removeAll();
    frameWizardIndex--;
    wizardFrame.add(((WizardFrameContainer)frameWizards.elementAt(frameWizardIndex)).panel);
  }
  
  /**
   * handles the actions from the menus if this package wizard is 
   * run in stand alone mode.  
   */
  public void actionPerformed(ActionEvent e) 
  {
    String command = e.getActionCommand();
    String paramString = e.paramString();
    Hashtable contentReps = new Hashtable();
    JFileChooser filechooser = new JFileChooser();
    FileSystemDataStore localDataStore = new FileSystemDataStore(framework);
    
    framework.debug(9, "action fired: |" + command + "|");
    
    if(command.equals("<< Previous"))
    { 
      handlePreviousAction();
    }
    else if(command.equals("Next >>"))
    { 
      handleNextAction();
    }
    else if(command.equals("Choose File"))
    {
      File datafile;
      filechooser.showOpenDialog(this);
      datafile = filechooser.getSelectedFile();
      fileTextField.setText(datafile.getAbsolutePath());
    }
    else if(command.equals("Finish"))
    {
      //-Change the button to 'Done'
      //-save the last frame's data
      //-make the last frame invisible and save it
      //-make a frame that has a button to save the package to Metacat
      
      changeDescription("Click 'Save to Metacat' if you would like to save " +
                        "your new package to a Metacat server.  Check the " +
                        "box if you would like your new package opened in " + 
                        "the package editor.");
      
      
        donePanel = new JPanel();
        openCheckBox = new JCheckBox("Open new package in package " +
                                               "editor?", true);
        saveToMetacatButton = new JButton("Save To Metacat");
        donePanel.setLayout(new BoxLayout(donePanel, BoxLayout.Y_AXIS));
        donePanel.add(Box.createRigidArea(new Dimension(0,150)));
        donePanel.add(Box.createVerticalGlue());
        donePanel.add(saveToMetacatButton);
        donePanel.add(Box.createRigidArea(new Dimension(0,20)));
        donePanel.add(openCheckBox);
        wizardFrame.add(donePanel);
    }
    else if(command.equals("Done"))
    {
      //-open up the package editor with the new package in it
      System.out.println("Done");
      this.setVisible(false);
    }
  }
  
  /**
   * Creates the initial layout and buttons of the wizard
   */
  private JPanel createWizardFrame()
  {
    JPanel mainPanel = new JPanel();
    JPanel wizardPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JPanel descriptionPanel = new JPanel();
    
    BoxLayout box = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
    mainPanel.setLayout(box);
    wizardPanel.setMaximumSize(new Dimension(595, 448));
    wizardPanel.setBorder(BorderFactory.createLineBorder(new Color(255,255,255)));
    buttonPanel.setMaximumSize(new Dimension(595, 48));
    descriptionPanel.setMaximumSize(new Dimension(595, 48));
    
    previous = new JButton("<< Previous");
    previous.setVisible(false);
    next = new JButton("Next >>");
    previous.addActionListener(this);
    next.addActionListener(this);
    BoxLayout buttonLayout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
    buttonPanel.setLayout(buttonLayout);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(previous);
    buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
    buttonPanel.add(next);
    buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
    
    mainPanel.add(descriptionPanel);
    mainPanel.add(wizardPanel);
    mainPanel.add(buttonPanel);
    return mainPanel;
  }
  
  public static void main(String[] args)
  {
    ConfigXML cxml = new ConfigXML("./lib/config.xml");
    ClientFramework cf = new ClientFramework(cxml);
    PackageWizardShell pws = new PackageWizardShell(cf);
    pws.show();
  }
  
  private class WizardFrameContainer
  {
    protected String id = null;
    protected PackageWizard wizard = null;
    protected File file = null;
    protected JPanel panel = null;
    protected String description = null;
    protected String type = null;
    protected JTextField textfield = null;
    private FileSystemDataStore localDataStore = new FileSystemDataStore(framework);
    
    WizardFrameContainer(JPanel panel)
    {
      this.panel = panel;
    }
    
    protected File getFile()
    {
      if(type.equals("WIZARD"))
      {
        id = framework.getNextId();
        String xml = wizard.getXML();
        if(xml == null)
        {
          return null;
        }
        StringReader xmlReader = new StringReader(xml);
        file = localDataStore.saveFile(id, xmlReader, false);
        return this.file;
      }
      else
      {
        id = framework.getNextId();
        file = new File(textfield.getText());
        FileReader fr = null;
        try
        {
          fr = new FileReader(file);
        }
        catch(FileNotFoundException fnfe)
        {
          JOptionPane.showConfirmDialog(panel,
                                 "The file you selected was not found.",
                                 "File Not Found", 
                                 JOptionPane.OK_CANCEL_OPTION,
                                 JOptionPane.WARNING_MESSAGE);
          return null;
        }
        file = localDataStore.saveFile(id, fr, false);
        return this.file;
      }
    }
  }
}
