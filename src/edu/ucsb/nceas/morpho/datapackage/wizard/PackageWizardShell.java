/**
 *  '$RCSfile: PackageWizardShell.java,v $'
 *    Purpose: A class that creates a custom data entry form that is made
 *             by parsing an xml file.  XML is then produced from the form
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-12-26 20:00:39 $'
 * '$Revision: 1.83 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.TextImportListener;
import edu.ucsb.nceas.morpho.framework.TextImportWizard;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.datapackage.*;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;

import javax.swing.*;
import javax.swing.border.*; 
import java.io.*;
import java.util.*;
import java.lang.*;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import com.arbortext.catalog.*;

/**
 * This is the shell of the package wizard.  it handles the 
 * non-information-gathering panels in the wizard dialog.  the instructions
 * pane, the buttons and the title bar.  it also serves as a container for 
 * the package wizard panel.  the shell's config file defines the order
 * in which the wizard frames are "played" and also the descriptions
 * that are put in the left most pane.
 */
public class PackageWizardShell extends javax.swing.JFrame
                                implements ActionListener, TextImportListener
{
  private Morpho morpho;
  private int frameWizardIndex = 0;
  private int tempIdCounter = 0;
  private Vector frames;
  private Vector frameWizards = new Vector();
  private Hashtable frameObjects;
  private WizardFrameContainer activeContainer;
  private TripleCollection triples = new TripleCollection();
  private String triplesFile;
  
  private String aclID = "";
  //visual components
  private Container contentPane;
  private JPanel descriptionPanel;
  private JPanel headPanel;
  private JTextArea descriptionText;
  private JLabel descriptionLabel;
  private JPanel wizardFrame;
  private JButton previous;
  private JButton next;
  private JTextField fileTextField = new JTextField();
  private JCheckBox includeDataFileCheckBox;
  private JPanel getFilePanel = new JPanel();
  private JPanel donePanel = new JPanel();
  private JCheckBox openCheckBox;
  private JCheckBox saveToMetacatCheckBox;
  private JCheckBox publicAccessCheckBox;
  private JButton saveToMetacatButton;
  private JButton cancelButton = new JButton();
  private JRadioButton simpleDataRButton;
  private JRadioButton importDataRButton;
  
  private String getDataDescription;
  private String finishDescription;
  private Hashtable descriptions = new Hashtable();
  
  private ConfigXML config;
  
  /** for use when we are adding data to an existing package 
   *  set to a number greater than 0 to indicate adding data  
  */
  private int startingFrame = 0;
  /** instance of AddMetadataWizard to return data to when adding data to existing package */
  private AddMetadataWizard addMetaWiz;
  
  public PackageWizardShell()
  {
    setTitle("Data Package Wizard");
    initComponents();
    pack();
    setSize(660, 550);
  }
  
  public PackageWizardShell(Morpho morpho)
  {
    this.morpho = morpho;
    setTitle("Data Package Wizard");
    initComponents();
    pack();
    setSize(660, 550);
  }
  
  public PackageWizardShell(int startingFrame, AddMetadataWizard amdw) {
    this();
    this.startingFrame = startingFrame;
    this.addMetaWiz = amdw;
    frameWizardIndex = startingFrame;
  }

  public PackageWizardShell(Morpho morpho, int startingFrame, AddMetadataWizard amdw) {
    this.morpho = morpho;
    setTitle("Data Package Wizard");
    this.startingFrame = startingFrame;
    frameWizardIndex = startingFrame;
    initComponents();
    pack();
    setSize(660, 550);
    this.addMetaWiz = amdw;
    this.show();
  }
  
  private void initComponents()
  {
    config = morpho.getConfiguration();
    Vector packageWizardConfigV = config.get("packageWizardConfig");
    String wizardFile = (String)packageWizardConfigV.elementAt(0);
    
    //the parser to parse the config file for the shell
    PackageWizardShellParser pwsp = null;
    
    try
    {
      //File xmlfile = new File(wizardFile);
      //FileReader xml = new FileReader(xmlfile);
      ClassLoader cl = this.getClass().getClassLoader();
      InputStream is = cl.getResourceAsStream(wizardFile);
      if (is == null) {
          Log.debug(10, "Null input stream returned for resource.");
      }
      BufferedReader xml = new BufferedReader(new InputStreamReader(is));
      pwsp = new PackageWizardShellParser(xml);
    }
    catch(Exception e)
    {
      Log.debug(1, "error reading or parsing file in " + 
                      "PackageWizardShell.initComponents().");
      e.printStackTrace();
    }
    descriptions = pwsp.getDescriptions();
    
    contentPane = getContentPane();
    JPanel mainWizardFrame = createWizardFrame();
    contentPane.add(mainWizardFrame);
    String description = (String)descriptions.get(pwsp.getMainFrame());
    
    changeDescription(description);
    getDataDescription = (String)descriptions.get("getData");
    finishDescription = (String)descriptions.get("Finish");
    
    frames = pwsp.getFrames();
    if(descriptions.containsKey("InitialDescription"))
    {
      Hashtable temphash = new Hashtable();
      temphash.put("InitialDescription", "");
      frames.insertElementAt(temphash, 0);
    }
    triplesFile = pwsp.getMainFrame();
    for(int i=0; i<frames.size(); i++)
    {
      //all of the wizard frames are pre built here and put into a vector
      //for retrieval later.
      Hashtable frame = (Hashtable)frames.elementAt(i);
      JPanel framePanel = new JPanel();
      WizardFrameContainer wfc = new WizardFrameContainer(framePanel);

      if(frame.containsKey("name"))
      {
        wfc.description = (String)descriptions.get((String)frame.get("name"));
      }
      
      wfc.attributes = frame;
      if(frame.containsKey("GETDATA"))
      { //this field is built when the wizard need to get a data file
        JLabel toplabel = new JLabel("<html>Choose a data file to " +
                                     "include " +
                                     "in your package.<br>  If you do not " +
                                     "wish " +
                                     "to include a data file, leave the box " +
                                     "empty and<br> click the 'next' button." +
                                     "</html>");
        toplabel.setPreferredSize(new Dimension(20,100));                             
        framePanel.setLayout(new BoxLayout(framePanel,BoxLayout.Y_AXIS));
        
        JPanel container1 = new JPanel();
        JButton chooseFileButton = new JButton("Browse...");
        chooseFileButton.addActionListener(this);
        fileTextField = new JTextField();
        fileTextField.setColumns(25);
        
        
        ButtonGroup bg = new ButtonGroup();
        simpleDataRButton = new JRadioButton("Manually enter data file " +
                                             "descriptions.");
        importDataRButton = new JRadioButton("Automatically generate " +
                                             "data file descriptions " +
                                             "(ASCII files only)");
        importDataRButton.setSelected(true);
        bg.add(importDataRButton);
        bg.add(simpleDataRButton);
        
        String includeIt = "Include data file in saved package?";
        includeDataFileCheckBox = new JCheckBox(includeIt, true);
        String toolTip = "If this box is unchecked, the data will be used to create "
                       + "metadata, but not be included in the final datapackage";
        includeDataFileCheckBox.setToolTipText(toolTip);
        
        framePanel.add(toplabel);
        container1.add(fileTextField);
        container1.add(chooseFileButton);
        
        framePanel.add(container1);
        framePanel.add(Box.createRigidArea(new Dimension(0, 60)));


        //framePanel.add(parseTextButton);
        //framePanel.add(parseLabel);
        framePanel.add(simpleDataRButton);
        framePanel.add(importDataRButton);
        framePanel.add(Box.createRigidArea(new Dimension(0, 60)));
        framePanel.add(includeDataFileCheckBox);
        
        //framePanel.setPreferredSize(new Dimension(450,300));
        framePanel.setAlignmentY(Component.LEFT_ALIGNMENT);
        
        wfc.textfield = fileTextField;
        wfc.type = "GETDATA";
        wfc.description = getDataDescription;
      }
      else if(frame.containsKey("InitialDescription"))
      { //this is the frame that is first displayed with the initial instructions
        String initdesc = (String)descriptions.get("InitialDescription");
        JLabel initdescLabel = new JLabel("<html><font color=000000>" + 
                                           initdesc + "</font></html>");
        initdescLabel.setForeground(Color.black); 
        initdescLabel.setFont(new Font("Dialog", Font.BOLD, 12));
        initdescLabel.setPreferredSize(new Dimension(400, 400));
        framePanel.add(initdescLabel);
        wfc.type="IGNORE";
        wfc.description = "Instructions";
        wfc.attributes.put("name", "InitialDescription");
        
        // get a new Accession number here so that that ACL accession number is smallest of package
        // since it must be inserted into Metacat before other package members
        AccessionNumber aa = new AccessionNumber(morpho);
        aclID = aa.getNextId();
        
      }
      else
      { //this builds a packagewizard frame
        PackageWizard pw = new PackageWizard(framePanel, 
                                             (String)frame.get("path"));
        wfc.wizard = pw;
        wfc.type = "WIZARD";
      }
      //frameWizards holds all of these frames for later retrieval
      frameWizards.addElement(wfc);
    }
    
    WizardFrameContainer nextContainer = (WizardFrameContainer)
                                       frameWizards.elementAt(frameWizardIndex);
    //nextContainer.description = description;
    changeDescription(nextContainer.description);
    activeContainer = nextContainer;
    wizardFrame.add(nextContainer.panel);
  }
  
  /**
   * Handles the action when the user presses the 'next' button.
   */
  private void handleNextAction()
  { //go forward a frame
    if(activeContainer.attributes.containsKey("repeatable"))
    { //certain frames can be repeated.  this handles adding them dynamically
      String repeatable = (String)activeContainer.attributes.get("repeatable");
      if(repeatable.equals("yes"))
      { //ask the user if he wished to add another file of the type which
        //he just created
        
        File f = activeContainer.getFile(true);
        //write out the file from the current Active container
        if(f == null)
        { //the user pressed the no button when prompted if he wanted to create
          //an invalid document OR the user did not enter a data file that was
          //valid
          return;
        }
        
        int choice = JOptionPane.YES_OPTION;
        choice = JOptionPane.showConfirmDialog(null, 
                               "Do you wish to add more " +
                               (String)activeContainer.attributes.get("name") +
                               " metadata?", 
                               "Package Wizard", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
        if(choice == JOptionPane.YES_OPTION)
        { //add the new frame to the wizard
          
          JPanel framePanel = new JPanel();
          WizardFrameContainer wfc = new WizardFrameContainer(framePanel);
          wfc.description = new String(activeContainer.description);
//DFH          wfc.attributes = new Hashtable(activeContainer.attributes);
          wfc.attributes = (Hashtable)(activeContainer.attributes).clone();
          activeContainer.attributes.remove("repeatable");
          PackageWizard pw = new PackageWizard(framePanel, 
                                            (String)wfc.attributes.get("path"));
          wfc.wizard = pw;
          wfc.type = new String(activeContainer.type);
          
          wizardFrame.removeAll();
          frameWizardIndex++;
          frameWizards.insertElementAt(wfc, frameWizardIndex);
          wizardFrame.add(wfc.panel);
          changeDescription(wfc.description);
          //show();
          wizardFrame.validate();
          wizardFrame.repaint();
          activeContainer = wfc;
          return;
        }
        else if(choice == JOptionPane.CANCEL_OPTION)
        { //stay where we are
          return;
        }
      }
    }
    
    if(frameWizardIndex == frameWizards.size()-1)
    { //we are at the end of the frames so we need to build the finish
      //frame and prepare to exit the wizard
      //-save the last frame's data
      //-display the finish frame
      File f = activeContainer.getFile(true);
      if(f == null)
      {//the user pressed the no button when prompted if he wanted to create
        //an invalid document OR the user did not enter a data file that was
        //valid
        return;
      }
    
      if (startingFrame>0) 
      {
        Vector filesVec = handleFinishAddData();
        this.hide();
        this.dispose();
        addMetaWiz.addingNewDataWizardCompleted(filesVec);
        return;
      }
      
      wizardFrame.removeAll();
      wizardFrame.invalidate();
      frameWizardIndex++;
      changeDescription(finishDescription);
      Vector listContent = new Vector();
      for(int i=0; i<frameWizards.size(); i++)
      { //build the summary list box
        WizardFrameContainer wfcont = (WizardFrameContainer)
                                                      frameWizards.elementAt(i);
        String id = wfcont.id;
        Hashtable atts = wfcont.attributes;
        String name = (String)atts.get("name");
        String item = name;
        
        if(name == null)
        {
          name = "Data File";
          item = wfcont.originalDataFilePath;
        }
        else if(!name.equals("InitialDescription") && !name.equals("IGNORE"))
        {
          if(wfcont.attributes.containsKey("displayNamePath"))
          {
            String namePath = (String)wfcont.attributes.get("displayNamePath");
            NodeList namelist = PackageUtil.getPathContent(wfcont.file, 
                                                           namePath, morpho);
            for(int j=0; j<namelist.getLength(); j++)
            {
              Node itemnode = namelist.item(j);
              item = (String)itemnode.getFirstChild().getNodeValue().trim();
              
              if(item != null && !item.equals("InitialDescription"))
              {
                listContent.addElement(item);
              }
            }
          }
        }
      }
      
      donePanel = new JPanel();
      if (Morpho.isConnected()) {
        saveToMetacatCheckBox = new JCheckBox("Save package to Network?", true);
      }
      else {
        saveToMetacatCheckBox = new JCheckBox("Save package to Network?", false);
      }
      publicAccessCheckBox = new JCheckBox("Package should be publicly " +
                                           "readable (on Network)?", true);
      //saveToMetacatButton = new JButton("Save To Network");
      JList idlist = new JList(listContent);
      idlist.setPreferredSize(new Dimension(100,1000));
      JLabel listLabel = new JLabel("You are creating the following package " +
                                    "members: ");
      donePanel.setLayout(new BoxLayout(donePanel, BoxLayout.Y_AXIS));
      donePanel.add(Box.createRigidArea(new Dimension(0,90)));
      donePanel.add(Box.createVerticalGlue());
      donePanel.add(listLabel);
      donePanel.add(new JScrollPane(idlist));
      donePanel.add(Box.createRigidArea(new Dimension(0,20)));
      donePanel.add(saveToMetacatCheckBox);
      donePanel.add(Box.createRigidArea(new Dimension(0,20)));
      donePanel.add(publicAccessCheckBox);
      //donePanel.add(Box.createRigidArea(new Dimension(0,20)));
      //donePanel.add(openCheckBox);
      wizardFrame.add(donePanel);
      wizardFrame.validate();
      next.setText("Finish");
      next.setIcon(null);
    }
    else
    { //this is the default action of the next button...to display the next panel
      //remove the current panel
      //display the next panel
      File f = activeContainer.getFile(true);
      if(f == null)
      { //the user pressed the no button when prompted if he wanted to create
        //an invalid document OR the user did not enter a data file that was
        //valid
        return;
      }
      
      String prevFrameType = ((WizardFrameContainer)
                             frameWizards.elementAt(frameWizardIndex)).type;
                             
      if(prevFrameType.equals("GETDATA") && importDataRButton.isSelected()
                       && ((fileTextField.getText().trim()).length()>0)
      )
      { //this is what happens when the user wants to use the text import 
        //wizard
        int entitynum = frameWizardIndex + 1;
        int attributenum = frameWizardIndex + 2;
        int physicalnum = frameWizardIndex + 3;
        WizardFrameContainer wfc1 = (WizardFrameContainer)frameWizards.elementAt(entitynum);
        WizardFrameContainer wfc2 = (WizardFrameContainer)frameWizards.elementAt(attributenum);
        WizardFrameContainer wfc3 = (WizardFrameContainer)frameWizards.elementAt(physicalnum);
        PackageWizard pw1 = wfc1.wizard;
        PackageWizard pw2 = wfc2.wizard;
        PackageWizard pw3 = wfc3.wizard;
        
        String fileTextName = null;
        if (!fileTextField.getText().equals("")) fileTextName = fileTextField.getText(); 
        if (!includeDataFileCheckBox.isSelected()) fileTextField.setText("");
        
        TextImportWizard tiw = new TextImportWizard(fileTextName, this);
        //the TextImport wizard has reference to PackageWizards so it can save the
        // XML text it generates
        tiw.setEntityWizard(pw1);
        tiw.setAttributeWizard(pw2);
        tiw.setPhysicalWizard(pw3);
        tiw.setVisible(true);
        this.setVisible(false);
        return;
      }
      
      wizardFrame.removeAll();
      wizardFrame.invalidate();
      frameWizardIndex++;
      WizardFrameContainer nextContainer = (WizardFrameContainer)
                                         frameWizards.elementAt(frameWizardIndex);
                                                                                  
      activeContainer = nextContainer;
      changeDescription(nextContainer.description);
      
      String test = null;
      
      if (frameWizardIndex>1) 
      {
        PackageWizard nextPW = nextContainer.wizard;
        if (nextPW!=null) 
        {
          test = nextPW.getXMLString();   
        }
      }
      if (test==null) 
      {
        wizardFrame.add(nextContainer.panel);
      }
      else 
      {
        // if an XML string in the PackageWizard for this frame is not null, then
        // it has been set by anothe class (probably the TextImportWizard)
        // Then just show that information has already been set.
        JPanel continuePanel = new JPanel();
        continuePanel.setLayout(new BoxLayout(continuePanel,BoxLayout.Y_AXIS));
        String messageStr = "This information has been automatically created from a data table.";
        String messageStr1 = "Please press the 'Next' button to continue.";
        JLabel message = new JLabel(messageStr);
        JLabel message1 = new JLabel(messageStr1);
        continuePanel.add(Box.createRigidArea(new Dimension(0,90)));
        continuePanel.add(message);
        continuePanel.add(Box.createRigidArea(new Dimension(0,90)));
        continuePanel.add(message1);
        wizardFrame.add(continuePanel);
      }
      
      
      if(frameWizardIndex > 0)
      {
        previous.setEnabled(true);
      }
    }
    
    wizardFrame.validate();
    wizardFrame.repaint();
  }
  
  /**
   * Handles the action when the user presses the 'previous' button
   */
  private void handlePreviousAction()
  { //go back a frame
 //DFH   File f = activeContainer.getFile(true);
 //DFH   if(f == null)
 //DFH   {//the user pressed the no button when prompted if he wanted to create
 //DFH     //an invalid document OR the user did not enter a data file that was
 //DFH     //valid
 //DFH     return;
 //DFH  }
    
    wizardFrame.removeAll();
    wizardFrame.invalidate();
    frameWizardIndex--;
    WizardFrameContainer nextContainer = (WizardFrameContainer)
                                       frameWizards.elementAt(frameWizardIndex);
    activeContainer = nextContainer;
    changeDescription(nextContainer.description);
    wizardFrame.add(nextContainer.panel);
    
    if(frameWizardIndex == 0)
    { //we are at the beginning, we don't want people going past the beginning
      //because bad stuff happens
      previous.setEnabled(false);
    }
    
    if(frameWizardIndex == frameWizards.size()-1)
    {
      next.setText("Next");
      next.setIcon(new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/navigation/Forward16.gif")));
      next.setHorizontalTextPosition(SwingConstants.LEFT);
    }
    wizardFrame.validate();
    wizardFrame.repaint();
    //show();
  }
  
  /**
   * Handles the action when the user presses the 'finish' button
   */
  private void handleFinishAction()
  { 
    if (addMetaWiz != null)
    {
      addMetaWiz.dispose();
    }//if
    //write out all of the files to their proper homes with proper (non temp) ids
    //add the triples to the triples file
    //open the new package in the package editor if the check box is true
    String aclid = "";
    Vector packageFiles = new Vector();
    //String triplesTag = "//triple";
    String triplesTag = config.get("triplesTag", 0);
    for(int i=0; i<frameWizards.size(); i++)
    { //find the triplesTag
      WizardFrameContainer wfc = (WizardFrameContainer)frameWizards.elementAt(i);
      
      if(wfc.attributes.containsKey("triplestag"))
      {
        triplesTag = (String)wfc.attributes.get("triplestag");
      }
    }
    
    for(int i=0; i<frameWizards.size(); i++)
    { //save all of the files to their new home
      WizardFrameContainer wfc = (WizardFrameContainer)
                                  frameWizards.elementAt(i);
      
      File f = wfc.getFile(false);
      if(f == null)
      {
        return;
      }
      
      if(f.getName().equals("FAKE"))
      {
        continue;
      }
    
      Vector fileVec = new Vector();
      fileVec.addElement(wfc.id);
      fileVec.addElement(f);
      fileVec.addElement(wfc.type);
      packageFiles.addElement(fileVec);
      
      String name = (String)wfc.attributes.get("name");
      String id = (String)wfc.id;
    }
    
    // Now create an AccessControl XML document for the dataset
    File aclFile = getACLFile(aclID);
    
    Vector fvec = new Vector();
    fvec.addElement(aclID);
    fvec.addElement(aclFile);
    fvec.addElement("ACL");
    
    // add the ACL File to the head of the packageFiles vector
    packageFiles.addElement(packageFiles.elementAt(packageFiles.size()-1));  
    for (int j=packageFiles.size()-2;j>-1;j--) {
      Object temp = packageFiles.elementAt(j);
      packageFiles.setElementAt(temp, j+1);
    }
    packageFiles.setElementAt(fvec,0);
    
    // now create a triple for the ACL relating it to datapackage file
    Vector aclTriples = new Vector();
    Triple aclt = null;
    for(int m=0; m<frameWizards.size(); m++) 
    {
      // link the ACL to ALL metadata files
      WizardFrameContainer wfc2 = (WizardFrameContainer)frameWizards.elementAt(m);
      if ((wfc2.id!=null)&&(!(wfc2.id).equals("NULLDATAFILE"))) 
      {
        aclt = new Triple(aclID, "provides access control rules for", wfc2.id);
        aclTriples.addElement(aclt);
      }
    }
    // add triple with acl pointing to itself
    aclt = new Triple(aclID, "provides access control rules for", aclID);
    aclTriples.addElement(aclt);
    
    Hashtable tripleNames = new Hashtable();
    for(int i=0; i<frameWizards.size(); i++)
    { //create a hashtable of file names to ids for use in triple creation
      WizardFrameContainer wfc = (WizardFrameContainer)
                                  frameWizards.elementAt(i);
      String id = wfc.id;
      String name = (String)wfc.attributes.get("name");
      if(name == null || name.equals("IGNORE"))
      {
        name = "DATAFILE";
      }
      Vector v;
      if(tripleNames.containsKey(name))
      {
        v = (Vector)tripleNames.remove(name);
      }
      else
      {
        v = new Vector();
      }
      
      if(id != null)
      {
        v.addElement(id);
      }
      tripleNames.put(name, v);
    }
    
    TripleCollection tc = new TripleCollection();
    for(int i=0; i<frameWizards.size(); i++)
    {//put ids in the triples
      WizardFrameContainer wfc = (WizardFrameContainer)
                                 frameWizards.elementAt(i);
      String relationship = "isRelatedTo";
      String id = wfc.id;
      String name = (String)wfc.attributes.get("name");
      if(name == null)
      {//add a triple linking the data file to the package (if there is a 
       //data file)
        name = "DATAFILE";
        relationship = wfc.textfield.getText();
        if(relationship.indexOf("/") != -1 || 
           relationship.indexOf("\\") != -1)
        { //strip out the path info
          int slashindex = relationship.lastIndexOf("/") + 1;
          if(slashindex == 0)
          {
            slashindex = relationship.lastIndexOf("\\") + 1;
          }
          
          relationship = relationship.substring(slashindex, 
                                                relationship.length());
                                                
          relationship = "isDataFileFor(" + normalize(relationship) + ")";
        }
        
        for(int k=0; k<frameWizards.size(); k++)
        {
          WizardFrameContainer wfc2 = (WizardFrameContainer)
                                     frameWizards.elementAt(k);
          if(((String)wfc2.attributes.get("name")).equals(triplesFile))
          {
            Triple t = new Triple(id, relationship, wfc2.id);
            tc.addTriple(t);
            break;
          }
        } 
      }
      
      if(wfc.attributes.containsKey("relatedTo"))
      {
        String relation = (String)wfc.attributes.get("relatedTo");
        Log.debug(30,"relation = "+relation);
        Vector v = (Vector)tripleNames.get(relation);
        for(int j=0; j<v.size(); j++)
        {
          String rel = (String)v.elementAt(j);
          Triple t = new Triple();
          if(rel.equals("NULLDATAFILE"))
          {
            for(int k=0; k<frameWizards.size(); k++)
            {
              WizardFrameContainer wfc2 = (WizardFrameContainer)
                                         frameWizards.elementAt(k);
              if(((String)wfc2.attributes.get("name")).equals(triplesFile))
              {
                String sub = (wfc.wizard).getGlobalRoot();
                relationship = "provides "+sub+ " information for package";
                t = new Triple(id, relationship, wfc2.id);
                break;
              }
            }
          }
          else
          {
            String sub = (wfc.wizard).getGlobalRoot();
            String obj = ((String)wfc.attributes.get("relatedTo"));
            relationship = "provides "+sub+ " information for "+obj;
            t = new Triple(id, relationship, rel);
          }
          tc.addTriple(t);
        }
      }
    }
    
    
    
    //add acl triples
    if (aclTriples!=null) {
      for (int kk=0;kk<aclTriples.size();kk++) {
        tc.addTriple((Triple)aclTriples.elementAt(kk));  
      }
    }
    
    
    triples = tc;
    
    for(int i=0; i<frameWizards.size(); i++)
    {//find the triples file and add the triples to it
      Document doc = null;
      
      WizardFrameContainer wfc = (WizardFrameContainer)
                                  frameWizards.elementAt(i);
      String name = (String)wfc.attributes.get("name");
      
      if(name != null && name.equals(triplesFile))
      { //put the triples in the triples file.
        File f = wfc.getFile(false);

        if(f == null)
        {
          return;
        }
        
        DocumentBuilder parser = morpho.createDomParser();
        InputSource in;
        FileInputStream fs;
        
        CatalogEntityResolver cer = new CatalogEntityResolver();
        try 
        {
          Catalog myCatalog = new Catalog();
          myCatalog.loadSystemCatalogs();
          ConfigXML config = morpho.getConfiguration();
          String catalogPath = config.get("local_catalog_path", 0);
          ClassLoader cl = Thread.currentThread().getContextClassLoader();
          URL catalogURL = cl.getResource(catalogPath);
        
          myCatalog.parseCatalog(catalogURL.toString());
         // myCatalog.parseCatalog(catalogPath);
          cer.setCatalog(myCatalog);
        } 
        catch (Exception e) 
        {
          Log.debug(11, "Problem creating Catalog in " +
                       "packagewizardshell.handleFinishAction!" + e.toString());
        }
        
        parser.setEntityResolver(cer);
        
        try
        { //parse the wizard created file without the triples
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
          doc = parser.parse(in);
          fs.close();
        }
        catch(Exception e1)
        {
          System.err.println("File: " + f.getPath() + " : parse threw (8): " + 
                             e1.toString());
        }
        NodeList tripleNodeList = triples.getNodeList();
        NodeList docTriplesNodeList = null;
        
        try
        {
          //find where the triples go in the file
          docTriplesNodeList = XPathAPI.selectNodeList(doc, triplesTag);
        }
        catch(TransformerException se)
        {
          System.err.println("File: " + f.getPath() + " : parse threw (9): " + 
                             se.toString());
        }
        
        Node docNode = doc.getDocumentElement();
        for(int j=0; j<tripleNodeList.getLength(); j++)
        { //add the triples to the appropriate position in the file
          Node n = doc.importNode(tripleNodeList.item(j), true);
          Node triplesNode = docTriplesNodeList.item(0);
          Node parent = triplesNode.getParentNode();
          parent.appendChild(n);
        }
        
        NodeList newtriples = null;
        try
        {
          //find where the triples go in the file
          newtriples = XPathAPI.selectNodeList(doc, triplesTag);
        }
        catch(TransformerException se)
        {
          System.err.println("File: " + f.getPath() + " : parse threw (10): " + 
                             se.toString());
        }
        
        for(int j=0; j<newtriples.getLength(); j++)
        { //find the blank template triple node and remove it
          Node n = newtriples.item(j);
          if(!n.getFirstChild().getNodeName().equals("subject"))
          {
            Node parent = n.getParentNode();
            parent.removeChild(n);
          }
        }
        
        String docString = PackageUtil.printDoctype(doc);
        docString += PackageUtil.print(doc.getDocumentElement());
        
        StringReader sr = new StringReader(docString);
        FileSystemDataStore localDataStore = new FileSystemDataStore(morpho);
        localDataStore.saveFile(wfc.id, sr); //write out the file
      }
    }
    
    String location;
    if(saveToMetacatCheckBox.isSelected())
    {
      location = DataPackageInterface.BOTH;
    }
    else
    {
      location = DataPackageInterface.LOCAL;
    }
    WizardFrameContainer wfc = (WizardFrameContainer)
                                frameWizards.elementAt(1);
    String identifier = wfc.id;
    Vector relations = triples.getCollection();
    DataPackage dp = new DataPackage(location, identifier, 
                                   relations, morpho, true);
                                   
    if(saveToMetacatCheckBox.isSelected())
    {
      //save the package to metacat here
      Log.debug(15, "saving the package to metacat");
      
      MetacatDataStore mds = new MetacatDataStore(morpho);
//      for(int i=packageFiles.size()-1; i>=0; i--)
      for(int i=0;i<packageFiles.size(); i++)
      {
        Vector v = (Vector)packageFiles.elementAt(i);
        String id = (String)v.elementAt(0);
        File f = (File)v.elementAt(1);
        if(f.getPath().equals("NULLDATAFILE"))
        {
          continue;
        }
        
        String type = (String)v.elementAt(2);
        FileReader fr;
        try
        {
          fr = new FileReader(f);
          boolean publicAcc = false;
          if(publicAccessCheckBox.isSelected())
          {
            publicAcc = true;
          }
          if((!type.equals("WIZARD")) && (!type.equals("ACL")))
          { //this is a data file.  send it to metacat
        System.out.println("FileName = "+f.toString());
          
            mds.newDataFile(id, f);
          }
          else
          { //this is an xml file
            //send it to metacat
            mds.newFile(id, fr, dp);
          }
          fr.close();
        }
        catch(FileNotFoundException fnfe)
        {
          Log.debug(0, "The upload to metacat failed (1): " + 
                                fnfe.getMessage()); 
        }
        catch(MetacatUploadException mue)
        {
          Log.debug(0, "The upload to metacat failed (2): " + 
                                mue.getMessage());
        }
        catch(IOException ioe)
        {
          Log.debug(0, "The upload to metacat failed (3): " + 
                                ioe.getMessage());
        }
      }
      location = DataPackageInterface.BOTH;
    }
    
    /* No longer needed because the query isn't shown by default
     * but we still may want to notify the query subsystem that a
     * change has happened in case future refreshes are implemented
    // Update the query window to reflect the newly created package
    try {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider = 
                      services.getServiceProvider(QueryRefreshInterface.class);
      ((QueryRefreshInterface)provider).refresh();
    } catch (ServiceNotHandledException snhe) {
      Log.debug(6, snhe.getMessage());
    }
    */
    
    // Show the package
    try 
    {
      ServiceController services = ServiceController.getInstance();
       ServiceProvider provider = 
                      services.getServiceProvider(DataPackageInterface.class);
       DataPackageInterface dataPackage = (DataPackageInterface)provider;
       dataPackage.openDataPackage(location, dp.getID(), null, null);
    }
    catch (ServiceNotHandledException snhe) 
    {
       Log.debug(6, snhe.getMessage());
    }

/* old package display code    
    // Show the package in a window
    DataPackageGUI gui = new DataPackageGUI(morpho, dp);
    gui.show();
*/
        
    //make the package wizard go away
    this.dispose();
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
    FileSystemDataStore localDataStore = new FileSystemDataStore(morpho);
    
    Log.debug(11, "action fired: |" + command + "|");
    
    if(command.equals("Previous"))
    { 
      handlePreviousAction();
    }
    else if(command.equals("Next"))
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
    else if(command.equals("Get Information from Text-Based Table..."))
    {/*
      // this code is very specific to the current configuration of the Package Wizard
      // it should be generalized at some point
      // e.g. the two ints defined below depend on the entity and attribute wizards following
      // the current frame immediatel
      int entitynum = frameWizardIndex + 1;
      int attributenum = frameWizardIndex + 2;
      WizardFrameContainer wfc1 = (WizardFrameContainer)frameWizards.elementAt(entitynum);
      WizardFrameContainer wfc2 = (WizardFrameContainer)frameWizards.elementAt(attributenum);
      PackageWizard pw1 = wfc1.wizard;
      PackageWizard pw2 = wfc2.wizard;
      
      
      TextImportWizard tiw = new TextImportWizard();
      //the TextImport wizard has reference to PackageWizards so it can save the
      // XML text it generates
      tiw.setEntityWizard(pw1);
      tiw.setAttributeWizard(pw2);
      tiw.setVisible(true);
    */}
    else if(command.equals("Finish"))
    {
      handleFinishAction();
    }
    else if(command.equals("Cancel"))
    {
      int choice = JOptionPane.YES_OPTION;
        choice = JOptionPane.showConfirmDialog(null, 
                               "Are you sure that you want to cancel the " +
                               "Package Wizard now?  All created documents " +
                               "will be lost.", 
                               "Package Wizard", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
      if(choice == JOptionPane.YES_OPTION)
      {
        this.dispose();
        MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
        morphoFrame.setVisible(true);
      }
    }
  }
  
  /**
   * Creates the initial layout and buttons of the wizard
   */
  private JPanel createWizardFrame()
  {
    JPanel mainPanel = new JPanel();
    wizardFrame = new JPanel();
    JPanel buttonPanel = new JPanel();
    descriptionPanel = new JPanel();
    headPanel = new JPanel();
    
    JLabel headLabel = new JLabel();
    headLabel.setText("Package Wizard");
    ImageIcon head = new ImageIcon(
                         getClass().
                         getResource("/edu/ucsb/nceas/morpho/editor/smallheader-bg.gif"));
    headLabel.setIcon(head);
    headLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    headLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headLabel.setAlignmentY(Component.LEFT_ALIGNMENT);
    headLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    headLabel.setForeground(Color.black);
    headLabel.setFont(new Font("Dialog", Font.BOLD, 14));
    headLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    headPanel.setLayout(new FlowLayout());
    headPanel.add(headLabel);
    headPanel.setPreferredSize(new Dimension(300, 50));
    headPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    
    descriptionPanel.setBackground(Color.white);
    descriptionPanel.setPreferredSize(new Dimension(160, 450));
    //MBJ ImageIcon logoIcon = new ImageIcon(
                             //MBJ morpho.getClass().getResource("logo-icon.gif"));
    JLabel imageLabel = new JLabel();
    //MBJ imageLabel.setIcon(logoIcon);
    descriptionPanel.add(imageLabel);
    
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
    wizardFrame.setMaximumSize(new Dimension(595, 450));
    wizardFrame.setPreferredSize(new Dimension(595, 450));
    buttonPanel.setMaximumSize(new Dimension(595, 50));
    buttonPanel.setPreferredSize(new Dimension(595, 50));
    
    previous = new JButton("Previous", new ImageIcon(getClass().
               getResource("/toolbarButtonGraphics/navigation/Back16.gif")));
    previous.setEnabled(false);
    next = new JButton("Next", new ImageIcon(getClass().
           getResource("/toolbarButtonGraphics/navigation/Forward16.gif")));
    next.setHorizontalTextPosition(SwingConstants.LEFT);
    
    previous.addActionListener(this);
    next.addActionListener(this);
//    cancelButton = new JButton("Cancel", new ImageIcon(getClass().
//                   getResource("/toolbarButtonGraphics/general/Stop16.gif")));
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);
    BoxLayout buttonLayout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
    buttonPanel.setLayout(buttonLayout);
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
    buttonPanel.add(previous);
    buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
    buttonPanel.add(next);
    buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
    
    mainPanel.add(descriptionPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(8,8)));
    JPanel rightpanel = new JPanel();
    rightpanel.setLayout(new BoxLayout(rightpanel, BoxLayout.Y_AXIS));
    headPanel.setAlignmentX(0);
    wizardFrame.setAlignmentX(0);
    buttonPanel.setAlignmentX(0);
    rightpanel.add(headPanel);
    rightpanel.add(wizardFrame);
    rightpanel.add(buttonPanel);
    mainPanel.add(rightpanel);
    mainPanel.add(Box.createHorizontalGlue());
    mainPanel.add(Box.createHorizontalStrut(8));
    return mainPanel;
  }
  
  /**
   * Changes the description in the description panel at the top of the wizard
   */
  private void changeDescription(String desc)
  {
    descriptionLabel = new JLabel("<html><font color=000000>" + desc + "</font></html>");
    descriptionLabel.setForeground(Color.black);
    descriptionLabel.setFont(new Font("Dialog", Font.BOLD, 12));
    //MBJ ImageIcon logoIcon = new ImageIcon(
                             //MBJ morpho.getClass().getResource("logo-icon.gif"));
    JLabel imageLabel = new JLabel();
    //MBJ imageLabel.setIcon(logoIcon);
    descriptionLabel.setPreferredSize(new Dimension(150, 400));
    descriptionLabel.setForeground(Color.black);
    descriptionLabel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    descriptionPanel.setBorder(BorderFactory.createLoweredBevelBorder());
    descriptionPanel.removeAll();
    descriptionPanel.add(imageLabel);
    descriptionPanel.add(descriptionLabel);
    descriptionPanel.validate();
    descriptionPanel.repaint();
  }
  
  
  /**
   * returns the current description in the description panel
   */
  private String getDescription()
  {
    return descriptionText.getText();
  }
  
  /**
   * Test method
   */
  public static void main(String[] args)
  {
    try {
      ConfigXML cxml = new ConfigXML("./lib/config.xml");
      Morpho morpho = new Morpho(cxml);
      PackageWizardShell pws = new PackageWizardShell(morpho);
      pws.show();
    } catch (FileNotFoundException fnf) {
      System.err.println("Failed to find the configuration file.");
    }
  }
 
  /** create a new ACL file */
  private File getACLFile(String id) {
    String pubID = "-//ecoinformatics.org//eml-access//2.0.0beta4//EN";  
    //current default
    if (config!=null) {
        pubID = config.get("accessFileType", 0);   
    }
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\"?>\n");
    sb.append("<!DOCTYPE acl PUBLIC \"" + pubID + "\" \"eml-access.dtd\">\n");
    sb.append("<acl authSystem=\"knb\" order=\"allowFirst\">\n");
    // Note: newer acl eml dtd requires the 'system' attribute - BE SURE NEW dtd 
    //is added to metacat!!
    sb.append("<identifier>" + id + "</identifier>\n");
    sb.append("<allow>\n");
    sb.append("<principal>" + morpho.getUserName() + "</principal>\n");
    sb.append("<permission>all</permission>\n");
    sb.append("</allow>\n");
    if (publicAccessCheckBox.isSelected()) {
        sb.append("<allow>\n");
        sb.append("<principal>public</principal>\n");
        sb.append("<permission>read</permission>\n");
        sb.append("</allow>\n");
    }
    sb.append("</acl>");
    String aclString = sb.toString();
    StringReader aclReader = new StringReader(aclString);
    FileSystemDataStore localDataStore = new FileSystemDataStore(morpho);
    File file = localDataStore.saveFile(id, aclReader);
    aclReader.close();
    return file;
  }
  
  /**
   * Provides a container for the wizard frames and all of their attributes
   * this also implements the getData() method which is where the xml (or
   * other output) gets written to disk (or to metacat)
   */
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
    protected String originalDataFilePath = null;
    private FileSystemDataStore localDataStore = 
        new FileSystemDataStore(morpho);
    
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
      AccessionNumber a = new AccessionNumber(morpho);
      if(type.equals("WIZARD"))
      {
        if(temp && id == null)
        {
          id = "tmp." + tempIdCounter++;
        }
        else if(!temp && (id.indexOf("tmp") != -1 || id == null))
        {
          id = a.getNextId();
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
          file = localDataStore.saveFile(id, xmlReader);
        }
        return this.file;
      }
      else if(type.equals("IGNORE"))
      {
        return new File("FAKE");
      }
      else
      {
        String filepath = textfield.getText().trim();
        
        if(temp && id == null)
        {
          id = "tmp." + tempIdCounter++;
        }
        else if(temp && id.equals("NULLDATAFILE"))
        {
          if(!filepath.equals(""))
          {
            id = "tmp." + tempIdCounter++;
          }
        }
        else if(!temp && (id.indexOf("tmp") != -1 || id == null))
        {
          id = a.getNextId();
        }

        if(!filepath.equals(""))
        {
          file = new File(filepath);
          originalDataFilePath = textfield.getText();
 //DFH         FileReader fr = null;
          FileInputStream fr = null;
          attributes.remove("name");
          try
          {
//DFH            fr = new FileReader(file);
              fr = new FileInputStream(file);
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
            file = localDataStore.saveTempDataFile(id, fr);
          }
          else
          {
            file = localDataStore.saveDataFile(id, fr);
          }
          return this.file;
        }
        else
        {
          file = new File("NULLDATAFILE");
          id = "NULLDATAFILE";
          attributes.put("name", "IGNORE");
          return this.file;
        }
      }
    }
  }
  
  public void importComplete() {
    if (startingFrame==0) {
      newImportComplete();  
    }
    else {
      addImportComplete();
    }
  }
  
  private void addImportComplete() {
    for(int i=startingFrame; i<frameWizards.size(); i++)
    {
      WizardFrameContainer wfcont = (WizardFrameContainer)frameWizards.elementAt(i);
      String id = wfcont.id;
      Hashtable atts = wfcont.attributes;
      String name = (String)atts.get("name");
      String item = name;
      if(name == null)
      {
        name = "Data File";
        item = wfcont.originalDataFilePath;
      }
      else if(!name.equals("InitialDescription") && !name.equals("IGNORE"))
      {
        wfcont.getFile(true);
      }
    }
    Vector filesVec = handleFinishAddData();
    this.hide();
    this.dispose();
    addMetaWiz.addingNewDataWizardCompleted(filesVec);
  }
  
  private void newImportComplete()
  {
    frameWizardIndex += 2;
    WizardFrameContainer nextContainer = (WizardFrameContainer)
                                         frameWizards.elementAt(frameWizardIndex);  
    //activeContainer = nextContainer;
    //changeDescription(nextContainer.description);
    
    //we are at the end of the frames so we need to build the finish
    //frame and prepare to exit the wizard
    //-save the last frame's data
    //-display the finish frame
    wizardFrame.removeAll();
    wizardFrame.invalidate();
    frameWizardIndex++;
    changeDescription(finishDescription);
    Vector listContent = new Vector();
    for(int i=0; i<frameWizards.size(); i++)
    { //build the summary list box
      WizardFrameContainer wfcont = (WizardFrameContainer)
                                                    frameWizards.elementAt(i);
      String id = wfcont.id;
      Hashtable atts = wfcont.attributes;
      String name = (String)atts.get("name");
      String item = name;
      if(name == null)
      {
        name = "Data File";
        item = wfcont.originalDataFilePath;
      }
      else if(!name.equals("InitialDescription") && !name.equals("IGNORE"))
      {
        if(wfcont.attributes.containsKey("displayNamePath"))
        {
          String namePath = (String)wfcont.attributes.get("displayNamePath");
          if(wfcont.file == null)
          {
            wfcont.getFile(true);
          }
          NodeList namelist = PackageUtil.getPathContent(wfcont.file, 
                                                         namePath, morpho);
          
          for(int j=0; j<namelist.getLength(); j++)
          {
            Node itemnode = namelist.item(j);
            item = (String)itemnode.getFirstChild().getNodeValue().trim();
            
            if(item != null && !item.equals("InitialDescription"))
            {
              listContent.addElement(item);
            }
          }
        }
      }
    }
    
    donePanel = new JPanel();
      if (Morpho.isConnected()) {
        saveToMetacatCheckBox = new JCheckBox("Save package to Network?", true);
      }
      else {
        saveToMetacatCheckBox = new JCheckBox("Save package to Network?", false);
      }
    publicAccessCheckBox = new JCheckBox("Package should be publicly " +
                                         "readable (on Network)?", true);
    //saveToMetacatButton = new JButton("Save To Network");
    JList idlist = new JList(listContent);
    idlist.setPreferredSize(new Dimension(100,1000));
    JLabel listLabel = new JLabel("You are creating the following package " +
                                  "members: ");
    donePanel.setLayout(new BoxLayout(donePanel, BoxLayout.Y_AXIS));
    donePanel.add(Box.createRigidArea(new Dimension(0,90)));
    donePanel.add(Box.createVerticalGlue());
    donePanel.add(listLabel);
    donePanel.add(new JScrollPane(idlist));
    donePanel.add(Box.createRigidArea(new Dimension(0,20)));
    donePanel.add(saveToMetacatCheckBox);
    donePanel.add(Box.createRigidArea(new Dimension(0,20)));
    donePanel.add(publicAccessCheckBox);
    //donePanel.add(Box.createRigidArea(new Dimension(0,20)));
    //donePanel.add(openCheckBox);
    wizardFrame.add(donePanel);
    wizardFrame.validate();
    next.setText("Finish");
    next.setIcon(null);
    
    this.setVisible(true);
  }
  
  public void importCanceled()
  {
    if (startingFrame==0) {
      this.setVisible(true);
    }
    else {
      addMetaWiz.addingNewDatAWizardCanceled(); 
    }
  }
  
    /** Normalizes the given string. */
    private String normalize(String s) {
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<': {
                    str.append("&lt;");
                    break;
                }
                case '>': {
                    str.append("&gt;");
                    break;
                }
                case '&': {
                    str.append("&amp;");
                    break;
                }
                case '"': {
                    str.append("&quot;");
                    break;
                }
                case '\r':
		case '\t':
                case '\n': {
                    if (false) {
                        str.append("&#");
                        str.append(Integer.toString(ch));
                        str.append(';');
                        break;
                    }
                    // else, default append char
			break;
                }
                default: {
                    str.append(ch);
                }
            }
        }

        return str.toString();

    } // normalize(String):String
	
private Vector handleFinishAddData()
{
    Vector packageFiles = new Vector();
    
    for(int i=startingFrame; i<frameWizards.size(); i++)
    { //save all of the files to their new home
      WizardFrameContainer wfc = (WizardFrameContainer)
                                  frameWizards.elementAt(i);
      
      File f = wfc.getFile(false);
      if(f == null)
      {
        return null;
      }
      
      if(f.getName().equals("FAKE"))
      {
        continue;
      }
    
      Vector fileVec = new Vector();
      if (!wfc.type.equals("GETDATA")) {
        fileVec.addElement(wfc.id);
      }
      else {
        fileVec.addElement(fileTextField.getText());
      }
      fileVec.addElement(f);
      fileVec.addElement(wfc.type);
      packageFiles.addElement(fileVec);
      
    }
    return packageFiles;
}
  
}
