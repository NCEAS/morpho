/**
 *  '$RCSfile: DataViewContainerPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-08-21 22:20:11 $'
 * '$Revision: 1.2 $'
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
import java.io.*;
import java.util.*;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.morpho.framework.*;
/**
 * A window that presents a data-centric view of a dataPackage
 */
public class DataViewContainerPanel extends javax.swing.JPanel implements javax.swing.event.ChangeListener
{
  /**
   * The DataPackage that contains the data
  */
  DataPackage dp;
  
  // toppanel is added to packageMetadataPanel by init
  public JPanel toppanel;
  
  PersistentVector lastPV = null;
  
  String referenceLabelText = "referenceLabel";
  
  //tabbedEntitiesPanel is the tabbed panel which contains the entity list (as tabs) + dataView + entityMetadata
  JTabbedPane tabbedEntitiesPanel;
  
  //dataViewPanel is the base panel for the data display
  JPanel dataViewPanel;
 
 //Panel where package level metadata is displayed
  JPanel packageMetadataPanel;

  String entityId;
  Morpho framework;
  ConfigXML config;
  File entityFile;
  
  JSplitPane entityPanel;
  JPanel entityMetadataPanel;
  JPanel currentDataPanel;
  
  Vector entityItems;
  JFrame dpv;
  
  int lastTabSelected = 0;
  
  Hashtable listValueHash = new Hashtable();

  public DataViewContainerPanel()
  {
    this.setLayout(new BorderLayout(0,0));
    packageMetadataPanel = new JPanel();
    packageMetadataPanel.setLayout(new BorderLayout(0,0));
    packageMetadataPanel.setMinimumSize(new Dimension(34,34));
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
    
    JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,packageMetadataPanel,tabbedEntitiesPanel);
    vertSplit.setOneTouchExpandable(true);
    this.add(BorderLayout.CENTER,vertSplit);
    vertSplit.setDividerLocation(34);
    this.setVisible(true);
 
  }
  
  public DataViewContainerPanel(DataPackage dp, DataPackageGUI dpgui)
  {
    this();
    this.dp = dp;
    JPanel packagePanel = new JPanel();
    packagePanel.setLayout(new BorderLayout(0,0));
    JLabel refLabel = new JLabel("<html>XXX"+dpgui.referenceLabel+"</html>");
    refLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    packagePanel.add(BorderLayout.CENTER,dpgui.basicInfoPanel);
    packagePanel.add(BorderLayout.EAST,dpgui.listPanel);
    packagePanel.add(BorderLayout.NORTH,refLabel);
    this.framework = dpgui.morpho;
    this.toppanel = packagePanel;
    this.entityItems = dpgui.entityitems;
    
    this.listValueHash = dpgui.listValueHash;
    this.setVisible(true);
  }
 
  
  public void init() {
    // first get info about the data package from DataPackageGUI
    if (toppanel==null) {
      System.out.println("toppanel is null");
    }
    else {
      packageMetadataPanel.removeAll();
      packageMetadataPanel.add(BorderLayout.CENTER,toppanel);
    }
    if (entityItems==null) System.out.println("EntityItems vector is null!!!");
    for (int i=0;i<entityItems.size();i++) {
      JSplitPane currentEntityPanel = createEntityPanel();
      tabbedEntitiesPanel.addTab((String)entityItems.elementAt(i), currentEntityPanel);
      String item = (String)entityItems.elementAt(i);
      String id = (String)listValueHash.get(item);
      String location = dp.getLocation();
      EntityGUI entityEdit = new EntityGUI(dp, id, location, null, framework);
      entityEdit.dpv = this.dpv;
       
      JPanel currentEntityMetadataPanel = (JPanel)currentEntityPanel.getRightComponent();
      
      JPanel entityInfoPanel = new JPanel();
      entityInfoPanel.setLayout(new BorderLayout(0,0));
      entityInfoPanel.add(BorderLayout.CENTER, entityEdit.entityPanel);
      entityInfoPanel.add(BorderLayout.NORTH, new JLabel("Entity Information"));
      JPanel entityEditControls = new JPanel();
      entityEditControls.add(entityEdit.editEntityButton);
      
      
      currentEntityMetadataPanel.add(BorderLayout.CENTER, entityInfoPanel);                                     
      currentEntityMetadataPanel.add(BorderLayout.SOUTH, entityEditControls);                                     
      currentEntityMetadataPanel.setMaximumSize(new Dimension(200,4000));

      currentEntityPanel.setDividerLocation(600);
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
  
  public void setFramework(Morpho cf) {
    this.framework = cf;
  }
  public void setTopPanel(JPanel jp) {
    this.toppanel = jp;
    this.toppanel.setVisible(true);
  }
  public void setEntityItems(Vector ei) {
    this.entityItems = ei;
  }
  public void setListValueHash(Hashtable ht) {
    this.listValueHash = ht;
  }

  public void removePVObject() {
    if (lastPV!=null) {
      lastPV.delete();  
    }
  }
    /**
   * creates the data display and puts it into the center of the window
   * This needs to be dynamically done as tabs are selected due to large memory usage
   */
  private void setDataViewer(int index) {
    JSplitPane entireDataPanel = (JSplitPane)(tabbedEntitiesPanel.getComponentAt(lastTabSelected));
    JPanel currentDataPanel1 = (JPanel)entireDataPanel.getLeftComponent();
    removePVObject();
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
    lastPV = dv.getPV();
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

  
  public void stateChanged(javax.swing.event.ChangeEvent event) {
    Object object = event.getSource();
    if (object == tabbedEntitiesPanel) {
      int k = tabbedEntitiesPanel.getSelectedIndex();
      setDataViewer(k);
    }
  }


}
