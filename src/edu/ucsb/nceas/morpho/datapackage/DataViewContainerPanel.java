/**
 *  '$RCSfile: DataViewContainerPanel.java,v $'Split
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2008-10-02 02:28:57 $'
 * '$Revision: 1.108 $'
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Node;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.DataStoreServiceController;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.EditingCompleteListener;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayFactoryInterface;
import edu.ucsb.nceas.morpho.plugins.MetaDisplayInterface;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.util.Base64;
import edu.ucsb.nceas.morpho.util.DocumentNotFoundException;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeListener;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.StoreStateChangeEvent;
import edu.ucsb.nceas.morpho.util.UISettings;
import edu.ucsb.nceas.morpho.util.XMLTransformer;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * A panel that presents a data-centric view of a dataPackage. In fact, the panel is somewhat
 * complicated, with numerous subpanels and components
 * The panel is made up of several JSplitPanes. The Top of the first split pane shows datapackage
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
  private static final String TOP_METAVIEW_TITLE = 
	  Language.getInstance().getMessage("DataPackageDocumentation"); //"Data Package Documentation";

  /**
   * top-panel metaviewer default title at startup
   */
  private static final String RIGHT_METAVIEW_TITLE = 
	  Language.getInstance().getMessage("Entity") 
	  + "/" 
	  + Language.getInstance().getMessage("Attribute"); //"Entity/Attribute";


  /**
   * The MorphoDataPackage that contains the data
   */
  MorphoDataPackage mdp;

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

  // Store the current data viewer
  private DataViewer dv = null;

  Hashtable<String, String> listValueHash = new Hashtable<String, String>();
  JSplitPane entityPanel;
  JPanel entityMetadataPanel;
  JPanel currentDataPanel;
  JSplitPane vertSplit;

  Vector<String> entityItems = null;
  PersistentVector lastPV = null;
  JLabel moreLabel;

  private static MetaDisplayFactoryInterface metaDisplayFactory = null;

  private static final int METADATA_PANEL_DEFAULT_WIDTH = 675;

  // Store the event
  private Vector<StateChangeEvent> storedStateChangeEventlist = new Vector<StateChangeEvent>();
  /*
   * no parameter constuctor for DataViewContainerPanel.
   * Some basic gui setup
   */

  /**
   * this array keeps the DataViewer objects for a package with multiple
   * entities in memory. This insures that temporary changes are not lost
   * as one moves from one tab to another.
   */
  private DataViewer[] dvArray = null;

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
          item = entityItems.elementAt(lastTabSelected);
