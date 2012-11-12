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
 * '$Revision: 1.7 $'
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
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;

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
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.SwingConstants;

import edu.ucsb.nceas.morpho.Language;//pstango 2010/03/15

/**
 * A dialog box for user delete datapakcage
 */
public class DeleteDialog extends JDialog
{

  /** Contorl button */ 
  private JButton executeButton = null;
  private JButton cancelButton = null;
  
  /** Action for executeButton */
  private GUIAction executeAction = new GUIAction(/*"Delete"*/ Language.getInstance().getMessage("Delete"), null, null);
  
  /** Radio button */
  private JRadioButton deleteLocal = new JRadioButton(/*"Delete local copy"*/ Language.getInstance().getMessage("DeleteLocalCopy"));
  private JRadioButton deleteNetwork = new JRadioButton(/*"Delete network copy"*/ Language.getInstance().getMessage("DeleteNetworkCopy"));
  private JRadioButton deleteBoth = new JRadioButton(/*"Delete both copies"*/ Language.getInstance().getMessage("DeleteBothCopies"));
  private JRadioButton deleteIncomplete = new JRadioButton(/*"Delete local incomplete copy"*/ Language.getInstance().getMessage("DeleteLocalIncompleteCopy"));
  
  private static final int PADDINGWIDTH = 8;
  private static String WARNING =
      /*"Are you sure you want to delete the data package"*/ Language.getInstance().getMessage("Delete.Warring_1") +"? \n" +
      /*"If yes, please choose one option and click the Delete button."*/ Language.getInstance().getMessage("Delete.Warring_2"); 

  /** A reference to morpho frame */
  MorphoFrame morphoFrame = null;
  
  /** A string indicating the morpho frame's type*/
  String morphoFrameType = null;
  
  /** A reference to open dialog*/
  OpenDialogBox openDialog = null;    
  
  /** selected docid to delete */
  String selectDocId = null;
    
  /** flag to indicate selected data package has local copy */
  private String  packageLocalStatus = null;
  
  /** flag to indicate selected data package has local copy */
  private String packageNetworkStatus = null;

  
  /**
   * Construct a new instance of the delete dialog which parent is morphoframe
   *
   * @param myParent  The parent frame for this dialog
   * @param frameType the frame is result search result frame or data package
   * @param mySelecteDocId the selected docid
   * @param myInLocal the local status of the datapackage 
   * @param myInNetwork the new status of  datapackage
   */
  public DeleteDialog(MorphoFrame myParent, String frameType, 
                      String mySelectedDocId, String myInLocal, 
                      String myInNetwork)
  {
    super(myParent);
    morphoFrame = myParent;
    morphoFrameType = frameType;
    selectDocId = mySelectedDocId;
    packageLocalStatus = myInLocal;
    packageNetworkStatus = myInNetwork;
    initialize(morphoFrame);
 
  }
  
  /**
   * Construct a new instance of the delete dialog which parent is open dialog
   *
   * @param myParent  The parent open dialog for this dialog
   * @param grandParent the parent frame of myParent open dialog
   * @param mySelecteDocId the selected docid
   * @param myInLocal if the datapackage is in local
   * @param myInNetwork if the datapackage is in network
   */
  public DeleteDialog(OpenDialogBox myParent, MorphoFrame grandParent, 
              String mySelectedDocId, String myInLocal, String myInNetwork)
  {
    super(myParent); 
    openDialog = myParent;
    morphoFrame = grandParent;
    selectDocId = mySelectedDocId;
    packageLocalStatus = myInLocal;
    packageNetworkStatus = myInNetwork;
    initialize(openDialog);
 
  }
  
  /** Method to initialize delete dialog */
  private void initialize(Window parent)
  {
     // Set OpenDialog size depent on parent size
    int parentWidth = parent.getWidth();
    int parentHeight = parent.getHeight();
    int dialogWidth = 400;
    int dialogHeight = 300;
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
    
    setTitle(/*"Delete"*/ Language.getInstance().getMessage("Delete"));
    // Set the default close operation is dispose
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    
    // Set the border layout as layout
    getContentPane().setLayout(new BorderLayout(0, 0));
     // Add padding for left and right
    getContentPane().add(BorderLayout.EAST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.WEST, 
                                      Box.createHorizontalStrut(PADDINGWIDTH));
  
   
    // Initially set radio button disable
    deleteLocal.setEnabled(false);
    deleteNetwork.setEnabled(false);
    deleteBoth.setEnabled(false);
    deleteIncomplete.setEnabled(false);
    // Put them into group
    ButtonGroup group = new ButtonGroup();
    group.add(deleteLocal);
    group.add(deleteNetwork);
    group.add(deleteBoth);
    group.add(deleteIncomplete);
    // Vector to keep track of enabled radio button
    Vector enabledRadioButtonList = new Vector();
    
    if(packageLocalStatus != null && 
      (packageLocalStatus.equals(QueryRefreshInterface.LOCALAUTOSAVEDINCOMPLETE)|| 
       packageLocalStatus.equals(QueryRefreshInterface.LOCALUSERSAVEDINCOMPLETE)))
    {
      deleteIncomplete.setEnabled(true);
      enabledRadioButtonList.add(deleteIncomplete);
    }
    else
    {
      // If has local copy
      if (packageLocalStatus != null && packageLocalStatus.equals(DataPackageInterface.LOCAL))
      {
        deleteLocal.setEnabled(true);
        // Add to the list
        enabledRadioButtonList.add(deleteLocal);
      }
      // If has network copy
      if (packageNetworkStatus != null && packageNetworkStatus.equals(DataPackageInterface.NETWORK))
      {
        deleteNetwork.setEnabled(true);
        // Add to the list
        enabledRadioButtonList.add(deleteNetwork);
      }
      // If have both
      if (packageLocalStatus != null && packageLocalStatus.equals(DataPackageInterface.LOCAL)&&
          packageNetworkStatus != null && packageNetworkStatus.equals(DataPackageInterface.NETWORK))
      {
        deleteBoth.setEnabled(true);
        // Add to the list
        enabledRadioButtonList.add(deleteBoth);
      }
    }
   
    
    
    // Create JPanel and set it border layout
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(0, 0));
    
