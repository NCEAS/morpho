/**
 *  '$RCSfile: ExternalRefsPage.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2004-04-05 21:33:48 $'
 * '$Revision: 1.5 $'
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
import edu.ucsb.nceas.morpho.util.SortableJTable;
import edu.ucsb.nceas.morpho.util.Log;

import edu.ucsb.nceas.utilities.OrderedMap;
import edu.ucsb.nceas.utilities.XMLUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;


import org.w3c.dom.Node;

public class ExternalRefsPage extends AbstractUIPage
{

  private ReferencesHandler referenceHandler;
  private SortableJTable table;
  private JList idList;
  private ColumnSortableTableModel resultsModel;
  private ReferenceSelectionEvent event;
  private String refID;
  private String currentDataPackageID;
  private AbstractDataPackage currentDataPackage;
  private Node referencedSubtree;
  private QueryRefreshInterface queryRefreshInterface;
  // select columns
  private String[] columnNames = {QueryRefreshInterface.TITLE,
                                  QueryRefreshInterface.DOCID};
  private static final int DOCIDINDEX = 1;

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

  protected void setCurrentDataPackageID(String currentDataPackageID)
  {

    this.currentDataPackageID = currentDataPackageID;
  }

  private void init()
  {

    this.setLayout(new BorderLayout());

    table = new SortableJTable();
    doQueryAndPopulateDialog();
    JScrollPane scroll = new JScrollPane(table);
    scroll.getViewport().setBackground(Color.white);
    this.add(scroll, BorderLayout.WEST);

    idList = new JList();
    JPanel refsPanel = new JPanel();
    refsPanel.setLayout(new BoxLayout(refsPanel, BoxLayout.Y_AXIS));
    refsPanel.add(idList);

    refsPanel.setOpaque(true);
    refsPanel.setBackground(Color.green);

    this.add(refsPanel, BorderLayout.CENTER);
  }

  /*
   * Method to add mouse listener to the table
   */
   private void addingMouseListenerForSearchResultTable()
   {
     // Listen for mouse events to see if the user double-clicks
     table.addMouseListener(new MouseAdapter()
     {
       public void mouseClicked(MouseEvent e)
       {
         int selectedRow = table.getSelectedRow();
         currentDataPackageID =
                 (String) table.getModel().getValueAt(selectedRow, DOCIDINDEX);
         // create a data package base on selected docid
         // because we only search the local, so set metacat = false
         boolean metacat = false;
         boolean local   = true;
         currentDataPackage = DataPackageFactory.
                           getDataPackage(currentDataPackageID, metacat, local);
         parsingPackageIntoList(currentDataPackage);
       }
     });
   }//addingMouseListener

  /*
   * Method to parse selected data package into a list
   */
  private void parsingPackageIntoList(AbstractDataPackage selectedPackage)
  {
     String id = null;
     List content = referenceHandler.getReferences(selectedPackage, id);
     //tansfer a List to vector and vector will be the data model of JList
     Iterator iterator = content.iterator();
     Vector listDataVector = new Vector();
     while (iterator.hasNext())
     {
       ReferenceMapping mapping = (ReferenceMapping) iterator.next();
       if (mapping != null)
       {
         listDataVector.add(mapping);
       }
     }
     idList.setListData(listDataVector);
     // set single selection mode
     idList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     // add list selection listener
     /*idList.addListSelectionListener
     ( new ListSelectionListener ()
          {
            public void valueChanged(ListSelectionEvent e)
            {
               //selected row number in list
               int selectedListRow = e.getLastIndex();
               Log.debug(20, "The selected row number in list is "
                         + selectedListRow);


            }
          }

      );*/
      idList.validate();
      idList.repaint();

   }

  /**
   * Run the local search query
   *
   * @return boolean
   */
  private boolean doQueryAndPopulateDialog()
  {

    final QueryRefreshInterface queryPlugin = getQueryPlugin();

    if (queryPlugin == null) return false;


    final SwingWorker worker = new SwingWorker() {



      public Object construct() {

        resultsModel = queryPlugin.doOwnerQueryForCurrentUser(columnNames);

        return null;
      }


      //Runs on the event-dispatching thread.
      public void finished()
      {
        setQueryResults(resultsModel);
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
  public void setQueryResults(ColumnSortableTableModel model)
  {

    table.setModel(model);
    table.validate();
    table.repaint();
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
    ReferenceMapping map = (ReferenceMapping) idList.getSelectedValue();
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
      Log.debug(30, "The external package is " + currentDataPackageID);
      Log.debug(30, "The reference id in external package is " + refID);
      // get subtee from current selected package
      referencedSubtree = currentDataPackage.getSubtreeAtReference(refID);
    }
    event.setReferenceID(refID);
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


}
