/**
 *  '$RCSfile: DataPackageViewer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-07-03 23:04:44 $'
 * '$Revision: 1.1.2.2 $'
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
 * A basic JFC 1.1 based application.
 */
public class DataPackageViewer extends javax.swing.JFrame
{
  
  /**
	 * The DataPackage that contains the data
	 */
	DataPackage dp;
  
  public JPanel toppanel;
  
  String entityId;
  ClientFramework framework;
  ConfigXML config;
  File entityFile;
  
  JPanel DataDisplayPanel;
  JPanel DataPackageInfoPanel;
  JTabbedPane TabbedEntitiesPanel;
  JSplitPane EntityPanel;
  JPanel EntityMetadataPanel;
  
  Vector entityItems;
  public Hashtable listValueHash = new Hashtable();

	public DataPackageViewer()
	{
		setTitle("DataPackageViewer");
		setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(800,600);
		setVisible(false);
		MainPanel.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER, MainPanel);

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();

		DataPackageInfoPanel = new JPanel();
    DataPackageInfoPanel.setLayout(new BorderLayout(0,0));
		EntityMetadataPanel = new JPanel();
    EntityMetadataPanel.setLayout(new BorderLayout(0,0));
		DataDisplayPanel = new JPanel();
    DataDisplayPanel.setLayout(new BorderLayout(0,0));
		JPanel AttributeMetadataPanel = new JPanel();
		