    // Create note box and add it to the north of mainPanel
    Box noteBox = Box.createVerticalBox();
    noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    JLabel note = WidgetFactory.makeHTMLLabel(WARNING, 3);
/*    JTextArea note = new JTextArea(WARNING);
    note.setEditable(false);
    note.setLineWrap(true);
    note.setWrapStyleWord(true);
    note.setOpaque(false);
*/
    noteBox.add(note);
    noteBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    mainPanel.add(BorderLayout.NORTH, noteBox);
    
    // Create a radio box 
    Box radioBox = Box.createVerticalBox();
    radioBox.add(deleteLocal);
    radioBox.add(deleteNetwork);
    radioBox.add(deleteBoth);
    radioBox.add(deleteIncomplete);
    
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
    executeAction.setEnabled(false);
    executeButton = new JButton(executeAction);   
    controlButtonsBox.add(executeButton);
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));
    
    //Cancel button
    GUIAction cancelAction = new GUIAction(/*"Cancel"*/ Language.getInstance().getMessage("Cancel"), null, 
                                                      new CancelCommand(this));
    cancelButton = new JButton(cancelAction);
    controlButtonsBox.add(cancelButton);
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));
    
    // Add controlButtonsBox to bottomBox
    bottomBox.add(controlButtonsBox);
    // Add the margin between controlButtonPanel to the bottom line
    bottomBox.add(Box.createVerticalStrut(10));
    
    // set the first radiobutton in enableRadioButtonList as the default 
    // selected radio button and enable excecute button
    if (enabledRadioButtonList.size() > 0)
    {
      Object obj = enabledRadioButtonList.elementAt(0);
      JRadioButton selectedRadioButton = (JRadioButton) obj;
      selectedRadioButton.setSelected(true);
      enableExecuteButton(selectedRadioButton, this);
    }
      
   
    // Register listener for radio button
    RadioButtonListener listener = new RadioButtonListener(this);
    deleteLocal.addItemListener(listener);
    deleteNetwork.addItemListener(listener);
    deleteBoth.addItemListener(listener);
    deleteIncomplete.addItemListener(listener);
    setVisible(false);
   
  }
  
    /** Method to enable executeButton and assign command */
   private void enableExecuteButton(Object object, JDialog dialog)
   {
     /*Log.debug(5, "local status is "+packageLocalStatus+
         "\nnewtwork status is "+packageNetworkStatus+"\non search DeleteDialog");*/
      if (object == deleteLocal) 
      {
        // Enable execute button
        Log.debug(30, "In delete local branch");
        executeAction.setEnabled(true);
        executeAction.setCommand( new DeleteCommand(openDialog, dialog, 
                       morphoFrame, morphoFrameType, DataPackageInterface.LOCAL, 
                       selectDocId, packageLocalStatus, packageNetworkStatus));
      } 
      else if (object == deleteNetwork) 
      {
        // Enable execute button
        Log.debug(30, "In delete network branch");
        executeAction.setEnabled(true);
        executeAction.setCommand( new DeleteCommand(openDialog, dialog,
                     morphoFrame, morphoFrameType, DataPackageInterface.NETWORK, 
                     selectDocId, packageLocalStatus, packageNetworkStatus));
      }
      else if (object == deleteBoth)
      {
         // Enable execute button
        Log.debug(50, "In delete both branch");
        executeAction.setEnabled(true);
        executeAction.setCommand( new DeleteCommand(openDialog, dialog, 
                        morphoFrame,morphoFrameType, DataPackageInterface.BOTH, 
                        selectDocId, packageLocalStatus, packageNetworkStatus));
      }
      else if(object == deleteIncomplete)
      {
        Log.debug(50, "In delete both branch");
        executeAction.setEnabled(true);
        executeAction.setCommand( new DeleteCommand(openDialog, dialog, 
                        morphoFrame,morphoFrameType, QueryRefreshInterface.LOCALINCOMPLETEPACKAGE, 
                        selectDocId, packageLocalStatus, packageNetworkStatus));
      }
   }//enableExecuteButton
 

  /** Class to listen for ItemEvents */
  private class RadioButtonListener implements ItemListener 
  {
    private JDialog dialogs = null;
       
    public RadioButtonListener(JDialog myDialog)
    {
      dialogs = myDialog;
    }
    
    public void itemStateChanged(ItemEvent event) 
    {
      Object obj = event.getItemSelectable();
      enableExecuteButton(obj, dialogs);
    }//itemStateChagned
  }//RadioButtonListener
 
}//DeleteDialog
