/**
 *       Name: ResultPaneAndFrameMediator.java
 *    Purpose: Mediator class between result panel and frame
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @Jing Tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-14 16:47:56 $'
 * '$Revision: 1.2 $'
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

import edu.ucsb.nceas.morpho.framework.*;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * This class is a mediator between components: Resultset table and open button
 * If there is a selection in the table, open button will enable. Otherwise,
 * it is disable. These two components are both in OpenDialogBox and Search
 * Result Frame, so this class can be applied into both of them
 * Of course, more components can be added into this class
 */
public class ResultPanelAndFrameMediator
{
  /** A reference to the ResultPanel */
  private JPanel resultPanel = null;

  /** A reference to the OpenButton*/
  private JButton openButton = null;
  
  /**
   * Method to register a resultPanel
   * @param myResultPanel the panel which contain the table that mediator need
   * to know
   */
  public void registerResultPanel(JPanel myResultPanel)
  {
    resultPanel = myResultPanel;
  }//registerResultPanel

  /**
   * Method to register a open button
   * @param myOpenButton the button that mediator need to know
   */
  public void registerOpenButton(JButton myOpenButton)
  {
    openButton = myOpenButton;
  }//resgisterOpenButton
  
  /**
   * Method to disable the open button
   */
  public void disableOpenButton()
  {
    openButton.setEnabled(false);
  }//disableOpenButton
  
  /**
   * Method to enable the open button
   */
  public void enableOpenButton()
  {
    openButton.setEnabled(true);
  }//enableOpenButton
   
  /**
   * Method to initialize
   */
  public void init()
  {
    openButton.setEnabled(false);
  }//init
   
}
