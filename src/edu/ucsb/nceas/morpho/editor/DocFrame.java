/**
 *        Name: DocFrame.java
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-10-11 20:10:18 $'
 * '$Revision: 1.102 $'
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
//import org.apache.xalan.xpath.xml.*;
//import org.apache.xalan.xslt.XSLTInputSource;
//import org.apache.xalan.xslt.XSLTProcessor;
//import org.apache.xalan.xslt.XSLTProcessorFactory;
//import org.apache.xalan.xslt.XSLTResultTarget;
import org.xml.sax.*;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.*;

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
    /** Description of the Field */
    public DefaultTreeModel treeModel;
    /** Description of the Field */
    public DefaultMutableTreeNode rootNode;
    /** Description of the Field */
    public DTDTree dtdtree;
    /** Description of the Field */
    public JTree tree;

    /**
     * a hashtable for saving trees with help and formatting info assume that
     * the key is the name of the rootnode
     */
    public static Hashtable helpTrees = new Hashtable();

    /** counter for name */
    public static int counter = 0;

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

    File file;

    /*
        the string representation of the XML being displayed
     */
    String XMLTextString;

    /*
        the publicID, if available, or the systemID, if available,
        or the rootnode name
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
    int numlevels = 15;
    boolean templateFlag = false;

    JSplitPane DocControlPanel;

    boolean treeValueFlag = true;

    // determines whether node containing no text are saved when output
    // is written
    boolean emptyFlag = true;

    //* nodeCopy is the 'local clipboard' for node storage
    DefaultMutableTreeNode nodeCopy = null;

    //* trimNodesNotInDTDflag indicates whether nodes not in DTD should be removed
    boolean trimNodesNotInDTDflag = true;
    //* indicates whether DTD info should be merged
    boolean dtdMergeflag = true;

    javax.swing.JMenuItem menuItem;
    javax.swing.JMenuItem CardmenuItem;
    javax.swing.JMenuItem DeletemenuItem;
    javax.swing.JMenuItem DupmenuItem;
    javax.swing.JMenuItem AttrmenuItem;
    javax.swing.JMenuItem CopymenuItem;
    javax.swing.JMenuItem ReplacemenuItem;
    javax.swing.JMenuItem PastemenuItem;
    javax.swing.JMenuItem AddtextItem;

    //* Morpho/Metacat id of the document being displayed */
    String id = null;

    //* location sting from Morpho/Metacat */
    String location = null;

    // Used by addNotify
    boolean frameSizeAdjusted = false;

    javax.swing.JPanel OutputScrollPanelContainer = new javax.swing.JPanel();
    javax.swing.JScrollPane OutputScrollPanel = new javax.swing.JScrollPane();
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
    javax.swing.JButton EditingExit = new javax.swing.JButton();
    javax.swing.JPanel NotesPanel = new javax.swing.JPanel();
    javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
    javax.swing.JLabel JLabel2 = new javax.swing.JLabel();
    javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
    javax.swing.JLabel JLabel4 = new javax.swing.JLabel();
    //Create the popup menu.
    javax.swing.JPopupMenu popup = new JPopupMenu();
    static Hashtable icons;

    /** A reference to the container framework */
    private Morpho morpho = null;

    /** This constructor builds the contents of the DocFrame Display  */

    public DocFrame()
    {
        setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout(0, 0));
        setSize(643, 452);
        setVisible(false);
        OutputScrollPanelContainer.setLayout(new BorderLayout(0, 0));
        getContentPane().add(OutputScrollPanelContainer);
        OutputScrollPanelContainer.add(BorderLayout.CENTER, OutputScrollPanel);
        TreeControlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
        OutputScrollPanelContainer.add(BorderLayout.SOUTH, TreeControlPanel);
        TrimTreeButton.setText("Trim");
        TrimTreeButton.setActionCommand("Trim");
        TrimTreeButton.setToolTipText("Remove all optional nodes that contain no text.");
        TreeControlPanel.add(TrimTreeButton);
        UntrimTreeButton.setText("Undo");
        UntrimTreeButton.setActionCommand("Undo");
        UntrimTreeButton.setToolTipText("Restore optional nodes without text.");
        UntrimTreeButton.setEnabled(false);
        TreeControlPanel.add(UntrimTreeButton);
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
        headLabel.setText("Working...!!!");
        TopLabelPanel.add(headLabel);
        TopPanel.add(BorderLayout.WEST, logoLabel);
        ControlPanel.setLayout(new BorderLayout(0, 0));
        getContentPane().add(BorderLayout.SOUTH, ControlPanel);
        ButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        ControlPanel.add(BorderLayout.EAST, ButtonPanel);
        CancelButton.setText("Cancel");
        CancelButton.setActionCommand("Cancel");
        ButtonPanel.add(CancelButton);
        EditingExit.setText("Save Changes");
        EditingExit.setActionCommand("jbutton");
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
            ImageIcon head = new ImageIcon(
                    getClass().getResource("smallheader-bg.gif"));
            icons.put("smallheader-bg.gif", head);
        }
        if (!icons.containsKey("logo-icon.gif")) {
            ImageIcon logoIcon =
                    new ImageIcon(getClass().getResource("logo-icon.gif"));
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

        //{{REGISTER_LISTENERS
        SymAction lSymAction = new SymAction();
        SymWindow aSymWindow = new SymWindow();
        this.addWindowListener(aSymWindow);
        EditingExit.addActionListener(lSymAction);
        CancelButton.addActionListener(lSymAction);
        TrimTreeButton.addActionListener(lSymAction);
        UntrimTreeButton.addActionListener(lSymAction);
        ExpandTreeButton.addActionListener(lSymAction);
        ContractTreeButton.addActionListener(lSymAction);
        SymComponent aSymComponent = new SymComponent();
        this.addComponentListener(aSymComponent);
        //}}
        DeletemenuItem.addActionListener(lSymAction);
        DupmenuItem.addActionListener(lSymAction);
        AttrmenuItem.addActionListener(lSymAction);
        CopymenuItem.addActionListener(lSymAction);
        ReplacemenuItem.addActionListener(lSymAction);
        PastemenuItem.addActionListener(lSymAction);
        AddtextItem.addActionListener(lSymAction);
        //Create the popup menu.
        javax.swing.JPopupMenu popup = new JPopupMenu();

        rootNode = newNode("Working...!!!");
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
     * @param sTitle  Description of Parameter
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
     * Constructor for the DocFrame object
     *
     * @param file  Description of Parameter
     */
    public DocFrame(File file)
    {
        this();
        this.file = file;
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
            setTitle("Working...");
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
            setTitle("Working...");
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
//			setLocation(50, 50);
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
     * Description of the Method
     *
     * @param finalMorpho  Description of Parameter
     * @param doctext      Description of Parameter
     * @param flag         Description of Parameter
     */
    public void initDoc(Morpho finalMorpho, String doctext, boolean flag)
    {
        final Morpho fMorpho = finalMorpho;
        final String fdoctext = doctext;
        final boolean fflag = flag;

        final SwingWorker worker =
            new SwingWorker()
            {
                public Object construct()
                {

                    initDocInner(fMorpho, fdoctext, fflag);
                    return null;
                }

                //Runs on the event-dispatching thread.
                public void finished()
                {
                if (!templateFlag) {
                  treeModel.reload();
                  tree.setModel(treeModel);
                } else {
                  DefaultTreeModel dftm = new DefaultTreeModel(dtdtree.rootNode);
                  tree.setModel(dftm);
                }

                    tree.expandRow(1);
                    tree.expandRow(2);
                    tree.setSelectionRow(0);
                    setTitle("Morpho Editor:" + id);
                    headLabel.setText("Morpho Editor");
                    logoLabel.setIcon((ImageIcon)icons.get("logo-icon.gif"));
                }
            };
        worker.start();
        //required for SwingWorker 3

    }

    void initDocInner(Morpho morpho, String doctext, boolean flag)
    {
        DefaultMutableTreeNode frootNode = null;
//	  this.templateFlag = flag;
        this.morpho = morpho;
        counter++;
        setName("Morpho Editor" + counter);
        XMLTextString = doctext;
        putXMLintoTree(treeModel, XMLTextString);

        // now want to possibly merge the input document with a formatting document
        // and set the 'editor' and 'help' fields for each node
        // use the root node name as a key
        rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
        String rootname = ((NodeInfo)rootNode.getUserObject()).getName();
        // arbitrary assumption that the formatting document has the rootname +
        // ".xml" as a file name; the formatting document is XML with the same
        // tree structure as the document being formatted; 'help' and 'editor' attributes
        // are used to set help and editor strings for nodes
//    if (!helpTrees.containsKey(rootname)) {
        if (true) {
            rootname = rootname + ".xml";
            frootNode = new DefaultMutableTreeNode("froot");
            DefaultTreeModel ftreeModel = new DefaultTreeModel(frootNode);
            String fXMLString = "";
            boolean formatflag = true;

            try {
                //MBJ//BufferedReader in = new BufferedReader(new FileReader(file));
                ClassLoader cl = this.getClass().getClassLoader();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        cl.getResourceAsStream(rootname)));
                StringWriter out = new StringWriter();
                int c;
                while ((c = in.read()) != -1) {
                    out.write(c);
                }
                in.close();
                out.flush();
                out.close();
                fXMLString = out.toString();
            } catch (Exception e) {
                formatflag = false;
            }

            if (formatflag) {
                putXMLintoTree(ftreeModel, fXMLString);
                frootNode = (DefaultMutableTreeNode)ftreeModel.getRoot();
                // formatting info has now been put into a JTree which is merged with
                // the previously created document tree
                treeUnion(rootNode, frootNode);
            }
            // if the document instance has a DTD, the DTD is parsed
            // and info from the result is merged into the tree
            if (dtdMergeflag) {
                if (dtdfile != null) {
                    dtdtree = new DTDTree(dtdfile);
                    dtdtree.setRootElementName(rootnodeName);
                    dtdtree.parseDTD();

                    rootNode = (DefaultMutableTreeNode)treeModel.getRoot();

                    // the treeUnion method will 'merge' the input document with
                    // a template XML document created using the DTD parser from the DTD doc
                    if (!templateFlag) {
                        //
//	           long temp = System.currentTimeMillis();
//	           System.out.println("StarttreeUnion:"+((new Long(temp)).toString()));
                        treeUnion(rootNode, dtdtree.rootNode);
//            treeUnion(frootNode, dtdtree.rootNode);
//            helpTrees.put(rootname, frootNode);
//            treeUnion(rootNode, frootNode);
//	           temp = System.currentTimeMillis();
//	           System.out.println("FinishtreeUnion:"+((new Long(temp)).toString()));

                        // treeTrim will remove nodes in the input that are not in the DTD
                        // remove the following line if this is not wanted
                        if (trimNodesNotInDTDflag) {
                            treeTrim(rootNode, dtdtree.rootNode);
                        }
                    }
                }
            }
        }
        // end of if (!helpTrees.containsKey())
        else {
//	           long temp = System.currentTimeMillis();
//	           System.out.println("StarttreeUnionCache:"+((new Long(temp)).toString()));
            treeUnion(rootNode, frootNode);
//	           temp = System.currentTimeMillis();
//	           System.out.println("FinishtreeUnionCache:"+((new Long(temp)).toString()));

            // treeTrim will remove nodes in the input that are not in the DTD
            // remove the following line if this is not wanted
            if (trimNodesNotInDTDflag) {
//	           temp = System.currentTimeMillis();
//	           System.out.println("StartTrimTree:"+((new Long(temp)).toString()));
                treeTrim(rootNode, dtdtree.rootNode);
//	           temp = System.currentTimeMillis();
//	           System.out.println("FiinishTrimTree:"+((new Long(temp)).toString()));
            }
        }
        setSelectedNodes(rootNode);

/*
        if (!templateFlag) {
            treeModel.reload();
            tree.setModel(treeModel);
        } else {
            DefaultTreeModel dftm = new DefaultTreeModel(dtdtree.rootNode);
            tree.setModel(dftm);
        }
*/        
        /*
            tree.expandRow(1);
            tree.expandRow(2);
            tree.setSelectionRow(0);
            setTitle("Morpho Editor:"+id);
            headLabel.setText("Morpho Editor");
            logoLabel.setIcon((ImageIcon)icons.get("logo-icon.gif"));
          */
    }

    /**
     * Description of the Method
     *
     * @param name  Description of Parameter
     * @return      Description of the Returned Value
     */
    DefaultMutableTreeNode newNode(Object name)
    {
        NodeInfo ni = new NodeInfo(name.toString());
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(ni);
        return node;
    }


    /**
     * Description of the Method
     *
     * @param node  Description of Parameter
     * @return      Description of the Returned Value
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
     * Description of the Method
     *
     * @param node  Description of Parameter
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
     * Description of the Method
     *
     * @param filename  Description of Parameter
     * @return          Description of the Returned Value
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
     * Description of the Method
     *
     * @param args  Description of Parameter
     */
    public static void main(String args[])
    {
        (new DocFrame()).setVisible(true);
    }

    /**
     * Sets the SelectedNodes attribute of the DocFrame object
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
                        parentNode = 
                            (DefaultMutableTreeNode)parentNode.getParent();
                    }
                    // now go from the root toward the leaf, 
                    // setting selected nodes
                    DefaultMutableTreeNode cNode;
                    for (int i = path2root.size() - 1; i > -1; i--) {
                        cNode = (DefaultMutableTreeNode)path2root.elementAt(i);
                        NodeInfo cni = (NodeInfo)cNode.getUserObject();
                        if (cni.isChoice()) {
                            //cni.setSelected(true);
                            for (Enumeration eee = 
                                (cNode.getParent()).children(); 
                                eee.hasMoreElements(); ) {
                                DefaultMutableTreeNode nnn = 
                                    (DefaultMutableTreeNode)eee.nextElement();
                                NodeInfo ni1 = (NodeInfo)nnn.getUserObject();
                                if (ni1.getName().equals(cni.getName())) {
                                    ni1.setSelected(true);
                                } else {
                                    ni1.setSelected(false);
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
        return a Vector containing all the nodes in a Vector that match 'match'
      */
    /**
     * Gets the Matches attribute of the DocFrame object
     *
     * @param match  Description of Parameter
     * @param vec    Description of Parameter
     * @return       The Matches value
     */
    Vector getMatches(DefaultMutableTreeNode match, Vector vec)
    {
        Vector matches = new Vector();
        Enumeration enum = vec.elements();
        while (enum.hasMoreElements()) {
            DefaultMutableTreeNode tn = 
                (DefaultMutableTreeNode)enum.nextElement();
            if (compareNodes(tn, match)) {
                matches.addElement(tn);
            }
        }
        return matches;
    }


    /**
     * Description of the Method
     *
     * @param tm       Description of Parameter
     * @param xmlText  Description of Parameter
     */
    void putXMLintoTree(DefaultTreeModel tm, String xmlText)
    {
        if (xmlText != null) {
            CatalogEntityResolver cer = new CatalogEntityResolver();
            config = morpho.getConfiguration();
            String local_dtd_directory = config.getConfigDirectory() + 
                File.separator + config.get("local_dtd_directory", 0);
            String catalogPath =
            //config.getConfigDirectory() + File.separator +
            config.get("local_catalog_path", 0);

            String xmlcatalogfile = local_dtd_directory + "/catalog";
            try {
                myCatalog = new Catalog();
                myCatalog.loadSystemCatalogs();
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                URL catalogURL = cl.getResource(catalogPath);

                myCatalog.parseCatalog(catalogURL.toString());
                //myCatalog.parseCatalog(xmlcatalogfile);
                cer.setCatalog(myCatalog);
            } catch (Exception e) {
                Log.debug(10, "Problem creating Catalog (772)!\n" +
                        e.getMessage());
            }
            try {
                StringReader sr = new StringReader(xmlText);
                XMLReader parser = null;
                // Get an instance of the parser
                XMLDisplayHandler mh = new XMLDisplayHandler(tm);
                parser = Morpho.createSaxParser((ContentHandler)mh, null);
                parser.setProperty(
                    "http://xml.org/sax/properties/lexical-handler", mh);
                parser.setEntityResolver(cer);
                InputSource is = new InputSource(sr);

                parser.parse(is);
                DefaultMutableTreeNode rt = 
                    (DefaultMutableTreeNode)tm.getRoot();
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
                    rootnodeName = 
                        ((DefaultMutableTreeNode)tm.getRoot()).toString();
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
     * Description of the Method
     *
     * @param event  Description of Parameter
     */
    void tree_valueChanged(javax.swing.event.TreeSelectionEvent event)
    {
        if (treeValueFlag) {
            TreePath tp = event.getNewLeadSelectionPath();
            if (tp != null) {
                Object ob = tp.getLastPathComponent();
                DefaultMutableTreeNode node = null;
                if (ob != null) {
                    node = (DefaultMutableTreeNode)ob;
                }
                selectedNode = node;

                NodeInfo ni = (NodeInfo)node.getUserObject();

                if (ni.isChoice()) {
                    //        if ((ni.getCardinality().equals("NOT SELECTED"))
                    //        ||(ni.getCardinality().equals("SELECTED"))) {
                    for (Enumeration eee = (node.getParent()).children(); 
                        eee.hasMoreElements(); ) {
                        DefaultMutableTreeNode nnn = 
                            (DefaultMutableTreeNode)eee.nextElement();
                        NodeInfo ni1 = (NodeInfo)nnn.getUserObject();
                        if (ni1.getName().equals(ni.getName())) {
//                  ni1.setCardinality("SELECTED");
                            ni1.setSelected(true);
                        } else {
//                  ni1.setCardinality("NOT SELECTED");
                            ni1.setSelected(false);
                        }

                    }
                    tree.invalidate();
                    OutputScrollPanel.repaint();
                }

                int width = this.getSize().width - 
                    DocControlPanel.getDividerLocation() - 40;
                XMLPanels xp = new XMLPanels(node, width);
                xp.setTreeModel(treeModel);
                xp.setContainer(this);
                xp.setTree(tree);
                NestedPanelScrollPanel.getViewport().removeAll();
                NestedPanelScrollPanel.getViewport().add(xp.topPanel);
//         xp.invalidate();
//         NestedPanelScrollPanel.repaint();
            }
        }
        treeValueFlag = true;

    }

    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
     */
    void Copy_actionPerformed(java.awt.event.ActionEvent event)
    {
        TreePath tp = tree.getSelectionPath();
        if (tp != null) {
            Object ob = tp.getLastPathComponent();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
            nodeCopy = deepNodeCopy(node);
            if (controller != null) {
                controller.setClipboardObject(nodeCopy);
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
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
                nodeCopy = 
                    (DefaultMutableTreeNode)controller.getClipboardObject();
            }
            if (nodeCopy != null) {
                String savenodename = 
                    ((NodeInfo)localcopy.getUserObject()).getName();
                if (nodename.equals(savenodename)) {
                    DefaultMutableTreeNode parent = 
                        (DefaultMutableTreeNode)node.getParent();
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
     * Description of the Method
     *
     * @param event  Description of Parameter
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

    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
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
            if (nodeCopy != null) {
                String savenodename = 
                    ((NodeInfo)localcopy.getUserObject()).getName();
                if (nodename.equals(savenodename)) {
                    DefaultMutableTreeNode parent = 
                        (DefaultMutableTreeNode)node.getParent();
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
     * Description of the Method
     *
     * @param event  Description of Parameter
     */
    void Attr_actionPerformed(java.awt.event.ActionEvent event)
    {
        TreePath tp = tree.getSelectionPath();
        if (tp != null) {
            Object ob = tp.getLastPathComponent();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
            String title = "Attributes of " + 
                ((NodeInfo)node.getUserObject()).getName();
            AttributeEditDialog aed = new AttributeEditDialog(
                this, title, node);
            aed.show();
        }
    }

    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
     */
    void Dup_actionPerformed(java.awt.event.ActionEvent event)
    {
        TreePath tp = tree.getSelectionPath();
        if (tp != null) {
            Object ob = tp.getLastPathComponent();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)ob;
            DefaultMutableTreeNode par = 
                (DefaultMutableTreeNode)node.getParent();
            int iii = par.getIndex(node);
            DefaultMutableTreeNode newnode = deepNodeCopy(node);
            if ((((NodeInfo)newnode.getUserObject()).isChoice()) &&
                    (((NodeInfo)newnode.getUserObject()).isSelected())
                    ) {
                ((NodeInfo)newnode.getUserObject()).setSelected(false);
            }
            tree.expandPath(tp);
            par.insert(newnode, iii + 1);
            treeModel.reload(par);
            tree.setSelectionPath(tp);
        }
    }

    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
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
            int cnt = 0;
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) {
                Enumeration eee = parent.children();
                while (eee.hasMoreElements()) {
                    DefaultMutableTreeNode cn = 
                        (DefaultMutableTreeNode)eee.nextElement();
                    NodeInfo ni1 = (NodeInfo)cn.getUserObject();
                    String name = ni1.getName();
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
        write the tree starting at the indicated node to a File named 'fn'
      */
    /**
     * Description of the Method
     *
     * @param node  Description of Parameter
     * @param fn    Description of Parameter
     */
    void writeXML(DefaultMutableTreeNode node, String fn)
    {
        //setSelectedNodes(node);
        File outputFile = new File(fn);
        try {
            FileWriter out = new FileWriter(outputFile);
            tempStack = new Stack();
            start = new StringBuffer();
            write_loop(node, 0);
            String str1 = start.toString();

            String doctype = "";
            if (publicIDString != null) {
                String rootNodeName = 
                    ((NodeInfo)node.getUserObject()).getName();
                String temp = "";
                if (publicIDString != null) {
                    temp = "\"" + publicIDString + "\"";
                }
                String temp1 = "";
                if (systemIDString != null) {
                    temp1 = "\"file://" + systemIDString + "\"";
                }
                doctype = "<!DOCTYPE " + rootNodeName + " PUBLIC " + 
                    temp + " " + temp1 + ">\n";
            }
            str1 = "<?xml version=\"1.0\"?>\n" + doctype + str1;

            out.write(str1);
            out.close();
        } catch (Exception e) {}
    }

    /*
        write the tree starting at the indicated node to a String
      */
    /**
     * Description of the Method
     *
     * @param node  Description of Parameter
     * @return      Description of the Returned Value
     */
    String writeXMLString(DefaultMutableTreeNode node)
    {
        //setSelectedNodes(node);
        tempStack = new Stack();
        start = new StringBuffer();
        if (trimFlag) {
            trimNoInfoNodes(node);
        }
        write_loop(node, 0);
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


    /*
        recursive routine to create xml output
      */
    /**
     * Description of the Method
     *
     * @param node    Description of Parameter
     * @param indent  Description of Parameter
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
//	  boolean emptyFlag = true;
        boolean emptyNodeParent = false;
        NodeInfo ni = (NodeInfo)node.getUserObject();
        name = ni.name;
//	  if (!((ni.getCardinality()).equals("NOT SELECTED"))) {
        if ((!ni.isChoice()) || (ni.isChoice() && (ni.isSelected()))) {
            // completely ignore NOT SELECTED nodes AND their children
            if ((!name.startsWith("(CHOICE)")) && 
                (!name.startsWith("(SEQUENCE)")) && (!name.equals("Empty"))) {
                // ignore (CHOICE) nodes but process their children
                start1.append("\n" + indentString + "<" + name);

                Enumeration keys = (ni.attr).keys();
                while (keys.hasMoreElements()) {
                    String str = (String)(keys.nextElement());
                    String val = (String)((ni.attr).get(str));
                    start1.append(" " + str + "=\"" + val + "\"");
                }
                start1.append(">");
                end = "</" + name + ">";
                tempStack.push(end);
            }
            Enumeration enum = node.children();

// if enum has no elements, then node is a leaf node
            if (!enum.hasMoreElements()) {
                start.append(start1.toString());
                start1 = new StringBuffer();
//	      textnode = true;
            }

            while (enum.hasMoreElements()) {
                // process child nodes
                DefaultMutableTreeNode nd = 
                    (DefaultMutableTreeNode)(enum.nextElement());
                NodeInfo ni1 = (NodeInfo)nd.getUserObject();
                if (ni1.name.equals("#PCDATA")) {
                    // remove nodes with empty PCDATA
                    String pcdata = ni1.getPCValue();
                    if (emptyFlag) {
                        if (pcdata.trim().length() < 1) {
                            String card = ni.getCardinality();
                            if ((card.equals("ZERO to MANY")) || 
                                (card.equals("OPTIONAL"))) {
                                start1 = new StringBuffer();
                                tempStack.pop();
                                tempStack.push("");
                            }
                        }
                    }
                    start.append(start1.toString());
                    start1 = new StringBuffer();
                    start.append(normalize(ni1.getPCValue()));
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
            if ((!name.startsWith("(CHOICE)")) && 
                (!name.startsWith("(SEQUENCE)")) && (!name.equals("Empty"))) {
                if (textnode) {
                    if (!tempStack.isEmpty()) {
                        start.append((String)(tempStack.pop()));
                    }
                } else {
                    if (!emptyNodeParent) {
                        if (!tempStack.isEmpty()) {
                            start.append("\n" + indentString + 
                                (String)(tempStack.pop()));
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
                        parentNode = 
                            (DefaultMutableTreeNode)parentNode.getParent();
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
                                parentNode = 
                                    (DefaultMutableTreeNode)cNode.getParent();
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
     * @param node  Description of Parameter
     * @return      Description of the Returned Value
     */
    boolean hasNonEmptyTextLeaves(DefaultMutableTreeNode node)
    {
        boolean res = false;
        DefaultMutableTreeNode parentNode = null;
        Enumeration enum = node.depthFirstEnumeration();
        while (enum.hasMoreElements()) {
            DefaultMutableTreeNode curNode = 
                (DefaultMutableTreeNode)enum.nextElement();
            if (curNode.isLeaf()) {
                NodeInfo ni = (NodeInfo)curNode.getUserObject();

                if (ni.name.equals("#PCDATA")) {
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
     * Description of the Method
     *
     * @param jt     Description of Parameter
     * @param level  Description of Parameter
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
     * @param input     Description of Parameter
     * @param template  Description of Parameter
     */
    void treeUnion(DefaultMutableTreeNode input, 
        DefaultMutableTreeNode template)
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
            mergeNodes(input, template);
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
                    DefaultMutableTreeNode nd = 
                        (DefaultMutableTreeNode)enum.nextElement();
                    for (Enumeration qq = nd.children(); 
                        qq.hasMoreElements(); ) {
                        DefaultMutableTreeNode nd1 = 
                            (DefaultMutableTreeNode)qq.nextElement();
                        nextLevelInputNodes.addElement(nd1);
                    }
                }
                nextLevelTemplateNodes = new Vector();
                for (Enumeration enum1 = currentLevelTemplateNodes.elements(); 
                    enum1.hasMoreElements(); ) {
                    DefaultMutableTreeNode ndt = 
                        (DefaultMutableTreeNode)enum1.nextElement();
                    for (Enumeration qq1 = ndt.children(); 
                        qq1.hasMoreElements(); ) {
                        DefaultMutableTreeNode ndt1 = 
                            (DefaultMutableTreeNode)qq1.nextElement();
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
                    if ((ni.getName().startsWith("(CHOICE)")) || 
                        (ni.getName().startsWith("(SEQUENCE)"))) {
                        DefaultMutableTreeNode templParent = 
                            (DefaultMutableTreeNode)tNode.getParent();
                        DefaultMutableTreeNode specCopy = 
                            (DefaultMutableTreeNode)tNode.clone();
                        // for now, ignore nested (CHOICE) nodes
                        Vector choiceParentHits = getMatches(templParent, 
                            currentLevelInputNodes);
                        for (int m = 0; m < choiceParentHits.size(); m++) {
                            DefaultMutableTreeNode workingInstanceNode = 
                                (DefaultMutableTreeNode)
                                choiceParentHits.elementAt(m);
                            DefaultMutableTreeNode specCopyClone = 
                                (DefaultMutableTreeNode)(specCopy.clone());
                            specCopyClone.setUserObject(((NodeInfo)
                                specCopy.getUserObject()).cloneNodeInfo());
                            Enumeration kids = workingInstanceNode.children();
                            Vector kidsVec = new Vector();
                            int cindex = -1;
                            while (kids.hasMoreElements()) {
                                DefaultMutableTreeNode kidNode = 
                                    (DefaultMutableTreeNode)kids.nextElement();
                                if (hasAMatch(kidNode, tNode)) {
                                    insTest = true;
                                }
                                kidsVec.addElement(kidNode);
                            }
                            if (insTest) {
                                for (int n = 0; n < kidsVec.size(); n++) {
                                    DefaultMutableTreeNode test = 
                                        (DefaultMutableTreeNode)
                                        (kidsVec.elementAt(n));
                                    if (hasAMatch(test, tNode)) {
                                        if (cindex < 0) {
                                            cindex = workingInstanceNode.
                                                getIndex(test);
                                        }
                                        specCopyClone.add(test);
                                    }
                                }
                                if (cindex == -1) {
                                    workingInstanceNode.insert(specCopyClone, 0);
                                    nextLevelInputNodes.insertElementAt(
                                        specCopyClone, 0);
                                } else {
                                    workingInstanceNode.insert(
                                        specCopyClone, cindex);
                                    nextLevelInputNodes.insertElementAt(
                                        specCopyClone, cindex);
                                }
                            }
                        }
                    }
                    if (!insTest) {
                        Vector hits = getMatches(tNode, nextLevelInputNodes);
//                Vector hits = simpleGetMatches(tNode, nextLevelInputNodes);
                        // merge hits with template node
                        tempVector = (Vector)currentLevelInputNodes.clone();
                        Enumeration en1 = hits.elements();
                        while (en1.hasMoreElements()) {
                            DefaultMutableTreeNode tempnode = 
                                (DefaultMutableTreeNode)en1.nextElement();
                            mergeNodes(tempnode, tNode);
                            // now remove the merged node parent
                            // needed to handle case when some elements have 
                            // text data and others with same name are blank
                            DefaultMutableTreeNode mergeParent = 
                                (DefaultMutableTreeNode)tempnode.getParent();
                            currentLevelInputNodes.removeElement(mergeParent);
                        }
                        // Here we need to add nodes that are 'missing'
                        // go to parent of tnode; find matching nodes in input 
                        // at same level; add children
                        DefaultMutableTreeNode newnode = null;
                        tempStack = new Stack();
                        //               if (hits.size()==0) {
                        DefaultMutableTreeNode ptNode = 
                            (DefaultMutableTreeNode)tNode.getParent();
                        int index = ptNode.getIndex(tNode);
                        Vector parent_hits = getMatches(ptNode, 
                            currentLevelInputNodes);
                        Enumeration en2 = parent_hits.elements();
                        while (en2.hasMoreElements()) {
                            DefaultMutableTreeNode ind = 
                                (DefaultMutableTreeNode)en2.nextElement();
//                        newnode = deepNodeCopy(tNode);
//                        newnode = (DefaultMutableTreeNode)tNode.clone();
                            NodeInfo tNodeNI = (NodeInfo)tNode.getUserObject();
                            newnode = new DefaultMutableTreeNode();
                            newnode.setUserObject(tNodeNI.cloneNodeInfo());

                            trimSpecialAttributes(newnode);
                            int index1 = findDuplicateIndex(ind.children(), 
                                index);
                            if (index1 >= ind.getChildCount()) {
                                if (newnode != null) {
                                    ind.add(newnode);
                                }
                            } else {
                                if (newnode != null) {
                                    ind.insert(newnode, index1);
                                }
                            }
                        }
                        // put removed elements back
                        currentLevelInputNodes = tempVector;
                        // recalculate nextLevelInput
                        nextLevelInputNodes = new Vector();
                        for (Enumeration enumrecalc = 
                            currentLevelInputNodes.elements(); 
                            enumrecalc.hasMoreElements(); ) {
                            DefaultMutableTreeNode ndrecalc = 
                                (DefaultMutableTreeNode)enumrecalc.nextElement();
                            for (Enumeration qqrecalc = ndrecalc.children(); 
                                qqrecalc.hasMoreElements(); ) {
                                DefaultMutableTreeNode nd1recalc = 
                                (DefaultMutableTreeNode)qqrecalc.nextElement();
                                nextLevelInputNodes.addElement(nd1recalc);
                            }
                        }

                    }
                    //end else
                }

                currentLevelInputNodes = nextLevelInputNodes;
                currentLevelTemplateNodes = nextLevelTemplateNodes;

            }
            // end levels loop
        }
        //end else
    }


    /**
     * returns boolean indicating if input node mathches any child of tempparent
     *
     * @param input       Description of Parameter
     * @param tempparent  Description of Parameter
     * @return            Description of the Returned Value
     */
    boolean hasAMatch(DefaultMutableTreeNode input, 
        DefaultMutableTreeNode tempparent)
    {
        Vector specNodes = new Vector();
        String inputS = ((NodeInfo)input.getUserObject()).getName();
        Enumeration enum = tempparent.children();
        while (enum.hasMoreElements()) {
            DefaultMutableTreeNode enumNode = 
                (DefaultMutableTreeNode)enum.nextElement();
            String matchS = ((NodeInfo)enumNode.getUserObject()).getName();
            if ((matchS.startsWith("(CHOICE)")) || 
                (matchS.startsWith("(SEQUENCE)"))) {
                specNodes.addElement(enumNode);
            }
            if (matchS.startsWith(inputS)) {
                return true;
            }
        }
        // the following ia for the case of two consecutive (CHOICE) or
        // (SEQUENCE) nodes a more general case of 3 or more (CHOICE) elements 
        // should be developed!
        if (specNodes.size() > 0) {
            for (int i = 0; i < specNodes.size(); i++) {
                DefaultMutableTreeNode specialNode = 
                    (DefaultMutableTreeNode)specNodes.elementAt(i);
                Enumeration enum1 = specialNode.children();
                while (enum1.hasMoreElements()) {
                    DefaultMutableTreeNode enum1Node = 
                        (DefaultMutableTreeNode)enum1.nextElement();
                    String matchSpecial = 
                        ((NodeInfo)enum1Node.getUserObject()).getName();
                    if (matchSpecial.startsWith(inputS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * treeTrim is designed to remove any nodes in the input that do not match
     * the the nodes in the template tree; i.e. the goal is to remove
     * undesirable nodes from the input tree
     *
     * @param input     Description of Parameter
     * @param template  Description of Parameter
     */
    void treeTrim(DefaultMutableTreeNode input, DefaultMutableTreeNode template)
    {
        //       if (true) return;
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
                    DefaultMutableTreeNode nd = 
                        (DefaultMutableTreeNode)enum.nextElement();
                    for (Enumeration qq = nd.children(); 
                        qq.hasMoreElements(); ) {
                        DefaultMutableTreeNode nd1 = 
                            (DefaultMutableTreeNode)qq.nextElement();
                        nextLevelInputNodes.addElement(nd1);
                    }
                }
                nextLevelTemplateNodes = new Vector();
                for (Enumeration enum1 = currentLevelTemplateNodes.elements(); 
                    enum1.hasMoreElements(); ) {
                    DefaultMutableTreeNode ndt = 
                        (DefaultMutableTreeNode)enum1.nextElement();
                    for (Enumeration qq1 = ndt.children(); 
                        qq1.hasMoreElements(); ) {
                        DefaultMutableTreeNode ndt1 = 
                        (DefaultMutableTreeNode)qq1.nextElement();
                        nextLevelTemplateNodes.addElement(ndt1);
                    }
                }
                // now have a list of all elements in input and template trees 
                // at the level being processed
                // loop over all the input nodes at the 'next' level
                Enumeration enum = nextLevelInputNodes.elements();
                while (enum.hasMoreElements()) {
                    inNode = (DefaultMutableTreeNode)enum.nextElement();
//                Vector hits = getMatches(inNode, nextLevelTemplateNodes);
                    Vector hits = simpleGetMatches(inNode, 
                        nextLevelTemplateNodes);
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
     * Description of the Method
     *
     * @param match  Description of Parameter
     * @param vec    Description of Parameter
     * @return       Description of the Returned Value
     */
    Vector simpleGetMatches(DefaultMutableTreeNode match, Vector vec)
    {
        Vector matches = new Vector();
        Enumeration enum = vec.elements();
        while (enum.hasMoreElements()) {
            DefaultMutableTreeNode tn = 
                (DefaultMutableTreeNode)enum.nextElement();
            if (simpleCompareNodes(tn, match)) {
                matches.addElement(tn);
            }
        }
        return matches;
    }

    /**
     * Description of the Method
     *
     * @param node1  Description of Parameter
     * @param node2  Description of Parameter
     * @return       Description of the Returned Value
     */
    boolean compareNodes(DefaultMutableTreeNode node1, 
        DefaultMutableTreeNode node2)
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
     * Description of the Method
     *
     * @param node1  Description of Parameter
     * @param node2  Description of Parameter
     * @return       Description of the Returned Value
     */
    boolean simpleCompareNodes(DefaultMutableTreeNode node1, 
        DefaultMutableTreeNode node2)
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
     * Description of the Method
     *
     * @param node  Description of Parameter
     * @return      Description of the Returned Value
     */
    String pathToString(DefaultMutableTreeNode node)
    {
        int start = 0;
        StringBuffer sb = new StringBuffer();
        TreeNode[] tset = node.getPath();
        int numiterations = tset.length;
        // following line arbitrarily limits the path length to '3' 
        // to speed up code
        if (numiterations > 3) {
            start = numiterations - 3;
        }
        for (int i = start; i < numiterations; i++) {
            String temp = ((NodeInfo)((DefaultMutableTreeNode)tset[i]).
                getUserObject()).getName();
            sb.append(temp + "/");
        }
        return sb.toString();
    }

    /**
     * Description of the Method
     *
     * @param input     Description of Parameter
     * @param template  Description of Parameter
     */
    void mergeNodes(DefaultMutableTreeNode input, 
        DefaultMutableTreeNode template)
    {
        if (compareNodes(input, template)) {
            NodeInfo inputni = (NodeInfo)input.getUserObject();
            NodeInfo templateni = (NodeInfo)template.getUserObject();
            inputni.setCardinality(templateni.getCardinality());
            inputni.setChoice(templateni.isChoice());
            //           inputni.setSelected(templateni.isSelected());
//            if (templateni.getCardinality().equals("NOT SELECTED")) {
//                inputni.setCardinality("SELECTED");
//            }
            // first set all sibling of input to be not selected
            if (templateni.isChoice()) {
                DefaultMutableTreeNode parent = 
                    (DefaultMutableTreeNode)input.getParent();
                if (((NodeInfo)parent.getUserObject()).isChoice()) {
                    DefaultMutableTreeNode grandparent = 
                        (DefaultMutableTreeNode)parent.getParent();
                    if (grandparent != null) {
                        Enumeration penum = grandparent.children();
                        while (penum.hasMoreElements()) {
                            DefaultMutableTreeNode sib = 
                                (DefaultMutableTreeNode)penum.nextElement();
                            ((NodeInfo)sib.getUserObject()).setSelected(false);
                        }
                    }
                    ((NodeInfo)parent.getUserObject()).setSelected(true);
                }
                Enumeration enum = parent.children();
                while (enum.hasMoreElements()) {
                    DefaultMutableTreeNode sib = 
                        (DefaultMutableTreeNode)enum.nextElement();
                    ((NodeInfo)sib.getUserObject()).setSelected(false);
                }
                // the fact that the input node exists indicates that it 
                // should be the selected node
                inputni.setSelected(true);
            }

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
            String help = (String)(inputni.attr).get("help");
            if (help != null) {
                inputni.setHelp(help);
                inputni.attr.remove("help");
            }
        }
    }


    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
     */
    void EditingExit_actionPerformed(java.awt.event.ActionEvent event)
    {
        // hide the Frame
        this.setVisible(false);
        
        treeModel = (DefaultTreeModel)tree.getModel();
        rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
        String xmlout = writeXMLString(rootNode);
        controller.fireEditingCompleteEvent(this, xmlout);
        // free the system resources
        this.dispose();
    }

    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
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
        controller.fireEditingCanceledEvent(this, XMLTextString);
        this.setVisible(false);
        // hide the Frame
        this.dispose();
        // free the system resources
//	  }
    }


    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
     */
    void CancelButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        //MBJ if (framework!=null) {
        //MBJ framework.removeWindow(this);
        //MBJ }
        controller.fireEditingCanceledEvent(this, XMLTextString);

        this.setVisible(false);
        // hide the Frame
        this.dispose();
        // free the system resources
    }

    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
     */
    void TrimTreeButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        treeModel = (DefaultTreeModel)tree.getModel();
        rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
        if (fullTree == null) {
            fullTree = deepNodeCopy(rootNode);
        }
        trimNoInfoNodes(rootNode);
        UntrimTreeButton.setEnabled(true);
        treeModel.reload();
        tree.expandRow(1);
        tree.setSelectionRow(0);
    }

    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
     */
    void UntrimTreeButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        treeModel = new DefaultTreeModel(fullTree);
        tree.setModel(treeModel);
        ((DefaultTreeModel)tree.getModel()).reload();
        tree.expandRow(1);
        tree.setSelectionRow(0);
    }

    /**
     * Description of the Method
     *
     * @param event  Description of Parameter
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
     * Description of the Method
     *
     * @param event  Description of Parameter
     */
    void ExpandTreeButton_actionPerformed(java.awt.event.ActionEvent event)
    {
        exp_level++;
        ((DefaultTreeModel)tree.getModel()).reload();
        expandTreeToLevel(tree, exp_level);
        tree.setSelectionRow(0);
    }


    /** Description of the Method */
    void DocFrame_componentResized()
    {
        int width = this.getSize().width - 
            DocControlPanel.getDividerLocation() - 40;
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
     * @param topnode  Description of Parameter
     * @param name     Description of Parameter
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
            DefaultMutableTreeNode parent = 
                (DefaultMutableTreeNode)nd.getParent();
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
     * @param text      Description of Parameter
     */
    void selectMatchingNode(DefaultMutableTreeNode topnode, String nodename, 
        String text)
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
            DefaultMutableTreeNode parent = 
                (DefaultMutableTreeNode)nd.getParent();
            TreePath tp = new TreePath(parent.getPath());
            tree.setSelectionPath(tp);
            tree.scrollPathToVisible(tp);

        }
    }

    /**
     * get the children of a node, stripping out the 'SEQUENCE' and 'CHOICE'
     * nodes and inserting their children 'vec' is a vector of child nodes; node
     * is parent
     *
     * @param node  Description of Parameter
     * @param vec   Description of Parameter
     */
    private void getRealChildren(DefaultMutableTreeNode node, Vector vec)
    {
        if ((node != null) && (node.children() != null)) {
            Enumeration enum = node.children();
            while (enum.hasMoreElements()) {
                DefaultMutableTreeNode child = 
                    (DefaultMutableTreeNode)enum.nextElement();
                vec.addElement(child);

                if (((NodeInfo)child.getUserObject()).
                    getName().startsWith("(SEQUENCE)")) {
                    getRealChildren(child, vec);
                } else if (((NodeInfo)child.getUserObject()).
                    getName().startsWith("(CHOICE)")) {
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
     * @return      Description of the Returned Value
     */
    private Vector sameParent(Vector list)
    {
        Vector ret = new Vector();
        ret.addElement(list.elementAt(0));
        if (list.size() == 1) {
            return ret;
        }
        DefaultMutableTreeNode node0 = 
            (DefaultMutableTreeNode)list.elementAt(0);
        DefaultMutableTreeNode pnode = 
            (DefaultMutableTreeNode)node0.getParent();
        for (int i = 1; i < list.size(); i++) {
            DefaultMutableTreeNode nd = 
                (DefaultMutableTreeNode)list.elementAt(i);
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
     * @return      Description of the Returned Value
     */
    private int findDuplicateIndex(Enumeration enum, int indx)
    {
        int dupcount = 0;
        int uniquecount = 1;
        if (indx == 0) {
            return 0;
        }
        DefaultMutableTreeNode oldnd = 
            (DefaultMutableTreeNode)enum.nextElement();
        while ((uniquecount <= indx) && (enum.hasMoreElements())) {
            DefaultMutableTreeNode nd = 
                (DefaultMutableTreeNode)enum.nextElement();
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
                DefaultMutableTreeNode nd1 = 
                    (DefaultMutableTreeNode)childnodes.nextElement();
                trimSpecialAttributes(nd1);
            }
        }
    }


    /**
     * Normalizes the given string.
     *
     * @param s  Description of Parameter
     * @return   Description of the Returned Value
     */
    private String normalize(String s)
    {
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<':
                {
                    str.append("&lt;");
                    break;
                }
                case '>':
                {
                    str.append("&gt;");
                    break;
                }
                case '&':
                {
                    str.append("&amp;");
                    break;
                }
                case '"':
                {
                    str.append("&quot;");
                    break;
                }
                case '\r':
                case '\t':
                case '\n':
                {
                    if (false) {
                        str.append("&#");
                        str.append(Integer.toString(ch));
                        str.append(';');
                        break;
                    }
                    // else, default append char
                    break;
                }
                default:
                {
                    str.append(ch);
                }
            }
        }
        String res = str.toString();
        res = res.trim();
        if (res.length() == 0) {
            res = " ";
        }
        return res;
    }


    /**
     * Description of the Class
     *
     * @author   jones
     */
    class SymAction implements java.awt.event.ActionListener
    {
        /**
         * Description of the Method
         *
         * @param event  Description of Parameter
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
            }

            if (object == EditingExit) {
                EditingExit_actionPerformed(event);
            } else if (object == CancelButton) {
                CancelButton_actionPerformed(event);
            } else if (object == TrimTreeButton) {
                TrimTreeButton_actionPerformed(event);
            } else if (object == UntrimTreeButton) {
                UntrimTreeButton_actionPerformed(event);
            } else if (object == ExpandTreeButton) {
                ExpandTreeButton_actionPerformed(event);
            } else if (object == ContractTreeButton) {
                ContractTreeButton_actionPerformed(event);
            }
        }
    }

    /**
     * Description of the Class
     *
     * @author   jones
     */
    class SymTreeSelection implements javax.swing.event.TreeSelectionListener
    {
        /**
         * Description of the Method
         *
         * @param event  Description of Parameter
         */
        public void valueChanged(javax.swing.event.TreeSelectionEvent event)
        {
            Object object = event.getSource();
            if (object == tree) {
                tree_valueChanged(event);
            }
        }
    }


    /**
     * Description of the Class
     *
     * @author   jones
     */
    class PopupListener extends MouseAdapter
    {
        // on the Mac, popups are triggered on mouse pressed, while 
        // mouseReleased triggers them on the PC; use the trigger flag to 
        // record a trigger, but do not show popup until the
        // mouse released event
        boolean trigger = false;

        /**
         * Description of the Method
         *
         * @param e  Description of Parameter
         */
        public void mousePressed(MouseEvent e)
        {
            // maybeShowPopup(e);
            if (e.isPopupTrigger()) {
                trigger = true;
            }
        }

        /**
         * Description of the Method
         *
         * @param e  Description of Parameter
         */
        public void mouseReleased(MouseEvent e)
        {
            maybeShowPopup(e);
        }

        /**
         * Description of the Method
         *
         * @param e  Description of Parameter
         */
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
                        nodeCopy = (DefaultMutableTreeNode)
                            controller.getClipboardObject();
                    }
                    if (nodeCopy != null) {
                        String nodename = ((NodeInfo)
                            selectedNode.getUserObject()).getName();
                        String savenodename = ((NodeInfo)
                            nodeCopy.getUserObject()).getName();
                        if (nodename.equals(savenodename)) {
                            PastemenuItem.setEnabled(true);
                            ReplacemenuItem.setEnabled(true);
                        }
                    }

                    NodeInfo ni = (NodeInfo)selectedNode.getUserObject();

                    if (selectedNode.isLeaf()) {
                        if (!(ni.getName().equals("#PCDATA")) && 
                            (!(ni.getName().equals("Empty")))) {
                            AddtextItem.setEnabled(true);
                        } else {
                            AddtextItem.setEnabled(false);
                        }
                    }

                    CardmenuItem.setText("Number: " + ni.getCardinality());
                    if (ni.getCardinality().equalsIgnoreCase("ONE")) {
//          ||   (ni.getCardinality().equalsIgnoreCase("SELECTED"))  ||
//             (ni.getCardinality().equalsIgnoreCase("NOT SELECTED"))
//             )

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
    // normalize(String):String


    /**
     * Description of the Class
     *
     * @author   jones
     */
    class SymWindow extends java.awt.event.WindowAdapter
    {

        /**
         * Description of the Method
         *
         * @param event  Description of Parameter
         */
        public void windowClosing(java.awt.event.WindowEvent event)
        {
            Object object = event.getSource();
            if (object == DocFrame.this) {
                DocFrame_windowClosing(event);
            }
        }
    }

    /**
     * Description of the Class
     *
     * @author   jones
     */
    class SymComponent extends java.awt.event.ComponentAdapter
    {
        /**
         * Description of the Method
         *
         * @param event  Description of Parameter
         */
        public void componentMoved(java.awt.event.ComponentEvent event)
        {
            Object object = event.getSource();
            if (object == DocFrame.this) {

            }
        }

        /**
         * Description of the Method
         *
         * @param event  Description of Parameter
         */
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
}

