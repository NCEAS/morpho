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
 *     '$Date: 2003-08-06 05:44:18 $'
 * '$Revision: 1.1 $'
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

import java.util.Vector;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Container;
import java.awt.Point;

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
//import javax.swing.table.TableModel;
//import javax.swing.table.DefaultTableModel;

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
    

//    tableModel = new AbstractTableModel() {
//    
//      public int getColumnCount() { return colNames.length; }
//      public int getRowCount()    { return 1; }
//      public Object getValueAt(int row, int col) { return colNames.length }
//    }

    Vector colNamesVec = new Vector(colNames.length);
    
    for (int i=0; i<colNames.length; i++) {
    
      colNamesVec.add(i, colNames[i]);
    }
    
    Vector rowsData = new Vector();
    Vector row0Data = new Vector(2);
    row0Data.add("CELL1");
    row0Data.add("CELL2");
    rowsData.add(row0Data);
    
    table = new JTable(rowsData, colNamesVec);
         
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setShowHorizontalLines(false);
    table.setShowVerticalLines(false);
    
    StringRenderer stringRenderer = new StringRenderer();
   
    table.setDefaultRenderer(String.class, stringRenderer);
    //table.setRowHeight((int)(stringRenderer.getPreferredSize().height));

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.getViewport().setBackground(java.awt.Color.white);

    this.add(scrollPane, BorderLayout.CENTER);
    
  }

  
  private void initButtons( boolean showAddButton,    boolean showEditButton,       
                            boolean showDeleteButton, boolean showMoveUpButton,     
                                                      boolean showMoveDownButton) {
  
    Box buttonBox = Box.createVerticalBox();

    if (showAddButton) {
      
      addButton      = new JButton(new AddAction());
      WidgetFactory.setPrefMaxSizes(addButton, WizardSettings.LIST_BUTTON_DIMS);
      addButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(addButton);
    }
    
    if (showEditButton) {
      
      editButton     = new JButton(new EditAction());
      WidgetFactory.setPrefMaxSizes(editButton, WizardSettings.LIST_BUTTON_DIMS);
      editButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(editButton);
    }
    
    if (showDeleteButton) {
      
      deleteButton   = new JButton(new DeleteAction());
      WidgetFactory.setPrefMaxSizes(deleteButton, WizardSettings.LIST_BUTTON_DIMS);
      deleteButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(deleteButton);
    }
    
    if (showMoveUpButton) {
      
      moveUpButton   = new JButton(new MoveUpAction());
      WidgetFactory.setPrefMaxSizes(moveUpButton, WizardSettings.LIST_BUTTON_DIMS);
      moveUpButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(moveUpButton);
    }
    
    if (showMoveDownButton) {
      
      moveDownButton = new JButton(new MoveDownAction());
      WidgetFactory.setPrefMaxSizes(moveDownButton, WizardSettings.LIST_BUTTON_DIMS);
      moveDownButton.setFont(WizardSettings.WIZARD_CONTENT_FONT);
      buttonBox.add(moveDownButton);
    }
    buttonBox.add(Box.createGlue());
    this.add(buttonBox, BorderLayout.EAST);
  }
  
}


class StringRenderer extends JTextField implements TableCellRenderer {


  private JTextField[] textFields;
  
  public Component getTableCellRendererComponent(JTable table,
                                                Object  value,
                                                boolean isSelected,
                                                boolean hasFocus,
                                                int     row,
                                                int     col ) {
                                                
    if (value==null) return this;
    if (!(value instanceof String)) return null;
    
    if (isSelected) {
      this.setBackground(table.getSelectionBackground());
      this.setForeground(table.getSelectionForeground());
    } else {
      this.setBackground(table.getBackground());
      this.setForeground(table.getForeground());
    }
    this.setEnabled(table.isEnabled());
    this.setFont(table.getFont());
    this.setOpaque(true);

    this.setText((String)value);

    return this;
  }
}

 

class AddAction extends AbstractAction {

  public AddAction() { super("Add"); }
  
  public void actionPerformed(ActionEvent e) {

    Log.debug(45, "CustomList ADD action");  
  }
}

class EditAction extends AbstractAction {

  public EditAction() { super("Edit"); }
  
  public void actionPerformed(ActionEvent e) {
  
    Log.debug(45, "CustomList EDIT action");  
  }
}

class DeleteAction extends AbstractAction {

  public DeleteAction() { super("Delete"); }
  
  public void actionPerformed(ActionEvent e) {
  
    Log.debug(45, "CustomList DELETE action");  
  }
}

class MoveUpAction extends AbstractAction {

  public MoveUpAction() { super("Move Up"); }
  
  public void actionPerformed(ActionEvent e) {
  
    Log.debug(45, "CustomList MOVE UP action");  
  }
}

class MoveDownAction extends AbstractAction {

  public MoveDownAction() { super("Move Down"); }
  
  public void actionPerformed(ActionEvent e) {
  
    Log.debug(45, "CustomList MOVE DOWN action");  
  }
}

