/**
 *  '$RCSfile: ProfileDialog.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-06-07 01:33:33 $'
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

package edu.ucsb.nceas.morpho.framework;

import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * A graphical window for creating a new user profile with user provided
 * information.
 */
public class ProfileDialog extends JDialog
{
  ConfigXML config;
  ClientFramework container = null;

  JLabel helpLabel = new JLabel();
  JPanel screenPanel = null;
  JTextField firstNameField = new JTextField();
  JTextField lastNameField = new JTextField();
  JTextField usernameField = new JTextField();
  JPasswordField passwordField = new JPasswordField();
  JPasswordField passwordField2 = new JPasswordField();
  JButton previousButton = null;
  JButton nextButton = null;
  JButton cancelButton = new JButton();

  /**
   * Construct a dialog and set the framework
   *
   * @param cont the container framework that created this ProfileDialog
   */
  public ProfileDialog(ClientFramework cont) {
    this(cont, true);
  }

  /**
   * Construct the dialog
   */
  public ProfileDialog(ClientFramework cont, boolean modal)
  {
    super((Frame)cont, modal);

    container = cont;

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setTitle("New Profile");
    setSize(650,400);
    getContentPane().setLayout(new BoxLayout(getContentPane(),
                               BoxLayout.X_AXIS));
    // Center the Frame
    Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
    Rectangle frameDim = getBounds();
    setLocation((screenDim.width - frameDim.width) / 2 ,
            (screenDim.height - frameDim.height) /2);
    setVisible(false);

    JPanel helpPanel = new JPanel();
    helpPanel.setLayout(new BoxLayout(helpPanel, BoxLayout.Y_AXIS));
    helpPanel.setBackground(Color.white);
    helpPanel.setMinimumSize(new Dimension(100,400));
    helpPanel.setPreferredSize(new Dimension(100,400));
    helpPanel.setBorder(BorderFactory.createLoweredBevelBorder());
    helpLabel.setText(
              "<html>This is a whole bunch of help text.</html>");
    helpLabel.setForeground(Color.black);
    helpLabel.setFont(new Font("Dialog", Font.BOLD, 12));
    helpLabel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    helpLabel.setVerticalAlignment(SwingConstants.TOP);
    helpLabel.setMinimumSize(new Dimension(100,400));
    helpPanel.add(helpLabel);
    getContentPane().add(helpPanel);

    getContentPane().add(Box.createRigidArea(new Dimension(8,8)));

    JPanel entryPanel = new JPanel();
    entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
    entryPanel.setBackground(Color.red);
    entryPanel.add(Box.createVerticalStrut(8));

    JLabel headLabel = new JLabel();
    headLabel.setText("New Profile");
    ImageIcon head = new ImageIcon(
                         getClass().getResource("smallheader-bg.gif"));
    headLabel.setIcon(head);
    headLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    headLabel.setHorizontalAlignment(SwingConstants.LEFT);
    headLabel.setAlignmentY(Component.LEFT_ALIGNMENT);
    headLabel.setForeground(Color.black);
    headLabel.setFont(new Font("Dialog", Font.BOLD, 14));
    headLabel.setBorder(BorderFactory.createLoweredBevelBorder());
    entryPanel.add(headLabel);
    entryPanel.add(Box.createVerticalStrut(8));

    JLabel firstNameLabel = new JLabel();
    JLabel lastNameLabel = new JLabel();
    JLabel usernameLabel = new JLabel();
    JLabel passwordLabel = new JLabel();
    JLabel passwordLabel2 = new JLabel();
    firstNameLabel.setText("First name: ");
    lastNameLabel.setText("Last name: ");
    usernameLabel.setText("Username: ");
    passwordLabel.setText("Password: ");
    passwordLabel2.setText("Password again: ");
    firstNameLabel.setForeground(Color.black);
    lastNameLabel.setForeground(Color.black);
    usernameLabel.setForeground(Color.black);
    passwordLabel.setForeground(Color.black);
    passwordLabel2.setForeground(Color.black);
    firstNameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    lastNameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    usernameLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    passwordLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    passwordLabel2.setFont(new Font("Dialog", Font.PLAIN, 12));
    firstNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    lastNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    usernameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    passwordLabel2.setHorizontalAlignment(SwingConstants.RIGHT);

    firstNameField.setColumns(15);
    lastNameField.setColumns(15);
    usernameField.setColumns(15);
    passwordField.setColumns(10);
    passwordField2.setColumns(10);

    screenPanel = new JPanel();
    screenPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    screenPanel.setBackground(Color.green);
    GridBagLayout gridbag = new GridBagLayout();
    screenPanel.setLayout(gridbag);
    JLabel[] labels = {firstNameLabel, lastNameLabel, usernameLabel,
                       passwordLabel, passwordLabel2};
    JTextField[] textFields = {firstNameField, lastNameField, usernameField,
                       passwordField, passwordField2};
    addLabelTextRows(labels, textFields, gridbag, screenPanel);

    entryPanel.add(screenPanel);
    entryPanel.add(Box.createVerticalGlue());
    entryPanel.add(Box.createRigidArea(new Dimension(8,8)));

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBackground(Color.yellow);
    buttonPanel.add(Box.createHorizontalGlue());
    cancelButton.setText("Cancel");
    cancelButton.setActionCommand("Cancel");
    cancelButton.setEnabled(true);
    buttonPanel.add(cancelButton);
    buttonPanel.add(Box.createHorizontalStrut(8));
    previousButton = new JButton("Previous", new ImageIcon( getClass().
          getResource("/toolbarButtonGraphics/navigation/Back16.gif")));
    previousButton.setHorizontalTextPosition(SwingConstants.RIGHT);
    buttonPanel.add(previousButton);
    buttonPanel.add(Box.createHorizontalStrut(8));
    nextButton = new JButton("Next", new ImageIcon( getClass().
          getResource("/toolbarButtonGraphics/navigation/Forward16.gif")));
    nextButton.setHorizontalTextPosition(SwingConstants.LEFT);
    nextButton.setEnabled(true);
    buttonPanel.add(nextButton);
    buttonPanel.add(Box.createHorizontalStrut(8));

    entryPanel.add(buttonPanel);
    entryPanel.add(Box.createVerticalStrut(8));
    getContentPane().add(entryPanel);
    getContentPane().add(Box.createHorizontalGlue());
    getContentPane().add(Box.createHorizontalStrut(8));

    // register a listener for ActionEvents
    ActionHandler myActionHandler = new ActionHandler();
    previousButton.addActionListener(myActionHandler);
    nextButton.addActionListener(myActionHandler);
    cancelButton.addActionListener(myActionHandler);

    config = new ConfigXML("lib/config.xml");
    
    if (container!=null) {
      usernameField.setText(container.getUserName());
    }
  }

