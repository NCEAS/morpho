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
 *     '$Date: 2001-06-04 21:26:44 $'
 * '$Revision: 1.8 $'
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
  private JCheckBox saveToMetacatCheckBox;
  private JCheckBox publicAccessCheckBox;
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
      saveToMetacatCheckBox = new JCheckBox("Save package to Metacat?", true);
      publicAccessCheckBox = new JCheckBox("Package should be publicly " +
                                           "readable?", true);
      //saveToMetacatButton = new JButton("Save To Metacat");
      JList idlist = new JList(listContent);
      idlist.setPreferredSize(new Dimension(100,100));
      JLabel listLabel = new JLabel("You are creating the following files: ");
      donePanel.setLayout(new BoxLayout(donePanel, BoxLayout.Y_AXIS));
      donePanel.add(Box.createRigidArea(new Dimension(0,90)));
      donePanel.add(Box.createVerticalGlue());
      donePanel.add(listLabel);
      donePanel.add(new JScrollPane(idlist));
      donePanel.add(Box.createRigidArea(new Dimension(0,20)));
      donePanel.add(saveToMetacatCheckBox);
      donePanel.add(Box.createRigidArea(new Dimension(0,20)));
      donePanel.add(publicAccessCheckBox);
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
   * Handles the action when the user presses the 'finish' button
   */
  private void handleFinishAction()
  { //write out all of the files to their proper homes with proper ids
    //add the triples to the triples file
    //open the new package in the package editor if the check box is true
    Vector packageFiles = new Vector();
    String triplesTag = "/triples";
    for(int i=0; i<frameWizards.size(); i++)
    { //create the triplesCollection
      //put in the name of the module as a place holder.  it will be exchanged
      //for the id in the next for loop
      WizardFrameContainer wfc = (WizardFrameContainer)frameWizards.elementAt(i);
      if(wfc.attributes.containsKey("relatedTo"))
      {
        String relation = (String) wfc.attributes.get("relatedTo");
        String name = (String) wfc.attributes.get("name");
        triples.addTriple(new Triple(name, "isRelatedTo", relation));
      }
      if(wfc.attributes.containsKey("triplestag"))
      {
        triplesTag = (String)wfc.attributes.get("triplestag");
      }
    }
    
    Vector triplesV = triples.getCollection();
    
    for(int i=0; i<frameWizards.size(); i++)
    { //save all of the files to their new home
      WizardFrameContainer wfc = (WizardFrameContainer)
                                  frameWizards.elementAt(i);
      
      File f = wfc.getFile(false);
      if(f == null)
      {
        return;
      }
      Vector fileVec = new Vector();
      fileVec.addElement(wfc.id);
      fileVec.addElement(f);
      fileVec.addElement(wfc.type);
      packageFiles.addElement(fileVec);
      
      String name = (String)wfc.attributes.get("name");
      String id = (String)wfc.id;
      
      for(int j=0; j<triplesV.size(); j++)
      { //replace the names of the modules with the ids of the instantiations
        Triple t = (Triple)triplesV.elementAt(j);
        String sub = t.getSubject();
        String obj = t.getObject();
        if(sub.equals(name))
        { 
          t.setSubject(id);
        }
        else if(obj.equals(name))
        {
          t.setObject(id);
        }
        else if(sub.equals("DATAFILE"))
        { //the keyword 'DATAFILE' is how the user can tell the wizard to 
          //associate metadata with the data file itself.
          if(!wfc.type.equals("WIZARD"))
          {
            t.setSubject(id);
          }
        }
        else if(obj.equals("DATAFILE"))
        {
          if(!wfc.type.equals("WIZARD"))
          {
            t.setObject(id);
          }
        }
      }
    }
    
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
        Vector fileVec = new Vector();
        fileVec.addElement(wfc.id);
        fileVec.addElement(f);
        fileVec.addElement(wfc.type);
        packageFiles.addElement(fileVec);
        
        DOMParser parser = new DOMParser();
        InputSource in;
        FileInputStream fs;
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
          parser.parse(in);
          fs.close();
        }
        catch(Exception e1)
        {
          System.err.println("File: " + f.getPath() + " : parse threw: " + 
                             e1.toString());
        }
        //get the DOM rep of the document without triples
        doc = parser.getDocument();
        NodeList tripleNodeList = triples.getNodeList();
        NodeList docTriplesNodeList = null;
        
        try
        {
          //find where the triples go in the file
          docTriplesNodeList = XPathAPI.selectNodeList(doc, triplesTag); 
        }
        catch(SAXException se)
        {
          System.err.println("file: " + f.getPath() + " : parse threw: " + 
                             se.toString());
        }
        
        Node docNode = doc.getDocumentElement();
        for(int j=0; j<tripleNodeList.getLength(); j++)
        { //add the triples to the appropriate position in the file
          Node n = doc.importNode(tripleNodeList.item(j), true);
          Node triplesNode = docTriplesNodeList.item(0);
          triplesNode.appendChild(n);
        }
        
        //write out the tiples file
        StringReader sr = new StringReader(print(doc.getDocumentElement()));
        FileSystemDataStore localDataStore = new FileSystemDataStore(framework);
        localDataStore.saveFile(wfc.id, sr, false); //write out the file
        //framework.debug(9, "saving file: " + wfc.id);
        //System.out.println("xml with triples: " + 
        //                      print(doc.getDocumentElement()));
      }
    }
    
    if(openCheckBox.isSelected())
    {
      //open the package in the package editor
      System.out.println("opening the package in the package editor (not really");
    }
    
    if(saveToMetacatCheckBox.isSelected())
    {
      //save the package to metacat here
      System.out.println("saving the package to metacat (not really)");
      /*
      for(int i=0; i<packageFiles.size(); i++)
      {
        MetacatDataStore mds = new MetacatDataStore(framework);
        Vector v = (Vector)packageFiles.elementAt(i);
        String id = (String)v.elementAt(0);
        File f = (File)v.elementAt(1);
        String type = (String)v.elementAt(2);
        System.out.println("id: " + id + " type: " + type);
        
        FileReader fr;
        try
        {
          fr = new FileReader(f);
          boolean publicAcc = false;
          if(publicAccessCheckBox.isSelected())
          {
            publicAcc = true;
          }
          if(!type.equals("WIZARD"))
          { //this is a data file.  we need to figure out how to handle this
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          }
          else
          { //this is an xml file
            mds.newFile(id, fr, publicAcc);
          }
          fr.close();
        }
        catch(FileNotFoundException fnfe)
        {
          framework.debug(0, "Your package is hosed.  A file was not found.");
          return;
        }
        catch(MetacatUploadException mue)
        {
          framework.debug(0, "Metacat is broken. You can't upload your " +
                             "package now.");
          return;
        }
        catch(IOException ioe)
        {
          framework.debug(0, "IO Error in packagewizardshell");
          return;
        }
      }
      */
    }
    
    //make the package wizard go away
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
  
  /**
   * This method can 'print' any DOM subtree. Specifically it is
   * set (by means of 'out') to write the in-memory DOM to the
   * same XML file that was originally read. Action thus saves
   * a new version of the XML doc
   * 
   * @param node node usually set to the 'doc' node for complete XML file
   * re-write
   */
  public String print(Node node)
  {
    StringBuffer sb = new StringBuffer();
    // is there anything to do?
    if (node == null)
    {
      return null;
    }

    int type = node.getNodeType();
    switch (type)
    {
      // print document
    case Node.DOCUMENT_NODE:
    {

      sb.append("<?xml version=\"1.0\"?>");
      print(((Document) node).getDocumentElement());
      //sb.flush();
      break;
    }

      // print element with attributes
    case Node.ELEMENT_NODE:
    {
      sb.append('<');
      sb.append(node.getNodeName());
      Attr attrs[] = sortAttributes(node.getAttributes());
      for (int i = 0; i < attrs.length; i++)
      {
        Attr attr = attrs[i];
        sb.append(' ');
        sb.append(attr.getNodeName());
        sb.append("=\"");
        sb.append(normalize(attr.getNodeValue()));
        sb.append('"');
      }
      sb.append('>');
      NodeList children = node.getChildNodes();
      if (children != null)
      {
        int len = children.getLength();
        for (int i = 0; i < len; i++)
        {
          sb.append(print(children.item(i)));
        }
      }
      break;
    }

      // handle entity reference nodes
    case Node.ENTITY_REFERENCE_NODE:
    {
      sb.append('&');
      sb.append(node.getNodeName());
      sb.append(';');

      break;
    }

      // print cdata sections
    case Node.CDATA_SECTION_NODE:
    {
      sb.append("<![CDATA[");
      sb.append(node.getNodeValue());
      sb.append("]]>");

      break;
    }

      // print text
    case Node.TEXT_NODE:
    {
      sb.append(normalize(node.getNodeValue()));
      break;
    }

      // print processing instruction
    case Node.PROCESSING_INSTRUCTION_NODE:
    {
      sb.append("<?");
      sb.append(node.getNodeName());
      String data = node.getNodeValue();
      if (data != null && data.length() > 0)
      {
        sb.append(' ');
        sb.append(data);
      }
      sb.append("?>");
      break;
    }
    }

    if (type == Node.ELEMENT_NODE)
    {
      sb.append("</");
      sb.append(node.getNodeName());
      sb.append('>');
    }

    //sb.flush();
    return sb.toString();
  }
  
  /** Returns a sorted list of attributes. */
  protected Attr[] sortAttributes(NamedNodeMap attrs)
  {

    int len = (attrs != null) ? attrs.getLength() : 0;
    Attr array[] = new Attr[len];
    for (int i = 0; i < len; i++)
    {
      array[i] = (Attr) attrs.item(i);
    }
    for (int i = 0; i < len - 1; i++)
    {
      String name = array[i].getNodeName();
      int index = i;
      for (int j = i + 1; j < len; j++)
      {
        String curName = array[j].getNodeName();
        if (curName.compareTo(name) < 0)
        {
          name = curName;
          index = j;
        }
      }
      if (index != i)
      {
        Attr temp = array[i];
        array[i] = array[index];
        array[index] = temp;
      }
    }

    return (array);

  } // sortAttributes(NamedNodeMap):Attr[]

  /** Normalizes the given string. */
  protected String normalize(String s)
  {
    StringBuffer str = new StringBuffer();

    int len = (s != null) ? s.length() : 0;
    for (int i = 0; i < len; i++)
    {
      char ch = s.charAt(i);
      switch (ch)
      {
      case '<':
      {
        str.append("&lt;");
        break;
      }
        case '>':
      {
        str.append("&gt;");
        break;
      }
      case '&':
      {
        str.append("&amp;");
        break;
      }
      case '"':
      {
        str.append("&quot;");
        break;
      }
      case '\r':
      case '\n':
      {
        // else, default append char
      }
      default:
      {
        str.append(ch);
      }
      }
    }

    return (str.toString());

  } // normalize(String):String
  
  /**
   * Test method
   */
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
