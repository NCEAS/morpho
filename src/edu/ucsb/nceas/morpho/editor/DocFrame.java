/**
 *        Name: DocFrame.java
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-02-20 16:32:50 $'
 * '$Revision: 1.151 $'
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

package edu.ucsb.nceas.morpho.editor;
import com.arbortext.catalog.*;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.DBValidate;
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.morpho.util.SAXValidate;
import edu.ucsb.nceas.utilities.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import javax.swing.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import org.xml.sax.*;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/**
 * DocFrame is a container for an XML editor which shows combined outline and
 * nested panel views of an XML document. This class uses a DTDParser to 'merge'
 * an existing XML instance with a template created from its DTD. This merging
 * adds optional nodes missing from the original document. Help information and
 * special custom node editors are also loaded from a 'template'.
 *
 * @author   higgins
 */
public class DocFrame extends javax.swing.JFrame
{

  // various global variables
  public DefaultTreeModel treeModel;
  public DefaultMutableTreeNode rootNode;
  public DTDTree dtdtree;
  public JTree tree;


  /**
   *  most recent instance of DocFrame
   *  (for use by specialized editors)
   */
  static DocFrame currentDocFrameInstance = null; 
   
  /**
   *   cached copy of template tree
   */
  static DefaultMutableTreeNode frootNode = null;

  /**
   *   used with cached template to see if new template tree is needed
   */
  static String templateRootName = "";
  
  /**
   *   the DOM node passed in which contains a parsed XML document
   *   to be displayed and edited
   */
  Node docnode = null;

  File openfile = null;
  File file = null;
    
  /** counter for name */
  public static int counter = 0;

  /* flag to indicate whether help, cardinality, CHOICE, SEQUENCE nodes should
   * be stripped when output is created 
   */
  public boolean removeExtraInfoFlag = true;
  
  /* flag to indicate whether XML attribute values should appear as
   * child nodes in the tree 
   */
  public boolean xmlAttributesInTreeFlag = true;
    
  /** number of level of expansion of the initial JTree  */
  int exp_level = 5;

  /**
   * container for a copy of the full tree used when empty nodes are 'trimmed'
   */
  DefaultMutableTreeNode fullTree = null;

  /**
   * flag to indicate whether noInfoNodes should be trimmed when a tree is
   * saved
   */
  boolean trimFlag = true;

  /** The configuration options object reference from the framework */
  ConfigXML config;

  EditorPlugin controller = null;

  /*
   * the string representation of the XML being displayed
   */
  String XMLTextString;

  /*
   * the publicID, if available, or the systemID, if available,
   * or the rootnode name
   */
  String doctype = null;

  String rootnodeName = null;
  String publicIDString = null;
  String systemIDString = null;
  DefaultMutableTreeNode selectedNode;
  StringBuffer sb;
  StringBuffer start;
  Stack tempStack;
  boolean textnode = false;

  Catalog myCatalog;
  String dtdfile;
  int numlevels = 20;
  boolean templateFlag = false;

  JSplitPane DocControlPanel;

  /* treeValueFlag is used to turn off/on the response of the
   * tree to changes in selection. When done by the user, the
   * right-hand display should be updated, but some programatic tree changes 
   * should not trigger update events
   */
  boolean treeValueFlag = true;
  
  /**
   * if true, this flag will cause the display of missing subtrees by
   * merging from the template tree
   * if false, only those subtrees already in the template are displayed
   */
  boolean mergeMissingFlag = false;

  /** determines whether node containing no text are saved when output
   * is written
   */
  boolean emptyFlag = false;

  /** nodeCopy is the 'local clipboard' for node storage 
   *  because it is static it can be shared between multiple object.
   */
  static DefaultMutableTreeNode nodeCopy = null;

  /** trimNodesNotInDTDflag indicates whether nodes not in DTD should be removed */
  boolean trimNodesNotInDTDflag = true;
  /** indicates whether DTD info should be merged*/
  boolean dtdMergeflag = false;

  /** Morpho/Metacat id of the document being displayed */
  String id = null;

  /** location sting from Morpho/Metacat */
  String location = null;

  /** flag used to communicate between the mousedown event handler and
   * the tree selection changed handler; used in setting check boxes
   * in a meaningful manner.
   */   
  boolean treeSelChangedFlag = false;


  /**
   *  declarations for tree popup menus
   */
  javax.swing.JMenuItem menuItem;
  javax.swing.JMenuItem CardmenuItem;
  javax.swing.JMenuItem DeletemenuItem;
  javax.swing.JMenuItem DupmenuItem;
  javax.swing.JMenuItem AttrmenuItem;
  javax.swing.JMenuItem CopymenuItem;
  javax.swing.JMenuItem ReplacemenuItem;
  javax.swing.JMenuItem PastemenuItem;
  javax.swing.JMenuItem AddtextItem;
  javax.swing.JMenuItem NewWindowItem;

  javax.swing.JPanel OutputScrollPanelContainer = new javax.swing.JPanel();
  javax.swing.JScrollPane OutputScrollPanel = new javax.swing.JScrollPane();
  javax.swing.JPanel ControlsPanel = new javax.swing.JPanel();
  javax.swing.JPanel TreeChoicePanel = new javax.swing.JPanel();
  javax.swing.JPanel TreeControlPanel = new javax.swing.JPanel();
  javax.swing.JButton TrimTreeButton = new javax.swing.JButton();
  javax.swing.JButton UntrimTreeButton = new javax.swing.JButton();
  javax.swing.JButton ExpandTreeButton = new javax.swing.JButton();
  javax.swing.JButton ContractTreeButton = new javax.swing.JButton();
  javax.swing.JScrollPane NestedPanelScrollPanel = new javax.swing.JScrollPane();
  javax.swing.JPanel TopPanel = new javax.swing.JPanel();
  javax.swing.JPanel TopLabelPanel = new javax.swing.JPanel();
  javax.swing.JLabel headLabel = new javax.swing.JLabel();
  javax.swing.JLabel logoLabel = new javax.swing.JLabel();
  javax.swing.JPanel ControlPanel = new javax.swing.JPanel();
  javax.swing.JPanel ButtonPanel = new javax.swing.JPanel();
  javax.swing.JButton CancelButton = new javax.swing.JButton();
  javax.swing.JButton OpenButton = new javax.swing.JButton();
  javax.swing.JButton NewButton = new javax.swing.JButton();
  javax.swing.JButton EditingExit = new javax.swing.JButton();
  javax.swing.JPanel NotesPanel = new javax.swing.JPanel();
  javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
  javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
  javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
  javax.swing.JLabel JLabel4 = new javax.swing.JLabel();
  javax.swing.JComboBox choiceCombo = new javax.swing.JComboBox();
  
  //Create the popup menu.
  javax.swing.JPopupMenu popup = new JPopupMenu();
  static Hashtable icons;

  /** A reference to the container framework */
  private Morpho morpho = null;

  /** This constructor builds the contents of the DocFrame Display  */

  public DocFrame()  {
    currentDocFrameInstance = this;
    setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout(0, 0));
    setSize(800, 600);
    // following changes initial window size if using a bigger screen
    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension ddd = tk.getScreenSize();
    if (ddd.width>800) {
      int dh = 8*ddd.height/10;
      int dw = 8*ddd.width/10;
      setSize(dw, dh);
    }
    setVisible(false);
		final DocFrame df = this;
		// Register window listeners
    this.addWindowListener(
      new WindowAdapter() {
                public void windowActivated(WindowEvent e) 
                {
                  Log.debug(50, "Processing window activated event");
								  currentDocFrameInstance = df;
                } 
                public void windowClosing(WindowEvent event)
                {
                 }
                public void windowDeactivated(WindowEvent event)
                {
                }
      });

    ControlsPanel.setLayout(new BorderLayout(0, 0));
    OutputScrollPanelContainer.setLayout(new BorderLayout(0, 0));
    getContentPane().add(OutputScrollPanelContainer);
    JLabel test = new JLabel("Find: ");
    String[] choices = {"eml", "dataset", "access", "creator", "contact", "keywordSet",
            "dataTable", "attributeList", "abstract", "geographicCoverage",
            "temporalCoverage", "taxonomicCoverage", "methods", "project",
            "entityName", "physical", "spatialRaster", "spatialVector"};
    choiceCombo = new JComboBox(choices);
    choiceCombo.setVisible(false);
    choiceCombo.setEditable(true);
    TreeChoicePanel.add(test);
    TreeChoicePanel.add(choiceCombo);
    choiceCombo.setVisible(true);
    TreeControlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
    ControlsPanel.add(BorderLayout.NORTH, TreeControlPanel);
    ControlsPanel.add(BorderLayout.SOUTH, TreeChoicePanel);
		OutputScrollPanelContainer.add(BorderLayout.NORTH, ControlsPanel);
    OutputScrollPanelContainer.add(BorderLayout.CENTER, OutputScrollPanel);
    TrimTreeButton.setText("Trim");
    TrimTreeButton.setActionCommand("Trim");
    TrimTreeButton.setToolTipText("Remove all optional nodes that contain no text.");
		TrimTreeButton.setEnabled(false);
    UntrimTreeButton.setText("Show All");
    UntrimTreeButton.setActionCommand("Show All");
    UntrimTreeButton.setToolTipText("Show all possible elements.");
    UntrimTreeButton.setEnabled(true);
    TreeControlPanel.add(UntrimTreeButton);
    TreeControlPanel.add(TrimTreeButton);
    ExpandTreeButton.setText("+");
    ExpandTreeButton.setActionCommand("+");
    ExpandTreeButton.setToolTipText("Expand Tree levels displayed");
    TreeControlPanel.add(ExpandTreeButton);
    ContractTreeButton.setText("-");
    ContractTreeButton.setActionCommand("-");
    ContractTreeButton.setToolTipText("Contract Tree levels displayed");
    TreeControlPanel.add(ContractTreeButton);
    getContentPane().add(BorderLayout.CENTER, NestedPanelScrollPanel);
    TopPanel.setLayout(new BorderLayout(0, 0));
    getContentPane().add(BorderLayout.NORTH, TopPanel);
    TopLabelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    TopPanel.add(BorderLayout.CENTER, TopLabelPanel);
//    headLabel.setText("Morpho Editor");
    headLabel.setText("Working...");
    TopLabelPanel.add(headLabel);
    TopPanel.add(BorderLayout.EAST, logoLabel);
    ControlPanel.setLayout(new BorderLayout(0, 0));
    getContentPane().add(BorderLayout.SOUTH, ControlPanel);
    ButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    ControlPanel.add(BorderLayout.EAST, ButtonPanel);
    NewButton.setText("New EML2");
    NewButton.setActionCommand("Open");

    NewButton.setVisible(false);
    
    ButtonPanel.add(NewButton);
    OpenButton.setText("Open");
    OpenButton.setActionCommand("Open");

    OpenButton.setVisible(false);  
    
    ButtonPanel.add(OpenButton);
//    CancelButton.setText("Cancel");
    CancelButton.setText("Revert");
    CancelButton.setActionCommand("Cancel");
    ButtonPanel.add(CancelButton);
//    EditingExit.setText("Save Changes");
    EditingExit.setText("Finish");
    EditingExit.setActionCommand("Finish");
    ButtonPanel.add(EditingExit);
    NotesPanel.setLayout(new GridLayout(2, 2, 6, 0));
    ControlPanel.add(BorderLayout.WEST, NotesPanel);
    JLabel1.setText("required; repeatable (ONE to MANY)");
    NotesPanel.add(JLabel1);
    JLabel1.setBackground(java.awt.Color.black);
    JLabel1.setForeground(java.awt.Color.black);
    JLabel1.setFont(new Font("Dialog", Font.PLAIN, 10));
    JLabel2.setText("required (ONE)");
    NotesPanel.add(JLabel2);
    JLabel2.setBackground(java.awt.Color.black);
    JLabel2.setForeground(java.awt.Color.black);
    JLabel2.setFont(new Font("Dialog", Font.PLAIN, 10));
    JLabel3.setText("optional; repeatable (ZERO to MANY)");
    NotesPanel.add(JLabel3);
    JLabel3.setBackground(java.awt.Color.black);
    JLabel3.setForeground(java.awt.Color.black);
    JLabel3.setFont(new Font("Dialog", Font.PLAIN, 10));
    JLabel4.setText("optional (ZERO to ONE)");
    NotesPanel.add(JLabel4);
    JLabel4.setBackground(java.awt.Color.black);
    JLabel4.setForeground(java.awt.Color.black);
    JLabel4.setFont(new Font("Dialog", Font.PLAIN, 10));
    if (!icons.containsKey("blue.gif")) {
      ImageIcon plus = new ImageIcon(getClass().getResource("blue.gif"));
      icons.put("blue.gif", plus);
    }
    JLabel1.setIcon((ImageIcon)icons.get("blue.gif"));

    if (!icons.containsKey("red.gif")) {
      ImageIcon square = new ImageIcon(getClass().getResource("red.gif"));
      icons.put("red.gif", square);
    }
    JLabel2.setIcon((ImageIcon)icons.get("red.gif"));

    if (!icons.containsKey("green.gif")) {
      ImageIcon astr = new ImageIcon(getClass().getResource("green.gif"));
      icons.put("green.gif", astr);
    }
    JLabel3.setIcon((ImageIcon)icons.get("green.gif"));

    if (!icons.containsKey("yellow.gif")) {
      ImageIcon qu = new ImageIcon(getClass().getResource("yellow.gif"));
      icons.put("yellow.gif", qu);
    }
    JLabel4.setIcon((ImageIcon)icons.get("yellow.gif"));

    if (!icons.containsKey("smallheader-bg.gif")) {
      ImageIcon head = new ImageIcon(getClass().getResource("smallheader-bg.gif"));
      icons.put("smallheader-bg.gif", head);
    }
    if (!icons.containsKey("logo-icon.gif")) {
      ImageIcon logoIcon = new ImageIcon(getClass().getResource("logo-icon.gif"));
      icons.put("logo-icon.gif", logoIcon);
    }
    if (!icons.containsKey("Btfly4.gif")) {
      ImageIcon flapping = new ImageIcon(getClass().getResource("Btfly4.gif"));
      icons.put("Btfly4.gif", flapping);
    }

    logoLabel.setIcon((ImageIcon)icons.get("Btfly4.gif"));
    headLabel.setIcon((ImageIcon)icons.get("smallheader-bg.gif"));

    headLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    headLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headLabel.setAlignmentY(Component.LEFT_ALIGNMENT);
    headLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    headLabel.setForeground(Color.black);
    headLabel.setFont(new Font("Dialog", Font.BOLD, 14));
    headLabel.setBorder(BorderFactory.createLoweredBevelBorder());

    DocControlPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT
                , OutputScrollPanelContainer, NestedPanelScrollPanel);

    DocControlPanel.setOneTouchExpandable(true);
    DocControlPanel.setDividerLocation(225);
    getContentPane().add(BorderLayout.CENTER, DocControlPanel);

    //{{INIT_MENUS
    CardmenuItem = new JMenuItem("One Element Allowed");

    popup.add(CardmenuItem);
    popup.add(new JSeparator());
    DupmenuItem = new JMenuItem("Duplicate");
    popup.add(DupmenuItem);
    DeletemenuItem = new JMenuItem("Delete");
    popup.add(DeletemenuItem);
    popup.add(new JSeparator());
    AttrmenuItem = new JMenuItem("Edit Attributes");
    popup.add(AttrmenuItem);
