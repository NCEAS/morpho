/**
 *  '$RCSfile: CustomList.java,v $'
 *    Purpose: A class that handles xml messages passed by the 
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: brooke $'
 *     '$Date: 2003-09-04 23:41:10 $'
 * '$Revision: 1.10 $'
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

import java.awt.Component;
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
   *                  the default editing widget for the cells in each column. 
   *                  For example,<code><pre>
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
    doEnablesDisables(0);
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
    
    table = new CustomJTable(rowsData, colNamesVec);
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
    
    final JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(java.awt.Color.white);
    scrollPane.getViewport().addChangeListener(new ChangeListener() {
    
      public void stateChanged(ChangeEvent e) {
      
        setColumnSizes(scrollPane.getSize().getWidth());
      }
    });
    this.add(scrollPane, BorderLayout.CENTER);

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
          
          ListSelectionModel lsModel = ((ListSelectionModel)(e.getSource()));
                
          doEnablesDisables(lsModel.getMaxSelectionIndex());
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
            column.setCellEditor(new DefaultCellEditor((JTextField)editor));
          
          } else if (editor instanceof JCheckBox) {
        
            Log.debug(45, "(JCheckBox)");
            column.setCellEditor(new DefaultCellEditor((JCheckBox)editor));

          } else if (editor instanceof JComboBox) {
        
            Log.debug(45, "(JComboBox)");
            column.setCellEditor(new DefaultCellEditor((JComboBox)editor));
          
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
    final double  minFactor = 0.7;
    final double  maxFactor = 5;
    
    for (int i = 0; i < table.getColumnCount(); i++) {
    
      TableColumn column = table.getColumnModel().getColumn(i);
      
      int preferredWidth = (int)(tableWidth*fraction) - 2;
      if (preferredWidth <150) preferredWidth = 150;
      
      int minimumWidth   = (int)(preferredWidth*minFactor);
      int maximumWidth   = (int)(preferredWidth*maxFactor);

      column.setPreferredWidth(preferredWidth);
      column.setMinWidth(minimumWidth);
      column.setMaxWidth(maximumWidth);
    }
  }

  
  private void initButtons() {
  
    Box buttonBox = Box.createVerticalBox();

    if (showAddButton) {
      
      addButton      = new JButton(addAction);
      WidgetFactory.setPrefMaxSizes(addButton, WizardSettings.LIST_BUTTON_DIMS);
      addButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(addButton);
    }
    
    if (showEditButton) {
      
      editButton     = new JButton(new EditAction(table, this));
      WidgetFactory.setPrefMaxSizes(editButton, WizardSettings.LIST_BUTTON_DIMS);
      editButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(editButton);
    }
    
    if (showDuplicateButton) {
      
      duplicateButton = new JButton(new DuplicateAction(table, this));
      WidgetFactory.setPrefMaxSizes(duplicateButton, WizardSettings.LIST_BUTTON_DIMS);
      duplicateButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(duplicateButton);
    }
    
    
    
    if (showDeleteButton) {
      
      deleteButton   = new JButton(new DeleteAction(table, this));
      WidgetFactory.setPrefMaxSizes(deleteButton, WizardSettings.LIST_BUTTON_DIMS);
      deleteButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(deleteButton);
    }
    
    if (showMoveUpButton) {
      
      moveUpButton   = new JButton(new MoveUpAction(table, this));
      WidgetFactory.setPrefMaxSizes(moveUpButton, WizardSettings.LIST_BUTTON_DIMS);
      moveUpButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(moveUpButton);
    }
    
    if (showMoveDownButton) {
      
      moveDownButton = new JButton(new MoveDownAction(table, this));
      WidgetFactory.setPrefMaxSizes(moveDownButton, WizardSettings.LIST_BUTTON_DIMS);
      moveDownButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(moveDownButton);
    }
    buttonBox.add(Box.createGlue());
    this.add(buttonBox, BorderLayout.EAST);
  }

 
  
  private boolean selectionExists = true;
  //
  private void doEnablesDisables(int selRow) {
  
    Log.debug(45, "\n\n>>>> doEnablesDisables(): selRow = "+selRow);
    
    selectionExists = (selRow > -1);

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
                                        && selRow > 0);
    
    // MOVE DOWN available only if a row selected *and* it's not last row:
    if (showMoveDownButton) moveDownButton.setEnabled(selectionExists 
                                        && selRow < (table.getRowCount() - 1));
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
   *  if no row is selected
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
    if (table.getEditorComponent()!=null) {
      table.editingStopped(new ChangeEvent(table.getEditorComponent()));
    }
    table.tableChanged(getTableModelEvent());
    Component comp = table.getComponentAt(row, 0);
    if (comp!=null) {
      table.editCellAt(row, 0, new EventObject(comp));
      comp.requestFocus();
    }
    table.setRowSelectionInterval(row, row);
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
  
    if (table.getEditorComponent()!=null) {
      table.editingStopped(new ChangeEvent(table.getEditorComponent()));
    }
    table.tableChanged(getTableModelEvent());
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
    
    if  (parentList.getCustomAddAction()==null) parentList.addRow(new Vector());
    else parentList.getCustomAddAction().actionPerformed(null);
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
    if (table.getEditorComponent()!=null) {
      table.editingStopped(new ChangeEvent(table.getEditorComponent()));
    }
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
    if (table.getEditorComponent()!=null) {
      table.editingStopped(new ChangeEvent(table.getEditorComponent()));
    }
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
    int row = table.getSelectedRow();
    if (row < 0) return;
    if (table.getEditorComponent()!=null) {
      table.editingStopped(new ChangeEvent(table.getEditorComponent()));
    }
    
    parentList.removeRow(row);

  
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
    int row = table.getSelectedRow();
    if (row < 0) return;
    if (table.getEditorComponent()!=null) {
      table.editingStopped(new ChangeEvent(table.getEditorComponent()));
    }
    model.moveRow(row, row, row - 1);
    table.tableChanged(parentList.getTableModelEvent());
    table.setRowSelectionInterval(row - 1, row - 1);
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
    int row = table.getSelectedRow();
    if (row < 0) return;
    if (table.getEditorComponent()!=null) {
      table.editingStopped(new ChangeEvent(table.getEditorComponent()));
    }
    model.moveRow(row, row, row + 1);
    table.tableChanged(parentList.getTableModelEvent());
    table.setRowSelectionInterval(row + 1, row + 1);
  }
}


class CustomJTable extends JTable  {

//    EditableStringRenderer    editableStringRenderer 
//                                    = new EditableStringRenderer();
    DefaultTableCellRenderer  defaultRenderer 
                                    = new DefaultTableCellRenderer();
    
    DefaultCellEditor         defaultCellEditor 
                                    = new DefaultCellEditor(new JTextField());
    boolean[] columnsEditableFlags;
    
    public CustomJTable(Vector rowVect, Vector colNamesVec) {
    
      super(rowVect, colNamesVec);
      
      super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      defaultCellEditor.setClickCountToStart(1);
      
      columnsEditableFlags = new boolean[colNamesVec.size()];
      Arrays.fill(columnsEditableFlags, true);
    }
    
    //override super
    public TableCellRenderer getCellRenderer(int row, int col) {
    
        Class colClass = getModel().getColumnClass(col);
//        Log.debug(45, "getCellRenderer(): colClass.getName() = "+colClass.getName());
        // can test for surrogates here////

//        if (colClass.getName().equals("java.lang.String")) return editableStringRenderer;
        return defaultRenderer;
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