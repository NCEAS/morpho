/**
 *  '$RCSfile: DataPackageGUI.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: berkley $'
 *     '$Date: 2001-05-16 22:48:43 $'
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

package edu.ucsb.nceas.morpho.datapackage;

import edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;

public class DataPackageGUI extends javax.swing.JFrame 
                                  //implements ActionListener, ItemListener
{
  private ClientFramework framework;
  
  public DataPackageGUI(ClientFramework framework)
  {
    this.framework = framework;
    setTitle("Data Package Editor");
    initComponents();
    pack();
    setSize(500, 500);
  }
  
  /**
   * Creates the panels and hands off tasks to other methods
   */
  private void initComponents()
  {
    Container contentPane = getContentPane();
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);  
    contentPane.setLayout(new FlowLayout());
    
    JPanel mainPanel = new JPanel();
    mainPanel.setMaximumSize(new Dimension(500,500));
    PackageWizard pw = new PackageWizard(framework, mainPanel, 
                                         "Resource Information");
    //JTextField nameField = new JTextField();
    //JLabel nameLabel = new JLabel("name");
    //nameField.setColumns(20);
    //contentPane.add(nameLabel);
    //contentPane.add(nameField);
    
    contentPane.add(mainPanel);
    //createMenu(contentPane);
    //createPanel(doc, contentPane, docPanel);
  }
  
  public static void main(String[] args)
  {
    ConfigXML conf = new ConfigXML("./lib/config.xml");
    ClientFramework cf = new ClientFramework(conf);
    new DataPackageGUI(cf).show();
  }
}
