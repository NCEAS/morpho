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
 *     '$Date: 2001-05-30 22:32:50 $'
 * '$Revision: 1.3 $'
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
  private JPanel descriptionPanel;
  private JTextArea descriptionText;
  private JPanel wizardFrame;
  private Vector frames;
  private int framesIndex = 0;
  private Vector previousFrames = new Vector();
  private boolean getdataFlag = false;
  private Hashtable frameObjects;
  private boolean lastFrameFlag = false;
  private JButton previous;
  private JButton next;
  
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
      System.out.println("frames: " + pwsp.getFrames().toString());
      System.out.println("mainframe: " + pwsp.getMainFrame());
    }
    catch(Exception e)
    {
      framework.debug(1, "error reading or parsing file in " + 
                      "PackageWizardShell.initComponents().");
      e.printStackTrace();
    }
    
    Container contentPane = getContentPane();
    JPanel mainWizardFrame = createWizardFrame();
    contentPane.add(mainWizardFrame);
    //get the context of the top frame (not the button frame) so that we
    //can add the first wizard frame to it.
    descriptionPanel = (JPanel)mainWizardFrame.getComponent(0);
    changeDescription("Enter your contact information and " +
                      "basic data package information here.");
    
    wizardFrame = (JPanel)mainWizardFrame.getComponent(1);
    //get the location of the main wizard frame config file, parse it
    //and have it draw itself into the topWizardFrame
    frames = pwsp.getFrames();
    frameObjects = pwsp.getFrameObjects();
    Hashtable firstFrameHash = getNextFrame();
    String path = (String)firstFrameHash.get("path");
    PackageWizard pw = new PackageWizard(framework, wizardFrame, path);
    visiblePackageWizard = pw;
  }
  
  /**
   * returns the hashtable representing the next frame to be displayed
   */
  private Hashtable getNextFrame()
  {
    Hashtable frameHash = (Hashtable)frames.elementAt(framesIndex);
    
    framesIndex++;
    
    if(framesIndex >= 2)
    {
      previous.setVisible(true);
    }
    
    if(framesIndex == frames.size())
    {
      lastFrameFlag = true;
    }
    
    
    
    return frameHash;
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
   * handles the actions from the menus if this package wizard is 
   * run in stand alone mode.  
   */
  public void actionPerformed(ActionEvent e) 
  {
    /*
      1) Display mainframe
      2) Display GETDATA form
      3) Display next frame in list
    */
    String command = e.getActionCommand();
    String paramString = e.paramString();
    Hashtable contentReps = new Hashtable();
    JFileChooser filechooser = new JFileChooser();
    FileSystemDataStore localDataStore = new FileSystemDataStore(framework);
    
    framework.debug(9, "action fired: |" + command + "|");
    
    if(command.equals("<< Previous"))
    { //go back a frame
      System.out.println("go back a frame");
    }
    else if(command.equals("Next >>"))
    { 
      //get the data from the current frame and display the next one
      
      //save the current frame
      //in case the user presses the previous button.
      WizardFrame prevFrame = new WizardFrame(visiblePackageWizard); 
      if(!getdataFlag)
      {
        String xml = visiblePackageWizard.getXML();
        if(xml == null)
        { //the user pressed the 'no' button on the 'are you sure you want
          //to create an invalid document' dialog
          return;
        }
        
        StringReader xmlReader = new StringReader(xml);
        String id = framework.getNextId();
        prevFrame.id = id;
        prevFrame.file = localDataStore.saveFile(id, xmlReader, false);
        previousFrames.addElement(prevFrame);
      }
      
      visiblePackageWizard.setVisible(false); //make current wizard invisible
      
      Hashtable nextFrame = getNextFrame();
      
      if(lastFrameFlag)
      { //we are on the last frame, change the next button to 'finish' and
        //prepare to write out the triples in the package
        System.out.println("LAST FRAME");
        next.setText("Finish");
      }
      
      System.out.println("nextFrame: " + nextFrame.toString());
      if(nextFrame.containsKey("GETDATA"))
      { //display the data acquisition frame
        
        //JPanel nextPanel = createDataAcquisitionPanel();
        ConfigXML config = framework.getConfiguration();
        String datadir = config.get("local_xml_directory", 0);
        filechooser = new JFileChooser(datadir);
        changeDescription("Click the \"Choose File\" button " +
                          "to choose the data file that you want " +
                          "your metadata to describe.");
        
        JButton showOpenDialog = new JButton("Choose File");
        showOpenDialog.setPreferredSize(new Dimension(200, 100));
        //add an icon to the button here!
        showOpenDialog.addActionListener(this);
        wizardFrame.add(showOpenDialog);
        getdataFlag = true;
      }
      else
      { //show next frame in the frames vector
        if(getdataFlag)
        {//hide the getdata screen
          wizardFrame.removeAll();
          getdataFlag = false;
        }
        else
        {
          visiblePackageWizard.setVisible(false);
        }
        
        String path = (String)nextFrame.get("path");
        String name = (String)nextFrame.get("name");
        PackageWizard pw = new PackageWizard(framework, wizardFrame, path);
        visiblePackageWizard = pw;
        String description = (String)nextFrame.get("description");
        System.out.println("description: " + description);
        changeDescription(description);
      }
    }
    else if(command.equals("Choose File"))
    {
      filechooser.showOpenDialog(this);
      //get the file that the user chose, assign it an id and cache it
      File datafile = filechooser.getSelectedFile();
      File f = null;
      FileReader fr = null;
      try
      {
        fr = new FileReader(datafile);
      }
      catch(FileNotFoundException fnfe)
      {
        JOptionPane.showConfirmDialog(this,
                               "The file you selected was not found.",
                               "File Not Found", 
                               JOptionPane.OK_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
        return;
      }
      
      String id = framework.getNextId();
      framework.debug(9, "data file id generated: " + id);
      f = localDataStore.saveFile(id, fr, false);
      WizardFrame wf = new WizardFrame(wizardFrame);
      wf.file = f;
      wf.id = id;
      previousFrames.addElement(wf);
    }
    else if(command.equals("Finish"))
    {
      System.out.println("we're finishing");
      //work on finishing up the package now.--------------------!!!!!!!
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
    //buttonPanel.setBorder(BorderFactory.createLineBorder(new Color(255,255,255)));
    
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
  
  private class WizardFrame
  {
    protected String id = null;
    protected PackageWizard wizard = null;
    protected File file = null;
    protected JPanel panel = null;
    WizardFrame(PackageWizard pw)
    {
      wizard = pw;
    }
    
    WizardFrame(JPanel panel)
    {
      this.panel = panel;
    }
  }
}
