/**
 *  '$RCSfile: DataPackageGUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-07-02 17:04:09 $'
 * '$Revision: 1.34 $'
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
public class DataPackageGUI extends javax.swing.JFrame 
                            implements ActionListener, 
                                       EditingCompleteListener
{
  private ClientFramework framework;
  private ConfigXML config;
  Container contentPane;
  private DataPackage dataPackage;
  private JList otherFileList;
  private JList entityFileList;
  private JList dataFileList;
  private String location = null;
  private String id = null;
  private JButton editBaseInfoButton = new JButton();
  private Hashtable listValueHash = new Hashtable();
  
  public DataPackageGUI(ClientFramework framework, DataPackage dp)
  {
    this.location = dp.getLocation();
    this.id = dp.getID();
    this.dataPackage = dp;
    this.framework = framework;
    this.config = framework.getConfiguration();
    contentPane = getContentPane();
    setTitle("Data Package Editor");
    BoxLayout box = new BoxLayout(contentPane, BoxLayout.Y_AXIS);
    contentPane.setLayout(box);
    initComponents();
    pack();
    setSize(500, 550);
  }
  
  /**
   * Creates the panels and hands off tasks to other methods
   */
  private void initComponents()
  {
    contentPane.setLayout(new FlowLayout());
    Vector orig = new Vector();
    String title = "No Title Provided";
    String altTitle = "No Alternate Title Provided";
    Hashtable docAtts = dataPackage.getAttributes();
    
    String entitytype = config.get("entitydoctype", 0);
    String resourcetype = config.get("resourcedoctype", 0);
    String attributetype = config.get("attributedoctype", 0);
    
    if(docAtts.containsKey("originator"))
    {
      orig = (Vector)docAtts.get("originator");
    }
    
    if(docAtts.containsKey("title"))
    {
      Vector v = (Vector)docAtts.get("title");
      if(v.size() != 0)
      {
        title = (String)v.elementAt(0);
      }
    }
    
    if(docAtts.containsKey("altTitle"))
    {
      Vector v = (Vector)docAtts.get("altTitle");
      if(v.size() != 0)
      {
        altTitle = (String)v.elementAt(0);
      }
    }
    
    JPanel basicInfoPanel = new JPanel();
    //create the top panel with the package basic info
    basicInfoPanel = createBasicInfoPanel(dataPackage.getIdentifier(), 
                                                 title, 
                                                 altTitle, orig);
                                                 
    Hashtable relfiles = dataPackage.getRelatedFiles();
    Vector otheritems = new Vector();
    Vector dataitems = new Vector();
    Vector entityitems = new Vector();
    Enumeration keys = relfiles.keys();
    while(keys.hasMoreElements()) 
    { //populate the list box vectors
      String key = (String)keys.nextElement();
      if(key.equals("Data File"))
      {
        Vector v = (Vector)relfiles.get(key);
        for(int i=0; i<v.size(); i++)
        {
          String eleid = (String)v.elementAt(i);
          String s = key + " (" + eleid + ")";
          dataitems.addElement(s);
        }
      }
      else if(key.equals(entitytype))
      {
        Vector v = (Vector)relfiles.get(key);
        String spacecount = "";
        for(int i=0; i<v.size(); i++)
        {
          String eleid = (String)v.elementAt(i);
          //String s = "Entity File (" + eleid + ")";
          File xmlfile;
          try
          {
            if(dataPackage.getLocation().equals(DataPackage.METACAT))
            {
              MetacatDataStore mds = new MetacatDataStore(framework);
              xmlfile = mds.openFile(eleid);
            }
            else
            {
              FileSystemDataStore fsds = new FileSystemDataStore(framework);
              xmlfile = fsds.openFile(eleid);
            }
          }
          catch(FileNotFoundException fnfe)
          {
            framework.debug(0, "The file specified was not found.");
            return;
          }
          catch(CacheAccessException cae)
          {
            framework.debug(0, "You do not have proper permissions to write" +
                               " to the cache.");
            return;
          }
          NodeList nl = PackageUtil.getPathContent(xmlfile, "//entityName", 
                                                   framework);
          //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          //note that the path here is hardwired to the entityName tag
          //this is just because I can't figure out a good way to get it
          //from the config file at this point.  this should be changed
          //for the release!
          //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          Node n = nl.item(0);
          String s = n.getFirstChild().getNodeValue().trim();
          spacecount += " ";
          entityitems.addElement(s + spacecount);
          listValueHash.put(s + spacecount, eleid);
        }
      }
      else if(!key.equals(resourcetype) && !key.equals(attributetype))
      {
        Vector v = (Vector)relfiles.get(key);
        for(int i=0; i<v.size(); i++)
        {
          String eleid = (String)v.elementAt(i);
          String s = key + " (" + eleid + ")";
          otheritems.addElement(s);
        }
      }
    }
    
    //if you don't add these spaces, the sizes of the list boxes get all
    //messed up.
    if(otheritems.size()==0)
    {
      otheritems.add(" ");
    }
    if(entityitems.size()==0)
    {
      entityitems.add(" ");
    }
    
    //create the banner panel
    ImageIcon head = new ImageIcon(
                         framework.getClass().getResource("smallheader-bg.gif"));
    ImageIcon logoIcon = 
              new ImageIcon(framework.getClass().getResource("logo-icon.gif"));
    JLabel logoLabel = new JLabel();
    JLabel headLabel = new JLabel("Package Editor");
    logoLabel.setIcon(logoIcon);
    headLabel.setIcon(head);
    headLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    headLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headLabel.setAlignmentY(Component.LEFT_ALIGNMENT);
    headLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    headLabel.setForeground(Color.black);
    headLabel.setFont(new Font("Dialog", Font.BOLD, 14));
    headLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    JPanel toppanel = new JPanel();
    toppanel.setLayout(new BoxLayout(toppanel, BoxLayout.X_AXIS));
    toppanel.add(logoLabel);
    toppanel.add(headLabel);
    
    JPanel listPanel = createListPanel(entityitems, otheritems);
    JPanel layoutPanel = new JPanel();
    layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
    layoutPanel.setPreferredSize(new Dimension(450, 500));
    layoutPanel.setMinimumSize(new Dimension(450, 500));
    
    layoutPanel.add(toppanel);      
    //JPanel layoutpanel2 = new JPanel();
    //layoutpanel2.add(basicInfoPanel);
    //layoutpanel2.add(listPanel);
    layoutPanel.add(basicInfoPanel);
    layoutPanel.add(listPanel);
    //layoutpanel2.setBorder(BorderFactory.createLineBorder(Color.black));
    //layoutPanel.add(layoutpanel2);
    contentPane.add(layoutPanel);
  }
  
  /**
   * creates the basicinfopanel
   */
  private JPanel createBasicInfoPanel(String identifier, String title, 
                                      String altTitle, Vector originator)
  {
    JPanel textpanel = new JPanel();
    textpanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLoweredBevelBorder(),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    textpanel.setLayout(new BoxLayout(textpanel, BoxLayout.Y_AXIS));
    textpanel.setBackground(Color.white);
    editBaseInfoButton = new JButton("Edit Basic Information");
    editBaseInfoButton.addActionListener(this);
    JPanel panel = new JPanel();
    JLabel identifierL = new JLabel("Identifier: ");
    JLabel titleL = new JLabel("Title: ");
    JLabel altTitleL = new JLabel("Short Title: ");
    JLabel originatorL = new JLabel("Data Originator: ");
    String htmlBegin = "<html><p><font color=black>";
    String htmlEnd = "</font></p></html>";
    
    JPanel titleTempPanel = new JPanel();
    titleTempPanel.setLayout(new BoxLayout(titleTempPanel, BoxLayout.X_AXIS));
    titleTempPanel.setBackground(Color.white);
    titleTempPanel.add(titleL);
    JLabel titleLabel = new JLabel(title);
    JTextArea titleArea = new JTextArea(title);
    
    titleArea.setWrapStyleWord(true);
    titleArea.setLineWrap(true);
    titleArea.setRows(1);
    titleArea.setColumns(60);
    titleArea.setBorder(null);
    titleArea.setEditable(false);
    titleArea.setFont(new Font("Times", Font.PLAIN, 13));
    JScrollPane jsp = new JScrollPane(titleArea);
    jsp.setMinimumSize(new Dimension(250, 25));
    jsp.setMaximumSize(new Dimension(600, 40));
    jsp.setBorder(new EmptyBorder(0,0,0,0));
    titleTempPanel.setPreferredSize(new Dimension(125,75));
    titleTempPanel.setMinimumSize(new Dimension(125,75));
    titleTempPanel.add(jsp);
    textpanel.add(Box.createRigidArea(new Dimension(0,5)));
    textpanel.add(titleTempPanel);
    textpanel.add(Box.createRigidArea(new Dimension(0,5)));
    
    JPanel idTempPanel = new JPanel();
    idTempPanel.setLayout(new BoxLayout(idTempPanel, BoxLayout.X_AXIS));
    idTempPanel.setBackground(Color.white);
    idTempPanel.add(identifierL);
    JTextArea idArea = new JTextArea(identifier);
    idArea.setWrapStyleWord(true);
    idArea.setRows(1);
    idArea.setColumns(15);
    idArea.setEditable(false);
    idArea.setBorder(null);
    idArea.setFont(new Font("Times", Font.PLAIN, 12));
    idTempPanel.setPreferredSize(new Dimension(75, 25));
    idTempPanel.setMinimumSize(new Dimension(75, 40));
    JScrollPane jsp2 = new JScrollPane(idArea);
    jsp2.setBorder(new EmptyBorder(0,0,0,0));
    jsp2.setMaximumSize(new Dimension(500,25));
    idTempPanel.add(jsp2);
    textpanel.add(idTempPanel);
    textpanel.add(Box.createRigidArea(new Dimension(0,5)));
    
    JPanel alttitleTempPanel = new JPanel();
    alttitleTempPanel.setLayout(new BoxLayout(alttitleTempPanel, BoxLayout.X_AXIS));
    alttitleTempPanel.setBackground(Color.white);
    alttitleTempPanel.add(altTitleL);
    JTextArea alttitleArea = new JTextArea(altTitle);
    alttitleArea.setFont(new Font("Times", Font.PLAIN, 12));
    alttitleArea.setWrapStyleWord(true);
    alttitleArea.setLineWrap(true);
    alttitleArea.setRows(1);
    alttitleArea.setColumns(25);
    alttitleArea.setBorder(null);
    alttitleArea.setEditable(false);
    JScrollPane jsp3 = new JScrollPane(alttitleArea);
    jsp3.setBorder(new EmptyBorder(0,0,0,0));
    jsp3.setMaximumSize(new Dimension(500,25));
    alttitleTempPanel.add(jsp3);
    textpanel.add(alttitleTempPanel);
    textpanel.add(Box.createRigidArea(new Dimension(0,5)));
    
    JPanel origTempPanel = new JPanel();
    origTempPanel.setBackground(Color.white);
    origTempPanel.setLayout(new BoxLayout(origTempPanel, BoxLayout.X_AXIS));
    origTempPanel.add(originatorL);
    
    String text = "";
    for(int i=0; i<originator.size(); i++)
    {
      String person = (String)originator.elementAt(i);
      if(i == originator.size()-1)
      {
        text += person;
      }
      else
      {
        text += person + ", ";
      }
    }
    JTextArea origArea = new JTextArea(text);
    origArea.setFont(new Font("Times", Font.PLAIN, 12));
    origArea.setLineWrap(true);
    origArea.setRows(1);
    origArea.setWrapStyleWord(true);
    origArea.setColumns(25);
    origArea.setBorder(null);
    origArea.setEditable(false);
    JScrollPane jsp4 = new JScrollPane(origArea);
    jsp4.setBorder(new EmptyBorder(0,0,0,0));
    jsp4.setMaximumSize(new Dimension(500, 25));
    origTempPanel.add(jsp4);
    
    textpanel.add(origTempPanel);
    textpanel.add(Box.createRigidArea(new Dimension(0,5)));
    textpanel.add(editBaseInfoButton);
    
    panel.add(textpanel);
    return panel;
  }
  
  /**
   * creates the list panel with the list boxes
   */
  private JPanel createListPanel(Vector entityfiles, 
                                 Vector otherfiles)
  { 
    JPanel listPanel = new JPanel();
    JPanel outerPanel = new JPanel();
    
    JButton dataFileAdd = new JButton("Add");
    dataFileAdd.addActionListener(this);
    JButton dataFileRemove = new JButton("Remove");
    dataFileRemove.addActionListener(this);
    JButton dataFileEdit = new JButton("Edit");
    dataFileEdit.addActionListener(this);
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(dataFileAdd);
    buttonPanel.add(dataFileRemove);
    buttonPanel.add(dataFileEdit);
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    
    //////////////entity files////////////////////////
    
    entityFileList = new JList(entityfiles);
    entityFileList.setPreferredSize(new Dimension(80, 70));
    entityFileList.setMaximumSize(new Dimension(80, 70));
    entityFileList.addListSelectionListener(new EntitySelectionHandler());
    entityFileList.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if(e.getClickCount() == 2) 
        {
          actionPerformed(new ActionEvent(this, 0, "EditEntity"));
        }
      }
    });
    entityFileList.setVisibleRowCount(10);
    JScrollPane entityFileScrollPane = new JScrollPane(entityFileList);
    JPanel entityFileButtonList = new JPanel();
    entityFileButtonList.setLayout(new BoxLayout(entityFileButtonList,
                                                 BoxLayout.Y_AXIS));
    entityFileButtonList.add(new JLabel("Table Descriptions"));
    entityFileButtonList.add(entityFileScrollPane);
    
    listPanel.add(entityFileButtonList);
    
    ////////////////other files//////////////////////
    
    otherFileList = new JList(otherfiles);
    otherFileList.setPreferredSize(new Dimension(80, 70));
    otherFileList.setMaximumSize(new Dimension(80, 70));
    otherFileList.addListSelectionListener(new OtherSelectionHandler());
    otherFileList.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if(e.getClickCount() == 2) 
        {
          actionPerformed(new ActionEvent(this, 0, "Edit"));
        }
      }
    });
    otherFileList.setVisibleRowCount(10);
    JScrollPane otherFileScrollPane = new JScrollPane(otherFileList);
    JPanel otherFileButtonList = new JPanel();
    otherFileButtonList.setLayout(new BoxLayout(otherFileButtonList,
                                               BoxLayout.Y_AXIS));
    otherFileButtonList.add(new JLabel("Other Descriptions"));
    otherFileButtonList.add(otherFileScrollPane);
    
    //listPanel.setBorder(BorderFactory.createCompoundBorder(
    //                    BorderFactory.createTitledBorder(
    //                    /*"Package Members"*/""),
    //                   BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.X_AXIS));
    //listPanel.setPreferredSize(new Dimension(300, 100));
    
    
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
    listPanel.add(otherFileButtonList);
    //outerPanel.setPreferredSize(new Dimension(500, 100));
    outerPanel.add(listPanel);
    outerPanel.add(buttonPanel);
    
    return outerPanel; 
  }
  
  /**
   * Handles actions from all components in the Container  
   */
  public void actionPerformed(ActionEvent e) 
  {
    String command = e.getActionCommand();
    framework.debug(11, "action fired: " + command);
    EditorInterface editor;
    String item = null;
    
    if(command.equals("Edit Basic Information"))
    { //this is the button that edits the basic info.
      //just set the item to the appropriate id and the let the edit
      //if statement take care of it.
      item = "Base Info (" + dataPackage.getID() + ")";
      command = "Edit";
    }
    
    if(command.equals("Edit"))
    { //edit the currently select package member
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
      
      if(item == null)
      {
        if(otherFileList.getSelectedIndex() == -1)
        {
          if(entityFileList.getSelectedIndex() == -1)
          { //nothing is selected, give an error and return
            ClientFramework.debug(1, "You must select an item to edit.");
            return;
          }
          else
          { 
            actionPerformed(new ActionEvent(this, 0, "EditEntity"));
            return;
            //item = (String)entityFileList.getSelectedValue();
            //item = item + "(" + (String)listValueHash.get(item) + ")";
          }
        }
        else
        {
          item = (String)otherFileList.getSelectedValue();
        }
      }
      
      String id = item.substring(item.indexOf("(")+1, item.indexOf(")"));
      //System.out.println("id: " + id);
      File xmlFile;
      try
      {
        FileSystemDataStore fsds = new FileSystemDataStore(framework);
        xmlFile = fsds.openFile(id);
      }
      catch(Exception eee)
      {
        try
        {
          MetacatDataStore mds = new MetacatDataStore(framework);
          xmlFile = mds.openFile(id);
        }
        catch(Exception eeee)
        {
          framework.debug(0, "Error opening selected file: " + 
                             eeee.getMessage());
          return;
        }
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
        framework.debug(0, "Error reading file : " + id + " " + 
                           eeeee.getMessage());
        return;
      }
      editor.openEditor(sb.toString(), id, location, this);
    }
    else if(command.equals("Add"))
    {
      //ClientFramework.debug(9, "Adding-doesn't work yet!");
      NewPackageMetadataWizard npmw = new NewPackageMetadataWizard(framework,
                                                                   false, 
                                                                   dataPackage);
      npmw.show();
    }
    else if(command.equals("Remove"))
    {
      ClientFramework.debug(9, "Removing-doesn't work yet!");
    }
    else if(command.equals("EditEntity"))
    {
      item = "";
      if(otherFileList.getSelectedIndex() == -1)
      {
        if(entityFileList.getSelectedIndex() == -1)
        { //nothing is selected, give an error and return
          ClientFramework.debug(1, "You must select an item to edit.");
          return;
        }
        else
        { 
          item = (String)entityFileList.getSelectedValue();
          item = item + "(" + (String)listValueHash.get(item) + ")";
        }
      }
      else
      {
        item = (String)otherFileList.getSelectedValue();
      }
      
      String id = item.substring(item.indexOf("(")+1, item.indexOf(")"));
      ClientFramework.debug(20, "Edititing entity: " + id);
      EntityGUI entityEdit = new EntityGUI(dataPackage, id, location, this, 
                                           framework);
      entityEdit.show();
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
    //System.out.println(xmlString);
    framework.debug(11, "editing complete: id: " + id + " location: " + location);
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
      DataPackageGUI newgui = new DataPackageGUI(framework, newPackage);
      newgui.show();
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
   * makes sure that there is only one file selected in the list boxes at
   * any given time
   */
  private class EntitySelectionHandler implements ListSelectionListener
  {
    public void valueChanged(ListSelectionEvent e)
    {
      otherFileList.clearSelection();
    }
  }
  
  /**
   * makes sure that there is only one file selected in the list boxes at
   * any given time
   */
  private class OtherSelectionHandler implements ListSelectionListener
  {
    public void valueChanged(ListSelectionEvent e)
    {
      entityFileList.clearSelection();
    }
  }
}
