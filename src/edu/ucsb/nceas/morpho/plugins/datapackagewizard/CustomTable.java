/**
 *  '$RCSfile: CustomTable.java,v $'
 *    Purpose: A widget that displays data of multiple columns from multiple tables
 *						 in a columnar fashion and allows the user to select multiple columns
 * 						 using checkboxes
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
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
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.ListModel;
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
import javax.swing.table.TableModel;
import javax.swing.table.JTableHeader;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class CustomTable extends JPanel {
	
	/**
	
	*/
	
	private ColumnarTable table;
	private JScrollPane scrollPane = null;
	
	public CustomTable(Vector headerNames, Vector colValues) {
		
		if(headerNames.size() == 0)
			return;
		table = new ColumnarTable(headerNames, colValues); 
		init();
	}
	
	private void init() {
		
		this.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
	  		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getViewport().removeAll();
		scrollPane.getViewport().add(table);
    scrollPane.getViewport().setBackground(java.awt.Color.white);
    
    this.add(scrollPane, BorderLayout.CENTER);
		
	}
	
	public void addPopupListener(CustomTablePopupListener l) {
		
		table.addPopupListener(l);
	}
	
	public List getColumnData(int colIdx) {
		
		return table.getColumnData(colIdx);
	}
	
	public int[] getSelectedColumns() {
		return table.getSelectedColumns();
	}
	
	public void setSelectedColumns(int[] selectedCols) {
		
		table.setSelectedColumns(selectedCols);
	}
	
	public Vector getColumnHeaderStrings(int colIdx) {
		
		return table.getColumnHeaderStrings(colIdx);
	}
	
	public void setColumnHeaderStrings(int colIdx, Vector colHeader) {
		
		table.setColumnHeaderStrings(colIdx, colHeader);
		return;
	}
	
	public void setExtraColumnHeaderInfo(int colIdx, String info) {
		
		table.setExtraColumnHeaderInfo(colIdx, info);
		return;
	}
	
	public int getHeaderHeight() {
		
		TableColumnModel columnModel = table.getColumnModel();
		TableColumn column = columnModel.getColumn(0);
		CustomHeaderRenderer chr = (CustomHeaderRenderer)column.getHeaderRenderer();
		return (int)chr.getTableCellRendererComponent(table, "", false, false, 0, 0).getSize().getHeight();
                                               
		//return chr.getHeight();
		
	}
	
}

class ColumnarTable extends JTable {
	
	private static final int MAX_ROWS_DISPLAYED = 10;
	
	private Vector headerNames;
	private Vector tableNames;
	private Vector colNames;
	private Vector rowData;
	private DefaultListSelectionModel colSelectionModel;
	private TableColumnModel columnModel;
	private List listeners = null;
	
	private boolean displayTableNames;
	private boolean displayColumnNames;
	
	ColumnarTable(Vector headerNames, Vector rowData) {
		
		super();
		Vector firstrow = (Vector)headerNames.get(0);
		setModel(new DefaultTableModel(rowData, firstrow));
		setColumnSelectionAllowed(true);
		setRowSelectionAllowed(false);
		setShowVerticalLines(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		this.headerNames = headerNames;
		this.rowData = rowData;
		
		columnModel = getColumnModel();
		colSelectionModel = new DefaultListSelectionModel();
		colSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		columnModel.setSelectionModel(colSelectionModel);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		
		
		for(int i=0; i< firstrow.size(); i++) {
			Vector rowHeader = new Vector();
			for(int j =0;j < headerNames.size(); j++) {
				Vector header = (Vector) headerNames.get(j);
				rowHeader.add((String)header.get(i));
			}
			TableColumn column = columnModel.getColumn(i);
			column.setHeaderRenderer(new CustomHeaderRenderer(rowHeader));
			column.setPreferredWidth(85);
			column.setMinWidth(85);
		}
		
		
		this.getTableHeader().addMouseListener( new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				
				int index = columnModel.getColumnIndexAtX(me.getX());
				TableColumn column = columnModel.getColumn(index);
				CustomHeaderRenderer chr = (CustomHeaderRenderer)column.getHeaderRenderer();
				if(chr.invertSelection()) {
					
					DefaultListSelectionModel orig = null;
					try {
						orig = (DefaultListSelectionModel) colSelectionModel.clone();
					} catch(Exception e) {}
					if(listeners != null) {
						boolean refresh = false;
						
						Iterator it = listeners.iterator();
						while(it.hasNext()) {
							CustomTablePopupListener l = (CustomTablePopupListener)it.next();
							JDialog d = l.getPopupDialog();
							d.setModal(true);
							int w = d.getWidth(); 
							int h = d.getHeight();
							int xc = me.getX() + (int)me.getComponent().getLocationOnScreen().getX();
							int yc = me.getY() + (int)me.getComponent().getLocationOnScreen().getY() - h/2;
							d.setBounds(xc, yc, w, h);
							d.setVisible(true);
							String t = l.getDisplayString();
							if(t != null) {
								chr.setExtraHeaderString(t);
								chr.showExtraHeaderInfo(true);
								
							}
						}
					}
					
					colSelectionModel.addSelectionInterval( index, index);
					
				} else {
					
					colSelectionModel.removeSelectionInterval(index, index);
					chr.showExtraHeaderInfo(false);
				}
				
				int rows = ColumnarTable.this.rowData.size();
				if (rows > 0) {
          ColumnarTable.this.setRowSelectionInterval(0, rows - 1);
				}
				
				ColumnarTable.this.getTableHeader().resizeAndRepaint();
				
			}
		});
		
	}
	
	
	private int getNumberOfRows(Vector rowData) {
		
		int rows = 0;
		for(int i = 0;i < rowData.size(); i++) {
			List t = (List)rowData.get(i);
			if(t.size() > rows) {
				rows = t.size();
				if(rows >= MAX_ROWS_DISPLAYED) 
					break; 
			}
		}
		return rows;
	}
	
	public List getColumnData( int colIdx) {
		
		if(colIdx >= getColumnCount()) 
			return null;
		Iterator it = rowData.iterator();
		List ret = new ArrayList();
		while(it.hasNext()){
			
			Vector row = (Vector)it.next();
			ret.add(row.get(colIdx));
		}
		return ret;
	}
	
	public Vector getColumnHeaderStrings(int colIdx) {
		
		if(colIdx >= getColumnCount()) 
			return null;
		TableColumn column = columnModel.getColumn(colIdx);
		CustomHeaderRenderer chr = (CustomHeaderRenderer)column.getHeaderRenderer();
		return chr.getColumnHeader();
	}
	
	public void setColumnHeaderStrings(int colIdx, Vector colHeader) {
		
		if(colIdx >= getColumnCount()) 
			return;
		TableColumn column = columnModel.getColumn(colIdx);
		CustomHeaderRenderer chr = (CustomHeaderRenderer)column.getHeaderRenderer();
		chr.setColumnHeader(colHeader);
	}
	
	public void setExtraColumnHeaderInfo(int colIdx, String info) {
		
		if(colIdx >= getColumnCount()) 
			return;
		TableColumn column = columnModel.getColumn(colIdx);
		CustomHeaderRenderer chr = (CustomHeaderRenderer)column.getHeaderRenderer();
		chr.setExtraColumnHeaderInfo(info);
		return;
	}
	
	public void setSelectedColumns(int[] selectedCols) {
		
		colSelectionModel.clearSelection();
		if(columnModel == null)
			columnModel = this.getColumnModel();
		
		for(int i=0; i < selectedCols.length; i++) {
			colSelectionModel.addSelectionInterval(selectedCols[i], selectedCols[i]);
			TableColumn column = columnModel.getColumn(selectedCols[i]);
			CustomHeaderRenderer chr = (CustomHeaderRenderer)column.getHeaderRenderer();
			chr.setCheckboxSelected(true);
		}
		
		int rows = rowData.size();
		if (rows > 0) {
          this.setRowSelectionInterval(0, rows - 1);
		}
		getTableHeader().resizeAndRepaint();
		return;
	}
	
	public boolean isCellEditable(int row, int col) {
		return false;
	}
	
	public void processMouseEvent(MouseEvent me) {
		return;
	}
	
	public void processMouseMotionEvent(MouseEvent me) {
		return;
	}
	
	
	public void addPopupListener(CustomTablePopupListener l) {
		
		if(listeners == null) {
			listeners = new ArrayList();
		}
		listeners.add(l);
	}
	
}


