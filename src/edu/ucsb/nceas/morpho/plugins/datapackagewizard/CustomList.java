/**
 *  '$RCSfile: CustomList.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: sambasiv $'
 *     '$Date: 2004-04-21 23:20:16 $'
 * '$Revision: 1.55 $'
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

import edu.ucsb.nceas.morpho.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;


import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.InputVerifier;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

/**
 *  Interface   CustomList
 *
 */

public class CustomList extends JPanel {

  public static final short NULL = 0;
  public static final short EMPTY_STRING_TRIM = 10;
  public static final short EMPTY_STRING_NOTRIM = 20;
  public static final short IGNORE = 30;

  public static final short OR = 51;
  public static final short AND = 61;

  private CustomJTable table;

  private JButton addButton;
  private JButton editButton;
  private JButton duplicateButton;
  private JButton deleteButton;
  private JButton moveUpButton;
  private JButton moveDownButton;

  private boolean showAddButton;
  private boolean showEditButton;
  private boolean showDuplicateButton;
  private boolean showDeleteButton;
  private boolean showMoveUpButton;
  private boolean showMoveDownButton;

  protected AddAction addAction;
  protected DeleteAction deleteAction;
  protected EditAction editAction;
  protected DuplicateAction duplicateAction;
  protected MoveUpAction moveUpAction;
  protected MoveDownAction moveDownAction;

  private static TableModelEvent tableModelEvent;
  private DefaultTableModel model;
  private Dimension buttonDims;
  private JScrollPane scrollPane;

  // these Actions are optional, but are defined by the caller to provide
  // custom functionality for the list buttons
  private Action customAddAction;
  private Action customEditAction;
  private Action customDuplicateAction;
  private Action customDeleteAction;

  private boolean enabled = true;
  private double[] columnWidthPercentages;
  private JPanel buttonBox;

  ////////////

  /**
   *  constructor - creates a multi-column list based on the parameters passed:
   *
   *  @param colNames array containing Strings for column names that will be
   *                  displayed at the top of each column.
   *
   *  @param columnEditors array containing Objects that this class will use as
   *                  the default display and editing widget for the cells in
   *                  each column. For example,<code><pre>
   *
   *                    String[] listValues = new String[] {"item 1", "item 2"};
   *                    Object[] columnEditors
   *                          = new Object[] {  new JComboBox(listValues),
   *                                            new JTextField() };
   *
   *                  </pre></code>If any array element is null, the
   *                  corresponding column will be made non-editable, and if the
   *                  entire array is null, all columns will be made
   *                  non-editable.
   *
   *  @param displayRows the number of rows that should be shown in the list for
   *                  display purposes. Note that this will be overridden if,
   *                  for example, the list is added to the center of a
   *                  BorderLayout
   *
   *  @param showAddButton boolean value to indicate whether this button is
   *                  displayed next to the list
   *
   *  @param showEditButton boolean value to indicate whether this button is
   *                  displayed next to the list
   *
   *  @param showDuplicateButton boolean value to indicate whether this button is
   *                  displayed next to the list
   *
   *  @param showDeleteButton boolean value to indicate whether this button is
   *                  displayed next to the list
   *
   *  @param showMoveUpButton boolean value to indicate whether this button is
   *                  displayed next to the list
   *
   *  @param showMoveDownButton boolean value to indicate whether this button is
   *                  displayed next to the list
   *
   */
  public CustomList(String[] colNames, Object[] columnEditors, int displayRows,
                    boolean showAddButton, boolean showEditButton,
                    boolean showDuplicateButton, boolean showDeleteButton,
                    boolean showMoveUpButton, boolean showMoveDownButton) {

    this.showAddButton = showAddButton;
    this.showEditButton = showEditButton;
    this.showDuplicateButton = showDuplicateButton;
    this.showDeleteButton = showDeleteButton;
    this.showMoveUpButton = showMoveUpButton;
    this.showMoveDownButton = showMoveDownButton;

    init(colNames, displayRows, columnEditors);
  }

  private void init(String[] colNames, int displayRows,
                    Object[] columnEditors) {

    this.setLayout(new BorderLayout());

    initList(colNames, displayRows, columnEditors);

    initButtons();
    doEnablesDisables(new int[] {});
  }

