/**
 *  '$RCSfile: TextImportWizard.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-12-04 18:04:40 $'
 * '$Revision: 1.46 $'
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

package edu.ucsb.nceas.morpho.framework;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Date;
import java.util.Enumeration;
import java.text.DateFormat;

import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.datapackage.wizard.PackageWizard;
import edu.ucsb.nceas.morpho.datapackage.ColumnMetadataEditPanel;

/**
 * 'Text Import Wizard' is modeled after
 * the text import wizard in Excel. Its  purpose is to automatically 
 * create table entity and attribute metadata 
 * directly from a text based data file. It 'guesses' text-based tables column
 * data types (and delimiters) and checks for input validity
 
 * parses lines array based on assumed delimiters to determine data in each column
 * of the table. Table data is stored in a Vector of vectors. Outer vector is a
 * list of row vectors. Each row vector has a list of column data in String
 * format.
 * 
 */
public class TextImportWizard extends javax.swing.JFrame
{
  
 /**
  * The editor panel for entering and displaying column metadata
  * This component was originally used with the data display table
  * (when a new column is created)and is reused here
  */
 ColumnMetadataEditPanel cmePanel = null;
 
  /**
  * a global reference to the table used to display the data that is
  * being referenced by this text import process
  */
  JTable table;
  
  /**
   * flag indicating that user has returned to first screen using "back" button
   */
  boolean hasReturnedFromScreen2;

  
  /**
   * flag indicating that multiple, sequential delimiters should be ignored
   * primarily useful with space delimiters
   */
  boolean ignoreConsequtiveDelimiters = false;
  
  /**
   * used as a flag to indicate whether 'blank' fields should be reported 
   * as errors; if false, they are not reported
   */
  boolean blankCheckFlag = false;
  
	/**
	 * actual number of lines in fdata file
	 */
	int nlines_actual;  

	/**
	 * number of parsed lines in file
	 */
	int nlines; 
	
	/**
	 * max number of lines to be parsed in file
	 */
	int nlines_max = 5000;  

	/**
	 * array of line strings
	 */
	String[] lines; 
  String filename;
  
  /**
   * selected column variable
   */
  int selectedCol = -1;
  
  /**
   * flag used to avoid parsing everytime a checkbox is changed
   */
  boolean parseOn = true;
  
  /**
   * step number in wizard
   */
  int stepNumber = 1;
  
  /**
   * StringBuffer used to store results from various parts of the wizard
   */
  StringBuffer resultsBuffer;
  // indicates whether input is text
  boolean textFlag;
  
  // starting line
  int startingLine = 1;
  
  /**
   * flag indicating that labels for each column are contained in the
   * starting line of parsed data
   */
  boolean labelsInStartingLine = true;
  
	/**
	 * vector containing column Title strings
	 */
	// contains column titles
	Vector colTitles;

	/**
	 * vector containing ColumnData objects
	 */
	Vector colDataInfo;

	 	
	/**
	 * vector of vectors with table data
	 */
	Vector vec;
  
  /**
   * entity wizard
   */
  PackageWizard entityWizard = null;
  
  /**
   * attribute wizard
   */
  PackageWizard attributeWizard = null;

  /**
   * physical wizard
   */
  PackageWizard physicalWizard = null;
  
  public boolean save_flag = false;
  
  private TextImportListener listener = null;
  
  boolean finishFlag = false;
  
  String delimiter = "";
  
	public TextImportWizard(String dataFileName, TextImportListener listener)
	{
    this.listener = listener;
    cmePanel = new ColumnMetadataEditPanel();
    cmePanel.setTextImportWizard(this);
    cmePanel.setPreferredSize(new Dimension(300, 4000));

		//{{INIT_CONTROLS
		setTitle("Text Import Wizard");
		setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(695,486);
		setVisible(false);
		saveFileDialog.setMode(FileDialog.SAVE);
		saveFileDialog.setTitle("Save");
		openFileDialog.setMode(FileDialog.LOAD);
		openFileDialog.setTitle("Open");
		//$$ openFileDialog.move(0,336);
		MainDisplayPanel.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER, MainDisplayPanel);
		ControlsPlusDataPanel.setLayout(new GridLayout(2,1,0,4));
		MainDisplayPanel.add(BorderLayout.CENTER,ControlsPlusDataPanel);
		ControlsPanel.setLayout(new CardLayout(0,0));
		ControlsPlusDataPanel.add(ControlsPanel);

//-----------------------------------------------------    
		Step1ControlsPanel.setAlignmentY(0.0F);
		Step1ControlsPanel.setAlignmentX(0.0F);
		Step1ControlsPanel.setLayout(new GridLayout(7,1,0,10));
		ControlsPanel.add("card1", Step1ControlsPanel);
		Step1_TopTitlePanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		Step1ControlsPanel.add(Step1_TopTitlePanel);
		Step1_TopTitleLabel.setText("This set of screens will create metadata based on the content of the specified data file");
		Step1_TopTitlePanel.add(Step1_TopTitleLabel);
		Step1_TopTitleLabel.setForeground(java.awt.Color.black);
		Step1_TableNamePanel.setAlignmentY(0.473684F);
		Step1_TableNamePanel.setAlignmentX(0.0F);
		Step1_TableNamePanel.setLayout(new BoxLayout(Step1_TableNamePanel,BoxLayout.X_AXIS));
		Step1ControlsPanel.add(Step1_TableNamePanel);
		Step1_NameLabel.setText(" Table Name: ");
		Step1_TableNamePanel.add(Step1_NameLabel);
		Step1_NameLabel.setForeground(java.awt.Color.black);
		Step1_NameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		Step1_TableNamePanel.add(TableNameTextField);
		Step1_TableDescriptionPanel.setAlignmentY(0.473684F);
		Step1_TableDescriptionPanel.setAlignmentX(0.0F);
		Step1_TableDescriptionPanel.setLayout(new BoxLayout(Step1_TableDescriptionPanel,BoxLayout.X_AXIS));
		Step1ControlsPanel.add(Step1_TableDescriptionPanel);
		Step1_TableDescriptionLabel.setText(" Description: ");
		Step1_TableDescriptionPanel.add(Step1_TableDescriptionLabel);
		Step1_TableDescriptionLabel.setForeground(java.awt.Color.black);
		Step1_TableDescriptionLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		Step1_TableDescriptionPanel.add(TableDescriptionTextField);
		Step1_DelimiterChoicePanel.setAlignmentX(0.0F);
		Step1_DelimiterChoicePanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		Step1ControlsPanel.add(Step1_DelimiterChoicePanel);
		Step1_DelimiterLabel.setText("Choose the method used to separate fields on each line of your data");
		Step1_DelimiterLabel.setAlignmentY(0.0F);
		Step1_DelimiterChoicePanel.add(Step1_DelimiterLabel);
		Step1_DelimiterLabel.setForeground(new java.awt.Color(102,102,153));
		Step1_DelimiterLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		Step1_DelimeterRadioPanel.setAlignmentX(0.0F);
		Step1_DelimeterRadioPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		Step1ControlsPanel.add(Step1_DelimeterRadioPanel);
		DelimitedRadioButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
		DelimitedRadioButton.setText("Delimited  -  Characters such as tabs or commas separate each data field");
		DelimitedRadioButton.setActionCommand("Delimited  -  Characters such as tabs or commas separate each data field");
		DelimitedRadioButton.setAlignmentY(0.0F);
		DelimitedRadioButton.setSelected(true);
		Step1_DelimeterRadioPanel.add(DelimitedRadioButton);
		DelimitedRadioButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		Step1_FixedFieldRadioPanel.setAlignmentX(0.0F);
		Step1_FixedFieldRadioPanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		Step1ControlsPanel.add(Step1_FixedFieldRadioPanel);
		FixedFieldRadioButton.setText("Fixed Width  -  Fields are aligned in columns with specified number of characters");
		FixedFieldRadioButton.setActionCommand("Fixed Width  -  Fields are aligned in columns with specified number of characters");
		FixedFieldRadioButton.setAlignmentY(0.0F);
		FixedFieldRadioButton.setEnabled(false);
		Step1_FixedFieldRadioPanel.add(FixedFieldRadioButton);
		FixedFieldRadioButton.setFont(new Font("Dialog", Font.PLAIN, 12));
		StartingLinePanel.setAlignmentX(0.0F);
		StartingLinePanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		Step1ControlsPanel.add(StartingLinePanel);
		StartingLineLabel.setText("Start import at row: ");
		StartingLinePanel.add(StartingLineLabel);
		StartingLineLabel.setForeground(java.awt.Color.black);
		StartingLineLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		StartingLineTextField.setText("1");
		StartingLineTextField.setColumns(4);
		StartingLinePanel.add(StartingLineTextField);
		ColumnLabelsLabel.setText("     ");
		StartingLinePanel.add(ColumnLabelsLabel);
		ColumnLabelsCheckBox.setText("Column Labels are in starting row");
		ColumnLabelsCheckBox.setActionCommand("Column Labels are in starting row");
		ColumnLabelsCheckBox.setSelected(true);
		StartingLinePanel.add(ColumnLabelsCheckBox);
		ColumnLabelsCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));

