/**
 *       Name: SaveDialog.java
 *    Purpose: Visual display for Export Choices
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @higgins@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-11-25 23:20:14 $'
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
package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.framework.UIController;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.util.Vector;


import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.SwingConstants;

/**
 * A dialog box for user choice of export options
 */
public class SaveDialog extends JDialog
{
 
  /** Control button */ 
  private JButton executeButton = null;
  private JButton cancelButton = null;
  
  
  /** Radio button */
  private JCheckBox localLoc = new JCheckBox("Save Locally");
  private JCheckBox networkLoc = new JCheckBox("Save to Network.");
  private JCheckBox saveData = new JCheckBox("Save Data?");
  
  private static final int PADDINGWIDTH = 8;
  private static String WARNING =
      "Please choose where the current DataPackage \n" +
      " should be saved."; 

  /** A reference to morpho frame */
  MorphoFrame morphoFrame = null;
  
  /** A string indicating the morpho frame's type*/
  String morphoFrameType = null;
  
  
  /** selected docid  */
  String selectDocId = null;
    
  /** flag to indicate selected data package has local copy */
  private boolean inLocal = false;
  
  /** flag to indicate selected data package has local copy */
  private boolean inNetwork = false;

  
  /** the AbstractDataPackage object to be saved  */
  AbstractDataPackage adp = null;
  
  
  /**
   * Construct a new instance of the dialog where parent is morphoframe
   *
   */
  public SaveDialog(AbstractDataPackage adp)
  {
    this.adp = adp;
    morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    initialize(morphoFrame);
 
  }
  

  
  /** Method to initialize save dialog */
  private void initialize(Window parent)
  {
     // Set OpenDialog size depent on parent size
    int parentWidth = parent.getWidth();
    int parentHeight = parent.getHeight();
    int dialogWidth = 400;
    int dialogHeight = 270;
    setSize(dialogWidth, dialogHeight);
    setResizable(false);
    
    // Set location of dialog, it shared same center of parent
    double parentX = parent.getLocation().getX();
    double parentY = parent.getLocation().getY();
    double centerX = parentX + 0.5 * parentWidth;
    double centerY = parentY + 0.5 * parentHeight;
    int dialogX = (new Double(centerX - 0.5 * dialogWidth)).intValue();
    int dialogY = (new Double(centerY - 0.5 * dialogHeight)).intValue();
    setLocation(dialogX, dialogY);
    
    setTitle("Save Current DataPackage");
    // Set the default close operation is dispose
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    // Set the border layout as layout
    getContentPane().setLayout(new BorderLayout(0, 0));
     // Add padding for left and right
    getContentPane().add(BorderLayout.EAST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.WEST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
  
   
    
    // Create JPanel and set it border layout
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(0, 0));
    
    // Create note box and add it to the north of mainPanel
    Box noteBox = Box.createVerticalBox();
    noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    JTextArea note = new JTextArea(WARNING);
    note.setEditable(false);
    note.setLineWrap(true);
    note.setWrapStyleWord(true);
    note.setOpaque(false);
    noteBox.add(note);
    noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    mainPanel.add(BorderLayout.NORTH, noteBox);
    
    // Create a radio box 
    Box radioBox = Box.createVerticalBox();
    radioBox.add(localLoc);
    radioBox.add(networkLoc);
    radioBox.add(saveData);
    
    // create another center box which will put radion box in the center
    // and it will be add into center of mainPanel
    Box centerBox = Box.createHorizontalBox();
    centerBox.add(Box.createHorizontalGlue());
    centerBox.add(radioBox);
    centerBox.add(Box.createHorizontalGlue());
    mainPanel.add(BorderLayout.CENTER, centerBox);
   
    // Finish mainPanel and add it the certer of contentpanel    
    getContentPane().add(BorderLayout.CENTER, mainPanel);
    
    // Create bottom box
    Box bottomBox = Box.createVerticalBox();
    getContentPane().add(BorderLayout.SOUTH, bottomBox);
    //Create padding between result panel and Contorl button box
    bottomBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    // Create a controlbuttionBox
    Box controlButtonsBox = Box.createHorizontalBox();
    controlButtonsBox.add(Box.createHorizontalGlue());
    
    // Diable the execute action
    executeButton = new JButton("Save");   
    controlButtonsBox.add(executeButton);
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));
    
    //Cancel button
    cancelButton = new JButton("Cancel");
    controlButtonsBox.add(cancelButton);
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));
    
    // Add controlButtonsBox to bottomBox
    bottomBox.add(controlButtonsBox);
    // Add the margin between controlButtonPanel to the bottom line
    bottomBox.add(Box.createVerticalStrut(10));
    
		SymAction lSymAction = new SymAction();
		executeButton.addActionListener(lSymAction);
		cancelButton.addActionListener(lSymAction);
    
    setVisible(true);
   
  }
  
    /** Method to enable executeButton and assign command */
   private void enableExecuteButton(Object object, JDialog dialog)
   {
   }//enableExecuteButton
 

	class SymAction implements java.awt.event.ActionListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == executeButton) {
				executeButton_actionPerformed(event);
      }
      else if (object == cancelButton) {
        cancelButton_actionPerformed(event);
      }
		}
	}
  
  void cancelButton_actionPerformed(java.awt.event.ActionEvent event)
	{
		this.setVisible(false);
		this.dispose();
	}

  void executeButton_actionPerformed(java.awt.event.ActionEvent event)
	{
    Morpho morpho = Morpho.thisStaticInstance;
    String loc = adp.getLocation();
    Log.debug(1,"Location (inside Save): "+loc);
    if ((loc.equals(AbstractDataPackage.METACAT))||
          (loc.equals(AbstractDataPackage.LOCAL))||
          (loc.equals(AbstractDataPackage.BOTH))) {  
      // package exists, so just increment version number        
      String id = adp.getAccessionNumber();
      AccessionNumber an = new AccessionNumber(morpho);
      String newid = an.incRev(id);
    Log.debug(1, "New ID: "+newid); 
    adp.setAccessionNumber(newid);
      if((loc.equals(AbstractDataPackage.LOCAL))||(loc.equals(AbstractDataPackage.BOTH))) {
        //save locally
    Log.debug(1, "inside save local"); 
        adp.serialize(AbstractDataPackage.LOCAL);
      }
      if((loc.equals(AbstractDataPackage.METACAT))||(loc.equals(AbstractDataPackage.BOTH))) {
        //save to metacat
    Log.debug(1, "inside save metacat"); 
        adp.serialize(AbstractDataPackage.METACAT);
      }
    
    }
    else { 
    Log.debug(1, "inside else"); 
      // a new package, so get a new id
      if ((localLoc.isSelected())&&(networkLoc.isSelected())) {
          
      }
      else if (localLoc.isSelected()) {

      }
      else if (networkLoc.isSelected()) {
        
      }
      else {
        Log.debug(1, "No location for saving is selected!");
        return;
      }
   }
		this.setVisible(false);
		this.dispose();
	}

 
}//ExportDialog
