/**
 *       Name: OpenDialogBox.java
 *    Purpose: Visual display for OpenDialogBox
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: @Jing Tao@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-04-19 20:44:50 $'
 * '$Revision: 1.21 $'
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
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.util.Command;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.SortableJTable;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;

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
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * A dialog box for user to open a datapakcage
 */
public class OpenCrashedDocDialogBox extends OpenDialogBox
{
  /** A reference to the container Morpho application */
  private Morpho morpho = null;

  /** The configuration options object reference from the framework */
  private ConfigXML config = null;

  /** the reference to mediator */
  private ResultPanelAndFrameMediator mediator = null;

  /** the reference to the owner query */
  private Query ownerQuery = null;

  /** the reference to the parent of dialog */
  private MorphoFrame parentFrame = null;


  //{{DECLARE_CONTROLS
  private JButton openButton = null;
  private JButton cancelButton = null;
  private JButton searchButton = null;
  //}}

  // ResultSet and result panel for the owner
  private ResultSet results = null;
  private ResultPanel ownerPanel = null;

  // DIMENTION Factor to parent
  private static final double DIMENSIONFACTOR = 0.9;
  private static final int PADDINGWIDTH = 8;
  private static final String INFO = "<html><table width=\"100%\"><tr><td valign=\"top\" width=\"100%\">"
  /*+"Morpho did not exit cleanly the last time it was run. "*/
  + Language.getInstance().getMessages("OpenCrashedDocDialogBox.INFO_1") + " "
  /*+"The following documents were automatically saved in an incomplete state. "*/
  + Language.getInstance().getMessages("OpenCrashedDocDialogBox.INFO_2") + " "
  /*+"You may open one of them now to continue from the point of the last automatic save.  "*/ 
  + Language.getInstance().getMessages("OpenCrashedDocDialogBox.INFO_3") + " "
  /*+"If you cancel, you will still be able to access these documents later by using the Open dialog"*/
  + Language.getInstance().getMessages("OpenCrashedDocDialogBox.INFO_4")
  +"</td></tr></table></html>";


  /**
   * Construct a new instance of the query dialog
   *
   * @param parent  The parent frame for this dialog
   * @param morpho  A reference to the Morpho application
   * @param myQuery a Query to get the user's own packages
   */
  public OpenCrashedDocDialogBox(MorphoFrame parent, Morpho morpho, Query myQuery)
  {
    super(parent);
    this.parentFrame = parent;
    this.morpho = morpho;
    this.config = morpho.getConfiguration();
    this.mediator = new ResultPanelAndFrameMediator();
    this.ownerQuery = myQuery;
    boolean findCrachedDoc = query();
    if(findCrachedDoc)
    {
      initGUI();
    }
    else
    {
      this.setVisible(false);
      this.dispose();
    }
   
    
  }
  
  /*
   * Query incomplete directory. If no crashed documents find, false will be returned.
   */
  private boolean query()
  {
    LocalQuery crashedDocQuery = new LocalQuery(ownerQuery, morpho);
    Vector resultVector = null;
    Vector resultVectorWithCrashedDoc = new Vector();
    results = crashedDocQuery.executeInInCompleteDoc();
    if(results != null)
    {
      resultVector = results.getResultsVector();
      if(resultVector != null)
      {
        for(int i=0; i<resultVector.size(); i++)
        {
          Vector row = (Vector)resultVector.elementAt(i);
          String localStatus = (String)row.get(ResultSet.ISLOCALINDEX);
          Log.debug(30, "OpenCrashedDocDialog.query - the docid "+row.elementAt(6)+" has the local status is "+localStatus);
          if(localStatus != null  && localStatus.equals(QueryRefreshInterface.LOCALAUTOSAVEDINCOMPLETE))
          {
            Log.debug(30, "OpenCrashedDocDialog.query - add docid "+row.elementAt(6)+" into results");
            resultVectorWithCrashedDoc.add(row);
          }
        }
      }
    }
    if(resultVectorWithCrashedDoc == null || resultVectorWithCrashedDoc.isEmpty())
    {
      return false;
    }
    else
    {
      Log.debug(30, "OpenCrashedDocDialog.query - result vector size is "+resultVectorWithCrashedDoc.size());
      results.setResultsVector(resultVectorWithCrashedDoc);
      return true;
    }   
  }
  
