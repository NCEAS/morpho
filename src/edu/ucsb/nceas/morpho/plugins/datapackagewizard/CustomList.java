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
 *     '$Date: 2003-12-16 01:29:18 $'
 * '$Revision: 1.29 $'
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

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.morpho.util.Log;

import java.util.EventObject;
import java.util.Vector;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.Box;
import javax.swing.AbstractAction;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.DefaultCellEditor;
import javax.swing.ListSelectionModel;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;


/**
 *  Interface   CustomList
 *
 */

public class CustomList extends JPanel {


  public static final short NULL                = 0;
  public static final short EMPTY_STRING_TRIM   =10;
  public static final short EMPTY_STRING_NOTRIM =20;
  public static final short IGNORE              =30;

  public static final short OR                  =51;
  public static final short AND                 =61;

  private CustomJTable table;

  private JButton addButton;
  private JButton editButton;
  private JButton duplicateButton;
  private JButton deleteButton;
  private JButton moveUpButton;
  private JButton moveDownButton;
  private double  tableWidthReference;

  private boolean showAddButton;
  private boolean showEditButton;
  private boolean showDuplicateButton;
  private boolean showDeleteButton;
  private boolean showMoveUpButton;
  private boolean showMoveDownButton;

  protected static  AddAction     addAction;
  private static  TableModelEvent tableModelEvent;
  private DefaultTableModel model;
  private Dimension   buttonDims;
  private JScrollPane scrollPane;

