/**
 *  '$RCSfile: ColumnMetadataEditPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-08-26 22:30:15 $'
 * '$Revision: 1.3 $'
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

import edu.ucsb.nceas.morpho.framework.*;
/**
 * A panel that displays the metadata for a column in a data table
 */
public class ColumnMetadataEditPanel extends javax.swing.JPanel //implements javax.swing.event.ChangeListener
{

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
    JLabel topLabel = new JLabel("Column Information");
    topLabelPanel.add(topLabel);
    this.add(topLabelPanel);
    
    JPanel namePanel = new JPanel();
    namePanel.setLayout(new BoxLayout(namePanel,BoxLayout.X_AXIS));
    JLabel nameLabel = new JLabel("Name");
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
			if (object == enumButton) {
        textPanel.setVisible(false);
        numPanel.setVisible(false);
        enumPanel.setVisible(true);
      }
      else if(object == textButton) {
        textPanel.setVisible(true);
        numPanel.setVisible(false);
        enumPanel.setVisible(false);
      }
      else if(object == numButton){
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
    enumButton = new JRadioButton("Enumeration", true);
    enumButton.setToolTipText("<html>This element describes any<br>"+
                              "code associated with the<br>"+
                              "attribute.</html>");
    enumButton.setActionCommand("Enumeration");
    buttonPanel.add(enumButton);
    textButton = new JRadioButton("Text", false);
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
    JScrollPane enumScrollPane = new JScrollPane();
    enumPanel.add(BorderLayout.CENTER,enumScrollPane);
    table = new JTable();
    pv = new PersistentVector();
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
    enumScrollPane.getViewport().add(table);
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
        temp = "<minimum>"+normalize(minimumTextField.getText())+"</minimum>\n";
        attribute.append(temp);
        temp = "<maximum>"+normalize(maximumTextField.getText())+"</maximum>\n";
        attribute.append(temp);
      attribute.append("</textDomain>\n");  
    }
    else if (numButton.isSelected()) {
      attribute.append("<numericDomain>\n");  
        temp = "<definition>"+normalize(textDefinitionTextField.getText())+"</definition>\n";
        attribute.append(temp);
        temp = "<pattern>"+normalize(textPatternTextField.getText())+"</pattern>\n";
        attribute.append(temp);
        temp = "<source>"+normalize(textSourceTextField.getText())+"</source>\n";
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
    res.addElement("float");
    res.addElement("decimal");
    res.addElement("double");
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

}