		JSplitPane TableDataPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,DataDisplayPanel,AttributeMetadataPanel);
		TableDataPanel.setOneTouchExpandable(true);
    
		EntityPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,EntityMetadataPanel,TableDataPanel);
		EntityPanel.setOneTouchExpandable(true);
    
    TabbedEntitiesPanel = new JTabbedPane(SwingConstants.BOTTOM);
    //TabbedEntitiesPanel.addTab("First Entity", EntityPanel);
    
		JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,DataPackageInfoPanel,TabbedEntitiesPanel);
		vertSplit.setOneTouchExpandable(true);
		MainPanel.add(BorderLayout.CENTER,vertSplit);
		vertSplit.setDividerLocation(200);
		TableDataPanel.setDividerLocation(5000);
		EntityPanel.setDividerLocation(0);
    
	}

  /**
   * Creates a new instance of DataPackageViewer with the given title.
   * @param sTitle the title for the new frame.
   * @see #DataPackageViewer()
   */
	public DataPackageViewer(String sTitle)
	{
		this();
		setTitle(sTitle);
	}
  
  /**
   * Create and new instance and set the DataPackage
   */
  public DataPackageViewer(String sTitle, DataPackage dp) {
    this();
    setTitle(sTitle);
    this.dp = dp;
  }
  
  /**
   *Create and new instance and set the DataPackage
   */
  public DataPackageViewer(String sTitle, DataPackage dp, DataPackageGUI dpgui) {
    this();
    setTitle(sTitle);
    this.dp = dp;
    
    JPanel packagePanel = new JPanel();
    packagePanel.setLayout(new BorderLayout(0,0));
    packagePanel.add(BorderLayout.CENTER,dpgui.basicInfoPanel);
    packagePanel.add(BorderLayout.EAST,dpgui.listPanel);
    this.framework = dpgui.framework;
    this.toppanel = packagePanel;
    this.entityItems = dpgui.entityitems;
    
    this.listValueHash = dpgui.listValueHash;
    this.init();
    this.setVisible(true);

  }
	
	/**
	 * The entry point for this application.
	 * Sets the Look and Feel to the System Look and Feel.
	 * Creates a new DataPackageViewer and makes it visible.
	 */
	static public void main(String args[])
	{
		try {
		    // Add the following code if you want the Look and Feel
		    // to be set to the Look and Feel of the native system.
		    
		    try {
//		        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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

    /**
     * Notifies this component that it has been added to a container
     * This method should be called by <code>Container.add</code>, and 
     * not by user code directly.
     * Overridden here to adjust the size of the frame if needed.
     * @see java.awt.Container#removeNotify
     */
	public void addNotify()
	{
		// Record the size of the window prior to calling parents addNotify.
		Dimension size = getSize();
		
		super.addNotify();
		
		if (frameSizeAdjusted)
			return;
		frameSizeAdjusted = true;
		
		// Adjust size of frame according to the insets and menu bar
		javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
		int menuBarHeight = 0;
		if (menuBar != null)
		    menuBarHeight = menuBar.getPreferredSize().height;
		Insets insets = getInsets();
		setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
	}

	// Used by addNotify
	boolean frameSizeAdjusted = false;

	javax.swing.JPanel MainPanel = new javax.swing.JPanel();


	void exitApplication()
	{
		
		    	this.setVisible(false);    // hide the Frame
		    	this.dispose();            // free the system resources
			
		}

	class SymWindow extends java.awt.event.WindowAdapter
	{
		public void windowClosing(java.awt.event.WindowEvent event)
		{
			Object object = event.getSource();
			if (object == DataPackageViewer.this)
				DataPackageViewer_windowClosing(event);
		}
	}

	void DataPackageViewer_windowClosing(java.awt.event.WindowEvent event)
	{
		// to do: code goes here.
			 
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
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			
			
		}
	}
  
  public void init() {
    // first get info about the data package from DataPackageGUI
    if (toppanel==null) {
      System.out.println("toppanel is null");
    }
    else {
      DataPackageInfoPanel.add(BorderLayout.CENTER,toppanel);
    }
    
 //   String firstEntityName = (String)entityItems.elementAt(0);
 //   TabbedEntitiesPanel.setTitleAt(0, firstEntityName);
    for (int i=0;i<entityItems.size();i++) {
      JSplitPane currentEntityPanel = createEntityPanel();
      TabbedEntitiesPanel.addTab((String)entityItems.elementAt(i), currentEntityPanel);
      String item = (String)entityItems.elementAt(i);
      String id = (String)listValueHash.get(item);
      String location = dp.getLocation();
      EntityGUI entityEdit = new EntityGUI(dp, id, location, null, 
                                           framework);
 //     entityEdit.show();
      
      JPanel currentEntityMetadataPanel = (JPanel)currentEntityPanel.getLeftComponent();
      
      JPanel entityInfoPanel = new JPanel();
      entityInfoPanel.setLayout(new BorderLayout(0,0));
      entityInfoPanel.add(BorderLayout.CENTER, entityEdit.entityPanel);
      entityInfoPanel.add(BorderLayout.NORTH, new JLabel("Entity Information"));
      JPanel entityEditControls = new JPanel();
      entityEditControls.add(entityEdit.editEntityButton);
      
      
      currentEntityMetadataPanel.add(BorderLayout.CENTER, entityInfoPanel);                                     
      currentEntityMetadataPanel.add(BorderLayout.SOUTH, entityEditControls);                                     

   //   currentEntityMetadataPanel.add(BorderLayout.CENTER, entityEdit.entityPanel);
//    }
/*    
    String item = "";
    item = TabbedEntitiesPanel.getTitleAt(TabbedEntitiesPanel.getSelectedIndex());
    String id = (String)listValueHash.get(item);
    
    String location = dp.getLocation();
    EntityGUI entityEdit = new EntityGUI(dp, id, location, null, 
                                           framework);
    entityEdit.show();
    EntityMetadataPanel.add(BorderLayout.CENTER, entityEdit.entityPanel);
 */
    EntityPanel.setDividerLocation(200);
    this.entityFile = entityEdit.entityFile;
    
    // create the data display panel (usually a table) using DataViewer class
    String fn = dp.getDataFileName(id);    
    File fphysical = dp.getPhysicalFile(id);
//      System.out.println("eml-physical: "+fphysical.getName());
    File fattribute = dp.getAttributeFile(id);
//      System.out.println("eml-attribute: "+fattribute.getName());
    File f = dp.getDataFile(id);
    String dataString = "";
    DataViewer dv = new DataViewer(framework, "DataFile: "+fn, f);
    dv.setDataID(dp.getDataFileID(id));
    dv.setPhysicalFile(fphysical);
    dv.setAttributeFile(fattribute);
    dv.setEntityFile(entityFile);
    dv.setDataPackage(this.dp);
    dv.init();
    dv.parseFile();
    JPanel tablePanel = dv.DataViewerPanel;
    
    JSplitPane TableDataPanel = (JSplitPane)((currentEntityPanel).getRightComponent());
    JPanel currentDataPanel = (JPanel)TableDataPanel.getLeftComponent();
    currentDataPanel.setLayout(new BorderLayout(0,0));
    currentDataPanel.add(BorderLayout.CENTER,tablePanel);
    }
  }
  
  public void setFramework(ClientFramework cf) {
    this.framework = cf;
  }
  
  private JSplitPane createEntityPanel() {
    DataPackageInfoPanel = new JPanel();
    DataPackageInfoPanel.setLayout(new BorderLayout(0,0));
    JPanel EntityMetadataPanel = new JPanel();
    EntityMetadataPanel.setLayout(new BorderLayout(0,0));
	  DataDisplayPanel = new JPanel();
    DataDisplayPanel.setLayout(new BorderLayout(0,0));
    JPanel AttributeMetadataPanel = new JPanel();
		
    JSplitPane TableDataPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,DataDisplayPanel,AttributeMetadataPanel);
	  TableDataPanel.setOneTouchExpandable(true);
    TableDataPanel.setDividerLocation(5000);
    
	  EntityPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,EntityMetadataPanel,TableDataPanel);
	  EntityPanel.setOneTouchExpandable(true);
    EntityPanel.setDividerLocation(200);
    
  return EntityPanel;
  }
  
}