  private void initList(String[] colNames, int displayRows,
                        Object[] columnEditors) {

    Vector colNamesVec = new Vector(colNames.length + 1);

    for (int i = 0; i < colNames.length; i++) {

      colNamesVec.add(i, colNames[i]);
    }
    // The last column is never displayed, but is used to hold a pointer to any
    // Object the user wants to associate with the row
    colNamesVec.add(colNames.length, "*UserObject*");

    Vector rowsData = new Vector();

    table = new CustomJTable(this, rowsData, colNamesVec, columnEditors);
    model = (DefaultTableModel) (table.getModel());
    table.setColumnSelectionAllowed(false);
    table.setRowSelectionAllowed(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setShowHorizontalLines(false);
    table.setShowVerticalLines(true);

    // The last column is never displayed, but is used to hold a pointer to any
    // Object the user wants to associate with the row

    TableColumnModel columnModel = table.getColumnModel();
    columnModel.removeColumn(columnModel.getColumn(table.getColumnCount() - 1));

    /////////////////////////////////
    columnWidthPercentages = new double[table.getColumnCount()];
    double equalP = 100.0 / table.getColumnCount();
    for (int i = 0; i < table.getColumnCount(); i++) {
      columnWidthPercentages[i] = equalP;

    }
    scrollPane = new JScrollPane(table);
    scrollPane.setVerticalScrollBarPolicy(
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    scrollPane.getViewport().setBackground(java.awt.Color.white);
    scrollPane.getViewport().addChangeListener(new ChangeListener() {

      public void stateChanged(ChangeEvent e) {

        setColumnSizes(scrollPane.getViewport().getSize().getWidth());
      }
    });
		this.add(scrollPane, BorderLayout.CENTER);
    this.setBorder(new EmptyBorder(0, 0, //WizardSettings.PADDING,
                                   2 * WizardSettings.PADDING, 0));
    //WizardSettings.PADDING));

    addAction = new AddAction(table, this);
    deleteAction = new DeleteAction(table, this);
    editAction = new EditAction(table, this);
    duplicateAction = new DuplicateAction(table, this);
    moveUpAction = new MoveUpAction(table, this);
    moveDownAction = new MoveDownAction(table, this);

//    addAction.addRowNoCustomAction();

//    if (table.getComponentAt(0, 0)!=null) {
//      table.editCellAt(0, 0, new EventObject(table.getComponentAt(0, 0)));
//    }
//    table.setRowSelectionInterval(0, 0);

    table.getSelectionModel().addListSelectionListener(
        new ListSelectionListener() {

      public void valueChanged(ListSelectionEvent e) {

        if(table.getRowCount() == 0) {
          doEnablesDisables(new int[]{});
          return;
        }

        if (e.getValueIsAdjusting()) {
          return;
        }

        Log.debug(45, "ListSelectionListener::valueChanged():");
        Log.debug(45,
                  "      - ListSelectionEvent first index  = " + e.getFirstIndex());
        Log.debug(45,
                  "      - ListSelectionEvent last  index  = " + e.getLastIndex());
        Log.debug(45, "      - getSelectedRowIndex  = " + getSelectedRowIndex());
        doEnablesDisables(table.getSelectedRows());
      }
    });

    if (columnEditors == null) {

      Log.debug(45, "NULL Column Editor Array; making ALL columns non-editable");
      for (int colIdx = 0; colIdx < table.getColumnCount(); colIdx++) {
        table.makeColumnNotEditable(colIdx);
      }

    }
    else {

      for (int i = 0; i < columnEditors.length; i++) {

        if (i > table.getColumnCount() - 1) {
          continue;
        }

        Object editor = columnEditors[i];
        TableColumn column = table.getColumnModel().getColumn(i);

        // if editor null, make non-editable:
        if (editor == null) {

          Log.debug(51,
                    "\nNULL Column Editor; making column " + i + " non-editable");
          table.makeColumnNotEditable(i);

        }
        else {

          Log.debug(51, "\nsetting Column " + i + " Editor = " + editor);
          if (editor instanceof JTextField) {

            /*JTextField jtf = new JTextField();
            if (! ( (JTextField) editor).isEditable()) {
              jtf.setEditable(false);
            }
            InputVerifier iv = ( (JTextField) editor).getInputVerifier();
            if (iv != null) {
              jtf.setInputVerifier(iv);

            }
            jtf.setForeground( ( (JTextField) editor).getForeground());
            jtf.setDisabledTextColor( ( (JTextField) editor).
                                     getDisabledTextColor());
            jtf.setBackground( ( (JTextField) editor).getBackground());*/
            Log.debug(51, "(JTextField)");
            DefaultCellEditor cellEd = new DefaultCellEditor((JTextField)editor);
            cellEd.setClickCountToStart(1);

            column.setCellEditor(cellEd);

          }
          else if (editor instanceof JCheckBox) {

            Log.debug(51, "(JCheckBox)");
            DefaultCellEditor cellEd = new DefaultCellEditor( (JCheckBox)
                editor);
            cellEd.setClickCountToStart(1);
            column.setCellEditor(cellEd);

          }
          else if (editor instanceof JComboBox) {

            Log.debug(51, "(JComboBox)");
            DefaultCellEditor cellEd = new DefaultCellEditor( (JComboBox)
                editor);
            cellEd.setClickCountToStart(1);
            column.setCellEditor(cellEd);

          }
          else {

            Log.debug(51, "(NOT RECOGNIZED - SETTING NON-EDITABLE)");
            //make non-editable for now; do we need other editor types??
            table.makeColumnNotEditable(i);
          }
        }
      }
    }
  }

  /**
   *  @return the TableModelEvent to be reused when requesting table updates
   */
  public TableModelEvent getTableModelEvent() {

    if (tableModelEvent == null) {
      tableModelEvent = new TableModelEvent(table.getModel());
    }
    return tableModelEvent;
  }

  private void setColumnSizes(double tableWidth) {

    //final double  fraction  = 1d/((double)(table.getColumnCount()));
    final double minFactor = 0.5;
    final double maxFactor = 2;

    for (int i = 0; i < table.getColumnCount(); i++) {

      TableColumn column = table.getColumnModel().getColumn(i);
      double fraction = columnWidthPercentages[i] / 100;

      int preferredWidth = (int) (tableWidth * fraction) - 1;
      int headerWidth = getHeaderWidth(i, column);
      if (preferredWidth < headerWidth) {
        preferredWidth = headerWidth;

      }
      column.setPreferredWidth(preferredWidth);
      column.setMinWidth( (int) (preferredWidth * minFactor));
      column.setMaxWidth( (int) (preferredWidth * maxFactor));
    }
  }

  /**
   *  Sets the relative widths of each column in the columnList. This is used when unequally sized
   *	columns are needed. Input is an array of doubles containing the percentage width of
   *  each column. The length of the array must equal the number of columns in the customList and
   *	the sum of the percentages must equal 100.
   *
   *	@param columnWidths the array of doubles representing the percentage widths of the columns
   *
   */

  public void setColumnWidthPercentages(double columnWidths[]) {
    int i;
    int len = columnWidths.length;

    if (len != table.getColumnCount()) {
      Log.debug(50, "CustomList.setColumnWidthPercentages: " +
                "Length of array passed is notequal to the number of columns");
      return;
    }
    double sum = 0;
    for (i = 0; i < len; i++) {
      sum += columnWidths[i];

      // total percentage must sum to 100 %.
    }
    if (sum != 100.0) {
      Log.debug(50, "CustomList.setColumnWidthPercentages: " +
                "Sum of the column width percentages is not equal to 100%");
      return;
    }
    this.columnWidthPercentages = columnWidths;
  }

  /**
   *  Sets the border widths for the button Panel.
   *
   *	@param top the spacing on top of the button panel
   *	@param left the spacing on left of the button panel
   *	@param bottom the spacing on bottom of the button panel
   *	@param right the spacing on right of the button panel
   *
   */

  public void setBorderForButtonPanel(int top, int left, int bottom, int right) {

    buttonBox.setBorder(new EmptyBorder(top, left, bottom, right));
    this.remove(buttonBox);
    this.add(buttonBox, BorderLayout.EAST);
    this.validate();
    this.repaint();
  }

  private int getHeaderWidth(int colNumber, TableColumn column) {

    TableCellRenderer headerRenderer = column.getHeaderRenderer();
    if (headerRenderer == null) {
      headerRenderer = table.getTableHeader().getDefaultRenderer();
    }
    Component comp = headerRenderer.getTableCellRendererComponent(
        table, column.getHeaderValue(),
        false, false, -1, colNumber);
    int headerWidth = comp.getPreferredSize().width;
    Insets ins = ( (JComponent) headerRenderer).getInsets();
    return (ins.left + headerWidth + ins.right);
  }

  /**
   *  Sets the dimensions for all the buttons in the custom list.
   *
   *	@param dims the required Dimension for all the buttons
   *
   */
  public void setListButtonDimensions(Dimension dims) {

    if (dims == null) {
      buttonDims = WizardSettings.LIST_BUTTON_DIMS;
    }
    else {
      buttonDims = dims;
    }
    resizeButtons();
  }


  /**
   *  Gives the "Add" button keyboard focus
   */
  public void focusAddButton() {

    if (addButton!=null) addButton.requestFocus();
  }


  /**
   *  Scrolls the CustomList (if necessary) to the particular row such that
   *	it becomes visible. This can be used to scroll the custom list to the
   *	top or bottom.
   *
   *	@param row the row to be scrolled to.
   *
   */
  public void scrollToRow(int row) {
    if (row < 0 || row >= table.getRowCount()) {
      return;
    }
    table.scrollRectToVisible(table.getCellRect(row, 0, true));
  }

  private void initButtons() {

    buttonBox = new JPanel();
    buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.Y_AXIS));

    boolean buttonPresent = false;

    buttonBox.setBorder(new EmptyBorder(0, 2 * WizardSettings.PADDING,
                                        WizardSettings.PADDING,
                                        WizardSettings.PADDING));

    if (showAddButton) {
      addButton = new JButton(addAction);
      addButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(addButton);
      buttonPresent = true;
    }

    if (showEditButton) {
      editButton = new JButton(editAction);
      editButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(editButton);
      buttonPresent = true;
    }

    if (showDuplicateButton) {
      duplicateButton = new JButton(duplicateAction);
      duplicateButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(duplicateButton);
      buttonPresent = true;
    }

    if (showDeleteButton) {
      deleteButton = new JButton(deleteAction);
      deleteButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(deleteButton);
      buttonPresent = true;
    }

    if (showMoveUpButton) {
      moveUpButton = new JButton(moveUpAction);
      moveUpButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(moveUpButton);
      buttonPresent = true;
    }

    if (showMoveDownButton) {
      moveDownButton = new JButton(moveDownAction);
      moveDownButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(moveDownButton);
      buttonPresent = true;
    }

    if (!buttonPresent) {
      return;
    }

    setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS);
    resizeButtons();

