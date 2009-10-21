/**
 *  '$RCSfile: CitationPage.java,v $'
 *    Purpose: A class that handles display of Citation Information
 *  Copyright: 2000 Regents of the University of California and the
 *             National Center for Ecological Analysis and Synthesis
 *    Release: @release@
 *
 *   '$Author: tao $'
 *     '$Date: 2009-04-24 23:06:55 $'
 * '$Revision: 1.29 $'
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

package edu.ucsb.nceas.morpho.plugins.datapackagewizard.pages;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardPageLibrary;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * This page will display some entity information and data table itself for text importing.
 * The metadata information is entityName and entityDescription.
 * @author tao
 *
 */
public class TextImportAttribute extends AbstractUIPage 
{
	   public static final int FIRSTINDEX = 0;
	   private String pageID = DataPackageWizardInterface.TEXT_IMPORT_FIRST_ATTRIBUTE;
	   private String title = "Text Import";
	   private String subTitle = null;
	   private String pageNumber = null;
	   private WizardContainerFrame frame = null;
	   private ImportedTextFile textFile = null;
	   private WizardPageLibrary wizardPageLib = null;
	   private int columnIndex = 0;
	   private JTable table = null;
	   // Column Model of the table containting all the columns
		private TableColumnModel fullColumnModel = null;
	   
	   
	   /**
	    * Construct
	    * @param frame
	    */
	   public TextImportAttribute(WizardContainerFrame frame,int columnIndex)
	   {
		   this.frame = frame;
		   if(this.frame == null)
		   {
			   Log.debug(5, "The WizardContainerFrame is null and we can't initialize TexImportAttribute");
		       return;
			 
		   }
		   else
		   {
			   textFile = frame.getImportDataTextFile();
			   if(textFile == null)
			   {
				 
				   Log.debug(5, "The TextFile object is null and we can't initialize TexImportAttribute");
				   return;
				   
			   }
			  
		   }
		   this.wizardPageLib = new WizardPageLibrary(frame);
		   this.columnIndex = columnIndex;
		   init();
	   }
	   
	   /*
	    * Sets up table model
	    */
	   private void setupTableModel()
	   {
		   
		   this.table = textFile.getTable();
		   this.fullColumnModel = textFile.getFullColumnModel();
		   if(table != null && fullColumnModel != null)
		   {
			   TableColumnModel model = new DefaultTableColumnModel();
			   model.addColumn(fullColumnModel.getColumn(columnIndex));
			   DefaultListSelectionModel dlsm = new DefaultListSelectionModel();
			   dlsm.setSelectionInterval(0, 0);
			   model.setColumnSelectionAllowed(true);
			   model.setSelectionModel(dlsm);
			   table.setColumnModel(model);
			   table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			   table.doLayout();
		   }
		   
	   }
	   
	   /*
	    * Init the panels
	    */
	   private void init()
	   {
		    this.setLayout(new BorderLayout());
		    JPanel vbox = this;
		    columnDataScrollPanel.setPreferredSize(new Dimension(80, 4000));
		    //updateColumnDataPanel()
		    vbox.add(columnDataScrollPanel, BorderLayout.WEST);
		    AttributePage attributePage = (AttributePage) wizardPageLib.getPage(DataPackageWizardInterface.ATTRIBUTE_PAGE);
		    vbox.add(attributePage, BorderLayout.CENTER);
	   }
	   
	   /*
	    * Update data model and data panel
	    */
	   private void updateColumnDataPanel()
	   {
		   setupTableModel();
		   repaintColumnDataPanel();
	   }
	   
	   /*
	    * Force update data panel
	    */
	   private void repaintColumnDataPanel()
	   {
		   Log.debug(32, "The column data panel in TextTimportAttribute class is updating ===========");
		   columnDataScrollPanel.getViewport().removeAll();
		   columnDataScrollPanel.getViewport().add(table);
		   //columnDataScrollPanel.setVisible(true);
	   }
	  
	   /**
	   *  gets the unique ID for this UI page
	   *
	   *  @return   the unique ID String for this UI page
	   */
	  public String getPageID()
	  {
		 return pageID; 
	  }
	  
	  /**
	   *  Sets the unique ID for this UI page
	   *
	   *  @return   the unique ID String for this UI page
	   */
	  public void setPageID(String pageID)
	  {
		 this.pageID =pageID; 
	  }


	  /**
	   *  gets the title for this UI page
	   *
	   *  @return   the String title for this UI page
	   */
	  public String getTitle()
	  {
		  return title;
	  }


	  /**
	   *  gets the subtitle for this UI page
	   *
	   *  @return   the String subtitle for this UI page
	   */
	  public String getSubtitle()
	  {
		  return subTitle;
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
		  return nextPageID;
	  }
	  
	  /**
	   * Sets a dynamic id for next pageID
	   *
	   *  @return the String ID of the page that the user will see next, or null if
	   *  this is te last page
	   */
	  public void setNextPageID(String nextPageID)
	  {
		  this.nextPageID = nextPageID;
	  }


	  /**
	   *  Returns the serial number of the page
	   *
	   *  @return the serial number of the page
	   */
	  public String getPageNumber()
	  {
		  return pageNumber;
	  }


	  /**
	   *  The action to be executed when the page is displayed. May be empty
	   */
	  public void onLoadAction()
	  {
		  Log.debug(30, "The TextImportAttribute page has the index "+columnIndex);
		  updateColumnDataPanel();
	  }


	  /**
	   *  The action to be executed when the "Prev" button is pressed. May be empty
	   *
	   */
	  public  void onRewindAction()
	  {
		  if (table != null && columnIndex == FIRSTINDEX )
		  {
			  table.setColumnModel(fullColumnModel);
			  table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			  table.doLayout();
		  }
	  }


	  /**
	   *  The action to be executed when the "Next" button (pages 1 to last-but-one)
	   *  or "Finish" button(last page) is pressed. May be empty
	   *
	   *  @return boolean true if wizard should advance, false if not
	   *          (e.g. if a required field hasn't been filled in)
	   */
	  public boolean onAdvanceAction()
	  {
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
		  OrderedMap map = null;
		  return map;
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
		  OrderedMap map = null;
		  return map;
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
		  return true;
	  }
	  
	  
	  
	 
	  private JScrollPane columnDataScrollPanel = new JScrollPane();

}
