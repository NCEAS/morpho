/**
 *  '$RCSfile: ProfileDialog.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-08-26 03:58:27 $'
 * '$Revision: 1.30 $'
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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Log;

/**
 * Represents a dialog which can transform current data package to another metadata
 * language.
 * @author tao
 *
 */
public class ExportToAnotherMetadataDialog extends JDialog
{
  private static final String STYLESHEETLIST = "styleSheetList";
  private static final String STYLESHEET = "styleSheet";
  private static final String NAME = "name";
  private static final String LABEL = "label";
  private static final String LOCATION = "location";
  private static final String SLASH = "/";
  
  Vector<StyleSheet> styleSheetList = new Vector();
  
  private JPanel centralPanel  = null;
  private JPanel buttonPanel = null;
  private JLabel outputFileLocationLabel = null;
  private JTextField outputFileLocationField = null;
  private JLabel metadataLanguageLabel = null;
  private JList metadataLanguageList = null;
  private JLabel otherStyleSheetLocationLabel = null;
  private JTextField otherStyleSheetLocationField = null;

  /**
   * Default constructor
   */
  public ExportToAnotherMetadataDialog()
  {
    super(UIController.getInstance().getCurrentActiveWindow());
    readStyleSheetList();
    initGUI();
  }
  
  /*
   * Initialize GUI for this dialog
   */
  private void initGUI()
  {
    setModal(true);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setTitle("Export to Another Metadata Language");
    createCentralPanel();
    createButtonPanel();
    Container contentPanel = getContentPane();
    contentPanel.setLayout(new BorderLayout());
    contentPanel.add(centralPanel, BorderLayout.CENTER);
    contentPanel.add(buttonPanel, BorderLayout.SOUTH);
    setResizable(false);
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2 ,
            (screenDim.height - frameDim.height) /2);
  }
  
  /*
   * Create central panel. It contains output file location field, output file
   * locator button, metadata language list and style sheet locator (optional).
   * This panel will locate on central position of the dialog
   */
  private void createCentralPanel()
  {
    centralPanel = new JPanel();
    centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));
    JPanel outputFilePanel = new JPanel();
    
  }
  
  /*
   * Create a panel containing cancel and transform button.
   * It will locate on south of dialog.
   */
  private void createButtonPanel()
  {
    buttonPanel = new JPanel();
  }
  
  
  /*
   * Read the style sheet list from configure file
   */
  private void readStyleSheetList()
  {
    if(Morpho.thisStaticInstance != null)
    {
      ConfigXML config = Morpho.thisStaticInstance.getConfiguration();
      if(config != null)
      {
        NodeList nodeList = config.getPathContent(SLASH+SLASH+STYLESHEETLIST+SLASH+STYLESHEET);
        if(nodeList != null)
        {
          for(int i=0; i<nodeList.getLength(); i++)
          {
            Node styleSheetNode = nodeList.item(i);
            if(styleSheetNode != null)
            {
              NodeList children = styleSheetNode.getChildNodes();
              if(children != null)
              {
                StyleSheet sheet = null;
                for(int j=0; j<children.getLength(); j++)
                {
                  Node child = children.item(j);
                  String name = getValueForElement(child, NAME);
                  if(name != null)
                  {
                    sheet = new StyleSheet(name);
                  }
                  String label = getValueForElement(child, LABEL);
                  if(sheet != null && label != null)
                  {
                    sheet.setLabel(label);
                  }
                  String location = getValueForElement(child, LOCATION);
                  if(sheet != null && location != null)
                  {
                    sheet.setLocation(location);
                  }
                }
                styleSheetList.add(sheet);
              }
            }
          }
        }
      }
    }
  }
  
  /*
   * Gets a value for the given elementName. null will be returned if couldn't be found.
   */
  private String  getValueForElement(Node node, String elementName)
  {
    String value = null;
    if(node != null && elementName != null)
    {
      if(node.getNodeType()==Node.ELEMENT_NODE && elementName.equals(node.getNodeName()))
      {
        NodeList grandChildren = node.getChildNodes();
        for(int k=0; k<grandChildren.getLength(); k++)
        {
          Node textNode = grandChildren.item(k);
          if (textNode.getNodeType()==Node.TEXT_NODE
                              || textNode.getNodeType()==Node.CDATA_SECTION_NODE) 
          {
            value = textNode.getNodeValue();
            Log.debug(30, "The "+elementName +" has value "+value);
            break;
          }
        }
      }
    }
    return value;
  }
  
  /*
   * represents a configuration for a style sheet
   */
  private class StyleSheet
  {
    private String name = null;
    private String label  = null;
    private String location = null;
    
    /**
     * Constructor
     * @param name of the style sheet
     */
    public StyleSheet(String name)
    {
      this.name = name;
    }
    
    /**
     * Sets the label of the style sheet
     * @param label
     */
    public void setLabel(String label)
    {
      this.label = label;
    }
    
    /**
     * Gets the label of the style sheet
     * @return
     */
    public String getLabel()
    {
      return this.label;
    }
    
    /**
     * Sets the location of the style sheet
     * @param location
     */
    public void setLocation(String location)
    {
      this.location = location;
    }
    
    /**
     * Gets the location of the style sheet
     * @return
     */
    public String getLocation()
    {
      return this.location;
    }
  }
  
}
