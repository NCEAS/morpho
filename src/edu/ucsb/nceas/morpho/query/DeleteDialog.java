/**
 *       Name: SynchronizeDialog.java
 *    Purpose: Visual display for OpenDialogBox
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @Jing Tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-09-04 23:25:51 $'
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
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;


import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.SwingConstants;

/**
 * A dialog box for user delete datapakcage
 */
public class DeleteDialog extends JDialog
{
 
  /** Contorl button */ 
  private JButton executeButton = null;
  private JButton cancelButton = null;
  
  /** Action for executeButton */
  private GUIAction executeAction = new GUIAction("Execute", null, null);
  
  /** Radio button */
  private JRadioButton deleteLocal = new JRadioButton("Delete local copy");
  private JRadioButton deleteNetwork = new JRadioButton("Delete network copy");
  private JRadioButton deleteBoth = new JRadioButton("Delete both copies");
  
  private static final int PADDINGWIDTH = 8;
  private static String WARNING =
      "Are you sure to delete the data package? \n" +
      "If yes, please chose one option and click Execute button, " +
      "else please click Cancel button.";
  /** A reference to morpho frame */
  MorphoFrame parent = null;    
  
  /** selected docid to delete */
  String selectDocId = null;
    
  /** flag to indicate selected data package has local copy */
  private boolean inLocal = false;
  
  /** flag to indicate selected data package has local copy */
  private boolean inNetwork = false;

  
  /**
   * Construct a new instance of the synchonize dialog
   *
   * @param myParent  The parent frame for this dialog
   * @param mySelecteDocId the selected docid
   * @param myInLocal if the datapackage is in local
   * @param myInNetwork if the datapackage is in network
   */
  public DeleteDialog(MorphoFrame myParent, String mySelectedDocId, 
                                      boolean myInLocal, boolean myInNetwork)
  {
    super(myParent);
    parent = myParent;
    selectDocId = mySelectedDocId;
    inLocal = myInLocal;
    inNetwork = myInNetwork;
    
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
    
    setTitle("Delete");
    // Set the default close operation is dispose
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    // Set the border layout as layout
    getContentPane().setLayout(new BorderLayout(0, 0));
     // Add padding for left and right
    getContentPane().add(BorderLayout.EAST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.WEST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    // Create panel to store the note
    Box noteBox = Box.createVerticalBox();
    noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    JTextArea note = new JTextArea(WARNING);
    note.setEditable(false);
    note.setLineWrap(true);
    note.setOpaque(false);
    noteBox.add(note);
    getContentPane().add(BorderLayout.NORTH, noteBox);
    
   
    // Initially set radio button disable
    deleteLocal.setEnabled(false);
    deleteNetwork.setEnabled(false);
    deleteBoth.setEnabled(false);
    // Put them into group
    ButtonGroup group = new ButtonGroup();
    group.add(deleteLocal);
    group.add(deleteNetwork);
    group.add(deleteBoth);
    // If has local copy
    if (inLocal)
    {
      deleteLocal.setEnabled(true);  
    }
    // If has network copy
    if (inNetwork)
    {
      deleteNetwork.setEnabled(true);
    }
    // If have both
    if (inLocal && inNetwork)
    {
      deleteBoth.setEnabled(true);
    }
    
     // Create icon box
    Box radioBox = Box.createVerticalBox();
    radioBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    radioBox.add(deleteLocal);
    radioBox.add(deleteNetwork);
    radioBox.add(deleteBoth);
    getContentPane().add(BorderLayout.CENTER, radioBox);
    
    // Create bottom box
    Box bottomBox = Box.createVerticalBox();
    getContentPane().add(BorderLayout.SOUTH, bottomBox);
    //Create padding between result panel and Contorl button box
    bottomBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    // Create a controlbuttionBox
    Box controlButtonsBox = Box.createHorizontalBox();
    controlButtonsBox.add(Box.createHorizontalGlue());
    
    // Diable the execute action
    executeAction.setEnabled(false);
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
   
    // Register listener for radio button
    RadioButtonListener listener = new RadioButtonListener(this);
    deleteLocal.addItemListener(listener);
    deleteNetwork.addItemListener(listener);
    deleteBoth.addItemListener(listener);
    
    setVisible(false);
   
  }

  /** Class to listen for ItemEvents */
  private class RadioButtonListener implements ItemListener 
  {
    private JDialog dialog = null;
       
    public RadioButtonListener(JDialog myDialog)
    {
      dialog = myDialog;
    }
    
    public void itemStateChanged(ItemEvent event) 
    {
      Object object = event.getItemSelectable();

      if (object == deleteLocal) 
      {
        // Enable execute button
        Log.debug(45, "In delete local branch");
        executeAction.setEnabled(true);
        executeAction.setCommand( new DeleteCommand(dialog, parent, 
            DataPackageInterface.LOCAL, selectDocId, inLocal, inNetwork));
      } 
      else if (object == deleteNetwork) 
      {
        // Enable execute button
        Log.debug(50, "In delete network branch");
        executeAction.setEnabled(true);
        executeAction.setCommand( new DeleteCommand(dialog, parent, 
            DataPackageInterface.METACAT, selectDocId, inLocal, inNetwork));
      }
      else if (object == deleteBoth)
      {
         // Enable execute button
        Log.debug(50, "In delete both branch");
        executeAction.setEnabled(true);
        executeAction.setCommand( new DeleteCommand(dialog, parent, 
              DataPackageInterface.BOTH, selectDocId, inLocal, inNetwork));
      }
      
    }//itemStateChagned
  }//RadioButtonListener
 
}//DeleteDialog