    buttonBox.add(Box.createGlue());
    this.add(buttonBox, BorderLayout.EAST);
  }

  private void resizeButtons() {

    if (showAddButton) {
      WidgetFactory.setPrefMaxSizes(addButton, buttonDims);
    }
    if (showEditButton) {
      WidgetFactory.setPrefMaxSizes(editButton, buttonDims);
    }
    if (showDuplicateButton) {
      WidgetFactory.setPrefMaxSizes(duplicateButton, buttonDims);
    }
    if (showDeleteButton) {
      WidgetFactory.setPrefMaxSizes(deleteButton, buttonDims);
    }
    if (showMoveUpButton) {
      WidgetFactory.setPrefMaxSizes(moveUpButton, buttonDims);
    }
    if (showMoveDownButton) {
      WidgetFactory.setPrefMaxSizes(moveDownButton, buttonDims);
    }
  }

  private boolean selectionExists = true;

  //
  private void doEnablesDisables(int[] selRows) {

    Log.debug(51,
              "\n\n>>>> doEnablesDisables(): selRows count = " + selRows.length);

    selectionExists = (selRows != null && selRows.length > 0);

    // ADD always available:
    if (showAddButton) {
      addButton.setEnabled(true);

      // EDIT available only if a row selected:
    }
    if (showEditButton) {
      editButton.setEnabled(selectionExists);

      // DUPLICATE available only if a row selected:
    }
    if (showDuplicateButton) {
      duplicateButton.setEnabled(selectionExists);

      // DELETE available only if a row selected:
    }
    if (showDeleteButton) {
      deleteButton.setEnabled(selectionExists);

      // MOVE UP available only if a row selected *and* it's not row 0:
    }
    if (showMoveUpButton) {
      moveUpButton.setEnabled(selectionExists
                              && selRows[0] > 0);

      // MOVE DOWN available only if a row selected *and* it's not last row:
    }
    if (showMoveDownButton) {
      moveDownButton.setEnabled(selectionExists
                                &&
                                selRows[selRows.length - 1] <
                                (table.getRowCount() - 1));
    }
  }

  /**
   *  Tells the list to stop editing and commit all edited values to the model.
   *  Typically called by a wizard page when next> is pressed, so all changes
   *  are committed before validation.
   */
  public void fireEditingStopped() {

    Component editingComp = table.getEditorComponent();
    if (editingComp != null) {
      table.editingStopped(new ChangeEvent(editingComp));
      EventListener[] list = editingComp.getListeners(java.awt.event.FocusListener.class);
      for(int i = 0; list != null && i < list.length; i++)
        ((FocusListener)list[i]).focusLost(new FocusEvent(editingComp, FocusEvent.FOCUS_LOST));
    }
  }

  /**
   *		Method to fire the Add action of the CustomList. A row is added to the customlist as
   *		a result.This is useful when the customlist needs to be controlled from outside.
   *
   */
  public void fireAddAction() {
    addAction.actionPerformed(null);
  }

  /**
   *		Method to fire the Edit action of the CustomList. The selected row is edited as
   *		a result.This is useful when the customlist needs to be controlled from outside.
   *
   */
  public void fireEditAction() {
    editAction.actionPerformed(null);
  }

  /**
   *		Method to fire the Delete action of the CustomList. The selected row is deleted as
   *		a result.This is useful when the customlist needs to be controlled from outside.
   *
   */
  public void fireDeleteAction() {
    deleteAction.actionPerformed(null);
  }

  /**
   *		Method to fire the Duplicate action of the CustomList. The selected row is duplicated 	*		as a result.This is useful when the customlist needs to be controlled from outside.
   *
   */
  public void fireDuplicateAction() {
    duplicateAction.actionPerformed(null);
  }

  /**
   *		Method to fire the Move-Up action of the CustomList. The selected row is moved one row
   *		up as a result.This is useful when the customlist needs to be controlled from outside.
   *
   */
  public void fireMoveUpAction() {
    moveUpAction.actionPerformed(null);
  }

  /**
   *		Method to fire the Move-Down action of the CustomList. The selected row is moved one row
   *		down as a result.This is useful when the customlist needs to be controlled from outside.
   *
   */
  public void fireMoveDownAction() {
    moveDownAction.actionPerformed(null);
  }

  /**
   *  returns the index of the currently-selected row, or -1 if none selected
   *
   *  @return the index of the currently-selected row, or -1 if none selected
   */
  public int getSelectedRowIndex() {

    return table.getSelectedRow();
  }

  /**
   *  returns the List containing the objects in the currently-selected row, or
   *  null if none selected
   *
   *  @return the List containing the objects in the currently-selected row, or
   *          null if none selected
   */
  public List getSelectedRowList() {

    int selRow = this.getSelectedRowIndex();
    if (selRow < 0) {
      return null;
    }

    List listOfRowLists = this.getListOfRowLists();
    if (listOfRowLists == null) {
      return null;
    }

    Object rowObj = listOfRowLists.get(selRow);
    if (rowObj == null) {
      return null;
    }

    return (List) rowObj;
  }


  /**
   * returns an array of Lists containing the objects in the currently-selected
   * rows, or null if none selected
   *
   *  @return the List containing the objects in the currently-selected row, or
   *          null if none selected
   */
  public List[] getSelectedRows() {

    int[] rows = table.getSelectedRows();

    if (rows == null || rows.length < 1)return null;

    List listOfRowLists = this.getListOfRowLists();
    if (listOfRowLists == null)return null;

    List[] returnList = new List[rows.length];

    for (int i=0; i < rows.length; i++) {

      returnList[i] = (List)(listOfRowLists.get(rows[i]));
    }
    return returnList;
  }



  public void setSelectedRows(int idx[]) {

    DefaultListSelectionModel newModel = new DefaultListSelectionModel();
    newModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    for (int i = 0; i < idx.length; i++) {
      newModel.addSelectionInterval(idx[i], idx[i]);
    }
    table.setSelectionModel(newModel);
  }

  /**
   * replaces the currently-selected row with the Objects in the List provided,
   * or with a blank row if the List is null
   *
   * @param newRow List containing the objects in the new row that will replace
   *   the currently-selected row.
   */
  public void replaceSelectedRow(List newRow) {

    if (newRow == null) {
      newRow = new ArrayList();

    }
    int selRow = this.getSelectedRowIndex();
    if (selRow < 0) {
      return;
    }

    removeRow(selRow);
    model.insertRow(selRow, newRow.toArray());
  }

  /**
   * replaces the row corresponding to the given row number with the Objects in the
   *	List provided,or with a blank row if the List is null. Row numbers are 0-indexed.
   *
   * @param rowIndex index of the row to be replaced
   * @param newRow List containing the objects in the new row that will replace
   *   the currently-selected row.
   */
  public void replaceRow(int rowIndex, List newRow) {

    if (model.getRowCount() <= rowIndex) {
      return;
    }
    if (newRow == null) {
      newRow = new ArrayList();

    }
    removeRow(rowIndex);
    model.insertRow(rowIndex, newRow.toArray());
  }

  /**
   *  returns the total number of rows in the list
   *
   *  @return the total number of rows in the list
   */
  public int getRowCount() {

    return table.getRowCount();
  }

  /**
   * adds the row to the list after the currently-selected row, or at the end if
   * no row is selected. Then scrolls to make the new row visible.
   *
   * @param rowList List defining the row data. If the list has "n" columns,
   *   the passed List may have 0 -> (n+1) elements as follows: <ul><li>0..n
   *   represents the actual data that will appear displayed in the list
   *   columns. If the number of elements is less than the number of display
   *   columns, the remaining columns will appear blank </li><li>List element
   *   (n+1) is the optional "user object", which may be any object that the
   *   user wants to associate with this row </li>
   */
  public void addRow(List rowList) {

    int row = getSelectedRowIndex();

    if(table.getRowCount() == 0)
      row = -1;

    if (row < 0) {

      row = model.getRowCount();
      model.addRow(rowList.toArray());

    }
    else {

      model.insertRow(++row, rowList.toArray());
    }
    fireEditingStopped();
    table.tableChanged(getTableModelEvent());
    Component comp = table.getComponentAt(row, 0);
    if (comp != null) {
      table.editCellAt(row, 0, new EventObject(comp));
      comp.requestFocus();
    }
    table.scrollRectToVisible(table.getCellRect(row, 0, true));
    table.setRowSelectionInterval(row, row);
    table.validate();
    table.repaint();

  }

  /**
   * removes the row with the specified index from the list
   *
   * @param row index of the row to be removed
   */
  public void removeRow(int row) {

    model.removeRow(row);
//    if (table.getRowCount() < 1) addFirstRowBack();
    table.tableChanged(getTableModelEvent());
    table.clearSelection();
  }

  /**
   *  removes all the rows from the list
   *
   */
  public void removeAllRows() {
    model = (DefaultTableModel) table.getModel();
    for (; table.getRowCount() > 0; ) {
      model.removeRow(0);
    }
    table.tableChanged(getTableModelEvent());
    model = (DefaultTableModel) table.getModel();
  }

  /**
   *  Removes any rows that are "empty", as defined by the parameters specified
   *  in the passed <code>short</code> array "conditions", as described below. <ul>
   *  <li>If the value of parameter "logicMode is <em>OR</em>, then the row is
   *  deleted if <em>any</em> of the conditions are met.</li>
   *  <li>If the value of parameter "logicMode is <em>AND</em>, then the row is
   *  deleted if <em>all</em> of the conditions are met.</li></ul>
   *  The elements of the "conditions" array correspond to the columns in the
   *  table (so element [0] defines the first column, element [1] the second,
   *  and so on). Legal values for the "conditions" array are as follows:
   *
   *  <ul><li>
   *  NULL                - if the contents of this column are null, delete the
   *                        row
   *  </li><li>
   *
   *  EMPTY_STRING_TRIM   - delete the row if the contents of this column are
   *                        null, or if the column contains a non-String value,
   *                        or if the column contains the empty string
   *                        <em>NOTE: this parameter requires that the String is
   *                        TRIMMED before evaluation (@see java.lang.String -
   *                        trim()</em>. If you wish to preserve whitespace, use
   *                        EMPTY_STRING_NOTRIM
   *  </li><li>
   *
   *  EMPTY_STRING_NOTRIM - delete the row if the contents of this column are
   *                        null, or if the column contains a non-String value,
   *                        or if the column contains the empty string
   *                        <em>NOTE: this parameter requires that the String is
   *                        *NOT* TRIMMED before evaluation
   *                        (@see java.lang.String - trim()</em>. If you wish to
   *                        have whitespace removed before evaluation, use
   *                        EMPTY_STRING_TRIM
   *  </li><li>
   *
   *  IGNORE              - do not validate this column
   *  </li><ul>
   *
   *  @param logicMode  the logical mode in which the conditions will be .
   *                    processed CustomList.OR signifies that the row is
   *                    deleted if <em>any</em> of the conditions are met, and
   *                    CustomList.AND signifies that the row is deleted only if
   *                    <em>all</em> of the conditions are met.
   *
   *  @param conditions The elements of the "conditions" array correspond to the
   *                    columns in the table (so element [0] defines the first
   *                    column, element [1] the second, and so on). Legal values
   *                    for the "conditions" array are:
   *                    <ul><li>NULL</li>
   *                    <li>EMPTY_STRING_TRIM</li>
   *                    <li>EMPTY_STRING_NOTRIM</li>
   *                    <li>IGNORE</li><ul>
   */
  public void deleteEmptyRows(short logicMode, short[] conditions) {

    if (conditions == null) {
      return;
    }
    if (logicMode != OR && logicMode != AND) {
      return;
    }

    List rowLists = this.getListOfRowLists();

    boolean[] rowsToDelete = new boolean[rowLists.size()];

    Arrays.fill(rowsToDelete, false);

    int rowNumber = -1;

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      rowNumber++;

      Object nextRowObj = it.next();

      if (nextRowObj == null) {

        rowsToDelete[rowNumber] = true;
        continue;
      }

      List nextRow = (List) nextRowObj;

      if (nextRow.size() < 1) {

        rowsToDelete[rowNumber] = true;
        continue;
      }

      if (logicMode == OR) {
        checkColumnContents_OR(nextRow, conditions,
                               rowsToDelete, rowNumber);
      }
      else {
        checkColumnContents_AND(nextRow, conditions,
                                rowsToDelete, rowNumber);
      }
    }
    // remove rows to be deleted IN REVERSE ORDER - since each removal
    // reduces the number of rows
    for (int i = rowsToDelete.length - 1; i > -1; i--) {

      if (rowsToDelete[i]) {
        this.removeRow(i);
      }
    }
  }

  private void checkColumnContents_OR(List nextRow, short[] conditions,
                                      boolean[] rowsToDelete, int rowNumber) {

    for (int colIdx = 0; colIdx < conditions.length; colIdx++) {

      switch (conditions[colIdx]) {

        case IGNORE:
          break;

        case NULL:
          if (nextRow.get(colIdx) == null) {
            rowsToDelete[rowNumber] = true;
          }
          break;

        case EMPTY_STRING_NOTRIM:
          if ( (nextRow.get(colIdx) == null)
              || ! (nextRow.get(colIdx) instanceof String)
              || ( (String) (nextRow.get(colIdx))).equals("")) {

            rowsToDelete[rowNumber] = true;
          }
          break;

        case EMPTY_STRING_TRIM:
          if ( (nextRow.get(colIdx) == null)
              || ! (nextRow.get(colIdx) instanceof String)
              || ( (String) (nextRow.get(colIdx))).trim().equals("")) {

            rowsToDelete[rowNumber] = true;
          }
          break;
      }
    }
  }

  private void checkColumnContents_AND(List nextRow, short[] conditions,
                                       boolean[] rowsToDelete, int rowNumber) {

    boolean result = true;
    int ignoreCount = 0;

    for (int colIdx = 0; colIdx < conditions.length; colIdx++) {

      switch (conditions[colIdx]) {

        case IGNORE:

          //check to ensure we dont have ALL conditions == "IGNORE"...
          ignoreCount++;
          break;

        case NULL:
          result = result && (nextRow.get(colIdx) == null);
          break;

        case EMPTY_STRING_NOTRIM:
          result = result &&
              ( (nextRow.get(colIdx) == null)
               || ! (nextRow.get(colIdx) instanceof String)
               || ( (String) (nextRow.get(colIdx))).equals(""));
          break;

        case EMPTY_STRING_TRIM:
          result = result &&
              ( (nextRow.get(colIdx) == null)
               || ! (nextRow.get(colIdx) instanceof String)
               || ( (String) (nextRow.get(colIdx))).trim().equals(""));
          break;
      }
    }
    //check to ensure we dont have ALL conditions == "IGNORE"...
    if (ignoreCount >= conditions.length) {
      result = false;

    }
    rowsToDelete[rowNumber] = result;
  }

  /**
   *  returns a <code>java.util.List</code> containing elements that are also
   *  <code>java.util.List</code> objects, each of which represents a row from
   *  the list. Therefore, element 0 in the return List will contain a List that
   *  has the first row objects in it, and so on.  Each row element will contain
   *  the objects used to poopulate the list's (table-)model, so the minimum
   *  number of entries in a row will be zero, and the max number will equal the
   *  number of columns in the list
   *
   *  @return a <code>java.util.List</code> containing elements that are also
   *    <code>java.util.List</code> objects, each of which represents a row from
   *    the list
   */
  public List getListOfRowLists() {

    //maintain current selection:
    int currentSelection = getSelectedRowIndex();
    fireEditingStopped();
    return (List) (model.getDataVector());
  }

  /**
   * Sets the <code>javax.swing.Action</code> to be executed on pressing the ADD
   * button. NOTE that if no custom add action is set, or if a null action is
   * set, the ADD button's 'private' Action (defined elsewhere in this class)
   * will be executed; otherwise the custom action will be executed (and the
   * 'private' Action will NOT be executed). <em>Note that this behavior is
   * different for the other custom action get/set methods, which are executed
   * IN ADDITION to private actions</em>
   *
   * @param a <code>javax.swing.Action</code> to be executed
   */
  public void setCustomAddAction(Action a) {

    this.customAddAction = a;
  }

  /**
   *  Gets the <code>javax.swing.Action</code> to be executed on pressing the
   *  ADD button. NOTE that if no custom add action is set, or if a null action
   *  is set, the ADD button's 'private' Action (defined elsewhere in this
   *  class) will be executed; otherwise the custom action will be executed (and
   *  the 'private' Action will NOT be executed).
   *  <em>Note that this behavior is different for the other custom action
   *  get/set methods, which are executed IN ADDITION to private actions</em>
   *
   *
   *  @return the <code>javax.swing.Action</code> to be executed
   */
  public Action getCustomAddAction() {

    return this.customAddAction;
  }

  /**
   * Sets the <code>javax.swing.Action</code> to be executed on pressing the
   * appropriate list button. NOTE that the button's 'private' Action (defined
   * elsewhere in this class) will be executed first, and then the custom action
   * will be executed
   *
   * @param a <code>javax.swing.Action</code> to be executed
   */
  public void setCustomEditAction(Action a) {

    this.customEditAction = a;
  }

  /**
   *  Gets the <code>javax.swing.Action</code> to be executed on pressing the
   *  appropriate list button. NOTE that the button's 'private' Action (defined
   *  elsewhere in this class) will be executed first, and then the custom
   *  action will be executed
   *
   *  @return the <code>javax.swing.Action</code> to be executed
   */
  public Action getCustomEditAction() {

    return this.customEditAction;
  }

  /**
   * Sets the <code>javax.swing.Action</code> to be executed on pressing the
   * appropriate list button. NOTE that the button's 'private' Action (defined
   * elsewhere in this class) will be executed first, and then the custom action
   * will be executed
   *
   * @param a <code>javax.swing.Action</code> to be executed
   */
  public void setCustomDuplicateAction(Action a) {

    this.customDuplicateAction = a;
  }

  /**
   *  Gets the <code>javax.swing.Action</code> to be executed on pressing the
   *  appropriate list button. NOTE that the button's 'private' Action (defined
   *  elsewhere in this class) will be executed first, and then the custom
   *  action will be executed
   *
   *  @return the <code>javax.swing.Action</code> to be executed
   */
  public Action getCustomDuplicateAction() {

    return this.customDuplicateAction;
  }

  /**
   * Sets the <code>javax.swing.Action</code> to be executed on pressing the
   * appropriate list button. NOTE that the CUSTOM action will be executed FIRST,
   * and then the button's 'private' Action (defined elsewhere in this class)
   * will be executed
   *
   * @param a <code>javax.swing.Action</code> to be executed
   */
  public void setCustomDeleteAction(Action a) {

    this.customDeleteAction = a;
  }

  /**
   *  Gets the <code>javax.swing.Action</code> to be executed on pressing the
   *  appropriate list button. NOTE that the button's 'private' Action (defined
   *  elsewhere in this class) will be executed first, and then the custom
   *  action will be executed
   *
   *  @return the <code>javax.swing.Action</code> to be executed
   */
  public Action getCustomDeleteAction() {

    return this.customDeleteAction;
  }
	
	/**
   * 	Sets the boolean to indicate whether the customlist can be edited or not. If false,
	 *	all the columns are made non-editable
   *
   * @param editable boolean indicating whether the custom list can be edited or not
   */
	 
  public void setEditable(boolean editable) {
    table.setEditableForAllColumns(editable);
  }

	/**
   * 	Method to edit a particular cell. This method call results in the cursor being placed 
	 *	at that cell for editing (if the cell is editable).
   *
   * @param row the row of the cell
	 * @param col the column of the cell
   */
  public void editCellAt(int row, int col) {

    if (row >= table.getRowCount() || row < 0) {
      return;
    }
    if (col >= table.getColumnCount() || col < 0) {
      return;
    }
    table.editCellAt(row, col);
  }
	
	public void selectAndEditCell(int row, int col) {
		if (row >= table.getRowCount() || row < 0) {
      return;
    }
    if (col >= table.getColumnCount() || col < 0) {
      return;
    }
    table.selectAndEditCell(row, col);
	}
	
	/**
   * 	Sets the boolean to indicate whether the customlist should be disabled or not. If false,
	 *	all the columns are made non-editable and the buttons are disabled
   *
   * @param editable boolean indicating whether the custom list be enabled or not
   */
	
  public void setEnabled(boolean enabled) {

    this.enabled = enabled;

    table.setEnabled(enabled);
    if (!enabled) {

      if (showAddButton) {
        addButton.setEnabled(enabled);
      }
      if (showEditButton) {
        editButton.setEnabled(enabled);
      }
      if (showDuplicateButton) {
        duplicateButton.setEnabled(enabled);
      }
      if (showDeleteButton) {
        deleteButton.setEnabled(enabled);
      }
      if (showMoveUpButton) {
        moveUpButton.setEnabled(enabled);
      }
      if (showMoveDownButton) {
        moveDownButton.setEnabled(enabled);
      }
			this.setEditable(enabled);
    }
    else {
      doEnablesDisables(table.getSelectedRows());
			this.setEditable(true);
    }
  }
	
	/**
   * 	Gets the boolean indicating whether the customlist is enabled or not
   *
   * @return boolean true custom list is enabled and false otherwise
   */
	
  public boolean isEnabled() {
    return enabled;
  }
	
	/**
   * 	Sets the boolean to indicate whether the customlist can be edited or not. If false,
	 *	all the columns are made non-editable
   *
   * @param editable boolean indicating whether the custom list can be edited or not
   */
	
	public void setBackground(Color bgColor) {
		
		if(bgColor != null && table != null) {
			table.setBackground(bgColor);
			scrollPane.getViewport().setBackground(bgColor);
		}
	}
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//                       A C T I O N   C L A S S E S
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class AddAction
    extends AbstractAction {

  private CustomJTable table;
  private CustomList parentList;

  public AddAction(CustomJTable table, CustomList parentList) {

    super("Add");
    this.table = table;
    this.parentList = parentList;
  }

  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList ADD action");

    if (parentList.getCustomAddAction() == null) {

      List newRowList = new ArrayList();

      for (int i = 0; i < table.getColumnCount(); i++) {

        TableCellEditor cellEditor
            = table.getColumnModel().getColumn(i).getCellEditor();
        String cellVal = (cellEditor != null) ?
            String.valueOf(cellEditor.getCellEditorValue()) : "";
        String colClassName = table.getColumnClass(i).getName();

        if (colClassName.equals("javax.swing.JTextField")) {

          Log.debug(45, "\nAddAction - (JTextField)");
          newRowList.add("");

        }
        else if (colClassName.equals("javax.swing.JCheckBox")) {

          Log.debug(45, "\nAddAction - (JCheckBox)");
          newRowList.add(new Boolean(cellVal));

        }
        else if (colClassName.equals("javax.swing.JComboBox")) {

          Log.debug(45, "\nAddAction - (JComboBox)");
          newRowList.add(cellVal);

        }
        else {

          Log.debug(45, "\nAddAction - NOT RECOGNIZED");
          newRowList.add("");
        }
      }
      parentList.addRow(newRowList);

    }
    else {
      parentList.getCustomAddAction().actionPerformed(null);
    }
  }
}

