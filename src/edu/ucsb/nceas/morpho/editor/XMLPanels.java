
/**
 *       Name: XMLPanels.java
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Dan Higgins
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2006-06-13 21:58:55 $'
 * '$Revision: 1.45 $'
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

/**
 * XMLPanels is an alternative view of the TreeModel data
 * structure. Rather than the JTree outline view, this
 * class creates a set of nested panels for showing the
 * hierarchy. Included is support for dynamically loaded
 * 'custom' editor that can be assigned at run time for any
 * special nodes in the hierarchy. An XMLPanels object is
 * usually associated with a JTree view which serves as an
 * 'outline' and selecting any node in the outline displays the
 * nested panel (or custom editor) view of that node and its
 * children. Tree leaves are shown as text input boxes that
 * are labeled with the element name. Editing the text box thus
 * serves as editing text in the original hierarchy. The
 * class is designed to look like a form to the user. One can
 * enter text into the textboxes and then press tab to move to
 * the next box. The display scrolls as the user moves to a
 * textbox out of view.
 *
 * @author higgins
 */
package edu.ucsb.nceas.morpho.editor;

import java.awt.*;
import javax.swing.*;
import java.util.Hashtable;
import javax.swing.tree.*;
import java.util.Enumeration;
import java.lang.reflect.*;
import edu.ucsb.nceas.morpho.util.Log;


public class XMLPanels extends Component
{

 public JPanel topPanel;
 public DefaultMutableTreeNode doc;
// public MyDefaultTreeModel treeModel = null;
 public DefaultTreeModel treeModel = null;
 public JTree tree = null;

 public DocFrame container = null;

 // create default panel if true
 private boolean defaultPanel = true;

 private int numPanels = 0;

 // nodeMap will store the tree node associated with each textfield
 Hashtable nodeMap;

    //
    // Constructors
    //

    /** Default constructor. */
    public XMLPanels() {
        this(null);
        }

    /** Constructs a panel tree with the specified root. */
    public XMLPanels(DefaultMutableTreeNode node) {
        this.doc = node;
        topPanel = new JPanel();
        nodeMap = new Hashtable();  // textfield key mapped to node
        init();
    }

    /** Constructs a panel tree with the specified root.
      * and the specified default panel width
      */
    public XMLPanels(DefaultMutableTreeNode node, int defaultWidth) {
        this.doc = node;
        nodeMap = new Hashtable();  // textfield key mapped to node
        topPanel = new JPanel();
        topPanel.setSize(new Dimension(defaultWidth, 300));
        init();
    }


//    public void setTreeModel(MyDefaultTreeModel tm) {
    public void setTreeModel(DefaultTreeModel tm) {
        treeModel = tm;
    }

    public void setTree(JTree tree) {
      this.tree = tree;
    }

