/**
 *       Name: SynchronizeDialog.java
 *    Purpose: Visual display for OpenDialogBox
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @Jing Tao@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2004-03-31 23:33:46 $'
 * '$Revision: 1.10 $'
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

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;

import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;


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

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

/**
 * A dialog box for user to synchronize data package
 */
public class SynchronizeDialog extends JDialog
{
 
  /** Contorl button */ 
  private JButton executeButton = null;
  private JButton cancelButton = null;
  
  /** Icons  */
  private ImageIcon localIcon 
      = new ImageIcon(getClass().getResource("local-package.png"));
  private ImageIcon networkIcon 
      = new ImageIcon(getClass().getResource("network-package-large.png"));
  private ImageIcon arrowIcon = null;
    
  private static final int PADDINGWIDTH = 8;
  
  private static String SYNCHRONIZE =
      /*
      "\"Synchronize\" will keep the Data Packages on your local computer "
      + "identical with those on the network. "
      + "In order to do this, Morpho will copy the Data Package as shown below:"
      */
	  Language.getInstance().getMessage("SynchronizeDialog.Description") +" :"
      ;
 
  private static String WARNING =
      /*"Note:\n"*/ Language.getInstance().getMessage("Note") + " :\n  "
      /*+"  If you are copying from local to network, you may be"
      + " prompted to renumber the Data Package"*/
      + Language.getInstance().getMessage("SynchronizeDialog.Warning_1")
      ;

  /** A reference to morpho frame */
  private MorphoFrame morphoFrame = null;
  
  /** Type of morpho frame, search result or package frame */
  private String morphoFrameType = null;
  
  /** A reference to open dialog */
  private OpenDialogBox openDialog = null;
  
  /** A flag indicate if it come from a open dialog */
  private boolean comeFromOpen = false;
  
  /** Docid need to be synchronized */
  private String docid = null;
  
  /** flag to indicate selected data package has local copy */
  private boolean inLocal = false;
  
  /** flag to indicate selected data package has local copy */
  private boolean inNetwork = false;
  
  /**
   * Construct a new instance of the synchronize dialog which's parent is
   * morpho frame
   *
   * @param parent  The parent frame for this dialog
   * @param frameType the paremt frame's type, search result frame or package 
   * @param myDocid the docid need to be synchronized
   * @param myInLocal if the docid is in local
   * @param myInNetwork if the docid is in Network
   */
  public SynchronizeDialog(MorphoFrame parent, String frameType,
                           String myDocid, boolean myInLocal, 
                           boolean myInNetwork)
  {
    super(parent);
    morphoFrame = parent;
    morphoFrameType = frameType;
    this.docid = myDocid;
    this.inLocal = myInLocal;
    this.inNetwork = myInNetwork;
    initialize(morphoFrame);
  
  }

