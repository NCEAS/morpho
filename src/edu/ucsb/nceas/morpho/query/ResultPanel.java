/**
 *  '$RCSfile: ResultPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-04-19 20:44:50 $'
 * '$Revision: 1.70 $'
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import edu.ucsb.nceas.morpho.Language;
import edu.ucsb.nceas.morpho.Morpho;
import edu.ucsb.nceas.morpho.datapackage.AccessionNumber;
import edu.ucsb.nceas.morpho.datastore.DataStoreInterface;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.util.GUIAction;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.SortTableCommand;
import edu.ucsb.nceas.morpho.util.SortableJTable;
import edu.ucsb.nceas.morpho.util.StateChangeEvent;
import edu.ucsb.nceas.morpho.util.StateChangeMonitor;
import edu.ucsb.nceas.morpho.util.StoreStateChangeEvent;


/**
 * Display a ResultSet in a table view in a panel that can be
 * embedded in a window or tab or other location
 */
public class ResultPanel extends JPanel implements StoreStateChangeEvent
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
  /** vector to store the state change event */
  Vector storedStateChangeEventlist = null;

  /** Button used to trigger a re-execution of the query */
//  private JButton refreshButton;
//  /** Button used to trigger a revision using QueryDialog */
//  private JButton reviseButton;
//  /** Button used to trigger a save of the current Query */
//  private JButton saveButton;
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
  private JMenuItem synchronizeMenu = null;
  private JMenuItem uploadMenu = null;
  private JMenuItem downloadMenu = null;
  private JMenuItem deleteMenu = null;
  private JMenuItem refreshMenu = null;
  private JMenuItem exportMenu = null;
  private JMenuItem exportToZipMenu = null;
  /**a string to keep track of the selected row's id*/
  private String selectedId = "";
  /**the location of the data package*/
  private String metacatStatus = null;
  private String localStatus = null;
  private String doctype = "";
  private Dimension preferredSize;

  ImageIcon bfly;
  ImageIcon flapping;
  int threadCount = 0;
  int selectedRow = -1;

  private int vers = -1;
  private String packageName = "";

  //this variable to indicate disable the mouse listener in streaming search
  private boolean enableMouseListener = true;
  
  //private boolean listCrashedDoc = false;


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

  
  /*public ResultPanel(OpenDialogBox dialog, ResultSet results,
          ResultPanelAndFrameMediator myMediator, boolean disableRightClick)
{
    this(dialog, results, 12, myMediator, new Dimension(775,500));
}*/
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
    //this.listCrashedDoc = listCrashedDocs;
    storedStateChangeEventlist = new Vector();
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

      // In open dialog, doesn't show popup
      //if (dialog == null)
      //{
        //Build the popup menu for the right click functionality
       //if (!listCrashedDoc)
       {
	        popup = new JPopupMenu();
	        // Create a openPackage action
	        GUIAction openAction = new GUIAction(/*"Open Package"*/ Language.getInstance().getMessage("OpenPackage"), null,
	                            new OpenPackageCommand(dialog));
	        openMenu = new JMenuItem(openAction);
	        popup.add(openMenu);
	
	        // Create a OpenPreviousVersion action
	        GUIAction openPreviousAction = new GUIAction(/*"Open Previous Version"*/ Language.getInstance().getMessage("OpenPreviousVersion") ,
	        					null,
	                            new OpenPreviousVersionCommand(dialog, null));
	        /*openPreviousAction.setEnabledOnStateChange(
	            StateChangeEvent.SEARCH_RESULT_SELECTED_VERSIONS, 
	            true, GUIAction.EVENT_LOCAL);
	        openPreviousAction.setEnabledOnStateChange(
              StateChangeEvent.SEARCH_RESULT_SELECTED_NO_VERSIONS,
              false, GUIAction.EVENT_LOCAL);*/
	        openPreviousVersion = new JMenuItem(openPreviousAction);
	        popup.add(openPreviousVersion);
	        openPreviousAction.setEnabled(false);
	
	        // Create a refresh action
	        GUIAction refreshAction = null;
	        if (dialog != null)
	        {
	          // refresh open dialog
	          refreshAction = new GUIAction(/*"Refresh"*/ Language.getInstance().getMessage("Refresh") ,
	        		  						null,
	                                                new RefreshCommand(dialog));
	        }
	        else
	        {
	          // refresh current acitve frame
	          refreshAction = new GUIAction(/*"Refresh"*/ Language.getInstance().getMessage("Refresh"),
	        		  							null, new RefreshCommand());
	        }
	        refreshMenu = new JMenuItem(refreshAction);
	        popup.add(refreshMenu);
	
	        popup.add(new JSeparator());
	
	        // Create a action to open a synchronize dialog
	        GUIAction synchronizeAction = new GUIAction(/*"Synchronize..."*/ Language.getInstance().getMessage("Synchronize") + Language.getInstance().getMessage("..."), 
	        								null,
	                                      new OpenSynchronizeDialogCommand(dialog));
	        /*synchronizeAction.setEnabledOnStateChange(
	            StateChangeEvent.SEARCH_RESULT_SELECTED_UNSYNCHRONIZED,
	            true, GUIAction.EVENT_LOCAL);
	        synchronizeAction.setEnabledOnStateChange(
              StateChangeEvent.SEARCH_RESULT_SELECTED_SYNCHRONIZED,
              false, GUIAction.EVENT_LOCAL);*/
	        synchronizeMenu = new JMenuItem(synchronizeAction);
	        popup.add(synchronizeMenu);
	
	
	        popup.add(new JSeparator());
	
	        // Create a action to open a delete dialog
	        GUIAction openDeleteDialogAction = new GUIAction(
	        		/*"Delete..."*/ Language.getInstance().getMessage("Delete") + Language.getInstance().getMessage("..."), 
	        		null,
	        		new OpenDeleteDialogCommand(dialog));
	        deleteMenu = new JMenuItem(openDeleteDialogAction);
	        popup.add(deleteMenu);
	
	        popup.add(new JSeparator());
	
	        // Create export
	        GUIAction exportAction = new GUIAction(
	        		/*"Export..."*/ Language.getInstance().getMessage("Export") + Language.getInstance().getMessage("..."), 
	        		null,
	        		new OpenExportDialogCommand(dialog));
	        /*exportAction.setEnabledOnStateChange(
	            StateChangeEvent.CHOOSE_COMPLETE_DATAPACKAGE, 
	            true, GUIAction.EVENT_LOCAL);
	        exportAction.setEnabledOnStateChange(
	            StateChangeEvent.CHOOSE_INCOMPLETE_DATAPACKAGE,
	            false, GUIAction.EVENT_LOCAL);*/
	        exportMenu = new JMenuItem(exportAction);
	        popup.add(exportMenu);
	//        GUIAction exportToZipAction = new GUIAction("Export to Zip...", null,
	  //                            new ExportCommand(dialog, ExportCommand.ZIP));
	//                             new OpenExportDialogCommand(dialog));
	//        exportToZipMenu = new JMenuItem(exportToZipAction);
	//        popup.add(exportToZipMenu);
      //}//if
       }


      MouseListener popupListener = new PopupListener(this);
      table.addMouseListener(popupListener);

      // Listen for mouse events to see if the user double-clicks
      table.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          if (enableMouseListener)
          {
            //if (2 == e.getClickCount() && !listCrashedDoc) {
            if (2 == e.getClickCount()) {
              //doOpenDataPackage();
              // Using OpenPackageCommand to open package
              doDoubleClickOpen();
            }
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
    open.execute(null);
  }// doDoubleClickOpen

  /**
   * Get the result set from ResultPanel
   */
  public ResultSet getResultSet()
  {
    return results;
  }//getResultSet
  

  /**
   * Set the result set for ResultPanel and repaint table
   */
  public void setResultSet(ResultSet result)
  {
     Vector newVector = result.getResultsVector();
     resetResultsVector(newVector);
  }//setResultSet

 /**
  * A method to reset a vector as result, repaint table
  * @param newResultVector Vector
  */
  public void resetResultsVector (Vector newResultVector)
  {
    results.setResultsVector(newResultVector);
    updateTable();
  }

  /*
   * Update the table model and repaint table after results changed
   */
  private void updateTable()
  {
    table.resetModel(results);
    results.fireTableDataChanged();
  }

  /**
   * Get the Jable
   */
  public SortableJTable getJTable()
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
   *  get the doctype
   */
  public String getDocType()
  {
    return doctype;
  }

  /**
   * Get the metacatLoc
   */
  public boolean getMetacatLocation()
  {
    if(metacatStatus != null && metacatStatus.equals(QueryRefreshInterface.NETWWORKCOMPLETE))
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * Get the local location
   */
  public boolean getLocalLocation()
  {
    if(localStatus != null && localStatus.equals(QueryRefreshInterface.LOCALCOMPLETE))
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  
  /**
   * Gets the localStatus as a String. 
   * @return
   */
  public String getLocalStatus()
  {
    return localStatus;
  }
  
  /**
   * Gets the metacatStatus as a string
   * @return
   */
  public String getMetacatStatus()
  {
    return metacatStatus;
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
  public int getPreviousVersions()
  {
    return vers;
  }

  /**
   * Enable or disable mouse listner
   * @param enable boolean  the value of true or false
   */
  public void setEnableMouseListener(boolean enable)
  {
    enableMouseListener = enable;
  }

  /**
   * Get current of value of enable mouse listener
   * @return boolean
   */
  public boolean getEnableMouseListener()
  {
    return enableMouseListener;
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
   * Method to sort the table in result panel
   * @param index the index of column need to be sort
   * @param order the order need to be sort
   */
  public void sortTable(int index, String order)
  {
    // create an instance of SortTableCommmand
    SortTableCommand sortCommand = new SortTableCommand(table, index, order);
    sortCommand.execute(null);
  }//sortTable

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
   * Return true if the synchronize menu need be enable
   */
   private boolean enableSynchronizeMenu()
   {
     if ((localStatus == null && metacatStatus != null && metacatStatus.equals(QueryRefreshInterface.NETWWORKCOMPLETE)) ||
         (metacatStatus == null && localStatus != null && localStatus.equals(QueryRefreshInterface.LOCALCOMPLETE))||
         (localStatus != null && localStatus.equals(DataStoreInterface.NONEXIST) && metacatStatus != null && metacatStatus.equals(QueryRefreshInterface.NETWWORKCOMPLETE))||
         (metacatStatus != null && metacatStatus.equals(DataStoreInterface.NONEXIST) && localStatus.equals(QueryRefreshInterface.LOCALCOMPLETE)))
     {
       return true;
     }
     else
     {
       return false;
     }
         
   }
   
   /*
    * Return true if user selected an incomplete document row.
    */
   private boolean selectedIncompleteDocument()
   {
     if(localStatus != null && 
         (localStatus.equals(QueryRefreshInterface.LOCALAUTOSAVEDINCOMPLETE) || 
         localStatus.equals(QueryRefreshInterface.LOCALUSERSAVEDINCOMPLETE)))    
     {
       return true;
     }
     else
     {
       return false;
     }
   }
   
   

  class PopupListener extends MouseAdapter {
    // on the Mac, popups are triggered on mouse pressed, while mouseReleased triggers them
    // on the PC; use the trigger flag to record a trigger, but do not show popup until the
    // mouse released event (DFH)
    boolean trigger = false;
    String docid = "";
    ResultPanel pane = null;

    public PopupListener(ResultPanel panel)
    {
      pane = panel;
    }

    public void mousePressed(MouseEvent e)
    {
      if(enableMouseListener)
      {
        //select the clicked row first
        table.clearSelection();
        int selrow = table.rowAtPoint(new Point(e.getX(), e.getY()));
        selectedRow = selrow;
        table.setRowSelectionInterval(selrow, selrow);
        Vector resultV = results.getResultsVector();
        Vector rowV = (Vector) resultV.elementAt(selrow);

        // If the panel need a mediator
        if (mediator != null) {
          // If select a row, open button will enable
          mediator.enableOpenButton();
        }

        docid = (String) rowV.elementAt(6);
        selectedId = docid;
        Log.debug(30, "selectedId is: " + docid);
        //localLoc = ( (Boolean) rowV.elementAt(9)).booleanValue();
        localStatus = (String) rowV.elementAt(ResultSet.ISLOCALINDEX);
        //metacatLoc = ( (Boolean) rowV.elementAt(10)).booleanValue();
        metacatStatus = (String) rowV.elementAt(ResultSet.ISMETACATINDEX);
        packageName = AccessionNumber.getInstance().getIdNoRev(selectedId);
        Log.debug(30, "the package name is: " + packageName);
        vers = AccessionNumber.getInstance().getNumberOfPrevVersions(selectedId);
        Log.debug(30, "the number of previous version is: " + vers);
        doctype = (String) rowV.elementAt(8);
        // Fire state change event only in morpho frame
        if (dialog == null) 
        {

          StateChangeMonitor monitor = StateChangeMonitor.getInstance();
          // select a data package event
          monitor.notifyStateChange(
              new StateChangeEvent(
              pane,
              StateChangeEvent.SEARCH_RESULT_SELECTED));
          if (enableSynchronizeMenu()) {
            // unsynchronized package
            //Log.debug(5, "enable synchorize menu");
            monitor.notifyStateChange(
                new StateChangeEvent(
                pane,
                StateChangeEvent.SEARCH_RESULT_SELECTED_UNSYNCHRONIZED));
          }
          else {
            //Log.debug(5, "disable synchorize menu");
            // synchronized package
            monitor.notifyStateChange(
                new StateChangeEvent(
                pane,
                StateChangeEvent.SEARCH_RESULT_SELECTED_SYNCHRONIZED));
          }
          
          if(selectedIncompleteDocument())
          {
            //Log.debug(5, "disable export");
            monitor.notifyStateChange(
                new StateChangeEvent(
                pane, 
                StateChangeEvent.CHOOSE_INCOMPLETE_DATAPACKAGE));
          }
          else
          {
            //Log.debug(5, "enable export");
            monitor.notifyStateChange(
                new StateChangeEvent(
                 pane,
                 StateChangeEvent.CHOOSE_COMPLETE_DATAPACKAGE));
                
          }

          if (vers > 0) {
            // mutipleversion package
            //Log.debug(5, "enable open previous");
            monitor.notifyStateChange(
                new StateChangeEvent(
                pane,
                StateChangeEvent.SEARCH_RESULT_SELECTED_VERSIONS));
          }
          else {
            // one version package
            //Log.debug(5, "disable open previous");
            monitor.notifyStateChange(
                new StateChangeEvent(
                pane,
                StateChangeEvent.SEARCH_RESULT_SELECTED_NO_VERSIONS));
          }

        } // if dialg == null

        if (e.isPopupTrigger()) {
          trigger = true;
        }
      }//if enable
    }

    public void mouseReleased(MouseEvent e)
    {
        if (enableMouseListener)
        {
          maybeShowPopup(e);
        }

    }

    private void maybeShowPopup(MouseEvent e)
    {
      if(e.isPopupTrigger() || trigger)
      {
        if (vers>0) {
          openPreviousVersion.setEnabled(true);
        }
        else {
          openPreviousVersion.setEnabled(false);
        }

        synchronizeMenu.setEnabled
                      (enableSynchronizeMenu());
        exportMenu.setEnabled(!selectedIncompleteDocument());
        // delete menu always enable
        deleteMenu.setEnabled(true);
        trigger = false;
        popup.show(e.getComponent(), e.getX(), e.getY());

      }
    }

  }

}
