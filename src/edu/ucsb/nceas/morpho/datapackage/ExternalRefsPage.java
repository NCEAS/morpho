/**
 *  '$RCSfile: ExternalRefsPage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-04-07 01:55:47 $'
 * '$Revision: 1.12 $'
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

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.framework.QueryRefreshInterface;
import edu.ucsb.nceas.morpho.framework.SwingWorker;
import edu.ucsb.nceas.morpho.plugins.ServiceController;
import edu.ucsb.nceas.morpho.plugins.ServiceNotHandledException;
import edu.ucsb.nceas.morpho.util.ColumnSortableTableModel;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.SortableJTable;
import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.w3c.dom.Node;

public class ExternalRefsPage extends AbstractUIPage
{

  private ReferencesHandler referenceHandler;
  private SortableJTable dataPackageTable;
  private JTable referenceIdTable;
  private JList idList;
  private ColumnSortableTableModel resultsModel;
  private ReferenceSelectionEvent event;
  private String refID;
  private Vector refIDTableCloumnName = new Vector();
  private String currentDataPackageID;
  private String selectedDataPackageID;
  private AbstractDataPackage currentDataPackage;
  private AbstractDataPackage selectedDataPackage;
  private Node referencedSubtree;
  private QueryRefreshInterface queryRefreshInterface;
  // select columns
  private String[] columnNames = {QueryRefreshInterface.TITLE,
                                  QueryRefreshInterface.DOCID};

  // constant
  private static final int       DOCIDINDEX = 1;
  private static final String SELECTPACKAGE = "1) Select a data package";
  private static final int           TOPGAP = 0;
  private static final int        BOTTOMGAP = 8;
  private static final int       BIGSIDEGAP = 16;
  private static final int     SMALLSIDEGAP = 8;
  private static final int        LABELTOOM = 10;

  ExternalRefsPage(ReferencesHandler referenceHandler)
  {
    this.referenceHandler = referenceHandler;

    init();
    addingMouseListenerForSearchResultTable();
   }



  protected void setReferenceSelectionEvent(ReferenceSelectionEvent event)
  {
    this.event = event;
  }


  protected ReferenceSelectionEvent getReferenceSelectionEvent() {
    return event;
  }


  protected void setCurrentDataPackageID(String currentDataPackageID) {

    this.currentDataPackageID = currentDataPackageID;
  }


  private void init() {

    this.setLayout(new BorderLayout());

    //left panel is data package panel
    JPanel packageListPanel = new JPanel();
    packageListPanel.setBorder(BorderFactory.createEmptyBorder
                               (TOPGAP, BIGSIDEGAP, BOTTOMGAP, SMALLSIDEGAP));
    packageListPanel.setLayout(new BorderLayout());
    JLabel selectPackageLabel = new JLabel(SELECTPACKAGE);
    selectPackageLabel.setBorder(BorderFactory.createEmptyBorder
                               (0, 0, LABELTOOM, 0));
    packageListPanel.add(selectPackageLabel, BorderLayout.NORTH);
    dataPackageTable = new SortableJTable();



    JScrollPane scroll = new JScrollPane(dataPackageTable);
    Dimension scrollDimension = scroll.getPreferredSize();
    doQueryAndPopulateDialog(new Double(scrollDimension.getWidth()).intValue());
    scroll.getViewport().setBackground(Color.white);
    packageListPanel.add(scroll, BorderLayout.CENTER);

    this.add(packageListPanel, BorderLayout.WEST);


    // right panel is the reference id panel
    JPanel refsPanel = new JPanel();
    String selectRefsString = "2) Select a previous entry from this data package";
    JLabel selectedRefsLabel = new JLabel(selectRefsString);
    selectedRefsLabel.setBorder(BorderFactory.createEmptyBorder
                               (0, 0, LABELTOOM, 0));
    refsPanel.setBorder(BorderFactory.createEmptyBorder(
                        TOPGAP, SMALLSIDEGAP, BOTTOMGAP, BIGSIDEGAP));
    refsPanel.setLayout(new BorderLayout());
    refsPanel.add(selectedRefsLabel, BorderLayout.NORTH);
    referenceIdTable = new JTable(null, refIDTableCloumnName);
    JScrollPane scrollPanel = new JScrollPane(referenceIdTable);
    scrollPanel.getViewport().setBackground(Color.white);
    refsPanel.add(scrollPanel);

    this.add(refsPanel, BorderLayout.CENTER);
  }





  /**
   * Method to set displayName for the ExternalRefsDialog
   * @param String displayName for the ExternalRefsDialog
   */
  public void setDisplayName(String displayName) {

    if (displayName==null) displayName = "";
    // add the displayName into the column name vector in reference id
    refIDTableCloumnName.clear();
    refIDTableCloumnName.add(displayName);
    referenceIdTable.validate();
    referenceIdTable.repaint();
  }


  /*
   * Method to add mouse listener to the table
   */
   private void addingMouseListenerForSearchResultTable()
   {
     // Listen for mouse events to see if the user double-clicks
     dataPackageTable.addMouseListener(new MouseAdapter()
     {
       public void mouseClicked(MouseEvent e)
       {
         int selectedRow = dataPackageTable.getSelectedRow();
         selectedDataPackageID = (String)
                dataPackageTable.getModel().getValueAt(selectedRow, DOCIDINDEX);
         // create a data package base on selected docid
         // because we only search the local, so set metacat = false
         boolean metacat = false;
         boolean local   = true;
         selectedDataPackage = DataPackageFactory.
                          getDataPackage(selectedDataPackageID, metacat, local);
         parsingPackageIntoTable(selectedDataPackage);
       }
     });
   }//addingMouseListener

  /*
   * Method to parse selected data package into a table
   */
  private void parsingPackageIntoTable(AbstractDataPackage selectedPackage)
  {
     String id = null;
     List content = referenceHandler.getReferences(selectedPackage, id);
     //tansfer a List to vector of vector, the vetor will be the data model
     //of jtable
     Iterator iterator = content.iterator();
     Vector dataVector = new Vector();

     while (iterator.hasNext())
     {
       Vector rowDataVector = new Vector();
       ReferenceMapping mapping = (ReferenceMapping) iterator.next();
       if (mapping != null)
       {
         rowDataVector.add(mapping);
         dataVector.add(rowDataVector);
       }
     }
     // sort dataVector
     if (!dataVector.isEmpty())
     {
       Collections.sort(dataVector, new ReferenceComparator());
     }
      //new data model for table
     DefaultTableModel referenceIdModel = new DefaultTableModel();
     referenceIdModel.setDataVector(dataVector, refIDTableCloumnName);
     referenceIdTable.setModel(referenceIdModel);
     referenceIdTable.validate();
     referenceIdTable.repaint();
   }

  /**
   * Run the local search query
   *
   * @return boolean
   */
  private boolean doQueryAndPopulateDialog(final int width)
  {

    final QueryRefreshInterface queryPlugin = getQueryPlugin();

    if (queryPlugin == null) return false;


    final SwingWorker worker = new SwingWorker() {



      public Object construct() {

        resultsModel = queryPlugin.doOwnerQueryForCurrentUser(
                                   columnNames, currentDataPackageID);

        return null;
      }


      //Runs on the event-dispatching thread.
      public void finished()
      {
        setQueryResults(resultsModel, width);
      }
    };
    worker.start(); //required for SwingWorker 3
    return true;
  }//doQuery


  private QueryRefreshInterface getQueryPlugin()
  {

    if (queryRefreshInterface == null)
    {

      ServiceController sc;
      //QueryRefreshInterface queryRefreshInterface = null;
      try
      {
        sc = ServiceController.getInstance();
        queryRefreshInterface = (QueryRefreshInterface)sc.getServiceProvider(
            QueryRefreshInterface.class);
      }
      catch (ServiceNotHandledException se)
      {
        Log.debug(6, se.getMessage());
        se.printStackTrace();
      }
    }
    return queryRefreshInterface;
  }



  /**
   * sets the AbstractTableModel to be used as the basis of the query results
   * listing (shows all packages owned by current user on local system)
   *
   * @param model AbstractTableModel to be used as basis of query results
   * listing (shows all packages owned by current user on local system)
   */
  private void setQueryResults(ColumnSortableTableModel model, int width)
  {

    dataPackageTable.setModel(model);
    setTableColumnSize(width, dataPackageTable);
    dataPackageTable.validate();
    dataPackageTable.repaint();
  }

  /*
   * Method to setup table's column size
   */
  private void setTableColumnSize(int width, JTable table)
  {
    double [] columnWidth = {0.75, 0.25};
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


    for (int i = 0; i < columnNames.length; i++)
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
     // Set preferred width
     column.setPreferredWidth(preferredSize);
     // Set minimum width
     column.setMinWidth(minimumSize);
     // Set maxmum width
     column.setMaxWidth(maxmumSize);
   }//for

  }


  /**
   *  gets the unique ID for this UI page
   *
   *  @return   the unique ID String for this UI page
   */
  public String getPageID()
  {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getPageID() method not implemented!");
  }


  /**
   *  gets the title for this UI page
   *
   *  @return   the String title for this UI page
   */
  public String getTitle()
  {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getTitle() method not implemented!");
  }



  /**
   *  gets the subtitle for this UI page
   *
   *  @return   the String subtitle for this UI page
   */
  public String getSubtitle()
  {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getSubtitle() method not implemented!");
  }



  /**
   *  Returns the ID of the page that the user will see next, after the "Next"
   *  button is pressed. If this is the last page, return value must be null
   *
   *  @return the String ID of the page that the user will see next, or null if
   *  this is te last page
   */
  public String getNextPageID()
  {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getNextPageID() method not implemented!");
  }



  /**
   *  Returns the serial number of the page
   *
   *  @return the serial number of the page
   */
  public String getPageNumber()
  {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getPageNumber() method not implemented!");
  }



  /**
   *  The action to be executed when the page is displayed. May be empty
   */
  public void onLoadAction() {}


  /**
   *  The action to be executed when the "Prev" button is pressed. May be empty
   *
   */
  public void onRewindAction() {}


  /**
   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
   *  or "Finish" button(last page) is pressed. May be empty
   *
   *  @return boolean true if wizard should advance, false if not
   *          (e.g. if a required field hasn't been filled in)
   */
  public boolean onAdvanceAction()
  {
    ReferenceMapping map = null;
    int selectedRowInReferencedTable = referenceIdTable.getSelectedRow();
    // because referenceIdtable has only one column, so column =0;
    if (selectedRowInReferencedTable != -1)
    {
      map = (ReferenceMapping)
          referenceIdTable.getValueAt(selectedRowInReferencedTable, 0);
    }
    // get refID
    if (map != null)
    {
      refID = map.getID();
    }
    if (refID==null)
    {
      return false;
    }
    else
    {
      Log.debug(30, "The external package is " + selectedDataPackageID);
      Log.debug(30, "The reference id in external package is " + refID);
      // get subtee from current selected package
      referencedSubtree = selectedDataPackage.getSubtreeAtReference(refID);
    }
    event.setReferenceID(refID);
    event.setLocation(ReferenceSelectionEvent.DIFFERENT_DATA_PACKAGE);
    event.setSubtreeRootNodeName(referencedSubtree.getNodeName());
    event.setXPathValsMap(XMLUtilities.getDOMTreeAsXPathMap(referencedSubtree));
    return true;
  }



  /**
   *  gets the Map object that contains all the key/value paired
   *  settings for this particular UI page
   *
   *  @return   data the Map object that contains all the
   *            key/value paired settings for this particular UI page
   */
  public OrderedMap getPageData()
  {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getPageData() method not implemented!");
  }


  /**
   * gets the Map object that contains all the key/value paired settings for
   * this particular UI page
   *
   * @param rootXPath the root xpath to prepend to all the xpaths returned by
   *   this method
   * @return data the Map object that contains all the key/value paired
   *   settings for this particular UI page
   */
  public OrderedMap getPageData(String rootXPath)
  {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> getPageData(rootXPath) method not implemented!");
  }



  /**
   * sets the fields in the UI page using the Map object that contains all
   * the key/value paired
   *
   * @param data the Map object that contains all the key/value paired settings
   *   for this particular UI page
   * @param rootXPath the String that represents the "root" of the XPath to the
   *   content of this widget, INCLUDING PREDICATES. example - if this is a
   *   "Party" widget, being used for the second "Creator" entry in a list,
   *   then xPathRoot = "/eml:eml/dataset[1]/creator[2]
   * @return boolean true if this page can handle all the data passed in the
   * OrderedMap, false if not. <em>NOTE that the setPageData() method should
   * still complete its work and fill out all the UI values, even if it is
   * returning false</em>
   */
  public boolean setPageData(OrderedMap data, String rootXPath)
  {
    throw new UnsupportedOperationException(
      "ExternalRefsPage -> setPageData() method not implemented!");
  }

  // new class for sorting the reference id
  class ReferenceComparator implements Comparator
  {

    public int compare(Object object1, Object object2)
    {
      int results = -1;
      Vector vector1 = (Vector) object1;
      Vector vector2 = (Vector) object2;
      ReferenceMapping mapping1 = (ReferenceMapping) vector1.elementAt(0);
      ReferenceMapping mapping2 = (ReferenceMapping) vector2.elementAt(0);
      String surrogate1 = mapping1.toString();
      String surrogate2 = mapping2.toString();
      return (surrogate1.compareToIgnoreCase(surrogate2));
    }
  }

}
