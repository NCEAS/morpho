/**
 *  '$RCSfile: InsertRowCommand.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-27 03:54:59 $'
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
 * Class to handle insert a row command
 */
public class InsertRowCommand implements Command 
{
  /* Indicate before selected column */
  private boolean beforeFlag = true;
  
  /* Referrence to  morphoframe */
  private MorphoFrame morphoFrame = null;
  
  /* Constant string for column position */
  public static final String AFTER = "after";
  public static final String BEFORE = "before";
  
 
 
  /**
   * Constructor of Import data command
   */
  public InsertRowCommand(String column)
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
    DataViewContainerPanel resultPane = null;
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
         insertRow(jtable, ptmodel);
       }
       
    }//if
  
  }//execute
  
  
  
  /* Method to insert a new row into table */
  private void insertRow(JTable table, PersistentTableModel ptm)
                           
  {  
     int sel = table.getSelectedRow();
     if (sel>-1) 
     {
          Vector blanks = new Vector();
          blanks.addElement(" \t");
          blanks.addElement(" \t");
          blanks.addElement(" \t");
          if (beforeFlag)
          {
            ptm.insertRow(sel, blanks);
          }
          else
          {
            ptm.insertRow(sel+1, blanks);
          }
     }

  }//insert row

  /**
   * could also have undo functionality; disabled for now
   */ 
  // public void undo();

}//class CancelCommand
