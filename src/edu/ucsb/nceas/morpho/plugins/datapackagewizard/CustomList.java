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
 *     '$Date: 2003-08-30 03:02:31 $'
 * '$Revision: 1.6 $'
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

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

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
  private JButton deleteButton;
  private JButton moveUpButton;
  private JButton moveDownButton;
  private double  tableWidthReference;

  private boolean showAddButton;
  private boolean showEditButton;
  private boolean showDeleteButton;
  private boolean showMoveUpButton;
  private boolean showMoveDownButton;
  
  protected static  AddAction       addAction;
  private static  TableModelEvent tableModelEvent;
  

  
  public CustomList(String[] colNames, Object[] columnEditors, int displayRows, 
                    boolean showAddButton, 
                    boolean showEditButton,       boolean showDeleteButton, 
                    boolean showMoveUpButton,     boolean showMoveDownButton) { 
  
    this.showAddButton      = showAddButton;
    this.showEditButton     = showEditButton;
    this.showDeleteButton   = showDeleteButton;
    this.showMoveUpButton   = showMoveUpButton;
    this.showMoveDownButton = showMoveDownButton;
    
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
    
    Vector colNamesVec = new Vector(colNames.length);
    
    for (int i=0; i<colNames.length; i++) {
    
      colNamesVec.add(i, colNames[i]);
    }
    
    Vector rowsData = new Vector();
    
    table = new CustomJTable(rowsData, colNamesVec);
         
    table.setColumnSelectionAllowed(false);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setShowHorizontalLines(false);
    table.setShowVerticalLines(true);
    
    final JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(java.awt.Color.white);
    scrollPane.getViewport().addChangeListener(new ChangeListener() {
    
      public void stateChanged(ChangeEvent e) {
      
        setColumnSizes(scrollPane.getSize().getWidth());
      }
    });
    this.add(scrollPane, BorderLayout.CENTER);

    tableModelEvent = new TableModelEvent(table.getModel());
      
    addAction = new AddAction(table);
    addAction.actionPerformed(null);

    if (table.getComponentAt(0, 0)!=null) {
      table.editCellAt(0, 0, new EventObject(table.getComponentAt(0, 0)));
    }
    table.setRowSelectionInterval(0, 0);
      
    table.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
      
        public void valueChanged(ListSelectionEvent e) {
        
          if (e.getValueIsAdjusting()) return;
          
          ListSelectionModel lsModel = ((ListSelectionModel)(e.getSource()));
                
          doEnablesDisables(lsModel.getMaxSelectionIndex());
        }
      });
      
    for (int i=0; i<columnEditors.length; i++) {
    
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
          
          Log.debug(45, "(NOT RECOGNIZED)");
          //do nothing for now. do we need other editor types??
        }
        
      }
    }
    
  }
  
  
  
  private void setTableWidthReference(double width) {
  
    tableWidthReference = width;
  }
  
  private double getTableWidthReference() {
  
    return tableWidthReference;
  }
  
  
  public static TableModelEvent getTableModelEvent() {
  
    return tableModelEvent;
  }
  
  
  private void setColumnSizes(double tableWidth) {
  
    final double  fraction  = 1d/((double)(table.getColumnCount()));
    final double  minFactor = 0.7;
    final double  maxFactor = 5;
    
    for (int i = 0; i < table.getModel().getColumnCount(); i++) {
    
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
      
      editButton     = new JButton(new EditAction(table));
      WidgetFactory.setPrefMaxSizes(editButton, WizardSettings.LIST_BUTTON_DIMS);
      editButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(editButton);
    }
    
    if (showDeleteButton) {
      
      deleteButton   = new JButton(new DeleteAction(table));
      WidgetFactory.setPrefMaxSizes(deleteButton, WizardSettings.LIST_BUTTON_DIMS);
      deleteButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(deleteButton);
    }
    
    if (showMoveUpButton) {
      
      moveUpButton   = new JButton(new MoveUpAction(table));
      WidgetFactory.setPrefMaxSizes(moveUpButton, WizardSettings.LIST_BUTTON_DIMS);
      moveUpButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(moveUpButton);
    }
    
    if (showMoveDownButton) {
      
      moveDownButton = new JButton(new MoveDownAction(table));
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
    table.tableChanged(CustomList.getTableModelEvent());
    DefaultTableModel model = (DefaultTableModel)( table.getModel() );
    return (List)(model.getDataVector());
  }
}
  
  
 
 

class AddAction extends AbstractAction {

