/**
 *  '$RCSfile: DataPackageGUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-08 16:55:51 $'
 * '$Revision: 1.13 $'
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

public class DataPackageGUI extends javax.swing.JFrame 
                            implements ActionListener, EditingCompleteListener
{
  private ClientFramework framework;
  private ConfigXML config;
  Container contentPane;
  private DataPackage dataPackage;
  private JList otherFileList;
  private String location = null;
  private String id = null;
  
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
    setSize(500, 500);
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
    Vector listitems = new Vector();
    listitems.add("Basic Information (" + this.id + ")");
    Enumeration keys = relfiles.keys();
    while(keys.hasMoreElements())
    {
      String key = (String)keys.nextElement();
      Vector v = (Vector)relfiles.get(key);
      for(int i=0; i<v.size(); i++)
      {
        String eleId = (String)v.elementAt(i);
        if(!eleId.equals(this.id))
        {
          String s = key + " (" + eleId + ")";
          listitems.addElement(s);
        }
      }
    }
    JPanel listPanel = createListPanel(listitems);
    contentPane.add(basicInfoPanel);
    contentPane.add(listPanel);
  }
  
  private JPanel createBasicInfoPanel(String identifier, String title, 
                                      String altTitle, Vector originator)
  {
    JPanel panel = new JPanel();
    JLabel identifierL = new JLabel("Identifier: ");
    JLabel titleL = new JLabel("Title: ");
    JLabel altTitleL = new JLabel("Alternate Title: ");
    JLabel originatorL = new JLabel("Data Originator: ");
    String htmlBegin = "<html><p>";
    String htmlEnd = "</p></html";
    
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    JPanel tempPanel = new JPanel();
    tempPanel.add(identifierL);
    JLabel idLabel = new JLabel(identifier);
    tempPanel.add(idLabel);
    panel.add(tempPanel);
    
    tempPanel = new JPanel();
    tempPanel.add(titleL);
    tempPanel.add(new JLabel(title));
    //tempPanel.setPreferredSize(new Dimension(400, 50));
    /*
    JPanel layoutPanel = new JPanel();
    layoutPanel.add(titleL);
    layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
    int l = title.length();
    int inc = 40;
    for(int i=0; i<l; i+=inc)
    {
      int end;
      if(i+inc > l)
      {
        end = l;
      }
      else
      {
        end = i+inc;
      }
      String sub = title.substring(i, end);
      JLabel sublabel = new JLabel(sub);
      layoutPanel.add(sublabel, BorderLayout.CENTER);
    }
    */
    //JLabel titleLabel = new JLabel(title);
    //tempPanel.add(titleLabel);
    panel.add(tempPanel);
    //panel.add(layoutPanel);
    
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
    
    return panel;
  }
  
  private JPanel createListPanel(Vector v)
  { 
    otherFileList = new JList(v);
    otherFileList.setVisibleRowCount(10);
    JScrollPane otherFileScrollPane = new JScrollPane(otherFileList);
    JButton otherFileAdd = new JButton("Add");
    otherFileAdd.addActionListener(this);
    JButton otherFileRemove = new JButton("Remove");
    otherFileRemove.addActionListener(this);
    JButton otherFileEdit = new JButton("Edit");
    otherFileEdit.addActionListener(this);
    JPanel otherFileButtonPanel = new JPanel();
    otherFileButtonPanel.setLayout(new BoxLayout(otherFileButtonPanel, 
                                                BoxLayout.X_AXIS));
    otherFileButtonPanel.add(otherFileAdd);
    otherFileButtonPanel.add(otherFileRemove);
    otherFileButtonPanel.add(otherFileEdit);
    JPanel otherFileButtonList = new JPanel();
    otherFileButtonList.setLayout(new BoxLayout(otherFileButtonList,
                                               BoxLayout.Y_AXIS));
    otherFileButtonList.add(new JLabel("Package Members"));
    otherFileButtonList.add(otherFileScrollPane);
    otherFileButtonList.add(otherFileButtonPanel);
    
    JPanel listPanel = new JPanel();
    listPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                        /*"Package Members"*/""),
                        BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.X_AXIS));
    listPanel.setPreferredSize(new Dimension(400, 270));
    //listPanel.add(dataFileButtonList);
    //listPanel.add(entityFileButtonList);
    listPanel.add(otherFileButtonList);
    
    return listPanel; 
  }
  
  /**
   * Handles actions from all components in the Container  
   */
  public void actionPerformed(ActionEvent e) 
  {
    String command = e.getActionCommand();
    framework.debug(9, "action fired: " + command);
    EditorInterface editor;
    
    if(command.equals("Edit"))
    {
      System.out.println("Editing");
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
      String item = (String)otherFileList.getSelectedValue();
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
      System.out.println("SB------------" + sb.toString());
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
    System.out.println(xmlString);
    try
    {
      if(location.equals(DataPackage.METACAT))
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
          mds.saveFile(id, new StringReader(xmlString), true);
        }
        else
        {
          mds.saveFile(id, new StringReader(xmlString), false);
        }
      
      }
      else if(location.equals(DataPackage.LOCAL))
      { //save it locally
        FileSystemDataStore fsds = new FileSystemDataStore(framework);
        fsds.saveFile(id, new StringReader(xmlString), false);
      }
    }
    catch(Exception e)
    {
      framework.debug(0, "Error saving file "+ id + " to " + location);
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args)
  {
    ConfigXML conf = new ConfigXML("./lib/config.xml");
    ClientFramework cf = new ClientFramework(conf);
    //new DataPackageGUI(cf, new DataPackage()).show();
  }
}
