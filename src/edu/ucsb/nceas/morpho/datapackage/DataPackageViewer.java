/**
 *  '$RCSfile: DataPackageViewer.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-08-01 22:01:35 $'
 * '$Revision: 1.1.2.5 $'
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
  
  /**
	 * The DataPackage that contains the data
	 */
	DataPackage dp;
  
  // toppanel is added to packageMetadataPanel by init
  public JPanel toppanel;
  
  
  String referenceLabelText = "referenceLabel";
  //DataViewContainer is the top level container
  JPanel dataViewContainerPanel = new javax.swing.JPanel();
  
  //tabbedEntitiesPanel is the tabbed panel which contains the entity list (as tabs) + dataView + entityMetadata
  JTabbedPane tabbedEntitiesPanel;
  
  //dataViewPanel is the base panel for the data display
  JPanel dataViewPanel;
 
 //Panel where package level metadata is displayed
  JPanel packageMetadataPanel;

  String entityId;
  ClientFramework framework;
  ConfigXML config;
  File entityFile;
  
  JSplitPane entityPanel;
  JPanel entityMetadataPanel;
  JPanel currentDataPanel;
  
  Vector entityItems;
  
  int lastTabSelected = 0;
  
  public Hashtable listValueHash = new Hashtable();

	public DataPackageViewer()
	{
		setTitle("Data Package Viewer");
		setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0,0));
		setSize(800,600);
		setVisible(false);
		dataViewContainerPanel.setLayout(new BorderLayout(0,0));
		getContentPane().add(BorderLayout.CENTER, dataViewContainerPanel);

		//{{REGISTER_LISTENERS
		SymWindow aSymWindow = new SymWindow();
		this.addWindowListener(aSymWindow);
		SymAction lSymAction = new SymAction();

		packageMetadataPanel = new JPanel();
    packageMetadataPanel.setLayout(new BorderLayout(0,0));
		entityMetadataPanel = new JPanel();
    entityMetadataPanel.setLayout(new BorderLayout(0,0));
		dataViewPanel = new JPanel();
    dataViewPanel.setLayout(new BorderLayout(0,0));
		JPanel AttributeMetadataPanel = new JPanel();
		
		JSplitPane TableDataPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,dataViewPanel,AttributeMetadataPanel);
		TableDataPanel.setOneTouchExpandable(true);
    
		entityPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,entityMetadataPanel,TableDataPanel);
		entityPanel.setOneTouchExpandable(true);
    
    tabbedEntitiesPanel = new JTabbedPane(SwingConstants.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedEntitiesPanel.addChangeListener(this);
    //tabbedEntitiesPanel.addTab("First Entity", entityPanel);
    
		JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,packageMetadataPanel,tabbedEntitiesPanel);
		vertSplit.setOneTouchExpandable(true);
		dataViewContainerPanel.add(BorderLayout.CENTER,vertSplit);
		vertSplit.setDividerLocation(100);
    
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
      packageMetadataPanel.add(BorderLayout.CENTER,toppanel);
    }
    
    for (int i=0;i<entityItems.size();i++) {
      JSplitPane currentEntityPanel = createEntityPanel();
      tabbedEntitiesPanel.addTab((String)entityItems.elementAt(i), currentEntityPanel);
      String item = (String)entityItems.elementAt(i);
      String id = (String)listValueHash.get(item);
      String location = dp.getLocation();
      EntityGUI entityEdit = new EntityGUI(dp, id, location, null, 
                                           framework);
      entityEdit.dpv = this;
       
      JPanel currentEntityMetadataPanel = (JPanel)currentEntityPanel.getRightComponent();
      
      JPanel entityInfoPanel = new JPanel();
      entityInfoPanel.setLayout(new BorderLayout(0,0));
      entityInfoPanel.add(BorderLayout.CENTER, entityEdit.entityPanel);
      entityInfoPanel.add(BorderLayout.NORTH, new JLabel("Entity Information"));
      JPanel entityEditControls = new JPanel();
      entityEditControls.add(entityEdit.editEntityButton);
      
      
      currentEntityMetadataPanel.add(BorderLayout.CENTER, entityInfoPanel);                                     
      currentEntityMetadataPanel.add(BorderLayout.SOUTH, entityEditControls);                                     


      entityPanel.setDividerLocation(600);
      this.entityFile = entityEdit.entityFile;
    
      // create the data display panel (usually a table) using DataViewer class
      String fn = dp.getDataFileName(id);    
      File fphysical = dp.getPhysicalFile(id);
      File fattribute = dp.getAttributeFile(id);
      File f = dp.getDataFile(id);
      String dataString = "";
    }
    if ((entityItems!=null) && (entityItems.size()>0)) {
      setDataViewer(0);
    }
  }
  
  public void setFramework(ClientFramework cf) {
    this.framework = cf;
  }
  
  /**
   * creates the data display and puts it into the center of the window
   * This needs to be dynamically done as tabs are selected due to large memory usage
   */
  private void setDataViewer(int index) {
    JSplitPane entireDataPanel = (JSplitPane)(tabbedEntitiesPanel.getComponentAt(lastTabSelected));
    JPanel currentDataPanel1 = (JPanel)entireDataPanel.getLeftComponent();
    currentDataPanel1.removeAll();
    lastTabSelected = index;
    String item = (String)entityItems.elementAt(index);
    String id = (String)listValueHash.get(item);
    String fn = dp.getDataFileName(id);    
    File fphysical = dp.getPhysicalFile(id);
    File fattribute = dp.getAttributeFile(id);
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
    
    JSplitPane EntireDataPanel = (JSplitPane)(tabbedEntitiesPanel.getComponentAt(index));
    JPanel currentDataPanel = (JPanel)EntireDataPanel.getLeftComponent();
    currentDataPanel.setLayout(new BorderLayout(0,0));
    currentDataPanel.add(BorderLayout.CENTER,tablePanel);
   
  }
  
  private JSplitPane createEntityPanel() {
    JPanel entityMetadataPanel = new JPanel();
    entityMetadataPanel.setLayout(new BorderLayout(0,0));
	  dataViewPanel = new JPanel();
    dataViewPanel.setLayout(new BorderLayout(0,0));
    
	  entityPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,dataViewPanel, entityMetadataPanel);
	  entityPanel.setOneTouchExpandable(true);
    entityPanel.setDividerLocation(600);
    
  return entityPanel;
  }

	public void stateChanged(javax.swing.event.ChangeEvent event)
		{
			Object object = event.getSource();
			if (object == tabbedEntitiesPanel) {
				int k = tabbedEntitiesPanel.getSelectedIndex();
        setDataViewer(k);
		  }
	}

  
}
