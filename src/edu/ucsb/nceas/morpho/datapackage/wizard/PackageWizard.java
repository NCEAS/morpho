/**
 *  '$RCSfile: PackageWizard.java,v $'
 *    Purpose: A class that creates a custom data entry form that is made
 *             by parsing an xml file.  XML is then produced from the form
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-17 17:59:08 $'
 * '$Revision: 1.5 $'
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

package edu.ucsb.nceas.morpho.datapackage.wizard;

import edu.ucsb.nceas.morpho.framework.*;
import javax.swing.*;
import javax.swing.border.*; 
import java.io.*;
import java.util.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;

public class PackageWizard extends javax.swing.JFrame 
                           implements ActionListener, ItemListener
                                                                 
{
  private XMLElement doc = new XMLElement();
  private static PackageWizardParser pwp;
  JTabbedPane mainTabbedPane;
  JPanelWrapper docPanel;
  
  /**
   * constructor which initializes the window based on the paramater values
   * in the wizard tag of the xml configuration document.
   */
  public PackageWizard() 
  {
    doc = pwp.getDoc();
    Hashtable wizardAtts = doc.attributes;
    String size = (String)wizardAtts.get("size");
    //System.out.println("width: " + width + " height: " + height);
    //System.out.println("name: " + (String)wizardAtts.get("dtd"));
    setTitle((String)wizardAtts.get("dtd"));
    initComponents();
    pack();
    setSize(parseSize(size));
  }
  
  public PackageWizard(ClientFramework framework, Container contentPane, 
                       String framename)
  {
    try
    {
      //get configuration information
      ConfigXML config = framework.getConfiguration();
      Vector frameNameV = config.get("frameName");
      Vector frameLocationV = config.get("frameConfigFile");
      Vector mainFrameV = config.get("mainFrame");
      String mainFrame = (String)mainFrameV.elementAt(0);
      Vector saxparserV = config.get("saxparser");
      String saxparser = (String)saxparserV.elementAt(0);
      Hashtable frames = new Hashtable();
      
      for(int i=0; i<frameNameV.size(); i++)
      {
        frames.put((String)frameNameV.elementAt(i), 
                   (String)frameLocationV.elementAt(i));
      }
      
      //
      //File mainFile = new File((String)frames.get(mainFrame));
      if(!frames.containsKey(mainFrame))
      {
        framework.debug(1, "The frame name provided to PackageWizard is not " +
                           "a valid frame name as described in config.xml");
        framework.debug(1, "The valid names are: " + frames.toString());
        return;
      }
      
      File mainFile = new File((String)frames.get(mainFrame));
      FileReader xml = new FileReader(mainFile);
      pwp = new PackageWizardParser(xml, saxparser);
      doc = pwp.getDoc();
      
      mainTabbedPane = new JTabbedPane();
      mainTabbedPane.setPreferredSize(new Dimension(500,400));
      //mainTabbedPane.setLayout(new FlowLayout());
      contentPane.add(mainTabbedPane);
      docPanel = new JPanelWrapper();
      //createMenu(contentPane);
      docPanel.element = doc;
      createPanel(doc, contentPane, docPanel);
    }
    catch(Exception e)
    {
      framework.debug(9, "error initializing custom frame");
      e.printStackTrace();
    }
  }
  
  /**
   * Creates the panels and hands off tasks to other methods
   */
  private void initComponents()
  {
    Container contentPane = getContentPane();
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);  
    //contentPane.setLayout(/*new BoxLayout(contentPane, BoxLayout.Y_AXIS)*/
    //                       /*new GridLayout(5,0)*/);
    
    mainTabbedPane = new JTabbedPane();
    contentPane.add(mainTabbedPane);
    docPanel = new JPanelWrapper();
    createMenu(contentPane);
    docPanel.element = doc;
    createPanel(doc, contentPane, docPanel);
  }
  
  /**
   * creates the menu bar at the top of the screen
   */
  private void createMenu(Container contentPane)
  {
    JMenuBar menuBar;
    JMenu menu, submenu;
    JMenuItem menuItem;
    
    menuBar = new JMenuBar();
    setJMenuBar(menuBar);
    menu = new JMenu("File");
    menu.setMnemonic(KeyEvent.VK_F);
    menu.getAccessibleContext().setAccessibleDescription("File Menu");
    menuBar.add(menu);
    menuItem = new JMenuItem("Save", KeyEvent.VK_S);
    menuItem.getAccessibleContext().setAccessibleDescription("Save form to XML");
    menuItem.addActionListener(this);
    menu.add(menuItem);
    menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
    menuItem.getAccessibleContext().setAccessibleDescription("Exit Applicaiton");
    menuItem.addActionListener(this);
    menu.add(menuItem);
    menuBar.add(menu);
  }
  
  /**
   * handles the actions from the menus.  
   */
  public void actionPerformed(ActionEvent e) 
  {
    String command = e.getActionCommand();
    String paramString = e.paramString();
    Hashtable contentReps = new Hashtable();
    
    if(command.equals("Save"))
    {
      Stack fieldStack = new Stack();
      Vector pathReps = new Vector();
      int choice = JOptionPane.YES_OPTION;
      pathReps = createDocument(docPanel, pathReps, "");
      //System.out.println(pathReps);
      Hashtable content = createContentHash(docPanel, contentReps, "");
      //System.out.println(content.toString());
      if(content.containsKey("MISSINGREQUIREDELEMENTS"))
      {
        if(((String)content.get("MISSINGREQUIREDELEMENTS")).equals("true"))
        { //tell the user that there are missing required fields.
          choice = JOptionPane.showConfirmDialog(null, 
                               "This package may be invalid because certain \n" + 
                               "fields are marked as 'required' but are not " +
                               "filled in.\n" +
                               "Are you sure you want to save now?", 
                               "Invalid Document", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
        }
      }
      if(choice == JOptionPane.YES_OPTION)
      { //only save the document if it is valid or if the user wants an invalid
        //document
        StringBuffer xmldoc = createDocumentContent(pathReps, content);
        System.out.println(xmldoc);
      }
    }
    else if(command.equals("Exit"))
    {
      System.exit(0);
    }
  }

  public void itemStateChanged(ItemEvent e) 
  {
  
  }
  
  /**
   * This method creates the xml document(s) from the content of the panels.
   * It gets the documents structure from the createDocument method which
   * creates a list of all of the paths that should be generated from
   * the panel to complete the xml document.  This method parses those
   * paths and writes out tags and content for the xml document(s).
   * @param paths: a vector of the paths and the content from createDocument.
   * @param content: a hashtable of the content of each node with the path to 
   * the node as the key
   */
  private StringBuffer createDocumentContent(Vector paths, Hashtable content)
  {
    StringBuffer doc = new StringBuffer();
    Vector vStack = new Vector();
    
    for(int i=0; i<paths.size(); i++)
    {//put the paths into a vector for easy manipulation.
      String path = (String)paths.elementAt(i);
      String s = "";
      Vector pathVec = new Vector();
      for(int j=0; j<path.length(); j++)
      {//put each part of the path in a seperate string object in a vector
        if(path.charAt(j) == '/')
        {
          if(j!=0)
          {//discard the leading /
            pathVec.addElement(new String(s));
            s = "";
          }
        }
        else
        {
          s += path.charAt(j);
        }
      }
      pathVec.addElement(new String(s));
      //System.out.println(pathVec);
      vStack.addElement(pathVec); //vector of vectors
    }
    
    Vector elements = new Vector();
    int level = 0;
    String spaces = ""; //spaces is used for textual representation of the xml
    //System.out.println(vStack.toString());
    for(int i=0; i<vStack.size(); i++)
    { //go through the paths one by one.
      boolean samenode = false;
      if(i == 0)
      { //initial state
        Vector pathVec = (Vector)vStack.elementAt(i);
        String root = (String)pathVec.elementAt(i);
        elements.addElement(root);  
      }
      else
      { 
        Vector pathVec = (Vector)vStack.elementAt(i);
        int diff = 0;
        
        for(int j=0; j<pathVec.size(); j++)
        { //find the point at which the elements vector and the pathVec
          //diverge and record it as diff
          String pathvecstr = ((String)pathVec.elementAt(j)).trim();
          String elementstr = ((String)elements.elementAt(j)).trim();
          if((elements.size()-1) == j ||
            !pathvecstr.equals(elementstr))
          {
            diff=j; //record the divergence
            break;
          }
          else if((pathVec.size()-1 == j) && (j == pathVec.size()-1))
          {
            diff=j;
          }
        }
        
        if(diff == 0)
        { //if diff==0 then the whole content vector needs to become the element
          //vector and the start tags from each element need to be printed
          //doc.append("diff==0\n");
          elements = pathVec;
          
          for(int j=0; j<elements.size(); j++)
          {
            String startTag = (String)elements.elementAt(j);
            spaces += "  ";
            //System.out.println(spaces + "<" + startTag + ">");
            doc.append(spaces + "<" + startTag.trim() + ">\n");
            String keyPath = "";
          }
          String keyPath = "";
          for(int k=0; k<elements.size(); k++)
          {
            keyPath += "/" + pathVec.elementAt(k);
          }
          if(content.containsKey(keyPath))
          {
            if(!((String)content.get(keyPath)).equals(""))
            {
              doc.append(spaces + "  " + content.get(keyPath) + "\n");
            }
          }
        }
        else if(diff == (elements.size()-1))
        { //in this state, the vectors differ at the end of the elements
          //vector.  
          String e = (String)elements.elementAt(diff);
          String p = (String)pathVec.elementAt(diff);
          if(e.equals(p))
          { //if the tags at the end of elements and at diff of pathVec
            //are the same then we need to increment diff so that the
            //next tag will get taken.
            diff++;
          }
          
          for(int j=elements.size()-1; j>=diff; j--)
          { //print out any end tags that need to be closed.
            String endTag = (String)elements.remove(j);
            if(!endTag.equals(elements.elementAt(elements.size()-1)))
            { //make sure that the endTag is not on the end of the elements
              //vector.  if it is it is not time to print it yet.
              //System.out.println(spaces + "</" + endTag+ ">");
              doc.append(spaces + "</" + endTag.trim() + ">\n");
              spaces = spaces.substring(0, spaces.length() - 2);
            }
          }
          
          for(int j=diff; j<pathVec.size(); j++)
          { //print out any new start tags.
            elements.addElement(pathVec.elementAt(j));
            String startTag = (String)pathVec.elementAt(j);
            spaces += "  ";
            //System.out.println(spaces + "<" + startTag + ">");
            doc.append(spaces + "<" + startTag.trim() + ">\n");
            
            String keyPath = "";
            for(int k=0; k<elements.size(); k++)
            {
              keyPath += "/" + pathVec.elementAt(k);
            }
            //System.out.println("keypath: " + keyPath); 
            if(content.containsKey(keyPath))
            {
              if(!((String)content.get(keyPath)).equals(""))
              {
                //System.out.println(spaces + "  " + content.get(keyPath));
                doc.append(spaces + "  " + content.get(keyPath) + "\n");
              }
            }
          }
        }
        else if(diff < (elements.size()-1))
        { //in this state, there is a difference before the end of the 
          //elements vector so several elements items need to be removed and
          //ended.
          //doc.append("diff<elements.size()-1\n");
          for(int j=elements.size()-1; j>=diff; j--)
          { //end the overlapping tags.
            String endTag = (String)elements.remove(j);
            //System.out.println(spaces + "</" + endTag+ ">");
            doc.append(spaces + "</" + endTag.trim() + ">\n");
            spaces = spaces.substring(0, spaces.length() - 2);
          }
          for(int j=diff; j<pathVec.size(); j++)
          { //print out new start tags and push the new paths onto the
            //elements vector
            elements.addElement(pathVec.elementAt(j));
            String startTag = (String)pathVec.elementAt(j);
            spaces += "  ";
            //System.out.println(spaces + "<" + startTag + ">");
            doc.append(spaces + "<" + startTag.trim() + ">\n");
            
            String keyPath = "";
            for(int k=0; k<elements.size(); k++)
            {
              keyPath += "/" + pathVec.elementAt(k);
            }
            //System.out.println("keypath: " + keyPath);
            if(content.containsKey(keyPath))
            {
              if(!((String)content.get(keyPath)).equals(""))
              {
                //System.out.println(spaces + "  " + content.get(keyPath));
                doc.append(spaces + "  " + content.get(keyPath) + "\n");
              }
            }
          }
        }
      }
    }
    
    for(int i=elements.size()-1; i>=0; i--)
    { //print out the remainder of the elements vector to finish off the
      //document
      //System.out.println(spaces + "</" + elements.remove(i) + ">");
      doc.append(spaces + "</" + elements.remove(i) + ">\n");
      spaces = spaces.substring(0, spaces.length() - 2);
    }
    return doc;
  }
  
  /**
   * This method creates a hash table of the content of the text and combo
   * boxes so that the content can be related back to the path.  It is
   * essentially the same method as createDocument but with a hashtable
   * instead of a vector.
   * This method also checks to make sure that any element that was marked
   * as required has content.  If it does not it creates a hash in the
   * returned hashtable with a key of "MISSINGREQUIREDELENTS" and sets the 
   * value to true.
   */
  private Hashtable createContentHash(JPanelWrapper jpw, Hashtable paths, String fields)
  {
    String field = new String();   
    String endField = new String();
    Hashtable content = new Hashtable();
    for(int i=0; i<jpw.children.size(); i++)
    { 
      Hashtable atts = ((JPanelWrapper)jpw).element.attributes;
      if(atts.containsKey("field"))
      { //check to see if this group has a field associated with it
        field = (String)atts.get("field");
        if(!fields.endsWith(field))
        { //make sure it is not already there
          fields += "/" + field;
          paths.put(fields, "");
        }
      }
      
      try
      {
        //this recursion is the heart of this method.  If the cast in the 
        //recursion throws an exception then we are not in a group so go to 
        //the try statement and handle the text or combo box.
        //if the cast is accepted, then we are in another group and 
        //we need to recurse down to the next level.
        
        paths = createContentHash((JPanelWrapper)jpw.children.elementAt(i), 
                                paths, fields);
      }
      catch(java.lang.ClassCastException cce)
      {
        try
        { //get the textfield and append its field onto the paths
          JTextFieldWrapper jtfw = (JTextFieldWrapper)jpw.children.elementAt(i);
          //paths.addElement(fields + "/" + jtfw.element.attributes.get("field"));
          String allowNullS = "TRUE";
          boolean allowNullB = true;
          if(jtfw.element.attributes.containsKey("allowNull"))
          {
            allowNullS = (String)jtfw.element.attributes.get("allowNull");
            allowNullS = allowNullS.toUpperCase();
            if(allowNullS.equals("FALSE"))
            {
              allowNullB = false;
            }
          }
          
          String jtfwContent = jtfw.getText();
          if(jtfwContent.equals("") && !allowNullB)
          {
            paths.put("MISSINGREQUIREDELEMENTS", "true");
          }
          String localfield = (String)jtfw.element.attributes.get("field");
          
          while(paths.containsKey(fields + "/" + localfield))
          {
            localfield += " ";
          }
          
          paths.put(fields + "/" + localfield, jtfwContent);
          
          
          //put the field and the content into the hash.
        }
        catch(java.lang.ClassCastException cce2)
        { //get the combobox and append its field onto the paths
          JComboBoxWrapper jcbw = (JComboBoxWrapper)jpw.children.elementAt(i);
          String allowNullS = "TRUE";
          boolean allowNullB = true;
          if(jcbw.element.attributes.containsKey("allowNull"))
          {
            allowNullS = (String)jcbw.element.attributes.get("allowNull");
            allowNullS = allowNullS.toUpperCase();
            if(allowNullS.equals("FALSE"))
            {
              allowNullB = false;
            }
          }
          //paths.addElement(fields + "/" + jcbw.element.attributes.get("field"));
          String jcbwContent = (String)jcbw.getSelectedItem();
          if(jcbwContent.equals("") && !allowNullB)
          {
            paths.put("MISSINGREQUIREDELEMENTS", "true");
          }
          String localfield = (String)jcbw.element.attributes.get("field");
          
          while(paths.containsKey(fields + "/" + localfield))
          {
            localfield += " ";
          }
          
          paths.put(fields + "/" + localfield, jcbwContent);
          //paths.put(fields + "/" + jcbw.element.attributes.get("field"), jcbwContent);
          //put the field and the content into the hash.
        }
      }
      catch(java.util.EmptyStackException ese)
      { //this is bad if this happens!
        System.out.println("empty stack");
        ese.printStackTrace(System.out);
      }
    }
    
    return paths; //return the content hash
  }
  
  /**
   * When the user chooses to save the document, this method builds all of the
   * paths in the xml tree along with a hashtable of their values
   * for use in the createDocumentContent method.
   */
  private Vector createDocument(JPanelWrapper jpw, Vector paths, String fields)
  {
    String field = new String();   
    String endField = new String();
    Hashtable content = new Hashtable();
    for(int i=0; i<jpw.children.size(); i++)
    { 
      Hashtable atts = ((JPanelWrapper)jpw).element.attributes;
      if(atts.containsKey("field"))
      { //check to see if this group has a field associated with it
        field = (String)atts.get("field");
        if(!fields.endsWith(field))
        { //make sure it is not already there
          fields += "/" + field;
          paths.addElement(fields);
        }
      }
      
      try
      {
        //this recursion is the heart of this method.  If the cast in the 
        //recursion throws an exception then we are not in a group so go to 
        //the try statement and handle the text or combo box.
        //if the cast is accepted, then we are in another group and 
        //we need to recurse down to the next level.
        
        paths = createDocument((JPanelWrapper)jpw.children.elementAt(i), 
                                         paths, fields);
      }
      catch(java.lang.ClassCastException cce)
      {
        try
        { //get the textfield and append its field onto the paths
          JTextFieldWrapper jtfw = (JTextFieldWrapper)jpw.children.elementAt(i);
          String localpath = fields + "/" + 
                             (String)jtfw.element.attributes.get("field");
          while(paths.contains(localpath))
          {
            localpath += " ";
          }
          paths.addElement(localpath);
          String jtfwConent = jtfw.getText();
          content.put(paths.elementAt(paths.size()-1).toString(), jtfwConent); 
        }
        catch(java.lang.ClassCastException cce2)
        { //get the combobox and append its field onto the paths
          JComboBoxWrapper jcbw = (JComboBoxWrapper)jpw.children.elementAt(i);
          String localpath = fields + "/" + 
                             (String)jcbw.element.attributes.get("field");
          while(paths.contains(localpath))
          {
            localpath += " ";
          }
          paths.addElement(localpath);
          String jcbwContent = (String)jcbw.getSelectedItem();
          content.put((String)paths.elementAt(paths.size()-1), jcbwContent);
        }
      }
      catch(java.util.EmptyStackException ese)
      { //this is bad if this happens!
        System.out.println("empty stack");
        ese.printStackTrace(System.out);
      }
    }

    return paths; //return the paths
  }
  
  private void createPanel(XMLElement e, final Container contentPane, 
                           final JPanelWrapper parentPanel)
  {
    createPanel(e, contentPane, parentPanel, null);
  }
  
  /**
   * The method goes through the XMLElement doc and creates from it the panel
   * and text element structure.  It also builds a tree out of the J*
   * elements so that the structure of the document can be recreated.
   */
  private void createPanel(XMLElement e, final Container contentPane, 
                           final JPanelWrapper parentPanel, Integer prevIndex)
  {
    for(int i=0; i<e.content.size(); i++)
    {
      final XMLElement tempElement = (XMLElement)e.content.elementAt(i);
      final int tempi = i;
      final JPanelWrapper tempPanel = new JPanelWrapper();
      
      if(tempElement.name.equals("group"))
      {//add a new path or panel
        JButton button = null;
        tempPanel.element = tempElement;
        
        if(tempElement.attributes.containsKey("type") && 
          ((String)tempElement.attributes.get("type")).equals("panel"))
        {
          if(tempElement.attributes.containsKey("size"))
          {
            String size = (String)tempElement.attributes.get("size");
            tempPanel.setPreferredSize(parseSize(size));
          }
          //tempPanel.setLayout(new /*GridLayout(0,1)*/FlowLayout());
          BoxLayout box = new BoxLayout(tempPanel, BoxLayout.Y_AXIS);
          tempPanel.setLayout(box);
          parentPanel.children.addElement(tempPanel);
          JScrollPane tempScrollPane = new JScrollPane(tempPanel);
          mainTabbedPane.addTab((String)tempElement.attributes.get("label"), 
                                tempScrollPane);
        }
        else
        {
          tempPanel.setBorder(BorderFactory.createCompoundBorder(
                              BorderFactory.createTitledBorder(
                              (String)tempElement.attributes.get("label")),
                              BorderFactory.createEmptyBorder(4, 4, 4, 4)));
          
          if(tempElement.attributes.containsKey("repeatable"))
          { //allow this panel to be repeated.
            String repeatable = (String)tempElement.attributes.get("repeatable");
            repeatable = repeatable.toUpperCase();
            if(repeatable.equals("YES"))
            {
              button = new JButton("Repeat");
              button.addActionListener(
                new ActionListener() 
                {
                  public void actionPerformed(ActionEvent e) 
                  { //if the user wants to repeat this group, make a copy 
                    //of it and stick it in the tree, then repaint the main 
                    //panel
                    XMLElement newtempElement = new XMLElement(tempElement);
                    JPanelWrapper newtempPanel = new JPanelWrapper();
                    newtempPanel.element = newtempElement;
                    XMLElement newe = new XMLElement();
                    newe.content.addElement(newtempElement);
                    createPanel(newe, contentPane, parentPanel, 
                                new Integer(parentPanel.children.indexOf(tempPanel))); 
                    contentPane.repaint();
                  }
                }
              );
            }
          }
          
          if(button != null)
          {
            JPanel layoutpanel = new JPanel();
            layoutpanel.add(button);
            tempPanel.add(layoutpanel);
            tempPanel.add(new JPanel());
          }
          
          if(tempElement.attributes.containsKey("size"))
          {
            String size = (String)tempElement.attributes.get("size");
            //tempPanel.setPreferredSize(parseSize(size));
          }
          
          //tempPanel.setLayout(new /*GridLayout(0,2)*/FlowLayout());
          BoxLayout box = new BoxLayout(tempPanel, BoxLayout.Y_AXIS);
          tempPanel.setLayout(box);
          if(prevIndex == null)
          {
            parentPanel.children.addElement(tempPanel);
          }
          else
          {
            parentPanel.children.insertElementAt(tempPanel, prevIndex.intValue()+1);
          }
          
          parentPanel.add(new JScrollPane(tempPanel));
        }
      }
      else if(tempElement.name.equals("textbox"))
      {//add a new text box
        final JTextFieldWrapper textfield = new JTextFieldWrapper();
        textfield.element = tempElement;
        textfield.setPreferredSize(new Dimension(10, 20));
        final JLabel label = new JLabel(
                                 (String)tempElement.attributes.get("label"));
        Integer size = new Integer((String)tempElement.attributes.get("size"));
        JButton button = null;
        boolean required = false;
        
        if(tempElement.attributes.containsKey("repeatable"))
        { //if the text box is repeatable, make the label into a button
          //and when the button is pressed, repeat the element.
          String repeatable = (String)tempElement.attributes.get("repeatable");
          repeatable = repeatable.toUpperCase();
          if(repeatable.equals("YES"))
          {
            button = new JButton();
            button.addActionListener(
              new ActionListener() 
              {
                public void actionPerformed(ActionEvent e) 
                { //the user wants to repeat this element, make a copy of it
                  //and stick it in the tree.  then repaint.
                  XMLElement newtempElement = new XMLElement(tempElement);
                  JLabel newLabel = new JLabel(label.getText());
                  JTextFieldWrapper newtextfield = new JTextFieldWrapper();
                  newtextfield.element = newtempElement;
                  newtextfield.setPreferredSize(new Dimension(10, 20));
                  Integer newsize = new Integer(
                                 (String)newtempElement.attributes.get("size"));
                  newtextfield.setColumns(newsize.intValue());
                  int textfieldindex = parentPanel.children.indexOf(textfield);
                  parentPanel.children.insertElementAt(newtextfield, 
                                                       textfieldindex+1);
                  
                  int numcomponents = parentPanel.getComponentCount();
                  int insertindex = numcomponents;
                  for(int j=0; j<numcomponents; j++)
                  {
                    Component nextcomp = parentPanel.getComponent(j);
                    if(nextcomp == textfield)
                    {
                      insertindex = j;
                    }
                  }
                  JPanel labelAndText = new JPanel();
                  labelAndText.add(newLabel);
                  labelAndText.add(newtextfield);
                  
                  parentPanel.add(newLabel, insertindex + 1);
                  parentPanel.add(newtextfield, insertindex + 2);
                  //parentPanel.add(labelAndText /*,insertindex + 1*/);
                  contentPane.repaint();
                }
              }
            );
          }
        }
        
        if(tempElement.attributes.containsKey("allowNull"))
        {
          String allowNull = (String)tempElement.attributes.get("allowNull");
          allowNull = allowNull.toUpperCase();
          if(allowNull.equals("FALSE"))
          {
            required = true;
          }
        }
        if(required)
        {
          label.setForeground(Color.red);
        }
        textfield.setColumns(size.intValue());
        parentPanel.children.addElement(textfield);
        
        if(button != null)
        {
          JPanel layoutpanel = new JPanel();
          button.add(label);
          //layoutpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
          layoutpanel.add(button);
          parentPanel.add(button);
        }
        else
        {
          parentPanel.add(label);
        }
        parentPanel.add(textfield);
      }
      else if(tempElement.name.equals("combobox"))
      {//add a new combo box with it's enumerated items
        final JComboBoxWrapper combofield = new JComboBoxWrapper();
        Vector items = new Vector();
        for(int j=0; j<tempElement.content.size(); j++)
        {//get all of the items and add them to the combo box
          XMLElement itemElement = (XMLElement)tempElement.content.elementAt(j);
          String item = (String)itemElement.attributes.get("value");
          combofield.addItem(item);
        }
        combofield.element = tempElement;
        combofield.setEditable(true);
        combofield.setAlignmentX(Component.LEFT_ALIGNMENT);
        final JLabel label = new JLabel((String)tempElement.attributes.get("label"));
        JButton button = null;
        boolean required = false;
        
        if(tempElement.attributes.containsKey("repeatable"))
        { //if the combo box is repeatable, make the label into a button
          //and when the button is pressed, repeat the element.
          String repeatable = (String)tempElement.attributes.get("repeatable");
          repeatable = repeatable.toUpperCase();
          if(repeatable.equals("YES"))
          {
            button = new JButton();
            button.addActionListener(
              new ActionListener() 
              {
                public void actionPerformed(ActionEvent e) 
                { //the user wants to repeat this element, make a copy of it
                  //and stick it in the tree.  then repaint.
                  XMLElement newtempElement = new XMLElement(tempElement);
                  JLabel newLabel = new JLabel(label.getText());
                  JComboBoxWrapper newcombofield = new JComboBoxWrapper();
                  newcombofield.element = newtempElement;
                  for(int j=0; j<newtempElement.content.size(); j++)
                  {//get all of the items and add them to the combo box
                    XMLElement itemElement = (XMLElement)newtempElement.content.elementAt(j);
                    String item = (String)itemElement.attributes.get("value");
                    newcombofield.addItem(item);
                  }
                  newcombofield.setEditable(true);
                  newcombofield.setAlignmentX(Component.LEFT_ALIGNMENT);
                  int combofieldindex = parentPanel.children.indexOf(combofield);
                  parentPanel.children.insertElementAt(newcombofield, combofieldindex+1);
                  
                  int numcomponents = parentPanel.getComponentCount();
                  int insertindex = numcomponents;
                  for(int j=0; j<numcomponents; j++)
                  {
                    Component nextcomp = parentPanel.getComponent(j);
                    if(nextcomp == combofield)
                    {
                      insertindex = j;
                    }
                  }
                  
                  parentPanel.add(newLabel, insertindex + 1);
                  parentPanel.add(newcombofield, insertindex + 2);
                  contentPane.repaint();
                }
              }
            );
          }
        }
        
        if(tempElement.attributes.containsKey("allowNull"))
        {
          String allowNull = (String)tempElement.attributes.get("allowNull");
          allowNull = allowNull.toUpperCase();
          if(allowNull.equals("FALSE"))
          {
            required = true;
          }
        }
        
        parentPanel.children.addElement(combofield);
        
        if(required)
        {
          label.setForeground(Color.red);
        }
        
        if(button != null)
        {
          JPanel layoutpanel = new JPanel();
          //layoutpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
          button.add(label);
          layoutpanel.add(button);
          parentPanel.add(button);
        }
        else
        {
          parentPanel.add(label);
        }
        parentPanel.add(combofield);
      }
      
      //do the recursive call to the next level of the document:
      //this if construct ensures that the correct parent is sent to the 
      //next level of recursion.  if we are already in a group
      //element then send tempPanel (which is the newly created panel)
      //if we are in another type of element then send the previous
      //parent because we haven't gone out of the existing group yet.
      if(tempElement.name.equals("group") || 
         tempElement.name.equals("wizard"))
      {
        //System.out.println("tempPanel: " + tempPanel.element.name + ":" + 
        //                    tempPanel.element.attributes.toString());
        createPanel(tempElement, contentPane, tempPanel);
      }
      else
      {
        //System.out.println("parentPanel: " + parentPanel.element.name + ":" + 
        //                    parentPanel.element.attributes.toString());
        createPanel(tempElement, contentPane, parentPanel);
      }
    }
  }
  
  /**
   * parses a NxM (ex. 500x500) string dimension into a Dimension object.  
   */
  private Dimension parseSize(String size)
  {
    int xindex = size.indexOf("x");
    int width = new Integer(size.substring(0, xindex)).intValue();
    int height = new Integer(size.substring(xindex+1, size.length())).intValue();
    return new Dimension(width, height);
  }
  
  /**
   * wrapper for a JComponent that allows the tracing of paths back through
   * the form to recreate the XML document.
   */
  private class JComponentWrapper extends JComponent
  {
    public Vector children = new Vector();
    public XMLElement element;
    
    JComponentWrapper()
    {
      
    }
  }
  
   /**
   
   */
  private class JPanelWrapper extends JPanel
  {
    public Vector children = new Vector();
    public XMLElement element;
    
    JPanelWrapper()
    {
      
    }
  }
  
   /**
  .
   */
  private class JComboBoxWrapper extends JComboBox
  {
    public XMLElement element;
    
    JComboBoxWrapper()
    {
      
    }
  }
  
   /**

   */
  private class JTextFieldWrapper extends JTextField
  {
    public XMLElement element;
    
    public JTextFieldWrapper()
    {
      
    }
  }
  
  private class JFieldWrapper extends JComponent
  {
    private JTextFieldWrapper textfield = null;
    private JComboBoxWrapper combobox = null;
    private JLabel label;
    
    public JFieldWrapper(String label, JTextFieldWrapper textfield)
    {
      if(combobox == null)
      {
        this.label = new JLabel(label);
        this.textfield = textfield;
      }
      else
      {
        //error because either combobox or text box needs to be null
        System.out.println("error1 in JFieldWrapper: this class can only be " +
        "instantiated for a combobox or a textbox but not both");
      }
    }
    /*
    public JFieldWrapper(String label, JComboBoxWrapper combobox)
    {
      if(textbox == null)
      {
        this.label = new JLabel(label);
        this.combobox = combobox;
      }
      else
      {
         //error because either combobox or text box needs to be null
        System.out.println("error2 in JFieldWrapper: this class can only be " +
        "instantiated for a combobox or a textbox but not both");
      }
    
      public JPanel getWrappedElement()
      {
        JPanel tempPanel = new JPanel();
        tempPanel.add(this.label);
        if(textbox == null && combobox != null)
        {
          tempPanel.add(this.combobox);
        }
        else if(textbox != null && combobox == null)
        {
          tempPanel.add(this.textbox);
        }
        else
        {
          //error because either combobox or text box needs to be null
          System.out.println("error3 in JFieldWrapper: this class can only be " +
          "instantiated for a combobox or a textbox but not both");
        }
      }
    }
    */
  }
  
  /**
   * Test method that allows command line execution.  The first command line
   * argument is the xml file name that you wish to use as a confg file.
   * USAGE: java PackageWizard configfile.xml
   */
  public static void main(String[] args)
  {
    String filename = args[0];
    try
    {
      FileReader xml = new FileReader(new File(filename));
      pwp = new PackageWizardParser(xml, 
			      "org.apache.xerces.parsers.SAXParser");
      //System.out.println("Doc is: " );
      //pwp.printDoc(pwp.getDoc());
      new PackageWizard().show();
    }
    catch(Exception e)
    {
      System.out.println("error in main");
      e.printStackTrace(System.out);
    }
  }
}
