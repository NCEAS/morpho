/**
 *  '$RCSfile: ProgressBarThread.java,v $'
 *    Purpose: A class that shows a progress bar...
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Authors: Saurabh Garg
 *    Release: @release@
 *
 *   '$Author: sgarg $'
 *     '$Date: 2004-04-13 01:10:28 $'
 * '$Revision: 1.4 $'
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
package edu.ucsb.nceas.morpho.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import javax.swing.Action;

public class ProgressBarThread
    extends Thread {

  private JDialog parentDialog = null;
  private JDialog dialog = null;
  private JProgressBar progressBar = null;
  private Timer timer = null;
  private boolean increaseValue = true;
  private JButton cancelButton;
  private int progressUpdateTime = 30;
  private int maximumValue = 100;
  private String pBarString = "";
  private int progressUpdateValue = 2;
  private Action customCancelAction;

  /**
   * Create a new progress bar object with parentDialog as null
   * and initialize a javax.swing.JProgressBar object.
   */
  public ProgressBarThread() {
    parentDialog = null;

    progressBar = new JProgressBar();
    progressBar.setMaximum(maximumValue);
    progressBar.setStringPainted(true);
    progressBar.setString(pBarString);
  }

  /**
   * Create a new progress bar object with parentDialog passed as parameter
   * and initialize a javax.swing.JProgressBar object.
   *
   * @param parentDialog: the JDialog object which will be parent of this
   *                      Component
   */
  public ProgressBarThread(JDialog parentDialog) {
    this.parentDialog = parentDialog;

    progressBar = new JProgressBar();
    progressBar.setMaximum(maximumValue);
    progressBar.setStringPainted(true);
    progressBar.setString(pBarString);
  }


  /**
   * Execute the thread.
   */
  public void run() {
    // create the JDialog
    if(parentDialog != null) {
      dialog = new JDialog( (java.awt.Dialog) parentDialog, //owner
          "Generating Access Tree", // title
          true); // modal
    } else {
    	MorphoFrame defaultOwner = UIController.getInstance().getCurrentActiveWindow();
    	dialog = new JDialog(defaultOwner, //owner
               "Generating Access Tree", // title
               true); // modal
    }

    // create the ProgressBar
    progressBar.setPreferredSize(new Dimension(400, 30));

    // create cancel button, its action listener and its panel
    ActionListener actionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (customCancelAction != null) {
          customCancelAction.actionPerformed(null);
        }
        timer.stop();
        dialog.dispose();
      }
    };
    cancelButton = WidgetFactory.makeJButton("Cancel",
        actionListener);
    JPanel cancelButtonPanel = new JPanel();
    cancelButtonPanel.add(cancelButton);

    // get dialog's content pane and put progress bar and cancel button in it
    Container dialogContentPane = dialog.getContentPane();
    dialogContentPane.add(progressBar, BorderLayout.CENTER);
    dialogContentPane.add(cancelButtonPanel, BorderLayout.SOUTH);

    // create actionlistener for the timer... the action is performed every
    // 'delay' miliseconds and it sets the value of progress bar....
    ActionListener taskPerformer = new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        //change value of progressBar...
        int value = progressBar.getValue();
        if (value == maximumValue) {
          increaseValue = false;
        }
        if (value == 0) {
          increaseValue = true;
        }

        if (increaseValue) {
          value += progressUpdateValue;
        } else {
          value -= progressUpdateValue;
        }
        progressBar.setValue(value);
      }
    };

    // create the timer and execute it repeatedly...
    timer = new Timer(progressUpdateTime, taskPerformer);
    timer.setRepeats(true);
    timer.start();

    // dialog is ready to go ... display the dialog relative to parent dialog
    dialog.setLocationRelativeTo(parentDialog);
    dialog.pack();
    dialog.show();
  }

  /**
   * Stops the timer, closes the dialog and exits.
   */
  public void exitProgressBarThread() {
    timer.stop();
    dialog.dispose();
  }

  /**
   *  gets the progress bar
   *
   *  @return   the Progress Bar being used in this dialog
   */
  public JProgressBar getProgressBar() {
    return progressBar;
  }

  /**
   * Method to set the progress bar.
   *
   * @param progressBar JProgressBar
   */
  public void setProgressBar(JProgressBar progressBar) {
    this.progressBar = progressBar;
  }

  /**
   *  gets the time after which progress bar is updated
   *
   *  @return   the time after which progress bar is updated...
   */
  public int getProgressUpdateTime() {
    return progressUpdateTime;
  }

  /**
   *  gets the maximum value of the progress bar
   *
   *  @return   gets the maximum value of the progress bar.
   */
  public int getMaximumValue() {
    return maximumValue;
  }

  /**
   *  gets the string that is shown on the progress bar.
   *
   *  @return   the String that is shown on the progress bar.
   */
  public String getProgressBarString() {
    return progressBar.getString();
  }

  /**
   *  gets the integer value by which progress bar is updated
   *
   *  @return   the Integer value by which progress bar is updated
   */
  public int getProgressUpdateValue() {
    return progressUpdateValue;
  }

  /**
   *  gets the parentDialog
   *
   *  @return   the parent JDialog
   */
  public JDialog getParentDialog() {
    return parentDialog;
  }

  /**
   * Method to set the time after which progress bar is updated
   *
   * @param progressUpdateTime int
   */
  public void setProgressUpdateTime(int progressUpdateTime) {
    this.progressUpdateTime = progressUpdateTime;
  }

  /**
   * Method to set the maximum value of the progress bar
   *
   * @param maximumValue int
   */
  public void setMaximumValue(int maximumValue) {
    this.maximumValue = maximumValue;
  }

  /**
   * Method to set the String shown on the progress bar.
   *
   * @param value String
   */
  public void setProgressBarString(String value) {
    progressBar.setString(value);
  }

  /**
   * Method to set the int value with which progress bar is updated.
   *
   * @param progressUpdateValue int
   */
  public void setProgressUpdateValue(int progressUpdateValue) {
    this.progressUpdateValue = progressUpdateValue;
  }

  /**
   * Method to set the parent dialog.
   *
   * @param parentDialog JDialog
   */
  public void setParentDialog(JDialog parentDialog) {
    this.parentDialog = parentDialog;
  }

  /**
   * Sets the <code>javax.swing.Action</code> to be executed on pressing the
   * cancel button. NOTE that the button's 'private' Action (defined
   * elsewhere in this class) will be executed first, and then the custom action
   * will be executed
   *
   * @param a <code>javax.swing.Action</code> to be executed
   */
  public void setCustomCancelAction(Action a) {

    this.customCancelAction = a;
  }

}
