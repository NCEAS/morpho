/**
 *  '$RCSfile: DataPackageGUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-28 17:20:13 $'
 * '$Revision: 1.87 $'
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

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.EditingCompleteListener;
import edu.ucsb.nceas.morpho.framework.EditorInterface;
//import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.framework.XPathAPI;
import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;
import edu.ucsb.nceas.morpho.util.Log;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.html.*;

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

/**
 * Class that implements a GUI to edit a data package
 */
public class DataPackageGUI extends javax.swing.JFrame 
                            implements ActionListener, 
                                       EditingCompleteListener,
                                       WindowListener
{
  public JPanel basicInfoPanel;
  public String referenceLabel = "";
  Morpho morpho;
  private ConfigXML config;
  Container contentPane;
  private DataPackage dataPackage;
  private JList otherFileList;
  private JList entityFileList;
  private JList dataFileList;
  private String location = null;
  private String id = null;
  private JButton editBaseInfoButton = new JButton();
  Hashtable listValueHash = new Hashtable();
  private Hashtable fileAttributes = new Hashtable();
  private static final String htmlBegin = "<html><font color=black>";
  private static final String htmlEnd = "</font></html>";
  
  JScrollPane tpscroll;
  String wholelabel;
  JEditorPane biglabel;
  JPanel listPanel;
  
  Vector otheritems;
  Vector dataitems;
  public Vector entityitems;

 
  public DataPackageGUI(Morpho morpho, DataPackage dp)
  {
    this.location = dp.getLocation();
    this.id = dp.getID();
    this.dataPackage = dp;
    this.morpho = morpho;
    this.config = morpho.getConfiguration();
    this.addWindowListener(this);
    
    contentPane = getContentPane();
    setTitle("Data Package Editor");
//    BoxLayout box = new BoxLayout(contentPane, BoxLayout.Y_AXIS);
    contentPane.setLayout(new BorderLayout());
    initComponents();
    pack();
    setSize(500, 550);
    /* Center the Frame */
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2 ,
                (screenDim.height - frameDim.height) / 2);
    
    biglabel.setCaretPosition(0);
  }
  
  /**
   * Creates the panels and hands off tasks to other methods
   */
  private void initComponents()
  {
    //get the xml file attributes from the config file
    fileAttributes = PackageUtil.getConfigFileTypeAttributes(morpho, 
                                                             "xmlfiletype");
    
//    contentPane.setLayout(new FlowLayout());
    Vector orig = new Vector();
    String title = "No Title Provided";
    String altTitle = "No Alternate Title Provided";
    Hashtable docAtts = dataPackage.getAttributes();
    
    Vector entityDoctypeList = config.get("entitydoctype");
    Vector resourceDoctypeList = config.get("resourcedoctype");
    Vector attributeDoctypeList = config.get("attributedoctype");
    //String entitytype = config.get("entitydoctype", 0);
    //String resourcetype = config.get("resourcedoctype", 0);
    //String attributetype = config.get("attributedoctype", 0);
    
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
    
    basicInfoPanel = new JPanel();
    //create the top panel with the package basic info
    basicInfoPanel = createBasicInfoPanel();
    
    Hashtable relfiles = dataPackage.getRelatedFiles();
    otheritems = new Vector();
    dataitems = new Vector();
    entityitems = new Vector();
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
      else if (vectorContainsString(entityDoctypeList, key))
      {
        Vector v = (Vector)relfiles.get(key);
        String spacecount = "";
        for(int i=0; i<v.size(); i++)
        {
          String eleid = (String)v.elementAt(i);
          String ss = "Entity File (" + eleid + ")";
          File xmlfile;
          try
          {
            if(dataPackage.getLocation().equals(DataPackageInterface.METACAT))
            {
              MetacatDataStore mds = new MetacatDataStore(morpho);
              xmlfile = mds.openFile(eleid);
            }
            else
            {
              FileSystemDataStore fsds = new FileSystemDataStore(morpho);
              xmlfile = fsds.openFile(eleid);
            }
          }
          catch(FileNotFoundException fnfe)
          {
            Log.debug(0, "The file specified was not found.");
            return;
          }
          catch(CacheAccessException cae)
          {
            Log.debug(0, "You do not have proper permissions to write" +
                               " to the cache.");
            return;
          }
          
          String entityNamePath = config.get("entityNamePath", 0);
          
          NodeList nl = PackageUtil.getPathContent(xmlfile, entityNamePath, 
                                                   morpho);
 //DFH         Node n = nl.item(0);
          Node n = null;
          for (int ii=0;ii<nl.getLength();ii++) {
            n = nl.item(ii);
            String s = n.getFirstChild().getNodeValue().trim();
            //System.out.println("node = "+s);
            spacecount += " ";
            entityitems.addElement(s + spacecount);
            listValueHash.put(s + spacecount, eleid);
          }
        }
      }
      else if (!vectorContainsString(resourceDoctypeList, key) &&
               !vectorContainsString(attributeDoctypeList, key))
      {
        Vector v = (Vector)relfiles.get(key);
        for(int i=0; i<v.size(); i++)
        {
          String eleid = (String)v.elementAt(i);
          Hashtable h = (Hashtable)fileAttributes.get(key);
          String displayName = "";
          if ((h==null)||(h.get("displaypath")==null)) {
            displayName = "";
          }
          else {
           displayName = (String)h.get("displaypath");
          }
          if ((displayName == null) || (displayName.indexOf("FIXED:") != -1))
          {  // only substring if displayName != null
            if (displayName == null) {
              otheritems.addElement(eleid.trim());
            } else {
              displayName = displayName.substring(displayName.indexOf(":") + 1,
                                                displayName.length());
              String s = displayName.trim() + " (" + eleid + ")";
              otheritems.addElement(s.trim());
            }
          }
          else
          { //read the file to get the display text from the file
            File f;
            try
            {
              f = PackageUtil.openFile(eleid, morpho);
            }
            catch(Exception e)
            {
              Log.debug(0, "File from package not found: " + 
                                    e.getMessage());
              e.printStackTrace();
              return;
            }
            if (displayName.length()>0) {
              NodeList nl = PackageUtil.getPathContent(f, displayName, morpho);
              for(int j=0; j<nl.getLength(); j++)
              {
                Node n = nl.item(j);
                String nodeContent = n.getFirstChild().getNodeValue();
                String s = nodeContent + " (" + eleid + ")";
                otheritems.addElement(s.trim());
              }
            }
          }
        }
      }
    }
    
    //if you don't add these spaces, the sizes of the list boxes get all
    //messed up.
    if(otheritems.size()==0)
    {
      otheritems.addElement(" ");
    }
    if(entityitems.size()==0)
    {
      entityitems.addElement(" ");
    }
    
    //create the banner panel
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    //MBJ ImageIcon head = new ImageIcon(
                         //MBJ cl.getResource("../framework/smallheader-bg.gif"));
    //MBJ ImageIcon logoIcon = 
              //MBJ new ImageIcon(cl.getResource("../framework/logo-icon.gif"));
    JLabel logoLabel = new JLabel();
    JLabel headLabel = new JLabel("Package Editor");
    //MBJ logoLabel.setIcon(logoIcon);
    //MBJ headLabel.setIcon(head);
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
    
    listPanel = createListPanel(entityitems, otheritems);
    JPanel layoutPanel = new JPanel();
    layoutPanel.setLayout(new GridLayout(2,1,8,8));