class EditAction
    extends AbstractAction {

  private CustomJTable table;
  private CustomList parentList;

  public EditAction(CustomJTable table, CustomList parentList) {

    super("Edit");
    this.parentList = parentList;
    this.table = table;
  }

  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList EDIT action");
    int row = table.getSelectedRow();
    if (row < 0) {
      return;
    }
    parentList.fireEditingStopped();

    // get object here - only a String surrogate is shown by the cell renderer,
    // but the entire object is actually in the table model!

    //execute the user's custom action:
    if (parentList.getCustomEditAction() != null) {
      parentList.getCustomEditAction().actionPerformed(null);
    }
  }
}

class DuplicateAction
    extends AbstractAction {

  private CustomJTable table;
  private CustomList parentList;

  public DuplicateAction(CustomJTable table, CustomList parentList) {

    super("Duplicate");
    this.table = table;
    this.parentList = parentList;
  }

  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList DUPLICATE action");
    int row = table.getSelectedRow();
    if (row < 0) {
      return;
    }
    parentList.fireEditingStopped();

    // get object here - only a String surrogate is shown by the cell renderer,
    // but the entire object is actually in the table model!


    //execute the user's custom action:
    if (parentList.getCustomDuplicateAction() != null) {
      parentList.getCustomDuplicateAction().actionPerformed(null);
    }
  }
}

