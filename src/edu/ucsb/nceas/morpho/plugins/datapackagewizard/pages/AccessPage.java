/**
 *  '$RCSfile: AccessPage.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-03-13 03:57:28 $'
 * '$Revision: 1.28 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.dataone.service.types.v1.Group;
import org.dataone.service.types.v1.Person;
import org.dataone.service.types.v1.Subject;
import org.dataone.service.types.v1.SubjectInfo;
import org.dataone.service.types.v1.comparators.GroupNameComparator;
import org.dataone.service.types.v1.comparators.PersonFamilyNameComparator;
import org.dataone.service.util.TypeMarshaller;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataONEDataStoreService;
import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.ModalDialog;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.HyperlinkButton;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.ProgressBarThread;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.utilities.OrderedMap;

public class AccessPage
    extends AbstractUIPage {

  private final String pageID = DataPackageWizardInterface.ACCESS_PAGE;
  private final String pageNumber = "";
  private final String title = "Access Page";
  private final String subtitle = "";
  private final static String EMPTY_STRING = "";
  private final static String SUBJECTINFO = "subjectInfo";
  private final static String ACCESSLIST = "accesslist";


  private JTextField treeFilterField;
  protected JTree accessTree;
  private JPanel bottomPanel;
  private JPanel topPanel;
  private JPanel middlePanel;
  protected JTextField dnField;
  private JButton refreshButton;
  private JLabel warnLabel;
  private JLabel introLabel;
  private JLabel accessDesc1, accessDesc2;
  private String userAccessType = " " + Language.getInstance().getMessage("Allow");
  protected String userAccess = " " + Language.getInstance().getMessage("Read");
  protected JComboBox typeComboBox;
  protected JComboBox accessComboBox;
  private JScrollPane accessTreePane;
  protected AccessProgressThread pbt = null;
  private String accessListFilePath = null;
  public JTreeTable treeTable = null;
  private QueryNetworkThread queryNetwork = null;
  private boolean queryCancelled;
  private String userDN = null;
  private String userName = null;
  private String userEmail = null;
  private String userOrg = null;

  private ConfigXML config;
  private Vector<String> orgList;

  private final String[] accessTypeText = new String[] {
      /*"  Allow"*/ " " + Language.getInstance().getMessage("Allow"),
      /*"  Deny"*/ " " + Language.getInstance().getMessage("Deny")
  };

  private final String[] accessText = new String[] {
      /*"  Read"*/ " " + Language.getInstance().getMessage("Read"),
      /*"  Read & Write"*/ " " + Language.getInstance().getMessage("Read") + " & " + Language.getInstance().getMessage("Write"),
      /*"  Read, Write & Change Permissions"*/ " " + Language.getInstance().getMessage("Read") + ", " + Language.getInstance().getMessage("Write") + " & " + Language.getInstance().getMessage("ChangePermissions"),
      /*"  All "*/  " " + Language.getInstance().getMessage("All")
  };

  public boolean accessIsAllow = true;
  private String xPathRoot = "/eml:eml/access";

  public void setQueryCancelled(boolean queryCancelled) {
    this.queryCancelled = queryCancelled;
  }

  public AccessPage() {
	nextPageID = "";
    init();
  }

  /**
   * initialize method does frame-specific design - i.e. adding the widgets that
   are displayed only in this frame (doesn't include prev/next buttons etc)
   */
  private void init() {

    this.setLayout(new BorderLayout());

    // Defining the top most panel.....
    topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    topPanel.add(WidgetFactory.makeHalfSpacer());
    topPanel.setBorder(new javax.swing.border.EmptyBorder(
        0, 4 * WizardSettings.PADDING, 0, 0));
    JLabel desc = WidgetFactory.makeHTMLLabel(
        "<font size=\"4\"><b>" + /*Define Access*/ Language.getInstance().getMessage("DefineAccess") + " :</b></font>"
    		, 1);
    topPanel.add(desc);
    topPanel.add(WidgetFactory.makeHalfSpacer());
    introLabel = WidgetFactory.makeHTMLLabel(
        /*"<b>Select a user or group from the list below:</b>"*/
    	"<b>" + Language.getInstance().getMessage("AccessPage.SelectUser") + " :</b>"	
    		, 1);
    topPanel.add(introLabel);
    this.add(topPanel, BorderLayout.NORTH);
    ///////////////////////////////////////////////////////

    // tree filtering
	treeFilterField = WidgetFactory.makeOneLineTextField();

    // Define the middle panel which has the  accessTree ....
    middlePanel = new JPanel();
    middlePanel.setLayout(new BorderLayout());

    middlePanel.setBorder(new javax.swing.border.EmptyBorder(
        5 * WizardSettings.PADDING, 6 * WizardSettings.PADDING,
        4 * WizardSettings.PADDING, 5 * WizardSettings.PADDING));
    this.add(middlePanel, BorderLayout.CENTER);

    /// Define bottom panel
    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

    bottomPanel.add(WidgetFactory.makeHalfSpacer());
    JPanel accessDefinitionPanel = new JPanel();
    accessDefinitionPanel.setLayout(new BorderLayout());

    JLabel accessDefinitionLabel = WidgetFactory.makeHTMLLabel(
        "<b>&nbsp;" + /*Description of access levels*/ Language.getInstance().getMessage("AccessPage.AccesLevel")+ " :</b>"
        + "<ul>" 
       /*+ "<li>Read: Able to view data package.</li>"*/
        + "<li>" + /*Read*/ Language.getInstance().getMessage("Read") 
        + " : "+ /*"Able to view data package.*/ Language.getInstance().getMessage("ReadDescription")+"</li>"
        
       /*+ "<li>Read & Write: Able to view and modify data package.</li>"*/
        + "<li>"+ /*Read & Write*/ Language.getInstance().getMessage("Read") + " & " +Language.getInstance().getMessage("Write") 
        + " : " + /*"Able to view and modify data package.*/ Language.getInstance().getMessage("ReadWriteDescription") + "</li>"
        
       /*+ "<li>Read, Write & Change Permissions: Able to view and modify "
        + "datapackage, and modify access permissions.</li>"*/
        + "<li>" + Language.getInstance().getMessage("Read") + ", " + Language.getInstance().getMessage("Write") + " & " + Language.getInstance().getMessage("ChangePermissions")
        + " : " + Language.getInstance().getMessage("ReadWriteChangePermissionsDescription") + "</li>"
        
        /*+ "<li>All: Able to do everything (this is the same as Read, Write "
        + "& Change Permissions)*/
        + "<li>" + Language.getInstance().getMessage("All") + " : " + Language.getInstance().getMessage("AllAccessDescription") + "</li>"
        
        +"</li></ul>", 5);

    warnLabel = WidgetFactory.makeLabel(EMPTY_STRING, true);

    accessDefinitionPanel.add(accessDefinitionLabel, BorderLayout.CENTER);
    accessDefinitionPanel.add(warnLabel, BorderLayout.SOUTH);
    bottomPanel.add(accessDefinitionPanel);
    bottomPanel.setBorder(new javax.swing.border.EmptyBorder(0,
        4 * WizardSettings.PADDING,
        3 * WizardSettings.PADDING, 8 * WizardSettings.PADDING));

    this.add(bottomPanel, BorderLayout.SOUTH);

    if (Access.accessTreeNode != null &&
        Access.accessTreeMetacatServerName.compareTo(Morpho.thisStaticInstance.getDataONEDataStoreService().getCNodeURL()) == 0) {

      displayTree(Access.accessTreeNode);
    }

    config = Morpho.getConfiguration();
    accessListFilePath = ConfigXML.getConfigDirectory() + "/" + Morpho.ACCESS_FILE_NAME;
    orgList = config.get("organization");
  }

  /**
   * Generates Access.accessTreeNode ... the algorithm followed is the following:
   * 0. show a progress bar with text at the bottom showing which step
   *    is being performed and a cancel button .. if cancel button is pressed,
   *    thing on step 4 are performed....
   * 1. try to read accessListFilePath and find if there is an entry for current
   *    metacat server name...
   * 2. if there is an entry generate the dom for the <result></result> and
   *    send it domToTreeNode() funtion.
   * 3. If not, contact metacat server with action=getprincipals...
   * 4. If metacat server is not available, set appropriate text in middlePanel,
   *    show dnPanel, make other required changes in panel and
   *    close the progressbar
   * 5. If metacat server is available, get the result... store it in file..
   *    if it in the file already then delete the old entry and write
   *    the file again...
   *
   *  @return
   */

  public void generateAccessTree(boolean force) {
    SubjectInfo subjectInfo = getSubjectInfoFromFile();
    if (subjectInfo == null || force) {
      pbt = new AccessProgressThread(this);
      pbt.start();

      pbt.setCustomCancelAction(
          new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          Log.debug(45, "\nAccessPage: CustomCancelAction called");
          cancelGetDocumentFromNetwork();
        }
      });

      getSubjectInfoFromNetwork();
    } else {
      DefaultMutableTreeNode treeNode = getTreeFromDocument(subjectInfo);
      displayTree(treeNode);
    }
  }

  
  
  /*
   * Get the SubjecInformation from the cached file.
   */
  private SubjectInfo getSubjectInfoFromFile() {
    SubjectInfo subjectInfo= null;
    ConfigXML accessXML = null;

    try {
      accessXML = new ConfigXML(accessListFilePath);

      Document doc = accessXML.getDocument();
      NodeList nl = doc.getElementsByTagName("server");
      if (nl.getLength() < 1) {
        Log.debug(45, "No server nodes found in " + accessListFilePath);
        return null;
      }
      Node cn = null;
      Node serverNode = null;
      
      for (int i = 0; i < nl.getLength(); i++) {
        cn = nl.item(i).getFirstChild(); // assume 1st child is text node
        if ( (cn != null) && (cn.getNodeType() == Node.TEXT_NODE) &&
            cn.getNodeValue().compareTo(Morpho.thisStaticInstance.getDataONEDataStoreService().getCNodeURL()) == 0) {
          serverNode = cn;
          break;
        }
      }
      if (serverNode == null) {
        Log.debug(45,
            "No server nodes found with current metacat server name " +
            "found in " + accessListFilePath);
        return null;
      }

      Node resultNode = serverNode.getParentNode().getParentNode();
      NodeList resultChildren = resultNode.getChildNodes();
      String xml = null;
      if(resultChildren != null) {
        for(int i=0; i<resultChildren.getLength(); i++) {
          Node child = resultChildren.item(i);
          if(child.getLocalName() != null && child.getLocalName().equals(SUBJECTINFO)) {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            StringWriter sw = new StringWriter();
            serializer.transform(new DOMSource(child), new StreamResult(sw));
            xml = sw.getBuffer().toString();
            continue;
          }
        }
      }
     
      if(xml != null) {
        ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        subjectInfo = TypeMarshaller.unmarshalTypeFromStream(SubjectInfo.class, input);
      }
      return subjectInfo;
    }
    catch (FileNotFoundException e) {
      Log.debug(10, accessListFilePath + " not found");
      Log.debug(45, "Exception in AccessPage class in getDocumentfromFile(). "
          + "Exception:" + e.getClass());
      Log.debug(45, e.getMessage());

      // creating accessListFilePath....
      try {
        String xmlSource =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<accesslist></accesslist>\n";
        byte buf[] = xmlSource.getBytes(Charset.forName("UTF-8"));
        OutputStream f1 = new FileOutputStream(accessListFilePath);
        f1.write(buf);
        f1.close();
      }
      catch (Exception e1) {
        Log.debug(10, "Unable to create " + accessListFilePath);
        Log.debug(45,
            "Exception in AccessPage class in getDocumentfromFile(). "
            + "Exception:" + e1.getClass());
        Log.debug(45, e1.getMessage());
      }

      return null;
    }
    catch (Exception e) {
      Log.debug(45, "Exception in AccessPage class in getDocumentfromFile(). "
          + "Exception:" + e.getClass());
      Log.debug(45, e.getMessage());
      return null;
    }
  }

  protected void getSubjectInfoFromNetwork() {
    if (pbt != null) {
      pbt.setProgressBarString(
          "Contacting Server for Access information....");
    }

    queryCancelled = false;
    queryNetwork = new QueryNetworkThread(this);
    queryNetwork.start();

    return;
  }

  
  
  /*
   * Insert the xml presentation of a SubjectInfo object into access list
   */
  private void insertSubjectInfoInAccessList(SubjectInfo subjectInfo) {

    ConfigXML accessXML = null;
    boolean fileExists = false;

    try {
      accessXML = new ConfigXML(accessListFilePath);
      fileExists = true;
    }
    catch (FileNotFoundException e) {
      Log.debug(10, accessListFilePath + " not found.");
      Log.debug(45, "Exception in AccessPage class in getDocumentfromFile(). "
          + "Exception:" + e.getClass());
      Log.debug(45, e.getMessage());

    }

    try {
      if (!fileExists) {
        // creating accessListFilePath....
        try {
          String xmlSource =
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<accesslist></accesslist>\n";
          byte buf[] = xmlSource.getBytes(Charset.forName("UTF-8"));
          OutputStream f1 = new FileOutputStream(accessListFilePath);
          f1.write(buf);
          f1.close();
        }
        catch (Exception e1) {
          Log.debug(10, "Unable to create " + accessListFilePath);
          Log.debug(45,
              "Exception in AccessPage class in getDocumentfromFile(). "
              + "Exception:" + e1.getClass());
          Log.debug(45, e1.getMessage());
          return;
        }

        accessXML = new ConfigXML(accessListFilePath);
      }

      Document doc1 = accessXML.getDocument();
      NodeList nl = doc1.getElementsByTagName("server");
      if (nl.getLength() < 1) {
        Log.debug(45, "No server nodes found in " + accessListFilePath
            + " Inserting new entry for current document in the document");
        insertNewEntryInAccessList(accessXML, subjectInfo);
        return;
      }

      Node cn = null;
      Node serverNode = null;

      for (int i = 0; i < nl.getLength(); i++) {
        cn = nl.item(i).getFirstChild(); // assume 1st child is text node
        if ( (cn != null) && (cn.getNodeType() == Node.TEXT_NODE) &&
            cn.getNodeValue().compareTo(
                Morpho.thisStaticInstance.getDataONEDataStoreService().getCNodeURL()) == 0) {
          serverNode = cn;
          continue;
        }
      }

      if (serverNode == null) {
        insertNewEntryInAccessList(accessXML, subjectInfo);
      } else {
        modifyOldEntryInAccessList(accessXML, subjectInfo);
      }

    }
    catch (Exception e) {
      Log.debug(10,
          "Exception in AccessPage class in insertDocInAccessList(). "
          + "Exception:" + e.getClass());
      Log.debug(10, e.getMessage());
    }
  }

  
  private void insertNewEntryInAccessList(ConfigXML accessXML, SubjectInfo subjectInfo) throws Exception {
    Log.debug(10, "Inserting a new entry in " + accessListFilePath);
    
    Node subjectInfoNode = createNodeForSubjectInfo(subjectInfo);
    if (subjectInfoNode == null) {
      return;
    }
    
    Document doc1 = accessXML.getDocument();
    Node node = getAccessListNode(doc1);
    if(node == null) {
      node = doc1.createElement(ACCESSLIST);
      doc1.appendChild(node);
    }
    Node result = doc1.createElement("result");
    Node server = doc1.createElement("server");
    Node serverName = doc1.createTextNode(
        Morpho.thisStaticInstance.getDataONEDataStoreService().getCNodeURL());

   

    Node principals = doc1.importNode(subjectInfoNode, true);

    server.appendChild(serverName);
    result.appendChild(server);
    result.appendChild(principals);
    node.appendChild(result);

    accessXML.save();
    
    StateChangeMonitor.getInstance().notifyStateChange(new StateChangeEvent(this, StateChangeEvent.ACCESS_LIST_MODIFIED));
  }

   
  
  private void modifyOldEntryInAccessList(ConfigXML accessXML, SubjectInfo subjectInfo) throws Exception{

    Log.debug(10, "Modifying an old entry in " + accessListFilePath);

    Document doc1 = accessXML.getDocument();
    Node node = getAccessListNode(doc1);
    NodeList nl = doc1.getElementsByTagName("server");

    for (int count = 0; count < nl.getLength(); count++) {
      Node tempNode = nl.item(count);
      String value = tempNode.getFirstChild().getNodeValue();
      if (value != null && value.compareTo(
          Morpho.thisStaticInstance.getDataONEDataStoreService().getCNodeURL()) == 0) {
        Node resultNode = tempNode.getParentNode();
        resultNode.getParentNode().removeChild(resultNode);
      }
    }

    Node result = doc1.createElement("result");
    Node server = doc1.createElement("server");
    Node serverName = doc1.createTextNode(
        Morpho.thisStaticInstance.getDataONEDataStoreService().getCNodeURL());

    server.appendChild(serverName);
    result.appendChild(server);

    Node subjectInfoNode = createNodeForSubjectInfo(subjectInfo);
    if(subjectInfoNode != null) {
      Node principals = doc1.importNode(subjectInfoNode, true);
      result.appendChild(principals);
    }
    node.appendChild(result);
    accessXML.save();
    
    StateChangeMonitor.getInstance().notifyStateChange(new StateChangeEvent(this, StateChangeEvent.ACCESS_LIST_MODIFIED));

  }
  
  /*
   * Get the accesslist node from the doc. Null will be return if it can't be found.
   */
  private Node getAccessListNode(Document doc) {
    Node node = null;
    if(doc != null) {
      NodeList children = doc.getChildNodes();
      if(children != null) {
        for(int i=0; i<children.getLength(); i++) {
          Node child = children.item(i);
          if(child != null && child.getLocalName() != null && child.getLocalName().equals(ACCESSLIST)) {
            node= child;
            break;
          }
        }
      } 
    }
    return node;
  }
  
  /*
   * Create a node presentation for the subjectInfo object
   */
  private Node createNodeForSubjectInfo(SubjectInfo subjectInfo) throws Exception {
    Node node = null;
    if(subjectInfo != null && (subjectInfo.sizePersonList() >0 || subjectInfo.sizeGroupList() >0)) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      TypeMarshaller.marshalTypeToOutputStream(subjectInfo, output);
      String xml = output.toString("UTF-8");
      ///System.out.println("the xml file is "+xml);
      //System.out.println(""+xml);
      DocumentBuilder parser = Morpho.createDomParser();
      Document doc = parser.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
      NodeList nodeList = doc.getChildNodes(); 
      if(nodeList != null) {
        for(int i=0; i<nodeList.getLength(); i++) {
          Node child = nodeList.item(i);
          if(child != null && child.getLocalName() != null && child.getLocalName().equals(SUBJECTINFO)) {
            node = child.cloneNode(true);
            break;
          }
         
        }
      }
    }
    return node;
  }

  protected void parseInputStream(SubjectInfo queryResult) {
    pbt.setProgressBarString(
        "Creating Access tree from information received....");

    try {
      

      DefaultMutableTreeNode treeNode = getTreeFromDocument(queryResult);
      insertSubjectInfoInAccessList(queryResult);
      displayTree(treeNode);
    }
    catch (Exception e) {
      Log.debug(10, "Unable to parse the reply from server.");
      Log.debug(10, "Exception in AccessPage class in parseInputStream()."
          + "Exception: " + e.getClass());
      Log.debug(10, e.getMessage());
      //// File is not on harddisk and data is not avaiable from
      //// display a dn field to be entered by user...
      if (Access.accessTreeNode != null &&
          Access.accessTreeMetacatServerName.compareTo(Morpho.thisStaticInstance.getDataONEDataStoreService().getCNodeURL()) == 0) {
        Log.debug(10,
            "Retrieving access information from server failed. "
            + "Displaying the old access information.");
        displayTree(Access.accessTreeNode);
      } else {
        displayDNPanel();
      }
    }

    pbt.exitProgressBarThread();

    // save doc to the file

  }
  


  protected void displayDNPanel() {
    JPanel panel = null;

    panel = WidgetFactory.makePanel(1);
    JLabel dnLabel = WidgetFactory.makeLabel(/*"Distinguished Name"*/ Language.getInstance().getMessage("DistinguishedName"),
     										false);
    panel.add(dnLabel);
    dnField = WidgetFactory.makeOneLineTextField();
    dnField.setBackground(java.awt.Color.white);
    if (userDN != null) {
      dnField.setText(userDN);
    }
    panel.add(dnField);
    panel.setBorder(new javax.swing.border.EmptyBorder(
        0, WizardSettings.PADDING,
        0, 4 * WizardSettings.PADDING));

    middlePanel.add(panel, BorderLayout.CENTER);
    middlePanel.add(getAccessControlPanel(true, 
    									/*"Retrieve the user list ..."*/ Language.getInstance().getMessage("AccessPage.RetrieveUserList") + " ..."),
        BorderLayout.SOUTH);
    introLabel.setText(/*"Specify a Distinguished Name in text field below:"*/
    					Language.getInstance().getMessage("AccessPage.SpecifyDistinguishedName") + " :"
    					);
    middlePanel.revalidate();
    middlePanel.repaint();

    typeComboBox.setEnabled(true);
    accessComboBox.setEnabled(true);

  }

  private JPanel getAccessControlPanel(boolean withRefreshLink,
      String refreshString) {

    accessDesc1 = WidgetFactory.makeLabel(/*" selected user(s)"*/ " " + Language.getInstance().getMessage("AccessPage.SelectedUser") , false);
    accessDesc2 = WidgetFactory.makeLabel(/*"   access"*/ "   " + Language.getInstance().getMessage("access") , false);

    // define item listener for allow-deny list....
    ItemListener accessTypeListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Log.debug(45, "got itemStateChanged command in access type list");

        if (e.getItem().toString().compareTo(accessTypeText[0]) == 0) {
          userAccessType = accessTypeText[0];
          accessIsAllow = true;
        } else if (e.getItem().toString().compareTo(accessTypeText[1]) == 0) {
          userAccessType = accessTypeText[1];
          accessIsAllow = false;
        }
      }
    };

    typeComboBox = WidgetFactory.makePickList(accessTypeText, false,
        0, accessTypeListener);
    typeComboBox.setEnabled(false);
    if (userAccessType.compareTo(accessTypeText[1]) == 0) {
      typeComboBox.setSelectedIndex(1);
    }

    ItemListener accessListener = new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Log.debug(45, "got itemStateChanged command in access list");

        if (e.getItem().toString().compareTo(accessText[0]) == 0) {
          userAccess = accessText[0];
        } else if (e.getItem().toString().compareTo(accessText[1]) == 0) {
          userAccess = accessText[1];
        } else if (e.getItem().toString().compareTo(accessText[2]) == 0) {
          userAccess = accessText[2];
        } else if (e.getItem().toString().compareTo(accessText[3]) == 0) {
          userAccess = accessText[3];
        }
      }
    };

    accessComboBox = WidgetFactory.makePickList(accessText, false, 0,
        accessListener);
    accessComboBox.setEnabled(false);
    if (userAccess.compareTo(accessText[1]) == 0) {
      accessComboBox.setSelectedIndex(1);
    } else if (userAccess.compareTo(accessText[2]) == 0) {
      accessComboBox.setSelectedIndex(2);
    } else if (userAccess.compareTo(accessText[3]) == 0) {
      accessComboBox.setSelectedIndex(3);
    }

    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
    controlPanel.setBorder(new javax.swing.border.EmptyBorder(5 *
        WizardSettings.PADDING, 0, 0, 0));

    controlPanel.add(typeComboBox);
    controlPanel.add(accessDesc1);
    controlPanel.add(accessComboBox);
    controlPanel.add(accessDesc2);

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.add(WidgetFactory.makeHalfSpacer(), BorderLayout.NORTH);

    if (withRefreshLink) {
      final AccessPage accessP = this;
      GUIAction refreshListAction
          = new GUIAction(refreshString, null,
          new Command() {

        public void execute(ActionEvent ae) {
          Log.debug(45, "got action performed command from Referesh button");

          refreshButton.setEnabled(false);
          typeComboBox.setEnabled(false);
          accessComboBox.setEnabled(false);

          middlePanel.removeAll();
          middlePanel.revalidate();
          middlePanel.repaint();

          pbt = new AccessProgressThread(accessP);
          pbt.start();
          pbt.setCustomCancelAction(
              new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
              Log.debug(45, "\nAccess: CustomAddAction called");
              cancelGetDocumentFromNetwork();
            }
          });
          getSubjectInfoFromNetwork();
        }
      });

      /// define and add refresh tree button....
      refreshButton = new HyperlinkButton(refreshListAction);
      panel.add(refreshButton, BorderLayout.EAST);
      
      // show when the list was last refreshed
      File accessFile = new File(ConfigXML.getConfigDirectory() + "/" + Morpho.ACCESS_FILE_NAME);
      //File accessFile = new File(accessListFilePath);
      Calendar lastModified = Calendar.getInstance();
      lastModified.setTimeInMillis(accessFile.lastModified());
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
      JLabel dateLabel = 
    	  new JLabel(
    			  Language.getInstance().getMessage("LastModified") + ": " + sdf.format(lastModified.getTime()));
      panel.add(dateLabel, BorderLayout.WEST);

    }
    panel.add(controlPanel, BorderLayout.SOUTH);
    return panel;
  }

  protected void cancelGetDocumentFromNetwork() {

    if (Access.accessTreeNode != null &&
        Access.accessTreeMetacatServerName.compareTo(Morpho.thisStaticInstance.getDataONEDataStoreService().getCNodeURL()) == 0) {
      Log.debug(10,
          "Retrieving access information from server cancelled. "
          + "Using the old access tree.");
      displayTree(Access.accessTreeNode);
    } else {
      displayDNPanel();
    }

    setQueryCancelled(true);
  }

  /**
   * Checks if treenode is present - if present, creates a
   * ScrollPane and sends back the scrollpane... otherwise sends back
   * null.
   */

  protected void displayTree(DefaultMutableTreeNode treeNode) {
    accessTreePane = null;

    // clear out any other components on the screen...
    dnField = null;
    middlePanel.removeAll();

    if (treeNode != null) {
      treeTable = new JTreeTable(new AccessTreeModel(treeNode));
      treeTable.getTree().setCellRenderer(new AccessTreeCellRenderer());

      accessTreePane = new JScrollPane(treeTable);
      accessTreePane.setPreferredSize(new java.awt.Dimension(500, 500));

      if (userDN != null) {

        int rowCount = treeTable.getRowCount();
        for (int count = rowCount; count > 0; count--) {
          treeTable.expandIt(count - 1);
        }

        boolean dnFound = false;

        rowCount = treeTable.getRowCount();
        for (int count = 0; count < rowCount && !dnFound; count++) {
          Object o = treeTable.getValueAt(count, 0);
          if (o instanceof Person) {
            Person person = (Person) o;
            if ( person != null && person.getSubject() != null && person.getSubject().getValue() != null &&
                person.getSubject().getValue().compareTo(userDN) == 0) {
              treeTable.setRowSelectionInterval(count, count);
              treeTable.scrollRectToVisible(treeTable.getCellRect( ( (count - 5 >
                  0) ? count - 5 : 0), 0, true));
              dnFound = true;
            }
          } else if (o instanceof Group) {
            Group group = (Group) o;
            if ( group != null && group.getSubject() != null && group.getSubject().getValue() != null && 
                group.getSubject().getValue().compareTo(userDN) == 0) {
              treeTable.setRowSelectionInterval(count, count);
              treeTable.scrollRectToVisible(treeTable.getCellRect( ( (count - 5 >
                  0) ? count - 5 : 0), 0, true));
              dnFound = true;
            }
          }
        }
        if (!dnFound) {
          displayDNPanel();
          return;
        }
      }
    }

    if (accessTreePane != null) {
    	// filter the tree
    	JPanel filterPanel = WidgetFactory.makePanel();
    	final AccessPage accessP = this;
    	final ActionListener searchAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accessP.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					generateAccessTree(false);
					expandTree();
				}
				finally {
					accessP.setCursor(Cursor.getDefaultCursor());
				}
			}
    	};
    	final ActionListener resetAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accessP.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					resetTree();
				}
				finally {
					accessP.setCursor(Cursor.getDefaultCursor());
				}
			}
    	};
    	// add the key listener if we don't already have it
    	if (treeFilterField.getKeyListeners().length == 0) {
	    	treeFilterField.addKeyListener(new KeyAdapter() {
	    		public void keyPressed(KeyEvent e) {
	    			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
	    				searchAction.actionPerformed(null);
	    			}
	    		}
	    		
	    	});
    	}
    	//disable the default button
    	if (this.getRootPane() != null) {
    		this.getRootPane().setDefaultButton(null);
    	}
    	// add a button for executing the filter
    	JButton filterButton = WidgetFactory.makeJButton(
    			Language.getInstance().getMessage("Search"), 
    			searchAction);
    	JButton resetButton = WidgetFactory.makeJButton(
    			Language.getInstance().getMessage("Reset"), 
    			resetAction);
    	filterPanel.add(treeFilterField);
    	filterPanel.add(filterButton);
    	filterPanel.add(resetButton);
    	
    	// add the tree filter
    	middlePanel.add(filterPanel, BorderLayout.NORTH);
    	
    	
      middlePanel.add(accessTreePane, BorderLayout.CENTER);
      middlePanel.add(getAccessControlPanel(true, 
    		  								/*"Refresh the user list..."*/ Language.getInstance().getMessage("AccessPage.RefreshUserList") + " ..."
    		  								),
          BorderLayout.SOUTH);
    } else {
      displayDNPanel();

      return;
    }

    middlePanel.revalidate();
    middlePanel.repaint();

    typeComboBox.setEnabled(true);
    accessComboBox.setEnabled(true);
  }
  
  private boolean containsUserNode(DefaultMutableTreeNode treeNode) {
	 
	  int childCount = treeNode.getChildCount();
	  for (int i = 0; i < childCount; i++) {
		  DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
		  // is this a leaf that we want?
		  if (child.isLeaf()) {
		    if( child.getUserObject() instanceof Person || child.getUserObject() instanceof Group || child.getUserObject() instanceof Subject) {
		      return true;
		    }
			  
			} else {
			  return containsUserNode(child);
		  }
	  }
	  // check this node
	  Object childAccessObject =  treeNode.getUserObject();
	  if (childAccessObject instanceof Person ||childAccessObject instanceof Group || childAccessObject instanceof Subject) {
		  return true;
	  }
	  return false;
	  
  }
  
  private DefaultMutableTreeNode pruneTree(DefaultMutableTreeNode treeNode) {
	  
	  List<DefaultMutableTreeNode> nodesToRemove = new ArrayList<DefaultMutableTreeNode>();
	  Enumeration nodes = treeNode.breadthFirstEnumeration();
	  while (nodes.hasMoreElements()) {
		  DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
		  if (!containsUserNode(node)) {
			  nodesToRemove.add(node);
		  }
	  }
	  for (DefaultMutableTreeNode node: nodesToRemove) {
		  node.removeFromParent();
	  }
	  return treeNode;
  }
  
  private void expandTree() {
	  int rowCount = treeTable.getRowCount();
      for (int count = rowCount; count > 0; count--) {
        treeTable.expandIt(count - 1);
      }
  }
  
  private void resetTree() {
	  treeFilterField.setText(EMPTY_STRING);
	  generateAccessTree(false);
  }

  protected DefaultMutableTreeNode getTreeFromDocument(SubjectInfo subjectInfo) {
    DefaultMutableTreeNode treeNode = null;

    DefaultMutableTreeNode topNode =
        new DefaultMutableTreeNode("Access Tree                        ");
    NodeList nl = null;

    if (subjectInfo != null) {
      //nl = doc.getElementsByTagName("authSystem");

      //if (nl != null) {
    	  // filter using the string given
    	  String filter = treeFilterField.getText();
    	  if (filter != null && filter.length() == 0) {
    		  filter = null;
    	  }
    	  if (filter != null) {
    		  filter = ".*" + filter + ".*";
    	  }
        createSubTree(subjectInfo, topNode, filter);
      //}
      //treeNode = topNode;
      treeNode = pruneTree(topNode);
    }

    if (treeNode != null) {
      Access.accessTreeNode = treeNode;
      Access.accessTreeMetacatServerName = Morpho.thisStaticInstance.getDataONEDataStoreService().getCNodeURL();
    } else {
      Log.debug(1, "Unable to retrieve access tree. "
          + "The old list will be displayed again");
    }

    return Access.accessTreeNode;
  }

  
  
  /*
   * Create a tree from the SubjectInfo project
   */
  private DefaultMutableTreeNode createSubTree(SubjectInfo subjectInfo,
      DefaultMutableTreeNode top, String filter) {
    List<Person> persons = subjectInfo.getPersonList();
    List<Person> filteredPersons = new Vector<Person> ();
    //handle persons
    if(persons != null) {
      if(filter != null && !filter.trim().equals(EMPTY_STRING)) {
        filter = filter.toLowerCase();
        for(Person person : persons) {
          if (person.getFamilyName() != null && person.getFamilyName().toLowerCase().matches(filter)) {
            //family name
            filteredPersons.add(person);
          } else if (person.getSubject() != null && person.getSubject().getValue() != null && person.getSubject().getValue().toLowerCase().matches(filter)) {
            // subject
            filteredPersons.add(person);
          } else {
            //first name
            List<String> givenNames = person.getGivenNameList();
            if(givenNames != null) {
              for(String givenName : givenNames) {
                if(givenName != null && givenName.toLowerCase().matches(filter)) {
                  filteredPersons.add(person);
                  break;
                }
              }
            }
          }
          continue;
        }
      } else {
        filteredPersons = persons;
      }
      Collections.sort(filteredPersons, new PersonFamilyNameComparator());
      //generate nodes
      for(Person person : filteredPersons) {
        AccessTreeNode tempTreeNode = new AccessTreeNode();
        tempTreeNode.setUserObject(person);
        top.add(tempTreeNode);
      }
    }
    //handle groups
    List<Group> groups = subjectInfo.getGroupList();
    List<Group> filteredGroups = new Vector<Group>();
    if(groups != null) {
      if(filter != null && !filter.trim().equals(EMPTY_STRING)) {
        filter = filter.toLowerCase();
        for(Group group: groups) {
          if(group.getGroupName() != null && group.getGroupName().toLowerCase().matches(filter)) {
            filteredGroups.add(group);
          } else if (group.getSubject() != null && group.getSubject().getValue() != null && group.getSubject().getValue().toLowerCase().matches(filter)) {
            filteredGroups.add(group);
          }
          continue;
        }
      } else {
        filteredGroups = groups;
      }
      Collections.sort(filteredGroups, new GroupNameComparator());
      //generate nodes
      for(Group group : filteredGroups) {
        AccessTreeNode tempTreeNode = new AccessTreeNode();
        tempTreeNode.setUserObject(group);
        List<Subject> memberList = group.getHasMemberList();
        if(memberList != null) {
          Collections.sort(memberList);
          for(Subject subject : memberList) {
            AccessTreeNode groupMemberNode = new AccessTreeNode();
            groupMemberNode.setUserObject(subject);
            tempTreeNode.add(groupMemberNode);
          }
        }
        top.add(tempTreeNode);
      }
    }
    return top;
  }

  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {
    if (dnField == null) {

      int[] i = treeTable.getSelectedRows();
      if (i.length == 0) {
        warnLabel.setText(/*"Warning: Invalid input. Please make a selection."*/
                  Language.getInstance().getMessage("Warning") + " : "
                  + Language.getInstance().getMessage("AccessPage.InvalidInput_1") + "! "
                  + Language.getInstance().getMessage("AccessPage.InvalidInput_2")
                  );
        return false;
      }
      for (int j = 0; j < i.length; j++) {
        Object o = treeTable.getValueAt(i[j], 0);
        if (o instanceof Person) {
          Person nodeOb = (Person) o;
          warnLabel.setText(EMPTY_STRING);
          if (userDN != null) {
            userDN = nodeOb.getSubject().getValue();
          }
          return true;
        } else if (o instanceof Group) {
          Group nodeOb = (Group) o;
          warnLabel.setText(EMPTY_STRING);
          if (userDN != null) {
            userDN = nodeOb.getSubject().getValue();
          }
          return true;
        } else if (o instanceof Subject) {
          Subject nodeOb = (Subject) o;
          warnLabel.setText(EMPTY_STRING);
          if (userDN != null) {
            userDN = nodeOb.getValue();
          }
          return true;
        } else {
          warnLabel.setText(
              /*"Warning: Invalid input. Please select a user or a group."*/
            Language.getInstance().getMessage("Warning") + " : "
            + Language.getInstance().getMessage("AccessPage.InvalidInput_1") + " "
            + Language.getInstance().getMessage("AccessPage.InvalidInput_3")
            );
          return false;
        }
      }
    } else {
      if (dnField.getText().trim().compareTo(EMPTY_STRING) != 0) {
        warnLabel.setText(EMPTY_STRING);
        if (userDN != null) {
          userDN = dnField.getText().trim();
        }
        return true;
      } else {
        warnLabel.setText(
            /*"Warning: Distinguished Name field can not be empty."*/
          Language.getInstance().getMessage("Warning") + " : "
          + Language.getInstance().getMessage("DistinguishedName") + " "
          + Language.getInstance().getMessage("AccessPage.DistinguishedNameEmpty") 
          );
      }
    }
    return false;
  }

  /**
   *  @return a List contaiing 2 String elements - one for each column of the
   *  2-col list in which this surrogate is displayed
   *
   */

  public List getSurrogate() {

    List surrogate = new ArrayList();

    // Get the value of the DN
    if (dnField == null) {
      if (treeTable != null) {
        int[] i = treeTable.getSelectedRows();
        for (int j = 0; j < i.length; j++) {
          Object o = treeTable.getValueAt(i[j], 0);
          
            
            if (o instanceof Group) {
              Group nodeOb = (Group) o;
              List sub_surrogate = new ArrayList();
              sub_surrogate.add(" " + nodeOb.getGroupName().trim());
              sub_surrogate.add(" " + EMPTY_STRING);
              sub_surrogate.add(" " + EMPTY_STRING);
              // Get access given to the user
              sub_surrogate.add(" " + userAccessType + "   " +
                  userAccess.trim());
              surrogate.add(sub_surrogate);
            } else if (o instanceof Person) {
              Person nodeOb = (Person) o;
              List sub_surrogate = new ArrayList();
              sub_surrogate.add(" " + getPersonName(nodeOb));
              sub_surrogate.add(EMPTY_STRING);
              if (nodeOb.getEmailList() != null && nodeOb.getEmailList().size() >0 && nodeOb.getEmail(0) != null &&
                  nodeOb.getEmail(0).compareTo(EMPTY_STRING) != 0) {
                sub_surrogate.add(" " + nodeOb.getEmail(0).trim());
              } else {
                sub_surrogate.add(EMPTY_STRING);
              }
              // Get access given to the user
              sub_surrogate.add(" " + userAccessType + "   " +
                  userAccess.trim());
              surrogate.add(sub_surrogate);
            } else if (o instanceof Subject) {
              Subject nodeOb = (Subject) o;
              List sub_surrogate = new ArrayList();
              sub_surrogate.add(" " + nodeOb.getValue().trim());
              sub_surrogate.add(EMPTY_STRING);
              sub_surrogate.add(EMPTY_STRING);
              // Get access given to the user
              sub_surrogate.add(" " + userAccessType + "   " +
                  userAccess.trim());
              surrogate.add(sub_surrogate);
            }
          
        }//for
      }
    } else {
      List sub_surrogate = new ArrayList();

      if (userName == null) {
        String userOrg = dnField.getText().trim();
        if (userOrg != null && userOrg.indexOf("o=") > 0) {
          userOrg = userOrg.substring(userOrg.indexOf("o=") + 2);
          userOrg = userOrg.substring(0, userOrg.indexOf(","));
        } else {
          userOrg = EMPTY_STRING;
        }
        sub_surrogate.add(" " + dnField.getText().trim());
        sub_surrogate.add(" " + userOrg);
        sub_surrogate.add(" ");
      } else {
        sub_surrogate.add(userName);
        sub_surrogate.add(" " + userOrg);
        sub_surrogate.add(userEmail);
      }
      // Get access given to the user
      sub_surrogate.add(" " + userAccessType + "   " + userAccess.trim());
      surrogate.add(sub_surrogate);
    }

    return surrogate;
  }

  /**
   *  gets the Map object that contains all the key/value paired
   *
   *  @param    xPathRoot the string xpath to which this dialog's xpaths will be
   *            appended when making name/value pairs.  For example, in the
   *            xpath: /eml:eml/dataset/keywordSet[2]/keywordThesaurus, the
   *            root would be /eml:eml/dataset/keywordSet[2]
   *            NOTE - MUST NOT END WITH A SLASH, BUT MAY END WITH AN INDEX IN
   *            SQUARE BRACKETS []
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular wizard page
   */
  private OrderedMap returnMap = new OrderedMap();

  //
  public OrderedMap getPageData() {
    return getPageData(xPathRoot);
  }

  public OrderedMap getPageData(String xPathRoot) {

    returnMap.clear();

    if (dnField == null) {
      int[] i = treeTable.getSelectedRows();
      for (int j = 0; j < i.length; j++) {
        Object o = treeTable.getValueAt(i[j], 0);
        if (o instanceof Person) {
          Person nodeOb = (Person) o;
          returnMap.put(xPathRoot + "/principal[" + (j + 1) + "]",
              nodeOb.getSubject().getValue());
        } else if (o instanceof Group) {
          Group nodeOb = (Group) o;
          returnMap.put(xPathRoot + "/principal[" + (j + 1) + "]",
              nodeOb.getSubject().getValue());
        } else if (o instanceof Subject) {
          Subject nodeOb = (Subject) o;
          returnMap.put(xPathRoot + "/principal[" + (j + 1) + "]",
              nodeOb.getValue());
        }
      }
    } else {
      returnMap.put(xPathRoot + "/principal", dnField.getText().trim());
    }

    if (userAccess.compareTo(accessText[0]) == 0) {
      returnMap.put(xPathRoot + "/permission", "read");
    } else if (userAccess.compareTo(accessText[1]) == 0) {
      returnMap.put(xPathRoot + "/permission[1]", "read");
      returnMap.put(xPathRoot + "/permission[2]", "write");
    } else if (userAccess.compareTo(accessText[2]) == 0) {
      returnMap.put(xPathRoot + "/permission[1]", "read");
      returnMap.put(xPathRoot + "/permission[2]", "write");
      returnMap.put(xPathRoot + "/permission[3]", "changePermission");
    } else if (userAccess.compareTo(accessText[3]) == 0) {
      returnMap.put(xPathRoot + "/permission", "all");
    }
    return returnMap;
  }

  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onRewindAction() {
  }

  /**
   *  The action to be executed when the page is loaded
   *  Here, it does nothing because this is just a Panel and not the outer container
   */

  public void onLoadAction() {
	  resetTree();
  }

  /**
   *  gets the unique ID for this wizard page
   *
   *  @return   the unique ID String for this wizard page
   */
  public String getPageID() {
    return this.pageID;
  }

  /**
   *  gets the title for this wizard page
   *
   *  @return   the String title for this wizard page
   */
  public String getTitle() {
    return title;
  }

  /**
   *  gets the subtitle for this wizard page
   *
   *  @return   the String subtitle for this wizard page
   */
  public String getSubtitle() {
    return subtitle;
  }

  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID() {
    return this.nextPageID;
  }

  /**
   *  Returns the serial number of the page
   *
   *  @return the serial number of the page
   */
  public String getPageNumber() {
    return pageNumber;
  }

  public boolean isQueryCancelled() {
    return queryCancelled;
  }

  public boolean setPageData(OrderedMap map, String xPathRoot) {
    Log.debug(45,
        "AccessPage.setPageData() called with xPathRoot = " + xPathRoot
        + "\n Map = \n" + map);

    if (xPathRoot != null && xPathRoot.trim().length() > 0) {
      this.xPathRoot = xPathRoot;
    }

    int access = 0;

    if (xPathRoot.indexOf("allow") > -1) {
      userAccessType = accessTypeText[0];
    } else {
      userAccessType = accessTypeText[1];
    }

    List toDeleteList = new ArrayList();
    Iterator keyIt = map.keySet().iterator();
    Object nextXPathObj = null;
    String nextXPath = null;
    Object nextValObj = null;
    String nextVal = null;

    while (keyIt.hasNext()) {

      nextXPathObj = keyIt.next();
      if (nextXPathObj == null) {
        continue;
      }
      nextXPath = (String) nextXPathObj;

      nextValObj = map.get(nextXPathObj);
      nextVal = (nextValObj == null) ? "" : ( (String) nextValObj).trim();

      Log.debug(45, "Access:  nextXPath = " + nextXPath
          + "\n nextVal   = " + nextVal);

      // remove everything up to and including the last occurrence of
      // this.xPathRoot to get relative xpaths, in case we're handling a
      // project elsewhere in the tree...
      nextXPath = nextXPath.substring(nextXPath.lastIndexOf(this.xPathRoot)
          + this.xPathRoot.length() + 1);

      Log.debug(45, "Access: TRIMMED nextXPath   = " + nextXPath);

      if (nextXPath.startsWith("permission") ||
          nextXPath.startsWith("/permission")) {
        if (nextVal.compareTo("read") == 0) {
          access = access | 1;
        } else if (nextVal.compareTo("write") == 0) {
          access = access | 2;
        } else if (nextVal.compareTo("changePermission") == 0) {
          access = access | 4;
        } else if (nextVal.compareTo("all") == 0) {
          access = access | 8;
        } else {
          Log.debug(20, "Unknown access type received in setPageData() in " +
              "AccessPage.java");
        }
        toDeleteList.add(nextXPathObj);
      } else if (nextXPath.startsWith("principal") ||
          nextXPath.startsWith("/principal")) {
        if (userDN == null) {
          userDN = (String) nextValObj;
          ConfigXML accessXML = null;
          try {
            accessXML = new ConfigXML(accessListFilePath);

            Vector username = accessXML.getValuesForPath("username[.='" +
                (String) nextValObj + "']/../name");
            if (username.size() > 0) {
              userName = (String) username.get(0);
            }
            Vector useremail = accessXML.getValuesForPath("username[.='" +
                (String) nextValObj + "']/../email");
            if (useremail.size() > 0) {
              userEmail = (String) useremail.get(0);
            }
            Vector userorg = accessXML.getValuesForPath("username[.='" +
                (String) nextValObj + "']/../organization");
            if (userorg.size() > 0) {
              userOrg = (String) userorg.get(0);
            }

          }
          catch (Exception e) {

          }

        } else {
          Log.debug(10, "AccessPage.setPageData returning FALSE! Principal "
              + "contains multiple DNs. Multiple are not "
              + "supported in Access Wizard screen yet.");
          return false;
        }
        toDeleteList.add(nextXPathObj);
      }
    }

    Log.debug(45, "Access type found to be" + access);
    if (access == 3) {
      userAccess = accessText[1];
    } else if (access == 7) {
      userAccess = accessText[2];
    } else if (access == 8) {
      userAccess = accessText[3];
    }

    //remove entries we have used from map:
    Iterator dlIt = toDeleteList.iterator();
    while (dlIt.hasNext()) {
      map.remove(dlIt.next());
    }

    //if anything left in map, then it included stuff we can't handle...
    boolean returnVal = map.isEmpty();

    if (!returnVal) {
      Log.debug(10,
          "AccessPage.setPageData returning FALSE! Map still contains:"
          + map);
    }

    displayDNPanel();
    return returnVal;
  }
  
  /**
   * Utility method to get person's name (combination of the first name and family name).
   * Empty string will be returned if there is no name can't be found.
   * @param person
   * @return
   */
  public static String getPersonName(Person person) {
    String name = null;
    if (person != null) {
      List<String> firstNames = person.getGivenNameList();
      if (firstNames != null && firstNames.size() >0) {
        name = person.getGivenName(0) +" "+person.getFamilyName();
      } else {
        name = person.getFamilyName();
      }
    }
    if(name == null) {
      name = EMPTY_STRING;
    }
    return name;
  }
}