    public void setContainer(DocFrame df) {
      container = df;
    }
    /**
     */
    void init(){
//        topPanel.setPreferredSize(new Dimension(400,30));
//        topPanel.setMinimumSize(new Dimension(400,300));
//        topPanel.setMaximumSize(new Dimension(300,30));
         // is there anything to do?
        if (doc == null) { return; }
        NodeInfo info = (NodeInfo)(doc.getUserObject());
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(info.toString()),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
        topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.Y_AXIS));
        String temp = info.getRootEditor();
        if (temp!=null) {
          try {
   //         defaultPanel = false;  // set to avoid 'doPanels' creating a duplicate
            Object[] Args = new Object[] {doc};
            Class[] ArgsClass = new Class[] {DefaultMutableTreeNode.class};
            Class componentDefinition = Class.forName(temp);
            Constructor ArgsConstructor = componentDefinition.getConstructor(ArgsClass);
            Object obj = createObject(ArgsConstructor,Args);

            // obj should be a component that can be added to a container (e.g. a descendent
            // of JPanel) with a constructor that takes a node as an argument
            if (obj!=null) {
              topPanel.add((Component)obj);
            }
          }
          catch (ClassNotFoundException e) {
            System.out.println(e);
          }
          catch (NoSuchMethodException e) {
            System.out.println(e);
          }
        }
          refLock = false;
          doPanels(doc,topPanel);
    }

    /**
     *  refLock is used to 'lock' all the subpanels of any 'referenced' node
     *  displayed on the right side of the editor. i.e. the referenced data is
     *  displayed but can only be edited in the original location of the node
     */
    boolean refLock = false;

    /**
     *  this method recursively displays a node and its children in a Panel
     *  that appears on the right side of the display
     *  It looks for 'special editors' in the nodes data to determine how
     *  to display the subtree; if no special editor, a set of nested panels
     *  with simple text boxes is displayed.
     */
    void doPanels(DefaultMutableTreeNode node, JPanel panel) {
      boolean locked = false;
    // panel is the surrounding panel for this node
    // check to see if there is a special editor for this node
      NodeInfo inf = (NodeInfo)(node.getUserObject());
      DefaultMutableTreeNode refsNode = getRefsNode(node);
      if (refsNode!=null) {
        refLock = true;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)refsNode.getRoot();
        DefaultMutableTreeNode kid = (DefaultMutableTreeNode)refsNode.getFirstChild();
        NodeInfo kidni = (NodeInfo)(kid.getUserObject());
        String referencedId  = kidni.getPCValue();
        DefaultMutableTreeNode referencedNode = getReferencedNode(root,referencedId);
        node = referencedNode;
        inf = (NodeInfo)(node.getUserObject());
      }
      String temp = inf.getEditor();
      if (temp!=null) {
        if (temp.indexOf("LockedPanel")>-1) {
          locked = true;
          temp = null;
        }
      }
      if (temp!=null) {
        try {
          Object[] Args = new Object[] {node};
          Class[] ArgsClass = new Class[] {DefaultMutableTreeNode.class};
          Class componentDefinition = Class.forName(temp);
          Constructor ArgsConstructor = componentDefinition.getConstructor(ArgsClass);
          Object obj = createObject(ArgsConstructor,Args);

          // obj should be a component that can be added to a container (e.g. a descendent
          // of JPanel) with a constructor that takes a node as an argument
          if (obj!=null) {
            panel.add((Component)obj);
          }
        }
        catch (ClassNotFoundException e) {
          System.out.println(e);
        }
        catch (NoSuchMethodException e) {
          System.out.println(e);
        }
      }
      else {
        panel.add(getDataPanel(node, locked||refLock));
        // iterate over children of this node
        Enumeration nodes = node.children();
        // loop over child node
        while(nodes.hasMoreElements()) {
          DefaultMutableTreeNode nd = (DefaultMutableTreeNode)(nodes.nextElement());
          NodeInfo info = (NodeInfo)(nd.getUserObject());
//          Log.debug(0, "info.name: "+info.name);
          if (!((info.name).equals("#PCDATA")))
          {
            if (((info.name).indexOf("CHOICE")<0) &&
                ((info.name).indexOf("SEQUENCE")<0) )
            {
              JPanel new_panel = new JPanel();
              String borderTitle = info.toString();
              if (refLock) borderTitle = borderTitle + ":     (REFERENCE)";
              new_panel.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createTitledBorder(borderTitle),
                 BorderFactory.createEmptyBorder(4, 4, 4, 4)
                 ));
              numPanels++;
              if (numPanels<500) {   // limited for performance reasons
                new_panel.setLayout(new BoxLayout(new_panel,BoxLayout.Y_AXIS));
                panel.add(new_panel);
                doPanels(nd, new_panel);
              }
              else {
                String message = "<html><p>List Terminated Due to Large Size!<br>";
                message = message + "Click on SubNodes in the Outline on the Left to Edit those Items</html>";
                JLabel trunc = new JLabel(message);
                panel.add(trunc);
                break;
              }
            }
            else {
              doPanels(nd, panel);
            }
          }
        }
      }
      defaultPanel = true;
    }


    JPanel getDataPanel(DefaultMutableTreeNode node, boolean locked) {
        int panelWidth = topPanel.getWidth() - 40;

        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp,BoxLayout.Y_AXIS));
        jp.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel jp1 = new JPanel();
        jp1.setLayout(new BoxLayout(jp1,BoxLayout.Y_AXIS));
        jp1.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel jp2 = new JPanel();
        jp2.setLayout(new BoxLayout(jp2,BoxLayout.Y_AXIS));
        jp2.setAlignmentX(Component.LEFT_ALIGNMENT);
        jp1.setMaximumSize(new Dimension(panelWidth,45));
        jp1.setPreferredSize(new Dimension(panelWidth,45));
        jp2.setMaximumSize(new Dimension(panelWidth,25));
        jp.add(jp1);
        jp.add(jp2);
        NodeInfo info = (NodeInfo)(node.getUserObject());


        StringBuffer name = new StringBuffer();
        if (info.getHelp()!=null) {
          name.append(info.getHelp());
        }
        String helpString = name.toString();
        if (helpString.length()==0) {
          jp1.setMaximumSize(new Dimension(panelWidth,2));
          jp1.setPreferredSize(new Dimension(panelWidth,2));
        }
        if (helpString.length()>0) {
          JScrollPane jsp = new JScrollPane();
          jsp.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
          JTextArea jta = new JTextArea();
          jta.setFont(new Font("Dialog", Font.PLAIN, 10));
          jta.setLineWrap(true);
          jta.setWrapStyleWord(true);
          jta.setEditable(false);
          jta.setEnabled(false); // adding this line makes the help text gray & skips focus on help
          jta.setBackground(jp1.getBackground());
          jsp.getViewport().add(jta);
            jp1.add(jsp);
            jta.setText(helpString);
            jta.setCaretPosition(0);
        }
        //now check if there are child TEXT nodes
        Enumeration nodes = node.children();
        // loop over child node
        String txt ="";
        DefaultMutableTreeNode nd = null;
        while(nodes.hasMoreElements()) {
          nd = (DefaultMutableTreeNode)(nodes.nextElement());
          NodeInfo info1 = (NodeInfo)(nd.getUserObject());
          if ((info1.name).equals("#PCDATA")) {
            txt = info1.getPCValue();
            if (txt.equals("")) txt = " ";
          }
        }
          if (txt.length()>0) {
            JTextField jtf1 = new JTextField();
            jtf1.setPreferredSize(new Dimension(panelWidth,19));
            jp2.add(jtf1);
            nodeMap.put(jtf1,nd);  // for use in saving changes to text
            jtf1.addFocusListener(new dfhFocus());
     //       if (txt.equals("text")) { txt = " "; }
            jtf1.setText(txt.trim());
            jtf1.setEnabled(!locked);
          }

        return jp;
    }

    // get pixels from any component inside topPanel to top of topPanel
    int pixelsFromTop(JComponent comp) {
        int dist = 0;
        JComponent parent = (JComponent)comp.getParent();
        dist = dist + comp.getY();
        while (parent!=topPanel) {
            comp = parent;
            parent = (JComponent)comp.getParent();
            dist = dist + comp.getY();
        }
        return dist;
    }

