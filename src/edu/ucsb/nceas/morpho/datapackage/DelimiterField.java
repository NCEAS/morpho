/**
 *  '$RCSfile: DelimiterField.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-17 20:27:36 $'
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

import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
/**
 * A class to handle if input delimiter into text fild
 */
public class DelimiterField extends JTextField 
{
    // store the delimiter
    private String delimiter = null;
    // parent of JOptionPane
    private JPanel parent = null;
    
    /**
     * Constructor of DelimiterField
     * 
     * @param container the parent of JOptionPanel
     * @param delimiter the delimiter in the data file
     * @param value the value of input
     * @param columns the number of column need to be set
     */
    public DelimiterField(JPanel containter, String delimiter, 
                          String value, int columns) 
    {
        super(columns);
        parent = containter;
        this.delimiter = delimiter;
        setValue(value);
    }
    
    /**
     * Method to get cell value
     */
    public String getValue() 
    {
        String retVal = "";
        retVal = getText();
        return retVal;
    }
    
    /**
     * Method to set a stirng value
     *
     * @param value the value will be set to the field
     */
    public void setValue(String value) 
    {
        setText(value);
    }
    
    /**
     * Method to create default model for text field
     */
    protected Document createDefaultModel() 
    {
        return new NoDelimiterDocument();
    }
    
    
    protected class NoDelimiterDocument extends PlainDocument 
    {
        public void insertString(int offs,
                                 String str,
                                 AttributeSet a)
                throws BadLocationException 
       {
           
            char[] source = str.toCharArray();
            char[] result = new char[source.length];
            int j = 0;
            for (int i = 0; i < result.length; i++) 
            {
               if ( source[i] != delimiter.charAt(0))
               {
                    result[j++] = source[i];
                }//if
                else
                {
                   JOptionPane.showMessageDialog(parent, 
                                    "Could't input delimiter!", 
                                    "Alter!", JOptionPane.ERROR_MESSAGE);
                }//else
               
            }//for
            super.insertString(offs, new String(result, 0, j), a);
        }//insetString
    }//NoDelimiterDocument
}
