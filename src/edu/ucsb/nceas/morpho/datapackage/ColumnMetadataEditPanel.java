/**
 *  '$RCSfile: ColumnMetadataEditPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-12-02 22:28:16 $'
 * '$Revision: 1.14 $'
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.swing.table.*;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.framework.ColumnData;

import javax.xml.parsers.DocumentBuilder;
//import org.apache.xalan.xpath.xml.FormatterToXML;
//import org.apache.xalan.xpath.xml.TreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import com.arbortext.catalog.*;
import java.net.URL;

import edu.ucsb.nceas.morpho.framework.*;
/**
 * A panel that displays the metadata for a column in a data table
 */
public class ColumnMetadataEditPanel extends javax.swing.JPanel //implements javax.swing.event.ChangeListener
{
  /**
   * an instance of the ColumnData class for sharing information with the
   * TextImportWizard
   */ 
   ColumnData colData = null;
   
   /**
    * an instance of the TextImportWizard for use in sharing data
    */
   TextImportWizard tiw = null; 
    
  /**
   * root node of the in-memory DOM structure
   */
  private Node root;
  private Morpho morpho;

  /**
   * Document node of the in-memory DOM structure
   */
  private Document doc;

  // assorted components that need to be global to retreive data 
  JRadioButton enumButton;
  JRadioButton textButton;
  JRadioButton numButton;
  JPanel textPanel;
  JPanel numPanel;
  JPanel enumPanel;
  JTextField nameTextField;
  JTextField labelTextField;
  JTextArea definitionTextArea;
  JTextField unitTextField;
  JComboBox typeComboBox;
  JTextField missingValueTextField;
  JTextField precisionTextField;
  JTextField minimumTextField;
  JTextField maximumTextField;
  JTextField textDefinitionTextField;
  JTextField textPatternTextField;
  JTextField textSourceTextField;
  JTable table;
  JScrollPane enumScrollPane;
  
  
  SymFocus aSymFocus;
/**
 * A persistent vector which stores the data in the table
 */
  PersistentVector pv;
  
  int labelWidth = 80;
  
  public ColumnMetadataEditPanel() {
    this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); 
    this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    
    JPanel topLabelPanel = new JPanel();
    topLabelPanel.setPreferredSize(new Dimension(3000,30));
    topLabelPanel.setMaximumSize(new Dimension(3000,30));
    JLabel topLabel = new JLabel("<html><b>Column Information</b></html>");
    topLabelPanel.add(topLabel);
    this.add(topLabelPanel);
    
    JPanel namePanel = new JPanel();
    namePanel.setLayout(new BoxLayout(namePanel,BoxLayout.X_AXIS));
    JLabel nameLabel = new JLabel("Name");
    nameLabel.setForeground(java.awt.Color.red);
    nameLabel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    String tooltipString = "<html>This element specifies name of the<br>"+
                           "attribute (field) in the dataset. <br>"+
                           "This information is typically used as<br>"+
                           "a column header for the field/variable<br>"+
                           "in a spreadsheet. It is usually terse, <br>"+
                           "without spaces, and can be used to 'name'<br>"+
                           "a column in a table definition statement.</html>";
    nameLabel.setToolTipText(tooltipString);
    nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    nameLabel.setPreferredSize(new Dimension(labelWidth,20));
    nameLabel.setMaximumSize(new Dimension(labelWidth,20));
    nameTextField = new JTextField();
    nameTextField.setMaximumSize(new Dimension(3000,20));
    namePanel.add(nameLabel);
    namePanel.add(nameTextField);
    this.add(namePanel);
    
    this.add(Box.createRigidArea(new Dimension(0, 5)));
    JPanel labelPanel = new JPanel();
    labelPanel.setLayout(new BoxLayout(labelPanel,BoxLayout.X_AXIS));
    JLabel labelLabel = new JLabel("Label");
    labelLabel.setForeground(java.awt.Color.red);
    labelLabel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    String labeltooltipString = "<html>A brief label used to describe<br>"+
                                "the attribute. This is often needed<br>"+
                                "because attribute names are usually very<br>"+
                                "terse - often shortened to 8 characters or<br>"+
                                "less to deal with analysis systems. <br>"+
                                "A label is provided as a concise but<br>"+
                                "more descriptive representation of the <br>"+
                                "attribute.(</html>";
    labelLabel.setToolTipText(labeltooltipString);
    labelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    labelLabel.setPreferredSize(new Dimension(labelWidth,20));
    labelLabel.setMaximumSize(new Dimension(labelWidth,20));
    labelTextField = new JTextField();
    labelTextField.setMaximumSize(new Dimension(3000,20));
    labelPanel.add(labelLabel);
    labelPanel.add(labelTextField);
    this.add(labelPanel);

    this.add(Box.createRigidArea(new Dimension(0, 5)));
    JPanel definitionPanel = new JPanel();
    definitionPanel.setMinimumSize(new Dimension(3000,140));
    definitionPanel.setMaximumSize(new Dimension(3000,140));
    definitionPanel.setLayout(new BoxLayout(definitionPanel,BoxLayout.X_AXIS));
    JLabel definitionLabel = new JLabel("Definition");
    definitionLabel.setForeground(java.awt.Color.red);
    definitionLabel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    String definitiontooltipString = "<html>This element gives a precise<br>"+
                                            "definition of attribute in the<br>"+
                                            "dataset. It explains the contents<br>"+
                                            "of the attribute fully and<br>"+
                                            "possibly provides pointers to<br>"+
                                            "the methods used to generate<br>"+
                                            "the attribute data.</html>";
    definitionLabel.setToolTipText(definitiontooltipString);
    definitionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    definitionLabel.setPreferredSize(new Dimension(labelWidth,20));
    definitionLabel.setMaximumSize(new Dimension(labelWidth,20));
    JScrollPane jsp = new JScrollPane();
    definitionTextArea = new JTextArea();
    definitionTextArea.setLineWrap(true);
		definitionTextArea.setWrapStyleWord(true);
    jsp.getViewport().add(definitionTextArea);
    definitionPanel.add(definitionLabel);
    definitionPanel.add(jsp);
    this.add(definitionPanel);
  