//-------------------------------------------------------    
		Step2ControlsPanel.setLayout(new GridLayout(6,1,0,0));
		ControlsPanel.add("card2", Step2ControlsPanel);
		Step2ControlsPanel.setVisible(false);
		Step2_TopLabelPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		Step2ControlsPanel.add(Step2_TopLabelPanel);
		Step2_TopLabel.setText("If the columns indicated in the table are incorrect, try changing the assumed delimiter(s)");
		Step2_TopLabelPanel.add(Step2_TopLabel);
		Step2_TopLabel.setForeground(java.awt.Color.black);
		Step2_DelimiterChoicePanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		Step2ControlsPanel.add(Step2_DelimiterChoicePanel);
		Step2_DelimterChoiceLabel.setText("  Delimiters: ");
		Step2_DelimiterChoicePanel.add(Step2_DelimterChoiceLabel);
		Step2_DelimterChoiceLabel.setForeground(java.awt.Color.black);
		Step2_DelimterChoiceLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
		TabCheckBox.setText("tab");
		TabCheckBox.setActionCommand("tab");
		TabCheckBox.setSelected(true);
		Step2_DelimiterChoicePanel.add(TabCheckBox);
		TabCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
		CommaCheckBox.setText("comma");
		CommaCheckBox.setActionCommand("comma");
		Step2_DelimiterChoicePanel.add(CommaCheckBox);
		CommaCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
		SpaceCheckBox.setText("space");
		SpaceCheckBox.setActionCommand("space");
		Step2_DelimiterChoicePanel.add(SpaceCheckBox);
		SpaceCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
		SemicolonCheckBox.setText("semicolon");
		SemicolonCheckBox.setActionCommand("semicolon");
		Step2_DelimiterChoicePanel.add(SemicolonCheckBox);
		SemicolonCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
		OtherCheckBox.setText("other");
		OtherCheckBox.setActionCommand("other");
		Step2_DelimiterChoicePanel.add(OtherCheckBox);
		OtherCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
		OtherTextField.setColumns(2);
		Step2_DelimiterChoicePanel.add(OtherTextField);
		JPanel10.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		Step2ControlsPanel.add(JPanel10);
		JPanel11.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		Step2ControlsPanel.add(JPanel11);
		Step2_ConsequtivePanel.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		Step2ControlsPanel.add(Step2_ConsequtivePanel);
		ConsecutiveCheckBox.setText("Treat consecutive delimiters as one");
		ConsecutiveCheckBox.setActionCommand("Treat consecutive delimiters as one");
		Step2_ConsequtivePanel.add(ConsecutiveCheckBox);
		ConsecutiveCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));
    
    //---------------------------------
		Step3ControlsPanel.setLayout(new BorderLayout(0,0));
		ControlsPanel.add("card3", Step3ControlsPanel);
		Step3ControlsPanel.setVisible(false);
		Step3_HelpPanel.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
		Step3ControlsPanel.add(BorderLayout.WEST, Step3_HelpPanel);
		Step3_HelpPanel.setBackground(java.awt.Color.white);
		Step3_HelpLabel.setText("<html><br>Select column of<p>interest by \'clicking\'"+
             "<p>on any cell in <p>column.<p>A red label indicates</p><p>a requiired item.</p>");
		Step3_HelpPanel.add(Step3_HelpLabel);
		Step3_HelpLabel.setForeground(java.awt.Color.black);
    ColDataSummaryPanel.setLayout(new GridLayout(2,1));
    ColDataSummaryPanel.add(TopColSummaryPanel);
    ColDataSummaryPanel.add(BottomColSummaryPanel);    
		TopColSummaryPanel.setLayout(new BoxLayout(TopColSummaryPanel,BoxLayout.Y_AXIS));
    ColDataSummaryPanel.setBorder(BorderFactory.createEmptyBorder(10,5,5,5));
    TopColSummaryPanel.add(ColDataSummaryLabel);
    BottomColSummaryPanel.setLayout(new BorderLayout(0,0));
    BottomColSummaryPanel.add(BorderLayout.NORTH, new JLabel("Unique Items List"));
    BottomColSummaryPanel.add(BorderLayout.CENTER, UniqueItemsScrollPane);
    BottomColSummaryPanel.add(BorderLayout.EAST, insertEnumPanel);
    insertEnumPanel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
    insertEnumPanel.setLayout(new BoxLayout(insertEnumPanel,BoxLayout.Y_AXIS));
    insertEnumLabel.setText("<html>Click to add <br>unique items <br>to enumeration</html>");
    insertEnumPanel.add(insertEnumLabel);
    insertEnumPanel.add(insertEnumButton);
    UniqueItemsScrollPane.getViewport().add(UniqueItemsList);
    insertEnumPanel.setVisible(false);
		Step3ControlsPanel.add(BorderLayout.CENTER, ColDataSummaryPanel);
    
    //------------------------------------------------
		DataPanel.setLayout(new BorderLayout(0,0));
		ControlsPlusDataPanel.add(DataPanel);
		DataPanel.add(BorderLayout.CENTER, DataScrollPanel);
    
		ColumnDataPanel.setLayout(new BorderLayout(0,0));
		MainDisplayPanel.add(BorderLayout.EAST,ColumnDataPanel);
		ColumnDataPanel.setVisible(false);
    
		((CardLayout) ControlsPanel.getLayout()).show(ControlsPanel,"card1");
		ButtonsPanel.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.SOUTH, ButtonsPanel);
		JPanelLeft.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
		ButtonsPanel.add(BorderLayout.WEST,JPanelLeft);
		ShowResultsButton.setText("Show Results of Data Scan");
		ShowResultsButton.setActionCommand("Show Results");
		ShowResultsButton.setDefaultCapable(false);
		JPanelLeft.add(ShowResultsButton);
		JPanelCenter.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
		ButtonsPanel.add(BorderLayout.CENTER,JPanelCenter);
		StepNumberLabel.setText("Step #1");
		JPanelCenter.add(StepNumberLabel);
		StepNumberLabel.setForeground(java.awt.Color.black);
		CancelButton.setText("Cancel");
		CancelButton.setActionCommand("Cancel");
		JPanelCenter.add(CancelButton);
		BackButton.setText("< Back");
		BackButton.setActionCommand("< Back");
		BackButton.setEnabled(false);
		JPanelCenter.add(BackButton);
		NextButton.setText("Next >");
		NextButton.setActionCommand("Next >");
		JPanelCenter.add(NextButton);
		FinishButton.setText("Finish");
		FinishButton.setActionCommand("Finish");
		FinishButton.setEnabled(false);
		JPanelCenter.add(FinishButton);

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();
		NextButton.addActionListener(lSymAction);
		BackButton.addActionListener(lSymAction);
		FinishButton.addActionListener(lSymAction);
		ShowResultsButton.addActionListener(lSymAction);
		SymListSelection lSymListSelection = new SymListSelection();
		StartingLineTextField.addActionListener(lSymAction);
		SymFocus aSymFocus = new SymFocus();
		StartingLineTextField.addFocusListener(aSymFocus);
		SymItem lSymItem = new SymItem();
		ColumnLabelsCheckBox.addItemListener(lSymItem);
		TabCheckBox.addItemListener(lSymItem);
		CommaCheckBox.addItemListener(lSymItem);
		SpaceCheckBox.addItemListener(lSymItem);
		SemicolonCheckBox.addItemListener(lSymItem);
		OtherCheckBox.addItemListener(lSymItem);
		CancelButton.addActionListener(lSymAction);
    insertEnumButton.addActionListener(lSymAction);
		//}}
		
		resultsBuffer = new StringBuffer();

    ColumnDataPanel.add(BorderLayout.CENTER, cmePanel);

    //assign the filename and get the wizard started.
    if(dataFileName != null)
    {
      File ff = new File(dataFileName);
      TableNameTextField.setText(ff.getName());
      filename = dataFileName; 
      parsefile(filename);
      createLinesTable();
      resultsBuffer = new StringBuffer();
      stepNumber = 1;
      hasReturnedFromScreen2 = false;
      StepNumberLabel.setText("Step #"+stepNumber);
      CardLayout cl = (CardLayout)ControlsPanel.getLayout();
      cl.show(ControlsPanel, "card"+stepNumber);
      BackButton.setEnabled(false);
      FinishButton.setEnabled(false);
      NextButton.setEnabled(true);
    }
	}

	
	/**
	 * reference to a packagewizard
	 * used to pass a fixed XML string to a PackageWizard
	 */
	public void setEntityWizard(PackageWizard entity) {
	  this.entityWizard = entity;
	}

	/**
	 * reference to a packagewizard
	 * used to pass a fixed XML string to a PackageWizard
	 */
	public void setAttributeWizard(PackageWizard attribute) {
	  this.attributeWizard = attribute;
	}

	/**
	 * reference to a packagewizard
	 * used to pass a fixed XML string to a PackageWizard
	 */
	public void setPhysicalWizard(PackageWizard physical) {
	  this.physicalWizard = physical;
	}
	
	

	//{{DECLARE_CONTROLS
	java.awt.FileDialog saveFileDialog = new java.awt.FileDialog(this);
	java.awt.FileDialog openFileDialog = new java.awt.FileDialog(this);
	javax.swing.JPanel MainDisplayPanel = new javax.swing.JPanel();
	javax.swing.JPanel ControlsPlusDataPanel = new javax.swing.JPanel();
	javax.swing.JPanel ControlsPanel = new javax.swing.JPanel();
	javax.swing.JPanel Step1ControlsPanel = new javax.swing.JPanel();
	javax.swing.JPanel Step1_TopTitlePanel = new javax.swing.JPanel();
	javax.swing.JLabel Step1_TopTitleLabel = new javax.swing.JLabel();
	javax.swing.JPanel Step1_TableNamePanel = new javax.swing.JPanel();
	javax.swing.JLabel Step1_NameLabel = new javax.swing.JLabel();
	javax.swing.JTextField TableNameTextField = new javax.swing.JTextField();
	javax.swing.JPanel Step1_TableDescriptionPanel = new javax.swing.JPanel();
	javax.swing.JLabel Step1_TableDescriptionLabel = new javax.swing.JLabel();
	javax.swing.JTextField TableDescriptionTextField = new javax.swing.JTextField();
	javax.swing.JPanel Step1_DelimiterChoicePanel = new javax.swing.JPanel();
	javax.swing.JLabel Step1_DelimiterLabel = new javax.swing.JLabel();
	javax.swing.JPanel Step1_DelimeterRadioPanel = new javax.swing.JPanel();
	javax.swing.JRadioButton DelimitedRadioButton = new javax.swing.JRadioButton();
	javax.swing.JPanel Step1_FixedFieldRadioPanel = new javax.swing.JPanel();
	javax.swing.JRadioButton FixedFieldRadioButton = new javax.swing.JRadioButton();
	javax.swing.JPanel StartingLinePanel = new javax.swing.JPanel();
	javax.swing.JLabel StartingLineLabel = new javax.swing.JLabel();
	javax.swing.JTextField StartingLineTextField = new javax.swing.JTextField();
	javax.swing.JLabel ColumnLabelsLabel = new javax.swing.JLabel();
	javax.swing.JCheckBox ColumnLabelsCheckBox = new javax.swing.JCheckBox();
	javax.swing.JPanel Step2ControlsPanel = new javax.swing.JPanel();
	javax.swing.JPanel Step2_TopLabelPanel = new javax.swing.JPanel();
	javax.swing.JLabel Step2_TopLabel = new javax.swing.JLabel();
	javax.swing.JPanel Step2_DelimiterChoicePanel = new javax.swing.JPanel();
	javax.swing.JLabel Step2_DelimterChoiceLabel = new javax.swing.JLabel();
	javax.swing.JCheckBox TabCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox CommaCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox SpaceCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox SemicolonCheckBox = new javax.swing.JCheckBox();
	javax.swing.JCheckBox OtherCheckBox = new javax.swing.JCheckBox();
	javax.swing.JTextField OtherTextField = new javax.swing.JTextField();
	javax.swing.JPanel JPanel10 = new javax.swing.JPanel();
	javax.swing.JPanel JPanel11 = new javax.swing.JPanel();
	javax.swing.JPanel Step2_ConsequtivePanel = new javax.swing.JPanel();
	javax.swing.JCheckBox ConsecutiveCheckBox = new javax.swing.JCheckBox();
	javax.swing.JPanel Step3ControlsPanel = new javax.swing.JPanel();
	javax.swing.JPanel Step3_HelpPanel = new javax.swing.JPanel();
	javax.swing.JLabel Step3_HelpLabel = new javax.swing.JLabel();
	javax.swing.JPanel ColDataSummaryPanel = new javax.swing.JPanel();
	javax.swing.JPanel DataPanel = new javax.swing.JPanel();
	javax.swing.JScrollPane DataScrollPanel = new javax.swing.JScrollPane();
	javax.swing.JPanel ColumnDataPanel = new javax.swing.JPanel();
	javax.swing.JPanel ButtonsPanel = new javax.swing.JPanel();
	javax.swing.JPanel JPanelLeft = new javax.swing.JPanel();
	javax.swing.JButton ShowResultsButton = new javax.swing.JButton();
	javax.swing.JPanel JPanelCenter = new javax.swing.JPanel();
	javax.swing.JLabel StepNumberLabel = new javax.swing.JLabel();
	javax.swing.JButton CancelButton = new javax.swing.JButton();
	javax.swing.JButton BackButton = new javax.swing.JButton();
	javax.swing.JButton NextButton = new javax.swing.JButton();
	javax.swing.JButton FinishButton = new javax.swing.JButton();
  javax.swing.JLabel ColDataSummaryLabel = new JLabel("<html><b>Column Contents:</b></html>");
	javax.swing.JScrollPane UniqueItemsScrollPane = new javax.swing.JScrollPane();
	javax.swing.JList UniqueItemsList = new javax.swing.JList();
  javax.swing.JPanel TopColSummaryPanel = new JPanel();
  javax.swing.JPanel BottomColSummaryPanel = new JPanel();
  javax.swing.JButton insertEnumButton = new JButton("Insert");
  javax.swing.JPanel insertEnumPanel = new JPanel();
  javax.swing.JLabel insertEnumLabel = new JLabel();
  
