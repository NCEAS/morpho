/**
 *  '$RCSfile: ResultPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-26 21:12:01 $'
 * '$Revision: 1.52 $'
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

package edu.ucsb.nceas.morpho.query;

import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.datapackage.*;
import edu.ucsb.nceas.morpho.framework.ConfigXML;
import edu.ucsb.nceas.morpho.framework.DataPackageInterface;
import edu.ucsb.nceas.morpho.framework.MorphoFrame;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.framework.UIController;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceProvider;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JMenu;



/**
 * Display a ResultSet in a table view in a panel that can be
 * embedded in a window or tab or other location
 */
public class ResultPanel extends JPanel
{
  /** A reference to the ResultSet being displayed */
  private ResultSet results = null;
  /** A reference to the framework */
  private Morpho morpho = null;
  /** A reference to the mediator */
  private ResultPanelAndFrameMediator mediator = null;
  /** A reference to a dialog box if the panel will be set into a open dialog */
  private OpenDialogBox dialog = null;
  /** The table used to display the results */
  ToolTippedSortableJTable table = null;
  /** Button used to trigger a re-execution of the query */
  private JButton refreshButton;
  /** Button used to trigger a revision using QueryDialog */
  private JButton reviseButton;
  /** Button used to trigger a save of the current Query */
  private JButton saveButton;
  /** The label used to display the query title */
  private JLabel titleLabel;
  /** The label used to display the number of records */
  private JLabel recordCountLabel;
  /** Morpho butterfly label */
  private JLabel bflyLabel;
  /**popup menu for right clicks*/
  private JPopupMenu popup;
  /**menu items for the popup menu*/
  private JMenuItem openMenu = null;
  private JMenuItem openPreviousVersion = new JMenuItem("Open Previous Version");

  private JMenuItem uploadMenu = new JMenuItem("Upload to Metacat");
  private JMenuItem downloadMenu = new JMenuItem("Download from Metacat");
  private JMenuItem deleteLocalMenu = new JMenuItem("Delete Local Copy");
  private JMenuItem deleteMetacatMenu = new JMenuItem("Delete Metacat Copy");
  private JMenuItem deleteAllMenu = new JMenuItem("Delete Both Copies");
  private JMenuItem refreshMenu = new JMenuItem("Refresh");
  private JMenuItem exportMenu = new JMenuItem("Export...");
  private JMenuItem exportToZipMenu = new JMenuItem("Export to ZIP...");
  /**a string to keep track of the selected row's id*/
  private String selectedId = "";
  /**the location of the data package*/
  boolean metacatLoc = false;
  boolean localLoc = false;
  private Dimension preferredSize;
  
  ImageIcon bfly;
  ImageIcon flapping;
  int threadCount = 0;
  int selectedRow = -1;
  
  int vers;
  String packageName = "";
  
 // DynamicMenuAction dynamicmenuhandler;
  
  /**
   * Construct a new ResultPanel and display the result set.  By default
   * the panel has reset and refresh buttons.
   *
   * @param dialog the dialog the resultpanel will be set to. If it is null, 
   *               the result panel would be set to a dialog
   * @param results the result listing to display
   * @param myMediator the mediaor passed from frame to control table
   */
  public ResultPanel(OpenDialogBox dialog, ResultSet results, 
                                        ResultPanelAndFrameMediator myMediator)
  {
    this(dialog, results, 12, myMediator, new Dimension(775,500));
  }

  /**
   * Construct a new ResultPanel and display the result set.  By default
   * the panel has reset and refresh buttons.
   *
   * @param dialog the dialog the resultpanel will be set to. If it is null, 
   *               the result panel would be set to a dialog
   * @param results the result listing to display
   * @param myMediator the mediaor passed from frame to control table
   * @param preferredSize the specific size of the panel
   */
  public ResultPanel(OpenDialogBox dialog, ResultSet results, 
              ResultPanelAndFrameMediator myMediator, Dimension preferredSize)
  {
    this(dialog, results, 12, myMediator, preferredSize);
  }


