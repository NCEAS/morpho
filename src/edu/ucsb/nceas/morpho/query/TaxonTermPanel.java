/**
 *       Name: TaxonTermPanel.java
 *    Purpose: subpanel repeated in QueryDialog
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2001-07-13 17:29:03 $'
 * '$Revision: 1.3.2.1 $'
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
 * This panel contains all the elements for a single taxon
 * query term. It is repeated for multiple combined queries.
 * It is essential a query component that can return a search
 * term, a search type (e.g. contains, matches, etc.), and
 * some information about the path
 */
public class TaxonTermPanel extends JComponent
{

  //{{DECLARE_CONTROLS
  private JComboBox rankComboBox = new JComboBox();
  private JComboBox searchModeComboBox = new JComboBox();
  private JTextField textValueBox = new JTextField();
  //}}

  /**
   * Constructor
   */
  public TaxonTermPanel()
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

/*
    JPanel queryTermHelpPanel = new JPanel();
    queryTermHelpPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
    JLabel helpLabel = new JLabel();
    helpLabel.setText("Check boxes determine which metadata fields " +
                      "are searched.");
    queryTermHelpPanel.add(helpLabel);
    setChoicesPanel.add(queryTermHelpPanel);
*/

    JPanel queryTermPanel = new JPanel();
    queryTermPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

    rankComboBox.addItem("Kingdom");
    rankComboBox.addItem("Phylum");
    rankComboBox.addItem("Division");
    rankComboBox.addItem("Superclass");
    rankComboBox.addItem("Class");
    rankComboBox.addItem("Subclass");
    rankComboBox.addItem("Order");
    rankComboBox.addItem("Family");
    rankComboBox.addItem("Genus");
    rankComboBox.addItem("Species");
    rankComboBox.addItem("Subspecies");
    rankComboBox.setSelectedIndex(9);
    rankComboBox.setBackground(java.awt.Color.white);
    queryTermPanel.add(rankComboBox);

    searchModeComboBox.addItem("contains");
    searchModeComboBox.addItem("starts-with");
    searchModeComboBox.addItem("ends-with");
    searchModeComboBox.addItem("matches-exactly");
    searchModeComboBox.setSelectedIndex(0);
    searchModeComboBox.setBackground(java.awt.Color.white);
    queryTermPanel.add(searchModeComboBox);

    JLabel valueLabel = new JLabel();
    valueLabel.setText("  ");
    valueLabel.setForeground(java.awt.Color.black);
    valueLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    queryTermPanel.add(valueLabel);

    textValueBox.setColumns(20);
    queryTermPanel.add(textValueBox);

    setChoicesPanel.add(queryTermPanel);
    setChoicesPanel.setBorder(BorderFactory.createLineBorder(Color.black));
    //}}

    //{{REGISTER_LISTENERS
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
    for (int i = 0; i < searchModeComboBox.getItemCount(); i++) {
      if (value.equals(searchModeComboBox.getItemAt(i))) {
        searchModeComboBox.setSelectedIndex(i);
      }
    }
  }

  /**
   * return the value of the combobox which contains the
   * taxonomic rank (i.e. contains, Kingdom, Phylum, etc.)
   */
  public String getTaxonRank()
  {
    String ret = (String) rankComboBox.getSelectedItem();
    return ret;
  }

  /**
   * set the taxon rank field
   */
  public void setTaxonRank(String value)
  {
    for (int i = 0; i < rankComboBox.getItemCount(); i++) {
      if (value.equals(rankComboBox.getItemAt(i))) {
        rankComboBox.setSelectedIndex(i);
      }
    }
  }
}