//}}


	void exitApplication()
	{
	  // passes string version of XML docs created by TextImportWizard
	  // to a package wizard
	  
	  this.setVisible(false);
	  this.dispose();
	}

   class UneditableTableModel extends javax.swing.table.DefaultTableModel 
   {
        public UneditableTableModel(Vector data, Vector columnNames) {
            super(data, columnNames);
        }
        
        public boolean isCellEditable(int row, int col) {
            return false;
        }
   }


	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == TextImportWizard.this)
				TextImportWizard_windowClosing(event);
		}
	}

	void TextImportWizard_windowClosing(java.awt.event.WindowEvent event)
	{
		TextImportWizard_windowClosing_Interaction1(event);
	}

	void TextImportWizard_windowClosing_Interaction1(java.awt.event.WindowEvent event) {
		try {
			this.exitApplication();
		} catch (Exception e) {
		}
	}
  
  public void showInsertEnumPanel(boolean b) {
     insertEnumPanel.setVisible(b);
  }
  
  public void resetColumnHeader(String newColHeader) {
    int selectedCol = table.getSelectedColumn();
    if ((selectedCol>-1)&&(colTitles.size()>0)) {
      colTitles.removeElementAt(selectedCol);
      colTitles.insertElementAt(newColHeader,selectedCol);
      buildTable(colTitles, vec);
      ColumnData cd = (ColumnData)colDataInfo.elementAt(selectedCol);
      cd. colTitle = newColHeader;
      table.setColumnSelectionInterval(selectedCol,selectedCol);
    }
  }

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == NextButton)
				NextButton_actionPerformed(event);
			else if (object == BackButton)
				BackButton_actionPerformed(event);
			else if (object == FinishButton)
				FinishButton_actionPerformed(event);
			else if (object == ShowResultsButton)
				ShowResultsButton_actionPerformed(event);
			else if (object == CancelButton)
				CancelButton_actionPerformed(event);
      else if (object == insertEnumButton)
        insertEnumButton_actionPerformed(event);
		}
	}

  
  