  // these Actions are optional, but are defined by the caller to provide
  // custom functionality for the list buttons
  private Action customAddAction;
  private Action customEditAction;
  private Action customDuplicateAction;
  private Action customDeleteAction;
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
                    boolean showAddButton,        boolean showEditButton,
                    boolean showDuplicateButton,  boolean showDeleteButton,
                    boolean showMoveUpButton,     boolean showMoveDownButton) {

    this.showAddButton        = showAddButton;
    this.showEditButton       = showEditButton;
    this.showDuplicateButton  = showDuplicateButton;
    this.showDeleteButton     = showDeleteButton;
    this.showMoveUpButton     = showMoveUpButton;
    this.showMoveDownButton   = showMoveDownButton;

    init(colNames, displayRows, columnEditors);
  }

  private void init(String[] colNames, int displayRows,
                                                      Object[] columnEditors) {

    this.setLayout(new BorderLayout());


    initList(colNames, displayRows, columnEditors);

    initButtons();
    doEnablesDisables(new int[] {-1});
  }

  private void initList(String[] colNames, int displayRows,
                                                      Object[] columnEditors) {

    Vector colNamesVec = new Vector(colNames.length + 1);

    for (int i=0; i<colNames.length; i++) {

      colNamesVec.add(i, colNames[i]);
    }
    // The last column is never displayed, but is used to hold a pointer to any
    // Object the user wants to associate with the row
    colNamesVec.add(colNames.length, "*UserObject*");

    Vector rowsData = new Vector();

    table = new CustomJTable(rowsData, colNamesVec, columnEditors);
    model = (DefaultTableModel)(table.getModel());
    table.setColumnSelectionAllowed(false);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setShowHorizontalLines(false);
    table.setShowVerticalLines(true);

    // The last column is never displayed, but is used to hold a pointer to any
    // Object the user wants to associate with the row

    TableColumnModel columnModel = table.getColumnModel();
    columnModel.removeColumn(columnModel.getColumn(table.getColumnCount() - 1));

    /////////////////////////////////

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

    this.setBorder(new EmptyBorder(0, 0, 
													//WizardSettings.COMPONENT_PADDING,
                                       2*WizardSettings.COMPONENT_PADDING, 0));
                                       //WizardSettings.COMPONENT_PADDING));

    addAction = new AddAction(table, this);
//    addAction.addRowNoCustomAction();

//    if (table.getComponentAt(0, 0)!=null) {
//      table.editCellAt(0, 0, new EventObject(table.getComponentAt(0, 0)));
//    }
//    table.setRowSelectionInterval(0, 0);

    table.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {

        public void valueChanged(ListSelectionEvent e) {

          if (e.getValueIsAdjusting()) return;

          Log.debug(45,"ListSelectionListener::valueChanged():");
          Log.debug(45,"      - ListSelectionEvent first index  = "+e.getFirstIndex());
          Log.debug(45,"      - ListSelectionEvent last  index  = "+e.getLastIndex());
          Log.debug(45,"      - getSelectedRowIndex  = "+getSelectedRowIndex());
          doEnablesDisables(table.getSelectedRows());
        }
      });

    if (columnEditors==null)  {

      Log.debug(45,"NULL Column Editor Array; making ALL columns non-editable");
      for (int colIdx=0; colIdx<table.getColumnCount(); colIdx++) {
        table.makeColumnNotEditable(colIdx);
      }

    } else {

      for (int i=0; i<columnEditors.length; i++) {

        if (i>table.getColumnCount() - 1) continue;

        Object editor = columnEditors[i];
        TableColumn column = table.getColumnModel().getColumn(i);

        // if editor null, make non-editable:
        if (editor==null) {

          Log.debug(45, "\nNULL Column Editor; making column "+i+" non-editable");
          table.makeColumnNotEditable(i);

        } else {

          Log.debug(45, "\nsetting Column "+i+" Editor = "+editor);
          if (editor instanceof JTextField) {

            Log.debug(45, "(JTextField)");
            DefaultCellEditor cellEd = new DefaultCellEditor(new JTextField());
            cellEd.setClickCountToStart(1);
            column.setCellEditor(cellEd);

          } else if (editor instanceof JCheckBox) {

            Log.debug(45, "(JCheckBox)");
            DefaultCellEditor cellEd = new DefaultCellEditor((JCheckBox)editor);
            cellEd.setClickCountToStart(1);
            column.setCellEditor(cellEd);

          } else if (editor instanceof JComboBox) {

            Log.debug(45, "(JComboBox)");
            DefaultCellEditor cellEd = new DefaultCellEditor((JComboBox)editor);
            cellEd.setClickCountToStart(1);
            column.setCellEditor(cellEd);

          } else {

            Log.debug(45, "(NOT RECOGNIZED - SETTING NON-EDITABLE)");
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

    if (tableModelEvent==null) {
      tableModelEvent = new TableModelEvent(table.getModel());
    }
    return tableModelEvent;
  }


  private void setColumnSizes(double tableWidth) {

    final double  fraction  = 1d/((double)(table.getColumnCount()));
    final double  minFactor = 0.5;
    final double  maxFactor = 2;

    for (int i = 0; i < table.getColumnCount(); i++) {

      TableColumn column = table.getColumnModel().getColumn(i);

      int preferredWidth = (int)(tableWidth*fraction) - 1;
      int headerWidth = getHeaderWidth(i, column);
      if (preferredWidth < headerWidth) preferredWidth = headerWidth;

      column.setPreferredWidth(preferredWidth);
      column.setMinWidth((int)(preferredWidth*minFactor));
      column.setMaxWidth((int)(preferredWidth*maxFactor));
    }
  }

	public void setColumnWidthPercentages(double columnWidths[])
	{
		final double  minFactor = 0.5;
    final double  maxFactor = 2;
		int i;
		int len = columnWidths.length;
		if(len != table.getColumnCount()) {
			System.out.println("lengths not equals");
			return;
		}
			
		double sum = 0;
		for(i = 0; i< len; i++)  
			sum += columnWidths[i];
		
		// total percentage must sum to 100 %.
		if(sum != 100.0) {
			System.out.println("Sum not equal to 100%");
			return;
		}
		
		double totalWidth = scrollPane.getViewport().getSize().getWidth();
		for(i=0; i < len; i++)
		{
			TableColumn column = table.getColumnModel().getColumn(i);
			int width = (int) (((double)columnWidths[i] / 100.0) * totalWidth);
			System.out.println("Setting width of column " + i + " = " + width +" , totalwidth =" + totalWidth);
			column.setPreferredWidth(width);
			column.setMinWidth((int)width);
      column.setMaxWidth((int)width);
		}
	}

  private int getHeaderWidth(int colNumber, TableColumn column) {

    TableCellRenderer headerRenderer = column.getHeaderRenderer();
    if (headerRenderer==null) {
      headerRenderer = table.getTableHeader().getDefaultRenderer();
    }
    Component comp = headerRenderer.getTableCellRendererComponent(
                                                table, column.getHeaderValue(),
                                                false, false, -1, colNumber);
    int headerWidth = comp.getPreferredSize().width;
    Insets ins = ((JComponent)headerRenderer).getInsets();
    return (ins.left + headerWidth + ins.right);
  }


  public void setListButtonDimensions(Dimension dims) {

    if (dims==null) buttonDims = WizardSettings.LIST_BUTTON_DIMS;
    else buttonDims = dims;
    resizeButtons();
  }

  private void initButtons() {

    Box buttonBox = Box.createVerticalBox();

    buttonBox.setBorder(new EmptyBorder(0,WizardSettings.COMPONENT_PADDING,
                                        WizardSettings.COMPONENT_PADDING,
                                        WizardSettings.COMPONENT_PADDING));

    if (showAddButton) {

      addButton      = new JButton(addAction);
      addButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(addButton);
      buttonBox.add(WidgetFactory.makeHalfSpacer());
    }

    if (showEditButton) {

      editButton     = new JButton(new EditAction(table, this));
      editButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(editButton);
      buttonBox.add(WidgetFactory.makeHalfSpacer());
    }

    if (showDuplicateButton) {

      duplicateButton = new JButton(new DuplicateAction(table, this));
      duplicateButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(duplicateButton);
      buttonBox.add(WidgetFactory.makeHalfSpacer());
    }

    if (showDeleteButton) {

      deleteButton   = new JButton(new DeleteAction(table, this));
      deleteButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(deleteButton);
      buttonBox.add(WidgetFactory.makeHalfSpacer());
    }

    if (showMoveUpButton) {

      moveUpButton   = new JButton(new MoveUpAction(table, this));
      moveUpButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(moveUpButton);
      buttonBox.add(WidgetFactory.makeHalfSpacer());
    }

    if (showMoveDownButton) {

      moveDownButton = new JButton(new MoveDownAction(table, this));
      moveDownButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(moveDownButton);
      buttonBox.add(WidgetFactory.makeHalfSpacer());
    }

    setListButtonDimensions(WizardSettings.LIST_BUTTON_DIMS);
    resizeButtons();

    buttonBox.add(Box.createGlue());
    this.add(buttonBox, BorderLayout.EAST);
  }


  private void resizeButtons() {

    if (showAddButton) WidgetFactory.setPrefMaxSizes(addButton, buttonDims);
    if (showEditButton) WidgetFactory.setPrefMaxSizes(editButton, buttonDims);
    if (showDuplicateButton) WidgetFactory.setPrefMaxSizes(duplicateButton, buttonDims);
    if (showDeleteButton) WidgetFactory.setPrefMaxSizes(deleteButton, buttonDims);
    if (showMoveUpButton) WidgetFactory.setPrefMaxSizes(moveUpButton, buttonDims);
    if (showMoveDownButton) WidgetFactory.setPrefMaxSizes(moveDownButton, buttonDims);
  }



  private boolean selectionExists = true;
  //
  private void doEnablesDisables(int[] selRows) {

    Log.debug(45, "\n\n>>>> doEnablesDisables(): selRows count = "+selRows.length);

    selectionExists = (selRows.length > 0);

    // ADD always available:
    //if (showAddButton) addButton.setEnabled(true);

    // EDIT available only if a row selected:
    if (showEditButton) editButton.setEnabled(selectionExists);

    // DUPLICATE available only if a row selected:
    if (showDuplicateButton) duplicateButton.setEnabled(selectionExists);

    // DELETE available only if a row selected:
    if (showDeleteButton) deleteButton.setEnabled(selectionExists);

    // MOVE UP available only if a row selected *and* it's not row 0:
    if (showMoveUpButton) moveUpButton.setEnabled(selectionExists
                                        && selRows[0] > 0);

    // MOVE DOWN available only if a row selected *and* it's not last row:
    if (showMoveDownButton) moveDownButton.setEnabled(selectionExists
                              		&& selRows[selRows.length-1] < (table.getRowCount() - 1));
  }




  /**
   *  Tells the list to stop editing and commit all edited values to the model.
   *  Typically called by a wizard page when next> is pressed, so all changes
   *  are committed before validation.
   */
  public void fireEditingStopped() {

    if (table.getEditorComponent()!=null) {
      table.editingStopped(new ChangeEvent(table.getEditorComponent()));
    }
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

    List listOfRowLists = this.getListOfRowLists();
    if (listOfRowLists==null) return null;

    int selRow = this.getSelectedRowIndex();
    if (selRow < 0) return null;

    Object rowObj = listOfRowLists.get(selRow);
    if (rowObj==null) return null;

    return (List)rowObj;
  }



  /**
   *  replaces the currently-selected row with the Objects in the List provided,
   *  or with a blank row if the List is null
   *
   *  @param  the List containing the objects in the new row that will replace
   *          the currently-selected row.
   */
  public void replaceSelectedRow(List newRow) {

    if (newRow==null) newRow = new ArrayList();

    int selRow = this.getSelectedRowIndex();
    if (selRow < 0) return;

    removeRow(selRow);
    model.insertRow(selRow, newRow.toArray());
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
   *  adds the row to the list after the currently-selected row, or at the end
   *  if no row is selected. Then scrolls to make the new row visible.
   *
   *  @param  the List defining the row data. If the list has "n" columns, the
   *          passed List may have 0 -> (n+1) elements as follows:
   *          <ul><li>0..n represents the actual data that will appear displayed
   *            in the list columns. If the number of elements is less than the
   *          number of display columns, the remaining columns will appear blank
   *          </li><li>List element (n+1) is the optional "user object", which
   *            may be any object that the user wants to associate with this row
   *            </li>
   */
  public void addRow(List rowList) {

    int row = getSelectedRowIndex();

    if (row < 0) {

      row = model.getRowCount();
      model.addRow(rowList.toArray());

    } else {

      model.insertRow(++row, rowList.toArray());
    }
    fireEditingStopped();
    table.tableChanged(getTableModelEvent());
    Component comp = table.getComponentAt(row, 0);
    if (comp!=null) {
      table.editCellAt(row, 0, new EventObject(comp));
      comp.requestFocus();
    }
    table.scrollRectToVisible(table.getCellRect(row,0,true));
    table.setRowSelectionInterval(row, row);
    table.validate();
    table.repaint();

  }


  /**
   *  removes the row with the specified index from the list
   *
   *  @param the index of the row to be removed
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
	  for(int i = 0; table.getRowCount() > 0 && i<10;i++)
		  model.removeRow(0);

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

    if (conditions==null) return;
    if (logicMode!=OR && logicMode!=AND) return;

    List rowLists = this.getListOfRowLists();

    boolean[] rowsToDelete  = new boolean[rowLists.size()];

    Arrays.fill(rowsToDelete, false);

    int rowNumber = -1;

    for (Iterator it = rowLists.iterator(); it.hasNext(); ) {

      rowNumber++;

      Object nextRowObj = it.next();

      if (nextRowObj==null) {

        rowsToDelete[rowNumber] = true;
        continue;
      }

      List nextRow = (List)nextRowObj;

      if (nextRow.size() < 1) {

        rowsToDelete[rowNumber] = true;
        continue;
      }

      if (logicMode==OR) checkColumnContents_OR(  nextRow,      conditions,
                                                  rowsToDelete, rowNumber );
      else               checkColumnContents_AND( nextRow,      conditions,
                                                  rowsToDelete, rowNumber );
    }
    // remove rows to be deleted IN REVERSE ORDER - since each removal
    // reduces the number of rows
    for (int i=rowsToDelete.length - 1; i > -1; i--) {

      if (rowsToDelete[i]) this.removeRow(i);
    }
  }


  private void checkColumnContents_OR(List nextRow, short[]   conditions,
                                      boolean[] rowsToDelete, int rowNumber) {

    for (int colIdx=0; colIdx<conditions.length; colIdx++) {

      switch (conditions[colIdx]) {

        case IGNORE:
          break;

        case NULL:
          if (nextRow.get(colIdx)==null) rowsToDelete[rowNumber] = true;
          break;

        case EMPTY_STRING_NOTRIM:
          if (  (nextRow.get(colIdx)==null)
            || !(nextRow.get(colIdx) instanceof String)
            || ((String)(nextRow.get(colIdx))).equals("") ) {

            rowsToDelete[rowNumber] = true;
          }
          break;

        case EMPTY_STRING_TRIM:
          if (  (nextRow.get(colIdx)==null)
            || !(nextRow.get(colIdx) instanceof String)
            || ((String)(nextRow.get(colIdx))).trim().equals("") ) {

            rowsToDelete[rowNumber] = true;
          }
          break;
      }
    }
  }


  private void checkColumnContents_AND(List nextRow, short[]   conditions,
                                       boolean[] rowsToDelete, int rowNumber) {

    boolean result  = true;
    int ignoreCount = 0;

    for (int colIdx=0; colIdx<conditions.length; colIdx++) {

      switch (conditions[colIdx]) {

        case IGNORE:
          //check to ensure we dont have ALL conditions == "IGNORE"...
          ignoreCount++;
          break;

        case NULL:
          result = result && (nextRow.get(colIdx)==null);
          break;

        case EMPTY_STRING_NOTRIM:
          result = result &&
            (  (nextRow.get(colIdx)==null)
            || !(nextRow.get(colIdx) instanceof String)
            || ((String)(nextRow.get(colIdx))).equals("") );
          break;

        case EMPTY_STRING_TRIM:
          result = result &&
            (  (nextRow.get(colIdx)==null)
            || !(nextRow.get(colIdx) instanceof String)
            || ((String)(nextRow.get(colIdx))).trim().equals("") );
          break;
      }
    }
    //check to ensure we dont have ALL conditions == "IGNORE"...
    if (ignoreCount>=conditions.length) result  = false;

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
    return (List)(model.getDataVector());
  }




  /**
   *  Sets the <code>javax.swing.Action</code> to be executed on pressing the
   *  ADD button. NOTE that if no custom add action is set, or if a null action
   *  is set, the ADD button's 'private' Action (defined elsewhere in this
   *  class) will be executed; otherwise the custom action will be executed (and
   *  the 'private' Action will NOT be executed).
   *  <em>Note that this behavior is different for the other custom action
   *  get/set methods, which are executed IN ADDITION to private actions</em>
   *
   *  @param the <code>javax.swing.Action</code> to be executed
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
   *  Sets the <code>javax.swing.Action</code> to be executed on pressing the
   *  appropriate list button. NOTE that the button's 'private' Action (defined
   *  elsewhere in this class) will be executed first, and then the custom
   *  action will be executed
   *
   *  @param the <code>javax.swing.Action</code> to be executed
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
   *  Sets the <code>javax.swing.Action</code> to be executed on pressing the
   *  appropriate list button. NOTE that the button's 'private' Action (defined
   *  elsewhere in this class) will be executed first, and then the custom
   *  action will be executed
   *
   *  @param the <code>javax.swing.Action</code> to be executed
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
   *  Sets the <code>javax.swing.Action</code> to be executed on pressing the
   *  appropriate list button. NOTE that the button's 'private' Action (defined
   *  elsewhere in this class) will be executed first, and then the custom
   *  action will be executed
   *
   *  @param the <code>javax.swing.Action</code> to be executed
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

}



////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
//                       A C T I O N   C L A S S E S
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////


class AddAction extends AbstractAction {

  private CustomJTable table;
  private CustomList parentList;
  private DefaultTableModel model;

  public AddAction(CustomJTable table, CustomList parentList) {

    super("Add");
    this.table = table;
    this.parentList = parentList;
    model = (DefaultTableModel)(table.getModel());
  }


  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList ADD action");

    if (parentList.getCustomAddAction()==null) {

      List newRowList = new ArrayList();

      for (int i=0; i < table.getColumnCount(); i++) {

        TableCellEditor cellEditor
                          = table.getColumnModel().getColumn(i).getCellEditor();
        String cellVal = (cellEditor!=null)?
                          String.valueOf(cellEditor.getCellEditorValue()) : "";
        String colClassName = table.getColumnClass(i).getName();

        if (colClassName.equals("javax.swing.JTextField")) {

          Log.debug(45, "\nAddAction - (JTextField)");
          newRowList.add("");

        } else if (colClassName.equals("javax.swing.JCheckBox")) {

          Log.debug(45, "\nAddAction - (JCheckBox)");
          newRowList.add(new Boolean(cellVal));

        } else if (colClassName.equals("javax.swing.JComboBox")) {

          Log.debug(45, "\nAddAction - (JComboBox)");
          newRowList.add(cellVal);

        } else {

          Log.debug(45, "\nAddAction - NOT RECOGNIZED");
          newRowList.add("");
        }
      }
      parentList.addRow(newRowList);

    } else parentList.getCustomAddAction().actionPerformed(null);
  }
}


class EditAction extends AbstractAction {

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
    if (row < 0) return;
    parentList.fireEditingStopped();

    // get object here - only a String surrogate is shown by the cell renderer,
    // but the entire object is actually in the table model!

    //execute the user's custom action:
    if (parentList.getCustomEditAction()!=null) {
      parentList.getCustomEditAction().actionPerformed(null);
    }
  }
}




class DuplicateAction extends AbstractAction {

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
    if (row < 0) return;
    parentList.fireEditingStopped();

    // get object here - only a String surrogate is shown by the cell renderer,
    // but the entire object is actually in the table model!


    //execute the user's custom action:
    if (parentList.getCustomDuplicateAction()!=null) {
      parentList.getCustomDuplicateAction().actionPerformed(null);
    }
  }
}




class DeleteAction extends AbstractAction {

  private CustomJTable table;
  private CustomList parentList;
  private DefaultTableModel model;
  private AddAction addAction;

  public DeleteAction(CustomJTable table, CustomList parentList) {

    super("Delete");
    this.table = table;
    this.parentList = parentList;
    model = (DefaultTableModel)(table.getModel());
  }

  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList DELETE action");
    int[] rows = table.getSelectedRows();
    if (rows.length == 0) return;
		
    parentList.fireEditingStopped();
		
		for(int i = 0; i< rows.length; i++)
			parentList.removeRow(rows[i] - i);


    //execute the user's custom action:
    if (parentList.getCustomDeleteAction()!=null) {
      parentList.getCustomDeleteAction().actionPerformed(null);
    }
  }

//  private void addFirstRowBack() {
//
//    if (addAction==null) addAction = new AddAction(table, parentList);
//    CustomList.addAction.addRowNoCustomAction();
//  }
}

class MoveUpAction extends AbstractAction {

  private CustomJTable table;
  private CustomList parentList;
  private DefaultTableModel model;

  public MoveUpAction(CustomJTable table, CustomList parentList) {

    super("Move Up");
    this.table = table;
    this.parentList = parentList;
    model = (DefaultTableModel)(table.getModel());
  }

  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList MOVE UP action");
    int rows[] = table.getSelectedRows();
    if (rows.length == 0) return;
    
		parentList.fireEditingStopped();
		
		for(int i = 0; i < rows.length; i++)
			model.moveRow(rows[i], rows[i], rows[i] - 1);
    table.tableChanged(parentList.getTableModelEvent());
    //table.setRowSelectionInterval(row - 1, row - 1);
    table.repaint();
  }
}

class MoveDownAction extends AbstractAction {

  private CustomJTable table;
  private CustomList parentList;
  private DefaultTableModel model;

  public MoveDownAction(CustomJTable table, CustomList parentList) {

    super("Move Down");
    this.table = table;
    this.parentList = parentList;
    model = (DefaultTableModel)(table.getModel());
  }

  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList MOVE DOWN action");
    int rows[] = table.getSelectedRows();
    if (rows.length < 0) return;
		
    parentList.fireEditingStopped();
		
		for (int i = rows.length-1 ; i >= 0; i--)
			model.moveRow(rows[i], rows[i], rows[i] + 1);
    table.tableChanged(parentList.getTableModelEvent());
    //table.setRowSelectionInterval(row + 1, row + 1);
  }
}


class CustomJTable extends JTable  {

//    EditableStringRenderer    editableStringRenderer
//                                    = new EditableStringRenderer();
    private DefaultTableCellRenderer defaultRenderer
                                      = new DefaultTableCellRenderer();

    private DefaultCellEditor defaultCellEditor
                                      = new DefaultCellEditor(new JTextField());
    Object[] editors;

    boolean[] columnsEditableFlags;

    public CustomJTable(Vector rowVect, Vector colNamesVec, Object[] editors) {

      super(new DefaultTableModel(rowVect, colNamesVec));
      this.editors = editors;
      super.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      defaultCellEditor.setClickCountToStart(1);

      columnsEditableFlags = new boolean[colNamesVec.size()];
      Arrays.fill(columnsEditableFlags, true);
    }

    //override super
    public TableCellRenderer getCellRenderer(int row, int col) {

      Class colClass = null;

      if (editors!=null && editors[col]!=null) colClass = editors[col].getClass();

      Log.debug(45, "\nCustomJTable.getCellRenderer(): colClass.getName() = "
                                                                    +colClass);
      if (colClass==null) return defaultRenderer;

/////// JCHECKBOX //////////////////////////////////////////////////////////////
      if (colClass.getName().equals("javax.swing.JCheckBox")) {
        return new TableCellRenderer() {

            public Component getTableCellRendererComponent( JTable table,
                                                            Object value,
                                                            boolean isSelected,
                                                            boolean hasFocus,
                                                            int row,
                                                            int column) {

              boolean checked = false;
              if (value!=null) checked = ((Boolean)value).booleanValue();
              JCheckBox cell = new JCheckBox("", checked);
              if (cell.getForeground()==null) cell.setForeground(table.getForeground());
              if (cell.getBackground()==null) cell.setBackground(table.getBackground());
              if (isSelected) cell.setForeground(table.getSelectionForeground());
              if (hasFocus) {
                if (table.isCellEditable(row,column)) {
                    cell.setForeground(table.getSelectionForeground());
                    cell.setBackground(table.getSelectionBackground());
                }
              }
              if (cell.getFont()==null) cell.setFont(table.getFont());
              cell.setOpaque(true);
              cell.validate();
              cell.repaint();
              return cell;
            }
          };
      }

/////// JCOMBOBOX //////////////////////////////////////////////////////////////
      if (colClass.getName().equals("javax.swing.JComboBox")) {

        JComboBox origList = (JComboBox)(editors[col]);

        ListModel model = origList.getModel();

        int listLength = model.getSize();

        final Object[] listElemArray = new Object[listLength];

        for (int i = 0; i < listLength; i++) {

          listElemArray[i] = model.getElementAt(i);
        }

        return new TableCellRenderer() {

            public Component getTableCellRendererComponent( JTable table,
                                                            Object value,
                                                            boolean isSelected,
                                                            boolean hasFocus,
                                                            int row,
                                                            int column) {

              JComboBox cell = new JComboBox(listElemArray);

              if (value!=null) cell.setSelectedItem(value);
              else cell.setSelectedIndex(0);

              if (cell.getFont()==null) cell.setFont(table.getFont());

              cell.validate();
              cell.repaint();
              return cell;
            }
          };
      }
			if (colClass.getName().equals("javax.swing.JLabel")) {
	       final JLabel origLabel = (JLabel)(editors[col]);
				 return new TableCellRenderer() {

            public Component getTableCellRendererComponent( JTable table,
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

      return defaultRenderer;
    }


    //override super
    public Class getColumnClass(int col) {

      Class colClass = java.lang.String.class;

      if (editors[col]!=null) colClass = editors[col].getClass();

      Log.debug(45, "\nCustomJTable.getColumnClass("+col+"): "
                                                          + colClass.getName());
      return colClass;
    }

    //override super
    public boolean getDragEnabled() { return false; }

    //override super
    public int getSelectedRow() {

      return super.getSelectionModel().getMaxSelectionIndex();
    }

    public void makeColumnNotEditable(int col) {

      columnsEditableFlags[col] = false;
    }

    public boolean isCellEditable(int row, int col) {

      return columnsEditableFlags[col];
    }

}