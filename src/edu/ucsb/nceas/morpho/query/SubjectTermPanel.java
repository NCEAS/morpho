/**
 *       Name: SubjectTermPanel.java
 *    Purpose: subpanel repeated in QueryDialog
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-05-31 18:47:09 $'
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
package edu.ucsb.nceas.morpho.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This panel contains all the elements for a single text
 * query term. It is repeated for multiple combined queries.
 * It is essential a query component that can return a search
 * term, a search type (e.g. contains, matches, etc.), and
 * some information about the path (e.g. all, title, etc.)
 */
public class SubjectTermPanel extends JComponent
{

  //{{DECLARE_CONTROLS
  private JCheckBox titleCheckBox = new JCheckBox();
  private JCheckBox abstractCheckBox = new JCheckBox();
  private JCheckBox keywordsCheckBox = new JCheckBox();
  private JCheckBox allCheckBox = new JCheckBox();
  private JComboBox searchModeComboBox = new JComboBox();
  private JTextField textValueBox = new JTextField();
  //}}

  /**
   * Constructor
   */
  public SubjectTermPanel()
  {
    //{{INIT_CONTROLS
    setLayout(new BorderLayout(0, 0));
    setBackground(java.awt.Color.lightGray);
    setSize(685, 58);
    JPanel setChoicesPanel = new JPanel();
    setChoicesPanel.setAlignmentX(0.496933F);
    setChoicesPanel.setLayout(new
                              BoxLayout(setChoicesPanel, BoxLayout.Y_AXIS));
    add(BorderLayout.CENTER, setChoicesPanel);

    JPanel queryTermHelpPanel = new JPanel();
    queryTermHelpPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    JLabel helpLabel = new JLabel();
    helpLabel.setText("Check boxes determine which metadata fields " +
                      "are searched.");
    queryTermHelpPanel.add(helpLabel);
    setChoicesPanel.add(queryTermHelpPanel);

    JPanel queryTermPanel = new JPanel();
    queryTermPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    Box checkBoxHorizontal = Box.createHorizontalBox();
    allCheckBox.setText("All");
    allCheckBox.setActionCommand("All");
    allCheckBox.setSelected(true);
    allCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    checkBoxHorizontal.add(allCheckBox);

    JPanel checkBoxVertical = new JPanel();
    checkBoxVertical.setLayout(new BoxLayout(checkBoxVertical,
                                             BoxLayout.Y_AXIS));
    checkBoxVertical.setAlignmentX(Component.LEFT_ALIGNMENT);
    titleCheckBox.setText("Title");
    titleCheckBox.setActionCommand("Title");
    titleCheckBox.setSelected(true);
    titleCheckBox.setEnabled(false);
    checkBoxVertical.add(titleCheckBox);
    abstractCheckBox.setText("Abstract");
    abstractCheckBox.setActionCommand("Abstract");
    abstractCheckBox.setSelected(true);
    abstractCheckBox.setEnabled(false);
    checkBoxVertical.add(abstractCheckBox);
    keywordsCheckBox.setText("Keywords");
    keywordsCheckBox.setActionCommand("Keywords");
    keywordsCheckBox.setSelected(true);
    keywordsCheckBox.setEnabled(false);
    checkBoxVertical.add(keywordsCheckBox);
    checkBoxHorizontal.add(checkBoxVertical);
    queryTermPanel.add(checkBoxHorizontal);

    queryTermPanel.add(searchModeComboBox);
    searchModeComboBox.setBackground(java.awt.Color.white);
    JLabel valueLabel = new JLabel();
    //valueLabel.setText("Subject");
    valueLabel.setText("  ");
    queryTermPanel.add(valueLabel);
    valueLabel.setForeground(java.awt.Color.black);
    valueLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    textValueBox.setColumns(20);
    queryTermPanel.add(textValueBox);
    setChoicesPanel.add(queryTermPanel);
    setChoicesPanel.add(Box.createVerticalGlue());
    setChoicesPanel.add(Box.createRigidArea(new Dimension(8, 8)));
    //}}
    searchModeComboBox.addItem("contains");
    searchModeComboBox.addItem("starts-with");
    searchModeComboBox.addItem("ends-with");
    searchModeComboBox.addItem("matches-exactly");
    searchModeComboBox.setSelectedIndex(0);

    setChoicesPanel.setBorder(BorderFactory.createLineBorder(Color.black));

    //{{REGISTER_LISTENERS
    SymItem lSymItem = new SymItem();
    allCheckBox.addItemListener(lSymItem);
    //}}
  }

  /**
   * get the text that is to be seached for.
   */
  public String getValue()
  {
    String ret = textValueBox.getText();
      return ret;
  }

  /**
   * set the text that is to be seached for.
   */
  public void setValue(String value)
  {
    textValueBox.setText(value);
  }

  /**
   * return the value of the combobox which contains the
   * search mode (i.e. contains, starts-with, etc.)
   */
  public String getSearchMode()
  {
    String ret = (String) searchModeComboBox.getSelectedItem();
      return ret;
  }

  /**
   * set the search mode field
   */
  public void setSearchMode(String value)
  {
    for (int i = 0; i < searchModeComboBox.getItemCount(); i++)
    {
      if (value.equals(searchModeComboBox.getItemAt(i)))
      {
        searchModeComboBox.setSelectedIndex(i);
      }
    }
  }

  /**
   * get state of 'All' check box
   */
  public boolean getAllState()
  {
    return allCheckBox.isSelected();
  }

  /**
   * set state of 'All' check box
   */
  public void setAllState(boolean state)
  {
    allCheckBox.setSelected(state);
  }

  /**
   * returns state of Title checkbox
   */
  public boolean getTitleState()
  {
    return titleCheckBox.isSelected();
  }

  /**
   * set state of Title check box
   */
  public void setTitleState(boolean state)
  {
    titleCheckBox.setSelected(state);
  }

  /**
   * returns state of Abstract checkbox
   */
  public boolean getAbstractState()
  {
    return abstractCheckBox.isSelected();
  }

  /**
   * set state of Abstract check box
   */
  public void setAbstractState(boolean state)
  {
    abstractCheckBox.setSelected(state);
  }

   /**
    * returns state of Keywords check box
    */
  public boolean getKeywordsState()
  {
    return keywordsCheckBox.isSelected();
  }

  /**
   * set state of Keywords check box
   */
  public void setKeywordsState(boolean state)
  {
    keywordsCheckBox.setSelected(state);
  }

  private class SymItem implements java.awt.event.ItemListener
  {
    public void itemStateChanged(java.awt.event.ItemEvent event)
    {
      Object object = event.getSource();
      if (object == allCheckBox) {
        allCheckBox_itemStateChanged(event);
      }
    }
  }

  /**
   * used to change the enabled state of checkbox
   * 
   * @param event
   */
  private void allCheckBox_itemStateChanged(java.awt.event.ItemEvent event)
  {
    if (allCheckBox.isSelected()) {
      titleCheckBox.setEnabled(false);
      abstractCheckBox.setEnabled(false);
      keywordsCheckBox.setEnabled(false);
    } else {
      titleCheckBox.setEnabled(true);
      abstractCheckBox.setEnabled(true);
      keywordsCheckBox.setEnabled(true);
    }
  }
}
