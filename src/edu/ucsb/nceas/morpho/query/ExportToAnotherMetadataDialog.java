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
package edu.ucsb.nceas.morpho.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.morpho.util.XMLTransformer;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * Represents a dialog which can transform current data package to another metadata
 * language. The metadata language list is configured in config.xml at .morpho dir.
 * @author tao
 *
 */
public class ExportToAnotherMetadataDialog implements Command
{
  private static final String STYLESHEETLIST = "styleSheetList";
  private static final String STYLESHEET = "styleSheet";
  private static final String NAME = "name";
  private static final String LABEL = "label";
  private static final String LOCATION = "location";
  private static final String SLASH = "/";
  private static final String TITLE = "Export to Another Metadata Language";
  private static final String EXPORTBUTTONNAME = "Export";
  
  Vector<StyleSheet> styleSheetList = new Vector<StyleSheet>();
  private boolean debugHilite = false;
  private File lastChosenOutputDir = null;
  private File styleSheetFile = null;
  private File outputFile = null;
  private JDialog parent = null;
  private String docid = null;
  private String documentLocation = null;
  private JFileChooser exportFileChooser = new JFileChooser();
  
  
  /**
   * Constructor with JDialog as parent
   * @param parent 
   * @param docid  the docid which will be exported
   * @param documentLocation the document location (local/metacat)
   */
  public ExportToAnotherMetadataDialog(JDialog parent, String docid, String documentLocation)
  {
    this.parent = parent;
    this.docid = docid;
    this.documentLocation = documentLocation;
    readStyleSheetList();
   
  }
  
  /**
   * Initialize the GUI when the command executes
   */    
  public void execute(ActionEvent event)
  { 
    if(styleSheetList.size()==0)
    {
      Log.debug(5, "Morpho couldn't find any configuration for another metadata language at configure file!");
      return;
    }
    if(docid == null || docid.equals(""))
    {
      Log.debug(5, "There is no docid specified for export!");
      return;
    }
    if(documentLocation == null )
    {
      Log.debug(5, "Morpho couldn't find the location of this document and couldn't exmport it.");
      return;
    }     
    if(documentLocation.equals("") )
    {
      Log.debug(5, "Please save the document first before exporting it.");
      return;
    }
    
    if(parent != null)
    {
      parent.setVisible(false);
    }
    initGUI();
    if(parent != null)
    {
      parent.dispose();
      parent = null;
    }
    
  }
  
  /*
   * Initialize GUI for this a file chooser
   */
  private void initGUI()
  {
    if(lastChosenOutputDir != null)
    {
      exportFileChooser.setCurrentDirectory(lastChosenOutputDir);
    }
    exportFileChooser.setDialogTitle(TITLE);
    exportFileChooser.setAcceptAllFileFilterUsed(false);
    exportFileChooser.resetChoosableFileFilters();
    for(int i=0; i<styleSheetList.size(); i++)
    {
      StyleSheet sheet = styleSheetList.elementAt(i);
      MetadataLanguageFileFilter fileFilter = new MetadataLanguageFileFilter(sheet);
      exportFileChooser.addChoosableFileFilter(fileFilter);
    }
    //exportFileChooser.addActionListener(new ExportAction());
    int returnValue = exportFileChooser.showDialog(parent, EXPORTBUTTONNAME);
    if(returnValue == JFileChooser.APPROVE_OPTION)
    {
      outputFile = exportFileChooser.getSelectedFile();
      if(outputFile != null)
      {
        lastChosenOutputDir = outputFile.getParentFile();
      }
      String styleSheetLocation = null;
      Log.debug(35, "Export");
      //export(outputFile,styleSheetLocation);
    }
    else
    {
      Log.debug(35, "Cancel");
      Log.debug(30, "File choosing was canceled in ExportToAnotherMetadataLanguageDialog select style sheet location");
    }
  }
  
 
  
  
  
  
  
  /*
   * Make label for given text and dimension
   */
  private JLabel makeLabel(String text, Dimension dims)
  {
    if (text==null) text="";
    JLabel label = new JLabel(text);    
    Util.setPrefMaxSizes(label, dims);
    label.setMinimumSize(dims);
    label.setAlignmentX(SwingConstants.LEADING);
    return label;
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
   * Export the current data package to output file name in another metadata format.
   * Note: this method doesn't check if the parameters are null since they were checked
   * on method actionPerformed.
   */
  private void export(File outputFile, String styleSheetLocation)
  {
    //get output file writer
    FileWriter outputFileWriter = null;
      
    //get style sheet reader
    File styleSheetFile = new File(styleSheetLocation);
    FileReader styleSheetReader = null;
    File styleSheetDir = null;
    try
    {
      styleSheetReader = new FileReader(styleSheetFile);
      styleSheetDir = styleSheetFile.getParentFile();
    }
    catch(Exception e)
    {
      Log.debug(5, "Morpho couldn't find a style sheet file at location "+styleSheetLocation);
      return;
    }
    
    //Get eml reader
    /*AbstractDataPackage dataPackage = UIController.getInstance().getCurrentAbstractDataPackage();
    if(dataPackage == null)
    {
      Log.debug(5, "The current data package is null and we couldn't transform it to another metadata format. ");
      return;
    }*/
    //String eml = XMLUtilities.getDOMTreeAsString(dataPackage.getMetadataNode());
    //StringReader emlReader = new StringReader(eml);
    //Document emlDoc = dataPackage.
    //transform
    
    try
    {
      XMLTransformer transformer = XMLTransformer.getInstance();
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider = 
                 services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      Document emlDoc = dataPackage.getDocumentNode(docid, documentLocation);
      if(emlDoc == null)
      {
        Log.debug(5, "Morpho couldn't generate the document for the package "+docid);
        return;
      }
      Reader anotherMetadataReader = transformer.transform(emlDoc, styleSheetReader, 
                                        styleSheetDir.getAbsolutePath());
      outputFileWriter = new FileWriter(outputFile);
      char[] chartArray = new char[4*1024];
      int index = anotherMetadataReader.read(chartArray);
      while(index != -1)
      {
        outputFileWriter.write(chartArray, 0, index);
        outputFileWriter.flush();
        index = anotherMetadataReader.read(chartArray);
      }
      anotherMetadataReader.close();
      outputFileWriter.close();
      JOptionPane.showMessageDialog(parent, "Package export is complete!");
      //this.setVisible(false);
      //this.dispose();
    }
    catch(Exception e)
    {
      Log.debug(5, "Morpho couldn't transform the eml to the metadata language "+
                   "since "+e.getMessage());
    }
   
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
  
  /*
   * Represents a customized file filter for JFileChooser. It will used the StyleSheet
   * label as file description
   */
  private class MetadataLanguageFileFilter extends FileFilter
  {
    private StyleSheet sheet = null;
    /**
     * Constructor
     */
    public MetadataLanguageFileFilter(StyleSheet sheet)
    {
      this.sheet = sheet;
    }
    
    /**
     * Accepts any file type
     */
    public boolean accept(File file)
    {
      return true;
    }
    
    /**
     * Gets the description from style sheet label. If style sheet is null, empty
     * String will be returned.
     */
    public String getDescription()
    {
      if(sheet != null)
      {
        return sheet.getLabel();
      }
      else
      {
        return "";
      }
    }
  }
  
}
