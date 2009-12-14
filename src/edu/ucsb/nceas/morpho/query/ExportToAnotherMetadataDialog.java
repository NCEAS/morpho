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
public class ExportToAnotherMetadataDialog extends JDialog implements Command
{
  private static final String STYLESHEETLIST = "styleSheetList";
  private static final String STYLESHEET = "styleSheet";
  private static final String NAME = "name";
  private static final String LABEL = "label";
  private static final String LOCATION = "location";
  private static final String SLASH = "/";
  private static final String OTHER = "Other";
  private static final  Dimension TEXTFIELD_DIMS = new Dimension(400,20);
  private static final  Dimension LABEL_DIMS = new Dimension(150,20);
  private static final Dimension DEFAULT_SPACER_DIMS = new Dimension(15, 15);
  private static final int WIDTH = 800;
  private static final int HEIGHT = 350;
  private static final int EXTRAL = 75;
  private static final int PADDING = 5;
  private static final int HEADER = 30;
  private static final String SELECT = "Select";
  private static final String INITFIELDVALUE = "  Use button to select a file -->";
  private static final Color CONTENT_HILITE_BG_COLOR = new Color(175, 0, 0);
  private static final Color CONTENT_HILITE_FG_COLOR = new Color(255, 255, 255);
  private static final Color REGULAR_TEXT_COLOR = Color.BLACK;
  
  Vector<StyleSheet> styleSheetList = new Vector<StyleSheet>();
  private boolean debugHilite = false;
  private File lastChosenOutputDir = null;
  private File lastChosenStyleSheetDir = null;
  private File styleSheetFile = null;
  
  private JPanel centralPanel  = null;
  private JPanel buttonPanel = null;
  private JPanel otherStyleSheetLocationPanel = null;
  private JLabel outputFileLocationLabel = null;
  private JTextField outputFileLocationField = null;
  private JLabel metadataLanguageLabel = null;
  private JComboBox metadataLanguageList = null;
  private JLabel otherStyleSheetLocationLabel = null;
  private JTextField otherStyleSheetLocationField = null;
  private JDialog parent = null;
  private String docid = null;
  private String documentLocation = null;
  
  
  /**
   * Constructor with JDialog as parent
   * @param parent 
   * @param docid  the docid which will be exported
   * @param documentLocation the document location (local/metacat)
   */
  public ExportToAnotherMetadataDialog(JDialog parent, String docid, String documentLocation)
  {
    super(parent);
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
    if(parent == null)
    {
      Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
      Rectangle frameDim = getBounds();
      /*setLocation((screenDim.width - frameDim.width) / 2 ,
              (screenDim.height - frameDim.height) /2);*/
      this.setBounds((screenDim.width - frameDim.width) / 2, (screenDim.height - frameDim.height) /2,  
                            WIDTH, HEIGHT);
    }
    else
    {
      double parentX = parent.getLocation().getX();
      double parentY = parent.getLocation().getY();
      int parentWidth = parent.getWidth();
      int parentHeight = parent.getHeight();
      double centerX = parentX + 0.5 * parentWidth;
      double centerY = parentY + 0.5 * parentHeight;
      int dialogX = (new Double(centerX - 0.5 * WIDTH)).intValue();
      int dialogY = (new Double(centerY - 0.5 * HEIGHT)).intValue();
      this.setBounds(dialogX, dialogY,  WIDTH, HEIGHT);
    }
    setResizable(false);
    setVisible(true);
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
    
    centralPanel.add(Box.createVerticalStrut(HEADER));
    
    //makeSpacer();
    //output fiel panel : lable, field and button
    JPanel outputFilePanel = new JPanel();
    outputFilePanel.setLayout(new BoxLayout(outputFilePanel, BoxLayout.X_AXIS));
    outputFileLocationLabel = makeLabel("Output File Name:", LABEL_DIMS);
    outputFilePanel.add(outputFileLocationLabel);
    JPanel outputFileRightPanel = new JPanel();
    outputFileRightPanel.setLayout(new BoxLayout(outputFileRightPanel, BoxLayout.X_AXIS));
    outputFileLocationField = makeOneLineTextField(INITFIELDVALUE); 
    outputFileLocationField.setEditable(false);
    outputFileRightPanel.add(outputFileLocationField);
    outputFileRightPanel.add(Box.createHorizontalStrut(PADDING));
    JButton outputFileLocationButton = new JButton(SELECT);
    ActionListener outputFileLocationButtonListener = new OutputFileLocationListener(this);
    outputFileLocationButton.addActionListener(outputFileLocationButtonListener);
    outputFileRightPanel.add(outputFileLocationButton);
    outputFilePanel.add(outputFileRightPanel);
    centralPanel.add(outputFilePanel);
    //makeSpacer();
    
    //metadata language select panel
    JPanel metadataSelectionPanel = new JPanel();
    metadataSelectionPanel.setLayout(new BoxLayout(metadataSelectionPanel, BoxLayout.X_AXIS));
    metadataLanguageLabel = makeLabel("Metadata Format:",  LABEL_DIMS);
    metadataSelectionPanel.add(metadataLanguageLabel);
    JPanel metadataRightPanel = new JPanel();
    metadataRightPanel.setLayout(new BoxLayout(metadataRightPanel, BoxLayout.X_AXIS));
    metadataLanguageList = makeMetadataLanguageSelectionList();
    metadataRightPanel.add(metadataLanguageList);
    metadataRightPanel.add(Box.createHorizontalStrut(EXTRAL));
    metadataSelectionPanel.add(metadataRightPanel);
    //System.out.println("the button size is "+outputFileLocationButton.getPreferredSize().getWidth());
    //metadataSelectionPanel.add(Box.createHorizontalStrut(outputFileLocationButton.getWidth()));
    centralPanel.add(metadataSelectionPanel);
    
    otherStyleSheetLocationPanel = new JPanel();
    centralPanel.add(otherStyleSheetLocationPanel);
    
    //centralPanel.add(Box.createVerticalGlue());
    
    //if there is only "other" option, we should display the style sheet locator widget
    if(metadataLanguageList != null)
    {
      String initialValue = (String)metadataLanguageList.getSelectedItem();
      if(initialValue != null && initialValue.equals(OTHER))
      {
        createCentralPanelWithStyleSheetLocator();
      }
    }
  }
  