class DeleteAction
    extends AbstractAction {

  private CustomJTable table;
  private CustomList parentList;

  public DeleteAction(CustomJTable table, CustomList parentList) {

    super("Delete");
    this.table = table;
    this.parentList = parentList;
  }


  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList DELETE action");
    int[] rows = table.getSelectedRows();

    if (rows.length == 0) return;

    parentList.fireEditingStopped();

    //execute the user's custom action:
    if (parentList.getCustomDeleteAction() != null) {
      parentList.getCustomDeleteAction().actionPerformed(
      new ActionEvent(parentList, ActionEvent.ACTION_PERFORMED, "Delete"));
    }

    //now remove the rows
    for (int i = 0; i < rows.length; i++) {
      parentList.removeRow(rows[i] - i);
    }
  }

}

class MoveUpAction
    extends AbstractAction {

  private CustomJTable table;
  private CustomList parentList;
  private DefaultTableModel model;

  public MoveUpAction(CustomJTable table, CustomList parentList) {

    super("Move Up");
    this.table = table;
    this.parentList = parentList;
    model = (DefaultTableModel) (table.getModel());
  }

  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList MOVE UP action");
    int rows[] = table.getSelectedRows();
    if (rows.length == 0) {
      return;
    }

    parentList.fireEditingStopped();

    for (int i = 0; i < rows.length; i++) {
      model.moveRow(rows[i], rows[i], rows[i] - 1);
    }
    table.tableChanged(parentList.getTableModelEvent());
    //table.setRowSelectionInterval(row - 1, row - 1);
    table.repaint();
  }
}