  /**
   * Construct a new instance of the synchronize dialog which's parent is
   * a open dialog
   *
   * @param open  the parent (open dialog) of this synchronize dialog
   * @param frame  the parent frame for open dialog
   * @param myDocid  the docid need to be synchronized
   * @param myInLocal  if the docid is in local
   * @param myInNetwork  if the docid is in Network
   */
  public SynchronizeDialog(OpenDialogBox open,MorphoFrame frame, String myDocid, 
                                        boolean myInLocal, boolean myInNetwork)
  {
    super(open);
    openDialog = open;
    morphoFrame = frame;
    this.docid = myDocid;
    this.inLocal = myInLocal;
    this.inNetwork = myInNetwork;
    initialize(openDialog);
  
  }

  
  /** A method to initialize synchronize dialog */
  private void initialize(Window parent)
  {
    // Set OpenDialog size depent on parent size
    int parentWidth = parent.getWidth();
    int parentHeight = parent.getHeight();
    int dialogWidth = 400;
    int dialogHeight = 275;
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
    
    setTitle(/*"Synchronize"*/ Language.getInstance().getMessage("Synchronize"));
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
    note.setWrapStyleWord(true);
    note.setOpaque(false);
    note.setFont(new Font("SansSerif", Font.PLAIN, 11));
    noteBox.add(note);
    // Add left and right padding for notebox and add them to the north of
    // of content panel
    Box notePadding = Box.createHorizontalBox();
    notePadding.add(Box.createHorizontalStrut(PADDINGWIDTH));
    notePadding.add(noteBox);
    notePadding.add(Box.createHorizontalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.NORTH, notePadding);
    
    // Add padding for left and right
    getContentPane().add(BorderLayout.EAST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.WEST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
   
    GUIAction executeAction = null;
    String warningMessage = null;
    // If inLocal and not inNetwork do upload
    if (inLocal && !inNetwork)
    {
     
      arrowIcon = new ImageIcon(getClass().getResource("rightarrow.gif"));
      executeAction = new GUIAction(/*"Execute"*/ Language.getInstance().getMessage("Execute"), null, new LocalToNetworkCommand
                                   (openDialog, this, morphoFrame, 
                                   morphoFrameType, docid, inLocal, inNetwork)); 
      warningMessage = WARNING;
    }
    // down load 
    if (!inLocal && inNetwork)
    {
      
      arrowIcon = new ImageIcon(getClass().getResource("leftarrow.gif"));
      executeAction = new GUIAction(/*"Execute"*/ Language.getInstance().getMessage("Execute"), null, new NetworkToLocalCommand
                                   (openDialog, this, morphoFrame, 
                                   morphoFrameType, docid, inLocal, inNetwork));
      warningMessage = "";
    }
    
     // Create icon box
    Box iconBox = Box.createHorizontalBox();
    JLabel localLabel = new JLabel(/*"Local"*/ Language.getInstance().getMessage("Local") , localIcon, SwingConstants.RIGHT);
    //localLabel.setText("Local");
    localLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
    JLabel networkLabel = new JLabel(/*"Network"*/ Language.getInstance().getMessage("Network"), networkIcon, 
                                                      SwingConstants.RIGHT );
    //networkLabel.setText("Network");
    networkLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
    JLabel arrowLabel = new JLabel(arrowIcon);
    
    iconBox.add(Box.createHorizontalGlue());
    iconBox.add(localLabel);
    iconBox.add(Box.createHorizontalStrut(20));
    iconBox.add(arrowLabel);
    iconBox.add(Box.createHorizontalStrut(20));
    iconBox.add(networkLabel);
    iconBox.add(Box.createHorizontalGlue());
    
    getContentPane().add(BorderLayout.CENTER, iconBox);
    
    // Create bottom box
    Box bottomBox = Box.createVerticalBox();
    //Create padding between result panel and Contorl button box
    bottomBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    JTextArea warning = new JTextArea(warningMessage);
    warning.setEditable(false);
    warning.setLineWrap(true);
    warning.setWrapStyleWord(true);
    warning.setOpaque(false);
    warning.setFont(new Font("SansSerif", Font.PLAIN, 11));

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
    GUIAction cancelAction = new GUIAction(/*"Cancel"*/ Language.getInstance().getMessage("Cancel"), null, 
                                                      new CancelCommand(this));
    cancelButton = new JButton(cancelAction);
    controlButtonsBox.add(cancelButton);
    //controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));
    
    // Add controlButtonsBox to bottomBox
    bottomBox.add(controlButtonsBox);
    // Add the margin between controlButtonPanel to the bottom line
    bottomBox.add(Box.createVerticalStrut(10));
   
    // Add left and right padding for bottomBox and add it to south of content
    // panel
    Box bottomPadding = Box.createHorizontalBox();
    bottomPadding.add(Box.createHorizontalStrut(PADDINGWIDTH));
    bottomPadding.add(bottomBox);
    bottomPadding.add(Box.createHorizontalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.SOUTH, bottomPadding);
    
    setVisible(false);
  }
 
}
