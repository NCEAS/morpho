/**
 *  '$RCSfile: DataViewContainerPanel.java,v $'Split
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: higgins $'
 *     '$Date: 2003-10-15 21:14:15 $'
 * '$Revision: 1.62 $'
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
import javax.swing.event.ChangeListener;
import java.io.*;
import java.util.*;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.datapackage.wizard.*;

import edu.ucsb.nceas.morpho.plugins.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.PluginInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceExistsException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.XMLTransformer;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.StoreStateChangeEvent;
import edu.ucsb.nceas.morpho.datastore.FileSystemDataStore;
import edu.ucsb.nceas.morpho.datastore.MetacatDataStore;
import edu.ucsb.nceas.morpho.datastore.CacheAccessException;
import edu.ucsb.nceas.morpho.query.LocalQuery;


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
             implements MouseListener,
                        ChangeListener, 
                        StateChangeListener, 
                        StoreStateChangeEvent,
                        EditingCompleteListener
{
  /**
   * top-panel metaviewer default title at startup
   */
  private static final String TOP_METAVIEW_TITLE = "Data Package Documentation";
    
  /**
   * top-panel metaviewer default title at startup
   */
  private static final String RIGHT_METAVIEW_TITLE = "Entity/Attribute";
    
  /**
   * The DataPackage that contains the data
   */
  DataPackage dp;

  /**
   * The AbstractDataPackage that contains the data
   */
  AbstractDataPackage adp;
  
  /**
   * toppanel is added to packageMetadataPanel by init
   */
  JPanel toppanel;
  
  private JPanel packagePanel;
  
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
  
  // Store the current data viewer
  private DataViewer dv = null;
  
  Hashtable listValueHash = new Hashtable();
  JSplitPane entityPanel;
  JPanel entityMetadataPanel;
  JPanel currentDataPanel;
  JSplitPane vertSplit;
  
  Vector entityItems = null;
  PersistentVector lastPV = null;
  JLabel moreLabel;
  

  private static MetaDisplayFactoryInterface metaDisplayFactory = null;
  
  private ActionListener mdHideListener;
  
  private static final int METADATA_PANEL_DEFAULT_WIDTH = 675;
  
  // Store the event
  private Vector storedStateChangeEventlist = new Vector();
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
//    tabbedEntitiesPanel.addChangeListener(this);

    tabbedEntitiesPanel.addMouseListener(
      new MouseAdapter() {
        
        String id = null;
        String item = null;
        MetaDisplayInterface mdi = null;
        TabbedContainer container = null;
        
        public void mouseReleased(MouseEvent me) { 
          container = (TabbedContainer)
                            tabbedEntitiesPanel.getComponentAt(lastTabSelected);
          mdi = container.getMetaDisplayInterface();
          item = (String)entityItems.elementAt(lastTabSelected);
//          id = (String)listValueHash.get(item);
          id = getEntityIDForThisEntityName(item);
          if (id==mdi.getIdentifier()) return;
          //update metaview to show entity:
          try {
            mdi.display(id);
          } catch (DocumentNotFoundException m) {
            Log.debug(5, "Unable to display Entity:\n"+m.getMessage());
          }
          //deselect columns in table:
          resetTableSelection();
        }
      });
    
    // this panel added just so splitter colors can be set
    JPanel entitiesContainerPanel = new JPanel();