  private void initGUI()
  {
 // Set OpenDialog size depent on parent size
    int parentWidth = parentFrame.getWidth();
    int parentHeight = parentFrame.getHeight();
    int dialogWidth = 820;
    int dialogHeight = 500;
    setSize(dialogWidth, dialogHeight);

    // Set location of dialog, it shared same center of parent
    double parentX = parentFrame.getLocation().getX();
    double parentY = parentFrame.getLocation().getY();
    double centerX = parentX + 0.5 * parentWidth;
    double centerY = parentY + 0.5 * parentHeight;
    int dialogX = (new Double(centerX - 0.5 * dialogWidth)).intValue();
    int dialogY = (new Double(centerY - 0.5 * dialogHeight)).intValue();
    setLocation(dialogX, dialogY);

    setTitle("Open Recovered Documents");
    // Set the default close operation is dispose
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    // Set the border layout as layout
    getContentPane().setLayout(new BorderLayout(0, 0));

    // Create padding
    /*getContentPane().add(BorderLayout.NORTH,
                                      Box.createVerticalStrut(PADDINGWIDTH));*/
    getContentPane().add(BorderLayout.EAST,
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.WEST,
                                      Box.createHorizontalStrut(PADDINGWIDTH));
    
    //create information box
    Box topBox = Box.createVerticalBox();
    //Create padding between result panel and Contorl button box
    topBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    JLabel info = new JLabel(INFO);
    Dimension dims = new Dimension(820, 80);
    info.setPreferredSize(dims);
    info.setMaximumSize(dims);
    topBox.add(info);
    topBox.add(Box.createVerticalStrut(PADDINGWIDTH));
    getContentPane().add(BorderLayout.NORTH, topBox);
    
    // Create result panel
    createOwnerPanel();
    ownerPanel.setBackground(Color.white);
    // Sort ownerPanel by last updated date
    ownerPanel.sortTable(5, SortableJTable.DECENDING);
    getContentPane().add(BorderLayout.CENTER, ownerPanel);

    // Create bottom box
    Box bottomBox = Box.createVerticalBox();
    getContentPane().add(BorderLayout.SOUTH, bottomBox);
    //Create padding between result panel and Contorl button box
    bottomBox.add(Box.createVerticalStrut(PADDINGWIDTH));

    // Create a controlbuttionBox
    Box controlButtonsBox = Box.createHorizontalBox();
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));

    // Search button
    /*GUIAction searchAction = new GUIAction("Search...", new ImageIcon
       (getClass().getResource("/toolbarButtonGraphics/general/Search16.gif")),
       new SearchCommand(this, morpho));
    searchAction.setToolTipText("Switch to search system to open packages from"
                                + " the whole network");*/
    /*GUIAction searchAction =null;
    searchButton = new JButton(searchAction);
    // Set text on the left of icon
    searchButton.setHorizontalTextPosition(SwingConstants.LEFT);
    controlButtonsBox.add(searchButton);*/

    controlButtonsBox.add(Box.createHorizontalGlue());

    // Open button
    //boolean isCrashedDoc = true;
    GUIAction openAction = new GUIAction(/*"Open"*/ Language.getInstance().getMessages("Open"), null,
                                  new OpenPackageCommand(this));
    //GUIAction openAction = null;
    openButton = new JButton(openAction);
    // Registor open button to mediator
    mediator.registerOpenButton(openButton);
    // After registor resultPanel and open button, init mediator
    mediator.init();
    controlButtonsBox.add(openButton);

    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));

    //Cancel button
    GUIAction cancelAction = new GUIAction(/*"Cancel"*/ Language.getInstance().getMessages("Cancel"), null,
                                                      new CancelCommand(this));
    cancelButton = new JButton(cancelAction);
    controlButtonsBox.add(cancelButton);
    controlButtonsBox.add(Box.createHorizontalStrut(PADDINGWIDTH));

    // Add controlButtonsBox to bottomBox
    bottomBox.add(controlButtonsBox);
    // Add the margin between controlButtonPanel to the bottom line
    bottomBox.add(Box.createVerticalStrut(10));

    // Add a keyPressActionListener
    //this.addKeyListener(new KeyPressActionListener());
    setModal(true);
    setVisible(true);

  }

  /**
   * Method to get the parent morphoFrame of dialog
   */
  public MorphoFrame getParentFrame()
  {
    return parentFrame;
  }//getParent

  /**
   * Method to get the parent morphoFrame of dialog
   */
  public ResultPanel getResultPanel()
  {
    return ownerPanel;
  }//getParent

  /**
   * Method to set a resultpanel to the dialog
   *
   * @param resultPanel the ResultPanel need to be setted
   */
  public void setResultPanel(ResultPanel resultPanel)
  {
    //ownerPanel = resultPanel;
     if (resultPanel != null)
     {
         Container contentPane = getContentPane();
         if (ownerPanel != null)
         {
           contentPane.remove(ownerPanel);
         }
         ownerPanel = resultPanel;
         contentPane.add(BorderLayout.CENTER, ownerPanel);
         validate();

     }

  }

  /**
   * Listens for key events coming from the dialog.  responds to escape and
   * enter buttons.  escape toggles the cancel button and enter toggles the
   * Search button
   */
  class KeyPressActionListener extends java.awt.event.KeyAdapter
  {
    public KeyPressActionListener()
    {
    }

    public void keyPressed(KeyEvent e)
    {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        java.awt.event.ActionEvent event = new
                       java.awt.event.ActionEvent(openButton, 0, "Search");

      } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        dispose();
        java.awt.event.ActionEvent event = new
                       java.awt.event.ActionEvent(cancelButton, 0, "Cancel");

      }
    }
  }

 /**
   * Create the owner JTable with the appropriate query
   */
  private void createOwnerPanel()
  {
	  //LocalQuery crashedDocQuery = new LocalQuery(ownerQuery, morpho);
    //results = crashedDocQuery.executeInInCompleteDoc();
    ownerPanel = new ResultPanel(this, results, mediator);
    //ownerPanel.setVisible(true);
    StateChangeEvent event = null;
    boolean showSearchNumber = false;
    boolean sort = true;
    int index = 5;
    String order = SortableJTable.DECENDING;
    /*ownerQuery.displaySearchResult(parentFrame, ownerPanel, sort,
                             index, order, showSearchNumber, event);*/


   }// createOwnerPanel



}

