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
 *     '$Date: 2001-06-01 21:27:16 $'
 * '$Revision: 1.7 $'
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
import edu.ucsb.nceas.morpho.datapackage.*;
import javax.swing.*;
import javax.swing.border.*; 
import java.io.*;
import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xalan.xpath.xml.FormatterToXML;
import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

public class PackageWizardShell extends javax.swing.JFrame
                                implements ActionListener
{
  private ClientFramework framework;
  private int frameWizardIndex = 0;
  private int tempIdCounter = 0;
  private Vector frames;
  private Vector frameWizards = new Vector();
  private Hashtable frameObjects;
  private WizardFrameContainer activeContainer;
  private TripleCollection triples = new TripleCollection();
  private String triplesFile;
  
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
    String description = "Enter your contact information and " +
                          "basic data package information here.";
    changeDescription(description);
    
    wizardFrame = (JPanel)mainWizardFrame.getComponent(1);
    //get the location of the main wizard frame config file, parse it
    //and have it draw itself into the topWizardFrame
    
    frames = pwsp.getFrames();
    triplesFile = pwsp.getMainFrame();
    System.out.println("frames: " + frames);
    for(int i=0; i<frames.size(); i++)
    {
      Hashtable frame = (Hashtable)frames.elementAt(i);
      JPanel framePanel = new JPanel();
      WizardFrameContainer wfc = new WizardFrameContainer(framePanel);
      wfc.description = (String)frame.get("description");
      wfc.attributes = frame;
      if(frame.containsKey("GETDATA"))
      {
        JButton chooseFileButton = new JButton("Browse...");
        chooseFileButton.addActionListener(this);
        fileTextField = new JTextField();
        fileTextField.setColumns(25);
        framePanel.add(fileTextField);
        framePanel.add(chooseFileButton);
        wfc.textfield = fileTextField;
        wfc.type = "GETDATA";
        wfc.description="Enter the path to the data file that you wish to " +
                        "describe. Click the 'Browse' button to browse for " +
                        "the file.";
      }
      else
      {
        PackageWizard pw = new PackageWizard(framework, framePanel, (String)frame.get("path"));
        wfc.wizard = pw;
        wfc.type = "WIZARD";
      }
      frameWizards.addElement(wfc);
    }
    
    WizardFrameContainer nextContainer = (WizardFrameContainer)
                                       frameWizards.elementAt(frameWizardIndex);
    nextContainer.description = description;
    activeContainer = nextContainer;
    wizardFrame.add(nextContainer.panel);
  }
  
  /**
   * Handles the action when the user presses the 'next' button.
   */
  private void handleNextAction()
  { //go forward a frame
    if(frameWizardIndex == frameWizards.size()-1)
    { //we are at the end of the frames...
      
      //-save the last frame's data
      //-display the finish frame
      File f = activeContainer.getFile(true);
      if(f == null)
      {//the user pressed the no button when prompted if he wanted to create
        //an invalid document OR the user did not enter a data file that was
        //valid
        return;
      }
      
      wizardFrame.removeAll();
      frameWizardIndex++;
      changeDescription("Click 'Save to Metacat' if you would like to save " +
                        "your new package to a Metacat server.  Check the " +
                        "box if you would like your new package opened in " + 
                        "the package editor.");
      
      Vector listContent = new Vector();
      for(int i=0; i<frameWizards.size(); i++)
      {
        WizardFrameContainer wfcont = (WizardFrameContainer)
                                                      frameWizards.elementAt(i);
        String id = wfcont.id;
        Hashtable atts = wfcont.attributes;
        String name = (String)atts.get("name");
        if(name == null)
        {
          name = "Data";
        }
        String item = name + " (" + id + ")";
        listContent.add(item);
      }
      
      donePanel = new JPanel();
      openCheckBox = new JCheckBox("Open new package in package " +
                                             "editor?", true);
      saveToMetacatButton = new JButton("Save To Metacat");
      JList idlist = new JList(listContent);
      idlist.setPreferredSize(new Dimension(100,100));
      JLabel listLabel = new JLabel("You are creating the following files: ");
      donePanel.setLayout(new BoxLayout(donePanel, BoxLayout.Y_AXIS));
      donePanel.add(Box.createRigidArea(new Dimension(0,90)));
      donePanel.add(Box.createVerticalGlue());
      donePanel.add(listLabel);
      donePanel.add(new JScrollPane(idlist));
      donePanel.add(Box.createRigidArea(new Dimension(0,20)));
      donePanel.add(saveToMetacatButton);
      donePanel.add(Box.createRigidArea(new Dimension(0,20)));
      donePanel.add(openCheckBox);
      wizardFrame.add(donePanel);
      next.setText("Finish");
    }
    else
    {
      //remove the current panel
      //display the next panel
      File f = activeContainer.getFile(true);
      if(f == null)
      { //the user pressed the no button when prompted if he wanted to create
        //an invalid document OR the user did not enter a data file that was
        //valid
        return;
      }
      
      wizardFrame.removeAll();
      frameWizardIndex++;
      WizardFrameContainer nextContainer = (WizardFrameContainer)
                                         frameWizards.elementAt(frameWizardIndex);
      activeContainer = nextContainer;
      changeDescription(nextContainer.description);
      wizardFrame.add(nextContainer.panel);
      
      if(frameWizardIndex > 0)
      {
        previous.setVisible(true);
      }
    }
    
    show();
  }
  
  /**
   * Handles the action when the user presses the 'previous' button
   */
  private void handlePreviousAction()
  { //go back a frame
    File f = activeContainer.getFile(true);
    if(f == null)
    {//the user pressed the no button when prompted if he wanted to create
      //an invalid document OR the user did not enter a data file that was
      //valid
      return;
    }
    
    wizardFrame.removeAll();
    frameWizardIndex--;
    WizardFrameContainer nextContainer = (WizardFrameContainer)
                                       frameWizards.elementAt(frameWizardIndex);
    activeContainer = nextContainer;
    changeDescription(nextContainer.description);
    wizardFrame.add(nextContainer.panel);
    
    if(frameWizardIndex == 0)
    {
      previous.setVisible(false);
    }
    
    if(frameWizardIndex == frameWizards.size()-2)
    {
      next.setText("Next >>");
    }
    
    show();
  }
  
  /**
   * Handles the action when the user presses the 'previous' button
   */
  private void handleFinishAction()
  { //write out all of the files to their proper homes with proper ids
    //open the new package in the package editor if the check box is true
    
    //add the triples and IDs here /////////////////////////////////////////
    String triplesTag = "/triples";
    for(int i=0; i<frameWizards.size(); i++)
    { //create the triplesCollection
      WizardFrameContainer wfc = (WizardFrameContainer)frameWizards.elementAt(i);
      if(wfc.attributes.containsKey("relatedTo"))
      {
        String relation = (String) wfc.attributes.get("relatedTo");
        String name = (String) wfc.attributes.get("name");
        triples.addTriple(new Triple(name, "isRelatedTo", relation));
      }
      if(wfc.attributes.containsKey("triplesTag"))
      {
        triplesTag = (String)wfc.attributes.get("triplesTag");
      }
    }
    
    System.out.println("triples: " + triples.toString());
    
    
    for(int i=0; i<frameWizards.size(); i++)
    {
      WizardFrameContainer wfc = (WizardFrameContainer)
                                  frameWizards.elementAt(i);
      String name = (String)wfc.attributes.get("name");
      
      File f = wfc.getFile(false);
      if(f == null)
      {
        return;
      }
      
      if(name != null && name.equals(triplesFile))
      { //put the triples in the triples file.
        System.out.println("putting triples in triple file: " + name);
        DOMParser parser = new DOMParser();
        InputSource in;
        FileInputStream fs;
        try
        {
          fs = new FileInputStream(f);
          in = new InputSource(fs);
        }
        catch(FileNotFoundException fnf)
        {
          fnf.printStackTrace();
          return;
        }
        try
        {
          System.out.println("--------------parsing-----------");
          parser.parse(in);
          fs.close();
        }
        catch(Exception e1)
        {
          System.err.println("file: " + f.getPath() + " : parse threw: " + 
                             e1.toString());
          //e1.printStackTrace();
        }
        Document doc = parser.getDocument();
        NodeList tripleNodeList = triples.getNodeList();
        /*try
        {
          NodeList nl = XPathAPI.selectNodeList(doc, triplesTag);
        }
        catch(SAXException se)
        {
          System.err.println("file: " + f.getPath() + " : parse threw: " + 
                             se.toString());
        }*/
        for(int j=0; j<tripleNodeList.getLength(); j++)
        {
          doc.importNode(tripleNodeList.item(j), true);
        }
        
      }
      
      framework.debug(9, "saving file: " + wfc.id);
    }
    
    
    if(openCheckBox.isSelected())
    {
      //open the package in the package editor
      System.out.println("opening the package in the package editor");
    }
    
    this.hide();
  }
  
  /**
   * Handles actions from all components in the Container  
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
    else if(command.equals("Browse..."))
    {
      File datafile;
      filechooser.showOpenDialog(this);
      datafile = filechooser.getSelectedFile();
      fileTextField.setText(datafile.getAbsolutePath());
    }
    else if(command.equals("Finish"))
    {
      handleFinishAction();
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
    protected Hashtable attributes = null;
    private FileSystemDataStore localDataStore = new FileSystemDataStore(framework);
    
    WizardFrameContainer(JPanel panel)
    {
      this.panel = panel;
    }
    
    /**
     * writes out the file.  if temp is true then write it to the temp directory
     * with a temp id.
     */
    protected File getFile(boolean temp)
    {
      if(type.equals("WIZARD"))
      {
        if(temp && id == null)
        {
          id = "tmp." + tempIdCounter++;
        }
        else if(!temp && (id.indexOf("tmp") != -1 || id == null))
        {
          id = framework.getNextId();
        }
      
        String xml = wizard.getXML();
        if(xml == null)
        {
          return null;
        }
        StringReader xmlReader = new StringReader(xml);
        
        if(temp)
        {
          file = localDataStore.saveTempFile(id, xmlReader);
        }
        else
        {
          file = localDataStore.saveFile(id, xmlReader, false);
        }
        return this.file;
      }
      else
      {
        if(temp && id == null)
        {
          id = "tmp." + tempIdCounter++;
        }
        else if(!temp && (id.indexOf("tmp") != -1 || id == null))
        {
          id = framework.getNextId();
        }

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
        
        if(temp)
        {
          file = localDataStore.saveTempFile(id, fr);
        }
        else
        {
          file = localDataStore.saveFile(id, fr, false);
        }
        return this.file;
      }
    }
  }
}
