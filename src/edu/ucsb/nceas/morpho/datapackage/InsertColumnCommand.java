/**
 *  '$RCSfile: InsertColumnCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-10-24 23:14:02 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.Log;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.Point;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import org.w3c.dom.Document;


/**
 * Class to handle insert a cloumn command
 */
public class InsertColumnCommand implements Command 
{
  /* Indicate before selected column */
  private boolean beforeFlag = true;
  
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;
  
  /* Constant string for column position */
  public static final String AFTER = "after";
  public static final String BEFORE = "before";
  
  /* Flag if need to add a column*/
  private boolean columnAddFlag = false;
  /* Reference to ColumnMetadataEditPanel*/
  ColumnMetadataEditPanel cmep = null;
  /* Control in column meta data edit panel*/
  private JPanel controlPanel;
  private JButton controlOK;
  private JButton controlCancel;
  private JDialog columnDialog;
  
  private DataViewContainerPanel resultPane;
  
  /**
   * Constructor of Import data command
   */
  public InsertColumnCommand(String column)
  {
    if (column.equals(AFTER))
    {
      beforeFlag = false;
    }
  }

  /**
   * execute insert command
   */    
  public void execute(ActionEvent event)
  {   
    resultPane = null;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    if (morphoFrame != null)
    {
       resultPane = AddDocumentationCommand.
                          getDataViewContainerPanelFromMorphoFrame(morphoFrame);
    }//if
    
    // make sure resulPanel is not null
    if (resultPane != null)
    {
       DataViewer dataView = resultPane.getCurrentDataViewer();
       if (dataView != null)
       {
         // Get parameters and run it
         JTable jtable=dataView.getDataTable();
         PersistentTableModel ptmodel=(PersistentTableModel)jtable.getModel();
         PersistentVector vector=dataView.getPV();
         Morpho morph=dataView.getMorpho();
         Document attributeDocoumnet=dataView.getAttributeDoc();
         Vector columnLabels=dataView.getColumnLabels();
//         String fieldDelimiter=dataView.getFieldDelimiter();
         String fieldDelimiter= vector.getFieldDelimiter();
         JPanel tablePanel=dataView.getTablePanel();
         insertColumn(jtable, ptmodel, vector, morph, attributeDocoumnet,
                      columnLabels, fieldDelimiter, tablePanel);
       }
       
    }//if
  
  }//execute
  
  
  
  /* Method to insert a new column into table */
  private void insertColumn(JTable table, PersistentTableModel ptm, 
                           PersistentVector pv, Morpho morpho,
                           Document attributeDoc, Vector column_labels,
                           String field_delimiter, JPanel TablePanel)
  {  
    
    int sel = table.getSelectedColumn();
    if (sel>-1) 
    {
       showColumnMetadataEditPanel();
       if (columnAddFlag) 
       {
         try 
         {
           cmep.setMorpho(morpho);
           if (beforeFlag)
           {
              cmep.insertNewAttributeAt(sel, attributeDoc);
           }
           else
           {
              cmep.insertNewAttributeAt((sel+1), attributeDoc);
            }

          }//try
          catch (Exception w) 
          {
              Log.debug(20, "Exception trying to modify attribute DOM");
          }//catch
            
          String newHeader = cmep.getColumnName();
          if (newHeader.trim().length()==0) newHeader = "New Column";
          String type = cmep.getDataType();
          String unit = cmep.getUnit();
          newHeader = "<html><font face=\"Courier\"><center><small>"+type+
                                           "<br>"+unit +"<br></small><b>"+
                                           newHeader+"</b></font></center></html>";
          if (beforeFlag)
          {
            column_labels.insertElementAt(newHeader, sel);
            DataViewer dataView = resultPane.getCurrentDataViewer();
            dataView.setColumnLabels(column_labels);
            ptm.insertColumn(sel);
          }
          else
          {
            column_labels.insertElementAt(newHeader, sel+1);
            DataViewer dataView = resultPane.getCurrentDataViewer();
            dataView.setColumnLabels(column_labels);
            ptm.insertColumn(sel+1);
          }
          pv = ptm.getPersistentVector();
          setUpDelimiterEditor(table, field_delimiter, TablePanel);
          
          
       }//if
    }//if
  }//insetColumn

  /*
   * Method to show column meta data edit panel
   */
  private void showColumnMetadataEditPanel() 
  {
    int newCompWidth = 400;
    int newCompHeight = 650;
    MorphoFrame mf = UIController.getInstance().getCurrentActiveWindow();
    Point curLoc = mf.getLocationOnScreen();
    Dimension dim = mf.getSize();
    int newx = curLoc.x +dim.width/2;
    int newy = curLoc.y+dim.height/2;
    columnDialog = new JDialog(mf,true);
    columnDialog.getContentPane().setLayout(new BorderLayout(0,0));
    columnDialog.setSize(newCompWidth,newCompHeight);
    columnDialog.setLocation(newx-newCompWidth/2, newy-newCompHeight/2);
    cmep = new ColumnMetadataEditPanel();
    columnDialog.getContentPane().add(BorderLayout.CENTER, cmep);
    controlPanel = new JPanel();
    controlCancel = new JButton("Cancel");
    controlOK= new JButton("OK");
    controlPanel.add(controlCancel);
    controlPanel.add(controlOK);
    columnDialog.getContentPane().add(BorderLayout.SOUTH, controlPanel);
    ColAction cAction = new ColAction();
		controlOK.addActionListener(cAction);
    controlCancel.addActionListener(cAction);

    columnDialog.setVisible(true);
  }
  
  /*
   * Method to set a table's interger and string column editor
   */
   private void setUpDelimiterEditor(JTable jtable, String delimiter,
                                                         JPanel pane) 
   {
      //Set up the editor for the integer and string cells.
      int columns = jtable.getColumnCount();
      final DelimiterField delimiterField = 
                              new DelimiterField(pane, delimiter, "", columns);
      delimiterField.setHorizontalAlignment(DelimiterField.RIGHT);

      DefaultCellEditor delimiterEditor =
            new DefaultCellEditor(delimiterField) 
            {
                //Override DefaultCellEditor's getCellEditorValue method
                public Object getCellEditorValue() 
                {
                    return new String(delimiterField.getValue());
                }
            };
       TableColumnModel columnModel = jtable.getColumnModel();
       for (int j = 0; j< columns; j++)
       {
          columnModel.getColumn(j).setCellEditor(delimiterEditor);
          columnModel.getColumn(j).setPreferredWidth(85);
       }
     
    }
  
  /*
   * Private class to handle the button action in column meta data edit panel
   */
  class ColAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == controlOK) {
        columnAddFlag = true;
				columnDialog.dispose();
      }
      else if (object == controlCancel) {
        columnAddFlag = false;
				columnDialog.dispose();
      }
		}
	}
  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
