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
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import edu.ucsb.nceas.morpho.framework.AbstractUIPage;
import edu.ucsb.nceas.morpho.plugins.DataPackageWizardInterface;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WidgetFactory;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardContainerFrame;
import edu.ucsb.nceas.morpho.plugins.datapackagewizard.WizardSettings;
import edu.ucsb.nceas.morpho.util.Log;
import edu.ucsb.nceas.morpho.util.Util;
import edu.ucsb.nceas.utilities.OrderedMap;

/**
 * This page will display some entity information and data table itself for text importing.
 * The metadata information is entityName and entityDescription.
 * @author tao
 *
 */
public class TextImportEntity extends AbstractUIPage 
{
	   private String pageID = DataPackageWizardInterface.TEXT_IMPORT_ENTITY;
	   private String title = "Text Import";
	   private String subTitle = null;
	   private String pageNumber = null;
	   private WizardContainerFrame frame = null;
	   private ImportedTextFile textFile = null;
	   private boolean isTextFile = true;
	   private JTable linesTable = null;
	   private String shortFileName = null;
	   
	   /**
	    * Construct
	    * @param frame
	    */
	   public TextImportEntity(WizardContainerFrame frame)
	   {
		   if(frame == null)
		   {
			   Log.debug(30, "The WizardContainerFrame is null and we can't initialize TexImpoortEntity");
			   return;
		   }
		   this.frame = frame;	   
		   textFile = frame.getImportDataTextFile();
		   buildLinesTable();
		   nextPageID = DataPackageWizardInterface.TEXT_IMPORT_DELIMITERS;
		   init();
	   }
	   
	   /*
	    * Build linesTable which will be displayed on data panel
	    */
	   private boolean buildLinesTable()
	   {
		   boolean success = true;
		   if (textFile == null && textFile.getDataFile() == null )
		   {
			   Log.debug(30, "The imported data file is null and we can't initialize TexImpoortEntity");
			   return false;
		   }
		   Log.debug(35, "The data file from previous page is ============= "+textFile.getDataFile().getAbsolutePath());		   
		   textFile.parsefile();		   
		   textFile.createLinesTable();
		   linesTable = textFile.getLinesTable();
		   return success;
	   }
	   
	   /*
	    * Init the panels
	    */
	   private void init()
	   {
		    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		    JPanel vbox = this;
		    JLabel desc1 = WidgetFactory.makeHTMLLabel(
		    	      "<p><b>This set of screens will create metadata based on the content of the specified data file</b></p>", 1);
		    vbox.add(desc1);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    
		    JPanel namePanel = WidgetFactory.makePanel(1);	   
		    nameLabel = WidgetFactory.makeLabel("Title:", true);
		    namePanel.add(nameLabel);
		    TableNameTextField = WidgetFactory.makeOneLineTextField();
		    if(textFile != null)
		    {
		       TableNameTextField.setText(textFile.getShortFilename());
		    }
		    namePanel.add(TableNameTextField);
		    namePanel.setBorder(new javax.swing.border.EmptyBorder(WizardSettings.PADDING, 0, 0,
		            WizardSettings.PADDING));
		    vbox.add(namePanel);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    
		    JPanel tableDescriptionPanel = WidgetFactory.makePanel(1);	   
		    JLabel Step1_TableDescriptionLabel = WidgetFactory.makeLabel("Description:", false);
		    tableDescriptionPanel.add(Step1_TableDescriptionLabel);
		    TableDescriptionTextField = WidgetFactory.makeOneLineTextField();
		    tableDescriptionPanel.add(TableDescriptionTextField);
		    tableDescriptionPanel.setBorder(new javax.swing.border.EmptyBorder(WizardSettings.PADDING, 0, 0,
		            WizardSettings.PADDING));
		    vbox.add(tableDescriptionPanel);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    
		    JPanel StartingLinePanel=  WidgetFactory.makePanel(1);	
		    JLabel StartingLineLabel = WidgetFactory.makeLabel("Start import at row: ", false, new Dimension(120,20));
		    StartingLinePanel.add(StartingLineLabel);
		    StartingLineTextField = WidgetFactory.makeOneLineShortTextField("1");
		    StartingLinePanel.add(StartingLineTextField);
		    JLabel blank = WidgetFactory.makeLabel("      ",false);
		    StartingLinePanel.add(blank);
		    ColumnLabelsCheckBox = WidgetFactory.makeCheckBox("Column Labels are in starting row", false);
		    ColumnLabelsCheckBox.setActionCommand("Column Labels are in starting row");
		    ColumnLabelsCheckBox.setSelected(false);
		    StartingLinePanel.add(ColumnLabelsCheckBox);	    
		    vbox.add(StartingLinePanel);
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    vbox.add(WidgetFactory.makeDefaultSpacer());
		    //vbox.add(WidgetFactory.makeDefaultSpacer());
		    
		    DataScrollPanel.getViewport().removeAll();
		    DataScrollPanel.getViewport().add(linesTable);
		    vbox.add(DataScrollPanel);
	   }
	   
	   /*
	    * Update data panel
	    */
	   private void updateDataScrollPanel()
	   {
		   DataScrollPanel.getViewport().removeAll();
		   DataScrollPanel.getViewport().add(linesTable);
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
		  if(frame != null)
		  {
			  Log.debug(35, "The beginning of the onLoadAction");
			  ImportedTextFile newFile = frame.getImportDataTextFile();
			  boolean isSame = textFile.equals(newFile);
			  if(!isSame)
			  {
				  //File object was changed we need to update both data file and data panel
				  textFile = newFile;
				  buildLinesTable();
				  updateDataScrollPanel();
				  //set default table name only when data file was changed
				  if(textFile != null)
				  {
					  TableNameTextField.setText(textFile.getShortFilename());
				  }
			  }
			 
			  Log.debug(35, "The status of isTextFile is ========"+isTextFile);
		  }
	  }


	  /**
	   *  The action to be executed when the "Prev" button is pressed. May be empty
	   *
	   */
	  public  void onRewindAction()
	  {
		  WidgetFactory.unhiliteComponent(nameLabel);
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
		  if(Util.isBlank(TableNameTextField.getText()))
		  {
			 WidgetFactory.hiliteComponent(nameLabel);
			 TableNameTextField.requestFocus();
			 return false;  
		  }
		  else
		  {
			 WidgetFactory.unhiliteComponent(nameLabel);
		     return true;
		  }
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
	  
	  
	  private JLabel nameLabel = null;
	  private JTextField TableNameTextField = new JTextField();
	  private JTextField TableDescriptionTextField = new JTextField();
	  private JTextField StartingLineTextField = new JTextField();
	  private JCheckBox ColumnLabelsCheckBox = new JCheckBox();
	  private JScrollPane DataScrollPanel = new JScrollPane();

}
