/**
 *  '$RCSfile: DataViewContainerPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2002-09-06 22:29:03 $'
 * '$Revision: 1.14 $'
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
import javax.swing.border.*;
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
import edu.ucsb.nceas.morpho.plugins.metadisplay.*;

import edu.ucsb.nceas.morpho.framework.*;

/**
 * A panel that presents a data-centric view of a dataPackage. In fact, the panel is somewhat
 * complicated, with numerous subpanels and components
 * The panel is made up of several JSplitPanes. The Top of the first split pane shows datapackage
 * level metadata. A summary can be seen at the top showing a summary of the datapackage in
 * a reference like format, followed by more package level metadata details. The bottom of this
 * splitPane contains a tabbed pane which has a tab for each entity in the package.
 * For each tab, another splitPane appears with a data display taking up most of the room on
 * the left and a display of entity metadata on the right. Initially, most of the screen
 * space is alloted to the data display, but the dividers can be dragged by the user to
 * customize the display
 */
public class DataViewContainerPanel extends javax.swing.JPanel 
                                    implements javax.swing.event.ChangeListener
{
  /**
   * The DataPackage that contains the data
   */
  DataPackage dp;
  
  /**
   * toppanel is added to packageMetadataPanel by init
   */
  JPanel toppanel;
  
  /**
   * tabbedEntitiesPanel is the tabbed panel which contains
   * the entity list (as tabs) + dataView + entityMetadata
   */
   JTabbedPane tabbedEntitiesPanel;
  
  /**
   * dataViewPanel is the base panel for the data display
   */
  JPanel dataViewPanel;
 
  /**
   * Panel where package level metadata is displayed
   */
  JPanel packageMetadataPanel;

  /**
   * reference to the top level Morpho class
   */
  Morpho morpho;
  
  /**
   * the configuration class (of course)
   */
  ConfigXML config;
  
  /**
   * Array of Entity File objects corresponding to each of the tabs
   */
  File[] entityFile;
  
  /**
   * global to keep track of last tab selected
   */
  int lastTabSelected = 0;
  
  Hashtable listValueHash = new Hashtable();
  JSplitPane entityPanel;
  JPanel entityMetadataPanel;
  JPanel currentDataPanel;
  JSplitPane vertSplit;
  
  Vector entityItems;
  PersistentVector lastPV = null;

  
  /*
   * no parameter constuctor for DataViewContainerPanel.
   * Some basic gui setup
   */
  public DataViewContainerPanel()
  {
    this.setLayout(new BorderLayout(0,0));
    this.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
    packageMetadataPanel = new JPanel();
    packageMetadataPanel.setLayout(new BorderLayout(0,0));
    packageMetadataPanel.setMinimumSize(new Dimension(34,34));
    entityMetadataPanel = new JPanel();
    entityMetadataPanel.setLayout(new BorderLayout(0,0));
    dataViewPanel = new JPanel();
    dataViewPanel.setLayout(new BorderLayout(0,0));
    JPanel AttributeMetadataPanel = new JPanel();
		
  
//ScrollTabLayout only works for Java 1.4; commented out for now so will compile unbder 1.3
// tabbedEntitiesPanel = new JTabbedPane(SwingConstants.BOTTOM, JTabbedPane.SCROLL_TAB_LAYOUT);

    tabbedEntitiesPanel = new JTabbedPane(SwingConstants.BOTTOM);
    tabbedEntitiesPanel.addChangeListener(this);
    
    vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,packageMetadataPanel,tabbedEntitiesPanel);
    vertSplit.setOneTouchExpandable(true);
    this.add(BorderLayout.CENTER,vertSplit);
    vertSplit.setDividerLocation(52);
    this.setVisible(true);
 
  }
  
  /*
   * this constructor uses DataPackage and DataPackageGUI objects
   * to build the interior object in the Panel
   * (DataPackageGui is used because it already exist. The parts of it
   * that are used will probably be moved and the rest deleted since
   * many of its methods are now not needed)
   */
  public DataViewContainerPanel(DataPackage dp, DataPackageGUI dpgui)
  {
    this();
    this.dp = dp;
    JPanel packagePanel = new JPanel();
    packagePanel.setLayout(new BorderLayout(5,5));

// the following code builds the datapackage summary at the top of
// the DataViewContainerPanel
    JLabel refLabel = new JLabel("<html>"+dpgui.referenceLabel+"</html>");
    refLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    JPanel refPanel = new JPanel();
    refPanel.setPreferredSize(new Dimension(5000,50));
    Border margin = BorderFactory.createEmptyBorder(2,2,2,2);
    Border lineBorder = BorderFactory.createLineBorder(Color.black);
    refPanel.setBorder(new CompoundBorder(margin, lineBorder));
    refPanel.setLayout(new BorderLayout(5,5));
    refPanel.add(BorderLayout.CENTER, refLabel);
    refLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    JPanel locationPanel = new JPanel();
    locationPanel.setLayout(new BorderLayout(0,0));
    Border lineBorder1 = BorderFactory.createLineBorder(Color.black);
    Border margin1 = BorderFactory.createEmptyBorder(5,5,5,5);
    Border inner = new CompoundBorder(lineBorder1,margin1);
    locationPanel.setBorder(new CompoundBorder(margin,inner));
    ImageIcon localIcon 
      = new ImageIcon(getClass().getResource("local-package-small.png"));
    ImageIcon metacatIcon 
      = new ImageIcon(getClass().getResource("network-package-small.png"));
    ImageIcon blankIcon 
      = new ImageIcon(getClass().getResource("blank.gif"));
    JLabel localLabel = new JLabel("local");
    localLabel.setIcon(localIcon);
    localLabel.setToolTipText("Package is stored locally");
    JLabel netLabel = new JLabel("net");
    netLabel.setIcon(metacatIcon);
    netLabel.setToolTipText("Package is stored on the network");
    String location = dp.getLocation();
    if (location.equals(DataPackageInterface.METACAT)) {
      localLabel.setText("");
      localLabel.setIcon(blankIcon);
    }
    else if (location.equals(DataPackageInterface.LOCAL)) {
      netLabel.setText("");
      netLabel.setIcon(blankIcon);
    }
    else {   // both
      
    }
    locationPanel.add(BorderLayout.NORTH,localLabel);
    locationPanel.add(BorderLayout.SOUTH, netLabel);
    refPanel.add(BorderLayout.EAST, locationPanel);
// ------------------------------------
// this is where the datapackage metadata is inserted into the container !!!!! 
// simply add Component to the packagePanel container, which has a Border
// layout. leave the 'NORTH' region empty, since the
// refpanel is added there later
  
// -----------------------Test of MetaDisplay-------------------------
    MetaDisplay md = new MetaDisplay();
    Component mdcomponent = null;
    try{
      mdcomponent = md.getDisplayComponent(dp.getID(), dp, null);
    }
    catch (Exception m) {
      Log.debug(20, "Unable to get metadisplay component"); 
    }
    packagePanel.add(BorderLayout.CENTER, mdcomponent);
// ------------------==End Test of MetaDisplay-------------------------
    
// the  following 2 lines add the Morpho 1.1.2 metadata displays when uncommented 
//    packagePanel.add(BorderLayout.CENTER,dpgui.basicInfoPanel);
//    packagePanel.add(BorderLayout.EAST,dpgui.listPanel);
// ------------------------------------
    
// refpanel is created in this class and added to the top of the 
// panel in the next statement  
    packagePanel.add(BorderLayout.NORTH,refPanel);
    this.morpho = dpgui.morpho;
    this.toppanel = packagePanel;
    this.entityItems = dpgui.entityitems;
    
    this.listValueHash = dpgui.listValueHash;
    this.setVisible(true);
   
// trying to get the height here always gives zero
//  vertSplit.setDividerLocation(refPanel.getHeight());

  }
 
  /*
   * Initialization continues here. In particular, the tabbed panels
   * for each of the entities are created and added to the tab panel;
   * Entity metadata is created and added, but adding the data table
   * (or image) display is defered until runtime due to potentially
   * large memory requirements
   */
  public void init() {
    // first get info about the data package from DataPackageGUI
    if (toppanel==null) {
      System.out.println("toppanel is null");
    }
    else {
      packageMetadataPanel.removeAll();
      packageMetadataPanel.add(BorderLayout.CENTER,toppanel);
    }
    if (entityItems==null) Log.debug(20, "EntityItems vector is null!!!");
    entityFile = new File[entityItems.size()];
    for (int i=0;i<entityItems.size();i++) {
      JSplitPane currentEntityPanel = createEntityPanel();
      tabbedEntitiesPanel.addTab((String)entityItems.elementAt(i), currentEntityPanel);
      String item = (String)entityItems.elementAt(i);
      // id is the id of the Entity metadata module
      // code from here to 'end_setup' comment sets up the display for the
      // entity metadata
      String id = (String)listValueHash.get(item);
      String location = dp.getLocation();
      EntityGUI entityEdit = new EntityGUI(dp, id, location, null, morpho);
       
      JPanel currentEntityMetadataPanel = (JPanel)currentEntityPanel.getRightComponent();
      
      JPanel entityInfoPanel = new JPanel();
      entityInfoPanel.setLayout(new BorderLayout(0,0));
      entityInfoPanel.add(BorderLayout.CENTER, entityEdit.entityPanel);
      entityInfoPanel.add(BorderLayout.NORTH, new JLabel("Entity Information"));
      JPanel entityEditControls = new JPanel();
      entityEditControls.add(entityEdit.editEntityButton);
      // ---------------------end_setup
      
      // this is where entity metadata is inserted !!!!!!!!!!!!!!!!
      // add Component to 'currentEntityMetadataPanel' which has a borderlayout
      
      // -----------------------Test of MetaDisplay-------------------------
      MetaDisplay md = new MetaDisplay();
      Component mdcomponent = null;
      try{
        mdcomponent = md.getDisplayComponent(id, dp, null);
      }
      catch (Exception m) {
        Log.debug(20, "Unable to get metadisplay component"); 
      }
      currentEntityMetadataPanel.add(BorderLayout.CENTER, mdcomponent);
// ------------------==End Test of MetaDisplay-------------------------

//      currentEntityMetadataPanel.add(BorderLayout.CENTER, entityInfoPanel);                                     
//      currentEntityMetadataPanel.add(BorderLayout.SOUTH, entityEditControls);                                     
//      currentEntityMetadataPanel.setMaximumSize(new Dimension(200,4000));

      currentEntityPanel.setDividerLocation(675);
      this.entityFile[i] = entityEdit.entityFile;
    
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

// public setters for various members  
  public void setFramework(Morpho cf) {
    this.morpho = cf;
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
   * This needs to be dynamically done as tabs are selected due to potentially
   * large memory usage
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
    
    DataViewer dv = new DataViewer(morpho, "DataFile: "+fn, f);
    dv.setDataID(dp.getDataFileID(id));
    dv.setPhysicalFile(fphysical);
    dv.setAttributeFile(fattribute);
    dv.setEntityFile(entityFile[index]);
    dv.setEntityFileId(id);
    dv.setDataPackage(this.dp);
    dv.init();
    dv.getEntityInfo();
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
    entityPanel.setDividerLocation(700);
    
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
