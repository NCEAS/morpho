/**
 *  '$RCSfile: ModalDialog.java,v $'
 *    Purpose: A class that handles xml messages passed by the
 *             package wizard
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Chad Berkley
 *    Release: @release@
 *
 *   '$Author: leinfelder $'
 *     '$Date: 2008-11-19 01:38:39 $'
 * '$Revision: 1.5 $'
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

package edu.ucsb.nceas.morpho.framework;

import edu.ucsb.nceas.morpho.util.UISettings;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;


public class ModalDialog extends JDialog {

  public short USER_RESPONSE;

  public static final short OK_OPTION      = 10;
  public static final short CANCEL_OPTION  = 20;
  public static final short CLOSED_OPTION  = 30;

  private final int PADDING = 5;

  protected AbstractUIPage wizardPage;

  public ModalDialog(AbstractUIPage page,
                           Frame parent, int width, int height) {

    this(page, parent, width, height, true);
  }

  public ModalDialog(AbstractUIPage page, Frame parent,
                           int width, int height, boolean showNow) {

    super(parent, true);
    this.parent = parent;
    this.wizardPage = page;
    this.wizardPage.setModalDialog(this);
    this.dialogWidth = width;
    this.dialogHeight = height;
    init();
    this.setVisible(showNow);
    validate();
  }

  private void init() {

    resetBounds();
    initContentPane();
    initTopPanel();
    initMiddlePanel();
    initBottomPanel();
    initButtons();

    /* onLoadAction() should be called for the wizardPage which is being loaded
     */
    this.wizardPage.onLoadAction();
  }

  /**
   *  The action to be executed when the "OK" button is pressed. If no onAdvance
   *  processing is required, implementation must return boolean true.
   *
   *  @return boolean true if dialog should close and return to wizard, false
   *          if not (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction() {

    if (wizardPage == null) return false;
    return wizardPage.onAdvanceAction();
  }



  /**
   *  resets location and dimensions to original values
   */
  public void resetBounds() {

    int xcoord, ycoord;
    if(parent == null) {
      xcoord = ycoord = 50;
    } else {
      xcoord = ( parent.getX() + parent.getWidth()/2 )
                                              - this.dialogWidth/2;
      ycoord = ( parent.getY() + parent.getHeight()/2 )
                                              - this.dialogHeight/2;
    }

    this.setBounds(xcoord, ycoord,  this.dialogWidth,
                                    this.dialogHeight);
  }

  private void initContentPane() {

    contentPane = this.getContentPane();
    contentPane.setLayout(new BorderLayout());
  }


  private void initTopPanel() {

    contentPane.add(Box.createRigidArea(new Dimension(15, 15)),
                    BorderLayout.NORTH);

  }

  private void initMiddlePanel() {

    middlePanel = new JPanel();
    middlePanel.setLayout(new BorderLayout());
    if(wizardPage != null) {
      middlePanel.add(wizardPage,BorderLayout.CENTER);

    }

    //middlePanel.setBorder(new EmptyBorder(PADDING,3*PADDING,PADDING,3*PADDING));
    contentPane.add(wizardPage, BorderLayout.CENTER);
    wizardPage.validate();
    validate();
  }

  private void initBottomPanel() {

    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
    bottomPanel.add(Box.createHorizontalGlue());
    bottomPanel.setOpaque(false);

    bottomPanel.setBorder(
                  BorderFactory.createMatteBorder(2, 0, 0, 0, UISettings.TITLEBAR_COLOR));
    contentPane.add(bottomPanel, BorderLayout.SOUTH);
  }

  private void initButtons()  {

    okButton  = addButton(UISettings.OK_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okAction();
      }
    });
    addButton(UISettings.CANCEL_BUTTON_TEXT, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelAction();
      }
    });
    this.getRootPane().setDefaultButton(okButton);
  }


  /**
   * adds a button with a specified title and action to the bottom panel
   *
   * @param title text to be shown on the button
   * @param actionListener the ActionListener that will respond to the button
   *   press
   * @return JButton
   */
  private JButton addButton(String title, ActionListener actionListener) {

    JButton button = new JButton(title);
    button.setForeground(UISettings.POPUPDIALOG_BUTTON_TEXT_COLOR);
    button.setFont(UISettings.POPUPDIALOG_BUTTON_FONT);
    if (actionListener!=null) button.addActionListener(actionListener);
    bottomPanel.add(button);
    bottomPanel.add(Box.createHorizontalStrut(PADDING));
    return button;
  }


  public void okAction() {

    USER_RESPONSE = OK_OPTION;
    if (onAdvanceAction()) {
    	this.setVisible(false);
    	if (this.parent != null) {
          this.parent.toFront();
        }
        this.dispose();
    }
  }

  private void cancelAction() {

    USER_RESPONSE = CANCEL_OPTION;
    if (wizardPage != null) {
    	wizardPage.cancelAction();
    }
    this.setVisible(false);
    if (this.parent != null) {
      this.parent.toFront();
    }
    this.dispose();
  }




  // * * *  P R I V A T E   V A R I A B L E S  * * * * * * * * * * * * * * * * *


  private int dialogWidth;
  private int dialogHeight;
  private Frame parent;
  private Container contentPane;
  protected JPanel middlePanel;
  private JPanel bottomPanel;
  private JButton okButton;
}