    this.add(Box.createRigidArea(new Dimension(0, 5)));
    JPanel unitPanel = new JPanel();
    unitPanel.setLayout(new BoxLayout(unitPanel,BoxLayout.X_AXIS));
    JLabel unitLabel = new JLabel("Unit");
    unitLabel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    String unittooltipString = "<html>This element specifies<br>"+
                                      "unit of measurement for<br>"+
                                      "data in the field.(</html>";
    unitLabel.setToolTipText(unittooltipString);
    unitLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    unitLabel.setPreferredSize(new Dimension(labelWidth,20));
    unitLabel.setMaximumSize(new Dimension(labelWidth,20));
    unitTextField = new JTextField();
    unitTextField.setMaximumSize(new Dimension(3000,20));
    unitPanel.add(unitLabel);
    unitPanel.add(unitTextField);
    this.add(unitPanel);
  
    this.add(Box.createRigidArea(new Dimension(0, 5)));
    JPanel typePanel = new JPanel();
    typePanel.setLayout(new BoxLayout(typePanel,BoxLayout.X_AXIS));
    JLabel typeLabel = new JLabel("Type");
    typeLabel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    String typetooltipString = "<html>XML Schema primitive datatypes(</html>";
    typeLabel.setToolTipText(typetooltipString);
    typeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    typeLabel.setPreferredSize(new Dimension(labelWidth,20));
    typeLabel.setMaximumSize(new Dimension(labelWidth,20));
    typeComboBox = new JComboBox(getSchemaDatatypeList());
    typeComboBox.setMaximumSize(new Dimension(3000,30));
    typePanel.add(typeLabel);
    typePanel.add(typeComboBox);
    this.add(typePanel);

  
    this.add(Box.createRigidArea(new Dimension(0, 5)));
    this.add(buildAttributeDomainPanel());

  
    this.add(Box.createRigidArea(new Dimension(0, 5)));
    JPanel missingValuePanel = new JPanel();
    missingValuePanel.setLayout(new BoxLayout(missingValuePanel,BoxLayout.X_AXIS));
    JLabel missingValueLabel = new JLabel("MissingValue");
    missingValueLabel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    String missingValuetooltipString = "<html>This element is to specify<br>"+
                                        "missing value in the data of the<br>"+
                                        "field.(</html>";
    missingValueLabel.setToolTipText(missingValuetooltipString);
    missingValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    missingValueLabel.setPreferredSize(new Dimension(labelWidth,20));
    missingValueLabel.setMaximumSize(new Dimension(labelWidth,20));
    missingValueTextField = new JTextField();
    missingValueTextField.setMaximumSize(new Dimension(3000,20));
    missingValuePanel.add(missingValueLabel);
    missingValuePanel.add(missingValueTextField);
    this.add(missingValuePanel);

    this.add(Box.createRigidArea(new Dimension(0, 5)));
    JPanel precisionPanel = new JPanel();
    precisionPanel.setLayout(new BoxLayout(precisionPanel,BoxLayout.X_AXIS));
    JLabel precisionLabel = new JLabel("Precision");
    precisionLabel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    String precisiontooltipString = "<html>If data of that field are of<br>"+
                                    " floating point storage type this<br>"+
                                    "element specifies the number of<br>"+
                                    "significant digits after the<br>"+
                                    "floating point.</html>";
    precisionLabel.setToolTipText(precisiontooltipString);
    precisionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    precisionLabel.setPreferredSize(new Dimension(labelWidth,20));
    precisionLabel.setMaximumSize(new Dimension(labelWidth,20));
    precisionTextField = new JTextField();
    precisionTextField.setMaximumSize(new Dimension(3000,20));
    precisionPanel.add(precisionLabel);
    precisionPanel.add(precisionTextField);
    this.add(precisionPanel);
    
		SymAction lSymAction = new SymAction();
		nameTextField.addActionListener(lSymAction);
		labelTextField.addActionListener(lSymAction);
    unitTextField.addActionListener(lSymAction);
    missingValueTextField.addActionListener(lSymAction);
    precisionTextField.addActionListener(lSymAction);
    minimumTextField.addActionListener(lSymAction);
    maximumTextField.addActionListener(lSymAction);
    textDefinitionTextField.addActionListener(lSymAction);
    textPatternTextField.addActionListener(lSymAction);
    textSourceTextField.addActionListener(lSymAction);
    
