/**
 *  '$RCSfile: DataPackageGUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-06 22:39:15 $'
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
                                  //implements ActionListener, ItemListener
{
  private ClientFramework framework;
  private ConfigXML config;
  Container contentPane;
  private DataPackage dataPackage;
  
  public DataPackageGUI(ClientFramework framework, DataPackage dp)
  {
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
    Enumeration keys = relfiles.keys();
    while(keys.hasMoreElements())
    {
      String key = (String)keys.nextElement();
      System.out.println("key: " + key);
      Vector v = (Vector)relfiles.get(key);
      for(int i=0; i<v.size(); i++)
      {
        String s = key + " (" + v.elementAt(i) + ")";
        listitems.addElement(s);
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
    
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    
    JPanel tempPanel = new JPanel();
    tempPanel.add(identifierL);
    tempPanel.add(new JLabel(identifier));
    panel.add(tempPanel);
    
    tempPanel = new JPanel();
    tempPanel.add(titleL);
    tempPanel.add(new JLabel(title));
    panel.add(tempPanel);
    
    tempPanel = new JPanel();
    tempPanel.add(altTitleL);
    tempPanel.add(new JLabel(altTitle));
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
    ///////////////dataFile/////////////////
    /*
    JList dataFileList = new JList(v);
    dataFileList.setVisibleRowCount(10);
    JScrollPane dataFileScrollPane = new JScrollPane(dataFileList);
    JButton dataFileAdd = new JButton("Add");
    JButton dataFileRemove = new JButton("Remove");
    JPanel dataFileButtonPanel = new JPanel();
    dataFileButtonPanel.setLayout(new BoxLayout(dataFileButtonPanel, 
                                                BoxLayout.X_AXIS));
    dataFileButtonPanel.add(dataFileAdd);
    dataFileButtonPanel.add(dataFileRemove);
    JPanel dataFileButtonList = new JPanel();
    dataFileButtonList.setLayout(new BoxLayout(dataFileButtonList,
                                               BoxLayout.Y_AXIS));
    dataFileButtonList.add(new JLabel("Data Descriptors"));
    dataFileButtonList.add(dataFileScrollPane);
    dataFileButtonList.add(dataFileButtonPanel);
    
    /////////////entityFile///////////////////
    JList entityFileList = new JList(v);
    entityFileList.setVisibleRowCount(10);
    JScrollPane entityFileScrollPane = new JScrollPane(entityFileList);
    JButton entityFileAdd = new JButton("Add");
    JButton entityFileRemove = new JButton("Remove");
    JPanel entityFileButtonPanel = new JPanel();
    entityFileButtonPanel.setLayout(new BoxLayout(entityFileButtonPanel, 
                                                BoxLayout.X_AXIS));
    entityFileButtonPanel.add(entityFileAdd);
    entityFileButtonPanel.add(entityFileRemove);
    JPanel entityFileButtonList = new JPanel();
    entityFileButtonList.setLayout(new BoxLayout(entityFileButtonList,
                                               BoxLayout.Y_AXIS));
    entityFileButtonList.add(new JLabel("Entity Descriptors"));
    entityFileButtonList.add(entityFileScrollPane);
    entityFileButtonList.add(entityFileButtonPanel);
    */
    /////////otherFile////////////////////////
    JList otherFileList = new JList(v);
    otherFileList.setVisibleRowCount(10);
    JScrollPane otherFileScrollPane = new JScrollPane(otherFileList);
    JButton otherFileAdd = new JButton("Add");
    JButton otherFileRemove = new JButton("Remove");
    JPanel otherFileButtonPanel = new JPanel();
    otherFileButtonPanel.setLayout(new BoxLayout(otherFileButtonPanel, 
                                                BoxLayout.X_AXIS));
    otherFileButtonPanel.add(otherFileAdd);
    otherFileButtonPanel.add(otherFileRemove);
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
  
  public static void main(String[] args)
  {
    ConfigXML conf = new ConfigXML("./lib/config.xml");
    ClientFramework cf = new ClientFramework(conf);
    //new DataPackageGUI(cf, new DataPackage()).show();
  }
}
