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
 *     '$Date: 2001-06-12 23:09:35 $'
 * '$Revision: 1.26 $'
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
  private String saxparser;
  private ClientFramework framework;
  private String globalDtd;
  private String globalDoctype;
  private String globalRoot;
  
  /**
   * constructor which initializes the window based on the paramater values
   * in the wizard tag of the xml configuration document.
   */
  public PackageWizard() 
  {
    doc = pwp.getDoc();
    Hashtable wizardAtts = doc.attributes;
    String size = (String)wizardAtts.get("size");
    setTitle((String)wizardAtts.get("dtd"));
    initComponents();
    pack();
    setSize(parseSize(size));
  }
  
  /**
   * constructor which creates a package wizard frame in the given contentPane
   * using the given framefile (xml configuration file).
   * @param framework: the framework in which this wizard is created
   * @param contentPane: the Container in which this wizard is created
   * @param framefile: the configuration file used to create this wizard
   */
  public PackageWizard(ClientFramework framework, Container contentPane, 
                       String framefile)
  {
    try
    {
      this.framework = framework;
      //get configuration information
      ConfigXML config = framework.getConfiguration();
      Vector saxparserV = config.get("saxparser");
      saxparser = (String)saxparserV.elementAt(0);
      
      //get the config file and parse it
      File mainFile = new File(framefile);
      FileReader xml = new FileReader(mainFile);
      pwp = new PackageWizardParser(xml, saxparser);
      doc = pwp.getDoc();
      globalDtd = pwp.getDtd();
      globalDoctype = pwp.getDoctype();
      globalRoot = pwp.getRoot();
      
      Hashtable wizardAtts = doc.attributes;
      String size = (String)wizardAtts.get("size");
      
      //create the initial tabbed pane
      mainTabbedPane = new JTabbedPane();
      mainTabbedPane.setPreferredSize(parseSize(size));
      contentPane.add(mainTabbedPane);
      docPanel = new JPanelWrapper();
      docPanel.element = doc;
      //create the content of the initial frame
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
    mainTabbedPane = new JTabbedPane();
    contentPane.add(mainTabbedPane);
    docPanel = new JPanelWrapper();
    createMenu(contentPane);
    docPanel.element = doc;
    createPanel(doc, contentPane, docPanel);
  }
  
  public void setVisible(boolean visible)
  {
    mainTabbedPane.setVisible(visible);
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
   * gets the xml produced from the wizard
   */
  public String getXML()
  {
    int choice = JOptionPane.YES_OPTION;
    Stack fieldStack = new Stack();
    Vector pathReps = new Vector();
    Hashtable contentReps = new Hashtable();
    
    pathReps = createDocument(docPanel, pathReps, ""); //IMPORTANT
    Hashtable content = createContentHash(docPanel, contentReps, ""); //IMPORTANT
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
      StringBuffer xmldoc = createDocumentContent(pathReps, content); //IMPORTANT
      String doctype = "<?xml version=\"1.0\"?>\n<!DOCTYPE " + globalRoot;
      doctype += " PUBLIC \"" + globalDoctype + "\" \"" + globalDtd + "\">";
      //doctype += " PUBLIC \"" + globalDoctype + "\">";
      doctype += "\n" + xmldoc.toString();
      return doctype;
    }
    else
    {
      return null;
    }
  }
  
  /**
   * handles the actions from the menus if this package wizard is 
   * run in stand alone mode.  
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
      //printVector(pathReps);
      Hashtable content = createContentHash(docPanel, contentReps, "");
      //printHashtable(content);
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

  /**
   * not used in this implementation
   */
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
    String attName = "";
    boolean attFlag = false;
    
    for(int i=0; i<paths.size(); i++)
    {//put the paths into a vector for easy manipulation.
      String path = (String)paths.elementAt(i);
      //System.out.println("path: " + path);
      String s = "";
      Vector pathVec = new Vector();
      for(int j=0; j<path.length(); j++)
      {//put each part of the path in a seperate string object in a vector
        if(path.charAt(j) == '/')
        {
          if(j!=0)
          {//discard the leading /
            //System.out.println("s: " + s);
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
      //System.out.println("s2: " + s);
      //System.out.println("pathVec: " + pathVec);
      vStack.addElement(pathVec); //vector of vectors
    }
    
    Vector elements = new Vector();
    int level = 0;
    String spaces = ""; //used for textual representation of the xml
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
        
        attFlag = false;
        for(int j=0; j<pathVec.size(); j++)
        {
          String pathvecstr = ((String)pathVec.elementAt(j)).trim();
          if(pathvecstr.indexOf("@") != -1)
          {
            attFlag = true;
            attName = pathvecstr;
            break;
          }
        }
        
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
        
        boolean continueFlag = false;
        boolean emptyflag = true;
        
/////////this if statemen deals entirely with tags that have attributes////////
        if(attFlag)
        { //we are in an attribute group.  print out each of the next elements
          //as attributes and values
          //doc.append("diff: " + diff + "\n");
          //doc.append("elements size: " + (elements.size()-1) + "\n");
          //doc.append("elements: " + elements.toString() + "\n");
          //doc.append("pathVec: " + pathVec.toString() + "\n");
          
          if(diff == elements.size()-1)
          { //first print out the end tag for the last element.
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
                if(endTag.equals("ENDEMPTY"))
                {
                  doc.append("/>\n");
                }
                else
                {
                  doc.append(spaces + "</" + endTag.trim() + ">\n");
                }
                spaces = spaces.substring(0, spaces.length() - 2);
              }
            }
          }
          else if(diff < elements.size()-1)
          {
            for(int j=elements.size()-1; j>=diff; j--)
            { //end the overlapping tags.
              String endTag = (String)elements.remove(j);  
              //System.out.println(spaces + "</" + endTag+ ">");
              doc.append(spaces + "</" + endTag.trim() + ">\n");
              spaces = spaces.substring(0, spaces.length() - 2);
            }
          }
          
          String attributeTag = (String)pathVec.elementAt(diff);
          String tag = attributeTag.substring(1, attributeTag.length());
          String tagContent = "";
          spaces += "  ";
          doc.append(spaces + "<" + tag);
          i++;
          elements = pathVec;
          pathVec = (Vector)vStack.elementAt(i);
          
          while(attributeTag.indexOf("@") != -1)
          { //while there is an @ sign in the name of the parent tag, we need
            //to take the next tags and make them into attributes
            String attribute = (String)pathVec.elementAt(pathVec.size()-1);
            if(attribute.equals("FIELDVALUE"))
            { //the FIELDVALUE attribute is a keyword that tells you which
              //text box to take the value of the actual tag from
              emptyflag = false;
              String keyPath = "";
              for(int k=0; k<pathVec.size(); k++)
              {
                keyPath += "/" + pathVec.elementAt(k);
              }
              
              if(content.containsKey(keyPath))
              { //get the content and save it
                if(!((String)content.get(keyPath)).equals(""))
                {
                  tagContent = (String)content.get(keyPath);
                }
              }
            }
            else
            {
              doc.append(" " + attribute + "=\"");
              
              //get the path to the content in the hash
              String keyPath = "";
              for(int k=0; k<pathVec.size(); k++)
              {
                keyPath += "/" + pathVec.elementAt(k);
              }
              
              if(content.containsKey(keyPath))
              { //get the content and print it
                if(!((String)content.get(keyPath)).equals(""))
                {
                  doc.append(content.get(keyPath));
                }
              }
              doc.append("\"");
            }
            
            i++;
            //go the the next path
            elements = pathVec;
            pathVec = (Vector)vStack.elementAt(i);

            if(diff >= pathVec.size())
            { //if we have gone into a different tag then we need to take 
              //a step back, break out of this loop and continue in the 
              //main for i loop
              continueFlag = true;
              i--;
              break;
            }
            
            attributeTag = (String)pathVec.elementAt(diff);
            
            if(!attributeTag.equals(attName))
            { //if there are two attributes with an @ sign in a row, this
              //if statement makes sure that they get made into different
              //tags and not just stuck together.
              continueFlag = true;
              i--;
              break;
            }
          }
          
          if(emptyflag)
          {
            //doc.append("/>\n");
          }
          else
          {
            doc.append(">\n"); //end the tag and add the content that was saved
                               //from before
          }
          
          if(tagContent != "")
          {
            doc.append(spaces + "  " + tagContent + "\n");
          }
          
          for(int j=diff+1; j<elements.size(); j++)
          { //remove any extra attributes that we have already used.
            elements.remove(j);
          }
          
          //add the end tag
          String attributeEndTag = (String)elements.remove(diff);
          //remove the @ sign
          attributeEndTag = attributeEndTag.substring(1, 
                                                      attributeEndTag.length());
          if(!emptyflag)
          {
            elements.addElement(attributeEndTag);
          }
          else
          {
            elements.addElement("ENDEMPTY");
          }
          
          attFlag = false;
          
          if(continueFlag)
          {
            continue;
          }
        }
        
//1///////////////////////////diff==0///////////////////////////////////////////
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
//2//////////////////diff==elements.size()-1///////////////////////////////////
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
              if(endTag.equals("ENDEMPTY"))
              {
                doc.append("/>\n");
              }
              else
              {
                doc.append(spaces + "</" + endTag.trim() + ">\n");
              }
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
//3///////////////////////diff<element.size()-1////////////////////////////////
        else if(diff < (elements.size()-1))
        { //in this state, there is a difference before the end of the 
          //elements vector so several elements items need to be removed and
          //ended.
          //doc.append("diff<elements.size()-1\n");
          for(int j=elements.size()-1; j>=diff; j--)
          { //end the overlapping tags.
            String endTag = (String)elements.remove(j);  
            //System.out.println(spaces + "</" + endTag+ ">");
            if(endTag.equals("ENDEMPTY"))
            {
              doc.append("/>\n");
            }
            else
            {
              doc.append(spaces + "</" + endTag.trim() + ">\n");
            }
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
      String endTag = (String)elements.remove(i);
      if(endTag.equals("ENDEMPTY"))
      {
        doc.append("/>\n");
      }
      else
      {
        doc.append(spaces + "</" + endTag.trim() + ">\n");
      }
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
            if(allowNullS.equals("NO"))
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
            if(allowNullS.equals("NO"))
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
  
  /**
   * The method goes through the XMLElement doc and creates from it the panel
   * and text element structure.  It also builds a tree out of the J*
   * elements so that the structure of the document can be recreated.  
   * in this method a prevIndex is not required.
   */
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
       //this part of the if statement builds all of the containers that
       //the text and combo boxes will be contained in.  It encases them
       //in scroll panes in case they are too big for the screen.
        JButton button = null;
        tempPanel.element = tempElement;
        
        if(tempElement.attributes.containsKey("type") && 
          ((String)tempElement.attributes.get("type")).equals("panel"))
        { //go here if we are building a new tab.
          if(tempElement.attributes.containsKey("size"))
          {
            String size = (String)tempElement.attributes.get("size");
            tempPanel.setPreferredSize(parseSize(size));
          }
          
          if(tempElement.attributes.containsKey("visible"))
          {
            String visible = (String)tempElement.attributes.get("visible");
            if(visible.equals("no"))
            {
              tempPanel.setVisible(false);
            }
          }
          
          //tempPanel.setLayout(new /*GridLayout(0,1)*/FlowLayout());
          BoxLayout box = new BoxLayout(tempPanel, BoxLayout.Y_AXIS);
          //if you want to change the layout of the tabbed pane change it here
          tempPanel.setLayout(box);
          parentPanel.children.addElement(tempPanel);
          JScrollPane tempScrollPane = new JScrollPane(tempPanel);
          mainTabbedPane.addTab((String)tempElement.attributes.get("label"), 
                                tempScrollPane);
        }
        else
        { //go here if we are building a panel that is not tabbed.
          if(tempElement.attributes.containsKey("label"))
          {
            tempPanel.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder(
                                (String)tempElement.attributes.get("label")),
                                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
          }
          else
          {
            tempPanel.setBorder(null);
          }
          
          if(tempElement.attributes.containsKey("repeatable"))
          { //allow this panel to be repeated.
            String repeatable = (String)tempElement.attributes.get("repeatable");
            repeatable = repeatable.toUpperCase();
            if(repeatable.equals("YES"))
            {
              button = new JButton("Repeat");
              //if an element is repeatable, we put the label into a button
              //and add an action listener to reproduce the element when the
              //button is pressed
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
          { //if the config file has a size attribute then set it here
            //if it doesn't then let the layout manager choose the size
            //of the panel
            String size = (String)tempElement.attributes.get("size");
            tempPanel.setPreferredSize(parseSize(size));
          }
          
          //layout management for internal panels
          BoxLayout box = new BoxLayout(tempPanel, BoxLayout.Y_AXIS);
          tempPanel.setLayout(box);
          if(tempElement.attributes.containsKey("layout"))
          { 
            String layout = (String)tempElement.attributes.get("layout");
            if(layout.equals("flow"))
            {
              tempPanel.setLayout(new FlowLayout());
            }
          }
          
          if(prevIndex == null)
          { //add this group as a child of it's parent for later reconstruction.
            parentPanel.children.addElement(tempPanel);
          }
          else
          { //if an item is repeated it has to be inserted next to the element
            //that created it.
            parentPanel.children.insertElementAt(tempPanel, 
                                                 prevIndex.intValue()+1);
          }
          
          if(tempElement.attributes.containsKey("visible"))
          {
            String visible = (String)tempElement.attributes.get("visible");
            if(visible.equals("no"))
            {
              tempPanel.setVisible(false);
              parentPanel.add(tempPanel);
            }
          }
          else
          {
            parentPanel.add(/*new JScrollPane(*/tempPanel);
            //add the panel in a scroll pane in case it's too big.
          }
        }
      }
      else if(tempElement.name.equals("textbox"))
      {//add a new text box 
        final JTextFieldWrapper textfield = new JTextFieldWrapper();
        textfield.element = tempElement;
        
        final JLabel label = new JLabel(
                                 (String)tempElement.attributes.get("label"));
        Integer size = new Integer(10);
        if(tempElement.attributes.containsKey("size"))
        {
          size = new Integer((String)tempElement.attributes.get("size"));
        }
        JButton button = null;
        boolean required = false;
        String defaultText = null;
        
        if(tempElement.attributes.containsKey("defaulttext"))
        {
          defaultText = (String)tempElement.attributes.get("defaulttext");
        }
        
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
                  Integer newsize = new Integer(10);
                  if(newtempElement.attributes.containsKey("size"))
                  {
                    newsize = new Integer(
                              (String)newtempElement.attributes.get("size"));
                  }
                  newtextfield.setColumns(newsize.intValue());
                  int textfieldindex = parentPanel.children.indexOf(textfield);
                  parentPanel.children.insertElementAt(newtextfield, 
                                                       textfieldindex+1);
                  JPanel parentPanel2 = (JPanel)textfield.getParent().getParent();
                  int numcomponents = parentPanel2.getComponentCount();
                  
                  int insertindex = numcomponents;
                  for(int j=0; j<numcomponents; j++)
                  {
                    Component nextcomp = parentPanel2.getComponent(j); 
                    try
                    {
                      JTextField t = (JTextField)((Component)
                                            ((JPanel)nextcomp).getComponent(1));
                      if(t == textfield)
                      {
                        insertindex = j;
                      }
                    }
                    catch(ClassCastException cce)
                    {
                      
                    }
                    catch(ArrayIndexOutOfBoundsException aioobe)
                    {
                      
                    }
                  }
                  
                  if(tempElement.attributes.containsKey("defaulttext"))
                  {
                    String defaultText = (String)
                                     tempElement.attributes.get("defaulttext");
                    newtextfield.setText(defaultText);
                  }
                  JPanel layoutpanel = new JPanel();
                  layoutpanel.add(newLabel);
                  layoutpanel.add(newtextfield);
                  //Dimension d = parentPanel2.getSize();
                  //parentPanel2.setSize(d.width, d.height + 10);
                  //System.out.println("new size: " + d.width + "x" + d.height);
                  parentPanel2.add(layoutpanel, insertindex + 1);
                  //parentPanel.add(newLabel, insertindex + 1);
                  //parentPanel.add(newtextfield, insertindex + 2);
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
          if(allowNull.equals("NO"))
          {
            required = true;
          }
        }
        
        if(required)
        {
          label.setForeground(Color.red);
        }
        
        if(tempElement.attributes.containsKey("editable"))
        {
          String editable = (String)tempElement.attributes.get("editable");
          if(editable.equals("no"))
          {
            textfield.setEnabled(false);
            textfield.setBackground(new Color(230,230,230));
          }
        }
        
        //set the user defined size of the text field
        textfield.setColumns(size.intValue());
        parentPanel.children.addElement(textfield);
        
        JPanel layoutpanel = new JPanel();
        if(button != null)
        { //if this item is repeatable add the button
          
          button.add(label);
          layoutpanel.add(button);
          //parentPanel.add(button);
        }
        else
        { //add just the label if it is not repeatable
          //parentPanel.add(label);
          layoutpanel.add(label);
        }
        
        if(tempElement.attributes.containsKey("visible"))
        {
          String visible = (String)tempElement.attributes.get("visible");
          if(visible.equals("no"))
          {
            layoutpanel.setVisible(false);
            textfield.setVisible(false);
          }
        }
       
        textfield.setText(defaultText);
        //parentPanel.add(textfield);
        layoutpanel.add(textfield);
        parentPanel.add(layoutpanel);
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
                  parentPanel.children.insertElementAt(newcombofield, 
                                                       combofieldindex+1);
                  
                  //note that you have to use getParent here because all 
                  //text and combo boxes are inside of another panel for layout
                  //reasons
                  JPanel parentPanel2 = (JPanel)combofield.getParent().getParent();
                  int numcomponents = parentPanel2.getComponentCount();
                  int insertindex = numcomponents;
                  
                  for(int j=0; j<numcomponents; j++)
                  { //add the combo box in the correct position
                    Component nextcomp = parentPanel2.getComponent(j); 
                    try
                    {
                      JComboBox c = (JComboBox)((Component)
                                            ((JPanel)nextcomp).getComponent(1));
                      if(c == combofield)
                      {
                        insertindex = j;
                      }
                    }
                    catch(ClassCastException cce)
                    {
                      
                    }
                    catch(ArrayIndexOutOfBoundsException aioobe)
                    {
                      
                    }
                  }
                  
                  if(newtempElement.attributes.containsKey("defaulttext"))
                  {
                    String defaultText = (String)
                                         newtempElement.attributes.get("defaulttext");
                    combofield.setSelectedItem(defaultText);
                  }
                  
                  JPanel layoutpanel = new JPanel();
                  layoutpanel.add(newLabel);
                  layoutpanel.add(newcombofield);
                  parentPanel2.add(layoutpanel, insertindex + 1);
                  //parentPanel.add(newLabel, insertindex + 1);
                  //parentPanel.add(newcombofield, insertindex + 2);
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
          if(allowNull.equals("NO"))
          {
            required = true;
          }
        }
        
        //make this combobox a child of the parent frame
        parentPanel.children.addElement(combofield);
        
        if(required)
        {
          label.setForeground(Color.red);
        }
        
        JPanel layoutpanel = new JPanel();
        
        if(button != null)
        {
          
          button.add(label);
          layoutpanel.add(button);
          //parentPanel.add(button);
        }
        else
        {
          //parentPanel.add(label);
          layoutpanel.add(label);
        }
        
        if(tempElement.attributes.containsKey("defaulttext"))
        {
          String defaultText = (String)
                               tempElement.attributes.get("defaulttext");
          combofield.setSelectedItem(defaultText);
        }
        layoutpanel.add(combofield);
        parentPanel.add(layoutpanel);
        //parentPanel.add(combofield);
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
  
  private void printVector(Vector v)
  {
    for(int i=0; i<v.size(); i++)
    {
      System.out.println(v.elementAt(i).toString().trim());
    }
  }
  
  private void printHashtable(Hashtable h)
  {
    Enumeration keys = h.keys();
    while(keys.hasMoreElements())
    {
      String key = (String)keys.nextElement();
      System.out.print(key + " : " );
      System.out.print(h.get(key));
      System.out.println();
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