//    entitiesContainerPanel.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    entitiesContainerPanel.setLayout(new BorderLayout(0,0));
    entitiesContainerPanel.add(BorderLayout.CENTER, tabbedEntitiesPanel);
    vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, packageMetadataPanel,
                                                          entitiesContainerPanel);
    if (UIManager.getSystemLookAndFeelClassName().indexOf(
                                                "WindowsLookAndFeel")>-1) {
      vertSplit.setUI(new javax.swing.plaf.metal.MetalSplitPaneUI());
    }
    SymComponent aSymComponent = new SymComponent();
		packageMetadataPanel.addComponentListener(aSymComponent);

    vertSplit.setOneTouchExpandable(true);
    this.add(BorderLayout.CENTER,vertSplit);
    vertSplit.setDividerLocation(UISettings.VERT_SPLIT_INIT_LOCATION);
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
    JLabel authorRefLabel = new JLabel(dpgui.authorRefLabel);
    authorRefLabel.setFont(UISettings.TITLE_CITATION_FONT);
    JLabel titleRefLabel = new JLabel(dpgui.titleRefLabel);
    titleRefLabel.setFont(UISettings.TITLE_CITATION_FONT_BOLD);
    JLabel accessionRefLabel = new JLabel(dpgui.accessionRefLabel);
    accessionRefLabel.setFont(UISettings.TITLE_CITATION_FONT_BOLD);
    JLabel keywordsRefLabel = new JLabel(dpgui.keywordsRefLabel);
    keywordsRefLabel.setFont(UISettings.TITLE_CITATION_FONT);
    moreLabel = new JLabel("<html><a href=\".\"><b>more</b></a></html>");
    moreLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    moreLabel.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    moreLabel.addMouseListener(this);
    moreLabel.setToolTipText("Show more/less of package documentation");

    JPanel refPanelTop = new JPanel();
    refPanelTop.setPreferredSize(UISettings.TITLE_CITATION_DIMS);
    refPanelTop.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
    refPanelTop.setLayout(new BorderLayout(0,0));
    refPanelTop.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    JPanel refPanel = new JPanel();
    refPanel.setLayout(new GridLayout(3,1));
    refPanel.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    JPanel refPanelLine1 = new JPanel();
    refPanelLine1.setLayout(new BorderLayout(0,0));
    refPanelLine1.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    refPanelLine1.add(BorderLayout.WEST, authorRefLabel);
    refPanelLine1.add(BorderLayout.CENTER, titleRefLabel);
    JPanel refPanelLine2 = new JPanel();
    refPanelLine2.setLayout(new BorderLayout(0,0));
    refPanelLine2.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    refPanelLine2.add(BorderLayout.WEST, accessionRefLabel);
    refPanelLine2.add(BorderLayout.CENTER, keywordsRefLabel);
    refPanel.add(refPanelLine1);
    refPanel.add(refPanelLine2);
    refPanel.add(moreLabel);
    refPanelTop.add(BorderLayout.CENTER, refPanel);
    
 //   refPanel.add(BorderLayout.CENTER, refLabel);
 //   refLabel.setFont(UISettings.TITLE_CITATION_FONT);

    // location panel contains 2 labels whose icons indicate wether the
    // displayed package is local/network or both
    JPanel locationPanel = new JPanel();
  //  locationPanel.setBackground(UISettings.CUSTOM_GRAY);
    Border margin0 = BorderFactory.createEmptyBorder(0, 2,0,2); //top,lft,bot,rgt
    locationPanel.setPreferredSize(UISettings.TITLE_LOCATION_DIMS);
    locationPanel.setBorder(margin0);
    locationPanel.setBackground(UISettings.CUSTOM_GRAY);
    locationPanel.setOpaque(true);
    ImageIcon localIcon 
      = new ImageIcon(getClass().getResource("local-package-small.png"));
    ImageIcon metacatIcon 
      = new ImageIcon(getClass().getResource("network-package-small.png"));
    ImageIcon blankIcon 
      = new ImageIcon(getClass().getResource("blank.gif"));
    JLabel localLabel = new JLabel("local");
    localLabel.setBackground(UISettings.CUSTOM_GRAY);
    localLabel.setOpaque(true);
    localLabel.setFont(UISettings.TITLE_LOCATION_FONT);
    localLabel.setIcon(localIcon);
    localLabel.setToolTipText("Package is stored locally");
    JLabel netLabel = new JLabel("net");
    netLabel.setBackground(UISettings.CUSTOM_GRAY);
    netLabel.setFont(UISettings.TITLE_LOCATION_FONT);
    netLabel.setOpaque(true);
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
    
    refPanelTop.add(BorderLayout.EAST, locationPanel);
    
// refpanel is created in this class and added to the top of the 
// panel in the next statement  
    packagePanel.add(BorderLayout.NORTH,refPanelTop);
    this.morpho = dpgui.morpho;
    this.toppanel = packagePanel;
    this.entityItems = dpgui.getEntityitems();
    this.listValueHash = dpgui.listValueHash;
    this.setVisible(true);
  // trying to get the height here always gives zero