  private CustomJTable table;
  private DefaultTableModel model;
  
  public AddAction(CustomJTable table) { 
    
    super("Add"); 
    this.table = table;
    model = (DefaultTableModel)(table.getModel());
  }
  
  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList ADD action");
    int row = table.getSelectedRow();
    if (row < 0) {
      row = model.getRowCount();
      model.addRow(new Vector());
    } else {
      model.insertRow(++row, (Vector)null);
    }
    if (table.getEditorComponent()!=null) {
      table.editingStopped(new ChangeEvent(table.getEditorComponent()));
    }
    table.tableChanged(CustomList.getTableModelEvent());
    Component comp = table.getComponentAt(row, 0);
    if (comp!=null) {
      table.editCellAt(row, 0, new EventObject(comp));
      comp.requestFocus();
    }
    table.setRowSelectionInterval(row, row);
  }
}


class EditAction extends AbstractAction {

  private CustomJTable table;
  
  public EditAction(CustomJTable table) { 
    
    super("Edit"); 
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
  }
}

class DeleteAction extends AbstractAction {

  private CustomJTable table;
  private DefaultTableModel model;
  private AddAction addAction;
  
  public DeleteAction(CustomJTable table) { 
    
    super("Delete"); 
    this.table = table;
    model = (DefaultTableModel)(table.getModel());
  }
  
  public void actionPerformed(ActionEvent e) {
  
    Log.debug(45, "CustomList DELETE action");  
    int row = table.getSelectedRow();
    if (row < 0) return;
    if (table.getEditorComponent()!=null) {
      table.editingStopped(new ChangeEvent(table.getEditorComponent()));
    }
    model.removeRow(row);
    if (table.getRowCount() < 1) addFirstRowBack();
    table.tableChanged(CustomList.getTableModelEvent());
    table.clearSelection();
  }
  
  private void addFirstRowBack() {
  
    if (addAction==null) addAction = new AddAction(table);
    CustomList.addAction.actionPerformed(null);
  }
}

class MoveUpAction extends AbstractAction {

  private CustomJTable table;
  private DefaultTableModel model;
  
  public MoveUpAction(CustomJTable table) { 
    
    super("Move Up"); 
    this.table = table;
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
    table.tableChanged(CustomList.getTableModelEvent());
    table.setRowSelectionInterval(row - 1, row - 1);
    table.repaint();
  }
}

class MoveDownAction extends AbstractAction {

  private CustomJTable table;
  private DefaultTableModel model;
  
  public MoveDownAction(CustomJTable table) { 
    
    super("Move Down"); 
    this.table = table;
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
    table.tableChanged(CustomList.getTableModelEvent());
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





//class EditableStringRenderer extends JTextField implements TableCellRenderer {
//
//
//  public EditableStringRenderer() {
//  
//    Dimension prefSize 
//        = new Dimension(1000, this.getFontMetrics(this.getFont()).getHeight());
//    this.setPreferredSize(prefSize);
//  }
//  
//  
//  public Component getTableCellRendererComponent( final JTable table,
//                                                  Object  value,
//                                                  boolean isSelected,
//                                                  boolean hasFocus,
//                                                  int     row,
//                                                  int     col ) {
//                                                
//    if (value==null)                return this;
//    if (!(value instanceof String)) return null;
//    
//    this.setEnabled(table.isEnabled());
//    this.setFont(table.getFont());
//    this.setOpaque(true);
//
//    if (isSelected) {
//      this.setBackground(table.getSelectionBackground());
//      this.setForeground(table.getSelectionForeground());
//    } else {
//      this.setBackground(table.getBackground());
//      this.setForeground(table.getForeground());
//    }
//
//    this.setText((String)value);
//    table.addMouseListener(new MouseAdapter() {
//    
//      public void mouseClicked(MouseEvent e) {
//        Log.debug(45, "\n***** EditableStringRenderer -> mouseClicked");
//        int i = table.getSelectedRow();
//        int j = table.getSelectedColumn();
//        if (i > -1 && j > -1) {
//          table.editCellAt(  
//              i, j, new ChangeEvent(table.getModel().getValueAt(i, j)));
//          
//          Component comp = table.getComponentAt(i, j);
//          
//          if (comp instanceof JTextField) {
//            JTextField field = (JTextField)comp;
//            Log.debug(45, "\n***** EditableStringRenderer -> JTextField text = "+field.getText());
//            field.setCaretPosition(field.getText().length());
//            field.requestFocus();
//          }
//        }
//        
//      }
//    });
//    return this;
//  }
//}