class MoveDownAction
    extends AbstractAction {

  private CustomJTable table;
  private CustomList parentList;
  private DefaultTableModel model;

  public MoveDownAction(CustomJTable table, CustomList parentList) {

    super("Move Down");
    this.table = table;
    this.parentList = parentList;
    model = (DefaultTableModel) (table.getModel());

  }

  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList MOVE DOWN action");
    int rows[] = table.getSelectedRows();
    if (rows.length < 0) {
      return;
    }

    parentList.fireEditingStopped();

    for (int i = rows.length - 1; i >= 0; i--) {
      model.moveRow(rows[i], rows[i], rows[i] + 1);
    }
    table.tableChanged(parentList.getTableModelEvent());
    //table.setRowSelectionInterval(row + 1, row + 1);
  }
}

class CustomJTable extends JTable{

//    EditableStringRenderer    editableStringRenderer
//                                    = new EditableStringRenderer();
  private DefaultTableCellRenderer defaultRenderer
      = new DefaultTableCellRenderer();

  private DefaultCellEditor defaultCellEditor
      = new DefaultCellEditor(new JTextField());
  Object[] editors;
	
	boolean[] columnsEditableFlags;

  int DOUBLE_CLICK = 2;
	
	private int currentRow = -1;
	private int currentCol = -1;