/**
 * TreeSelectionAction  Class
 *
 */
class TreeSelectionAction
    implements TreeSelectionListener {

  AccessPage accessPage = null;

  public TreeSelectionAction(AccessPage accessPage) {
    this.accessPage = accessPage;
  }

  public void valueChanged(TreeSelectionEvent e) {

    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        accessPage.accessTree.getLastSelectedPathComponent();

    if (node != null &&
        node.getUserObject() instanceof Person) {
        accessPage.dnField.setText( ( (Person) node.
            getUserObject()).
            getSubject().getValue());

        accessPage.typeComboBox.setEnabled(true);
        accessPage.accessComboBox.setEnabled(true);
      
      } else if (node != null &&
            node.getUserObject() instanceof Group) {
            accessPage.dnField.setText( ( (Group) node.
                getUserObject()).
                getSubject().getValue());
            accessPage.typeComboBox.setEnabled(true);
            accessPage.accessComboBox.setEnabled(true);
        

    } else {
      accessPage.dnField.setText("");
    }
  }
}



/**
 * AccessProgressThread class
 *
 */
class AccessProgressThread
    extends ProgressBarThread {

  private AccessPage accessPage = null;

  // Constructor accessPage
  public AccessProgressThread(AccessPage accessPage) {
    super();
    this.accessPage = accessPage;
  }

  public void run() {


    // get the ModalDialog which parent of accessPage shown...
    // the JDialog will be tied to this Dialog
    Component parentDialog = accessPage.getParent();
    while ( (parentDialog != null) && !(parentDialog instanceof ModalDialog)) {
      parentDialog = parentDialog.getParent();
    }
    this.setParentDialog( (JDialog) parentDialog);

    // progress bar will be showing soon ... the access tree node can
    // now be contacted
    super.run();
  }
}


