/**
 *  '$RCSfile: NewPackageMetadataWizard.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-07-23 16:01:33 $'
 * '$Revision: 1.16 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;

import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xalan.xpath.xml.FormatterToXML;
import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.apache.xerces.dom.DocumentTypeImpl;

import com.arbortext.catalog.*;

/**
 * A graphical window for adding new files to a package
 */
public class NewPackageMetadataWizard extends JFrame
                                      implements ActionListener,
                                                 EditingCompleteListener, 
                                                 WindowListener
{
  ConfigXML config;
  ClientFramework framework = null;
  /** the total number of screens to be processed */
  int numScreens;
  /** the screen currently displaying (indexed from 0 to numScreens-1) */
  int currentScreen;
  Hashtable newXMLFileAtts = new Hashtable();
  Vector radioButtons = new Vector();
  DataPackage dataPackage;
  File newxmlFile;
  Hashtable relatedFiles = new Hashtable();
  boolean prevFlag = false;
  String relatedto = "";
  String relatedtoId = "";
  Vector relatedFileIds;
  File addedFile = null;
  String relateDataFileTo = "";
  String dummydoc = "";
  boolean editingExistingFile;
  String existingFileId;
  boolean finishflag = false;
  
  JLabel helpLabel = new JLabel();
  JPanel screenPanel = null;
  JButton previousButton = null;
  JButton nextButton = null;
  JButton cancelButton = new JButton();

  ImageIcon forwardIcon = null;
  
  JRadioButton createNew = new JRadioButton("New Description");
  JRadioButton existingFile = new JRadioButton("Existing Data File");
  JTextField fileTextField = new JTextField();
  JList relatedFileList;
  
  /**
   * Construct a dialog and set the framework
   *
   * @param cont the container framework that created this ProfileDialog
   */
  public NewPackageMetadataWizard(ClientFramework cont) {
    this(cont, true, null);
  }

  /**
   * Construct the dialog
   */
  public NewPackageMetadataWizard(ClientFramework cont, boolean modal,
                                  DataPackage dataPackage)
  {
    //super((Frame)cont, modal);
    framework = cont;
    config = framework.getConfiguration();
    this.dataPackage = dataPackage;
    this.addWindowListener(this);
    
    //if functionality to add an existing file is added, remove this line.
    //existingFile.setEnabled(false);
    
    numScreens = 5;
    currentScreen = 0;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setTitle("Add New Descriptions");
    getContentPane().setLayout(new BoxLayout(getContentPane(),
                               BoxLayout.X_AXIS));
    setVisible(false);

    createNew.setSelected(true);
    ButtonGroup group1 = new ButtonGroup();
    group1.add(createNew);
    group1.add(existingFile);
    
    //parse the config file and create the new file buttons
    newXMLFileAtts = PackageUtil.getConfigFileTypeAttributes(framework, "label");
    
    Enumeration keys = newXMLFileAtts.keys();
    ButtonGroup newRadioButtons = new ButtonGroup();
    while(keys.hasMoreElements())
    { //create the radio buttons for the file type choices
      String key = (String)keys.nextElement();
      if(key.equals("DATAFILE"))
      {
        continue;
      }
      Hashtable h = (Hashtable)newXMLFileAtts.get(key);
      JRadioButton b = new JRadioButton((String)h.get("label"));
      if(h.containsKey("visible"))
      {
        String visible = (String)h.get("visible");
        if(visible.equals("no"))
        {
          b.setVisible(false);
        }
      }
      
      b.setToolTipText((String)h.get("tooltip"));
      newRadioButtons.add(b);
      radioButtons.addElement(b);
    }
    
    JPanel helpPanel = new JPanel();
    helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
    helpPanel.setBackground(Color.white);
    helpPanel.setMaximumSize(new Dimension(150,400));
    helpPanel.setPreferredSize(new Dimension(150,400));
    helpPanel.setBorder(BorderFactory.createLoweredBevelBorder());
    helpPanel.add(Box.createRigidArea(new Dimension(8,8)));
    ImageIcon logoIcon = 
              new ImageIcon(framework.getClass().getResource("logo-icon.gif"));
    JLabel imageLabel = new JLabel();
    imageLabel.setIcon(logoIcon);
    helpPanel.add(imageLabel);
    helpLabel.setText(
              "<html>This is a whole bunch of help text.</html>");
    helpLabel.setForeground(Color.black);
    helpLabel.setFont(new Font("Dialog", Font.BOLD, 12));
    helpLabel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    helpLabel.setVerticalAlignment(SwingConstants.TOP);
    helpPanel.add(helpLabel);
    getContentPane().add(helpPanel);

    getContentPane().add(Box.createRigidArea(new Dimension(8,8)));

    JPanel entryPanel = new JPanel();
    entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
    entryPanel.add(Box.createVerticalStrut(8));

    JLabel headLabel = new JLabel();
    headLabel.setText("Add New Descriptions");
    ImageIcon head = new ImageIcon(
                         framework.getClass().getResource("smallheader-bg.gif"));
    headLabel.setIcon(head);
    headLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    headLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headLabel.setAlignmentY(Component.LEFT_ALIGNMENT);
    headLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    headLabel.setForeground(Color.black);
    headLabel.setFont(new Font("Dialog", Font.BOLD, 14));
    headLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    JPanel headPanel = new JPanel();
    headPanel.setLayout(new FlowLayout());
    headPanel.add(headLabel);
    entryPanel.add(headPanel);
    entryPanel.add(Box.createVerticalStrut(8));

    screenPanel = new JPanel();
    screenPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    entryPanel.add(screenPanel);
    entryPanel.add(Box.createVerticalGlue());
    entryPanel.add(Box.createRigidArea(new Dimension(8,8)));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(Box.createHorizontalGlue());
    cancelButton.setText("Cancel");
    cancelButton.setActionCommand("Cancel");
    cancelButton.setEnabled(true);
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createHorizontalStrut(8));
    previousButton = new JButton("Previous", new ImageIcon( framework.getClass().
          getResource("/toolbarButtonGraphics/navigation/Back16.gif")));
    previousButton.setHorizontalTextPosition(SwingConstants.RIGHT);
    buttonPanel.add(previousButton);
    buttonPanel.add(Box.createHorizontalStrut(8));
    forwardIcon = new ImageIcon( framework.getClass().
          getResource("/toolbarButtonGraphics/navigation/Forward16.gif"));
    nextButton = new JButton("Next", forwardIcon);
    nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
    nextButton.setEnabled(true);
    buttonPanel.add(nextButton);
    buttonPanel.add(Box.createHorizontalStrut(8));

    entryPanel.add(buttonPanel);
    entryPanel.add(Box.createVerticalStrut(8));
    getContentPane().add(entryPanel);
    getContentPane().add(Box.createHorizontalGlue());
    getContentPane().add(Box.createHorizontalStrut(8));

    // Layout the first screen
    layoutScreen();

    // Size and center the frame
    pack();
    setResizable(false);
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2 ,
            (screenDim.height - frameDim.height) /2);

    // register a listener for ActionEvents
    ActionHandler myActionHandler = new ActionHandler();
    previousButton.addActionListener(myActionHandler);
    nextButton.addActionListener(myActionHandler);
    cancelButton.addActionListener(myActionHandler);
  }

  /**
   * Listener used to detect button presses
   */
  private class ActionHandler implements ActionListener
  {
    public void actionPerformed(ActionEvent event)
    {
      Object object = event.getSource();
      if (object == previousButton) {
        previousButtonHandler(event);
      } else if (object == nextButton) {
        nextButtonHandler(event);
      } else if (object == cancelButton) {
        cancelButtonHandler(event);
      }
    }
  }

  /**
   * Perform actions associated with the Previous button
   */
  private void previousButtonHandler(ActionEvent event)
  {
    prevFlag = true;
    if (currentScreen > 0) {
      currentScreen--;
      layoutScreen();
    }
  }
  
  /**
   * Perform actions associated with the Next button
   */
  private void nextButtonHandler(ActionEvent event)
  {
    prevFlag = false;
    if (currentScreen == numScreens - 1) {
      handleFinishAction();
      this.dispose();
    } else {
      currentScreen++;
      layoutScreen();
    }
  }

  /**
   * Perform actions associated with the Disconnect button
   */
  private void cancelButtonHandler(ActionEvent event)
  {
    dispose();
  }

  /**
   * Lay out labels and fields in an orderly grid.
   */
  private void addLabelTextRows(JLabel[] labels,
                                JTextField[] textFields,
                                GridBagLayout gridbag,
                                Container container) {
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.EAST;
    int numLabels = labels.length;

    for (int i = 0; i < numLabels; i++) {
      c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
      c.fill = GridBagConstraints.NONE;      //reset to default
      c.weightx = 0.0;                       //reset to default
      c.anchor = GridBagConstraints.EAST;
      gridbag.setConstraints(labels[i], c);
      container.add(labels[i]);

      c.gridwidth = GridBagConstraints.REMAINDER;     //end row
      //c.fill = GridBagConstraints.HORIZONTAL; // fill remaining space
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.WEST;
      gridbag.setConstraints(textFields[i], c);
      container.add(textFields[i]);
    }
  }

  /** 
   * Put the new components on the screen and redraw.
   */
  private void layoutScreen()
  {
    screenPanel.removeAll();

    GridBagLayout gridbag = new GridBagLayout();
    screenPanel.setLayout(gridbag);

    if (0 == currentScreen) 
    { //find out whether the user wants to create a new file or use an existing 
      //one
      String helpText = "<html><p>This wizard will assist you in creating or " +
                        "inserting new descriptions or data files into your " +
                        "Data Package.  You will be asked several questions " +
                        "about the type of information that you would like to" +
                        "provide then the wizard will insert your new " +
                        "information into the package.</p></html>";
      helpLabel.setText(helpText);

      screenPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(8,8,8,8),
                            ""));
      
      JLabel initLabel = new JLabel("<html><p><font color=black>What kind of " +
                                    "information would you like " +
                                    "to provide?  To add an existing data file " +
                                    "on your system, select 'Existing " +
                                    "File'. " +
                                    "To add new descriptions from scratch, " +
                                    "select 'New Description'." +
                                    "</font></p></html>");
      initLabel.setMaximumSize(new Dimension(375, 100));
      screenPanel.add(initLabel);
      screenPanel.setMaximumSize(new Dimension(400, 300));
      screenPanel.setPreferredSize(new Dimension(400, 300));
      JPanel layoutpanel = new JPanel();
      layoutpanel.setLayout(new BoxLayout(layoutpanel, BoxLayout.Y_AXIS));
      layoutpanel.add(createNew);
      layoutpanel.add(existingFile);
      screenPanel.add(layoutpanel);
      screenPanel.setLayout(new GridLayout(0,1));
    } 
    else if (1 == currentScreen) 
    {
      if(createNew.isSelected())
      { //give the user choices as to which type of MD they want to create
        String helpText = "<html>Select the type of description that you " +
                          "to add. Holding your mouse over any of the " +
                          "selections will give you more information on that " +
                          "item. Clicking the 'Next' button will temporarily " +
                          "close this window and open the editor so you can " +
                          "fill out your new description.  Closing the editor " +
                          "will reopen this wizard.</html>";
        helpLabel.setText(helpText);
        JLabel initLabel = new JLabel("<html><p><font color=black>What kind of " +
                                    "description would you like " +
                                    "to add?</font></p></html>");
        initLabel.setMaximumSize(new Dimension(375, 100));
        JPanel layoutpanel = new JPanel();
        layoutpanel.setLayout(new BoxLayout(layoutpanel, BoxLayout.Y_AXIS));
        layoutpanel.add(initLabel);
        boolean selected = false;
        for(int i=0; i<radioButtons.size(); i++)
        { //add the dynamically created buttons for the file types
          //these file types are specified in the config.xml file
          JRadioButton jrb = (JRadioButton)radioButtons.elementAt(i);
          if(jrb.isVisible() && !selected)
          {
            jrb.setSelected(true);
            selected = true;
          }
          layoutpanel.add(jrb);
        }
        screenPanel.add(layoutpanel);
        screenPanel.setLayout(new GridLayout(0,1));
        dummydoc = "";
      }
      else
      { //display an open file dialog
        String helpText = "<html>Select a file to add to your data package." +
                          "</html>";
        helpLabel.setText(helpText);
        JLabel initLabel = new JLabel("<html><p><font color=black>Choose a " +
                                      "file.</font></p></html>");
        initLabel.setMaximumSize(new Dimension(375, 100));
        JPanel layoutpanel = new JPanel();
        //layoutpanel.setLayout(new BoxLayout(layoutpanel, BoxLayout.X_AXIS));
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(this);
        fileTextField.setColumns(20);
        screenPanel.add(initLabel);
        layoutpanel.add(fileTextField);
        layoutpanel.add(browseButton);
        screenPanel.add(layoutpanel);
        screenPanel.setLayout(new GridLayout(0,1));
      }
    }
    else if(2 == currentScreen)
    {
      //if(prevFlag)
      //{
      //  previousButtonHandler(new ActionEvent(this, 0, ""));
      //  return;
      //}
      
      if(createNew.isSelected())
      { //open the editor and handle the editing complete action
        
        //pick which type of file to open
        //dummydoc = "";
        if(dummydoc.equals(""))
        {
          for(int i=0; i<radioButtons.size(); i++)
          {
            JRadioButton jrb = (JRadioButton)radioButtons.elementAt(i);
            if(jrb.isSelected())
            { //this was the type selected.  open the editor for this type.
              String label = jrb.getLabel();
              Hashtable h = (Hashtable)newXMLFileAtts.get(label);
              String editexisting = (String)h.get("editexisting");
              String doctype = (String)h.get("xmlfiletype");
              String rootnode = (String)h.get("rootnode");
              dummydoc += "<?xml version=\"1.0\"?>\n";
              dummydoc += "<!DOCTYPE " + rootnode + " PUBLIC \"" + doctype + 
                          "\" \"" + rootnode + ".dtd\">\n";
              dummydoc += "<" + rootnode + ">" + "</" + rootnode + ">";
              editingExistingFile = false;
              
              if(editexisting.toUpperCase().equals("YES"))
              { //if editexisting is yes, then we send the existing document
                //of this type to the editor instead of a blank doc.
                Hashtable existingDocs = dataPackage.getRelatedFiles();
                if(existingDocs.containsKey(doctype))
                { //get the id and open the file
                  Vector ids = (Vector)existingDocs.get(doctype.trim());
                  String id = (String)ids.elementAt(0);
                  StringBuffer sb = new StringBuffer();
                  try
                  {
                    File dummyfile = PackageUtil.openFile(id, framework);
                    FileReader fr = new FileReader(dummyfile);
                    int c = fr.read();
                    while(c != -1)
                    {
                      sb.append((char)c);
                      c = fr.read();
                    }
                    fr.close();
                  }
                  catch(Exception e)
                  {
                    ClientFramework.debug(0, "Error reading existing file in " +
                                          "package: " + e.getMessage());
                    e.printStackTrace();
                  }
                  //overwrite dummydoc with the document that needs to be edited
                  dummydoc = sb.toString();
                  editingExistingFile = true;
                  existingFileId = id;
                }
              }
              break;
            }
          }
        }
      
        EditorInterface editor = PackageUtil.getEditor(framework);
        editor.openEditor(dummydoc, null, null, this);
        this.hide();
      }
      else
      { //the user wishes to put an existing file into the package.
        addedFile = new File(fileTextField.getText());
/*
        StringBuffer sb;
        try
        {
          FileReader fr = new FileReader(addedFile);
          sb = new StringBuffer();
          int c = fr.read();
          while(c != -1)
          {
            sb.append((char)c);
            c = fr.read();
          }
        }
        catch(Exception e)
        {
          ClientFramework.debug(0, "Error reading selected file.: " +
                                e.getMessage());
          e.printStackTrace();
          return;
        }
        String fileString = sb.toString();
        if(fileString.indexOf("<?xml") != -1)
        { //this is an xml file
          //System.out.println("xml file: " + fileString);
          //ClientFramework.debug(0, "This doesn't work yet");
        }
        else
        { //this is a data file
          boolean metacatpublic = false;
          int choice = JOptionPane.showConfirmDialog(null, 
                                 "<html>The file you entered is not an XML file. " +
                                 "Because of this, Morpho assumes that this " +
                                 "is a data file.  Is this assumption " +
                                 "correct?  If you are not adding a data " +
                                 "file, click no and choose an XML " +
                                 "description file.</html>", 
                                 "Data File?", 
                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                 JOptionPane.WARNING_MESSAGE);
          if(choice != JOptionPane.YES_OPTION)
          {
            return;
          }
 */       
          
/*
          // This sections establishes the relationships to go in triples
          relatedFileIds = (Vector)packageFiles.get(relatedto.trim());
          if(relatedFileIds != null && relatedFileIds.size() == 1)
          {
            relatedtoId = (String)relatedFileIds.elementAt(0);
          }
          else if(relatedFileIds != null && relatedFileIds.size() > 1)
          {
            for(int i=0; i<relatedFileIds.size(); i++)
            { //get the name and ids of the files related to this new one
              File f;
              try
              {
                f = PackageUtil.openFile((String)relatedFileIds.elementAt(i), 
                                         framework);
              }
              catch (Exception e)
              {
                ClientFramework.debug(0, "Error in NewPackageMetadataWizard." +
                                      "editingComplete(): " + e.getMessage());
                return;
              }
              NodeList nl = PackageUtil.getPathContent(f, displayPath, framework);
             
              for(int j=0; j<nl.getLength(); j++)
              {
                Node n = nl.item(j);
                String name = n.getFirstChild().getNodeValue().trim();
                relatedFiles.put(relatedFileIds.elementAt(i), name);
              }
            }
          }
*/

/*
        }
*/
      }
    }
    else if(3 == currentScreen)
    {
      //display the file that was just created and ask the user which file to 
      //associate it with if there are more than one files to associate it with
      
      if(relatedFiles.size() > 1)
      {
        String helpText = "<html><font color=black>The file " +
                              "that you have created must be associated with " +
                              "another file in the package. Please choose " +
                              "from the following list of files which you " +
                              "would like to associate it with.</font></html>";
        helpLabel.setText(helpText);
        JLabel explanationL = new JLabel("<html><font color=black>Choose " + 
                              "the package member to associate your new " +
                              "description with from the list below.</font>" +
                              "</html>");
        explanationL.setPreferredSize(new Dimension(400, 100));
        explanationL.setMaximumSize(new Dimension(400, 100));
        Vector listvec = new Vector();
        Enumeration keys = relatedFiles.keys();
        while(keys.hasMoreElements())
        {
          String name = (String)relatedFiles.get(keys.nextElement());
          listvec.add(name);
        }
        relatedFileList = new JList(listvec);
        relatedFileList.setVisibleRowCount(5);
        relatedFileList.setPreferredSize(new Dimension(300, 150));
        relatedFileList.setMaximumSize(new Dimension(300, 150));
        relatedFileList.setMinimumSize(new Dimension(300, 150));
        JPanel layoutpanel = new JPanel();
        layoutpanel.setPreferredSize(new Dimension(400, 300));
        layoutpanel.setMaximumSize(new Dimension(400, 300));
        layoutpanel.setMinimumSize(new Dimension(400, 300));
        layoutpanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        layoutpanel.add(explanationL);
        layoutpanel.add(new JScrollPane(relatedFileList));
        screenPanel.add(layoutpanel);
      }
      else
      { //there is only one file to associate the new file with so we skip 
        //this frame
        if(prevFlag)
        {
          previousButtonHandler(new ActionEvent(this, 0, ""));
          return;
        }
        nextButtonHandler(new ActionEvent(this, 0, ""));
      }
    }
    else if(4 == currentScreen)
    { //check the input from the last screen...go to handleFinishAction
      if(relatedFileIds != null)
      {
        if(relatedFileIds.size() > 1)
        {
          if(relatedFileList.getSelectedValue() == null)
          {
            ClientFramework.debug(0, "You must choose an item to related this " +
                                     "file to.");
            previousButtonHandler(new ActionEvent(this, 0, ""));
            return;
          }
          
          Enumeration keys = relatedFiles.keys();
          while(keys.hasMoreElements())
          {
            String key = (String)keys.nextElement();
            String name = (String)relatedFiles.get(key);
            if(name.equals(relatedFileList.getSelectedValue()))
            {
              relatedtoId = key;
              break;
            }
          }
        }
        else
        {
          relatedtoId = (String)relatedFileIds.elementAt(0);
        }
      }
      else
      {
        relatedtoId = dataPackage.getID();
      }
      
      //so now we have a pointer to the new file and the id of the file that
      //we are relating it to so we can create the new triples and put 
      //them in the triples file when the user presses the finish button
      
      String helpText = "<html><font color=black>When you press the 'Finish'" +
                        "button, you will be presented with the Package " +
                        "Editor which will show your new descriptions " +
                        "</font></html>";
      helpLabel.setText(helpText);
      JLabel lastlabel = new JLabel("<html><font color=black>The wizard " +
                         "is now ready to finish creating your new " +
                         "description.  Click the 'Finish' button to " +
                         "complete the process.</font></html>");
      lastlabel.setPreferredSize(new Dimension(400, 300));
      lastlabel.setMaximumSize(new Dimension(400, 300));
      lastlabel.setMinimumSize(new Dimension(400, 300));
      screenPanel.add(lastlabel);
    }
    
    // Set button states properly
    if (0 == currentScreen) {
      previousButton.setEnabled(false);
    } else {
      previousButton.setEnabled(true);
    }

    if (currentScreen == numScreens-1) {
      nextButton.setEnabled(true);
      nextButton.setText("Finished");
      nextButton.setIcon(null);
    } else {
      nextButton.setEnabled(true);
      nextButton.setText("Next");
      nextButton.setIcon(forwardIcon);
    }

    // Repaint the screen
    screenPanel.validate();
    screenPanel.paint(screenPanel.getGraphics());
  }
  
  private void handleAddDataFile(boolean locLocal, boolean locMetacat, 
                                 String newid)
  { //add a data file here whenever metacat supports it.
    AccessionNumber a = new AccessionNumber(framework);
    if(locLocal)
    {
      FileSystemDataStore fsds = new FileSystemDataStore(framework);
      Triple t = new Triple(newid, "isRelatedTo", relatedtoId);
      TripleCollection triples = new TripleCollection();
      triples.addTriple(t);
      File packageFile = dataPackage.getTriplesFile();
      //add the triple to the triple file
      String docString = PackageUtil.addTriplesToTriplesFile(triples, 
                                                             dataPackage, 
                                                             framework);
      //write out the files
      File newDPTempFile;
      //get a new id for the package file
      String dataPackageId = a.incRev(dataPackage.getID());
      try
      { //this handles the package file
        //save a temp file with the new id
        newDPTempFile = fsds.saveTempFile(dataPackageId,
                                          new StringReader(docString));
        //inc the revision of the new Package file in the triples
        docString = a.incRevInTriples(newDPTempFile, dataPackage.getID(), 
                                      dataPackageId);
        //save new temp file that has the right id and the id inced in the triples
        newDPTempFile = fsds.saveTempFile(dataPackageId, 
                                          new StringReader(docString));
      }
      catch(Exception e)
      {
        ClientFramework.debug(0, "Error saving file: " + e.getMessage());
        e.printStackTrace();
        return;
      }
      
      File newPackageMember;
      try
      { //save the new package member
        newPackageMember = fsds.newFile(newid, new FileReader(addedFile), 
                                        false);
      }
      catch(Exception e)
      {
        ClientFramework.debug(0, "Error saving file: " + e.getMessage());
        e.printStackTrace();
        e.printStackTrace();
        return;
      }
      
      try
      { //save the new package file
        fsds.saveFile(dataPackageId, new FileReader(newDPTempFile), false);
      }
      catch(Exception e)
      {
        ClientFramework.debug(0, "Error saving file: " + e.getMessage());
        e.printStackTrace();
        return;
      }
    }
    
    if(locMetacat)
    {
      ClientFramework.debug(0, "Metacat does not currently support uploading" +
                            " data files.");
      return;
    }
    
  }
  
  /**
   * handles the action when the user clicks the finish button
   */
  private void handleFinishAction()
  {
    finishflag = true;
    AccessionNumber a = new AccessionNumber(framework);
    String newid = "";
    String location = dataPackage.getLocation();
    boolean locMetacat = false;
    boolean locLocal = false;
    String docString;
    FileSystemDataStore fsds = new FileSystemDataStore(framework);
    File packageFile = dataPackage.getTriplesFile();
    
    if(location.equals(DataPackage.LOCAL) || 
       location.equals(DataPackage.BOTH))
    {
      locLocal = true;
    }
    
    if(location.equals(DataPackage.METACAT) || 
       location.equals(DataPackage.BOTH))
    {
      locMetacat = true;
    }
    
    if(addedFile != null)
    { //we are adding a data file
      handleAddDataFile(locLocal, locMetacat, newid);
      return;
    }
     
    if(editingExistingFile)
    { //if we edited an existing file we don't need to create a new id
      //and a new triple
      newid = a.incRev(existingFileId);
      docString = a.incRevInTriples(packageFile, existingFileId, newid);
    }
    else
    { //create a new id and a new triple for the triple file.
      newid = a.getNextId();
      Triple t = new Triple(newid, "isRelatedTo", relatedtoId);
      TripleCollection triples = new TripleCollection();
      triples.addTriple(t);
      //add the triple to the triple file
      docString = PackageUtil.addTriplesToTriplesFile(triples, dataPackage, 
                                                      framework);
    }
    
    //System.out.println(docString);
    File newDPTempFile;
    //get a new id for the package file
    String dataPackageId = a.incRev(dataPackage.getID());
    try
    { //this handles the package file
      //save a temp file with the new id
      newDPTempFile = fsds.saveTempFile(dataPackageId,
                                    new StringReader(docString));
      //inc the revision of the new Package file in the triples
      docString = a.incRevInTriples(newDPTempFile, dataPackage.getID(), 
                                    dataPackageId);
      //save new temp file that has the right id and the id inced in the triples
      newDPTempFile = fsds.saveTempFile(dataPackageId, 
                                    new StringReader(docString));
    }
    catch(Exception e)
    {
      ClientFramework.debug(0, "Error saving file(5): " + e.getMessage());
      e.printStackTrace();
      return;
    }
    
    /*
    try
    {
    FileReader fr = new FileReader(newxmlFile);
    int c = fr.read();
    while(c != -1)
    {
      System.out.print((char)c);
      c = fr.read();
    }
    }
    catch(Exception e)
    {}
    */
    
    if(locLocal)
    { //save the real files locally.
      File newPackageMember;
      try
      { //save the new package member
        if(editingExistingFile)
        {
          newPackageMember = fsds.saveFile(newid, new FileReader(newxmlFile), 
                                           false);
        }
        else
        {
          newPackageMember = fsds.newFile(newid, new FileReader(newxmlFile), 
                                          false);
        }
      }
      catch(Exception e)
      {
        ClientFramework.debug(0, "Error saving file(4): " + e.getMessage());
        e.printStackTrace();
        e.printStackTrace();
        return;
      }
      
      try
      { //save the new package file
        fsds.saveFile(dataPackageId, new FileReader(newDPTempFile), false);
      }
      catch(Exception e)
      {
        ClientFramework.debug(0, "Error saving file(3): " + e.getMessage());
        e.printStackTrace();
        return;
      }
    }
    
    if(locMetacat)
    { //save the real files to metacat.
      MetacatDataStore mds = new MetacatDataStore(framework);
      boolean metacatpublic = false;
      try
      {
        int choice = JOptionPane.showConfirmDialog(null, 
                               "Do you wish to make this file publicly readable "+ 
                               "(Searchable) on Metacat?", 
                               "New Description", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
        if(choice == JOptionPane.YES_OPTION)
        {
          metacatpublic = true;
        }
        
        if(editingExistingFile)
        {
          mds.saveFile(newid, new FileReader(newxmlFile), metacatpublic);
        }
        else
        {
          mds.newFile(newid, new FileReader(newxmlFile), metacatpublic);
        }
        
      }
      catch(Exception e)
      {
        ClientFramework.debug(0, "Error saving file(1): " + e.getMessage());
        e.printStackTrace();
        return;
      }
      
      try
      {
        mds.saveFile(dataPackageId, new FileReader(newDPTempFile), 
                     metacatpublic);
      }
      catch(Exception e)
      {
        ClientFramework.debug(0, "Error saving file(2): " + e.getMessage());
        e.printStackTrace();
        return;
      }
    }
    
    //refresh the package editor that this wizard came from.
    DataPackage newpackage = new DataPackage(dataPackage.getLocation(),
                                             dataPackageId, null,
                                             framework);
    DataPackageGUI dpg = new DataPackageGUI(framework, newpackage);
    dpg.show();
    dpg.setName("Package Editor:" + newpackage.getID());
    framework.addWindow(dpg);
    framework.removeWindow(this);
  }

  public void actionPerformed(ActionEvent e) 
  {
    String command = e.getActionCommand();
    if(command.equals("Browse..."))
    {
      JFileChooser filechooser = new JFileChooser();
      File datafile;
      filechooser.showOpenDialog(this);
      datafile = filechooser.getSelectedFile();
      fileTextField.setText(datafile.getAbsolutePath());
    }
  }
  
  /**
   * this is called whenever the editor exits.  the file returned is saved
   * back to its  original location.
   * @param xmlString the xml in string format
   * @param id the id of the file
   * @param location the location of the file
   */
  public void editingCompleted(String xmlString, String id, String location)
  {
    dummydoc = xmlString;
    this.show();
    id = "tmp.npmw.0";
    String displayPath = "";
    
    for(int i=0; i<radioButtons.size(); i++)
    { //figure out which type of file this is related to
      JRadioButton jrb = (JRadioButton)radioButtons.elementAt(i);
      if(jrb.isSelected())
      { 
        String label = jrb.getLabel();
        Hashtable h = (Hashtable)newXMLFileAtts.get(label);
        relatedto = (String)h.get("relatedto");
      }
    }
    
    if(relatedto == null)
    {
      //if the file isn't related to anyting, we just relate it to the package
      relatedto = "PACKAGE";
      relatedtoId = dataPackage.getID();
    }
    
    Enumeration keys = newXMLFileAtts.keys();
    while(keys.hasMoreElements())
    { //get the display path for the type of file that this file is related to
      //so that we can display the name instead of the id in the list in the
      //next panel.
      String key = (String)keys.nextElement();
      Hashtable h = (Hashtable)newXMLFileAtts.get(key);
      String s = (String)h.get("xmlfiletype");
      if(s.equals(relatedto))
      {
        displayPath = (String)h.get("displaypath");
        break;
      }
    }
    
    Hashtable packageFiles = dataPackage.getRelatedFiles();
    relatedFileIds = (Vector)packageFiles.get(relatedto.trim());
    if(relatedFileIds != null && relatedFileIds.size() == 1)
    {
      relatedtoId = (String)relatedFileIds.elementAt(0);
    }
    else if(relatedFileIds != null && relatedFileIds.size() > 1)
    {
      for(int i=0; i<relatedFileIds.size(); i++)
      { //get the name and ids of the files related to this new one
        File f;
        try
        {
          f = PackageUtil.openFile((String)relatedFileIds.elementAt(i), 
                                   framework);
        }
        catch (Exception e)
        {
          ClientFramework.debug(0, "Error in NewPackageMetadataWizard." +
                                "editingComplete(): " + e.getMessage());
          return;
        }
        NodeList nl = PackageUtil.getPathContent(f, displayPath, framework);
       
        for(int j=0; j<nl.getLength(); j++)
        {
          Node n = nl.item(j);
          String name = n.getFirstChild().getNodeValue().trim();
          relatedFiles.put(relatedFileIds.elementAt(i), name);
        }
      }
    }
    else
    {
      relatedFiles = new Hashtable();
    }
    
    
    if(relatedto.equals("DATAFILE") && 
       relatedFileIds != null && 
       !relatedFileIds.contains("DATAFILE"))
    {
      relatedtoId = dataPackage.getID();
    }
    
    FileSystemDataStore fsds = new FileSystemDataStore(framework);
    //get a pointer to the file we just created.
    newxmlFile = fsds.saveTempFile(id, new StringReader(xmlString));
    nextButtonHandler(new ActionEvent(this, 0, ""));
  }
  
  public void editingCanceled(String xmlString, String id, String location)
  { //the user pressed the cancel button on the editor so we go back a frame
    //and let him choose again
    currentScreen--;
    layoutScreen();
    this.show();
  }
  
  public void windowClosed(WindowEvent event)
  {
    if(!finishflag)
    {
      DataPackageGUI dpg = new DataPackageGUI(framework, dataPackage);
      dpg.show();
      dpg.setName("Package Editor:" + dataPackage.getID());
      framework.addWindow(dpg);
    }
    framework.removeWindow(this); 
  }
  public void windowClosing(WindowEvent event)
  {}
  public void windowActivated(WindowEvent event)
  {}
  public void windowDeactivated(WindowEvent event)
  {}
  public void windowIconified(WindowEvent event)
  {}
  public void windowDeiconified(WindowEvent event)
  {}
  public void windowOpened(WindowEvent event)
  {}
}