class dfhAction implements java.awt.event.ActionListener
{
    public void actionPerformed(java.awt.event.ActionEvent event)
    {
      Object object = event.getSource();
      if (object instanceof JTextField)  {
        DefaultMutableTreeNode nd = (DefaultMutableTreeNode)nodeMap.get(object);
        NodeInfo info = (NodeInfo)(nd.getUserObject());
//        info.setPCValue(" "+((JTextField)object).getText());
        String temp = ((JTextField)object).getText().trim();
        ((JTextField)object).setText(temp);
        info.setPCValue(temp);
        if (treeModel!=null) {
          treeModel.reload();
        }
      }
    }
}

  class dfhFocus extends java.awt.event.FocusAdapter
  {
    public void focusLost(java.awt.event.FocusEvent event)
    {
      Object object = event.getSource();
      if (object instanceof JTextField) {

        DefaultMutableTreeNode nd = (DefaultMutableTreeNode) nodeMap.get(object);

        JTextField textField = (JTextField) object;
        if(!textField.getText().trim().equals("")){
          DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) nd.
              getParent();
          NodeInfo cni = null;
          while (parentNode != null) {
            cni = (NodeInfo) parentNode.getUserObject();

            if ( (cni.isCheckbox() || cni.isChoice()) && !cni.isSelected()) {
              cni.setSelected(true);
            }
            parentNode = (DefaultMutableTreeNode) parentNode.getParent();
          }
          if(container != null){
        	 // fixed bug 2200
            //container.setSelectedNodes( (DefaultMutableTreeNode) nd.getRoot());
          }
          if(tree != null){
            tree.repaint();
          }
        }
        NodeInfo info = (NodeInfo)(nd.getUserObject());
//        info.setPCValue(" "+((JTextField)object).getText());
        String temp = ((JTextField)object).getText().trim();
        ((JTextField)object).setText(temp);
        info.setPCValue(temp);
        if (treeModel!=null) {
//				        treeModel.reload();
        }
      }
    }

    public void focusGained(java.awt.event.FocusEvent event)
    {
      Object object = event.getSource();
      if (object instanceof JTextField) {
        int dist = pixelsFromTop((JComponent)object);
        topPanel.scrollRectToVisible(new Rectangle(0,dist,50,50));
        DefaultMutableTreeNode nd = (DefaultMutableTreeNode)nodeMap.get(object);
        DefaultMutableTreeNode prevnd = (DefaultMutableTreeNode)nd.getPreviousNode();
        if (container!=null) {
          container.setTreeValueFlag(false);
          TreePath tp = new TreePath(prevnd.getPath());
          tree.setSelectionPath(tp);
            tree.scrollPathToVisible(tp);
        }
      }
    }
  }





