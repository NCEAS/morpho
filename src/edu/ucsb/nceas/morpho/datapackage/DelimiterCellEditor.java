package edu.ucsb.nceas.morpho.datapackage;

import java.awt.Component;
import java.awt.TextField;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;


public class DelimiterCellEditor extends DefaultCellEditor
{
  // String to store the delimiter
  private String delimiter = null;
  
  // Data view Panel
  private JPanel parent = new JPanel();
  
  /** constructor of delimiter
   * @param myDelimiter the delimiter will be used.
   * @param panel the parent of error message panel
   */
  public DelimiterCellEditor(String myDelimiter, JPanel panel)
  {
    super(new JTextField());
    delimiter = myDelimiter;
    parent = panel;
  }// DelimiterCellEditor
  
  /**
   * Overiwrite the stopCellEding method. If in the input string there is 
   * delimiter, show error message.
   */
  public boolean stopCellEditing() 
  {
    try
    {
      String str = (String)super.getCellEditorValue();
      if ((str.indexOf(delimiter)) != -1)
      {
        JOptionPane.showMessageDialog(parent, "Could't input delimiter!", 
                                    "Alter!", JOptionPane.ERROR_MESSAGE);
        return false;
      }//if
    }//try
    catch (ClassCastException except)
    {
      return false;
    }//catch
    return super.stopCellEditing();
  }//stopCellEditing

}//DelimiterCellEditor