  public CustomJTable(final CustomList parentList, Vector rowVect, Vector colNamesVec, Object[] editors) {

    super(new DefaultTableModel(rowVect, colNamesVec));
    this.editors = editors;
    super.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    defaultCellEditor.setClickCountToStart(1);

    columnsEditableFlags = new boolean[colNamesVec.size()];
    Arrays.fill(columnsEditableFlags, true);

    // Added a mouse listener which looks for double clicks on
    // columns. If there is a double click on the column,
    // edit action is fired for the parentList
		this.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				
				if(e.getClickCount()==DOUBLE_CLICK){
          parentList.fireEditAction();
        }
			}
      public void mouseEntered(MouseEvent e) {}
      public void mouseExited(MouseEvent e) {}
      public void mousePressed(MouseEvent e) {}
      public void mouseReleased(MouseEvent e) {}
    }
    );
		
	}

  //override super
  public TableCellRenderer getCellRenderer(int row, int col) {

    Class colClass = null;

    if (editors != null && editors[col] != null) {
      colClass = editors[col].getClass();

    }
    if (colClass == null) {
      return defaultRenderer;
    }

/////// JCHECKBOX //////////////////////////////////////////////////////////////
    if (colClass.getName().equals("javax.swing.JCheckBox")) {
      return new TableCellRenderer() {

        public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

          boolean checked = false;
          if (value != null) {
            checked = ( (Boolean) value).booleanValue();
          }
          JCheckBox cell = new JCheckBox("", checked);
          if (cell.getForeground() == null) {
            cell.setForeground(table.getForeground());
          }
          if (cell.getBackground() == null) {
            cell.setBackground(table.getBackground());
          }
          if (isSelected) {
            cell.setForeground(table.getSelectionForeground());
          }
          if (hasFocus) {
            if (table.isCellEditable(row, column)) {
              cell.setForeground(table.getSelectionForeground());
              cell.setBackground(table.getSelectionBackground());
            }
          }
          if (cell.getFont() == null) {
            cell.setFont(table.getFont());
          }
          cell.setOpaque(true);
          cell.validate();
          cell.repaint();
          return cell;
        }
      };
    }