public static Object createObject(Constructor constructor, Object[] arguments) {
//      System.out.println ("Constructor: " + constructor.toString());
      Object object = null;
      try {
        object = constructor.newInstance(arguments);
//        System.out.println ("Object: " + object.toString());
        return object;
      } catch (InstantiationException e) {
          System.out.println(e);
      } catch (IllegalAccessException e) {
          System.out.println(e);
      } catch (IllegalArgumentException e) {
          System.out.println(e);
      } catch (InvocationTargetException e) {
          System.out.println(e);
      }
      return object;
   }

  /**
   *  Finds a node with the given id, starting at the input node.
   *  Note that id is an attribute with the name "ID"
   */
  private DefaultMutableTreeNode getReferencedNode(DefaultMutableTreeNode root, String id) {
    Enumeration enumeration = root.breadthFirstEnumeration();
    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode curNode = (DefaultMutableTreeNode)enumeration.nextElement();
      NodeInfo curni = (NodeInfo)curNode.getUserObject();
      String idval = curni.getAttrValue("id");
      if (idval!=null) {  // check its value
        if (idval.equals(id)) return curNode;
      }
    }
    return null; // didn't find a match
  }

  /**
   *  Checks to see if a child of the input node has the name 'references'
   *  If so, return that child; otherwise, return null
   */
  private DefaultMutableTreeNode getRefsNode(DefaultMutableTreeNode node) {
    DefaultMutableTreeNode ret = null;
    Enumeration enumeration = node.children();
    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode kid = (DefaultMutableTreeNode)enumeration.nextElement();
      if (kid==null) return ret;
      String name = ((NodeInfo)kid.getUserObject()).getName();
      if (name.equals("references")) {
        // before we return the node, check to see if it contans anything
        if (kid.getChildCount()<1) return ret;
        DefaultMutableTreeNode textNode = (DefaultMutableTreeNode)kid.getFirstChild();
        String tname = ((NodeInfo)textNode.getUserObject()).getName();
        if (tname.equalsIgnoreCase("#PCDATA")) {
          String val = textNode.toString().trim();
          if ((val==null)||(val.equals(""))) {
            return ret;
          }
          else {
            return kid;
          }
        }
        return ret;
      }
    }
    return ret;
  }

}
