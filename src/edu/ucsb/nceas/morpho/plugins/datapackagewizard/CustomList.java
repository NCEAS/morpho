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
 *     '$Date: 2003-08-08 00:56:17 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.morpho.util.Log;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Container;
import java.awt.Point;
import java.awt.Dimension;

import javax.swing.BoxLayout;
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

import javax.swing.event.TableModelEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.CellEditorListener;

  

/**
 *  Interface   CustomList
 *
 */

public class CustomList extends JPanel {

  private JTable table;
//  private TableModel tableModel;

  private JButton addButton;
  private JButton editButton;
  private JButton deleteButton;
  private JButton moveUpButton;
  private JButton moveDownButton;
  private double  tableWidthReference;
  

  public CustomList(String[] colNames, int displayRows, boolean showAddButton, 
                    boolean showEditButton,       boolean showDeleteButton, 
                    boolean showMoveUpButton,     boolean showMoveDownButton) { 
  
    init(colNames, displayRows, showAddButton, showEditButton, 
          showDeleteButton, showMoveUpButton, showMoveDownButton); 
  }
    
  private void init(String[] colNames, int displayRows, boolean showAddButton, 
                    boolean showEditButton,       boolean showDeleteButton, 
                    boolean showMoveUpButton,     boolean showMoveDownButton) { 
  
    this.setLayout(new BorderLayout());

    initList(colNames, displayRows);
    
    initButtons(showAddButton,  showEditButton,   showDeleteButton, 
                                showMoveUpButton, showMoveDownButton);
  }
  
  private void initList(String[] colNames, int displayRows) {
    

    Vector colNamesVec = new Vector(colNames.length);
    
    for (int i=0; i<colNames.length; i++) {
    
      colNamesVec.add(i, colNames[i]);
    }
    
    Vector rowsData = new Vector();
    Vector row0Data = new Vector(2);
    row0Data.add("");
    row0Data.add("");
    rowsData.add(row0Data);
    
    table = new CustomJTable(rowsData, colNamesVec);
         
    table.setColumnSelectionAllowed(false);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setShowHorizontalLines(false);
    table.setShowVerticalLines(true);
    table.editCellAt(0, 0, new EventObject(table.getValueAt(0, 0)));
    table.setRowSelectionInterval(0, 0);
    
    final JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(java.awt.Color.white);
    scrollPane.getViewport().addChangeListener(new ChangeListener() {
    
      public void stateChanged(ChangeEvent e) {
      
        setColumnSizes(scrollPane.getSize().getWidth());
      }
    });
    this.add(scrollPane, BorderLayout.CENTER);
  }
  
  private void setTableWidthReference(double width) {
  
    tableWidthReference = width;
  }
  
  private double getTableWidthReference() {
  
    return tableWidthReference;
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

  

  
  private void initButtons( boolean showAddButton,    boolean showEditButton,       
                            boolean showDeleteButton, boolean showMoveUpButton,     
                                                      boolean showMoveDownButton) {
  
    Box buttonBox = Box.createVerticalBox();

    if (showAddButton) {
      
      addButton      = new JButton(new AddAction(table));
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
  
}

 

class AddAction extends AbstractAction {

  private JTable table;
  private DefaultTableModel model;
  
  public AddAction(JTable table) { 
    
    super("Add"); 
    this.table = table;
    model = (DefaultTableModel)(table.getModel());
  }
  
  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList ADD action");
    int row = table.getSelectedRow();
    
    if (row < 0) {
      model.addRow(new Vector());
      row = model.getRowCount() - 1;
    } else {
      model.insertRow(++row, (Vector)null);
    }
    table.tableChanged(new TableModelEvent(model));
    Component comp = table.getComponentAt(row, 0);
    table.editCellAt(row, 0, new EventObject(comp));
    comp.requestFocus();
    table.setRowSelectionInterval(row, row);
  }
}


class EditAction extends AbstractAction {

  private JTable table;
  
  public EditAction(JTable table) { 
    
    super("Edit"); 
    this.table = table;
  }
  
  public void actionPerformed(ActionEvent e) {
  
    Log.debug(45, "CustomList EDIT action");  
    int row = table.getSelectedRow();
    if (row < 0) return;
    // get object here - only a String surrogate is shown by the cell renderer, 
    // but the entire object is actually in the table model!
  }
}

class DeleteAction extends AbstractAction {

  private JTable table;
  private DefaultTableModel model;
  
  public DeleteAction(JTable table) { 
    
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
    table.tableChanged(new TableModelEvent(model));
    table.clearSelection();
  }
}

class MoveUpAction extends AbstractAction {

  private JTable table;
  private DefaultTableModel model;
  
  public MoveUpAction(JTable table) { 
    
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
    table.tableChanged(new TableModelEvent(model));
    table.setRowSelectionInterval(row - 1, row - 1);
    table.repaint();
  }
}

class MoveDownAction extends AbstractAction {

  private JTable table;
  private DefaultTableModel model;
  
  public MoveDownAction(JTable table) { 
    
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
    table.tableChanged(new TableModelEvent(model));
    table.setRowSelectionInterval(row + 1, row + 1);
  }
}


class CustomJTable extends JTable  {

    EditableStringRenderer    editableStringRenderer 
                                    = new EditableStringRenderer();
    DefaultTableCellRenderer  defaultRenderer 
                                    = new DefaultTableCellRenderer();
    
    DefaultCellEditor         defaultCellEditor 
                                    = new DefaultCellEditor(new JTextField());
    
    public CustomJTable(Vector rowVect, Vector colNamesVec) {
    
      super(rowVect, colNamesVec);
      
      super.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      defaultCellEditor.setClickCountToStart(1);
      this.setRowHeight((int)(editableStringRenderer.getPreferredSize().height));
    }
    
    //override super
    public TableCellRenderer getCellRenderer(int row, int col) {
    
        Class colClass = getModel().getColumnClass(col);
        Log.debug(45, "getCellRenderer(): colClass.getName() = "+colClass.getName());
        // can test for surrogates here////

//        if (colClass.getName().equals("java.lang.String")) return editableStringRenderer;
        return defaultRenderer;
    }

    //override super
    public TableCellEditor getCellEditor(int row, int col) {

        Class colClass = getModel().getColumnClass(col);
        Log.debug(45, "getCellEditor(): colClass.getName() = "+colClass.getName());
        // can test for surrogates here////
        
        return defaultCellEditor;
    }




    //override super
    public boolean getDragEnabled() { return false; }

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
