/**
 *  '$RCSfile: ResultPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2002-08-30 01:22:31 $'
 * '$Revision: 1.56 $'
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
  private JMenuItem openPreviousVersion = null;

  private JMenuItem uploadMenu = null;
  private JMenuItem downloadMenu = null;
  private JMenuItem deleteLocalMenu = null;
  private JMenuItem deleteMetacatMenu = null;
  private JMenuItem deleteAllMenu = null;
  private JMenuItem refreshMenu = null;
  private JMenuItem exportMenu = null;
  private JMenuItem exportToZipMenu = null;
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
  
  private int vers = -1;
  private String packageName = "";
  

  
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
                            new OpenPackageCommand(dialog));
      openMenu = new JMenuItem(openAction);
      popup.add(openMenu);
      
      // Create a OpenPreviousVersion action
      GUIAction openPreviousAction = new GUIAction("Open Previous Version",null,
                            new OpenPreviousVersionCommand(dialog, null));
      openPreviousVersion = new JMenuItem(openPreviousAction);
      popup.add(openPreviousVersion);
      openPreviousAction.setEnabled(false);
      
      // Create a refresh action
      GUIAction refreshAction = new GUIAction("Refresh", null, 
                               new RefreshCommand(dialog));
      refreshMenu = new JMenuItem(refreshAction);
      popup.add(refreshMenu);
      
      popup.add(new JSeparator());
      
      // Create a new JMenu SynChronize...
      JMenu synchronize = new JMenu("Synchronize...");
      // Create a upload action and add it to synchronize
      GUIAction uploadAction = new GUIAction("Local to network", null,
                                new LocalToNetworkCommand(dialog));
      uploadMenu = new JMenuItem(uploadAction);
      synchronize.add(uploadMenu);
      // Create a download action
      GUIAction downloadAction = new GUIAction("Network to Local", null,
                       new NetworkToLocalCommand(dialog));
      downloadMenu = new JMenuItem(downloadAction);
      synchronize.add(downloadMenu);
      // Add synchronize to pop
      popup.add(synchronize);
      popup.add(new JSeparator());
      
      // Create a new JMenu Delete
      JMenu delete = new JMenu("Delete...");
      GUIAction deleteLocalAction = new GUIAction("Local", null,
                         new DeleteCommand(dialog, DataPackageInterface.LOCAL));
      deleteLocalMenu = new JMenuItem(deleteLocalAction);
      delete.add(deleteLocalMenu);
      GUIAction deleteNetworkAction = new GUIAction("Network", null,
                       new DeleteCommand(dialog, DataPackageInterface.METACAT));
      deleteMetacatMenu = new JMenuItem(deleteNetworkAction);
      delete.add(deleteMetacatMenu);
      GUIAction deleteBothAction = new GUIAction("Both", null,
                       new DeleteCommand(dialog, DataPackageInterface.BOTH));
      deleteAllMenu = new JMenuItem(deleteBothAction);
      delete.add(deleteAllMenu);
      popup.add(delete);
      popup.add(new JSeparator());
      
      // Create export
      GUIAction exportAction = new GUIAction("Export...", null, 
                            new ExportCommand(dialog, ExportCommand.REGULAR));
      exportMenu = new JMenuItem(exportAction);
      popup.add(exportMenu);
      GUIAction exportToZipAction = new GUIAction("Export to Zip...", null, 
                            new ExportCommand(dialog, ExportCommand.ZIP));
      exportToZipMenu = new JMenuItem(exportToZipAction);
      popup.add(exportToZipMenu);
      
    
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
    OpenPackageCommand open = new OpenPackageCommand(dialog);
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
  
  /**
   * Get the package name (docid without version). This method is for 
   * openPreviousVersionCommand
   */
  public String getPackageName()
  {
    return packageName;
  }
  
  /**
   * Get the version number. This method is for OpenPrevisouVrsionCommand
   */
  public int getVersion()
  {
    return vers;
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

 
  
}