  /**
   * Construct a new ResultPanel and display the result set
   *
   * @param dialog the dialog the resultpanel will be set to. If it is null, 
   *               the result panel would be set to a dialog
   * @param results the result listing to display
   * @param fontSize the fontsize for the cells of the table
   * @param myMediator the mediaor passed from frame to control table
   */
  public ResultPanel(OpenDialogBox dialog, ResultSet results, int fontSize, 
            ResultPanelAndFrameMediator myMediator, Dimension preferredSize)
  {
    super();
    this.dialog = dialog;
    this.results = results;
    //this.hasRefreshButton = showRefresh;
    //this.hasReviseButton = showRevise;
    this.morpho = results.getFramework();
    this.mediator = myMediator;
    this.preferredSize = preferredSize;
    // If the panel don't need a mediator, null will be passed here
    if (mediator != null)
    {
      // Register result panel to mediator
      mediator.registerResultPanel(this);
    }

    try {
        bfly = new javax.swing.ImageIcon(getClass().getResource("Btfly.gif"));
        flapping = new javax.swing.ImageIcon(getClass().getResource("Btfly4.gif"));
    } catch (Exception w) {
        System.out.println("Error in loading images");
    }


    setLayout(new BorderLayout());
    setBackground(Color.white);
    //MBJ setPreferredSize(new Dimension(775,500));
    setPreferredSize(preferredSize);

    if (results != null) {
      setName(results.getQuery().getQueryTitle());


 
      // Set up the results table
      table = new ToolTippedSortableJTable(results);
           
      // Set resize model
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      // Set horizontal line off
      table.setShowHorizontalLines(false);
      table.setShowVerticalLines(false);
      
      ToolTippedTextRenderer stringRenderer = 
                                       new ToolTippedTextRenderer(fontSize);
     
      //stringRenderer.setRows(5);
      //table.setRowHeight((int)(stringRenderer.getPreferredSize().height));
      table.setRowHeight(results.getRowHeight());
      table.setDefaultRenderer(String.class, stringRenderer);
      table.setDefaultRenderer
                            (javax.swing.ImageIcon.class, new ImageRenderer());
     
      // Create the scroll pane and add the table to it. 
      JScrollPane scrollPane = new JScrollPane(table);
      
      // Set JScrollPane background color white
      scrollPane.getViewport().setBackground(Color.white);
      // Initialize column, pass the width of virwport to the table
      //initTableColumnSize(table, results, 775);
      initTableColumnSize(table, results, (int)preferredSize.getWidth());
      // Add the table to scroll pane
      //scrollPane.add(table);
  
      //Add the scroll pane to this Panel.
      add(scrollPane, BorderLayout.CENTER);
    
    
    
      //Build the popup menu for the right click functionality
      popup = new JPopupMenu();
      // Create a openPackage action
      GUIAction openAction = new GUIAction("Open Package", null,
                            new OpenPackageCommand(this));
      openMenu = new JMenuItem(openAction);
      popup.add(openMenu);
      
      openPreviousVersion.setEnabled(false);
      popup.add(openPreviousVersion);
      
      popup.add(refreshMenu);
      popup.add(new JSeparator());
      popup.add(uploadMenu);
      popup.add(downloadMenu);
      popup.add(new JSeparator());
      popup.add(deleteLocalMenu);
      popup.add(deleteMetacatMenu);
      popup.add(deleteAllMenu);
      popup.add(new JSeparator());
      popup.add(exportMenu);
      popup.add(exportToZipMenu);
      
      MenuAction menuhandler = new MenuAction();
      //openMenu.addActionListener(menuhandler);
      uploadMenu.addActionListener(menuhandler);
      downloadMenu.addActionListener(menuhandler);
      deleteLocalMenu.addActionListener(menuhandler);
      deleteMetacatMenu.addActionListener(menuhandler);
      deleteAllMenu.addActionListener(menuhandler);
      refreshMenu.addActionListener(menuhandler);
      exportMenu.addActionListener(menuhandler);
      exportToZipMenu.addActionListener(menuhandler);
      openPreviousVersion.addActionListener(menuhandler);
      
//      dynamicmenuhandler = new DynamicMenuAction();
      
      MouseListener popupListener = new PopupListener();
      table.addMouseListener(popupListener);
      
      // Listen for mouse events to see if the user double-clicks
      table.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          if (2 == e.getClickCount()) 
          {
             //doOpenDataPackage();
             // Using OpenPackageCommand to open package
             doDoubleClickOpen();
          }
        }
      });
    }
  }
  /**
   * Double click to open a package
   */
  private void doDoubleClickOpen()
  { 
    // Create a open pakcag command
    OpenPackageCommand open = new OpenPackageCommand(this);
             open.execute();
  }// doDoubleClickOpen

  /**
   * Get the result set from ResultPanel
   */
  public ResultSet getResultSet()
  {
    return results;
  }//getResultSet
  
  /** 
   * Get the Jable
   */
  public JTable getJTable()
  {
    return table;
  }//getJTable
  
  /** 
   * Get the dialog from ResultPanle
   */
  public OpenDialogBox getDialog()
  {
    return dialog;
  }//getDialog
  
  /**
   * Get the selectedId
   */
  public String getSelectedId()
  {
    return selectedId;
  }
  
  /**
   * Get the metacatLoc
   */
  public boolean getMetacatLocation()
  {
    return metacatLoc;
  }
  
  /**
   * Get the local location
   */
  public boolean getLocalLocation()
  {
    return localLoc;
  }
  
  
  
  /*
   * This method picks column sizes depend on the length of talbe and the 
   * value for every column in an array.
   */
  private void initTableColumnSize(JTable table, ResultSet results, int width) 
  {
    // width for the each column (by percentage), change the value in the
    // array, the column width will be changed
    // Add the all element in the array together you will get 1 (100%100)
    // The value is 30/768, 282/768, 96/768, 72/768, 120/768, 90/768, 42/768, 
    // 36/768
    /*double [] columnWidth = {0.0390625, 0.3671875, 0.125, 0.09375, 0.15625, 
                            0.1171875, 0.0546875, 0.046875};*/
     double [] columnWidth = {0.03, 0.355, 0.132, 0.1, 0.14, 
                             0.13, 0.066, 0.047};
    // column object
    TableColumn column = null;
    // Minimum factor for MinWidth
    double minFactor = 0.7;
    // Maxmum factor for MaxWidth
    int maxFactor = 5;
    // Percentage width
    double percentage = 0.0;
    // Perferred size of column
    int preferredSize = 0;
    // Minimum size
    int minimumSize = 0;
    // Maxmum size 
    int maxmumSize = 0;
    
    
    for (int i = 0; i < results.getColumnCount(); i++) 
    {
      // Get the column
      column = table.getColumnModel().getColumn(i);
      // Get the percentage of width for this column from the array
      percentage = columnWidth[i];
      // Get the width as preferred width
      preferredSize = (new Double(width*percentage)).intValue();
     
      // Get the minimum size
      minimumSize =(new Double(preferredSize*minFactor)).intValue();
      // Get the maxmum size
      maxmumSize = preferredSize*maxFactor;
       // In order to keep title at least show 6 words, we need to make sure
      // it is bigger than 200
      if ( i== 1 && preferredSize <200 )
      {
        // Set preferred size is 200
        preferredSize = 200;
        // Get the minimum size
        minimumSize=( new Double(preferredSize*minFactor)).intValue();
        // Get the maxmum size
        maxmumSize = preferredSize*maxFactor;
    
      }//if
      // Set preferred width
      column.setPreferredWidth(preferredSize);
      // Set minimum width
      column.setMinWidth(minimumSize);
      // Set maxmum width
      column.setMaxWidth(maxmumSize);
    }//for
  }// initTableColumnSize 


  /*
   * This method picks good column sizes.
   * If all column heads are wider than the column's cells' 
   * contents, then you can just use column.sizeWidthToFit().
   */
  private void initColumnSizes(JTable table, ResultSet results) {
    TableColumn column = null;
    Component comp = null;
    Component hcomp = null;
    int headerWidth = 0;
    int cellWidth = 0;
    Object[] longValues = null;;

    for (int i = 0; i < results.getColumnCount(); i++) {
      column = table.getColumnModel().getColumn(i);
      hcomp = (Component)column.getHeaderRenderer();
      if (hcomp != null) {
        headerWidth = hcomp.getPreferredSize().width;
      }

      comp = table.getDefaultRenderer(results.getColumnClass(i)).
                 getTableCellRendererComponent(
                 table, results.getValueAt(1, i),
                 false, false, 0, i);
      cellWidth = comp.getPreferredSize().width;
      column.setPreferredWidth(Math.max(headerWidth, cellWidth));
      if (cellWidth < 100 && i > 0) {
        column.setMinWidth(cellWidth+100);
      } else if (0 == i) {
        // Special rule to size the icon column correctly
        column.setMinWidth(cellWidth+20);
      }
    }
  } 

  /**
   * Open a result record when the user double-clicks on the table.  If
   * multiple rows are selected, open them all.
   */
  private void openResultRecord(JTable table) {
   
    int[] selectedRows = table.getSelectedRows();

    for (int i = 0; i < selectedRows.length; i++) {
     
      results.openResultRecord(selectedRows[i]);
    }
  }

  /**
   * Handle actions from the panel's components
   */
  private class ActionHandler implements ActionListener
  {
    public void actionPerformed(ActionEvent event)
    {
      Object object = event.getSource();
      if (object == reviseButton) {
        reviseQuery();
      } else if (object == refreshButton) {
//        refreshQuery();
        doRefreshQuery();
      } else if (object == saveButton) {
        saveQuery();
      }
    }
  }
  
  /**
   * exports the datapackage to a different location
   * @param id the id of the datapackage to export
   */
  private void exportDataset(String id)
  {
    String curdir = System.getProperty("user.dir");
    JFileChooser filechooser = new JFileChooser(curdir);
//    filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    filechooser.setDialogTitle("Export Datapackage to Selected Directory");
    filechooser.setApproveButtonText("Export");
    filechooser.setApproveButtonMnemonic('E');
    filechooser.setApproveButtonToolTipText("Choose a directory to export " +
                                            "this Datapackage to.");
    String msg = "ALERT: Please select a DIRECTORY, not a file in the File Dialog which will appear next!";
 
    File exportDir;
    int result = filechooser.showSaveDialog(this);
    
    exportDir = filechooser.getCurrentDirectory();
    if (result==JFileChooser.APPROVE_OPTION) {
      //now we know where to export the files to, so export them.
      DataPackageInterface dataPackage;
      try 
      {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider = 
                   services.getServiceProvider(DataPackageInterface.class);
        dataPackage = (DataPackageInterface)provider;
      } 
      catch (ServiceNotHandledException snhe) 
      {
        Log.debug(6, snhe.getMessage());
        return;
      }
    
      String location = "";
      //figure out where this thing is.
      if(metacatLoc && localLoc)
      {
        location = DataPackage.BOTH;
      }
      else if(metacatLoc && !localLoc)
      {
        location = DataPackage.METACAT;
      }
      else if(!metacatLoc && localLoc)
      {
        location = DataPackage.LOCAL;
      }
    
      //export it.
      dataPackage.export(selectedId, exportDir.toString(), location);
    }
  }
  
  /**
   * exports the datapackage to a different location in a zip file
   * @param id the id of the datapackage to export
   */
  private void exportDatasetToZip(String id)
  {
    String curdir = System.getProperty("user.dir");
    curdir = curdir + System.getProperty("file.separator") + id + ".zip";
    File zipFile = new File(curdir);
    JFileChooser filechooser = new JFileChooser(curdir);
    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    filechooser.setDialogTitle("Export Datapackage to Selected Zip File");
    filechooser.setApproveButtonText("Export");
    filechooser.setApproveButtonMnemonic('E');
    filechooser.setApproveButtonToolTipText("Choose a file to export " +
                                            "this Datapackage to.");
    filechooser.setSelectedFile(zipFile);                                        
    //filechooser.updateUI();
    File exportDir;
    int result = filechooser.showSaveDialog(this);
    exportDir = filechooser.getSelectedFile();
    if (result==JFileChooser.APPROVE_OPTION) {
      //now we know where to export the files to, so export them.
      DataPackageInterface dataPackage;
      try 
      {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider = 
                   services.getServiceProvider(DataPackageInterface.class);
        dataPackage = (DataPackageInterface)provider;
      } 
      catch (ServiceNotHandledException snhe) 
      {
        Log.debug(6, snhe.getMessage());
        return;
      }
    
      String location = "";
      //figure out where this thing is.
      if(metacatLoc && localLoc)
      {
        location = DataPackage.BOTH;
      }
      else if(metacatLoc && !localLoc)
      {
        location = DataPackage.METACAT;
      }
      else if(!metacatLoc && localLoc)
      {
        location = DataPackage.LOCAL;
      }
    
      //export it.
      dataPackage.exportToZip(selectedId, exportDir.toString(), location);
    }
  }
  
  /**
   * Event handler for the right click popup menu
   */
  class MenuAction implements java.awt.event.ActionListener 
  {
    
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			
      Object object = event.getSource();
      String docid = selectedId;
      DataPackageInterface dataPackage;
      try 
      {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
        dataPackage = (DataPackageInterface)provider;
      } 
      catch (ServiceNotHandledException snhe) 
      {
        Log.debug(6, snhe.getMessage());
        return;
      }
			if (object == openMenu)
      { 
        
        doOpenDataPackage();
        //open the current selection in the package editor
      }
      else if (object == openPreviousVersion)
      {
        if (vers>0) {
          OpenPreviousDialog opd = new OpenPreviousDialog(packageName,vers,morpho,localLoc );
          opd.setVisible(true);
        }
      }
			else if (object == uploadMenu)
      { 
        doUpload();
      }
			else if (object == downloadMenu)
      { 
        doDownload();
        
      }
			else if (object == deleteLocalMenu)
		  { 
		    doDeleteLocal();
      }
			else if (object == deleteMetacatMenu)
			{ 
			  doDeleteMetacat();
			  
      }
			else if (object == deleteAllMenu)
			{ 
			  doDeleteAll();
      }
      else if (object == exportMenu)
      {
        doExport();
        
 /*       Log.debug(20, "Exporting dataset");
        exportDataset(docid);
 */
      }
      else if (object == exportToZipMenu)
      {
        doExportToZip();
      }
//      refreshQuery();
      getParent().invalidate();
      getParent().repaint();
		}
  }

  /**
   * Event handler for the right click popup menu dynamic menus
   * i.e. those for previous version of a datapackage
   */
  class DynamicMenuAction implements java.awt.event.ActionListener 
  {
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			JMenuItem jmi = (JMenuItem)object;
      Log.debug(30, "Event String:"+jmi.getText());

      DataPackageInterface dataPackage;
      try 
      {
        ServiceController services = ServiceController.getInstance();
        ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
        dataPackage = (DataPackageInterface)provider;
      } 
      catch (ServiceNotHandledException snhe) 
      {
        Log.debug(6, snhe.getMessage());
        return;
      }
      String location = "";
      if (localLoc) location = "local";
      else {
        location = "metacat";
      }
      dataPackage.openDataPackage(location, jmi.getText(), null);

      getParent().invalidate();
      getParent().repaint();
		}
  }
  
  
  class PopupListener extends MouseAdapter {
    // on the Mac, popups are triggered on mouse pressed, while mouseReleased triggers them
    // on the PC; use the trigger flag to record a trigger, but do not show popup until the
    // mouse released event (DFH)
    boolean trigger = false;
    String docid = "";
    
    public void mousePressed(MouseEvent e) 
    {
      //select the clicked row first
      table.clearSelection();
      int selrow = table.rowAtPoint(new Point(e.getX(), e.getY()));
      selectedRow = selrow;
      table.setRowSelectionInterval(selrow, selrow);
      Vector resultV = results.getResultsVector();
      Vector rowV = (Vector)resultV.elementAt(selrow);
      
      // If the panel need a mediator
      if (mediator != null)
      {
        // If select a row, open button will enable
        mediator.enableOpenButton();
      }
    
      /*//System.out.println("resultsV: " + resultV.toString());
      for(int i=0; i<resultV.size(); i++)
      {
        Vector v = (Vector)resultV.elementAt(i);
        //System.out.println(i + "\n--------------------------");
        for(int j=0; j<v.size(); j++)
        {
          //System.out.print(j + ": " + v.elementAt(j).toString() + " ");
        }
        //System.out.println();
      }
      System.out.println("\nrow: " + selrow + " rowV: " + rowV.toString());*/
      docid = (String)rowV.elementAt(6);
      selectedId = docid;
      localLoc = ((Boolean)rowV.elementAt(9)).booleanValue();
      metacatLoc = ((Boolean)rowV.elementAt(10)).booleanValue();
      Log.debug(30, "selectedId is: "+docid);
      if (e.isPopupTrigger()) 
      {
        trigger = true;
      }  
    }

    public void mouseReleased(MouseEvent e) 
    {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) 
    {
      if(e.isPopupTrigger() || trigger) 
      {     
        vers = getNumberOfPrevVersions();
        packageName = getIdWithoutVersion();
        if (vers>0) {
          openPreviousVersion.setEnabled(true);
        }
        else {
          openPreviousVersion.setEnabled(false);
        }
        
        uploadMenu.setEnabled(localLoc && !metacatLoc);
        downloadMenu.setEnabled(metacatLoc && !localLoc);
        deleteLocalMenu.setEnabled(localLoc);
        deleteMetacatMenu.setEnabled(metacatLoc);
        deleteAllMenu.setEnabled(metacatLoc && localLoc);
        
	      trigger = false;
        popup.show(e.getComponent(), e.getX(), e.getY());
        
      }
    }
    
    private int getNumberOfPrevVersions() {
      int prevVersions = 0;
      int iii = docid.lastIndexOf(".");
      String ver = docid.substring(iii+1,docid.length());
      prevVersions = (new Integer(ver)).intValue();
      prevVersions = prevVersions - 1;
      return prevVersions;
    }
    
    private String getIdWithoutVersion() {
      int prevVersions = 0;
      int iii = docid.lastIndexOf(".");
      String ver = docid.substring(0,iii);
      return ver;
    }
    
  }	

  /**
   * Revise the query by loading it into the QueryDialog GUI
   */
  public void reviseQuery()
  {
  
  } 

  /**
   * Refresh the results by running the query again to produce a new ResulSet
   */
  public void refreshQuery()
  {
    Query query = results.getQuery();
    ResultSet newResults = query.execute();
    setResults(newResults);
  } 

  /**
   * Save a query in the user's profile so they can run it again later. It
   * will show up in the "Search" menu sp they can execute it.
   */
  public void saveQuery()
  {
   
  }

  /**
   * Set the ResultSet (usually as a result of refreshing or revising a Query)
   */
  public void setResults(ResultSet newResults) 
  {
    this.results = newResults;

    // Notify the frame of any title changes
    String newTitle = results.getQuery().getQueryTitle();
    Container parent = getRootPane().getParent();
    if (parent instanceof MorphoFrame) {
      MorphoFrame rsf = (MorphoFrame)parent;
      rsf.setTitle(newTitle);
    }
 
    // Notify the JTable that the TableModel changed a bunch!
    table.setModel(results);
    //initTableColumnSize(table, results, 775);
    initTableColumnSize(table, results, (int)preferredSize.getWidth());
  }



  /**
   * Add a new menu item to the Search menu for the query
   *
   * @param query the query to be added to the Search menu
   */
  private void addQueryToMenu(final Query query)
  {
 
  }
  

  