  /*
   * When user choose "other" option in metadata list, we should bring a 
   * new widget for user to choose a style sheet from file system directly.
   */
  private void createCentralPanelWithStyleSheetLocator()
  {
    if(otherStyleSheetLocationPanel != null && centralPanel != null)
    {
      centralPanel.remove(otherStyleSheetLocationPanel);
      otherStyleSheetLocationPanel = new JPanel();
      
      otherStyleSheetLocationPanel.setLayout(new BoxLayout(otherStyleSheetLocationPanel, BoxLayout.X_AXIS));
      otherStyleSheetLocationLabel = makeLabel("Style Sheet Name", LABEL_DIMS);
      otherStyleSheetLocationPanel.add(otherStyleSheetLocationLabel);
      
      JPanel otherStyleSheetRightPanel = new JPanel();
      otherStyleSheetRightPanel.setLayout(new BoxLayout(otherStyleSheetRightPanel, BoxLayout.X_AXIS));
      if(otherStyleSheetLocationField == null)
      {
        otherStyleSheetLocationField =  makeOneLineTextField(INITFIELDVALUE); 
      }
      else
      {
       String fieldValue = otherStyleSheetLocationField.getText();
       otherStyleSheetLocationField =  makeOneLineTextField(fieldValue);
      }
      otherStyleSheetLocationField.setEditable(false);
      otherStyleSheetRightPanel.add(otherStyleSheetLocationField);
      
      
      otherStyleSheetRightPanel.add(Box.createHorizontalStrut(PADDING));
      JButton styleSheetLocationButton = new JButton(SELECT);
      ActionListener styleSheetLocationButtonListener = new StyleSheetFileLocationListener(this);
      styleSheetLocationButton.addActionListener(styleSheetLocationButtonListener);
      otherStyleSheetRightPanel.add(styleSheetLocationButton);
      
      otherStyleSheetLocationPanel.add(otherStyleSheetRightPanel);
      centralPanel.add(otherStyleSheetLocationPanel);
      
      validate();
      repaint();
    }
  }
  
