/**
 *  '$RCSfile: DataPackageViewer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-08-09 15:11:07 $'
 * '$Revision: 1.1.2.7 $'
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.symantec.itools.javax.swing.JToolBarSeparator;
import com.symantec.itools.javax.swing.icons.ImageIcon;
import java.io.*;
import java.util.*;

import edu.ucsb.nceas.morpho.framework.*;
/**
 * A window that presents a data-centric view of a dataPackage
 */
public class DataPackageViewer extends javax.swing.JFrame implements javax.swing.event.ChangeListener
{
  
  // toppanel is added to packageMetadataPanel by init
  public JPanel toppanel;
  
  
  DataViewContainerPanel dataViewContainerPanel ;
  
  ClientFramework framework;
  ConfigXML config;

  public DataPackageViewer() {
    setTitle("Data Package Viewer");
    setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout(0,0));
    setSize(800,600);
    setVisible(false);
    dataViewContainerPanel = new DataViewContainerPanel();
    getContentPane().add(BorderLayout.CENTER, dataViewContainerPanel);

    //{{REGISTER_LISTENERS
    SymWindow aSymWindow = new SymWindow();
    this.addWindowListener(aSymWindow);
    SymAction lSymAction = new SymAction();
  }

  /**
   * Creates a new instance of DataPackageViewer with the given title.
   * @param sTitle the title for the new frame.
   * @see #DataPackageViewer()
   */
  public DataPackageViewer(String sTitle) {
   this();
   setTitle(sTitle);
  }
  
  /**
   * Create and new instance and set the DataPackage
   */
  public DataPackageViewer(String sTitle, DataPackage dp) {
    this();
    System.out.println("DataPackageViewer creation-1 ");
    setTitle(sTitle);
    dataViewContainerPanel.dp = dp;
  }
  
  /**
   *Create and new instance and set the DataPackage
   */
  public DataPackageViewer(String sTitle, DataPackage dp, DataPackageGUI dpgui) {
    this();
    setTitle(sTitle);
    this.setVisible(true);
  }
  
  public DataViewContainerPanel getDataViewContainerPanel() {
    return dataViewContainerPanel; 
  }
	
  /**
   * The entry point for this application.
   * Sets the Look and Feel to the System Look and Feel.
   * Creates a new DataPackageViewer and makes it visible.
   */
  static public void main(String args[]) {
    try {
      // Add the following code if you want the Look and Feel
      // to be set to the Look and Feel of the native system.
		    
      try {
//	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } 
      catch (Exception e) { 
      }
		    

    //Create a new instance of our application's frame, and make it visible.
    (new DataPackageViewer()).setVisible(true);
    } 
    catch (Throwable t) {
      t.printStackTrace();
    //Ensure the application exits with an error condition.
      System.exit(1);
    }
  }

   

  void exitApplication() {
    this.setVisible(false);    // hide the Frame
    dataViewContainerPanel.removePVObject();
    this.dispose();            // free the system resources
			
  }

  class SymWindow extends java.awt.event.WindowAdapter
  {
    public void windowClosing(java.awt.event.WindowEvent event) {
      Object object = event.getSource();
      if (object == DataPackageViewer.this)
        DataPackageViewer_windowClosing(event);
    }
  }

  void DataPackageViewer_windowClosing(java.awt.event.WindowEvent event) {
    DataPackageViewer_windowClosing_Interaction1(event);
  }

  void DataPackageViewer_windowClosing_Interaction1(java.awt.event.WindowEvent event) {
    try {
      this.exitApplication();
    } catch (Exception e) {
    }
  }

  class SymAction implements java.awt.event.ActionListener 
  {
     public void actionPerformed(java.awt.event.ActionEvent event) {
       Object object = event.getSource();
     }
  }
  
  
  public void setFramework(ClientFramework cf) {
    this.framework = cf;
  }
  

  public void stateChanged(javax.swing.event.ChangeEvent event) {
    Object object = event.getSource();
  }

  
}
