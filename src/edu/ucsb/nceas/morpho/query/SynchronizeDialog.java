/**
 *       Name: SynchronizeDialog.java
 *    Purpose: Visual display for OpenDialogBox
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @Jing Tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-31 00:28:11 $'
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
package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
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
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.SwingConstants;

/**
 * A dialog box for user to open a datapakcage
 */
public class SynchronizeDialog extends JDialog
{
 
  /** Contorl button */ 
  private JButton executeButton = null;
  private JButton cancelButton = null;
  
  /** Radio button */
  private JRadioButton localToNetworkRadio = null;
  private JRadioButton networkToLocalRadio = null;
    
  private static final int PADDINGWIDTH = 8;
  
  private static String SYNCHRONIZE =
      "\"Synchronize\" will keep the Data Packages on your local computer "
      + "identical with those on the network.\n"
      + "In order to do this, Morpho will copy the Data Package as shown below:";
 
  private static String WARNING =
      "Note:\n"
      +"  If you are copying from local to network, you may be"
      + "prompted to renumber the Data Package";
  private static String LOCALTONETWORK =" will be copied from local to network";
  private static String NETWORKTOLOCAL =
                                      " will be copied from network to local";
      
  
  /**
   * Construct a new instance of the synchonize dialog
   *
   * @param parent  The parent frame for this dialog
   */
  public SynchronizeDialog(MorphoFrame parent, String docid, boolean inLocal,
                                                          boolean inNetwork)
  {
    super(parent);
    String localToNetRadioStr ="Datapackage "+docid+LOCALTONETWORK;
    String netToLocalRadioStr ="Datapackage "+docid+NETWORKTOLOCAL;
    
    // Set OpenDialog size depent on parent size
    int parentWidth = parent.getWidth();
    int parentHeight = parent.getHeight();
    int dialogWidth = 500;
    int dialogHeight = 300;
    setSize(dialogWidth, dialogHeight);
    
    // Set location of dialog, it shared same center of parent
    double parentX = parent.getLocation().getX();
    double parentY = parent.getLocation().getY();
    double centerX = parentX + 0.5 * parentWidth;
    double centerY = parentY + 0.5 * parentHeight;
    int dialogX = (new Double(centerX - 0.5 * dialogWidth)).intValue();
    int dialogY = (new Double(centerY - 0.5 * dialogHeight)).intValue();
    setLocation(dialogX, dialogY);
    
    setTitle("Synchronize");
    // Set the default close operation is dispose
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    // Set the border layout as layout
    getContentPane().setLayout(new BorderLayout(0, 0));
    
    // Create panel to store the note
    Box noteBox = Box.createVerticalBox();
    noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    JTextArea note = new JTextArea(SYNCHRONIZE);
    note.setEditable(false);
    note.setLineWrap(true);
    note.setOpaque(false);
    noteBox.add(note);
    getContentPane().add(BorderLayout.NORTH, noteBox);
    
    // Add padding for left and right
    getContentPane().add(BorderLayout.EAST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.WEST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    
    // Create radio panel
    JPanel radioPanel = new JPanel();
    localToNetworkRadio = new JRadioButton(localToNetRadioStr);
    networkToLocalRadio = new JRadioButton(netToLocalRadioStr);
    
    GUIAction executeAction = null;
    // If inLocal and not inNetwork do upload
    if (inLocal && !inNetwork)
    {
      
     executeAction = new GUIAction("Execute", null, 
                                  new LocalToNetworkCommand(this)); 
    }
    if (!inLocal && inNetwork)
    {
      executeAction = new GUIAction("Execute", null, 
                                  new NetworkToLocalCommand(this)); 
    }
//    radioPanel.add(localToNetworkRadio);
//    radioPanel.add(networkToLocalRadio);
    
    getContentPane().add(BorderLayout.CENTER, radioPanel);
 
    // Create bottom box
    Box bottomBox = Box.createVerticalBox();
    getContentPane().add(BorderLayout.SOUTH, bottomBox);
    //Create padding between result panel and Contorl button box
    bottomBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    JTextArea warning = new JTextArea(WARNING);
    warning.setEditable(false);
    bottomBox.add(warning);
    bottomBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    // Create a controlbuttionBox
    Box controlButtonsBox = Box.createHorizontalBox();
    controlButtonsBox.add(Box.createHorizontalGlue());
    
    //   
    executeButton = new JButton(executeAction);   
 
   
    controlButtonsBox.add(executeButton);
    
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));
    
    //Cancel button
    GUIAction cancelAction = new GUIAction("Cancel", null, 
                                                      new CancelCommand(this));
    cancelButton = new JButton(cancelAction);
    controlButtonsBox.add(cancelButton);
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));
    
    // Add controlButtonsBox to bottomBox
    bottomBox.add(controlButtonsBox);
    // Add the margin between controlButtonPanel to the bottom line
    bottomBox.add(Box.createVerticalStrut(10));
   
    
    setVisible(false);
   
  }

 
 
}