private void doUpload() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          //bflyLabel.setIcon(flapping);
          threadCount++;
          
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            Log.debug(6, "Error in upload");
            return null;
          }
          
          { //upload the current selection to metacat
            Log.debug(20, "Uploading package.");
            try
            {
              dataPackage.upload(docid, false);
              refreshQuery();
            }
            catch(MetacatUploadException mue)
            {
              //ask the user if he is sure he wants to overwrite the package
              //if he is do it, otherwise return
              String message = "A conflict has been found in one or more of the " +
                "identifiers \nin your package.  It is possible that you or \n" + 
                "someone else has made a change on the server that has not \n" +
                "been reflected on your local copy. If you proceed, you may \n" +
                "overwrite package information.  If you proceed the identifier \n"+
                "for this package will be changed.  Are you sure you want to \n" +
                "proceed with the upload?";
              int choice = JOptionPane.YES_OPTION;
              choice = JOptionPane.showConfirmDialog(null, message, 
                                 "Morpho", 
                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                 JOptionPane.WARNING_MESSAGE);
              if(choice == JOptionPane.YES_OPTION)
              {
                try
                {
                  dataPackage.upload(docid, true);
                  refreshQuery();
                }
                catch(MetacatUploadException mue2)
                {
                  Log.debug(0, mue2.getMessage());
                }
              }
              else
              {
                return null;
              }
            }
          }
          
        return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           //recordCountLabel.setText(results.getRowCount() + " data packages");    
           //bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3

  
}
 
  
private void doDownload() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          //bflyLabel.setIcon(flapping);
          threadCount++;
 
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            Log.debug(6, "Error in upload");
            return null;
          }
          
          
        //download the current selection to the local disk
        Log.debug(20, "Downloading package.");
        dataPackage.download(docid);
        refreshQuery();
          
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           //recordCountLabel.setText(results.getRowCount() + " data packages");    
           //bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}

