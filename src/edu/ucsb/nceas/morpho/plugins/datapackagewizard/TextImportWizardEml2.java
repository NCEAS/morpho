/**
 *  '$RCSfile: TextImportWizardEml2.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-04-13 01:00:58 $'
 * '$Revision: 1.19 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.TextImportListener;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributePage;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.AttributeSettings;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages.ImportWizard;
import edu.ucsb.nceas.morpho.util.XMLUtil;

import edu.ucsb.nceas.utilities.OrderedMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.WindowAdapter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


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
public class TextImportWizardEml2 extends JFrame {

  /**
   * a global reference to the table used to display the data that is
   * being referenced by this text import process
   */
  private JTable table;

  /**
   * 	a global reference to the table used to display the lines read from the file
   *	this is the table that is displayed on the first screen of the TIW
   */
  private JTable linesTable = null;

  /**
   * flag indicating that user has returned to first screen using "back" button
   */

  private boolean hasReturnedFromScreen2;

  /**
   * flag indicating that multiple, sequential delimiters should be ignored
   * primarily useful with space delimiters
   */
  private boolean ignoreConsequtiveDelimiters = false;

  /**
   * actual number of lines in fdata file
   */
  private int nlines_actual;

  /**
   * number of parsed lines in file
   */
  private int nlines;

  /**
   * max number of lines to be parsed in file
   */
  private int nlines_max = 5000;

  /**
   * array of line strings
   */
  private String[] lines;

  private File dataFile;

  private String shortFilename;

  /**
   * flag used to avoid parsing everytime a checkbox is changed
   */
  private boolean parseOn = true;

  /**
   * step number in wizard
   **/
  private int stepNumber = 1;


  // starting line
  private int startingLine = 1;

  /**
   * flag indicating that labels for each column are contained in the
   * starting line of parsed data
   */
  private boolean labelsInStartingLine = false;

  /**
   * vector containing column Title strings
   */
  // contains column titles
  private Vector colTitles;

  /**
   * vector containing AttributePage objects
   */
  private Vector columnAttributes;
	
	/**
   * vector containing Orderedmaps of the AttributePage objects
   */
  private Vector columnMaps;

  private boolean[] needToSetPageData;

  /**
   * vector of vectors with table data
   */
  private Vector vec;

  private TextImportListener listener = null;

  // Column Model of the table containting all the columns
  private TableColumnModel fullColumnModel = null;

  //number types for interval/ratio number type

  private String[] numberTypesArray = new String[] {
                        "NATURAL (non-zero counting numbers: 1, 2, 3..)",
                        "WHOLE  (counting numbers & zero: 0, 1, 2, 3..)",
                        "INTEGER (+/- counting nums & zero: -2, -1, 0, 1..)",
                        "REAL  (+/- fractions & non-fractions: -1/2, 3.14..)"};

  private short distribution = WizardSettings.ONLINE;

  private WizardContainerFrame mainWizFrame;

  /**
   * constructor
   *
   * @param file the full path and filename of the data file to be imported or
   *   inspected
   * @param listener the <code>TextImportListener</code> object to be called
   *   back when import is complete
   */
  public TextImportWizardEml2(File file, TextImportListener listener, WizardContainerFrame container) {

    this.listener = listener;
    this.dataFile = file;
    this.shortFilename = dataFile.getName();
    this.mainWizFrame = container;

    setDistribution(distribution);

    initControls();

    registerListeners();
  }


  /**
   * Start the import. Returns true if file parsed OK, otherwise returns false
   *
   * @return boolean true if file parsed OK, otherwise returns false
   */
  public boolean startImport() {

    TableNameTextField.setText(shortFilename);

    if (parsefile(dataFile)) {

      createLinesTable();
      stepNumber = 1;
      hasReturnedFromScreen2 = false;
      StepNumberLabel.setText("Step #" + stepNumber);
      CardLayout cl = (CardLayout)ControlsPanel.getLayout();
      cl.show(ControlsPanel, "card" + stepNumber);
      BackButton.setEnabled(false);
      FinishButton.setEnabled(false);
      NextButton.setEnabled(true);
      setVisible(true);
      return true;
    }
    return false;
  }

  private SymAction lSymAction;

  private void initControls() {

    lSymAction = new SymAction();

    //{{INIT_CONTROLS
    setTitle("Text Import Wizard");
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout(0, 0));
    setSize(695, 500);
    setVisible(false);
    saveFileDialog.setMode(FileDialog.SAVE);
    saveFileDialog.setTitle("Save");
    openFileDialog.setMode(FileDialog.LOAD);
    openFileDialog.setTitle("Open");
    MainDisplayPanel.setLayout(new BorderLayout(0, 0));
    getContentPane().add(MainDisplayPanel, BorderLayout.CENTER);
    ControlsPlusDataPanel.setLayout(new GridLayout(2, 1, 0, 4));
    MainDisplayPanel.add(ColumnDataScrollPanel, BorderLayout.WEST);
    MainDisplayPanel.add(ControlsPlusDataPanel, BorderLayout.CENTER);

    ColumnDataScrollPanel.setPreferredSize(new Dimension(80, 4000));
    ColumnDataScrollPanel.setVisible(false);

    ControlsPanel.setLayout(new CardLayout(0, 0));
    ControlsPlusDataPanel.add(ControlsPanel);

    //-----------------------------------------------------
    Step1FullControlsPanel.setAlignmentY(0.0F);
    Step1FullControlsPanel.setAlignmentX(0.0F);
    Step1FullControlsPanel.setLayout(new BorderLayout());
    ControlsPanel.add("card1", Step1FullControlsPanel);


    JLabel Step1_titleLabel = new JLabel("New DataTable Wizard");
    Step1_titleLabel.setFont(WizardSettings.TITLE_FONT);
    Step1_titleLabel.setForeground(WizardSettings.TITLE_TEXT_COLOR);
    Step1_titleLabel.setBorder(new EmptyBorder(WizardSettings.PADDING,0,WizardSettings.PADDING,0));

    JPanel Step1_topPanel = new JPanel();
    Step1_topPanel.setLayout(new BorderLayout());
    Step1_topPanel.setPreferredSize(WizardSettings.TOP_PANEL_DIMS);
    Step1_topPanel.setMaximumSize(WizardSettings.TOP_PANEL_DIMS);
    Step1_topPanel.setBorder(new EmptyBorder(0,3*WizardSettings.PADDING,0,3*WizardSettings.PADDING));
    Step1_topPanel.setBackground(WizardSettings.TOP_PANEL_BG_COLOR);
    Step1_topPanel.setOpaque(true);
    Step1_topPanel.add(Step1_titleLabel, BorderLayout.CENTER);
    Step1FullControlsPanel.add(Step1_topPanel, BorderLayout.NORTH);

    Step1ControlsPanel.setLayout(new BoxLayout(Step1ControlsPanel, BoxLayout.Y_AXIS));

    Step1_TopTitlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    Step1ControlsPanel.add(Step1_TopTitlePanel);
    Step1_TopTitleLabel.setText("This set of screens will create metadata based on the content of the specified data file");
    Step1_TopTitlePanel.add(Step1_TopTitleLabel);
    Step1_TopTitleLabel.setForeground(java.awt.Color.black);
    Step1_TopTitleLabel.setFont(new Font("Dialog", Font.BOLD, 12));
    Step1_TableNamePanel.setLayout(new BoxLayout(Step1_TableNamePanel,
                                                 BoxLayout.X_AXIS));
    Step1ControlsPanel.add(Step1_TableNamePanel);
    Step1ControlsPanel.add(Box.createGlue());
    Step1_NameLabel.setText(" Table Name: ");
    Step1_NameLabel.setPreferredSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    Step1_NameLabel.setMinimumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    Step1_NameLabel.setMaximumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    Step1_TableNamePanel.add(Step1_NameLabel);
    Step1_NameLabel.setForeground(java.awt.Color.black);
    Step1_NameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    Step1_TableDescriptionLabel.setMaximumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    Step1_TableDescriptionLabel.setMinimumSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    Step1_TableDescriptionLabel.setPreferredSize(WizardSettings.
                                                 WIZARD_CONTENT_LABEL_DIMS);
    TableNameTextField.setPreferredSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    TableNameTextField.setMaximumSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    Step1_TableNamePanel.add(TableNameTextField);
    Step1_TableDescriptionPanel.setLayout(new BoxLayout(
        Step1_TableDescriptionPanel, BoxLayout.X_AXIS));
    Step1ControlsPanel.add(Step1_TableDescriptionPanel);
    Step1_TableDescriptionLabel.setText(" Description: ");
    Step1ControlsPanel.add(Box.createGlue());
    Step1_TableDescriptionPanel.add(Step1_TableDescriptionLabel);
    Step1_TableDescriptionLabel.setForeground(java.awt.Color.black);
    Step1_TableDescriptionLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    TableDescriptionTextField.setPreferredSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    TableDescriptionTextField.setMaximumSize(WizardSettings.WIZARD_CONTENT_SINGLE_LINE_DIMS);
    Step1_TableDescriptionPanel.add(TableDescriptionTextField);

    StartingLinePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
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
    ColumnLabelsCheckBox.setSelected(false);
    StartingLinePanel.add(ColumnLabelsCheckBox);
    ColumnLabelsCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));

    Step1FullControlsPanel.add(Step1ControlsPanel, BorderLayout.CENTER);

    //-------------------------------------------------------
    JLabel Step2_titleLabel = new JLabel("Text Import Wizard");
    Step2_titleLabel.setFont(WizardSettings.TITLE_FONT);
    Step2_titleLabel.setForeground(WizardSettings.TITLE_TEXT_COLOR);
    Step2_titleLabel.setBorder(new EmptyBorder(WizardSettings.PADDING,0,WizardSettings.PADDING,0));

    JPanel Step2_topPanel = new JPanel();
    Step2_topPanel.setLayout(new BorderLayout());
    Step2_topPanel.setPreferredSize(WizardSettings.TOP_PANEL_DIMS);
    Step2_topPanel.setMaximumSize(WizardSettings.TOP_PANEL_DIMS);
    Step2_topPanel.setBorder(new EmptyBorder(0,3*WizardSettings.PADDING,0,3*WizardSettings.PADDING));
    Step2_topPanel.setBackground(WizardSettings.TOP_PANEL_BG_COLOR);
    Step2_topPanel.setOpaque(true);
    Step2_topPanel.add(Step2_titleLabel, BorderLayout.CENTER);


    Step2FullControlsPanel.setLayout(new BorderLayout());
    Step2FullControlsPanel.add(Step2_topPanel, BorderLayout.NORTH);

    Step2ControlsPanel.setLayout(new BoxLayout(Step2ControlsPanel, BoxLayout.Y_AXIS));
    ControlsPanel.add("card2", Step2FullControlsPanel);
    Step2FullControlsPanel.setVisible(false);
    Step2_TopLabelPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    Step2ControlsPanel.add(Step2_TopLabelPanel);
    Step2_TopLabel.setText("If the columns indicated in the table are incorrect, try changing the assumed delimiter(s)");
    Step2_TopLabelPanel.add(Step2_TopLabel);
    Step2_TopLabel.setForeground(java.awt.Color.black);
    Step2_DelimiterChoicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
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
    JPanel10.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    //Step2ControlsPanel.add(JPanel10);
    JPanel11.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    //Step2ControlsPanel.add(JPanel11);
    Step2_ConsequtivePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    Step2ControlsPanel.add(Box.createGlue());
    Step2ControlsPanel.add(Step2_ConsequtivePanel);
    ConsecutiveCheckBox.setText("Treat consecutive delimiters as one");
    ConsecutiveCheckBox.setActionCommand("Treat consecutive delimiters as one");
    Step2_ConsequtivePanel.add(ConsecutiveCheckBox);
    ConsecutiveCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));

    Step2FullControlsPanel.add(Step2ControlsPanel, BorderLayout.CENTER);
    //---------------------------------

    ControlsPanel.add("card3", ColumnDataPanel);
    ColumnDataPanel.setLayout(new BorderLayout(0, 0));

    ColumnDataPanel.setVisible(false);

    //------------------------------------------------
    DataPanel.setLayout(new BorderLayout(0, 0));
    ControlsPlusDataPanel.add(DataPanel);
    DataPanel.add(BorderLayout.CENTER, DataScrollPanel);

    ((CardLayout)ControlsPanel.getLayout()).show(ControlsPanel, "card1");
    ButtonsPanel.setLayout(new BorderLayout(0, 0));
    getContentPane().add(BorderLayout.SOUTH, ButtonsPanel);
    JPanelLeft.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    ButtonsPanel.add(BorderLayout.WEST, JPanelLeft);

    JPanelCenter.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
    ButtonsPanel.add(BorderLayout.CENTER, JPanelCenter);
    StepNumberLabel.setText("Step #1");
    JPanelCenter.add(StepNumberLabel);
    StepNumberLabel.setForeground(java.awt.Color.black);

    CancelButton
      = WidgetFactory.makeJButton(WizardSettings.CANCEL_BUTTON_TEXT, lSymAction,
                                  WizardSettings.NAV_BUTTON_DIMS);
    JPanelCenter.add(CancelButton);
    BackButton
      = WidgetFactory.makeJButton(WizardSettings.PREV_BUTTON_TEXT, lSymAction,
                                  WizardSettings.NAV_BUTTON_DIMS);
    BackButton.setEnabled(true);
    JPanelCenter.add(BackButton);
    NextButton
      = WidgetFactory.makeJButton(WizardSettings.NEXT_BUTTON_TEXT, lSymAction,
                                  WizardSettings.NAV_BUTTON_DIMS);
    JPanelCenter.add(NextButton);
    FinishButton
      = WidgetFactory.makeJButton(WizardSettings.IMPORT_BUTTON_TEXT, lSymAction,
                                  WizardSettings.NAV_BUTTON_DIMS);
    FinishButton.setEnabled(false);
    JPanelCenter.add(FinishButton);
  }


  private void registerListeners() {
    //{{REGISTER_LISTENERS
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);

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
  ConsecutiveCheckBox.addItemListener(lSymItem);
  }

  private void setDistribution(short distribution) {

    if (distribution != WizardSettings.ONLINE
        && distribution != WizardSettings.INLINE
        && distribution != WizardSettings.OFFLINE
        && distribution != WizardSettings.NODATA) {

      throw new IllegalArgumentException(
          "distribution must be WizardSettings.[ONLINE | INLINE | OFFLINE | NODATA]");
    }
    this.distribution = distribution;
  }


  void exitApplication() {
    // passes string version of XML docs created by TextImportWizard
    // to a package wizard

    this.setVisible(false);
    this.dispose();
  }


  class UneditableTableModel extends DefaultTableModel {
    public UneditableTableModel(Vector data, Vector columnNames) {
      super(data, columnNames);
    }


    public boolean isCellEditable(int row, int col) {
      return false;
    }
  }

  class SymWindow extends java.awt.event.WindowAdapter {
    public void windowClosing(java.awt.event.WindowEvent event) {
      Object object = event.getSource();
      if (object == TextImportWizardEml2.this)
        TextImportWizard_windowClosing(event);
    }
  }

  void TextImportWizard_windowClosing(java.awt.event.WindowEvent event) {
    try {
      CancelButton_actionPerformed(null);
    } catch (Exception e) { e.printStackTrace(); }
  }


  public void resetColumnHeader(String newColHeader) {
    int selectedCol = table.getSelectedColumn();
    if ((selectedCol > -1) && (colTitles.size() > 0)) {
      colTitles.removeElementAt(selectedCol);
      colTitles.insertElementAt(newColHeader, selectedCol);
      buildTable();
      table.setColumnSelectionInterval(selectedCol, selectedCol);
    }
  }


  class SymAction implements java.awt.event.ActionListener {

    public void actionPerformed(java.awt.event.ActionEvent event) {
      Object object = event.getSource();
      if (object == NextButton)
        NextButton_actionPerformed(event);
      else if (object == BackButton)
        BackButton_actionPerformed(event);
      else if (object == FinishButton)
        FinishButton_actionPerformed(event);
      else if (object == CancelButton)
        CancelButton_actionPerformed(event);
      else if (object == StartingLineTextField)
        StartingLineTextField_actionPerformed(event);
    }
  }



  public static void main(String args[]) {
    File f = new File(args[0]);
    new TextImportWizardEml2(f, null, null);
  }


  /**
   * creates a JTable based on lines in input
   */
  void createLinesTable() {

    if(linesTable == null) {

      Vector listOfRows = new Vector();
      for (int i = 0; i < nlines; i++) {
        Vector row = new Vector();
        row.add(new String().valueOf(i + 1));
        row.add(lines[i]);
        listOfRows.add(row);
      }
      Vector title = new Vector();
      title.add("#");
      title.add("Lines in " + dataFile.getName());
      UneditableTableModel linesTM = new UneditableTableModel(listOfRows, title);
      linesTable = new JTable(linesTM);
      linesTable.setFont(new Font("MonoSpaced", Font.PLAIN, 14));
      (linesTable.getTableHeader()).setReorderingAllowed(false);

      TableColumn column = null;
      column = linesTable.getColumnModel().getColumn(0);
      column.setPreferredWidth(40);
      column.setMaxWidth(40);
    }
    DataScrollPanel.getViewport().removeAll();
    DataScrollPanel.getViewport().add(linesTable);

  }


  /**
   * parses data input file into an array of lines (Strings)
   *
   * @param f the file name
   * @return boolean true if parse was successful (textfile only); false if
   *   parse unsuccessful (non-text file)
   */
  private boolean parsefile(File f) {

    String temp = null;

    if (isTextFile(f)) {

      BufferedReader in = null;
      try {
        in = new BufferedReader(new FileReader(f));
      } catch (IOException e) {
        e.printStackTrace();
      }
      nlines = 0;
      nlines_actual = 0;

      List linesList = new ArrayList();
      try {
        while ((temp = in.readLine()) != null) {
          // do not count blank lines
          if (temp.length() > 0) {
            nlines_actual++;
            if (nlines < nlines_max) {
              nlines++;
              temp+="\n";
              linesList.add(temp);
            } /*else {
              // we can stop reading the remaining lines. we dont need the actual number of
              // lines present
              break;
            }*/
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try { in.close(); } catch (IOException e) { /* ignore */ }
      }

      if (nlines_actual > nlines_max) {
        JOptionPane.showMessageDialog(this,
          "Data File parsing has been truncated due to large size! (Note: NO data has been lost!)",
          "Message",
          JOptionPane.INFORMATION_MESSAGE, null);
      }
      //convert list to the "lines" array:
      lines = (String[])(linesList.toArray(new String[nlines]));
      guessDelimiter();

      return true;

    } else {

      JOptionPane.showMessageDialog(this, "Selected File is NOT a text file!",
                                    "Message",
                                    JOptionPane.INFORMATION_MESSAGE, null);
      CancelButton_actionPerformed(null);

      return false;
    }
  }


  private void parseDelimited() {

    if (lines != null) {
      int start = startingLine; // startingLine is 1-based not 0-based
      int numcols = 0; // init
      if (hasReturnedFromScreen2 && isScreen1Unchanged() && colTitles != null) {
        //don't redefine column headings etc - keep user's previous values,
        // since nothing has changed. In this case colTitles is already set:
        numcols = colTitles.size();
      } else {
        if (labelsInStartingLine) {
          colTitles = getColumnValues(lines[startingLine - 1]);
        } else {
          colTitles = getColumnValues(lines[startingLine - 1]); // use just to get # of cols
          int temp = colTitles.size();
          colTitles = new Vector();
          for (int l = 0; l < temp; l++) {
            colTitles.addElement("Column " + (l + 1));
          }
          start--; // include first line
        }
        vec = new Vector();
        Vector vec1;
        numcols = colTitles.size();
        for (int i = start; i < nlines; i++) {
          vec1 = getColumnValues(lines[i]);
          boolean missing = false;
          int currSize = vec1.size();
          while (currSize < numcols) {
            vec1.addElement("");
            currSize++;
            missing = true;
          }
          vec.addElement(vec1);
        }

        buildTable();
      }
      if(!hasReturnedFromScreen2) {

        columnAttributes = new Vector();
        needToSetPageData = new boolean[numcols];
        Arrays.fill(needToSetPageData, true);

      }
    }
    DataScrollPanel.getViewport().removeAll();
    DataScrollPanel.getViewport().add(table);
    hasReturnedFromScreen2 = false;
  }


  /**
   * builds JTable from input data
   */
  private void buildTable() {
    UneditableTableModel myTM = new UneditableTableModel(vec, colTitles);
    table = new JTable(myTM);

    table.setColumnSelectionAllowed(true);
    table.setRowSelectionAllowed(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    fullColumnModel = table.getColumnModel();

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
      while (st.hasMoreTokens()) {
        token = st.nextToken().trim();
        res.addElement(token);
      }
    } else {
      StringTokenizer st = new StringTokenizer(str, sDelim, true);
      while (st.hasMoreTokens()) {
        token = st.nextToken().trim();
        if (!inDelimiterList(token, sDelim)) {
          res.addElement(token);
        } else {
          if (inDelimiterList(oldToken, sDelim)) {
              //&& (inDelimiterList(token, sDelim))) {
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
    if (TabCheckBox.isSelected())str = str + "\t";
    if (CommaCheckBox.isSelected())str = str + ",";
    if (SpaceCheckBox.isSelected())str = str + " ";
    if (SemicolonCheckBox.isSelected())str = str + ";";
    if (OtherCheckBox.isSelected()) {
      String temp = OtherTextField.getText();
      if (temp.length() > 0) {
        temp = temp.substring(0, 1);
        str = str + temp;
      }
    }
    return str;
  }


  private String getDelimiterStringAsText() {
    String str = "";
    if (TabCheckBox.isSelected())str = str + "#x09";
    if (CommaCheckBox.isSelected())str = str + ",";
    if (SpaceCheckBox.isSelected())str = str + "#x20";
    if (SemicolonCheckBox.isSelected())str = str + ";";
    if (OtherCheckBox.isSelected()) {
      String temp = OtherTextField.getText();
      if (temp.length() > 0) {
        temp = temp.substring(0, 1);
        str = str + temp;
      }
    }
    return str;
  }


  private boolean inDelimiterList(String token, String delim) {
    boolean result = false;
    int test = delim.indexOf(token);
    if (test > -1) {
      result = true;
    }
    return result;
  }


  void NextButton_actionPerformed(java.awt.event.ActionEvent event) {
    if (stepNumber >= 3) {
      AttributePage attrd = (AttributePage)columnAttributes.elementAt(
          stepNumber - 3);
      if (!attrd.onAdvanceAction())
        return;
    }

    stepNumber++;

    if (fullColumnModel != null
        && stepNumber == (fullColumnModel.getColumnCount() + 2))
      FinishButton.setEnabled(true);
    if (stepNumber < 3)BackButton.setEnabled(true);

    if(fullColumnModel != null)
        StepNumberLabel.setText("Step #" + stepNumber + " of " + (fullColumnModel.getColumnCount() + 2));
    else
      StepNumberLabel.setText("Step #" + stepNumber);
    CardLayout cl = (CardLayout)ControlsPanel.getLayout();
    cl.show(ControlsPanel, "card" + stepNumber);

    if (stepNumber == 2) {
      parseDelimited();
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }
    if (stepNumber >= 3) {
      TableColumnModel model = new DefaultTableColumnModel();
      model.addColumn(fullColumnModel.getColumn(stepNumber - 3));
      DefaultListSelectionModel dlsm = new DefaultListSelectionModel();
      dlsm.setSelectionInterval(0, 0);
      model.setColumnSelectionAllowed(true);
      model.setSelectionModel(dlsm);
      table.setColumnModel(model);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      table.sizeColumnsToFit( -1);
      ColumnDataScrollPanel.getViewport().removeAll();
      ColumnDataScrollPanel.getViewport().add(table);
      ColumnDataScrollPanel.setVisible(true);
      DataPanel.setVisible(false);
      StepNumberLabel.setText("Step #" + stepNumber + " of "
                              + (fullColumnModel.getColumnCount() + 2));

      int attrNum = stepNumber - 3;
      if(attrNum >= columnAttributes.size()) {
        AttributePage ad = (AttributePage)WizardPageLibrary.getPage(
          DataPackageWizardInterface.ATTRIBUTE_PAGE);
        ad.setBorder(BorderFactory.createLineBorder(Color.black));
        columnAttributes.add(ad);
      }
      if(needToSetPageData[attrNum]) {
        fillAttributePageData(attrNum);
        needToSetPageData[attrNum] = false;
      }
      AttributePage attrd = (AttributePage)columnAttributes.elementAt(attrNum);
      MainDisplayPanel.remove(MainDisplayPanel.getComponent(1));
      MainDisplayPanel.add(attrd, BorderLayout.CENTER);
      attrd.refreshUI();
      MainDisplayPanel.validate();
      MainDisplayPanel.repaint();
      this.repaint();
    } else {
      ColumnDataPanel.setVisible(false);
    }

    if (stepNumber >= fullColumnModel.getColumnCount() + 2) {
      NextButton.setEnabled(false);
    } else {
      NextButton.setEnabled(true);
    }
  }

  private void fillAttributePageData(int attrNum) {

    AttributePage ad = (AttributePage)columnAttributes.elementAt(attrNum);
    String type = guessColFormat(attrNum);
    OrderedMap map = ad.getPageData(AttributeSettings.Attribute_xPath);
    map.put(AttributeSettings.AttributeName_xPath, colTitles.elementAt(attrNum));

    // either nominal/ordinal  . We guess as  nominal
    if (type.equals("text")) {

      Vector unique = getUniqueColValues(attrNum);
      Enumeration en = unique.elements();
      int pos = 1;
      while (en.hasMoreElements()) {
        String elem = (String)en.nextElement();
        String path = AttributeSettings.Nominal_xPath
        + "/enumeratedDomain[1]/codeDefinition[" + pos
        + "]/code";
        map.put(path, elem);
        path = AttributeSettings.Ordinal_xPath
        + "/enumeratedDomain[1]/codeDefinition[" + pos + "]/code";
        map.put(path, elem);
        pos++;
      }
      ad.setPageData(map, null);
    }

    else if (type.equals("float")) {
      String numberTypePath = AttributeSettings.Interval_xPath
      + "/numericDomain/numberType";
      map.put(numberTypePath, numberTypesArray[3]);
      numberTypePath = AttributeSettings.Ratio_xPath
      + "/numericDomain/numberType";
      map.put(numberTypePath, numberTypesArray[3]);
      ad.setPageData(map, null);
    }

    else if (type.equals("integer")) {

      String numType = guessNumberType(attrNum);

      if (numType.equals("Natural")) {
        String numberTypePath = AttributeSettings.Interval_xPath
        + "/numericDomain/numberType";
        map.put(numberTypePath, numberTypesArray[0]);
        numberTypePath = AttributeSettings.Ratio_xPath
        + "/numericDomain/numberType";
        map.put(numberTypePath, numberTypesArray[0]);
      } else if (numType.equals("Whole")) {
        String numberTypePath = AttributeSettings.Interval_xPath
        + "/numericDomain/numberType";
        map.put(numberTypePath, numberTypesArray[1]);
        numberTypePath = AttributeSettings.Ratio_xPath
        + "/numericDomain/numberType";
        map.put(numberTypePath, numberTypesArray[1]);
      } else if (numType.equals("Integer")) {
        String numberTypePath = AttributeSettings.Interval_xPath
        + "/numericDomain/numberType";
        map.put(numberTypePath, numberTypesArray[2]);
        numberTypePath = AttributeSettings.Ratio_xPath
        + "/numericDomain/numberType";
        map.put(numberTypePath, numberTypesArray[2]);
      }
      ad.setPageData(map, null);
    } else if (type.equals("date")) {
      map.put(AttributeSettings.DateTime_xPath + "/dateTimePrecision",
      new String("0"));
      ad.setPageData(map, null);
    }


  }


  void BackButton_actionPerformed(java.awt.event.ActionEvent event) {
    stepNumber--;
    if (stepNumber == 2) {
      ColumnDataScrollPanel.setVisible(false);
      table.setColumnModel(fullColumnModel);

      table.setColumnSelectionAllowed(true);
      table.setRowSelectionAllowed(false);
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      DataScrollPanel.getViewport().removeAll();
      DataScrollPanel.getViewport().add(table);
      DataPanel.setVisible(true);
      MainDisplayPanel.remove(MainDisplayPanel.getComponent(1));
      MainDisplayPanel.add(ControlsPlusDataPanel, BorderLayout.CENTER);
      MainDisplayPanel.validate();
      MainDisplayPanel.repaint();

    }
    if (stepNumber < 2){
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      saveScreen1Settings();
      hasReturnedFromScreen2 = true;
    }
    if (stepNumber >= 3) {
      TableColumnModel model = new DefaultTableColumnModel();
      model.addColumn(fullColumnModel.getColumn(stepNumber - 3));
      DefaultListSelectionModel dlsm = new DefaultListSelectionModel();
      dlsm.setSelectionInterval(0, 0);
      model.setColumnSelectionAllowed(true);
      model.setSelectionModel(dlsm);
      table.setColumnModel(model);

      ColumnDataScrollPanel.getViewport().removeAll();
      ColumnDataScrollPanel.getViewport().add(table);
      ColumnDataScrollPanel.setVisible(true);
      DataPanel.setVisible(false);

      AttributePage attrd = (AttributePage)columnAttributes.elementAt(
          stepNumber - 3);
      MainDisplayPanel.remove(MainDisplayPanel.getComponent(1));
      MainDisplayPanel.add(attrd, BorderLayout.CENTER);
      attrd.refreshUI();
      MainDisplayPanel.validate();
      MainDisplayPanel.repaint();
      this.repaint();
      //table.setColumnSelectionInterval(stepNumber-3,stepNumber-3);
    } else {
      ColumnDataPanel.setVisible(false);
    }

    if (fullColumnModel != null
        && stepNumber < (fullColumnModel.getColumnCount() + 2))
      FinishButton.setEnabled(false);
    if (stepNumber > 1) {
      NextButton.setEnabled(true);
    } else {
      BackButton.setEnabled(false);
    }
    StepNumberLabel.setText("Step #" + stepNumber + " of "
                            + (fullColumnModel.getColumnCount() + 2));

    CardLayout cl = (CardLayout)ControlsPanel.getLayout();
    cl.show(ControlsPanel, "card" + stepNumber);
    if (stepNumber == 1) {
      if (lines != null) {
        createLinesTable();
      }
    }
  }


  void FinishButton_actionPerformed(java.awt.event.ActionEvent event) {
    AttributePage ad = (AttributePage)columnAttributes.elementAt(
        columnAttributes.size() - 1);

    // info should be null if all fields are not blank
    if (!ad.onAdvanceAction()) return;

    int attrsToBeImported = mainWizFrame.getAttributeImportCount();

    Iterator it = columnAttributes.iterator();
		int index = 1;
    boolean importNeeded = false;

    int currentEntityID;
    String entityName = TableNameTextField.getText();
    List colNames = new ArrayList();
		columnMaps = new Vector();
		String prefix = AttributeSettings.Attribute_xPath;
    while(it.hasNext()) {
      ad = (AttributePage) it.next();
      OrderedMap map1 = ad.getPageData(prefix + "[" + index + "]");
			columnMaps.add(map1);
      String colName = getColumnName(map1, prefix + "[" + index + "]");
			
			if(ad.isImportNeeded()) {

        String mScale = getMeasurementScale(map1, prefix + "[" + index + "]");
        mainWizFrame.addAttributeForImport(entityName, colName, mScale, map1, prefix + "[" + index + "]", true);
        importNeeded = true;
      }
			index++;
      colNames.add(colName);
      
    }

    mainWizFrame.setLastImportedEntity(entityName);
    mainWizFrame.setLastImportedAttributes(colNames);
    if(vec != null)
      mainWizFrame.setLastImportedDataSet(vec);
    else {
      mainWizFrame.setLastImportedDataSet(((UneditableTableModel)table.getModel()).getDataVector());
    }

    String prevPageID = mainWizFrame.getPreviousPageID();

    if(attrsToBeImported > 0) {
      if(listener != null)
        ((ImportWizard)listener).nextPageID = DataPackageWizardInterface.CODE_DEFINITION;
    } else if(importNeeded) {
      if(listener != null)
      ((ImportWizard)listener).nextPageID=DataPackageWizardInterface.CODE_IMPORT_SUMMARY;
    } else {
      if(listener != null)
        ((ImportWizard)listener).nextPageID = DataPackageWizardInterface.SUMMARY;
    }


    if (listener != null) listener.importComplete(createEml2NVPairs());

    this.dispose();
  }

  private String getColumnName(OrderedMap map, String xPath) {

    Object o1 = map.get(xPath + "/attributeName");
    if(o1 == null) return "";
    else return (String) o1;
  }

  private String getMeasurementScale(OrderedMap map, String xPath) {

    Object o1 = map.get(xPath + "/measurementScale/nominal/nonNumericDomain/enumeratedDomain[1]/codeDefinition[1]/code");
    if(o1 != null) return "Nominal";
    boolean b1 = map.containsKey(xPath + "/measurementScale/nominal/nonNumericDomain/enumeratedDomain[1]/entityCodeList/entityReference");
    if(b1) return "Nominal";
    o1 = map.get(xPath + "/measurementScale/nominal/nonNumericDomain/textDomain[1]/definition");
    if(o1 != null) return "Nominal";

    o1 = map.get(xPath + "/measurementScale/ordinal/nonNumericDomain/enumeratedDomain[1]/codeDefinition[1]/code");
    if(o1 != null) return "Ordinal";
    b1 = map.containsKey(xPath + "/measurementScale/ordinal/nonNumericDomain/enumeratedDomain[1]/entityCodeList/entityReference");
    if(b1) return "Ordinal";
    o1 = map.get(xPath + "/measurementScale/ordinal/nonNumericDomain/textDomain[1]/definition");
    if(o1 != null) return "Ordinal";

    o1 = map.get(xPath + "/measurementScale/interval/unit/standardUnit");
    if(o1 != null) return "Interval";
    o1 = map.get(xPath + "/measurementScale/ratio/unit/standardUnit");
    if(o1 != null) return "Ratio";

    o1 = map.get(xPath + "/measurementScale/datetime/formatString");
    if(o1 != null) return "Datetime";

    return "";
  }


  /*
   *    Called if user is returning to screen 1 from screen 2 -
   *    Saves the screen 1 settings so we can tell if anything has been changed
   *    when user hits "next" again
   */

  String[] screen1Settings = new String[2];

  private final int IMPORT_START_ROW = 0;

  private final int LABELS_IN_START_ROW = 1;

  private void saveScreen1Settings() {

    screen1Settings[IMPORT_START_ROW] = StartingLineTextField.getText();
    screen1Settings[LABELS_IN_START_ROW] = (ColumnLabelsCheckBox.isSelected()) ?
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

  private boolean isScreen1Unchanged() {

    labelsInStartLine_Status = (ColumnLabelsCheckBox.isSelected()) ? "true"
                               : "false";

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
  private int charCount(String inS, String subS) {
    int cnt = -1;
    int pos = 0;
    int pos1 = 0;
    while (pos > -1) {
      pos1 = inS.indexOf(subS, pos + 1);
      pos = pos1;
      cnt++;
    }
    if (cnt < 0)cnt = 0;
    return cnt;
  }


  /**
   * return most frequent number of occurances of indicated substring
   *
   * @param subS delimiter substring
   * @return int
   */
  private int mostFrequent(String subS) {
    int maxcnt = 500; // arbitrary limit of 500 occurances
    int[] freq = new int[maxcnt];
    int mostfreq = 0;
    int mostfreqindex = 0;

    for (int i = 0; i < nlines; i++) {
      int cnt = charCount(lines[i], subS);
      if (cnt > maxcnt - 1)cnt = maxcnt - 1;
      freq[cnt]++;
      if(freq[cnt] > mostfreq) {
        mostfreq = freq[cnt];
        mostfreqindex = cnt;
      }
    }

    int tot = nlines;

    /*for (int j = 0; j < maxcnt; j++) {
      tot = tot + freq[j];
      if (freq[j] > mostfreq) {
        mostfreq = freq[j];
        mostfreqindex = j;
      }
    }*/
    // establish a threshold; if less than, then return 0
    if ((100 * mostfreq / tot) < 80) {
      mostfreqindex = 0;
    }

    return mostfreqindex;
  }


  /**
   * guesses a delimiter based on frequency of appearance of common delimites
   *
   * @return String
   */
  private String guessDelimiter() {
    parseOn = false;
    TabCheckBox.setSelected(false);
    CommaCheckBox.setSelected(false);
    SpaceCheckBox.setSelected(false);
    SemicolonCheckBox.setSelected(false);
    OtherCheckBox.setSelected(false);
    if (mostFrequent("\t") > 0) {
      TabCheckBox.setSelected(true);
      parseOn = true;
      return "tab";
    } else if (mostFrequent(",") > 0) {
      CommaCheckBox.setSelected(true);
      parseOn = true;
      return "comma";
    } else if (mostFrequent(" ") > 0) {
      SpaceCheckBox.setSelected(true);
      parseOn = true;
      return "space";
    } else if (mostFrequent(";") > 0) {
      SemicolonCheckBox.setSelected(true);
      parseOn = true;
      return "semicolon";
    } else if (mostFrequent(":") > 0) {
      SpaceCheckBox.setSelected(true);
      OtherCheckBox.setSelected(true);
      OtherTextField.setText(":");
      parseOn = true;
      return "colon";
    } else {
      SpaceCheckBox.setSelected(true);
      parseOn = true;
      return "unknown";
    }
  }


  //000000000000000000000000000000000



  // ----------------------------------------------
  // routines for checking content of columns

  boolean isInteger(String s) {
    boolean res = true;
    try {
      int III = Integer.parseInt(s);
    } catch (Exception w) {
      res = false;
    }
    return res;
  }


  boolean isDouble(String s) {
    boolean res = true;
    try {
      Double III = Double.valueOf(s);
    } catch (Exception w) {
      res = false;
    }
    return res;
  }


  boolean isDate(String s) {
    DateFormat dateFormat;
    Date dt;
    dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    boolean res = true;
    try {
      dt = dateFormat.parse(s);
    } catch (Exception w) {
      try {
        dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        dt = dateFormat.parse(s);
      } catch (Exception w1) {
        try {
          dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
          dt = dateFormat.parse(s);
        } catch (Exception w2) {
          res = false;
        }
      }
    }
    return res;
  }


  /**
   * guesses column type based on frequency of content types include text,
   * integer, floating point number, and date. Guess is based on frequency of
   * occurance
   *
   * @param colNum column number
   * @return String
   */
  String guessColFormat(int colNum) {
    int minInt = 0;
    int maxInt = 0;
    double minDouble = Double.POSITIVE_INFINITY;
    double maxDouble = Double.NEGATIVE_INFINITY;
    int emptyCount = 0;
    int dateCount = 0;
    int numericCount = 0;
    double numericAverage = 0.0;
    double numericSum = 0.0;
    boolean doublePresent = false;
    for (int i = 0; i < vec.size(); i++) {
      Vector v = (Vector)vec.elementAt(i);
      String str = (String)v.elementAt(colNum);
      if (str.trim().length() < 1) {
        emptyCount++;
      } else {
        boolean isInt = isInteger(str);
        boolean isDbl = isDouble(str);
        if (isInt || isDbl) {
          numericCount++;
          if (isDbl)
            doublePresent = true;
          double d = Double.parseDouble(str);
          minDouble = Math.min(minDouble, d);
          maxDouble = Math.max(maxDouble, d);
          numericSum += d;
        }

        if (isDate(str)) {
          dateCount++;
        }
      }
    }
    numericAverage = numericSum / numericCount;
    if ((numericCount > 0)
        && ((100 * (numericCount + emptyCount) / vec.size())) > 90) {
      if (doublePresent)return "float";
      else return "integer";
    }

    else if ((dateCount > 0)
             && ((100 * (dateCount + emptyCount) / vec.size())) > 90) {
      return "date";
    }
    return "text";
  }


  private String guessNumberType(int k) {
    Vector v = getUniqueColValues(k);
    Enumeration e = v.elements();
    boolean zero = false;
    boolean neg = false;
    while (e.hasMoreElements()) {
      String s = (String)e.nextElement();
      try {
        int num = Integer.parseInt(s);
        if (num == 0)zero = true;
        if (num < 0)neg = true;
      } catch (Exception ex) {
        return "";
      }
    }
    if (neg)return "Integer";
    if (zero)return "Whole";
    return "Natural";
  }


  /**
   * creates a Vector containing all the unique items (Strings) in the column
   *
   * @param colNum int
   * @return Vector
   */
  Vector getUniqueColValues(int colNum) {
    Vector res = new Vector();
    for (int i = 0; i < vec.size(); i++) {
      Vector v = (Vector)vec.elementAt(i);
      String str = (String)v.elementAt(colNum);
      if (!str.equals("")) { // ignore empty strings
        if (!res.contains(str)) {
          res.addElement(str);
        }
      }
    }
    return res;
  }


  /**
   * attempts to check to see if a file is just text or is binary. Reads bytes
   * in file and looks for '0'. If any '0's are found, assumed that the file is
   * NOT a text file.
   *
   * @param file the File to be checked
   * @return boolean true if it's a text file, false if not
   */
  private boolean isTextFile(File file) {
     boolean text = true;
     int res;
     int cnt = 0;
     int maxcnt = 2000; // only check this many bytes to avoid performance problems
     FileInputStream in = null;
     try {
       in = new FileInputStream(file);
       while (((res = in.read()) > -1) && (cnt < maxcnt)) {
         cnt++;
         if (res == 0) {
           text = false;
           break;
         }
       }
       in.close();
     } catch (Exception e) { e.printStackTrace(); }
     finally {
       try { in.close(); }
       catch (IOException e) {}
     }
     return text;
   }


  class SymListSelection implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent event) {
      Object object = event.getSource();
    }
  }

  void StartingLineTextField_actionPerformed(java.awt.event.ActionEvent event) {
    String str = StartingLineTextField.getText();
    if (isInteger(str)) {
      startingLine = (Integer.valueOf(str)).intValue();
      // startingLine is assumed to be 1-based
      if (startingLine < 1) {
        startingLine = 1;
        StartingLineTextField.setText("1");
      }
    } else {
      StartingLineTextField.setText(String.valueOf(startingLine));
    }
  }


  class SymFocus extends java.awt.event.FocusAdapter {
    public void focusLost(java.awt.event.FocusEvent event) {
      Object object = event.getSource();
      if (object == StartingLineTextField)
        StartingLineTextField_focusLost(event);
    }
  }

  void StartingLineTextField_focusLost(java.awt.event.FocusEvent event) {
    String str = StartingLineTextField.getText();
    if (isInteger(str)) {
      startingLine = (Integer.valueOf(str)).intValue();
      // startingLine is assumed to be 1-based
      if (startingLine < 1) {
        startingLine = 1;
        StartingLineTextField.setText("1");
      }
    } else {
      StartingLineTextField.setText(String.valueOf(startingLine));
    }
  }


  class SymItem implements java.awt.event.ItemListener {
    public void itemStateChanged(java.awt.event.ItemEvent event) {
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
      else if (object == ConsecutiveCheckBox)
        ConsecutiveCheckBox_itemStateChanged(event);
    }
  }

  void ColumnLabelsCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
    labelsInStartingLine = ColumnLabelsCheckBox.isSelected();
  }


  void TabCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
    if (parseOn) {
      parseDelimited();
    }
  }


  void CommaCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
    if (parseOn) {
      parseDelimited();

    }
  }


  void SpaceCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
    if (parseOn) {
      parseDelimited();
    }
  }


  void SemicolonCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
    if (parseOn) {
      parseDelimited();
    }
  }


  void OtherCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
    if (parseOn) {
      parseDelimited();
    }
  }

  void ConsecutiveCheckBox_itemStateChanged(java.awt.event.ItemEvent event) {
    if (parseOn) {
      parseDelimited();

    }
  }

  /**
   * Create a set (OrderedMap) of Name/Value pairs for eml2 corresponding to the
   * entity/attribute/physical parts of the eml2 tree created by the
   * TextImportWizard. Note: The TextImportWizard creates only text based
   * 'dataTable' entities
   *
   * @return OrderedMap
   */
  public OrderedMap createEml2NVPairs() {
    String header = "/eml:eml/dataset/dataTable/";
    OrderedMap om = new OrderedMap();
    om.put(header + "entityName", XMLUtil.normalize(TableNameTextField.getText()));
    om.put(header + "entityDescription",
           XMLUtil.normalize(TableDescriptionTextField.getText()));
    // physical NV pairs are inserted here
    om.put(header + "physical/objectName", shortFilename);
    long filesize = dataFile.length();
    String filesizeString = (new Long(filesize)).toString();
    om.put(header + "physical/size", filesizeString);
    om.put(header + "physical/size/@unit", "byte");
    int numHeaderLines = startingLine;
    if (!labelsInStartingLine)numHeaderLines = numHeaderLines - 1;
    om.put(header + "physical/dataFormat/textFormat/numHeaderLines",
           "" + numHeaderLines);
    om.put(header + "physical/dataFormat/textFormat/recordDelimiter",
           "#x0A");
    om.put(header + "physical/dataFormat/textFormat/attributeOrientation",
           "column");
    String delimit = getDelimiterStringAsText();
    om.put(header
           + "physical/dataFormat/textFormat/simpleDelimited/fieldDelimiter",
           delimit);
    Enumeration e = columnMaps.elements();
    int index = 1;
    while (e.hasMoreElements()) {
      OrderedMap map = (OrderedMap) e.nextElement();
      om.putAll(map);
    }

    int temp = 0;
    if (labelsInStartingLine)temp = 1;
    int numrecs = nlines_actual - startingLine + 1 + temp;
    String numRecords = (new Integer(numrecs)).toString();
    om.put(header + "numberOfRecords", XMLUtil.normalize(numRecords));

    return om;
  }




  void CancelButton_actionPerformed(java.awt.event.ActionEvent event) {

    this.setVisible(false);
    this.dispose();
    if (listener != null) {
      listener.importCanceled();
    }
  }


  private FileDialog saveFileDialog = new FileDialog(this);
  private FileDialog openFileDialog = new FileDialog(this);
  private JPanel MainDisplayPanel = new JPanel();
  private JPanel ControlsPlusDataPanel = new JPanel();
  private JPanel ControlsPanel = new JPanel();
  private JPanel Step1FullControlsPanel = new JPanel();
  private JPanel Step1ControlsPanel = new JPanel();
  private JPanel Step1_TopTitlePanel = new JPanel();
  private JLabel Step1_TopTitleLabel = new JLabel();
  private JPanel Step1_TableNamePanel = new JPanel();
  private JLabel Step1_NameLabel = new JLabel();
  private JTextField TableNameTextField = new JTextField();
  private JPanel Step1_TableDescriptionPanel = new JPanel();
  private JLabel Step1_TableDescriptionLabel = new JLabel();
  private JTextField TableDescriptionTextField = new JTextField();
  private JPanel StartingLinePanel = new JPanel();
  private JLabel StartingLineLabel = new JLabel();
  private JTextField StartingLineTextField = new JTextField();
  private JLabel ColumnLabelsLabel = new JLabel();
  private JCheckBox ColumnLabelsCheckBox = new JCheckBox();
  private JPanel Step2FullControlsPanel = new JPanel();
  private JPanel Step2ControlsPanel = new JPanel();
  private JPanel Step2_TopLabelPanel = new JPanel();
  private JLabel Step2_TopLabel = new JLabel();
  private JPanel Step2_DelimiterChoicePanel = new JPanel();
  private JLabel Step2_DelimterChoiceLabel = new JLabel();
  private JCheckBox TabCheckBox = new JCheckBox();
  private JCheckBox CommaCheckBox = new JCheckBox();
  private JCheckBox SpaceCheckBox = new JCheckBox();
  private JCheckBox SemicolonCheckBox = new JCheckBox();
  private JCheckBox OtherCheckBox = new JCheckBox();
  private JTextField OtherTextField = new JTextField();
  private JPanel JPanel10 = new JPanel();
  private JPanel JPanel11 = new JPanel();
  private JPanel Step2_ConsequtivePanel = new JPanel();
  private JCheckBox ConsecutiveCheckBox = new JCheckBox();
  private JPanel DataPanel = new JPanel();
  private JScrollPane DataScrollPanel = new JScrollPane();
  private JScrollPane ColumnDataScrollPanel = new JScrollPane();
  private JPanel ColumnDataPanel = new JPanel();
  private JPanel ButtonsPanel = new JPanel();
  private JPanel JPanelLeft = new JPanel();
  private JPanel JPanelCenter = new JPanel();
  private JLabel StepNumberLabel = new JLabel();
  private JButton CancelButton;
  private JButton BackButton;
  private JButton NextButton;
  private JButton FinishButton;
}
