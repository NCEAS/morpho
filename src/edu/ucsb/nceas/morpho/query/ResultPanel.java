/**
 *  '$RCSfile: ResultPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2002-08-06 21:10:39 $'
 * '$Revision: 1.41 $'
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

import edu.ucsb.nceas.morpho.framework.*;
import edu.ucsb.nceas.morpho.datastore.MetacatUploadException;
import edu.ucsb.nceas.morpho.datapackage.*;

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
  private ClientFramework framework = null;
  /** The table used to display the results */
  JTable table = null;
  /** Indicate whether the refresh button should appear */
  private boolean hasRefreshButton = false;
  /** Indicate whether the revise button should appear */
  private boolean hasReviseButton = false;
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
  /** A static hash listing all of the search menu query Actions by id */
  private static Hashtable savedQueriesList;
  /**popup menu for right clicks*/
  private JPopupMenu popup;
  /**menu items for the popup menu*/
  private JMenuItem openMenu = new JMenuItem("Open");
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
   * @param results the result listing to display
   */
  public ResultPanel(ResultSet results)
  {
    this(results, true, true);
  }

  /**
   * Construct a new ResultPanel and display the result set
   *
   * @param results the result listing to display
   * @param showRefresh boolean true if the Refresh button should appear
   * @param showRevise boolean true if the Revise button should appear
   */
  public ResultPanel(ResultSet results, boolean showRefresh, 
                     boolean showRevise)
  {
    this(results, showRefresh, showRevise, 12);
  }

  /**
   * Construct a new ResultPanel and display the result set
   *
   * @param results the result listing to display
   * @param showRefresh boolean true if the Refresh button should appear
   * @param showRevise boolean true if the Revise button should appear
   * @param fontSize the fontsize for the cells of the table
   */
  public ResultPanel(ResultSet results, boolean showRefresh,
                     boolean showRevise, int fontSize)
  {
    super();
    this.results = results;
    this.hasRefreshButton = showRefresh;
    this.hasReviseButton = showRevise;
    this.framework = results.getFramework();

    try {
        bfly = new javax.swing.ImageIcon(getClass().getResource("Btfly.gif"));
        flapping = new javax.swing.ImageIcon(getClass().getResource("Btfly4.gif"));
    } catch (Exception w) {
        System.out.println("Error in loading images");
    }


    setLayout(new BorderLayout());
    setBackground(Color.white);
    setPreferredSize(new Dimension(775,500));

    if (results != null) {
      setName(results.getQuery().getQueryTitle());

      // Set up the Header panel with a title and refresh/revise buttons
      titleLabel = new JLabel(results.getQuery().getQueryTitle());
      titleLabel.setForeground(Color.black);
      titleLabel.setFont(new Font("Dialog", Font.BOLD, 18));
      Box headerBox = Box.createHorizontalBox();
      headerBox.setBackground(Color.white);
      headerBox.add(Box.createHorizontalStrut(8));
      headerBox.add(titleLabel);
      headerBox.add(Box.createHorizontalGlue());
      recordCountLabel = new JLabel(results.getRowCount() + " data packages");
      recordCountLabel.setForeground(Color.black);
      recordCountLabel.setFont(new Font("Dialog", Font.BOLD, 18));
      bflyLabel = new JLabel(bfly);
      headerBox.add(recordCountLabel);
      headerBox.add(Box.createHorizontalStrut(4));
      refreshButton = new JButton("Refresh", new ImageIcon( getClass().
          getResource("/toolbarButtonGraphics/general/Refresh16.gif")));
      refreshButton.setText(null);
      refreshButton.setToolTipText("Refresh");
      if (hasRefreshButton) {
        headerBox.add(refreshButton);
        headerBox.add(Box.createHorizontalStrut(4));
      }
      reviseButton = new JButton("Revise", new ImageIcon( getClass().
          getResource("/toolbarButtonGraphics/general/Search16.gif")));
      reviseButton.setText(null);
      reviseButton.setToolTipText("Revise search");
      if (hasReviseButton) {
        headerBox.add(reviseButton);
        headerBox.add(Box.createHorizontalStrut(4));
      }
      saveButton = new JButton("Save search", new ImageIcon( getClass().
          getResource("/toolbarButtonGraphics/general/Save16.gif")));
      saveButton.setText(null);
      saveButton.setToolTipText("Save search");
      if (hasReviseButton) {
        headerBox.add(saveButton);
        headerBox.add(Box.createHorizontalStrut(4));
      }
      headerBox.add(bflyLabel);
      ActionHandler dispatcher = new ActionHandler();
      refreshButton.addActionListener(dispatcher);
      reviseButton.addActionListener(dispatcher);
      saveButton.addActionListener(dispatcher);
 
      JPanel headerPanel = new JPanel();
      headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
      headerPanel.setBackground(Color.white);
      headerPanel.add(Box.createVerticalStrut(4));
      headerPanel.add(headerBox);
      headerPanel.add(Box.createVerticalStrut(4));
      Border headerBorder = BorderFactory.createLineBorder(Color.black);
      headerPanel.setBorder(headerBorder);
      add(headerPanel, BorderLayout.NORTH);
 
      // Set up the results table
      table = new JTable(results);
      WrappedTextRenderer stringRenderer = new WrappedTextRenderer(fontSize);
      stringRenderer.setRows(5);
//DFH      table.setRowHeight((int)(stringRenderer.getPreferredSize().getHeight()));
      table.setRowHeight((int)(stringRenderer.getPreferredSize().height));
      //table.setRowHeight(results.getRowHeight());
      table.setDefaultRenderer(String.class, stringRenderer);
      initColumnSizes(table, results);
  
      //Create the scroll pane and add the table to it. 
      JScrollPane scrollPane = new JScrollPane(table);
  
      //Add the scroll pane to this Panel.
      add(scrollPane, BorderLayout.CENTER);
    
    
    
      //Build the popup menu for the right click functionality
      popup = new JPopupMenu();
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
      openMenu.addActionListener(menuhandler);
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
 //           openResultRecord(table);
            doOpenDataPackage();
          }
        }
      });
    }
  }

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
      System.out.println("row: " + selectedRows[i]);
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
        ServiceProvider provider = 
                   framework.getServiceProvider(DataPackageInterface.class);
        dataPackage = (DataPackageInterface)provider;
      } 
      catch (ServiceNotHandledException snhe) 
      {
        framework.debug(6, snhe.getMessage());
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
        ServiceProvider provider = 
                   framework.getServiceProvider(DataPackageInterface.class);
        dataPackage = (DataPackageInterface)provider;
      } 
      catch (ServiceNotHandledException snhe) 
      {
        framework.debug(6, snhe.getMessage());
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
        ServiceProvider provider = 
                     framework.getServiceProvider(DataPackageInterface.class);
        dataPackage = (DataPackageInterface)provider;
      } 
      catch (ServiceNotHandledException snhe) 
      {
        framework.debug(6, snhe.getMessage());
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
          OpenPreviousDialog opd = new OpenPreviousDialog(packageName,vers,framework,localLoc );
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
        
 /*       ClientFramework.debug(20, "Exporting dataset");
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
      ClientFramework.debug(30, "Event String:"+jmi.getText());

      DataPackageInterface dataPackage;
      try 
      {
        ServiceProvider provider = 
                     framework.getServiceProvider(DataPackageInterface.class);
        dataPackage = (DataPackageInterface)provider;
      } 
      catch (ServiceNotHandledException snhe) 
      {
        framework.debug(6, snhe.getMessage());
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
      ClientFramework.debug(30, "selectedId is: "+docid);
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
    // Save the original identifier
    String identifier = results.getQuery().getIdentifier();

    // QueryDialog Create and show as modal
    ResultFrame rsf = null;
    QueryDialog queryDialog1 = null;
    Container parent = getRootPane().getParent();
    if (parent instanceof ResultFrame) {
      rsf = (ResultFrame)parent;
      queryDialog1 = new QueryDialog(rsf, framework);
    } else {
      queryDialog1 = new QueryDialog(framework);
    }
    queryDialog1.setQuery(results.getQuery());
    queryDialog1.setModal(true);
    queryDialog1.show();
    if (queryDialog1.isSearchStarted()) {
      Query query = queryDialog1.getQuery();
      if (query != null) {
        query.setIdentifier(identifier);
        ResultSet newResults = query.execute();
        setResults(newResults);
      }
    }
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
    // Serialize the query in the profiles directory
    AccessionNumber a = new AccessionNumber(framework);
    Query query = results.getQuery();
    String identifier = query.getIdentifier();
    if (identifier == null) {
      identifier = a.getNextId();
      query.setIdentifier(identifier);
    }

    try {
      ClientFramework.debug(10, "Saving query to disk...");
      query.save();
      ClientFramework.debug(10, "Adding query to menu...");
      addQueryToMenu(query);

    } catch (IOException ioe) {
      ClientFramework.debug(6, "Failed to save query: I/O error.");
    }
  }

  /**
   * Set the ResultSet (usually as a result of refreshing or revising a Query)
   */
  public void setResults(ResultSet newResults) 
  {
    this.results = newResults;

    // Notify the frame of any title changes
    String newTitle = results.getQuery().getQueryTitle();
    titleLabel.setText(newTitle);
    Container parent = getRootPane().getParent();
    if (parent instanceof ResultFrame) {
      ResultFrame rsf = (ResultFrame)parent;
      rsf.setTitle(newTitle);
    } else if (parent instanceof ClientFramework) {
    } else {
      framework.debug(1, "Error: Parent instance of: " + 
                      parent.getClass().getName());
    }
 
    // Update the record count
    recordCountLabel.setText(results.getRowCount() + " data packages");

    // Notify the JTable that the TableModel changed a bunch!
    table.setModel(results);
    initColumnSizes(table, results);
  }

  /**
   * Load the saved queries into the Search menu so that the user can launch
   * any queries they saved from previosu sessions.
   */
  public void loadSavedQueries()
  {
    ClientFramework.debug(20, "Loading saved queries...");
    // See if the query list is null, and initialize it if so
    if (savedQueriesList == null) {
      savedQueriesList = new Hashtable();
    }

    // Make sure the list is empty (because this may be called when the
    // profile is being switched)
    if (!savedQueriesList.isEmpty()) {
      for (int i = savedQueriesList.size()+1; i > 1; i--) {
        // Clear the search menu too 
        framework.removeMenuItem("Search", i);
      }
      savedQueriesList = new Hashtable();
    }

    // Look in the profile queries directory and load any pathquery docs
    ConfigXML config = framework.getConfiguration();
    ConfigXML profile = framework.getProfile();
    String queriesDirName = config.getConfigDirectory() + File.separator +
                            config.get("profile_directory", 0) +
                            File.separator +
                            config.get("current_profile", 0) +
                            File.separator +
                            profile.get("queriesdir", 0); 
    File queriesDir = new File(queriesDirName);
    if (queriesDir.exists()) {
//DFH      File[] queriesList = queriesDir.listFiles();
      File[] queriesList = listFiles(queriesDir);
      for (int n=0; n < queriesList.length; n++) {
        File queryFile = queriesList[n];
        if (queryFile.isFile()) {
          try {
            FileReader xml = new FileReader(queryFile);
            Query newQuery = new Query(xml, framework);
            addQueryToMenu(newQuery);
          } catch (FileNotFoundException fnf) {
            ClientFramework.debug(9, "Poof. The query disappeared.");
          }
        }
      }
    }
    ClientFramework.debug(20, "Finished loading saved queries.");
  }

  /**
   * Add a new menu item to the Search menu for the query
   *
   * @param query the query to be added to the Search menu
   */
  private void addQueryToMenu(Query query)
  {
    // See if the query list is null, and initialize it if so
    if (savedQueriesList == null) {
      savedQueriesList = new Hashtable();
    }

    // Add a menu item in the framework to execute this query, but only
    // if the menu item doesn't already exist, which is determined
    // by seeing if the query identifier is in the static list of queries
    if (! savedQueriesList.containsKey(query.getIdentifier())) {
      Action[] menuActions = new Action[1];
      Action savedSearchItemAction = 
             new AbstractAction(query.getQueryTitle()) {
        public void actionPerformed(ActionEvent e) {
 //DFH - following lines disabled because donot work with Java 1.17
          Action queryAction = ((JMenuItem)e.getSource()).getAction();
          Query savedQuery = (Query)queryAction.getValue("SAVED_QUERY_OBJ");
          if (savedQuery != null) {
            ResultSet rs = savedQuery.execute();
            ResultFrame rsf = new ResultFrame(framework, rs);
          }
        }
      };
      savedSearchItemAction.putValue("SAVED_QUERY_OBJ", query);
      savedSearchItemAction.putValue(Action.SHORT_DESCRIPTION, 
                            "Execute saved search");
      menuActions[0] = savedSearchItemAction;
      framework.addMenu("Search", new Integer(3), menuActions);
      savedQueriesList.put(query.getIdentifier(), savedSearchItemAction);
    } else {
      // The menu already exists, so update its title and query object
      Action savedQueryAction = 
             (Action)savedQueriesList.get(query.getIdentifier());
      savedQueryAction.putValue(Action.NAME, query.getQueryTitle());
      savedQueryAction.putValue("SAVED_QUERY_OBJ", query);
    }
  }
  
  private File[] listFiles(File dir) {
    String[] fileStrings = dir.list();
    int len = fileStrings.length;
    File[] list = new File[len];
    for (int i=0; i<len; i++) {
        list[i] = new File(dir, fileStrings[i]);    
    }
    return list;
  }
  


private void doUpload() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          bflyLabel.setIcon(flapping);
          threadCount++;
          
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceProvider provider = 
                     framework.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            framework.debug(6, "Error in upload");
            return null;
          }
          
          { //upload the current selection to metacat
            ClientFramework.debug(20, "Uploading package.");
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
                  framework.debug(0, mue2.getMessage());
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
           recordCountLabel.setText(results.getRowCount() + " data packages");    
           bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3

  
}
 
  
private void doDownload() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          bflyLabel.setIcon(flapping);
          threadCount++;
 
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceProvider provider = 
                     framework.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            framework.debug(6, "Error in upload");
            return null;
          }
          
          
        //download the current selection to the local disk
        ClientFramework.debug(20, "Downloading package.");
        dataPackage.download(docid);
        refreshQuery();
          
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           recordCountLabel.setText(results.getRowCount() + " data packages");    
           bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}