private void doDeleteLocal() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          //bflyLabel.setIcon(flapping);
          threadCount++;
          
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            Log.debug(6, "Error in upload");
            return null;
          }
          
          
		    //delete the local package
        Log.debug(20, "Deleteing the local package.");
        String message = "Are you sure you want to delete \nthe package from " +
                         "your local file system?";
        int choice = JOptionPane.YES_OPTION;
        choice = JOptionPane.showConfirmDialog(null, message, 
                               "Morpho", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
        if(choice == JOptionPane.YES_OPTION)
        {
          dataPackage.delete(docid, DataPackage.LOCAL);
          refreshQuery();
        }
          
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           //recordCountLabel.setText(results.getRowCount() + " data packages");    
           //bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}

private void doDeleteMetacat() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          //bflyLabel.setIcon(flapping);
          threadCount++;
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            Log.debug(6, "Error in upload");
            return null;
          }
          
          
			   //delete the object on metacat
         Log.debug(20, "Deleteing the metacat package.");
         String message = "Are you sure you want to delete \nthe package from " +
                         "Metacat? You \nwill not be able to upload \nit " +
                         "again with the same identifier.";
         int choice = JOptionPane.YES_OPTION;
         choice = JOptionPane.showConfirmDialog(null, message, 
                               "Morpho", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
         if(choice == JOptionPane.YES_OPTION)
         {
           dataPackage.delete(docid, DataPackage.METACAT);
           refreshQuery();
          
         }
          
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           //recordCountLabel.setText(results.getRowCount() + " data packages");    
           //bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}
  
private void doDeleteAll() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          //bflyLabel.setIcon(flapping);
          threadCount++;
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            Log.debug(6, "Error in upload");
            return null;
          }
          
			  //delete both of the objects
        Log.debug(20, "Deleting both copies of the package.");
        String message = "Are you sure you want to delete \nthe package from " +
                         "Metacat and your \nlocal file system? " +
                         "Deleting a package\n cannot be undone!";
        int choice = JOptionPane.YES_OPTION;
        choice = JOptionPane.showConfirmDialog(null, message, 
                               "Morpho", 
                               JOptionPane.YES_NO_CANCEL_OPTION,
                               JOptionPane.WARNING_MESSAGE);
        if(choice == JOptionPane.YES_OPTION)
        {
          dataPackage.delete(docid, DataPackage.BOTH);
          refreshQuery();
        }
          
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           //recordCountLabel.setText(results.getRowCount() + " data packages");    
           //bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}