//  this menu item is not added to menu because all attributes should now appear in the tree
//  leave code for possibile use in debugging (to see attributes trimmed from tree)
    popup.add(new JSeparator());
    AddtextItem = new JMenuItem("Add Text");
    AddtextItem.setEnabled(false);
    popup.add(AddtextItem);
    popup.add(new JSeparator());
    CopymenuItem = new JMenuItem("Copy Node & Children");
    popup.add(CopymenuItem);
    ReplacemenuItem = new JMenuItem("Replace Selected Node from Clipboard");
    ReplacemenuItem.setEnabled(false);
    popup.add(ReplacemenuItem);
    PastemenuItem = new JMenuItem("Paste as Sibling to Selected Node");
    popup.add(PastemenuItem);
    NewWindowItem = new JMenuItem("Open New Editor");
    popup.add(new JSeparator());
    popup.add(NewWindowItem);
    
    //  REGISTER_LISTENERS
    SymAction lSymAction = new SymAction();
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);
    choiceCombo.addActionListener(lSymAction);
    EditingExit.addActionListener(lSymAction);
    CancelButton.addActionListener(lSymAction);
    OpenButton.addActionListener(lSymAction);
    NewButton.addActionListener(lSymAction);
    TrimTreeButton.addActionListener(lSymAction);
    UntrimTreeButton.addActionListener(lSymAction);
    ExpandTreeButton.addActionListener(lSymAction);
    ContractTreeButton.addActionListener(lSymAction);
    SymComponent aSymComponent = new SymComponent();
    this.addComponentListener(aSymComponent);

    DeletemenuItem.addActionListener(lSymAction);
    DupmenuItem.addActionListener(lSymAction);
    AttrmenuItem.addActionListener(lSymAction);
    CopymenuItem.addActionListener(lSymAction);
    ReplacemenuItem.addActionListener(lSymAction);
    PastemenuItem.addActionListener(lSymAction);
    AddtextItem.addActionListener(lSymAction);
    NewWindowItem.addActionListener(lSymAction);
    //Create the popup menu.
    javax.swing.JPopupMenu popup = new JPopupMenu();

    rootNode = newNode("Morpho Editor");
    treeModel = new DefaultTreeModel(rootNode);

    tree = new JTree(treeModel);
    OutputScrollPanel.getViewport().add(tree);
    tree.setCellRenderer(new XMLTreeCellRenderer());

    tree.setShowsRootHandles(true);
    tree.setEditable(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setShowsRootHandles(true);
    tree.putClientProperty("JTree.lineStyle", "Angled");

    SymTreeSelection lSymTreeSelection = new SymTreeSelection();
    tree.addTreeSelectionListener(lSymTreeSelection);

    MouseListener popupListener = new PopupListener();
    tree.addMouseListener(popupListener);
    validate();
    setLocation(50, 50);
    setVisible(true);
    Graphics g = getGraphics();
    paint(g);
    g.dispose();
  }

  /**
   * Constructor which adds a title string to the Frame
   *
   * @param sTitle  Window title string
   */
  public DocFrame(String sTitle)
  {
    this();
    setTitle(sTitle);
  }

  /**
   * Constructor which adds a title and passes the xml to display as a string;
   * puts XML into tree
   *
   * @param sTitle   title string
   * @param doctext  XML document as a string
   */
   public DocFrame(String sTitle, String doctext)
   {
     this();
     setTitle(sTitle);
   }

  /**
   * Constructor for the DocFrame object which pass the xml as a File
   *
   * @param file  Description of Parameter
   */
   public DocFrame(File file)
   {
     this();
     this.file = file;
     try {
       BufferedReader in = new BufferedReader(new FileReader(file));
       StringWriter out = new StringWriter();
       int c;
       while ((c = in.read()) != -1) {
         out.write(c);
       }
       in.close();
       out.flush();
       out.close();
       System.out.println("Finished reading input file!");
       String fileString = out.toString();
       initDoc(null, fileString);
     } catch (Exception e) {
       System.out.println("Problem reading input file!");
     }
   }

  /**
   * This constructor actual handles the creation of a tree and panel for
   * displaying and editing the information is an XML document, as represented
   * in the String 'doctext'
   *
   * @param morpho   the main morpho object controller
   * @param sTitle   window title string
   * @param doctext  xml document as a string
   * @param flag     only the template based on the DocType is displayed 
   * (i.e. no merging with existing data) For use in creating new docs.
   */
  public DocFrame(Morpho morpho, String sTitle, String doctext, boolean flag)
  {
    this();
    setTitle(sTitle);
    XMLTextString = doctext;
    templateFlag = flag;
  }

  /**
   * this version of the constructor is needed so that each DocFrame can
   * 'remember' the id and location parameters used to create it
   *
   * @param morpho    the main morpho object controller
   * @param sTitle    window title string
   * @param doctext   xml document as a string
   * @param id        document id
   * @param location  i.e. local or metacat
   */
  public DocFrame(Morpho morpho, String sTitle, String doctext, String id, String location)
  {
    this(morpho, sTitle, doctext, false);
    if (id != null) {
      setTitle("Morpho Editor");
      setName("Morpho Editor" + counter + ":" + id);
    }
    this.id = id;
    this.location = location;
  }

  /**
   * this version of the constructor is needed so that each DocFrame can
   * 'remember' the id and location parameters used to create it; includes
   * template flag
   *
   * @param morpho    the main morpho object controller
   * @param sTitle    window title string
   * @param doctext   xml document as a string
   * @param id        document id
   * @param location  i.e. local or metacat
   * @param templFlag  only the template based on the DocType is displayed 
   * (i.e. no merging with existing data) For use in creating new docs.
   */
  public DocFrame(Morpho morpho, String sTitle, String doctext, String id, String location, boolean templFlag)
  {
    this(morpho, sTitle, doctext, templFlag);
    if (id != null) {
      setTitle("Morpho Editor");
      setName("Morpho Editor" + counter + ":" + id);
    }
    this.id = id;
    this.location = location;
  }


  /**
   * this version allows one to create a new DocFrame and set the initially
   * selected nodename/nodetext
   *
   * @param morpho    the main morpho object controller
   * @param sTitle    window title string
   * @param doctext   xml document as a string
   * @param id        document id
   * @param location  i.e. local or metacat
   * @param nodeName   node name
   * @param nodeValue  value of node name
   */
  public DocFrame(Morpho morpho, String sTitle, String doctext, String id, String location,
            String nodeName, String nodeValue)
  {
    this(morpho, sTitle, doctext, id, location);
    if (nodeValue != null) {
      selectMatchingNode(rootNode, nodeName, nodeValue);
    } else {
      selectMatchingNode(rootNode, nodeName);
    }
  }

  /**
   * Sets the File attribute of the DocFrame object
   *
   * @param f  The new File value
   */
  public void setFile(File f)
  {
    file = f;
  }

  /**
   * Sets the Doctype attribute of the DocFrame object
   *
   * @param doctype  The new Doctype value
   */
  public void setDoctype(String doctype)
  {
    this.doctype = doctype;
  }

  /**
   * Sets the Visible attribute of the DocFrame object
   *
   * @param b  The new Visible value
   */
  public void setVisible(boolean b)
  {
    if (b) {
      super.setVisible(b);
    }
  }

  /**
   * Sets the TreeValueFlag attribute of the DocFrame object
   *
   * @param flg  The new TreeValueFlag value
   */
  public void setTreeValueFlag(boolean flg)
  {
    treeValueFlag = flg;
  }


  /**
   * Sets the Controller attribute of the DocFrame object
   * The 'controller' is the class used to call the DocFrame
   * editor. It is set here to an EditorPlugin object.
   * Control is passed back to the controller when the
   * editor is finished.
   *
   * @param con  The new Controller value
   */
  public void setController(EditorPlugin con)
  {
    this.controller = con;
  }


  /**
   * Gets the IdString attribute of the DocFrame object
   *
   * @return   The IdString value
   */
  public String getIdString()
  {
    return id;
  }

  /**
   * Gets the LocationString attribute of the DocFrame object
   *
   * @return   The LocationString value
   */
  public String getLocationString()
  {
    return location;
  }

  /**
   * The initialization routine for DocFrame; this
   * method creates a secondary thread where the input
   * XML string is parsed and turned into an editable document.
   *
   *
   * @param finalMorpho  The parent Morpho class
   * @param doctext      xml to be edited
   * @param flag     
   */
  public void initDoc(Morpho finalMorpho, String doctext)
  {
    final Morpho fMorpho = finalMorpho;
    final String fdoctext = doctext;
    final SwingWorker worker =  new SwingWorker()
   {
      public Object construct()
      {
        initDocInner(fMorpho, fdoctext);
        return null;
      }

      //Runs on the event-dispatching thread.
      public void finished()
      {
        setLeafNodes((DefaultMutableTreeNode)treeModel.getRoot());
//        setAllNodesAsSelected(rootNode);  //DFH
        setAttributeNames(rootNode);
        setChoiceNodes(rootNode);
        if (xmlAttributesInTreeFlag) {
          addXMLAttributeNodes(rootNode);
        }
//      setSelectedNodes(rootNode);
        treeModel.reload();
        tree.setModel(treeModel);

        tree.expandRow(1);
        tree.expandRow(2);
        tree.setSelectionRow(0);
        setTitle("Morpho Editor:" + id);
        headLabel.setText("Morpho Editor");
        logoLabel.setIcon((ImageIcon)icons.get("logo-icon.gif"));
        headLabel.setText("Morpho Editor");
       }
    };
     worker.start();
     //required for SwingWorker 3
  }
    
    
  /**
   *  The method for initialization that is executed in the worker
   *  thread. Takes the xml string in doctext and initilizes it for editing
   *
   */
  void initDocInner(Morpho morpho, String doctext)
  {
    DefaultMutableTreeNode frootNode = null;
    this.morpho = morpho;
    counter++;
    setName("Morpho Editor" + counter);
    XMLTextString = doctext;
    // the following line put the xml instance into a JTree
    rootNode = newNode("Morpho Editor");
    treeModel = new DefaultTreeModel(rootNode);
    putXMLintoTree(treeModel, XMLTextString);
    NodeInfo ni = (NodeInfo)(((DefaultMutableTreeNode)(treeModel.getRoot())).getUserObject());
    
    // if templateFlag is true, don't bother merging the instance
    // with the template; reset the flag for next time
    if (templateFlag) {
      templateFlag = false;
      rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
      setAttributeNames(rootNode);
      setChoiceNodes(rootNode);
      setAllNodesAsSelected(rootNode);
      setSelectedNodes(rootNode);
      return;
    }
    // now want to possibly merge the input document with a formatting/template document
    // and set the 'editor' and 'help' fields for each node
    // use the root node name as a key
    rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
    // the next line sets all the nodes from the instance as selected
    // this is for initial CHOICES in the merged tree
    setAllNodesAsSelected(rootNode);  //DFH
    String rootname = ((NodeInfo)rootNode.getUserObject()).getName();
    // arbitrary assumption that the formatting document has the rootname +
    // ".xml" as a file name; the formatting document is XML with the same
    // tree structure as the document being formatted; 'help' and 'editor' attributes
    // are used to set help and editor strings for nodes
    rootname = rootname + ".xml";
    frootNode = new DefaultMutableTreeNode("froot");
    DefaultTreeModel ftreeModel = new DefaultTreeModel(frootNode);
    String fXMLString = "";
    boolean formatflag = true;

    try {
      ClassLoader cl = this.getClass().getClassLoader();
// next 2 lines check for templates inside jar files; at least temporarily
// changed to look in lib directory to make it easier for user to customize      
//      BufferedReader in = new BufferedReader(new InputStreamReader(
//                        cl.getResourceAsStream(rootname)));
      BufferedReader in = new BufferedReader(new FileReader(
                        "./lib/"+rootname));
      StringWriter out = new StringWriter();
      int c;
      while ((c = in.read()) != -1) { out.write(c);}
      in.close();
      out.flush();
      out.close();
      fXMLString = out.toString();
    
    // if catch is called, then we don't have a valid template
    } catch (Exception e) {formatflag = false;}
    if (formatflag) {
      // put the template/formatting xml into a tree
      putXMLintoTree(ftreeModel, fXMLString);
      frootNode = (DefaultMutableTreeNode)ftreeModel.getRoot();
      // formatting info has now been put into a JTree which is merged with
      // the previously created document tree
      // first remove all the nodes with visLevel>0 to simplify the display
      // (the '0' value should be a parameter)

			// remove 'references' nodes from template
      removeAllReferences(frootNode);
			
      removeNodesVisLevel(frootNode, 0);//
      
      treeUnion(rootNode, frootNode);
    }
    
    // if the document instance has a DTD, the DTD is parsed
    // and info from the result is merged into the tree
    // DFH left over from earlier versions where DTD is dynamically parsed
    // DFH if template has all DTD schema info, the is not needed
    // DFH currently skipped by the dtdMergeFlag settin
    if (dtdMergeflag) {
      if (dtdfile != null) {
        dtdtree = new DTDTree(dtdfile);
        dtdtree.setRootElementName(rootnodeName);
        dtdtree.parseDTD();

        rootNode = (DefaultMutableTreeNode)treeModel.getRoot();

        // the treeUnion method will 'merge' the input document with
        // a template XML document created using the DTD parser from the DTD doc
        if (!templateFlag) {
          // this is the second merge that can be avoided if all schema info
          // is included in the template
          treeUnion(rootNode, dtdtree.rootNode);

          // treeTrim will remove nodes in the input that are not in the DTD
          // remove the following line if this is not wanted
          if (trimNodesNotInDTDflag) {
            treeTrim(rootNode, dtdtree.rootNode);
          }
        }
      }
    }
    
    setAttributeNames(rootNode);
    setChoiceNodes(rootNode);
    setAllNodesAsSelected(rootNode);
    setSelectedNodes(rootNode);
  }

  /*
   *  this class initializes the editor from a DOM representation
   *  of an XML document rather than a string.
   */
  public void initDoc(Morpho morpho, Node docnode, String id, String loc)
  {
    this.id = id;
    this.location = loc;
    this.docnode = docnode;
    setName("Morpho Editor");
    treeModel = putDOMintoTree(docnode);
    rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
    setAllNodesAsSelected(rootNode);
    // if templateFlag is true, don't bother merging the instance
    // with the template; reset the flag for next time
    if (templateFlag) {
      templateFlag = false;
      rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
      setAttributeNames(rootNode);
      setChoiceNodes(rootNode);
      setAllNodesAsSelected(rootNode);
      setSelectedNodes(rootNode);
      
      if (xmlAttributesInTreeFlag) {
        addXMLAttributeNodes(rootNode);
      }
      
      treeModel.reload();
      tree.setModel(treeModel);

      tree.expandRow(1);
      tree.setSelectionRow(0);
      return;
    }
    boolean formatflag = true;

    rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
    String rname = ((NodeInfo)rootNode.getUserObject()).getName();
         // check for changes in the root name indicating need for new template
   if((frootNode==null)||(!rname.equals(templateRootName))) { 
    // now want to possibly merge the input document with a formatting/template document
    // and set the 'editor' and 'help' fields for each node
    // use the root node name as a key
    rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
    // the next line sets all the nodes from the instance as selected
    // this is for initial CHOICES in the merged tree
    String rootname = ((NodeInfo)rootNode.getUserObject()).getName();
    templateRootName = rootname;
    // arbitrary assumption that the formatting document has the rootname +
    // ".xml" as a file name; the formatting document is XML with the same
    // tree structure as the document being formatted; 'help' and 'editor' attributes
    // are used to set help and editor strings for nodes
    rootname = rootname + ".xml";
    frootNode = new DefaultMutableTreeNode("froot");
    String fXMLString = "";

    try {
      ClassLoader cl = this.getClass().getClassLoader();
// next 2 lines check for templates inside jar files; at least temporarily
// changed to look in lib directory to make it easier for user to customize      
//      BufferedReader in = new BufferedReader(new InputStreamReader(
//                        cl.getResourceAsStream(rootname)));
      BufferedReader in = new BufferedReader(new FileReader(
                        "./lib/"+rootname));
      StringWriter out = new StringWriter();
      int c;
      while ((c = in.read()) != -1) { out.write(c);}
      in.close();
      out.flush();
      out.close();
      fXMLString = out.toString();

      // put the template/formatting xml into a tree
      DefaultTreeModel ftreeModel = new DefaultTreeModel(frootNode);
      putXMLintoTree(ftreeModel, fXMLString);
      frootNode = (DefaultMutableTreeNode)ftreeModel.getRoot();
      // formatting info has now been put into a JTree which is merged with
      // the previously created document tree
      // first remove all the nodes with visLevel>0 to simplify the display
      // (the '0' value should be a parameter)
      removeNodesVisLevel(frootNode, 0);//
			
			// remove 'references' nodes from template
      removeAllReferences(frootNode);
		 
    // if catch is called, then we don't have a valid template
    } catch (Exception e) {
      formatflag = false;
      frootNode = null;
    }
   } // this code block is skippped when frootNode is not null !
   
    if (formatflag) {
      
      treeUnion(rootNode, frootNode);

      setAttributeNames(rootNode);
      setChoiceNodes(rootNode);
      setSelectedNodes(rootNode);
      setLeafNodes(rootNode);
      
      if (xmlAttributesInTreeFlag) {
        addXMLAttributeNodes(rootNode);
      }
    }
      
    treeModel.reload();
    tree.setModel(treeModel);

    tree.expandRow(1);
//    tree.expandRow(2);
    tree.setSelectionRow(0);
    headLabel.setText("Morpho Editor");
    logoLabel.setIcon((ImageIcon)icons.get("logo-icon.gif"));
  }

  /*
   *  this class initializes the editor from a DOM Document
   *  rather than a string. Document is simply used to get the
   *  root node
   */
  public void initDoc(Morpho morpho, Document doc, String id, String loc) 
  {
    Node docnode = doc.getDocumentElement() ;
    initDoc(morpho, docnode, id, loc);
  }
  
  /**
   *  This method will reset the treeModel rootnode, thus changing
   *  the node displayed at the top of the tree
   *
   *  NOTE: this only works if the treemodel was created from
   *  a DOM since it uses XPath to search the DOM and then
   *  looks up the equivalent JTree node
   */
  public void setTopOfTree(Node rootNode, String path) {
    Node nd = null;
    DOMTree.Model model = (DOMTree.Model)treeModel;
    try{
      nd = XMLUtilities.getNodeWithXPath(rootNode, path);
    } catch (Exception w) {Log.debug(1,"Exception evaluation path");}
    if (nd!=null) {
      DefaultMutableTreeNode tn = (DefaultMutableTreeNode)model.getInvNode(nd);
      treeModel.setRoot(tn);
      treeModel.reload();
      tree.setModel(treeModel);

      tree.expandRow(1);
      tree.setSelectionRow(0);

    }
  }

  /**
   *  This method will reset the treeModel rootnode, thus changing
   *  the node displayed at the top of the tree
   *
   *  'name' is name of new node to be at top of tree
   */
  public void setTopOfTree(DefaultMutableTreeNode rootTreeNode, String name) {
    DefaultMutableTreeNode nd = null;
    DefaultMutableTreeNode tn = null;
    Enumeration enum = rootTreeNode.breadthFirstEnumeration();
    while (enum.hasMoreElements()) {
      nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
      if (nodeName.equals(name)) {
        tn = nd;
        break;
      }
    }
    if (tn!=null) {
      treeModel.setRoot(tn);
      treeModel.reload();
      tree.setModel(treeModel);
      tree.expandRow(1);
      tree.setSelectionRow(0);
    }
  }

  /**
   *  Searches the tree for a node that contains the 'name'
   *  Then selects the node found
   */
   private void findNode(DefaultMutableTreeNode treeNode, String name) {
    Enumeration enum = treeNode.preorderEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      String nodeName = (ni.getName()).trim();
      if (nodeName.indexOf(name)>-1) {
        Object[] path = nd.getPath();
        TreePath tp = new TreePath(path);
        tree.scrollPathToVisible(tp);
        tree.makeVisible(tp);
        tree.setSelectionPath(tp);
        return;
      }
    }
    // no node was found
    String msg = "Sorry, could not locate a node containing '"+name+"'";
    JOptionPane.showMessageDialog(this, msg, "alert", JOptionPane.INFORMATION_MESSAGE);
  }     
  
  /**
   * Creates a new DefaultMutableTreeNode with the special
   * NodeInfo userObject used here
   *
   * @param name  name of the new node
   * @return      DefaultMutableTreeNode with new NodeInfo userObject
   */
  DefaultMutableTreeNode newNode(Object name)
  {
    NodeInfo ni = new NodeInfo(name.toString());
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(ni);
    return node;
  }


  /**
   * Uses serialization to make a complete copy of a node
   * and all its decendents.
   * The copy is made in memeory
   *
   * @param node  The node to clode
   * @return      a deep copy of node
   */
  DefaultMutableTreeNode deepNodeCopy(DefaultMutableTreeNode node)
  {
    if (node == null) {
      Log.debug(20, "Attempt to clone a null node!");
      return null;
    }
    DefaultMutableTreeNode newnode = null;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream s = new ObjectOutputStream(out);
      s.writeObject(node);
      s.flush();

      // now read it
      ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
      ObjectInputStream os = new ObjectInputStream(in);
      newnode = (DefaultMutableTreeNode)os.readObject();
    } catch (Exception e) {
      Log.debug(20, "Exception in creating copy of node!");
    }
    return newnode;
  }

  /**
   * Uses serialization to make a complete copy of a node
   * and all its decendents.
   * The copy is written to a file called "treeNodeFile.ser"
   *
   * @param node  The node to clode
   */
  void deepNodeCopyFile(DefaultMutableTreeNode node)
  {
    if (node == null) {
      Log.debug(20, "Attempt to clone a null node!");
    }
    DefaultMutableTreeNode newnode = null;
    try {
      File fl = new File("treeNodeFile.ser");
      FileOutputStream out = new FileOutputStream(fl);
      ObjectOutputStream s = new ObjectOutputStream(out);
      s.writeObject(node);
      s.flush();

    } catch (Exception e) {
      Log.debug(20, "Exception in creating copy of node!");
    }
  }

  /**
   * reads the serialized clone of a node
   *
   * @param filename  file name where serialzized copy is stored
   * @return        a node
   */
  DefaultMutableTreeNode readDeepNodeCopyFile(String filename)
  {
    DefaultMutableTreeNode node = null;
    try {
      FileInputStream in = new FileInputStream(filename);
      ObjectInputStream os = new ObjectInputStream(in);
      node = (DefaultMutableTreeNode)os.readObject();
    } catch (Exception e) {
      return null;
    }
    return node;
  }

  /**
   * usual Main method
   *
   * @param args  not used
   */
  public static void main(String args[])
  {
    try{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
    catch (Exception e) {}
    DocFrame df = new DocFrame();
    df.setVisible(true);
    // first create a DOM
    Node domnode = null;
    try{
      domnode = XMLUtilities.getXMLAsDOMTreeRootNode("/test.xml");
    }
    catch (Exception e) {Log.debug(4,"Problem in creating DOM!"+e);}
    // then display it
    df.initDoc(null, domnode, null, null);
  }

  
  /*
   * adds a #PCDATA node to leaf nodes
   */
  void setLeafNodes(DefaultMutableTreeNode node) {
    DefaultMutableTreeNode parentNode = null;
    DefaultMutableTreeNode curNode = node.getFirstLeaf();
    Vector leafs = new Vector();
    leafs.addElement(curNode);
    while (curNode != null) {
    curNode = curNode.getNextLeaf();
      if (curNode != null) {
      leafs.addElement(curNode);
      }
    }
    Enumeration enum = leafs.elements();
    while (enum.hasMoreElements()) {
      parentNode = (DefaultMutableTreeNode)enum.nextElement();
      if (!(((NodeInfo)parentNode.getUserObject()).getName().equals("#PCDATA"))){
		    NodeInfo ni = new NodeInfo("#PCDATA");
        ni.setPCValue(" ");
		    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (ni);
        parentNode.add(newNode);
      }
    }
  }

  /**
   * Sets the SelectedNodes attribute of nodes in the tree.
   * makes sure that nodes with text data in leaves is 'selected' if a
   * choice node appears in path to root
   *
   * @param node  The new SelectedNodes value
   */
  void setSelectedNodes(DefaultMutableTreeNode node)
  {
    DefaultMutableTreeNode parentNode = null;
    DefaultMutableTreeNode tempNode = null;
    DefaultMutableTreeNode curNode = node.getFirstLeaf();
    Vector leafs = new Vector();
    leafs.addElement(curNode);
    while (curNode != null) {
      curNode = curNode.getNextLeaf();
      if (curNode != null) {
        leafs.addElement(curNode);
      }
    }
    Enumeration enum = leafs.elements();
    while (enum.hasMoreElements()) {
      curNode = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)curNode.getUserObject();
      if (ni.name.equals("#PCDATA")) {
          // is a text node
        String pcdata = ni.getPCValue();
        if (pcdata.trim().length() > 0) {
          // has non-blank text data
          parentNode = (DefaultMutableTreeNode)curNode.getParent();
          // first build Vector of nodes from current leaf to root
          Vector path2root = new Vector();
          path2root.addElement(curNode);
          while (parentNode != null) {
            path2root.addElement(parentNode);
            parentNode = (DefaultMutableTreeNode)parentNode.getParent();
          }
          // now go from the root toward the leaf, 
          // setting selected nodes
          DefaultMutableTreeNode cNode;
          for (int i = path2root.size() - 1; i > -1; i--) {
            cNode = (DefaultMutableTreeNode)path2root.elementAt(i);
            NodeInfo cni = (NodeInfo)cNode.getUserObject();
            if (cni.isChoice()) {
//              cni.setSelected(true);
              for (Enumeration eee = (cNode.getParent()).children();eee.hasMoreElements(); ) {
                DefaultMutableTreeNode nnn = (DefaultMutableTreeNode)eee.nextElement();
                NodeInfo ni1 = (NodeInfo)nnn.getUserObject();
                if ((ni1.getName().equals(cni.getName()))||
                  (ni1.getName().startsWith("attribute-")))
                     // 'attribute' check needed for eml2
                {
                  ni1.setSelected(true);
                } else {
                  if (cni.isCheckbox() && hasNonEmptyTextLeaves(nnn)) {
                       ni1.setSelected(true);
                  } else {
                    ni1.setSelected(false);
                  }
                }
              }
            }
          }
        }
        // end 'if (pcdata...'
      }
    }
  }

  /*
   * returns a Vector containing all the nodes in a Vector that match 'match'
   * Gets the Matches attribute of the DocFrame object
   *
   * @param match  Description of Parameter
   * @param vec  Description of Parameter
   * @return     The Matches vector
   */
  Vector getMatches(DefaultMutableTreeNode match, Vector vec)
  {
    Vector matches = new Vector();
    Enumeration enum = vec.elements();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode tn = (DefaultMutableTreeNode)enum.nextElement();
      if (compareNodes(tn, match)) {
        matches.addElement(tn);
      }
    }
    return matches;
  }


  /**
   * reads xml and puts it into a JTree using a 
   * SAX parser
   *
   * @param tm       treemodel where XML tree is placed
   * @param xmlText  string where xml is located
   */
  void putXMLintoTree(DefaultTreeModel tm, String xmlText)
  {
    if (xmlText != null) {
      CatalogEntityResolver cer = new CatalogEntityResolver();
      config = morpho.getConfiguration();
      String local_dtd_directory = config.getConfigDirectory() + 
          File.separator + config.get("local_dtd_directory", 0);
      String catalogPath = config.get("local_catalog_path", 0);
      String xmlcatalogfile = local_dtd_directory + "/catalog";
      try {
        myCatalog = new Catalog();
        myCatalog.loadSystemCatalogs();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL catalogURL = cl.getResource(catalogPath);
        myCatalog.parseCatalog(catalogURL.toString());
        cer.setCatalog(myCatalog);
      } catch (Exception e) { Log.debug(10, "Problem creating Catalog (772)!\n" +
                        e.getMessage());
      }
      try {
        StringReader sr = new StringReader(xmlText);
        XMLReader parser = null;
        // Get an instance of the parser
        XMLDisplayHandler mh = new XMLDisplayHandler(tm);
        parser = Morpho.createSaxParser((ContentHandler)mh, null);
        parser.setProperty("http://xml.org/sax/properties/lexical-handler", mh);
        parser.setEntityResolver(cer);
        InputSource is = new InputSource(sr);

        parser.parse(is);
        DefaultMutableTreeNode rt = (DefaultMutableTreeNode)tm.getRoot();
        if (mh.getPublicId() != null) {
          doctype = mh.getPublicId();
          publicIDString = doctype;
        } else if (mh.getSystemId() != null) {
           doctype = mh.getSystemId();
        } else {
          doctype = ((NodeInfo)rt.getUserObject()).toString();
        }
        rootnodeName = mh.getDocname();
        if (rootnodeName == null) {
          rootnodeName = ((DefaultMutableTreeNode)tm.getRoot()).toString();
        }
        String temp = myCatalog.resolvePublic(doctype, null);
        if (temp != null) {
          if (temp.startsWith("file:")) {
            temp = temp.substring(5, temp.length());
          }
          dtdfile = temp;
          systemIDString = temp;
        }
      } catch (Exception e) {
        System.err.println(e.toString());
      }
    }
  }

  /**
   * Create a JTree from a DOM and set the TreeModel
   * (uses an example class from Xerces samples)
   *
   * @param tm       treemodel where XML tree is placed
   * @param node  DOM node
   */
  DefaultTreeModel putDOMintoTree(Node node)
  {
    rootnodeName = node.getNodeName();
    Document doc = node.getOwnerDocument();
    JTree domtree = new DOMTree(doc);
    return (DefaultTreeModel)domtree.getModel();
  }
  
  /**
   * method to handle changes in the JTree selection
   *
   * @param tp  treePath event
   */
  void tree_valueChanged(TreePath tp)
  {
    if (treeValueFlag) {
      if (tp != null) {
        Object ob = tp.getLastPathComponent();
        DefaultMutableTreeNode node = null;
        if (ob != null) {
          node = (DefaultMutableTreeNode)ob;
        }
        selectedNode = node;
        NodeInfo ni = (NodeInfo)node.getUserObject();
        if (ni.isChoice()&&!ni.isCheckbox()) {
          // this loop is for radio button CHOICE elements
          // it should change the selected radio button, allowing only one button
          // in the group to be selected
          for (Enumeration eee = (node.getParent()).children();eee.hasMoreElements(); ) {
            DefaultMutableTreeNode nnn = (DefaultMutableTreeNode)eee.nextElement();
            NodeInfo ni1 = (NodeInfo)nnn.getUserObject();
            if (ni1.getName().equals(ni.getName())) {
              ni1.setSelected(true);
            } else {
              ni1.setSelected(false);
            }
          }

          tree.invalidate();
          OutputScrollPanel.repaint();
        }

        int width = this.getSize().width - DocControlPanel.getDividerLocation() - 40;
        XMLPanels xp = new XMLPanels(node, width);
        xp.setTreeModel(treeModel);
        xp.setContainer(this);
        xp.setTree(tree);
        NestedPanelScrollPanel.getViewport().removeAll();
        NestedPanelScrollPanel.getViewport().add(xp.topPanel);
      }
    }
    treeValueFlag = true;
  }

  /**
   * Copy popup menu action
   *
   * @param event  the event
   */
  void Copy_actionPerformed(java.awt.event.ActionEvent event)
  {
    TreePath tp = tree.getSelectionPath();
    if (tp != null) {
      Object ob = tp.getLastPathComponent();
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
      nodeCopy = deepNodeCopy(node);
      // assign the cloned copy to the controller so that one
      // can copy/paste between editor instances
      if (controller != null) {
        controller.setClipboardObject(nodeCopy);
      }
    }
  }

  /**
   * Paste popup menu action
   *
   * @param event  the event
   */
  void Paste_actionPerformed(java.awt.event.ActionEvent event)
  {
    TreePath tp = tree.getSelectionPath();
    if (tp != null) {
      Object ob = tp.getLastPathComponent();
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
      DefaultMutableTreeNode localcopy = deepNodeCopy(nodeCopy);
      // simple node comparison
      String nodename = ((NodeInfo)node.getUserObject()).getName();
      if (controller != null) {
        nodeCopy = (DefaultMutableTreeNode)controller.getClipboardObject();
      }
      if (nodeCopy != null) {
        String savenodename = ((NodeInfo)localcopy.getUserObject()).getName();
        if (savenodename.startsWith("attribute-")) savenodename = "attribute-";
        if (nodename.startsWith(savenodename)) {
          DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
          int indx = parent.getIndex(node);
          parent.insert(localcopy, indx + 1);
          tree.expandPath(tp);
          treeModel.reload(parent);
          tree.setSelectionPath(tp);
        }
      }
    }
  }

  /**
   * This menu action adds a text node to the selected
   * node in the tree. In theory, this should never need to
   * be done, but the arbitrary number of levels to which a
   * recursive schema is extracted may cut-off the text node
   * so this action was added to the popup menu
   *
   * @param event  the event
   */
  void Addtext_actionPerformed(java.awt.event.ActionEvent event)
  {
    TreePath tp = tree.getSelectionPath();
    if (tp != null) {
      Object ob = tp.getLastPathComponent();
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
      NodeInfo ni = new NodeInfo("#PCDATA");
      ni.setPCValue(" ");
      DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(ni);
      node.add(newNode);
      AddtextItem.setEnabled(false);
    }
  }

  
  void NewWindow_actionPerformed(java.awt.event.ActionEvent event)
  {
    (new DocFrame()).setVisible(true);    
  }
  
  
  /**
   * popup menu action to replace a selected node
   * in the tree with one from the clipboard
   *
   * @param event  the event
   */
  void Replace_actionPerformed(java.awt.event.ActionEvent event)
  {
    TreePath tp = tree.getSelectionPath();
    if (tp != null) {
      Object ob = tp.getLastPathComponent();
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
      DefaultMutableTreeNode localcopy = deepNodeCopy(nodeCopy);
      // simple node comparison
      String nodename = ((NodeInfo)node.getUserObject()).getName();
      if (nodename.startsWith("attribute-")) nodename = "attribute";
      if (nodeCopy != null) {
        String savenodename = ((NodeInfo)localcopy.getUserObject()).getName();
        if (savenodename.startsWith("attribute-")) savenodename = "attribute";
        if (nodename.equals(savenodename)) {
          DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
          int indx = parent.getIndex(node);
          parent.insert(localcopy, indx + 1);
          parent.remove(indx);
          tree.expandPath(tp);
          treeModel.reload(parent);
          tree.setSelectionPath(tp);
        }
      }
    }
  }


  /**
   * popup menu action to display the xml attribute
   * editor/display window 
   *
   * @param event  the event
   */
  void Attr_actionPerformed(java.awt.event.ActionEvent event)
  {
    TreePath tp = tree.getSelectionPath();
    if (tp != null) {
      Object ob = tp.getLastPathComponent();
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
      String title = "Attributes of " + ((NodeInfo)node.getUserObject()).getName();
      AttributeEditDialog aed = new AttributeEditDialog(this, title, node);
      aed.show();
    }
  }

  /**
   * popup menu action to duplicate the selected node
   *
   * @param event  the event
   */
  void Dup_actionPerformed(java.awt.event.ActionEvent event)
  {
    TreePath tp = tree.getSelectionPath();
    if (tp != null) {
      Object ob = tp.getLastPathComponent();
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
      DefaultMutableTreeNode par = (DefaultMutableTreeNode)node.getParent();
      int iii = par.getIndex(node);
      DefaultMutableTreeNode newnode = deepNodeCopy(node);
      if ((((NodeInfo)newnode.getUserObject()).isChoice()) &&
            (((NodeInfo)newnode.getUserObject()).isSelected())) {
        ((NodeInfo)newnode.getUserObject()).setSelected(false);
      }
      tree.expandPath(tp);
      par.insert(newnode, iii + 1);
      treeModel.reload(par);
      tree.setSelectionPath(tp);
    }
  }

  /**
   * popup menu action to delete the selected node
   *
   * @param event  the event
   */
  void Del_actionPerformed(java.awt.event.ActionEvent event)
  {
    int selRow = -1;
    TreePath currentSelection = tree.getSelectionPath();
    int[] selRows = tree.getSelectionRows();
    if ((selRows != null) && (selRows.length > 0)) {
      selRow = selRows[0];
    }
    if (currentSelection != null) {
      DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                (currentSelection.getLastPathComponent());
      NodeInfo ni = (NodeInfo)currentNode.getUserObject();
      String curNodeName = ni.getName();
      if (curNodeName.startsWith("attribute-")) curNodeName = "attribute";
      int cnt = 0;
      MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
      if (parent != null) {
        Enumeration eee = parent.children();
        while (eee.hasMoreElements()) {
          DefaultMutableTreeNode cn = (DefaultMutableTreeNode)eee.nextElement();
          NodeInfo ni1 = (NodeInfo)cn.getUserObject();
          String name = ni1.getName();
          if (name.startsWith("attribute-")) name = "attribute";          
          if (name.equals(curNodeName)) {
            cnt++;
          }
        }
      }
      if ((parent != null)) {
        if (cnt > 1) {
          treeModel.removeNodeFromParent(currentNode);
        } else {
          if ((ni.getCardinality().equalsIgnoreCase("OPTIONAL")) ||
                    (ni.getCardinality().equalsIgnoreCase("ZERO to MANY"))) {
            treeModel.removeNodeFromParent(currentNode);
          }
        }
        if (selRow > 0) {
          tree.setSelectionRow(selRow - 1);
        }
        return;
      }
    }

    // Either there was no selection, or the root was selected.
    Toolkit.getDefaultToolkit().beep();
  }

  /*
   * write the tree starting at the indicated node to a File named 'fn'
   * i.e. serializes the tree to xml in a file
   *
   * @param node  top level node
   * @param fn    file name
   */
  void writeXML(DefaultMutableTreeNode node, String fn)
  {
    // make a copy since we are modifying before saving
    DefaultMutableTreeNode clone = deepNodeCopy(node);
    trimAttributeNames(clone);  // remove extra info in attribute nodes
    File outputFile = new File(fn);
    try {
      FileWriter out = new FileWriter(outputFile);
      tempStack = new Stack();
      start = new StringBuffer();
      if (trimFlag) {
        trimNoInfoNodes(clone);
      }
      saveAttributeValues(clone);
      write_loop(clone, 0);
      String str1 = start.toString();

      String doctype = "";
      if (publicIDString != null) {
        String rootNodeName = ((NodeInfo)node.getUserObject()).getName();
        String temp = "";
        if (publicIDString != null) {
          temp = "\"" + publicIDString + "\"";
        }
        String temp1 = "";
        if (systemIDString != null) {
          temp1 = "\"file://" + systemIDString + "\"";
        }
        doctype = "<!DOCTYPE " + rootNodeName + " PUBLIC " + temp + " " + temp1 + ">\n";
      }
//      str1 = "<?xml version=\"1.0\"?>\n" + doctype + str1;
      str1 = "<?xml version=\"1.0\"?>\n" + str1;

      out.write(str1);
      out.close();
    } catch (Exception e) {}
  }

  /**
   * write the tree starting at the indicated node to a String
   *
   * @param node  top level node
   * @return      xml string
   */
  public String writeXMLString(DefaultMutableTreeNode node)
  {
    // make a copy since we are modifying before saving
    DefaultMutableTreeNode clone = deepNodeCopy(node);
    trimAttributeNames(clone);  // remove extra info in attribute nodes
    tempStack = new Stack();
    start = new StringBuffer();
    if (trimFlag) {
      trimNoInfoNodes(clone);
    }
    saveAttributeValues(clone);
    write_loop(clone, 0);
    String str1 = start.toString();

    String doctype = "";
    if (publicIDString != null) {
      String rootNodeName = ((NodeInfo)node.getUserObject()).getName();
      String temp = "";
      if (publicIDString != null) {
        temp = "\"" + publicIDString + "\"";
      }
      String temp1 = "";
      if (systemIDString != null) {
        temp1 = "\"file://" + systemIDString + "\"";
      }
      doctype = "<!DOCTYPE " + rootNodeName + " PUBLIC " + temp + " " + 
                temp1 + ">\n";
    }
    str1 = "<?xml version=\"1.0\"?>\n" + doctype + str1;

    return str1;
  }

  /**
   *  This method writes a DOM Node based on the TreeNode
   *   returns the root node of the DOM
   */
  public Node writeToDOM(DefaultMutableTreeNode node) {
    String xml = writeXMLString(node);
    StringReader sr = new StringReader(xml);
    Node DOMOut = null;
    try{
      DOMOut = XMLUtilities.getXMLReaderAsDOMTreeRootNode(sr);
    }
    catch (Exception e) {
      Log.debug(4, "Problem writing DOM from XML string!");
    }
    return DOMOut;
  }
  /**
   * recursive routine to create xml output
   *
   * @param node  starting node
   * @param indent  indent level
   */
  void write_loop(DefaultMutableTreeNode node, int indent)
  {
    String indentString = "";
    while (indentString.length() < indent) {
      indentString = indentString + " ";
    }
    StringBuffer start1 = new StringBuffer();
    String name;
    String end;
    boolean emptyNodeParent = false;
    NodeInfo ni = (NodeInfo)node.getUserObject();
    name = ni.name;
    if (((!ni.isChoice()) || (ni.isChoice() && (ni.isSelected())) || (!removeExtraInfoFlag))
       && (!ni.isXMLAttribute())
       ) {
      if (ni.isXMLAttribute()) Log.debug(0,"node "+ni.name+" is Attribute");
      // completely ignore NOT SELECTED nodes AND their children
      if ((!(name.indexOf("CHOICE")>-1)) && 
          (!(name.indexOf("SEQUENCE")>-1)) && (!(name.equals("Empty")))
          || (!removeExtraInfoFlag&&(!name.equals("Empty"))) 
        ) {
        // ignore (CHOICE) nodes but process their children
        
        // modify if the node has name 'eml' to add namespace info
        // needed for eml2 docs
        if (name.equals("eml")) {
          start1.append("\n" + indentString + "<" + "eml:eml ");;
        } else {
          start1.append("\n" + indentString + "<" + name);
        }

        Enumeration keys = (ni.attr).keys();
        while (keys.hasMoreElements()) {
          String str = (String)(keys.nextElement());
          String val = (String)((ni.attr).get(str));
          val = val.trim();
          if (!(str.equals("minOccurs")) && (!(str.equals("maxOccurs")))
            && (!str.equals("editor"))
            && (!str.equals("help"))
            && (!str.equals("")) 
//            &&(!(str.indexOf("schemaLocation")>-1))
          ) {
//            if (!((str.equals("id"))&&(val.equals("")))) {
            if (!(val.equals(""))) {
              start1.append(" " + str + "=\"" + val + "\"");
            }
//DFH            start1.append(str + "=\"" + val + "\"");
          }
        }
        // next line adds cardinality info
        if (!removeExtraInfoFlag) {
          start1.append(" cardinality=\""+ni.getCardinality()+"\"");
          // next line adds help as attribute
          start1.append(" help=\""+ni.getHelp()+"\"");
        }
        start1.append(">");
        if (name.equals("eml")) {
          end = "</eml:eml>";
        } else {
          end = "</" + name + ">";
        }
        tempStack.push(end);
      }
      Enumeration enum = node.children();
      // if enum has no elements, then node is a leaf node
      if (!enum.hasMoreElements()) {
        start.append(start1.toString());
        start1 = new StringBuffer();
      }

      while (enum.hasMoreElements()) {
        // process child nodes
        DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(enum.nextElement());
        NodeInfo ni1 = (NodeInfo)nd.getUserObject();
        if (ni1.name.equals("#PCDATA")) {
          // remove nodes with empty PCDATA
          String pcdata = ni1.getPCValue();
          if (emptyFlag) {
            if (pcdata.trim().length() < 1) {
              String card = ni.getCardinality();
              if ((card.equals("ZERO to MANY")) || (card.equals("OPTIONAL"))) {
                start1 = new StringBuffer();
                tempStack.pop();
                tempStack.push("");
              }
            }
          }
          start.append(start1.toString());
          start1 = new StringBuffer();
          start.append(XMLUtil.normalize(ni1.getPCValue()));
          textnode = true;
        } else if (ni1.name.equals("Empty")) {
          // remove the '>' at the end and replace with '/>'
          start1.setCharAt(start1.length() - 1, '/');
          start1.append(">");
          start.append(start1.toString());
          start1 = new StringBuffer();
          tempStack.pop();
          emptyNodeParent = true;
          write_loop(nd, indent + 2);
        } else {
          start.append(start1.toString());
          start1 = new StringBuffer();
          write_loop(nd, indent + 2);
        }
      }
      if ((!(name.indexOf("CHOICE")>-1)) && 
          (!(name.indexOf("SEQUENCE")>-1)) && (!(name.equals("Empty")))
          || (!removeExtraInfoFlag&&(!name.equals("Empty"))) 
      ) {

        if (textnode) {
          if (!tempStack.isEmpty()) {
            start.append((String)(tempStack.pop()));
          }
        } else {
          if (!emptyNodeParent) {
            if (!tempStack.isEmpty()) {
              start.append("\n" + indentString + (String)(tempStack.pop()));
            }
          } else {
            emptyNodeParent = false;
          }
        }
        textnode = false;
      }
    }
  }

  /**
   * Trim tree branches where there is no information in the leaf text node 
   * and there is a parent node that is optional.
   *
   * @param node  Description of Parameter
   */
  void trimNoInfoNodes(DefaultMutableTreeNode node)
  {
    DefaultMutableTreeNode parentNode = null;
    DefaultMutableTreeNode tempNode = null;
    DefaultMutableTreeNode curNode = node.getFirstLeaf();
    Vector leafs = new Vector();
    leafs.addElement(curNode);
    while (curNode != null) {
      curNode = curNode.getNextLeaf();
      if (curNode != null) {
        leafs.addElement(curNode);
      }
    }
    Enumeration enum = leafs.elements();
    while (enum.hasMoreElements()) {
      curNode = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)curNode.getUserObject();
      if (ni.name.equals("#PCDATA")) {
        // is a text node
        String pcdata = ni.getPCValue();
        if (pcdata.trim().length() < 1) {
          // has no text data
          parentNode = (DefaultMutableTreeNode)curNode.getParent();
          // first build Vector of nodes from current leaf to root
          Vector path2root = new Vector();
          path2root.addElement(curNode);
          while (parentNode != null) {
            path2root.addElement(parentNode);
            parentNode = (DefaultMutableTreeNode)parentNode.getParent();
          }
          // now go from the root toward the leaf, trimming branches
          DefaultMutableTreeNode cNode;
          for (int i = path2root.size() - 1; i > -1; i--) {
            cNode = (DefaultMutableTreeNode)path2root.elementAt(i);
            NodeInfo cni = (NodeInfo)cNode.getUserObject();
            String card = cni.getCardinality();
            // if node is not required, perhaps trim it
            if ((card.equals("ZERO to MANY")) || 
                (card.equals("OPTIONAL"))) {
              // first see if there are nonempty sub-branches

              if (!hasNonEmptyTextLeaves(cNode)) {
                tempNode = cNode;
                parentNode = (DefaultMutableTreeNode)cNode.getParent();
                if (parentNode != null) {
                  parentNode.remove(tempNode);
                }
              }
            }
            else if (card.equals("ONE to MANY")) {
              // if there is more than one, it can be eliminated
              DefaultMutableTreeNode nextSibling = cNode.getNextSibling();
              DefaultMutableTreeNode prevSibling = cNode.getPreviousSibling();
              String cNodeName = cni.getName();
              boolean multipleFlag = false;
              if (prevSibling!=null) {
                NodeInfo pni =(NodeInfo)prevSibling.getUserObject();
                if ((pni.getName()).equals(cNodeName)) {
                  multipleFlag  = true;
                }
              }
              if (nextSibling!=null) {
                NodeInfo nni =(NodeInfo)nextSibling.getUserObject();
                if ((nni.getName()).equals(cNodeName)) {
                  multipleFlag  = true;
                }
              }
              if ((!hasNonEmptyTextLeaves(cNode))&&(multipleFlag)) {
                tempNode = cNode;
                parentNode = (DefaultMutableTreeNode)cNode.getParent();
                if (parentNode != null) {
                  parentNode.remove(tempNode);
                }
              }
            }
          }
        }
        // end 'if (pcdata...'
      }
    }
  }

  /**
   * Returns true if this node has any descendents with non-empty text leaves.
   *
   */
  boolean hasNonEmptyTextLeaves(DefaultMutableTreeNode node)
  {
    boolean res = false;
    DefaultMutableTreeNode parentNode = null;
    Enumeration enum = node.depthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode curNode = (DefaultMutableTreeNode)enum.nextElement();
      if (curNode.isLeaf()) {
        NodeInfo ni = (NodeInfo)curNode.getUserObject();
        if ((!ni.isXMLAttribute())&&(ni.name.equals("#PCDATA"))) {
          // is a text node
          String pcdata = ni.getPCValue();
          if (pcdata.trim().length() > 0) {
            // has text data
            return true;
          }
        }
      }
    }
    return res;
  }

  /**
   * Returns true if this node has any descendents with text leaves
   * which have contain ONLY white space
   *
   * @param node  Description of Parameter
   * @return      Description of the Returned Value
   */
  boolean hasEmptyTextLeaves(DefaultMutableTreeNode node)
  {
    boolean res = false;
    DefaultMutableTreeNode parentNode = null;
    Enumeration enum = node.depthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode curNode = (DefaultMutableTreeNode)enum.nextElement();
      if (curNode.isLeaf()) {
        NodeInfo ni = (NodeInfo)curNode.getUserObject();
        if (ni.name.equals("#PCDATA")) {
          // is a text node
          String pcdata = ni.getPCValue();
          Log.debug(1,"White space check: "+pcdata);
          if (pcdata.trim().length() == 0) {
            // has only white space
            return true;
          }
        }
      }
    }
    return res;
  }
  
  /**
   * expands a JTree to the indicated level
   *
   * @param jt   JTree to expand
   * @param level  number of 'level' to expand to
   */
  void expandTreeToLevel(JTree jt, int level)
  {
    DefaultMutableTreeNode childNode;
    TreeModel tm = jt.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)tm.getRoot();
    Enumeration enum = root.breadthFirstEnumeration();
    DefaultMutableTreeNode curNode = (DefaultMutableTreeNode)enum.nextElement();
    while ((enum.hasMoreElements()) && (curNode.getLevel() < level)) {
      try {
        childNode = (DefaultMutableTreeNode)curNode.getFirstChild();
        NodeInfo ni = (NodeInfo)childNode.getUserObject();
        if (!(ni.getName().equals("#PCDATA"))) {
          TreeNode[] tn = curNode.getPath();
          TreePath tp = new TreePath(tn);
          tree.expandPath(tp);
        }
      } catch (Exception w) {
      }
      curNode = (DefaultMutableTreeNode)enum.nextElement();
    }
  }


  /*
    -----------------------------------------------
    code for combining the content of two DefaultMutableTreeNode trees
    inputTree is modified based on content of template tree
    template may be based on DTD and thus provides info like cardinality
    It is assumed that nodes use NodeInfo user objects
    */

  /**
   * Modify input tree by adding info in template
   * input and template are root nodes of trees
   * input tree will be modified using template
   *
   * @param input   instance node
   * @param template  template node
   */
  void treeUnion(DefaultMutableTreeNode input, DefaultMutableTreeNode template)
  {
    Stack tempStack;
    Vector tempVector = new Vector();
    DefaultMutableTreeNode tNode;
    DefaultMutableTreeNode nd2;
    DefaultMutableTreeNode pqw;
    DefaultMutableTreeNode qw = null;
    // first check to see if root nodes have same names
    if (!compareNodes(input, template)) {
      Log.debug(20, "Root nodes do not match!!!");
    } else {
      // root nodes match
      //mergeNodes(input, template);  // not needed ? (see mergeData method near end)--- DFH
      //so start comparing children
      Vector nextLevelInputNodes;
      Vector nextLevelTemplateNodes;
      Vector currentLevelInputNodes = new Vector();
      currentLevelInputNodes.addElement(input);
      Vector currentLevelTemplateNodes = new Vector();
      currentLevelTemplateNodes.addElement(template);
      for (int j = 0; j < numlevels; j++) {
        nextLevelInputNodes = new Vector();
        for (Enumeration enum = currentLevelInputNodes.elements(); 
          enum.hasMoreElements(); ) {
          DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
          for (Enumeration qq = nd.children(); qq.hasMoreElements(); ) {
            DefaultMutableTreeNode nd1 = (DefaultMutableTreeNode)qq.nextElement();
            nextLevelInputNodes.addElement(nd1);
          }
        }
        nextLevelTemplateNodes = new Vector();
        for (Enumeration enum1 = currentLevelTemplateNodes.elements();enum1.hasMoreElements(); ) {
          DefaultMutableTreeNode ndt = (DefaultMutableTreeNode)enum1.nextElement();
          for (Enumeration qq1 = ndt.children();qq1.hasMoreElements(); ) {
            DefaultMutableTreeNode ndt1 = (DefaultMutableTreeNode)qq1.nextElement();
            nextLevelTemplateNodes.addElement(ndt1);
          }
        }
        // now have a list of all elements in input and template 
        // trees at the level being processed
        // loop over all the template nodes at the 'next' level
        Enumeration enum = nextLevelTemplateNodes.elements();
        while (enum.hasMoreElements()) {
          boolean insTest = false;
          tNode = (DefaultMutableTreeNode)enum.nextElement();

          // insert (CHOICE) or (SEQUENCE) elements into instance
          NodeInfo ni = (NodeInfo)tNode.getUserObject();
          if ((ni.getName().indexOf("CHOICE")>-1) || 
                (ni.getName().indexOf("SEQUENCE")>-1)) {
            DefaultMutableTreeNode templParent = (DefaultMutableTreeNode)tNode.getParent();
            DefaultMutableTreeNode specCopy = (DefaultMutableTreeNode)tNode.clone();
            Vector choiceParentHits = getMatches(templParent, currentLevelInputNodes);
            for (int m = 0; m < choiceParentHits.size(); m++) {
              DefaultMutableTreeNode workingInstanceNode = (DefaultMutableTreeNode)choiceParentHits.elementAt(m);
              DefaultMutableTreeNode specCopyClone = (DefaultMutableTreeNode)(specCopy.clone());
              specCopyClone.setUserObject(((NodeInfo)specCopy.getUserObject()).cloneNodeInfo());
              Enumeration kids = workingInstanceNode.children();
              Vector kidsVec = new Vector();
              while (kids.hasMoreElements()) {
                kidsVec.addElement((DefaultMutableTreeNode)(kids.nextElement()));
              }
              int cindex = -1;
              boolean insertTest = true;
              for (int n = 0; n < kidsVec.size(); n++) {
                DefaultMutableTreeNode kidNode = (DefaultMutableTreeNode)(kidsVec.elementAt(n));
                // 'hasAMatch' returns true if children of 2nd param match first param
                if (hasAMatch(kidNode, tNode)) {
                  cindex = workingInstanceNode.getIndex(kidNode);
                  if (insertTest) {  // only insert once
                    workingInstanceNode.insert(specCopyClone, cindex);
                    nextLevelInputNodes.insertElementAt(specCopyClone, cindex);
                    insertTest = false;
                  }
                  // move kidNode
                  specCopyClone.add(kidNode);
                }
              }
            }
          }
        }
        // now move to the next level in the tree hierarchy
        currentLevelInputNodes = nextLevelInputNodes;
        currentLevelTemplateNodes = nextLevelTemplateNodes;

      }
    }
    // this loop has been over the nodes in the template in order to
    // insert CHOICE and SEQUENCE nodes in the instance
    // Next 2 lines are loops over instance to speed up the merging of data
    // and the additon of subtrees into the instance.
    mergeData(input, template);
    if (mergeMissingFlag) {
      mergingMissingSubtrees(input, template);
    }
  }

  /**
   *  merge data from all matching template nodes into instance
   *
   */
  void mergeData(DefaultMutableTreeNode instance, DefaultMutableTreeNode template)  {
    if (!compareNodes(instance, template)) {
      Log.debug(20, "Root nodes do not match!!!");
    } else {
      // root nodes match
      mergeNodes(instance, template);
      //so start comparing children
      Vector nextLevelInputNodes;
      Vector nextLevelTemplateNodes;
      Vector currentLevelInputNodes = new Vector();
      currentLevelInputNodes.addElement(instance);
      Vector currentLevelTemplateNodes = new Vector();
      currentLevelTemplateNodes.addElement(template);
      // loop over all the levels of the instance (assumed small compared to template)
      for (int j = 0; j < instance.getDepth(); j++) {
        nextLevelInputNodes = new Vector();
        for (Enumeration enum = currentLevelInputNodes.elements(); 
          enum.hasMoreElements(); ) {
          DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
          for (Enumeration qq = nd.children(); qq.hasMoreElements(); ) {
            DefaultMutableTreeNode nd1 = (DefaultMutableTreeNode)qq.nextElement();
            nextLevelInputNodes.addElement(nd1);
          }
        }
        nextLevelTemplateNodes = new Vector();
        Hashtable nextLevelTemplateHash = new Hashtable();
        for (Enumeration enum1 = currentLevelTemplateNodes.elements();enum1.hasMoreElements(); ) {
          DefaultMutableTreeNode ndt = (DefaultMutableTreeNode)enum1.nextElement();
          for (Enumeration qq1 = ndt.children(); qq1.hasMoreElements(); ) {
            DefaultMutableTreeNode ndt1 = (DefaultMutableTreeNode)qq1.nextElement();
            nextLevelTemplateNodes.addElement(ndt1);
            String name = pathToString(ndt1);
            nextLevelTemplateHash.put(name,ndt1); 
          }
        }
        // now have a list of all elements in input and template 
        // trees at the level being processed
        // loop over all the instance nodes at the 'next' level
        Enumeration enum = nextLevelInputNodes.elements();
        while (enum.hasMoreElements()) {
          DefaultMutableTreeNode tNode = (DefaultMutableTreeNode)enum.nextElement();
          String nm = pathToString(tNode);
          DefaultMutableTreeNode fromNode = (DefaultMutableTreeNode)nextLevelTemplateHash.get(nm);
          if (fromNode!=null) {
            mergeNodes(tNode, fromNode);
          }
        }
        currentLevelInputNodes = nextLevelInputNodes;
        currentLevelTemplateNodes = nextLevelTemplateNodes;
      }
    }
  }
 

  /**
   * merges any subtrees that exist in the template into the instance tree
   *
   */
  void mergingMissingSubtrees(DefaultMutableTreeNode instance, DefaultMutableTreeNode template) {
    if (!compareNodes(instance, template)) {
      Log.debug(20, "Root nodes do not match!!!");
    } else {
      // root nodes match
      //so start comparing children
      Vector nextLevelInputNodes;
      Vector nextLevelTemplateNodes;
      Vector currentLevelInputNodes = new Vector();
      currentLevelInputNodes.addElement(instance);
      Vector currentLevelTemplateNodes = new Vector();
      currentLevelTemplateNodes.addElement(template);
      // Note: Vectors are built which contain both the current and next level
      // tree nodes. These vectors are built because the tree itself is being
      // manipulated and we want a list that is not changing!
      
      // loop over all the levels of the instance (assumed small compared to template)
      for (int j = 0; j < instance.getDepth(); j++) {
        nextLevelInputNodes = new Vector();
        for (Enumeration enum = currentLevelInputNodes.elements(); 
          enum.hasMoreElements(); ) {
          DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
          for (Enumeration qq = nd.children(); qq.hasMoreElements(); ) {
            DefaultMutableTreeNode nd1 = (DefaultMutableTreeNode)qq.nextElement();
            nextLevelInputNodes.addElement(nd1);
          }
        }
        nextLevelTemplateNodes = new Vector();
        Hashtable curLevelTemplateHash = new Hashtable();
        for (Enumeration enum1 = currentLevelTemplateNodes.elements(); enum1.hasMoreElements(); ) {
          DefaultMutableTreeNode ndt = (DefaultMutableTreeNode)enum1.nextElement();
          String name = pathToString(ndt);
          curLevelTemplateHash.put(name.trim(), ndt);
  
          for (Enumeration qq1 = ndt.children(); qq1.hasMoreElements(); ) {
            DefaultMutableTreeNode ndt1 = (DefaultMutableTreeNode)qq1.nextElement();
            nextLevelTemplateNodes.addElement(ndt1);
          }
        }
          
          // now have a list of all elements in input and template 
          // trees at the level being processed
          // loop over all the instance nodes at the 'current' level
          Enumeration enum2 = currentLevelInputNodes.elements();
          while (enum2.hasMoreElements()) {
            DefaultMutableTreeNode inNode = (DefaultMutableTreeNode)enum2.nextElement();
            // now find matching template nodes at the same level
            String nm = pathToString(inNode);
            DefaultMutableTreeNode fromNode = (DefaultMutableTreeNode)curLevelTemplateHash.get(nm.trim());
            if (fromNode!=null) {
              mergeMissingChildren(inNode, fromNode);
            }
            else {
            }
          }
        currentLevelInputNodes = nextLevelInputNodes;
        currentLevelTemplateNodes = nextLevelTemplateNodes;         
      }
    }
  }
   
  /**
   *  merges child nodes from template that are missing from instance
   *  works for a given node in the instamce document 
   *  (called from mergingMissingSubtrees)
   */
   
  void mergeMissingChildren(DefaultMutableTreeNode instance, DefaultMutableTreeNode template) {
    Enumeration enum = instance.children();
    Vector instChildren = new Vector();
    while (enum.hasMoreElements()) {
      instChildren.addElement(enum.nextElement());
    }
    instChildren.addElement(newNode("end")); // end of set marker
    int instChildrenIndex = 0;
    Enumeration enum1 = template.children();
    Vector templChildren = new Vector();
    while (enum1.hasMoreElements()) {
      templChildren.addElement(enum1.nextElement());
    }
    // now go thru the template children and add missing children to instance
    for (int i=0;i<templChildren.size();i++) {
      DefaultMutableTreeNode templChild = (DefaultMutableTreeNode)templChildren.elementAt(i);
      if (instChildren.size()==0) {
        instance.add(templChild);
      }
      else{
        DefaultMutableTreeNode instChild = 
                 (DefaultMutableTreeNode)instChildren.elementAt(instChildrenIndex);
      if(!simpleCompareNodes(instChild, templChild)){
        if (instChildrenIndex<(instChildren.size()-1)) {
          instance.insert(templChild, instance.getIndex(instChild));
        }
        else {
          instance.add(templChild);
        }
      }
      else {
        // a match, so increment the child instance node pointer
        if(instChildrenIndex<(instChildren.size()-1)) instChildrenIndex++;
        // must increment child instance node pointer when multiple copies of nodes
        // exist in instance child list
          if ((instChildrenIndex<(instChildren.size()))&&(instChildrenIndex>0)) {
            instChild = (DefaultMutableTreeNode)instChildren.elementAt(instChildrenIndex);
            DefaultMutableTreeNode prevInstChild = 
                      (DefaultMutableTreeNode)instChildren.elementAt(instChildrenIndex-1);
            while ((simpleCompareNodes(instChild, prevInstChild)) &&
                (instChildrenIndex<(instChildren.size()-1))){
              instChildrenIndex++;
              instChild = (DefaultMutableTreeNode)instChildren.elementAt(instChildrenIndex);
              prevInstChild = (DefaultMutableTreeNode)instChildren.elementAt(instChildrenIndex-1);
            }
          }
        }
      }
    }
  }
  
  /**
   * returns boolean indicating if input node matches any CHILD of tempparent
   *
   * @param input     Description of Parameter
   * @param tempparent  Description of Parameter
   * @return            Description of the Returned Value
   */
  boolean hasAMatch(DefaultMutableTreeNode input, DefaultMutableTreeNode tempparent)
  {
    Vector specNodes = new Vector();
    String inputS = ((NodeInfo)input.getUserObject()).getName();
    Enumeration enum = tempparent.children();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode enumNode = (DefaultMutableTreeNode)enum.nextElement();
      String matchS = ((NodeInfo)enumNode.getUserObject()).getName();
      if ((matchS.indexOf("CHOICE")>-1) || (matchS.indexOf("SEQUENCE")>-1)) {
        specNodes.addElement(enumNode);
      }
      if (matchS.startsWith(inputS)) {
        return true;
      }
    }
    if (specNodes.size() > 0) {
      for (int i = 0; i < specNodes.size(); i++) {
        DefaultMutableTreeNode specialNode = (DefaultMutableTreeNode)specNodes.elementAt(i);
        Vector list = getRealChildren(specialNode);
        Enumeration enum1 = list.elements();
        while (enum1.hasMoreElements()) {
          DefaultMutableTreeNode enum1Node = (DefaultMutableTreeNode)enum1.nextElement();
          String matchSpecial = ((NodeInfo)enum1Node.getUserObject()).getName();
          if (matchSpecial.startsWith(inputS)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   *  given a node, get all the chudren; if child is CHOICE or SEQUENCE then
   *  skip over and add the next level of children. This should return a list
   *  that 'collapses' all the CHOICE/SEQUENCE nodes
   */
  private Vector getRealChildren(DefaultMutableTreeNode nd) {
    Vector res = new Vector();
    Enumeration enum = nd.children();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode curnode = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)curnode.getUserObject();
      if ((ni.getName().indexOf("CHOICE")>-1) || (ni.getName().indexOf("SEQUENCE")>-1)) {
        // recursive call
        Vector temp = getRealChildren(curnode);
        for (int j=0;j<temp.size();j++) {
          res.addElement(temp.elementAt(j));
        }
      }
      else {
        res.addElement(curnode);
      }
    }
    return res;
  }
  
  /**
   * treeTrim is designed to remove any nodes in the input that do not match
   * the the nodes in the template tree; i.e. the goal is to remove
   * undesirable nodes from the input tree
   *
   * @param input   Description of Parameter
   * @param template  Description of Parameter
   */
  void treeTrim(DefaultMutableTreeNode input, DefaultMutableTreeNode template)
  {
    DefaultMutableTreeNode inNode;
    DefaultMutableTreeNode parNode;
    // first check to see if root nodes have same names
    if (!compareNodes(input, template)) {
      Log.debug(20, "Root nodes do not match!!!");
    } else {
      // root nodes match, so start comparing children
      Vector nextLevelInputNodes;
      Vector nextLevelTemplateNodes;
      Vector currentLevelInputNodes = new Vector();
      currentLevelInputNodes.addElement(input);
      Vector currentLevelTemplateNodes = new Vector();
      currentLevelTemplateNodes.addElement(template);
      for (int j = 0; j < numlevels; j++) {
        nextLevelInputNodes = new Vector();
        for (Enumeration enum = currentLevelInputNodes.elements(); 
          enum.hasMoreElements(); ) {
          DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
          for (Enumeration qq = nd.children(); qq.hasMoreElements(); ) {
            DefaultMutableTreeNode nd1 = (DefaultMutableTreeNode)qq.nextElement();
            nextLevelInputNodes.addElement(nd1);
          }
        }
        nextLevelTemplateNodes = new Vector();
        for (Enumeration enum1 = currentLevelTemplateNodes.elements(); enum1.hasMoreElements(); ) {
          DefaultMutableTreeNode ndt = (DefaultMutableTreeNode)enum1.nextElement();
          for (Enumeration qq1 = ndt.children(); qq1.hasMoreElements(); ) {
            DefaultMutableTreeNode ndt1 = (DefaultMutableTreeNode)qq1.nextElement();
            nextLevelTemplateNodes.addElement(ndt1);
          }
        }
        // now have a list of all elements in input and template trees 
        // at the level being processed
        // loop over all the input nodes at the 'next' level
        Enumeration enum = nextLevelInputNodes.elements();
        while (enum.hasMoreElements()) {
          inNode = (DefaultMutableTreeNode)enum.nextElement();
          Vector hits = simpleGetMatches(inNode,nextLevelTemplateNodes);
          // if there are no 'hits' then the node should be removed
          if (hits.size() < 1) {
            parNode = (DefaultMutableTreeNode)inNode.getParent();
            parNode.remove(inNode);
          }
        }
        currentLevelInputNodes = nextLevelInputNodes;
        currentLevelTemplateNodes = nextLevelTemplateNodes;

      }
      // end of levels loop
    }
  }

  /**
   * uses a simpleCompareNodes to get those nodes in a vector
   * which match the 'match' node
   *
   * @param match  the node to match
   * @param vec  collection of nodes to test
   * @return     vector of simple matches from vec
   */
  Vector simpleGetMatches(DefaultMutableTreeNode match, Vector vec)
  {
    Vector matches = new Vector();
    Enumeration enum = vec.elements();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode tn = (DefaultMutableTreeNode)enum.nextElement();
      if (simpleCompareNodes(tn, match)) {
        matches.addElement(tn);
      }
    }
    return matches;
  }

  /**
   * Compares node by comparing the path to the node as
   * a String. More accurate comparison than simple name
   * matching, but more time consuming
   *
   * @param node1  first node
   * @param node2  second node
   * @return     do they match?
   */
  boolean compareNodes(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2)
  {
    boolean ret = false;
    String node1Str = pathToString(node1);
    String node2Str = pathToString(node2);
    if (node1Str.equals(node2Str)) {
      ret = true;
    }
    return ret;
  }

  /**
   * Compares node1 and node2 on the basis of node name
   * Note that this comparison is quite shallow; equality
   * really depends on the entire path
   *
   * @param node1  first node
   * @param node2  second node
   * @return     do they match?
   */
  boolean simpleCompareNodes(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2)
  {
    boolean ret = false;
    NodeInfo node1ni = (NodeInfo)node1.getUserObject();
    NodeInfo node2ni = (NodeInfo)node2.getUserObject();
    if (node1ni.getName().equals(node2ni.getName())) {
      ret = true;
    }
    return ret;
  }

  /**
   * create a string which is the 'path' of node names
   * that are ancestors of the input node.
   * Note that only the first 3 ancestors are used here
   * in an attempt to enhance performance
   *
   * @param node  Description of Parameter
   * @return    String path with ancestor node names separated by '/'
   */
  String pathToString(DefaultMutableTreeNode node)
  {
    int start = 0;
    StringBuffer sb = new StringBuffer();
    TreeNode[] tset = node.getPath();
    int numiterations = tset.length;
    // following line arbitrarily limits the path length to '3' 
    // to speed up code
    int pathLength = 3;
    if (numiterations > pathLength) {
      start = numiterations - pathLength;
     }
     for (int i = start; i < numiterations; i++) {
      String temp = ((NodeInfo)((DefaultMutableTreeNode)tset[i]).
       getUserObject()).getName();
      sb.append(temp + "/");
     }
  return sb.toString();
  }

  /**
   * Compares a node from the instance tree to a node from the
   * template and compies information from the template to the
   * instance. This is how cardinality, help, nodeEditor info, etc.
   * get added to the instance
   *
   * @param input   instance node (destination)
   * @param template  template node (source)
   */
  void mergeNodes(DefaultMutableTreeNode input, DefaultMutableTreeNode template)
  {
    if (compareNodes(input, template)) {
      NodeInfo inputni = (NodeInfo)input.getUserObject();
      NodeInfo templateni = (NodeInfo)template.getUserObject();
      inputni.setCardinality(templateni.getCardinality());
      inputni.setChoice(templateni.isChoice());
      // first set all sibling of input to be not selected
      // This whole code section is for handling CHOICES where
      // only one of a set of nodes can be 'selected' at one time.
      if (templateni.isChoice()) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)input.getParent();
        if (((NodeInfo)parent.getUserObject()).isChoice()) {
          DefaultMutableTreeNode grandparent = (DefaultMutableTreeNode)parent.getParent();
          if (grandparent != null) {
            Enumeration penum = grandparent.children();
            while (penum.hasMoreElements()) {
              DefaultMutableTreeNode sib = (DefaultMutableTreeNode)penum.nextElement();
              ((NodeInfo)sib.getUserObject()).setSelected(false);
            }
          }
          ((NodeInfo)parent.getUserObject()).setSelected(true);
        }
        Enumeration enum = parent.children();
        while (enum.hasMoreElements()) {
          DefaultMutableTreeNode sib = (DefaultMutableTreeNode)enum.nextElement();
          ((NodeInfo)sib.getUserObject()).setSelected(false);
        }
        // the fact that the input node exists indicates that it 
        // should be the selected node
        inputni.setSelected(true);
      }
      // add help info from template
      if (templateni.getHelp() != null) {
        inputni.setHelp(templateni.getHelp());
      }

      // copy attribute to input tree
      Enumeration attrlist = templateni.attr.keys();
      while (attrlist.hasMoreElements()) {
        String key = (String)attrlist.nextElement();
        if (!inputni.attr.containsKey(key)) {
          inputni.attr.put(key, templateni.attr.get(key));
        }
      }
      // add node editor information from template
      String editor = (String)(inputni.attr).get("editor");
      if (editor != null) {
        inputni.setEditor(editor);
        inputni.attr.remove("editor");
      }
      String rooteditor = (String)(inputni.attr).get("rooteditor");
      if (rooteditor != null) {
        inputni.setRootEditor(rooteditor);
        inputni.attr.remove("rooteditor");
      }
      // move help info from attribute to location in nodeInfo object
      String help = (String)(inputni.attr).get("help");
      if (help != null) {
        inputni.setHelp(help);
        inputni.attr.remove("help");
      }
      
    }
  }


  /**
   * This is the method that is executed when editing is completed
   * and the user wants to save the results.
   * The JTree is converted to an XML string by the 'writeXMLString'
   * method and then validated using the validate method.
   * fires editingCompleteEvent
   *
   * @param event  event created when 'Save Changes' button is
   * clicked.
   */
  void EditingExit_actionPerformed(java.awt.event.ActionEvent event)
  {
    treeModel = (DefaultTreeModel)tree.getModel();
    rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
    String xmlout = writeXMLString(rootNode);
Log.debug(20, xmlout);
  /*
  // the following code for checking for empty leaf nodes is
  // commented out because of problems with eml-attribute documents
  // eml-attribute docs have a DTD element defined as 
  // "<!ELEMENT attributeDomain ((enumeratedDomain | textDomain)+ | numericDomain+)>"
  // Handling a CHOICE element followed by a '+' creates problems for the XML editor
  // which, in this case decides that 'enumeratedDomain is a required element and
  // 'textDomain' is not required. It thus adds an 'enumeratedDomain' node even when
  // a 'textDomain' node is present. The resulting xml is valid, but the empty 
  // enumeratedDomain node is not really wanted.
  // The trouble is that when a CHOICE is followed by a '+', the DTD really means tha
  // at least one of the choices is required, but all the choices might be there.
  // The editor allows choice of one node (using check boxes) if the '+' after the
  // parenthesis is missing, but has no way to indicate multiple choices if a '+'
  // is present (DFH)
    if (hasEmptyTextLeaves(rootNode)) {
      int opt = JOptionPane.showConfirmDialog(null,
         "Some required fields may be empty.\n"+
         "If you choose to continue a 'space' \n"+
         "will inserted in these fields.", 
         "DO YOU WANT TO CONTINUE?",
         JOptionPane.YES_NO_OPTION);
      if (opt == JOptionPane.YES_OPTION) {
      
      }
      else {
      return;
      }
    }
  */  
    String valresult = xmlvalidate(xmlout);
    if (valresult.indexOf("<valid />")>-1) {
      if (controller!=null) {
        controller.fireEditingCompleteEvent(this, xmlout);
      // hide the Frame
      this.setVisible(false);
      // free the system resources
      this.dispose();
      tree = null;
      treeModel = null;
      OutputScrollPanelContainer = null;
      NestedPanelScrollPanel = null;
      System.gc();
      }
      else {
        writeOutputFile(xmlout); 
      }
    }
    else {
      Log.debug(20,"Validation problem: "+valresult);
      String msg = "The saved document is not valid EML2 for some reason.\n"+
                   "You can save it locally and fix the problem later,\n"+
                   "but you will be unable to submit it to the network storage system.\n"+
                   "\nDo you want to Continue Exiting the Editor?";
      int opt1 = JOptionPane.showConfirmDialog(null,
         msg,
         "Validation Problem!",
         JOptionPane.YES_NO_OPTION);
      if (opt1== JOptionPane.YES_OPTION) {
        if (controller!=null) {
          controller.fireEditingCompleteEvent(this, xmlout);
          // hide the Frame
          this.setVisible(false);
          // free the system resources
          this.dispose();
          System.gc();
        }
        else {
          writeOutputFile(xmlout);      
        }
      }
    }
  }

  void writeOutputFile(String xmlout) {
    final JFileChooser fc = new JFileChooser();
    String userdir = System.getProperty("user.dir");
    fc.setCurrentDirectory(new File(userdir));
    fc.setSelectedFile(new File("out.xml"));
    //In response to a button click:
    int returnVal = fc.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File savefile = fc.getSelectedFile();
      //this is where file is saved
      StringReader sr = null;
      try{
        FileWriter w = new FileWriter(savefile);
        sr = new StringReader(xmlout);
        int c;
        while ((c = sr.read())!=-1)
          w.write(c);
          w.close();
          sr.close();
        }
      catch (Exception e) {}
    } else {
      // user cancelled the open file command
    }
  }
  
  /**
   * method for quiting editor without saving any changes
   *
   * @param event  triggered by window closing event
   */
  void DocFrame_windowClosing(java.awt.event.WindowEvent event)
  {
  // Show a confirmation dialog
  /*
    int reply = JOptionPane.showConfirmDialog(this,
    "Do you really want to exit without saving changes?",
    "Editor - Exit",
    JOptionPane.YES_NO_OPTION,
    JOptionPane.QUESTION_MESSAGE);
    / If the confirmation was affirmative, handle exiting.
    if (reply == JOptionPane.YES_OPTION) {
    */
  //MBJ if (framework!=null) {
  //MBJ framework.removeWindow(this);
  //MBJ }
  if (controller!=null) {
    controller.fireEditingCanceledEvent(this, XMLTextString);
  }
  this.setVisible(false);
  // hide the Frame
  this.dispose();
  // free the system resources
      tree = null;
      treeModel = null;
      OutputScrollPanelContainer = null;
      NestedPanelScrollPanel = null;
      System.gc();
  if (controller==null) {
    System.exit(0);
  }
  }

  /**
   * Actions to be carried out when OpenButton is clicked
   * This should let the user select a file which the editor
   * will try to open for editing
   *
   * @param event  Event that triggers the button click
   */
  void OpenButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    //Create a file chooser
    final JFileChooser fc = new JFileChooser();
    String userdir = System.getProperty("user.dir");
    fc.setCurrentDirectory(new File(userdir));
    fc.setSelectedFile(new File("*.xml"));
    //In response to a button click:
    int returnVal = fc.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      openfile = fc.getSelectedFile();
      id = openfile.getName();
      //this is where file is opened
      StringWriter sw = null;
      try{
        FileReader r = new FileReader(openfile);
        sw = new StringWriter();
        int c;
        while ((c = r.read())!=-1)
          sw.write(c);
          r.close();
          sw.close();
        }
      catch (Exception e) {}
      logoLabel.setIcon((ImageIcon)icons.get("Btfly4.gif"));
      headLabel.setText("Working...");
        
      initDoc(null, sw.toString());
    } else {
      // user cancelled the open file command
    }
  }

  /**
   * Actions to be carried out when NewButton is clicked
   * This should open a 'new' eml2 doc based on the template
   *
   * @param event  Event that triggers the button click
   */
  void NewButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    templateFlag = false; // set to avoid merging new doc with itself!
    logoLabel.setIcon((ImageIcon)icons.get("Btfly4.gif"));
    headLabel.setText("Working...");
    openfile = new File("./lib/eml.xml");
    //this is where file is opened
    StringWriter sw = null;
    try{
      FileReader r = new FileReader(openfile);
      sw = new StringWriter();
      int c;
      while ((c = r.read())!=-1)
        sw.write(c);
      
      r.close();
      sw.close();
    }
    catch (Exception e) {}
    
    initDoc(null, sw.toString());
  }
  
  /**
   * method carried out when 'Revert' button is clicked
   * This method shold revert to the DOM intially parsed when
   * this frame was opened
   *
   * @param event  Revert button clicked event
   */
  void CancelButton_actionPerformed(java.awt.event.ActionEvent event)
  { 
    initDoc(morpho, docnode, id, location);
  }
  
  /**
   *  Let user select a  DTD file; file is parsed into a tree
   *  showing schema which can then be saved as a DTD template
   *  XNML document.
   */
  void createDTDInstance() {
    //Create a file chooser
    final JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("Select a DTD file to see schema");
    String userdir = System.getProperty("user.dir");
    fc.setCurrentDirectory(new File(userdir));
    fc.setSelectedFile(new File("*.dtd"));
    //In response to a button click:
    int returnVal = fc.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File dtdfile = fc.getSelectedFile();

      DefaultMutableTreeNode nd = buildDTDTree(dtdfile.getAbsolutePath());
      if (nd!=null) {
        rootNode = nd;
        treeModel = new DefaultTreeModel(rootNode);

        treeModel.reload();
        tree.setModel(treeModel);

        tree.expandRow(1);
        tree.expandRow(2);
        tree.setSelectionRow(0);
        setTitle("Morpho Editor:" + id);
        headLabel.setText("Morpho Editor");
        logoLabel.setIcon((ImageIcon)icons.get("logo-icon.gif"));
        headLabel.setText("Morpho Editor");
      }
      else {
        Log.debug(0,"dtd tree root node is null!");
      }
    }
  }
    
  /**
   * removes nodes that have no data from the displayed tree
   * while saving the full tree for later display
   *
   * @param event  the event
   */
  void TrimTreeButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    logoLabel.setIcon((ImageIcon)icons.get("Btfly4.gif"));
    headLabel.setText("Working...");
    // the changes to headLabel and logo label DO NOT appear
    // need to figure out how to force screen update
    treeModel = (DefaultTreeModel)tree.getModel();
    rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
    trimNoInfoNodes(rootNode);
    TrimTreeButton.setEnabled(false);
    UntrimTreeButton.setEnabled(true);
    treeModel.reload();
    tree.expandRow(1);
    tree.setSelectionRow(0);
    headLabel.setText("Morpho Editor");
    logoLabel.setIcon((ImageIcon)icons.get("logo-icon.gif"));
  }

  /**
   * reverts the displayed tree from a 'trimmed' state to
   * an untrimmed state
   *
   * @param event  the event
   */
  void UntrimTreeButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    logoLabel.setIcon((ImageIcon)icons.get("Btfly4.gif"));
    headLabel.setText("Working...");
    treeModel = (DefaultTreeModel)tree.getModel();
    rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
    String xmlout = writeXMLString(rootNode);
    mergeMissingFlag = true;
    initDoc(null, xmlout);
    TrimTreeButton.setEnabled(true);
    UntrimTreeButton.setEnabled(false);
  }

  /**
   * contracts open nodes in the displayed tree
   *
   * @param event  the action event
   */
  void ContractTreeButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    exp_level--;
    if (exp_level < 0) {
      exp_level = 0;
    }
    ((DefaultTreeModel)tree.getModel()).reload();
    expandTreeToLevel(tree, exp_level);
    tree.setSelectionRow(0);
  }

  
  /**
   * expands closed nodes in the displayed tree
   *
   * @param event  the action event
   */
  void ExpandTreeButton_actionPerformed(java.awt.event.ActionEvent event)
  {
    exp_level++;
    ((DefaultTreeModel)tree.getModel()).reload();
    expandTreeToLevel(tree, exp_level);
    tree.setSelectionRow(0);
  }


  /** 
   * called when the docFrame is resized; uses all the space in the
   * nested scroll pane area.
   *
   */
  void DocFrame_componentResized()
  {
    int width = this.getSize().width - DocControlPanel.getDividerLocation() - 40;
    XMLPanels xp = new XMLPanels(selectedNode, width);
    xp.setTreeModel(treeModel);
    xp.setContainer(this);
    xp.setTree(tree);
    NestedPanelScrollPanel.getViewport().removeAll();
    NestedPanelScrollPanel.getViewport().add(xp.topPanel);
  }


  /**
   * select the treenode that has a name the matches the input
   *
   * @param topnode  starting node
   * @param name   name
   */
  void selectMatchingNode(DefaultMutableTreeNode topnode, String name)
  {
    DefaultMutableTreeNode nd = null;
    boolean hit = false;
    Enumeration enum = topnode.breadthFirstEnumeration();
    while (enum.hasMoreElements() && (!hit)) {
      nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      if (ni.getName().equals(name)) {
        hit = true;
      }
    }
    // if hit is true at this point, then there is a match
    // otherwise, there was no match
    if (hit) {
      // special case hardcoded to select the parent of the node
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode)nd.getParent();
      setTreeValueFlag(false);
      TreePath tp = new TreePath(parent.getPath());
      tree.setSelectionPath(tp);
      tree.scrollPathToVisible(tp);
    }
  }

  /**
   * select the treenode that has a name the matches the input and has a text
   * node child with a match
   *
   * @param topnode   Description of Parameter
   * @param nodename  Description of Parameter
   * @param text    Description of Parameter
   */
  void selectMatchingNode(DefaultMutableTreeNode topnode, String nodename, String text)
  {
    DefaultMutableTreeNode nd = null;
    boolean hit = false;
    Enumeration enum = topnode.breadthFirstEnumeration();
    while (enum.hasMoreElements() && (!hit)) {
      nd = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      if (ni.getName().equals(nodename)) {
        //now check if there are child TEXT nodes
        Enumeration nodes = nd.children();
        // loop over child node
        String txt = "";
        DefaultMutableTreeNode ndchild = null;
        while (nodes.hasMoreElements()) {
          ndchild = (DefaultMutableTreeNode)(nodes.nextElement());
          NodeInfo info1 = (NodeInfo)(ndchild.getUserObject());
          if ((info1.name).equals("#PCDATA")) {
            txt = info1.getPCValue();
          }
          if (txt.equals(text)) {
            hit = true;
          }
        }
      }
    }
    // if hit is true at this point, then there is a match
    // otherwise, there was no match
    if (hit) {
      // special case hardcoded to select the 'parent' of the node that
      // matches the selection criteria;
      // should allow more flexible path selection criteria
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode)nd.getParent();
      TreePath tp = new TreePath(parent.getPath());
      tree.setSelectionPath(tp);
    tree.scrollPathToVisible(tp);

    }
  }

  /**
   * get the children of a node, stripping out the 'SEQUENCE' and 'CHOICE'
   * nodes and inserting their children; 'vec' is a vector of child nodes; node
   * is parent
   *
   * @param node  parent
   * @param vec   vector of child nodes
   */
  private void getRealChildren(DefaultMutableTreeNode node, Vector vec)
  {  
    if ((node != null) && (node.children() != null)) {
      Enumeration enum = node.children();
      while (enum.hasMoreElements()) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode)enum.nextElement();
        vec.addElement(child);

        if (((NodeInfo)child.getUserObject()).getName().indexOf("SEQUENCE")>-1) {
          getRealChildren(child, vec);
        } else if (((NodeInfo)child.getUserObject()).getName().indexOf("CHOICE")>-1) {
          getRealChildren(child, vec);
        }
      }
    } 
  }



  /**
   * given a vector to nodes with the same name, return those with the same
   * parent (first set)
   *
   * @param list  Description of Parameter
   * @return    Description of the Returned Value
   */
  private Vector sameParent(Vector list)
  {
    Vector ret = new Vector();
    ret.addElement(list.elementAt(0));
    if (list.size() == 1) {
      return ret;
    }
    DefaultMutableTreeNode node0 = (DefaultMutableTreeNode)list.elementAt(0);
    DefaultMutableTreeNode pnode = (DefaultMutableTreeNode)node0.getParent();
    for (int i = 1; i < list.size(); i++) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)list.elementAt(i);
      DefaultMutableTreeNode pnd = (DefaultMutableTreeNode)nd.getParent();
      if (pnd.equals(pnode)) {
        ret.addElement(nd);
      }
    }
    return ret;
  }

  /**
   * input tree can have duplicate node, while DTDParser tree only has single
   * copy of each node. Need to determine index in tree with duplicates that
   * corresponds to index in tree without duplicates enum is Enumeration with
   * duplicates
   *
   * @param enum  Description of Parameter
   * @param indx  Description of Parameter
   * @return    Description of the Returned Value
   */
  private int findDuplicateIndex(Enumeration enum, int indx)
  {
    int dupcount = 0;
    int uniquecount = 1;
    if (indx == 0) {
      return 0;
    }
    DefaultMutableTreeNode oldnd = (DefaultMutableTreeNode)enum.nextElement();
    while ((uniquecount <= indx) && (enum.hasMoreElements())) {
      DefaultMutableTreeNode nd = (DefaultMutableTreeNode)enum.nextElement();
      if (!compareNodes(oldnd, nd)) {
        uniquecount++;
      } else {
        dupcount++;
      }
      oldnd = nd;
    }
    return (indx + dupcount);
  }

  /**
   * trim 'help' and 'editor' attributes in a node
   *
   * @param nd  Description of Parameter
   */
  private void trimSpecialAttributes(DefaultMutableTreeNode nd)
  {
    if (nd != null) {
      NodeInfo ni = (NodeInfo)nd.getUserObject();
      Hashtable ht = ni.attr;
      String editor = (String)ht.get("editor");
      if (editor != null) {
        ni.setEditor(editor);
        ht.remove("editor");
      }

      String rooteditor = (String)ht.get("rooteditor");
      if (rooteditor != null) {
        ni.setRootEditor(rooteditor);
        ht.remove("rooteditor");
      }

      String help = (String)ht.get("help");
      if (help != null) {
        ni.setHelp(help);
        ht.remove("help");
      }
      //now trim children
      Enumeration childnodes = nd.children();
      while (childnodes.hasMoreElements()) {
        DefaultMutableTreeNode nd1 = (DefaultMutableTreeNode)childnodes.nextElement();
        trimSpecialAttributes(nd1);
      }
    }
  }




  /**
   * This embedded class handles all the actions for the DocFrame window
   *
   * @author   higgins
   */
  class SymAction implements java.awt.event.ActionListener
  {
    /**
     * handles ActionListener events
     *
     */
    public void actionPerformed(java.awt.event.ActionEvent event)
    {
      Object object = event.getSource();
      if (object == DeletemenuItem) {
        Del_actionPerformed(event);
      } else if (object == DupmenuItem) {
        Dup_actionPerformed(event);
      } else if (object == AttrmenuItem) {
        Attr_actionPerformed(event);
      } else if (object == CopymenuItem) {
        Copy_actionPerformed(event);
      } else if (object == ReplacemenuItem) {
        Replace_actionPerformed(event);
      } else if (object == PastemenuItem) {
        Paste_actionPerformed(event);
      } else if (object == AddtextItem) {
        Addtext_actionPerformed(event);
      } else if (object == NewWindowItem) {
        NewWindow_actionPerformed(event);
      } else if (object == EditingExit) {
        EditingExit_actionPerformed(event);
      } else if (object == CancelButton) {
        CancelButton_actionPerformed(event);
      } else if (object == OpenButton) {
        OpenButton_actionPerformed(event);
      } else if (object == NewButton) {
        NewButton_actionPerformed(event);
      } else if (object == TrimTreeButton) {
        TrimTreeButton_actionPerformed(event);
      } else if (object == UntrimTreeButton) {
        UntrimTreeButton_actionPerformed(event);
      } else if (object == ExpandTreeButton) {
        ExpandTreeButton_actionPerformed(event);
      } else if (object == ContractTreeButton) {
        ContractTreeButton_actionPerformed(event);
      } else if (object == choiceCombo) {
        choiceCombo_actionPerformed(event); 
      }
      
    }
  }

  /**
   * handles valueChanged events for the tree
   *
   * @author   higgins
   */
  class SymTreeSelection implements javax.swing.event.TreeSelectionListener
  {
    /**
     * handles valueChanged events
     *
     */
    public void valueChanged(javax.swing.event.TreeSelectionEvent event)
    {
      Object object = event.getSource();
      if (object == tree) {
        TreePath tp = event.getNewLeadSelectionPath();
        tree_valueChanged(tp);
        treeSelChangedFlag = true;
      }
    }
  }

  public void choiceCombo_actionPerformed(java.awt.event.ActionEvent event)
  {
      String sel = (String)choiceCombo.getSelectedItem();
       if (sel.equals("eml")) {
        treeModel.setRoot(rootNode);
        treeModel.reload();
        tree.setModel(treeModel);
        tree.expandRow(1);
        tree.setSelectionRow(0);
      }
      else {
        findNode(rootNode, sel);
      }
  }

  

  /**
   * handles tree popup actions
   *
   * @author   higgins
   */
  class PopupListener extends MouseAdapter
  {
    // on the Mac, popups are triggered on mouse pressed, while 
    // mouseReleased triggers them on the PC; use the trigger flag to 
    // record a trigger, but do not show popup until the
    // mouse released event
    boolean trigger = false;

    public void mousePressed(MouseEvent e)
    {
      // maybeShowPopup(e);
      if (e.isPopupTrigger()) {
        trigger = true;
      }
      else {
        TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
        if ((tp!=null)&&(!treeSelChangedFlag)) {
          Object ob = tp.getLastPathComponent();
          DefaultMutableTreeNode node = null;
          if (ob != null) {
            node = (DefaultMutableTreeNode)ob;
          }
          if ((node != null)&&(compareNodes(node, selectedNode))) {
            NodeInfo ni = (NodeInfo)node.getUserObject();
            if (ni.isCheckbox()) {
              ni.setSelected(!ni.isSelected());
            }
          }
          tree.invalidate();
          OutputScrollPanel.repaint();
        }
        treeSelChangedFlag = false;
      }
    }

    public void mouseReleased(MouseEvent e)
    {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {
      if ((e.isPopupTrigger()) || (trigger)) {

        PastemenuItem.setEnabled(false);
        ReplacemenuItem.setEnabled(false);

        trigger = false;
        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        tree.setSelectionPath(selPath);
        if (selectedNode != null) {
          // simple node comparison
          if (controller != null) {
            nodeCopy = (DefaultMutableTreeNode)controller.getClipboardObject();
          }
          if (nodeCopy != null) {
            String nodename = ((NodeInfo)selectedNode.getUserObject()).getName();
            String savenodename = ((NodeInfo)nodeCopy.getUserObject()).getName();
            String card = ((NodeInfo)selectedNode.getUserObject()).getCardinality();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode.getParent();
            NodeInfo parni = (NodeInfo)parent.getUserObject();
            if (((nodename.equals(savenodename))&&(!card.equals("ONE"))) || 
               (parni.getName().indexOf("CHOICE")>-1)&&(!parni.getCardinality().equals("ONE")) )
            {
              PastemenuItem.setEnabled(true);
            }
            if ((nodename.startsWith("attribute"))&&(savenodename.startsWith("attribute"))) {
              PastemenuItem.setEnabled(true);
            }
            if (nodename.equals(savenodename)) {
              ReplacemenuItem.setEnabled(true);
            }
            if ((nodename.startsWith("attribute"))&&(savenodename.startsWith("attribute"))) {
              ReplacemenuItem.setEnabled(true);
            }
          }

          NodeInfo ni = (NodeInfo)selectedNode.getUserObject();

          if (selectedNode.isLeaf()) {
            if (!(ni.getName().equals("#PCDATA")) && (!(ni.getName().equals("Empty")))) {
              AddtextItem.setEnabled(true);
            } else {
              AddtextItem.setEnabled(false);
            }
          }

          CardmenuItem.setText("Number: " + ni.getCardinality());
          if (ni.getCardinality().equalsIgnoreCase("ONE")) {

            DupmenuItem.setEnabled(false);
            DeletemenuItem.setEnabled(false);
          } else {
            DupmenuItem.setEnabled(true);
            DeletemenuItem.setEnabled(true);
          }
          if (ni.getCardinality().equalsIgnoreCase("OPTIONAL")) {
            DupmenuItem.setEnabled(false);
          }
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }



  /**
   * handles window closing event
   *
   * @author   higgins
   */
  class SymWindow extends java.awt.event.WindowAdapter
  {

    public void windowClosing(java.awt.event.WindowEvent event) {
      Object object = event.getSource();
      if (object == DocFrame.this) {
        DocFrame_windowClosing(event);
      }
    }
  }

  /**
   * Used to handle resizing events
   * Needed to redraw the nested panels display when the window is resized
   *
   * @author   higgins
   */
  class SymComponent extends java.awt.event.ComponentAdapter
  {
    public void componentMoved(java.awt.event.ComponentEvent event)
    {
      Object object = event.getSource();
      if (object == DocFrame.this) {
        // do nothing
      }
    }

    public void componentResized(java.awt.event.ComponentEvent event)
    {
      validate();
      setVisible(true);
      Graphics g = getGraphics();
      paint(g);
      g.dispose();

      Object object = event.getSource();
      if (object == DocFrame.this) {
        SwingUtilities.invokeLater(
          new Runnable()
          {
            public void run()
            {
              DocFrame_componentResized();
            }
          });
      }
    }
  }
  static {
    icons = new Hashtable();
  }
  
//-------------------------------------------------------------------------------------
  /**
   * specialized method for searching for <attribute> tags and then getting their 'name'
   * from a child node for display at the attribute level in the tree
   * 
   * NOTE: this method makes the schema specific assumption that 'attributeName'
   * is a child of the 'attribute' node
   */
  void setAttributeNames(DefaultMutableTreeNode root) {
    setLeafNodes(root);
    // first find all the attribute nodes
    String attr_name = "";
    Enumeration kids = root.breadthFirstEnumeration();
    while(kids.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)kids.nextElement();
      NodeInfo ni = (NodeInfo)node.getUserObject();
      if(ni.toString().equals("attribute")) {  // an attribute node
        // we set the Checkbox flag here due to an eml2 technicality which
        // causes the attributes to be shown with radio buttons
        // This can result in inadvertent data loss if viewed in the editor.
        // (in most places, there is a choice between a sequence and 'references').
//        ni.setCheckboxFlag(true);
// no need to set the CheckboxFlag when the references nodes are trimmed ! DFH
        Enumeration attr_kids = node.breadthFirstEnumeration();
        while(attr_kids.hasMoreElements()) {  // attributes children
          DefaultMutableTreeNode node1 = (DefaultMutableTreeNode)attr_kids.nextElement();
          NodeInfo ni1 = (NodeInfo)node1.getUserObject();
          if(ni1.toString().equals("attributeName")) {
            DefaultMutableTreeNode name_node = (DefaultMutableTreeNode)(node1.getFirstChild());
            attr_name = ((NodeInfo)(name_node.getUserObject())).toString();
            ni.setName(ni.getName()+"-"+attr_name);
          }
        }
      }
    }
  }
  
  /**
   * need to trim extra text added to attribute nodes before saving
   *
   */
  void trimAttributeNames(DefaultMutableTreeNode root) {
    Enumeration kids = root.breadthFirstEnumeration();
    while(kids.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)kids.nextElement();
      NodeInfo ni = (NodeInfo)node.getUserObject();
      if(ni.toString().startsWith("attribute-")) {  // an attribute node
        ni.setName("attribute");
      }
    }
  }
  
  /**
   * walk the tree and set the ChoiceNode flag in NodeInfo for all children
   * of any node that is a CHOICE
   */
  void setChoiceNodes(DefaultMutableTreeNode root) {
    Enumeration enum = root.depthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)enum.nextElement();
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode)(node.getParent());
      if (parent!=null) {
        NodeInfo ni = (NodeInfo)parent.getUserObject();
        if (ni.getName().indexOf("CHOICE")>-1) {
          NodeInfo nc = (NodeInfo)node.getUserObject();
          nc.setChoice(true);
          if ((ni.getCardinality().equals("ONE to MANY")) || 
               (ni.getCardinality().equals("ZERO to MANY"))) {
            nc.setCheckboxFlag(true);
          }
        }
        else {
          ni.setChoice(false);
        }
      }
    }
  }
  
  /**
   * marks all nodes in the tree as selected
   * should be used on instance tree before other info is added
   */
  void setAllNodesAsSelected(DefaultMutableTreeNode root) {
    Enumeration enum = root.depthFirstEnumeration();
    while (enum.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)enum.nextElement();
      NodeInfo ni = (NodeInfo)node.getUserObject();
      ni.setSelected(true);
    }
    // now handle the children of CHOICE nodes and set only the first child
    // as selected. This is not needed for valid instances, but is needed for 
    // displaying templates
    Enumeration enum1 = root.depthFirstEnumeration();
    while (enum1.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)enum1.nextElement();
      NodeInfo ni = (NodeInfo)node.getUserObject();
      if (ni.getName().indexOf("CHOICE")>-1) {
        Enumeration kids = node.children();
        boolean flag = true;
        while (kids.hasMoreElements()) {
          DefaultMutableTreeNode kidnode = (DefaultMutableTreeNode)kids.nextElement();
          NodeInfo kidni = (NodeInfo)kidnode.getUserObject();
          if (flag) {
            flag = false;
            kidni.setSelected(true);
          } else {
//            kidni.setSelected(false);
          }
        }
      }
    }
  }
  
  /**
   * removes all nodes woth nodeVisLevels greater than
   * indicated level
   */
  void removeNodesVisLevel(DefaultMutableTreeNode root, int vlev) {
    Enumeration enum = root.breadthFirstEnumeration();
    Vector vec = new Vector();
    while (enum.hasMoreElements()) {
      vec.addElement(enum.nextElement());
    }
    for (int i=0;i<vec.size();i++) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)vec.elementAt(i);
      NodeInfo ni = (NodeInfo)node.getUserObject();
      if (ni.getNodeVisLevel()>vlev) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
        if (parent!=null) {
          parent.remove(node);
        }
      }
    }
  }

  /**
   * modify tree by moving all xml attributes to the first child nodes in the
   * tree display
   * 
   */
   void addXMLAttributeNodes(DefaultMutableTreeNode root) {
    if (root==null) {
      Log.debug(0,"root is null!");
    } else {
      Enumeration enum = root.breadthFirstEnumeration();
      Vector vec = new Vector();
      while (enum.hasMoreElements()) {
        vec.addElement(enum.nextElement());
      }
      for (int i=0;i<vec.size();i++) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)vec.elementAt(i);
        NodeInfo ni = (NodeInfo)node.getUserObject();
        Enumeration enum1 = ni.attr.keys();
        while (enum1.hasMoreElements()) {
          // filter for 'special' attributes
          String keyName = (String)enum1.nextElement();
          String value = (String)ni.attr.get(keyName);
          if ( (!keyName.equalsIgnoreCase("minOccurs")) &&
               (!keyName.equalsIgnoreCase("maxOccurs")) &&
               (!keyName.equalsIgnoreCase("editor")) &&
               (!keyName.equalsIgnoreCase("help")) &&
               (!keyName.startsWith("xmlns")) &&
               (!(keyName.indexOf("schemaLocation")>-1)) &&
               (!(keyName.equals("")))               
             )
          {
             if ((value==null)||(value.length()==0)) value = "";
            NodeInfo newni = new NodeInfo(keyName);
            newni.setXMLAttribute(true);
            newni.setCardinality("ONE");
            newni.setIcon("equal.gif");
            DefaultMutableTreeNode newnode = new DefaultMutableTreeNode(); 
            newnode.setUserObject(newni);
            NodeInfo valni = new NodeInfo("#PCDATA");
            valni.setCardinality("ONE");
            valni.setXMLAttribute(true);
            valni.setPCValue(value);
            DefaultMutableTreeNode valNode = new DefaultMutableTreeNode();
            valNode.setUserObject(valni);
            newnode.add(valNode);
            node.insert(newnode, 0);
          }
        }
      }
    }
  }
   
  /**
   * if xml attributes are displayed as separate child node, their value may br
   * updated by the user (see method 'addXMLAttributeNodes'). Need a method to
   * move these values back to attr hashtable in parent before serialization
   */
   void saveAttributeValues(DefaultMutableTreeNode root) {
    if (root==null) {
      Log.debug(0,"root  in 'saveAttribute' method is null!");
    } else {
      Enumeration enum = root.breadthFirstEnumeration();
      while (enum.hasMoreElements()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)enum.nextElement();
        NodeInfo ni = (NodeInfo)node.getUserObject();
        if ((ni.isXMLAttribute())&&(ni.name.equalsIgnoreCase("#PCDATA"))) {
          String val = ni.toString();
          DefaultMutableTreeNode dad = (DefaultMutableTreeNode)(node.getParent());
          String attName = ((NodeInfo)dad.getUserObject()).name;
          DefaultMutableTreeNode granddad = (DefaultMutableTreeNode)(dad.getParent());
          NodeInfo gdni = (NodeInfo)granddad.getUserObject();
          if (gdni.attr.containsKey(attName)) {
            (gdni.attr).put(attName, val);
          }
        }
      }
    }
   }
   
  /**
   *  if xml attributes are displayed, remove them
   */
  void removeAttributeNodes(DefaultMutableTreeNode root) {
    Vector attrNodes = new Vector();
    if (root==null) {
      Log.debug(0,"root  in 'saveAttribute' method is null!");
    } else {
      Enumeration enum = root.breadthFirstEnumeration();
      while (enum.hasMoreElements()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)enum.nextElement();
        NodeInfo ni = (NodeInfo)node.getUserObject();
        if (ni.isXMLAttribute()) {
          attrNodes.addElement(node);
        }
      }
      for (int i=0;i<attrNodes.size();i++) {
        DefaultMutableTreeNode nd = (DefaultMutableTreeNode)attrNodes.elementAt(i);
        DefaultMutableTreeNode par = (DefaultMutableTreeNode)(nd.getParent());
        par.remove(nd);

      }
    }
  }
  
  /**
   *  build a DTD-based tree and return root
   */
  DefaultMutableTreeNode buildDTDTree(String filename) {
    trimFlag = false;
    removeExtraInfoFlag = false;
    DTDTree dtdtree = new DTDTree(filename);
    dtdtree.setRootElementName("dtdroot");
    dtdtree.parseDTD();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode)dtdtree.rootNode;
    return root;
  }
