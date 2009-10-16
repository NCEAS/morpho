package edu.ucsb.nceas.morpho.plugins.datapackagewizard;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * Represents a non-editable table data model
 * @author tao
 *
 */
public class UneditableTableModel extends DefaultTableModel 
{
    public UneditableTableModel(Vector data, Vector columnNames) 
    {
      super(data, columnNames);
    }


    public boolean isCellEditable(int row, int col) 
    {
      return false;
    }
  }