private void doExport() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          //bflyLabel.setIcon(flapping);
          threadCount++;
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            Log.debug(6, "Error in upload");
            return null;
          }
          
			  //do export
          Log.debug(20, "Exporting dataset");
          exportDataset(docid);

          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           //recordCountLabel.setText(results.getRowCount() + " data packages");    
           //bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}

private void doExportToZip() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          //bflyLabel.setIcon(flapping);
          threadCount++;
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            Log.debug(6, "Error in upload");
            return null;
          }
          
			  //do exportToZip
          Log.debug(20, "Exporting dataset to zip file");
          exportDatasetToZip(docid);

          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           //recordCountLabel.setText(results.getRowCount() + " data packages");    
           //bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}

  public void doOpenDataPackage() {
   
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          //bflyLabel.setIcon(flapping);
          threadCount++;
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceController services = ServiceController.getInstance();
            ServiceProvider provider = 
                     services.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            Log.debug(6, "Error in doOpenDataPackage");
            return null;
          }
          
			  //do open
          Log.debug(20, "Opening package!.");
          openResultRecord(table);
        
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           //recordCountLabel.setText(results.getRowCount() + " data packages");    
           //bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}
 
  
private void doRefreshQuery() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          //bflyLabel.setIcon(flapping);
          threadCount++;
          refreshQuery();
        
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           //recordCountLabel.setText(results.getRowCount() + " data packages");    
           //bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}
  
  
}