  /**
   * Listener used to detect button presses
   */
  private class ActionHandler implements ActionListener
  {
    public void actionPerformed(ActionEvent event)
    {
      Object object = event.getSource();
      if (object == previousButton) {
        previosuButtonHandler(event);
      } else if (object == nextButton) {
        nextButtonHandler(event);
      } else if (object == cancelButton) {
        cancelButtonHandler(event);
      }
    }
  }

  /**
   * Perform actions associated with the Previous button
   */
  private void previosuButtonHandler(ActionEvent event)
  {
    screenPanel.validate();
    screenPanel.paint(screenPanel.getGraphics());
  }
  
  /**
   * Perform actions associated with the Next button
   */
  private void nextButtonHandler(ActionEvent event)
  {
  }

  /**
   * Perform actions associated with the Disconnect button
   */
  private void cancelButtonHandler(ActionEvent event)
  {
    dispose();
  }

  /**
   * Lay out labels and fields in an orderly grid.
   */
  private void addLabelTextRows(JLabel[] labels,
                                JTextField[] textFields,
                                GridBagLayout gridbag,
                                Container container) {
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.EAST;
    int numLabels = labels.length;

    for (int i = 0; i < numLabels; i++) {
      c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
      c.fill = GridBagConstraints.NONE;      //reset to default
      c.weightx = 0.0;                       //reset to default
      c.anchor = GridBagConstraints.EAST;
      gridbag.setConstraints(labels[i], c);
      container.add(labels[i]);

      c.gridwidth = GridBagConstraints.REMAINDER;     //end row
      //c.fill = GridBagConstraints.HORIZONTAL; // fill remaining space
      c.weightx = 1.0;
      c.anchor = GridBagConstraints.WEST;
      gridbag.setConstraints(textFields[i], c);
      container.add(textFields[i]);
    }
  }
}
