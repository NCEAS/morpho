/**
 *  '$RCSfile: DataPackageGUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-15 16:14:39 $'
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
    setSize(800, 450);
    this.show();
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
    
    JPanel basicInfoPanel = createBasicInfoPanel(dataPackage.getIdentifier(), 
                                                 title, 
                                                 altTitle, orig);
    Hashtable relfiles = dataPackage.getRelatedFiles();
    System.out.println("relfiles: " + relfiles.toString());
    Vector otheritems = new Vector();
    Vector dataitems = new Vector();
    Vector entityitems = new Vector();
    //listitems.add("Basic Information (" + this.id + ")");
    Enumeration keys = relfiles.keys();
    while(keys.hasMoreElements())
    {
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
        for(int i=0; i<v.size(); i++)
        {
          String eleid = (String)v.elementAt(i);
          String s = "Entity File (" + eleid + ")";
          entityitems.addElement(s);
        }
      }
      else
      {
        Vector v = (Vector)relfiles.get(key);
        for(int i=0; i<v.size(); i++)
        {
          String eleid = (String)v.elementAt(i);
          String s = key + " (" + eleid + ")";
          otheritems.addElement(s);
        }
      }
      /*
      Vector v = (Vector)relfiles.get(key);
      for(int i=0; i<v.size(); i++)
      {
        String eleId = (String)v.elementAt(i);
        if(!eleId.equals(this.id))
        {
          String s = key + " (" + eleId.trim() + ")";
          listitems.addElement(s);
        }
      }
      */
    }
    JPanel listPanel = createListPanel(dataitems, entityitems, otheritems);
    contentPane.add(basicInfoPanel);
    contentPane.add(listPanel);
  }
  
  private JPanel createBasicInfoPanel(String identifier, String title, 
                                      String altTitle, Vector originator)
  {
    editBaseInfoButton = new JButton("Edit Base Info");
    editBaseInfoButton.addActionListener(this);
    JPanel panel = new JPanel();
    JLabel identifierL = new JLabel("Identifier: ");
    JLabel titleL = new JLabel("Title: ");
    JLabel altTitleL = new JLabel("Alternate Title: ");
    JLabel originatorL = new JLabel("Data Originator: ");
    String htmlBegin = "<html>";
    String htmlEnd = "</html>";
    
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    JPanel tempPanel = new JPanel();
    tempPanel.add(identifierL);
    JLabel idLabel = new JLabel(identifier);
    tempPanel.add(idLabel);
    panel.add(tempPanel);
    
    tempPanel = new JPanel();
    tempPanel.add(titleL);
    tempPanel.add(new JLabel(title));
    
    panel.add(tempPanel);
    
    tempPanel = new JPanel();
    tempPanel.add(altTitleL);
    JLabel altTitleLabel = new JLabel(altTitle);
    tempPanel.add(altTitleLabel);
    panel.add(tempPanel);
    
    tempPanel = new JPanel();
    tempPanel.add(originatorL);
    JPanel tempPanel2 = new JPanel();
    tempPanel2.setLayout(new BoxLayout(tempPanel2, BoxLayout.Y_AXIS));
    for(int i=0; i<originator.size(); i++)
    {
      String person = (String)originator.elementAt(i);
      tempPanel2.add(new JLabel(person));
    }
    tempPanel.add(tempPanel2);
    
    panel.add(tempPanel);
    panel.add(editBaseInfoButton);
    
    return panel;
  }
  
  private JPanel createListPanel(Vector datafiles, Vector entityfiles, 
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
    buttonPanel.setLayout(new BoxLayout(buttonPanel, 
                                                BoxLayout.X_AXIS));
    buttonPanel.add(dataFileAdd);
    buttonPanel.add(dataFileRemove);
    buttonPanel.add(dataFileEdit);
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    
    ////////////////////data files///////////////////////
    
    dataFileList = new JList(datafiles);
    dataFileList.addListSelectionListener(new DataSelectionHandler());
    dataFileList.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if(e.getClickCount() == 2) 
        {
          actionPerformed(new ActionEvent(this, 0, "Edit"));
        }
      }
    });
    dataFileList.setVisibleRowCount(10);
    JScrollPane dataFileScrollPane = new JScrollPane(dataFileList);
    
    JPanel dataFileButtonList = new JPanel();
    dataFileButtonList.setLayout(new BoxLayout(dataFileButtonList,
                                               BoxLayout.Y_AXIS));
    dataFileButtonList.add(new JLabel("Data Files"));
    dataFileButtonList.add(dataFileScrollPane);
    
    listPanel.add(dataFileButtonList);
    
    //////////////entity files////////////////////////
    
    entityFileList = new JList(entityfiles);
    entityFileList.addListSelectionListener(new EntitySelectionHandler());
    entityFileList.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if(e.getClickCount() == 2) 
        {
          actionPerformed(new ActionEvent(this, 0, "Edit"));
        }
      }
    });
    entityFileList.setVisibleRowCount(10);
    JScrollPane entityFileScrollPane = new JScrollPane(entityFileList);
    JPanel entityFileButtonList = new JPanel();
    entityFileButtonList.setLayout(new BoxLayout(entityFileButtonList,
                                                 BoxLayout.Y_AXIS));
    entityFileButtonList.add(new JLabel("Entity Members"));
    entityFileButtonList.add(entityFileScrollPane);
    
    listPanel.add(entityFileButtonList);
    
    ////////////////other files//////////////////////
    
    otherFileList = new JList(otherfiles);
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
    otherFileButtonList.add(new JLabel("Other Members"));
    otherFileButtonList.add(otherFileScrollPane);
    
    listPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                        /*"Package Members"*/""),
                        BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.X_AXIS));
    listPanel.setPreferredSize(new Dimension(700, 270));
    
    
    outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
    listPanel.add(otherFileButtonList);
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
    
    if(command.equals("Edit Base Info"))
    {
      item = "Base Info (" + dataPackage.getID() + ")";
      command = "Edit";
    }
    
    if(command.equals("Edit"))
    {
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
        if(dataFileList.getSelectedIndex() == -1)
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
              item = (String)entityFileList.getSelectedValue();
            }
          }
          else
          {
            item = (String)otherFileList.getSelectedValue();
          }
        }
        else
        {
          item = (String)dataFileList.getSelectedValue();
        }
      }
      
      String id = item.substring(item.indexOf("(")+1, item.indexOf(")"));
      System.out.println("id: " + id);
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
      System.out.println("Adding");
    }
    else if(command.equals("Remove"))
    {
      System.out.println("Removing");
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
    System.out.println("editing complete: id: " + id + " location: " + location);
    AccessionNumber a = new AccessionNumber(framework);
    boolean metacatpublic = false;
    FileSystemDataStore fsds = new FileSystemDataStore(framework);
    //System.out.println(xmlString);
    try
    {
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
      
      if(localloc)
      { //save it locally
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
      this.hide();
      DataPackageGUI newgui = new DataPackageGUI(framework, newPackage);
    }
    catch(Exception e)
    {
      framework.debug(0, "Error saving file "+ id + " to " + location +
                         " --message: " + e.getMessage());
      
      e.printStackTrace();
    }
  }
  
  private class EntitySelectionHandler implements ListSelectionListener
  {
    public void valueChanged(ListSelectionEvent e)
    {
      otherFileList.clearSelection();
      dataFileList.clearSelection();
    }
  }
  
  private class DataSelectionHandler implements ListSelectionListener
  {
    public void valueChanged(ListSelectionEvent e)
    {
      entityFileList.clearSelection();
      otherFileList.clearSelection();
    }
  }
  
  private class OtherSelectionHandler implements ListSelectionListener
  {
    public void valueChanged(ListSelectionEvent e)
    {
      entityFileList.clearSelection();
      dataFileList.clearSelection();
    }
  }
}