class CustomHeaderRenderer extends DefaultTableCellRenderer {
	
	private String tableName = "";
	private String columnName = "";
	private JCheckBox cb;
	private boolean displayTableName;
	private boolean displayColumnName;
	private String extraHeaderInformation = "";
	private boolean displayExtraHeaderInfo = false;
	
	private Vector headerValues;
	
	CustomHeaderRenderer(Vector headerValues) {
		super();
		this.headerValues = headerValues;
		cb = new JCheckBox("");
	}
	
	
	public Component getTableCellRendererComponent(JTable table,
                                               Object value,
                                               boolean isSelected,
                                               boolean hasFocus,
                                               int row,
                                               int column) {
																								 
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		
		JPanel cbPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cbPanel.add(cb);//, BorderLayout.CENTER);
		panel.add(cbPanel);
		
		Iterator it = headerValues.iterator();
		while(it.hasNext()) {
			String val = (String)it.next();
			JPanel tPanel = new JPanel(new BorderLayout());
			tPanel.add(new JLabel(val, SwingConstants.CENTER), BorderLayout.CENTER);
			//tPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
			tPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, WizardSettings.TOP_PANEL_BG_COLOR));
			panel.add(tPanel);
		}
		
		JPanel ePanel = new JPanel(new BorderLayout());
		ePanel.add(new JLabel("( "+extraHeaderInformation+" )", SwingConstants.CENTER));
		//ePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		ePanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, WizardSettings.TOP_PANEL_BG_COLOR));
		ePanel.setVisible(displayExtraHeaderInfo);
		panel.add(ePanel);
		
		
		return panel;
	}
	
	public void setCheckboxSelected(boolean sel) {
		
		cb.setSelected(sel);
	}
	
	public boolean invertSelection() {
		cb.setSelected(!cb.isSelected());
		return cb.isSelected();
	}
	
	public boolean isSelected() {
		return cb.isSelected();
	}
	
	public void setExtraHeaderString(String t) {
		extraHeaderInformation = t;
	}
	
	public void showExtraHeaderInfo(boolean b) {
		displayExtraHeaderInfo = b;
	}
	
	public Vector getColumnHeader() {
		
		Vector v = null;
		if(displayExtraHeaderInfo) {
			v = (Vector)headerValues.clone();
			v.add(extraHeaderInformation);
		} else 
			v = headerValues;
		
		return v;
	}
	
	public void setColumnHeader(Vector colHeader) {
		
		int size = colHeader.size();
		System.out.println("in table: current header size = " + headerValues.size() + " , new header size =  " + colHeader.size());
		headerValues = colHeader;
		return;
	}
	
	public void setExtraColumnHeaderInfo(String info) {
		
		this.extraHeaderInformation = info;
		this.displayExtraHeaderInfo = true;
		return;
	}
	
}