/////// JCOMBOBOX //////////////////////////////////////////////////////////////
    if (colClass.getName().equals("javax.swing.JComboBox")) {

      JComboBox origList = (JComboBox) (editors[col]);

      ListModel model = origList.getModel();

      int listLength = model.getSize();

      final Object[] listElemArray = new Object[listLength];

      for (int i = 0; i < listLength; i++) {

        listElemArray[i] = model.getElementAt(i);
      }

      return new TableCellRenderer() {

        public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

          JComboBox cell = new JComboBox(listElemArray);

          if (value != null) {
            cell.setSelectedItem(value);
          }
          else {
            cell.setSelectedIndex(0);

          }
          if (cell.getFont() == null) {
            cell.setFont(table.getFont());

          }
          cell.validate();
          cell.repaint();
          return cell;
        }
      };
    }
    if (colClass.getName().equals("javax.swing.JLabel")) {
      final JLabel origLabel = (JLabel) (editors[col]);
      return new TableCellRenderer() {

        public Component getTableCellRendererComponent(JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column) {

          JLabel cell = new JLabel(origLabel.getText());
          cell.setHorizontalAlignment(origLabel.getHorizontalAlignment());
          return cell;
        }
      };
    }
    if (colClass.getName().equals("javax.swing.JTextField")) {
      final JTextField origTextField = (JTextField) (editors[col]);
      DefaultTableCellRenderer defaultR = new DefaultTableCellRenderer();
      defaultR.setBackground(origTextField.getBackground());
      defaultR.setForeground(origTextField.getForeground());
      EventListener[] list = origTextField.getListeners(java.awt.event.FocusListener.class);
      for(int i = 0; i < list.length; i++)
        defaultR.addFocusListener((java.awt.event.FocusListener)list[i]);

      return defaultR;

    }

    return defaultRenderer;
  }

  //override super
  public Class getColumnClass(int col) {

    Class colClass = java.lang.String.class;

    if (editors != null && editors[col] != null) {
      colClass = editors[col].getClass();

    }
    Log.debug(45, "\nCustomJTable.getColumnClass(" + col + "): "
              + colClass.getName());
    return colClass;
  }
	
	private JTextField getTextFieldEditor(int row, int col) {
		
		String val = (String)this.getValueAt(row, col);
		return new JTextField(val);
	}

  //override super
  public boolean getDragEnabled() {
    return false;
  }

  //override super
  public int getSelectedRow() {

    return super.getSelectionModel().getMaxSelectionIndex();
  }


  public void makeColumnNotEditable(int col) {

    columnsEditableFlags[col] = false;
  }

  public void setEditableForAllColumns(boolean editable) {

    for (int i = 0; i < getColumnCount(); i++) {
      columnsEditableFlags[i] = editable;
    }
  }

  public boolean isCellEditable(int row, int col) {

    return columnsEditableFlags[col];
  }

	public void selectAndEditCell(int row, int col) {
		
		editCellAt(row, col);
		changeSelection(row, col, false, false);
	}
	
	public void changeSelection(int rowIndex,
                            int columnIndex,
                            boolean toggle,
														boolean extend) {
															
		
		if(rowIndex != currentRow || columnIndex != currentCol) {
			
			if(editors != null && currentCol >= 0 && currentCol < editors.length && editors[currentCol] != null) {
				if(editors[currentCol] instanceof JTextField) {
					
					InputVerifier iv = ((JTextField)editors[currentCol]).getInputVerifier();
					JTextField jtf = getTextFieldEditor(currentRow, currentCol);
					if(iv != null) { 
						boolean res = iv.verify(jtf);
						if(!res) { 
							//jtf.requestFocus();
							this.editCellAt(currentRow, currentCol);
							//super.changeSelection(currentRow, currentCol, false, false);
							return;
						}
					}
					
				}
			}
		}
		currentRow = rowIndex;
		currentCol = columnIndex;
		super.changeSelection(rowIndex, columnIndex, toggle, extend);
		
	}
	
	
}