    aSymFocus = new SymFocus();
		nameTextField.addFocusListener(aSymFocus);
		labelTextField.addFocusListener(aSymFocus);
    unitTextField.addFocusListener(aSymFocus);
    missingValueTextField.addFocusListener(aSymFocus);
    precisionTextField.addFocusListener(aSymFocus);
    minimumTextField.addFocusListener(aSymFocus);
    maximumTextField.addFocusListener(aSymFocus);
    textDefinitionTextField.addFocusListener(aSymFocus);
    textPatternTextField.addFocusListener(aSymFocus);
    textSourceTextField.addFocusListener(aSymFocus);
    definitionTextArea.addFocusListener(aSymFocus);
    typeComboBox.addFocusListener(aSymFocus);
  }
  
  	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
      if (colData!=null) {
			  Object object = event.getSource();
	      if (tiw!=null) {
          tiw.save_flag = true;  
        }
		  if (object == nameTextField)
				  colData.colName = getColumnName();
			  else if (object == labelTextField) {
				  colData.colTitle = getColumnLabel();
          if (tiw!=null) {
            tiw.resetColumnHeader(colData.colTitle);  
          }
        }
        else if (object == definitionTextArea)
          colData.colDefinition = getColumnDefinition();
			  else if (object == unitTextField)
          colData.colUnits = getUnit();
			  else if (object == missingValueTextField)
          colData.colMissingValue = getMissingValue();
			  else if (object == precisionTextField)
          colData.colPrecision = getPrecision();
			  else if (object == minimumTextField)
          colData.colMin = (new Double(getMinimum())).doubleValue();
			  else if (object == maximumTextField)
          colData.colMin = (new Double(getMaximum())).doubleValue();
			  else if (object == textDefinitionTextField)
          colData.colTextDefinition = getTextDefinition();
			  else if (object == textPatternTextField)
          colData.colTextPattern = getTextPattern();
			  else if (object == textSourceTextField)
          colData.colTextSource = getTextSource();
      }
		}
	}

  class SymFocus extends java.awt.event.FocusAdapter
	{
		public void focusLost(java.awt.event.FocusEvent event)
		{
      if (tiw!=null) {
          tiw.save_flag = true;  
      }
/*      if (colData!=null) {
			  Object object = event.getSource();
			  if (object == nameTextField)
				  colData.colName = getColumnName();
			  else if (object == labelTextField){
				  colData.colTitle = getColumnLabel();
        }
        else if (object == definitionTextArea)
          colData.colDefinition = getColumnDefinition();
			  else if (object == unitTextField)
          colData.colUnits = getUnit();
			  else if (object == missingValueTextField)
          colData.colUnits = getMissingValue();
			  else if (object == precisionTextField)
          colData.colUnits = getPrecision();
			  else if (object == minimumTextField)
          colData.colMin = (new Double(getMinimum())).doubleValue();
			  else if (object == maximumTextField)
          colData.colMin = (new Double(getMaximum())).doubleValue();
			  else if (object == textDefinitionTextField)
          colData.colTextDefinition = getTextDefinition();
			  else if (object == textPatternTextField)
          colData.colTextPattern = getTextPattern();
			  else if (object == textSourceTextField)
          colData.colTextSource = getTextSource();
			  else if (object == table) {
//          TableCellEditor tce = table.getCellEditor();
//          if (tce!=null) tce.stopCellEditing();
          enumTableToColData();
        }
        else if (object == typeComboBox) {
          colData.colType = getDataType();  
        }
      }
*/      
    }
  }
        
        
  public void setMorpho(Morpho morpho) {
    this.morpho = morpho;    
  }
  
	public static void main(String argv[])
	{
		class DriverFrame extends javax.swing.JFrame
		{
			public DriverFrame()
			{
				addWindowListener(new java.awt.event.WindowAdapter()
				{
					public void windowClosing(java.awt.event.WindowEvent event)
					{
						dispose();	  // free the system resources
						System.exit(0); // close the application
					}
				});
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception w) {}
				getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
				setSize(400,650);
				ColumnMetadataEditPanel cmep = new ColumnMetadataEditPanel();
				getContentPane().add(cmep);
			}
		}

		new DriverFrame().show();
	}

  class RadioListener implements java.awt.event.ActionListener
  {
    public void actionPerformed(ActionEvent e) {
      Object object = e.getSource();
      colData.enumChoice = false;
      colData.numChoice =false;
      colData.textChoice = false;
			if (object == enumButton) {
        colData.enumChoice = true; 
        textPanel.setVisible(false);
        numPanel.setVisible(false);
        enumPanel.setVisible(true);
      }
      else if(object == textButton) {
        colData.textChoice = true; 
        textPanel.setVisible(true);
        numPanel.setVisible(false);
        enumPanel.setVisible(false);
      }
      else if(object == numButton){
        colData.numChoice = true; 
        textPanel.setVisible(false);
        numPanel.setVisible(true);
        enumPanel.setVisible(false);
      }      
    }
  }


  private JPanel buildAttributeDomainPanel() {
    JPanel attrPanel = new JPanel();
    attrPanel.setLayout(new BoxLayout(attrPanel,BoxLayout.Y_AXIS));
    attrPanel.setBorder(BorderFactory.createTitledBorder("Domains"));
    attrPanel.setMaximumSize(new Dimension(3000, 175));
    attrPanel.setPreferredSize(new Dimension(3000, 175));
    attrPanel.setMinimumSize(new Dimension(3000, 174));
    JPanel buttonPanel = new JPanel();
    buttonPanel.setMaximumSize(new Dimension(3000, 40));
    buttonPanel.setPreferredSize(new Dimension(3000, 40));
    buttonPanel.setMinimumSize(new Dimension(3000, 40));
    enumButton = new JRadioButton("Enumeration", false);
    enumButton.setToolTipText("<html>This element describes any<br>"+
                              "code associated with the<br>"+
                              "attribute.</html>");
    enumButton.setActionCommand("Enumeration");
    buttonPanel.add(enumButton);
    textButton = new JRadioButton("Text", true);
    textButton.setActionCommand("Text");
    textButton.setToolTipText("<html>This element describes a free<br>"+
                              "text domain for the attribute.<br>"+
                              "By default, if pattern is missing<br>"+
                              "or empty, then any text is allowed.<br>"+
                              "If pattern is present, then it is<br>"+
                              "interpreted as a regular expression<br>"+
                              "constraining the allowable character<br>"+
                              "sequences for the attribute.</html>");
    buttonPanel.add(textButton);
    numButton = new JRadioButton("Numeric", false);
    numButton.setToolTipText("<html>This element specifies the minimum<br>"+
                             "and maximum values of a numeric attribute.<br>"+
                             "These are theoretical or expected values,<br>"+
                             "and not necessarily the actual minimum and<br>"+
                             "maximum occurring in a given data set.</html>");
    numButton.setActionCommand("Numeric");
    buttonPanel.add(numButton);
    ButtonGroup group = new ButtonGroup();
    group.add(enumButton);
    group.add(textButton);
    group.add(numButton);
    
    // Register a listener for the radio buttons.
    RadioListener myListener = new RadioListener();
    enumButton.addActionListener(myListener);
    textButton.addActionListener(myListener);
    numButton.addActionListener(myListener);
    
    attrPanel.add(buttonPanel);
    


   // create scrolling table for enumerations 
    enumPanel = new JPanel();
    enumPanel.setLayout(new BorderLayout(0,0));
    enumPanel.setMaximumSize(new Dimension(3000,100));
    enumPanel.setMinimumSize(new Dimension(3000,100));
    enumPanel.setPreferredSize(new Dimension(3000,100));
    enumScrollPane = new JScrollPane();
    enumPanel.add(BorderLayout.CENTER,enumScrollPane);
    buildEnumTable();
    attrPanel.add(enumPanel);
    
    // create numeric domain panel
    numPanel = new JPanel();
    numPanel.setLayout(new BoxLayout(numPanel,BoxLayout.Y_AXIS));
    numPanel.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));
    JPanel minimumPanel = new JPanel();
    minimumPanel.setLayout(new BoxLayout(minimumPanel,BoxLayout.X_AXIS));
    minimumPanel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    JLabel minimumLabel = new JLabel("Minimum");
    String tooltipString = "<html>Minumum Allowed Numeric Value</html>";
    minimumLabel.setToolTipText(tooltipString);
    minimumLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    minimumLabel.setPreferredSize(new Dimension(labelWidth,20));
    minimumLabel.setMaximumSize(new Dimension(labelWidth,20));
    minimumTextField = new JTextField();
    minimumTextField.setMaximumSize(new Dimension(3000,20));
    minimumPanel.add(minimumLabel);
    minimumPanel.add(minimumTextField);
    numPanel.add(minimumPanel);

    JPanel maximumPanel = new JPanel();
    maximumPanel.setLayout(new BoxLayout(maximumPanel,BoxLayout.X_AXIS));
    maximumPanel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    JLabel maximumLabel = new JLabel("Maximum");
    tooltipString = "<html>Maximum Allowed Numeric Value</html>";
    maximumLabel.setToolTipText(tooltipString);
    maximumLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    maximumLabel.setPreferredSize(new Dimension(labelWidth,20));
    maximumLabel.setMaximumSize(new Dimension(labelWidth,20));
    maximumTextField = new JTextField();
    maximumTextField.setMaximumSize(new Dimension(3000,20));
    maximumPanel.add(maximumLabel);
    maximumPanel.add(maximumTextField);
    numPanel.add(maximumPanel);
    
    numPanel.setVisible(false);
    
    attrPanel.add(numPanel);
    
    // create text domain panel
    textPanel = new JPanel();
    textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.Y_AXIS));
    textPanel.setBorder(BorderFactory.createEmptyBorder(5,0,5,0));

    JPanel textDefinitionPanel = new JPanel();
    textDefinitionPanel.setLayout(new BoxLayout(textDefinitionPanel,BoxLayout.X_AXIS));
    textDefinitionPanel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    JLabel textDefinitionLabel = new JLabel("Definition");
    tooltipString = "<html>Text Definition</html>";
    textDefinitionLabel.setToolTipText(tooltipString);
    textDefinitionLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    textDefinitionLabel.setPreferredSize(new Dimension(labelWidth,20));
    textDefinitionLabel.setMaximumSize(new Dimension(labelWidth,20));
    textDefinitionTextField = new JTextField();
    textDefinitionTextField.setMaximumSize(new Dimension(3000,20));
    textDefinitionPanel.add(textDefinitionLabel);
    textDefinitionPanel.add(textDefinitionTextField);
    textPanel.add(textDefinitionPanel);

    JPanel textPatternPanel = new JPanel();
    textPatternPanel.setLayout(new BoxLayout(textPatternPanel,BoxLayout.X_AXIS));
    textPatternPanel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    JLabel textPatternLabel = new JLabel("Pattern");
    tooltipString = "<html>The 'pattern' element specifies a regular<br>"+
                    "expression pattern that constrains the set of<br>"+
                    "allowable values for the attribute. This is<br>"+
                    "commonly used to define template patterns for<br>"+
                    "data such as phone numbers where the attribute<br>"+
                    "is text but the values are not drawn from an<br>"+
                    "enumeration. If the pattern field is empty or <br>"+
                    "missing, it defaults to '.*', which matches any<br>"+
                    "string, including the empty string.The regular<br>"+
                    "expression syntax is the same as that used in<br>"+
                    "the XML Schema Datatypes Recommendation from<br>"+
                    "the W3C.</html>";
    textPatternLabel.setToolTipText(tooltipString);
    textPatternLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    textPatternLabel.setPreferredSize(new Dimension(labelWidth,20));
    textPatternLabel.setMaximumSize(new Dimension(labelWidth,20));
    textPatternTextField = new JTextField();
    textPatternTextField.setMaximumSize(new Dimension(3000,20));
    textPatternPanel.add(textPatternLabel);
    textPatternPanel.add(textPatternTextField);
    textPanel.setVisible(false);
    textPanel.add(textPatternPanel);


    JPanel textSourcePanel = new JPanel();
    textSourcePanel.setLayout(new BoxLayout(textSourcePanel,BoxLayout.X_AXIS));
    textSourcePanel.setBorder(BorderFactory.createEmptyBorder(1,3,1,3));
    JLabel textSourceLabel = new JLabel("Source");
    String textSourcetooltipString = "<html>The name of the source from<br>"+
                                     "which this text domain and its<br>"+
                                     "associated definition are drawn.<br>"+
                                     "This is commonly used for identifying<br>"+
                                     "standard coding systems, like the<br>"+
                                     "FIPS standard for postal abbreviations<br>"+
                                     "for states in the US. In other cases,<br>"+
                                     "the coding may be the researcher's<br>"+
                                     "customized way of recording and<br>"+
                                     "classifying their data, and no<br>"+
                                     "external 'source' would exist.</html>";
    textSourceLabel.setToolTipText(textSourcetooltipString);
    textSourceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    textSourceLabel.setPreferredSize(new Dimension(labelWidth,20));
    textSourceLabel.setMaximumSize(new Dimension(labelWidth,20));
    textSourceTextField = new JTextField();
    textSourceTextField.setMaximumSize(new Dimension(3000,20));
    textSourcePanel.add(textSourceLabel);
    textSourcePanel.add(textSourceTextField);
    textPanel.add(textSourcePanel);
    
    attrPanel.add(textPanel);
    
    
    return attrPanel;
  }
  
  private void buildEnumTable() {
    table = new JTable();
    table.addFocusListener(aSymFocus);

    if (pv == null) {
      pv = new PersistentVector();
    }
    for (int j=0;j<100;j++) {
      String[] vals = {"", "", ""};
      pv.addElement(vals);
    }
    Vector colLabels = new Vector();
    colLabels.addElement("Code");
    colLabels.addElement("Definition");
    colLabels.addElement("Source");
    PersistentTableModel ptm = new PersistentTableModel(pv, colLabels);
    table.setModel(ptm);
    TableColumn column = null;
    for (int k = 0; k < 3; k++) {
      column = table.getColumnModel().getColumn(k);
      if (k == 0) {
        column.setPreferredWidth(50); 
      } else {
        column.setPreferredWidth(150);
      }
    }
    enumScrollPane.getViewport().removeAll();
    enumScrollPane.getViewport().add(table);
  }
  
  public String output() {
    return createAttributeStringBuffer().toString();  
  }
  
  /*
   * combine information in components to create an attribute tree
   * in xml format in StringBuffer
   */
  StringBuffer createAttributeStringBuffer() {
    StringBuffer attribute = new StringBuffer();
    String temp = "<attribute>\n";
    attribute.append(temp);
    
    temp = "<attributeName>"+normalize(nameTextField.getText())+"</attributeName>\n";
    attribute.append(temp);
    
    temp = "<attributeLabel>"+normalize(labelTextField.getText())+"</attributeLabel>\n";
    attribute.append(temp);

    temp = "<attributeDefinition>"+normalize(definitionTextArea.getText())
                                                     +"</attributeDefinition>\n";
    attribute.append(temp);

    temp = "<unit>"+unitTextField.getText()+"</unit>\n";
    attribute.append(temp);
   
    temp = "<dataType>"+normalize(typeComboBox.getSelectedItem().toString())
                                                +"</dataType>\n";
    attribute.append(temp);
    
    attribute.append("<attributeDomain>\n");
    if (enumButton.isSelected()) {
      attribute.append("<enumeratedDomain>\n");
      for (int j=0;j<pv.size();j++) {
        String[] rec = (String[])pv.elementAt(j);
        if (rec[0].length()>0) {
          temp = "<code>"+normalize(rec[0])+"</code>\n";
          attribute.append(temp);
          temp = "<definition>"+normalize(rec[1])+"</definition>\n";
          attribute.append(temp);
          temp = "<source>"+normalize(rec[2])+"</source>\n";
          attribute.append(temp);
        }
      }
    attribute.append("</enumeratedDomain>\n");
    }
    else if (textButton.isSelected()) {
      attribute.append("<textDomain>\n");  
        temp = "<definition>"+normalize(textDefinitionTextField.getText())+"</definition>\n";
        attribute.append(temp);
        temp = "<pattern>"+normalize(textPatternTextField.getText())+"</pattern>\n";
        attribute.append(temp);
        temp = "<source>"+normalize(textSourceTextField.getText())+"</source>\n";
        attribute.append(temp);
      attribute.append("</textDomain>\n");  
    }
    else if (numButton.isSelected()) {
      attribute.append("<numericDomain>\n");  
        temp = "<minimum>"+normalize(minimumTextField.getText())+"</minimum>\n";
        attribute.append(temp);
        temp = "<maximum>"+normalize(maximumTextField.getText())+"</maximum>\n";
        attribute.append(temp);
        attribute.append(temp);
      attribute.append("</numericDomain>\n");  
    }
    
    
    attribute.append("</attributeDomain>\n");

    attribute.append("</attribute>");
    
    return attribute;
  }

 // Vector of XMLSchema primative types
 private Vector getSchemaDatatypeList() {
    Vector res = new Vector();
    res.addElement("string");
    res.addElement("integer");
    res.addElement("float");
    res.addElement("decimal");
    res.addElement("double");
    res.addElement("duration");
    res.addElement("dateTime");
    res.addElement("time");
    res.addElement("date");
    res.addElement("gYearMonth");
    res.addElement("gYear");
    res.addElement("gMonthDay");
    res.addElement("gDay");
    res.addElement("gMonth");
    res.addElement("boolean");
    res.addElement("base64Binary");
    res.addElement("hexBinary");
    res.addElement("anyURI");
    res.addElement("QName");
    res.addElement("NOTATION");   
    return res;
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

  void insertNewAttributeAt(int index, Document doc) 
  {
    this.doc = doc;
    root = doc.getDocumentElement();
    
    // create the root of the new attribute subtree
    Node newAttrRoot = doc.createElement("attribute");
    
    Node temp = doc.createElement("attributeName");
    Node newText = doc.createTextNode(normalize(nameTextField.getText()));
    temp.appendChild(newText);
    newAttrRoot.appendChild(temp);

    temp = doc.createElement("attributeLabel");
    newText = doc.createTextNode(normalize(labelTextField.getText()));
    temp.appendChild(newText);
    newAttrRoot.appendChild(temp);
    
    temp = doc.createElement("attributeDefinition");
    newText = doc.createTextNode(normalize(definitionTextArea.getText()));
    temp.appendChild(newText);
    newAttrRoot.appendChild(temp);
   
    temp = doc.createElement("unit");
    newText = doc.createTextNode(normalize(unitTextField.getText()));
    temp.appendChild(newText);
    newAttrRoot.appendChild(temp);

    temp = doc.createElement("dataType");
    newText = doc.createTextNode(normalize(typeComboBox.getSelectedItem().toString()));
    temp.appendChild(newText);
    newAttrRoot.appendChild(temp);
    
    Node domainNode = doc.createElement("attributeDomain");
    newAttrRoot.appendChild(domainNode);
    if (enumButton.isSelected()) {
      if (pv.size()==0) {
        Node enumNode = doc.createElement("enumeratedDomain");
        domainNode.appendChild(enumNode);
          
        temp = doc.createElement("code");
        newText = doc.createTextNode("");
        temp.appendChild(newText);
        enumNode.appendChild(temp);      

        temp = doc.createElement("definition");
        newText = doc.createTextNode("");
        temp.appendChild(newText);
        enumNode.appendChild(temp);      
        
        temp = doc.createElement("source");
        newText = doc.createTextNode("");
        temp.appendChild(newText);
        enumNode.appendChild(temp);      
      }
      for (int j=0;j<pv.size();j++) {
        String[] rec = (String[])pv.elementAt(j);
        if (rec[0].length()>0) {
          Node enumNode = doc.createElement("enumeratedDomain");
          domainNode.appendChild(enumNode);
          
          temp = doc.createElement("code");
          newText = doc.createTextNode(normalize(rec[0]));
          temp.appendChild(newText);
          enumNode.appendChild(temp);      

          temp = doc.createElement("definition");
          newText = doc.createTextNode(normalize(rec[1]));
          temp.appendChild(newText);
          enumNode.appendChild(temp);      
        
          temp = doc.createElement("source");
          newText = doc.createTextNode(normalize(rec[2]));
          temp.appendChild(newText);
          enumNode.appendChild(temp);      
        }
      }
    }
    else if (textButton.isSelected()) {
      Node textNode = doc.createElement("textDomain");
      domainNode.appendChild(textNode);

      temp = doc.createElement("definition");
      newText = doc.createTextNode(normalize(textDefinitionTextField.getText()));
      temp.appendChild(newText);
      textNode.appendChild(temp);      

      temp = doc.createElement("pattern");
      newText = doc.createTextNode(normalize(textPatternTextField.getText()));
      temp.appendChild(newText);
      textNode.appendChild(temp);      

      temp = doc.createElement("source");
      newText = doc.createTextNode(normalize(textSourceTextField.getText()));
      temp.appendChild(newText);
      textNode.appendChild(temp);      
    }
    else if (numButton.isSelected()) {
      Node numericNode = doc.createElement("numericDomain");
      domainNode.appendChild(numericNode);
      
      temp = doc.createElement("minimum");
      newText = doc.createTextNode(normalize(minimumTextField.getText()));
      temp.appendChild(newText);
      numericNode.appendChild(temp);      

      temp = doc.createElement("maximum");
      newText = doc.createTextNode(normalize(maximumTextField.getText()));
      temp.appendChild(newText);
      numericNode.appendChild(temp);      
      
    }

    temp = doc.createElement("missingValueCode");
    newText = doc.createTextNode(normalize(missingValueTextField.getText()));
    temp.appendChild(newText);
    newAttrRoot.appendChild(temp);
    
    temp = doc.createElement("precision");
    newText = doc.createTextNode(normalize(precisionTextField.getText()));
    temp.appendChild(newText);
    newAttrRoot.appendChild(temp);
    
    // --------------------------------
    // now find the 'index'th attribute and insert the new branch there
    NodeList nl = doc.getElementsByTagName("attribute");
    // nl should now contain all the 'Attribute' nodes
    int cnt = index;
    if (cnt > nl.getLength()) cnt=nl.getLength();
    Node nextNode = nl.item(cnt);
    root.insertBefore(newAttrRoot, nextNode);
  }
  
  public void setTextImportWizard(TextImportWizard tiw) {
    this.tiw = tiw;
  }
 
 // get Column Info values
  public String getColumnName() {
    return nameTextField.getText();
  } 
  public String getColumnLabel() {
    return labelTextField.getText();
  }
  public String getColumnDefinition() {
    return definitionTextArea.getText();
  }
  public String getUnit() {
    return unitTextField.getText();
  }   
  public String getDataType() {
    return typeComboBox.getSelectedItem().toString();
  }   
  public String getMissingValue() {
    return missingValueTextField.getText();
  }   
  public String getPrecision() {
    return precisionTextField.getText();
  }   
  public String getMinimum() {
    return minimumTextField.getText();
  }   
  public String getMaximum() {
    return maximumTextField.getText();
  }   
  public String getTextDefinition() {
    return textDefinitionTextField.getText();
  }   
  public String getTextPattern() {
    return textPatternTextField.getText();
  }   
  public String getTextSource() {
    return textSourceTextField.getText();
  }   
  public boolean getEnumButton() {
    return enumButton.isSelected();
  }
  public boolean gettextButton() {
    return textButton.isSelected();
  }
  public boolean getNumButton() {
    return numButton.isSelected();
  }
  
   // set Column Info values
  public void setColumnName(String val) {
    nameTextField.setText(val);
  } 
  public void setColumnLabel(String val) {
    labelTextField.setText(val);
  }
  public void setColumnDefinition(String val) {
    definitionTextArea.setText(val);
  }
  public void setUnit(String val) {
    unitTextField.setText(val);
  }   
  public void setDataType(String val) {
    typeComboBox.setSelectedItem(val);
  }   
  public void setMissingValue(String val) {
    missingValueTextField.setText(val);
  }   
  public void setPrecision(String val) {
    precisionTextField.setText(val);
  }   
  public void setMinimum(String val) {
    minimumTextField.setText(val);
  }   
  public void setMaximum(String val) {
    maximumTextField.setText(val);
  }   
  public void setTextDefinition(String val) {
    textDefinitionTextField.setText(val);
  }   
  public void setTextPattern(String val) {
    textPatternTextField.setText(val);
  }   
  public void setTextSource(String val) {
    textSourceTextField.setText(val);
  }   
  public void setEnumButton(boolean b) {
    enumButton.setSelected(b);
  }
  public void settextButton(boolean b) {
    textButton.setSelected(b);
  }
  public void setNumButton(boolean b) {
    numButton.setSelected(b);
  }

  public void setColumnData(ColumnData cd) {
    colData = cd;
    colDataToFields();
  }
  public ColumnData getColumnData() {
    FieldsToColData();
    return colData;
  }
  
  
  // fill in fields based on colData object
  public void colDataToFields() {
    setColumnName(colData.colName);
    setColumnLabel(colData.colTitle);
    setColumnDefinition(colData.colDefinition);
    setUnit(colData.colUnits);
    setDataType(colData.colType);
    setMissingValue(colData.colMissingValue);
    setPrecision(colData.colPrecision);
    setMinimum((new Double(colData.colMin)).toString());
    setMaximum((new Double(colData.colMax)).toString());
    setTextDefinition(colData.colTextDefinition);
    setTextPattern(colData.colTextPattern);
    if (colData.numChoice) {
      pv = null;
      buildEnumTable();
      numButton.setSelected(true);
      textPanel.setVisible(false);
      numPanel.setVisible(true);
      enumPanel.setVisible(false);
    }
    else if (colData.textChoice) {
      pv = null;
      buildEnumTable();
      textButton.setSelected(true);
      textPanel.setVisible(true);
      numPanel.setVisible(false);
      enumPanel.setVisible(false);
    }
    else if (colData.enumChoice) {
      colDataToEnumTable();
      enumButton.setSelected(true);
      textPanel.setVisible(false);
      numPanel.setVisible(false);
      enumPanel.setVisible(true);
    }
  }
  
  private void colDataToEnumTable() {
   if (colData.enumCodeVector!=null) {
    Vector vec0 = colData.enumCodeVector;
    Vector vec1 = colData.enumDefinitionVector;
    Vector vec2 = colData.enumSourceVector;
    table = new JTable();
    table.addFocusListener(aSymFocus);
    pv = new PersistentVector();
    for (int j=0;j<100;j++) {
      if (j<vec0.size()) {
        String[] vals = {(String)vec0.elementAt(j), (String)vec1.elementAt(j), (String)vec2.elementAt(j)};
        pv.addElement(vals);
      }
      else{
        String[] vals1 = {"", "", ""};
        pv.addElement(vals1);
      }
    }
    Vector colLabels = new Vector();
    colLabels.addElement("Code");
    colLabels.addElement("Definition");
    colLabels.addElement("Source");
    PersistentTableModel ptm = new PersistentTableModel(pv, colLabels);
    table.setModel(ptm);
    TableColumn column = null;
    for (int k = 0; k < 3; k++) {
      column = table.getColumnModel().getColumn(k);
      if (k == 0) {
        column.setPreferredWidth(50); 
      } else {
        column.setPreferredWidth(150);
      }
    } 
   }
    enumScrollPane.getViewport().removeAll();
    enumScrollPane.getViewport().add(table);     
  }
 
 public void enumTableToColData() {
   colData.enumCodeVector = new Vector();    
   colData.enumDefinitionVector = new Vector();    
   colData.enumSourceVector = new Vector(); 
   int cnt = 0;
   for (int j=0;j<pv.size();j++) {
      String[] rec = (String[])pv.elementAt(j);
      if (rec[0].length()>0) {
        colData.enumCodeVector.addElement(rec[0]);
        colData.enumDefinitionVector.addElement(rec[1]);
        colData.enumSourceVector.addElement(rec[2]);
        cnt++;
      }
    }   
 }
 
   // fill in colData object based on fields
   public void FieldsToColData() {
    colData.colName = getColumnName();
    colData.colTitle = getColumnLabel();
    colData.colDefinition = getColumnDefinition();
    colData.colUnits = getUnit();
    colData.colType = getDataType();
    colData.colMissingValue = getMissingValue();
    colData.colPrecision = getPrecision();
    colData.colMin = (new Double(getMinimum())).doubleValue();
    colData.colMax = (new Double(getMaximum())).doubleValue();
    colData.colTextDefinition = getTextDefinition();
    colData.colTextPattern = getTextPattern();
    if (numButton.isSelected()) {
      colData.numChoice = true;
      colData.textChoice = false;
      colData.enumChoice = false;
    }
    if (textButton.isSelected()) {
      colData.numChoice = false;
      colData.textChoice = true;
      colData.enumChoice = false;
    }
     if (enumButton.isSelected()) {
      colData.numChoice = false;
      colData.textChoice = false;
      colData.enumChoice = true;
    }
   }
  
  //-----------------------------------------
  String fileName = "outtest";
  
    /**
   * Print writer (output)
   */
  private PrintWriter out;

    /**
   * Save the DOM doc as a file
   */
  public void save()
  {
    saveDOM(root);
  }

  /**
   * This method wraps the 'print' method to send DOM back to the
   * XML document (file) that was used to create the DOM. i.e.
   * this method saves changes to disk
   * 
   * @param nd node (usually the document root)
   */
  public void saveDOM(Node nd)
  { 
    File outfile = new File(fileName);
    try
    {
      out = new PrintWriter(new FileWriter(fileName));
    }
    catch(Exception e)
    {
    }
    out.println("<?xml version=\"1.0\"?>");
    print(nd);
    out.close(); 
  }

  /**
   * This method can 'print' any DOM subtree. Specifically it is
   * set (by means of 'out') to write the in-memory DOM  
   * Action thus saves a new version of the XML doc
   * 
   * @param node node usually set to the 'doc' node for complete XML file
   * re-write
   */
  public void print(Node node)
  {

    // is there anything to do?
    if (node == null)
    {
      return;
    }

    int type = node.getNodeType();
    switch (type)
    {
      // print document
    case Node.DOCUMENT_NODE:
    {

      out.println("<?xml version=\"1.0\"?>");
      print(((Document) node).getDocumentElement());
      out.flush();
      break;
    }

      // print element with attributes
    case Node.ELEMENT_NODE:
    {
      out.print('<');
      out.print(node.getNodeName());
      Attr attrs[] = sortAttributes(node.getAttributes());
      for (int i = 0; i < attrs.length; i++)
      {
        Attr attr = attrs[i];
        out.print(' ');
        out.print(attr.getNodeName());
        out.print("=\"");
        out.print(normalize(attr.getNodeValue()));
        out.print('"');
      }
      out.print('>');
      NodeList children = node.getChildNodes();
      if (children != null)
      {
        int len = children.getLength();
        for (int i = 0; i < len; i++)
        {
          print(children.item(i));
        }
      }
      break;
    }

      // handle entity reference nodes
    case Node.ENTITY_REFERENCE_NODE:
    {
      out.print('&');
      out.print(node.getNodeName());
      out.print(';');

      break;
    }

      // print cdata sections
    case Node.CDATA_SECTION_NODE:
    {
      out.print("<![CDATA[");
      out.print(node.getNodeValue());
      out.print("]]>");

      break;
    }

      // print text
    case Node.TEXT_NODE:
    {
      out.print(normalize(node.getNodeValue()));
      break;
    }

      // print processing instruction
    case Node.PROCESSING_INSTRUCTION_NODE:
    {
      out.print("<?");
      out.print(node.getNodeName());
      String data = node.getNodeValue();
      if (data != null && data.length() > 0)
      {
        out.print(' ');
        out.print(data);
      }
      out.print("?>");
      break;
    }
    }

    if (type == Node.ELEMENT_NODE)
    {
      out.print("</");
      out.print(node.getNodeName());
      out.println('>');
    }

    out.flush();

  } // print(Node)

  /** Returns a sorted list of attributes. */
  protected Attr[] sortAttributes(NamedNodeMap attrs)
  {

    int len = (attrs != null) ? attrs.getLength() : 0;
    Attr array[] = new Attr[len];
    for (int i = 0; i < len; i++)
    {
      array[i] = (Attr) attrs.item(i);
    }
    for (int i = 0; i < len - 1; i++)
    {
      String name = array[i].getNodeName();
      int index = i;
      for (int j = i + 1; j < len; j++)
      {
        String curName = array[j].getNodeName();
        if (curName.compareTo(name) < 0)
        {
          name = curName;
          index = j;
        }
      }
      if (index != i)
      {
        Attr temp = array[i];
        array[i] = array[index];
        array[index] = temp;
      }
    }

    return (array);

  } // sortAttributes(NamedNodeMap):Attr[]

  


}
