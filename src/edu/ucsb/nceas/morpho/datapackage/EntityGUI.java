/**
 *  '$RCSfile: EntityGUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-26 22:38:51 $'
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
 * Class that implements a GUI to edit a data package
 */
public class EntityGUI extends javax.swing.JFrame 
                       implements ActionListener, 
                                  EditingCompleteListener
{
  private static final String htmlBegin = "<html><p><font color=black>";
  private static final String htmlEnd = "</font></p></html>";
  private static final String namePath = "//entityName";
  private static final String descPath = "//entityDescription";
  private static final String numrecPath = "//numberOfRecords";
  private static final String caseSensPath = "//caseSensitive";
  private static final String orientationPath = "//orientation";
  
  private ClientFramework framework;
  private Container contentPane;
  private DataPackage dataPackage;
  private String entityId;
  private Vector attributes = new Vector();
  private String location;
  
  //visual components
  private JLabel name = new JLabel(htmlBegin + "xml_documents" + htmlEnd);
  private JLabel description = new JLabel(htmlBegin + "Some entity that is neat and has a very very very very very long description" + htmlEnd);
  private JLabel numrecords = new JLabel(htmlBegin + "1232123" + htmlEnd);
  private JLabel caseSensitive = new JLabel(htmlBegin + "yes" + htmlEnd);
  private JLabel orientation = new JLabel(htmlBegin + "column" + htmlEnd);
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
      name = new JLabel(htmlBegin + s + htmlEnd);
    }
    if(descList.getLength() != 0)
    {
      String s = descList.item(0).getFirstChild().getNodeValue();
      description = new JLabel(htmlBegin + s + htmlEnd);
    }
    if(numrecList.getLength() != 0)
    {
      String s = numrecList.item(0).getFirstChild().getNodeValue();
      numrecords = new JLabel(htmlBegin + s + htmlEnd);
    }
    if(caseSensList.getLength() != 0)
    {
      NamedNodeMap nnm = caseSensList.item(0).getAttributes();
      Node n = nnm.getNamedItem("yesorno");
      String s = n.getFirstChild().getNodeValue();
      caseSensitive = new JLabel(htmlBegin + s + htmlEnd);
    }
    if(orientationList.getLength() != 0)
    {
      NamedNodeMap nnm = orientationList.item(0).getAttributes();
      Node n = nnm.getNamedItem("columnorrow");
      String s = n.getFirstChild().getNodeValue();
      orientation = new JLabel(htmlBegin + s + htmlEnd);
    }
  }
  
  private void initComponents()
  {
    String attributeNamePath = "/eml-variable/variable/variable_name";
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
      
      NodeList nl = PackageUtil.getPathContent(f, attributeNamePath, framework);
      
      for(int j=0; j<nl.getLength(); j++)
      {
        Node n = nl.item(j);
        String att = n.getFirstChild().getNodeValue();
        attributes.addElement(att.trim());
      }
    }
    
    attributeList = new JList(attributes);
    attributeList.setPreferredSize(new Dimension(180, 215)); 
    attributeList.setMaximumSize(new Dimension(180, 215));
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
    listandbuttons.add(attributeList);
    attributePanel.setPreferredSize(new Dimension(225, 315));
    attributePanel.setBackground(Color.white);
    
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
    entityPanel.add(new JLabel(" "));
    entityPanel.add(orientationL);
    entityPanel.add(orientation);
    entityPanel.add(new JLabel(" "));
    entityPanel.add(new JLabel(" "));
    entityPanel.add(editEntityButton);
    entityPanel.setPreferredSize(new Dimension(225, 280));
    entityPanel.setBackground(Color.white);
    
    JButton edit = new JButton("Edit Attributes");
    edit.addActionListener(this);
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(edit);
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
    //mainPanel.add(attributePanel);
    
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
    FileSystemDataStore fsds = new FileSystemDataStore(framework);
    //System.out.println(xmlString);
  
    boolean metacatloc = false;
    boolean localloc = false;
    boolean bothloc = false;
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
    
    try
    {
      if(metacatloc)
      { //save it to metacat
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
          mds.saveFile(newid, new StringReader(xmlString), metacatpublic);
          newPackageId = a.incRev(dataPackage.getID());
          String newPackageFile = a.incRevInTriples(dataPackage.getTriplesFile(),
                                                    oldid,
                                                    newid);
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
          fsds.saveFile(newPackageId, new StringReader(newPackageFile), false); 
        }
      }
      
      DataPackage newPackage = new DataPackage(location, newPackageId, null,
                                                 framework);
      this.dispose();
      parent.dispose();
      
      DataPackageGUI newgui = new DataPackageGUI(framework, newPackage);
      EntityGUI newEntitygui = new EntityGUI(dataPackage, a.incRev(entityId), location, 
                                             newgui, framework);
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
  
  /**
   * Handles actions from all components in the Container  
   */
  public void actionPerformed(ActionEvent e) 
  {
    String command = e.getActionCommand();
    framework.debug(20, "Action fired: " + command);
    
    if(command.equals("Edit Table Description"))
    {
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
      
    }
  }
}