//          id = (String)listValueHash.get(item);
          id = mdi.getIdentifier();

          //update metaview to show entity:
          try {
            mdi.useTransformerProperty(XMLTransformer.SELECTED_DISPLAY_XSLPROP,
                            XMLTransformer.XSLVALU_DISPLAY_ENTITY);
            AbstractDataPackage adp = mdp.getAbstractDataPackage();
            String entityType = adp.getEntityType(lastTabSelected);
			mdi.useTransformerProperty(XMLTransformer.SELECTED_ENTIY_TYPE_XSLPROP,
					 entityType);
			int indexForEntityType = adp.getEntityIndexForType(lastTabSelected, entityType);
			mdi.useTransformerProperty(XMLTransformer.SELECTED_ENTITY_XSLPROP,
	                String.valueOf(indexForEntityType + 1));
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
   * this constructor uses an 'AbstractDataPackage' object
   * to build the interior object in the Panel
   */
  public DataViewContainerPanel(MorphoDataPackage mdp)
  {
    this();
    this.mdp = mdp;
    AbstractDataPackage adp = mdp.getAbstractDataPackage();
    JPanel packagePanel = new JPanel();
    packagePanel.setLayout(new BorderLayout(5,5));

    entityItems = new Vector<String>();
    int numEnts = 0;
    if (adp.getEntityArray()==null) {
      numEnts = 0;
    } else {
      numEnts = (adp.getEntityArray()).length;
    }
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
    if (location.equals(DataPackageInterface.NETWORK)) {
      localLabel.setText("");
      localLabel.setIcon(blankIcon);
    }
    else if (location.equals(DataPackageInterface.LOCAL)) {
      netLabel.setText("");
      netLabel.setIcon(blankIcon);
    }
    else if (location.equals(DataPackageInterface.BOTH)){

    }
    else {  // not yet saved anywhere
      localLabel.setText("");
      localLabel.setIcon(blankIcon);
      netLabel.setText("");
      netLabel.setIcon(blankIcon);
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
    if ((entityItems==null)||(entityItems.size()==0)) {
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
      initDPMetaView(false);
      return;
    }
    
    // moved out of loop since this is constant for each entity (BRL 20130117)
    AbstractDataPackage adp = mdp.getAbstractDataPackage();
    String id = adp.getAccessionNumber();
    if ((id==null)||(id.equals(""))) id = "tempid";
    
    entityFile = new File[entityItems.size()];
    for (int i=0;i<entityItems.size();i++) {
      JSplitPane currentEntityPanel = createEntityPanel();

      String item = entityItems.elementAt(i);
      // id is the id of the Entity metadata module
      // code from here to 'end_setup' comment sets up the display for the
      // entity metadata
//DFH      String id = getEntityIDForThisEntityName(item);

//      String location = dp.getLocation();

        JTabbedPane entityTabbedPane = (JTabbedPane) currentEntityPanel.getRightComponent();

      JPanel currentEntityMetadataPanel = (JPanel) entityTabbedPane.getComponent(0);

    MetaDisplayInterface md = null;
//DFH    if (dp!=null) {  // old datapackage
      // this is where entity metadata is inserted !!!!!!!!!!!!!!!!
      // add Component to 'currentEntityMetadataPanel' which has a borderlayout

// --------- E N T I T Y / A T T R I B U T E   M e t a D i s p l a y -----------
      md = getMetaDisplayInstance();
      md.addEditingCompleteListener(this);
      md.setTitle(RIGHT_METAVIEW_TITLE);

      md.useTransformerProperty(XMLTransformer.SELECTED_DISPLAY_XSLPROP,
                  XMLTransformer.XSLVALU_DISPLAY_ENTITY);
      md.useTransformerProperty(XMLTransformer.SELECTED_ENTITY_XSLPROP,
                                "1");
      String entityType = adp.getEntityType(0);
      md.useTransformerProperty(XMLTransformer.SELECTED_ENTIY_TYPE_XSLPROP,
				 entityType);


      Component mdcomponent = null;
      try{
        mdcomponent = md.getDisplayComponent(id, adp,
                                      new MetaViewListener(currentEntityPanel));
      }
      catch (Exception m) {
        Log.debug(5, "Unable to display MetaData:\n"+m.getMessage());
        m.printStackTrace();
        // can't display requested ID, so just display empty viewer:
        try{
          mdcomponent = md.getDisplayComponent(adp, null);
        }
        catch (Exception e) {
          Log.debug(15, "Error showing blank MetaData view:\n"+e.getMessage());
          e.printStackTrace();
        }
      }
      currentEntityMetadataPanel.add(BorderLayout.CENTER, mdcomponent);
      currentEntityPanel.setDividerLocation(METADATA_PANEL_DEFAULT_WIDTH);

      // create a tabbed component instance
      TabbedContainer component = new TabbedContainer();
      component.setSplitPane(currentEntityPanel);
        component.setMetaDisplayInterface(md);
      component.setVisible(true);
      tabbedEntitiesPanel.addTab(entityItems.elementAt(i), component);
      //tabbedEntitiesPanel.addTab((String)entityItems.elementAt(i), currentEntityPanel);

    }
    tabbedEntitiesPanel.addChangeListener(this);

    if (entityItems.size()>0)
    {
      dvArray = new DataViewer[entityItems.size()];
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
        initDPMetaView(true);
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
    	AbstractDataPackage adp = mdp.getAbstractDataPackage();
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
        Iterator<String> it = entityItems.iterator();
        while (it.hasNext()) {
            nextObj = it.next();
            if (nextObj==null || !(nextObj instanceof String)) continue;
        }
        md.useTransformerProperty(
                              XMLTransformer.SUPPRESS_TRIPLES_SUBJECTS_XSLPROP,
                              suppressBuff.toString());
        md.useTransformerProperty(
                              XMLTransformer.SUPPRESS_TRIPLES_OBJECTS_XSLPROP,
                              suppressBuff.toString());
      }
      else {  // there is no data to display!
        moreLabel.setText("<html><a href=\".\"><b>less</b></a></html>");
        vertSplit.setDividerLocation(1.0);
      }
      try{
    	  
        String tempid = adp.getAccessionNumber();
        if ((tempid==null)||(tempid.equals(""))) tempid = "tempid";
        mdcomponent = md.getDisplayComponent( tempid, adp,
                                              new MetaViewListener(vertSplit));
      }
      catch (Exception m) {
        Log.debug(30, "Unable to display Datapackage MetaData:\n"+m.getMessage());
        // can't display requested ID, so just display empty viewer:
        try{
            mdcomponent = md.getDisplayComponent(adp, null);
          moreLabel.setText("<html><a href=\".\"><b>less</b></a></html>");
          vertSplit.setDividerLocation(1.0);

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
   * Method to get current data viewer
   */
  public DataViewer getCurrentDataViewer()
  {
    return dv;
  }

  public JTabbedPane getEntityTabbedPane() {
	  TabbedContainer container = 
		  (TabbedContainer) tabbedEntitiesPanel.getComponentAt(lastTabSelected);
	  return (JTabbedPane) container.getSplitPane().getRightComponent();
}


public void setTopPanel(JPanel jp) {
    this.toppanel = jp;
    this.toppanel.setVisible(true);
  }
  public void setEntityItems(Vector<String> ei) {
    this.entityItems = ei;
  }
  public void setListValueHash(Hashtable<String, String> ht) {
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
	  //check if this is for our frame
	  MorphoFrame thisAncestor = GUIAction.getMorphoFrameAncestor(this);
	  if (!GUIAction.isLocalEvent(event, thisAncestor)) {
		  return;
	  }
	  
    // Handle select data table column
    if ((event.getChangedState()).equals(StateChangeEvent.SELECT_DATATABLE_COLUMN))
    {
      // Get attribute file id and show it the metacat panel
      if (event.getSource() instanceof JTable) {
    	  JTable table = (JTable)event.getSource();
    	  int viewIndex = table.getSelectedColumn();
    	  int modelIndex =  table.getColumnModel().getColumn(viewIndex).getModelIndex();
    	  runShowDataViewAndAttributePanel(modelIndex);
      } else {
        runShowDataViewAndAttributePanel(0);
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
        String item = entityItems.elementAt(lastTabSelected);
//        String entityID = (String)listValueHash.get(item);
        String entityID = getEntityIDForThisEntityName(item);
        if (newID.equalsIgnoreCase(entityID)) {
          //deselect columns in table:
          resetTableSelection();
        } else {
          String selectedAttribs
              = container.getMetaDisplayInterface().getTransformerProperty(
                                      XMLTransformer.SELECTED_ATTRIBS_XSLPROP);
          if(selectedAttribs == null) {
            resetTableSelection();
            return;
          }
          int selectedColIndex = -1;
          try {
            selectedColIndex = Integer.parseInt(selectedAttribs);

          } catch (NumberFormatException nfe) {
            Log.debug(12,"Can't handle multiple column selections yet!!");
            return;
          }
          dv.getDataTable().setColumnSelectionInterval( selectedColIndex -1,
                                                        selectedColIndex -1);

          Log.debug(50,"& & & & & & & & & & selectedAttribs="+selectedAttribs
                                   +";\n selectedColIndex = "+selectedColIndex);
        }
    }
  }

  public void resetTableSelection()
  {
    if (dv!=null && dv.getDataTable()!=null) {
      JTable table = dv.getDataTable();

      if (table.getRowCount()>0 && table.getColumnCount()>0) {
        table.setRowSelectionInterval(0,0);
        table.setColumnSelectionInterval(0,0);
        table.changeSelection(0, 0, true, false);
      }
    }
  }

  //convenience method to get the entity identifier for a given entity
  //title/name
  private String getEntityIDForThisEntityName(String entityName) {

      return listValueHash.get(entityName);
  }

  /**
   *  checks the dvArray for dataViewer that may have changed data
   *  and then saves the changed data
   */
  public void saveDataChanges() {
//    Log.debug(1,"Saving any data changes");
    if (dvArray!=null) {
      for (int i=0; i<dvArray.length; i++) {
        if (dvArray[i]!=null) {
          boolean changeFlag = dvArray[i].getDataChangedFlag();
          if (changeFlag) {
            dvArray[i].saveCurrentTable(false);
            AbstractDataPackage adp = mdp.getAbstractDataPackage();
            adp.setLocation("");
            int entityIndex = dvArray[i].getEntityIndex();
            adp.addDirtyEntityIndex(entityIndex);
            Log.debug(30, "The index of dirty entity is "+entityIndex+
           		 " and the file id is "+dvArray[i].getEntityFileId());
          }
          dvArray[i].setDataChangedFlag(false);
        }
      }  // end of loop over all dvs
    }
  }
  


  /**
   *  checks the dvArray for dataViewer and reload unsaved information
   */
  public void removeDataChanges() {
    MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    mdp = morphoFrame.getMorphoDataPackage(); 
    AbstractDataPackage adp = mdp.getAbstractDataPackage();

    AbstractDataPackage tempAdp = null;
    Entity[] entArray = null;
    try{
      MorphoDataPackage mdp = DataStoreServiceController.getInstance().read(adp.getAccessionNumber(), adp.getLocation());
      tempAdp = mdp.getAbstractDataPackage();
      entArray = tempAdp.getEntityArray();
    }catch(Exception e){
      Log.debug(20,"Unable to read the file in RevertCommand.java");
    }

    if (dvArray!=null) {
      for (int i=0; i<dvArray.length; i++) {
        if (dvArray[i]!=null) {
          int entityIndex = dvArray[i].getEntityIndex();
          if(tempAdp!=null && tempAdp.getAttributeCountForAnEntity(entityIndex)
              != adp.getAttributeCountForAnEntity(entityIndex)){
              // column has been added or deleted
              adp.deleteEntity(entityIndex);
              adp.insertEntity(entArray[entityIndex], entityIndex);
          }
          dvArray[i].init();
        }
      }
    }
  }

  /**
   * creates the data display and puts it into the center of the window
   * This needs to be dynamically done as tabs are selected due to potentially
   * large memory usage
   */
  private void setDataViewer(int index) {
    if (dvArray[lastTabSelected]!=null) {
      boolean fg = dvArray[lastTabSelected].getDataChangedFlag();
//      Log.debug(1,"Data change = "+ fg+ "--- indx: "+lastTabSelected);
    }
    if (dvArray[index]==null) {
      File displayFile = null;
      TabbedContainer comp =
          (TabbedContainer) tabbedEntitiesPanel.getComponentAt(lastTabSelected);
      JSplitPane entireDataPanel = comp.getSplitPane();
      JPanel currentDataPanelOld = (JPanel)entireDataPanel.getLeftComponent();
//      removePVObject();
      currentDataPanelOld.removeAll();
      lastTabSelected = index;
      String dataId = null;
      AbstractDataPackage adp = mdp.getAbstractDataPackage();
        if (adp==null) {
          Log.debug(1, "adp is null! No data package");
          return;
        }
        String inline = adp.getDistributionInlineData(index, 0,0);
           // assume the first set of physical and distribution data
        if (inline.length()>0) {  // there is inline data
          // there may be a problem here with putting inline data in a string
          // if the amount of inline data is vary large; assume for now that very large sets
          // of inline data has been removed before reaching here (needs to be done so that
          // DOM can be created!!!)
          // ********************
          // now check to see if the inline data is in base64 format; if not, assumed to be in
          // a text format
          String encMethod = adp.getEncodingMethod(index, 0);
          if ((encMethod.indexOf("Base64")>-1)||(encMethod.indexOf("base64")>-1)||
              (encMethod.indexOf("Base 64")>-1)||(encMethod.indexOf("base 64")>-1)) {
            // is Base64


            byte[] decodedData = Base64.decode(inline);
            ByteArrayInputStream bais = new ByteArrayInputStream(decodedData);
            displayFile = Morpho.thisStaticInstance.getLocalDataStoreService().saveTempDataFile(adp.getAccessionNumber(), bais);
          }
          else {
            // is assumed to be text
            InputStream inlineStream = new ByteArrayInputStream(inline.getBytes(Charset.forName("UTF-8")));
            displayFile = Morpho.thisStaticInstance.getLocalDataStoreService().saveTempDataFile(adp.getAccessionNumber(), inlineStream);
          }
        } else if (adp.getDistributionUrl(index, 0,0).length()>0) {
          // this is the case where there is a url link to the data
          String urlInfo = adp.getDistributionUrl(index, 0,0);
          String protocol = AbstractDataPackage.getUrlProtocol(urlInfo);
          if (protocol.startsWith(AbstractDataPackage.ECOGRID)) {
              dataId = AbstractDataPackage.getUrlInfo(urlInfo);
            if (dataId == null || dataId.trim().equals("")) return;
            
            try{
              String loc = adp.getLocation();
              if (loc.equals(DataPackageInterface.LOCAL) || loc.equals(DataPackageInterface.BOTH) || loc.equals(DataPackageInterface.NETWORK)) {
                displayFile = DataStoreServiceController.getInstance().openFile(dataId, loc);
              }
              else if (loc.equals("")) {
            	  // try getting local sources first, then network
            	  try {
            		  displayFile = Morpho.thisStaticInstance.getLocalDataStoreService().getDataFileFromAllLocalSources(dataId);
            	  } catch (Exception e) {
                      displayFile = DataStoreServiceController.getInstance().openFile(dataId, DataPackageInterface.NETWORK);
				} 
              }
            }
            catch (Exception q) {
              Log.debug(5,"Exception opening file!");
              q.printStackTrace();
            }
          }
          else if (protocol.equals("http:")) {
            Log.debug(20, "urlinfo: "+urlInfo);
          }
          else {
            Log.debug(20, "protocol: "+protocol);
          }
        }
        else if (adp.getDistributionArray(index, 0)==null) {
          // case where there is no distribution data in the package
//        Log.debug(1, "This entity has NO distribution information!");
//        JOptionPane.showMessageDialog(null,
//                  "This entity has NO distribution information!",
//                  "Information", JOptionPane.INFORMATION_MESSAGE );
        }
        dv = new DataViewer(morpho, "DataFile: ", null);  // file is null for now
        dv.setMorphoDataPackage(mdp);
        dv.setEntityIndex(index);

        dv.setDataFile(displayFile);
        dv.setDataFileId(dataId);
        //dv.setCurrentURLInfo(currentURLInfo);
        dv.init();
        dvArray[index] = dv;
    } else {
      lastTabSelected = index;
      dv = dvArray[index];
    }
      lastPV = dv.getPV();
      JPanel tablePanel = null;

      tablePanel = dv.DataViewerPanel;

      tablePanel.setOpaque(true);
      tablePanel.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
      TabbedContainer compn =
          (TabbedContainer) tabbedEntitiesPanel.getComponentAt(index);
      JSplitPane entireDataPane = compn.getSplitPane();
      JPanel currentDataPanel = (JPanel)entireDataPane.getLeftComponent();
      currentDataPanel.setLayout(new BorderLayout(0,0));
      currentDataPanel.add(BorderLayout.CENTER,tablePanel);
      currentDataPanel.setBackground(UISettings.NONEDITABLE_BACKGROUND_COLOR);
      //alert others to the state change
      StateChangeEvent selectionEvent = new StateChangeEvent(this, StateChangeEvent.SELECT_DATA_VIEWER);
      
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
        storingStateChangeEvent(selectionEvent);
      }//if
      else
      {
        //frame already has, borading the event
        dv.broadcastStoredStateChangeEvent();
        StateChangeMonitor.getInstance().notifyStateChange(selectionEvent);
     }
      
      

  }

  private JSplitPane createEntityPanel() {
    JPanel entityMetadataPanel = new JPanel();
    entityMetadataPanel.setLayout(new BorderLayout(0,0));
    JTabbedPane entityTabbedPane  = new JTabbedPane();
    entityTabbedPane.addTab("Metadata", entityMetadataPanel);
    dataViewPanel = new JPanel();
    dataViewPanel.setLayout(new BorderLayout(0,0));

    entityPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,dataViewPanel, entityTabbedPane);
    if (UIManager.getSystemLookAndFeelClassName().indexOf("WindowsLookAndFeel")>-1) {
      entityPanel.setUI(new javax.swing.plaf.metal.MetalSplitPaneUI());
    }
    entityPanel.setOneTouchExpandable(true);
    entityPanel.setDividerLocation(700);

    return entityPanel;
  }

  /**
   * Run the rendering in a background thread
   * @param selectedColIndex
   */
  private void runShowDataViewAndAttributePanel(int selectedColIndex) {
	  final int index = selectedColIndex;
	  Executors.newSingleThreadExecutor().execute(new Runnable() {
		public void run() {
			showDataViewAndAttributePanel(index);
		} 
	  });
  }
  
  /*  Method to create a attribute panel to replace entity */
  private void showDataViewAndAttributePanel(int selectedColIndex)
  {
    TabbedContainer container =
        (TabbedContainer) tabbedEntitiesPanel.getComponentAt(lastTabSelected);
    MetaDisplayInterface meta = container.getMetaDisplayInterface();
    // Get Uattribute file identifier
    String item = (String)entityItems.elementAt(lastTabSelected);
//    String id = (String)listValueHash.get(item);
    String id = getEntityIDForThisEntityName(item);
    String identifier = meta.getIdentifier();
    try
    {
      meta.useTransformerProperty(XMLTransformer.SELECTED_DISPLAY_XSLPROP,
                XMLTransformer.XSLVALU_DISPLAY_ATTRB);
      meta.useTransformerProperty(XMLTransformer.SELECTED_ATTRIBS_XSLPROP,
                                  String.valueOf(selectedColIndex + 1));
      // do not set the entity type/special index for displaying attributes
      //String entityType = adp.getEntityType(lastTabSelected);
      int indexForEntityType = lastTabSelected; //adp.getEntityIndexForType(lastTabSelected, entityType);
      meta.useTransformerProperty(XMLTransformer.SELECTED_ENTITY_XSLPROP,
              String.valueOf(indexForEntityType + 1));
      
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
  public Vector<StateChangeEvent> getStoredStateChangeEvent()
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
        StateChangeEvent event = storedStateChangeEventlist.elementAt(i);
        StateChangeMonitor.getInstance().notifyStateChange(event);
      }//for
    }//if
  }

 /**
  *  returns the MorphoDataPackage for this Panel
  */
 public MorphoDataPackage getMorphoDataPackage() {
    return mdp;
  }

 /**
  * get the last tab selected in the entities display
  */
 public int getLastTabSelected() {
   return lastTabSelected;
 }

 /**
  * get location of the AbstractDataPackage associated with this object
  */
 public String getPackageLocation() {
   AbstractDataPackage adp = getMorphoDataPackage().getAbstractDataPackage();
   return adp.getLocation();
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

    Log.debug(20, "editing complete: id: " + id + " location: " + location);
    MorphoFrame morphoFrame = UIController.getInstance().getCurrentActiveWindow();
    morphoFrame.setVisible(false);
    UIController uicontroller = UIController.getInstance();

    // turn the xml string into a dom root node
    try{
//  Log.debug(1,xmlString);
      StringReader sr = new StringReader(xmlString);
      Node nd = XMLUtilities.getXMLReaderAsDOMTreeRootNode(sr);
      AbstractDataPackage newadp = DataPackageFactory.getDataPackage(nd);
      newadp.setLocation("");  // we've changed it and not yet saved

      ServiceController services = ServiceController.getInstance();
      ServiceProvider provider = services.getServiceProvider(DataPackageInterface.class);
      DataPackageInterface dataPackageInt = (DataPackageInterface)provider;
      MorphoDataPackage mdp = new MorphoDataPackage();
      mdp.setAbstractDataPackage(newadp);
      dataPackageInt.openNewDataPackage(mdp, null);
      uicontroller.removeWindow(morphoFrame);
      morphoFrame.dispose();

    } catch (Exception e) {
        Log.debug(5, "Exception in converting edited XML to DOM!"+e.getMessage());
    }

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