private void doDeleteLocal() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          bflyLabel.setIcon(flapping);
          threadCount++;
          
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceProvider provider = 
                     framework.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            framework.debug(6, "Error in upload");
            return null;
          }
          
          
		    //delete the local package
        ClientFramework.debug(20, "Deleteing the local package.");
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
           recordCountLabel.setText(results.getRowCount() + " data packages");    
           bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}

private void doDeleteMetacat() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          bflyLabel.setIcon(flapping);
          threadCount++;
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceProvider provider = 
                     framework.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            framework.debug(6, "Error in upload");
            return null;
          }
          
          
			   //delete the object on metacat
         ClientFramework.debug(20, "Deleteing the metacat package.");
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
           recordCountLabel.setText(results.getRowCount() + " data packages");    
           bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}
  
private void doDeleteAll() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          bflyLabel.setIcon(flapping);
          threadCount++;
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceProvider provider = 
                     framework.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            framework.debug(6, "Error in upload");
            return null;
          }
          
			  //delete both of the objects
        ClientFramework.debug(20, "Deleting both copies of the package.");
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
           recordCountLabel.setText(results.getRowCount() + " data packages");    
           bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}

private void doExport() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          bflyLabel.setIcon(flapping);
          threadCount++;
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceProvider provider = 
                     framework.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            framework.debug(6, "Error in upload");
            return null;
          }
          
			  //do export
          ClientFramework.debug(20, "Exporting dataset");
          exportDataset(docid);

          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           recordCountLabel.setText(results.getRowCount() + " data packages");    
           bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}