/**
 * QueryNetworkThread
 *
 */
class QueryNetworkThread
    extends Thread {

  AccessPage accessPage;
  SubjectInfo queryResult;

  public QueryNetworkThread(AccessPage accessPage) {
    this.accessPage = accessPage;
  }

  public void run() {

    try {
      queryResult = null;
      //if (morpho.isConnected()) {
      if (Morpho.thisStaticInstance.getDataONEDataStoreService().getNetworkStatus()) {
        String cnURL = null;//use the default in the config.xml
        queryResult = Morpho.thisStaticInstance.getDataONEDataStoreService().getAllIdentityInfo(cnURL);
      }
      if (!accessPage.isQueryCancelled()) {
        accessPage.parseInputStream(queryResult);
      }
    }
    catch (Exception w) {
      Log.debug(10, "Error in retrieving User list from server.");
      Log.debug(45, w.getMessage());

      if (Access.accessTreeNode != null &&
          Access.accessTreeMetacatServerName.compareTo(Morpho.
          thisStaticInstance.getDataONEDataStoreService().getCNodeURL()) == 0) {
        Log.debug(10,
            "Retrieving access information from server failed. "
            + "Using the old access tree.");
        accessPage.displayTree(Access.accessTreeNode);
      } else {
        accessPage.displayDNPanel();
      }
      //// File is not on harddisk and data is not avaiable from
      //// display a dn field to be entered by user...
    }
  }

}