//  vertSplit.setDividerLocation(refPanel.getHeight());

  }
 
  /*
   * this constructor uses an 'AbstractDataPackage' object
   * to build the interior object in the Panel
   */
  public DataViewContainerPanel(AbstractDataPackage adp)
  {
    this();
    this.adp = adp;
    JPanel packagePanel = new JPanel();
    packagePanel.setLayout(new BorderLayout(5,5));
    
    entityItems = new Vector();
    int numEnts = (adp.getEntityArray()).length;
    for (int k=0;k<numEnts;k++) {
      entityItems.addElement(adp.getEntityName(k));
    }

// the following code builds the datapackage summary at the top of
// the DataViewContainerPanel
    JLabel authorRefLabel = new JLabel((adp.getAuthor()).trim()+":    ");
    authorRefLabel.setFont(UISettings.TITLE_CITATION_FONT);
    JLabel titleRefLabel = new JLabel(adp.getTitle());
    titleRefLabel.setFont(UISettings.TITLE_CITATION_FONT_BOLD);
    JLabel accessionRefLabel = new JLabel("Accession Number: "+adp.getAccessionNumber()+ " ");
    accessionRefLabel.setFont(UISettings.TITLE_CITATION_FONT_BOLD);
    JLabel keywordsRefLabel = new JLabel("Keywords: "+adp.getKeywords());
    keywordsRefLabel.setFont(UISettings.TITLE_CITATION_FONT);
    moreLabel = new JLabel("<html><a href=\".\"><b>more</b></a></html>");
    moreLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    moreLabel.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    moreLabel.addMouseListener(this);
    moreLabel.setToolTipText("Show more/less of package documentation");

    JPanel refPanelTop = new JPanel();
    refPanelTop.setPreferredSize(UISettings.TITLE_CITATION_DIMS);
    refPanelTop.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
    refPanelTop.setLayout(new BorderLayout(0,0));
    refPanelTop.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    JPanel refPanel = new JPanel();
    refPanel.setLayout(new GridLayout(3,1));
    refPanel.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    JPanel refPanelLine1 = new JPanel();
    refPanelLine1.setLayout(new BorderLayout(0,0));
    refPanelLine1.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    refPanelLine1.add(BorderLayout.WEST, authorRefLabel);
    refPanelLine1.add(BorderLayout.CENTER, titleRefLabel);
    JPanel refPanelLine2 = new JPanel();
    refPanelLine2.setLayout(new BorderLayout(0,0));
    refPanelLine2.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    refPanelLine2.add(BorderLayout.WEST, accessionRefLabel);
    refPanelLine2.add(BorderLayout.CENTER, keywordsRefLabel);
    refPanel.add(refPanelLine1);
    refPanel.add(refPanelLine2);
    refPanel.add(moreLabel);
    refPanelTop.add(BorderLayout.CENTER, refPanel);
    
 //   refPanel.add(BorderLayout.CENTER, refLabel);
 //   refLabel.setFont(UISettings.TITLE_CITATION_FONT);

    // location panel contains 2 labels whose icons indicate wether the
    // displayed package is local/network or both
    JPanel locationPanel = new JPanel();
  //  locationPanel.setBackground(UISettings.CUSTOM_GRAY);
    Border margin0 = BorderFactory.createEmptyBorder(0, 2,0,2); //top,lft,bot,rgt
    locationPanel.setPreferredSize(UISettings.TITLE_LOCATION_DIMS);
    locationPanel.setBorder(margin0);
    locationPanel.setBackground(UISettings.CUSTOM_GRAY);
    locationPanel.setOpaque(true);
    ImageIcon localIcon 
      = new ImageIcon(getClass().getResource("local-package-small.png"));
    ImageIcon metacatIcon 
      = new ImageIcon(getClass().getResource("network-package-small.png"));
    ImageIcon blankIcon 
      = new ImageIcon(getClass().getResource("blank.gif"));
    JLabel localLabel = new JLabel("local");
    localLabel.setBackground(UISettings.CUSTOM_GRAY);
    localLabel.setOpaque(true);
    localLabel.setFont(UISettings.TITLE_LOCATION_FONT);
    localLabel.setIcon(localIcon);
    localLabel.setToolTipText("Package is stored locally");
    JLabel netLabel = new JLabel("net");
    netLabel.setBackground(UISettings.CUSTOM_GRAY);
    netLabel.setFont(UISettings.TITLE_LOCATION_FONT);
    netLabel.setOpaque(true);
    netLabel.setIcon(metacatIcon);
    netLabel.setToolTipText("Package is stored on the network");
    String location = adp.getLocation();
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
    
    refPanelTop.add(BorderLayout.EAST, locationPanel);
// refpanel is created in this class and added to the top of the 
// panel in the next statement  
    packagePanel.add(BorderLayout.NORTH,refPanelTop);
//    this.morpho = dpgui.morpho;
    this.toppanel = packagePanel;
//    this.entityItems = dpgui.getEntityitems();
//    this.listValueHash = dpgui.listValueHash;
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
      Log.debug(12, "toppanel is null");
    }
    else {
      packageMetadataPanel.removeAll();
      packageMetadataPanel.add(BorderLayout.CENTER,toppanel);
    }
    if (entityItems==null) {
      Log.debug(20, "EntityItems vector is null");
      vertSplit.removeAll();
      vertSplit.add(packageMetadataPanel);
      if (GUIAction.getMorphoFrameAncestor(this) == null)
      {
        //Store the event
        storingStateChangeEvent(new StateChangeEvent(this, 
                          StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME));
      }
      else
      {
        StateChangeMonitor.getInstance().notifyStateChange(
                      new StateChangeEvent(this, 
                          StateChangeEvent.CREATE_NOENTITY_DATAPACKAGE_FRAME));
      }
      moreLabel.setVisible(false);
//      initDPMetaView(false);
      return;
    }
    entityFile = new File[entityItems.size()];
    for (int i=0;i<entityItems.size();i++) {
      JSplitPane currentEntityPanel = createEntityPanel();
      
      String item = (String)entityItems.elementAt(i);
      // id is the id of the Entity metadata module
      // code from here to 'end_setup' comment sets up the display for the
      // entity metadata
      String id = getEntityIDForThisEntityName(item);
      
//      String location = dp.getLocation();
       
      JPanel currentEntityMetadataPanel = (JPanel)currentEntityPanel.getRightComponent();
    
    MetaDisplayInterface md = null;
    if (dp!=null) {  // old datapackage
      // this is where entity metadata is inserted !!!!!!!!!!!!!!!!
      // add Component to 'currentEntityMetadataPanel' which has a borderlayout
      
// --------- E N T I T Y / A T T R I B U T E   M e t a D i s p l a y -----------
      md = getMetaDisplayInstance();
      md.addEditingCompleteListener(this);
      md.setTitle(RIGHT_METAVIEW_TITLE);
      Component mdcomponent = null;
      try{
        mdcomponent = md.getDisplayComponent(id, dp, 
                                      new MetaViewListener(currentEntityPanel));
      }
      catch (Exception m) {
        Log.debug(5, "Unable to display MetaData:\n"+m.getMessage()); 
        // can't display requested ID, so just display empty viewer:
        try{
          mdcomponent = md.getDisplayComponent(dp, null);
        }
        catch (Exception e) {
          Log.debug(15, "Error showing blank MetaData view:\n"+e.getMessage()); 
          e.printStackTrace();
        }
      }
      currentEntityMetadataPanel.add(BorderLayout.CENTER, mdcomponent);
    }
      currentEntityPanel.setDividerLocation(METADATA_PANEL_DEFAULT_WIDTH);
      
      // create a tabbed component instance
      TabbedContainer component = new TabbedContainer();
      component.setSplitPane(currentEntityPanel);
      if(dp != null) {
        component.setMetaDisplayInterface(md);
      }
      component.setVisible(true);
      tabbedEntitiesPanel.addTab((String)entityItems.elementAt(i),component);
      //tabbedEntitiesPanel.addTab((String)entityItems.elementAt(i), currentEntityPanel);
      if (dp!=null) {
        this.entityFile[i] = dp.getFileFromId(id);
      }
    
      // create the data display panel (usually a table) using DataViewer class
//      String fn = dp.getDataFileName(id);    
//      File fphysical = dp.getPhysicalFile(id);
//      File fattribute = dp.getAttributeFile(id);
//      File f = dp.getDataFile(id);
//      String dataString = "";
    }
    tabbedEntitiesPanel.addChangeListener(this);
    
    if (entityItems.size()>0) 
    {
      setDataViewer(0);
      
      // Register the instance of this class as an listener in state change 
      // monitor
      StateChangeMonitor stateMonitor = StateChangeMonitor.getInstance();
      stateMonitor.addStateChangeListener
                                (StateChangeEvent.SELECT_DATATABLE_COLUMN,this);
      stateMonitor.addStateChangeListener
                                (StateChangeEvent.METAVIEWER_HISTORY_BACK,this);

      if (GUIAction.getMorphoFrameAncestor(this) == null)
      {
        //Store the event
        storingStateChangeEvent(new StateChangeEvent( this, 
                            StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME));
        
      }//
      else
      {                         
        stateMonitor.notifyStateChange( new StateChangeEvent( this, 
                            StateChangeEvent.CREATE_ENTITY_DATAPACKAGE_FRAME));
      }//else
      if (dp!=null) {
        initDPMetaView(true);
      }
    } else {
      moreLabel.setEnabled(false);
      initDPMetaView(false);
    }
  }

    // this is where the datapackage metadata is inserted into the container.
    // Simply add Component to the packagePanel container, which has a Border
    // layout. leave the 'NORTH' region empty, since the
    // refpanel is added there later
    private void initDPMetaView(boolean hasData) 
    {
// -------------- D A T A P A C K A G E   M e t a D i s p l a y ----------------
      MetaDisplayInterface md = getMetaDisplayInstance();
      md.addEditingCompleteListener(this);
      md.setTitle(TOP_METAVIEW_TITLE);
      Component mdcomponent = null;
  
      if (hasData) {
        StringBuffer suppressBuff = new StringBuffer();
        String nextAttributeFileID = null;
        String nextDataFileID = null;
        String nextEntityID = null;
        Object nextObj = null;
        Iterator it = entityItems.iterator();
        while (it.hasNext()) {
            nextObj = it.next();
            if (nextObj==null || !(nextObj instanceof String)) continue;
            if (dp!=null) {
              nextEntityID        = getEntityIDForThisEntityName((String)nextObj);
              nextDataFileID      = dp.getDataFileID(nextEntityID);
              nextAttributeFileID = dp.getAttributeFileId(nextEntityID);
            }
            
            if (nextDataFileID!=null && !nextDataFileID.equals("")) {
                suppressBuff.append(XMLTransformer.SUPPRESS_TRIPLES_DELIMETER);
                suppressBuff.append(nextDataFileID);
                Log.debug(44,"adding datafile: "+nextDataFileID
                                                     +" to XSL suppress list");
            }
            
            if (nextAttributeFileID!=null && !nextAttributeFileID.equals("")) {
                suppressBuff.append(XMLTransformer.SUPPRESS_TRIPLES_DELIMETER);
                suppressBuff.append(nextAttributeFileID);
                Log.debug(44,"adding attribute: "+nextAttributeFileID
                                                     +" to XSL suppress list");
            }
        }
        md.useTransformerProperty(  
                              XMLTransformer.SUPPRESS_TRIPLES_SUBJECTS_XSLPROP, 
                              suppressBuff.toString());
        md.useTransformerProperty( 
                              XMLTransformer.SUPPRESS_TRIPLES_OBJECTS_XSLPROP, 
                              suppressBuff.toString());
      }
      try{
        mdcomponent = md.getDisplayComponent( dp.getID(), dp,  
                                              new MetaViewListener(vertSplit));
      }
      catch (Exception m) {
        Log.debug(5, "Unable to display MetaData:\n"+m.getMessage()); 
        // can't display requested ID, so just display empty viewer:
        try{
          if (dp!=null) {
            mdcomponent = md.getDisplayComponent(dp, null);
          }
        }
        catch (Exception e) {
          Log.debug(15, "Error showing blank MetaData view:\n"+e.getMessage()); 
          e.printStackTrace();
        }
      }
      toppanel.add(BorderLayout.CENTER, mdcomponent);
// ---------------------- End DataPackage MetaDisplay --------------------------
    }  
  
  
  