private void doExportToZip() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          bflyLabel.setIcon(flapping);
          threadCount++;
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceProvider provider = 
                     framework.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            framework.debug(6, "Error in upload");
            return null;
          }
          
			  //do exportToZip
          ClientFramework.debug(20, "Exporting dataset to zip file");
          exportDatasetToZip(docid);

          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           recordCountLabel.setText(results.getRowCount() + " data packages");    
           bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}

private void doOpenDataPackage() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          bflyLabel.setIcon(flapping);
          threadCount++;
          
          String docid = selectedId;
          DataPackageInterface dataPackage;
          try 
          {
            ServiceProvider provider = 
                     framework.getServiceProvider(DataPackageInterface.class);
            dataPackage = (DataPackageInterface)provider;
          } 
          catch (ServiceNotHandledException snhe) 
          {
            framework.debug(6, "Error in upload");
            return null;
          }
          
			  //do open
          ClientFramework.debug(20, "Opening package.");
          openResultRecord(table);
        
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           recordCountLabel.setText(results.getRowCount() + " data packages");    
           bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}
 
  
private void doRefreshQuery() {
  final SwingWorker worker = new SwingWorker() {
        public Object construct() {
          bflyLabel.setIcon(flapping);
          threadCount++;
          refreshQuery();
        
          return null;  
        }

        //Runs on the event-dispatching thread.
        public void finished() {
          threadCount--;
          if (threadCount<1) {
           recordCountLabel.setText(results.getRowCount() + " data packages");    
           bflyLabel.setIcon(bfly);
          }
        }
    };
    worker.start();  //required for SwingWorker 3
}
  
  
}