//--------------------------------------------------------------------------------------    

  /**
   * used to validate the xml string from the editor before leaving
   */
  public String xmlvalidate(String xml) {
//  if (controller!=null) {
  if (true) {
    StringReader sr = new StringReader(xml);
    try {
      SAXValidate validator = new SAXValidate(true);
      validator.runTest(sr, "DEFAULT", "eml://ecoinformatics.org/eml-2.0.0 ./xsd/eml.xsd");
      return "<valid />";
    }
    catch(IOException ioe)
    {
      return "IOException: Error reading file:"+ ioe.getMessage();
//      html.append("<p>").append(ioe.getMessage()).append("</p>");
    }
    catch(ClassNotFoundException cnfe)
    {
      return "Parser class not found";
//      html.append("<p>").append(cnfe.getMessage()).append("</p>");
    }
    catch(Exception w)
    {
      return ("Exception:" + w.getMessage());
//      html.append("<p>").append(cnfe.getMessage()).append("</p>");
    }
  }
  // for now just return valid string
  // need to insert eml2 validation test here
  return "<valid />";
  }

  /**
	 *  locate the first node in the template tree by name
	 *  return null if unable to locate
	 *  to be used to get subtrees
	 */
  public DefaultMutableTreeNode findTemplateNodeByName(String nodeName) {
		if (frootNode==null) return null;
		Enumeration enum = frootNode.breadthFirstEnumeration();
		while (enum.hasMoreElements()) {
			DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(enum.nextElement());
			NodeInfo ni = (NodeInfo)(nd).getUserObject();
			String ndname = ni.getName();
			if (ndname.equals(nodeName)) {
				return nd;
			}
		}
		return null;
	}

	/**
	 *   look for any nodes named 'references' (eml2 specific)
	 *   when found, there should be a 'parent' CHOICE node and probably
	 *   a sibling SEQUENCE node. remove references node, parent CHOOICE
	 *   to simplify (apply this to the eml2 template tree
	 */
	void removeAllReferences(DefaultMutableTreeNode node) {
		Vector refsnodes = new Vector();
		// first, list all 'references' nodes
		Enumeration enum = node.depthFirstEnumeration();
		while (enum.hasMoreElements()) {
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)(enum.nextElement());
			NodeInfo ni = (NodeInfo)(dmtn.getUserObject());
			if ((ni.getName()).equals("references")) {
			  refsnodes.addElement(dmtn);
			}
		}
		for (int i=0; i<refsnodes.size();i++) {
			DefaultMutableTreeNode nd = (DefaultMutableTreeNode)refsnodes.elementAt(i);
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode)(nd.getParent());
			NodeInfo pni = (NodeInfo)parent.getUserObject();
			String parname = pni.getName();
			if (parname.indexOf("SEQUENCE")>-1) {
				nd = parent;
				parent = (DefaultMutableTreeNode)(parent.getParent());
			}
			DefaultMutableTreeNode grandparent = (DefaultMutableTreeNode)(parent.getParent());
			nd.removeFromParent();
			parent.removeFromParent();
			Enumeration kids = parent.children();
			while (kids.hasMoreElements()) {
				DefaultMutableTreeNode cnd = (DefaultMutableTreeNode)kids.nextElement();
        // kid nodes are set to be choice nodes (choice between kids and references)
        // thus, remove this setting when removing 'references'
        NodeInfo ni_cnd = (NodeInfo)cnd.getUserObject();
        ni_cnd.setChoice(false);
        ni_cnd.setSelected(ni_cnd.isSelected()); // forces icon update
				grandparent.add(cnd);
			}
		}
	}
	
	
}


