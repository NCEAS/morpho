/**
 *  '$RCSfile: EntityGUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-06-25 21:19:46 $'
 * '$Revision: 1.1 $'
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
  
  /**
   * Creates a new entity editor.
   * @param dp the datapackage that the entity belongs to
   * @param id the id of the entity file that we want to edit.
   */
  public EntityGUI(DataPackage dp, String id, String location, ClientFramework cf)
  {
    this.location = location;
    this.framework = cf;
    this.dataPackage = dp;
    this.entityId = id;
    contentPane = getContentPane();
    setTitle("Table Editor");
    BoxLayout box = new BoxLayout(contentPane, BoxLayout.Y_AXIS);
    contentPane.setLayout(box);
    initComponents();
    pack();
    setSize(500, 450);
  }
  
  private void initComponents()
  {
    JButton editEntityButton = new JButton("Edit Table Description");
    editEntityButton.addActionListener(this);
    
    attributes.add("attribute 1");
    attributes.add("attribute 2");
    attributes.add("attribute 3");
    attributeList = new JList(attributes);
    attributeList.setPreferredSize(new Dimension(180, 225)); 
    attributeList.setMaximumSize(new Dimension(180, 225));
    //attributeList.setBorder(BorderFactory.createLoweredBevelBorder());
    attributeList.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(Color.black),
                            BorderFactory.createLoweredBevelBorder()));
    //attributeList.setMaximumSize(new Dimension(200, 250)); 
    
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
    entityPanel.add(editEntityButton);
    entityPanel.setPreferredSize(new Dimension(225, 280));
    entityPanel.setBackground(Color.white);
    
    JButton add = new JButton("Add");
    JButton remove = new JButton("Remove");
    JButton edit = new JButton("Edit");
    add.setFont(new Font("Dialog", Font.PLAIN, 9));
    edit.setFont(new Font("Dialog", Font.PLAIN, 9));
    remove.setFont(new Font("Dialog", Font.PLAIN, 9));
    remove.addActionListener(this);
    edit.addActionListener(this);
    add.addActionListener(this);
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(add);
    buttonPanel.add(remove);
    buttonPanel.add(edit);
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBackground(Color.white);
    listandbuttons.add(buttonPanel);
    listandbuttons.setBorder(BorderFactory.createLineBorder(Color.black));
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
  }
}
