/**
 *  '$RCSfile: TextImportWizardEml2.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-12-21 06:08:12 $'
 * '$Revision: 1.4 $'
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
import edu.ucsb.nceas.morpho.util.XMLUtil;
import edu.ucsb.nceas.utilities.OrderedMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

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
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
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
   * flag indicating that user has returned to first screen using "back" button
   */

  private boolean hasReturnedFromScreen2;

  /**
   * flag indicating that multiple, sequential delimiters should be ignored
   * primarily useful with space delimiters
   */
  private boolean ignoreConsequtiveDelimiters = false;

  /**
   * used as a flag to indicate whether 'blank' fields should be reported
   * as errors; if false, they are not reported
   */
  private boolean blankCheckFlag = false;

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

  /**
   * StringBuffer used to store results from various parts of the wizard
   */
  private StringBuffer resultsBuffer;

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
   * vector of vectors with table data
   */
  private Vector vec;

  private TextImportListener listener = null;

  // Column Model of the table containting all the columns
  private TableColumnModel fullColumnModel;

  //number types for interval/ratio number type

  private String[] numberTypesArray = new String[] {
                        "NATURAL (non-zero counting numbers: 1, 2, 3..)",
                        "WHOLE  (counting numbers & zero: 0, 1, 2, 3..)",
                        "INTEGER (+/- counting nums & zero: -2, -1, 0, 1..)",
                        "REAL  (+/- fractions & non-fractions: -1/2, 3.14..)"};

  private short distribution = WizardSettings.ONLINE;

  /**
   * constructor
   *
   * @param file the full path and filename of the data file to be imported or
   *   inspected
   * @param listener the <code>TextImportListener</code> object to be called
   *   back when import is complete
   */
  public TextImportWizardEml2(File file, TextImportListener listener) {

    this.listener = listener;
    this.dataFile = file;

    setDistribution(distribution);

    initControls();

    registerListeners();

    resultsBuffer = new StringBuffer();

    //assign the filename and get the wizard started.
    if (file != null) {
      shortFilename = file.getName();
      startImport(shortFilename);
    }
  }


  private void initControls() {
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
    //$$ openFileDialog.move(0,336);
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
    Step1ControlsPanel.setAlignmentY(0.0F);
    Step1ControlsPanel.setAlignmentX(0.0F);
    Step1ControlsPanel.setLayout(new GridLayout(7, 1, 0, 10));
    ControlsPanel.add("card1", Step1ControlsPanel);
    Step1_TopTitlePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    Step1ControlsPanel.add(Step1_TopTitlePanel);
    Step1_TopTitleLabel.setText("This set of screens will create metadata based on the content of the specified data file");
    Step1_TopTitlePanel.add(Step1_TopTitleLabel);
    Step1_TopTitleLabel.setForeground(java.awt.Color.black);
    Step1_TopTitleLabel.setFont(new Font("Dialog", Font.BOLD, 12));
    Step1_TableNamePanel.setAlignmentY(0.473684F);
    Step1_TableNamePanel.setAlignmentX(0.0F);
    Step1_TableNamePanel.setLayout(new BoxLayout(Step1_TableNamePanel,
                                                 BoxLayout.X_AXIS));
    Step1ControlsPanel.add(Step1_TableNamePanel);
    Step1_NameLabel.setText(" Table Name: ");
    Step1_NameLabel.setPreferredSize(WizardSettings.WIZARD_CONTENT_LABEL_DIMS);
    Step1_TableNamePanel.add(Step1_NameLabel);
    Step1_NameLabel.setForeground(java.awt.Color.black);
    Step1_NameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    Step1_TableDescriptionLabel.setPreferredSize(WizardSettings.
                                                 WIZARD_CONTENT_LABEL_DIMS);
    Step1_TableNamePanel.add(TableNameTextField);
    Step1_TableDescriptionPanel.setAlignmentY(0.473684F);
    Step1_TableDescriptionPanel.setAlignmentX(0.0F);
    Step1_TableDescriptionPanel.setLayout(new BoxLayout(
        Step1_TableDescriptionPanel, BoxLayout.X_AXIS));
    Step1ControlsPanel.add(Step1_TableDescriptionPanel);
    Step1_TableDescriptionLabel.setText(" Description: ");
    Step1_TableDescriptionPanel.add(Step1_TableDescriptionLabel);
    Step1_TableDescriptionLabel.setForeground(java.awt.Color.black);
    Step1_TableDescriptionLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    Step1_TableDescriptionPanel.add(TableDescriptionTextField);
    Step1_DelimiterChoicePanel.setAlignmentX(0.0F);
    Step1_DelimiterChoicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    Step1ControlsPanel.add(Step1_DelimiterChoicePanel);
    Step1_DelimiterLabel.setText(
        "Choose the method used to separate fields on each line of your data");
    Step1_DelimiterLabel.setAlignmentY(0.0F);
    Step1_DelimiterChoicePanel.add(Step1_DelimiterLabel);
    Step1_DelimiterLabel.setForeground(new java.awt.Color(102, 102, 153));
    Step1_DelimiterLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    Step1_DelimeterRadioPanel.setAlignmentX(0.0F);
    Step1_DelimeterRadioPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    Step1ControlsPanel.add(Step1_DelimeterRadioPanel);
    DelimitedRadioButton.setHorizontalTextPosition(SwingConstants.
                                                   RIGHT);
    DelimitedRadioButton.setText(
        "Delimited  -  Characters such as tabs or commas separate each data field");
    DelimitedRadioButton.setActionCommand(
        "Delimited  -  Characters such as tabs or commas separate each data field");
    DelimitedRadioButton.setAlignmentY(0.0F);
    DelimitedRadioButton.setSelected(true);
    Step1_DelimeterRadioPanel.add(DelimitedRadioButton);
    DelimitedRadioButton.setFont(new Font("Dialog", Font.PLAIN, 12));
    Step1_FixedFieldRadioPanel.setAlignmentX(0.0F);
    Step1_FixedFieldRadioPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    Step1ControlsPanel.add(Step1_FixedFieldRadioPanel);
    FixedFieldRadioButton.setText("Fixed Width  -  Fields are aligned in columns with specified number of characters");
    FixedFieldRadioButton.setActionCommand("Fixed Width  -  Fields are aligned in columns with specified number of characters");
    FixedFieldRadioButton.setAlignmentY(0.0F);
    FixedFieldRadioButton.setEnabled(false);
    Step1_FixedFieldRadioPanel.add(FixedFieldRadioButton);
    FixedFieldRadioButton.setFont(new Font("Dialog", Font.PLAIN, 12));
    StartingLinePanel.setAlignmentX(0.0F);
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

    //-------------------------------------------------------
    Step2ControlsPanel.setLayout(new GridLayout(6, 1, 0, 0));
    ControlsPanel.add("card2", Step2ControlsPanel);
    Step2ControlsPanel.setVisible(false);
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
    Step2ControlsPanel.add(JPanel10);
    JPanel11.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    Step2ControlsPanel.add(JPanel11);
    Step2_ConsequtivePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    Step2ControlsPanel.add(Step2_ConsequtivePanel);
    ConsecutiveCheckBox.setText("Treat consecutive delimiters as one");
    ConsecutiveCheckBox.setActionCommand("Treat consecutive delimiters as one");
    Step2_ConsequtivePanel.add(ConsecutiveCheckBox);
    ConsecutiveCheckBox.setFont(new Font("Dialog", Font.PLAIN, 12));

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
  }


  private void registerListeners() {
    //{{REGISTER_LISTENERS
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);
    SymAction lSymAction = new SymAction();
    NextButton.addActionListener(lSymAction);
    BackButton.addActionListener(lSymAction);
    FinishButton.addActionListener(lSymAction);
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
    //}}
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
    } catch (Exception e) {}
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

  public void startImport(String file) {
    TableNameTextField.setText(file);
    parsefile(file);
    createLinesTable();
    resultsBuffer = new StringBuffer();
    stepNumber = 1;
    hasReturnedFromScreen2 = false;
    StepNumberLabel.setText("Step #" + stepNumber);
    CardLayout cl = (CardLayout)ControlsPanel.getLayout();
    cl.show(ControlsPanel, "card" + stepNumber);
    BackButton.setEnabled(false);
    FinishButton.setEnabled(false);
    NextButton.setEnabled(true);
    setVisible(true);
  }


  public static void main(String args[]) {
    new TextImportWizardEml2(new File(args[0]), null).startImport(args[0]);
  }


  /**
   * creates a JTable based on lines in input
   */
  void createLinesTable() {
    Vector vec = new Vector();
    for (int i = 0; i < nlines; i++) {
      Vector vec1 = new Vector();
      vec1.addElement(new String().valueOf(i + 1));
      vec1.addElement(lines[i]);
      vec.addElement(vec1);
    }
    Vector title = new Vector();
    title.addElement("#");
    title.addElement("Lines in " + dataFile.getName());
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
  private void parsefile(String f) {
    int i;
    String temp;

    if (isTextFile(f)) {
      resultsBuffer = new StringBuffer("");
      resultsBuffer.append(f + " is apparently a text file\n");
      try {
        BufferedReader in = new BufferedReader(new FileReader(f));
        nlines = 0;
        try {
          while ((temp = in.readLine()) != null) {
            if (temp.length() > 0) { // do not count blank lines
              nlines++;
            }
          }
          in.close();
        } catch (IOException e) {}
        ;
      } catch (IOException e) {}
      ;

      nlines_actual = nlines;
      if (nlines > nlines_max) {
        nlines = nlines_max;
        JOptionPane.showMessageDialog(this, "Data File parsing has been truncated due to large size! (Note: NO data has been lost!)",
                                      "Message",
                                      JOptionPane.INFORMATION_MESSAGE, null);
      }

      lines = new String[nlines];
      // now read again since we know how many lines
      try {
        BufferedReader in1 = new BufferedReader(new FileReader(f));
        try {
          for (i = 0; i < nlines; i++) {
            temp = in1.readLine();
            while (temp.length() == 0) {temp = in1.readLine();
            }
            lines[i] = temp + "\n";
          }
          in1.close();
        } catch (IOException e) {}
        ;
      } catch (IOException e) {}
      ;
      resultsBuffer.append("Number of lines: " + nlines + "\n");
      resultsBuffer.append("Most probable delimiter is " + guessDelimiter()
                           + "\n");
    } else {
      resultsBuffer = new StringBuffer("");
      resultsBuffer.append(f + " is NOT a text file\n");
      JOptionPane.showMessageDialog(this, "Selected File is NOT a text file!",
                                    "Message", JOptionPane.INFORMATION_MESSAGE, null);

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
        Vector vec1 = new Vector();
        numcols = colTitles.size();
        resultsBuffer.append("Number of columns assumed: " + numcols + "\n");
        for (int i = start; i < nlines; i++) {
          vec1 = getColumnValues(lines[i]);
          boolean missing = false;
          while (vec1.size() < numcols) {
            vec1.addElement("");
            missing = true;
          }
          if (missing) {
            resultsBuffer.append("Insufficient number of items in row "
                                 + (i + 1) + "\n" + " Empty strings added!"
                                 + "\n" + "\n");
          }
          vec.addElement(vec1);
        }
      }
      buildTable();

      columnAttributes = new Vector();
      ServiceController sc;
      AttributePage ad;
      for (int k = 0; k < numcols; k++) {

        ad = (AttributePage)WizardPageLibrary.getPage(
            DataPackageWizardInterface.ATTRIBUTE_PAGE);
        ad.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        //WizardPopupDialog wpd = new WizardPopupDialog(ad, WizardContainerFrame.frame, false);
        //wpd.setVisible(false);

        columnAttributes.add(ad);

        String type = guessColFormat(k);
        OrderedMap map = ad.getPageData(AttributeSettings.Attribute_xPath);
        map.put(AttributeSettings.AttributeName_xPath, colTitles.elementAt(k));

        // either nominal/ordinal  . We guess as  nominal
        if (type.equals("text")) {

          Vector unique = getUniqueColValues(k);
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
          ad.setPageData(map);
        }

        else if (type.equals("float")) {
          String numberTypePath = AttributeSettings.Interval_xPath
                                  + "/numericDomain/numberType";
          map.put(numberTypePath, numberTypesArray[3]);
          numberTypePath = AttributeSettings.Ratio_xPath
                           + "/numericDomain/numberType";
          map.put(numberTypePath, numberTypesArray[3]);
          ad.setPageData(map);
        }

        else if (type.equals("integer")) {

          String numType = guessNumberType(k);

          if (numType.equals("Natural")) {
            String numberTypePath = AttributeSettings.Interval_xPath
                                    + "/numericDomain/numberType";
            map.put(numberTypePath, numberTypesArray[0]);
            numberTypePath = AttributeSettings.Ratio_xPath
                             + "/numericDomain/numberType";
            map.put(numberTypePath, numberTypesArray[0]);
          }
          if (numType.equals("Whole")) {
            String numberTypePath = AttributeSettings.Interval_xPath
                                    + "/numericDomain/numberType";
            map.put(numberTypePath, numberTypesArray[1]);
            numberTypePath = AttributeSettings.Ratio_xPath
                             + "/numericDomain/numberType";
            map.put(numberTypePath, numberTypesArray[1]);
          }
          if (numType.equals("Integer")) {
            String numberTypePath = AttributeSettings.Interval_xPath
                                    + "/numericDomain/numberType";
            map.put(numberTypePath, numberTypesArray[2]);
            numberTypePath = AttributeSettings.Ratio_xPath
                             + "/numericDomain/numberType";
            map.put(numberTypePath, numberTypesArray[2]);
          }
          ad.setPageData(map);
        } else if (type.equals("date")) {
          map.put(AttributeSettings.DateTime_xPath + "/dateTimePrecision",
                  new String("0"));
          ad.setPageData(map);
        }
      }

    }
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
          if ((inDelimiterList(oldToken, sDelim))
              && (inDelimiterList(token, sDelim))) {
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
    } else {result = false;
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
    if (stepNumber == 2)table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    if (fullColumnModel != null
        && stepNumber == (fullColumnModel.getColumnCount() + 2))
      FinishButton.setEnabled(true);
    if (stepNumber < 3)BackButton.setEnabled(true);

    StepNumberLabel.setText("Step #" + stepNumber);
    CardLayout cl = (CardLayout)ControlsPanel.getLayout();
    cl.show(ControlsPanel, "card" + stepNumber);

    if (stepNumber == 2)parseDelimited();
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
      AttributePage attrd = (AttributePage)columnAttributes.elementAt(
          stepNumber - 3);
      /*ColumnDataPanel.removeAll();
             ColumnDataPanel.add(attrd,BorderLayout.CENTER);
             ColumnDataPanel.validate();*/
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
    if (stepNumber < 2)table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    if (stepNumber == 1) {
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

    if (listener != null) listener.importComplete(createEml2NVPairs());

    this.dispose();
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
    for (int i = 0; i < nlines; i++) {
      int cnt = charCount(lines[i], subS);
      if (cnt > maxcnt - 1)cnt = maxcnt - 1;
      freq[cnt]++;
    }
    int mostfreq = 0;
    int mostfreqindex = 0;
    int tot = 0;
    for (int j = 0; j < maxcnt; j++) {
      tot = tot + freq[j];
      if (freq[j] > mostfreq) {
        mostfreq = freq[j];
        mostfreqindex = j;
      }
    }
    // establish a threshold; if less than, then return 0
    if ((100 * mostfreq / tot) < 80)mostfreq = 0;
    return mostfreqindex;
  }


  /**
   * guesses a delimiter based on frequency of appearance of common delimites
   *
   * @return String
   */
  private String guessDelimiter() {
    if (mostFrequent("\t") > 0) {
      parseOn = false;
      TabCheckBox.setSelected(true);
      CommaCheckBox.setSelected(false);
      SpaceCheckBox.setSelected(false);
      SemicolonCheckBox.setSelected(false);
      OtherCheckBox.setSelected(false);
      parseOn = true;
      return "tab";
    } else if (mostFrequent(",") > 0) {
      parseOn = false;
      TabCheckBox.setSelected(false);
      CommaCheckBox.setSelected(true);
      SpaceCheckBox.setSelected(false);
      SemicolonCheckBox.setSelected(false);
      OtherCheckBox.setSelected(false);
      parseOn = true;
      return "comma";
    } else if (mostFrequent(" ") > 0) {
      parseOn = false;
      TabCheckBox.setSelected(false);
      CommaCheckBox.setSelected(false);
      SpaceCheckBox.setSelected(true);
      SemicolonCheckBox.setSelected(false);
      OtherCheckBox.setSelected(false);
      parseOn = true;
      return "space";
    } else if (mostFrequent(";") > 0) {
      parseOn = false;
      TabCheckBox.setSelected(false);
      CommaCheckBox.setSelected(false);
      SpaceCheckBox.setSelected(false);
      SemicolonCheckBox.setSelected(true);
      OtherCheckBox.setSelected(false);
      parseOn = true;
      return "semicolon";
    } else if (mostFrequent(":") > 0) {
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
   * Uses the assumed column type to check all values in a column.
   * If data is not the correct type, a message is added to the Results
   * StringBuffer.
   *
   * @param colNum column number
   */
  void checkColumnInfo(int colNum) {

    if (colNum >= columnAttributes.size())
      return;
    AttributePage ad = (AttributePage)columnAttributes.elementAt(colNum);
    OrderedMap map = ad.getPageData(AttributeSettings.Attribute_xPath);
    String type = findMeasurementScale(map);
    if (type.equalsIgnoreCase("Nominal") || type.equalsIgnoreCase("Ordinal")) {
    } else if (type.equalsIgnoreCase("Interval")
               || type.equalsIgnoreCase("Ratio")) {
      Object o1 = map.get(AttributeSettings.Interval_xPath
                          + "/numericDomain/numberType");
      if (o1 == null)return;
      String numTypeStr = (String)o1;
      int numType = -1;
      if (numTypeStr.equalsIgnoreCase("natural"))numType = 0;
      if (numTypeStr.equalsIgnoreCase("whole"))numType = 1;
      if (numTypeStr.equalsIgnoreCase("integer"))numType = 2;
      if (numTypeStr.equalsIgnoreCase("real"))numType = 3;

      for (int j = 0; j < vec.size(); j++) {
        Vector v = (Vector)vec.elementAt(j);
        String str = (String)v.elementAt(colNum);
        if ((blankCheckFlag) || (str.length() > 0)) {
          if (numType == 0 && (!isInteger(str) || Integer.parseInt(str) <= 0)) {
            resultsBuffer.append("Item in col " + (colNum + 1) + " and row "
                                 + (j + 1) + " is NOT a Natural number!\n");
          } else if (numType == 1
                     && (!isInteger(str) || Integer.parseInt(str) < 0)) {
            resultsBuffer.append("Item in col " + (colNum + 1) + " and row "
                                 + (j + 1) + " is NOT a Whole number!\n");
          } else if (numType == 2 && !isInteger(str)) {
            resultsBuffer.append("Item in col " + (colNum + 1) + " and row "
                                 + (j + 1) + " is NOT an Integer!\n");
          } else if (numType == 3 && !isDouble(str)) {
            resultsBuffer.append("Item in col " + (colNum + 1) + " and row "
                                 + (j + 1) + " is NOT a Real number!\n");
          }
        }
      }
    } else if (type.equalsIgnoreCase("Datetime")) {
      for (int j = 0; j < vec.size(); j++) {
        Vector v = (Vector)vec.elementAt(j);
        String str = (String)v.elementAt(colNum);
        if ((blankCheckFlag) || (str.length() > 0)) {
          if (!isDate(str)) {
            resultsBuffer.append("Item in col " + (colNum + 1) + " and row "
                                 + (j + 1) + " is NOT a Date!\n");
          }
        }
      }
    }

  }


  private String findMeasurementScale(OrderedMap map) {

    Object o1 = map.get(AttributeSettings.Nominal_xPath
                        + "/enumeratedDomain[1]/codeDefinition[1]/code");
    if (o1 != null)return "Nominal";
    o1 = map.get(AttributeSettings.Nominal_xPath + "/textDomain[1]/definition");
    if (o1 != null)return "Nominal";

    o1 = map.get(AttributeSettings.Ordinal_xPath
                 + "/enumeratedDomain[1]/codeDefinition[1]/code");
    if (o1 != null)return "Ordinal";
    o1 = map.get(AttributeSettings.Ordinal_xPath + "/textDomain[1]/definition");
    if (o1 != null)return "Ordinal";

    o1 = map.get(AttributeSettings.Interval_xPath + "/unit/standardUnit");
    if (o1 != null)return "Interval";
    o1 = map.get(AttributeSettings.Ratio_xPath + "/unit/standardUnit");
    if (o1 != null)return "Ratio";

    o1 = map.get(AttributeSettings.DateTime_xPath + "/formatString");
    if (o1 != null)return "Datetime";

    return "";
  }


  /**
   * attempts to check to see if a file is just text or is binary. Reads bytes in
   * file and looks for '0'. If any '0's are found, assumed that the file is NOT
   * a text file.
   *
   * @param filename / //000000000000000000000000000000000 /* Checks a file to
    *   see if it is a text file by looking for bytes containing '0'
    * @return boolean
    */
   private boolean isTextFile(String filename) {
     boolean text = true;
     int res;
     int cnt = 0;
     int maxcnt = 2000; // only check this many bytes to avoid performance problems
     try {
       FileInputStream in = new FileInputStream(filename);
       while (((res = in.read()) > -1) && (cnt < maxcnt)) {
         cnt++;
         if (res == 0)text = false;
       }
     } catch (Exception e) {}
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
/**
    switch (distribution) {

      case WizardSettings.ONLINE:
        String id = saveDataFileAsTemp(filename);
        om.put(header + "physical/distribution/online/url",
               "ecogrid://knb/" + id);
        break;

      case WizardSettings.INLINE:
        String encoded = encodeAsBase64(new File(filename));
        om.put(header + "physical/distribution/inline", encoded);
        break;

      case WizardSettings.OFFLINE:

        //note - bug #1154 will eventually require at least one of the "offline"
        //element's children to be populated. For now, this is not required
        om.put(header + "physical/distribution/offline", "");
        break;
      case WizardSettings.NODATA:
        //if no data, then miss out the distribution elements altogether
    }
**/
    Enumeration e = columnAttributes.elements();
    int index = 1;
    while (e.hasMoreElements()) {
      AttributePage ad = (AttributePage)e.nextElement();
      OrderedMap map = ad.getPageData(header + "attributeList/attribute["
                                      + (index++) + "]");
      om.putAll(map);
    }

    int temp = 0;
    if (labelsInStartingLine)temp = 1;
    int numrecs = nlines_actual - startingLine + 1 + temp;
    String numRecords = (new Integer(numrecs)).toString();
    om.put(header + "numberOfRecords", XMLUtil.normalize(numRecords));

    return om;
  }


  /*
   * create a new id,
   * assign id to the data file and save a copy with that id as the
   * name
   */
//  private String saveDataFileAsTemp(String fn) {
//    AccessionNumber an = new AccessionNumber(Morpho.thisStaticInstance);
//    String id = an.getNextId();
//    File f = new File(fn);
//    FileSystemDataStore fds = new FileSystemDataStore(Morpho.thisStaticInstance);
//    try {
//      File res = fds.saveTempDataFile(id, new FileReader(f));
//    } catch (Exception w) {Log.debug(1, "error in TIW saving temp data file!");
//    }
//    return id;
//  }


  /*
   *  this method converts the input file to a byte array and then
   *  encodes it as a Base64 string
   */
//  private String encodeAsBase64(File f) {
//    byte[] b = null;
//    long len = f.length();
//    if (len > 200000) { // choice of 200000 is arbitrary - DFH
//      Log.debug(1, "Data file is too long to be put 'inline'!");
//      return null;
//    }
//    try {
//      FileReader fsr = new FileReader(f);
//      ByteArrayOutputStream baos = new ByteArrayOutputStream();
//      int chr = 0;
//      while ((chr = fsr.read()) != -1) {
//        baos.write(chr);
//      }
//      fsr.close();
//      baos.close();
//      b = baos.toByteArray();
//    } catch (Exception e) {Log.debug(1, "Problem encoding data as Base64!");
//    }
//    String enc = Base64.encode(b);
//    return enc;
//  }


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
  private JPanel Step1ControlsPanel = new JPanel();
  private JPanel Step1_TopTitlePanel = new JPanel();
  private JLabel Step1_TopTitleLabel = new JLabel();
  private JPanel Step1_TableNamePanel = new JPanel();
  private JLabel Step1_NameLabel = new JLabel();
  private JTextField TableNameTextField = new JTextField();
  private JPanel Step1_TableDescriptionPanel = new JPanel();
  private JLabel Step1_TableDescriptionLabel = new JLabel();
  private JTextField TableDescriptionTextField = new JTextField();
  private JPanel Step1_DelimiterChoicePanel = new JPanel();
  private JLabel Step1_DelimiterLabel = new JLabel();
  private JPanel Step1_DelimeterRadioPanel = new JPanel();
  private JRadioButton DelimitedRadioButton = new JRadioButton();
  private JPanel Step1_FixedFieldRadioPanel = new JPanel();
  private JRadioButton FixedFieldRadioButton = new JRadioButton();
  private JPanel StartingLinePanel = new JPanel();
  private JLabel StartingLineLabel = new JLabel();
  private JTextField StartingLineTextField = new JTextField();
  private JLabel ColumnLabelsLabel = new JLabel();
  private JCheckBox ColumnLabelsCheckBox = new JCheckBox();
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
  private JButton CancelButton = new JButton();
  private JButton BackButton = new JButton();
  private JButton NextButton = new JButton();
  private JButton FinishButton = new JButton();

}