  /*
   * When user choose an option (rather than "Other" in metadata list, we should bring a 
   * new widget for user without the style sheet location chooser.
   */
  private void createCentralPanelWithoutStyleSheetLocator()
  {
    if(otherStyleSheetLocationPanel != null && centralPanel != null)
    {
      centralPanel.remove(otherStyleSheetLocationPanel);
      otherStyleSheetLocationPanel = new JPanel();
      centralPanel.add(otherStyleSheetLocationPanel);
      
      invalidate();
      repaint();
    }
  }
  
  
  /*
   * Create a panel containing cancel and transform button.
   * It will locate on south of dialog.
   */
  private void createButtonPanel()
  {
    buttonPanel = new JPanel();
    JButton cancelButton = new JButton("Cancel");
    ActionListener cancelAction = new DialogCancelAction(this);
    cancelButton.addActionListener(cancelAction);
    JButton exportButton = new JButton("Export");
    ActionListener exportAction = new DialogExportAction();
    exportButton.addActionListener(exportAction);
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createHorizontalStrut(PADDING));
    buttonPanel.add(exportButton);
  }
  
  /*
   * Makes a metadata language selection list.
   */
  private JComboBox makeMetadataLanguageSelectionList()
  {
    JComboBox selectionList = null;
    // the size lable list should be 1 more than the style sheet list, since it has "Other" option.
    String[] styleSheetLabelList = new String[styleSheetList.size()+1];
    for(int i=0; i<styleSheetList.size(); i++)
    {
      styleSheetLabelList[i]= ((StyleSheet)styleSheetList.elementAt(i)).getLabel();
    }
    //add other to be the last option
    styleSheetLabelList[styleSheetList.size()] = OTHER;
    selectionList = new JComboBox(styleSheetLabelList);
    MetadataLanguageSelectionListener listener = new MetadataLanguageSelectionListener();
    selectionList.addActionListener(listener);
    selectionList.setEditable(false);
    Util.setPrefMaxSizes(selectionList, TEXTFIELD_DIMS);
    return selectionList;
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
   * Make one line text field for given initialValue
   */
  private static JTextField makeOneLineTextField(String initialValue) 
  {
    if (initialValue==null) initialValue="";
    JTextField field = new JTextField();
    Util.setPrefMaxSizes(field, TEXTFIELD_DIMS);
    field.setText(initialValue);
    return field;
  }
  
  /*
   * Represents a listener to metadata language selection list
   */
  private class MetadataLanguageSelectionListener implements ActionListener
  {
    public void actionPerformed(ActionEvent e) 
    {
      if(metadataLanguageList != null)
      {
        String selectedValue = (String)metadataLanguageList.getSelectedItem();
        Log.debug(30, "In ExportToAnotherMetadataDialog.MetadataLanguageSelectionListener,  the selected value is "+selectedValue);
        if(selectedValue != null && selectedValue.equals(OTHER) )
        {
          createCentralPanelWithStyleSheetLocator();
        }
        else
        {
          createCentralPanelWithoutStyleSheetLocator();
        }
      }
    }
  }
  
  /*
   * Represents the action when user click cancel action on this dialog
   */
  private class DialogCancelAction implements ActionListener
  {
    private JDialog dialog = null;
    /**
     * Consturctor
     * @param dialog
     */
    public DialogCancelAction(JDialog dialog)
    {
      this.dialog = dialog;
    }
    /**
     * Cancel the dialog
     */
    public void actionPerformed(ActionEvent e) 
    {
      dialog.setVisible(false);
      dialog.dispose();
    }
  }
  
  /*
   * Represents the action when user click export action on this dialog
   */
  private class DialogExportAction implements ActionListener
  {
   
    /**
     * Does transform action
     */
    public void actionPerformed(ActionEvent e) 
    {
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
        Log.debug(5, "Please save the document first before exmporting it.");
        return;
      }
      
      Util.unhiliteComponent(outputFileLocationLabel, null, REGULAR_TEXT_COLOR);
      Util.unhiliteComponent(otherStyleSheetLocationLabel, null, REGULAR_TEXT_COLOR);
      String outputFileName = outputFileLocationField.getText();
      String styleSheetLocation = null;
      if(outputFileName == null || outputFileName.trim().equals("")||
          outputFileName.equals(INITFIELDVALUE))
      {
        Util.hiliteComponent(outputFileLocationLabel, CONTENT_HILITE_BG_COLOR, CONTENT_HILITE_FG_COLOR);
        //Util.unhiliteComponent(outputFileLocationLabel, null, REGULAR_TEXT_COLOR);
        return;
      }
      else if(((String)metadataLanguageList.getSelectedItem()).equals(OTHER))
      {
        //this is on "Other" option
        styleSheetLocation = otherStyleSheetLocationField.getText();
        if(styleSheetLocation ==null || styleSheetLocation.trim().equals("") ||
            styleSheetLocation.equals(INITFIELDVALUE))
        {
          Util.hiliteComponent(otherStyleSheetLocationLabel, CONTENT_HILITE_BG_COLOR, CONTENT_HILITE_FG_COLOR);
          return;
        }
      }
      else
      {
        //this is on chosen a given metadata language option
        int index = metadataLanguageList.getSelectedIndex();
        StyleSheet styleSheet = styleSheetList.elementAt(index);
        styleSheetLocation = styleSheet.getLocation();
        if(styleSheetLocation ==null || styleSheetLocation.trim().equals("") ||
            styleSheetLocation.equals(INITFIELDVALUE))
        {
          Log.debug(5, "The Metadata format "+metadataLanguageList.getSelectedItem()+" doesn't have a valid "+
                   "style sheet location. Please check the config.xml in your .morpho folder.");
          return;
        }
      }
      Log.debug(30, "The output file location in ExportToAnotherMedataDialog is "+outputFileName);
      Log.debug(30, "The style sheet location in ExportToAnotherMedataDialog is "+styleSheetLocation);
      export(outputFileName,styleSheetLocation);
    }
  }
  
  /*
   * Export the current data package to output file name in another metadata format.
   * Note: this method doesn't check if the parameters are null since they were checked
   * on method actionPerformed.
   */
  private void export(String outputFileName, String styleSheetLocation)
  {
    //get output file writer
    File outputFile = new File(outputFileName);
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
      JOptionPane.showMessageDialog(this, "Package export is complete!");
      this.setVisible(false);
      this.dispose();
    }
    catch(Exception e)
    {
      Log.debug(5, "Morpho couldn't transform the eml to the metadata language "+
                   "since "+e.getMessage());
    }
   
  }
  
  
  /*
   * When user choose "other" option in metadata list. it will bring new panel for
   * selecting style sheet directly from file system. This listener is for the style sheet
   * file selection button
   * It will bring a file selector.
   */
  private class StyleSheetFileLocationListener implements ActionListener
  {
    private Component parent = null;
    /**
     * Constructor
     * @param parent of the button
     */
    public StyleSheetFileLocationListener(Component parent)
    {
      this.parent = parent;
    }
    
    /**
     * When the button is executed. It will bring a file chooser for user
     * to select output file location
     */
    public void actionPerformed(ActionEvent e) 
    {
      JFileChooser styleSheetFileChooser = new JFileChooser();
      if(lastChosenStyleSheetDir != null)
      {
        styleSheetFileChooser.setCurrentDirectory(lastChosenStyleSheetDir);
      }
      int returnValue = styleSheetFileChooser.showOpenDialog(parent);
      if(returnValue == JFileChooser.APPROVE_OPTION)
      {
        styleSheetFile = styleSheetFileChooser.getSelectedFile();
        if(styleSheetFile != null)
        {
          otherStyleSheetLocationField.setText(styleSheetFile.getAbsolutePath());
          lastChosenStyleSheetDir = styleSheetFile.getParentFile();
        }
      }
      else
      {
        Log.debug(30, "File choosing was canceled in ExportToAnotherMetadataLanguageDialog select style sheet location");
      }
    }
  }
  
  /*
   * Represents a listener when output file location button is clicked.
   * It will bring a file selector.
   */
  private class OutputFileLocationListener implements ActionListener
  {
    private Component parent = null;
    /**
     * Constructor
     * @param parent of the button
     */
    public OutputFileLocationListener(Component parent)
    {
      this.parent = parent;
    }
    
    /**
     * When the button is executed. It will bring a file chooser for user
     * to select output file location
     */
    public void actionPerformed(ActionEvent e) 
    {
      JFileChooser outPutFileChooser = new JFileChooser();
      if(lastChosenOutputDir != null)
      {
        outPutFileChooser.setCurrentDirectory(lastChosenOutputDir);
      }
      int returnValue = outPutFileChooser.showDialog(parent, "Select");
      if(returnValue == JFileChooser.APPROVE_OPTION)
      {
        File outputFile = outPutFileChooser.getSelectedFile();
        if(outputFile != null)
        {
          outputFileLocationField.setText(outputFile.getAbsolutePath());
          lastChosenOutputDir = outputFile.getParentFile();
        }
        
      }
      else
      {
        Log.debug(30, "File choosing was canceled in ExportToAnotherMetadataLanguageDialog select output file location");
      }
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
  
}