public void startImport(String file) {
        TableNameTextField.setText(file);
        parsefile(file);
        createLinesTable();
		    resultsBuffer = new StringBuffer();
		    stepNumber = 1;
		    StepNumberLabel.setText("Step #"+stepNumber);
		    CardLayout cl = (CardLayout)ControlsPanel.getLayout();
		    cl.show(ControlsPanel, "card"+stepNumber);
		    BackButton.setEnabled(false);
		    FinishButton.setEnabled(false);
		    NextButton.setEnabled(true);
}




  /**
   * creates a JTable based on lines in input
   */
  void createLinesTable() {
        Vector vec = new Vector();
        for (int i=0;i<nlines;i++) {
          Vector vec1 = new Vector();
          vec1.addElement(new String().valueOf(i+1));
          vec1.addElement(lines[i]);
          vec.addElement(vec1);
        }
        Vector title = new Vector();
        title.addElement("#");
        title.addElement("Lines in "+filename);
        UneditableTableModel linesTM = new UneditableTableModel(vec, title);
        table = new JTable(linesTM);
		    table.setFont(new Font("MonoSpaced", Font.PLAIN, 14));
		   (table.getTableHeader()).setReorderingAllowed(false);
        
		    TableColumn column = null;
		    column = table.getColumnModel().getColumn(0);
		    column.setPreferredWidth(40);
		    column.setMaxWidth(40);
        DataScrollPanel.getViewport().add(table);
    
  }
	
    /**
     * parses data input file into an array of lines (Strings)
     * 
     * @param f the file name
     */
    private void parsefile (String f) {
      int i;
      int pos;
      String temp, temp1;
        
      if (isTextFile(f)) {
        resultsBuffer = new StringBuffer("");
        resultsBuffer.append(f+" is apparently a text file\n");
        textFlag = true;
        try {
        BufferedReader in = new BufferedReader(new FileReader(f));
        nlines = 0;
        try {
            while ((temp = in.readLine())!=null) {
                if (temp.length()>0) {   // do not count blank lines
                nlines++;} 
            }
            in.close();
        }
        catch (IOException e) {};
        }
        catch (IOException e) {};
        
        nlines_actual = nlines;
        if (nlines>nlines_max) {
           nlines = nlines_max; 
           JOptionPane.showMessageDialog(this, "Data File parsing has been truncated due to large size! (Note: NO data has been lost!)",
                        "Message",JOptionPane.INFORMATION_MESSAGE, null);
        }
        
        lines = new String[nlines];
        // now read again since we know how many lines
        try {
        BufferedReader in1 = new BufferedReader(new FileReader(f));
        try {
            for (i=0;i<nlines;i++) {
                temp = in1.readLine();
                while (temp.length()==0) {temp=in1.readLine();}
                lines[i] = temp + "\n";
            }
            in1.close();
        }
        catch (IOException e) {};
        }
        catch (IOException e) {};
        resultsBuffer.append("Number of lines: "+nlines+"\n");
        resultsBuffer.append("Most probable delimiter is "+guessDelimiter()+"\n");
      }
      else {
        resultsBuffer = new StringBuffer("");
        resultsBuffer.append(f+" is NOT a text file\n");
        textFlag = false;
        JOptionPane.showMessageDialog(this, "Selected File is NOT a text file!",
           "Message",JOptionPane.INFORMATION_MESSAGE, null);
        
      }
    }            
    /**
     * parses data input string into an array of lines (Strings)
     * 
     * @param s input string
     */

    private void parseString (String s) {
        int i;
        int pos;
        String temp, temp1;
          BufferedReader in = new BufferedReader(new StringReader(s));
          nlines = 0;
          try {
            while ((temp = in.readLine())!=null) {
                if (temp.length()>0) {   // do not count blank lines
                nlines++;} 
            }
            in.close();
          }
        catch (Exception e) {};
        
        nlines_actual = nlines;
        if (nlines>nlines_max) nlines=nlines_max;
        lines = new String[nlines];
          // now read again since we know how many lines
          BufferedReader in1 = new BufferedReader(new StringReader(s));
          try {
            for (i=0;i<nlines;i++) {
                temp = in1.readLine();
                while (temp.length()==0) {temp=in1.readLine();}
                lines[i] = temp + "\n";

            }
            in1.close();
          }
          catch (Exception e) {};
        resultsBuffer = new StringBuffer("");
        resultsBuffer.append("Number of lines: "+nlines_actual+"\n");
        resultsBuffer.append("Most probable delimiter is "+guessDelimiter()+"\n");
    }            
	
	private void parseDelimited() {

        if (lines!=null) {
	    int start = startingLine;  // startingLine is 1-based not 0-based
        int numcols  = 0;          // init
        if (hasReturnedFromScreen2 && isScreen1Unchanged() & colTitles!=null){
            //don't redefine column headings etc - keep user's previous values, 
            // since nothing has changed. In this case colTitles is already set:
            numcols  = colTitles.size();
        } else {
            if (labelsInStartingLine) {
              colTitles = getColumnValues(lines[startingLine-1]);
            }
            else {
              colTitles = getColumnValues(lines[startingLine-1]);  // use just to get # of cols
              int temp = colTitles.size();
              colTitles = new Vector();
              for (int l=0;l<temp;l++) {
                colTitles.addElement("Column "+(l+1));  
              }
              start--;  // include first line
            }
            vec = new Vector();
            Vector vec1 = new Vector();
            numcols = colTitles.size();
            resultsBuffer.append("Number of columns assumed: "+numcols+"\n");
            for (int i=start;i<nlines;i++) {
              vec1 = getColumnValues(lines[i]);
              boolean missing = false;
              while (vec1.size()<numcols) {
                vec1.addElement("");
                missing = true;
              }
              if (missing) {
                resultsBuffer.append("Insufficient number of items in row "+(i+1)+"\n"+" Empty strings added!"+"\n"+"\n"); 
              }
              vec.addElement(vec1);
            }
        }
	    buildTable(colTitles, vec);
	    colDataInfo = new Vector();   // vector of ColumnData objects
	    for (int k=0;k<numcols;k++) {
	      ColumnData cd = new ColumnData(k);
	      colDataInfo.addElement(cd);
	      cd.colType = guessColFormat(k);
        if (cd.colType.equals("text")) cd.textChoice=true;
        if (cd.colType.equals("float")) cd.numChoice=true;
        if (cd.colType.equals("integer")) cd.numChoice=true;
	      cd.colTitle = (String)colTitles.elementAt(k);
	      cd.colName = (String)colTitles.elementAt(k);
	      Vector lst = getUniqueColValues(k);
	      cd.colUniqueItemsList = lst;
	      cd.colUniqueItemsDefs = lst;
	      cd.colNumUniqueItems = lst.size();
	    }
    }
	}
	
	/**
	 * builds JTable from input data ans includes event code for handling clicks on
	 * table (e.g. column selection)
	 * 
	 * @param cTitles
	 * @param data
	 */
	private void buildTable(Vector cTitles, Vector data) {
 //     final JTable table = new JTable(vec, colTitles);
      UneditableTableModel myTM = new UneditableTableModel(vec, colTitles);
      table = new JTable(myTM);
      
      table.setColumnSelectionAllowed(true);
      table.setRowSelectionAllowed(false);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      
      ListSelectionModel colSM = table.getColumnModel().getSelectionModel();
          colSM.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                  //Ignore extra messages.
                  if (e.getValueIsAdjusting()) return;
                  
                  ListSelectionModel lsm =
                      (ListSelectionModel)e.getSource();
                  if (lsm.isSelectionEmpty()) {
                      //no columns are selected
                  } else {
                     // first save current data
                     if (save_flag) {
                      cmePanel.enumTableToColData();
                      cmePanel.FieldsToColData();
                     }
                       
                      String str = "<b>Column Contents:</b><br>";
                      selectedCol = lsm.getMinSelectionIndex();
                      str = str + "Selected Column: "+selectedCol +"<br>";
                      //selectedCol is selected
                      ColumnData cd = (ColumnData)colDataInfo.elementAt(selectedCol);
                      int numUnique =  cd.colNumUniqueItems;
                      str = str + "There are "+numUnique+" unique item(s) in this column<br>";
                      if ((cd.colType.equals("float"))||(cd.colType.equals("decimal"))
                                                     ||(cd.colType.equals("double"))) {
                        String dmin = Double.toString(cd.colMin);
                        String dmax = Double.toString(cd.colMax);
                        long raver = (Math.round(cd.colAverage*100.0));
                        String daver = Long.toString(raver);
                        daver = daver.substring(0,daver.length()-2)+"."+daver.substring(daver.length()-2);
                        str = str + "Min:"+ dmin +"  Max:" + dmax + "  Aver:" +daver+"<br>";
                      } 
                      else if ((cd.colType.equals("integer"))) {
                        String min = Integer.toString((int)cd.colMin);
                        String max = Integer.toString((int)cd.colMax);
                        long iaver = (Math.round(cd.colAverage*100.0));
                        String aver = Long.toString(iaver);
                        aver = aver.substring(0,aver.length()-2)+"."+aver.substring(aver.length()-2);
                        str = str + "Min:"+ min +"  Max:" + max + "  Aver:" +aver+"<br>";
                     }
                      else {
                      }
                      String[] headers = new String[2];
                      headers[0] = "Code";
                      headers[1] = "Definition";
                      DefaultTableModel dtm = new DefaultTableModel(headers,0);
                      String[] row = new String[2];
                      for (int j=0;j<cd.colUniqueItemsList.size();j++) {
                        row[0] = (String)cd.colUniqueItemsList.elementAt(j);
                        row[1] = (String)cd.colUniqueItemsDefs.elementAt(j);
                        dtm.addRow(row);
                      }
                      for (int i=0;i<20;i++) {
                        row[0] = "";
                        row[1] = "";
                        dtm.addRow(row);
                      }
                      UniqueItemsList.setListData(cd.colUniqueItemsList);
                      
                      ColDataSummaryLabel.setText("<html>"+str+"</html>");
                      showInsertEnumPanel(cd.enumChoice); 
                      cmePanel.setColumnData(cd);
                     
                  }
              }
          });
      
      DataScrollPanel.getViewport().removeAll();
      DataScrollPanel.getViewport().add(table);
	}

	/**
	 * parses a line of text data into a Vector of column data for that row
	 * 
	 * @param str a line of string data from input
	 * @return a vector with each elements being column data for the row
	 */
	private Vector getColumnValues(String str) {
	    String sDelim = getDelimiterString();
	    String oldToken = "";
	    String token = "";
	    Vector res = new Vector();
	    ignoreConsequtiveDelimiters = ConsecutiveCheckBox.isSelected();
	    if (ignoreConsequtiveDelimiters) {
	      StringTokenizer st = new StringTokenizer(str, sDelim, false);
	      while( st.hasMoreTokens() ) {
	        token = st.nextToken().trim();
	        res.addElement(token);
	      }
	    }
	    else {
	      StringTokenizer st = new StringTokenizer(str, sDelim, true);
	      while( st.hasMoreTokens() ) {
	        token = st.nextToken().trim();
	        if (!inDelimiterList(token, sDelim)) {
	            res.addElement(token);
	        }
	        else {
	            if ((inDelimiterList(oldToken,sDelim))&&(inDelimiterList(token,sDelim))) {
	                res.addElement("");
                }
	        }
	        oldToken = token;
	      }
	    }
	    return res;
	}
	
	private String getDelimiterString() {
	  String str = "";
	  if (TabCheckBox.isSelected()) str = str+"\t";
	  if (CommaCheckBox.isSelected()) str = str + ",";
	  if (SpaceCheckBox.isSelected()) str = str + " ";
	  if (SemicolonCheckBox.isSelected()) str = str +";";
	  if (OtherCheckBox.isSelected()) {
	    String temp = OtherTextField.getText();
	    if (temp.length()>0) {
	      temp = temp.substring(0,1);
	      str = str + temp;
	    }
	  }
	  return str;
	}

	private String getDelimiterStringAsText() {
	  String str = "";
	  if (TabCheckBox.isSelected()) str = str+"#x09";
	  if (CommaCheckBox.isSelected()) str = str + ",";
	  if (SpaceCheckBox.isSelected()) str = str + "#x20";
	  if (SemicolonCheckBox.isSelected()) str = str +";";
	  if (OtherCheckBox.isSelected()) {
	    String temp = OtherTextField.getText();
	    if (temp.length()>0) {
	      temp = temp.substring(0,1);
	      str = str + temp;
	    }
	  }
	  return str;
	}
	
	
	private boolean inDelimiterList(String token, String delim) {
	    boolean result = false;
	    int test = delim.indexOf(token);
	    if (test>-1) {
	        result = true;
	    }
	    else { result = false; }
	    return result;
	}
	
  void insertEnumButton_actionPerformed(java.awt.event.ActionEvent event) {
    int selectedCol = table.getSelectedColumn();
    ColumnData cd = (ColumnData)colDataInfo.elementAt(selectedCol);
    cd.enumCodeVector = cd.colUniqueItemsList;
    
    if (cd.enumDefinitionVector==null) {
      cd.enumDefinitionVector = new Vector();
    }
    if (cd.enumDefinitionVector.size()!=cd.colUniqueItemsList.size()){
      cd.enumDefinitionVector = new Vector();
      for (int i=0;i<cd.colUniqueItemsList.size();i++) {
        cd.enumDefinitionVector.addElement("");
      }
    }

    if (cd.enumSourceVector==null) {
      cd.enumSourceVector = new Vector();
    }
    if (cd.enumSourceVector.size()!=cd.colUniqueItemsList.size()){
      cd.enumSourceVector = new Vector();
      for (int i=0;i<cd.colUniqueItemsList.size();i++) {
        cd.enumSourceVector.addElement("");
      }
    }
    cmePanel.setColumnData(cd);
  }
  
  
	void NextButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		stepNumber++;
    if (stepNumber>1) table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		if (stepNumber==3) FinishButton.setEnabled(true);
	  if (stepNumber<3) {
		  BackButton.setEnabled(true);
		}
		else {
		  //NextButton.setEnabled(false);
      
		}
		StepNumberLabel.setText("Step #"+stepNumber);
		CardLayout cl = (CardLayout)ControlsPanel.getLayout();
		cl.show(ControlsPanel, "card"+stepNumber);
		if (stepNumber == 2) parseDelimited();
		if (stepNumber >= 3) {
		  StepNumberLabel.setText("Step #"+stepNumber+" of " + (table.getColumnCount()+2));
		  ColumnDataPanel.setVisible(true);
      table.setColumnSelectionInterval(stepNumber-3,stepNumber-3);
		}
		else {
		    ColumnDataPanel.setVisible(false);
	  }
    if (stepNumber>=table.getColumnCount()+2) {
      NextButton.setEnabled(false);
    }
    else {
      NextButton.setEnabled(true);      
    }
	}

	void BackButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		stepNumber--;
    if (stepNumber<2) table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        if (stepNumber==1){
            saveScreen1Settings();
            hasReturnedFromScreen2=true;
        }
		if (stepNumber >= 3) {
		    ColumnDataPanel.setVisible(true);
        table.setColumnSelectionInterval(stepNumber-3,stepNumber-3);
		}
		else {
		    ColumnDataPanel.setVisible(false);
	    }
        
		if (stepNumber<3) FinishButton.setEnabled(false);
		if (stepNumber>1) {
		  NextButton.setEnabled(true);
		}
		else {
		  BackButton.setEnabled(false); 
		}
		StepNumberLabel.setText("Step #"+stepNumber+" of " + (table.getColumnCount()+2));
		CardLayout cl = (CardLayout)ControlsPanel.getLayout();
		cl.show(ControlsPanel, "card"+stepNumber);
		if (stepNumber==1) {
		  if (lines!=null) {
		    createLinesTable(); 
		  }
		}
	}

	void FinishButton_actionPerformed(java.awt.event.ActionEvent event)
	{
	String info = checkForBlankInfo();
	// info should be null if all fields are not blank
	if (info!=null) {
        int  choice = JOptionPane.showConfirmDialog(null, 
                             "This package may be invalid because certain metadata" + 
                             "fields which refer to columns \n"+info+"\n contain no information. \n " +
                             "To correct this, please press Cancel or No\n" +
                             "and then select each column in the table\n" +
                             "and enter the appropriate metadata information.\n\n"+
                             "Are you sure you want to save now?", 
                             "Invalid Document", 
                             JOptionPane.YES_NO_CANCEL_OPTION,
                             JOptionPane.WARNING_MESSAGE);
      if((choice == JOptionPane.CANCEL_OPTION)||(choice == JOptionPane.NO_OPTION)) {
        return;  
      }
	}
	    
    if (entityWizard!=null) {
      //System.out.println("===============creating xml string: " + createXMLEntityString());
	    entityWizard.setXMLString(createXMLEntityString());
	  }
	  if (attributeWizard!=null) {
	    attributeWizard.setXMLString(createXMLAttributeString());
      //System.out.println("===============creating xml string: " + createXMLAttributeString());
	  }
	  if (physicalWizard!=null) {
	    physicalWizard.setXMLString(createXMLPhysicalString());
	  }
    
	  for (int i=0;i<colTitles.size();i++) {
	    checkColumnInfo(i);
	  }
	  
	  
	  String tempS = "Click on 'Show Results' button to see results, including XML files.";
	  if (entityWizard!=null) {
	    tempS = tempS + " Information will automatically be added to the Data Package under construction.";
	  }
	  else {
	    tempS = tempS + " Also, see File Menu to Save Files.";
	  }
	  finishFlag = true;
    if(listener != null)
    {
      listener.importComplete();
    }
    this.dispose();
	}


  /*
   *    Called if user is returning to screen 1 from screen 2 - 
   *    Saves the screen 1 settings so we can tell if anything has been changed 
   *    when user hits "next" again
   */

  String[] screen1Settings = new String[2];
  
  private final int IMPORT_START_ROW    = 0;
  private final int LABELS_IN_START_ROW = 1;

  private void saveScreen1Settings(){
    
    screen1Settings[IMPORT_START_ROW]   = StartingLineTextField.getText();
    screen1Settings[LABELS_IN_START_ROW]= (ColumnLabelsCheckBox.isSelected())?
                                                              "true" : "false";
 }
  
  
  /*
   *    Called if user is returning to screen 1 from screen 2 - 
   *    Chaecks latest screen 1 settings against the saved original settings so 
   *    we can tell if anything has been changed.  
   *
   *    Returns true if settings *NOT* changed (ie "isScreen1Unchanged" is true)
   */
  String labelsInStartLine_Status = null;

  private boolean isScreen1Unchanged(){
      
    labelsInStartLine_Status=(ColumnLabelsCheckBox.isSelected())?"true":"false";

    return (
      screen1Settings[IMPORT_START_ROW].equals(StartingLineTextField.getText())
      && screen1Settings[LABELS_IN_START_ROW].equals(labelsInStartLine_Status));
  }
 

  
  
 /*-----------------------------------
  * routines for trying to guess the delimiter being used
  *
  */
  
  /* returns the number of occurances of a substring in specified input string 
   * inS is input string
   * subS is substring
  */
  private int charCount(String inS, String subS ) {
    int cnt = -1;
    int pos = 0;
    int pos1 = 0;
    while (pos > -1) {
      pos1=inS.indexOf(subS, pos+1);
      pos = pos1;
      cnt++; 
    }
    if (cnt<0) cnt = 0;
    return cnt;
  }
  
   /**
    * return most frequent number of occurances of indicated substring
    * 
    * @param subS delimiter substring
    */
   private int mostFrequent(String subS) {
    int maxcnt = 500; // arbitrary limit of 500 occurances
    int[] freq = new int[maxcnt];  
      for (int i=0;i<nlines;i++) {
        int cnt = charCount(lines[i],subS);
        if (cnt>maxcnt-1) cnt = maxcnt-1;
        freq[cnt]++;
      }
      int mostfreq = 0;
      int mostfreqindex = 0;
      int tot = 0;
      for (int j=0;j<maxcnt;j++) {
        tot = tot + freq[j];
        if (freq[j]>mostfreq) {
          mostfreq = freq[j];
          mostfreqindex = j;
        }
      }
      // establish a threshold; if less than, then return 0
      if ( (100*mostfreq/tot)<80) mostfreq = 0;
      return mostfreqindex;
   }

   
  /**
   * guesses a delimiter based on frequency of appearance of common delimites
   */
  private String guessDelimiter() {
    if (mostFrequent("\t")>0) {
      parseOn = false;
      TabCheckBox.setSelected(true);
      CommaCheckBox.setSelected(false);
      SpaceCheckBox.setSelected(false);
      SemicolonCheckBox.setSelected(false);
      OtherCheckBox.setSelected(false);
      parseOn = true;
      return "tab";
    }
    else if (mostFrequent(",")>0) {
      parseOn = false;
      TabCheckBox.setSelected(false);
      CommaCheckBox.setSelected(true);
      SpaceCheckBox.setSelected(false);
      SemicolonCheckBox.setSelected(false);
      OtherCheckBox.setSelected(false);
      parseOn = true;
      return "comma";
    }
    else if (mostFrequent(" ")>0) {
      parseOn = false;
      TabCheckBox.setSelected(false);
      CommaCheckBox.setSelected(false);
      SpaceCheckBox.setSelected(true);
      SemicolonCheckBox.setSelected(false);
      OtherCheckBox.setSelected(false);
      parseOn = true;
      return "space";
    }
    else if (mostFrequent(";")>0) {
      parseOn = false;
      TabCheckBox.setSelected(false);
      CommaCheckBox.setSelected(false);
      SpaceCheckBox.setSelected(false);
      SemicolonCheckBox.setSelected(true);
      OtherCheckBox.setSelected(false);
      parseOn = true;
      return "semicolon";
    }
    else if (mostFrequent(":")>0) {
      parseOn = false;
      TabCheckBox.setSelected(false);
      CommaCheckBox.setSelected(false);
      SpaceCheckBox.setSelected(true);
      SemicolonCheckBox.setSelected(false);
      OtherCheckBox.setSelected(true);
      OtherTextField.setText(":");
      parseOn = true;
      return "colon";
    }
      parseOn = false;
      TabCheckBox.setSelected(false);
      CommaCheckBox.setSelected(false);
      SpaceCheckBox.setSelected(true);
      SemicolonCheckBox.setSelected(false);
      OtherCheckBox.setSelected(false);
      parseOn = true;
    return "unknown";
  }
  
  //000000000000000000000000000000000
  
  
  
  // ----------------------------------------------
  // routines for checking content of columns
  
  boolean isInteger(String s) {
    boolean res = true;
    try {
      int III = Integer.parseInt(s);
    }
    catch (Exception w) {
      res = false;
    }
    return res;
  }

  
  boolean isDouble(String s) {
    boolean res = true;
    try {
      Double III = Double.valueOf(s);
    }
    catch (Exception w) {
      res = false;
    }
    return res;
  }
  
  //DateFormat dateFormat = DateFormat.getDateInstance();  //see isDate() method
  boolean isDate(String s) {
    boolean res = true;
    try {
      long III = Date.parse(s);   //--- DEPRECATED
      //Date III = dateFormat.parse(s);     //--- This should replace the above 
                                            //(deprecated) method call, but it 
                                            //DOESN'T WORK in the same way - 
                                            //ie results are different!
    }
    catch (Exception w) {
      res = false;
    }
    return res;
  }

  
  /**
   * guesses column type based on frequency of content
   * types include text, integer, floating point number, and date.
   * Guess is based on frequency of occurance
   * 
   * @param colNum column number
   */
  String guessColFormat(int colNum) {
    int minInt = 0;
    int maxInt = 0;
    int intSum = 0;
    double intAverage = 0.0;
    double doubleAverage = 0.0;
    double minDouble = 0;
    double maxDouble = 0;
    double doubleSum = 0.0;
    int integerCount = 0;
    int doubleCount = 0;
    int dateCount = 0;
    int emptyCount = 0;
    boolean firsttime = true;
    for (int i=0;i<vec.size();i++) {
      Vector v = (Vector)vec.elementAt(i);
      String str = (String)v.elementAt(colNum);
      if (str.trim().length()<1) {
        emptyCount++;
      }
      else {
        if (isInteger(str)) {
          integerCount++;
          int val = Integer.parseInt(str);
          if (firsttime) {
            maxInt = val;
            minInt = val;
            firsttime = false;
          }
          if (val>maxInt) maxInt = val;
          if (val<minInt) minInt = val;
          intSum = intSum+val;
          intAverage = ((double)(intSum))/integerCount;
        }
        else if (isDouble(str)) {
          doubleCount++;
          Double dval = new Double(str);
          double val = dval.doubleValue();
          if (firsttime) {
            maxDouble = val;
            minDouble = val;
            firsttime = false;
          }
          if (val>maxDouble) maxDouble = val;
          if (val<minDouble) minDouble = val;
          doubleSum = doubleSum+val;
          doubleAverage = ((doubleSum))/doubleCount;
        }
        if (isDate(str)) {
          dateCount++;
        }
      }
    }
    if (integerCount>doubleCount) {
      if ((integerCount>0)&&((100*(integerCount+emptyCount)/vec.size()))>90) {
	      ColumnData cd = (ColumnData)colDataInfo.elementAt(colNum);
          cd.colAverage = intAverage;
          cd.colMin = minInt;
          cd.colMax = maxInt;
        return "integer";  
      }
    }
    if (doubleCount>integerCount) {
      if ((doubleCount>0)&&((100*(doubleCount+emptyCount)/vec.size()))>90) {
	      ColumnData cd = (ColumnData)colDataInfo.elementAt(colNum);
          cd.colAverage = doubleAverage;
          cd.colMin = minDouble;
          cd.colMax = maxDouble;
        return "float";  
      }
    }
    if ((dateCount>0)&&((100*(dateCount+emptyCount)/vec.size()))>90) {
      return "date";  
    }
    return "string";
  }
 
 /**
  *  creates a Vector containing all the unique items (Strings) in the
  *  column
  */
  Vector getUniqueColValues(int colNum) {
    Vector res = new Vector();
    for (int i=0;i<vec.size();i++) {
      Vector v = (Vector)vec.elementAt(i);
      String str = (String)v.elementAt(colNum);
      if (!str.equals("")) {  // ignore empty strings
        if (!res.contains(str)) {
            res.addElement(str);  
        }
      }
    }
    ColumnData cd = (ColumnData)colDataInfo.elementAt(colNum);
    cd.colUniqueItemsList = res;
    return res;
  }
  
 /**
  * Uses the assumed column type to check all values in a column.
  * If data is not the correct type, a message is added to the Results
  * StringBuffer.
  * 
  * @param colNum column number
  */
 void checkColumnInfo(int colNum) {
  ColumnData cd = (ColumnData)colDataInfo.elementAt(colNum);   
  if ((cd.colType.length())>0) {
      String type = cd.colType;
      if (type.equals("text")) {
      }
      else if (type.equals("integer")) {
        for (int j=0;j<vec.size();j++) {
          Vector v = (Vector)vec.elementAt(j);
          String str = (String)v.elementAt(colNum);
          if ((blankCheckFlag)||(str.length()>0)) {
            if (!isInteger(str)) {
                resultsBuffer.append("Item in col "+(colNum+1)+" and row "+(j+1)+" is NOT an integer!\n");
            }
          }
        }
      }
      else if (type.equals("double")) {
        for (int j=0;j<vec.size();j++) {
          Vector v = (Vector)vec.elementAt(j);
          String str = (String)v.elementAt(colNum);
          if ((blankCheckFlag)||(str.length()>0)) {
            if (!isDouble(str)) {
                resultsBuffer.append("Item in col "+(colNum+1)+" and row "+(j+1)+" is NOT a Floating Point number!\n");
            }
          }
        }
      }
      else if (type.equals("date")) {
        for (int j=0;j<vec.size();j++) {
          Vector v = (Vector)vec.elementAt(j);
          String str = (String)v.elementAt(colNum);
          if ((blankCheckFlag)||(str.length()>0)) {
            if (!isDate(str)) {
                resultsBuffer.append("Item in col "+(colNum+1)+" and row "+(j+1)+" is NOT a Date!\n");
            }
          }
        }
      }
  }
 }

  
  /**
   * attempts to check to see if a file is just text or is binary. Reads bytes in 
   * file and looks for '0'. If any '0's are found, assumed that the file is NOT a
   * text file.
   * 
   * @param filename
   */
  //000000000000000000000000000000000
  
 
 /* Checks a file to see if it is a text file by looking for bytes containing '0'
 */
   private boolean isTextFile(String filename) { 
     boolean text = true; 
     int res; 
     int cnt = 0;
     int maxcnt = 2000; // only check this many bytes to avoid performance problems
     try { 
         FileInputStream in = new FileInputStream(filename); 
         while (((res = in.read())>-1) &&(cnt<maxcnt)) { 
             cnt++;
             if (res==0) text = false; 
         } 
     } 
     catch (Exception e) {} 
     return text; 
 } 
  

	void ShowResultsButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		// to do: code goes here.
			 
		ShowResultsButton_actionPerformed_Interaction1(event);
	}

	void ShowResultsButton_actionPerformed_Interaction1(java.awt.event.ActionEvent event)
	{
	  for (int i=0;i<colTitles.size();i++) {
	    checkColumnInfo(i);
	  }
	    
	  try {
			// ResultsFrame Create and show the ResultsFrame
			TextImportResultsFrame rf = new TextImportResultsFrame();
			rf.ResultsTextArea.setText(resultsBuffer.toString());
			rf.setVisible(true);
		} catch (java.lang.Exception e) {
		}
	}


	void ColumnLabelTextField_actionPerformed(java.awt.event.ActionEvent event)
	{
    if (selectedCol>-1) {
      if (colTitles.size()>0) {
        colTitles.removeElementAt(selectedCol);
//        colTitles.insertElementAt(ColumnLabelTextField.getText(), selectedCol);
        buildTable(colTitles, vec);
        ColumnData cd = (ColumnData)colDataInfo.elementAt(selectedCol);
 //       cd.colTitle = ColumnLabelTextField.getText();
      }
    }
	}






	void ColumnLabelTextField_focusLost(java.awt.event.FocusEvent event)
	{
    if (selectedCol>-1) {
      if (colTitles.size()>0) {
        colTitles.removeElementAt(selectedCol);
//        colTitles.insertElementAt(ColumnLabelTextField.getText(), selectedCol);
        buildTable(colTitles, vec);
        ColumnData cd = (ColumnData)colDataInfo.elementAt(selectedCol);
//        cd.colTitle = ColumnLabelTextField.getText();
      }
    }
	}



	class SymListSelection implements javax.swing.event.ListSelectionListener
	{
		public void valueChanged(javax.swing.event.ListSelectionEvent event)
		{
			Object object = event.getSource();
		}
	}

   
	void StartingLineTextField_actionPerformed(java.awt.event.ActionEvent event)
	{
 		String str = StartingLineTextField.getText();
		if (isInteger(str)) {
          startingLine = (Integer.valueOf(str)).intValue();
 		} else {
		  StartingLineTextField.setText(String.valueOf(startingLine));
		}
	}

	class SymFocus extends java.awt.event.FocusAdapter
	{
		public void focusLost(java.awt.event.FocusEvent event)
		{
			Object object = event.getSource();
			if (object == StartingLineTextField)
				StartingLineTextField_focusLost(event);
		}
	}

	void StartingLineTextField_focusLost(java.awt.event.FocusEvent event)
	{
		String str = StartingLineTextField.getText();
		if (isInteger(str)) {
		  startingLine = (Integer.valueOf(str)).intValue();  
		}
		else {
		  StartingLineTextField.setText(String.valueOf(startingLine));
		}			 
	}
	
	

	class SymItem implements java.awt.event.ItemListener
	{
		public void itemStateChanged(java.awt.event.ItemEvent event)
		{
			Object object = event.getSource();
			if (object == ColumnLabelsCheckBox)
				ColumnLabelsCheckBox_itemStateChanged(event);
			else if (object == TabCheckBox)
				TabCheckBox_itemStateChanged(event);
			else if (object == CommaCheckBox)
				CommaCheckBox_itemStateChanged(event);
			else if (object == SpaceCheckBox)
				SpaceCheckBox_itemStateChanged(event);
			else if (object == SemicolonCheckBox)
				SemicolonCheckBox_itemStateChanged(event);
			else if (object == OtherCheckBox)
				OtherCheckBox_itemStateChanged(event);
		}
	}

	void ColumnLabelsCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
    labelsInStartingLine = ColumnLabelsCheckBox.isSelected();
  }
  
  
  

	void TabCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
	  if (parseOn) {
		  parseDelimited();
		}
	}

	void CommaCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
	  if (parseOn) {
		  parseDelimited();
		}
	}

	void SpaceCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
	  if (parseOn) {
  		parseDelimited();
  	}
	}

	void SemicolonCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
	  if (parseOn) {
		  parseDelimited();
		}
	}

	void OtherCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
	{
	  if (parseOn) {
		  parseDelimited();
		}
	}
	
	/**
	 * Hardcoded routine to create an XML Attribute metadata string based on
	 * data
	 * ---BAD PRACTICE--- should use config to get info
	 */
	public String createXMLAttributeString() {
	  StringBuffer XMLBuffer = new StringBuffer();
	  XMLBuffer.append("<?xml version=\"1.0\"?>\n");
	  XMLBuffer.append("<!DOCTYPE eml-attribute PUBLIC \"-//ecoinformatics.org//eml-attribute-2.0.0beta6//EN\" \"eml-attribute.dtd\">\n");
	  XMLBuffer.append("<eml-attribute>\n");
	  XMLBuffer.append("    <identifier> </identifier>\n");
	  for (int i=0;i<colTitles.size();i++) {
	    ColumnData cd = (ColumnData)colDataInfo.elementAt(i);
	    XMLBuffer.append("    <attribute>\n");
	    XMLBuffer.append("        <attributeName> "+normalize(cd.colName)+"</attributeName>\n");
	    XMLBuffer.append("        <attributeLabel> "+normalize(cd.colTitle)+"</attributeLabel>\n");
	    XMLBuffer.append("        <attributeDefinition>"+normalize(cd.colDefinition)+"</attributeDefinition>\n");
	    XMLBuffer.append("        <unit> "+normalize(cd.colUnits)+"</unit>\n");
	    XMLBuffer.append("        <dataType> "+normalize(cd.colType)+"</dataType>\n");
	    XMLBuffer.append("        <attributeDomain>\n");
      if (cd.numChoice) {
	        XMLBuffer.append("             <numericDomain>\n");
	        XMLBuffer.append("                <minimum>"+cd.colMin +"</minimum>\n");
	        XMLBuffer.append("                <maximum>"+cd.colMax +"</maximum>\n");
	        XMLBuffer.append("             </numericDomain>\n");
	    }
      else if(cd.enumChoice) {
	        for (int k=0;k<cd.enumCodeVector.size();k++) {
	            XMLBuffer.append("             <enumeratedDomain>\n");
	            XMLBuffer.append("                <code>"+(String)cd.enumCodeVector.elementAt(k)+"</code>\n");
	            XMLBuffer.append("                <definition>"+(String)cd.enumDefinitionVector.elementAt(k)+"</definition>\n");
	            XMLBuffer.append("                <source>"+(String)cd.enumSourceVector.elementAt(k)+"</source>\n");
	            XMLBuffer.append("             </enumeratedDomain>\n");
	        }
	    }
	    else {
	        XMLBuffer.append("             <textDomain>\n");
          if (cd.colTextDefinition.length()>0) {
            XMLBuffer.append("                <definition>"+cd.colTextDefinition+"</definition>\n");
          }
          else{
	          XMLBuffer.append("                <definition>any text</definition>\n");
          }
          if (cd.colTextPattern.length()>0) {
            XMLBuffer.append("                <definition>"+cd.colTextPattern+"</definition>\n");
          }
          if (cd.colTextSource.length()>0) {
            XMLBuffer.append("                <definition>"+cd.colTextSource+"</definition>\n");
          }
	        XMLBuffer.append("             </textDomain>\n");
	    }
	    XMLBuffer.append("        </attributeDomain>\n");
	    XMLBuffer.append("        <missingValueCode> </missingValueCode>\n");
	    XMLBuffer.append("        <precision> </precision>\n");
	    XMLBuffer.append("    </attribute>\n");
    }	  
	  XMLBuffer.append("</eml-attribute>\n");
	  return XMLBuffer.toString();
	}

	/**
	 * Hardcoded routine to create an XML Table Entity metadata string based on
	 * data
	 * ---BAD PRACTICE--- should use config to get info
	 */
	public String createXMLEntityString() {
	  StringBuffer XMLBuffer = new StringBuffer();
	  XMLBuffer.append("<?xml version=\"1.0\"?>\n");
	  XMLBuffer.append("<!DOCTYPE table-entity PUBLIC \"-//ecoinformatics.org//eml-entity-2.0.0beta6//EN\" \"eml-entity.dtd\">\n");
	  XMLBuffer.append("<table-entity>\n");
	  XMLBuffer.append("    <identifier> </identifier>\n");
	  XMLBuffer.append("    <entityName> "+normalize(TableNameTextField.getText())+"</entityName>\n");
	  XMLBuffer.append("    <entityDescription> "+normalize(TableDescriptionTextField.getText())+"</entityDescription>\n");
	  XMLBuffer.append("    <orientation columnorrow=\"columnmajor\"></orientation>\n");
	  XMLBuffer.append("    <caseSensitive yesorno=\"no\"></caseSensitive>\n");
	  String numRecords = (new Integer(nlines_actual - startingLine)).toString();
	  XMLBuffer.append("    <numberOfRecords> "+normalize(numRecords)+"</numberOfRecords>\n");
	  XMLBuffer.append("</table-entity>\n");
	  return XMLBuffer.toString();
	}

	/**
	 * Hardcoded routine to create an XML eml-physical metadata string based on
	 * data
	 * ---BAD PRACTICE--- should use config to get info
	 */
	public String createXMLPhysicalString() {
	  long filesize = (new File(filename)).length();
	  String filesizeString = (new Long(filesize)).toString();
	  String delimit = getDelimiterStringAsText();
	  StringBuffer XMLBuffer = new StringBuffer();
	  int numHeaderLines = startingLine;
	  if (!labelsInStartingLine) numHeaderLines = numHeaderLines-1;
	  
	  XMLBuffer.append("<?xml version=\"1.0\"?>\n");
	  XMLBuffer.append("<!DOCTYPE eml-physical PUBLIC \"-//ecoinformatics.org//eml-physical-2.0.0beta6//EN\" \"eml-physical.dtd\">\n");
	  XMLBuffer.append("<eml-physical>\n");
	  XMLBuffer.append("    <identifier> </identifier>\n");
      XMLBuffer.append("    <format> Text</format>\n");  // text import wizard only handles text 
      XMLBuffer.append("    <size unit=\"bytes\">"+filesizeString+"</size>\n");  
      XMLBuffer.append("    <numHeaderLines>"+numHeaderLines+"</numHeaderLines>\n");  
      XMLBuffer.append("    <recordDelimiter>"+"#x0A"+"</recordDelimiter>\n"); 
      XMLBuffer.append("    <fieldDelimiter>"+delimit+"</fieldDelimiter>\n"); 
      XMLBuffer.append("</eml-physical>\n");
	  return XMLBuffer.toString();
    }

	void CancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		this.setVisible(false);
		this.dispose();
    if(listener != null)
    {
      listener.importCanceled();
    }
	}
	
	
    /** Normalizes the given string. */
    private String normalize(Object ss) {
        String s = "";
        s = (String)ss;
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<': {
                    str.append("&lt;");
                    break;
                }
                case '>': {
                    str.append("&gt;");
                    break;
                }
                case '&': {
                    str.append("&amp;");
                    break;
                }
                case '"': {
                    str.append("&quot;");
                    break;
                }
                case '\r':
		case '\t':
                case '\n': {
                    if (false) {
                        str.append("&#");
                        str.append(Integer.toString(ch));
                        str.append(';');
                        break;
                    }
                    // else, default append char
			break;
                }
                default: {
                    str.append(ch);
                }
            }
        }

        return str.toString();

    } // normalize(String):String

	private String checkForBlankInfo() {
	    String res = null;  // return null if all fields have data
	    String temp = "";
	    for (int i=0;i<colDataInfo.size();i++) {
	        ColumnData cd = (ColumnData)colDataInfo.elementAt(i);
	        if (!cd.hasInfo()) {
	            temp = temp + "#" + (i+1) +" ";
	        }
	    }
	    if (temp.length()>0) res = temp;
	    return res;
    }

    
	



}
