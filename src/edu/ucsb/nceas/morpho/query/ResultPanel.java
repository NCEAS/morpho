/**
 *  '$RCSfile: ResultPanel.java,v $'
 *  Copyright: 2000 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *    Authors: @authors@
 *    Release: @release@
 *
 *   '$Author: jones $'
 *     '$Date: 2001-06-13 07:25:45 $'
 * '$Revision: 1.15 $'
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

import edu.ucsb.nceas.morpho.framework.ClientFramework;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableColumn;

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
  JButton refreshButton;

  /** Button used to trigger a revision using QueryDialog */
  JButton reviseButton;

  /** Button used to trigger a save of the current Query */
  JButton saveButton;

  /** The label used to display the query title */
  JLabel titleLabel;

  /** The label used to display the number of records */
  JLabel recordCountLabel;

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

    setLayout(new BorderLayout());
    setBackground(Color.white);
    setPreferredSize(new Dimension(775,500));

    if (results != null) {
      setName(results.getQuery().getQueryTitle());

      // Set up the Header panel with a title and refresh/revise buttons
      titleLabel = new JLabel(results.getQuery().getQueryTitle());
      titleLabel.setForeground(Color.black);
      titleLabel.setFont(new Font(null, Font.BOLD, 18));
      Box headerBox = Box.createHorizontalBox();
      headerBox.setBackground(Color.white);
      headerBox.add(Box.createHorizontalStrut(8));
      headerBox.add(titleLabel);
      headerBox.add(Box.createHorizontalGlue());
      recordCountLabel = new JLabel(results.getRowCount() + " data sets");
      recordCountLabel.setForeground(Color.black);
      recordCountLabel.setFont(new Font(null, Font.BOLD, 18));
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
      table.setRowHeight((int)(stringRenderer.getPreferredSize().getHeight()));
      //table.setRowHeight(results.getRowHeight());
      table.setDefaultRenderer(String.class, stringRenderer);
      initColumnSizes(table, results);
  
      //Create the scroll pane and add the table to it. 
      JScrollPane scrollPane = new JScrollPane(table);
  
      //Add the scroll pane to this Panel.
      add(scrollPane, BorderLayout.CENTER);
    
      // Listen for mouse events to see if the user double-clicks
      table.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          if (2 == e.getClickCount()) {
            openResultRecord(table);
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
        refreshQuery();
      } else if (object == saveButton) {
        saveQuery();
      }
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
    Query query = results.getQuery();
    String identifier = query.getIdentifier();
    if (identifier == null) {
      identifier = framework.getNextId();
      query.setIdentifier(identifier);
    }

    try {
      query.save();

      // Add a menu item in the framework to execute this query
      Action[] menuActions = new Action[1];
      Action savedSearchItemAction = new AbstractAction(query.getQueryTitle()) {
        public void actionPerformed(ActionEvent e) {
          /*
          if (query != null) {
            ResultSet rs = query.execute();
            ResultFrame rsf = new ResultFrame(framework, rs);
          }
          */
        }
      };
      savedSearchItemAction.putValue(Action.SHORT_DESCRIPTION, 
                            "Execute saved search");
      menuActions[0] = savedSearchItemAction;
      framework.addMenu("Search", new Integer(3), menuActions);
    } catch (IOException ioe) {
      ClientFramework.debug(6, "Failed to saved query: I/O error.");
      ClientFramework.debug(6, ioe.getMessage());
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
    recordCountLabel.setText(results.getRowCount() + " data sets");

    // Notify the JTable that the TableModel changed a bunch!
    table.setModel(results);
    initColumnSizes(table, results);
  }
}