// public setters for various members  
  public void setFramework(Morpho cf) {
    this.morpho = cf;
  }
  
  /**
   * Method to get frame work
   */
  public Morpho getFramework()
  {
    return morpho;
  }
  
  /**
   * Method to get data package
   */
  public DataPackage getDataPackage()
  {
    return dp;
  }
  
  /**
   * Method to get current data viewer
   */
  public DataViewer getCurrentDataViewer()
  {
    return dv;
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
   * Method implements from StateChangeListener. This method will handle
   * state change event
   */
  public void handleStateChange(StateChangeEvent event)
  {
    // Handle select data table column
    if ((event.getChangedState()).equals(StateChangeEvent.SELECT_DATATABLE_COLUMN))
    {
      // Get attribute file id and show it the metacat panel
      if (event.getSource() instanceof JTable) {
        showDataViewAndAttributePanel(
                            ( (JTable)event.getSource() ).getSelectedColumn());
      } else {
        showDataViewAndAttributePanel(0);
      }
    }
    if (event.getChangedState().equals(StateChangeEvent.METAVIEWER_HISTORY_BACK)
            && dv!=null && tabbedEntitiesPanel!=null && dv.getDataTable()!=null)
    {
        // Get new ID displayed in metaviewer
        TabbedContainer container = 
           (TabbedContainer)tabbedEntitiesPanel.getComponentAt(lastTabSelected);
        String newID = container.getMetaDisplayInterface().getIdentifier();
        Log.debug(50,"newID="+newID);

        //check if it's an entity (if so, deselect all)  
        //or an attribute (if so, select that attribute)
        String item = (String)entityItems.elementAt(lastTabSelected);
//        String entityID = (String)listValueHash.get(item);
        String entityID = getEntityIDForThisEntityName(item);
        if (newID.equalsIgnoreCase(entityID)) {
          //deselect columns in table:
          resetTableSelection();
        } else {
          String selectedAttribs
              = container.getMetaDisplayInterface().getTransformerProperty(
                                      XMLTransformer.SELECTED_ATTRIBS_XSLPROP);
          int selectedColIndex = -1;
          try {
            selectedColIndex = Integer.parseInt(selectedAttribs);
          } catch (NumberFormatException nfe) {
            Log.debug(12,"Can't handle multiple column selections yet!!");
            return;
          }
          dv.getDataTable().setColumnSelectionInterval( selectedColIndex,
                                                        selectedColIndex);
          
          Log.debug(50,"& & & & & & & & & & selectedAttribs="+selectedAttribs
                                   +";\n selectedColIndex = "+selectedColIndex);
        }
    }
  }
  
  private void resetTableSelection()
  {
    if (dv!=null && dv.getDataTable()!=null) {
      JTable table = dv.getDataTable();
    
      if (table.getRowCount()>0 && table.getColumnCount()>0) {
        table.setRowSelectionInterval(0,0);
        table.setColumnSelectionInterval(0,0);
      }
    }
  }
  
  //convenience method to get the entity identifier for a given entity 
  //title/name
  private String getEntityIDForThisEntityName(String entityName) {
  
      return (String)listValueHash.get(entityName);
  }
  
  /**
   * creates the data display and puts it into the center of the window
   * This needs to be dynamically done as tabs are selected due to potentially
   * large memory usage
   */
  private void setDataViewer(int index) {
    TabbedContainer comp = 
        (TabbedContainer) tabbedEntitiesPanel.getComponentAt(lastTabSelected);
    JSplitPane entireDataPanel = comp.getSplitPane();
    JPanel currentDataPanelOld = (JPanel)entireDataPanel.getLeftComponent();
    removePVObject();
    currentDataPanelOld.removeAll();
    lastTabSelected = index;
    String dataId = null;
    if (dp!=null) {
      String item = (String)entityItems.elementAt(index);
      String id = getEntityIDForThisEntityName(item);
      dataId = dp.getDataFileID(id);
      String fn = dp.getDataFileName(id);    
      File fphysical = dp.getPhysicalFile(id);
      File fattribute = dp.getAttributeFile(id);
      File f = dp.getDataFile(id);
      String dataString = "";
      dv = new DataViewer(morpho, "DataFile: "+fn, f);
      dv.setDataID(dp.getDataFileID(id));
      dv.setPhysicalFile(fphysical);
      dv.setAttributeFile(fattribute);
      dv.setEntityFile(entityFile[index]);
      dv.setEntityFileId(id);
      dv.setDataPackage(this.dp);
    } else {  // new eml2.0.0
      dv = new DataViewer(morpho, "DataFile: ", null);  // file is null for now
      dv.setAbstractDataPackage(adp);
      dv.setEntityIndex(index);
      File testFile = new File("C:/Documents and Settings/higgins/.morpho/profiles/higgins/data/jscientist/3.1");
      dv.setDataFile(testFile);
    }
    dv.init();
//    dv.getEntityInfo();  // this is already done in init
    lastPV = dv.getPV();
    JPanel tablePanel = null;
    if (dv.getShowDataView())
    {
      tablePanel = dv.DataViewerPanel;
    }
    else
    {
      tablePanel = new JPanel();
      tablePanel.add(BorderLayout.NORTH, Box.createVerticalStrut(80));
      String text = null;
      if (dataId.equals("") || dataId == null)
      {
        text = "Either there is no data file, or format of data "+
               "file was not recongnized!";
      }
      else
      {
        text = "Data in data file "+ dataId+" cannot be read!";
      }
      JLabel warning = new JLabel(text);
      warning.setForeground(UISettings.ALERT_TEXT_COLOR);
      tablePanel.add(BorderLayout.CENTER, warning);
    }
    
    tablePanel.setOpaque(true);
    tablePanel.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    //JSplitPane EntireDataPanel = (JSplitPane)(tabbedEntitiesPanel.getComponentAt(index));
    //JPanel currentDataPanel = (JPanel)EntireDataPanel.getLeftComponent();
    TabbedContainer compn = 
        (TabbedContainer) tabbedEntitiesPanel.getComponentAt(index);
    JSplitPane entireDataPane = compn.getSplitPane();
    JPanel currentDataPanel = (JPanel)entireDataPane.getLeftComponent();
    currentDataPanel.setLayout(new BorderLayout(0,0));
    currentDataPanel.add(BorderLayout.CENTER,tablePanel);
    currentDataPanel.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
    if (GUIAction.getMorphoFrameAncestor(this) == null)
    {
      //Store the event create from its kid - DataViewer
      Vector eventList = dv.getStoredStateChangeEvent();
      if (eventList != null)
      {
        for (int i= 0; i<eventList.size(); i++)
        {
          StateChangeEvent eventInKid = 
                                  (StateChangeEvent) eventList.elementAt(i);
          storingStateChangeEvent(eventInKid);
        }//for
      }//if
        
    }//if
    else
    {
      //frame already has, borading the event
      dv.broadcastStoredStateChangeEvent();
    }
   
  }
  
  private JSplitPane createEntityPanel() {
    JPanel entityMetadataPanel = new JPanel();
    entityMetadataPanel.setLayout(new BorderLayout(0,0));
    dataViewPanel = new JPanel();
    dataViewPanel.setLayout(new BorderLayout(0,0));
    
    entityPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,dataViewPanel, entityMetadataPanel);
    if (UIManager.getSystemLookAndFeelClassName().indexOf("WindowsLookAndFeel")>-1) {
      entityPanel.setUI(new javax.swing.plaf.metal.MetalSplitPaneUI());
    }
    entityPanel.setOneTouchExpandable(true);
    entityPanel.setDividerLocation(700);
    
    return entityPanel;
  }
  
  /*  Method to create a attribute panel to replace entity */
  private void showDataViewAndAttributePanel(int selectedColIndex) 
  {
    TabbedContainer container = 
        (TabbedContainer) tabbedEntitiesPanel.getComponentAt(lastTabSelected);
    MetaDisplayInterface meta = container.getMetaDisplayInterface();
    // Get attribute file identifier
    String item = (String)entityItems.elementAt(lastTabSelected);
//    String id = (String)listValueHash.get(item);
    String id = getEntityIDForThisEntityName(item);
    String identifier = dp.getAttributeFileId(id);
    try
    {
      meta.useTransformerProperty(XMLTransformer.SELECTED_ATTRIBS_XSLPROP,
                                  String.valueOf(selectedColIndex));
      meta.display(identifier);
                        
    } catch (DocumentNotFoundException m) {
      Log.debug(5, "Unable to display Attribute:\n"+m.getMessage());
    }
  }
  
  public void stateChanged(javax.swing.event.ChangeEvent event) {
    Object object = event.getSource();
    if (object == tabbedEntitiesPanel) {
      int k = tabbedEntitiesPanel.getSelectedIndex();
      setDataViewer(k);
    }
  }
  
  
  private MetaDisplayInterface getMetaDisplayInstance()
  {
    if (metaDisplayFactory==null) {
      ServiceProvider provider = null;
      try {
        ServiceController services = ServiceController.getInstance();
        provider=services.getServiceProvider(MetaDisplayFactoryInterface.class);
      } catch(ServiceNotHandledException ee) {
        Log.debug(0, "Error acquiring MetaDisplay plugin: "+ee);
        ee.printStackTrace();
        return null;
      }
      metaDisplayFactory = (MetaDisplayFactoryInterface)provider;
    }
    return metaDisplayFactory.getInstance();
  }

  /**
   * Method implements form StoreStateChangeEvent
   * This method will be called to store a event
   *
   * @param event  the state change event need to be stored
   */
  public void storingStateChangeEvent(StateChangeEvent event)
  {
    if (storedStateChangeEventlist != null)
    {
      storedStateChangeEventlist.add(event);
    }
  }
  
    
  /**
   * Get the  stored state change event.
   */
  public Vector getStoredStateChangeEvent()
  {
    return storedStateChangeEventlist;
  }
  
  /**
   * Broadcast the stored StateChangeEvent
   */
  public void broadcastStoredStateChangeEvent()
  {
    if (storedStateChangeEventlist != null)
    {
      for ( int i = 0; i< storedStateChangeEventlist.size(); i++)
      {
        StateChangeEvent event = 
                (StateChangeEvent) storedStateChangeEventlist.elementAt(i);
        (StateChangeMonitor.getInstance()).notifyStateChange(event);
      }//for
    }//if
  }
  
  /*
   * A class to keep the information for every tabbed panel
   */
  private class TabbedContainer extends Container
  {
    // JSplitPanel in tabbed panel
    JSplitPane splitPane = null;
    // MetaDispaly in the splitPane
    MetaDisplayInterface metaDisplay = null;
    
    /*
     * Constructor of this class
     */
    public TabbedContainer()
    {
      super();
      this.setLayout(new BorderLayout());
    }
    /*
     * Method to get the JSplitPane
     */
    public JSplitPane getSplitPane()
    {
      return splitPane;
    }
    
    /*
     * Method to set up SplitPane
     */
    public void setSplitPane(JSplitPane pane)
    {
      splitPane = pane;
      this.add(splitPane, BorderLayout.CENTER);
    }
    
    /*
     * Method to get meta display interface
     */
    public MetaDisplayInterface getMetaDisplayInterface()
    {
      return metaDisplay;
    }
    
    /*
     * Method to set meta display interface
     */
    public void setMetaDisplayInterface(MetaDisplayInterface display)
    {
      metaDisplay = display;
    }
    
  }//TabbedComponent

  class MetaViewListener implements ActionListener 
  {
  
    private JSplitPane entitySplitPane;
    
    MetaViewListener(JSplitPane splitPane){
      this.entitySplitPane = splitPane;
    }
  
    public void actionPerformed(ActionEvent e)
    {
      int closedPosition = 0;
      if (e.getID()==MetaDisplayInterface.CLOSE_EVENT) {
              if (entitySplitPane.getOrientation()==JSplitPane.VERTICAL_SPLIT){
                closedPosition = UISettings.VERT_SPLIT_INIT_LOCATION;
                Log.debug(50,"VERTICAL_SPLIT, closedPosition="+closedPosition);
                moreLabel.setText("<html><a href=\".\"><b>more</b></a></html>");
              } else {
                closedPosition = entitySplitPane.getWidth();
                Log.debug(50,"HORIZONAL_SPLIT, closedPosition="+closedPosition);
              }
              entitySplitPane.setDividerLocation(closedPosition);
      }
    }
  }

  public void editingCompleted(String xmlString, String id, String location) {

    Log.debug(11, "editing complete: id: " + id + " location: " + location);
    
    /* metadisplay class does not 'know' the location of a package
     * so it is set to null when the editor is called.
     * We thus get it here by checking the dataPackage instance.
     */
    if (location==null) {
      location = dp.getLocation();
    }
    AccessionNumber a = new AccessionNumber(morpho);
    boolean metacatpublic = false;
    FileSystemDataStore fsds = new FileSystemDataStore(morpho);
    boolean metacatloc = false;
    boolean localloc = false;
    boolean bothloc = false;
    String newid = "";
    String newPackageId = "";
    if(location.equals(DataPackageInterface.BOTH))
    {
      metacatloc = true;
      localloc = true;
    }
    else if(location.equals(DataPackageInterface.METACAT))
    {
      metacatloc = true;
    }
    else if(location.equals(DataPackageInterface.LOCAL))
    {
      localloc = true;
    }
  
    try
    { 
      if(localloc)
      { //save the file locally
        if(id.trim().equals(dp.getID().trim()))
        { //we just edited the package file itself
          String oldid = id;
          newid = a.incRev(id);
          File f = fsds.saveTempFile(oldid, new StringReader(xmlString));
          
          // remove old version from cache
          if (LocalQuery.dom_collection.containsKey(oldid)) {
              LocalQuery.dom_collection.remove(oldid);
          }

     if (f==null) {
        Log.debug(1,"file is null!");  
     }
          String newPackageFile = a.incRevInTriples(f, oldid, newid);
          fsds.saveFile(newid, new StringReader(newPackageFile));
          newPackageId = newid;
        }
        else
        { //we edited a file in the package
          Vector newids = new Vector();
          Vector oldids = new Vector();
          String oldid = id;
          newid = a.incRev(id);
          fsds.saveFile(newid, new StringReader(xmlString));
          newPackageId = a.incRev(dp.getID());
          oldids.addElement(oldid);
          oldids.addElement(dp.getID());
          newids.addElement(newid);
          newids.addElement(newPackageId);
          //increment the package files id in the triples
          String newPackageFile = a.incRevInTriples(dp.getTriplesFile(), 
                                                    oldids, 
                                                    newids);
          System.out.println("oldid: " + oldid + " newid: " + newid);          
          fsds.saveFile(newPackageId, new StringReader(newPackageFile)); 
        }
      }
    }
    catch(Exception e)
    {
      Log.debug(0, "Error saving file locally"+ id + " to " + location +
                         "--message: " + e.getMessage());
      Log.debug(11, "File: " + xmlString);
      e.printStackTrace();
    }
    
    try
    {
      if(metacatloc)
      { //save it to metacat
        MetacatDataStore mds = new MetacatDataStore(morpho);
        
        if(id.trim().equals(dp.getID().trim()))
        { //edit the package file
          Vector oldids = new Vector();
          Vector newids = new Vector();
          String oldid = id;
          newid = a.incRev(id);
          File f = fsds.saveTempFile(oldid, new StringReader(xmlString));
          oldids.addElement(oldid);
          newids.addElement(newid);
          String newPackageFile = a.incRevInTriples(f, oldids, newids);
          mds.saveFile(newid, new StringReader(newPackageFile), 
                       dp);
          newPackageId = newid;
        }
        else
        { //edit another file in the package
          Vector oldids = new Vector();
          Vector newids = new Vector();
          String oldid = id;
          newid = a.incRev(id);
 //         mds.saveFile(newid, new StringReader(xmlString), dp);
          Vector names = new Vector();
          Vector readers = new Vector();
          names.addElement(newid);
          readers.addElement(new StringReader(xmlString));
          newPackageId = a.incRev(dp.getID());
          //increment the package files id in the triples
          oldids.addElement(oldid);
          oldids.addElement(dp.getID());
          newids.addElement(newid);
          newids.addElement(newPackageId);
          String newPackageFile = a.incRevInTriples(dp.getTriplesFile(),
                                                    oldids,
                                                    newids);
//          mds.saveFile(newPackageId, new StringReader(newPackageFile), 
//                       dp);
          names.addElement(newPackageId);
          readers.addElement(new StringReader(newPackageFile));
          String res = mds.saveFilesTransaction(names, readers, dp);
          Log.debug(20,"Transaction result is: "+res);
        }
      }
    }
    catch(Exception e)
    {
      String message = e.getMessage();
      if(message.indexOf("Next revision number must be") != -1)
      {
        Log.debug(0,"The file you are attempting to update " +
                                 "has been changed by another user.  " +
                                 "Please refresh your query screen, " + 
                                 "open the package again and " +
                                 "re-enter your changes.");
        return;
      }
      Log.debug(0, "Error saving file to metacat "+ id + " to " + location +
                         "--message: " + e.getMessage());
      //Log.debug(11, "File: " + xmlString);
      e.printStackTrace();
    }
    MorphoFrame thisFrame = (UIController.getInstance()).getCurrentActiveWindow();
    
    // Show the new package
    try 
    {
      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider = 
                      services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackage = (DataPackageInterface)provider;
      dataPackage.openDataPackage(location, newPackageId, null, null, null);
    }
    catch (ServiceNotHandledException snhe) 
    {
       Log.debug(6, snhe.getMessage());
    }
    
    thisFrame.setVisible(false);
    UIController controller = UIController.getInstance();
    controller.removeWindow(thisFrame);
    thisFrame.dispose();

  }
  
  public void editingCanceled(String xmlString, String id, String location) {
    
  }
  
   public void mousePressed(MouseEvent e) {
   }

   public void mouseReleased(MouseEvent e) {
   }

   public void mouseEntered(MouseEvent e) {
  }

   public void mouseExited(MouseEvent e) {
   }

  public void mouseClicked(MouseEvent e) {
    String temp = moreLabel.getText();
    if (temp.indexOf("more")>-1) {
      moreLabel.setText("<html><a href=\".\"><b>less</b></a></html>");
      vertSplit.setDividerLocation(1.0);
    }
    else {
      moreLabel.setText("<html><a href=\".\"><b>more</b></a></html>");
      vertSplit.setDividerLocation(UISettings.VERT_SPLIT_INIT_LOCATION);
    }
 }

	class SymComponent extends java.awt.event.ComponentAdapter
	{
		public void componentResized(java.awt.event.ComponentEvent event)
		{
			Object object = event.getSource();
			if (object == packageMetadataPanel) {
				int cursize = vertSplit.getDividerLocation();
        int maxsize = vertSplit.getMaximumDividerLocation();
        if ((maxsize-cursize)<1) {
          moreLabel.setText("<html><a href=\".\"><b>less</b></a></html>");
        }
        else {
          moreLabel.setText("<html><a href=\".\"><b>more</b></a></html>");          
        }
      }
		}
	}

}

