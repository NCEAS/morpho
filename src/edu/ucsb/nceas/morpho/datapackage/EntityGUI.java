/**
 *  '$RCSfile: EntityGUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-09-24 16:41:23 $'
 * '$Revision: 1.20 $'
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
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

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

/**
 * Class that implements a GUI to edit an entity
 */
public class EntityGUI extends javax.swing.JFrame 
                       implements ActionListener, 
                                  EditingCompleteListener
{
  private static final String htmlBegin = "<html><p><font color=black>";
  private static final String htmlEnd = "</font></p></html>";
  private Vector namePath;
  private Vector descPath;
  private Vector numrecPath;
  private Vector caseSensPath;
  private Vector orientationPath;
  private Vector attributeNamePath;
  private Vector attributeNameNode;
  
  private ClientFramework framework;
  private ConfigXML config;
  private Container contentPane;
  private DataPackage dataPackage;
  private String entityId;
  private Vector attributes = new Vector();
  private String location;
  private Hashtable attributeHash = new Hashtable();
  private boolean editAttribute = false;
  
  //visual components
  private JLabel name = new JLabel();
  private JLabel description = new JLabel();
  private JLabel numrecords = new JLabel();
  private JLabel caseSensitive = new JLabel();
  private JLabel orientation = new JLabel();
  private JList attributeList = new JList();
  private DataPackageGUI parent;
  
  /**
   * Creates a new entity editor.
   * @param dp the datapackage that the entity belongs to
   * @param id the id of the entity file that we want to edit.
   * @param location the location of this data package
   * @param parent the DataPackageGUI parent of this window.
   * @param cf the clientframework from which this morpho instantiation was
   *        created.
   */
  public EntityGUI(DataPackage dp, String id, String location, 
                   DataPackageGUI parent, ClientFramework cf)
  {
    config = cf.getConfiguration();
    
    namePath = config.get("entityNamePath");
    descPath = config.get("entityDescPath");
    numrecPath = config.get("entityNumrecPath");
    caseSensPath = config.get("entityCaseSensPath");
    orientationPath = config.get("entityOrientationPath");
    attributeNamePath = config.get("attributeNamePath");
    attributeNameNode = config.get("attributeNameNode");
    
    this.parent = parent;
    this.location = location;
    this.framework = cf;
    this.dataPackage = dp;
    this.entityId = id;
    contentPane = getContentPane();
    setTitle("Table Editor");
    BoxLayout box = new BoxLayout(contentPane, BoxLayout.Y_AXIS);
    contentPane.setLayout(box);
    parseEntity();
    initComponents();
    pack();
    setSize(500, 450);
    /* Center the Frame */
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2 ,
            (screenDim.height - frameDim.height) /2);
  }
  
  /**
   * parse the entity file to fill in the entity fields
   */
  private void parseEntity()
  {
    File xmlFile;
    try
    {
      if(location.equals(DataPackage.LOCAL) || 
         location.equals(DataPackage.BOTH))
      {
        FileSystemDataStore fsds = new FileSystemDataStore(framework);
        xmlFile = fsds.openFile(entityId);
      }
      else
      {
        MetacatDataStore mds = new MetacatDataStore(framework);
        xmlFile = mds.openFile(entityId);
      }
    }
    catch(FileNotFoundException fnfe)
    {
      framework.debug(0, "Error reading file : " + entityId + " " + 
                         fnfe.getMessage() + "---File NOT found.");
      return;
    }
    catch(CacheAccessException cae)
    {
      framework.debug(0, "Error reading file : " + entityId + " " + 
                         cae.getMessage() + "---Cache could not be accessed");
      return;
    }
    
    NodeList nameList = PackageUtil.getPathContent(xmlFile, 
                                                   namePath, 
                                                   framework);
    NodeList descList = PackageUtil.getPathContent(xmlFile, 
                                                   descPath, 
                                                   framework);
    NodeList numrecList = PackageUtil.getPathContent(xmlFile, 
                                                     numrecPath, 
                                                     framework);
    NodeList caseSensList = PackageUtil.getPathContent(xmlFile, 
                                                       caseSensPath, 
                                                       framework);
    NodeList orientationList = PackageUtil.getPathContent(xmlFile, 
                                                          orientationPath, 
                                                          framework);
    
    if(nameList.getLength() != 0)
    { 
      String s = nameList.item(0).getFirstChild().getNodeValue();
      name = new JLabel(s);
    }
    if(descList.getLength() != 0)
    {
      String s = descList.item(0).getFirstChild().getNodeValue();
      description = new JLabel(s);
    }
    if(numrecList.getLength() != 0)
    {
      String s = numrecList.item(0).getFirstChild().getNodeValue();
      numrecords = new JLabel(s);
    }
    if(caseSensList.getLength() != 0)
    {
      NamedNodeMap nnm = caseSensList.item(0).getAttributes();
      Node n = nnm.getNamedItem("yesorno");
      String s = n.getFirstChild().getNodeValue();
      caseSensitive = new JLabel(s);
    }
    if(orientationList.getLength() != 0)
    {
      NamedNodeMap nnm = orientationList.item(0).getAttributes();
      Node n = nnm.getNamedItem("columnorrow");
      String s = n.getFirstChild().getNodeValue();
      orientation = new JLabel(s);
    }
  }
  
  private void initComponents()
  {
    JButton editEntityButton = new JButton("Edit Table Description");
    JButton editAttributes = new JButton("Edit Attributes");
    editEntityButton.addActionListener(this);
    editAttributes.addActionListener(this);
    
    //poplulate the attributes list box
    Vector triples = dataPackage.getTriples().getCollectionByObject(entityId);
    
    for(int i=0; i<triples.size(); i++)
    {
      Triple t = (Triple)triples.elementAt(i);
      String id = t.getSubject();
      File f;
      try
      {
        if(location.equals(DataPackage.LOCAL) || 
           location.equals(DataPackage.BOTH))
        {
          FileSystemDataStore fsds = new FileSystemDataStore(framework);
          f = fsds.openFile(id);
        }
        else
        {
          MetacatDataStore mds = new MetacatDataStore(framework);
          f = mds.openFile(id);
        }
      }
      catch(Exception e)
      {
        framework.debug(0, "The attribute file referenced in the package does" +
                           " not exist: EntityGUI.initComponents().: " +
                           e.getMessage());
        return;
      }
      
      /*try
      { //print out the file to be parsed
        FileReader fr = new FileReader(f);
        int c = fr.read();
        while(c != -1)
        {
          System.out.print((char)c);
          c = fr.read();
        }
          
      }
      catch(Exception e)
      {
        System.out.println("Error: " + e.getMessage());
      }*/
      
      NodeList nl = PackageUtil.getPathContent(f, attributeNamePath, framework);
     
      if(nl != null)
      {
        for(int j=0; j<nl.getLength(); j++)
        {
          Node n = nl.item(j);
          String att = n.getFirstChild().getNodeValue();
          attributes.addElement(att.trim());
          attributeHash.put(att.trim(), id);
        }
      }
    }
    
    if(attributes.size() == 0)
    {
     attributes.addElement(" "); 
    }
    attributeList = new JList(attributes);
    attributeList.setSelectedIndex(0);
    attributeList.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if(e.getClickCount() == 2) 
        {
          actionPerformed(new ActionEvent(this, 0, "Edit Attributes"));
        }
      }
    });
    attributeList.setPreferredSize(new Dimension(180, 1000)); 
    //attributeList.setMaximumSize(new Dimension(180, 1000));
    attributeList.setVisibleRowCount(14);
    attributeList.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.black),
                            BorderFactory.createLoweredBevelBorder()));
    
    JLabel nameL = new JLabel("Name");
    JLabel descriptionL = new JLabel("Description");
    JLabel numrecordsL = new JLabel("Number of Records");
    JLabel caseSensitiveL = new JLabel("Case Sensitive");
    JLabel orientationL = new JLabel("Orientation");
    JLabel attributeL = new JLabel("Attributes");
    
    JPanel nameP = new JPanel();
    JPanel descriptionP = new JPanel();
    JPanel numrecordsP = new JPanel();
    JPanel caseSensitiveP = new JPanel();
    JPanel orientationP = new JPanel();
    
    nameP.add(nameL);
    nameP.add(name);
    descriptionP.add(descriptionL);
    descriptionP.add(description);
    numrecordsP.add(descriptionL);
    numrecordsP.add(description);
    caseSensitiveP.add(caseSensitiveL);
    caseSensitiveP.add(caseSensitive);
    orientationP.add(orientationL);
    orientationP.add(orientation);
    
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new FlowLayout());
    
    JPanel entityPanel = new JPanel();
    entityPanel.setLayout(new BoxLayout(entityPanel, BoxLayout.Y_AXIS));
    
    JPanel attributePanel = new JPanel();
    JPanel listandbuttons = new JPanel();
    attributePanel.setLayout(new FlowLayout());
    attributePanel.add(attributeL);
    listandbuttons.add(new JScrollPane(attributeList));
    attributePanel.setPreferredSize(new Dimension(225, 315));
    attributePanel.setBackground(Color.white);
    
    StringBuffer entityInfo = new StringBuffer();
    entityInfo.append("<html><font color=black>");
    entityInfo.append("<b>Name:</b> ").append(name.getText()).append("<br>");
    entityInfo.append("<b>Description:</b> ").append(description.getText());
    entityInfo.append("<br>");
    entityInfo.append("<b>Number Of Records:</b> ").append(numrecords.getText());
    entityInfo.append("<br>");
    entityInfo.append("<b>Case Sensitive:</b> ").append(caseSensitive.getText());
    entityInfo.append("<br>");
    entityInfo.append("<b>Orientation:</b> ").append(orientation.getText());
    entityInfo.append("<br></font></html>");
    
    JLabel entityInfoLabel = new JLabel(entityInfo.toString());
    entityInfoLabel.setPreferredSize(new Dimension(190,1000));
    entityInfoLabel.setForeground(Color.black);
    entityInfoLabel.setAlignmentX(0);
    JPanel entityInfoPanel = new JPanel();
    entityInfoPanel.setLayout(new BoxLayout(entityInfoPanel, BoxLayout.Y_AXIS));
    entityInfoPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    entityInfoPanel.add(entityInfoLabel);
    entityInfoPanel.setBackground(Color.white);
    entityInfoPanel.setAlignmentX(0);
    entityPanel.add(new JScrollPane(entityInfoPanel));
    
    /*
    entityPanel.add(nameL);
    entityPanel.add(name);
    entityPanel.add(new JLabel(" "));
    entityPanel.add(descriptionL);
    entityPanel.add(description);
    entityPanel.add(new JLabel(" "));
    entityPanel.add(numrecordsL);
    entityPanel.add(numrecords);
    entityPanel.add(new JLabel(" "));
    entityPanel.add(caseSensitiveL);
    entityPanel.add(caseSensitive);
    entityPanel.add(orientationL);
    entityPanel.add(orientation);
    entityPanel.add(new JLabel(" "));
    */
    entityPanel.add(editEntityButton);
    entityPanel.setPreferredSize(new Dimension(225, 280));
    entityPanel.setBackground(Color.white);
    
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(editAttributes);
    buttonPanel.setBackground(Color.white);
    listandbuttons.add(new JLabel(" "));
    listandbuttons.add(buttonPanel);
    listandbuttons.setLayout(new BoxLayout(listandbuttons, BoxLayout.Y_AXIS));
    listandbuttons.setBackground(Color.white);
    attributePanel.add(listandbuttons);
    
    JPanel entattPanel = new JPanel();
    entattPanel.add(entityPanel);
    entattPanel.add(attributePanel);
    entattPanel.setBorder(BorderFactory.createLoweredBevelBorder());
    entattPanel.setBackground(Color.white);
    
    mainPanel.add(createHeadPanel());
    mainPanel.add(entattPanel);
    mainPanel.add(editEntityButton);
    mainPanel.add(editAttributes);
    
    contentPane.add(mainPanel);
  }
  
  private JPanel createHeadPanel()
  {
    ImageIcon head = new ImageIcon(
                         framework.getClass().getResource("smallheader-bg.gif"));
    ImageIcon logoIcon = 
              new ImageIcon(framework.getClass().getResource("logo-icon.gif"));
    JLabel logoLabel = new JLabel();
    JLabel headLabel = new JLabel("Table Description");
    logoLabel.setIcon(logoIcon);
    headLabel.setIcon(head);
    headLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    headLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headLabel.setAlignmentY(Component.LEFT_ALIGNMENT);
    headLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    headLabel.setForeground(Color.black);
    headLabel.setFont(new Font("Dialog", Font.BOLD, 14));
    headLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    
    JPanel headPanel = new JPanel();
    headPanel.add(logoLabel);
    headPanel.add(headLabel);
    headPanel.setLayout(new BoxLayout(headPanel, BoxLayout.X_AXIS));
    return headPanel;
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
    ClientFramework.debug(11, "Editing complete: id: " + id + " location: " + 
                              location);
    AccessionNumber a = new AccessionNumber(framework);
    boolean metacatpublic = false;
    boolean newfile = false;
    boolean metacatloc = false;
    boolean localloc = false;
    boolean bothloc = false;
    FileSystemDataStore fsds = new FileSystemDataStore(framework);
    
    if(id == null)
    { //this is a new file so we need to assign it an id and put a triple
      //in the triples file
      id = a.getNextId();
      location = dataPackage.getLocation();
      //create the triple
      Triple t = new Triple(id, "isRelatedTo", entityId);
      TripleCollection tc = new TripleCollection();
      tc.addTriple(t);
      String packageFile = PackageUtil.addTriplesToTriplesFile(tc, 
                                                               dataPackage, 
                                                               framework);
      String packageId = a.incRev(dataPackage.getID());
      File f = fsds.saveTempFile("tmp.0", new StringReader(packageFile));
      packageFile = a.incRevInTriples(f, dataPackage.getID(), packageId);
      
      if(location.equals(DataPackage.BOTH))
      {
        metacatloc = true;
        localloc = true;
      }
      else if(location.equals(DataPackage.METACAT))
      {
        metacatloc = true;
      }
      else if(location.equals(DataPackage.LOCAL))
      {
        localloc = true;
      }
      
      if(metacatloc)
      { //save the files to metacat
        try
        {
          MetacatDataStore mds = new MetacatDataStore(framework);
          int choice = JOptionPane.showConfirmDialog(null, 
                               "Do you wish to make this file publicly readable "+ 
                               "(Searchable) on Metacat?", 
                               "Package Editor", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
          if(choice == JOptionPane.YES_OPTION)
          {
            metacatpublic = true;
          }
          mds.newFile(id, new StringReader(xmlString), metacatpublic);
          mds.saveFile(packageId, new StringReader(packageFile), metacatpublic);
        }
        catch(Exception e)
        {
          framework.debug(0, "Error saving to metacat: " + e.getMessage());
          e.printStackTrace();
        }
      }
      
      if(localloc)
      { //save the files locally
        try
        {
          fsds.newFile(id, new StringReader(xmlString), false);
          fsds.saveFile(packageId, new StringReader(packageFile), false);
        }
        catch(Exception e)
        {
          framework.debug(0, "Error saving package locally: " + e.getMessage());
          e.printStackTrace();
        }
      }
      
      DataPackage newPackage = new DataPackage(location, packageId, null,
                                               framework);
      this.dispose();
      parent.dispose();
      
      DataPackageGUI newgui = new DataPackageGUI(framework, newPackage);
      EntityGUI newEntitygui;
      newEntitygui = new EntityGUI(newPackage, entityId, location, newgui, 
                                   framework);
      newgui.show();
      newEntitygui.show();
      return;
    }
    
    String newid = "";
    String newPackageId = "";
    if(location.equals(DataPackage.BOTH))
    {
      metacatloc = true;
      localloc = true;
    }
    else if(location.equals(DataPackage.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackage.LOCAL))
    {
      localloc = true;
    }
    
    MetacatDataStore mds = new MetacatDataStore(framework);
    
    try
    {
      if(metacatloc)
      { //save it to metacat
        int choice = JOptionPane.showConfirmDialog(null, 
                               "Do you wish to make this file publicly readable "+ 
                               "(Searchable) on Metacat?", 
                               "Package Editor", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
        if(choice == JOptionPane.YES_OPTION)
        {
          metacatpublic = true;
        }
        if(id.trim().equals(dataPackage.getID().trim()))
        { //edit the package file
          String oldid = id;
          newid = a.incRev(id);
          File f = fsds.saveTempFile(oldid, new StringReader(xmlString));
          String newPackageFile = a.incRevInTriples(f, oldid, newid);
          mds.saveFile(newid, new StringReader(newPackageFile), metacatpublic);
          newPackageId = newid;
        }
        else
        { //edit another file in the package
          String oldid = id;
          newid = a.incRev(id);
          //save the file that was edited
          mds.saveFile(newid, new StringReader(xmlString), metacatpublic);
          newPackageId = a.incRev(dataPackage.getID());
          String newPackageFile = a.incRevInTriples(dataPackage.getTriplesFile(),
                                                    oldid,
                                                    newid);
          File tempPackageFile = fsds.saveTempFile("tmp.0.1", 
                                             new StringReader(newPackageFile));
          newPackageFile = a.incRevInTriples(tempPackageFile, 
                                             dataPackage.getID(), 
                                             newPackageId);
          mds.saveFile(newPackageId, new StringReader(newPackageFile), 
                       metacatpublic);
        }
      }
    }
    catch(Exception e)
    {
      framework.debug(0, "Error saving file to metacat"+ id + " to " + location +
                         "--message: " + e.getMessage());
      framework.debug(11, "File: " + xmlString);
      e.printStackTrace();
    }
    
    try
    { 
      if(localloc)
      { //save the file locally
        if(id.trim().equals(dataPackage.getID().trim()))
        { //we just edited the package file itself
          String oldid = id;
          newid = a.incRev(id);
          File f = fsds.saveTempFile(oldid, new StringReader(xmlString));
          String newPackageFile = a.incRevInTriples(f, oldid, newid);
          fsds.saveFile(newid, new StringReader(newPackageFile), false);
          newPackageId = newid;
        }
        else
        { //we edited a file in the package
          String oldid = id;
          newid = a.incRev(id);
          fsds.saveFile(newid, new StringReader(xmlString), false);
          newPackageId = a.incRev(dataPackage.getID());
          String newPackageFile = a.incRevInTriples(dataPackage.getTriplesFile(), 
                                                    oldid, 
                                                    newid);
          File tempPackageFile = fsds.saveTempFile("tmp.0.1", 
                                             new StringReader(newPackageFile));
          newPackageFile = a.incRevInTriples(tempPackageFile, 
                                             dataPackage.getID(), 
                                             newPackageId);
          fsds.saveFile(newPackageId, new StringReader(newPackageFile), false); 
        }
      }
      
      DataPackage newPackage = new DataPackage(location, newPackageId, null,
                                                 framework);
      this.dispose();
      parent.dispose();
      
      DataPackageGUI newgui = new DataPackageGUI(framework, newPackage);
      EntityGUI newEntitygui;
      DataPackage newDataPackage = new DataPackage(location, 
                                           a.incRev(dataPackage.getID()),
                                           null, framework);
      if(editAttribute)
      {
        newEntitygui = new EntityGUI(newDataPackage, entityId, location, newgui, 
                                     framework);
      }
      else
      {
        newEntitygui = new EntityGUI(newDataPackage, a.incRev(entityId), 
                                     location, newgui, framework);
      }
      newgui.show();
      newEntitygui.show();
    }
    catch(Exception e)
    {
      framework.debug(0, "Error saving file locally"+ id + " to " + location +
                         "--message: " + e.getMessage());
      framework.debug(11, "File: " + xmlString);
      e.printStackTrace();
    }
  }
  
  public void editingCanceled(String xmlString, String id, String location)
  { //do nothing
  }
  
  /**
   * Handles actions from all components in the Container  
   */
  public void actionPerformed(ActionEvent e) 
  {
    String command = e.getActionCommand();
    framework.debug(20, "Action fired: " + command);
    
    if(command.equals("Edit Table Description"))
    {
      editAttribute = false;
      EditorInterface editor;
      try
      {
        ServiceProvider provider = 
                        framework.getServiceProvider(EditorInterface.class);
        editor = (EditorInterface)provider;
      }
      catch(Exception ee)
      {
        framework.debug(0, "Error acquiring editor plugin: " + ee.getMessage());
        ee.printStackTrace();
        return;
      }
      
      File xmlFile;
      try
      {
        if(location.equals(DataPackage.LOCAL) || 
           location.equals(DataPackage.BOTH))
        {
          FileSystemDataStore fsds = new FileSystemDataStore(framework);
          xmlFile = fsds.openFile(entityId);
        }
        else
        {
          MetacatDataStore mds = new MetacatDataStore(framework);
          xmlFile = mds.openFile(entityId);
        }
      }
      catch(FileNotFoundException fnfe)
      {
        framework.debug(0, "Error reading file : " + entityId + " " + 
                           fnfe.getMessage() + "---File NOT found.");
        return;
      }
      catch(CacheAccessException cae)
      {
        framework.debug(0, "Error reading file : " + entityId + " " + 
                           cae.getMessage() + "---Cache could not be accessed");
        return;
      }
      
      StringBuffer sb = new StringBuffer();
      try
      {
        FileReader fr = new FileReader(xmlFile);
        int c = fr.read();
        while(fr.ready() && c != -1)
        {
          sb.append((char)c);
          c = fr.read();
        }
        sb.append((char)c);
        fr.close();
      }
      catch(Exception eeeee)
      {
        framework.debug(0, "Error reading file : " + entityId + " " + 
                           eeeee.getMessage());
        return;
      }
      editor.openEditor(sb.toString(), entityId, location, this);
    }
    else if(command.equals("Edit Attributes"))
    {
      editAttribute = true;
      String selectedItem = (String)attributeList.getSelectedValue();
      File f;
      String id = (String)attributeHash.get(selectedItem);
      if(id == null)
      {
        /*ClientFramework.debug(0, "You must select an attribute to edit." +
                              " If there are no attributes in the list, " +
                              "you must first add a field description " +
                              "using the 'add' button in the Package " +
                              "Editor.");
        return;*/
        //create a new variable file
        Hashtable fileatts = PackageUtil.getConfigFileTypeAttributes(framework, 
                                                                "xmlfiletype");
        String attributedoctype = config.get("attributedoctype", 0);
        Hashtable attributeAtts = (Hashtable)fileatts.get(attributedoctype);
        String root = (String)attributeAtts.get("rootnode");
        String dummydoc = "<?xml version=\"1.0\"?>\n";
        dummydoc += "<!DOCTYPE " + root + " PUBLIC \"" + attributedoctype + 
                    "\" \"" + root + ".dtd\">\n";
        dummydoc += "<" + root + ">" + "</" + root + ">";
        EditorInterface editor = PackageUtil.getEditor(framework);
        editor.openEditor(dummydoc, null, null, this);
        return;
      }
      
      try
      {
        f = PackageUtil.openFile(id, location, framework);
      }
      catch(FileNotFoundException fnfe)
      {
        framework.debug(0, "The attribute file was not found in EntityGUI." + 
                           "actionPerformed(): " + fnfe.getMessage());
        return;
      }
      catch(CacheAccessException cae)
      {
        framework.debug(0, "Cannot access the cache in EntityGUI." + 
                           "actionPerformed(): " + cae.getMessage());
        return;
      }
      String s = PackageUtil.getStringFromFile(f);
      EditorInterface editor = PackageUtil.getEditor(framework);
      editor.openEditor(s, id, location, (String)attributeNameNode.elementAt(0), 
                        selectedItem, this);
    }
  }
}