//    layoutPanel.setPreferredSize(new Dimension(450, 500));
//    layoutPanel.setMinimumSize(new Dimension(450, 500));
/*    
    basicInfoPanel.setPreferredSize(new Dimension(500, 250));
    basicInfoPanel.setMaximumSize(new Dimension(500, 250));
    basicInfoPanel.setMinimumSize(new Dimension(500, 250));
*/    //basicInfoPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    
    toppanel.setAlignmentX(0);
    basicInfoPanel.setAlignmentX(0);
    listPanel.setAlignmentX(0);
    
    contentPane.add(BorderLayout.NORTH, toppanel);
//    layoutPanel.add(toppanel);      
//    layoutPanel.add(Box.createRigidArea(new Dimension(0, 8)));
    layoutPanel.add(basicInfoPanel);
//    layoutPanel.add(Box.createRigidArea(new Dimension(0, 8)));
    layoutPanel.add(listPanel);
    contentPane.add(BorderLayout.CENTER,layoutPanel);
  }
  
  /**
   * Determine if the string s is contained within the Vector v
   */
  private boolean vectorContainsString(Vector v, String s) {
    boolean foundMatch = false;
    for (int i=0; i < v.size(); i++) {
      if (s.equals((String)v.elementAt(i))) {
        foundMatch = true;
      }
    }
    return foundMatch;
  }
  
  /**
   * creates the basicinfopanel
   */
  public JPanel createBasicInfoPanel()
  {
    referenceLabel = "";
    String authorList = "";
    
    String idPath = config.get("datasetIdPath", 0);
    String shortNamePath = config.get("datasetShortNamePath", 0);
    String titlePath = config.get("datasetTitlePath", 0);
    String abstractPath = config.get("datasetAbstractPath", 0);
    String keywordPath = config.get("datasetKeywordPath", 0);
    String originatorPath = config.get("datasetOriginatorPath", 0);
    
    //layout the big main panel
    JPanel textpanel = new JPanel();
   // textpanel.setBorder(BorderFactory.createCompoundBorder(
   //                     BorderFactory.createLoweredBevelBorder(),
   //                     BorderFactory.createEmptyBorder(10, 10, 10, 10)));
//    textpanel.setLayout(new BoxLayout(textpanel, BoxLayout.Y_AXIS));
    textpanel.setLayout(new BorderLayout());
    textpanel.setBackground(Color.white);
    //the button to edit the base info
    editBaseInfoButton = new JButton("Edit");
    editBaseInfoButton.addActionListener(this);
    editBaseInfoButton.setActionCommand("Edit Basic Information");
    //the top label
    JLabel headerLabel = new JLabel(htmlBegin + 
                                    "<font size=4>Basic Package Information" +
                                    "</font>" + htmlEnd);
    JPanel headerPanel = new JPanel();
    headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
    headerPanel.add(headerLabel);
    headerPanel.add(Box.createHorizontalGlue());
    headerPanel.add(editBaseInfoButton);
  //  headerPanel.setBackground(Color.white);
    
    Document doc = dataPackage.getTripleFileDom();
    
    NodeList idNL;
    NodeList shortNameNL;
    NodeList titleNL;
    NodeList abstractNL;
    NodeList keywordNL;
    NodeList originatorNL;
    
    try
    {
    //get the node lists from the document to fill in the data
      idNL = XPathAPI.selectNodeList(doc, idPath);
      shortNameNL = XPathAPI.selectNodeList(doc, shortNamePath);
      titleNL = XPathAPI.selectNodeList(doc, titlePath);
      abstractNL = XPathAPI.selectNodeList(doc, abstractPath);
      keywordNL = XPathAPI.selectNodeList(doc, keywordPath);
      originatorNL = XPathAPI.selectNodeList(doc, originatorPath);
    }
    catch(Exception e)
    {
      Log.debug(0, "Error selecting nodes from package file.");
      e.printStackTrace();
      return null;
    }
    
    //get the data from the nodes
    wholelabel = "<html><font color=black>";
    String id = "";
    String shortName = "";
    String title = "";
    String abstractS = "";
    String keywords = ""; 
    
    if(idNL != null && idNL.getLength() != 0)
    {
      id = idNL.item(0).getFirstChild().getNodeValue();
    }
    if(shortNameNL != null && shortNameNL.getLength() != 0)
    {
      shortName = shortNameNL.item(0).getFirstChild().getNodeValue();
    }
    if(titleNL != null)
    {
      title = titleNL.item(0).getFirstChild().getNodeValue();
    }
    if(abstractNL != null && abstractNL.getLength() != 0)
    {
      NodeList children = abstractNL.item(0).getChildNodes();
      for(int i=0; i<children.getLength(); i++)
      {
        Node n = children.item(i);
        if(n.getNodeName().equals("paragraph"))
        {
          String nodeval = getTextValue(n);
//          String nodeval = n.getFirstChild().getNodeValue();
          if(nodeval.trim().equals(""))
          {
            abstractS += "";
          }
          else
          {
            abstractS += "<p>" + getTextValue(n) + "</p>";
//            abstractS += "<p>" + n.getFirstChild().getNodeValue() + "</p>";
          }
        }
      }
    }
    if(keywordNL != null && keywordNL.getLength() != 0)
    {
      for(int i=0; i<keywordNL.getLength(); i++)
      { //get the keywords and concat them into one string
        String keyword = keywordNL.item(i).getFirstChild().getNodeValue();
        keywords += keyword;
        if(i != keywordNL.getLength() - 1)
        {
          keywords += ", ";
        }
      }
    }
    
    wholelabel += htmlize(id, "ID") + htmlize(title, "Title") + 
                  htmlize(shortName, "Short Name") + 
                  htmlize(keywords, "Keywords") + 
                  htmlize(abstractS, "Abstract");
    
    referenceLabel = referenceLabel + "<b><i>" + title + "</i></b><br>" +
           "<b>KNB Accession Number " + id + "</b>  Keywords: " + keywords; 
    
    String originators = "<br><b>Originator(s)</b><br>";
    String name = "";
    String orgname = "";
    String address = "";
    String phone = "";
    String email = "";
    String web = "";
    String role = "";
    
    for(int i=0; i<originatorNL.getLength(); i++)
    {
      name = "";
      orgname = "";
      address = "";
      phone = "";
      email = "";
      web = "";
      role = "";
        
      Node node = originatorNL.item(i);
      NodeList origChildren = node.getChildNodes();
      for(int k=0; k<origChildren.getLength(); k++)
      {
        Node n = origChildren.item(k);
        String nodename = n.getNodeName().trim();
        if(nodename.equals("individualName"))
        {
          NodeList children = n.getChildNodes();
          String firstName = "";
          String lastName = "";
          for(int j=0; j<children.getLength(); j++)
          {
            if(children.item(j).getNodeName().trim().equals("givenName"))
            {
              firstName = getTextValue(children.item(j));
//              firstName = children.item(j).getFirstChild().getNodeValue().trim();
            }
            else if(children.item(j).getNodeName().trim().equals("surName"))
            {
              lastName = getTextValue(children.item(j));
//              lastName = children.item(j).getFirstChild().getNodeValue().trim();
            }
          }
          name = firstName + " " + lastName;
        }
        else if(nodename.equals("organizationName"))
        {
          orgname = getTextValue(n);
//          orgname = n.getFirstChild().getNodeValue().trim();
        }
        else if(nodename.equals("address"))
        {
          NodeList children = n.getChildNodes();
          String dp = "";
          String city = "";
          String aa = "";
          String pc = "";
          for(int j=0; j<children.getLength(); j++)
          {
            Node cn = children.item(j); //child node
            String cnn = cn.getNodeName().trim(); //child node name
            if(cnn.equals("deliveryPoint"))
            {
              dp = getTextValue(cn);
 //             dp = cn.getFirstChild().getNodeValue().trim();
            }
            else if(cnn.equals("city"))
            {
              city = getTextValue(cn);
//              city = cn.getFirstChild().getNodeValue().trim();
            }
            else if(cnn.equals("administrativeArea"))
            {
              aa = getTextValue(cn);
//              aa = cn.getFirstChild().getNodeValue().trim();
            }
            else if(cnn.equals("postalCode"))
            {
              pc = getTextValue(cn);
//              pc = cn.getFirstChild().getNodeValue().trim();
            }
          }
          
          if(!city.equals(""))
          {
            address = htmlize(dp) + city + ", " + aa + "  " + pc;
          }
          else
          {
            address = htmlize(dp) + aa + " " + pc;
          }
        }
        else if(nodename.equals("phone"))
        {
          phone = getTextValue(n);
 //         phone = n.getFirstChild().getNodeValue().trim();
        }
        else if(nodename.equals("electronicMailAddress"))
        {
          email = getTextValue(n);
//          email = n.getFirstChild().getNodeValue().trim();
        }
        else if(nodename.equals("onlineLink"))
        {
          web = getTextValue(n);
//          web = n.getFirstChild().getNodeValue().trim();
        }
        else if(nodename.equals("role"))
        {
          role = getTextValue(n);
//          role = n.getFirstChild().getNodeValue().trim();
        }
      }
      originators += htmlize(name) + htmlize(orgname) + htmlize(address) + 
                     htmlize(phone) + htmlize(email) + htmlize(web) +
                     htmlize(role) + "<br>";
                     
      authorList = authorList + name + ", ";          
    }
    referenceLabel = authorList + referenceLabel;
    
 //   System.out.println("Ref Label: "+referenceLabel);
    
    wholelabel += originators + "</html>";
//    JLabel biglabel = new JLabel(wholelabel);
    biglabel = new JEditorPane("text/html",wholelabel);
    biglabel.setEditable(false);
//    biglabel.setContentType("text/html");
//    biglabel.setText(wholelabel);
//    biglabel.setMaximumSize(new Dimension(350,1000));
 /*   JPanel biglabelPanel = new JPanel();
    biglabelPanel.setLayout(new BoxLayout(biglabelPanel, BoxLayout.Y_AXIS));
    biglabelPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    biglabel.setAlignmentX(0);
    biglabelPanel.add(biglabel);
    biglabelPanel.setBackground(Color.white);
*/    
    headerPanel.setAlignmentX(0);
    textpanel.add(BorderLayout.NORTH,headerPanel);
//    biglabelPanel.setAlignmentX(0);
    tpscroll = new JScrollPane(biglabel);
//    tpscroll.getViewport().add(biglabel);
    textpanel.add(BorderLayout.CENTER,tpscroll);
    return textpanel;
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
 //   entityFileList.setPreferredSize(new Dimension(60, 70));
 //   entityFileList.setMaximumSize(new Dimension(60, 70));
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
//    entityFileList.setVisibleRowCount(10);
    JScrollPane entityFileScrollPane = new JScrollPane(entityFileList);
    JPanel entityFileButtonList = new JPanel();
    entityFileButtonList.setLayout(new BoxLayout(entityFileButtonList,
                                                 BoxLayout.Y_AXIS));
    entityFileButtonList.add(new JLabel("Table Descriptions"));
    entityFileButtonList.add(entityFileScrollPane);
    
//    listPanel.add(entityFileButtonList);
    
    ////////////////other files//////////////////////
    
    otherFileList = new JList(otherfiles);
//    otherFileList.setPreferredSize(new Dimension(60, 70));
//    otherFileList.setMaximumSize(new Dimension(60, 70));
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
//    otherFileList.setVisibleRowCount(10);
    JScrollPane otherFileScrollPane = new JScrollPane(otherFileList);
    JPanel otherFileButtonList = new JPanel();
    otherFileButtonList.setLayout(new BoxLayout(otherFileButtonList,
                                               BoxLayout.Y_AXIS));
    otherFileButtonList.add(new JLabel("Other Information"));
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
    Log.debug(11, "action fired: " + command);
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
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider = 
                        services.getServiceProvider(EditorInterface.class);
        editor = (EditorInterface)provider;
      }
      catch(Exception ee)
      {
        Log.debug(0, "Error acquiring editor plugin: " + ee.getMessage());
        ee.printStackTrace();
        return;
      }
      
      if(item == null)
      {
        if(otherFileList.getSelectedIndex() == -1)
        {
          if(entityFileList.getSelectedIndex() == -1)
          { //nothing is selected, give an error and return
            Log.debug(1, "You must select an item to edit.");
            return;
          }
          else
          { 
            actionPerformed(new ActionEvent(this, 0, "EditEntity"));
            return;
          }
        }
        else
        {
          item = (String)otherFileList.getSelectedValue();
        }
      }
      
      String id = item.substring(item.indexOf("(")+1, item.indexOf(")"));

      StringBuffer sb = new StringBuffer();
      Reader reader = null;
      try {
        reader = dataPackage.openAsReader(id);
        char[] buff = new char[4096];
        int numCharsRead;
      
        while ((numCharsRead = reader.read( buff, 0, buff.length ))!=-1) {
            sb.append(buff, 0, numCharsRead);
        }
      } catch (DocumentNotFoundException dnfe) {
        Log.debug(0, "Error finding file : "+id+" "+dnfe.getMessage());
        return;
      } catch (IOException ioe) {
        Log.debug(0, "Error reading file : "+id+" "+ioe.getMessage());
      } finally {
        try { reader.close();
        } catch (IOException ce) {  
          Log.debug(12, "Error closing Reader : "+id+" "+ce.getMessage());
        }
      }
      editor.openEditor(sb.toString(), id, location, this);
      
    } else if(command.equals("Add")) {
    
      AddMetadataWizard npmw 
                        = new AddMetadataWizard(morpho, false, dataPackage);
      this.dispose();                                                          
      npmw.show();
      npmw.setName("New Description Wizard:" + dataPackage.getID());
      //MBJ framework.addWindow(npmw);
    }
    else if(command.equals("Remove"))
    {
      int sure = JOptionPane.showConfirmDialog(null, 
                                 "If you remove this package member it will " +
                                 "no longer be available in this package. " +
                                 "Are you sure you want to do this?", 
                                 "Package Editor", 
                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                 JOptionPane.WARNING_MESSAGE);
      if(sure == JOptionPane.NO_OPTION || sure == JOptionPane.CANCEL_OPTION)
      { //let the user opt out of this if it was an accident.
        return;
      }
      
      item = "";
      if(otherFileList.getSelectedIndex() == -1)
      {
        if(entityFileList.getSelectedIndex() == -1)
        { //nothing is selected, give an error and return
          Log.debug(1, "You must select an item to edit.");
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
      //get the id of the file that is to be removed.
      String id = item.substring(item.indexOf("(")+1, item.indexOf(")"));
      
      //remove the file.
      boolean locMetacat = false;
      boolean locLocal = false;
      if(location.equals(DataPackageInterface.METACAT) || 
         location.equals(DataPackageInterface.BOTH))
      {
        locMetacat = true;
      }
      if(location.equals(DataPackageInterface.LOCAL) ||
         location.equals(DataPackageInterface.BOTH))
      {
        locLocal = true;
      }
      
      FileSystemDataStore fsds = new FileSystemDataStore(morpho);
      AccessionNumber a = new AccessionNumber(morpho);
      
      //remove the file id from the triples in the package file.
      String newPackageFile = PackageUtil.deleteTriplesInTriplesFile(id, 
                                                                    dataPackage,
                                                                    morpho);
      File tempPackageFile = fsds.saveTempFile("tmp.0", 
                                             new StringReader(newPackageFile));
      String newPackageId = a.incRev(dataPackage.getID());
      newPackageFile = a.incRevInTriples(tempPackageFile, dataPackage.getID(), 
                                         newPackageId);
      //delete the files.
      if(locLocal)
      { //delete locally
        boolean deleted = false;
        try
        {
          deleted = fsds.deleteFile(id);
          fsds.saveFile(newPackageId, new StringReader(newPackageFile));
        }
        catch(Exception ee)
        {
          Log.debug(0, "Error saving package file and/or deleting" +
                                " requested file: " + ee.getMessage());
        }
        
        if(!deleted)
        {
          Log.debug(0, "Error deleting requested file.");
        }
      }
      
      if(locMetacat)
      { //delete on metacat
        boolean deleted = false;
        try
        {
          MetacatDataStore mds = new MetacatDataStore(morpho);
          deleted = mds.deleteFile(id);
          mds.saveFile(newPackageId, new StringReader(newPackageFile), 
                       dataPackage);
        }
        catch(Exception eee)
        {
          Log.debug(0, "Error saving package file and/or deleting" +
                          " requested file from Metacat: " + eee.getMessage()); 
        }
        
        if(!deleted)
        {
          Log.debug(0, "Error deleting requested file from " +
                                "Metacat.");
        }
      }
      
      //refresh the PE
      this.dispose();
      DataPackage newpack = new DataPackage(location, newPackageId, null, 
                                            morpho);
      DataPackageGUI dpg = new DataPackageGUI(morpho, newpack);

      /* Not needed because owner query no longer exists to be refreshed
      // Refresh the query results after the edit is completed
      try {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider = 
                      services.getServiceProvider(QueryRefreshInterface.class);
        ((QueryRefreshInterface)provider).refresh();
      } catch (ServiceNotHandledException snhe) {
        Log.debug(6, snhe.getMessage());
      }
      */

      dpg.show();
    }
    else if(command.equals("EditEntity"))
    {
      item = "";
      if(otherFileList.getSelectedIndex() == -1)
      {
        if(entityFileList.getSelectedIndex() == -1)
        { //nothing is selected, give an error and return
          Log.debug(1, "You must select an item to edit.");
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
      Log.debug(20, "Edititing entity: " + id);
      EntityGUI entityEdit = new EntityGUI(dataPackage, id, location, this, 
                                           morpho);
      entityEdit.show();
    }
  }
  
  /**
   * puts correct html tags on the string provided.  if the string is null
   * or empty it returns an empty string
   * @param s the string to htmlize
   */
  private static String htmlize(String s)
  {
    return htmlize(s, null);
  }
  
  /**
   * puts correct html tags on the string provided.  if the string is null
   * or empty it returns an empty string
   * @param s the string to htmlize
   * @param label the label to add to the string
   */
  private static String htmlize(String s, String label)
  {
    if(s == null || s.trim().equals(""))
    {
      return "";
    }
    else if(label != null)
    {
      return "<b>" + label + "</b>: " + s + "<br>";
    }
    else
    {
      return s + "<br>";
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
    Log.debug(11, "editing complete: id: " + id + " location: " + location);
    AccessionNumber a = new AccessionNumber(morpho);
    boolean metacatpublic = false;
    FileSystemDataStore fsds = new FileSystemDataStore(morpho);
    //System.out.println(xmlString);
  
    boolean metacatloc = false;
    boolean localloc = false;
    boolean bothloc = false;
    String newid = "";
    String newPackageId = "";
    if(location.equals(DataPackageInterface.BOTH))
    {
      metacatloc = true;
      localloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
    {
      localloc = true;
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
          fsds.saveFile(newid, new StringReader(newPackageFile));
          newPackageId = newid;
        }
        else
        { //we edited a file in the package
          Vector newids = new Vector();
          Vector oldids = new Vector();
          String oldid = id;
          newid = a.incRev(id);
          fsds.saveFile(newid, new StringReader(xmlString));
          newPackageId = a.incRev(dataPackage.getID());
          oldids.addElement(oldid);
          oldids.addElement(dataPackage.getID());
          newids.addElement(newid);
          newids.addElement(newPackageId);
          //increment the package files id in the triples
          String newPackageFile = a.incRevInTriples(dataPackage.getTriplesFile(), 
                                                    oldids, 
                                                    newids);
          System.out.println("oldid: " + oldid + " newid: " + newid);          
          fsds.saveFile(newPackageId, new StringReader(newPackageFile)); 
        }
      }
    }
    catch(Exception e)
    {
      Log.debug(0, "Error saving file locally"+ id + " to " + location +
                         "--message: " + e.getMessage());
      Log.debug(11, "File: " + xmlString);
      e.printStackTrace();
    }
    
    try
    {
      if(metacatloc)
      { //save it to metacat
        MetacatDataStore mds = new MetacatDataStore(morpho);
        
        if(id.trim().equals(dataPackage.getID().trim()))
        { //edit the package file
          Vector oldids = new Vector();
          Vector newids = new Vector();
          String oldid = id;
          newid = a.incRev(id);
          File f = fsds.saveTempFile(oldid, new StringReader(xmlString));
          oldids.addElement(oldid);
          newids.addElement(newid);
          String newPackageFile = a.incRevInTriples(f, oldids, newids);
          mds.saveFile(newid, new StringReader(newPackageFile), 
                       dataPackage);
          newPackageId = newid;
        }
        else
        { //edit another file in the package
          Vector oldids = new Vector();
          Vector newids = new Vector();
          String oldid = id;
          newid = a.incRev(id);
 //         mds.saveFile(newid, new StringReader(xmlString), dataPackage);
          Vector names = new Vector();
          Vector readers = new Vector();
          names.addElement(newid);
          readers.addElement(new StringReader(xmlString));
          newPackageId = a.incRev(dataPackage.getID());
          //increment the package files id in the triples
          oldids.addElement(oldid);
          oldids.addElement(dataPackage.getID());
          newids.addElement(newid);
          newids.addElement(newPackageId);
          String newPackageFile = a.incRevInTriples(dataPackage.getTriplesFile(),
                                                    oldids,
                                                    newids);
//          mds.saveFile(newPackageId, new StringReader(newPackageFile), 
//                       dataPackage);
          names.addElement(newPackageId);
          readers.addElement(new StringReader(newPackageFile));
          String res = mds.saveFilesTransaction(names, readers, dataPackage);
          Log.debug(20,"Transaction result is: "+res);
        }
      }
    }
    catch(Exception e)
    {
      String message = e.getMessage();
      if(message.indexOf("Next revision number must be") != -1)
      {
        Log.debug(0,"The file you are attempting to update " +
                                 "has been changed by another user.  " +
                                 "Please refresh your query screen, " + 
                                 "open the package again and " +
                                 "re-enter your changes.");
      /* Not needed because owner query no longer exists
        try {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider = 
                      services.getServiceProvider(QueryRefreshInterface.class);
        ((QueryRefreshInterface)provider).refresh();
        } 
        catch (ServiceNotHandledException snhe) 
        {
          Log.debug(6, snhe.getMessage());
        }
      */
        this.dispose();
        return;
      }
      Log.debug(0, "Error saving file to metacat "+ id + " to " + location +
                         "--message: " + e.getMessage());
      //Log.debug(11, "File: " + xmlString);
      e.printStackTrace();
    }
    
      DataPackage newPackage = new DataPackage(location, newPackageId, null,
                                                 morpho);

      /* Not needed because owner query no longer exists
      // Refresh the query results after the edit is completed
      try {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider = 
                      services.getServiceProvider(QueryRefreshInterface.class);
        ((QueryRefreshInterface)provider).refresh();
      } catch (ServiceNotHandledException snhe) {
        Log.debug(6, snhe.getMessage());
      }
      */
    
      this.dispose();
      DataPackageGUI newgui = new DataPackageGUI(morpho, newPackage);
      newgui.show();
    
  }
  
  public void editingCanceled(String xmlString, String id, String location)
  { //do nothing
  }
  
  public void windowClosed(WindowEvent event)
  {
    //MBJ framework.removeWindow(this);
  }
  
  public void windowClosing(WindowEvent event)
  {
    //MBJ framework.removeWindow(this);
  }
  public void windowActivated(WindowEvent event)
  {
    }
  public void windowDeactivated(WindowEvent event)
  {}
  public void windowIconified(WindowEvent event)
  {}
  public void windowDeiconified(WindowEvent event)
  {}
  public void windowOpened(WindowEvent event)
  {
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
  
  
  private String getTextValue(Node nd) {
    String val = "";
    Node child = nd.getFirstChild();
    if (child!=null) {
      val = child.getNodeValue().trim();
    }
    return val;
  }
  
}
